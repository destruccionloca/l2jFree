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
package com.l2jfree.gameserver.templates;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.effects.EffectTemplate;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.skills.funcs.FuncTemplate;

import javolution.util.FastList;


public abstract class L2Equip extends L2Item
{
	private int						_sex				= -1;
	private Integer[]				_allowedRaces		= null;
	private Integer[]				_allowedClasses		= null;
	private L2Skill[]				_itemSkills			= null;

	protected FuncTemplate[]		_funcTemplates		= null;
	protected EffectTemplate[]		_effectTemplates	= null;

	public static final Func[]		EMPTY_FUNC_SET		= new Func[0];
	public static final L2Effect[]	EMPTY_EFFECT_SET	= new L2Effect[0];

	// TODO: Replace by chance skills
	public static class WeaponSkill
	{
		public L2Skill skill;
		public int chance;
	}

	public L2Equip(AbstractL2ItemType type, StatsSet set)
	{
		super(type, set);
		_sex = set.getInteger("sex");

		String[] races = set.getString("races").split(",");
		String[] classes = set.getString("classes").split(",");
		String[] itemSkillDefs = set.getString("skills_item").split(";");

		FastList<Integer> allowedRaces = null;
		FastList<Integer> allowedClasses = null;
		FastList<L2Skill> itemSkills = null;

		// Allowed races
		if (races != null && races.length > 0)
		{
			allowedRaces = parseRestriction(races, "race", "armor");
		}

		// Allowed classes
		if (classes != null && classes.length > 0)
		{
			allowedClasses = parseRestriction(classes, "class", "armor");
		}

		// Item skills
		if (itemSkillDefs != null && itemSkillDefs.length > 0)
		{
			itemSkills = parseSkills(itemSkillDefs, "item", "armor");
		}

		if (allowedRaces != null)
			_allowedRaces = allowedRaces.toArray(new Integer[allowedRaces.size()]);
		if (allowedClasses != null)
			_allowedClasses = allowedClasses.toArray(new Integer[allowedClasses.size()]);
		if (itemSkills != null)
			_itemSkills = itemSkills.toArray(new L2Skill[itemSkills.size()]);
	}

	protected FastList<Integer> parseRestriction(String[] from, String restrictType, String itemType)
	{
		FastList<Integer> values = null;
		for (String strVal : from)
		{
			int intVal = 0;
			try
			{
				intVal = Integer.parseInt(strVal);
			}
			catch (Exception e)
			{
				_log.error("Cannot parse " + restrictType + " restriction \"" + strVal + "\" for " + itemType + " " + getItemId());
				continue;
			}

			if (intVal <= 0)
				continue;

			if (values == null)
				values = new FastList<Integer>();
			values.add(intVal);
		}
		return values;
	}

	protected FastList<L2Skill> parseSkills(String[] from, String skillType, String itemType)
	{
		FastList<L2Skill> itemSkills = null;
		for (String skillStr : from)
		{
			if (skillStr.length() == 0)
				continue;

			int skillId = 0;
			int skillLevel = 0;
			L2Skill skill = null;
			try
			{
				String[] skillDef = skillStr.split("-");
				skillId = Integer.parseInt(skillDef[0]);
				skillLevel = Integer.parseInt(skillDef[1]);
			}
			catch (Exception e)
			{
				_log.error("Cannot parse " + skillType + " skill \"" + skillStr + "\" for " + itemType + " item " + getItemId());
				continue;
			}

			skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			if (skill == null)
			{
				_log.error("Cannot find " + skillType + " skill (" + skillId + "," + skillLevel + ") for " + itemType + " item " + getItemId());
			}
			else
			{
				if (itemSkills == null)
					itemSkills = new FastList<L2Skill>();
				itemSkills.add(skill);
			}
		}
		return itemSkills;
	}

