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
 * This class represents cells that do not require details, generally landscape.
 * It contains min and max Z values which are lower and upper bound for one's motion.
 * In general, if minZ < maxZ it is water with minZ = floor and maxZ = surface.
 * 
 * @author  Fulminus
 */
public class GeoCoarseCell extends GeoCell
{
    private short minZ;
    private short maxZ;
    private byte NSEW;
    
    public GeoCoarseCell()
    {
        minZ = -9999;
        maxZ = -9999;
        NSEW = 0;
    }
    
    public short getMinZ()
    {
        return minZ;
    }
    
    public void setMinZ(short z)
    {
        minZ = z;
    }
    
    public short getMaxZ()
    {
        return maxZ;
    }
    
    public void setMaxZ(short z)
    {
        maxZ = z;
    }
    public byte getNSEW()
    {
        return NSEW;
    }
    
    public void setNSEW(byte flags)
    {
        NSEW = flags;
    }

    // geodata is NOT definitive of water. It contains some info regarding water, but actually
    // water may exist even when geodata is suggesting otherwise.
    public Boolean isWater(int x, int y, int z)
    {
    	System.out.println("reading water info IN COARSE for subcell["+(x>>>4)+"]["+(y>>>4)+"]");
    	System.out.println("player Z(-50), minZ, maxZ:\t"+z+", "+minZ+", "+maxZ);
        return ((minZ < maxZ) && (z <= maxZ));
    }
    
    public GeoSubCell getNearestFloorSubcell(int x, int y, int z)
    {
    	System.out.println("reading info IN COARSE for subcell["+(x>>>4)+"]["+(y>>>4)+"]");
        return new GeoSubCell(minZ, NSEW);
    }
    
    public GeoSubCell getNearestSubcell(int x, int y, int z)
    {
    	System.out.println("reading info IN COARSE for subcell["+(x>>>4)+"]["+(y>>>4)+"]");
        return new GeoSubCell(minZ, NSEW);
    }
 
}
