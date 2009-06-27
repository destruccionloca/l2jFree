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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.ItemContainer;
import com.l2jfree.gameserver.model.itemcontainer.PcFreight;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.ItemList;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.item.L2EtcItemType;

import static com.l2jfree.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;
import static com.l2jfree.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

/**
 * @author -Wooden-
 */
public final class RequestPackageSend extends L2GameClientPacket
{
	private static final String	_C_9F_REQUESTPACKAGESEND	= "[C] 9F RequestPackageSend";
	protected static final Log	_log						= LogFactory.getLog(RequestPackageSend.class.getName());
	private int					_objectID;

	private static final int BATCH_LENGTH = 8; // length of the one item
	private static final int BATCH_LENGTH_FINAL = 12;

	private List<Item> _items = null;

	@Override
	protected void readImpl()
	{
		_objectID = readD();
		int count = readD();
		if (count < 0
				|| count > Config.MAX_ITEM_IN_PACKET
				|| count * (Config.PACKET_FINAL ? BATCH_LENGTH_FINAL : BATCH_LENGTH) != getByteBuffer().remaining())
		{
			return;
		}
		_items = new ArrayList<Item>(count);
		for(int i = 0; i < count; i++)
		{
			int id = readD(); //this is some id sent in PackageSendableList
			long cnt = readCompQ();
			_items.add(new Item(id, cnt));
		}
	}

	/**
	 * @see com.l2jfree.gameserver.network.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		if (_items == null || _items.isEmpty())
			return;

		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		L2PcInstance target = L2PcInstance.load(_objectID);

		try
		{
			PcFreight freight = target.getFreight();
			getClient().getActiveChar().setActiveWarehouse(freight);
			ItemContainer warehouse = player.getActiveWarehouse();
			if (warehouse == null)
				return;
			L2NpcInstance manager = player.getLastFolkNPC();
			if ((manager == null || !player.isInsideRadius(manager, INTERACTION_DISTANCE, false, false)) && !player.isGM())
				return;

			if (warehouse instanceof PcFreight && Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN
					&& player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
			{
				player.sendMessage("Unsufficient privileges.");
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			// Alt game - Karma punishment
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
				return;

			// Freight price from config or normal price per item slot (30)
			long fee = _items.size() * Config.ALT_GAME_FREIGHT_PRICE;
			long currentAdena = player.getAdena();
			int slots = 0;

			Iterator<Item> iter = _items.iterator();
			while (iter.hasNext())
			{
				Item i = iter.next();

				// Check validity of requested item
				L2ItemInstance item = player.checkItemManipulation(i.objectId, i.count, "deposit");
				if (item == null)
				{
					_log.warn("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
					iter.remove();
					continue;
				}

				if (!item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST)
				{
					iter.remove();
					continue;
				}

				// Calculate needed adena and slots
				if (item.getItemId() == ADENA_ID)
					currentAdena -= i.count;
				if (!item.isStackable())
					slots += i.count;
				else if (warehouse.getItemByItemId(item.getItemId()) == null)
					slots++;
			}

			// Item Max Limit Check
			if (!warehouse.validateCapacity(slots))
			{
				sendPacket(new SystemMessage(SystemMessageId.WAREHOUSE_FULL));
				return;
			}

			// Check if enough adena and charge the fee
			if (currentAdena < fee || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
			{
				sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
				return;
			}

			// Proceed to the transfer
			InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
			for (Item i : _items)
			{
				L2ItemInstance oldItem = player.getInventory().getItemByObjectId(i.objectId);
				if (oldItem == null)
				{
					_log.warn("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
					continue;
				}

				if (oldItem.isHeroItem())
					continue;

				L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", i.objectId, i.count, warehouse, player, player.getLastFolkNPC());
				if (newItem == null)
				{
					_log.warn("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
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
		finally
		{
			target.deleteMe();
		}
	}

	/**
	 * @see com.l2jfree.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C_9F_REQUESTPACKAGESEND;
	}

	private class Item
	{
		final int	objectId;
		final long	count;

		public Item(int i, long c)
		{
			objectId = i;
			count = c;
		}
	}
}