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
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.Shutdown.DisableType;
import com.l2jfree.gameserver.model.ItemRequest;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.TradeList;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.util.Util;

import static com.l2jfree.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;
import static com.l2jfree.gameserver.model.itemcontainer.PcInventory.MAX_ADENA;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPrivateStoreSell extends L2GameClientPacket
{
	private static final String	_C__96_REQUESTPRIVATESTORESELL	= "[C] 96 RequestPrivateStoreSell";

	private static final int BATCH_LENGTH = 20; // length of the one item
	private static final int BATCH_LENGTH_FINAL = 28;

	private int					_storePlayerId;
	private ItemRequest[]		_items = null;

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

		for (int i = 0; i < count; i++)
		{
			int objectId = readD();
			int itemId = readD();
			readH(); //TODO: analyse this
			readH(); //TODO: analyse this
			long cnt = readCompQ();
			long price = readCompQ();

			if (objectId < 1 || itemId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new ItemRequest(objectId, itemId, cnt, price);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
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
			//_log.warn("Player "+player.getName()+" requested private shop from outside of his knownlist.");
		}

		if (!(object instanceof L2PcInstance))
			return;

		L2PcInstance storePlayer = (L2PcInstance) object;

		if (!player.isInsideRadius(storePlayer, INTERACTION_DISTANCE, true, false))
			return;

		if (storePlayer.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_BUY)
			return;

		TradeList storeList = storePlayer.getBuyList();
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
			if ((MAX_ADENA / i.getCount()) < i.getPrice())
			{
				String msgErr = "[RequestPrivateStoreSell] player "+getClient().getActiveChar().getName()+" tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(),msgErr,Config.DEFAULT_PUNISH);
				return;
			}
			priceTotal += i.getCount() * i.getPrice();
			if (MAX_ADENA < priceTotal || priceTotal < 0)
			{
				String msgErr = "[RequestPrivateStoreSell] player "+getClient().getActiveChar().getName()+" tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(),msgErr,Config.DEFAULT_PUNISH);
				return;
			}
		}
		if (storePlayer.getAdena() < priceTotal)
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
			return;
		}

		if (!storeList.privateStoreSell(player, _items))
			return;

		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
	}

	@Override
	public String getType()
	{
		return _C__96_REQUESTPRIVATESTORESELL;
	}
}
