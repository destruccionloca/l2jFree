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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import javolution.text.TextBuilder;
import javolution.util.FastList;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AdminSpawn implements IAdminCommandHandler
{
  
    private static final String[][] ADMIN_COMMANDS = {
    	{"admin_spawn_menu",                                       // show spawn menu
    		
    		"Admin Menu - Spawn NPC.",
    	}, 				                       
    	{"admin_spawnsearch_menu",                                 // show page with NPC search results
    		
    		"Admin Menu - NPC search results.",
    		"Usage: spawnsearch_menu level|name|part string <page>",
    		"Options:",
    		"level - list NPC's by level <string>",
    		"name - list NPC's witch name started with <string>",
    		"part - list NPC's where <string> is part of name",
    		"string - part of name or monster level",
    		"<page> - page number with results search"
     	},
     	{"admin_spawndelay",									   // respawn delay for spawn commands
			
			"Set or show default respawn delay for newly spawned NPC.",
			"Usage spawndelay <sec>",
			"Options:",
			"<sec> - set default respawn time in seconds"
	    },
     	{"admin_delay",									   // respawn delay for spawn commands
			
			"Set respawn delay for targeted NPC and save in DB.",
			"Usage delay <sec>",
			"Options:",
			"<sec> - set respawn time in seconds"
	    },
     	{"admin_spawnlist",									       // get list of NPC spawns
			
			"Show list of regular spawns of NPC.",
			"Usage: spawnlist id|name",
			"Options:",
			"id - NPC template ID",
			"name - NPC name (use underscope to separate words in npc name)"
	    },
     	{"admin_spawnlist_menu",								   // show list of NPC spawns
			
			"Admin Menu - Show spawns of NPC.",
			"Usage: spawnlist id|name",
			"Options:",
			"id - NPC template ID",
			"name - NPC name (use underscope to separate words in npc name)"
	    },
     	{"admin_spawn",										       // spawn NPC and save to DB's default
    				
    		"Spawn NPC and store in DB.",
    		"Usage: spawn id|name <num> <radius>",
    		"Options:",
    		"id - NPC template ID",
    		"name - NPC name (use underscope to separate words in npc name)",
    		"<num> - NPC amount to spawn, Default: 1",
    		"<radius> - radius for NPC spawns, Default: 300"
    	},                                       
    	{"admin_cspawn",                                           // spawn NPC and save to DB's custom table
				
    		"Spawn NPC and store in DB in custom table.",
    		"Usage: cspawn id|name <num> <radius>",
			"Options:",
			"id - NPC template ID",
			"name - NPC name (use underscope to separate words in npc name)",
			"<num> - NPC amount to spawn, Default: 1",
			"<radius> - radius for NPC spawns, Default: 300"
    	},
    	{"admin_otspawn", 					                       // spawn NPC but do not store spawn in DB
			
			"Spawn NPC and do not store in DB.",
			"Usage: otspawn id|name <num> <radius>",
			"Options:",
			"id - NPC template ID",
			"name - NPC name (use underscope to separate words in npc name)",
			"<num> - NPC amount to spawn, Default: 1",
			"<radius> - radius for NPC spawns, Default: 300"
	    },
    	{"admin_spawn_once", 					                   // spawn NPC, don't store, don't respawn
			
			"Spawn NPC and don't respawn after death.",
			"Usage: spawn_once id|name <num> <radius>",
			"Options:",
			"id - NPC template ID",
			"name - NPC name (use underscope to separate words in npc name)",
			"<num> - NPC amount to spawn, Default: 1",
			"<radius> - radius for NPC spawns, Default: 300"
	    },
    	{"admin_unspawnall",                                       // delete all spawned NPC's
	    		
	    	"Delete all spawned NPC's.",
			"Usage: unspawnall",
    	},                                   
    	{"admin_respawnall",                                       // delete all spawned NPC's then respawn again
    		
    		"Delete all spawned NPC's and respawn again.",
			"Usage: respawnall",
    	},                                  
    	{"admin_spawnnight",                                       // spawn night creatures
    		
		    "Spawn night creatures.",
			"Usage: spawnnight",
    	},					               
    	{"admin_spawnday",                                         // spawn day creatures
    		
	        "Spawn day creatures.",
			"Usage: spawnday",
    	}};	                               
    
    public static Log _log = LogFactory.getLog(AdminSpawn.class.getName());

    private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;
    private static final int REQUIRED_LEVEL2 = Config.GM_NPC_VIEW;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        	if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
        		return false;

        StringTokenizer st = new StringTokenizer(command, " ");
        
        String cmd = st.nextToken();  // get command
        
		if (cmd.equals("admin_show_spawns"))
			AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
		else if (cmd.startsWith("admin_spawn_index"))
		{
			try
			{
				int level = Integer.parseInt(st.nextToken());
				int from = 0;
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch (NoSuchElementException nsee) {}
				showMonsters(activeChar, level, from);
			}
			catch (Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		else if (cmd.equals("admin_show_npcs"))
			AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
		else if (cmd.startsWith("admin_npc_index"))
		{
			try
			{
				String letter = st.nextToken();
				int from = 0;
				try
				{
					from = Integer.parseInt(st.nextToken());
				} 
				catch (NoSuchElementException nsee) {}
				showNpcs(activeChar, letter, from);
			}
			catch (Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
			}
		}
		else if (cmd.startsWith("admin_unspawnall"))
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
				player.sendPacket(new SystemMessage(SystemMessageId.NPC_SERVER_NOT_OPERATING));
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			L2World.getInstance().deleteVisibleNpcSpawns();
			GmListTable.broadcastMessageToGMs("NPC Unspawn completed!");
		}
		else if (cmd.startsWith("admin_spawnday"))
			DayNightSpawnManager.getInstance().spawnDayCreatures();
		else if (cmd.startsWith("admin_spawnnight"))
			DayNightSpawnManager.getInstance().spawnNightCreatures();
		else if (cmd.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload"))
		{
			// make sure all spawns are deleted
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			L2World.getInstance().deleteVisibleNpcSpawns();
			// now respawn all
			NpcTable.getInstance().reloadAll();
			SpawnTable.getInstance().reloadAll();
			RaidBossSpawnManager.getInstance().reloadBosses();
			GmListTable.broadcastMessageToGMs("NPC Respawn completed!");
		}
		else if (cmd.startsWith("admin_teleport_reload"))
		{
			TeleportLocationTable.getInstance().reloadAll();
			GmListTable.broadcastMessageToGMs("Teleport List Table reloaded.");
		}
        else if (cmd.equals("admin_spawndelay"))
        {
        	int delay = 0;
        	if (st.hasMoreTokens())
        	{
        		try
            	{ 
            		delay = Integer.parseInt(st.nextToken());
            		Config.STANDARD_RESPAWN_DELAY = delay;
            	}
            	catch (Exception e)
            	{
            		showAdminCommandHelp(activeChar,cmd);
            	}
        	}	
        	activeChar.sendMessage("Current default respawn delay is "+ Config.STANDARD_RESPAWN_DELAY + " seconds.");
        }
        else if (cmd.equals("admin_delay"))
        {
        	int delay = 0;
        	L2NpcInstance target = null;
        	
        	if (activeChar.getTarget() instanceof L2NpcInstance)
        		target = ((L2NpcInstance)activeChar.getTarget());
        	
        	if (st.hasMoreTokens() && target != null)
        	{
        		try
            	{ 
            		delay = Integer.parseInt(st.nextToken());
            		
            		L2Spawn spawn = target.getSpawn();
            		
            		if (spawn.IsRespawnable())
            		{
            			SpawnTable.getInstance().deleteSpawn(spawn, true);
            			target.deleteMe();
            			spawn.setRespawnDelay(delay);
            			SpawnTable.getInstance().addNewSpawn(spawn, true);
            			target.setSpawn(spawn);
            			target.spawnMe();
            			activeChar.sendMessage("Respawn delay  for "+target.getName()+" changed to "+delay+" seconds.");
            		}
            		else
            			activeChar.sendMessage("Respawn delay  for "+target.getName()+" cant be changed.");
            	}
            	catch (Exception e)
            	{
            		showAdminCommandHelp(activeChar,cmd);
            	}
        	}
        	else
        		showAdminCommandHelp(activeChar,cmd);
        }        
        else if (cmd.equals("admin_spawn") || cmd.equals("admin_cspawn") || cmd.equals("admin_otspawn") || cmd.equals("admin_spawn_once"))
        {
			boolean custom = cmd.equals("admin_cspawn");
			boolean respawn = !cmd.equals("admin_spawn_once");
			boolean storeInDb = !cmd.equals("admin_otspawn") && respawn;
			
			
        	int npcId = 0;
        	String npcName = "";
        	int count = 1;
        	int radius = 300;

            try
            {
            	npcName = st.nextToken();
            	
            	try
                {
            		npcId =  Integer.parseInt(npcName);
                }
            	catch (NumberFormatException  e)
                {}
            	
                if (st.hasMoreTokens())
                    count = Integer.parseInt(st.nextToken());
                if (st.hasMoreTokens())
                    radius = Integer.parseInt(st.nextToken());
                
                if (npcId > 0)
                	spawnNpc(activeChar, npcId, count, radius, storeInDb, respawn, custom);
                else
                	if (npcName.length() > 0)
                		spawnNpc(activeChar, npcName, count, radius, storeInDb, respawn, custom);
                	else
                		showAdminCommandHelp(activeChar,cmd);
            }
            catch (Exception e)
            {
            	showAdminCommandHelp(activeChar,cmd);
            }
        }
        else if (cmd.equals("admin_unspawnall"))
        {
            for (L2PcInstance player : L2World.getInstance().getAllPlayers())
            {
                player.sendPacket(new SystemMessage(SystemMessageId.NPC_SERVER_NOT_OPERATING));
            }

            RaidBossSpawnManager.getInstance().cleanUp();
            DayNightSpawnManager.getInstance().cleanUp();
            L2World.getInstance().deleteVisibleNpcSpawns();
            activeChar.sendMessage("All NPCs unspawned.");
        }
        else if (cmd.equals("admin_spawnday"))
        {
            DayNightSpawnManager.getInstance().spawnDayCreatures();
            activeChar.sendMessage("All daylight NPCs spawned.");
        }
        else if (cmd.equals("admin_spawnnight"))
        {
            DayNightSpawnManager.getInstance().spawnNightCreatures();
            activeChar.sendMessage("All nightly NPCs spawned.");
        }
        else if (cmd.equals("admin_respawnall"))
        {
        	activeChar.sendMessage("NPCs respawn sequence initiated.");
            RaidBossSpawnManager.getInstance().cleanUp();
            DayNightSpawnManager.getInstance().cleanUp();
            L2World.getInstance().deleteVisibleNpcSpawns();
            NpcTable.getInstance().cleanUp();
            
            NpcTable.getInstance().reloadAll();
            SpawnTable.getInstance().reloadAll();
            RaidBossSpawnManager.getInstance().reloadBosses();
            activeChar.sendMessage("NPCs respawn sequence complete.");
        } 
        return true;
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
     */
    public String[] getAdminCommandList()
    {
    	String[] _adminCommandsOnly = new String[ADMIN_COMMANDS.length];
    	for (int i=0; i < ADMIN_COMMANDS.length; i++)
    	{
    		_adminCommandsOnly[i] = ADMIN_COMMANDS[i][0];
    	}
    	
        return _adminCommandsOnly;
    }

    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }
    private boolean checkLevel2(int level)
    {
        return (level >= REQUIRED_LEVEL2);
    }
    /**
     * Spawn NPC. 
     * @param npcId id of NPC Template
     * @param count count of NPCs to spawn 
     * @param radius radius of spawn
     * @param respawn if false spawn only once
     * @param custom if true then spawn will be custom
     */
    private void spawnNpc(L2PcInstance activeChar, int npcId, int count, int radius, boolean saveInDb, boolean respawn, boolean custom)
    {
        L2Object target = activeChar.getTarget();
        if (target == null)
            target = activeChar;

        if (!checkLevel(activeChar.getAccessLevel()))
        	return;
       
        L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
        
        if (template == null)
        {
        	activeChar.sendMessage("NPC template ID " +npcId + " not found.");
    	    return;
        }
       
        try
        {
        	for (int i=0; i < count; i++)
        	{
        		int x = target.getX();
        		int y = target.getY();
        		int z = target.getZ();
        		int heading = activeChar.getHeading();
        		
        		if (radius  >0 && count >1)
        		{
                    int signX = (Rnd.nextInt(2) == 0) ? -1 : 1;
                    int signY = (Rnd.nextInt(2) == 0) ? -1 : 1;
                    int randX = Rnd.nextInt(radius);
                    int randY = Rnd.nextInt(radius);
                    int randH = Rnd.nextInt(0xFFFF);
                    
                    x = x + signX * randX;
                    y = y + signY * randY;
                    heading = randH;
        		}
            
        		L2Spawn spawn = new L2Spawn(template);
            
        		if (custom) spawn.setCustom();
            
        		spawn.setLocx(x);
        		spawn.setLocy(y);
        		spawn.setLocz(z);
        		spawn.setAmount(1);
        		spawn.setHeading(heading);
                spawn.setRespawnDelay(Config.STANDARD_RESPAWN_DELAY);

                if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()) && 
                		respawn && !Config.ALT_DEV_NO_SPAWNS)
            	    activeChar.sendMessage("You cannot spawn another instance of " + template.getName() + ".");
                else
                {
                	if(respawn==true && Config.ALT_DEV_NO_SPAWNS==false)
                	{
                		if (RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcId()) != null)
                			RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template.getBaseHpMax(), template.getBaseMpMax(), true);
						else
							SpawnTable.getInstance().addNewSpawn(spawn, saveInDb);
                	}
                	else
                		spawn.spawnOne();
					spawn.init();
					if(!respawn)
						spawn.stopRespawn();
					activeChar.sendMessage("Created " + template.getName() + " on " + target.getObjectId() + ".");
                }
            } 
        }
        catch (Exception e)
        {
        }
    }

    /**
     * Spawn NPC. 
     * @param npcName name of NPC
     * @param count count of NPCs to spawn 
     * @param radius radius of spawn
     * @param respawn if false spawn only once
     * @param custom if true then spawn will be custom
     */
    private void spawnNpc(L2PcInstance activeChar, String npcName, int count, int radius, boolean saveInDb, boolean respawn, boolean custom)
    {
		L2NpcTemplate template = NpcTable.getInstance().getTemplateByName(npcName);
    	if (template != null)
        	spawnNpc(activeChar, template.getNpcId(), count, radius, saveInDb, respawn, custom);
        else
        	activeChar.sendMessage("NPC template with name " +npcName + " not found.");
    }

	private void showMonsters(L2PcInstance activeChar, int level, int from)
	{
		TextBuilder tb = new TextBuilder();

		L2NpcTemplate[] mobs = NpcTable.getInstance().getAllMonstersOfLevel(level);

		// Start
		tb.append("<html><title>Spawn Monster:</title><body><p> Level "+level+":<br>Total Npc's : "+mobs.length+"<br>");
		String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index "+level+" $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";

		// Loop
		boolean ended = true;
		for (int i=from; i<mobs.length; i++)
		{
			String txt = "<a action=\"bypass -h admin_spawn_monster "+mobs[i].getNpcId()+"\">"+mobs[i].getName()+"</a><br1>";

			if ((tb.length() + txt.length() + end2.length()) > 8192)
			{
				end1 = end1.replace("$from$", ""+i);
				ended = false;
				break;
			}

			tb.append(txt);
		}

		// End
		if (ended)
			tb.append(end2);
		else
			tb.append(end1);

		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));
	}

	private void showNpcs(L2PcInstance activeChar, String starting, int from)
	{
		TextBuilder tb = new TextBuilder();
		L2NpcTemplate[] mobs = NpcTable.getInstance().getAllNpcStartingWith(starting);
		// Start
		tb.append("<html><title>Spawn Monster:</title><body><p> There are "+mobs.length+" Npcs whose name starts with "+starting+":<br>");
		String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index "+starting+" $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		// Loop
		boolean ended = true;
		for (int i = from; i < mobs.length; i++)
		{
			String txt = "<a action=\"bypass -h admin_spawn_monster "+mobs[i].getNpcId()+"\">"+mobs[i].getName()+"</a><br1>";

			if ((tb.length() + txt.length() + end2.length()) > 8192)
			{
				end1 = end1.replace("$from$", ""+i);
				ended = false;
				break;
			}
			tb.append(txt);
		}
		// End
		if (ended)
			tb.append(end2);
		else
			tb.append(end1);
		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));
	}
    
    /**
     * Show tips about command usage and syntax. 
     * @param command admin command name
     */    
    private void showAdminCommandHelp(L2PcInstance activeChar, String command)
    {
    	for (int i=0; i < ADMIN_COMMANDS.length; i++)
    	{
    		if (command.equals(ADMIN_COMMANDS[i][0]))
    		{
    			for (int k=1; k < ADMIN_COMMANDS[i].length; k++)
    				activeChar.sendMessage(ADMIN_COMMANDS[i][k]);
    		}
    	}
    }
}