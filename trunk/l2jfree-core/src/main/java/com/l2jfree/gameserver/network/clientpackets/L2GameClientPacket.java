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

import static com.l2jfree.gameserver.network.serverpackets.ActionFailed.STATIC_PACKET;

import java.nio.BufferUnderflowException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.ReceivablePacket;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.IOFloodManager;
import com.l2jfree.gameserver.network.InvalidPacketException;
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
		catch (RuntimeException e)
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
		catch (InvalidPacketException e)
		{
			IOFloodManager.getInstance().report(ErrorMode.FAILED_RUNNING, getClient(), this, e);
		}
		catch (RuntimeException e)
		{
			IOFloodManager.getInstance().report(ErrorMode.FAILED_RUNNING, getClient(), this, e);
		}
	}
	
	protected abstract void runImpl() throws InvalidPacketException;
	
	protected final void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}
	
	protected final void sendPacket(SystemMessageId sm)
	{
		getClient().sendPacket(sm.getSystemMessage());
	}
	
	protected final L2PcInstance getActiveChar()
	{
		return getClient().getActiveChar();
	}
	
	public String getType()
	{
		return getClass().getSimpleName();
	}
	
	protected final void requestFailed(SystemMessageId sm)
	{
		requestFailed(sm.getSystemMessage());
	}
	
	protected final void requestFailed(L2GameServerPacket gsp)
	{
		sendPacket(gsp);
		sendPacket(STATIC_PACKET);
	}
	
	/** Should be overridden. */
	protected int getMinimumLength()
	{
		return 0;
	}
	
	protected final long readCompQ()
	{
		if (Config.PACKET_FINAL)
			return readQ();
		else
			return readD();
	}
}
