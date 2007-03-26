package net.sf.l2j.tools.network;

import junit.framework.TestCase;

public class TestNet extends TestCase
{
    
    public void testCreateWithIP ()
    {
        Net net = new Net ("127.0.0.1");
        assertNotNull(net);
    }

    public void testCreateWithIPAndMask ()
    {
        Net net = new Net ("192.168.0.0/16");
        assertNotNull(net);
    }

    public void testIsInNet ()
    {
        Net net = new Net ("192.168.0.0/16");
        assertNotNull(net);
        assertTrue(net.isInNet("192.168.0.6"));
    }
}
