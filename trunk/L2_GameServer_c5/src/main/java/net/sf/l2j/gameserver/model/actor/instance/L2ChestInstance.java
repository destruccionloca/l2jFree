/*
 *@autor AlterEgo - tnx to Demonia
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
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class manages all chest. 
 */
public final class L2ChestInstance extends L2Attackable
{
    private volatile boolean _isOpen;
    
    public L2ChestInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
	{
		if (!isAlikeDead() && isBox())
        {
			doDie(attacker);
            return;
        }
        super.reduceCurrentHp(damage,attacker,awake);
	}
	
    public boolean isAutoAttackable(L2Character attacker)
    {
        return true;
    }

    public boolean isAttackable()
    {
        return true;
    }

    public void doDie(L2Character killer)
    {
        if(!isSpoil()) killer.setTarget(null);
		setCurrentHpMp(0,0);
		super.doDie(killer);
    }

    public boolean isAggressive()
    {
        return false;
    }

    public void OnSpawn()
    {
        super.OnSpawn();
        _isOpen = false;
        setHaveToDrop(true);
        setMustRewardExpSp(true);
        if (isBox())
        {
            setHaveToDrop(false);
            setMustRewardExpSp(false);
        }
    }

	public boolean isBox() {
        return (getTemplate().npcId>=18265 && getTemplate().npcId<=18286);
    }
    
	public synchronized boolean open() {
        boolean wasOpen = _isOpen;
        _isOpen = true;
        return wasOpen;
    }
	
	public void setSpecialDrop()
	{
        setHaveToDrop(true);
	}
	
    //cast - trap chest
    public void chestTrap(L2Character player)
    {
        int trapSkillId = 0;
        int rnd = Rnd.get(120);

        if (getTemplate().level >= 61)
        {
            if (rnd >= 90) trapSkillId = 4139;//explosion
            else if (rnd >= 50) trapSkillId = 4118;//area paralysys 
            else if (rnd >= 20) trapSkillId = 1167;//poison cloud
            else trapSkillId = 223;//sting
        }
        else if (getTemplate().level >= 41)
        {
            if (rnd >= 90) trapSkillId = 4139;//explosion
            else if (rnd >= 60) trapSkillId = 96;//bleed 
            else if (rnd >= 20) trapSkillId = 1167;//poison cloud
            else trapSkillId = 4118;//area paralysys
        }
        else if (getTemplate().level >= 21)
        {
            if (rnd >= 80) trapSkillId = 4139;//explosion
            else if (rnd >= 50) trapSkillId = 96;//bleed 
            else if (rnd >= 20) trapSkillId = 1167;//poison cloud
            else trapSkillId = 129;//poison
        }
        else
        {
            if (rnd >= 80) trapSkillId = 4139;//explosion
            else if (rnd >= 50) trapSkillId = 96;//bleed 
            else trapSkillId = 129;//poison
        }

        player.sendPacket(new SystemMessage(714)); // Closest matching systemmsg
        handleCast(player, trapSkillId);
    }
    //<--
    //cast casse
    //<--
    private boolean handleCast(L2Character player, int skillId)
    {
        int skillLevel = 1;

        byte lvl = getTemplate().level;
        if (lvl > 20 && lvl <= 40) skillLevel = 3;
        else if (lvl > 40 && lvl <= 60) skillLevel = 5;
        else if (lvl > 60) skillLevel = 6;
        
        if (player.isDead() 
            || !player.isVisible()
            || !player.isInsideRadius(this, getDistanceToWatchObject(player), false, false))
            return false;

        L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

        if (player.getEffect(skill) == null)
        {
            skill.getEffects(this, player);
            broadcastPacket(new MagicSkillUser(this, player, skill.getId(), skillLevel,
                                                skill.getSkillTime(), 0));
            return true;
        }
        return false;
    }
}