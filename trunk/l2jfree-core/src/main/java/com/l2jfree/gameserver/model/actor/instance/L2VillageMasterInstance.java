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

import java.util.Iterator;
import java.util.Set;

import javolution.text.TextBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.CharTemplateTable;
import com.l2jfree.gameserver.datatables.ClanTable;
import com.l2jfree.gameserver.datatables.SkillTreeTable;
import com.l2jfree.gameserver.instancemanager.CastleManager;
import com.l2jfree.gameserver.instancemanager.FortManager;
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2ClanMember;
import com.l2jfree.gameserver.model.L2PledgeSkillLearn;
import com.l2jfree.gameserver.model.L2Skill;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.L2Clan.SubPledge;
import com.l2jfree.gameserver.model.base.ClassId;
import com.l2jfree.gameserver.model.base.ClassType;
import com.l2jfree.gameserver.model.base.PlayerClass;
import com.l2jfree.gameserver.model.base.Race;
import com.l2jfree.gameserver.model.base.SubClass;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.AcquireSkillDone;
import com.l2jfree.gameserver.network.serverpackets.AcquireSkillList;
import com.l2jfree.gameserver.network.serverpackets.ActionFailed;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.PledgeReceiveSubPledgeCreated;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.UserInfo;
import com.l2jfree.gameserver.templates.chars.L2NpcTemplate;
import com.l2jfree.gameserver.util.FloodProtector;
import com.l2jfree.gameserver.util.FloodProtector.Protected;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.3.2.8 $ $Date: 2005/03/29 23:15:15 $
 */
public final class L2VillageMasterInstance extends L2NpcInstance
{
	private final static Log	_log	= LogFactory.getLog(L2VillageMasterInstance.class.getName());

	/**
	 * @param template
	 */
	public L2VillageMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		String[] commandStr = command.split(" ");
		String actualCommand = commandStr[0]; // Get actual command

		String cmdParams = "";
		String cmdParams2 = "";

		if (commandStr.length >= 2)
			cmdParams = commandStr[1];
		if (commandStr.length >= 3)
			cmdParams2 = commandStr[2];

