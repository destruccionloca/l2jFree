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
package com.l2jfree.gameserver.model.actor.stat;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.skills.Stats;

public class NpcStat extends CharStat
{
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public NpcStat(L2Npc activeChar)
	{
		super(activeChar);

		setLevel(getActiveChar().getTemplate().getLevel());
	}

	// =========================================================
	// Method - Public

	// =========================================================
	// Method - Private

	// =========================================================
	// Property - Public
	@Override
	public L2Npc getActiveChar()
	{
		return (L2Npc) _activeChar;
	}

	@Override
	public final int getMaxHp()
	{
		return (int)calcStat(Stats.MAX_HP, getActiveChar().getTemplate().getBaseHpMax()
			* (getActiveChar().isChampion() ? Config.CHAMPION_HP : 1) , null, null);
	}

	@Override
	public int getWalkSpeed()
	{
		return (int) calcStat(Stats.WALK_SPEED, getActiveChar().getTemplate().getBaseWalkSpd(), null, null);
	}

	@Override
	public float getMovementSpeedMultiplier()
	{
		if (getActiveChar().isRunning())
			return getRunSpeed() * 1f / getActiveChar().getTemplate().getBaseRunSpd();
		else
			return getWalkSpeed() * 1f / getActiveChar().getTemplate().getBaseWalkSpd();
	}
}
