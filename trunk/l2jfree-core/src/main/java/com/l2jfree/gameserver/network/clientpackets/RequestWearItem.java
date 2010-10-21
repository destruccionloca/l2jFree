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

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.datatables.ItemTable;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2TradeList;
import com.l2jfree.gameserver.model.actor.L2Merchant;
import com.l2jfree.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.PcInventory;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.templates.item.L2Item;

/**
 * This class ...
 * 
 * @version $Revision: 1.12.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestWearItem extends L2GameClientPacket
{
	private static final String	_C__C6_REQUESTWEARITEM	= "[C] C6 RequestWearItem";

	//private int _unknow;

	/** List of ItemID to Wear */
	private int					_listId;

	/** Number of Item to Wear */
	private int					_count;

	/** Table of ItemId containing all Item to Wear */
	private int[]				_items;

	private static class RemoveWearItemsTask implements Runnable
	{
		private final L2PcInstance _activeChar;
		
		public RemoveWearItemsTask(L2PcInstance activeChar)
		{
			_activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			_activeChar.destroyWearedItems("Wear", null, true);
		}
	}

	/**
	 * Decrypt the RequestWearItem Client->Server Packet and Create _items table containing all ItemID to Wear.<BR><BR>
	 * 
	 */
	@Override
	protected void readImpl()
	{
		/*_unknow = */readD();
		_listId = readD(); // List of ItemID to Wear
		_count = readD(); // Number of Item to Wear

		if (_count < 0)
			_count = 0;
		if (_count > 100)
			_count = 0; // prevent too long lists

		// Create _items table that will contain all ItemID to Wear
		_items = new int[_count];

		// Fill _items table with all ItemID to Wear
		for (int i = 0; i < _count; i++)
		{
			int itemId = readD();
			_items[i] = itemId;
		}
	}

	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final L2Object merchant = (L2Object)player.getTarget(L2Merchant.class);
		final L2TradeList list = L2MerchantInstance.getTradeList(player, merchant, _listId);
		
		if (list == null)
		{
			sendAF();
			return;
		}

		_listId = list.getListId();

		// Check if the quantity of Item to Wear
		if (_count < 1 || _listId >= 1000000)
		{
			sendAF();
			return;
		}

		// Total Price of the Try On
		long totalPrice = 0;

		// Check for buylist validity and calculates summary values
		int slots = 0;
		int weight = 0;

		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i];

			if (!list.containsItemId(itemId))
			{
				requestFailed(SystemMessageId.NO_INVENTORY_CANNOT_PURCHASE);
				//Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " from account "+player.getAccountName() + " tried to falsify buylist contents.", Config.DEFAULT_PUNISH);
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(itemId);
			weight += template.getWeight();
			slots++;

			totalPrice += Config.WEAR_PRICE;
			if (totalPrice > PcInventory.MAX_ADENA)
			{
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
		}

		// Check the weight
		if (!player.getInventory().validateWeight(weight))
		{
			requestFailed(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}

		// Check the inventory capacity
		if (!player.getInventory().validateCapacity(slots))
		{
			requestFailed(SystemMessageId.SLOTS_FULL);
			return;
		}

		// Charge buyer and add tax to castle treasury if not owned by npc clan because a Try On is not Free
		if ((totalPrice < 0) || !player.reduceAdena("Wear", (int) totalPrice, player.getLastFolkNPC(), false))
		{
			requestFailed(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		// Proceed the wear
		InventoryUpdate playerIU = new InventoryUpdate();
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i];

			/* Already done. Verify and remove?
			if (!list.containsItemId(itemId))
			{
				Util.handleIllegalPlayerAction(player,"Warning!! Character "+player.getName()+" of account "+player.getAccountName()+" sent a false BuyList list_id.",Config.DEFAULT_PUNISH);
				return;
			}
			*/

			// If player doesn't own this item : Add this L2ItemInstance to Inventory and set properties lastchanged to ADDED and _wear to True
			// If player already own this item : Return its L2ItemInstance (will not be destroy because property _wear set to False)
			L2ItemInstance item = player.getInventory().addWearItem("Wear", itemId, player, merchant);

			// Equip player with this item (set its location)
			player.getInventory().equipItemAndRecord(item);

			// Add this Item in the InventoryUpdate Server->Client Packet
			playerIU.addItem(item);
		}

		// Send the InventoryUpdate Server->Client Packet to the player
		// Add Items in player inventory and equip them
		sendPacket(playerIU);

		// Send the StatusUpdate Server->Client Packet to the player with new CUR_LOAD (0x0e) information
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		sendPacket(su);

		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _knownPlayers
		player.broadcastUserInfo();

		// All weared items should be removed in ALLOW_WEAR_DELAY sec.
		ThreadPoolManager.getInstance().scheduleGeneral(new RemoveWearItemsTask(player), Config.WEAR_DELAY * 1000);

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__C6_REQUESTWEARITEM;
	}
}
