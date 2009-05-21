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
import com.l2jfree.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfree.gameserver.model.L2Party;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfree.gameserver.model.zone.L2Zone;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.L2GameClient.GameClientState;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.CharSelectionInfo;
import com.l2jfree.gameserver.network.serverpackets.RestartResponse;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestRestart extends L2GameClientPacket
{
    private static final String _C__46_REQUESTRESTART = "[C] 46 RequestRestart";
    private final static Log _log = LogFactory.getLog(RequestRestart.class.getName());

    /**
     * packet type id 0x46
     * format:      c
     * @param decrypt
     */
    @Override
    protected void readImpl()
    {
        // trigger
    }

    @Override
    protected void runImpl()
    {

        L2PcInstance player = getClient().getActiveChar();
        if (player == null)
        {
            _log.warn("[RequestRestart] activeChar null!?");
            return;
        }

        if(player.atEvent)
        {
            player.sendMessage("A superior power doesn't allow you to leave the event.");
            return;
        }
        
        // prevent from player disconnect when in Olympiad mode
        if (player.isInOlympiadMode() || Olympiad.getInstance().isRegistered(player) || player.getOlympiadGameId()!=-1)
        {
            if (_log.isDebugEnabled()) _log.debug("Player " + player.getName() + " tried to logout while in Olympiad");
            player.sendMessage("You can't restart when in Olympiad.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player) && !player.isGM())
        {
            if (_log.isDebugEnabled())
                _log.debug("Player " + player.getName() + " tried to logout while fighting.");

            player.sendPacket(new SystemMessage(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING));
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (player.getPet() != null && !player.getPet().isBetrayed() && (player.getPet() instanceof L2PetInstance))
        {
            L2PetInstance pet = (L2PetInstance)player.getPet();

            if (pet.isAttackingNow())
            {
                pet.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE));
                player.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            } 
            pet.unSummon(player);
        }
        // Prevent player from restarting if they are a festival participant
        // and it is in progress, otherwise notify party members that the player
        // is not longer a participant.
        if (player.isFestivalParticipant())
        {
            if (SevenSignsFestival.getInstance().isFestivalInitialized())
            {
                player.sendMessage("You cannot restart while you are a participant in a festival.");
                player.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
            L2Party playerParty = player.getParty();

            if (playerParty != null)
                player.getParty().broadcastToPartyMembers(SystemMessage.sendString(player.getName()
                                                              + " has been removed from the upcoming festival."));
        }

        // [L2J_JP ADD START]
        if (!(player.isGM()))
        {
            if(player.isInsideZone(L2Zone.FLAG_NOESCAPE))
            {
                player.sendPacket(new SystemMessage(SystemMessageId.NO_RESTART_HERE));
                player.sendPacket(ActionFailed.STATIC_PACKET);
                return;
            }
        }
        
        if(player.isFlying())
        {
            player.sendMessage("You can not restart while flying.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        // [L2J_JP ADD END]
        
        if (player.getPrivateStoreType() != 0)
        {
            player.sendMessage("Cannot restart while trading.");
            return;
        }
        
        if (player.getActiveRequester() != null)
        {
            player.getActiveRequester().onTradeCancel(player);
            player.onTradeCancel(player.getActiveRequester());
        }

        if (player.isTeleporting())
        {
            player.abortCast();
            player.setIsTeleporting(false);
        }

        player.getInventory().updateDatabase();

        L2GameClient client = getClient();

        // detach the client from the char so that the connection isnt closed in the deleteMe
        player.setClient(null);

        //save character
        L2GameClient.saveCharToDisk(player, true);
        player.deleteMe();

        // prevent deleteMe from being called a second time on disconnection
        getClient().setActiveChar(null);
        
        // return the client to the authed status
        client.setState(GameClientState.AUTHED);

        RestartResponse response = new RestartResponse();
        sendPacket(response);
        
        // send char list
        CharSelectionInfo cl = new CharSelectionInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1);
        sendPacket(cl);
        getClient().setCharSelection(cl.getCharInfo());

        RegionBBSManager.getInstance().changeCommunityBoard();
    }

    /* (non-Javadoc)
     * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__46_REQUESTRESTART;
    }
}
