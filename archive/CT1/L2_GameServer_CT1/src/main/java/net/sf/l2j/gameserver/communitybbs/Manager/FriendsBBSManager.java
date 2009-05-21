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

import java.util.Map;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2FriendList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.FriendAddRequest;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FriendsBBSManager extends BaseBBSManager
{
    private static Log _logChat = LogFactory.getLog("chat"); 
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager#parsecmd(java.lang.String, net.sf.l2j.gameserver.model.actor.instance.L2PcInstance)
     */
    @Override
    public void parsecmd(String command, L2PcInstance activeChar)
    {
        if (command.equals("_bbsgetfav"))
        {
            showFriendsList(activeChar);    
        }
        else if (command.startsWith("_bbsgetfav;playerdelete;"))
        {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            String name = st.nextToken();
            
            deleteFriend(activeChar, name);
            showFriendsList(activeChar);
        }
        else if (command.startsWith("_bbsgetfav;playeradd;"))
        {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            String name = st.nextToken();
            
            addFriend(activeChar, name);
            showFriendsList(activeChar);
        }
        else if (command.startsWith("_bbsgetfav;playerinfo;"))
        {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            st.nextToken();
            String name = st.nextToken();
            
            showFriendsPI(activeChar, name);
        }
        else
        {
            if(Config.COMMUNITY_TYPE.toLowerCase().equals("old"))
            {
                showFriendsList(activeChar);    
            }
            else
            {
                ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: "+command+" is not implemented yet</center><br><br></body></html>","101");
                activeChar.sendPacket(sb);
                activeChar.sendPacket(new ShowBoard(null,"102"));
                activeChar.sendPacket(new ShowBoard(null,"103"));
            }
        }
    }

    /**
     * @param activeChar
     * @param name
     */
    private void showFriendsPI(L2PcInstance activeChar, String name)
    {
        TextBuilder htmlCode = new TextBuilder("<html><body><br>");
        htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");        
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
            htmlCode.append("<table border=0><tr><td>"+player.getName()+" ("+sex+" "+player.getTemplate().getClassName()+"):</td></tr>");
            htmlCode.append("<tr><td>Level: "+levelApprox+"</td></tr>");
            htmlCode.append("<tr><td><br></td></tr>");
            
            if (activeChar != null && (activeChar.isGM() || player.getObjectId() == activeChar.getObjectId()
                    || Config.SHOW_LEVEL_COMMUNITYBOARD))
            {
                long nextLevelExp = 0;
                long nextLevelExpNeeded = 0;
                if (player.getLevel() < 75)
                {
                    nextLevelExp = Experience.LEVEL[player.getLevel() + 1];
                    nextLevelExpNeeded = nextLevelExp-player.getExp();
                }
                
                htmlCode.append("<tr><td>Level: "+player.getLevel()+"</td></tr>");
                htmlCode.append("<tr><td>Experience: "+player.getExp()+"/"+nextLevelExp+"</td></tr>");
                htmlCode.append("<tr><td>Experience needed for level up: "+nextLevelExpNeeded+"</td></tr>");
                htmlCode.append("<tr><td><br></td></tr>");
            }
            
            int uptime = (int)player.getUptime()/1000;
            int h = uptime/3600;
            int m = (uptime-(h*3600))/60;
            int s = ((uptime-(h*3600))-(m*60));
            
            htmlCode.append("<tr><td>Uptime: "+h+"h "+m+"m "+s+"s</td></tr>");
            htmlCode.append("<tr><td><br></td></tr>");
            
            if (player.getClan() != null)
            {
                htmlCode.append("<tr><td>Clan: "+player.getClan().getName()+"</td></tr>");
                htmlCode.append("<tr><td><br></td></tr>");
            }
            
            htmlCode.append("<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"Send PM\" action=\"Write Region PM "+player.getName()+" pm pm pm\" width=110 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr><tr><td><br><button value=\"Back\" action=\"bypass _bbsgetfav\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
            htmlCode.append("</td></tr></table>");          
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
    private void showFriendsList(L2PcInstance activeChar)
    {       
        TextBuilder htmlCode = new TextBuilder("<html><body><br>");
        //add new friend
        htmlCode.append("<table border=0>");
        //htmlCode.append("<tr><td>Add new friend:</td><td><edit var= \"friendname\" width=50></td><td><button value=\"Add Friend\" action=\"bypass -h _bbsgetfav;playeradd;$friendname\" width=70 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
        htmlCode.append("<tr><td>To add someone to your friendlist type '/addfriend friendname'</td></tr>");
        htmlCode.append("<tr><td>You can remove someone from your friendlist only when your friend is online</td></tr>");
        htmlCode.append("</table><br>");
        //show friendlist
        htmlCode.append("<table border=0>");
        htmlCode.append("<tr><td align=left FIXWIDTH=150>Your Friends List</td><td></td></tr>");
        for(Map.Entry<Integer, String> _friend : L2FriendList.getFriendList(activeChar).entrySet())
        {
            int friendId = _friend.getKey();
            String friendName = _friend.getValue();
            
            L2PcInstance friend = (L2PcInstance)L2World.getInstance().findObject(friendId);
            if (friend == null)
                htmlCode.append("<tr><td align=left valign=top FIXWIDTH=150>"+friendName+"</td><td>Offline</td><td></td></tr>");
            else
                htmlCode.append("<tr><td align=left valign=top FIXWIDTH=150><a action=\"bypass _bbsgetfav;playerinfo;" + friendName + "\">"+friendName+"</a></td><td>Online</td><td><a action=\"bypass _bbsgetfav;playerdelete;" + friendName + "\">delete</a></td></tr>");
        }
        htmlCode.append("</table>");
        htmlCode.append("</body></html>");
        
        separateAndSend(htmlCode.toString(),activeChar);        	
    }

    /**
     * @param activeChar
     */
    private void deleteFriend(L2PcInstance activeChar, String name)
    {   
    	L2FriendList.removeFromFriendList(activeChar, name);
    }
    
    /**
     * @param activeChar
     */
    private void addFriend(L2PcInstance activeChar, String name)
    {   
    	L2PcInstance friend = L2World.getInstance().getPlayer(name);
    	
	    FriendAddRequest ajf = new FriendAddRequest(activeChar.getName());
	    friend.sendPacket(ajf);  
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
                htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>L2J Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");

                try
                {
                    
                    L2PcInstance reciever = L2World.getInstance().getPlayer(ar2);
                    if (reciever == null)
                    {
                        htmlCode.append("Player not found!<br><button value=\"Back\" action=\"bypass _bbsgetfav;playerinfo;"+ar2+"\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
                        htmlCode.append("</td></tr></table></body></html>");
                        separateAndSend(htmlCode.toString(),activeChar);
                        return;
                    }
                    
                    if (!reciever.getMessageRefusal())
                    {
                        if (Config.LOG_CHAT)  
                        { 
                            _logChat.info("WHISPER[" + activeChar.getName() + " to "+reciever.getName()+"]" + ar3); 
                        }
                        CreatureSay cs = new CreatureSay(activeChar.getObjectId(), SystemChatChannelId.Chat_Tell.getId(), activeChar.getName(), ar3);
                        reciever.sendPacket(cs);
                        activeChar.sendPacket(cs);
                        htmlCode.append("Message Sent<br><button value=\"Back\" action=\"bypass _bbsgetfav;playerinfo;"+reciever.getName()+"\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
                        htmlCode.append("</td></tr></table></body></html>");
                        separateAndSend(htmlCode.toString(),activeChar)  ;
                    }
                    else
                    {
                        SystemMessage sm = new SystemMessage(SystemMessageId.THE_PERSON_IS_IN_MESSAGE_REFUSAL_MODE);
                        activeChar.sendPacket(sm);
                        parsecmd("_bbsgetfav;playerinfo;"+reciever.getName(), activeChar);
                    }
                }
                catch (StringIndexOutOfBoundsException e)
                {
                    // ignore
                }
        }
        else
        {
            ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: "+ar1+" is not implemented yet</center><br><br></body></html>","101");
            activeChar.sendPacket(sb);
            activeChar.sendPacket(new ShowBoard(null,"102"));
            activeChar.sendPacket(new ShowBoard(null,"103"));  
        }
    }
    private static FriendsBBSManager _instance = null;
    /**
     * @return
     */
    public static FriendsBBSManager getInstance()
    {
        if(_instance == null)
        {
            _instance = new FriendsBBSManager();
        }
        return _instance;
    }
}
