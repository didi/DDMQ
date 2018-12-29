package carrera

import (
	"encoding/json"
	"time"

	"carrera/CarreraProducer"
	"carrera/common/util"
	log "go.intra.xiaojukeji.com/golang/commons/dlog"
	"strings"

	"errors"
	"carrera/pool"
	"go.intra.xiaojukeji.com/golang/go.uuid"
	"go.intra.xiaojukeji.com/golang/thrift-lib/0.9.2"
	"math"
	"carrera/common/qlog"
	"carrera/common"
)

const (
	OK                      = 0
	CACHE_OK                = 1
	CACHE_FAIL              = 2
	CACHE_FAIL_CLOSED       = 3
	CACHE_FAIL_BEYOND_RETRY = 4

	FAIL_UNKNOWN                 = 10
	FAIL_ILLEGAL_MSG             = 11
	FAIL_TIMEOUT                 = 12
	FAIL_TOPIC_NOT_ALLOWED       = 13
	FAIL_TOPIC_NOT_EXIST         = 14
	FAIL_REFUSED_BY_RATE_LIMITER = 15

	DOWNGRADE          = 100
	CLIENT_EXCEPTION   = 101
	MISSING_PARAMETERS = 102
	DPOOL_EXCEPTION    = 103

	DELAY_ACTIONS_ADD    int32 = 1
	DELAY_ACTIONS_CANCEL int32 = 2
)

type Producer struct {
	carreraConfig *CarreraConfig

	running   bool
	closeChan chan bool

	dpoolMap map[string]*pool.DPool
}

func checkParams(conf *CarreraConfig) {
	if conf.carreraProxyList == nil || len(conf.carreraProxyList) == 0 {
		panic("Invalid Local Mode params, carreraProxyList is empty!")
	}

	if conf.carreraPoolSize <= 0 {
		panic("Invalid Config params, carreraPoolSize must greater than 0!")
	}
}

func newProducer(conf *CarreraConfig) *Producer {
	checkParams(conf)

	producer := &Producer{
		carreraConfig: conf,
		closeChan:     make(chan bool),

		dpoolMap: make(map[string]*pool.DPool),
	}

	producer.dpoolMap[localModeMapKey] = newCarreraDPool(conf.carreraProxyList, conf)

	return producer
}

func init() {
	// for default
	qlog.InitLog(10, 10)
}

func (self *Producer) start() {
	log.Info("producer start")
	self.running = true
}

func (self *Producer) shutdown() {
	log.Info("producer shutting down")

	for _, dpool := range self.dpoolMap {
		dpool.Shutdown()
	}

	close(self.closeChan)
	self.running = false
	log.Info("producer shutdown")

}

func (self *Producer) getConnection(topic string) (*PooledConnection, error) {
	if dpool, ok := self.dpoolMap[localModeMapKey]; ok {
		if conn, err := dpool.Get(self.carreraConfig.carreraClientTimeout); err != nil {
			return nil, err
		} else {
			return conn.(*PooledConnection), nil
		}
	} else {
		return nil, errors.New("dpool get conn, don't have " + localModeMapKey + " dpool")
	}
}

func (self *Producer) returnConnection(topic string, conn *PooledConnection, broken bool) error {
	if dpool, ok := self.dpoolMap[localModeMapKey]; ok {
		return dpool.Put(conn, broken)
	} else {
		return errors.New("dpool return conn, don't have " + localModeMapKey + " dpool")
	}
}

func (self *Producer) sendMessage(msg *CarreraProducer.Message) *CarreraProducer.Result {
	begin := time.Now().UnixNano() / 1000000
	var result = "failure"
	var proxyAddr = ""
	var ret = &CarreraProducer.Result{
		Code: CLIENT_EXCEPTION,
	}

	retryCount := 0
	costMap := make(map[int]int64)
	for ; retryCount <= self.carreraConfig.carreraClientRetry; retryCount++ {
		_begin := time.Now().UnixNano() / 1000000
		proxyAddr, ret = self.sendImpl(msg)
		_end := time.Now().UnixNano() / 1000000
		costMap[retryCount] = _end - _begin
		ret.Key = &msg.Key
		used := _end - _begin
		if ret.Code > CACHE_OK {
			result = "failure"
			time.Sleep(time.Duration(math.Max(float64(self.carreraConfig.carreraProxyTimeout-used), 0)) * time.Millisecond)
			continue
		}

		if ret.Code == OK {
			result = "success"
		} else if ret.Code == CACHE_OK {
			result = "cache_ok"
		}
		break
	}
	end := time.Now().UnixNano() / 1000000

	if ret.Code > CACHE_OK {
		log.Errorf("send msg result: %s; msg[ip:%s,topic:%s,key:%s,partition:%v,hashID:%v,len:%d,used:%v,sendUsed:%v,retryCount:%d,ret.code:%d,ret.msg:%s]", result, proxyAddr, msg.Topic, msg.Key, msg.PartitionId, msg.HashId, len(msg.Body), end-begin, costMap, retryCount, ret.Code, ret.Msg)
		reason, _ := json.Marshal(ret)
		message, _ := json.Marshal(msg)
		qlog.DROP_LOGGER.Infof("REASON:%s,CARRERA_MESSAGE:%s", reason, message)
	}
	return ret
}

