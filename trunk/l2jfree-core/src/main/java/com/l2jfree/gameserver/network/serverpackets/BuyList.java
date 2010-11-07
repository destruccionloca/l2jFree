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

import java.util.Collection;
import java.util.List;

import com.l2jfree.gameserver.model.L2TradeList;
import com.l2jfree.gameserver.model.L2TradeList.L2TradeItem;

public final class BuyList extends L2GameServerPacket
{
	private static final String		_S__07_BUYLIST	= "[S] 07 BuyList [ddh (hdddhhdhhhdddddddd)]";
	private final int				_listId;
	private final Collection<L2TradeItem>		_list;
	private final long				_money;
	private double					_taxRate		= 1.;
	
	public BuyList(L2TradeList list, long currentMoney)
	{
		_listId = list.getListId();
		_list = list.getItems();
		_money = currentMoney;
	}
	
	public BuyList(L2TradeList list, long currentMoney, double taxRate)
	{
		_listId = list.getListId();
		_list = list.getItems();
		_money = currentMoney;
		_taxRate = taxRate;
	}
	
	public BuyList(List<L2TradeItem> lst, int listId, long currentMoney)
	{
		_listId = listId;
		_list = lst;
		_money = currentMoney;
	}
	
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(0x07);
		// current money
		writeQ(_money);
		writeD(_listId);
		writeH(_list.size());
		
		for (L2TradeItem item : _list)
		{
			if (item.getCount() > 0 || item.getCount() == -1)
			{
				writeH(item.getItem().getType1()); // item type1
				writeD(0x00); // objectId
				writeD(item.getItemDisplayId());
				writeQ(item.getCount() >= 0 ? item.getCount() : 0); // max amount of items that a player can buy at a time (with this itemid)
				writeH(item.getItem().getType2()); // item type2
				writeH(0x00); // custom type1
				writeD(item.getItem().getBodyPart());
				writeH(0x00); // enchant level
				writeH(0x00); // custom type2
				writeH(0x00);
				
				writeQ((long) (item.getPrice() * _taxRate));
				writeElementalInfo(item);
				writeEnchantEffectInfo();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__07_BUYLIST;
	}
}
