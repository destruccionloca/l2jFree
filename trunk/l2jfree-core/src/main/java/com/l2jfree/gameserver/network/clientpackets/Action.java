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
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 18:46:19 $
 */
public final class Action extends L2GameClientPacket
{
	private static final String	ACTION__C__04	= "[C] 04 Action";

	// cddddc
	private int					_objectId;
	@SuppressWarnings("unused")
	private int					_originX;
	@SuppressWarnings("unused")
	private int					_originY;
	@SuppressWarnings("unused")
	private int					_originZ;
	private int					_actionId;

	@Override
	protected void readImpl()
	{
		_objectId = readD(); // Target object Identifier
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_actionId = readC(); // Action identifier : 0-Simple click, 1-Shift click
	}

	@Override
	protected void runImpl()
	{
		if (_log.isDebugEnabled())
			_log.debug("Action:" + _actionId);
		if (_log.isDebugEnabled())
			_log.debug("oid:" + _objectId);

		// Get the current L2PcInstance of the player
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
			return;

		if (activeChar.inObserverMode())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			getClient().sendPacket(sm);
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		L2Object obj = null;

		// Get object from target
		if (activeChar.getTargetId() == _objectId)
			obj = activeChar.getTarget();

		// Get object from world
		if (obj == null)
		{
			obj = L2World.getInstance().findObject(_objectId);
			//_log.warn("Player "+activeChar.getName()+" clicked object from outside of his knownlist.");
		}

		// If object requested does not exist, add warn msg into logs
		if (obj == null)
		{
			// pressing e.g. pickup many times quickly would get you here 
			//_log.warn("Character: " + activeChar.getName() + " request action with non existent ObjectID:" + _objectId);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		// Check if the target is valid, if the player haven't a shop or isn't the requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...)
		//if (activeChar.getPrivateStoreType() == 0 && activeChar.getActiveRequester() == null)
		if (activeChar.getActiveRequester() == null)
		{
			switch (_actionId)
			{
				case 0:
					obj.onAction(activeChar);
					break;
				case 1:
					if (obj instanceof L2Character && ((L2Character) obj).isAlikeDead() && !activeChar.isGM())
						obj.onAction(activeChar);
					else
						obj.onActionShift(activeChar);
					break;
				default:
					// Ivalid action detected (probably client cheating), log this
					_log.warn("Character: " + activeChar.getName() + " requested invalid action: " + _actionId);
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					break;
			}
		}
		else
			// Actions prohibited when in trade
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return ACTION__C__04;
	}
}