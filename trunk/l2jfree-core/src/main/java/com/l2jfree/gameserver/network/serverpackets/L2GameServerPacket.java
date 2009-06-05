/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.network.serverpackets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.SendablePacket;

import com.l2jfree.gameserver.CoreInfo;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.L2GameClient;

/**
 * @author KenM
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient>
{
	protected static final Log _log = LogFactory.getLog(L2GameServerPacket.class);
	
	/**
	 * @see com.l2jserver.mmocore.network.SendablePacket#write()
	 */
	@Override
	protected final void write(L2GameClient client)
	{
		try
		{
			writeImpl();
			writeImpl(client, client.getActiveChar());
		}
		catch (Exception e)
		{
			_log.fatal("Failed writing: " + client + " - " + getType() + " - " + CoreInfo.getVersionInfo(), e);
		}
	}
	
	public void prepareToSend(L2GameClient client, L2PcInstance activeChar)
	{
	}
	
	public void packetSent(L2GameClient client, L2PcInstance activeChar)
	{
	}
	
	protected void writeImpl()
	{
	}
	
	protected void writeImpl(L2GameClient client, L2PcInstance activeChar)
	{
	}
	
	/**
	 * @return a String with this packet name for debuging purposes
	 */
	public String getType()
	{
		return getClass().getSimpleName();
	}
	
	/**
	 * @see org.mmocore.network.SendablePacket#getHeaderSize()
	 */
	@Override
	protected final int getHeaderSize()
	{
		return 2;
	}
	
	/**
	 * @see org.mmocore.network.SendablePacket#writeHeader(int)
	 */
	@Override
	protected final void writeHeader(int dataSize)
	{
		writeH(dataSize + getHeaderSize());
	}
	
	public boolean canBeSentTo(L2GameClient client, L2PcInstance activeChar)
	{
		return true;
	}

	public static int toInt(long var)
	{
		if (var > Integer.MAX_VALUE)
			return Integer.MAX_VALUE - 1; //TODO: HACK TO BYPASS THE EXPLOIT CHECKS WHICH CAN BE REMOVED NOW
		if (var < 0)
			return 0;
		return (int) var;
	}
}
