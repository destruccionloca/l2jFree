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
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.instancemanager.DuelManager;
import com.l2jfree.gameserver.model.L2Attackable;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2FortBallistaInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.model.actor.stat.CharStat;
import com.l2jfree.gameserver.model.entity.Duel;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.tools.random.Rnd;
import com.l2jfree.util.SingletonSet;

/**
 * Represent the status of a character.
 * 
 * Each L2Character and its subclass should use a CharStatus (or a subclass of CharStatus) 
 * to manipulate its status (hp and mp for example).
 * 
 * If a subclass of L2Character needs to add treatment to some methods, this subclass have to use
 * a subclass of CharStatus and redefine the proper method. 
 * And don't forget to override the getStatus() !!
 * 
 */
public class CharStatus
{
	protected static Log		_log				= LogFactory.getLog(CharStatus.class.getName());

	private L2Character			_activeChar;
	private double				_currentCp			= 0;												//Current CP of the L2Character
	private double				_currentHp			= 0;												//Current HP of the L2Character
	private double				_currentMp			= 0;												//Current MP of the L2Character

	/** Array containing all clients that need to be notified about hp/mp updates of the L2Character */
	private Set<L2PcInstance> _statusListeners;

	private Future<?>			_regTask;
	private byte				_flagsRegenActive	= 0;
	private static final byte	REGEN_FLAG_CP		= 4;
	private static final byte	REGEN_FLAG_HP		= 1;
	private static final byte	REGEN_FLAG_MP		= 2;

	public CharStatus(L2Character activeChar)
	{
		_activeChar = activeChar;
	}

	/**
	 * Add the object to the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Target a PC or NPC</li><BR><BR>
	 *
	 * @param player L2Character to add to the listener
	 *
	 */
	public final void addStatusListener(L2PcInstance player)
	{
		if (getActiveChar() == player)
			return;
		
		synchronized (getStatusListeners())
		{
			getStatusListeners().add(player);
		}
	}

	/**
	 * @param value the cp to remove
	 */
	public final void reduceCp(int value)
	{
		if (getCurrentCp() > value)
			setCurrentCp(getCurrentCp() - value);
		else
			setCurrentCp(0);
	}

