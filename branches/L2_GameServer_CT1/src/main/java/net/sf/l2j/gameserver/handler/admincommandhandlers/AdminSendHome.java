package net.sf.l2j.gameserver.handler.admincommandhandlers;

//import org.apache.commons.logging.Log;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminSendHome implements IAdminCommandHandler {
    //private final static Log _log = LogFactory.getLog(AdminSendHome.class.getName());
    private static String[] _adminCommands = {"admin_sendhome"};
    private static final int REQUIRED_LEVEL = Config.GM_TELEPORT;

    public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
        	return false;
        
        if (command.startsWith("admin_sendhome"))
        {
            if(command.split(" ").length > 1)
                handleSendhome(activeChar, command.split(" ")[1]);
            else
            	handleSendhome(activeChar);
        }
        
        return true;
    }
    
    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }
    
    private void handleSendhome(L2PcInstance activeChar)
    {
        handleSendhome(activeChar, null);
    }
    
    private void handleSendhome(L2PcInstance activeChar, String player) {
        L2Object obj = activeChar.getTarget();
        
        if (player != null)
        {
            L2PcInstance plyr = L2World.getInstance().getPlayer(player);
            
            if (plyr != null)
            {
                obj = plyr;
            }
        }
        
        if (obj == null)
            obj = activeChar;
        
        if ((obj != null) && (obj instanceof L2Character)) 
            doSendhome((L2Character)obj);
        else 
            activeChar.sendMessage("Incorrect target.");
    }
    
    private void doSendhome(L2Character targetChar)
    {
    	targetChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
    }
    
}