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

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.ManagePledgePower;

public class RequestPledgePower extends L2GameClientPacket
{
    static Log _log = LogFactory.getLog(ManagePledgePower.class.getName());
    private static final String _C__C0_REQUESTPLEDGEPOWER = "[C] C0 RequestPledgePower";
    private int _rank;
    private int _action;
    private int _privs;
    
    @Override
    protected void readImpl()
    {
        _rank = readD();
        _action = readD();
        if (_action == 2)
        {
            _privs = readD();
        }
        else _privs = 0;
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;
        if(_action == 2)
        {
        	if(player.getClan() != null && player.isClanLeader())
        	{
                    player.getClan().setRankPrivs(_rank, _privs);
        	}
        }
        else
        {
            ManagePledgePower mpp = new ManagePledgePower(getClient().getActiveChar().getClan(), _action, _rank);
            player.sendPacket(mpp);
        }
    }
    
    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__C0_REQUESTPLEDGEPOWER;
    }
}
