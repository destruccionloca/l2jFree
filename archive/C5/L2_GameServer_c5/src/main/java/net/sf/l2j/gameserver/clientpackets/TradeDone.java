/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.6.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class TradeDone extends ClientBasePacket
{
	private static final String _C__17_TRADEDONE = "[C] 17 TradeDone";
	private final static Log _log = LogFactory.getLog(TradeDone.class.getName());

	private final int _response;
	
	public TradeDone(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_response = readD();
	}

	void runImpl()
	{
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;
		
        if (Config.SAFE_REBOOT && Config.SAFE_REBOOT_DISABLE_TRANSACTION && Shutdown.getCounterInstance() != null 
        		&& Shutdown.getCounterInstance().getCountdow() <= Config.SAFE_REBOOT_TIME)
        {
			player.sendMessage("Transactions isn't allowed during restart/shutdown!");
			player.cancelActiveTrade();
			sendPacket(new ActionFailed());
			return;
        }
		
        TradeList trade = player.getActiveTradeList();
        if (trade == null)
        	{
            _log.warn("player.getTradeList == null in "+getType()+" for player "+player.getName());
        	return;
        	}
        if (trade.isLocked()) return;

		if (_response == 1)
		{
	        if (trade.getPartner() == null || L2World.getInstance().findObject(trade.getPartner().getObjectId()) == null)
	        {
	            // Trade partner not found, cancel trade
	            player.cancelActiveTrade();
	            SystemMessage msg = new SystemMessage(SystemMessage.PLAYER_NOT_ONLINE);
	            player.sendPacket(msg);
	            msg = null;
	            return;
	        }

	        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN
	            && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
	        {
	            player.sendMessage("Transactions are disable for your Access Level");
	            player.cancelActiveTrade();
	            sendPacket(new ActionFailed());
	            return;
	        }
	        trade.Confirm();
		}
		else player.cancelActiveTrade();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__17_TRADEDONE;
	}
}
