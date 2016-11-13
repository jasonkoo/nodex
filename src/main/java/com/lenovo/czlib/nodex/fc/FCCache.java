package com.lenovo.czlib.nodex.fc;

public interface FCCache {
    public long inrcBy(String cacheKey, long count);

    public void setExpire(String cacheKey, int expire);

    public long getCount(String cacheKey);

    public void delete(String cacheKey);

}
