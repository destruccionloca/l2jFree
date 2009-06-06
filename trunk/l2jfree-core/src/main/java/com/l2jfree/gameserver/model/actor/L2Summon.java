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

import com.l2jfree.Config;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2CharacterAI;
import com.l2jfree.gameserver.ai.L2SummonAI;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.geodata.GeoData;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Party;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2WorldRegion;
import com.l2jfree.gameserver.model.actor.L2Attackable.AggroInfo;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2MerchantSummonInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfree.gameserver.model.actor.knownlist.SummonKnownList;
import com.l2jfree.gameserver.model.actor.reference.ImmutableReference;
import com.l2jfree.gameserver.model.actor.shot.SummonShots;
import com.l2jfree.gameserver.model.actor.stat.SummonStat;
import com.l2jfree.gameserver.model.base.Experience;
import com.l2jfree.gameserver.model.itemcontainer.PetInventory;
import com.l2jfree.gameserver.model.olympiad.Olympiad;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.AbstractNpcInfo;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ExPartyPetWindowAdd;
import com.l2jfree.gameserver.network.serverpackets.ExPartyPetWindowDelete;
import com.l2jfree.gameserver.network.serverpackets.ExPartyPetWindowUpdate;
import com.l2jfree.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfree.gameserver.network.serverpackets.PartySpelled;
import com.l2jfree.gameserver.network.serverpackets.PetDelete;
import com.l2jfree.gameserver.network.serverpackets.PetInfo;
import com.l2jfree.gameserver.network.serverpackets.PetStatusShow;
import com.l2jfree.gameserver.network.serverpackets.PetStatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.UserInfo;
import com.l2jfree.gameserver.network.serverpackets.EffectInfoPacket.EffectInfoPacketList;
import com.l2jfree.gameserver.taskmanager.DecayTaskManager;
import com.l2jfree.gameserver.taskmanager.LeakTaskManager;
import com.l2jfree.gameserver.taskmanager.SQLQueue;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.templates.item.L2Weapon;

public abstract class L2Summon extends L2Playable
{
	public static final int	SIEGE_GOLEM_ID			= 14737;
	public static final int	HOG_CANNON_ID			= 14768;
	public static final int	SWOOP_CANNON_ID			= 14839;

	private L2PcInstance	_owner;
	private int				_attackRange			= 36;		//Melee range
	private boolean			_follow					= true;
	private boolean			_previousFollowStatus	= true;

