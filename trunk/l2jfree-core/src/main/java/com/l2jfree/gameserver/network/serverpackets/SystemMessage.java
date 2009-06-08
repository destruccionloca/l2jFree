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

import com.l2jfree.gameserver.model.L2Effect;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.L2Summon;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.templates.item.L2Item;

public final class SystemMessage extends L2GameServerPacket
{
	private static abstract class Element
	{
		public final void write(SystemMessage sm)
		{
			sm.writeD(getType());
			
			write2(sm);
		}
		
		protected abstract int getType();
		
		protected abstract void write2(SystemMessage sm);
	}
	
	private static final class TextElement extends Element
	{
		private final String _text;
		
		private TextElement(String text)
		{
			_text = text;
		}
		
		@Override
		protected int getType()
		{
			return TYPE_TEXT;
		}
		
		@Override
		protected void write2(SystemMessage sm)
		{
			sm.writeS(_text);
		}
	}
	
	private static class NumberElement extends Element
	{
		private final int _number;
		
		private NumberElement(int number)
		{
			_number = number;
		}
		
		@Override
		protected int getType()
		{
			return TYPE_NUMBER;
		}
		
		@Override
		protected final void write2(SystemMessage sm)
		{
			sm.writeD(_number);
		}
	}
	
	private static final class FortElement extends NumberElement
	{
		private FortElement(int fortId)
		{
			super(fortId);
		}
		
		@Override
		protected int getType()
		{
			return TYPE_FORTRESS;
		}
	}
	
	private static final class NpcElement extends NumberElement
	{
		private NpcElement(int npcId)
		{
			super(npcId);
		}
		
		@Override
		protected int getType()
		{
			return TYPE_NPC_NAME;
		}
	}
	
	private static final class ItemElement extends NumberElement
	{
		private ItemElement(int itemId)
		{
			super(itemId);
		}
		
		@Override
		protected int getType()
		{
			return TYPE_ITEM_NAME;
		}
	}
	
	private static final class LongNumberElement extends Element
	{
		private final long _number;
		
		private LongNumberElement(long number)
		{
			_number = number;
		}
		
		@Override
		protected int getType()
		{
			return TYPE_ITEM_NUMBER;
		}
		
		@Override
		protected void write2(SystemMessage sm)
		{
			sm.writeQ(_number);
		}
	}
	
	private static final class SkillElement extends Element
	{
		private final int _skillId;
		private final int _skillLvl;
		
		private SkillElement(int skillId, int skillLvl)
		{
			_skillId = skillId;
			_skillLvl = skillLvl;
		}
		
		@Override
		protected int getType()
		{
			return TYPE_SKILL_NAME;
		}
		
		@Override
		protected void write2(SystemMessage sm)
		{
			sm.writeD(_skillId);
			sm.writeD(_skillLvl);
		}
	}
	
	private static final class LocElement extends Element
	{
		private final int _x;
		private final int _y;
		private final int _z;
		
		private LocElement(int x, int y, int z)
		{
			_x = x;
			_y = y;
			_z = z;
		}
		
		@Override
		protected int getType()
		{
			return TYPE_ZONE_NAME;
		}
		
		@Override
		protected void write2(SystemMessage sm)
		{
			sm.writeD(_x);
			sm.writeD(_y);
			sm.writeD(_z);
		}
	}
	
	private static final String _S__7A_SYSTEMMESSAGE = "[S] 64 SystemMessage";
	
	// d d (d S/d d/d dd)
	//      |--------------> 0 - String  1-number 2-textref npcname (1000000-1002655)  3-textref itemname 4-textref skills 5-fortress names
	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_ITEM_NUMBER = 6;
	private static final int TYPE_FORTRESS = 5; // maybe not only for fortress, rename if needed
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	
	private final int _messageId;
	private Element[] _elements;
	
	public SystemMessage(SystemMessageId messageId)
	{
		_messageId = messageId.getId();
		
		_elements = new Element[messageId.size()];
	}
	
	public SystemMessage(int messageId)
	{
		_messageId = messageId;
		
		SystemMessageId smId = SystemMessageId.getSystemMessageId(messageId, false);
		
		_elements = new Element[smId == null ? 3 : smId.size()];
	}
	
