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
package com.l2jfree.gameserver.handler.usercommandhandlers;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.IUserCommandHandler;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * Support for /mount command.  
 * @author Tempy
 */
public class Mount implements IUserCommandHandler
{
	private static final int[]	COMMAND_IDS	=
											{ 61 };

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IUserCommandHandler#useUserCommand(int, com.l2jfree.gameserver.model.L2PcInstance)
	 */
	public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
			return false;

		L2Summon pet = activeChar.getPet();

		if (pet != null && pet.isMountable() && !activeChar.isMounted() && !pet.isBetrayed() && pet.isMountableOverTime())
		{
			if (pet.getNpcId() == 16030 && pet.getLevel() < Config.GREAT_WOLF_MOUNT_LEVEL)
			{
				activeChar.sendMessage("Your Wolf needs minimum level " + Config.GREAT_WOLF_MOUNT_LEVEL);
				return false;
			}
			if (activeChar._haveFlagCTF)
			{
				// You cannot mount a steed while holding a flag.
				activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_HOLDING_A_FLAG);
				return false;
			}
			if (activeChar.isTransformed())
			{
				// You cannot mount a steed while transformed.
				activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_TRANSFORMED);
				return false;
			}
			else if (activeChar.isParalyzed())
			{
				// You cannot mount a steed while petrified.
				activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_PETRIFIED);
				return false;
			}
			else if (activeChar.isDead())
			{
				// You cannot mount a steed while dead.
				activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_DEAD);
				return false;
			}
			else if (activeChar.isFishing())
			{
				// You cannot mount a steed while fishing.
				activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_FISHING);
				return false;
			}
			else if (activeChar.isInDuel())
			{
				// You cannot mount a steed while in a duel.
				activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_IN_A_DUEL);
				return false;
			}
			else if (activeChar.isSitting())
			{
				// You cannot mount a steed while sitting.
				activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_SITTING);
				return false;
			}
			else if (activeChar.isCastingNow())
			{
				// You cannot mount a steed while skill casting.
				activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_SKILL_CASTING);
				return false;
			}
			else if (activeChar.isCursedWeaponEquipped())
			{
				// You cannot mount a steed while a cursed weapon is equipped.
				activeChar.sendPacket(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_A_CURSED_WEAPON_IS_EQUIPPED);
				return false;
			}
			/** 
			 * TODO: Add Siege Flag Restriction, 
			 else if (activeChar.isFlagEquipped())
			{
			    // You cannot mount a steed while holding a flag.
			    SystemMessage msg = new SystemMessage(SystemMessageId.YOU_CANNOT_MOUNT_A_STEED_WHILE_HOLDING_A_FLAG);
			    activeChar.sendPacket(msg);
			}
			 */
			else if (activeChar.isInCombat() || activeChar.getPvpFlag() != 0)
			{
				// A pet cannot be ridden while player is in battle.
				activeChar.sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				return false;
			}
			else if (activeChar.isRentedPet())
			{
				activeChar.stopRentPet();
				return false;
			}
			else if (activeChar.isMoving() || activeChar.isInsideZone(L2Zone.FLAG_WATER))
			{
				// A strider can be ridden only when player is standing.
				activeChar.sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				return false;
			}
			else if (pet.isInCombat())
			{
				// A strider in battle cannot be ridden.
				activeChar.sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				return false;
			}
			else if (pet.isDead())
			{
				// A dead strider cannot be ridden.
				activeChar.sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				return false;
			}
			else
				activeChar.mount(pet);
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