func (self *Producer) sendWithPartition(topic string, partitionId int32, hashId int64, body []byte, key, tags string) *CarreraProducer.Result {
	if !self.running {
		return &CarreraProducer.Result{
			Code: CLIENT_EXCEPTION,
			Msg:  "please execute the Start() method before sending the message",
			Key:  &key,
		}
	}

	return self.sendMessage(self.buildMessage(topic, partitionId, hashId, body, key, tags))
}

func (self *Producer) sendImpl(msg *CarreraProducer.Message) (string, *CarreraProducer.Result) {
	ret := &CarreraProducer.Result{
		Code: CLIENT_EXCEPTION,
	}
	conn, err := self.getConnection(msg.GetTopic())
	if err != nil {
		log.Errorf("get conn error, topic=%v, key=%v, err=%v", msg.Topic, msg.Key, err.Error())

		ret.Code = DPOOL_EXCEPTION
		ret.Msg = err.Error()
		return "", ret
	}

	begin := time.Now().UnixNano()
	ret, err = conn.client.SendSync(msg, self.carreraConfig.carreraProxyTimeout)
	if err != nil {
		used := (time.Now().UnixNano() - begin) / 1000000
		log.Errorf("send msg error, addr=%s, topic=%v, key=%v, err=%v, cost=%v", conn.addr, msg.Topic, msg.Key, err.Error(), used)
		self.returnConnection(msg.GetTopic(), conn, true)
		return conn.addr, &CarreraProducer.Result{
			Code: CLIENT_EXCEPTION,
			Msg:  err.Error(),
		}
	}

	self.returnConnection(msg.GetTopic(), conn, false)
	return conn.addr, ret
}

func (self *Producer) buildMessage(topic string, partitionId int32, hashId int64, body []byte, key, tags string) *CarreraProducer.Message {
	msg := CarreraProducer.NewMessage()
	msg.Topic = topic
	msg.PartitionId = partitionId
	msg.HashId = hashId
	msg.Tags = tags
	curSdkVersion := common.CUR_SDK_VERSION
	msg.Version = &curSdkVersion
	msg.Body = body

	if strings.EqualFold("", key) {
		msg.Key = self.genRandKey()
	} else {
		msg.Key = key
	}
	return msg
}

func (self *Producer) genRandKey() string {
	return util.GenRandKey()
}

func (self *Producer) sendDelay(topic string, body []byte, delayMeta *CarreraProducer.DelayMeta, tags string) *CarreraProducer.DelayResult {
	return self.sendDelayMessage(self.buildDelayMessage4Add(topic, body, delayMeta, tags))
}

func (self *Producer) cancelDelay(topic, uniqDelayMsgId, tags string) *CarreraProducer.DelayResult {
	return self.sendDelayMessage(self.buildDelayMessage4Cancel(topic, uniqDelayMsgId, tags))
}

