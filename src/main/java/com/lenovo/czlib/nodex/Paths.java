package com.lenovo.czlib.nodex;

public class Paths {
	public static final String ROOT = "/com.lenovo.czlib.nodex";
	public static final String CONF_ROOT = ROOT + "/conf";
	public static final String GROUPS_ROOT = ROOT + "/groups";
	public static final String NODES_ROOT = ROOT + "/nodes";
	public static final String FLOWCONTROL_ROOT = ROOT + "/flowcontrol";
	
	public static String getNodeName(String path){
		int pos = path.lastIndexOf("/");
		if(pos>0){
			return path.substring(pos+1);
		}
		throw new IllegalArgumentException("Illegal zookeeper path : " + path);
	}
	
	public static String getPath(String ... parts){
		StringBuilder sb = new StringBuilder();
		for(String part:parts){
			sb.append(part).append("/");
		}
		return formatPath(sb.toString());
	}
	
	public static String formatPath(String path) {
		String formated = path.trim().replaceAll("/+", "/");
		if("/".equals(formated)){
			return path;
		}
		if(!formated.startsWith("/")){
			formated = "/" + formated;
		}
		if(formated.endsWith("/")){
			formated = formated.substring(0,formated.length()-1);
		}
		return formated;
	}

}
