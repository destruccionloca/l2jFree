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

import java.util.Map;
import java.util.Map.Entry;

import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.model.entity.Castle;

/**
 * A reply packet sent after clients sends RequestDominionInfo.
 * 
 * @author savormix
 */
public class ExReplyDominionInfo extends L2GameServerPacket
{
	private static final String		_S__FE_92_EXREPLYDOMINIONINFO	= "[S] FE:92 ExReplyDominionInfo";
	
	private final int				_warTime;
	
	public ExReplyDominionInfo()
	{
		_warTime = (int) (System.currentTimeMillis() / 1000);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x92);
		
		Map<Integer, Castle> castles = CastleManager.getInstance().getCastles();
		writeD(castles.size());// territory count
		for (Entry<Integer, Castle> set : castles.entrySet())
		{
			Castle castle = set.getValue();
			writeD(0x50 + castle.getCastleId()); // territory ID
			writeS(castle.getName().toLowerCase() + "_dominion"); // special string
			if (castle.getOwnerId() > 0)
			{
				if (ClanTable.getInstance().getClan(castle.getOwnerId()) != null)
					writeS(ClanTable.getInstance().getClan(castle.getOwnerId()).getName()); // owner clan
				else
				{
					_log.warn("Castle owner with no name! Castle: " + castle.getName() 
							+ " has an OwnerId = " + castle.getOwnerId()
							+ " who does not have a  name!");
					writeS("");
				}
			}
			else
				writeS("");
			
			writeD(1); // emblem count
			writeD(0x50 + castle.getCastleId()); // emblem IDs (currently each ward has own emblem)
			writeD(_warTime); // next battle date
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_92_EXREPLYDOMINIONINFO;
	}
}