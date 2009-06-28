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


import static com.l2jfree.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.Shutdown.DisableType;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.ClanWarehouse;
import com.l2jfree.gameserver.model.itemcontainer.ItemContainer;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.ItemList;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.templates.item.L2EtcItemType;
import com.l2jfree.gameserver.util.IllegalPlayerAction;
import com.l2jfree.gameserver.util.Util;

/**
 * This class ...
 *
 * 31  SendWareHouseDepositList  cd (dd)
 * 
 * @version $Revision: 1.3.4.5 $ $Date: 2005/04/11 10:06:09 $
 */
public class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final String _C__31_SENDWAREHOUSEDEPOSITLIST = "[C] 31 SendWareHouseDepositList";
	private final static Log _log = LogFactory.getLog(SendWareHouseDepositList.class.getName());

	private static final int BATCH_LENGTH = 8; // length of the one item
	private static final int BATCH_LENGTH_FINAL = 12;

	private WarehouseItem _items[] = null;
	
	@Override
	protected void readImpl()
	{
		int count = readD();
		if (count <= 0
				|| count > Config.MAX_ITEM_IN_PACKET
				|| count * (Config.PACKET_FINAL ? BATCH_LENGTH_FINAL : BATCH_LENGTH) != getByteBuffer().remaining())
		{
			return;
		}

		_items = new WarehouseItem[count];
		for (int i = 0; i < count; i++)
		{
			int objId = readD();
			long cnt  = readCompQ();

			if (objId < 1 || cnt < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new WarehouseItem(objId, cnt);
		}
	}

	@Override
	protected void runImpl()
	{
		if (_items == null)
			return;

		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
			return;

		L2NpcInstance manager = player.getLastFolkNPC();
		
		if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
		{
			player.sendMessage("Transactions are not allowed during restart/shutdown.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((manager == null
				|| !manager.isWarehouse()
				|| !player.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, false, false)) && !player.isGM())
			return;
		
		if ((warehouse instanceof ClanWarehouse) && Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.getActiveEnchantItem() != null)
		{
			Util.handleIllegalPlayerAction(player,"Player "+player.getName()+" tried to use enchant Exploit!", IllegalPlayerAction.PUNISH_KICKBAN);
			return;
		}

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Freight price from config or normal price per item slot (30)
		long fee = _items.length * 30;
		long currentAdena = player.getAdena();
		int slots = 0;

		for (WarehouseItem i : _items)
		{
			L2ItemInstance item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "deposit");
			if (item == null)
			{
				_log.warn("Error depositing a warehouse object for char "+player.getName()+" (validity check)");
				return;
			}

			// Calculate needed adena and slots
			if (item.getItemId() == ADENA_ID)
				currentAdena -= i.getCount();
			if (!item.isStackable())
				slots += i.getCount();
			else if (warehouse.getItemByItemId(item.getItemId()) == null)
				slots++;
		}

		// Item Max Limit Check
		if (!warehouse.validateCapacity(slots))
		{
			sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}

		// Check if enough adena and charge the fee
		if (currentAdena < fee || !player.reduceAdena(warehouse.getName(), fee, player.getLastFolkNPC(), false))
		{
			sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (WarehouseItem i : _items)
		{
			// Check validity of requested item
			L2ItemInstance oldItem = player.checkItemManipulation(i.getObjectId(), i.getCount(), "deposit");
			if (oldItem == null)
			{
				_log.warn("Error depositing a warehouse object for char "+player.getName()+" (olditem == null)");
				return;
			}

			if ((oldItem.isHeroItem() && Config.ALT_STRICT_HERO_SYSTEM)
					|| ((warehouse instanceof ClanWarehouse) && !oldItem.isTradeable())
					|| oldItem.getItemType() == L2EtcItemType.QUEST)
				continue;

			L2ItemInstance newItem = player.getInventory().transferItem(warehouse.getName(), i.getObjectId(), i.getCount(), warehouse, player, manager);
			if (newItem == null)
			{
				_log.warn("Error depositing a warehouse object for char "+player.getName()+" (newitem == null)");
				continue;
			}

			if (playerIU != null)
			{
				if (oldItem.getCount() > 0 && oldItem != newItem)
					playerIU.addModifiedItem(oldItem);
				else
					playerIU.addRemovedItem(oldItem);
			}
		}

		// Send updated item list to the player
		if (playerIU != null)
			player.sendPacket(playerIU);
		else
			player.sendPacket(new ItemList(player, false));

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

	private class WarehouseItem
	{
		private final int _objectId;
		private final long _count;
		
		public WarehouseItem(int id, long num)
		{
			_objectId = id;
			_count = num;
		}

		public int getObjectId()
		{
			return _objectId;
		}

		public long getCount()
		{
			return _count;
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__31_SENDWAREHOUSEDEPOSITLIST;
	}
}
