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

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPrivateStoreSell extends L2GameClientPacket
{
    private static final String _C__96_REQUESTPRIVATESTORESELL = "[C] 96 RequestPrivateStoreSell";
    private final static Log _log = LogFactory.getLog(RequestPrivateStoreSell.class.getName());
    
    private int _storePlayerId;
    private int _count;
    private int _price;
    private ItemRequest[] _items;
    
    @Override
    protected void readImpl()
    {
        _storePlayerId = readD();
        _count = readD();
        if (_count < 0  || _count * 20 > getByteBuffer().remaining() || _count > Config.MAX_ITEM_IN_PACKET)
            _count = 0;
        _items = new ItemRequest[_count];

        long priceTotal = 0;
        for (int i = 0; i < _count; i++)
        {
            int objectId = readD();
            int itemId = readD();
            readH(); //TODO: analyse this
            readH(); //TODO: analyse this
            long count   = readD();
            int price    = readD();
            
            if (count >= Integer.MAX_VALUE || count < 0)
            {
                String msgErr = "[RequestPrivateStoreSell] player "+getClient().getActiveChar().getName()+" tried an overflow exploit, ban this player!";
                Util.handleIllegalPlayerAction(getClient().getActiveChar(),msgErr,Config.DEFAULT_PUNISH);
                _count = 0;
                _items = null;
                return;
            }
            _items[i] = new ItemRequest(objectId, itemId, (int)count, price);
            priceTotal += price * count;
        }
        
        if(priceTotal < 0 || priceTotal >= Integer.MAX_VALUE)
        {
            String msgErr = "[RequestPrivateStoreSell] player "+getClient().getActiveChar().getName()+" tried an overflow exploit, ban this player!";
            Util.handleIllegalPlayerAction(getClient().getActiveChar(),msgErr,Config.DEFAULT_PUNISH);
            _count = 0;
            _items = null;
            return;
        }

        _price = (int)priceTotal;
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null || player.isCursedWeaponEquipped())
            return;

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

        if (storePlayer.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_BUY)
            return;

        TradeList storeList = storePlayer.getBuyList();
        if (storeList == null) return;
        
        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
            player.sendMessage("Unsufficient privileges.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        
        if (storePlayer.getAdena() < _price)
        {
			// [L2J_JP EDIT]
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
			return;
        }
        
        if (!storeList.privateStoreSell(player, _items, _price))
        {
            player.sendPacket(ActionFailed.STATIC_PACKET);
            _log.warn("PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
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
        return _C__96_REQUESTPRIVATESTORESELL;
    }
}
