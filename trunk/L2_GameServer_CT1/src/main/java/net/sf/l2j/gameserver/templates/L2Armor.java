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
package net.sf.l2j.gameserver.templates;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.skills.funcs.FuncTemplate;

/**
 * This class is dedicated to the management of armors.
 * 
 * @version $Revision: 1.2.2.1.2.6 $ $Date: 2005/03/27 15:30:10 $
 */
public final class L2Armor extends L2Item
{
	private final int _avoidModifier;
	private final int _pDef;
	private final int _mDef;
	private final int _mpBonus;
	private final int _hpBonus;
	private String _races;
	private String _classes;
	private String _sIds;
	private String _sLvls;
	private final int _sex;
	private FastList<Integer> _racesAllowed = null;
	private FastList<Integer> _classesAllowed = null;
	private FastList<Integer> _sId = null;
	private FastList<Integer> _sLvl = null;
    
	private FastList<L2Skill> _itemSkills = null;
	
    /**
     * Constructor for Armor.<BR><BR>
     * <U><I>Variables filled :</I></U><BR>
     * <LI>_avoidModifier</LI>
     * <LI>_pDef & _mDef</LI>
     * <LI>_mpBonus & _hpBonus</LI>
     * <LI>_races & _classes & _sex</LI>
     * <LI>_sIds & _sLvls</LI>
     * @param type : L2ArmorType designating the type of armor
     * @param set : StatsSet designating the set of couples (key,value) characterizing the armor
     * @see L2Item constructor
     */
	public L2Armor(L2ArmorType type, StatsSet set)
	{
		super(type, set);
		_avoidModifier = set.getInteger("avoid_modify");
		_pDef          = set.getInteger("p_def");
		_mDef          = set.getInteger("m_def");
		_mpBonus       = set.getInteger("mp_bonus", 0);
		_hpBonus       = set.getInteger("hp_bonus", 0);
		_races         = set.getString("races");
		_classes       = set.getString("classes");
		_sex           = set.getInteger("sex");
		_sIds          = set.getString("item_skill_id");
		_sLvls         = set.getString("item_skill_lvl");
		
		if (_races.length()>0)
		{
			try
			{
				int _checker = Integer.parseInt(_races);
				if (_checker != -1) { _racesAllowed = new FastList<Integer>(); _racesAllowed.add(_checker); }
			}
			catch (Throwable t)
			{
				_racesAllowed = new FastList<Integer>();
		        for (String id : _races.split(",")) 
		        	_racesAllowed.add(Integer.parseInt(id));
			}
		}
		if (_classes.length()>0)
		{
			try
			{
				int _checker = Integer.parseInt(_classes);
				if (_checker != -1) { _classesAllowed = new FastList<Integer>(); _classesAllowed.add(_checker); }
			}
			catch (Throwable t)
			{
				_classesAllowed = new FastList<Integer>();
				for (String id : _classes.split(",")) 
	                _classesAllowed.add(Integer.parseInt(id));
			}
		}
		if (_sIds.length()>0 && _sLvls.length()>0)
		{
			try
			{
				int _checker = Integer.parseInt(_sIds);
				if (_checker > 0) { _sId = new FastList<Integer>(); _sId.add(_checker); }
			}
			catch (Throwable t)
			{
				_sId = new FastList<Integer>();
				for (String id : _sIds.split(",")) 
	                _sId.add(Integer.parseInt(id));
			}
			try
			{
				int _checker = Integer.parseInt(_sLvls);
				if (_checker > 0) { _sLvl = new FastList<Integer>(); _sLvl.add(_checker); }
			}
			catch (Throwable t)
			{
				_sLvl = new FastList<Integer>();
				for (String id : _sLvls.split(",")) 
	                _sLvl.add(Integer.parseInt(id));
			}
		}
		
		if (_sId != null && _sLvl != null)
		{
			_itemSkills = new FastList<L2Skill>();
			for (int i = 0; i < _sId.size(); i++)
				if (_sId.get(i) > 0 && _sLvl.get(i) > 0) // Some people might try to experiment with negative skills lol
					if (SkillTable.getInstance().getInfo(_sId.get(i),_sLvl.get(i)) != null)
						_itemSkills.add(SkillTable.getInstance().getInfo(_sId.get(i),_sLvl.get(i)));
					else System.out.println("Adding: id "+String.valueOf(_sId.get(i))+" lvl "+String.valueOf(_sLvl.get(i))+"skill is NULL");
				else System.out.println("Adding: id "+String.valueOf(_sId.get(i))+" lvl "+String.valueOf(_sLvl.get(i))+"skill id/level value is NEGATIVE");
		}
		_sId = null; _sLvl = null; //not needed any longer
		if (_itemSkills != null && _itemSkills.size() < 1) _itemSkills = null; //if negative/wrong skill id(s)/level(s)
	}
	
