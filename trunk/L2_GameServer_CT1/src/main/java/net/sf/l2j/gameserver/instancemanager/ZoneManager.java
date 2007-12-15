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

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.entity.Town;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZonePoly;
import net.sf.l2j.gameserver.model.zone.ZoneRect;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
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
            _instance = new ZoneManager();
            _instance.load();
        }
        return _instance;
    }

    private FastMap<Short, FastMap<ZoneType, FastList<IZone>>> _zoneMap;

    public ZoneManager()
    {
    }

    public boolean checkIfInZoneXY(ZoneType zoneType, L2Object obj)
    {
        return checkIfInZone(zoneType, obj.getX(), obj.getY());
    }

    public boolean checkIfInZone(ZoneType zoneType, L2Object obj)
    {
        return checkIfInZone(zoneType, obj.getX(), obj.getY(), obj.getZ());
    }

    public boolean checkIfInZone(ZoneType zoneType, String zoneName, L2Object obj)
    {
        return checkIfInZone(zoneType, obj.getX(), obj.getY(), obj.getZ());
    }

    public boolean checkIfInZone(ZoneType zoneType, int x, int y)
    {
        for (IZone zone : ZoneManager.getInstance().getZones(zoneType, x, y))
            if (zone.checkIfInZone(x, y))
                return true;
        return false;
    }

    public boolean checkIfInZone(ZoneType zoneType, String zoneName, int x, int y)
    {
        for (IZone zone : ZoneManager.getInstance().getZones(zoneType, x, y))
            if (zone.getZoneName().equalsIgnoreCase(zoneName) && zone.checkIfInZone(x, y))
                return true;
        return false;
    }

    public boolean checkIfInZone(ZoneType zoneType, int x, int y, int z)
    {
        for (IZone zone : ZoneManager.getInstance().getZones(zoneType, x, y))
            if (zone.checkIfInZone(x, y, z))
                return true;
        return false;
    }

    public boolean checkIfInZone(ZoneType zoneType, String zoneName, int x, int y, int z)
    {
        for (IZone zone : ZoneManager.getInstance().getZones(zoneType, x, y))
            if (zone.getZoneName().equalsIgnoreCase(zoneName) && zone.checkIfInZone(x, y, z))
                return true;
        return false;
    }

    public boolean checkIfInZonePeace(L2Object obj)
    {
        return checkIfInZonePeace(obj.getX(), obj.getY(), obj.getZ());
    }

    public boolean checkIfInZonePeace(int x, int y, int z)
    {
        Town town = TownManager.getInstance().getTown(x, y, z);

        if (town != null && checkIfInZone(ZoneType.Peace, x, y, z))
            return town.isInPeace();
        else
            return (checkIfInZone(ZoneType.Peace, x, y, z) || checkIfInZone(ZoneType.Newbie, x, y, z) || (!Config.JAIL_IS_PVP && checkIfInZone(
                    ZoneType.Jail, x, y, z)));
    }

    public boolean checkIfInZonePvP(L2Object obj)
    {
        return checkIfInZonePvP(obj.getX(), obj.getY(), obj.getZ());
    }

    public boolean checkIfInZonePvP(int x, int y, int z)
    {
        Town town = TownManager.getInstance().getTown(x, y, z);

        if (town != null)
            return !town.isInPeace();
        else
            return (checkIfInZone(ZoneType.Arena, x, y, z) || checkIfInZone(ZoneType.OlympiadStadia, x, y, z) || (Config.JAIL_IS_PVP && checkIfInZone(
                    ZoneType.Jail, x, y, z)));
    }

    public final IZone getIfInZone(ZoneType zoneType, int x, int y, int z)
    {
        for (IZone zone : getZones(zoneType, x, y))
            if ((zone.getZoneType() == zoneType) && (zone.checkIfInZone(x, y, z)))
                return zone;
        return null;
    }

    public final IZone getIfInZone(ZoneType zoneType, int x, int y)
    {
        for (IZone zone : getZones(zoneType, x, y))
            if ((zone.getZoneType() == zoneType) && (zone.checkIfInZone(x, y)))
                return zone;
        return null;
    }

    public void reload()
    {
        getZoneMap().clear();
        load();
    }

    private void load()
    {
        Document doc = null;

        for (File f : Util.getDatapackFiles("zone", ".xml"))
        {
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setValidating(false);
                factory.setIgnoringComments(true);
                doc = factory.newDocumentBuilder().parse(f);
            } catch (Exception e)
            {
                _log.fatal("ZoneManager: Error loading file " + f.getAbsolutePath(), e);
            }
            try
            {
                parseDocument(doc);
            } catch (Exception e)
            {
                _log.fatal("ZoneManager: Error in file " + f.getAbsolutePath(), e);
            }
        }

        for (ZoneType zt : ZoneType.values())
        {
            _log.info("ZoneManager: Loaded '" + zt.toString() + "': " + getZones(zt).size() + " region(s).");
        }
    }

    protected void parseDocument(Document doc)
    {
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if ("list".equalsIgnoreCase(n.getNodeName()))
            {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
                {
                    if ("item".equalsIgnoreCase(d.getNodeName()))
                    {
                        IZone zone = parseEntry(d);
                        if (zone != null)
                            addZone(zone);
                    }
                }
            } else if ("item".equalsIgnoreCase(n.getNodeName()))
            {
                IZone zone = parseEntry(n);
                if (zone != null)
                    addZone(zone);
            }
        }
    }

    protected IZone parseEntry(Node n)
    {
        int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
        String name = n.getAttributes().getNamedItem("name").getNodeValue();
        int castleId = 0;
        int townId = -1;
        String typeName = "Default";

        List<Point3D> points = new FastList<Point3D>();
        Map<RestartType, Point3D> restarts = new FastMap<RestartType, Point3D>();

        Node first = n.getFirstChild();
        for (n = first; n != null; n = n.getNextSibling())
        {
            if ("restart".equalsIgnoreCase(n.getNodeName()))
            {
                restarts.put(RestartType.RestartNormal, parsePoint(n));

            } else if ("point".equalsIgnoreCase(n.getNodeName()))
            {
                points.add(parsePoint(n));
            } else if ("restart_chaotic".equalsIgnoreCase(n.getNodeName()))
            {
                restarts.put(RestartType.RestartChaotic, parsePoint(n));
            } else if ("restart_owner".equalsIgnoreCase(n.getNodeName()))
            {
                restarts.put(RestartType.RestartOwner, parsePoint(n));
            } else if ("castle".equalsIgnoreCase(n.getNodeName()))
            {
                castleId = Integer.parseInt(n.getTextContent());

            } else if ("town".equalsIgnoreCase(n.getNodeName()))
            {
                townId = Integer.parseInt(n.getTextContent());

            } else if ("type".equalsIgnoreCase(n.getNodeName()))
            {
                typeName = n.getTextContent();
            }
        }

        IZone zone;
        ZoneType zoneType = ZoneType.getZoneTypeEnum(typeName);

        if (zoneType == null)
        {
            _log.error("ZoneManager: Unknown zone type '" + typeName + "' !");
            return null;
        }

        if ((zoneType == ZoneType.Water && !Config.ALLOW_WATER) || (zoneType == ZoneType.Fishing && !Config.ALLOW_FISHING))
            return null;

        if (points.size() > 2)
        {
            zone = new ZonePoly(id, castleId, townId, name, zoneType);
            points.add(points.get(0));

        } else if (points.size() == 2)
        {
            zone = new ZoneRect(id, castleId, townId, name, zoneType);
        } else
            return null;

        for (Point3D point : points)
            zone.addPoint(point);
        for (Map.Entry<RestartType, Point3D> restart : restarts.entrySet())
            zone.addRestartPoint(restart.getKey(), restart.getValue());

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

    public final void addZone(IZone zone)
    {
        short region = 0;
        short region_new = 0;
        int multiRegion = 0;

        for (int x = zone.getMin().getX(); x <= zone.getMax().getX(); x += (1 << 14))
        {
            for (int y = zone.getMin().getY(); y <= zone.getMax().getY(); y += (1 << 14))
            {
                region_new = getMapRegion(x, y);
                if (region != region_new)
                {
                    region = region_new;
                    FastList<IZone> zones = getZones(zone.getZoneType(), region);
                    if (!zones.contains(zone))
                    {
                        zones.add(zone);
                        multiRegion++;
                        if (_log.isDebugEnabled())
                            if (multiRegion > 1)
                                _log.info("ZoneManager: multi-region zone (" + (region >> 8) + "_" + (region << 24 >> 24)
                                        + "): " + zone.getId() + " " + zone.getZoneName());
                            else
                                _log.info("ZoneManager: adding zone       (" + (region >> 8) + "_" + (region << 24 >> 24)
                                        + "): " + zone.getId() + " " + zone.getZoneName());

                    }
                }
            }
        }

    }

    public final IZone getZone(ZoneType zoneType, int id)
    {
        for (IZone zone : getZones(zoneType))
            if (zone.getId() == id)
                return zone;
        return null;
    }

    public final IZone getZone(ZoneType zoneType, String zoneName)
    {
        for (IZone zone : getZones(zoneType))
            if (zone.getZoneName().equalsIgnoreCase(zoneName))
                return zone;
        return null;
    }

    public final FastList<IZone> getZones(ZoneType zoneType, String zoneName)
    {
        FastList<IZone> zones = new FastList<IZone>();

        for (IZone zone : getZones(zoneType))
            if (zone.getZoneName().equalsIgnoreCase(zoneName))
                zones.add(zone);

        return zones;
    }

    public final FastList<IZone> getZones(ZoneType zoneType)
    {
        FastList<IZone> zones = new FastList<IZone>();

        for (short region : getZoneMap().keySet())
            for (Map.Entry<ZoneType, FastList<IZone>> zt : getZoneMap().get(region).entrySet())
                if (zt.getKey() == zoneType)
                    zones.addAll(zt.getValue());

        return zones;
    }

    public final FastList<IZone> getZones(ZoneType zoneType, int x, int y)
    {
        return getZones(zoneType, getMapRegion(x, y));
    }

    public final FastList<IZone> getZones(ZoneType zoneType, short region)
    {
        FastList<IZone> zones;

        if (getZoneMap().get(region) == null)
            getZoneMap().put(region, new FastMap<ZoneType, FastList<IZone>>());

        if (getZoneMap().get(region).get(zoneType) == null)
        {
            zones = new FastList<IZone>();
            getZoneMap().get(region).put(zoneType, zones);
        }

        return getZoneMap().get(region).get(zoneType);
    }

    public final FastMap<Short, FastMap<ZoneType, FastList<IZone>>> getZoneMap()
    {
        if (_zoneMap == null)
            _zoneMap = new FastMap<Short, FastMap<ZoneType, FastList<IZone>>>();
        return _zoneMap;
    }

    public static short getMapRegion(int x, int y)
    {
        int rx = ((x - L2World.MAP_MIN_X) >> 15) + 16;
        int ry = ((y - L2World.MAP_MIN_Y) >> 15) + 10;
        return (short) ((rx << 8) + ry);
    }

}