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
package com.l2jfree.gameserver.model.actor.instance;

import javolution.text.TextBuilder;

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.model.L2Multisell;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2TransformSkillLearn;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.model.quest.State;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.AcquireSkillList;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

public class L2TransformManagerInstance extends L2MerchantInstance
{
    /**
	 * @param objectId
	 * @param template
	 */
	public L2TransformManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/default/" + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (command.startsWith("TransformSkillList"))
		{
			player.setSkillLearningClassId(player.getClassId());
			showTransformSkillList(player);
		}
		else if (command.startsWith("BuyTransform"))
		{
			if (testQuestTransformation(player))
				L2Multisell.getInstance().separateAndSend(getTemplate().getNpcId(), player, false, getCastle().getTaxRate());
			else
			{
				showHtmlFile(player, getTemplate().getNpcId()+"-cantbuy.htm");
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	/**
	 * this displays TransformationSkillList to the player.
	 * @param player
	 */
	public void showTransformSkillList(L2PcInstance player)
	{
		L2TransformSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableTransformSkills(player);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;

		for (L2TransformSkillLearn s: skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null)
				continue;

			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 0);
		}

		if (counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewTransformSkill(player);

			if (minlevel > 0)
			{
				// No more skills to learn, come back when you level.
				SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_REACHED_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				TextBuilder sb = new TextBuilder();
				sb.append("<html><head><body>");
				sb.append("You've learned all skills.<br>");
				sb.append("</body></html>");
				html.setHtml(sb.toString());
				player.sendPacket(html);

			}
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void showHtmlFile(L2PcInstance player, String file)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile("data/html/default/" + file);
		player.sendPacket(html);
	}

	public boolean testQuestTransformation(L2PcInstance player)
	{
		if (player == null)
			return false;

		String _questName = "136_MoreThanMeetsTheEye";
		QuestState qs = player.getQuestState(_questName);
		if (qs == null)
			return false;
		return State.getStateName(qs.getState()) == "Completed";
	}
}