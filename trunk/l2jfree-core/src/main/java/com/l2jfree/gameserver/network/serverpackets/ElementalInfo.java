package com.l2jfree.gameserver.network.serverpackets;

import com.l2jfree.Config;
import com.l2jfree.gameserver.model.ItemInfo;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.TradeList.TradeItem;
import com.l2jfree.gameserver.templates.item.L2WarehouseItem;

public abstract class ElementalInfo extends L2GameServerPacket
{

	public void writeElementalInfo(L2ItemInstance item)
	{
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

	public void writeElementalInfo(TradeItem item)
	{
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

	public void writeElementalInfo(L2WarehouseItem item)
	{
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

	public void writeElementalInfo(ItemInfo item)
	{
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
