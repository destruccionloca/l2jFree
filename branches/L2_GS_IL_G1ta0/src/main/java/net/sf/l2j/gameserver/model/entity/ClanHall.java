/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.IZone;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.RestartType;
import net.sf.l2j.gameserver.model.zone.ZoneEnum.ZoneType;
import net.sf.l2j.gameserver.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.templates.StatsSet;
import net.sf.l2j.tools.geometry.Point3D;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClanHall
{
	protected static Log _log = LogFactory.getLog(ClanHall.class.getName());

	private static final String RESTORE_CLANHALL = "SELECT clan_id, paidUntil FROM clanhall WHERE id = ?";
	private static final String UPDATE_CLANHALL = "UPDATE clanhall SET clan_id=?, paidUntil=? WHERE id = ?";
	private static final String RESTORE_CLANHALL_FUNCTIONS = "SELECT type, lvl, lease, rate, endTime FROM clanhall_functions WHERE hall_id = ?";
	private static final String DELETE_CLANHALL_FUNCTION = "DELETE FROM clanhall_functions WHERE hall_id = ? AND type = ?";
	private static final String DELETE_CLANHALL_FUNCTIONS = "DELETE FROM clanhall_functions WHERE hall_id = ?";
	private static final String UPDATE_CLANHALL_FUNCTION = "REPLACE INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)";

	private static final int _chFeeRate = 604800000; // clan hall fee rate, 7 days

	private int _clanHallId;
	private List<L2DoorInstance> _doors;
	private List<String> _doorDefault;
	private String _name;
	private int _ownerId;
	protected long _paidUntil;
	private StatsSet _settings;
	private Map<Integer, ClanHallFunction> _functions;
	private Map<RestartType, FastList<Point3D>> _restarts;
	private Map<ZoneType, IZone> _zones;

	/** Clan Hall Functions */
	public static final int FUNC_TELEPORT = 1;
	public static final int FUNC_ITEM_CREATE = 2;
	public static final int FUNC_RESTORE_HP = 3;
	public static final int FUNC_RESTORE_MP = 4;
	public static final int FUNC_RESTORE_EXP = 5;
	public static final int FUNC_SUPPORT = 6;
	public static final int FUNC_DECO_FRONTPLATEFORM = 7;
	public static final int FUNC_DECO_CURTAINS = 8;

	public class ClanHallFunction
	{
		private int _type;
		private int _lvl;
		protected int _fee;
		protected int _tempFee;
		private long _rate;
		private long _endDate;

		public ClanHallFunction(int type, int lvl, int lease, int tempLease, long rate, long time)
		{
			_type = type;
			_lvl = lvl;
			_fee = lease;
			_tempFee = tempLease;
			_rate = rate;
			_endDate = time;
			initializeFeeTask();
		}

		public int getType()
		{
			return _type;
		}

		public int getLvl()
		{
			return _lvl;
		}

		public int getLease()
		{
			return _fee;
		}

		public long getRate()
		{
			return _rate;
		}

		public long getEndTime()
		{
			return _endDate;
		}

		public void setLvl(int lvl)
		{
			_lvl = lvl;
		}

		public void setLease(int lease)
		{
			_fee = lease;
		}

		public void setEndTime(long time)
		{
			_endDate = time;
		}

		private void initializeFeeTask()
		{
			if(isFree())
				return;
			long currentTime = System.currentTimeMillis();
			if(_endDate > currentTime)
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), _endDate - currentTime);
			else
				ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), 0);
		}

		private class FunctionTask implements Runnable
		{
			public FunctionTask()
			{}

			public void run()
			{
				try
				{
					if(isFree())
						return;

					if(ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= getLease())
					{
						int fee = getLease();

						if(getEndTime() <= 0)
							fee = _tempFee;

						setEndTime(System.currentTimeMillis() + getRate());
						ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_function_fee", 57, fee, null, null);
						save();
						ThreadPoolManager.getInstance().scheduleGeneral(new FunctionTask(), getRate());
					}
					else
						removeFunction(getType());
				}
				catch (Throwable t)
				{}
			}
		}

		public void save()
		{
			java.sql.Connection con = null;
			try
			{
				PreparedStatement statement;

				con = L2DatabaseFactory.getInstance().getConnection(con);

				statement = con.prepareStatement(UPDATE_CLANHALL_FUNCTION);
				statement.setInt(1, getClanHallId());
				statement.setInt(2, getType());
				statement.setInt(3, getLvl());
				statement.setInt(4, getLease());
				statement.setLong(5, getRate());
				statement.setLong(6, getEndTime());
				statement.execute();
				statement.close();
			}
			catch (Exception e)
			{
				_log.error("Error while updating clan hall function: " + e.getMessage(), e);
			}
			finally
			{
				try
				{
					con.close();
				}
				catch (Exception e)
				{}
			}
		}
	}

	public ClanHall(int clanHallId, String name)
	{
		_clanHallId = clanHallId;
		_name = name;

		_doorDefault = new FastList<String>();

		load();
		if(!isFree())
		{
			initializeFeeTask();
			loadFunctions();
		}
	}

	/** Return Id Of Clan hall */
	public final int getClanHallId()
	{
		return _clanHallId;
	}

	/** Return name */
	public final String getName()
	{
		return _name;
	}

	/** Return OwnerId */
	public final int getOwnerId()
	{
		return _ownerId;
	}

	/** Return lease*/
	public final int getLease()
	{
		return getSettings().getInteger("defaultLease", 50000);
	}

	/** Return Desc */
	public final String getDesc()
	{
		return getSettings().getString("description", "");
	}

	/** Return Location */
	public final String getLocation()
	{
		return getSettings().getString("location", "Unknown");
	}

	/** Return master castle id */
	public final int getCastleId()
	{
		return getSettings().getInteger("masterCastleid", 0);
	}
	
	/** Return PaidUntil */
	public final long getPaidUntil()
	{
		return _paidUntil;
	}

	/** Return Grade */
	public final int getGrade()
	{
		return getSettings().getInteger("grade", 0);
	}

	/** Return if clan hall is free */
	public final boolean isFree()
	{
		return (getOwnerId() > 0);
	}

	public final Map<Integer, ClanHallFunction> getFunctions()
	{
		if(_functions == null)
			_functions = new FastMap<Integer, ClanHallFunction>();

		return _functions;
	}

	public final ClanHallFunction getFunction(int type)
	{
		return getFunctions().get(type);
	}

	/** Return all DoorInstance */
	public final List<L2DoorInstance> getDoors()
	{
		if(_doors == null)
			_doors = new FastList<L2DoorInstance>();
		return _doors;
	}

	/** Return Door */
	public final L2DoorInstance getDoor(int doorId)
	{
		if(doorId <= 0)
			return null;
		for(int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			if(door.getDoorId() == doorId)
				return door;
		}
		return null;
	}

	public void setSettings(StatsSet settings)
	{
		_settings = settings;
	}

	public StatsSet getSettings()
	{
		return _settings;
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

	public void addZone(IZone zone)
	{
		if(_zones == null)
			_zones = new FastMap<ZoneType, IZone>();

		_zones.put(zone.getZoneType(), zone);
	}

	public IZone getZone(ZoneType zoneType)
	{
		if(_zones == null)
			return null;
		else
			return _zones.get(zoneType);
	}

	/** Free this clan hall */
	public synchronized void free()
	{
		if(!isFree())
		{
			L2Clan clan = ClanTable.getInstance().getClan(getOwnerId());
			if(clan != null)
				clan.setHasHideout(0);
		}

		_ownerId = 0;
		_paidUntil = 0;

		// update database
		removeFunctions();
		save();
	}

	/** Set owner if clan hall is free */
	public void setOwner(L2Clan clan)
	{
		if(!isFree() || clan == null || clan.getClanId() == getOwnerId())
			return;

		_ownerId = clan.getClanId();
		initializeFeeTask();

		// announce to online member
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
	}

	/** Respawn all doors */
	public void spawnDoor()
	{
		spawnDoor(false);
	}

	/** Respawn all doors */
	public void spawnDoor(boolean isDoorWeak)
	{
		for(int i = 0; i < getDoors().size(); i++)
		{
			L2DoorInstance door = getDoors().get(i);
			if(door.getStatus().getCurrentHp() <= 0)
			{
				door.decayMe(); // Kill current if not killed already
				door = DoorTable.parseList(_doorDefault.get(i));
				if(isDoorWeak)
					door.getStatus().setCurrentHp(door.getMaxHp() / 2);
				door.spawnMe(door.getX(), door.getY(), door.getZ());
				getDoors().set(i, door);
			}
			else if(door.getOpen() == 0)
				door.closeMe();
		}
	}

	/** Open or Close Door */
	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if(activeChar != null && activeChar.getClanId() == getOwnerId())
			openCloseDoor(doorId, open);
	}

	public void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}

	public void openCloseDoor(L2DoorInstance door, boolean open)
	{
		if(door != null)
			if(open)
				door.openMe();
			else
				door.closeMe();
	}

	public void openCloseDoors(L2PcInstance activeChar, boolean open)
	{
		if(activeChar != null && activeChar.getClanId() == getOwnerId())
			openCloseDoors(open);
	}

	public void openCloseDoors(boolean open)
	{
		for(L2DoorInstance door : getDoors())
		{
			if(door != null)
				if(open)
					door.openMe();
				else
					door.closeMe();
		}
	}

	/** Banish Foreigner */
	public void banishForeigner(L2PcInstance activeChar)
	{
	//TODO not done !!!
	/*
	// Get players from this and nearest world regions
	for(L2PlayableInstance player : L2World.getInstance().getVisiblePlayable(activeChar))
	{
		if(!(player instanceof L2PcInstance))
			continue;
		// Skip if player is in clan
		if(((L2PcInstance) player).getClanId() == getOwnerId())
			continue;
		if(checkIfInZone(player))
			player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
	}*/
	}

	/** Restore clan hall owner and paid period from DB */
	private final void load()
	{
		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement(RESTORE_CLANHALL);
			statement.setInt(1, getClanHallId());
			rs = statement.executeQuery();
			if (rs.next())
			{
				_ownerId = rs.getInt("clan_id");
				_paidUntil = rs.getLong("paidUntil");
				statement.close();

				if(getOwnerId() > 0)
				{
					L2Clan clan = ClanTable.getInstance().getClan(getOwnerId());

					if(clan == null)
					{
						free();
						AuctionManager.getInstance().initNPC(getClanHallId());
					}
					else
						clan.setHasHideout(getClanHallId());
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Error while loading clan hall " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{}
		}
	}

	/** Restore all clan hall functions from DB */
	private void loadFunctions()
	{
		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement(RESTORE_CLANHALL_FUNCTIONS);
			statement.setInt(1, getClanHallId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				getFunctions().put(rs.getInt("type"), new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), 0, rs.getLong("rate"), rs.getLong("endTime")));
			}
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error while loading clan hall functions: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{}
		}
	}

	/** Remove all clan hall functions specified type from DB */
	public void removeFunction(int functionType)
	{
		getFunctions().remove(functionType);
		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement(DELETE_CLANHALL_FUNCTION);
			statement.setInt(1, getClanHallId());
			statement.setInt(2, functionType);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error while removing clan hall functions: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{}
		}
	}

	/** Remove all clan hall functions  from DB */
	public void removeFunctions()
	{
		getFunctions().clear();

		java.sql.Connection con = null;
		try
		{
			PreparedStatement statement;
			con = L2DatabaseFactory.getInstance().getConnection(con);
			statement = con.prepareStatement(DELETE_CLANHALL_FUNCTIONS);
			statement.setInt(1, getClanHallId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error while removing clan hall functions: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{}
		}
	}

	/** Update Function */
	public boolean updateFunctions(int type, int lvl, int lease, long rate, boolean addNew)
	{
		L2Clan clan = ClanTable.getInstance().getClan(getOwnerId());

		if(clan == null)
			return false;

		if(addNew)
		{
			if(clan.getWarehouse().getAdena() < lease)
				return false;
			getFunctions().put(type, new ClanHallFunction(type, lvl, lease, 0, rate, 0));
		}
		else
		{
			if(lvl == 0 && lease == 0)
				removeFunction(type);
			else
			{
				int diffLease = lease - getFunctions().get(type).getLease();

				if(diffLease > 0)
				{
					if(clan.getWarehouse().getAdena() < diffLease)
						return false;

					getFunctions().remove(type);
					getFunctions().put(type, new ClanHallFunction(type, lvl, lease, diffLease, rate, -1));
				}
				else
				{
					getFunctions().get(type).setLease(lease);
					getFunctions().get(type).setLvl(lvl);
					getFunctions().get(type).save();
				}
			}
		}
		return true;
	}

	/** Save clan hall owner and paid period in DB */
	public void save()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;
			statement = con.prepareStatement(UPDATE_CLANHALL);
			statement.setInt(1, _ownerId);
			statement.setLong(2, _paidUntil);
			statement.setInt(3, _clanHallId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error while updating clan hall: " + e.getMessage(), e);
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{}
		}
	}

	/** Initialize Fee Task */
	private void initializeFeeTask()
	{
		long currentTime = System.currentTimeMillis();

		if(_paidUntil > currentTime)
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _paidUntil - currentTime);
		else
			ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), 0);
	}

	/** Fee Task */
	private class FeeTask implements Runnable
	{
		public FeeTask()
		{}

		public void run()
		{
			try
			{
				if(isFree())
					return;

				if(ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= getLease())
				{
					_paidUntil = System.currentTimeMillis() + _chFeeRate;
					ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_rental_fee", 57, getLease(), null, null);
					save();
					ThreadPoolManager.getInstance().scheduleGeneral(new FeeTask(), _chFeeRate);
				}
				else
				{
					free();
					AuctionManager.getInstance().initNPC(getClanHallId());
				}
			}
			catch (Throwable t)
			{}
		}
	}
}
