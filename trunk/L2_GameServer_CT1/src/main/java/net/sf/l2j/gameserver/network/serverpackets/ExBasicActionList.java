/**
 * 
 */
package net.sf.l2j.gameserver.network.serverpackets;

/**
 * thx red rabbit
 */
public class ExBasicActionList extends L2GameServerPacket
{
	@Override
	public String getType()
	{
		return "ExBasicActionList";
	}

	@Override
	protected void writeImpl()
	{
        writeC(0xfe);
        writeH(0x5f);
        writeD(0x22);
        writeD(0x00);
        writeD(0x01);
        writeD(0x02);
        writeD(0x03);
        writeD(0x04);
        writeD(0x05);
        writeD(0x06);
        writeD(0x0a);
        writeD(0x1c);
        writeD(0x28);
        writeD(0x25);
        writeD(0x37);
        writeD(0x39);
        writeD(0x3a);
        writeD(0x3b);
        writeD(0x07);
        writeD(0x08);
        writeD(0x09);
        writeD(0x0b);
        writeD(0x32);
        writeD(0x38);
        writeD(0x3c);
        writeD(0x0c);
        writeD(0x0d);
        writeD(0x0e);
        writeD(0x18);
        writeD(0x19);
        writeD(0x1a);
        writeD(0x1d);
        writeD(0x1e);
        writeD(0x1f);
        writeD(0x21);
        writeD(0x22);
        writeD(0x23);
	}

}
