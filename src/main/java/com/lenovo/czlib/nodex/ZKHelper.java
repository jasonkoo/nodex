package com.lenovo.czlib.nodex;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

/**
 * 用于简化zookeeper的状态管理
 * 
 * @author chenzhao1
 */
public class ZKHelper {
    private static final Logger logger = Logger.getLogger(ZKHelper.class);
    private static final int DEFAULT_SESSION_TIMEOUT = 30000;
    public static byte[] NULL_DATA = new byte[0];
    public static int ANY_VERSION = -1;

    private ZooKeeper zooKeeper;
    private String zookeeperConnStr;

    private Map<String, Set<ZKPathListener>> pathListenerMap = new HashMap<String, Set<ZKPathListener>>();

    private Watcher watcher = new Watcher() {
        @Override
        public void process(WatchedEvent event) {
            if (logger.isDebugEnabled()) {
                logger.debug(event.toString());
            }
            if (event.getState() == KeeperState.Expired) {
                try {
                    if (zooKeeper != null) {
                        try {
                            zooKeeper.close();
                            zooKeeper = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    zooKeeper = new ZooKeeper(zookeeperConnStr, DEFAULT_SESSION_TIMEOUT, watcher);
                } catch (IOException e) {
                    throw new NodexException("Create ZKManager Error .", e);
                }
            } else if (event.getState() == KeeperState.Disconnected) {
                try {
                    if (zooKeeper != null) {
                        try {
                            zooKeeper.close();
                            zooKeeper = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    zooKeeper = new ZooKeeper(zookeeperConnStr, DEFAULT_SESSION_TIMEOUT, watcher);
                } catch (IOException e) {
                    throw new NodexException("Create ZKManager Error .", e);
                }
            }
            if (EventType.None.equals(event.getType())) {
                return;
            } else {
                String path = event.getPath();
                Set<ZKPathListener> pathListeners = pathListenerMap.get(path);
                if (pathListeners != null) {
                    switch (event.getType()) {
                    case NodeChildrenChanged:
                        List<String> subNodes = children(path);
                        for (ZKPathListener pathListener : pathListeners) {
                            try {
                                pathListener.onChildrenChange(path, subNodes);
                            } catch (Exception e) {
                                logger.warn(e, e);
                            }
                        }
                        break;
                    case NodeCreated:
                        for (ZKPathListener pathListener : pathListeners) {
                            try {
                                pathListener.onCreate(path, get(path));
                            } catch (Exception e) {
                                logger.warn(e, e);
                            }
                        }
                        break;
                    case NodeDataChanged:
                        for (ZKPathListener pathListener : pathListeners) {
                            try {
                                pathListener.onDataChange(path, get(path));
                            } catch (Exception e) {
                                logger.warn(e, e);
                            }
                        }
                        break;
                    case NodeDeleted:
                        for (ZKPathListener pathListener : pathListeners) {
                            try {
                                pathListener.onDelete(path);
                            } catch (Exception e) {
                                logger.warn(e, e);
                            }
                        }
                        break;
                    default:
                        break;
                    }
                }
                exists(path);
            }

        }
    };

    public ZKHelper(String zkConnStr) {
        try {
            this.zookeeperConnStr = zkConnStr;
            zooKeeper = new ZooKeeper(zkConnStr, DEFAULT_SESSION_TIMEOUT, watcher);
        } catch (IOException e) {
            throw new NodexException("Create ZKManager Error .", e);
        }
    }

    public ZKHelper(String zkConnStr, int sessionTimeOut) {
        try {
            this.zookeeperConnStr = zkConnStr;
            zooKeeper = new ZooKeeper(zkConnStr, sessionTimeOut, watcher);
        } catch (IOException e) {
            throw new NodexException("Create ZKManager Error .", e);
        }
    }

    /**
     * 设定指定节点的值，如果路径甚至各级父路径不存在，会自动创建 如果失败，会抛出NodexException
     * 
     * @return
     */
    public void set(String path, byte[] data) {
        ZooKeeper zk = getConnectedZK();
        try {
            if (!exists(path)) {
                // 如果不存在则自动创建:
                String[] paths = path.split("/");

                String checkPath = "";
                for (int i = 0; i < paths.length - 1; i++) {
                    String part = paths[i];
                    if (part == null || part.trim().length() == 0) {
                        continue;
                    }
                    checkPath += ("/" + part);
                    if (!exists(checkPath)) {
                        zk.create(checkPath, NULL_DATA, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    }
                    exists(checkPath);
                }
                zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                exists(path);
            } else {
                zk.setData(path, data, ANY_VERSION);
            }
        } catch (Exception e) {
            throw new NodexException(e);
        }
    }

    /**
     * 创建与zookeeper client同时存在的节点。如果zookeeper client断开连接，该节点会被zk服务自动删除，
     * 如果失败，会抛出NodexException
     * 
     * @return
     */
    public String createEphemeralNode(String path, String nodeName, byte[] data, boolean sequential) {
        if (exists(path)) {
            ZooKeeper zk = getConnectedZK();
            String nodePath = path + "/" + nodeName;
            String realPath = null;
            try {
                if (sequential) {
                    realPath = zk.create(nodePath, NULL_DATA, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
                } else {
                    realPath = zk.create(nodePath, NULL_DATA, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                }
                exists(nodePath);
                return realPath;
            } catch (Exception e) {
                throw new NodexException(e);
            }
        }
        throw new NodexException("Parent not must exist before create EPHEMERAL node!");
    }

    /**
     * 检查节点是否存在，如果失败，会抛出NodexException
     * 
     * @return
     */
    public boolean exists(String path) {
        ZooKeeper zk = getConnectedZK();
        boolean watch = pathListenerMap.containsKey(path);
        try {
            if (zk.exists(path, watch) != null) {
                children(path);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new NodexException(e);
        }
    }

    /**
     * 取得节点值，如果失败，会抛出NodexException
     * 
     * @return
     */
    public byte[] get(String path) {
        ZooKeeper zk = getConnectedZK();
        try {
            if (!exists(path)) {
                return null;
            }
            boolean watch = pathListenerMap.containsKey(path);
            return zk.getData(path, watch, null);
        } catch (Exception e) {
            throw new NodexException(e);
        }
    }

    /**
     * 取得子节点列表，如果失败，会抛出NodexException
     * 
     * @return 如果节点不存在，返回null，否则返回子节点列表。
     */
    public List<String> children(String path) {
        ZooKeeper zk = getConnectedZK();
        try {
            boolean watch = pathListenerMap.containsKey(path);
            if (zk.exists(path, watch) != null) { // 为了防止无限递归调用，不能使用ZKManager提供的exists方法，而是用zookeeper原生的exists方法
                return zk.getChildren(path, watch);
            }
            return null;
        } catch (Exception e) {
            throw new NodexException(e);
        }
    }

    /**
     * 注册路径监听器，如果失败，会抛出NodexException
     * 
     * @return
     */
    public void regZKPathListener(String path, ZKPathListener zkPathListener) {
        Set<ZKPathListener> pathListeners = pathListenerMap.get(path);
        if (pathListeners == null) {
            pathListeners = new HashSet<ZKPathListener>();
            pathListenerMap.put(path, pathListeners);
        }
        if (!pathListeners.contains(zkPathListener)) {
            pathListeners.add(zkPathListener);
        }
        if (pathListeners.size() > 100) {
            logger.warn("Too many ZKPathListener keys regested ! Forget call unregZKPathListener() when remove a zookeeper node? Current key count is : "
                    + pathListeners.size());
        }
        exists(path);
    }

    /**
     * 注销路径监听器，如果失败，会抛出NodexException
     * 
     * @return
     */
    public void unregZKPathListener(String path, ZKPathListener zkPathListener) {
        Set<ZKPathListener> pathListeners = pathListenerMap.get(path);
        if (pathListeners != null) {
            pathListeners.remove(zkPathListener);
            if (pathListeners.size() == 0) {
                pathListeners.remove(path);
            }
        }
        exists(path);
    }

    /**
     * 取得已连接的ZooKeeper客户端，如果失败，会抛出NodexException
     * 
     * @return
     */
    public ZooKeeper getConnectedZK() {
        for (int i = 0; i < 30; i++) {
            if (zooKeeper.getState().isAlive()) {
                return zooKeeper;
            } else {
                logger.info("Wait for zookeeper connected, try count :" + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.warn(e);
                }
            }
        }
        throw new NodexException("ZooKeeper client is still not connected . Current state : " + zooKeeper.getState());
    }
}
