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
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

/**
 * @author decad
 * 
 * Implementation of the Bluff Effect
 */
public final class EffectBluff extends L2Effect
{
	public EffectBluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLUFF; // test for bluff effect
	}

	/** Notify started */
	@Override
	public boolean onStart()
	{
		getEffected().startFear();
		if (getEffected() instanceof L2FolkInstance)
			return false;
		// if(getEffected() instanceof L2SiegeGuardInstance) return;
		// Cannot be used on Headquarters Flag.
		// bluff now is a PVE PVP skill
		if (getEffected() instanceof L2NpcInstance && ((L2NpcInstance) getEffected()).getNpcId() == 35062 || getSkill().getId() != 358)
			return false;

		if (getEffected() instanceof L2SiegeSummonInstance)
			return false;

		int posX = getEffected().getX();
		int posY = getEffected().getY();
		int posZ = getEffected().getZ();
		int signx = -1;
		int signy = -1;
		if (getEffected().getX() > getEffector().getX())
			signx = 1;
		if (getEffected().getY() > getEffector().getY())
			signy = 1;

		// posX += signx*40; //distance less than melee attacks (40)
		// posY += signy*40;

		getEffected().setTarget(null);
		getEffected().abortAttack();
		getEffected().abortCast();
		if (!(getEffected() instanceof L2PetInstance))
			getEffected().setRunning();
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX + (signx * 40), posY + (signy * 40), posZ, 0));
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopFear(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
