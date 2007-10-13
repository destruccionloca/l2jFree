/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.model.zone.form;

import javolution.util.FastList;

import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.zone.L2ZoneForm;
import net.sf.l2j.tools.geometry.Point3D;

/**
 * A not so primitive npoly zone
 * 
 *
 * @author  durgus
 */
public class ZoneNPoly extends L2ZoneForm
{
	private int[] _x;
	private int[] _y;
	
	private int _xMin, _xMax;
	private int _yMin, _yMax;
	private int _zMin, _zMax;
	private Point3D _min;
	private Point3D _max;
	
	private FastList<Point3D> _planePoints;
	
	public ZoneNPoly(int zMin, int zMax)
	{
		_zMin = zMin;
		_zMax = zMax;
	}

	public boolean isInsideZone(int x, int y)
	{
		boolean inside = false;
		for (int i = 0, j = _x.length-1; i < _x.length; j = i++)
		{
			if ( (((_y[i] <= y) && (y < _y[j])) || ((_y[j] <= y) && (y < _y[i]))) && (x < (_x[j] - _x[i]) * (y - _y[i]) / (_y[j] - _y[i]) + _x[i]) )
			{
				inside = !inside;
			}
		}
		return inside;
	}
	
	@Override
	public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2)
	{
		int tX, tY, uX, uY;
		
		// First check if a point of the polygon lies inside the rectangle
		if (_x[0] > ax1 && _x[0] < ax2 && _y[0] > ay1 && _y[0] < ay2) return true;
		
		// Or a point of the rectangle inside the polygon
		if (isInsideZone(ax1, ay1, (_zMax - 1))) return true;
		
		// If the first point wasnt inside the rectangle it might still have any line crossing any side
		// of the rectangle
		
		// Check every possible line of the polygon for a collision with any of the rectangles side
		for (int i = 0; i < _y.length; i++)
		{
			tX = _x[i];
			tY = _y[i];
			uX = _x[(i+1) % _x.length];
			uY = _y[(i+1) % _x.length];
			
			// Check if this line intersects any of the four sites of the rectangle
			if (lineIntersectsLine(tX, tY, uX, uY, ax1, ay1, ax1, ay2)) return true;
			if (lineIntersectsLine(tX, tY, uX, uY, ax1, ay1, ax2, ay1)) return true;
			if (lineIntersectsLine(tX, tY, uX, uY, ax2, ay2, ax1, ay2)) return true;
			if (lineIntersectsLine(tX, tY, uX, uY, ax2, ay2, ax2, ay1)) return true;
		}
		
		return false;
	}
	
	@Override
	public double getDistanceToZone(int x, int y)
	{
		double test, shortestDist = Math.pow(_x[0]-x, 2) + Math.pow(_y[0]-y, 2);
		
		for (int i = 1; i < _y.length; i++)
		{
			test = Math.pow(_x[i]-x, 2) + Math.pow(_y[i]-y, 2);
			if (test < shortestDist) shortestDist = test;
		}

		return Math.sqrt(shortestDist);
	}

	public void addPoint(Point3D point)
	{
		if (_planePoints == null) _planePoints = new FastList<Point3D>();
		if (_min == null) _min = point;
		if (_max == null) _max = point;
		
		_min = new Point3D( Math.min(_min.getX(), point.getX()),
							Math.min(_min.getY(), point.getY()),
							Math.min(_min.getZ(), point.getZ()) );
		_max = new Point3D( Math.max(_max.getX(), point.getX()),
							Math.max(_max.getY(), point.getY()),
							Math.max(_max.getZ(), point.getZ()) );

		_planePoints.add(point);
	}

	public void finish()
	{
		int size = _planePoints.size();
		int[] x = new int[size];
		int[] y = new int[size];
		
		for (int i = 0; i < size; i++)
		{
			x[i] = _planePoints.get(i).getX();
			y[i] = _planePoints.get(i).getY();
		}
		
		_planePoints = null;
	}

	@Override
	public Location getRandomLocation()
	{
		int x, y;
		do
		{
			x = _xMin + Rnd.nextInt(_xMax - _xMin);
			y = _yMin + Rnd.nextInt(_yMax - _yMin);
		}
		while(!isInsideZone(x, y));

		return new Location(x, y, _zMin);
	}
}
