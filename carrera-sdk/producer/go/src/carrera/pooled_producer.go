package carrera

import (
	"bufio"
	"encoding/json"
	"io"
	"io/ioutil"
	"math"
	"os"
	"path/filepath"
	"regexp"
	"strings"
	"sync/atomic"
	"time"

	"carrera/CarreraProducer"
	"carrera/common/qlog"
	log "go.intra.xiaojukeji.com/golang/commons/dlog"
)

var dropLogFileRegexp = regexp.MustCompile("(drop)\\.log\\.[0-9]+")

const localModeMapKey = "carrera_local"

type PooledProducer struct {
	config   *CarreraConfig
	producer sender

	closeChan        chan bool
	closeRecoverChan chan bool
	closed           int32 //0 初始化 ，1running 2close
}

type sender interface {
	sendWithPartition(topic string, partitionId int32, hashId int64, body []byte, key, tags string) *CarreraProducer.Result
	sendMessage(msg *CarreraProducer.Message) *CarreraProducer.Result
	sendDelay(topic string, body []byte, delayMeta *CarreraProducer.DelayMeta, tags string) *CarreraProducer.DelayResult
	cancelDelay(topic, uniqDelayMsgId, tags string) *CarreraProducer.DelayResult
	shutdown()
	start()
}

func NewCarreraPooledProducer(conf *CarreraConfig) *PooledProducer {
	log.Infof("pooled producer receive params: %s", conf.String())
	return _newPooledProducer(conf, _newProducer)
}

func _newProducer(config *CarreraConfig) sender {
	return newProducer(config)
}
func _newPooledProducer(conf *CarreraConfig, f func(*CarreraConfig) sender) *PooledProducer {

	pooledProducer := &PooledProducer{
		config:           conf,
		closeChan:        make(chan bool),
		closeRecoverChan: make(chan bool),
		closed:           0,
	}

	pooledProducer.producer = f(conf)

	return pooledProducer
}

func (p *PooledProducer) Start() {
	if !atomic.CompareAndSwapInt32(&p.closed, 0, 1) {
		panic("Start Failed!")
	}
	p.producer.start()

	p.goCheckShutdown()

	if p.config.recoverFromDropLog {
		p.goIntervalRecoverFromDropLog()
	}
}

func (p *PooledProducer) goCheckShutdown() {
	go func() {
		defer func() {
			if err := recover(); err != nil {
				log.Errorf("Error while close channel: %v", err)
			}
		}()

		select {
		case <-p.closeChan:
			p.producer.shutdown()
			close(p.closeRecoverChan)
			close(p.closeChan)
			log.Info("shutdown pooled producer")
		}
	}()
}

func (p *PooledProducer) goIntervalRecoverFromDropLog() {
	ticker := time.NewTicker(p.config.recoverFromDropLogInterval)
	go func() {
		defer func() {
			if err := recover(); err != nil {
				log.Errorf("Error while recover data from drop log: %v", err)
			}
		}()
		for {
			select {
			case <-ticker.C:
				p.recoverFromDropLog()
				break
			case <-p.closeRecoverChan:
				return
			}
		}
	}()
}

func (p *PooledProducer) MessageBuilder() *MessageBuilder {
	return NewMessageBuilder(p.producer)
}

func (p *PooledProducer) AddDelayMessageBuilder() *AddDelayMessageBuilder {
	return NewAddDelayMessageBuilder(p.producer)
}

func (p *PooledProducer) CancelDelayMessageBuilder() *CancelDelayMessageBuilder {
	return NewCancelDelayMessageBuilder(p.producer)
}

func (p *PooledProducer) AddTxMonitorMessageBuilder(addDelayMessageBuilder *AddDelayMessageBuilder) *AddTxMonitorMessageBuilder {
	return NewAddTxMonitorMessageBuilder(addDelayMessageBuilder)
}

