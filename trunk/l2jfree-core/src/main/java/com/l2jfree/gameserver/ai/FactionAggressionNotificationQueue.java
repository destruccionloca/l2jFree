/**
 * 
 */
package com.l2jfree.gameserver.ai;

import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.threadmanager.FIFOExecutableQueue;
import com.l2jfree.util.L2FastSet;

/**
 * @author NB4L1
 */
public final class FactionAggressionNotificationQueue extends FIFOExecutableQueue
{
	private static final class NotificationInfo
	{
		private final L2Npc _npc;
		private final L2Character _target;
		
		private NotificationInfo(L2Npc npc, L2Character target)
		{
			_npc = npc;
			_target = target;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (!(obj instanceof NotificationInfo))
				return false;
			
			final NotificationInfo ni = (NotificationInfo)obj;
			
			return _npc == ni._npc && _target == ni._target;
		}
		
		@Override
		public int hashCode()
		{
			return (_npc.getObjectId() << 2) + _target.getObjectId();
		}
	}
	
	private final L2FastSet<NotificationInfo> _set = new L2FastSet<NotificationInfo>();
	
	public void add(L2Npc npc, L2Character target)
	{
		synchronized (_set)
		{
			_set.add(new NotificationInfo(npc, target));
		}
		
		execute();
	}
	
	@Override
	protected boolean isEmpty()
	{
		synchronized (_set)
		{
			return _set.isEmpty();
		}
	}
	
	@Override
	protected void removeAndExecuteFirst()
	{
		NotificationInfo ni = null;
		
		synchronized (_set)
		{
			ni = _set.removeFirst();
		}
		
		switch (ni._npc.getAI().getIntention())
		{
			case AI_INTENTION_IDLE:
			case AI_INTENTION_ACTIVE:
				ni._npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, ni._target, 1);
		}
	}
}
