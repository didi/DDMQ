package consumer

import (
	"errors"
	carrera "carrera/consumer/CarreraConsumer"
	"go.intra.xiaojukeji.com/golang/testify/assert"
	"sync/atomic"
	"testing"
	"time"
)

type mockClient struct {
	flag        int32
	pullCount   int32
	submitCount int32
	openCount   int32
	closeCount  int32
}

var increaseOffset int32 = 0

func (c *mockClient) pull(request *carrera.PullRequest) (r *carrera.PullResponse, err error) {
	switch atomic.LoadInt32(&c.flag) {
	case 1:
		msgs := make([]*carrera.Message, 0)
		msgs = append(msgs, &carrera.Message{Key: "key1", Value: []byte{1, 1, 1}, Offset: int64(atomic.AddInt32(&increaseOffset, 1))})
		msgs = append(msgs, &carrera.Message{Key: "key2", Value: []byte{1, 1, 1}, Offset: int64(atomic.AddInt32(&increaseOffset, 1))})
		atomic.AddInt32(&c.pullCount, 1)
		return &carrera.PullResponse{Context: &carrera.Context{GroupId: "aa", Topic: "topic", Qid: "1"}, Messages: msgs}, nil

	case 2:
		return nil, nil
	case 3:
		time.Sleep(1 * time.Second)
		return nil, nil
	default:
		return nil, errors.New("test error")

	}
}
func (c *mockClient) submit(result *carrera.ConsumeResult_) (r bool, err error) {
	atomic.AddInt32(&c.submitCount, 1)
	return true, nil
}
func (c *mockClient) open(inet string, timeout int32) (err error) {
	atomic.AddInt32(&c.openCount, 1)
	return nil
}
func (c *mockClient) close() {
	atomic.AddInt32(&c.closeCount, 1)
}

func (c *mockClient) getConsumeStats(request *carrera.ConsumeStatsRequest) (r []*carrera.ConsumeStats, err error) {
	return nil, nil
}

func TestCarreraConsumerNormal(t *testing.T) {

	msgProceed := func(context *carrera.Context, msg *carrera.Message) bool {
		return true
	}
	mc := &mockClient{flag: 1, submitCount: 0, openCount: 0, closeCount: 0}
	consumer := newCarreraConsumer(Config{GoroutineNum: 2, Group: "testgroup", Topic: "testtopic", ProxyList: []string{"127.0.0.1", "127.0.0.1"}, MsgProceedFunc: msgProceed}, func() client {
		return mc
	})

	time.Sleep(2 * time.Second)
	assert.Equal(t, mc.openCount, int32(2))
	assert.Equal(t, mc.pullCount > 0, true)
	consumer.Shutdown()
	assert.Equal(t, mc.closeCount, int32(2))
}

func TestCarreraConsumeNormalAfterFail(t *testing.T) {
	msgProceed := func(context *carrera.Context, msg *carrera.Message) bool {
		return true
	}
	mc := &mockClient{flag: 2, submitCount: 0, openCount: 0, closeCount: 0}
	consumer := newCarreraConsumer(Config{GoroutineNum: 2, Group: "testgroup", Topic: "testtopic", ProxyList: []string{"127.0.0.1", "127.0.0.1"}, MsgProceedFunc: msgProceed}, func() client {
		return mc
	})

	time.Sleep(2 * time.Second)
	assert.Equal(t, int32(0), mc.pullCount)
	mc.flag = 1
	time.Sleep(2 * time.Second)
	assert.Equal(t, mc.pullCount > 0, true)
	consumer.Shutdown()
}
