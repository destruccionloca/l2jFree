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

public class L2DynamicZone extends L2Zone
{
	private ScheduledFuture<?> _task;
	private Condition _cond;
	
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
	
	@Override
	protected boolean checkDynamicConditions(L2Character character)
	{
		if (!checkCondition(character))
			return false;
		
		return super.checkDynamicConditions(character);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		super.onEnter(character);
		
		// Timer turns on if characters are in zone
		startZoneTask();
	}
	
	private synchronized void startZoneTask()
	{
		if (_task == null)
			//one abnormal effect animation cycle = 3000ms
			_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ZoneTask(), 0, 3000);
	}
	
	private synchronized void stopZoneTask()
	{
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
	}
	
	private final class ZoneTask implements Runnable
	{
		public void run()
		{
			// Timer turns off if zone is empty
			if (getCharactersInside().isEmpty())
			{
				stopZoneTask();
				return;
			}
			
			revalidateAllInZone();
			
			for (L2Character character : getCharactersInsideActivated())
			{
				checkForDamage(character);
				if (isRepeatingBuff())
					checkForEffects(character);
			}
		}
	}
	
	protected void checkForDamage(L2Character character)
	{
	}
	
	protected void checkForEffects(L2Character character)
	{
		if (getApplyEnter() != null)
			for (L2Skill sk : getApplyEnter())
				if (character.getFirstEffect(sk.getId()) == null)
					sk.getEffects(character, character);
		
		if (getRemoveEnter() != null)
			for (int id : getRemoveEnter())
				character.stopSkillEffects(id);
	}
	
	// Zone parser
	
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
