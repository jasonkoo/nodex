package com.lenovo.czlib.nodex;

import org.junit.Assert;
import org.junit.Test;


public class TestInit {

	@Test
	public void test() throws Exception {
		Assert.assertNotNull(NodexContext.getZKHelper());
		Assert.assertNotNull(NodexContext.getZKHelper().getConnectedZK());
		NodexGroup.getCurrentnodeinfo();
	}

}