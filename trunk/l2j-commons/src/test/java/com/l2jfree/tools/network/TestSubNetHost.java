package com.l2jfree.tools.network;

import junit.framework.TestCase;

public class TestSubNetHost extends TestCase {
	public void testIsInNet() {
		SubNet net = new SubNet("170.0.0.0/32,10.0.0.0/8");
		assertNotNull(net);

		SubNetHost nethost = new SubNetHost("10.1.1.1");

		nethost.addSubNet(net);
		nethost.addSubNet("192.168.0.0", "16");
		assertTrue(nethost.isInSubnet("192.168.0.6"));
		assertFalse(nethost.isInSubnet("10.1.0.1"));
	}
}
