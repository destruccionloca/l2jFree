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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2CharacterAI;
import com.l2jfree.gameserver.ai.L2FortSiegeGuardAI;
import com.l2jfree.gameserver.model.L2CharPosition;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.L2SiegeGuard;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.knownlist.FortSiegeGuardKnownList;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

public class L2FortSiegeGuardInstance extends L2SiegeGuard
{
	private final static Log	_log	= LogFactory.getLog(L2FortSiegeGuardInstance.class.getName());

	public L2FortSiegeGuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // Inits the knownlist
	}

	@Override
	public FortSiegeGuardKnownList getKnownList()
	{
		if (_knownList == null)
			_knownList = new FortSiegeGuardKnownList(this);
		
		return (FortSiegeGuardKnownList)_knownList;
	}

	@Override
	public L2CharacterAI getAI()
	{
		 L2CharacterAI ai = _ai; // Copy handle
		 if (ai == null)
		 {
			 synchronized(this)
			 {
				 if (_ai == null)
					_ai = new L2FortSiegeGuardAI(new AIAccessor());
				 return _ai;
			 }
		 }
		 return ai;
	}

	/**
	 * Return True if a siege is in progress and the L2Character attacker isn't
	 * a Defender.<BR>
	 * <BR>
	 * 
	 * @param attacker The L2Character that the L2FortSiegeGuardInstance try to
	 *            attack
	 * 
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		// Summons and traps are attackable, too
		L2PcInstance player = attacker.getActingPlayer();
		if (player == null)
			return false;
		if (player.getClan() == null)
			return true;

		boolean isFort = ( getFort() != null && getFort().getSiege().getIsInProgress()
				&& !getFort().getSiege().checkIsDefender(player.getClan()));

		// Attackable during siege by all except defenders ( Castle or Fort )
		return isFort;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	/**
	 * This method forces guard to return to home location previously set
	 * 
	 */
	public void returnHome()
	{
		if (getStat().getWalkSpeed() <= 0)
			return;

		if (getSpawn() != null && !isInsideRadius(getSpawn().getLocx(), getSpawn().getLocy(), 40, false))
		{
			if (_log.isDebugEnabled())
				_log.debug(getObjectId() + ": moving home");
			setisReturningToSpawnPoint(true);
			clearAggroList();

			if (hasAI())
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(getSpawn().getLocx(), getSpawn().getLocy(), getSpawn().getLocz(), 0));
		}
	}

	/**
	 * Custom onAction behaviour. Note that super() is not called because guards
	 * need extra check to see if a player should interact or ATTACK them when
	 * clicked.
	 * 
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		if (!canTarget(player))
			return;

		// Check if the L2PcInstance already target the L2NpcInstance
		if (this != player.getTarget())
		{
			if (_log.isDebugEnabled())
				_log.debug("new target selected:" + getObjectId());

			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet StatusUpdate of the L2NpcInstance to the L2PcInstance to update its HP bar
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
		}
		else
		{
			if (isAutoAttackable(player) && !isAlikeDead())
			{
				if (Math.abs(player.getZ() - getZ()) < 600) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
				else
				{
					// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			if (!isAutoAttackable(player))
			{
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with AI_INTENTION_INTERACT
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
				else
				{
					showChatWindow(player, 0);
				}
				// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}

	@Override
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
			return;

		if (!(attacker instanceof L2FortSiegeGuardInstance))
		{
			if (attacker instanceof L2Playable)
			{
				L2PcInstance player = null;
				if (attacker instanceof L2PcInstance)
					player = ((L2PcInstance)attacker);
				else if (attacker instanceof L2Summon)
					player = ((L2Summon)attacker).getOwner();
				if (player != null && player.getClan() != null && player.getClan().getHasFort() == getFort().getFortId())
					return;
			}
			super.addDamageHate(attacker, damage, aggro);
		}
	}
}