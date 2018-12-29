package com.xiaojukeji.carrera.nodemgr;


import com.xiaojukeji.carrera.config.CarreraConfig;
import org.junit.After;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;


public class NodeManagerTest {

    private NodeManager nodeMgr;

    @Before
    public void setUp() {
        List<String> hosts = Arrays.asList("127.0.0.1:9613");
        CarreraConfig config = new CarreraConfig();
        nodeMgr = NodeManager.newLocalNodeManager(config, hosts);
    }

    @After
    public void destroy() {
        nodeMgr.shutdown();
    }

    @Test
    public void testConcurrentAccess() throws Exception {
        int concurrency = 200;
        final CountDownLatch latch = new CountDownLatch(concurrency);
        final ExecutorService pool = Executors.newFixedThreadPool(concurrency, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NodeManager Test Pool");
            }
        });

        for (int idx = 0; idx < concurrency; idx ++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    for (int cnt = 0; cnt < 10; cnt ++) {
                        Node node = nodeMgr.getNode();
                        nodeMgr.unhealthyNode(node);
                        System.out.println(Thread.currentThread().getId() + " - " + node);
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        pool.shutdown();
    }

}