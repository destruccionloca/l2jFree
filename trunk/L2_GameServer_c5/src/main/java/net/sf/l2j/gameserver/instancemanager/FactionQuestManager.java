package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.entity.faction.FactionQuest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author evill33t
 *
 */
public class FactionQuestManager
{
    protected static Log _log = LogFactory.getLog(FactionQuestManager.class.getName());

    // =========================================================
    private static FactionQuestManager _Instance;
    public static final FactionQuestManager getInstance()
    {
        if (_Instance == null)
        {
            if ( _log.isDebugEnabled())_log.debug("Initializing FactionQuestManager");
            _Instance = new FactionQuestManager();
            _Instance.load();
        }
        return _Instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private FastList<FactionQuest> _Quests;
    
    // =========================================================
    // Constructor
    public FactionQuestManager()
    {
    }

    // =========================================================
    // Method - Public
    public final void reload()
    {
        this.getFactionQuests().clear();
        this.load();
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection(con);

            statement = con.prepareStatement("Select id, faction_id, name, description, reward, mobid, amount, min_level from faction_quests order by id");
            rs = statement.executeQuery();
            while (rs.next())
            {
                getFactionQuests().add(new FactionQuest(
                                                        rs.getInt("id"),
                                                        rs.getInt("faction_id"),
                                                        rs.getString("name"),
                                                        rs.getString("description"),
                                                        rs.getInt("reward"),  rs.getInt("mobid"),
                                                        rs.getInt("amount"),
                                                        rs.getInt("min_level")                
                                                        ));
            }

            statement.close();

            _log.info("Loaded: " + getFactionQuests().size() + " factionquests");
        }
        catch (Exception e)
        {
            _log.error("Exception: FactionQuestManager.load(): " + e.getMessage(),e);
        }
        
        finally {try { con.close(); } catch (Exception e) {}}
        
    }

    // =========================================================
    // Property - Public
    public final FactionQuest getFactionQuest(int questId)
    {
        int index = getFactionQuestIndex(questId);
        if (index >= 0) return getFactionQuests().get(index);
        return null;
    }
    
    public final int getFactionQuestIndex(int questId)
    {
        FactionQuest quest;
        for (int i = 0; i < getFactionQuests().size(); i++)
        {
            quest = getFactionQuests().get(i);
            if (quest != null && quest.getId() == questId) return i;
        }
        return -1;
    }
    
    public final FastList<FactionQuest> getFactionQuests()
    {
        if (_Quests == null) _Quests = new FastList<FactionQuest>();
        return _Quests;
    }
}
