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

import java.util.List;

import javolution.text.TextBuilder;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.model.L2EnchantSkillLearn;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2SkillLearn;
import com.l2jfree.gameserver.model.actor.L2Npc;
import com.l2jfree.gameserver.model.actor.status.FolkStatus;
import com.l2jfree.gameserver.model.base.ClassId;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.AcquireSkillList;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.ExEnchantSkillList;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.ExEnchantSkillList.EnchantSkillType;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;

public class L2NpcInstance extends L2Npc
{
	private List<ClassId>	_classesToTeach;

	public L2NpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		_classesToTeach = template.getTeachInfo();
	}

	@Override
	public void onAction(L2PcInstance player)
	{
		player.setLastFolkNPC(this);
		super.onAction(player);
	}

	@Override
	public FolkStatus getStatus()
	{
		if (_status == null)
			_status = new FolkStatus(this);
		return (FolkStatus) _status;
	}

	/**
	 * this displays SkillList to the player.
	 * @param player
	 */
	public void showSkillList(L2PcInstance player, ClassId classId)
	{
		if (_log.isDebugEnabled())
			_log.debug("SkillList activated on: " + getObjectId());

		int npcId = getTemplate().getNpcId();

		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:"
					+ npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if (!getTemplate().canTeach(classId))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;

		for (L2SkillLearn s : skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

			if (sk == null || !sk.getCanLearn(player.getClassId()) || !sk.canTeachBy(npcId))
				continue;

			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if (counts == 0)
		{
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);

			if (minlevel > 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_REACHED_S1);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
				player.sendPacket(sm);
			}
		}
		else
		{
			player.sendPacket(asl);
		}

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * this displays EnchantSkillList to the player.
	 * @param player
	 */
	public void showEnchantSkillList(L2PcInstance player, boolean isSafeEnchant)
	{
		if (_log.isDebugEnabled())
			_log.debug("EnchantSkillList activated on: " + getObjectId());
		int npcId = getTemplate().getNpcId();

		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:"
					+ npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if (!getTemplate().canTeach(player.getClassId()))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if (player.getClassId().level() < 3) // Requires to have 3rd class quest completed
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("You must have 3rd class change quest completed.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			return;
		}

		int playerLevel = player.getLevel();

		if (playerLevel >= 76)
		{
			ExEnchantSkillList esl = new ExEnchantSkillList(isSafeEnchant ? EnchantSkillType.SAFE : EnchantSkillType.NORMAL);
			L2Skill[] charSkills = player.getAllSkills();
			int counts = 0;
			for (L2Skill skill : charSkills)
			{
				L2EnchantSkillLearn enchantLearn = SkillTreeTable.getInstance().getSkillEnchantmentForSkill(skill);
				if (enchantLearn != null)
				{
					esl.addSkill(skill.getId(), skill.getLevel());
					counts++;
				}
			}

			if (counts == 0)
			{
				player.sendPacket(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
			}
			else
			{

				player.sendPacket(esl);
			}
		}
		else
		{
			player.sendPacket(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * Show the list of enchanted skills for changing enchantment route
	 * 
	 * @param player
	 */
	public void showEnchantChangeSkillList(L2PcInstance player)
	{
		if (_log.isDebugEnabled())
		{
			_log.info("Enchanted Skill List activated on: " + getObjectId());
		}

		int npcId = getTemplate().getNpcId();

		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:"
					+ npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if (!getTemplate().canTeach(player.getClassId()))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if (player.getClassId().level() < 3) // Requires to have 3rd class quest completed
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("You must have 3rd class change quest completed.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}
		int playerLevel = player.getLevel();

		if (playerLevel >= 76)
		{
			ExEnchantSkillList esl = new ExEnchantSkillList(EnchantSkillType.CHANGE_ROUTE);
			L2Skill[] charSkills = player.getAllSkills();

			for (L2Skill skill : charSkills)
			{
				// is enchanted?
				if (skill.getLevel() > 100)
				{
					esl.addSkill(skill.getId(), skill.getLevel());
				}
			}

			player.sendPacket(esl);
		}

		else
		{
			player.sendPacket(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * Show the list of enchanted skills for untraining
	 * 
	 * @param player
	 * @param classId
	 */
	public void showEnchantUntrainSkillList(L2PcInstance player, ClassId classId)
	{
		if (_log.isDebugEnabled())
		{
			_log.info("Enchanted Skill List activated on: " + getObjectId());
		}

		int npcId = getTemplate().getNpcId();

		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:"
					+ npcId + ", Your classId:" + player.getClassId().getId() + "<br>");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if (!getTemplate().canTeach(classId))
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("I cannot teach you any skills.<br> You must find your current class teachers.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}

		if (player.getClassId().level() < 3) // Requires to have 3rd class quest completed
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("You must have 3rd class change quest completed.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);

			return;
		}
		int playerLevel = player.getLevel();

		if (playerLevel >= 76)
		{
			ExEnchantSkillList esl = new ExEnchantSkillList(EnchantSkillType.UNTRAIN);
			L2Skill[] charSkills = player.getAllSkills();

			for (L2Skill skill : charSkills)
			{
				// is enchanted?
				if (skill.getLevel() > 100)
				{
					esl.addSkill(skill.getId(), skill.getLevel());
				}
			}

			player.sendPacket(esl);
		}
		else
		{
			player.sendPacket(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.inObserverMode())
			return;

		if (command.startsWith("SkillList"))
		{
			if (Config.ALT_GAME_SKILL_LEARN)
			{
				String id = command.substring(9).trim();

				if (id.length() != 0)
				{
					player.setSkillLearningClassId(ClassId.values()[Integer.parseInt(id)]);
					showSkillList(player, ClassId.values()[Integer.parseInt(id)]);
				}
				else
				{
					boolean own_class = false;

					if (_classesToTeach != null)
					{
						for (ClassId cid : _classesToTeach)
						{
							if (cid.equalsOrChildOf(player.getClassId()))
							{
								own_class = true;
								break;
							}
						}
					}

					String text = "<html><body><center>Skill learning:</center><br>";

					if (!own_class)
					{
						String charType = player.getClassId().isMage() ? "fighter" : "mage";
						text += "Skills of your class are the easiest to learn.<br>\n" + "Skills of another class of your race are a little harder.<br>"
								+ "Skills for classes of another race are extremely difficult.<br>" + "But the hardest of all to learn are the " + charType
								+ " skills!<br>";
					}

					// Make a list of classes
					if (_classesToTeach != null)
					{
						int count = 0;
						ClassId classCheck = player.getClassId();

						while ((count == 0) && (classCheck != null))
						{
							for (ClassId cid : _classesToTeach)
							{
								if (cid.level() != classCheck.level())
									continue;

								if (SkillTreeTable.getInstance().getAvailableSkills(player, cid).length == 0)
									continue;

								text += "<a action=\"bypass -h npc_%objectId%_SkillList " + cid.getId() + "\">Learn " + cid + "'s class Skills</a><br>\n";
								count++;
							}
							classCheck = classCheck.getParent();
						}
						classCheck = null;
					}
					else
					{
						text += "No Skills.<br>\n";
					}

					text += "</body></html>";

					insertObjectIdAndShowChatWindow(player, text);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
			}
			else
			{
				player.setSkillLearningClassId(player.getClassId());
				showSkillList(player, player.getClassId());
			}
		}
		else if (command.startsWith("EnchantSkillList"))
		{
			showEnchantSkillList(player, false);
		}
		else if (command.startsWith("SafeEnchantSkillList"))
		{
			showEnchantSkillList(player, true);
		}
		else if (command.startsWith("ChangeEnchantSkillList"))
		{
			showEnchantChangeSkillList(player);
		}
		else if (command.startsWith("UntrainEnchantSkillList"))
		{
			showEnchantUntrainSkillList(player, player.getClassId());
		}
		else
		{
			// This class dont know any other commands, let forward the command to the parent class
			super.onBypassFeedback(player, command);
		}
	}
}