	/**
	 * Reduce the current HP of the L2Character and launch the doDie Task if necessary.<BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2Attackable : Update the attacker AggroInfo of the L2Attackable _aggroList</li><BR><BR>
	 *
	 * @param value The HP decrease value
	 * @param attacker The L2Character who attacks
	 *
	 */
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false);
	}

	public void reduceHp(double value, L2Character attacker, boolean awake)
	{
		reduceHp(value, attacker, awake, false);
	}

	public void increaseHp(double value)
	{
		setCurrentHp(getCurrentHp() + value);
	}

	/**
	 * Reduce the current HP of the L2Character and launch the doDie Task if necessary.<BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2Attackable : Update the attacker AggroInfo of the L2Attackable _aggroList</li><BR><BR>
	 *
	 * @param value The HP decrease value
	 * @param attacker The L2Character who attacks
	 * @param awake The awake state (If True : stop sleeping)
     * @param isDOT
	 *
	 */
	public void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT)
	{
		if (getActiveChar().isDead() || getActiveChar().isPetrified())
			return;

		if (getActiveChar() instanceof L2FortBallistaInstance && getActiveChar().getMaxHp() == value)
		{
		}
		else if (getActiveChar().isInvul())
			return;

		if (attacker instanceof L2PcInstance)
		{
			L2PcInstance pcInst = (L2PcInstance)attacker;
			if (pcInst.isGM() && pcInst.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
				return;
		}


		L2PcInstance player = null;
		L2PcInstance attackerPlayer = null;

		if (getActiveChar() instanceof L2PcInstance)
			player = (L2PcInstance)getActiveChar();
		else if (getActiveChar() instanceof L2Summon)
			player = ((L2Summon)getActiveChar()).getOwner();

		if (attacker instanceof L2PcInstance)
			attackerPlayer = (L2PcInstance)attacker;
		else if (attacker instanceof L2Summon)
			attackerPlayer = ((L2Summon)attacker).getOwner();

		if (player != null)
		{
			if (player.isInDuel())
			{
				// the duel is finishing - players do not receive damage
				if (player.getDuelState() == Duel.DUELSTATE_DEAD)
					return;
				else if (player.getDuelState() == Duel.DUELSTATE_WINNER)
					return;

				// cancel duel if player got hit by a monster or NPC
				if (!(attacker instanceof L2PlayableInstance))
				{
					player.setDuelState(Duel.DUELSTATE_INTERRUPTED);
				}
				// cancel duel if player got hit by another player or his summon and that is not part of the duel
				else if (attackerPlayer != null && attackerPlayer.getDuelId() != player.getDuelId())
				{
					player.setDuelState(Duel.DUELSTATE_INTERRUPTED);
				}
			}
		}
		else if (attackerPlayer != null && attackerPlayer.isInDuel())
		{
			attackerPlayer.setDuelState(Duel.DUELSTATE_INTERRUPTED);
		}

		if (!isDOT)
		{
			if (awake)
			{
				if (getActiveChar().isSleeping())
					getActiveChar().stopSleeping(null);
				if (getActiveChar().isImmobileUntilAttacked())
					getActiveChar().stopImmobileUntilAttacked(null);
			}

			if (getActiveChar().isStunned() && Rnd.get(10) == 0)
				getActiveChar().stopStunning(null);
		}
		else if (awake && getActiveChar() instanceof L2PcInstance)
		{
			if (getActiveChar().isSleeping())
				getActiveChar().stopSleeping(null);
		}

		// Add attackers to npc's attacker list
		if (getActiveChar() instanceof L2NpcInstance)
			getActiveChar().addAttackerToAttackByList(attacker);

		// Additional prevention
		// Check if player is GM and has sufficient rights to make damage
		if (attackerPlayer != null)
		{
			if (attackerPlayer.isGM() && attackerPlayer.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
				return;
		}

		if (value > 0) // Reduce Hp if any
		{
			// add olympiad damage
			if(player != null && player.isInOlympiadMode() && attackerPlayer != null && attackerPlayer.isInOlympiadMode())
			{
				if (Config.ALT_OLY_SUMMON_DAMAGE_COUNTS
						|| (attacker instanceof L2PcInstance && getActiveChar() instanceof L2PcInstance))
					attackerPlayer.addOlyDamage((int)value);
			}

			// If we're dealing with an L2Attackable Instance and the attacker hit it with an over-hit enabled skill, set the over-hit values.
			// Anything else, clear the over-hit flag
			if (getActiveChar() instanceof L2Attackable)
			{
				if (((L2Attackable) getActiveChar()).isOverhit())
					((L2Attackable) getActiveChar()).setOverhitValues(attacker, value);
				else
					((L2Attackable) getActiveChar()).overhitEnabled(false);
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
				((L2Attackable) getActiveChar()).overhitEnabled(false);
			}
		}

		if (getActiveChar().getStatus().getCurrentHp() < 0.5) // Die
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

			// first die (and calculate rewards), if currentHp < 0,
			// then overhit may be calculated
			if (_log.isDebugEnabled())
				_log.debug("char is dead.");

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
				((L2Attackable) getActiveChar()).overhitEnabled(false);
			}
		}
	}

	/**
	 * @param value the mp to remove
	 */
	public void reduceMp(double value)
	{
		value = getCurrentMp() - value;
		if (value < 0)
			value = 0;
		setCurrentMp(value);
	}

	/**
	 * Remove the object from the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Untarget a PC or NPC</li><BR><BR>
	 *
	 * @param player L2Character to add to the listener
	 *
	 */
	public final void removeStatusListener(L2PcInstance player)
	{
		synchronized (getStatusListeners())
		{
			getStatusListeners().remove(player);
		}
	}

	/**
	 * Start the HP/MP/CP Regeneration task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the regen task period </li>
	 * <li>Launch the HP/MP/CP Regeneration task with Medium priority </li><BR><BR>
	 *
	 */
	public synchronized final void startHpMpRegeneration()
	{
		if (_regTask == null && !getActiveChar().isDead())
		{
			if (_log.isDebugEnabled())
				_log.debug("HP/MP/CP regen started");

			// Get the Regeneration periode
			int period = Formulas.getInstance().getRegeneratePeriod(getActiveChar());

			// Create the HP/MP/CP Regeneration task
			_regTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new RegenTask(), period, period);
		}
	}

	/**
	 * Stop the HP/MP/CP Regeneration task.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the RegenActive flag to False </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
	 *
	 */
	public void stopHpMpRegeneration()
	{
		if (_regTask != null)
		{
			if (_log.isDebugEnabled())
				_log.debug("HP/MP/CP regen stop");

			// Stop the HP/MP/CP Regeneration task
			_regTask.cancel(false);
			_regTask = null;

			// Set the RegenActive flag to false
			_flagsRegenActive = 0;
		}
	}

	public L2Character getActiveChar()
	{
		return _activeChar;
	}

	/**
	 * 
	 * @return the current cp
	 */
	public final double getCurrentCp()
	{
		return _currentCp;
	}

	/**
	 * 
	 * @param newCp the cp to set
	 */
	public final void setCurrentCp(double newCp)
	{
		setCurrentCp(newCp, true);
	}

	/**
	 * 
	 * @param newCp the cp to set
	 * @param broadcastPacket if we had to send a system message
	 */
	public final void setCurrentCp(double newCp, boolean broadcastPacket)
	{
		// Get the Max CP of the L2Character
		int maxCp = getActiveChar().getStat().getMaxCp();

		synchronized (this)
		{
			if (getActiveChar().isDead())
				return;


			if (newCp < 0)
				newCp = 0;

			if (newCp >= maxCp)
			{
				// Set the RegenActive flag to false
				_currentCp = maxCp;
				_flagsRegenActive &= ~REGEN_FLAG_CP;

				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentCp = newCp;
				_flagsRegenActive |= REGEN_FLAG_CP;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
			getActiveChar().broadcastStatusUpdate();
	}

	/** 
	* @return the current hp
	*/
	public final double getCurrentHp()
	{
		return _currentHp;
	}

	/**
	 * 
	 * @param newHp the hp to set
	 */
	public final void setCurrentHp(double newHp)
	{
		setCurrentHp(newHp, true);
	}

	/**
	 * 
	 * @param newHp the hp to set
	 * @param broadcastPacket if we have to broadcast the information
	 */
	public final void setCurrentHp(double newHp, boolean broadcastPacket)
	{
		// Get the Max HP of the L2Character
		double maxHp = getActiveChar().getStat().getMaxHp();
		synchronized (this)
		{
			if (getActiveChar().isDead())
				return;
			if (newHp >= maxHp)
			{
				// Set the RegenActive flag to false
				_currentHp = maxHp;
				_flagsRegenActive &= ~REGEN_FLAG_HP;

				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentHp = newHp;
				_flagsRegenActive |= REGEN_FLAG_HP;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		if (getActiveChar() instanceof L2PcInstance)
		{
			if (getCurrentHp() <= maxHp * .3)
			{
				QuestState qs = ((L2PcInstance) getActiveChar()).getQuestState("255_Tutorial");
				if (qs != null)
					qs.getQuest().notifyEvent("CE45", null, ((L2PcInstance) getActiveChar()));
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
			getActiveChar().broadcastStatusUpdate();
	}

	/**
	 * 
	 * @param newHp the hp to set
	 * @param newMp the mp to set
	 */
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHp(newHp, false);
		setCurrentMp(newMp, true); //send the StatusUpdate only once
	}

	/**
	 * 
	 * @return the current mp
	 */
	public final double getCurrentMp()
	{
		return _currentMp;
	}

	/**
	 * 
	 * @param newMp the mp to set
	 */
	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}

	/**
	 * 
	 * @param newMp the mp to set
	 * @param broadcastPacket true if we have to broadcast information
	 */
	public final void setCurrentMp(double newMp, boolean broadcastPacket)
	{
		// Get the Max MP of the L2Character
		int maxMp = getActiveChar().getStat().getMaxMp();
		synchronized (this)
		{
			if (getActiveChar().isDead())
				return;

			if (newMp >= maxMp)
			{
				// Set the RegenActive flag to false
				_currentMp = maxMp;
				_flagsRegenActive &= ~REGEN_FLAG_MP;

				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
					stopHpMpRegeneration();
			}
			else
			{
				// Set the RegenActive flag to true
				_currentMp = newMp;
				_flagsRegenActive |= REGEN_FLAG_MP;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (broadcastPacket)
			getActiveChar().broadcastStatusUpdate();
	}

	/**
	 * Return the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 *
	 * @return The list of L2Character to inform or null if empty
	 *
	 */
	public final Set<L2PcInstance> getStatusListeners() 
	{
		if (_statusListeners == null)
			_statusListeners = new SingletonSet<L2PcInstance>();
		
		return _statusListeners; 
	}

	/** 
	 * Task of HP/MP/CP regeneration 
	*/
	class RegenTask implements Runnable
	{
		public void run()
		{
			try
			{
				CharStat charstat = getActiveChar().getStat();

				// Modify the current CP of the L2Character and broadcast Server->Client packet StatusUpdate
				if (getCurrentCp() < charstat.getMaxCp())
					setCurrentCp(getCurrentCp() + Formulas.getInstance().calcCpRegen(getActiveChar()), false);

				// Modify the current HP of the L2Character and broadcast Server->Client packet StatusUpdate
				if (getCurrentHp() < charstat.getMaxHp())
					setCurrentHp(getCurrentHp() + Formulas.getInstance().calcHpRegen(getActiveChar()), false);

				// Modify the current MP of the L2Character and broadcast Server->Client packet StatusUpdate
				if (getCurrentMp() < charstat.getMaxMp())
					setCurrentMp(getCurrentMp() + Formulas.getInstance().calcMpRegen(getActiveChar()), false);

				if (!getActiveChar().isInActiveRegion())
				{
					// no broadcast necessary for characters that are in inactive regions.
					// stop regeneration for characters who are filled up and in an inactive region.
					if ((getCurrentCp() == charstat.getMaxCp()) && (getCurrentHp() == charstat.getMaxHp()) && (getCurrentMp() == charstat.getMaxMp()))
						stopHpMpRegeneration();
				}
				else
					getActiveChar().broadcastStatusUpdate(); //send the StatusUpdate packet
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}
	}
}
