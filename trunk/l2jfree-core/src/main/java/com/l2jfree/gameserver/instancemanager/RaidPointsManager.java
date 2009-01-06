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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;

/*
 * code parts from L2_Fortress
 * author DiezelMax
 */
public class RaidPointsManager
{
	private static final Log									_log	= LogFactory.getLog(RaidPointsManager.class.getName());
	private static RaidPointsManager							_instance;
	protected Map<Integer, PointList>							_points;

	public static class PointList extends FastMap<Integer, Integer>
	{
		private static final long serialVersionUID = -1L;
		public int scoreSum = 0;
		public int ranking = 0;
	}

	private RaidPointsManager()
	{
		_points = new FastMap<Integer, PointList>();
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			// Read raidboss points
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `character_raid_points`");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				int charId = rset.getInt("charId");
				int bossId = rset.getInt("boss_id");
				int points = rset.getInt("points");
				PointList list = _points.get(charId);
				if (list == null)
				{
					list = new PointList();
					_points.put(charId, list);
				}
				list.put(bossId, points);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("RaidPointsManager: Couldn't load raid points");
		}
		catch (Exception e)
		{
			_log.error(e.getMessage(), e);
		}
		finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }

		calculateRanking();
	}

	public static RaidPointsManager getInstance()
	{
		if (_instance == null)
			_instance = new RaidPointsManager();
		return _instance;
	}

	public void addPoints(L2PcInstance player, int bossId, int points)
	{
		int ownerId = player.getObjectId();
		int currentPoints = 0;
		PointList list = _points.get(ownerId);
		if (list == null)
		{
			list = new PointList();
			list.put(bossId, points);
			_points.put(ownerId, list);
		}
		else
		{
			if (list.containsKey(bossId))
				currentPoints = list.get(bossId).intValue();
			list.put(bossId, currentPoints + points);
		}
		updatePointsInDB(player, bossId, currentPoints + points);
	}

	public final void updatePointsInDB(L2PcInstance player, int raidId, int points)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("REPLACE INTO character_raid_points (`charId`,`boss_id`,`points`) VALUES (?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, raidId);
			statement.setInt(3, points);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not update char raid points:", e);
		}
		finally
		{
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public PointList getPlayerEntry(L2PcInstance player)
	{
		return _points.get(player.getObjectId());
	}

	public final void cleanUp()
	{
		_points.clear();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_raid_points;");
			statement.executeUpdate();
			statement.close();
			_points.clear();
		}
		catch (Exception e)
		{
			_log.error("Could not clean raid points: ", e);
		}
		finally
		{
			try {
				if (con != null)
					con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void calculateRanking()
	{
		List<PointList> lists = new FastList<PointList>();
		lists.addAll(_points.values());

		//  Calculate sum
		for (PointList pl : lists)
		{
			pl.scoreSum = 0;
			for (int bossScore : pl.values())
				pl.scoreSum += bossScore;
		}

		// Sort
		Collections.sort(lists, new Comparator<PointList>()
		{
			public int compare(PointList p1, PointList p2)
			{
				if (p1.scoreSum < p2.scoreSum)
					return 1;
				if (p1.scoreSum == p2.scoreSum)
					return 0;
				return -1;
			}
		});

		// Insert ranking
		int r = 1;
		for (PointList pl : lists)
		{
			pl.ranking = r++;
		}
	}
}