		if (actualCommand.equalsIgnoreCase("create_clan"))
		{
			if (cmdParams.isEmpty())
				return;

			ClanTable.getInstance().createClan(player, command.substring(actualCommand.length()).trim());
		}
		else if (actualCommand.equalsIgnoreCase("create_academy"))
		{
			if (cmdParams.isEmpty())
				return;

			createSubPledge(player, cmdParams, null, -1, 5);
		}
		else if (actualCommand.equalsIgnoreCase("create_royal"))
		{
			if (cmdParams.isEmpty())
				return;

			createSubPledge(player, cmdParams, cmdParams2, 100, 6);
		}
		else if (actualCommand.equalsIgnoreCase("assign_subpl_leader"))
		{
			if (cmdParams.isEmpty())
				return;

			assignSubPledgeLeader(player, cmdParams, cmdParams2);
		}
		else if (actualCommand.equalsIgnoreCase("rename_royal1") || actualCommand.equalsIgnoreCase("rename_royal2")
				|| actualCommand.equalsIgnoreCase("rename_knights1") || actualCommand.equalsIgnoreCase("rename_knights2")
				|| actualCommand.equalsIgnoreCase("rename_knights3") || actualCommand.equalsIgnoreCase("rename_knights4"))
		{
			if (cmdParams.isEmpty())
				return;
			renameSubPledge(player, cmdParams, actualCommand);
		}
		else if (actualCommand.equalsIgnoreCase("create_knight"))
		{
			if (cmdParams.isEmpty())
				return;

			createSubPledge(player, cmdParams, cmdParams2, 1001, 7);
		}
		else if (actualCommand.equalsIgnoreCase("create_ally"))
		{
			if (cmdParams.isEmpty())
				return;

			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
				return;
			}
			player.getClan().createAlly(player, command.substring(actualCommand.length()).trim());
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_ally"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
				return;
			}
			player.getClan().dissolveAlly(player);
		}
		else if (actualCommand.equalsIgnoreCase("dissolve_clan"))
		{
			dissolveClan(player, player.getClanId());
		}
		else if (actualCommand.equalsIgnoreCase("change_clan_leader"))
		{
			if (cmdParams.isEmpty())
				return;

			changeClanLeader(player, cmdParams);
		}
		else if (actualCommand.equalsIgnoreCase("recover_clan"))
		{
			recoverClan(player, player.getClanId());
		}
		else if (actualCommand.equalsIgnoreCase("increase_clan_level"))
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			player.getClan().levelUpClan(player);
		}
		else if (actualCommand.equalsIgnoreCase("learn_clan_skills"))
		{
			showPledgeSkillList(player);
		}
		else if (command.startsWith("Subclass"))
		{
			int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());

			// Subclasses may not be changed while a skill is in use.
			if (player.isCastingNow() || player.isAllSkillsDisabled() || player.isTransformed())
			{
				player.sendPacket(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE);
				return;
			}

			TextBuilder content = new TextBuilder("<html><body>");
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			Set<PlayerClass> subsAvailable;

			int paramOne = 0;
			int paramTwo = 0;

			try
			{
				int endIndex = command.indexOf(' ', 11);
				if (endIndex == -1)
					endIndex = command.length();

				paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
				if (command.length() > endIndex)
					paramTwo = Integer.parseInt(command.substring(endIndex).trim());
			}
			catch (Exception NumberFormatException)
			{
			}

			switch (cmdChoice)
			{
			case 1: // Add Subclass - Initial
				// Avoid giving player an option to add a new sub class, if they have three already.
				if (player.getTotalSubClasses() == Config.ALT_MAX_SUBCLASS)
				{
					player.sendMessage("You can now only change one of your current sub classes.");
					return;
				}

				subsAvailable = getAvailableSubClasses(player);

				if (subsAvailable != null && !subsAvailable.isEmpty())
				{
					content.append("Add Subclass:<br>Which sub class do you wish to add?<br>");

					for (PlayerClass subClass : subsAvailable)
						content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 4 " + subClass.ordinal() + "\" msg=\"1268;"
								+ CharTemplateTable.getClassNameById(subClass.ordinal()) + "\">" + CharTemplateTable.getClassNameById(subClass.ordinal())
								+ "</a><br>");
				}
				else
				{
					player.sendMessage("There are no sub classes available at this time.");
					return;
				}
				break;
			case 2: // Change Class - Initial
				content.append("Change Subclass:<br>");

				final int baseClassId = player.getBaseClass();

				if (player.getSubClasses().isEmpty())
				{
					content.append("You can't change sub classes when you don't have a sub class to begin with.<br>" + "<a action=\"bypass -h npc_"
							+ getObjectId() + "_Subclass 1\">Add subclass.</a>");
				}
				else
				{
					content.append("Which class would you like to switch to?<br>");

					if (baseClassId == player.getActiveClass())
						content.append(CharTemplateTable.getClassNameById(baseClassId) + "&nbsp;<font color=\"LEVEL\">(Base Class)</font><br>");
					else
						content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 0\">" + CharTemplateTable.getClassNameById(baseClassId)
								+ "</a>&nbsp;" + "<font color=\"LEVEL\">(Base Class)</font><br>");

					for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
					{
						SubClass subClass = subList.next();
						int subClassId = subClass.getClassId();

						if (subClassId == player.getActiveClass())
							content.append(CharTemplateTable.getClassNameById(subClassId) + "<br>");
						else
							content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 5 " + subClass.getClassIndex() + "\">"
									+ CharTemplateTable.getClassNameById(subClassId) + "</a><br>");
					}
				}
				break;
			case 3: // Change/Cancel Subclass - Initial
				content.append("Change Subclass:<br>Which of the following sub classes would you like to change?<br>");
				int classIndex = 1;
				for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
				{
					SubClass subClass = subList.next();

					content.append("Sub-class " + classIndex++ + "<br1>");
					content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 6 " + subClass.getClassIndex() + "\">"
							+ CharTemplateTable.getClassNameById(subClass.getClassId()) + "</a><br>");
				}
				content.append("<br>If you change a sub class, you'll start at level 40 after the 2nd class transfer.");
				break;
			case 4: // Add Subclass - Action (Subclass 4 x[x])
				boolean allowAddition = true;
				/*
				 * If the character is less than level 75 on any of their previously chosen
				 * classes then disallow them to change to their most recently added sub-class choice.
				 */

				if (!FloodProtector.tryPerformAction(player, Protected.SUBCLASS))
				{
					_log.warn("Player "+player.getName()+" has performed a subclass change too fast");
					return;
				}

				if (player.getLevel() < 75)
				{
					player.sendMessage("You may not add a new sub class before you are level 75 on your previous class.");
					allowAddition = false;
				}

				if (player._inEventCTF || player._inEventDM || player._inEventTvT || player._inEventVIP)
				{
					player.sendMessage("You may not add a new sub class while being registered on event.");
					return;
				}

				if (allowAddition)
				{
					if (!player.getSubClasses().isEmpty())
					{
						for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
						{
							SubClass subClass = subList.next();

							if (subClass.getLevel() < 75)
							{
								player.sendMessage("You may not add a new sub class before you are level 75 on your previous sub class.");
								allowAddition = false;
								break;
							}
						}
					}
				}

				/*
				 * If quest checking is enabled, verify if the character has completed the Mimir's Elixir (Path to Subclass)
				 * and Fate's Whisper (A Grade Weapon) quests by checking for instances of their unique reward items.
				 * 
				 * If they both exist, remove both unique items and continue with adding the sub-class.
				 */
				if (!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
				{

					QuestState qs = player.getQuestState("234_FatesWhisper");
					if (qs == null || !qs.isCompleted())
					{
						player.sendMessage("You must have completed the Fate's Whisper quest to continue adding your sub class.");
						return;
					}

					if (!player.isKamaelic())
					{
						qs = player.getQuestState("235_MimirsElixir");
						if (qs == null || !qs.isCompleted())
						{
							player.sendMessage("You must have completed the Mimir's Elixir quest to continue adding your sub class.");
							return;
						}
					}
					//Kamael have different quest than 235
					else
					{
						qs = player.getQuestState("236_SeedsOfChaos");
						if (qs == null || !qs.isCompleted())
						{
							player.sendMessage("You must have completed the Seeds of Chaos quest to continue adding your sub class.");
							return;
						}
					}
				}

				////////////////// \\\\\\\\\\\\\\\\\\
				if (allowAddition)
				{
					String className = CharTemplateTable.getClassNameById(paramOne);

					if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1))
					{
						player.sendMessage("The sub class could not be added.");
						return;
					}

					player.setActiveClass(player.getTotalSubClasses());

					content.append("Add Subclass:<br>The sub class of <font color=\"LEVEL\">" + className + "</font> has been added.");
					player.sendPacket(SystemMessageId.CLASS_TRANSFER); // Transfer to new class.
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					html.setFile("data/html/villagemaster/SubClass_Fail.htm");
				}
				break;
			case 5: // Change Class - Action
				/*
				 * If the character is less than level 75 on any of their previously chosen
				 * classes then disallow them to change to their most recently added sub-class choice.
				 *
				 * Note: paramOne = classIndex
				 */
				if (player._inEventCTF || player._inEventDM || player._inEventTvT || player._inEventVIP)
				{
					player.sendMessage("You are registered at event right now.");
					return;
				}

				/*
				 * DrHouse: Despite this is not 100% retail like, it is here to avoid some exploits during subclass changes, specially
				 * on small servers. TODO: On retail, each village master doesn't offer any subclass that is not given by itself so player
				 * always has to move to other location to change subclass after changing previously. Thanks Aikimaniac for this info.
				 */
				if (!FloodProtector.tryPerformAction(player, Protected.SUBCLASS))
				{
					_log.warn("Player "+player.getName()+" has performed a subclass change too fast");
					return;
				}

				player.setActiveClass(paramOne);

				content.append("Change Subclass:<br>Your active sub class is now a <font color=\"LEVEL\">"
						+ CharTemplateTable.getClassNameById(player.getActiveClass()) + "</font>.");

				player.sendPacket(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED); // Transfer completed.
				break;
			case 6: // Change/Cancel Subclass - Choice
				content.append("Please choose a sub class to change to. If the one you are looking for is not here, "
						+ "please seek out the appropriate master for that class.<br>"
						+ "<font color=\"LEVEL\">Warning!</font> All classes and skills for this class will be removed.<br><br>");

				subsAvailable = getAvailableSubClasses(player);

				if (subsAvailable != null && !subsAvailable.isEmpty())
				{
					for (PlayerClass subClass : subsAvailable)
						content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 7 " + paramOne + " " + subClass.ordinal() + "\">"
								+ CharTemplateTable.getClassNameById(subClass.ordinal()) + "</a><br>");
				}
				else
				{
					player.sendMessage("There are no sub classes available at this time.");
					return;
				}
				break;
			case 7: // Change Subclass - Action
				/*
				 * Warning: the information about this subclass will be removed from the
				 * subclass list even if false!
				 */

				if (!FloodProtector.tryPerformAction(player, Protected.SUBCLASS))
				{
					_log.warn("Player "+player.getName()+" has performed a subclass change too fast");
					return;
				}
				else if (player.modifySubClass(paramOne, paramTwo))
				{
					player.stopAllEffects(); // all effects from old subclass stopped!
					player.setActiveClass(paramOne);

					content.append("Change Subclass:<br>Your sub class has been changed to <font color=\"LEVEL\">"
							+ CharTemplateTable.getClassNameById(paramTwo) + "</font>.");

					player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS); // Subclass added.
					player.sendPacket(ActionFailed.STATIC_PACKET);

					// check player skills
					if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
						player.checkAllowedSkills();
				}
				else
				{
					/*
					 * This isn't good! modifySubClass() removed subclass from memory
					 * we must update _classIndex! Else IndexOutOfBoundsException can turn
					 * up some place down the line along with other seemingly unrelated
					 * problems.
					 */
					player.setActiveClass(0); // Also updates _classIndex plus switching _classid to baseclass.

					player.sendMessage("The sub class could not be added, you have been reverted to your base class.");
					return;
				}
				break;
			}

			content.append("</body></html>");

			// If the content is greater than for a basic blank page,
			// then assume no external HTML file was assigned.
			if (content.length() > 26)
				html.setHtml(content.toString());

			player.sendPacket(html);
		}
		else
		{
			// This class dont know any other commands, let forward
			// the command to the parent class
			super.onBypassFeedback(player, command);
		}
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/villagemaster/" + pom + ".htm";
	}

	//Private stuff
	/**
	 * @param player
	 * @param clanId
	 */
	public void dissolveClan(L2PcInstance player, int clanId)
	{
		if (_log.isDebugEnabled())
			_log.info(player.getObjectId() + "(" + player.getName() + ") requested dissolve a clan from " + getObjectId() + "(" + getName() + ")");

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		/*
		 * Until proper clan leader change support is done, this is a little
		 * exploit fix (leader, while fliying wyvern changes clan leader and the new leader
		 * can ride the wyvern too)
		 * DrHouse
		 */
		if (player.isFlying())
		{
			player.sendMessage("Please, stop flying");
			return;
		}

		L2Clan clan = player.getClan();
		if (clan.getAllyId() != 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_DISPERSE_THE_CLANS_IN_ALLY);
			return;
		}
		if (clan.isAtWar())
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_WAR);
			return;
		}
		if (clan.getHasCastle() != 0 || clan.getHasHideout() != 0 || clan.getHasFort() != 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE);
			return;
		}
		for (Castle castle : CastleManager.getInstance().getCastles().values())
		{
			if (SiegeManager.getInstance().checkIsRegistered(clan, castle.getCastleId()))
			{
				player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
				return;
			}
		}
		for (Fort fort : FortManager.getInstance().getForts())
		{
			if (FortSiegeManager.getInstance().checkIsRegistered(clan, fort.getFortId()))
			{
				player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
				return;
			}
		}
		if (SiegeManager.getInstance().checkIfInZone(player))
		{
			player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE);
			return;
		}
		if (clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.DISSOLUTION_IN_PROGRESS);
			return;
		}

		clan.setDissolvingExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_DISSOLVE_DAYS * 86400000L); //24*60*60*1000 = 86400000
		clan.updateClanInDB();

		ClanTable.getInstance().scheduleRemoveClan(clan.getClanId());

		// The clan leader should take the XP penalty of a full death.
		player.deathPenalty(false, false);
	}

	/**
	 * @param player
	 * @param clanId
	 */
	public void recoverClan(L2PcInstance player, int clanId)
	{
		if (_log.isDebugEnabled())
			_log.info(player.getObjectId() + "(" + player.getName() + ") requested recover a clan from " + getObjectId() + "(" + getName() + ")");

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		L2Clan clan = player.getClan();

		clan.setDissolvingExpiryTime(0);
		clan.updateClanInDB();
	}

	public void changeClanLeader(L2PcInstance player, String target)
	{
		if (_log.isDebugEnabled())
			_log.info(player.getObjectId() + "(" + player.getName() + ") requested change a clan leader from " + getObjectId() + "(" + getName() + ")");

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		if (player.getName().equalsIgnoreCase(target))
		{
			return;
		}

		/*
		 * Until proper clan leader change support is done, this is a little
		 * exploit fix (leader, while fliying wyvern changes clan leader and the new leader
		 * can ride the wyvern too)
		 * DrHouse
		 */
		if (player.isFlying())
		{
			player.sendMessage("Please, stop flying");
			return;
		}

		L2Clan clan = player.getClan();

		L2ClanMember member = clan.getClanMember(target);
		if (member == null)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_DOES_NOT_EXIST);
			sm.addString(target);
			player.sendPacket(sm);
			sm = null;
			return;
		}
		if (!member.isOnline())
		{
			player.sendPacket(SystemMessageId.INVITED_USER_NOT_ONLINE);
			return;
		}
		clan.setNewLeader(member);
	}

	public void createSubPledge(L2PcInstance player, String clanName, String leaderName, int pledgeType, int minClanLvl)
	{
		if (_log.isDebugEnabled())
			_log.info(player.getObjectId() + "(" + player.getName() + ") requested sub clan creation from " + getObjectId() + "(" + getName() + ")");

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		L2Clan clan = player.getClan();
		if (clan.getLevel() < minClanLvl)
		{
			if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY);
			}
			else
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT);
			}
			return;
		}

		if (!Config.CLAN_ALLY_NAME_PATTERN.matcher(clanName).matches())
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return;
		}

		int leaderId = pledgeType != L2Clan.SUBUNIT_ACADEMY ? clan.getClanMember(leaderName).getObjectId() : 0;
		if (leaderId != 0 && clan.getLeaderSubPledge(leaderId) != 0)
		{
			player.sendMessage(leaderName + " is already a sub unit leader.");
			return;
		}

		for (L2Clan tempClan : ClanTable.getInstance().getClans())
		{
			if (tempClan.getSubPledge(clanName) != null)
			{
				if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_ALREADY_EXISTS);
					sm.addString(clanName);
					player.sendPacket(sm);
				}
				else
				{
					player.sendPacket(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME);
				}
				return;
			}
		}

		if (pledgeType != L2Clan.SUBUNIT_ACADEMY)
		{
			if (clan.getClanMember(leaderName) == null || clan.getClanMember(leaderName).getSubPledgeType() != 0)
			{
				if (pledgeType >= L2Clan.SUBUNIT_KNIGHT1)
				{
					player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
				}
				else if (pledgeType >= L2Clan.SUBUNIT_ROYAL1)
				{
					player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
				}
				return;
			}
		}

		if (clan.createSubPledge(player, pledgeType, leaderId, clanName) == null)
			return;

		SystemMessage sm;
		if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			sm = new SystemMessage(SystemMessageId.THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else if (pledgeType >= L2Clan.SUBUNIT_KNIGHT1)
		{
			sm = new SystemMessage(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else if (pledgeType >= L2Clan.SUBUNIT_ROYAL1)
		{
			sm = new SystemMessage(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED);
			sm.addString(player.getClan().getName());
		}
		else
			sm = new SystemMessage(SystemMessageId.CLAN_CREATED);

		player.sendPacket(sm);
		if (pledgeType != L2Clan.SUBUNIT_ACADEMY)
		{
			L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
			L2PcInstance subLeader = leaderSubPledge.getPlayerInstance();
			if (subLeader == null)
				return;
			subLeader.setPledgeClass(L2ClanMember.getCurrentPledgeClass(subLeader));
			subLeader.sendPacket(new UserInfo(subLeader));
			try
			{
				clan.getClanMember(leaderName).updateSubPledgeType();
				for (L2Skill skill : leaderSubPledge.getPlayerInstance().getAllSkills())
					leaderSubPledge.getPlayerInstance().removeSkill(skill, false);
				clan.getClanMember(leaderName).getPlayerInstance().setActiveClass(0);
			}
			catch (Throwable t)
			{
			}

			for (L2ClanMember member : clan.getMembers())
			{
				if (member == null || member.getPlayerInstance() == null || member.getPlayerInstance().isOnline() == 0)
					continue;
				SubPledge[] subPledge = clan.getAllSubPledges();
				for (SubPledge element : subPledge)
				{
					member.getPlayerInstance().sendPacket(new PledgeReceiveSubPledgeCreated(element, clan));
				}
			}
		}
	}

	public void renameSubPledge(L2PcInstance player, String newName, String command)
	{
		if (player == null || player.getClan() == null || !player.isClanLeader())
		{
			if (player != null)
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT));
			return;
		}
		L2Clan clan = player.getClan();
		SubPledge[] subPledge = clan.getAllSubPledges();
		for (SubPledge element : subPledge)
		{
			switch (element.getId())
			{
			case 100: // 1st Royal Guard
				if (command.equalsIgnoreCase("rename_royal1"))
				{
					changeSubPledge(clan, element, newName);
					return;
				}
				break;
			case 200: // 2nd Royal Guard
				if (command.equalsIgnoreCase("rename_royal2"))
				{
					changeSubPledge(clan, element, newName);
					return;
				}
				break;
			case 1001: // 1st Order of Knights
				if (command.equalsIgnoreCase("rename_knights1"))
				{
					changeSubPledge(clan, element, newName);
					return;
				}
				break;
			case 1002: // 2nd Order of Knights
				if (command.equalsIgnoreCase("rename_knights2"))
				{
					changeSubPledge(clan, element, newName);
					return;
				}
				break;
			case 2001: // 3rd Order of Knights
				if (command.equalsIgnoreCase("rename_knights3"))
				{
					changeSubPledge(clan, element, newName);
					return;
				}
				break;
			case 2002: // 4th Order of Knights
				if (command.equalsIgnoreCase("rename_knights4"))
				{
					changeSubPledge(clan, element, newName);
					return;
				}
				break;
			}
		}
		player.sendMessage("Sub unit not found.");
	}

	public void changeSubPledge(L2Clan clan, SubPledge element, String newName)
	{
		if (newName.length() > 16 || newName.length() < 3)
		{
			clan.getLeader().getPlayerInstance().sendPacket(new SystemMessage(SystemMessageId.CLAN_NAME_TOO_LONG));
			return;
		}
		String oldName = element.getName();
		element.setName(newName);
		clan.updateSubPledgeInDB(element.getId());
		for (L2ClanMember member : clan.getMembers())
		{
			if (member == null || member.getPlayerInstance() == null || member.getPlayerInstance().isOnline() == 0)
				continue;
			SubPledge[] subPledge = clan.getAllSubPledges();
			for (SubPledge sp : subPledge)
			{
				member.getPlayerInstance().sendPacket(new PledgeReceiveSubPledgeCreated(sp, clan));
			}
			if (member.getPlayerInstance() != null)
				member.getPlayerInstance().sendMessage("Clan sub unit " + oldName + "'s name has been changed into " + newName + ".");
		}
	}

	public void assignSubPledgeLeader(L2PcInstance player, String clanName, String leaderName)
	{
		L2Clan clan = player.getClan();
		if (clan == null)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		if (leaderName.length() > 16)
		{
			player.sendPacket(SystemMessageId.NAMING_CHARNAME_UP_TO_16CHARS);
			return;
		}

		if (player.getName().equals(leaderName))
		{
			player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
			return;
		}

		SubPledge subPledge = player.getClan().getSubPledge(clanName);

		if (null == subPledge)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return;
		}
		if (subPledge.getId() == L2Clan.SUBUNIT_ACADEMY)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return;
		}

		L2PcInstance newLeader = L2World.getInstance().getPlayer(leaderName);
		if (newLeader == null || newLeader.getClan() == null || newLeader.getClan() != clan)
		{
			player.sendMessage(leaderName + " is not in your clan!");
			return;
		}

		if (clan.getClanMember(leaderName) == null || (clan.getClanMember(leaderName).getSubPledgeType() != 0))
		{
			if (subPledge.getId() >= L2Clan.SUBUNIT_KNIGHT1)
			{
				player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
			}
			else if (subPledge.getId() >= L2Clan.SUBUNIT_ROYAL1)
			{
				player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
			}
			return;
		}

		try
		{
			L2ClanMember oldLeader = clan.getClanMember(subPledge.getLeaderId());
			String oldLeaderName = oldLeader == null ? "" : oldLeader.getName();
			clan.getClanMember(oldLeaderName).setSubPledgeType(0);
			clan.getClanMember(oldLeaderName).updateSubPledgeType();
			clan.getClanMember(oldLeaderName).getPlayerInstance().setPledgeClass(
					L2ClanMember.getCurrentPledgeClass(clan.getClanMember(oldLeaderName).getPlayerInstance()));
			clan.getClanMember(oldLeaderName).getPlayerInstance().setActiveClass(0);
		}
		catch (Throwable t)
		{
		}

		int leaderId = clan.getClanMember(leaderName).getObjectId();

		subPledge.setLeaderId(leaderId);
		clan.updateSubPledgeInDB(subPledge.getId());
		L2ClanMember leaderSubPledge = clan.getClanMember(leaderName);
		L2PcInstance subLeader = leaderSubPledge.getPlayerInstance();
		if (subLeader != null)
		{
			subLeader.setPledgeClass(L2ClanMember.getCurrentPledgeClass(subLeader));
			subLeader.sendPacket(new UserInfo(subLeader));
		}
		clan.broadcastClanStatus();
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2);
		sm.addString(leaderName);
		sm.addString(clanName);
		clan.broadcastToOnlineMembers(sm);
		sm = null;
	}

	private final Set<PlayerClass> getAvailableSubClasses(L2PcInstance player)
	{
		int baseClassId = player.getBaseClass();

		// For calculation of available subclasses, we must treat 3rd-stage classes like their 2nd-stage parent classes.
		// So use the parent class
		if ((baseClassId >= 88 && baseClassId <= 118) || (baseClassId >= 131 && baseClassId <= 134) || baseClassId == 136)
			baseClassId = ClassId.values()[baseClassId].getParent().getId();

		PlayerClass baseClass = PlayerClass.values()[baseClassId];

		/**
		 * If the race of your main class is Elf or Dark Elf,
		 * you may not select each class as a subclass to the other class.
		 *
		 * If the race of your main class is Kamael, you may not subclass any other race
		 * If the race of your main class is NOT Kamael, you may not subclass any Kamael class
		 *
		 * You may not select Overlord and Warsmith class as a subclass.
		 *
		 * 
		 * You may not select a similar class as the subclass.
		 * The occupations classified as similar classes are as follows:
		 * 
		 * Treasure Hunter, Plainswalker and Abyss Walker
		 * Hawkeye, Silver Ranger and Phantom Ranger
		 * Paladin, Dark Avenger, Temple Knight and Shillien Knight
		 * Warlocks, Elemental Summoner and Phantom Summoner
		 * Elder and Shillien Elder
		 * Swordsinger and Bladedancer
		 * Sorcerer, Spellsinger and Spellhowler
		 * 
		 * Also, Kamael have a special hidden subclass, the inspector, which can
		 * only be taken if you have already completed the other two Kamael subclasses
		 *
		 */

		final Race npcRace = getVillageMasterRace();
		final ClassType npcTeachType = getVillageMasterTeachType();

		Set<PlayerClass> availSubs = baseClass.getAvailableSubclasses(player);

		// Can't take subclass already taken
		// Can't take subclass you already have as base class
		if (availSubs != null && !availSubs.isEmpty())
		{
			for (PlayerClass availSub : availSubs)
			{
				for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
				{
					SubClass prevSubClass = subList.next();

					int subClassId = prevSubClass.getClassId();
					if ((subClassId >= 88 && subClassId <= 118) || (subClassId >= 131 && subClassId <= 134) || subClassId == 136)
						subClassId = ClassId.values()[subClassId].getParent().getId();

					if (availSub.ordinal() == subClassId || availSub.ordinal() == baseClassId)
						availSubs.remove(availSub);
				}

				if (npcRace == Race.Human || npcRace == Race.Elf)
				{
					// If the master is human or light elf, ensure that fighter-type
					// masters only teach fighter classes, and priest-type masters
					// only teach priest classes etc.
					if (!availSub.isOfType(npcTeachType))
						availSubs.remove(availSub);

					// Remove any non-human or light elf classes.
					else if (!availSub.isOfRace(Race.Human) && !availSub.isOfRace(Race.Elf))
						availSubs.remove(availSub);
				}
				else
				{
					// If the master is not human and not light elf,
					// then remove any classes not of the same race as the master.
					if (!availSub.isOfRace(npcRace))
						availSubs.remove(availSub);
				}
			}
		}
		return availSubs;
	}

	/**
	 * this displays PledgeSkillList to the player.
	 * @param player
	 */
	public void showPledgeSkillList(L2PcInstance player)
	{
		if (_log.isDebugEnabled())
			_log.info("PledgeSkillList activated on: " + getObjectId());
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		if (player.getClan() == null || !player.isClanLeader())
		{
			TextBuilder sb = new TextBuilder();
			sb.append("<html><body>");
			sb.append("<br><br>You're not qualified to learn Clan skills.");
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			player.sendPacket(html);
			player.sendPacket(ActionFailed.STATIC_PACKET);

			return;
		}

		L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Clan);
		int counts = 0;

		for (L2PledgeSkillLearn s : skills)
		{
			int cost = s.getRepCost();
			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if (counts == 0)
		{
			if (player.getClan().getLevel() < 8)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_COME_BACK_WHEN_REACHED_S1);
				if (player.getClan().getLevel() < 5)
					sm.addNumber(5);
				else
					sm.addNumber(player.getClan().getLevel() + 1);

				player.sendPacket(sm);
				player.sendPacket(new AcquireSkillDone());
			}
			else
			{
				TextBuilder sb = new TextBuilder();
				sb.append("<html><body>");
				sb.append("You've learned all skills available for your Clan.<br>");
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

	private final Race getVillageMasterRace()
	{
		String npcClass = getTemplate().getJClass().toLowerCase();

		if (npcClass.indexOf("human") > -1)
			return Race.Human;

		if (npcClass.indexOf("darkelf") > -1)
			return Race.Darkelf;

		if (npcClass.indexOf("elf") > -1)
			return Race.Elf;

		if (npcClass.indexOf("orc") > -1)
			return Race.Orc;

		if (npcClass.indexOf("dwarf") > -1)
			return Race.Dwarf;

		return Race.Kamael;
	}

	private final ClassType getVillageMasterTeachType()
	{
		String npcClass = getTemplate().getJClass().toLowerCase();

		if (npcClass.indexOf("sanctuary") > -1 || npcClass.indexOf("clergyman") > -1 || npcClass.indexOf("temple") > -1)
			return ClassType.Priest;

		if (npcClass.indexOf("mageguild") > -1 || npcClass.indexOf("patriarch") > -1)
			return ClassType.Mystic;

		return ClassType.Fighter;
	}

	private Iterator<SubClass> iterSubClasses(L2PcInstance player)
	{
		return player.getSubClasses().values().iterator();
	}
}