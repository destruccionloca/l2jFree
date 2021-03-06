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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Olympiad;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.NpcWalkerRoutesTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.datatables.TradeListTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.FortSiegeManager;
import net.sf.l2j.gameserver.instancemanager.Manager;
import net.sf.l2j.gameserver.instancemanager.MapRegionManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PetInfo;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.util.Util;

/**
 * This class handles following admin commands:
 * - admin|admin1/admin2/admin3/admin4/admin5 = slots for the 5 starting admin menus 
 * - gmliston/gmlistoff = includes/excludes active character from /gmlist results 
 * - silence = toggles private messages acceptance mode 
 * - diet = toggles weight penalty mode 
 * - tradeoff = toggles trade acceptance mode 
 * - reload = reloads specified component from multisell|skill|npc|htm|item|instancemanager 
 * - set/set_menu/set_mod = alters specified server setting
 * - saveolymp = saves olympiad state manually 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2007/07/28 10:06:06 $
 */
public class AdminAdmin implements IAdminCommandHandler
{
	//private final static Log _log = LogFactory.getLog(AdminAdmin.class);

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_admin",
		"admin_admin1",
		"admin_admin2",
		"admin_admin3",
		"admin_admin4",
		"admin_admin5",
		"admin_gmliston",
		"admin_gmlistoff",
		"admin_silence",
		"admin_diet",
		"admin_tradeoff",
		"admin_reload",
		"admin_set",
		"admin_set_menu",
		"admin_set_mod",
		"admin_saveolymp",
		"admin_endolympiad",
		// L2J-FREE
		"admin_camera",	// test for moviemode.
		"admin_reload_config",
		"admin_config_server",
		"admin_summon",
		"admin_unsummon",
		"admin_memusage"
	};

	private static final int REQUIRED_LEVEL = Config.GM_MENU;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
		
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
		
		if (command.startsWith("admin_admin"))
		{
			showMainPage(activeChar, command);
		}
		else if (command.equals("admin_config_server"))
		{
			ShowConfigPage(activeChar);
		}
		else if (command.equals("admin_config_server2"))
		{
			ShowConfigPage2(activeChar);
		}
		else if(command.startsWith("admin_gmliston"))
		{
			GmListTable.getInstance().showGm(activeChar);
			activeChar.sendMessage("Showing on gm list");
		}
		else if(command.startsWith("admin_gmlistoff"))
		{
			GmListTable.getInstance().hideGm(activeChar);
			activeChar.sendMessage("Hiding from gm list");
		}
		else if(command.startsWith("admin_silence"))
		{	 	
			if (activeChar.getMessageRefusal()) // already in message refusal mode
			{
				activeChar.setMessageRefusal(false);
				activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_ACCEPTANCE_MODE));
			}
			else
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(new SystemMessage(SystemMessageId.MESSAGE_REFUSAL_MODE));
			}		
			
		}
		else if(command.startsWith("admin_reload_config"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();

			try
			{
				String type = st.nextToken();
				if(type.equals("all"))
				{
					Config.load();
					activeChar.sendMessage("All configs reloaded");
				}
				else if(type.equals("rates"))
				{
					Config.loadRatesConfig();
					activeChar.sendMessage("Rates config reloaded");
				}
				else if(type.equals("enchant"))
				{
					Config.loadEnchantConfig();
					activeChar.sendMessage("Enchant config reloaded");
				}
				else if(type.equals("pvp"))
				{
					Config.loadPvpConfig();
					activeChar.sendMessage("Pvp config reloaded");
				}
				else if(type.equals("options"))
				{
					Config.loadOptionsConfig();
					activeChar.sendMessage("Options config reloaded");
				}
				else if(type.equals("other"))
				{
					Config.loadOtherConfig();
					activeChar.sendMessage("Other config reloaded");
				}
				else if(type.equals("alt"))
				{
					Config.loadAltConfig();
					activeChar.sendMessage("Alt config reloaded");
				}
				else if(type.equals("clans"))
				{
					Config.loadClansConfig();
					activeChar.sendMessage("Clans config reloaded");
				}
				else if(type.equals("champions"))
				{
					Config.loadChampionsConfig();
					activeChar.sendMessage("Champions config reloaded");
				}
				else if(type.equals("lottery"))
				{
					Config.loadLotteryConfig();
					activeChar.sendMessage("Lottery config reloaded");
				}
				else if(type.equals("sepulchers"))
				{
					Config.loadSepulchersConfig();
					activeChar.sendMessage("Four Sepulchers config reloaded");
				}
				else if(type.equals("clanhall"))
				{
					Config.loadClanHallConfig();
					activeChar.sendMessage("Clanhall config reloaded");
				}
				else if(type.equals("funengines"))
				{
					Config.loadFunEnginesConfig();
					activeChar.sendMessage("Fun engines config reloaded");
				}
				else if(type.equals("sevensigns"))
				{
					Config.loadSevenSignsConfig();
					activeChar.sendMessage("Seven Signs config reloaded");
				}
				else if(type.equals("gmconf"))
				{
					Config.loadGmAccess();
					activeChar.sendMessage("Gm config reloaded");
				}
				else if(type.equals("irc"))
				{
					Config.loadIrcConfig();
					activeChar.sendMessage("Irc config reloaded");
				}
				else if(type.equals("boss"))
				{
					Config.loadBossConfig();
					activeChar.sendMessage("Boss config reloaded");
				}
				else if(type.equals("sayfilter"))
				{
					Config.loadSayFilter();
					activeChar.sendMessage("Sayfilter reloaded");
				}
				else if(type.equals("access"))
				{
					Config.loadPrivilegesConfig();
					activeChar.sendMessage("Access config reloaded");
				}
				else if(type.equals("fortsiege"))
				{
					Config.loadFortSiegeConfig();
					activeChar.sendMessage("FortSiege config reloaded");
				}
				else if(type.equals("siege"))
				{
					Config.loadSiegeConfig();
					activeChar.sendMessage("Siege config reloaded");
				}
				else if(type.equals("wedding"))
				{
					Config.loadWeddingConfig();
					activeChar.sendMessage("Wedding config reloaded");
				}
				else if(type.equals("kamael"))
				{
					Config.loadKamaelConfig();
					activeChar.sendMessage("Kamael config reloaded");
				}
				else if(type.equals("elayne"))
				{
					Config.loadElayneConfig();
					activeChar.sendMessage("Elayne config reloaded");
				}
				else
				{
					activeChar.sendMessage("Usage:  //reload_config <all|rates|enchant|pvp|options|other|alt|olympiad|clans|champions|lottery|sepulchers|clanhall|funengines|sevensigns|gmconf|access|irc|boss|sayfilter|siege|fortsiege|wedding|kamael|elayne>");
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage:  //reload_config <all|rates|enchant|pvp|options|other|alt|olympiad|clans|champions|lottery|sepulchers|clanhall|funengines|sevensigns|gmconf|access|irc|boss|sayfilter|siege|fortsiege|wedding|kamael|elayne>");
			}
		}

		//[L2J_JP_ADD
        else if(command.startsWith("admin_camera"))
        {
            if(activeChar.getTarget() == null)
            {
                activeChar.sendMessage("Target incorrect.");
                activeChar.sendMessage("Usage:  //camera dist yaw pitch time duration");
            }
            else
            {
                StringTokenizer st = new StringTokenizer(command);
                st.nextToken();

                try
                {
                	L2Object target = activeChar.getTarget();
                	int scDist = Integer.parseInt(st.nextToken());
                	int scYaw = Integer.parseInt(st.nextToken());
                	int scPitch = Integer.parseInt(st.nextToken());
                	int scTime = Integer.parseInt(st.nextToken());
                	int scDuration = Integer.parseInt(st.nextToken());
                	activeChar.enterMovieMode();
                	activeChar.specialCamera(target, scDist, scYaw, scPitch, scTime, scDuration);
                }
                catch(Exception e)
                {
                    activeChar.sendMessage("Usage:  //camera dist yaw pitch time duration");
                }
                finally
                {
                	activeChar.leaveMovieMode();
                }
            }
        }		
		else if(command.startsWith("admin_summon"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try{
				int npcId = Integer.parseInt(st.nextToken());
				if(npcId != 0)
					adminSummon(activeChar, npcId);
			}catch(Exception e){
				activeChar.sendMessage("Usage: //summon <npcid>");
			}
		}
		else if(command.startsWith("admin_memsuage"))
		{
			for (String line : Util.getMemUsage())
			{
				activeChar.sendMessage(line);
			}
		}
		else if(command.startsWith("admin_unsummon"))
		{
			if (activeChar.getPet() != null)
				activeChar.getPet().unSummon(activeChar);
		}
		else if(command.startsWith("admin_saveolymp"))
		{
			try 
			{
				Olympiad.getInstance().save();
			}
			catch(Exception e){e.printStackTrace();}
			activeChar.sendMessage("Olympiad stuff saved!");
		}
		else if(command.startsWith("admin_endolympiad"))
		{
			try 
			{
				Olympiad.getInstance().manualSelectHeroes();
			}
			catch(Exception e){e.printStackTrace();}
			activeChar.sendMessage("Heroes formed");
		}
		else if(command.startsWith("admin_diet"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				if(st.nextToken().equalsIgnoreCase("on"))
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("Diet mode on");
				}
				else if(st.nextToken().equalsIgnoreCase("off"))
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("Diet mode off");
				}
			}
			catch(Exception ex)
			{
				if(activeChar.getDietMode())
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("Diet mode off");
				}
				else
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("Diet mode on");
				}
			}
			finally
			{
				activeChar.refreshOverloaded();
			}
		}
		else if(command.startsWith("admin_tradeoff"))
		{
			try
			{
				String mode = command.substring(15);
				if (mode.equalsIgnoreCase("on"))
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("Trade refusal enabled");
				}
				else if (mode.equalsIgnoreCase("off"))
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("Trade refusal disabled");
				}
			}
			catch(Exception ex) 
			{
				if(activeChar.getTradeRefusal())
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("Trade refusal disabled");
				}
				else
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("Trade refusal enabled");
				}
			}			
		}
		else if(command.startsWith("admin_reload"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				if(type.startsWith("multisell"))
				{
					L2Multisell.getInstance().reload();
					activeChar.sendMessage("Multisell reloaded");
				}
				else if(type.startsWith("teleport"))
				{
					TeleportLocationTable.getInstance().reloadAll();
					activeChar.sendMessage("Teleport location table reloaded");
				}
				else if(type.startsWith("skill"))
				{
					SkillTable.getInstance().reload();
					activeChar.sendMessage("Skills reloaded");
				}
				else if(type.startsWith("npcwalker"))
				{
					NpcWalkerRoutesTable.getInstance().load();
					activeChar.sendMessage("All NPC walker routes have been reloaded");
				}
				else if(type.startsWith("npc"))
				{
					NpcTable.getInstance().cleanUp();
					NpcTable.getInstance().reloadAll();
					activeChar.sendMessage("Npcs reloaded");
				}
				else if(type.startsWith("htm"))
				{
					HtmCache.getInstance().reload();
					activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage()  + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");
				}
				else if(type.startsWith("item"))
				{
					ItemTable.getInstance().reload();
					activeChar.sendMessage("Item templates reloaded");
				}
				else if(type.startsWith("instancemanager"))
				{
					Manager.reloadAll();
					activeChar.sendMessage("All instance manager has been reloaded");
				}
				else if(type.startsWith("tradelist"))
				{
					TradeListTable.getInstance().reloadAll();
					activeChar.sendMessage("TradeList Table reloaded.");
				}
				else if(type.startsWith("zone"))
				{
					ZoneManager.getInstance().reload();
					activeChar.sendMessage("Zones reloaded.");
				}
				else if(type.startsWith("mapregion"))
				{
					MapRegionManager.getInstance().reload();
					activeChar.sendMessage("MapRegions reloaded.");
				}
				else if(type.startsWith("siege"))
				{
					SiegeManager.getInstance().reload();
					FortSiegeManager.getInstance().reload();
					activeChar.sendMessage("Castle/Fortress Siege config reloaded");
				}
				else if(type.startsWith("fortsiege"))
				{
					FortSiegeManager.getInstance().reload();
					activeChar.sendMessage("Castle/Fortress Siege config reloaded");
				}
				else
				{
					activeChar.sendMessage("Usage:  //reload <multisell|skill|npc|htm|item|instancemanager|teleport|tradelist|zone|mapregion|npcwalkers|siege|fortsiege>");
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage:  //reload <multisell|skill|npc|htm|item|instancemanager|teleport|tradelist|zone|mapregion|siege|fortsiege>");
			}
		}

		else if(command.startsWith("admin_set"))
		{
			StringTokenizer st = new StringTokenizer(command);
			String[] cmd = st.nextToken().split("_");
			try
			{
				String[] parameter = st.nextToken().split("=");
				String pName = parameter[0].trim();
				String pValue = parameter[1].trim();
				if (Config.setParameterValue(pName, pValue))
					activeChar.sendMessage("parameter "+pName+" succesfully set to "+pValue);
				else 
					activeChar.sendMessage("Invalid parameter!");
			}
			catch(Exception e)
			{
				if (cmd.length==2)
					activeChar.sendMessage("Usage: //set parameter=value");
			}
			finally
			{
				if (cmd.length==3)
				{
					if (cmd[2].equalsIgnoreCase("menu"))
						AdminHelpPage.showHelpPage(activeChar, "settings.htm");
					else if (cmd[2].equalsIgnoreCase("mod"))
						AdminHelpPage.showHelpPage(activeChar, "mods_menu.htm");
				}
			}
		}
		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level) 
	{
		return (level >= REQUIRED_LEVEL);
	}
	

	public void showMainPage(L2PcInstance activeChar, String command)
	{
		int mode = 0;
		String filename=null;
		try
		{
			mode = Integer.parseInt(command.substring(11));
		}
		catch (Exception e) {}
		switch (mode)
		{
		case 1:
			filename="main";
			break;
		case 2:
			filename="game";
			break;
		case 3:
			filename="effects";
			break;
		case 4:
			filename="server";
			break;
		case 5:
			filename="mods";
			break;
		default:
			if (Config.GM_ADMIN_MENU_STYLE.equals("modern"))
				filename="main";
			else
				filename="classic";
			break;
		}
		AdminHelpPage.showHelpPage(activeChar, filename+"_menu.htm");
	}
	
	public void ShowConfigPage2(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<center><table width=270><tr><td width=60><button value=\"Admin\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=150><font color=\"LEVEL\">Config Server Panel</font></td><td width=60><button value=\"Panel1\" action=\"bypass -h admin_config_server\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></center><br>");
		replyMSG.append("<center><table width=260>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Show GM Login</font> = " + Config.SHOW_GM_LOGIN + "</td><td></td><td><button value=\""+ !Config.SHOW_GM_LOGIN +"\" action=\"bypass -h admin_set ShowGMLogin " + !Config.SHOW_GM_LOGIN + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Hide GM Status</font> = " + Config.HIDE_GM_STATUS + "</td><td></td><td><button value=\""+ !Config.HIDE_GM_STATUS +"\" action=\"bypass -h admin_set HideGMStatus " + !Config.HIDE_GM_STATUS + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Spawn Siege Guard</font> = " + Config.SPAWN_SIEGE_GUARD + "</td><td></td><td><button value=\""+ !Config.SPAWN_SIEGE_GUARD +"\" action=\"bypass -h admin_set SpawnSiegeGuard " + !Config.SPAWN_SIEGE_GUARD + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Auto Loot</font> = " + Config.AUTO_LOOT + "</td><td></td><td><button value=\""+ !Config.AUTO_LOOT +"\" action=\"bypass -h admin_set AutoLoot " + !Config.AUTO_LOOT + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Magic Failures</font> = " + Config.ALT_GAME_MAGICFAILURES + "</td><td></td><td><button value=\""+ !Config.ALT_GAME_MAGICFAILURES +"\" action=\"bypass -h admin_set MagicFailures " + !Config.ALT_GAME_MAGICFAILURES + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Book Needed</font> = " + Config.SP_BOOK_NEEDED + "</td><td></td><td><button value=\""+ !Config.SP_BOOK_NEEDED +"\" action=\"bypass -h admin_set SpBookNeeded " + !Config.SP_BOOK_NEEDED + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Learn Other Skill</font> = " + Config.ALT_GAME_SKILL_LEARN + "</td><td></td><td><button value=\""+ !Config.ALT_GAME_SKILL_LEARN +"\" action=\"bypass -h admin_set AltGameSkillLearn " + !Config.ALT_GAME_SKILL_LEARN + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Shield Block From All</font> = " + Config.ALT_GAME_SHIELD_BLOCKS + "</td><td></td><td><button value=\""+ !Config.ALT_GAME_SHIELD_BLOCKS +"\" action=\"bypass -h admin_set AltShieldBlocks " + !Config.ALT_GAME_SHIELD_BLOCKS + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Free Teleport</font> = " + Config.ALT_GAME_FREE_TELEPORT + "</td><td></td><td><button value=\""+ !Config.ALT_GAME_FREE_TELEPORT +"\" action=\"bypass -h admin_set AltFreeTeleporting " + !Config.ALT_GAME_FREE_TELEPORT + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Can Discard Items</font> = " + Config.ALLOW_DISCARDITEM + "</td><td></td><td><button value=\""+ !Config.ALLOW_DISCARDITEM +"\" action=\"bypass -h admin_set AllowDiscardItem " + !Config.ALLOW_DISCARDITEM + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Global Chat</font> = " + Config.DEFAULT_GLOBAL_CHAT + "</td><td><edit var=\"menu_command1\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set GlobalChat $menu_command1\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Coord Synchronize</font> = " + Config.COORD_SYNCHRONIZE + "</td><td><edit var=\"menu_command2\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set CoordSynchronize $menu_command2\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("</table></body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply); 
	}
	
	public void ShowConfigPage(L2PcInstance activeChar)
	{
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		TextBuilder replyMSG = new TextBuilder("<html><body>"); 
		replyMSG.append("<center><table width=270><tr><td width=60><button value=\"Admin\" action=\"bypass -h admin_admin\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=150><font color=\"LEVEL\">Config Server Panel</font></td><td width=60><button value=\"Panel2\" action=\"bypass -h admin_config_server2\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></center><br>");
		replyMSG.append("<center><table width=260>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Auto loot</font> = " + Config.AUTO_LOOT + "</td><td></td><td><button value=\""+ !Config.AUTO_LOOT +"\" action=\"bypass -h admin_set AutoLoot " + !Config.AUTO_LOOT + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Allow Discard Item</font> = " + Config.ALLOW_DISCARDITEM + "</td><td></td><td><button value=\""+ !Config.ALLOW_DISCARDITEM +"\" action=\"bypass -h admin_set AllowDiscardItem " + !Config.ALLOW_DISCARDITEM + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Deep Blue Mobs Drop Rule</font> = " + Config.DEEPBLUE_DROP_RULES + "</td><td></td><td><button value=\""+ !Config.DEEPBLUE_DROP_RULES +"\" action=\"bypass -h admin_set UseDeepBlueDropRules " + !Config.DEEPBLUE_DROP_RULES + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Auto Destroy Item aft. sec.</font> = " + Config.AUTODESTROY_ITEM_AFTER + "</td><td><edit var=\"menu_command\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set AutoDestroyDroppedItemAfter $menu_command\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate EXP</font> = " + Config.RATE_XP + "</td><td><edit var=\"menu_command1\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateXP $menu_command1\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate SP</font> = " + Config.RATE_SP + "</td><td><edit var=\"menu_command2\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateSP $menu_command2\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Drop Adena</font> = " + Config.RATE_DROP_ADENA + "</td><td><edit var=\"menu_command3\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateDropAdena $menu_command3\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Drop Items</font> = " + Config.RATE_DROP_ITEMS + "</td><td><edit var=\"menu_command4\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateDropItems $menu_command4\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Drop Spoil</font> = " + Config.RATE_DROP_SPOIL + "</td><td><edit var=\"menu_command5\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateDropSpoil $menu_command5\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Quest Reward</font> = " + Config.RATE_QUESTS_REWARD + "</td><td><edit var=\"menu_command6\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RateQuestsReward $menu_command6\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Raid Regen HP</font> = " + Config.RAID_HP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command7\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RaidHpRegenMultiplier $menu_command7\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Raid Regen MP</font> = " + Config.RAID_MP_REGEN_MULTIPLIER + "</td><td><edit var=\"menu_command8\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RaidMpRegenMultiplier $menu_command8\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Raid PDefence</font> = " + Config.RAID_PDEFENCE_MULTIPLIER + "</td><td><edit var=\"menu_command9\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RaidPDefenceMultiplier $menu_command9\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Rate Raid MDefence</font> = " + Config.RAID_MDEFENCE_MULTIPLIER + "</td><td><edit var=\"menu_command10\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set RaidMDefenceMultiplier $menu_command10\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Alt Buff Time</font> = " + Config.ALT_BUFF_TIME + "</td><td><edit var=\"menu_command11\" width=40 height=15></td><td><button value=\"Set\" action=\"bypass -h admin_set AltBuffTime $menu_command11\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">Alt game creation</font> = " + Config.ALT_GAME_CREATION + "</td><td></td><td><button value=\""+ !Config.ALT_GAME_CREATION +"\" action=\"bypass -h admin_set AltGameCreation " + !Config.ALT_GAME_CREATION + "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("</table></body></html>");

		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply); 
	}
	
	
	//[L2J_JP_ADD]
	public void adminSummon(L2PcInstance activeChar, int npcId)
	{
		if (activeChar.getPet() != null)
		{
			activeChar.getPet().unSummon(activeChar);
		}
		
		L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(npcId);
		L2SummonInstance summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, null);
		
		summon.setTitle(activeChar.getName());
		summon.setExpPenalty(0);
		if (summon.getLevel() >= Experience.LEVEL.length)
		{
			summon.getStat().setExp(Experience.LEVEL[Experience.LEVEL.length - 1]);
		}
		else
		{
			summon.getStat().setExp(Experience.LEVEL[(summon.getLevel() % Experience.LEVEL.length)]);
		}
		summon.getStat().setExp(0);
		summon.getStatus().setCurrentHp(summon.getMaxHp());
		summon.getStatus().setCurrentMp(summon.getMaxMp());
		summon.setHeading(activeChar.getHeading());
		summon.setRunning();
		activeChar.setPet(summon);
		
		L2World.getInstance().storeObject(summon);
		summon.spawnMe(activeChar.getX()+50, activeChar.getY()+100, activeChar.getZ());
			
		summon.setFollowStatus(true);
		summon.setShowSummonAnimation(false); // addVisibleObject created the info packets with summon animation
											  // if someone comes into range now, the animation shouldnt show any more
		activeChar.sendPacket(new PetInfo(summon));
	}
}
