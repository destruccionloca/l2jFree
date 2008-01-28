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

import java.util.concurrent.Future;

import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.ThreadPoolManager;

/**
 * 
 * @author Darki699
 */
final class EffectPhoenixSoul extends L2Effect
{

	public EffectPhoenixSoul(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}

	@Override
	public void onStart()
	{
		if (getEffected() instanceof L2PlayableInstance)
			((L2PlayableInstance) getEffected()).startNoblesseBlessing();
	}

	@Override
	public void onExit()
	{
		if (getEffected().isAlikeDead() && getEffected() instanceof L2PlayableInstance)
		{
			// If dead, revive to full health, and kill the effect
			getEffected().doRevive(100);
			PhoenixRevive pr = new PhoenixRevive();
			/* For some reason it doesn't heal HP unless it's timed after Ressurection */
			Future task = ThreadPoolManager.getInstance().scheduleGeneral(pr, 1000);
			pr.setTask(task);
		}
		else 
		{
			// if not dead, and doesn't have Nobless Bless buff (id 1323), just remove the effect...
			if (getEffected().getFirstEffect(1323) == null && getEffected() instanceof L2PlayableInstance)
			{
				((L2PlayableInstance) getEffected()).stopNoblesseBlessing(this);
			}
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	private class PhoenixRevive implements Runnable
	{
		Future _task = null;
		
		public void setTask(Future task)
		{
			_task = task;
		}
		
		public void run()
		{
			if (_task != null)
			{
				_task.cancel(true);
				_task = null;
			}

			getEffected().getStatus().setCurrentHpMp(getEffected().getMaxHp(),getEffected().getMaxMp());
			getEffected().getStatus().setCurrentCp(getEffected().getMaxCp(),true);
			
		}
	}
}
