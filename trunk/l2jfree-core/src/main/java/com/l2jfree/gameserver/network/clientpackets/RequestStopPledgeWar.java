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
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class RequestStopPledgeWar extends L2GameClientPacket
{
	private static final String _C__4F_REQUESTSTOPPLEDGEWAR = "[C] 4F RequestStopPledgeWar";

	private String _pledgeName;

    @Override
    protected void readImpl()
    {
        _pledgeName = readS();
    }

    @Override
    protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null) return;
		L2Clan clan = player.getClan();
		if (clan == null)
	    {
	    	requestFailed(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
	        return;
	    }

		L2Clan warClan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (warClan == null)
        {
            requestFailed(SystemMessageId.CLAN_DOESNT_EXISTS);
            return;
        }

		if (!clan.isAtWarWith(warClan.getClanId()))
        {
            requestFailed(new SystemMessage(SystemMessageId.NO_CLAN_WAR_AGAINST_CLAN_S1).addString(warClan.getName()));
            return;
        }

		//_log.info("RequestStopPledgeWar: By leader: " + playerClan.getLeaderName() + " of clan: "
		//	+ playerClan.getName() + " to clan: " + _pledgeName);

		//        L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());
		//        if(leader != null && leader.isOnline() == 0)
		//        {
		//            player.sendMessage("Clan leader isn't online.");
		//            player.sendPacket(new ActionFailed());
		//            return;
		//        }

		//        if (leader.isProcessingRequest())
		//        {
		//            SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER);
		//            sm.addString(leader.getName());
		//            player.sendPacket(sm);
		//            return;
		//        }

		ClanTable.getInstance().deleteclanswars(clan.getClanId(), warClan.getClanId());

        sendAF();
		//        player.onTransactionRequest(leader);
		//        leader.sendPacket(new StopPledgeWar(_clan.getName(),player.getName()));
	}

	@Override
	public String getType()
	{
		return _C__4F_REQUESTSTOPPLEDGEWAR;
	}
}
