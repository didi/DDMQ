package com.xiaojukeji.carrera.producer.examples;

import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.CarreraProducer;
import com.xiaojukeji.carrera.thrift.DelayMeta;
import com.xiaojukeji.carrera.thrift.DelayResult;
import com.xiaojukeji.carrera.thrift.Result;

import java.util.concurrent.TimeUnit;


public class CarreraProducerWithPropertiesExample {

    public static void main(String[] args) throws Exception {
        String msgT = "testMsg";
        String body = String.format("{\"%s\":%d, \"testMsg\":\"%s\"}", "timestamp", System.currentTimeMillis(), msgT);
        String tags = null;

        // 实时消息测试
        send(body, tags);

        // 延迟消息测试
        sendDelay( body, tags);
    }

    public static void send(String body, String tags) throws Exception {
        String topic = "topic_test";

        CarreraConfig config = CarreraProducerExample.getConfig();
        CarreraProducer producer = new CarreraProducer(config);

        producer.start();

        Result ret = producer.messageBuilder()
                .setTopic(topic)
                .setTags(tags)
                .setPressureTraffic(false)  // optional 是否是压测数据, 默认是非压测流量, 注意：如果设置为true, 则表示压测流量, 在console上的"是否接收压测流量(订阅关系->订阅关系列表->编辑订阅)"这个checkbox如果没有打勾, 则接收不到这个压测流量
                .setTraceId("xxx")          // optional 设置traceId, 便于追踪消息和把脉展示
                .setSpanId("yyy")           // optional 设置spanId, 便于追踪消息和把脉展示
                .addProperty("propertyKey1", "propertyValue1")  // optional 添加属性, 可以多次addProperty, http推送时会放到body里边 eg: params=xxx&propertyKey1=propertyValue1
                .setBody(body)
                .send();

        // 强烈建议一定要在日志中打印生产的结果。
        // Result 包含三个属性：code和msg表示生产的结果。key是用来表示消息的唯一ID，后续要追踪这条消息的生产消费情况，都需要这个值。
        System.out.println("send result: " + ret);
        producer.shutdown();
    }

    public static void sendDelay(String body, String tags) throws Exception {
        String delayTopic = "your_delay_topic";

        CarreraConfig config = CarreraProducerExample.getConfig();
        CarreraProducer producer = new CarreraProducer(config);

        producer.start();

        int dmsgtype = 2;  //2-延迟消息  3-延迟循环消息
        long timestamp = System.currentTimeMillis() / 1000 + 600; // 10分钟之后触发

        // 延迟消息需要设置: 1.消息类型, 2.触发时间, 其它属性不需要设置
        DelayMeta delayMeta = new DelayMeta();
        delayMeta.setDmsgtype(dmsgtype);
        delayMeta.setTimestamp(timestamp);

        // 延迟循环消息属性，对应dmsgtype=3
        dmsgtype = 3;  // 2-延迟消息  3-延迟循环消息
        timestamp = System.currentTimeMillis() / 1000 + 600; // 10分钟之后触发第一次
        long expire = System.currentTimeMillis() / 1000 + 3600; // 一小时之后消息过期
        long interval = 10; // 循环间隔是10秒
        long times = 100; // 循环次数是100次

        delayMeta.setTimestamp(timestamp);
        delayMeta.setDmsgtype(dmsgtype);
        delayMeta.setInterval(interval); //循环间隔
        delayMeta.setExpire(expire); //消息触发之后的过期时间
        delayMeta.setTimes(times); //循环执行次数

        DelayResult addRetDelay = producer.addDelayMessageBuilder()
                .setTopic(delayTopic)
                .setDelayMeta(delayMeta)
                .setPressureTraffic(false)  // optional 是否是压测数据, 默认是非压测流量, 注意：如果设置为true, 则表示压测流量, 在console上的"是否接收压测流量(订阅关系->订阅关系列表->编辑订阅)"这个checkbox如果没有打勾, 则接收不到这个压测流量
                .setTraceId("xxx")          // optional 设置traceId, 便于追踪消息和把脉展示
                .setSpanId("yyy")           // optional 设置spanId, 便于追踪消息和把脉展示
                .addProperty("propertyKey1", "propertyValue1")  // optional 添加属性, 可以多次addProperty, http推送时会放到body里边 eg: params=xxx&propertyKey1=propertyValue1
                .addHeader("headerKey1", "headerValue1")        // optional 添加header到http header(必须是http推送才有效), 可以多次addHeader
                .setBody(body)
                .send();

        // 强烈建议一定要在日志中打印生产的结果。
        // DelayResult 包含三个属性：code和msg表示生产的结果。uniqDelayMsgId是用来表示消息的唯一ID，后续要追踪这条消息的生产消费情况，都需要这个值。
        System.out.println("send add delayMsg result: " + addRetDelay);

        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DelayResult cancelRetDelay = producer.cancelDelayMessageBuilder()
                .setTopic(delayTopic)
                .setUniqDelayMsgId(addRetDelay.uniqDelayMsgId)
                .send();

        // 强烈建议一定要在日志中打印生产的结果。
        // DelayResult 包含三个属性：code和msg表示生产的结果。uniqDelayMsgId是用来表示消息的唯一ID，后续要追踪这条消息的生产消费情况，都需要这个值
        System.out.println("send cancel delayMsg result: " + cancelRetDelay);
        producer.shutdown();
    }

