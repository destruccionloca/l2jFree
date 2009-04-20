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
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.Location;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfree.gameserver.model.zone.form.Shape;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.skills.funcs.Func;
import com.l2jfree.gameserver.skills.funcs.FuncOwner;
import com.l2jfree.gameserver.skills.funcs.FuncTemplate;
import com.l2jfree.tools.random.Rnd;
import com.l2jfree.util.L2Collections;

public abstract class L2Zone implements FuncOwner
{
	protected static Log _log = LogFactory.getLog(L2Zone.class.getName());

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
		Stadium,
		Town,
		Water
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

	public static final byte FLAG_PVP			= 0;
	public static final byte FLAG_PEACE			= 1;
	public static final byte FLAG_SIEGE			= 2;
	public static final byte FLAG_MOTHERTREE	= 3;
	public static final byte FLAG_CLANHALL		= 4;
	public static final byte FLAG_NOESCAPE		= 5;
	public static final byte FLAG_NOLANDING		= 6;
	public static final byte FLAG_NOSTORE		= 7;
	public static final byte FLAG_WATER			= 8;
	public static final byte FLAG_FISHING		= 9;
	public static final byte FLAG_JAIL			= 10;
	public static final byte FLAG_STADIUM		= 11;
	public static final byte FLAG_SUNLIGHTROOM	= 12;
	public static final byte FLAG_DANGER		= 13;
	public static final byte FLAG_CASTLE		= 14;
	public static final byte FLAG_NOSUMMON		= 15;
	public static final byte FLAG_FORT			= 16;
	public static final byte FLAG_SWAMP			= 17;
	public static final byte FLAG_NOHEAL		= 18;
	public static final byte FLAG_P_ATK_DOWN	= 19;
	public static final byte FLAG_P_DEF_DOWN	= 20;

	/** Tested on CT1 and CT1.5 NA retail */
	public static final double WATER_MOVE_SPEED_BONUS = 0.55;
	/** UNK */
	public static final double SWAMP_MOVE_SPEED_BONUS = 0.85;
	/** UNK */
	public static final double PADOWN_POWER_ATTACK_BONUS = 0.85;
	/** UNK */
	public static final double PDDOWN_POWER_DEFENSE_BONUS = 0.85;

	protected int _id;
	protected String _name;

	protected boolean _enabled;

	protected Shape[] _shapes;
	protected Shape[] _exShapes;
	protected FastMap<Integer, L2Character> _characterList;

	protected FastMap<RestartType, FastList<Location>> _restarts;

	protected int _castleId;
	protected int _clanhallId;
	protected int _townId;
	protected int _fortId;

	protected ZoneType _type;
	protected PvpSettings _pvp;
	protected Boss _boss;
	protected Affected _affected = Affected.ALL;
	
	protected boolean _noEscape, _noLanding, _noPrivateStore, _noSummon, _envSlow, _noHeal,
	_padown, _pddown;

	protected SystemMessage _onEnterMsg, _onExitMsg;

	protected int _abnormal;
	protected int _hpDamage;
	protected int _mpDamage;

	protected boolean _exitOnDeath;
	protected boolean _buffRepeat; // Used for buffs and debuffs

	protected FastList<L2Skill> _applyEnter, _applyExit, _removeEnter, _removeExit;

	// Instances
	protected String _instanceName;
	protected String _instanceGroup;
	protected int _minPlayers;
	protected int _maxPlayers;

	protected static final Func[] EMPTY_FUNC_SET = new Func[0];
	protected FuncTemplate[] _funcTemplates;

	public L2Zone()
	{
		// Constructor
		_characterList = new FastMap<Integer, L2Character>().setShared(true);
	}

