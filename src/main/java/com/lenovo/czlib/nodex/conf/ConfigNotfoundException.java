package com.lenovo.czlib.nodex.conf;

public class ConfigNotfoundException extends RuntimeException {
	private static final long serialVersionUID = -4231128012845882224L;

	public ConfigNotfoundException() {
	}

	public ConfigNotfoundException(String msg) {
		super(msg);
	}

	public ConfigNotfoundException(Throwable throwable) {
		super(throwable);
	}

	public ConfigNotfoundException(String msg, Throwable throwable) {
		super(msg,throwable);
	}

}
