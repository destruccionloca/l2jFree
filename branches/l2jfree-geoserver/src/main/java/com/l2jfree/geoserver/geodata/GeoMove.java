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
package com.l2jfree.geoserver.geodata;

import java.util.Vector;

import com.l2jfree.geoserver.geodata.PathFind.GeoNode;
import com.l2jfree.GeoConfig;
import com.l2jfree.geoserver.model.Location;

/**
 * @Author: Diamond
 * @Date: 20/5/2007
 * @Time: 9:57:48
 */
public class GeoMove
{
	private final GeoInterface	engine;

	public GeoMove(GeoInterface engine)
	{
		this.engine = engine;
	}

	public Vector<Location> checkMovement(int x, int y, int z, Location target)
	{
		Vector<Location> targetRecorder = new Vector<Location>();
		PathFind n = new PathFind(x, y, z, target.getX(), target.getY(), target.getZ(), engine);

		if (n.getPath() == null)
			return targetRecorder;

		if (!n.getPath().isEmpty())
		{
			targetRecorder.add(new Location(x, y, z));

			for (GeoNode p : n.getPath())
				targetRecorder.add(new Location((p.location.getX() << 4) + GeoConfig.MAP_MIN_X, (p.location.getY() << 4) + GeoConfig.MAP_MIN_Y, p.location.getZ()));

			targetRecorder.add(target);
		}

		//if(GeoConfig.PATH_CLEAN)
		pathClean(targetRecorder);

		return targetRecorder;
	}

	private void pathClean(Vector<Location> path)
	{
		int current = 0;
		int sub;
		while (current < path.size() - 2)
		{
			sub = current + 2;
			while (sub < path.size())
			{
				Location one = path.elementAt(current);
				Location two = path.elementAt(sub);
				if (engine.canMoveToTargetWithCollision(one.getX(), one.getY(), one.getZ(), two.getX(), two.getY(), two.getZ()))
				{
					path.remove(current + 1);
					sub--;
				}
				sub++;
			}
			current++;
		}
	}
}
