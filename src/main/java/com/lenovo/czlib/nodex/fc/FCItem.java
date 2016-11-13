package com.lenovo.czlib.nodex.fc;

public class FCItem {
	public FCItem(){}
	
	public FCItem(String key, long resetInteval, long ttl, long max) {
		this.key = key;
		this.resetInteval = resetInteval;
		this.ttl = ttl;
		this.max = max;
		this.createTime = System.currentTimeMillis();
		this.nextResetTime = createTime + resetInteval;
	}

	private String key;
	private long resetInteval;
	private boolean allow = true;
	private long createTime,nextResetTime;
	private long ttl;
	private long max;
    private transient String cacheKey;
    private transient long changeKeyTime = 0;

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public long getResetInteval() {
		return resetInteval;
	}

	public void setResetInteval(long resetInteval) {
		this.resetInteval = resetInteval;
	}

	public boolean isAllow() {
		return allow;
	}
	public void setAllow(boolean allow) {
		this.allow = allow;
	}
	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public long getTtl() {
		return ttl;
	}
	public void setTtl(long ttl) {
		this.ttl = ttl;
	}
	public long getMax() {
		return max;
	}
	public void setMax(long max) {
		this.max = max;
	}

	public long getNextResetTime() {
		return nextResetTime;
	}

	public void setNextResetTime(long nextResetTime) {
		this.nextResetTime = nextResetTime;
	}

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public long getChangeKeyTime() {
        return changeKeyTime;
    }

    public void setChangeKeyTime(long changeKeyTime) {
        this.changeKeyTime = changeKeyTime;
    }

    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FCItem [key=").append(key).append(", resetInteval=")
				.append(resetInteval).append(", allow=").append(allow)
				.append(", createTime=").append(createTime)
				.append(", nextResetTime=").append(nextResetTime)
				.append(", ttl=").append(ttl).append(", max=").append(max)
				.append("]");
		return builder.toString();
	}
}
