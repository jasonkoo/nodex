package com.lenovo.czlib.nodex.conf;

public class BasicValueParser implements ValueParser {
	public static final Class<?>[] supported_types = new Class<?>[]{
		String.class,Boolean.class,Integer.class,Long.class,
		Float.class,Double.class,Byte.class,Short.class,
		boolean.class,int.class,long.class,
		float.class,double.class,byte.class,short.class
	};
	
	@Override
	public Object parseValue(String value,Class<?> clazz) throws ParseException{
		if( String.class.isAssignableFrom( clazz ) ) return value;
	 	if( Boolean.class.isAssignableFrom( clazz ) || boolean.class.isAssignableFrom( clazz )) return Boolean.parseBoolean( value );
	    if( Byte.class.isAssignableFrom( clazz )|| byte.class.isAssignableFrom( clazz )) return Byte.parseByte( value );
	    if( Short.class.isAssignableFrom( clazz )|| short.class.isAssignableFrom( clazz )) return Short.parseShort( value );
	    if( Integer.class.isAssignableFrom( clazz )|| int.class.isAssignableFrom( clazz )) return Integer.parseInt( value );
	    if( Long.class.isAssignableFrom( clazz )|| long.class.isAssignableFrom( clazz )) return Long.parseLong( value );
	    if( Float.class.isAssignableFrom( clazz )|| float.class.isAssignableFrom( clazz )) return Float.parseFloat( value );
	    if( Double.class.isAssignableFrom( clazz )|| double.class.isAssignableFrom( clazz )) return Double.parseDouble( value );
		throw new ParseException(value,clazz);
	}

}
