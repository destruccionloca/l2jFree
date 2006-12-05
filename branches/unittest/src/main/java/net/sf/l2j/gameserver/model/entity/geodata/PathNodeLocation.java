package net.sf.l2j.gameserver.model.entity.geodata;    

import net.sf.l2j.gameserver.model.Location;

public class PathNodeLocation extends Location
{
    /** Creates a new instance of XYZLocation */
    public int[] nextNode = new int[8];
    public int index;
    public PathNodeLocation(int X, int Y, short Z)
    {
        super(X,Y,Z);
    }
}
