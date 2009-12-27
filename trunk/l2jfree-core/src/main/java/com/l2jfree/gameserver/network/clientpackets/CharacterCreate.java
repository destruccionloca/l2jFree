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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Calendar;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.datatables.CharNameTable;
import com.l2jfree.gameserver.datatables.CharTemplateTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.instancemanager.QuestManager;
import com.l2jfree.gameserver.instancemanager.RecommendationManager;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2ShortCut;
import com.l2jfree.gameserver.model.L2SkillLearn;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.actor.stat.PcStat;
import com.l2jfree.gameserver.model.quest.Quest;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.network.Disconnection;
import com.l2jfree.gameserver.network.L2GameClient;
import com.l2jfree.gameserver.network.serverpackets.CharCreateFail;
import com.l2jfree.gameserver.network.serverpackets.CharCreateOk;
import com.l2jfree.gameserver.network.serverpackets.CharSelectionInfo;
import com.l2jfree.gameserver.taskmanager.SQLQueue;
import com.l2jfree.gameserver.templates.chars.L2PcTemplate;
import com.l2jfree.gameserver.templates.chars.L2PcTemplate.PcTemplateItem;

/**
 * This class represents a packet sent by the client when a new character is being created.
 *
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterCreate extends L2GameClientPacket
{
	private static final String _C__0B_CHARACTERCREATE = "[C] 0B CharacterCreate";
	private static final Object CREATION_LOCK = new Object();

	// cSdddddddddddd
	private String _name;
	//private int _race;
	private byte _sex;
	private int _classId;
	/*
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	*/
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;

	@Override
	protected void readImpl()
	{
		_name      = readS();
		/*_race  = */readD();
		_sex       = (byte) readD();
		_classId   = readD();
		/*_int   = */readD();
		/*_str   = */readD();
		/*_con   = */readD();
		/*_men   = */readD();
		/*_dex   = */readD();
		/*_wit   = */readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face      = (byte) readD();
	}

	@Override
	protected void runImpl()
	{
		// Only 1 packet may be executed at a time (prevent multiple names)
		synchronized (CREATION_LOCK)
		{
			int reason = -1;
			if (CharNameTable.getInstance().doesCharNameExist(_name))
			{
				if (_log.isDebugEnabled())
					_log.debug("charname: "+ _name + " already exists. creation failed.");
				reason = CharCreateFail.REASON_NAME_ALREADY_EXISTS;
			}
			else if (CharNameTable.getInstance().accountCharNumber(getClient().getAccountName()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT && Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0)
			{
				if (_log.isDebugEnabled())
					_log.debug("Max number of characters reached. Creation failed.");
				reason = CharCreateFail.REASON_TOO_MANY_CHARACTERS;
			}
			else if (!Config.CNAME_PATTERN.matcher(_name).matches())
			{
				if (_log.isDebugEnabled())
					_log.debug("charname: " + _name + " is invalid. creation failed.");
				reason = CharCreateFail.REASON_16_ENG_CHARS;
			}

			if (_log.isDebugEnabled())
				_log.debug("charname: " + _name + " classId: " + _classId);

			L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(_classId);
			if (template == null || template.getClassBaseLevel() > 1)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}
			else if (reason != -1)
			{
				sendPacket(new CharCreateFail(reason));
				return;
			}

			int objectId = IdFactory.getInstance().getNextId();
			L2PcInstance newChar = L2PcInstance.create(objectId, template, getClient().getAccountName(),_name, _hairStyle, _hairColor, _face, _sex!=0);
			newChar.getStatus().setCurrentHp(template.getBaseHpMax());
			newChar.getStatus().setCurrentCp(template.getBaseCpMax());
			newChar.getStatus().setCurrentMp(template.getBaseMpMax());
			//newChar.setMaxLoad(template.baseLoad);

			// send acknowledgement
			sendPacket(new CharCreateOk());

			initNewChar(getClient(), newChar);
			sendAF();
		}
	}

	private void initNewChar(L2GameClient client, L2PcInstance newChar)
	{
		if (_log.isDebugEnabled()) _log.debug("Character init start");
		storeCreationDate(newChar);

		L2World.getInstance().storeObject(newChar);

		L2PcTemplate template = newChar.getTemplate();

		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);

		int[] xyz = template.getStartingPosition();
		newChar.getPosition().setXYZInvisible(xyz[0], xyz[1], xyz[2]);
		newChar.setTitle("");

		newChar.setVitalityPoints(PcStat.MAX_VITALITY_POINTS, true);

		if (Config.STARTING_LEVEL > 1)
			newChar.getStat().addLevel((byte)(Config.STARTING_LEVEL - 1));
		
		if (Config.STARTING_SP > 0)
			newChar.getStat().addSp(Config.STARTING_SP);
		
		L2ShortCut shortcut;
		//add attack shortcut
		shortcut = new L2ShortCut(0,0,3,2,0,1);
		newChar.registerShortCut(shortcut);
		//add take shortcut
		shortcut = new L2ShortCut(3,0,3,5,0,1);
		newChar.registerShortCut(shortcut);
		//add sit shortcut
		shortcut = new L2ShortCut(10,0,3,0,0,1);
		newChar.registerShortCut(shortcut);

		for (PcTemplateItem ia : template.getItems())
		{
			L2ItemInstance item = newChar.getInventory().addItem("Init", ia.getItemId(), ia.getAmount(), newChar, null);

			// add tutorial guide shortcut
			if (item.getItemId() == 5588)
			{
				shortcut = new L2ShortCut(11, 0, 1, item.getObjectId(), 0, 1);
				newChar.registerShortCut(shortcut);
			}

			if (item.isEquipable() && ia.isEquipped())
				newChar.getInventory().equipItemAndRecord(item);
		}

		SQLQueue.getInstance().run();

		for (L2SkillLearn skill: SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId()))
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);
			if (skill.getId() == 1001 || skill.getId() == 1177)
			{
				shortcut = new L2ShortCut(1, 0, 2, skill.getId(), skill.getLevel(), 1);
				newChar.registerShortCut(shortcut);
			}
			if (skill.getId() == 1216)
			{
				shortcut = new L2ShortCut(10, 0, 2, skill.getId(), skill.getLevel(), 1);
				newChar.registerShortCut(shortcut);
			}
			if (_log.isDebugEnabled())
				_log.debug("adding starter skill:" + skill.getId() + " / " + skill.getLevel());
		}
		startTutorialQuest(newChar);
		RecommendationManager.getInstance().onCreate(newChar);
		new Disconnection(getClient(), newChar).store().deleteMe();

		// send char list
		CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
		if (_log.isDebugEnabled())
			_log.debug("Character init end");
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

	private final void storeCreationDate(L2PcInstance player)
	{
		Connection con = null;
		try
		{
			Calendar now = Calendar.getInstance();
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO character_birthdays VALUES (?,?,?)");
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, now.get(Calendar.YEAR));
			ps.setDate(3, new Date(now.getTimeInMillis()));
			ps.executeUpdate();
			ps.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store " + player + "'s creation day!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	@Override
	public String getType()
	{
		return _C__0B_CHARACTERCREATE;
	}
}
