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
package com.l2jfree.gameserver.taskmanager;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;

import com.l2jfree.Config;
import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.ai.CtrlEvent;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.instance.L2BoatInstance;

/**
 * @author NB4L1
 */
@SuppressWarnings("unused")
public final class MovementController extends AbstractPeriodicTaskManager
{
	private static final class SingletonHolder
	{
		private static final MovementController INSTANCE = new MovementController();
	}
	
	public static MovementController getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static final class TickRange
	{
		private int begin;
		private final int end;
		
		private TickRange(int ticks)
		{
			begin = GameTimeController.getGameTicks();
			end = begin + ticks;
		}
	}
	
	private final FastMap<L2Character, TickRange> _movingChars = new FastMap<L2Character, TickRange>().setShared(true);
	
	private final EvtArrivedManager _evtArrivedManager = new EvtArrivedManager();
	private final EvtArrivedRevalidateManager _evtArrivedRevalidateManager = new EvtArrivedRevalidateManager();
	
	private MovementController()
	{
		super(GameTimeController.MILLIS_IN_TICK);
	}
	
	public void add(L2Character cha, int ticks)
	{
		_movingChars.put(cha, new TickRange(ticks));
	}
	
	@Override
	public void run()
	{
		for (Entry<L2Character, TickRange> entry = _movingChars.head(), end = _movingChars.tail();
				(entry = entry.getNext()) != end;)
		{
			L2Character cha = entry.getKey();
			TickRange range = entry.getValue();
			int remaining = range.end - GameTimeController.getGameTicks();
			
			if (remaining > Config.DATETIME_MOVE_DELAY && remaining % Config.DATETIME_MOVE_DELAY != 0)
				continue;
			
			if (!cha.updatePosition(GameTimeController.getGameTicks()))
				continue;
			
			_movingChars.remove(cha);
			_evtArrivedManager.add(cha);
		}
	}
	
	private final class EvtArrivedManager extends AbstractFIFOPeriodicTaskManager<L2Character>
	{
		private EvtArrivedManager()
		{
			super(GameTimeController.MILLIS_IN_TICK);
		}
		
		@Override
		protected void callTask(L2Character cha)
		{
			cha.getKnownList().updateKnownObjects();
			
			if (cha.hasAI())
				cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
		}
		
		@Override
		protected String getCalledMethodName()
		{
			return "notifyEvent(CtrlEvent.EVT_ARRIVED)";
		}
	}
	
	private final class EvtArrivedRevalidateManager extends AbstractPeriodicTaskManager
	{
		private EvtArrivedRevalidateManager()
		{
			super(GameTimeController.MILLIS_IN_TICK);
		}
		
		@Override
		public void run()
		{
			for (Entry<L2Character, TickRange> entry = _movingChars.head();
					(entry = entry.getNext()) != _movingChars.tail();)
			{
				L2Character cha = entry.getKey();
				TickRange range = entry.getValue();
				int minDelay = getMinDelayInTick(cha);
				
				if (minDelay < 0)
					continue;
				
				if (GameTimeController.getGameTicks() >= range.end - minDelay)
					continue;
				
				if (GameTimeController.getGameTicks() <= range.begin + minDelay)
					continue;
				
				cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_REVALIDATE);
				
				range.begin = GameTimeController.getGameTicks();
			}
		}
		
		private int getMinDelayInTick(L2Character cha)
		{
			if (cha instanceof L2Playable)
				return Config.DATETIME_MOVE_DELAY * 2;
			
			if (cha instanceof L2BoatInstance)
				return -1;
			
			return Config.DATETIME_MOVE_DELAY * 4;
		}
	}
}
