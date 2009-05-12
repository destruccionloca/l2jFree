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

import java.util.Iterator;
import java.util.List;

import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.effects.CharEffects;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.util.LinkedBunch;

public class CharEffectList extends CharEffects
{
	private FastList<L2Effect>				_buffs;
	private FastList<L2Effect>				_debuffs;

	public CharEffectList(L2Character owner)
	{
		super(owner);
	}

	/**
	 * Returns all effects affecting stored in this CharEffectList
	 * @return
	 */
	@Override
	public final L2Effect[] getAllEffects()
	{
		// If no effect is active, return EMPTY_EFFECTS
		if ((_buffs == null || _buffs.isEmpty()) && (_debuffs == null || _debuffs.isEmpty()))
		{
			return L2Effect.EMPTY_ARRAY;
		}

		// Create a copy of the effects
		LinkedBunch<L2Effect> temp = new LinkedBunch<L2Effect>();

		synchronized (this)
		{
			// Add all buffs and all debuffs
			if (_buffs != null && !_buffs.isEmpty())
				temp.addAll(_buffs);
			if (_debuffs != null && !_debuffs.isEmpty())
				temp.addAll(_debuffs);
		}

		// Return all effects in an array
		return temp.moveToArray(new L2Effect[temp.size()]);
	}

	/**
	 * Removes the first buff of this list.
	 *
	 * @param s Is the skill that is being applied.
	 */
	private void removeFirstBuff(L2Skill checkSkill)
	{
		boolean danceBuff = false;
		if (!checkSkill.isDance() && !checkSkill.isSong() && getBuffCount() >= _owner.getMaxBuffCount())
		{
			switch (checkSkill.getSkillType())
			{
				case BUFF:
				case REFLECT:
				case HEAL_PERCENT:
				case MANAHEAL_PERCENT:
					break;
				default:
					return;
			}
		}
		else if ((checkSkill.isDance() || checkSkill.isSong()) && getDanceCount(true, true) >= Config.ALT_DANCES_SONGS_MAX_AMOUNT)
		{
			danceBuff = true;
		}
		else
			return;

		L2Effect[] effects = getAllEffects();
		L2Effect removeMe = null;

		for (L2Effect e : effects)
		{
			if (e == null)
				continue;

			if (e.getSkill().bestowed())
				continue;

			switch (e.getSkill().getSkillType())
			{
			case BUFF:
			case DEBUFF:
			case REFLECT:
			case HEAL_PERCENT:
			case MANAHEAL_PERCENT:
				break;
			default:
				continue;
			}

			if ((danceBuff && (e.getSkill().isDance() || e.getSkill().isSong())) || (!danceBuff && !e.getSkill().isDance() && !e.getSkill().isSong()))
			{
				if (e.getSkill().getId() == checkSkill.getId())
				{
					removeMe = e;
					break;
				}
				else if (removeMe == null)
					removeMe = e;
			}
		}
		if (removeMe != null)
			removeMe.exit();
	}

