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
package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2PetData;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PetDataTable
{
    private final static Log _log = LogFactory.getLog(L2PetInstance.class.getName()); 
    private static PetDataTable _instance;
   
    public final static int PET_WOLF_ID = 12077;
    
    public final static int HATCHLING_WIND_ID = 12311;
    public final static int HATCHLING_STAR_ID = 12312;
    public final static int HATCHLING_TWILIGHT_ID = 12313;
    
    public final static int STRIDER_WIND_ID = 12526;
    public final static int STRIDER_STAR_ID = 12527;
    public final static int STRIDER_TWILIGHT_ID = 12528;
    
    public final static int WYVERN_ID = 12621;
    
    public final static int BABY_BUFFALO_ID = 12780;
    public final static int BABY_KOOKABURRA_ID = 12781;
    public final static int BABY_COUGAR_ID = 12782;
    
    public final static int SIN_EATER_ID = 12564;

    private static FastMap<Integer, FastMap<Integer, L2PetData>> petTable;
    
    public static PetDataTable getInstance()
    {
        if (_instance == null)
            _instance = new PetDataTable();
        
        return _instance;
    }
    
    private PetDataTable()
    {
        petTable = new FastMap<Integer, FastMap<Integer, L2PetData>>();
    }
    
    public void loadPetsData() 
    { 
        java.sql.Connection con = null;
        
        try
        { 
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("SELECT typeID, level, expMax, hpMax, mpMax, patk, pdef, matk, mdef, acc, evasion, crit, speed, atk_speed, cast_speed, feedMax, feedbattle, feednormal, loadMax, hpregen, mpregen, owner_exp_taken FROM pets_stats");
            ResultSet rset = statement.executeQuery();
            
            int petId, petLevel;
            
            while (rset.next())
            {
                petId = rset.getInt("typeID");
                petLevel = rset.getInt("level");
                
                //build the petdata for this level
                L2PetData petData = new L2PetData();
                petData.setPetID(petId);
                petData.setPetLevel(petLevel);
                petData.setPetMaxExp(rset.getLong("expMax"));
                petData.setPetMaxHP(rset.getInt("hpMax"));                        
                petData.setPetMaxMP(rset.getInt("mpMax"));
                petData.setPetPAtk( rset.getInt("patk") ); 
                petData.setPetPDef( rset.getInt("pdef") ); 
                petData.setPetMAtk( rset.getInt("matk") ); 
                petData.setPetMDef( rset.getInt("mdef") ); 
                petData.setPetAccuracy( rset.getInt("acc") ); 
                petData.setPetEvasion( rset.getInt("evasion") ); 
                petData.setPetCritical( rset.getInt("crit") ); 
                petData.setPetSpeed( rset.getInt("speed") ); 
                petData.setPetAtkSpeed( rset.getInt("atk_speed") ); 
                petData.setPetCastSpeed( rset.getInt("cast_speed") ); 
                petData.setPetMaxFeed( rset.getInt("feedMax") ); 
                petData.setPetFeedNormal( rset.getInt("feednormal") ); 
                petData.setPetFeedBattle( rset.getInt("feedbattle") ); 
                petData.setPetMaxLoad( rset.getInt("loadMax") ); 
                petData.setPetRegenHP( rset.getInt("hpregen") ); 
                petData.setPetRegenMP( rset.getInt("mpregen") );
                petData.setOwnerExpTaken( rset.getFloat("owner_exp_taken") );
                
                // if its the first data for this petid, we initialize its level FastMap
                if (!petTable.containsKey(petId))
                    petTable.put(petId, new FastMap<Integer, L2PetData>());
                
                petTable.get(petId).put(petLevel,petData);
            }
            
            rset.close();
            statement.close();            
        }
        catch (Exception e)
        {
            _log.warn("Could not load pets stats: "+ e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
        _log.info("PetDataTable: loaded " + petTable.size() + " pets.");
    }
    
    public void addPetData(L2PetData petData)
    {
        FastMap<Integer, L2PetData> h = petTable.get(petData.getPetID());
        
        if (h == null)
        {
            FastMap<Integer, L2PetData> statTable = new FastMap<Integer, L2PetData>();
            statTable.put(petData.getPetLevel(), petData);
            petTable.put(petData.getPetID(), statTable);
            return;
        }

        h.put(petData.getPetLevel(), petData);
    }
    
    public void addPetData(L2PetData[] petLevelsList) 
    {
        for (int i = 0; i < petLevelsList.length; i++) 
            addPetData(petLevelsList[i]);
    }
    
    public L2PetData getPetData(int petID, int petLevel)
    {
        return petTable.get(petID).get(petLevel);
    }
    
    public static int getPetIdByItemId(int itemId)
    {
        for(L2Pet pet: L2Pet.values())
            if (pet.getControlItemId() == itemId) return pet.getNpcId();
        return 0;
    }

    public static int getItemIdByPetId(int npcId)
    {
        for(L2Pet pet: L2Pet.values())
            if (pet.getNpcId() == npcId) return pet.getControlItemId();
        return 0;
    }
    
    public static int getFoodItemId(int npcId)
    {
        for(L2Pet pet: L2Pet.values())
            if (pet.getNpcId() == npcId) return pet.getFoodId();
        return 0;
    }
    
    public static boolean isPet(int npcId)
    {
        for(L2Pet pet: L2Pet.values())
            return (pet.getNpcId() == npcId);
        return false;
    }
    
    public static boolean isPetFood(int itemId)
    {
        for(L2Pet pet: L2Pet.values())
            return (pet.getFoodId() == itemId);
        return false;
    }
    
    public static boolean isPetItem(int itemId)
    {
        for(L2Pet pet: L2Pet.values())
            return (pet.getControlItemId() == itemId);
        return false;
    }

    public static boolean isMountable(int npcId)
    {
        for(L2Pet pet: L2Pet.values())
            if (pet.getNpcId() == npcId) return pet.isMountable();
        return false;
    }

    public static boolean isWolf(int npcId)
    {
        return ( PET_WOLF_ID == npcId);
    }
    
    public static boolean isHatchling(int npcId)
    {
        return ( HATCHLING_WIND_ID == npcId || HATCHLING_STAR_ID == npcId || HATCHLING_TWILIGHT_ID == npcId );
    }
    
    public static boolean isStrider(int npcId)
    {
        return ( STRIDER_WIND_ID == npcId || STRIDER_STAR_ID == npcId || STRIDER_TWILIGHT_ID == npcId );
    }
    
    public static boolean isWyvern(int npcId)
    {
        return ( WYVERN_ID == npcId  );
    }
    
    public static boolean isBaby(int npcId)
    {
        return ( BABY_BUFFALO_ID == npcId || BABY_KOOKABURRA_ID == npcId || BABY_COUGAR_ID == npcId );
    }

    public static boolean isSinEater(int npcId)
    {
        return npcId == SIN_EATER_ID;
    }
    
    
    /**
     * This class describes basic pet info
     * NPC template id, control item id, food item id and can be pet mounted
     */
    private static enum L2Pet
    {
        WOLF                    ( PET_WOLF_ID, 2375, 2515, false),
    	HATCHLING_WIND          ( HATCHLING_WIND_ID, 3500, 4038, false),
    	HATCHLING_STAR          ( HATCHLING_STAR_ID, 3501,  4038, false),
    	HATCHLING_TWILIGHT      ( HATCHLING_TWILIGHT_ID, 3502, 4038, false),
    	STRIDER_WIND            ( STRIDER_WIND_ID, 4422, 5168, true),
    	STRIDER_STAR            ( STRIDER_STAR_ID, 4423,  5168, true),
    	STRIDER_TWILIGHT        ( STRIDER_TWILIGHT_ID,4424, 5168, true),
    	WYVERN                  ( WYVERN_ID, 5249, 6316, true),
    	BABY_BUFFALO            ( BABY_BUFFALO_ID, 6648, 7582, false),
    	BABY_KOOKABURRA         ( BABY_KOOKABURRA_ID, 6649,  7582, false),
    	BABY_COUGAR             ( BABY_COUGAR_ID, 6650, 7582, false),
    	SIN_EATER               ( SIN_EATER_ID, 4425, 2515, false);
        
    	private final int _npcId;
    	private final int _controlItemId;
    	private final int _foodId;
    	private final boolean _isMountabe;
    	
    	private L2Pet (int npcId, int controlItemId, int foodId, boolean isMountabe)
    	{
    		_npcId = npcId;
    		_controlItemId = controlItemId;
    		_foodId = foodId;
    		_isMountabe = isMountabe;
    	}
    	
    	public int getNpcId() {
    		return _npcId;
    	}
    	
    	public int getControlItemId()
    	{
    		return _controlItemId;
    	}
    	
    	public int getFoodId()
    	{
    		return _foodId;
    	}
    	
    	public boolean isMountable()
    	{
    		return _isMountabe;
    	}
    }
}
