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
package com.l2jfree.gameserver.network.serverpackets;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance.TimeStamp;
import com.l2jfree.gameserver.network.L2GameClient;

/**
 * @author KenM
 */
public final class SkillCoolTime extends StaticPacket
{
	public static final SkillCoolTime STATIC_PACKET = new SkillCoolTime();
	
	private SkillCoolTime()
	{
	}
	
	@Override
	public String getType()
	{
		return "[S] C7 SkillCoolTime";
	}
	
	@Override
	protected void writeImpl(L2GameClient client, L2PcInstance activeChar)
	{
		if (activeChar == null)
			return;
		
		writeC(0xc7);
		writeD(activeChar.getReuseTimeStamps().size()); // list size
		for (TimeStamp ts : activeChar.getReuseTimeStamps().values())
		{
			writeD(ts.getSkillId());
			writeD(0x00);
			writeD((int) Math.ceil(ts.getReuseDelay() / 1000.0));
			writeD((int) Math.ceil(ts.getRemaining() / 1000.0));
		}
	}
}
