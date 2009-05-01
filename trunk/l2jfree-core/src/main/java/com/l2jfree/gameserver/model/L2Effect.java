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
package com.l2jfree.gameserver.model;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.EffectInfoPacket.EffectInfoPacketList;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.effects.EffectTemplate;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.skills.funcs.FuncOwner;
import com.l2jfree.gameserver.skills.funcs.FuncTemplate;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.util.LinkedBunch;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.12 $ $Date: 2005/04/11 10:06:07 $
 */
public abstract class L2Effect implements FuncOwner
{
	static final Log	_log	= LogFactory.getLog(L2Effect.class.getName());
	
	public static final L2Effect[] EMPTY_ARRAY = new L2Effect[0];
	
	public static enum EffectState
	{
		CREATED,
		ACTING,
		FINISHING
	}
	
	// member _effector is the instance of L2Character that cast/used the spell/skill that is
	// causing this effect. Do not confuse with the instance of L2Character that
	// is being affected by this effect.
	private final L2Character		_effector;
	
	// member _effected is the instance of L2Character that was affected
	// by this effect. Do not confuse with the instance of L2Character that
	// casted/used this effect.
	private final L2Character		_effected;
	
	// the skill that was used.
	private final L2Skill			_skill;
	
	// or the items that was used.
	// private final L2Item _item;
	
	// the current state
	private EffectState				_state;
	
	// period, seconds
	private int						_period;
	private int						_periodStartTicks;
	private int						_periodfirsttime;

	// Effect template
	private EffectTemplate			_template;
	
	// counter
	private int						_count;
	
	public final class EffectTask implements Runnable
	{
		protected final int	_delay;
		protected final int	_rate;
		
		EffectTask(int delay, int rate)
		{
			_delay = delay;
			_rate = rate;
		}
		
		public void run()
		{
			try
			{
				if (_periodfirsttime == 0)
					_periodStartTicks = GameTimeController.getGameTicks();
				else
					_periodfirsttime = 0;
				scheduleEffect();
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}
	}
	
	private ScheduledFuture<?> _currentFuture;
	private EffectTask         _currentTask;
	
	private boolean			_inUse	= false;
	private boolean			_startConditionsCorrect = true;
	
	protected L2Effect(Env env, EffectTemplate template)
	{
		_state = EffectState.CREATED;
		_skill = env.skill;
		// _item = env._item == null ? null : env._item.getItem();
		_template = template;
		_effected = env.target;
		_effector = env.player;
		_count = template.count;

		// TODO DrHouse: This should be reworked, we need to be able to change effect time out of Effect Constructor
		// maybe using a child class
		// Support for retail herbs duration when _effected has a Summon
		int id = _skill.getId();
		int temp = template.period;
		if ((id > 2277 && id < 2286) || (id >= 2512 && id <= 2514))
		{
			if (_effected instanceof L2SummonInstance
				|| (_effected instanceof L2PcInstance && _effected.getPet() instanceof L2SummonInstance))
			{
				temp /= 2;
			}
		}
		
		if (env.skillMastery)
			temp *= 2;
		
		_period = temp;

		_periodStartTicks = GameTimeController.getGameTicks();
		_periodfirsttime = 0;
		scheduleEffect();
	}
	
	/**
	 * Special constructor to "steal" buffs. Must be implemented on
	 * every child class that can be stolen.
	 *
	 * @param env
	 * @param effect
	 */
	protected L2Effect(Env env, L2Effect effect)
	{
		_template = effect._template;
		_state = EffectState.CREATED;
		_skill = env.skill;
		_effected = env.target;
		_effector = env.player;
		_count = effect.getCount();
		_period = _template.period - effect.getTime();
		_periodStartTicks = effect.getPeriodStartTicks();
		_periodfirsttime = effect.getPeriodfirsttime();
		scheduleEffect();
	}

	public int getCount()
	{
		return _count;
	}
	
	public int getTotalCount()
	{
		return _template.count;
	}
	
	public void setCount(int newcount)
	{
		_count = newcount;
	}
	
