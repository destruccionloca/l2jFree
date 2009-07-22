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
package com.l2jfree.gameserver.handler.itemhandlers;

import com.l2jfree.gameserver.SevenSigns;
import com.l2jfree.gameserver.handler.IItemHandler;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.instancemanager.MercTicketManager;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.network.SystemMessageId;

public class MercTicket implements IItemHandler
{
	private static final String[]	MESSAGES	=
												{ "To arms!", "I am ready to serve you my lord when the time comes.", "You summon me." };

	/**
	 * handler for using mercenary tickets.  Things to do:
	 * 1) Check constraints:
	 * 1.a) Tickets may only be used in a castle
	 * 1.b) Only specific tickets may be used in each castle (different tickets for each castle)
	 * 1.c) only the owner of that castle may use them
	 * 1.d) tickets cannot be used during siege
	 * 1.e) Check if max number of tickets has been reached
	 * 1.f) Check if max number of tickets from this ticket's TYPE has been reached
	 * 2) If allowed, call the MercTicketManager to add the item and spawn in the world
	 * 3) Remove the item from the person's inventory
	 */
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		int itemId = item.getItemId();
		L2PcInstance activeChar = (L2PcInstance) playable;
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		int castleId = -1;
		if (castle != null)
			castleId = castle.getCastleId();

		// Add check that certain tickets can only be placed in certain castles
		if (MercTicketManager.getInstance().getTicketCastleId(itemId) != castleId)
		{
			if (castleId == -1)
			{
				// Player is not in a castle
				activeChar.sendMessage("Mercenary Tickets can only be used in a castle.");
				return;
			}

			switch (castleId)
			{
			case 1:
				activeChar.sendMessage("This Mercenary Ticket can only be used in Gludio.");
				return;
			case 2:
				activeChar.sendMessage("This Mercenary Ticket can only be used in Dion.");
				return;
			case 3:
				activeChar.sendMessage("This Mercenary Ticket can only be used in Giran.");
				return;
			case 4:
				activeChar.sendMessage("This Mercenary Ticket can only be used in Oren.");
				return;
			case 5:
				activeChar.sendMessage("This Mercenary Ticket can only be used in Aden.");
				return;
			case 6:
				activeChar.sendMessage("This Mercenary Ticket can only be used in Heine.");
				return;
			case 7:
				activeChar.sendMessage("This Mercenary Ticket can only be used in Goddard.");
				return;
			case 8:
				activeChar.sendMessage("This Mercenary Ticket can only be used in Rune.");
				return;
			case 9:
				activeChar.sendMessage("This Mercenary Ticket can only be used in Schuttgart.");
				return;
			}
		}

		if ((activeChar.getClanPrivileges() & L2Clan.CP_CS_MERCENARIES) != L2Clan.CP_CS_MERCENARIES)
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_AUTHORITY_TO_POSITION_MERCENARIES);
			return;
		}

		if (castle != null && castle.getSiege().getIsInProgress())
		{
			activeChar.sendMessage("You cannot hire mercenary while siege is in progress!");
			return;
		 }

		// Checking Seven Signs Quest Period
		if (SevenSigns.getInstance().getCurrentPeriod() != SevenSigns.PERIOD_SEAL_VALIDATION)
		{
			//_log.warning("Someone has tried to spawn a guardian during Quest Event Period of The Seven Signs.");
			activeChar.sendMessage("You cannot position any Mercenaries during Quest Period.");
			return;
		}
		// Checking the Seal of Strife status
		switch (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
		{
			case SevenSigns.CABAL_NULL:
				if (SevenSigns.getInstance().checkIsDawnPostingTicket(itemId))
				{
					//_log.warning("Someone has tried to spawn a Dawn Mercenary though the Seal of Strife is not controlled by anyone.");
					activeChar.sendMessage("You cannot position any Dawn Mercenaries at this time.");
					return;
				}
				break;
			case SevenSigns.CABAL_DUSK:
				if (!SevenSigns.getInstance().checkIsRookiePostingTicket(itemId))
				{
					//_log.warning("Someone has tried to spawn a non-Rookie Mercenary though the Seal of Strife is controlled by Revolutionaries of Dusk.");
					activeChar.sendMessage("You can position only Rookie Mercenaries at this time.");
					return;
				}
				break;
			case SevenSigns.CABAL_DAWN:
				break;
		}

		if (MercTicketManager.getInstance().isAtCasleLimit(item.getItemId()))
		{
			activeChar.sendMessage("You cannot hire any more mercenaries");
			return;
		}
		if (MercTicketManager.getInstance().isAtTypeLimit(item.getItemId()))
		{
			activeChar.sendMessage("You cannot hire any more mercenaries of this type.  You may still hire other types of mercenaries");
			return;
		}
		if (MercTicketManager.getInstance().isTooCloseToAnotherTicket(activeChar.getX(), activeChar.getY(), activeChar.getZ()))
		{
			activeChar.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_BECAUSE_DISTANCE_BETWEEN_MERCENARIES_TOO_SHORT);
			return;
		}

		MercTicketManager.getInstance().addTicket(item.getItemId(), activeChar, MESSAGES);
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false); // Remove item from char's inventory
	}

	// Left in here for backward compatibility
	public int[] getItemIds()
	{
		return MercTicketManager.getInstance().getItemIds();
	}
}