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
package com.l2jfree.loginserver.gameserverpackets;

import com.l2jfree.loginserver.L2LoginServer;

/**
 * @author savormix
 */
public abstract class GameToLoginPacket
{
	private final byte[] _data;
	private final boolean _l2j;
	private int _off;

	protected GameToLoginPacket(byte[] decrypted, boolean l2j)
	{
		this._data = decrypted;
		this._l2j = l2j;
		this._off = 1;
	}

	/**
	 * Creates a receivable packet.
	 * @param decrypted packet bytes
	 * @param protocol network protocol version
	 */
	public GameToLoginPacket(byte[] decrypted, int protocol)
	{
		this(decrypted, protocol == L2LoginServer.PROTOCOL_L2J);
	}

	/**
	 * Creates a receivable packet.
	 * @param decrypted packet bytes
	 */
	public GameToLoginPacket(byte[] decrypted)
	{
		this(decrypted, false);
	}

	public int readD()
	{
		int result = _data[_off++] & 0xff;
		result |= _data[_off++] << 8 & 0xff00;
		result |= _data[_off++] << 0x10 & 0xff0000;
		result |= _data[_off++] << 0x18 & 0xff000000;
		return result;
	}

	public int readC()
	{
		int result = _data[_off++] & 0xff;
		return result;
	}

	public int readH()
	{
		int result = _data[_off++] & 0xff;
		result |= _data[_off++] << 8 & 0xff00;
		return result;
	}

	public double readF()
	{
		long result = _data[_off++] & 0xff;
		result |= _data[_off++] << 8 & 0xff00;
		result |= _data[_off++] << 0x10 & 0xff0000;
		result |= _data[_off++] << 0x18 & 0xff000000;
		result |= _data[_off++] << 0x20 & 0xff00000000l;
		result |= _data[_off++] << 0x28 & 0xff0000000000l;
		result |= _data[_off++] << 0x30 & 0xff000000000000l;
		result |= _data[_off++] << 0x38 & 0xff00000000000000l;
		return Double.longBitsToDouble(result);
	}

	public String readS()
	{
		String result = null;
		try
		{
			if (_l2j)
			{
				result = new String(_data, _off, _data.length - _off, "UTF-16LE");
				result = result.substring(0, result.indexOf(0x00));
				_off += result.length() * 2 + 2;
			}
			else
			{
				result = new String(_data, _off, _data.length - _off, "UTF-8");
				result = result.substring(0, result.indexOf(0x00));
				_off += result.length() + 2;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}

	public final byte[] readB(int length)
	{
		byte[] result = new byte[length];
		for (int i = 0; i < length; i++)
			result[i] = _data[_off + i];
		_off += length;
		return result;
	}
}
