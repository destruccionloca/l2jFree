/**
 * 
 */
package net.sf.l2j.gameserver.network.serverpackets;

/** 
 *	thx red rabbit
 */
public class ExAttributeEnchantResult extends L2GameServerPacket
{
	private int _result;
	
	public ExAttributeEnchantResult (int result)
	{
		_result = result;
	}
	
	protected final void writeImpl()
	{
		writeC(0xfe);
		writeH(0x61);
		writeD(_result);
	}
	
	public String getType()
	{
		return "ExAttributeEnchantResult";
	}
}
