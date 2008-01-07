/*
 * This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.npcserverpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.NpcServerThread;

/**
 * Represents packets that are being sent to npc server
 *
 * @author  Luno
 */
public abstract class GsBasePacket
{
    private static int PACKET_MAX_SIZE = 8 * 1024;
    
    public GsBasePacket    _next;
    protected ByteBuffer    _buf;
    protected int        _length;
      
    protected GsBasePacket() 
    { 
        _buf    = ByteBuffer.allocate(PACKET_MAX_SIZE);
        _length = 0;
        _next   = null;
        
    } 
    public int getLength()
    {
        return _length;
    }
    public  ByteBuffer getData()
    {
        return _buf;
    }
    
    protected final void writeByte(byte value)
    {
        _buf.put(value);
        _length += 1;
    }

    public final void send()
    {
        NpcServerThread.getInstance().addPacket(this);
    }
 
    protected final void writeShort(short value)
    {
        _buf.putShort(value);
        _length += 2;
    }
    protected final void writeInt(int value)
    {
        _buf.putInt(value);
        _length += 4;
    }
    protected final void writeLong(long value)
    {
        _buf.putLong(value);
        _length += 8;
    }
    protected final void writeFloat(float value)
    {
        _buf.putFloat(value);
        _length += 4;
    }
    protected final void writeDouble(double value)
    {
        _buf.putDouble(value);
        _length += 8;
    }
    protected final void writeString(String text)
    {
        if (text == null)
        {
            _buf.putChar('\000');
            _length += 2;
        }
        else
        {
            final int len = text.length();
            for (int i=0; i < len; i++)
            {
                _buf.putChar(text.charAt(i));
                _length += 2;
            }
            _buf.putChar('\000');
            _length += 2;
        }
    }
}