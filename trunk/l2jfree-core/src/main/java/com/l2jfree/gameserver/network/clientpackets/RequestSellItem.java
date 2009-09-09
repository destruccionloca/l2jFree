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

import static com.l2jfree.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;
import static com.l2jfree.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

import com.l2jfree.Config;
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.Shutdown.DisableType;
import com.l2jfree.gameserver.cache.HtmCache;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Merchant;
import com.l2jfree.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetManagerInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ItemList;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestSellItem extends L2GameClientPacket
{
	private static final String _C__1E_REQUESTSELLITEM = "[C] 1E RequestSellItem";

	private static final int BATCH_LENGTH = 12; // length of the one item
	private static final int BATCH_LENGTH_FINAL = 16;

	private int _listId;
	private Item[] _items = null;

	/**
	 * packet type id 0x1e
	 * 
	 * sample
	 * 
	 * 1e
	 * 00 00 00 00		// list id
	 * 02 00 00 00		// number of items
	 * 
	 * 71 72 00 10		// object id
	 * ea 05 00 00		// item id
	 * 01 00 00 00		// item count
	 * 
	 * 76 4b 00 10		// object id
	 * 2e 0a 00 00		// item id
	 * 01 00 00 00		// item count
	 * 
	 * format:		cdd (ddd)
	 */

	@Override
	protected void readImpl()
	{
		_listId = readD();
		int count = readD();
		if (count <= 0
				|| count > Config.MAX_ITEM_IN_PACKET
				|| count * (Config.PACKET_FINAL ? BATCH_LENGTH_FINAL : BATCH_LENGTH) != getByteBuffer().remaining())
		{
			return;
		}

		_items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			int objectId = readD();
			int itemId = readD();
			long cnt = readCompQ();
			if (objectId < 1 || itemId < 1 || cnt < 1)
			{
				_items = null;
				return;
			}
			_items[i] = new Item(objectId, itemId, cnt);
		}
	}

	@Override
	protected void runImpl()
	{
		processSell();
	}

	protected void processSell()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null) return;

		if (_items == null)
		{
			sendAF();
			return;
		}

		if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
		{
			player.cancelActiveTrade();
			requestFailed(SystemMessageId.FUNCTION_INACCESSIBLE_NOW);
			return;
		}

		L2Merchant merchant = player.getTarget(L2Merchant.class);
		String htmlFolder;
		if (merchant instanceof L2FishermanInstance)
			htmlFolder = "fisherman";
		else if (merchant instanceof L2PetManagerInstance)
			htmlFolder = "petmanager";
		else
			htmlFolder = "merchant";

		if (!canShop(player, merchant))
		{
			sendAF();
			return;
		}

		if (merchant != null && _listId > 1000000) // lease
		{
			if (merchant.getTemplate().getNpcId() != _listId-1000000)
			{
				sendAF();
				return;
			}
		}

		long totalPrice = 0;
		// Proceed the sell
		for (Item i : _items)
		{
			L2ItemInstance item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "sell");
			if (item == null || !item.isSellable())
				continue;

			long price = item.getReferencePrice() / 2;
			totalPrice += price * i.getCount();
			if ((MAX_ADENA / i.getCount()) < price || totalPrice > MAX_ADENA)
			{
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED); 
				return;
			}

			item = player.getInventory().destroyItem("Sell", i.getObjectId(), i.getCount(), player, null);
		}
		player.addAdena("Sell", totalPrice, (L2Character)merchant, false);

		if (merchant != null)
		{
			String html = HtmCache.getInstance().getHtm("data/html/"+ htmlFolder +"/" + merchant.getNpcId() + "-sold.htm");
	
			if (html != null)
			{
				NpcHtmlMessage soldMsg = new NpcHtmlMessage(merchant.getObjectId());
				soldMsg.setHtml(html.replaceAll("%objectId%", String.valueOf(merchant.getObjectId())));
				player.sendPacket(soldMsg);
			}
		}

		// Update current load as well
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		sendPacket(su);
		sendPacket(new ItemList(player, true));

		sendAF();
	}

	private boolean canShop(L2PcInstance player, L2Merchant target)
	{
		if (player.isGM())
			return true;

		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
			return false;

		if (target == null)
			return false;

		if (!player.isInsideRadius((L2Character) target, INTERACTION_DISTANCE, false, false))
		{
			player.sendPacket(SystemMessageId.TOO_FAR_FROM_NPC);
			return false;
		}

		return true;
	}

	private class Item
	{
		private final int _objectId;
//		private final int _itemId;
		private final long _count;

		public Item(int objId, int id, long num)
		{
			_objectId = objId;
//			_itemId = id;
			_count = num;
		}

		public int getObjectId()
		{
			return _objectId;
		}

//		public int getItemId()
//		{
//			return _itemId;
//		}

		public long getCount()
		{
			return _count;
		}
	}

	@Override
	public String getType()
	{
		return _C__1E_REQUESTSELLITEM;
	}
}
