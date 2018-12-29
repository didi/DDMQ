#include<iostream>

#include "CarreraProducer.h"
#include "CarreraDefine.h"


int main() {
    CarreraProducer::CarreraConfig config;
    std::vector <std::string> proxy_list;
    proxy_list.push_back("127.0.01:9613");

    config.SetProxyList(proxy_list);
    config.SetProxyTimeOut(100);    //这个Proxy端处理请求的超时时间。写队列超时之后会尝试Cache。Cache成功后会返回CACHE_OK;
    config.SetClientRetry(2);       //客户端重试的次数。在 Proxy Server 短暂不可用（比如重启）的时候会重试。
    config.SetClientTimeOut(1000);  //这个是 Client 和 Proxy Server 之间的 Socket 超时时间。一般不建议设太小。必须大于ProxyTimeout的值。
    config.SetPoolSize(20);         //设置 Client 与每台 Proxy Server 的最大连接数。单个连接上的生产请求是串行执行的。

    CarreraProducer::CarreraProducer producer(config);
    producer.Start(); //启动生产实例。这里会进行一些初始化操作。

    printf("Send Single Message Demo:\n");

    std::string topic = "mytest";//请在用户控制台申请Topic资源
    std::string body = "this is message body."; // 测试的消息体。
    //
    //--------------- 生产用法 ---------------
    //

    //0.最简单的用法，只指定topic和消息体。
    CarreraProducer::Result ret = producer.Send(topic, body);
    //强烈建议一定要在日志中打印生产的结果。
    //Result 包含三个属性：code和msg表示生产的结果。key是用来表示消息的唯一ID，后续要追踪这条消息的生产消费情况，都需要这个值。
    printf("Send(topic,body) --> Result{code=%d, key=%s, msg=%s}\n", ret.code, ret.key.c_str(), ret.msg.c_str());

    if (ret.code == OK
        || ret.code == CACHE_OK) {
        printf("produce success\n"); // OK 和 CACHE_OK 两个结果都可以认为是生产成功了。
    } else if (ret.code > CACHE_OK) {
        printf("produce failure\n"); // 失败的情况，根据code和msg做相应处理。
    }

    //1.推荐用法。
    ret = CarreraProducer::MessageBuilder(producer)
            .setTopic(topic)
            .setBody(body)
                    //****以下配置，全是可选配置****
            .setPartitionByHashId(565286556993685) // 设定消息路由模式：相同hashId的消息，会被存储到同一个Partition中。一般用在顺序消费场景下。不设置则使用随机路由方式。
            .setTag("test") //设置消息Tag，一般用于消息过滤。
            .setTraceId("0a6b55415aa209b80000034db1e741a2").setSpanId("876c4c25057566b8") //traceId和spanId
            .setPressureTraffic(false)  //标记消息是否来自压测流量，消费端默认不会消费压测流量。
            .addProperty("Cityid", "62") // 支持自定义属性，一般用于高级的消息过滤等功能。（独立于消息存储，不解析消息体）
            .addProperty("clientTs", "1520568754718")
            .send();   //执行发送。
    printf("SendWithBuilder --> Result{code=%d, key=%s, msg=%s}\n", ret.code, ret.key.c_str(), ret.msg.c_str());


    //2. 批量接口
    std::vector <CarreraProducer::Message> msgs;
    for (int i = 0; i < 8; ++i) {
        CarreraProducer::Message msg = CarreraProducer::MessageBuilder(producer)
                .setTopic(topic)
                .setBody(body)
                .getMessage();
        msgs.push_back(msg);
    }
    ret = producer.SendBatchSync(msgs);
    printf("SendBatch --> Result{code=%d, key=%s, msg=%s}\n", ret.code, ret.key.c_str(), ret.msg.c_str());

    //
    //--------------- 关闭生产实例 ---------------
    //
    producer.ShutDown();
    return 0;
}

