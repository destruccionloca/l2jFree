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

import java.util.List;
import java.util.Map;

import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.skills.effects.EffectCharmOfCourage;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * @author NB4L1
 */
public class CharEffects
{
	protected final L2Character _owner;
	
	private L2Effect[] _toArray;
	private List<L2Effect> _effects;
	protected Map<String, List<L2Effect>/*StackQueue*/> _stackedEffects;
	
	public CharEffects(L2Character owner)
	{
		_owner = owner;
	}
	
	private boolean isEmpty()
	{
		return _effects == null || _effects.isEmpty();
	}
	
	public synchronized L2Effect[] getAllEffects()
	{
		if (isEmpty())
			return L2Effect.EMPTY_ARRAY;
		
		if (_toArray == null)
			_toArray = _effects.toArray(new L2Effect[_effects.size()]);
		
		return _toArray;
	}
	
	/*
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
		
		StackQueue stackQueue = _stackedEffects.get(newEffect.getStackType());
		
		if (stackQueue == null)
			stackQueue = StackQueue.newInstance(this, newEffect.getStackType());
		
		stackQueue.add(newEffect);
		
		if (newEffect.getSkill().isDance() || newEffect.getSkill().isSong())
		{
			while (getDanceCount(true, true) > Config.ALT_DANCES_SONGS_MAX_AMOUNT)
			{
				// remove first dance/song stackQueue
			}
		}
		else
		{
			while (getBuffCount() > _activeChar.getMaxBuffCount())
			{
				// remove first buff stackQueue
			}
		}
	}
	
	private static int getOrder(L2Effect e)
	{
		if (e.getSkill().isToggle())
			return 3;
		
		if (e.getSkill().isOffensive() || e.getSkill().isDebuff())
			return 2;
		
		return 1;
	}
	
	public synchronized int getBuffCount()
	{
		throw new UnsupportedOperationException();
	}
	
	public synchronized int getDanceCount(boolean dances, boolean songs)
	{
		throw new UnsupportedOperationException();
	}
	
	public synchronized boolean removeEffect(L2Effect effect)
	{
		final int index = _effects.indexOf(effect);
		if (index < 0)
			return false;
		
		_stackedEffects.get(effect.getStackType()).remove(effect);
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
		
		private void add(final L2Effect effect)
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
		}
		
		private void remove(final L2Effect effect)
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
		
		private void stopAllEffects()
		{
			for (int index = _queue.size() - 1; index >= 0; index--)
				_queue.get(index).exit();
		}
	}
	*/
	
	// TODO: rework the rest
	
	/**
	 * Returns the first effect matching the given EffectType
	 * 
	 * @param tp
	 * @return
	 */
	public final L2Effect getFirstEffect(L2EffectType tp)
	{
		L2Effect eventNotInUse = null;
		for (L2Effect e : getAllEffects())
		{
			if (e.getEffectType() == tp)
			{
				if (e.getInUse())
					return e;
				
				eventNotInUse = e;
			}
		}
		return eventNotInUse;
	}
	
	/**
	 * Returns the first effect matching the given L2Skill
	 * 
	 * @param skill
	 * @return
	 */
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		L2Effect eventNotInUse = null;
		for (L2Effect e : getAllEffects())
		{
			if (e.getSkill() == skill)
			{
				if (e.getInUse())
					return e;
				
				eventNotInUse = e;
			}
		}
		return eventNotInUse;
	}
	
	/**
	 * Returns the first effect matching the given skillId
	 * 
	 * @param index
	 * @return
	 */
	public final L2Effect getFirstEffect(int skillId)
	{
		L2Effect eventNotInUse = null;
		for (L2Effect e : getAllEffects())
		{
			if (e.getSkill().getId() == skillId)
			{
				if (e.getInUse())
					return e;
				
				eventNotInUse = e;
			}
		}
		return eventNotInUse;
	}
	
	/**
	 * Return the number of buffs in this CharEffectList not counting Songs/Dances
	 * 
	 * @return
	 */
	public int getBuffCount()
	{
		int buffCount = 0;
		
		for (L2Effect e : getAllEffects())
		{
			if (e != null
				&& e.getShowIcon()
				&& !e.getSkill().isDance()
				&& !e.getSkill().isSong()
				&& !e.getSkill().isDebuff()
				&& !e.getSkill().bestowed()
				&& (e.getSkill().getSkillType() == L2SkillType.BUFF
					|| e.getSkill().getSkillType() == L2SkillType.REFLECT
					|| e.getSkill().getSkillType() == L2SkillType.HEAL_PERCENT || e.getSkill().getSkillType() == L2SkillType.MANAHEAL_PERCENT)
				&& !(e.getSkill().getId() > 4360 && e.getSkill().getId() < 4367)) // Seven Signs buffs
			{
				buffCount++;
			}
		}
		return buffCount;
	}
	
	/**
	 * Return the number of Songs/Dances in this CharEffectList
	 * 
	 * @return
	 */
	public int getDanceCount(boolean dances, boolean songs)
	{
		int danceCount = 0;
		
		for (L2Effect e : getAllEffects())
		{
			if (e != null && ((e.getSkill().isDance() && dances) || (e.getSkill().isSong() && songs)) && e.getInUse())
				danceCount++;
		}
		return danceCount;
	}
	
	/**
	 * Exits all effects in this CharEffectList
	 */
	public final void stopAllEffects()
	{
		for (L2Effect e : getAllEffects())
		{
			if (e != null && e.getSkill().getId() != 5660)
			{
				e.exit();
			}
		}
	}
	
	/**
	 * Exits all effects in this CharEffectList
	 */
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		for (L2Effect e : getAllEffects())
		{
			if (e != null)
			{
				if (e instanceof EffectCharmOfCourage)
					continue;
				e.exit();
			}
		}
	}
	
	/**
	 * Exit all effects having a specified type
	 * 
	 * @param type
	 */
	public final void stopEffects(L2EffectType type)
	{
		for (L2Effect e : getAllEffects())
		{
			// Stop active skills effects of the selected type
			if (e.getEffectType() == type)
				e.exit();
		}
	}
	
	/**
	 * Exits all effects created by a specific skillId
	 * 
	 * @param skillId
	 */
	public final void stopSkillEffects(int skillId)
	{
		for (L2Effect e : getAllEffects())
		{
			if (e.getSkill().getId() == skillId)
				e.exit();
		}
	}
}
