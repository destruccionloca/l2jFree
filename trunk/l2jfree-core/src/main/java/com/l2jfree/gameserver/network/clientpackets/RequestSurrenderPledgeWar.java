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

import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class RequestSurrenderPledgeWar extends L2GameClientPacket
{
    private static final String _C__51_REQUESTSURRENDERPLEDGEWAR = "[C] 51 RequestSurrenderPledgeWar";
    private final static Log _log = LogFactory.getLog(RequestSurrenderPledgeWar.class.getName());

    String _pledgeName;
    L2Clan _clan;
    L2PcInstance _activeChar;
    
    @Override
    protected void readImpl()
    {
        _pledgeName  = readS();
    }

    @Override
    protected void runImpl()
    {
        _activeChar = getClient().getActiveChar();
	    if (_activeChar == null)
	        return;
        _clan = _activeChar.getClan();
	    if (_clan == null)
	        return;
        L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

        if(clan == null)
        {
            _activeChar.sendMessage("No such clan.");
            _activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        _log.info("RequestSurrenderPledgeWar by "+getClient().getActiveChar().getClan().getName()+" with "+_pledgeName);
        
        if(!_clan.isAtWarWith(clan.getClanId()))
        {
            _activeChar.sendMessage("You aren't at war with this clan.");
            _activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        
        
        SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN);
        msg.addString(_pledgeName);
        _activeChar.sendPacket(msg);
        msg = null;
        _activeChar.deathPenalty(false, false, false);
        ClanTable.getInstance().deleteclanswars(_clan.getClanId(), clan.getClanId());
        /*L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());
        if(leader != null && leader.isOnline() == 0)
        {
           _activeChar.sendMessage("Clan leader isn't online.");
            _activeChar.sendPacket(new ActionFailed());
            return;
        }
        
        if (leader.isTransactionInProgress())
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
            sm.addString(leader.getName());
            _activeChar.sendPacket(sm);
            return;
        }
        
        leader.setTransactionRequester(player);
        _activeChar.setTransactionRequester(leader);
        leader.sendPacket(new SurrenderPledgeWar(_clan.getName(),_activeChar.getName()));*/
    }
    
    @Override
    public String getType()
    {
        return _C__51_REQUESTSURRENDERPLEDGEWAR;
    }
}
