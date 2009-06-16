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

import java.util.Vector;

import com.l2jfree.gameserver.loginserverthread.CrossLoginServerThread;

/**
 * @author -Wooden-
 * 
 */
public class ServerStatusL2jfree extends GameServerBasePacket
{
	private Vector<Attribute> _attributes;

    public static final String[] STATUS_STRING = { "Auto", "Maintenance", "Down" };

    public static final int SERVER_LIST_PVP			= 0x01;
    public static final int SERVER_LIST_MAX_PLAYERS = 0x02;
    public static final int SERVER_LIST_STATUS		= 0x03;
    public static final int SERVER_LIST_UNK			= 0x04;
    public static final int SERVER_LIST_CLOCK		= 0x05;
    public static final int SERVER_LIST_HIDE_NAME	= 0x06;
    public static final int TEST_SERVER				= 0x07;
    public static final int SERVER_LIST_BRACKETS	= 0x08;
    public static final int SERVER_AGE_LIMIT		= 0x09;

    public static final int STATUS_AUTO				= 0x00;
    public static final int STATUS_GM_ONLY			= 0x01;
    public static final int STATUS_DOWN				= 0x02;

    class Attribute
    {
        public int id;
        public int value;

        Attribute(int pId, int pValue)
        {
            id = pId;
            value = pValue;
        }
    }

    public ServerStatusL2jfree()
    {
    	super(CrossLoginServerThread.PROTOCOL_LEGACY, 0x06);
        _attributes = new Vector<Attribute>();
    }

    public void addAttribute(int id, boolean on)
    {
        _attributes.add(new Attribute(id, on ? 1 : 0));
    }

    public void addMaxPlayerAttribute(int count)
    {
    	_attributes.add(new Attribute(SERVER_LIST_MAX_PLAYERS, count));
    }

    public void addMinAgeAttribute(int age)
    {
    	_attributes.add(new Attribute(SERVER_AGE_LIMIT, age));
    }

    public void addServerDownAttribute()
    {
        _attributes.add(new Attribute(SERVER_LIST_STATUS, STATUS_DOWN));
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
        return getBytes();
    }
}
