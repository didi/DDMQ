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
	TOPIC               = "请在用户控制台申请Topic资源"      // 测试连接可以用mytest.
	STRING_MESSAGE_BODY = "this is message body." // 测试的消息体。
	TAG                 = ""                      //测试用的Tag，根据自己需要设置
)

var (
	proxyList = []string{
		"127.0.0.1:9613",
		"127.0.0.2:9613",
	}
)

func initLocalAllDefaultModel() *carrera.CarreraConfig {
	//-------------- 构造本地配置，全部默认配置，但必须指定proxy列表----------------
	config := carrera.NewDefaultLocalCarreraConfig()

	//指定proxy列表，每个集群的proxy必须全部配置上
	config.SetCarreraProxyList(proxyList)

	return config
}

func initLocalAllSpecifyModel() *carrera.CarreraConfig {
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

func sendOne(producer *carrera.PooledProducer) {
	//--------------- 生产用法 ---------------

	// 指定消息路由。相同hashId的消息，会被存储到同一个Partition中, hashId 比如是driver_id的hashcode
	// var hashId int64 = 1

	ret := producer.MessageBuilder().
		SetTopic(TOPIC).
		// SetRandomPartition().  //随机选择一个partition，这个与SetHashId根据需要只选择一个
		// SetHashId(hashId). //根据hashid确定partition
		SetBody(STRING_MESSAGE_BODY).
		SetTags(TAG).                          //tag根据自己需要是否添加
		SetPressureTraffic(false).              //设置压测标记，是否压测数据
		SetTraceId("111111").                  //设置 trace id
		AddProperty("testKey1", "testValue1"). //添加消息属性，可以添加多个
		Send()

	//强烈建议一定要在日志中打印生产的结果。
	//Result 包含三个属性：code和msg表示生产的结果。key是用来表示消息的唯一ID，后续要追踪这条消息的生产消费情况，都需要这个值
	fmt.Println(ret)

	if ret.Code == carrera.OK || ret.Code == carrera.CACHE_OK {
		fmt.Println("produce success") // OK 和 CACHE_OK 两个结果都可以认为是生产成功了。
	} else if ret.Code > carrera.CACHE_OK {
		fmt.Println("produce failure") // 失败的情况，根据code和msg做相应处理。
	}

	//-------------------延时消息生产用法---------------------------
	timestamp := time.Now().Unix()

	delayMeta := &CarreraProducer.DelayMeta{
		Timestamp: timestamp + 60,     // 延迟60s执行
		Dmsgtype:  thrift.Int32Ptr(2), // 2-延迟消息, cproxy会根据用户在console上设置的重试次数进行重试，用户可以设置重试间隔
	}
	delayResult := producer.AddDelayMessageBuilder().
		SetTopic(TOPIC).
		SetBody(STRING_MESSAGE_BODY).
		SetDelayMeta(delayMeta).
		SetTags(TAG).                                // tag根据自己需要是否添加
		SetPressureTraffic(false).                   // 设置压测标记，是否压测数据
		SetTraceId("222222").                        // 设置 trace id
		AddHeader("headerKey111", "headerValue111"). // 延迟消息http header信息，根据需要设置
		AddProperty("testKey1", "testValue1").       // 添加消息属性，可以添加多个
		Send()
	fmt.Println(delayResult)

	//-------------------延时循环消息生产用法---------------------------
	timestamp = time.Now().Unix() + 60 // 延迟60s执行第一次
	delayMeta = &CarreraProducer.DelayMeta{
		Timestamp: timestamp,
		Dmsgtype:  thrift.Int32Ptr(3),                 // 3-延迟循环消息
		Interval:  thrift.Int64Ptr(10),                // 循环间隔10s
		Expire:    thrift.Int64Ptr(timestamp + 86400), // 消息触发的24小时之后过期
		Times:     thrift.Int64Ptr(100),               // 循环执行100次
	}
	delayResult = producer.AddDelayMessageBuilder().
		SetTopic(TOPIC).
		SetBody(STRING_MESSAGE_BODY).
		SetDelayMeta(delayMeta).
		SetTags(TAG).                                // tag根据自己需要是否添加
		SetPressureTraffic(false).                   // 设置压测标记，是否压测数据
		SetTraceId("3333333").                       // 设置 trace id
		AddHeader("headerKey222", "headerValue222"). // 延迟消息http header信息，根据需要设置
		AddProperty("testKey1", "testValue1").       // 添加消息属性，可以添加多个
		Send()
	fmt.Println(delayResult)

	//不立即取消
	time.Sleep(time.Duration(500) * time.Millisecond)

	//-------------------延时或者延迟循环消息取消用法---------------------------
	delayResult = producer.CancelDelayMessageBuilder().
		SetTopic(TOPIC).
		SetUniqDelayMsgId(delayResult.UniqDelayMsgId).
		SetTags(TAG). //tag根据自己需要是否添加
		Send()
	fmt.Println(delayResult)

}

func main() {
	//
	//--------------- 构造、启动生产实例 ---------------
	//

	//mq日志信息和drop日志信息都在一个路径下！
	//初始化日志文件大小、drop日志文件大小，单位GB，日志级别默认INFO，日志路径./log/mq/,
	// 日志文件为 ./log/mq/mq.log ./log/mq/drop/drop.log
	// qlog.InitLog(10, 50)

	//如果需要单独指定日志级别，日志路径，使用下面的函数
	qlog.InitQLog(10, 50, qlog.LEVEL_DEBUG, qlog.LOG_PATH) // 注意，DEBUG模式一般只在测试时候开启，正常使用一般不开

	//全部使用默认值
	 config := initLocalAllDefaultModel()

	//参数全部单独指定
	//config := initLocalAllSpecifyModel()

	//初始化producer
	producer := carrera.NewCarreraPooledProducer(config)

	//启动producer，不可忽略
	producer.Start()

	//生产过程
	for i := 0; i < 1; i++ {
		sendOne(producer)
		time.Sleep(time.Duration(15000) * time.Millisecond)
	}

	//--------------- 关闭生产实例 ---------------
	producer.Shutdown()
}
