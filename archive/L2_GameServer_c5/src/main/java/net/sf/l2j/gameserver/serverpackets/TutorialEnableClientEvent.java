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
package net.sf.l2j.gameserver.serverpackets;

/**
 * A2 00 FF 6F 7F 
 * 
 * Format: (s) cccc
 * 
 * @author  DaDummy
 */
public class TutorialEnableClientEvent extends ServerBasePacket
{
    private static final String _S__A2_TUTORIALENABLECLIENTEVENT = "[S] a2 TutorialEnableClientEvent";
    private int _event;
    
    public TutorialEnableClientEvent(int event)
    {
        _event = event;
    }
    
    /**
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#runImpl()
     */
    @Override
    void runImpl()
    {
        // TODO Auto-generated method stub

    }

    /**
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
     */
    @Override
    void writeImpl()
    {
        writeC(0xA2);
        writeC(_event);
        writeC(0xFF); // unknown
        writeC(0x6F); // unknown
        writeC(0x7F); // unknown
    }

    /**
     * @see net.sf.l2j.gameserver.network.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _S__A2_TUTORIALENABLECLIENTEVENT;
    }

}
