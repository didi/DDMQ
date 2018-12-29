package main

import (
	"encoding/json"
	"fmt"
	"carrera/delay"
	"carrera/common/qlog"
	"net/http"
	"time"
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

func main() {
	// Please have the attention of log init
	qlog.InitLog(10, 50)

	task, headers, product, bizKey, err := prepareData()
	if err != nil {
		fmt.Errorf("Error while TestDelayProducer_Send, err=%v", err)
		return
	}

	config := delay.NewDefaultDelayConfig()
	config.SetBridgeQList(bridgeQList) // Please don't lose the prefix "http://"

	fmt.Printf("prepareData: task=%+v, headers=%v, product=%v, bizKey=%v, config=%+v\n", task, headers, product, bizKey, config)

	producer := delay.NewDelayProducer(config)
	errorInfo := producer.Send(task, headers, product, bizKey)
	fmt.Printf("result=%+v\n", errorInfo)
	if errorInfo.Errno == 0 {
		fmt.Printf("succ send task")
	} else {
		fmt.Printf("fail to send task")
	}
}

func prepareData() (task *delay.Task, httpHeaders http.Header, product, bizKey string, err error) {
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

	p := &People{Id: 1, Name: "tom"}
	paramJson, err := json.Marshal(p)
	if err != nil {
		fmt.Errorf("Error while json marshal, err=%v", err)
		return nil, httpHeaders, product, bizKey, err
	}

	task = &delay.Task{
		Topic:         "topic_test", // This is just a topic for test, Please use your topic applied.
		Url:           callbackUrl,
		Type:          1,
		Mode:          2,
		Retry:         3,
		RetryInterval: 3,
		RetryPolicy:   0,
		Timeout:       3000,
		Expire:        100000,
		Timestamp:     time.Now().Unix(),
		Params:        string(paramJson),
		ParamHeaders:  paramHeaders,
	}

	return
}
