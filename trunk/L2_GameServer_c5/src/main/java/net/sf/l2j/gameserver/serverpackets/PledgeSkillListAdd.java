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
 *
 * sample
 * 0000: 9c c10c0000 48 00 61 00 6d 00 62 00 75 00 72    .....H.a.m.b.u.r
 * 0010: 00 67 00 00 00 00000000 00000000 00000000 00000000 00000000 00000000 
 * 00 00 
 * 00000000                                           ...
 
 * format   dd C5 ???
 * 
 * genarally used for updating one of the skills (to show the skill emediatelly it has been earned)
 * 
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgeSkillListAdd extends ServerBasePacket
{
	private static final String _S__FE_3a_PLEDGESKILLLIST = "[S] FE:3a PledgeSkillListAdd";
    private static int _val1;
    private static int _val2;

	
	public PledgeSkillListAdd(int val1, int val2)
	{
		//_clan = clan;
        _val1 = val1;
        _val2 = val2;
	}	
	
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
        writeC(0xFE);
        writeH(0x3a);
		//writeS("tomciaaa");
		//writeS("Hier");
        //for (int i=0; i<10; i++)
        //{
            writeD(_val1);//skill id
            writeD(_val2);//skill lvl
        //}
        //writeS("tomciaaa");
        //writeS("Hier");
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__FE_3a_PLEDGESKILLLIST;
	}

}
