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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2WeaponType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import net.sf.l2j.gameserver.util.Util;
/** 
 * @author decad
 * 
 */ 

public class Crits implements ISkillHandler
{
    // all the items ids that this handler knowns
    private final static Log _log = LogFactory.getLog(Crits.class);
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    private static SkillType[] _skillIds = {
        SkillType.CRITS,
    };
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        if (activeChar.isAlikeDead())
            return;
        
        int damage = 0;
        int CritS=0;
        
        if (_log.isDebugEnabled())
            if (_log.isDebugEnabled()) _log.debug("Begin Skill processing in Crits.java " + skill.getSkillType());
        
        for(int index = 0;index < targets.length;index++)
        {
            L2Character target = (L2Character)targets[index];
            L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
            if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && target.isAlikeDead() && target.isFakeDeath())
            {
                target.stopFakeDeath(null);
            }
            else if (target.isAlikeDead())
                continue;
            
            boolean dual = activeChar.isUsingDualWeapon();
            boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
            boolean crit = false;
            if (skill.ignoreShld())
            {
                shld = false;
            }
            if (Config.ALT_DAGGER_FORMULA)
            {
                CritS = Rnd.get(100);
                CritS += Config.ALT_DAGGER_RATE; //extra chance
                {
                if (activeChar instanceof L2PcInstance && activeChar.isBehindTarget())
                    CritS += Config.ALT_DAGGER_RATE_BEHIND;
                for (int degrees = 45; degrees < 135; degrees++) 
                {
                    if (activeChar instanceof L2PcInstance && activeChar.isInFront(target,degrees))
                    CritS += Config.ALT_DAGGER_RATE_FRONT;
                }
                }
                if (CritS > 100)
                    CritS= 100;
                if (CritS >= Rnd.get(100) + Config.ALT_DAGGER_FAIL_RATE)
                    crit = true;
                else 
                    crit = false;
                }
            else if (!Config.ALT_DAGGER_FORMULA )
            {
                crit = Formulas.getInstance().calcCrit(activeChar, target, activeChar.getCriticalHit(target, skill));
            }
            boolean soul = (weapon!= null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER );
            if (target instanceof L2PcInstance && skill.getId() == 30 && crit) 
            {
                double Hpdamage = 0;
                damage = (int)Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);
                if (damage >= (target.getCurrentHp() + target.getCurrentCp()))
                {
                    if (target.isPetrified())
                    {
                        damage = 0;
                    }
                    else
                        target.reduceCurrentHp(damage, activeChar);
                }
                else 
                {
                    if (damage >= target.getCurrentHp())
                    {
                        if (!target.isPetrified())
                        {
                            target.setCurrentHp(0);
                            target.doDie(activeChar);
                        }
                    }
                    else if (damage <= target.getCurrentHp())
                    {
                        
                        Hpdamage = (target.getCurrentHp() - damage);
                        if (!target.isPetrified())
                        {
                            target.setCurrentHp(Hpdamage);
                        }
                        else
                        {
                            damage =0;
                        }
                    
                    SystemMessage smsg = new SystemMessage(SystemMessage.S1_GAVE_YOU_S2_DMG);
                    smsg.addNumber(damage);
                    smsg.addString(activeChar.getName());
                    target.sendPacket(smsg);
                    }
                    if (target.getCurrentHp() <= 0)
                    {
                        target.setCurrentHp(0);
                        target.doDie(activeChar);
                    }
                }
            } 
            else if (target instanceof L2NpcInstance && !target.isRaid() && crit && ((L2NpcInstance) target).getTemplate().npcId != 35062) 
            {
                damage = (int)Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);
                if (skill.isCritical())
                    damage = 0;
                if (target.isPetrified())
                    damage = 0;
                target.reduceCurrentHp(damage, activeChar);
            }
            else damage = (int)Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);
            if (skill.isCritical() && !crit)
                damage = 0;
            if ( crit == false && (skill.getCondition() & L2Skill.COND_CRIT) != 0) // if blow and crit=false then dmg=0
                damage = 0;
            else if (crit == false && weapon.getItemType() == L2WeaponType.DAGGER)
                damage = 0;  
           
            activeChar.stopEffect(skill.getId());
            if (activeChar.getEffect(skill.getId()) != null)
                activeChar.removeEffect(activeChar.getEffect(skill.getId()));
            if (activeChar instanceof L2PcInstance && (damage >= 0) && (skill.getId() == 321 || skill.getId() == 369)) // evade shot-blinding even if no critical set bonus
                {
                    skill.getEffects(activeChar, activeChar);
                    SystemMessage sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
                    sm.addSkillName(skill.getId());
                    activeChar.sendPacket(sm);
                }
            else if  (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, false, false) && (skill.getId() != 321) || skill.getId() != 369)
            { 
                skill.getEffects(activeChar, target);
                if (target instanceof L2PcInstance)
                {
                    if (activeChar instanceof L2PcInstance && weapon.getItemType() != L2WeaponType.DAGGER)
                    {
                    SystemMessage sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
                    sm.addSkillName(skill.getId());
                    target.sendPacket(sm);
                    }
                }
            }
            else if  (!Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, false, false) && (weapon.getItemType() != L2WeaponType.DAGGER) &&  (skill.getId() != 369))
            { 
                SystemMessage sm = new SystemMessage(139);
                sm.addString(target.getName());
                sm.addSkillName(skill.getId());
                activeChar.sendPacket(sm);
            }
            if (damage > 5000 && activeChar instanceof L2PcInstance)
            {
                    String name = "";
                if (target instanceof L2RaidBossInstance)
                    name = "RaidBoss ";
                if (target instanceof L2NpcInstance)
                    name += target.getName()+"("+((L2NpcInstance)target).getTemplate().npcId+")";
                if (target instanceof L2PcInstance)
                    name = target.getName()+"("+target.getObjectId()+") ";
                name += target.getLevel()+" lvl";
                if(_log.isDebugEnabled())
                    _log.info(activeChar.getName()+"("+activeChar.getObjectId()+") "+activeChar.getLevel()+" lvl did damage "+damage+" with skill "+skill.getName()+"("+skill.getId()+") to "+name);
            }

            if (skill.isInstantKill() && !target.isRaid())
            {
                if(Rnd.get(100) <= skill.getInstantKillRate() && crit)
                {
                    boolean RateEffect2 = Rnd.get(100) <= Config.ALT_INSTANT_KILL_EFFECT_2; //rate for effect lvl 2 HP=1 NOcp change 
                    if (target instanceof L2PcInstance && skill.getId() == 30 && !RateEffect2)
                    {
                        if (!target.isPetrified())
                        {
                            target.setCurrentCp(1);
                            damage=0;
                        }
                    }
                    else if (target instanceof L2PcInstance && skill.getId() == 30 && RateEffect2) 
                    {
                        if (!target.isPetrified())
                        {
                            target.setCurrentHp(1);
                            damage=0;
                        }
                    }
                    else if (target instanceof L2PcInstance && skill.getId() != 30)
                    {
                        if (!target.isPetrified())
                            target.setCurrentCp(1);
                    }
                    else if (target instanceof L2NpcInstance)
                    {
                        if (!target.isPetrified() && !target.isRaid() && ((L2NpcInstance) target).getTemplate().npcId != 35062) 
                        {
                            target.setCurrentHp(0);
                            target.doDie(activeChar);
                        }
                    }
                    //Your lethal strike was successful!
                    activeChar.sendPacket(new SystemMessage(1668));
                } 
                else 
                {
                    activeChar.sendPacket(SystemMessage.sendString("Your lethal strike failed!"));
                }
            
            }             
            if (skill.getId() != 30)
            {
                if (target.isPetrified())
                    damage= 0;
                target.reduceCurrentHp(damage, activeChar);
            }
            if (soul && weapon!= null)
                    weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE); 
            if (damage > 0)
            {
                if (activeChar instanceof L2PcInstance) 
                {
                    if (crit) activeChar.sendPacket(new SystemMessage(SystemMessage.CRITICAL_HIT)); 
                    SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG); 
                    sm.addNumber(damage); 
                    activeChar.sendPacket(sm);
                }
             } 
            else if (crit && activeChar instanceof L2PcInstance && weapon.getItemType() == L2WeaponType.DAGGER) 
                activeChar.sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET)); //msg when the blow misses the target
        }   
    }
    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }
}
