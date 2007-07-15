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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import javolution.util.FastList;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ExSendManorList;

/**
 * Format: ch
 * c (id) 0xD0
 * h (subid) 0x08
 * @author -Wooden-
 *
 */
public class RequestManorList extends ClientBasePacket
{
    private static final String _C__FE_08_REQUESTMANORLIST = "[S] FE:08 RequestManorList";
    /**
     * @param buf
     * @param client
     */
    public RequestManorList(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        // just a trigger
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
    void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        FastList<String> ManorsName = new FastList<String>();
        ManorsName.add("Gludio Manor");
        ManorsName.add("Dion Manor");
        ManorsName.add("Grian Manor");
        ManorsName.add("Oren Manor");
        ManorsName.add("Aden Manor");
        ManorsName.add("Innadril Manor");
        ManorsName.add("Goddard Manor");
        ManorsName.add("Runne Manor");
        FastList<Integer> ManorsId = new FastList<Integer>();
        ManorsId.add(7996);
        ManorsId.add(7997);
        ManorsId.add(7998);
        ManorsId.add(7999);
        ManorsId.add(8000);
        ManorsId.add(8058);
        ManorsId.add(8059);
        ManorsId.add(8060);
        ManorsId.add(8402);
        ManorsId.add(8403);
        ExSendManorList manorlist = new ExSendManorList(ManorsName,ManorsId);
        player.sendPacket(manorlist);
        
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__FE_08_REQUESTMANORLIST;
    }
    
}