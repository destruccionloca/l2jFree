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
package com.l2jfree.gameserver.model.zone;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javolution.util.FastMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.instancemanager.InstanceManager;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.Location;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Playable;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Instance;
import com.l2jfree.gameserver.model.zone.form.Shape;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.skills.funcs.FuncOwner;
import com.l2jfree.tools.random.Rnd;
import com.l2jfree.util.L2Collections;

public class L2Zone implements FuncOwner
{
	protected static final Log _log = LogFactory.getLog(L2Zone.class);
	
	public static enum ZoneType
	{
		Arena,
		Boss,
		Castle,
		CastleTeleport,
		Clanhall,
		CoreBarrier,
		Damage,
		Danger,
		Dynamic,
		Default,
		DefenderSpawn,
		Fishing,
		Fort,
		HeadQuarters,
		Jail,
		Mothertree,
		Regeneration,
		Siege,
		SiegeDanger,
		Stadium,
		Town,
		Water;
		
		private String getZoneClassName()
		{
			switch (this)
			{
				case Default:
					return "L2Zone";
				default:
					return "L2" + name() + "Zone";
			}
		}
	}
	
	// Overridden by siege zones, jail zone and town zones
	public static enum PvpSettings
	{
		GENERAL,
		ARENA,
		PEACE
	}
	
	public static enum RestartType
	{
		CHAOTIC,
		OWNER
		// Others are handled by mapregion manager
	}
	
	public static enum Affected
	{
		PLAYABLE,
		PC,
		NPC,
		ALL
	}
	
	public static enum Boss
	{
		ANAKIM,
		ANTHARAS,
		BAIUM,
		BAYLOR,
		FOURSEPULCHERS,
		FRINTEZZA,
		LASTIMPERIALTOMB,
		LILITH,
		SAILREN,
		SUNLIGHTROOM,
		VALAKAS,
		VANHALTER,
		ZAKEN
	}
	
	public static final byte FLAG_PVP = 0;
	public static final byte FLAG_PEACE = 1;
	public static final byte FLAG_SIEGE = 2;
	public static final byte FLAG_MOTHERTREE = 3;
	public static final byte FLAG_CLANHALL = 4;
	public static final byte FLAG_NOESCAPE = 5;
	public static final byte FLAG_NOLANDING = 6;
	public static final byte FLAG_NOSTORE = 7;
	public static final byte FLAG_WATER = 8;
	public static final byte FLAG_FISHING = 9;
	public static final byte FLAG_JAIL = 10;
	public static final byte FLAG_STADIUM = 11;
	public static final byte FLAG_SUNLIGHTROOM = 12;
	public static final byte FLAG_DANGER = 13;
	public static final byte FLAG_CASTLE = 14;
	public static final byte FLAG_NOSUMMON = 15;
	public static final byte FLAG_FORT = 16;
	public static final byte FLAG_NOHEAL = 17;
	
	/**
	 * Move speed multiplier applied when character is in water (swimming).<BR>
	 * Tested on CT1 and CT1.5 NA retail
	 */
	public static final double WATER_MOVE_SPEED_BONUS = 0.55;
	
	private int _id;
	private String _name;
	private ZoneType _type;
	private boolean _enabled;
	
	private Shape[] _shapes;
	private Shape[] _exShapes;
	
	private final Location[][] _restarts = new Location[RestartType.values().length][];
	
	private int _castleId;
	private int _clanhallId;
	private int _townId;
	private int _fortId;
	
	private PvpSettings _pvp;
	private Boss _boss;
	private Affected _affected = Affected.ALL;

	/** Can't logout (including back to character selection menu); can't use SoE? */
	private boolean _noEscape;
	/** Can't dismount */
	private boolean _noLanding;
	private boolean _noPrivateStore;
	private boolean _noSummon;
	/** Can't cast heal if caster is in zone; can't receive healing if target is in zone */
	private boolean _noHeal;
	
	private SystemMessage _onEnterMsg;
	private SystemMessage _onExitMsg;
	
	private int _abnormal;
	private int _hpDamage;
	private int _mpDamage;
	
