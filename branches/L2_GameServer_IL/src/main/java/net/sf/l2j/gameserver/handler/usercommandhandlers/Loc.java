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
package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.instancemanager.MapRegionManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Town;
import net.sf.l2j.gameserver.model.mapregion.L2MapRegion;
import net.sf.l2j.gameserver.model.mapregion.L2MapRegionRestart;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * 
 *
 */
public class Loc implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 0 }; 

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(@SuppressWarnings("unused") int id, L2PcInstance activeChar)
    {
    	L2MapRegionRestart restart = null;
    	SystemMessageId msg = SystemMessageId.LOC_ADEN_S1_S2_S3;

    	//Standard Town
    	Town town = TownManager.getInstance().getTown(Config.ALT_DEFAULT_RESTARTTOWN);
    	if (town != null && town.getMapRegion() != null)
    	{
    		restart = MapRegionManager.getInstance().getRestartLocation(town.getMapRegion().getRestartId()); 
    		
    		if (restart != null)
    			msg = SystemMessageId.getSystemMessageId(restart.getLocName());
    	}

    	L2MapRegion region = MapRegionManager.getInstance().getRegion(activeChar);
    	if (region != null)
    	{
    		int restartId = region.getRestartId();
    		restart = MapRegionManager.getInstance().getRestartLocation(restartId);
    		msg = SystemMessageId.getSystemMessageId(restart.getLocName());
    	}
    	
        SystemMessage sm = new SystemMessage(msg);
        sm.addNumber(activeChar.getX());
        sm.addNumber(activeChar.getY());
        sm.addNumber(activeChar.getZ());
        activeChar.sendPacket(sm);
        
        if (restart != null)
        {
        	if (restart.getLocName() < 1222)
        	{
        		if (restart.getLocName() != 943)
        		{
        			SystemMessage message = new SystemMessage(SystemMessageId.getSystemMessageId(msg.getId()+31));
        			activeChar.sendPacket(message);
        		}
        		else
        			activeChar.sendMessage("Restart at the Town of Gludio.");        			
        	}
        	else
        		if (SystemMessageId.LOC_GM_CONSULATION_SERVICE_S1_S2_S3.getId() == restart.getLocName())
        			activeChar.sendMessage("Restart at the GM Consulation Service.");
        		else if (SystemMessageId.LOC_RUNE_S1_S2_S3.getId() == restart.getLocName())
        			activeChar.sendMessage("Restart at Rune Township.");
        		else if (SystemMessageId.LOC_GODDARD_S1_S2_S3.getId() == restart.getLocName())
        			activeChar.sendMessage("Restart at the Town of Goddard.");
        		else if (SystemMessageId.LOC_DIMENSIONAL_GAP_S1_S2_S3.getId() == restart.getLocName())
        			activeChar.sendMessage("Restart at the Dimensional Gap.");
        		else if (SystemMessageId.LOC_CEMETARY_OF_THE_EMPIRE_S1_S2_S3.getId() == restart.getLocName())
        			activeChar.sendMessage("Restart at the Cemetary of the Empire.");
        		else if (SystemMessageId.LOC_SCHUTTGART_S1_S2_S3.getId() == restart.getLocName())
        			activeChar.sendMessage("Restart at the Town of Schuttgart.");
        		else if (SystemMessageId.LOC_PRIMEVAL_ISLE_S1_S2_S3.getId() == restart.getLocName())
        			activeChar.sendMessage("Restart at the Primeval Isle.");
        }

        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
