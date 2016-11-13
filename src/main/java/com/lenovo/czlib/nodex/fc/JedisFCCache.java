package com.lenovo.czlib.nodex.fc;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class JedisFCCache implements FCCache {

    private static final Logger Log = Logger.getLogger(JedisFCCache.class);

    public JedisFCCache(String jedisShards) {
        if (jedisShards == null) {
            throw new IllegalArgumentException("please set 'jedisShards' field before build");
        }
        String[] jedisConnStrArray = jedisShards.split(",");
        List<JedisShardInfo> shardInfoList = new ArrayList<JedisShardInfo>();
        for (String jedisConnStr : jedisConnStrArray) {
            jedisConnStr = jedisConnStr.trim();
            int port = 6379;
            String host = jedisConnStr;
            int portPos = jedisConnStr.lastIndexOf(':');
            if (portPos > 1) {
                host = jedisConnStr.substring(0, portPos);
                port = Integer.parseInt(jedisConnStr.substring(portPos + 1));
            }
            shardInfoList.add(new JedisShardInfo(host, port));
        }
        this.pool = new ShardedJedisPool(new JedisPoolConfig(), shardInfoList);
    }

    public JedisFCCache(ShardedJedisPool pool) {
        this.pool = pool;
    }

    private ShardedJedisPool pool;

    @Override
    public long inrcBy(String cacheKey, long count) {
        ShardedJedis jedis = pool.getResource();
        try {
            return jedis.incrBy(cacheKey, count);
        } catch (Exception e) {
            if (null != jedis && e instanceof JedisConnectionException) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
            Log.error("ex", e);
        } finally {
            if (null != jedis)
                pool.returnResource(jedis);
        }
        return -1;
    }

    @Override
    public void setExpire(String cacheKey, int expire) {
        ShardedJedis jedis = pool.getResource();
        try {
            jedis.expire(cacheKey, expire);
        } catch (Exception e) {
            if (null != jedis && e instanceof JedisConnectionException) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
            Log.error("ex", e);
        } finally {
            if (null != jedis)
                pool.returnResource(jedis);
        }
    }

    @Override
    public long getCount(String cacheKey) {
        ShardedJedis jedis = pool.getResource();
        try {
            String value = jedis.get(cacheKey);
            if (value == null) {
                return -1;
            }
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ex) {
                return -1;
            }
        } catch (Exception e) {
            if (null != jedis && e instanceof JedisConnectionException) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
            Log.error("ex", e);
        } finally {
            if (null != jedis)
                pool.returnResource(jedis);
        }
        return -1;
    }

    @Override
    public void delete(String cacheKey) {
        ShardedJedis jedis = pool.getResource();
        try {
            jedis.del(cacheKey);
        } catch (Exception e) {
            if (null != jedis && e instanceof JedisConnectionException) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
            Log.error("ex", e);
        } finally {
            if (null != jedis)
                pool.returnResource(jedis);
        }
    }

}