	private boolean _exitOnDeath;
	private boolean _buffRepeat; // Used for buffs and debuffs
	
	protected L2Skill[] _applyEnter;
	private L2Skill[] _applyExit;
	private int[] _removeEnter;
	protected int[] _removeExit;
	
	// Instances
	private String _instanceName;
	private String _instanceGroup;
	private int _minPlayers;
	private int _maxPlayers;
	
	private Func[] _statFuncs;
	
	protected final void addStatFunc(Func func)
	{
		_statFuncs = (Func[])ArrayUtils.add(_statFuncs, func);
	}
	
	protected void register() throws Exception
	{
	}
	
	public final int getId()
	{
		return _id;
	}
	
	public final String getName()
	{
		return _name;
	}
	
	public final ZoneType getType()
	{
		return _type;
	}
	
	public final String getClassName()
	{
		return getClass().getSimpleName();
	}
	
	public final int getCastleId()
	{
		return _castleId;
	}
	
	public final int getTownId()
	{
		return _townId;
	}
	
	public final int getClanhallId()
	{
		return _clanhallId;
	}
	
	public final int getFortId()
	{
		return _fortId;
	}
	
	public final Boss getBoss()
	{
		return _boss;
	}
	
	/**
	 * <B>Get HP damage over time</B> (<I>per cycle</I>)<BR><BR>
	 * <U>The interval is not necessarily one second</U>.
	 * Default interval is 3000 ms.
	 * @return HP amount to be subtracted
	 * @see #getMPDamagePerSecond()
	 */
	public final int getHPDamagePerSecond()
	{
		return _hpDamage;
	}
	
	/**
	 * <B>Get MP damage over time</B> (<I>per cycle</I>)<BR><BR>
	 * <U>The interval is not necessarily one second</U>.
	 * Default interval is 3000 ms.
	 * @return HP amount to be subtracted
	 * @see #getHPDamagePerSecond()
	 */
	public final int getMPDamagePerSecond()
	{
		return _mpDamage;
	}
	
	public final boolean isRepeatingBuff()
	{
		return _buffRepeat;
	}
	
	public final boolean isPeace()
	{
		return _pvp == PvpSettings.PEACE;
	}
	
	public final boolean isEnabled()
	{
		return _enabled;
	}
	
	public final void setEnabled(boolean val)
	{
		_enabled = val;
	}
	
	public final Location getRestartPoint(RestartType type)
	{
		Location[] points = _restarts[type.ordinal()];
		
		if (points != null && points.length != 0)
			return points[Rnd.get(points.length)];
		
		// No restartpoint defined
		return null;
	}
	
	protected final L2Skill[] getApplyEnter()
	{
		return _applyEnter;
	}
	
	protected final int[] getRemoveEnter()
	{
		return _removeEnter;
	}
	
	private final FastMap<L2Character, Boolean> _charactersInside = new FastMap<L2Character, Boolean>().setShared(true);
	
	protected final FastMap<L2Character, Boolean> getCharactersInsideMap()
	{
		return _charactersInside;
	}
	
	public final Set<L2Character> getCharactersInside()
	{
		return _charactersInside.keySet();
	}
	
	public final Iterable<L2Character> getCharactersInsideActivated()
	{
		return L2Collections.convertingIterable(_charactersInside.entrySet(),
			new L2Collections.Converter<Map.Entry<L2Character, Boolean>, L2Character>() {
				@Override
				public L2Character convert(Entry<L2Character, Boolean> src)
				{
					if (Boolean.TRUE.equals(src.getValue()))
						return src.getKey();
					
					return null;
				}
			});
	}
	
	/**
	 * Check default never changing conditions. Determines zone can be activated on player or not at all.
	 */
	protected boolean checkConstantConditions(L2Character character)
	{
		return isCorrectType(character);
	}
	
	/**
	 * Check conditions that can be changed anytime. Determines zone can be activated or not on the player currently.
	 */
	protected boolean checkDynamicConditions(L2Character character)
	{
		if (_exitOnDeath && character.isDead())
			return false;
		
		return isEnabled();
	}
	
