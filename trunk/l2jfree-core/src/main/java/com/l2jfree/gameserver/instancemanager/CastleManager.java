/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.SevenSigns;
import com.l2jfree.gameserver.model.L2Clan;
import com.l2jfree.gameserver.model.L2ClanMember;
import com.l2jfree.gameserver.model.L2ItemInstance;
import com.l2jfree.gameserver.model.L2Object;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.entity.Castle;
import com.l2jfree.gameserver.model.zone.L2SiegeDangerZone;

public class CastleManager
{
    private static final String	ADD_TRAP		= "INSERT INTO castle_zoneupgrade (level,castleId,side) VALUES (?,?,?)";
    private static final String	LOAD_TRAP		= "SELECT level FROM castle_zoneupgrade WHERE castleId=? AND side=?";
    private static final String	UPGRADE_TRAP	= "UPDATE castle_zoneupgrade SET level=? WHERE castleId=? AND side=?";
    private static final String	REMOVE_TRAPS	= "DELETE FROM castle_zoneupgrade WHERE castleId=?";
	protected static Log		_log			= LogFactory.getLog(CastleManager.class.getName());

	private static CastleManager		_instance;
	private FastMap<Integer, Castle>	_castles;

	public static final CastleManager getInstance()
	{
		if (_instance == null)
			_instance = new CastleManager();
		
		return _instance;
	}

	private static final int	_castleCirclets[]	=
													{ 0, 6838, 6835, 6839, 6837, 6840, 6834, 6836, 8182, 8183 };

	public CastleManager()
	{
		load();
	}

	public final Castle getClosestCastle(L2Object activeObject)
	{
		Castle castle = getCastle(activeObject);
		if (castle == null)
		{
			double closestDistance = Double.MAX_VALUE;
			double distance;

			for (Castle castleToCheck : getCastles().values())
			{
				if (castleToCheck == null)
					continue;
				distance = castleToCheck.getDistanceToZone(activeObject.getX(), activeObject.getY());
				if (closestDistance > distance)
				{
					closestDistance = distance;
					castle = castleToCheck;
				}
			}
		}
		return castle;
	}

	public void reload()
	{
		getCastles().clear();
		load();
	}

	private final void load()
	{
		Connection con = null;
		try
		{
			PreparedStatement statement;
			ResultSet rs;

			con = L2DatabaseFactory.getInstance().getConnection(con);

			statement = con.prepareStatement("SELECT id FROM castle ORDER BY id");
			rs = statement.executeQuery();

			while (rs.next())
			{
				int id = rs.getInt("id");
				getCastles().put(id, new Castle(id));
			}

			statement.close();

			_log.info("Loaded: " + getCastles().size() + " castles");
		}
		catch (SQLException e)
		{
			_log.warn("Exception: loadCastleData(): " + e.getMessage());
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public final Castle getCastleById(int castleId)
	{
		return getCastles().get(castleId);
	}

	public final Castle getCastle(L2Object activeObject)
	{
		return getCastle(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public final Castle getCastle(int x, int y, int z)
	{
		Castle castle;
		for (int i = 1; i <= getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.checkIfInZone(x, y, z))
				return castle;
		}
		return null;
	}

	public final Castle getCastleByName(String name)
	{
		Castle castle;
		for (int i = 1; i <= getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.getName().equalsIgnoreCase(name.trim()))
				return castle;
		}
		return null;
	}

	public final Castle getCastleByOwner(L2Clan clan)
	{
		if (clan == null)
			return null;

		Castle castle;
		for (int i = 1; i <= getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if (castle != null && castle.getOwnerId() == clan.getClanId())
				return castle;
		}
		return null;
	}

	public final FastMap<Integer, Castle> getCastles()
	{
		if (_castles == null)
			_castles = new FastMap<Integer, Castle>();
		return _castles;
	}

	public final void validateTaxes(int sealStrifeOwner)
	{
		int maxTax;
		switch (sealStrifeOwner)
		{
		case SevenSigns.CABAL_DUSK:
			maxTax = 5;
			break;
		case SevenSigns.CABAL_DAWN:
			maxTax = 25;
			break;
		default: // No owner
			maxTax = 15;
			break;
		}

		for (Castle castle : _castles.values())
			if (castle.getTaxPercent() > maxTax)
				castle.setTaxPercent(maxTax);
	}

	int	_castleId	= 1;	// from this castle

	public int getCirclet()
	{
		return getCircletByCastleId(_castleId);
	}

	public int getCircletByCastleId(int castleId)
	{
		if (castleId > 0 && castleId < 10)
			return _castleCirclets[castleId];

		return 0;
	}

	// Remove this castle's circlets from the clan
	public void removeCirclet(L2Clan clan, int castleId)
	{
		for (L2ClanMember member : clan.getMembers())
			removeCirclet(member, castleId);
	}

	public void removeCirclet(L2ClanMember member, int castleId)
	{
		if (member == null)
			return;
		L2PcInstance player = member.getPlayerInstance();
		int circletId = getCircletByCastleId(castleId);

		if (circletId != 0)
		{
			// Online Player circlet removal
			if (player != null &&
					player.getInventory() != null)
			{
				L2ItemInstance circlet = player.getInventory().getItemByItemId(circletId);
				if (circlet != null)
				{
					if (circlet.isEquipped())
						player.getInventory().unEquipItemInSlotAndRecord(circlet.getLocationSlot());
					player.destroyItemByItemId("CastleCircletRemoval", circletId, 1, player, true);
				}
				return;
			}
			// Else Offline Player circlet removal
			Connection con = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id = ? and item_id = ?");
				statement.setInt(1, member.getObjectId());
				statement.setInt(2, circletId);
				statement.execute();
				statement.close();
			}
			catch (SQLException e)
			{
				_log.error("Failed to remove castle circlets offline for player " + member.getName(), e);
			}
			finally
			{
				L2DatabaseFactory.close(con);
			}
		}
	}

	public void loadDangerZone(int castle, L2SiegeDangerZone sdz)
	{
		boolean east = (sdz.getName().endsWith("e") || sdz.getName().endsWith("i"));
		getCastleById(castle).getSiege().registerZone(sdz, east);
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(LOAD_TRAP);
			statement.setInt(1, castle);
			statement.setInt(2, east ? 1 : 2);
			ResultSet rs = statement.executeQuery();
			if (rs.next())
				getCastleById(castle).getSiege().activateZone(east, rs.getInt(1));
			statement.close();
		}
		catch (SQLException e)
		{
			_log.error("Failed to load siege danger zone [" + sdz.getName() + "] data!", e);
		}
		finally { L2DatabaseFactory.close(con); }
	}

	public void upgradeDangerZones(Castle castle, boolean east, int level, int newLevel)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			String sql = ADD_TRAP;
			if (level > 0)
				sql = UPGRADE_TRAP;
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, newLevel);
			statement.setInt(2, castle.getCastleId());
			statement.setInt(3, east ? 1 : 2);
			statement.executeUpdate();
			statement.close();
			castle.getSiege().activateZone(east, newLevel);
		}
		catch (SQLException e)
		{
			_log.error("Failed to upgrade siege danger zone data!", e);
		}
		finally { L2DatabaseFactory.close(con); }
	}

	public void removeDangerZones(int castle)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(REMOVE_TRAPS);
			statement.setInt(1, castle);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.error("Failed to delete siege danger zone data!", e);
		}
		finally { L2DatabaseFactory.close(con); }
	}
}