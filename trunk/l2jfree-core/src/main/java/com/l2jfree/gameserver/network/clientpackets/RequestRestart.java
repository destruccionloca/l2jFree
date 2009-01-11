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

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.L2GameClient.GameClientState;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.CharSelectionInfo;
import com.l2jfree.gameserver.network.serverpackets.RestartResponse;

public final class RequestRestart extends L2GameClientPacket
{
	private static final String _C__46_REQUESTRESTART = "[C] 46 RequestRestart";
	
	/**
	 * packet type id 0x46 format: c
	 */
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2GameClient client = getClient();
		final L2PcInstance activeChar = client.getActiveChar();
		
		if (activeChar == null)
			return;
		
		if (!activeChar.canLogout())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// detach the client from the char so the connection won't be closed in deleteMe()
		activeChar.setClient(null);
		// prevent deleteMe() from being called a second time onDisconnection()
		client.setActiveChar(null);
		
		L2GameClient.saveCharToDisk(activeChar, true);
		activeChar.deleteMe();
		
		// return the client to the authed status
		client.setState(GameClientState.AUTHED);
		
		sendPacket(new RestartResponse());
		
		// send char list
		CharSelectionInfo cl =
			new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
	
	@Override
	public String getType()
	{
		return _C__46_REQUESTRESTART;
	}
}
