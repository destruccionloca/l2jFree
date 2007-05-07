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
/**
 *  Clanth Oct 2006
 * 
 * 
 * */

package net.sf.l2j.gameserver.model.actor.instance;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.Future;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class L2PetBabyInstance extends L2PetInstance 
{
    final static Log _log = LogFactory.getLog(L2PetBabyInstance.class.getName());
    
    private Future _healTask;
    float _expPenalty;
    boolean _thinking = false;
    
    protected L2PetBabyInstance _pet;    
    public L2PetBabyInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
    {
        super(objectId, template, owner,control);
        _pet = this;
    }
    class HealTask implements Runnable
    {
        public void run()
        {
            if (_thinking)
            {
                return;
            }
            _thinking = true;
            
            int maxHp = getOwner().getStat().getMaxHp();
            double curHP = getOwner().getStatus().getCurrentHp();
            int random;
            L2Skill skill=null;
            try
            {
                if (!getOwner().isDead() && getOwner() != null )
                {
                    random = Rnd.get(100);
                    if ( random <= 25 )
                    {
                        if ( curHP <= ((maxHp / 100) * 80) && curHP >= ((maxHp / 100) * 15))     
                        {
                            skill = SkillTable.getInstance().getInfo(4717,getSkillLevel());
                        }
                    }
                    else
                    {
                        if (curHP < ((maxHp / 100) * 15))
                        {
                            skill = SkillTable.getInstance().getInfo(4718,getSkillLevel());
                        }
                    }
                    if (skill != null)
                    { 
                        useMagic(skill,false,false);
                    }
                }
            }
            catch (Throwable e) 
            {
                if (_log.isDebugEnabled())
                {
                    _log.warn("Pet [#"+getObjectId()+"] a heal task error has occurred: "+e);
                }
            }
            _thinking = false;
        }
    }
    public synchronized void stopHealTask()
    {
        if (_healTask != null)
        {
            _healTask.cancel(false);
            _healTask = null;
            if (_log.isDebugEnabled())
                _log.warn("Pet [#"+getObjectId()+"] Heal task stop");
        }
    }
    
    public synchronized void startHealTask()
    {
        if (_healTask != null) { stopHealTask(); }
        
        if (_healTask == null && !isDead())
        {
            _healTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new HealTask(), 3000, 1000);
        }
    }

    public static L2PetBabyInstance spawnPet(L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
    {
         L2PetBabyInstance pet = restore(control, template, owner);
         // add the pet instance to world
         if (pet != null)
         {
             L2World.getInstance().addPet(owner.getObjectId(), pet);
         }
         return pet;
    }    
     public synchronized void doDie(L2Character killer)
     {
        stopHealTask();
        super.doDie(killer);
    }

    public void doRevive(double revivePower)
    {
         // Restore the pet's lost experience, 
         // depending on the % return of the skill used (based on its power).
         restoreExp(revivePower);
         doRevive();
    }
     
    public void doRevive()
    {
        // Restore the pet's lost experience, 
        // depending on the % return of the skill used (based on its power).

        getOwner().removeReviving();
        
        super.doRevive();

        // stopDecay
        DecayTaskManager.getInstance().cancelDecayTask(this);
        startHealTask();        
        startFeed(false);        
    }
    
    
    private static L2PetBabyInstance restore(L2ItemInstance control, L2NpcTemplate template, L2PcInstance owner)
    {
        java.sql.Connection con = null;
        try
        {
            L2PetBabyInstance pet = new L2PetBabyInstance(IdFactory.getInstance().getNextId(), template, owner, control);
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("SELECT item_obj_id, objId, name, level, maxHp, curHp, maxMp, curMp, acc, crit, evasion, mAtk, mDef, mSpd, pAtk, pDef, pSpd, str, con, dex, _int, men, wit, exp, sp, karma, pkkills, maxload, fed, max_fed FROM pets WHERE item_obj_id=?");
            statement.setInt(1, control.getObjectId());
            ResultSet rset = statement.executeQuery();
            if (!rset.next()) return pet;
            
            pet.setExpPenalty((float).1); // baby pets get 10% flat rate while fighting or not. Exp. pen. done at owner Exp. calc..
            pet._respawned = true;
            pet.setName(rset.getString("name"));

            pet.getStat().setLevel(rset.getByte("level"));
            pet.getStat().setExp(rset.getInt("exp"));
            pet.getStat().setSp(rset.getInt("sp"));

            pet.getStatus().setCurrentHp(rset.getDouble("curHp"));
            pet.getStatus().setCurrentMp(rset.getDouble("curMp"));
            pet.getStatus().setCurrentCp(pet.getMaxCp());

            pet.setKarma(rset.getInt("karma"));
            pet.setPkKills(rset.getInt("pkkills"));
            pet.setCurrentFed(rset.getInt("fed"));

            rset.close();
            statement.close();
            return pet;
        } catch (Exception e) {
            _log.warn("could not restore pet data: "+ e);
            return null;
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
    }
    public void setExpPenalty(float expPenalty)
    {
        _expPenalty = expPenalty;
    }
    public float getExpPenalty()
    {
        return _expPenalty;
    }
    public synchronized void unSummon (L2PcInstance owner)
    {
        stopHealTask();
        super.unSummon(owner);
    }

    public int getSkillLevel()
    {
        int lvl = getLevel();
        return lvl > 70 ? 7 + (lvl - 70) / 5 : lvl / 10;
    }
}