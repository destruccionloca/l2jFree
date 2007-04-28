/*
 * This program is free software; you can redistribute it and/or modify
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

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.CastleDoors;
import net.sf.l2j.gameserver.handler.voicedcommandhandlers.Wedding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class VoicedCommandHandler
{
	private final static Log _log = LogFactory.getLog(ItemHandler.class.getName());
	
	private static VoicedCommandHandler _instance;
	
	private FastMap<String, IVoicedCommandHandler> _datatable;
	
	public static VoicedCommandHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new VoicedCommandHandler();
		}
		return _instance;
	}
	
	private VoicedCommandHandler()
	{
		_datatable = new FastMap<String, IVoicedCommandHandler>();
        registerVoicedCommandHandler(new CastleDoors());
        if(Config.ALLOW_WEDDING)
        {
            registerVoicedCommandHandler(new Wedding());
        }
	}
	
	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		String[] ids = handler.getVoicedCommandList();
		for (int i = 0; i < ids.length; i++)
		{
			if (_log.isDebugEnabled()) _log.debug("Adding handler for command "+ids[i]);
			_datatable.put(new String(ids[i]), handler);
		}
	}
	
	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.indexOf(" ") != -1) {
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		if (_log.isDebugEnabled())
			_log.debug("getting handler for command: "+command+
					" -> "+(_datatable.get(new String(command)) != null));
		return _datatable.get(command);
	}

    /**
     * @return
     */
    public int size()
    {
        return _datatable.size();
    }
}
