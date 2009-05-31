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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.skills.conditions.Condition;
import com.l2jfree.gameserver.skills.effects.EffectTemplate;
import com.l2jfree.gameserver.templates.StatsSet;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.util.ObjectPool;

/**
 * @author mkizub
 */
final class DocumentSkill extends DocumentBase
{
	private static final class StatsSetPool extends ObjectPool<StatsSet>
	{
		private StatsSetPool()
		{
			super(false);
		}

		@Override
		protected StatsSet create()
		{
			return new StatsSet();
		}

		@Override
		protected void reset(StatsSet set)
		{
			set.getSet().clear();
		}

		private StatsSet getSkillSet(int level)
		{
			StatsSet set = get();

			set.set("skill_id", _currentSkillId);
			set.set("level", level);
			set.set("name", _currentSkillName);

			return set;
		}
	}

	private static final StatsSetPool			STATS_SET_POOL		= new StatsSetPool();

	private static final String[]				VALID_NODE_NAMES	= { "set", "for", "cond", // ...
			"enchant1",
			"enchant1for",
			"enchant1cond", // ...
			"enchant2",
			"enchant2for",
			"enchant2cond", // ...
			"enchant3",
			"enchant3for",
			"enchant3cond", // ...
			"enchant4",
			"enchant4for",
			"enchant4cond", // ...
			"enchant5",
			"enchant5for",
			"enchant5cond", // ...
			"enchant6",
			"enchant6for",
			"enchant5cond", // ...
																	};

	private static int							_currentSkillId;
	private static int							_currentSkillLevel;
	private static String						_currentSkillName;

	private static final Map<String, String[]>	_tables				= new HashMap<String, String[]>();

	private static final List<StatsSet>			_sets				= new ArrayList<StatsSet>();
	private static final List<StatsSet>			_enchsets1			= new ArrayList<StatsSet>();
	private static final List<StatsSet>			_enchsets2			= new ArrayList<StatsSet>();
	private static final List<StatsSet>			_enchsets3			= new ArrayList<StatsSet>();
	private static final List<StatsSet>			_enchsets4			= new ArrayList<StatsSet>();
	private static final List<StatsSet>			_enchsets5			= new ArrayList<StatsSet>();
	private static final List<StatsSet>			_enchsets6			= new ArrayList<StatsSet>();

	private static final List<L2Skill>			_skills				= new ArrayList<L2Skill>();

	private final List<L2Skill>					_skillsInFile		= new ArrayList<L2Skill>();

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
			return _tables.get(value)[(Integer) template - 1];
		else
			return _tables.get(value)[_currentSkillLevel];
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

