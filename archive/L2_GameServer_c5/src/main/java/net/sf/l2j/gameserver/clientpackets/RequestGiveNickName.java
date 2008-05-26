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
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestGiveNickName extends ClientBasePacket
{
	private static final String _C__55_REQUESTGIVENICKNAME = "[C] 55 RequestGiveNickName";
	static Log _log = LogFactory.getLog(RequestGiveNickName.class.getName());
	
	private String _target;
	private final String _title;
	
	public RequestGiveNickName(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_target = readS();
		_title  = readS();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
		
		//Can the player change/give a titel?
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_GIVE_TITLE) == L2Clan.CP_CL_GIVE_TITLE) 
		{	
			if (activeChar.getClan().getLevel() < 3)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
                activeChar.sendPacket(sm);
                sm = null;
				return;
			}
            
            //if (getClient().getRevision() >= 690)
               // _target = activeChar.getTarget().getName();
            
			L2ClanMember member1 = activeChar.getClan().getClanMember(_target);
            //L2ClanMember member1 = ; // C5
            if (member1 != null)
            {
                L2PcInstance member = member1.getPlayerInstance();
                 //is target from the same clan?
                if (member != null)
                {
                	if (!Config.TITLE_PATTERN.matcher(_title).matches() || 
                			_title.length() < 3 || _title.length() > 16 )
                	{
                        SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                        sm.addString("Incorrect title. Please try again.");
                        activeChar.sendPacket(sm);
                        sm = null;
                	}
                	else
                	{
                		member.setTitle(_title);
        				SystemMessage sm = new SystemMessage(SystemMessage.TITLE_CHANGED);
        				member.sendPacket(sm);
        				sm = null;
    					member.broadcastUserInfo();
    					
    					if (member != activeChar)
    					{
            				sm = new SystemMessage(SystemMessage.CLAN_MEMBER_S1_TITLE_CHANGED_TO_S2);
            				sm.addString(member.getName());
            				sm.addString(member.getTitle());
            				member.sendPacket(sm);
            				sm = null;
    					}
    					
                	}
                }
                else
                {
                    SystemMessage sm = new SystemMessage(SystemMessage.PLAYER_NOT_ONLINE);
                    activeChar.sendPacket(sm);
                    sm = null;
                }
			}
            else
            {
                SystemMessage sm = new SystemMessage(SystemMessage.TARGET_MUST_BE_IN_CLAN);
                activeChar.sendPacket(sm);
                sm = null;
            }
		}
        else if(activeChar.isNoble())
        {
            if(activeChar.getTarget() == activeChar)
            {
            	if (!Config.TITLE_PATTERN.matcher(_title).matches() || 
            			_title.length() < 3 || _title.length() > 16 )
            	{
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                    sm.addString("Incorrect title. Please try again.");
                    activeChar.sendPacket(sm);
                    sm = null;
            	}
            	else
            	{
            		activeChar.setTitle(_title);
                    SystemMessage sm = new SystemMessage(SystemMessage.TITLE_CHANGED);
                    activeChar.sendPacket(sm);
                    activeChar.broadcastUserInfo();
                    sm = null;
            	}
            }
        }        
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__55_REQUESTGIVENICKNAME;
	}
}
