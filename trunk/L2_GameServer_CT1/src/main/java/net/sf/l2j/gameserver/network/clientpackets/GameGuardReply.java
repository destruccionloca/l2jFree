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
package net.sf.l2j.gameserver.network.clientpackets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.util.Util;

/**
 * Format: c dddd
 * 
 * @author  KenM
 */

public class GameGuardReply extends L2GameClientPacket
{
    private static final String _C__CA_GAMEGUARDREPLY = "[C] CA GameGuardReply";

    private static final byte[] VALID =
    {
        0xFFFFFFF6 , 0x59 , 0xFFFFFFDE , 0xFFFFFFE4 , 0x0 , 0xFFFFFFD5 , 0x3 , 0xFFFFFF82,
        0xFFFFFFEA , 0xFFFFFFAC , 0xFFFFFFB5 , 0xFFFFFF95 , 0x0 , 0x1A , 0xFFFFFFE7,
        0xFFFFFFB6 , 0x10 , 0xFFFFFFE3 , 0xFFFFFF84 , 0xFFFFFFB3
    };
    
    private byte[] _reply = new byte[8];
    
    @Override
    protected void readImpl()
    {
        readB(_reply, 0, 4);
        readD();
        readB(_reply, 4, 4);
    }

    @Override
    protected void runImpl()
    {
        L2GameClient client = this.getClient();
        try
        {
            MessageDigest md = MessageDigest.getInstance( "SHA" );
            byte[] result = md.digest(_reply);
            if (Arrays.equals(result, VALID))
            {
                client.setGameGuardOk(true);
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public String getType()
    {
        return _C__CA_GAMEGUARDREPLY;
    }
}
