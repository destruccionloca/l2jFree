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
import com.l2jfree.gameserver.instancemanager.CastleManorManager.SeedProduction;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.instance.L2ManorManagerInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.item.L2Item;

/**
 * Format: cdd[dd]
 * c    // id (0xC4)
 * 
 * d    // manor id
 * d    // seeds to buy
 * [
 * d    // seed id
 * d    // count
 * ]
 * @author l3x
 */ 

public class RequestBuySeed extends L2GameClientPacket
{
	private static final String _C__C4_REQUESTBUYSEED = "[C] C4 RequestBuySeed";

	private int _count;
	private int _manorId;
	private int[] _items; // size _count * 2

    @Override
	protected void readImpl()
	{
		_manorId = readD();
		_count = readD();
		// check values
		if (_count > 500 || _count * 8 < getByteBuffer().remaining() || _count < 1)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		_items = new int[_count * 2];

		for (int i = 0; i < _count; i++)
		{
			int itemId = readD();
			_items[(i * 2)] = itemId;
			long cnt = readD();
			if (cnt >= Integer.MAX_VALUE || cnt < 1)
			{
				_items = null;
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			_items[i * 2 + 1] = (int) cnt;
		}
	}

	@Override
	protected void runImpl()
	{
		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;

		L2PcInstance player = getClient().getActiveChar();
		if (player == null) return;

		L2Object target = player.getTarget();
		if (!(target instanceof L2ManorManagerInstance))
			target = player.getLastFolkNPC();
		if (!(target instanceof L2ManorManagerInstance))
			return;
		
		Castle castle = CastleManager.getInstance().getCastleById(_manorId);

		for (int i = 0; i < _count; i++)
		{
			int seedId = _items[(i * 2)];
			int count = _items[i * 2 + 1];
			int price = 0;
			int residual = 0;

			SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			price = seed.getPrice();
			residual = seed.getCanProduce();

			if (price <= 0 || residual < count)
			{
				//any message, or is it server's misconfiguration?
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			totalPrice += count * price;

			L2Item template = ItemTable.getInstance().getTemplate(seedId);
			totalWeight += count * template.getWeight();
			if (!template.isStackable())
				slots += count;
			else if (player.getInventory().getItemByItemId(seedId) == null)
				slots++;
		}

		if (totalPrice >= Integer.MAX_VALUE)
		{
			requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		else if (!player.getInventory().validateWeight(totalWeight))
		{
			requestFailed(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		else if (!player.getInventory().validateCapacity(slots))
		{
			requestFailed(SystemMessageId.SLOTS_FULL);
			return;
		}

		// Charge buyer
		if ((totalPrice < 0) || !player.reduceAdena("Buy", (int) totalPrice, target, false))
		{
			requestFailed(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		// Adding to treasury for Manor Castle
		castle.addToTreasuryNoTax((int) totalPrice);

		// Proceed the purchase
		InventoryUpdate playerIU = new InventoryUpdate();
		for (int i = 0; i < _count; i++)
		{
			int seedId = _items[(i * 2)];
			int count = _items[i * 2 + 1];
			if (count < 0)
				count = 0;

			// Update Castle Seeds Amount
			SeedProduction seed = castle.getSeed(seedId,
					CastleManorManager.PERIOD_CURRENT);
			seed.setCanProduce(seed.getCanProduce() - count);
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				CastleManager.getInstance().getCastleById(_manorId).updateSeed(
						seed.getId(), seed.getCanProduce(),
						CastleManorManager.PERIOD_CURRENT);

			// Add item to Inventory and adjust update packet
			L2ItemInstance item = player.getInventory().addItem("Buy", seedId,
					count, player, target);

			if (item.getCount() > count)
				playerIU.addModifiedItem(item);
			else
				playerIU.addNewItem(item);

			// Send Char Buy Messages
			SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(item);
			sm.addNumber(count);
			sendPacket(sm);
			sm = null;
		}
		// Send update packets
		sendPacket(playerIU);

		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		sendPacket(su);
		su = null;

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__C4_REQUESTBUYSEED;
	}
}
