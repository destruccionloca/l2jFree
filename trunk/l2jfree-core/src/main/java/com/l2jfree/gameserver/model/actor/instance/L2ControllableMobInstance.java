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
package com.l2jfree.gameserver.model.actor.instance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2CharacterAI;
import com.l2jfree.gameserver.ai.L2ControllableMobAI;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

/**
 * @author littlecrow
 *
 */
public class L2ControllableMobInstance extends L2MonsterInstance
{
    private final static Log _log = LogFactory.getLog(L2ControllableMobInstance.class.getName());

	private boolean _isInvul;
	private L2ControllableMobAI _aiBackup;	// To save ai, avoiding beeing detached

	protected class ControllableAIAcessor extends AIAccessor
    {
		@Override
		public void detachAI()
        {
			// Do nothing, AI of controllable mobs can't be detached automatically
		}
	}

	@Override
	public boolean isAggressive()
    {
		return true;
	}

	@Override
	public int getAggroRange()
    {
		// Force mobs to be aggro
		return 500;
	}

	public L2ControllableMobInstance(int objectId, L2NpcTemplate template)
    {
		super(objectId, template);
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // Copy handle
		if (ai == null)
		{
			synchronized(this)
			{
				if (_aiBackup == null)
				{
					_ai = new L2ControllableMobAI(new ControllableAIAcessor());
					_aiBackup = (L2ControllableMobAI)_ai;
				}
				else
				{
					_ai = _aiBackup;
				}
				return _ai;
			}
		}
		return ai;
	}

	@Override
	public boolean isInvul()
    {
		return _isInvul;
	}

	public void setInvul(boolean isInvul)
    {
		_isInvul = isInvul;
	}

	@Override
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
    {
		if (isInvul() || isDead())
			return;

		if (awake)
		{
			if(isSleeping())
				stopSleeping(true);
			if(isImmobileUntilAttacked())
				stopImmobileUntilAttacked(true);
		}

		i = getStatus().getCurrentHp() - i;

		if (i < 0)
			i = 0;

        getStatus().setCurrentHp(i);

		if (getStatus().getCurrentHp() < 0.5) // Die
		{
			// First die (and calculate rewards), if currentHp < 0,  then overhit may be calculated
			if (_log.isDebugEnabled()) _log.debug("char is dead.");

			stopMove(null);

			// Start the doDie process
			doDie(attacker);

			// Now reset currentHp to zero
			getStatus().setCurrentHp(0);
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		removeAI();
		return true;
	}

	@Override
	public void deleteMe()
    {
		removeAI();
		super.deleteMe();
	}

	/**
	 * Definitively remove AI
	 */
	protected void removeAI()
    {
		synchronized (this)
        {
			if (_aiBackup != null)
            {
				_aiBackup.setIntention(CtrlIntention.AI_INTENTION_IDLE);
				_aiBackup = null;
				_ai = null;
			}
		}
	}
}