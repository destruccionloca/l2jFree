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
package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Iterator;

import javolution.util.FastList;
import net.sf.l2j.gameserver.templates.L2PcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.7 $ $Date: 2005/03/27 15:29:39 $
 */
public class CharTemplates extends L2GameServerPacket
{
	private static final String _S__0D_CHARTEMPLATES = "[S] 0d CharTemplates [d (ddddddddddddddddddd)]";
	private FastList<L2PcTemplate> _chars = new FastList<L2PcTemplate>();
	
	public void addChar(L2PcTemplate template)
	{
		_chars.add(template);
	}	

	@Override
	protected final void writeImpl()
	{
		writeC(0x0D);
		writeD(_chars.size());

		for (Iterator it = _chars.iterator(); it.hasNext(); writeD(0x0a))
		{
			L2PcTemplate temp = (L2PcTemplate)it.next();
			//writeD(temp.getRace().ordinal());
			writeD(0x00);
			writeD(temp.getClassId().getId());
			writeD(0x46);
			writeD(temp.getBaseSTR());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseDEX());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseCON());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseINT());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseWIT());
			writeD(0x0a);
			writeD(0x46);
			writeD(temp.getBaseMEN());
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__0D_CHARTEMPLATES;
	}
}
