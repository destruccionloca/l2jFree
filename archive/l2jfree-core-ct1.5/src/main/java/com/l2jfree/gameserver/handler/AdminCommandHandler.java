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
package com.l2jfree.gameserver.handler;

import java.util.StringTokenizer;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminAdmin;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminBBS;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminBan;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminBanChat;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminCTFEngine;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminCache;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminCamera;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminChangeAccessLevel;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminCreateItem;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminDMEngine;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminDelete;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminDoorControl;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminEditChar;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminEditNpc;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminEffects;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminEnchant;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminEventEngine;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminExpSp;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminFightCalculator;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminFortSiege;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminGeoEditor;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminGeodata;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminGm;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminGmChat;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminHeal;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminHelpPage;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminIRC;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminInstance;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminInvul;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminJail;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminKick;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminKill;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminLevel;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminLogin;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminMammon;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminManor;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminMenu;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminMobGroup;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminMonsterRace;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminPForge;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminPathNode;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminPetition;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminPledge;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminPolymorph;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminQuest;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminRegion;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminRepairChar;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminRes;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminRide;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminSendHome;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminShop;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminShutdown;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminSiege;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminSkill;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminSmartShop;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminSortMultisellItems;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminSpawn;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminTarget;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminTeleport;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminTest;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminTvTEngine;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminUnblockIp;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminVIPEngine;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminZone;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class AdminCommandHandler
{
	private final static Log						_log	= LogFactory.getLog(AdminCommandHandler.class.getName());

	private static AdminCommandHandler				_instance;

	private FastMap<String, IAdminCommandHandler>	_datatable;

	public static AdminCommandHandler getInstance()
	{
		if (_instance == null)
			_instance = new AdminCommandHandler();
		return _instance;
	}

	private AdminCommandHandler()
	{
		_datatable = new FastMap<String, IAdminCommandHandler>();
		registerAdminCommandHandler(new AdminAdmin());
		registerAdminCommandHandler(new AdminAnnouncements());
		registerAdminCommandHandler(new AdminBan());
		registerAdminCommandHandler(new AdminJail());
		registerAdminCommandHandler(new AdminBanChat());
		registerAdminCommandHandler(new AdminBBS());
		registerAdminCommandHandler(new AdminCache());
		registerAdminCommandHandler(new AdminCamera());
		registerAdminCommandHandler(new AdminChangeAccessLevel());
		registerAdminCommandHandler(new AdminCreateItem());
		registerAdminCommandHandler(new AdminCTFEngine());
		registerAdminCommandHandler(new AdminCursedWeapons());
		registerAdminCommandHandler(new AdminDelete());
		registerAdminCommandHandler(new AdminDMEngine());
		registerAdminCommandHandler(new AdminDoorControl());
		registerAdminCommandHandler(new AdminEditChar());
		registerAdminCommandHandler(new AdminEditNpc());
		registerAdminCommandHandler(new AdminEffects());
		registerAdminCommandHandler(new AdminEnchant());
		registerAdminCommandHandler(new AdminEventEngine());
		registerAdminCommandHandler(new AdminExpSp());
		registerAdminCommandHandler(new AdminFightCalculator());
		registerAdminCommandHandler(new AdminFortSiege());
		registerAdminCommandHandler(new AdminGeodata());
		registerAdminCommandHandler(new AdminGeoEditor());
		registerAdminCommandHandler(new AdminGm());
		registerAdminCommandHandler(new AdminGmChat());
		registerAdminCommandHandler(new AdminHeal());
		registerAdminCommandHandler(new AdminHelpPage());
		registerAdminCommandHandler(new AdminInvul());
		registerAdminCommandHandler(new AdminInstance());
		registerAdminCommandHandler(new AdminKick());
		registerAdminCommandHandler(new AdminKill());
		registerAdminCommandHandler(new AdminLevel());
		registerAdminCommandHandler(new AdminLogin());
		registerAdminCommandHandler(new AdminMammon());
		registerAdminCommandHandler(new AdminManor());
		registerAdminCommandHandler(new AdminMenu());
		registerAdminCommandHandler(new AdminMobGroup());
		registerAdminCommandHandler(new AdminMonsterRace());
		registerAdminCommandHandler(new AdminPetition());
		registerAdminCommandHandler(new AdminPForge());
		registerAdminCommandHandler(new AdminPathNode());
		registerAdminCommandHandler(new AdminPledge());
		registerAdminCommandHandler(new AdminPolymorph());
		registerAdminCommandHandler(new AdminQuest());
		registerAdminCommandHandler(new AdminRegion());
		registerAdminCommandHandler(new AdminRepairChar());
		registerAdminCommandHandler(new AdminRes());
		registerAdminCommandHandler(new AdminRide());
		registerAdminCommandHandler(new AdminSendHome());
		registerAdminCommandHandler(new AdminShop());
		registerAdminCommandHandler(new AdminSortMultisellItems());
		registerAdminCommandHandler(new AdminShutdown());
		registerAdminCommandHandler(new AdminSiege());
		registerAdminCommandHandler(new AdminSkill());
		registerAdminCommandHandler(new AdminSmartShop());
		registerAdminCommandHandler(new AdminSpawn());
		registerAdminCommandHandler(new AdminTarget());
		registerAdminCommandHandler(new AdminTeleport());
		registerAdminCommandHandler(new AdminTvTEngine());
		registerAdminCommandHandler(new AdminTest());
		registerAdminCommandHandler(new AdminUnblockIp());
		registerAdminCommandHandler(new AdminVIPEngine());
		registerAdminCommandHandler(new AdminZone());
		if (Config.IRC_ENABLED)
			registerAdminCommandHandler(new AdminIRC());
		_log.info("AdminCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}

	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (String element : ids)
		{
			if (_log.isDebugEnabled())
				_log.debug("Adding handler for command " + element);

			if (_datatable.keySet().contains(new String(element)))
			{
				_log.warn("Duplicated command \"" + element + "\" definition in " + handler.getClass().getName() + ".");
			}
			else
				_datatable.put(element, handler);

			if (Config.ALT_PRIVILEGES_ADMIN && !Config.GM_COMMAND_PRIVILEGES.containsKey(element))
				_log.warn("Command \"" + element + "\" have no access level definition. Can't be used.");
		}
	}

	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;
		if (adminCommand.indexOf(" ") != -1)
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		if (_log.isDebugEnabled())
			_log.debug("getting handler for command: " + command + " -> " + (_datatable.get(command) != null));
		return _datatable.get(command);
	}

	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}

	public void checkDeprecated()
	{
		if (Config.ALT_PRIVILEGES_ADMIN)
			for (Object cmd : Config.GM_COMMAND_PRIVILEGES.keySet())
			{
				String _cmd = String.valueOf(cmd);
				if (!_datatable.containsKey(_cmd))
					_log.warn("Command \"" + _cmd + "\" is no used anymore.");
			}
	}

	public final boolean checkPrivileges(L2PcInstance player, String command)
	{
		// Can execute a admin command if everybody has admin rights
		if (Config.EVERYBODY_HAS_ADMIN_RIGHTS)
			return true;

		//Only a GM can execute a admin command
		if (!player.isGM())
			return false;

		StringTokenizer st = new StringTokenizer(command, " ");

		String cmd = st.nextToken(); // get command

		//Check command existance
		if (!_datatable.containsKey(cmd))
			return false;

		//Check command privileges
		if (Config.ALT_PRIVILEGES_ADMIN)
		{
			if (Config.GM_COMMAND_PRIVILEGES.containsKey(cmd))
			{
				return (player.getAccessLevel() >= Config.GM_COMMAND_PRIVILEGES.get(cmd));
			}
			_log.warn("Command \"" + cmd + "\" have no access level definition. Can't be used.");
			return false;
		}
		/*
		else
			if (!_datatable.get(cmd).checkLevel(player.getAccessLevel()))
				return false;	
		*/
		if (player.getAccessLevel() > 0)
			return true;
		_log.warn("GM " + player.getName() + "(" + player.getObjectId() + ") have no access level.");
		return false;
	}
}