	/**
	 * Returns the type of the armor.
	 * @return L2ArmorType
	 */
	public L2ArmorType getItemType()
	{
		return (L2ArmorType)super._type;
	}
	
	/**
	 * Returns the ID of the item after applying the mask.
	 * @return int : ID of the item
	 */
	public final int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * Returns the magical defense of the armor
	 * @return int : value of the magic defense
	 */
	public final int getMDef()
	{
		return _mDef;
	}
	
	/**
	 * Returns the physical defense of the armor
	 * @return int : value of the physical defense
	 */
	public final int getPDef()
	{
		return _pDef;
	}
	
	/**
	 * Returns avoid modifier given by the armor
	 * @return int : avoid modifier
	 */
	public final int getAvoidModifier()
	{
		return _avoidModifier;
	}
	
	/**
	 * Returns magical bonus given by the armor
	 * @return int : value of the magical bonus
	 */
	public final int getMpBonus()
	{
		return _mpBonus;
	}
	
	/**
	 * Returns physical bonus given by the armor
	 * @return int : value of the physical bonus
	 */
	public final int getHpBonus()
	{
		return _hpBonus;
	}

    /** 
     * Returns passive skills linked to that armor
     * @return
     */
    public FastList<L2Skill> getSkills()
    {
        return _itemSkills;
    }

	/**
	 * Returns array of Func objects containing the list of functions used by the armor 
	 * @param instance : L2ItemInstance pointing out the armor
	 * @param player : L2Character pointing out the player
	 * @return Func[] : array of functions
	 */
	public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
    {
    	List<Func> funcs = new FastList<Func>();
    	if (_funcTemplates != null)
    	{
    		for (FuncTemplate t : _funcTemplates) {
		    	Env env = new Env();
		    	env.player = player;
		    	env.item = instance;
		    	Func f = t.getFunc(env, instance);
		    	if (f != null)
			    	funcs.add(f);
    		}
    	}
    	return funcs.toArray(new Func[funcs.size()]);
    }
	
	/** 
     * Returns true if player can equip the item
     * @param raceId: player's race
     * @param classId: player's class
     * @param isFemale: player's sex
     * @return boolean: ability to equip
     */
    public boolean allowEquip(int raceId, int classId, boolean isFemale)
    {
    	return allowEquipForRace(raceId) && allowEquipForClass(classId) && allowEquipForSex(isFemale);
    }
    
    public boolean allowEquipForRace(int raceId)
    {
    	if (_racesAllowed == null) return true;
    	else if (_racesAllowed.contains(raceId)) return true;
    	return false;
    }
    
    public boolean allowEquipForClass(int classId)
    {
    	if (_classesAllowed == null) return true;
    	else if (_classesAllowed.contains(classId)) return true;
    	return false;
    }
    
    public boolean allowEquipForSex(boolean isFemale)
    {
    	int serial;
    	if (isFemale) serial = 1; else serial = 0;
    	if (_sex == -1) return true;
    	else return (serial == _sex);
    }
}
