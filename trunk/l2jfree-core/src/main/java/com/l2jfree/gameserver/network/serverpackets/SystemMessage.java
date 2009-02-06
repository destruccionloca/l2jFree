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

import java.util.List;

import javolution.util.FastList;

import com.l2jfree.gameserver.model.L2Character;
import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.templates.item.L2Item;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;


/**
 * This class ...
 * 
 * @version $Revision: 1.18.2.5.2.8 $ $Date: 2005/04/05 19:41:08 $
 */
public class SystemMessage extends L2GameServerPacket
{
	// d d (d S/d d/d dd)
	//      |--------------> 0 - String  1-number 2-textref npcname (1000000-1002655)  3-textref itemname 4-textref skills 5-??
	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	private static final String _S__7A_SYSTEMMESSAGE = "[S] 64 SystemMessage";
	private int _messageId;
	private List<Integer> _types = new FastList<Integer>(2); // Average parameter size for most common messages
	private List<Object> _values = new FastList<Object>(2); // Average parameter size for most common messages
	private int _skillLvL = 1;
	
	public SystemMessage(SystemMessageId messageId)
	{
		_messageId = messageId.getId();
	}
	
	public SystemMessage(int messageId)
	{
		_messageId = messageId;
	}
	
 	public static SystemMessage sendString(String msg)
	{
 		SystemMessage sm = new SystemMessage(SystemMessageId.S1);
 		sm.addString(msg);
 		
 		return sm;
	}
 	
	public SystemMessage addString(String text)
	{
		_types.add(TYPE_TEXT);
		_values.add(text);
		
		return this;
	}

	public SystemMessage addNumber(int number)
	{
		_types.add(TYPE_NUMBER);
		_values.add(number);
		return this;
	}

	public SystemMessage addCharName(L2Character cha)
	{
		if (cha instanceof L2NpcInstance)
			return addNpcName((L2NpcInstance)cha);
		if (cha instanceof L2PcInstance)
			return addPcName((L2PcInstance)cha);
		if (cha instanceof L2Summon)
			return addNpcName((L2Summon)cha);
		return addString(cha.getName());
	}

	public SystemMessage addPcName(L2PcInstance pc)
	{
		return addString(pc.getAppearance().getVisibleName());
	}

	public SystemMessage addNpcName(L2NpcInstance npc)
	{
		return addNpcName(npc.getTemplate());
	}

	public SystemMessage addNpcName(L2Summon npc)
	{
		return addNpcName(npc.getTemplate());
	}

	public SystemMessage addNpcName(L2NpcTemplate tpl)
	{
		if (tpl.isCustom())
			return addString(tpl.getName());
		return addNpcName(tpl.getNpcId());
	}
	
	public SystemMessage addNpcName(int id)
	{
		_types.add(TYPE_NPC_NAME);
		_values.add(1000000 + id);
		
		return this;
	}

	public SystemMessage addItemName(L2ItemInstance item)
	{
		return addItemName(item.getItem());
	}

	public SystemMessage addItemName(L2Item item)
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

	public SystemMessage addItemName(int id)
	{
		_types.add(TYPE_ITEM_NAME);
		_values.add(id);
		
		return this;
	}

	public SystemMessage addZoneName(int x, int y, int z)
	{
		_types.add(TYPE_ZONE_NAME);
		int[] coord = {x, y, z};
		_values.add(coord);
		
		return this;
	}

	public SystemMessage addSkillName(L2Effect effect)
	{
		return addSkillName(effect.getSkill());
	}

	public SystemMessage addSkillName(L2Skill skill)
	{
		if (skill.getId() != skill.getDisplayId()) //custom skill -  need nameId or smth like this.
			return addString(skill.getName());
		return addSkillName(skill.getId(), skill.getLevel());
	}

	public SystemMessage addSkillName(int id)
	{
		return addSkillName(id, 1);
	}

	public SystemMessage addSkillName(int id, int lvl)
	{
		_types.add(TYPE_SKILL_NAME);
		_values.add(id);
		_skillLvL = lvl;
		
		return this;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x62);

		writeD(_messageId);
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
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__7A_SYSTEMMESSAGE;
	}
	
	public int getMessageID()
	{
		return _messageId;
	}
}