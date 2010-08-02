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

import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2ClanMember;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.taskmanager.AttackStanceTaskManager;

public class RequestStopPledgeWar extends L2GameClientPacket
{
	private static final String	_C__REQUESTSTOPPLEDGEWAR = "[C] 05 RequestStopPledgeWar c[s]";
	
	private String _pledgeName;
	
	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getActiveChar();
		if (player == null)
			return;
		L2Clan clan = player.getClan();
		if (!L2Clan.checkPrivileges(player, L2Clan.CP_CL_PLEDGE_WAR))
		{
			requestFailed(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		L2Clan warClan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (warClan == null)
		{
			requestFailed(SystemMessageId.CLAN_DOESNT_EXISTS);
			return;
		}
		else if (!clan.isAtWarWith(warClan.getClanId()))
		{
			requestFailed(new SystemMessage(SystemMessageId.NO_CLAN_WAR_AGAINST_CLAN_S1).addString(warClan.getName()));
			return;
		}
		
		for (L2ClanMember member : clan.getMembers())
		{
			if (member == null || member.getPlayerInstance() == null)
				continue;
			if (AttackStanceTaskManager.getInstance().getAttackStanceTask(member.getPlayerInstance()))
			{
				requestFailed(SystemMessageId.CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT);
				return;
			}
		}
		
		ClanTable.getInstance().deleteClanWars(clan.getClanId(), warClan.getClanId());
		
		sendAF();
	}
	
	@Override
	public String getType()
	{
		return _C__REQUESTSTOPPLEDGEWAR;
	}
}
