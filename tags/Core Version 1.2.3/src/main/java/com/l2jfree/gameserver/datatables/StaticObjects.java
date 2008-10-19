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
package com.l2jfree.gameserver.datatables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.gameserver.idfactory.IdFactory;
import com.l2jfree.gameserver.model.actor.instance.L2StaticObjectInstance;

public class StaticObjects
{
	private final static Log							_log	= LogFactory.getLog(StaticObjects.class.getName());

	private static StaticObjects						_instance;
	private FastMap<Integer, L2StaticObjectInstance>	_staticObjects;

	public static StaticObjects getInstance()
	{
		if (_instance == null)
			_instance = new StaticObjects();
		return _instance;
	}

	public StaticObjects()
	{
		_staticObjects = new FastMap<Integer, L2StaticObjectInstance>();
		parseData();
		_log.info("StaticObject: Loaded " + _staticObjects.size() + " StaticObject Templates.");
	}

	private void parseData()
	{
		LineNumberReader lnr = null;
		try
		{
			File doorData = new File(Config.DATAPACK_ROOT, "data/staticobjects.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(doorData)));

			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.trim().length() == 0 || line.startsWith("#"))
					continue;

				L2StaticObjectInstance obj = parse(line);
				_staticObjects.put(obj.getStaticObjectId(), obj);
			}
		}
		catch (FileNotFoundException e)
		{
			_log.warn("staticobjects.csv is missing in data folder");
		}
		catch (Exception e)
		{
			_log.warn("error while creating StaticObjects table " + e);
		}
		finally { try { if (lnr != null) lnr.close(); } catch (Exception e) { e.printStackTrace(); } }
	}

	public static L2StaticObjectInstance parse(String line)
	{
		StringTokenizer st = new StringTokenizer(line, ";");

		st.nextToken(); //Pass over static object name (not used in server)

		int id = Integer.parseInt(st.nextToken());
		int x = Integer.parseInt(st.nextToken());
		int y = Integer.parseInt(st.nextToken());
		int z = Integer.parseInt(st.nextToken());
		int type = Integer.parseInt(st.nextToken());
		String texture = st.nextToken();
		int map_x = Integer.parseInt(st.nextToken());
		int map_y = Integer.parseInt(st.nextToken());

		L2StaticObjectInstance obj = new L2StaticObjectInstance(IdFactory.getInstance().getNextId());
		obj.setType(type);
		obj.setStaticObjectId(id);
		obj.getPosition().setXYZ(x, y, z);
		obj.setMap(texture, map_x, map_y);
		obj.spawnMe();

		return obj;
	}
}