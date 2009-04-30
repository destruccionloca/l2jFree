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

import java.io.File;

import com.l2jfree.Config;
import com.l2jfree.gameserver.Announcements;
import com.l2jfree.gameserver.L2JfreeInfo;
import com.l2jfree.gameserver.SevenSigns;
import com.l2jfree.gameserver.communitybbs.Manager.RegionBBSManager;
import com.l2jfree.gameserver.datatables.GmListTable;
import com.l2jfree.gameserver.datatables.SkillTable;
import com.l2jfree.gameserver.handler.AdminCommandHandler;
import com.l2jfree.gameserver.instancemanager.ClanHallManager;
import com.l2jfree.gameserver.instancemanager.CoupleManager;
import com.l2jfree.gameserver.instancemanager.CrownManager;
import com.l2jfree.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jfree.gameserver.instancemanager.FortManager;
import com.l2jfree.gameserver.instancemanager.FortSiegeManager;
import com.l2jfree.gameserver.instancemanager.InstanceManager;
import com.l2jfree.gameserver.instancemanager.PetitionManager;
import com.l2jfree.gameserver.instancemanager.SiegeManager;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2ClanMember;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2ShortCut;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.ClanHall;
import com.l2jfree.gameserver.model.entity.Couple;
import com.l2jfree.gameserver.model.entity.Fort;
import com.l2jfree.gameserver.model.entity.FortSiege;
import com.l2jfree.gameserver.model.entity.Hero;
import com.l2jfree.gameserver.model.entity.L2Event;
import com.l2jfree.gameserver.model.entity.Siege;
import com.l2jfree.gameserver.model.mapregion.TeleportWhereType;
import com.l2jfree.gameserver.model.olympiad.Olympiad;
import com.l2jfree.gameserver.model.quest.Quest;
import com.l2jfree.gameserver.model.quest.QuestState;
import com.l2jfree.gameserver.model.restriction.ObjectRestrictions;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions;
import com.l2jfree.gameserver.network.SystemMessageId;
import com.l2jfree.gameserver.network.serverpackets.ClientSetTime;
import com.l2jfree.gameserver.network.serverpackets.Die;
import com.l2jfree.gameserver.network.serverpackets.ExBasicActionList;
import com.l2jfree.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jfree.gameserver.network.serverpackets.FriendList;
import com.l2jfree.gameserver.network.serverpackets.GameGuardQuery;
import com.l2jfree.gameserver.network.serverpackets.HennaInfo;
import com.l2jfree.gameserver.network.serverpackets.ItemList;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfree.gameserver.network.serverpackets.PledgeShowMemberListAll;
import com.l2jfree.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jfree.gameserver.network.serverpackets.PledgeStatusChanged;
import com.l2jfree.gameserver.network.serverpackets.QuestList;
import com.l2jfree.gameserver.network.serverpackets.ShortCutInit;
import com.l2jfree.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jfree.gameserver.network.serverpackets.SkillCoolTime;
import com.l2jfree.gameserver.network.serverpackets.SystemMessage;
import com.l2jfree.gameserver.network.serverpackets.UserInfo;
import com.l2jfree.gameserver.util.FloodProtector;

