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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.PledgeReceiveSubPledgeCreated;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.PledgeSkillListAdd;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.4.2.7 $ $Date: 2005/04/06 16:13:41 $
 */
//TODO rewrite this file in more readable format (shorten the functions etc.)
public class L2Clan
{
    private static final Log _log = LogFactory.getLog(L2Clan.class.getName());
    
    private String _name;
    private int _clanId;
    private L2ClanMember _leader;
    private Map<String, L2ClanMember> _members = new FastMap<String, L2ClanMember>();
    private Map<String, L2ClanMember> _subMembers = new FastMap<String, L2ClanMember>();
    
    private String _allyName;
    private int _allyId;
    private int _level;
    private int _hasCastle;
    private int _hasHideout;
    private boolean _hasCrest;
    private int _hiredGuards;
    private int _crestId;
    private int _crestLargeId;
    private int _allyCrestId;
    private int _auctionBiddedAt;
    
    private ItemContainer _warehouse = new ClanWarehouse(this);
    private List<L2Clan> _atWarWith = new FastList<L2Clan>();
    private List<L2Clan> _underAtackFrom = new FastList<L2Clan>();
    
    private boolean _hasCrestLarge;
    
    private Forum _Forum;
    
    //  Clan Privileges  
    public static final int CP_NOTHING = 0;  
    public static final int CP_CL_JOIN_CLAN = 2; // Join clan  
    public static final int CP_CL_GIVE_TITLE = 4; // Give a title  
    public static final int CP_CL_VIEW_WAREHOUSE = 8; // View warehouse content 
    public static final int CP_CL_MANAGE_RANKS = 16; // View warehouse content 
    public static final int CP_CL_PLEDGE_WAR = 32; // View warehouse content 
    public static final int CP_CL_DISMISS = 64; 
    public static final int CP_CL_REGISTER_CREST = 128; // Register clan crest
    public static final int CP_CL_MASTER_RIGHTS = 256;
    public static final int CP_CL_MANAGE_LEVELS = 512;
    public static final int CP_CH_OPEN_DOOR = 1024; // open a door  
    public static final int CP_CH_OTHER_RIGHTS = 2048; //??
    public static final int CP_CH_AUCTION = 4096;    
    public static final int CP_CH_DISMISS = 8192; //??
    public static final int CP_CH_SET_FUNCTIONS = 16384;
    public static final int CP_CS_OPEN_DOOR = 32768;  
    public static final int CP_CS_MANOR_ADMIN = 65536; //??? 
    public static final int CP_CS_MANAGE_SIEGE = 131072;
    public static final int CP_CS_USE_FUNCTIONS = 262144;
    public static final int CP_CS_DISMISS = 524288; //??? 
    public static final int CP_CS_TAXES =1048576;
    public static final int CP_CS_MERCENARIES =2097152;
    public static final int CP_CS_SET_FUNCTIONS =4194304;
    public static final int CP_ALL = 8388606;  
    
    /** FastMap(Integer, L2Skill) containing all skills of the L2Clan */
    protected final Map<Integer, L2Skill> _Skills = new FastMap<Integer, L2Skill>();
    protected final Map<Integer, RankPrivs> _Privs = new FastMap<Integer, RankPrivs>();
    protected final Map<Integer, SubPledge> _SubPledges = new FastMap<Integer, SubPledge>();
    
    private int _reputationScore = 0;
    private int _rank = 0;
    
    /**
     * called if a clan is referenced only by id.
     * in this case all other data needs to be fetched from db
     * @param clanId
     */
    public L2Clan(int clanId)
    {
        _clanId = clanId;
        restore();
        getWarehouse().restore();
    }
    
    /**
     * this is only called if a new clan is created
     * @param clanId
     * @param clanName
     * @param leader
     */
    public L2Clan(int clanId, String clanName, L2ClanMember leader)
    {
        _clanId = clanId;
        _name = clanName;
        setLeader(leader);
    }
    
