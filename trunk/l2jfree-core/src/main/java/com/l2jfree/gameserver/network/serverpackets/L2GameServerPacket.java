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

import gnu.trove.TIntArrayList;

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
	
	private static final int[] PAPERDOLL_SLOTS_WITH_JEWELS = initPaperdollSlots(true);
	private static final int[] PAPERDOLL_SLOTS_WITHOUT_JEWELS = initPaperdollSlots(false);
	
	protected static int[] getPaperdollSlots(boolean writeJewels)
	{
		return writeJewels ? PAPERDOLL_SLOTS_WITH_JEWELS : PAPERDOLL_SLOTS_WITHOUT_JEWELS;
	}
	
	private static int[] initPaperdollSlots(boolean writeJewels)
	{
		TIntArrayList slots = new TIntArrayList();
		
		slots.add(Inventory.PAPERDOLL_UNDER);
		if (writeJewels)
		{
			slots.add(Inventory.PAPERDOLL_REAR);
			slots.add(Inventory.PAPERDOLL_LEAR);
			slots.add(Inventory.PAPERDOLL_NECK);
			slots.add(Inventory.PAPERDOLL_RFINGER);
			slots.add(Inventory.PAPERDOLL_LFINGER);
		}
		slots.add(Inventory.PAPERDOLL_HEAD);
		slots.add(Inventory.PAPERDOLL_RHAND);
		slots.add(Inventory.PAPERDOLL_LHAND);
		slots.add(Inventory.PAPERDOLL_GLOVES);
		slots.add(Inventory.PAPERDOLL_CHEST);
		slots.add(Inventory.PAPERDOLL_LEGS);
		slots.add(Inventory.PAPERDOLL_FEET);
		slots.add(Inventory.PAPERDOLL_BACK);
		slots.add(Inventory.PAPERDOLL_LRHAND);
		slots.add(Inventory.PAPERDOLL_HAIR);
		slots.add(Inventory.PAPERDOLL_HAIR2);
		slots.add(Inventory.PAPERDOLL_RBRACELET);
		slots.add(Inventory.PAPERDOLL_LBRACELET);
		slots.add(Inventory.PAPERDOLL_DECO1);
		slots.add(Inventory.PAPERDOLL_DECO2);
		slots.add(Inventory.PAPERDOLL_DECO3);
		slots.add(Inventory.PAPERDOLL_DECO4);
		slots.add(Inventory.PAPERDOLL_DECO5);
		slots.add(Inventory.PAPERDOLL_DECO6);
		if (Config.PACKET_FINAL)
			slots.add(Inventory.PAPERDOLL_BELT); // CT2.3
		
		return slots.toNativeArray();
	}
	
	protected final void writePaperdollObjectIds(PcInventory inv, boolean writeJewels)
	{
		for (int slot : L2GameServerPacket.getPaperdollSlots(writeJewels))
			writeD(inv.getPaperdollObjectId(slot));
	}
	
	protected final void writePaperdollItemDisplayIds(PcInventory inv, boolean writeJewels)
	{
		for (int slot : L2GameServerPacket.getPaperdollSlots(writeJewels))
			writeD(inv.getPaperdollItemDisplayId(slot));
	}
	
	protected final void writePaperdollAugmentationIds(PcInventory inv, boolean writeJewels)
	{
		for (int slot : L2GameServerPacket.getPaperdollSlots(writeJewels))
			writeD(inv.getPaperdollAugmentationId(slot));
	}
}
