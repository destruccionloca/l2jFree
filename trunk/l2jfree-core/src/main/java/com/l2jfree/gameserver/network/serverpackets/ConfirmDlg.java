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
package com.l2jfree.gameserver.network.serverpackets;

import com.l2jfree.gameserver.network.SystemMessageId;

/**
 * Format: c dd[d s/d/dd/ddd] dd
 * 
 * @author kombat
 */
public class ConfirmDlg extends AbstractSystemMessage<ConfirmDlg>
{
	private static final String _S__F3_CONFIRMDLG = "[S] f3 ConfirmDlg";
	
	private int _time = 0;
	private int _requesterId = 0;
	
	public ConfirmDlg(SystemMessageId messageId)
	{
		super(messageId);
	}
	
	public ConfirmDlg(int messageId)
	{
		super(messageId);
	}
	
	public static ConfirmDlg sendString(String msg)
	{
		ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1);
		dlg.addString(msg);
		return dlg;
	}
	
	public ConfirmDlg addTime(int time)
	{
		_time = time;
		return this;
	}
	
	public ConfirmDlg addRequesterId(int id)
	{
		_requesterId = id;
		return this;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xf3);
		writeMessageIdAndElements();
		// timed dialog (Summon Friend skill request)
		writeD(_time);
		writeD(_requesterId);
	}
	
	@Override
	public String getType()
	{
		return _S__F3_CONFIRMDLG;
	}
}