/**
 * Enter World Packet Handler
 * <p>
 * 0000: 03
 * <p>
 * packet format rev656 cbdddd
 * <p>
 * 
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends L2GameClientPacket
{
	private static final String	_C__03_ENTERWORLD	= "[C] 03 EnterWorld";

	/**
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			_log.warn("EnterWorld failed! activeChar is null...");
			getClient().closeNow();
			return;
		}

		// restore instance
		if (Config.RESTORE_PLAYER_INSTANCE)
			activeChar.setInstanceId(InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId()));
		else
		{
			int instanceId = InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId());
			if (instanceId > 0)
				InstanceManager.getInstance().getInstance(instanceId).removePlayer(activeChar.getObjectId());
		}

		// Restore Vitality
		if (Config.RECOVER_VITALITY_ON_RECONNECT)
			activeChar.restoreVitality();

		// Register in flood protector
		FloodProtector.registerNewPlayer(activeChar);

		if (activeChar.isGM())
		{
			if (Config.SHOW_GM_LOGIN)
			{
				String gmname = activeChar.getName();
				String text = "GM " + gmname + " has logged on.";
				Announcements.getInstance().announceToAll(text);
			}
			else
			{
				if (Config.GM_STARTUP_INVISIBLE)
					AdminCommandHandler.getInstance().useAdminCommand(activeChar, "admin_invisible");
				
				if (Config.GM_STARTUP_SILENCE)
					AdminCommandHandler.getInstance().useAdminCommand(activeChar, "admin_silence");
			}
			
			if (Config.GM_STARTUP_INVULNERABLE)
				AdminCommandHandler.getInstance().useAdminCommand(activeChar, "admin_invul");
			
			if (Config.GM_NAME_COLOR_ENABLED)
			{
				if (activeChar.getAccessLevel() >= 100)
					activeChar.getAppearance().setNameColor(Config.ADMIN_NAME_COLOR);
				else if (activeChar.getAccessLevel() >= 75)
					activeChar.getAppearance().setNameColor(Config.GM_NAME_COLOR);
			}
			if (Config.GM_TITLE_COLOR_ENABLED)
			{
				if (activeChar.getAccessLevel() >= 100)
					activeChar.getAppearance().setTitleColor(Config.ADMIN_TITLE_COLOR);
				else if (activeChar.getAccessLevel() >= 75)
					activeChar.getAppearance().setTitleColor(Config.GM_TITLE_COLOR);
			}

			if (Config.GM_STARTUP_AUTO_LIST)
				GmListTable.getInstance().addGm(activeChar, false);
			else
				GmListTable.getInstance().addGm(activeChar, true);
		}
		else if (activeChar.getClan() != null && activeChar.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED
				&& activeChar.getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
		{
			if (Config.CLAN_LEADER_COLORED == Config.ClanLeaderColored.name)
				activeChar.getAppearance().setNameColor(Config.CLAN_LEADER_COLOR);
			else
				activeChar.getAppearance().setTitleColor(Config.CLAN_LEADER_COLOR);
		}
		if (activeChar.isCharViP())
		{
			if (Config.CHAR_VIP_COLOR_ENABLED)
				activeChar.getAppearance().setNameColor(Config.CHAR_VIP_COLOR);
		}

		if (activeChar.getStatus().getCurrentHp() < 0.5) // is dead
			activeChar.setIsDead(true);

		// Restore character's siege state
		if (activeChar.getClan() != null)
		{
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
					continue;
				if (siege.checkIsAttacker(activeChar.getClan()))
					activeChar.setSiegeState((byte) 1);
				else if (siege.checkIsDefender(activeChar.getClan()))
					activeChar.setSiegeState((byte) 2);
			}

			for (FortSiege fsiege : FortSiegeManager.getInstance().getSieges())
			{
				if (!fsiege.getIsInProgress())
					continue;
				if (fsiege.checkIsAttacker(activeChar.getClan()))
					activeChar.setSiegeState((byte) 1);
				else if (fsiege.checkIsDefender(activeChar.getClan()))
					activeChar.setSiegeState((byte) 2);
			}
		}

		if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
			activeChar.setHero(true);

		//Updating Seal of Strife Buff/Debuff
		if (SevenSigns.getInstance().isSealValidationPeriod())
		{
			int owner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE);
			if (owner != SevenSigns.CABAL_NULL)
			{
				int cabal = SevenSigns.getInstance().getPlayerCabal(activeChar);
				if (cabal == owner)
					activeChar.addSkill(SkillTable.getInstance().getInfo(5074, 1), false);
				else if (cabal != SevenSigns.CABAL_NULL)
					activeChar.addSkill(SkillTable.getInstance().getInfo(5075, 1), false);
			}
		}

		sendPacket(new UserInfo(activeChar));

		// Send Macro List
		activeChar.getMacroses().sendUpdate();

		// Send Item List
		sendPacket(new ItemList(activeChar, false));

		// Send gg check (even if we are not going to check for reply)
		activeChar.queryGameGuard();

		// Send Shortcuts
		sendPacket(new ShortCutInit(activeChar));

		activeChar.sendSkillList();

		activeChar.sendPacket(new HennaInfo(activeChar));

		Quest.playerEnter(activeChar);
		activeChar.sendPacket(new QuestList(activeChar));
		loadTutorial(activeChar);

		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			activeChar.setProtection(true);

		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		activeChar.getKnownList().updateKnownObjects();

		if (L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
			L2Event.restoreChar(activeChar);
		else if (L2Event.connectionLossData.containsKey(activeChar.getName()))
			L2Event.restoreAndTeleChar(activeChar);

		activeChar.updateEffectIcons();
		activeChar.sendEtcStatusUpdate();

		//Expand Skill
		ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);
		activeChar.sendPacket(esmc);

		FriendList fl = new FriendList(activeChar);
		sendPacket(fl);

		SystemMessage sm = new SystemMessage(SystemMessageId.WELCOME_TO_LINEAGE);
		sendPacket(sm);

		// Send client time
		sendPacket(ClientSetTime.STATIC_PACKET);

		if (Config.SHOW_LICENSE)
			L2JfreeInfo.versionInfo(activeChar);

		if (Config.SHOW_HTML_NEWBIE && activeChar.getLevel() < Config.LEVEL_HTML_NEWBIE)
		{
			String Newbie_Path = "data/html/newbie.htm";
			File mainText = new File(Config.DATAPACK_ROOT, Newbie_Path);
			if (mainText.exists())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Newbie_Path);
				html.replace("%name%", activeChar.getName()); // replaces %name% with activeChar.getName(), so you can say like "welcome to the server %name%"
				sendPacket(html);
			}
		}
		else if (Config.SHOW_HTML_GM && activeChar.isGM())
		{
			String Gm_Path = "data/html/gm.htm";
			File mainText = new File(Config.DATAPACK_ROOT, Gm_Path); // Return the pathfile of the HTML file
			if (mainText.exists())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Gm_Path);
				html.replace("%name%", activeChar.getName()); // replaces %name% with activeChar.getName(), so you can say like "welcome to the server %name%"
				sendPacket(html);
			}
		}
		else if (Config.SHOW_HTML_WELCOME)
		{
			String Welcome_Path = "data/html/welcome.htm";
			File mainText = new File(Config.DATAPACK_ROOT, Welcome_Path); // Return the pathfile of the HTML file
			if (mainText.exists())
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(Welcome_Path);
				html.replace("%name%", activeChar.getName()); // replaces %name% with activeChar.getName(), so you can say like "welcome to the server %name%"
				sendPacket(html);
			}
		}

		// Resume paused restrictions
		ObjectRestrictions.getInstance().resumeTasks(activeChar.getObjectId());

		// check player skills
		if (Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
			activeChar.checkAllowedSkills();

		// check for academy
		activeChar.academyCheck(activeChar.getClassId().getId());

		// check for crowns
		CrownManager.getInstance().checkCrowns(activeChar);

		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);

		if (Config.ONLINE_PLAYERS_AT_STARTUP)
		{
			sm = new SystemMessage(SystemMessageId.S1);
			if (L2World.getInstance().getAllPlayers().size() == 1)
				sm.addString("Player online: " + L2World.getInstance().getAllPlayers().size());
			else
				sm.addString("Players online: " + L2World.getInstance().getAllPlayers().size());
			sendPacket(sm);
		}

		PetitionManager.getInstance().checkPetitionMessages(activeChar);

		if (activeChar.getClanId() != 0 && activeChar.getClan() != null)
		{
			PledgeShowMemberListAll psmla = new PledgeShowMemberListAll(activeChar.getClan());
			sendPacket(psmla);
			PledgeStatusChanged psc = new PledgeStatusChanged(activeChar.getClan());
			sendPacket(psc);
		}

		if (activeChar.isAlikeDead()) // dead or fake dead
		{
			// no broadcast needed since the player will already spawn dead to others
			Die d = new Die(activeChar);
			sendPacket(d);
		}

		// engage and notify Partner
		if (Config.ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar);

			// Check if player is maried and remove if necessary Cupid's Bow
			if (!activeChar.isMaried())
			{
				L2ItemInstance item = activeChar.getInventory().getItemByItemId(9140);
				// Remove Cupid's Bow
				if (item != null)
				{
					activeChar.destroyItem("Removing Cupid's Bow", item, activeChar, true);
					activeChar.getInventory().updateDatabase();
					// Log it
					_log.info("Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " got Cupid's Bow removed.");
				}
			}
		}

		// notify Friends
		notifyFriends(activeChar);

		//notify Clanmembers
		notifyClanMembers(activeChar);
		//notify sponsor or apprentice
		notifySponsorOrApprentice(activeChar);

		activeChar.onPlayerEnter();

		sendPacket(new SkillCoolTime(activeChar));

		if (Olympiad.getInstance().playerInStadia(activeChar))
		{
			activeChar.doRevive();
			activeChar.teleToLocation(TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium.");
		}

		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), true)) // Exclude waiting room
		{
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);
		}

		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			sm = new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
			activeChar.sendPacket(sm);
		}

		if (activeChar.getClan() != null)
		{
			//Sets the apropriate Pledge Class for the clannie (e.g. Viscount, Count, Baron, Marquiz)
			activeChar.setPledgeClass(L2ClanMember.getCurrentPledgeClass(activeChar));

			// Add message if clanHall not paid. Possibly this is custom...
			ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());
			if (clanHall != null)
			{
				if (!clanHall.getPaid())
					activeChar.sendPacket(new SystemMessage(
							SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_TOMORROW));
			}
		}

		updateShortCuts(activeChar);

		// remove combat flag before teleporting
		L2ItemInstance flag = activeChar.getInventory().getItemByItemId(9819);
		if (flag != null)
		{
			Fort fort = FortManager.getInstance().getFort(activeChar);
			if (fort != null)
			{
				FortSiegeManager.getInstance().dropCombatFlag(activeChar);
			}
			else
			{
				int slot = flag.getItem().getBodyPart();
				activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
				activeChar.destroyItem("CombatFlag", flag, null, true);
			}
		}
		if (!activeChar.isGM() && activeChar.getSiegeState() < 2 && SiegeManager.getInstance().checkIfInZone(activeChar))
		{
			// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
			activeChar.teleToLocation(TeleportWhereType.Town);
			//activeChar.sendMessage("You have been teleported to the nearest town due to you being in siege zone"); - custom
		}

		RegionBBSManager.getInstance().changeCommunityBoard();

		if (Config.GAMEGUARD_ENFORCE)
		{
			activeChar.sendPacket(GameGuardQuery.STATIC_PACKET);
		}
		
		if (!activeChar.isTransformed())
		{
			activeChar.regiveTemporarySkills();
			// Send Action list
			activeChar.sendPacket(ExBasicActionList.DEFAULT_ACTION_LIST);
		}
		else
		{
			activeChar.sendPacket(ExBasicActionList.TRANSFORMED_ACTION_LIST);
		}
		
		GlobalRestrictions.playerLoggedIn(activeChar);
	}

	/**
	 * @param activeChar
	 */
	private void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();

		for (Couple cl : CoupleManager.getInstance().getCouples())
		{
			if (cl.getPlayer1Id() == _chaid || cl.getPlayer2Id() == _chaid)
			{
				if (cl.getMaried())
					cha.setMaried(true);

				cha.setCoupleId(cl.getId());

				if (cl.getPlayer1Id() == _chaid)
				{
					cha.setPartnerId(cl.getPlayer2Id());
				}
				else
				{
					cha.setPartnerId(cl.getPlayer1Id());
				}
			}
		}
	}

	/**
	 * @param activeChar partnerid
	 */
	private void notifyPartner(L2PcInstance cha)
	{
		if (cha.getPartnerId() != 0)
		{

			L2PcInstance partner = L2World.getInstance().getPlayer(cha.getPartnerId());

			if (partner != null)
				partner.sendMessage("Your Partner " + cha.getName() + " has logged in.");
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifyFriends(L2PcInstance cha)
	{
		SystemMessage sm = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm.addPcName(cha);
		
		for (Integer objId : cha.getFriendList().getFriendIds())
		{
			L2PcInstance friend = L2World.getInstance().findPlayer(objId);
			if (friend != null)
			{
				friend.sendPacket(new FriendList(friend));
				friend.sendPacket(sm);
			}
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			L2ClanMember clanmember = clan.getClanMember(activeChar.getObjectId());
			if (clanmember != null)
			{
				clanmember.setPlayerInstance(activeChar);
				SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
				msg.addString(activeChar.getName());
				clan.broadcastToOtherOnlineMembers(msg, activeChar);
				msg = null;
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
				if (clan.isNoticeEnabled() && !clan.getNotice().isEmpty())
					sendPacket(new NpcHtmlMessage(1, "<html><title>Clan Announcements</title><body><br><center><font color=\"CCAA00\">"
							+ activeChar.getClan().getName() + "</font> <font color=\"6655FF\">Clan Alert Message</font></center><br>"
							+ "<img src=\"L2UI.SquareWhite\" width=270 height=1><br>" + activeChar.getClan().getNotice() + "</body></html>"));
			}
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = L2World.getInstance().getPlayer(activeChar.getSponsor());

			if (sponsor != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = L2World.getInstance().getPlayer(activeChar.getApprentice());

			if (apprentice != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}

	/**
	 * CT1 doesn't update shortcuts so we need to reregister them to the client
	 * 
	 * @param activeChar
	 */
	private void updateShortCuts(L2PcInstance activeChar)
	{
		L2ShortCut[] allShortCuts = activeChar.getAllShortCuts();

		for (L2ShortCut sc : allShortCuts)
		{
			activeChar.sendPacket(new ShortCutRegister(sc));
		}
	}

	private void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent("UC", null, player);
	}

	/* (non-Javadoc)
	 * @see com.l2jfree.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
}
