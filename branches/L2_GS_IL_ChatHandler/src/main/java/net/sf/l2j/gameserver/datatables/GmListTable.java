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
package net.sf.l2j.gameserver.datatables;

import javolution.util.FastList;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class stores references to all online game masters. (access level > 100)
 * 
 * @version $Revision: 1.2.2.1.2.7 $ $Date: 2005/04/05 19:41:24 $
 */
public class GmListTable
{
	private final static Log _log = LogFactory.getLog(GmListTable.class.getName());
	private static GmListTable _instance;
	
	private class GmListEntry
	{
		public L2PcInstance gm;
		public boolean hidden;
		
		public GmListEntry(L2PcInstance player)
		{
			gm = player;
		}
	}

	/** Set(L2PcInstance>) containing all the GM in game */
	private FastList<GmListEntry> _gmList;
	
	public static GmListTable getInstance()
	{
		if (_instance == null)
			_instance = new GmListTable();
		return _instance;
	}

	public L2PcInstance[] getAllGms(boolean includeHidden)
	{
		FastList<L2PcInstance> tmpGmList = new FastList<L2PcInstance>();
		
		for (GmListEntry temp : _gmList)
		{
			if (includeHidden || !temp.hidden)
				tmpGmList.add(temp.gm);
		}

		return tmpGmList.toArray(new L2PcInstance[tmpGmList.size()]);
	}

	public String[] getAllGmNames(boolean includeHidden)
	{
		FastList<String> tmpGmList = new FastList<String>();

		for (GmListEntry temp : _gmList)
		{
			if (!temp.hidden)
				tmpGmList.add(temp.gm.getName());
			else if (includeHidden)
				tmpGmList.add(temp.gm.getName()+" (invis)");
		}

		return tmpGmList.toArray(new String[tmpGmList.size()]);
	}

	private GmListTable()
	{
		_log.info("GmListTable: initalized.");		
		_gmList = new FastList<GmListEntry>();
	}
	
	/**
	 * Add a L2PcInstance player to the Set _gmList
	 */
	public void addGm(L2PcInstance player, boolean hidden)
	{
		if (_log.isDebugEnabled()) _log.debug("added gm: "+player.getName());
		_gmList.add(new GmListEntry(player));
	}
	
	public void deleteGm(L2PcInstance player)
	{
		if (_log.isDebugEnabled()) _log.debug("deleted gm: "+player.getName());

		for (GmListEntry temp : _gmList)
		{
			if (temp.gm == player)
			{
				_gmList.remove(temp);
				return;
			}
		}
	}
	
	/**
	 * GM will be displayed on clients gmlist
	 * @param player
	 */
	public void showGm(L2PcInstance player)
	{
		for (GmListEntry temp : _gmList)
		{
			if (temp.gm == player)
			{
				temp.hidden = false;
				return;
			}
		}
	}
	
	/**
	 * GM will no longer be displayed on clients gmlist
	 * @param player
	 */
	public void hideGm(L2PcInstance player)
	{
		for (GmListEntry temp : _gmList)
		{
			if (temp.gm == player)
			{
				temp.hidden = true;
				return;
			}
		}
	}

	public boolean isGmOnline(boolean includeHidden)
	{
		for (GmListEntry temp : _gmList)
		{
			if (includeHidden || !temp.hidden)
				return true;
		}

		return false;
	}
	
	public void sendListToPlayer (L2PcInstance player)
	{
		if (!isGmOnline(player.isGM()))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
			player.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.GM_LIST);
			player.sendPacket(sm);

			for (String name : getAllGmNames(player.isGM()))
			{
				sm = new SystemMessage(SystemMessageId.GM_S1);
				sm.addString(name);
				player.sendPacket(sm);
			}
		}
	}

	public static void broadcastToGMs(L2GameServerPacket packet)
	{
		for (L2PcInstance gm : getInstance().getAllGms(true))
		{
			gm.sendPacket(packet);
		}
	}

	public static void broadcastMessageToGMs(String message)
	{
		for (L2PcInstance gm : getInstance().getAllGms(true))
		{
			gm.sendPacket(SystemMessage.sendString(message));
		}
	}
}
