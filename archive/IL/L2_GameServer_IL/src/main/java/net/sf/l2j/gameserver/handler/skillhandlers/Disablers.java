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
package net.sf.l2j.gameserver.handler.skillhandlers; 

import java.io.IOException;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2AttackableAI;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.skills.funcs.Func;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * This Handles Disabler skills
 * @author _drunk_ 
 */
public class Disablers implements ISkillHandler
{
    private static final SkillType[] SKILL_IDS = {SkillType.STUN,
                                       SkillType.ROOT,
                                       SkillType.SLEEP,
                                       SkillType.CONFUSION,
                                       SkillType.AGGDAMAGE, 
                                       SkillType.AGGREDUCE,
                                       SkillType.AGGREDUCE_CHAR, 
                                       SkillType.AGGREMOVE,
                                       SkillType.UNBLEED, 
                                       SkillType.UNPOISON,
                                       SkillType.MUTE, 
                                       SkillType.FAKE_DEATH,
                                       SkillType.CONFUSE_MOB_ONLY,
                                       SkillType.NEGATE,
                                       SkillType.CANCEL, 
                                       SkillType.PARALYZE,
                                       SkillType.UNSUMMON_ENEMY_PET,
                                       SkillType.BETRAY,
                                       SkillType.CANCEL_TARGET,
                                       SkillType.ERASE,
                                       SkillType.DEBUFF,
                                       SkillType.MAGE_BANE,
                                       SkillType.WARRIOR_BANE};
    protected static Log _log = LogFactory.getLog(L2Skill.class.getName());
    private  String[] _negateStats=null;
    private  float _negatePower=0.f;
    private int _negateId=0;
    
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        SkillType type = skill.getSkillType();

        boolean ss = false;
        boolean sps = false;
        boolean bss = false;

        L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

        if (activeChar instanceof L2PcInstance)
        {
            if (weaponInst == null && skill.isOffensive())
            {
                SystemMessage sm2 = new SystemMessage(SystemMessageId.S1_S2);
                sm2.addString("You must equip a weapon before casting a spell.");
                activeChar.sendPacket(sm2);
                return;
            }
        }

