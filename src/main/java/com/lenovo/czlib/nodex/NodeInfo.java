package com.lenovo.czlib.nodex;


public class NodeInfo {
	private String id;
	private String appId;
	private long startTime;
	private String ip;
	private int pid;
	private String hostName;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	@Override
	public String toString() {
		return "NodeInfo [\nid=" + id + "\nappName=" + appId + "\nhostName="
				+ hostName + "\nstartTime=" + startTime + "\nip=" + ip
				+ "\npid=" + pid + "\n]";
	}
}
