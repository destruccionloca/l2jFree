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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author  Noctarius
 */
public class ObjectRestrictions
{    
    // Restrictions SQL String Definitions:
    private static final String RESTORE_RESTRICTIONS = "SELECT type, delay, message FROM obj_restrictions WHERE obj_Id=?";
    private static final String UPDATE_RESTRICTIONS = "UPDATE obj_restrictions SET type=?, delay=?, message=? WHERE entryId=?";
    private static final String INSERT_RESTRICTIONS = "INSERT INTO obj_restrictions ('obj_Id', 'type', 'delay', 'message' VALUES (?, ?, ?, ?)";

    private static final ObjectRestrictions _instance = new ObjectRestrictions();
	
	private Map<Object, List<AvailableRestriction>> _restrictionList = new HashMap<Object, List<AvailableRestriction>>();
	private Map<Integer, List<PausedTimedEvent>> _pausedActions = new HashMap<Integer, List<PausedTimedEvent>>();
	private Map<Integer, List<TimedRestrictionAction>> _runningActions = new HashMap<Integer, List<TimedRestrictionAction>>();
	
	
	public static final ObjectRestrictions getInstance() {
		return _instance;
	}
	
	private ObjectRestrictions() {
		//TODO: Data loading
	}
	public void shutdown() {
		//TODO: Data saving
	}
	
	
	/**
	 * Adds a restriction without timelimit
	 * @param owner
	 * @param restriction
	 */
	public void addRestriction(Object owner, AvailableRestriction restriction) {
		if (_restrictionList.get(owner) == null) {
			_restrictionList.put(owner, new ArrayList<AvailableRestriction>());
		}
		
		if (!_restrictionList.get(owner).contains(restriction))
			_restrictionList.get(owner).add(restriction);
	}
	
