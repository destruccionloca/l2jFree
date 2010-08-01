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

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ExEnchantSkillInfo;

/**
 * Format (ch) dd
 * c: (id) 0xD0
 * h: (subid) 0x06
 * d: skill id
 * d: skill lvl
 * 
 * @author -Wooden-
 */
public final class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private static final String _C__D0_06_REQUESTEXENCHANTSKILLINFO = "[C] D0:06 RequestExEnchantSkillInfo";
	
	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		/*
		final L2Npc trainer = activeChar.getLastFolkNPC();
		if (!(trainer instanceof L2NpcInstance))
			return;
		
		if (!trainer.canInteract(activeChar) && !activeChar.isGM())
		{
			requestFailed(SystemMessageId.TOO_FAR_FROM_NPC);
			return;
		}
		*/
		
		if (activeChar.getLevel() < 76)
		{
			requestFailed(SystemMessageId.YOU_DONT_MEET_SKILL_LEVEL_REQUIREMENTS);
			return;
		}
		if (activeChar.getClassId().level() < 3)
		{
			requestFailed(SystemMessageId.NOT_COMPLETED_QUEST_FOR_SKILL_ACQUISITION);
			return;
		}
		
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if (skill == null || skill.getId() != _skillId)
		{
			requestFailed(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
			return;
		}
		
		/*
		 * if (!skill.canTeachBy(trainer.getNpcId()) || !skill.getCanLearn(activeChar.getClassId()))
		 * {
		 * if (!Config.ALT_GAME_SKILL_LEARN)
		 * {
		 * sendPacket(ActionFailed.STATIC_PACKET);
		 * Util.handleIllegalPlayerAction(activeChar, "Client " + getClient() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
		 * return;
		 * }
		 * }
		 */

		if (SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId) == null)
			return;
		
		int playerSkillLvl = activeChar.getSkillLevel(_skillId);
		if (playerSkillLvl == -1 || playerSkillLvl != _skillLvl)
			return;
		
		sendPacket(new ExEnchantSkillInfo(_skillId, _skillLvl));
		sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_06_REQUESTEXENCHANTSKILLINFO;
	}
}
