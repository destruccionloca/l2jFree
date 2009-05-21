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

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ScheduledFuture;


import javolution.util.FastList;
import javolution.util.FastMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.base.Race;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.conditions.Condition;
import com.l2jfree.gameserver.skills.conditions.ConditionGameChance;
import com.l2jfree.gameserver.skills.conditions.ConditionGameTime;
import com.l2jfree.gameserver.skills.conditions.ConditionLogicAnd;
import com.l2jfree.gameserver.skills.conditions.ConditionLogicNot;
import com.l2jfree.gameserver.skills.conditions.ConditionLogicOr;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerCp;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerHp;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerHpPercentage;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerLevel;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerMp;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerRace;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerState;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetActiveEffectId;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetActiveSkillId;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetAggro;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetClassIdRestriction;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetLevel;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetRaceId;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetUndead;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetUsesWeaponKind;
import com.l2jfree.gameserver.skills.conditions.ConditionGameTime.CheckGameTime;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import com.l2jfree.gameserver.templates.L2ArmorType;
import com.l2jfree.gameserver.templates.L2WeaponType;

public class L2ConditionalZone extends L2DefaultZone
{
	private ScheduledFuture<?> _task;
	private Condition _cond;
	private FastMap<Integer, L2Character> _checkedList;

	public L2ConditionalZone()
	{
		super();
		_checkedList = new FastMap<Integer, L2Character>().setShared(true);
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

	@Override
	public void revalidateInZone(L2Character character)
	{
		if (isInsideZone(character))
		{
			if (!_characterList.containsKey(character.getObjectId()))
			{
				_characterList.put(character.getObjectId(), character);
				// Timer turns on if characters are in zone
				if (_task == null)
					startTimerTask();
			}
		}
		else
		{
			if (_characterList.containsKey(character.getObjectId()))
			{
				_characterList.remove(character.getObjectId());
				// Timer turns off if zone is empty
				if (_characterList.size() == 0)
					stopTimerTask();
			}
		}
	}

	private synchronized void startTimerTask()
	{
		_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ConditionTask(), 1000, 1000);
	}

	private synchronized void stopTimerTask()
	{
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
	}

	private class ConditionTask implements Runnable
	{
		public void run()
		{
			for (L2Character character : getCharactersInside().values())
			{
				if (checkCondition(character))
				{
					if (!_checkedList.containsKey(character.getObjectId()))
					{
						_checkedList.put(character.getObjectId(), character);
						onEnter(character);
					}
				}
				else
				{
					if (_checkedList.containsKey(character.getObjectId()))
					{
						_checkedList.remove(character.getObjectId());
						onExit(character);
					}
				}
			}
		}
	}

	@Override
	protected void parseCondition(Node n) throws Exception
	{
		_cond = parseCondition(n, this);
	}

	private Condition parseCondition(Node n, Object template)
	{
		while (n != null && n.getNodeType() != Node.ELEMENT_NODE)
			n = n.getNextSibling();
		if (n == null)
			return null;
		if ("and".equalsIgnoreCase(n.getNodeName()))
			return parseLogicAnd(n, template);
		if ("or".equalsIgnoreCase(n.getNodeName()))
			return parseLogicOr(n, template);
		if ("not".equalsIgnoreCase(n.getNodeName()))
			return parseLogicNot(n, template);
		if ("player".equalsIgnoreCase(n.getNodeName()))
			return parsePlayerCondition(n);
		if ("target".equalsIgnoreCase(n.getNodeName()))
			return parseTargetCondition(n, template);
		if ("game".equalsIgnoreCase(n.getNodeName()))
			return parseGameCondition(n);
		return null;
	}

