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
package com.l2jfree.gameserver.handler.skillhandlers;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.L2Skill.SkillType;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.model.restriction.AvailableRestriction;
import com.l2jfree.gameserver.model.restriction.ObjectRestrictions;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.util.Util;

/**
 * @authors BiTi, Sami
 * 
 */
public class SummonFriend implements ISkillHandler
{
	private static final Log			_log		= LogFactory.getLog(SummonFriend.class.getName());
	private static final SkillType[]	SKILL_IDS	=
													{ SkillType.SUMMON_FRIEND };

	public void useSkill(@SuppressWarnings("unused")
	L2Character activeChar, @SuppressWarnings("unused")
	L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
			return; // currently not implemented for others

		L2PcInstance activePlayer = (L2PcInstance) activeChar;

		if (activePlayer.isInOlympiadMode())
		{
			activePlayer.sendPacket(new SystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		// Checks summoner not in arenas, siege zones, jail
		if (activePlayer.isInsideZone(L2Zone.FLAG_PVP) || activePlayer.isInFunEvent())
		{
			activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}

		// check for summoner not in raid areas
		FastList<L2Object> objects = L2World.getInstance().getVisibleObjects(activeChar, 5000);

		if (objects != null)
		{
			for (L2Object object : objects)
			{
				if (object instanceof L2RaidBossInstance)
				{
					activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION));
					return;
				}
			}
		}

		try
		{
			for (L2Object element : targets)
			{
				if (!(element instanceof L2Character))
					continue;

				L2Character target = (L2Character) element;

				if (activeChar == target)
					continue;

				if (target instanceof L2PcInstance)
				{
					L2PcInstance targetChar = (L2PcInstance) target;

					// CHECK TARGET CONDITIONS

					//This message naturally doesn't bring up a box...
					//$s1 wishes to summon you from $s2. Do you accept?
					//SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT);
					//sm2.addString(activeChar.getName());
					//String nearestTown = MapRegionTable.getInstance().getClosestTownName(activeChar);
					//sm2.addString(nearestTown);
					//targetChar.sendPacket(sm2);

					// is in same party (not necessary any more) 
					// if (!(targetChar.getParty() != null && targetChar.getParty().getPartyMembers().contains(activeChar)))
					//	continue;

					if (ObjectRestrictions.getInstance()
							.checkRestriction(targetChar, AvailableRestriction.PlayerSummonFriend)) {
						activeChar.sendMessage("You cannot summon your friend due to his restrictions.");
						targetChar.sendMessage("You cannot be summoned due to a restriction.");
						return;
					}

					if (targetChar.isAlikeDead())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						continue;
					}

					if (targetChar.isInStoreMode())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						continue;
					}

					// Target cannot be in combat (or dead, but that's checked by TARGET_PARTY)
					if (targetChar.isRooted() || targetChar.isInCombat())
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
						sm.addString(targetChar.getName());
						activeChar.sendPacket(sm);
						continue;
					}

					// Check for the the target's festival status
					if (targetChar.isInOlympiadMode())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD));
						continue;
					}

					// Check for the the target's festival status
					if (targetChar.isFestivalParticipant())
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}

					// Check for the target's jail status, arenas and siege zones
					if (targetChar.isInsideZone(L2Zone.FLAG_PVP))
					{
						activeChar.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
						continue;
					}

					// Requires a Summoning Crystal
					if (skill.getTargetConsume() != 0)
					{
						if (targetChar.getInventory().getInventoryItemCount(skill.getTargetConsumeId(), 0) < skill.getTargetConsume())
						{
							activeChar.sendMessage("Your target cannot be summoned while he hasn't got enough Summoning Crystal");
							targetChar.sendMessage("You cannot be summoned while you haven't got enough Summoning Crystal");
							continue;
						}
					}

					if (!Util.checkIfInRange(0, activeChar, target, false))
					{
						// set correct instance id
						targetChar.setInstanceId(activeChar.getInstanceId());
						targetChar.sendMessage("You are summoned to a party member.");
						targetChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), true);
						if (skill.getTargetConsume() != 0)
							targetChar.getInventory().destroyItemByItemId("Consume", skill.getTargetConsumeId(), skill.getTargetConsume(), targetChar,
									activeChar);
					}
				}
			}
		}
		catch (Throwable e)
		{
			_log.error(e.getMessage(), e);
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
