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

import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.zone.L2ZoneForm;

/**
 * A primitive rectangular zone
 * 
 *
 * @author  durgus
 */
public class ZoneCuboid extends L2ZoneForm
{
	private int _xMin, _xMax, _yMin, _yMax;
	
	public ZoneCuboid(int xMin, int xMax, int yMin, int yMax, int zMin, int zMax)
	{
		_xMin = xMin;
		_xMax = xMax;
		if (_xMin > _xMax) // switch them if alignment is wrong
		{
			_xMin = xMax; _xMax = xMin;
		}
		
		_yMin = yMin;
		_yMax = yMax;
		if (_yMin > _yMax) // switch them if alignment is wrong
		{
			_yMin = yMax; _yMax = yMin;
		}
		
		_zMin  = zMin;
		_zMax  = zMax;
		if (_zMin > _zMax) // switch them if alignment is wrong
		{
			_zMin = zMax; _zMax = zMin;
		}
	}

	@Override
	public boolean isInsideZone(int x, int y)
	{
		return (x > _xMin && x < _xMax) && (y > _yMin && y < _yMax);
	}
	
	@Override
	public boolean intersectsRectangle(int axMin, int axMax, int ayMin, int ayMax)
	{
		// Check if any point inside this rectangle
		if (isInsideZone(axMin, ayMin, (_zMax-1))) return true;
		if (isInsideZone(axMin, ayMax, (_zMax-1))) return true;
		if (isInsideZone(axMax, ayMin, (_zMax-1))) return true;
		if (isInsideZone(axMax, ayMax, (_zMax-1))) return true;
		
		// Check if any point from this rectangle is inside the other one
		if (_xMin > axMin && _xMin < axMax && _yMin > ayMin && _yMin < ayMax) return true;
		if (_xMin > axMin && _xMin < axMax && _yMax > ayMin && _yMax < ayMax) return true;
		if (_xMax > axMin && _xMax < axMax && _yMin > ayMin && _yMin < ayMax) return true;
		if (_xMax > axMin && _xMax < axMax && _yMax > ayMin && _yMax < ayMax) return true;
		
		// Horizontal lines may intersect vertical lines
		if (lineIntersectsLine(_xMin, _yMin, _xMax, _yMin, axMin, ayMin, axMin, ayMax)) return true;
		if (lineIntersectsLine(_xMin, _yMin, _xMax, _yMin, axMax, ayMin, axMax, ayMax)) return true;
		if (lineIntersectsLine(_xMin, _yMax, _xMax, _yMax, axMin, ayMin, axMin, ayMax)) return true;
		if (lineIntersectsLine(_xMin, _yMax, _xMax, _yMax, axMax, ayMin, axMax, ayMax)) return true;
		
		// Vertical lines may intersect horizontal lines
		if (lineIntersectsLine(_xMin, _yMin, _xMin, _yMax, axMin, ayMin, axMax, ayMin)) return true;
		if (lineIntersectsLine(_xMin, _yMin, _xMin, _yMax, axMin, ayMax, axMax, ayMax)) return true;
		if (lineIntersectsLine(_xMax, _yMin, _xMax, _yMax, axMin, ayMin, axMax, ayMin)) return true;
		if (lineIntersectsLine(_xMax, _yMin, _xMax, _yMax, axMin, ayMax, axMax, ayMax)) return true;
		
		return false;
	}
	
	@Override
	public double getDistanceToZone(int x, int y)
	{
		double test, shortestDist = Math.pow(_xMin-x, 2) + Math.pow(_yMin-y, 2);
		
		test = Math.pow(_xMin-x, 2) + Math.pow(_yMax-y, 2);
		if (test < shortestDist) shortestDist = test;
		
		test = Math.pow(_xMax-x, 2) + Math.pow(_yMin-y, 2);
		if (test < shortestDist) shortestDist = test;
		
		test = Math.pow(_xMax-x, 2) + Math.pow(_yMax-y, 2);
		if (test < shortestDist) shortestDist = test;
		
		return Math.sqrt(shortestDist);
	}

	@Override
	public Location getRandomLocation()
	{
		int x = _xMin + Rnd.nextInt(_xMax - _xMin);
		int y = _yMin + Rnd.nextInt(_yMax - _yMin);
		return new Location(x, y, _zMin);
	}
}
