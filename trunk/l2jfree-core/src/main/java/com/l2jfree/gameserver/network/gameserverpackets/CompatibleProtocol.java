/**
 * 
 */
package com.l2jfree.gameserver.network.gameserverpackets;

import com.l2jfree.gameserver.loginserverthread.CrossLoginServerThread;

/**
 * @author savormix
 *
 */
public final class CompatibleProtocol extends GameServerBasePacket
{
	/**
	 * A packet that notifies the login server that we can use a fully custom protocol
	 * instead of L2somefork-based
	 */
	public CompatibleProtocol()
	{
		super(CrossLoginServerThread.PROTOCOL_CURRENT, 0xAF);
	}
}
