/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZonePoly;
import net.sf.l2j.gameserver.model.zone.ZoneRect;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.templates.StatsSet;
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

	private int _count;
	
	public static final ZoneManager getInstance()
	{
		if(_instance == null)
		{
			_log.info("Initializing ZoneManager");
			_instance = new ZoneManager();
			_instance.load();
		}
		return _instance;
	}

	public ZoneManager()
	{}

	public boolean checkIfInZone(ZoneType zoneType, L2Character characer)
	{
		return characer.isInsideZone(zoneType);
	}

	public void reload()
	{
		//TODO:
		_log.fatal("ZoneManager: reload not done !!!");
	}

	private void load()
	{
		Document doc = null;

		_count = 0;
		
		for(File f : Util.getDatapackFiles("zone", ".xml"))
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
				parseDocument(doc);
			}
			catch (Exception e)
			{
				_log.fatal("ZoneManager: Error in file " + f.getAbsolutePath(), e);
			}
		}
		_log.info("ZoneManager: Loaded "+_count+" zones.");
	}

	protected void parseDocument(Document doc)
	{
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if("list".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("zone".equalsIgnoreCase(d.getNodeName()))
					{
						IZone zone = parseZone(d);
						if(zone != null)
						{
							addZone(zone);
							_count++;
						}
					}
				}
			}
			else if("zone".equalsIgnoreCase(n.getNodeName()))
			{
				IZone zone = parseZone(n);
				if(zone != null)
				{
					addZone(zone);
					_count++;
				}
			}
		}
	}

	public static IZone parseZone(Node n)
	{
		int id = 0;
		try
		{
			id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		}
		catch (Exception e)
		{

		}

		String zoneName = "";

		try
		{
			zoneName = n.getAttributes().getNamedItem("name").getNodeValue();
		}
		catch (Exception e)
		{

		}

		String typeName = "Default";
		StatsSet zoneSet = null;

		int zMin = 0, zMax = 0;

		List<Point3D> points = new FastList<Point3D>();

		Node first = n.getFirstChild();
		for(n = first; n != null; n = n.getNextSibling())
		{
			if("form".equalsIgnoreCase(n.getNodeName()))
			{
				zMin = Integer.parseInt(n.getAttributes().getNamedItem("zMin").getNodeValue());
				zMax = Integer.parseInt(n.getAttributes().getNamedItem("zMax").getNodeValue());

				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("point".equalsIgnoreCase(d.getNodeName()))
					{
						points.add(parsePoint(d));
					}
				}
			}
			else if("settings".equalsIgnoreCase(n.getNodeName()))
			{
				zoneSet = parseSettings(n);
			}
			else if("type".equalsIgnoreCase(n.getNodeName()))
			{
				typeName = n.getTextContent();
			}
		}

		IZone zone;
		ZoneType zoneType = ZoneType.getZoneTypeEnum(typeName);

		if(zoneType == null)
		{
			_log.error("ZoneManager: Unknown zone type '" + typeName + "' !");
			return null;
		}

		if(points.size() > 2)
		{
			zone = new ZonePoly(id, zoneName, zoneType, zoneSet);
			points.add(points.get(0));

		}
		else if(points.size() == 2)
		{
			zone = new ZoneRect(id, zoneName, zoneType, zoneSet);
		}
		else
			return null;

		zone.setZ(zMin, zMax);

		for(Point3D point : points)
			zone.addPoint(point.getX(), point.getY());

		return zone;
	}


	public static Point3D parsePoint(Node n)
	{
		int x = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());
		int y = Integer.parseInt(n.getAttributes().getNamedItem("y").getNodeValue());
		int z = 0;
		if(n.getAttributes().getNamedItem("z") != null)
			z = Integer.parseInt(n.getAttributes().getNamedItem("z").getNodeValue());

		return new Point3D(x, y, z);
	}

	public static StatsSet parseSettings(Node n)
	{
		StatsSet _settings = new StatsSet();

		Node first = n.getFirstChild();
		for(n = first; n != null; n = n.getNextSibling())
		{
			if("set".equalsIgnoreCase(n.getNodeName()))
				_settings.set(n.getAttributes().getNamedItem("name").getNodeValue(), n.getAttributes().getNamedItem("val").getNodeValue());
		}
		return _settings;
	}

	public static void addZone(IZone zone)
	{
		if((zone.getZoneType() == ZoneType.Water && !Config.ALLOW_WATER) || (zone.getZoneType() == ZoneType.Fishing && !Config.ALLOW_FISHING))
			return;

		L2WorldRegion region = null;
		L2WorldRegion region_new = null;

		for(int x = zone.getMin().getX(); x <= zone.getMax().getX(); x += (1 << (L2World.SHIFT_BY - 1)))
		{
			for(int y = zone.getMin().getY(); y <= zone.getMax().getY(); y += (1 << (L2World.SHIFT_BY - 1)))
			{
				region_new = L2World.getInstance().getRegion(x, y);
				if(region != region_new)
				{
					region = region_new;
					region.addZone(zone);
					if(_log.isDebugEnabled())
						_log.info("ZoneManager: adding zone " + region.getName() + " : " + zone.getId() + " " + zone.getZoneName());
				}
			}
		}

	}

}