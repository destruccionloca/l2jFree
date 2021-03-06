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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.Shutdown;
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
	private final static Log _log = LogFactory.getLog(TradeRequest.class.getName());
	
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
        if (player == null) return;
        
		if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null 
        		&& Shutdown.getCounterInstance().getCountdown() <= Config.SAFE_REBOOT_TIME)
        {
			player.sendMessage("Transactions are not allowed during restart/shutdown.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
        }
		
        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
        	player.sendMessage("Transactions are disabled for your Access Level");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        
		L2Object target = L2World.getInstance().findObject(_objectId);
        if (target == null || !player.getKnownList().knowsObject(target) 
        		|| !(target instanceof L2PcInstance) || (target.getObjectId() == player.getObjectId()))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		
        L2PcInstance partner = (L2PcInstance)target;

        if (partner.isInOlympiadMode() || player.isInOlympiadMode())
        {
            player.sendMessage("You or your target can't request trade in Olympiad mode");
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
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
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
		SystemMessage sm = new SystemMessage(SystemMessageId.REQUEST_S1_FOR_TRADE);
		sm.addString(partner.getName());
		player.sendPacket(sm);
	} 
	
	@Override
	public String getType()
	{
		return TRADEREQUEST__C__15;
	}
}
