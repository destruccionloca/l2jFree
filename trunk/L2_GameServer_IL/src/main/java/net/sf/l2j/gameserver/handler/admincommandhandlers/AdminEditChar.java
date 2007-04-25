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

import java.util.Collection;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.skills.Stats;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles following admin commands:
 * - edit_character
 * - current_player
 * - character_list
 * - show_characters
 * - find_character
 * - save_modifications
 * 
 * @version $Revision: 1.3.2.1.2.10 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminEditChar implements IAdminCommandHandler 
{
    private final static Log _log = LogFactory.getLog(AdminEditChar.class.getName());
        
       private static String[] _adminCommands = 
        {
                "admin_show_characters",
                "admin_find_character",
                "admin_save_stats",
                "admin_edit_quest",
                "admin_edit_stats",
                "admin_edit_class",
                "admin_edit",
                "admin_nokarma", // this is to remove karma from selected char...
                "admin_setkarma", // sets karma of target char to any amount. //setkarma <karma>
                "admin_rec",
                "admin_settitle",
                "admin_setname",
                "admin_setsex",
                "admin_setcolor",
                "admin_sethero",
                "admin_fullfood",
                "admin_remclanwait"
                };

        private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;
        private static final int REQUIRED_LEVEL2 = Config.GM_CHAR_EDIT_OTHER;
        private static final int REQUIRED_LEVEL_VIEW = Config.GM_CHAR_VIEW;

        public boolean useAdminCommand(String command, L2PcInstance activeChar) 
        {
            if (!((checkLevel(activeChar.getAccessLevel()) || checkLevel2(activeChar.getAccessLevel())) && activeChar.isGM())) 
                return false;
            
            if (command.equals("admin_remclanwait"))
            {
                L2Object target = activeChar.getTarget();
                L2PcInstance player = null;
                if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
                    return false;
                if (target instanceof L2PcInstance) {
                    player = (L2PcInstance)target;
                } else {
                    return false;
                }

                if (player.getClan() == null)
                {
                	player.setClanJoinExpiryTime(0);
                    player.sendMessage("A GM Has reset your clan wait time, You may now join another clan.");
                    activeChar.sendMessage("You have reset " +player.getName()+ "'s wait time to join another clan.");
                }
                else
                {
                    activeChar.sendMessage("Sorry, but " +player.getName()+ " must not be in a clan. Player must leave clan before the wait limit can be reset.");
                }
            }           
            else if (command.startsWith("admin_show_characters"))
            {
                try
                {   
                    String val = command.substring(22);
                    int page = Integer.parseInt(val);
                    listCharacters(activeChar, page);
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    //Case of empty page
                }
            }
            else if (command.startsWith("admin_find_character"))
            {
                try
                {
                    String val = command.substring(21); 
                    findCharacter(activeChar, val);
                }
                catch (StringIndexOutOfBoundsException e)
                {   //Case of empty character name
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                    sm.addString("You didnt enter a character name to find.");
                    activeChar.sendPacket(sm);
                    
                    listCharacters(activeChar, 0);
                }           
            }
            else if (command.startsWith("admin_edit"))
            {
                StringTokenizer st = new StringTokenizer(command);
                
                String plyr="";
                String cmd="";
                
                L2PcInstance target = null;
                
                if (st.countTokens()>0) cmd=st.nextToken();

                if (st.hasMoreTokens()) 
                {
                    plyr=st.nextToken();
                    target = L2World.getInstance().getPlayer(plyr);
                } else
                    if (activeChar.getTarget() instanceof L2PcInstance)
                        target=(L2PcInstance)activeChar.getTarget();
                
                if (target==null) target=activeChar;
                
                if (target == activeChar || activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
                {
                    if (cmd.equals("admin_edit"))
                    editCharacter(activeChar,target);
                    else
                    if (cmd.equals("admin_edit_stats"))
                    editCharacterStats(activeChar,target);
                    else
                    if (cmd.equals("admin_edit_class"))
                    editCharacterClass(activeChar,target);
                }
                
            }
            else if (command.startsWith("admin_save_stats"))
            {
                L2PcInstance target = null;

                if (activeChar.getTarget() instanceof L2PcInstance)
                    target=(L2PcInstance)activeChar.getTarget();
                
                if (target == activeChar || activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
                {
                    saveCharacterStats(activeChar,target,command.substring(17));
                }
            }
            // Karma control commands
            else if (command.equals("admin_nokarma"))
            {
                setTargetKarma(activeChar, 0);
            }
            else if (command.startsWith("admin_setkarma"))
            {
                try
                {   
                    String val = command.substring(15);
                    int karma = Integer.parseInt(val);
                    if (activeChar == activeChar.getTarget() || activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
                    
                    setTargetKarma(activeChar, karma);
                }catch (StringIndexOutOfBoundsException e){}
            }
            else if (command.equals("admin_rec"))
            {
                if (activeChar != activeChar.getTarget() && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
                    return false;
                L2Object target = activeChar.getTarget();
                L2PcInstance player = null;

                if (target instanceof L2PcInstance) {
                    player = (L2PcInstance)target;
                }
                else
                    return false;

                player.setRecomHave(player.getRecomHave() + 1);
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("You have been recommended by a GM");
                player.sendPacket(sm);
                player.broadcastUserInfo();
            }
            else if (command.startsWith("admin_rec"))
            {
                try
                {
                    String val = command.substring(10);
                    int recVal = Integer.parseInt(val);
                    L2Object target = activeChar.getTarget();
                    L2PcInstance player = null;
                    if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
                        return false;
                    if (target instanceof L2PcInstance) {
                        player = (L2PcInstance)target;
                    } else {
                        return false;
                    }
                    player.setRecomHave(player.getRecomHave() + recVal);
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                    sm.addString("You have been recommended by a GM");
                    player.sendPacket(sm);
                    player.broadcastUserInfo();
                } catch (NumberFormatException nfe)
                {
                    activeChar.sendMessage("You must specify the number of recommendations to add.");
                }
            }
            else if (command.startsWith("admin_settitle"))
            {
                try
                {
                    String val = command.substring(15); 
                    L2Object target = activeChar.getTarget();
                    L2PcInstance player = null;
            if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
                return false;
                    if (target instanceof L2PcInstance) {
                        player = (L2PcInstance)target;
                    } else {
                        return false;
                    }
                    player.setTitle(val);
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                    sm.addString("Your title has been changed by a GM");
                    player.sendPacket(sm);
                    player.broadcastUserInfo();
                }
                catch (StringIndexOutOfBoundsException e)
                {   //Case of empty character title
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                    sm.addString("You need to specify the new title.");
                    activeChar.sendPacket(sm);
                }           
            }
            else if (command.startsWith("admin_setname"))
            {
                try
                {
                    String val = command.substring(14); 
                    L2Object target = activeChar.getTarget();
                    L2PcInstance player = null;
            if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
                return false;
                    if (target instanceof L2PcInstance) {
                        player = (L2PcInstance)target;
                    } else {
                        return false;
                    }
                    player.setName(val);
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                    sm.addString("Your name has been changed by a GM");
                    player.sendPacket(sm);
                    player.broadcastUserInfo();
                }
                catch (StringIndexOutOfBoundsException e)
                {   //Case of empty character name
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                    sm.addString("You need to specify the new name.");
                    activeChar.sendPacket(sm);
                }           
            }   
            else if (command.startsWith("admin_setsex"))
           {
                if (activeChar != activeChar.getTarget() && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
                    return false;
               String[] parts = command.split(" ");
               L2PcInstance player = null;
               if (parts.length == 1)
                   if (activeChar.getTarget() instanceof L2PcInstance)
                       player =((L2PcInstance)activeChar.getTarget());
               else if (parts.length == 2)
                   player = L2World.getInstance().getPlayer(parts[1]);
               if (player != null)
               {
                   player.getAppearance().setSex(player.getAppearance().getSex()? false : true);
                   String _sex = player.getAppearance().getSex()==false?"Male":"Female";
                   player.store();
                   player.broadcastPacket(new CharInfo(player));
                   player.sendPacket(new UserInfo(player));
                   
                   player.sendMessage("Your genader has been changed to \""+_sex+"\" by a GM");
               }   
            }   
            else if (command.startsWith("admin_setcolor"))
            {
                try
                {
                    String val          = command.substring(15); 
                    L2Object target     = activeChar.getTarget();
                    L2PcInstance player = null;
                    if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
                    	return false;
                    if (target instanceof L2PcInstance) {
                        player = (L2PcInstance)target;
                    } else {
                        return false;
                    }
                    player.getAppearance().setNameColor(Integer.decode("0x"+val));
                    player.sendMessage("Your name color has been changed by a GM");
                    player.broadcastUserInfo();
                }
                catch (StringIndexOutOfBoundsException e)
                {   //Case of empty color
                    activeChar.sendMessage("You need to specify the new color.");
                }
            }  
            else if (command.startsWith("admin_fullfood"))
            {
               L2Object target = activeChar.getTarget();
               
               if (target instanceof L2PetInstance)
               {
                   L2PetInstance targetPet = (L2PetInstance)target;
                    targetPet.setCurrentFed(targetPet.getMaxFed());
               }
               else {
                   activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
               }
            }
            // [L2J_JP ADD START]
            else if (command.startsWith("admin_sethero"))
            {
                L2Object target = activeChar.getTarget();
                L2PcInstance player = null;
                if (activeChar != target && activeChar.getAccessLevel()<REQUIRED_LEVEL2)
                    return false;
                if (target instanceof L2PcInstance)
                    player = (L2PcInstance)target;
                else
                    return false;
                player.setHero(player.isHero() ? false : true);
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Admin changed your hero status");
                player.sendPacket(sm);
                player.broadcastUserInfo();       
            }
            // [L2J_JP ADD END]
           
            return true;
        }
        
        public String[] getAdminCommandList() {
            return _adminCommands;
        }
        
        private boolean checkLevel(int level) {
            return (level >= REQUIRED_LEVEL);
        }
        private boolean checkLevel2(int level) {
            return (level >= REQUIRED_LEVEL_VIEW);
        }
        
        private void listCharacters(L2PcInstance activeChar, int page)
        {   
            Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
            L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);

            int MaxCharactersPerPage = 20;
            int MaxPages = players.length / MaxCharactersPerPage;
            
            if (players.length > MaxCharactersPerPage * MaxPages)
                MaxPages++;
            
            //Check if number of users changed
            if (page>MaxPages)
            {
                page=MaxPages;
            }

            int CharactersStart = MaxCharactersPerPage*page;
            int CharactersEnd = players.length;     
            if (CharactersEnd - CharactersStart > MaxCharactersPerPage)
                CharactersEnd = CharactersStart + MaxCharactersPerPage;
            
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);      

            TextBuilder replyMSG = new TextBuilder("<html><body>");
            replyMSG.append("<table width=260><tr>");
            replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
            replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table>");
            replyMSG.append("<br><br>");
            replyMSG.append("<table width=270>");
            replyMSG.append("<tr><td width=270>You can find a character by writing his name and</td></tr>");
            replyMSG.append("<tr><td width=270>clicking Find bellow.<br></td></tr>");
            replyMSG.append("<tr><td width=270>Note: Names should be written case sensitive.</td></tr>");
            replyMSG.append("</table><br>");
            replyMSG.append("<center><table><tr><td>");
            replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
            replyMSG.append("</td></tr></table></center><br><br>");
            
            for (int x=0; x<MaxPages; x++)
            {
                int pagenr = x + 1;
                replyMSG.append("<center><a action=\"bypass -h admin_show_characters " + x + "\">Page " + pagenr + "</a></center>");    
            }           
            replyMSG.append("<br>");

            //List Players in a Table
            replyMSG.append("<table width=270>");       
            replyMSG.append("<tr><td width=80>Name:</td><td width=110>Class:</td><td width=40>Level:</td></tr>");
            for (int i = CharactersStart; i < CharactersEnd; i++)
            {   //Add player info into new Table row
                replyMSG.append("<tr><td width=80>" + "<a action=\"bypass -h admin_edit " + players[i].getName() + "\">" + players[i].getName() + "</a></td><td width=110>" + players[i].getTemplate().className + "</td><td width=40>" + players[i].getLevel() + "</td></tr>");
            }
            replyMSG.append("</table>");
            replyMSG.append("</body></html>");
            
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);          
        }
        private void editCharacter(L2PcInstance activeChar,L2PcInstance player)
        {
            if (player == null)
                return;
            activeChar.setTarget(player);
            
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5); 
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            replyMSG.append("<table width=260><tr>");
            replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=180><center>Character Edit Menu</center></td>");
            replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table>");
            replyMSG.append("<br><br>");
            
            
            // Character Player Info
            replyMSG.append("<table width=270>");
            replyMSG.append("<tr><td width=135><font color=\"LEVEL\">" + player.getName() + "</font></td><td width=135>Lv " + player.getLevel() + " " + player.getTemplate().className+ "</td></tr>");
            replyMSG.append("<tr><td width=135>Account: " + player.getAccountName() + "</td><td width=135>Access Level: " + player.getAccessLevel() + "</td></tr>");
            String _clanName="No Clan";
            if (player.getClanId()>0) _clanName="Clan "+ClanTable.getInstance().getClan(player.getClanId()).getName()+"("+player.getClanId()+")";
            String _sex = player.getAppearance().getSex()==false?"Male":"Female";
            replyMSG.append("<tr><td width=135>"+ _clanName + "</td><td width=130>Gender: " +  _sex + "</td></tr>");
            replyMSG.append("<tr><td width=135>Character location (x,y,z)</td><td width=135>" + player.getX() + " " + player.getY() + " " + player.getZ() + "</td></tr>");
            replyMSG.append("</table>");
            replyMSG.append("<br>");
           
            // Character edit
            replyMSG.append("<table width=260><tr>");
            replyMSG.append("<td width=65><button value=\"Edit Stats\" action=\"bypass -h admin_edit_stats\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=65><button value=\"Edit Skills\" action=\"bypass -h admin_show_skills\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=65><button value=\"Ench.Equip\" action=\"bypass -h admin_enchant\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=65><button value=\"Edit Quests\" action=\"bypass -h admin_show_quests\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table><br>");
            replyMSG.append("<table width=260><tr>");
            if (!player.isChatBanned())
            replyMSG.append("<td width=65><button value=\"Mute\" action=\"bypass -h admin_banchat "+player.getName()+" 3600 \" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            else
            replyMSG.append("<td width=65><button value=\"MuteOff\" action=\"bypass -h admin_unbanchat "+player.getName()+"\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            if (!player.isInJail())
            replyMSG.append("<td width=65><button value=\"Jail\" action=\"bypass -h admin_jail "+player.getName()+"\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            else
            replyMSG.append("<td width=65><button value=\"Unjail\" action=\"bypass -h admin_unjail "+player.getName()+"\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=65><button value=\"Get\" action=\"bypass -h admin_recall "+player.getName()+"\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=65><button value=\"Go\" action=\"bypass -h admin_teleportto "+player.getName()+"\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table>");
            replyMSG.append("<br>");
            replyMSG.append("<table width=260><tr>");
            replyMSG.append("<td width=65><button value=\"Ban\" action=\"bypass -h admin_ban "+player.getName()+"\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=65><button value=\"Kick\" action=\"bypass -h admin_kick "+player.getName()+"\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            if (player.isDead()==false)
            replyMSG.append("<td width=65><button value=\"Kill\" action=\"bypass -h admin_kill "+player.getName()+"\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            else
            replyMSG.append("<td width=65><button value=\"Resurrect\" action=\"bypass -h admin_res\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");            
            if (player.isParalyzed()==false)
            replyMSG.append("<td width=65><button value=\"Paralize\" action=\"bypass -h admin_para\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            else
            replyMSG.append("<td width=65><button value=\"UnParalize\" action=\"bypass -h admin_unpara\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");            
            replyMSG.append("</tr></table>");
            replyMSG.append("<br>");
        
            // Character Stats 
            replyMSG.append("<table width=270>");
            replyMSG.append("<tr><td width=20>HP</td><td width=115>" + (int)player.getCurrentHp() + " / " + player.getMaxHp() + "</td><td width=20>CP </td><td width=115>" + (int)player.getCurrentCp() + " / " + player.getMaxCp() + "</td></tr>");
            replyMSG.append("<tr><td width=20>MP</td><td width=115>" + (int)player.getCurrentMp() + " / " + player.getMaxMp() + "</td><td width=20>Load</td><td width=115>" + player.getCurrentLoad() + " / " + player.getMaxLoad() + "</td></tr>");
            replyMSG.append("<tr><td width=20>XP</td><td width=115>" + (int)player.getExp()+"</td><td width=40>SP</td><td width=70>" + player.getSp() + "</td></tr>");
            replyMSG.append("<tr><td width=20></td><td width=115>"+(int)player.getStat().getExpForLevel(player.getLevel()+1)+ "</td><td width=40></td><td width=70></td></tr>");
            replyMSG.append("</table>");
            replyMSG.append("<br>");
            
            replyMSG.append("<table width=260>");       
            replyMSG.append("<tr><td width=70>P. Atk.</td><td width=40>" + player.getPAtk(null) + "</td><td width=70>M. Atk:</td><td width=40>" + player.getMAtk(null, null) + "</td></tr>");
            replyMSG.append("<tr><td width=70>P. Def.</td><td width=40>" + player.getPDef(null) + "</td><td width=70>M. Def:</td><td width=40>" + player.getMDef(null, null) + "</td></tr>");
            replyMSG.append("<tr><td width=70>Accuracy</td><td width=40>" + player.getAccuracy() + "</td><td width=70>Evasion</td><td width=40>" + player.getEvasionRate(null) + "</td></tr>");
            replyMSG.append("<tr><td width=70>Critical</td><td width=40>" + player.getCriticalHit(null,null) + "</td><td width=70>Speed</td><td width=40>" + player.getRunSpeed() + "</td></tr>");
            replyMSG.append("<tr><td width=70>Atk. Spd.</td><td width=40>" + player.getPAtkSpd() + "</td><td width=70>Casting Spd.</td><td width=40>" + player.getMAtkSpd() + "</td></tr>");
            replyMSG.append("<tr><td width=70>Shld. Def.</td><td width=40>" + player.getStat().getShldDef()+ "</td><td width=70>Shld. Rate</td><td width=40>" + player.getStat().calcStat(Stats.SHIELD_RATE, 0, null, null) + "</td></tr>");
            replyMSG.append("</table>");
            replyMSG.append("<br>");
            
            replyMSG.append("<table width=270>");
            replyMSG.append("<tr><td width=45>STR</td><td width=45>" + player.getSTR() + "</td><td width=45>DEX</td><td width=45>" + player.getDEX() + "</td><td width=45>CON</td><td width=45>" + player.getCON() + "</td></tr>");
            replyMSG.append("<tr><td width=45>INT</td><td width=45>" + player.getINT() + "</td><td width=45>WIT</td><td width=45>" + player.getWIT() + "</td><td width=45>MEN</td><td width=45>" + player.getMEN() + "</td></tr>");
            replyMSG.append("</table>");
            replyMSG.append("<br>");
            
            replyMSG.append("<table width=260>");       
            replyMSG.append("<tr><td width=70>Karma</td><td width=40>" + player.getKarma() + "</td><td width=70>PvP Kills</td><td width=40>" + player.getPkKills()+ "</td></tr>");
            replyMSG.append("<tr><td width=70>PvP Flag</td><td width=40>" + (int)player.getPvpFlag() + "</td><td width=70>PK Kills</td><td width=40>" + player.getPvpKills() + "</td></tr>");
            replyMSG.append("</table>");
            replyMSG.append("<br>");      
            replyMSG.append("</body></html>");

            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }
        
        private void setTargetKarma(L2PcInstance activeChar, int newKarma) {
            // function to change karma of selected char
            L2Object target = activeChar.getTarget();
            L2PcInstance player = null;
            if (target instanceof L2PcInstance) {
                player = (L2PcInstance)target;
            } else {
                return;
            }
            
            if ( newKarma >= 0 ) {
                // for display
                int oldKarma = player.getKarma();           
                
                // update karma
                player.setKarma(newKarma);
                
                StatusUpdate su = new StatusUpdate(player.getObjectId());
                su.addAttribute(StatusUpdate.KARMA, newKarma);
                player.sendPacket(su);
                
                CharInfo info1 = new CharInfo(player);
                player.broadcastPacket(info1);
                UserInfo info2 = new UserInfo(player);
                player.sendPacket(info2);
                
                //Common character information
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Admin has changed your karma from " + oldKarma + " to " + newKarma + ".");
                player.sendPacket(sm);
                
                //Admin information 
                SystemMessage smA = new SystemMessage(SystemMessage.S1_S2);
                smA.addString("Successfully Changed karma for "+player.getName()+" from (" + oldKarma + ") to (" + newKarma + ").");        
                activeChar.sendPacket(smA);         
                
                if (_log.isDebugEnabled())
                    _log.debug("[SET KARMA] [GM]"+activeChar.getName()+" Changed karma for "+player.getName()+" from (" + oldKarma + ") to (" + newKarma + ").");
            }
            else {
                // tell admin of mistake 
                SystemMessage smA = new SystemMessage(SystemMessage.S1_S2);
                smA.addString("You must enter a value for karma greater than or equal to 0.");      
                activeChar.sendPacket(smA);
                
                if (_log.isDebugEnabled())
                    _log.debug("[SET KARMA] ERROR: [GM]"+activeChar.getName()+" entered an incorrect value for new karma: " + newKarma + " for "+player.getName()+".");         
            }
        }
            
        private void saveCharacterStats(L2PcInstance activeChar, L2PcInstance player, String modifications)
        {
                if (player == null)
                return;
            
                int hpval = (int)player.getCurrentHp();
                int mpval = (int)player.getCurrentMp();
                int cpval = (int)player.getCurrentCp();
                int karmaval = player.getKarma();
                int pvpflagval = player.getPvpFlag();
                int pvpkillsval = player.getPvpKills();
                int pkkillsval = player.getPkKills();
                int classidval = player.getClassId().getId();

                modifications=modifications.trim().replaceAll("  "," ");
                
                StringTokenizer st = new StringTokenizer(modifications);
                String param="";
                
                if (st.countTokens()<2) 
                {
                    return;
                } else
                    for (String var : modifications.split(" ")) 
                    {
                        try
                        {
                        int val=Integer.parseInt(var);
                        
                        if (param.equals("hp"))
                        {
                            hpval=val;      
                        }
                        if (param.equals("mp"))
                        {
                            mpval=val;      
                        }
                        if (param.equals("cp"))
                        {
                            cpval=val;      
                        }       
                        if (param.equals("karma"))
                        {
                            karmaval=val;       
                        }   
                        if (param.equals("pvpflag"))
                        {
                            pvpflagval=val; 
                        }   
                        if (param.equals("pvp"))
                        {
                            pvpkillsval=val;        
                        }
                        if (param.equals("pk"))
                        {
                            pkkillsval=val; 
                        }
                        if (param.equals("class"))
                        {
                            classidval=val;     
                        }
                        }
                        catch (NumberFormatException  e)
                        {
                        param=var;
                        }
                    }
                
            //Common character information
            player.sendMessage("Admin has changed your stats." +
                               "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval +
                               "  Karma: " + karmaval + "  PvP Flag: " + pvpflagval + " PvP/PK " + pvpkillsval + "/" + pkkillsval + 
                               "  Class: " + player.getTemplate().className + " (" + classidval + ")");
            
            player.setCurrentHp(hpval);
            player.setCurrentMp(mpval);
            player.setCurrentCp(cpval);
            player.setKarma(karmaval);
            player.setPvpFlag(pvpflagval);
            player.setPvpKills(pvpkillsval);
            player.setPkKills(pkkillsval);
            player.setClassId(classidval);
                
            // Update the base class also if this character is not on a sub-class.
            if (!player.isSubClassActive())
                player.setBaseClass(classidval);
                
            // Save the changed parameters to the database.
            player.store();
                
            StatusUpdate su = new StatusUpdate(player.getObjectId());
            su.addAttribute(StatusUpdate.CUR_HP, hpval);
            su.addAttribute(StatusUpdate.MAX_HP, player.getMaxHp());
            su.addAttribute(StatusUpdate.CUR_MP, mpval);
            su.addAttribute(StatusUpdate.MAX_MP, player.getMaxMp());
            su.addAttribute(StatusUpdate.CUR_CP, cpval);
            su.addAttribute(StatusUpdate.MAX_CP, player.getMaxCp());
            su.addAttribute(StatusUpdate.KARMA, karmaval);
            su.addAttribute(StatusUpdate.PVP_FLAG, pvpflagval);
            player.sendPacket(su);
                
            //Admin information 
            activeChar.sendMessage("Changed stats of " + player.getName() + "." +
                               "  HP: " + hpval + "  MP: " + mpval + "  CP: " + cpval + 
                               "  Karma: " + karmaval + "  PvP: " + pvpflagval + " / " + pvpkillsval + 
                               "  Class: " + player.getTemplate().className + " (" + classidval + ")");
                
            if (_log.isDebugEnabled())
                _log.debug("[GM]"+activeChar.getName()+" changed stats of "+player.getName()+". " +
                          " HP: "+hpval+" MP: "+mpval+" CP: " + cpval + " Karma: "+karmaval+
                          " PvP: "+pvpflagval+" / "+pvpkillsval+ " ClassID: "+classidval);

            player.broadcastPacket(new CharInfo(player));
            player.sendPacket(new UserInfo(player));
                
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            int x=player.getX(); int y=player.getY(); int z=player.getZ();
            player.decayMe();
            player.spawnMe(x,y,z);
            activeChar.setTarget(player);
            editCharacterStats(activeChar,player);
            }

        private void editCharacterStats(L2PcInstance activeChar,L2PcInstance player)
        {
            if (player == null) 
                return;
            activeChar.setTarget(player);
            
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5); 
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            replyMSG.append("<table width=260><tr>");
            replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=180><center>Stats Edit Menu</center></td>");
            replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_edit "+player.getName()+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table>");
            replyMSG.append("<br><br>");

            replyMSG.append("<table width=270>");
            replyMSG.append("<tr><td width=135><font color=\"LEVEL\">" + player.getName() + "</font></td><td width=135>" + player.getClassId().getId() + " " + player.getTemplate().className+ "</td></tr>");
            replyMSG.append("</table>");
            replyMSG.append("<br>");

            replyMSG.append("<table width=260>");
            replyMSG.append("<tr><td width=50>HP</td><td width=70>" + (int)player.getCurrentHp() + " / " + player.getMaxHp() + "</td><td width=20></td><td width=80><edit var=\"hp\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h admin_save_stats hp $hp\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>MP</td><td width=70>" + (int)player.getCurrentMp() + "/" + player.getMaxMp() + "</td><td width=20></td><td width=80><edit var=\"mp\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h admin_save_stats mp $mp\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>CP</td><td width=70>" + (int)player.getCurrentCp() + " / " + player.getMaxCp() + "</td><td width=20></td><td width=80><edit var=\"cp\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h admin_save_stats cp $cp\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>XP</td><td width=70>" + player.getExp()+ "</td><td width=20><button value=\"[ - ]\" action=\"bypass -h" + " admin_remove_exp_sp $xp 0\" width=20 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=80><edit var=\"xp\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"[ + ]\" action=\"bypass -h admin_add_exp_sp $xp 0\" width=20 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>SP</td><td width=70>" + player.getSp()+ "</td><td width=20><button value=\"[ - ]\" action=\"bypass -h" + " admin_remove_exp_sp 0 $sp\" width=20 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=80><edit var=\"sp\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"[ + ]\" action=\"bypass -h admin_add_exp_sp 0 $sp\" width=20 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>Level</td><td width=70>" + player.getLevel() + "</td><td width=20></td><td width=80><edit var=\"level\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h" + " admin_set_level $level\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>Gender</td><td width=70>" + (player.getAppearance().getSex()==false?"Male":"Female") + "</td><td width=20></td><td width=80>"+(player.getAppearance().getSex()==true?"Male":"Female")+"</td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h" + " admin_setsex "+player.getName()+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50><a action=\"bypass -h" + " admin_edit_class "+player.getName()+"\"><font color=\"FFFFFF\">Class</a></td><td width=70>" + player.getClassId().getId() + "</td><td width=20></td><td width=80><edit var=\"class\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h" + " admin_save_stats class $class\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>Karma</td><td width=70>" + player.getKarma() + "</td><td width=20></td><td width=80><edit var=\"karma\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h" + " admin_save_stats karma $karma\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>PvP Flag</td><td width=70>" + player.getPvpFlag() + "</td><td width=20></td><td width=80><edit var=\"pvpflag\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h" + " admin_save_stats pvpflag $pvpflag\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>PvP Kills</td><td width=70>" +player.getPvpKills()+ "</td><td width=20></td><td width=80><edit var=\"pvp\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h" + " admin_save_stats pvp $pvp\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>PK Kills</td><td width=70>" +player.getPkKills()+ "</td><td width=20></td><td width=80><edit var=\"pk\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h" + " admin_save_stats pk $pk\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("<tr><td width=50>AccessLv</td><td width=70>" + player.getAccessLevel() + "</td><td width=20></td><td width=80><edit var=\"access\" width=70></td>");
            replyMSG.append("<td width=40><button value=\"Set\" action=\"bypass -h admin_changelvl "+player.getName()+" $access \" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            replyMSG.append("</table><br>");
            replyMSG.append("<br><center><table><tr><td><button value=\"Set All\" action=\"bypass -h" + " admin_save_stats hp $hp mp $mp cp $cp class $class karma $karma pvpflag $pvpflag pvp $pvp pk $pk\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table></center>");
            replyMSG.append("<table><tr><td>Note: Access Level,Gender,XP,SP,Level will be not changed after \"Set All\"</td></tr>");
            replyMSG.append("</table>");
            replyMSG.append("</body></html>");

            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }

        //TODO: rework to smaller html output
        private void editCharacterClass(L2PcInstance activeChar,L2PcInstance player)
        {
            if (player==null) 
                return;
            activeChar.setTarget(player);
            
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5); 
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            replyMSG.append("<table width=260><tr>");
            replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=180><center>Class Edit Menu</center></td>");
            replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_edit_stats "+player.getName()+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table>");
            replyMSG.append("<br><br>");

            replyMSG.append("<table width=270>");
            replyMSG.append("<tr><td width=135><font color=\"LEVEL\">" + player.getName() + "</font></td><td width=135>" + player.getClassId().getId() + " " + player.getTemplate().className+ "</td></tr>");
            replyMSG.append("</table>");
            replyMSG.append("<br>");
            
            for (String _class : CharTemplateTable.charClasses)
            {
                if (!_class.startsWith("dummy"))
                {
                    int _classId=CharTemplateTable.getClassIdByName(_class);
                    replyMSG.append("<br>"+(_classId-1)+"<a action=\"bypass -h" + " admin_save_stats class "+(_classId-1)+"\">"+_class+"</a>");
                }
            }
            replyMSG.append("</body></html>");

            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
        }

        private void findCharacter(L2PcInstance activeChar, String CharacterToFind)
        {
            Collection<L2PcInstance> allPlayers = L2World.getInstance().getAllPlayers();
            L2PcInstance[] players = allPlayers.toArray(new L2PcInstance[allPlayers.size()]);
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            int CharactersFound = 0;
            
            TextBuilder replyMSG = new TextBuilder("<html><body>");
            replyMSG.append("<table width=260><tr>");
            replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
            replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr></table>");
            replyMSG.append("<br><br>");
            replyMSG.append("<table width=270>");
            
            for (int i = 0; i < players.length; i++)
            {   //Add player info into new Table row
                if (players[i].getName().startsWith((CharacterToFind)))
                {
                    if (CharactersFound == 0)
                        replyMSG.append("<tr><td width=80>Name</td><td width=110>Class</td><td width=40>Level</td></tr>");
                    
                    CharactersFound++;
                    replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_edit " + players[i].getName() + "\">" + players[i].getName() + "</a></td><td width=110>" + players[i].getTemplate().className + "</td><td width=40>" + players[i].getLevel() + "</td></tr>");
                }
            }
            
            if (CharactersFound==0)
            {
                replyMSG.append("<tr><td width=270>Your search did not find any characters.</td></tr>");
                replyMSG.append("<tr><td width=270>Please try again.<br></td></tr>");
                replyMSG.append("</table><br>");
                replyMSG.append("<center><table><tr><td>");
                replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
                replyMSG.append("</td></tr></table></center>");
            }
            else
            {
                replyMSG.append("</table>");
                replyMSG.append("<center><br>Found " + CharactersFound + " character");
                
                if (CharactersFound==1)
                {
                    replyMSG.append(".");
                }
                else 
                {
                    if (CharactersFound>1)
                    {
                        replyMSG.append("s.");
                    }
                }

            }
            
            replyMSG.append("</center></body></html>");
            
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);          
        }
    }
