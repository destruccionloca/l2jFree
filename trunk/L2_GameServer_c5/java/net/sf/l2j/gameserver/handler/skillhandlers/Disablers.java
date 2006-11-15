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
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
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
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

/** 
 * This Handles Disabler skills
 * @author _drunk_ 
 */
public class Disablers implements ISkillHandler
{
    protected SkillType[] _skillIds = {L2Skill.SkillType.STUN, L2Skill.SkillType.ROOT,
                                       L2Skill.SkillType.SLEEP, L2Skill.SkillType.CONFUSION,
                                       L2Skill.SkillType.AGGDAMAGE, L2Skill.SkillType.AGGREDUCE,
                                       L2Skill.SkillType.AGGREDUCE_CHAR, L2Skill.SkillType.AGGREMOVE,
                                       L2Skill.SkillType.UNBLEED, L2Skill.SkillType.UNPOISON,
                                       L2Skill.SkillType.MUTE, L2Skill.SkillType.FAKE_DEATH,
                                       L2Skill.SkillType.CONFUSE_MOB_ONLY,L2Skill.SkillType.NEGATE,
                                       L2Skill.SkillType.CANCEL, L2Skill.SkillType.PARALYZE,
                                       L2Skill.SkillType.UNSUMMON_ENEMY_PET,L2Skill.SkillType.BETRAY,
                                       L2Skill.SkillType.CANCEL_TARGET};
    protected static Logger _log = Logger.getLogger(L2Skill.class.getName());
    private  String[] _negateStats=null;
    private  float _negatePower=0.f;
    
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        SkillType type = skill.getSkillType();

        boolean ss = false;
        boolean sps = false;
        boolean bss = false;

        L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

        if (activeChar instanceof L2PcInstance)
        {
            if (weaponInst == null)
            {
                SystemMessage sm2 = new SystemMessage(614);
                sm2.addString("You must equip a weapon before casting a spell.");
                activeChar.sendPacket(sm2);
                return;
            }
        }

