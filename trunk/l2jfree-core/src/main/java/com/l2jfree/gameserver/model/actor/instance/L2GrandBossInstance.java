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
package com.l2jfree.gameserver.model.actor.instance;

import java.util.List;
import java.util.concurrent.Future;

import com.l2jfree.Config;
import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.GrandBossSpawnManager;
import com.l2jfree.gameserver.instancemanager.grandbosses.BaiumManager;
import com.l2jfree.gameserver.model.L2Boss;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.templates.L2NpcTemplate;


/**
 * This class manages all Bosses. 
 * 
 * @version $Revision: 1.0.0.0 $ $Date: 2006/06/16 $
 */
public class L2GrandBossInstance extends L2Boss
{
	// [L2J_JP ADD SANDMAN]
	private boolean		_teleportedToNest;

	private long		_lastNurseAntHealTime	= 0;
	private L2Skill		_nurseAntHeal			= null;

	protected Future<?>	minionMaintainTask		= null;

	protected boolean	_isInSocialAction		= false;

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
	 * Constructor for L2GrandBossInstance. This represent all grandbosses:
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
	 * <b>For now it's (mostly) nothing more than a L2Monster but there'll be a scripting<br>
	 * engine for AI soon and we could add special behaviour for those boss</b><br>
	 * <br>
	 * @param objectId ID of the instance
	 * @param template L2NpcTemplate of the instance
	 */
	public L2GrandBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		GrandBossSpawnManager.getInstance().updateStatus(this, true);
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

	@Override
	public void onSpawn()
	{
		switch (getNpcId())
		{
		case 29022: // Zaken (Note:teleport-out of instant-move execute onSpawn.)
			if (GameTimeController.getInstance().isNowNight())
				setIsInvul(true);
			else
				setIsInvul(false);
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
		if (IsInSocialAction() || isInvul())
			return;

		switch (getTemplate().getNpcId())
		{
		case 29014: // Orfen
		{
			if ((getStatus().getCurrentHp() - damage) < getMaxHp() / 2 && !getTeleported())
			{
				clearAggroList();
				setCanReturnToSpawnPoint(false);
				setTeleported(true);
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				teleToLocation(43577, 15985, -4396, false);
			}
			break;
		}
		case 29001: // Queen ant
		{
			List<L2MinionInstance> _minions = _minionList.getSpawnedMinions();

			if (_minions.isEmpty())
			{
				if (_minionMaintainTask == null)
				{
					_minionMaintainTask = ThreadPoolManager.getInstance().scheduleGeneral(new RespawnNurseAnts(), Config.NURSEANT_RESPAWN_DELAY);
				}
			}
			else if ((_lastNurseAntHealTime + 5000) < System.currentTimeMillis())
			{
				_lastNurseAntHealTime = System.currentTimeMillis();
				if (_nurseAntHeal == null)
					_nurseAntHeal = SkillTable.getInstance().getInfo(4020, 1);

				callMinions();
				for (L2MinionInstance m : _minions)
				{
					m.setTarget(this);
					m.doCast(_nurseAntHeal);
				}
			}
			break;
		}
		}

		super.reduceCurrentHp(damage, attacker, awake);
	}

	// [L2J_JP ADD START SANDMAN]
	// respawn nurse ants.
	private class RespawnNurseAnts implements Runnable
	{
		public void run()
		{
			try
			{
				_minionList.maintainMinions();
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
			finally
			{
				_minionMaintainTask = null;
			}
		}
	}

	@Override
	public void doAttack(L2Character target)
	{
		if (_isInSocialAction)
			return;
		super.doAttack(target);
	}

	@Override
	public void doCast(L2Skill skill)
	{
		if (_isInSocialAction)
			return;
		super.doCast(skill);
	}
	// [L2J_JP ADD END SANDMAN]
}
