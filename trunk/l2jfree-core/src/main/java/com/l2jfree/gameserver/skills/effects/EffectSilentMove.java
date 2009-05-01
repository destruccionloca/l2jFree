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
package com.l2jfree.gameserver.skills.effects;

import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

public final class EffectSilentMove extends L2Effect
{
	public EffectSilentMove(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	/** Notify started */
	@Override
	public boolean onStart()
	{
		if (getEffected() instanceof L2Playable)
			((L2Playable) getEffected()).setSilentMoving(true);
		return true;
	}

	/** Notify exited */
	@Override
	public void onExit()
	{
		L2Character effected = getEffected();
		if (effected instanceof L2Playable)
			((L2Playable) effected).setSilentMoving(false);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SILENT_MOVE;
	}

	@Override
	public boolean onActionTime()
	{
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != L2SkillType.CONT)
			return false;

		if (getEffected().isDead())
			return false;

		double manaDam = calc();

		if (manaDam > getEffected().getStatus().getCurrentMp())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			getEffected().sendPacket(sm);
			return false;
		}

		getEffected().reduceCurrentMp(manaDam);
		return true;
	}

}