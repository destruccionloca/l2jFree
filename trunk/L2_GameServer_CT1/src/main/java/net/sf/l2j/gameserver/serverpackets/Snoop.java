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
 * CDSDDSS -> (0xd5)(objId)(name)(0x00)(type)(speaker)(name)
 */

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Snoop extends L2GameServerPacket
{
	private static final String _S__D5_SNOOP = "[S] D5 Snoop";
	private int _convoId;
	private String _name;
	private int _type;
	private String _speaker;
	private String _msg;
	private L2PcInstance GM;
	
	public Snoop(int id, String name, int type, String speaker, String msg, L2PcInstance Snooper)
	{
		_convoId = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_msg = msg;
		GM = Snooper;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
//		TODO: NEEDS A NEW DECRYPTING, CURRENT ONE ONLY POPS THE WINDOW!
			writeC(0xd5);
			writeD(GM.getObjectId());
			writeS(_name);
			writeD(_convoId); //??
			writeD(_type);
			writeS("B");
			writeS("blabla"+_convoId);
			//System.out.println(_convoId+" "+_name+" "+_type+" "+_speaker+" "+_msg);
			System.out.println(_convoId);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__D5_SNOOP;
	}
}