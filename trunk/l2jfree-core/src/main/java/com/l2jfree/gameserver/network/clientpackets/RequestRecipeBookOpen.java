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

import com.l2jfree.gameserver.RecipeController;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class RequestRecipeBookOpen extends L2GameClientPacket
{
	private static final String _C__AC_REQUESTRECIPEBOOKOPEN = "[C] AC RequestRecipeBookOpen";
	@SuppressWarnings("unused")
	private final static Log _log = LogFactory.getLog(RequestRecipeBookOpen.class.getName());

	private boolean _isDwarvenCraft;

	/**
	 * packet type id 0xac
	 * packet format rev656  cd
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_isDwarvenCraft = (readD() == 0);
	}

	@Override
	protected void runImpl()
	{
		if (getClient().getActiveChar() == null)
			return;

		if (getClient().getActiveChar().getPrivateStoreType() != 0)
		{
			getClient().getActiveChar().sendPacket(new SystemMessage(SystemMessageId.PRIVATE_STORE_UNDER_WAY));
			return;
		}

		RecipeController.getInstance().requestBookOpen(getClient().getActiveChar(), _isDwarvenCraft);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__AC_REQUESTRECIPEBOOKOPEN;
	}
}
