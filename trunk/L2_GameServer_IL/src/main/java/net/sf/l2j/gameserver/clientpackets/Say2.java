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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ChatHandler;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.VoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.16.2.12.2.7 $ $Date: 2005/04/11 10:06:11 $
 */
public class Say2 extends L2GameClientPacket
{
    private static final String _C__38_SAY2 = "[C] 38 Say2";
    private final static Log _log = LogFactory.getLog(Say2.class.getName());
    private static Log _logChat = LogFactory.getLog("chat");

    private String _text;
    private SystemChatChannelId _type;
    private String _target;
    /**
     * packet type id 0x38
     * format:      cSd (S)
     * @param decrypt
     */
    @Override
    protected void readImpl()
    {
        _text = readS();
        try
        {
            _type = SystemChatChannelId.getChatType(readD());
        }
        catch (BufferUnderflowException e) 
        {
            _type = SystemChatChannelId.Chat_None;
        }
        _target = _type == SystemChatChannelId.Chat_Tell ? readS() : null;
    }

    @Override
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();

        // If no channel is choosen - return
        if (_type == SystemChatChannelId.Chat_None)
        {
            _log.warn("[Say2.java] Illegal chat channel was used.");
            return;
        }

        if (activeChar == null)
        {
            _log.warn("[Say2.java] Active Character is null.");
            return;
        }
 
        // If player is chat banned
        if (activeChar.isChatBanned())
        {
            if (_type != SystemChatChannelId.Chat_User_Pet && _type !=SystemChatChannelId.Chat_Tell)
            {
                // [L2J_JP EDIT]
                activeChar.sendPacket(new SystemMessage(SystemMessageId.CHATTING_IS_CURRENTLY_PROHIBITED));
                return;
            }
        }
        
        // If player is jailed
        if ((activeChar.isInJail() || ZoneManager.getInstance().checkIfInZone(ZoneType.Jail, activeChar)) && Config.JAIL_DISABLE_CHAT && !activeChar.isGM())
        {
            if (_type != SystemChatChannelId.Chat_User_Pet && _type != SystemChatChannelId.Chat_Normal)
            {
                activeChar.sendMessage("You can not chat with the outside of the jail.");
                return;
            }
        }
        
        // If Petition and GM use GM_Petition Channel
        if (_type == SystemChatChannelId.Chat_User_Pet && activeChar.isGM()) 
            _type = SystemChatChannelId.Chat_GM_Pet;
        
        // Say Filter implementation
        if(Config.USE_SAY_FILTER) 
        {
            for(String pattern : Config.FILTER_LIST)
            {
                    _text = _text.replaceAll(pattern,"^_^");
            }
        }       

        if (_text.startsWith(".") && !_text.startsWith("..") &&
            _type == SystemChatChannelId.Chat_Normal)
        {
            StringTokenizer st = new StringTokenizer(_text);

            if (st.countTokens()>=1)
            {
                String command = st.nextToken().substring(1);
                String params = "";
                if (st.countTokens()==0)
                {
                    if (activeChar.getTarget()!=null) params=activeChar.getTarget().getName();
                }
                else params=st.nextToken().trim();

                IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);

                if (vch != null) 
                    vch.useVoicedCommand(command, activeChar, params);
                else
                    _log.warn("No handler registered for voice command '"+command+"'");
            }
            
            return;
        }
        // Some custom implementation to show how to add channels
        // (for me Chat_System is used for emotes - further informations
        // in ChatSystem.java)
        // else if (_text.startsWith("(")&&
        //		_text.length() >= 5 &&
        //		_type == SystemChatChannelId.Chat_Normal)
        //{
        //	_type = SystemChatChannelId.Chat_System;
        //	
        //	_text = _text.substring(1);
        //	_text = "*" + _text + "*";
        //}
        
        // Log chat to file
        if (Config.LOG_CHAT) 
        {
            if (_type == SystemChatChannelId.Chat_Tell)
                _logChat.info( _type.getName() + "[" + activeChar.getName() + " to "+_target+"] " + _text);
            else
                _logChat.info( _type.getName() + "[" + activeChar.getName() + "] " + _text);
        }
        
        IChatHandler ich = ChatHandler.getInstance().getChatHandler(_type);
        
        if (ich != null)
            ich.useChatHandler(activeChar, _target, _type, _text);
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__38_SAY2;
    }

    public void changeString(String newString) { _text = newString; }
   
    public String getSay() { return _text; }
}
