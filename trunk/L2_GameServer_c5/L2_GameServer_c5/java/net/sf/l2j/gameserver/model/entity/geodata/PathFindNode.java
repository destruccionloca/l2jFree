package net.sf.l2j.gameserver.model.entity.geodata;

import java.util.ArrayList;
import net.sf.l2j.gameserver.model.Location;
import javolution.util.FastMap;

/**
 * coded by : clanth 
 * Last modified : November 4 2006
 * This class uses Geodata and PathNode.bin to find a path.
 * 
 */

public class PathFindNode
{
    static final byte  EAST=1, WEST=2, SOUTH=4, NORTH=8;    
    public Path path; 
	
    public FastMap<String,Node > openMaped = new FastMap<String,Node>(); 
    public FastMap<String,Node > closedMaped = new FastMap<String,Node>();
    //GeodataMapHandler map;
    //private MapHandler mh = null;
    
    public PathFindNode(int sourceX, int sourceY, short sourceZ , int destX, int destY, short destZ,int mode)
    {
        
    	// with Geodata
    	if (mode == 1)
    	{
    		SquareLocation startSL = new SquareLocation(sourceX&0xFFFFFFF0 ,sourceY&0xFFFFFFF0,sourceZ);
        	destZ = GeoDataRequester.getInstance().getGeoInfoNearest(destX&0xFFFFFFF0,destY&0xFFFFFFF0, destZ).getZ();
        	SquareLocation endSL = new SquareLocation(destX&0xFFFFFFF0,destY&0xFFFFFFF0,destZ);
        	AStar asGeo = new AStar(new GeodataMapHandler());
        	path = asGeo.findPath(startSL, endSL);
    	}
    	else if (mode == 2)
    	{
	    	// with PathNode.bin and Index
	    	PathNodeLocation start = new PathNodeLocation(sourceX,sourceY,sourceZ);
	    	PathNodeLocation end = new PathNodeLocation(destX,destY,destZ);    	
	    	start = PathNodeBinRequester.getInstance().getClosestNodes(start);
	    	end =  PathNodeBinRequester.getInstance().getClosestNodes(end);
	        AStar as = new AStar(new PathNodeMapHandler());
	        path = as.findPath(start, end);
    	}
    }
    
    private class AStar
    {
        MapHandler mh;
        private Location endpoint = null;

        /** Create AStar */
        public AStar(GeodataMapHandler mapHandler)
        {
            mh = (GeodataMapHandler)mapHandler;
        	endpoint = (SquareLocation)endpoint;
        }

        public AStar(PathNodeMapHandler mapHandler)
        {
            mh = (PathNodeMapHandler)mapHandler;
            endpoint = (PathNodeLocation)endpoint;
        }

        private GeoNode handleNode(GeoNode node)
        {
        	ArrayList<GeoNode> nodes = null;
            String nodeKey;
           	openMaped.remove(node.location.getX() + "" + node.location.getY() + "" + node.location.getZ());
            closedMaped.put(node.location.getX() + "" + node.location.getY() + "" + node.location.getZ(), node);

            if (mh instanceof GeodataMapHandler)
            {
            	nodes = ((GeodataMapHandler)mh).getAdjacentNodes(node,endpoint);
            }

            for(GeoNode n: nodes)
            {
                if (n == null)
                {
                	continue;
                }
            	nodeKey = n.location.getX() + "" + n.location.getY() + "" + n.location.getZ();
                
            	if (n.location.getX() == endpoint.getX() && n.location.getY() == endpoint.getY() && n.location.getZ() == endpoint.getZ() )
                {
                    return(n);
                }
                else if(closedMaped.containsKey(nodeKey))
                {
                    continue;
                }
                else if(openMaped.containsKey(nodeKey))
                {
                    Node on = openMaped.get(nodeKey);
                    if(n.moveCost<on.moveCost)
                    {
                        openMaped.remove(nodeKey);
                        openMaped.put(nodeKey,n);
                    }
                }
                else
                {
                    openMaped.put(nodeKey,n);
                }
            }
            return(null);
        }

        
        private PathNode handleNode(PathNode node)
        {
        	ArrayList<PathNode> nodes = null;
            String nodeKey;
           	openMaped.remove(node.location.getX() + "" + node.location.getY() + "" + node.location.getZ());
            closedMaped.put(node.location.getX() + "" + node.location.getY() + "" + node.location.getZ(), node);
            PathNodeLocation cl;
            if (mh instanceof PathNodeMapHandler)
            {
            	nodes = ((PathNodeMapHandler)mh).getAdjacentNodes(node,endpoint);
            }

            for(PathNode n: nodes)
            {
                if (n == null)
                {
                	continue;
                }
                nodeKey = n.location.getX() + "" + n.location.getY() + "" + n.location.getZ();
                cl = (PathNodeLocation) n.location;
            	for(int i = 0 ; i < 8 ; i ++)
            	{
            		if (((PathNodeLocation)endpoint).nextNode[i] != 0)
            		{
            			if (cl.index  == ((PathNodeLocation)endpoint).nextNode[i])
            			{
            				return(n);
            			}
            		}
            	}
            	if(closedMaped.containsKey(nodeKey))
                {
                    continue;
                }
                else if(openMaped.containsKey(nodeKey))
                {
                    Node on = openMaped.get(nodeKey);
                    if(n.moveCost<on.moveCost)
                    {
                        openMaped.remove(nodeKey);
                        openMaped.put(nodeKey,n);
                    }
                }
                else
                {
                    openMaped.put(nodeKey,n);
                }
            }
            return(null);
        }
        
        

