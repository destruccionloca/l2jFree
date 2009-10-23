package com.l2jfree.gameserver.network.serverpackets;

/**
 * This packet pops up a birthday cake icon on the left hand side
 * of the screen. After clicking the icon, you get a confirm dialog
 * saying your birthday present has arrived and that you may receive
 * it at any Gatekeeper.
 * @author savormix
 */
public final class ExNotifyBirthDay extends StaticPacket
{
	private static final String _S__FE_8F_EXNOTIFYBIRTHDAY = "[S] FE:8F ExNotifyBirthDay";
	public static final ExNotifyBirthDay PACKET = new ExNotifyBirthDay();

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x8f);
	}

	@Override
	public String getType()
	{
		return _S__FE_8F_EXNOTIFYBIRTHDAY;
	}
}
