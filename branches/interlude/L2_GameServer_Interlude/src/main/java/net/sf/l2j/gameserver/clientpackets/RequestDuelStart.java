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
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ExDuelAskStart;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  sample
 *  29 
 *  42 00 00 10 
 *  01 00 00 00
 * 
 *  format  cdd
 * 
 * 
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestDuelStart extends ClientBasePacket
{
	private static final String _C__29_REQUESTJOINPARTY = "[C] 29 RequestDuelStart";
	private final static Log _log = LogFactory.getLog(RequestJoinParty.class.getName());
	
	private final String _name;
	private final int _duelType;

    public RequestDuelStart(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);

        _name = readS();
        _duelType = readD();
	}

	void runImpl()
	{
        L2PcInstance requestor = getClient().getActiveChar();
        L2PcInstance target = L2World.getInstance().getPlayer(_name);
        
		if (requestor == null)
		    return;
        
        if (target == null)
        {
            requestor.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
            return;
        }
        
        if(target.getClientRevision() < 729)
        {
            requestor.sendMessage("A player with C5 Client cant be asked for duell");
            return;
        }
        
        if (target.isCursedWeaponEquiped() || requestor.isCursedWeaponEquiped())
        {
            requestor.sendMessage("A player wielding a Cursed Weapon can't participate in a duel");
            return;
        }
        
        SystemMessage msg;
        
		if (target.isDuelling() >0 ) 
        {
			requestor.sendMessage("That player is already duelling");
			return;
		}
        
        if (requestor.isDuelling()>0)
        {
            requestor.sendMessage("You are already duelling");
            return;
        }

		if (target == requestor) 
        {
			msg = new SystemMessage(SystemMessage.INCORRECT_TARGET);
			msg.addString(target.getName());
			requestor.sendPacket(msg);
			return;
		}

        if (target.isInOlympiadMode() || requestor.isInOlympiadMode())
            return;        
        
		/*if (!requestor.isInParty())
            //asker has no party
			createNewParty(target, requestor);
		else
            //asker has a party
			addTargetToParty(target, requestor);*/
        createDuel(target, requestor);
	}

	/**
	 * @param client
	 * @param duelType
	 * @param target
	 * @param requestor
	 */
	private void createDuel(L2PcInstance target, L2PcInstance requestor)
	{
       
       SystemMessage msg;
       
       if (_duelType>0 && (requestor.getParty()==null || target.getParty()==null))
       {
           requestor.sendMessage("You can't ask for a party duel if not both players are in one!");
           return;
       }
       if (_duelType>0 && (!requestor.getParty().isLeader(requestor) || !target.getParty().isLeader(target)))
       {
           requestor.sendMessage("Only part leaders may start up a duel");
           return;
       }
       if (_duelType>0 && (requestor.getParty().getMemberCount()-target.getParty().getMemberCount() < -3 || requestor.getParty().getMemberCount()-target.getParty().getMemberCount() >3)) //Not sure about this one, but it would make sense to me.
       {
           requestor.sendMessage("Your parties are too unequally matched to participate in a duel");
           return;
       }
       if (_duelType==0 && (requestor.getParty()!=null || target.getParty()!=null))
       {
           requestor.sendMessage("You can't ask for a duel if one of the players is in a party!");
           return;
       }

       if (!target.isProcessingRequest())
        {           
           requestor.onTransactionRequest(target);
           target.sendPacket(new ExDuelAskStart(requestor.getName(), _duelType));
           
           if (_log.isDebugEnabled())
               _log.debug("sent out a duel invitation to:"+target.getName());
           
           requestor.sendMessage("Your invitation to duel was sent");
		}
		else
		{
           msg = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
           msg.addString(target.getName());
           requestor.sendPacket(msg);
           
           if (_log.isDebugEnabled())
               _log.warn(requestor.getName() + " already received a duel invitation");
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__29_REQUESTJOINPARTY;
	}
}
