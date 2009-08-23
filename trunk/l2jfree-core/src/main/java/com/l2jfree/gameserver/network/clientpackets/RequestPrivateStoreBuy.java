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
import com.l2jfree.gameserver.model.ItemRequest;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.TradeList;
import com.l2jfree.gameserver.model.TradeList.TradeItem;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.util.Util;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private static final String _C__79_REQUESTPRIVATESTOREBUY = "[C] 79 RequestPrivateStoreBuy";

	private static final int BATCH_LENGTH = 12; // length of the one item
	private static final int BATCH_LENGTH_FINAL = 20;

	private int _storePlayerId;
	private ItemRequest[] _items = null;

	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		int count = readD();
		if (count <= 0
				|| count > Config.MAX_ITEM_IN_PACKET
				|| count * (Config.PACKET_FINAL ? BATCH_LENGTH_FINAL : BATCH_LENGTH) != getByteBuffer().remaining())
		{
			return;
		}
		_items = new ItemRequest[count];

		for (int i = 0; i < count ; i++)
		{
			int objectId = readD();
			long cnt = readCompQ();
			long price = readCompQ();

			if (objectId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new ItemRequest(objectId, cnt, price);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null || player.isCursedWeaponEquipped())
			return;

		if (_items == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
		{
			player.sendMessage("Transactions are not allowed during restart/shutdown.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object object = null;

		// Get object from target
		if (player.getTargetId() == _storePlayerId)
			object = player.getTarget();

		// Get object from world
		if (object == null)
		{
			object = L2World.getInstance().getPlayer(_storePlayerId);
			// _log.warn("Player "+player.getName()+" requested private shop from outside of his knownlist.");
		}

		if (!(object instanceof L2PcInstance))
			return;

		L2PcInstance storePlayer = (L2PcInstance) object;

		if (!player.isInsideRadius(storePlayer, INTERACTION_DISTANCE, true, false))
			return;

		if (!(storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL || storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL))
			return;

		TradeList storeList = storePlayer.getSellList();
		if (storeList == null)
			return;

		if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		long priceTotal = 0;
		for (ItemRequest i : _items)
		{
			TradeItem sellersItem = storeList.getItem(i.getObjectId());
			if (sellersItem == null)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy an item not sold in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			if ((MAX_ADENA / i.getCount()) < i.getPrice())
			{
				requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED); 
				return;
			}
			if (i.getPrice() != sellersItem.getPrice())
			{
				String msgErr = "[RequestPrivateStoreBuy] player "+getClient().getActiveChar().getName()+" tried to change the seller's price in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(),msgErr,Config.DEFAULT_PUNISH);
				return;
			}
			priceTotal += i.getPrice() * i.getCount();
		}

		// FIXME: this check should be (and most probably is) done in the TradeList mechanics
		if (priceTotal < 0 || priceTotal > MAX_ADENA)
		{
			requestFailed(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED); 
			return;
		}

		if (player.getAdena() < priceTotal)
		{
			sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
		{
			if (storeList.getItemCount() > _items.length)
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy less items then sold by package-sell, ban this player for bot-usage!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
		}

		if (!storeList.privateStoreBuy(player, _items, priceTotal))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
    }

    @Override
    public String getType()
    {
        return _C__79_REQUESTPRIVATESTOREBUY;
    }
}
