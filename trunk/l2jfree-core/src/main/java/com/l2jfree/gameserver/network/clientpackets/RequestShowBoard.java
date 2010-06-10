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
import com.l2jfree.gameserver.communitybbs.CommunityBoard;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class RequestShowBoard extends L2GameClientPacket
{
	private static final String	_C__57_REQUESTSHOWBOARD	= "[C] 57 RequestShowBoard";
	
	// private int _unknown;
	
	/**
	 * packet type id 0x57
	 * sample
	 * 57
	 * 01 00 00 00 // unknown (always 1?)
	 * format: cd
	 */
	@Override
	protected void readImpl()
	{
		/* _unknown = */readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.ENABLE_COMMUNITY_BOARD)
		{
			L2PcInstance activeChar = getClient().getActiveChar();
			
			if (activeChar == null)
				return;
			
			if (CommunityServerThread.getInstance().isAuthed())
				CommunityServerThread.getInstance().sendPacket(new RequestShowCommunityBoard(activeChar.getObjectId(), "_bbshome"), true);
			else
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CB_OFFLINE));
		}
		else
			CommunityBoard.handleCommands(getClient(), Config.BBS_DEFAULT);
	}
	
	@Override
	public String getType()
	{
		return _C__57_REQUESTSHOWBOARD;
	}
}