	public final void removeEffect(L2Effect effect)
	{
		if (effect == null || (_buffs == null && _debuffs == null))
			return;

		FastList<L2Effect> effectList = effect.getSkill().isDebuff() ? _debuffs : _buffs;

		synchronized (effectList)
		{
			if (_stackedEffects == null)
				return;

			// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
			List<L2Effect> stackQueue = _stackedEffects.get(effect.getStackType());

			if (stackQueue == null || stackQueue.size() < 1)
				return;

			// Get the identifier of the first stacked effect of the stack group selected
			L2Effect frontEffect = stackQueue.get(0);

			// Remove the effect from the stack group
			boolean removed = stackQueue.remove(effect);

			if (removed)
			{
				// Check if the first stacked effect was the effect to remove
				if (frontEffect == effect)
				{
					// Remove all its Func objects from the L2Character calculator set
					_owner.removeStatsOwner(effect);

					// Check if there's another effect in the Stack Group
					if (!stackQueue.isEmpty())
					{
						// Add its list of Funcs to the Calculator set of the L2Character
						for (L2Effect e : effectList)
						{
							if (e == stackQueue.get(0))
							{
								// Add its list of Funcs to the Calculator set of the L2Character
								_owner.addStatFuncs(e.getStatFuncs());
								// Set the effect to In Use
								e.setInUse(true);
								break;
							}
						}
					}
				}
				if (stackQueue.isEmpty())
					_stackedEffects.remove(effect.getStackType());
				else
					// Update the Stack Group table _stackedEffects of the L2Character
					_stackedEffects.put(effect.getStackType(), stackQueue);
			}

			// Remove the active skill L2effect from _effects of the L2Character
			// The Integer key of _effects is the L2Skill Identifier that has created the effect
			for (L2Effect e : effectList)
			{
				if (e == effect)
				{
					effectList.remove(e);
					if (_owner instanceof L2PcInstance)
					{
						SystemMessage sm;
						if (effect.getSkill().isToggle())
							sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED);
						else
							sm = new SystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED);
						sm.addSkillName(effect);
						_owner.sendPacket(sm);
					}
					break;
				}
			}

		}
	}

	public void addEffect(L2Effect newEffect)
	{
		if (newEffect == null)
			return;

		synchronized (this)
		{
			if (_buffs == null)
				_buffs = new FastList<L2Effect>();
			if (_debuffs == null)
				_debuffs = new FastList<L2Effect>();
			if (_stackedEffects == null)
				_stackedEffects = new FastMap<String, List<L2Effect>>();
		}

		FastList<L2Effect> effectList = newEffect.getSkill().isDebuff() ? _debuffs : _buffs;
		L2Effect tempEffect = null;
		boolean stopNewEffect = false;
		
		synchronized (effectList)
		{
			// Check for same effects
			for (L2Effect e : effectList)
			{
				if (e == null)
					continue;

				if (e.getSkill().getId() == newEffect.getSkill().getId() && e.getEffectType() == newEffect.getEffectType()
						&& e.getStackOrder() == newEffect.getStackOrder())
				{
					if (!newEffect.getSkill().isDebuff())
					{
						tempEffect = e; // exit this
						break;
					}

					// Started scheduled timer needs to be canceled.
					stopNewEffect = true;
					break;
				}
			}
		}

		if (tempEffect != null)
			tempEffect.exit();

		// if max buffs, no herb effects are used, even if they would replace one old
		if (stopNewEffect || (getBuffCount() >= _owner.getMaxBuffCount() && newEffect.isHerbEffect()))
		{
			newEffect.stopEffectTask();
			return;
		}

		// Remove first buff when buff list is full
		L2Skill tempSkill = newEffect.getSkill();
		if (!_stackedEffects.containsKey(newEffect.getStackType()) && !tempSkill.isDebuff() && !tempSkill.bestowed() && !(tempSkill.getId() > 4360 && tempSkill.getId() < 4367))
		{
			removeFirstBuff(tempSkill);
		}

		synchronized(effectList)
		{
			// Add the L2Effect to all effect in progress on the L2Character
			if (!newEffect.getSkill().isToggle() && !newEffect.getSkill().isDebuff())
			{
				int pos = 0;
				for (L2Effect e : effectList)
				{
					if (e != null)
					{
						int skillid = e.getSkill().getId();
						if (!e.getSkill().isToggle() && (!(skillid > 4360 && skillid < 4367)))
							pos++;
					}
					else
						break;
				}
				effectList.add(pos, newEffect);
			}
			else
				effectList.addLast(newEffect);
		}

		// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
		List<L2Effect> stackQueue = _stackedEffects.get(newEffect.getStackType());
		if (stackQueue == null)
			stackQueue = new FastList<L2Effect>();

		L2Effect[] allEffects = getAllEffects();

		tempEffect = null;
		if (!stackQueue.isEmpty())
		{
			// Get the first stacked effect of the Stack group selected
			for (L2Effect e : allEffects)
			{
				if (e == stackQueue.get(0))
				{
					tempEffect = e;
					break;
				}
			}
		}

		// Add the new effect to the stack group selected at its position
		stackQueue = effectQueueInsert(newEffect, stackQueue);

		if (stackQueue == null)
			return;

		// Update the Stack Group table _stackedEffects of the L2Character
		_stackedEffects.put(newEffect.getStackType(), stackQueue);

		// Get the first stacked effect of the Stack group selected
		L2Effect tempEffect2 = null;
		for (L2Effect e : allEffects)
		{
			if (e == stackQueue.get(0))
			{
				tempEffect2 = e;
				break;
			}
		}

		if (tempEffect != tempEffect2)
		{
			if (tempEffect != null)
			{
				// Remove all Func objects corresponding to this stacked effect from the Calculator set of the L2Character
				_owner.removeStatsOwner(tempEffect);

				// Set the L2Effect to Not In Use
				tempEffect.setInUse(false);
			}
			if (tempEffect2 != null)
			{
				// Set this L2Effect to In Use
				tempEffect2.setInUse(true);

				// Add all Func objects corresponding to this stacked effect to the Calculator set of the L2Character
				_owner.addStatFuncs(tempEffect2.getStatFuncs());
			}
		}
	}

	/**
	 * Insert an effect at the specified position in a Stack Group.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWalk and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 *
	 * @param id The identifier of the stacked effect to add to the Stack Group
	 * @param stackOrder The position of the effect in the Stack Group
	 * @param stackQueue The Stack Group in which the effect must be added
	 *
	 */
	private List<L2Effect> effectQueueInsert(L2Effect newStackedEffect, List<L2Effect> stackQueue)
	{
		FastList<L2Effect> effectList = newStackedEffect.getSkill().isDebuff() ? _debuffs : _buffs;

		// Get the L2Effect corresponding to the effect identifier from the L2Character _effects
		if (_buffs == null && _debuffs == null)
			return null;

		// Create an Iterator to go through the list of stacked effects in progress on the L2Character
		Iterator<L2Effect> queueIterator = stackQueue.iterator();

		int i = 0;
		while (queueIterator.hasNext())
		{
			L2Effect cur = queueIterator.next();
			if (newStackedEffect.getStackOrder() < cur.getStackOrder())
				i++;
			else
				break;
		}

		// Add the new effect to the Stack list in function of its position in the Stack group
		stackQueue.add(i, newStackedEffect);

		// skill.exit() could be used, if the users don't wish to see "effect
		// removed" always when a timer goes off, even if the buff isn't active
		// any more (has been replaced). but then check e.g. npc hold and raid petrification.
		if (Config.EFFECT_CANCELING && !newStackedEffect.isHerbEffect() && stackQueue.size() > 1)
		{
			// only keep the current effect, cancel other effects
			for (L2Effect e : effectList)
			{
				if (e == stackQueue.get(1))
				{
					effectList.remove(e);
					break;
				}
			}
			stackQueue.remove(1);
		}

		return stackQueue;
	}
}
