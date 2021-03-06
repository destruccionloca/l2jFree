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

import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author -Wooden-
 * 
 */
public class SnoopQuit extends L2GameClientPacket
{
	private static final String _C__AB_SNOOPQUIT = "[C] AB SnoopQuit";

	private int _snoopID;

	@Override
	protected void readImpl()
	{
		_snoopID = readD();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		L2Object target = L2World.getInstance().findObject(_snoopID);

		if (target == null || !(target instanceof L2PcInstance))
			return;

		player.removeSnooped((L2PcInstance)target);
		((L2PcInstance)target).removeSnooper(player);
		player.sendMessage("Surveillance of player "+target.getName()+" canceled.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.l2jfree.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__AB_SNOOPQUIT;
	}
}
