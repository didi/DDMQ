package consumer

import (
	carrera "carrera/consumer/CarreraConsumer"
	log "go.intra.xiaojukeji.com/golang/dlog"
	"math"
	"math/rand"
	"sync"
	"sync/atomic"
	"time"
	"unsafe"
	"carrera/common/qlog"
	"fmt"
)

type CarreraConsumer interface {
	/**
	   等待占用的资源自动释放后再关闭资源
	 */
	Shutdown()

	/**
	   强制关闭拉取连接
	 */
	ShutdownNow()
	GetConsumeStats(request *carrera.ConsumeStatsRequest) (r []*carrera.ConsumeStats, err error)
}

type CarreraConsumerProxy struct {
	consumers []CarreraConsumer
}

type CarreraConsumerImpl struct {
	pullWaitGroup    *sync.WaitGroup
	proceedWaitGroup *sync.WaitGroup
	msgChan          []chan *msg
	isClose          int32
	config           Config
	pullClientMap    *SyncMap
}

type processResult string

const (
	processResultSuccess processResult = "succ"
	processResultFail    processResult = "fail"
	processResultSkip    processResult = "skip"
)

type result struct {
	offset int64
	isSucc processResult
}

type msg struct {
	response   *carrera.PullResponse
	msgIdx     int
	resultChan chan *result
}

type Config struct {
	Group, Topic   string      //消费组，订阅的topic
	ProxyList      []string    //消费proxy地址, 新版本（v1.5.5以后）服务发现请忽略此参数
	GoroutineNum   int         //业务处理的goroutine数量
	MsgProceedFunc MsgCProceed //处理消息的函数
	BatchNum       int32
	MaxLingerTime  int32
	ClientTimeout  int32
}

func (cf *Config) String() string {
	if cf == nil {
		return "<nil>"
	}
	return fmt.Sprintf("Config(%+v)", *cf)
}

type SyncMap struct {
	sync.RWMutex
	Map map[string]client
}

func newSyncMap() *SyncMap {
	sm := new(SyncMap)
	sm.Map = make(map[string]client)
	return sm

}

func (sm *SyncMap) get(key string) client {
	sm.RLock()
	value := sm.Map[key]
	sm.RUnlock()
	return value
}

func (sm *SyncMap) put(key string, value client) {
	sm.Lock()
	sm.Map[key] = value
	sm.Unlock()
}

func (sm *SyncMap) delete(key string) {
	sm.Lock()
	delete(sm.Map, key)
	sm.Unlock()
}

type MsgCProceed func(*carrera.Context, *carrera.Message) bool

var MyRand *rand.Rand

func init() {
	qlog.InitConsumerLog(10, qlog.LEVEL_INFO, "")
	MyRand = rand.New(rand.NewSource(time.Now().UnixNano()))
}

func InitLogger(dirpath string) {
	qlog.InitConsumerLog(10, qlog.LEVEL_INFO, dirpath)
}

func (consumer *CarreraConsumerProxy) Shutdown() {
	wg := sync.WaitGroup{}
	for _, c := range consumer.consumers {
		wg.Add(1)
		go func(myConsumer CarreraConsumer) {
			myConsumer.Shutdown()
			wg.Done()
		}(c)
	}
	wg.Wait()
}

func (consumer *CarreraConsumerProxy) ShutdownNow() {
	wg := sync.WaitGroup{}
	for _, c := range consumer.consumers {
		wg.Add(1)
		go func(myConsumer CarreraConsumer) {
			myConsumer.ShutdownNow()
			wg.Done()
		}(c)
	}
	wg.Wait()
}

func (consumer *CarreraConsumerProxy) GetConsumeStats(request *carrera.ConsumeStatsRequest) (r []*carrera.ConsumeStats, err error) {
	return consumer.consumers[0].GetConsumeStats(request)
}

func (consumer *CarreraConsumerImpl) Shutdown() {
	if atomic.CompareAndSwapInt32(&consumer.isClose, 0, 1) {
		p := unsafe.Pointer(consumer)
		log.Infof("closing consumer [%d]", p)
		consumer.isClose = 1

		consumer.pullWaitGroup.Wait()
		for _, proceedChan := range consumer.msgChan {
			close(proceedChan)
		}
		consumer.proceedWaitGroup.Wait()
		log.Infof("closed consumer [%d]", p)
	} else {
		consumer.proceedWaitGroup.Wait()
	}
}

func (consumer *CarreraConsumerImpl) ShutdownNow() {
	if atomic.CompareAndSwapInt32(&consumer.isClose, 0, 1) {
		p := unsafe.Pointer(consumer)
		log.Infof("closing consumer [%d]", p)

		consumer.pullClientMap.Lock()
		for addr, c := range consumer.pullClientMap.Map {
			if c != nil {
				c.close()
			}
			delete(consumer.pullClientMap.Map, addr)
		}
		consumer.pullClientMap.Unlock()

		consumer.isClose = 1

		consumer.pullWaitGroup.Wait()
		for _, proceedChan := range consumer.msgChan {
			close(proceedChan)
		}
		consumer.proceedWaitGroup.Wait()
		log.Infof("closed consumer [%d]", p)
	} else {
		consumer.proceedWaitGroup.Wait()
	}
}

