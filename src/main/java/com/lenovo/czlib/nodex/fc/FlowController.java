package com.lenovo.czlib.nodex.fc;

import gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.lenovo.czlib.nodex.NodexContext;
import com.lenovo.czlib.nodex.Paths;
import com.lenovo.czlib.nodex.ZKHelper;
import com.lenovo.czlib.nodex.ZKPathListener;
import redis.clients.jedis.ShardedJedisPool;

public class FlowController implements ZKPathListener {
    private static final Logger logger = Logger.getLogger(FlowController.class);
    private ZKHelper zkh;
    private String fcRootPath;
    protected FCCache fcCache;
    protected ConcurrentHashMap<String, FCItem> fcItems = new ConcurrentHashMap<String, FCItem>();

    private static FlowController defaultInstance;

    public FlowController(String jedisShards) {
        this(NodexContext.getProperty(NodexContext.PN_NODEX_FC_GROUPID), new JedisFCCache(jedisShards), NodexContext
                .getZKHelper());
    }

    public FlowController(ShardedJedisPool pool) {
        this(NodexContext.getProperty(NodexContext.PN_NODEX_FC_GROUPID), new JedisFCCache(pool), NodexContext
                .getZKHelper());
    }

    public FlowController(String fcGroupId, FCCache fcCache, ZKHelper zkh) {
        this.zkh = zkh;
        this.fcRootPath = Paths.getPath(Paths.FLOWCONTROL_ROOT, fcGroupId);
        if (!zkh.exists(fcRootPath)) {
            zkh.set(fcRootPath, ZKHelper.NULL_DATA);
        }
        zkh.regZKPathListener(fcRootPath, this);
        this.fcCache = fcCache;
        List<String> keys = zkh.children(fcRootPath);
        sync(keys);
    }

    public void save(FCItem fcItem) {
        try {
            cleanOld();
            byte[] data = new Gson().toJson(fcItem).getBytes("UTF-8");
            zkh.set(Paths.getPath(fcRootPath, fcItem.getKey()), data);
            fcItems.put(fcItem.getKey(), fcItem);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void remove(String key) {
        FCItem fcItem = fcItems.get(key);
        if (fcItem != null) {
            try {
                fcItems.remove(key);
                zkh.getConnectedZK().delete(Paths.getPath(fcRootPath, key), -1);
            } catch (Exception e) {
                return;
            }
        }
    }

    public boolean isAllow(String key, long incrCnt) {
        FCItem fcItem = fcItems.get(key);
        if (fcItem == null) {
            return false;
        }
        if (fcItem.getMax() < 0) {
            return true;
        }

        long now = System.currentTimeMillis();
        long count = -1;
        if (now > fcItem.getChangeKeyTime()) {
            long timeSegmentStart = now / fcItem.getResetInteval();
            fcItem.setChangeKeyTime((timeSegmentStart + 1) * fcItem.getResetInteval());
            fcItem.setCacheKey(fcItem.getKey() + "_" + timeSegmentStart);
            count = fcCache.inrcBy(fcItem.getCacheKey(), incrCnt);
            fcCache.setExpire(fcItem.getCacheKey(), (int) (fcItem.getResetInteval() / 1000) * 2);
        } else {
            count = fcCache.inrcBy(fcItem.getCacheKey(), incrCnt);
        }

        if (count > fcItem.getMax()) {
            return false;
        }

        return true;
    }

    public long getCount(String key) {
        FCItem item = fcItems.get(key);
        if (item != null) {
            long timeSegmentStart = System.currentTimeMillis() / item.getResetInteval();
            return fcCache.getCount(key + "_" + timeSegmentStart);
        }
        return -1;
    }

    public FCItem get(String key) {
        return fcItems.get(key);
    }

    public Set<String> keys() {
        return fcItems.keySet();
    }

    @Override
    public void onDataChange(String path, byte[] value) {
        updateFCItem(value);
    }

    @Override
    public void onDelete(String path) {
        String fcKey = Paths.getNodeName(path);
        fcItems.remove(fcKey);
        zkh.unregZKPathListener(path, this);
    }

    @Override
    public void onChildrenChange(String path, List<String> subnodes) {
        if (fcRootPath.equals(path)) {
            sync(subnodes);
        }
    }

    @Override
    public void onCreate(String path, byte[] value) {
        updateFCItem(value);
        zkh.regZKPathListener(path, this);
    }

    private void sync(List<String> subnodes) {
        for (String key : subnodes) {
            if (!fcItems.containsKey(key)) {
                updateFCItem(zkh.get(fcRootPath + "/" + key));
            }
        }
    }

    private void cleanOld() {
        if (fcItems == null || fcItems.size() == 0) {
            return;
        }
        List<String> keyToRemove = new ArrayList<String>();
        for (FCItem item : fcItems.values()) {
            if (item.getCreateTime() + item.getTtl() < System.currentTimeMillis()) {
                keyToRemove.add(item.getKey());
            }
        }
        for (String key : keyToRemove) {
            remove(key);
        }
    }

    private void updateFCItem(byte[] value) {
        try {
            FCItem fcItem = new Gson().fromJson(new String(value, "UTF-8"), FCItem.class);
            fcItems.put(fcItem.getKey(), fcItem);
            zkh.regZKPathListener(fcRootPath + "/" + fcItem.getKey(), this);
        } catch (Exception e) {
            logger.warn(e, e);
        }
    }

}
