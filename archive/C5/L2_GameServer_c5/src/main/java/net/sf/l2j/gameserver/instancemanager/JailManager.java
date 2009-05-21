package net.sf.l2j.gameserver.instancemanager;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.Jail;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.model.entity.ZoneType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JailManager
{
    protected static Log _log = LogFactory.getLog(JailManager.class.getName());

    // =========================================================
    // Data Field
    private static JailManager _Instance;
    private FastList<Jail> _Jails;
    
    // =========================================================
    // Constructor
    public JailManager()
    {
    }

    // =========================================================
    // Method - Public
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { return (getJail(obj) != null); }

    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { return (getJail(x, y) != null); }

    // =========================================================
    // Method - Private
    public final void reload()
    {
	_Jails = null;
	load();
    }
    private final void load()
    {
        for (Zone zone: ZoneManager.getInstance().getZones(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Jail)))
            getJails().add(new Jail(zone.getId()));
    }

    // =========================================================
    // Property - Public
    public static final JailManager getInstance()
    {
        if (_Instance == null)
        {
        	_Instance = new JailManager();
            _Instance.load();
        }
        return _Instance;
    }

    public final Jail getJail(int jailId)
    {
        int index = getJailIndex(jailId);
        if (index >= 0) return getJails().get(index);
        return null;
    }

    public final Jail getJail(L2Object activeObject) { return getJail(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final Jail getJail(int x, int y)
    {
        int index = getJailIndex(x, y);
        if (index >= 0) return getJails().get(index);
        return null;
    }

    public final Jail getJail(String name)
    {
        int index = getJailIndex(name);
        if (index >= 0) return getJails().get(index);
        return null;
    }

    public final int getJailIndex(int jailId)
    {
        Jail jail;
        for (int i = 0; i < getJails().size(); i++)
        {
            jail = getJails().get(i);
            if (jail != null && jail.getJailId() == jailId) return i;
        }
        return -1;
    }

    public final int getJailIndex(L2Object activeObject) { return getJailIndex(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final int getJailIndex(int x, int y)
    {
        Jail jail;
        for (int i = 0; i < getJails().size(); i++)
        {
            jail = getJails().get(i);
            if (jail != null && jail.checkIfInZone(x, y)) return i;
        }
        return -1;
    }

    public final int getJailIndex(String name)
    {
        Jail jail;
        for (int i = 0; i < getJails().size(); i++)
        {
            jail = getJails().get(i);
            if (jail != null && jail.getName().equalsIgnoreCase(name.trim())) return i;
        }
        return -1;
    }

    public final FastList<Jail> getJails()
    {
        if (_Jails == null) _Jails = new FastList<Jail>();
        return _Jails;
    }
}
