package net.sf.l2j.gameserver.model.entity.geodata;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2CharPosition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PathNodeBinRequester
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
    
    static int NODERANGE = 300;
    
    public static PathNodeBinRequester _instance = new PathNodeBinRequester();
    private final static Log _log = LogFactory.getLog(PathNodeBinRequester.class.getName());
    private FastTable<IndexNode> pathNodeBinIndex;
    private FastTable<ZoneNode> pathNodeBinZoneIndex;
    private FastMap<Integer,PathNodeLocation> pathNodeBin; 
    private FastMap<String,PathNodeBlock> pathNodeBufferList;
    private short defaultCapacity;
    private long defaultExpirationTime;


    public PathNodeBinRequester()
    {
    	pathNodeBinIndex = new FastTable <IndexNode>();
    	pathNodeBinZoneIndex = new FastTable<ZoneNode>();
    	defaultCapacity = 500;
    	defaultExpirationTime = 900000;
    	pathNodeBin = new FastMap<Integer,PathNodeLocation>().setShared(true);
    	pathNodeBufferList = new FastMap<String, PathNodeBlock>().setShared(true);
    	
    	if ( !LoadIndex())
    	{
    		// error while loading
    		// _ log
    	}
    }

    public static PathNodeBinRequester getInstance()
    {
        if(_instance == null)
        {
            _instance = new PathNodeBinRequester();
        }
        return _instance;
    }
    
    public PathNodeLocation getNode(int nodeIndex, int X, int Y)
    {
    	PathNodeLocation node;
    	getPathNodeBlock(X, Y);
    	node = pathNodeBin.get(nodeIndex);
    	if (node == null)
    	{
    		int i;
    		IndexNode in=null;
    		for ( i = 0 ; i < pathNodeBinIndex.size() ; i ++) 
    		{
    			in = pathNodeBinIndex.get(i);
    			if ( in.startNode + in.nodeCount > nodeIndex )
    			{
    				break;
    			}
    		}
    		getPathNodeBlock(in.x, in.y);
    		node = pathNodeBin.get(nodeIndex);
    	}	
    	return(node);
    }
    
    public synchronized void getPathNodeBlock(int X , int Y)
    {

        int tileX;
        int tileY;
        int subZoneX;
        int subZoneY;
        String skey;
        PathNodeBlock block;
        boolean ok;

        tileX = (X >> SHIFT_BY) + OFFSET_X;
        tileY = (Y >> SHIFT_BY) + OFFSET_Y;
        subZoneX =((X >> SHIFT_BY) + OFFSET_X) % 16;
        subZoneY =((Y >> SHIFT_BY) + OFFSET_Y) % 16;
        skey = String.valueOf(tileX) + String.valueOf(tileY);

        try
        {
            if ( pathNodeBufferList.containsKey(skey) == true )
            {
                block = pathNodeBufferList.get(skey);
                block.expirationTime = System.currentTimeMillis();
            }
            else
            {
                block = new PathNodeBlock();
                ok = readInPathNodeBin(X, Y,block);
                if (!ok)
                {
                    // no pathnodes
                    return;
                }
                block.expirationTime = System.currentTimeMillis();
                block.key = skey;
                block.zoneX = tileX;
                block.zoneY = tileY;
                block.subZoneX = subZoneX;
                block.subZoneY = subZoneY;
                pathNodeBufferList.put(skey,block);
            }

            FlushPathNodeList();
            return;
        }
        catch ( Exception e)
        {
            _log.warn("Error load GeoBlock Exception : " + e.getMessage());
            return;
        }
    }

    private synchronized void FlushPathNodeList()
    {
        long now = System.currentTimeMillis();

        try
        {
            for (PathNodeBlock expiredBlock : pathNodeBufferList.values())
            {
                if( (now -  expiredBlock.expirationTime) > defaultExpirationTime )
                {
                    flushZoneFromArray(expiredBlock);
                    pathNodeBufferList.remove(expiredBlock.key);
                    expiredBlock = null;
                }
            }
        }
        catch (Exception e)
        {
            _log.warn("Error FlushGeoList Expired Block Exception : " + e.getMessage());
        }
        if (pathNodeBufferList.size() >= defaultCapacity)
        {
            // remove the oldest
            PathNodeBlock toBeRemoved = null;
            long longest = 0;
            //geoList.head().getNext();
            try
            {
                for (PathNodeBlock overPopulatedBlock : pathNodeBufferList.values())
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
                _log.warn("Error FlushPathNodeList Crowded Block frist loop Exception : " + e.getMessage());
            }
            try
            {
                flushZoneFromArray(toBeRemoved);
                pathNodeBufferList.remove(toBeRemoved.key);
                toBeRemoved = null;
            }
            catch (Exception e)
            {
                _log.warn("Error FlushPathNodeList Crowded Block Second loop Exception : " + e.getMessage());
            }
        }
    }	
    public void flushZoneFromArray(PathNodeBlock block)
    {
        IndexNode pIndex;
        ZoneNode pZone;
        int zoneX;
        int zoneY;
        int subZoneX;
        int subZoneY;
        int sectionStartNode=-1;
        int sectionEndNode=-1;
        zoneX = block.zoneX;
        zoneY = block.zoneY;
        subZoneX = block .subZoneX;
        subZoneY = block .subZoneY;
        
        
        //get zone info for the index
        for( int j = 0 ; j < pathNodeBinZoneIndex.size() ; j ++)
        {
            pZone = pathNodeBinZoneIndex.get(j);
            if (pZone.zoneX == zoneX && pZone.zoneY == zoneY && pZone.active == 1)
            {
                sectionStartNode = pZone.startNode;
                sectionEndNode = pZone.startNode + pZone.sectionCount;
            }
        }
        // position Reader
        // and get how many to Read
        for (int i=sectionStartNode; i< sectionEndNode;i++)
        {
            pIndex = pathNodeBinIndex.get(i);
            
            if ( pIndex.zoneX == zoneX && pIndex.zoneY == zoneY &&
                 pIndex.subZoneX == subZoneX && pIndex.subZoneY == subZoneY )   
            {       
                for (int k = pIndex.startNode; k <= pIndex.nodeCount + pIndex.startNode; k++) 
                {
                    pathNodeBin.remove(k);
                }
            }
        }
    }
    
    public boolean readInPathNodeBin(int X, int Y, PathNodeBlock block)
    {
		byte reader[];
		int rCounter;
		int readLenght = 0;
		int currentNodeCounter=0;
		int x;
		int y;
		int z;
		int zoneX=0;
		int zoneY=0;
		int subZoneX=0;
		int subZoneY=0;
		int sectionStartNode=-1;
		int sectionEndNode=-1;

        PathNodeLocation pnl;
		IndexNode pIndex;
		ZoneNode pZone;
		RandomAccessFile pathnode_bin;
		// parse index to load in map
		zoneX = ((X >> SHIFT_BY) + OFFSET_X) / 16 + 16;
		zoneY = ((Y >> SHIFT_BY) + OFFSET_Y) / 16 + 10;
        subZoneX =((X >> SHIFT_BY) + OFFSET_X) % 16;
        subZoneY =((Y >> SHIFT_BY) + OFFSET_Y) % 16;
        
        int counter = 0;
		try
		{
			pathnode_bin = new RandomAccessFile("data/geodata/pathnode.bin","r");

			//get zone info for the index
			for( int j = 0 ; j < pathNodeBinZoneIndex.size() ; j ++)
			{
				pZone = pathNodeBinZoneIndex.get(j);
				if (pZone.zoneX == zoneX && pZone.zoneY == zoneY && pZone.active == 1)
				{
					sectionStartNode = pZone.startNode;
					sectionEndNode = pZone.startNode + pZone.sectionCount;
				}
			}
			// position Reader
			// and get how many to Read
			for (int i=sectionStartNode; i< sectionEndNode;i++)
			{
				pIndex = pathNodeBinIndex.get(i);
				
				if ( pIndex.zoneX == zoneX && pIndex.zoneY == zoneY &&
					 pIndex.subZoneX == subZoneX && pIndex.subZoneY == subZoneY ) 	
				{		
					
					reader = new byte[44*pIndex.nodeCount];
                    
                    pathnode_bin.seek(pIndex.startNode * 44);
			    	pathnode_bin.read(reader);
			    	rCounter = 0;
					currentNodeCounter= pIndex.startNode;
					readLenght = 0;

                    while (readLenght < pIndex.nodeCount)
			        {
						x = parseFlippedInt(reader[rCounter], reader[rCounter+1],
			        			reader[rCounter+2], reader[rCounter+3]);
			        	rCounter+=4;
			        	
			        	y = parseFlippedInt(reader[rCounter], reader[rCounter+1],
			        			reader[rCounter+2], reader[rCounter+3]);
			        	rCounter+=4;
			        	
			        	z = parseFlippedInt(reader[rCounter], reader[rCounter+1],
			        			reader[rCounter+2], reader[rCounter+3]);
			        	rCounter+=4;
			        	
			        	pnl = new PathNodeLocation (x,y,(short)z);
			        	
						for (int j = 0 ; j < 8 ; j ++)
						{
			            	pnl.nextNode[j] = parseFlippedInt(reader[rCounter],	reader[rCounter+1],
			            			reader[rCounter+2], reader[rCounter+3] );
			            	rCounter+=4;
						}
						
			        	if (pnl.nextNode[0] == 0 && pnl.nextNode[1] == 0 &&
			        		pnl.nextNode[2] == 0 && pnl.nextNode[3] == 0 &&
			        		pnl.nextNode[4] == 0 && pnl.nextNode[5] == 0 &&
			        		pnl.nextNode[6] == 0 && pnl.nextNode[7] == 0)
			        	{
			        		//section.nodeCount --;
			        		//pathNodeBin.put(currentNodeCounter,null);
			        	}
			        	else
			        	{
			        	   	pnl.index = currentNodeCounter;
			        		pathNodeBin.put(currentNodeCounter,pnl);
			        	   	counter ++; 	
			        	}
			        	readLenght ++; 
			        	currentNodeCounter++;
			        }
				}
			}
            
            currentNodeCounter --;
            pathnode_bin.close();
	    	return true;

		}
		catch (Exception e)
		{
			return false;
			// io error
		}
    }
    
    public boolean LoadIndex()
	{

		int currentNodeCounter = 0;
	    int totalNodes = 0; 
	    IndexNode section;
	    ZoneNode zone=null;
		int currentZoneId = 0;
		int currentSectionId = 0;
		int sectionCounter = 0;
		int subZoneX=0;
		int subZoneY=0;
		int x;
		int y;
		int z;
	    String []lines; 
	    
        try
        {

        	BufferedReader pathnode_idx = 	new BufferedReader(
        													new InputStreamReader(
        													new FileInputStream("data/geodata/pathnode.idx"),"ISO-8859-1"));
         	
         	String line;
         	line = pathnode_idx.readLine();
			// (ex.) decode  Total = 8208747
			if ( line.contains("Total") == true )
			{
				lines = line.split(" ");
				totalNodes =  Integer.decode(lines[2]);
			}

			currentNodeCounter = 1;
			while ( line != null ) //pathnode_idx currentNodeCounter <= totalNodes)
			{ 
				line = pathnode_idx.readLine();
				if (line == null) continue;
				if ( line.trim().length() == 0 )
				{

				} 
				else if ( line.contains("--- Zone") == true )
				{
					if (zone != null)
					{
						zone.sectionCount = currentSectionId - zone.startNode;
						pathNodeBinZoneIndex.add(zone);
					}
					// --- Zone(10,10)[0] ---
					currentZoneId ++;
					zone = new ZoneNode();
					zone.zoneX = Byte.decode(line.substring(9,11));
					zone.zoneY = Byte.decode(line.substring(12,14));
					zone.active = Byte.decode(line.substring(16,17));
					zone.startNode = currentSectionId;
				}
				else
				{

					lines = line.split(" ");
					y = Integer.decode(lines[1]);
					x = Integer.decode(lines[0]);
					sectionCounter = Integer.decode(lines[2]);
					currentSectionId ++;
					// insert into section
			        subZoneX =((x >> SHIFT_BY) + OFFSET_X) % 16;
			        subZoneY =((y >> SHIFT_BY) + OFFSET_Y) % 16;

			        section = new IndexNode();
			        section.startNode = currentNodeCounter;
					section.x = x;
					section.y = y;
			        section.zoneX =  zone.zoneX;
					section.zoneY =  zone.zoneY;
					section.subZoneX = (byte) subZoneX;
					section.subZoneY = (byte) subZoneY;
					section.nodeCount = sectionCounter;
					pathNodeBinIndex.add(section);
					currentNodeCounter += sectionCounter; 
	            	//if ( currentNodeCounter % 100000 == 0)
	            	//{
	            	//	_log.debugr(currentNodeCounter);
	            	//}
				}	            
	        }
			pathnode_idx.close();            

			return true;
	    }
        catch(Exception e)
        {
            return false;
            //_log.warn( "Error reading Patnode file e=" + e.getMessage());
        }
    }
 
    private int parseFlippedInt(byte val1, byte val2, byte val3,byte val4)
    {
    	int temp;
        // The next line is necessary.  Otherwise, negative numbers get
        // filled with leading 1's and mess up the following bit-wise OR.
    	//temp  = val1 & 0xff;
        temp = (((val4 & 0xff) << 24) | ((val3& 0xff) << 16) | ((val2 & 0xff) << 8) | (val1&0xff));
        return temp;
    }
    
    protected class ZoneNode
    {
    	public byte zoneX;
    	public byte zoneY;
    	public byte active;
    	public int sectionCount;
    	public int startNode;
    }
    
    protected class IndexNode
    {
    	//public String key;
    	public byte zoneX;
    	public byte zoneY;
    	public byte subZoneX; 
    	public byte subZoneY;
    	public int x;
    	public int y;
    	public int nodeCount;
    	public int startNode;
    }
    

    public class PathNodeBlock
    {
        public long expirationTime;
        public String key;
        public int zoneX;
        public int zoneY;
        public int subZoneX;
        public int subZoneY;
    }


    public PathNodeLocation getClosestNodes (PathNodeLocation node)
    {
    	PathNodeLocation result = node;
        int nodecounter;
        int zoneX = ((node.getX() >> SHIFT_BY) + OFFSET_X)/16 + 16;
        int zoneY = ((node.getY() >> SHIFT_BY) + OFFSET_Y)/16 +10;
    	int subZoneX =((node.getX() >> SHIFT_BY) + OFFSET_X) % 16;
        int subZoneY =((node.getY() >> SHIFT_BY) + OFFSET_Y) % 16;
        ZoneNode pZone;
    	IndexNode pIndex;
    	PathNodeLocation pNode;
        
        L2CharPosition cp = new L2CharPosition(node.getX(),node.getY(),node.getZ(),0);
    	FastMap<Integer, PathNodeLocation> sm = new FastMap<Integer, PathNodeLocation>(); 
    	int range;
        int sectionStartNode= -1;
    	int sectionEndNode = -1;

    	getPathNodeBlock(node.getX(), node.getY());

		//get zone info for the index
		for( int i = 0 ; i < pathNodeBinZoneIndex.size() ; i ++)
		{
			pZone = pathNodeBinZoneIndex.get(i);
			if (pZone.zoneX == zoneX && pZone.zoneY == zoneY && pZone.active == 1)
			{
				sectionStartNode = pZone.startNode;
				sectionEndNode = pZone.startNode + pZone.sectionCount;
			}
		}
		// position Reader
		// and get how many to Read

        for (int i=sectionStartNode; i< sectionEndNode;i++)
        {
            pIndex = pathNodeBinIndex.get(i);
            if ( pIndex.zoneX == zoneX && pIndex.zoneY == zoneY &&
                 pIndex.subZoneX == subZoneX && pIndex.subZoneY == subZoneY)    
            {       
                // check if in range
                range = (Math.max(pIndex.x,node.getX()) - Math.min(pIndex.x,node.getX())) +
                (Math.max(pIndex.y,node.getY()) - Math.min(pIndex.y,node.getY())); 
                if (range <= NODERANGE)
                {
                    //validate node with LoS
                    for(int j = pIndex.startNode; j < (pIndex.startNode + pIndex.nodeCount); j++)
                    {
                        pNode = pathNodeBin.get(j);
                        if (pNode == null)
                        {
                            continue;
                        }
                        if (GeoDataRequester.getInstance().hasMovementLoS(cp.getObjectId(),node.getX(),node.getY(),(short)node.getZ(),pNode.getX(),pNode.getY(),(short)pNode.getZ()).LoS == true)
                        {
                            //_log.warn("Get Closest nodes :: objId" +cp.getObjectId()  + " pnodeX:" + pNode.getX() + " pnodeY:" + pNode.getY() + " pnodeZ:" + pNode.getZ() );
                            sm.put(j,pNode);
                        }
                    }
                }
            }       
        }
        if (sm.size() > 8)
        {
            while (sm.size() > 8)
            {
                int maxDist = 0;
                int remove = 0;
                for (PathNodeLocation k : sm.values() )
                {
                    range = (Math.max(k.getX(),node.getX()) - Math.min(k.getX(),node.getX())) +
                    (Math.max(k.getY(),node.getY()) - Math.min(k.getY(),node.getY())); 
                    if ( range > maxDist )
                    {
                        remove = k.index;
                        maxDist = range;
                    }
                }
                sm.remove(remove);
            }
        }
        nodecounter =0;
        for (PathNodeLocation lNode : sm.values() )
        {
            result.nextNode[nodecounter] = lNode.index;
            nodecounter ++;
        }
        return result;
    }
}
