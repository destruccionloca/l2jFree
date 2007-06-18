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
package net.sf.l2j.gameserver.communitybbs.Manager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.clientpackets.Say2;
import net.sf.l2j.gameserver.datatables.RecordTable;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * 
 * This manager extends {@link net.sf.l2j.gameserver.communityBBS.manager.BaseBBSManager} and is used by
 * {@link net.sf.l2j.gameserver.communityBBS.CommunityBoard}. This manager is in charge of print
 * all players in the community board and print some extra informations like RATES, server uptime etc...
 * 
 * The RegionBBSManager needs to be notified when a player leaves or enter the game. 
 * 
 */
public class RegionBBSManager extends BaseBBSManager
{
    /**
     * Logger for chat to log pm
     */
    private static Logger _logChat = Logger.getLogger("chat");
    /**
     * singleton instance
     */
    private static RegionBBSManager _Instance = null;
    /**
     * number of players online
     */
    private int _onlineCount = 0;
    /**
     * number of gm online
     */
    private int _onlineCountGm = 0;
    /**
     * map of all online players
     */
    private static Map<Integer, List<L2PcInstance>> _onlinePlayers = new FastMap<Integer, List<L2PcInstance>>().setShared(true);
    /**
     * map of community pages. The pages are stored to avoid recreating pages on each click on community  board.
     * The key is the number of the page. The value is a map with... ?
     */
    private static Map<Integer, Map<String, String>> _communityPages = new FastMap<Integer, Map<String, String>>().setShared(true);
    
    /**
     * constants for html creation
     */
    private final static String tdClose = "</td>";
    private final static String tdOpen = "<td align=left valign=top>";
    private final static String trClose = "</tr>";
    private final static String trOpen = "<tr>";
    private final static String colSpacer = "<td FIXWIDTH=15></td>";
    private final static String smallButton = "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">";
    

    /**
     * @return a singleton of RegionBBSManager
     */
    public static RegionBBSManager getInstance()
    {
        if(_Instance == null)
        {
            _Instance = new RegionBBSManager();
        }
        return _Instance;
    }   
    
    /**
     * parse the command for RegionBBSManager.
     * The available commands are : 
     * 
     * <pre>
     *  _bbsloc => print the old community board first page
     *  _bbsloc;page;i => print the page i
     *  _bbsloc;playerinfo; => print the player information (should be factorized with FriendManager printFriends)
     * </pre>
     * 
     * @param command
     * @param activeChar
     * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
     */
    @Override
    public void parsecmd(String command, L2PcInstance activeChar)
    {
        if (command.equals("_bbsloc"))
        {
            showOldCommunity(activeChar, 1);    
        }
        else if (command.startsWith("_bbsloc;page;"))
        {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            int page = 0;
            try
            {
                page = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException nfe) {}
            
            showOldCommunity(activeChar, page); 
        }
        else if (command.startsWith("_bbsloc;playerinfo;"))
        {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            String name = st.nextToken();
            
            showOldCommunityPI(activeChar, name);   
        }
        else
        {
            if(Config.COMMUNITY_TYPE.equals("old"))
            {
                showOldCommunity(activeChar, 1);    
            }
            else
            {
                showBoardNotImplemented(command, activeChar);
            }
        }
    }
    
