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

import com.l2jfree.gameserver.GeoData;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.Location;
import com.l2jfree.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfree.gameserver.skills.Env;

/**
 * @author littlecrow
 * 
 * Implementation of the Fear Effect
 */
public final class EffectFear extends L2Effect
{
	public static final int	FEAR_RANGE	= 500;

	public EffectFear(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	public EffectType getEffectType()
	{
		return EffectType.FEAR;
	}

	/** Notify started */
	public void onStart()
	{
		if (!getEffected().isAfraid())
		{
			getEffected().startFear();
			onActionTime();
		}
	}

	/** Notify exited */
	public void onExit()
	{
		getEffected().stopFear(this);
	}

	public boolean onActionTime()
	{
		// Fear skills cannot be used L2Pcinstance to L2Pcinstance.
		// Heroic Dread, Curse: Fear, Fear, Horror, Sword Symphony, Word of Fear and Mass Curse Fear are the exceptions.
		if (getEffected() instanceof L2PcInstance && getEffector() instanceof L2PcInstance)
		{
			switch (getSkill().getId())
			{
			case 65:
			case 98:
			case 1092:
			case 1169:
			case 1272:
			case 1376:
			case 1381:
				// all ok
				break;
			default:
				return false;
			}
		}

		if (getEffected() instanceof L2FolkInstance)
			return false;
		if (getEffected() instanceof L2SiegeGuardInstance)
			return false;

		// Fear skills cannot be used on Headquarters Flag.
		if (getEffected() instanceof L2SiegeFlagInstance)
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

		posX += signx * FEAR_RANGE;
		posY += signy * FEAR_RANGE;

		Location destiny = GeoData.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), posX, posY, posZ);
		getEffected().setRunning();

		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(destiny.getX(), destiny.getY(), destiny.getZ(), 0));

		destiny = null;
		return true;
	}
}
