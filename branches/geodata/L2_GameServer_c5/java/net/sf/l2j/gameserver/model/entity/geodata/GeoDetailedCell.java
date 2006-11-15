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
 * This class represents single-layered cells that require details.
 * It contains an array with info about each subcell.
 * 
 * @author  Fulminus
 */
public class GeoDetailedCell extends GeoCell
{
    private GeoSubCell[][] details;
    
    public GeoDetailedCell()
    {
        details = new GeoSubCell[8][8];
    }
    
    public GeoSubCell getNearestFloorSubcell(int xWithin, int yWithin, int z)
    {
        return details[(xWithin & 0x07F)>>>4][(yWithin & 0x07F)>>>4];
    }    
    
    public GeoSubCell getNearestSubcell(int xWithin, int yWithin, int z)
    {
        return details[(xWithin & 0x07F)>>>4][(yWithin & 0x07F)>>>4];
    }    
    
    public void setSubcellInfo(short z, byte NSEW, short xIndex, short yIndex)
    {
        short zInfo = (short)((z & 0x0fff0) | (NSEW & 0x0f));
        
        setSubcellInfo(zInfo, xIndex, yIndex);
    }

    public void setSubcellInfo(short zInfo, short xIndex, short yIndex)
    {
        if(xIndex >=0 && xIndex<8 && yIndex>=0 && yIndex<8)
            details[xIndex][yIndex] = new GeoSubCell(zInfo);
    }

    public void setSubcellInfo(short zInfo, short oneLineIndex)
    {
        short xIndex = (short)(oneLineIndex/8);
        short yIndex = (short)(oneLineIndex%8);
        
        if(xIndex >=0 && xIndex<8 && yIndex>=0 && yIndex<8)
            details[xIndex][yIndex] = new GeoSubCell(zInfo);
    }
}
