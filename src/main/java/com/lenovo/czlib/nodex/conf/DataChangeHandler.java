package com.lenovo.czlib.nodex.conf;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.lenovo.czlib.nodex.Paths;
import com.lenovo.czlib.nodex.ZKPathListener;

class DataChangeHandler implements ZKPathListener {
	private static final Logger logger = Logger.getLogger(DataChangeHandler.class);
	private Map<String,Set<SubscriberInfo>> subscribers 
		= new HashMap<String,Set<SubscriberInfo>>();
	private NodexConfigurer nodexConfigurer;
	
	DataChangeHandler(NodexConfigurer nodxConfigurer) {
		this.nodexConfigurer = nodxConfigurer;
	}
	
	private static class SubscriberInfo{
		Object targetObject;
		String fieldName;
		ConfigChangeListener configChangeListener;
		
		public SubscriberInfo(Object targetObject,String fieldName,ConfigChangeListener configChangeListener){
			this.targetObject = targetObject;
			this.fieldName = fieldName;
			this.configChangeListener = configChangeListener;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((fieldName == null) ? 0 : fieldName.hashCode());
			result = prime * result
					+ ((targetObject == null) ? 0 : targetObject.hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SubscriberInfo other = (SubscriberInfo) obj;
			return targetObject == other.targetObject && fieldName == other.fieldName;
		}
		
	}

	public synchronized void addSubscribe(String key,Object targetObject,String fieldName,ConfigChangeListener configChangeListener){
		if(logger.isInfoEnabled()){
			logger.info("addSubscribe: key = " + key + " , targetObject = " + targetObject.getClass().getName());
		}
		Set<SubscriberInfo> pathSubscribers = subscribers.get(key);
		if(pathSubscribers==null){
			pathSubscribers = new HashSet<SubscriberInfo>();
			subscribers.put(key, pathSubscribers);
		}
		pathSubscribers.add(new SubscriberInfo(targetObject,fieldName,configChangeListener));
	}

	@Override
	public void onDataChange(String path, byte[] value) {
		logger.info("Config changed : path = " + path);
		String key = path.replace(Paths.CONF_ROOT,"");
		Set<SubscriberInfo> pathSubscribers = subscribers.get(key);
		if(pathSubscribers==null){
			return;
		}
		for(SubscriberInfo subscriber:pathSubscribers){
			try {
				nodexConfigurer.setField(subscriber.targetObject, subscriber.fieldName, new String(value,Charset.forName("UTF-8")));
				if(subscriber.configChangeListener!=null){
					subscriber.configChangeListener.configChanged(
							key,
							subscriber.targetObject, 
							subscriber.fieldName);
				}
			} catch (Exception e) {
				logger.error("DataChangeHandler set object value faild . ",e);
			} 
		}
	}

	@Override
	public void onDelete(String path) {}

	@Override
	public void onChildrenChange(String path, List<String> subnodes) {}

	@Override
	public void onCreate(String path, byte[] value) {}
	
	

}
