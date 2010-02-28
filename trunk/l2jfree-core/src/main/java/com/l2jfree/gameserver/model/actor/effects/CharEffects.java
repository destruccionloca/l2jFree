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
package com.l2jfree.gameserver.model.actor.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javolution.text.TextBuilder;
import javolution.util.FastMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.network.serverpackets.EffectInfoPacket.EffectInfoPacketList;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.util.ObjectPool;

/**
 * @author NB4L1
 */
public final class CharEffects
{
	private static final Log _log = LogFactory.getLog(CharEffects.class);
	
	private final L2Character _owner;
	
	private L2Effect[] _toArray;
	private List<L2Effect> _effects;
	private Map<String, StackQueue> _stackedEffects;
	
	public CharEffects(L2Character owner)
	{
		_owner = owner;
	}
	
	private boolean isEmpty()
	{
		return _effects == null || _effects.isEmpty();
	}
	
	private static boolean isActiveBuff(L2Effect e)
	{
		if (e == null)
			return false;
		
		if (!e.getSkill().isBuff() || e.getSkill().isDebuff())
			return false;
		
		if (e.getSkill().isDanceOrSong())
			return false;
		
		switch (e.getEffectType())
		{
			case TRANSFORMATION:
			case ENVIRONMENT:
				return false;
		}
		
		return e.isInUse() && e.getShowIcon();
	}
	
	private static boolean isActiveDance(L2Effect e, boolean isDance, boolean isSong)
	{
		return e != null && e.isInUse() && (isDance && e.getSkill().isDance() || isSong && e.getSkill().isSong());
	}
	
	public synchronized L2Effect[] getAllEffects()
	{
		if (isEmpty())
			return L2Effect.EMPTY_ARRAY;
		
		if (_toArray == null)
			_toArray = _effects.toArray(new L2Effect[_effects.size()]);
		
		return _toArray;
	}
	
	public synchronized L2Effect[] getAllEffects(String stackType)
	{
		if (isEmpty())
			return L2Effect.EMPTY_ARRAY;
		
		final StackQueue queue = _stackedEffects.get(stackType);
		
		if (queue == null)
			return L2Effect.EMPTY_ARRAY;
		
		return queue.getAllEffects();
	}
	
	public synchronized void addEffect(L2Effect newEffect)
	{
		if (_effects == null)
			_effects = new ArrayList<L2Effect>(4);
		
		if (_stackedEffects == null)
			_stackedEffects = new FastMap<String, StackQueue>(4);
		
		final int newOrder = getOrder(newEffect);
		
		int index;
		for (index = 0; index < _effects.size(); index++)
		{
			final L2Effect e = _effects.get(index);
			
			if (getOrder(e) > newOrder)
				break;
		}
		
		_effects.add(index, newEffect);
		_toArray = null;
		
		for (String stackType : newEffect.getStackTypes())
		{
			StackQueue stackQueue = _stackedEffects.get(stackType);
			
			if (stackQueue == null)
				stackQueue = StackQueue.newInstance(this, stackType);
			
			stackQueue.add(newEffect);
		}
		
		if (isActiveDance(newEffect, true, true))
		{
			if (getDanceCount(true, true) > Config.ALT_DANCES_SONGS_MAX_AMOUNT)
			{
				for (int i = 0; i < _effects.size(); i++)
				{
					final L2Effect e = _effects.get(i);
					
					if (isActiveDance(e, true, true))
					{
						stopStackedEffects(e);
						return;
					}
				}
			}
		}
		else if (isActiveBuff(newEffect))
		{
			if (getBuffCount() > _owner.getMaxBuffCount())
			{
				for (int i = 0; i < _effects.size(); i++)
				{
					final L2Effect e = _effects.get(i);
					
					if (isActiveBuff(e))
					{
						stopStackedEffects(e);
						return;
					}
				}
			}
		}
	}
	
	private static int getOrder(L2Effect e)
	{
		if (e.getSkill().isToggle())
			return 3;
		
		if (e.getSkill().isOffensive())
			return 2;
		
		if (e.getSkill().isDanceOrSong())
			return 1;
		
		return 0;
	}
	
	public synchronized boolean removeEffect(L2Effect effect)
	{
		final int index = _effects.indexOf(effect);
		if (index < 0)
			return false;
		
		for (String stackType : effect.getStackTypes())
		{
			StackQueue queue = _stackedEffects.get(stackType);
			
			if (queue != null)
				queue.remove(effect);
		}
		_effects.remove(index);
		_toArray = null;
		
		return true;
	}
	
	private static final class StackQueue
	{
		private static final ObjectPool<StackQueue> POOL = new ObjectPool<StackQueue>() {
			@Override
			protected StackQueue create()
			{
				return new StackQueue();
			}
		};
		
