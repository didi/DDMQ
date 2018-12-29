package delay

import (
	"testing"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
	"carrera/common/qlog"
)

const (
	callbackUrl = "http://127.0.0.1:8000/" // Please use your own callback url
)

//var bridgeQList = []string{"http://127.0.0.1:8000"}
var bridgeQList = []string{"http://127.0.0.1:8000"}

type People struct {
	Id   int    `json:"id"`
	Name string `json:"name"`
}

func TestDelayProducer_Send(t *testing.T) {
	// Please have the attention of log init
	qlog.InitLog(10, 50)

	task, headers, product, bizKey, err := prepareData(t)
	if err != nil {
		t.Errorf("Error while TestDelayProducer_Send, err=%v", err)
		return
	}

	config := NewDefaultDelayConfig()
	config.SetBridgeQList(bridgeQList) // Please don't lose the prefix "http://"

	t.Logf("prepareData: task=%+v, headers=%v, product=%v, bizKey=%v, config=%+v\n", task, headers, product, bizKey, config)

	producer := NewDelayProducer(config)
	errorInfo := producer.Send(task, headers, product, bizKey)
	t.Logf("result=%+v\n", errorInfo)
	if errorInfo.Errno == 0 {
		t.Logf("succ send task")
	} else {
		t.Logf("fail to send task")
	}
}

func TestDelayProducer_SendWithParamsTransformed(t *testing.T) {
	// Please have the attention of log init
	qlog.InitLog(10, 50)

	task, headers, product, bizKey, err := prepareData(t)
	if err != nil {
		t.Errorf("Error while TestDelayProducer_Send, err=%v", err)
		return
	}

	config := NewDefaultDelayConfig()
	config.SetBridgeQList(bridgeQList) // Please don't lose the prefix "http://"

	t.Logf("prepareData: task=%+v, headers=%v, product=%v, bizKey=%v, config=%+v\n", task, headers, product, bizKey, config)

	producer := NewDelayProducer(config)
	errorInfo := producer.SendWithParamsTransformed(task, headers, product, bizKey)
	t.Logf("result=%+v\n", errorInfo)
	if errorInfo.Errno == 0 {
		t.Logf("succ send task")
	} else {
		t.Logf("fail to send task")
	}
}

func TestDelayProducer_SendAndCancel(t *testing.T) {
	// Please have the attention of log init
	qlog.InitLog(10, 50)

	task, headers, product, bizKey, err := prepareData(t)
	if err != nil {
		t.Errorf("Error while TestDelayProducer_Send, err=%v", err)
		return
	}

	timeDelay, _ := time.ParseDuration("30s")
	task.Timestamp = time.Now().Unix() + int64(timeDelay)

	config := NewDefaultDelayConfig()
	config.SetBridgeQList(bridgeQList) // Please don't lose the prefix "http://"

	t.Logf("prepareData: task=%+v, headers=%v, product=%v, bizKey=%v, config=%+v\n", task, headers, product, bizKey, config)

	producer := NewDelayProducer(config)
	sendErrorInfo := producer.Send(task, headers, product, bizKey)
	t.Logf("sendErrorInfo=%+v\n", sendErrorInfo)

	if sendErrorInfo.Errno == 0 {
		t.Logf("succ send task")
	} else {
		t.Logf("fail to send task")
		return
	}

	cancelErrorInfo := producer.Cancel(sendErrorInfo.Taskid, headers, product, bizKey)
	t.Logf("cancelErrorInfo=%+v\n", cancelErrorInfo)

	if cancelErrorInfo.Errno == 0 {
		t.Logf("succ cancel task")
	} else {
		t.Logf("fail to cancel task")
		return
	}
}



func prepareData(t *testing.T) (task *Task, httpHeaders http.Header, product, bizKey string, err error) {
	traceId := fmt.Sprintf("%d", time.Now().Unix()) // Please use your own traceId
	spanId := fmt.Sprintf("%d", time.Now().Unix())  // Please use your own spanId

	httpHeaders = http.Header{}
	httpHeaders.Set("didi-header-rid", traceId)
	httpHeaders.Set("didi-header-spanid", spanId)
	httpHeaders.Set("Content-Type", "application/x-www-form-urlencoded")
	// Here you can add more header field names and values

	bizKey = "88888888" // This is just a bizKey for test, please use your bizKey applied

	product = "alipay_card"

	paramHeaders := make(map[string]interface{})

	paramHeaders["a"] = "aaaa"
	paramHeaders["b"] = "bbbb"

	p := &People{Id:1, Name:"tom"}
	paramJson, err := json.Marshal(p)
	if err != nil {
		t.Errorf("Error while json marshal, err=%v", err)
		return nil, httpHeaders, product, bizKey, err
	}

	task = &Task{
		Topic:"topic_test", // This is just a topic for test, Please use your topic applied
		Url:callbackUrl,
		Mode:2,
		Retry:3,
		Type: 1,
		RetryInterval:3,
		RetryPolicy: 0,
		Timeout:3000,
		Expire:100000,
		Timestamp:time.Now().Unix(),
		Params:string(paramJson),
		ParamHeaders:paramHeaders,
	}

	return
}
