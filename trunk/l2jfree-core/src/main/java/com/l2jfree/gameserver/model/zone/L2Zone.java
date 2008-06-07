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
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.Location;
import com.l2jfree.gameserver.model.zone.form.Shape;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.skills.Env;
import com.l2jfree.gameserver.model.base.Race;
import com.l2jfree.gameserver.skills.conditions.Condition;
import com.l2jfree.gameserver.skills.conditions.ConditionChangeWeapon;
import com.l2jfree.gameserver.skills.conditions.ConditionGameChance;
import com.l2jfree.gameserver.skills.conditions.ConditionGameTime;
import com.l2jfree.gameserver.skills.conditions.ConditionLogicAnd;
import com.l2jfree.gameserver.skills.conditions.ConditionLogicNot;
import com.l2jfree.gameserver.skills.conditions.ConditionLogicOr;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerCp;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerHp;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerHpPercentage;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerLevel;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerMp;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerRace;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerState;
import com.l2jfree.gameserver.skills.conditions.ConditionSlotItemId;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetActiveEffectId;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetActiveSkillId;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetAggro;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetClassIdRestriction;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetLevel;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetRaceId;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetUndead;
import com.l2jfree.gameserver.skills.conditions.ConditionTargetUsesWeaponKind;
import com.l2jfree.gameserver.skills.conditions.ConditionUsingItemType;
import com.l2jfree.gameserver.skills.conditions.ConditionGameTime.CheckGameTime;
import com.l2jfree.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import com.l2jfree.gameserver.skills.Stats;
import com.l2jfree.gameserver.templates.L2ArmorType;
import com.l2jfree.gameserver.templates.L2WeaponType;
import com.l2jfree.tools.random.Rnd;

public abstract class L2Zone
{
	protected static Log _log = LogFactory.getLog(L2Zone.class.getName());

	public static enum ZoneType
	{
		Arena,
		Boss,
		Castle,
		CastleTeleport,
		Clanhall,
		Damage,
		Default,
		DefenderSpawn,
		Fishing,
		Fort,
		HeadQuarters,
		Jail,
		Mothertree,
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

	public static final int FLAG_PVP = 1;
	public static final int FLAG_PEACE = 2;
	public static final int FLAG_SIEGE = 4;
	public static final int FLAG_MOTHERTREE = 8;
	public static final int FLAG_CLANHALL = 16;
	public static final int FLAG_NOESCAPE = 32;
	public static final int FLAG_NOLANDING = 64;
	public static final int FLAG_NOSTORE = 128;
	public static final int FLAG_WATER = 256;
	public static final int FLAG_FISHING = 512;
	public static final int FLAG_JAIL = 1024;
	public static final int FLAG_STADIUM = 2048;
	public static final int FLAG_SUNLIGHTROOM = 4096;
	public static final int FLAG_DANGER = 8192;
	public static final int FLAG_CASTLE = 16384;

	protected int _id;
	protected String _name;

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
	
	protected boolean _noEscape, _noLanding, _noPrivateStore;

	protected SystemMessage _onEnterMsg, _onExitMsg;

	protected int _abnormal;
	protected int _damage;

	protected boolean _exitOnDeath;

	protected FastList<L2Skill> _applyEnter, _applyExit, _removeEnter, _removeExit;

	protected Condition _cond;

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

