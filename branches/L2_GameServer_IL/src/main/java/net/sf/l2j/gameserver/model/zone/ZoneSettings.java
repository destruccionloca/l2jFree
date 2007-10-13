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
package net.sf.l2j.gameserver.model.zone;

import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.StringTokenizer;
import java.text.ParseException;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Node;

/**
 * @author crion
 * 
 */

public final class ZoneSettings
{
	private final static Log _log = LogFactory.getLog(ZoneSettings.class.getName());

	// Messages
	private int _onEnterSysMsg;
	private int _onExitSysMsg;

	private String _onEnterMsg;
	private String _onExitMsg;

	// Zone effects
	private FastList<L2Skill> _zoneSkills;

	private int _zoneMask = 0;
	private int _abnormal = 0;
	private int _castleId = 0;
	private int _clanhallId = 0;
	private int _stadium = 0;
	private int _townId = 0;
	private int _redirect = 9;
	private int _taxById = 0;
	private String _townName;
	
	
	public int getOnEnterSysMsg()
	{
		return _onEnterSysMsg;
	}
	
	public int getOnExitSysMsg()
	{
		return _onExitSysMsg;
	}
	
	public String getOnEnterMsg()
	{
		return _onEnterMsg;
	}
	
	public String getOnExitMsg()
	{
		return _onExitMsg;
	}

	public int getMask()
	{
		return _zoneMask;
	}

	public int getCastleId()
	{
		return _castleId;
	}

	public int getClanHallId()
	{
		return _clanhallId;
	}

	public int getStadiumId()
	{
		return _stadium;
	}

	public String getTownName()
	{
		return _townName;
	}
	
	public int getTownId()
	{
		return _townId;
	}
	
	public int getRedirect()
	{
		return _redirect;
	}
	
	public int getTaxById()
	{
		return _taxById;
	}

	public FastList<L2Skill> getZoneSkills()
	{
		return _zoneSkills;
	}

	private ZoneSettings(StatsSet set)
	{
		parseMessages(set.getString("onEnter", null), set.getString("onExit", null));
		parseZoneMask(set.getString("zoneMask", null));
		parseZoneSkills(set.getString("skill", null));
		parseCastleClanhall(set.getString("castle", null), set.getString("clanhall", null));
		parseStadium(set.getString("stadium", null));
		parseTown(set.getString("townName", null), set.getString("town", null),
					set.getString("redirectTown", null), set.getString("taxById", null));
	}

	public static ZoneSettings parse(Node setNode)
	{
		StatsSet set = new StatsSet();
		
		Node first = setNode.getFirstChild();
		for (Node n = first; n != null; n = n.getNextSibling())
		{
			if ("set".equalsIgnoreCase(n.getNodeName()))
				parseSet(n, set);
		}
		
		return new ZoneSettings(set);
	}

	private void parseTown(String name, String townId, String redirect, String tax)
	{
		_townName = (name == null ? "" : name);
		try
		{
			int id = Integer.parseInt(townId);
			if(id > 0) _townId = id;
		}
		catch(NumberFormatException nfe)
		{
			_log.warn("Unknown town id: "+townId);
		}

		try
		{
			int id = Integer.parseInt(redirect);
			if(id > 0) _redirect = id;
		}
		catch(NumberFormatException nfe)
		{
			_log.warn("Unknown redirect town id: "+redirect);
		}

		try
		{
			int taxBy = Integer.parseInt(tax);
			if(taxBy > 0) _taxById = taxBy;
		}
		catch(NumberFormatException nfe)
		{
			_log.warn("Unknown tax id: "+tax);
		}
	}

	private void parseStadium(String stadium)
	{
		try
		{
			int std = Integer.parseInt(stadium);
			if(std > 0) _stadium = std;
		}
		catch(NumberFormatException nfe)
		{
			_log.warn("Invalid stadium: "+stadium);
		}
	}

	private void parseCastleClanhall(String castle, String clanhall)
	{
		try
		{
			int castleId = Integer.parseInt(castle);
			if(castleId > 0) _castleId = castleId;
		}
		catch(NumberFormatException nfe)
		{
			_log.warn("Unknown castle id: "+castle);
		}

		try
		{
			int chId = Integer.parseInt(clanhall);
			if(chId > 0) _clanhallId = chId;
		}
		catch(NumberFormatException nfe)
		{
			_log.warn("Unknown clanhall id: "+clanhall);
		}
	}
	
