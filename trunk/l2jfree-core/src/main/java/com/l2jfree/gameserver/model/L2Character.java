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

import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import static com.l2jfree.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.Shutdown;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.Shutdown.DisableType;
import com.l2jfree.gameserver.ai.CtrlEvent;
import com.l2jfree.gameserver.ai.CtrlIntention;
import com.l2jfree.gameserver.ai.L2AttackableAI;
import com.l2jfree.gameserver.ai.L2CharacterAI;
import com.l2jfree.gameserver.datatables.DoorTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.geodata.GeoData;
import com.l2jfree.gameserver.geodata.pathfinding.AbstractNodeLoc;
import com.l2jfree.gameserver.geodata.pathfinding.PathFinding;
import com.l2jfree.gameserver.handler.SkillHandler;
import com.l2jfree.gameserver.instancemanager.FactionManager;
import com.l2jfree.gameserver.instancemanager.MapRegionManager;
import com.l2jfree.gameserver.model.L2Skill.SkillTargetType;
import com.l2jfree.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jfree.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfree.gameserver.model.actor.instance.L2ControlTowerInstance;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2EffectPointInstance;
import com.l2jfree.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfree.gameserver.model.actor.instance.L2MinionInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcWalkerInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.model.actor.instance.L2RiftInvaderInstance;
import com.l2jfree.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import com.l2jfree.gameserver.model.actor.knownlist.CharKnownList;
import com.l2jfree.gameserver.model.actor.stat.CharStat;
import com.l2jfree.gameserver.model.actor.status.CharStatus;
import com.l2jfree.gameserver.model.itemcontainer.Inventory;
import com.l2jfree.gameserver.model.mapregion.TeleportWhereType;
import com.l2jfree.gameserver.model.quest.Quest;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.Disconnection;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.Attack;
import com.l2jfree.gameserver.network.serverpackets.ChangeMoveType;
import com.l2jfree.gameserver.network.serverpackets.ChangeWaitType;
import com.l2jfree.gameserver.network.serverpackets.FlyToLocation;
import com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillCanceled;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jfree.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jfree.gameserver.network.serverpackets.MoveToLocation;
import com.l2jfree.gameserver.network.serverpackets.Revive;
import com.l2jfree.gameserver.network.serverpackets.SetupGauge;
import com.l2jfree.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfree.gameserver.network.serverpackets.StopMove;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.TeleportToLocation;
import com.l2jfree.gameserver.network.serverpackets.ValidateLocation;
import com.l2jfree.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2jfree.gameserver.skills.Calculator;
import com.l2jfree.gameserver.skills.Formulas;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.skills.funcs.FuncOwner;
import com.l2jfree.gameserver.skills.l2skills.L2SkillAgathion;
import com.l2jfree.gameserver.skills.l2skills.L2SkillChargeDmg;
import com.l2jfree.gameserver.skills.l2skills.L2SkillMount;
import com.l2jfree.gameserver.skills.l2skills.L2SkillSummon;
import com.l2jfree.gameserver.taskmanager.PacketBroadcaster;
import com.l2jfree.gameserver.taskmanager.PacketBroadcaster.BroadcastMode;
import com.l2jfree.gameserver.templates.chars.L2CharTemplate;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.templates.item.L2Weapon;
import com.l2jfree.gameserver.templates.item.L2WeaponType;
import com.l2jfree.gameserver.templates.skills.L2EffectType;
import com.l2jfree.gameserver.templates.skills.L2SkillType;
import com.l2jfree.gameserver.util.Broadcast;
import com.l2jfree.gameserver.util.Util;
import com.l2jfree.lang.L2System;
import com.l2jfree.tools.geometry.Point3D;
import com.l2jfree.tools.random.Rnd;
import com.l2jfree.util.SingletonList;
import com.l2jfree.util.SingletonSet;

/**
 * Mother class of all character objects of the world (PC, NPC...)<BR>
 * <BR>
 * L2Character :<BR>
 * <BR>
 * <li>L2CastleGuardInstance</li>
 * <li>L2DoorInstance</li>
 * <li>L2NpcInstance</li>
 * <li>L2PlayableInstance </li>
 * <BR>
 * <BR>
 * <B><U> Concept of L2CharTemplate</U> :</B><BR>
 * <BR>
 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different
 * template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of L2Character is
 * spawned, server just create a link between the instance and the template. This link is stored in <B>_template</B><BR>
 * <BR>
 *
 * @version $Revision: 1.53.2.45.2.34 $ $Date: 2005/04/11 10:06:08 $
 */
public abstract class L2Character extends L2Object
{
	public final static Log		_log								= LogFactory.getLog(L2Character.class.getName());

	// =========================================================
	// Data Field
	private List<L2Character>		_attackByList;
	private L2Character				_attackingChar;
	private volatile boolean		_isCastingNow						= false;
	private volatile boolean		_isCastingSimultaneouslyNow			= false;
	private L2Skill					_lastSimultaneousSkillCast;
	private boolean					_block_buffs						= false;
	private boolean					_isAfraid							= false;											// Flee in a random direction
	private boolean					_isConfused							= false;											// Attack anyone randomly
	private boolean					_isFakeDeath						= false;											// Fake death
	private boolean					_isFallsdown						= false;											// Falls down [L2J_JP_ADD]
	private boolean					_isMuted							= false;											// Cannot use magic
	private boolean					_isPhysicalMuted					= false;											// Cannot use physical attack
	private boolean					_isPhysicalAttackMuted				= false;											// Cannot use attack
	private boolean					_isDead								= false;
	private boolean					_isImmobilized						= false;
	private boolean					_isOverloaded						= false;											// the char is carrying too much
	private boolean					_isParalyzed						= false;											// cannot do anything
	private boolean					_isPetrified						= false;											// cannot receive dmg from hits.

	private boolean					_isPendingRevive					= false;
	private boolean					_isRooted							= false;											// Cannot move until root timed out
	private boolean					_isRunning							= true;
	private boolean					_isImmobileUntilAttacked			= false;
	private boolean					_isSleeping							= false;											// Cannot move/attack until sleep
	// timed out or monster is attacked
	private boolean					_isBlessedByNoblesse				= false;
	private boolean					_isLuckByNoblesse					= false;
	private boolean					_isBetrayed							= false;
	private boolean					_isStunned							= false;											// Cannot move/attack until stun
	// timed out
	protected boolean				_isTeleporting						= false;
	protected boolean				_isInvul							= false;
	protected boolean				_isDisarmed							= false;
	protected boolean				_isMarked							= false;
	private int						_lastHealAmount						= 0;
	private int[]					lastPosition						=
																		{ 0, 0, 0 };
	protected CharStat				_stat;
	protected CharStatus			_status;
	private L2CharTemplate			_template;																				// The link on the L2CharTemplate
	protected boolean				_showSummonAnimation				= false;
	// object containing generic and
	// static properties of this
	// L2Character type (ex : Max HP,
	// Speed...)
	private String					_title;
	private boolean					_champion							= false;
	private double					_hpUpdateIncCheck					= .0;
	private double					_hpUpdateDecCheck					= .0;
	private double					_hpUpdateInterval					= .0;

	/** Table of Calculators containing all used calculator */
	private Calculator[]			_calculators;

	/** FastMap(Integer, L2Skill) containing all skills of the L2Character */
	protected Map<Integer, L2Skill>	_skills;
	protected ChanceSkillList		_chanceSkills;
	/** Current force buff this caster is casting to a target */
	protected ForceBuff				_forceBuff;

	protected byte					_zoneValidateCounter				= 4;

	private boolean					_isRaid								= false;

	/**
	 * Objects known by this object
	 */
	protected CharKnownList _knownList;
	
	// =========================================================
	// Constructor
	/**
	 * Constructor of L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different
	 * template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of L2Character is
	 * spawned, server just create a link between the instance and the template This link is stored in <B>_template</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the _template of the L2Character </li>
	 * <li>Set _overloaded to false (the charcater can take more items)</li>
	 * <BR>
	 * <BR>
	 * <li>If L2Character is a L2NPCInstance, copy skills from template to object</li>
	 * <li>If L2Character is a L2NPCInstance, link _calculators to NPC_STD_CALCULATOR</li>
	 * <BR>
	 * <BR>
	 * <li>If L2Character is NOT a L2NPCInstance, create an empty _skills slot</li>
	 * <li>If L2Character is a L2PcInstance or L2Summon, copy basic Calculator set to object</li>
	 * <BR>
	 * <BR>
	 *
	 * @param objectId
	 *            Identifier of the object to initialized
	 * @param template
	 *            The L2CharTemplate to apply to the object
	 */
	public L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		getKnownList();

		// Set its template to the new L2Character
		_template = template;

		if (template != null && this instanceof L2NpcInstance)
		{
			// Copy the Standard Calcultors of the L2NPCInstance in _calculators
			if (this instanceof L2DoorInstance)
				_calculators = Formulas.getStdDoorCalculators();
			else
				_calculators = NPC_STD_CALCULATOR;

			// Copy the skills of the L2NPCInstance from its template to the L2Character Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			_skills = ((L2NpcTemplate) template).getSkills();
			if (_skills != null)
			{
				for (Map.Entry<Integer, L2Skill> skill : _skills.entrySet())
					addStatFuncs(skill.getValue().getStatFuncs(null, this));
			}
		}
		else
		{
			// Initialize the FastMap _skills to null
			_skills = new FastMap<Integer, L2Skill>().setShared(true);

			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			_calculators = new Calculator[Stats.NUM_STATS];
			Formulas.addFuncsToNewCharacter(this);
		}

