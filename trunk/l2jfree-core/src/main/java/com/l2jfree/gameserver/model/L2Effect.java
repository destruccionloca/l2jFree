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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.effects.CharEffects;
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
import com.l2jfree.gameserver.threadmanager.FIFORunnableQueue;
import com.l2jfree.util.L2Collections;

public abstract class L2Effect implements FuncOwner, Runnable
{
	protected static final Log _log = LogFactory.getLog(L2Effect.class);
	
	public static final L2Effect[] EMPTY_ARRAY = new L2Effect[0];
	
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
	
	// the current state
	private boolean _isActing;
	
	// period, seconds
	private final int _period;
	private int _periodStartTicks;
	private int _periodfirsttime;

	// Effect template
	private EffectTemplate			_template;
	
	// counter
	private int						_count;
	
	private ScheduledFuture<?> _currentFuture;
	
	private volatile boolean _inUse = false;
	private volatile boolean _startConditionsCorrect = true;
	
	protected L2Effect(Env env, EffectTemplate template)
	{
		_template = template;
		_skill = env.skill;
		_effected = env.target;
		_effector = env.player;
		_count = template.count;
		
		// TODO DrHouse: This should be reworked, we need to be able to change effect time out of Effect Constructor
		// maybe using a child class
		// Support for retail herbs duration when _effected has a Summon
		int id = _skill.getId();
		int temp = template.period;
		if (2277 < id && id < 2286 || 2512 <= id && id <= 2514)
		{
			if (_effected.getActingSummon() instanceof L2SummonInstance)
			{
				temp /= 2;
			}
		}
		
		if (env.skillMastery)
			temp *= 2;
		
		_period = temp;
		_periodStartTicks = GameTimeController.getGameTicks();
		_periodfirsttime = 0;
		
		startEffect();
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
		_skill = env.skill;
		_effected = env.target;
		_effector = env.player;
		_count = effect.getCount();
		_period = _template.period - effect.getTime();
		_periodStartTicks = effect.getPeriodStartTicks();
		_periodfirsttime = effect.getPeriodfirsttime();
		
		startEffect();
	}
	
	private synchronized void startEffect()
	{
		if (_skill.isPvpSkill() && _effected instanceof L2PcInstance)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
			sm.addSkillName(this);
			_effected.sendPacket(sm);
		}
		
		if (_currentFuture != null)
			_currentFuture.cancel(false);
		
