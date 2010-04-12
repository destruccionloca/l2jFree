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

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.Elementals;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.Inventory;
import com.l2jfree.gameserver.model.itemcontainer.PcInventory;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jfree.lang.L2Math;
import com.l2jfree.mmocore.network.SendablePacket;

/**
 * @author KenM
 */
public abstract class L2GameServerPacket extends SendablePacket<L2GameClient, L2GameClientPacket, L2GameServerPacket>
{
	protected static final Log _log = LogFactory.getLog(L2GameServerPacket.class);
	
	protected L2GameServerPacket()
	{
	}
	
	@Override
	protected final void write(L2GameClient client)
	{
		writeImpl(client, client.getActiveChar());
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
		writeImpl();
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
	
	protected final void writePaperdollObjectIds(PcInventory inv, boolean writeJewels)
	{
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_UNDER));
		if (writeJewels)
		{
			writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_REAR));
			writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR));
			writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_NECK));
			writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER));
			writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER));
		}
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_FEET));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_BACK));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_LRHAND));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR2));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_RBRACELET));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_LBRACELET));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO1));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO2));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO3));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO4));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO5));
		writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_DECO6));
		if (Config.PACKET_FINAL)
			writeD(inv.getPaperdollObjectId(Inventory.PAPERDOLL_BELT)); // CT2.3
	}
	
	protected final void writePaperdollItemIds(PcInventory inv, boolean writeJewels)
	{
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_UNDER));
		if (writeJewels)
		{
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_REAR));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LEAR));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_NECK));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER));
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER));
		}
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LRHAND));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR2));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_RBRACELET));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_LBRACELET));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO1));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO2));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO3));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO4));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO5));
		writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_DECO6));
		if (Config.PACKET_FINAL)
			writeD(inv.getPaperdollItemId(Inventory.PAPERDOLL_BELT)); // CT2.3
	}
	
	protected final void writePaperdollAugmentationIds(PcInventory inv, boolean writeJewels)
	{
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_UNDER));
		if (writeJewels)
		{
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_REAR));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LEAR));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_NECK));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RFINGER));
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LFINGER));
		}
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HEAD));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_GLOVES));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_CHEST));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LEGS));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_FEET));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_BACK));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LRHAND));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_HAIR2));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RBRACELET));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LBRACELET));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO1));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO2));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO3));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO4));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO5));
		writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_DECO6));
		if (Config.PACKET_FINAL)
			writeD(inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_BELT)); // CT2.3
	}
}
