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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

import javolution.util.FastSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlEvent;
import com.l2jfree.gameserver.calendar.L2Calendar;
import com.l2jfree.gameserver.datatables.DoorTable;
import com.l2jfree.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance.ConditionListenerDependency;
import com.l2jfree.gameserver.network.serverpackets.ClientSetTime;
import com.l2jfree.gameserver.taskmanager.AbstractFIFOPeriodicTaskManager;

public final class GameTimeController extends Thread
{
	public static final int					TICKS_PER_SECOND	= 10;
	public static final int					MILLIS_IN_TICK		= 1000 / TICKS_PER_SECOND;

	private static final Log				_log				= LogFactory.getLog(GameTimeController.class);
	private static L2Calendar				_calendar;
	private final Set<L2Character>			_movingChars		= new FastSet<L2Character>();
	private static final GameTimeController	_instance			= new GameTimeController();
	public long								_startMoveTime;

	public static GameTimeController getInstance()
	{
		return _instance;
	}
	
	// Close door of pirate's room.
	private class ClosePiratesRoom implements Runnable
	{
		public void run()
		{
			DoorTable.getInstance().getDoor(21240006).closeMe();
		}
	}

	private class MinuteCounter implements Runnable
	{
		public void run()
		{
			boolean isNight = isNowNight();
			int oldHour = _calendar.getDate().get(Calendar.HOUR_OF_DAY);
			int oldDay = _calendar.getDate().get(Calendar.DAY_OF_YEAR);
			int oldYear = _calendar.getDate().get(Calendar.YEAR);
			_calendar.getDate().add(Calendar.MINUTE, 1);

			//check if one hour passed
			int newHour = _calendar.getDate().get(Calendar.HOUR_OF_DAY);
			if (newHour != oldHour)
			{
				//update time for all players
				for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				{
					player.sendPacket(ClientSetTime.STATIC_PACKET);
				}
				//check for zaken door
				if (newHour == Config.ALT_TIME_IN_A_DAY_OF_OPEN_A_DOOR)
				{
					DoorTable.getInstance().getDoor(21240006).openMe();
					ThreadPoolManager.getInstance().schedule(new ClosePiratesRoom(), Config.ALT_TIME_OF_OPENING_A_DOOR * 60 * 1000);
				}
				//check if night state changed
				if (isNight != isNowNight())
				{
					DayNightSpawnManager.getInstance().notifyChangeMode();
					
					for (L2PcInstance player : L2World.getInstance().getAllPlayers())
						player.refreshConditionListeners(ConditionListenerDependency.GAME_TIME);
				}
				//check if a whole day passed
				if (oldDay != _calendar.getDate().get(Calendar.DAY_OF_YEAR))
				{
					_log.info("A day passed its now: " + getFormatedDate());
					int newYear = _calendar.getDate().get(Calendar.YEAR);
					if (oldYear != newYear)
					{
						Announcements.getInstance().announceToAll("A new year has begun, good luck to all in the year " + newYear);
					}
				}
			}
			if (Config.DATETIME_SAVECAL)
				saveData();
		}
	}

	public GregorianCalendar getDate()
	{
		return _calendar.getDate();
	}

	public String getFormatedDate()
	{
		SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
		if(Config.DATETIME_SAVECAL)
			format = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		return format.format(getDate().getTime());
	}

	private GameTimeController()
	{
		super("GameTimeController");
		setDaemon(true);
		setPriority(MAX_PRIORITY);
		if (Config.DATETIME_SAVECAL)
			_calendar = (L2Calendar) loadData();
		if (_calendar == null)
		{
			_calendar = new L2Calendar();
			_calendar.getDate().set(Calendar.YEAR, 1281);
			_calendar.getDate().set(Calendar.MONTH, 5);
			_calendar.getDate().set(Calendar.DAY_OF_MONTH, 5);
			_calendar.getDate().set(Calendar.HOUR_OF_DAY, 23);
			_calendar.getDate().set(Calendar.MINUTE, 45);
			_calendar.setGameStarted(System.currentTimeMillis());
			saveData();
		}
		start();

		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new MinuteCounter(), 0, 1000 * Config.DATETIME_MULTI);

		_log.info("GameTimeController: Initialized.");
	}
	
	public static int getGameTicks()
	{
		return _calendar.gameTicks;
	}

	public int getGameTime()
	{
		return _calendar.getGameTime();
	}

	private L2Character[] getMovingChars()
	{
		synchronized (_movingChars)
		{
			return _movingChars.toArray(new L2Character[_movingChars.size()]);
		}
	}

	public boolean isNowNight()
	{
		int hour = _calendar.getDate().get(Calendar.HOUR_OF_DAY);
		if (hour < Config.DATETIME_SUNRISE || hour > Config.DATETIME_SUNSET)
			return true;
		return false;
	}

	private Object loadData()
	{
		try
		{
			String filename = "data/serial/clock.dat";
			ObjectInputStream objstream = new ObjectInputStream(new FileInputStream(filename));
			Object object = objstream.readObject();
			objstream.close();
			return object;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	private static final class ArrivedCharacterManager extends AbstractFIFOPeriodicTaskManager<L2Character>
	{
		private static final ArrivedCharacterManager _instance = new ArrivedCharacterManager();
		
		public static ArrivedCharacterManager getInstance()
		{
			return _instance;
		}
		
		private ArrivedCharacterManager()
		{
			super(GameTimeController.MILLIS_IN_TICK);
		}
		
		@Override
		protected void callTask(L2Character cha)
		{
			cha.getKnownList().updateKnownObjects();
			
			if (cha instanceof L2BoatInstance)
				((L2BoatInstance)cha).evtArrived();
			
			if (cha.hasAI())
				cha.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
		}
		
		@Override
		protected String getCalledMethodName()
		{
			return "getAI().notifyEvent(CtrlEvent.EVT_ARRIVED)";
		}
	}
	
	private void moveObjects()
	{
		for (L2Character cha : getMovingChars())
		{
			if (!cha.updatePosition(_calendar.gameTicks))
			{
				continue;
			}
			
			synchronized (_movingChars)
			{
				_movingChars.remove(cha);
			}
			
			ArrivedCharacterManager.getInstance().add(cha);
		}
	}

	public void registerMovingChar(L2Character cha)
	{
		synchronized (_movingChars)
		{
			_movingChars.add(cha);
		}
	}

	@Override
	public void run()
	{
		for (;;)
		{
			long currentTime = System.currentTimeMillis();
			_startMoveTime = currentTime;
			_calendar.gameTicks = (int) ((currentTime - _calendar.getGameStarted()) / MILLIS_IN_TICK);
			moveObjects();
			currentTime = System.currentTimeMillis();
			_calendar.gameTicks = (int) ((currentTime - _calendar.getGameStarted()) / MILLIS_IN_TICK);
			//move delay
			long sleepTime = Config.DATETIME_MOVE_DELAY - (currentTime - _startMoveTime);
			if (sleepTime > 0)
			{
				try
				{
					sleep(sleepTime);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void saveData()
	{
		if (Config.DATETIME_SAVECAL)
		{
			try
			{
				String filename = "data/serial/clock.dat";
				ObjectOutputStream objstream = new ObjectOutputStream(new FileOutputStream(filename));
				objstream.writeObject(_calendar);
				objstream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void stopTimer()
	{
		interrupt();
	}
}
