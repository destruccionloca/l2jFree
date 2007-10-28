/**
 * 
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
			// Create the zone handler constructor
			c = Class.forName("net.sf.l2j.gameserver.hadler.zonehandlers." + zone.getZoneType().toString() + "Zone").getConstructor(IZone.class);
		}
		catch (Exception e)
		{}

		if(c == null)
			try
			{
				// Create the zone handler constructor
				c = Class.forName("net.sf.l2j.gameserver.hadler.zonehandlers.DefaultZone").getConstructor(IZone.class);
			}
			catch (Exception e)
			{}

		if(c != null)
			try
			{
				// Create the zone handler instance
				h = (IZoneHandler) c.newInstance((Object) zone);
			}
			catch (Exception e)
			{}

		return h;
	}

}
