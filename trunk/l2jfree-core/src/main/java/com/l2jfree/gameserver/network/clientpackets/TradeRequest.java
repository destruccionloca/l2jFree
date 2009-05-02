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
import com.l2jfree.gameserver.model.BlockList;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SendTradeRequest;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 *
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class TradeRequest extends L2GameClientPacket
{
	private static final String TRADEREQUEST__C__15 = "[C] 15 TradeRequest";

	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
		{
			player.sendMessage("Transactions are not allowed during restart/shutdown.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object obj = null;

		// Get object from target
		if (player.getTargetId() == _objectId)
			obj = player.getTarget();

		// Get object from world
		if (obj == null)
		{
			obj = L2World.getInstance().getPlayer(_objectId);
			//_log.warn("Player "+player.getName()+" requested trade from player from outside of his knownlist.");
		}

		if (!(obj instanceof L2PcInstance) || obj.getObjectId() == player.getObjectId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}

		L2PcInstance partner = (L2PcInstance)obj;

		if (partner.isInOlympiadMode() || player.isInOlympiadMode())
		{
			player.sendMessage("You or your target can't request trade in Olympiad mode");
			return;
		}

		if (BlockList.isBlocked(partner, player))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addCharName(partner);
			player.sendPacket(sm);
			return;
		}

		if (player.getDistanceSq(partner) > 22500) // 150
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.TARGET_TOO_FAR);
			player.sendPacket(sm);
			return;
		}

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE && (player.getKarma() > 0 || partner.getKarma() > 0))
		{
			player.sendMessage("Chaotic players can't use Trade.");
			return;
		}

		if (player.getPrivateStoreType() != 0 || partner.getPrivateStoreType() != 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}

		if (player.isProcessingTransaction())
		{
			if (_log.isDebugEnabled()) _log.debug("already trading with someone");
			player.sendPacket(new SystemMessage(SystemMessageId.ALREADY_TRADING));
			return;
		}

		if (partner.isProcessingRequest() || partner.isProcessingTransaction())
		{
			if (_log.isDebugEnabled()) _log.debug("transaction already in progress.");
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(partner.getName());
			player.sendPacket(sm);
			return;
		}

		if (partner.getTradeRefusal())
		{
			player.sendMessage("Target is in trade refusal mode.");
			return;
		}

		player.onTransactionRequest(partner);
		partner.sendPacket(new SendTradeRequest(player.getObjectId()));
		SystemMessage sm = new SystemMessage(SystemMessageId.REQUEST_C1_FOR_TRADE);
		sm.addString(partner.getName());
		player.sendPacket(sm);
	}

	@Override
	public String getType()
	{
		return TRADEREQUEST__C__15;
	}
}
