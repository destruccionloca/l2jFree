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
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.Base64;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.CrownTable;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.Olympiad;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.CTF;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Couple;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.entity.TvT;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.serverpackets.ClientSetTime;
import net.sf.l2j.gameserver.serverpackets.Die;
import net.sf.l2j.gameserver.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.serverpackets.FriendList;
import net.sf.l2j.gameserver.serverpackets.GameGuardQuery;
import net.sf.l2j.gameserver.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PledgeReceivePowerInfo;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.serverpackets.PledgeSkillList;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.QuestList;
import net.sf.l2j.gameserver.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.serverpackets.SignsSky;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import net.sf.l2j.gameserver.serverpackets.ExStorageMaxCount;

/**
 * Enter World Packet Handler<p>
 * <p>
 * 0000: 03 <p>
 * packet format rev656 cbdddd
 * <p>
 * 
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends ClientBasePacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private final static Log _log = LogFactory.getLog(EnterWorld.class.getName());

	public TaskPriority getPriority() { return TaskPriority.PR_URGENT; }

	private static String Welcome_Path = "welcome" ;
	
	/**
	 * @param decrypt
	 */
	public EnterWorld(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		// this is just a trigger packet. it has no content
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
        { 
            _log.warn("EnterWorld failed! activeChar is null..."); 			
		    return;
        }

		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null) 
        { 
			if (_log.isDebugEnabled())
					_log.warn("User already exist in OID map! User "+activeChar.getName()+" is character clone"); 
                //activeChar.closeNetConnection(); 
        }
		if(!getClient().getLoginName().equalsIgnoreCase(getClient().getAccountName(activeChar.getName())))
        {
            _log.fatal("Possible Hacker Account:"+getClient().getLoginName()+" tried to login with char: "+activeChar.getName() + "of Account:" + getClient().getAccountName(activeChar.getName()));
            activeChar.closeNetConnection();
        }
        if(!getClient().isAuthed())
        {
            _log.fatal("Possible Hacker Account:"+getClient().getLoginName()+" is not authed");
            activeChar.closeNetConnection();
        }
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
                    activeChar.setInvisible();

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
                    activeChar.setNameColor(Config.ADMIN_NAME_COLOR);
                else if (activeChar.getAccessLevel() >= 75)
                    activeChar.setNameColor(Config.GM_NAME_COLOR);
            }

            if (Config.GM_TITLE_COLOR_ENABLED)
            {
                if (activeChar.getAccessLevel() >= 100)
                    activeChar.setTitleColor(Config.ADMIN_TITLE_COLOR);
                else if (activeChar.getAccessLevel() >= 75)
                    activeChar.setTitleColor(Config.GM_TITLE_COLOR);
            }
            
            if (Config.GM_STARTUP_AUTO_LIST)
                GmListTable.getInstance().addGm(activeChar);
        }
            if(activeChar.getClan() != null && activeChar.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED && activeChar.getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
            {
                if(Config.CLAN_LEADER_COLORED == Config.ClanLeaderColored.name)
                    activeChar.setNameColor(Config.CLAN_LEADER_COLOR);
                else
                    activeChar.setTitleColor(Config.CLAN_LEADER_COLOR);
            }
        
        
        if (Config.PLAYER_SPAWN_PROTECTION > 0)
            activeChar.setProtection(true);
        
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
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
        
        ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);  
        activeChar.sendPacket(esmc);
       
        activeChar.getMacroses().sendUpdate();
        
        setPledgeClass(activeChar);
        
        sendPacket(new ItemList(activeChar, false));

        sendPacket(new UserInfo(activeChar));

        sendPacket(new ShortCutInit(activeChar));

        sendPacket(new HennaInfo(activeChar));
        
        sendPacket(new FriendList(activeChar));
        
        sendPacket(new ClientSetTime());
                
        SystemMessage sm = new SystemMessage(34);
        sendPacket(sm);

        if (Config.SHOW_L2J_LICENSE)
        {
        	sm = new SystemMessage(SystemMessage.S1_S2);
	        sm.addString(getText("VGhpcyBTZXJ2ZXIgaXMgcnVubmluZyBMMko="));
            sm.addString(getText("IHZlcnNpb24gNiBkZXYvdW5zdGFibGU="));
	        sendPacket(sm);
	        sm = new SystemMessage(SystemMessage.S1_S2);
	        sm.addString(getText("Y3JlYXRlZCBieSBMMkNoZWYgYW5kIHRoZQ=="));
	        sm.addString(getText("IEwySiB0ZWFtLg=="));
	        sendPacket(sm);
	        sm = new SystemMessage(SystemMessage.S1_S2);
	        sm.addString(getText("dmlzaXQgbDJqc2VydmVyLmNvbQ=="));
	        sm.addString(getText("ICBmb3Igc3VwcG9ydC4="));
	        sendPacket(sm);
	        sm = new SystemMessage(SystemMessage.S1_S2);
	        sm.addString(getText("V2VsY29tZSB0byA="));
	        sm.addString(LoginServerThread.getInstance().getServerName());
	        sendPacket(sm);
        
	        if (Config.SERVER_VERSION != null)
	        {
	        	sm = new SystemMessage(SystemMessage.S1_S2);
	            sm.addString(getText("TDJKIFNlcnZlciBWZXJzaW9uOg==")+"   "+Config.SERVER_VERSION);
	            sendPacket(sm);
	            sm = new SystemMessage(SystemMessage.S1_S2);
	            sm.addString(getText("TDJKIFNlcnZlciBCdWlsZCBEYXRlOg==")+" "+Config.SERVER_BUILD_DATE);
	            sendPacket(sm);
	        }
        }
        
        if (Config.SHOW_HTML_WELCOME) {
            Welcome_Path = "data/html/welcome.htm";
            File mainText = new File(Config.DATAPACK_ROOT, Welcome_Path);        // Return the pathfile of the HTML file
            if (mainText.exists())
            {   
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(Welcome_Path);
                sendPacket(html);
            } }

        // check for crowns
        L2Clan activeCharClan  = activeChar.getClan();
        if(activeCharClan!=null) // character has clan ?
        {
            Castle activeCharCastle= CastleManager.getInstance().getCastleByOwner(activeChar.getClan());
            if(activeCharCastle!=null) // clan has castle ?
            {
                for (L2ClanMember member : activeCharClan.getMembers())
                {
                    if(member.getObjectId() == activeChar.getObjectId()) // find member 
                    {
                        int CrownId = CrownTable.getCrownId(activeCharCastle.getCastleId()); // get crown id
        
                        if(activeCharClan.getLeader().getObjectId()==member.getObjectId()) // if leader give lord crown and normal crown
                        {
                            if(activeChar.getInventory().getItemByItemId(6841)==null) // check if character already has a crown in inventory
                            {
                                activeChar.getInventory().addItem("Crown",6841,1,member.getPlayerInstance(),null); // give lord crown
                                activeChar.getInventory().updateDatabase(); // update database
                            }
                            if(activeChar.getInventory().getItemByItemId(CrownId)==null) // check if character already has a crown in inventory
                            {
                                if(CrownId!=0) // check for crown id
                                    activeChar.getInventory().addItem("Crown",CrownId,1,member.getPlayerInstance(),null); // give castle crown
                                    activeChar.getInventory().updateDatabase(); // update database
                            }
                        }
                        else // no leader give normal crown
                        {
                            if(activeChar.getInventory().getItemByItemId(CrownId)==null) // check for crown id in inventory
                            {
                                if(CrownId!=0)
                                {
                                    activeChar.getInventory().addItem("Crown",CrownId,1,member.getPlayerInstance(),null); // give crown
                                    activeChar.getInventory().updateDatabase(); // update database
                                }
                            }
                        }
                    }
                }
            }
        }
        sm = null;

        SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
        Announcements.getInstance().showAnnouncements(activeChar);

        if(Config.ONLINE_PLAYERS_AT_STARTUP)
        {
             sm = new SystemMessage(SystemMessage.S1_S2);
             sm.addString("Players online: ");
             sm.addNumber(L2World.getInstance().getAllPlayers().size());
             sendPacket(sm);
        }        

		Quest.playerEnter(activeChar);

		String serverNews = HtmCache.getInstance().getHtm("data/html/servnews.htm");
		
		if (serverNews != null)
		{
			NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			htmlMsg.setHtml(serverNews);
			sendPacket(htmlMsg);
		}
		
		PetitionManager.getInstance().checkPetitionMessages(activeChar);
				
		// send user info again .. just like the real client
        // sendPacket(ui);

        if (activeChar.getClanId() != 0 && activeChar.getClan() != null)
        {
            if(activeChar.isClanLeader()) 
                activeChar.setClanPrivileges(L2Clan.CP_ALL);
            sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
            sendPacket(new PledgeStatusChanged(activeChar.getClan()));
            sendPacket(new PledgeReceivePowerInfo(activeChar));
    	}
	
		if (activeChar.isAlikeDead())
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}

		if (Config.ALLOW_WATER)
		    activeChar.checkWaterState();

        if (Hero.getInstance().getHeroes() != null &&
                Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
            activeChar.setHero(true);
        
        setPledgeClass(activeChar);

		//add char to online characters
		activeChar.setOnlineStatus(true);

        // engage and notify Partner
        if(Config.ALLOW_WEDDING)
        {
            engage(activeChar);
            notifyPartner(activeChar,activeChar.getPartnerId());
        }

        // notify Friends
        notifyFriends(activeChar);

        //notify Clanmembers
		notifyClanMembers(activeChar);
        if (activeChar.getClan() != null)
            activeChar.getClan().addSkillEffects();
        showPledgeSkillList(activeChar);
        
        activeChar.onPlayerEnter();

        if (Olympiad.getInstance().playerInStadia(activeChar))
        {
            activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
            activeChar.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadia");
        }
        
        if(Config.GAMEGUARD_ENFORCE)
            activeChar.sendPacket(new GameGuardQuery());
        
        if (TvT._savePlayers.contains(activeChar.getName()))
           TvT.addDisconnectedPlayer(activeChar);

    	if (CTF._savePlayers.contains(activeChar.getName()))
               CTF.addDisconnectedPlayer(activeChar);

        QuestList ql = new QuestList();
        activeChar.sendPacket(ql);
        
        activeChar.setClientRevision(getClient().getRevision());
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
    private void notifyPartner(L2PcInstance cha,int partnerId)
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
		java.sql.Connection con = null;
		try {
		    con = L2DatabaseFactory.getInstance().getConnection();
		    PreparedStatement statement;
            statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?");
		    statement.setInt(1, cha.getObjectId());
          //statement.setString(2, cha.getName());
		    ResultSet rset = statement.executeQuery();

            L2PcInstance friend;
            int objectId;
            
            SystemMessage sm = new SystemMessage(SystemMessage.FRIEND_S1_HAS_LOGGED_IN);
            sm.addString(cha.getName());

            while (rset.next())
            {
                objectId = rset.getInt("friend_id");

                friend = (L2PcInstance)L2World.getInstance().findObject(objectId);
                
                if (friend != null) //friend logged in.
                {
                    friend.sendPacket(new FriendList(friend));                	
                    friend.sendPacket(sm);
                }
		    }
            sm = null;            
        } 
		catch (Exception e) {
            _log.warn("could not restore friend data:"+e);
        } 
		finally {
            try {con.close();} catch (Exception e){}
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
			clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
			SystemMessage msg = new SystemMessage(SystemMessage.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			L2PcInstance[] clanMembers = clan.getOnlineMembers(activeChar.getName());
            PledgeShowMemberListUpdate ps = new PledgeShowMemberListUpdate(activeChar);
			for (int i = 0; i < clanMembers.length; i++)
			{
				clanMembers[i].sendPacket(ps);
				clanMembers[i].sendPacket(msg);
			}
            ps = null;
            msg = null;            
		}
	}
    
    /**
     * @param activeChar
     */
    private void showPledgeSkillList(L2PcInstance activeChar)
    {
        L2Clan clan = activeChar.getClan();
        if (clan != null)
        {
            PledgeSkillList response = new PledgeSkillList();
            L2Skill[] skills = clan.getAllSkills();
            
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
    
    /**
     * @param activeChar
     * @return
     */
    private void setPledgeClass(L2PcInstance activeChar)
    {
        int pledgeClass = 0;
        L2Clan clan = activeChar.getClan();
        if (clan != null)
        {
            switch (activeChar.getClan().getLevel())
            {
                case 4:
                    if (activeChar.isClanLeader())
                        pledgeClass = 3;
                    break;
                case 5:
                    if (activeChar.isClanLeader())
                        pledgeClass = 4;
                    else
                        pledgeClass = 2;
                    break;
                case 6:
                    switch (activeChar.getPledgeType())
                    {
                        case -1:
                          pledgeClass = 1;
                          break;
                        case 100:
                        case 200:
                            pledgeClass = 2;
                            break;
                        case 0:
                            if (activeChar.isClanLeader())
                                pledgeClass = 5;
                            else
                                switch (clan.getLeadersSubPledge(activeChar.getName()))
                                {
                                    case 100:
                                    case 200:
                                        pledgeClass = 4;
                                        break;
                                    case -1:
                                        pledgeClass = 3;
                                        break;
                                }
                            break;
                    }
                    break;
                case 7:
                    switch (activeChar.getPledgeType())
                    {
                        case -1:
                          pledgeClass = 1;
                          break;
                        case 100:
                        case 200:
                                pledgeClass = 3;
                            break;
                        case 1001:
                        case 1002:
                        case 2001:
                        case 2002:
                                pledgeClass = 2;
                            break;
                        case 0:
                            if (activeChar.isClanLeader())
                                pledgeClass = 7;
                            else
                                switch (clan.getLeadersSubPledge(activeChar.getName()))
                                {
                                    case 100:
                                    case 200:
                                        pledgeClass = 6;
                                        break;
                                    case 1001:
                                    case 1002:
                                    case 2001:
                                    case 2002:
                                        pledgeClass = 5;
                                        break;
                                    case -1:
                                        pledgeClass = 4;
                                        break;
                                }
                            break;
                    }
                    break;
                case 8:
                    switch (activeChar.getPledgeType())
                    {
                        case -1:
                          pledgeClass = 1;
                          break;
                        case 100:
                        case 200:
                                pledgeClass = 4;
                            break;
                        case 1001:
                        case 1002:
                        case 2001:
                        case 2002:
                                pledgeClass = 3;
                            break;
                        case 0:
                            if (activeChar.isClanLeader())
                                pledgeClass = 8;
                            else
                                switch (clan.getLeadersSubPledge(activeChar.getName()))
                                {
                                    case 100:
                                    case 200:
                                        pledgeClass = 7;
                                        break;
                                    case 1001:
                                    case 1002:
                                    case 2001:
                                    case 2002:
                                        pledgeClass = 6;
                                        break;
                                    case -1:
                                        pledgeClass = 5;
                                        break;
                                }
                            break;
                    }
                    break;
                default:
                    pledgeClass = 1;
                break;
            }
        }
        if (pledgeClass < 5 && activeChar.isNoble())
            pledgeClass = 5;
        else if (activeChar.isHero())
            pledgeClass = 8;
        activeChar.setPledgeClass(pledgeClass);
    }
    
    /* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
}
