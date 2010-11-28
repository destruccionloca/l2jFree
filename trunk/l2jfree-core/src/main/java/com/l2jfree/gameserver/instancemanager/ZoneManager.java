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
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.L2WorldRegion;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.util.Util;

public final class ZoneManager
{
	private static final Log _log = LogFactory.getLog(ZoneManager.class);
	
	public static ZoneManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private L2Zone[] _zones = new L2Zone[0];
	private final FastMap<Integer, L2Zone> _uniqueZones = new FastMap<Integer, L2Zone>();
	
	private ZoneManager()
	{
		load();
	}
	
	public void reload()
	{
		// Get the world regions
		L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		for (L2WorldRegion[] finalWorldRegion : worldRegions)
		{
			for (L2WorldRegion finalElement : finalWorldRegion)
			{
				finalElement.clearZones();
			}
		}
		// Remove registered siege danger zones
		for (Castle c : CastleManager.getInstance().getCastles().values())
			c.getSiege().onZoneReload();
		
		_zones = new L2Zone[0];
		
		// Remove registered unique zones
		_uniqueZones.clear();
		
		// Load the zones
		load();
	}
	
	private void load()
	{
		Document doc = null;
		
		for (File f : Util.getDatapackFiles("zone", ".xml"))
		{
			int count = 0;
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(true);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);
			}
			catch (Exception e)
			{
				_log.fatal("ZoneManager: Error loading file " + f.getAbsolutePath(), e);
				continue;
			}
			try
			{
				count = parseDocument(doc);
			}
			catch (Exception e)
			{
				_log.fatal("ZoneManager: Error in file " + f.getAbsolutePath(), e);
				continue;
			}
			_log.info("ZoneManager: " + f.getName() + " loaded with " + count + " zones");
		}
	}
	
	protected int parseDocument(Document doc)
	{
		List<L2Zone> zones = new ArrayList<L2Zone>();
		
		// Get the world regions
		L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("zone".equalsIgnoreCase(d.getNodeName()))
					{
						final L2Zone zone = L2Zone.parseZone(d);
						if (zone == null)
							continue;
						Integer id = zone.getQuestZoneId();
						if (_uniqueZones.containsKey(id))
						{
							_log.warn("Zone " + zone.getName() + " doesn't specify a valid unique ID, ignored!");
							continue;
						}
						else if (id > 0)
							_uniqueZones.put(id, zone);
						
						zones.add(zone);
						
						// Register the zone to any intersecting world region
						int ax, ay, bx, by;
						for (int x = 0; x < worldRegions.length; x++)
						{
							for (int y = 0; y < worldRegions[x].length; y++)
							{
								ax = (x - L2World.OFFSET_X) << L2World.SHIFT_BY;
								bx = ((x + 1) - L2World.OFFSET_X) << L2World.SHIFT_BY;
								ay = (y - L2World.OFFSET_Y) << L2World.SHIFT_BY;
								by = ((y + 1) - L2World.OFFSET_Y) << L2World.SHIFT_BY;
								
								if (zone.intersectsRectangle(ax, bx, ay, by))
								{
									worldRegions[x][y].addZone(zone);
								}
							}
						}
					}
				}
			}
		}
		
		_zones = (L2Zone[])ArrayUtils.addAll(_zones, zones.toArray(new L2Zone[zones.size()]));
		
		return zones.size();
	}
	
	public L2Zone[] getZones()
	{
		return _zones;
	}
	
	@SuppressWarnings("unchecked")
	public final <T extends L2Zone> T isInsideZone(Class<T> type, int x, int y)
	{
		final L2WorldRegion region = L2World.getInstance().getRegion(x, y);
		
		for (L2Zone zone : region.getZones())
			if (type.isInstance(zone))
				if (zone.isInsideZone(x, y))
					return (T)zone;
		
		return null;
	}
	
	/**
	 * Returns a unique zone which has the specified <U>unique</U> ID.
	 * This is a direct <CODE>get(key)</CODE> operation on a map which
	 * contains only zones which have an unique ID.
	 * @param uniqueId The questZoneId specified declaring the zone
	 * @return A unique zone or null
	 */
	public L2Zone getZoneById(int uniqueId)
	{
		return _uniqueZones.get(uniqueId);
	}
	
	/**
	 * Returns zone from where the object is located by type
	 * 
	 * @param object
	 * @param type
	 * @return zone
	 */
	public <T extends L2Zone> T getZone(L2Object object, Class<T> type)
	{
		return getZone(object.getX(), object.getY(), object.getZ(), type);
	}
	
	/**
	 * Returns zone from given coordinates
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param type
	 * @return zone
	 */
	@SuppressWarnings("unchecked")
	public <T extends L2Zone> T getZone(int x, int y, int z, Class<T> type)
	{
		final L2WorldRegion region = L2World.getInstance().getRegion(x, y);
		
		for (L2Zone zone : region.getZones())
			if (type.isInstance(zone))
				if (zone.isInsideZone(x, y, z))
					return (T)zone;
		
		return null;
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final ZoneManager _instance = new ZoneManager();
	}
}
