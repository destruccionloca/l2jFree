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

/**
 * 
 * @author FBIagent / fixed SqueezeD
 * 
 */

package net.sf.l2j.gameserver.model.entity.events;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.Vector;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TvT
{   
    private final static Log _log = LogFactory.getLog(TvT.class.getName());    
    public static String _eventName = new String(),
                         _eventDesc = new String(),
                         _topTeam = new String(),
                         _joiningLocationName = new String();
    public static Vector<String> _teams = new Vector<String>(),
                                 _savePlayers = new Vector<String>(),
                                 _savePlayerTeams = new Vector<String>();
    public static Vector<L2PcInstance> _players = new Vector<L2PcInstance>(),
                                       _playersShuffle = new Vector<L2PcInstance>();
    public static Vector<Integer> _teamPlayersCount = new Vector<Integer>(),
                                  _teamKillsCount = new Vector<Integer>(),
                                  _teamColors = new Vector<Integer>(),
                                  _teamsX = new Vector<Integer>(),
                                  _teamsY = new Vector<Integer>(),
                                  _teamsZ = new Vector<Integer>();
    public static boolean _joining = false,
                          _teleport = false,
                          _started = false,
                          _sitForced = false;
    public static L2Spawn _npcSpawn;
    public static int _npcId = 0,
                      _npcX = 0,
                      _npcY = 0,
                      _npcZ = 0,
                      _rewardId = 0,
                      _rewardAmount = 0,
                      _topKills = 0,
                      _minlvl = 0,
                      _maxlvl = 0;

    public static void setNpcPos(L2PcInstance activeChar)
    {
        _npcX = activeChar.getX();
        _npcY = activeChar.getY();
        _npcZ = activeChar.getZ();
    }
    
    public static void addTeam(String teamName)
    {
        if (!checkTeamOk())
        {
            if (_log.isDebugEnabled())_log.debug("TvT Engine[addTeam(" + teamName + ")]: checkTeamOk() = false");
            return;
        }
        
        if (teamName.equals(" "))
            return;

        _teams.add(teamName);
        _teamPlayersCount.add(0);
        _teamKillsCount.add(0);
        _teamColors.add(0);
        _teamsX.add(0);
        _teamsY.add(0);
        _teamsZ.add(0);
    }
    
    public static boolean checkMaxLevel(int maxlvl)
    {
        if (_minlvl >= maxlvl)
            return false;
        
        return true;
    }
    
    public static boolean checkMinLevel(int minlvl)
    {
        if (_maxlvl <= minlvl)
            return false;
        
        return true;
    }
    
    public static void removeTeam(String teamName)
    {
        if (!checkTeamOk() || _teams.isEmpty())
        {
            if (_log.isDebugEnabled())_log.debug("TvT Engine[removeTeam(" + teamName + ")]: checkTeamOk() = false");
            return;
        }
        
        if (teamPlayersCount(teamName) > 0)
        {
            if (_log.isDebugEnabled())_log.debug("TvT Engine[removeTeam(" + teamName + ")]: teamPlayersCount(teamName) > 0");
            return;
        }
        
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;

        _teamsZ.remove(index);
        _teamsY.remove(index);
        _teamsX.remove(index);
        _teamColors.remove(index);
        _teamKillsCount.remove(index);
        _teamPlayersCount.remove(index);
        _teams.remove(index);
    }
    
    public static void setTeamPos(String teamName, L2PcInstance activeChar)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;
        
        _teamsX.set(index, activeChar.getX());
        _teamsY.set(index, activeChar.getY());
        _teamsZ.set(index, activeChar.getZ());
    }
    
    public static void setTeamColor(String teamName, int color)
    {
        if (!checkTeamOk())
            return;

        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;

        _teamColors.set(index, color);
    }
    
    public static boolean checkTeamOk()
    {
        if (_started || _teleport || _joining)
            return false;
        
        return true;
    }
    
    public static void startJoin(L2PcInstance activeChar)
    {
        if (!startJoinOk())
        {
            if (_log.isDebugEnabled())_log.debug("TvT Engine[startJoin(" + activeChar.getName() + ")]: startJoinOk() = false");
            return;
        }
        
        _joining = true;
        spawnEventNpc(activeChar);
        Announcements.getInstance().announceToAll(_eventName + "(TvT): Joinable in " + _joiningLocationName + "!");
    }
    
    private static boolean startJoinOk()
    {
        if (_started || _teleport || _joining || _teams.size() < 2 || _eventName.equals("") ||
            _joiningLocationName.equals("") || _eventDesc.equals("") || _npcId == 0 ||
            _npcX == 0 || _npcY == 0 || _npcZ == 0 || _rewardId == 0 || _rewardAmount == 0 ||
            _teamsX.contains(0) || _teamsY.contains(0) || _teamsZ.contains(0))
            return false;
        
        return true;
    }
    
    private static void spawnEventNpc(L2PcInstance activeChar)
    {
        L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(_npcId);

        try
        {
            _npcSpawn = new L2Spawn(tmpl);

            _npcSpawn.setLocx(_npcX);
            _npcSpawn.setLocy(_npcY);
            _npcSpawn.setLocz(_npcZ);
            _npcSpawn.setAmount(1);
            _npcSpawn.setHeading(activeChar.getHeading());
            _npcSpawn.setRespawnDelay(1);

            SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);

            _npcSpawn.init();
            _npcSpawn.getLastSpawn().setCurrentHp(999999999);
            _npcSpawn.getLastSpawn().setTitle(_eventName);
            _npcSpawn.getLastSpawn()._isEventMobTvT = true;
            _npcSpawn.getLastSpawn().isAggressive();
            _npcSpawn.getLastSpawn().decayMe();
            _npcSpawn.getLastSpawn().spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());

            _npcSpawn.getLastSpawn().broadcastPacket(new MagicSkillUser(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
        }
        catch (Exception e)
        {
            _log.error("TvT Engine[spawnEventNpc(" + activeChar.getName() + ")]: exception: " + e.getMessage());
        }
    }
    
    public static void teleportStart()
    {
        if (!_joining || _started || _teleport)
            return;
        
        _joining = false;
        Announcements.getInstance().announceToAll(_eventName + "(TvT): Teleport to team spot in 20 seconds!");

        if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
            shuffleTeams();
        
        setUserData();
        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
                                                       {
                                                           public void run()
                                                           {
                                                               TvT.sit();
                                                               
                                                               for (L2PcInstance player : TvT._players)
                                                               {
                                                                   if (player !=  null)
                                                                       player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)), _teamsY.get(_teams.indexOf(player._teamNameTvT)), _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
                                                               }
                                                           }
                                                       }, 20000);
        _teleport = true;
    }
    
    public static void startEvent(L2PcInstance activeChar)
    {
        if (!startEventOk())
        {
            if (_log.isDebugEnabled())_log.debug("TvT Engine[startEvent(" + activeChar.getName() + ")]: startEventOk() = false");
            return;
        }
        
        _teleport = false;
        sit();
        Announcements.getInstance().announceToAll(_eventName + "(TvT): Started. Go to kill your enemies!");
        _started = true;
    }
    
    private static boolean startEventOk()
    {
        if (_joining || !_teleport || _started)
            return false;
        
        if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
        {
            if (_teamPlayersCount.contains(0))
                return false;
        }
        else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
        {
            Vector<L2PcInstance> playersShuffleTemp = new Vector<L2PcInstance>();
            int loopCount = 0;
            
            loopCount = _playersShuffle.size();

            for (int i=0;i<loopCount;i++)
            {
                if (_playersShuffle != null)
                    playersShuffleTemp.add(_playersShuffle.get(i));
            }
            
            _playersShuffle = playersShuffleTemp; 
            playersShuffleTemp.clear();
            
          //  if (_playersShuffle.size() < (_teams.size()*2)){
          //      return false;
          //  }
          }
        
        return true;
    }
    
    public static void shuffleTeams()
    {
        int teamCount = 0,
            playersCount = 0;

        for (;;)
        {
            if (_playersShuffle.isEmpty())
                break;

            int playerToAddIndex = new Random().nextInt(_playersShuffle.size());
            
            _players.add(_playersShuffle.get(playerToAddIndex));
            _players.get(playersCount)._teamNameTvT = _teams.get(teamCount);
            _savePlayers.add(_players.get(playersCount).getName());
            _savePlayerTeams.add(_teams.get(teamCount));
            playersCount++;

            if (teamCount == _teams.size()-1)
                teamCount = 0;
            else
                teamCount++;
            
            _playersShuffle.remove(playerToAddIndex);
        }
    }
    
    public static void setUserData()
    {
        for (L2PcInstance player : _players)
        {
            player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameTvT)));
            player.setKarma(0);
            player.broadcastUserInfo();
        }
    }
    
    public static void finishEvent(L2PcInstance activeChar)
    {
        if (!finishEventOk())
        {
            if (_log.isDebugEnabled())_log.debug("TvT Engine[finishEvent(" + activeChar.getName() + ")]: finishEventOk() = false");
            return;
        }

        _started = false;
        unspawnEventNpc();
        processTopTeam();

        if (_topKills == 0)
            Announcements.getInstance().announceToAll(_eventName + "(TvT): No team win the match(nobody killed).");
        else
        {
            Announcements.getInstance().announceToAll(_eventName + "(TvT): " + _topTeam + "'s win the match! " + _topKills + " kills.");
            rewardTeam(activeChar, _topTeam);
        }
        
        teleportFinish();
    }
    
    private static boolean finishEventOk()
    {
        if (!_started)
            return false;
        
        return true;
    }
    
    public static void processTopTeam()
    {
        for (String team : _teams)
        {
            if (teamKillsCount(team) > _topKills)
            {
                _topTeam = team;
                _topKills = teamKillsCount(team);
            }
        }
    }
    
    public static void rewardTeam(L2PcInstance activeChar, String teamName)
    {
        for (L2PcInstance player : _players)
        {
            if (player != null)
            {
                if (player._teamNameTvT.equals(teamName) && player._countTvTkills>0)
                {
                    PcInventory inv = player.getInventory();
                
                    if (ItemTable.getInstance().createDummyItem(_rewardId).isStackable())
                        inv.addItem("TvT Event: " + _eventName, _rewardId, _rewardAmount, player, activeChar.getTarget());
                    else
                    {
                        for (int i=0;i<=_rewardAmount-1;i++)
                            inv.addItem("TvT Event: " + _eventName, _rewardId, 1, player, activeChar.getTarget());
                    }
                
                    SystemMessage sm;

                    if (_rewardAmount > 1)
                    {
                        sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
                        sm.addItemName(_rewardId);
                        sm.addNumber(_rewardAmount);
                        player.sendPacket(sm);
                    }
                    else
                    {
                        sm = new SystemMessage(SystemMessage.EARNED_ITEM);
                        sm.addItemName(_rewardId);
                        player.sendPacket(sm);
                    }
                
                    StatusUpdate su = new StatusUpdate(player.getObjectId());
                    su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
                    player.sendPacket(su);

                    NpcHtmlMessage nhm = new NpcHtmlMessage(5);
                    TextBuilder replyMSG = new TextBuilder("");

                    replyMSG.append("<html><head><body>Your team win the event. Look in your inventar there should be the reward.</body></html>");

                    nhm.setHtml(replyMSG.toString());
                    player.sendPacket(nhm);
                }
            }
        }
    }
    
    public static void abortEvent()
    {
        if (!_joining && !_teleport && !_started)
            return;
        
        _joining = false;
        _teleport = false;
        _started = false;
        unspawnEventNpc();
        Announcements.getInstance().announceToAll(_eventName + "(TvT): Match aborted!");
        teleportFinish();
    }
    
    public static void sit()
    {
        if (_sitForced)
            _sitForced = false;
        else
            _sitForced = true;
        
        for (L2PcInstance player : _players)
        {
            if (player != null)
            {
                if (_sitForced)
                {
                    player.stopMove(null, false);
                    player.abortAttack();
                    player.abortCast();
                    
                    if (!player.isSitting())
                        player.sitDown();
                }
                else
                {
                    if (player.isSitting())
                        player.standUp();
                }
            }
        }
        for (L2PcInstance player : _players)
        {
            //Remove Buffs
            for (L2Effect e : player.getAllEffects())
                e.exit();
            
            //Remove Summon's buffs
            if (player.getPet() != null)
            {
                L2Summon summon = player.getPet();
                for (L2Effect e : summon.getAllEffects())
                    e.exit();
                
                if (summon instanceof L2PetInstance)
                    summon.unSummon(player);
            }
            
            //Remove player from his party
            if (player.getParty() != null)
            {
                L2Party party = player.getParty();
                party.removePartyMember(player);
            }
        }
    }
    
    public static void dumpData()
    {
        System.out.println("");
        System.out.println("");
        
        if (!_joining && !_teleport && !_started)
        {
            System.out.println("<<---------------------------------->>");
            System.out.println(">> TvT Engine infos dump (INACTIVE) <<");
            System.out.println("<<--^----^^-----^----^^------^^----->>");
        }
        else if (_joining && !_teleport && !_started)
        {
            System.out.println("<<--------------------------------->>");
            System.out.println(">> TvT Engine infos dump (JOINING) <<");
            System.out.println("<<--^----^^-----^----^^------^----->>");
        }
        else if (!_joining && _teleport && !_started)
        {
            System.out.println("<<---------------------------------->>");
            System.out.println(">> TvT Engine infos dump (TELEPORT) <<");
            System.out.println("<<--^----^^-----^----^^------^^----->>");
        }
        else if (!_joining && !_teleport && _started)
        {
            System.out.println("<<--------------------------------->>");
            System.out.println(">> TvT Engine infos dump (STARTED) <<");
            System.out.println("<<--^----^^-----^----^^------^----->>");
        }

        System.out.println("Name: " + _eventName);
        System.out.println("Desc: " + _eventDesc);
        System.out.println("Join location: " + _joiningLocationName);
        System.out.println("Min lvl: " + _minlvl);
        System.out.println("Max lvl: " + _maxlvl);
        System.out.println("");
        System.out.println("##########################");
        System.out.println("# _teams(Vector<String>) #");
        System.out.println("##########################");
        
        for (String team : _teams)
           System.out.println(team + " Kills Done :" + _teamKillsCount.get(_teams.indexOf(team)));

        if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
        {
            System.out.println("");
            System.out.println("#########################################");
            System.out.println("# _playersShuffle(Vector<L2PcInstance>) #");
            System.out.println("#########################################");
        
            for (L2PcInstance player : _playersShuffle)
            {
                if (player != null)
                    System.out.println("Name: " + player.getName());
            }
        }
        
        System.out.println("");
        System.out.println("##################################");
        System.out.println("# _players(Vector<L2PcInstance>) #");
        System.out.println("##################################");
        
        for (L2PcInstance player : _players)
        {
            if (player != null)
                System.out.println("Name: " + player.getName() + "   Team: " + player._teamNameTvT + "  Kills Done:" + player._countTvTkills);
        }
        
        System.out.println("");
        System.out.println("#####################################################################");
        System.out.println("# _savePlayers(Vector<String>) and _savePlayerTeams(Vector<String>) #");
        System.out.println("#####################################################################");
        
        for (String player : _savePlayers)
            System.out.println("Name: " + player + "    Team: " + _savePlayerTeams.get(_savePlayers.indexOf(player)));
        
        System.out.println("");
        System.out.println("");
    }
    
    public static void loadData()
    {
        _eventName = new String();
        _eventDesc = new String();
        _topTeam = new String();
        _joiningLocationName = new String();
        _teams = new Vector<String>();
        _savePlayers = new Vector<String>();
        _savePlayerTeams = new Vector<String>();
        _players = new Vector<L2PcInstance>();
        _playersShuffle = new Vector<L2PcInstance>();
        _teamPlayersCount = new Vector<Integer>();
        _teamKillsCount = new Vector<Integer>();
        _teamColors = new Vector<Integer>();
        _teamsX = new Vector<Integer>();
        _teamsY = new Vector<Integer>();
        _teamsZ = new Vector<Integer>();
        _joining = false;
        _teleport = false;
        _started = false;
        _sitForced = false;
        _npcId = 0;
        _npcX = 0;
        _npcY = 0;
        _npcZ = 0;
        _rewardId = 0;
        _rewardAmount = 0;
        _topKills = 0;
        _minlvl = 0;
        _maxlvl = 0;
        
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection(con);

            statement = con.prepareStatement("Select * from tvt");
            rs = statement.executeQuery();
            
            int teams =0;
            
            while (rs.next())
            {        
                _eventName = rs.getString("eventName");
                _eventDesc = rs.getString("eventDesc");
                _joiningLocationName = rs.getString("joiningLocation");
                _minlvl = rs.getInt("minlvl");
                _maxlvl = rs.getInt("maxlvl");
                _npcId = rs.getInt("npcId");
                _npcX = rs.getInt("npcX");
                _npcY = rs.getInt("npcY");
                _npcZ = rs.getInt("npcZ");
                _rewardId = rs.getInt("rewardId");
                _rewardAmount = rs.getInt("rewardAmount"); 
                teams = rs.getInt("teamsCount");
            
            }                    
            statement.close();            
            
            int index = -1;
            if (teams > 0)
                index = 0;    
            while (index < teams && index > -1)
            { 
                statement = con.prepareStatement("Select * from tvt_teams where teamId = ?");
                statement.setInt(1, index);
                rs = statement.executeQuery(); 
                while (rs.next())
                {
                    _teams.add(rs.getString("teamName"));
                    _teamPlayersCount.add(0);
                    _teamKillsCount.add(0); 
                    _teamColors.add(0);
                    _teamsX.add(0);
                    _teamsY.add(0);
                    _teamsZ.add(0);
                    _teamsX.set(index, rs.getInt("teamX"));
                    _teamsY.set(index, rs.getInt("teamY"));
                    _teamsZ.set(index, rs.getInt("teamZ"));
                    _teamColors.set(index, rs.getInt("teamColor"));
                }                
                index ++;
                statement.close();            
            }       
        }        
        catch (Exception e)
        {
            _log.error("Exception: TvT.loadData(): " + e.getMessage());
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }
    
    public static void saveData()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            
            statement = con.prepareStatement("Delete from tvt");
            statement.execute();
            statement.close();

            statement = con.prepareStatement("INSERT INTO tvt (eventName, eventDesc, joiningLocation, minlvl, maxlvl, npcId, npcX, npcY, npcZ, rewardId, rewardAmount, teamsCount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");  
            statement.setString(1, _eventName);
            statement.setString(2, _eventDesc);
            statement.setString(3, _joiningLocationName);
            statement.setInt(4, _minlvl);
            statement.setInt(5, _maxlvl);
            statement.setInt(6, _npcId);
            statement.setInt(7, _npcX);
            statement.setInt(8, _npcY);
            statement.setInt(9, _npcZ);
            statement.setInt(10, _rewardId);
            statement.setInt(11, _rewardAmount);
            statement.setInt(12, _teams.size());
            statement.execute();
            statement.close();
            
            statement = con.prepareStatement("Delete from tvt_teams");
            statement.execute();
            statement.close();
            
            for (String teamName : _teams)
            { 
                int index = _teams.indexOf(teamName);
                
                if (index == -1)
                    return;
                statement = con.prepareStatement("INSERT INTO tvt_teams (teamId ,teamName, teamX, teamY, teamZ, teamColor) VALUES (?, ?, ?, ?, ?, ?)");  
                statement.setInt(1 , index);
                statement.setString(2, teamName);
                statement.setInt(3, _teamsX.get(index));
                statement.setInt(4, _teamsY.get(index));
                statement.setInt(5, _teamsZ.get(index));
                statement.setInt(6, _teamColors.get(index));                
                statement.execute();
                statement.close();
            }
        }
        catch (Exception e)
        {
            _log.error("Exception: TvT.saveData(): " + e.getMessage());
        }        
        finally {try { con.close(); } catch (Exception e) {}}
    }

    public static void showEventHtml(L2PcInstance eventPlayer, String objectId)
    {
        try
        {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

            TextBuilder replyMSG = new TextBuilder("<html><head><body>");
            replyMSG.append("TvT Match<br><br><br>");
            replyMSG.append("Current event...<br1>");
            replyMSG.append("    ... name:&nbsp;<font color=\"00FF00\">" + _eventName + "</font><br1>");
            replyMSG.append("    ... description:&nbsp;<font color=\"00FF00\">" + _eventDesc + "</font><br><br>");

            if (!_started && !_joining)
                replyMSG.append("<center>Wait till the admin/gm start the participation.</center>");
            else if (!_started && _joining && eventPlayer.getLevel()>=_minlvl && eventPlayer.getLevel()<_maxlvl)
            {
                if (_players.contains(eventPlayer) || _playersShuffle.contains(eventPlayer))
                {
                    if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
                        replyMSG.append("You participated already in team <font color=\"LEVEL\">" + eventPlayer._teamNameTvT + "</font><br><br>");
                    else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
                        replyMSG.append("You participated already!<br><br>");

                    replyMSG.append("<table border=\"0\"><tr>");
                    replyMSG.append("<td width=\"200\">Wait till event start or</td>");
                    replyMSG.append("<td width=\"60\"><center><button value=\"remove\" action=\"bypass -h npc_" + objectId + "_tvt_player_leave\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
                    replyMSG.append("<td width=\"100\">your participation!</td>");
                    replyMSG.append("</tr></table>");
                }
                else
                {
                    replyMSG.append("You want to participate in the event?<br><br>");
                    replyMSG.append("<td width=\"200\">Admin set min lvl : <font color=\"00FF00\">" + _minlvl + "</font></td><br>");
                    replyMSG.append("<td width=\"200\">Admin set max lvl : <font color=\"00FF00\">" + _maxlvl + "</font></td><br><br>");
                    
                    if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
                    {
                        replyMSG.append("<center><table border=\"0\">");
                    
                        for (String team : _teams)
                        {
                            replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font>&nbsp;(" + teamPlayersCount(team) + " joined)</td>");
                            replyMSG.append("<td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_" + objectId + "_tvt_player_join " + team + "\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
                        }
                    
                        replyMSG.append("</table></center>");
                    }
                    else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
                    {
                        replyMSG.append("<center><table border=\"0\">");
                        
                        for (String team : _teams)
                            replyMSG.append("<tr><td width=\"100\"><font color=\"LEVEL\">" + team + "</font></td>");
                    
                        replyMSG.append("</table></center><br>");
                        
                        replyMSG.append("<button value=\"Join\" action=\"bypass -h npc_" + objectId + "_tvt_player_join eventShuffle\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
                        replyMSG.append("Teams will be reandomly generated!");
                    }
                }
            }
            else if (_started && !_joining)
                replyMSG.append("<center>TvT match is in progress.</center>");
            else if (eventPlayer.getLevel()<_minlvl || eventPlayer.getLevel()>_maxlvl )
            {
                replyMSG.append("Your lvl : <font color=\"00FF00\">" + eventPlayer.getLevel() +"</font><br>");
                replyMSG.append("Admin set min lvl : <font color=\"00FF00\">" + _minlvl + "</font><br>");
                replyMSG.append("Admin set max lvl : <font color=\"00FF00\">" + _maxlvl + "</font><br><br>");
                replyMSG.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
            }
            
            replyMSG.append("</body></html>");
            adminReply.setHtml(replyMSG.toString());
            eventPlayer.sendPacket(adminReply);
        }
        catch (Exception e)
        {
            _log.error("TvT Engine[showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
        }
    }

    public static void addPlayer(L2PcInstance player, String teamName)
    {
        if (!addPlayerOk(teamName, player))
            return;
        
        if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
        {
            player._teamNameTvT = teamName;
            _players.add(player);
            setTeamPlayersCount(teamName, teamPlayersCount(teamName)+1);
        }
        else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
            _playersShuffle.add(player);
        
        player._originalNameColorTvT = player.getAppearance().getNameColor();
        player._originalKarmaTvT = player.getKarma();
        player._inEventTvT = true;
        player._countTvTkills = 0;
    }
    
    public static boolean addPlayerOk(String teamName, L2PcInstance eventPlayer)
    {
        if (CTF._savePlayers.contains(eventPlayer.getName())) 
        {
            eventPlayer.sendMessage("You already participated in another event!"); 
            return false;
        }

        if (Config.TVT_EVEN_TEAMS.equals("NO"))
            return true;
        else if (Config.TVT_EVEN_TEAMS.equals("BALANCE"))
        {
            boolean allTeamsEqual = true;
            int countBefore = -1;
        
            for (int playersCount : _teamPlayersCount)
            {
                if (countBefore == -1)
                    countBefore = playersCount;
            
                if (countBefore != playersCount)
                {
                    allTeamsEqual = false;
                    break;
                }
            
                countBefore = playersCount;
            }
        
            if (allTeamsEqual)
                return true;

            countBefore = Integer.MAX_VALUE;
        
            for (int teamPlayerCount : _teamPlayersCount)
            {
                if (teamPlayerCount < countBefore)
                    countBefore = teamPlayerCount;
            }

            Vector<String> joinableTeams = new Vector<String>();
        
            for (String team : _teams)
            {
                if (teamPlayersCount(team) == countBefore)
                    joinableTeams.add(team);
            }
        
            if (joinableTeams.contains(teamName))
                return true;
        }
        else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
            return true;

        eventPlayer.sendMessage("Too many players in team \"" + teamName + "\"");
        return false;
    }

    public static synchronized void addDisconnectedPlayer(L2PcInstance player)
    {
        if ((Config.TVT_EVEN_TEAMS.equals("SHUFFLE") && (_teleport || _started)) || (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE")))
        {
            player._teamNameTvT = _savePlayerTeams.get(_savePlayers.indexOf(player.getName()));
            _players.add(player);
            player._originalNameColorTvT = player.getAppearance().getNameColor();
            player._originalKarmaTvT = player.getKarma();
            player._inEventTvT = true;
            player._countTvTkills = 0;

            if (_teleport || _started)
            {
                player.getAppearance().setNameColor(_teamColors.get(_teams.indexOf(player._teamNameTvT)));
                player.setKarma(0);
                player.broadcastUserInfo();
                player.teleToLocation(_teamsX.get(_teams.indexOf(player._teamNameTvT)), _teamsY.get(_teams.indexOf(player._teamNameTvT)), _teamsZ.get(_teams.indexOf(player._teamNameTvT)));
            }
        }
    }
    
    public static void removePlayer(L2PcInstance player)
    {
        if (player != null)
        {
            if (Config.TVT_EVEN_TEAMS.equals("NO") || Config.TVT_EVEN_TEAMS.equals("BALANCE"))
            {
                _players.remove(player);
                setTeamPlayersCount(player._teamNameTvT, teamPlayersCount(player._teamNameTvT)-1);                
            }
            else if (Config.TVT_EVEN_TEAMS.equals("SHUFFLE"))
                _playersShuffle.remove(player);
            
            player.getAppearance().setNameColor(player._originalNameColorTvT);
            player.setKarma(player._originalKarmaTvT);
            player.broadcastUserInfo();
            player._teamNameTvT = new String();
            player._inEventTvT = false;
            player._countTvTkills = 0;
        }
    }
    
    public static void cleanTvT()
    {
        for (String team : _teams)
        {
            int index = _teams.indexOf(team);

            _teamPlayersCount.set(index, 0);
            _teamKillsCount.set(index, 0);
        }
        
        for (L2PcInstance player : _players)
        {
            removePlayer(player);            
        }

        _topKills = 0;
        _topTeam = new String();
        _players = new Vector<L2PcInstance>();
        _playersShuffle = new Vector<L2PcInstance>();

    }
    
    public static void unspawnEventNpc()
    {
        if (_npcSpawn == null)
            return;

        _npcSpawn.getLastSpawn().deleteMe();
        _npcSpawn.stopRespawn();
        SpawnTable.getInstance().deleteSpawn(_npcSpawn, true);
    }
    
    public static void teleportFinish()
    {
        Announcements.getInstance().announceToAll(_eventName + "(TvT): Teleport back to participation NPC in 20 seconds!");

        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
                                                       {
                                                            public void run()
                                                            {                                                                
                                                                for (L2PcInstance player : _players)
                                                                {
                                                                    if (player !=  null)
                                                                        player.teleToLocation(_npcX, _npcY, _npcZ);
                                                                } 
                                                                cleanTvT();
                                                             }
                                                       }, 20000);
    }

    public static int teamKillsCount(String teamName)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return -1;

        return _teamKillsCount.get(index);
    }
    
    public static void setTeamKillsCount(String teamName, int teamKillsCount)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;

        _teamKillsCount.set(index, teamKillsCount);
    }
    
    public static int teamPlayersCount(String teamName)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return -1;

        return _teamPlayersCount.get(index);
    }
    
    public static void setTeamPlayersCount(String teamName, int teamPlayersCount)
    {
        int index = _teams.indexOf(teamName);
        
        if (index == -1)
            return;
        
        _teamPlayersCount.set(index, teamPlayersCount);
    }
}