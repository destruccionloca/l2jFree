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
package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2TradeList;

public final class BuyList extends L2GameServerPacket
{
	private static final String _S__07_BUYLIST = "[S] 07 BuyList [ddh (hdddhhdhhhdddddddd)]";
	private int _listId;
	private L2ItemInstance[] _list;
	private int _money;
	private double _taxRate = 0;

	public BuyList(L2TradeList list, int currentMoney)
	{
		_listId = list.getListId();
		List<L2ItemInstance> lst = list.getItems();
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
	}	

	public BuyList(L2TradeList list, int currentMoney, double taxRate)
	{
		_listId = list.getListId();
		List<L2ItemInstance> lst = list.getItems();
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
		_taxRate = taxRate;
	}	
	
	public BuyList(List<L2ItemInstance> lst, int listId, int currentMoney)
	{
		_listId = listId;
		_list = lst.toArray(new L2ItemInstance[lst.size()]);
		_money = currentMoney;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x07);
		writeD(_money);		// current money
		writeD(_listId);
		writeH(_list.length);

		for (L2ItemInstance item : _list)
		{
			if(item.getCount() >0 || item.getCount() == -1)
			{
				writeH(item.getItem().getType1()); // item type1
				writeD(item.getObjectId());
				writeD(item.getItemDisplayId());
				writeD(item.getCount() >= 0 ? item.getCount() : 0); // max amount of items that a player can buy at a time (with this itemid)
				writeH(item.getItem().getType2());					// item type2
				writeH(item.getCustomType1());						// custom type1
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());						// enchant level
				writeH(item.getCustomType2());						// custom type2
                writeH(0x00);
	            if (item.getItemId() >= 3960 && item.getItemId() <= 4026)//Config.RATE_SIEGE_GUARDS_PRICE-//'
	                writeD((int)(item.getPriceToSell() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
	            else
	                writeD((int)(item.getPriceToSell() * (1 + _taxRate)));
                writeD(item.getAttackAttrElement());
                writeD(item.getAttackAttrElementVal());
                writeD(item.getDefAttrFire());
                writeD(item.getDefAttrWater());
                writeD(item.getDefAttrWind());
                writeD(item.getDefAttrEarth());
                writeD(item.getDefAttrHoly());
                writeD(item.getDefAttrUnholy());
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__07_BUYLIST;
	}
}