	private State getExpectedState(L2Character character)
	{
		if (!isInsideZone(character))
			return State.OUTSIDE;
		
		if (!checkConstantConditions(character))
			return State.INSIDE_BUT_NOT_ACTIVED;
		
		if (!checkDynamicConditions(character))
			return State.INSIDE_BUT_NOT_ACTIVED;
		
		return State.INSIDE_AND_ACTIVATED;
	}
	
	private State getCurrentState(L2Character character)
	{
		final Boolean isActive = _charactersInside.get(character);
		
		if (isActive == null)
			return State.OUTSIDE;
		
		return isActive ? State.INSIDE_AND_ACTIVATED : State.INSIDE_BUT_NOT_ACTIVED;
	}
	
	private enum State
	{
		OUTSIDE,
		INSIDE_BUT_NOT_ACTIVED,
		INSIDE_AND_ACTIVATED;
	}
	
	public final void revalidateAllInZone()
	{
		for (L2Character character : getCharactersInside())
			revalidateInZone(character);
	}
	
	public final void revalidateInZone(L2Character character)
	{
		changeStateOf(character, getExpectedState(character));
	}
	
	private void changeStateOf(L2Character character, State expectedState)
	{
		final State currentState = getCurrentState(character);
		
		if (currentState == expectedState)
			return;
		
		switch (expectedState)
		{
			case INSIDE_AND_ACTIVATED:
			{
				_charactersInside.put(character, Boolean.TRUE);
				onEnter(character);
				break;
			}
			case INSIDE_BUT_NOT_ACTIVED:
			{
				_charactersInside.put(character, Boolean.FALSE);
				if (currentState == State.INSIDE_AND_ACTIVATED)
					onExit(character);
				break;
			}
			case OUTSIDE:
			{
				_charactersInside.remove(character);
				if (currentState == State.INSIDE_AND_ACTIVATED)
					onExit(character);
				break;
			}
		}
	}
	
	protected void onEnter(L2Character character)
	{
		if (_onEnterMsg != null && character instanceof L2PcInstance)
			character.sendPacket(_onEnterMsg);
		
		if (_abnormal > 0)
			character.startAbnormalEffect(_abnormal);
		
		if (_applyEnter != null)
			for (L2Skill sk : _applyEnter)
				sk.getEffects(character, character);
		
		if (_removeEnter != null)
			for (int id : _removeEnter)
				character.stopSkillEffects(id);
		
		if (_statFuncs != null)
			character.addStatFuncs(_statFuncs);
		
		if (_pvp == PvpSettings.ARENA)
		{
			character.setInsideZone(FLAG_NOSUMMON, true);
			character.setInsideZone(FLAG_PVP, true);
		}
		else if (_pvp == PvpSettings.PEACE)
		{
			if (Config.ZONE_TOWN != 2)
				character.setInsideZone(FLAG_PEACE, true);
		}
		
		if (_noLanding && character instanceof L2PcInstance)
		{
			character.setInsideZone(FLAG_NOLANDING, true);
			
			if (((L2PcInstance)character).getMountType() == 2)
				((L2PcInstance)character).enteredNoLanding();
		}
		
		if (_noEscape)
			character.setInsideZone(FLAG_NOESCAPE, true);
		if (_noPrivateStore)
			character.setInsideZone(FLAG_NOSTORE, true);
		if (_noSummon)
			character.setInsideZone(FLAG_NOSUMMON, true);
		if (_noHeal)
			character.setInsideZone(FLAG_NOHEAL, true);
		
		if (_instanceName != null && _instanceGroup != null && character instanceof L2PcInstance)
			tryPortIntoInstance((L2PcInstance)character);
	}
	
