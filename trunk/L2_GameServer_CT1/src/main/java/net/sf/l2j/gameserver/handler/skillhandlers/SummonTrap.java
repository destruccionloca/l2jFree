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

import net.sf.l2j.tools.random.Rnd;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.skills.effects.EffectTrap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Darki699
 * Summons a trap, Detects a trap, Removes a trap.
 * All traps instances are configured and temporarily saved in EffectTrap.java
 */
public class SummonTrap implements ISkillHandler
{
	private static Log _log = LogFactory.getLog(SummonTrap.class.getName());
	private static final SkillType[] SKILL_IDS = {SkillType.SUMMON_TRAP , SkillType.DETECT_TRAP , SkillType.REMOVE_TRAP};

	public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused")L2Object[] targets)
	{
		if (activeChar == null || skill == null) return;

		switch (skill.getSkillType())
		{
			case SUMMON_TRAP:

				if (skill.getTriggeredSkill() == null) return;

				try
				{
					EffectTrap.getInstance().addTrap(activeChar , skill);
				}
				catch(Throwable t)
				{
					_log.warn("Failed to summon trap for "+activeChar.getName()+", from skill "+skill.getName()+"(Id: "+skill.getId()+"): "+t);
				}

				return;

			case DETECT_TRAP:

				if (Rnd.get(100) < ((skill.getLandingPercent() > 0) ? skill.getLandingPercent() : 100))
				{
					if (EffectTrap.getInstance().detectTrap(activeChar.getX(), activeChar.getY(), activeChar.getZ(), skill.getSkillRadius(), (int)skill.getPower()))
					{

						if (activeChar instanceof L2PcInstance)
						{
							((L2PcInstance)activeChar).sendMessage("A trap device has been detected.");
						}

						return;
					}
				}

			case REMOVE_TRAP:

				if (activeChar.getTarget() == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_CANT_FOUND));
					activeChar.sendPacket(new ActionFailed());
					return;
				}
				else if (!(activeChar.getTarget() instanceof L2NpcInstance))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
					activeChar.sendPacket(new ActionFailed());
					return;
				}

				// Removes one trap
				else if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_ONE)
				{
					if (Rnd.get(100) < ((skill.getLandingPercent() > 0) ? skill.getLandingPercent() : 100))
					{

						if (EffectTrap.getInstance().removeOneTrap(activeChar.getTarget(), (int)skill.getPower()))
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED));
							return;
						}

					}
				}

				// Removes all traps in the skill radius
				else
				{
					if (Rnd.get(100) < ((skill.getLandingPercent() > 0) ? skill.getLandingPercent() : 100))
					{

						if (EffectTrap.getInstance().removeTrap(activeChar.getX(), activeChar.getY(), activeChar.getZ(), skill.getSkillRadius(), (int)skill.getPower()))
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED));
							return;
						}

					}
				}
		}

		activeChar.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
		return;
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}