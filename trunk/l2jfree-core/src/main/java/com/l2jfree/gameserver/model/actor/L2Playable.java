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
package com.l2jfree.gameserver.model.actor;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.knownlist.PlayableKnownList;
import com.l2jfree.gameserver.model.actor.stat.PcStat;
import com.l2jfree.gameserver.model.actor.stat.PlayableStat;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.taskmanager.PacketBroadcaster.BroadcastMode;
import com.l2jfree.gameserver.templates.chars.L2CharTemplate;
import com.l2jfree.gameserver.templates.skills.L2EffectType;

/**
 * This class represents all Playable characters in the world.<BR><BR>
 * 
 * L2Playable :<BR><BR>
 * <li>L2PcInstance</li>
 * <li>L2Summon</li><BR><BR>
 * 
 */
public abstract class L2Playable extends L2Character
{
	public static final L2Playable[] EMPTY_ARRAY = new L2Playable[0];
	
	private boolean	_isNoblesseBlessed	= false;	// For Noblesse Blessing skill, restores buffs after death
	private boolean	_getCharmOfLuck		= false;	// Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	private boolean	_isPhoenixBlessed	= false;	// For Soul of The Phoenix or Salvation buffs
	private boolean	_isSilentMoving		= false;	// Silent Move
	private boolean	_protectionBlessing	= false;	// Blessed by Blessing of Protection

	/**
	 * Constructor of L2Playable (use L2Character constructor).<BR><BR>
	 * 
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and link copy basic Calculator set to this L2Playable </li><BR><BR>
	 * 
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the L2Playable
	 * 
	 */
	public L2Playable(int objectId, L2CharTemplate template)
	{
		super(objectId, template);
		getKnownList(); // Init knownlist
		getStat(); // Init stats
		getStatus(); // Init status
	}

	@Override
	public PlayableKnownList getKnownList()
	{
		if (_knownList == null)
			_knownList = new PlayableKnownList(this);
		
		return (PlayableKnownList)_knownList;
	}

	@Override
	public PlayableStat getStat()
	{
		if (_stat == null)
			_stat = new PlayableStat(this);
		
		return (PcStat)_stat;
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (killer != null && killer.getActingPlayer() != null)
		{
			killer.getActingPlayer().onKillUpdatePvPKarma(this);
		}

		return true;
	}

	public boolean checkIfPvP(L2Character target)
	{
		if (target == null)
			return false; // Target is null
		if (target == this)
			return false; // Target is self
		if (!(target instanceof L2Playable))
			return false; // Target is not a L2Playable

		L2PcInstance player = null;
		if (this instanceof L2PcInstance)
			player = (L2PcInstance) this;
		else if (this instanceof L2Summon)
			player = ((L2Summon) this).getOwner();

		if (player == null)
			return false; // Active player is null
		if (player.getKarma() != 0)
			return false; // Active player has karma

		L2PcInstance targetPlayer = null;
		if (target instanceof L2PcInstance)
			targetPlayer = (L2PcInstance) target;
		else if (target instanceof L2Summon)
			targetPlayer = ((L2Summon) target).getOwner();

		if (targetPlayer == null)
			return false; // Target player is null

		if (targetPlayer == this)
			return false; // Target player is self

        return targetPlayer.getKarma() == 0;
	}

	/**
	 * Return True.<BR><BR>
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}

	// Support for Noblesse Blessing skill, where buffs are retained after resurrect
	public final boolean isNoblesseBlessed()
	{
		return _isNoblesseBlessed;
	}

	public final void setIsNoblesseBlessed(boolean value)
	{
		_isNoblesseBlessed = value;
		updateAbnormalEffect();
	}

	public final void startNoblesseBlessing()
	{
		setIsNoblesseBlessed(true);
	}

	public final void stopNoblesseBlessing(boolean all)
	{
		if (all)
			stopEffects(L2EffectType.NOBLESSE_BLESSING);

		setIsNoblesseBlessed(false);
	}

	// Support for Soul of the Phoenix and Salvation skills
	public final boolean isPhoenixBlessed()
	{
		return _isPhoenixBlessed;
	}

	public final void setIsPhoenixBlessed(boolean value)
	{
		_isPhoenixBlessed = value;
		updateAbnormalEffect();
	}

	public final void startPhoenixBlessing()
	{
		setIsPhoenixBlessed(true);
	}

	public final void stopPhoenixBlessing(boolean all)
	{
		if (all)
			stopEffects(L2EffectType.PHOENIX_BLESSING);

		setIsPhoenixBlessed(false);
	}

	/**
	 * Set the Silent Moving mode Flag.<BR><BR>
	 */
	public void setSilentMoving(boolean flag)
	{
		_isSilentMoving = flag;
	}

	/**
	 * Return True if the Silent Moving mode is active.<BR><BR>
	 */
	public boolean isSilentMoving()
	{
		return _isSilentMoving;
	}

	// For Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you
	public final boolean getProtectionBlessing()
	{
		return _protectionBlessing;
	}

	public final void setProtectionBlessing(boolean value)
	{
		_protectionBlessing = value;
		updateAbnormalEffect();
	}

	public void startProtectionBlessing()
	{
		setProtectionBlessing(true);
	}

	 /**
	 * @param effect
	 */
	public void stopProtectionBlessing(boolean all)
	{
		if (all)
			stopEffects(L2EffectType.PROTECTION_BLESSING);

		setProtectionBlessing(false);
	}

	// Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	public final boolean getCharmOfLuck()
	{
		return _getCharmOfLuck;
	}

	public final void setCharmOfLuck(boolean value)
	{
		_getCharmOfLuck = value;
		updateAbnormalEffect();
	}

	public final void startCharmOfLuck()
	{
		setCharmOfLuck(true);
	}

	public final void stopCharmOfLuck(boolean all)
	{
		if (all)
			stopEffects(L2EffectType.CHARM_OF_LUCK);

		setCharmOfLuck(false);
	}
	
	public final void updateEffectIcons()
	{
		addPacketBroadcastMask(BroadcastMode.UPDATE_EFFECT_ICONS);
	}
	
	public abstract void updateEffectIconsImpl();
	
	@Override
	public final void onForcedAttack(L2PcInstance player)
	{
		final L2PcInstance targetPlayer = getActingPlayer();
		
		if (player.getOlympiadGameId() != targetPlayer.getOlympiadGameId())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInOlympiadMode() && targetPlayer.isInOlympiadMode())
		{
			if (!player.isOlympiadStart())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		super.onForcedAttack(player);
	}
}