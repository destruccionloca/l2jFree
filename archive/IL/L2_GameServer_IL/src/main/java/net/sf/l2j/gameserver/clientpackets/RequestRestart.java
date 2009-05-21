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

import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.serverpackets.RestartResponse;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;
import net.sf.l2j.gameserver.Olympiad;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

        if(player.atEvent) {
            player.sendMessage("A superior power doesn't allow you to leave the event.");
            return;
        }
        
        // prevent from player disconnect when in Olympiad mode
        if (player.isInOlympiadMode() || Olympiad.getInstance().isRegistered(player) || player.getOlympiadGameId()!=-1)
        {
            if (_log.isDebugEnabled()) _log.debug("Player " + player.getName() + " tried to logout while in Olympiad");
            player.sendMessage("You can't restart when in Olympiad.");
            player.sendPacket(new ActionFailed());
            return;
        }

        if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
        {
            if (_log.isDebugEnabled())
                _log.debug("Player " + player.getName() + " tried to logout while fighting.");

            player.sendPacket(new SystemMessage(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING));
            player.sendPacket(new ActionFailed());
            return;
        }

        if (player.getPet() != null && !player.isBetrayed() && (player.getPet() instanceof L2PetInstance))
        {
            L2PetInstance pet = (L2PetInstance)player.getPet();

            if (pet.isAttackingNow())
            {
                pet.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE));
                player.sendPacket(new ActionFailed());
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
                player.sendPacket(new ActionFailed());
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
            if(ZoneManager.getInstance().checkIfInZone(ZoneType.NoEscape,player)){
                player.sendMessage("You can not restart in here.");
                player.sendPacket(new ActionFailed());
                return;                   
            }
        }
        
        if(player.isFlying())
        {
            player.sendMessage("You can not restart while flying.");
            player.sendPacket(new ActionFailed());
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
        player.getInventory().updateDatabase();

        L2GameClient client = getClient();

        // detach the client from the char so that the connection isnt closed in the deleteMe
        player.setClient(null);
        
        RegionBBSManager.getInstance().changeCommunityBoard();
        
        player.deleteMe();
        L2GameClient.saveCharToDisk(getClient().getActiveChar());

        // removing player from the world
        getClient().setActiveChar(null);
        
        // return the client to the authed status
        client.setState(GameClientState.AUTHED);

        RestartResponse response = new RestartResponse();
        sendPacket(response);    
        
        // send char list
        CharSelectInfo cl = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1);
        sendPacket(cl);
        getClient().setCharSelection(cl.getCharInfo());
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__46_REQUESTRESTART;
    }
}
