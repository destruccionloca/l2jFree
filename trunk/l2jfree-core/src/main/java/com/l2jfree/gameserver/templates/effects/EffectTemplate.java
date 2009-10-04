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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.skills.ChanceCondition;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.TriggeredSkill;
import com.l2jfree.gameserver.skills.funcs.FuncTemplate;
import com.l2jfree.gameserver.templates.StatsSet;
import com.l2jfree.gameserver.templates.skills.L2SkillType;

/**
 * @author mkizub
 */
public final class EffectTemplate
{
	private static final Log _log = LogFactory.getLog(EffectTemplate.class);
	
	private static final Map<String, Integer> ABNORMAL_EFFECTS = new HashMap<String, Integer>();
	private static final Map<String, Integer> SPECIAL_EFFECTS = new HashMap<String, Integer>();
	
	static
	{
		ABNORMAL_EFFECTS.put("bleed", L2Character.ABNORMAL_EFFECT_BLEEDING);
		ABNORMAL_EFFECTS.put("bleeding", L2Character.ABNORMAL_EFFECT_BLEEDING);
		ABNORMAL_EFFECTS.put("poison", L2Character.ABNORMAL_EFFECT_POISON);
		ABNORMAL_EFFECTS.put("redcircle", L2Character.ABNORMAL_EFFECT_REDCIRCLE);
		ABNORMAL_EFFECTS.put("ice", L2Character.ABNORMAL_EFFECT_ICE);
		ABNORMAL_EFFECTS.put("wind", L2Character.ABNORMAL_EFFECT_WIND);
		ABNORMAL_EFFECTS.put("fear", L2Character.ABNORMAL_EFFECT_FEAR);
		ABNORMAL_EFFECTS.put("stun", L2Character.ABNORMAL_EFFECT_STUN);
		ABNORMAL_EFFECTS.put("sleep", L2Character.ABNORMAL_EFFECT_SLEEP);
		ABNORMAL_EFFECTS.put("mute", L2Character.ABNORMAL_EFFECT_MUTED);
		ABNORMAL_EFFECTS.put("root", L2Character.ABNORMAL_EFFECT_ROOT);
		ABNORMAL_EFFECTS.put("hold1", L2Character.ABNORMAL_EFFECT_HOLD_1);
		ABNORMAL_EFFECTS.put("hold2", L2Character.ABNORMAL_EFFECT_HOLD_2);
		ABNORMAL_EFFECTS.put("unknown13", L2Character.ABNORMAL_EFFECT_UNKNOWN_13);
		ABNORMAL_EFFECTS.put("bighead", L2Character.ABNORMAL_EFFECT_BIG_HEAD);
		ABNORMAL_EFFECTS.put("flame", L2Character.ABNORMAL_EFFECT_FLAME);
		ABNORMAL_EFFECTS.put("unknown16", L2Character.ABNORMAL_EFFECT_UNKNOWN_16);
		ABNORMAL_EFFECTS.put("grow", L2Character.ABNORMAL_EFFECT_GROW);
		ABNORMAL_EFFECTS.put("floatroot", L2Character.ABNORMAL_EFFECT_FLOATING_ROOT);
		ABNORMAL_EFFECTS.put("dancestun", L2Character.ABNORMAL_EFFECT_DANCE_STUNNED);
		ABNORMAL_EFFECTS.put("firerootstun", L2Character.ABNORMAL_EFFECT_FIREROOT_STUN);
		ABNORMAL_EFFECTS.put("stealth", L2Character.ABNORMAL_EFFECT_STEALTH);
		ABNORMAL_EFFECTS.put("imprison1", L2Character.ABNORMAL_EFFECT_IMPRISIONING_1);
		ABNORMAL_EFFECTS.put("imprison2", L2Character.ABNORMAL_EFFECT_IMPRISIONING_2);
		ABNORMAL_EFFECTS.put("magiccircle", L2Character.ABNORMAL_EFFECT_MAGIC_CIRCLE);
		ABNORMAL_EFFECTS.put("ice2", L2Character.ABNORMAL_EFFECT_ICE2);
		ABNORMAL_EFFECTS.put("earthquake", L2Character.ABNORMAL_EFFECT_EARTHQUAKE);
		ABNORMAL_EFFECTS.put("unknown27", L2Character.ABNORMAL_EFFECT_UNKNOWN_27);
		ABNORMAL_EFFECTS.put("invulnerable", L2Character.ABNORMAL_EFFECT_INVULNERABLE);
		ABNORMAL_EFFECTS.put("vitality", L2Character.ABNORMAL_EFFECT_VITALITY);
		ABNORMAL_EFFECTS.put("unknown30", L2Character.ABNORMAL_EFFECT_UNKNOWN_30);
		ABNORMAL_EFFECTS.put("deathmark", L2Character.ABNORMAL_EFFECT_DEATH_MARK);
		ABNORMAL_EFFECTS.put("unknown32", L2Character.ABNORMAL_EFFECT_UNKNOWN_32);
		
		SPECIAL_EFFECTS.put("invulnerable", L2Character.SPECIAL_EFFECT_INVULNERABLE);
		SPECIAL_EFFECTS.put("redglow", L2Character.SPECIAL_EFFECT_AIR_STUN);
		SPECIAL_EFFECTS.put("redglow2", L2Character.SPECIAL_EFFECT_AIR_ROOT);
		SPECIAL_EFFECTS.put("baguettesword", L2Character.SPECIAL_EFFECT_BAGUETTE_SWORD);
		SPECIAL_EFFECTS.put("yellowafro", L2Character.SPECIAL_EFFECT_YELLOW_AFFRO);
		SPECIAL_EFFECTS.put("pinkafro", L2Character.SPECIAL_EFFECT_PINK_AFFRO);
		SPECIAL_EFFECTS.put("blackafro", L2Character.SPECIAL_EFFECT_BLACK_AFFRO);
		SPECIAL_EFFECTS.put("unknown8", L2Character.SPECIAL_EFFECT_UNKNOWN8);
		SPECIAL_EFFECTS.put("unknown9", L2Character.SPECIAL_EFFECT_UNKNOWN9);
	}
	
