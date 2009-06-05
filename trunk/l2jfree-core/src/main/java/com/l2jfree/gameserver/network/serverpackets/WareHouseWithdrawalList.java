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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
/**
 * 0x42 WarehouseWithdrawalList  dh (h dddhh dhhh d)
 * 
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public class WareHouseWithdrawalList extends L2GameServerPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 4;
	public static final int CASTLE = 3; //not sure
	public static final int FREIGHT = 1;
	private final static Log _log = LogFactory.getLog(WareHouseWithdrawalList.class.getName());
	private static final String _S__54_WAREHOUSEWITHDRAWALLIST = "[S] 42 WareHouseWithdrawalList";
	private L2PcInstance _activeChar;
	private long _activeCharAdena;
	private L2ItemInstance[] _items;
	private int _whType;

	public WareHouseWithdrawalList(L2PcInstance player, int type)
	{
		_activeChar = player;
		_whType = type;
		
		_activeCharAdena = _activeChar.getAdena();
		if (_activeChar.getActiveWarehouse() == null)
		{
            // Something went wrong!
            _log.warn("error while sending withdraw request to: " + _activeChar.getName());
            return;
		}
		_items = _activeChar.getActiveWarehouse().getItems();
		
		if (_log.isDebugEnabled())
			for (L2ItemInstance item : _items)
				_log.info("item:" + item.getItem().getName() +
						" type1:" + item.getItem().getType1() + " type2:" + item.getItem().getType2());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x42);
		/* 0x01-Private Warehouse  
		* 0x02-Clan Warehouse  
		* 0x03-Castle Warehouse  
		* 0x04-Warehouse */  
		writeH(_whType);
		if (Config.PACKET_FINAL)
			writeQ(_activeCharAdena);
		else
			writeD(toInt(_activeCharAdena));
		writeH(_items.length);
		
		for (L2ItemInstance item : _items)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemDisplayId());
			if (Config.PACKET_FINAL)
				writeQ(item.getCount());
			else
				writeD(toInt(item.getCount()));
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1() );
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2() );
			writeH(0x00);	// ?
			writeD(item.getObjectId());
			if (item.isAugmented())
			{
				writeD(0x0000FFFF&item.getAugmentation().getAugmentationId());
				writeD(item.getAugmentation().getAugmentationId()>>16);
			}
			else
				writeQ(0x00);

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

			writeD(item.getMana());

			// T2
			writeD(item.isTimeLimitedItem() ? (int) (item.getRemainingTime()/1000) : -1);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__54_WAREHOUSEWITHDRAWALLIST;
	}
}
