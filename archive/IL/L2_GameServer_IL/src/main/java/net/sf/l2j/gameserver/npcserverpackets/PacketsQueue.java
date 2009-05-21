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

/**
 * 
 *
 * @author  Luno
 */
public class PacketsQueue
{
    private GsBasePacket _first = null;
    private GsBasePacket _last  = null;
    private int _size = 0;
    
    public PacketsQueue()
    {
        
    }
    public boolean isEmpty()
    {
        return _first == null;
    }
    public void add(GsBasePacket packet)
    {
        if(_first == null)
        {
            _first = packet;
            _last  = packet;
        }
        else
        {
            _last._next = packet;
            _last = packet;
        }
        _size++;    
    }
    /**
     * Returns first packet in queue
     * @return packet that were put in queue as first
     * @throws EmptyQueueException
     */
    public GsBasePacket get() //throws EmptyQueueException
    {
        if(isEmpty())
            //throw new EmptyQueueException("Empty packet queue");
            return null;
        
        GsBasePacket res = _first;
        _first = res._next;
        
        _size--;
        
        return res;
    }
}