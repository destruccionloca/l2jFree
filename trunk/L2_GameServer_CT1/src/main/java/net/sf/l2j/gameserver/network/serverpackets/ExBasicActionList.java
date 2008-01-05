/**
 * 
 */
package net.sf.l2j.gameserver.network.serverpackets;

/**
 * thx red rabbit
 */
public class ExBasicActionList extends L2GameServerPacket
{
	private static final String S_FE_5F_EXBASICACTIONLIST = "[S] FE:5F ExBasicActionList [d(d)]";

	private static final int[] ActionList =
	{
		0x22 , 0x00 , 0x01 , 0x02 , 0x03 , 0x04 , 0x05 , 0x06 , 0x0a , 0x1c , 0x28 ,
		0x25 , 0x37 , 0x39 , 0x3a , 0x3b , 0x07 , 0x08 , 0x09 , 0x0b , 0x32 , 0x38 ,
		0x3c , 0x0c , 0x0d , 0x0e , 0x18 , 0x19 , 0x1a , 0x1d , 0x1e , 0x1f , 0x21 ,
		0x22 , 0x23
	};

	@Override
	public String getType()
	{
		return S_FE_5F_EXBASICACTIONLIST;
	}

	@Override
	protected void writeImpl()
	{
        writeC(0xfe);
        writeH(0x5f);
        
        for (int action : ActionList)
        {
        	writeD(action);
        }
    }
}