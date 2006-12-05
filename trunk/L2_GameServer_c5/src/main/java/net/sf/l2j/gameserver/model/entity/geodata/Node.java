package net.sf.l2j.gameserver.model.entity.geodata;
import net.sf.l2j.gameserver.model.Location;

    public abstract class Node
    {
        public Node parent=null;
        public Location location = null;
    	public int moveCost = 0;
        public int estimatedMoveCost = 0;
        public int score = 0; 
        public Node (Location loc, int mCost,int emCost, Node pNode)
        {
        	location = loc;
        	moveCost = mCost;
        	estimatedMoveCost = emCost;
        	parent = pNode;
        }
    }
