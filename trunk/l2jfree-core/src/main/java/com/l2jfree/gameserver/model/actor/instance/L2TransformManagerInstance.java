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

import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.model.L2CertificationSkillsLearn;
import com.l2jfree.gameserver.model.L2ItemInstance;
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
		else if (command.startsWith("CertificationSkills"))
		{
			if (!player.isSubClassActive())
			{
				QuestState qs = player.getQuestState("136_MoreThanMeetsTheEye");
				if (qs == null || !qs.isCompleted())
				{
					player.sendMessage("You must have completed the More than meets the eye quest for receiving these special skills.");
					return;
				}
				
				player.setSkillLearningClassId(player.getClassId());
				showCertificationSkillsList(player);
			}
			else
			{
				showChatWindow(player,"data/html/default/32323-10.htm");
				return;
			}
		}
		else if (command.startsWith("DeleteCertifications"))
		{
			if (!player.isSubClassActive())
			{	
				int subclassItemIds[] = {10280,10281,10282,10283,10284,10285,10286,10287,10288,10289,10290,10291,10292,10293,10294,10612};
				
				if (player.reduceAdena("Subclass Certification Removal", 10000000, player, true))
				{
					L2ItemInstance item;
					int skillId;

					for (L2Skill skill : player.getAllSkills())
					{
						skillId = skill.getId();
						switch (skillId)
						{
						case 631:
						case 632:
						case 633:
						case 634:
						case 637:
						case 638:
						case 639:
						case 640:
						case 641:
						case 642:
						case 643:
						case 644:
						case 645:
						case 646:
						case 647:
						case 648:
						case 650:
						case 651:
						case 652:
						case 653:
						case 654:
						case 655:
						case 656:
						case 657:
						case 658:
						case 659:
						case 660:
						case 661:
						case 662:
						case 799:
						case 800:
						case 801:
						case 802:
						case 803:
						case 804:
						case 1489:
						case 1490:
						case 1491:
							player.removeSkill(skill);
							break;
						}
					}
					for (int itemId : subclassItemIds)
					{
						item = player.getInventory().getItemByItemId(itemId);
						if (item != null)
							player.destroyItemByItemId("Subclass Certification Removal", itemId, item.getCount(), player, true);

						item = player.getWarehouse().getItemByItemId(itemId);
						if (item != null)
							player.getWarehouse().destroyItemByItemId("Subclass Certification Removal", itemId, item.getCount(), player, null);
					}
					player.deleteSubclassCertifications();

					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setHtml("<html><body>Ok, your certifications have been removed!</body></html>");
					player.sendPacket(html);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setHtml("<html><body>Subclass certification removal costs 10'000'000 Adena.</body></html>");
					player.sendPacket(html);
				}
			}
			else
			{
				showChatWindow(player,"data/html/default/32323-13.htm");
				return;
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
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setHtml("<html><body>You've learned all skills.<br></body></html>");
				player.sendPacket(html);
			}
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	public void showCertificationSkillsList(L2PcInstance player)
	{        
		if (player.isTransformed())
			return;

		L2CertificationSkillsLearn[] skills = SkillTreeTable.getInstance().getAvailableCertificationSkills(player);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;

		for (L2CertificationSkillsLearn s: skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null)
				continue;

			counts++;

			asl.addSkill(s.getId(), s.getLevel(), 1, 0, 0);
		}

		if (counts == 0)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setHtml("<html><body>You've learned all skills.<br></body></html>");
			player.sendPacket(html);
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