	// TODO: currently, all servitors use 1 shot.  However, this value should vary depending on the servitor template (id and level)!
	private int				_soulShotsPerHit		= 1;
	private int				_spiritShotsPerHit		= 1;

	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}

		public L2Summon getSummon()
		{
			return L2Summon.this;
		}

		public boolean isAutoFollow()
		{
			return getFollowStatus();
		}

		public void doPickupItem(L2Object object)
		{
			L2Summon.this.doPickupItem(object);
		}
	}

	public L2Summon(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		getStat(); // init stats
		getStatus(); // init status

		setInstanceId(owner.getInstanceId()); // set instance to owners one

		_showSummonAnimation = true;
		_owner = owner;
		_ai = new L2SummonAI(new L2Summon.AIAccessor());

		getPosition().setXYZInvisible(owner.getX() + 50, owner.getY() + 100, owner.getZ() + 100);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		if (!(this instanceof L2MerchantSummonInstance))
		{
			setFollowStatus(true);
			broadcastFullInfoImpl(0);
			getOwner().broadcastRelationChanged();
			L2Party party = getOwner().getParty();
			if (party != null)
			{
				party.broadcastToPartyMembers(getOwner(), new ExPartyPetWindowAdd(this));
			}
			if (this instanceof L2SummonInstance && getOwner() != null && getOwner().getActiveWeaponInstance() != null)
			{
				getOwner().getActiveWeaponInstance().updateElementAttrBonus(getOwner());
				this.getOwner().sendPacket(new UserInfo(this.getOwner()));
			}
		}

		setShowSummonAnimation(false); // addVisibleObject created the info packets with summon animation
		// if someone comes into range now, the animation shouldnt show any more
	}

	@Override
	public final SummonKnownList getKnownList()
	{
		if (_knownList == null)
			_knownList = new SummonKnownList(this);

		return (SummonKnownList) _knownList;
	}

	@Override
	public SummonStat getStat()
	{
		if (_stat == null)
			_stat = new SummonStat(this);

		return (SummonStat) _stat;
	}
	
	@Override
	public L2CharacterAI getAI()
	{
		if (this instanceof L2MerchantSummonInstance)
			return null;

		L2CharacterAI ai = _ai; // copy handle
		if (ai == null)
		{
			synchronized (this)
			{
				if (_ai == null)
					_ai = new L2SummonAI(new L2Summon.AIAccessor());
				return _ai;
			}
		}
		return ai;
	}

	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}

	// this defines the action buttons, 1 for Summon, 2 for Pets
	public abstract int getSummonType();

	/**
	 * @return Returns the mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		if (player == _owner && player.getTarget() == this)
		{
			player.sendPacket(new PetStatusShow(this));
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (player.getTarget() != this)
		{
			if (_log.isDebugEnabled())
				_log.debug("new target selected:" + getObjectId());
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
			player.sendPacket(my);

			//sends HP/MP status of the summon to other characters
			StatusUpdate su = new StatusUpdate(getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
			su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
			player.sendPacket(su);
		}
		else if (player.getTarget() == this)
		{
			if (isAutoAttackable(player))
			{
				if (Config.GEODATA > 0)
				{
					if (GeoData.getInstance().canSeeTarget(player, this))
					{
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
						player.onActionRequest();
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
					player.onActionRequest();
				}
			}
			else
			{
				// This Action Failed packet avoids player getting stuck when clicking three or more times
				player.sendPacket(ActionFailed.STATIC_PACKET);
				if (Config.GEODATA > 0)
				{
					if (GeoData.getInstance().canSeeTarget(player, this))
						player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
				}
				else
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, this);
			}
		}
	}

	public long getExpForThisLevel()
	{
		if (getLevel() >= Experience.LEVEL.length)
		{
			return 0;
		}
		return Experience.LEVEL[getLevel()];
	}

	public long getExpForNextLevel()
	{
		if (getLevel() >= Experience.LEVEL.length - 1)
		{
			return 0;
		}
		return Experience.LEVEL[getLevel() + 1];
	}

	public final int getKarma()
	{
		return getOwner() != null ? getOwner().getKarma() : 0;
	}

	public final L2PcInstance getOwner()
	{
		return _owner;
	}

	public final int getNpcId()
	{
		return getTemplate().getNpcId();
	}

	public final int getSoulShotsPerHit()
	{
		return _soulShotsPerHit;
	}

	public final int getSpiritShotsPerHit()
	{
		return _spiritShotsPerHit;
	}

	public void followOwner()
	{
		setFollowStatus(true);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
			return false;

		if (this instanceof L2MerchantSummonInstance)
			return true;

		L2PcInstance owner = getOwner();

		if (owner != null)
		{
			for (L2Character TgMob : getKnownList().getKnownCharacters())
			{
				// get the mobs which have aggro on the this instance
				if (TgMob instanceof L2Attackable)
				{
					if (TgMob.isDead())
						continue;

					AggroInfo info = ((L2Attackable) TgMob).getAggroListRP().get(this);
					if (info != null)
						((L2Attackable) TgMob).addDamageHate(owner, info._damage, info._hate);
				}
			}
		}

		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}

	public boolean doDie(L2Character killer, boolean decayed)
	{
		if (!super.doDie(killer))
			return false;
		if (!decayed)
		{
			DecayTaskManager.getInstance().addDecayTask(this);
		}

		return true;
	}

	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}

	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}

	@Override
	public final void broadcastStatusUpdateImpl()
	{
		getOwner().sendPacket(new PetStatusUpdate(this));
		
		final L2Party party = getParty();
		
		if (party != null)
			party.broadcastToPartyMembers(getOwner(), new ExPartyPetWindowUpdate(this));
	}
	
	@Override
	public final void updateEffectIconsImpl()
	{
		if (this instanceof L2MerchantSummonInstance)
			return;

		final EffectInfoPacketList list = new EffectInfoPacketList(this);
		
		final L2Party party = getParty();
		
		if (party != null)
			party.broadcastToPartyMembers(new PartySpelled(list));
		else
			getOwner().sendPacket(new PartySpelled(list));
	}

	public void deleteMe(L2PcInstance owner)
	{
		if (this instanceof L2MerchantSummonInstance)
			return;

		getAI().stopFollow();
		owner.sendPacket(new PetDelete(getObjectId(), 2));
		L2Party party = owner.getParty();
		if (party != null)
			party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));

		//pet will be deleted along with all his items
		if (getInventory() != null)
		{
			getInventory().destroyAllItems("pet deleted", getOwner(), this);
		}

		stopAllEffects();

		L2WorldRegion oldRegion = getWorldRegion();
		decayMe();
		if (oldRegion != null)
			oldRegion.removeFromZones(this);

		getKnownList().removeAllKnownObjects();
		owner.setPet(null);
		setTarget(null);
		
		LeakTaskManager.getInstance().add(this);
	}

	public final void unSummon()
	{
		unSummon(getOwner());
	}
	
	public void unSummon(L2PcInstance owner)
	{
		if (isVisible())
		{
			if (!(this instanceof L2MerchantSummonInstance))
			{
				getAI().stopFollow();
				owner.sendPacket(new PetDelete(getObjectId(), 2));
				L2Party party;
				if ((party = owner.getParty()) != null)
				{
					party.broadcastToPartyMembers(owner, new ExPartyPetWindowDelete(this));
				}
				
				store();
				giveAllToOwner();
				SQLQueue.getInstance().run();
				owner.setPet(null);
			}

			stopAllEffects();

			L2WorldRegion oldRegion = getWorldRegion();
			decayMe();
			if (oldRegion != null)
				oldRegion.removeFromZones(this);

			getKnownList().removeAllKnownObjects();
			setTarget(null);
			
			LeakTaskManager.getInstance().add(this);
		}
	}
	
	private ImmutableReference<L2Summon> _immutableReference;
	
	public ImmutableReference<L2Summon> getImmutableReference()
	{
		if (_immutableReference == null)
			_immutableReference = new ImmutableReference<L2Summon>(this);
		
		return _immutableReference;
	}
	
	public int getAttackRange()
	{
		return _attackRange;
	}

	public void setAttackRange(int range)
	{
		if (range < 36)
			range = 36;
		_attackRange = range;
	}

	public void setFollowStatus(boolean state)
	{
		_follow = state;
		if (_follow)
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getOwner());
		else
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
	}

	public boolean getFollowStatus()
	{
		return _follow;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}

	public int getControlItemId()
	{
		return 0;
	}

	public L2Weapon getActiveWeapon()
	{
		return null;
	}

	@Override
	public PetInventory getInventory()
	{
		return null;
	}

	public int getCurrentLoad()
	{
		return 0;
	}

	public int getMaxLoad()
	{
	    return 0;
	}
	
	/**
	 * @param object
	 */
	protected void doPickupItem(L2Object object)
	{
	}

	public void giveAllToOwner()
	{
	}

	public void store()
	{
	}

	/**
	 * Return True if the L2Summon is invulnerable or if the summoner is in spawn protection.<BR><BR>
	 */
	@Override
	public boolean isInvul()
	{
		return _isInvul || _isTeleporting || getOwner().getProtection() > 0;
	}

	public abstract int getCurrentFed();

	public abstract int getMaxFed();

	/**
	 * Return the L2Party object of its L2PcInstance owner or null.<BR><BR>
	 */
	@Override
	public L2Party getParty()
	{
		if (_owner == null)
			return null;

		return _owner.getParty();
	}

	/**
	 * Return True if the L2Character has a Party in progress.<BR><BR>
	 */
	@Override
	public boolean isInParty()
	{
		if (_owner == null)
			return false;

		return _owner.getParty() != null;
	}

	/**
	 * Check if the active L2Skill can be casted.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the target is correct </li>
	 * <li>Check if the target is in the skill cast range </li>
	 * <li>Check if the summon owns enough HP and MP to cast the skill </li>
	 * <li>Check if all skills are enabled and this skill is enabled </li><BR><BR>
	 * <li>Check if the skill is active </li><BR><BR>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 * 
	 */
	public void useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if (skill == null || isDead())
			return;

		// Check if the skill is active
		if (skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			return;
		}

		//************************************* Check Casting in Progress *******************************************

		// If a skill is currently being used
		if (isCastingNow())
		{
			return;
		}

		//************************************* Check Target *******************************************

		// Get the target for the skill
		L2Character target = null;

		switch (skill.getTargetType())
		{
		// OWNER_PET should be cast even if no target has been found
		case TARGET_OWNER_PET:
			target = getOwner();
			break;
		// PARTY, AURA, SELF should be cast even if no target has been found
		case TARGET_PARTY:
		case TARGET_AURA:
		case TARGET_FRONT_AURA:
		case TARGET_BEHIND_AURA:
		case TARGET_SELF:
			target = this;
			break;
		default:
			// Get the first target of the list
			target = skill.getFirstOfTargetList(this);
			break;
		}

		// Check the validity of the target
		if (target == null)
		{
			if (getOwner() != null)
				getOwner().sendPacket(new SystemMessage(SystemMessageId.TARGET_CANT_FOUND));
			return;
		}

		//************************************* Check skill availability *******************************************

		// Check if this skill is enabled (e.g. reuse time)
		if (isSkillDisabled(skill.getId()))
		{
			if (getOwner() != null)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill);
				getOwner().sendPacket(sm);
			}
			return;
		}

		//************************************* Check Consumables *******************************************
		if (skill.getItemConsume() > 0 && getOwner().getInventory() != null)
		{
			L2ItemInstance requiredItems = getOwner().getInventory().getItemByItemId(skill.getItemConsumeId());
			// Check if the caster owns enough consumed Item to cast
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				// Send a System Message to the caster
				getOwner().sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
				return;
			}
		}

		// Check if the summon has enough MP
		if (getStatus().getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			if (getOwner() != null)
				getOwner().sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
			return;
		}

		// Check if the summon has enough HP
		if (getStatus().getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			if (getOwner() != null)
				getOwner().sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_HP));
			return;
		}

		//************************************* Check Summon State *******************************************

		// Check if this is offensive magic skill
		if (skill.isOffensive())
		{
			if (isInsidePeaceZone(this, target))
			{
				// If summon or target is in a peace zone, send a system message TARGET_IN_PEACEZONE
				sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				return;
			}

			if (getOwner() != null && getOwner().isInOlympiadMode() && !getOwner().isOlympiadStart())
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			// Check if the target is attackable
			if (target instanceof L2DoorInstance)
			{
				if (!((L2DoorInstance) target).isAttackable(getOwner()))
					return;
			}
			else
			{
				if (!target.isAttackable() && getOwner() != null && (getOwner().getAccessLevel() < Config.GM_PEACEATTACK))
				{
					return;
				}

				// Check if a Forced ATTACK is in progress on non-attackable target
				if (!target.isAutoAttackable(this) && !forceUse)
				{
					switch (skill.getTargetType())
					{
					case TARGET_AURA:
					case TARGET_FRONT_AURA:
					case TARGET_BEHIND_AURA:
					case TARGET_CLAN:
					case TARGET_ALLY:
					case TARGET_PARTY:
					case TARGET_SELF:
						break;
					default:
						return;
					}
				}
			}
		}
		getOwner().setCurrentPetSkill(skill, forceUse, dontMove);
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
	}

	@Override
	public void setIsImmobilized(boolean value)
	{
		super.setIsImmobilized(value);

		if (value)
		{
			_previousFollowStatus = getFollowStatus();
			// if immobilized temporarly disable follow mode
			if (_previousFollowStatus)
				setFollowStatus(false);
		}
		else
		{
			// if not more immobilized restore previous follow mode
			setFollowStatus(_previousFollowStatus);
		}
	}

	public void setOwner(L2PcInstance newOwner)
	{
		_owner = newOwner;
	}

	@Override
	public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss)
		{
			if (this instanceof L2SummonInstance)
				getOwner().sendMessage("The summoned monster has missed.");
			else
				getOwner().sendMessage("Your pet has missed.");
			target.sendAvoidMessage(this);
			return;
		}
		
		if (pcrit || mcrit)
		{
			if (this instanceof L2SummonInstance)
				getOwner().sendPacket(SystemMessageId.CRITICAL_HIT_BY_SUMMONED_MOB);
			else
				getOwner().sendPacket(SystemMessageId.CRITICAL_HIT_BY_PET);
		}
		
		if (target.getObjectId() != getOwner().getObjectId())
		{
			if (getOwner().isInOlympiadMode() && target instanceof L2PcInstance
				&& ((L2PcInstance)target).isInOlympiadMode()
				&& ((L2PcInstance)target).getOlympiadGameId() == getOwner().getOlympiadGameId())
			{
				Olympiad.getInstance().notifyCompetitorDamage(getOwner(), damage, getOwner().getOlympiadGameId());
			}
		}
		
		SystemMessage sm;
		if (this instanceof L2SummonInstance)
			sm = new SystemMessage(SystemMessageId.SUMMON_GAVE_DAMAGE_S1);
		else
			sm = new SystemMessage(SystemMessageId.PET_HIT_FOR_S1_DAMAGE);
		sm.addNumber(damage);
		getOwner().sendPacket(sm);
	}
	
	@Override
	public final void sendAvoidMessage(L2Character attacker)
	{
		if (this instanceof L2SummonInstance)
			getOwner().sendMessage("The summoned monster has avoided " + attacker.getName() + "'s attack.");
		else
			getOwner().sendMessage("Your pet has avoided " + attacker.getName() + "'s attack.");
	}
	
	public void reduceCurrentHp(int damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);

		if (isDOT)
			return;

		SystemMessage sm;
		if (this instanceof L2SummonInstance)
			sm = new SystemMessage(SystemMessageId.SUMMON_RECEIVED_DAMAGE_S2_BY_C1);
		else
			sm = new SystemMessage(SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_C1);

		sm.addCharName(attacker);
		sm.addNumber(damage);
		getOwner().sendPacket(sm);
	}

	@Override
	public final boolean isOutOfControl()
	{
		return isConfused() || isAfraid() || isBetrayed();
	}

	/**
	 * Servitors' skills automatically change their level based on the servitor's level.
	 * Until level 70, the servitor gets 1 lv of skill per 10 levels. After that, it is 1
	 * skill level per 5 servitor levels.  If the resulting skill level doesn't exist use
	 * the max that does exist!
	 * 
	 * @see com.l2jfree.gameserver.model.actor.L2Character#doCast(com.l2jfree.gameserver.model.L2Skill)
	 */
	@Override
	public void doCast(L2Skill skill)
	{
		final L2PcInstance actingPlayer = getActingPlayer();

		if (actingPlayer.isGM() && actingPlayer.getAccessLevel() < Config.GM_CAN_GIVE_DAMAGE)
		{
			actingPlayer.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			actingPlayer.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!actingPlayer.checkPvpSkill(getTarget(), skill))
		{
			// Send a System Message to the L2PcInstance
			actingPlayer.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));

			// Send a Server->Client packet ActionFailed to the L2PcInstance
			actingPlayer.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			int petLevel = getLevel();
			int skillLevel = petLevel / 10;
			if (petLevel >= 70)
				skillLevel += (petLevel - 65) / 10;

			// adjust the level for servitors less than lv 10
			if (skillLevel < 1)
				skillLevel = 1;

			L2Skill skillToCast = SkillTable.getInstance().getInfo(skill.getId(), skillLevel);
			if (skillToCast != null)
				super.doCast(skillToCast);
			else
				super.doCast(skill);
		}
	}

	@Override
	public final L2PcInstance getActingPlayer()
	{
		return getOwner();
	}

	@Override
	public final L2Summon getActingSummon()
	{
		return this;
	}
	
	@Override
	public boolean isInCombat()
	{
		return getOwner().isInCombat();
	}
	
	public int getWeapon()
	{
		return 0;
	}

	public int getArmor()
	{
		return 0;
	}

	public int getPetSpeed()
	{
		return getTemplate().getBaseRunSpd();
	}

	public boolean isHungry()
	{
		return false;
	}
	
	@Override
	public boolean isRunning()
	{
		return true; // summons always run
	}
	
	@Override
	public void broadcastFullInfoImpl()
	{
		broadcastFullInfoImpl(1);
	}
	
	public void broadcastFullInfoImpl(int val)
	{
		getOwner().sendPacket(new PetInfo(this, val));
		getOwner().sendPacket(new PetStatusUpdate(this));
		
		broadcastPacket(new AbstractNpcInfo.SummonInfo(this, val));
		
		final L2Party party = getOwner().getParty();
		
		if (party != null)
			party.broadcastToPartyMembers(getOwner(), new ExPartyPetWindowUpdate(this));
		
		updateEffectIcons();
	}
	
	@Override
	public SummonShots getShots()
	{
		if (_shots == null)
			_shots = new SummonShots(this);
		
		return (SummonShots)_shots;
	}
}
