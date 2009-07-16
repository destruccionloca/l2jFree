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

import com.l2jfree.Config;
import com.l2jfree.gameserver.CoreInfo;
import com.l2jfree.gameserver.model.Elementals;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.lang.L2Math;

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
		catch (RuntimeException e)
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
	
	//TODO: HACK TO BYPASS THE EXPLOIT CHECKS WHICH CAN BE REMOVED NOW
	protected final void writeCompQ(long value)
	{
		if (Config.PACKET_FINAL)
			writeQ(value);
		else
			writeD(L2Math.limit(Integer.MIN_VALUE, value, Integer.MAX_VALUE));
	}
	
	protected final void writeCompH(int value)
	{
		if (Config.PACKET_FINAL)
			writeH(value);
		else
			writeD(value);
	}
	
	public interface ElementalOwner
	{
		public byte getAttackElementType();
		
		public int getAttackElementPower();
		
		public int getElementDefAttr(byte element);
	}
	
	protected final void writeElementalInfo(ElementalOwner owner)
	{
		writeCompH(owner.getAttackElementType());
		writeCompH(owner.getAttackElementPower());
		for (byte i = 0; i < 6; i++)
		{
			writeCompH(owner.getElementDefAttr(i));
		}
	}
	
	protected final void writePlayerElementAttribute(L2PcInstance player)
	{
		byte attackAttribute = player.getAttackElement();
		writeCompH(attackAttribute);
		writeCompH(player.getAttackElementValue(attackAttribute));
		writeCompH(player.getDefenseElementValue(Elementals.FIRE));
		writeCompH(player.getDefenseElementValue(Elementals.WATER));
		writeCompH(player.getDefenseElementValue(Elementals.WIND));
		writeCompH(player.getDefenseElementValue(Elementals.EARTH));
		writeCompH(player.getDefenseElementValue(Elementals.HOLY));
		writeCompH(player.getDefenseElementValue(Elementals.DARK));
	}
}
