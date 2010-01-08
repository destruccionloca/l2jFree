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
package com.l2jfree.loginserver;

import java.nio.ByteBuffer;

import com.l2jfree.Config;
import com.l2jfree.loginserver.L2LoginClient.LoginClientState;
import com.l2jfree.loginserver.clientpackets.AuthGameGuard;
import com.l2jfree.loginserver.clientpackets.L2LoginClientPacket;
import com.l2jfree.loginserver.clientpackets.RequestAuthLogin;
import com.l2jfree.loginserver.clientpackets.RequestServerList;
import com.l2jfree.loginserver.clientpackets.RequestServerLogin;
import com.l2jfree.loginserver.clientpackets.RequestSubmitCardNo;
import com.l2jfree.loginserver.serverpackets.L2LoginServerPacket;
import com.l2jfree.mmocore.network.IPacketHandler;

/**
 * Handler for packets received by Login Server
 * 
 * @author KenM
 */
public final class L2LoginPacketHandler implements
	IPacketHandler<L2LoginClient, L2LoginClientPacket, L2LoginServerPacket>
{
	public L2LoginClientPacket handlePacket(ByteBuffer buf, L2LoginClient client, final int opcode)
	{
		final LoginClientState state = client.getState();
		
		switch (state)
		{
			case CONNECTED:
				if (opcode == 0x07)
				{
					return new AuthGameGuard();
				}
				else
				{
					debugOpcode(opcode, state, client);
				}
				break;
			case AUTHED_GG:
				if (opcode == 0x00)
				{
					return new RequestAuthLogin();
				}
				else
				{
					debugOpcode(opcode, state, client);
				}
				break;
			case AUTHED_LOGIN:
				if (opcode == 0x05)
				{
					return new RequestServerList();
				}
				else if (opcode == 0x02)
				{
					return new RequestServerLogin();
				}
				else if (opcode == 0x06)
				{
					if (Config.SECURITY_CARD_LOGIN)
						return new RequestSubmitCardNo();
				}
				else
				{
					debugOpcode(opcode, state, client);
				}
				break;
		}
		
		return null;
	}
	
	private void debugOpcode(int opcode, LoginClientState state, L2LoginClient client)
	{
		System.out.println("Unknown Opcode: " + opcode + " for state: " + state.name() + " for client " + client);
	}
}
