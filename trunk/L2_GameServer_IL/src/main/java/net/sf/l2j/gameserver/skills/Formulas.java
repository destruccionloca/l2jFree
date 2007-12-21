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
package net.sf.l2j.gameserver.skills;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2BossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState;
import net.sf.l2j.gameserver.skills.conditions.ConditionUsingItemType;
import net.sf.l2j.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Global calculations, can be modified by server admins
 */
public final class Formulas 
{
    /** Regen Task period */
    protected static final Log _log = LogFactory.getLog(L2Character.class.getName());
    private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs
    
    public static int MAX_STAT_VALUE = 100;
    
    private static final double[] STRCompute = new double[]{1.036, 34.845}; //{1.016, 28.515}; for C1
    private static final double[] INTCompute = new double[]{1.020, 31.375}; //{1.020, 31.375}; for C1
    private static final double[] DEXCompute = new double[]{1.009, 19.360}; //{1.009, 19.360}; for C1
    private static final double[] WITCompute = new double[]{1.050, 20.000}; //{1.050, 20.000}; for C1
    private static final double[] CONCompute = new double[]{1.030, 27.632}; //{1.015, 12.488}; for C1
    private static final double[] MENCompute = new double[]{1.010, -0.060}; //{1.010, -0.060}; for C1

    protected static final double[] WITbonus = new double[MAX_STAT_VALUE];
    protected static final double[] MENbonus = new double[MAX_STAT_VALUE];
    protected static final double[] INTbonus = new double[MAX_STAT_VALUE];
    protected static final double[] STRbonus = new double[MAX_STAT_VALUE];
    protected static final double[] DEXbonus = new double[MAX_STAT_VALUE];
    protected static final double[] CONbonus = new double[MAX_STAT_VALUE];
    
    // These values are 100% matching retail tables, no need to change and no need add 
    // calculation into the stat bonus when accessing (not efficient),
    // better to have everything precalculated and use values directly (saves CPU)
    static
    {
        for (int i=0; i < STRbonus.length; i++)
            STRbonus[i] = Math.floor(Math.pow(STRCompute[0], i - STRCompute[1]) *100 +.5d) /100;
        for (int i=0; i < INTbonus.length; i++)
            INTbonus[i] = Math.floor(Math.pow(INTCompute[0], i - INTCompute[1]) *100 +.5d) /100;
        for (int i=0; i < DEXbonus.length; i++)
            DEXbonus[i] = Math.floor(Math.pow(DEXCompute[0], i - DEXCompute[1]) *100 +.5d) /100;
        for (int i=0; i < WITbonus.length; i++)
            WITbonus[i] = Math.floor(Math.pow(WITCompute[0], i - WITCompute[1]) *100 +.5d) /100;
        for (int i=0; i < CONbonus.length; i++)
            CONbonus[i] = Math.floor(Math.pow(CONCompute[0], i - CONCompute[1]) *100 +.5d) /100;
        for (int i=0; i < MENbonus.length; i++)
            MENbonus[i] = Math.floor(Math.pow(MENCompute[0], i - MENCompute[1]) *100 +.5d) /100;
    }

    static class FuncAddLevel3 extends Func
    {
        static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];
        
        static Func getInstance(Stats stat)
        {
            int pos = stat.ordinal();
            if (_instancies[pos] == null) _instancies[pos] = new FuncAddLevel3(stat);
            return _instancies[pos];
        }
        
        private FuncAddLevel3(Stats pStat)
        {
            super(pStat, 0x10, null);
        }
        
