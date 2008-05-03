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
package net.sf.l2j.gameserver;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2Territory;
import net.sf.l2j.gameserver.util.SqlUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Territory
{
	private final static Log						_log		= LogFactory.getLog(Territory.class.getName());
	private static final Territory					_instance	= new Territory();
	private static FastMap<Integer, L2Territory>	_territory;
	
	public static Territory getInstance()
	{
		return _instance;
	}
	
	private Territory()
	{
		// load all data at server start
		reload_data();
	}
	
	public int[] getRandomPoint(int terr)
	{
		return _territory.get(terr).getRandomPoint();
	}
	
	public int getProcMax(int terr)
	{
		return _territory.get(terr).getProcMax();
	}
	
	public void reload_data()
	{
		_territory = new FastMap<Integer, L2Territory>();
		
		Integer[][] point = SqlUtils.get2DIntArray(new String[] { "loc_id", "loc_x", "loc_y", "loc_zmin", "loc_zmax", "proc" }, "locations", "loc_id > 0");
		for (Integer[] row : point)
		{
			// _log.info("row = "+row[0]);
			Integer terr = row[0];
			if (terr == null)
			{
				_log.warn("Null territory!");
				continue;
			}
			
			if (_territory.get(terr) == null)
			{
				L2Territory t = new L2Territory(terr);
				_territory.put(terr, t);
			}
			_territory.get(terr).add(row[1], row[2], row[3], row[4], row[5]);
		}
	}
}
