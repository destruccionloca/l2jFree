package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.LeaveWorld;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class AdminKick implements IAdminCommandHandler {
    //private final static Log _log = LogFactory.getLog(AdminKick.class.getName());
    private static String[] _adminCommands = {"admin_kick" ,"admin_kick_non_gm"};
    private static final int REQUIRED_LEVEL = Config.GM_KICK;
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {

        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
    		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
            {
                //_log.debugr("Not required level");
                return false;
            }
        }
        
        String target = (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target");
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");        
        
        if (command.startsWith("admin_kick"))
        {
            //_log.debugr("ADMIN KICK");
            StringTokenizer st = new StringTokenizer(command);
            //_log.debugr("Tokens: "+st.countTokens());
            if (st.countTokens() > 1)
            {
                st.nextToken();
                String player = st.nextToken();
                //_log.debugr("Player1 "+player);
                L2PcInstance plyr = L2World.getInstance().getPlayer(player);
                if (plyr != null)
                {
                    //_log.debugr("Player2 "+plyr.getName());
                    plyr.logout();
                    SystemMessage sm = new SystemMessage(614);
                    sm.addString("You kicked " + plyr.getName() + " from the game.");
                    activeChar.sendPacket(sm);
                    sm = null;
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
        			player.sendPacket(new LeaveWorld());
        			player.logout();
        		}
            }
        	activeChar.sendMessage("Kicked "+counter+" players");
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }
}