	protected void onExit(L2Character character)
	{
		if (_onExitMsg != null && character instanceof L2PcInstance)
			character.sendPacket(_onExitMsg);
		
		if (_abnormal > 0)
			character.stopAbnormalEffect(_abnormal);
		
		if (_applyExit != null)
			for (L2Skill sk : _applyExit)
				sk.getEffects(character, character);
		
		if (_removeExit != null)
			for (int id : _removeExit)
				character.stopSkillEffects(id);
		
		if (_statFuncs != null)
			character.removeStatsOwner(this);
		
		if (_pvp == PvpSettings.ARENA)
		{
			character.setInsideZone(FLAG_NOSUMMON, false);
			character.setInsideZone(FLAG_PVP, false);
		}
		else if (_pvp == PvpSettings.PEACE)
		{
			character.setInsideZone(FLAG_PEACE, false);
		}
		
		if (_noLanding && character instanceof L2PcInstance)
		{
			character.setInsideZone(FLAG_NOLANDING, false);
			
			if (((L2PcInstance)character).getMountType() == 2)
				((L2PcInstance)character).exitedNoLanding();
		}
		
		if (_noEscape)
			character.setInsideZone(FLAG_NOESCAPE, false);
		if (_noPrivateStore)
			character.setInsideZone(FLAG_NOSTORE, false);
		if (_noSummon)
			character.setInsideZone(FLAG_NOSUMMON, false);
		if (_noHeal)
			character.setInsideZone(FLAG_NOHEAL, false);
		
		if (_instanceName != null && character instanceof L2PcInstance && character.getInstanceId() > 0)
			portIntoInstance((L2PcInstance)character, 0);
	}
	
	private static final int REASON_OK = 0;
	private static final int REASON_MULTIPLE_INSTANCE = 1;
	private static final int REASON_INSTANCE_FULL = 2;
	private static final int REASON_SMALL_GROUP = 3;
	
	private static final class InstanceResult
	{
		private int instanceId = 0;
		private int reason = REASON_OK;
	}
	
	private void tryPortIntoInstance(L2PcInstance pl)
	{
		InstanceResult ir = new InstanceResult();
		
		if (_instanceGroup.equals("party"))
		{
			if (pl.isInParty())
			{
				List<L2PcInstance> list = pl.getParty().getPartyMembers();
				getInstanceFromGroup(ir, list, false);
				checkPlayersInside(ir, list);
			}
		}
		else if (_instanceGroup.equals("clan"))
		{
			if (pl.getClan() != null)
			{
				List<L2PcInstance> list = pl.getClan().getOnlineMembersList();
				getInstanceFromGroup(ir, list, true);
				checkPlayersInside(ir, list);
			}
		}
		else if (_instanceGroup.equals("alliance"))
		{
			if (pl.getAllyId() > 0)
			{
				List<L2PcInstance> list = pl.getClan().getOnlineAllyMembers();
				getInstanceFromGroup(ir, list, true);
				checkPlayersInside(ir, list);
			}
		}
		
		if (ir.reason == REASON_MULTIPLE_INSTANCE)
		{
			pl.sendMessage("You cannot enter this instance while other " + _instanceGroup + " members are in another instance.");
		}
		else if (ir.reason == REASON_INSTANCE_FULL)
		{
			pl.sendMessage("This instance is full. There is a maximum of " + _maxPlayers + " players inside.");
		}
		else if (ir.reason == REASON_SMALL_GROUP)
		{
			pl.sendMessage("Your " + _instanceGroup + " is too small. There is a minimum of " + _minPlayers + " players inside.");
		}
		else
		{
			try
			{
				if (ir.instanceId == 0)
					ir.instanceId = InstanceManager.getInstance().createDynamicInstance(_instanceName);
				
				portIntoInstance(pl, ir.instanceId);
			}
			catch (Exception e)
			{
				pl.sendMessage("The requested instance could not be created.");
			}
		}
	}
	
	private void getInstanceFromGroup(InstanceResult ir, List<L2PcInstance> group, boolean allowMultiple)
	{
		for (L2PcInstance mem : group)
		{
			if (mem == null || mem.getInstanceId() == 0)
				continue;
			
			Instance i = InstanceManager.getInstance().getInstance(mem.getInstanceId());
			if (i.getName().equals(_instanceName))
			{
				ir.instanceId = i.getId(); // Player in this instance template found
				return;
			}
			else if (!allowMultiple)
			{
				ir.reason = REASON_MULTIPLE_INSTANCE;
				return;
			}
		}
	}
	
