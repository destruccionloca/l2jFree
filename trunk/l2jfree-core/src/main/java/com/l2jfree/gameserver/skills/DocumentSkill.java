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
package com.l2jfree.gameserver.skills;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.templates.ValidatingStatsSet;
import com.l2jfree.gameserver.templates.effects.EffectTemplate;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.util.ObjectPool;

/**
 * @author mkizub
 */
final class DocumentSkill extends DocumentBase
{
	private final class ValidatingStatsSetPool extends ObjectPool<ValidatingStatsSet>
	{
		@Override
		protected ValidatingStatsSet create()
		{
			return new ValidatingStatsSet();
		}
		
		@Override
		protected void reset(ValidatingStatsSet set)
		{
			set.clear();
		}
		
		private ValidatingStatsSet getSkillSet(int level)
		{
			final ValidatingStatsSet set = get();
			set.setDescription("Skill ID: " + _currentSkillId + ", Lvl: " + level + ", Name: " + _currentSkillName);
			set.set("skill_id", _currentSkillId);
			set.set("level", level);
			set.set("name", _currentSkillName);
			
			return set;
		}
	}
	
	private final ValidatingStatsSetPool STATS_SET_POOL = new ValidatingStatsSetPool();
	
	private static final String[] VALID_NODE_NAMES = { "set", "for", "cond", // ...
		"enchant1", "enchant1for", "enchant1cond", // ...
		"enchant2", "enchant2for", "enchant2cond", // ...
		"enchant3", "enchant3for", "enchant3cond", // ...
		"enchant4", "enchant4for", "enchant4cond", // ...
		"enchant5", "enchant5for", "enchant5cond", // ...
		"enchant6", "enchant6for", "enchant6cond", // ...
		"enchant7", "enchant7for", "enchant7cond", // ...
		"enchant8", "enchant8for", "enchant8cond", // ...
	};
	
	private int _currentSkillId;
	private int _currentSkillLevel;
	private String _currentSkillName;
	
	private final ValidatingStatsSet _tables = new ValidatingStatsSet();
	
	private final List<ValidatingStatsSet> _sets = new ArrayList<ValidatingStatsSet>();
	private final List<ValidatingStatsSet> _enchsets1 = new ArrayList<ValidatingStatsSet>();
	private final List<ValidatingStatsSet> _enchsets2 = new ArrayList<ValidatingStatsSet>();
	private final List<ValidatingStatsSet> _enchsets3 = new ArrayList<ValidatingStatsSet>();
	private final List<ValidatingStatsSet> _enchsets4 = new ArrayList<ValidatingStatsSet>();
	private final List<ValidatingStatsSet> _enchsets5 = new ArrayList<ValidatingStatsSet>();
	private final List<ValidatingStatsSet> _enchsets6 = new ArrayList<ValidatingStatsSet>();
	private final List<ValidatingStatsSet> _enchsets7 = new ArrayList<ValidatingStatsSet>();
	private final List<ValidatingStatsSet> _enchsets8 = new ArrayList<ValidatingStatsSet>();
	
	private final List<L2Skill> _skills = new ArrayList<L2Skill>();
	
	private final List<L2Skill> _skillsInFile = new ArrayList<L2Skill>();
	
	DocumentSkill(File file)
	{
		super(file);
	}
	
	List<L2Skill> getSkills()
	{
		return _skillsInFile;
	}
	
	@Override
	String getTableValue(String value, Object template)
	{
		if (template instanceof Integer)
			return _tables.getStringArray(value)[(Integer)template - 1];
		else
			return _tables.getStringArray(value)[_currentSkillLevel];
	}
	
	@Override
	String getDefaultNodeName()
	{
		return "skill";
	}
	
	@Override
	void parseDefaultNode(Node n)
	{
		try
		{
			parseSkill(n);
			
			_skillsInFile.addAll(_skills);
		}
		catch (Exception e)
		{
			_log.warn("Error while parsing skill id " + _currentSkillId + ", level " + (_currentSkillLevel + 1), e);
		}
		finally
		{
			_currentSkillId = 0;
			_currentSkillLevel = 0;
			_currentSkillName = null;
			
			_tables.clear();
			
			clear(_sets);
			clear(_enchsets1);
			clear(_enchsets2);
			clear(_enchsets3);
			clear(_enchsets4);
			clear(_enchsets5);
			clear(_enchsets6);
			clear(_enchsets7);
			clear(_enchsets8);
			
			_skills.clear();
		}
	}
	
