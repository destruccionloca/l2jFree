package net.sf.l2j.gameserver.templates;

import junit.framework.TestCase;

public class TestL2PcTemplate extends TestCase
{

    /**
     * fastidious test but necessary to test all setters
     *
     */
    public void testCreationWithStatSet ()
    {
        StatsSet set = new StatsSet();
        set.set("baseSTR",1);
        set.set("baseCON",2);
        set.set("baseDEX",3);
        set.set("baseINT",4);
        set.set("baseWIT",5);
        set.set("baseMEN",6);
        set.set ("baseHpMax",7);
        set.set("baseCpMax",8);
        set.set ("baseMpMax",9);
        set.set ("baseHpReg",10);
        set.set ("baseMpReg",11);
        set.set("basePAtk",12);
        set.set("baseMAtk",13);
        set.set("basePDef",14);
        set.set("baseMDef",15);
        set.set("basePAtkSpd",16);
        set.set("baseMAtkSpd",17);
        set.set("baseShldDef",19);
        set.set("baseAtkRange",20);
        set.set("baseShldRate",21);
        set.set("baseCritRate",22);
        set.set("baseRunSpd",23);
        set.set("baseWalkSpd",23);
        
        // Geometry
        set.set("collision_radius",56.0);
        set.set("collision_height",57.0);
        
        set.set("classId",60);
        set.set("raceId",4);
        set.set("className","classname");
        set.set("spawnX",62);
        set.set("spawnY",63);
        set.set("spawnZ",64);
        set.set("classBaseLevel",65);
        set.set("lvlHpAdd",66.f);
        set.set("lvlHpMod",67.f);
        set.set("lvlCpAdd",68.f);
        set.set("lvlCpMod",69.f);
        set.set("lvlMpAdd",70.f);
        set.set("lvlMpMod",71.f);
        
        L2PcTemplate template = new L2PcTemplate(set);
        assertNotNull(template);
        
        assertEquals("dummyEntry3",template.getClassId().name());
        assertEquals("dwarf",template.getRace().name());
        assertEquals("classname",template.getClassName());
        assertEquals(62,template.getSpawnX());
        assertEquals(63,template.getSpawnY());
        assertEquals(64,template.getSpawnZ());
        assertEquals(65,template.getClassBaseLevel());
        assertEquals(66.f,template.getLvlHpAdd());
        assertEquals(67.f,template.getLvlHpMod());
        assertEquals(68.f,template.getLvlCpAdd());
        assertEquals(69.f,template.getLvlCpMod());
        assertEquals(70.f,template.getLvlMpAdd());
        assertEquals(71.f,template.getLvlMpMod());
    }
}
