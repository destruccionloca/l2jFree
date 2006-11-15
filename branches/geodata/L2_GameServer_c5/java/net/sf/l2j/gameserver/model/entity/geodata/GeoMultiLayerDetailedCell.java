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
package net.sf.l2j.gameserver.model.entity.geodata;

import javolution.util.FastList;

/**
 * This class represents single-layered cells that require details.
 * It contains an array with info about each subcell.
 * 
 * @author  Fulminus
 */
public class GeoMultiLayerDetailedCell extends GeoCell
{
    private FastList<GeoSubCell>[][] details;
    
    public GeoMultiLayerDetailedCell()
    {
        details = new FastList[8][8];
    }
    
    public GeoSubCell getNearestFloorSubcell(int xWithin, int yWithin, int z)
    {
        FastList layers = details[(xWithin & 0x07F)>>>4][(yWithin & 0x07F)>>>4];
        
        // initialize the nearest(floor) and the lowest subcells.
        // lowest will only be used if the passed z is lower than all floors.
        GeoSubCell nearestSubCell = null;
        if (layers != null)
        {
	        GeoSubCell lowestSubCell = (GeoSubCell)layers.getFirst();
	        for(GeoSubCell oneLayer: (FastList<GeoSubCell>)layers)
	        {
	            // if this is a valid floor for the passed z value
	            if (oneLayer.getZ() <= z)
	            {
	                // if it is the first valid value, store a reference to it
	                if (nearestSubCell == null)
	                    nearestSubCell = oneLayer;
	                // otherwise, check if it's a closer floor than the last good value
	                else
	                    if ((oneLayer.getZ() <= z) && (nearestSubCell.getZ() < oneLayer.getZ()))
	                        nearestSubCell = oneLayer;
	            }
	
	            // As long as no valid floor is found, keep track of the lowest subcell.
	            if (nearestSubCell == null)
	                if (oneLayer.getZ() < lowestSubCell.getZ())
	                    lowestSubCell = oneLayer;
	        }
	        
	        // Now return the nearest floor (if one exists) or the lowest subcell otherwise. 
	        if (nearestSubCell == null)
	            return lowestSubCell;
	        return nearestSubCell;
        }
        return new GeoSubCell((short)z,(byte)0);
    }    
    
    public GeoSubCell getNearestSubcell(int xWithin, int yWithin, int z)
    {
        FastList layers = details[(xWithin & 0x07F)>>>4][(yWithin & 0x07F)>>>4];
        
        // initialize the nearest(floor) and the lowest subcells.
        // lowest will only be used if the passed z is lower than all floors.
        GeoSubCell nearestSubCell = null;
        int smallestDist = 9999999;
        if (layers != null)
        {
	        GeoSubCell lowestSubCell = (GeoSubCell)layers.getFirst();
	        for(GeoSubCell oneLayer: (FastList<GeoSubCell>)layers)
	        {
	            // if this is a valid floor for the passed z value
	            if ( ((oneLayer.getZ()-z) < smallestDist) || ((z-oneLayer.getZ()) < smallestDist) )
                    nearestSubCell = oneLayer;
	        }
	        return nearestSubCell;
        }
        return new GeoSubCell((short)z,(byte)0);
    }    
    
    public void addSubcellInfo(short z, byte NSEW, short xIndex, short yIndex)
    {
        short zInfo = (short)((z & 0x0fff0) | (NSEW & 0x0f));
        setSubcellInfo(zInfo, xIndex, yIndex);
    }

    public void setSubcellInfo(short zInfo, short xIndex, short yIndex)
    {
        if (details[xIndex][yIndex] == null)
            details[xIndex][yIndex] = new FastList<GeoSubCell>();
            
        if(xIndex >=0 && xIndex<8 && yIndex>=0 && yIndex<8)
            details[xIndex][yIndex].add(new GeoSubCell(zInfo));
    }

    public void addSubcellInfo(short zInfo, short oneLineIndex)
    {
        short xIndex = (short)(oneLineIndex/8);
        short yIndex = (short)(oneLineIndex%8);
        
        if (details[xIndex][yIndex] == null)
            details[xIndex][yIndex] = new FastList<GeoSubCell>();
            
        if(xIndex >=0 && xIndex<8 && yIndex>=0 && yIndex<8)
            details[xIndex][yIndex].add(new GeoSubCell(zInfo));
    }
}
