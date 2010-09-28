/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.network.serverpackets;

import java.util.List;

import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Migi, DS
 */
public class ExPostItemList extends L2GameServerPacket
{
	private static final String		_S__FE_B2_EXPOSTITEMLIST	= "[S] FE:B2 ExPostItemList";
	
	L2PcInstance					_activeChar;
	private List<L2ItemInstance>	_itemList;
	
	public ExPostItemList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_itemList = _activeChar.getInventory().getAvailableItems(true, false);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xb2);
		writeD(_itemList.size());
		for (L2ItemInstance item : _itemList)
		{
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeQ(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			writeElementalInfo(item);
			writeEnchantEffectInfo();
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_B2_EXPOSTITEMLIST;
	}
}