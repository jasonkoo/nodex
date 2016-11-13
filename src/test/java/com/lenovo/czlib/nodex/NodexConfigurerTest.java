package com.lenovo.czlib.nodex;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.lenovo.czlib.nodex.conf.ConfigChangeListener;
import com.lenovo.czlib.nodex.conf.NodexConfigurer;
import com.lenovo.czlib.nodex.conf.ZKProperties;

public class NodexConfigurerTest {
	private static NodexConfigurer nodexConfigurer = new NodexConfigurer();
	
	@Test
	public void test() throws Exception {
		testInitConf();
		testGetConf();
		testSubscribe();
	}

	private void testSubscribe() throws Exception {
		
		class TestTargetClazz {
			private String x;
			private int y;
			private boolean z;
			public String getX() {
				return x;
			}
			public void setX(String x) {
				this.x = x;
			}
			public int getY() {
				return y;
			}
			public void setY(int y) {
				this.y = y;
			}
			public boolean isZ() {
				return z;
			}
			public void setZ(boolean z) {
				this.z = z;
			}
		};
		TestTargetClazz targetObj = new TestTargetClazz();
		
		ConfigChangeListener ccl = new ConfigChangeListener() {
			
			@Override
			public void configChanged(String key, Object targetObj,
					String targetFieldName) {
				System.out.println(" key = " + key + " , targetObj.class = " + targetObj.getClass().getName() + " , targetFieldName = " + targetFieldName);				
			}
		};
		
		nodexConfigurer.subscibe("com.lenovo.czlib.nodex.test.key4", targetObj, "x",ccl);
		nodexConfigurer.subscibe("com.lenovo.czlib.nodex.test.key2", targetObj, "y",ccl);
		nodexConfigurer.subscibe("com.lenovo.czlib.nodex.test.key3", targetObj, "z",ccl);
		
		Thread.sleep(1000);
		
		Assert.assertEquals("newkeyValue3", targetObj.getX());
		Assert.assertTrue( targetObj.getY() == 30000);
		Assert.assertTrue(targetObj.isZ());
		
		Map<String,String> conf = new HashMap<String,String>();
		conf.put("com.lenovo.czlib.nodex.test.key4", "xxx");
		conf.put("com.lenovo.czlib.nodex.test.key2", "10000");
		conf.put("com.lenovo.czlib.nodex.test.key3", "false");
		nodexConfigurer.initConfigs(conf, true);
		
		Thread.sleep(1000);
		
		Assert.assertEquals("xxx", targetObj.getX());
		Assert.assertTrue( targetObj.getY() == 10000);
		Assert.assertTrue(!targetObj.isZ());
		
		Thread.sleep(1000);
	}

	private void testGetConf() {
		Assert.assertTrue(nodexConfigurer.getValue("com.lenovo.czlib.nodex.test.key2", Integer.class).equals(30000));
		Assert.assertTrue(nodexConfigurer.getValue("com.lenovo.czlib.nodex.test.key3", boolean.class));
	}

	private void testInitConf() {
		ZKHelper zkh = NodexContext.getZKHelper();
		
		Map<String,String> conf = new HashMap<String,String>();
		conf.put("com.lenovo.czlib.nodex.test.key1", "stringvalue");
		conf.put("com.lenovo.czlib.nodex.test.key2", "10000");
		conf.put("com.lenovo.czlib.nodex.test.key3", "true");
		nodexConfigurer.initConfigs(conf, false);
		
		Assert.assertTrue(zkh.exists(Paths.getPath(Paths.CONF_ROOT,"com.lenovo.czlib.nodex.test.key1")));
		Assert.assertTrue(zkh.exists(Paths.getPath(Paths.CONF_ROOT,"com.lenovo.czlib.nodex.test.key2")));
		Assert.assertTrue(zkh.exists(Paths.getPath(Paths.CONF_ROOT,"com.lenovo.czlib.nodex.test.key3")));
		
		conf = new HashMap<String,String>();
		conf.put("com.lenovo.czlib.nodex.test.key2", "20000");
		conf.put("com.lenovo.czlib.nodex.test.key4", "newkeyValue");
		nodexConfigurer.initConfigs(conf, false);
		
		conf = new HashMap<String,String>();
		conf.put("com.lenovo.czlib.nodex.test.key2", "30000");
		conf.put("com.lenovo.czlib.nodex.test.key4", "newkeyValue3");
		nodexConfigurer.initConfigs(conf, true);
	}
	
//	@After
//	public void cleanData() throws Exception{
//		ZKHelper zkh = NodexContext.getZKHelper();
//		ZooKeeper zk = zkh.getConnectedZK();
//		zk.delete(Paths.getConfPath("com.lenovo.czlib.nodex.test.key1"), -1);
//		zk.delete(Paths.getConfPath("com.lenovo.czlib.nodex.test.key2"), -1);
//		zk.delete(Paths.getConfPath("com.lenovo.czlib.nodex.test.key3"), -1);
//		zk.delete(Paths.getConfPath("com.lenovo.czlib.nodex.test.key4"), -1);
//	}
	
	@Test
	public void testZKProperties() throws Exception{
		ZKProperties.inport("localhost:2181", new File("D:/test.properties"), true);
		ZKProperties.export("localhost:2181", new File("D:/out.properties"));
		ZKProperties zkProperties = new ZKProperties(new String[]{"/tset2/a/b","/"},true);
		System.out.println(zkProperties.getProperty("askdfjaskdf"));
	}

}