    public static void sendDelayLoop( String body, String tags) throws Exception {
        String delayTopic = "your_delay_loop_topic";

        CarreraConfig config = CarreraProducerExample.getConfig();
        CarreraProducer producer = new CarreraProducer(config);

        producer.start();

        // 延迟循环消息需要设置: 1.消息类型, 2.触发时间, 3.循环间隔, 4.循环次数, 5.过期时间 循环次数和过期时间这两个条件有一个满足, 消息就会失效
        int dmsgtype = 3;  //2-延迟消息  3-延迟循环消息
        long timestamp = System.currentTimeMillis() / 1000 + 600; // 10分钟之后触发第一次
        long expire = System.currentTimeMillis() / 1000 + 3600; // 一小时之后消息过期
        long interval = 10; // 循环间隔是10秒
        long times = 100; // 循环次数是100次

        DelayMeta delayMeta = new DelayMeta();
        delayMeta.setTimestamp(timestamp);
        delayMeta.setDmsgtype(dmsgtype);
        delayMeta.setInterval(interval); //循环间隔
        delayMeta.setExpire(expire); //消息触发之后的过期时间
        delayMeta.setTimes(times); //循环执行次数

        DelayResult addRetDelay = producer.addDelayMessageBuilder()
                .setTopic(delayTopic)
                .setDelayMeta(delayMeta)
                .setPressureTraffic(false)  // optional 是否是压测数据, 默认是非压测流量, 注意：如果设置为true, 则表示压测流量, 在console上的"是否接收压测流量(订阅关系->订阅关系列表->编辑订阅)"这个checkbox如果没有打勾, 则接收不到这个压测流量
                .setTraceId("xxx")          // optional 设置traceId, 便于追踪消息和把脉展示
                .setSpanId("yyy")           // optional 设置spanId, 便于追踪消息和把脉展示
                .addProperty("testKey2", "testValue2")    // optional 添加属性, addProperty, http推送时会放到body里边 eg: params=xxx&propertyKey1=propertyValue1
                .addHeader("headerKey1", "headerValue1")  // optional 添加header到http header(必须是http推送才有效), 可以多次addHeader
                .setBody(body)
                .send();

        // 强烈建议一定要在日志中打印生产的结果。
        // DelayResult 包含三个属性：code和msg表示生产的结果。uniqDelayMsgId是用来表示消息的唯一ID，后续要追踪这条消息的生产消费情况，都需要这个值。
        System.out.println("send add delayMsg result: " + addRetDelay);

        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DelayResult cancelRetDelay = producer.cancelDelayMessageBuilder()
                .setTopic(delayTopic)
                .setUniqDelayMsgId(addRetDelay.uniqDelayMsgId)
                .send();

        // 强烈建议一定要在日志中打印生产的结果。
        // DelayResult 包含三个属性：code和msg表示生产的结果。uniqDelayMsgId是用来表示消息的唯一ID，后续要追踪这条消息的生产消费情况，都需要这个值
        System.out.println("send cancel delayMsg result: " + cancelRetDelay);

        producer.shutdown();
    }
}