	private Condition parseLogicAnd(Node n, Object template)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
				cond.add(parseCondition(n, template));
		}
		if (cond.conditions == null || cond.conditions.length == 0)
			_log.fatal("Empty <and> condition in zone " + _name);
		return cond;
	}

	private Condition parseLogicOr(Node n, Object template)
	{
		ConditionLogicOr cond = new ConditionLogicOr();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
				cond.add(parseCondition(n, template));
		}
		if (cond.conditions == null || cond.conditions.length == 0)
			_log.fatal("Empty <or> condition in zone " + _name);
		return cond;
	}

	private Condition parseLogicNot(Node n, Object template)
	{
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				return new ConditionLogicNot(parseCondition(n, template));
			}
		}
		_log.fatal("Empty <not> condition in zone " + _name);
		return null;
	}

	private Condition parsePlayerCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("race".equalsIgnoreCase(a.getNodeName()))
			{
				Race race = Race.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerRace(race));
			}
			else if ("level".equalsIgnoreCase(a.getNodeName()))
			{
				int lvl = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerLevel(lvl));
			}
			else if ("resting".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.RESTING, val));
			}
			else if ("moving".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.MOVING, val));
			}
			else if ("running".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.RUNNING, val));
			}
			else if ("flying".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.FLYING, val));
			}
			else if ("hp".equalsIgnoreCase(a.getNodeName()))
			{
				int hp = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerHp(hp));
			}
			else if ("hprate".equalsIgnoreCase(a.getNodeName()))
			{
				double rate = Double.parseDouble(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerHpPercentage(rate));
			}
			else if ("mp".equalsIgnoreCase(a.getNodeName()))
			{
				int mp = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerMp(mp));
			}
			else if ("cp".equalsIgnoreCase(a.getNodeName()))
			{
				int cp = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerCp(cp));
			}
		}

		if (cond == null)
			_log.fatal("Unrecognized <player> condition in zone " + _name);
		return cond;
	}

	/**
	 * @param n  
	 * @param template  
	 */
	private Condition parseTargetCondition(Node n, Object template)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("aggro".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetAggro(val));
			}
			else if ("level".equalsIgnoreCase(a.getNodeName()))
			{
				int lvl = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetLevel(lvl));
			}
			else if ("class_id_restriction".equalsIgnoreCase(a.getNodeName()))
			{
				FastList<Integer> array = new FastList<Integer>();
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(item));
				}
				cond = joinAnd(cond, new ConditionTargetClassIdRestriction(array));
			}
			else if ("active_effect_id".equalsIgnoreCase(a.getNodeName()))
			{
				int effect_id = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetActiveEffectId(effect_id));
			}
			else if ("active_skill_id".equalsIgnoreCase(a.getNodeName()))
			{
				int skill_id = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
			}
			else if ("race_id".equalsIgnoreCase(a.getNodeName()))
			{
				ArrayList<Integer> array = new ArrayList<Integer>();
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					//-1 because we want to take effect for exactly race that is by -1 lower in FastList
					array.add(Integer.decode(item) - 1);
				}
				cond = joinAnd(cond, new ConditionTargetRaceId(array));
			}
			else if ("undead".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetUndead(val));
			}
			else if ("using".equalsIgnoreCase(a.getNodeName()))
			{
				int mask = 0;
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					for (L2WeaponType wt : L2WeaponType.values())
					{
						if (wt.toString().equals(item))
						{
							mask |= wt.mask();
							break;
						}
					}
					for (L2ArmorType at : L2ArmorType.values())
					{
						if (at.toString().equals(item))
						{
							mask |= at.mask();
							break;
						}
					}
				}
				cond = joinAnd(cond, new ConditionTargetUsesWeaponKind(mask));
			}
		}
		if (cond == null)
			_log.fatal("Unrecognized <target> condition in zone " + _name);
		return cond;
	}

	private Condition parseGameCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("night".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionGameTime(CheckGameTime.NIGHT, val));
			}
			if ("chance".equalsIgnoreCase(a.getNodeName()))
			{
				int val = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionGameChance(val));
			}
		}
		if (cond == null)
			_log.fatal("Unrecognized <game> condition in zone " + _name);
		return cond;
	}

	private Condition joinAnd(Condition cond, Condition c)
	{
		if (cond == null)
			return c;
		if (cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}
}