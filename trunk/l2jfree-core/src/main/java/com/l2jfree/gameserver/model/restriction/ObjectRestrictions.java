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
package com.l2jfree.gameserver.model.restriction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author  Noctarius
 */
public class ObjectRestrictions
{
	// Restrictions SQL String Definitions:
	private static final String RESTORE_RESTRICTIONS = "SELECT obj_Id, type, delay, message FROM obj_restrictions";
	private static final String DELETE_RESTRICTIONS = "DELETE FROM obj_restrictions";
	private static final String INSERT_RESTRICTIONS = "INSERT INTO obj_restrictions (`obj_Id`, `type`, `delay`, `message`) VALUES (?, ?, ?, ?)";

	private static final Log _log = LogFactory.getLog(ObjectRestrictions.class.getName());
	private static final ObjectRestrictions _instance = new ObjectRestrictions();
	
	private Map<Integer, List<AvailableRestriction>> _restrictionList = new HashMap<Integer, List<AvailableRestriction>>();
	private Map<Integer, List<PausedTimedEvent>> _pausedActions = new HashMap<Integer, List<PausedTimedEvent>>();
	private Map<Integer, List<TimedRestrictionAction>> _runningActions = new HashMap<Integer, List<TimedRestrictionAction>>();
	
	
	public static final ObjectRestrictions getInstance()
	{
		return _instance;
	}
	
