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
package com.l2jfree.gameserver.model.entity;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jfree.Config;
import com.l2jfree.gameserver.Announcements;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.datatables.DoorTable;
import com.l2jfree.gameserver.datatables.NpcTable;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.instancemanager.InstanceManager;
import com.l2jfree.gameserver.instancemanager.MapRegionManager;
import com.l2jfree.gameserver.model.L2Spawn;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.mapregion.TeleportWhereType;
import com.l2jfree.gameserver.network.SystemChatChannelId;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.CreatureSay;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.util.L2FastSet;

/**
 * @author evill33t
 * 
 */
public class Instance
{
	private final static Log			_log				= LogFactory.getLog(Instance.class);

	private final int					_id;
	private int							_template;
	private int							_tpx;
	private int							_tpy;
	private int							_tpz;
	private String						_name;
	private final Set<Integer>			_players			= new L2FastSet<Integer>().setShared(true);
	private final Set<L2Npc>			_npcs				= new L2FastSet<L2Npc>().setShared(true);
	private final Map<Integer, L2DoorInstance> _doors		= new FastMap<Integer, L2DoorInstance>().setShared(true);
	private L2DoorInstance[]			_doorArray;
	private int[]						_spawnLoc;
	private boolean						_allowSummon		= true;
	private boolean						_isPvPInstance		= false;
	protected ScheduledFuture<?>		_checkTimeUpTask	= null;
	private long						_emptyDestroyTime	= -1;
	private long						_lastLeft			= -1;
	private long						_instanceEndTime	= -1;

	public Instance(int id)
	{
		_id = id;
		_template = -1;
	}

	public int getId()
	{
		return _id;
	}

	public int getTemplate()
	{
		return _template;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public void setReturnTeleport(int tpx, int tpy, int tpz)
	{
		_tpx = tpx;
		_tpy = tpy;
		_tpz = tpz;
	}

	public int[] getReturnTeleport()
	{
		int[] returnCoords = {_tpx,_tpy,_tpz};
		return returnCoords;
	}

	/**
	 * Returns whether summon friend type skills are allowed for this instance
	 */
	public boolean isSummonAllowed()
	{
		return _allowSummon;
	}

	/**
	 * Sets the status for the instance for summon friend type skills
	 */
	public void setAllowSummon(boolean b)
	{
		_allowSummon = b;
	}

	/**
	 * Returns true if entire instance is PvP zone
	 */
	public boolean isPvPInstance()
	{
		return _isPvPInstance;
	}

	/**
	 * Sets PvP zone status of the instance
	 */
	public void setPvPInstance(boolean b)
	{
		_isPvPInstance = b;
	}

	/**
	 * Set the instance duration task
	 * @param duration in milliseconds
	 */
	public void setDuration(int duration)
	{
		if (_checkTimeUpTask != null)
			_checkTimeUpTask.cancel(true);

		_checkTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(duration), 500);
		_instanceEndTime = System.currentTimeMillis() + duration + 500;
	}

	/**
	 * Set time before empty instance will be removed
	 * 
	 * @param time in milliseconds
	 */
	public void setEmptyDestroyTime(long time)
	{
		_emptyDestroyTime = time;
	}

	public boolean containsPlayer(Integer objectId)
	{
		return _players.contains(objectId);
	}

	public void addPlayer(Integer objectId)
	{
		_players.add(objectId);
	}

	public void removePlayer(Integer objectId)
	{
		if (_players.remove(objectId) && _players.isEmpty() && _emptyDestroyTime >= 0)
		{
			_lastLeft = System.currentTimeMillis();
			setDuration((int) (_instanceEndTime - System.currentTimeMillis() - 1000));
		}
	}

