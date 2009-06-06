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
import com.l2jfree.gameserver.datatables.ItemTable;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.instancemanager.CastleManorManager;
import com.l2jfree.gameserver.instancemanager.CastleManorManager.CropProcure;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Manor;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2ManorManagerInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.item.L2Item;
import com.l2jfree.gameserver.util.Util;

/**
 * Format: (ch) d [dddd] d: size [ d obj id d item id d manor id d count ]
 * 
 * @author l3x
 * 
 */
public class RequestProcureCropList extends L2GameClientPacket
{
	private static final String	_C__D0_09_REQUESTPROCURECROPLIST	= "[C] D0:09 RequestProcureCropList";

	private int					_size;

	private long[]				_items;																	// count*4

	@Override
	protected void readImpl()
	{
		_size = readD();
		if (_size * (Config.PACKET_FINAL ? 20 : 16) > getByteBuffer().remaining() || _size > 500)
		{
			_size = 0;
			return;
		}

		_items = new long[_size * 4];
		for (int i = 0; i < _size; i++)
		{
			int objId = readD();
			_items[(i * 4)] = objId;
			int itemId = readD();
			_items[i * 4 + 1] = itemId;
			int manorId = readD();
			_items[i * 4 + 2] = manorId;
			long count = 0;
			count = readCompQ();
			if (count > Integer.MAX_VALUE)
				count = Integer.MAX_VALUE;
			_items[i * 4 + 3] = count;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		L2Object target = player.getTarget();

		if (!(target instanceof L2ManorManagerInstance))
			target = player.getLastFolkNPC();

		if (!player.isGM() && (target == null || !(target instanceof L2ManorManagerInstance) || !player.isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false)))
			return;

		if (_size < 1)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2ManorManagerInstance manorManager = (L2ManorManagerInstance) target;

		int currentManorId = manorManager.getCastle().getCastleId();

		// Calculate summary values
		int slots = 0;
		int weight = 0;

		for (int i = 0; i < _size; i++)
		{
			int itemId = (int) _items[i * 4 + 1];
			int manorId = (int) _items[i * 4 + 2];
			long count = _items[i * 4 + 3];

			if (itemId == 0 || manorId == 0 || count == 0)
				continue;
			if (count < 1)
				continue;

			//FIXME: count cannot be higher than MAX_VALUE
			if (count > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.",
						Config.DEFAULT_PUNISH);
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				return;
			}

			Castle castle = CastleManager.getInstance().getCastleById(manorId);
			if (castle == null)
				continue;

			CropProcure crop = castle.getCrop(itemId, CastleManorManager.PERIOD_CURRENT);

			if (crop == null)
				continue;

			int rewardItemId = L2Manor.getInstance().getRewardItem(itemId, crop.getReward());

			L2Item template = ItemTable.getInstance().getTemplate(rewardItemId);

			if (template == null)
				continue;

			weight += count * template.getWeight();

			if (!template.isStackable())
				slots += count;
			else if (player.getInventory().getItemByItemId(itemId) == null)
				slots++;
		}

		if (!player.getInventory().validateWeight(weight))
		{
			sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}

		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}

		// Proceed the purchase
		InventoryUpdate playerIU = new InventoryUpdate();

		for (int i = 0; i < _size; i++)
		{
			int objId = (int) _items[(i * 4)];
			int cropId = (int) _items[i * 4 + 1];
			int manorId = (int) _items[i * 4 + 2];
			long count = _items[i * 4 + 3];

			if (objId == 0 || cropId == 0 || manorId == 0 || count == 0)
				continue;

			if (count < 1)
				continue;

			CropProcure crop = null;

			Castle castle = CastleManager.getInstance().getCastleById(manorId);
			if (castle == null)
				continue;

			crop = castle.getCrop(cropId, CastleManorManager.PERIOD_CURRENT);

			if (crop == null || crop.getId() == 0 || crop.getPrice() == 0)
				continue;

			long fee = 0; // fee for selling to other manors

			int rewardItem = L2Manor.getInstance().getRewardItem(cropId, crop.getReward());

			if (count > crop.getAmount())
				continue;

			long sellPrice = (count * crop.getPrice());
			long rewardPrice = ItemTable.getInstance().getTemplate(rewardItem).getReferencePrice();

			if (rewardPrice == 0)
				continue;

			long rewardItemCount = sellPrice / rewardPrice;
			if (rewardItemCount < 1)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(cropId);
				sm.addItemNumber(count);
				player.sendPacket(sm);
				continue;
			}

			if (manorId != currentManorId)
				fee = sellPrice * 5 / 100; // 5% fee for selling to other manor

			if (player.getInventory().getAdena() < fee)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(cropId);
				sm.addItemNumber(count);
				player.sendPacket(sm);
				sm = new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				player.sendPacket(sm);
				continue;
			}

			// Add item to Inventory and adjust update packet
			L2ItemInstance itemDel = null;
			L2ItemInstance itemAdd = null;
			if (player.getInventory().getItemByObjectId(objId) != null)
			{
				// check if player have correct items count
				L2ItemInstance item = player.getInventory().getItemByObjectId(objId);
				if (item.getCount() < count)
					continue;

				itemDel = player.getInventory().destroyItem("Manor", objId, count, player, manorManager);
				if (itemDel == null)
					continue;

				if (fee > 0)
					player.getInventory().reduceAdena("Manor", fee, player, manorManager);

				crop.setAmount(crop.getAmount() - count);

				if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
					CastleManager.getInstance().getCastleById(manorId).updateCrop(crop.getId(), crop.getAmount(), CastleManorManager.PERIOD_CURRENT);

				itemAdd = player.getInventory().addItem("Manor", rewardItem, rewardItemCount, player, manorManager);
			}
			else
			{
				continue;
			}

			if (itemAdd == null)
				continue;

			playerIU.addRemovedItem(itemDel);
			if (itemAdd.getCount() > rewardItemCount)
				playerIU.addModifiedItem(itemAdd);
			else
				playerIU.addNewItem(itemAdd);

			// Send System Messages
			SystemMessage sm = new SystemMessage(SystemMessageId.TRADED_S2_OF_CROP_S1);
			sm.addItemName(cropId);
			sm.addItemNumber(count);
			player.sendPacket(sm);

			if (fee > 0)
			{
				sm = new SystemMessage(SystemMessageId.S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES);
				sm.addItemNumber(fee);
				player.sendPacket(sm);
			}

			sm = new SystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
			sm.addItemName(cropId);
			sm.addItemNumber(count);
			player.sendPacket(sm);

			if (fee > 0)
			{
				sm = new SystemMessage(SystemMessageId.S1_ADENA_DISAPPEARED);
				sm.addItemNumber(fee);
				player.sendPacket(sm);
			}

			sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(rewardItem);
			sm.addItemNumber(rewardItemCount);
			player.sendPacket(sm);
		}

		// Send update packets
		player.sendPacket(playerIU);

		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

	@Override
	public String getType()
	{
		return _C__D0_09_REQUESTPROCURECROPLIST;
	}
}