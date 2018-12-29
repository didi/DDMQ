package com.xiaojukeji.carrera.dynamic;

import com.xiaojukeji.carrera.dynamic.zk.IZkDataListener;
import com.xiaojukeji.carrera.dynamic.zk.ZkClient;
import com.xiaojukeji.carrera.utils.CommonFastJsonUtils;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.xiaojukeji.carrera.dynamic.ParameterDynamicConfig.CHARSET;


public class ParameterDynamicZookeeper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterDynamicZookeeper.class);

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "ParameterDynamicScheduler"));

    private volatile ParameterDynamicConfig config;
    private volatile ZkClient zkClient;
    private volatile String zkHost;
    private volatile boolean isConfigCentre;
    private volatile boolean useZooKeeper = false;
    private CountDownLatch latch = new CountDownLatch(1);

    private ReentrantReadWriteLock fileLock = new ReentrantReadWriteLock();

    private IZkStateListener zkStateListener = new IZkStateListener() {
        @Override
        public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
            LOGGER.info("ParameterDynamicZookeeper - zkClient WatchedEvent:{}, isConfigCentre:{}", state, isConfigCentre);
            try {
                switch (state) {
                    case Disconnected:
                        useZooKeeper = false;
                        break;
                    case SyncConnected:
                        useZooKeeper = true;
                        if (!isConfigCentre) {
                            syncFromZooKeeper();
                        }
                        break;
                    case Expired:
                        useZooKeeper = false;
                        break;
                }
            } catch (Exception e) {
                LOGGER.error("ParameterDynamicZookeeper Exception", e);
            }
            latch.countDown();
        }

        @Override
        public void handleNewSession() throws Exception {
            useZooKeeper = true;
        }

        @Override
        public void handleSessionEstablishmentError(Throwable throwable) throws Exception {
            LOGGER.error("ParameterDynamicZookeeper Exception - SessionEstablishmentError", throwable);
            useZooKeeper = false;
        }
    };

    private ConcurrentHashMap<String/*zk path*/, Integer> versionMap = new ConcurrentHashMap<>();

    public ParameterDynamicZookeeper(ParameterDynamicConfig config) throws Exception {
        this.config = config;
        this.checkConfig();
        this.zkHost = config.getZooKeeperHost();
        this.isConfigCentre = config.isConfigCentre();
        this.zkClient = getZooKeeperInstance();
        latch.await(3000, TimeUnit.MICROSECONDS);
        if (!this.isConfigCentre) {
            startScheduled();
        }
        LOGGER.info("ParameterDynamicZookeeper Start");
    }

    public ParameterDynamicConfig getConfig() {
        return config;
    }

    public void setConfig(ParameterDynamicConfig config) {
        this.config = config;
    }

    private void checkConfig() throws Exception {
        if (StringUtils.isEmpty(this.config.getZooKeeperHost())) {
            throw new Exception("zooKeeper host can not be null");
        }
    }

    private ZkClient getZooKeeperInstance() throws IOException {
        LOGGER.info("ParameterDynamicZookeeper - creating new ZK instance");
        return new ZkClient(this.zkHost, ParameterDynamicConfig.DEFAULT_SESSION_TIMEOUT, Integer.MAX_VALUE, new BytesPushThroughSerializer(), zkStateListener);
    }

    private void syncFromZooKeeper() throws ZkException, IOException {
        ConcurrentHashMap<String/*zk path*/, Set<IZkDataListener>> listeners = zkClient.getDataListener();
        Enumeration<String> paths = listeners.keys();
        while (paths.hasMoreElements()) {
            String path = paths.nextElement();
            initPathData(path);
        }
    }

    private class ZkChildListener<T> implements IZkChildListener {
        private DataChangeCallback<T> callback;
        private Class<T> clazz;

        public ZkChildListener(DataChangeCallback<T> callback, Class<T> t) {
            this.callback = callback;
            this.clazz = t;
        }

        @Override
        public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
            if (CollectionUtils.isNotEmpty(currentChilds)) {
                for (String currentChild : currentChilds) {
                    String path = parentPath + "/" + currentChild;
                    if (zkClient.getDataListener().containsKey(path)) {
                        LOGGER.debug("path has contains listener, skip, path={}", path);
                        continue;
                    }
                    recursiveWatch(path, getChildren(path), callback, clazz);
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ZkChildListener<?> that = (ZkChildListener<?>) o;

            return (callback != null ? callback.equals(that.callback) : that.callback == null) && (clazz != null ? clazz.equals(that.clazz) : that.clazz == null);
        }

        @Override
        public int hashCode() {
            int result = callback != null ? callback.hashCode() : 0;
            result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
            return result;
        }
    }

    public interface DataChangeCallback<T> {

        void handleDataChange(String dataPath, T data, Stat stat) throws Exception;

        void handleDataDeleted(String dataPath) throws Exception;
    }

    private void writeCacheFile(String localConfigPath, String child, String content) {
        try {
            fileLock.writeLock().lock();
            FileUtils.writeStringToFile(new File(localConfigPath, child), content, CHARSET);
        } catch (Exception e) {
            LOGGER.error("ParameterDynamicZookeeper write local file exception, file={}", localConfigPath + "/" + child, e);
        } finally {
            fileLock.writeLock().unlock();
        }
    }

    private String readCacheFile(File file) {
        try {
            fileLock.readLock().lock();
            return FileUtils.readFileToString(file, CHARSET);
        } catch (Exception e) {
            LOGGER.error("ParameterDynamicZookeeper read local file exception, file={}", file.getAbsolutePath(), e);
        } finally {
            fileLock.readLock().unlock();
        }
        return null;
    }

    private class ZkDataListenerImpl<T> implements IZkDataListener {
        private DataChangeCallback<T> callback;
        private Class<T> clazz;

        public ZkDataListenerImpl(DataChangeCallback<T> callback, Class<T> t) {
            this.callback = callback;
            this.clazz = t;
        }

        @Override
        public void handleDataChange(String dataPath, Object data, Stat stat) throws Exception {
            dataChanged(dataPath, data, stat);
        }

        private void dataChanged(String dataPath, Object data, Stat stat) {
            try {
                String localConfigPath = getLocalConfigPath(dataPath);
                versionMap.put(dataPath, stat.getVersion());
                String content = IOUtils.toString((byte[]) data, CHARSET);
                writeCacheFile(localConfigPath, Integer.toString(stat.getVersion()), content);
                cleanExpiredConfigFile(localConfigPath, stat.getVersion());

                callback.handleDataChange(dataPath, CommonFastJsonUtils.toObject(content, clazz), stat);
            } catch (Exception e) {
                LOGGER.error("DataChanged Exception - dataPath={}", dataPath, e);
            }
        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            callback.handleDataDeleted(dataPath);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ZkDataListenerImpl<?> that = (ZkDataListenerImpl<?>) o;

            return (callback != null ? callback.equals(that.callback) : that.callback == null) && (clazz != null ? clazz.equals(that.clazz) : that.clazz == null);
        }

        @Override
        public int hashCode() {
            int result = callback != null ? callback.hashCode() : 0;
            result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
            return result;
        }
    }

    public <T> String getData(String path, DataChangeCallback<T> callback, Class<T> clazz) throws IOException {
        zkClient.subscribeDataChanges(path, new ZkDataListenerImpl<>(callback, clazz));
        return getData(path, new Stat());
    }

    public <T> void getAndWatch(String path, DataChangeCallback<T> callback, Class<T> clazz) throws Exception {
        ZkDataListenerImpl zkDataListener = new ZkDataListenerImpl<>(callback, clazz);
        zkClient.subscribeDataChanges(path, zkDataListener);
        Stat stat = new Stat();
        String data = getData(path, stat);
        zkDataListener.handleDataChange(path, data.getBytes(CHARSET), stat);
    }

    public <T> void removeWatch(String path, DataChangeCallback<T> callback, Class<T> clazz) {
        zkClient.unsubscribeDataChanges(path, new ZkDataListenerImpl<>(callback, clazz));
    }

    public <T> void recursiveWatch(String path, DataChangeCallback<T> callback, Class<T> clazz) throws Exception {
        recursiveWatch(path, getChildren(path), callback, clazz);
    }

    private <T> void recursiveWatch(String path, List<String> childs, DataChangeCallback<T> callback, Class<T> clazz) throws Exception {
        zkClient.subscribeChildChanges(path, new ZkChildListener<>(callback, clazz));
        if (CollectionUtils.isEmpty(childs)) {
            getAndWatch(path, callback, clazz);
            return;
        }

        for (String child : childs) {
            String childPath = path + "/" + child;
            recursiveWatch(childPath, zkClient.getChildren(childPath), callback, clazz);
        }
    }

    public List<String> getChildren(String path) {
        return zkClient.getChildren(path);
    }

    public String getData(String path) throws IOException {
        return getData(path, new Stat());
    }

    public String getData(String path, Stat stat) throws IOException {
        if (useZooKeeper) {
            byte[] data = zkClient.readData(path, stat);
            String content = IOUtils.toString(data, CHARSET);
            writeCacheFile(getLocalConfigPath(path), Integer.toString(stat.getVersion()), content);
            versionMap.put(path, stat.getVersion());
            return content;
        }

        String localConfigPath = getLocalConfigPath(path);
        File file = getLastVersionConfigFile(localConfigPath);
        if (file == null) {
            LOGGER.error("ParameterDynamicZookeeper Error - GetData Get Last Version Config File Is Empty. zkPath:{}, localPath:{}", path, localConfigPath);
            return null;
        }
        int version = getConfigFileVersion(file);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ParameterDynamicZookeeper GetData zkPath:{}, localPath:{}, version:{}, lastVersion:{}", path, localConfigPath, version, versionMap.get(path));
        }
        versionMap.put(path, version);
        return readCacheFile(file);
    }

    private void createNode(String path) throws ZkException {
        if (StringUtils.isNoneBlank(path) && !zkClient.exists(path)) {
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            createNode(parentPath);
            this.zkClient.create(path, "carrera".getBytes(), CreateMode.PERSISTENT);
        }
    }

    public void setData(String path, String data) throws ZkException {
        setData(path, data, -1);
    }

    public void setData(String path, String data, int expectedVersion) throws ZkException {
        try {
            this.zkClient.writeData(path, data.getBytes(), expectedVersion);
        } catch (ZkNoNodeException e) {
            createNode(path);
            this.zkClient.writeData(path, data.getBytes());
        }
    }

    public void delete(String path) throws ZkException {
        this.zkClient.delete(path);
    }

    private void startScheduled() {
        scheduleSyncZkData();
        scheduleLoadLocalData();
    }

    private void scheduleLoadLocalData() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            ConcurrentHashMap<String/*zk path*/, Set<IZkDataListener>> listeners = zkClient.getDataListener();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("ParameterDynamicZookeeper Scheduler - Get Data From Local File useZooKeeper:{}, watcherMap.size:{}",
                        useZooKeeper, listeners.size());
            }
            if (!useZooKeeper) {
                Enumeration<String> paths = listeners.keys();
                while (paths.hasMoreElements()) {
                    try {
                        String path = paths.nextElement();
                        String localConfigPath = getLocalConfigPath(path);
                        File file = getLastVersionConfigFile(localConfigPath);
                        if (file == null) {
                            LOGGER.error("ParameterDynamicZookeeper Error - Scheduler Get Last Version Config File Is Null. zkPath:{}, localPath:{}", path, localConfigPath);
                            continue;
                        }
                        int version = getConfigFileVersion(file);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("ParameterDynamicZookeeper Load From Local File. zkPath:{}, localPath:{}, fileVersion:{}, lastVersion:{}",
                                    path, localConfigPath, version, versionMap.get(path));
                        }
                        if (!versionMap.containsKey(path) || versionMap.get(path) < version) {
                            try {
                                String data = readCacheFile(file);
                                versionMap.put(path, version);
                                Set<IZkDataListener> subSet = listeners.get(path);
                                if (CollectionUtils.isNotEmpty(subSet)) {
                                    for (IZkDataListener iZkDataListener : subSet) {
                                        iZkDataListener.handleDataChange(path, data, new Stat());
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.error("ParameterDynamicZookeeper Exception - Scheduler Load From Local File", e);
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("ParameterDynamicZookeeper Exception - Scheduler Exception", e);
                    }
                }
            }
        }, 0, ParameterDynamicConfig.GET_DATA_FROM_LOCAL_FILE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void scheduleSyncZkData() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                if (useZooKeeper) {
                    syncFromZooKeeper();
                }
            } catch (Exception e) {
                LOGGER.error("ParameterDynamicZookeeper Exception - Scheduler Exception", e);
            }
        }, 300 * 1000, 300 * 1000, TimeUnit.MILLISECONDS);
    }

    private void initPathData(String path) throws ZkException, IOException {
        if (zkClient.exists(path)) {
            Stat stat = new Stat();
            getData(path, stat);
            String localConfigPath = getLocalConfigPath(path);
            LOGGER.debug("ParameterDynamicZookeeper - SyncFromZooKeeper zkPath:{}, localPath:{}, version:{}",
                    path, localConfigPath, stat.getVersion());
        } else {
            LOGGER.error("ParameterDynamicZookeeper Error - SyncFromZooKeeper Lost A Config File [{}]", path);
        }
    }

    private void stopScheduled() {
        scheduledExecutorService.shutdown();
    }

    public void shutdown() {
        LOGGER.info("ParameterDynamicZookeeper Shutting Down");
        this.stopScheduled();
        try {
            this.zkClient.close();
        } catch (ZkInterruptedException e) {
            LOGGER.error("ParameterDynamicZookeeper Exception - Shutdown", e);
        }
        LOGGER.info("ParameterDynamicZookeeper Shutdown");
    }

    private File[] getLocalConfigFiles(String localPath) {
        File dir = new File(localPath);
        if (dir.exists() && dir.isDirectory()) {
            return dir.listFiles((dir1, name) -> {
                try {
                    int version = Integer.parseInt(name);
                    return version >= 0;
                } catch (Exception e) {
                    return false;
                }
            });
        }
        return null;
    }

    private void cleanExpiredConfigFile(String localPath, int version) {
        File[] configFiles = getLocalConfigFiles(localPath);
        if (ArrayUtils.isNotEmpty(configFiles)) {
            for (File configFile : configFiles) {
                if (Integer.parseInt(configFile.getName()) != version) { //以zk版本为准，删掉所有其他版本
                    FileUtils.deleteQuietly(configFile);
                }
            }
        }
    }

    private String getLocalConfigPath(String zkPath) {
        return ParameterDynamicConfig.LOCAL_CONFIG_FILE_DIR + File.separator + zkPath;
    }

    private File getLastVersionConfigFile(String localPath) {
        int version = -1;
        File[] configFiles = getLocalConfigFiles(localPath);
        if (ArrayUtils.isEmpty(configFiles)) {
            return null;
        }

        File lastVersionFile = null;
        for (File configFile : configFiles) {
            if (Integer.parseInt(configFile.getName()) > version) {
                version = Integer.parseInt(configFile.getName());
                lastVersionFile = configFile;
            }
        }
        return lastVersionFile;
    }

    private int getConfigFileVersion(File file) {
        try {
            return Integer.parseInt(file.getName());
        } catch (Exception e) {
            return -1;
        }
    }

}