        private Node getBestOpenNode()
        {
            Node bestNode = null;

            for(Node n : openMaped.values())
            {
                if(bestNode == null)
                {
                    bestNode = n;
                }
                else
                {
                    if(n.score<bestNode.score)
                    {
                        bestNode = n;
                    }
                }
            }
            return(bestNode);
        }

        private Path tracePath(Node f)
        {
            ArrayList<Node> nodes = new ArrayList<Node>();
            int totalCost = f.moveCost;
            Node parent = f.parent;
            nodes.add(0,f);

            while(true)
            {
                if(parent.parent == null)
                {
                    break;
                }
                nodes.add(0,parent);
                parent=parent.parent;
            }
            return(new Path(nodes,totalCost));
        }

        public Path findPath(SquareLocation from,SquareLocation to)
        {
            endpoint = to;
        	openMaped.put(from.getX() + "" + from.getY() + "" + from.getZ(), new GeoNode(from,0,0,null));
            
            GeoNode nextNode = null;
            nextNode=(GeoNode)getBestOpenNode();
            GeoNode finish = null;

            while(nextNode!=null)
            {
                finish = handleNode(nextNode);
                if(finish!=null)
                {
                    return(tracePath(finish));
                }
                nextNode=(GeoNode) getBestOpenNode();
            }
            return(null);
        }
    
	    public Path findPath(PathNodeLocation from,PathNodeLocation to)
	    {
	    	endpoint = (PathNodeLocation)to;
	    	openMaped.put( from.getX() + "" + from.getY() + "" + from.getZ(), ((PathNodeMapHandler)mh).getNode(from));
	        
	        PathNode nextNode = null;
	        nextNode=(PathNode)getBestOpenNode();
	        Node finish = null;
	
	        while(nextNode!=null)
	        {
	            finish = handleNode(nextNode);
	            if(finish!=null)
	            {
	                return(tracePath(finish));
	            }
	            nextNode=(PathNode)getBestOpenNode();
	        }
	        return(null);
	    }
    }
    
    public class GeoNode extends Node
    {
        /** Creates a new instance of GeoNode */
        //public GeoNode(Location loc, int mCost,int emCost, Node pNode)
    	public GeoNode(Location loc, int mCost,int emCost, Node pNode)
    	{
        	super(loc,mCost,emCost,pNode);
        }
    }

    public class PathNode extends Node 
    {
    	public PathNode(Location loc, int mCost,int emCost, Node pNode)
    	{
    		super(loc,mCost,emCost,pNode);
    	}
    }
     
    private class PathNodeMapHandler extends MapHandler
    {
    	public PathNodeMapHandler()
    	{
    		
    		
    	}

    	public void handleNode(PathNodeLocation node, PathNode from,ArrayList<PathNode> result,int destx,int desty, short destz)
    	{
    		PathNode n = new PathNode(node,0,0,null);
            int dx = 0;
            int dy = 0;
            int cx=0;
            int cy=0;
            if(node!=null)
            {
            	cx = Math.max(node.getX(),from.location.getX()) - Math.min(node.getX(),from.location.getX());
            	cy = Math.max(node.getY(),from.location.getY()) - Math.min(node.getY(),from.location.getY());
            	n.moveCost = cx+cy+ from.moveCost;

            	dx = Math.max(node.getX(),destx) - Math.min(node.getX(),destx);
                dy = Math.max(node.getY(),desty) - Math.min(node.getY(),desty);
                n.estimatedMoveCost = dx+dy;
                
                n.score = n.moveCost+n.estimatedMoveCost;
                n.parent=from;
                if ( n.score < 10000 )
                {
                    result.add(n);
                }
            }

    	}
    	public ArrayList<PathNode> getAdjacentNodes(PathNode node,Location dl)
    	{
            Location cl = node.location;
            ArrayList<PathNode> result = new ArrayList<PathNode>();
            PathNodeLocation nNode;
            int destx = dl.getX();
            int desty = dl.getY();
            short destz = (short)dl.getZ();

            //if (GeoDataRequester.getInstance().hasMovementLoS(0,cl.x,cl.y,cl.z,dl.x,dl.y,dl.z) == true)
			//{
            //	result.add(new PathNode(dl,0,0,node));
            //	return result;
            	//return null;
			//}
			//else
			//{
	    		for (int i =0 ; i < 8 ; i ++)
	    		{
	    			if(( (PathNodeLocation)cl).nextNode[i] != 0)
	    			{
		    			nNode = PathNodeBinRequester.getInstance().getNode(((PathNodeLocation)cl).nextNode[i],cl.getX(),cl.getY());
		    			
		    			if (nNode != null)
		    			{
		    				//nNode.index = ((PathNodeLocation)cl).nextNode[i];
		    				handleNode(nNode,node,result,destx,desty,destz);
		    			}
		    			else
		    			{
		    				//System.out.println(((PathNodeLocation)cl).nextNode[i]);
		    			}
	    			}
	    		}
	    		return result;
			//}
    	}
    
