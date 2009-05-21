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
 *
 * sample
 * <p>
 * 4b 
 * c1 b2 e0 4a 
 * 00 00 00 00
 * <p>
 * 
 * format
 * cdd
 * 
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class ExDuelReady extends L2GameServerPacket
{
	private static final String _S__4B_ExDuelAskStart_0X4B = "[S] 39 ExDuelReady 0x4b";
	private int _unk1;

    /**
     * 
     */
    public ExDuelReady()
    {
        _unk1 = 0;
    }    
    
	/**
	 * @param int objectId of the target
	 * @param int 
	 */
	public ExDuelReady(int unk1)
	{
		_unk1 = unk1;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
        writeH(0x4c);
        writeD(_unk1);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__4B_ExDuelAskStart_0X4B;
	}
}
