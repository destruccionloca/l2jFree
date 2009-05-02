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

import com.l2jfree.gameserver.model.BlockList;
import com.l2jfree.gameserver.model.L2Party;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.AskJoinParty;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 *  sample
 *  29
 *  42 00 00 10
 *  01 00 00 00
 * 
 *  format  cdd
 * 
 * 
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinParty extends L2GameClientPacket
{
	private static final String _C__29_REQUESTJOINPARTY = "[C] 29 RequestJoinParty";
	private final static Log _log = LogFactory.getLog(RequestJoinParty.class.getName());
	
	private String _name;
	private int _itemDistribution;

	@Override
	protected void readImpl()
	{
		_name = readS();
		_itemDistribution = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance requestor = getClient().getActiveChar();
		L2PcInstance target = L2World.getInstance().getPlayer(_name);

		if (requestor == null)
			return;

		if (target == null || (target.isGM() && target.getAppearance().isInvisible() && !requestor.isGM()))
		{
			requestor.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		if (BlockList.isBlocked(target, requestor))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_ADDED_YOU_TO_IGNORE_LIST);
			sm.addCharName(target);
			requestor.sendPacket(sm);
			return;
		}

		if (target.isInParty())
		{
			SystemMessage msg = new SystemMessage(SystemMessageId.C1_IS_ALREADY_IN_PARTY);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			return;
		}

		if (target == requestor)
		{
			requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		/*
		if (!requestor.isGM() && target.getInstanceId() != requestor.getInstanceId())
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		*/
		
		if (!GlobalRestrictions.canInviteToParty(requestor, target))
			return;
		
		if (!requestor.isInParty())     //Asker has no party
		{
			createNewParty(target, requestor);
		}
		else                            //Asker is in party
		{
			if(requestor.getParty().isInDimensionalRift())
			{
				requestor.sendMessage("You can't invite a player when in Dimensional Rift.");
			}
			else
			{
				addTargetToParty(target, requestor);
			}
		}
	}

	/**
	 * @param client
	 * @param itemDistribution
	 * @param target
	 * @param requestor
	 */
	private void addTargetToParty(L2PcInstance target, L2PcInstance requestor)
	{
		SystemMessage msg;

		// summary of ppl already in party and ppl that get invitation
		if (!requestor.getParty().isLeader(requestor))
		{
			requestor.sendPacket(SystemMessageId.ONLY_LEADER_CAN_INVITE);
			return;
		}

		if (requestor.getParty().getMemberCount() >= 9 )
		{
			requestor.sendPacket(SystemMessageId.PARTY_FULL);
			return;
		}
		
		if (requestor.getParty().getPendingInvitation())
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		
		if (!target.isProcessingRequest())
		{
			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), requestor.getParty().getLootDistribution()));
			requestor.getParty().setPendingInvitation(true);

			if (_log.isDebugEnabled())
				_log.debug("sent out a party invitation to:"+target.getName());
		}
		else
		{
			msg = new SystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			msg.addString(target.getName());
			requestor.sendPacket(msg);

			if (_log.isDebugEnabled())
				_log.warn(requestor.getName() + " already received a party invitation");
		}
		msg = null;
	}

	/**
	 * @param client
	 * @param itemDistribution
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(L2PcInstance target, L2PcInstance requestor)
	{
		if (!target.isProcessingRequest())
		{
			requestor.setParty(new L2Party(requestor, _itemDistribution));

			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
			requestor.getParty().setPendingInvitation(true);

			if (_log.isDebugEnabled())
				_log.debug("sent out a party invitation to:"+target.getName());
		}
		else
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);

			if (_log.isDebugEnabled())
				_log.warn(requestor.getName() + " already received a party invitation");
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__29_REQUESTJOINPARTY;
	}
}