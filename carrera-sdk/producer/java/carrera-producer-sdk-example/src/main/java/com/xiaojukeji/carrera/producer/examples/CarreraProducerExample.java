package com.xiaojukeji.carrera.producer.examples;

import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.producer.CarreraProducer;
import com.xiaojukeji.carrera.producer.CarreraReturnCode;
import com.xiaojukeji.carrera.thrift.Result;
import org.apache.commons.lang.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;


public class CarreraProducerExample {

    public static final String TOPIC = "testTopic"; // create Topic in DDMQ Web Console
    public static final String STRING_MESSAGE_BODY = "this is message body.";

    public static void main(String[] args) throws Exception {

        //
        //--------------- 构造、启动生产实例 ---------------
        //
        CarreraConfig config = getConfig();
        CarreraProducer producer = new CarreraProducer(config);

        producer.start(); // 启动生产实例。这里会进行一些初始化操作。

        //
        //--------------- 生产用法 ---------------
        //

        //0.最简单的用法，只指定topic和字符串消息体(默认按UTF-8编码)。
        Result ret = producer.send(TOPIC, STRING_MESSAGE_BODY);
        //强烈建议一定要在日志中打印生产的结果。
        //Result 包含三个属性：code和msg表示生产的结果。key是用来表示消息的唯一ID，后续要追踪这条消息的生产消费情况，都需要这个值。
        System.out.println(ret);

        if (ret.getCode() == CarreraReturnCode.OK) {
            System.out.println("produce success"); // OK 和 CACHE_OK 两个结果都可以认为是生产成功了。
        } else {
            System.out.println("produce failure"); // 失败的情况，根据code和msg做相应处理。
        }

        //1. 生产二进制数据
        byte[] data = new byte[]{1, 2, 3};
        ret = producer.send(TOPIC, data);
        System.out.println(ret);

        //2. 自己指定消息Key。比如使用业务方的自己的traceId。 消息key只要做到尽量唯一即可。
        String myKey = RandomStringUtils.randomAlphanumeric(CarreraConfig.RANDOM_KEY_SIZE);
        ret = producer.send(TOPIC, STRING_MESSAGE_BODY, myKey);
        System.out.println(ret);

        //3. 指定消息路由。相同hashId的消息，会被存储到同一个Partition中。
        Object obj = new Object();
        long hashId = obj.hashCode();
        ret = producer.sendWithHashId(TOPIC, hashId, STRING_MESSAGE_BODY, myKey);
        System.out.println(ret);

        //4. 发送消息到指定的Partition上。不推荐使用。
        //该方法只在强顺序投递场景下使用，需要使用方自己处理partition失效等问题。
        int targetPartitionId = 1; //需要保证：targetPartitionId >= 0;
        ret = producer.sendWithPartition(TOPIC, targetPartitionId, 0, STRING_MESSAGE_BODY, myKey);
        System.out.println(ret);

        //
        //--------------- 关闭生产实例 ---------------
        //
        producer.shutdown();
    }

    public static CarreraConfig getConfig() {
        CarreraConfig config = new CarreraConfig();
        List<String> servers = new ArrayList<String>(); // pproxy ip list.
        servers.add("127.0.0.1:9613");
        config.setCarreraProxyList(servers);
        config.setCarreraProxyTimeout(100);     //这个Proxy端处理请求的超时时间。写队列超时之后会尝试Cache。Cache成功后会返回CACHE_OK;
        config.setCarreraClientRetry(2);        //客户端重试的次数。在 Proxy Server 短暂不可用（比如重启）的时候会重试。
        config.setCarreraClientTimeout(1000);   //这个是 Client 和 Proxy Server 之间的 Socket 超时时间。一般不建议设太小。必须大于ProxyTimeout的值。
        config.setCarreraPoolSize(100);           //设置 Client 与每台 Proxy Server 的最大连接数。单个连接上的生产请求是串行执行的。
        return config;
    }
}