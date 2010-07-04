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

import javolution.util.FastList;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2TradeList;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.templates.item.L2Item;

/**
 * @author ShanSoft
 */
public class ExBuySellListPacket extends L2GameServerPacket
{
	private static final String _S__B7_ExBuySellListPacket = "[S] B7 ExBuySellListPacket";
	
	private final int _buyListId;
	private final List<L2ItemInstance> _buyList = new FastList<L2ItemInstance>();
	private final long _money;
	private double _taxRate = 0;
	private List<L2ItemInstance> _sellList = null;
	private L2ItemInstance[] _refundList = null;
	private final boolean _done;
	
	public ExBuySellListPacket(L2PcInstance player, L2TradeList list, boolean done)
	{
		_money = player.getAdena();
		_buyListId = list.getListId();
		for (L2ItemInstance item : list.getItems())
		{
			if (item.getCount() > 0 || item.getCount() == -1)
				_buyList.add(item);
			else
				continue;
		}
		_sellList = player.getInventory().getAvailableItems(true, false);
		if (player.hasRefund())
			_refundList = player.getRefund().getItems();
		_done = done;
	}
	
	public ExBuySellListPacket(L2PcInstance player, L2TradeList list, double taxRate, boolean done)
	{
		_money = player.getAdena();
		_taxRate = taxRate;
		_buyListId = list.getListId();
		for (L2ItemInstance item : list.getItems())
		{
			if (item.getCount() > 0 || item.getCount() == -1)
				_buyList.add(item);
			else
				continue;
		}
		_sellList = player.getInventory().getAvailableItems(false, false);
		if (player.hasRefund())
			_refundList = player.getRefund().getItems();
		_done = done;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xB7);
		writeQ(_money);
		
		writeD(_buyListId);
		writeH(_buyList.size());
		for (L2ItemInstance item : _buyList)
		{
			writeH(item.getItem().getType1());
			writeD(0x00); // objectId
			writeD(item.getItemId());
			writeQ(item.getCount() >= 0 ? item.getCount() : 0);
			writeH(item.getItem().getType2());
			writeH(0x00); // ?
			if (item.getItem().getType1() != L2Item.TYPE1_ITEM_QUESTITEM_ADENA)
			{
				writeD(item.getItem().getBodyPart());
				writeH(0x00); // item enchant level
				writeH(0x00); // ?
				writeH(0x00);
			}
			else
			{
				writeD(0x00);
				writeH(0x00);
				writeH(0x00);
				writeH(0x00);
			}
			
			if (item.getItemId() >= 3960 && item.getItemId() <= 4026)// Config.RATE_SIEGE_GUARDS_PRICE-//'
				writeQ((long)(item.getPriceToSell() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
			else
				writeQ((long)(item.getPriceToSell() * (1 + _taxRate)));
			
			// T1
			for (byte i = 0; i < 8; i++)
				writeH(0x00);
			
			writeEnchantEffectInfo();
		}
		
		if (_sellList != null && _sellList.size() > 0)
		{
			writeH(_sellList.size());
			for (L2ItemInstance item : _sellList)
			{
				writeH(item.getItem().getType1());
				writeD(item.getObjectId());
				writeD(item.getItemId());
				writeQ(item.getCount());
				writeH(item.getItem().getType2());
				writeH(0x00);
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(0x00);
				writeH(0x00);
				writeQ(item.getItem().getReferencePrice() / 2);
				
				// T1
				writeElementalInfo(item);
				writeEnchantEffectInfo();
			}
		}
		else
			writeH(0x00);
		
		if (_refundList != null && _refundList.length > 0)
		{
			writeH(_refundList.length);
			int idx = 0;
			for (L2ItemInstance item : _refundList)
			{
				writeD(idx++);
				writeD(item.getItemId());
				writeQ(item.getCount());
				writeH(item.getItem().getType2());
				writeH(0x00); // ?
				writeH(item.getEnchantLevel());
				writeH(0x00); // ?
				writeQ(item.getCount() * item.getItem().getReferencePrice() / 2);
				
				// T1
				writeElementalInfo(item);
				writeEnchantEffectInfo();
			}
		}
		else
			writeH(0x00);
		
		writeC(_done ? 0x01 : 0x00);
		
		_buyList.clear();
	}
	
	@Override
	public String getType()
	{
		return _S__B7_ExBuySellListPacket;
	}
}
