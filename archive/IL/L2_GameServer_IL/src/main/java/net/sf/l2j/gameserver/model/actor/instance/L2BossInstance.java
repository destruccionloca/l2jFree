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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.Future;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class manages all Bosses. 
 * 
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public final class L2BossInstance extends L2MonsterInstance
{
	private boolean _teleportedToNest;
    
    private static final int BOSS_MAINTENANCE_INTERVAL = 10000;

    // [L2J_JP ADD START SANDMAN]
    final static Log _log = LogFactory.getLog(L2BossInstance.class.getName());

    protected int doTeleport = 0;
    protected L2Object _target;
    protected L2Character _Atacker;
    protected static final int NurseAntRespawnDelay = Config.NURSEANT_RESPAWN_DELAY;

    protected Future minionMaintainTask = null;

    protected L2PcInstance _TargetForKill = null;
    public void setTargetForKill(L2PcInstance Target)
    {
    	_TargetForKill = Target;
    }
    
    protected boolean _isInSocialAction = false;
    
    public boolean IsInSocialAction()
    {
        return _isInSocialAction;
    }
    
    public void setIsInSocialAction(boolean value)
    {
        _isInSocialAction = value;
    }
    
    // [L2J_JP ADD END SANDMAN]

    /**
     * Constructor for L2BossInstance. This represent all grandbosses:
     * <ul>
     * <li>29001    Queen Ant</li>
     * <li>29014    Orfen</li>
     * <li>29019    Antharas</li>
     * <li>29067    Antharas</li>
     * <li>29068    Antharas</li>
     * <li>29020    Baium</li>
     * <li>29022    Zaken</li>
     * <li>29028    Valakas</li>
     * <li>29006    Core</li>
     * <li>29045    Frintezza</li>
     * <li>29046    Scarlet Van Halisha 1st Morph</li>
     * <li>29047    Scarlet Van Halisha 3rd Morph</li>
     * </ul>
     * <br>
     * <b>For now it's nothing more than a L2Monster but there'll be a scripting<br>
     * engine for AI soon and we could add special behaviour for those boss</b><br>
     * <br>
     * @param objectId ID of the instance
     * @param template L2NpcTemplate of the instance
     */
    public L2BossInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    @Override
    protected int getMaintenanceInterval() { return BOSS_MAINTENANCE_INTERVAL; }

    @Override
    public boolean doDie(L2Character killer)
    {
        if (!super.doDie(killer))
            return false;

        // [L2J_JP ADD START SANDMAN]
        if (killer instanceof L2PlayableInstance)
        {
            SystemMessage msg = new SystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL);
            broadcastPacket(msg);
        }
        // [L2J_JP ADD END SANDMAN]

        return true;
    }
    /**
     * Used by Orfen to set 'teleported' flag, when hp goes to <50%
     * @param flag
     */
    private void setTeleported(boolean flag)
    {
        _teleportedToNest = flag;
    }
    
    private boolean getTeleported()
    {
        return _teleportedToNest;
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
		if (!checkSkillCanAffectMyself(skill.getSkillType()) || Config.FORBIDDEN_RAID_SKILLS_LIST.contains(skill.getId()))
			return false;
		
		return true;
	}
	
	@Override
	public boolean checkSkillCanAffectMyself(SkillType type)
	{
		if (type == SkillType.CONFUSION	||	type == SkillType.MUTE		||	type == SkillType.PARALYZE	||
			type == SkillType.ROOT		||	type == SkillType.FEAR		||	type == SkillType.SLEEP		||
			type == SkillType.STUN		||	type == SkillType.DEBUFF	||	type == SkillType.AGGDEBUFF
		)
			return false;
		
		return true;
	}    

    @Override
    public void onSpawn()
    {
        // [L2J_JP ADD START SANDMAN]
        // get players in lair and update known list.
    	//getKnownList().getKnownPlayers().clear();
    	switch (getNpcId())
		{
			case 29022:	// Zaken (Note:teleport-out of instant-move execute onSpawn.)
				if(GameTimeController.getInstance().isNowNight())
					setIsInvul(true);
				else
					setIsInvul(false);
				break;				
			default:
				break;
		}
        super.onSpawn();
    }

    /**
     * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.<BR><BR> 
     * 
     */
    @Override
    public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
    {

        // [L2J_JP ADD SANDMAN]
    	if (IsInSocialAction() || isInvul()) return;

        switch (getTemplate().getNpcId())
        {
            case 29014: // Orfen
                if ((getStatus().getCurrentHp() - damage) < getMaxHp() / 2 && !getTeleported())
                {
                    clearAggroList();
                    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                    teleToLocation(43577,15985,-4396, false);
                    setTeleported(true);
                }
                break;
            // [L2J_JP ADD SANDMAN]
            case 29001: // Queen ant
                List<L2MinionInstance> _minions = _minionList.getSpawnedMinions();

                if (_minions.isEmpty())
                {
                    if (_minionMaintainTask == null)
                    {
                        try
                        {
                            _minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneral(
                            			new RespawnNurseAnts(),NurseAntRespawnDelay);
                        }
                        catch (NullPointerException e)
                        {
                        }
                    }
                }
                else
                {
                    L2Skill _heal1 = SkillTable.getInstance().getInfo(4020, 1);
                    L2Skill _heal2 = SkillTable.getInstance().getInfo(4024, 1);

                    for (L2MinionInstance m : _minions)
                    {
                        callMinions();
                        m.setTarget(this);
                        m.doCast(_heal1);
                        m.setTarget(this);
                        m.doCast(_heal2);
                    }
                }
                break;
            default:
                break;
        }

        super.reduceCurrentHp(damage, attacker, awake);
    }
    
    @Override
    public boolean isRaid()
    {
        return true;
    }

    // [L2J_JP ADD START SANDMAN]
    // respawn nurse ants.
    private class RespawnNurseAnts implements Runnable
    {

        public RespawnNurseAnts()
        {
        }

        public void run()
        {
            try
            {
                _minionList.maintainMinions();
            }
            catch (Throwable e)
            {
                _log.fatal("", e);
            }
            finally
            {
            	_minionMaintainTask = null;
            }
        }
    }
    // [L2J_JP ADD END SANDMAN]
}