        public void calc(Env env)
        {
            env.value += env.player.getLevel() / 3.0;
        }
    }
    
    static class FuncMultLevelMod extends Func
    {
        static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];
        
        static Func getInstance(Stats stat)
        {

            int pos = stat.ordinal();
            if (_instancies[pos] == null) _instancies[pos] = new FuncMultLevelMod(stat);
            return _instancies[pos];
        }
        
        private FuncMultLevelMod(Stats pStat)
        {
            super(pStat, 0x20, null);
        }
        
        public void calc(Env env)
        {
            env.value *= env.player.getLevelMod();
        }
    }
    
    static class FuncMultRegenResting extends Func
    {
        static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];
        
        /**
         * Return the Func object corresponding to the state concerned.<BR><BR>
         */
        static Func getInstance(Stats stat)
        {
            int pos = stat.ordinal();
            
            if (_instancies[pos] == null) _instancies[pos] = new FuncMultRegenResting(stat);            
            
            return _instancies[pos];
        }
        
        /**
         * Constructor of the FuncMultRegenResting.<BR><BR>
         */
        private FuncMultRegenResting(Stats pStat)
        {
            super(pStat, 0x20, null);
            setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
        }
        
        /**
         * Calculate the modifier of the state concerned.<BR><BR>
         */
        public void calc(Env env)
        {
            if (!cond.test(env)) return;
            
            env.value *= 1.45;
        }
    }
    
    static class FuncPAtkMod extends Func
    {
        static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

        static Func getInstance()
        {
            return _fpa_instance;
        }
        
        private FuncPAtkMod()
        {
            super(Stats.POWER_ATTACK, 0x30, null);
        }
        
        public void calc(Env env)
        {
            env.value *= STRbonus[env.player.getStat().getSTR()] * env.player.getLevelMod();
        }
    }
    
    static class FuncMAtkMod extends Func
    {
        static final FuncMAtkMod _fma_instance = new FuncMAtkMod();
        
        static Func getInstance() 
        {
            return _fma_instance;
        }
        private FuncMAtkMod()
        {
            super(Stats.MAGIC_ATTACK, 0x20, null);
        }
 
        public void calc(Env env)
        {
            double intb = INTbonus[env.player.getINT()];
            double lvlb = env.player.getLevelMod();
            env.value *= (lvlb * lvlb) * (intb * intb);
        }
    }
    
    static class FuncMDefMod extends Func
    {
        static final FuncMDefMod _fmm_instance = new FuncMDefMod();
        
        static Func getInstance() 
        {
            return _fmm_instance;
        }
        private FuncMDefMod()
        {
            super(Stats.MAGIC_DEFENCE, 0x20, null);
        }

        public void calc(Env env)
        {
            if (env.player instanceof L2PcInstance)
            {
                L2PcInstance p = (L2PcInstance) env.player;
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null) env.value -= 5;
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null) env.value -= 5;
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null) env.value -= 9;
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null) env.value -= 9;
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null) env.value -= 13;
            }
            env.value *= MENbonus[env.player.getStat().getMEN()] * env.player.getLevelMod();
        }
    }

    static class FuncPDefMod extends Func
    {
        static final FuncPDefMod _fmm_instance = new FuncPDefMod();

        static Func getInstance()
        {
            return _fmm_instance;
        }

        private FuncPDefMod()
        {
            super(Stats.POWER_DEFENCE, 0x20, null);
        }
 
        public void calc(Env env)
        {
            if (env.player instanceof L2PcInstance)
            {
                L2PcInstance p = (L2PcInstance) env.player;
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null) env.value -= 12;
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST) != null)
                    env.value -= ((p.getClassId().isMage()) ? 15 : 31);
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null)
                    env.value -= ((p.getClassId().isMage()) ? 8 : 18);
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null) env.value -= 8;
                if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null) env.value -= 7;
            }
            env.value *= env.player.getLevelMod();
        }
    }
    
    static class FuncBowAtkRange extends Func
    {
        private static final FuncBowAtkRange _fbarInstance = new FuncBowAtkRange();
        
        static Func getInstance() 
        {
            return _fbarInstance;
        }
        
        private FuncBowAtkRange()
        {
            super(Stats.POWER_ATTACK_RANGE, 0x10, null);
            setCondition(new ConditionUsingItemType(L2WeaponType.BOW.mask()));
        }
        
        public void calc(Env env)
        {
            if (!cond.test(env)) return;
            env.value += 450;
        }
    }
    
    static class FuncAtkAccuracy extends Func
    {
        static final FuncAtkAccuracy _faaInstance = new FuncAtkAccuracy();
        
        static Func getInstance() 
        {
            return _faaInstance;
        }
        
        private FuncAtkAccuracy()
        {
            super(Stats.ACCURACY_COMBAT, 0x10, null);
        }
        
        public void calc(Env env)
        {
            L2Character p = env.player;
            //[Square(DEX)]*6 + lvl + weapon hitbonus;
            env.value += Math.sqrt(p.getStat().getDEX()) * 6;
            env.value += p.getLevel();
            if( p instanceof L2Summon) env.value += (p.getLevel() < 60) ? 4 : 5;
        }
    }
    
    static class FuncAtkEvasion extends Func
    {
        static final FuncAtkEvasion _faeInstance = new FuncAtkEvasion();
        
        static Func getInstance() 
        {
            return _faeInstance;
        }
        
        private FuncAtkEvasion()
        {
            super(Stats.EVASION_RATE, 0x10, null);
        }
        
        public void calc(Env env)
        {
            L2Character p = env.player;
            //[Square(DEX)]*6 + lvl;
            env.value += Math.sqrt(p.getStat().getDEX()) * 6;
            env.value += p.getLevel();
        }
    }
    
    static class FuncAtkCritical extends Func
    {
        static final FuncAtkCritical _facInstance = new FuncAtkCritical();
        
        static Func getInstance() 
        {
            return _facInstance;
        }
        
        private FuncAtkCritical()
        {
            super(Stats.CRITICAL_RATE, 0x30, null);
        }
        
        public void calc(Env env)
        {
            L2Character p = env.player;
            if( p instanceof L2Summon) env.value = 40;
            else if (p instanceof L2PcInstance && p.getActiveWeaponInstance() == null) env.value = 40;
            else
            {
                env.value *= DEXbonus[p.getStat().getDEX()];
                env.value *= 10;
                if(env.value > 500)
                    env.value = 500;                
            }
        }
    }
    
    static class FuncMoveSpeed extends Func
    {
        static final FuncMoveSpeed _fmsInstance = new FuncMoveSpeed();
        
        static Func getInstance() 
        {
            return _fmsInstance;
        }
        
        private FuncMoveSpeed()
        {
            super(Stats.RUN_SPEED, 0x30, null);
        }
        
        public void calc(Env env)
        {
            L2PcInstance p = (L2PcInstance) env.player;
            env.value *= DEXbonus[p.getStat().getDEX()];
        }
    }
    
    static class FuncPAtkSpeed extends Func
    {
        static final FuncPAtkSpeed _fasInstance = new FuncPAtkSpeed();
        
        static Func getInstance() 
        {
            return _fasInstance;
        }
        
        private FuncPAtkSpeed()
        {
            super(Stats.POWER_ATTACK_SPEED, 0x20, null);
        }
        
        public void calc(Env env)
        {
            L2PcInstance p = (L2PcInstance) env.player;
            env.value *= DEXbonus[p.getStat().getDEX()];
        }
    }
    
    static class FuncMAtkSpeed extends Func
    {
        static final FuncMAtkSpeed _fasInstance = new FuncMAtkSpeed();
        
        static Func getInstance() 
        {
            return _fasInstance;
        }
        
        private FuncMAtkSpeed()
        {
            super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
        }
        
        public void calc(Env env)
        {
            L2PcInstance p = (L2PcInstance) env.player;
            env.value *= WITbonus[p.getStat().getWIT()];
        }
    }
    
    static class FuncMaxLoad extends Func
    {
        static final FuncMaxLoad _fmsInstance = new FuncMaxLoad();
        
        static Func getInstance() 
        {
            return _fmsInstance;
        }
        
        private FuncMaxLoad()
        {
            super(Stats.MAX_LOAD, 0x30, null);
        }
        
        public void calc(Env env)
        {
            L2PcInstance p = (L2PcInstance) env.player;
            env.value *= CONbonus[p.getStat().getCON()];
        }
    }
    
    
    static class FuncHennaSTR extends Func
    {
        static final FuncHennaSTR _fhInstance = new FuncHennaSTR();
        
        static Func getInstance() 
        {
            return _fhInstance;
        }
        
        private FuncHennaSTR()
        {
            super(Stats.STAT_STR, 0x10, null);
        }
        
        public void calc(Env env)
        {
            //          L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
            L2PcInstance pc = (L2PcInstance) env.player;
            if (pc != null) env.value += pc.getHennaStatSTR();
        }
    }
    
    static class FuncHennaDEX extends Func
    {
        static final FuncHennaDEX _fhInstance = new FuncHennaDEX();
        
        static Func getInstance() 
        {
            return _fhInstance;
        }
        
        private FuncHennaDEX()
        {
            super(Stats.STAT_DEX, 0x10, null);
        }
        
        public void calc(Env env)
        {
            //          L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
            L2PcInstance pc = (L2PcInstance) env.player;
            if (pc != null) env.value += pc.getHennaStatDEX();
        }
    }
    
    static class FuncHennaINT extends Func
    {
        static final FuncHennaINT _fhInstance = new FuncHennaINT();
        
        static Func getInstance() 
        {
            return _fhInstance;
        }
        
        private FuncHennaINT()
        {
            super(Stats.STAT_INT, 0x10, null);
        }
        
        public void calc(Env env)
        {
            //          L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
            L2PcInstance pc = (L2PcInstance) env.player;
            if (pc != null) env.value += pc.getHennaStatINT();
        }
    }
    
    static class FuncHennaMEN extends Func
    {
        static final FuncHennaMEN _fhInstance = new FuncHennaMEN();
        
        static Func getInstance() 
        {
            return _fhInstance;
        }
        
        private FuncHennaMEN()
        {
            super(Stats.STAT_MEN, 0x10, null);
        }
        
        public void calc(Env env)
        {
            //          L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
            L2PcInstance pc = (L2PcInstance) env.player;
            if (pc != null) env.value += pc.getHennaStatMEN();
        }
    }
    
    static class FuncHennaCON extends Func
    {
        static final FuncHennaCON _fhInstance = new FuncHennaCON();
        
        static Func getInstance() 
        {
            return _fhInstance;
        }
        
        private FuncHennaCON()
        {
            super(Stats.STAT_CON, 0x10, null);
        }
        
        public void calc(Env env)
        {
            //          L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
            L2PcInstance pc = (L2PcInstance) env.player;
            if (pc != null) env.value += pc.getHennaStatCON();
        }
    }
    
    static class FuncHennaWIT extends Func
    {
        static final FuncHennaWIT _fhInstance = new FuncHennaWIT();
        
        static Func getInstance() 
        {
            return _fhInstance;
        }
        
        private FuncHennaWIT()
        {
            super(Stats.STAT_WIT, 0x10, null);
        }
        
        public void calc(Env env)
        {
            //          L2PcTemplate t = (L2PcTemplate)env._player.getTemplate();
            L2PcInstance pc = (L2PcInstance) env.player;
            if (pc != null) env.value += pc.getHennaStatWIT();
        }
    }
    
    static class FuncMaxHpAdd extends Func
    {
        static final FuncMaxHpAdd _fmhaInstance = new FuncMaxHpAdd();
        
        static Func getInstance() 
        {
            return _fmhaInstance;
        }
        
        private FuncMaxHpAdd()
        {
            super(Stats.MAX_HP, 0x10, null);
        }
        
        public void calc(Env env)
        {
            L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
            int lvl = env.player.getLevel() - t.getClassBaseLevel();
            double hpmod = t.getLvlHpMod() * lvl;
            double hpmax = (t.getLvlHpAdd() + hpmod) * lvl;
            double hpmin = (t.getLvlHpAdd() * lvl) + hpmod;
            env.value += (hpmax + hpmin) / 2;
        }
    }
    
    static class FuncMaxHpMul extends Func
    {
        static final FuncMaxHpMul _fmhmInstance = new FuncMaxHpMul();
        
        static Func getInstance() 
        {
            return _fmhmInstance;
        }
        
        private FuncMaxHpMul()
        {
            super(Stats.MAX_HP, 0x20, null);
        }
        
        public void calc(Env env)
        {
            L2PcInstance p = (L2PcInstance) env.player;
            env.value *= CONbonus[p.getStat().getCON()];
        }
    }
    
    static class FuncMaxCpAdd extends Func
    {
        static final FuncMaxCpAdd _fmcaInstance = new FuncMaxCpAdd();
        
        static Func getInstance() 
        {
            return _fmcaInstance;
        }
        
        private FuncMaxCpAdd()
        {
            super(Stats.MAX_CP, 0x10, null);
        }
        
        public void calc(Env env)
        {
            L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
            int lvl = env.player.getLevel() - t.getClassBaseLevel();
            double cpmod = t.getLvlCpMod() * lvl;
            double cpmax = (t.getLvlCpAdd() + cpmod) * lvl;
            double cpmin = (t.getLvlCpAdd() * lvl) + cpmod;
            env.value += (cpmax + cpmin) / 2;
        }
    }
    
    static class FuncMaxCpMul extends Func
    {
        static final FuncMaxCpMul _fmcmInstance = new FuncMaxCpMul();
        
        static Func getInstance() 
        {
            return _fmcmInstance;
        }
        
        private FuncMaxCpMul()
        {
            super(Stats.MAX_CP, 0x20, null);
        }
        
        public void calc(Env env)
        {
            L2PcInstance p = (L2PcInstance) env.player;
            env.value *= CONbonus[p.getStat().getCON()];
        }
    }
    
    static class FuncMaxMpAdd extends Func
    {
        static final FuncMaxMpAdd _fmmaInstance = new FuncMaxMpAdd();
        
        static Func getInstance() 
        {
            return _fmmaInstance;
        }
        
        private FuncMaxMpAdd()
        {
            super(Stats.MAX_MP, 0x10, null);
        }
        
        public void calc(Env env)
        {
            L2PcTemplate t = (L2PcTemplate) env.player.getTemplate();
            int lvl = env.player.getLevel() - t.getClassBaseLevel();
            double mpmod = t.getLvlMpMod() * lvl;
            double mpmax = (t.getLvlMpAdd() + mpmod) * lvl;
            double mpmin = (t.getLvlMpAdd()* lvl) + mpmod;
            env.value += (mpmax + mpmin) / 2;
        }
    }
    
    static class FuncMaxMpMul extends Func
    {
        static final FuncMaxMpMul _fmmmInstance = new FuncMaxMpMul();
        
        static Func getInstance() 
        {
            return _fmmmInstance;
        }
        
        private FuncMaxMpMul()
        {
            super(Stats.MAX_MP, 0x20, null);
        }
        
        public void calc(Env env)
        {
            L2PcInstance p = (L2PcInstance) env.player;
            env.value *= MENbonus[p.getStat().getMEN()];
        }
    }
    
    private static final Formulas _instance = new Formulas();
    
    public static Formulas getInstance()
    {
        return _instance;
    }
    
    private Formulas() 
    {
    }
    
    
    /**
     * Return the period between 2 regenerations task (3s for L2Character, 5 min for L2DoorInstance).<BR><BR>
     */
    public int getRegeneratePeriod(L2Character cha)
    {
        if (cha instanceof L2DoorInstance) return HP_REGENERATE_PERIOD * 100; // 5 mins
        
        return HP_REGENERATE_PERIOD; // 3s
    }
    
    
    /**
     * Return the standard NPC Calculator set containing ACCURACY_COMBAT and EVASION_RATE.<BR><BR>
     *
     * <B><U> Concept</U> :</B><BR><BR>
     * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
     * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
     *
     * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
     *
     * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
     *
     */
    public Calculator[] getStdNPCCalculators()
    {
        Calculator[] std = new Calculator[Stats.NUM_STATS];
        
        // Add the FuncAtkAccuracy to the Standard Calculator of ACCURACY_COMBAT
        std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
        std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());
        
        // Add the FuncAtkEvasion to the Standard Calculator of EVASION_RATE
        std[Stats.EVASION_RATE.ordinal()] = new Calculator();
        std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
        
        return std;
    }
    
    
    /**
     * Add basics Func objects to L2PcInstance and L2Summon.<BR><BR>
     *
     * <B><U> Concept</U> :</B><BR><BR>
     * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
     * In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR><BR>
     *
     * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
     *
     * @param cha L2PcInstance or L2Summon that must obtain basic Func objects
     */
    public void addFuncsToNewCharacter(L2Character cha)
    {
        if (cha instanceof L2PcInstance)
        {
            cha.addStatFunc(FuncMaxHpAdd.getInstance());
            cha.addStatFunc(FuncMaxHpMul.getInstance());
            cha.addStatFunc(FuncMaxCpAdd.getInstance());
            cha.addStatFunc(FuncMaxCpMul.getInstance());
            cha.addStatFunc(FuncMaxMpAdd.getInstance());
            cha.addStatFunc(FuncMaxMpMul.getInstance());
            //cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
            //cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_CP_RATE));
            //cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
            cha.addStatFunc(FuncBowAtkRange.getInstance());
            //cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_ATTACK));
            //cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_DEFENCE));
            //cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENCE));
            if(Config.LEVEL_ADD_LOAD)
                cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAX_LOAD));
            cha.addStatFunc(FuncPAtkMod.getInstance());
            cha.addStatFunc(FuncMAtkMod.getInstance());
            cha.addStatFunc(FuncPDefMod.getInstance());
            cha.addStatFunc(FuncMDefMod.getInstance());
            cha.addStatFunc(FuncAtkCritical.getInstance());
            cha.addStatFunc(FuncAtkAccuracy.getInstance());
            cha.addStatFunc(FuncAtkEvasion.getInstance());
            cha.addStatFunc(FuncPAtkSpeed.getInstance());
            cha.addStatFunc(FuncMAtkSpeed.getInstance());
            cha.addStatFunc(FuncMoveSpeed.getInstance());
            cha.addStatFunc(FuncMaxLoad.getInstance());
            
            cha.addStatFunc(FuncHennaSTR.getInstance());
            cha.addStatFunc(FuncHennaDEX.getInstance());
            cha.addStatFunc(FuncHennaINT.getInstance());
            cha.addStatFunc(FuncHennaMEN.getInstance());
            cha.addStatFunc(FuncHennaCON.getInstance());
            cha.addStatFunc(FuncHennaWIT.getInstance());
        }
        else if (cha instanceof L2PetInstance)
        {
            cha.addStatFunc(FuncPAtkMod.getInstance());
            cha.addStatFunc(FuncMAtkMod.getInstance());
            cha.addStatFunc(FuncPDefMod.getInstance());
            cha.addStatFunc(FuncMDefMod.getInstance());
        }
        else if (cha instanceof L2Summon)
        {
            //cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
            //cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
            cha.addStatFunc(FuncAtkCritical.getInstance());
            cha.addStatFunc(FuncAtkAccuracy.getInstance());
            cha.addStatFunc(FuncAtkEvasion.getInstance());
        }
        
    }
    
    /**
     * Calculate the HP regen rate (base + modifiers).<BR><BR>
     */
    public final double calcHpRegen(L2Character cha)
    {
        double init = cha.getTemplate().getBaseHpReg();
        double hpRegenMultiplier;
        double hpRegenBonus = 0;

        if(cha.isRaid())
           hpRegenMultiplier=Config.RAID_HP_REGEN_MULTIPLIER;
        else if(cha instanceof L2PcInstance)
           hpRegenMultiplier=Config.PLAYER_HP_REGEN_MULTIPLIER;
        else 
           hpRegenMultiplier=Config.NPC_HP_REGEN_MULTIPLIER;
        
        if (cha.isChampion())
            hpRegenMultiplier *= Config.CHAMPION_HP_REGEN;

		// [L2J_JP ADD SANDMAN]
		// The recovery power of Zaken decreases under sunlight.
		if (cha instanceof L2BossInstance)
		{
			L2BossInstance boss = (L2BossInstance) cha;
			if ((boss.getNpcId() == 29022) && (ZoneManager.getInstance().checkIfInZone(ZoneType.BossDungeon, "Sunlight Room" , boss)) && (boss.getZ() >= -2952))
				hpRegenMultiplier *= 0.75;
		}
        
        if (cha instanceof L2PcInstance)
        {
            L2PcInstance player = (L2PcInstance) cha;

            // Calculate correct baseHpReg value for certain level of PC
            if(player.getLevel()>=71) init = 8.5;
            else if(player.getLevel()>=61) init = 7.5;
            else if(player.getLevel()>=51) init = 6.5;
            else if(player.getLevel()>=41) init = 5.5;
            else if(player.getLevel()>=31) init = 4.5;
            else if(player.getLevel()>=21) init = 3.5;
            else if(player.getLevel()>=11) init = 2.5;
            else init = 2.0;
            
            // SevenSigns Festival modifier
            if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant()) 
                init *= calcFestivalRegenModifier(player);
            else
            {
                double siegeModifier = calcSiegeRegenModifer(player);
                if (siegeModifier > 0) init *= siegeModifier;
            }
            
            if (player.getIsInClanHall() == 2 && player.getClan() != null)
            {
            	int clanHallIndex = player.getClan().getHasHideout();
            	if (clanHallIndex > 0) 
            	{
            		ClanHall clansHall = ClanHallManager.getInstance().getClanHall(clanHallIndex);
            		if(clansHall != null)
            			if (clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null) 
            				hpRegenMultiplier *= 1+ clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).getLvl()/100;
            	}
            }

            // Mother Tree effect is calculated at last
            if (player.getInMotherTreeZone()) hpRegenBonus += 2;

            // Calculate Movement bonus
            if (player.isSitting() && player.getLevel() < 41) // Sitting below lvl 40
            {
                init *= 1.5;
                hpRegenBonus += (40 - player.getLevel()) * 0.7;
            }
            else if (player.isSitting()) init *= 1.5;      // Sitting
            else if (!player.isRunning()) init *= 1.5; // Not Running
            else if (!player.isMoving()) init *= 1.1; // Staying
            else if (player.isRunning()) init *= 0.7; // Running
            // Add CON bonus
            init *= cha.getLevelMod() * CONbonus[cha.getStat().getCON()];
       }

        if (init < 1) init = 1;

        return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier + hpRegenBonus;
    }
    
    /**
     * Calculate the MP regen rate (base + modifiers).<BR><BR>
     */
    public final double calcMpRegen(L2Character cha)
    {
        double init = cha.getTemplate().getBaseMpReg();
        double mpRegenMultiplier;
        double mpRegenBonus = 0;
        
        if(cha.isRaid())
            mpRegenMultiplier=Config.RAID_MP_REGEN_MULTIPLIER;
        else if(cha instanceof L2PcInstance)
            mpRegenMultiplier=Config.PLAYER_MP_REGEN_MULTIPLIER;
        else 
            mpRegenMultiplier=Config.NPC_MP_REGEN_MULTIPLIER;
        
        if (cha instanceof L2PcInstance)
        {
            L2PcInstance player = (L2PcInstance) cha;

            // Calculate correct baseMpReg value for certain level of PC
            if(player.getLevel()>=71) init = 3.0;
            else if(player.getLevel()>=61) init = 2.7;
            else if(player.getLevel()>=51) init = 2.4;
            else if(player.getLevel()>=41) init = 2.1;
            else if(player.getLevel()>=31) init = 1.8;
            else if(player.getLevel()>=21) init = 1.5;
            else if(player.getLevel()>=11) init = 1.2;
            else init = 0.9;
            
            // SevenSigns Festival modifier
            if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
                init *= calcFestivalRegenModifier(player);

            // Mother Tree effect is calculated at last
            if (player.getInMotherTreeZone()) mpRegenBonus += 1;
            
            if (player.getIsInClanHall() == 2 && player.getClan() != null)
            {
            	int clanHallIndex = player.getClan().getHasHideout();
            	if (clanHallIndex > 0)
            	{
            		ClanHall clansHall = ClanHallManager.getInstance().getClanHall(clanHallIndex);
            		if(clansHall != null)
            			if (clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null) 
            				mpRegenMultiplier *= 1+ clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).getLvl()/100;
            	}
            }

            // Calculate Movement bonus
            if (player.isSitting()) init *= 2.5;      // Sitting.
            else if (!player.isRunning()) init *= 1.5; // Not running
            else if (!player.isMoving()) init *= 1.1; // Staying
            else if (player.isRunning()) init *= 0.7; // Running

            // Add MEN bonus
            init *= cha.getLevelMod() * MENbonus[cha.getStat().getMEN()];
       }

        if (init < 1) init = 1;

        return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier + mpRegenBonus;
    }
    
    /**
     * Calculate the CP regen rate (base + modifiers).<BR><BR>
     */
    public final double calcCpRegen(L2Character cha)
    {
        double init = cha.getTemplate().getBaseHpReg();
        double cpRegenMultiplier = Config.PLAYER_CP_REGEN_MULTIPLIER;
        double cpRegenBonus = 0;

        if (cha instanceof L2PcInstance)
        {
           L2PcInstance player = (L2PcInstance) cha;
   
           // Calculate correct baseHpReg value for certain level of PC
           init += (player.getLevel() > 10) ? ((player.getLevel()-1)/10.0) : 0.5;
           
           // Calculate Movement bonus
           if (player.isSitting()) init *= 1.5;      // Sitting
           else if (!player.isMoving()) init *= 1.1; // Staying
           else if (player.isRunning()) init *= 0.7; // Running
        } else
        {
           // Calculate Movement bonus
           if (!cha.isMoving()) init *= 1.1; // Staying
           else if (cha.isRunning()) init *= 0.7; // Running
        }
        
        // Apply CON bonus
        init *= cha.getLevelMod() * CONbonus[cha.getStat().getCON()];
        if (init < 1) init = 1;

        return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier + cpRegenBonus;
    }
    
    @SuppressWarnings("deprecation")
    public final double calcFestivalRegenModifier(L2PcInstance activeChar)
    {
        final int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(activeChar);
        final int oracle = festivalInfo[0];
        final int festivalId = festivalInfo[1];
        int[] festivalCenter;
        
        // If the player isn't found in the festival, leave the regen rate as it is.
        if (festivalId < 0) return 0;
        
        // Retrieve the X and Y coords for the center of the festival arena the player is in.
        if (oracle == SevenSigns.CABAL_DAWN) festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId];
        else festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];

        // Check the distance between the player and the player spawn point, in the center of the arena.
        double distToCenter = activeChar.getDistance(festivalCenter[0], festivalCenter[1]);
        
        if (_log.isDebugEnabled())
            _log.info("Distance: " + distToCenter + ", RegenMulti: " + (distToCenter * 2.5) / 50);
        
        return 1.0 - (distToCenter * 0.0005); // Maximum Decreased Regen of ~ -65%;
    }
    
    public final double calcSiegeRegenModifer(L2PcInstance activeChar)
    {
        if (activeChar == null || activeChar.getClan() == null) return 0;

        Siege siege = SiegeManager.getInstance().getSiege(activeChar);
        if (siege == null || !siege.getIsInProgress()) return 0;
        
        L2SiegeClan siegeClan = siege.getAttackerClan(activeChar.getClan().getClanId());
        if (siegeClan == null || siegeClan.getFlag().size() == 0
            || !Util.checkIfInRange(200, activeChar, siegeClan.getFlag().get(0), true)) return 0;

        return 1.5; // If all is true, then modifer will be 50% more
    }

	/** Calculate blow damage based on cAtk */
	public double calcBlowDamage(L2Character attacker, L2Character target, L2Skill skill, boolean shld, boolean ss)
	{
		double power = skill.getPower();
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		if(ss)
			damage *= 2.;
		if(shld)
			defence += target.getStat().getShldDef();
		if(ss && skill.getSSBoost()>0)
			power *= skill.getSSBoost();
		
		//Multiplier should be removed, it's false ??
		damage += 1.5*attacker.calcStat(Stats.CRITICAL_DAMAGE, damage+power, target, skill);
		//damage *= (double)attacker.getLevel()/target.getLevel();

		// get the natural vulnerability for the template
		if (target instanceof L2NpcInstance)
		{
			damage *= ((L2NpcInstance) target).getTemplate().getVulnerability(Stats.DAGGER_WPN_VULN);
		}
		// get the vulnerability for the instance due to skills (buffs, passives, toggles, etc)
		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		damage *= 70. / defence;
		damage += Rnd.get() * attacker.getRandomDamage(target);
		if (target instanceof L2PlayableInstance) //aura flare de-buff, etc
			damage *= skill.getPvpMulti();
		return damage < 1 ? 1. : damage;
	}

    /** Calculated damage caused by ATTACK of attacker on target,
     * called separatly for each weapon, if dual-weapon is used.
     *
     * @param attacker player or NPC that makes ATTACK
     * @param target player or NPC, target of ATTACK
     * @param miss one of ATTACK_XXX constants
     * @param crit if the ATTACK have critical success
     * @param dual if dual weapon is used
     * @param ss if weapon item was charged by soulshot
     * @return damage points
     */
    public final double calcPhysDam(L2Character attacker, L2Character target, L2Skill skill,
                                     boolean shld, boolean crit, boolean dual, boolean ss)
    {
       if (attacker instanceof L2PcInstance)
       {
           L2PcInstance pcInst = (L2PcInstance)attacker;
           if (pcInst.isGM() && pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
                   return 0;
       }

        double damage = attacker.getPAtk(target);
        double defence = target.getPDef(attacker);
        if (ss) damage *= 2;
        if (skill != null)
        {
        	double skillpower = skill.getPower();
        	float ssboost = skill.getSSBoost();
        	if (ssboost <= 0)
        		damage += skillpower;
        	else if (ssboost > 0)
        	{
        		if (ss)
        		{
        			skillpower *= ssboost;
        			damage += skillpower;
        		}
        		else
        			damage += skillpower;
        	}
        }
        // In C5 summons make 10 % less dmg in PvP.
        if(attacker instanceof L2Summon && target instanceof L2PcInstance) damage *= 0.9;
        
        // defence modifier depending of the attacker weapon
        L2Weapon weapon = attacker.getActiveWeaponItem();
        Stats stat = null;
        if (weapon != null)
        {
            switch (weapon.getItemType())
            {
                case BOW:
                    stat = Stats.BOW_WPN_VULN;
                    break;
                case BLUNT:
                case BIGBLUNT:
                    stat = Stats.BLUNT_WPN_VULN;
                    break;
                case DAGGER:
                    stat = Stats.DAGGER_WPN_VULN;
                    break;
                case DUAL:
                    stat = Stats.DUAL_WPN_VULN;
                    break;
                case DUALFIST:
                    stat = Stats.DUALFIST_WPN_VULN;
                    break;
                case ETC:
                    stat = Stats.ETC_WPN_VULN;
                    break;
                case FIST:
                    stat = Stats.FIST_WPN_VULN;
                    break;
                case POLE:
                    stat = Stats.POLE_WPN_VULN;
                    break;
                case SWORD:
                    stat = Stats.SWORD_WPN_VULN;
                    break;
                case BIGSWORD:
                    stat = Stats.SWORD_WPN_VULN;
                    break;
            }
        }


        if (crit) damage += attacker.getCriticalDmg(target, damage);
        if (shld && !Config.ALT_GAME_SHIELD_BLOCKS)
        {
            defence += target.getStat().getShldDef();
        }
        //if (!(attacker instanceof L2RaidBossInstance) && 
        /*
        if ((attacker instanceof L2NpcInstance || attacker instanceof L2SiegeGuardInstance))
        {
            if (attacker instanceof L2RaidBossInstance) damage *= 1; // was 10 changed for temp fix
            //          else
            //          damage *= 2;
            //          if (attacker instanceof L2NpcInstance || attacker instanceof L2SiegeGuardInstance){
            //damage = damage * attacker.getSTR() * attacker.getAccuracy() * 0.05 / defence;
            //          damage = damage * attacker.getSTR()*  (attacker.getSTR() + attacker.getLevel()) * 0.025 / defence;
            //          damage += _rnd.nextDouble() * damage / 10 ;
        }
        */
        //      else {
        //if (skill == null)
        damage = 70 * damage / defence;

        if (stat != null)
        {
            // get the vulnerability due to skills (buffs, passives, toggles, etc)
            damage = target.calcStat(stat, damage, target, null);
            if (target instanceof L2NpcInstance)
            {
                // get the natural vulnerability for the template
                damage *= ((L2NpcInstance) target).getTemplate().getVulnerability(stat);
            }
        }

        damage += Rnd.nextDouble() * damage / 10;
        //      damage += _rnd.nextDouble()* attacker.getRandomDamage(target);
        //      }
        if (shld && Config.ALT_GAME_SHIELD_BLOCKS)
        {           damage -= target.getStat().getShldDef();
            if (damage < 0) damage = 0;
        }
        if (target instanceof L2PcInstance && weapon != null && weapon.getItemType() == L2WeaponType.DAGGER && skill != null)
        {
            L2Armor armor = ((L2PcInstance)target).getActiveChestArmorItem();
            if (armor != null)
            {
                if(((L2PcInstance)target).isWearingHeavyArmor())
                    damage /= Config.ALT_DAGGER_DMG_VS_HEAVY;
                if(((L2PcInstance)target).isWearingLightArmor())
                    damage /= Config.ALT_DAGGER_DMG_VS_LIGHT;  
                if(((L2PcInstance)target).isWearingMagicArmor())
                    damage /= Config.ALT_DAGGER_DMG_VS_ROBE;   
            }            
        }

        if (attacker instanceof L2NpcInstance)
        {
            //Skill Race : Undead
            if (((L2NpcInstance)attacker).getTemplate().getRace() == L2NpcTemplate.Race.UNDEAD)
                damage /= attacker.getStat().getPDefUndead(target);
            //Skill Valakas
            if (((L2NpcInstance)attacker).getTemplate().getIdTemplate() == 12899)  damage /= attacker.getStat().getPDefValakas(target); 
            
        }
        if (target instanceof L2NpcInstance)
        {
            switch (((L2NpcInstance) target).getTemplate().getRace())
            {
                case UNDEAD:
                    damage *= attacker.getStat().getPAtkUndead(target);
                    break;
                case BEAST:
                    damage *= attacker.getStat().getPAtkMonsters(target);
                    break;
                case ANIMAL:
                    damage *= attacker.getStat().getPAtkAnimals(target);
                    break;
                case PLANT:
                    damage *= attacker.getStat().getPAtkPlants(target);
                    break;
                case DRAGON:
                    damage *= attacker.getStat().getPAtkDragons(target);
                    break;
                case BUG:
                    damage *= attacker.getStat().getPAtkInsects(target);
                    break;
                default:
                    // nothing
                    break;
            }
            //Skill Valakas
            if ( ((L2NpcInstance) target).getTemplate().getIdTemplate() == 12899) damage *= attacker.getStat().getPAtkValakas(target); 
        }
        
        if (skill != null) 
        {
            if (skill.getSkillType() == SkillType.FATALCOUNTER)
                damage *= (1.0 - attacker.getStatus().getCurrentHp()/attacker.getMaxHp()) * 2.0;
			
			if (target instanceof L2PlayableInstance) //aura flare de-buff, etc
				damage *= skill.getPvpMulti();
        }

        if (shld)
        {
            if (100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100)) 
            {
                damage = 1;
                target.sendPacket(new SystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
            }
        }
        if  (damage > 0 && damage < 1)
        {
            damage = 1;
        }
        else if (damage < 0)
        {
            damage = 0;
        }

        // Dmg bonusses in PvP fight
        if((attacker instanceof L2PcInstance || attacker instanceof L2Summon)
                && (target instanceof L2PcInstance || target instanceof L2Summon))
        {
            if(skill == null)
                damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1, null, null);
            else
                damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
        }

        if (attacker instanceof L2PcInstance){
           if (((L2PcInstance) attacker).getClassId().isMage())
            damage = damage*Config.ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
           else damage = damage*Config.ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;}
        else if (attacker instanceof L2Summon)
           damage = damage*Config.ALT_PETS_PHYSICAL_DAMAGE_MULTI;
        else if (attacker instanceof L2NpcInstance)
            damage = damage*Config.ALT_NPC_PHYSICAL_DAMAGE_MULTI;
        
        return damage;
    }
    
    public final double calcMagicDam(L2Character attacker, L2Character target, L2Skill skill,
                                        boolean ss, boolean bss, boolean mcrit)
    {
       if (attacker instanceof L2PcInstance)
       {
           L2PcInstance pcInst = (L2PcInstance)attacker;
           if (pcInst.isGM() && pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
               return 0;
       }
               
        double mAtk = attacker.getMAtk(target, skill);
        double mDef = target.getMDef(attacker, skill);
        if (bss) mAtk *= 4;
        else if (ss) mAtk *= 2;

        double damage = 91 * Math.sqrt(mAtk) / mDef * skill.getPower(attacker) * calcSkillVulnerability(target, skill);
       
        // In C5 summons make 10 % less dmg in PvP.
        if(attacker instanceof L2Summon && target instanceof L2PcInstance) damage *= 0.9;
       
        //      if(attacker instanceof L2PcInstance && target instanceof L2PcInstance) damage *= 0.9; // PvP modifier (-10%)
        
        // Failure calculation
        if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
        {
            if (attacker instanceof L2PcInstance)
            {
                if (calcMagicSuccess(attacker, target, skill)
                    && (target.getLevel() - attacker.getLevel()) <= 9)
                {
                    if (skill.getSkillType() == SkillType.DRAIN) attacker.sendPacket(new SystemMessage(

                                                                                                        SystemMessageId.DRAIN_HALF_SUCCESFUL));
                    else attacker.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));

                    damage /= 2;
                }
                else
                {
                    SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                    sm.addString(target.getName());
                    sm.addSkillName(skill.getId());
                    attacker.sendPacket(sm);

                    damage = 1;
                }
            }

            if (target instanceof L2PcInstance)
            {
                if (skill.getSkillType() == SkillType.DRAIN)
                {
                    SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_DRAIN);
                    sm.addString(attacker.getName());
                    target.sendPacket(sm);
                }
                else
                {
                    SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_MAGIC);
                    sm.addString(attacker.getName());
                    target.sendPacket(sm);
                }
            }
        }
        else if (mcrit) damage *= 4;
        
        // Pvp bonusses for dmg
		if((attacker instanceof L2PcInstance || attacker instanceof L2Summon)
				&& (target instanceof L2PcInstance || target instanceof L2Summon))
        {
            if(skill.isMagic())
                damage *= attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1, null, null);
            else
                damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
        }        

        if (attacker instanceof L2PcInstance){
           if (((L2PcInstance) attacker).getClassId().isMage())
            damage = damage*Config.ALT_MAGES_MAGICAL_DAMAGE_MULTI;
           else damage = damage*Config.ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;}
        else if (attacker instanceof L2Summon)
           damage = damage*Config.ALT_PETS_MAGICAL_DAMAGE_MULTI;
        else if (attacker instanceof L2NpcInstance)
            damage = damage*Config.ALT_NPC_MAGICAL_DAMAGE_MULTI;
		
		if (skill != null)
        {
			if (target instanceof L2PlayableInstance) //aura flare de-buff, etc
				damage *= skill.getPvpMulti();
			
            if (skill.getSkillType() == SkillType.DEATHLINK)
                damage = damage * (1.0 - attacker.getStatus().getCurrentHp()/attacker.getMaxHp()) * 2.0;
        }
		
        return damage;
    }

	/** Returns true in case of critical hit */
	public final boolean calcCrit(double rate)
	{
		return rate > Rnd.get(1000);
	}
	/** Calcul value of blow success */
	public final boolean calcBlow(L2Character activeChar, L2Character target, int chance)
	{
		return activeChar.calcStat(Stats.BLOW_RATE, chance*(1.0+(activeChar.getStat().getDEX()-20)/100), target, null)>Rnd.get(100);
	}
	/** Calcul value of lethal chance */
	public final double calcLethal(L2Character activeChar, L2Character target, int baseLethal)
	{
		return activeChar.calcStat(Stats.LETHAL_RATE, (baseLethal*((double)activeChar.getLevel()/target.getLevel())), target, null);
	}
	
    /** Returns true in case of critical hit */
    public final boolean calcCrit(L2Character attacker, L2Character target, double rate)
    {
        int critHit = Rnd.get(1000);
        if(attacker instanceof L2PcInstance)
        {
            if(attacker.isBehindTarget())
                critHit = Rnd.get(700);
            else if(!attacker.isInFront(target, 60) && !attacker.isBehindTarget())
                critHit = Rnd.get(800);
            critHit = Rnd.get(900);
        }
        return rate > critHit;
    }
    
    public final boolean calcMCrit(double mRate)
    {
    	return mRate > Rnd.get(1000);
    }
    
    /** Returns true in case when ATTACK is canceled due to hit */
   public final boolean calcAtkBreak(L2Character target, double dmg)
   {
        if (target.isRaid()) return false;
		
		double init = 0;

       if (Config.ALT_GAME_CANCEL_CAST && target.isCastingNow()) init = 50;
       if (Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
       {
           L2Weapon wpn = target.getActiveWeaponItem();
           if (wpn != null && wpn.getItemType() == L2WeaponType.BOW) init = 15;
       }

        if (init <= 0) return false; // No attack break

        // Chance of break is higher with higher dmg
        init += Math.sqrt(13*dmg);    

        // Chance is affected by target MEN
        init -= (MENbonus[target.getStat().getMEN()] * 100 - 100);

        // Calculate all modifiers for ATTACK_CANCEL
        double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null); 

        // Adjust the rate to be between 1 and 99
        if (rate > 99) rate = 99;
        else if (rate < 1) rate = 1;

        return Rnd.get(100) < rate;
    }
    
    /** Calculate delay (in milliseconds) before next ATTACK */
    public final int calcPAtkSpd(@SuppressWarnings("unused") L2Character attacker,
                                 @SuppressWarnings("unused") L2Character target, double atkSpd, double base)
	{
		if (attacker instanceof L2PcInstance)
			base *= Config.ALT_ATTACK_DELAY;
		
		if (atkSpd < 10)
			atkSpd = 10;
		
		return (int)(base/atkSpd);
	}
    
    /** Calculate delay (in milliseconds) for skills cast */
    public final int calcMAtkSpd(L2Character attacker, @SuppressWarnings("unused")
    L2Character target, L2Skill skill, double time)
    {
        if (skill.isMagic()) return (int) (time * 333 / attacker.getMAtkSpd());
        return (int) (time * 333 / attacker.getPAtkSpd());

    }

    /** Calculate delay (in milliseconds) for skills cast */
    public final int calcMAtkSpd(L2Character attacker, L2Skill skill, double time)
    {
        if (skill.isMagic()) return (int) (time * 333 / attacker.getMAtkSpd());
        return (int) (time * 333 / attacker.getPAtkSpd());
    }
    
    /** Returns true if hit missed (taget evaded) */
    public boolean calcHitMiss(L2Character attacker, L2Character target) 
    {
        // accuracy+dexterity => probability to hit in percents
        int acc_attacker;
        int evas_target;
        acc_attacker = attacker.getAccuracy();
        if(attacker instanceof L2PcInstance)
        {
            if(attacker.isBehindTarget())
                acc_attacker +=10;
            else if(!attacker.isInFront(target, 60) && !attacker.isBehindTarget())
                acc_attacker +=5;
            if(attacker.getZ()-target.getZ() >= 32)
                acc_attacker +=3;
            else if(attacker.getZ()-target.getZ() <= -32)
                acc_attacker -=3;
            if(GameTimeController.getInstance().isNowNight())
                acc_attacker -=10;
        }
        evas_target = target.getEvasionRate(attacker);
        int d = (int) (10 * Math.pow(1.1, evas_target - acc_attacker));
        if(d > 75) d = 75; // max chance
        if(d < 5) d = 5;  // min chance
        return Rnd.get(100) > (100 - d);
	}

    /** Returns true if shield defence successfull */
    public boolean calcShldUse(L2Character attacker, L2Character target) 
    {
        double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null)
            * DEXbonus[target.getStat().getDEX()];
        
		if (shldRate == 0.0) return false;
		
		double shldAngle = target.calcStat(Stats.SHIELD_ANGLE, 60, null, null);
		
        if (!target.isInFront(attacker, shldAngle))
            return false;
		
        // if attacker use bow and target wear shield, shield block rate is multiplied by 1.5 (50%)
		if (attacker != null && attacker.getActiveWeaponItem() != null 
			&& attacker.getActiveWeaponItem().getItemType() == L2WeaponType.BOW
		)
            shldRate *= 1.5;
		
        return Rnd.get(100) < shldRate;
    }

	// This should be deprecated and calcSkillSuccess() should be used instead
    public boolean calcMagicAffected(L2Character actor, L2Character target, L2Skill skill)
    {
        double defence = 0;
        //FIXME: CHECK/FIX THIS FORMULA UP!!
        double attack = 0; 
        
        if (!target.checkSkillCanAffectMyself(skill))
            return false;
        
        if (skill.isActive() && skill.isOffensive())
            defence = target.getMDef(actor, skill);
        
        if (actor instanceof L2PcInstance) 
            attack = 3.7 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);
        else
            attack = 2 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);

        double d = attack - defence;
        d /= attack + defence;
        d += 0.5 * Rnd.nextGaussian();
        return d > 0;
    }

	public double calcSkillVulnerability(L2Character target, L2Skill skill)
	{
		return calcSkillVulnerability(target, skill, skill.getSkillType());
	}
	
	public double calcSkillVulnerability(L2Character target, L2Skill skill, SkillType type)
	{
		double multiplier = 1; // initialize...

		// Get the skill type to calculate its effect in function of base stats
		// of the L2Character target
		if (skill != null)
		{
			// first, get the natural template vulnerability values for the target
			Stats stat = skill.getStat();
			if (stat != null)
			{
				switch (stat)
				{
				case AGGRESSION:
					multiplier *= target.getTemplate().baseAggressionVuln;
					break;
				case BLEED:
					multiplier *= target.getTemplate().baseBleedVuln;
					break;
				case POISON:
					multiplier *= target.getTemplate().basePoisonVuln;
					break;
				case STUN:
					multiplier *= target.getTemplate().baseStunVuln;
					break;
				case ROOT:
					multiplier *= target.getTemplate().baseRootVuln;
					break;
				case MOVEMENT:
					multiplier *= target.getTemplate().baseMovementVuln;
					break;
				case CONFUSION:
					multiplier *= target.getTemplate().baseConfusionVuln;
					break;
				case SLEEP:
					multiplier *= target.getTemplate().baseSleepVuln;
					break;
				case FIRE:
					multiplier *= target.getTemplate().baseFireVuln;
					break;
				case WIND:
					multiplier *= target.getTemplate().baseWindVuln;
					break;
				case WATER:
					multiplier *= target.getTemplate().baseWaterVuln;
					break;
				case EARTH:
					multiplier *= target.getTemplate().baseEarthVuln;
					break;
				case HOLY:
					multiplier *= target.getTemplate().baseHolyVuln;
					break;
				case DARK:
					multiplier *= target.getTemplate().baseDarkVuln;
					break;
				}
			}

			// Next, calculate the elemental vulnerabilities
			switch (skill.getElement())
			{
			case L2Skill.ELEMENT_EARTH:
				multiplier = target.calcStat(Stats.EARTH_VULN, multiplier, target, skill);
				break;
			case L2Skill.ELEMENT_FIRE:
				multiplier = target.calcStat(Stats.FIRE_VULN, multiplier, target, skill);
				break;
			case L2Skill.ELEMENT_WATER:
				multiplier = target.calcStat(Stats.WATER_VULN, multiplier, target, skill);
				break;
			case L2Skill.ELEMENT_WIND:
				multiplier = target.calcStat(Stats.WIND_VULN, multiplier, target, skill);
				break;
			case L2Skill.ELEMENT_HOLY:
				multiplier = target.calcStat(Stats.HOLY_VULN, multiplier, target, skill);
				break;
			case L2Skill.ELEMENT_DARK:
				multiplier = target.calcStat(Stats.DARK_VULN, multiplier, target, skill);
				break;
			}
			
			// Finally, calculate skilltype vulnerabilities
			if (type != null)
			{
				switch (type)
				{
					case BLEED:
						multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
						break;
					case POISON:
						multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
						break;
					case STUN:
						multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
						break;
					case PARALYZE:
						multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
						break;
					case ROOT:
						multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
						break;
					case SLEEP:
						multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
						break;
					case MUTE:
					case FEAR:
					case BETRAY:
					case AGGREDUCE_CHAR:
						multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
						break;
					case CONFUSION:
						multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
						break;
					case DEBUFF:
					case WEAKNESS:
						multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
						break;
				}
			}
		}
		return multiplier;
	}

	public double calcSkillStatModifier(SkillType type, L2Character target)
	{
		double multiplier = 1;
		if (type == null) return multiplier;
		switch (type)
		{
			case STUN:
			case BLEED:
				multiplier = 2 - Math.sqrt(CONbonus[target.getStat().getCON()]);
				break;
			case POISON:
			case SLEEP:
			case DEBUFF:
			case WEAKNESS:
			case ERASE:
			case ROOT:
			case MUTE:
			case FEAR:
			case BETRAY:
			case CONFUSION:
			case AGGREDUCE_CHAR:
			case PARALYZE:
				multiplier = 2 - Math.sqrt(MENbonus[target.getStat().getMEN()]);
				break;
			default:
				return multiplier;
		}
		if (multiplier < 0)
			multiplier = 0;
		return multiplier;
	}
 
    public boolean calcSkillSuccess(L2Character attacker, L2Character target, L2Skill skill, boolean ss, boolean sps, boolean bss)
    {
        if (!target.checkSkillCanAffectMyself(skill))
            return false;
        
        int value = (int) skill.getPower();
        int lvlDepend = skill.getLevelDepend();

		SkillType type = skill.getSkillType();
        
        if (type == SkillType.PDAM || type == SkillType.MDAM || type == SkillType.DRAIN)
        {
            value = skill.getEffectPower();
            type = skill.getEffectType();
        }
		
		// FIXME: Skills should be checked to be able to remove this dirty check
		if (type == null)
		{
			if (_log.isDebugEnabled())
				_log.debug("Skill ID: " + skill.getId() + " hasn't got definied type!");
			
			if (skill.getSkillType() == SkillType.PDAM)
				type = SkillType.STUN;
			else if (skill.getSkillType() == SkillType.MDAM || type == SkillType.DRAIN)
				type = SkillType.PARALYZE;
		}
		
        if (!target.checkSkillCanAffectMyself(type))
            return false;
        
		if (value == 0)
		{
			if (_log.isDebugEnabled())
				_log.debug("Skill ID: " + skill.getId() + " hasn't got definied power!");
			value = 20; //To avoid unbalanced overpowered skills...
		}
		
		if (lvlDepend == 0)
		{
			if (_log.isDebugEnabled())
				_log.debug("Skill ID: " + skill.getId() + " hasn't got definied lvlDepend!");
			lvlDepend = 1; //To avoid unbalanced overpowered skills...
		}
		
        //FIXME: Temporary fix for NPC skills with MagicLevel not set
        // int lvlmodifier = (skill.getMagicLevel() - target.getLevel()) * lvlDepend;
        int lvlmodifier = lvlDepend * (
			(skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()) - target.getLevel());
        double statmodifier = calcSkillStatModifier(type, target);
        double resmodifier = calcSkillVulnerability(target, skill, type);
        
        int ssmodifier = 100;
        if (bss) ssmodifier = 150;
        else if (sps || ss) ssmodifier = 125;        
        
        int rate = (int) ((value * statmodifier + lvlmodifier) * resmodifier);
        if (skill.isMagic())
            rate += (int) (Math.pow((double) attacker.getMAtk(target, skill)
                / target.getMDef(attacker, skill), 0.1) * 100) - 100;

        if (rate > 99) rate = 99;
        else if (rate < 1) rate = 1;
        
        if (ssmodifier != 100)
        {
            if (rate > 10000 / (100 + ssmodifier)) rate = 100 - (100 - rate) * 100 / ssmodifier;
            else rate = rate * ssmodifier / 100;
        }
 
        if (_log.isDebugEnabled())
            _log.debug(skill.getName()
                + ": "
                + value
                + ", "
                + statmodifier
                + ", "
                + lvlmodifier
                + ", "
                + resmodifier
                + ", "
                + ((int) (Math.pow((double) attacker.getMAtk(target, skill)
                    / target.getMDef(attacker, skill), 0.2) * 100) - 100) + ", " + ssmodifier + " ==> "
                + rate);
            return (Rnd.get(100) <= rate);
    }
    
    public boolean calcMagicSuccess(L2Character attacker, L2Character target, L2Skill skill)
    {
		double lvlDifference = (target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()));
		int rate = Math.round((float)(Math.pow(1.3, lvlDifference) * 100));

		return (Rnd.get(10000) > rate);
    }
    
    public boolean calculateUnlockChance(L2Skill skill)
    {
        int level = skill.getLevel();
        int chance = 0;
        switch (level)
        {
            case 1:
                chance = 30;
                break;
                
            case 2:
                chance = 50;
                break;
                
            case 3:
                chance = 75;
                break;
                
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                chance = 100;
                break;
        }
        if(Rnd.get(120) > chance)
        {
            return false;
        }
        
        return true;
    }

