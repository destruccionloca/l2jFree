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

import java.util.Arrays;
import java.util.Comparator;

import com.l2jfree.gameserver.datatables.HennaTreeTable;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.templates.item.L2Henna;

public final class HennaEquipList extends L2GameServerPacket
{
	private static final String	_S__EE_HennaEquipList	= "[S] EE HennaEquipList";

	private final L2PcInstance	_player;
	private final L2Henna[]		_hennas;

	public HennaEquipList(L2PcInstance player)
	{
		_player = player;
		_hennas = HennaTreeTable.getInstance().getAvailableHenna(player).clone();

		Arrays.sort(_hennas, new Comparator<L2Henna>() {
			@Override
			public int compare(L2Henna o1, L2Henna o2)
			{
				return checkRequirements(o1).compareTo(checkRequirements(o2));
			}
		});
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xee);
		writeCompQ(_player.getAdena());
		writeD(3); //available equip slot
		writeD(_hennas.length);

		for (L2Henna element : _hennas)
		{
			writeD(element.getSymbolId()); //symbol ID
			writeD(element.getItemId()); //item ID of dye
			writeCompQ(element.getAmount()); //amount of dye required
			writeCompQ(element.getPrice()); //amount of adena required
			
			writeD(1); //meet the requirement(1) or not(0) - seems not working at client
		}
	}

	private final Integer checkRequirements(L2Henna henna)
	{
		final L2ItemInstance hennaItem = _player.getInventory().getItemByItemId(henna.getItemId());

		if (hennaItem == null)
			return 3;

		if (hennaItem.getCount() < henna.getAmount())
			return 2;

		if (_player.getAdena() < henna.getPrice())
			return 1;

		return 0;
	}

	@Override
	public String getType()
	{
		return _S__EE_HennaEquipList;
	}
}
