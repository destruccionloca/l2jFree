/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.clientpackets;

import java.io.File;
import java.io.UnsupportedEncodingException;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.Olympiad;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CrownManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2FriendList;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.CharKnownList.KnownListAsynchronousUpdateTask;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.events.CTF;
import net.sf.l2j.gameserver.model.entity.events.DM;
import net.sf.l2j.gameserver.model.entity.events.TvT;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.gameserver.serverpackets.ClientSetTime;
import net.sf.l2j.gameserver.serverpackets.Die;
import net.sf.l2j.gameserver.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.serverpackets.FriendList;
import net.sf.l2j.gameserver.serverpackets.GameGuardQuery;
import net.sf.l2j.gameserver.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.QuestList;
import net.sf.l2j.gameserver.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.serverpackets.SignsSky;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
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
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private final static Log _log = LogFactory.getLog(EnterWorld.class.getName());

	public TaskPriority getPriority() { return TaskPriority.PR_URGENT; }
	
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

//		if(!getClient().getAccountName().equalsIgnoreCase(getClient().getAccountName(activeChar.getName())))
//        {
//            _log.fatal("Possible Hacker Account:"+getClient().getAccountName()+" tried to login with char: "+activeChar.getName() + "of Account:" + getClient().getAccountName(activeChar.getName()));
//            activeChar.closeNetConnection();
//        }
//        if(!getClient().isAuthed())
//        {
//            _log.fatal("Possible Hacker Account:"+getClient().getAccountName()+" is not authed");
//            activeChar.closeNetConnection();
//        }
        if (activeChar.isGM())
        {
            if (Config.SHOW_GM_LOGIN) 
            { 
                String gmname = activeChar.getName(); 
                String text = "GM "+gmname+" has logged on."; 
                Announcements.getInstance().announceToAll(text); 
            }
            else
            {
                if(Config.GM_STARTUP_INVISIBLE 
                        && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_GODMODE
                          || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invisible")))
                    activeChar.getAppearance().setInvisible();

                if(Config.GM_STARTUP_SILENCE 
                        && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_MENU
                          || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_silence")))
                    activeChar.setMessageRefusal(true);
            }

            if (Config.GM_STARTUP_INVULNERABLE 
                    && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_GODMODE
                      || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invul")))
                activeChar.setIsInvul(true);

            if (Config.GM_NAME_COLOR_ENABLED)
            {
                if (activeChar.getAccessLevel() >= 100)
                    activeChar.getAppearance().setNameColor(Config.ADMIN_NAME_COLOR);
                else if (activeChar.getAccessLevel() >= 75)
                    activeChar.getAppearance().setNameColor(Config.GM_NAME_COLOR);
            }
            
            if (Config.GM_STARTUP_AUTO_LIST)
                GmListTable.getInstance().addGm(activeChar);
        }
        if(activeChar.getClan() != null && activeChar.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED && activeChar.getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
        {
        	if(Config.CLAN_LEADER_COLORED == Config.ClanLeaderColored.name)
        		activeChar.getAppearance().setNameColor(Config.CLAN_LEADER_COLOR);
        	else
        		activeChar.getAppearance().setTitleColor(Config.CLAN_LEADER_COLOR);
        }
        if (activeChar.isCharViP())
        {
        	if(Config.CHAR_VIP_COLOR_ENABLED)
        		activeChar.getAppearance().setNameColor(Config.CHAR_VIP_COLOR);
        }        
        
        if (Config.PLAYER_SPAWN_PROTECTION > 0)
            activeChar.setProtection(true);
        
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		
		ThreadPoolManager.getInstance().executeTask(new KnownListAsynchronousUpdateTask(activeChar));
		
		if (L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
	        L2Event.restoreChar(activeChar);
		else if (L2Event.connectionLossData.containsKey(activeChar.getName()))            
			L2Event.restoreAndTeleChar(activeChar);
	
		
		if (SevenSigns.getInstance().isSealValidationPeriod())
            sendPacket(new SignsSky());
        
        if (Config.STORE_SKILL_COOLTIME)
            activeChar.restoreEffects();
        
        if (activeChar.getAllEffects() != null)
        {
            for (L2Effect e : activeChar.getAllEffects())
            {
                if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
                {
                    activeChar.stopEffects(L2Effect.EffectType.HEAL_OVER_TIME);
                    activeChar.removeEffect(e);
                }
                if (e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
                {
                    activeChar.stopEffects(L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME);
                    activeChar.removeEffect(e);
                }
            }
        }
        // apply augmentation boni for equipped items
        for (L2ItemInstance temp : activeChar.getInventory().getAugmentedItems())
        	if (temp != null && temp.isEquipped()) temp.getAugmentation().applyBoni(activeChar);
        
        //Expand Skill
        ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);  
        activeChar.sendPacket(esmc);
       
        activeChar.getMacroses().sendUpdate();

        UserInfo ui = new UserInfo(activeChar);
        sendPacket(ui);

        HennaInfo hi = new HennaInfo(activeChar);
        sendPacket(hi);
        
        FriendList fl = new FriendList(activeChar);
        sendPacket(fl);
        
        ItemList il = new ItemList(activeChar, false);
        sendPacket(il);

        ShortCutInit sci = new ShortCutInit(activeChar);
        sendPacket(sci);
        
        ClientSetTime cst = new ClientSetTime();
        sendPacket(cst);
                
        SystemMessage sm = new SystemMessage(SystemMessageId.WELCOME_TO_LINEAGE);
        sendPacket(sm);

        if (Config.SHOW_L2J_LICENSE)
        {
        	sm = new SystemMessage(SystemMessageId.S1_S2);
	        sm.addString(getText("VGhpcyBTZXJ2ZXIgaXMgcnVubmluZyBMMko="));
            sm.addString(getText("IHZlcnNpb24gNiBkZXYvdW5zdGFibGU="));
	        sendPacket(sm);
	        sm = new SystemMessage(SystemMessageId.S1_S2);
	        sm.addString(getText("Y3JlYXRlZCBieSBMMkNoZWYgYW5kIHRoZQ=="));
	        sm.addString(getText("IEwySiB0ZWFtLg=="));
	        sendPacket(sm);
	        sm = new SystemMessage(SystemMessageId.S1_S2);
	        sm.addString(getText("dmlzaXQgbDJqc2VydmVyLmNvbQ=="));
	        sm.addString(getText("ICBmb3Igc3VwcG9ydC4="));
	        sendPacket(sm);
	        sm = new SystemMessage(SystemMessageId.S1_S2);
	        sm.addString(getText("V2VsY29tZSB0byA="));
	        sm.addString(LoginServerThread.getInstance().getServerName());
	        sendPacket(sm);
        
            VersionningService versionningService = (VersionningService)L2Registry.getBean(IServiceRegistry.VERSIONNING);
            Version version = versionningService.getVersion();
            if (version!= null)
	        {
	        	sm = new SystemMessage(SystemMessageId.S1_S2);
	            sm.addString(getText("TDJKIFNlcnZlciBWZXJzaW9uOg==")+"   "+version.getRevisionNumber());
	            sendPacket(sm);
	            sm = new SystemMessage(SystemMessageId.S1_S2);
	            sm.addString(getText("TDJKIFNlcnZlciBCdWlsZCBEYXRlOg==")+" "+version.getBuildDate());
	            sendPacket(sm);
	        }
        }
        
        if (Config.SHOW_HTML_WELCOME)
        {
            String Welcome_Path = "data/html/welcome.htm";
            File mainText = new File(Config.DATAPACK_ROOT, Welcome_Path);	// Return the pathfile of the HTML file
            if (mainText.exists())
            {   
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(Welcome_Path);
                sendPacket(html);
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
        		sendPacket(html);
        	}
        }
        
        // set hero status to character if character is Hero
        if (Hero.getInstance().getHeroes() != null &&
                Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
            activeChar.setHero(true);
        
        // check player skills
        if(Config.CHECK_SKILLS_ON_ENTER && !Config.ALT_GAME_SKILL_LEARN)
        	activeChar.checkAllowedSkills();

        // check for academy
        activeChar.academyCheck(activeChar.getClassId().getId());
        
        // check for crowns
        CrownManager.getInstance().checkCrowns(activeChar);

        SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
        Announcements.getInstance().showAnnouncements(activeChar);

        if(Config.ONLINE_PLAYERS_AT_STARTUP)
        {
             sm = new SystemMessage(SystemMessageId.S1_S2);
             if (L2World.getInstance().getAllPlayers().size() == 1)
            	 sm.addString("Player online: ");
             else
            	 sm.addString("Players online: ");
             sm.addNumber(L2World.getInstance().getAllPlayers().size());
             sendPacket(sm);
        }        

		Quest.playerEnter(activeChar);
		
		PetitionManager.getInstance().checkPetitionMessages(activeChar);
		
        if (activeChar.getClanId() != 0 && activeChar.getClan() != null)
        {
        	PledgeShowMemberListAll psmla = new PledgeShowMemberListAll(activeChar.getClan(), activeChar);
            sendPacket(psmla);
            PledgeStatusChanged psc = new PledgeStatusChanged(activeChar.getClan());
            sendPacket(psc);
        }
	
		if (activeChar.isAlikeDead())
		{
			// no broadcast needed since the player will already spawn dead to others
			Die d = new Die(activeChar);
			sendPacket(d);
		}

		if (Config.ALLOW_WATER)
		    activeChar.checkWaterState();

		setPledgeClass(activeChar);
		
		//add char to online characters
		activeChar.setOnlineStatus(true);

        // engage and notify Partner
        if(Config.ALLOW_WEDDING)
        {
            engage(activeChar);
            notifyPartner(activeChar);
        }

        // notify Friends
        notifyFriends(activeChar);

        //notify Clanmembers
		notifyClanMembers(activeChar);
        //notify sponsor or apprentice
        notifySponsorOrApprentice(activeChar);
        
        showPledgeSkillList(activeChar);
        
        activeChar.onPlayerEnter();
        
        if (Olympiad.getInstance().playerInStadia(activeChar))
        {
            activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
            activeChar.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadia");
        }
        
		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			sm = new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
			activeChar.sendPacket(sm);
		}
		
		if (activeChar.getClan() != null)
		{
			PledgeSkillList psl = new PledgeSkillList(activeChar.getClan());
			activeChar.sendPacket(psl);
			
			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress()) continue;
				if (siege.checkIsAttacker(activeChar.getClan()))
					activeChar.setSiegeState((byte)1);
				else if (siege.checkIsDefender(activeChar.getClan()))
					activeChar.setSiegeState((byte)2);
			}
		}

        RegionBBSManager.getInstance().changeCommunityBoard();

        if(Config.GAMEGUARD_ENFORCE)
        {
            GameGuardQuery ggq = new GameGuardQuery();
        	activeChar.sendPacket(ggq);
        }
        
        if (TvT._savePlayers.contains(activeChar.getName()))
           TvT.addDisconnectedPlayer(activeChar);

    	if (CTF._savePlayers.contains(activeChar.getName()))
    	    CTF.addDisconnectedPlayer(activeChar);
        
        if (DM._savePlayers.contains(activeChar.getName()))
            DM.addDisconnectedPlayer(activeChar);

        QuestList ql = new QuestList();
        activeChar.sendPacket(ql);
	}


    /**
     * @param activeChar
     */
    private void engage(L2PcInstance cha)
    {
        int _chaid = cha.getObjectId();
    
        for(Couple cl: CoupleManager.getInstance().getCouples())
        {
           if(cl.getPlayer1Id()==_chaid || cl.getPlayer2Id()==_chaid)
            {
                if(cl.getMaried())
                    cha.setMaried(true);

                cha.setCoupleId(cl.getId());
                
                if(cl.getPlayer1Id()==_chaid)
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
        if(cha.getPartnerId()!=0)
        {
            L2PcInstance partner;
            partner = (L2PcInstance)L2World.getInstance().findObject(cha.getPartnerId());
            
            if (partner != null)
            {
                partner.sendMessage("Your Partner has logged in");
            }
            
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
        
        for(String friendName : L2FriendList.getFriendListNames(cha))
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
			if(clanmember!=null)
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
           L2PcInstance sponsor = (L2PcInstance)L2World.getInstance().findObject(activeChar.getSponsor());
           
           if (sponsor != null)
           {
               SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
               msg.addString(activeChar.getName());
               sponsor.sendPacket(msg);
           }
       }
       else if (activeChar.getApprentice() != 0)
       {
           L2PcInstance apprentice = (L2PcInstance)L2World.getInstance().findObject(activeChar.getApprentice());
           
           if (apprentice != null)
           {
               SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN);
               msg.addString(activeChar.getName());
               apprentice.sendPacket(msg);
           }
        }
    }

    /**
     * @param activeChar
     */
    private void showPledgeSkillList(L2PcInstance activeChar)
    {
        L2Clan clan = activeChar.getClan();
        if (clan != null && clan.getReputationScore() >= 0)
        {
            PledgeSkillList response = new PledgeSkillList(clan);
            L2Skill[] skills = clan.getAllSkills();
            
            // TODO why this code ???
            for (int i = 0; i < skills.length; i++)
            {
                L2Skill s = skills[i];
                
                if (s == null) 
                    continue;
                
                response.addSkill(s.getId(), s.getLevel());
            }
            
            sendPacket(response);
        }
    }

	/**
	 * @param string
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getText(String string)
	{
		try {
			String result = new String(Base64.decode(string), "UTF-8"); 
			return result;
		} catch (UnsupportedEncodingException e) {
			// huh, UTF-8 is not supported? :)
			return null;
		}
	}

    /* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}

	private void setPledgeClass(L2PcInstance activeChar)
	{
		int pledgeClass = 0;
		if ( activeChar.getClan() != null)
			pledgeClass = activeChar.getClan().getClanMember(activeChar.getObjectId()).calculatePledgeClass(activeChar);
		
		if (activeChar.isNoble() && pledgeClass < 5)
	           pledgeClass = 5;
		
	    if (activeChar.isHero())
	           pledgeClass = 8;
	           
	    activeChar.setPledgeClass(pledgeClass);
	}
}