    	public Node getNode(PathNodeLocation location)
        {
    		return(new PathNode(location,0,0,null));
        }
    
    }
    
    
    private class GeodataMapHandler extends MapHandler 
    {

        public GeodataMapHandler()
        {
            
        }

        public ArrayList<GeoNode> getAdjacentNodes(GeoNode node,Location dl)
        {
            Location cl = node.location;
            ArrayList<GeoNode> result = new ArrayList<GeoNode>();

            int destx = dl.getX();
            int desty = dl.getY();
            short destz =(short)dl.getZ();
            byte NSWE;
            
            GeoDataRequester.GeoSubCell cell;
            cell = GeoDataRequester.getInstance().getGeoInfoNearest(cl.getX(), cl.getY(),(short) cl.getZ());
            NSWE = cell.getNSEW();
            if ((NSWE & EAST) == EAST)
            {
                handleNode(cl.getX()+16,cl.getY(),
                        GeoDataRequester.getInstance().getGeoInfoNearest(cl.getX()+16, cl.getY(),(short) cl.getZ()).getZ(),
                        node,result,destx,desty,destz); 
            }
            
            if ((NSWE & WEST) == WEST)
            {
                handleNode(cl.getX()-16,cl.getY(),
                        GeoDataRequester.getInstance().getGeoInfoNearest(cl.getX()-16, cl.getY(),(short) cl.getZ()).getZ(),
                        node,result,destx,desty,destz);
            }
            if ((NSWE & SOUTH) == SOUTH)
            {
                handleNode(cl.getX(),cl.getY()+16,
                        GeoDataRequester.getInstance().getGeoInfoNearest(cl.getX(), cl.getY()+16,(short) cl.getZ()).getZ(),
                        node,result,destx,desty,destz);
            }
            if ((NSWE & NORTH) == NORTH)
            {
                handleNode(cl.getX(),cl.getY()-16,
                        GeoDataRequester.getInstance().getGeoInfoNearest(cl.getX(), cl.getY()-16,(short) cl.getZ()).getZ(),
                        node,result,destx,desty,destz);
            }
            return(result);
        }

        public void handleNode(int x, int y,short z, GeoNode from,ArrayList<GeoNode> result,int destx,int desty, short destz)
        {
            byte nswe; 
            int weight = 0;
            GeoNode n = getNode(new SquareLocation(x,y,z));
            nswe = GeoDataRequester.getInstance().getGeoInfoNearest(x, y, z ).getNSEW();
            int dx = 0;
            int dy = 0;
            // if this cell is near an object add weight to the movecost
            // this is to pass in the middle of doors
            if (nswe != 15)
            {
                weight = 64;
            }
            if(n!=null)
            {
                dx = Math.max(x,destx) - Math.min(x,destx);
                dy = Math.max(y,desty) - Math.min(y,desty);
                n.estimatedMoveCost = dx+dy;
                n.moveCost += from.moveCost + weight;
                n.score = n.moveCost+n.estimatedMoveCost;
                n.parent=from;
                if (n.score < 2000)
                {
                    result.add(n);
                }
            }
        }
        public GeoNode getNode(Location location)
        {
            return(new GeoNode(location,16,0,null));
        }
    }

    public class Path
    {
        int totalCost = 0;
        ArrayList<Node> nodes = null;
        
        public Path(ArrayList<Node> nodeList, int tCost)
        {
            this.nodes = nodeList;
            this.totalCost = tCost;
        }

        public ArrayList<Node> getNodes()
        {
            return(nodes);
        }

        public int getTotalMoveCost()
        {
            return(totalCost);
        }
    }

    public class SquareLocation extends Location
    {
        /** Creates a new instance of XYZLocation */
        public SquareLocation(int X, int Y,short Z )
        {
            super(X,Y,Z);
        }
    }
}