	protected FastList<WeaponSkill> parseChanceSkills(String[] from, String skillType, String itemType)
	{
		FastList<WeaponSkill> itemSkills = null;
		for (String skillStr : from)
		{
			if (skillStr.length() == 0)
				continue;

			int skillId = 0;
			int skillLevel = 0;
			int chance = 0;
			L2Skill skill = null;
			try
			{
				String[] skillDef = skillStr.split("-");
				skillId = Integer.parseInt(skillDef[0]);
				skillLevel = Integer.parseInt(skillDef[1]);
				chance = Integer.parseInt(skillDef[2]);
			}
			catch (Exception e)
			{
				_log.error("Cannot parse " + skillType + " skill \"" + skillStr + "\" for " + itemType + " item " + getItemId());
				continue;
			}

			skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
			if (skill == null)
			{
				_log.error("Cannot find " + skillType + " skill (" + skillId + "," + skillLevel + ") for " + itemType + " item " + getItemId());
			}
			else
			{
				//skill.attach(new ConditionGameChance(chance), true);
				if (itemSkills == null)
					itemSkills = new FastList<WeaponSkill>();
				WeaponSkill ws = new WeaponSkill();
				ws.skill = skill;
				ws.chance = chance;
				itemSkills.add(ws);
			}
		}
		return itemSkills;
	}

	/** 
	* Returns passive skills linked to that item
	* @return
	*/
	public L2Skill[] getSkills()
	{
		return _itemSkills;
	}

	/**
	 * Returns array of Func objects containing the list of functions used by the item 
	 * @param instance : L2ItemInstance pointing out the item
	 * @param player : L2Character pointing out the player
	 * @return Func[] : array of functions
	 */
	public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
	{
		if (_funcTemplates == null)
			return EMPTY_FUNC_SET;

		FastList<Func> funcs = new FastList<Func>();
		for (FuncTemplate t : _funcTemplates)
		{
			Env env = new Env();
			env.player = player;
			env.target = player;
			env.item = instance;
			Func f = t.getFunc(env, instance);
			if (f != null)
				funcs.add(f);
		}

		if (funcs.size() == 0)
			return EMPTY_FUNC_SET;
		return funcs.toArray(new Func[funcs.size()]);
	}

	/**
	 * Returns the effects associated with the item.
	 * @param instance : L2ItemInstance pointing out the item
	 * @param player : L2Character pointing out the player
	 * @return L2Effect[] : array of effects generated by the item
	 */
	public L2Effect[] getEffects(L2ItemInstance instance, L2Character player)
	{
		if (_effectTemplates == null)
			return EMPTY_EFFECT_SET;

		FastList<L2Effect> effects = new FastList<L2Effect>();
		for (EffectTemplate et : _effectTemplates)
		{
			Env env = new Env();
			env.player = player;
			env.target = player;
			env.item = instance;
			L2Effect e = et.getEffect(env);
			if (e != null)
				effects.add(e);
		}

		if (effects.size() == 0)
			return EMPTY_EFFECT_SET;
		return effects.toArray(new L2Effect[effects.size()]);
	}

	/**
	 * Add the FuncTemplate f to the list of functions used with the item
	 * @param f : FuncTemplate to add
	 */
	public void attach(FuncTemplate f)
	{
		// If _functTemplates is empty, create it and add the FuncTemplate f in it
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]
			{ f };
		}
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}

	/**
	 * Add the EffectTemplate effect to the list of effects generated by the item
	 * @param effect : EffectTemplate
	 */
	public void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[]
			{ effect };
		}
		else
		{
			int len = _effectTemplates.length;
			EffectTemplate[] tmp = new EffectTemplate[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			//                                      number of components to be copied)
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
	}

	/** 
	 * Returns true if player can equip the item
	 * @param player: the player to check
	 * @return boolean: ability to equip
	 */
	public boolean allowEquip(L2PcInstance player)
	{
		return allowEquipForRace(player.getRace().ordinal()) && allowEquipForClass(player.getClassId().getId())
				&& allowEquipForSex(player.getAppearance().getSex());
	}

	public boolean allowEquipForRace(int raceId)
	{
		if (_allowedRaces == null)
			return true;
		for (int race : _allowedRaces)
			if (race == raceId)
				return true;
		return false;
	}

	public boolean allowEquipForClass(int classId)
	{
		if (_allowedClasses == null)
			return true;
		for (int cls : _allowedClasses)
			if (cls == classId)
				return true;
		return false;
	}

	public boolean allowEquipForSex(boolean isFemale)
	{
		return _sex == -1 || (_sex == 1 && isFemale) || (_sex == 0 && !isFemale);
	}
}
