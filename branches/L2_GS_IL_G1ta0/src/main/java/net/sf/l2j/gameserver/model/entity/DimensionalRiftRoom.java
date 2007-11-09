/**
 * 
 */
package net.sf.l2j.gameserver.model.entity;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.instancemanager.DimensionalRiftManager.RoomType;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.tools.geometry.Point3D;

/**
 * @author G1ta0
 *
 */
public class DimensionalRiftRoom
{
    private boolean _isBoss;
    private int _roomId;
    private String _name;
    private RoomType _roomType;
    private List<L2Spawn> _roomSpawns;
    private Map<RestartType, FastList<Point3D>> _restarts;
    private IZone _zone;
    
    public  DimensionalRiftRoom(int roomId, String name)
    {
        _roomId = roomId;
        _name = name;
    }

	/** Return Id Of Dimensional Rift Room */
	public final int getId()
	{
		return _roomId;
	}

	/** Return name */
	public final String getName()
	{
		return _name;
	}
	
    public void setRoomType(RoomType roomType)
    {
        _roomType = roomType;
    }

    public RoomType getRoomType()
    {
        return _roomType;
    }

    public void setIsBoss(boolean isBoss)
    {
        _isBoss = isBoss;
    }

    public boolean isBoss()
    {
        return _isBoss;
    }
    
    public boolean isInRoom(L2Character character)
    {
        return (getZone() != null) && (getZone().checkIfCharacterInZone(character));
    }
    
	public void setZone(IZone zone)
	{
		_zone = zone;
	}
	
	public IZone getZone()
	{
		return _zone;
	}
	
	public void addRestartPoint(RestartType restartType, Point3D point)
	{
		if(_restarts == null)
			_restarts = new FastMap<RestartType, FastList<Point3D>>();

		if(_restarts.get(restartType) == null)
			_restarts.put(restartType, new FastList<Point3D>());

		_restarts.get(restartType).add(point);
	}

	public Location getRestartPoint(RestartType restartType)
	{
		if(_restarts != null)
		{
			if(_restarts.get(restartType) != null)
			{
				Point3D point = _restarts.get(restartType).get(Rnd.nextInt(_restarts.get(restartType).size()));
				return new Location(point.getX(), point.getY(), point.getZ());
			}
		}

		return null;
	}
	
	public Location getTeleport()
	{
		return getRestartPoint(RestartType.RestartNormal);
	}

    public List<L2Spawn> getSpawns()
    {
    	if (_roomSpawns== null) _roomSpawns = new FastList<L2Spawn>();
        return _roomSpawns;
    }

    public void spawn()
    {
        for (L2Spawn spawn : _roomSpawns)
        {
            spawn.doSpawn();
            spawn.startRespawn();
        }
    }

    public void unspawn()
    {
        for (L2Spawn spawn : _roomSpawns)
        {
            spawn.stopRespawn();
            if (spawn.getLastSpawn() != null)
                spawn.getLastSpawn().deleteMe();
        }
    }

}