	public void setFirstTime(int newfirsttime)
	{
		if (_currentFuture != null)
		{
			_periodStartTicks = GameTimeController.getGameTicks() - newfirsttime * GameTimeController.TICKS_PER_SECOND;
			_currentFuture.cancel(false);
			_currentFuture = null;
			_currentTask = null;
			_periodfirsttime = newfirsttime;
			int duration = _period - _periodfirsttime;
			// _log.warn("Period: "+_period+"-"+_periodfirsttime+"="+duration);
			_currentTask = new EffectTask(duration * 1000, -1);
			_currentFuture = ThreadPoolManager.getInstance().scheduleEffect(_currentTask, duration * 1000);
		}
	}

	public boolean getShowIcon()
	{
		return _template.showIcon;
	}

	public int getPeriod()
	{
		return _period;
	}
	
	public int getTime()
	{
		return (GameTimeController.getGameTicks() - _periodStartTicks) / GameTimeController.TICKS_PER_SECOND;
	}
	
	/**
	 * Returns the elapsed time of the task.
	 * 
	 * @return Time in seconds.
	 */
	public int getElapsedTaskTime()
	{
		return (getTotalCount() - _count) * _period + getTime() + 1;
	}
	
	public int getTotalTaskTime()
	{
		return getTotalCount() * _period;
	}
	
	public int getRemainingTaskTime()
	{
		return getTotalTaskTime() - getElapsedTaskTime();
	}
	
	public int getPeriodfirsttime()
	{
		return _periodfirsttime;
	}
	
	public void setPeriodfirsttime(int periodfirsttime)
	{
		_periodfirsttime = periodfirsttime;
	}
	
	public int getPeriodStartTicks()
	{
		return _periodStartTicks;
	}

	public void setPeriodStartTicks(int periodStartTicks)
	{
		_periodStartTicks = periodStartTicks;
	}
	
	public final boolean getInUse()
	{
		return _inUse;
	}
	
	public final void setInUse(boolean inUse)
	{
		_inUse = inUse;
		if (_inUse)
		{
			_startConditionsCorrect = onStart();
			
			if (_template.abnormalEffect != 0)
				getEffected().startAbnormalEffect(_template.abnormalEffect);
		}
		else
		{
			if (_template.abnormalEffect != 0)
				getEffected().stopAbnormalEffect(_template.abnormalEffect);
			
			if (_startConditionsCorrect)
				onExit();
		}
	}
	
	public String getStackType()
	{
		return _template.stackType;
	}
	
	public float getStackOrder()
	{
		return _template.stackOrder;
	}
	
	public final L2Skill getSkill()
	{
		return _skill;
	}
	
	public final L2Character getEffector()
	{
		return _effector;
	}

	public final L2Character getEffected()
	{
		return _effected;
	}
	
	public boolean isSelfEffect()
	{
		return _skill._effectTemplatesSelf != null;
	}
	
	public boolean isHerbEffect()
	{
        return getSkill().getName().contains("Herb");
    }
	
	public final double calc()
	{
		return _template.lambda;
	}
	
	private synchronized void startEffectTask(int duration)
	{
		stopEffectTask();
		_currentTask = new EffectTask(duration, -1);
		_currentFuture = ThreadPoolManager.getInstance().scheduleEffect(_currentTask, duration);
		
		if (_state == EffectState.ACTING)
			_effected.addEffect(this);
	}

	private synchronized void startEffectTaskAtFixedRate(int delay, int rate)
	{
		stopEffectTask();
		_currentTask = new EffectTask(delay, rate);
		_currentFuture = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(_currentTask, delay, rate);
		
		if (_state == EffectState.ACTING)
			_effected.addEffect(this);
	}
	
	/**
	 * Stop the L2Effect task and send Server->Client update packet.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Cancel the effect in the the abnormal effect map of the L2Character </li>
	 * <li>Stop the task of the L2Effect, remove it and update client magic icons </li>
	 * <BR>
	 * <BR>
	 */
	public final void exit()
	{
		_state = EffectState.FINISHING;
		scheduleEffect();
	}
	
	/**
	 * Stop the task of the L2Effect, remove it and update client magic icons.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Cancel the task </li>
	 * <li>Stop and remove L2Effect from L2Character and update client magic icons </li>
	 * <BR>
	 * <BR>
	 */
	public void stopEffectTask()
	{
		if (_currentFuture != null)
		{
			// Cancel the task
			_currentFuture.cancel(false);
			_currentFuture = null;
			_currentTask = null;
			
			_effected.removeEffect(this);
		}
	}
	
	/** returns effect type */
	public abstract L2EffectType getEffectType();
	
	/** Notify started */
	protected boolean onStart()
	{
		return true;
	}
	