    /**
     * @return Returns the clanId.
     */
    public int getClanId()
    {
        return _clanId;
    }
    /**
     * @param clanId The clanId to set.
     */
    public void setClanId(int clanId)
    {
        _clanId = clanId;
    }
    /**
     * @return Returns the leaderId.
     */
    public int getLeaderId()
    {
        return (_leader != null ? _leader.getObjectId() : 0);
    }
    /**
     * @return L2ClanMember of clan leader.
     */
    public L2ClanMember getLeader()
    {
        return _leader;
    }
    /**
     * @param leaderId The leaderId to set.
     */
    public void setLeader(L2ClanMember leader)
    {
        _leader = leader;
        _members.put(leader.getName(), leader);
    }
    public void setNewLeader()
    {
        int maxLevel = 0;
        L2ClanMember newLeader = null;
        for (L2ClanMember member : getMembers())
        {
            if (member.getLevel() > maxLevel)
            {
                maxLevel = member.getLevel();
                newLeader = member;
            }
        }
        if (newLeader != null)
        {
            setLeader(newLeader);
            updateClanInDB();
            
            // Give Lord Crown if Clan has Castle and new Leader is Online
            /*            int CrownId = CrownTable.getCrownId(_hasCastle);
             if(CrownId!=0)
             {
             if(newLeader.isOnline())
             {
             newLeader.getPlayerInstance().getInventory().addItem("Crown",6841,1,newLeader.getPlayerInstance(),null);
             newLeader.getPlayerInstance().getInventory().updateDatabase();
             }
             }*/
            
            broadcastToOnlineMembers(new PledgeStatusChanged(this));
            broadcastToOnlineMembers(SystemMessage.sendString(newLeader.getName() + " is the new clan leader."));
        }
    }
    /**
     * @return Returns the leaderName.
     */
    public String getLeaderName()
    {
        return (_leader!=null ? _leader.getName() : "");
    }
    
    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return _name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        _name = name;
    }
    
    private void addClanMember(L2ClanMember member)
    {
        _members.put(member.getName(), member);
    }
    private void addSubClanMember(L2ClanMember member)
    {
        _subMembers.put(member.getName(), member);
    }
    
    public void addClanMember(L2PcInstance player)
    {
        L2ClanMember member = new L2ClanMember(player);
        // store in db
        storeNewMemberInDatabase(member);
        // store in memory
        if (member.getPledgeType() == 0)
        {
            addClanMember(member);
            //_log.warn("member added to clan");
        }
        else
        {
            addSubClanMember(member);
            //_log.warn("member added to clan");
        }
    }
    
    public void updateClanMember(L2PcInstance player)
    {
        L2ClanMember member = new L2ClanMember(player);
        
        addClanMember(member);
    }
    
    public L2ClanMember getClanMember(String name)
    {
        if (_members.get(name) != null)
            return _members.get(name);
        else if (_subMembers.get(name) != null)
            return _subMembers.get(name);
        else
            return null;
    }
    
    public void removeClan()
    {
        broadcastToOnlineMembers(new SystemMessage(SystemMessage.CLAN_MEMBERSHIP_TERMINATED));
        for (L2ClanMember member : getMembers())
        {
            
            /*            int CrownId = CrownTable.getCrownId(_hasCastle);
             if(CrownId!=0)
             {
             if(member.isOnline()) // Member is Online
             {
             if(_leader.getObjectId()==member.getObjectId())
             {
             if(_leader.isOnline())
             {
             member.getPlayerInstance().getInventory().destroyItem("Crown",6841,1,member.getPlayerInstance(),null);
             member.getPlayerInstance().getInventory().updateDatabase();
             }
             else
             {
             java.sql.Connection con = null;
             try
             {
             con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("delete from items where owner_id = ? and item_id = ?");
             statement.setInt(1, member.getObjectId());
             statement.setInt(2, 6841);
             statement.execute();
             statement.close();
             }
             catch (Exception e) {} 
             finally {try { con.close(); } catch (Exception e) {}}
             }
             }
             member.getPlayerInstance().getInventory().addItem("Crown",CrownId,1,member.getPlayerInstance(),null);
             member.getPlayerInstance().getInventory().updateDatabase();
             }
             else // Member is Offline
             {
             java.sql.Connection con = null;
             try
             {
             con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("delete from items where owner_id = ? and item_id = ?");
             statement.setInt(1, member.getObjectId());
             statement.setInt(2, CrownId);
             statement.execute();
             statement.close();
             }
             catch (Exception e) {} 
             finally {try { con.close(); } catch (Exception e) {}}
             }
             }*/
            
            removeClanMember(member.getName());
        }
        
        _warehouse.destroyAllItems("ClanRemove", getLeader().getPlayerInstance(), null);
        
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
            statement.setInt(1, getClanId());
            statement.execute();
            statement.close();                  
            if (_log.isDebugEnabled()) _log.debug("clan removed in db: "+getClanId());
        }
        catch (Exception e)
        {
            _log.warn("error while removing clan in db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public void removeClanMember(String name)
    {
        L2ClanMember exMember = _members.remove(name);
        if(exMember == null)
        {
            L2ClanMember exSubMember = _members.remove(name);
            if(exSubMember == null)
            {
                _log.warn("Member "+name+" not found in clan while trying to remove");
                return;
            }
            removeMemberInDatabase(exSubMember);
            return;
        }
        if (exMember.getName().equals(getLeaderName()))
        {
            setNewLeader();
        }
        removeMemberInDatabase(exMember);
        if (_members.isEmpty())
        {
            removeClan();
        }
        else
        {
            /*            int CrownId = CrownTable.getCrownId(_hasCastle);
             if(exMember.isOnline()) // Member is Online
             {
             if(_leader.getObjectId()==exMember.getObjectId())
.             {
             if(_leader.isOnline())
             {
             exMember.getPlayerInstance().getInventory().destroyItem("Crown",6841,1,exMember.getPlayerInstance(),null);
             exMember.getPlayerInstance().getInventory().updateDatabase();
             }
             else
             {
             java.sql.Connection con = null;
             try
             {
             con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("delete from items where owner_id = ? and item_id = ?");
             statement.setInt(1, exMember.getObjectId());
             statement.setInt(2, 6841);
             statement.execute();
             statement.close();
             }
             catch (Exception e) {} 
             finally {try { con.close(); } catch (Exception e) {}}
             }
             }
             exMember.getPlayerInstance().getInventory().addItem("Crown",CrownId,1,exMember.getPlayerInstance(),null);
             exMember.getPlayerInstance().getInventory().updateDatabase();
             }
             else // Member is Offline
             {
             java.sql.Connection con = null;
             try
             {
             con = L2DatabaseFactory.getInstance().getConnection();
             PreparedStatement statement = con.prepareStatement("delete from items where owner_id = ? and item_id = ?");
             statement.setInt(1, exMember.getObjectId());
             statement.setInt(2, CrownId);
             statement.execute();
             statement.close();
             }
             catch (Exception e) {} 
             finally {try { con.close(); } catch (Exception e) {}}
             }*/
        }
    }
    
    public L2ClanMember[] getMembers()
    {
        return _members.values().toArray(new L2ClanMember[_members.size()]);
    }
    
    public L2ClanMember[] getSubMembers()
    {
        return _subMembers.values().toArray(new L2ClanMember[_subMembers.size()]);
    }
    
    public int getSubPledgeMembersCount(int pledgeType)
    {
        int count = 0;
        for (L2ClanMember scm : _subMembers.values())
        {
            if (scm.getPledgeType() == pledgeType)
                count++;
        }
        return count;
    }
    
    public int getAllMembersCount()
    {
        return _members.size()+_subMembers.size();
    }
    
    public int getMembersCount()
    {
        return _members.size();
    }
    
    public L2PcInstance[] getOnlineMembers(String exclude)
    {
        List<L2PcInstance> result = new FastList<L2PcInstance>();
        for (L2ClanMember temp : _members.values())
        {
            //L2ClanMember temp = (L2ClanMember) iter.next();
            if (temp.isOnline() && temp.getPlayerInstance()!=null && !temp.getName().equals(exclude))
            {
                result.add(temp.getPlayerInstance());
            }
        }
        for (L2ClanMember temp : _subMembers.values())
        {
            //L2ClanMember temp = (L2ClanMember) iter.next();
            if (temp.isOnline() && temp.getPlayerInstance()!=null && !temp.getName().equals(exclude))
            {
                result.add(temp.getPlayerInstance());
            }
        }
        
        return result.toArray(new L2PcInstance[result.size()]);
        
    }
    
    /**
     * @return
     */
    public int getAllyId()
    {
        return _allyId;
    }
    /**
     * @return
     */
    public String getAllyName()
    {
        return _allyName;
    }
    
    public void setAllyCrestId(int allyCrestId)
    {
        _allyCrestId = allyCrestId;
    }
    
    /**
     * @return
     */
    public int getAllyCrestId()
    {
        return _allyCrestId;
    }
    /**
     * @return
     */
    public int getLevel()
    {
        return _level;
    }
    /**
     * @return
     */
    public int getHasCastle()
    {
        return _hasCastle;
    }
    /**
     * @return
     */
    public int getHasHideout()
    {
        return _hasHideout;
    }
    
    /**
     * @param crestId The id of pledge crest.
     */
    public void setCrestId(int crestId)
    {
        _crestId = crestId;
    }
    
    /**
     * @return Returns the clanCrestId.
     */
    public int getCrestId()
    {
        return _crestId;
    }
    
    /**
     * @param crestLargeId The id of pledge LargeCrest.
     */
    public void setCrestLargeId(int crestLargeId)
    {
        _crestLargeId = crestLargeId;
    }
    
    /**
     * @return Returns the clan CrestLargeId
     */
    public int getCrestLargeId()
    {
        return _crestLargeId;
    }
    
    /**
     * @param allyId The allyId to set.
     */
    public void setAllyId(int allyId)
    {
        _allyId = allyId;
    }
    /**
     * @param allyName The allyName to set.
     */
    public void setAllyName(String allyName)
    {
        _allyName = allyName;
    }
    /**
     * @param hasCastle The hasCastle to set.
     */
    public void setHasCastle(int hasCastle)
    {
        _hasCastle = hasCastle;
    }
    /**
     * @param hasHideout The hasHideout to set.
     */
    public void setHasHideout(int hasHideout)
    {
        _hasHideout = hasHideout;
    }
    /**
     * @param level The level to set.
     */
    public void setLevel(int level)
    {
        _level = level;
        if(_Forum == null)
        {
            if(_level >= 2)
            {
                _Forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot").GetChildByName(_name);
                if(_Forum == null)
                {
                    _Forum = ForumsBBSManager.getInstance().CreateNewForum(_name,ForumsBBSManager.getInstance().getForumByName("ClanRoot"),Forum.CLAN,Forum.CLANMEMBERONLY,getClanId());
                }
            }
        }
    }
    
    /**
     * @param player name
     * @return
     */
    public boolean isMember(String name)
    {
        return (name == null ? false :_members.containsKey(name));
    }
    
    /**
     * @param player name
     * @return
     */
    public boolean isSubMember(String name)
    {
        return (name == null ? false :_subMembers.containsKey(name));
    }
    
    public void updateClanInDB()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=? WHERE clan_id=?");
            statement.setInt(1, getLeaderId());
            statement.setInt(2, getAllyId());
            statement.setString(3, getAllyName());
            statement.setInt(4, getReputationScore());
            statement.setInt(5, getClanId());
            statement.execute();
            statement.close();
            if (_log.isDebugEnabled()) _log.debug("New clan leader saved in db: "+getClanId());
        }
        catch (Exception e)
        {
            _log.warn("error while saving new clan leader to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }   
    
    public void store()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,hasHideout,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id) values (?,?,?,?,?,?,?,?,?,?,?)");
            statement.setInt(1, getClanId());
            statement.setString(2, getName());
            statement.setInt(3, getLevel());
            statement.setInt(4, getHasCastle());
            statement.setInt(5, getHasHideout());
            statement.setInt(6, getAllyId());
            statement.setString(7, getAllyName());
            statement.setInt(8, getLeaderId());
            statement.setInt(9, getCrestId());
            statement.setInt(10,getCrestLargeId());
            statement.setInt(11,getAllyCrestId());
            statement.execute();
            statement.close();
            
            statement = con.prepareStatement("UPDATE characters SET clanid=? WHERE obj_Id=?");
            statement.setInt(1, getClanId());
            statement.setInt(2, getLeaderId());
            statement.execute();
            statement.close();                  
            
            if (_log.isDebugEnabled()) _log.debug("New clan saved in db: "+getClanId());
        }
        catch (Exception e)
        {
            _log.warn("error while saving new clan to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }   
    
    private void storeNewMemberInDatabase(L2ClanMember member)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=? WHERE obj_Id=?");
            statement.setInt(1, getClanId());
            statement.setInt(2, member.getObjectId());
            statement.execute();
            statement.close();                  
            /*if (_log.isDebugEnabled())*/ _log.debug("New clan member saved in db: "+getClanId());
        }
        catch (Exception e)
        {
            _log.warn("error while saving new clan member to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void removeMemberInDatabase(L2ClanMember member)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=0, allyId=0, title=?, clan_privs=0, wantspeace=0, pledge_rank=0, pledge_type=0, apprentice=? WHERE obj_Id=?");
            statement.setString(1, "");
            statement.setString(2, "");
            statement.setInt(3, member.getObjectId());
            statement.execute();
            statement.close();                  
            if (_log.isDebugEnabled()) _log.debug("clan member removed in db: "+getClanId());
        }
        catch (Exception e)
        {
            _log.warn("error while removing clan member in db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    
    @SuppressWarnings("unused")
    private void UpdateWarsInDB()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("UPDATE clan_wars SET wantspeace1=? WHERE clan1=?");
            statement.setInt(1, 0);
            statement.setInt(2, 0);
        }
        catch (Exception e)
        {
            _log.warn("could not update clans wars data:" + e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }
    }
    
    @SuppressWarnings("unused") //Moved to Clan Table
    private void restorewars()
    {
        java.sql.Connection con = null;
        
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("SELECT clan1, clan2, wantspeace1, wantspeace2 FROM clan_wars");
            ResultSet rset = statement.executeQuery();
            while(rset.next())
            {
                if(rset.getInt("clan1") == this._clanId) this.setEnemyClan(rset.getInt("clan2"));
                if(rset.getInt("clan2") == this._clanId) this.setAtackerClan(rset.getInt("clan1"));
            }
            statement.close();
        }
        catch (Exception e)
        {
            _log.warn("could not restore clan wars data:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void restoreSkills()
    {
        java.sql.Connection con = null;
        
        try
        {
            // Retrieve all skills of this L2PcInstance from the database
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
            statement.setInt(1, getClanId());
            if ( _log.isDebugEnabled())_log.debug("ClanId : "+getClanId());
            ResultSet rset = statement.executeQuery();
            
            // Go though the recordset of this SQL query
            while (rset.next())
            {
                int id = rset.getInt("skill_id");
                int level = rset.getInt("skill_level");
                // Create a L2Skill object for each record
                L2Skill skill = SkillTable.getInstance().getInfo(id, level);
                // Add the L2Skill object to the L2Clan _skills
                _Skills.put(skill.getId(), skill);
            }
            
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warn("Could not restore clan skills: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    /** used to retrieve all skills */
    public final L2Skill[] getAllSkills()
    {
        if (_Skills == null)
            return new L2Skill[0];
        
        return _Skills.values().toArray(new L2Skill[_Skills.values().size()]);
    }
    
    
    /** used to add a skill to skill list of this L2Clan */
    public L2Skill addSkill(L2Skill newSkill)
    {
        L2Skill oldSkill    = null;
        
        if (newSkill != null)
        {
            // Replace oldSkill by newSkill or Add the newSkill
            oldSkill = _Skills.put(newSkill.getId(), newSkill);
        }
        
        return oldSkill;
    }
    
    /** used to add a new skill to the list, send a packet to all online clan members, update their stats and store it in db*/
    public L2Skill addNewSkill(L2Skill newSkill)
    {
        L2Skill oldSkill    = null;
        java.sql.Connection con = null;
        
        if (newSkill != null)
        {
            
            // Replace oldSkill by newSkill or Add the newSkill
            oldSkill = _Skills.put(newSkill.getId(), newSkill);
            
            
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement;
                
                if (oldSkill != null)
                {
                    statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
                    statement.setInt(1, newSkill.getLevel());
                    statement.setInt(2, oldSkill.getId());
                    statement.setInt(3, getClanId());
                    statement.execute();
                    statement.close();
                }
                else
                {
                    statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name) VALUES (?,?,?,?)");
                    statement.setInt(1, getClanId());
                    statement.setInt(2, newSkill.getId());
                    statement.setInt(3, newSkill.getLevel());
                    statement.setString(4, newSkill.getName());
                    statement.execute();
                    statement.close();
                }
            }
            catch (Exception e)
            {
                _log.warn("Error could not store char skills: " + e);
            }
            finally
            {
                try { con.close(); } catch (Exception e) {}
            }
            
                
            for (L2ClanMember temp : _members.values())
            {
                //TODO apply skill changes according to members class( in ex. don't add Clan Agillity skill's effect to lower class then Baron)
                if (temp.isOnline() && temp.getPlayerInstance()!=null)
                {
                    if (newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
                    {
                        temp.getPlayerInstance().addSkill(newSkill);
                        temp.getPlayerInstance().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
                    }
                }
            }
            for (L2ClanMember temp : _subMembers.values())
            {
                //TODO apply skill changes according to members class( in ex. don't add Clan Agillity skill's effect to lower class then Baron)
                if (temp.isOnline() && temp.getPlayerInstance()!=null)
                {
                    if (newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
                        temp.getPlayerInstance().addSkill(newSkill);
                }
            }
        }
        
        return oldSkill;
    }
    
    
    public void addSkillEffects()
    {
        for(L2Skill skill : _Skills.values())
        {
            for (L2ClanMember temp : _members.values())
            {
                if (temp.isOnline() && temp.getPlayerInstance()!=null)
                {
                    //TODO add skills according to members class( in ex. don't add Clan Agillity skill's effect to lower class then Baron)
                    if (skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
                        temp.getPlayerInstance().addSkill(skill);
                }
            }
            for (L2ClanMember temp : _subMembers.values())
            {
                if (temp.isOnline() && temp.getPlayerInstance()!=null)
                {
                    //TODO add skills according to members class( in ex. don't add Clan Agillity skill's effect to lower class then Baron)
                    if (skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
                        temp.getPlayerInstance().addSkill(skill);
                }
            }
        }
    }
    
    public void addSkillEffects(L2PcInstance cm)
    {
        if (cm == null)
            return;
        
        for(L2Skill skill : _Skills.values())
        {
            //TODO add skills according to members class( in ex. don't add Clan Agillity skill's effect to lower class then Baron)
            if (skill.getMinPledgeClass() <= cm.getPledgeClass())
                cm.addSkill(skill);
        }
    }
    
    private void restoreSubPledges()
    {
        java.sql.Connection con = null;
        
        try
        {
            // Retrieve all skills of this L2PcInstance from the database
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT sub_pledge_id,name,leader_name FROM clan_subpledges WHERE clan_id=?");
            statement.setInt(1, getClanId());
            if ( _log.isDebugEnabled())_log.debug("subPledge restore for ClanId : "+getClanId());
            ResultSet rset = statement.executeQuery();
            
            // Go though the recordset of this SQL query
            while (rset.next())
            {
                int id = rset.getInt("sub_pledge_id");
                String name = rset.getString("name");
                String leaderName = rset.getString("leader_name");
                    // Create a SubPledge object for each record
                    SubPledge pledge = new SubPledge(id, name, leaderName);
                    _SubPledges.put(id, pledge);                    
            }
            
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warn("Could not restore clan Sub pledges: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    /** used to retrieve all subPledges */
    public final SubPledge[] getAllSubPledges()
    {
        if (_SubPledges == null)
            return new SubPledge[0];
        
        return _SubPledges.values().toArray(new SubPledge[_SubPledges.values().size()]);
    }
    
    private void restoreRankPrivs()
    {
        java.sql.Connection con = null;
        
        try
        {
            // Retrieve all skills of this L2PcInstance from the database
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT privilleges,rank,party FROM clan_privs WHERE clan_id=?");
            statement.setInt(1, getClanId());
            if (_log.isDebugEnabled())_log.debug("clanPrivs restore for ClanId : "+getClanId());
            ResultSet rset = statement.executeQuery();
            
            // Go though the recordset of this SQL query
            while (rset.next())
            {
                int rank = rset.getInt("rank");
                int party = rset.getInt("party");
                int privileges = rset.getInt("privilleges");
                    // Create a SubPledge object for each record
                    RankPrivs privs = new RankPrivs(rank, party, privileges);
                    _Privs.put(rank, privs);                    
            }
            
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warn("Could not restore clan privs by rank: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public int getRankPrivs(int rank)
    {
        if (_Privs.get(rank) != null)
            return _Privs.get(rank).getPrivs();
        else
            return 0;
    }
    public void setRankPrivs(int rank, int privs)
    {
        if (_Privs.get(rank)!= null)
        {
            _Privs.get(rank).setPrivs(privs);
            
            
            java.sql.Connection con = null;
            
            try
            {
                if ( _log.isDebugEnabled() )_log.debug("requested store clan privs in db for rank: "+rank+", privs: "+privs);
                // Retrieve all skills of this L2PcInstance from the database
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("UPDATE clan_privs SET privilleges=? WHERE clan_id=? AND rank=? AND party=?");
                statement.setInt(1, privs);
                statement.setInt(2, getClanId());
                statement.setInt(3, rank);
                statement.setInt(4, 0);
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                _log.warn("Could not store clan privs for rank: " + e);
            }
            finally
            {
                try { con.close(); } catch (Exception e) {}
            }
            for (L2ClanMember cm : getMembers())
            {
                if (cm.isOnline())
                    if (cm.getRank() == rank)
                        if (cm.getPlayerInstance() != null)
                        {
                            cm.getPlayerInstance().setClanPrivileges(privs);
                            cm.getPlayerInstance().sendPacket(new UserInfo(cm.getPlayerInstance()));
                        }
            }
        }
        else
        {
            _Privs.put(rank, new RankPrivs(rank, 0, privs));
            
            java.sql.Connection con = null;
            
            try
            {
                if ( _log.isDebugEnabled() )_log.warn("requested store clan new privs in db for rank: "+rank);
                // Retrieve all skills of this L2PcInstance from the database
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privilleges) VALUES (?,?,?,?)");
                statement.setInt(1, getClanId());
                statement.setInt(2, rank);
                statement.setInt(3, 0);
                statement.setInt(4, privs);
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                _log.warn("Could not create new rank and store clan privs for rank: " + e);
            }
            finally
            {
                try { con.close(); } catch (Exception e) {}
            }
        }
    }
    
    /** used to retrieve all subPledges */
    public final RankPrivs[] getAllRankPrivs()
    {
        if (_Privs == null)
            return new RankPrivs[0];
        
        return _Privs.values().toArray(new RankPrivs[_Privs.values().size()]);
    }
    
    private void restore()
    {
        //restorewars();
        java.sql.Connection con = null;
        try
        {
            L2ClanMember member;
            
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT clan_name,clan_level,hasCastle,hasHideout,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,reputation_score,rank, auction_bid_at FROM clan_data where clan_id=?");
            statement.setInt(1, getClanId());
            ResultSet clanData = statement.executeQuery();
            
            if (clanData.next())
            {
                setName(clanData.getString("clan_name"));
                setLevel(clanData.getInt("clan_level"));
                setHasCastle(clanData.getInt("hasCastle"));
                setHasHideout(clanData.getInt("hasHideout"));
                setAllyId(clanData.getInt("ally_id"));
                setAllyName(clanData.getString("ally_name"));
                
                setCrestId(clanData.getInt("crest_id"));
                if (getCrestId() != 0)
                {
                    setHasCrest(true);
                }
                
                setCrestLargeId(clanData.getInt("crest_large_id"));
                if (getCrestLargeId() != 0)
                {
                    setHasCrestLarge(true);
                }
                
                setAllyCrestId(clanData.getInt("ally_crest_id"));
                
                setReputationScore(clanData.getInt("reputation_score"));
                setRank(clanData.getInt("rank"));
                setAuctionBiddedAt(clanData.getInt("auction_bid_at"), false);
                
                int leaderId = (clanData.getInt("leader_id"));          
                
                PreparedStatement statement2 = con.prepareStatement("SELECT char_name,level,classid,obj_Id,pledge_type,pledge_rank,apprentice FROM characters WHERE clanid=?");
                statement2.setInt(1, getClanId());
                ResultSet clanMembers = statement2.executeQuery();
                
                while (clanMembers.next())
                {
                    member = new L2ClanMember(clanMembers.getString("char_name"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("obj_id"), clanMembers.getInt("pledge_type"), clanMembers.getInt("pledge_rank"), clanMembers.getString("apprentice"));
                    if (member.getObjectId() == leaderId)
                    {
                        setLeader(member);
                    }
                    else
                    {
                        if (member.getPledgeType() == 0)
                            addClanMember(member);
                        else
                            addSubClanMember(member);
                    }                   
                }               
                clanMembers.close();
                statement2.close();              
            }
            
            clanData.close();
            statement.close();
            
            if (getName() != null)
                _log.info("Restored clan data for \"" + getName() + "\" from database.");
            //restorewars();
            restoreSkills();
            restoreSubPledges();
            restoreRankPrivs();
        }
        catch (Exception e)
        {
            _log.warn("error while restoring clan "+e,e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public void broadcastToOnlineAllyMembers(ServerBasePacket packet)
    {
        if (getAllyId()==0)
            return;
        for (L2Clan clan : ClanTable.getInstance().getClans()){
            if (clan.getAllyId() == this.getAllyId())
                clan.broadcastToOnlineMembers(packet);
        }
    }
    
    public void broadcastToOnlineMembers(ServerBasePacket packet)
    {
        for (L2ClanMember member : _members.values())
        {
            if (member.isOnline() && member.getPlayerInstance() != null)
            {
                member.getPlayerInstance().sendPacket(packet);
            }
        }
        for (L2ClanMember member : _subMembers.values())
        {
            if (member.isOnline() && member.getPlayerInstance() != null)
            {
                member.getPlayerInstance().sendPacket(packet);
            }
        }
    }
    
    public void broadcastToOtherOnlineMembers(ServerBasePacket packet, L2PcInstance player)
    {
        for (L2ClanMember member : _members.values())
        {
            if (member.isOnline() && member.getPlayerInstance() != null && member.getPlayerInstance() != player)
            {
                member.getPlayerInstance().sendPacket(packet);
            }
        }
        for (L2ClanMember member : _subMembers.values())
        {
            if (member.isOnline() && member.getPlayerInstance() != null && member.getPlayerInstance() != player)
            {
                member.getPlayerInstance().sendPacket(packet);
            }
        }
    }
    
    public String toString()
    {
        return getName();
    }
    
    /**
     * @return
     */
    public boolean hasCrest()
    {
        return _hasCrest;
    }
    
    public boolean hasCrestLarge()
    {
        return _hasCrestLarge;
    }
    
    public void setHasCrest(boolean flag)
    {
        _hasCrest = flag;
    }
    
    public void setHasCrestLarge(boolean flag)
    {
        _hasCrestLarge = flag;
    }
    
    public ItemContainer getWarehouse()
    {
        return _warehouse;
    }
    public boolean isAtWarWith(Integer id)
    {
        L2Clan clan = ClanTable.getInstance().getClan(id);
        if ((_atWarWith != null)&&(_atWarWith.size() > 0))
            if (_atWarWith.contains(clan)) return true;
        return false;       
    }
    public void setEnemyClan(L2Clan clan)
    {
        //Integer id = clan.getClanId();
        _atWarWith.add(clan);
    }
    public void setEnemyClan(Integer clan)
    {
        if (_log.isDebugEnabled() )_log.debug("setEnemyClan");
        L2Clan Clan = ClanTable.getInstance().getClan(clan);
        _atWarWith.add(Clan);
    }
    public void deleteEnemyClan(L2Clan clan)
    {
        //Integer id = clan.getClanId();
        _atWarWith.remove(clan);
    }
    
    // clans that are atacking this clan
    public void setAtackerClan(L2Clan clan)
    {
        //int id = clan.getClanId();
        _underAtackFrom.add(clan);
    }
    public void setAtackerClan(Integer clan)
    {
        L2Clan Clan = ClanTable.getInstance().getClan(clan);
        _underAtackFrom.add(Clan);
        if ( _log.isDebugEnabled() )_log.debug("setAtackerClan");
    }
    public void deleteAtackerClan(L2Clan clan)
    {
        //Integer id = clan.getClanId();
        _underAtackFrom.remove(clan);
    }
    public boolean isAttackedBy(int id)
    {
        L2Clan clan = ClanTable.getInstance().getClan(id);
        if ((_underAtackFrom != null)&&(_underAtackFrom.size() > 0))
            if (_underAtackFrom.contains(clan)) return true;
        return false; 
    }
    public List<L2Clan> getEnemyClans()
    {
        return _atWarWith;
    }
    public List<L2Clan> getAtackerClans()
    {
        return _underAtackFrom;
    }
    
    public int getHiredGuards(){ return _hiredGuards; }
    public void incrementHiredGuards(){ _hiredGuards++; }
    
    public int isAtWar()
    {
        if ((_atWarWith != null)&&(_atWarWith.size() > 0))
            return 1;
        return 0;       
    }
    
    public int createSubPledge(int pledgeType, String leaderName, String subPledgeName)
    {
        pledgeType = getAvailablePledgeTypes(pledgeType);
        if (pledgeType == 0)
        {
            return 0;
        }
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_name) values (?,?,?,?)");
            statement.setInt(1, getClanId());
            statement.setInt(2, pledgeType);
            statement.setString(3, subPledgeName);
            if (pledgeType != -1)
                statement.setString(4, leaderName);
            else
                statement.setString(4, "");
            statement.execute();
            statement.close();
            
            _SubPledges.put(pledgeType, new SubPledge(pledgeType, subPledgeName, leaderName));
            
            if (_log.isDebugEnabled()) _log.debug("New sub_clan saved in db: "+getClanId()+"; "+pledgeType);
        }
        catch (Exception e)
        {
            _log.warn("error while saving new sub_clan to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
        broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(new SubPledge(pledgeType, subPledgeName, leaderName)));
        return 1;
    }
    public int getAvailablePledgeTypes(int pledgeType)
    {
        /*SubPledge[] currentSubPledges = getAllSubPledges();
        for (int i = 0; i<currentSubPledges.length; i++)
        {*/
            if (_SubPledges.get(pledgeType) != null)
            {
                if (_log.isDebugEnabled())_log.debug("found Sub pledge with id: "+pledgeType);
                switch(pledgeType)
                {
                    case -1:
                        return 0;
                    case 100:
                        pledgeType = getAvailablePledgeTypes(200);
                        break;
                    case 200:
                        return 0;
                    case 1001:
                        pledgeType = getAvailablePledgeTypes(1002);
                        break;
                    case 1002:
                        pledgeType = getAvailablePledgeTypes(2001);
                        break;
                    case 2001:
                        pledgeType = getAvailablePledgeTypes(2002);
                        break;
                    case 2002:
                        return 0;
                }
            }
        //}
            //_log.warn("gonnna return pledgeTypeOf: "+pledgeType);
        return pledgeType;
    }
    
    public void broadcastClanStatus()
    {
        for(L2PcInstance member: this.getOnlineMembers(""))
        {
            PledgeShowMemberListAll pm = new PledgeShowMemberListAll(this, member);
            member.sendPacket(pm);
        }
    }
    
    public void setReputationScore(int score)
    {
        if (_reputationScore != 0)
        {
            _reputationScore = score;
            updateClanInDB();
        }
        _reputationScore = score;
        broadcastClanStatus();
    }
    public int getReputationScore()
    {
        return _reputationScore;
    }
    public void setRank(int rank)
    {
        _rank = rank;
    }
    public int getRank()
    {
        return _rank;
    }
    
    public void updatePrivsForRank(int rank)
    {
        for (L2ClanMember member : _members.values())
        {
            if (member.isOnline() && member.getPlayerInstance() != null && member.getPlayerInstance().getRank()==rank)
            {
                member.getPlayerInstance().setClanPrivileges(getRankPrivs(rank));
                member.getPlayerInstance().sendPacket(new UserInfo(member.getPlayerInstance()));
            }
        }
        for (L2ClanMember member : _subMembers.values())
        {
            if (member.isOnline() && member.getPlayerInstance() != null && member.getPlayerInstance().getRank()==rank)
            {
                member.getPlayerInstance().setClanPrivileges(getRankPrivs(rank));
                member.getPlayerInstance().sendPacket(new UserInfo(member.getPlayerInstance()));
            }
        }
    }
    
    public int getLeadersSubPledge(String name)
    {
        int id = -1;
        for (SubPledge sp : _SubPledges.values())
        {
            if (sp.getLeaderName() == name)
                id = sp.getId();
        }
        return id;
    }
    
    public int getAuctionBiddedAt()
    {
        return _auctionBiddedAt;
    }
    
    public void setAuctionBiddedAt(int id, boolean storeInDb)
    {
        _auctionBiddedAt = id;
        
        if(storeInDb)
        {
           java.sql.Connection con = null;
           try
           {
               con = L2DatabaseFactory.getInstance().getConnection();
               PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?");
               statement.setInt(1, id);
               statement.setInt(2, getClanId());
               statement.execute();
               statement.close();
           }
           catch (Exception e)
           {
               _log.warn("Could not store auction for clan: " + e);
           }
           finally
           {
               try { con.close(); } catch (Exception e) {}
           }        
        }
    } 
    
    public class SubPledge
    {
       private int _Id;
       private String _Name;
       private String _LeaderName;
       
       public SubPledge(int id, String name, String leaderName)
       {
           _Id = id;
           _Name = name;
           _LeaderName = leaderName;
       }
       
       public int getId()
       {
           return _Id;
       }
       public String getName()
       {
           return _Name;
       }
       public String getLeaderName()
       {
           return _LeaderName;
       }
       
       public void setLeaderName(String leaderName)
       {
           _LeaderName = leaderName;
       }
    }
    public class RankPrivs
    {
       @SuppressWarnings("hiding")
       private int _rank;
       private int _party;// TODO find out what this stuff means and implement it
       private int _privs;
       
       public RankPrivs(int rank, int party, int privs)
       {
           _rank = rank;
           _party = party;
           _privs = privs;
       }
       
       public int getRank()
       {
           return _rank;
       }
       public int getParty()
       {
           return _party;
       }
       public int getPrivs()
       {
           return _privs;
       }
       public void setPrivs(int privs)
       {
           _privs = privs;
       }
    }
}