			_skills.clear();
		}
	}

	private void clear(List<StatsSet> statsSets)
	{
		for (StatsSet set : statsSets)
			STATS_SET_POOL.store(set);

		statsSets.clear();
	}

	private void parseSkill(Node n) throws Exception
	{
		final NamedNodeMap attrs = n.getAttributes();

		_currentSkillId = Integer.decode(attrs.getNamedItem("id").getNodeValue());
		_currentSkillName = attrs.getNamedItem("name").getNodeValue();

		final int levels = getLevel(attrs, "levels", null);
		final int enchantLevels1 = getLevel(attrs, "enchantLevels1", 0);
		final int enchantLevels2 = getLevel(attrs, "enchantLevels2", 0);
		final int enchantLevels3 = getLevel(attrs, "enchantLevels3", 0);
		final int enchantLevels4 = getLevel(attrs, "enchantLevels4", 0);
		final int enchantLevels5 = getLevel(attrs, "enchantLevels5", 0);
		final int enchantLevels6 = getLevel(attrs, "enchantLevels6", 0);

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

				_tables.put(name, table);
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

		makeSkills(_sets);
		makeSkills(_enchsets1);
		makeSkills(_enchsets2);
		makeSkills(_enchsets3);
		makeSkills(_enchsets4);
		makeSkills(_enchsets5);
		makeSkills(_enchsets6);

		int startLvl = 0;

		attach(first, startLvl += 0, levels, "cond", "for");
		attach(first, startLvl += levels, enchantLevels1, "enchant1cond", "enchant1for");
		attach(first, startLvl += enchantLevels1, enchantLevels2, "enchant2cond", "enchant2for");
		attach(first, startLvl += enchantLevels2, enchantLevels3, "enchant3cond", "enchant3for");
		attach(first, startLvl += enchantLevels3, enchantLevels4, "enchant4cond", "enchant4for");
		attach(first, startLvl += enchantLevels4, enchantLevels5, "enchant5cond", "enchant5for");
		attach(first, startLvl += enchantLevels4, enchantLevels5, "enchant6cond", "enchant6for");
	}

	private int getLevel(NamedNodeMap attrs, String nodeName, Integer defaultValue)
	{
		if (attrs.getNamedItem(nodeName) != null)
			return Integer.decode(attrs.getNamedItem(nodeName).getNodeValue());

		return defaultValue.intValue();
	}

	private void parseBeanSets(Node first, int length, int startLvl, List<StatsSet> statsSets, String setName)
	{
		for (int i = 0; i < length; i++)
		{
			StatsSet set = STATS_SET_POOL.getSkillSet(i + startLvl);

			statsSets.add(set);

			if (startLvl >= 100)
			{
				for (Node n = first; n != null; n = n.getNextSibling())
				{
					if ("set".equalsIgnoreCase(n.getNodeName()))
						parseBeanSet(n, set, _sets.size());
				}
			}

			for (Node n = first; n != null; n = n.getNextSibling())
			{
				if (setName.equalsIgnoreCase(n.getNodeName()))
					parseBeanSet(n, set, i + 1);
			}
		}
	}

	private void parseBeanSet(Node n, StatsSet set, int level)
	{
		String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
		String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();

		set.set(name, getValue(value, level));
	}

	private void makeSkills(List<StatsSet> statsSets) throws Exception
	{
		for (StatsSet set : statsSets)
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
	void parseTemplateNode(Node n, Object template, Condition condition)
	{
		if ("effect".equalsIgnoreCase(n.getNodeName()))
			attachEffect(n, template, condition);
		else
			super.parseTemplateNode(n, template, condition);
	}

	final void attachEffect(Node n, Object template, Condition attachCond)
	{
		if (!(template instanceof L2Skill))
			throw new IllegalStateException("Attaching an effect to a non-L2Skill template");

		final L2Skill skill = (L2Skill) template;
		final NamedNodeMap attrs = n.getAttributes();

		final String name = attrs.getNamedItem("name").getNodeValue();

		int count = 1;
		if (attrs.getNamedItem("count") != null)
			count = Integer.decode(getValue(attrs.getNamedItem("count").getNodeValue(), template));

		count = Math.max(1, count);

		final int time = Integer.decode(getValue(attrs.getNamedItem("time").getNodeValue(), template)) * skill.getTimeMulti();

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
			String abn = attrs.getNamedItem("abnormal").getNodeValue();

			if (abn.equals("poison"))
				abnormal = L2Character.ABNORMAL_EFFECT_POISON;

			else if (abn.equals("bleed"))
				abnormal = L2Character.ABNORMAL_EFFECT_BLEEDING;
			else if (abn.equalsIgnoreCase("stun"))
				abnormal = L2Character.ABNORMAL_EFFECT_STUN;
			else if (abn.equalsIgnoreCase("dancestun"))
				abnormal = L2Character.ABNORMAL_EFFECT_DANCE_STUNNED;
			else if (abn.equalsIgnoreCase("sleep"))
				abnormal = L2Character.ABNORMAL_EFFECT_SLEEP;
			else if (abn.equalsIgnoreCase("redcircle"))
				abnormal |= L2Character.ABNORMAL_EFFECT_REDCIRCLE;
			else if (abn.equalsIgnoreCase("ice"))
				abnormal |= L2Character.ABNORMAL_EFFECT_ICE;
			else if (abn.equalsIgnoreCase("wind"))
				abnormal |= L2Character.ABNORMAL_EFFECT_WIND;
			else if (abn.equalsIgnoreCase("flame"))
				abnormal |= L2Character.ABNORMAL_EFFECT_FLAME;
			else if (abn.equalsIgnoreCase("stun"))
				abnormal |= L2Character.ABNORMAL_EFFECT_STUN;
			else if (abn.equalsIgnoreCase("bighead"))
				abnormal |= L2Character.ABNORMAL_EFFECT_BIG_HEAD;
			else if (abn.equalsIgnoreCase("stealth"))
				abnormal |= L2Character.ABNORMAL_EFFECT_STEALTH;
			else if (abn.equalsIgnoreCase("invul"))
				abnormal |= L2Character.ABNORMAL_EFFECT_INVULNERABLE;
			else if (abn.equalsIgnoreCase("root"))
				abnormal = L2Character.ABNORMAL_EFFECT_ROOT;
			else if (abn.equalsIgnoreCase("mute"))
				abnormal = L2Character.ABNORMAL_EFFECT_MUTED;
			else if (abn.equalsIgnoreCase("earthquake"))
				abnormal = L2Character.ABNORMAL_EFFECT_EARTHQUAKE;
			else if (abn.equalsIgnoreCase("vitality"))
				abnormal = L2Character.ABNORMAL_EFFECT_VITALITY;
			else
				throw new IllegalStateException("Invalid abnormal value: '" + abn + "'!");
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

		EffectTemplate effectTemplate = new EffectTemplate(attachCond, name, lambda, count, time, abnormal, stackType, stackOrder, showIcon);

		parseTemplate(n, effectTemplate);

		if (!self)
			skill.attach(effectTemplate);
		else
			skill.attachSelf(effectTemplate);
	}
}
