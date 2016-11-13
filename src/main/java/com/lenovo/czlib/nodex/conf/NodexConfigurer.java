package com.lenovo.czlib.nodex.conf;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.lenovo.czlib.nodex.NodexContext;
import com.lenovo.czlib.nodex.Paths;
import com.lenovo.czlib.nodex.ZKHelper;

/**
 * 基于zookeeper的配置管理工具
 * @author chenzhao1
 *
 */
public class NodexConfigurer {
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final Logger logger = Logger.getLogger(NodexConfigurer.class);
	private final Map<Class<?>,ValueParser> parserMap = new HashMap<Class<?>,ValueParser>();
	private final DataChangeHandler dataChangeHandler = new DataChangeHandler(this);
	private ZKHelper zkh;
	
	public NodexConfigurer(){
		this(NodexContext.getZKHelper());
	}
	
	public NodexConfigurer(ZKHelper zkh){
		this.zkh = zkh;
		BasicValueParser basicValueParser = new BasicValueParser();
		for(Class<?> clazz:BasicValueParser.supported_types){
			parserMap.put(clazz, basicValueParser);
		}
	}
	
	public void addValueParser(Class<?> clazz,ValueParser valueParser){
		parserMap.put(clazz, valueParser);
	}
	
	public <T> T getValue(String key,Class<T> retrunType){
		if(!parserMap.containsKey(retrunType)){
			throw new IllegalArgumentException("Faild to resove ValueParser of type : " + retrunType);
		}
		String value = loadValue(key);
		try {
			return parseTo(value,retrunType);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Faild to parse value.",e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T parseTo(String value,Class<T> retrunType) throws ParseException{
		if(parserMap.containsKey(retrunType)){
			return (T)parserMap.get(retrunType).parseValue(value,retrunType);
		} 
		throw new IllegalArgumentException("Faild to resove ValueParser of type : " + retrunType);
	}
	
	private String loadValue(String key) {
		byte[] data = zkh.get(Paths.getPath(Paths.CONF_ROOT,key));
		if(data!=null && data.length>0){
			return new String(data,UTF8);
		}
		throw new ConfigNotfoundException("Config not found for key : " + key);
	}

	public void subscibe(String key,Object targetObject,String fieldName,ConfigChangeListener configChangeListener){
		try {
			String value= loadValue(key);
			setField(targetObject,fieldName,value);
		} catch (Exception e) {
			logger.warn(e,e);
			throw new ConfigNotfoundException("Config not found for key : " + key);
		}
		zkh.regZKPathListener(Paths.getPath(Paths.CONF_ROOT,key), dataChangeHandler);
		dataChangeHandler.addSubscribe(key, targetObject, fieldName, configChangeListener);
	}
	
	public synchronized void initConfigs(Map<String,String> config,boolean overwrite){
		for(Entry<String, String> entry:config.entrySet()){
			String key = entry.getKey();
			String path = Paths.getPath(Paths.CONF_ROOT,key);
			String value = entry.getValue();
			byte[] data = ZKHelper.NULL_DATA;
			if(value!=null){
				data = value.getBytes(UTF8);
			}
			if(overwrite || !zkh.exists(path)){
				zkh.set(path, data);
				logger.info("Write config item to zookeeper : key = " + key + " , value = " + value);
			}else{
				logger.info("Opmit config key : key = " + key );
			}
		}
	}
	
	void setField(Object targetObject, String fieldName, String strValue)
			throws NoSuchFieldException, IllegalAccessException, ParseException {
		Field field = targetObject.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		Class<?> type = field.getType();
		Object value = parseTo(strValue,type);
		field.set(targetObject, value);
		if(logger.isInfoEnabled()){
			logger.info("set value to field: field = " + field.toGenericString() + " , value = " + strValue);
		}
	}

	protected ZKHelper getZkh() {
		return zkh;
	}
	
}
