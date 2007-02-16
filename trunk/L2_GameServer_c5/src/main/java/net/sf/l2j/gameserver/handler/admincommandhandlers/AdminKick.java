package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.LeaveWorld;

public class AdminKick implements IAdminCommandHandler {
    //private final static Log _log = LogFactory.getLog(AdminKick.class.getName());
    private static String[] _adminCommands = {"admin_kick" ,"admin_kick_non_gm"};
    private static final int REQUIRED_LEVEL = Config.GM_KICK;
    
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {

        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
                return false;
        }
        
        String target = (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target");
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");        
        
        if (command.startsWith("admin_kick"))
        {
            StringTokenizer st = new StringTokenizer(command);
            if (st.countTokens() > 1)
            {
                st.nextToken();
                String plyr = st.nextToken();
                L2PcInstance player = L2World.getInstance().getPlayer(plyr);
                if (player != null)
                {
                    kickPlayer (player);
                }
            }
        }
        if (command.startsWith("admin_kick_non_gm"))
        {
            int counter = 0;
            for (L2PcInstance player : L2World.getInstance().getAllPlayers())
            {
                if(!player.isGM())
                {
                    counter++;
                    kickPlayer (player);
                }
            }
            activeChar.sendMessage("Kicked "+counter+" players");
        }
        return true;
    }
        
    private void kickPlayer (L2PcInstance player)
    {
        try {
            ClientThread.saveCharToDisk(player);
            player.sendPacket(new LeaveWorld());
            player.deleteMe();
            player.logout();
            } catch (Throwable t)   {}
 
        try {
            player.closeNetConnection();
            } catch (Throwable t)   {} 
    }
    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }
}
