/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 * 
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
package net.sf.l2j.loginserver.serverpackets;

import junit.framework.TestCase;
import net.sf.l2j.loginserver.gameserverpackets.ServerStatus;
import net.sf.l2j.util.Util;

/**
 * This class ...
 * 
 * @version $Revision: $ $Date: $
 * @author  Chris
 */
public class ServerListTest extends TestCase
{
    protected static byte[] _correctContent = {
        (byte)0x04, // packetId
        (byte)0x03, // servercount
        (byte)0x00,
        // server 2
        (byte)0x03, // ID
        (byte)0x7f, (byte)0x00, (byte)0x00, (byte)0x01, // IP
        (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, // Port
        (byte)0x0f,
        (byte)0x00, // PVP
        (byte)0x02, (byte)0x00, // current players
        (byte)0xff, (byte)0x00, // max     players
        (byte)0x01, // status != down
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        // server 0
        (byte)0x01, // ID
        (byte)0x7f, (byte)0x00, (byte)0x00, (byte)0x01, // IP
        (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, // Port
        (byte)0x0f,
        (byte)0x00, // PVP
        (byte)0x09, (byte)0x00, // current players
        (byte)0xff, (byte)0x00, // max     players
        (byte)0x01, // status != down
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        // server 1
        (byte)0x02, // ID
        (byte)0x7f, (byte)0x00, (byte)0x00, (byte)0x01, // IP
        (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, // Port
        (byte)0x0f,
        (byte)0x00, // PVP
        (byte)0x00, (byte)0x00, // current players
        (byte)0xff, (byte)0x00, // max     players
        (byte)0x00, // status != down
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        // getBytes() data
        (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, 
    };

    /**
     * Test method for {@link net.sf.l2j.loginserver.serverpackets.ServerList#getContent()}.
     */
    public void testGetContent()
    {
        ServerList sl = new ServerList();
        
        sl.addServer("127.0.0.1", 1, false, false, 9, 255, false, false, ServerStatus.STATUS_NORMAL, 0);
        sl.addServer("127.0.0.1", 1, false, false, 0, 255, false, false, ServerStatus.STATUS_DOWN, 1);
        sl.addServer("127.0.0.1", 1, false, false, 2, 255, false, false, ServerStatus.STATUS_NORMAL, 2);
        
        byte[] data = sl.getContent();
        
        assertEquals (Util.printData(_correctContent), Util.printData(data));
    }

}
