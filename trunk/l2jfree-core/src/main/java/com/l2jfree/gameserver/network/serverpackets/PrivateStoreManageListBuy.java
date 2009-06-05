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

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.TradeList;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:40 $
 */
public class PrivateStoreManageListBuy extends L2GameServerPacket
{
	private static final String _S__D0_PRIVATESELLLISTBUY = "[S] b7 PrivateSellListBuy";
	private int _objId;
	private long _playerAdena;
	private L2ItemInstance[] _itemList;
	private TradeList.TradeItem[] _buyList;
	
	public PrivateStoreManageListBuy(L2PcInstance player)
	{
		_objId = player.getObjectId();
		_playerAdena = player.getAdena();
		_itemList = player.getInventory().getUniqueItems(false,true);
		_buyList = player.getBuyList().getItems(); 
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xbd);
		//section 1 
		writeD(_objId);
		if (Config.PACKET_FINAL)
			writeQ(_playerAdena);
		else
			writeD(toInt(_playerAdena));

		//section2 
		writeD(_itemList.length); // inventory items for potential buy
		for (L2ItemInstance item : _itemList)
		{
			writeD(item.getItemDisplayId()); 
			writeH(0); //show enchant lvl as 0, as you can't buy enchanted weapons
			if (Config.PACKET_FINAL)
			{
				writeQ(item.getCount());
				writeQ(item.getReferencePrice());
			}
			else
			{
				writeD(toInt(item.getCount()));
				writeD(toInt(item.getReferencePrice()));
			}

			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());

			if (Config.PACKET_FINAL)
			{
				writeH(item.getAttackElementType());
				writeH(item.getAttackElementPower());
				for (byte i = 0; i < 6; i++)
				{
					writeH(item.getElementDefAttr(i));
				}
			}
			else
			{
				writeD(item.getAttackElementType());
				writeD(item.getAttackElementPower());
				for (byte i = 0; i < 6; i++)
				{
					writeD(item.getElementDefAttr(i));
				}
			}
		}

		//section 3
		writeD(_buyList.length); //count for all items already added for buy
		for (TradeList.TradeItem item : _buyList)
		{
			writeD(item.getItem().getItemDisplayId()); 
			writeH(0);
			if (Config.PACKET_FINAL)
			{
				writeQ(item.getCount());
				writeQ(item.getItem().getReferencePrice());
			}
			else
			{
				writeD(toInt(item.getCount()));
				writeD(toInt(item.getItem().getReferencePrice()));
			}

			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
			if (Config.PACKET_FINAL)
			{
				writeQ(item.getPrice());//your price
				writeQ(item.getItem().getReferencePrice());//fixed store price
			}
			else
			{
				writeD(toInt(item.getPrice()));//your price
				writeD(toInt(item.getItem().getReferencePrice()));//fixed store price
			}

			if (Config.PACKET_FINAL)
			{
				writeH(item.getAttackElementType());
				writeH(item.getAttackElementPower());
				for (byte i = 0; i < 6; i++)
				{
					writeH(item.getElementDefAttr(i));
				}
			}
			else
			{
				writeD(item.getAttackElementType());
				writeD(item.getAttackElementPower());
				for (byte i = 0; i < 6; i++)
				{
					writeD(item.getElementDefAttr(i));
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
    public String getType()
	{
		return _S__D0_PRIVATESELLLISTBUY;
	}
}
