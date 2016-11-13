package com.lenovo.czlib.nodex.conf;

public class ParseException extends Exception {
	private static final long serialVersionUID = -4231128012845882224L;
	
	private String value;
	private Class<?> type;
	
	public ParseException(String value, Class<?> type) {
		this.value = value;
		this.type = type;
	}
	
	public String getValue() {
		return value;
	}
	public Class<?> getType() {
		return type;
	}
}