	/**
	 * Removes a restriction
	 * @param owner
	 * @param restriction
	 */
	public void removeRestriction(Object owner, AvailableRestriction restriction) {
		if (_restrictionList.get(owner) != null &&
			_restrictionList.get(owner).contains(restriction)) {
			_restrictionList.get(owner).remove(restriction);
			
			if (owner instanceof L2Object) {
				L2Object o = (L2Object)owner;
				
				if (_runningActions.get(o.getObjectId()) != null) {
					for (TimedRestrictionAction action : _runningActions.get(o.getObjectId())) {
						if (action.getRestriction() == restriction) {
							action.getTask().cancel(true);
							_runningActions.get(o.getObjectId()).remove(action);
							break;
						}
					}
				}
				
				if (_pausedActions.get(o.getObjectId()) != null) {
					for (PausedTimedEvent paused : _pausedActions.get(o.getObjectId())) {
						if (paused.getAction().getRestriction() == restriction) {
							_pausedActions.get(o.getObjectId()).remove(paused);
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
	public void addRestrictionList(Object owner, List<AvailableRestriction> restrictions) {
		if (_restrictionList.get(owner) == null) {
			_restrictionList.put(owner, new ArrayList<AvailableRestriction>());
		}
		
		_restrictionList.get(owner).addAll(restrictions);
	}
	
	/**
	 * Checks if restriction is underway
	 * @param owner
	 * @param restriction
	 * @return
	 */
	public boolean checkRestriction(Object owner, AvailableRestriction restriction) {
		if (_restrictionList.get(owner) == null)
			return false;
		
		return _restrictionList.get(owner).contains(restriction);
	}
	
	/**
	 * Schedules a new RemoveRestriction event without info message
	 * @param owner
	 * @param restriction
	 * @param delay
	 */
	public void timedRemoveRestriction(L2Object owner, AvailableRestriction restriction, long delay) {
		TimedRestrictionEvent event = new TimedRestrictionEvent(owner, restriction, 
				TimedRestrictionEventType.Remove, delay);
				
		ScheduledFuture task = ThreadPoolManager.getInstance().scheduleGeneral(event, delay);
	
		event.getActionObject().setTask(task);
		addTask(owner, event.getActionObject());
	}
	/**
	 * Schedules a new RemoveRestriction event with info message
	 * @param owner
	 * @param restriction
	 * @param delay
	 * @param message
	 */
	public void timedRemoveRestriction(L2Object owner, AvailableRestriction restriction, long delay, String message) {
		TimedRestrictionEvent event = new TimedRestrictionEvent(owner, restriction, 
				TimedRestrictionEventType.Remove, delay, message);
				
		ScheduledFuture task = ThreadPoolManager.getInstance().scheduleGeneral(event, delay);
		
		event.getActionObject().setTask(task);
		addTask(owner, event.getActionObject());
	}

	/**
	 * Schedules a new AddRestriction event without info message
	 * @param owner
	 * @param restriction
	 * @param delay
	 */
	public void timedAddRestriction(L2Object owner, AvailableRestriction restriction, long delay) {
		TimedRestrictionEvent event = new TimedRestrictionEvent(owner, restriction, 
				TimedRestrictionEventType.Add, delay);
				
		ScheduledFuture task = ThreadPoolManager.getInstance().scheduleGeneral(event, delay);
		
		event.getActionObject().setTask(task);
		addTask(owner, event.getActionObject());
	}
	/**
	 * Schedules a new AddRestriction event with info message
	 * @param owner
	 * @param restriction
	 * @param delay
	 * @param message
	 */
	public void timedAddRestriction(L2Object owner, AvailableRestriction restriction, long delay, String message) {
		TimedRestrictionEvent event = new TimedRestrictionEvent(owner, restriction, 
				TimedRestrictionEventType.Add, delay, message);

		ScheduledFuture task = ThreadPoolManager.getInstance().scheduleGeneral(event, delay);
		
		event.getActionObject().setTask(task);
		addTask(owner, event.getActionObject());
	}
	
	
	/**
	 * Adds a new active scheduled task
	 * @param owner
	 * @param action
	 */
	private void addTask(L2Object owner, TimedRestrictionAction action) {
		if (_runningActions.get(owner.getObjectId()) == null)
			_runningActions.put(owner.getObjectId(), new ArrayList<TimedRestrictionAction>());
		
		if (!_runningActions.get(owner.getObjectId()).contains(action))
			_runningActions.get(owner.getObjectId()).add(action);
	}
	/**
	 * Adds a new paused scheduled task
	 * @param owner
	 * @param action
	 */
	private void addPausedTask(L2Object owner, PausedTimedEvent action) {
		if (_pausedActions.get(owner.getObjectId()) == null)
			_pausedActions.put(owner.getObjectId(), new ArrayList<PausedTimedEvent>());
		
		if (!_pausedActions.get(owner.getObjectId()).contains(action))
			_pausedActions.get(owner.getObjectId()).add(action);
	}
	
	/**
	 * Checks if there are paused tasks
	 * @param owner
	 * @return
	 */
	public boolean containsPausedTask(L2Object owner) {
		return (_pausedActions.get(owner.getObjectId())!= null &&
				_pausedActions.get(owner.getObjectId()).size() > 0);
	}
	/**
	 * Checks if there are running tasks
	 * @param owner
	 * @return
	 */
	public boolean containsRunningTask(L2Object owner) {
		return (_runningActions.get(owner.getObjectId())!= null &&
				_runningActions.get(owner.getObjectId()).size() > 0);
	}
	
	/**
	 * Pauses tasks on player logout
	 * @param owner
	 */
	public void pauseTasks(L2Object owner) {
		if (!containsRunningTask(owner))
			return;
		
		for (TimedRestrictionAction action : _runningActions.get(owner.getObjectId())) {
			// Cancel active task
			action.getTask().cancel(true);
			
			// Save PausedEventObject
			PausedTimedEvent paused = new PausedTimedEvent(action, action.getBalancedTime());
			addPausedTask(owner, paused);
		}
		
		// Clear up running tasks 
		_runningActions.get(owner.getObjectId()).clear();
		_runningActions.put(owner.getObjectId(), null);
	}
	/**
	 * Resumes tasks on player login
	 * @param owner
	 */
	public void resumeTasks(L2Object owner) {
		if (!containsPausedTask(owner))
			return;
		
		for (PausedTimedEvent paused : _pausedActions.get(owner.getObjectId())) {
			switch (paused.getAction().getEventType()) {
				case Add:
					timedAddRestriction(owner, paused.getAction().getRestriction(),
							paused.getBalancedTime(),
							paused.getAction().getMessage());
					break;
					
				case Remove:
					timedRemoveRestriction(owner, paused.getAction().getRestriction(),
							paused.getBalancedTime(),
							paused.getAction().getMessage());
			}
		}
		
		// Clear up running tasks 
		_pausedActions.get(owner.getObjectId()).clear();
		_pausedActions.put(owner.getObjectId(), null);
	}
	
	
	
	private class TimedRestrictionEvent implements Runnable {
		private final TimedRestrictionAction _action;
		
		public TimedRestrictionEvent(L2Object owner, AvailableRestriction restriction, TimedRestrictionEventType type, long delay) {
			_action = new TimedRestrictionAction(owner, restriction, type, null, delay);
		}
		public TimedRestrictionEvent(L2Object owner, AvailableRestriction restriction, TimedRestrictionEventType type, long delay, String message) {
			_action = new TimedRestrictionAction(owner, restriction, type, message, delay);
		}
	
		public void run() {
			switch (_action.getEventType()) {
				case Add:
					ObjectRestrictions.getInstance().addRestriction(_action.getOwner(), _action.getRestriction());
					break;
					
				case Remove:
					ObjectRestrictions.getInstance().removeRestriction(_action.getOwner(), _action.getRestriction());
			}
			
			if (_action.getMessage() != null) {
				if (_action.getOwner() instanceof L2PcInstance) {
					((L2PcInstance)_action.getOwner()).sendMessage(_action.getMessage());
				} else if (_action.getOwner() instanceof L2Summon) {
					L2Summon summon = (L2Summon)_action.getOwner();
					
					summon.getOwner().sendMessage(_action.getMessage());
				}
			}
		}
		
		public TimedRestrictionAction getActionObject() {
			return _action;
		}
	}
	private enum TimedRestrictionEventType {
		Remove,
		Add
	}
	private class TimedRestrictionAction {
		private final L2Object _owner;
		private final AvailableRestriction _restriction;
		private final TimedRestrictionEventType _type;
		private final String _message;
		private final Long _delay;
		private Long _starttime;
		private ScheduledFuture _task;
		
		public TimedRestrictionAction (L2Object owner, AvailableRestriction restriction, TimedRestrictionEventType type, String message, long delay) {
			_owner			= owner;
			_restriction	= restriction;
			_type			= type;
			_message		= message;
			_delay			= delay;
			
			_starttime = Calendar.getInstance().getTimeInMillis();
		}
		
		public L2Object getOwner() {
			return _owner;
		}
		public AvailableRestriction getRestriction() {
			return _restriction;
		}
		public TimedRestrictionEventType getEventType() {
			return _type;
		}
		public String getMessage() {
			return _message;
		}
		public Long getDelay() {
			return _delay;
		}
		public Long getBalancedTime() {
			return (_delay * 1000) - (Calendar.getInstance().getTimeInMillis() - _starttime);
		}
		
		public ScheduledFuture getTask() {
			return _task;
		}
		public void setTask(ScheduledFuture task) {
			_task = task;
		}
	}
	private class PausedTimedEvent {
		private final Long _balancedTime;
		private final TimedRestrictionAction _action;
		
		public PausedTimedEvent(TimedRestrictionAction action, long balancedTime) {
			_action = action;
			_balancedTime = balancedTime;
		}
		
		public Long getBalancedTime() {
			return _balancedTime;
		}
		public TimedRestrictionAction getAction() {
			return _action;
		}
	}
}