	private void checkPlayersInside(InstanceResult ir, List<L2PcInstance> group)
	{
		if (ir.reason != REASON_OK)
			return;
		
		int valid = 0, all = 0;
		
		for (L2PcInstance mem : group)
		{
			if (mem != null && mem.getInstanceId() == ir.instanceId)
				valid++;
			all++;
			
			if (valid == _maxPlayers)
			{
				ir.reason = REASON_INSTANCE_FULL;
				return;
			}
		}
		if (all < _minPlayers)
		{
			ir.reason = REASON_SMALL_GROUP;
		}
	}
	
	private void portIntoInstance(L2PcInstance pl, int instanceId)
	{
		pl.setInstanceId(instanceId);
		pl.getKnownList().updateKnownObjects();
		L2Summon pet = pl.getPet();
		if (pet != null)
		{
			pet.setInstanceId(instanceId);
			pet.getKnownList().updateKnownObjects();
		}
	}
	
	public final void onDie(L2Character character)
	{
		if (getCurrentState(character) == State.INSIDE_AND_ACTIVATED)
			onDieInside(character);
		
		// will exit the zone, if it's disabled because of "_exitOnDeath"
		revalidateInZone(character);
	}
	
	public final void onRevive(L2Character character)
	{
		// will enter the zone, if it was disabled because of "_exitOnDeath"
		revalidateInZone(character);
		
		if (getCurrentState(character) == State.INSIDE_AND_ACTIVATED)
			onReviveInside(character);
	}
	
	protected void onDieInside(L2Character character)
	{
	}
	
	protected void onReviveInside(L2Character character)
	{
	}
	
	public final void removeFromZone(L2Character character)
	{
		changeStateOf(character, State.OUTSIDE);
	}
	
