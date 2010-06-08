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

public class RestartResponse extends L2GameServerPacket
{
	private static final String			_S__RESTARTRESPONSE	= "[S] 71 RestartResponse c[ds]";
	
	public static final RestartResponse	PACKET_SUCCESS				= new RestartResponse(true);
	public static final RestartResponse	PACKET_FAILED				= new RestartResponse(false);
	
	private boolean						_result;
	
	private RestartResponse(boolean result)
	{
		 _result = result;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x71);
		writeD(_result ? 1 : 0);
	}
	
	@Override
	public String getType()
	{
		return _S__RESTARTRESPONSE;
	}
}
