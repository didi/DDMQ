#include<iostream>

#include "CarreraProducer.h"
#include "HttpMQTask.h"
#include <map>
#include <iostream>
#include <sys/time.h>
#include <uuid/uuid.h>

using namespace std;

int main() {
    CarreraProducer::CarreraConfig config;
    config.SetProxyTimeOut(100); //这个Proxy端处理请求的超时时间。写队列超时之后会尝试Cache。Cache成功后会返回CACHE_OK;
    config.SetClientRetry(2); //客户端重试的次数。在 Proxy Server 短暂不可用（比如重启）的时候会重试。
    config.SetClientTimeOut(1000); //这个是 Client 和 Proxy Server 之间的 Socket 超时时间。一般不建议设太小。必须大于ProxyTimeout的值。
    config.SetPoolSize(20); //设置 Client 与每台 Proxy Server 的最大连接数。单个连接上的生产请求是串行执行的。

    std::string delayTopic = "delaytopic_test"; //请在用户控制台申请Topic资源
    std::string client_idc_ = "test"; // client idc
    std::string sd_server_ = "127.0.0.1:9513";

    CarreraProducer::CarreraSdProducer sdProducer(delayTopic, client_idc_, sd_server_);
    sdProducer.SetCarreraConfig(config);

    std::cout << "Start SdProducer..." << std::endl;
    sdProducer.Start();
    std::cout << "SdProducer started." << std::endl;

    std::string delayBody = "{\"dd\":4}"; // 测试的消息体


    //-------------------延时消息生产用法---------------------------
    timeval time;
    ::gettimeofday(&time, 0);
    int64_t timeStamp = (time.tv_sec * 1000 + time.tv_usec/1000)/1000; // 秒

    CarreraProducer::DelayMeta delayMeta;
    delayMeta.__set_timestamp(timeStamp + 10); // 延迟10s执行
    delayMeta.__set_dmsgtype(DELAY_MSG_TYPE); // 2-延迟消息

    CarreraProducer::DelayResult ret = CarreraProducer::AddDelayMessageBuilder(sdProducer)
            .setTopic(delayTopic)
            .setBody(delayBody)
            .setDelayMeta(delayMeta)
            .setTags("this is tag") //tag根据自己需要是否添加 （可选）
            .setPressureTraffic(false) //设置压测标记，是否压测数据 （可选）
            .setTraceId("222222") //设置 trace id （可选）
            .setSpanId("spanId") // 设置 spanId  （可选）
            .addHeader("headerkey", "headerval") // 延迟消息http header信息，根据需要设置 （可选）
            .addProperty("propertyKey", "propertyVal") //添加消息属性，可以添加多个 （可选）
            .send();

    printf("SendWithBuilder --> DelayResult{code=%d, msg=%s, uniqueId=%s}\n", ret.code, ret.msg.c_str(), ret.uniqDelayMsgId.c_str());
    if (ret.code == OK
        || ret.code == CACHE_OK) {
        printf("produce success\n"); // OK 和 CACHE_OK 两个结果都可以认为是生产成功了。
    } else if (ret.code > CACHE_OK) {
        printf("produce failure\n"); // 失败的情况，根据code和msg做相应处理。
    }


    //-------------------延时循环消息生产用法---------------------------
    ::gettimeofday(&time, 0);
    timeStamp = (time.tv_sec * 1000 + time.tv_usec/1000)/1000;

    delayMeta.__set_timestamp(timeStamp + 10); // 延迟10s执行第一次
    delayMeta.__set_dmsgtype(LOOP_DELAY_MSG_TYPE); // 3-延迟循环消息
    delayMeta.__set_interval(10); // 循环间隔10s
    delayMeta.__set_expire(timeStamp + 86400); // 消息触发的24小时之后过期
    delayMeta.__set_times(100); // 循环执行100次

    ret = CarreraProducer::AddDelayMessageBuilder(sdProducer)
            .setTopic(delayTopic)
            .setBody(delayBody)
            .setDelayMeta(delayMeta)
            .setTags("this is tag") //tag根据自己需要是否添加
            .setPressureTraffic(false) //设置压测标记，是否压测数据
            .setTraceId("222222") //设置 trace id
            .addHeader("headerkey", "headerval") // 延迟消息http header信息，根据需要设置
            .addProperty("propertyKey", "propertyVal") //添加消息属性，可以添加多个
            .send();

    printf("SendWithBuilder --> DelayResult{code=%d, msg=%s, uniqueId=%s}\n", ret.code, ret.msg.c_str(), ret.uniqDelayMsgId.c_str());
    if (ret.code == OK
        || ret.code == CACHE_OK) {
        printf("produce success\n"); // OK 和 CACHE_OK 两个结果都可以认为是生产成功了。
    } else if (ret.code > CACHE_OK) {
        printf("produce failure\n"); // 失败的情况，根据code和msg做相应处理。
    }

    //不立即取消
    sleep(5);

    //-------------------延时或者延迟循环消息取消用法---------------------------
    ret = CarreraProducer::CancelDelayMessageBuilder(sdProducer)
            .setTopic(delayTopic)
            .setUniqDelayMsgId(ret.uniqDelayMsgId)
            .setTags("tags")
            .send();

    printf("SendWithBuilder --> DelayResult{code=%d, msg=%s, uniqueId=%s}\n", ret.code, ret.msg.c_str(), ret.uniqDelayMsgId.c_str());
    if (ret.code == OK
        || ret.code == CACHE_OK) {
        printf("produce success\n"); // OK 和 CACHE_OK 两个结果都可以认为是生产成功了。
    } else if (ret.code > CACHE_OK) {
        printf("produce failure\n"); // 失败的情况，根据code和msg做相应处理。
    }

    // 关闭生产实例
    sdProducer.Stop();
    return 0;
}

