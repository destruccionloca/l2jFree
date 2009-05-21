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
package com.l2jfree.gameserver.ai;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;

import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2SiegeGuard;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.threadmanager.FIFOExecutableQueue;

/**
 * @author mkizub
 */
public class L2DoorAI extends L2CharacterAI
{
	
	public L2DoorAI(L2DoorInstance.AIAccessor accessor)
	{
		super(accessor);
	}
	
	// rather stupid AI... well, it's for doors :D
	@Override
	protected void onIntentionIdle()
	{
	}
	
	@Override
	protected void onIntentionActive()
	{
	}
	
	@Override
	protected void onIntentionRest()
	{
	}
	
	@Override
	protected void onIntentionAttack(L2Character target)
	{
	}
	
	@Override
	protected void onIntentionCast(L2Skill skill, L2Object target)
	{
	}
	
	@Override
	protected void onIntentionMoveTo(L2CharPosition destination)
	{
	}
	
	@Override
	protected void onIntentionFollow(L2Character target)
	{
	}
	
	@Override
	protected void onIntentionPickUp(L2Object item)
	{
	}
	
	@Override
	protected void onIntentionInteract(L2Object object)
	{
	}
	
	@Override
	protected void onEvtThink()
	{
	}
	
	private GuardNotificationQueue _guardNotificationTasks;
	
	@Override
	protected void onEvtAttacked(L2Character attacker)
	{
		if (_guardNotificationTasks == null)
			_guardNotificationTasks = new GuardNotificationQueue();
		
		_guardNotificationTasks.add(attacker);
	}
	
	@Override
	protected void onEvtAggression(L2Character target, int aggro)
	{
	}
	
	@Override
	protected void onEvtStunned(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtSleeping(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtRooted(L2Character attacker)
	{
	}
	
	@Override
	protected void onEvtReadyToAct()
	{
	}
	
	@Override
	protected void onEvtUserCmd(Object arg0, Object arg1)
	{
	}
	
	@Override
	protected void onEvtArrived()
	{
	}
	
	@Override
	protected void onEvtArrivedRevalidate()
	{
	}
	
	@Override
	protected void onEvtArrivedBlocked(L2CharPosition blocked_at_pos)
	{
	}
	
	@Override
	protected void onEvtForgetObject(L2Object object)
	{
	}
	
	@Override
	protected void onEvtCancel()
	{
	}
	
	@Override
	protected void onEvtDead()
	{
	}
	
	private final class GuardNotificationQueue extends FIFOExecutableQueue
	{
		private final FastMap<L2Character, Integer> _map = new FastMap<L2Character, Integer>();
		
		private void add(L2Character attacker)
		{
			synchronized (_map)
			{
				Entry<L2Character, Integer> entry = _map.getEntry(attacker);
				
				if (entry != null)
					entry.setValue(entry.getValue() + 15);
				else
					_map.put(attacker, 15);
			}
			
			execute();
		}
		
		@Override
		protected boolean isEmpty()
		{
			synchronized (_map)
			{
				return _map.isEmpty();
			}
		}
		
		@Override
		protected void removeAndExecuteFirst()
		{
			L2Character attacker = null;
			int aggro = 0;
			
			synchronized (_map)
			{
				Entry<L2Character, Integer> first = _map.head().getNext();
				
				attacker = first.getKey();
				aggro = first.getValue();
				
				_map.remove(attacker);
			}
			
			getActor().getKnownList().updateKnownObjects();
			
			for (L2Object obj : getActor().getKnownList().getKnownObjects().values())
			{
				if (obj instanceof L2SiegeGuard)
				{
					L2SiegeGuard guard = (L2SiegeGuard)obj;
					
					if (Math.abs(attacker.getZ() - guard.getZ()) < 200)
						if (getActor().isInsideRadius(guard, guard.getFactionRange(), false, true))
							guard.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, aggro);
				}
			}
		}
	}
}
