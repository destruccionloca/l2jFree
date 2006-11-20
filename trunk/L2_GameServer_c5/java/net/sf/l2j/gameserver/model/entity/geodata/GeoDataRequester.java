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

import java.util.logging.Logger;
import java.io.File;
import java.io.RandomAccessFile;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import javolution.util.FastMap;

/**
clanth --  22 oct 2006
 **/
public class GeoDataRequester
{
    private static final int SHIFT_BY = 11;
    /** Map dimensions */
    public static final int MAP_MIN_X = -131072;
    public static final int MAP_MAX_X = 228608;
    public static final int MAP_MIN_Y = -262144;
    public static final int MAP_MAX_Y = 262144;
    public static final int CELL_OFFSET = 0x7FF;

    /** calculated offset used so top left region is 0,0 */
    private static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
    private static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
    private static final byte  EAST=1, WEST=2, SOUTH=4, NORTH=8;

    private short defaultCapacity;
    private long defaultExpirationTime;
    public static GeoDataRequester _instance = new GeoDataRequester();

    private static Logger _log = Logger.getLogger(GeoDataRequester.class.getName());

    private FastMap<String,GeoBlock> geoList;
    private FastMap<Integer,GeoDataBuffer> geoRequestBuffer;


    public class FarPoint
    {
        public int x;
        public int y;
        public short z;
        public boolean LoS;
    }

    private class GeoBlock
    {
        public Cell[][] geoInnerBlock = new Cell[16][16];
        public long expirationTime;
        public String key;
    }

    class Cell
    {

    }
    private class CoarseCell extends Cell
    {
        public short minZ_NSEW;
        public short maxZ;
    }

    private class DetailedCell extends Cell
    {
        public short[][] NSEW_Z = new short [8][8];
    }

    private class multiLevelDetailedCell extends Cell
    {
        public layers[][] NSEW_Z = new layers [8][8];

    }

    private class layers
    {
        public short[] layer;
        private layers(short amount)
        {
            layer = new short[amount];
        }
    }
    public class GeoSubCell
    {

        //public int currentLayer;
        //public byte cellType;

        public short zNSEW;
        public short Z = 0;
        public byte NSEW = 0;

        public GeoSubCell()
        {
            //do nothing
        }

        public GeoSubCell(short newZNSEW)
        {
            zNSEW = newZNSEW;
        }

        public GeoSubCell(short newZ, byte newNSEW)
        {
            zNSEW = (short)(((newZ<<1)& 0x0fff0) | (newNSEW & 0x0f));
        }

        public short getZ()
        {
            if (Z == 0)
            {
                return (short)((zNSEW>>1) & 0xFFFFFFF8);
            }
            else
            {
                return Z;
            }
        }

        public byte getNSEW()
        {
            if (NSEW == 0)
            {
                return (byte)(zNSEW & 0x0F);
            }
            else
            {
                return NSEW;
            }
        }
    }


    private class GeoDataBuffer
    {
        public int key;
        public int x;
        public int y;
        public int z;
        public int targetX;
        public int targetY;
        public int targetZ;
        public byte mov_atk; // move = 0 atk = 1
        public boolean hadLoS;
    }

    public GeoDataRequester()
    {
        defaultCapacity = 500;  //maximum of 16x16 regions loaded in memory
        defaultExpirationTime  = Config.ALLOW_GEODATA_EXPIRATIONTIME;     // unused files expire after 900 secs.
        geoList = new FastMap<String,GeoBlock>().setShared(true);
        geoRequestBuffer = new FastMap<Integer,GeoDataBuffer>().setShared(true);
    }

    public static GeoDataRequester getInstance()
    {
        if(_instance == null)
        {
            _instance = new GeoDataRequester();
        }
        return _instance;
    }

    public void setDefaultExpirationTime (long expTime)
    {
        defaultExpirationTime = expTime;
    }

    public long getDefaultExpirationTime ()
    {
        return defaultExpirationTime;
    }

    public int getGeoListSize()
    {
        return geoList.size();
    }

    public synchronized GeoBlock getGeoBlock(int X , int Y)
    {

        int tileX;
        int tileY;
        GeoBlock block;
        String skey;
        byte[] allBlocks;

        tileX = (X >> SHIFT_BY) + OFFSET_X;
        tileY = (Y >> SHIFT_BY) + OFFSET_Y;
        skey = String.valueOf(tileX) + String.valueOf(tileY);

        try
        {
            if ( geoList.containsKey(skey) == true )
            {
                block = geoList.get(skey);
                block.expirationTime = System.currentTimeMillis();
                return block;
            }
            else
            {
                block = new GeoBlock();
                allBlocks  = readGeoData( X,Y );
                if (allBlocks == null)
                {
                    // no geodata
                    return null;
                }
                block.geoInnerBlock = parseGeoBlock(allBlocks);
                block.expirationTime = System.currentTimeMillis();
                block.key = skey;
                geoList.put(skey,block);
            }

            FlushGeoList();
            return block;
        }
        catch ( Exception e)
        {
            _log.warning("Error load GeoBlock Exception : " + e.getMessage());
            return null;
        }
    }

