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

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.CharDeleteFail;
import com.l2jfree.gameserver.network.serverpackets.CharDeleteSuccess;
import com.l2jfree.gameserver.network.serverpackets.CharSelectionInfo;

/**
 * This class ...
 * 
 * @version $Revision: 1.8.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterDelete extends L2GameClientPacket
{
	private static final String _C__0C_CHARACTERDELETE = "[C] 0C CharacterDelete";
	private final static Log _log = LogFactory.getLog(CharacterDelete.class.getName());

	// cd
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		if (_log.isDebugEnabled()) _log.debug("deleting slot:" + _charSlot);

		L2PcInstance character = null;
		try
		{
			if (Config.DELETE_DAYS == 0)
				character = getClient().deleteChar(_charSlot);
			else
				character = getClient().markToDeleteChar(_charSlot);
		}
		catch (Exception e)
		{
			_log.fatal( "Error:", e);
		}
		if (character == null)
		{
			sendPacket(new CharDeleteSuccess());
		}
		else
		{
			if (character.isClanLeader())
			{
				sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
			}
			else
			{
				sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
			}
			character.deleteMe();
		}

		CharSelectionInfo cl = new CharSelectionInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0C_CHARACTERDELETE;
	}
}
