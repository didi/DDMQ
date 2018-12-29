package com.xiaojukeji.carrera.producer.examples;

import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.*;
import com.xiaojukeji.carrera.thrift.DelayMeta;
import com.xiaojukeji.carrera.thrift.DelayResult;
import com.xiaojukeji.carrera.thrift.Result;

import java.util.ArrayList;
import java.util.List;


public class CarreraTxMsgExample {

    public static void main(String[] args) throws Exception {
        CarreraConfig config = new CarreraConfig();
        List<String> servers = new ArrayList<String>();
        servers.add("127.0.0.1:9613");

        config.setCarreraProxyList(servers);
        config.setCarreraProxyTimeout(100); // 这个Proxy端处理请求的超时时间。写队列超时之后会尝试Cache。Cache成功后会返回CACHE_OK;
        config.setCarreraClientRetry(2); // 客户端重试的次数。在 Proxy Server 短暂不可用（比如重启）的时候会重试。
        config.setCarreraClientTimeout(1000); // 这个是 Client 和 Proxy Server 之间的 Socket 超时时间。一般不建议设太小。必须大于ProxyTimeout的值。
        config.setCarreraPoolSize(5); // 设置 Client 与每台 Proxy Server 的最大连接数。单个连接上的生产请求是串行执行的。

        CarreraProducer producer = new CarreraProducer(config);
        producer.start(); // 启动生产实例。这里会进行一些初始化操作。

        mock(producer);

        producer.shutdown();
    }

    public static void mock(CarreraProducer producer) throws Exception {
        String delayTopic = "chronos_test";
        String delayBody = "delay body content";

        String realtimeTopic = "test";
        String realtimeBody = "realtime body content";

        int dmsgtype = 2;                                         // 2-延迟消息, cproxy会根据用户在console上设置的重试次数进行重试，用户可以设置重试间隔
        long timestamp = System.currentTimeMillis() / 1000 + 120; // 延迟两分钟之后执行

        DelayMeta delayMeta = new DelayMeta();
        delayMeta.setTimestamp(timestamp);
        delayMeta.setDmsgtype(dmsgtype);

        AddDelayMessageBuilder addDelayMessageBuilder = producer.addDelayMessageBuilder()
                .setTopic(delayTopic)
                .setDelayMeta(delayMeta)
                .setPressureTraffic(false)                      // optional 是否是压测数据, 默认是非压测流量, 注意：如果设置为true, 则表示压测流量, 在console上的"是否接收压测流量(订阅关系->订阅关系列表->编辑订阅)"这个checkbox如果没有打勾, 则接收不到这个压测流量
                .setTraceId("xxx")                              // optional 设置traceId, 便于追踪消息和把脉展示
                .setSpanId("yyy")                               // optional 设置spanId, 便于追踪消息和把脉展示
                .addProperty("propertyKey1", "propertyValue1")  // optional 添加属性, 可以多次addProperty, http推送时会放到body里边 eg: params=xxx&propertyKey1=propertyValue1
                .addHeader("headerKey1", "headerValue1")        // optional 添加header到http header(必须是http推送才有效), 可以多次addHeader
                .setBody(delayBody);

        // 发送监控消息, 也就是发送一个延时循环消息来监控后边执行的本地事务及业务消息发送是否成功,
        // 该监控消息需要业务提供回调接口消费(push)或者通过carrera consumer sdk进行消费(pull)
        DelayResult delayResult = producer.addTxMonitorMessageBuilder(addDelayMessageBuilder).send();
        if (delayResult.getCode() != CarreraReturnCode.OK) {
            System.out.println("fail to add tx monitor message, result:" + delayResult);
            return;
        }
        System.out.println("succ to add tx monitor message, result:" + delayResult);

        CancelDelayMessageBuilder cancelDelayMessageBuilder = producer.cancelDelayMessageBuilder()
                .setTopic(delayTopic)
                .setUniqDelayMsgId(delayResult.getUniqDelayMsgId());

        // 根据本地事务是否执行成功分为以下两种情况:
        // 1. 如果本地事务执行成功, 则发送业务消息, 如果发送业务消息, 则取消监控消息.
        // 2. 如果本地事务执行失败, 则取消监控消息.
        if (localTx()) {
            System.out.println("succ to execute local tx");

            MessageBuilder messageBuilder = producer.messageBuilder()
                    .setTopic(realtimeTopic)
                    .setPressureTraffic(false)              // optional 是否是压测数据, 默认是非压测流量, 注意：如果设置为true, 则表示压测流量, 在console上的"是否接收压测流量(订阅关系->订阅关系列表->编辑订阅)"这个checkbox如果没有打勾, 则接收不到这个压测流量
                    .setTraceId("xxx")                      // optional 设置traceId, 便于追踪消息和把脉展示
                    .setSpanId("yyy")                       // optional 设置spanId, 便于追踪消息和把脉展示
                    .addProperty("testKey1", "testValue1")  // optional 添加属性, 可以多次addProperty, http推送时会放到body里边 eg: params=xxx&propertyKey1=propertyValue1
                    .setBody(realtimeBody);

            Result result = producer.txBusinessMessageBuilder(messageBuilder).send();

            // 发送业务消息失败则结束, 业务还会再收到监控消息, 在下次收到监控消息时业务再次发起发送业务消息
            if (result.getCode() != CarreraReturnCode.OK) {
                System.out.println("fail to send business message, result:" + result);
                return;
            }
            System.out.println("succ to send business message, result" + result);

            // 业务消息发送成功, 则需要把之前的监控消息给取消掉
            delayResult = producer.cancelTxMonitorMessageBuilder(cancelDelayMessageBuilder).send();
            if (delayResult.getCode() != CarreraReturnCode.OK) {
                System.out.println("fail to cancel tx monitor message, result:" + delayResult);
                return;
            }
            System.out.println("succ to cancel tx monitor message, result:" + delayResult);
        } else {
            System.out.println("fail to execute local tx");

            // 执行本地事务失败, 则需要把之前的监控消息给取消掉
            // 如果取消失败, 之后业务还会再收到这个监控消息, 业务需要再次发起cancel
            delayResult = producer.cancelTxMonitorMessageBuilder(cancelDelayMessageBuilder).send();
            if (delayResult.getCode() != CarreraReturnCode.OK) {
                System.out.println("fail to cancel tx monitor message, result:" + delayResult);
                return;
            }
            System.out.println("succ to cancel tx monitor message, result:" + delayResult);
        }
    }

    private static boolean localTx() {
        return false;
    }
}