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

/**
 * format: dSSSdddddddd (dd(d))
 * @author GodKratos & savormix
 */
public class ExShowDominionRegistry extends L2GameServerPacket
{
	private final int _territoryId;
	private final int _clanReq = 0x00;
	private final int _mercReq = 0x00;
	private final int _warTime = (int) (System.currentTimeMillis() / 1000);
	private final int _currentTime = (int) (System.currentTimeMillis() / 1000);
	private final int _clanTerrId;
	private final int _playerTerrId;
	private final boolean _canRequest = false;

	public ExShowDominionRegistry(int terrId, int clanTerrId, int playerTerrId)
	{
		_territoryId = terrId;
		_clanTerrId = clanTerrId;
		_playerTerrId = playerTerrId;
	}

    @Override
    public String getType()
    {
        return "[S] FE:90 ExShowDominionRegistry";
    }

    @Override
    protected void writeImpl()
    {
        writeC(0xfe);
        writeH(0x90);

        writeD(_territoryId); // Current Territory Id
        writeS("No Clan");    // Owners Clan
        writeS("No Owner");   // Owner Clan Leader
        writeS("No Ally");    // Owner Alliance
        writeD(_clanReq); // Clan Request
        writeD(_mercReq); // Merc Request
        writeD(_warTime); // War Time
        writeD(_currentTime); // Current Time
        writeD(_clanTerrId); // Clan's Merc. Request
        writeD(_playerTerrId); // Player's Merc. Request
        writeD(_canRequest); // Is request period
        writeD(0x09); // Territory Count
        for (int i = 0; i < 9; i++)
        {
        	writeD(0x51 + i); // Territory Id
        	writeD(0x01);     // Emblem Count
        	writeD(0x51 + i); // Emblem ID - should be in for loop for emblem count
        }
    }
}
