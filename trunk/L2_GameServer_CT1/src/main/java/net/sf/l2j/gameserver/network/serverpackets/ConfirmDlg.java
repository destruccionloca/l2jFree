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
package net.sf.l2j.gameserver.network.serverpackets;

/**
 * @author Dezmond_snz
 */
public class ConfirmDlg extends L2GameServerPacket
{
	private static final String _S__F3_CONFIRMDLG = "[S] f3 ConfirmDlg [dddsdddd{d}d]";
	private int _requestId;
	private String _name;
	private int _param;

	public ConfirmDlg(int requestId, String requestorName)
	{
		_requestId = requestId;
		_name = requestorName;
		_param = 0;
	}

	public ConfirmDlg(int requestId, String requestorName, int param)
	{
		_requestId = requestId;
		_name = requestorName;
		_param = param;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xf3);
		writeD(_requestId);
		writeD(0x02); // ??
		writeD(0x00); // ??
		writeS(_name);
		writeD(0x01);
		writeD(_param); // Value: Restore Percent, old values
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__F3_CONFIRMDLG;
	}
}
