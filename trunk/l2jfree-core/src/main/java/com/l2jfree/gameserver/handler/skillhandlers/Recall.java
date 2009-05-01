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

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.handler.ISkillHandler;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.mapregion.TeleportWhereType;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.tools.random.Rnd;

public class Recall implements ISkillHandler
{
	private static final L2SkillType[]	SKILL_IDS	=
													{ L2SkillType.RECALL };

	public void useSkill(@SuppressWarnings("unused")
	L2Character activeChar, @SuppressWarnings("unused")
	L2Skill skill, L2Character... targets)
	{
		// [L2J_JP ADD SANDMAN]
		// <!--- Zaken skills - teleport PC --> or <!--- Zaken skills - teleport -->
		if (skill.getId() == 4216 || skill.getId() == 4217 || skill.getId() == 4222)
		{
			doZakenTeleport(targets);
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			if (activeChar instanceof L2MonsterInstance)
				((L2MonsterInstance) activeChar).clearAggroList();
			return;
		}

		if (activeChar instanceof L2PcInstance)
		{
			if (((L2PcInstance) activeChar).isInOlympiadMode())
			{
				activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return;
			}
		}

		for (L2Character target : targets)
		{
			if (target == null)
				continue;
			
			if (target instanceof L2PcInstance)
			{
				L2PcInstance targetChar = (L2PcInstance) target;

				// [L2J_JP ADD]
				if (targetChar.isInsideZone(L2Zone.FLAG_NOESCAPE))
				{
					targetChar.sendMessage("You can not escape from here.");
					targetChar.sendPacket(ActionFailed.STATIC_PACKET);
					break;
				}

				// Check to see if the current player target is in a
				// festival.
				if (targetChar.isFestivalParticipant())
				{
					targetChar.sendMessage("You may not use an escape skill in a festival.");
					continue;
				}

				// Check to see if the current player target is in TvT , CTF
				// or ViP events.
				if (targetChar._inEventCTF || targetChar._inEventTvT || targetChar._inEventVIP)
				{
					targetChar.sendMessage("You may not use an escape skill in a Event.");
					continue;
				}

				// Check to see if player is in jail
				if (targetChar.isInJail() || targetChar.isInsideZone(L2Zone.FLAG_JAIL))
				{
					targetChar.sendMessage("You can not escape from jail.");
					continue;
				}

				// Check to see if player is in a duel
				if (targetChar.isInDuel())
				{
					targetChar.sendMessage("You cannot use escape skills during a duel.");
					continue;
				}
			}
			target.setInstanceId(0);
			target.teleToLocation(TeleportWhereType.Town);
		}
	}

	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	// [L2J_JP ADD SANDMAN]
	protected void doZakenTeleport(L2Character... targets)
	{
		final int loc[][] =
		{
		{ 54228, 220136, -3496 },
		{ 56315, 220127, -3496 },
		{ 56285, 218078, -3496 },
		{ 54238, 218066, -3496 },
		{ 55259, 219107, -3496 },
		{ 56295, 218078, -3224 },
		{ 56283, 220133, -3224 },
		{ 54241, 220127, -3224 },
		{ 54238, 218077, -3224 },
		{ 55268, 219090, -3224 },
		{ 56284, 218078, -2952 },
		{ 54252, 220135, -2952 },
		{ 54244, 218095, -2952 },
		{ 55270, 219086, -2952 } };

		int rndLoc = 0;
		int rndX = 0;
		int rndY = 0;

		for (L2Character target : targets)
		{
			if (target == null)
				continue;

			target.abortAttack();

			rndLoc = Rnd.get(14);
			rndX = Rnd.get(-400, 400);
			rndY = Rnd.get(-400, 400);

			target.teleToLocation(loc[rndLoc][0] + rndX, loc[rndLoc][1] + rndY, loc[rndLoc][2]);
		}
	}
}
