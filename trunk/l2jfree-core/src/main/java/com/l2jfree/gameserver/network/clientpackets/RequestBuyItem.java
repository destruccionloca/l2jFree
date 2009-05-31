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

import javolution.util.FastList;

import com.l2jfree.Config;
import com.l2jfree.gameserver.cache.HtmCache;
import com.l2jfree.gameserver.datatables.ItemTable;
import com.l2jfree.gameserver.datatables.TradeListTable;
import com.l2jfree.gameserver.instancemanager.MercTicketManager;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2TradeList;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2CastleChamberlainInstance;
import com.l2jfree.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import com.l2jfree.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jfree.gameserver.model.actor.instance.L2MercManagerInstance;
import com.l2jfree.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetManagerInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ItemList;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.templates.item.L2Item;
import com.l2jfree.gameserver.util.Util;

/**
 * This class represents a packet sent by the client when the player confirms his item
 * selection in a general shop (not exchange shop)
 * 
 * @version $Revision: 1.12.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestBuyItem extends L2GameClientPacket
{
	private static final String	_C__1F_REQUESTBUYITEM	= "[C] 1F RequestBuyItem";

	private int					_listId;
	private int					_count;
	private int[]				_items;																		// count*2

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
		_count = readD();
		if ((_count * 2 < 0) || (_count * 8 > getByteBuffer().remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
			_count = 0;

		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			int itemId = readD();
			_items[i * 2] = itemId;
			long cnt=0;
			if(Config.PACKET_FINAL)
				cnt = toInt(readQ());
			else
				cnt = readD();
			if ((cnt >= Integer.MAX_VALUE) || (cnt < 0))
			{
				_count = 0;
				_items = null;
				return;
			}
			_items[i * 2 + 1] = (int) cnt;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null) return;

		L2Object target = player.getTarget();

		if (!canShop(player, target))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		String htmlFolder = "";
		if (target instanceof L2MerchantInstance)
			htmlFolder = "merchant";
		else if (target instanceof L2FishermanInstance)
			htmlFolder = "fisherman";
		else if (target instanceof L2PetManagerInstance)
			htmlFolder = "petmanager";

		L2Npc merchant = null;
		if (target instanceof L2Npc)
			merchant = (L2Npc) target;

		L2TradeList list = null;

		if (merchant != null && !player.isGM())
		{
			FastList<L2TradeList> lists = TradeListTable.getInstance().getBuyListByNpcId(merchant.getNpcId());

			if (lists == null)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				return;
			}

			for (L2TradeList tradeList : lists)
			{
				if (tradeList.getListId() == _listId)
					list = tradeList;
			}
		}
		else
			list = TradeListTable.getInstance().getBuyList(_listId);

		if (list == null)
		{
			if (!player.isGM())
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				return;
			}
			else
				player.sendMessage("Buylist " + _listId + " empty or not exists.");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (list.isGm() && !player.isGM())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
					+ " sent a modified packet to buy from gmshop.", Config.DEFAULT_PUNISH);
			return;
		}

		_listId = list.getListId();

		if (_listId > 1000000) // lease
		{
			if (merchant != null && merchant.getTemplate().getNpcId() != _listId / 100)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		if (_count < 1)
		{
			requestFailed(SystemMessageId.ITEM_OUT_OF_STOCK);
			return;
		}

		double taxRate = 1.0;
		if (merchant != null && merchant.getIsInTown())
			taxRate = merchant.getCastle().getTaxRate();

		long taxedPriceTotal = 0;
		long taxTotal = 0;

		// Check for buylist validity and calculates summary values
		long slots = 0;
		long weight = 0;
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i * 2];
			int count = _items[i * 2 + 1];
			int price = -1;

			if (!list.containsItemId(itemId))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(itemId);
			if (template == null)
				continue;

			if (count > Integer.MAX_VALUE || (!template.isStackable() && (count > 1)))
			{
				//Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
				//		+ " tried to purchase invalid quantity of items at the same time.", Config.DEFAULT_PUNISH);
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}

			price = list.getPriceForItemId(itemId);

			for (int item : MercTicketManager.getInstance().getItemIds())
			{
				if (itemId == item)
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

			if ((price == 0) && !player.isGM() && Config.ONLY_GM_ITEMS_FREE)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " tried buy item for 0 adena.", Config.DEFAULT_PUNISH);
				return;
			}

			long stackPrice = (long)price * count;
			long taxedPrice = (long) (stackPrice * taxRate);
			long tax = taxedPrice - stackPrice;
			if (taxedPrice >= Integer.MAX_VALUE)
			{
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			taxedPriceTotal += taxedPrice;
			taxTotal += tax;

			weight += (long) count * template.getWeight();
			if (!template.isStackable())
				slots += count;
			else if (player.getInventory().getItemByItemId(itemId) == null)
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
			if ((taxedPriceTotal < 0) || (taxedPriceTotal >= Integer.MAX_VALUE) || !player.reduceAdena("Buy", (int) taxedPriceTotal, merchant, false))
			{
				requestFailed(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				return;
			}
		}

		if (!player.isGM())
		{
			//  Charge buyer and add tax to castle treasury if not owned by npc clan
			if (merchant != null && merchant.getIsInTown() && merchant.getCastle().getOwnerId() > 0)
				merchant.getCastle().addToTreasury((int) taxTotal);
		}
		//  Check if player is GM and buying from GM shop or have proper access level
		else if (list.isGm() && (player.getAccessLevel() < Config.GM_CREATE_ITEM))
		{
			requestFailed(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		// Proceed the purchase
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[(i * 2)];
			int count = _items[i * 2 + 1];
			if (count < 0)
				count = 0;

			if (!list.containsItemId(itemId))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName()
						+ " sent a false BuyList list_id.", Config.DEFAULT_PUNISH);
				return;
			}

			if (list.countDecrease(itemId))
			{
				if (!list.decreaseCount(itemId, count))
				{
					requestFailed(SystemMessageId.ITEM_OUT_OF_STOCK);
					return;
				}
			}

			// Add item to Inventory and adjust update packet
			player.getInventory().addItem(list.isGm() ? "GMShop" : "Buy", itemId, count, player, merchant);
		}

		if (merchant != null)
		{
			String html = HtmCache.getInstance().getHtm("data/html/" + htmlFolder + "/" + merchant.getNpcId() + "-bought.htm");

			if (html != null)
			{
				NpcHtmlMessage boughtMsg = new NpcHtmlMessage(merchant.getObjectId());
				boughtMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(merchant.getObjectId())));
				sendPacket(boughtMsg);
				boughtMsg = null;
			}
		}

		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		sendPacket(su); su = null;
		sendPacket(new ItemList(player, true));
	}

	private final boolean canShop(L2PcInstance player, L2Object target)
	{
		if (player.isGM())
			return true;

		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
			return false;
		else if (!(target instanceof L2MerchantInstance ||
				target instanceof L2FishermanInstance ||
				target instanceof L2MercManagerInstance ||
				target instanceof L2ClanHallManagerInstance ||
				target instanceof L2CastleChamberlainInstance))
			return false;
		else if (!player.isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false))
		{
			sendPacket(SystemMessageId.TOO_FAR_FROM_NPC);
			return false;
		}

		return true;
	}

	@Override
	public String getType()
	{
		return _C__1F_REQUESTBUYITEM;
	}
}