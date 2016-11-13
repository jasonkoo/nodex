package com.lenovo.czlib.nodex;

public class NodexException extends RuntimeException {
	private static final long serialVersionUID = 8892136321291318921L;

	public NodexException() {
	}

	public NodexException(String msg) {
		super(msg);
	}

	public NodexException(Throwable throwable) {
		super(throwable);
	}

	public NodexException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

}
