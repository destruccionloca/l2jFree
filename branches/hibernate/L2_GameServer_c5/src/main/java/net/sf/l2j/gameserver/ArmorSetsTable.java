package net.sf.l2j.gameserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import javolution.util.FastMap;

import net.sf.l2j.L2Registry;
import net.sf.l2j.gameserver.model.L2ArmorSet;

/**
 * 
 *
 * @author  Luno
 */
public class ArmorSetsTable
{
    private static Logger _log = Logger.getLogger(ArmorSetsTable.class.getName());
    private static ArmorSetsTable _instance;
    
    private FastMap<Integer,L2ArmorSet> _armorSets;
    
    public static ArmorSetsTable getInstance()
    {
        if(_instance == null)
            _instance = new ArmorSetsTable();
        return _instance;
    }
    private ArmorSetsTable()
    {
        _armorSets = new FastMap<Integer,L2ArmorSet>();
        loadData();
    }
    private void loadData()
    {
        Connection con;
        try 
        {
            con = L2Registry.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill FROM armorsets");
            ResultSet rset = statement.executeQuery();
            
            while(rset.next())
            {
                int chest = rset.getInt("chest");
                int legs  = rset.getInt("legs");
                int head  = rset.getInt("head");
                int gloves = rset.getInt("gloves");
                int feet  = rset.getInt("feet");
                int skill_id = rset.getInt("skill_id");
                int shield = rset.getInt("shield");
                int shield_skill_id = rset.getInt("shield_skill_id");
                int enchant6skill = rset.getInt("enchant6skill");
                _armorSets.put(chest, new L2ArmorSet(chest, legs, head, gloves, feet,skill_id, shield, shield_skill_id, enchant6skill));
            }
            
            _log.config("ArmorSetsTable: Loaded "+_armorSets.size()+" armor sets.");
            
            rset.close();
            statement.close();
            con.close();
        }
        catch (Exception e) 
        {
            _log.severe("ArmorSetsTable: Error reading ArmorSets table: " + e);
        }
    }
    public boolean setExists(int chestId)
    {
        return _armorSets.containsKey(chestId);
    }
    public L2ArmorSet getSet(int chestId)
    {
        return _armorSets.get(chestId);
    }
}