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
package net.sf.l2j.gameserver.instancemanager;

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
import net.sf.l2j.L2DatabaseFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * code parts from L2_Fortress
 * author DiezelMax
 */

public class RaidPointsManager
{
	private static final Log _log = LogFactory.getLog(RaidPointsManager.class.getName());    
    private static RaidPointsManager _instance;
	protected static Map<Integer, Map<Integer, Integer>> _points;

	private RaidPointsManager()
	{
		_points = new FastMap<Integer, Map<Integer, Integer>>();
		FastList<Integer> _owners = new FastList<Integer>();
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);

			//read raidboss points
			PreparedStatement statement = con.prepareStatement("SELECT * FROM `character_raidpoints`");
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				_owners.add(rset.getInt("owner_id"));
			}

			rset.close();
			statement.close();

			for(FastList.Node<Integer> n = _owners.head(), end = _owners.tail(); (n = n.getNext()) != end;)
			{
				int ownerId = n.getValue();
				FastMap<Integer, Integer> tmpScore = new FastMap<Integer, Integer>();

				statement = con.prepareStatement("SELECT * FROM `character_raidpoints` WHERE `owner_id`=?");
				statement.setInt(1, ownerId);
				rset = statement.executeQuery();
				while(rset.next())
					if(rset.getInt("boss_id") != -1 || rset.getInt("boss_id") != 0)
						tmpScore.put(rset.getInt("boss_id"), rset.getInt("points"));

				rset.close();
				statement.close();

				_points.put(ownerId, tmpScore);
			}
		}
		catch (SQLException e)
		{
			_log.warn("RaidPointsManager: Couldnt load raid points");
		}
		catch (Exception e) {_log.error(e.getMessage(),e);}
        finally
        {
            try {con.close();} catch(Exception e) {_log.error(e.getMessage(),e);}
        }
	}

	public static RaidPointsManager getInstance()
	{
		if (_instance == null)
            _instance = new RaidPointsManager();
        
        return _instance;
	}

	public void calculateRanking()
	{
		Map<Integer, Integer> tmpRanking = new FastMap<Integer, Integer>();
		Map<Integer, Map<Integer, Integer>> tmpPoints = new FastMap<Integer, Map<Integer, Integer>>();

		for(int ownerId : _points.keySet())
		{
			Map<Integer, Integer> tmpPoint = new FastMap<Integer, Integer>();
			tmpPoint = _points.get(ownerId);
			int totalPoints = 0;

			for(int bossId : tmpPoint.keySet())
				if(bossId != -1 && bossId != 0)
					totalPoints += tmpPoint.get(bossId);

			// no need to store players w/o points
			if(totalPoints != 0)
			{
				tmpPoint.remove(0);
				tmpPoint.put(0, totalPoints);
				tmpPoints.put(ownerId, tmpPoint);

				tmpRanking.put(ownerId, totalPoints);
			}
		}

		Vector<Entry<Integer, Integer>> list = new Vector<Map.Entry<Integer, Integer>>(tmpRanking.entrySet());

		// descending
		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>(){
			public int compare(Map.Entry<Integer, Integer> entry, Map.Entry<Integer, Integer> entry1)
			{
				return entry.getValue().equals(entry1.getValue()) ? 0 : entry.getValue() < entry1.getValue() ? 1 : -1;
			}
		});

		int ranking = 1;
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

		_points.clear();
		_points = tmpPoints;
	}

	public synchronized void addPoints(int ownerId, int bossId, int points)
	{
		Map<Integer, Integer> tmpPoint = new FastMap<Integer, Integer>();
		tmpPoint = _points.get(ownerId);
		_points.remove(ownerId);

		if(tmpPoint == null || tmpPoint.isEmpty())
		{
			tmpPoint = new FastMap<Integer, Integer>();
			tmpPoint.put(bossId, points);
		}
		else
		{
			int currentPoins = tmpPoint.containsKey(bossId) ? tmpPoint.get(bossId).intValue() : 0;

			tmpPoint.remove(bossId);
			tmpPoint.put(bossId, currentPoins == 0 ? points : currentPoins + points);
		}
		_points.put(ownerId, tmpPoint);
	}

	public void cleanUp()
	{
		Connection con = null;

		for(int ownerId : _points.keySet())
			try
			{
                con = null;
				con = L2DatabaseFactory.getInstance().getConnection(con);

				Map<Integer, Integer> tmpPoint = _points.get(ownerId);
				if(tmpPoint == null || tmpPoint.isEmpty())
					continue;

				for(int bossId : tmpPoint.keySet())
				{
					if(bossId == -1 || bossId == 0)
						continue;

					int points = tmpPoint.get(bossId);
					if(points == 0)
						continue;
					
					PreparedStatement statement = con.prepareStatement("INSERT INTO `character_raidpoints` VALUES(?,?,?) ON DUPLICATE KEY UPDATE points = ?");
					statement.setInt(1, ownerId);
					statement.setInt(2, bossId);
					statement.setInt(3, points);
					statement.setInt(4, points);
					statement.execute();
					statement.close();
				}
			}
			catch (SQLException e)
			{
				_log.warn("RaidBossPointsManager: Couldnt update character_raidpoints table",e);
			}
	        finally
	        {
	            try {con.close();} catch(Exception e) {_log.error(e.getMessage(),e);}
	        }
	}

	public Map<Integer, Map<Integer, Integer>> getPoints()
	{
		return _points;
	}

	public Map<Integer, Integer> getPointsByOwnerId(int ownerId)
	{
		return _points.get(ownerId);
	}
}
