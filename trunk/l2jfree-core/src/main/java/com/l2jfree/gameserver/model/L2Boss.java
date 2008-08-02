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
package com.l2jfree.gameserver.model;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.instancemanager.BossSpawnManager;
import com.l2jfree.gameserver.model.L2Skill.SkillType;
import com.l2jfree.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.L2NpcTemplate;
import com.l2jfree.tools.random.Rnd;

public abstract class L2Boss extends L2MonsterInstance
{
    private static final int BOSS_MAINTENANCE_INTERVAL = 10000;

    public static final int BOSS_INTERACTION_DISTANCE = 500;

    protected final static Log _log = LogFactory.getLog(L2Boss.class.getName());

    public L2Boss(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    private BossSpawnManager.StatusEnum _raidStatus;

    @Override
    protected int getMaintenanceInterval() { return BOSS_MAINTENANCE_INTERVAL; }

    @Override
    public boolean doDie(L2Character killer)
    {
        if (!super.doDie(killer))
            return false;

        if (killer instanceof L2PlayableInstance)
        {
            SystemMessage msg = new SystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL);
            broadcastPacket(msg);
        }
        return true;
    }

    /**
     * Boss are not affected by some type of skills (confusion, mute, paralyze, root
     * and a list of skills define in the configuration)

     * @param skill the casted skill
     * @see L2Character#checkSkillCanAffectMyself(L2Skill)
     */
    @Override
    public boolean checkSkillCanAffectMyself(L2Skill skill)
    {
        return checkSkillCanAffectMyself(skill.getSkillType());
    }

    @Override
    public boolean checkSkillCanAffectMyself(SkillType type)
    {
        switch(type)
        {
            case CONFUSION: case MUTE: case PARALYZE: case ROOT: case SHIFT_TARGET:
            case DEBUFF: case AGGDEBUFF: case FEAR: case SLEEP: case STUN:
                return Rnd.get(1000) == 1;
        }
        return true;
    }

    @Override
    public boolean isRaid()
    {
        return true;
    }

    @Override
    public boolean hasRandomAnimation()
    {
        return false;
    }

    @Override
    protected boolean canInteract(L2PcInstance player)
    {
        // TODO: NPC busy check etc...
        if (!isInsideRadius(player, BOSS_INTERACTION_DISTANCE, false, false))
            return false;

        return true;
    }

    public void setRaidStatus (BossSpawnManager.StatusEnum status)
    {
        _raidStatus = status;
    }

    public BossSpawnManager.StatusEnum getRaidStatus()
    {
        return _raidStatus;
    }

    /**
     * Spawn all minions at a regular interval
     * if minions are not near the raid boss, teleport them 
     * 
     */
    @Override
    protected void manageMinions()
    {
        _minionList.spawnMinions();
        _minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
        {
            public void run()
            {
                // teleport raid boss home if it's too far from home location
                L2Spawn bossSpawn = getSpawn();
                if(!isInsideRadius(bossSpawn.getLocx(),bossSpawn.getLocy(),bossSpawn.getLocz(), 5000, true, false))
                {
                    teleToLocation(bossSpawn.getLocx(),bossSpawn.getLocy(),bossSpawn.getLocz(), true);
                    healFull(); // prevents minor exploiting with it
                }
                _minionList.maintainMinions();
            }
        }, 60000, getMaintenanceInterval()+Rnd.get(5000));
    }

    /**
     * Restore full Amount of HP and MP 
     * 
     */
    public void healFull()
    {
        super.getStatus().setCurrentHp(super.getMaxHp());
        super.getStatus().setCurrentMp(super.getMaxMp());
    }
}
