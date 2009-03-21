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

import java.nio.BufferUnderflowException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.ReceivablePacket;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.IOFloodManager;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.IOFloodManager.ErrorMode;
import com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * Packets received by the game server from clients
 * 
 * @author KenM
 */
public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	protected static final Log _log = LogFactory.getLog(L2GameClientPacket.class);
	
	@Override
	protected final boolean read()
	{
		if (getAvaliableBytes() < getMinimumLength())
		{
			IOFloodManager.getInstance().report(ErrorMode.BUFFER_UNDER_FLOW, getClient(), this, null);
			return false;
		}
		
		try
		{
			readImpl();
			return true;
		}
		catch (BufferUnderflowException e)
		{
			IOFloodManager.getInstance().report(ErrorMode.BUFFER_UNDER_FLOW, getClient(), this, e);
		}
		catch (Exception e)
		{
			IOFloodManager.getInstance().report(ErrorMode.FAILED_READING, getClient(), this, e);
		}
		
		return false;
	}
	
	protected abstract void readImpl();
	
	@Override
	public final void run()
	{
		try
		{
			final L2PcInstance activeChar = getClient().getActiveChar();
			
			if (activeChar != null && activeChar.getProtection() > 0)
			{
				// could include pickup and talk too, but less is better
				if (this instanceof MoveBackwardToLocation ||
					this instanceof AttackRequest ||
					this instanceof RequestActionUse ||
					this instanceof RequestMagicSkillUse)
				{
					// removes onspawn protection
					activeChar.onActionRequest();
				}
			}
			
			runImpl();
		}
		catch (Exception e)
		{
			IOFloodManager.getInstance().report(ErrorMode.FAILED_RUNNING, getClient(), this, e);
		}
	}
	
	protected abstract void runImpl();
	
	protected final void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}
	
	protected final void sendPacket(SystemMessageId sm)
	{
		getClient().sendPacket(sm.getSystemMessage());
	}
	
	/**
	 * @return a String with this packet name for debuging purposes
	 */
	public abstract String getType();
	
	/**
	 * Should be overriden.
	 */
	protected int getMinimumLength()
	{
		return 0;
	}
}