        if (weaponInst != null)
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
            else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
            {
                ss = true;
                activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
            }
        }
        for (int index = 0; index < targets.length; index++) 
        {
            // Get a target L2Character targets
            if (!(targets[index] instanceof L2Character)) continue;
   
            L2Character target = (L2Character) targets[index];
           
            if (target == null || target.isDead()) //bypass if target is null or dead
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
            SystemMessage sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
            sm.addSkillName(skill.getId());
            target.sendPacket(sm);
            }
            else
            {
               SystemMessage sm = new SystemMessage(139);
            sm.addString(target.getName());
            sm.addSkillName(skill.getId());
            activeChar.sendPacket(sm);
            }
            break;
       }
     case BETRAY:
      {
         if ( target != null && activeChar instanceof L2PcInstance && target instanceof L2Summon && Rnd.get(100) < 50)
         skill.getEffects(activeChar, target);
         else
         {
            SystemMessage sm = new SystemMessage(139);
         sm.addString(target.getName());
         sm.addSkillName(skill.getId());
         activeChar.sendPacket(sm);
         break;
         }
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
      case STUN:
        {
          if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, false,
                                                                false))
            {   
                skill.getEffects(activeChar, target);
            }
          else
          {
              if (activeChar instanceof L2PcInstance)
              {
                  SystemMessage sm = new SystemMessage(139);
                  sm.addString(target.getName());
                  sm.addSkillName(skill.getId());
                  activeChar.sendPacket(sm);
              }
          }
            break;
        }
       case SLEEP:
       case ROOT:
       case PARALYZE: //use same as root for now
       {   
           if (target instanceof L2NpcInstance){
           target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,50);
           }
           if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, sps, bss))
           {
               skill.getEffects(activeChar, target);
           }
           else
           {
               if (activeChar instanceof L2PcInstance)
               {
                   SystemMessage sm = new SystemMessage(139);
                   sm.addString(target.getName());
                   sm.addSkillName(skill.getId());
                   activeChar.sendPacket(sm);
               }
           }
           break;
       }
       case CONFUSION:
       case MUTE:
       {    if (target instanceof L2NpcInstance){
           target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,50);}
           if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, ss, bss))
           {   
               // stop same type effect if avaiable
               L2Effect[] effects = target.getAllEffects();
               for (L2Effect e : effects)
               {
                   if (e.getSkill().getSkillType() == type) e.exit();
               }
               // then restart
               // Make above skills mdef dependant                    
               if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, sps, bss))
                   //if(Formulas.getInstance().calcMagicAffected(activeChar, target, skill))
                   {
                   skill.getEffects(activeChar, target);
                   }
               else
               {
                   if (activeChar instanceof L2PcInstance)
                   {
                       SystemMessage sm = new SystemMessage(139);
                       sm.addString(target.getName());
                       sm.addSkillName(skill.getId());
                       activeChar.sendPacket(sm);
                   }
               }
           }
           else
           {
               if (activeChar instanceof L2PcInstance)
               {
                   SystemMessage sm = new SystemMessage(139);
                   sm.addString(target.getName());
                   sm.addSkillName(skill.getId());
                   activeChar.sendPacket(sm);
               }
           }
           break;
       }
    case CONFUSE_MOB_ONLY:
    {
        // do nothing if not on mob
        if (target instanceof L2Attackable) skill.getEffects(activeChar, target);
        else activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
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
                        activeChar.stopEffect(skill.getId());
                        target.setTarget(activeChar); //c5 hate PvP
                        if (activeChar.getEffect(skill.getId()) != null)
                            activeChar.removeEffect(activeChar.getEffect(skill.getId()));
                        skill.getEffects(activeChar, activeChar);
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,(int) skill.getPower());
                        }
                }     
           }
        }
        
        break;
    }
    case AGGREDUCE:
    {
        if (target instanceof L2Attackable)
            target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, null,
                                       -(int) skill.getPower());
        break;
    }
    case AGGREDUCE_CHAR:
    {
        skill.getEffects(activeChar, target);
        if (target instanceof L2Attackable)
        {
            target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,
                                       -(int) skill.getPower());
            if(target instanceof L2PcInstance)
            {
                SystemMessage sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
            sm.addSkillName(skill.getId());
            target.sendPacket(sm);
            }
        }
        break;
    }
    case AGGREMOVE:
    {
        // 1034 = repose, 1049 = requiem
        //if (skill.getId() == 1034 || skill.getId() == 1049)
        if ((skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD && target.isUndead())
                || target.isAttackable())
        {
            target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, null,
                                       -(int) skill.getPower());
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
    
    case NEGATE:
    case CANCEL:
  	{
      if (skill.getId() == 1056 && target != activeChar) //can't cancel your self
      {
      /*
         L2Effect[] effects = target.getAllEffects();
            for (L2Effect e : effects)
  	         {
                if (e.getSkill().getSkillType() == SkillType.BUFF
                        || e.getSkill().getSkillType() == SkillType.REFLECT
                        || e.getSkill().getSkillType() == SkillType.HEAL_PERCENT
                        || e.getSkill().getSkillType() != SkillType.STUN // CANCEL can remove only buffs no stun
                        || e.getSkill().getSkillType() != SkillType.DEBUFF // CANCEL can remove only buffs no debuff
                        || e.getSkill().getSkillType() != SkillType.SLEEP // CANCEL can remove only buffs no sleep
                        && e.getSkill().getId() != 4082 //Cannot cancel skills 4082, 4215, 4515 , 110, 111
                        && e.getSkill().getId() != 4215
                        && e.getSkill().getId() != 4515
                        && e.getSkill().getId() != 110
                        && e.getSkill().getId() != 111
                        && Rnd.get(100) <= skill.getLandingPercent()) //landing percent on DP
                    e.exit();
             }
					break;
        }*/
          int lvlmodifier= 52+skill.getMagicLevel()*2;
          if(skill.getMagicLevel()==12) lvlmodifier = 78;
          int landrate = skill.getLandingPercent();
          if((target.getLevel() - lvlmodifier)>0) landrate = 90-4*(target.getLevel()-lvlmodifier);
          if(Rnd.get(100) < landrate)
          {
              L2Effect[] effects = target.getAllEffects();
              int maxfive = 5;
              for (L2Effect e : effects)
              { 
                  if (e.getSkill().getId() != 4082 //Cannot cancel skills 4082, 4215, 4515 , 110, 111
                          && e.getSkill().getId() != 4215
                          && e.getSkill().getId() != 4515
                          && e.getSkill().getId() != 110
                          && e.getSkill().getId() != 111)
                  {
                      if(e.getSkill().getSkillType() != SkillType.BUFF) e.exit(); //sleep, slow, surrenders etc
                      else
                      {
                          int rate = 100;
                          int level = e.getLevel();
                          if (level > 0) rate = Integer.valueOf(150/(1 + level));
                          if (rate > 95) rate = 95;
                          else if (rate < 5) rate = 5;
                          if(Rnd.get(100) < rate) {
                              e.exit();
                              maxfive--;
                              if(maxfive == 0) break;
                          }
                      }
                  }
              }
          } else
          {
              SystemMessage sm = new SystemMessage(614);
              sm.addString(skill.getName() + " failed."); 
              if (activeChar instanceof L2PcInstance)
                  activeChar.sendPacket(sm);
          }
      }
     //finish cancel
      
  	if (skill.getId() == 1344 || skill.getId() == 1350) //warrior bane
  	{
  	    L2Effect[] effects = target.getAllEffects();
  	    for (L2Effect e : effects)
  	        {
  	        if (e.getSkill().getSkillType() == SkillType.BUFF //remove attck.speed and speed buffs
  	                && ((e.getSkill().getId() == 268  
  	                        || e.getSkill().getId() == 1204 || e.getSkill().getId() == 298 || e.getSkill().getId() == 1282
  	                        || e.getSkill().getId() == 230 || e.getSkill().getId() == 1086 || e.getSkill().getId() == 1062
  	                        || e.getSkill().getId() == 1356 || e.getSkill().getId() == 275|| e.getSkill().getId() == 1261
  	                        || e.getSkill().getId() == 1251 || e.getSkill().getId() == 1361)))
  	            e.exit(); 
  	        }
  	break;
  	}
  	else if (skill.getId() == 1345 || skill.getId() == 1351) //mage bane m.attk _ c.speed
  	{
       L2Effect[] effects = target.getAllEffects();
  	    for (L2Effect e : effects)
  	    {
  	        if (e.getSkill().getSkillType() == SkillType.BUFF 
                &&((e.getSkill().getId() == 273 || e.getSkill().getId() == 1059 
                        || e.getSkill().getId() == 1365  || e.getSkill().getId() == 1062 || e.getSkill().getId() == 1261 
                        || e.getSkill().getId() == 1361 || e.getSkill().getId() == 1355 || e.getSkill().getId() == 276 
                        || e.getSkill().getId() == 1085  || e.getSkill().getId() == 1004  || e.getSkill().getId() == 1002)))
  	            e.exit(); 
  	    }
  	    break;
        }
  	// Touch of Death
  	else if (skill.getId() == 342)
  	{
      L2Effect[] effects = target.getAllEffects();
  	    for (L2Effect e : effects)
  	    {
  	        if (e.getSkill().getSkillType() == SkillType.BUFF ||
  	            e.getSkill().getSkillType() == SkillType.CONT ||
  	            e.getSkill().getSkillType() == SkillType.DEATHLINK_PET)
              for (int buff = 0; buff < 5; buff++) //max buffs 5
              {
              e.exit();
              }
  	    }
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
                                if (stat == "buff") negateEffect(target,SkillType.BUFF,-1);
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
                                        _log.severe("Couldn't find skill handler for HEAL.");
                                        continue;
                                    }
                                    L2Object tgts[] = new L2Object[]{target};
                                    try {
                                        Healhandler.useSkill(activeChar, skill, tgts);
                                    } catch (IOException e) {
                                    _log.log(Level.WARNING, "", e);
                                    }
                                  }
                              }//end for                                                              
                        }//end else
                    }// end case                                    
                }//end switch
            }//end for        
        } //end void
        
        private void negateEffect(L2Character target, SkillType type, double power) {
            L2Effect[] effects = target.getAllEffects();
            for (L2Effect e : effects)
               if (power == -1) // if power is -1 the effect is always removed without power/lvl check ^^
               {
                   if (e.getSkill().getSkillType() == type || (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type))
                       e.exit();
               }
               else if ((e.getSkill().getSkillType() == type && e.getSkill().getPower() <= power) 
                       || (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type && e.getSkill().getEffectLvl() <= power))
                    e.exit();
         }
    public SkillType[] getSkillIds() 
    { 
        return _skillIds; 
    } 
}
