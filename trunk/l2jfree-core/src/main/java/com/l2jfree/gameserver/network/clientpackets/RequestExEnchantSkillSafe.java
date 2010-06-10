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
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.itemcontainer.PcInventory;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ExEnchantSkillInfo;
import com.l2jfree.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import com.l2jfree.gameserver.network.serverpackets.ExEnchantSkillResult;
import com.l2jfree.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.UserInfo;
import com.l2jfree.tools.random.Rnd;

/**
 * Format (ch) dd
 * c: (id) 0xD0
 * h: (subid) 0x32
 * d: skill id
 * d: skill lvl
 * 
 * @author -Wooden-
 */
public final class RequestExEnchantSkillSafe extends L2GameClientPacket
{
	private int	_skillId;
	private int	_skillLvl;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		final L2Npc trainer = player.getLastFolkNPC();
		if (!(trainer instanceof L2NpcInstance))
			return;
		
		if (!trainer.canInteract(player) && !player.isGM())
		{
			requestFailed(SystemMessageId.TOO_FAR_FROM_NPC);
			return;
		}
		
		else if (player.getLevel() < 76)
		{
			requestFailed(SystemMessageId.YOU_DONT_MEET_SKILL_LEVEL_REQUIREMENTS);
			return;
		}
		else if (player.getClassId().level() < 3)
		{
			requestFailed(SystemMessageId.NOT_COMPLETED_QUEST_FOR_SKILL_ACQUISITION);
			return;
		}
		
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

		int costMultiplier = SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
		int reqItemId = SkillTreeTable.SAFE_ENCHANT_BOOK;
		
		L2EnchantSkillLearn s = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			requestFailed(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
			return;
		}
		EnchantSkillDetail esd = s.getEnchantSkillDetail(_skillLvl);
		if (player.getSkillLevel(_skillId) != esd.getMinSkillLevel())
		{
			requestFailed(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
			return;
		}
		
		int requiredSp = esd.getSpCost() * costMultiplier;
		int requiredAdena = esd.getAdenaCost() * costMultiplier;
		int rate = esd.getRate(player);
		
		if (player.getSp() < requiredSp)
		{
			requestFailed(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		// No config option for safe enchant book consume
		L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
		if (Config.ALT_ES_SP_BOOK_NEEDED && spb == null)// Haven't spellbook
		{
			requestFailed(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		// decrease sp
		boolean check = player.getStat().removeExpAndSp(0, requiredSp, false);
		
		// maybe destroy book
		if (Config.ALT_ES_SP_BOOK_NEEDED)
			check &= player.destroyItem("Consume", spb.getObjectId(), 1, trainer, true);
		
		check &= player.destroyItemByItemId("Consume", PcInventory.ADENA_ID, requiredAdena, trainer, true);
		
		if (!check)
		{
			requestFailed(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
			return;
		}
		
		// ok. Destroy ONE copy of the book
		if (Rnd.get(100) <= rate)
		{
			player.addSkill(skill, true);
			
			if (_log.isDebugEnabled())
				_log.info("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requiredAdena + " EXP.");
			
			sendPacket(new ExEnchantSkillResult(true));
			
			SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
			sm.addSkillName(_skillId);
			sendPacket(sm);
			updateSkillShortcuts(player);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_S1_LEVEL_WILL_REMAIN);
			sm.addSkillName(_skillId);
			sendPacket(sm);
			sendPacket(new ExEnchantSkillResult(false));
		}
		
		sendPacket(new UserInfo(player));
		// FIXME ?
		// player.sendSkillList();
		sendPacket(new ExEnchantSkillInfo(_skillId, player.getSkillLevel(_skillId)));
		sendPacket(new ExEnchantSkillInfoDetail(0, _skillId, player.getSkillLevel(_skillId) + 1, player));
		
		((L2NpcInstance) trainer).showEnchantSkillList(player, true); // list of safe enchants
		
		sendPacket(ActionFailed.STATIC_PACKET);
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
		return "[C] D0:32 RequestExEnchantSkillSafe";
	}
}
