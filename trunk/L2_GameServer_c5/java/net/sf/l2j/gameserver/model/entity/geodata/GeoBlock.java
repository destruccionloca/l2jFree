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

import java.io.File;
import java.util.logging.Logger;

/**
 * 
 * This class is represents a block of geodata.  Each block represents a region 
 * of size 4096x4096 ingame units and corresponds to precisely ONE L2WorldRegion.
 * The actual geodata is contained in a 32x32 of GeoCells.
 * 
 * @author  Fulminus
 */
public class GeoBlock
{
    private GeoCell[][] cellsInfo; 
    private Boolean _hasGeodata;     // keeps track if geodata was successfully loaded.
 
    private static Logger _log = Logger.getLogger(GeoBlock.class.getName());
    
    public GeoBlock(int tileX, int tileY)
    {
        cellsInfo = new GeoCell[32][32]; 
        _hasGeodata = false;     // initialize to false..change it if successfully read.
        
        readGeoData(tileX, tileY);
    }
    
    // destructor...sort of...removes the geocell references.
    public void clearMemory()
    {
        cellsInfo = null;
        _hasGeodata = false;
    }
    
    public Boolean hasLoadedCells()
    {
    	return _hasGeodata; 
    }
    
    public GeoCell getGeoCell(int xWithin, int yWithin, short z)
    {
    	if (!hasLoadedCells() || cellsInfo==null)
    		return null;
    	
    	return cellsInfo[(xWithin & 0x0FFF) >>> 7][(yWithin & 0x0FFF) >>> 7];
    }

// START PARSER SECTION    
    private void readGeoData(int tileX, int tileY)
    {
        byte[] rawInfo = null;

        // 1) get the name of the file containing the geodata for this block 
        String filename = getFileName(tileX, tileY);
        
        // 2) get this block's index within the file.
        int blockIndex = getBlockIndex(tileX, tileY);
        
        // 3) put a copy ENTIRE block into a local buffer (to avoid conflicts when multiple 
        //    blocks are trying to read in the info at the same time).
        GeoDataFileReader fileReader = GeoFilePoolManager.getInstance().checkOut(filename);
        if((fileReader != null) && (fileReader.getName() != null))
        {
            int arraySize = fileReader.getBlockSize(blockIndex);
            if (arraySize > 0)
                rawInfo = new byte[arraySize];
            
            // 4) if successful, parse the data from this array.
            if (rawInfo != null)
            {
                int readLength = fileReader.readGeoData(blockIndex, rawInfo, arraySize);
                	
                if (readLength == arraySize)
                {
                    _hasGeodata = true;
                    
                    int header;
                    int readIndex = 0;
                    // 4) start parcing from this local copy.
                    for (int x = 0; x < 32; x++)		
                        for(int y = 0; y < 32; y++)
                        {
                            // reading more bytes than what managed to come through the file is wasteful and meaningless. 
                            if (readIndex > readLength)
                                return;
                            
                            header = parseFlippedShort(rawInfo[readIndex], rawInfo[readIndex+1]);
                            readIndex += 2;
                            
                            if (header == 0x0000)
                                readIndex = parseCoarseCell(rawInfo, readIndex, x, y);
                            else if (header == 0x0040)
                                readIndex = parseDetailedCell(rawInfo, readIndex, x, y);
                            else
                                readIndex = parseMultiLayerDetailedCell(rawInfo, readIndex, x, y);
                        }
                }
                _log.warning("Loaded Geodata for ("+tileX+","+tileY+") from file: "+filename);
            }
            else
                _log.warning("Failed to load Geodata (null raw) for ("+tileX+","+tileY+") from file: "+filename);

            // all done with this file...release it back to the threadpool.
            GeoFilePoolManager.getInstance().checkIn(fileReader); 
        }
        else
            _log.warning("Failed to load Geodata (null file pointer) for ("+tileX+","+tileY+") from file: "+filename);
    }    
    
    private int parseFlippedShort(byte first, byte second)
    {
    	int temp = first;
    	// The next line is necessary.  Otherwise, negative numbers get 
    	// filled with leading 1's and mess up the following bit-wise OR.
    	temp = temp & 0x0ff;	    	
    	temp = temp | (second<<8);
    	return temp;
    }
    
