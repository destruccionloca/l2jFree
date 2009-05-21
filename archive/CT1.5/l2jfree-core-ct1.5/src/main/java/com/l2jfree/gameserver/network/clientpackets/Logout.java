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
package com.l2jfree.gameserver.network.clientpackets;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.Olympiad;
import com.l2jfree.gameserver.SevenSignsFestival;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.model.L2Party;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.LeaveWorld;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class Logout extends L2GameClientPacket
{
    private static final String _C__09_LOGOUT = "[C] 09 Logout";
    private final static Log _log = LogFactory.getLog(Logout.class.getName());
    
    // c

    /**
     * @param decrypt
     */
    @Override
    protected void readImpl()
    {

    }

    @Override
    protected void runImpl()
    {
        // Dont allow leaving if player is fighting
        L2PcInstance player = getClient().getActiveChar();
        
        if (player == null)
            return;

        player.sendPacket(ActionFailed.STATIC_PACKET);
        // [L2J_JP ADD START]
        if (!(player.isGM()))
        {
            if(player.isInsideZone(L2Zone.FLAG_NOESCAPE))
            {
                player.sendPacket(new SystemMessage(SystemMessageId.NO_LOGOUT_HERE));
                return;
            }
        }

        if(player.isFlying())
        {
            player.sendMessage("You can not log out while flying.");
            return;
        }
        // [L2J_JP ADD END]

        if(AttackStanceTaskManager.getInstance().getAttackStanceTask(player) && !player.isGM())
        {
            if (_log.isDebugEnabled()) _log.debug("Player " + player.getName() + " tried to logout while fighting");
            
            player.sendPacket(new SystemMessage(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING));
            return;
        }
        
        if (player.getPet() != null && !player.getPet().isBetrayed() && (player.getPet() instanceof L2PetInstance))
        {
            L2PetInstance pet = (L2PetInstance)player.getPet();

            if (pet.isAttackingNow())
            {
                pet.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE));
                return;
            }
            pet.unSummon(player);
        }
        
        if(player.atEvent)
        {
            player.sendMessage("A superior power doesn't allow you to leave the event.");
            return;
        }
        
        if (player.isInOlympiadMode() || Olympiad.getInstance().isRegistered(player))
        {
            player.sendMessage("You can't logout in olympiad mode.");
            return;
        }
        
        // Prevent player from logging out if they are a festival participant
        // and it is in progress, otherwise notify party members that the player
        // is not longer a participant.
        if (player.isFestivalParticipant())
        {
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

        //save character
        L2GameClient.saveCharToDisk(player, true);
        sendPacket(LeaveWorld.STATIC_PACKET);
        player.deleteMe();

        // prevent deleteMe from being called a second time on disconnection
        getClient().setActiveChar(null);

        // normally the server would send serveral "delete object" before "leaveWorld"
        // we skip that for now
    }

    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__09_LOGOUT;
    }
}
