package com.lenovo.czlib.nodex;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.junit.Test;

public class ZKManagerTest {
	private ZKHelper zkm = new ZKHelper("localhost:2181");
	
	private ZKPathListener zkpl = new ZKPathListener(){

		@Override
		public void onDataChange(String path, byte[] value) {
			try {
				System.out.println("onDataChange:" + path + "=" + new String(value,"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}			
		}

		@Override
		public void onDelete(String path) {
			try {
				System.out.println("onDelete:" + path);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}	
		}

		@Override
		public void onChildrenChange(String path, List<String> subnodes) {
			try {
				System.out.println("onChildrenChange:" + path);
				for(String subnode:subnodes){
					System.out.println(subnode);
				}
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}	
		}

		@Override
		public void onCreate(String path, byte[] value) {
			try {
				System.out.println("onCreate:" + path + "=" + new String(value,"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException(e);
			}
		}
	};
		
	@Test
	public void test() throws Exception {
		String path1 = "/test/01";
		zkm.regZKPathListener(path1, zkpl);
		zkm.set(path1, "Hello".getBytes("UTF-8"));
		zkm.createEphemeralNode(path1, "EphemeralNode", "haha".getBytes("UTF-8"),false);
		
		System.out.println(zkm.exists("/test/01"));
		System.out.println(zkm.exists("/test/01/EphemeralNode"));
		
		List<String> children = zkm.children("/test/01");
		for(String child:children){
			System.out.println("   "+child);
		}
		Thread.sleep(1000);
	}

}
