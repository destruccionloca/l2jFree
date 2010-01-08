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
package com.l2jfree.loginserver.clientpackets;

import java.nio.BufferUnderflowException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.loginserver.L2LoginClient;
import com.l2jfree.loginserver.serverpackets.L2LoginServerPacket;
import com.l2jfree.mmocore.network.ReceivablePacket;

/**
 * @author KenM
 */
public abstract class L2LoginClientPacket extends
	ReceivablePacket<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket>
{
	protected static final Log _log = LogFactory.getLog(L2LoginClientPacket.class);
	
	protected L2LoginClientPacket()
	{
	}
	
	@Override
	protected final boolean read() throws BufferUnderflowException, RuntimeException
	{
		return readImpl();
	}
	
	protected abstract boolean readImpl() throws BufferUnderflowException, RuntimeException;
}
