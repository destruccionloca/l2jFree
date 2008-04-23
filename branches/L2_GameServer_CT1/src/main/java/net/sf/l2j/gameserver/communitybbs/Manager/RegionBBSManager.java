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
package net.sf.l2j.gameserver.communitybbs.Manager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.datatables.RecordTable;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RegionBBSManager extends BaseBBSManager
{
    private static Log _logChat = LogFactory.getLog("chat");
    private static RegionBBSManager _instance = null;
    private int _onlineCount = 0;
    private int _onlineCountGm = 0; 
    private static FastMap<Integer, FastList<L2PcInstance>> _onlinePlayers = new FastMap<Integer, FastList<L2PcInstance>>().setShared(true);
    private static FastMap<Integer, FastMap<String, String>> _communityPages = new FastMap<Integer, FastMap<String, String>>().setShared(true);
    
    private final static String tdClose = "</td>";
    private final static String tdOpen = "<td align=left valign=top>";
    private final static String trClose = "</tr>";
    private final static String trOpen = "<tr>";
    private final static String colSpacer = "<td FIXWIDTH=15></td>";
    private final static String smallButton = "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
    

    /**
     * @return a singleton
     */
    public static RegionBBSManager getInstance()
    {
        if(_instance == null)
        {
            _instance = new RegionBBSManager();
        }
        return _instance;
    }   
    
    /* (non-Javadoc)
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
     * @param activeChar
     * @param name
     */
    private void showOldCommunityPI(L2PcInstance activeChar, String name)
    {
        TextBuilder htmlCode = new TextBuilder("<html><body><br>");
        htmlCode.append("<table border=0>"+trOpen + colSpacer+"<td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=2>"+tdClose+trClose+trOpen+colSpacer+tdOpen);        
        L2PcInstance player = L2World.getInstance().getPlayer(name);
        
        if (player != null)
        {
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
            
            int uptime = (int)player.getUptime()/1000;
            int h = uptime/3600;
            int m = (uptime-(h*3600))/60;
            int s = ((uptime-(h*3600))-(m*60));
            
            htmlCode.append(trOpen+tdOpen+"Uptime: "+h+"h "+m+"m "+s+"s"+tdClose+trClose);
            htmlCode.append(trOpen+tdOpen+"<br>"+tdClose+trClose);
            
            if (player.getClan() != null)
            {
                htmlCode.append(trOpen+tdOpen+"Clan: "+player.getClan().getName()+tdClose+trClose);
                htmlCode.append(trOpen+tdOpen+"<br>"+tdClose+trClose);
            }
            
            htmlCode.append(trOpen+tdOpen+"<multiedit var=\"pm\" width=240 height=40><button value=\"Send PM\" action=\"Write Region PM "+player.getName()+" pm pm pm\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"+tdClose+trClose+trOpen+tdOpen+"<br><button value=\"Back\" action=\"bypass _bbsloc"+smallButton+tdClose+trClose+"</table>");
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
     * @param activeChar
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
                    activeChar.sendMessage("You can not chat with the outside of the jail.");
                    parsecmd("_bbsloc;playerinfo;"+receiver.getName(), activeChar);
                    return;
                }
                if (activeChar.isChatBanned())
                {
                    activeChar.sendPacket(new SystemMessage(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED));
                    parsecmd("_bbsloc;playerinfo;"+receiver.getName(), activeChar);
                    return;
                }
                
                CreatureSay cs = new CreatureSay(activeChar.getObjectId(), SystemChatChannelId.Chat_Tell.getId(), activeChar.getName(), ar3);
                if (!BlockList.isBlocked(receiver, activeChar))
                {
                    if (Config.JAIL_DISABLE_CHAT && receiver.isInJail())
                    {
                        activeChar.sendMessage("Player is in jail.");
                        parsecmd("_bbsloc;playerinfo;"+receiver.getName(), activeChar);
                        return;
                    }
                    if (receiver.isChatBanned())
                    {
                        activeChar.sendMessage("Player is chat banned.");
                        parsecmd("_bbsloc;playerinfo;"+receiver.getName(), activeChar);
                        return;
                    }
                    if (receiver.getMessageRefusal())
                    {
                        SystemMessage sm = new SystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);        
                        activeChar.sendPacket(sm);
                        parsecmd("_bbsloc;playerinfo;"+receiver.getName(), activeChar);
                        return;
                    }

                    if (Config.LOG_CHAT)
                    {
                        _logChat.info( "WHISPER[" + activeChar.getName() + " to " + receiver.getName() + "] " + ar3);
                    }
                    receiver.sendPacket(cs);
                    activeChar.sendPacket(new CreatureSay(activeChar.getObjectId(), SystemChatChannelId.Chat_Tell.getId(), "->" + receiver.getName(), ar3));
                    htmlCode.append("Message Sent<br><button value=\"Back\" action=\"bypass _bbsloc;playerinfo;"+receiver.getName()+smallButton);
                    htmlCode.append("</td></tr></table></body></html>");
                    separateAndSend(htmlCode.toString(),activeChar)  ;
                }
                else
                {
                    SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_NOT_ONLINE);
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
    
    public synchronized void changeCommunityBoard()
    {
		Collection<L2PcInstance> players = L2World.getInstance().getAllPlayers();
		FastList<L2PcInstance> sortedPlayers = new FastList<L2PcInstance>();
		sortedPlayers.addAll(players);
		players = null;
		
		Collections.sort(sortedPlayers, new Comparator<L2PcInstance>()
		{
			public int compare(L2PcInstance p1, L2PcInstance p2)
			{
				return p1.getName().compareToIgnoreCase(p2.getName());
			}
		});
		
		_onlinePlayers.clear();
		_onlineCount = 0;
		_onlineCountGm = 0;
		
		for (L2PcInstance player : sortedPlayers)
		{
			addOnlinePlayer(player);
		}
        _communityPages.clear();
        writeCommunityPages();
    }

    private void addOnlinePlayer(L2PcInstance player)
    {
        boolean added = false;
        
        for (FastList<L2PcInstance> page : _onlinePlayers.values())
        {
            if (page.size() < Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
            {
                if (!page.contains(player))
                {
                    page.add(player);
                    if (!player.getAppearance().getInvisible())
                        _onlineCount++;
                    _onlineCountGm++;
                }
                added = true;
                break;
            }
            else if (page.contains(player))
            {
                added = true;
                break;
            }
        }

        if (!added)
        {
            FastList<L2PcInstance> temp = new FastList<L2PcInstance>();
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
            if (Config.SHOW_LEGEND)
                htmlCode.append(trOpen + tdOpen + "<font color=\"LEVEL\">GM</font> <font color=\"00FF00\">Clan Leader</font> <font color=\"FF0000\">Cursedweapon</font> <font color=\"FF00FF\">Karma</font> <font color=\"999999\">Jailed</font>" + tdClose + trClose);
            htmlCode.append("</table>");
    
            showOnlinePlayers("gm",page, htmlCode);

            paginateOnlinePlayers("gm",page, htmlCode);

            htmlCode.append("</body></html>");

            communityPage.put("gm", htmlCode.toString());

            htmlCode = new TextBuilder("<html><body><br>");
            writeHeader(htmlCode);
            
            htmlCode.append(trOpen + tdOpen + getOnlineCount("pl") + " Player(s) Online" + tdClose + trClose);
            if (Config.SHOW_LEGEND)
                htmlCode.append(trOpen + tdOpen + "<font color=\"LEVEL\">GM</font> <font color=\"00FF00\">Clan Leader</font> <font color=\"FF0000\">Cursedweapon</font> <font color=\"FF00FF\">Karma</font> <font color=\"999999\">Jailed</font>" + tdClose + trClose);
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
            if (page == 1) htmlCode.append("<td align=right width=190><button value=\"Prev\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
            else htmlCode.append("<td align=right width=190><button value=\"Prev\" action=\"bypass _bbsloc;page;"
                + (page - 1)
                + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
            htmlCode.append("<td FIXWIDTH=10></td>");
            htmlCode.append("<td align=center valign=top width=200>Displaying " + (((page - 1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + 1) + " - "
                + (((page -1) * Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + getOnlinePlayers(page).size()) + " player(s)</td>");
            htmlCode.append("<td FIXWIDTH=10></td>");
            if (getOnlineCount(type) <= (page * Config.NAME_PAGE_SIZE_COMMUNITYBOARD)) htmlCode.append("<td align=left width=190><button value=\"Next\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
            else htmlCode.append("<td align=left width=190><button value=\"Next\" action=\"bypass _bbsloc;page;"
                + (page + 1)
                + "\" width=50 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
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
        
        int cell = 0;

        if (Config.BBS_SHOW_PLAYERLIST)
        {
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
                else if (player.getClan() != null && player.isClanLeader() && Config.SHOW_CLAN_LEADER
                        && player.getClan().getLevel() >= Config.SHOW_CLAN_LEADER_CLAN_LEVEL)
                    htmlCode.append("<font color=\"00FF00\">" + player.getName() + "</font>");
                else if (player.isCursedWeaponEquipped() && Config.SHOW_CURSED_WEAPON_OWNER)
                    htmlCode.append("<font color=\"FF0000\">" + player.getName() + "</font>");
                else if (player.getKarma() > 0 && Config.SHOW_KARMA_PLAYERS)
                    htmlCode.append("<font color=\"FF00FF\">" + player.getName() + "</font>");
                else if (player.isInJail() && Config.SHOW_JAILED_PLAYERS)
                    htmlCode.append("<font color=\"999999\">" + player.getName() + "</font>");
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
            htmlCode.append(trOpen);
            htmlCode.append("<td><img src=\"sek.cbui355\" width=600 height=1><br></td>");
            htmlCode.append(trClose);
            htmlCode.append("</table>");
        }
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
        htmlCode.append(tdOpen + "Server Restarted: " + GameServer.dateTimeServerStarted.getTime() + tdClose + trClose);
        htmlCode.append("</table>");
   
        htmlCode.append("<table>");
        htmlCode.append(trOpen + tdOpen + "XP Rate: x" + Config.RATE_XP + tdClose + colSpacer);
        htmlCode.append(tdOpen + "Party XP Rate: x" +  Config.RATE_XP * Config.RATE_PARTY_XP + tdClose + colSpacer);
        htmlCode.append(tdOpen + "XP Exponent: " + Config.ALT_GAME_EXPONENT_XP + tdClose + trClose);
        
        htmlCode.append(trOpen + tdOpen + "SP Rate: x" + Config.RATE_SP + tdClose + colSpacer);
        htmlCode.append(tdOpen + "Party SP Rate: x" + Config.RATE_SP * Config.RATE_PARTY_SP + tdClose + colSpacer);
        htmlCode.append(tdOpen + "SP Exponent: " + Config.ALT_GAME_EXPONENT_SP + tdClose + trClose);
        
        htmlCode.append(trOpen + tdOpen + "Drop Rate: " + Config.RATE_DROP_ITEMS + tdClose + colSpacer);
        htmlCode.append(tdOpen + "Spoil Rate: " + Config.RATE_DROP_SPOIL + tdClose + colSpacer);
        htmlCode.append(tdOpen + "Adena Rate: " + Config.RATE_DROP_ADENA + tdClose + trClose);
        htmlCode.append("</table>");
   
        htmlCode.append("<table>");
        htmlCode.append(trOpen + tdOpen + " Record of Player(s) Online:" +  recordTableInstance.getMaxPlayer() + tdClose + trClose);
        htmlCode.append(trOpen + tdOpen + " On date : " + recordTableInstance.getDateMaxPlayer() + tdClose + trClose);

    }
    
    private int getOnlineCount(String type)
    {
        if (type.equalsIgnoreCase("gm"))
            return _onlineCountGm;
        return _onlineCount;
    }
    
    private FastList<L2PcInstance> getOnlinePlayers(int page)
    {
        return _onlinePlayers.get(page);
    }
    
    public String getCommunityPage(int page, String type)
    {
        return _communityPages.get(page) != null ? _communityPages.get(page).get(type) : null;
    }
}