	public void ejectPlayer(Integer objectId)
	{
		L2PcInstance player = L2World.getInstance().findPlayer(objectId);
		if (player != null && player.isSameInstance(getId()))
		{
			if (!player.isInMultiverse())
				player.setInstanceId(0);
			//player.sendPacket(SystemMessageId.INSTANCE_ZONE_DELETED_CANT_ACCESSED);
			if (_tpx == 0 || _tpy == 0 || _tpz == 0)
				player.teleToLocation(TeleportWhereType.Town);
			else
				player.teleToLocation(_tpx, _tpy, _tpz);
		}
	}

	public void addNpc(L2Npc npc)
	{
		_npcs.add(npc);
	}

	public void removeNpc(L2Npc npc)
	{
		_npcs.remove(npc);
	}

	public void removeDoor(L2DoorInstance door)
	{
		_doors.remove(door.getDoorId());
		_doorArray = null;
	}

	/**
	 * Adds a door into the instance
	 * @param doorId - from doors.csv
	 * @param open - initial state of the door
	 */
	public void addDoor(int doorId, boolean open)
	{
		if (_doors.containsKey(doorId))
		{
			_log.warn("Door ID " + doorId + " already exists in instance " + getId());
			return;
		}
		
		L2DoorInstance temp = DoorTable.getInstance().getDoor(doorId);
		L2DoorInstance newdoor = new L2DoorInstance(IdFactory.getInstance().getNextId(), temp.getTemplate(), temp.getDoorId(), temp.getName(), temp.isUnlockable());
		newdoor.setInstanceId(getId());
		newdoor.setRange(temp.getXMin(), temp.getYMin(), temp.getZMin(), temp.getXMax(), temp.getYMax(), temp.getZMax());
		try
		{
			newdoor.setMapRegion(MapRegionManager.getInstance().getRegion(temp.getX(), temp.getY(), temp.getZ()));
		}
		catch (Exception e)
		{
			_log.fatal("Error in door data, ID:" + temp.getDoorId(), e);
		}
		newdoor.getStatus().setCurrentHpMp(newdoor.getMaxHp(), newdoor.getMaxMp());
		newdoor.setOpen(open);
		newdoor.getPosition().setXYZInvisible(temp.getX(), temp.getY(), temp.getZ());
		newdoor.spawnMe(newdoor.getX(), newdoor.getY(), newdoor.getZ());

		_doors.put(newdoor.getDoorId(), newdoor);
		_doorArray = null;
	}

	public Set<Integer> getPlayers()
	{
		return _players;
	}

	public Set<L2Npc> getNpcs()
	{
		return _npcs;
	}

	public L2DoorInstance[] getDoors()
	{
		if (_doorArray == null)
			_doorArray = _doors.values().toArray(new L2DoorInstance[_doors.size()]);
		
		return _doorArray;
	}

	public L2DoorInstance getDoor(int doorId)
	{
		return _doors.get(doorId);
	}

	/**
	 * Returns the spawn location for this instance to be used when leaving the instance
	 * @return int[3]
	 */
	public int[] getSpawnLoc()
	{
		return _spawnLoc;
	}

	public void setSpawnLoc(int[] loc)
	{
		if (loc == null || loc.length < 3)
			return;
		System.arraycopy(loc, 0, _spawnLoc, 0, 3);
	}

	public void removePlayers()
	{
		for (Integer objectId : _players)
		{
			removePlayer(objectId);
			ejectPlayer(objectId);
		}
		_players.clear();
	}

