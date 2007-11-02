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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Format: (ch) S
 * @author  -Wooden-
 * 
 */
public class RequestPCCafeCouponUse extends L2GameClientPacket
{
    private final static Log _log = LogFactory.getLog(RequestPCCafeCouponUse.class.getName());
    private static final String _C__D0_20_REQUESTPCCAFECOUPONUSE = "[C] D0:20 RequestPCCafeCouponUse";
    private String _str;

    /**
     * @param buf
     * @param client
     */
    @Override
    protected void readImpl()
    {
        _str = readS();
    }

    /**
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
    protected void runImpl()
    {
        //TODO: implementation missing
        _log.debug("C5: RequestPCCafeCouponUse: S: "+_str);
    }

    /**
     * @see net.sf.l2j.gameserver.network.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__D0_20_REQUESTPCCAFECOUPONUSE;
    }
}