	private void parseMessages(String onEnter, String onExit)
	{
		try
		{
			int msg = Integer.parseInt(onEnter);
			if(msg > 0)	_onEnterSysMsg = msg;
		}
		catch(NumberFormatException nfe)
		{
			_onEnterMsg = onEnter;
		}

		try
		{
			int msg = Integer.parseInt(onExit);
			if(msg > 0) _onExitSysMsg = msg;
		}
		catch(NumberFormatException nfe)
		{
			_onExitMsg = onExit;
		}
	}

	private void parseZoneSkills(String skillSet)
	{
		if(skillSet != null)
		{
			if(_zoneSkills == null)
				_zoneSkills = new FastList<L2Skill>();
			else
				_zoneSkills.clear();

			StringTokenizer st = new StringTokenizer(skillSet, ";");
			while(st.hasMoreTokens())
			{
				int skillId = 0;
				int level = 0;
				try
				{
					StringTokenizer st2 = new StringTokenizer(st.nextToken(), ",");
					skillId = Integer.parseInt(st2.nextToken());
					level = Integer.parseInt(st2.nextToken());
				}
				catch(Exception e)
				{
					_log.warn("Invalid zone skills: "+skillSet);
					continue;
				}
				
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
				if(skill == null)
				{
					_log.warn("Invalid zone skills: "+skillSet);
					continue;
				}
				
				_zoneSkills.add(skill);
			}
		}
	}

	private void parseZoneMask(String maskSet)
	{
		if(maskSet != null)
		{
			StringTokenizer st = new StringTokenizer(maskSet, ";");
			while(st.hasMoreTokens())
			{
				String mask = st.nextToken();
				if(mask.equalsIgnoreCase("PVP")) _zoneMask |= L2Character.ZONE_PVP;
				else if(mask.equalsIgnoreCase("PEACE"))	_zoneMask |= L2Character.ZONE_PEACE;
				else if(mask.equalsIgnoreCase("SIEGE"))	_zoneMask |= L2Character.ZONE_SIEGE;
				else if(mask.equalsIgnoreCase("MOTHERTREE")) _zoneMask |= L2Character.ZONE_MOTHERTREE;
				else if(mask.equalsIgnoreCase("CLANHALL")) _zoneMask |= L2Character.ZONE_CLANHALL;
				else if(mask.equalsIgnoreCase("NOESCAPE")) _zoneMask |= L2Character.ZONE_NOESCAPE;
				else if(mask.equalsIgnoreCase("NOLANDING")) _zoneMask |= L2Character.ZONE_NOLANDING;
				else if(mask.equalsIgnoreCase("WATER")) _zoneMask |= L2Character.ZONE_WATER;
				else if(mask.equalsIgnoreCase("JAIL")) _zoneMask |= L2Character.ZONE_JAIL;
				else if(mask.equalsIgnoreCase("MOSTERTRACK")) _zoneMask |= L2Character.ZONE_MOSTERTRACK;
				else if(mask.equalsIgnoreCase("FOURSEPULCHERS")) _zoneMask |= L2Character.ZONE_FOURSEPULCHERS;

				else if(mask.equalsIgnoreCase("ANTHARAS")) _zoneMask |= L2Character.LAIR_ANTHARAS;
				else if(mask.equalsIgnoreCase("BAIUM")) _zoneMask |= L2Character.LAIR_BAIUM;
				else if(mask.equalsIgnoreCase("SAILREN")) _zoneMask |= L2Character.LAIR_SAILREN;
				else if(mask.equalsIgnoreCase("VALAKAS")) _zoneMask |= L2Character.LAIR_VALAKAS;
				else if(mask.equalsIgnoreCase("SUNLIGHT")) _zoneMask |= L2Character.LAIR_SUNLIGHT;
				else _log.warn("Invalid zone type: "+mask);
			}
		}
	}

	protected static void parseSet(Node n, StatsSet set)
	{
		String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
		String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
		set.set(name, value);
	}
}
