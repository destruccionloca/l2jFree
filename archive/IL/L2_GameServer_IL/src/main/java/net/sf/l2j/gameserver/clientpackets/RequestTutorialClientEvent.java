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


/*
 * events:
 * 00 none
 * 01 Move Char
 * 02 Move Point of View
 * 03 ??
 * 04 ??
 * 05 ??
 * 06 ??
 * 07 ??
 * 08 Talk to Newbie Helper
 */

/**
 * 7E 01 00 00 00 
 * 
 * Format: (c) cccc
 * 
 * @author  DaDummy
 */
public class RequestTutorialClientEvent extends L2GameClientPacket
{
    private static final String _C__7E_REQUESTTUTORIALCLIENTEVENT = "[C] 7E RequestTutorialClientEvent";
    @SuppressWarnings("unused")
    private int _event;
    
    @Override
    protected void readImpl()
    {
        _event = readC(); // event
        readC(); // unknown
        readC(); // unknown
        readC(); // unknown
    }

    /**
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
    protected void runImpl()
    {
    }

    /**
     * @see net.sf.l2j.gameserver.network.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__7E_REQUESTTUTORIALCLIENTEVENT;
    }
}