	/**
	 * Cancel the effect in the the abnormal effect map of the effected L2Character.<BR>
	 * <BR>
	 */
	protected void onExit()
	{
	}
	
	/** Return true for continueation of this effect */
	public abstract boolean onActionTime();
	
	public final void rescheduleEffect()
	{
		if (_state != EffectState.ACTING)
		{
			scheduleEffect();
		}
		else
		{
			if (_count > 1)
			{
				startEffectTaskAtFixedRate(5, _period * 1000);
				return;
			}
			if (_period > 0)
			{
				startEffectTask(_period * 1000);
			}
		}
	}
	
	public final void scheduleEffect()
	{
		if (_state == EffectState.CREATED)
		{
			_state = EffectState.ACTING;
			
			if (_skill.isPvpSkill())
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				smsg.addSkillName(_skill);
				getEffected().sendPacket(smsg);
			}
			
			GlobalRestrictions.effectCreated(this);
			
			if (_count > 1)
			{
				startEffectTaskAtFixedRate(5, _period * 1000);
				return;
			}
			
			if (_period > 0)
			{
				startEffectTask(_period * 1000);
				return;
			}
			// effects not having count or period should start
			setInUse(true);
		}
		
		if (_state == EffectState.ACTING)
		{
			if (_count-- > 0)
			{
				if (getInUse()) // effect has to be in use
				{
					if (onActionTime() && _startConditionsCorrect)
						return; // false causes effect to finish right away
				}
				else if (_count > 0) // do not finish it yet, in case reactivated
					return;
			}
			_state = EffectState.FINISHING;
		}
		
		if (_state == EffectState.FINISHING)
		{
			// Cancel the effect in the the abnormal effect map of the L2Character
			if (getInUse() || !(_count > 1 || _period > 0))
				setInUse(false);
			
			// If the time left is equal to zero, send the message
			if (_count == 0)
			{
				SystemMessage smsg3 = new SystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
				smsg3.addSkillName(_skill);
				getEffected().sendPacket(smsg3);
			}
			// Stop the task of the L2Effect, remove it and update client magic icons
			stopEffectTask();
			
		}
	}
	
	public Func[] getStatFuncs()
	{
		if (_template.funcTemplates == null)
			return Func.EMPTY_ARRAY;
		
		LinkedBunch<Func> funcs = new LinkedBunch<Func>();
		for (FuncTemplate t : _template.funcTemplates)
		{
			Env env = new Env();
			env.player = getEffector();
			env.target = getEffected();
			env.skill = getSkill();
			Func f = t.getFunc(env, this); // effect is owner
			if (f != null)
				funcs.add(f);
		}
		
		if (funcs.size() == 0)
			return Func.EMPTY_ARRAY;
		
		return funcs.moveToArray(new Func[funcs.size()]);
	}
	
	public final void addPacket(EffectInfoPacketList list)
	{
		if (!_inUse || !getShowIcon())
			return;
		
		switch (_state)
		{
			case CREATED:
			case FINISHING:
				return;
		}
		
		// FIXME: why?
		switch (_skill.getId())
		{
			case 2031:
			case 2032:
			case 2037:
				return;
		}
		
		switch (getEffectType())
		{
			case SIGNET_GROUND:
				return;
		}
		
		final EffectTask task = _currentTask;
		final ScheduledFuture<?> future = _currentFuture;
		
		if (task == null || future == null)
			return;
		
		int time;
		
		if (task._rate > 0)
			time = getRemainingTaskTime() * 1000;
		else
			time = (int)future.getDelay(TimeUnit.MILLISECONDS);
		
		time = (time < 0 ? -1 : time / 1000);
		
		list.addEffect(_skill.getDisplayId(), _skill.getLevel(), time);
	}
	
	public final int getId()
	{
		return getSkill().getId();
	}
	
	public final int getLevel()
	{
		return getSkill().getLevel();
	}
	
	public EffectTemplate getEffectTemplate()
	{
		return _template;
	}
	
	public final String getFuncOwnerName()
	{
		return _skill.getFuncOwnerName();
	}
	
	public final L2Skill getFuncOwnerSkill()
	{
		return _skill.getFuncOwnerSkill();
	}
	
	public boolean canBeStoredInDb()
	{
		if (getSkill().isToggle())
			return false;
		
		if (isHerbEffect())
			return false;
		
		return true;
	}
}