	private final Constructor<?> _constructor;
	private final Constructor<?> _stolenConstructor;
	
	public final String name;
	public final double lambda;
	public final int count;
	public final int period;
	public int abnormalEffect;
	public int specialEffect;
	public String[] stackTypes;
	public float stackOrder;
	public boolean showIcon;
	public final double effectPower; // to handle chance
	public final L2SkillType effectType; // to handle resistances etc...
	
	public final TriggeredSkill triggeredSkill;
	public final ChanceCondition chanceCondition;
	
	public FuncTemplate[] funcTemplates;
	
	public EffectTemplate(StatsSet set, L2Skill skill)
	{
		name = set.getString("name");
		lambda = set.getDouble("val", 0);
		count = Math.max(1, set.getInteger("count", 1));
		
		int time = set.getInteger("time", 1) * skill.getTimeMulti();
		
		if (time < 0)
		{
			if (count == 1)
				period = (int)TimeUnit.DAYS.toSeconds(10); // 'infinite' - still in integer range, even in msec
			else
				throw new IllegalStateException("Invalid count (> 1) for effect with infinite duration!");
		}
		else
			period = Math.max(1, time);
		
		if (set.contains("abnormal"))
		{
			final String abnormal = set.getString("abnormal").toLowerCase();
			
			if (!ABNORMAL_EFFECTS.containsKey(abnormal))
				throw new IllegalStateException("Invalid abnormal value: '" + abnormal + "'!");
			
			abnormalEffect = ABNORMAL_EFFECTS.get(abnormal);
		}
		else
			abnormalEffect = 0;
		
		if (set.contains("special"))
		{
			final String special = set.getString("special").toLowerCase();
			
			if (!SPECIAL_EFFECTS.containsKey(special))
				throw new IllegalStateException("Invalid special value: '" + special + "'!");
			
			specialEffect = SPECIAL_EFFECTS.get(special);
		}
		else
			specialEffect = 0;
		
		stackTypes = set.getString("stackType", skill.generateUniqueStackType()).split(";");
		stackOrder = set.getFloat("stackOrder", skill.generateStackOrder());
		
		for (int i = 0; i < stackTypes.length; i++)
			stackTypes[i] = stackTypes[i].intern();
		
		if (stackTypes.length > 1 && stackOrder != 99)
			throw new IllegalStateException("'stackOrder' should be 99 for merged effects!");
		
		showIcon = set.getInteger("noicon", 0) == 0;
		
		effectPower = set.getDouble("effectPower", -1);
		effectType = set.getEnum("effectType", L2SkillType.class, null);
		
		if ((effectPower == -1) != (effectType == null))
			throw new IllegalArgumentException("Missing effectType/effectPower for effect: " + name);
		
		Integer trigId = null;
		if (set.contains("triggeredId"))
			trigId = set.getInteger("triggeredId");
		
		Integer trigLvl = null;
		if (set.contains("triggeredLevel"))
			trigLvl = set.getInteger("triggeredLevel");
		
		String chanceType = null;
		if (set.contains("chanceType"))
			chanceType = set.getString("chanceType");
		
		Integer activationChance = null;
		if (set.contains("activationChance"))
			activationChance = set.getInteger("activationChance");
		
		String activationElements = null;
		if (set.contains("activationElements"))
			activationElements = set.getString("activationElements");
		
		triggeredSkill = TriggeredSkill.parse(trigId, trigLvl);
		chanceCondition = ChanceCondition.parse(chanceType, activationChance, activationElements);
		
		if ("ChanceSkillTrigger".equals(name))
		{
			if (triggeredSkill == null)
				throw new NoSuchElementException(name + " requires proper TriggeredSkill parameters!");
			
			if (chanceCondition == null)
				throw new NoSuchElementException(name + " requires proper ChanceCondition parameters!");
		}
		else
		{
			if (triggeredSkill != null)
				throw new NoSuchElementException(name + " can't have TriggeredSkill parameters!");
			
			if (chanceCondition != null)
				throw new NoSuchElementException(name + " can't have ChanceCondition parameters!");
		}
		
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
			return (L2Effect)_constructor.newInstance(env, this);
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
				return (L2Effect)_stolenConstructor.newInstance(env, stolen);
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
	
	/**
	 * Support for improved buffs, in case it gets overwritten in DP
	 * 
	 * @param skill
	 * @param template
	 * @return
	 */
	public boolean merge(L2Skill skill, EffectTemplate template)
	{
		if (!name.equals(template.name))
			return false;
		
		if (lambda != 0 || template.lambda != 0)
			return false;
		
		if (count != template.count || period != template.period)
			return false;
		
		if (effectPower != template.effectPower || effectType != template.effectType)
			return false;
		
		if (triggeredSkill != null || template.triggeredSkill != null)
			return false;
		
		if (chanceCondition != null || template.chanceCondition != null)
			return false;
		
		abnormalEffect |= template.abnormalEffect;
		specialEffect |= template.specialEffect;
		
		final HashSet<String> tmp = new HashSet<String>();
		
		for (String s : stackTypes)
			tmp.add(s);
		
		for (String s : template.stackTypes)
			tmp.add(s);
		
		stackTypes = tmp.toArray(new String[tmp.size()]);
		stackOrder = 99;
		
		showIcon = showIcon || template.showIcon;
		
		for (FuncTemplate f : template.funcTemplates)
			attach(f);
		
		_log.info("Effect templates merged for " + skill);
		return true;
	}
}
