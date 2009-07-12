package com.l2jfree.tools.network;

import junit.framework.TestCase;

public class TestSubNet extends TestCase {

	public void testCreateWithIP() {
		SubNet net = new SubNet("127.0.0.1");
		assertNotNull(net);
	}

	public void testCreateWithIPAndMask() {
		SubNet net = new SubNet("192.168.0.0/16");
		assertNotNull(net);
	}

	public void testEquals() {
		SubNet net1 = new SubNet("192.168.0.0/16");
		assertNotNull(net1);

		SubNet net2 = new SubNet("192.168.0.0/255.255.0.0");
		assertNotNull(net2);

		assertTrue(net1.equals(net2));
	}

	public void testIsInNet() {
		SubNet net = new SubNet("192.168.0.0/16");
		assertNotNull(net);
		assertTrue(net.isInSubnet("192.168.0.6"));
		assertFalse(net.isInSubnet("10.1.0.1"));
		assertFalse(net.isInSubnet("192.0.0.1"));
	}
}
