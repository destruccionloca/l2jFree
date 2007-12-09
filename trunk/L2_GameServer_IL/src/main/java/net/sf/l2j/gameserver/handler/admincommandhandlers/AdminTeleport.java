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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2CharPosition;
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

/**
 * This class handles following admin commands:
 * - show_moves
 * - show_teleport
 * - teleport_to_character
 * - move_to
 * - teleport_character
 * 
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminTeleport implements IAdminCommandHandler 
{
	private static final Log _log = LogFactory.getLog(AdminTeleport.class.getName());
	
	private static final String[] ADMIN_COMMANDS = {
		"admin_bookmark", // L2JP_JP ADD
		"admin_show_moves",
		"admin_show_moves_other",
		"admin_show_teleport",
		"admin_teleport_to_character",
		"admin_teleportto",
		"admin_move_to",
		"admin_teleport_character",
		"admin_recall",
		"admin_recall_gm", //[L2J_JP ADD - TSL]
		"admin_recall_party", //[L2J_JP ADD - TSL]
		"admin_recall_all", //[L2J_JP ADD - TSL]
		"admin_recall_offline",
		"admin_walk",
		"admin_recall_npc",
		"admin_go",
		"admin_tele",
		"admin_teleto"
		};
	private static final int REQUIRED_LEVEL = Config.GM_TELEPORT;
	private static final int REQUIRED_LEVEL2 = Config.GM_TELEPORT_OTHER;
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
		if (command.startsWith("admin_bookmark"))// L2J_JP ADD
		{
			bookmark(activeChar, command.substring(15));
		}
		else if (command.equals("admin_teleto"))
		{
			activeChar.setTeleMode(1);
		}
		else if (command.startsWith("admin_recall_offline "))
		{
			try{
				String[] param = command.split(" ");
				if (param.length<2){
					activeChar.sendMessage("Wrong usage: //recall_offline <player name>");
					return false;
				}
				changeCharacterPosition(activeChar, param[1]);
			}
			catch(Throwable t){			
			}
		}
		else if (command.equals("admin_teleto r"))
		{
			activeChar.setTeleMode(2);
		}
		else if (command.equals("admin_teleto end"))
		{
			activeChar.setTeleMode(0);
		}
		else if (command.equals("admin_show_moves"))
		{
			AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
		}
		else if (command.equals("admin_show_moves_other"))
		{
			AdminHelpPage.showHelpPage(activeChar, "tele/other.html");
		}
		else if (command.equals("admin_show_teleport"))
		{
			showTeleportCharWindow(activeChar);
		}
		else if (command.equals("admin_recall_npc"))
		{
			recallNPC(activeChar);
		}
		else if (command.equals("admin_teleport_to_character"))
		{
			teleportToCharacter(activeChar, activeChar.getTarget());
		}
		else if (command.startsWith("admin_walk"))
		{
			try
			{
				String val = command.substring(11);
				StringTokenizer st = new StringTokenizer(val);
				String x1 = st.nextToken();
				int x = Integer.parseInt(x1);
				String y1 = st.nextToken();
				int y = Integer.parseInt(y1);
				String z1 = st.nextToken();
				int z = Integer.parseInt(z1);
				L2CharPosition pos = new L2CharPosition(x,y,z,0);
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,pos);
			}
			catch (Exception e)
			{
				if (_log.isDebugEnabled()) _log.info("admin_walk: "+e);
			}
		}
		else if (command.startsWith("admin_move_to"))
		{
			try
			{
				String val = command.substring(14);
				teleportTo(activeChar, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				//Case of empty or missing coordinates
				AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
			}		
		}
		else if (command.startsWith("admin_teleport_character"))
		{
			try
			{
				String val = command.substring(25); 
				
				if (activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
					teleportCharacter(activeChar, val);
			}
			catch (StringIndexOutOfBoundsException e)
			{
				//Case of empty coordinates
				activeChar.sendMessage("Wrong or no Coordinates given.");
				showTeleportCharWindow(activeChar); //back to character teleport
			}
		}
		else if (command.startsWith("admin_teleportto "))
		{
			try
			{
				String targetName = command.substring(17);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				teleportToCharacter(activeChar, player);
			}
			catch (StringIndexOutOfBoundsException e)
			{ }
		}
		else if (command.startsWith("admin_recall "))
		{
			try
			{
				String targetName = command.substring(13);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				if (activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
					teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
			catch (StringIndexOutOfBoundsException e)
			{ }
		}
		// [L2J_JP ADD START - TSL]
		else if (command.startsWith("admin_recall_party "))
		{
			try
			{
				String targetName = command.substring(16);
				L2PcInstance player = L2World.getInstance().getPlayer(targetName);
				if (activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
				{
					if (player.getParty() != null)
					{
						for (L2PcInstance character : player.getParty().getPartyMembers())
						{
							if (character == activeChar) continue;
							teleportCharacter(character, activeChar.getX(), activeChar.getY(), activeChar.getZ());
						}
					}
					else
						activeChar.sendMessage("Wrong or Player is not in party.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{ }
		}
		else if (command.startsWith("admin_recall_gm ")){
			try{
				for (L2PcInstance player : L2World.getInstance().getAllPlayers())
					if (player.getAccessLevel()>0)
						teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ());
			}
			catch (StringIndexOutOfBoundsException e)
			{ }
		}
		else if (command.equals("admin_recall_all"))
		{
			if (activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
			{
				for (L2PcInstance character : L2World.getInstance().getAllPlayers())
				{
					if (character == activeChar) continue;
					teleportCharacter(character, activeChar.getX(), activeChar.getY(), activeChar.getZ());
				}
			}
		}
		// [L2J_JP ADD END - TSL]
		else if (command.equals("admin_tele"))
		{
			showTeleportWindow(activeChar);
		}
		else if (command.startsWith("admin_go"))
		{
			int intVal=150;
			int x = activeChar.getX(),y = activeChar.getY(),z = activeChar.getZ();
			try
			{
				String val = command.substring(8);
				StringTokenizer st = new StringTokenizer(val);
				String dir=st.nextToken();
				if (st.hasMoreTokens())
					intVal = Integer.parseInt(st.nextToken());
				if (dir.equals("east"))
					x+=intVal;
				else if (dir.equals("west"))
					x-=intVal;
				else if (dir.equals("north"))
					y-=intVal;
				else if (dir.equals("south"))
					y+=intVal;
				else if (dir.equals("up"))
					z+=intVal;
				else if (dir.equals("down"))
					z-=intVal;
				activeChar.teleToLocation(x, y, z, false);
				showTeleportWindow(activeChar);
			}
			catch (Exception e) 
			{
				activeChar.sendMessage("Usage: //go<north|south|east|west|up|down> [offset] (default 150)");
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

	// L2J_JP ADD
	private void bookmark(L2PcInstance activeChar, String Name)
	{
		File file = new File(Config.DATAPACK_ROOT+"/"+"data/html/admin/tele/bookmark.txt");
		LineNumberReader lnr = null;
		String bookmarks = "";
		try
		{
			int i=0;
			String line = null;
			lnr = new LineNumberReader(new FileReader(file));
			while ( (line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line,"\r\n");
				if (st.hasMoreTokens())
				{   
					bookmarks += st.nextToken();
					i++;
				}
			}
			if(Name.equals("show"))
			{
				FileInputStream fis = null;
				fis = new FileInputStream(new File(Config.DATAPACK_ROOT+"/"+"data/html/admin/tele/bookmarks.htm"));
				byte[] raw = new byte[fis.available()];
				fis.read(raw);
				String content = new String(raw, "UTF-8");
				content = content.replaceAll("%bookmarks%", bookmarks);
				NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
				adminReply.setHtml(content);
				activeChar.sendPacket(adminReply);
				fis.close();
			}
			else
			{
				FileWriter save = new FileWriter(file);
				bookmarks += "<tr><td width=\"270\"><a action=\"bypass -h admin_move_to "+activeChar.getX()+" "+activeChar.getY()+" "+activeChar.getZ()+"\">"+Name+"</a></td></tr>\r\n";
				save.write(bookmarks);
				save.close();
				bookmark(activeChar, "show");
			}
		}
		catch (FileNotFoundException e)
		{
			activeChar.sendMessage("bookmark.txt not found");
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch (Exception e2)
			{}
		}
	}
	
	private void teleportTo(L2PcInstance activeChar, String Cords)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(Cords);
			String x1 = st.nextToken();
			int x = Integer.parseInt(x1);
			String y1 = st.nextToken();
			int y = Integer.parseInt(y1);
			String z1 = st.nextToken();
			int z = Integer.parseInt(z1);
			
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z, false);
			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("You have been teleported to " + Cords);
			activeChar.sendPacket(sm);
		}
		catch (NoSuchElementException nsee)
		{
			activeChar.sendMessage("Wrong or no Coordinates given.");
		}
	}
	
	private void showTeleportWindow(L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "move.htm");
	}
	
	
	private void showTeleportCharWindow(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) 
		{
			player = (L2PcInstance)target;
		} 
		else 
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5); 
		
		TextBuilder replyMSG = new TextBuilder("<html><title>Teleport Character</title>");
		replyMSG.append("<body>");
		replyMSG.append("The character you will teleport is " + player.getName() + ".");
		replyMSG.append("<br>");
		
		replyMSG.append("Coordinate x");
		replyMSG.append("<edit var=\"char_cord_x\" width=110>");
		replyMSG.append("Coordinate y");
		replyMSG.append("<edit var=\"char_cord_y\" width=110>");
		replyMSG.append("Coordinate z");
		replyMSG.append("<edit var=\"char_cord_z\" width=110>");
		replyMSG.append("<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");	
		replyMSG.append("<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character " + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + "\" width=115 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);			
	}
	
	private void teleportCharacter(L2PcInstance activeChar , String Cords)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) 
		{
			player = (L2PcInstance)target;
		} 
		else 
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		}
		else
		{
			try
			{
				StringTokenizer st = new StringTokenizer(Cords);
				String x1 = st.nextToken();
				int x = Integer.parseInt(x1);
				String y1 = st.nextToken();
				int y = Integer.parseInt(y1);
				String z1 = st.nextToken();
				int z = Integer.parseInt(z1);
				teleportCharacter(player, x,y,z);
			}
			catch (NoSuchElementException nsee){}
		}
	}
	
	/**
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 */
	private void teleportCharacter(L2PcInstance player, int x, int y, int z)
	{
		if (player != null)
		{
			player.sendMessage("Admin is teleporting you.");
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.teleToLocation(x, y, z);
		}
	}
	
	private void teleportToCharacter(L2PcInstance activeChar, L2Object target)
	{
		L2PcInstance player = null;
		if (target != null && target instanceof L2PcInstance) 
		{
			player = (L2PcInstance)target;
		} 
		else 
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{	
			player.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_ON_YOURSELF));
		}
		else
		{
			int x = player.getX();
			int y = player.getY();
			int z = player.getZ();
			
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z);
			
			activeChar.sendMessage("You have teleported to character " + player.getName() + ".");
		}
	}
	private void recallNPC(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if ((obj != null) && (obj instanceof L2NpcInstance))
		{
			L2NpcInstance target = (L2NpcInstance) obj;

			int monsterTemplate = target.getTemplate().getNpcId();
			L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
			if (template1 == null)
			{
				activeChar.sendMessage("Incorrect monster template.");
				_log.warn("ERROR: NPC " + target.getObjectId() + " has a 'null' template.");
				return;
			}

			L2Spawn spawn = target.getSpawn();
			
			if (spawn == null)
			{
				activeChar.sendMessage("Incorrect monster spawn.");
				_log.warn("ERROR: NPC " + target.getObjectId() + " has a 'null' spawn.");
				 return;
			}
			
			target.decayMe();
			spawn.setLocx(activeChar.getX());
			spawn.setLocy(activeChar.getY());
			spawn.setLocz(activeChar.getZ());
			spawn.setHeading(activeChar.getHeading());
			spawn.respawnNpc(target);
			SpawnTable.getInstance().updateSpawn(spawn);
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
		}
	}
	
	private void changeCharacterPosition(L2PcInstance activeChar, String name){
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=? WHERE char_name=?");
			statement.setInt(1, activeChar.getX());
			statement.setString(2, name);
			statement.execute();
			statement = con.prepareStatement("UPDATE characters SET y=? WHERE char_name=?");
			statement.setInt(1, activeChar.getY());
			statement.setString(2, name);
			statement.execute();
			statement = con.prepareStatement("UPDATE characters SET z=? WHERE char_name=?");
			statement.setInt(1, activeChar.getZ());
			statement.setString(2, name);
			statement.execute();
			int count = statement.getUpdateCount();
			statement.close();
			if (count == 0)
				activeChar.sendMessage("Character not found or position unaltered.");
			else{
				activeChar.sendMessage("Character's position is now set to ("+activeChar.getX()+","+activeChar.getY()+","+activeChar.getZ()+")");
			}
		}
		catch (SQLException se)
		{
			activeChar.sendMessage("SQLException while changing offline character's position");
		}
		finally
		{
			try 
			{
				con.close();
			}
			catch (Exception e) {}
		}
	}
}