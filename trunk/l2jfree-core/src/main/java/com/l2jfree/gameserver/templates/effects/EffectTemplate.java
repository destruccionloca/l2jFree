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
package com.l2jfree.gameserver.templates.effects;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.conditions.Condition;
import com.l2jfree.gameserver.skills.funcs.FuncTemplate;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * @author mkizub
 */
public final class EffectTemplate
{
	private static final Log		_log	= LogFactory.getLog(EffectTemplate.class);

	private final Constructor<?>	_constructor;
	private final Constructor<?>	_stolenConstructor;
	private final Condition			_attachCond;

	public final String				name;
	public final double				lambda;
	public final int				count;
	public final int				period;
	public final int				abnormalEffect;
	public final String				stackType;
	public final float				stackOrder;
	public final boolean			showIcon;
	public final double				effectPower;										// to thandle chance
	public final L2SkillType		effectType;										// to handle resistences etc...

	public FuncTemplate[]			funcTemplates;

	public EffectTemplate(Condition pAttachCond, String pName, double pLambda, int pCount, int pPeriod, int pAbnormalEffect, String pStackType, float pStackOrder, boolean pShowIcon, double ePower, L2SkillType eType)
	{
		_attachCond = pAttachCond;

		name = pName;
		lambda = pLambda;
		count = pCount;
		period = pPeriod;
		abnormalEffect = pAbnormalEffect;
		stackType = pStackType.intern();
		stackOrder = pStackOrder;
		showIcon = pShowIcon;
		effectPower = ePower;
		effectType = eType;
		try
		{
			final Class<?> clazz = Class.forName("com.l2jfree.gameserver.skills.effects.Effect" + name);

			_constructor = clazz.getConstructor(Env.class, EffectTemplate.class);

			Constructor<?> stolenConstructor = null;
			try
			{
				stolenConstructor = clazz.getConstructor(Env.class, L2Effect.class);
			}
			catch (NoSuchMethodException e)
			{
			}
			_stolenConstructor = stolenConstructor;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public L2Effect getEffect(Env env)
	{
		try
		{
			if (_attachCond == null || _attachCond.test(env))
				return (L2Effect) _constructor.newInstance(env, this);
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}

		return null;
	}

	public L2Effect getStolenEffect(Env env, L2Effect stolen)
	{
		try
		{
			if (_stolenConstructor != null)
				return (L2Effect) _stolenConstructor.newInstance(env, stolen);
		}
		catch (Exception e)
		{
			_log.warn("", e);
		}

		return null;
	}

	public void attach(FuncTemplate f)
	{
		if (funcTemplates == null)
			funcTemplates = new FuncTemplate[1];
		else
			funcTemplates = Arrays.copyOf(funcTemplates, funcTemplates.length + 1);

		funcTemplates[funcTemplates.length - 1] = f;
	}
}
