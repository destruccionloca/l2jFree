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

import java.util.Vector;

import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.templates.item.L2Item;

/**
 * @author kombat
 * Format: c dd[d s/d/dd/ddd] dd
 */
public class ConfirmDlg extends L2GameServerPacket
{
	private static final String _S__F3_CONFIRMDLG = "[S] f3 ConfirmDlg";
	private int _messageId;

	private int _skillLvL = 1;

	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;

	private Vector<Integer> _types = new Vector<Integer>(2); // Average parameter size for most common messages
	private Vector<Object> _values = new Vector<Object>(2); // Average parameter size for most common messages

	private int _time = 0;
	private int _requesterId = 0;

	public ConfirmDlg(int messageId)
	{
		_messageId = messageId;
	}

	public ConfirmDlg addString(String text)
	{
		_types.add(TYPE_TEXT);
		_values.add(text);
		return this;
	}

	public ConfirmDlg addNumber(int number)
	{
		_types.add(TYPE_NUMBER);
		_values.add(number);
		return this;
	}

	public ConfirmDlg addCharName(L2Character cha)
	{
		if (cha instanceof L2Npc)
			return addNpcName((L2Npc)cha);
		if (cha instanceof L2PcInstance)
			return addPcName((L2PcInstance)cha);
		if (cha instanceof L2Summon)
			return addNpcName((L2Summon)cha);
		return addString(cha.getName());
	}

	public ConfirmDlg addPcName(L2PcInstance pc)
	{
		return addString(pc.getAppearance().getVisibleName());
	}

	public ConfirmDlg addNpcName(L2Npc npc)
	{
		return addNpcName(npc.getTemplate());
	}

	public ConfirmDlg addNpcName(L2Summon npc)
	{
		return addNpcName(npc.getNpcId());
	}

	public ConfirmDlg addNpcName(L2NpcTemplate tpl)
	{
		if (tpl.isCustom())
			return addString(tpl.getName());
		return addNpcName(tpl.getNpcId());
	}

	public ConfirmDlg addNpcName(int id)
	{
		_types.add(TYPE_NPC_NAME);
		_values.add(1000000 + id);
		return this;
	}

	public ConfirmDlg addItemName(L2ItemInstance item)
	{
		return addItemName(item.getItem().getItemId());
	}

	public ConfirmDlg addItemName(L2Item item)
	{
		if(item.getItemDisplayId() == item.getItemId())
		{
			_types.add(TYPE_ITEM_NAME);
			_values.add(item.getItemId());
		}
		else
		{
			// Custom item - send custom name
			_types.add(TYPE_TEXT);
			_values.add(item.getName());
		}
		return this;
	}

	public ConfirmDlg addItemName(int id)
	{
		_types.add(TYPE_ITEM_NAME);
		_values.add(id);
		return this;
	}

	public ConfirmDlg addZoneName(int x, int y, int z)
	{
		_types.add(TYPE_ZONE_NAME);
		int[] coord = {x, y, z};
		_values.add(coord);
		return this;
	}

	public ConfirmDlg addSkillName(L2Effect effect)
	{
		return addSkillName(effect.getSkill());
	}

	public ConfirmDlg addSkillName(L2Skill skill)
	{
		if (skill.getId() != skill.getDisplayId()) //custom skill -  need nameId or smth like this.
			return addString(skill.getName());
		return addSkillName(skill.getId(), skill.getLevel());
	}

	public ConfirmDlg addSkillName(int id)
	{
		return addSkillName(id, 1);
	}

	public ConfirmDlg addSkillName(int id, int lvl)
	{
		_types.add(TYPE_SKILL_NAME);
		_values.add(id);
		_skillLvL = lvl;
		return this;
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
		writeD(_messageId);

		if (_types != null && !_types.isEmpty())
		{
			writeD(_types.size());
			for (int i = 0; i < _types.size(); i++)
			{
				int t = _types.get(i);

				writeD(t);

				switch (t)
				{
					case TYPE_TEXT:
					{
						writeS( (String)_values.get(i));
						break;
					}
					case TYPE_NUMBER:
					case TYPE_NPC_NAME:
					case TYPE_ITEM_NAME:
					{
						int t1 = (Integer) _values.get(i);
						writeD(t1);
						break;
					}
					case TYPE_SKILL_NAME:
					{
						int t1 = (Integer) _values.get(i);
						writeD(t1); // Skill Id
						writeD(_skillLvL); // Skill lvl
						break;
					}
					case TYPE_ZONE_NAME:
					{
						int t1 = ((int[])_values.get(i))[0];
						int t2 = ((int[])_values.get(i))[1];
						int t3 = ((int[])_values.get(i))[2];
						writeD(t1);
						writeD(t2);
						writeD(t3);
						break;
					}
				}
			}
			// timed dialog (Summon Friend skill request)
			writeD(_time);
			writeD(_requesterId);
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__F3_CONFIRMDLG;
	}
}