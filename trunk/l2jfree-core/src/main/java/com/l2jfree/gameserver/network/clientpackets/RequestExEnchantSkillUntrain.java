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

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.model.L2EnchantSkillLearn;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2ShortCut;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2EnchantSkillLearn.EnchantSkillDetail;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.PcInventory;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ExEnchantSkillInfo;
import com.l2jfree.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import com.l2jfree.gameserver.network.serverpackets.ExEnchantSkillResult;
import com.l2jfree.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.UserInfo;

/**
 * Format (ch) dd
 * c: (id) 0xD0
 * h: (subid) 0x33
 * d: skill id
 * d: skill lvl
 * 
 * @author -Wooden-
 */
public final class RequestExEnchantSkillUntrain extends L2GameClientPacket
{
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
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		/*
		L2Npc trainer = player.getLastFolkNPC();
		if (!(trainer instanceof L2NpcInstance))
			return;
		
		if (!trainer.canInteract(player) && !player.isGM())
		{
			requestFailed(SystemMessageId.TOO_FAR_FROM_NPC);
			return;
		}
		*/
		
		if (player.getLevel() < 76)
		{
			requestFailed(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ON_THIS_LEVEL);
			return;
		}
		
		if (player.getClassId().level() < 3) // requires to have 3rd class quest completed
		{
			requestFailed(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_IN_THIS_CLASS);
			return;
		}
		
		if (!player.isAllowedToEnchantSkills())
		{
			requestFailed(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ATTACKING_TRANSFORMED_BOAT);
			return;
		}
		
		L2EnchantSkillLearn s = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			requestFailed(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
			return;
		}
		
		if (_skillLvl % 100 == 0)
			_skillLvl = s.getBaseLevel();
		
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if (skill == null)
		{
			requestFailed(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
			return;
		}
		
		/*
		 * int npcid = trainer.getNpcId();
		 * if (!skill.canTeachBy(npcid) || !skill.getCanLearn(player.getClassId()))
		 * {
		 * if (!Config.ALT_GAME_SKILL_LEARN)
		 * {
		 * sendPacket(ActionFailed.STATIC_PACKET);
		 * Util.handleIllegalPlayerAction(player, "Client "+getClient()+" tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
		 * return;
		 * }
		 * }
		 */

		int reqItemId = SkillTreeTable.UNTRAIN_ENCHANT_BOOK;
		
		int currentLevel = player.getSkillLevel(_skillId);
		if (currentLevel - 1 != _skillLvl && (currentLevel % 100 != 1 || _skillLvl != s.getBaseLevel()))
		{
			requestFailed(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
			return;
		}
		
		EnchantSkillDetail esd = s.getEnchantSkillDetail(currentLevel);
		int requiredSp = esd.getSpCost();
		int requiredAdena = esd.getAdenaCost();
		
		L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
		if (Config.ALT_ES_SP_BOOK_NEEDED && spb == null)
		{
			requestFailed(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		if (player.getInventory().getAdena() < requiredAdena)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
			return;
		}
		
		boolean check = true;
		if (Config.ALT_ES_SP_BOOK_NEEDED)
			check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
		
		check &= player.destroyItemByItemId("Consume", PcInventory.ADENA_ID, requiredAdena, player, true);
		
		if (!check)
		{
			requestFailed(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		player.getStat().addSp((int)(requiredSp * 0.8));
		
		player.addSkill(skill, true);
		sendPacket(new ExEnchantSkillResult(true));
		
		if (_log.isDebugEnabled())
			_log.info("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requiredAdena + " Adena.");
		
		sendPacket(new UserInfo(player));
		
		SystemMessage sm;
		if (_skillLvl > 100)
		{
			sm = new SystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_DECREASED_BY_ONE);
			sm.addSkillName(_skillId);
			sendPacket(sm);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_RESETED);
			sm.addSkillName(_skillId);
			sendPacket(sm);
		}
		
		player.sendSkillList();
		
		sendPacket(new ExEnchantSkillInfo(_skillId, player.getSkillLevel(_skillId)));
		sendPacket(new ExEnchantSkillInfoDetail(ExEnchantSkillInfoDetail.TYPE_UNTRAIN_ENCHANT, _skillId, player
				.getSkillLevel(_skillId) - 1, player));
		
		updateSkillShortcuts(player);
	}
	
	private void updateSkillShortcuts(L2PcInstance player)
	{
		// update all the shortcuts to this skill
		L2ShortCut[] allShortCuts = player.getAllShortCuts();
		
		for (L2ShortCut sc : allShortCuts)
		{
			if (sc.getId() == _skillId && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), player.getSkillLevel(_skillId), 1);
				player.sendPacket(new ShortCutRegister(newsc));
				player.registerShortCut(newsc);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:33 RequestExEnchantSkillUntrain";
	}
}
