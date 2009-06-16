/* This program is free software; you can redistribute it and/or modify
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
package com.l2jfree.loginserver.loginserverpackets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.l2jfree.loginserver.L2LoginServer;

/**
 * Original author unknown.
 * @author savormix
 */
public abstract class LoginToGamePacket
{
	private final ByteArrayOutputStream _bao;
	private final boolean _l2j;

	protected LoginToGamePacket(int opCode, boolean l2j)
	{
		_bao = new ByteArrayOutputStream();
		_l2j = l2j;
		writeC(opCode);
	}

	/**
	 * Creates a sendable packet.
	 * @param opCode Packet identifier
	 * @param protocol Network protocol version
	 */
	public LoginToGamePacket(int opCode, int protocol)
	{
		this(opCode, protocol == L2LoginServer.PROTOCOL_L2J);
	}

	/**
	 * Creates a sendable packet.
	 * @param opCode Packet identifier
	 */
	public LoginToGamePacket(int opCode)
	{
		this(opCode, false);
	}

	protected final void writeD(int value)
	{
		_bao.write(value & 0xff);
		_bao.write(value >> 8 & 0xff);
		_bao.write(value >> 16 & 0xff);
		_bao.write(value >> 24 & 0xff);
	}

	protected final void writeH(int value)
	{
		_bao.write(value & 0xff);
		_bao.write(value >> 8 & 0xff);
	}

	protected final void writeC(int value)
	{
		_bao.write(value & 0xff);
	}

	protected final void writeF(double org)
	{
		long value = Double.doubleToRawLongBits(org);
		_bao.write((int) (value & 0xff));
		_bao.write((int) (value >> 8 & 0xff));
		_bao.write((int) (value >> 16 & 0xff));
		_bao.write((int) (value >> 24 & 0xff));
		_bao.write((int) (value >> 32 & 0xff));
		_bao.write((int) (value >> 40 & 0xff));
		_bao.write((int) (value >> 48 & 0xff));
		_bao.write((int) (value >> 56 & 0xff));
	}

	protected final void writeS(String text)
	{
		try
		{
			if (text != null)
			{
				if (_l2j)
					_bao.write(text.getBytes("UTF-16LE"));
				else
					_bao.write(text.getBytes("UTF-8"));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		_bao.write(0);
		_bao.write(0);
	}

	protected final void writeB(byte[] array)
	{
		try
		{
			_bao.write(array);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public final int getLength()
	{
		return _bao.size() + 2;
	}

	public final byte[] getBytes()
	{
		writeD(0x00); // reserve for checksum

		int padding = _bao.size() % 8;
		if (padding != 0)
			for (int i = padding; i < 8; i++)
				writeC(0x00);

		return _bao.toByteArray();
	}

	public byte[] getContent()
	{
		return getBytes();
	}
}
