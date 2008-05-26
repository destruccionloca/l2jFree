package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.ConfigHelper;
import net.sf.l2j.gameserver.model.actor.knownlist.ObjectKnownList;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.model.actor.position.ObjectPosition;
import junit.framework.TestCase;

public class TestL2Object extends TestCase
{
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ConfigHelper.configure();
    }
    
    public void testCreation ()
    {
        L2DummyObject l2DummyObject = new L2DummyObject(54);
        assertTrue(l2DummyObject instanceof L2Object);
    }
    
    public void testGetPositionNotNull ()
    {
        L2DummyObject l2DummyObject = new L2DummyObject(54);
        ObjectPosition objectPosition = l2DummyObject.getPosition();
        
        assertNotNull(objectPosition);
        
        assertEquals(0,l2DummyObject.getX());
        assertEquals(0,l2DummyObject.getY());
        assertEquals(0,l2DummyObject.getZ());
    }
    
    
    public void testGetPolyNotNull ()
    {
        L2DummyObject l2DummyObject = new L2DummyObject(54);
        ObjectPoly objectPoly = l2DummyObject.getPoly();
        
        assertNotNull(objectPoly);
        
        assertEquals(0,objectPoly.getPolyId());
        assertEquals(null,objectPoly.getPolyType());
    }
    
    public void testRefreshID ()
    {
        L2DummyObject l2DummyObject = new L2DummyObject(54);
        assertTrue(l2DummyObject.getObjectId()==54);
        
        l2DummyObject.refreshID();
        
        assertTrue(l2DummyObject.getObjectId()!=54);
    }    
    
    public void testVisibility ()
    {
        L2DummyObject l2DummyObject = new L2DummyObject(54);
        assertTrue(l2DummyObject.isVisible());
        
        l2DummyObject.setIsVisible(false);
        
        assertTrue(!l2DummyObject.isVisible());
    }    

    public void testGetKnownListNotNull ()
    {
        L2DummyObject l2DummyObject = new L2DummyObject(54);
        ObjectKnownList objectKnownList = l2DummyObject.getKnownList();
        
        assertNotNull(objectKnownList);
        
        assertEquals(0,objectKnownList.getKnownObjects().size());
   }
    
    public void testSpawnMe ()
    {
        L2DummyObject l2DummyObject = new L2DummyObject(54);
        
        l2DummyObject.spawnMe(1, 1, 1);
        
        assertEquals(1,l2DummyObject.getX());
        assertEquals(1,l2DummyObject.getY());
        assertEquals(1,l2DummyObject.getZ());     
        
        assertNotNull(L2World.getInstance().findObject(54));
   }    
    
    public void testDecayMe ()
    {
        L2DummyObject l2DummyObject = new L2DummyObject(54);
        
        l2DummyObject.decayMe();
        
        assertTrue( ! l2DummyObject.isVisible());    
        assertNull(L2World.getInstance().findObject(54));
        assertTrue(l2DummyObject.getWorldRegion()==null);
   }
    
    public void testToggleVisible ()
    {
        L2DummyObject l2DummyObject = new L2DummyObject(55);
        
        // should decay the L2Object if it was previously visible
        l2DummyObject.toggleVisible();
        
        assertTrue( ! l2DummyObject.isVisible());    
        assertNull(L2World.getInstance().findObject(54));
        assertTrue(l2DummyObject.getWorldRegion()==null);
        
        l2DummyObject.setIsVisible(true);
        
        // should decay the L2Object if it was previously visible
        l2DummyObject.toggleVisible();
        
        assertEquals(0,l2DummyObject.getX());
        assertEquals(0,l2DummyObject.getY());
        assertEquals(0,l2DummyObject.getZ());     
        
        assertNotNull(L2World.getInstance().findObject(55));        
        
   }        
    
}
