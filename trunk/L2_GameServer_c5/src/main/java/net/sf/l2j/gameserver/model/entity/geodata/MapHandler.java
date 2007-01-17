package net.sf.l2j.gameserver.model.entity.geodata;
import java.util.ArrayList;

import net.sf.l2j.gameserver.model.Location;

    
    public abstract class MapHandler 
    {
    	
    	public void handleNode(int x, int y,short z, Node from,ArrayList<Node> result,int destx,int desty, short destz)
    	{
    		return;
    	}
    	
    	public ArrayList<Node> getAdjacentNodes(Node node,Location dl)
    	{
    		return null;
    	}
    	
    	public Node getNode(Location location)
    	{
    		return null;
    	}
    }
