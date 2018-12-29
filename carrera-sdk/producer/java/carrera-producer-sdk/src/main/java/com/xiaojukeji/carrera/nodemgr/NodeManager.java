package com.xiaojukeji.carrera.nodemgr;

import com.xiaojukeji.carrera.config.CarreraConfig;
import com.xiaojukeji.carrera.nodemgr.connection.CarreraConnection;
import com.xiaojukeji.carrera.nodemgr.connection.CarreraConnectionFactory;
import com.xiaojukeji.carrera.utils.TimeUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public abstract class NodeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);
    protected volatile GenericKeyedObjectPool<Node, CarreraConnection> connPool;
    private CarreraConfig config;

    protected List<Node> healthyNodes;
    protected Map<Node, NodeInfo> allNodesMap;
    protected ScheduledExecutorService restoreFromCooldownScheduler;
    protected static final double MIN_HEALTHY_RATIO = 0.5;

    public NodeManager(CarreraConfig config) {
        this.config = config;
        allNodesMap = new ConcurrentHashMap<>();
        healthyNodes = Collections.synchronizedList(new ArrayList<Node>());
    }

    public CarreraConfig getConfig() {
        return config;
    }

    public void initConnectionPool() throws Exception {
        connPool = new GenericKeyedObjectPool<>(new CarreraConnectionFactory(config), config);
        for (Node node : getAllNodes()) {
            connPool.addObject(node);
        }
        initScheduler();
    }

    public static NodeManager newLocalNodeManager(CarreraConfig config, List<String> hosts) {
        return new LocalNodeManager(config, hosts);
    }

    public synchronized Node getNode() {
        int cnt = healthyNodes.size();
        if (cnt == 0) { //no more healthy nodes
            return null;
        }
        int idx = RandomUtils.nextInt(cnt);
        return healthyNodes.get(idx);
    }

    public synchronized List<Node> getAllNodes() {
        List<Node> allNodes = new ArrayList<>();
        for (Node node : allNodesMap.keySet()) {
            allNodes.add(node);
        }
        return allNodes;
    }

    public CarreraConnection borrowConnection(long timeout) throws Exception {
        while (true) {
            try {
                return doBorrowConnection(timeout);
            } catch (NoSuchElementException ex) {
                LOGGER.warn("get connection failed", ex);
            }
            Thread.sleep(timeout);
        }
    }

    public synchronized CarreraConnection doBorrowConnection(long timeout) throws Exception {
        Node node = getNode();
        if (node == null) {
            return null;
        }

        return connPool.borrowObject(node, timeout);
    }

    public void returnConnection(CarreraConnection connection) {
        if (allNodesMap.containsKey(connection.getNode())) {
            try {
                connPool.returnObject(connection.getNode(), connection);
            } catch (Exception ex) {
                LOGGER.error("return object failed", ex);
            }
        } else {
            LOGGER.warn("node={} not in nodes map, healthyNodes={}", connection.getNode().toString(), healthyNodes);
        }
    }


    public void shutdown() {
        if (restoreFromCooldownScheduler != null && !restoreFromCooldownScheduler.isShutdown()) {
            restoreFromCooldownScheduler.shutdown();
        }

        connPool.close();
    }

    private void initScheduler() {
        restoreFromCooldownScheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "RestoreFromCooldownScheduler");
            }
        });

        restoreFromCooldownScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                restoreFromCooldown(null);
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    private synchronized void restoreFromCooldown(Node nodeExclude) {
        for (Map.Entry<Node, NodeInfo> nodeEntry : allNodesMap.entrySet()) {
            Node node = nodeEntry.getKey();
            NodeInfo nodeInfo = nodeEntry.getValue();
            if (!node.equals(nodeExclude) && !nodeInfo.isHealthy()) {
                nodeInfo.setHealthy(true);
                nodeInfo.setStartCooldownTime(0);
                if (!healthyNodes.contains(node)) {
                    connPool.clear(node);
                    try {
                        connPool.addObject(node);
                        healthyNodes.add(node);
                        LOGGER.info("restart connections of node:{}", node);
                    } catch (Exception ex) {
                        LOGGER.error("restore node failed", ex);
                        continue;
                    }
                }
                LOGGER.info("restore nodes:{}, current healthy node:{}", node, healthyNodes);
            }
        }
    }

    public void unhealthyNode(Node node) {
        if (node != null) {
            synchronized (this) {
                LOGGER.info("node={}, is unhealthy", node);
                NodeInfo nodeInfo = allNodesMap.get(node);
                nodeInfo.setHealthy(false);
                nodeInfo.setStartCooldownTime(TimeUtils.getCurTime());
                healthyNodes.remove(node);
                int healthyCnt = healthyNodes.size();
                int totalCnt = allNodesMap.size();
                boolean needMore = (int) (Math.floor(totalCnt * MIN_HEALTHY_RATIO)) > healthyCnt;
                if (needMore) {
                    LOGGER.info("restore nodes, exclude={}", node);
                    restoreFromCooldown(node);
                }
            }
        }
    }

}