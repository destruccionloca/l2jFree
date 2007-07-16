package net.sf.l2j.gameserver.templates;

import junit.framework.TestCase;

public class TestL2CharTemplate extends TestCase
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
        set.set ("baseMReuseDelay", 18.0);
        set.set("baseShldDef",19);
        set.set("baseAtkRange",20);
        set.set("baseShldRate",21);
        set.set("baseCritRate",22);
        set.set("baseRunSpd",23);
        // SpecialStats
        set.set("baseBreath",         24);
        set.set("baseAggression",     25);
        set.set("baseBleed",          26);
        set.set("basePoison",         27);
        set.set("baseStun",           28);
        set.set("baseRoot",           29);
        set.set("baseMovement",       30);
        set.set("baseConfusion",      31);
        set.set("baseSleep",          32);
        set.set("baseFire",           33);
        set.set("baseWind",           34);
        set.set("baseWater",          35);
        set.set("baseEarth",          36);
        set.set("baseHoly",           37);
        set.set("baseDark",           38);
        set.set("baseAaggressionRes", 39); // setting baseRes stat to 1 for mul type effects in DP
        set.set("baseBleedRes",       40);
        set.set("basePoisonRes",      41);
        set.set("baseStunRes",        42);
        set.set("baseRootRes",        43);
        set.set("baseMovementRes",    44);
        set.set("baseConfusionRes",   45);
        set.set("baseSleepRes",       46);
        set.set("baseFireRes",        47);
        set.set("baseWindRes",        48);
        set.set("baseWaterRes",       49);
        set.set("baseEarthRes",       50);
        set.set("baseHolyRes",        51);
        set.set("baseDarkRes",        52);
        set.set("isUndead", 0);
        //C4 Stats
        set.set("baseMpConsumeRate",        53);
        set.set("baseHpConsumeRate",        54);
        // Geometry
        set.set("collision_radius",56.0);
        set.set("collision_height",57.0);
        
        L2CharTemplate template = new L2CharTemplate(set);
        assertNotNull(template);
        
        assertEquals(1,template.getBaseSTR());
        assertEquals(2,template.getBaseCON());
        assertEquals(3,template.getBaseDEX());
        assertEquals(4,template.getBaseINT());
        assertEquals(5,template.getBaseWIT());
        assertEquals(6,template.getBaseMEN());
        assertEquals(new Float(7.0).floatValue(),template.getBaseHpMax());
        assertEquals(new Float(8.0).floatValue(),template.getBaseCpMax());
        assertEquals(new Float(9).floatValue(),template.getBaseMpMax());
        assertEquals(new Float(10).floatValue(),template.getBaseHpReg());
        assertEquals(new Float(11).floatValue(),template.getBaseMpReg());
        assertEquals(12,template.getBasePAtk());
        assertEquals(13,template.getBaseMAtk());
        assertEquals(14,template.getBasePDef());
        assertEquals(15,template.getBaseMDef());
        assertEquals(16,template.getBasePAtkSpd());
        assertEquals(17,template.getBaseMAtkSpd());
        assertEquals(new Float(18).floatValue(),template.getBaseMReuseRate());
        assertEquals(19,template.getBaseShldDef());
        assertEquals(20,template.getBaseAtkRange());
        assertEquals(21,template.getBaseShldRate());
        assertEquals(22,template.getBaseCritRate());
        assertEquals(23,template.getBaseRunSpd());
        // SpecialStats
        assertEquals(24,template.getBaseBreath());
        assertEquals(25,template.getBaseAggression());
        assertEquals(26,template.getBaseBleed());
        assertEquals(27,template.getBasePoison());
        assertEquals(28,template.getBaseStun());
        assertEquals(29,template.getBaseRoot());
        assertEquals(30,template.getBaseMovement());
        assertEquals(31,template.getBaseConfusion());
        assertEquals(33,template.getBaseFire());
        assertEquals(32,template.getBaseSleep());
        assertEquals(34,template.getBaseWind());
        assertEquals(35,template.getBaseWater());
        assertEquals(36,template.getBaseEarth());
        assertEquals(37,template.getBaseHoly());
        assertEquals(38,template.getBaseDark());
        assertEquals(39,template.getBaseAggressionRes());
        assertEquals(40,template.getBaseBleedRes());
        assertEquals(41,template.getBasePoisonRes());
        assertEquals(42,template.getBaseStunRes());
        assertEquals(43,template.getBaseRootRes());
        assertEquals(44,template.getBaseMovementRes());
        assertEquals(45,template.getBaseConfusionRes());
        assertEquals(46,template.getBaseSleepRes());
        assertEquals(47,template.getBaseFireRes());
        assertEquals(48,template.getBaseWindRes());
        assertEquals(49,template.getBaseWaterRes());
        assertEquals(50,template.getBaseEarthRes());
        assertEquals(51,template.getBaseHolyRes());
        assertEquals(52,template.getBaseDarkRes());
        assertEquals(false,template.isUndead());
        //C4 Stats
        assertEquals(53,template.getBaseMpConsumeRate());
        assertEquals(54,template.getBaseHpConsumeRate());
        // Geometry
        assertEquals(56,template.getCollisionRadius());
        assertEquals(57,template.getCollisionHeight());
    }
    
    /**
     * fastidious test but necessary to test all setters
     *
     */
    public void testCreationWithDefaultValueForSomeStats()
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
        // Geometry
        set.set("collision_radius",56.0);
        set.set("collision_height",57.0);
        
        L2CharTemplate template = new L2CharTemplate(set);
        assertNotNull(template);
        assertEquals(1,template.getBaseSTR());
        assertEquals(2,template.getBaseCON());
        assertEquals(3,template.getBaseDEX());
        assertEquals(4,template.getBaseINT());
        assertEquals(5,template.getBaseWIT());
        assertEquals(6,template.getBaseMEN());
        assertEquals(new Float(7.0).floatValue(),template.getBaseHpMax());
        assertEquals(new Float(8.0).floatValue(),template.getBaseCpMax());
        assertEquals(new Float(9).floatValue(),template.getBaseMpMax());
        assertEquals(new Float(10).floatValue(),template.getBaseHpReg());
        assertEquals(new Float(11).floatValue(),template.getBaseMpReg());
        assertEquals(12,template.getBasePAtk());
        assertEquals(13,template.getBaseMAtk());
        assertEquals(14,template.getBasePDef());
        assertEquals(15,template.getBaseMDef());
        assertEquals(16,template.getBasePAtkSpd());
        assertEquals(17,template.getBaseMAtkSpd());
        assertEquals(1.f,template.getBaseMReuseRate());
        assertEquals(19,template.getBaseShldDef());
        assertEquals(20,template.getBaseAtkRange());
        assertEquals(21,template.getBaseShldRate());
        assertEquals(22,template.getBaseCritRate());
        assertEquals(23,template.getBaseRunSpd());
        // SpecialStats
        assertEquals(100,template.getBaseBreath());
        assertEquals(0,template.getBaseAggression());
        assertEquals(0,template.getBaseBleed());
        assertEquals(0,template.getBasePoison());
        assertEquals(0,template.getBaseStun());
        assertEquals(0,template.getBaseRoot());
        assertEquals(0,template.getBaseMovement());
        assertEquals(0,template.getBaseConfusion());
        assertEquals(0,template.getBaseFire());
        assertEquals(0,template.getBaseSleep());
        assertEquals(0,template.getBaseWind());
        assertEquals(0,template.getBaseWater());
        assertEquals(0,template.getBaseEarth());
        assertEquals(0,template.getBaseHoly());
        assertEquals(0,template.getBaseDark());
        assertEquals(1,template.getBaseAggressionRes());
        assertEquals(1,template.getBaseBleedRes());
        assertEquals(1,template.getBasePoisonRes());
        assertEquals(1,template.getBaseStunRes());
        assertEquals(1,template.getBaseRootRes());
        assertEquals(1,template.getBaseMovementRes());
        assertEquals(1,template.getBaseConfusionRes());
        assertEquals(1,template.getBaseSleepRes());
        assertEquals(1,template.getBaseFireRes());
        assertEquals(1,template.getBaseWindRes());
        assertEquals(1,template.getBaseWaterRes());
        assertEquals(1,template.getBaseEarthRes());
        assertEquals(1,template.getBaseHolyRes());
        assertEquals(1,template.getBaseDarkRes());
        assertEquals(false,template.isUndead());
        //C4 Stats
        assertEquals(0,template.getBaseMpConsumeRate());
        assertEquals(0,template.getBaseHpConsumeRate());
        // Geometry
        assertEquals(56,template.getCollisionRadius());
        assertEquals(57,template.getCollisionHeight());
    }    
    
    public void testDefaultValueWithoutInstantiationWithStatSet()
    {
        L2CharTemplate template = new L2CharTemplate();
        assertEquals(1.f,template.getBaseMReuseRate());
        assertEquals(100,template.getBaseBreath());
        assertEquals(1,template.getBaseAggressionRes());
        assertEquals(1,template.getBaseBleedRes());
        assertEquals(1,template.getBasePoisonRes());
        assertEquals(1,template.getBaseStunRes());
        assertEquals(1,template.getBaseRootRes());
        assertEquals(1,template.getBaseMovementRes());
        assertEquals(1,template.getBaseConfusionRes());
        assertEquals(1,template.getBaseSleepRes());
        assertEquals(1,template.getBaseFireRes());
        assertEquals(1,template.getBaseWindRes());
        assertEquals(1,template.getBaseWaterRes());
        assertEquals(1,template.getBaseEarthRes());
        assertEquals(1,template.getBaseHolyRes());
        assertEquals(1,template.getBaseDarkRes());
        assertEquals(false,template.isUndead());        
    }
    
}
