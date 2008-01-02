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

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * 0x42 WarehouseWithdrawalList  dh (h dddhh dhhh d)
 * 
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/29 23:15:10 $
 */
public class WareHouseWithdrawalList extends L2GameServerPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; //not sure
	public static final int FREIGHT = 4; //not sure
	private final static Log _log = LogFactory.getLog(WareHouseWithdrawalList.class.getName());
	private static final String _S__54_WAREHOUSEWITHDRAWALLIST = "[S] 42 WareHouseWithdrawalList";
	private L2PcInstance _activeChar;
	private int _activeCharAdena;
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
		writeD(_activeCharAdena);
		writeH(_items.length);
		
		for (L2ItemInstance item : _items)
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemDisplayId());
			writeD(item.getCount());
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
			else writeQ(0x00);
            writeD(item.getAttackAttrElement());
            writeD(item.getAttackAttrElementVal());
            writeD(item.getDefAttrFire());
            writeD(item.getDefAttrWater());
            writeD(item.getDefAttrWind());
            writeD(item.getDefAttrEarth());
            writeD(item.getDefAttrHoly());
            writeD(item.getDefAttrUnholy());
            writeD(item.getMana());
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__54_WAREHOUSEWITHDRAWALLIST;
	}
}
