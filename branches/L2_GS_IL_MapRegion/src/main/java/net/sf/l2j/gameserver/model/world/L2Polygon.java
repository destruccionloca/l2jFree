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
package net.sf.l2j.gameserver.model.world;

import java.io.Serializable;

/**
 *
 * @author  tReXpert
 */
public class L2Polygon implements Serializable
{
	private int _nPoints;
	private int _xPoints[];
	private int _yPoints[];
	
	private static final long serialVersionUID = -6460061437900069969L;
	
	public L2Polygon()
	{
		_xPoints = new int[3];
		_yPoints = new int[3];
	}
	
    public void addPoint(int x, int y)
    {
    	if (_nPoints == _xPoints.length)
    	{
    		int tmp[];

    	    tmp = new int[_nPoints + 1];
    	    System.arraycopy(_xPoints, 0, tmp, 0, _nPoints);
    	    _xPoints = tmp;

    	    tmp = new int[_nPoints + 1];
    	    System.arraycopy(_yPoints, 0, tmp, 0, _nPoints);
    	    _yPoints = tmp;
    	}
    	_xPoints[_nPoints] = x;
    	_yPoints[_nPoints] = y;
    	_nPoints++;
    }
    
    public boolean contains(int x, int y)
    {
    	return contains((double) x, (double) y);
    }
    
    public boolean contains(double x, double y)
    {
    	if (_nPoints <= 2)
	        return false;
    	int hits = 0;

    	int lastx = _xPoints[_nPoints - 1];
    	int lasty = _yPoints[_nPoints - 1];
    	int curx, cury;

    	// Walk the edges of the polygon
    	for (int i = 0; i < _nPoints; lastx = curx, lasty = cury, i++)
    	{
    		curx = _xPoints[i];
    		cury = _yPoints[i];

    		if (cury == lasty) continue;

    		int leftx;
    		if (curx < lastx)
    		{
    			if (x >= lastx) continue;
    			leftx = curx;
    		}
    		else
    		{
    			if (x >= curx) continue;
    			leftx = lastx;
    		}

    		double test1, test2;
    		if (cury < lasty)
    		{
    			if (y < cury || y >= lasty) continue;
    			
    			if (x < leftx)
    			{
    				hits++;
    				continue;
    			}
    			test1 = x - curx;
    			test2 = y - cury;
    		}
    		else
    		{
    			if (y < lasty || y >= cury) continue;
    			
    			if (x < leftx)
    			{
    				hits++;
    				continue;
    			}
    			test1 = x - lastx;
    			test2 = y - lasty;
    		}
    		
    		if (test1 < test2 / (lasty - cury) * (lastx - curx)) hits++;
    	}

    	return (hits & 1) != 0;
    }

    public int[] getYPoints()
    {
    	return _yPoints;
    }
    
    public int[] getXPoints()
    {
    	return _xPoints;
    }
    
    public int size()
    {
    	return _nPoints;
    }
}
