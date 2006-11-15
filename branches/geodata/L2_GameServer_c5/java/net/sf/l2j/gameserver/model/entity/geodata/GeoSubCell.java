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
 * Simple structure to contain the z and NSEW flag info for a single subcell.
 * Typically, a subcell is equivalent to an ingame 16x16 region.
 * 
 * @author  Fulminus
 */
public class GeoSubCell
{
    private short zNSEW;
    
    /**
     * 
     * @param newZ: Z value for this subcell
     * @param newNSEW: NSEW flags for this subcell
     */
    public GeoSubCell(short newZ, byte newNSEW)
    {    	
        zNSEW = (short)(((newZ<<1)& 0x0fff0) | (newNSEW & 0x0f));
    }
    
    public GeoSubCell(short newZNSEW)
    {
        zNSEW = newZNSEW;
    }
    
    /**
     * copy constructor
     * @param copyCell: original subcell
     */
    public GeoSubCell(GeoSubCell copyCell)
    {
        zNSEW = copyCell.zNSEW;
    }
    
    public short getZ()
    {
        return (short)((zNSEW>>1) & 0xfff8);
    }
    
    public byte getNSEW()
    {
        return (byte)(zNSEW & 0x0f);
    }
}
