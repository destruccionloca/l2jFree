/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 * 
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

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.MapRegionManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.mapregion.L2MapRegion;
import net.sf.l2j.gameserver.model.mapregion.L2MapRegionRestart;
import net.sf.l2j.gameserver.model.mapregion.TeleportWhereType;

/**
 * @author Noctarius
 *
 */
public class AdminRegion implements IAdminCommandHandler
{
    private static final int REQUIRED_LEVEL = Config.GM_TEST;
    private static final String[] ADMIN_COMMANDS =
    {
        "admin_region_check" 
    };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (activeChar == null) return false;

        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (activeChar.getAccessLevel() < REQUIRED_LEVEL) return false;
        
        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken();

        if (actualCommand.equalsIgnoreCase("admin_region_check"))
        {
        	L2MapRegion region = MapRegionManager.getInstance().getRegion(activeChar);
        	
        	if (region != null)
        	{
        		L2MapRegionRestart restart = MapRegionManager.getInstance().getRestartLocation(region.getRestartId(activeChar.getRace()));
        		
        		activeChar.sendMessage("Actual region: "+region.getId());
        		activeChar.sendMessage("Respawn position will be: "+restart.getName()+" ("+restart.getLocName()+")");
        		
        		if (restart.getBannedRace() != null)
        		{
        			L2MapRegionRestart redirect = MapRegionManager.getInstance().getRestartLocation(restart.getRedirectId());
        			activeChar.sendMessage("Banned race: "+restart.getBannedRace().name());
        			activeChar.sendMessage("Redirect To: "+redirect.getName()+" ("+redirect.getLocName()+")");
        		}

                Location loc;
                loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Castle);
                activeChar.sendMessage("TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

                loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.ClanHall);
                activeChar.sendMessage("TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

                loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SiegeFlag);
                activeChar.sendMessage("TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

                loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.Town);
                activeChar.sendMessage("TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

                String nearestTown = TownManager.getInstance().getClosestTownName(activeChar);
                Announcements.getInstance().announceToAll(activeChar.getName() + " has tried spawn-announce near " + nearestTown + "!");
        	}
        }
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
     */
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }
}