    private int parseCoarseCell(byte[] rawInfo, int startIndex, int x, int y)
    {
        cellsInfo[x][y] = new GeoCoarseCell();

        int temp = parseFlippedShort(rawInfo[startIndex],rawInfo[startIndex+1]);
        startIndex += 2;
        ((GeoCoarseCell) cellsInfo[x][y]).setMinZ((short)(temp & 0x0fff0));
        ((GeoCoarseCell) cellsInfo[x][y]).setNSEW((byte)(temp & 0x0f));
        
        temp = parseFlippedShort(rawInfo[startIndex],rawInfo[startIndex+1]);
        startIndex += 2;
        ((GeoCoarseCell) cellsInfo[x][y]).setMaxZ((short)(temp & 0x0fff0));

        return startIndex;
    }
    
    private int parseDetailedCell(byte[] rawInfo, int startIndex, int x, int y)
    {
        cellsInfo[x][y] = new GeoDetailedCell();

        // Values are provided in y-first order
        for (short i=0;i<64;i++)
        {
        	int temp = parseFlippedShort(rawInfo[startIndex],rawInfo[startIndex+1]);
            startIndex += 2;
            ((GeoDetailedCell) cellsInfo[x][y]).setSubcellInfo((short)temp, i);
        }        
        
        return startIndex;
    }
    
    private int parseMultiLayerDetailedCell(byte[] rawInfo, int startIndex, int x, int y)
    {
        cellsInfo[x][y] = new GeoMultiLayerDetailedCell();

        // Values are provided in y-first order
        for (short i=0;i<64;i++)
        {
            int numLayers = parseFlippedShort(rawInfo[startIndex],rawInfo[startIndex+1]);
            startIndex += 2;
            
            for(int j=0;j<numLayers;j++)
            {      
                int temp = parseFlippedShort(rawInfo[startIndex],rawInfo[startIndex+1]);
                startIndex += 2;
                ((GeoMultiLayerDetailedCell) cellsInfo[x][y]).addSubcellInfo((short)temp, i);
            }
        }        
       
        return startIndex;
    }   
    
    /**
     * find the block within the file which corresponds to this GeoBlock.
     * within this file, this is block tileX%8, tileY%8.  The blocks are layed 
     * out like so:
     *
     * 0  8 16 24 ... 
     * 1  9 17 25 ... 
     * 2 10 18 26 ...
     * 3 11 19 27 ...
     * 4 12 20 28 ...
     * 5 13 21 29 ...
     * 6 14 22 30 ... 62
     * 7 15 23 31 ... 63
     */
    private int getBlockIndex(int tileX, int tileY)
    {
        return (tileX%8)*8 + (tileY%8);
    }
    
    /**
     * find the file that contains the info for this GeoBlock
     * Each file contains 8x8 = 64 blocks with block 0,0 at the beginning on file 16_10  
     * Given the tileX and tileY find the file.
     * 
     * @param tileX: 0-based tile number in the X direction for this block
     * @param tileY: 0-based tile number in the Y direction for this block
     * @return String: name of the file containing geodata for this block.  
     */
    private String getFileName(int tileX, int tileY)
    {
        // find the exact filename for this (tileX,tileY)
        // for example if we have 
        // x = 58  and y = 113, we know we are in 23_24, to get back to this filename we do the following
        // 58 divide by 8 = 7.25 (round down to int)
        // 113 devide by 8 = 14.125 (round down to int)
        // so we have 7,14   now just add   16 to x    and 10 to y
        // 7+16 = 23         14+10 = 24
        // now we have our file name x,y values
        // 23_24_conv.l2j
        
        int fileX = (tileX / 8) + 16;
        int fileY = (tileY / 8) + 10;
        
        String filename = "data/geodata/"+fileX+"_"+fileY+"_conv.l2j";
        
        File filecheck = new File(filename);
        if (!filecheck.exists())
            _log.config("Geodata file " + filecheck.getAbsolutePath() + " does not exist");
        else
            _log.config("Geodata file located = " + filename);

        return filename;
    }
//  END PARSING SECTION
}
