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

import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.instancemanager.ClanHallManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.ClanHall;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinSiege extends L2GameClientPacket
{
	private static final String	_C__A4_RequestJoinSiege	= "[C] a4 RequestJoinSiege";

	private int					_siegeableID;
	private int					_isAttacker;
	private int					_isJoining;

	@Override
	protected void readImpl()
	{
		_siegeableID = readD();
		_isAttacker = readD();
		_isJoining = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || !activeChar.isClanLeader())
			return;

		ClanHall hideout = null;
		Castle castle = CastleManager.getInstance().getCastleById(_siegeableID);
		if (castle == null)
			hideout = ClanHallManager.getInstance().getClanHallById(_siegeableID);

		if (_isJoining == 1 && System.currentTimeMillis() < activeChar.getClan().getDissolvingExpiryTime())
		{
			requestFailed(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
			return;
		}

		if (castle != null)
		{
			if (castle.getSiege().getIsInProgress())
			{
				requestFailed(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
				return;
			}
			if (_isJoining == 1)
			{
				if (_isAttacker == 1)
					castle.getSiege().registerAttacker(activeChar);
				else
					castle.getSiege().registerDefender(activeChar);
			}
			else
				castle.getSiege().removeSiegeClan(activeChar);
			castle.getSiege().listRegisterClan(activeChar);
		}
		else if (hideout != null)
		{
			if (hideout.getSiege().getIsInProgress())
			{
				requestFailed(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
				return;
			}
			if (_isJoining == 1)
			{
				if (_isAttacker == 1)
					hideout.getSiege().registerAttacker(activeChar, false);
				else
					sendPacket(SystemMessageId.DEFENDER_SIDE_FULL);
			}
			else
				hideout.getSiege().removeSiegeClan(activeChar);
			hideout.getSiege().listRegisterClan(activeChar);
		}

		sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public String getType()
	{
		return _C__A4_RequestJoinSiege;
	}
}
