package com.lenovo.czlib.nodex;

import org.junit.Test;


public class NodexGroupTest {

	@Test
	public void test() throws InterruptedException {
		NodexGroup.joinDefaultGroup();
		System.out.println(NodexGroup.getDefaultGroup().getGroupPath());
		Thread.sleep(6000);
	}

}
