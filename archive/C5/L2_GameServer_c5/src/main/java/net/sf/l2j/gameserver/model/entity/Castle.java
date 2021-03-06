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
package net.sf.l2j.gameserver.model.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.CastleUpdater;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.CropProcure;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Manor;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.SeedProduction;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.PledgeShowInfoUpdate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Castle
{
    protected static Log _log = LogFactory.getLog(Castle.class.getName());
    
    // =========================================================
    // Data Field
    private FastList<CropProcure> _procure = new FastList<CropProcure>();
    private FastList<SeedProduction> _production = new FastList<SeedProduction>();

    // =========================================================
    // Data Field
    private int _CastleId                      = 0;
    private FastList<L2DoorInstance> _Doors        = new FastList<L2DoorInstance>();
    private FastList<String> _DoorDefault          = new FastList<String>();
    private String _Name                       = "";
    private int _OwnerId                       = 0;
    private Siege _Siege                       = null;
    private Calendar _SiegeDate;
    private int _SiegeDayOfWeek                = 7; // Default to saturday
    private int _SiegeHourOfDay                = 20; // Default to 8 pm server time
    private int _TaxPercent                    = 0;
    private double _TaxRate                    = 0;
    private int _Treasury                      = 0;
    private Zone _Zone;
    private FastList<Zone> _ZoneTown;
    private L2Clan _formerOwner;

    // =========================================================
    // Constructor
    public Castle(int castleId)
    {
        _CastleId = castleId;
        this.load();
        this.loadDoor();
    }

    // =========================================================
    // Method - Public
    // This method add to the treasury
    /** Add amount to castle instance's treasury (warehouse). */
    public void addToTreasury(int amount)
    {
        if (getOwnerId() <= 0) return;

        if (_Name.equalsIgnoreCase("Schuttgart") || _Name.equalsIgnoreCase("Goddard"))
        {
        	Castle rune = CastleManager.getInstance().getCastle("rune");
        	if (rune != null )
        	{
        		int runeTax = (int)(amount * rune.getTaxRate());
        		if (rune.getOwnerId() > 0 ) rune.addToTreasury(runeTax);
        		amount -= runeTax;
        	}
        }
        if (!_Name.equalsIgnoreCase("aden") && !_Name.equalsIgnoreCase("Rune") && !_Name.equalsIgnoreCase("Schuttgart") && !_Name.equalsIgnoreCase("Goddard"))    // If current castle instance is not Aden, Rune, Goddard or Schuttgart.
        {
            Castle aden = CastleManager.getInstance().getCastle("aden");
            if (aden != null)
            {
                int adenTax = (int)(amount * aden.getTaxRate());        // Find out what Aden gets from the current castle instance's income
                if (aden.getOwnerId() > 0) aden.addToTreasury(adenTax); // Only bother to really add the tax to the treasury if not npc owned
                    
                amount -= adenTax; // Subtract Aden's income from current castle instance's income
            }
        }
        
        _Treasury += amount; // Add to the current treasury total.  Use "-" to substract from treasury
        
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("Update castle set treasury = ? where id = ?");
            statement.setInt(1, getTreasury());
            statement.setInt(2, getCastleId());
            statement.execute();
            statement.close();
        }
        catch (Exception e) {} 
        finally {try { con.close(); } catch (Exception e) {}}
    }
    
    /**
     * Move non clan members off castle area and to nearest town.<BR><BR>
     */
    public void banishForeigner(L2PcInstance activeChar)
    {
        // Get players from this and nearest world regions
        for (L2PlayableInstance player : L2World.getInstance().getVisiblePlayable(activeChar))
        {
            if(!(player instanceof L2PcInstance)) continue;
            
            // Skip if player is in clan
            if (((L2PcInstance)player).getClanId() == getOwnerId())
                continue;
            
            if (checkIfInZone(player)) player.teleToLocation(MapRegionTable.TeleportWhereType.Town); 
        }
    }
    
    /**
     * Return true if object is inside the zone
     */
    public boolean checkIfInZone(L2Object obj)
    {
        return checkIfInZone(obj.getX(), obj.getY());
    }

    /**
     * Return true if object is inside the zone
     */
    public boolean checkIfInZone(int x, int y)
    {
        return getZone().checkIfInZone(x, y);
    }

    /**
     * Return true if object is inside the zone
     */
    public boolean checkIfInZoneTowns(L2Object obj)
    {
        return checkIfInZoneTowns(obj.getX(), obj.getY());
    }

    /**
     * Return true if object is inside the zone
     */
    public boolean checkIfInZoneTowns(int x, int y)
    {
        for (Zone zone: getZoneTowns())
            if (zone.checkIfInZone(x, y)) return true;
        return false;
    }
    
    public void closeDoor(L2PcInstance activeChar, int doorId)
    {
        openCloseDoor(activeChar, doorId, false);
    }

    public void openDoor(L2PcInstance activeChar, int doorId)
    {
        openCloseDoor(activeChar, doorId, true);
    }

    public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
    {
        if (activeChar.getClanId() != getOwnerId())
            return;

        L2DoorInstance door = getDoor(doorId);
        if (door != null)
        {
            if (open)
                door.openMe();
            else
                door.closeMe();
        }
    }
    
    // This method is used to begin removing all castle upgrades
    public void removeUpgrade()
    {
        removeDoorUpgrade();
    }
    
    // This method updates the castle tax rate
    public void setOwner(L2Clan clan)
    {
        // Remove old owner
        if (getOwnerId() > 0 && (clan == null || clan.getClanId() != getOwnerId()))
        {
            L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId());            // Try to find clan instance 
            if (oldOwner != null)
            {
                
                if (_formerOwner == null)
                    _formerOwner = oldOwner;
//                int CrownId = CrownTable.getCrownId(getCastleId());
                oldOwner.setHasCastle(0);                                               // Unset has castle flag for old owner
                new Announcements().announceToAll(oldOwner.getName() + " has lost " + getName() + " castle!");

                // Remove Lord Crown from Leader
                if(clan.getLeader().isOnline())
                {
                    clan.getLeader().getPlayerInstance().getInventory().destroyItem("Crown",6841,1,clan.getLeader().getPlayerInstance(),null);
                    clan.getLeader().getPlayerInstance().getInventory().updateDatabase();
                }
                else // Leader is offline 
                {
                    java.sql.Connection con = null;
                    try
                    {
                        con = L2DatabaseFactory.getInstance().getConnection(con);
                        PreparedStatement statement = con.prepareStatement("delete from items where owner_id = ? and item_id = ?");
                        statement.setInt(1, clan.getLeader().getObjectId());
                        statement.setInt(2, 6841);
                        statement.execute();
                        statement.close();
                    }
                    catch (Exception e) {} 
                    finally {try { con.close(); } catch (Exception e) {}}
                }
            }                           
        }

        updateOwnerInDB(clan);                                                          // Update in database

        if (getSiege().getIsInProgress())                                               // If siege in progress
            getSiege().midVictory();                                                    // Mid victory phase of siege
    }

    // This method updates the castle tax rate
    public void setTaxPercent(L2PcInstance activeChar, int taxPercent)
    {
        if (taxPercent < 0 || taxPercent > 15)
        {
            activeChar.sendMessage("Tax value must be between 1 and 15.");
            return;
        }
        
        _TaxPercent = taxPercent;
        _TaxRate = _TaxPercent / 100.0;

        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("Update castle set taxPercent = ? where id = ?");
            statement.setInt(1, taxPercent);
            statement.setInt(2, getCastleId());
            statement.execute();
            statement.close();
        }
        catch (Exception e) {} 
        finally {try { con.close(); } catch (Exception e) {}}

        activeChar.sendMessage(getName() + " castle tax changed to " + taxPercent + "%.");
    }
    
    /**
     * Respawn all doors on castle grounds<BR><BR>
     */
    public void spawnDoor()
    {
        spawnDoor(false);
    }
    
    /**
     * Respawn all doors on castle grounds<BR><BR>
     */
    public void spawnDoor(boolean isDoorWeak)
    {
        for (int i = 0; i < getDoors().size(); i++)
        {
            L2DoorInstance door = getDoors().get(i);
            if (door.getCurrentHp() <= 0)
            {
                door.decayMe(); // Kill current if not killed already
                door = DoorTable.parseList(_DoorDefault.get(i));
                if (isDoorWeak)
                    door.setCurrentHp(door.getMaxHp() / 2);
                door.spawnMe(door.getX(), door.getY(),door.getZ());
                getDoors().set(i, door);
            }
            else if (door.getOpen() == 0)
                door.closeMe();
        }
        loadDoorUpgrade(); // Check for any upgrade the doors may have
    }

    // This method upgrade door
    public void upgradeDoor(int doorId, int hp, int pDef, int mDef)
    {
        L2DoorInstance door = getDoor(doorId);
        if (door == null)
            return;
        
        if (door != null && door.getDoorId() == doorId)
        {
            door.setCurrentHp(door.getMaxHp() + hp);

            saveDoorUpgrade(doorId, hp, pDef, mDef);
            return;
        }
    }
    
    // =========================================================
    // Method - Private
    // This method loads castle
    private void load()
    {
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection(con);

            statement = con.prepareStatement("Select * from castle where id = ?");
            statement.setInt(1, getCastleId());
            rs = statement.executeQuery();
    restoreManorData();
            while (rs.next())
            {
                _Name = rs.getString("name");
                //_OwnerId = rs.getInt("ownerId");

                _SiegeDate = Calendar.getInstance();
                _SiegeDate.setTimeInMillis(rs.getLong("siegeDate"));
                
                _SiegeDayOfWeek = rs.getInt("siegeDayOfWeek");
                if (_SiegeDayOfWeek < 1 || _SiegeDayOfWeek > 7)
                    _SiegeDayOfWeek = 7;

                _SiegeHourOfDay = rs.getInt("siegeHourOfDay");
                if (_SiegeHourOfDay < 0 || _SiegeHourOfDay > 23)
                    _SiegeHourOfDay = 20;

                _TaxPercent = rs.getInt("taxPercent");
                _Treasury = rs.getInt("treasury");
            }

            statement.close();

            _TaxRate = _TaxPercent / 100.0;

            statement = con.prepareStatement("Select clan_id from clan_data where hasCastle = ?");
            statement.setInt(1, getCastleId());
            rs = statement.executeQuery();

            while (rs.next())
            {
                _OwnerId = rs.getInt("clan_id");
            }

            if (getOwnerId() > 0)
            {
                L2Clan clan = ClanTable.getInstance().getClan(getOwnerId());                        // Try to find clan instance 
                ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000);     // Schedule owner tasks to start running 
            }

            statement.close();
        }
        catch (Exception e)
        {
            _log.error("Exception: loadCastleData(): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }

    // This method loads castle door data from database
    private void loadDoor()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("Select * from castle_door where castleId = ?");
            statement.setInt(1, getCastleId());
            ResultSet rs = statement.executeQuery();

            while (rs.next())
            {
                // Create list of the door default for use when respawning dead doors
                _DoorDefault.add(rs.getString("name") 
                        + ";" + rs.getInt("id") 
                        + ";" + rs.getInt("x") 
                        + ";" + rs.getInt("y") 
                        + ";" + rs.getInt("z") 
                        + ";" + rs.getInt("hp") 
                        + ";" + rs.getInt("pDef") 
                        + ";" + rs.getInt("mDef"));

                L2DoorInstance door = DoorTable.parseList(_DoorDefault.get(_DoorDefault.size() - 1));
                door.spawnMe(door.getX(), door.getY(),door.getZ());             
                _Doors.add(door);
            }

            statement.close();
        }
        catch (Exception e)
        {
            _log.error("Exception: loadCastleDoor(): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }

    // This method loads castle door upgrade data from database
    private void loadDoorUpgrade()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("Select * from castle_doorupgrade where doorId in (Select Id from castle_door where castleId = ?)");
            statement.setInt(1, getCastleId());
            ResultSet rs = statement.executeQuery();

            while (rs.next())
            {
                upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
            }

            statement.close();
        }
        catch (Exception e)
        {
            _log.error("Exception: loadCastleDoorUpgrade(): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }

    private void removeDoorUpgrade()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("delete from castle_doorupgrade where doorId in (select id from castle_door where castleId=?)");
            statement.setInt(1, getCastleId());
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.error("Exception: removeDoorUpgrade(): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }

    private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement = con.prepareStatement("INSERT INTO castle_doorupgrade (doorId, hp, pDef, mDef) values (?,?,?,?)");
            statement.setInt(1, doorId);
            statement.setInt(2, hp);
            statement.setInt(3, pDef);
            statement.setInt(4, mDef);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.error("Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + e.getMessage(),e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void updateOwnerInDB(L2Clan clan)
    {
        if (clan != null)
            _OwnerId = clan.getClanId();    // Update owner id property
        else
            _OwnerId = 0;                   // Remove owner

        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;

            // ============================================================================
            // NEED TO REMOVE HAS CASTLE FLAG FROM CLAN_DATA
            // SHOULD BE CHECKED FROM CASTLE TABLE
            statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?");
            statement.setInt(1, getCastleId());
            statement.execute();
            statement.close();   

            statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=?");
            statement.setInt(1, getCastleId());
            statement.setInt(2, getOwnerId());
            statement.execute();
            statement.close();   
            // ============================================================================

//            int CrownId = CrownTable.getCrownId(getCastleId());
            
            // Announce to clan memebers
            if (clan != null)
            {
                clan.setHasCastle(getCastleId()); // Set has castle flag for new owner
                new Announcements().announceToAll(clan.getName() + " has taken " + getName() + " castle!");
                clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                
                ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000);   // Schedule owner tasks to start running 
            }
        }
        catch (Exception e)
        {
            _log.error("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    // =========================================================
    // Proeprty
    public final int getCastleId()
    {
        return _CastleId;
    }

    public final L2DoorInstance getDoor(int doorId)
    {
        if (doorId <= 0)
            return null;
        
        for (int i = 0; i < getDoors().size(); i++)
        {
            L2DoorInstance door = getDoors().get(i);
            if (door.getDoorId() == doorId)
                return door;
        }
        return null;
    }

    public final FastList<L2DoorInstance> getDoors()
    {
        return _Doors;
    }

    public final String getName()
    {
        return _Name;
    }

    public final int getOwnerId()
    {
        return _OwnerId;
    }

    public final Siege getSiege()
    {
        if (_Siege == null) _Siege = new Siege(new Castle[] {this});
        return _Siege;
    }

    public final Calendar getSiegeDate() { return _SiegeDate; }

    public final int getSiegeDayOfWeek() { return _SiegeDayOfWeek; }

    public final int getSiegeHourOfDay() { return _SiegeHourOfDay; }

    public final int getTaxPercent()
    {
        return _TaxPercent;
    }

    public final double getTaxRate()
    {
        return _TaxRate;
    }

    public final int getTreasury()
    {
        return _Treasury;
    }

    public final Zone getZone()
    {
        if (_Zone == null) _Zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.CastleArea), getName());
        return _Zone;
    }

    public final Zone getZoneTown(int id)
    {
        for (Zone zone: getZoneTowns())
            if (zone.getId() == id) return zone;
        return null;
    }

    public final Zone getZoneTown(String name)
    {
        for (Zone zone: getZoneTowns())
            if (zone.getName() == name) return zone;
        return null;
    }

    public final FastList<Zone> getZoneTowns()
    {
        if (_ZoneTown == null)
        {
            _ZoneTown = new FastList<Zone>();
            // Add towns that belong to castle
            for (Zone zone: ZoneManager.getInstance().getZones(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Town)))
                if (zone != null && zone.getTaxById() == getCastleId()) _ZoneTown.add(zone);
        }
        return _ZoneTown;
    }


    /**
     * Manor code for manage the manor data
     * @author Scar69
     * @from L2Emu Team 629-834
     */
    public void loadDefManorData(){
        //Load def seeds by L2Emu team
        boolean Added;
        FastList<Integer> seedIds = new FastList<Integer>();
        seedIds = L2Manor.getInstance().getSeedsForCastle(getCastleId());
        for(int i = 0;i < seedIds.size();i++){
            Added = false;
            for(int ch = 0;ch < _production.size(); ch++){
                if(seedIds.get(i) == _production.get(ch).getSeedId()){
                    Added = true;
                }
            }
            if(!Added){
            _production.add(new SeedProduction(seedIds.get(i),0,0));
            }
        }
        
        
        //Load the def crops
        FastList<Integer> cropIds = new FastList<Integer>();
        cropIds = L2Manor.getInstance().getCropForCastle(getCastleId());
        for(int c = 0;c < cropIds.size();c++){
            Added = false;
            for(int chk = 0; chk <_procure.size();chk++){
                if(cropIds.get(c) == _procure.get(chk).getId()){
                    Added = true;
                }
            }
            if(!Added){
            _procure.add(new CropProcure(cropIds.get(c),0,1));
            }
        }
    }
    
   public void restoreManorData()
    {
       //Load the def crops/seeds  
       loadDefManorData();
       //Load the crop/seed data from the database
        java.sql.Connection con = null;
        ResultSet rs;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;

            // restore procure info
            statement = con.prepareStatement("SELECT * FROM castle_manor_procure WHERE castle_id=?"); 
            statement.setInt(1, getCastleId());
            rs = statement.executeQuery();
            
            while(rs.next())
            {
                setCropReward(rs.getInt("crop_id"),rs.getInt("reward_type"));
                setCropAmount(rs.getInt("crop_id"),rs.getInt("can_buy"));
            }
            
            statement.close();

            if ( _log.isDebugEnabled())_log.debug("Restored procure from BD");

            // restore seed production info
            statement = con.prepareStatement("SELECT * FROM castle_manor_production WHERE castle_id=?"); 
            statement.setInt(1, getCastleId());
            rs = statement.executeQuery();
            
            while(rs.next())
            {
                setSeedAmount(rs.getInt("seed_id"),rs.getInt("can_produce"));
                setSeedPrice(rs.getInt("seed_id"),rs.getInt("seed_price"));
            }

            if ( _log.isDebugEnabled())_log.debug("Restored Production from BD");
    
            statement.close();

        }
        catch (Exception e)
        {
            _log.error("Error restoring manor procure data: " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }

    }
    //remove seed data
    private void EraseAllSeedData() // check me(int seed_Id, int can_produce, int castle_id, int seed_price)
    {
        java.sql.Connection con = null;
        try{
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=?;");
            statement.setInt(1, getCastleId());
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.error("Error removing seed production data: " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    //save manor production data
    public void saveSeedData()//check me(int seed_Id, int can_produce, int castle_id, int seed_price)
    {
        EraseAllSeedData();
        java.sql.Connection con = null;
        try{
            con = L2DatabaseFactory.getInstance().getConnection(con);
            for(SeedProduction s : _production){
    PreparedStatement statement = null;
            if(s.getCanProduce() > 0 && s.getPrice() > 0 & s.getSeedId() >0 && getCastleId() >0){
                statement = con.prepareStatement("INSERT INTO castle_manor_production VALUES \n (?,?,?,?);");
                statement.setInt(1, getCastleId());
                statement.setInt(2, s.getCanProduce());
                statement.setInt(3, s.getSeedId());
                statement.setInt(4, s.getPrice());
                try{statement.execute();}
        catch (Exception e){}

            }
            try{statement.close();}catch (Exception e){}

            }

        }
        catch (Exception e)
        {
            _log.error("Error adding seed production data: " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    //remove  crop data 
    // Related to castle_production table
    private void EraseAllCropData()//check me (int castle_id, int crop_id, int can_buy, int reward_type)
    {
        java.sql.Connection con = null;
        try{
            con = L2DatabaseFactory.getInstance().getConnection(con);
            PreparedStatement statement;
            statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=?;");
            statement.setInt(1, getCastleId());
            try{statement.execute();} catch (Exception e){}
            statement.close();
        }
        catch (Exception e)
        {
            _log.error("Error removing seed production data: " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    //save crop procure data
    // Related to castle_procure table
    public void saveCropData()//check me (int castle_id, int crop_id, int can_buy, int reward_type)
    {
        EraseAllCropData();
        java.sql.Connection con = null;
        try{
            con = L2DatabaseFactory.getInstance().getConnection(con);
            
            for(CropProcure cp : _procure){
        PreparedStatement statement = null;
                if(cp.getAmount() > 0 && cp.getReward() > 0 && cp.getId() >0 && getCastleId() >0 ){
                statement = con.prepareStatement("INSERT INTO castle_manor_procure VALUES \n (?,?,?,?);");
                    statement.setInt(1, getCastleId());
                    statement.setInt(2, cp.getId());
                    statement.setInt(3, cp.getAmount());
                    statement.setInt(4, cp.getReward());
                    try{statement.execute();}
             catch (Exception e){}

               }
     try{statement.close();}catch (Exception e){}
        }
           

        }
        catch (Exception e)
        {
            _log.error("Error adding seed production data: " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
        
    }
    public void addCropProcure(CropProcure crop)
    {
        _procure.add(crop);
    }
    public void setCropReward(int crop,int NewRw){
        for(CropProcure cp : _procure){
            if(cp.getId() == crop){
                cp.setReward(NewRw);
            }
                
        }
    saveCropData();
    }
    public int getCropReward(int crop){
        int rw = 0;
        for(CropProcure cp : _procure){
            if(cp.getId() == crop){
                rw = cp.getReward();
            }
                
        }
        return rw;
    }
    public void setCropAmount(int crop,int amount){
        for(CropProcure cp : _procure){
            if(cp.getId() == crop){
                cp.setAmount(amount);
            }
        }
    saveCropData();
    }
    public int getCropAmount(int crop){
        int amo = 0;
        for(CropProcure cp : _procure){
            if(cp.getId() == crop){
                amo = cp.getAmount();
            }
        }
        return amo;
    }
    public void addSeedProduction(SeedProduction seed)
    {
        _production.add(seed);
    }
    
    public FastList<CropProcure> getManorRewards()
    {
        return _procure;
    }
    
    public int getSeedAmount(int seedId)
    {
        for(SeedProduction s : _production)
        {
            if(s.getSeedId() == seedId)
                return s.getCanProduce();
        }
        
        return 0;
    }
    public FastList<SeedProduction> getSeedProduction(){
        
        return _production;
    }
    public void setSeedAmount(int seed , int amount){
        for(SeedProduction s : _production)
        {
            if(s.getSeedId() == seed){
                s.setCanProduce(amount);
            }
        }
    saveSeedData();
    }
    public void setSeedPrice(int seed,int pri){
        for(SeedProduction s : _production)
        {
            if(s.getSeedId() == seed){
                s.setPrice(pri);
            }
        }
    saveSeedData();
    }
    public int getSeedPrice(int seed){

        for(SeedProduction s : _production)
        {
            if(s.getSeedId() == seed){
                return s.getPrice();
            }
        }
        return 0;
    }
    public FastList<CropProcure> getCropProcure()
    {
        return _procure;
    }
    public void updateClansReputation()
    {
        if (_formerOwner != null )
        {
            if (_formerOwner != ClanTable.getInstance().getClan(getOwnerId()))
                _formerOwner.setReputationScore(_formerOwner.getReputationScore()-2000, true);
            else
            {
                _formerOwner.setReputationScore(_formerOwner.getReputationScore()+500, true);
            }
        }
        else 
        {

            L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
            owner.setReputationScore(owner.getReputationScore()+1000, true);
        }
    }
}