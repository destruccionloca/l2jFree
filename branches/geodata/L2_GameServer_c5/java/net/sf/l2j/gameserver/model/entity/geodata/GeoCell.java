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

/**
 * 
 * This class is an abstract representation of a cell containing 
 * geodata for a 128x128 region.
 * Direct subclasses:
 * 1. GeoCoarseCell (0000 layer; no details)
 * 2. GeoDetailedCell (4000 layer; contains more details for subcells)
 * 3. GeoMultilayerDetailedCell (4100+ layers; contains more details and multileveled subcells)  
 * 
 * @author  Fulminus
 */
public abstract class GeoCell
{
    // public static final GEO_CELL_SIZE = 128;  //each cell is of size 128x128
    /**
     * this method is overwritten by subclasses 
     * @return subcell containing the largest Z value that is smaller than 
     * the passed z, for the passed x,y.  If the passed z is smaller than 
     * Z values for all subcells, return the lowest among the subcells.
     * if x,y do not belong to this cell, return null. 
     * In other words, return info related to the nearest floor.
     */
    public GeoSubCell getNearestFloorSubcell(int x, int y, int z) 
    {
    	System.out.println("reading info in ABSTRACT for subcell["+(x>>>4)+"]["+(y>>>4)+"]");
        //return null;
        return new GeoSubCell((short)z,(byte)0x00);	// default to a value that allows full freedom 
    }
    
    /**
     * this method is overwritten by subclasses 
     * @return subcell containing the Z value that is the closest to the 
     * passed z (among all layers) for the passed x,y.  The returned
     * subcell may be above or below the passed z... 
     */
    public GeoSubCell getNearestSubcell(int x, int y, int z) 
    {
    	System.out.println("reading info in ABSTRACT for subcell["+(x>>>4)+"]["+(y>>>4)+"]");
        //return null;
        return new GeoSubCell((short)z,(byte)0x00);	// default to a value that allows full freedom 
    }
    
    public Boolean isWater(int x, int y, int z)
    {
    	System.out.println("reading water info in ABSTRACT for subcell["+(x>>>4)+"]["+(y>>>4)+"]");
        return false;
    }
}
