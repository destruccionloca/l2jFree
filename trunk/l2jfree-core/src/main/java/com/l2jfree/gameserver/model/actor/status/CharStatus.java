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
package com.l2jfree.gameserver.model.actor.status;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.instancemanager.DuelManager;
import com.l2jfree.gameserver.model.actor.L2Attackable;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.instance.L2FortBallistaInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance.ConditionListenerDependency;
import com.l2jfree.gameserver.model.actor.stat.CharStat;
import com.l2jfree.gameserver.model.entity.Duel;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.taskmanager.AbstractIterativePeriodicTaskManager;
import com.l2jfree.tools.random.Rnd;
import com.l2jfree.util.SingletonSet;

public class CharStatus
{
	protected static final Log _log = LogFactory.getLog(CharStatus.class);
	
	protected final L2Character _activeChar;
	private final int _period;
	
	private double _currentHp = 0;
	private double _currentMp = 0;
	private double _currentCp = 0;
	
	public CharStatus(L2Character activeChar)
	{
		_activeChar = activeChar;
		_period = Formulas.getRegeneratePeriod(_activeChar);
	}
	
	// ========================================================================
	
	protected L2Character getActiveChar()
	{
		return _activeChar;
	}
	
	public final double getCurrentHp()
	{
		return _currentHp;
	}
	
	public final double getCurrentMp()
	{
		return _currentMp;
	}
	
	public final double getCurrentCp()
	{
		return _currentCp;
	}
	
