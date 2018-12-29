package carrera

import (
	"flag"
	"fmt"
	"os"
	"sync"
	"sync/atomic"
	"testing"
	"time"

	"carrera/CarreraProducer"
	"carrera/common/qlog"
	log "go.intra.xiaojukeji.com/golang/commons/dlog"
	"go.intra.xiaojukeji.com/golang/testify/assert"
)

type mockProducer struct {
	waitgroup    *sync.WaitGroup
	curSendCount int32
}

func (p *mockProducer) start() {

}
func (p *mockProducer) shutdown() {

}
func (p *mockProducer) sendWithPartition(topic string, partitionId int32, hashId int64, body []byte, key, tags string) *CarreraProducer.Result {
	atomic.AddInt32(&p.curSendCount, 1)
	defer func() {
		atomic.AddInt32(&p.curSendCount, -1)
	}()
	if p.waitgroup != nil {
		p.waitgroup.Wait()
	} else {
		time.Sleep(10 * time.Millisecond)
	}
	return &CarreraProducer.Result{Code: OK, Msg: "good"}
}

func (p *mockProducer) sendMessage(msg *CarreraProducer.Message) *CarreraProducer.Result {
	return nil
}
func (p *mockProducer) sendDelay(topic string, body []byte, delayMeta *CarreraProducer.DelayMeta, tags string) *CarreraProducer.DelayResult {
	return nil
}
func (p *mockProducer) cancelDelay(topic, uniqDelayMsgId, tags string) *CarreraProducer.DelayResult {
	return nil
}

func newMockProducer(conf *CarreraConfig) sender {
	return &mockProducer{}
}

func TestPooledProducer_Local_Send(t *testing.T) {
	config := NewDefaultLocalCarreraConfig()
	config.SetCarreraProxyList([]string{"127.0.0.1:9613", "127.0.0.2:9613"})
	producer := NewCarreraPooledProducer(config)
	producer.Start()

	ret := producer.Send("test-0", "test-------zhc", "asdffff")
	fmt.Println(ret.String())

	producer.Shutdown()
}


func TestPooledProducer_Local_Mock_Send(t *testing.T) {
	config := NewDefaultLocalCarreraConfig()
	config.SetCarreraProxyList([]string{"127.0.0.1:9613", "127.0.0.2:9613"})
	mockP := &mockProducer{waitgroup: &sync.WaitGroup{}}
	producer := _newPooledProducer(config, func(conf *CarreraConfig) sender {
		return mockP
	})
	for i := 0; i < 500; i++ {
		mockP.waitgroup.Add(1)
		go func() {
			ret := producer.Send("test-0", "test-------zhc", "asdffff")
			assert.Equal(t, ret.Code, int32(OK))
		}()
	}

	time.Sleep(2 * time.Second)
	assert.Equal(t, int32(500), mockP.curSendCount)

	for i := 0; i < 500; i++ {
		mockP.waitgroup.Done()
	}
	time.Sleep(2 * time.Second)
	assert.Equal(t, int32(0), mockP.curSendCount)
	producer.Shutdown()
}

func TestPooledProducer_Local_Mock_Concurrent_Send(t *testing.T) {
	qlog.InitLog(10, 50)

	config := NewDefaultLocalCarreraConfig()
	config.SetCarreraProxyList([]string{"127.0.0.1:9613", "127.0.0.2:9613"})
	mockP := &mockProducer{}
	producer := _newPooledProducer(config, func(conf *CarreraConfig) sender {
		return mockP
	})

	waitGroup := sync.WaitGroup{}
	for i := 0; i < 10000; i++ {
		waitGroup.Add(1)
		go func() {
			ret := producer.Send("11", "test", "asdffff")
			assert.Equal(t, ret.Code, int32(OK))
			waitGroup.Done()
		}()
	}

	waitGroup.Wait()
	assert.Equal(t, int32(0), mockP.curSendCount)
	producer.Shutdown()
}

func TestPooledProducer_Local_Mock_Concurrent_SendBinaryData(t *testing.T) {
	config := NewDefaultLocalCarreraConfig()
	config.SetCarreraProxyList([]string{"127.0.0.1:9613"})
	mockP := &mockProducer{}
	producer := _newPooledProducer(config, func(conf *CarreraConfig) sender {
		return mockP
	})

	waitGroup := sync.WaitGroup{}
	for i := 0; i < 100; i++ {
		waitGroup.Add(1)
		go func() {
			ret := producer.SendBinaryData("test-0", []byte("test====zhc===中文测试"), "asdffff")
			assert.Equal(t, ret.Code, int32(OK))
			waitGroup.Done()
		}()
	}

	waitGroup.Wait()
	assert.Equal(t, int32(0), mockP.curSendCount)
	producer.Shutdown()
}

func TestPooledProducer_Local_Mock_Shutdown_In_Sending(t *testing.T) {
	config := NewDefaultLocalCarreraConfig()
	config.SetCarreraProxyList([]string{"127.0.0.1:9613", "127.0.0.2:9613"})
	mockP := &mockProducer{}
	producer := _newPooledProducer(config, func(conf *CarreraConfig) sender {
		return mockP
	})
	waitGroup := sync.WaitGroup{}
	waitGroup.Add(1000)
	var failCount int32 = 0

	go func() {
		for i := 0; i < 1000; i++ {

			go func() {
				for j := 0; j < 100; j++ {
					ret := producer.Send("test-0", "test====zhc", "asdffff")
					if ret.Code != OK {
						atomic.AddInt32(&failCount, 1)
					}
				}
				waitGroup.Done()
			}()
		}
	}()
	log.Info("ready to shutdown")
	time.Sleep(20 * time.Millisecond)
	producer.Shutdown()
	waitGroup.Wait()
	assert.NotEqual(t, int32(0), atomic.LoadInt32(&failCount))
}

func TestMain(m *testing.M) {
	flag.Set("alsologtostderr", "true")
	os.Exit(m.Run())
}
