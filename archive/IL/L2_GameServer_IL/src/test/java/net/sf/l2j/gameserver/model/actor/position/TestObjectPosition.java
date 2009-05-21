package net.sf.l2j.gameserver.model.actor.position;

import junit.framework.TestCase;
import net.sf.l2j.gameserver.ConfigHelper;
import net.sf.l2j.gameserver.model.L2DummyObject;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.tools.geometry.Point3D;

public class TestObjectPosition extends TestCase
{

    public void testCreateObjectPosition ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        ObjectPosition objectPosition = new ObjectPosition(l2Potion);
        assertNotNull(objectPosition);
    }
    
    public void testSetXYZ ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        ObjectPosition objectPosition = new ObjectPosition(l2Potion);
        assertNotNull(objectPosition);
        
        objectPosition.setXYZ(0, 1, 2);
        
        assertEquals(0,objectPosition.getX());
        assertEquals(1,objectPosition.getY());
        assertEquals(2,objectPosition.getZ());
        
        assertTrue(l2Potion.isVisible());
        
        assertEquals(0,objectPosition.getWorldPosition().getX());
        assertEquals(1,objectPosition.getWorldPosition().getY());
        assertEquals(2,objectPosition.getWorldPosition().getZ());        
    }
    
    public void testSetXYZInvisible ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        ObjectPosition objectPosition = new ObjectPosition(l2Potion);
        assertNotNull(objectPosition);
        
        objectPosition.setXYZInvisible(0, 1, 2);
        
        assertEquals(0,objectPosition.getX());
        assertEquals(1,objectPosition.getY());
        assertEquals(2,objectPosition.getZ());
        
        assertFalse(l2Potion.isVisible());
        
        assertEquals(0,objectPosition.getWorldPosition().getX());
        assertEquals(1,objectPosition.getWorldPosition().getY());
        assertEquals(2,objectPosition.getWorldPosition().getZ());
    }     
    
    public void testSetHeading ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        ObjectPosition objectPosition = new ObjectPosition(l2Potion);
        assertNotNull(objectPosition);
        
        objectPosition.setHeading(1);
        
        assertEquals(1,objectPosition.getHeading());
    }        
    
    public void testSetWorldPosition ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        ObjectPosition objectPosition = new ObjectPosition(l2Potion);
        assertNotNull(objectPosition);
        
        objectPosition.setWorldPosition(0, 1, 2);
        
        assertEquals(0,objectPosition.getX());
        assertEquals(1,objectPosition.getY());
        assertEquals(2,objectPosition.getZ());
        
        assertTrue(l2Potion.isVisible());
        
        assertEquals(0,objectPosition.getWorldPosition().getX());
        assertEquals(1,objectPosition.getWorldPosition().getY());
        assertEquals(2,objectPosition.getWorldPosition().getZ());        
    }
    
    public void testSetWorldPositionWithPoint3D ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        ObjectPosition objectPosition = new ObjectPosition(l2Potion);
        assertNotNull(objectPosition);
        Point3D point3D = new Point3D (0,1,2);
        objectPosition.setWorldPosition(point3D);
        
        assertEquals(0,objectPosition.getX());
        assertEquals(1,objectPosition.getY());
        assertEquals(2,objectPosition.getZ());
        
        assertTrue(l2Potion.isVisible());
        
        assertEquals(0,objectPosition.getWorldPosition().getX());
        assertEquals(1,objectPosition.getWorldPosition().getY());
        assertEquals(2,objectPosition.getWorldPosition().getZ());        
    }      
    
    public void testWorldRegion ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        ObjectPosition objectPosition = new ObjectPosition(l2Potion);
        assertNotNull(objectPosition);
        Point3D point3D = new Point3D (0,1,2);
        objectPosition.setWorldPosition(point3D);
        
        L2WorldRegion l2WorldRegion = objectPosition.getWorldRegion();
        
        objectPosition.setWorldPosition(new Point3D(25000,25000,25000));
        
        L2WorldRegion newL2WorldRegion = objectPosition.getWorldRegion();
        
        assertTrue (l2WorldRegion==newL2WorldRegion);
        
        objectPosition.updateWorldRegion();

        newL2WorldRegion = objectPosition.getWorldRegion();

        assertTrue (l2WorldRegion!=newL2WorldRegion);        
    }
    
}