    /**
     * Show player information
     * @param activeChar
     * @param name 
     */
    private void showOldCommunityPI(L2PcInstance activeChar, String name)
    {
        // initialize html
        // ----------------
        TextBuilder htmlCode = new TextBuilder("<html><body><br>");
        htmlCode.append("<table border=0>"+trOpen + colSpacer+"<td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=2>"+tdClose+trClose+trOpen+colSpacer+tdOpen);        

        // get the player instance of this player name
        // from L2World 
        // ----------------------------------------------
        L2PcInstance player = L2World.getInstance().getPlayer(name);
        
        if (player != null)
        {
            // add sex and level to html
            // --------------------------
            String sex = "Male";
            if (player.getAppearance().getSex())
            {
                sex = "Female";
            }
            String levelApprox = "low";
            if (player.getLevel() >= 60)
                levelApprox = "very high";
            else if (player.getLevel() >= 40)
                levelApprox = "high";
            else if (player.getLevel() >= 20)
                levelApprox = "medium";
            htmlCode.append("<table border=0>"+trOpen+tdOpen+player.getName()+" ("+sex+" "+player.getTemplate().getClassName()+"):"+tdClose+trClose);
            htmlCode.append(trOpen+tdOpen+"Level: "+levelApprox+tdClose+trClose);
            htmlCode.append(trOpen+tdOpen+"<br>"+tdClose+trClose);
            
            // show experience, level and experience needed for level up if active char is gm, or 
            // SHOW_LEVEL_COMMUNITYBOARD = true or player == activechar
            // ----------------------------------------------------------
            if (activeChar != null && (activeChar.isGM() || player.getObjectId() == activeChar.getObjectId()
                    || Config.SHOW_LEVEL_COMMUNITYBOARD))
            {
                long nextLevelExp = 0;
                long nextLevelExpNeeded = 0;
                if (player.getLevel() < (Experience.MAX_LEVEL - 1))
                {
                    nextLevelExp = Experience.LEVEL[player.getLevel() + 1];
                    nextLevelExpNeeded = nextLevelExp-player.getExp();
                }
                
                htmlCode.append(trOpen+tdOpen+"Level: "+player.getLevel()+tdClose+trClose);
                htmlCode.append(trOpen+tdOpen+"Experience: "+player.getExp()+"/"+nextLevelExp+tdClose+trClose);
                htmlCode.append(trOpen+tdOpen+"Experience needed for level up: "+nextLevelExpNeeded+tdClose+trClose);
                htmlCode.append(trOpen+tdOpen+"<br>"+tdClose+trClose);
            }
            
            // add uptime for this player
            // ---------------------------
            int uptime = (int)player.getUptime()/1000;
            int h = uptime/3600;
            int m = (uptime-(h*3600))/60;
            int s = ((uptime-(h*3600))-(m*60));
            
            htmlCode.append(trOpen+tdOpen+"Uptime: "+h+"h "+m+"m "+s+"s"+tdClose+trClose);
            htmlCode.append(trOpen+tdOpen+"<br>"+tdClose+trClose);
            
            // add clan for this player
            // -------------------------
            if (player.getClan() != null)
            {
                htmlCode.append(trOpen+tdOpen+"Clan: "+player.getClan().getName()+tdClose+trClose);
                htmlCode.append(trOpen+tdOpen+"<br>"+tdClose+trClose);
            }
            
            // add button to send a pm
            // ------------------------
            htmlCode.append(trOpen+tdOpen+"<multiedit var=\"pm\" width=240 height=40><button value=\"Send PM\" action=\"Write Region PM "+player.getName()+" pm pm pm\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">"+tdClose+trClose+trOpen+tdOpen+"<br><button value=\"Back\" action=\"bypass _bbsloc"+smallButton+tdClose+trClose+"</table>");
            htmlCode.append(tdClose+trClose+"</table>");          
            htmlCode.append("</body></html>");
            separateAndSend(htmlCode.toString(),activeChar);
        }
        else
        {
            ShowBoard sb = new ShowBoard("<html><body><br><br><center>No player with name "+name+"</center><br><br></body></html>","101");
            activeChar.sendPacket(sb);
            activeChar.sendPacket(new ShowBoard(null,"102"));
            activeChar.sendPacket(new ShowBoard(null,"103"));  
        }
    }

