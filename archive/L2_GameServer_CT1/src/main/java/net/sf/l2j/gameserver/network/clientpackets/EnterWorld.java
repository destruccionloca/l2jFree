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
package net.sf.l2j.gameserver.network.clientpackets;

import java.io.File;
import java.io.UnsupportedEncodingException;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.Olympiad;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CrownManager;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.instancemanager.TransformationManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2FriendList;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.events.CTF;
import net.sf.l2j.gameserver.model.entity.events.DM;
import net.sf.l2j.gameserver.model.entity.events.TvT;
import net.sf.l2j.gameserver.model.mapregion.TeleportWhereType;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.QuestState;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ClientSetTime;
import net.sf.l2j.gameserver.network.serverpackets.Die;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExBasicActionList;
import net.sf.l2j.gameserver.network.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.network.serverpackets.FriendList;
import net.sf.l2j.gameserver.network.serverpackets.GameGuardQuery;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.network.serverpackets.QuestList;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.network.serverpackets.ShortCutRegister;
import net.sf.l2j.gameserver.network.serverpackets.SkillCoolTime;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.gameserver.util.FloodProtector;
import net.sf.l2j.tools.L2Registry;
import net.sf.l2j.tools.codec.Base64;
import net.sf.l2j.tools.versionning.model.Version;
import net.sf.l2j.tools.versionning.service.VersionningService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Enter World Packet Handler<p>
 * <p>
 * 0000: 03 <p>
 * packet format rev656 cbdddd
 * <p>
 *
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends L2GameClientPacket
{
	private static final String	_C__03_ENTERWORLD	= "[C] 03 EnterWorld";
	private final static Log	_log				= LogFactory.getLog(EnterWorld.class.getName());

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_URGENT;
	}

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

		// Register in flood protector
		FloodProtector.getInstance().registerNewPlayer(activeChar.getObjectId());

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
				if (Config.GM_STARTUP_INVISIBLE
						&& (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_GODMODE || Config.ALT_PRIVILEGES_ADMIN
								&& AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invisible")))
					activeChar.getAppearance().setInvisible();

				if (Config.GM_STARTUP_SILENCE
						&& (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_MENU || Config.ALT_PRIVILEGES_ADMIN
								&& AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_silence")))
					activeChar.setMessageRefusal(true);
			}

			if (Config.GM_STARTUP_INVULNERABLE
					&& (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_GODMODE || Config.ALT_PRIVILEGES_ADMIN
							&& AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invul")))
				activeChar.setIsInvul(true);

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
		if (!activeChar.isGM() && activeChar.getClan() != null && activeChar.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED
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

		if (activeChar.getClan() != null)
		{
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
					continue;
				if (siege.checkIsAttacker(activeChar.getClan()))
					activeChar.setSiegeState((byte)1);
				else if (siege.checkIsDefender(activeChar.getClan()))
					activeChar.setSiegeState((byte)2);
			}
		}

		if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
			activeChar.setHero(true);

		sendPacket(new UserInfo(activeChar));

		// Send Macro List
		activeChar.getMacroses().sendUpdate();

		// Send Item List
		sendPacket(new ItemList(activeChar, false));

		// Send gg check (even if we are not going to check for reply)
		activeChar.queryGameGuard();

		// Send Shortcuts
		sendPacket(new ShortCutInit(activeChar));

		// Send Action list
		activeChar.sendPacket(new ExBasicActionList());

		activeChar.sendSkillList();

		activeChar.sendPacket(new HennaInfo(activeChar));


		Quest.playerEnter(activeChar);
		activeChar.sendPacket(new QuestList(activeChar));
		loadTutorial(activeChar);

		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			activeChar.setProtection(true);

		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		if (L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
			L2Event.restoreChar(activeChar);
		else if (L2Event.connectionLossData.containsKey(activeChar.getName()))
			L2Event.restoreAndTeleChar(activeChar);

		activeChar.updateEffectIcons();

		activeChar.sendPacket(new EtcStatusUpdate(activeChar));

		//Expand Skill
		ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);
		activeChar.sendPacket(esmc);

		FriendList fl = new FriendList(activeChar);
		sendPacket(fl);

		SystemMessage sm = new SystemMessage(SystemMessageId.WELCOME_TO_LINEAGE);
		sendPacket(sm);
		
		// Send client time
		sendPacket(ClientSetTime.STATIC_PACKET);

		if (Config.SHOW_L2J_LICENSE)
		{
			sm = new SystemMessage(SystemMessageId.S2_S1);
			sm.addString(getText("IHZlcnNpb24gNiBkZXYvdW5zdGFibGU="));
			sm.addString(getText("VGhpcyBTZXJ2ZXIgaXMgcnVubmluZyBMMko="));
			sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.S2_S1);
			sm.addString(getText("IEwySiB0ZWFtLg=="));
			sm.addString(getText("Y3JlYXRlZCBieSBMMkNoZWYgYW5kIHRoZQ=="));
			sendPacket(sm);
			sm = new SystemMessage(SystemMessageId.S2_S1);
			sm.addString(getText("ICBmb3Igc3VwcG9ydC4="));
			sm.addString(getText("dmlzaXQgbDJqc2VydmVyLmNvbQ=="));
			sendPacket(sm);

			VersionningService versionningService = (VersionningService) L2Registry.getBean(IServiceRegistry.VERSIONNING);
			Version version = versionningService.getVersion();
			if (version != null)
			{
				sm = new SystemMessage(SystemMessageId.S1);
				sm.addString(getText("TDJKIFNlcnZlciBWZXJzaW9uOg==") + "   " + version.getRevisionNumber());
				sendPacket(sm);
				sm = new SystemMessage(SystemMessageId.S1);
				sm.addString(getText("TDJKIFNlcnZlciBCdWlsZCBEYXRlOg==") + " " + version.getBuildDate());
				sendPacket(sm);
			}
		}

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
			PledgeShowMemberListAll psmla = new PledgeShowMemberListAll(activeChar.getClan(), activeChar);
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
			activeChar.teleToLocation(TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium.");
		}

		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
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
							SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
			}
		}

		updateShortCuts(activeChar);

		if (!activeChar.isGM() && activeChar.getSiegeState() < 2 && SiegeManager.getInstance().checkIfInZone(activeChar))
		{
			// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
			activeChar.teleToLocation(TeleportWhereType.Town);
			activeChar.sendMessage("You have been teleported to the nearest town due to you being in siege zone");
		}

		RegionBBSManager.getInstance().changeCommunityBoard();

		if (Config.GAMEGUARD_ENFORCE)
		{
			activeChar.sendPacket(GameGuardQuery.STATIC_PACKET);
		}

		if (TvT._savePlayers.contains(activeChar.getName()))
			TvT.addDisconnectedPlayer(activeChar);

		if (CTF._savePlayers.contains(activeChar.getName()))
			CTF.addDisconnectedPlayer(activeChar);

		if (DM._savePlayers.contains(activeChar.getName()))
			DM.addDisconnectedPlayer(activeChar);

		if (!activeChar.isTransformed())
			activeChar.regiveTemporarySkills();

		if (activeChar.isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(activeChar.getCursedWeaponEquippedId()).cursedOnLogin();
		}
		else if (activeChar.transformSelectInfo() > 0)
		{
			TransformationManager.getInstance().transformPlayer(activeChar.transformId(), activeChar, Long.MAX_VALUE);
		}
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
			L2Object obj = L2World.getInstance().findObject(cha.getPartnerId());
			if (obj == null || !(obj instanceof L2PcInstance))
			{
				// If other char is deleted, maybe a npc or mob takes the ID
				return;
			}

			L2PcInstance partner = (L2PcInstance) obj;
			partner.sendMessage("Your Partner has logged in");

			partner = null;
		}
	}

	/**
	 * @param activeChar
	 */
	private void notifyFriends(L2PcInstance cha)
	{
		SystemMessage sm = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
		sm.addString(cha.getName());

		for (String friendName : L2FriendList.getFriendListNames(cha))
		{
			L2PcInstance friend = L2World.getInstance().getPlayer(friendName);
			if (friend != null) //friend logged in.
			{
				friend.sendPacket(new FriendList(friend));
				friend.sendPacket(sm);
			}
		}

		sm = null;
	}

	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			L2ClanMember clanmember = clan.getClanMember(activeChar.getName());
			if (clanmember != null)
			{
				clanmember.setPlayerInstance(activeChar);
				SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
				msg.addString(activeChar.getName());
				clan.broadcastToOtherOnlineMembers(msg, activeChar);
				msg = null;
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
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
			L2PcInstance sponsor = (L2PcInstance) L2World.getInstance().findObject(activeChar.getSponsor());

			if (sponsor != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = (L2PcInstance) L2World.getInstance().findObject(activeChar.getApprentice());

			if (apprentice != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}

	/**
	 * CT1 doesn't update shortcuts so we need to reregister them to the client
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

	/**
	 * @param string
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getText(String string)
	{
		try
		{
			String result = new String(Base64.decode(string), "UTF-8");
			return result;
		}
		catch (UnsupportedEncodingException e)
		{
			// huh, UTF-8 is not supported? :)
			return null;
		}
	}

	private void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");
		if (qs != null)
			qs.getQuest().notifyEvent("UC", null, player);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
}
