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
package com.l2jfree.gameserver.network.clientpackets;

import com.l2jfree.gameserver.datatables.HennaTable;
import com.l2jfree.gameserver.datatables.HennaTreeTable;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.item.L2Henna;

/**
 * This class represents a packet sent by the client when a player confirms henna dye
 * selection.
 */
public class RequestHennaEquip extends L2GameClientPacket
{
	private static final String _C__BC_RequestHennaEquip = "[C] bc RequestHennaEquip";

	private int _symbolId;

	/**
	 * packet type id 0xbb
	 * format: cd
	 */
	@Override
	protected void readImpl()
	{
		_symbolId  = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		L2Henna temp = HennaTable.getInstance().getTemplate(_symbolId);
		if (temp == null)
		{
			requestFailed(SystemMessageId.SYMBOL_NOT_FOUND);
			return;
		}
		if (activeChar.getHennaEmptySlots() < 1)
		{
			requestFailed(SystemMessageId.SYMBOLS_FULL);
			return;
		}
		if (!isDrawable(activeChar))
		{
			requestFailed(SystemMessageId.CANT_DRAW_SYMBOL);
			return;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByItemId(temp.getItemId());
		long count = (item == null ? 0 : item.getCount());
		if (count >= temp.getAmount() && activeChar.getAdena() >= temp.getPrice())
		{
			activeChar.addHenna(temp);
			SystemMessage sm = new SystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addItemName(temp.getItemId());
			sm.addItemNumber(temp.getAmount());
			sendPacket(sm);
			activeChar.reduceAdena("Henna", temp.getPrice(), activeChar.getLastFolkNPC(), true);
			L2ItemInstance dye = activeChar.getInventory().destroyItemByItemId("Henna", temp.getItemId(), temp.getAmount(), activeChar, activeChar.getLastFolkNPC());
			// Send inventory update packet
			activeChar.getInventory().updateInventory(dye);
			sendPacket(SystemMessageId.SYMBOL_ADDED);
		}
		else
			sendPacket(SystemMessageId.NUMBER_INCORRECT);

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * Prevents henna drawing exploit: 
	 * 1) talk to L2SymbolMakerInstance 
	 * 2) RequestHennaList
	 * 3) Don't close the window and go to a GrandMaster and change your subclass
	 * 4) Get SymbolMaker range again and press draw
	 * You could draw any kind of henna just having the required subclass...
	 * @param activeChar a player that is not null
	 */
	private final boolean isDrawable(L2PcInstance activeChar)
	{
		for (L2Henna h : HennaTreeTable.getInstance().getAvailableHenna(activeChar))
			if (h.getSymbolId() == _symbolId)
				return true;
		return false;
	}

	@Override
	public String getType()
	{
		return _C__BC_RequestHennaEquip;
	}
}
