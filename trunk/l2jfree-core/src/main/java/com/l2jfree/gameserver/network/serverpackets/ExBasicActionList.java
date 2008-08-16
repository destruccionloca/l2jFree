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
 *
 * @author  KenM/Crion
 */
public class ExBasicActionList extends L2GameServerPacket
{
	private static final String _S__FE_5E_EXBASICACTIONLIST = "[S] FE:5F ExBasicActionList";

	private final int[] _actionIds;

	private static final int[] DEFAULT_ACTIONS = 
	{
		0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 24, 25, 26, 28, 29, 30, 31, 33, 34, 35, 37, 38, 40, 50, 51, 55, 56, 57, 58, 59, 60, 61, 62
	};

	private static final int[] TRANSFORMED_ACTIONS = 
	{
		2, 3, 4, 5, 6, 7, 8, 9, 11, 40, 50, 55, 56, 57
	};

	public static final ExBasicActionList DEFAULT_ACTION_LIST = new ExBasicActionList(DEFAULT_ACTIONS);
	public static final ExBasicActionList TRANSFORMED_ACTION_LIST = new ExBasicActionList(TRANSFORMED_ACTIONS);

	/*
	0 - Sit/Stand
	1 - Walk/Run
	2 - Attack
	3 - Exchange
	4 - Next Target
	5 - Pick Up
	6 - Assist
	7 - Invite
	8 - Leave Party
	9 - Dismiss Party Member
	10 - Private Store - Sell
	11 - Party Matching
	12 - Greeting
	13 - Victory
	14 - Advance
	15 - 
	...
	23 - 
	24 - Yes
	25 - No
	26 - Bow
	27 - 
	28 - Private Store - Buy
	29 - Unaware
	30 - Social Waiting
	31 - Laugh
	32 - 
	33 - Applaud
	34 - Dance
	35 - Sorrow
	36 - 
	37 - Dwarven Manufacture
	38 - Mount/Dismount
	39 - 
	40 - Recommend
	41 - 
	...
	49 - 
	50 - Change of Party Leader
	51 - General Manufacture
	52 - 
	53 -
	54 -
	55 - Start/End Recording Replay
	56 - Command Channel Invitation
	57 - Find Store
	58 - Duel
	59 - Withdraw
	60 - Party Duel
	61 - Package Sale
	62 - Charm
	*/

	private ExBasicActionList(int... actionIds)
	{
		_actionIds = actionIds;
	}

	/**
	* @see com.l2jfree.gameserver.serverpackets.L2GameServerPacket#writeImpl()
	*/
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x5f);
		writeD(_actionIds.length);
		for (int i = 0; i < _actionIds.length; i++)
		{
			writeD(_actionIds[i]);
		}
	}

	/**
	* @see com.l2jfree.gameserver.serverpackets.L2GameServerPacket#getType()
	*/
	@Override
	public String getType()
	{
		return _S__FE_5E_EXBASICACTIONLIST;
	}
}
