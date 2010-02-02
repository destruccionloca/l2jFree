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

import java.util.ArrayList;

import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.ai.CtrlEvent;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.threadmanager.FIFOSimpleExecutableQueue;
import com.l2jfree.util.L2Collections;
import com.l2jfree.util.L2FastSet;
import com.l2jfree.util.concurrent.RunnableStatsManager;

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
	
	private final L2FastSet<L2Character> _movingChars = new L2FastSet<L2Character>().setShared(true);
	
	private final EvtArrivedManager _evtArrivedManager = new EvtArrivedManager();
	private final EvtArrivedRevalidateManager _evtArrivedRevalidateManager = new EvtArrivedRevalidateManager();
	
	private MovementController()
	{
		super(GameTimeController.MILLIS_IN_TICK);
	}
	
	public void add(L2Character cha, int ticks)
	{
		_movingChars.add(cha);
	}
	
	public void remove(L2Character cha)
	{
		_movingChars.remove(cha);
		_evtArrivedManager.remove(cha);
		_evtArrivedRevalidateManager.remove(cha);
	}
	
	@Override
	public void run()
	{
		final ArrayList<L2Character> arrivedChars = L2Collections.newArrayList();
		final ArrayList<L2Character> followers = L2Collections.newArrayList();
		
		for (L2Character cha : _movingChars)
		{
			boolean arrived = cha.updatePosition(GameTimeController.getGameTicks());
			
			// normal movement to an exact coordinate
			if (cha.getAI().getFollowTarget() == null)
			{
				if (arrived)
					arrivedChars.add(cha);
			}
			// following a target
			else
			{
				followers.add(cha);
			}
		}
		
		// the followed chars must move before checking for acting radius
		for (L2Character follower : followers)
		{
			// we have reached our target
			if (follower.getAI().isInsideActingRadius())
				arrivedChars.add(follower);
		}
		
		_movingChars.remove(arrivedChars);
		followers.remove(arrivedChars);
		
		_evtArrivedManager.executeAll(arrivedChars);
		_evtArrivedRevalidateManager.executeAll(followers);
		
		L2Collections.recycle(arrivedChars);
		L2Collections.recycle(followers);
	}
	
	private final class EvtArrivedManager extends FIFOSimpleExecutableQueue<L2Character>
	{
		@Override
		protected void removeAndExecuteFirst()
		{
			final L2Character cha = removeFirst();
			final long begin = System.nanoTime();
			
			try
			{
				cha.getKnownList().updateKnownObjects();
				
				if (cha.hasAI())
					cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
			}
			catch (RuntimeException e)
			{
				_log.warn("", e);
			}
			finally
			{
				RunnableStatsManager.handleStats(cha.getClass(), "notifyEvent(CtrlEvent.EVT_ARRIVED)", System.nanoTime() - begin);
			}
		}
	}
	
	private final class EvtArrivedRevalidateManager extends FIFOSimpleExecutableQueue<L2Character>
	{
		@Override
		protected void removeAndExecuteFirst()
		{
			final L2Character cha = removeFirst();
			final long begin = System.nanoTime();
			
			try
			{
				if (cha.hasAI())
					cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_REVALIDATE);
			}
			catch (RuntimeException e)
			{
				_log.warn("", e);
			}
			finally
			{
				RunnableStatsManager.handleStats(cha.getClass(), "notifyEvent(CtrlEvent.EVT_ARRIVED_REVALIDATE)", System.nanoTime() - begin);
			}
		}
	}
}