	protected void register()
	{
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public ZoneType getType()
	{
		return _type;
	}

	public String getClassName()
	{
		return getClass().getSimpleName();
	}

	public int getCastleId()
	{
		return _castleId;
	}

	public int getTownId()
	{
		return _townId;
	}

	public int getClanhallId()
	{
		return _clanhallId;
	}

	public int getFortId()
	{
		return _fortId;
	}

	public int getHPDamagePerSecond()
	{
		return _hpDamage;
	}

	public int getMPDamagePerSecond()
	{
		return _mpDamage;
	}

	public boolean isPeace()
	{
		return _pvp == PvpSettings.PEACE;
	}

	public void setEnabled(boolean val)
	{
		_enabled = val;
	}

	public FastMap<RestartType, FastList<Location>> getRestartMap()
	{
		if (_restarts == null)
			_restarts = new FastMap<RestartType, FastList<Location>>().setShared(true);

		return _restarts;
	}

	public Location getRestartPoint(RestartType type)
	{
		if(_restarts.containsKey(type) && !_restarts.get(type).isEmpty())
		{
			FastList<Location> rts = _restarts.get(type);
			return rts.get(Rnd.nextInt(rts.size()-1));
		}

		// No restartpoint defined
		return null;
	}

	public void revalidateInZone(L2Character character)
	{
		if (_enabled && isCorrectType(character) && isInsideZone(character))
		{
			if (!_characterList.containsKey(character.getObjectId()))
			{
				_characterList.put(character.getObjectId(), character);
				onEnter(character);
			}
		}
		else
		{
			if (_characterList.containsKey(character.getObjectId()))
			{
				_characterList.remove(character.getObjectId());
				onExit(character);
			}
		}
	}

	protected abstract void onEnter(L2Character cha);
	protected abstract void onExit(L2Character character);
	public abstract void onDieInside(L2Character character);
	public abstract void onReviveInside(L2Character character);

	public FastMap<Integer, L2Character> getCharactersInside()
	{
		return _characterList;
	}

	public void removeCharacter(L2Character character)
	{
		if (_characterList.containsKey(character.getObjectId()))
		{
			_characterList.remove(character.getObjectId());
			onExit(character);
		}
	}

	public boolean isCharacterInZone(L2Character character)
	{
		return _characterList.containsKey(character.getObjectId());
	}

	public boolean isCorrectType(L2Character character)
	{
		switch (_affected)
		{
			case PLAYABLE:
				return character instanceof L2PlayableInstance;
			case PC:
				return character instanceof L2PcInstance;
			case NPC:
				return character instanceof L2NpcInstance;
			case ALL:
				return true;
		}
		return false;
	}

	/**
	 * Checks if the given coordinates are within the zone
	 * @param x
	 * @param y
	 */
	public boolean isInsideZone(int x, int y)
	{
		boolean inside = false;
		for (Shape sh : _shapes)
		{
			if (sh.contains(x, y))
			{
				inside = true;
				break;
			}
		}

		if (_exShapes != null && inside)
		{
			for (Shape sh : _exShapes)
			{
				if (sh.contains(x, y))
				{
					inside = false;
					break;
				}
			}
		}
		return inside;
	}

	/**
	 * Checks if the given coordinates are within the zone
	 * @param x
	 * @param y
	 * @param z
	 */
	public boolean isInsideZone(int x, int y, int z)
	{
		boolean inside = false;
		for (Shape sh : _shapes)
		{
			if (sh.contains(x, y, z))
			{
				inside = true;
				break;
			}
		}

		if (_exShapes != null && inside)
		{
			for (Shape sh : _exShapes)
			{
				if (sh.contains(x, y, z))
				{
					inside = false;
					break;
				}
			}
		}
		return inside;
	}

	/**
	 * Checks if the given obejct is inside the zone.
	 *
	 * @param object
	 */
	public boolean isInsideZone(L2Object object)
	{
		return isInsideZone(object.getX(), object.getY(), object.getZ());
	}

	public double getDistanceToZone(L2Object object)
	{
		return getDistanceToZone(object.getX(), object.getY());
	}

	public double getDistanceToZone(int x, int y)
	{
		double dist = Double.MAX_VALUE;
		for (Shape sh : _shapes)
		{
			dist = Math.min(dist, sh.getDistanceToZone(x, y));
		}
		return dist;
	}

	public int getMiddleX()
	{
		if (_shapes.length == 0)
		{
			_log.error(this + " has no shapes defined");
			return 0;
		}

		int sum = 0;
		for (Shape sh : _shapes)
		{
			sum += sh.getMiddleX();
		}
		return (sum / _shapes.length);
	}

	public int getMiddleY()
	{
		if (_shapes.length == 0)
		{
			_log.error(this + " has no shapes defined");
			return 0;
		}

		int sum = 0;
		for (Shape sh : _shapes)
		{
			sum += sh.getMiddleY();
		}
		return (sum / _shapes.length);
	}

	public boolean intersectsRectangle(int ax, int bx, int ay, int by)
	{
		for (Shape sh : _shapes)
		{
			if (sh.intersectsRectangle(ax, bx, ay, by))
				return true;
		}
		return false;
	}

	public int getMaxZ(L2Object obj)
	{
		return getMaxZ(obj.getX(), obj.getY(), obj.getZ());
	}

	public int getMinZ(L2Object obj)
	{
		return getMinZ(obj.getX(), obj.getY(), obj.getZ());
	}

	public int getMaxZ(int x, int y, int z)
	{
		for (Shape sh : _shapes)
		{
			if (sh.contains(x, y))
				return sh.getMaxZ();
		}
		return z;
	}

	public int getMinZ(int x, int y, int z)
	{
		for (Shape sh : _shapes)
		{
			if (sh.contains(x, y))
				return sh.getMinZ();
		}
		return z;
	}

	public Location getRandomLocation()
	{
		if (_shapes.length == 0)
		{
			_log.error(this + " has no shapes defined");
			return new Location(0, 0, 0);
		}
		
		return _shapes[Rnd.nextInt(_shapes.length)].getRandomLocation();
	}

	/**
	* Some GrandBosses send all players in zone to a specific part of the zone,
	* rather than just removing them all. If this is the case, this command should
	* be used.
	* 
	* @param x
	* @param y
	* @param z
	*/
	public void movePlayersTo(int x, int y, int z)
	{
		if (_characterList == null)
			return;
		if (_characterList.isEmpty())
			return;
		for (L2Character character : _characterList.values())
		{
			if (character == null)
				continue;
			if (character instanceof L2PcInstance && ((L2PcInstance)character).isOnline() == 1)
			{
				character.teleToLocation(x, y, z);
			}
		}
	}

	public static L2Zone parseZone(Node zn)
	{
		String type = "Default";
		String name = "";
		int id = 0;
		boolean enabled = true;
		L2Zone zone = null;
		Class<?> clazz;
		Constructor<?> constructor;
		try
		{
			id = Integer.parseInt(zn.getAttributes().getNamedItem("id").getNodeValue());
			Node tn = zn.getAttributes().getNamedItem("type");
			Node nn = zn.getAttributes().getNamedItem("name");
			Node en = zn.getAttributes().getNamedItem("enabled");
			if (tn != null)
				type = tn.getNodeValue();
			if (en != null)
				enabled = Boolean.parseBoolean(en.getNodeValue());

			name = (nn != null) ? nn.getNodeValue() : Integer.valueOf(id).toString();

			clazz = Class.forName("com.l2jfree.gameserver.model.zone.L2"+type+"Zone");
			constructor = clazz.getConstructor();
			zone = (L2Zone)constructor.newInstance();
		}
		catch (Exception e)
		{
			_log.error("Cannot create a L2"+type+"Zone for id "+id, e);
			return null;
		}

		zone._id = id;
		zone._type = ZoneType.valueOf(type);
		zone._name = name;
		zone._enabled = enabled;

		FastList<Shape> shapes = new FastList<Shape>();
		FastList<Shape> exShapes = new FastList<Shape>();
		for (Node n = zn.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("shape".equalsIgnoreCase(n.getNodeName()))
			{
				Shape sh = Shape.parseShape(n, id);
				if(sh != null)
				{
					if(sh.isExclude())
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
				catch(Exception e)
				{
					_log.error("Cannot parse entity for zone "+zone.getName()+" ("+zone.getId()+")", e);
					return null;
				}
			}
			else if ("instance".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseInstance(n);
				}
				catch(Exception e)
				{
					_log.error("Cannot parse instance for zone "+zone.getName()+" ("+zone.getId()+")", e);
					return null;
				}
			}
			else if ("settings".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseSettings(n);
				}
				catch(Exception e)
				{
					_log.error("Cannot parse settings for zone "+zone.getName()+" ("+zone.getId()+")", e);
					return null;
				}
			}
			else if ("msg".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseMessages(n);
				}
				catch(Exception e)
				{
					_log.error("Cannot parse messages for zone "+zone.getName()+" ("+zone.getId()+")", e);
					return null;
				}
			}
			else if ("skill".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseSkills(n);
				}
				catch(Exception e)
				{
					_log.error("Cannot parse skills for zone "+zone.getName()+" ("+zone.getId()+")", e);
					return null;
				}
			}
			else if ("cond".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseCondition(n.getFirstChild());
				}
				catch(Exception e)
				{
					_log.error("Cannot parse skills for zone "+zone.getName()+" ("+zone.getId()+")", e);
					return null;
				}
			}
			else if ("restart_chaotic".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseRestart(n, RestartType.CHAOTIC);
				}
				catch(Exception e)
				{
					_log.error("Cannot parse chaotic restart point for zone "+zone.getName()+" ("+zone.getId()+")", e);
					return null;
				}
			}
			else if ("restart_owner".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					zone.parseRestart(n, RestartType.OWNER);
				}
				catch(Exception e)
				{
					_log.error("Cannot parse owner restart point for zone "+zone.getName()+" ("+zone.getId()+")", e);
					return null;
				}
			}
		}
		zone._shapes = shapes.toArray(new Shape[shapes.size()]);
		if (!exShapes.isEmpty())
			zone._exShapes = exShapes.toArray(new Shape[exShapes.size()]);
		shapes.clear();
		exShapes.clear();

		zone.register();

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

		if(!getRestartMap().containsKey(t))
			getRestartMap().put(t, new FastList<Location>());

		getRestartMap().get(t).add(new Location(x, y, z));
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
		Node envSlow = n.getAttributes().getNamedItem("slow");
		Node noHeal = n.getAttributes().getNamedItem("noHeal");
		Node padown = n.getAttributes().getNamedItem("pAtkDown");
		Node pddown = n.getAttributes().getNamedItem("pDefDown");

		_pvp = (pvp != null) ? PvpSettings.valueOf(pvp.getNodeValue().toUpperCase()) : PvpSettings.GENERAL;
		_noLanding = (noLanding != null) && Boolean.parseBoolean(noLanding.getNodeValue());
		_noEscape = (noEscape != null) && Boolean.parseBoolean(noEscape.getNodeValue());
		_noPrivateStore = (noPrivateStore != null) && Boolean.parseBoolean(noPrivateStore.getNodeValue());
		_noSummon = (noSummon != null) && Boolean.parseBoolean(noSummon.getNodeValue());
		_abnormal = (abnorm != null) ? Integer.decode("0x"+abnorm.getNodeValue()) : 0;
		_exitOnDeath = (exitOnDeath != null) && Boolean.parseBoolean(exitOnDeath.getNodeValue());
		_hpDamage = (hpDamage != null) ? Integer.parseInt(hpDamage.getNodeValue()) : 0;
		_mpDamage = (mpDamage != null) ? Integer.parseInt(mpDamage.getNodeValue()) : 0;
		_buffRepeat = (buffRepeat != null) && Boolean.parseBoolean(buffRepeat.getNodeValue());
		_envSlow = (envSlow != null) && Boolean.parseBoolean(envSlow.getNodeValue());
		_noHeal = (noHeal != null) && Boolean.parseBoolean(noHeal.getNodeValue());
		_padown = (padown != null) && Boolean.parseBoolean(padown.getNodeValue());
		_pddown = (pddown != null) && Boolean.parseBoolean(pddown.getNodeValue());
		if (boss != null)
			_boss = Boss.valueOf(boss.getNodeValue().toUpperCase());
		if (affected != null)
			_affected = Affected.valueOf(affected.getNodeValue().toUpperCase());
		if (_affected == null)
			_affected = Affected.PLAYABLE;
	}

	private void parseMessages(Node n) throws Exception
	{
		Node enter = n.getAttributes().getNamedItem("onEnter");
		Node exit = n.getAttributes().getNamedItem("onExit");
		int msg = -1;

		if(enter != null)
		{
			String onEnter = enter.getNodeValue();

			try
			{
				msg = Integer.parseInt(onEnter);
			}
			catch(NumberFormatException nfe){}
			
			if(msg != -1)
				_onEnterMsg = new SystemMessage(SystemMessageId.getSystemMessageId(msg));
			else
				_onEnterMsg = SystemMessage.sendString(onEnter);
		}
		else
			_onEnterMsg = null;

		if(exit != null)
		{
			String onExit = exit.getNodeValue();
			msg = -1;
			try
			{
				msg = Integer.parseInt(onExit);
			}
			catch(NumberFormatException nfe){}
			
			if(msg != -1)
				_onExitMsg = new SystemMessage(SystemMessageId.getSystemMessageId(msg));
			else
				_onExitMsg = SystemMessage.sendString(onExit);
		}
		else
			_onExitMsg = null;
	}

	private void parseSkills(Node n) throws Exception
	{
		Node aen = n.getAttributes().getNamedItem("applyEnter");
		Node aex = n.getAttributes().getNamedItem("applyExit");
		Node ren = n.getAttributes().getNamedItem("removeEnter");
		Node rex = n.getAttributes().getNamedItem("removeExit");
		
		if(aen != null)
		{
			_applyEnter = new FastList<L2Skill>();
			parseApplySkill(_applyEnter, aen.getNodeValue());
		}
		if(aex != null)
		{
			_applyExit = new FastList<L2Skill>();
			parseApplySkill(_applyExit, aex.getNodeValue());
		}
		if(ren != null)
		{
			_removeEnter = new FastList<L2Skill>();
			parseRemoveSkill(_removeEnter, ren.getNodeValue());
		}
		if(rex != null)
		{
			_removeExit = new FastList<L2Skill>();
			parseRemoveSkill(_removeExit, rex.getNodeValue());
		}
	}

	private void parseApplySkill(FastList<L2Skill> list, String set)
	{
		StringTokenizer st = new StringTokenizer(set, ";");
		while(st.hasMoreTokens())
		{
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), ",");
			int skillId = Integer.parseInt(st2.nextToken());
			int level = Integer.parseInt(st2.nextToken());

			L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
			
			if(skill != null)
				list.add(skill);
		}
	}

	private void parseRemoveSkill(FastList<L2Skill> list, String set)
	{
		StringTokenizer st = new StringTokenizer(set, ";");
		while(st.hasMoreTokens())
		{
			int skillId = Integer.parseInt(st.nextToken());
			L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
			if(skill != null)
				list.add(skill);
		}
	}

	@Override
	public String toString()
	{
		return getClassName() + "[id='" + getId() + "',name='" + getName() + "']";
	}

	protected void parseCondition(Node n) throws Exception
	{
		throw new IllegalStateException("This zone shouldn't have conditions!");
	}

	protected List<Func> getStatFuncs(L2Character player)
	{
		if (_funcTemplates == null)
			return L2Collections.emptyList();
		
		List<Func> funcs = new ArrayList<Func>(_funcTemplates.length);
		for (FuncTemplate t : _funcTemplates)
		{
			Env env = new Env();
			env.player = player;
			env.target = player;
			Func f = t.getFunc(env, this);
			if (f != null)
				funcs.add(f);
		}
		
		return funcs;
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
}
