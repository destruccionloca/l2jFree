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
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

/**
 * @author -Nemesiss-
 * 
 */
public class EffectPhysicalMute extends L2Effect
{

	public EffectPhysicalMute(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	public EffectType getEffectType()
	{
		return L2Effect.EffectType.PHYSICAL_MUTE;
	}

	public void onStart()
	{
		getEffected().startPhysicalMuted();
	}

	public boolean onActionTime()
	{
		// Simply stop the effect
		getEffected().stopPhysicalMuted(this);
		return false;
	}

	public void onExit()
	{
		getEffected().stopPhysicalMuted(this);
	}
}
