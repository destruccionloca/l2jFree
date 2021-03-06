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
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Env;

public final class EffectCharge extends L2Effect
{
	public int	numCharges;

	public EffectCharge(Env env, EffectTemplate template)
	{
		super(env, template);
		numCharges = 1;
		if (env.target instanceof L2PcInstance)
		{
			env.target.sendPacket(new EtcStatusUpdate((L2PcInstance) env.target));
			SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
			sm.addNumber(numCharges);
			getEffected().sendPacket(sm);
		}
	}

	public EffectType getEffectType()
	{
		return EffectType.CHARGE;
	}

	public boolean onActionTime()
	{
		// ignore
		return true;
	}

	public int getLevel()
	{
		return numCharges;
	}

	public void addNumCharges(int i)
	{
		numCharges += i;
	}
}