		private static StackQueue newInstance(CharEffects effects, String stackType)
		{
			StackQueue stackQueue = POOL.get();
			
			stackQueue._queue.clear();
			stackQueue._effects = effects;
			stackQueue._stackType = stackType;
			stackQueue._effects._stackedEffects.put(stackQueue._stackType, stackQueue);
			
			return stackQueue;
		}
		
		private static void recycle(StackQueue stackQueue)
		{
			stackQueue._effects._stackedEffects.remove(stackQueue._stackType);
			stackQueue._stackType = null;
			stackQueue._effects = null;
			stackQueue._queue.clear();
			
			POOL.store(stackQueue);
		}
		
		private final ArrayList<L2Effect> _queue = new ArrayList<L2Effect>(4);
		private CharEffects _effects;
		private String _stackType;
		
		private synchronized L2Effect[] getAllEffects()
		{
			return _queue.toArray(new L2Effect[_queue.size()]);
		}
		
		private synchronized void add(final L2Effect effect)
		{
			int index;
			for (index = 0; index < _queue.size(); index++)
			{
				final L2Effect e = _queue.get(index);
				
				if (effect.getStackOrder() >= e.getStackOrder())
					break;
			}
			
			if (index == 0 && !_queue.isEmpty())
				_queue.get(0).setInUse(false);
			
			_queue.add(index, effect);
			
			if (index == 0)
				_queue.get(0).setInUse(true);
			
			// TODO: Config.EFFECT_CANCELING
		}
		
		private synchronized void remove(final L2Effect effect)
		{
			final int index = _queue.indexOf(effect);
			
			if (index == 0)
				_queue.get(0).setInUse(false);
			
			_queue.remove(index);
			
			if (index == 0 && !_queue.isEmpty())
				_queue.get(0).setInUse(true);
			
			if (_queue.isEmpty())
				StackQueue.recycle(this);
		}
		
		private synchronized L2Effect getFirstEffect()
		{
			if (_queue.isEmpty())
				return null;
			
			return _queue.get(0);
		}
		
		private synchronized void stopAllEffects()
		{
			while (!_queue.isEmpty())
				_queue.get(_queue.size() - 1).exit();
		}
	}
	
	/**
	 * For debugging purpose...
	 */
	public void printStackTrace(String[] stackTypes, L2Effect effect)
	{
		TextBuilder tb = TextBuilder.newInstance();
		
		tb.append(_owner);
		
		if (stackTypes != null)
			tb.append(" -> ").append(StringUtils.join(stackTypes, ";"));
		
		if (effect != null)
			tb.append(" -> ").append(effect.getSkill().toString());
		
		_log.warn(tb, new IllegalStateException());
		
		TextBuilder.recycle(tb);
	}
	
	public synchronized L2Effect getFirstEffect(L2Skill skill)
	{
		return getFirstEffect(skill.getId());
	}
	
	public synchronized boolean hasEffect(L2Skill skill)
	{
		return hasEffect(skill.getId());
	}
	
	public synchronized void stopEffects(L2Skill skill)
	{
		stopEffects(skill.getId());
	}
	
	public synchronized L2Effect getFirstEffect(int id)
	{
		return getFirstEffect(id, true);
	}
	
	public synchronized boolean hasEffect(int id)
	{
		return getFirstEffect(id, false) != null;
	}
	
	public synchronized void stopEffects(int id)
	{
		for (L2Effect e; (e = getFirstEffect(id, false)) != null;)
			e.exit();
	}
	
	private L2Effect getFirstEffect(int id, boolean searchForInUse)
	{
		if (isEmpty())
			return null;
		
		L2Effect notUsedEffect = null;
		
		for (int i = 0; i < _effects.size(); i++)
		{
			final L2Effect e = _effects.get(i);
			
			if (e.getSkill().getId() == id)
			{
				if (e.isInUse() || !searchForInUse)
					return e;
				else if (notUsedEffect == null)
					notUsedEffect = e;
			}
		}
		
		return notUsedEffect;
	}
	
	public synchronized L2Effect getFirstEffect(L2EffectType tp)
	{
		return getFirstEffect(tp, true);
	}
	
	public synchronized boolean hasEffect(L2EffectType tp)
	{
		return getFirstEffect(tp, false) != null;
	}
	
	public synchronized void stopEffects(L2EffectType tp)
	{
		for (L2Effect e; (e = getFirstEffect(tp, false)) != null;)
			e.exit();
	}
	
	private L2Effect getFirstEffect(L2EffectType tp, boolean searchForInUse)
	{
		if (isEmpty())
			return null;
		
		L2Effect notUsedEffect = null;
		
		for (int i = 0; i < _effects.size(); i++)
		{
			final L2Effect e = _effects.get(i);
			
			if (e.getEffectType() == tp)
			{
				if (e.isInUse() || !searchForInUse)
					return e;
				else if (notUsedEffect == null)
					notUsedEffect = e;
			}
		}
		
		return notUsedEffect;
	}
	
