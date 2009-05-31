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
import com.l2jfree.gameserver.model.TradeList.TradeItem;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPrivateStoreBuy extends L2GameClientPacket
{
    private static final String _C__79_REQUESTPRIVATESTOREBUY = "[C] 79 RequestPrivateStoreBuy";

    private int _storePlayerId;
    private int _count;
    private ItemRequest[] _items;

    @Override
    protected void readImpl()
    {
        _storePlayerId = readD();
        _count = readD();
        if (_count < 0  || _count * 12 > getByteBuffer().remaining() || _count > Config.MAX_ITEM_IN_PACKET)
            _count = 0;
        _items = new ItemRequest[_count];

        for (int i = 0; i < _count ; i++)
        {
            int objectId = readD();
            long count   = 0;
            if(Config.PACKET_FINAL)
            	count = toInt(readQ());
            else
            	count = readD();
            if (count >= Integer.MAX_VALUE) count = Integer.MAX_VALUE;
            int price =0;
            if(Config.PACKET_FINAL)
            	price    = toInt(readQ());
            else
            	price    = readD();
            
            _items[i] = new ItemRequest(objectId, (int)count, price);
        }
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

        if (!(storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL || storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL))
            return;

        TradeList storeList = storePlayer.getSellList();
        if (storeList == null) return;
        
        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
            player.sendMessage("Unsufficient privileges.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        
        long priceTotal = 0;
        for (ItemRequest ir : _items)
        {
			if (ir.getCount() < 0)
			{
			    return;
			}
			TradeItem sellersItem = storeList.getItem(ir.getObjectId());
			if(sellersItem == null)
			{
			    return;
			}
			if(ir.getPrice() != sellersItem.getPrice())
			{
			    return;
			}
			priceTotal += ir.getPrice() * ir.getCount();
        }
        
        if(priceTotal < 0 || priceTotal >= Integer.MAX_VALUE)
        {
            return;
        }
        
        if (player.getAdena() < priceTotal)
        {
            sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
        {
           if (storeList.getItemCount() > _count)
           {
               return;
           }
        }

        if (!storeList.privateStoreBuy(player, _items, (int) priceTotal))
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
