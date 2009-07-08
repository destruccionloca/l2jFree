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
package com.l2jfree.gameserver.instancemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.Location;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.ClanHall;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.model.entity.FortSiege;
import com.l2jfree.gameserver.model.entity.Instance;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.model.entity.Town;
import com.l2jfree.gameserver.model.mapregion.L2MapArea;
import com.l2jfree.gameserver.model.mapregion.L2MapRegion;
import com.l2jfree.gameserver.model.mapregion.L2MapRegionRestart;
import com.l2jfree.gameserver.model.mapregion.L2SpecialMapRegion;
import com.l2jfree.gameserver.model.mapregion.TeleportWhereType;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.util.Util;

/**
 * @author Noctarius
 */
public final class MapRegionManager
{
	private static final Log _log = LogFactory.getLog(MapRegionManager.class);
	
	private final Map<Integer, L2MapRegionRestart> _mapRegionRestart = new FastMap<Integer, L2MapRegionRestart>();
	
	private L2SpecialMapRegion[] _specialMapRegions = new L2SpecialMapRegion[0];
	private L2MapArea[] _mapAreas = new L2MapArea[0];
	
	public static MapRegionManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private MapRegionManager()
	{
		load();
	}
	
	private void load()
	{
		for (File xml : Util.getDatapackFiles("mapregion", ".xml"))
		{
			Document doc = null;
			
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				
				doc = factory.newDocumentBuilder().parse(xml);
			}
			catch (Exception e)
			{
				_log.warn("MapRegionManager: Error while loading XML definition: " + xml.getName() + e, e);
				return;
			}
			
			try
			{
				parseDocument(doc);
			}
			catch (Exception e)
			{
				_log.warn("MapRegionManager: Error in XML definition: " + xml.getName() + e, e);
				return;
			}
		}
	}
	
	public void reload()
	{
		load();
	}
	
	private void parseDocument(Document doc) throws Exception
	{
		final Map<Integer, L2MapRegionRestart> restarts = new FastMap<Integer, L2MapRegionRestart>();
		
		final List<L2MapRegion> specialMapRegions = new ArrayList<L2MapRegion>();
		final List<L2MapArea> mapAreas = new ArrayList<L2MapArea>();
		
		final Set<Integer> restartAreas = new HashSet<Integer>();
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("mapregion".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("regions".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node f = d.getFirstChild(); f != null; f = f.getNextSibling())
						{
							if ("region".equalsIgnoreCase(f.getNodeName()))
							{
								specialMapRegions.add(new L2SpecialMapRegion(f));
							}
						}
					}
					else if ("restartpoints".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node f = d.getFirstChild(); f != null; f = f.getNextSibling())
						{
							if ("restartpoint".equalsIgnoreCase(f.getNodeName()))
							{
								L2MapRegionRestart restart = new L2MapRegionRestart(f);
								
								if (!restarts.containsKey(restart.getRestartId()))
									restarts.put(restart.getRestartId(), restart);
								else
									throw new Exception("Duplicate restartpointId: " + restart.getRestartId() + ".");
							}
						}
					}
					else if ("restartareas".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node f = d.getFirstChild(); f != null; f = f.getNextSibling())
						{
							if ("restartarea".equalsIgnoreCase(f.getNodeName()))
							{
								final int restartId = Integer.parseInt(f.getAttributes().getNamedItem("restartId").getNodeValue());
								
								restartAreas.add(restartId);
								
								for (Node r = f.getFirstChild(); r != null; r = r.getNextSibling())
								{
									if ("map".equalsIgnoreCase(r.getNodeName()))
									{
										int X = Integer.parseInt(r.getAttributes().getNamedItem("X").getNodeValue());
										int Y = Integer.parseInt(r.getAttributes().getNamedItem("Y").getNodeValue());
										
										mapAreas.add(new L2MapArea(restartId, X, Y));
									}
								}
							}
						}
					}
				}
			}
		}
		
		_specialMapRegions = specialMapRegions.toArray(new L2SpecialMapRegion[specialMapRegions.size()]);
		_mapAreas = mapAreas.toArray(new L2MapArea[mapAreas.size()]);
		
		_mapRegionRestart.clear();
		_mapRegionRestart.putAll(restarts);
		
		int redirectCount = 0;
		
		for (L2MapRegionRestart restart : _mapRegionRestart.values())
		{
			if (restart.getBannedRace() != null)
				redirectCount++;
		}
		
		_log.info("MapRegionManager: Loaded " + _mapRegionRestart.size() + " restartpoint(s).");
		_log.info("MapRegionManager: Loaded " + restartAreas.size() + " restartareas with " + _mapAreas.length + " arearegion(s).");
		_log.info("MapRegionManager: Loaded " + _specialMapRegions.length + " zoneregion(s).");
		_log.info("MapRegionManager: Loaded " + redirectCount + " race depending redirects.");
	}
	
	public L2MapRegionRestart getRestartLocation(L2PcInstance activeChar)
	{
		L2MapRegion region = getRegion(activeChar);
		
		// Temporary fix for new hunting grounds
		if (region == null)
			return _mapRegionRestart.get(Config.ALT_DEFAULT_RESTARTTOWN);
		else
			return _mapRegionRestart.get(region.getRestartId(activeChar));
	}
	
	public L2MapRegionRestart getRestartLocation(int restartId)
	{
		return _mapRegionRestart.get(restartId);
	}
	
	public Location getRestartPoint(int restartId, L2PcInstance activeChar)
	{
		L2MapRegionRestart restart = _mapRegionRestart.get(restartId);
		
		if (restart == null)
			restart = _mapRegionRestart.get(Config.ALT_DEFAULT_RESTARTTOWN);
		
		return restart.getRandomRestartPoint(activeChar);
	}
	
	public Location getChaoticRestartPoint(int restartId, L2PcInstance activeChar)
	{
		L2MapRegionRestart restart = _mapRegionRestart.get(restartId);
		
		if (restart == null)
			restart = _mapRegionRestart.get(Config.ALT_DEFAULT_RESTARTTOWN);
		
		return restart.getRandomChaoticRestartPoint(activeChar);
	}
	
	public L2MapRegion getRegion(L2Character activeChar)
	{
		return getRegion(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
	
	public L2MapRegion getRegion(int x, int y, int z)
	{
		for (L2SpecialMapRegion region : _specialMapRegions)
			if (region.checkIfInRegion(x, y, z))
				return region;
		
		for (L2MapArea region : _mapAreas)
			if (region.checkIfInRegion(x, y, z))
				return region;
		
		return null;
	}
	
	public L2MapRegion getRegion(int x, int y)
	{
		return getRegion(x, y, -1);
	}
	
	//TODO: Needs to be clean rewritten
	public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)activeChar;
			L2Clan clan = player.getClan();
			Castle castle = null;
			Fort fort = null;
			ClanHall clanhall = null;
			
			if (player.isFlyingMounted()) // prevent flying players to teleport outside of gracia
				return new Location(-186330, 242944, 2544);
			
			// Checking if in Dimensinal Gap
			if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getX(), player.getY(), player.getZ(), true)) // true -> ignore waiting room :)
			{
				if (player.isInParty() && player.getParty().isInDimensionalRift())
				{
					player.getParty().getDimensionalRift().usedTeleport(player);
				}
				
				return DimensionalRiftManager.getInstance().getWaitingRoomTeleport();
			}
			
			// Checking if in an instance
			if (player.getInstanceId() > 0)
			{
				Instance playerInstance = InstanceManager.getInstance().getInstance(player.getInstanceId());
				if (playerInstance != null)
				{
					if (playerInstance.getSpawnLoc() != null)
					{
						int[] coord = playerInstance.getSpawnLoc();
						return new Location(coord[0], coord[1], coord[2]);
					}
				}
			}
			
			// Checking if in arena
			L2Zone arena = ZoneManager.getInstance().isInsideZone(L2Zone.ZoneType.Arena, player.getX(), player.getY());
			if (arena != null && arena.isInsideZone(player))
			{
				Location loc = arena.getRestartPoint(L2Zone.RestartType.OWNER);
				if (loc == null)
					loc = arena.getRandomLocation();
				return loc;
			}
			
			if (teleportWhere == TeleportWhereType.Town)
			{
				L2MapRegionRestart restart = getRestartLocation(player);
				
				Location loc = null;
				
				// Karma player land out of city
				if (player.isChaotic())
					loc = restart.getRandomChaoticRestartPoint(player);
				
				if (loc == null)
					loc = restart.getRandomRestartPoint(player);
				
				return loc;
			}
			
			if (clan != null)
			{
				// If teleport to clan hall
				if (teleportWhere == TeleportWhereType.ClanHall)
				{
					clanhall = ClanHallManager.getInstance().getClanHallByOwner(clan);
					if (clanhall != null)
					{
						L2Zone zone = clanhall.getZone();
						
						if (zone != null)
						{
							Location loc = zone.getRestartPoint(L2Zone.RestartType.OWNER);
							if (loc == null)
								loc = zone.getRandomLocation();
							return loc;
						}
					}
				}
				
				// If teleport to castle
				if (teleportWhere == TeleportWhereType.Castle)
					castle = CastleManager.getInstance().getCastleByOwner(clan);
				
				else if (teleportWhere == TeleportWhereType.Fortress)
					fort = FortManager.getInstance().getFortByOwner(clan);
				
				// If Teleporting to castle or
				if (castle != null && teleportWhere == TeleportWhereType.Castle)
				{
					L2Zone zone = castle.getZone();
					if (zone != null)
					{
						if (castle.getSiege().getIsInProgress() && player.isChaotic())
						{
							// Karma player respawns out of siege zone (only during sieges ? o.O )
							return zone.getRestartPoint(L2Zone.RestartType.CHAOTIC);
						}
						
						return zone.getRestartPoint(L2Zone.RestartType.OWNER);
					}
				}
				else if (fort != null && teleportWhere == TeleportWhereType.Fortress)
				{
					L2Zone zone = fort.getZone();
					if (zone != null)
					{
						// If is on castle with siege and player's clan is defender
						if (fort.getSiege().getIsInProgress() && player.isChaotic())
						{
							// Karma player respawns out of siege zone (only during sieges ? o.O )
							return zone.getRestartPoint(L2Zone.RestartType.CHAOTIC);
						}
						
						return zone.getRestartPoint(L2Zone.RestartType.OWNER);
					}
				}
				else if (teleportWhere == TeleportWhereType.SiegeFlag)
				{
					Siege siege = SiegeManager.getInstance().getSiege(clan);
					FortSiege fsiege = FortSiegeManager.getInstance().getSiege(clan);
					
					// Check if player's clan is attacker
					if (siege != null && fsiege == null && siege.checkIsAttacker(clan) && siege.checkIfInZone(player))
					{
						// Karma player respawns out of siege zone
						if (player.isChaotic())
						{
							L2Zone zone = siege.getCastle().getZone();
							if (zone != null)
							{
								return zone.getRestartPoint(L2Zone.RestartType.CHAOTIC);
							}
						}
						// get nearest flag
						L2Npc flag = siege.getClosestFlag(player);
						// spawn to flag
						if (flag != null)
							return new Location(flag.getX(), flag.getY(), flag.getZ());
					}
					else if (siege == null && fsiege != null && fsiege.checkIsAttacker(clan) && fsiege.checkIfInZone(player))
					{
						// Karma player respawns out of siege zone
						if (player.isChaotic())
						{
							L2Zone zone = fsiege.getFort().getZone();
							if (zone != null)
							{
								return zone.getRestartPoint(L2Zone.RestartType.CHAOTIC);
							}
						}
						// Get nearest flag
						L2Npc flag = fsiege.getClosestFlag(player);
						// Spawn to flag
						if (flag != null)
							return new Location(flag.getX(), flag.getY(), flag.getZ());
					}
				}
			}
		}
		
		// teleport to default town if nothing else will work
		return getRestartPoint(Config.ALT_DEFAULT_RESTARTTOWN, activeChar.getActingPlayer());
	}
	
	public int getAreaCastle(L2Character activeChar)
	{
		Town town = TownManager.getInstance().getClosestTown(activeChar);
		
		if (town == null)
			return 5;
		
		return town.getCastleId();
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final MapRegionManager _instance = new MapRegionManager();
	}
}
