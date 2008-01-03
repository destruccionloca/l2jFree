/* This program is free software; you can redistribute it and/or modify
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

/** 
 *	thx red rabbit
 */
public class ExAttributeEnchantResult extends L2GameServerPacket
{
	private static final String _S__FE_61_EXATTRIBUTEENCHANTRESULT = "[S] FE:61 ExAttributeEnchantResult [d]";
	
	private int _result;
	
	public ExAttributeEnchantResult (int result)
	{
		_result = result;
	}
	
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x61);
		
		writeD(_result);
	}
	
	public String getType()
	{
		return _S__FE_61_EXATTRIBUTEENCHANTRESULT;
	}
}
