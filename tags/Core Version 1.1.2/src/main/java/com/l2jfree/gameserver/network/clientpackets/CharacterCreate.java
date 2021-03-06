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
package com.l2jfree.gameserver.network.clientpackets;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.TaskPriority;
import com.l2jfree.gameserver.datatables.CharNameTable;
import com.l2jfree.gameserver.datatables.CharTemplateTable;
import com.l2jfree.gameserver.datatables.ItemTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.instancemanager.QuestManager;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2ShortCut;
import com.l2jfree.gameserver.model.L2SkillLearn;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.quest.Quest;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.serverpackets.CharCreateFail;
import com.l2jfree.gameserver.network.serverpackets.CharCreateOk;
import com.l2jfree.gameserver.network.serverpackets.CharSelectionInfo;
import com.l2jfree.gameserver.templates.L2Item;
import com.l2jfree.gameserver.templates.L2PcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:30 $
 */
@SuppressWarnings("unused")
public class CharacterCreate extends L2GameClientPacket
{
	private static final String _C__0B_CHARACTERCREATE = "[C] 0B CharacterCreate";
	private final static Log _log = LogFactory.getLog(CharacterCreate.class.getName());
	
	// cSdddddddddddd
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;
	
	public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }
	
	/**
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		
		_name      = readS();
		_race      = readD();
		_sex       = (byte)readD();
		_classId   = readD();
		_int       = readD();
		_str       = readD();
		_con       = readD();
		_men       = readD();
		_dex       = readD();
		_wit       = readD();
		_hairStyle = (byte)readD();
		_hairColor = (byte)readD();
		_face      = (byte)readD();
	}

	@Override
	protected void runImpl()
	{
		if (CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
		{
			if (_log.isDebugEnabled())
				_log.debug("Max number of characters reached. Creation failed.");
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS);
			sendPacket(ccf);
			return;
		}
		else if (CharNameTable.getInstance().doesCharNameExist(_name))
		{
			if (_log.isDebugEnabled())
				_log.debug("charname: "+ _name + " already exists. creation failed.");
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS);
			sendPacket(ccf);
			return;
		}
		else if (!Config.CNAME_PATTERN.matcher(_name).matches())
		{
			if (_log.isDebugEnabled()) 
				_log.debug("charname: " + _name + " is invalid. creation failed.");
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_16_ENG_CHARS);
			sendPacket(ccf);
			return;
		}

		if (_log.isDebugEnabled())
			_log.debug("charname: " + _name + " classId: " + _classId);
		
		L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(_classId);
		if(template == null || template.getClassBaseLevel() > 1) 
		{
			CharCreateFail ccf = new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED);
			sendPacket(ccf);
			return;
		}
		
		int objectId = IdFactory.getInstance().getNextId();
		L2PcInstance newChar = L2PcInstance.create(objectId, template, getClient().getAccountName(),_name, _hairStyle, _hairColor, _face, _sex!=0);
		newChar.getStatus().setCurrentHp(template.getBaseHpMax());
		newChar.getStatus().setCurrentCp(template.getBaseCpMax());
		newChar.getStatus().setCurrentMp(template.getBaseMpMax());
		//newChar.setMaxLoad(template.baseLoad);
		
		// send acknowledgement
		CharCreateOk cco = new CharCreateOk();
		sendPacket(cco);

		initNewChar(getClient(), newChar);
	}
	
	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{   
		if (_log.isDebugEnabled()) _log.debug("Character init start");
		L2World.getInstance().storeObject(newChar);
		
		L2PcTemplate template = newChar.getTemplate();
		
		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		
		newChar.getPosition().setXYZInvisible(template.getSpawnX(), template.getSpawnY(), template.getSpawnZ());
		newChar.setTitle("");
		
		L2ShortCut shortcut;
		//add attack shortcut
		shortcut = new L2ShortCut(0,0,3,2,-1,1);
		newChar.registerShortCut(shortcut);
		//add take shortcut
		shortcut = new L2ShortCut(3,0,3,5,-1,1);
		newChar.registerShortCut(shortcut);
		//add sit shortcut
		shortcut = new L2ShortCut(10,0,3,0,-1,1);
		newChar.registerShortCut(shortcut);
		
		L2Item[] items = template.getItems();
		for (L2Item element : items)
		{
			L2ItemInstance item = newChar.getInventory().addItem("Init", element.getItemId(), 1, newChar, null);
			if (item.getItemId()==5588)
			{
				//add tutbook shortcut
				shortcut = new L2ShortCut(11,0,1,item.getObjectId(),-1,1);
				newChar.registerShortCut(shortcut);
			}
			if (item.isEquipable())
			{
				if (newChar.getActiveWeaponItem() == null || !(item.getItem().getType2() != L2Item.TYPE2_WEAPON))
					newChar.getInventory().equipItemAndRecord(item);
			}
		}
		
		L2SkillLearn[] startSkills = SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId());
		for (L2SkillLearn element : startSkills)
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(element.getId(), element.getLevel()), true);
			if (element.getId()==1001 || element.getId()==1177)
			{
				shortcut = new L2ShortCut(1,0,2,element.getId(),1,1);
				newChar.registerShortCut(shortcut);
			}
			if (element.getId()==1216)
			{
				shortcut = new L2ShortCut(10,0,2,element.getId(),1,1);
				newChar.registerShortCut(shortcut);
			}
			if (_log.isDebugEnabled()) 
				_log.debug("adding starter skill:" + element.getId()+ " / "+ element.getLevel());
		}
		startTutorialQuest(newChar);
		L2GameClient.saveCharToDisk(newChar);
		newChar.deleteMe(); // release the world of this character and it's inventory
		
		// send char list
		
		CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.getConnection().sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
		if (_log.isDebugEnabled()) _log.debug("Character init end");
	}

	public void startTutorialQuest(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");
		Quest q = null;
		if (qs == null)
			q = QuestManager.getInstance().getQuest("255_Tutorial");
		if (q != null)
			q.newQuestState(player);
	}

	
	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__0B_CHARACTERCREATE;
	}
}
