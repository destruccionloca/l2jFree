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

import com.l2jfree.gameserver.model.L2Party;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ExAskJoinMPCC;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

public class RequestExAskJoinMPCC extends L2GameClientPacket
{
	private static final String _C__REQUESTEXASKJOINMPCC = "[C] D0:06 RequestExAskJoinMPCC ch[s]";

	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
			return;

		L2PcInstance player = L2World.getInstance().getPlayer(_name);
		if (player == null)
		{
			requestFailed(SystemMessageId.NO_USER_INVITED_TO_COMMAND_CHANNEL);
			return;
		}
		else if (!player.isInParty())
		{
			sendAF();
			return;
		}

		L2Party activeParty = activeChar.getParty();
		L2Party invitedParty = player.getParty();
		if (!activeChar.isInParty())
		{
			requestFailed(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
			return;
		}
		else if (activeParty.getLeader() != activeChar)
		{
			requestFailed(SystemMessageId.COMMAND_CHANNEL_ONLY_FOR_PARTY_LEADER);
			return;
		}
		// invite yourself? ;)
		else if (activeParty.equals(player.getParty()))
		{
			sendAF();
			return;
		}
		else if (invitedParty.isInCommandChannel())
		{
			requestFailed(new SystemMessage(SystemMessageId.C1_ALREADY_MEMBER_OF_COMMAND_CHANNEL).addString(player.getName()));
			return;
		}

		if (activeParty.isInCommandChannel())
		{
			if (!activeParty.getCommandChannel().getChannelLeader().equals(activeChar))
			{
				requestFailed(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
				return;
			}
			else
				tryInvite(invitedParty, false);
		}
		else
			tryInvite(invitedParty, true);

		sendAF();
	}

	private final void tryInvite(L2Party invited, boolean newCC)
	{
		L2PcInstance activeChar = getActiveChar();
		if (newCC)
		{
			if (!canCreateCC(activeChar))
			{
				sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
				return;
			}
		}

		L2PcInstance contact = invited.getLeader();
		if (!contact.isProcessingRequest())
		{
			activeChar.onTransactionRequest(contact);
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_INVITING_YOU_TO_COMMAND_CHANNEL_CONFIRM);
			sm.addString(activeChar.getName());
			contact.sendPacket(sm);
			contact.sendPacket(new ExAskJoinMPCC(activeChar.getName()));
		}
		else
			//sendPacket(new SystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(_name));
			sendPacket(new SystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(contact.getName()));
	}

	private final boolean canCreateCC(L2PcInstance creator)
	{
		if (creator == null)
			return false;

		for (L2Skill s : creator.getClan().getAllSkills())
			if (s.getId() == 391 && s.checkCondition(creator, creator))
				return true;

		// TODO: revise! 8871 Strategy Guide. Should be destroyed after successful invite?
		return creator.destroyItemByItemId("MPCC Creation", 8871, 1, creator, true);
	}

	@Override
	public String getType()
	{
		return _C__REQUESTEXASKJOINMPCC;
	}
}
