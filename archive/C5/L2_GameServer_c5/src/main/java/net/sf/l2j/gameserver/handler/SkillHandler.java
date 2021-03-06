/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.handler;

import java.util.Map;
import java.util.TreeMap;

import net.sf.l2j.gameserver.handler.skillhandlers.BalanceLife;
import net.sf.l2j.gameserver.handler.skillhandlers.CPperHeal;
import net.sf.l2j.gameserver.handler.skillhandlers.Charge;
import net.sf.l2j.gameserver.handler.skillhandlers.ChargeSelf;
import net.sf.l2j.gameserver.handler.skillhandlers.CombatPointHeal;
import net.sf.l2j.gameserver.handler.skillhandlers.Continuous;
import net.sf.l2j.gameserver.handler.skillhandlers.Craft;
import net.sf.l2j.gameserver.handler.skillhandlers.Crits;
import net.sf.l2j.gameserver.handler.skillhandlers.Disablers;
import net.sf.l2j.gameserver.handler.skillhandlers.DrainSoul;
import net.sf.l2j.gameserver.handler.skillhandlers.Fishing;
import net.sf.l2j.gameserver.handler.skillhandlers.FishingSkill;
import net.sf.l2j.gameserver.handler.skillhandlers.Heal;
import net.sf.l2j.gameserver.handler.skillhandlers.ManaHeal;
import net.sf.l2j.gameserver.handler.skillhandlers.Manadam;
import net.sf.l2j.gameserver.handler.skillhandlers.Mdam;
import net.sf.l2j.gameserver.handler.skillhandlers.Pdam;
import net.sf.l2j.gameserver.handler.skillhandlers.Recall;
import net.sf.l2j.gameserver.handler.skillhandlers.Resurrect;
import net.sf.l2j.gameserver.handler.skillhandlers.SiegeFlag;
import net.sf.l2j.gameserver.handler.skillhandlers.Spoil;
import net.sf.l2j.gameserver.handler.skillhandlers.StrSiegeAssault;
import net.sf.l2j.gameserver.handler.skillhandlers.SummonCp;
import net.sf.l2j.gameserver.handler.skillhandlers.SummonCursedBones;
import net.sf.l2j.gameserver.handler.skillhandlers.SummonFriend;
import net.sf.l2j.gameserver.handler.skillhandlers.SummonTreasureKey;
import net.sf.l2j.gameserver.handler.skillhandlers.Sweep;
import net.sf.l2j.gameserver.handler.skillhandlers.TakeCastle;
import net.sf.l2j.gameserver.handler.skillhandlers.Unlock;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.4 $ $Date: 2005/04/03 15:55:06 $
 */
public class SkillHandler
{
	//private final static Log _log = LogFactory.getLog(SkillHandler.class.getName());
	
	private static SkillHandler _instance;
	
	private Map<L2Skill.SkillType, ISkillHandler> _datatable;
	
	public static SkillHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new SkillHandler();
		}
		return _instance;
	}
	
	private SkillHandler()
	{
		_datatable = new TreeMap<SkillType, ISkillHandler>();
        registerSkillHandler(new Pdam());
        registerSkillHandler(new Crits());
        registerSkillHandler(new Mdam());
        registerSkillHandler(new Manadam());
        registerSkillHandler(new Heal());
        registerSkillHandler(new CombatPointHeal());
        registerSkillHandler(new ManaHeal());
        registerSkillHandler(new BalanceLife());
        registerSkillHandler(new Charge());
        registerSkillHandler(new ChargeSelf());
        registerSkillHandler(new Continuous());
        registerSkillHandler(new Resurrect());
        registerSkillHandler(new Spoil());
        registerSkillHandler(new Sweep());
        registerSkillHandler(new Disablers());
        registerSkillHandler(new Recall());
        registerSkillHandler(new CPperHeal()); 
        registerSkillHandler(new SiegeFlag());
        registerSkillHandler(new TakeCastle());
        registerSkillHandler(new Unlock());
        registerSkillHandler(new DrainSoul());
        registerSkillHandler(new Craft()); 
        registerSkillHandler(new Fishing()); 
        registerSkillHandler(new FishingSkill());
        registerSkillHandler(new SummonCp());
        registerSkillHandler(new SummonCursedBones());
        registerSkillHandler(new SummonTreasureKey());
        registerSkillHandler(new StrSiegeAssault());
        registerSkillHandler(new SummonFriend());        
	}
	
	public void registerSkillHandler(ISkillHandler handler)
	{
		SkillType[] types = handler.getSkillIds();
		for (SkillType t : types)
		{
			_datatable.put(t, handler);
		}
	}
	
	public ISkillHandler getSkillHandler(SkillType skillType)
	{
		return _datatable.get(skillType);
	}

    /**
     * @return
     */
    public int size()
    {
        return _datatable.size();
    }
}
