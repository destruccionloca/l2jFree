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
package net.sf.l2j.gameserver.serverpackets;

import java.util.Map;

import net.sf.l2j.gameserver.model.L2FriendList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * Support for "Chat with Friends" dialog. 
 * 
 * Format: ch (hdSdh)
 * h: Total Friend Count
 * 
 * h: Unknown
 * d: Player Object ID
 * S: Friend Name
 * d: Online/Offline
 * h: Unknown
 * 
 * @author Tempy
 *
 */
public class FriendList extends ServerBasePacket
{
	private static final String _S__FA_FRIENDLIST = "[S] FA FriendList";
	
    private L2PcInstance _cha;
    
    public FriendList(L2PcInstance cha)
    {
        _cha = cha;
    }
	
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		if (_cha == null)  
			return;  
		
		if (L2FriendList.getFriendList(_cha).size() > 0)
		{
			writeC(0xfa);
			writeH(L2FriendList.getFriendListNames(_cha).length);
            
			for(Map.Entry<Integer, String> _friend : L2FriendList.getFriendList(_cha).entrySet())
	        {

				L2PcInstance friend = L2World.getInstance().getPlayer(_friend.getValue());
	        	writeH(0); // ??
				writeD(_friend.getKey());
				writeS(_friend.getValue());
				if (friend == null)
					writeD(0); // offline
				else
					writeD(1); // online
				writeH(0); // ??	
	        }
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__FA_FRIENDLIST;
	}
}
