package main

import (
	"fmt"
	"carrera/consumer"
	"carrera/consumer/CarreraConsumer"
	"time"
)

func main() {
	//指定日志输出路径
	//consumer.InitLogger("./mq/log")

	/*
			ProxyList 中的svr会并发拉取，拉取后投递到GoroutineNum数量的goroutine中，进行处理

			当GoroutineNum小于proxyList中svr数量时，sdk会强制将goroutine数量设定为proxysvr的数量

			例如：

			1. proxysvr为2台，3个 goroutine则有1个goroutine会随机消费两个svr中的1个

			   svr1         svr2--------
			     |	         |          |
			  goroutine1  goroutine2  goroutine3


			2. proxysvr为4台，2个goroutine的情况下

			    svr1        svr2        svr3        svr4
			      |		  |	      |		  |
			      |   	  |	      |		  |
			      |		  |	      |		  |
		        goroutine1   goroutine2   goroutine3   goroutine4


	*/
	//consumer := consumer.NewDiscoveryCarreraConsumer(consumer.Config{GoroutineNum:5,Group:"test-thrift-client",Topic:"test-1",MsgProceedFunc:testMsgProceed},"alias!carrera_cproducer")
	consumer := consumer.NewCarreraConsumer(consumer.Config{GoroutineNum: 340, Group: "cg_test", ProxyList: []string{"127.0.0.1:9713"}, MsgProceedFunc: testMsgProceed})
	time.Sleep(2 * time.Second)

	request := &CarreraConsumer.ConsumeStatsRequest{
		Group: "cg_test",
	}
	ret, _ := consumer.GetConsumeStats(request)
	fmt.Printf("consume stats:[%s]", ret)

	topic := "test"
	request = &CarreraConsumer.ConsumeStatsRequest{
		Group: "cg_test",
		Topic: &topic,
	}
	ret, _ = consumer.GetConsumeStats(request)
	fmt.Printf("consume stats:[%s]", ret)

	consumer.Shutdown()
}

func testMsgProceed(context *CarreraConsumer.Context, msg *CarreraConsumer.Message) bool {
	fmt.Printf("receive msg context[%s] offset[%d] msg[%s] \n", context.String(), msg.Offset, msg.Key)
	time.Sleep(time.Second)
	return true
}
