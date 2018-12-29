package com.xiaojukeji.carrera.consumer.thrift.client;

import com.xiaojukeji.carrera.consumer.thrift.Context;
import com.xiaojukeji.carrera.consumer.thrift.Message;
import org.junit.Test;

import java.io.File;


public class LowLevelCarreraConsumerTest {
    @Test
    public void test() throws Exception {
        final LowLevelCarreraConfig config = new LowLevelCarreraConfig();
        config.setGroupId("cg_lowlevel_test");
        config.setServers("127.0.0.1:9713");
        config.setCommitAckInterval(10000);
        config.setClusterName("R_test");
        config.setRetryInterval(1000);
        config.setTimeout(30000);
        LowLevelCarreraConsumer consumer = new LowLevelCarreraConsumer(config);
        consumer.startConsume(new MessageProcessor() {
            @Override
            public Result process(Message message, Context context) {
                return Result.SUCCESS;
            }
        });
    }

    @Test
    public void testDir() throws Exception {
        File f = new File("./");
        System.out.println(f.getAbsoluteFile());
    }
}