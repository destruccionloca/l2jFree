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

/**
 * @author Dezmond_snz
 */
public class ConfirmDlg extends L2GameServerPacket
{
	private static final String _S__F3_CONFIRMDLG = "[S] f3 ConfirmDlg [dddsdddd{d}d]";
	private int _requestId;
	private String _name;
    private int _time;
    private int _loc[];	

	public ConfirmDlg(int requestId, String requestorName)
	{
		_requestId = requestId;
		_name = requestorName;
	}

	public ConfirmDlg(int requestId, String requestorName, int time, int loc[], int id)
    {
		_requestId = requestId;
        _name = requestorName;
        _time = time;
        _loc = loc;
        _requestId = id;
    }
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xf3);
		writeD(_requestId);
		writeD(0x02); // ??
		writeD(0x00); // ??
		writeS(_name);
        writeD(_time == 0 ? 6 : 7);
        writeD(_loc != null ? _loc[0] : 0);
        writeD(_loc != null ? _loc[1] : 0);
        writeD(_loc != null ? _loc[2] : 0);
        if(_time != 0)
            writeD(_time);
        writeD(_requestId);
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