func (p *PooledProducer) CancelTxMonitorMessageBuilder(cancelDelayMessageBuilder *CancelDelayMessageBuilder) *CancelTxMonitorMessageBuilder {
	return NewCancelTxMonitorMessageBuilder(cancelDelayMessageBuilder)
}

func (p *PooledProducer) TxBusinessMessageBuilder(messageBuilder *MessageBuilder) *TxBusinessMessageBuilder {
	return NewTxBusinessMessageBuilder(messageBuilder)
}

func (p *PooledProducer) sendData(topic string, body []byte, tags []string) *CarreraProducer.Result {
	if len(tags) == 0 {
		return p.sendDataWithPartition(topic, PARTITION_RAND, 0, body, "", []string{TAG_ALL})
	} else {
		return p.sendDataWithPartition(topic, PARTITION_RAND, 0, body, "", []string{strings.Join(tags, "||")})
	}
}

func (p *PooledProducer) sendDataWithPartition(topic string, partitionId int32, hashId int64, body []byte, key string, tags []string) *CarreraProducer.Result {
	if atomic.LoadInt32(&p.closed) == 1 {
		return p.producer.sendWithPartition(topic, partitionId, hashId, body, key, strings.Join(tags, "||"))
	}

	retKey := key
	if len(key) == 0 {
		retKey = ""
	}
	ret := &CarreraProducer.Result{
		Code: CLIENT_EXCEPTION,
		Key:  &retKey,
	}
	if atomic.LoadInt32(&p.closed) == 0 {
		ret.Msg = "Please Start PooledProducer!"
		return ret
	} else if atomic.LoadInt32(&p.closed) == 2 {
		ret.Msg = "PooledProducer has closed!"
		return ret
	} else {
		ret.Msg = "PooledProducer unknown state!"
		return ret
	}
}

func (p *PooledProducer) Send(topic string, body string, tags ...string) *CarreraProducer.Result {
	return p.sendData(topic, []byte(body), tags)
}

func (p *PooledProducer) SendWithKey(topic string, body string, key string, tags ...string) *CarreraProducer.Result {
	return p.sendDataWithPartition(topic, PARTITION_RAND, 0, []byte(body), key, []string{strings.Join(tags, "||")})
}

func (p *PooledProducer) SendWithPartition(topic string, partitionId int32, hashId int64, body string, key string, tags ...string) *CarreraProducer.Result {
	return p.sendDataWithPartition(topic, partitionId, hashId, []byte(body), key, tags)
}

func (p *PooledProducer) SendBinaryData(topic string, body []byte, tags ...string) *CarreraProducer.Result {

	return p.sendData(topic, body, tags)
}

func (p *PooledProducer) SendBinaryDataWithPartition(topic string, partitionId int32, hashId int64, body []byte, key string, tags ...string) *CarreraProducer.Result {
	return p.sendDataWithPartition(topic, partitionId, hashId, body, key, tags)
}

func (p *PooledProducer) SendDelayBinary(topic string, body []byte, delayMeta *CarreraProducer.DelayMeta, tags ...string) *CarreraProducer.DelayResult {
	if atomic.LoadInt32(&p.closed) == 1 {
		return p.producer.sendDelay(topic, body, delayMeta, strings.Join(tags, "||"))
	}

	ret := &CarreraProducer.DelayResult{
		Code:           CLIENT_EXCEPTION,
		UniqDelayMsgId: "",
	}
	if atomic.LoadInt32(&p.closed) == 0 {
		ret.Msg = "Please Start PooledProducer!"
		return ret
	} else if atomic.LoadInt32(&p.closed) == 2 {
		ret.Msg = "PooledProducer has closed!"
		return ret
	} else {
		ret.Msg = "PooledProducer unknown state!"
		return ret
	}
}

func (p *PooledProducer) SendDelay(topic, body string, delayMeta *CarreraProducer.DelayMeta, tags ...string) *CarreraProducer.DelayResult {
	return p.SendDelayBinary(topic, []byte(body), delayMeta, tags...)
}

