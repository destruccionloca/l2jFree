package com.l2jfree.gameserver.network.serverpackets;

/**
 * A very strange packet.<BR>
 * If it's sent as a trigger, the player is informed that after
 * server integration, it's clan name (which is never filled in)
 * has overlapped and he must change it.<BR>
 * If we write a zero byte after opcodes, client ignores the packet.<BR>
 * If we write <U>anything</U> else after opcodes, the message changes
 * to something like "requested name invalid/unavailable, please try again"
 * <BR><BR>
 * The name is sent with RequestExChangeName.
 * @author savormix
 */
public final class ExNeedToChangeName extends L2GameServerPacket
{
	private static final String _S__FE_69_EXNEEDTOCHANGENAME = "[S] FE:69 ExNeedToChangeName";

	public ExNeedToChangeName()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x69);

		// write 0x01 if player must retry, nothing otherwise.
		writeD(0x01);
	}

	@Override
	public String getType()
	{
		return _S__FE_69_EXNEEDTOCHANGENAME;
	}
}