	public String getClassName()
	{
		String[] parts = this.getClass().toString().split("\\.");
		return parts[parts.length-1];
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

	public int getDamagePerSecond()
	{
		return _damage;
	}

	public boolean isPeace()
	{
		return _pvp == PvpSettings.PEACE;
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

	protected boolean checkCondition(L2Character character)
	{
		if (_cond == null)
			return true;

		// Works with ConditionPlayer* and ConditionTarget* and some other

		Env env = new Env();
		env.player = character;
		env.target = character;
		return _cond.test(env);
	}

	public void revalidateInZone(L2Character character)
	{
		if (checkCondition(character) && isInsideZone(character))
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

	/**
	 * Checks if the given coordinates are within the zone
	 * @param x
	 * @param y
	 * @param z
	 */
	public boolean isInsideZone(int x, int y)
	{
		boolean inside = false;
		for(Shape sh : _shapes)
		{
			if(sh.contains(x, y))
			{
				inside = true;
				break;
			}
		}

		if(_exShapes != null)
		{
			for(Shape sh : _exShapes)
			{
				if(sh.contains(x, y))
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
		for(Shape sh : _shapes)
		{
			if(sh.contains(x, y, z))
			{
				inside = true;
				break;
			}
		}

		if(_exShapes != null)
		{
			for(Shape sh : _exShapes)
			{
				if(sh.contains(x, y, z))
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
		for(Shape sh : _shapes)
		{
			dist = Math.min(dist, sh.getDistanceToZone(x, y));
		}
		return dist;
	}

	public int getMiddleX()
	{
		if(_shapes.length == 0)
		{
			_log.error(getClassName()+" \""+getName()+"\" "+getId()+" has no shapes defined");
			return 0;
		}

		int sum = 0;
		for(Shape sh : _shapes)
		{
			sum += sh.getMiddleX();
		}
		return (int)(sum / _shapes.length);
	}

	public int getMiddleY()
	{
		if(_shapes.length == 0)
		{
			_log.error(getClassName()+" \""+getName()+"\" "+getId()+" has no shapes defined");
			return 0;
		}

		int sum = 0;
		for(Shape sh : _shapes)
		{
			sum += sh.getMiddleY();
		}
		return (int)(sum / _shapes.length);
	}

	public boolean intersectsRectangle(int ax, int bx, int ay, int by)
	{
		for(Shape sh : _shapes)
		{
			if(sh.intersectsRectangle(ax, bx, ay, by))
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
		for(Shape sh : _shapes)
		{
			if(sh.contains(x, y))
				return sh.getMaxZ();
		}
		return z;
	}

	public int getMinZ(int x, int y, int z)
	{
		for(Shape sh : _shapes)
		{
			if(sh.contains(x, y))
				return sh.getMinZ();
		}
		return z;
	}

	public Location getRandomLocation()
	{
		if(_shapes.length == 0)
		{
			_log.error(getClassName()+" \""+getName()+"\" "+getId()+" has no shapes defined");
			return new Location(0, 0, 0);
		}
		
		return _shapes[Rnd.nextInt(_shapes.length)].getRandomLocation();
	}

	public static L2Zone parseZone(Node zn)
	{
		String type = "Default";
		String name = "";
		int id = 0;
		L2Zone zone = null;
		Class<?> clazz;
		Constructor<?> constructor;
		try
		{
			id = Integer.parseInt(zn.getAttributes().getNamedItem("id").getNodeValue());
			Node tn = zn.getAttributes().getNamedItem("type");
			Node nn = zn.getAttributes().getNamedItem("name");
			if(tn != null)
				type = tn.getNodeValue();

			name = (nn != null) ? nn.getNodeValue() : new Integer(id).toString();

			clazz = Class.forName("com.l2jfree.gameserver.model.zone.L2"+type+"Zone");
			constructor = clazz.getConstructor();
			zone = (L2Zone)constructor.newInstance();
		}
		catch (Exception e)
		{
			_log.error("Cannot create a L2"+type+"Zone for id "+id);
			return null;
		}

		zone._id = id;
		zone._type = ZoneType.valueOf(type);
		zone._name = name;

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
					_log.error("Cannot parse entity for zone "+zone.getName()+" ("+zone.getId()+")");
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
					_log.error("Cannot parse settings for zone "+zone.getName()+" ("+zone.getId()+")");
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
					_log.error("Cannot parse messages for zone "+zone.getName()+" ("+zone.getId()+")");
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
					_log.error("Cannot parse skills for zone "+zone.getName()+" ("+zone.getId()+")");
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
					_log.error("Cannot parse skills for zone "+zone.getName()+" ("+zone.getId()+")");
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
					_log.error("Cannot parse chaotic restart point for zone "+zone.getName()+" ("+zone.getId()+")");
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
					_log.error("Cannot parse owner restart point for zone "+zone.getName()+" ("+zone.getId()+")");
					return null;
				}
			}
		}
		zone._shapes = shapes.toArray(new Shape[shapes.size()]);
		if(exShapes.size() > 0)
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

	private void parseSettings(Node n) throws Exception
	{
		Node pvp = n.getAttributes().getNamedItem("pvp");
		Node noLanding = n.getAttributes().getNamedItem("noLanding");
		Node noEscape = n.getAttributes().getNamedItem("noEscape");
		Node noPrivateStore = n.getAttributes().getNamedItem("noPrivateStore");
		Node boss = n.getAttributes().getNamedItem("boss");
		Node abnorm = n.getAttributes().getNamedItem("abnormal");
		Node exitOnDeath = n.getAttributes().getNamedItem("exitOnDeath");
		Node damage = n.getAttributes().getNamedItem("damage");
		
		_pvp = (pvp != null) ? PvpSettings.valueOf(pvp.getNodeValue().toUpperCase()) : PvpSettings.GENERAL;
		_noLanding = (noLanding != null) ? Boolean.parseBoolean(noLanding.getNodeValue()) : false;
		_noEscape = (noEscape != null) ? Boolean.parseBoolean(noEscape.getNodeValue()) : false;
		_noPrivateStore = (noPrivateStore != null) ? Boolean.parseBoolean(noPrivateStore.getNodeValue()) : false;
		_abnormal = (abnorm != null) ? Integer.decode("0x"+abnorm.getNodeValue()) : 0;
		_exitOnDeath = (exitOnDeath != null) ? Boolean.parseBoolean(exitOnDeath.getNodeValue()) : false;
		_damage = (damage != null) ? Integer.parseInt(damage.getNodeValue()) : 0;
		if(boss != null)
			_boss = Boss.valueOf(boss.getNodeValue().toUpperCase());
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

	private void parseCondition(Node n) throws Exception
	{
		_cond = parseCondition(n, this);
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

	private Condition parseCondition(Node n, Object template)
	{
		while (n != null && n.getNodeType() != Node.ELEMENT_NODE)
			n = n.getNextSibling();
		if (n == null)
			return null;
		if ("and".equalsIgnoreCase(n.getNodeName()))
			return parseLogicAnd(n, template);
		if ("or".equalsIgnoreCase(n.getNodeName()))
			return parseLogicOr(n, template);
		if ("not".equalsIgnoreCase(n.getNodeName()))
			return parseLogicNot(n, template);
		if ("player".equalsIgnoreCase(n.getNodeName()))
			return parsePlayerCondition(n);
		if ("target".equalsIgnoreCase(n.getNodeName()))
			return parseTargetCondition(n, template);
		if ("game".equalsIgnoreCase(n.getNodeName()))
			return parseGameCondition(n);
		return null;
	}

	private Condition parseLogicAnd(Node n, Object template)
	{
		ConditionLogicAnd cond = new ConditionLogicAnd();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
				cond.add(parseCondition(n, template));
		}
		if (cond.conditions == null || cond.conditions.length == 0)
			_log.fatal("Empty <and> condition in zone " + _name);
		return cond;
	}

	private Condition parseLogicOr(Node n, Object template)
	{
		ConditionLogicOr cond = new ConditionLogicOr();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
				cond.add(parseCondition(n, template));
		}
		if (cond.conditions == null || cond.conditions.length == 0)
			_log.fatal("Empty <or> condition in zone " + _name);
		return cond;
	}

	private Condition parseLogicNot(Node n, Object template)
	{
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeType() == Node.ELEMENT_NODE)
			{
				return new ConditionLogicNot(parseCondition(n, template));
			}
		}
		_log.fatal("Empty <not> condition in zone " + _name);
		return null;
	}

	private Condition parsePlayerCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("race".equalsIgnoreCase(a.getNodeName()))
			{
				Race race = Race.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerRace(race));
			}
			else if ("level".equalsIgnoreCase(a.getNodeName()))
			{
				int lvl = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerLevel(lvl));
			}
			else if ("resting".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.RESTING, val));
			}
			else if ("moving".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.MOVING, val));
			}
			else if ("running".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.RUNNING, val));
			}
			else if ("flying".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerState(CheckPlayerState.FLYING, val));
			}
			else if ("hp".equalsIgnoreCase(a.getNodeName()))
			{
				int hp = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerHp(hp));
			}
			else if ("hprate".equalsIgnoreCase(a.getNodeName()))
			{
				double rate = Double.parseDouble(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerHpPercentage(rate));
			}
			else if ("mp".equalsIgnoreCase(a.getNodeName()))
			{
				int mp = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerMp(mp));
			}
			else if ("cp".equalsIgnoreCase(a.getNodeName()))
			{
				int cp = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionPlayerCp(cp));
			}
		}

		if (cond == null)
			_log.fatal("Unrecognized <player> condition in zone " + _name);
		return cond;
	}

	private Condition parseTargetCondition(Node n, Object template)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("aggro".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetAggro(val));
			}
			else if ("level".equalsIgnoreCase(a.getNodeName()))
			{
				int lvl = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetLevel(lvl));
			}
			else if ("class_id_restriction".equalsIgnoreCase(a.getNodeName()))
			{
				FastList<Integer> array = new FastList<Integer>();
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					array.add(Integer.decode(item));
				}
				cond = joinAnd(cond, new ConditionTargetClassIdRestriction(array));
			}
			else if ("active_effect_id".equalsIgnoreCase(a.getNodeName()))
			{
				int effect_id = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetActiveEffectId(effect_id));
			}
			else if ("active_skill_id".equalsIgnoreCase(a.getNodeName()))
			{
				int skill_id = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
			}
			else if ("race_id".equalsIgnoreCase(a.getNodeName()))
			{
				ArrayList<Integer> array = new ArrayList<Integer>();
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					//-1 because we want to take effect for exactly race that is by -1 lower in FastList
					array.add(Integer.decode(item) - 1);
				}
				cond = joinAnd(cond, new ConditionTargetRaceId(array));
			}
			else if ("undead".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionTargetUndead(val));
			}
			else if ("using".equalsIgnoreCase(a.getNodeName()))
			{
				int mask = 0;
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					for (L2WeaponType wt : L2WeaponType.values())
					{
						if (wt.toString().equals(item))
						{
							mask |= wt.mask();
							break;
						}
					}
					for (L2ArmorType at : L2ArmorType.values())
					{
						if (at.toString().equals(item))
						{
							mask |= at.mask();
							break;
						}
					}
				}
				cond = joinAnd(cond, new ConditionTargetUsesWeaponKind(mask));
			}
		}
		if (cond == null)
			_log.fatal("Unrecognized <target> condition in zone " + _name);
		return cond;
	}

	private Condition parseUsingCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("kind".equalsIgnoreCase(a.getNodeName()))
			{
				int mask = 0;
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
				while (st.hasMoreTokens())
				{
					String item = st.nextToken().trim();
					for (L2WeaponType wt : L2WeaponType.values())
					{
						if (wt.toString().equals(item))
						{
							mask |= wt.mask();
							break;
						}
					}
					for (L2ArmorType at : L2ArmorType.values())
					{
						if (at.toString().equals(item))
						{
							mask |= at.mask();
							break;
						}
					}
				}
				cond = joinAnd(cond, new ConditionUsingItemType(mask));
			}
			else if ("slotitem".equalsIgnoreCase(a.getNodeName()))
			{
				StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
				int id = Integer.parseInt(st.nextToken().trim());
				int slot = Integer.parseInt(st.nextToken().trim());
				int enchant = 0;
				if (st.hasMoreTokens())
					enchant = Integer.parseInt(st.nextToken().trim());
				cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
			}
			else if ("weaponChange".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionChangeWeapon(val));
			}
		}
		if (cond == null)
			_log.fatal("Unrecognized <using> condition in zone " + _name);
		return cond;
	}

	private Condition parseGameCondition(Node n)
	{
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++)
		{
			Node a = attrs.item(i);
			if ("night".equalsIgnoreCase(a.getNodeName()))
			{
				boolean val = Boolean.valueOf(a.getNodeValue());
				cond = joinAnd(cond, new ConditionGameTime(CheckGameTime.NIGHT, val));
			}
			if ("chance".equalsIgnoreCase(a.getNodeName()))
			{
				int val = Integer.decode(a.getNodeValue());
				cond = joinAnd(cond, new ConditionGameChance(val));
			}
		}
		if (cond == null)
			_log.fatal("Unrecognized <game> condition in zone " + _name);
		return cond;
	}

	private Condition joinAnd(Condition cond, Condition c)
	{
		if (cond == null)
			return c;
		if (cond instanceof ConditionLogicAnd)
		{
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}
}