	private void clear(List<ValidatingStatsSet> statsSets)
	{
		for (ValidatingStatsSet set : statsSets)
			STATS_SET_POOL.store(set);
		
		statsSets.clear();
	}
	
	private void parseSkill(Node n) throws Exception
	{
		final NamedNodeMap attrs = n.getAttributes();
		
		_currentSkillId = Integer.decode(attrs.getNamedItem("id").getNodeValue());
		_currentSkillName = attrs.getNamedItem("name").getNodeValue();
		_tables.setDescription("Skill ID: " + _currentSkillId + ", Name: " + _currentSkillName);
		
		final int levels = getLevel(attrs, "levels", null);
		final int enchantLevels1 = getLevel(attrs, "enchantLevels1", 0);
		final int enchantLevels2 = getLevel(attrs, "enchantLevels2", 0);
		final int enchantLevels3 = getLevel(attrs, "enchantLevels3", 0);
		final int enchantLevels4 = getLevel(attrs, "enchantLevels4", 0);
		final int enchantLevels5 = getLevel(attrs, "enchantLevels5", 0);
		final int enchantLevels6 = getLevel(attrs, "enchantLevels6", 0);
		final int enchantLevels7 = getLevel(attrs, "enchantLevels7", 0);
		final int enchantLevels8 = getLevel(attrs, "enchantLevels8", 0);
		
		final Node first = n.getFirstChild();
		
		node_loop: for (n = first; n != null; n = n.getNextSibling())
		{
			if ("table".equalsIgnoreCase(n.getNodeName()))
			{
				String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
				
				if (name.charAt(0) != '#')
					throw new IllegalStateException("Table name must start with '#'!");
				
				StringTokenizer st = new StringTokenizer(n.getFirstChild().getNodeValue());
				
				String[] table = new String[st.countTokens()];
				
				for (int i = 0; i < table.length; i++)
					table[i] = st.nextToken();
				
				_tables.set(name, table);
			}
			else if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				final String name = n.getNodeName();
				
				for (String validName : VALID_NODE_NAMES)
					if (validName.equals(name))
						continue node_loop;
				
				throw new IllegalStateException("Invalid tag <" + n.getNodeName() + ">");
			}
		}
		
		parseBeanSets(first, levels, 1, _sets, "set");
		parseBeanSets(first, enchantLevels1, 101, _enchsets1, "enchant1");
		parseBeanSets(first, enchantLevels2, 201, _enchsets2, "enchant2");
		parseBeanSets(first, enchantLevels3, 301, _enchsets3, "enchant3");
		parseBeanSets(first, enchantLevels4, 401, _enchsets4, "enchant4");
		parseBeanSets(first, enchantLevels5, 501, _enchsets5, "enchant5");
		parseBeanSets(first, enchantLevels6, 601, _enchsets6, "enchant6");
		parseBeanSets(first, enchantLevels7, 701, _enchsets7, "enchant7");
		parseBeanSets(first, enchantLevels8, 801, _enchsets8, "enchant8");
		
		makeSkills(_sets);
		makeSkills(_enchsets1);
		makeSkills(_enchsets2);
		makeSkills(_enchsets3);
		makeSkills(_enchsets4);
		makeSkills(_enchsets5);
		makeSkills(_enchsets6);
		makeSkills(_enchsets7);
		makeSkills(_enchsets8);
		
		int startLvl = 0;
		
