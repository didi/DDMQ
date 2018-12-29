package main

import (
	"fmt"
	"carrera"
	"carrera/CarreraProducer"
	"carrera/common/qlog"
	"go.intra.xiaojukeji.com/golang/thrift-lib/0.9.2"
	"time"
)

const (
	REALTIME_TOPIC = "mytest" // 请在用户控制台申请Topic资源
	DELAY_TOPIC = "chronos_test" // 请在用户控制台申请Topic资源

	REALTIME_BODY = "realtime body content"
	DELAY_BODY = "delay body content"
)

var (
	proxyList = []string{
		"127.0.0.1:9613",
		"127.0.0.2:9613",
	}
)

func initLocalAllDefaultModelTx() *carrera.CarreraConfig {
	//-------------- 构造本地配置，全部默认配置，但必须指定proxy列表----------------
	config := carrera.NewDefaultLocalCarreraConfig()

	//指定proxy列表，每个集群的proxy必须全部配置上
	config.SetCarreraProxyList(proxyList)

	return config
}

func initLocalAllSpecifyModelTx() *carrera.CarreraConfig {
	//-------------- 构造本地配置，全部默认配置，但必须指定proxy列表----------------
	config := carrera.NewDefaultLocalCarreraConfig()

	//-------------- 构造配置，参数全部单独设置-------------------------
	//指定proxy列表，每个集群的proxy必须全部配置上
	config.SetCarreraProxyList(proxyList)
	//producer 实例池,可以并发发送，如果实例都被占用，没有放回池子的话，send会等待获取producer实例
	config.SetCarreraPoolSize(20)
	//Proxy端处理请求的超时时间。写队列超时之后会尝试Cache。Cache成功后会返回CACHE_OK
	config.SetCarreraProxyTimeout(50)
	//client和proxy server的超时时间，一般不建议设太小。必须大于carreraProxyTimeout的值，建议设置2倍的比例
	config.SetCarreraClientTimeout(100)
	//客户端失败重试次数，总共发送n+1次
	config.SetCarreraClientRetry(3)
	//是否开启自动从drop log文件中恢复消息，重试carreraClientRetry次后，仍然失败的消息，
	//会写入本地drop.log文件，然后sdk会根据设置的周期定时读取消息，重新发送，失败后，仍然会写入drop.log文件
	//默认每个drop文件50M，写够一个文件后才会触发自动恢复
	config.SetRecoverFromDropLog(true)
	//指定自动从drop log文件中恢复消息周期，不设置，默认30分钟,见#carrera.DEFAULT_RECOVER_FROM_DROP_LOG_INTERVAL
	config.SetRecoverFromDropLogInterval(carrera.DEFAULT_RECOVER_FROM_DROP_LOG_INTERVAL) //从drop log恢复数据时间间隔

	return config
}

