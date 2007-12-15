/**
 * Added copyright notice
 *
 * 
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
package net.sf.l2j.gameserver.model;

import junit.framework.TestCase;
import net.sf.l2j.gameserver.model.L2Skill.SkillOpType;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2BoatInstance;
import net.sf.l2j.gameserver.skills.conditions.ConditionItemId;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillDefault;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * Class for L2Skill testing
 * 
 */
public class TestL2Skill extends TestCase
{   
    /**
     * StatsSet used for test
     */
    private StatsSet statsSetForTest = null;
    private StatsSet statsSetForTestNpc = null;
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Initialize stats set with mandatory value 
        statsSetForTest = new StatsSet();
        statsSetForTest.set("skill_id",1);
        statsSetForTest.set("level",1);
        statsSetForTest.set("name","Skill test");
        statsSetForTest.set("operateType",SkillOpType.OP_PASSIVE);
        statsSetForTest.set("target",SkillTargetType.TARGET_ALLY);
        statsSetForTest.set("skillType",SkillType.BLEED);
        
        statsSetForTestNpc = new StatsSet();
        statsSetForTestNpc.set("baseSTR",1);
        statsSetForTestNpc.set("baseCON",1);
        statsSetForTestNpc.set("baseDEX",1);
        statsSetForTestNpc.set("baseINT",1);
        statsSetForTestNpc.set("baseWIT",1);
        statsSetForTestNpc.set("baseMEN",1);
        statsSetForTestNpc.set("baseHpMax",1);
        statsSetForTestNpc.set("baseCpMax",1);
        statsSetForTestNpc.set("baseMpMax",1);
        statsSetForTestNpc.set("baseHpReg",1);
        statsSetForTestNpc.set("baseCpReg",1);
        statsSetForTestNpc.set("baseMpReg",1);
        statsSetForTestNpc.set("basePAtk",1);
        statsSetForTestNpc.set("baseMAtk",1);
        statsSetForTestNpc.set("basePDef",1);
        statsSetForTestNpc.set("baseMDef",1);
        statsSetForTestNpc.set("basePAtkSpd",1);
        statsSetForTestNpc.set("baseMAtkSpd",1);
        statsSetForTestNpc.set("baseShldDef",1);
        statsSetForTestNpc.set("baseAtkRange",1);
        statsSetForTestNpc.set("baseShldRate",1);
        statsSetForTestNpc.set("baseCritRate",1);
        statsSetForTestNpc.set("baseRunSpd",1);
        statsSetForTestNpc.set("baseWalkSpd",1);
        statsSetForTestNpc.set("collision_radius",1);
        statsSetForTestNpc.set("collision_height",1);        
    }
    
    
	/**
	 * Test for ticket #5 : http://www.l2jfree.com:8080/trac/l2j-free/ticket/5
     * We check a condition but if condition are on itemOrWeapon, it crashed
	 */
	public final void testCheckCondition()
	{
        try
        {
            L2Skill l2skill = new L2SkillDefault (statsSetForTest);
            l2skill.attach(new ConditionItemId(57),true);
            
            L2Character activeChar = new L2BoatInstance(1,new L2CharTemplate(statsSetForTestNpc),"BoatPeople");

            l2skill.checkCondition(activeChar,activeChar, true);
        }
        catch (Exception e)
        {
           fail (e.getMessage()); 
        }
	}
    
     

	

}
