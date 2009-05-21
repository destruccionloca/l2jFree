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
package net.sf.l2j.gameserver.model.entity.faction;

import java.sql.PreparedStatement;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author evill33t
 *
 */
public class FactionQuest
{
    protected static Log _log = LogFactory.getLog(FactionQuest.class.getName());

    private final int _questId;
    private static int _factionId;
    private static String _name;
    private static String _descr;
    private static int _reward;
    private static int _mobId;
    private static int _amount;
    private static int _minLevel;
    
    public FactionQuest(int questId, int factionId, String name, String descr, int reward, int mobId, int amount, int minLevel)
    {
        _questId = questId;
        _factionId = factionId;
        _name = name;
        _descr = descr;
        _reward = reward;
        _mobId = mobId;
        _amount = amount;
        _minLevel = minLevel; 
    }
    
    public int getId() { return _questId; }
    public static String getName() { return _name; }
    public static String getDescr() { return _descr;}
    public static int getReward() { return _reward;}
    public static int getAmount() { return _amount;}
    public static int getMobId() { return _mobId;}    
    public static int getFactionId() { return _factionId;}
    public static int getMinLevel() { return _minLevel;}

    public static void createFactionQuest(L2PcInstance player,int factionQuestId) 
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            statement = con.prepareStatement("INSERT INTO character_faction_quests (char_id,faction_quest_id) VALUES (?,?)");
            statement.setInt (1, player.getObjectId());
            statement.setInt (2, factionQuestId);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            _log.warn( "could not insert char faction quest:", e);
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
    }

    public static void endFactionQuest(L2PcInstance player,int factionQuestId)
    {
        player.sendMessage(getName()+" completed.");
        player.getNPCFaction().addFactionPoints(getReward()*Config.FACTION_QUEST_RATE);
        deleteFactionQuest(player,factionQuestId);
    }
    
    public static void deleteFactionQuest(L2PcInstance player,int factionQuestId)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            statement = con.prepareStatement("DELETE FROM character_faction_quests WHERE char_id=? AND faction_quest_id=?");
            statement.setInt (1, player.getObjectId());
            statement.setInt (2, factionQuestId);
        statement.executeUpdate();
            statement.close();
        } 
        catch (Exception e) 
        {
            _log.warn( "could not delete char faction quest:", e);
        } finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
}
