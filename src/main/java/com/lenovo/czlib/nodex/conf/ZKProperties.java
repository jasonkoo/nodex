package com.lenovo.czlib.nodex.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import com.lenovo.czlib.nodex.NodexException;
import com.lenovo.czlib.nodex.Paths;
import com.lenovo.czlib.nodex.ZKHelper;

public class ZKProperties extends Properties {
	private static final Logger logger = Logger.getLogger(ZKProperties.class);
	private static final long serialVersionUID = -6201477119405791341L;
	private String[] zkPaths;
	private NodexConfigurer nodexConfigurer;
	
	public ZKProperties(){
		this(true);
	}
	
	public ZKProperties(boolean lazy){
		nodexConfigurer = new NodexConfigurer();
		init(lazy);
	}
	
	public ZKProperties(String[] configPaths){
		this(configPaths,true);
	}
	
	public ZKProperties(String[] configPaths,boolean lazy){
		nodexConfigurer = new NodexConfigurer();
		this.zkPaths = configPaths;
		init(lazy);
	}
	
	public ZKProperties(String zkConnStr,boolean lazy,String ... configPaths){
		nodexConfigurer = new NodexConfigurer(new ZKHelper(zkConnStr));
		this.zkPaths = configPaths;
		init(lazy);
	}
	
	private void init(boolean lazy){
		if(zkPaths==null || zkPaths.length==0){
			zkPaths = new String[]{"/"};
		}
		if(!lazy){
			for(int i=0;i<zkPaths.length;i++){
				String path = Paths.formatPath(zkPaths[i]);
				zkPaths[i] = path;
				List<String> children = nodexConfigurer.getZkh().children(Paths.getPath(Paths.CONF_ROOT,path));
				if(children!=null){
					for(String child:children){
						getProperty(child);
					}
				}
			}
		}
	}

	@Override
	public String getProperty(String key) {
		String value = super.getProperty(key);
		if(value==null){
			for(String path:zkPaths){
				try {
					value = nodexConfigurer.getValue(Paths.getPath(path,key), String.class);
					logger.info("load value form zookeeper , key=" + key + " , value= " + value);
				} catch (ConfigNotfoundException e) {
					if(logger.isDebugEnabled()){
						logger.debug("config not found:" + Paths.getPath(path,key));
					}
				}
				if(value!=null){
					this.setProperty(key, value);
					return value;
				}
			}
		}
		return value;
	}
	
	@Override
	public synchronized Object get(Object arg0) {
		if(arg0 instanceof String){
			return getProperty((String)arg0);
		}
		return null;
	}

	public static void export(String zkConnStr,File targetFile) throws Exception{
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(targetFile),"UTF-8");
		ZKHelper zkh = new ZKHelper(zkConnStr);
		NodexConfigurer configurer = new NodexConfigurer(zkh);
		
		Map<String,TreeSet<String>> pathConfigMap = new HashMap<String,TreeSet<String>>();
		export(configurer,pathConfigMap,"/");
		
		List<String> paths = new ArrayList<String>();
		paths.addAll(pathConfigMap.keySet());
		Collections.sort(paths);
		
		for(String path:paths){
			out.write("[");
			out.write(path);
			out.write("]\n");
			TreeSet<String> set = pathConfigMap.get(path);
			for(String line:set){
				out.write(line);
				out.write("\n");
			}
			out.write("\n");
		}
		
		out.close();
		zkh.getConnectedZK().close();
	}
	
	private static void export(NodexConfigurer configurer,Map<String,TreeSet<String>> pathConfigMap,String path){
		List<String> children = configurer.getZkh().children(Paths.getPath(Paths.CONF_ROOT,path));
		if(children!=null){
			for(String child:children){
				export(configurer,pathConfigMap,Paths.getPath(path,child));
			}
		}
		String value = null;
		try {
			value = configurer.getValue(path, String.class);
		} catch (ConfigNotfoundException e) {
			return;
		}
		if(value!=null && value.length()>0){
			if("/".equals(path)){
				return;
			}
			int pos = path.lastIndexOf('/');
			String parent = null;
			String key = null;
			if(pos==0){
				parent = "/";
				key = path.substring(1);
			}else{
				parent = path.substring(0,pos);
				key = path.substring(pos+1);
			}
			value = key + "=" + value;
			TreeSet<String> pathConfig = pathConfigMap.get(parent);
			if(pathConfig==null){
				pathConfig = new TreeSet<String>();
				pathConfigMap.put(parent, pathConfig);
			}
			pathConfig.add(value);
		}
	}
	
	public static void inport(String zkConnStr,File configFile,boolean overwrite) throws Exception{
		FileInputStream in = new FileInputStream(configFile);
		ZKHelper zkh = new ZKHelper(zkConnStr);
		NodexConfigurer configurer = new NodexConfigurer(zkh);
		Map<String,String> properties = new HashMap<String,String>();
		
		LineIterator lit = IOUtils.lineIterator(in,"UTF-8");
		String path = null;
		while(lit.hasNext()){
			String line = lit.nextLine().trim();
			if(line.length()==0 || line.startsWith("#")){
				continue;
			}
			if(line.matches("^\\[.+\\]$")){
				path = line.substring(1,line.length()-1);
				if(!path.startsWith("/")){
					throw new NodexException("Invalid config path :" + path);
				}
				if(path.endsWith("/")){
					path = path.substring(0,path.length()-1);
				}
				continue;
			}
			int pos = line.indexOf('=');
			if(pos<1){
				throw new NodexException("Invalid config item :" + line);
			}
			String key = line.substring(0,pos).trim();
			String value = line.substring(pos+1).trim();
			if(key.contains("/")){
				throw new NodexException("Invalid config item (key must not contains '/') :" + line);
			}
			if(path==null){
				throw new NodexException("Invalid config file format , path info required (e.g. [/com/lenovo]) :" + line);
			}
			
			properties.put(Paths.getPath(path,key), value);
		}
		
		configurer.initConfigs(properties, overwrite);
		
		in.close();
		zkh.getConnectedZK().close();
	}

}
