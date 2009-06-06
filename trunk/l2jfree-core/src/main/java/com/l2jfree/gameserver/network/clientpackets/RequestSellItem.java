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
import com.l2jfree.gameserver.cache.HtmCache;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jfree.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetManagerInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ItemList;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.util.Util;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestSellItem extends L2GameClientPacket
{
	private static final String _C__1E_REQUESTSELLITEM = "[C] 1E RequestSellItem";
	//private final static Log _log = LogFactory.getLog(RequestSellItem.class.getName());

	private int _listId;
	private int _count;
	private int[] _items; // count*3
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
        _count = readD();
        if (_count <= 0  || _count * 12 > getByteBuffer().remaining() || _count > Config.MAX_ITEM_IN_PACKET)
        {
            _count = 0; _items = null;
            return;
        }
        _items = new int[_count * 3];
        for (int i = 0; i < _count; i++)
        {
            int objectId = readD(); _items[(i * 3)] = objectId;
            int itemId   = readD(); _items[i * 3 + 1] = itemId;
            long cnt      = 0;
            cnt = readCompQ();
            if (cnt >= Integer.MAX_VALUE || cnt <= 0)
            {
                _count = 0; _items = null;
                return;
            }
            _items[i * 3 + 2] = (int)cnt;
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
		if (player == null)
			return;

		if (Shutdown.isActionDisabled(DisableType.TRANSACTION))
		{
			player.sendMessage("Transactions are not allowed during restart/shutdown.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.cancelActiveTrade();
			return;
		}

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
			return;

        L2Object target = player.getTarget();
        if (!player.isGM() && (target == null								// No target (ie GM Shop)
        		|| !(target instanceof L2MerchantInstance)	// Target not a merchant and not mercmanager
			    || !player.isInsideRadius(target, L2Npc.INTERACTION_DISTANCE, false, false) 	// Distance is too far
			        )) return;

        boolean ok = true;
        String htmlFolder = "";
 
        if (target != null)
        {
        	if (target instanceof L2MerchantInstance)
        		htmlFolder = "merchant";
        	else if (target instanceof L2FishermanInstance)
        		htmlFolder = "fisherman";
        	else if (target instanceof L2PetManagerInstance)
        		htmlFolder = "petmanager";
        	else
        		ok = false;
        }
        else
        	ok = false;

        L2Npc merchant = null;

        if (ok)
        	merchant = (L2Npc)target;

		if (merchant != null && _listId > 1000000) // lease
		{
			if (merchant.getTemplate().getNpcId() != _listId-1000000)
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		long totalPrice = 0;
		// Proceed the sell
		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[(i * 3)];
			@SuppressWarnings("unused")
			int itemId   = _items[i * 3 + 1];
			int count   = _items[i * 3 + 2];

			if (count < 0)
			{
				Util.handleIllegalPlayerAction(player,"Warning!! Character "+player.getName()+" of account "+player.getAccountName()+" tried to purchase over "+Integer.MAX_VALUE+" items at the same time.",  Config.DEFAULT_PUNISH);
				SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				sm = null;
				return;
			}

			L2ItemInstance item = player.checkItemManipulation(objectId, count, "sell");
			if (item == null || !item.isSellable())
				continue;

			totalPrice += item.getReferencePrice() * count /2;
			if (totalPrice >= Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player,"Warning!! Character "+player.getName()+" of account "+player.getAccountName()+" tried to purchase over "+Integer.MAX_VALUE+" adena worth of goods.",  Config.DEFAULT_PUNISH);
				return;
			}

			item = player.getInventory().destroyItem("Sell", objectId, count, player, null);
		}
		player.addAdena("Sell", (int)totalPrice, merchant, false);

		if (merchant != null) {
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
		player.sendPacket(su);
		player.sendPacket(new ItemList(player, true));
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__1E_REQUESTSELLITEM;
	}
}