func (consumer *CarreraConsumerImpl) GetConsumeStats(request *carrera.ConsumeStatsRequest) (r []*carrera.ConsumeStats, err error) {
	c := newThriftClient()
	defer func() {
		if c != nil {
			c.close()
		}
	}()
	if err := c.open(consumer.config.ProxyList[MyRand.Intn(len(consumer.config.ProxyList))], consumer.config.ClientTimeout); err != nil {
		c = nil
		return nil, err
	}
	return c.getConsumeStats(request)
}

func NewCarreraConsumer(c Config) CarreraConsumer {
	return newCarreraConsumer(c, newThriftClient)
}

func newCarreraConsumer(config Config, clientFactory func() client) CarreraConsumer {
	//check config
	if config.GoroutineNum == 0 {
		panic("config.goroutineNum is 0")
	}
	if config.ProxyList == nil || len(config.ProxyList) == 0 {
		panic("config.proxyList is null")
	}
	if config.BatchNum <= 0 {
		config.BatchNum = 8
	}

	if config.MaxLingerTime <= 0 {
		config.MaxLingerTime = 50
	}

	if config.ClientTimeout <= 0 {
		config.ClientTimeout = 2000
	}

	if config.ClientTimeout < config.MaxLingerTime {
		config.ClientTimeout = config.MaxLingerTime * 2
	}

	log.Infof("receive config params: %v", config.String())

	//shuffle proxyList
	r := rand.New(rand.NewSource(time.Now().UnixNano()))
	for i := 0; i < len(config.ProxyList); i++ {
		idx := r.Intn(len(config.ProxyList) - i)
		temp := config.ProxyList[i]
		config.ProxyList[i] = config.ProxyList[idx+i]
		config.ProxyList[idx+i] = temp
	}
	log.Infof("shuffled [proxyList %s]", config.ProxyList)
	carreraConsumerProxy := &CarreraConsumerProxy{consumers: make([]CarreraConsumer, 0)}

	remainNum := config.GoroutineNum % len(config.ProxyList)
	goroutineNum := config.GoroutineNum - remainNum

	log.Infof("remainNum %d goroutineNum %d", remainNum, goroutineNum)
	//calc how many connection to svr and tune batchNum to ensure one msg which pulled in once pull being proceed by one goroutine
	if goroutineNum != 0 {
		singleConsumerConcurrency := len(config.ProxyList) * int(config.BatchNum)
		for goroutineNum-singleConsumerConcurrency > 0 {
			config.GoroutineNum = singleConsumerConcurrency
			goroutineNum -= singleConsumerConcurrency
			carreraConsumerProxy.consumers = append(carreraConsumerProxy.consumers, _newCarreraConsumerImpl(config, clientFactory))
		}
		if goroutineNum != 0 {
			config.GoroutineNum = goroutineNum
			config.BatchNum = int32(goroutineNum / len(config.ProxyList))
			carreraConsumerProxy.consumers = append(carreraConsumerProxy.consumers, _newCarreraConsumerImpl(config, clientFactory))
		}
	}
	if remainNum != 0 {
		config.GoroutineNum = remainNum
		config.BatchNum = 1
		if goroutineNum != 0 {
			log.Infof("shuffled [proxyList %s]", config.ProxyList)
			config.ProxyList = config.ProxyList[:remainNum]
		}
		carreraConsumerProxy.consumers = append(carreraConsumerProxy.consumers, _newCarreraConsumerImpl(config, clientFactory))
	}
	log.Infof("total create consumer %d", len(carreraConsumerProxy.consumers))

	return carreraConsumerProxy
}

func _newCarreraConsumerImpl(config Config, clientFactory func() client) CarreraConsumer {
	//process msg goroutine
	if config.GoroutineNum < len(config.ProxyList) {
		config.GoroutineNum = len(config.ProxyList)
	}

	connector := &CarreraConsumerImpl{
		pullWaitGroup:    &sync.WaitGroup{},
		proceedWaitGroup: &sync.WaitGroup{},
		isClose:          0,
		pullClientMap:    newSyncMap(),
	}
	connector.pullWaitGroup.Add(len(config.ProxyList))
	connector.proceedWaitGroup.Add(config.GoroutineNum)
	connector.msgChan = make([]chan *msg, len(config.ProxyList))
	connector.isClose = 0
	connector.config = config

	log.Infof("[consumer:%d]open consumer [proxyList %s batchNum %d goroutineNum %d]", unsafe.Pointer(connector), config.ProxyList, config.BatchNum, config.GoroutineNum)

	//pull msg goroutine
	for i := 0; i < len(config.ProxyList); i++ {
		connector.msgChan[i] = make(chan *msg, 10)
		go pull(config.ProxyList[i], config, connector, clientFactory, i, connector.msgChan[i])
	}

	for i := 0; i < config.GoroutineNum; i++ {
		go proceedMsgChan(connector.msgChan[i%len(config.ProxyList)], config, connector)
	}

	return connector
}