    private synchronized void FlushGeoList()
    {
        long now = System.currentTimeMillis();

        try
        {
            for (GeoBlock expiredBlock : geoList.values())
            {
                if( (now -  expiredBlock.expirationTime) > defaultExpirationTime )
                {
                    if(Config.DEBUG) _log.config("Unloading Geodata " + expiredBlock.key);
                    // object has expired so clean up
                    for(int i = 0 ; i < 16 ; i ++ )
                    {
                        for (int j = 0; j < 16 ; j ++ )
                        {
                            if  (expiredBlock.geoInnerBlock[i][j] instanceof DetailedCell)
                            {
                                ((DetailedCell)expiredBlock.geoInnerBlock[i][j]).NSEW_Z = null;
                            }

                            else if  (expiredBlock.geoInnerBlock[i][j] instanceof multiLevelDetailedCell)
                            {
                                for (int k = 0 ; k < 8 ; k ++)
                                {
                                    for (int l = 0 ; l < 8 ; l ++)
                                    {
                                        ((multiLevelDetailedCell)expiredBlock.geoInnerBlock[i][j]).NSEW_Z[k][l] = null;
                                    }
                                }
                            }
                            expiredBlock.geoInnerBlock[i][j] = null;
                        }
                    }
                    geoList.remove(expiredBlock.key);
                    expiredBlock.geoInnerBlock = null;
                    expiredBlock = null;
                }
            }
        }
        catch (Exception e)
        {
            _log.warning("Error FlushGeoList Expired Block Exception : " + e.getMessage());
        }
        if (geoList.size() >= defaultCapacity)
        {
            // remove the oldest
            GeoBlock toBeRemoved = null;
            long longest = 0;
            //geoList.head().getNext();
            try
            {
                for (GeoBlock overPopulatedBlock : geoList.values())
                {
                    if (now - overPopulatedBlock.expirationTime  >  longest)
                    {
                        toBeRemoved = overPopulatedBlock;
                        longest = now - overPopulatedBlock.expirationTime;
                    }
                }
            }
            catch (Exception e)
            {
                _log.warning("Error FlushGeoList Crowded Block frist loop Exception : " + e.getMessage());
            }
            try
            {
                for(int i = 0 ; i < 16 ; i ++ )
                {
                    for (int j = 0; j < 16 ; j ++ )
                    {
                        if (toBeRemoved.geoInnerBlock != null )
                        {
                            if  (toBeRemoved.geoInnerBlock[i][j] instanceof multiLevelDetailedCell)
                            {
                                for (int k = 0 ; k < 8 ; k ++)
                                {
                                    for (int l = 0 ; l < 8 ; l ++)
                                    {
                                        ((multiLevelDetailedCell)toBeRemoved.geoInnerBlock[i][j]).NSEW_Z[k][l] = null;
                                    }
                                }
                            }
                            toBeRemoved.geoInnerBlock[i][j] = null;
                        }
                    }
                }
                geoList.remove(toBeRemoved.key);
                toBeRemoved.geoInnerBlock = null;
                toBeRemoved = null;
            }
            catch (Exception e)
            {
                _log.warning("Error FlushGeoList Crowded Block Second loop Exception : " + e.getMessage());
            }
        }
        // TODO
        //Flush unused object in geo buffer
        L2Object obj;
        try
        {
            for (GeoDataBuffer expiredBlock : geoRequestBuffer.values())
            {
                obj = L2World.getInstance().findObject(expiredBlock.key);
                if ( obj == null)
                {
                    geoRequestBuffer.remove(expiredBlock.key);
                    expiredBlock = null;
                }
            }
        }
        catch (Exception e)
        {
            _log.warning("Error in flushing geoRequestBuffer");
        }

    }
    private GeoDataBuffer checkGeoBuffer(int objId, int npcX, int npcY, int npcZ, int targetX, int targetY, int  targetZ)
    {

        GeoDataBuffer geobuf;

        if( geoRequestBuffer.containsKey(objId) == true )
        {
            geobuf = geoRequestBuffer.get(objId);
            if ( geobuf.x==npcX && geobuf.y==npcY && geobuf.z==npcZ && geobuf.targetX==targetX && geobuf.targetY==targetY && geobuf.targetZ==targetZ )
            {
                return geobuf;
            }
            else
            {
                //_log.warning( " refreshing buffer " + objId );
                geobuf.x = npcX;
                geobuf.y = npcY;
                geobuf.z = npcZ;
                geobuf.targetX = targetX;
                geobuf.targetY = targetY;
                geobuf.targetZ = targetZ;
                return null;
            }
        }
        else
        {
            geobuf = new GeoDataBuffer();
            geobuf.key = objId;
            geobuf.x = npcX;
            geobuf.y = npcY;
            geobuf.z = npcZ;
            geobuf.targetX = targetX;
            geobuf.targetY = targetY;
            geobuf.targetZ = targetZ;
            geoRequestBuffer.put(objId,geobuf);
        }
        return null;
    }

