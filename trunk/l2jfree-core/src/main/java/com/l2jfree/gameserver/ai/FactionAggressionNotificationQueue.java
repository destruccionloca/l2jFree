/**
 * 
 */
package com.l2jfree.gameserver.ai;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;

import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.threadmanager.FIFOExecutableQueue;
import com.l2jfree.util.ObjectPool;

/**
 * @author NB4L1
 */
public final class FactionAggressionNotificationQueue extends FIFOExecutableQueue
{
	private static final class NotificationInfo
	{
		private static final ObjectPool<NotificationInfo> POOL = new ObjectPool<NotificationInfo>(1000) {
			@Override
			protected NotificationInfo create()
			{
				return new NotificationInfo();
			}
		};
		
		private static NotificationInfo newInstance(Integer uid, L2NpcInstance npc, L2Character target)
		{
			final NotificationInfo info = POOL.get();
			
			info._uid = uid;
			info._npc = npc;
			info._target = target;
			
			return info;
		}
		
		private static void recycle(NotificationInfo info)
		{
			info._target = null;
			info._npc = null;
			info._uid = null;
			
			POOL.store(info);
		}
		
		private Integer _uid;
		private L2NpcInstance _npc;
		private L2Character _target;
		
		@Override
		public boolean equals(Object obj)
		{
			return obj instanceof NotificationInfo && ((NotificationInfo)obj)._uid == _uid;
		}
		
		@Override
		public int hashCode()
		{
			return _uid;
		}
		
		private static final int getUID(L2NpcInstance npc, L2Character target)
		{
			return (npc.getObjectId() << 2) + target.getObjectId();
		}
	}
	
	private final FastMap<Integer, NotificationInfo> _map = new FastMap<Integer, NotificationInfo>();
	
	public void add(L2NpcInstance npc, L2Character target)
	{
		final Integer uid = NotificationInfo.getUID(npc, target);
		
		synchronized (_map)
		{
			if (!_map.containsKey(uid))
				_map.put(uid, NotificationInfo.newInstance(uid, npc, target));
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
		L2NpcInstance npc = null;
		L2Character target = null;
		
		synchronized (_map)
		{
			Entry<Integer, NotificationInfo> first = _map.head().getNext();
			
			NotificationInfo info = first.getValue();
			
			npc = info._npc;
			target = info._target;
			
			_map.remove(first.getKey());
			
			NotificationInfo.recycle(info);
		}
		
		switch (npc.getAI().getIntention())
		{
			case AI_INTENTION_IDLE:
			case AI_INTENTION_ACTIVE:
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 1);
		}
	}
}