		if (!(this instanceof L2PlayableInstance) && !(this instanceof L2Attackable) && !(this instanceof L2ControlTowerInstance)
				&& !(this instanceof L2DoorInstance) && !(this instanceof L2Trap) && !(this instanceof L2SiegeFlagInstance) && !(this instanceof L2Decoy)
				&& !(this instanceof L2EffectPointInstance) && !(this instanceof L2FolkInstance))
			setIsInvul(true);
	}

	private byte[]	_currentZones	= new byte[17];

	public boolean isInsideZone(byte zone)
	{
		return (zone == L2Zone.FLAG_PVP) ? (_currentZones[L2Zone.FLAG_PVP] > 0 && _currentZones[L2Zone.FLAG_PEACE] == 0) : (_currentZones[zone] > 0);
	}

	public void setInsideZone(int zone, boolean state)
	{
		if (state)
			_currentZones[zone]++;
		else if (_currentZones[zone] > 0)
			_currentZones[zone]--;
	}

	/**
	 * Returns character inventory, default null, overridden in L2Playable types and in L2NPcInstance
	 */
	public Inventory getInventory()
	{
		return null;
	}

	/**
	 * @param process
	 * @param itemId
	 * @param count
	 * @param reference
	 * @param sendMessage
	 */
	public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference, boolean sendMessage)
	{
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true;
	}

	/**
	 * @param process
	 * @param objectId
	 * @param count
	 * @param reference
	 * @param sendMessage
	 */
	public boolean destroyItem(String process, int objectId, int count, L2Object reference, boolean sendMessage)
	{
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true;
	}

	protected void initCharStatusUpdateValues()
	{
		_hpUpdateInterval = getMaxHp() / 352.0; // MAX_HP div MAX_HP_BAR_PX
		_hpUpdateIncCheck = getMaxHp();
		_hpUpdateDecCheck = getMaxHp() - _hpUpdateInterval;
	}

	// =========================================================
	// Event - Public
	/**
	 * Remove the L2Character from the world when the decay task is launched.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR>
	 * <BR>
	 */
	public void onDecay()
	{
		L2WorldRegion reg = getWorldRegion();
		decayMe();
		if (reg != null)
			reg.removeFromZones(this);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		// Force a revalidation
		revalidateZone(true);
	}

	public void onTeleported()
	{
		if (!isTeleporting())
			return;

		if (this instanceof L2Summon)
		{
			((L2Summon)this).getOwner().sendPacket(new TeleportToLocation(this, getPosition().getX(), getPosition().getY(), getPosition().getZ()));
		}

		setIsTeleporting(false);
		spawnMe(getPosition().getX(), getPosition().getY(), getPosition().getZ());
		if (_isPendingRevive)
			doRevive();
	}

	// =========================================================
	// Method - Public
	/**
	 * Add L2Character instance that is attacking to the attacker list.<BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2Character that attcks this one
	 */
	public void addAttackerToAttackByList(L2Character player)
	{
		if (player == null || player == this || getAttackByList() == null || getAttackByList().contains(player))
			return;
		getAttackByList().add(player);
	}

	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the _knownPlayers of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>. In order to inform other players of state modification on
	 * the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 */
	public final void broadcastPacket(L2GameServerPacket mov)
	{
		Broadcast.toSelfAndKnownPlayers(this, mov);
	}

	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the radius (max knownlist radius) from the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>. In order to inform other players of state modification on
	 * the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR>
	 * <BR>
	 */
	public final void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		Broadcast.toSelfAndKnownPlayersInRadius(this, mov, radiusInKnownlist);
	}

	/**
	 * Returns true if hp update should be done, false if not
	 *
	 * @return boolean
	 */
	protected boolean needHpUpdate(int barPixels)
	{
		double currentHp = getStatus().getCurrentHp();

		if (currentHp <= 1.0 || getMaxHp() < barPixels)
			return true;

		if (currentHp <= _hpUpdateDecCheck || currentHp >= _hpUpdateIncCheck)
		{
			if (currentHp == getMaxHp())
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;

				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}
			return true;
		}

		return false;
	}

	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all L2Character called _statusListener that must be informed of HP/MP updates
	 * of this L2Character </li>
	 * <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance : Send current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party</li>
	 * <BR>
	 * <BR>
	 */
	public final void broadcastStatusUpdate()
	{
		addPacketBroadcastMask(BroadcastMode.BROADCAST_STATUS_UPDATE);
	}
	
	public void broadcastStatusUpdateImpl()
	{
		if (getStatus().getStatusListeners().isEmpty() || !needHpUpdate(352))
			return;
		
		StatusUpdate su = new StatusUpdate(getObjectId());
		su.addAttribute(StatusUpdate.CUR_HP, (int)getStatus().getCurrentHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int)getStatus().getCurrentMp());
		
		synchronized (getStatus().getStatusListeners())
		{
			for (L2PcInstance player : getStatus().getStatusListeners())
				player.sendPacket(su);
		}
	}

	/**
	 * Not Implemented.<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance</li>
	 * <BR>
	 * <BR>
	 * @param gsp
	 */
	public void sendPacket(L2GameServerPacket gsp)
	{
	}

	/**
	 * @param sm
	 */
	public void sendPacket(SystemMessageId sm)
	{
	}

	/**
	 * Teleport a L2Character and its pet if necessary.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Stop the movement of the L2Character</li>
	 * <li>Set the x,y,z position of the L2Object and if necessary modify its _worldRegion</li>
	 * <li>Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in its _knownPlayers</li>
	 * <li>Modify the position of the pet if necessary</li>
	 * <BR>
	 * <BR>
	 */
	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		// Restrict teleport during restart/shutdown
		if (Shutdown.isActionDisabled(DisableType.TELEPORT))
		{
			sendMessage("Teleport is not allowed during restart/shutdown.");
			return;
		}

		// Stop movement
		setTarget(this);
		abortAttack();
		abortCast();
		isFalling(false, 0);
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		setIsTeleporting(true);

		if (Config.RESPAWN_RANDOM_ENABLED && allowRandomOffset)
		{
			x += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
			y += Rnd.get(-Config.RESPAWN_RANDOM_MAX_OFFSET, Config.RESPAWN_RANDOM_MAX_OFFSET);
		}

		z += 5;

		if (_log.isDebugEnabled())
			_log.debug("Teleporting to: " + x + ", " + y + ", " + z);

		// remove the object from its old location
		decayMe();

		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the _knownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z));

		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		getPosition().setXYZ(x, y, z);
		isFalling(false, 0);

		if (this instanceof L2PcInstance)
		{
		}
		else
			onTeleported();
	}

	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, true);
	}

	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();

		teleToLocation(x, y, z, allowRandomOffset);
	}

	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this, teleportWhere), true);
	}

	/** ************************************-+ Fall Damage +-************************************** */

	/**
	 * @author Darki699 Calculates if a L2Character is falling or not. If the character falls, it returns the fall height.
	 * @param  falling: if false no checks are made, but last position is set to the current one
	 * @param  fallHeight: an integer value of the fall already calculated before.
	 * @return A positive integer of the fall height, if not falling returns -1
	 */
	public int isFalling(boolean falling, int fallHeight)
	{

		if (isFallsdown() && fallHeight == 0) // Avoid double checks -> let him fall only 1 time =P
			return -1;

		// If the boolean falling is set to false, just initialize this fall
		if (!falling || (lastPosition[0] == 0 && lastPosition[1] == 0 && lastPosition[2] == 0))
		{
			lastPosition[0] = getPosition().getX();
			lastPosition[1] = getPosition().getY();
			lastPosition[2] = getPosition().getZ();
			setIsFallsdown(false);
			return -1;
		}

		int moveChangeX = Math.abs(lastPosition[0] - getPosition().getX()), moveChangeY = Math.abs(lastPosition[1] - getPosition().getY()),
		// Z has a Positive value ONLY if the L2Character is moving down!
		moveChangeZ = Math.max(lastPosition[2] - getPosition().getZ(), lastPosition[2] - getZ());

		// Add acumulated damage to this fall, calling this function at a short delay while the fall is in progress
		if (moveChangeZ > fallSafeHeight() && moveChangeY < moveChangeZ && moveChangeX < moveChangeZ && !isFlying())
		{

			setIsFallsdown(true);
			// Calculate the acumulated fall height for a total fall calculation
			fallHeight += moveChangeZ;

			// set the last position to the current one for the next future calculation
			lastPosition[0] = getPosition().getX();
			lastPosition[1] = getPosition().getY();
			lastPosition[2] = getPosition().getZ();
			getPosition().setXYZ(lastPosition[0], lastPosition[1], lastPosition[2]);

			// Call this function for further checks in the short future (next time we either keep falling, or finalize the fall)
			// This "next time" check is a rough estimate on how much time is needed to calculate the next check, and it is based on the current fall height.
			CheckFalling cf = new CheckFalling(fallHeight);
			Future<?> task = ThreadPoolManager.getInstance().scheduleGeneral(cf, Math.min(1200, moveChangeZ));
			cf.setTask(task);

			// Value returned but not currently used. Maybe useful for future features.
			return fallHeight;
		}

		// Stopped falling or is not falling at all.
		lastPosition[0] = getPosition().getX();
		lastPosition[1] = getPosition().getY();
		lastPosition[2] = getPosition().getZ();
		getPosition().setXYZ(lastPosition[0], lastPosition[1], lastPosition[2]);

		if (fallHeight > fallSafeHeight())
		{
			doFallDamage(fallHeight);
			return fallHeight;
		}

		return -1;
	}

	/**
	 * <font color="ff0000"><b>Needs to be completed!</b></font> Add to safeFallHeight the buff resist values which increase the fall resistance.
	 *
	 * @author Darki699
	 * @return integer safeFallHeight is the value from which above it this L2Character suffers a fall damage.
	 */
	private int fallSafeHeight()
	{

		int safeFallHeight = Config.ALT_MINIMUM_FALL_HEIGHT;

		try
		{
			if (this instanceof L2PcInstance)
			{
				safeFallHeight = ((L2PcInstance) this).getTemplate().getBaseFallSafeHeight(((L2PcInstance) this).getAppearance().getSex());
			}
		}

		catch (Exception e)
		{
			_log.fatal(e.getMessage(), e);
		}

		return safeFallHeight;
	}

	private int getFallDamage(int fallHeight)
	{
		int damage = (fallHeight - fallSafeHeight()) * 2; // Needs verification for actual damage
		damage = (int) (damage / getStat().calcStat(Stats.FALL_VULN, 1, this, null));

		if (damage >= getStatus().getCurrentHp())
		{
			damage = (int) (getStatus().getCurrentHp() - 1);
		}

		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
		disableAllSkills();

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			public void run()
			{
				L2Character.this.enableAllSkills();
				broadcastPacket(new ChangeWaitType(L2Character.this, ChangeWaitType.WT_STOP_FAKEDEATH));
				setIsFallsdown(false);

				// For some reason this is needed since the client side changes back to last airborn position after 1 second
				lastPosition[0] = getPosition().getX();
				lastPosition[1] = getPosition().getY();
				lastPosition[2] = getPosition().getZ();
			}
		}, 1100);

		return damage;
	}

	/**
	 * Receives a integer fallHeight and finalizes the damage effect from the fall.
	 *
	 * @author Darki699
	 */
	private void doFallDamage(int fallHeight)
	{
		isFalling(false, 0);

		if (isInvul() || (this instanceof L2PcInstance && isInFunEvent()))
		{
			setIsFallsdown(false);
			return;
		}

		int damage = getFallDamage(fallHeight);

		if (damage < 1)
			return;

		if (this instanceof L2PcInstance)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.FALL_DAMAGE_S1);
			sm.addNumber(damage);
			sendPacket(sm);
		}

		getStatus().reduceHp(damage, this);
		getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
	}

	/**
	 * @author Darki699 Once a character is falling, we call this to run in order to see when he is not falling down any more. Constructor receives the int
	 *         fallHeight already calculated, and function isFalling(boolean,int) will be called again to terminate the fall and calculate the damage.
	 */
	public class CheckFalling implements Runnable
	{
		private int			_fallHeight;
		private Future<?>	_task;

		public CheckFalling(int fallHeight)
		{
			_fallHeight = fallHeight;
		}

		public void setTask(Future<?> task)
		{
			_task = task;
		}

		public void run()
		{
			if (_task != null)
			{
				_task.cancel(true);
				_task = null;
			}

			try
			{
				isFalling(true, _fallHeight);
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}
	}

	// =========================================================
	// Method - Private
	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get the active weapon (always equipped in the right hand) </li>
	 * <BR>
	 * <BR>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the L2PcInstance with arrows in left hand)</li>
	 * <li>If weapon is a bow, consume MP and set the new period of bow non re-use </li>
	 * <BR>
	 * <BR>
	 * <li>Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack) </li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the L2Character AND to all L2PcInstance in the
	 * _knownPlayers of the L2Character</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li>
	 * <BR>
	 * <BR>
	 *
	 * @param target
	 *            The L2Character targeted
	 */
	protected void doAttack(L2Character target)
	{
		if (_log.isDebugEnabled())
			_log.debug(getName() + " doAttack: target=" + target);

		if (isAlikeDead() || target == null || (this instanceof L2NpcInstance && target.isAlikeDead())
				|| (this instanceof L2PcInstance && target.isDead() && !target.isFakeDeath()))
		{
			// If L2PcInstance is dead or the target is dead, the action is stoped
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (isAttackingDisabled())
			return;

		// GeoData Los Check here (or dz > 1000)
		if (!(target instanceof L2DoorInstance) && !GeoData.getInstance().canSeeTarget(this, target))
		{
			sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		boolean transformed = false;

		if (this instanceof L2PcInstance)
		{
			if (((L2PcInstance)this).isMounted() && ((L2PcInstance)this).getMountNpcId() == 12621)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (((L2PcInstance) this).inObserverMode())
			{
				sendPacket(new SystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			if (target instanceof L2PcInstance)
			{
				if (((L2PcInstance) target).isCursedWeaponEquipped() && getLevel() <= 20)
				{
					sendMessage("Can't attack a cursed player when under level 21.");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (((L2PcInstance) this).isCursedWeaponEquipped() && target.getLevel() <= 20)
				{
					sendMessage("Can't attack a newbie player using a cursed weapon.");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}

				if (getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL)
				{
					sendMessage("Your level is too low to participate in player vs player combat until level "
							+ String.valueOf(Config.ALT_PLAYER_PROTECTION_LEVEL) + ".");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				if (target.getLevel() < Config.ALT_PLAYER_PROTECTION_LEVEL)
				{
					sendMessage("Player under newbie protection until level " + String.valueOf(Config.ALT_PLAYER_PROTECTION_LEVEL) + ".");
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				// Checking if target has moved to peace zone
				if (target.isInsidePeaceZone((L2PcInstance) this))
				{
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (isInsidePeaceZone(this, target))
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}

			transformed = ((L2PcInstance) this).isTransformed();
		}

		// Get the active weapon instance (always equipped in the right hand)
		L2ItemInstance weaponInst = getActiveWeaponInstance();

		// TODO: unhardcode this to support boolean if with that weapon u can attack or not (for ex transform weapons)
		if (weaponInst != null && weaponInst.getItemId() == 9819)
		{
			sendPacket(new SystemMessage(SystemMessageId.THAT_WEAPON_CANT_ATTACK));
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Get the active weapon item corresponding to the active weapon instance (always equipped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();

		if ((weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD))
		{
			// You can't make an attack with a fishing pole.
			sendPacket(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// BOW and CROSSBOW checks
		if (weaponItem != null && !transformed)
		{
			if (weaponItem.getItemType() == L2WeaponType.BOW)
			{
				//Check for arrows and MP
				if (this instanceof L2PcInstance)
				{
					// Verify if the bow can be use
					if (_disableBowAttackEndTime <= GameTimeController.getGameTicks())
					{
						// Verify if L2PcInstance owns enough MP
						int saMpConsume = (int) getStat().calcStat(Stats.MP_CONSUME, 0, null, null);
						int mpConsume = saMpConsume == 0 ? weaponItem.getMpConsume() : saMpConsume;

						if (getStatus().getCurrentMp() < mpConsume)
						{
							// If L2PcInstance doesn't have enough MP, stop the attack
							ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));
							sendPacket(ActionFailed.STATIC_PACKET);
							return;
						}
						// If L2PcInstance have enough MP, the bow consumes it
						getStatus().reduceMp(mpConsume);

						// Set the period of bow no re-use
						_disableBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
					}
					else
					{
						// Cancel the action because the bow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);

						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
					if (!checkAndEquipArrows())
					{
						// Cancel the action because the L2PcInstance have no arrow
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS));
						return;
					}
				}
				else if (this instanceof L2NpcInstance)
				{
					if (_disableBowAttackEndTime > GameTimeController.getGameTicks())
						return;
				}
			}
			else if (weaponItem.getItemType() == L2WeaponType.CROSSBOW)
			{
				//Check for bolts
				if (this instanceof L2PcInstance)
				{
					// Checking if target has moved to peace zone - only for player-crossbow attacks at the moment
					// Other melee is checked in movement code and for offensive spells a check is done every time
					if (target.isInsidePeaceZone((L2PcInstance) this))
					{
						getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}

					// Verify if the crossbow can be use
					if (_disableCrossBowAttackEndTime <= GameTimeController.getGameTicks())
					{
						// Set the period of crossbow no re-use
						_disableCrossBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getGameTicks();
					}
					else
					{
						// Cancel the action because the crossbow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}

					// Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
					if (!checkAndEquipBolts())
					{
						// Cancel the action because the L2PcInstance have no arrow
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendPacket(ActionFailed.STATIC_PACKET);
						sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_BOLTS));
						return;
					}
				}
				else if (this instanceof L2NpcInstance)
				{
					if (_disableCrossBowAttackEndTime > GameTimeController.getGameTicks())
						return;
				}
			}
		}

		// Add the L2PcInstance to _knownObjects and _knownPlayer of the target
		target.getKnownList().addKnownObject(this);

		// Reduce the current CP if TIREDNESS configuration is activated
		if (Config.ALT_GAME_TIREDNESS)
			getStatus().setCurrentCp(getStatus().getCurrentCp() - 10);

		// Recharge any active auto soulshot tasks for player (or player's summon if one exists).
		if (this instanceof L2PcInstance)
			((L2PcInstance) this).rechargeAutoSoulShot(true, false, false);
		else if (this instanceof L2Summon)
			((L2Summon) this).getOwner().rechargeAutoSoulShot(true, false, true);

		// Verify if soulshots are charged.
		boolean wasSSCharged;

		if (this instanceof L2NpcInstance)
			wasSSCharged = ((L2NpcInstance) this).rechargeAutoSoulShot(true, false);
		else if (this instanceof L2Summon && !(this instanceof L2PetInstance))
			wasSSCharged = (((L2Summon) this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE);
		else
			wasSSCharged = (weaponInst != null && weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE);
		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		// the hit is calculated to happen halfway to the animation - might need further tuning e.g. in bow, dual case
		int timeToHit = timeAtk / 2;
		// Get the Attack Reuse Delay of the L2Weapon
		int reuse = calculateReuseTime(target, weaponItem);
		
		_attackEndTime = L2System.milliTime() + timeAtk;
		
		int ssGrade = 0;

		if (weaponItem != null)
		{
			ssGrade = weaponItem.getCrystalType();
			if (ssGrade == 6)
				ssGrade = 5;
		}

		// Create a Server->Client packet Attack
		Attack attack = new Attack(this, wasSSCharged, ssGrade);

		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		// Make sure that char is facing selected target
		// also works: setHeading(Util.convertDegreeToClientHeading(Util.calculateAngleFrom(this, target)));
		setHeading(Util.calculateHeadingFrom(this, target));

		boolean hitted;
		// Select the type of attack to start
		if (weaponItem == null || transformed)
			hitted = doAttackHitSimple(attack, target, timeToHit);

		else if (weaponItem.getItemType() == L2WeaponType.BOW)
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);

		else if (weaponItem.getItemType() == L2WeaponType.CROSSBOW)
			hitted = doAttackHitByCrossBow(attack, target, timeAtk, reuse);

		else if (weaponItem.getItemType() == L2WeaponType.POLE)
			hitted = doAttackHitByPole(attack, target, timeToHit);

		else if (isUsingDualWeapon())
			hitted = doAttackHitByDual(attack, target, timeToHit);

		else
			hitted = doAttackHitSimple(attack, target, timeToHit);

		// Flag the attacker if it's a L2PcInstance outside a PvP area
		L2PcInstance player = getActingPlayer();

		if (player != null && player.getPet() != target)
		{
			player.updatePvPStatus(target);
		}

		// Check if hit isn't missed
		if (!hitted)
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();
		else
		{
			/*
			 * ADDED BY nexus - 2006-08-17
			 *
			 * As soon as we know that our hit landed, we must discharge any active soulshots. This must be done so to avoid unwanted soulshot consumption.
			 */

			// If we didn't miss the hit, discharge the shoulshots, if any
			if (this instanceof L2Summon && !(this instanceof L2PetInstance))
				((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			else if (weaponInst != null)
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			if (player != null)
			{
				if (player.isCursedWeaponEquipped())
				{
					if (!target.isInvul())
						target.getStatus().setCurrentCp(0);
				}
				else if (player.isHero())
				{
					if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
						target.getStatus().setCurrentCp(0); // If Zariche is hitted by a Hero, Cp is reduced to 0
				}
			}
		}

		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the _knownPlayers of the L2Character
		if (attack.hasHits())
			broadcastPacket(attack);

		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
	}

	/**
	 * Launch a Bow attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>Consume arrows </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack </li>
	 * <BR>
	 * <BR>
	 *
	 * @param attack
	 *            Server->Client packet Attack in which the hit will be added
	 * @param target
	 *            The L2Character targeted
	 * @param sAtk
	 *            The Attack Speed of the attacker
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		if (miss1)
			sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET)); // msg miss the target

		// Consume arrows
		reduceArrowCount(false);

		_move = null;

		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(this, target, getStat().getCriticalHit(target, null));

			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
		}

		// Check if the L2Character is a L2PcInstance
		if (this instanceof L2PcInstance)
		{
			// Send a system message
			sendPacket(new SystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));

			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
		}

		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);

		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = (sAtk + reuse) / GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks();

		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);

		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	* Launch a CrossBow attack.<BR><BR>
	*
	* <B><U> Actions</U> :</B><BR><BR>
	* <li>Calculate if hit is missed or not </li>
	* <li>Consume bolts </li>
	* <li>If hit isn't missed, calculate if shield defense is efficient </li>
	* <li>If hit isn't missed, calculate if hit is critical </li>
	* <li>If hit isn't missed, calculate physical damages </li>
	* <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge </li>
	* <li>Create a new hit task with Medium priority</li>
	* <li>Calculate and set the disable delay of the crossbow in function of the Attack Speed</li>
	* <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	*
	* @param attack Server->Client packet Attack in which the hit will be added
	* @param target The L2Character targeted
	* @param sAtk The Attack Speed of the attacker
	*
	* @return True if the hit isn't missed
	*
	*/
	private boolean doAttackHitByCrossBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);

		// Consume bows
		reduceArrowCount(true);

		_move = null;

		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(getStat().getCriticalHit(target, null));

			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);
		}

		// Check if the L2Character is a L2PcInstance
		if (this instanceof L2PcInstance)
		{
			// Send a system message
			sendPacket(new SystemMessage(SystemMessageId.CROSSBOW_PREPARING_TO_FIRE));

			// Send a Server->Client packet SetupGauge
			SetupGauge sg = new SetupGauge(SetupGauge.RED, sAtk + reuse);
			sendPacket(sg);
		}

		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);

		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableCrossBowAttackEndTime = (sAtk + reuse) / GameTimeController.MILLIS_IN_TICK + GameTimeController.getGameTicks();

		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);

		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	 * Launch a Dual attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hits are missed or not </li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient </li>
	 * <li>If hits aren't missed, calculate if hit is critical </li>
	 * <li>If hits aren't missed, calculate physical damages </li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack </li>
	 * <BR>
	 * <BR>
	 *
	 * @param attack
	 *            Server->Client packet Attack in which the hit will be added
	 * @param target
	 *            The L2Character targeted
	 * @return True if hit 1 or hit 2 isn't missed
	 */
	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;

		// Calculate if hits are missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		if (miss1)
			sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET)); // msg miss
		if (miss2)
			sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET)); // msg miss

		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.calcShldUse(this, target);

			// Calculate if hit 1 is critical
			crit1 = Formulas.calcCrit(this, target, getStat().getCriticalHit(target, null));

			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, true, attack.soulshot);
			damage1 /= 2;
		}

		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.calcShldUse(this, target);

			// Calculate if hit 2 is critical
			crit2 = Formulas.calcCrit(this, target, getStat().getCriticalHit(target, null));

			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, true, attack.soulshot);
			damage2 /= 2;
		}

		// Create a new hit task with Medium priority for hit 1
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk / 2);

		// Create a new hit task with Medium priority for hit 2 with a higher delay
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack.soulshot, shld2), sAtk);

		// Add those hits to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);

		// Return true if hit 1 or hit 2 isn't missed
		return (!miss1 || !miss2);
	}

	/**
	 * Launch a Pole attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get all visible objects in a spherical area near the L2Character to obtain possible targets </li>
	 * <li>If possible target is the L2Character targeted, launch a simple attack against it </li>
	 * <li>If possible target isn't the L2Character targeted but is attackable, launch a simple attack against it </li>
	 * <BR>
	 * <BR>
	 *
	 * @param attack
	 *            Server->Client packet Attack in which the hit will be added
	 * @return True if one hit isn't missed
	 */
	private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		double angleChar;
		int maxRadius = getPhysicalAttackRange();
		int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);

		if (_log.isDebugEnabled())
		{
			_log.debug("doAttackHitByPole: Max radius = " + maxRadius);
			_log.debug("doAttackHitByPole: Max angle = " + maxAngleDiff);
		}

		// o1 x: 83420 y: 148158 (Giran)
		// o2 x: 83379 y: 148081 (Giran)
		// dx = -41
		// dy = -77
		// distance between o1 and o2 = 87.24
		// arctan2 = -120 (240) degree (excel arctan2(dx, dy); java arctan2(dy, dx))
		//
		// o2
		//
		// o1 ----- (heading)
		// In the diagram above:
		// o1 has a heading of 0/360 degree from horizontal (facing East)
		// Degree of o2 in respect to o1 = -120 (240) degree
		//
		// o2 / (heading)
		// /
		// o1
		// In the diagram above
		// o1 has a heading of -80 (280) degree from horizontal (facing north east)
		// Degree of o2 in respect to 01 = -40 (320) degree

		// Get char's heading degree
		angleChar = Util.convertHeadingToDegree(getHeading());
		int attackRandomCountMax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 3, null, null) - 1;
		int attackcount = 0;

		if (angleChar <= 0)
			angleChar += 360;
		// ===========================================================

		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		L2Character temp;
		for (L2Object obj : getKnownList().getKnownObjects().values())
		{
			if (obj == target)
				continue; // do not hit twice

			// Check if the L2Object is a L2Character
			if (obj instanceof L2Character)
			{
				if (obj instanceof L2PetInstance && this instanceof L2PcInstance && ((L2PetInstance) obj).getOwner() == this)
					continue;

				if (!Util.checkIfInRange(maxRadius, this, obj, false))
					continue;
				if (!GeoData.getInstance().canSeeTarget(this, obj))
					continue;

				// otherwise hit too high/low. 650 because mob z coord sometimes wrong on hills
				if (Math.abs(obj.getZ() - getZ()) > 650)
					continue;
				if (!isFacing(obj, maxAngleDiff))
					continue;

				temp = (L2Character) obj;

				// Launch a simple attack against the L2Character targeted
				if (!temp.isAlikeDead())
				{
					attackcount += 1;
					if (attackcount <= attackRandomCountMax)
					{
						if (temp == getAI().getAttackTarget() || temp.isAutoAttackable(this))
						{

							hitted |= doAttackHitSimple(attack, temp, attackpercent, sAtk);
							attackpercent /= 1.15;
						}
					}
				}
			}
		}

		// Return true if one hit isn't missed
		return hitted;
	}

	/**
	 * Launch a simple attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack </li>
	 * <BR>
	 * <BR>
	 *
	 * @param attack
	 *            Server->Client packet Attack in which the hit will be added
	 * @param target
	 *            The L2Character targeted
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}

	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = Formulas.calcHitMiss(this, target);
		if (miss1)
			sendPacket(new SystemMessage(SystemMessageId.MISSED_TARGET)); // msg miss

		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(this, target, getStat().getCriticalHit(target, null));

			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack.soulshot);

			if (attackpercent != 100)
				damage1 = (int) (damage1 * attackpercent / 100);
		}

		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack.soulshot, shld1), sAtk);

		// Add this hit to the Server-Client packet Attack
		attack.addHit(target, damage1, miss1, crit1, shld1);

		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted... </li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send a Server->Client packet MagicSkillUse (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message </li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li>
	 * <BR>
	 * <BR>
	 *
	 * @param skill
	 *            The L2Skill to use
	 */
	public void doCast(L2Skill skill)
	{
		beginCast(skill, false);
	}

	public void doSimultaneousCast(L2Skill skill)
	{
		beginCast(skill, true);
	}

	private void beginCast(L2Skill skill, boolean simultaneously)
	{
		if (!checkDoCastConditions(skill))
		{
			if (simultaneously)
				setIsCastingSimultaneouslyNow(false);
			else
				setIsCastingNow(false);
			if (this instanceof L2PcInstance)
				getAI().setIntention(AI_INTENTION_ACTIVE);
			return;
		}

		// Recharge AutoSoulShot
		if (skill.useSoulShot())
		{
			if (this instanceof L2NpcInstance)
				((L2NpcInstance) this).rechargeAutoSoulShot(true, false);
			else if (this instanceof L2PcInstance)
				((L2PcInstance) this).rechargeAutoSoulShot(true, false, false);
			else if (this instanceof L2Summon)
				((L2Summon) this).getOwner().rechargeAutoSoulShot(true, false, true);
		}
		else if (skill.useSpiritShot())
		{
			if (this instanceof L2PcInstance)
				((L2PcInstance) this).rechargeAutoSoulShot(false, true, false);
			else if (this instanceof L2Summon)
				((L2Summon) this).getOwner().rechargeAutoSoulShot(false, true, true);
		}

		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = null;

		// Get all possible targets of the skill in a table in function of the skill target type
		L2Character[] targets = skill.getTargetList(this);

		// AURA skills should always be using caster as target
		switch (skill.getTargetType())
		{
		case TARGET_AURA:
		case TARGET_FRONT_AURA:
		case TARGET_BEHIND_AURA:
		case TARGET_GROUND:
		{
			target = this;
			break;
		}
		default:
		{
			if (targets == null || targets.length == 0)
			{
				if (simultaneously)
					setIsCastingSimultaneouslyNow(false);
				else
					setIsCastingNow(false);
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				if (this instanceof L2PcInstance)
				{
					sendPacket(ActionFailed.STATIC_PACKET);
					getAI().setIntention(AI_INTENTION_ACTIVE);
				}
				return;
			}

			switch (skill.getSkillType())
			{
			case BUFF:
			case HEAL:
			case COMBATPOINTHEAL:
			case MANAHEAL:
			case REFLECT:
				target = targets[0];
				break;
			default:
			{
				switch (skill.getTargetType())
				{
				case TARGET_SELF:
				case TARGET_PET:
				case TARGET_SUMMON:
				case TARGET_PARTY:
				case TARGET_CLAN:
				case TARGET_ALLY:
				case TARGET_ENEMY_ALLY:
					target = targets[0];
					break;
				case TARGET_OWNER_PET:
					if (this instanceof L2PetInstance)
					{
						target = ((L2PetInstance) this).getOwner();
					}
					break;
				default:
				{
					target = (L2Character) getTarget();
					break;
				}
				}
			}
			}
		}
		}

		if (target == null)
		{
			if (simultaneously)
				setIsCastingSimultaneouslyNow(false);
			else
				setIsCastingNow(false);
			if (this instanceof L2PcInstance)
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}

			return;
		}

		setAttackingChar(this);
		// setLastSkillCast(skill);

		// Get the Identifier of the skill
		int magicId = skill.getId();

		// Get the Display Identifier for a skill that client can't display
		int displayId = skill.getDisplayId();

		// Get the level of the skill
		int level = skill.getLevel();

		if (level < 1)
			level = 1;

		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();

		// Get the delay under which the cast can be aborted (base)
		int skillInterruptTime = skill.getSkillInterruptTime();

		boolean effectWhileCasting = skill.getSkillType() == L2SkillType.FORCE_BUFF || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME;

		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		// Don't modify the skill time for FORCE_BUFF skills. The skill time for those skills represent the buff time.
		if (!effectWhileCasting)
		{
			hitTime = Formulas.calcAtkSpd(this, skill, hitTime);
			if (coolTime > 0)
				coolTime = Formulas.calcAtkSpd(this, skill, coolTime);
		}

		// Calculate the Interrupt Time of the skill (base + modifier) if the skill is a spell else 0
		if (skill.isMagic())
			skillInterruptTime = Formulas.calcAtkSpd(this, skill, skillInterruptTime);
		else
			skillInterruptTime = 0;

		// Calculate altered Cast Speed due to BSpS/SpS
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null && skill.isMagic() && !effectWhileCasting && skill.getTargetType() != SkillTargetType.TARGET_SELF)
		{
			if ((weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					|| (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT))
			{
				// Only takes 70% of the time to cast a BSpS/SpS cast
				hitTime = (int) (0.70 * hitTime);
				coolTime = (int) (0.70 * coolTime);
				skillInterruptTime = (int) (0.70 * skillInterruptTime);

				// Because the following are magic skills that do not actively 'eat' BSpS/SpS,
				// I must 'eat' them here so players don't take advantage of infinite speed increase
				switch (skill.getSkillType())
				{
				case BUFF:
				case MANAHEAL:
				case RESURRECT:
				case RECALL:
				case DOT:
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
					break;
				}
			}
		}
		else if (this instanceof L2NpcInstance && skill.useSpiritShot() && !effectWhileCasting)
		{
			if (((L2NpcInstance) this).rechargeAutoSoulShot(false, true))
			{
				hitTime = (int) (0.70 * hitTime);
				coolTime = (int) (0.70 * coolTime);
				skillInterruptTime = (int) (0.70 * skillInterruptTime);
			}
		}

		// Don't modify skills HitTime if staticHitTime is specified for skill in datapack.
		if (skill.isStaticHitTime())
		{
			hitTime = skill.getHitTime();
			coolTime = skill.getCoolTime();
		}

		// queue herbs and potions
		if (isCastingSimultaneouslyNow() && simultaneously)
		{
			ThreadPoolManager.getInstance().scheduleAi(new UsePotionTask(this, skill), 100);
			return;
		}

		// Set the _castInterruptTime and casting status (L2PcInstance already has this true)
		if (simultaneously)
			setIsCastingSimultaneouslyNow(true);
		else
			setIsCastingNow(true);

		// Note: _castEndTime = GameTimeController.getGameTicks() + (coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK;
		if (!simultaneously)
		{
			_castInterruptTime = GameTimeController.getGameTicks() + skillInterruptTime / GameTimeController.MILLIS_IN_TICK;
		}
		else
			setLastSimultaneousSkillCast(skill);

		// Init the reuse time of the skill
		int reuseDelay;
		if (skill.isStaticReuse())
		{
			reuseDelay = skill.getReuseDelay();
		}
		else
		{
			if (skill.isMagic())
			{
				reuseDelay = (int) (skill.getReuseDelay() * getStat().getMReuseRate(skill));
			}
			else
			{
				reuseDelay = (int) (skill.getReuseDelay() * getStat().getPReuseRate(skill));
			}
			if (reuseDelay != 0)
				reuseDelay *= 333.0 / (skill.isMagic() ? getMAtkSpd() : getPAtkSpd());
		}

		boolean skillMastery = Formulas.calcSkillMastery(this, skill);
		if (skillMastery)
		{
			reuseDelay = 0;
			if (getActingPlayer() != null)
				getActingPlayer().sendPacket(SystemMessageId.SKILL_READY_TO_USE_AGAIN);
		}

		// Skill reuse check
		if (reuseDelay > 30000)
			addTimeStamp(skill.getId(), reuseDelay);

		// Check if this skill consume mp on start casting
		int initmpcons = getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			StatusUpdate su = new StatusUpdate(getObjectId());
			if (skill.isDance() || skill.isSong())
			{
				getStatus().reduceMp(calcStat(Stats.DANCE_CONSUME_RATE, initmpcons, null, null));
			}
			else if (skill.isMagic())
			{
				getStatus().reduceMp(calcStat(Stats.MAGIC_CONSUME_RATE, initmpcons, null, null));
			}
			else
			{
				getStatus().reduceMp(calcStat(Stats.PHYSICAL_CONSUME_RATE, initmpcons, null, null));
			}
			su.addAttribute(StatusUpdate.CUR_MP, (int) getStatus().getCurrentMp());
			sendPacket(su);
		}

		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
		{
			disableSkill(skill.getId(), reuseDelay);
		}

		// Make sure that char is facing selected target
		if (target != this)
			setHeading(Util.calculateHeadingFrom(this, target));

		// For force buff skills, start the effect as long as the player is casting.
		if (effectWhileCasting)
		{
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsume() > 0)
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, false))
				{
					sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					if (simultaneously)
						setIsCastingSimultaneouslyNow(false);
					else
						setIsCastingNow(false);
					if (this instanceof L2PcInstance)
						getAI().setIntention(AI_INTENTION_ACTIVE);
					return;
				}
			}

			if (this instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) this;
				// Reset soul bonus for skills
				player.resetLastSoulConsume();

				// Consume Souls if necessary
				if (skill.getSoulConsumeCount() > 0 || skill.getMaxSoulConsumeCount() > 0)
				{
					player.decreaseSouls(skill);
				}

				// Consume Charges if necessary ... L2SkillChargeDmg does the consume by itself.
				if (skill.getNeededCharges() > 0 && !(skill instanceof L2SkillChargeDmg))
				{
					player.decreaseCharges(skill.getNeededCharges());
				}
			}

			if (skill.getSkillType() == L2SkillType.FORCE_BUFF)
				startForceBuff(target, skill);
			else
				callSkill(skill, targets);
		}

		// To prevent area skill animation/packet arrive too late
		broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));
		// Send a Server->Client packet MagicSkillUse with target, displayId, level, skillTime, reuseDelay
		// to the L2Character AND to all L2PcInstance in the _knownPlayers of the L2Character
		broadcastPacket(new MagicSkillUse(this, target, displayId, level, hitTime, reuseDelay));

		if (this instanceof L2PcInstance)
		{
			long protTime = hitTime + coolTime;

			if (reuseDelay < protTime)
				protTime /= 2;

			((L2PcInstance) this).setSkillQueueProtectionTime(System.currentTimeMillis() + protTime);
		}

		// Send a system message USE_S1 to the L2Character
		if (this instanceof L2PcInstance && magicId != 1312)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.USE_S1);
			sm.addSkillName(skill);
			sendPacket(sm);
		}

		switch (skill.getTargetType())
		{
		case TARGET_AURA:
		case TARGET_FRONT_AURA:
		case TARGET_BEHIND_AURA:
		case TARGET_GROUND:
		{
			if (targets.length == 0)
			{
				// now cancels both, simultaneous and normal
				getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
				return;
			}
			break;
		}
		default:
			break;
		}

		// Before start AI Cast Broadcast Fly Effect is Need
		if (skill.getFlyType() != null && (this instanceof L2PcInstance))
		{
			ThreadPoolManager.getInstance().scheduleEffect(new FlyToLocationTask(this, target, skill), 50);
		}

		// launch the magic in hitTime milliseconds
		if (hitTime > 210)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if (this instanceof L2PcInstance && !effectWhileCasting)
			{
				sendPacket(new SetupGauge(SetupGauge.BLUE, hitTime));
			}

			if (simultaneously)
			{
				if (_skillCast2 != null)
				{
					_skillCast2.cancel(true);
					_skillCast2 = null;
				}
				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 200 ms before!
				if (effectWhileCasting)
					_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2, simultaneously), hitTime);
				else
					_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1, simultaneously), hitTime-200);
			}
			else
			{
				if (_skillCast != null)
				{
					_skillCast.cancel(true);
					_skillCast = null;
				}
				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 200 ms before!
				if (effectWhileCasting)
					_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2, simultaneously), hitTime);
				else
					_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 1, simultaneously), hitTime-200);
			}
		}
		else
		{
			onMagicLaunchedTimer(targets, skill, coolTime, true, simultaneously);
		}
	}

	private boolean checkDoCastConditions(L2Skill skill)
	{
		if (skill == null || isSkillDisabled(skill.getId()))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the caster has enough MP
		if (getStatus().getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			if (this instanceof L2PcInstance)
			{
				// Send a System Message to the caster
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_MP));

				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
			}
			return false;
		}

		// Check if the caster has enough HP
		if (getStatus().getCurrentHp() <= skill.getHpConsume())
		{
			if (this instanceof L2PcInstance)
			{
				// Send a System Message to the caster
				sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_HP));

				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
			}
			return false;
		}

		switch (skill.getSkillType())
		{
		case SUMMON_TRAP:
		{
			if (isInsideZone(L2Zone.FLAG_PEACE))
			{
				if (this instanceof L2PcInstance)
					sendPacket(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE);
				return false;
			}
			if (this instanceof L2PcInstance && ((L2PcInstance) this).getTrap() != null)
			{
				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}

			break;
		}
		case SUMMON:
		{
			if (!skill.isCubic() && this instanceof L2PcInstance && (getPet() != null || ((L2PcInstance) this).isMounted()))
			{
				if (_log.isDebugEnabled())
					_log.info("player has a pet already. ignore summon skill");

				sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
				return false;
			}
		}
		}

		if (!skill.isPotion())
		{
			// Check if the skill is a magic spell and if the L2Character is not muted
			if (skill.isMagic())
			{
				if (isMuted())
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
			else
			{
				// Check if the skill is physical and if the L2Character is not physical_muted
				if (isPhysicalMuted())
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
				else if (isPhysicalAttackMuted()) // Prevent use attack
				{
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}

		// prevent casting signets to peace zone
		if (skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME)
		{
			L2WorldRegion region = getWorldRegion();
			if (region == null)
				return false;
			boolean canCast = true;
			if (skill.getTargetType() == SkillTargetType.TARGET_GROUND && this instanceof L2PcInstance)
			{
				Point3D wp = ((L2PcInstance) this).getCurrentSkillWorldPosition();
				if (!region.checkEffectRangeInsidePeaceZone(skill, wp.getX(), wp.getY(), wp.getZ()))
					canCast = false;
			}
			else if (!region.checkEffectRangeInsidePeaceZone(skill, getX(), getY(), getZ()))
				canCast = false;
			if (!canCast)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill);
				sendPacket(sm);
				return false;
			}
		}

		// Check if the caster owns the weapon needed
		if (!skill.getWeaponDependancy(this, true))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}

		// Check if the spell consumes an Item
		// TODO: combine check and consume
		if (skill.getItemConsume() > 0 && getInventory() != null)
		{
			// Get the L2ItemInstance consumed by the spell
			L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());

			// Check if the caster owns enough consumed Item to cast
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (skill.getSkillType() == L2SkillType.SUMMON)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addNumber(skill.getItemConsume());
					sendPacket(sm);
					return false;
				}

				// Send a System Message to the caster
				sendPacket(new SystemMessage(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL));
				return false;
			}
		}
		return true;
	}

	/**
	 * Index according to skill id the current timestamp of use.<br>
	 * <br>
	 * 
	 * @param skill
	 *            id
	 * @param reuse
	 *            delay <BR>
	 *            <B>Overridden in :</B> (L2PcInstance)
	 */
	public void addTimeStamp(int skill, int reuse)
	{
		/***/
	}

	/**
	 * Index according to skill id the current timestamp of use.<br>
	 * <br>
	 * 
	 * @param skill
	 *            id <BR>
	 *            <B>Overridden in :</B> (L2PcInstance)
	 */
	public void removeTimeStamp(int skill)
	{
		/***/
	}

	public void startForceBuff(L2Character target, L2Skill skill)
	{
		if (skill.getSkillType() != L2SkillType.FORCE_BUFF)
			return;

		if (_forceBuff == null)
			_forceBuff = new ForceBuff(this, target, skill);
	}

	/**
	 * Kill the L2Character.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set target to null and cancel Attack or Cast </li>
	 * <li>Stop movement </li>
	 * <li>Stop HP/MP/CP Regeneration task </li>
	 * <li>Stop all active skills effects in progress on the L2Character </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform </li>
	 * <li>Notify L2Character AI </li>
	 * <BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2NpcInstance : Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds </li>
	 * <li> L2Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine </li>
	 * <li> L2PcInstance : Apply Death Penalty, Manage gain/loss Karma and Item Drop </li>
	 * <BR>
	 * <BR>
	 *
	 * @param killer
	 *            The L2Character who killed it
	 */
	public boolean doDie(L2Character killer)
	{
		// killing is only possible one time
		synchronized (this)
		{
			if (isDead())
				return false;
			// now reset currentHp to zero
			getStatus().setCurrentHp(0);
			if (isFakeDeath())
				stopFakeDeath(null);
			setIsDead(true);
		}
		// Set target to null and cancel Attack or Cast
		setTarget(null);

		// Stop movement
		stopMove(null);

		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();

		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if (this instanceof L2PlayableInstance)
		{
			L2PlayableInstance pl = (L2PlayableInstance) this;
			if (pl.isPhoenixBlessed())
			{
				if (pl.getCharmOfLuck()) //remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
					pl.stopCharmOfLuck(null);
				if (pl.isNoblesseBlessed())
					pl.stopNoblesseBlessing(null);
			}
			// Same thing if the Character isn't a Noblesse Blessed L2PlayableInstance
			else if (pl.isNoblesseBlessed())
			{
				pl.stopNoblesseBlessing(null);
				if (pl.getCharmOfLuck()) // remove Lucky Charm if player have Nobless blessing buff
					pl.stopCharmOfLuck(null);

				// Delete transformation effects, even if you have noblesse blessing
				L2Effect[] effects = getAllEffects();
				for (L2Effect e : effects)
				{
					if (e != null && e.getSkill().getTransformId() > 0)
						e.exit();
				}
			}
			else
				stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		else
			stopAllEffectsExceptThoseThatLastThroughDeath();

		if (this instanceof L2PcInstance && ((L2PcInstance) this).getAgathionId() != 0)
			((L2PcInstance) this).setAgathionId(0);

		calculateRewards(killer);

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();

		if (getWorldRegion() != null)
			getWorldRegion().onDeath(this);

		// Notify L2Character AI
		getAI().notifyEvent(CtrlEvent.EVT_DEAD, null);

		// Notify Quest of character's death
		for (QuestState qs : getNotifyQuestOfDeath())
		{
			qs.getQuest().notifyDeath((killer == null ? this : killer), this, qs);
		}
		getNotifyQuestOfDeath().clear();
		// If character is PhoenixBlessed
		// or has charm of courage inside siege battlefield (exact operation to be confirmed)
		// a resurrection popup will show up
		if (this instanceof L2Summon)
		{
			if (((L2Summon)this).isPhoenixBlessed() && ((L2Summon)this).getOwner() != null)
				((L2Summon)this).getOwner().revivePetRequest(((L2Summon)this).getOwner(), null);
		}
		else if (this instanceof L2PcInstance)
		{
			if (((L2PlayableInstance)this).isPhoenixBlessed())
				((L2PcInstance)this).reviveRequest(((L2PcInstance)this), null);
			else if (((L2PcInstance)this).getCharmOfCourage()
					&& isInsideZone(L2Zone.FLAG_SIEGE)
					&& ((L2PcInstance)this).getSiegeState() != 0) // could check it more accurately too
			{
				((L2PcInstance)this).reviveRequest(((L2PcInstance)this), null);
			}
		}
		getAttackByList().clear();
		return true;
	}

	/**
	 * @param killer
	 */
	protected void calculateRewards(L2Character killer)
	{
	}

	/** Sets HP, MP and CP and revives the L2Character. */
	public void doRevive()
	{
		if (!isDead())
			return;
		if (!isTeleporting())
		{
			setIsPendingRevive(false);
			setIsDead(false);

			boolean restorefull = false;

			if (this instanceof L2PlayableInstance && ((L2PlayableInstance) this).isPhoenixBlessed())
			{
				restorefull = true;
				((L2PlayableInstance) this).stopPhoenixBlessing(null);
			}

			if(restorefull)
			{
				//_status.setCurrentCp(getMaxCp()); //this is not confirmed...
				_status.setCurrentHp(getMaxHp()); //confirmed
				_status.setCurrentMp(getMaxMp()); //and also confirmed
			}
			else
			{
				_status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
				//_status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
				//_status.setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
			}

			// Start broadcast status
			broadcastPacket(new Revive(this));

			if (getWorldRegion() != null)
				getWorldRegion().onRevive(this);
		}
		else
			setIsPendingRevive(true);
	}

	/** Revives the L2Character using skill.
	 * @param revivePower */
	public void doRevive(double revivePower)
	{
		doRevive();
	}

	// =========================================================
	// Property - Public
	/**
	 * Return the L2CharacterAI of the L2Character and if its null create a new one.
	 */
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if (ai == null)
		{
			synchronized (this)
			{
				_ai = new L2CharacterAI(new AIAccessor());
				return _ai;
			}
		}

		return ai;
	}

	public void setAI(L2CharacterAI newAI)
	{
		L2CharacterAI oldAI = getAI();
		if (oldAI != null && oldAI != newAI && oldAI instanceof L2AttackableAI)
			oldAI.stopAITask();
		_ai = newAI;
	}

	/** Return True if the L2Character has a L2CharacterAI. */
	public boolean hasAI()
	{
		return _ai != null;
	}

	/** Return True if the L2Character is RaidBoss or his minion. */
	public boolean isRaid()
	{
		return _isRaid;
	}

	/**
	 * Set this Npc as a Raid instance.<BR><BR>
	 * @param isRaid
	 */
	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}

	/** Return a list of L2Character that attacked. */
	public final List<L2Character> getAttackByList()
	{
		if (_attackByList == null)
			_attackByList = new SingletonList<L2Character>();
		
		return _attackByList;
	}

	public final L2Character getAttackingChar()
	{
		return _attackingChar;
	}

	/**
	 * Set _attackingChar to the L2Character that attacks this one.<BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2Character that attcks this one
	 */
	public final void setAttackingChar(L2Character player)
	{
		if (player == null || player == this)
			return;
		_attackingChar = player;
		addAttackerToAttackByList(player);
	}

	public final L2Skill getLastSimultaneousSkillCast()
	{
		return _lastSimultaneousSkillCast;
	}

	public void setLastSimultaneousSkillCast (L2Skill skill)
	{
		_lastSimultaneousSkillCast = skill;
	}

	public final boolean isAfraid()
	{
		return _isAfraid;
	}

	public final void setIsAfraid(boolean value)
	{
		_isAfraid = value;
	}

	/** Return True if the L2Character can't use its skills (ex : stun, sleep...). */
	public boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isStunned() || isSleeping() || isImmobileUntilAttacked() || isParalyzed() || isPetrified();
	}

	/** Return True if the L2Character can't attack (stun, sleep, attackEndTime, fakeDeath, paralyse). */
	public boolean isAttackingDisabled()
	{
		return isStunned() || isSleeping() || isImmobileUntilAttacked() || isAttackingNow() || isFakeDeath() || isParalyzed()
				|| isPetrified() || isFallsdown() || isPhysicalAttackMuted() || isCoreAIDisabled();
	}

	public final Calculator[] getCalculators()
	{
		return _calculators;
	}

	public final boolean isConfused()
	{
		return _isConfused;
	}

	public final void setIsConfused(boolean value)
	{
		_isConfused = value;
	}

	public final boolean isDead()
	{
		return _isDead;
	}

	public final void setIsDead(boolean value)
	{
		_isDead = value;
	}

	/** Return True if the L2Character is dead or use fake death.  */
	public final boolean isAlikeDead()
	{
		return isFakeDeath() || _isDead;
	}

	public final boolean isFakeDeath()
	{
		return _isFakeDeath;
	}

	public final void setIsFakeDeath(boolean value)
	{
		_isFakeDeath = value;
	}

	// [L2J_JP_ADD START]
	public final boolean isFallsdown()
	{
		return _isFallsdown;
	}

	public final void setIsFallsdown(boolean value)
	{
		_isFallsdown = value;
	}

	// [L2J_JP_ADD END]

	public boolean isFlying()
	{
		return false;
	}

	public boolean isImmobilized()
	{
		return _isImmobilized;
	}

	public void setIsImmobilized(boolean value)
	{
		_isImmobilized = value;
	}

	public final boolean isMuted()
	{
		return _isMuted;
	}

	public final void setIsMuted(boolean value)
	{
		_isMuted = value;
	}

	public final boolean isPhysicalMuted()
	{
		return _isPhysicalMuted;
	}

	public final void setIsPhysicalMuted(boolean value)
	{
		_isPhysicalMuted = value;
	}

	public final boolean isPhysicalAttackMuted()
	{
		return _isPhysicalAttackMuted;
	}

	public final void setIsPhysicalAttackMuted(boolean value)
	{
		_isPhysicalAttackMuted = value;
	}

	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}

	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}

	/** Return True if the L2Character can't move (stun, root, sleep, overload, paralyzed). */
	public boolean isMovementDisabled()
	{
		// check for isTeleporting to prevent teleport cheating (if appear packet not received)
		return isStunned() || isRooted() || isSleeping() || isTeleporting() || isImmobileUntilAttacked() || isOverloaded() || isParalyzed() || isImmobilized()
				|| isFakeDeath() || isFallsdown() || isPetrified();
	}

	/** Return True if the L2Character can not be controlled by the player (confused, afraid). */
	public boolean isOutOfControl()
	{
		return isConfused() || isAfraid();
	}

	public final boolean isOverloaded()
	{
		return _isOverloaded;
	}

	/** Set the overloaded status of the L2Character is overloaded (if True, the L2PcInstance can't take more item). */
	public final void setIsOverloaded(boolean value)
	{
		_isOverloaded = value;
	}

	public final boolean isParalyzed()
	{
		return _isParalyzed;
	}

	public final void setIsParalyzed(boolean value)
	{
		_isParalyzed = value;
	}

	public final boolean isPendingRevive()
	{
		return isDead() && _isPendingRevive;
	}

	public final void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}

	public final boolean isDisarmed()
	{
		return _isDisarmed;
	}

	public final void setIsDisarmed(boolean value)
	{
		_isDisarmed = value;
	}

	/**
	 * Return the L2Summon of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance</li>
	 * <BR>
	 * <BR>
	 */
	public L2Summon getPet()
	{
		return null;
	}

	public final boolean isRooted()
	{
		return _isRooted;
	}

	public final void setIsRooted(boolean value)
	{
		_isRooted = value;
	}

	/** Return True if the L2Character is running. */
	public final boolean isRunning()
	{
		return _isRunning;
	}

	public final void setIsRunning(boolean value)
	{
		_isRunning = value;
		if (getRunSpeed() != 0)
			broadcastPacket(new ChangeMoveType(this));
		
		broadcastFullInfo();
	}

	/** Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setRunning()
	{
		if (!isRunning())
			setIsRunning(true);
	}

	public final boolean isSleeping()
	{
		return _isSleeping;
	}

	public final void setIsImmobileUntilAttacked(boolean value)
	{
		_isImmobileUntilAttacked = value;
	}

	public final boolean isImmobileUntilAttacked()
	{
		return _isImmobileUntilAttacked;
	}

	public final void setIsSleeping(boolean value)
	{
		_isSleeping = value;
	}

	public final boolean isBlessedByNoblesse()
	{
		return _isBlessedByNoblesse;
	}

	public final void setIsBlessedByNoblesse(boolean value)
	{
		_isBlessedByNoblesse = value;
	}

	public final boolean isLuckByNoblesse()
	{
		return _isLuckByNoblesse;
	}

	public final void setIsLuckByNoblesse(boolean value)
	{
		_isLuckByNoblesse = value;
	}

	public final boolean isStunned()
	{
		return _isStunned;
	}

	public final void setIsStunned(boolean value)
	{
		_isStunned = value;
	}

	public final boolean isPetrified()
	{
		return _isPetrified;
	}

	public final void setIsPetrified(boolean value)
	{
		_isPetrified = value;
	}

	public final boolean isBetrayed()
	{
		return _isBetrayed;
	}

	public final void setIsBetrayed(boolean value)
	{
		_isBetrayed = value;
	}

	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}

	public final void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}

	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}

	public boolean isInvul()
	{
		return _isInvul || _isTeleporting;
	}

	public boolean isUndead()
	{
		return _template.isUndead();
	}

	@Override
	public CharKnownList getKnownList()
	{
		if (_knownList == null)
			_knownList = new CharKnownList(this);
		
		return _knownList;
	}

	public CharStat getStat()
	{
		if (_stat == null)
			_stat = new CharStat(this);

		return _stat;
	}

	public CharStatus getStatus()
	{
		if (_status == null)
			_status = new CharStatus(this);

		return _status;
	}

	public L2CharTemplate getTemplate()
	{
		return _template;
	}

	/**
	 * Set the template of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...). All of those properties are stored in a different
	 * template for each type of L2Character. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of L2Character is
	 * spawned, server just create a link between the instance and the template This link is stored in <B>_template</B><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li> this instanceof L2Character</li>
	 * <BR>
	 * <BR
	 */
	protected final void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}

	/** Return the Title of the L2Character. */
	public final String getTitle()
	{
		return _title;
	}

	/** Set the Title of the L2Character. */
	public final void setTitle(String value)
	{
		if (Config.FACTION_ENABLED)
			if (this instanceof L2PcInstance)
				if (FactionManager.getInstance().getFactionTitles().contains(value.toLowerCase()) && !value.isEmpty())
				{
					_title = getTitle();
					sendMessage("Title protected by Faction System");
					return;
				}
		if ((this instanceof L2PcInstance) && value.length() > 16)
			value = value.substring(0, 15);
		_title = value;
	}

	/** Set the L2Character movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance. */
	public final void setWalking()
	{
		if (isRunning())
			setIsRunning(false);
	}

	/** Task launching the function enableSkill() */
	class EnableSkill implements Runnable
	{
		int	_skillId;

		public EnableSkill(int skillId)
		{
			_skillId = skillId;
		}

		public void run()
		{
			try
			{
				enableSkill(_skillId);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Task launching the function onHitTimer().<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a
	 * L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li>
	 * <BR>
	 * <BR>
	 */
	class HitTask implements Runnable
	{
		L2Character	_hitTarget;
		int			_damage;
		boolean		_crit;
		boolean		_miss;
		byte		_shld;
		boolean		_soulshot;

		public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}

		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
			}
			catch (Exception e)
			{
				_log.fatal(e.getMessage(), e);
			}
		}
	}

	/** Task launching the magic skill phases */
	class MagicUseTask implements Runnable
	{
		L2Character[]	_targets;
		L2Skill		_skill;
		int			_coolTime;
		int			_phase;
		boolean _simultaneously;

		public MagicUseTask(L2Character[] targets, L2Skill skill, int coolTime, int phase, boolean simultaneously)
		{
			_targets = targets;
			_skill = skill;
			_coolTime = coolTime;
			_phase = phase;
			_simultaneously = simultaneously;
		}

		public void run()
		{
			try
			{
				switch (_phase)
				{
				case 1:
					onMagicLaunchedTimer(_targets, _skill, _coolTime, false, _simultaneously);
					break;
				case 2:
					onMagicHitTimer(_targets, _skill, _coolTime, false, _simultaneously);
					break;
				case 3:
					onMagicFinalizer(_skill, _targets[0], _simultaneously);
					break;
				default:
					break;
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
				if (_simultaneously)
					setIsCastingSimultaneouslyNow(false);
				else
					setIsCastingNow(false);
			}
		}
	}

	/** Task launching the function useMagic() */
	class QueuedMagicUseTask implements Runnable
	{
		L2PcInstance	_currPlayer;
		L2Skill			_queuedSkill;
		boolean			_isCtrlPressed;
		boolean			_isShiftPressed;

		public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
		{
			_currPlayer = currPlayer;
			_queuedSkill = queuedSkill;
			_isCtrlPressed = isCtrlPressed;
			_isShiftPressed = isShiftPressed;
		}

		public void run()
		{
			try
			{
				_currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	/** Task of AI notification */
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent	_evt;

		NotifyAITask(CtrlEvent evt)
		{
			_evt = evt;
		}

		public void run()
		{
			try
			{
				getAI().notifyEvent(_evt, null);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	/** Task launching the function stopPvPFlag() */
	public class PvPFlag implements Runnable
	{
		public void run()
		{
			try
			{
				if (System.currentTimeMillis() > getPvpFlagLasts())
				{
					stopPvPFlag();
				}
				else if (System.currentTimeMillis() > (getPvpFlagLasts() - 20000))
				{
					updatePvPFlag(2);
				}
				else
				{
					updatePvPFlag(1);
				}
			}
			catch (Exception e)
			{
				_log.warn(e.getMessage(), e);
			}
		}
	} // =========================================================

	// =========================================================
	// Abnormal Effect - NEED TO REMOVE ONCE L2CHARABNORMALEFFECT IS COMPLETE
	// Data Field
	/** Map 32 bits (0x0000) containing all abnormal effect in progress */
	private int				_abnormalEffects;

	private CharEffectList	_effects						= new CharEffectList(this);

	public static final int	ABNORMAL_EFFECT_BLEEDING		= 0x0000001;
	public static final int	ABNORMAL_EFFECT_POISON			= 0x0000002;
	public static final int	ABNORMAL_EFFECT_REDCIRCLE		= 0x0000004;
	public static final int	ABNORMAL_EFFECT_ICE				= 0x0000008;
	public static final int	ABNORMAL_EFFECT_WIND			= 0x0000010;
	public static final int	ABNORMAL_EFFECT_UNKNOWN_6		= 0x0000020;
	public static final int	ABNORMAL_EFFECT_STUN			= 0x0000040;
	public static final int	ABNORMAL_EFFECT_SLEEP			= 0x0000080;
	public static final int	ABNORMAL_EFFECT_MUTED			= 0x0000100;
	public static final int	ABNORMAL_EFFECT_ROOT			= 0x0000200;
	public static final int	ABNORMAL_EFFECT_HOLD_1			= 0x0000400;
	public static final int	ABNORMAL_EFFECT_HOLD_2			= 0x0000800;
	public static final int	ABNORMAL_EFFECT_UNKNOWN_13		= 0x0001000;
	public static final int	ABNORMAL_EFFECT_BIG_HEAD		= 0x0002000;
	public static final int	ABNORMAL_EFFECT_FLAME			= 0x0004000;
	public static final int	ABNORMAL_EFFECT_UNKNOWN_16		= 0x0008000;
	public static final int	ABNORMAL_EFFECT_GROW			= 0x0010000;
	public static final int	ABNORMAL_EFFECT_FLOATING_ROOT	= 0x0020000;
	public static final int	ABNORMAL_EFFECT_DANCE_STUNNED	= 0x0040000;
	public static final int	ABNORMAL_EFFECT_FIREROOT_STUN	= 0x0080000;
	public static final int	ABNORMAL_EFFECT_STEALTH			= 0x0100000;
	public static final int	ABNORMAL_EFFECT_IMPRISIONING_1	= 0x0200000;
	public static final int	ABNORMAL_EFFECT_IMPRISIONING_2	= 0x0400000;
	public static final int	ABNORMAL_EFFECT_MAGIC_CIRCLE	= 0x0800000;
	public static final int	ABNORMAL_EFFECT_ICE2			= 0x1000000;
	public static final int	ABNORMAL_EFFECT_EARTHQUAKE		= 0x2000000;
	public static final int	ABNORMAL_EFFECT_UNKNOWN27		= 0x4000000;
	public static final int	ABNORMAL_EFFECT_INVULNERABLE	= 0x8000000;

	// FIXME: TEMP HACKS (get the proper mask for these effects)
	public static final int	ABNORMAL_EFFECT_CONFUSED		= 0x0020;
	public static final int	ABNORMAL_EFFECT_AFRAID			= 0x0010;

	// Method - Public
	/**
	 * Launch and add L2Effect (including Stack Group management) to L2Character and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of
	 * _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in
	 * progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time on a
	 * L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Add the L2Effect to the L2Character _effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the L2Character (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the L2Character (remove previous stacked effect Funcs
	 * if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icons on player client</li>
	 * <BR>
	 */
	public void addEffect(L2Effect newEffect)
	{
		_effects.addEffect(newEffect);
	}

	/**
	 * Stop and remove L2Effect (including Stack Group management) from L2Character and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of
	 * _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * Several same effect can't be used on a L2Character at the same time. Indeed, effects are not stackable and the last cast will replace the previous in
	 * progress. More, some effects belong to the same Stack Group (ex WindWald and Haste Potion). If 2 effects of a same group are used at the same time on a
	 * L2Character, only the more efficient (identified by its priority order) will be preserve.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li>
	 * <BR>
	 */
	public void removeEffect(L2Effect effect)
	{
		_effects.removeEffect(effect);
	}
	
	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startAbnormalEffect(int mask)
	{
		_abnormalEffects |= mask;
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Confused flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startConfused()
	{
		setIsConfused(true);
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startFakeDeath()
	{
		// [L2J_JP ADD START]
		setIsFallsdown(true);

		if (Config.ALT_FAIL_FAKEDEATH)
		{
			// It fails in Fake Death at the probability
			setIsFakeDeath(true);
			if (_attackingChar != null)
			{
				int _diff;
				_diff = _attackingChar.getLevel() - getLevel();
				switch (_diff)
				{
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
					if (Rnd.nextInt(100) >= 95) // fails at 5%.
						setIsFakeDeath(false);
					break;
				case 6:
					if (Rnd.nextInt(100) >= 90) // fails at 10%.
						setIsFakeDeath(false);
					break;
				case 7:
					if (Rnd.nextInt(100) >= 85) // fails at 15%.
						setIsFakeDeath(false);
					break;
				case 8:
					if (Rnd.nextInt(100) >= 80) // fails at 20%.
						setIsFakeDeath(false);
					break;
				case 9:
					if (Rnd.nextInt(100) >= 75) // fails at 25%.
						setIsFakeDeath(false);
					break;
				default:
					if (_diff > 9)
					{
						if (Rnd.nextInt(100) >= 50) // fails at 50%.
							setIsFakeDeath(false);
					}
					else
					{
						setIsFakeDeath(true);
					}
				}
				// If _attackingChar is L2RaidBoss, Fake Death will have failed.
				if (_attackingChar.isRaid())
				{
					setIsFakeDeath(false);
				}
			}
			else
			// attacked from aggressive monster
			{
				if (Rnd.nextInt(100) >= 75) // fails at 25%.
					setIsFakeDeath(false);
			}
		}
		else
		{
			setIsFakeDeath(true);
		}
		// [L2J_JP ADD END]

		/* Aborts any attacks/casts if fake dead */
		abortAttack();
		abortCast();
		stopMove(null);
		sendPacket(ActionFailed.STATIC_PACKET);
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}

	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startFear()
	{
		setIsAfraid(true);
		getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startMuted()
	{
		setIsMuted(true);
		/* Aborts any casts if muted */
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Physical_Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startPhysicalMuted()
	{
		setIsPhysicalMuted(true);
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Root flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startRooted()
	{
		setIsRooted(true);
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED, null);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Sleep flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR>
	 * <BR>
	 */
	public final void startSleeping()
	{
		setIsSleeping(true);
		/* Aborts any attacks/casts if sleeped */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}

	public final void startImmobileUntilAttacked()
	{
		setIsImmobileUntilAttacked(true);
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING, null);
		updateAbnormalEffect();
	}

	public final void startLuckNoblesse()
	{
		setIsBlessedByNoblesse(true);
		getAI().notifyEvent(CtrlEvent.EVT_LUCKNOBLESSE, null);
	}

	public final void stopLuckNoblesse()
	{
		setIsBlessedByNoblesse(false);
		getAI().notifyEvent(CtrlEvent.EVT_LUCKNOBLESSE, null);
	}

	/**
	 * Launch a Stun Abnormal Effect on the L2Character.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this L2Character</li>
	 * <li>If Stun succeed, active the abnormal effect Stun flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Stun NOT succeed, send a system message Failed to the L2PcInstance attacker</li>
	 * <BR>
	 * <BR>
	 */
	public final void startStunning()
	{
		setIsStunned(true);
		/* Aborts any attacks/casts if stunned */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED, null);
		updateAbnormalEffect();
	}

	public final void startParalyze()
	{
		setIsParalyzed(true);
		/* Aborts any attacks/casts if paralyzed */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_PARALYZED, null);
		updateAbnormalEffect();
	}

	public final void startBetray()
	{
		setIsBetrayed(true);
		getAI().notifyEvent(CtrlEvent.EVT_BETRAYED, null);
		updateAbnormalEffect();
	}

	public final void stopBetray()
	{
		stopEffects(L2EffectType.BETRAY);
		setIsBetrayed(false);
		updateAbnormalEffect();
	}

	/**
	 * Modify the abnormal effect map according to the mask.<BR>
	 * <BR>
	 */
	public final void stopAbnormalEffect(int mask)
	{
		_abnormalEffects &= ~mask;
		updateAbnormalEffect();
	}

	/**
	 * Stop all active skills effects in progress on the L2Character.<BR>
	 * <BR>
	 */
	public final void stopAllEffects()
	{
		_effects.stopAllEffects();
		
		broadcastFullInfo();
	}
	
	public final void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		_effects.stopAllEffectsExceptThoseThatLastThroughDeath();
		
		broadcastFullInfo();
	}
	
	/**
	 * Stop a specified/all Confused abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Confused abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _confused to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * <BR>
	 * <BR>
	 */
	public final void stopConfused(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.CONFUSION);
		else
			removeEffect(effect);

		setIsConfused(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void startPhysicalAttackMuted()
	{
		setIsPhysicalAttackMuted(true);
		abortAttack();
	}

	public final void stopPhysicalAttackMuted(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.PHYSICAL_ATTACK_MUTE);
		else
			removeEffect(effect);
		setIsPhysicalAttackMuted(false);
	}

	/**
	 * Stop and remove the L2Effects corresponding to the L2Skill Identifier and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of
	 * _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 *
	 * @param skillId
	 *            The L2Skill Identifier of the L2Effect to remove from _effects
	 */
	public final void stopSkillEffects(int skillId)
	{
		_effects.stopSkillEffects(skillId);
	}

	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the L2Character and update client magic icon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>. The Integer key of
	 * _effects is the L2Skill Identifier that has created the L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li>
	 * <BR>
	 * <BR>
	 *
	 * @param type
	 *            The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 */
	public final void stopEffects(L2EffectType type)
	{
		_effects.stopEffects(type);
	}

	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _fake_death to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <BR>
	 * <BR>
	 */
	public final void stopFakeDeath(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.FAKE_DEATH);
		else
			removeEffect(effect);

		setIsFakeDeath(false);
		setIsFallsdown(false); // [L2J_JP_ADD]
		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		if (this instanceof L2PcInstance)
		{
			((L2PcInstance) this).setRecentFakeDeath(true);
		}
		ChangeWaitType revive = new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH);
		broadcastPacket(revive);
		//TODO: Temp hack: players see FD on ppl that are moving: Teleport to someone who uses FD - if he gets up he will fall down again for that client -
		// even tho he is actually standing... Probably bad info in CharInfo packet?
		broadcastPacket(new Revive(this));
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
	}

	/**
	 * Stop a specified/all Fear abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _afraid to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * <BR>
	 * <BR>
	 */
	public final void stopFear(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.FEAR);
		else
			removeEffect(effect);

		setIsAfraid(false);
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Muted abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _muted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * <BR>
	 * <BR>
	 */
	public final void stopMuted(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.MUTE);
		else
			removeEffect(effect);

		setIsMuted(false);
		updateAbnormalEffect();
	}

	public final void stopPhysicalMuted(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.PHYSICAL_MUTE);
		else
			removeEffect(effect);

		setIsPhysicalMuted(false);
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Root abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Root abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _rooted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * <BR>
	 * <BR>
	 */
	public final void stopRooting(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.ROOT);
		else
			removeEffect(effect);

		setIsRooted(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _sleeping to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * <BR>
	 * <BR>
	 */
	public final void stopSleeping(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.SLEEP);
		else
			removeEffect(effect);

		setIsSleeping(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopImmobileUntilAttacked(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.IMMOBILEUNTILATTACKED);
		else
		{
			removeEffect(effect);
			stopSkillEffects(effect.getSkill().getNegateId());
		}

		setIsImmobileUntilAttacked(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopNoblesse()
	{
		stopEffects(L2EffectType.NOBLESSE_BLESSING);
		stopEffects(L2EffectType.LUCKNOBLESSE);
		setIsBlessedByNoblesse(false);
		setIsLuckByNoblesse(false);
		getAI().notifyEvent(CtrlEvent.EVT_LUCKNOBLESSE, null);
	}

	/**
	 * Stop a specified/all Stun abnormal L2Effect.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _stuned to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li>
	 * <BR>
	 * <BR>
	 */
	public final void stopStunning(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.STUN);
		else
			removeEffect(effect);

		setIsStunned(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	public final void stopParalyze(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.PARALYZE);
		else
			removeEffect(effect);

		setIsParalyzed(false);
		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	/**
	* Stop L2Effect: Transformation<BR><BR>
	*
	* <B><U> Actions</U> :</B><BR><BR>
	* <li>Remove Transformation Effect</li>
	* <li>Notify the L2Character AI</li>
	* <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	*
	*/
	public final void stopTransformation(L2Effect effect)
	{
		if (effect == null)
		{
			stopEffects(L2EffectType.TRANSFORMATION);
		}
		else
		{
			removeEffect(effect);
		}

		// if this is a player instance, then untransform, also set the transform_id column equal to 0 if not cursed.
		if (this instanceof L2PcInstance)
		{
			if (((L2PcInstance) this).getTransformation() != null)
			{
				((L2PcInstance) this).untransform();
			}
		}

		getAI().notifyEvent(CtrlEvent.EVT_THINK, null);
		updateAbnormalEffect();
	}

	/**
	 * Not Implemented.<BR>
	 * <BR>
	 * <B><U> Overridden in</U> :</B><BR>
	 * <BR>
	 * <li>L2NPCInstance</li>
	 * <li>L2PcInstance</li>
	 * <li>L2Summon</li>
	 * <li>L2DoorInstance</li>
	 * <BR>
	 * <BR>
	 */
	public final void updateAbnormalEffect()
	{
		broadcastFullInfo();
	}
	
	// Property - Public
	/**
	 * Return a map of 16 bits (0x0000) containing all abnormal effect in progress for this L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...). The map is
	 * calculated by applying a BINARY OR operation on each effect.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li> Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...</li>
	 * <BR>
	 * <BR>
	 */
	public int getAbnormalEffect()
	{
		int ae = _abnormalEffects;
		if (isStunned())
			ae |= ABNORMAL_EFFECT_STUN;
		if (isRooted())
			ae |= ABNORMAL_EFFECT_ROOT;
		if (isSleeping())
			ae |= ABNORMAL_EFFECT_SLEEP;
		if (isConfused())
			ae |= ABNORMAL_EFFECT_CONFUSED;
		if (isMuted())
			ae |= ABNORMAL_EFFECT_MUTED;
		if (isAfraid())
			ae |= ABNORMAL_EFFECT_AFRAID;
		if (isPhysicalMuted())
			ae |= ABNORMAL_EFFECT_MUTED;
		return ae;
	}

	/**
	 * Return all active skills effects in progress on the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>. The Integer key of _effects is the L2Skill Identifier that
	 * has created the effect.<BR>
	 * <BR>
	 *
	 * @return A table containing all active skills effect in progress on the L2Character
	 */
	public final L2Effect[] getAllEffects()
	{
		return _effects.getAllEffects();
	}

	/**
	 * Return L2Effect in progress on the L2Character corresponding to the L2Skill Identifier.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param index
	 *            The L2Skill Identifier of the L2Effect to return from the _effects
	 * @return The L2Effect corresponding to the L2Skill Identifier
	 */
	public final L2Effect getFirstEffect(int index)
	{
		return _effects.getFirstEffect(index);
	}

	/**
	 * Return the first L2Effect in progress on the L2Character created by the L2Skill.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param skill
	 *            The L2Skill whose effect must be returned
	 * @return The first L2Effect created by the L2Skill
	 */
	public final L2Effect getFirstEffect(L2Skill skill)
	{
		return _effects.getFirstEffect(skill);
	}

	/**
	 * Return the first L2Effect in progress on the L2Character corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * <BR>
	 *
	 * @param tp
	 *            The Effect Type of skills whose effect must be returned
	 * @return The first L2Effect corresponding to the Effect Type
	 */
	public final L2Effect getFirstEffect(L2EffectType tp)
	{
		return _effects.getFirstEffect(tp);
	}

	// =========================================================

	// =========================================================
	// NEED TO ORGANIZE AND MOVE TO PROPER PLACE
	/** This class permit to the L2Character AI to obtain informations and uses L2Character method */
	public class AIAccessor
	{
		public AIAccessor()
		{
		}

		/**
		 * Return the L2Character managed by this Accessor AI.<BR>
		 * <BR>
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}

		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.<BR>
		 * <BR>
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			moveToLocation(x, y, z, offset);
		}

		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.<BR>
		 * <BR>
		 */
		public void moveTo(int x, int y, int z)
		{
			moveToLocation(x, y, z, 0);
		}

		/**
		 * Accessor to L2Character stopMove() method.<BR>
		 * <BR>
		 */
		public void stopMove(L2CharPosition pos)
		{
			L2Character.this.stopMove(pos);
		}

		/**
		 * Accessor to L2Character doAttack() method.<BR>
		 * <BR>
		 */
		public void doAttack(L2Character target)
		{
			if (L2Character.this != target)
				L2Character.this.doAttack(target);
		}

		/**
		 * Accessor to L2Character doCast() method.<BR>
		 * <BR>
		 */
		public void doCast(L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}

		/**
		 * Create a NotifyAITask.<BR>
		 * <BR>
		 */
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}

		/**
		 * Cancel the AI.<BR>
		 * <BR>
		 */
		public void detachAI()
		{
			_ai = null;
		}
	}

	/**
	 * This class group all mouvement data.<BR>
	 * <BR>
	 * <B><U> Data</U> :</B><BR>
	 * <BR>
	 * <li>_moveTimestamp : Last time position update</li>
	 * <li>_xDestination, _yDestination, _zDestination : Position of the destination</li>
	 * <li>_xMoveFrom, _yMoveFrom, _zMoveFrom : Position of the origin</li>
	 * <li>_moveStartTime : Start time of the movement</li>
	 * <li>_ticksToMove : Nb of ticks between the start and the destination</li>
	 * <li>_xSpeedTicks, _ySpeedTicks : Speed in unit/ticks</li>
	 * <BR>
	 * <BR>
	 */
	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		public int				_moveStartTime;
		public int				_moveTimestamp; // last update
		public int				_xDestination;
		public int				_yDestination;
		public int				_zDestination;
		public double			_xAccurate; // otherwise there would be rounding errors
		public double			_yAccurate;
		public double			_zAccurate;
		public int				_yMoveFrom;
		public int				_zMoveFrom;
		public int				_heading;

		public boolean			disregardingGeodata;
		public int				onGeodataPathIndex;
		public List<AbstractNodeLoc> geoPath;
		public int				geoPathAccurateTx;
		public int				geoPathAccurateTy;
		public int				geoPathGtx;
		public int				geoPathGty;
	}

	/** Table containing all skillId that are disabled */
	protected Set<Integer>				_disabledSkills;
	private boolean						_allSkillsDisabled;

	// private int _flyingRunSpeed;
	// private int _floatingWalkSpeed;
	// private int _flyingWalkSpeed;
	// private int _floatingRunSpeed;

	/** Movement data of this L2Character */
	protected MoveData					_move;

	/** Orientation of the L2Character */
	private int							_heading;

	/** L2Charcater targeted by the L2Character */
	private L2Object					_target					= null;

	// set by the start of attack, in game ticks
	private long						_attackEndTime;
	private int							_attacking;
	private int							_disableBowAttackEndTime;
	private int							_disableCrossBowAttackEndTime;

	private int							_castInterruptTime;

	/** Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE */
	private static final Calculator[]	NPC_STD_CALCULATOR;
	static
	{
		NPC_STD_CALCULATOR = Formulas.getStdNPCCalculators();
	}

	protected L2CharacterAI				_ai;

	/** Future Skill Cast */
	protected Future<?>					_skillCast;
	protected Future<?>					_skillCast2;

	/** Char Coords from Client */
	private int							_clientX;
	private int							_clientY;
	private int							_clientZ;
	private int							_clientHeading;

	/** List of all QuestState instance that needs to be notified of this character's death */
	private List<QuestState>			_NotifyQuestOfDeathList	= new SingletonList<QuestState>();

	/**
	 * Add QuestState instance that is to be notified of character's death.<BR>
	 * <BR>
	 *
	 * @param qs
	 *            The QuestState that subscribe to this event
	 */
	public void addNotifyQuestOfDeath(QuestState qs)
	{
		if (qs == null || _NotifyQuestOfDeathList.contains(qs))
			return;

		_NotifyQuestOfDeathList.add(qs);
	}

	/**
	 * Return a list of L2Character that attacked.<BR>
	 * <BR>
	 */
	public final List<QuestState> getNotifyQuestOfDeath()
	{
		if (_NotifyQuestOfDeathList == null)
			_NotifyQuestOfDeathList = new SingletonList<QuestState>();

		return _NotifyQuestOfDeathList;
	}

	/**
	 * Return True if the L2Character is avoiding a geodata obstacle.<BR>
	 * <BR>
	 */
	public final boolean isOnGeodataPath()
	{
		MoveData m = _move;
		if (m == null)
			return false;

		if (m.onGeodataPathIndex == -1)
			return false;

        return m.onGeodataPathIndex != m.geoPath.size() - 1;
    }
	
	public final void addStatFunc(Func f)
	{
		if (f == null)
			return;
		
		synchronized (_calculators)
		{
			// Check if Calculator set is linked to the standard Calculator set of NPC
			if (_calculators == NPC_STD_CALCULATOR)
			{
				// Create a copy of the standard NPC Calculator set
				_calculators = new Calculator[Stats.NUM_STATS];
				
				for (int i = 0; i < Stats.NUM_STATS; i++)
				{
					if (NPC_STD_CALCULATOR[i] != null)
						_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
				}
			}
			
			// Select the Calculator of the affected state in the Calculator set
			int stat = f.stat.ordinal();
			
			if (_calculators[stat] == null)
				_calculators[stat] = new Calculator();
			
			// Add the Func to the calculator corresponding to the state
			_calculators[stat].addFunc(f);
			
			if (this instanceof L2PcInstance)
				((L2PcInstance)this).onFuncAddition(f);
		}
		
		broadcastFullInfo();
	}
	
	public final void addStatFuncs(Func[] funcs)
	{
		for (Func f : funcs)
			addStatFunc(f);
	}
	
	public final void addStatFuncs(Iterable<Func> funcs)
	{
		for (Func f : funcs)
			addStatFunc(f);
	}
	
	public final void removeStatsOwner(FuncOwner owner)
	{
		// Go through the Calculator set
		synchronized (_calculators)
		{
			for (int i = 0; i < _calculators.length; i++)
			{
				if (_calculators[i] != null)
				{
					// Delete all Func objects of the selected owner
					_calculators[i].removeOwner(owner, this);
					
					if (_calculators[i].size() == 0)
						_calculators[i] = null;
				}
			}
			
			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if (this instanceof L2NpcInstance)
			{
				int i = 0;
				for (; i < Stats.NUM_STATS; i++)
				{
					if (!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
						break;
				}
				
				if (i >= Stats.NUM_STATS)
					_calculators = NPC_STD_CALCULATOR;
			}
		}
		
		broadcastFullInfo();
	}
	
	/**
	 * Return the orientation of the L2Character.<BR>
	 * <BR>
	 */
	public final int getHeading()
	{
		return _heading;
	}

	/**
	 * Set the orientation of the L2Character.<BR>
	 * <BR>
	 */
	public final void setHeading(int heading)
	{
		_heading = heading;
	}

	/**
	 * Return the X destination of the L2Character or the X position if not in movement.<BR>
	 * <BR>
	 */
	public final int getClientX()
	{
		return _clientX;
	}

	public final int getClientY()
	{
		return _clientY;
	}

	public final int getClientZ()
	{
		return _clientZ;
	}

	public final int getClientHeading()
	{
		return _clientHeading;
	}

	public final void setClientX(int val)
	{
		_clientX = val;
	}

	public final void setClientY(int val)
	{
		_clientY = val;
	}

	public final void setClientZ(int val)
	{
		_clientZ = val;
	}

	public final void setClientHeading(int val)
	{
		_clientHeading = val;
	}

	public final int getXdestination()
	{
		MoveData m = _move;

		if (m != null)
			return m._xDestination;

		return getX();
	}

	/**
	 * Return the Y destination of the L2Character or the Y position if not in movement.<BR>
	 * <BR>
	 */
	public final int getYdestination()
	{
		MoveData m = _move;

		if (m != null)
			return m._yDestination;

		return getY();
	}

	/**
	 * Return the Z destination of the L2Character or the Z position if not in movement.<BR>
	 * <BR>
	 */
	public final int getZdestination()
	{
		MoveData m = _move;

		if (m != null)
			return m._zDestination;

		return getZ();
	}

	/**
	 * Return True if the L2Character is in combat.<BR>
	 * <BR>
	 */
	public boolean isInCombat()
	{
		return (getAI().getAttackTarget() != null || getAI().isAutoAttacking());
	}

	/**
	 * Return True if the L2Character is moving.<BR>
	 * <BR>
	 */
	public final boolean isMoving()
	{
		return _move != null;
	}

	/**
	 * Return True if the L2Character is casting.<BR>
	 * <BR>
	 */
	public final boolean isCastingNow()
	{
		return _isCastingNow;
	}

	public void setIsCastingNow(boolean value)
	{
		_isCastingNow = value;
	}

	public final boolean isCastingSimultaneouslyNow()
	{
		return _isCastingSimultaneouslyNow;
	}

	public void setIsCastingSimultaneouslyNow(boolean value)
	{
		_isCastingSimultaneouslyNow = value;
	}

	/**
	 * Return True if the cast of the L2Character can be aborted.<BR>
	 * <BR>
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getGameTicks();
	}

	/**
	 * Return True if the L2Character is attacking.<BR>
	 * <BR>
	 */
	public boolean isAttackingNow()
	{
		return getAttackEndTime() > L2System.milliTime();
	}

	/**
	 * Return True if the L2Character has aborted its attack.<BR>
	 * <BR>
	 */
	public final boolean isAttackAborted()
	{
		return _attacking <= 0;
	}

	/**
	 * Abort the attack of the L2Character and send Server->Client ActionFailed packet.<BR>
	 * <BR>
	 */
	public final void abortAttack()
	{
		if (isAttackingNow())
		{
			_attacking = 0;
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}

	/**
	 * Returns body part (paperdoll slot) we are targeting right now
	 */
	public final int getAttackingBodyPart()
	{
		return _attacking;
	}

	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR>
	 * <BR>
	 */
	public final void abortCast()
	{
		if (isCastingNow() || isCastingSimultaneouslyNow())
		{
			// cancels the skill hit scheduled task
			if (_skillCast != null)
			{
				_skillCast.cancel(false);
				_skillCast = null;
			}
			if (_skillCast2 != null)
			{
				_skillCast2.cancel(false);
				_skillCast2 = null;
			}

			if (getForceBuff() != null)
				getForceBuff().onCastAbort();

			L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
			if (mog != null)
				mog.exit();

			if (_allSkillsDisabled)
				enableAllSkills(); // this remains for forced skill use, e.g. scroll of escape
			setIsCastingNow(false);
			setIsCastingSimultaneouslyNow(false);
			// safeguard for cannot be interrupt any more
			_castInterruptTime = 0;
			if (this instanceof L2PcInstance)
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING); // setting back previous intention
			broadcastPacket(new MagicSkillCanceled(getObjectId())); // broadcast packet to stop animations client-side
			sendPacket(ActionFailed.STATIC_PACKET); // send an "action failed" packet to the caster
		}
	}

	/**
	 * Update the position of the L2Character during a movement and return True if the movement is finished.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character. The
	 * position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the L2Character position on the
	 * server. Note, that the current server position can differe from the current client position even if each movement is straight foward. That's why, client
	 * send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server. But, it's always the server position that is used in
	 * range calculation.<BR>
	 * <BR>
	 * At the end of the estimated movement time, the L2Character position is automatically set to the destination position even if the movement is not
	 * finished.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet. But x and y
	 * positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR>
	 * <BR>
	 *
	 * @param gameTicks
	 *            Nb of ticks since the server start
	 * @return True if the movement is finished
	 */
	public boolean updatePosition(int gameTicks)
	{
		// Get movement data
		MoveData m = _move;

		if (m == null)
			return true;

		if (!isVisible())
		{
			_move = null;
			return true;
		}

		// Check if this is the first update
		if (m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}

		// Check if the position has already been calculated
		if (m._moveTimestamp == gameTicks)
			return false;

		int xPrev = getX();
		int yPrev = getY();
		int zPrev = getZ(); // the z coordinate may be modified by coordinate synchronizations

		double dx, dy, dz, distFraction;
		if (Config.COORD_SYNCHRONIZE == 1)
		// the only method that can modify x,y while moving (otherwise _move would/should be set null)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else // otherwise we need saved temporary values to avoid rounding errors
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}
		// Z coordinate will follow geodata or client values
		if (Config.GEODATA>0 && Config.COORD_SYNCHRONIZE == 2
			&& !isFlying() && !isInsideZone(L2Zone.FLAG_WATER)
			&& !m.disregardingGeodata
			&& GameTimeController.getGameTicks() % 10 == 0
			&& !(this instanceof L2BoatInstance)) // once a second to reduce possible cpu load
		{
			short geoHeight = GeoData.getInstance().getSpawnHeight(xPrev, yPrev, zPrev-30, zPrev+30, getObjectId());
			dz = m._zDestination - geoHeight;
			// quite a big difference, compare to validatePosition packet
			if (this instanceof L2PcInstance && Math.abs(getClientZ() - geoHeight) > 200
					&& Math.abs(getClientZ() - geoHeight) < 1500)
			{
				dz = m._zDestination - zPrev; // allow diff
			}
			else if (isInCombat() && Math.abs(dz) > 200 && (dx*dx + dy*dy) < 40000) // allow mob to climb up to pcinstance
			{
				dz = m._zDestination - zPrev; // climbing
			}
			else
			{
				zPrev = geoHeight;
			}
		}
		else
			dz = m._zDestination - zPrev;

		double distPassed = getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / GameTimeController.TICKS_PER_SECOND;
		if ((dx*dx + dy*dy) < 10000 && (dz*dz > 2500)) // close enough, allows error between client and server geodata if it cannot be avoided
		{
			distFraction = distPassed / Math.sqrt(dx*dx + dy*dy);
		}
		else
			distFraction = distPassed / Math.sqrt(dx*dx + dy*dy + dz*dz);

		
		if (distFraction > 1) // already there
		{
			// Set the position of the L2Character to the destination
			super.getPosition().setXYZ(m._xDestination, m._yDestination, m._zDestination);
			if (this instanceof L2BoatInstance)
			{
				((L2BoatInstance) this).updatePeopleInTheBoat(m._xDestination, m._yDestination, m._zDestination);
			}
		}
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;

			// Set the position of the L2Character to estimated after parcial move
			super.getPosition().setXYZ((int)(m._xAccurate), (int)(m._yAccurate), zPrev + (int)(dz * distFraction + 0.5));
			if(this instanceof L2BoatInstance)
			{
				((L2BoatInstance)this).updatePeopleInTheBoat((int)(m._xAccurate), (int)(m._yAccurate), zPrev + (int)(dz * distFraction + 0.5));
			}
			else
			{
				revalidateZone(false);
			}
		}

		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;

		return (distFraction > 1);
	}

	public void revalidateZone(boolean force)
	{
		// This function is called very often from movement code
		if (force)
			_zoneValidateCounter = 4;
		else
		{
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0)
				_zoneValidateCounter = 4;
			else
				return;
		}

		if (getWorldRegion() == null)
			return;
		getWorldRegion().revalidateZones(this);
	}

	/**
	 * Stop movement of the L2Character (Called by AI Accessor only).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Delete movement data of the L2Character </li>
	 * <li>Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading </li>
	 * <li>Remove the L2Object object from _gmList** of GmListTable </li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters </li>
	 * <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet StopMove/StopRotation </B></FONT><BR>
	 * <BR>
	 */
	public void stopMove(L2CharPosition pos)
	{
		stopMove(pos, false);
	}

	public void stopMove(L2CharPosition pos, boolean updateKnownObjects)
	{
		// Delete movement data of the L2Character
		_move = null;

		// if (getAI() != null)
		// getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading
		// All data are contained in a L2CharPosition object
		if (pos != null)
		{
			getPosition().setXYZ(pos.x, pos.y, pos.z);
			setHeading(pos.heading);
			revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
		if (updateKnownObjects)
			getKnownList().updateKnownObjects();
	}

	/**
	 * @return Returns the showSummonAnimation.
	 */
	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}

	/**
	 * @param showSummonAnimation The showSummonAnimation to set.
	 */
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}

	/**
	 * Target a L2Object (add the target to the L2Character _target, _knownObject and L2Character to _KnownObject of the L2Object).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * The L2Object (including L2Character) targeted is identified in <B>_target</B> of the L2Character<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the _target of L2Character to L2Object </li>
	 * <li>If necessary, add L2Object to _knownObject of the L2Character </li>
	 * <li>If necessary, add L2Character to _KnownObject of the L2Object </li>
	 * <li>If object==null, cancel Attak or Cast </li>
	 * <BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance : Remove the L2PcInstance from the old target _statusListener and add it to the new target if it was a L2Character</li>
	 * <BR>
	 * <BR>
	 *
	 * @param object
	 *            L2object to target
	 */
	public void setTarget(L2Object object)
	{
		if (object != null && !object.isVisible())
			object = null;

		if (object != null && object != _target)
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}
		_target = object;
	}

	/**
	 * Return the identifier of the L2Object targeted or -1.<BR>
	 * <BR>
	 */
	public final int getTargetId()
	{
		if (_target != null)
		{
			return _target.getObjectId();
		}

		return -1;
	}

	/**
	 * Return the L2Object targeted or null.<BR>
	 * <BR>
	 */
	public final L2Object getTarget()
	{
		return _target;
	}

	// called from AIAccessor only
	/**
	 * Calculate movement data for a move to location action and add the L2Character to movingObjects of GameTimeController (only called by AI Accessor).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character. The
	 * position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR>
	 * <BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those L2Character
	 * each 0.1s.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Get current position of the L2Character </li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset </li>
	 * <li>Create and Init a MoveData object </li>
	 * <li>Set the L2Character _move object to MoveData object </li>
	 * <li>Add the L2Character to movingObjects of the GameTimeController </li>
	 * <li>Create a task to notify the AI that L2Character arrives at a check point of the movement </li>
	 * <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/MoveToLocation </B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li> AI : onIntentionMoveTo(L2CharPosition), onIntentionPickUp(L2Object), onIntentionInteract(L2Object) </li>
	 * <li> FollowTask </li>
	 * <BR>
	 * <BR>
	 *
	 * @param x
	 *            The X position of the destination
	 * @param y
	 *            The Y position of the destination
	 * @param z
	 *            The Y position of the destination
	 * @param offset
	 *            The size of the interaction area of the L2Character targeted
	 */
	protected void moveToLocation(int x, int y, int z, int offset)
	{
		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled()) return;

		// Get current position of the L2Character
		final int curX = super.getX();
		final int curY = super.getY();
		final int curZ = super.getZ();
		
		// Calculate distance (dx,dy) between current position and destination
        // TODO: improve Z axis move/follow support when dx,dy are small compared to dz
		double dx = (x - curX);
		double dy = (y - curY);
		double dz = (z - curZ);
		double distance = Math.sqrt(dx*dx + dy*dy);
		
		// make water move short and use no geodata checks for swimming chars
		// distance in a click can easily be over 3000
		if (Config.GEODATA > 0 && isInsideZone(L2Zone.FLAG_WATER) && distance > 700)
        {
			double divider = 700/distance;
        	x = curX + (int)(divider * dx);
        	y = curY + (int)(divider * dy);
        	z = curZ + (int)(divider * dz);
        	dx = (x - curX);
    		dy = (y - curY);
    		dz = (z - curZ);
    		distance = Math.sqrt(dx*dx + dy*dy);
        }

		if (_log.isDebugEnabled()) _log.info("distance to target:" + distance);

		// Define movement angles needed
		// ^
		// |     X (x,y)
		// |   /
		// |  /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)

		double cos;
		double sin;

		// Check if a movement offset is defined or no distance to go through
		if (offset > 0 || distance < 1)
		{
			// approximation for moving closer when z coordinates are different
			// TODO: handle Z axis movement better
			offset -= Math.abs(dz);
			if (offset < 5) offset = 5;

			// If no distance to go through, the movement is canceled
			if (distance < 1 || distance - offset  <= 0)
			{
				if (_log.isDebugEnabled()) _log.info("already in range, no movement needed.");

				// Notify the AI that the L2Character is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);

				return;
			}
			// Calculate movement angles needed
			sin = dy/distance;
			cos = dx/distance;

			distance -= (offset-5); // due to rounding error, we have to move a bit closer to be in range

			// Calculate the new destination with offset included
			x = curX + (int)(distance * cos);
			y = curY + (int)(distance * sin);

		}
		else
		{
			// Calculate movement angles needed
			sin = dy/distance;
			cos = dx/distance;
		}

		// Create and Init a MoveData object
		MoveData m = new MoveData();

		// GEODATA MOVEMENT CHECKS AND PATHFINDING
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m.disregardingGeodata = false;
		
		if (Config.GEODATA > 0
			&& !isFlying() // flying chars not checked - even canSeeTarget doesn't work yet
			&& (!isInsideZone(L2Zone.FLAG_WATER) || isInsideZone(L2Zone.FLAG_SIEGE)) // swimming also not checked unless in siege zone - but distance is limited
			&& !(this instanceof L2NpcWalkerInstance)) // npc walkers not checked
		{
			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = (originalX - L2World.MAP_MIN_X) >> 4;
			int gty = (originalY - L2World.MAP_MIN_Y) >> 4;

			// Movement checks:
			// when geodata == 2, for all characters except mobs returning home (could be changed later to teleport if pathfinding fails)
			// when geodata == 1, for l2playableinstance and l2riftinstance only
			if ((Config.GEODATA == 2 &&	!(this instanceof L2Attackable && ((L2Attackable)this).isReturningToSpawnPoint()))
					|| this instanceof L2PcInstance
					|| (this instanceof L2Summon && !(getAI().getIntention() == AI_INTENTION_FOLLOW)) // assuming intention_follow only when following owner
					|| isAfraid()
					|| this instanceof L2RiftInvaderInstance)
			{
				if (isOnGeodataPath())
				{
					try {
						if (gtx == _move.geoPathGtx && gty == _move.geoPathGty)
							return;
						else
							_move.onGeodataPathIndex = -1; // Set not on geodata path
					} catch (NullPointerException e)
					{
						// nothing
					}
				}
				
				if (curX < L2World.MAP_MIN_X || curX > L2World.MAP_MAX_X || curY < L2World.MAP_MIN_Y  || curY > L2World.MAP_MAX_Y)
				{
					// Temporary fix for character outside world region errors
					_log.warn("Character "+getName()+" outside world area, in coordinates x:"+curX+" y:"+curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if (this instanceof L2PcInstance)
						new Disconnection((L2PcInstance)this).defaultSequence(true);
					else if (!(this instanceof L2Summon))
						onDecay(); // preventation when summon get out of world coords, player will not loose it, unsummon handled from pcinstance
					return;
				}
				Location destiny = GeoData.getInstance().moveCheck(curX, curY, curZ, x, y, z);
				// location different if destination wasn't reached (or just z coord is different)
				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				distance = Math.sqrt((x - curX)*(x - curX) + (y - curY)*(y - curY));
				
			}
			// Pathfinding checks. Only when geodata setting is 2, the LoS check gives shorter result
			// than the original movement was and the LoS gives a shorter distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if(Config.GEODATA == 2 && originalDistance-distance > 100 && distance < 2000 && !isAfraid())
			{
				// Path calculation
				// Overrides previous movement check
				if(this instanceof L2PlayableInstance || isInCombat() || this instanceof L2MinionInstance)
				{
		
					m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ);
                	if (m.geoPath == null || m.geoPath.size() < 2) // No path found
                	{
                		// * Even though there's no path found (remember geonodes aren't perfect),
                		// the mob is attacking and right now we set it so that the mob will go
                		// after target anyway, is dz is small enough.
                		// * With cellpathfinding this approach could be changed but would require taking
                		// off the geonodes and some more checks.
                		// * Summons will follow their masters no matter what.
                		// * Currently minions also must move freely since L2AttackableAI commands
                		// them to move along with their leader
                		if (this instanceof L2PcInstance
                				|| (!(this instanceof L2PlayableInstance)
                						&& !(this instanceof L2MinionInstance)
                						&& Math.abs(z - curZ) > 140)
                				|| (this instanceof L2Summon && !((L2Summon)this).getFollowStatus()))
                		{
                			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                			return;
                		}

						m.disregardingGeodata = true;
						x = originalX;
						y = originalY;
						z = originalZ;
						distance = originalDistance;
                	}
                	else
                	{
                		m.onGeodataPathIndex = 0; // on first segment
                		m.geoPathGtx = gtx;
                		m.geoPathGty = gty;
                		m.geoPathAccurateTx = originalX;
                		m.geoPathAccurateTy = originalY;
				
                		x = m.geoPath.get(m.onGeodataPathIndex).getX();
                		y = m.geoPath.get(m.onGeodataPathIndex).getY();
                		z = m.geoPath.get(m.onGeodataPathIndex).getZ();
                		
                		// check for doors in the route
                		if (DoorTable.getInstance().checkIfDoorsBetween(curX, curY, curZ, x, y, z))
            			{
            				m.geoPath = null;
            				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            				return;
            			}
                		for (int i = 0; i < m.geoPath.size()-1; i++)
                		{
                			if (DoorTable.getInstance().checkIfDoorsBetween(m.geoPath.get(i),m.geoPath.get(i+1)))
                			{
                				m.geoPath = null;
                				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                				return;
                			}
                		}

                		dx = (x - curX);
                		dy = (y - curY);
                		distance = Math.sqrt(dx*dx + dy*dy);
                		sin = dy/distance;
                		cos = dx/distance;
                	}
				}
			}
			// If no distance to go through, the movement is canceled
			if (distance < 1 && (Config.GEODATA == 2
					|| this instanceof L2PlayableInstance
					|| isAfraid()
					|| this instanceof L2RiftInvaderInstance))
			{
				if (this instanceof L2Summon)
					((L2Summon) this).setFollowStatus(false);
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return;
			}
		}

		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1+(int)(GameTimeController.TICKS_PER_SECOND * distance / speed);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client
		
		// Calculate and set the heading of the L2Character
		m._heading = 0; // initial value for coordinate sync
		setHeading(Util.calculateHeadingFrom(cos, sin));
		
		if (_log.isDebugEnabled())
			_log.info("dist:"+ distance +"speed:" + speed + " ttt:" + ticksToMove +
			          " heading:" + getHeading());

		m._moveStartTime = GameTimeController.getGameTicks();

		// Set the L2Character _move object to MoveData object
		_move = m;

		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.registerMovingChar(this);

		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if (ticksToMove*GameTimeController.MILLIS_IN_TICK > 3000)
			ThreadPoolManager.getInstance().scheduleAi( new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
	}

	public boolean moveToNextRoutePoint()
	{
		if (!isOnGeodataPath())
		{
			// Cancel the move action
			_move = null;
			return false;
		}

		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if (speed <= 0 || isMovementDisabled())
		{
			// Cancel the move action
			_move = null;
			return false;
		}

		// Create and Init a MoveData object
		MoveData m = new MoveData();
		MoveData md = _move;
		if (md == null)
			return false;

		// Update MoveData object
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1; // next segment
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;
		m.geoPathAccurateTx = md.geoPathAccurateTx;
		m.geoPathAccurateTy = md.geoPathAccurateTy;

		if (md.onGeodataPathIndex == md.geoPath.size()-2)
		{
			m._xDestination = md.geoPathAccurateTx;
			m._yDestination = md.geoPathAccurateTy;
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		else
		{
			m._xDestination = md.geoPath.get(m.onGeodataPathIndex).getX();
			m._yDestination = md.geoPath.get(m.onGeodataPathIndex).getY();
			m._zDestination = md.geoPath.get(m.onGeodataPathIndex).getZ();
		}
		double dx = (m._xDestination - super.getX());
		double dy = (m._yDestination - super.getY());

		double distance = Math.sqrt(dx * dx + dy * dy);
		double sin = dy / distance;
		double cos = dx / distance;

		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int)(GameTimeController.TICKS_PER_SECOND * distance / speed);

		// Calculate and set the heading of the L2Character
		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378);
		heading += 32768;
		setHeading(heading);
		m._heading = 0; // initial value for coordinate sync

		m._moveStartTime = GameTimeController.getGameTicks();

		if (_log.isDebugEnabled())
			_log.info("time to target:" + ticksToMove);

		// Set the L2Character _move object to MoveData object
		_move = m;

		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.registerMovingChar(this);

		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if (ticksToMove * GameTimeController.MILLIS_IN_TICK > 3000)
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController

		// Send a Server->Client packet MoveToLocation to the actor and all L2PcInstance in its _knownPlayers
		MoveToLocation msg = new MoveToLocation(this);
		broadcastPacket(msg);

		return true;
	}

	public boolean validateMovementHeading(int heading)
	{
		MoveData md = _move;
		if (md == null)
			return true;

		boolean result = true;
		// if (_move._heading < heading - 5 || _move._heading > heading 5)
		if (md._heading != heading)
		{
			result = (md._heading == 0);
			md._heading = heading;
		}

		return result;
	}

	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();

		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public final double getDistance(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Return the squared distance between the current position of the L2Character and the given object.<BR>
	 * <BR>
	 *
	 * @param object
	 *            L2Object
	 * @return the squared distance
	 */
	public final double getDistanceSq(L2Object object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}

	/**
	 * Return the squared distance between the current position of the L2Character and the given x, y, z.<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @param z
	 *            Z position of the target
	 * @return the squared distance
	 */
	public final double getDistanceSq(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		return (dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Return the squared plan distance between the current position of the L2Character and the given object.<BR>
	 * (check only x and y, not z)<BR>
	 * <BR>
	 *
	 * @param object
	 *            L2Object
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(L2Object object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}

	/**
	 * Return the squared plan distance between the current position of the L2Character and the given x, y, z.<BR>
	 * (check only x and y, not z)<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @return the squared plan distance
	 */
	public final double getPlanDistanceSq(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();

		return (dx * dx + dy * dy);
	}

	/**
	 * Check if this object is inside the given radius around the given object. Warning: doesn't cover collision radius!<BR>
	 * <BR>
	 * If the target is null, we consider that this object is not inside radius
	 *
	 * @param object
	 *            the target
	 * @param radius
	 *            the radius around the target
	 * @param checkZ
	 *            should we check Z axis also
	 * @param strictCheck
	 *            true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
	{
		if (object == null)
			return false;

		return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
	}

	/**
	 * Check if this object is inside the given plan radius around the given point. Warning: doesn't cover collision radius!<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @param radius
	 *            the radius around the target
	 * @param strictCheck
	 *            true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}

	/**
	 * Check if this object is inside the given radius around the given point.<BR>
	 * <BR>
	 *
	 * @param x
	 *            X position of the target
	 * @param y
	 *            Y position of the target
	 * @param z
	 *            Z position of the target
	 * @param radius
	 *            the radius around the target
	 * @param checkZ
	 *            should we check Z axis also
	 * @param strictCheck
	 *            true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public final boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		if (strictCheck)
		{
			if (checkZ)
				return (dx * dx + dy * dy + dz * dz) < radius * radius;

			return (dx * dx + dy * dy) < radius * radius;
		}

		if (checkZ)
			return (dx * dx + dy * dy + dz * dz) <= radius * radius;

		return (dx * dx + dy * dy) <= radius * radius;
	}

	/**
	 * Return the Weapon Expertise Penalty of the L2Character.<BR>
	 * <BR>
	 */
	public float getWeaponExpertisePenalty()
	{
		return 1.f;
	}

	/**
	 * Return the Armour Expertise Penalty of the L2Character.<BR>
	 * <BR>
	 */
	public float getArmourExpertisePenalty()
	{
		return 1.f;
	}

	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.<BR>
	 * <BR>
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}

	/**
	 * Retun True if arrows are available.<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance</li>
	 * <BR>
	 * <BR>
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}

	/**
	* Retun True if bolts are available.<BR><BR>
	*
	* <B><U> Overridden in </U> :</B><BR><BR>
	* <li> L2PcInstance</li><BR><BR>
	*
	*/
	protected boolean checkAndEquipBolts()
	{
		return true;
	}

	/**
	 * Add Exp and Sp to the L2Character.<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance</li>
	 * <li> L2PetInstance</li>
	 * <BR>
	 * <BR>
	 */
	public void addExpAndSp(@SuppressWarnings("unused")
	long addToExp, @SuppressWarnings("unused")
	int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}

	/**
	 * Return the active weapon instance (always equipped in the right hand).<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance</li>
	 * <BR>
	 * <BR>
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();

	/**
	 * Return the active weapon item (always equipped in the right hand).<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance</li>
	 * <BR>
	 * <BR>
	 */
	public abstract L2Weapon getActiveWeaponItem();

	/**
	 * Return the secondary weapon instance (always equipped in the left hand).<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance</li>
	 * <BR>
	 * <BR>
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();

	/**
	 * Return the secondary weapon item (always equipped in the left hand).<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance</li>
	 * <BR>
	 * <BR>
	 */
	public abstract L2Weapon getSecondaryWeaponItem();

	/**
	 * Manage hit process (called by Hit Task).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a
	 * L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li>
	 * <BR>
	 * <BR>
	 *
	 * @param target
	 *            The L2Character targeted
	 * @param damage
	 *            Nb of HP to reduce
	 * @param crit
	 *            True if hit is critical
	 * @param miss
	 *            True if hit is missed
	 * @param soulshot
	 *            True if SoulShot are charged
	 * @param shld
	 *            True if shield is efficient
	 */
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)
		if (target == null || isAlikeDead() || (this instanceof L2NpcInstance && ((L2NpcInstance) this).isEventMob))
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		if ((this instanceof L2NpcInstance && target.isAlikeDead()) || target.isDead()
				|| (!getKnownList().knowsObject(target) && !(this instanceof L2DoorInstance)))
		{
			// getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);

			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (miss)
		{
			// ON_EVADED_HIT
			if (target.getChanceSkills() != null)
				target.getChanceSkills().onEvadedHit(this);

			if (target instanceof L2PcInstance)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_S1S_ATTACK);
				sm.addCharName(this);
				target.sendPacket(sm);
			}
		}

		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance
		if (!isAttackAborted())
		{
			// Check Raidboss attack
			// Character will be petrified if attacking a raid that's more
			// than 8 levels lower
			if (target.isRaid() && !Config.ALT_DISABLE_RAIDBOSS_PETRIFICATION)
			{
				int level = 0;
				if (this instanceof L2PcInstance)
					level = getLevel();
				else if (this instanceof L2Summon)
					level = ((L2Summon) this).getOwner().getLevel();

				if (level > target.getLevel() + 8)
				{
					L2Skill skill = SkillTable.getInstance().getInfo(4515, 1);

					if (skill != null)
						skill.getEffects(target, this);
					else
						_log.warn("Skill 4515 at level 1 is missing in DP.");

					damage = 0; // prevents messing up drop calculation
				}
			}

			sendDamageMessage(target, damage, false, crit, miss);

			// If L2Character target is a L2PcInstance, send a system message
			if (target instanceof L2PcInstance)
			{
				L2PcInstance enemy = (L2PcInstance) target;
				enemy.getAI().clientStartAutoAttack();

				// Check if shield is efficient
				/*if (shld && 100 - Config.ALT_PERFECT_SHLD_BLOCK < Rnd.get(100))
					enemy.sendPacket(new SystemMessage(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL));
				// else if (!miss && damage < 1)
				// enemy.sendMessage("You hit the target's armor.");*/
			}
			else if (target instanceof L2Summon)
			{
				((L2Summon) target).getOwner().getAI().clientStartAutoAttack();
			}

			if (!miss && damage > 0)
			{
				L2Weapon weapon = getActiveWeaponItem();
				boolean isRangeWeapon = (weapon != null && (weapon.getItemType() == L2WeaponType.BOW || weapon.getItemType() == L2WeaponType.CROSSBOW));

				int reflectedDamage = 0;
				if (!isRangeWeapon) // Do not reflect if weapon is of type bow
				{
					// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
					double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);

					if (reflectPercent > 0)
					{
						reflectedDamage = (int) (reflectPercent / 100. * damage);
						damage -= reflectedDamage;

						if (reflectedDamage > target.getMaxHp()) // to prevent extreme damage when hitting a low lvl char...
							reflectedDamage = target.getMaxHp();
					}
				}

				// Reduce targets HP
				target.reduceCurrentHp(damage, this, null);

				if (reflectedDamage > 0)
				{
					reduceCurrentHp(reflectedDamage, target, true, false, null);

					// Custom messages - nice but also more network load
					/*
					 * if (target instanceof L2PcInstance) ((L2PcInstance)target).sendMessage("You reflected " + reflectedDamage + " damage."); else if
					 * (target instanceof L2Summon) ((L2Summon)target).getOwner().sendMessage("Summon reflected " + reflectedDamage + " damage.");
					 *
					 * if (this instanceof L2PcInstance) ((L2PcInstance)this).sendMessage("Target reflected to you " + reflectedDamage + " damage."); else
					 * if (this instanceof L2Summon) ((L2Summon)this).getOwner().sendMessage("Target reflected to your summon " + reflectedDamage + "
					 * damage.");
					 */
				}

				if (!isRangeWeapon) // Do not absorb if weapon is of type bow
				{
					// Absorb HP from the damage inflicted
					double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);

					if (absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxHp() - getStatus().getCurrentHp());
						int absorbDamage = (int) (absorbPercent / 100. * damage);

						if (absorbDamage > maxCanAbsorb)
							absorbDamage = maxCanAbsorb; // Can't absorb more than max hp

						if (absorbDamage > 0)
						{
							getStatus().increaseHp(absorbDamage);
						}
					}

					// Absorb CP from the damage inflicted
					double absorbCPPercent = getStat().calcStat(Stats.ABSORB_CP_PERCENT, 0, null, null);

					if (absorbCPPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxCp() - getStatus().getCurrentCp());
						int absorbDamage = (int) (absorbCPPercent / 100. * damage);

						if (absorbDamage > maxCanAbsorb)
							absorbDamage = maxCanAbsorb; // Can't absorb more than max cp

						getStatus().setCurrentCp(getStatus().getCurrentCp() + absorbDamage);
					}
				}

				// Notify AI with EVT_ATTACKED
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				getAI().clientStartAutoAttack();
				if (this instanceof L2Summon)
				{
					L2PcInstance owner = ((L2Summon)this).getOwner();
					if (owner != null)
					{
						owner.getAI().clientStartAutoAttack();
					}
				}

				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}

				// Maybe launch chance skills on us
				if (_chanceSkills != null)
					_chanceSkills.onHit(target, false, crit);

				// Maybe launch chance skills on target
				if (target.getChanceSkills() != null)
					target.getChanceSkills().onHit(this, true, crit);

				// Launch weapon Special ability effect if available
				L2Weapon activeWeapon = getActiveWeaponItem();

				if (activeWeapon != null && crit)
					activeWeapon.getSkillEffectsByCrit(this, target);
			}
			return;
		}

		if (!isCastingNow() && !isCastingSimultaneouslyNow())
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
	}

	/**
	 * Break an attack and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR>
	 * <BR>
	 */
	public void breakAttack()
	{
		if (isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client ActionFailed packet
			abortAttack();

			if (this instanceof L2PcInstance)
			{
				sendPacket(ActionFailed.STATIC_PACKET);

				// Send a system message
				sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
			}
		}
	}

	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.<BR>
	 * <BR>
	 */
	public void breakCast()
	{
		// damage can only cancel magical skills
		if (isCastingNow() && canAbortCast())
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceld/ActionFailed packet.
			abortCast();

			if (this instanceof L2PcInstance)
			{
				// Send a system message
				sendPacket(new SystemMessage(SystemMessageId.CASTING_INTERRUPTED));
			}
		}
	}

	/**
	 * Reduce the arrow number of the L2Character.<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance</li>
	 * <BR>
	 * <BR>
	 * @param bolts
	 */
	protected void reduceArrowCount(boolean bolts)
	{
		// default is to do nothin
	}

	/**
	 * Manage Forced attack (shift + select target).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If L2Character or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed </li>
	 * <li>If target is confused, send a Server->Client packet ActionFailed </li>
	 * <li>If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed </li>
	 * <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK </li>
	 * <BR>
	 * <BR>
	 *
	 * @param player
	 *            The L2PcInstance to attack
	 */
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		if (player.getTarget() == null || !(player.getTarget() instanceof L2Character))
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2Character target = (L2Character) player.getTarget();

		if (isInsidePeaceZone(player))
		{
			if (!player.isInFunEvent() || !target.isInFunEvent())
			{
				// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
				player.sendPacket(new SystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		if (player.isInOlympiadMode() && target instanceof L2PlayableInstance)
		{
			L2PcInstance ptarget = target.getActingPlayer();

			if ((ptarget.isInOlympiadMode() && !player.isOlympiadStart()) || (player.getOlympiadGameId() != ptarget.getOlympiadGameId()))
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}

		if (!target.isAttackable() && (player.getAccessLevel() < Config.GM_PEACEATTACK))
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (player.isConfused())
		{
			// If target is confused, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (this instanceof L2ArtefactInstance)
		{
			// If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// GeoData Los Check or dz > 1000
		if (!GeoData.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANT_SEE_TARGET));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		// Notify AI with AI_INTENTION_ATTACK
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
	}

	/**
	 * Return True if inside peace zone.<BR>
	 * <BR>
	 */
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		if (!isInFunEvent() || !attacker.isInFunEvent())
		{
			return isInsidePeaceZone(attacker, this);
		}
		return false;
	}

	public static boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
	{
		return ((attacker.getAccessLevel() < Config.GM_PEACEATTACK) && isInsidePeaceZone((L2Object) attacker, target));
	}

	public static boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if (target == null)
			return false;
		if (!(target instanceof L2PlayableInstance && attacker instanceof L2PlayableInstance))
			return false;

		if (Config.ALT_GAME_KARMA_PLAYER_CAN_BE_KILLED_IN_PEACEZONE)
		{
			// allows red to be attacked and red to attack flagged players
			if (target.getActingPlayer() != null && target.getActingPlayer().getKarma() > 0)
				return false;
			if (attacker.getActingPlayer() != null && attacker.getActingPlayer().getKarma() > 0 && target.getActingPlayer() != null
					&& target.getActingPlayer().getPvpFlag() > 0)
				return false;
		}

		return (((L2Character) attacker).isInsideZone(L2Zone.FLAG_PEACE) || ((L2Character) target).isInsideZone(L2Zone.FLAG_PEACE));
	}

	/**
	 * return true if this character is inside an active grid.
	 */
	public boolean isInActiveRegion()
	{
		L2WorldRegion region = getWorldRegion();
		return ((region != null) && (region.isActive()));
	}

	/**
	 * Return True if the L2Character has a Party in progress.<BR>
	 * <BR>
	 */
	public boolean isInParty()
	{
		return false;
	}

	/**
	 * Return the L2Party object of the L2Character.<BR>
	 * <BR>
	 */
	public L2Party getParty()
	{
		return null;
	}

	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).<BR>
	 * <BR>
	 * @param target
	 * @param weapon
	 */
	public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
	{
		return Formulas.calcPAtkSpd(this, target, getPAtkSpd(), 500000);
	}

	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		// Source L2P
		// Standing still and with no SA, normal bows and yumi bows shoot the exact same number of shots per second.
		// Normal Bows allow faster use of skills and more kiteability due to having a higher Atk. Spd. while Yumi Bows have significant P.Atk.
		// The SA "Quick Recovery" reduces the red bar Weapon Delay on a bow to the following:
		// Reuse goes from 639 EB QR, to 1500 Normal...

		if (weapon == null || (this instanceof L2PcInstance && ((L2PcInstance) this).isTransformed()))
			return 0;

		double reuse = weapon.getAttackReuseDelay();

		if (reuse == 0)
			return 0;

		reuse = getBowReuse(reuse) * 333;

		return Formulas.calcPAtkSpd(this, target, getPAtkSpd(), reuse);
	}

	/** Return the bow reuse time. */
	public final double getBowReuse(double reuse)
	{
		return calcStat(Stats.BOW_REUSE, reuse, null, null);
	}

	/**
	 * Return True if the L2Character use a dual weapon.<BR>
	 * <BR>
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}

	/**
	 * Add a skill to the L2Character _skills and its Func objects to the calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li>
	 * <BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li>
	 * <BR>
	 * <BR>
	 *
	 * @param newSkill
	 *            The L2Skill to add to the L2Character
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;

		if (newSkill != null)
		{
			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);

			// If an old skill has been replaced, remove all its Func objects
			if (oldSkill != null)
			{
				// if skill came with another one, we should delete the other one too.
				if((oldSkill.bestowTriggered() || oldSkill.triggerAnotherSkill()) && oldSkill.getTriggeredId() > 0 )
				{
					removeSkill(oldSkill.getTriggeredId(), true);
				}
				removeStatsOwner(oldSkill);
			}

			// Add Func objects of newSkill to the calculator set of the L2Character
			if (newSkill.getSkillType() != L2SkillType.NOTDONE)
				addStatFuncs(newSkill.getStatFuncs(null, this));

			try
			{
				if (newSkill.getElement() != 0)
				{
					getStat().addElement(newSkill);
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}

			if (oldSkill != null && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			if (newSkill.isChance())
			{
				addChanceSkill(newSkill);
			}

			if (!newSkill.isChance() && newSkill.getTriggeredId() > 0 && newSkill.bestowTriggered())
			{
				L2Skill bestowed = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(), newSkill.getTriggeredLevel());
				addSkill(bestowed);
				//bestowed skills are invisible for player. Visible for gm's looking thru gm window.
				//those skills should always be chance or passive, to prevent hlapex.
			}

			if(newSkill.isChance() && newSkill.getTriggeredId() > 0 && !newSkill.bestowTriggered() && newSkill.triggerAnotherSkill())
			{
				L2Skill triggeredSkill = SkillTable.getInstance().getInfo(newSkill.getTriggeredId(),newSkill.getTriggeredLevel());
				addSkill(triggeredSkill);
			}
		}

		return oldSkill;
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li>
	 * <BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li>
	 * <BR>
	 * <BR>
	 *
	 * @param skill
	 *            The L2Skill to remove from the L2Character
	 * @return The L2Skill removed
	 */
	public L2Skill removeSkill(L2Skill skill)
	{
		if (skill == null)
			return null;

		return removeSkill(skill.getId(), true);
	}

	public L2Skill removeSkill(L2Skill skill, @SuppressWarnings("unused") boolean cancelEffect)
	{
		if (skill == null)
			return null;

		// Remove the skill from the L2Character _skills
		return removeSkill(skill.getId());
	}

	public L2Skill removeSkill(int skillId)
	{
		return removeSkill(skillId, true);
	}

	public L2Skill removeSkill(int skillId, boolean cancelEffect)
	{
		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = _skills.remove(skillId);

		// Remove all its Func objects from the L2Character calculator set
		if (oldSkill != null)
		{
			try
			{
				if (oldSkill.getElement() != 0)
				{
					getStat().removeElement(oldSkill);
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}

			//this is just a fail-safe againts buggers and gm dummies...
			if ((oldSkill.bestowTriggered() || oldSkill.triggerAnotherSkill()) && oldSkill.getTriggeredId() > 0)
			{
				removeSkill(oldSkill.getTriggeredId(), true);
			}
			// Stop casting if this skill is used right now
			if (this instanceof L2PcInstance)
			{
				if (((L2PcInstance) this).getCurrentSkill() != null && isCastingNow())
				{
					if (oldSkill.getId() == ((L2PcInstance) this).getCurrentSkill().getSkillId())
						abortCast();
				}
			}
			if (getLastSimultaneousSkillCast() != null && isCastingSimultaneouslyNow())
			{
				if (oldSkill.getId() == getLastSimultaneousSkillCast().getId())
					abortCast();
			}

			if (cancelEffect || oldSkill.isToggle())
			{
				// for now, to support transformations, we have to let their
				// effects stay when skill is removed
				L2Effect e = getFirstEffect(oldSkill);
				if (e == null || e.getEffectType() != L2EffectType.TRANSFORMATION)
				{
					removeStatsOwner(oldSkill);
					stopSkillEffects(oldSkill.getId());
				}
			}

			if (oldSkill instanceof L2SkillAgathion && this instanceof L2PcInstance && ((L2PcInstance) this).getAgathionId() > 0)
			{
				((L2PcInstance) this).setAgathionId(0);
				((L2PcInstance) this).broadcastUserInfo();
			}

			if (oldSkill.isChance() && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}

			if (oldSkill instanceof L2SkillMount && this instanceof L2PcInstance && ((L2PcInstance)this).isMounted())
			{
				((L2PcInstance)this).dismount();
			}
			if (oldSkill instanceof L2SkillSummon && oldSkill.getId() == 710 && this instanceof L2PcInstance && ((L2PcInstance)this).getPet() != null && ((L2PcInstance)this).getPet().getNpcId() == 14870)
			{
				((L2PcInstance)this).getPet().unSummon(((L2PcInstance)this));
			}
		}

		return oldSkill;
	}

	public synchronized void addChanceSkill(L2Skill skill)
	{
		if (_chanceSkills == null)
			_chanceSkills = new ChanceSkillList(this);
		_chanceSkills.put(skill, skill.getChanceCondition());
	}

	public synchronized void removeChanceSkill(int id)
	{
		for (L2Skill skill : _chanceSkills.keySet())
		{
			if (skill.getId() == id)
				_chanceSkills.remove(skill);
		}
		if (_chanceSkills.size() == 0)
			_chanceSkills = null;
	}

	/**
	 * Return all skills own by the L2Character in a table of L2Skill.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills own by a L2Character are identified in <B>_skills</B> the L2Character <BR>
	 * <BR>
	 */
	public final L2Skill[] getAllSkills()
	{
		if (_skills == null)
			return new L2Skill[0];

		return _skills.values().toArray(new L2Skill[_skills.values().size()]);
	}

	/**
	 * Return the level of a skill owned by the L2Character.<BR>
	 * <BR>
	 *
	 * @param skillId
	 *            The identifier of the L2Skill whose level must be returned
	 * @return The level of the L2Skill identified by skillId
	 */
	public int getSkillLevel(int skillId)
	{
		if (_skills == null)
			return -1;

		L2Skill skill = _skills.get(skillId);

		if (skill == null)
			return -1;
		return skill.getLevel();
	}

	/**
	 * Return True if the skill is known by the L2Character.<BR>
	 * <BR>
	 *
	 * @param skillId
	 *            The identifier of the L2Skill to check the knowledge
	 */
	public final L2Skill getKnownSkill(int skillId)
	{
		if (_skills == null)
			return null;
		
		return _skills.get(skillId);
	}
	
	public final boolean hasSkill(int skillId)
	{
		return getKnownSkill(skillId) != null;
	}

	/**
	 * Return the number of buffs affecting this L2Character.<BR><BR>
	 *
	 * @return The number of Buffs affecting this L2Character
	 */
	public int getBuffCount()
	{
		return _effects.getBuffCount();
	}

	public int getDanceCount(boolean dances, boolean songs)
	{
		return _effects.getDanceCount(dances, songs);
	}

	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all L2PcInstance of L2Charcater _knownPlayers</li>
	 * <li>Consume MP, HP and Item if necessary</li>
	 * <li>Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK</li>
	 * <li>Notify the AI of the L2Character with EVT_FINISH_CASTING</li>
	 * <BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT><BR>
	 * <BR>
	 *
	 * @param skill
	 *            The L2Skill to use
	 */
	public void onMagicLaunchedTimer(L2Character[] targets, L2Skill skill, int coolTime, boolean instant, boolean simultaneously)
	{
		if (skill == null || targets == null || targets.length <= 0)
		{
			abortCast();
			setAttackingChar(null);
			return;
		}

		if (skill.getSkillType() == L2SkillType.NOTDONE)
		{
			abortCast();
			return;
		}

		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
			escapeRange = skill.getEffectRange();
		else if (skill.getCastRange() < 0 && skill.getSkillRadius() > 80)
			escapeRange = skill.getSkillRadius();

		if (escapeRange > 0)
		{
			List<L2Character> targetList = new FastList<L2Character>();
			for (L2Object element : targets)
			{
				if (element instanceof L2Character)
				{
					if ((!Util.checkIfInRange(escapeRange, this, element, true) || !GeoData.getInstance().canSeeTarget(this, element)))
						continue;
					if (skill.isOffensive() && !skill.isNeutral())
					{
						if (this instanceof L2PcInstance)
						{
							if (((L2Character) element).isInsidePeaceZone((L2PcInstance) this))
								continue;
						}
						else
						{
							if (L2Character.isInsidePeaceZone(this, element))
								continue;
						}
					}
					targetList.add((L2Character) element);
				}
			}
			if (targetList.isEmpty())
			{
				abortCast();
				return;
			}

			targets = targetList.toArray(new L2Character[targetList.size()]);
		}

		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Potions can be used while faking death.
		if ((simultaneously && !isCastingSimultaneouslyNow())
				|| (!simultaneously && !isCastingNow())
				|| (isAlikeDead() && !skill.isPotion()))
		{
			// now cancels both, simultaneous and normal
			setAttackingChar(null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);

			_castInterruptTime = 0;
			return;
		}

		// Get the display identifier of the skill
		int magicId = skill.getDisplayId();

		// Get the level of the skill
		int level = getSkillLevel(skill.getId());

		if (level < 1)
			level = 1;

		// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if (!skill.isPotion())
			broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));

		if (instant)
			onMagicHitTimer(targets, skill, coolTime, true, simultaneously);
		else
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 2, simultaneously), 200);
	}

	/*
	 * Runs in the end of skill casting
	 */
	public void onMagicHitTimer(L2Character[] targets, L2Skill skill, int coolTime, boolean instant, boolean simultaneously)
	{
		if (skill == null || targets == null || targets.length == 0)
		{
			abortCast();
			setAttackingChar(null);
			return;
		}
		if (getForceBuff() != null)
		{
			if (simultaneously)
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			notifyQuestEventSkillFinished(skill, targets[0]);
			getForceBuff().onCastAbort();
			return;
		}

		L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
		if (mog != null)
		{
			if (simultaneously)
			{
				_skillCast2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_skillCast = null;
				setIsCastingNow(false);
			}
			mog.exit();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}

		try
		{
			for (L2Object element : targets)
			{
				if (element instanceof L2PlayableInstance)
				{
					L2Character target = (L2Character) element;

					if (skill.getSkillType() == L2SkillType.BUFF)
					{
						SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
						smsg.addSkillName(skill);
						target.sendPacket(smsg);
					}

					if (this instanceof L2PcInstance && target instanceof L2Summon)
					{
						((L2Summon)target).broadcastFullInfo();
					}
				}
			}

			StatusUpdate su = new StatusUpdate(getObjectId());
			boolean isSendStatus = false;

			// Consume MP of the L2Character and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			double mpConsume = getStat().getMpConsume(skill);

			if (mpConsume > 0)
			{
				if (skill.isDance())
				{
					getStatus().reduceMp(calcStat(Stats.DANCE_CONSUME_RATE, mpConsume, null, null));
				}
				else if (skill.isMagic())
				{
					getStatus().reduceMp(calcStat(Stats.MAGIC_CONSUME_RATE, mpConsume, null, null));
				}
				else
				{
					getStatus().reduceMp(calcStat(Stats.PHYSICAL_CONSUME_RATE, mpConsume, null, null));
				}
				su.addAttribute(StatusUpdate.CUR_MP, (int) getStatus().getCurrentMp());
				isSendStatus = true;
			}

			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if (skill.getHpConsume() > 0)
			{
				double consumeHp;

				consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
				if (consumeHp + 1 >= getStatus().getCurrentHp())
					consumeHp = getStatus().getCurrentHp() - 1.0;

				getStatus().reduceHp(consumeHp, this);

				su.addAttribute(StatusUpdate.CUR_HP, (int) getStatus().getCurrentHp());
				isSendStatus = true;
			}

			// Consume CP if necessary and Send the Server->Client packet StatusUpdate with current CP/HP and MP to all other L2PcInstance to inform
			if (skill.getCpConsume() > 0)
			{
				double consumeCp;

				consumeCp = skill.getCpConsume();
				if (consumeCp + 1 >= getStatus().getCurrentHp())
					consumeCp = getStatus().getCurrentHp() - 1.0;

				getStatus().reduceCp((int) consumeCp);

				su.addAttribute(StatusUpdate.CUR_CP, (int) getStatus().getCurrentCp());
				isSendStatus = true;
			}

			// Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance
			if (isSendStatus)
				sendPacket(su);

			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if (skill.getItemConsume() > 0)
			{
				if (!destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, false))
				{
					sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					abortCast();
					return;
				}
			}

			if (this instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) this;
				// Reset soul bonus for skills
				player.resetLastSoulConsume();

				// Consume Souls if necessary
				if (skill.getSoulConsumeCount() > 0 || skill.getMaxSoulConsumeCount() > 0)
				{
					player.decreaseSouls(skill);
				}

				// Consume Charges if necessary ... L2SkillChargeDmg does the consume by itself.
				if (skill.getNeededCharges() > 0 && !(skill instanceof L2SkillChargeDmg))
				{
					player.decreaseCharges(skill.getNeededCharges());
				}
			}

			// Launch the magic skill in order to calculate its effects
			callSkill(skill, targets);
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
		}

		if (instant || coolTime == 0)
			onMagicFinalizer(skill, targets[0], simultaneously);
		else
		{
			if (simultaneously)
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3, simultaneously), coolTime);
			else
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(new MagicUseTask(targets, skill, coolTime, 3, simultaneously), coolTime);
		}
	}

	/*
	 * Runs after skill hitTime+coolTime
	 */
	public void onMagicFinalizer(L2Skill skill, L2Object target, boolean simultaneously)
	{
		if (simultaneously)
		{
			_skillCast2 = null;
			setIsCastingSimultaneouslyNow(false);
			return;
		}

		_skillCast = null;
		setIsCastingNow(false);
		_castInterruptTime = 0;

		// if the skill has changed the character's state to something other than STATE_CASTING
		// then just leave it that way, otherwise switch back to STATE_IDLE.
		// if(isCastingNow())
		// getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);

		switch (skill.getSkillType())
		{
		case PDAM:
		case BLOW:
		case CHARGEDAM:
		case SPOIL:
			// case SOW: case DRAIN_SOUL: // Soul Crystal casting
			if (getTarget() instanceof L2Character && getTarget() != this && target == getTarget())
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getTarget());
			break;
		}

		if (skill.isOffensive() && !skill.isNeutral() && skill.getSkillType() != L2SkillType.UNLOCK
				&& skill.getSkillType() != L2SkillType.DELUXE_KEY_UNLOCK
				&& skill.getSkillType() != L2SkillType.MAKE_KILLABLE
				&& skill.getSkillType() != L2SkillType.MAKE_QUEST_DROPABLE)
			getAI().clientStartAutoAttack();

		// Notify the AI of the L2Character with EVT_FINISH_CASTING
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);

		notifyQuestEventSkillFinished(skill, target);

		/*
		 * If character is a player, then wipe their current cast state and check if a skill is queued.
		 *
		 * If there is a queued skill, launch it and wipe the queue.
		 */
		if (this instanceof L2PcInstance)
		{
			L2PcInstance currPlayer = (L2PcInstance) this;
			SkillDat queuedSkill = currPlayer.getQueuedSkill();

			// Rescuing old skill cast task if exist
			//if (skill.isPotion())
			//queuedSkill = currPlayer.getCurrentSkill();

			currPlayer.setCurrentSkill(null, false, false);

			if (queuedSkill != null)
			{
				currPlayer.setQueuedSkill(null, false, false);

				// DON'T USE : Recursive call to useMagic() method
				// currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
				ThreadPoolManager.getInstance().executeTask(
						new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
			}
		}
	}

	// Quest event ON_SPELL_FNISHED
	private void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{
		if (this instanceof L2NpcInstance)
		{
			try
			{
				if (((L2NpcTemplate) getTemplate()).getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED) != null)
				{
					L2PcInstance player = target.getActingPlayer();
					for (Quest quest: ((L2NpcTemplate) getTemplate()).getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED))
					{
						quest.notifySpellFinished(((L2NpcInstance)this), player, skill);
					}
				}
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Enable a skill (remove it from _disabledSkills of the L2Character).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 *
	 * @param skillId
	 *            The identifier of the L2Skill to enable
	 */
	public void enableSkill(int skillId)
	{
		if (_disabledSkills == null)
			return;

		_disabledSkills.remove(Integer.valueOf(skillId));

		if (this instanceof L2PcInstance)
			removeTimeStamp(skillId);
	}

	/**
	 * Disable a skill (add it to _disabledSkills of the L2Character).<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 *
	 * @param skillId
	 *            The identifier of the L2Skill to disable
	 */
	public void disableSkill(int skillId)
	{
		if (_disabledSkills == null)
			_disabledSkills = new SingletonSet<Integer>();

		_disabledSkills.add(skillId);
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 *
	 * @param skillId
	 * @param delay
	 *            (seconds * 1000)
	 */
	public void disableSkill(int skillId, long delay)
	{
		disableSkill(skillId);
		if (delay > 10)
			ThreadPoolManager.getInstance().scheduleAi(new EnableSkill(skillId), delay);
	}

	/**
	 * Check if a skill is disabled.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR>
	 * <BR>
	 *
	 * @param skillId
	 *            The identifier of the L2Skill to disable
	 */
	public boolean isSkillDisabled(int skillId)
	{
		if (isAllSkillsDisabled())
			return true;

		if (_disabledSkills == null)
			return false;

		return _disabledSkills.contains(skillId);
	}

	/**
	 * Disable all skills (set _allSkillsDisabled to True).<BR>
	 * <BR>
	 */
	public void disableAllSkills()
	{
		if (_log.isDebugEnabled())
			_log.debug("all skills disabled");
		_allSkillsDisabled = true;
	}

	/**
	 * Enable all skills (set _allSkillsDisabled to False).<BR>
	 * <BR>
	 */
	public void enableAllSkills()
	{
		if (_log.isDebugEnabled())
			_log.debug("all skills enabled");
		_allSkillsDisabled = false;
	}

	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.<BR>
	 * <BR>
	 *
	 * @param skill
	 *            The L2Skill to use
	 * @param targets
	 *            The table of L2Object targets
	 */
	public void callSkill(L2Skill skill, L2Character... targets)
	{
		L2Weapon activeWeapon = getActiveWeaponItem();

		L2PcInstance player = getActingPlayer();

		for (L2Object trg : targets)
		{
			if (player != null && trg instanceof L2PcInstance && Config.SIEGE_ONLY_REGISTERED)
			{
				if (!((L2PcInstance) trg).canBeTargetedByAtSiege(player))
				{
					//quick fix should be just removed from targetlist
					return;
				}
			}

			if (trg instanceof L2Character)
			{
				// Set some values inside target's instance for later use
				L2Character target = (L2Character) trg;

				// Check Raidboss attack and
				// check buffing chars who attack raidboss. Results in mute.
				L2Character targetsAttackTarget = target.getAI().getAttackTarget();
				L2Character targetsCastTarget = target.getAI().getCastTarget();

				if (!Config.ALT_DISABLE_RAIDBOSS_PETRIFICATION
				&& ((target.isRaid() && getLevel() > target.getLevel() + 8) || (!skill.isOffensive() && targetsAttackTarget != null && targetsAttackTarget.isRaid()
				&& targetsAttackTarget.getAttackByList().contains(target) // has attacked raid
				&& getLevel() > targetsAttackTarget.getLevel() + 8) || (!skill.isOffensive() && targetsCastTarget != null && targetsCastTarget.isRaid()
				&& targetsCastTarget.getAttackByList().contains(target) // has attacked raid
				&& getLevel() > targetsCastTarget.getLevel() + 8 )))
				{
					if (skill.isMagic())
					{
						L2Skill tempSkill = SkillTable.getInstance().getInfo(4215, 1);
						if (tempSkill != null)
							tempSkill.getEffects(target, this);
						else
							_log.warn("Skill 4215 at level 1 is missing in DP.");
					}
					else
					{
						L2Skill tempSkill = SkillTable.getInstance().getInfo(4515, 1);
						if (tempSkill != null)
							tempSkill.getEffects(target, this);
						else
							_log.warn("Skill 4515 at level 1 is missing in DP.");
					}
					return;
				}

				// Check if over-hit is possible
				if (skill.isOverhit())
				{
					if (target instanceof L2Attackable)
						((L2Attackable) target).overhitEnabled(true);
				}

				// Launch weapon Special ability skill effect if available
				if (activeWeapon != null && !target.isDead())
				{
					if (activeWeapon.getSkillEffectsByCast(this, target, skill) && this instanceof L2PcInstance)
					{
						sendMessage("Target affected by weapon special ability!");
					}
				}

				// Maybe launch chance skills on us
				if (_chanceSkills != null)
					_chanceSkills.onSkillHit(target, false, skill.isMagic(), skill.isOffensive());
				// Maybe launch chance skills on target
				if (target.getChanceSkills() != null)
					target.getChanceSkills().onSkillHit(this, true, skill.isMagic(), skill.isOffensive());
			}
		}

		// Launch the magic skill and calculate its effects
		SkillHandler.getInstance().getSkillHandler(skill.getSkillType()).useSkill(this, skill, targets);

		if (player != null)
		{
			for (L2Object target : targets)
			{
				// EVT_ATTACKED and PvPStatus
				if (target instanceof L2Character)
				{
					if (skill.getSkillType() != L2SkillType.AGGREMOVE && skill.getSkillType() != L2SkillType.AGGREDUCE
							&& skill.getSkillType() != L2SkillType.AGGREDUCE_CHAR)
					{
						if (skill.isNeutral())
						{
							// no flags
						}
						else if (skill.isOffensive())
						{
							if (target instanceof L2PcInstance || target instanceof L2Summon || target instanceof L2Trap)
							{
								if (skill.getSkillType() != L2SkillType.SIGNET && skill.getSkillType() != L2SkillType.SIGNET_CASTTIME)
								{
									if (skill.getSkillType() != L2SkillType.AGGREDUCE && skill.getSkillType() != L2SkillType.AGGREDUCE_CHAR
											&& skill.getSkillType() != L2SkillType.AGGREMOVE)
									{
										// notify target AI about the attack
										((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
									}
									if (target instanceof L2PcInstance)
									{
										((L2PcInstance) target).getAI().clientStartAutoAttack();
									}
									else if (target instanceof L2Summon)
									{
										L2PcInstance owner = ((L2Summon) target).getOwner();
										if (owner != null)
										{
											owner.getAI().clientStartAutoAttack();
										}
									}

									if (!(target instanceof L2Summon) || player.getPet() != target)
										player.updatePvPStatus(target.getActingPlayer());
								}
							}
							else if (target instanceof L2Attackable)
							{
								if (skill.getSkillType() != L2SkillType.AGGREDUCE && skill.getSkillType() != L2SkillType.AGGREDUCE_CHAR
										&& skill.getSkillType() != L2SkillType.AGGREMOVE)
								{
									// notify target AI about the attack
									((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player);
								}
							}
						}
						else
						{
							if (target instanceof L2PcInstance)
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (target != this && (((L2PcInstance) target).getPvpFlag() > 0 || ((L2PcInstance) target).getKarma() > 0))
									player.updatePvPStatus();
							}
							else if (target instanceof L2Attackable && !(skill.getSkillType() == L2SkillType.SUMMON)
									&& !(skill.getSkillType() == L2SkillType.BEAST_FEED) && !(skill.getSkillType() == L2SkillType.UNLOCK)
									&& !(skill.getSkillType() == L2SkillType.DELUXE_KEY_UNLOCK)
									&& !(skill.getSkillType() == L2SkillType.HEAL_MOB) && !(skill.getSkillType() == L2SkillType.MAKE_KILLABLE)
									&& !(skill.getSkillType() == L2SkillType.MAKE_QUEST_DROPABLE)
									&& (!(target instanceof L2Summon) || player.getPet() != target))
								player.updatePvPStatus();
						}
					}
				}
			}

			// Mobs in range 1000 see spell
			for (L2Object spMob : player.getKnownList().getKnownObjects().values())
			{
				if (spMob instanceof L2NpcInstance)
				{
					L2NpcInstance npcMob = (L2NpcInstance) spMob;

					if ((npcMob.isInsideRadius(player, 1000, true, true))
							&& (npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null))
						for (Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
							quest.notifySkillSee(npcMob, player, skill, targets, this instanceof L2Summon);

					/**************** FULMINUS COMMENT START***************/
					if (skill.getAggroPoints() > 0)
					{
						if (npcMob.isInsideRadius(player, 1000, true, true) && npcMob.hasAI() && npcMob.getAI().getIntention() == AI_INTENTION_ATTACK)
						{
							L2Object npcTarget = npcMob.getTarget();
							for (L2Object target : targets)
								if (npcTarget == target || npcMob == target)
									npcMob.seeSpell(player, target, skill);
						}
					}
					/**************** FULMINUS COMMENT END ***************/
					// the section within "Fulminus Comment" should be deleted from core and placed
					// within the mob's AI Script's onSkillSee, which is called by quest.notifySkillSee
				}
			}
		}
	}

	/**
	 * @param caster
	 * @param target
	 * @param skill
	 */
	public void seeSpell(L2PcInstance caster, L2Object target, L2Skill skill)
	{
		// TODO: Aggro calculation due to spells ought to be inside the AI script's onSkillSee.
		// when it is added there, this function will no longer be needed here.  (Fulminus)
		if (this instanceof L2Attackable)
			((L2Attackable) this).addDamageHate(caster, 0, (-skill.getAggroPoints() / Config.ALT_BUFFER_HATE));
	}

	/**
	 * Return True if the L2Character is behind the target and can't be seen.<BR>
	 * <BR>
	 */
	public boolean isBehind(L2Object target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 45;

		if (target == null)
			return false;

		if (target instanceof L2Character)
		{
			L2Character target1 = (L2Character) target;
			angleChar = Util.calculateAngleFrom(this, target1);
			angleTarget = Util.convertHeadingToDegree(target1.getHeading());
			angleDiff = angleChar - angleTarget;
			if (angleDiff <= -360 + maxAngleDiff)
				angleDiff += 360;
			if (angleDiff >= 360 - maxAngleDiff)
				angleDiff -= 360;
			if (Math.abs(angleDiff) <= maxAngleDiff)
			{
				if (_log.isDebugEnabled())
					_log.debug("Char " + getName() + " is behind " + target.getName());
				return true;
			}
		}
		else
		{
			if (_log.isDebugEnabled())
				_log.debug("isBehind's target not an L2 Character.");
		}
		return false;
	}

	public boolean isBehindTarget()
	{
		return isBehind(getTarget());
	}

	/**
	 * Return True if the target is facing the L2Character.<BR><BR>
	 */
	public boolean isInFrontOf(L2Character target)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff = 45;

		if (target == null)
			return false;

		angleTarget = Util.calculateAngleFrom(target, this);
		angleChar = Util.convertHeadingToDegree(target.getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
			angleDiff += 360;
		if (angleDiff >= 360 - maxAngleDiff)
			angleDiff -= 360;
		return (Math.abs(angleDiff) <= maxAngleDiff);
	}

	public boolean isInFrontOfTarget()
	{
		L2Object target = getTarget();
		if (target instanceof L2Character)
			return isInFrontOf((L2Character) target);

		return false;
	}

	/** Returns true if target is in front of L2Character (shield def etc) */
	public boolean isFacing(L2Object target, int maxAngle)
	{
		double angleChar, angleTarget, angleDiff, maxAngleDiff;
		if (target == null)
			return false;
		maxAngleDiff = maxAngle / 2;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(getHeading());
		angleDiff = angleChar - angleTarget;
		if (angleDiff <= -360 + maxAngleDiff)
			angleDiff += 360;
		if (angleDiff >= 360 - maxAngleDiff)
			angleDiff -= 360;
		return (Math.abs(angleDiff) <= maxAngleDiff);
	}

	/**
	 * Return heading to L2Character<BR>
	 * If <b>boolean toChar</b> is true heading calcs this->target, else target->this.<BR>
	 * <BR>
	 */

	public int getHeadingTo(L2Character target, boolean toChar)
	{
		if (target == null || target == this)
			return -1;

		int dx = target.getClientX() - getClientX();
		int dy = target.getClientY() - getClientY();
		int heading = (int) (Math.atan2(-dy, -dx) * 32768. / Math.PI);
		if (toChar)
			heading = target.getHeading() - (heading + 32768);
		else
			heading = getHeading() - (heading + 32768);

		if (heading < 0)
			heading += 65536;
		return heading;
	}

	/**
	 * Return 1.<BR>
	 * <BR>
	 */
	public double getLevelMod()
	{
		return 1;
	}

	public final void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}

	/** Sets _isCastingNow to true and _castInterruptTime is calculated from end time (ticks) */
	public final void forceIsCasting(int newSkillCastEndTick)
	{
		setIsCastingNow(true);
		// for interrupt -200 ms
		_castInterruptTime = newSkillCastEndTick - 2;
	}

	private Future<?>	_PvPRegTask;

	private long		_pvpFlagLasts;

	private boolean		_AIdisabled	= false;

	private boolean		_isMinion = false;

	public void setPvpFlagLasts(long time)
	{
		_pvpFlagLasts = time;
	}

	public long getPvpFlagLasts()
	{
		return _pvpFlagLasts;
	}

	public void startPvPFlag()
	{
		updatePvPFlag(1);

		_PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlag(), 1000, 1000);
	}

	public void stopPvPFlag()
	{
		if (_PvPRegTask != null)
			_PvPRegTask.cancel(false);

		updatePvPFlag(0);

		_PvPRegTask = null;
	}

	/**
	 * @param value
	 */
	public void updatePvPFlag(int value)
	{
		// Overridden in L2PcInstance
	}

	/**
	 * Return a Random Damage in function of the weapon.<BR>
	 * <BR>
	 */
	public final int getRandomDamage(@SuppressWarnings("unused")
	L2Character target)
	{
		L2Weapon weaponItem = getActiveWeaponItem();

		if (weaponItem == null)
			return 5 + (int) Math.sqrt(getLevel());

		return weaponItem.getRandomDamage();
	}

	@Override
	public String toString()
	{
		return "mob " + getObjectId();
	}

	public long getAttackEndTime()
	{
		return _attackEndTime;
	}

	/**
	 * Not Implemented.<BR>
	 * <BR>
	 */
	public abstract int getLevel();

	// =========================================================

	// =========================================================
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	// Property - Public
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}

	// Property - Public
	public int getAccuracy()
	{
		return getStat().getAccuracy();
	}

	// public final int getAtkCancel() { return getStat().getAtkCancel(); }
	public final double getCriticalDmg(L2Character target, double init)
	{
		return getStat().getCriticalDmg(target, init);
	}

	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}

	public int getEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}

	public final int getINT()
	{
		return getStat().getINT();
	}

	public final int getMagicalAttackRange(L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}

	public final int getMaxCp()
	{
		return getStat().getMaxCp();
	}

	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}

	public final int getMAtkSps(L2Character target, L2Skill skill)
	{
		int matk = (int) calcStat(Stats.MAGIC_ATTACK, _template.getBaseMAtk(), target, skill);
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
				matk *= 4;
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
				matk *= 2;
		}
		return matk;
	}

	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}

	public final int getMaxMp()
	{
		return getStat().getMaxMp();
	}

	public final int getMaxHp()
	{
		return getStat().getMaxHp();
	}

	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}

	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}

	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}

	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}

	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}

	public int getShldDef()
	{
		return getStat().getShldDef();
	}

	public final int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}

	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}

	// =========================================================

	// =========================================================
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	// Method - Public
	public void reduceCurrentHp(double i, L2Character attacker)
	{
		reduceCurrentHp(i, attacker, true, false, null);
	}

	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, true, false, skill);
	}

	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, awake, false, skill);
	}

	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		getStatus().reduceHp(i, attacker, awake, isDOT);
	}

	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}

	// =========================================================
	public void setChampion(boolean champ)
	{
		_champion = champ;
	}

	public boolean isChampion()
	{
		return _champion;
	}

	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}

	public int getLastHealAmount()
	{
		return _lastHealAmount;
	}

	public void setLastHealAmount(int hp)
	{
		_lastHealAmount = hp;
	}

	/**
	 * Check if character reflected skill
	 *
	 * @param skill
	 * @return
	 */
	public boolean reflectSkill(L2Skill skill)
	{
		double reflect = calcStat(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0, null, null);

		if (!skill.isMagic() && skill.getCastRange() < 100) // is 100 maximum range for melee skills?
		{
			double reflectMeleeSkill = calcStat(Stats.REFLECT_SKILL_MELEE_PHYSIC, 0, null, null);
			reflect = (reflectMeleeSkill > reflect) ? reflectMeleeSkill : reflect;
		}

		return (Rnd.get(100) < reflect);
	}

	protected void refreshSkills()
	{
		_calculators = NPC_STD_CALCULATOR;
		_stat = new CharStat(this);

		_skills = ((L2NpcTemplate) _template).getSkills();
		if (_skills != null)
		{
			for (Map.Entry<Integer, L2Skill> skill : _skills.entrySet())
			{
				addStatFuncs(skill.getValue().getStatFuncs(null, this));
			}
		}
		getStatus().setCurrentHpMp(getMaxHp(), getMaxMp());
	}

	/**
	 * Check player max buff count
	 * @return max buff count
	 */
	public int getMaxBuffCount()
	{
		return Config.ALT_BUFFS_MAX_AMOUNT + Math.max(0, getSkillLevel(L2Skill.SKILL_DIVINE_INSPIRATION));
	}

	/**
	 * Send system message about damage.<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li> L2PcInstance
	 * <li> L2SummonInstance
	 * <li> L2PetInstance</li>
	 * <BR>
	 * <BR>
	 * @param target
	 * @param damage
	 * @param mcrit
	 * @param pcrit
	 * @param miss
	 */
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}

	public ForceBuff getForceBuff()
	{
		return _forceBuff;
	}

	public void setForceBuff(ForceBuff fb)
	{
		_forceBuff = fb;
	}

	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}

	public int getDefAttrFire()
	{
		return (int) ((getTemplate().baseFireVuln - calcStat(Stats.FIRE_VULN, 1, this, null)) * 100);
	}

	public int getDefAttrWater()
	{
		return (int) ((getTemplate().baseWaterVuln - calcStat(Stats.WATER_VULN, 1, this, null)) * 100);
	}

	public int getDefAttrEarth()
	{
		return (int) ((getTemplate().baseEarthVuln - calcStat(Stats.EARTH_VULN, 1, this, null)) * 100);
	}

	public int getDefAttrWind()
	{
		return (int) ((getTemplate().baseWindVuln - calcStat(Stats.WIND_VULN, 1, this, null)) * 100);
	}

	public int getDefAttrHoly()
	{
		return (int) ((getTemplate().baseHolyVuln - calcStat(Stats.HOLY_VULN, 1, this, null)) * 100);
	}

	public int getDefAttrUnholy()
	{
		return (int) ((getTemplate().baseDarkVuln - calcStat(Stats.DARK_VULN, 1, this, null)) * 100);
	}

	public int getAttackElement()
	{
		return getStat().getAttackElement();
	}

	// Wrapper
	public double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}

	// Wrapper
	public double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}

	// Wrapper
	public double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}

	public int getAttackElementValue(int attackAttribute)
	{
		return getStat().getAttackElementValue(attackAttribute);
	}

	public boolean mustFallDownOnDeath()
	{
		return isDead();
	}

	public void setPreventedFromReceivingBuffs(boolean value)
	{
		_block_buffs = value;
	}

	public boolean isPreventedFromReceivingBuffs()
	{
		return _block_buffs;
	}

	class FlyToLocationTask implements Runnable
	{
		L2Object	_target;
		L2Character	_actor;
		L2Skill		_skill;

		public FlyToLocationTask(L2Character actor, L2Object target, L2Skill skill)
		{
			_actor = actor;
			_target = target;
			_skill = skill;
		}

		public void run()
		{
			try
			{
				FlyType _flyType = FlyType.valueOf(_skill.getFlyType());
				broadcastPacket(new FlyToLocation(_actor, _target, _flyType));
				getPosition().setXYZ(_target.getX(), _target.getY(), _target.getZ());
				broadcastPacket(new ValidateLocation(_actor));
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	/** Task for potion and herb queue */
	private class UsePotionTask implements Runnable
	{
		private L2Character _activeChar;
		private L2Skill _skill;
		
		UsePotionTask(L2Character activeChar, L2Skill skill)
		{
			_activeChar = activeChar;
			_skill = skill;
		}
		
		public void run()
		{
			try
			{
				_activeChar.doSimultaneousCast(_skill);
			}
			catch (Exception e)
			{
				_log.error(e.getMessage(), e);
			}
		}
	}

	public boolean isRaidMinion()
	{
		return _isMinion;
	}

	public boolean isRaidBoss()
	{
		return _isRaid && !_isMinion;
	}

	/**
	 * Set this Npc as a Minion instance.<BR><BR>
	 * @param val
	 */
	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isMinion = val;
	}
	
	private byte _packetBroadcastMask;
	
	public final void addPacketBroadcastMask(BroadcastMode mode)
	{
		if (!(this instanceof L2PlayableInstance) && getKnownList().getKnownPlayers().isEmpty())
			return;
		
		synchronized (PacketBroadcaster.getInstance())
		{
			_packetBroadcastMask |= mode.mask();
			
			PacketBroadcaster.getInstance().add(this);
		}
	}
	
	public final byte clearPacketBroadcastMask()
	{
		synchronized (PacketBroadcaster.getInstance())
		{
			final byte mask = _packetBroadcastMask;
			
			_packetBroadcastMask = 0;
			
			return mask;
		}
	}
	
	public final void broadcastFullInfo()
	{
		addPacketBroadcastMask(BroadcastMode.BROADCAST_FULL_INFO);
	}
	
	public abstract void broadcastFullInfoImpl();
}