    /**
     * Print the community board (send the page through different packets to the active char)
     * @param activeChar
     * @param page number
     */
    private void showOldCommunity(L2PcInstance activeChar,int page)
    {       
        separateAndSend(getCommunityPage(page, activeChar.isGM() ? "gm" : "pl"),activeChar);
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsewrite(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
     */
    @Override
    public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
    {
        if (activeChar == null)
            return;
        
        if (ar1.equals("PM"))
        {           
            TextBuilder htmlCode = new TextBuilder("<html><body><br>");
            htmlCode.append("<table border=0>"+trOpen+colSpacer+"<td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=2>"+tdClose+trClose+trOpen+colSpacer+tdOpen);

            try
            {
                    
                L2PcInstance receiver = L2World.getInstance().getPlayer(ar2);
                if (receiver == null)
                {
                    htmlCode.append("Player not found!<br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;"+ar2+smallButton);
                    htmlCode.append(tdClose+trClose+"</table></body></html>");
                    separateAndSend(htmlCode.toString(),activeChar);
                    return;
                }
                    
                if (activeChar.isInJail() && Config.JAIL_DISABLE_CHAT)
                {
                    activeChar.sendMessage("You can not chat while in jail.");
                    return;
                }
                
                if (Config.LOG_CHAT)  
                { 
                    LogRecord record = new LogRecord(Level.INFO, ar3); 
                    record.setLoggerName("chat"); 
                    record.setParameters(new Object[]{"TELL", "[" + activeChar.getName() + " to "+receiver.getName()+"]"}); 
                    _logChat.log(record); 
                } 
                CreatureSay cs = new CreatureSay(activeChar.getObjectId(), Say2.TELL, activeChar.getName(), ar3);
                if (receiver != null && 
                        !BlockList.isBlocked(receiver, activeChar))
                {   
                    if (!receiver.getMessageRefusal())
                    {
                        receiver.sendPacket(cs);
                        activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), Say2.TELL, "->" + receiver.getName(), ar3));
                        htmlCode.append("Message Sent<br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;"+receiver.getName()+smallButton);
                        htmlCode.append("</td></tr></table></body></html>");
                        separateAndSend(htmlCode.toString(),activeChar)  ;
                    }
                    else
                    {
                        SystemMessage sm = new SystemMessage(SystemMessage.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);        
                        activeChar.sendPacket(sm);
                        parsecmd("_bbsloc;playerinfo;"+receiver.getName(), activeChar);
                    }
                }
                else
                {
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_NOT_ONLINE);
                    sm.addString(receiver.getName());
                    activeChar.sendPacket(sm);
                    sm = null;
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
                // ignore
            }
        }
        else
        {
            showBoardNotImplemented(ar1, activeChar);  
        }
        
    }

    /**
     * @param command
     * @param activeChar
     */
    private void showBoardNotImplemented(String command, L2PcInstance activeChar)
    {
        ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: "+command+" is not implemented yet</center><br><br></body></html>","101");
        activeChar.sendPacket(sb);
        activeChar.sendPacket(new ShowBoard(null,"102"));
        activeChar.sendPacket(new ShowBoard(null,"103"));
    }
    
    /**
     * Public method used to notify the community board that a player enter or leave L2World.
     * This method retrieves all players in the world, sort them by name, add them to online players
     * and write community pages to a internal map
     *
     */
    public synchronized void changeCommunityBoard()
    {
        // retrieve all players in the L2World
        // ------------------------------------
		Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
		List<L2PcInstance> sortedPlayers = new FastList<L2PcInstance>();
		sortedPlayers.addAll(players);
		players = null;
		
        // Sort player list by name
        // ------------------------
		Collections.sort(sortedPlayers, new Comparator<L2PcInstance>()
				{
					public int compare(L2PcInstance p1, L2PcInstance p2)
					{
						return p1.getName().compareToIgnoreCase(p2.getName());
					}
				}
		);
		// clear map and variables
        // ------------------------
		_onlinePlayers.clear();
		_onlineCount = 0;
		_onlineCountGm = 0;
		
        // add player to online player
        // ---------------------------
		for (L2PcInstance player : sortedPlayers)
		{
			addOnlinePlayer(player);
		}
        // write community page
        // --------------------
        _communityPages.clear();
        writeCommunityPages();
    }

    private void addOnlinePlayer(L2PcInstance player)
    {
        boolean added = false;
        
        for (List<L2PcInstance> l2pcInstance : _onlinePlayers.values())
        {
            if (l2pcInstance.size() < Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
            {
                if (!l2pcInstance.contains(player))
                {
                    l2pcInstance.add(player);
                    if (!player.getAppearance().getInvisible())
                        _onlineCount++;
                    _onlineCountGm++;
                }
                added = true;
                break;
            }
            else if (l2pcInstance.contains(player))
            {
                added = true;
                break;
            }
        }

        if (!added)
        {
            List<L2PcInstance> temp = new FastList<L2PcInstance>();
            int page = _onlinePlayers.size()+1;
            if (temp.add(player))
            {
                _onlinePlayers.put(page, temp);
                if (!player.getAppearance().getInvisible())
                    _onlineCount++;
                _onlineCountGm++;
            }
        }
    }
    
    private void writeCommunityPages()
    {
        for (int page : _onlinePlayers.keySet())
        {
            FastMap<String, String> communityPage = new FastMap<String, String>();
            TextBuilder htmlCode = new TextBuilder("<html><body><br>");
    
            writeHeader(htmlCode);
    
            htmlCode.append(trOpen + tdOpen + L2World.getInstance().getAllVisibleObjectsCount()
                + " Object count"+ tdClose + trClose);
    
            htmlCode.append(trOpen + tdOpen + getOnlineCount("gm") + " Player(s) Online" + tdClose + trClose);
            htmlCode.append("</table>");
    
            showOnlinePlayers("gm",page, htmlCode);

            paginateOnlinePlayers("gm",page, htmlCode);

            htmlCode.append("</body></html>");

            communityPage.put("gm", htmlCode.toString());

            htmlCode = new TextBuilder("<html><body><br>");
            writeHeader(htmlCode);
            
            htmlCode.append(trOpen + tdOpen + getOnlineCount("pl") + " Player(s) Online" + tdClose + trClose);
            htmlCode.append("</table>");
    
            showOnlinePlayers("pl",page, htmlCode);
            
            paginateOnlinePlayers("pl",page, htmlCode);

            htmlCode.append("</body></html>");
            
            communityPage.put("pl", htmlCode.toString());

            _communityPages.put(page, communityPage);
        }
    }

    /**
     * @param type
     * @param page
     * @param htmlCode
     */
    private void paginateOnlinePlayers(String type, int page, TextBuilder htmlCode)
    {
        if (getOnlineCount(type) > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
        {
            htmlCode.append("<table border=0 width=600>");
            
            htmlCode.append("<tr>");
            if (page == 1) htmlCode.append("<td align=right width=190><button value=\"Prev\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            else htmlCode.append("<td align=right width=190><button value=\"Prev\" action=\"bypass _bbsloc;page;"
                + (page - 1)
                + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            htmlCode.append("<td FIXWIDTH=10></td>");
            htmlCode.append("<td align=center valign=top width=200>Displaying " + (((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + 1) + " - "
                + (((page -1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + getOnlinePlayers(page).size()) + " player(s)</td>");
            htmlCode.append("<td FIXWIDTH=10></td>");
            if (getOnlineCount(type) <= (page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD)) htmlCode.append("<td align=left width=190><button value=\"Next\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            else htmlCode.append("<td align=left width=190><button value=\"Next\" action=\"bypass _bbsloc;page;"
                + (page + 1)
                + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            htmlCode.append("</tr>");
            htmlCode.append("</table>");
        }
    }

    /**
     * @param type
     * @param page
     * @param htmlCode
     */
    private void showOnlinePlayers(String type, int page, TextBuilder htmlCode)
    {
        htmlCode.append("<table border=0>");
        htmlCode.append(trOpen + tdOpen + "<table border=0>");
        
        int cell;
        cell = 0;
        for (L2PcInstance player : getOnlinePlayers(page))
        {
            // player can't see invisible players, gm can
            if ( type.equals("pl"))
            {
                if ((player == null) || (player.getAppearance().getInvisible()))
                    continue;                           // Go to next
            }
   
            cell++;
   
            if (cell == 1) htmlCode.append(trOpen);
   
            htmlCode.append("<td align=left valign=top FIXWIDTH=110><a action=\"bypass _bbsloc;playerinfo;"
                + player.getName() + "\">");
   
            if (player.isGM())
            	htmlCode.append("<font color=\"LEVEL\">" + player.getName() + "</font>");
            else if (player.isCursedWeaponEquiped() && Config.SHOW_CURSED_WEAPON_OWNER)
            	htmlCode.append("<font color=\"FF0000\">" + player.getName() + "</font>");
            else
            	htmlCode.append(player.getName());
   
            htmlCode.append("</a>"+ tdClose);
   
            if (cell < Config.NAME_PER_ROW_COMMUNITYBOARD) htmlCode.append(colSpacer);
   
            if (cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
            {
                cell = 0;
                htmlCode.append(trClose);
            }
        }
        if (cell > 0 && cell < Config.NAME_PER_ROW_COMMUNITYBOARD) htmlCode.append(trClose);
        htmlCode.append("</table><br>"+ tdClose + trClose);
        
        htmlCode.append("</table>");
    }

    /**
     * @param htmlCode
     */
    private void writeHeader(TextBuilder htmlCode)
    {
    	RecordTable recordTableInstance = RecordTable.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("H:mm");
        Calendar cal = Calendar.getInstance();
        int t = GameTimeController.getInstance().getGameTime();
        
        htmlCode.append("<table>");
        htmlCode.append(trOpen + tdOpen + "Server Time: " + format.format(cal.getTime()) + tdClose + colSpacer);
        cal.set(Calendar.HOUR_OF_DAY, t / 60);
        cal.set(Calendar.MINUTE, t % 60);
        htmlCode.append(tdOpen + "Game Time: " + format.format(cal.getTime()) + tdClose + colSpacer);
        htmlCode.append("<td align=left valign=top>Server Restarted: " + GameServer.DateTimeServerStarted.getTime() + tdClose + trClose);
        htmlCode.append("</table>");
   
        htmlCode.append("<table>");
        htmlCode.append(trOpen + tdOpen + "XP Rate: " + Config.RATE_XP + tdClose + colSpacer);
        htmlCode.append(tdOpen + "Party XP Rate: " + Config.RATE_PARTY_XP + tdClose + colSpacer);
        htmlCode.append(tdOpen + "XP Exponent: " + Config.ALT_GAME_EXPONENT_XP + tdClose + trClose);
        
        htmlCode.append(trOpen + tdOpen + "SP Rate: " + Config.RATE_SP + tdClose + colSpacer);
        htmlCode.append(tdOpen + "Party SP Rate: " + Config.RATE_PARTY_SP + tdClose + colSpacer);
        htmlCode.append(tdOpen + "SP Exponent: " + Config.ALT_GAME_EXPONENT_SP + tdClose + trClose);
        
        htmlCode.append(trOpen + tdOpen + "Drop Rate: " + Config.RATE_DROP_ITEMS + tdClose + colSpacer);
        htmlCode.append(tdOpen + "Spoil Rate: " + Config.RATE_DROP_SPOIL + tdClose + colSpacer);
        htmlCode.append(tdOpen + "Adena Rate: " + Config.RATE_DROP_ADENA + tdClose + trClose);
        htmlCode.append("</table>");
   
        htmlCode.append("<table>");
        htmlCode.append(trOpen + tdOpen + " Record of Player(s) Online:" +  recordTableInstance.getMaxPlayer() + tdClose + trClose);
        htmlCode.append(trOpen + tdOpen + " On date : " + recordTableInstance.getDateMaxPlayer() + tdClose + trClose);
        
    }
    
    /**
     * @param type
     * @return the number of online player for a specific type (gm or player)
     */
    private int getOnlineCount(String type)
    {
        if (type.equalsIgnoreCase("gm"))
            return _onlineCountGm;
        else
            return _onlineCount;
    }
    
    /**
     * 
     * @param page
     * @return the list of online player
     */
    private List<L2PcInstance> getOnlinePlayers(int page)
    {
        return _onlinePlayers.get(page);
    }
    
    /**
     * Return the community page for this page number and this type
     * @param page
     * @param type
     * @return the html of the page
     */
    public String getCommunityPage(int page, String type)
    {
        return _communityPages.get(page).get(type);
    }
}
