package com.lenovo.czlib.nodex.conf;

public interface ValueParser {
	public Object parseValue(String value,Class<?> clazz)throws ParseException;
}
