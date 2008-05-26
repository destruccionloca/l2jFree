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
 * This class ...
 * 
 * @author tomciaaa
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ExRedSky extends ServerBasePacket
{
	private static final String _S__FE_40_EXREDSKY = "[S] FE:40 ExRedSky";
	private int _duration;
	//private int _type;
    //private int _v3;
    //private int _v4;
    //private int _v5;

	/**
	 * 0xfe:0x40 ExRedSky         not known 
	 * @param _characters
	 */
	public ExRedSky(int duration/*, int type, int v3, int v4, int v5*/)
	{
		_duration = duration;
		//_type = type;
        //_v3 = v3;
        //_v4 = v4;
        //_v5 = v5;
	}


	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x40);     // sub id
		writeD(_duration);
		//writeD(_type);
        //writeD(_v3);
        //writeD(_v4);
        //writeD(_v5);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__FE_40_EXREDSKY;
	}
}
