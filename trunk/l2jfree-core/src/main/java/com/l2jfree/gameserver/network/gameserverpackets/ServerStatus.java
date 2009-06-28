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
package com.l2jfree.gameserver.network.gameserverpackets;

import javolution.util.FastList;

import com.l2jfree.gameserver.loginserverthread.CrossLoginServerThread;

public class ServerStatus extends GameServerBasePacket
{
	// Good, Normal and Full are not used for years
	// Oh and they wont be used. ever.
	public static final String[]	STATUS_STRING	= { "Auto", "Good", "Normal", "Full", "Down", "Gm Only" };

	public static final int SERVER_LIST_STATUS		= 0x01;
	public static final int SERVER_LIST_CLOCK		= 0x02;
	public static final int SERVER_LIST_BRACKETS	= 0x03;
	public static final int SERVER_LIST_MAX_PLAYERS	= 0x04;
	public static final int TEST_SERVER				= 0x05;
	public static final int SERVER_LIST_PVP			= 0x06;
	public static final int SERVER_LIST_UNK			= 0x07;
	public static final int SERVER_LIST_HIDE_NAME	= 0x08;
	public static final int SERVER_AGE_LIMITATION	= 0x09;
	private static final int[] LEGACY = {
		0x03, 0x05, 0x08, 0x02, 0x07, 0x01, 0x04, 0x06, 0x09
	};

	public static final int STATUS_AUTO				= 0x00;
	public static final int STATUS_DOWN				= 0x04;
	public static final int STATUS_GM_ONLY			= 0x05;

	private static final int ON						= 0x01;
	private static final int OFF					= 0x00;

	private final boolean				_legacy;
	private final FastList<Attribute>	_attributes;

	private class Attribute
	{
		public final int id;
		public final int value;

		private Attribute(int pId, int pValue)
		{
			id = convert(pId);
			value = pValue;
		}
	}

	public ServerStatus(int protocol)
	{
		super(protocol, 0x06);
		_legacy = (protocol == CrossLoginServerThread.PROTOCOL_LEGACY);
		_attributes = new FastList<Attribute>(1);
	}

	public void addAttribute(int id, int value)
	{
		_attributes.add(new Attribute(id, value));
	}

	public void addAttribute(int id, boolean on)
	{
		_attributes.add(new Attribute(id, on ? ON : OFF));
	}

	private final int convert(int id)
	{
		if (!_legacy)
			return id;
		else
			return LEGACY[id - 1];
	}

	@Override
	public byte[] getContent()
	{
		writeD(_attributes.size());
		for (int i = 0; i < _attributes.size(); i++)
		{
			Attribute temp = _attributes.get(i);
			writeD(temp.id);
			writeD(temp.value);
		}
		return super.getContent();
	}
}
