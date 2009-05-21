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

import java.util.Vector;

/**
 * 
 *
 * sample
 * 0000: 9c c10c0000 48 00 61 00 6d 00 62 00 75 00 72    .....H.a.m.b.u.r
 * 0010: 00 67 00 00 00 00000000 00000000 00000000 00000000 00000000 00000000 
 * 00 00 
 * 00000000                                           ...
 
  * format   d(dd) C5 ???
 * 
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgeSkillList extends ServerBasePacket
{
	private static final String _S__FE_39_PLEDGESKILLLIST = "[S] FE:39 PledgeSkillList";
    private Vector<Skill> _skills;

    class Skill
    {
        public int id;
        public int level;

        Skill(int pId, int pLevel)
        {
            this.id = pId;
            this.level = pLevel;
        }
    }
	
	public PledgeSkillList()
	{
		//_clan = clan;
        _skills = new Vector<Skill>();
	}	
    
    public void addSkill(int id, int level)
    {
        _skills.add(new Skill(id, level));
    }
	
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
        writeC(0xFE);
        writeH(0x39);;
		//writeD(_val1); //skill count
		//writeS("tomciaaa");
		//writeS("Hier");
        //for (int i=0; i<_val1; i++)
            /*writeD(_val2); //skill id
            writeD(_val3); //skill lvl*/
        writeD(_skills.size());

        for (int i = 0; i < _skills.size(); i++)
        {
            Skill temp = _skills.get(i);
            writeD(temp.id);
            writeD(temp.level);        
        }
            /*writeD(_val4);
            writeD(_val5);
            writeD(_val6); */           
        //writeS("tomciaaa");
        //writeS("Hier");
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__FE_39_PLEDGESKILLLIST;
	}

}
