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

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.entity.ZoneType;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.LeaveWorld;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class Logout extends ClientBasePacket
{
    private static final String _C__09_LOGOUT = "[C] 09 Logout";
    private final static Log _log = LogFactory.getLog(Logout.class.getName());
    
    // c

    /**
     * @param decrypt
     */
    public Logout(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        // this is just a trigger packet. it has no content
    }

    void runImpl()
    {
        // Dont allow leaving if player is fighting
        L2PcInstance player = getClient().getActiveChar();
        
        if (player == null)
            return;

        // [L2J_JP ADD START]
        if (!(player.isGM()))
        {
            if(ZoneManager.getInstance().checkIfInZone(ZoneType.ZoneTypeEnum.NoEscape.toString(),player)){
                player.sendPacket(SystemMessage.sendString("You can not log out in here."));
                player.sendPacket(new ActionFailed());
                return;                   
            }
        }
	
        if(player.isFlying())
        {
            player.sendPacket(SystemMessage.sendString("You can not log out while flying."));
            player.sendPacket(new ActionFailed());
            return;                   
        }
        // [L2J_JP ADD END]

        if(AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
        {
            if (_log.isDebugEnabled()) _log.debug("Player " + player.getName() + " tried to logout while fighting");
            
            player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_EXIT_WHILE_IN_COMBAT));
            player.sendPacket(new ActionFailed());
            return;
        }
        
        if (player.getPet() != null && !player.isBetrayed() && (player.getPet() instanceof L2PetInstance))
        {
        	L2PetInstance pet = (L2PetInstance)player.getPet();

            if (pet.isAttackingNow())
            {
            	pet.sendPacket(new SystemMessage(SystemMessage.PET_CANNOT_SENT_BACK_DURING_BATTLE));
                player.sendPacket(new ActionFailed());
                return;
            } 
            else
             pet.unSummon(player);
        }
        
        if(player.atEvent) {
            player.sendPacket(SystemMessage.sendString("A superior power doesn't allow you to leave the event."));
            return;
        }
        
        // prevent from player disconnect when in Olympiad mode
        if(player.isInOlympiadMode()) {
        	if (_log.isDebugEnabled()) _log.debug("Player " + player.getName() + " tried to logout while in Olympiad");
            player.sendPacket(SystemMessage.sendString("You can't disconnect when in Olympiad."));
            player.sendPacket(new ActionFailed());
            return;
        }
        
        // Prevent player from logging out if they are a festival participant
        // and it is in progress, otherwise notify party members that the player
        // is not longer a participant.
        if (player.isFestivalParticipant()) {
            if (SevenSignsFestival.getInstance().isFestivalInitialized()) 
            {
                player.sendMessage("You cannot log out while you are a participant in a festival.");
                return;
            }
            L2Party playerParty = player.getParty();
            
            if (playerParty != null)
                player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName() + " has been removed from the upcoming festival."));
        }
        if (player.isFlying()) 
        { 
           player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
        }
    	
        if (player.getPrivateStoreType() != 0)
        {
            player.sendMessage("Cannot log out while trading.");
            return;
        }
        
        if (player.getActiveRequester() != null)
        {
            player.getActiveRequester().onTradeCancel(player);
            player.onTradeCancel(player.getActiveRequester());
        }
        
        player.getInventory().updateDatabase();
        player.deleteMe();

        //save character
        ClientThread.saveCharToDisk(player);
        
        // normally the server would send serveral "delete object" before "leaveWorld"
        // we skip that for now
        sendPacket(new LeaveWorld());
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__09_LOGOUT;
    }
}