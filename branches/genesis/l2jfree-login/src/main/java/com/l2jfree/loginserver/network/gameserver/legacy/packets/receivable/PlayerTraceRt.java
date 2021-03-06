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
package com.l2jfree.loginserver.network.gameserver.legacy.packets.receivable;

import java.nio.BufferUnderflowException;

import com.l2jfree.loginserver.network.gameserver.legacy.packets.L2LegacyGameServerPacket;
import com.l2jfree.network.mmocore.InvalidPacketException;
import com.l2jfree.network.mmocore.MMOBuffer;

/**
 * @author savormix
 */
@SuppressWarnings("unused")
public final class PlayerTraceRt extends L2LegacyGameServerPacket
{
	/** Packet's identifier */
	public static final int OPCODE = 0x07;
	
	private static final int HOPS = 4;
	
	private String _account;
	private String _ip;
	private final String[] _hops;
	
	/** Constructs this packet. */
	public PlayerTraceRt()
	{
		_hops = new String[HOPS];
	}
	
	@Override
	protected int getMinimumLength()
	{
		return READ_S + READ_S + HOPS * READ_S;
	}
	
	@Override
	protected void read(MMOBuffer buf) throws BufferUnderflowException, RuntimeException
	{
		_account = buf.readS();
		_ip = buf.readS();
		for (int i = 0; i < _hops.length; i++)
			_hops[i] = buf.readS();
	}
	
	@Override
	protected void runImpl() throws InvalidPacketException, RuntimeException
	{
		// there is absolutely no reason to trust this
		// System.out.println(getClient() + "|" + getType() + "|" + _account + "|" + _ip + "|" + Arrays.toString(_hops));
	}
}
