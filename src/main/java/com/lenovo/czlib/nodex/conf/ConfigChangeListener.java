package com.lenovo.czlib.nodex.conf;

public interface ConfigChangeListener {
	public void configChanged(String key,Object targetObj,String targetFieldName);
}
