package net.sf.l2j.gameserver.model.actor.knownlist;

import junit.framework.TestCase;
import net.sf.l2j.gameserver.ConfigHelper;
import net.sf.l2j.gameserver.model.L2Potion;

public class TestObjectKnownList extends TestCase
{

    public void testCreateObjectKnownList ()
    {
        ConfigHelper.configure();
        L2Potion l2Potion = new L2Potion(5);
        ObjectKnownList objectKnownList = new ObjectKnownList(l2Potion);
        assertNotNull(objectKnownList);
    }
    
    public void testKnowsObject ()
    {
        ConfigHelper.configure();
        L2Potion l2Potion = new L2Potion(5);
        L2Potion l2Potion2 = new L2Potion(6);
        ObjectKnownList objectKnownList = new ObjectKnownList(l2Potion);
        assertNotNull(objectKnownList);
        
        assertFalse(objectKnownList.knowsObject(l2Potion2));
    }
    
    public void testAddKnownObject ()
    {
        ConfigHelper.configure();
        L2Potion l2Potion = new L2Potion(5);
        L2Potion l2Potion2 = new L2Potion(6);
        ObjectKnownList objectKnownList = new ObjectKnownList(l2Potion);
        assertNotNull(objectKnownList);
        
        assertFalse(objectKnownList.knowsObject(l2Potion2));
        
        objectKnownList.addKnownObject(l2Potion2);
        
        assertTrue(objectKnownList.knowsObject(l2Potion2));
    }
    
    public void testGetDistanceToObject()
    {
        ConfigHelper.configure();
        L2Potion l2Potion = new L2Potion(5);
        L2Potion l2Potion2 = new L2Potion(6);
        ObjectKnownList objectKnownList = new ObjectKnownList(l2Potion);
        assertNotNull(objectKnownList);
        assertEquals(0, objectKnownList.getDistanceToForgetObject(l2Potion2));
        assertEquals(0, objectKnownList.getDistanceToWatchObject(l2Potion2));        
    }
    
    public void testRemoveKnownObject ()
    {
        ConfigHelper.configure();
        L2Potion l2Potion = new L2Potion(5);
        L2Potion l2Potion2 = new L2Potion(6);
        ObjectKnownList objectKnownList = new ObjectKnownList(l2Potion);
        assertNotNull(objectKnownList);
        
        assertFalse(objectKnownList.knowsObject(l2Potion2));
        
        objectKnownList.addKnownObject(l2Potion2);
        
        assertTrue(objectKnownList.knowsObject(l2Potion2));

        objectKnownList.removeKnownObject(l2Potion2);
        assertFalse(objectKnownList.knowsObject(l2Potion2));
    }    
    
    public void testRemoveAllKnownObjects ()
    {
        ConfigHelper.configure();
        L2Potion l2Potion = new L2Potion(5);
        L2Potion l2Potion2 = new L2Potion(6);
        L2Potion l2Potion3 = new L2Potion(7);
        L2Potion l2Potion4 = new L2Potion(8);
        ObjectKnownList objectKnownList = new ObjectKnownList(l2Potion);
        assertNotNull(objectKnownList);
        
        assertFalse(objectKnownList.knowsObject(l2Potion2));
        
        objectKnownList.addKnownObject(l2Potion2);
        
        assertTrue(objectKnownList.knowsObject(l2Potion2));

        objectKnownList.removeKnownObject(l2Potion2);
        assertFalse(objectKnownList.knowsObject(l2Potion2));
        
        objectKnownList.removeAllKnownObjects();
        
        assertFalse(objectKnownList.knowsObject(l2Potion3));
        assertFalse(objectKnownList.knowsObject(l2Potion4));
    }        
        
    
    
}
