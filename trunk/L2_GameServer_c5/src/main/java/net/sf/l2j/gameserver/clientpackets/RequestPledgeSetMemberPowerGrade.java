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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPledgeSetMemberPowerGrade extends ClientBasePacket
{
	private static final String _C__24_REQUESTJOINPLEDGE = "[C] 24 RequestJoinPledge";
	static Log _log = LogFactory.getLog(RequestJoinPledge.class.getName());

	private final int _rank;
    private final String _name;
	
	public RequestPledgeSetMemberPowerGrade(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_name  = readS();
        _rank = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
		
		//is the guy leader of the clan ?
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_MANAGE_RANKS) == L2Clan.CP_CL_MANAGE_RANKS)  
		{
			L2ClanMember member = activeChar.getClan().getClanMember(_name);
            if (member != null && member.isOnline())
            {
                member.getPlayerInstance().setRank(_rank);
                member.getPlayerInstance().sendPacket(new UserInfo(member.getPlayerInstance()));
                if (activeChar.getClan().getRankPrivs(_rank) == 0)
                {
                    activeChar.getClan().setRankPrivs(_rank, 0);
                }
            }
            else if(member != null)
            {
                member.setRank(_rank);
            }
            else
                activeChar.sendMessage("the target doesn't belong to your clan");
		}
        else
        {
            activeChar.sendMessage("You don't have the authority to change this member's rank");
        }
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__24_REQUESTJOINPLEDGE;
	}
}