func mockTxMsg(producer *carrera.PooledProducer) {
	timestamp := time.Now().Unix()

	delayMeta := &CarreraProducer.DelayMeta{
		Timestamp: timestamp + 60,                     // 第一次执行延迟60s执行
		Dmsgtype:  thrift.Int32Ptr(3),                 // 3-延迟循环消息
		Interval:  thrift.Int64Ptr(10),                // 循环间隔10s
		Expire:    thrift.Int64Ptr(timestamp + 86400), // 消息触发的24小时之后过期, Expire和Times满足其中一个就会结束事务监控消息的生命周期
		Times:     thrift.Int64Ptr(100),               // 循环执行100次, Expire和Times满足其中一个就会结束事务监控消息的生命周期
	}

	addDelayMessageBuilder := producer.AddDelayMessageBuilder().
		SetTopic(DELAY_TOPIC).
		SetBody(DELAY_BODY).
		SetDelayMeta(delayMeta).
		SetPressureTraffic(false).			// 设置压测标记，是否压测数据
		SetTraceId("xxx").				// 设置 trace id
		SetSpanId("yyy").				// 设置 span id
		AddHeader("headerKey111", "headerValue111").	//延迟消息http header信息，根据需要设置
		AddProperty("testKey1", "testValue1") 		//添加消息属性，可以添加多个

	// 发送监控消息, 也就是发送一个延时循环消息来监控后边执行的本地事务及业务消息发送是否成功,
	// 该监控消息需要业务提供回调接口消费(push)或者通过carrera consumer sdk进行消费(pull)
	delayResult := producer.AddTxMonitorMessageBuilder(addDelayMessageBuilder).Send()
	if delayResult.Code != carrera.OK && delayResult.Code != carrera.CACHE_OK {
		fmt.Printf("fail to add tx monitor message, result: %v\n", delayResult)
		return
	}
	fmt.Printf("succ to add tx monitor message, result: %v\n", delayResult)

	cancelDelayMessageBuilder := producer.CancelDelayMessageBuilder().
		SetTopic(DELAY_TOPIC).
		SetUniqDelayMsgId(delayResult.UniqDelayMsgId)

	// 根据本地事务是否执行成功分为以下两种情况:
	// 1. 如果本地事务执行成功, 则发送业务消息, 如果发送业务消息, 则取消监控消息.
	// 2. 如果本地事务执行失败, 则取消监控消息.
	if localTx() {
		fmt.Println("succ to execute local tx")

		messageBuilder := producer.MessageBuilder().
			SetTopic(REALTIME_TOPIC).
			SetBody(REALTIME_BODY).
			SetPressureTraffic(false).// 设置压测标记，是否压测数据
			SetTraceId("xxx").// 设置 trace id
			SetSpanId("yyy").// 设置 span id
			AddProperty("testKey1", "testValue1") //添加消息属性，可以添加多个

		result := producer.TxBusinessMessageBuilder(messageBuilder).Send()

		// 发送业务消息失败则结束, 业务还会再收到监控消息, 在下次收到监控消息时业务再次发起发送业务消息
		if result.Code != carrera.OK && result.Code != carrera.CACHE_OK {
			fmt.Printf("fail to send business message, result: %v\n", result)
			return
		}

		fmt.Printf("succ to send business message, result: %v\n", result)

		// 业务消息发送成功, 则需要把之前的监控消息给取消掉
		delayResult = producer.CancelTxMonitorMessageBuilder(cancelDelayMessageBuilder).Send()
		if delayResult.Code != carrera.OK && delayResult.Code != carrera.CACHE_OK {
			// 如果取消失败, 之后业务还会再收到这个监控消息, 业务需要再次发起cancel
			fmt.Printf("fail to cancel tx monitor message, result: %v\n", delayResult)
		}

		fmt.Printf("succ to cancel tx monitor message, result: %v\n", delayResult)
	} else {
		fmt.Println("fail to execute local tx")

		// 执行本地事务失败, 则需要把之前的监控消息给取消掉
		delayResult = producer.CancelTxMonitorMessageBuilder(cancelDelayMessageBuilder).Send()
		if delayResult.Code != carrera.OK && delayResult.Code != carrera.CACHE_OK {
			// 如果取消失败, 之后业务还会再收到这个监控消息, 业务需要再次发起cancel
			fmt.Printf("fail to cancel tx monitor message, result: %v\n", delayResult)
		}

		fmt.Printf("succ to cancel tx monitor message, result: %v\n", delayResult)
	}
}

func localTx() bool {
	return true
}

func main() {
	//--------------- 构造、启动生产实例 ---------------

	// mq日志信息和drop日志信息都在一个路径下！
	// 初始化日志文件大小、drop日志文件大小，单位GB，日志级别默认INFO，日志路径./log/mq/,
	// 日志文件为 ./log/mq/mq.log ./log/mq/drop/drop.log
	// qlog.InitLog(10, 50)

	//如果需要单独指定日志级别，日志路径，使用下面的函数
	qlog.InitQLog(10, 50, qlog.LEVEL_DEBUG, qlog.LOG_PATH) // 注意，DEBUG模式一般只在测试时候开启，正常使用一般不开

	//本地配置proxy列表模式，全部使用默认值
	config := initLocalAllDefaultModelTx()

	//本地配置proxy列表模式，参数全部单独指定
	//config := initLocalAllSpecifyModelTx()

	//初始化producer
	producer := carrera.NewCarreraPooledProducer(config)

	//启动producer，不可忽略
	producer.Start()

	//生产过程
	for i := 0; i < 1; i++ {
		mockTxMsg(producer)
		time.Sleep(time.Duration(15000) * time.Millisecond)
	}

	//--------------- 关闭生产实例 ---------------
	producer.Shutdown()
}