	// ========================================================================
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHp(newHp);
		setCurrentMp(newMp);
	}
	
	public final void setCurrentHp(double newHp)
	{
		if (getActiveChar().isDead())
			return;
		
		if (setCurrentHp0(newHp))
			startHpMpRegeneration();
	}
	
	private boolean setCurrentHp0(double newHp)
	{
		double maxHp = getActiveChar().getStat().getMaxHp();
		if (newHp < 0)
			newHp = 0;
		
		boolean requireRegen;
		
		synchronized (this)
		{
			final double currentHp = _currentHp;
			
			if (newHp >= maxHp)
			{
				_currentHp = maxHp;
				requireRegen = false;
			}
			else
			{
				_currentHp = newHp;
				requireRegen = true;
			}
			
			if (currentHp != _currentHp)
				getActiveChar().broadcastStatusUpdate();
		}
		
		if (getActiveChar() instanceof L2PcInstance)
		{
			if (getCurrentHp() <= maxHp * 0.3)
			{
				QuestState qs = ((L2PcInstance)getActiveChar()).getQuestState("255_Tutorial");
				if (qs != null)
					qs.getQuest().notifyEvent("CE45", null, ((L2PcInstance)getActiveChar()));
			}
			
			((L2PcInstance)getActiveChar()).refreshConditionListeners(ConditionListenerDependency.PLAYER_HP);
		}
		
		return requireRegen;
	}
	
	public final void setCurrentMp(double newMp)
	{
		if (getActiveChar().isDead())
			return;
		
		if (setCurrentMp0(newMp))
			startHpMpRegeneration();
	}
	
	private boolean setCurrentMp0(double newMp)
	{
		double maxMp = getActiveChar().getStat().getMaxMp();
		if (newMp < 0)
			newMp = 0;
		
		boolean requireRegen;
		
		synchronized (this)
		{
			final double currentMp = _currentMp;
			
			if (newMp >= maxMp)
			{
				_currentMp = maxMp;
				requireRegen = false;
			}
			else
			{
				_currentMp = newMp;
				requireRegen = true;
			}
			
			if (currentMp != _currentMp)
				getActiveChar().broadcastStatusUpdate();
		}
		
		return requireRegen;
	}
	
	public final void setCurrentCp(double newCp)
	{
		if (getActiveChar().isDead())
			return;
		
		if (setCurrentCp0(newCp))
			startHpMpRegeneration();
	}
	
	private boolean setCurrentCp0(double newCp)
	{
		double maxCp = getActiveChar().getStat().getMaxCp();
		if (newCp < 0)
			newCp = 0;
		
		boolean requireRegen;
		
		synchronized (this)
		{
			final double currentCp = _currentCp;
			
			if (newCp >= maxCp)
			{
				_currentCp = maxCp;
				requireRegen = false;
			}
			else
			{
				_currentCp = newCp;
				requireRegen = true;
			}
			
			if (currentCp != _currentCp)
				getActiveChar().broadcastStatusUpdate();
		}
		
		return requireRegen;
	}
	
	// ========================================================================
	
	boolean canReduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isConsume)
	{
		if (attacker == null || getActiveChar().isDead())
			return false;
		
		if (getActiveChar() instanceof L2FortBallistaInstance && getActiveChar().getMaxHp() == value)
		{
		}
		else if (getActiveChar().isInvul())
		{
			if (attacker == getActiveChar())
			{
				if (!isDOT && !isConsume)
					return false;
			}
			else
				return false;
		}
		
		L2PcInstance attackerPlayer = attacker.getActingPlayer();
		
		// Additional prevention
		// Check if player is GM and has sufficient rights to make damage
		if (attackerPlayer != null)
			if (attackerPlayer.isGM() && attackerPlayer.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
				return false;
		
		if (Duel.isInvul(attacker, getActiveChar()))
			return false;
		
		return true;
	}
	
	public final void increaseHp(double value)
	{
		setCurrentHp(getCurrentHp() + value);
	}
	
	public final void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true);
	}
	
	public final void reduceHp(double value, L2Character attacker, boolean awake)
	{
		reduceHp(value, attacker, awake, false, false);
	}
	
	public final void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isConsume)
	{
		if (!canReduceHp(value, attacker, awake, isDOT, isConsume))
			return;
		
		reduceHp0(value, attacker, awake, isDOT, isConsume);
	}
	
	public final void reduceHpByDOT(double value, L2Character attacker)
	{
		reduceHp(value, attacker, false, true, false);
	}
	
	public final void reduceHpByConsume(double value)
	{
		reduceHp(value, getActiveChar(), false, false, true);
	}
	
	void reduceHp0(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isConsume)
	{
		if (!isDOT)
		{
			if (awake)
			{
				if (getActiveChar().isSleeping())
					getActiveChar().stopSleeping(true);
				if (getActiveChar().isImmobileUntilAttacked())
					getActiveChar().stopImmobileUntilAttacked(true);
			}
			
			if (getActiveChar().isStunned() && Rnd.get(10) == 0)
				getActiveChar().stopStunning(true);
		}
		else if (awake && getActiveChar() instanceof L2PcInstance)
		{
			if (getActiveChar().isSleeping())
				getActiveChar().stopSleeping(true);
		}
		
		final L2PcInstance player = getActiveChar().getActingPlayer();
		final L2PcInstance attackerPlayer = attacker.getActingPlayer();
		
		if (value > 0) // Reduce Hp if any
		{
			// add olympiad damage
			if (player != null && player.isInOlympiadMode() && attackerPlayer != null
					&& attackerPlayer.isInOlympiadMode())
			{
				if (Config.ALT_OLY_SUMMON_DAMAGE_COUNTS
						|| (attacker instanceof L2PcInstance && getActiveChar() instanceof L2PcInstance))
					attackerPlayer.addOlyDamage((int)value);
			}
			
			// If we're dealing with an L2Attackable Instance and the attacker hit it with an over-hit enabled skill, set the over-hit values.
			// Anything else, clear the over-hit flag
			if (getActiveChar() instanceof L2Attackable)
			{
				if (((L2Attackable)getActiveChar()).isOverhit())
					((L2Attackable)getActiveChar()).setOverhitValues(attacker, value);
				else
					((L2Attackable)getActiveChar()).overhitEnabled(false);
			}
			value = getCurrentHp() - value; // Get diff of Hp vs value
			if (value <= 0)
			{
				// is the dying a duelist? if so, change his duel state to dead
				if (player != null && player.isInDuel() && getActiveChar() instanceof L2PcInstance) // pets can die as usual
				{
					getActiveChar().disableAllSkills();
					stopHpMpRegeneration();
					attacker.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					attacker.sendPacket(ActionFailed.STATIC_PACKET);
					
					// let the DuelManager know of his defeat
					DuelManager.getInstance().onPlayerDefeat(player);
					value = 1;
				}
				else
					value = 0; // Set value to 0 if Hp < 0
			}
			
			setCurrentHp(value); // Set Hp
		}
		else
		{
			// If we're dealing with an L2Attackable Instance and the attacker's hit didn't kill the mob, clear the over-hit flag
			if (getActiveChar() instanceof L2Attackable)
			{
				((L2Attackable)getActiveChar()).overhitEnabled(false);
			}
		}
		
		if (getCurrentHp() < 0.5) // Die
		{
			if (player != null && player.isInOlympiadMode() && getActiveChar() instanceof L2PcInstance) // pets can die as usual
			{
				stopHpMpRegeneration();
				player.setIsDead(true);
				player.setIsPendingRevive(true);
				if (player.getPet() != null)
					player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
				return;
			}
			
			// Start the doDie process
			getActiveChar().doDie(attacker);
			
			if (player != null)
			{
				QuestState qs = player.getQuestState("255_Tutorial");
				if (qs != null)
					qs.getQuest().notifyEvent("CE30", null, player);
			}
		}
		else
		{
			// If we're dealing with an L2Attackable Instance and the attacker's hit didn't kill the mob, clear the over-hit flag
			if (getActiveChar() instanceof L2Attackable)
			{
				((L2Attackable)getActiveChar()).overhitEnabled(false);
			}
		}
		
		return;
	}
	
	public final void increaseMp(double value)
	{
		setCurrentMp(getCurrentMp() + value);
	}
	
	public void reduceMp(double value)
	{
		setCurrentMp(getCurrentMp() - value);
	}
	
	public final void reduceCp(int value)
	{
		setCurrentCp(getCurrentCp() - value);
	}
	
	// ========================================================================
	
	private static final class RegenTaskManager extends AbstractIterativePeriodicTaskManager<CharStatus>
	{
		private static final RegenTaskManager _instance = new RegenTaskManager();
		
		private static RegenTaskManager getInstance()
		{
			return _instance;
		}
		
		private RegenTaskManager()
		{
			super(1000);
		}
		
		@Override
		protected void callTask(CharStatus task)
		{
			if (task.regenTask())
				startTask(task);
			else
				stopTask(task);
		}
		
		@Override
		protected String getCalledMethodName()
		{
			return "regenTask()";
		}
	}
	
	private long _lastRunTime = System.currentTimeMillis();
	
	public final void startHpMpRegeneration()
	{
		if (getActiveChar().isDead())
			return;
		
		RegenTaskManager.getInstance().startTask(this);
	}
	
	public final void stopHpMpRegeneration()
	{
		RegenTaskManager.getInstance().stopTask(this);
	}
	
	public final boolean regenTask()
	{
		if (getActiveChar().isDead())
			return false;
		
		if (System.currentTimeMillis() < _lastRunTime + _period)
			return true;
		
		_lastRunTime = System.currentTimeMillis();
		
		CharStat cs = getActiveChar().getStat();
		
		boolean requireRegen = false;
		
		if (getCurrentHp() < cs.getMaxHp())
			requireRegen |= setCurrentHp0(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()));
		
		if (getCurrentMp() < cs.getMaxMp())
			requireRegen |= setCurrentMp0(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()));
		
		if (getCurrentCp() < cs.getMaxCp())
			requireRegen |= setCurrentCp0(getCurrentCp() + Formulas.calcCpRegen(getActiveChar()));
		
		return requireRegen;
	}
	
	// ========================================================================
	// Status Listeners
	
	private Set<L2PcInstance> _statusListeners;
	
	public final Set<L2PcInstance> getStatusListeners()
	{
		if (_statusListeners == null)
			_statusListeners = new SingletonSet<L2PcInstance>();
		
		return _statusListeners;
	}
	
	public final void addStatusListener(L2PcInstance player)
	{
		synchronized (getStatusListeners())
		{
			if (getActiveChar() != player)
				getStatusListeners().add(player);
		}
	}
	
	public final void removeStatusListener(L2PcInstance player)
	{
		synchronized (getStatusListeners())
		{
			getStatusListeners().remove(player);
		}
	}
}
