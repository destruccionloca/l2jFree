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
package com.l2jfree.gameserver.model.zone;

import java.util.concurrent.ScheduledFuture;

import org.w3c.dom.Node;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.conditions.Condition;
import com.l2jfree.gameserver.skills.conditions.ConditionParser;

public class L2DynamicZone extends L2DefaultZone
{
	private ScheduledFuture<?> _task;
	private Condition _cond;
	
	public L2DynamicZone()
	{
		super();
	}
	
	private boolean checkCondition(L2Character character)
	{
		if (_cond == null)
			return true;
		
		// Works with ConditionPlayer* and ConditionTarget* and some other
		Env env = new Env();
		env.player = character;
		env.target = character;
		return _cond.test(env);
	}
	
	// Called on movement
	@Override
	public void revalidateInZone(L2Character character)
	{
		if (_enabled && checkCondition(character) && isCorrectType(character) && isInsideZone(character))
		{
			if (!_characterList.containsKey(character.getObjectId()))
			{
				_characterList.put(character.getObjectId(), character);
				onEnter(character);
				// Timer turns on if characters are in zone
				if (_task == null)
					startZoneTask(character);
			}
		}
		else
		{
			if (_characterList.containsKey(character.getObjectId()))
			{
				_characterList.remove(character.getObjectId());
				onExit(character);
				// Timer turns off if zone is empty
				if (_characterList.size() == 0)
					stopZoneTask(character);
			}
		}
	}
	
	// Called by timer
	protected boolean revalidateCondition(L2Character character)
	{
		if (checkCondition(character))
		{
			if (!_characterList.containsKey(character.getObjectId()))
			{
				_characterList.put(character.getObjectId(), character);
				onEnter(character);
				// Timer turns on if characters are in zone
				if (_task == null)
					startZoneTask(character);
			}
			return true;
		}
		
		if (_characterList.containsKey(character.getObjectId()))
		{
			_characterList.remove(character.getObjectId());
			onExit(character);
			// Timer turns off if zone is empty
			if (_characterList.size() == 0)
				stopZoneTask(character);
		}
		return false;
	}
	
	private synchronized void startZoneTask(L2Character character)
	{
		if (_task == null)
			//one abnormal effect animation cycle = 3000ms
			_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ZoneTask(), 0, 3000);
	}
	
	private synchronized void stopZoneTask(L2Character character)
	{
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
	}
	
	private class ZoneTask implements Runnable
	{
		public void run()
		{
			for (L2Character character : getCharactersInside().values())
			{
				if (revalidateCondition(character))
				{
					checkForDamage(character);
					if (_buffRepeat)
						checkForEffects(character);
				}
			}
		}
	}
	
	protected void checkForDamage(L2Character character)
	{
	}
	
	protected void checkForEffects(L2Character character)
	{
		if (_applyEnter != null)
		{
			for (L2Skill sk : _applyEnter)
			{
				if (character.getFirstEffect(sk.getId()) == null)
					sk.getEffects(character, character);
			}
		}
	}
	
	@Override
	protected void parseCondition(Node n) throws Exception
	{
		Condition cond = ConditionParser.getDefaultInstance().parseExistingCondition(n, null);
		Condition old = _cond;
		
		if (old != null)
			_log.fatal("Replaced " + old + " condition with " + cond + " condition at zone: " + this);
		
		_cond = cond;
	}
}
