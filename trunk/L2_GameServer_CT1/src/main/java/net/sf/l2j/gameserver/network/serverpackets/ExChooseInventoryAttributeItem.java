/**
 * 
 */
package net.sf.l2j.gameserver.network.serverpackets;

/**
 * thx red rabbit
 */
public class ExChooseInventoryAttributeItem extends L2GameServerPacket
{
	private int _itemId;
	
	public ExChooseInventoryAttributeItem(int itemId)
	{
		_itemId = itemId;
	}
	
	@Override
	public String getType()
	{
		return "ExChooseInventoryAttributeItem";
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x62);
		writeD(_itemId);
	}

}