        if (weaponInst != null)
        {
            if (skill.isMagic())
            {
                if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
                {
                    bss = true;
                    if (skill.getId() != 1020) // vitalize
                        weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
                }
                else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
                {
                    sps = true;
                    if (skill.getId() != 1020) // vitalize
                        weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
                }
            }
            else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
            {
                ss = true;
                if (skill.getId() != 1020) // vitalize
                    weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
            }
        }
        // If there is no weapon equipped, check for an active summon.
        else if (activeChar instanceof L2Summon)
        {
            L2Summon activeSummon = (L2Summon) activeChar;

            if (skill.isMagic())
            {
                if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
                {
                    bss = true;
                    activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
                }
                else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
                {
                    sps = true;
                    activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
                }
            }
            else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
            {
                ss = true;
                activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
            }
        }
        for (L2Object element : targets) {
            // Get a target L2Character targets
            if (!(element instanceof L2Character)) continue;
   
            L2Character target = (L2Character) element;
                       
            if (target == null || target.isDead()) //bypass if target is null or dead
                continue;
            //check if skill is allowed on other.properties for raidbosses
            if(!target.checkSkillCanAffectMyself(skill))
                continue;
            
            switch (type)
            {
                case CANCEL_TARGET:
                {
                    if (target instanceof L2NpcInstance){
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,50);
                    }
                    target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                    target.setTarget(null);
                    target.breakAttack();
                    target.breakCast();
                    target.abortAttack();
                    target.abortCast();
                    if ( target != null && activeChar instanceof L2PcInstance && Rnd.get(100) < skill.getLandingPercent())
                    {
                        skill.getEffects(activeChar, target);
                        SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                        sm.addSkillName(skill.getId());
                        target.sendPacket(sm);
                    }
                    else
                    {
                        SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getId());
                        activeChar.sendPacket(sm);
                    }
                    break;
                }
                case BETRAY:
                {
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                        skill.getEffects(activeChar, target);
                    else
                    {
                        SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getId());
                        activeChar.sendPacket(sm);
                    }
                    break;
                }
                case UNSUMMON_ENEMY_PET:
                {
                    if (target != null && target instanceof L2Summon && Rnd.get(100) < skill.getLandingPercent())
                    {  
                        L2PcInstance targetOwner = null;
                        targetOwner = ((L2Summon)target).getOwner();
                        L2Summon Pet = null;
                        Pet = targetOwner.getPet();
                        Pet.unSummon(targetOwner);
                    }
                    break;
                }
                case FAKE_DEATH:
                {
                    // stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
                    skill.getEffects(activeChar, target);
                    break;
                }
                case ROOT:
                case STUN:
                {
                    if(target.reflectSkill(skill))
                       target = activeChar;
                    
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                    {   
                        skill.getEffects(activeChar, target);
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case SLEEP:
                case PARALYZE: //use same as root for now
                {   
                    if(target.reflectSkill(skill))
                       target = activeChar;
                    
                    if (target instanceof L2NpcInstance){
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,50);
                    }
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                    {
                        skill.getEffects(activeChar, target);
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case CONFUSION:
                case DEBUFF:
                {    
                    if (target instanceof L2NpcInstance){
                    target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,50);}
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                    {   
                        // stop same type effect if avaiable
                        L2Effect[] effects = target.getAllEffects();
                        for (L2Effect e : effects)
                        {
                            if (e.getSkill().getSkillType() == type) e.exit();
                        }
                        // then restart
                        // Make above skills mdef dependant
                        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                        {
                            skill.getEffects(activeChar, target);
                        }
                        else
                        {
                            if (activeChar instanceof L2PcInstance)
                            {
                                SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                                sm.addString(target.getName());
                                sm.addSkillName(skill.getDisplayId());
                                activeChar.sendPacket(sm);
                            }
                        }
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getDisplayId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case MUTE:
                {    
                    if(target.reflectSkill(skill))
                       target = activeChar;

                    if (target instanceof L2NpcInstance){
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,50);}
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                    {   
                        // stop same type effect if avaiable
                        L2Effect[] effects = target.getAllEffects();
                        for (L2Effect e : effects)
                        {
                            if (e.getSkill().getSkillType() == type) e.exit();
                        }
                        // then restart
                        // Make above skills mdef dependant
                        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                            //if(Formulas.getInstance().calcMagicAffected(activeChar, target, skill))
                        {
                            skill.getEffects(activeChar, target);
                        }
                        else
                        {
                            if (activeChar instanceof L2PcInstance)
                            {
                                SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2); 
                                sm.addString(target.getName());
                                sm.addSkillName(skill.getDisplayId());
                                activeChar.sendPacket(sm);
                            }
                        }
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2); 
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getDisplayId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case CONFUSE_MOB_ONLY:
                {
                    // do nothing if not on mob
                    if (target instanceof L2Attackable) skill.getEffects(activeChar, target);
                    else activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                    break;
                }
                case AGGDAMAGE:
                {  
        
                    if (target instanceof L2PcInstance && Rnd.get(100) < 75)
                    {
                        L2PcInstance PCChar= null;
                        PCChar = ((L2PcInstance)target);
                        if (PCChar != null && 
                                ((PCChar.getPvpFlag() !=0 
                                        || PCChar.isInOlympiadMode() 
                                        || PCChar.isInCombat() 
                                        || ZoneManager.getInstance().checkIfInZonePvP(PCChar))
                                ))
                        {
                            PCChar.setTarget(activeChar); //c5 hate PvP
                            PCChar.abortAttack();
                            PCChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK,activeChar);
                        }
                    }   
                    if (target instanceof L2Attackable && skill.getId()!= 368)
                    {  
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,
                                                   (int) skill.getPower());
                        break;
                    }
                    else 
                    {
                        if (target instanceof L2Attackable)
                        {
                            {
                                if (skill.getId()== 368) //Vengeance
                                {
                                    L2PcInstance PCChar= null;
                                    if(target instanceof L2PcInstance)
                                    {
                                        PCChar = ((L2PcInstance)target);
                                        if (PCChar != null && 
                                                ((PCChar.getPvpFlag() !=0 
                                                        || PCChar.isInOlympiadMode() 
                                                        || PCChar.isInCombat() 
                                                        || ZoneManager.getInstance().checkIfInZonePvP(PCChar))
                                                ))
                                        {
                                            target.setTarget(activeChar);
                                            target.getAI().setAutoAttacking(true);
                                            if (target instanceof L2PcInstance){
                                                target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK,activeChar);}
                                        }
                                    }
                                    target.setTarget(activeChar); //c5 hate PvP
                                    activeChar.stopSkillEffects(skill.getId());
                                    skill.getEffects(activeChar, activeChar);
                                    target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) ((150*skill.getPower())/(target.getLevel()+7)));
                                }
                            }     
                        }
                    }        
                    break;
                }
                case AGGREDUCE:
                {
                    //these skills needs to be rechecked
                    if (target instanceof L2Attackable)
                    {
                        skill.getEffects(activeChar, target);

                        double aggdiff = ((L2Attackable)target).getHating(activeChar) 
                                                    - target.calcStat(Stats.AGGRESSION, ((L2Attackable)target).getHating(activeChar), target, skill); 
                        
                        if (skill.getPower() > 0)
                            ((L2Attackable)target).reduceHate(null, (int) skill.getPower());
                        else if (aggdiff > 0)
                            ((L2Attackable)target).reduceHate(null, (int) aggdiff);
                    }
                    break;
                }
                case AGGREDUCE_CHAR:
                {
                    //these skills needs to be rechecked
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                    {
                        if (target instanceof L2Attackable)
                        {
                            L2Attackable targ = (L2Attackable)target;
                            targ.stopHating(activeChar);
                            if (targ.getMostHated() == null)
                            {
                                ((L2AttackableAI)targ.getAI()).setGlobalAggro(-25);
                                targ.clearAggroList();
                                targ.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                                targ.setWalking();    
                            }
                        }
                        skill.getEffects(activeChar, target);
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case AGGREMOVE:
                {
                    // 1034 = repose, 1049 = requiem
                    //these skills needs to be rechecked
                    if (target instanceof L2Attackable && !target.isRaid())
                    {
                        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                        {
                            ((L2Attackable)target).reduceHate(null, ((L2Attackable)target).getHating(((L2Attackable)target).getMostHated()));
                        }
                        else
                        {
                            if (activeChar instanceof L2PcInstance)
                            {
                                SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                                sm.addString(target.getName());
                                sm.addSkillName(skill.getId());
                                activeChar.sendPacket(sm);
                            }
                        }
                    }
                    break;
                }
                case UNBLEED:
                {
                    negateEffect(target,SkillType.BLEED,skill.getPower());
                    break;
                }  
                case UNPOISON:
                {
                    L2Effect[] effects = target.getAllEffects();
                    for (L2Effect e : effects)
                    {
                        if (e.getSkill().getSkillType() == SkillType.POISON &&
                                skill.getPower() >= e.getSkill().getPower()) 
                        {
                            e.exit();
                            break;
                        }  
                    }
                    break;
                }

                case ERASE: // Doesn't affect siege golem, wild hog cannon or swoop cannon
                {
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss) 
                        && !(target instanceof L2SiegeSummonInstance))
                    {
                        L2PcInstance summonOwner = null;
                        L2Summon summonPet = null;
                        summonOwner = ((L2Summon)target).getOwner();
                        summonPet = summonOwner.getPet();
                        summonPet.unSummon(summonOwner);
                        SystemMessage sm = new SystemMessage(SystemMessageId.LETHAL_STRIKE);
                        summonOwner.sendPacket(sm); 
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)  
                        {
                            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                    }

                    break;
                }
                case MAGE_BANE:
                {
                    for(L2Object t: targets)
                    {
                        L2Character target1 = (L2Character) t;

                        if(target1.reflectSkill(skill))
                           target1 = activeChar;

                        if (!Formulas.getInstance().calcSkillSuccess(activeChar, target1, skill, ss, sps, bss))
                        {
                            if (activeChar instanceof L2PcInstance)
                            {
                                SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                                sm.addString(target1.getName());
                                sm.addSkillName(skill.getId());
                                activeChar.sendPacket(sm);
                            }
                            continue;
                        }
                    
                        L2Effect[] effects = target1.getAllEffects();
                        for(L2Effect e: effects)
                        {
                            for(Func f: e.getStatFuncs())
                            {
                                if(f.stat == Stats.MAGIC_ATTACK || f.stat == Stats.MAGIC_ATTACK_SPEED)
                                {
                                    e.exit();
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
                   
                case WARRIOR_BANE:
                {
                    for(L2Object t: targets)
                    {
                        L2Character target1 = (L2Character) t;

                        if(target1.reflectSkill(skill))
                            target1 = activeChar;

                        if (!Formulas.getInstance().calcSkillSuccess(activeChar, target1, skill, ss, sps, bss))
                        {
                            if (activeChar instanceof L2PcInstance)
                            {
                                SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                                sm.addString(target1.getName());
                                sm.addSkillName(skill.getId());
                                activeChar.sendPacket(sm);
                            }
                            continue;
                        }
                    
                        L2Effect[] effects = target1.getAllEffects();
                        for(L2Effect e: effects)
                        {
                            for(Func f: e.getStatFuncs())
                            {
                                if(f.stat == Stats.RUN_SPEED || f.stat == Stats.POWER_ATTACK_SPEED)
                                {
                                    e.exit();
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }

                case NEGATE:
                case CANCEL:
                {
                    if(target.reflectSkill(skill))
                       target = activeChar;

                    if(skill.getId() == 1056 && target != activeChar) //can't cancel your self
                    {
                        int lvlmodifier= 52+skill.getMagicLevel()*2;
                        if(skill.getMagicLevel()==12) lvlmodifier = (Experience.MAX_LEVEL - 1);
                        int landrate = 90;
                        if((target.getLevel() - lvlmodifier)>0) landrate = 90-4*(target.getLevel()-lvlmodifier);

                        landrate = (int) activeChar.calcStat(Stats.CANCEL_VULN, landrate, target, null);

                        if(Rnd.get(100) < landrate)
                        {
                            L2Effect[] effects = target.getAllEffects();
                            int maxfive = 5;
                            for (L2Effect e : effects)
                            { 
                                switch(e.getSkill().getId())
                                {
                                    case 4082:
                                    case 4215:
                                    case 4515:
                                    case 110:
                                    case 111:
                                    case 1323:
                                    case 1325:
                                        // Cannot cancel skills 4082, 4215, 4515, 110, 111, 1323, 1325
                                        break;
                                    default:
                                        if(e.getSkill().getSkillType() == SkillType.BUFF) //sleep, slow, surrenders etc
                                        {
                                            int rate = 100;
                                            int level = e.getLevel();
                                            if (level > 0) rate = Integer.valueOf(150/(1 + level));
                                            if (rate > 95) rate = 95;
                                            else if (rate < 5) rate = 5;
                                            if(Rnd.get(100) < rate) {
                                                e.exit();
                                                maxfive--;
                                            }
                                        }
                                        else
                                            e.exit();
                                        break;
                                }
                                if(maxfive == 0) break;
                            }
                        } 
                        else
                        {
                            if (activeChar instanceof L2PcInstance)
                            {
                                SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                                sm.addString(target.getName());
                                sm.addSkillName(skill.getDisplayId());
                                activeChar.sendPacket(sm);
                            }
                        }
                        break;
                    }
                    // fishing potion
                    else if (skill.getId() == 2275)
                    {
                        _negatePower = skill.getNegatePower();
                        _negateId = skill.getNegateId();
     
                        negateEffect(target,SkillType.BUFF,_negatePower,_negateId);
                        break;
                    }
                    // Touch of Death
                    else if (skill.getId() == 342 && target != activeChar)//can't cancel your self
                    {
                        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, sps, bss))
                        {
                            L2Effect[] effects = target.getAllEffects();
                            int maxfive = 5;
                            for (L2Effect e : effects)
                            {
                                if (e.getSkill().getSkillType() == SkillType.BUFF ||
                                        e.getSkill().getSkillType() == SkillType.CONT ||
                                        e.getSkill().getSkillType() == SkillType.DEATHLINK_PET)
                                {
                                    int skillrate = 100;
                                    int level = e.getLevel();
                                    if (level > 0) skillrate = Integer.valueOf(200/(1 + level));
                                    if (skillrate > 95) skillrate = 95;
                                    else if (skillrate < 5) skillrate = 5;
                                    if(Rnd.get(100) < skillrate)
                                    {
                                        e.exit();
                                        maxfive--;
                                    }
                                }
                                if(maxfive == 0) break;
                            }
                            skill.getEffects(activeChar, target);
                        }
                        else if(activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                        break;
                    }
    
                    // purify
                    //  all others negate type skills
                    else
                    {
                        _negateStats = skill.getNegateStats();
                        _negatePower = skill.getNegatePower();
                            
                        for (String stat : _negateStats)
                        {
                            stat = stat.toLowerCase().intern();
                            if (stat == "buff") 
                            {
                                int lvlmodifier= 52+skill.getMagicLevel()*2;
                                if(skill.getMagicLevel()==12) lvlmodifier = (Experience.MAX_LEVEL - 1);
                                int landrate = 90;
                                if((target.getLevel() - lvlmodifier)>0) landrate = 90-4*(target.getLevel()-lvlmodifier);

                                landrate = (int) activeChar.calcStat(Stats.CANCEL_VULN, landrate, target, null);

                                if(Rnd.get(100) < landrate)
                                    negateEffect(target,SkillType.BUFF,-1);
                            }
                            if (stat == "debuff") negateEffect(target,SkillType.DEBUFF,-1);
                            if (stat == "weakness") negateEffect(target,SkillType.WEAKNESS,-1);
                            if (stat == "stun") negateEffect(target,SkillType.STUN,-1);
                            if (stat == "sleep") negateEffect(target,SkillType.SLEEP,-1);
                            if (stat == "confusion") negateEffect(target,SkillType.CONFUSION,-1);
                            if (stat == "mute") negateEffect(target,SkillType.MUTE,-1);
                            if (stat == "fear") negateEffect(target,SkillType.FEAR,-1);
                            if (stat == "poison") negateEffect(target,SkillType.POISON,_negatePower);
                            if (stat == "bleed") negateEffect(target,SkillType.BLEED,_negatePower);
                            if (stat == "paralyze") negateEffect(target,SkillType.PARALYZE,-1);
                            if (stat == "heal")
                            {
                                ISkillHandler Healhandler = SkillHandler.getInstance().getSkillHandler(SkillType.HEAL);
                                if (Healhandler == null)
                                {
                                    _log.fatal("Couldn't find skill handler for HEAL.");
                                    continue;
                                }
                                L2Object tgts[] = new L2Object[]{target};
                                try {
                                    Healhandler.useSkill(activeChar, skill, tgts);
                                } catch (IOException e) {
                                    _log.warn( "", e);
                                }
                            }
                        }//end for
                    }//end else
                }// end case
            }//end switch
        }//end for
        // self Effect :]
        L2Effect effect = activeChar.getFirstEffect(skill.getId());
        if (effect != null && effect.isSelfEffect())
        {
           //Replace old effect with new one.
           effect.exit();
        }
        skill.getEffectsSelf(activeChar);
    } //end void
        
    private void negateEffect(L2Character target, SkillType type, double power)
    {
       negateEffect(target, type, power, 0);
    }

    private void negateEffect(L2Character target, SkillType type, double power, int skillId)
    {
        L2Effect[] effects = target.getAllEffects();
        for (L2Effect e : effects)
            if (power == -1) // if power is -1 the effect is always removed without power/lvl check ^^
            {
                if (e.getSkill().getSkillType() == type || (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type)) {
                    if (skillId != 0)
                    {
                        if (skillId == e.getSkill().getId())
                            e.exit();
                    }
                    else
                        e.exit();
                }
            }
            else if ((e.getSkill().getSkillType() == type && e.getSkill().getPower() <= power) 
                    || (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type && e.getSkill().getEffectLvl() <= power)) {
                if (skillId != 0)
                {
                    if (skillId == e.getSkill().getId())
                        e.exit();
                }
                else
                    e.exit();
            }
    }

    public SkillType[] getSkillIds() 
    { 
        return SKILL_IDS; 
    } 
}
