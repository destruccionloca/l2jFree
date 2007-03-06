/* This program is free software; you can redistribute it and/or modify */
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.FactionManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.faction.Faction;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

/** 
 * @author evill33t
 * 
 */
public class faction implements IVoicedCommandHandler
{
    //private static final Log _log = LogFactory.getLog(Wedding.class);
    private static String[] _voicedCommands = { "faction" };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
        if(activeChar.getNPCFaction()!=null)
        {
                NpcHtmlMessage factionInfo = new NpcHtmlMessage(5);

                int factionId = activeChar.getNPCFaction().getFactionId();
                Faction faction = FactionManager.getInstance().getFactions(factionId);
                TextBuilder replyMSG = new TextBuilder("<html><body>");
                replyMSG.append("<HTML><HEAD><BODY>");
                replyMSG.append("faction id" + String.valueOf(factionId)+"<br>");
                replyMSG.append("faction name" + faction.getName()+"<br>");
                replyMSG.append("faction points" + activeChar.getNPCFactionPoints()+"<br>");
                replyMSG.append("faction side" + String.valueOf(faction.getSide())+"<br>");
                replyMSG.append("</BODY></HTML>");
                factionInfo.setHtml(replyMSG.toString());
                activeChar.sendPacket(factionInfo);
                return true;
        }
        else
            return false;
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public String[] getVoicedCommandList()
    {
        return _voicedCommands;
    }
}    
