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

import static com.l2jfree.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.ItemTable;
import com.l2jfree.gameserver.instancemanager.MercTicketManager;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2TradeList;
import com.l2jfree.gameserver.model.actor.L2Merchant;
import com.l2jfree.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ExBuySellListPacket;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.templates.item.L2Item;
import com.l2jfree.gameserver.util.Util;

/**
 * This class represents a packet sent by the client when the player confirms his item
 * selection in a general shop (not exchange shop)
 */
public class RequestBuyItem extends L2GameClientPacket
{
	private static final String _C__1F_REQUESTBUYITEM = "[C] 1F RequestBuyItem";
	
	private static final int BATCH_LENGTH = 12; // length of the one item
	
	private int _listId;
	private Item[] _items = null;
	
	/**
	 * packet type id 0x1f<br>
	 * <br>
	 * sample<br>
	 * <br>
	 * 1f<br>
	 * 44 22 02 01 // list id<br>
	 * 02 00 00 00 // items to buy<br>
	 * <br>
	 * 27 07 00 00 // item id<br>
	 * 06 00 00 00 // count<br>
	 * <br>
	 * 83 06 00 00<br>
	 * 01 00 00 00<br>
	 * <br>
	 * format: cdd (dd)
	 */
	@Override
	protected void readImpl()
	{
		_listId = readD();
		
		int count = readD();
		if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != getByteBuffer().remaining())
		{
			return;
		}
		
		_items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readQ();
			
			if (itemId < 1 || cnt < 1)
			{
				_items = null;
				return;
			}
			_items[i] = new Item(itemId, cnt);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (_items == null)
		{
			sendAF();
			return;
		}
		
		final L2Object merchant = (L2Object)player.getTarget(L2Merchant.class);
		final L2TradeList list = L2MerchantInstance.getTradeList(player, merchant, _listId);
		
		if (list == null)
		{
			sendAF();
			return;
		}
		
		int npcId = -1;
		
		// FIXME 1.4.0 -> fix taxing, because it's a mess
		double castleTaxRate = 0;
		double baseTaxRate = 0;
		
		if (merchant != null)
		{
			npcId = merchant.getActingMerchant().getTemplate().getNpcId();
			
			// FIXME 1.4.0 -> fix taxing, because it's a mess
			if (merchant instanceof L2MerchantInstance)
			{
				castleTaxRate = ((L2MerchantInstance)merchant).getMpc().getCastleTaxRate();
				baseTaxRate = ((L2MerchantInstance)merchant).getMpc().getBaseTaxRate();
			}
			else
			{
				baseTaxRate = 50;
			}
		}
		
		_listId = list.getListId();
		
		if (_listId > 1000000) // lease
		{
			if (npcId != -1 && npcId != _listId / 100)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		double taxRate = 1.0;
		if (merchant instanceof L2MerchantInstance && ((L2MerchantInstance) merchant).getIsInTown())
			taxRate = ((L2MerchantInstance) merchant).getCastle().getTaxRate();
		
		long taxedPriceTotal = 0;
		long taxTotal = 0;
		
		// Check for buylist validity and calculates summary values
		long slots = 0;
		long weight = 0;
		for (Item i : _items)
		{
			long price = -1;
			
			if (!list.containsItemId(i.getItemId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				return;
			}
			
			L2Item template = ItemTable.getInstance().getTemplate(i.getItemId());
			if (template == null)
				continue;
			
			if (!template.isStackable() && i.getCount() > 1)
			{
				// Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
				// + " tried to purchase invalid quantity of items at the same time.", Config.DEFAULT_PUNISH);
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			
			price = list.getPriceForItemId(i.getItemId());
			
			for (int item : MercTicketManager.getInstance().getItemIds())
			{
				if (i.getItemId() == item)
				{
					price *= Config.RATE_SIEGE_GUARDS_PRICE;
					break;
				}
			}
			
			if (price < 0)
			{
				_log.warn("Error, no price found .. wrong buylist?");
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (price == 0 && !player.isGM() && Config.ONLY_GM_ITEMS_FREE)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " tried buy item for 0 adena.", Config.DEFAULT_PUNISH);
				return;
			}
			if ((MAX_ADENA / i.getCount()) < price)
			{
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			
			long stackPrice = price * i.getCount();
			long taxedPrice = (long) (stackPrice * taxRate);
			long tax = taxedPrice - stackPrice;
			if (taxedPrice > MAX_ADENA)
			{
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			taxedPriceTotal += taxedPrice;
			taxTotal += tax;
			
			weight += i.getCount() * template.getWeight();
			if (!template.isStackable())
				slots += i.getCount();
			else if (player.getInventory().getItemByItemId(i.getItemId()) == null)
				slots++;
		}
		
		if ((weight >= Integer.MAX_VALUE) || (weight < 0) || !player.getInventory().validateWeight((int) weight))
		{
			requestFailed(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}
		
		if ((slots >= Integer.MAX_VALUE) || (slots < 0) || !player.getInventory().validateCapacity((int) slots))
		{
			requestFailed(SystemMessageId.SLOTS_FULL);
			return;
		}
		
		if (!player.isGM() || (player.isGM() && (player.getAccessLevel() < Config.GM_FREE_SHOP)))
		{
			if ((taxedPriceTotal < 0) || !player.reduceAdena("Buy", taxedPriceTotal, merchant, false))
			{
				requestFailed(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				return;
			}
		}
		
		if (!player.isGM())
		{
			// Charge buyer and add tax to castle treasury if not owned by npc clan
			if (merchant instanceof L2MerchantInstance && ((L2MerchantInstance) merchant).getIsInTown()
					&& ((L2MerchantInstance) merchant).getCastle().getOwnerId() > 0)
				((L2MerchantInstance) merchant).getCastle().addToTreasury(taxTotal);
		}
		// Check if player is GM and buying from GM shop or have proper access level
		else if (list.isGm() && (player.getAccessLevel() < Config.GM_CREATE_ITEM))
		{
			requestFailed(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		// Proceed the purchase
		for (Item i : _items)
		{
			if (!list.containsItemId(i.getItemId()))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				return;
			}
			
			if (list.countDecrease(i.getItemId()))
			{
				if (!list.decreaseCount(i.getItemId(), i.getCount()))
				{
					requestFailed(SystemMessageId.ITEM_OUT_OF_STOCK);
					return;
				}
			}
			
			// Add item to Inventory and adjust update packet
			player.getInventory().addItem(list.isGm() ? "GMShop" : "Buy", i.getItemId(), i.getCount(), player, merchant);
		}
		
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		sendPacket(su);
		sendPacket(new ExBuySellListPacket(player, list, castleTaxRate + baseTaxRate, true));
	}
	
	private static class Item
	{
		private final int	_itemId;
		private final long	_count;
		
		public Item(int id, long num)
		{
			_itemId = id;
			_count = num;
		}
		
		public int getItemId()
		{
			return _itemId;
		}
		
		public long getCount()
		{
			return _count;
		}
	}
	
	@Override
	public String getType()
	{
		return _C__1F_REQUESTBUYITEM;
	}
}