func proceedMsgChan(msgIn chan *msg, config Config, connector *CarreraConsumerImpl) {
	for m := range msgIn {
		if atomic.LoadInt32(&connector.isClose) == 0 {
			if proceedMsg(m.response.Context, m.response.Messages[m.msgIdx], config.MsgProceedFunc) {
				m.resultChan <- &result{isSucc: processResultSuccess, offset: m.response.Messages[m.msgIdx].GetOffset()}
			} else {
				m.resultChan <- &result{isSucc: processResultFail, offset: m.response.Messages[m.msgIdx].GetOffset()}
			}
		} else {
			log.Infof("[consumer:%d] consumer has closed, msg return skip, key=%s, offset=%d", unsafe.Pointer(connector), m.response.Messages[m.msgIdx].GetKey(), m.response.Messages[m.msgIdx].GetOffset())
			m.resultChan <- &result{isSucc: processResultSkip, offset: m.response.Messages[m.msgIdx].GetOffset()}
		}
	}
	connector.proceedWaitGroup.Done()
}

func pull(address string, config Config, connector *CarreraConsumerImpl, clientFactory func() client, idx int, proceedChan chan *msg) {
	var c client = nil

	request := new(carrera.PullRequest)
	request.GroupId = config.Group
	request.MaxBatchSize = &config.BatchNum
	if config.Topic != "" {
		request.Topic = &config.Topic
	}
	if config.MaxLingerTime > 0 {
		request.MaxLingerTime = &config.MaxLingerTime
	}
	retryInterval := int32(50)

	for atomic.LoadInt32(&connector.isClose) == 0 {
		if c == nil {
			//build thrift client
			c = clientFactory()
			if err := c.open(address, config.ClientTimeout); err != nil {
				log.Infof("[consumer:%d] proxy %d %s connect fail proxyList %s, Error:%v", unsafe.Pointer(connector), idx, address, config.ProxyList, err)
				c = nil
				time.Sleep(5 * time.Second)
				continue
			}
			connector.pullClientMap.put(address, c)
			log.Infof("[consumer:%d]proxy %d %s connected", unsafe.Pointer(connector), idx, address)
		}
		if resp, err := c.pull(request); err == nil {
			//proceed msgs
			if resp == nil || resp.Messages == nil || len(resp.Messages) == 0 {
				request.Result_ = nil
				time.Sleep(time.Duration(retryInterval) * time.Millisecond)
				retryInterval = int32(math.Min(float64(retryInterval*2), 500))
			} else {
				request.Result_ = &carrera.ConsumeResult_{SuccessOffsets: make([]int64, 0, 10), FailOffsets: make([]int64, 0, 10)}
				request.Result_.FailOffsets = request.Result_.FailOffsets[:0]
				request.Result_.SuccessOffsets = request.Result_.SuccessOffsets[:0]
				resultChan := make(chan *result, len(resp.Messages))
				for idx := range resp.Messages {
					proceedChan <- &msg{response: resp, msgIdx: idx, resultChan: resultChan}
				}

				for i := 0; i < len(resp.Messages); i++ {
					msgResult := <-resultChan
					switch msgResult.isSucc {
					case processResultSuccess:
						request.Result_.SuccessOffsets = append(request.Result_.SuccessOffsets, msgResult.offset)
					case processResultFail:
						request.Result_.FailOffsets = append(request.Result_.FailOffsets, msgResult.offset)
					}
				}
				request.Result_.Context = resp.Context
				retryInterval = int32(50)
			}
		} else if c != nil {
			log.Infof("[consumer:%d]proxy %d %s disconnected ，err: %v", unsafe.Pointer(connector), idx, address, err)
			if atomic.LoadInt32(&connector.isClose) == 0 {
				//close client
				c.close()
				c = nil
				connector.pullClientMap.delete(address)
				time.Sleep(5 * time.Second)
			}
		}
	}

	//close
	if c != nil {
		log.Infof("[consumer:%d]proxy %d %s release resource", unsafe.Pointer(connector), idx, address)
		if request.Result_ != nil {
			c.submit(request.Result_)
		}
		c.close()
		c = nil
		connector.pullClientMap.delete(address)
	}
	connector.pullWaitGroup.Done()
}

func proceedMsg(context *carrera.Context, msg *carrera.Message, proceed MsgCProceed) (result bool) {
	defer func() {
		if err := recover(); err != nil {
			log.Errorf("exception when processing message ,context[group=%s topic=%s qid=%s],message[offset=%v key=%s value=%v], err: %v ", context.GroupId, context.Topic, context.Qid, msg.Offset, msg.Key, msg.Value, err)
			result = false
		}
	}()
	return proceed(context, msg)
}
