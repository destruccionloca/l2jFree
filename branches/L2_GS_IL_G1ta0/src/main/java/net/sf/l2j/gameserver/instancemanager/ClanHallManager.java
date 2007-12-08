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
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.tools.geometry.Point3D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ClanHallManager
{
    protected static Log _log = LogFactory.getLog(ClanHallManager.class.getName());
	
	private static ClanHallManager _instance;
	
	private Map<Integer, ClanHall> _clanHalls;

	public static ClanHallManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClanHallManager();
			_instance.load();
		}
		return _instance;
	}

	private ClanHallManager()
	{}
	
	/** Reload all clan halls */
	public final void reload()
	{
		_clanHalls.clear();
		load();
	}
	
	/** Load all clan halls */
	private final void load()
	{

		Document doc = null;

		File clanhallsXml = new File(Config.DATAPACK_ROOT, "data/clanhalls.xml");

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(clanhallsXml);
		}
		catch (Exception e)
		{
			_log.error("ClanHallManager: Error loading " + clanhallsXml.getAbsolutePath() + ": "+e.getMessage(),e);
		}
		try
		{
			parseDocument(doc);
		}
		catch (Exception e)
		{
			_log.error("ClanHallManager: Error while reading " + clanhallsXml.getAbsolutePath() + ": "+e.getMessage(),e);
		}

		_log.info("ClanHallManager: Loaded " + getClanHalls().size() + " clan halls.");
	}

	protected void parseDocument(Document doc)
	{
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if("list".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("item".equalsIgnoreCase(d.getNodeName()))
					{
						ClanHall clanHall = parseEntity(d);
						if(clanHall != null)
							getClanHalls().put(clanHall.getClanHallId(), clanHall);
					}
				}
			}
			else if("item".equalsIgnoreCase(n.getNodeName()))
			{
				ClanHall clanHall = parseEntity(n);
				if(clanHall != null)
					getClanHalls().put(clanHall.getClanHallId(), clanHall);
			}
		}
	}

	protected ClanHall parseEntity(Node n)
	{
		int id = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
		String name = n.getAttributes().getNamedItem("name").getNodeValue();

		ClanHall clanHall = new ClanHall(id, name);

		Node first = n.getFirstChild();
		for(n = first; n != null; n = n.getNextSibling())
		{
			if("zone".equalsIgnoreCase(n.getNodeName()))
			{
				IZone zone = ZoneManager.parseZone(n);

				if(zone != null)
				{
					if(zone.getSettings() == null)
						zone.setSettings(new StatsSet());
					zone.getSettings().set("clanHallId", id);
					clanHall.addZone(zone);
					ZoneManager.addZone(zone);
				}
			}
			else if("settings".equalsIgnoreCase(n.getNodeName()))
			{
				clanHall.setSettings(ZoneManager.parseSettings(n));
			}
			else if("restart".equalsIgnoreCase(n.getNodeName()))
			{
				String type = n.getAttributes().getNamedItem("type").getNodeValue();
				RestartType restartType = RestartType.getRestartTypeEnum(type);

				if(restartType == null)
					continue;

				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("point".equalsIgnoreCase(d.getNodeName()))
					{
						Point3D point = ZoneManager.parsePoint(d);
						clanHall.addRestartPoint(restartType, point);
					}
				}
			}
		}
		
		return clanHall;
	}
	
    /** Get clan hall by Id */
    public final ClanHall getClanHallById(int clanHallId)
    {
        return getClanHalls().get(clanHallId);
    }

    /** Get clan hall by name */
    public final ClanHall getClanHallByName(String name)
    {
       for(ClanHall clanHall : getClanHalls().values())
       {
			if(clanHall != null && clanHall.getName().equalsIgnoreCase(name.trim()))
				return clanHall;
       }
       return null;
    }
    
    /** Get clan hall by owner */
    public final ClanHall getClanHallByOwner(L2Clan clan)
    {
    	if (clan == null) return null;
    	return getClanHalls().get(clan.getHasHideout());
    }
    
    /** Get clan hall by location */
	public final ClanHall getClanHallByLoc(int x, int y, int z)
	{
		for(ClanHall clanHall : getClanHalls().values())
		{
			if(clanHall != null && clanHall.getZone(ZoneType.ClanHall) != null)
			{
				if(clanHall.getZone(ZoneType.ClanHall).checkIfInZone(x, y, z))
					return clanHall;
			}
		}
		return null;
	}
    
    /** Get clan hall nearest to location */
	public final ClanHall getClosestClanHall(int x, int y, int z)
	{
		double closestDistance = Double.MAX_VALUE;
		double distance;
		ClanHall closestClanHall = null;
		
		for(ClanHall clanHall : getClanHalls().values())
		{
			if(clanHall != null && clanHall.getZone(ZoneType.ClanHall) != null)
			{
				distance = clanHall.getZone(ZoneType.ClanHall).getZoneDistance(x, y, z);
				if(closestDistance > distance)
				{
					closestDistance = distance;
					closestClanHall = clanHall;
				}
			}
		}
		
		return closestClanHall;
	}	
	
	public final Map<Integer, ClanHall> getClanHalls()
	{
		if(_clanHalls == null)
			_clanHalls = new FastMap<Integer, ClanHall>();
		return _clanHalls;
	}
}
