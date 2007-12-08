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
package net.sf.l2j.gameserver.handler;

import java.lang.reflect.Constructor;
import net.sf.l2j.gameserver.model.zone.IZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author G1ta0
 *
 */
public class ZoneHandler
{
	private final static Log _log = LogFactory.getLog(ZoneHandler.class.getName());

	private static ZoneHandler _instance;

	private static String _handlersClassPath = "net.sf.l2j.gameserver.handler.zonehandlers";
	
	public static ZoneHandler getInstance()
	{
		if(_instance == null)
			_instance = new ZoneHandler();
		return _instance;
	}

	public IZoneHandler getZoneHandler(IZone zone)
	{
		Constructor c = null;
		IZoneHandler h = null;

		try
		{
			// Get the zone handler constructor
			c = Class.forName(_handlersClassPath + "."+ zone.getZoneType().toString() + "Zone").getConstructor(IZone.class);
		}
		catch (Exception e)
		{}

		if(c == null)
			try
			{
				// Get the zone handler constructor
				c = Class.forName(_handlersClassPath + ".DefaultZone").getConstructor(IZone.class);
			}
			catch (Exception e)
			{}

		if(c != null)
			try
			{
				// Create the zone handler instance
				h = (IZoneHandler) c.newInstance(zone);
			}
			catch (Exception e)
			{
				_log.error("Unable to create zonehandler!", e);
			}

		return h;
	}

}
