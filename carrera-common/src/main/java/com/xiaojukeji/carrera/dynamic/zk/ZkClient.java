package com.xiaojukeji.carrera.dynamic.zk;

/**
 * Copyright 2010 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.I0Itec.zkclient.DataUpdater;
import org.I0Itec.zkclient.ExceptionUtil;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkConnection;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkConnection;
import org.I0Itec.zkclient.ZkLock;
import org.I0Itec.zkclient.exception.ZkAuthFailedException;
import org.I0Itec.zkclient.exception.ZkBadVersionException;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.I0Itec.zkclient.exception.ZkTimeoutException;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.SessionExpiredException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.Configuration;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;


public class ZkClient implements Watcher {

    private final static Logger LOG = LoggerFactory.getLogger(ZkClient.class);
    protected static final String JAVA_LOGIN_CONFIG_PARAM = "java.security.auth.login.config";
    protected static final String ZK_SASL_CLIENT = "zookeeper.sasl.client";
    protected static final String ZK_LOGIN_CONTEXT_NAME_KEY = "zookeeper.sasl.clientconfig";

    protected final IZkConnection _connection;
    protected final long _operationRetryTimeoutInMillis;
    private final ConcurrentHashMap<String, Set<IZkChildListener>> _childListener = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<IZkDataListener>> _dataListener = new ConcurrentHashMap<>();
    private final Set<IZkStateListener> _stateListener = new CopyOnWriteArraySet<>();
    private KeeperState _currentState;
    private final ZkLock _zkEventLock = new ZkLock();
    private boolean _shutdownTriggered;
    private ZkEventThread _eventThread;
    private Thread _zookeeperEventThread;
    private ZkSerializer _zkSerializer;
    private volatile boolean _closed;
    private boolean _isZkSaslEnabled;

    public ZkClient(String serverstring) {
        this(serverstring, Integer.MAX_VALUE);
    }

    public ZkClient(String zkServers, int connectionTimeout) {
        this(new ZkConnection(zkServers), connectionTimeout);
    }

    public ZkClient(String zkServers, int sessionTimeout, int connectionTimeout) {
        this(new ZkConnection(zkServers, sessionTimeout), connectionTimeout);
    }

    public ZkClient(String zkServers, int sessionTimeout, int connectionTimeout, ZkSerializer zkSerializer) {
        this(new ZkConnection(zkServers, sessionTimeout), connectionTimeout, zkSerializer);
    }

    public ZkClient(String zkServers, int sessionTimeout, int connectionTimeout, ZkSerializer zkSerializer, IZkStateListener zkStateListener) {
        this(new ZkConnection(zkServers, sessionTimeout), connectionTimeout, zkSerializer, -1, zkStateListener);
    }

    /**
     *
     * @param zkServers
     *            The Zookeeper servers
     * @param sessionTimeout
     *            The session timeout in milli seconds
     * @param connectionTimeout
     *            The connection timeout in milli seconds
     * @param zkSerializer
     *            The Zookeeper data serializer
     * @param operationRetryTimeout
     *            Most operations done through this {@link org.I0Itec.zkclient.ZkClient} are retried in cases like
     *            connection loss with the Zookeeper servers. During such failures, this
     *            <code>operationRetryTimeout</code> decides the maximum amount of time, in milli seconds, each
     *            operation is retried. A value lesser than 0 is considered as
     *            "retry forever until a connection has been reestablished".
     */
    public ZkClient(final String zkServers, final int sessionTimeout, final int connectionTimeout, final ZkSerializer zkSerializer, final long operationRetryTimeout) {
        this(new ZkConnection(zkServers, sessionTimeout), connectionTimeout, zkSerializer, operationRetryTimeout, null);
    }

    public ZkClient(IZkConnection connection) {
        this(connection, Integer.MAX_VALUE);
    }

    public ZkClient(IZkConnection connection, int connectionTimeout) {
        this(connection, connectionTimeout, new SerializableSerializer());
    }

    public ZkClient(IZkConnection zkConnection, int connectionTimeout, ZkSerializer zkSerializer) {
        this(zkConnection, connectionTimeout, zkSerializer, -1, null);
    }

    /**
     *
     * @param zkConnection
     *            The Zookeeper servers
     * @param connectionTimeout
     *            The connection timeout in milli seconds
     * @param zkSerializer
     *            The Zookeeper data serializer
     * @param operationRetryTimeout
     *            Most operations done through this {@link org.I0Itec.zkclient.ZkClient} are retried in cases like
     *            connection loss with the Zookeeper servers. During such failures, this
     *            <code>operationRetryTimeout</code> decides the maximum amount of time, in milli seconds, each
     *            operation is retried. A value lesser than 0 is considered as
     *            "retry forever until a connection has been reestablished".
     */
    public ZkClient(final IZkConnection zkConnection, final int connectionTimeout, final ZkSerializer zkSerializer, final long operationRetryTimeout, final IZkStateListener zkStateListener) {
        if (zkConnection == null) {
            throw new NullPointerException("Zookeeper connection is null!");
        }
        _connection = zkConnection;
        _zkSerializer = zkSerializer;
        _operationRetryTimeoutInMillis = operationRetryTimeout;
        _isZkSaslEnabled = isZkSaslEnabled();
        if(zkStateListener != null) {
            subscribeStateChanges(zkStateListener);
        }

        connect(connectionTimeout, this);
    }

    public ConcurrentHashMap<String/*zk path*/, Set<IZkDataListener>> getDataListener() {
        return _dataListener;
    }

    public ConcurrentHashMap<String/*zk path*/, Set<IZkChildListener>> getChildListener() {
        return _childListener;
    }

    public void setZkSerializer(ZkSerializer zkSerializer) {
        _zkSerializer = zkSerializer;
    }

    public List<String> subscribeChildChanges(String path, IZkChildListener listener) {
        synchronized (_childListener) {
            Set<IZkChildListener> listeners = _childListener.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<IZkChildListener>();
                _childListener.put(path, listeners);
            }
            listeners.add(listener);
        }
        return watchForChilds(path);
    }

    public void unsubscribeChildChanges(String path, IZkChildListener childListener) {
        synchronized (_childListener) {
            final Set<IZkChildListener> listeners = _childListener.get(path);
            if (listeners != null) {
                listeners.remove(childListener);
            }
        }
    }

    public void subscribeDataChanges(String path, IZkDataListener listener) {
        Set<IZkDataListener> listeners;
        synchronized (_dataListener) {
            listeners = _dataListener.get(path);
            if (listeners == null) {
                listeners = new CopyOnWriteArraySet<IZkDataListener>();
                _dataListener.put(path, listeners);
            }
            listeners.add(listener);
        }
        watchForData(path);
        LOG.debug("Subscribed data changes for " + path);
    }

    public void unsubscribeDataChanges(String path, IZkDataListener dataListener) {
        synchronized (_dataListener) {
            final Set<IZkDataListener> listeners = _dataListener.get(path);
            if (listeners != null) {
                listeners.remove(dataListener);
            }
            if (listeners == null || listeners.isEmpty()) {
                _dataListener.remove(path);
            }
        }
    }

    public void subscribeStateChanges(final IZkStateListener listener) {
        synchronized (_stateListener) {
            _stateListener.add(listener);
        }
    }

    public void unsubscribeStateChanges(IZkStateListener stateListener) {
        synchronized (_stateListener) {
            _stateListener.remove(stateListener);
        }
    }

    public void unsubscribeAll() {
        synchronized (_childListener) {
            _childListener.clear();
        }
        synchronized (_dataListener) {
            _dataListener.clear();
        }
        synchronized (_stateListener) {
            _stateListener.clear();
        }
    }

    // </listeners>

    /**
     * Create a persistent node.
     *
     * @param path
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public void createPersistent(String path) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        createPersistent(path, false);
    }

    /**
     * Create a persistent node and set its ACLs.
     *
     * @param path
     * @param createParents
     *            if true all parent dirs are created as well and no {@link ZkNodeExistsException} is thrown in case the
     *            path already exists
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public void createPersistent(String path, boolean createParents) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        createPersistent(path, createParents, ZooDefs.Ids.OPEN_ACL_UNSAFE);
    }

    /**
     * Create a persistent node and set its ACLs.
     *
     * @param path
     * @param acl
     *            List of ACL permissions to assign to the node
     * @param createParents
     *            if true all parent dirs are created as well and no {@link ZkNodeExistsException} is thrown in case the
     *            path already exists
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public void createPersistent(String path, boolean createParents, List<ACL> acl) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        try {
            create(path, null, acl, CreateMode.PERSISTENT);
        } catch (ZkNodeExistsException e) {
            if (!createParents) {
                throw e;
            }
        } catch (ZkNoNodeException e) {
            if (!createParents) {
                throw e;
            }
            String parentDir = path.substring(0, path.lastIndexOf('/'));
            createPersistent(parentDir, createParents, acl);
            createPersistent(path, createParents, acl);
        }
    }

    /**
     * Sets the acl on path
     *
     * @param path
     * @param acl
     *            List of ACL permissions to assign to the path.
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public void setAcl(final String path, final List<ACL> acl) throws ZkException {
        if (path == null) {
            throw new NullPointerException("Missing value for path");
        }

        if (acl == null || acl.size() == 0) {
            throw new NullPointerException("Missing value for ACL");
        }

        if (!exists(path)) {
            throw new RuntimeException("trying to set acls on non existing node " + path);
        }

        retryUntilConnected(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Stat stat = new Stat();
                _connection.readData(path, stat, false);
                _connection.setAcl(path, acl, stat.getAversion());
                return null;
            }
        });
    }

    /**
     * Gets the acl on path
     *
     * @param path
     * @return an entry instance with key = list of acls on node and value = stats.
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public Map.Entry<List<ACL>, Stat> getAcl(final String path) throws ZkException {
        if (path == null) {
            throw new NullPointerException("Missing value for path");
        }

        if (!exists(path)) {
            throw new RuntimeException("trying to get acls on non existing node " + path);
        }

        return retryUntilConnected(new Callable<Map.Entry<List<ACL>, Stat>>() {
            @Override
            public Map.Entry<List<ACL>, Stat> call() throws Exception {
                return _connection.getAcl(path);
            }
        });
    }

    /**
     * Create a persistent node.
     *
     * @param path
     * @param data
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public void createPersistent(String path, Object data) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        create(path, data, CreateMode.PERSISTENT);
    }

    /**
     * Create a persistent node.
     *
     * @param path
     * @param data
     * @param acl
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public void createPersistent(String path, Object data, List<ACL> acl) {
        create(path, data, acl, CreateMode.PERSISTENT);
    }

    /**
     * Create a persistent, sequental node.
     *
     * @param path
     * @param data
     * @return create node's path
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public String createPersistentSequential(String path, Object data) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        return create(path, data, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    /**
     * Create a persistent, sequential node and set its ACL.
     *
     * @param path
     * @param acl
     * @param data
     * @return create node's path
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public String createPersistentSequential(String path, Object data, List<ACL> acl) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        return create(path, data, acl, CreateMode.PERSISTENT_SEQUENTIAL);
    }

    /**
     * Create an ephemeral node.
     *
     * @param path
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public void createEphemeral(final String path) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        create(path, null, CreateMode.EPHEMERAL);
    }

    /**
     * Create an ephemeral node and set its ACL.
     *
     * @param path
     * @param acl
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public void createEphemeral(final String path, final List<ACL> acl) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        create(path, null, acl, CreateMode.EPHEMERAL);
    }

    /**
     * Create a node.
     *
     * @param path
     * @param data
     * @param mode
     * @return create node's path
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public String create(final String path, Object data, final CreateMode mode) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        return create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
    }

    /**
     * Create a node with ACL.
     *
     * @param path
     * @param data
     * @param acl
     * @param mode
     * @return create node's path
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public String create(final String path, Object data, final List<ACL> acl, final CreateMode mode) {
        if (path == null) {
            throw new NullPointerException("Missing value for path");
        }
        if (acl == null || acl.size() == 0) {
            throw new NullPointerException("Missing value for ACL");
        }
        final byte[] bytes = data == null ? null : serialize(data);

        return retryUntilConnected(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return _connection.create(path, bytes, acl, mode);
            }
        });

    }

    /**
     * Create an ephemeral node.
     *
     * @param path
     * @param data
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public void createEphemeral(final String path, final Object data) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        create(path, data, CreateMode.EPHEMERAL);
    }

    /**
     * Create an ephemeral node.
     *
     * @param path
     * @param data
     * @param acl
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public void createEphemeral(final String path, final Object data, final List<ACL> acl) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        create(path, data, acl, CreateMode.EPHEMERAL);
    }

    /**
     * Create an ephemeral, sequential node.
     *
     * @param path
     * @param data
     * @return created path
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public String createEphemeralSequential(final String path, final Object data) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        return create(path, data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    /**
     * Create an ephemeral, sequential node with ACL.
     *
     * @param path
     * @param data
     * @param acl
     * @return created path
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs
     */
    public String createEphemeralSequential(final String path, final Object data, final List<ACL> acl) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        return create(path, data, acl, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    @Override
    public void process(WatchedEvent event) {
        LOG.debug("Received event: " + event);
        _zookeeperEventThread = Thread.currentThread();

        boolean stateChanged = event.getPath() == null;
        boolean znodeChanged = event.getPath() != null;
        boolean dataChanged = event.getType() == EventType.NodeDataChanged || event.getType() == EventType.NodeDeleted || event.getType() == EventType.NodeCreated
                || event.getType() == EventType.NodeChildrenChanged;

        getEventLock().lock();
        try {

            // We might have to install child change event listener if a new node was created
            if (getShutdownTrigger()) {
                LOG.debug("ignoring event '{" + event.getType() + " | " + event.getPath() + "}' since shutdown triggered");
                return;
            }
            if (stateChanged) {
                processStateChanged(event);
            }
            if (dataChanged) {
                processDataOrChildChange(event);
            }
        } finally {
            if (stateChanged) {
                getEventLock().getStateChangedCondition().signalAll();

                // If the session expired we have to signal all conditions, because watches might have been removed and
                // there is no guarantee that those
                // conditions will be signaled at all after an Expired event
                if (event.getState() == KeeperState.Expired) {
                    getEventLock().getZNodeEventCondition().signalAll();
                    getEventLock().getDataChangedCondition().signalAll();
                    // We also have to notify all listeners that something might have changed
                    fireAllEvents();
                }
            }
            if (znodeChanged) {
                getEventLock().getZNodeEventCondition().signalAll();
            }
            if (dataChanged) {
                getEventLock().getDataChangedCondition().signalAll();
            }
            getEventLock().unlock();
            LOG.debug("Leaving process event");
        }
    }

    private void fireAllEvents() {
        for (Entry<String, Set<IZkChildListener>> entry : _childListener.entrySet()) {
            fireChildChangedEvents(entry.getKey(), entry.getValue());
        }
        for (Entry<String, Set<IZkDataListener>> entry : _dataListener.entrySet()) {
            fireDataChangedEvents(entry.getKey(), entry.getValue());
        }
    }

    public List<String> getChildren(String path) {
        return getChildren(path, hasListeners(path));
    }

    protected List<String> getChildren(final String path, final boolean watch) {
        return retryUntilConnected(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                return _connection.getChildren(path, watch);
            }
        });
    }

    /**
     * Counts number of children for the given path.
     *
     * @param path
     * @return number of children or 0 if path does not exist.
     */
    public int countChildren(String path) {
        try {
            return getChildren(path).size();
        } catch (ZkNoNodeException e) {
            return 0;
        }
    }

    protected boolean exists(final String path, final boolean watch) {
        return retryUntilConnected(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return _connection.exists(path, watch);
            }
        });
    }

    public boolean exists(final String path) {
        return exists(path, hasListeners(path));
    }

    private void processStateChanged(WatchedEvent event) {
        LOG.info("zookeeper state changed (" + event.getState() + ")");
        setCurrentState(event.getState());
        if (getShutdownTrigger()) {
            return;
        }
        fireStateChangedEvent(event.getState());
        if (event.getState() == KeeperState.Expired) {
            try {
                reconnect();
                fireNewSessionEvents();
            } catch (final Exception e) {
                LOG.info("Unable to re-establish connection. Notifying consumer of the following exception: ", e);
                fireSessionEstablishmentError(e);
            }
        }
    }

    private void fireNewSessionEvents() {
        for (final IZkStateListener stateListener : _stateListener) {
            _eventThread.send(new ZkEventThread.ZkEvent("New session event sent to " + stateListener) {

                @Override
                public void run() throws Exception {
                    stateListener.handleNewSession();
                }
            });
        }
    }

    private void fireStateChangedEvent(final KeeperState state) {
        for (final IZkStateListener stateListener : _stateListener) {
            _eventThread.send(new ZkEventThread.ZkEvent("State changed to " + state + " sent to " + stateListener) {

                @Override
                public void run() throws Exception {
                    stateListener.handleStateChanged(state);
                }
            });
        }
    }

    private void fireSessionEstablishmentError(final Throwable error) {
        for (final IZkStateListener stateListener : _stateListener) {
            _eventThread.send(new ZkEventThread.ZkEvent("Session establishment error(" + error + ") sent to " + stateListener) {

                @Override
                public void run() throws Exception {
                    stateListener.handleSessionEstablishmentError(error);
                }
            });
        }
    }

    private boolean hasListeners(String path) {
        Set<IZkDataListener> dataListeners = _dataListener.get(path);
        if (dataListeners != null && dataListeners.size() > 0) {
            return true;
        }
        Set<IZkChildListener> childListeners = _childListener.get(path);
        if (childListeners != null && childListeners.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean deleteRecursive(String path) {
        List<String> children;
        try {
            children = getChildren(path, false);
        } catch (ZkNoNodeException e) {
            return true;
        }

        for (String subPath : children) {
            if (!deleteRecursive(path + "/" + subPath)) {
                return false;
            }
        }

        return delete(path);
    }

    private void processDataOrChildChange(WatchedEvent event) {
        final String path = event.getPath();

        if (event.getType() == EventType.NodeChildrenChanged || event.getType() == EventType.NodeCreated || event.getType() == EventType.NodeDeleted) {
            Set<IZkChildListener> childListeners = _childListener.get(path);
            if (childListeners != null && !childListeners.isEmpty()) {
                fireChildChangedEvents(path, childListeners);
            }
        }

        if (event.getType() == EventType.NodeDataChanged || event.getType() == EventType.NodeDeleted || event.getType() == EventType.NodeCreated) {
            Set<IZkDataListener> listeners = _dataListener.get(path);
            if (listeners != null && !listeners.isEmpty()) {
                fireDataChangedEvents(event.getPath(), listeners);
            }
        }
    }

    private void fireDataChangedEvents(final String path, Set<IZkDataListener> listeners) {
        for (final IZkDataListener listener : listeners) {
            _eventThread.send(new ZkEventThread.ZkEvent("Data of " + path + " changed sent to " + listener) {

                @Override
                public void run() throws Exception {
                    // reinstall watch
                    exists(path, true);
                    Stat stat = new Stat();
                    try {
                        Object data = readData(path, stat, true);
                        listener.handleDataChange(path, data, stat);
                    } catch (ZkNoNodeException e) {
                        listener.handleDataDeleted(path);
                    }
                }
            });
        }
    }

    private void fireChildChangedEvents(final String path, Set<IZkChildListener> childListeners) {
        try {
            // reinstall the watch
            for (final IZkChildListener listener : childListeners) {
                _eventThread.send(new ZkEventThread.ZkEvent("Children of " + path + " changed sent to " + listener) {

                    @Override
                    public void run() throws Exception {
                        try {
                            // if the node doesn't exist we should listen for the root node to reappear
                            exists(path);
                            List<String> children = getChildren(path);
                            listener.handleChildChange(path, children);
                        } catch (ZkNoNodeException e) {
                            listener.handleChildChange(path, null);
                        }
                    }
                });
            }
        } catch (Exception e) {
            LOG.error("Failed to fire child changed event. Unable to getChildren.  ", e);
        }
    }

    public boolean waitUntilExists(String path, TimeUnit timeUnit, long time) throws ZkInterruptedException {
        Date timeout = new Date(System.currentTimeMillis() + timeUnit.toMillis(time));
        LOG.debug("Waiting until znode '" + path + "' becomes available.");
        if (exists(path)) {
            return true;
        }
        acquireEventLock();
        try {
            while (!exists(path, true)) {
                boolean gotSignal = getEventLock().getZNodeEventCondition().awaitUntil(timeout);
                if (!gotSignal) {
                    return false;
                }
            }
            return true;
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
    }

    protected Set<IZkDataListener> getDataListener(String path) {
        return _dataListener.get(path);
    }

    private boolean isZkSaslEnabled() {
        boolean isSecurityEnabled = false;
        boolean zkSaslEnabled = Boolean.parseBoolean(System.getProperty(ZK_SASL_CLIENT, "true"));
        String zkLoginContextName = System.getProperty(ZK_LOGIN_CONTEXT_NAME_KEY, "Client");

        if (!zkSaslEnabled) {
            LOG.warn("Client SASL has been explicitly disabled with " + ZK_SASL_CLIENT);
            return false;
        }

        String loginConfigFile = System.getProperty(JAVA_LOGIN_CONFIG_PARAM);
        if (loginConfigFile != null && loginConfigFile.length() > 0) {
            LOG.info("JAAS File name: " + loginConfigFile);
            File configFile = new File(loginConfigFile);
            if (!configFile.canRead()) {
                throw new IllegalArgumentException("File " + loginConfigFile + "cannot be read.");
            }

            try {
                Configuration loginConf = Configuration.getConfiguration();
                isSecurityEnabled = loginConf.getAppConfigurationEntry(zkLoginContextName) != null;
            } catch (Exception e) {
                throw new ZkException(e);
            }
        }
        return isSecurityEnabled;
    }

    public void waitUntilConnected() throws ZkInterruptedException {
        waitUntilConnected(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public boolean waitUntilConnected(long time, TimeUnit timeUnit) throws ZkInterruptedException {
        if (_isZkSaslEnabled) {
            return waitForKeeperState(KeeperState.SaslAuthenticated, time, timeUnit);
        } else {
            return waitForKeeperState(KeeperState.SyncConnected, time, timeUnit);
        }
    }

    public boolean waitForKeeperState(KeeperState keeperState, long time, TimeUnit timeUnit) throws ZkInterruptedException {
        if (_zookeeperEventThread != null && Thread.currentThread() == _zookeeperEventThread) {
            throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
        }
        Date timeout = new Date(System.currentTimeMillis() + timeUnit.toMillis(time));

        LOG.info("Waiting for keeper state " + keeperState);
        acquireEventLock();
        try {
            boolean stillWaiting = true;
            while (_currentState != keeperState) {
                if (!stillWaiting) {
                    return false;
                }
                stillWaiting = getEventLock().getStateChangedCondition().awaitUntil(timeout);
                // Throw an exception in the case authorization fails
                if (_currentState == KeeperState.AuthFailed && _isZkSaslEnabled) {
                    throw new ZkAuthFailedException("Authentication failure");
                }
            }
            LOG.debug("State is " + _currentState);
            return true;
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
    }

    private void acquireEventLock() {
        try {
            getEventLock().lockInterruptibly();
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        }
    }

    /**
     *
     * @param <T>
     * @param callable
     * @return result of Callable
     * @throws ZkInterruptedException
     *             if operation was interrupted, or a required reconnection got interrupted
     * @throws IllegalArgumentException
     *             if called from anything except the ZooKeeper event thread
     * @throws ZkException
     *             if any ZooKeeper exception occurred
     * @throws RuntimeException
     *             if any other exception occurs from invoking the Callable
     */
    public <T> T retryUntilConnected(Callable<T> callable) throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException {
        if (_zookeeperEventThread != null && Thread.currentThread() == _zookeeperEventThread) {
            throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
        }
        final long operationStartTime = System.currentTimeMillis();
        while (true) {
            if (_closed) {
                throw new IllegalStateException("ZkClient already closed!");
            }
            try {
                return callable.call();
            } catch (ConnectionLossException e) {
                // we give the event thread some time to update the status to 'Disconnected'
                Thread.yield();
                waitForRetry();
            } catch (SessionExpiredException e) {
                // we give the event thread some time to update the status to 'Expired'
                Thread.yield();
                waitForRetry();
            } catch (KeeperException e) {
                throw ZkException.create(e);
            } catch (InterruptedException e) {
                throw new ZkInterruptedException(e);
            } catch (Exception e) {
                throw ExceptionUtil.convertToRuntimeException(e);
            }
            // before attempting a retry, check whether retry timeout has elapsed
            if (_operationRetryTimeoutInMillis > -1 && (System.currentTimeMillis() - operationStartTime) >= _operationRetryTimeoutInMillis) {
                throw new ZkTimeoutException("Operation cannot be retried because of retry timeout (" + _operationRetryTimeoutInMillis + " milli seconds)");
            }
        }
    }

    private void waitForRetry() {
        if (_operationRetryTimeoutInMillis < 0) {
            waitUntilConnected();
            return;
        }
        waitUntilConnected(_operationRetryTimeoutInMillis, TimeUnit.MILLISECONDS);
    }

    public void setCurrentState(KeeperState currentState) {
        getEventLock().lock();
        try {
            _currentState = currentState;
        } finally {
            getEventLock().unlock();
        }
    }

    /**
     * Returns a mutex all zookeeper events are synchronized aginst. So in case you need to do something without getting
     * any zookeeper event interruption synchronize against this mutex. Also all threads waiting on this mutex object
     * will be notified on an event.
     *
     * @return the mutex.
     */
    public ZkLock getEventLock() {
        return _zkEventLock;
    }

    public boolean delete(final String path) {
        return delete(path, -1);
    }

    public boolean delete(final String path, final int version) {
        try {
            retryUntilConnected(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    _connection.delete(path, version);
                    return null;
                }
            });

            return true;
        } catch (ZkNoNodeException e) {
            return false;
        }
    }

    private byte[] serialize(Object data) {
        return _zkSerializer.serialize(data);
    }

    @SuppressWarnings("unchecked")
    private <T extends Object> T derializable(byte[] data) {
        if (data == null) {
            return null;
        }
        return (T) _zkSerializer.deserialize(data);
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T readData(String path) {
        return (T) readData(path, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T readData(String path, boolean returnNullIfPathNotExists) {
        T data = null;
        try {
            data = (T) readData(path, null);
        } catch (ZkNoNodeException e) {
            if (!returnNullIfPathNotExists) {
                throw e;
            }
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T readData(String path, Stat stat) {
        return (T) readData(path, stat, hasListeners(path));
    }

    @SuppressWarnings("unchecked")
    protected <T extends Object> T readData(final String path, final Stat stat, final boolean watch) {
        byte[] data = retryUntilConnected(new Callable<byte[]>() {

            @Override
            public byte[] call() throws Exception {
                return _connection.readData(path, stat, watch);
            }
        });
        return (T) derializable(data);
    }

    public void writeData(String path, Object object) {
        writeData(path, object, -1);
    }

    /**
     * Updates data of an existing znode. The current content of the znode is passed to the {@link DataUpdater} that is
     * passed into this method, which returns the new content. The new content is only written back to ZooKeeper if
     * nobody has modified the given znode in between. If a concurrent change has been detected the new data of the
     * znode is passed to the updater once again until the new contents can be successfully written back to ZooKeeper.
     *
     * @param <T>
     * @param path
     *            The path of the znode.
     * @param updater
     *            Updater that creates the new contents.
     */
    @SuppressWarnings("unchecked")
    public <T extends Object> void updateDataSerialized(String path, DataUpdater<T> updater) {
        Stat stat = new Stat();
        boolean retry;
        do {
            retry = false;
            try {
                T oldData = (T) readData(path, stat);
                T newData = updater.update(oldData);
                writeData(path, newData, stat.getVersion());
            } catch (ZkBadVersionException e) {
                retry = true;
            }
        } while (retry);
    }

    public void writeData(final String path, Object datat, final int expectedVersion) {
        writeDataReturnStat(path, datat, expectedVersion);
    }

    public Stat writeDataReturnStat(final String path, Object datat, final int expectedVersion) {
        final byte[] data = serialize(datat);
        return (Stat) retryUntilConnected(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                Stat stat = _connection.writeDataReturnStat(path, data, expectedVersion);
                return stat;
            }
        });
    }

    public void watchForData(final String path) {
        retryUntilConnected(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                _connection.exists(path, true);
                return null;
            }
        });
    }

    /**
     * Installs a child watch for the given path.
     *
     * @param path
     * @return the current children of the path or null if the zk node with the given path doesn't exist.
     */
    public List<String> watchForChilds(final String path) {
        if (_zookeeperEventThread != null && Thread.currentThread() == _zookeeperEventThread) {
            throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
        }
        return retryUntilConnected(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                exists(path, true);
                try {
                    return getChildren(path, true);
                } catch (ZkNoNodeException e) {
                    // ignore, the "exists" watch will listen for the parent node to appear
                }
                return null;
            }
        });
    }

    /**
     * Connect to ZooKeeper.
     *
     * @param maxMsToWaitUntilConnected
     * @param watcher
     * @throws ZkInterruptedException
     *             if the connection timed out due to thread interruption
     * @throws ZkTimeoutException
     *             if the connection timed out
     * @throws IllegalStateException
     *             if the connection timed out due to thread interruption
     */
    public void connect(final long maxMsToWaitUntilConnected, Watcher watcher) throws ZkInterruptedException, ZkTimeoutException, IllegalStateException {
        boolean started = false;
        acquireEventLock();
        try {
            setShutdownTrigger(false);
            _eventThread = new ZkEventThread(_connection.getServers());
            _eventThread.start();
            _connection.connect(watcher);

            LOG.debug("Awaiting connection to Zookeeper server");
            boolean waitSuccessful = waitUntilConnected(maxMsToWaitUntilConnected, TimeUnit.MILLISECONDS);
            if (!waitSuccessful) {
                throw new ZkTimeoutException("Unable to connect to zookeeper server '" + _connection.getServers() + "' with timeout of " + maxMsToWaitUntilConnected + " ms");
            }
            started = true;
        } finally {
            getEventLock().unlock();

            // we should close the zookeeper instance, otherwise it would keep
            // on trying to connect
            if (!started) {
                close();
            }
        }
    }

    public long getCreationTime(String path) {
        acquireEventLock();
        try {
            return _connection.getCreateTime(path);
        } catch (KeeperException e) {
            throw ZkException.create(e);
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
    }

    /**
     * Close the client.
     *
     * @throws ZkInterruptedException
     */
    public void close() throws ZkInterruptedException {
        if (_closed) {
            return;
        }
        LOG.debug("Closing ZkClient...");
        getEventLock().lock();
        try {
            setShutdownTrigger(true);
            _eventThread.interrupt();
            _eventThread.join(2000);
            _connection.close();
            _closed = true;
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
        LOG.debug("Closing ZkClient...done");
    }

    private void reconnect() {
        getEventLock().lock();
        try {
            _connection.close();
            _connection.connect(this);
        } catch (InterruptedException e) {
            throw new ZkInterruptedException(e);
        } finally {
            getEventLock().unlock();
        }
    }

    public void setShutdownTrigger(boolean triggerState) {
        _shutdownTriggered = triggerState;
    }

    public boolean getShutdownTrigger() {
        return _shutdownTriggered;
    }
}
