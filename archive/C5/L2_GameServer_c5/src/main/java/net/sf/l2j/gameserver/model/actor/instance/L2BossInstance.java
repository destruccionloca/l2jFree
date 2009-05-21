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
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.BossActionTaskManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.Earthquake;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
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
    protected static final int _ActivityTimeOfBoss = Config.ACTIVITY_TIME_OF_BOSS;
    protected static final int NurseAntRespawnDelay = Config.NURSEANT_RESPAWN_DELAY;

    protected Future _SocialTask = null;
    protected Future _SocialTask2 = null;
    protected Future _SocialTask3 = null;
    protected Future _RecallPcTask = null;
    protected Future _KillingPcTask = null;
    protected Future _CallAngelTask = null;
    protected Future _MobiliseTask = null;
    protected Future _DeleteTask = null;
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
     * <li>12001    Queen Ant</li>
     * <li>12169    Orfen</li>
     * <li>12211    Antharas</li>
     * <li>12372    Baium</li>
     * <li>12374    Zaken</li>
     * <li>12899    Valakas</li>
     * <li>12052    Core</li>
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

    protected int getMaintenanceInterval() { return BOSS_MAINTENANCE_INTERVAL; }

    public void doDie(L2Character killer)
    {
        // [L2J_JP ADD START SANDMAN]
        if (killer instanceof L2PlayableInstance)
        {
            SystemMessage msg = new SystemMessage(1209);
            broadcastPacket(msg);
        }

        switch (getTemplate().npcId)
        {
            case 29020: //Baium
                BossActionTaskManager.getInstance().RemoveArcAngel();
                break;

            default:
            	BossActionTaskManager.getInstance().setCubeSpawn(getNpcId());
                break;
        }
        
        // [L2J_JP ADD END SANDMAN]

        super.doDie(killer);
    }
    /**
     * Used by Orfen to set 'teleported' flag, when hp goes to <50%
     * @param flag
     */
    public void setTeleported(boolean flag)
    {
        _teleportedToNest = flag;
    }
    
    public boolean getTeleported()
    {
        return _teleportedToNest;
    }

    public void OnSpawn()
    {
        // [L2J_JP ADD START SANDMAN]
    	getKnownList().getKnownPlayers().clear();
		for (L2Object object : BossActionTaskManager.getInstance().getPlayersInLair(getNpcId()))
		{
			getKnownList().getKnownPlayers().put(object.getObjectId(), (L2PcInstance)object);
		}
        switch (getNpcId())
        {
            case 29020: //Baium
            {
                setIsImobilised(true);
                setIsInSocialAction(true);
                Earthquake eq = new Earthquake(getX(), getY(), getZ(), 30, 10);
                broadcastPacket(eq);
                SocialAction sa = new SocialAction(getObjectId(), 2);
                broadcastPacket(sa);
                _SocialTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(new Social(3), 15000);
                _RecallPcTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(new RecallPc(), 20000);
                _SocialTask2 = 
                	ThreadPoolManager.getInstance().scheduleEffect(new Social(1), 25000);
                _SocialTask2 = 
                	ThreadPoolManager.getInstance().scheduleEffect(new KillingPc(), 26000);
                _CallAngelTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(new CallArcAngel(),35000);
                _MobiliseTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(new SetMobilised(),40000);
                _DeleteTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(
                			new DeleteGrandBoss(),_ActivityTimeOfBoss); // Delete Spawn
                break;
            }
            case 29019: //Antharas
            {
                setIsInSocialAction(true);
                SocialAction sa = new SocialAction(getObjectId(), 3);
                broadcastPacket(sa);
                _SocialTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(new Social(2), 15000);
                _MobiliseTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(new SetMobilised(),30000);
                _DeleteTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(
                			new DeleteGrandBoss(),_ActivityTimeOfBoss); // Delete Spawn 
                break;
            }
            case 29028: //Valakas
            {
                setIsInSocialAction(true);
                SocialAction sa = new SocialAction(getObjectId(), 3);
                broadcastPacket(sa);
                _SocialTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(new Social(2), 26000);
                _MobiliseTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(new SetMobilised(),41000);
                _DeleteTask = 
                	ThreadPoolManager.getInstance().scheduleEffect(
                			new DeleteGrandBoss(),_ActivityTimeOfBoss); // Delete Spawn 
                break;
            }
        }

        super.OnSpawn();

    }

    /**
     * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.<BR><BR> 
     * 
     */
    public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
    {

        // [L2J_JP ADD SANDMAN]
        if (this.IsInSocialAction()) return;

        switch (getTemplate().npcId)
        {
            case 29014: // Orfen
                if ((getCurrentHp() - damage) < getMaxHp() / 2 && !getTeleported())
                {
                    clearAggroList();
                    getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                    teleToLocation(43577,15985,-4396, false);
                    setTeleported(true);
                }
                break;
            // [L2J_JP ADD SANDMAN]
            case 29001: // Queen ant
                List<L2MinionInstance> _minions = this.minionList.getSpawnedMinions();

                if (_minions.isEmpty())
                {
                    if (minionMaintainTask == null)
                    {
                        try
                        {
                            minionMaintainTask = 
                            	ThreadPoolManager.getInstance().scheduleGeneral(
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
                        this.callMinions();
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
    
    public boolean isRaid()
    {
        return true;
    }

    // [L2J_JP ADD START SANDMAN]
    protected void RecallTarget()
    {
    	_TargetForKill.teleToLocation(115831, 17248, 10078);
    }
    
    protected void KillTarget()
    {        	
    	_TargetForKill.reduceCurrentHp(100000 + Rnd.get(_TargetForKill.getMaxHp()/2,_TargetForKill.getMaxHp()),this);
    }

    private class RespawnNurseAnts implements Runnable
    {

        public RespawnNurseAnts()
        {
        }

        public void run()
        {
            try
            {
                minionList.maintainMinions();
            }
            catch (Throwable e)
            {
                _log.fatal("", e);
            }
            finally
            {
            	minionMaintainTask = null;
            }
        }
    }

    private class DeleteGrandBoss implements Runnable
    {

        public DeleteGrandBoss()
        {
        }

        public void run()
        {
            if (hasMinions())
            {
                List<L2MinionInstance> _minions = getSpawnedMinions();
                for (L2MinionInstance m : _minions)
                {
                    m.deleteMe();
                }
            }

            switch (getNpcId())
            {
                case 29020: //Baium
                	BossActionTaskManager.getInstance().RemoveArcAngel();
                    break;
            }

            getSpawn().stopRespawn();
            deleteMe();
            
            BossActionTaskManager.getInstance().banishesPlayers(getNpcId());
            
            BossActionTaskManager.getInstance().setUnspawn(getNpcId());

            if(_DeleteTask != null)
            {
                _DeleteTask.cancel(true);
                _DeleteTask = null;
            }
        }
    }

    private class Social implements Runnable
    {
        private int _action;

        public Social(int actionId)
        {
            _action = actionId;
        }

        public void run()
        {
            SocialAction sa = new SocialAction(getObjectId(), _action);
            broadcastPacket(sa);
        }
    }

    private class SetMobilised implements Runnable
    {
        public SetMobilised()
        {
        }

        public void run()
        {
            setIsImobilised(false);
            setIsInSocialAction(false);
            
            if (_SocialTask != null)
            {
            	_SocialTask.cancel(true);
                _SocialTask = null;
            }
            if (_SocialTask2 != null)
            {
            	_SocialTask2.cancel(true);
                _SocialTask2 = null;
            }
            if (_SocialTask3 != null)
            {
            	_SocialTask3.cancel(true);
                _SocialTask3 = null;
            }
        }
    }
    
    private class CallArcAngel implements Runnable
    {
    	public CallArcAngel()
    	{
    	}

    	public void run()
    	{
    		BossActionTaskManager.getInstance().CallArcAngel();
    		if(_CallAngelTask != null)
    		{
        		_CallAngelTask.cancel(true);
        		_CallAngelTask = null;
    		}
    	}
    }
    
    private class RecallPc implements Runnable
    {
    	public RecallPc()
    	{
    	}
    	public void run()
    	{
    		RecallTarget();
    	}
    }
    
    private class KillingPc  implements Runnable
    {
    	public KillingPc()
    	{
    	}
    	public void run()
    	{
    		KillTarget();
    	}
    }
    // [L2J_JP ADD END SANDMAN]
}
