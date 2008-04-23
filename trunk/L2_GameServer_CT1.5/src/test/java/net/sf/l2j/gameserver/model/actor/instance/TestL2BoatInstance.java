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
package net.sf.l2j.gameserver.model.actor.instance;

import junit.framework.TestCase;
import net.sf.l2j.gameserver.ConfigHelper;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 *
 */
public class TestL2BoatInstance extends TestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ConfigHelper.configure();
    }
    
    /**
     * Just test the correct creation 
     * and the cycle changes when we spawn the instance
     */
    public void testSpawn ()
    {
        // by default, datapack root is set to where the caller play the test
        L2BoatInstance l2BoatInstance = createL2BoatInstance();
        assertNotNull(l2BoatInstance);

        assertEquals(l2BoatInstance.getCycle(),0);

        l2BoatInstance.spawn();
        
        assertNotNull(l2BoatInstance.getTrajet1());
        assertNotNull(l2BoatInstance.getTrajet2());
        
        assertEquals(l2BoatInstance.getCycle(),L2BoatInstance.TRAJET_WAY_1);
    }
    
    /**
     * create a dummy L2boatInstance
     * @return a L2BoatInstance
     */
    private L2BoatInstance createL2BoatInstance()
    {
        L2BoatInstance boat;
        StatsSet npcDat = new StatsSet(); 
        npcDat.set("npcId", 1);
        npcDat.set("level", 0);
        npcDat.set("jClass", "boat");

        npcDat.set("baseSTR", 0);
        npcDat.set("baseCON", 0);
        npcDat.set("baseDEX", 0);
        npcDat.set("baseINT", 0);
        npcDat.set("baseWIT", 0);
        npcDat.set("baseMEN", 0);

        npcDat.set("baseShldDef", 0);
        npcDat.set("baseShldRate", 0);
        npcDat.set("baseAccCombat", 38);
        npcDat.set("baseEvasRate",  38);
        npcDat.set("baseCritRate",  38);

        //npcDat.set("name", "");
        npcDat.set("collision_radius", 0);
        npcDat.set("collision_height", 0);
        npcDat.set("fcollision_radius", 0);
        npcDat.set("fcollision_height", 0);
        npcDat.set("sex", "male");
        npcDat.set("type", "");
        npcDat.set("baseAtkRange", 0);
        npcDat.set("baseMpMax", 0);
        npcDat.set("baseCpMax", 0);
        npcDat.set("rewardExp", 0);
        npcDat.set("rewardSp", 0);
        npcDat.set("basePAtk", 0);
        npcDat.set("baseMAtk", 0);
        npcDat.set("basePAtkSpd", 0);
        npcDat.set("aggroRange", 0);
        npcDat.set("baseMAtkSpd", 0);
        npcDat.set("rhand", 0);
        npcDat.set("lhand", 0);
        npcDat.set("armor", 0);
        npcDat.set("baseWalkSpd", 0);
        npcDat.set("baseRunSpd", 0);
        npcDat.set("name", "mock boat");
        npcDat.set("baseHpMax", 50000);
        npcDat.set("baseHpReg", 3.e-3f);
        npcDat.set("baseMpReg", 3.e-3f);
        npcDat.set("basePDef", 100);
        npcDat.set("baseMDef", 100);        
        L2CharTemplate template = new L2CharTemplate(npcDat);       
        boat = new L2BoatInstance(283222,template,"mock boat");
        boat.getPosition().setHeading(32);
        boat.getPosition().setXYZ(1,2,3);

        int IdWaypoint1 = 1;
        int IdWTicket1 = 2200;
        int ntx1 = 3;
        int nty1 = 4;
        int ntz1 = 5;        
        String npc1 = "1234";
        String mess10_1 = "mess10_1";
        String mess5_1 = "mess5_1";
        String mess1_1 = "mess1_1";
        String mess0_1 = "mess0_1";
        String messb_1 = "messb_1";                                           
        boat.setTrajet1(IdWaypoint1,IdWTicket1,ntx1,nty1,ntz1,npc1,mess10_1,mess5_1,mess1_1,mess0_1,messb_1);
        IdWaypoint1 = 2;
        IdWTicket1 = 2300;
        ntx1 = 7;
        nty1 = 8;
        ntz1 = 9;        
        npc1 = "1235";
        mess10_1 = "mess10_1";
        mess5_1 = "mess5_1";
        mess1_1 = "mess1_1";
        mess0_1 = "mess0_1";
        messb_1 = "messb_1";
        boat.setTrajet2(IdWaypoint1,IdWTicket1,ntx1,nty1,ntz1,npc1,mess10_1,mess5_1,mess1_1,mess0_1,messb_1);
        return boat;    
    }
}
