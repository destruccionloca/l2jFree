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

import net.sf.l2j.gameserver.model.L2Clan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * sample
 * 0000: 9c c10c0000 48 00 61 00 6d 00 62 00 75 00 72    .....H.a.m.b.u.r
 * 0010: 00 67 00 00 00 00000000 00000000 00000000 00000000 00000000 00000000 
 * 00 00 
 * 00000000                                           ...
 
  * format   ddd (S dd)
 * 
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgeWarList extends ServerBasePacket
{
	private static final String _S__FE_3E_PLEDGEWARLIST = "[S] FE:3E PledgeWarList";
	private L2Clan _clan;
    private final static Log _log = LogFactory.getLog(ServerBasePacket.class.getName());
    private static int _updateType;
    private static int _page;


	
	public PledgeWarList(L2Clan clan, int type, int page)
	{
		_clan = clan;
        _updateType = type;
        _page = page;
        /*_val1 = val1;
        _val2 = val2;
        _val3 = val3;
        _val4 = val4;
        _val5 = val5;
        _val6 = val6;
        _val7 = val7;*/
	}	
	
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
        writeC(0xFE);
        writeH(0x3e);;
        writeD(_updateType); //which type of war list sould be revamped by this packet
        writeD(-1); //page number goes here(_page ), made it static cuz not sure how many war to add to one page so TODO here
        if (_updateType == 1)
        {
            writeD(_clan.getAtackerClans().size());  //war list length
            for (L2Clan clan : _clan.getAtackerClans())
            {
                writeS(clan.getName());
                writeD(1);
                writeD(0); //filler ??
            } 
        }
        else if (_updateType == 0)
        {
            writeD(_clan.getEnemyClans().size());  //war list length
            for (L2Clan clan : _clan.getEnemyClans())
            {
                writeS(clan.getName());
                writeD(0);
                writeD(0); // filler ??
            }
        }
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__FE_3E_PLEDGEWARLIST;
	}

}
