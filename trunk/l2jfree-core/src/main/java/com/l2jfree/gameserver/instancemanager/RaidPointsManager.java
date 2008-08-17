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
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

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
	protected Map<Integer, Map<Integer, Integer>>		_points;
	protected FastMap<Integer, Map<Integer, Integer>>	_list;

	private RaidPointsManager()
	{
		_list = new FastMap<Integer, Map<Integer, Integer>>();
		FastList<Integer> chars = new FastList<Integer>();
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			//read raidboss points
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `character_raidpoints`");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				chars.add(rset.getInt("charId"));
			}
			rset.close();
			statement.close();

			for (FastList.Node<Integer> n = chars.head(), end = chars.tail(); (n = n.getNext()) != end;)
			{
				int charId = n.getValue();
				FastMap<Integer, Integer> values = new FastMap<Integer, Integer>();
				statement = con.prepareStatement("SELECT * FROM `character_raidpoints` WHERE `charId`=?");
				statement.setInt(1, charId);
				rset = statement.executeQuery();
				while (rset.next())
				{
					values.put(rset.getInt("boss_id"), rset.getInt("points"));
				}
				rset.close();
				statement.close();
				_list.put(charId, values);
			}
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
		Map<Integer, Integer> tmpPoint = null;
		if (_points == null)
			_points = new FastMap<Integer, Map<Integer, Integer>>();
		else
			tmpPoint = _points.get(ownerId);

		int currentPoints = 0;
		if (tmpPoint == null)
		{
			tmpPoint = new FastMap<Integer, Integer>();
			tmpPoint.put(bossId, points);
		}
		else
		{
			if (tmpPoint.containsKey(bossId))
				currentPoints = tmpPoint.get(bossId).intValue();
			tmpPoint.put(bossId, currentPoints + points);
		}
		updatePointsInDB(player, bossId, currentPoints + points);
		_points.put(ownerId, tmpPoint);
		_list.put(ownerId, tmpPoint);
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
			try { con.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public Map<Integer, Map<Integer, Integer>> getPoints()
	{
		return _points;
	}

	public int getPointsByOwnerId(int ownerId)
	{
		if (_points == null)
			return 0;
		Map<Integer, Integer> tmpPoint = _points.get(ownerId);
		if (tmpPoint == null)
			return 0;
		int totalPoints = 0;
		for (int value : tmpPoint.values())
			totalPoints += value;
		return totalPoints;
	}

	public Map<Integer, Integer> getList(L2PcInstance player)
	{
		return _list.get(player.getObjectId());
	}

	public final void cleanUp()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_raid_points WHERE charId > 0");
			statement.executeUpdate();
			statement.close();
			_points.clear();
			_points = new FastMap<Integer, Map<Integer, Integer>>();
			_list.clear();
			_list = new FastMap<Integer, Map<Integer, Integer>>();
		}
		catch (Exception e)
		{
			_log.error("Could not clean raid points: ", e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) { e.printStackTrace(); }
		}
	}

	public final int calculateRanking(L2PcInstance player)
	{
		Map<Integer, Integer> tmpRanking = new FastMap<Integer, Integer>();
		Map<Integer, Map<Integer, Integer>> tmpPoints = new FastMap<Integer, Map<Integer, Integer>>();
		int totalPoints;
		for(int ownerId : _list.keySet())
		{
			totalPoints = getPointsByOwnerId(ownerId);
			if(totalPoints != 0)
			{
				tmpRanking.put(ownerId, totalPoints);
			}
		}
		Vector<Entry<Integer, Integer>> list = new Vector<Map.Entry<Integer, Integer>>(tmpRanking.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>()
		{
			public int compare(Map.Entry<Integer, Integer> entry, Map.Entry<Integer, Integer> entry1)
			{
				return entry.getValue().equals(entry1.getValue()) ? 0 : entry.getValue() < entry1.getValue() ? 1 : -1;
			}
		});

		int ranking = 0;
		for(Map.Entry<Integer, Integer> entry : list)
		{
			Map<Integer, Integer> tmpPoint = new FastMap<Integer, Integer>();
			tmpPoint = tmpPoints.get(entry.getKey());
			tmpPoint.remove(-1);
			tmpPoint.put(-1, ranking);
			tmpPoints.remove(entry.getKey());
			tmpPoints.put(entry.getKey(), tmpPoint);
			ranking++;
		}
		Map<Integer, Integer> rank = tmpPoints.get(player.getObjectId());
		if (rank != null)
			return rank.get(-1);
		return 0;
	}
}
