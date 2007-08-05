package net.sf.l2j.gameserver.model.actor.poly;

import net.sf.l2j.gameserver.model.L2Object;

public class ObjectPoly
{
    // =========================================================
    // Data Field
    private L2Object _activeObject;          
    private int _polyId;
    private String _polyType;
    private int _baseId;
    private boolean _firstMorph;

    
    // =========================================================
    // Constructor
    public ObjectPoly(L2Object activeObject)
    {
        _activeObject = activeObject;
    }
    
    // =========================================================
    // Method - Public
    public void setPolyInfo(String polyType, String polyId)
    {
        setPolyId(Integer.parseInt(polyId));     
        setPolyType(polyType);
    }
    
    
    public void setPolyInfo(String polyType, String polyId,String baseId)
        {
        	setPolyId(Integer.parseInt(polyId));     
        	setPolyType(polyType);
        	setBaseId(Integer.parseInt(baseId));
        	setFirstMorph(true);
        }

    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final L2Object getActiveObject()
    {
        return _activeObject;
    }
    
    public final boolean isMorphed() { return getPolyType() != null; }
    
    public final int getPolyId() { return _polyId; }
    public final void setPolyId(int value) { _polyId = value; }
    
    public final String getPolyType() { return _polyType; }
    public final void setPolyType(String value) { _polyType = value; }
    
    public final void setNotMorphed(){_polyType = null;}
    public final boolean isFirstMorph(){return getFirstMorph();};
             
    public final int getBaseId() { return _baseId; }
    public final void setBaseId(int value) { _baseId = value; }
           
    public final boolean getFirstMorph() { return _firstMorph; }
    public final void setFirstMorph(boolean value) { _firstMorph = value; }

}
