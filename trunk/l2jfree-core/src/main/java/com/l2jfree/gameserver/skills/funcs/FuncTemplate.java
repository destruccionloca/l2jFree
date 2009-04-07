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
package com.l2jfree.gameserver.skills.funcs;

import java.lang.reflect.Constructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.skills.conditions.Condition;

/**
 * @author mkizub
 */
public final class FuncTemplate
{
	private final static Log _log = LogFactory.getLog(FuncTemplate.class);
	
	private final Constructor<?> _constructor;
	private final Condition _attachCond;
	
	public final Stats stat;
	public final int order;
	public final Lambda lambda;
	public final Condition applayCond;
	
	public FuncTemplate(Condition pAttachCond, Condition pApplayCond, String pFunc, Stats pStat, int pOrder, Lambda pLambda)
	{
		_attachCond = pAttachCond;
		
		stat = pStat;
		order = pOrder;
		lambda = pLambda;
		applayCond = pApplayCond;
		
		try
		{
			_constructor = Class.forName("com.l2jfree.gameserver.skills.funcs.Func" + pFunc).getConstructor(
				Stats.class, Integer.TYPE, FuncOwner.class, Lambda.class, Condition.class);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public Func getFunc(Env env, FuncOwner funcOwner)
	{
		try
		{
			if (_attachCond == null || _attachCond.test(env))
				return (Func)_constructor.newInstance(stat, order, funcOwner, lambda, applayCond);
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}
		
		return null;
	}
}
