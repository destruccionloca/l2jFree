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

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfree.gameserver.skills.Env;

/**
 * 
 * @author -Nemesiss-
 */
public final class EffectTargetMe extends L2Effect
{
	public EffectTargetMe(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.TARGET_ME;
	}

	/** Notify started */
	@Override
	public void onStart()
	{
		// Should only work on PC?
		if (getEffected() instanceof L2PcInstance)
		{
			getEffected().setTarget(getEffector());
			MyTargetSelected my = new MyTargetSelected(getEffector().getObjectId(), 0);
			getEffected().sendPacket(my);
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getEffector());
		}
	}

	/** Notify exited */
	@Override
	public void onExit()
	{
		// nothing
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
