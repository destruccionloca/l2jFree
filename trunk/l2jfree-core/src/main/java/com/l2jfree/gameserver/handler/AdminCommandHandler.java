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

import com.l2jfree.Config;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminAI;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminAdmin;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminBBS;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminBan;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminBanChat;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminBoat;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminBuffs;
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
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminElement;
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
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminSummon;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminTarget;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminTeleport;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminTest;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminTvTEngine;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminUnblockIp;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminVIPEngine;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminVitality;
import com.l2jfree.gameserver.handler.admincommandhandlers.AdminZone;
import com.l2jfree.gameserver.model.GMAudit;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.util.Util;

public final class AdminCommandHandler extends Handler<String, IAdminCommandHandler>
{
	private static AdminCommandHandler _instance;
	
	public static AdminCommandHandler getInstance()
	{
		if (_instance == null)
			_instance = new AdminCommandHandler();
		
		return _instance;
	}
	
	private AdminCommandHandler()
	{
		register(new AdminAdmin());
		register(new AdminBuffs());
		register(new AdminAI());
		register(new AdminAnnouncements());
		register(new AdminBan());
		register(new AdminBoat());
		register(new AdminJail());
		register(new AdminBanChat());
		register(new AdminBBS());
		register(new AdminCache());
		register(new AdminCamera());
		register(new AdminChangeAccessLevel());
		register(new AdminCreateItem());
		register(new AdminCTFEngine());
		register(new AdminCursedWeapons());
		register(new AdminDelete());
		register(new AdminDMEngine());
		register(new AdminDoorControl());
		register(new AdminEditChar());
		register(new AdminEditNpc());
		register(new AdminEffects());
		register(new AdminElement());
		register(new AdminEnchant());
		register(new AdminEventEngine());
		register(new AdminExpSp());
		register(new AdminFightCalculator());
		register(new AdminFortSiege());
		register(new AdminGeodata());
		register(new AdminGeoEditor());
		register(new AdminGm());
		register(new AdminGmChat());
		register(new AdminHeal());
		register(new AdminHelpPage());
		register(new AdminInvul());
		register(new AdminInstance());
		register(new AdminKick());
		register(new AdminKill());
		register(new AdminLevel());
		register(new AdminLogin());
		register(new AdminMammon());
		register(new AdminManor());
		register(new AdminMenu());
		register(new AdminMobGroup());
		register(new AdminMonsterRace());
		register(new AdminPetition());
		register(new AdminPForge());
		register(new AdminPledge());
		register(new AdminPolymorph());
		register(new AdminQuest());
		register(new AdminRegion());
		register(new AdminRepairChar());
		register(new AdminRes());
		register(new AdminRide());
		register(new AdminSendHome());
		register(new AdminShop());
		register(new AdminSortMultisellItems());
		register(new AdminShutdown());
		register(new AdminSiege());
		register(new AdminSkill());
		register(new AdminSmartShop());
		register(new AdminSpawn());
		register(new AdminSummon());
		register(new AdminTarget());
		register(new AdminTeleport());
		register(new AdminTvTEngine());
		register(new AdminTest());
		register(new AdminUnblockIp());
		register(new AdminVIPEngine());
		register(new AdminZone());
		register(new AdminVitality());
		
		if (Config.IRC_ENABLED)
			register(new AdminIRC());
		
		_log.info("AdminCommandHandler: Loaded " + size() + " handlers.");
		
		for (String cmd : Config.GM_COMMAND_PRIVILEGES.keySet())
			if (get(cmd) == null)
				_log.warn("AdminCommandHandler: Command \"" + cmd + "\" isn't used anymore.");
	}
	
	private void register(IAdminCommandHandler handler)
	{
		putAll(handler, handler.getAdminCommandList());
		
		for (String element : handler.getAdminCommandList())
			if (!Config.GM_COMMAND_PRIVILEGES.containsKey(element))
				_log.warn("Command \"" + element + "\" have no access level definition. Can't be used.");
	}
	
	public void useAdminCommand(L2PcInstance activeChar, String message)
	{
		message = message.trim();
		
		String command = message;
		String params = "";
		
		if (message.indexOf(" ") != -1)
		{
			command = message.substring(0, message.indexOf(" "));
			params = message.substring(message.indexOf(" ") + 1);
		}
		
		command = command.trim().toLowerCase();
		params = params.trim();
		
		if (!activeChar.isGM() && !command.equals("admin_gm"))
		{
			Util.handleIllegalPlayerAction(activeChar, "AdminCommandHandler: A non-gm request.", Config.DEFAULT_PUNISH);
			return;
		}
		
		final IAdminCommandHandler handler = get(command);
		
		if (handler == null)
		{
			activeChar.sendMessage("No handler registered.");
			
			_log.warn("No handler registered for bypass '" + message + "'");
			
			return;
		}
		
		if (!Config.GM_COMMAND_PRIVILEGES.containsKey(command))
		{
			activeChar.sendMessage("It has no access level definition. It can't be used.");
			
			_log.warn(message + "' have no access level definition. It can't be used.");
			
			return;
		}
		
		if (activeChar.getAccessLevel() < Config.GM_COMMAND_PRIVILEGES.get(command))
		{
			activeChar.sendMessage("You don't have sufficient privileges.");
			
			_log.warn(activeChar + " does not have sufficient privileges for '" + message + "'.");
			
			return;
		}
		
		GMAudit.auditGMAction(activeChar, "admincommand", command, params);
		
		handler.useAdminCommand(message, activeChar);
	}
}
