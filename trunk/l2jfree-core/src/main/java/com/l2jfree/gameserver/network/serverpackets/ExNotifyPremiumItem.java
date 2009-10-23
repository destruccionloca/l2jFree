package com.l2jfree.gameserver.network.serverpackets;

/**
 * Pops up an icon on the left side of the screen and shows
 * a system message "The vitamin item has arrived".<BR>
 * After clicking the icon, a dialog "Your vitamin item has
 * arrived! Visit the vitamin manager in any village [...]
 * @author savormix
 */
public final class ExNotifyPremiumItem extends StaticPacket
{
	private static final String _S__FE_85_EXNOTIFYPREMIUMITEM = "[S] FE:85 ExNotifyPremiumItem";
	public static final ExNotifyPremiumItem PACKET = new ExNotifyPremiumItem();

	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x85);
	}

	@Override
	public String getType()
	{
		return _S__FE_85_EXNOTIFYPREMIUMITEM;
	}
}
