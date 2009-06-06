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
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.TradeList;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.TradeOtherAdd;
import com.l2jfree.gameserver.network.serverpackets.TradeOwnAdd;

/**
 * This class represents a packet that is sent by the client when you are adding an item
 * to the trade window.
 * 
 * @version $Revision: 1.5.2.2.2.5 $ $Date: 2005/03/27 15:29:29 $
 */
public class AddTradeItem extends L2GameClientPacket
{
    private static final String _C__16_ADDTRADEITEM = "[C] 16 AddTradeItem";

    private int _tradeId;
    private int _objectId;
    private long _count;

    @Override
    protected void readImpl()
    {
        _tradeId = readD();
        _objectId = readD();
        _count = readCompQ();
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;

        if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
        {
        	requestFailed(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
        	player.cancelActiveTrade();
            return;
        }

        TradeList trade = player.getActiveTradeList();
        if (trade == null)
        {
            _log.warn("Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
            requestFailed(SystemMessageId.TRADE_ATTEMPT_FAILED);
            return;
        }

        if (trade.getPartner() == null || L2World.getInstance().getPlayer(trade.getPartner().getObjectId()) == null)
        {
            // Trade partner not found, cancel trade
            if (trade.getPartner() != null)
                _log.warn("Character:" + player.getName() + " requested invalid trade object: " + _objectId);
            requestFailed(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
        	player.cancelActiveTrade();
            return;
        }

        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN
            && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
        	trade.getPartner().sendPacket(SystemMessageId.CANT_TRADE_WITH_TARGET);
        	requestFailed(SystemMessageId.ACCOUNT_CANT_TRADE_ITEMS);
        	player.cancelActiveTrade();
            return;
        }

        if (!player.validateItemManipulation(_objectId, "trade") && !player.isGM())
        {
        	requestFailed(SystemMessageId.TRADE_ATTEMPT_FAILED);
            return;
        }

        TradeList.TradeItem item = trade.addItem(_objectId, _count);
        if (item != null)
        {
            sendPacket(new TradeOwnAdd(item));
            trade.getPartner().sendPacket(new TradeOtherAdd(item));
        }

        sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
    public String getType()
    {
        return _C__16_ADDTRADEITEM;
    }
}