/*    public int calculateEnchantSkillSuccessRate(int skillLvl, int playerLvl)
    {
        int successRate=0,_skillLvl;
        if(skillLvl>140) _skillLvl=skillLvl-140;
        else _skillLvl=skillLvl-100;
        if (playerLvl>=78 && _skillLvl==1) successRate=97;
        else if (playerLvl>=78 && _skillLvl==2) successRate=95;
        else if (playerLvl>=78 && _skillLvl==3) successRate=93;
        else if ((playerLvl>=78 && _skillLvl==4) || (playerLvl==77 && _skillLvl==1)) successRate=92;
        else if ((playerLvl>=78 && _skillLvl==5) || (playerLvl==77 && _skillLvl==2)) successRate=90;
        else if ((playerLvl>=78 && _skillLvl==6) || (playerLvl==77 && _skillLvl==3)) successRate=88;
        else if ((playerLvl>=78 && _skillLvl==7) || (playerLvl==77 && _skillLvl==4)) successRate=82;
        else if ((playerLvl>=78 && _skillLvl==8) || (playerLvl==77 && _skillLvl==5)) successRate=80;
        else if ((playerLvl>=78 && _skillLvl==9) || (playerLvl==77 && _skillLvl==6)) successRate=78;
        else if ((playerLvl>=78 && _skillLvl==10) || (playerLvl==77 && _skillLvl==7)) successRate=40;
        else if ((playerLvl>=78 && _skillLvl==11) || (playerLvl==77 && _skillLvl==8)) successRate=30;
        else if ((playerLvl>=78 && _skillLvl==12) || (playerLvl==77 && _skillLvl==9)) successRate=20;
        else if ((playerLvl>=78 && _skillLvl==13) || (playerLvl==77 && _skillLvl==10)) successRate=14;
        else if ((playerLvl>=78 && _skillLvl==14) || (playerLvl==77 && _skillLvl==11)) successRate=10;
        else if ((playerLvl>=78 && _skillLvl==15) || (playerLvl==77 && _skillLvl==12)) successRate=6;
        else if ((playerLvl>=78 && _skillLvl>15 && _skillLvl<19) || (playerLvl==77 && _skillLvl>12 && _skillLvl<16)) successRate=2;
        else if ((playerLvl>=78 && _skillLvl>18 && _skillLvl<30) || (playerLvl==77 && _skillLvl>15 && _skillLvl<26)) successRate=1;
        else if (playerLvl==76 && _skillLvl==1) successRate=82;
        else if (playerLvl==76 && _skillLvl==2) successRate=80;
        else if (playerLvl==76 && _skillLvl==3) successRate=78;
        else if (playerLvl==76 && _skillLvl==4) successRate=40;
        else if (playerLvl==76 && _skillLvl==5) successRate=30;
        else if (playerLvl==76 && _skillLvl==6) successRate=20;
        else if (playerLvl==76 && _skillLvl==7) successRate=14;
        else if (playerLvl==76 && _skillLvl==8) successRate=10;
           
        return successRate;
    }*/
    
    public double calcManaDam(L2Character attacker, L2Character target, L2Skill skill,
           boolean ss, boolean bss)
    {
        //Mana Burnt = (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
        double mAtk = attacker.getMAtk(target, skill);
        double mDef = target.getMDef(attacker, skill);
        double mp = target.getMaxMp();
        if (bss) mAtk *= 4;
        else if (ss) mAtk *= 2;

        double damage = (Math.sqrt(mAtk) * skill.getPower(attacker) * (mp/97)) / mDef;
        damage *= calcSkillVulnerability(target, skill);
		if (target instanceof L2PlayableInstance) //aura flare de-buff, etc
			damage *= skill.getPvpMulti();
        return damage;
    }
    
    public double calculateSkillResurrectRestorePercent(double baseRestorePercent, int casterWIT)
    {
       double restorePercent = baseRestorePercent;
       double modifier = WITbonus[casterWIT];

       if(restorePercent != 100 && restorePercent != 0) {
                       
           restorePercent = baseRestorePercent * modifier;
                       
           if(restorePercent - baseRestorePercent > 20.0) 
               restorePercent = baseRestorePercent + 20.0;
       }
               
       if(restorePercent > 100) 
           restorePercent = 100;
       if(restorePercent < baseRestorePercent) 
           restorePercent = baseRestorePercent;
                   
       return restorePercent;
    }
    
    public double getSTRBonus(L2Character activeChar)
    {
    	return STRbonus[activeChar.getStat().getSTR()];
    }
    
    public boolean receiveBlock(L2Character cha, String type)
    {
        if(type.equalsIgnoreCase("buff"))
        {
            return cha.calcStat(Stats.BLOCK_RECEIVE_BUFF, 0, null, null) > 0;
        }
        
        if(type.equalsIgnoreCase("debuff"))
        {
            return cha.calcStat(Stats.BLOCK_RECEIVE_DEBUFF, 0, null, null) > 0;
        }
        
        if(type.equalsIgnoreCase("damage"))
        {
            return cha.calcStat(Stats.BLOCK_RECEIVE_DAMAGE, 0, null, null) > 0;
        }
        
        return false;
    }    
}
