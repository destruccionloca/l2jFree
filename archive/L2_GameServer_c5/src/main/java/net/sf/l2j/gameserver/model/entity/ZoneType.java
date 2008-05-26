package net.sf.l2j.gameserver.model.entity;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Object;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZoneType
{
    protected static Log _log = LogFactory.getLog(ZoneType.class.getName());

    // =========================================================
    // Data Field
    private String _TypeName;
    private FastList<Zone> _Zones;
    
    public static enum ZoneTypeEnum
    {
        Arena ("Arena"),
        ArenaSpawn ("Arena Spawn"),
        CastleArea ("Castle Area"),
        CastleDefenderSpawn ("Castle Defender Spawn"),
        ClanHall ("Clan Hall"),
        Peace ("Peace"),
        SiegeBattleField ("Siege Battlefield"),
        Town ("Town"),
        TownSpawn ("Town Spawn"),
        Underground ("Underground"),
        Water ("Water"), 
        NoLanding ("No Landing"),
        NoEscape ("Jail"),
        Jail ("Jail"),
        JailSpawn ("Jail Spawn"),
        MotherTree ("MotherTree"),
        Recharge ("Recharge"),
        Damage ("Damage"),
        Fishing ("Fishing"),
        MonsterDerbyTrack ("Monster Derby Track"),
        OlympiadStadia ("Olympiad Stadia"),
        Noobie ("Noobie"),
        FourSepulcher ("FourSepulcher"),
        LairofAntharas ("LairofAntharas"),
        LairofBaium ("LairofBaium"),
        LairofValakas ("LairofValakas"),
        LairofLilith ("LairofLilith"),
        LairofAnakim ("LairofAnakim"),
        LairofZaken ("LairofZaken");
        
        private final String _zoneName;
        
       	private ZoneTypeEnum (String ZoneName)
    	{
       		_zoneName = ZoneName;
    	}
    	
    	public String getName() {
    		return _zoneName;
    	}
    }
   
    public static String getZoneTypeName(ZoneTypeEnum zt)
    {
        return zt.getName();
    }

    // =========================================================
    // Constructor
    public ZoneType(String typeName)
    {
        _TypeName = typeName.trim();
    }

    // =========================================================
    // Method - Public
    public int addZone(int id, String zoneName, int taxById)
    {
        getZones().add(new Zone(id, zoneName, taxById));
        return getZones().size() - 1;
    }

    public int addZoneCoord(String zoneName, int x1, int y1, int x2, int y2, int z)
    {
        return getZone(zoneName).addCoord(x1, y1, x2, y2, z);
    }
    
    public boolean checkIfInZone(L2Object obj)
    {
        return checkIfInZone(obj.getX(), obj.getY());
    }
    
    public boolean checkIfInZone(int x, int y)
    {
        for (Zone zone: getZones())
        {
            if (zone.checkIfInZone(x, y)) return true;
        }
        return false;
    }
    
    public boolean checkIfInZoneIncludeZ(int x, int y, int z)
    {
        for (Zone zone: getZones())
        {
            if (zone.checkIfInZone(x, y, z)) return true;
        }
        return false;
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final String getTypeName()
    {
        return _TypeName;
    }
    
    public final Zone getZone(int zoneId)
    {
        for (Zone z: getZones())
            if (z.getId() == zoneId) return z;
        return null;
    }
    
    public final Zone getZone(String zoneName)
    {
        for (Zone z: getZones())
            if (z.getName().equalsIgnoreCase(zoneName.trim())) return z;

        getZones().add(new Zone(getZones().size() + 1, zoneName, 0));
        return getZones().get(getZones().size() - 1);
    }
    
    public final Zone getZone(int x, int y)
    {
        for (Zone z: getZones())
            if (z.checkIfInZone(x, y)) return z;
        return null;
    }
    
    public final FastList<Zone> getZones()
    {
        if (_Zones == null) _Zones = new FastList<Zone>();
        return _Zones;
    }
}