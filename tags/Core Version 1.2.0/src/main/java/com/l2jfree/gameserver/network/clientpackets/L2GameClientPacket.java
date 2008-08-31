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

import org.mmocore.network.ReceivablePacket;

import com.l2jfree.gameserver.GameServer;
import com.l2jfree.gameserver.GameTimeController;
import com.l2jfree.gameserver.exception.L2JFunctionnalException;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * Packets received by the game server from clients
 * @author  KenM
 */
public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	private final static Log _log = LogFactory.getLog(L2GameClientPacket.class.getName());
	
	@Override
	protected boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch (Throwable t)
		{
			_log.fatal("Client: "+getClient().toString()+" - Failed reading: "+getType()+" - L2J Server Version: "+GameServer.getVersionNumber(),t);
		}
		return false;
	}
	
	protected abstract void readImpl();
	
	@Override
	public void run() 
	{
		try
		{
			// flood protection
			if (GameTimeController.getGameTicks() - getClient().packetsSentStartTick > 10)
			{
				getClient().packetsSentStartTick = GameTimeController.getGameTicks();
				getClient().packetsSentInSec = 0;
			}
			else
			{
				getClient().packetsSentInSec++;
				// Client sends NORMALLY very often 50+ packets...
				if (getClient().packetsSentInSec > 50 && getClient().getActiveChar() != null)
				{
					getClient().getActiveChar().sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			
			runImpl();

			if (getClient().getActiveChar() != null && getClient().getActiveChar().getProtection() > 0)
			{
				if (this instanceof MoveBackwardToLocation 
					|| this instanceof AttackRequest 
					|| this instanceof RequestMagicSkillUse)
					// could include pickup and talk too, but less is better
				{
					// Removes onspawn protection - player has faster computer than
					// average
					getClient().getActiveChar().onActionRequest();
				}
			}
		}
		catch (Throwable t)
		{
			_log.fatal("Client: "+getClient().toString()+" - Failed running: "+this.getType()+" - L2J Server Version: "+GameServer.getVersionNumber(),t);
		}
	}
	
	protected abstract void runImpl()  throws L2JFunctionnalException;
	
	protected final void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}
	
	public void sendPacket(SystemMessageId sm)
	{
		getClient().sendPacket(sm.getSystemMessage());
	}
	/**
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();
}