	public synchronized int getBuffCount()
	{
		if (isEmpty())
			return 0;
		
		int buffCount = 0;
		
		for (int i = 0; i < _effects.size(); i++)
		{
			final L2Effect e = _effects.get(i);
			
			if (isActiveBuff(e))
				buffCount++;
		}
		
		return buffCount;
	}
	
	public synchronized int getDanceCount(boolean dances, boolean songs)
	{
		if (isEmpty())
			return 0;
		
		int danceCount = 0;
		
		for (int i = 0; i < _effects.size(); i++)
		{
			final L2Effect e = _effects.get(i);
			
			if (isActiveDance(e, dances, songs))
				danceCount++;
		}
		
		return danceCount;
	}
	
	public synchronized void addPacket(EffectInfoPacketList list)
	{
		if (isEmpty())
			return;
		
		for (int i = 0; i < _effects.size(); i++)
		{
			final L2Effect e = _effects.get(i);
			
			e.addPacket(list);
		}
	}
	
	/**
	 * Exits all effects in this CharEffectList
	 */
	public void stopAllEffects()
	{
		stopAllEffects(false);
	}
	
	public synchronized void stopAllEffects(boolean stopEffectsThatLastThroughDeathToo)
	{
		if (isEmpty())
			return;
		
		if (stopEffectsThatLastThroughDeathToo)
		{
			while (!_effects.isEmpty())
			{
				stopStackedEffects(_effects.get(_effects.size() - 1));
			}
		}
		else
		{
			for (int index = _effects.size() - 1; index >= 0; index = Math.min(index - 1, _effects.size() - 1))
			{
				final L2Effect e = _effects.get(index);
				
				if (e.isStayAfterDeath())
					continue;
				
				e.exit();
			}
		}
	}
	
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		stopAllEffects(false);
	}
	
	public synchronized void dispelBuff(int skillId, int skillLvl)
	{
		for (L2Effect e : getAllEffects())
		{
			if (e == null)
				continue;
			
			if (e.getId() != skillId || e.getLevel() != skillLvl)
				continue;
			
			if (!isActiveBuff(e) && (!Config.DANCE_CANCEL_BUFF || !isActiveDance(e, true, true)))
				continue;
			
			if (e.getEffectType() == L2EffectType.TRANSFORMATION)
				continue;
			
			if (!e.getSkill().canBeDispeled())
				continue;
			
			stopStackedEffects(e);
			
			e.exit(); // just to be sure
		}
	}
	
	private void stopStackedEffects(L2Effect e)
	{
		for (String stackType : e.getStackTypes())
		{
			StackQueue queue = _stackedEffects.get(stackType);
			
			if (queue != null)
				queue.stopAllEffects();
		}
	}
	
	public synchronized L2Effect getFirstEffect(String stackType)
	{
		return getFirstEffect(stackType, true);
	}
	
	public synchronized L2Effect getFirstEffect(String[] stackTypes)
	{
		return getFirstEffect(stackTypes, true);
	}
	
	public synchronized boolean hasEffect(String stackType)
	{
		return getFirstEffect(stackType, false) != null;
	}
	
	public synchronized boolean hasEffect(String[] stackTypes)
	{
		return getFirstEffect(stackTypes, false) != null;
	}
	
	public synchronized void stopEffects(String stackType)
	{
		if (isEmpty())
			return;
		
		final StackQueue queue = _stackedEffects.get(stackType);
		
		if (queue != null)
			queue.stopAllEffects();
	}
	
	public synchronized void stopEffects(String[] stackTypes)
	{
		if (isEmpty())
			return;
		
		for (String stackType : stackTypes)
		{
			final StackQueue queue = _stackedEffects.get(stackType);
			
			if (queue != null)
				queue.stopAllEffects();
		}
	}
	
	private L2Effect getFirstEffect(String stackType, boolean searchForInUse)
	{
		if (isEmpty())
			return null;
		
		final StackQueue queue = _stackedEffects.get(stackType);
		
		if (queue == null)
			return null;
		
		return queue.getFirstEffect();
	}
	
	private L2Effect getFirstEffect(String[] stackTypes, boolean searchForInUse)
	{
		if (isEmpty())
			return null;
		
		for (String stackType : stackTypes)
		{
			final StackQueue queue = _stackedEffects.get(stackType);
			
			if (queue == null)
				continue;
			
			final L2Effect e = queue.getFirstEffect();
			
			if (e != null)
				return e;
		}
		
		return null;
	}
}