    public byte[] readGeoData(int X, int Y)
    {

        RandomAccessFile currentFile;
        // 1) get the name of the file containing the geodata for this block
        String filename = getBlockFileName(X, Y);
        try
        {
            byte[] rawInfo = null;
            currentFile = new RandomAccessFile(filename,"r");
            //GeoDataFileReader fileReader = GeoFilePoolManager.getInstance().checkOut(filename);

            if( currentFile.length() > 0)
            {
                if(Config.DEBUG) _log.config("Loading Geodata from file " + filename);
                long arraySize = currentFile.length();

                if (arraySize > 0)
                {
                    rawInfo = new byte[(int)arraySize];
                }

                if (rawInfo != null)
                {
                    int readLength = currentFile.read(rawInfo);
                    if (readLength == -1 )
                    {
                        //_log.warning( "error reading geodata file " + filename);
                        currentFile.close();
                        return null;
                    }
                }
            }
            currentFile.close();
            return rawInfo;
        }
        catch(Exception e)
        {
            //_log.warning( "Error reading geodata file e=" + e.getMessage());
            return null;
        }

    }
    private Cell[][] parseGeoBlock (byte[] rawInfo)
    {
        byte header;
        int dataPosition = 0;
        Cell block[][] = new Cell[16][16];

        try
        {
            for (int x = 0; x < 16; x++)
            {
                for(int y = 0; y < 16; y++)
                {
                    header = rawInfo[dataPosition];
                    dataPosition ++;

                    if (header == 0x00)
                    {
                        block[x][y] = new CoarseCell();
                        ((CoarseCell) (block[x][y])).minZ_NSEW = parseFlippedShort(rawInfo[dataPosition],rawInfo[dataPosition+1]);
                        dataPosition += 2;
                        ((CoarseCell) (block[x][y])).maxZ = parseFlippedShort(rawInfo[dataPosition],rawInfo[dataPosition+1]);
                        dataPosition += 2;
                    }
                    else if (header == 0x01)
                    {
                        block[x][y] = new DetailedCell();

                        // Values are provided in y-first order
                        for (short i=0;i<8;i++)
                        {
                            for (short j= 0 ; j < 8 ; j ++)
                            {
                                ((DetailedCell) (block[x][y])).NSEW_Z[i][j] = parseFlippedShort(rawInfo[dataPosition],rawInfo[dataPosition+1]);
                                dataPosition += 2;
                            }
                        }
                    }
                    else if (header == 0x02)
                    {
                        block[x][y] = new multiLevelDetailedCell();

                        // Values are provided in y-first order
                        for (short i=0;i<8;i++)
                        {
                            for (short j= 0 ; j < 8 ; j ++)
                            {
                                short numLayers = parseFlippedShort(rawInfo[dataPosition],rawInfo[dataPosition+1]);
                                dataPosition += 2;
                                ((multiLevelDetailedCell) block[x][y]).NSEW_Z[i][j]  = new layers(numLayers);
                                for(int k=0;k<numLayers;k++)
                                {
                                    ((multiLevelDetailedCell) block[x][y]).NSEW_Z[i][j].layer[k]  = parseFlippedShort(rawInfo[dataPosition],rawInfo[dataPosition+1]);
                                    dataPosition += 2;
                                }
                            }
                        }
                    }
                    else
                    {
                        //geodata file structure error;
                        _log.warning( "Error in Geodata file, structure error");
                        block = null;
                        return null;
                    }
                }
            }
            return block;
        }
        catch(Exception e)
        {
            _log.warning( "Error parsing geodata : " + e.getMessage());
            block = null;
            return null;
        }
    }

    private short parseFlippedShort(byte first, byte second)
    {
        int temp = first;
        // The next line is necessary.  Otherwise, negative numbers get
        // filled with leading 1's and mess up the following bit-wise OR.
        temp = temp & 0x0ff;
        temp = temp | (second<<8);
        return (short)temp;
    }

    /**
     * find the file that contains the info for this GeoBlock
     * Each file contains 1 coarse cell or  8x8 = 64 blocks with block 0,0 at the beginning on file 16_10
     * Given the X and Y find the file.
     */
    private String getBlockFileName(int X,int Y)
    {
        int tileX = (X >> SHIFT_BY) + OFFSET_X;
        int tileY = (Y >> SHIFT_BY) + OFFSET_Y;
        int fileX =(tileX / 16) + 16;
        int fileY = (tileY / 16) + 10;
        int subx =(tileX % 16);
        int suby =(tileY % 16);

        String filename = "data/geodata/" + fileX + "_" + fileY;
        filename += "/" +fileX+"_" + fileY + "_" + subx + "_" + suby +  ".l2j";

        File filecheck = new File(filename);
        if (!filecheck.exists())
        {
            //_log.warning("check X = " + tileX + "   Y =" + tileY);
            //_log.warning("Geodata file " + filecheck.getAbsolutePath() + " does not exist");
            return null;
        }
        //_log.config("Geodata file located = " + filename);
        return filename;
    }

    /**
      not working yet
     */
    public FarPoint  getFarMovementPoint(L2Object source, L2Object target)
    {
        return getFarMovementPoint(source.getObjectId(), source.getX(), source.getY(), (short) source.getZ() , target.getX(), target.getY(), (short)target.getZ());
    }
    public FarPoint getFarMovementPoint(L2Object source, int targetX , int targetY , int targetZ)
    {
        return getFarMovementPoint(source.getObjectId(), source.getX(), source.getY(), (short) source.getZ() , targetX, targetY, (short)targetZ);
    }
    public FarPoint getFarMovementPoint(int objId, int sourceX, int sourceY, short sourceZ, int targetX , int targetY , short targetZ)
    {
        FarPoint p = new FarPoint();

        if (hasMovementLoS (objId, sourceX, sourceY, sourceZ, targetX, targetY, targetZ) == true)
        {
            p.x = targetX;
            p.y = targetY;
            p.z = targetZ;
            p.LoS = true;
            return p;
        }

        return p;
    }

