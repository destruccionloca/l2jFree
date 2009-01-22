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
package com.l2jfree.gameserver;

import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlEvent;
import com.l2jfree.gameserver.datatables.DoorTable;
import com.l2jfree.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2BoatInstance;

public final class GameTimeController extends Thread
{
	private static final Log _log = LogFactory.getLog(GameTimeController.class);

	public static final int TICKS_PER_SECOND = 10;
	public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;

	private static final long GAME_START_TIME = System.currentTimeMillis() - 3600000;

	private static int _gameTicks = 3600000 / MILLIS_IN_TICK;
	private static boolean _isNight = false;

	private static final GameTimeController _instance = new GameTimeController();

	public static GameTimeController getInstance()
	{
		return _instance;
	}

	private final Set<L2Character> _movingChars = new FastSet<L2Character>();
	private final FastList<L2Character> _endedChars = new FastList<L2Character>();

	private GameTimeController()
	{
		super("GameTimeController");
		setDaemon(true);
		setPriority(MAX_PRIORITY);
		start();

		// [L2J_JP ADD SANDMAN]
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new OpenPiratesRoom(), 2000, 600000);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new BroadcastSunState(), 0, 600000);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new MovingObjectArrived(), MILLIS_IN_TICK, MILLIS_IN_TICK);

		_log.info("GameTimeController: Initialized.");
	}

	// One ingame day is 240 real minutes
	public boolean isNowNight()
	{
		return _isNight;
	}

	public int getGameTime()
	{
		return _gameTicks / (TICKS_PER_SECOND * 10);
	}

	public static int getGameTicks()
	{
		return _gameTicks;
	}

	public void registerMovingChar(L2Character cha)
	{
		synchronized (_movingChars)
		{
			_movingChars.add(cha);
		}
	}

	private L2Character[] getMovingChars()
	{
		synchronized (_movingChars)
		{
			return _movingChars.toArray(new L2Character[_movingChars.size()]);
		}
	}

	private void moveObjects()
	{
		for (L2Character cha : getMovingChars())
		{
			if (!cha.updatePosition(_gameTicks))
				continue;

			synchronized (_movingChars)
			{
				_movingChars.remove(cha);
			}

			synchronized (_endedChars)
			{
				_endedChars.add(cha);
			}
		}
	}

	public void stopTimer()
	{
		interrupt();
	}

	@Override
	public void run()
	{
		for (;;)
		{
			long runtime = System.currentTimeMillis();
			int oldTicks = _gameTicks;

			_gameTicks = (int)(runtime - GAME_START_TIME) / MILLIS_IN_TICK;

			try
			{
				if (oldTicks != _gameTicks)
					moveObjects();
			}
			catch (Exception e)
			{
				_log.warn("", e);
			}

			runtime = System.currentTimeMillis() - runtime;

			try
			{
				sleep(1 + MILLIS_IN_TICK - (int)runtime % MILLIS_IN_TICK);
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	private L2Character getNextEndedChar()
	{
		synchronized (_endedChars)
		{
			return _endedChars.isEmpty() ? null : _endedChars.removeFirst();
		}
	}

	private class MovingObjectArrived implements Runnable
	{
		public void run()
		{
			for (L2Character cha; (cha = getNextEndedChar()) != null;)
				try
				{
					cha.getKnownList().updateKnownObjects();

					if (cha instanceof L2BoatInstance) {
						((L2BoatInstance)cha).evtArrived();
					}
					if (cha.hasAI())
						cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
				}
				catch (Exception e)
				{
					_log.warn("", e);
				}
		}
	}

	private class BroadcastSunState implements Runnable
	{
		public void run()
		{
			boolean tempIsNight = getGameTime() / 60 % 24 < 6;

			if (tempIsNight != _isNight)
			{
				_isNight = tempIsNight;
				DayNightSpawnManager.getInstance().notifyChangeMode();
			}
		}
	}

	// [L2J_JP ADD]
	// Open door of pirate's room at AM0:00 every day in game.
	private class OpenPiratesRoom implements Runnable
	{
		public void run()
		{
			// Avoid problems during server startup
			if (!DoorTable.isInitialized())
				return;

			if (getGameTime() / 60 % 24 == Config.ALT_TIME_IN_A_DAY_OF_OPEN_A_DOOR)
			{
				DoorTable.getInstance().getDoor(21240006).openMe();
				ThreadPoolManager.getInstance().schedule(new ClosePiratesRoom(),
					Config.ALT_TIME_OF_OPENING_A_DOOR * 60 * 1000);
			}
		}
	}

	// [L2J_JP ADD]
	// Close door of pirate's room.
	private class ClosePiratesRoom implements Runnable
	{
		public void run()
		{
			DoorTable.getInstance().getDoor(21240006).closeMe();
		}
	}
}
