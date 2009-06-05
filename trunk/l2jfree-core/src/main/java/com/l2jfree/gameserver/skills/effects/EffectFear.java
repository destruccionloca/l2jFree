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
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2FortCommanderInstance;
import com.l2jfree.gameserver.model.actor.instance.L2FortSiegeGuardInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeGuardInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.templates.effects.EffectTemplate;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

/**
 * @author littlecrow Implementation of the Fear Effect
 */
public class EffectFear extends L2Effect
{
	public static final int FEAR_RANGE = 500;
	
	private int _dX = -1;
	private int _dY = -1;
	
	public EffectFear(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FEAR;
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.L2Effect#onStart()
	 */
	@Override
	protected boolean onStart()
	{
		// Fear skills cannot be used l2pcinstance to l2pcinstance. Heroic
		// Dread, Curse: Fear, Fear, Horror, Sword Symphony, Word of Fear and
		// Mass Curse Fear are the exceptions.
		if (getEffected() instanceof L2PcInstance && getEffector() instanceof L2PcInstance
			&& getSkill().getId() != 1376 && getSkill().getId() != 1169 && getSkill().getId() != 65
			&& getSkill().getId() != 1092 && getSkill().getId() != 98 && getSkill().getId() != 1272
			&& getSkill().getId() != 1381)
			return false;
		
		if (getEffected() instanceof L2NpcInstance || getEffected() instanceof L2SiegeGuardInstance
			|| getEffected() instanceof L2FortSiegeGuardInstance || getEffected() instanceof L2FortCommanderInstance
			|| getEffected() instanceof L2SiegeFlagInstance || getEffected() instanceof L2SiegeSummonInstance)
			return false;
		
		if (!getEffected().isAfraid())
		{
			if (getEffected().getX() > getEffector().getX())
				_dX = 1;
			if (getEffected().getY() > getEffector().getY())
				_dY = 1;
			
			getEffected().startFear();
			onActionTime();
			return true;
		}
		return false;
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.L2Effect#onExit()
	 */
	@Override
	protected void onExit()
	{
		getEffected().stopFear(false);
	}
	
	/**
	 * @see com.l2jfree.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		int posX = getEffected().getX();
		int posY = getEffected().getY();
		int posZ = getEffected().getZ();
		
		posX += _dX * FEAR_RANGE;
		posY += _dY * FEAR_RANGE;
		if (!(getEffected() instanceof L2PetInstance))
			getEffected().setRunning();
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
		return true;
	}
}