	public void removeNpcs()
	{
		for (L2Npc mob : _npcs)
		{
			if (mob != null)
			{
				if (mob.getSpawn() != null)
					mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
		}
		_npcs.clear();
	}

	public void removeDoors()
	{
		for (L2DoorInstance door : _doors.values())
		{
			if (door != null)
				door.decayMe();
		}
		_doors.clear();
		_doorArray = null;
	}

	public void loadInstanceTemplate(String filename)
	{
		Document doc = null;
		File xml = new File(Config.DATAPACK_ROOT, "data/instances/" + filename);

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false); // DTD isn't used
			factory.setIgnoringComments(true);
			// Such validation will not find element 'instance' 
			//SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			//factory.setSchema(sf.newSchema(new File(Config.DATAPACK_ROOT, "data/templates/instances.xsd")));
			doc = factory.newDocumentBuilder().parse(xml);

			for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("instance".equalsIgnoreCase(n.getNodeName()))
				{
					parseInstance(n);
				}
			}
		}
		catch (IOException e)
		{
			_log.warn("Instance: can not find " + xml.getAbsolutePath() + " !", e);
		}
		catch (Exception e)
		{
			_log.warn("Instance: error while loading " + xml.getAbsolutePath() + " !", e);
		}
	}

	private void parseInstance(Node n) throws Exception
	{
		L2Spawn spawnDat;
		L2NpcTemplate npcTemplate;
		String name;
		name = n.getAttributes().getNamedItem("name").getNodeValue();
		setName(name);
		Node template = n.getAttributes().getNamedItem("template");
		if (template != null)
			_template = Integer.parseInt(template.getNodeValue());

		Node a;
		Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("activityTime".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
				{
					_checkTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(Integer.parseInt(a.getNodeValue()) * 60000), 15000);
					_instanceEndTime = System.currentTimeMillis() + _checkTimeUpTask.getDelay(TimeUnit.MILLISECONDS);
				}
			}
			/*else if ("timeDelay".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
					setTimeDelay(Integer.parseInt(a.getNodeValue()));
			}*/
			else if ("allowSummon".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
					setAllowSummon(Boolean.parseBoolean(a.getNodeValue()));
			}
			else if ("emptyDestroyTime".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
					_emptyDestroyTime = Long.parseLong(a.getNodeValue()) * 1000;
			}
			else if ("PvPInstance".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
					setPvPInstance(Boolean.parseBoolean(a.getNodeValue()));
			}
			else if ("returnTeleport".equalsIgnoreCase(n.getNodeName()))
			{
					int tpx = 0, tpy = 0, tpz = 0;

					tpx = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());
					tpy = Integer.parseInt(n.getAttributes().getNamedItem("y").getNodeValue());
					tpz = Integer.parseInt(n.getAttributes().getNamedItem("z").getNodeValue());
					
					setReturnTeleport(tpx, tpy, tpz);
			}
			else if ("doorList".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					int doorId = 0;
					boolean doorState = false;
					if ("door".equalsIgnoreCase(d.getNodeName()))
					{
						doorId = Integer.parseInt(d.getAttributes().getNamedItem("doorId").getNodeValue());
						if (d.getAttributes().getNamedItem("open") != null)
							doorState = Boolean.parseBoolean(d.getAttributes().getNamedItem("open").getNodeValue());
						addDoor(doorId, doorState);
					}
				}
			}
			else if ("spawnList".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					int npcId = 0, x = 0, y = 0, z = 0, respawn = 0, heading = 0, amount = 1;

					if ("spawn".equalsIgnoreCase(d.getNodeName()))
					{
						npcId = Integer.parseInt(d.getAttributes().getNamedItem("npcId").getNodeValue());
						x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
						y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
						z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
						heading = Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue());
						// these have default values in schema, but DF doesn't seem to care
						Node opt = d.getAttributes().getNamedItem("respawnDelay");
						if (opt != null)
							respawn = Integer.parseInt(opt.getNodeValue());
						opt = d.getAttributes().getNamedItem("amount");
						if (opt != null)
							amount = Integer.parseInt(opt.getNodeValue());

						npcTemplate = NpcTable.getInstance().getTemplate(npcId);
						if (npcTemplate != null)
						{
							spawnDat = new L2Spawn(npcTemplate);
							spawnDat.setLocx(x);
							spawnDat.setLocy(y);
							spawnDat.setLocz(z);
							spawnDat.setAmount(amount);
							spawnDat.setHeading(heading);
							spawnDat.setRespawnDelay(respawn);
							if (respawn == 0)
								spawnDat.stopRespawn();
							else
								spawnDat.startRespawn();
							spawnDat.setInstanceId(getId());
							spawnDat.doSpawn();
						}
						else
						{
							_log.warn("Instance: Data missing in NPC table for ID: " + npcTemplate + " in Instance " + getId());
						}
					}
				}
			}
			else if ("spawnPoint".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					_spawnLoc = new int[3];
					_spawnLoc[0] = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());
					_spawnLoc[1] = Integer.parseInt(n.getAttributes().getNamedItem("y").getNodeValue());
					_spawnLoc[2] = Integer.parseInt(n.getAttributes().getNamedItem("z").getNodeValue());
				}
				catch (Exception e)
				{
					_log.warn("Error parsing instance xml: ", e);
				}
			}
		}
		if (_log.isDebugEnabled())
			_log.info(name + " Instance Template for Instance " + getId() + " loaded");
	}

	protected void doCheckTimeUp(int remaining)
	{
		CreatureSay cs = null;
		int timeLeft;
		int interval;

		if (_players.isEmpty() && _emptyDestroyTime == 0)
		{
			remaining = 0;
			interval = 500;
		}
		else if (_players.isEmpty() && _emptyDestroyTime > 0)
		{
			Long emptyTimeLeft = _lastLeft + _emptyDestroyTime - System.currentTimeMillis();
			if (emptyTimeLeft <= 0)
			{
				interval = 0;
				remaining = 0;
			}
			else if (remaining > 300000 && emptyTimeLeft > 300000)
			{
				interval = 300000;
				remaining = remaining - 300000;
			}
			else if (remaining > 60000 && emptyTimeLeft > 60000)
			{
				interval = 60000;
				remaining = remaining - 60000;
			}
			else if (remaining > 30000 && emptyTimeLeft > 30000)
			{
				interval = 30000;
				remaining = remaining - 30000;
			}
			else
			{
				interval = 10000;
				remaining = remaining - 10000;
			}
		}
		else if (remaining > 300000)
		{
			timeLeft = remaining / 60000;
			interval = 300000;
			SystemMessage sm = new SystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
			sm.addString(Integer.toString(timeLeft));
			Announcements.getInstance().announceToInstance(sm, getId());
			remaining = remaining - 300000;
		}
		else if (remaining > 60000)
		{
			timeLeft = remaining / 60000;
			interval = 60000;
			SystemMessage sm = new SystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
			sm.addString(Integer.toString(timeLeft));
			Announcements.getInstance().announceToInstance(sm, getId());
			remaining = remaining - 60000;
		}
		else if (remaining > 30000)
		{
			timeLeft = remaining / 1000;
			interval = 30000;
			cs = new CreatureSay(0, SystemChatChannelId.Chat_Alliance, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 30000;
		}
		else
		{
			timeLeft = remaining / 1000;
			interval = 10000;
			cs = new CreatureSay(0, SystemChatChannelId.Chat_Alliance, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 10000;
		}
		if (cs != null)
		{
			for (int objectId : _players)
			{
				L2PcInstance player = L2World.getInstance().findPlayer(objectId);
				if (player != null && player.isSameInstance(getId()))
				{
					player.sendPacket(cs);
				}
			}
		}
		if (_checkTimeUpTask != null)
			_checkTimeUpTask.cancel(true);
		if (remaining >= 10000)
			_checkTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(remaining), interval);
		else
			_checkTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), interval);
	}

	private class CheckTimeUp implements Runnable
	{
		private final int	_remaining;

		public CheckTimeUp(int remaining)
		{
			_remaining = remaining;
		}

		public void run()
		{
			doCheckTimeUp(_remaining);
		}
	}

	private class TimeUp implements Runnable
	{
		public void run()
		{
			InstanceManager.getInstance().destroyInstance(getId());
		}
	}

	public void cancelTimer()
	{
		if (_checkTimeUpTask != null)
			_checkTimeUpTask.cancel(false);
	}
}
