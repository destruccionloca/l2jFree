/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.util.List;
import java.util.Map;

import java.lang.reflect.Constructor;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.entity.BaseEntity;
import net.sf.l2j.gameserver.model.entity.DefaultEntity;
import net.sf.l2j.gameserver.model.zone.form.ZoneCuboid;
import net.sf.l2j.gameserver.model.zone.form.ZoneCylinder;
import net.sf.l2j.gameserver.model.zone.form.ZoneNPoly;
import net.sf.l2j.gameserver.model.zone.L2Zone;
import net.sf.l2j.gameserver.model.zone.L2ZoneForm;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneSettings;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.tools.geometry.Point3D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ZoneManager
{
	protected static Log _log = LogFactory.getLog(ZoneManager.class.getName());

	private static ZoneManager _instance;

	public static final ZoneManager getInstance()
	{
		if (_instance == null)
		{
			_log.info("Initializing ZoneManager");
			_instance = new ZoneManager();
			_instance.load();
		}
		return _instance;
	}

	private void load()
	{
		Document doc = null;
		int zoneCount = 0;
		
		for (File f : Util.getDatapackFiles("zone", ".xml"))
		{
			try
			{
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setIgnoringComments(true);
				doc = factory.newDocumentBuilder().parse(f);
			}
			catch (Exception e)
			{
				_log.fatal("ZoneManager: Error loading file " + f.getAbsolutePath(), e);
			}
			try
			{
				zoneCount += parseDocument(doc);
			}
			catch (Exception e)
			{
				_log.fatal("ZoneManager: Error in file " + f.getAbsolutePath(), e);
			}
		}

		_log.info("ZoneManager: Loaded "+zoneCount+" region(s).");
	}

	protected int parseDocument(Document doc)
	{
		BaseEntity ent;
		int zoneCount = 0;
		
		// Get the world regions
		L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						ent = parseEntry(d);
						
						for(L2Zone temp : ent.getZones())
						{
							// Register the zone into any world region it intersects with...
							// currently 11136 test for each zone :>
							int ax,ay,bx,by;
							for (int x=0; x < worldRegions.length; x++)
							{
								for (int y=0; y < worldRegions[x].length; y++)
								{
									ax = (x-L2World.OFFSET_X) << L2World.SHIFT_BY;
									bx = ((x+1)-L2World.OFFSET_X) << L2World.SHIFT_BY;
									ay = (y-L2World.OFFSET_Y) << L2World.SHIFT_BY;
									by	= ((y+1)-L2World.OFFSET_Y) << L2World.SHIFT_BY;
									
									if (temp.getForm().intersectsRectangle(ax, bx, ay, by))
									{
										worldRegions[x][y].addZone(temp);
										zoneCount++;
									}
								}
							}
						}
					}
				}
			}
		}
		return zoneCount;
	}

	protected L2ZoneForm parseForm(Node s)
	{
		int rad = 0;
		int zMin = 0;
		int zMax = 0;
		
		List<Point3D> points = new FastList<Point3D>();

		Node first = s.getFirstChild();
		for (Node n = first; n != null; n = n.getNextSibling())
		{
			if ("point".equalsIgnoreCase(n.getNodeName()))
			{
				points.add(parsePoint(n));
			}
		}
		
		if(points.size() == 1) // Cylinder
		{
			rad = Integer.parseInt(s.getAttributes().getNamedItem("radius").getNodeValue());
		}
		zMin  = Integer.parseInt(s.getAttributes().getNamedItem("zMin").getNodeValue());
		zMax  = Integer.parseInt(s.getAttributes().getNamedItem("zMax").getNodeValue());

		L2ZoneForm form = null;
		
		switch(points.size())
		{
			// CYLINDER
			case 1:
				int x = points.get(0).getX();
				int y = points.get(0).getY();
				form = new ZoneCylinder(x, y, zMin, zMax, rad);
			// CUBE
			case 2:
				int xMin = points.get(0).getX();
				int xMax = points.get(1).getX();
				int yMin = points.get(0).getY();
				int yMax = points.get(1).getY();
				form = new ZoneCuboid(xMin, xMax, yMin, yMax, zMin, zMax);
				break;
			
			// N-POLY
			default:
				if(points.size() < 3) break;
			
				form = new ZoneNPoly(zMin, zMax);
				for(Point3D point : points)
					((ZoneNPoly)form).addPoint(point);
				
				// Make a point array and remove the fastlist from memory
				((ZoneNPoly)form).finish();
				break;
		}
		return form;
	}
	
	protected BaseEntity parseEntry(Node n)
	{
		int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		String name = n.getAttributes().getNamedItem("name").getNodeValue();
		int castleId = 0;
		int townId = 0;
		

		FastMap<RestartType, Point3D> restarts = new FastMap<RestartType, Point3D>();
		FastList<L2Zone> zones = new FastList<L2Zone>();

		Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("restart".equalsIgnoreCase(n.getNodeName()))
			{
				parseRestartPoint(n, restarts);
			}
			else if ("zone".equalsIgnoreCase(n.getNodeName()))
			{
				L2Zone zone = parseZone(n);
				if(zone != null)
				{
					zones.add(zone);
				}
			}
		}


		
		BaseEntity zoneEntity = new DefaultEntity();

		// Assigning the restart points to it
		for (Map.Entry<RestartType, Point3D> restart : restarts.entrySet())
			zoneEntity.addRestartPoint(restart.getKey(), restart.getValue());

		for (L2Zone zone : zones)
		{
			zone.setEntity(zoneEntity);
			zoneEntity.getZones().add(zone);
		}
		
		return zoneEntity;
	}

	protected L2Zone parseZone(Node n)
	{
		String typeName = "";
		L2ZoneForm form = null;
		ZoneSettings settings = null;
		Node first = n.getFirstChild();
		for (n = first; n != null; n = n.getNextSibling())
		{
			if ("settings".equalsIgnoreCase(n.getNodeName()))
			{
				settings = ZoneSettings.parse(n);
			}
			else if ("type".equalsIgnoreCase(n.getNodeName()))
			{
				typeName = n.getTextContent();
			}
			else if ("form".equalsIgnoreCase(n.getNodeName()))
			{
				form = parseForm(n);
			}
		}
		
		ZoneType zoneType = ZoneType.getZoneTypeEnum(typeName);
		
		L2Zone zone = getZoneByType(zoneType);

		if (zoneType == null)
		{
			_log.error("ZoneManager: Unknown zone type '" + typeName);
			return null;
		}
		if (form == null)
		{
			_log.error("ZoneManager: Can't build shape for zone item");
			return null;
		}

		if ((zoneType == ZoneType.Water && !Config.ALLOW_WATER) || (zoneType == ZoneType.Fishing && !Config.ALLOW_FISHING))
			return null;

		if(zone == null) return null;
		
		// Assign the zone shape to it
		zone.setForm(form);
		// Assign the zone settings
		zone.setSettings(settings);
		
		return zone;
	}

	protected Point3D parsePoint(Node n)
	{
		int x = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());
		int y = Integer.parseInt(n.getAttributes().getNamedItem("y").getNodeValue());
		int z = 0;
		if (n.getAttributes().getNamedItem("z") != null)
			z = Integer.parseInt(n.getAttributes().getNamedItem("z").getNodeValue());

		return new Point3D(x, y, z);
	}

	protected void parseRestartPoint(Node r, FastMap<RestartType, Point3D> map)
	{
		RestartType rt;
		String type = r.getAttributes().getNamedItem("type").getNodeValue();
		if("normal".equalsIgnoreCase(type))
			rt = RestartType.RestartNormal;
		else if("chaotic".equalsIgnoreCase(type))
			rt = RestartType.RestartChaotic;
		else if("owner".equalsIgnoreCase(type))
			rt = RestartType.RestartOwner;
		else
		{
			_log.warn("Invalid restart type: "+type);
			return;
		}
	
		for (Node point = r.getFirstChild(); point != null; point = point.getNextSibling())
		{
			if("point".equalsIgnoreCase(point.getNodeName()))
				map.put(rt, parsePoint(point));
		}
	}

	public static short getMapRegion(int x, int y)
	{
		int rx = ((x - L2World.MAP_MIN_X) >> 15) + 16;
		int ry = ((y - L2World.MAP_MIN_Y) >> 15) + 10;
		return (short) ((rx << 8) + ry);
	}

	private L2Zone getZoneByType(ZoneType type)
	{
		try
		{
			Constructor<? extends L2Zone> c = type.getZoneClass().getConstructor();
			return c.newInstance();
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