    public boolean hasMovementLoS(L2Object source, L2Object target)
    {
        return hasMovementLoS (source.getObjectId(),  source.getX(),source.getY(), (short) source.getZ() , target.getX(), target.getY(), (short) target.getZ());
    }
    public boolean hasMovementLoS(L2Object source, int targetX , int targetY , int targetZ)
    {
        return hasMovementLoS (source.getObjectId(), source.getX(), source.getY(), (short) source.getZ() , targetX, targetY, (short)targetZ);
    }
    public boolean hasLoS(L2Object source, int targetX , int targetY , short targetZ)
    {
        return hasMovementLoS (source.getObjectId(), source.getX(), source.getY(), (short) source.getZ() , targetX, targetY, targetZ);
    }
    public boolean hasMovementLoS(int objId, int sourceX, int sourceY, short sourceZ, int targetX , int targetY , short targetZ)
    {
        float m;
        float b;
        int x;
        int y;

        byte NSEW;
        short newZ;
        boolean LoS;
        GeoDataBuffer geobuf;
        GeoSubCell cellData;

        //_log.warning("Geodata MLOS start");
        double range = ((sourceX - targetX)*(sourceX - targetX) + (sourceY - targetY)*(sourceY - targetY));
        if (range < 900  && (sourceZ & 0xFFF8) == (targetZ & 0xFFF8))
        {
            // self / 30 Ticks or mob is 8z ticks away force LOS
            return true;
        }


        geobuf =checkGeoBuffer(objId, sourceX, sourceY, sourceZ, targetX, targetY, targetZ);
        if (geobuf != null)
        {
            if (geobuf.mov_atk == 0)
                return geobuf.hadLoS;
        }

        LoS = true;
        newZ =sourceZ;

        // avoid / 0 error
        if (sourceX != targetX)
        {
            m = (float) (sourceY - targetY) / (sourceX - targetX);
        }
        else
        {
            m =0;
        }

        b = sourceY - (m*sourceX);

        if ( sourceX == targetX ) // vertical line
        {
            if (sourceY > targetY)  //north
            {
                newZ =sourceZ;
                for (y=sourceY;y > targetY+16;y = y - 16)
                {
                    cellData = getGeoInfoNearest(sourceX, y & 0xFFFFFFF0,newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata MLOS x:" + sourceX + " y:" + y + " z:" + cellData.getZ() );
                    if ( (NSEW & SOUTH) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
            }
            else
            {
                newZ =sourceZ;
                for (y=sourceY;y < targetY-16;y=y + 16)
                {
                    cellData = getGeoInfoNearest(sourceX, (y+16) & 0xFFFFFFF0,newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata MLOS x:" + sourceX + " y:" + y + " z:" + cellData.getZ() );
                    if ( (NSEW & NORTH) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
            }
        }
        else if (targetY==sourceY)      // Horizontal Line or m=0
        {
            if (sourceX > targetX)  // west
            {
                newZ =sourceZ;
                for (x=sourceX;x > targetX+16;x=x - 16)
                {
                    cellData = getGeoInfoNearest(x & 0xFFFFFFF0,sourceY,newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata MLOS x:" + x + " y:" + sourceY + " z:" + cellData.getZ() );
                    if ( (NSEW & WEST) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
            }
            else // EAST
            {
                newZ =sourceZ;
                for (x=sourceX;x < targetX-16;x=x + 16)
                {
                    cellData = getGeoInfoNearest((x +16)& 0xFFFFFFF0,sourceY,newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata MLOS x:" + x + " y:" + sourceY + " z:" + cellData.getZ() );
                    if ( (NSEW & EAST) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
            }
        }
        else if (( m > 0 && m <= 1) || ( m < 0 && m >= -1) ) // east west line -- so more X's to verify than Y's
        {
            if ( sourceX < targetX) // going East
            {
                newZ =sourceZ;
                for (x=sourceX;x < targetX-16 ; x = x + 16)
                {
                    y = (int)((m*x) + b);
                    cellData = getGeoInfoNearest((x + 16) & 0xFFFFFFF0  , y , newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                    if ((NSEW & EAST) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
                if ( m > 0 ) // North bound
                {
                    newZ =sourceZ;
                    for (y=sourceY;y < targetY-16; y = y + 16)
                    {
                        x = (int) ((y-b) / m);
                        cellData = getGeoInfoNearest( x  , (y + 16) & 0xFFFFFFF0  , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & NORTH) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
                else // South Bound
                {
                    newZ =sourceZ;
                    for (y=sourceY;y > targetY+16; y = y - 16)
                    {
                        x = (int) ((y -b) / m);
                        cellData = getGeoInfoNearest( x  , y & 0xFFFFFFF0, newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & SOUTH) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
            }
            else // going West
            {
                newZ =sourceZ;
                for ( x=sourceX;x > targetX+16 ; x = x - 16)
                {
                    y = (int)((m*x) + b);
                    cellData = getGeoInfoNearest( x & 0xFFFFFFF0 ,y , newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                    if ((NSEW & WEST) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
                if ( m > 0 ) // North
                {
                    newZ =sourceZ;
                    for ( y=sourceY;y < targetY-16; y = y + 16)
                    {
                        x = (int) ((y-b) / m);
                        cellData = getGeoInfoNearest(x  ,(y + 16) & 0xFFFFFFF0 , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & NORTH) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
                else // South
                {
                    newZ =sourceZ;
                    for ( y = sourceY;y > targetY+16; y = y - 16)
                    {
                        x = (int) ((y-b) / m);
                        cellData = getGeoInfoNearest( x  , y & 0xFFFFFFF0 , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & SOUTH) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
            }
        }
        else if ( m > 1 || m < -1) //north south line --  so, more y's to verify than x's
        {
            if (sourceY > targetY) // going north
            {
                newZ =sourceZ;
                for ( y = sourceY ; y > targetY+16 ; y = y - 16)
                {
                    //y = (m * x) + b;
                    x = (int) ((y-b) / m);
                    cellData = getGeoInfoNearest( x , y & 0xFFFFFFF0 , newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                    if ((NSEW & NORTH) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }

                if ( m > 0 ) // EAST Bound
                {
                    newZ =sourceZ;
                    for ( x = sourceX;x < targetX-16; x = x + 16)
                    {
                        y = (int)((m*x)+b);
                        cellData = getGeoInfoNearest( (x + 16) & 0xFFFFFFF0  , y , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & EAST) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
                else // West Bound
                {
                    newZ =sourceZ;
                    for ( x = sourceX;x > targetX+16; x = x - 16)
                    {
                        y = (int)((m*x)+b);
                        cellData = getGeoInfoNearest( (x + 16) & 0xFFFFFFF0  , y , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & WEST) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
            }
            else // going South
            {
                newZ =sourceZ;
                for ( y = sourceY ; y < targetY-16 ; y = y + 16)
                {
                    //y = (m * x ) + b;
                    x =(int) ((y-b)/m);
                    cellData = getGeoInfoNearest(x ,(y+16) & 0xFFFFFFF0, newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                    if ((NSEW & SOUTH) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }

                if (m > 0) // WEST bound
                {
                    newZ =sourceZ;
                    for ( x = sourceX;x > targetX+16 ; x = x - 16)
                    {
                        y = (int)((m*x)+b);
                        cellData = getGeoInfoNearest( x & 0xFFFFFFF0 , y , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        if ((NSEW & WEST) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
                else // EAST bound
                {
                    newZ =sourceZ;
                    for ( x = sourceX;x < targetX-16 ; x = x + 16)
                    {
                        y = (int)((m*x)+b);
                        cellData = getGeoInfoNearest((x +16) & 0xFFFFFFF0 ,y , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata MLOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & EAST) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
            }
        }
        //_log.warning("Geodata MLOS stop");
        geobuf = geoRequestBuffer.get(objId);
        geobuf.hadLoS = LoS;
        geobuf.mov_atk =0;
        return LoS;
    }

    public boolean hasAttackLoS(L2Object source, L2Object target)
    {
        int Sz=0,Dz=0;
        if( source instanceof L2Character) Sz=((L2Character) source).getTemplate().collisionHeight;        
        if( target instanceof L2Character) Dz=((L2Character) target).getTemplate().collisionHeight;
        return hasAttackLoS (source.getObjectId(),  source.getX(),source.getY(), (short) (source.getZ() + Sz), target.getX(), target.getY(), (short) (target.getZ() + Dz));
    }
    public boolean hasAttackLoS(L2Object source, int targetX , int targetY , int targetZ)
    {
        int Sz=0;
        if( source instanceof L2Character) Sz=((L2Character) source).getTemplate().collisionHeight;
        return hasAttackLoS (source.getObjectId(), source.getX(), source.getY(), (short) (source.getZ() + Sz) , targetX, targetY, (short)targetZ);
    }
    public boolean hasAttackLoS(L2Object source, int targetX , int targetY , short targetZ)
    {
        int Sz=0;
        if( source instanceof L2Character) Sz=((L2Character) source).getTemplate().collisionHeight;
        return hasAttackLoS (source.getObjectId(), source.getX(), source.getY(), (short) (source.getZ() + Sz) , targetX, targetY, targetZ);
    }
    public boolean hasAttackLoS(int objId, int sourceX, int sourceY, short sourceZ, int targetX , int targetY , short targetZ)
    {
        float m;
        float b;
        int x;
        int y;

        byte NSEW;
        short newZ;
        boolean LoS;
        GeoDataBuffer geobuf;
        GeoSubCell cellData;

        //_log.warning("Geodata ALOS start");
        double range = ((sourceX - targetX)*(sourceX - targetX) + (sourceY - targetY)*(sourceY - targetY));
        if (range < 900  && (sourceZ & 0xFFF8) == (targetZ & 0xFFF8))
        {
            // self / 30 Ticks or mob is 8z ticks away force LOS
            return true;
        }


        geobuf =checkGeoBuffer(objId, sourceX, sourceY, sourceZ, targetX, targetY, targetZ);
        if (geobuf != null)
        {
            if (geobuf.mov_atk == 1)
                return geobuf.hadLoS;
        }

        LoS = true;
        newZ =sourceZ;

        // avoid / 0 error
        if (sourceX != targetX)
        {
            m = (float) (sourceY - targetY) / (sourceX - targetX);
        }
        else
        {
            m =0;
        }

        b = sourceY - (m*sourceX);

        if ( sourceX == targetX ) // vertical line
        {
            if (sourceY > targetY)  //north
            {
                newZ =sourceZ;
                for (y=sourceY;y > targetY+16;y = y - 16)
                {
                    cellData = getGeoInfoNearest(sourceX, y & 0xFFFFFFF0,newZ);
                    //edge checking
                    //if ( cellData.getZ() - newZ > 20 ) // impassable, too high, so get Floor
                    //{
                    //cellData = getGeoInfoNearestFloor(sourceX, y & 0xFFFFFFF0,newZ);
                    //}
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata ALOS x:" + sourceX + " y:" + y + " z:" + cellData.getZ() );
                    if ( (NSEW & SOUTH) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
            }
            else
            {
                newZ =sourceZ;
                for (y=sourceY;y < targetY-16;y=y + 16)
                {
                    cellData = getGeoInfoNearest(sourceX, (y+16) & 0xFFFFFFF0,newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata ALOS x:" + sourceX + " y:" + y + " z:" + cellData.getZ() );
                    if ( (NSEW & NORTH) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
            }
        }
        else if (targetY==sourceY)      // Horizontal Line or m=0
        {
            if (sourceX > targetX)  // west
            {
                newZ =sourceZ;
                for (x=sourceX;x > targetX+16;x=x - 16)
                {
                    cellData = getGeoInfoNearest(x & 0xFFFFFFF0,sourceY,newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata ALOS x:" + x + " y:" + sourceY  + " z:" + cellData.getZ() );
                    if ( (NSEW & WEST) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
            }
            else // EAST
            {
                newZ =sourceZ;
                for (x=sourceX;x < targetX-16;x=x + 16)
                {
                    cellData = getGeoInfoNearest((x +16)& 0xFFFFFFF0,sourceY,newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata ALOS x:" + x + " y:" + sourceY  + " z:" + cellData.getZ() );
                    if ( (NSEW & EAST) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
            }
        }
        else if (( m > 0 && m <= 1) || ( m < 0 && m >= -1) ) // east west line -- so more X's to verify than Y's
        {
            if ( sourceX < targetX) // going East
            {
                newZ =sourceZ;
                for (x=sourceX;x < targetX-16 ; x = x + 16)
                {
                    y = (int)((m*x) + b);
                    cellData = getGeoInfoNearest((x + 16) & 0xFFFFFFF0  , y , newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                    if ((NSEW & EAST) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
                if ( m > 0 ) // North bound
                {
                    newZ =sourceZ;
                    for (y=sourceY;y < targetY-16; y = y + 16)
                    {
                        x = (int) ((y-b) / m);
                        cellData = getGeoInfoNearest( x  , (y + 16) & 0xFFFFFFF0  , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & NORTH) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
                else // South Bound
                {
                    newZ =sourceZ;
                    for (y=sourceY;y > targetY+16; y = y - 16)
                    {
                        x = (int) ((y -b) / m);
                        cellData = getGeoInfoNearest( x  , y & 0xFFFFFFF0, newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & SOUTH) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
            }
            else // going West
            {
                newZ =sourceZ;
                for ( x=sourceX;x > targetX+16 ; x = x - 16)
                {
                    y = (int)((m*x) + b);
                    cellData = getGeoInfoNearest( x & 0xFFFFFFF0 ,y , newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                    if ((NSEW & WEST) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }
                if ( m > 0 ) // North
                {
                    newZ =sourceZ;
                    for ( y=sourceY;y < targetY-16; y = y + 16)
                    {
                        x = (int) ((y-b) / m);
                        cellData = getGeoInfoNearest(x  ,(y + 16) & 0xFFFFFFF0 , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & NORTH) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
                else // South
                {
                    newZ =sourceZ;
                    for ( y = sourceY;y > targetY+16; y = y - 16)
                    {
                        x = (int) ((y-b) / m);
                        cellData = getGeoInfoNearest( x  , y & 0xFFFFFFF0 , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & SOUTH) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
            }
        }
        else if ( m > 1 || m < -1) //north south line --  so, more y's to verify than x's
        {
            if (sourceY > targetY) // going north
            {
                newZ =sourceZ;
                for ( y = sourceY ; y > targetY+16 ; y = y - 16)
                {
                    //y = (m * x) + b;
                    x = (int) ((y-b) / m);
                    cellData = getGeoInfoNearest( x , y & 0xFFFFFFF0 , newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                    if ((NSEW & NORTH) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }

                if ( m > 0 ) // EAST Bound
                {
                    newZ =sourceZ;
                    for ( x = sourceX;x < targetX-16; x = x + 16)
                    {
                        y = (int)((m*x)+b);
                        cellData = getGeoInfoNearest( (x + 16) & 0xFFFFFFF0  , y , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & EAST) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
                else // West Bound
                {
                    newZ =sourceZ;
                    for ( x = sourceX;x > targetX+16; x = x - 16)
                    {
                        y = (int)((m*x)+b);
                        cellData = getGeoInfoNearest( (x + 16) & 0xFFFFFFF0  , y , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & WEST) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
            }
            else // going South
            {
                newZ =sourceZ;
                for ( y = sourceY ; y < targetY-16; y = y + 16)
                {
                    //y = (m * x ) + b;
                    x =(int) ((y-b)/m);
                    cellData = getGeoInfoNearest(x ,(y+16) & 0xFFFFFFF0, newZ);
                    NSEW = cellData.getNSEW();
                    newZ =  cellData.getZ();
                    //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                    if ((NSEW & SOUTH) == 0)
                    {
                        LoS = false;
                        break;
                    }
                }

                if (m > 0) // WEST bound
                {
                    newZ =sourceZ;
                    for ( x = sourceX;x > targetX+16; x = x - 16)
                    {
                        y = (int)((m*x)+b);
                        cellData = getGeoInfoNearest( x & 0xFFFFFFF0 , y , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & WEST) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
                else // EAST bound
                {
                    newZ =sourceZ;
                    for ( x = sourceX;x < targetX-16; x = x + 16)
                    {
                        y = (int)((m*x)+b);
                        cellData = getGeoInfoNearest((x +16) & 0xFFFFFFF0 ,y , newZ);
                        NSEW = cellData.getNSEW();
                        newZ =  cellData.getZ();
                        //_log.warning("Geodata ALOS x:" + x + " y:" + y + " z:" + cellData.getZ() );
                        if ((NSEW & EAST) == 0)
                        {
                            LoS = false;
                            break;
                        }
                    }
                }
            }
        }
        //_log.warning("Geodata ALOS stop");
        geobuf = geoRequestBuffer.get(objId);
        geobuf.hadLoS = LoS;
        geobuf.mov_atk = 1;
        return LoS;
    }


    public GeoSubCell getGeoInfoLayer(int x, int y, short z, short layer)
    {

        // get the Geodata info from the region's geodata where the passed xyz belong.
        //System.out.println("Checking cell info...");
        GeoBlock geo;
        GeoSubCell cell;
        geo = getGeoBlock(x,y); //load in mem
        if (geo != null) // if got geo file
        {

            cell = getNearestLayer(geo.geoInnerBlock[(x & CELL_OFFSET) >>> 7][(y & CELL_OFFSET) >>> 7], x ,y, z,layer);
            if ( cell != null)
            {
                return cell;
            }
        }
        GeoSubCell  subcell = new GeoSubCell();
        subcell.Z = z;
        subcell.NSEW = 15;
        return subcell;
    }



    public GeoSubCell getGeoInfoNearestFloor(int x, int y, short z)
    {

        // get the Geodata info from the region's geodata where the passed xyz belong.
        //System.out.println("Checking cell info...");
        GeoBlock geo;
        geo = getGeoBlock(x,y); //load in mem
        if (geo != null) // if got geo file
        {
            return getNearestFloorSubcell(geo.geoInnerBlock[(x & CELL_OFFSET) >>> 7][(y & CELL_OFFSET) >>> 7], x ,y, z);
        }
        GeoSubCell  subcell = new GeoSubCell();
        subcell.Z = z;
        subcell.NSEW = 15;
        return subcell;
    }

    public GeoSubCell getGeoInfoNearest(int x, int y, short z)
    {
        GeoBlock geo;
        geo = getGeoBlock(x,y); //load in mem
        if (geo != null)
        {
            return getNearestSubcell(geo.geoInnerBlock[(x & CELL_OFFSET) >>> 7][(y & CELL_OFFSET) >>> 7], x ,y, z);
        }
        GeoSubCell  subcell = new GeoSubCell();
        subcell.Z = z;
        subcell.NSEW = 15;
        return subcell;
    }

    /*
    public Boolean getIsInWater(int x, int y, int z)
    {
        // get the Geodata info from the region's geodata where the passed xyz belong.
            //System.out.println("Checking cell info...");
        return _worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y].getGeoBlock().
                        getGeoCell(x & CELL_OFFSET, y & CELL_OFFSET, (short)z).
                        isWater((x & 0x07F),(y & 0x07F), z);
    }
     */
    private GeoSubCell getNearestSubcell(Cell cell,int x, int y, short z)
    {

        GeoSubCell subcell;
        x = (x & 127) >>>4;
        y = (y & 127) >>> 4;

        if (cell instanceof CoarseCell )
        {
            subcell = new GeoSubCell ();
            if ( (Math.abs( z -  ((CoarseCell)(cell)).minZ_NSEW)) <= ( z - ((CoarseCell)(cell)).maxZ))
            {
                subcell.Z =  ((CoarseCell)(cell)).minZ_NSEW;
            }
            else
            {
                subcell.Z =  ((CoarseCell)(cell)).maxZ;
            }
            subcell.NSEW = 15;
            return subcell;

        }
        else if (cell instanceof DetailedCell  )
        {
            subcell = new GeoSubCell (((DetailedCell)(cell)).NSEW_Z[x][y]);
            return subcell;
        }
        else if ( cell instanceof multiLevelDetailedCell )
        {
            int layers;
            int smallestDist = 9999999;
            int smallestValue;
            int nearestLayer=0;
            int tmpValue;
            layers = ((multiLevelDetailedCell)(cell)).NSEW_Z[x][y].layer.length;

            for (int i = 0 ; i < layers ; i++)
            {
                // if this is a valid floor for the passed z value
                tmpValue = ((multiLevelDetailedCell)(cell)).NSEW_Z[x][y].layer[i] >>1 & 0xFFFFFFF8 ;
            if ( z >= tmpValue)
            {
                smallestValue = z - tmpValue;
            }
            else
            {
                smallestValue = tmpValue-z;
            }

            if (Math.abs(smallestValue) <= smallestDist)
            {
                smallestDist = Math.abs(smallestValue);
                nearestLayer = i;
            }
            }
            subcell = new GeoSubCell (((multiLevelDetailedCell)(cell)).NSEW_Z[x][y].layer[nearestLayer]);
            return subcell;
        }
        return null;
    }

    private GeoSubCell getNearestFloorSubcell(Cell cell,int x, int y, short z)
    {
        GeoSubCell subcell;
        x = (x & 127) >>>4;
        y = (y & 127) >>> 4;
        if (cell instanceof CoarseCell )
        {
            subcell = new GeoSubCell();
            subcell.Z =  ((CoarseCell)(cell)).minZ_NSEW;
            subcell.NSEW = 15;
            return subcell;
        }
        else if (cell instanceof DetailedCell  )
        {
            subcell = new GeoSubCell (((DetailedCell)(cell)).NSEW_Z[x][y]);
            return subcell;
        }
        else if ( cell instanceof multiLevelDetailedCell )
        {
            int layers;
            int smallestDist = 9999999;
            int smallestValue;
            int nearestLayer=0;
            int tmpValue;
            layers = ((multiLevelDetailedCell)(cell)).NSEW_Z[x][y].layer.length;

            for (int i = 0 ; i < layers ; i++)
            {
                // if this is a valid floor for the passed z value
                tmpValue = (((multiLevelDetailedCell)(cell)).NSEW_Z[x][y].layer[i] >>1) & 0xFFFFFFF8 ;

                if (tmpValue < z) // ok looking for floor data
                {
                    smallestValue =  z - tmpValue;
                    if (smallestValue  < smallestDist)
                    {
                        smallestDist = smallestValue;
                        nearestLayer = i;
                    }
                }
            }
            subcell = new GeoSubCell (((multiLevelDetailedCell)(cell)).NSEW_Z[x][y].layer[nearestLayer]);
            return subcell;
        }
        return null;
    }

    private GeoSubCell getNearestLayer(Cell cell,int x, int y, short z, short layer)
    {
        GeoSubCell subcell;
        x = (x & 127) >>>4;
                y = (y & 127) >>> 4;

                if (cell instanceof CoarseCell)
                {
                    if (layer == 0)
                    {
                        subcell = new GeoSubCell ();
                        subcell.Z =  ((CoarseCell)(cell)).minZ_NSEW;
                        subcell.NSEW = 15;
                        return subcell;
                    }
                    else return null;
                }
                else if (cell instanceof DetailedCell  )
                {
                    if (layer == 0)
                    {
                        subcell = new GeoSubCell (((DetailedCell)(cell)).NSEW_Z[x][y]);
                        return subcell;
                    }
                    else return null;
                }
                else if ( cell instanceof multiLevelDetailedCell )
                {
                    int layers;
                    layers = ((multiLevelDetailedCell)(cell)).NSEW_Z[x][y].layer.length;
                    if (layer < layers)
                    {
                        subcell = new GeoSubCell (((multiLevelDetailedCell)(cell)).NSEW_Z[x][y].layer[layer]);
                        return subcell;
                    }
                    else return null;
                }
                return null;
    }

    public Boolean getIsInWater(int x, int y, short z)
    {
        GeoBlock geo;
        Cell cell;
        geo = getGeoBlock(x,y);
        cell = geo.geoInnerBlock[(x & CELL_OFFSET) >>> 7][(y & CELL_OFFSET) >>> 7];
        if (geo != null)
            if (cell instanceof CoarseCell ){
                if((short)(((CoarseCell)(cell)).minZ_NSEW & 0x0fff0) != ((CoarseCell)(cell)).maxZ ){
                    //_log.warning( "Water XYZ=" + x + " " +y + " " +z + " minZ_NSEW=" + (short)(((CoarseCell)(cell)).minZ_NSEW & 0x0fff0) + " maxZ=" + ((CoarseCell)(cell)).maxZ);
                    return true;
                }                   
                //_log.warning( "Not Water XYZ=" + x + " " +y + " " +z + " minZ_NSEW=" + (short)(((CoarseCell)(cell)).minZ_NSEW & 0x0fff0) + " maxZ=" + ((CoarseCell)(cell)).maxZ);
            }
        return false;
    }

}
