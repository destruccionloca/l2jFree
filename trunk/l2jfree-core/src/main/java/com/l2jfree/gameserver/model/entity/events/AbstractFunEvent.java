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
package com.l2jfree.gameserver.model.entity.events;

import com.l2jfree.gameserver.model.restriction.global.AbstractRestriction;

/**
 * @author NB4L1
 */
public abstract class AbstractFunEvent
{
	public enum FunEventState
	{
		INACTIVE,
		REGISTRATION,
		PREPARATION,
		RUNNING,
		COOLDOWN;
	}
	
	private final AbstractRestriction[] _restrictions = new AbstractRestriction[FunEventState.values().length];
	
	private FunEventState _state;
	
	protected AbstractFunEvent()
	{
		for (FunEventState state : FunEventState.values())
			_restrictions[state.ordinal()] = initRestriction(state);
	}
	
	/**
	 * Called only from the constructor of {@link AbstractFunEvent} to initialize restrictions<br>
	 * that will be activated/de-actived as state changes.
	 * 
	 * @param state
	 * @return the proper restriction, can be null
	 */
	protected abstract AbstractRestriction initRestriction(FunEventState state);
	
	/**
	 * @return the current state
	 */
	public FunEventState getState()
	{
		return _state;
	}
	
	/**
	 * Sets the state of the funevent.
	 * 
	 * @param nextState
	 * @return the previous state
	 */
	protected synchronized final FunEventState setState(FunEventState nextState)
	{
		final FunEventState prevState = getState();
		
		setState(prevState, nextState);
		
		return prevState;
	}
	
	/**
	 * Sets the state of the funevent with validation.
	 * 
	 * @param expectedPrevState
	 * @param nextState
	 * @throws IllegalStateException if the previous and the expected previous state is different
	 */
	protected synchronized void setState(FunEventState expectedPrevState, FunEventState nextState) throws IllegalStateException
	{
		final FunEventState prevState = getState();
		
		if (expectedPrevState != prevState)
			throw new IllegalStateException();
		
		_state = nextState;
		
		for (AbstractRestriction restriction : _restrictions)
			if (restriction != null)
				restriction.deactivate();
		
		if (_restrictions[nextState.ordinal()] != null)
			_restrictions[nextState.ordinal()].activate();
		
		switch (nextState)
		{
			case INACTIVE:
			{
				break;
			}
			case REGISTRATION:
			{
				break;
			}
			case PREPARATION:
			{
				break;
			}
			case RUNNING:
			{
				break;
			}
			case COOLDOWN:
			{
				break;
			}
		}
	}
}
