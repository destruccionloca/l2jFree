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
package com.l2jfree.geoserver.model;

/**
 * coded by Balancer balancer@balancer.ru http://balancer.ru version 0.1,
 * 2005-03-12
 */

import java.io.Serializable;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("serial")
public class L2Territory implements Serializable
{
	private final static Log	_log	= LogFactory.getLog(L2Territory.class.getName());

	protected class Point implements Serializable
	{
		protected int	_x, _y, _zmin, _zmax;

		Point(int x, int y, int zmin, int zmax)
		{
			_x = x;
			_y = y;
			_zmin = zmin;
			_zmax = zmax;
		}
	}

	private FastList<Point>	_points;
	@SuppressWarnings("unused")
	private String			_name;
	private int				_xMin;
	private int				_xMax;
	private int				_yMin;
	private int				_yMax;
	private int				_zMin;
	private int				_zMax;

	public L2Territory(String name)
	{
		_points = new FastList<Point>();
		_name = name;
		_xMin = 999999;
		_xMax = -999999;
		_yMin = 999999;
		_yMax = -999999;
		_zMin = 999999;
		_zMax = -999999;
	}

	public void add(int x, int y, int zmin, int zmax)
	{
		_points.add(new Point(x, y, zmin, zmax));
		if (x < _xMin)
			_xMin = x;
		if (y < _yMin)
			_yMin = y;
		if (x > _xMax)
			_xMax = x;
		if (y > _yMax)
			_yMax = y;
		if (zmin < _zMin)
			_zMin = zmin;
		if (zmax > _zMax)
			_zMax = zmax;
	}

	public void print()
	{
		for (Point p : _points)
			_log.debug("(" + p._x + "," + p._y + ")");
	}

	public boolean isIntersect(int x, int y, Point p1, Point p2)
	{
		double dy1 = p1._y - y;
		double dy2 = p2._y - y;

		if (Math.signum(dy1) == Math.signum(dy2))
			return false;

		double dx1 = p1._x - x;
		double dx2 = p2._x - x;

		if (dx1 >= 0 && dx2 >= 0)
			return true;

		if (dx1 < 0 && dx2 < 0)
			return false;

		double dx0 = (dy1 * (p1._x - p2._x)) / (p1._y - p2._y);

		return dx0 <= dx1;
	}

	public boolean isInside(int x, int y)
	{
		int intersect_count = 0;
		for (int i = 0; i < _points.size(); i++)
		{
			Point p1 = _points.get(i > 0 ? i - 1 : _points.size() - 1);
			Point p2 = _points.get(i);

			if (isIntersect(x, y, p1, p2))
				intersect_count++;
		}

		return intersect_count % 2 == 1;
	}

	public int getXmax()
	{
		return _xMax;
	}

	public int getXmin()
	{
		return _xMin;
	}

	public int getYmax()
	{
		return _yMax;
	}

	public int getYmin()
	{
		return _yMin;
	}

	public int getZmin()
	{
		return _zMin;
	}

	public int getZmax()
	{
		return _zMax;
	}
}