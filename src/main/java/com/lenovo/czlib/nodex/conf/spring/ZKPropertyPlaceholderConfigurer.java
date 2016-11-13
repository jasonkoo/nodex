package com.lenovo.czlib.nodex.conf.spring;

import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.lenovo.czlib.nodex.conf.ZKProperties;

public class ZKPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	private String[] zkConfigPaths;
	private String zkConnStr;
	private boolean resolveFromZookeeperFirst = false;

	private ZKProperties zkProperties;

	private boolean inited = false;

	private void init() {
		if (inited) {
			return;
		}
		synchronized (this) {
			if (inited) {
				return;
			}
			if (zkConnStr == null) {
				zkProperties = new ZKProperties(zkConfigPaths, true);
			} else {
				zkProperties = new ZKProperties(zkConnStr, true, zkConfigPaths);
			}
			inited = true;
		}
	}

	protected String resolvePlaceholder(String placeholder, Properties props) {
		String result = null;
		result = resolveFromRuntimeEnv(placeholder);
		if (result == null) {
			if (resolveFromZookeeperFirst) {
				result = resolveFromZK(placeholder);
				if (result == null) {
					result = super.resolvePlaceholder(placeholder, props);
				}
			} else {
				result = super.resolvePlaceholder(placeholder, props);
				if (result == null) {
					result = resolveFromZK(placeholder);
				}
			}
		}
		return result;
	}

	private String resolveFromRuntimeEnv(String placeholder) {
		String value = System.getProperty(placeholder);
		return value;
	}

	protected String resolveFromZK(String placeholder) {
		init();
		if (zkProperties != null) {
			try {
				return zkProperties.getProperty(placeholder);
			} catch (Exception e) {
				logger.warn(e, e);
				return null;
			}
		}
		return null;
	}

	public String[] getZkConfigPaths() {
		return zkConfigPaths;
	}

	public void setZkConfigPaths(String[] zkConfigPaths) {
		if (zkConfigPaths == null) {
			return;
		}
		for (int i = 0; i < zkConfigPaths.length; i++) {
			String zkConfigPath = zkConfigPaths[i];
			if (zkConfigPath.startsWith("${")) {
				zkConfigPath = zkConfigPath.replace("${", "").replace("}", "");
				String env = System.getProperty(zkConfigPath);
				if (env == null || env.trim().length() == 0) {
					env = System.getenv(zkConfigPath);
				}
				zkConfigPaths[i] = env;
			}
		}
		this.zkConfigPaths = zkConfigPaths;
	}

	public String getZkConnStr() {
		return zkConnStr;
	}

	public void setZkConnStr(String zkConnStr) {
		if (zkConnStr != null && zkConnStr.startsWith("${")) {
			zkConnStr = zkConnStr.replace("${", "").replace("}", "");
			String env = System.getProperty(zkConnStr);
			if (env == null || env.trim().length() == 0) {
				env = System.getenv(zkConnStr);
			}
			zkConnStr = env;
		}
		this.zkConnStr = zkConnStr;
	}

	public boolean isResolveFromZookeeperFirst() {
		return resolveFromZookeeperFirst;
	}

	public void setResolveFromZookeeperFirst(boolean resolveFromZookeeperFirst) {
		this.resolveFromZookeeperFirst = resolveFromZookeeperFirst;
	}
}