		if (_count > 1)
			_currentFuture = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 5, _period * 1000);
		
		else if (_period > 0)
			_currentFuture = ThreadPoolManager.getInstance().schedule(this, _period * 1000);
		
		else
			_effected.getEffects().printStackTrace(getStackType(), this);
		
		setActing(true);
		
		_effected.getEffects().addEffect(this);
		
		GlobalRestrictions.effectCreated(this);
	}
	
	public final int getCount()
	{
		return _count;
	}
	
	public final int getTotalCount()
	{
		return _template.count;
	}
	
	public synchronized final void setTiming(int newcount, int newfirsttime)
	{
		_count = newcount;
		
		_periodStartTicks = GameTimeController.getGameTicks() - newfirsttime * GameTimeController.TICKS_PER_SECOND;
		
		_periodfirsttime = newfirsttime;
		int duration = _period - newfirsttime;
		
		_currentFuture.cancel(false);
		_currentFuture = ThreadPoolManager.getInstance().schedule(this, duration * 1000);
	}
	
	public final boolean getShowIcon()
	{
		return _template.showIcon;
	}

	public final int getPeriod()
	{
		return _period;
	}
	
	public final int getTime()
	{
		return (GameTimeController.getGameTicks() - _periodStartTicks) / GameTimeController.TICKS_PER_SECOND;
	}
	
	/**
	 * Returns the elapsed time of the task.
	 * 
	 * @return Time in seconds.
	 */
	public final int getElapsedTaskTime()
	{
		return (getTotalCount() - _count) * _period + getTime() + 1;
	}
	
	public final int getTotalTaskTime()
	{
		return getTotalCount() * _period;
	}
	
	public final int getRemainingTaskTime()
	{
		return getTotalTaskTime() - getElapsedTaskTime();
	}
	
	public int getPeriodfirsttime()
	{
		return _periodfirsttime;
	}
	
	public int getPeriodStartTicks()
	{
		return _periodStartTicks;
	}
	
	public final boolean isInUse()
	{
		return _inUse;
	}
	
	/**
	 * Must be called from synchronized context from {@link CharEffects}.
	 */
	public synchronized final void setInUse(boolean inUse)
	{
		if (_inUse != inUse)
		{
			_inUse = inUse;
			
			// Asynchronous FIFO execution to avoid deadlocks and concurrency
			EFFECT_QUEUE.execute(_inUse ? ON_START : ON_EXIT);
		}
		else
			_effected.getEffects().printStackTrace(getStackType(), this);
	}
	
	private static final FIFORunnableQueue<Runnable> EFFECT_QUEUE = new FIFORunnableQueue<Runnable>() {};
	
	private final Runnable ON_START = new Runnable() {
		public void run()
		{
			_startConditionsCorrect = onStart();
			
			if (_startConditionsCorrect)
			{
				_effected.addStatFuncs(getStatFuncs());
				_effected.startAbnormalEffect(_template.abnormalEffect | getTypeBasedAbnormalEffect());
			}
			
			if (_effected instanceof L2Playable)
				((L2Playable)_effected).updateEffectIcons();
		}
	};
	
	private final Runnable ON_EXIT = new Runnable() {
		public void run()
		{
			if (_startConditionsCorrect)
			{
				onExit();
				
				_effected.removeStatsOwner(L2Effect.this);
				_effected.stopAbnormalEffect(_template.abnormalEffect | getTypeBasedAbnormalEffect());
			}
			
			if (_effected instanceof L2Playable)
				((L2Playable)_effected).updateEffectIcons();
		}
	};
	
	private final boolean isActing()
	{
		return _isActing;
	}
	
	private final void setActing(boolean isActing)
	{
		_isActing = isActing;
	}
	
	public final String getStackType()
	{
		return _template.stackType;
	}
	
	public final float getStackOrder()
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
	
	public final boolean isSelfEffect()
	{
		return _skill._effectTemplatesSelf != null;
	}
	
	public final boolean isHerbEffect()
	{
		return _skill.isHerbEffect();
	}
	
	public final double calc()
	{
		return _template.lambda;
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
		if (_effected.getEffects().removeEffect(this))
		{
			synchronized (this)
			{
				if (isActing())
				{
					if (_count == 0 && _effected instanceof L2PcInstance)
					{
						// TODO: move to the proper places
						//if (effect.getSkill().isToggle())
						//	sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED);
						//else
						//	sm = new SystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED);
						
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
						sm.addSkillName(this);
						_effected.sendPacket(sm);
					}
					
					_currentFuture.cancel(false);
					
					setActing(false);
				}
			}
		}
	}
	
	protected int getTypeBasedAbnormalEffect()
	{
		return 0;
	}
	
	/** returns effect type */
	public abstract L2EffectType getEffectType();
	
	/** Notify started */
	protected boolean onStart()
	{
		return true;
	}
	
	/** Return true for continueation of this effect */
	protected boolean onActionTime()
	{
		return false;
	}
	
	/**
	 * Cancel the effect in the the abnormal effect map of the effected L2Character.<BR>
	 * <BR>
	 */
	protected void onExit()
	{
	}
	
	public final void run()
	{
		boolean shouldAct = false;
		
		synchronized (this)
		{
			if (_periodfirsttime == 0)
				_periodStartTicks = GameTimeController.getGameTicks();
			else
				_periodfirsttime = 0;
			
			if (isActing())
			{
				if (_count-- > 0)
				{
					if (_inUse)
						shouldAct = true;
					
					else if (_count > 0)
						return;
				}
			}
		}
		
		if (shouldAct && _startConditionsCorrect && onActionTime())
			return; // false causes effect to finish right away
		
		exit();
	}
	
	public final List<Func> getStatFuncs()
	{
		if (_template.funcTemplates == null)
			return L2Collections.emptyList();
		
		List<Func> funcs = new ArrayList<Func>(_template.funcTemplates.length);
		
		for (FuncTemplate t : _template.funcTemplates)
		{
			Env env = new Env();
			env.player = _effector;
			env.target = _effected;
			env.skill = _skill;
			
			Func f = t.getFunc(env, this);
			if (f != null)
				funcs.add(f);
		}
		
		return funcs;
	}
	
	public final void addPacket(EffectInfoPacketList list)
	{
		if (!_inUse || !getShowIcon())
			return;
		
		if (!isActing())
			return;
		
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
		
		final ScheduledFuture<?> future = _currentFuture;
		if (future == null)
			return;
		
		int time;
		
		if (_count > 1)
			time = getRemainingTaskTime() * 1000;
		else
			time = (int)future.getDelay(TimeUnit.MILLISECONDS);
		
		time = (time < 0 ? -1 : time / 1000);
		
		list.addEffect(_skill.getDisplayId(), _skill.getLevel(), time);
	}
	
	public final int getId()
	{
		return _skill.getId();
	}
	
	public final int getLevel()
	{
		return _skill.getLevel();
	}
	
	public final EffectTemplate getEffectTemplate()
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
		if (_skill.isToggle())
			return false;
		
		if (isHerbEffect())
			return false;
		
		return true;
	}
	
	public final boolean isBuff()
	{
		return _skill.isBuff();
	}
}
