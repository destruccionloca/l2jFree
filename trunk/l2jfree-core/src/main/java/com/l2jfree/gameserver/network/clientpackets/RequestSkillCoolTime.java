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

/**
 * Format: (c)
 */
public class RequestSkillCoolTime extends L2GameClientPacket
{
	private static final String	_C__A6_REQUESTSKILLCOOLTIME	= "[C] 0xa6 RequestSkillCoolTime";

	@Override
	protected void readImpl()
	{
		// Trigger
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getActiveChar();
		if (player == null)
			return;

		player.sendSkillCoolTime();

		sendAF();
	}

	@Override
	public String getType()
	{
		return _C__A6_REQUESTSKILLCOOLTIME;
	}
}
