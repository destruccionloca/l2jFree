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

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2TradeList;

import javolution.util.FastList;

/**
 * @author l3x
 */

public final class BuyListSeed extends L2GameServerPacket
{
	private static final String _S__E9_BUYLISTSEED = "[S] E9 BuyListSeed [dd h (hdddhhd)]";

	private int _manorId;
	private List<L2ItemInstance> _list = new FastList<L2ItemInstance>();
	private int _money;

	public BuyListSeed(L2TradeList list, int manorId, int currentMoney)
	{
		_money  = currentMoney;
		_manorId = manorId;
		_list   = list.getItems();
	}

	@Override
	public void writeImpl()
	{
		writeC(0xE9);

		writeD(_money);                        // current money
		writeD(_manorId);                      // manor id

		writeH(_list.size());                  // list length

		for (L2ItemInstance item : _list)
		{
			writeH(0x04);                      // item->type1
			writeD(0x00);                      // objectId
			writeD(item.getItemDisplayId());          // item id
			if(Config.PACKET_FINAL)
				writeQ(item.getCount());           // item count
			else
				writeD(item.getCount());           // item count
			writeH(0x04);                      // item->type2
			writeH(0x00);          
			if(Config.PACKET_FINAL)
				writeQ(item.getPriceToSell());     // price
			else
				writeD(item.getPriceToSell());     // price
		}
	}

	@Override
	public String getType()
	{
		return _S__E9_BUYLISTSEED;
	}
}