	public int length()
	{
		return _elements.length;
	}
	
	private boolean checkNPE(Object obj)
	{
		if (obj == null)
			_log.warn("", new NullPointerException());
		
		return obj == null;
	}
	
	private void addElement(Element element)
	{
		for (int i = 0; i < _elements.length; i++)
		{
			if (_elements[i] == null)
			{
				_elements[i] = element;
				return;
			}
		}
		
		_log.warn("SystemMessage: Too much parameter for ID: " + _messageId, new ArrayIndexOutOfBoundsException());
		
		_elements = Arrays.copyOf(_elements, _elements.length + 1);
		_elements[_elements.length - 1] = element;
	}
	
	@Override
	public void prepareToSend(L2GameClient client, L2PcInstance activeChar)
	{
		int count = 0;
		
		for (Element element : _elements)
		{
			if (element == null)
				break;
			
			count++;
		}
		
		if (count == _elements.length)
			return;
		
		_elements = Arrays.copyOf(_elements, count);
		
		_log.warn("SystemMessage: Empty parameter for ID: " + _messageId, new ArrayIndexOutOfBoundsException());
	}
	
	public static SystemMessage sendString(String msg)
	{
		SystemMessage sm = new SystemMessage(SystemMessageId.S1);
		sm.addString(msg);
		
		return sm;
	}
	
	public SystemMessage addString(String text)
	{
		if (checkNPE(text))
			return this;
		
		addElement(new TextElement(text));
		
		return this;
	}
	
	public SystemMessage add(String text)
	{
		return addString(text);
	}
	
	public SystemMessage addFortId(int number)
	{
		addElement(new FortElement(number));
		
		return this;
	}
	
	public SystemMessage addItemNumber(long number)
	{
		addElement(new LongNumberElement(number));
		
		return this;
	}
	
	public SystemMessage addExpNumber(long number)
	{
		addElement(new LongNumberElement(number));
		
		return this;
	}
	
	public SystemMessage addNumber(int number)
	{
		addElement(new NumberElement(number));
		
		return this;
	}
	
	public SystemMessage addCharName(L2Character cha)
	{
		if (checkNPE(cha))
			return this;
		
		if (cha instanceof L2Npc)
			return addNpcName((L2Npc)cha);
		
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
	
	public SystemMessage addNpcName(L2Npc npc)
	{
		return addNpcName(npc.getTemplate());
	}
	
	public SystemMessage addNpcName(L2Summon npc)
	{
		return addNpcName(npc.getTemplate());
	}
	
	public SystemMessage addNpcName(L2NpcTemplate tpl)
	{
		if (tpl.isCustom() || tpl.isServerSideName())
			return addString(tpl.getName());
		
		return addNpcName(tpl.getNpcId());
	}
	
	public SystemMessage addNpcName(int id)
	{
		addElement(new NpcElement(1000000 + id));
		
		return this;
	}
	
	public SystemMessage addItemName(L2ItemInstance item)
	{
		if (checkNPE(item))
			return this;
		
		return addItemName(item.getItem());
	}
	
	public SystemMessage addItemName(L2Item item)
	{
		if (checkNPE(item))
			return this;
		
		if (item.getItemDisplayId() == item.getItemId())
			return addItemName(item.getItemId());
		else
			// Custom item - send custom name
			return addString(item.getName());
	}
	
	public SystemMessage addItemName(int id)
	{
		addElement(new ItemElement(id));
		
		return this;
	}
	
	public SystemMessage addZoneName(int x, int y, int z)
	{
		addElement(new LocElement(x, y, z));
		
		return this;
	}
	
	public SystemMessage addSkillName(L2Effect effect)
	{
		if (checkNPE(effect))
			return this;
		
		return addSkillName(effect.getSkill());
	}
	
	public SystemMessage addSkillName(L2Skill skill)
	{
		if (checkNPE(skill))
			return this;
		
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
		addElement(new SkillElement(id, lvl));
		
		return this;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x62);
		
		writeD(_messageId);
		writeD(_elements.length);
		
		for (Element element : _elements)
			element.write(this);
	}
	
	@Override
	public String getType()
	{
		return _S__7A_SYSTEMMESSAGE;
	}
}
