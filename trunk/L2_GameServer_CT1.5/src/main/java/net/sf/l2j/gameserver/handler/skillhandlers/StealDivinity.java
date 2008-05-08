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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Sephiroth
 */
public class StealDivinity implements ISkillHandler
{
	private static final SkillType[]	SKILL_IDS	=
													{ SkillType.STEAL_DIV };

	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		if (weaponInst != null)
		{
			L2Character _Effected = null;

			for (L2Object element : targets)
			{
				_Effected = (L2Character) element;
				int iStealedCount = 0;
				int iMaxStealCount = 0;
				int iTargetRemainingTime = 0;

				iMaxStealCount = activeChar.getSkillLevel(L2Skill.SKILL_STEAL_DIVINITY);
				iMaxStealCount *= iMaxStealCount;

				L2Effect[] effects = _Effected.getAllEffects();
				for (L2Effect e : effects)
				{
					if (e.getSkill().getSkillType() == SkillType.BUFF)
					{
						if (e.getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_ONE
								|| e.getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY
								|| e.getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY_MEMBER
								|| e.getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY)
						{
							if (iStealedCount < iMaxStealCount)
							{
								if (element instanceof L2PcInstance)
								{
									SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
									sm2.addSkillName(e.getSkill().getId());
									((L2PcInstance) element).sendPacket(sm2);
								}

								SystemMessage sm1 = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
								sm1.addSkillName(e.getSkill().getId());
								activeChar.sendPacket(sm1);

								iTargetRemainingTime = e.getRemainingTaskTime();
								e.stopEffectTask();
								_Effected.removeEffect(e);
								e.setStolenEffected(activeChar);
								e.setPeriod(iTargetRemainingTime);
								e.startStolenEffectTask(iTargetRemainingTime * 1000);
								activeChar.updateEffectIcons();
								iStealedCount++;
							}
						}
					}
				}

			}
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
