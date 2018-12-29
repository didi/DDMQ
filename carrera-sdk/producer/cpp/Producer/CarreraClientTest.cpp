#include <iostream>

#include "CarreraProducer.h"
#include "CarreraDefine.h"
#include "CarreraProducer.h"

std::string topic__ = "test-0";//"sd_test_autotest";
std::string idc__ = "test"; //"gz01";
std::string sd_server__ = "127.0.0.1:9513";


void sd_task() {

    CarreraProducer::CarreraConfig config;
    std::vector <std::string> proxy_list;

    int time_out_ = 100,
            client_retry_ = 2,
            client_timeout_ = 1000,
            pool_size_ = 20;

    config.SetProxyTimeOut(time_out_);
    config.SetClientRetry(client_retry_);
    config.SetClientTimeOut(client_timeout_);
    config.SetPoolSize(pool_size_);

    std::string topic_ = "test-0"; //"sd_test_autotest";
    std::string idc_ = "test";  //"gz01";
    std::string sd_server_ = "127.0.0.1:9513";

    CarreraProducer::CarreraSdProducer sdProducer(topic_,idc_,sd_server_);
    sdProducer.Start(); //启动生产实例。这里会进行一些初始化操作。

    std::string body = "this is message body 01."; // 测试的消息体。


    //1.推荐用法。
    CarreraProducer::Result ret = CarreraProducer::MessageBuilder(sdProducer)
            .setTopic(topic_)
            .setBody(body)
            .setPartitionByHashId(565286556993685) // 设定消息路由模式：相同hashId的消息，会被存储到同一个Partition中。一般用在顺序消费场景下。不设置则使用随机路由方式。
            .setTag("test") //设置消息Tag，一般用于消息过滤。
            .setTraceId("0a6b55415aa209b80000034db1e741a2").setSpanId("876c4c25057566b8") //traceId和spanId
            .setPressureTraffic(false)  //标记消息是否来自压测流量，消费端默认不会消费压测流量。
            .addProperty("Cityid", "62") // 支持自定义属性，一般用于高级的消息过滤等功能。（独立于消息存储，不解析消息体）
            .addProperty("clientTs", "1520568754718")
            .send();   //执行发送。

    std::this_thread::sleep_for(std::chrono::seconds(10));

    printf("SendWithBuilder --> Result{code=%d, key=%s, msg=%s}\n", ret.code, ret.key.c_str(), ret.msg.c_str());
    if (ret.code == OK
        || ret.code == CACHE_OK) {
        printf("produce success\n"); // OK 和 CACHE_OK 两个结果都可以认为是生产成功了。
    } else if (ret.code > CACHE_OK) {
        printf("produce failure\n"); // 失败的情况，根据code和msg做相应处理。
    }

    sdProducer.Stop();
}

void send_msg(CarreraProducer::CarreraSdProducer &sdProducer) {
    std::string body = "this is message body 02.";
    CarreraProducer::Result ret = CarreraProducer::MessageBuilder(sdProducer)
            .setTopic(topic__)
            .setBody(body)
            .send();
    printf("SendInFunction--> Result{code=%d, key=%s, msg=%s}\n", ret.code, ret.key.c_str(), ret.msg.c_str());
    if (ret.code == OK
        || ret.code == CACHE_OK) {
        printf("produce success\n");
    }
}

int main() {
    printf("Single test:\n");
    std::thread t(sd_task);
    t.join();
    std::thread t2(sd_task);
    t2.join();
    std::thread t3(sd_task);
    t3.join();
    sd_task();
    sd_task();

    printf("Parallel test 01:\n");
    std::thread threads[5];
    printf("Spawning 5 threads...\n");
    for (int i = 0; i < 5; i++) {
        threads[i] = std::thread(sd_task);
    }
    for (auto& t: threads) {
        t.join();
    }
    printf("First group threads joined.\n");

    printf("Parallel test 02:\n");
    CarreraProducer::CarreraSdProducer sdProducer2(topic__,idc__,sd_server__);
    sdProducer2.Start();
    std::thread t4(send_msg, std::ref(sdProducer2));
    t4.join();
    std::thread t5(send_msg, std::ref(sdProducer2));
    t5.join();
    std::thread new_threads[200];
    printf("Spawning 200 new threads...\n");
    for (int i = 0; i < 200; i++) {
        new_threads[i] = std::thread(send_msg, std::ref(sdProducer2));
        // std::this_thread::sleep_for(std::chrono::seconds(10));
    }
    for (auto& t: new_threads) {
        t.join();
    }
    printf("Second group threads joined.\n");
    sdProducer2.Stop();
    return EXIT_SUCCESS;
}