	private ObjectRestrictions()
	{
		int i = 0;
		
		_log.info("ObjectRestrictions: loading...");
		Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement(RESTORE_RESTRICTIONS);
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				int objId = rset.getInt("obj_Id");
				AvailableRestriction type = AvailableRestriction.forName(rset.getString("type"));
				int delay = rset.getInt("delay");
				String message = rset.getString("message");
				
				switch (delay)
				{
					case -1:
						addRestriction(objId, type);
						break;
						
					default:
						timedAddRestriction(objId, type, delay, message);
				}
				i++;
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return;
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		_log.info("ObjectRestrictions: loaded "+i+" restrictions.");
	}
	public void shutdown()
	{
		System.out.println("ObjectRestrictions: storing started:");
		Connection con = null;
		
		try {
			con = L2DatabaseFactory.getInstance().getConnection(con);
			
			// Clean up old table data
			PreparedStatement statement = con.prepareStatement(DELETE_RESTRICTIONS);
			statement.execute();
			statement.close();
			
			System.out.println("ObjectRestrictions: storing permanent restrictions.");
			// Store permanent restrictions
			for (int id : _restrictionList.keySet())
			{
				for (AvailableRestriction restriction : _restrictionList.get(id))
				{
					statement = con.prepareStatement(INSERT_RESTRICTIONS);
					
					statement.setInt(1, id);
					statement.setString(2, restriction.name());
					statement.setLong(3, -1);
					statement.setString(4, "");
					
					statement.execute();
					statement.close();
				}
			}
			
			System.out.println("ObjectRestrictions: storing paused events.");
			// Store paused restriction events
			for (int id : _pausedActions.keySet())
			{
				for (PausedTimedEvent paused : _pausedActions.get(id)) {
					statement = con.prepareStatement(INSERT_RESTRICTIONS);
					
					statement.setInt(1, id);
					statement.setString(2, paused.getAction().getRestriction().name());
					statement.setLong(3, paused.getBalancedTime());
					statement.setString(4, paused.getAction().getMessage());
					
					statement.execute();
					statement.close();
				}
			}
			
			System.out.println("ObjectRestrictions: stopping and storing running events.");
			// Store running restriction events
			for (int id : _runningActions.keySet())
			{
				for (TimedRestrictionAction action : _runningActions.get(id))
				{
					// Shutdown task
					action.getTask().cancel(true);
					
					statement = con.prepareStatement(INSERT_RESTRICTIONS);
					
					statement.setInt(1, id);
					statement.setString(2, action.getRestriction().name());
					statement.setLong(3, action.getBalancedTime());
					statement.setString(4, action.getMessage());
					
					statement.execute();
					statement.close();
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}

		System.out.println("ObjectRestrictions: All data saved.");
	}
	
	/**
	 * Adds a restriction on startup
	 * @param objId
	 * @param restriction
	 * @throws RestrictionBindClassException
	 */
	private void addRestriction(int objId, AvailableRestriction restriction)
	{
		if (_restrictionList.get(objId) == null)
		{
			_restrictionList.put(objId, new ArrayList<AvailableRestriction>());
		}
		
		if (!_restrictionList.get(objId).contains(restriction))
			_restrictionList.get(objId).add(restriction);
	}

	/**
	 * Adds a restriction without timelimit
	 * @param owner
	 * @param restriction
	 */
	public void addRestriction(Object owner, AvailableRestriction restriction) throws RestrictionBindClassException
	{
		if (owner == null)
			return;

		if (!checkApplyable(owner, restriction))
			throw new RestrictionBindClassException("Restriction "+restriction.name()+" cannot bound to Class "+owner.getClass());
		
		int id = getObjectId(owner);
		addRestriction(id, restriction);
	}
	
	/**
	 * Removes a restriction
	 * @param owner
	 * @param restriction
	 */
	public void removeRestriction(Object owner, AvailableRestriction restriction)
	{
		if (owner == null)
			return;
		
		int id = -1;
		if (owner instanceof L2Object)
			id = ((L2Object)owner).getObjectId();
		else
			id = owner.hashCode();

		if (_restrictionList.get(id) != null &&
			_restrictionList.get(id).contains(restriction))
		{
			_restrictionList.get(id).remove(restriction);
			
			if (owner instanceof L2Object)
			{
				if (_runningActions.get(id) != null)
				{
					for (TimedRestrictionAction action : _runningActions.get(id))
					{
						if (action.getRestriction() == restriction)
						{
							action.getTask().cancel(true);
							_runningActions.get(id).remove(action);
							break;
						}
					}
				}
				
				if (_pausedActions.get(id) != null)
				{
					for (PausedTimedEvent paused : _pausedActions.get(id))
					{
						if (paused.getAction().getRestriction() == restriction)
						{
							_pausedActions.get(id).remove(paused);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Adds a complete bunch of restrictions without timelimit
	 * @param owner
	 * @param restrictions
	 */
	public void addRestrictionList(Object owner, List<AvailableRestriction> restrictions) throws RestrictionBindClassException
	{
		if (owner == null)
			return;
		
		int id = getObjectId(owner);

		if (_restrictionList.get(id) == null)
		{
			_restrictionList.put(id, new ArrayList<AvailableRestriction>());
		}

		for (AvailableRestriction restriction : restrictions)
		{
			if (!checkApplyable(owner, restriction))
				throw new RestrictionBindClassException("Restriction "+restriction.name()+" cannot bound to Class "+owner.getClass());
		}
		
		_restrictionList.get(id).addAll(restrictions);
	}
	
	/**
	 * Checks if restriction is underway
	 * @param owner
	 * @param restriction
	 * @return
	 */
	public boolean checkRestriction(Object owner, AvailableRestriction restriction)
	{
		if (owner == null)
			return false;
		
		int id = getObjectId(owner);
		
		if (_restrictionList.get(id) == null)
			return false;
		
		return _restrictionList.get(id).contains(restriction);
	}
	
	/**
	 * Schedules a new RemoveRestriction event without info message
	 * @param owner
	 * @param restriction
	 * @param delay
	 */
	public void timedRemoveRestriction(int objId, AvailableRestriction restriction, long delay)
	{
		TimedRestrictionEvent event = new TimedRestrictionEvent(objId, restriction, 
				TimedRestrictionEventType.Remove, delay);
				
		ScheduledFuture<?> task = ThreadPoolManager.getInstance().scheduleGeneral(event, delay);
	
		event.getActionObject().setTask(task);
		addTask(objId, event.getActionObject());
	}

	/**
	 * Schedules a new RemoveRestriction event with info message
	 * @param owner
	 * @param restriction
	 * @param delay
	 * @param message
	 */
	public void timedRemoveRestriction(int objId, AvailableRestriction restriction, long delay, String message)
	{
		TimedRestrictionEvent event = new TimedRestrictionEvent(objId, restriction, 
				TimedRestrictionEventType.Remove, delay, message);
				
		ScheduledFuture<?> task = ThreadPoolManager.getInstance().scheduleGeneral(event, delay);
		
		event.getActionObject().setTask(task);
		addTask(objId, event.getActionObject());
	}

	/**
	 * Schedules a new AddRestriction event without info message
	 * @param owner
	 * @param restriction
	 * @param delay
	 */
	public void timedAddRestriction(L2Object owner, AvailableRestriction restriction, long delay) throws RestrictionBindClassException
	{
		if (!checkApplyable(owner.getObjectId(), restriction))
			throw new RestrictionBindClassException();
		
		timedAddRestriction(owner.getObjectId(), restriction, delay);
	}

	private void timedAddRestriction(int objId, AvailableRestriction restriction, long delay)
	{
		TimedRestrictionEvent event = new TimedRestrictionEvent(objId, restriction, 
				TimedRestrictionEventType.Add, delay);
				
		ScheduledFuture<?> task = ThreadPoolManager.getInstance().scheduleGeneral(event, delay);
		
		event.getActionObject().setTask(task);
		addTask(objId, event.getActionObject());
	}

	/**
	 * Schedules a new AddRestriction event with info message
	 * @param owner
	 * @param restriction
	 * @param delay
	 * @param message
	 */
	public void timedAddRestriction(L2Object owner, AvailableRestriction restriction, long delay, String message) throws RestrictionBindClassException
	{
		if (!checkApplyable(owner, restriction))
			throw new RestrictionBindClassException();

		timedAddRestriction(owner.getObjectId(), restriction, delay, message);
	}

	private void timedAddRestriction(int objId, AvailableRestriction restriction, long delay, String message)
	{
		TimedRestrictionEvent event = new TimedRestrictionEvent(objId, restriction, 
				TimedRestrictionEventType.Add, delay, message);

		ScheduledFuture<?> task = ThreadPoolManager.getInstance().scheduleGeneral(event, delay);
		
		event.getActionObject().setTask(task);
		addTask(objId, event.getActionObject());
	}
	
	
	/**
	 * Adds a new active scheduled task
	 * @param owner
	 * @param action
	 */
	private void addTask(int objId, TimedRestrictionAction action)
	{
		if (_runningActions.get(objId) == null)
			_runningActions.put(objId, new ArrayList<TimedRestrictionAction>());
		
		if (!_runningActions.get(objId).contains(action))
			_runningActions.get(objId).add(action);
	}
	/**
	 * Adds a new paused scheduled task
	 * @param owner
	 * @param action
	 */
	private void addPausedTask(int objId, PausedTimedEvent action)
	{
		if (_pausedActions.get(objId) == null)
			_pausedActions.put(objId, new ArrayList<PausedTimedEvent>());
		
		if (!_pausedActions.get(objId).contains(action))
			_pausedActions.get(objId).add(action);
	}
	
	/**
	 * Checks if there are paused tasks
	 * @param owner
	 * @return
	 */
	public boolean containsPausedTask(int objId)
	{
		return (_pausedActions.get(objId)!= null &&
				!_pausedActions.get(objId).isEmpty());
	}
	/**
	 * Checks if there are running tasks
	 * @param owner
	 * @return
	 */
	public boolean containsRunningTask(int objId)
	{
		return (_runningActions.get(objId)!= null &&
				!_runningActions.get(objId).isEmpty());
	}
	
	/**
	 * Pauses tasks on player logout
	 * @param owner
	 */
	public void pauseTasks(int objId)
	{
		if (!containsRunningTask(objId))
			return;
		
		for (TimedRestrictionAction action : _runningActions.get(objId))
		{
			// Cancel active task
			action.getTask().cancel(true);
			
			// Save PausedEventObject
			PausedTimedEvent paused = new PausedTimedEvent(action, action.getBalancedTime());
			addPausedTask(objId, paused);
		}
		
		// Clear up running tasks 
		_runningActions.get(objId).clear();
		_runningActions.put(objId, null);
	}
	/**
	 * Resumes tasks on player login
	 * @param owner
	 */
	public void resumeTasks(int objId)
	{
		if (!containsPausedTask(objId))
			return;
		
		for (PausedTimedEvent paused : _pausedActions.get(objId))
		{
			switch (paused.getAction().getEventType()) {
				case Add:
					timedAddRestriction(objId, paused.getAction().getRestriction(),
							paused.getBalancedTime(),
							paused.getAction().getMessage());
					break;
					
				case Remove:
					timedRemoveRestriction(objId, paused.getAction().getRestriction(),
							paused.getBalancedTime(),
							paused.getAction().getMessage());
			}
		}
		
		// Clear up running tasks 
		_pausedActions.get(objId).clear();
		_pausedActions.put(objId, null);
	}
	
	
	
	private class TimedRestrictionEvent implements Runnable
	{
		private final TimedRestrictionAction _action;
		
		public TimedRestrictionEvent(int objId, AvailableRestriction restriction, TimedRestrictionEventType type, long delay)
		{
			_action = new TimedRestrictionAction(objId, restriction, type, null, delay);
		}
		public TimedRestrictionEvent(int objId, AvailableRestriction restriction, TimedRestrictionEventType type, long delay, String message)
		{
			_action = new TimedRestrictionAction(objId, restriction, type, message, delay);
		}

		public void run()
		{
			switch (_action.getEventType())
			{
				case Add:
					ObjectRestrictions.getInstance().addRestriction(_action.getObjectId(), _action.getRestriction());
					break;
					
				case Remove:
					ObjectRestrictions.getInstance().removeRestriction(_action.getObjectId(), _action.getRestriction());
			}
			
			if (_action.getMessage() != null)
			{
				L2Object owner = L2World.getInstance().findObject(_action.getObjectId());
				
				if (owner instanceof L2PcInstance)
				{
					((L2PcInstance)owner).sendMessage(_action.getMessage());
				}
				else if (owner instanceof L2Summon)
				{
					L2Summon summon = (L2Summon)owner;
					summon.getOwner().sendMessage(_action.getMessage());
				}
			}
		}
		
		public TimedRestrictionAction getActionObject()
		{
			return _action;
		}
	}

	private enum TimedRestrictionEventType
	{
		Remove,
		Add
	}

	private class TimedRestrictionAction
	{
		private final int _objId;
		private final AvailableRestriction _restriction;
		private final TimedRestrictionEventType _type;
		private final String _message;
		private final Long _delay;
		private Long _starttime;
		private ScheduledFuture<?> _task;
		
		public TimedRestrictionAction (int objId, AvailableRestriction restriction, TimedRestrictionEventType type, String message, long delay)
		{
			_objId			= objId;
			_restriction	= restriction;
			_type			= type;
			_message		= message;
			_delay			= delay;
			
			_starttime = System.currentTimeMillis();
		}
		
		public int getObjectId()
		{
			return _objId;
		}
		public AvailableRestriction getRestriction()
		{
			return _restriction;
		}
		public TimedRestrictionEventType getEventType()
		{
			return _type;
		}
		public String getMessage()
		{
			return _message;
		}
		public Long getDelay()
		{
			return _delay;
		}
		public Long getBalancedTime()
		{
			return (_delay * 1000) - (System.currentTimeMillis() - _starttime);
		}
		
		public ScheduledFuture<?> getTask()
		{
			return _task;
		}
		public void setTask(ScheduledFuture<?> task)
		{
			_task = task;
		}
	}

	private class PausedTimedEvent
	{
		private final Long _balancedTime;
		private final TimedRestrictionAction _action;
		
		public PausedTimedEvent(TimedRestrictionAction action, long balancedTime)
		{
			_action = action;
			_balancedTime = balancedTime;
		}
		
		public Long getBalancedTime()
		{
			return _balancedTime;
		}
		
		public TimedRestrictionAction getAction()
		{
			return _action;
		}
	}
	
	private boolean checkApplyable(Object owner, AvailableRestriction restriction)
	{
		return (restriction.getApplyableTo().isInstance(owner));
	}
	
	private int getObjectId(Object owner)
	{
		if (owner instanceof L2Object)
			return ((L2Object)owner).getObjectId();

		return owner.hashCode();
	}
}