func (p *PooledProducer) CancelDelay(topic, uniqDelayMsgId string, tags ...string) *CarreraProducer.DelayResult {
	if atomic.LoadInt32(&p.closed) == 1 {
		return p.producer.cancelDelay(topic, uniqDelayMsgId, strings.Join(tags, "||"))
	}

	ret := &CarreraProducer.DelayResult{
		Code:           CLIENT_EXCEPTION,
		UniqDelayMsgId: "",
	}
	if atomic.LoadInt32(&p.closed) == 0 {
		ret.Msg = "Please Start PooledProducer!"
		return ret
	} else if atomic.LoadInt32(&p.closed) == 2 {
		ret.Msg = "PooledProducer has closed!"
		return ret
	} else {
		ret.Msg = "PooledProducer unknown state!"
		return ret
	}
}

func (p *PooledProducer) Shutdown() {
	if !atomic.CompareAndSwapInt32(&p.closed, 1, 2) {
		return
	}
	p.closeChan <- true
	<-p.closeRecoverChan
}

func (p *PooledProducer) recoverFromDropLog() {
	logPath := filepath.Join(qlog.GetLogPath(), "/drop")
	log.Infof("start to recover from drop log, path:%s", logPath)
	fileInfos, err := ioutil.ReadDir(logPath)
	if err != nil {
		log.Errorf("Error while read drop files: %v", err)
		return
	}
	for _, fileInfo := range fileInfos {
		if dropLogFileRegexp.FindString(fileInfo.Name()) != "" {
			log.Infof("Start to recover file: %s", fileInfo.Name())
			p._recoverFromDropLog(logPath, fileInfo.Name())
			log.Infof("End to recover file: %s", fileInfo.Name())
		}
	}
	log.Info("recover from drop log ends")
}

func (p *PooledProducer) _recoverFromDropLog(logPath, filename string) {
	filePath := filepath.Join(logPath, filename)
	file, err := os.Open(filePath)
	if err != nil {
		log.Errorf("Error while open drop log file: %s, err: %v", filePath, err)
		return
	}
	defer func() {
		if err := file.Close(); err != nil {
			log.Errorf("Error while close drop log file: %s, err: %v", filePath, err)
		}
		if err := os.Remove(filePath); err != nil {
			log.Errorf("Error while remove drop log file: %s, err: %v", filePath, err)
		}
	}()
	buf := bufio.NewReader(file)
	for {
		line, err := buf.ReadString('\n')
		if err != nil {
			if err == io.EOF {
				break
			}
			log.Errorf("Error while read drop log file: %s, err: %v", filename, err)
		}
		reasonStart, reasonEnd := strings.Index(line, "REASON:")+len("REASON:"), strings.Index(line, ",CARRERA_MESSAGE:")
		messageStart := reasonEnd + len(",CARRERA_MESSAGE:")
		reason := line[reasonStart:reasonEnd]
		message := line[messageStart:]
		ret := &CarreraProducer.Result{}
		msg := &CarreraProducer.Message{}
		if json.Unmarshal([]byte(reason), ret) == nil && json.Unmarshal([]byte(message), msg) == nil {
			if len(msg.Body) == 0 {
				log.Warningf("Bad line to recover,Body is empty: %s", line)
				continue
			}

			_begin := time.Now().UnixNano() / 1000000
			ret = p.sendDataWithPartition(msg.Topic, msg.PartitionId, msg.HashId, msg.Body, msg.Key, []string{msg.Tags})
			if ret.Code > CACHE_OK {
				//达到限速后，降低自动恢复数据发送速度
				if ret.Code == FAIL_REFUSED_BY_RATE_LIMITER {
					used := (time.Now().UnixNano() / 1000000) - _begin
					time.Sleep(time.Duration(math.Max(float64(p.config.carreraClientTimeout-used), 0)) * time.Millisecond)
				}
			} else {
				log.Infof("Succ recovered msg: %v", msg)
			}
		}
	}
}
