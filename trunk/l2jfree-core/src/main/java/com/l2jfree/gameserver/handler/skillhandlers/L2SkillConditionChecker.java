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

import com.l2jfree.gameserver.SevenSigns;
import com.l2jfree.gameserver.handler.ISkillConditionChecker;
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2WorldRegion;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSummon;
import com.l2jfree.gameserver.skills.l2skills.L2SkillTrap;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.tools.geometry.Point3D;

/**
 * @author NB4L1
 */
public final class L2SkillConditionChecker extends ISkillConditionChecker
{
	private static final L2SkillType[] SKILL_IDS = { L2SkillType.SIGNET, L2SkillType.SIGNET_CASTTIME,
			L2SkillType.SUMMON_TRAP, L2SkillType.SUMMON };
	
	@Override
	public boolean checkConditions(L2Character activeChar, L2Skill skill)
	{
		switch (skill.getSkillType())
		{
			case SIGNET:
			case SIGNET_CASTTIME:
			{
				// prevent casting signets to peace zone
				final L2WorldRegion region = activeChar.getWorldRegion();
				if (region == null)
					return false;
				
				final int x, y, z;
				if (skill.getTargetType() == SkillTargetType.TARGET_GROUND && activeChar instanceof L2PcInstance)
				{
					final Point3D wp = ((L2PcInstance)activeChar).getCurrentSkillWorldPosition();
					
					x = wp.getX();
					y = wp.getY();
					z = wp.getZ();
				}
				else
				{
					x = activeChar.getX();
					y = activeChar.getY();
					z = activeChar.getZ();
				}
				
				if (!region.checkEffectRangeInsidePeaceZone(skill, x, y, z))
					return false;
				
				break;
			}
			case SUMMON_TRAP:
			{
				final L2SkillTrap skillTrap = (L2SkillTrap)skill;
				final L2PcInstance player = (L2PcInstance)activeChar;
				
				if (player.isInsideZone(L2Zone.FLAG_PEACE))
				{
					player.sendPacket(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE);
					return false;
				}
				
				if (player.getTrap() != null && player.getTrap().getSkill().getId() == skillTrap.getTriggerSkillId())
					return false;
				
				break;
			}
			case SUMMON:
			{
				final L2SkillSummon skillSummon = (L2SkillSummon)skill;
				final L2PcInstance player = (L2PcInstance)activeChar;
				
				if (!skillSummon.isCubic() && (player.getPet() != null || player.isMounted()))
				{
					if (_log.isDebugEnabled())
						_log.info("player has a pet already. ignore summon skill");
					
					player.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
					return false;
				}
				
				// Check if it's ok to summon
				switch (skill.getId())
				{
					case 13: // Siege Golem
					case 299: // Wild Hog Cannon
					case 448: // Swoop Cannon
					{
						if (!SiegeManager.getInstance().checkIfOkToSummon(player, false)
								&& !FortSiegeManager.getInstance().checkIfOkToSummon(player, false))
							return false;
						
						if (SevenSigns.getInstance().checkSummonConditions(player))
							return false;
						
						break;
					}
				}
				
				break;
			}
			default:
				break;
		}
		
		return super.checkConditions(activeChar, skill);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Character... targets)
	{
		skill.useSkill(activeChar, targets);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