		attach(first, startLvl += 0, levels, "cond", "for");
		attach(first, startLvl += levels, enchantLevels1, "enchant1cond", "enchant1for");
		attach(first, startLvl += enchantLevels1, enchantLevels2, "enchant2cond", "enchant2for");
		attach(first, startLvl += enchantLevels2, enchantLevels3, "enchant3cond", "enchant3for");
		attach(first, startLvl += enchantLevels3, enchantLevels4, "enchant4cond", "enchant4for");
		attach(first, startLvl += enchantLevels4, enchantLevels5, "enchant5cond", "enchant5for");
		attach(first, startLvl += enchantLevels5, enchantLevels6, "enchant6cond", "enchant6for");
		attach(first, startLvl += enchantLevels6, enchantLevels7, "enchant7cond", "enchant7for");
		attach(first, startLvl += enchantLevels7, enchantLevels8, "enchant8cond", "enchant8for");
	}
	
	private int getLevel(NamedNodeMap attrs, String nodeName, Integer defaultValue)
	{
		if (attrs.getNamedItem(nodeName) != null)
			return Integer.decode(attrs.getNamedItem(nodeName).getNodeValue());
		
		return defaultValue.intValue();
	}
	
	private void parseBeanSets(Node first, int length, int startLvl, List<ValidatingStatsSet> statsSets, String setName)
	{
		for (int i = 0; i < length; i++)
		{
			ValidatingStatsSet set = STATS_SET_POOL.getSkillSet(i + startLvl);
			
			statsSets.add(set);
			
			boolean isEnchant = false;
			
			if (startLvl >= 100)
			{
				isEnchant = true;
				
				for (Node n = first; n != null; n = n.getNextSibling())
				{
					if ("set".equalsIgnoreCase(n.getNodeName()))
						parseBeanSet(n, set, _sets.size(), false);
				}
			}
			
			for (Node n = first; n != null; n = n.getNextSibling())
			{
				if (setName.equalsIgnoreCase(n.getNodeName()))
					parseBeanSet(n, set, i + 1, isEnchant);
			}
		}
	}
	
	private void parseBeanSet(Node n, ValidatingStatsSet set, int level, boolean isEnchant)
	{
		String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
		String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
		
		set.setValidating(!isEnchant);
		set.set(name, getValue(value, level));
		set.setValidating(true);
	}
	
	private void makeSkills(List<ValidatingStatsSet> statsSets) throws Exception
	{
		for (ValidatingStatsSet set : statsSets)
			_skills.add(set.getEnum("skillType", L2SkillType.class).makeSkill(set));
	}
	
	private void attach(final Node first, final int startLvl, final int length, String condName, String forName)
	{
		for (int i = 0; i < length; i++)
		{
			final L2Skill skill = _skills.get(startLvl + i);
			
			_currentSkillLevel = i;
			
			boolean found = false;
			for (Node n = first; n != null; n = n.getNextSibling())
			{
				if (condName.equalsIgnoreCase(n.getNodeName()))
				{
					found = true;
					skill.attach(parseConditionWithMessage(n, skill));
				}
				else if (forName.equalsIgnoreCase(n.getNodeName()))
				{
					found = true;
					parseTemplate(n, skill);
				}
			}
			
			if (!found && startLvl > 0)
			{
				_currentSkillLevel = _sets.size() - 1;
				
				for (Node n = first; n != null; n = n.getNextSibling())
				{
					if ("cond".equalsIgnoreCase(n.getNodeName()))
					{
						skill.attach(parseConditionWithMessage(n, skill));
					}
					else if ("for".equalsIgnoreCase(n.getNodeName()))
					{
						parseTemplate(n, skill);
					}
				}
			}
		}
	}
	
	@Override
	void parseTemplateNode(Node n, Object template)
	{
		if ("effect".equalsIgnoreCase(n.getNodeName()))
			attachEffect(n, template);
		else
			super.parseTemplateNode(n, template);
	}
	
	final void attachEffect(Node n, Object template)
	{
		if (!(template instanceof L2Skill))
			throw new IllegalStateException("Attaching an effect to a non-L2Skill template");
		
		final L2Skill skill = (L2Skill)template;
		final NamedNodeMap attrs = n.getAttributes();
		
		final String name = attrs.getNamedItem("name").getNodeValue();
		
		int count = 1;
		if (attrs.getNamedItem("count") != null)
			count = Integer.decode(getValue(attrs.getNamedItem("count").getNodeValue(), template));
		
		count = Math.max(1, count);
		
		int time = 1;
		if (attrs.getNamedItem("time") != null)
			time = Integer.decode(getValue(attrs.getNamedItem("time").getNodeValue(), template)) * skill.getTimeMulti();
		
		if (time < 0)
		{
			if (count == 1)
				time = (int)TimeUnit.DAYS.toSeconds(10); // 'infinite' - still in integer range, even in msec
			else
				throw new IllegalStateException("Invalid count (> 1) for effect with infinite duration!");
		}
		else
			time = Math.max(1, time);
		
		boolean self = false;
		if (attrs.getNamedItem("self") != null)
			self = (Integer.decode(getValue(attrs.getNamedItem("self").getNodeValue(), template)) == 1);
		
		boolean showIcon = true;
		if (attrs.getNamedItem("noicon") != null)
			showIcon = !(Integer.decode(getValue(attrs.getNamedItem("noicon").getNodeValue(), template)) == 1);
		
		final double lambda = getLambda(n, template);
		
		int abnormal = 0;
		if (attrs.getNamedItem("abnormal") != null)
		{
			String abn = attrs.getNamedItem("abnormal").getNodeValue().toLowerCase();
			
			if (abn.equals("bleed") || abn.equals("bleeding"))
				abnormal = L2Character.ABNORMAL_EFFECT_BLEEDING;
			else if (abn.equals("poison"))
				abnormal = L2Character.ABNORMAL_EFFECT_POISON;
			else if (abn.equals("redcircle"))
				abnormal = L2Character.ABNORMAL_EFFECT_REDCIRCLE;
			else if (abn.equals("ice"))
				abnormal = L2Character.ABNORMAL_EFFECT_ICE;
			else if (abn.equals("wind"))
				abnormal = L2Character.ABNORMAL_EFFECT_WIND;
			else if (abn.equals("fear"))
				abnormal = L2Character.ABNORMAL_EFFECT_FEAR;
			else if (abn.equals("stun"))
				abnormal = L2Character.ABNORMAL_EFFECT_STUN;
			else if (abn.equals("sleep"))
				abnormal = L2Character.ABNORMAL_EFFECT_SLEEP;
			else if (abn.equals("mute"))
				abnormal = L2Character.ABNORMAL_EFFECT_MUTED;
			else if (abn.equals("root"))
				abnormal = L2Character.ABNORMAL_EFFECT_ROOT;
			else if (abn.equals("hold1"))
				abnormal = L2Character.ABNORMAL_EFFECT_HOLD_1;
			else if (abn.equals("hold2"))
				abnormal = L2Character.ABNORMAL_EFFECT_HOLD_2;
			else if (abn.equals("unknown13"))
				abnormal = L2Character.ABNORMAL_EFFECT_UNKNOWN_13;
			else if (abn.equals("bighead"))
				abnormal = L2Character.ABNORMAL_EFFECT_BIG_HEAD;
			else if (abn.equals("flame"))
				abnormal = L2Character.ABNORMAL_EFFECT_FLAME;
			else if (abn.equals("unknown16"))
				abnormal = L2Character.ABNORMAL_EFFECT_UNKNOWN_16;
			else if (abn.equals("grow"))
				abnormal = L2Character.ABNORMAL_EFFECT_GROW;
			else if (abn.equals("floatroot"))
				abnormal = L2Character.ABNORMAL_EFFECT_FLOATING_ROOT;
			else if (abn.equals("dancestun"))
				abnormal = L2Character.ABNORMAL_EFFECT_DANCE_STUNNED;
			else if (abn.equals("firerootstun"))
				abnormal = L2Character.ABNORMAL_EFFECT_FIREROOT_STUN;
			else if (abn.equals("stealth"))
				abnormal = L2Character.ABNORMAL_EFFECT_STEALTH;
			else if (abn.equals("imprison1"))
				abnormal = L2Character.ABNORMAL_EFFECT_IMPRISIONING_1;
			else if (abn.equals("imprison2"))
				abnormal = L2Character.ABNORMAL_EFFECT_IMPRISIONING_2;
			else if (abn.equals("magiccircle"))
				abnormal = L2Character.ABNORMAL_EFFECT_MAGIC_CIRCLE;
			else if (abn.equals("ice2"))
				abnormal = L2Character.ABNORMAL_EFFECT_ICE2;
			else if (abn.equals("earthquake"))
				abnormal = L2Character.ABNORMAL_EFFECT_EARTHQUAKE;
			else if (abn.equals("unknown27"))
				abnormal = L2Character.ABNORMAL_EFFECT_UNKNOWN_27;
			else if (abn.equals("invulnerable"))
				abnormal = L2Character.ABNORMAL_EFFECT_INVULNERABLE;
			else if (abn.equals("vitality"))
				abnormal = L2Character.ABNORMAL_EFFECT_VITALITY;
			else if (abn.equals("unknown30"))
				abnormal = L2Character.ABNORMAL_EFFECT_UNKNOWN_30;
			else if (abn.equals("deathmark"))
				abnormal = L2Character.ABNORMAL_EFFECT_DEATH_MARK;
			else if (abn.equals("unknown32"))
				abnormal = L2Character.ABNORMAL_EFFECT_UNKNOWN_32;
			else
				throw new IllegalStateException("Invalid abnormal value: '" + abn + "'!");
		}

		int special = 0;
		if (attrs.getNamedItem("special") != null)
		{
			String spc = attrs.getNamedItem("special").getNodeValue().toLowerCase();

			if (spc.equals("invulnerable"))
				special = L2Character.SPECIAL_EFFECT_INVULNERABLE;
			else if (spc.equals("redglow"))
				special = L2Character.SPECIAL_EFFECT_RED_GLOW;
			else if (spc.equals("redglow2"))
				special = L2Character.SPECIAL_EFFECT_RED_GLOW2;
			else if (spc.equals("baguettesword"))
				special = L2Character.SPECIAL_EFFECT_BAGUETTE_SWORD;
			else if (spc.equals("yellowafro"))
				special = L2Character.SPECIAL_EFFECT_YELLOW_AFFRO;
			else if (spc.equals("pinkafro"))
				special = L2Character.SPECIAL_EFFECT_PINK_AFFRO;
			else if (spc.equals("blackafro"))
				special = L2Character.SPECIAL_EFFECT_BLACK_AFFRO;
			else if (spc.equals("unknown8"))
				special = L2Character.SPECIAL_EFFECT_UNKNOWN8;
			else if (spc.equals("unknown9"))
				special = L2Character.SPECIAL_EFFECT_UNKNOWN9;
			else
				throw new IllegalStateException("Invalid special value: '" + spc + "'!");
		}

		
		final String stackType;
		if (attrs.getNamedItem("stackType") != null)
			stackType = attrs.getNamedItem("stackType").getNodeValue();
		else
			stackType = skill.generateUniqueStackType();
		
		float stackOrder;
		if (attrs.getNamedItem("stackOrder") != null)
			stackOrder = Float.parseFloat(getValue(attrs.getNamedItem("stackOrder").getNodeValue(), template));
		else
			stackOrder = skill.generateStackOrder();
		
		double effectPower = -1;
		if (attrs.getNamedItem("effectPower") != null)
			effectPower = Double.parseDouble(getValue(attrs.getNamedItem("effectPower").getNodeValue(), template));
		
		L2SkillType effectType = null;
		if (attrs.getNamedItem("effectType") != null)
		{
			String typeName = getValue(attrs.getNamedItem("effectType").getNodeValue(), template);
			
			try
			{
				effectType = Enum.valueOf(L2SkillType.class, typeName);
			}
			catch (RuntimeException e)
			{
				throw new IllegalArgumentException("Not skilltype found for: " + typeName, e);
			}
		}
		
		if ((effectPower == -1) != (effectType == null))
			throw new IllegalArgumentException("Missing effectType/effectPower for effect: " + name);
		
		Integer trigId = null;
		if (attrs.getNamedItem("triggeredId") != null)
			trigId = Integer.parseInt(getValue(attrs.getNamedItem("triggeredId").getNodeValue(), template));
		
		Integer trigLvl = null;
		if (attrs.getNamedItem("triggeredLevel") != null)
			trigLvl = Integer.parseInt(getValue(attrs.getNamedItem("triggeredLevel").getNodeValue(), template));
		
		String chanceType = null;
		if (attrs.getNamedItem("chanceType") != null)
			chanceType = getValue(attrs.getNamedItem("chanceType").getNodeValue(), template);
		
		Integer activationChance = null;
		if (attrs.getNamedItem("activationChance") != null)
			activationChance = Integer.parseInt(getValue(attrs.getNamedItem("activationChance").getNodeValue(),
				template));
		
		final TriggeredSkill trigSkill = TriggeredSkill.parse(trigId, trigLvl);
		final ChanceCondition chanceCond = ChanceCondition.parse(chanceType, activationChance);
		
		if ("ChanceSkillTrigger".equals(name))
		{
			if (trigSkill == null)
				throw new NoSuchElementException(name + " requires proper TriggeredSkill parameters!");
			
			if (chanceCond == null)
				throw new NoSuchElementException(name + " requires proper ChanceCondition parameters!");
		}
		else
		{
			if (trigSkill != null)
				throw new NoSuchElementException(name + " can't have TriggeredSkill parameters!");
			
			if (chanceCond != null)
				throw new NoSuchElementException(name + " can't have ChanceCondition parameters!");
		}
		
		EffectTemplate effectTemplate = new EffectTemplate(name, lambda, count, time, abnormal, special, stackType,
			stackOrder, showIcon, effectPower, effectType, trigSkill, chanceCond);
		
		parseTemplate(n, effectTemplate);
		
		if (!self)
			skill.attach(effectTemplate);
		else
			skill.attachSelf(effectTemplate);
	}
}