	private boolean isCorrectType(L2Character character)
	{
		switch (_affected)
		{
			case PLAYABLE:
				return character instanceof L2Playable;
			case PC:
				return character instanceof L2PcInstance;
			case NPC:
				return character instanceof L2Npc;
			case ALL:
				return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the given coordinates are within the zone
	 * 
	 * @param x
	 * @param y
	 */
	public final boolean isInsideZone(int x, int y)
	{
		if (_exShapes != null)
			for (Shape sh : _exShapes)
				if (sh.contains(x, y))
					return false;
		
		for (Shape sh : _shapes)
			if (sh.contains(x, y))
				return true;
		
		return false;
	}
	
	/**
	 * Checks if the given coordinates are within the zone
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public final boolean isInsideZone(int x, int y, int z)
	{
		if (_exShapes != null)
			for (Shape sh : _exShapes)
				if (sh.contains(x, y, z))
					return false;
		
		for (Shape sh : _shapes)
			if (sh.contains(x, y, z))
				return true;
		
		return false;
	}
	
	/**
	 * Checks if the given obejct is inside the zone.
	 * 
	 * @param object
	 */
	public final boolean isInsideZone(L2Object object)
	{
		return isInsideZone(object.getX(), object.getY(), object.getZ());
	}
	
	public final double getDistanceToZone(L2Object object)
	{
		return getDistanceToZone(object.getX(), object.getY());
	}
	
	public final double getDistanceToZone(int x, int y)
	{
		double dist = Double.MAX_VALUE;
		for (Shape sh : _shapes)
			dist = Math.min(dist, sh.getDistanceToZone(x, y));
		
		return dist;
	}
	
	public final boolean isCloserThan(int x, int y, int distance)
	{
		for (Shape sh : _shapes)
			if (sh.isCloserThan(x, y, distance))
				return true;
		
		return false;
	}
	
	public final int getMiddleX()
	{
		if (_shapes.length == 0)
		{
			_log.error(this + " has no shapes defined");
			return 0;
		}
		
		int sum = 0;
		for (Shape sh : _shapes)
			sum += sh.getMiddleX();
		
		return (sum / _shapes.length);
	}
	
	public final int getMiddleY()
	{
		if (_shapes.length == 0)
		{
			_log.error(this + " has no shapes defined");
			return 0;
		}
		
		int sum = 0;
		for (Shape sh : _shapes)
			sum += sh.getMiddleY();
		
		return (sum / _shapes.length);
	}
	
	public final boolean intersectsRectangle(int ax, int bx, int ay, int by)
	{
		for (Shape sh : _shapes)
			if (sh.intersectsRectangle(ax, bx, ay, by))
				return true;
		
		return false;
	}
	
	public final int getMaxZ(L2Object obj)
	{
		return getMaxZ(obj.getX(), obj.getY(), obj.getZ());
	}
	
	public final int getMinZ(L2Object obj)
	{
		return getMinZ(obj.getX(), obj.getY(), obj.getZ());
	}
	
	public final int getMaxZ(int x, int y, int z)
	{
		for (Shape sh : _shapes)
			if (sh.contains(x, y))
				return sh.getMaxZ();
		
		return z;
	}
	
	public final int getMinZ(int x, int y, int z)
	{
		for (Shape sh : _shapes)
			if (sh.contains(x, y))
				return sh.getMinZ();
		
		return z;
	}
	
	public final Location getRandomLocation()
	{
		if (_shapes.length == 0)
		{
			_log.error(this + " has no shapes defined");
			return Location.EMPTY_LOCATION;
		}
		
		return _shapes[Rnd.get(_shapes.length)].getRandomLocation();
	}
	
	/**
	 * Some GrandBosses send all players in zone to a specific part of the zone, rather than just removing them all. If
	 * this is the case, this command should be used.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public final void movePlayersTo(int x, int y, int z)
	{
		for (L2Character character : getCharactersInside())
			if (character instanceof L2PcInstance)
				character.teleToLocation(x, y, z);
	}
	
	@Override
	public final String toString()
	{
		return getClassName() + "[id='" + getId() + "',name='" + getName() + "']";
	}
	
	@Override
	public final String getFuncOwnerName()
	{
		return getName();
	}
	
	@Override
	public final L2Skill getFuncOwnerSkill()
	{
		return null;
	}
	
	// Zone parser
	
	public static L2Zone parseZone(Node zn)
	{
		Integer id = -1;
		ZoneType type = null;
		String name;
		Boolean enabled;
		L2Zone zone = null;
		try
		{
			id = Integer.parseInt(zn.getAttributes().getNamedItem("id").getNodeValue());
			
			Node tn = zn.getAttributes().getNamedItem("type");
			Node nn = zn.getAttributes().getNamedItem("name");
			Node en = zn.getAttributes().getNamedItem("enabled");
			
			type = (tn != null) ? ZoneType.valueOf(tn.getNodeValue()) : ZoneType.Default;
			name = (nn != null) ? nn.getNodeValue() : id.toString();
			enabled = (en != null) ? Boolean.parseBoolean(en.getNodeValue()) : true;
			
			Class<?> clazz = Class.forName("com.l2jfree.gameserver.model.zone." + type.getZoneClassName());
			Constructor<?> constructor = clazz.getConstructor();
			zone = (L2Zone)constructor.newInstance();
		}
		catch (Exception e)
		{
			_log.error("Cannot create a L2" + type + "Zone for id " + id, e);
			return null;
		}
		
		zone._id = id;
		zone._type = type;
		zone._name = name;
		zone._enabled = enabled;
		
		List<Shape> shapes = new ArrayList<Shape>();
		List<Shape> exShapes = new ArrayList<Shape>();
		for (Node n = zn.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("shape".equalsIgnoreCase(n.getNodeName()))
			{
				Shape sh = Shape.parseShape(n, id);
				if (sh != null)
				{
					if (sh.isExclude())
						exShapes.add(sh);
					else
						shapes.add(sh);
				}
				else
					return null;
			}
			else if ("entity".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseEntity(n);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse entity for " + zone, e);
					return null;
				}
			}
			else if ("instance".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseInstance(n);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse instance for " + zone, e);
					return null;
				}
			}
			else if ("settings".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseSettings(n);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse settings for " + zone, e);
					return null;
				}
			}
			else if ("msg".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseMessages(n);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse messages for " + zone, e);
					return null;
				}
			}
			else if ("skill".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseSkills(n);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse skills for " + zone, e);
					return null;
				}
			}
			else if ("cond".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseCondition(n.getFirstChild());
				}
				catch (Exception e)
				{
					_log.error("Cannot parse skills for " + zone, e);
					return null;
				}
			}
			else if ("restart_chaotic".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseRestart(n, RestartType.CHAOTIC);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse chaotic restart point for " + zone, e);
					return null;
				}
			}
			else if ("restart_owner".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseRestart(n, RestartType.OWNER);
				}
				catch (Exception e)
				{
					_log.error("Cannot parse owner restart point for " + zone, e);
					return null;
				}
			}
			else if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				_log.error("Cannot parse <" + n.getNodeName() + "> for " + zone);
				return null;
			}
		}
		
		zone._shapes = shapes.toArray(new Shape[shapes.size()]);
		if (!exShapes.isEmpty())
			zone._exShapes = exShapes.toArray(new Shape[exShapes.size()]);
		
		try
		{
			zone.register();
		}
		catch (Exception e)
		{
			_log.warn("Registration problem for " + zone, e);
		}
		
		return zone;
	}
	
	private void parseRestart(Node n, RestartType t) throws Exception
	{
		Node xn = n.getAttributes().getNamedItem("x");
		Node yn = n.getAttributes().getNamedItem("y");
		Node zn = n.getAttributes().getNamedItem("z");
		
		int x = Integer.parseInt(xn.getNodeValue());
		int y = Integer.parseInt(yn.getNodeValue());
		int z = Integer.parseInt(zn.getNodeValue());
		
		_restarts[t.ordinal()] = (Location[])ArrayUtils.add(_restarts[t.ordinal()], new Location(x, y, z));
	}
	
	private void parseEntity(Node n) throws Exception
	{
		Node castle = n.getAttributes().getNamedItem("castleId");
		Node clanhall = n.getAttributes().getNamedItem("clanhallId");
		Node town = n.getAttributes().getNamedItem("townId");
		Node fort = n.getAttributes().getNamedItem("fortId");
		
		_castleId = (castle != null) ? Integer.parseInt(castle.getNodeValue()) : -1;
		_clanhallId = (clanhall != null) ? Integer.parseInt(clanhall.getNodeValue()) : -1;
		_townId = (town != null) ? Integer.parseInt(town.getNodeValue()) : -1;
		_fortId = (fort != null) ? Integer.parseInt(fort.getNodeValue()) : -1;
	}
	
	private void parseInstance(Node n) throws Exception
	{
		Node instanceName = n.getAttributes().getNamedItem("instanceName");
		Node instanceGroup = n.getAttributes().getNamedItem("instanceGroup");
		Node minPlayers = n.getAttributes().getNamedItem("minPlayers");
		Node maxPlayers = n.getAttributes().getNamedItem("maxPlayers");
		
		_instanceName = (instanceName != null) ? instanceName.getNodeValue() : null;
		_instanceGroup = (instanceGroup != null) ? instanceGroup.getNodeValue().toLowerCase() : null;
		_minPlayers = (minPlayers != null) ? Integer.parseInt(minPlayers.getNodeValue()) : -1;
		_maxPlayers = (maxPlayers != null) ? Integer.parseInt(maxPlayers.getNodeValue()) : -1;
	}
	
	private void parseSettings(Node n) throws Exception
	{
		Node pvp = n.getAttributes().getNamedItem("pvp");
		Node noLanding = n.getAttributes().getNamedItem("noLanding");
		Node noEscape = n.getAttributes().getNamedItem("noEscape");
		Node noPrivateStore = n.getAttributes().getNamedItem("noPrivateStore");
		Node noSummon = n.getAttributes().getNamedItem("noSummon"); // Forbids summon friend skills.
		Node boss = n.getAttributes().getNamedItem("boss");
		Node affected = n.getAttributes().getNamedItem("affected");
		Node buffRepeat = n.getAttributes().getNamedItem("buffRepeat");
		Node abnorm = n.getAttributes().getNamedItem("abnormal");
		Node exitOnDeath = n.getAttributes().getNamedItem("exitOnDeath");
		Node hpDamage = n.getAttributes().getNamedItem("hpDamage");
		Node mpDamage = n.getAttributes().getNamedItem("mpDamage");
		Node noHeal = n.getAttributes().getNamedItem("noHeal");
		
		_pvp = (pvp != null) ? PvpSettings.valueOf(pvp.getNodeValue().toUpperCase()) : PvpSettings.GENERAL;
		_noLanding = (noLanding != null) && Boolean.parseBoolean(noLanding.getNodeValue());
		_noEscape = (noEscape != null) && Boolean.parseBoolean(noEscape.getNodeValue());
		_noPrivateStore = (noPrivateStore != null) && Boolean.parseBoolean(noPrivateStore.getNodeValue());
		_noSummon = (noSummon != null) && Boolean.parseBoolean(noSummon.getNodeValue());
		_boss = (boss != null) ? Boss.valueOf(boss.getNodeValue().toUpperCase()) : null;
		_affected = (affected != null) ? Affected.valueOf(affected.getNodeValue().toUpperCase()) : Affected.PLAYABLE;
		_buffRepeat = (buffRepeat != null) && Boolean.parseBoolean(buffRepeat.getNodeValue());
		_abnormal = (abnorm != null) ? Integer.decode("0x" + abnorm.getNodeValue()) : 0;
		_exitOnDeath = (exitOnDeath != null) && Boolean.parseBoolean(exitOnDeath.getNodeValue());
		_hpDamage = (hpDamage != null) ? Integer.parseInt(hpDamage.getNodeValue()) : 0;
		_mpDamage = (mpDamage != null) ? Integer.parseInt(mpDamage.getNodeValue()) : 0;
		_noHeal = (noHeal != null) && Boolean.parseBoolean(noHeal.getNodeValue());
	}
	
	private void parseMessages(Node n) throws Exception
	{
		Node enter = n.getAttributes().getNamedItem("onEnter");
		Node exit = n.getAttributes().getNamedItem("onExit");
		
		if (enter != null)
		{
			String onEnter = enter.getNodeValue();
			try
			{
				_onEnterMsg = SystemMessageId.getSystemMessageId(Integer.parseInt(onEnter), true).getSystemMessage();
			}
			catch (NumberFormatException nfe)
			{
				_onEnterMsg = SystemMessage.sendString(onEnter);
			}
		}
		
		if (exit != null)
		{
			String onExit = exit.getNodeValue();
			try
			{
				_onExitMsg = SystemMessageId.getSystemMessageId(Integer.parseInt(onExit), true).getSystemMessage();
			}
			catch (NumberFormatException nfe)
			{
				_onExitMsg = SystemMessage.sendString(onExit);
			}
		}
	}
	
	private void parseSkills(Node n) throws Exception
	{
		Node aen = n.getAttributes().getNamedItem("applyEnter");
		Node aex = n.getAttributes().getNamedItem("applyExit");
		Node ren = n.getAttributes().getNamedItem("removeEnter");
		Node rex = n.getAttributes().getNamedItem("removeExit");
		
		if (aen != null)
			_applyEnter = parseApplySkill(aen.getNodeValue());
		
		if (aex != null)
			_applyExit = parseApplySkill(aex.getNodeValue());
		
		if (ren != null)
			_removeEnter = parseRemoveSkill(ren.getNodeValue());
		
		if (rex != null)
			_removeExit = parseRemoveSkill(rex.getNodeValue());
	}
	
	private L2Skill[] parseApplySkill(String set)
	{
		L2Skill[] skills = null;
		StringTokenizer st = new StringTokenizer(set, ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), ",");
			int skillId = Integer.parseInt(st2.nextToken());
			int level = Integer.parseInt(st2.nextToken());
			
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
			if (skill != null)
				skills = (L2Skill[])ArrayUtils.add(skills, skill);
		}
		return skills;
	}
	
	private int[] parseRemoveSkill(String set)
	{
		int[] skillIds = null;
		StringTokenizer st = new StringTokenizer(set, ";");
		while (st.hasMoreTokens())
		{
			int skillId = Integer.parseInt(st.nextToken());
			skillIds = ArrayUtils.add(skillIds, skillId);
		}
		return skillIds;
	}
	
	protected void parseCondition(Node n) throws Exception
	{
		throw new IllegalStateException("This zone shouldn't have conditions!");
	}
}