func (self *Producer) sendDelayMessage(delayMessage *CarreraProducer.DelayMessage) *CarreraProducer.DelayResult {
	if !self.running {
		return &CarreraProducer.DelayResult{
			Code:           CLIENT_EXCEPTION,
			Msg:            "please execute the Start() method before sending the message",
			UniqDelayMsgId: "",
		}
	}

	begin := time.Now().UnixNano() / 1000000
	var result = "failure"
	var proxyAddr = ""
	var ret = &CarreraProducer.DelayResult{
		Code: CLIENT_EXCEPTION,
	}

	uuid := ""
	if delayMessage.Uuid != nil {
		uuid = *(delayMessage.Uuid)
	}

	retryCount := 0
	for ; retryCount <= self.carreraConfig.carreraClientRetry; retryCount++ {
		_begin := time.Now().UnixNano() / 1000000
		proxyAddr, ret = self.sendDelayImpl(delayMessage)
		_end := time.Now().UnixNano() / 1000000

		used := (time.Now().UnixNano() / 1000000) - _begin
		if ret.Code > CACHE_OK {
			result = "failure"
			time.Sleep(time.Duration(math.Max(float64(self.carreraConfig.carreraProxyTimeout-used), 0)) * time.Millisecond)
			log.Debugf("send delay msg result:%s; msg[ip:%s,topic:%s,uuid:%s,len:%d,used:%v,retryCount:%d]", result, proxyAddr, delayMessage.Topic, uuid, len(delayMessage.Body), _end-_begin, retryCount)
			continue
		}

		if ret.Code == OK {
			result = "success"
		} else if ret.Code == CACHE_OK {
			result = "cache_ok"
		}
		log.Debugf("send delay msg result:%s; msg[ip:%s,topic:%s,uuid:%s,len:%d,used:%v,retryCount:%d]", result, proxyAddr, delayMessage.Topic, uuid, len(delayMessage.Body), _end-_begin, retryCount)
		break
	}
	end := time.Now().UnixNano() / 1000000

	log.Infof("send delay msg result:%s; msg[ip:%s,topic:%s,uuid:%s,len:%d,used:%v,retryCount:%d]", result, proxyAddr, delayMessage.Topic, uuid, len(delayMessage.Body), end-begin, retryCount)
	if ret.Code > CACHE_OK {
		log.Errorf("send delay msg result:%s; msg[ip:%s,topic:%s,uuid:%s,len:%d,used:%v,retryCount:%d,ret.code:%d,ret.msg:%s]", result, proxyAddr, delayMessage.Topic, uuid, len(delayMessage.Body), end-begin, retryCount, ret.Code, ret.Msg)
	}
	return ret
}

func (self *Producer) sendDelayImpl(dMsg *CarreraProducer.DelayMessage) (string, *CarreraProducer.DelayResult) {
	ret := &CarreraProducer.DelayResult{
		Code: CLIENT_EXCEPTION,
	}
	conn, err := self.getConnection(dMsg.GetTopic())
	if err != nil {
		log.Debugf("get conn error, err=%v, msg=%v", err.Error(), dMsg.String())
		log.Errorf("get conn error, err=%v", err.Error())

		ret.Code = DPOOL_EXCEPTION
		ret.Msg = err.Error()
		return "", ret
	}

	ret, err = conn.client.SendDelaySync(dMsg, self.carreraConfig.carreraProxyTimeout)
	if err != nil {
		log.Debugf("send msg error, err=%v, msg=%v", err.Error(), dMsg.String())
		log.Errorf("send msg error, topic=%v, uuid=%v, err=%v", dMsg.GetTopic(), dMsg.GetUuid(), err.Error())
		self.returnConnection(dMsg.GetTopic(), conn, true)
		return conn.addr, &CarreraProducer.DelayResult{
			Code:           CLIENT_EXCEPTION,
			Msg:            err.Error(),
			UniqDelayMsgId: "",
		}
	}

	self.returnConnection(dMsg.GetTopic(), conn, false)
	return conn.addr, ret
}

func (self *Producer) buildDelayMessage4Add(topic string, body []byte, delayMeta *CarreraProducer.DelayMeta, tags string) *CarreraProducer.DelayMessage {
	dMsg := CarreraProducer.NewDelayMessage()
	dMsg.Topic = topic
	dMsg.Body = body
	dMsg.Action = DELAY_ACTIONS_ADD
	dMsg.Timestamp = thrift.Int64Ptr(delayMeta.Timestamp)
	dMsg.Dmsgtype = delayMeta.Dmsgtype
	dMsg.Interval = delayMeta.Interval
	dMsg.Expire = delayMeta.Expire
	dMsg.Times = delayMeta.Times
	dMsg.Uuid = thrift.StringPtr(uuid.NewV1().String())
	dMsg.Version = thrift.StringPtr(common.CUR_SDK_VERSION)
	dMsg.Tags = tags
	dMsg.Properties = delayMeta.Properties

	return dMsg
}

func (self *Producer) buildDelayMessage4Cancel(topic, uniqDelayMsgId, tags string) *CarreraProducer.DelayMessage {
	dMsg := CarreraProducer.NewDelayMessage()
	dMsg.Topic = topic
	dMsg.Body = []byte("c")
	dMsg.UniqDelayMsgId = thrift.StringPtr(uniqDelayMsgId)
	dMsg.Action = DELAY_ACTIONS_CANCEL
	dMsg.Version = thrift.StringPtr(common.CUR_SDK_VERSION)
	dMsg.Tags = tags

	return dMsg
}
