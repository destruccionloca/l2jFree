package com.l2jfree.gameserver.model.entity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import javolution.util.FastSet;

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
import com.l2jfree.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.mapregion.TeleportWhereType;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.CreatureSay;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.L2NpcTemplate;

/** 
 * @author evill33t
 * 
 */
public class Instance
{
	private final static Log			_log				= LogFactory.getLog(Instance.class.getName());

	private int							_id;
	private String						_name;
	private FastSet<Integer>			_players			= new FastSet<Integer>();
	private FastList<L2NpcInstance>		_npcs				= new FastList<L2NpcInstance>();
	private FastList<L2DoorInstance>	_doors				= new FastList<L2DoorInstance>();

	protected ScheduledFuture<?>		_CheckTimeUpTask	= null;

	public Instance(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public boolean containsPlayer(int objectId)
	{
		if (_players.contains(objectId))
			return true;
		return false;
	}

	public void addPlayer(int objectId)
	{
		if (!_players.contains(objectId))
			_players.add(objectId);
	}

	public void removePlayer(int objectId)
	{
		L2PcInstance player = L2World.getInstance().findPlayer(objectId);
		if (player != null && player.getInstanceId() == this.getId())
		{
			player.setInstanceId(0);
			player.sendMessage("You were removed from the instance");
			player.teleToLocation(TeleportWhereType.Town);
		}
		_players.remove(objectId);
	}

	public void removeNpc(L2Spawn spawn)
	{
		_npcs.remove(spawn);
	}

	public void removeDoor(L2DoorInstance door)
	{
		_doors.remove(door);
	}

	public FastSet<Integer> getPlayers()
	{
		return _players;
	}

	public FastList<L2NpcInstance> getNpcs()
	{
		return _npcs;
	}

	public FastList<L2DoorInstance> getDoors()
	{
		return _doors;
	}

	public void removePlayers()
	{
		for (int objectId : _players)
		{
			removePlayer(objectId);
		}
		_players.clear();
	}

	public void removeNpcs()
	{
		for (L2NpcInstance mob : _npcs)
		{
			if (mob != null)
			{
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
		}
		_doors.clear();
		_npcs.clear();
	}

	public void loadInstanceTemplate(String filename) throws FileNotFoundException
	{
		Document doc = null;
		File xml = new File(Config.DATAPACK_ROOT, "data/instances/" + filename);

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
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
		String name = null;
		name = n.getAttributes().getNamedItem("name").getNodeValue();
		setName(name);

		Node a;
		Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("activityTime".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if (a != null)
					_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(Integer.parseInt(a.getNodeValue()) * 60000), 15000);
			}
			/*			else if ("timeDelay".equalsIgnoreCase(n.getNodeName()))
						{
							a = n.getAttributes().getNamedItem("val");
							if (a != null)
								instance.setTimeDelay(Integer.parseInt(a.getNodeValue()));
						}*/
			else if ("doorlist".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					int doorId = 0;
					if ("door".equalsIgnoreCase(d.getNodeName()))
					{
						doorId = Integer.parseInt(d.getAttributes().getNamedItem("doorId").getNodeValue());
						L2DoorInstance temp = DoorTable.getInstance().getDoor(doorId);
						L2DoorInstance newdoor = new L2DoorInstance(IdFactory.getInstance().getNextId(), temp.getTemplate(), temp.getDoorId(), temp.getName(),
								temp.isUnlockable());
						newdoor.setInstanceId(getId());
						newdoor.setRange(temp.getXMin(), temp.getYMin(), temp.getZMin(), temp.getXMax(), temp.getYMax(), temp.getZMax());
						try
						{
							newdoor.setMapRegion(MapRegionManager.getInstance().getRegion(temp.getX(), temp.getY(), temp.getZ()));
						}
						catch (Exception e)
						{
							_log.fatal("Error in door data, ID:" + temp.getDoorId());
						}
						newdoor.getStatus().setCurrentHpMp(newdoor.getMaxHp(), newdoor.getMaxMp());
						newdoor.setOpen(1);
						newdoor.getPosition().setXYZInvisible(temp.getX(), temp.getY(), temp.getZ());
						newdoor.spawnMe(newdoor.getX(), newdoor.getY(), newdoor.getZ());

						_doors.add(newdoor);
					}
				}
			}
			else if ("spawnlist".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					int npcId = 0, x = 0, y = 0, z = 0, respawn = 0, heading = 0;

					if ("spawn".equalsIgnoreCase(d.getNodeName()))
					{

						npcId = Integer.parseInt(d.getAttributes().getNamedItem("npcId").getNodeValue());
						x = Integer.parseInt(d.getAttributes().getNamedItem("x").getNodeValue());
						y = Integer.parseInt(d.getAttributes().getNamedItem("y").getNodeValue());
						z = Integer.parseInt(d.getAttributes().getNamedItem("z").getNodeValue());
						heading = Integer.parseInt(d.getAttributes().getNamedItem("heading").getNodeValue());
						respawn = Integer.parseInt(d.getAttributes().getNamedItem("respawn").getNodeValue());

						npcTemplate = NpcTable.getInstance().getTemplate(npcId);
						if (npcTemplate != null)
						{
							spawnDat = new L2Spawn(npcTemplate);
							spawnDat.setLocx(x);
							spawnDat.setLocy(y);
							spawnDat.setLocz(z);
							spawnDat.setAmount(1);
							spawnDat.setHeading(heading);
							spawnDat.setRespawnDelay(respawn);
							if (respawn == 0)
								spawnDat.stopRespawn();
							spawnDat.setInstanceId(getId());
							L2NpcInstance newmob = spawnDat.doSpawn();
							_npcs.add(newmob);
						}
						else
						{
							_log.warn("Instance: Data missing in NPC table for ID: " + npcTemplate + " in Instance " + getId());
						}
					}
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

		if (remaining > 300000)
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
			cs = new CreatureSay(0, 9, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 30000;
		}
		else
		{
			timeLeft = remaining / 1000;
			interval = 10000;
			cs = new CreatureSay(0, 9, "Notice", timeLeft + " seconds left.");
			remaining = remaining - 10000;
		}
		if (cs != null)
		{
			for (int objectId : _players)
			{
				L2PcInstance player = L2World.getInstance().findPlayer(objectId);
				if (player != null && player.getInstanceId() == getId())
				{
					player.sendPacket(cs);
				}
			}
		}
		if (_CheckTimeUpTask != null)
			_CheckTimeUpTask.cancel(true);
		if (remaining >= 10000)
			_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(remaining), interval);
		else
			_CheckTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), interval);
	}

	private class CheckTimeUp implements Runnable
	{
		private int	_remaining;

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
}