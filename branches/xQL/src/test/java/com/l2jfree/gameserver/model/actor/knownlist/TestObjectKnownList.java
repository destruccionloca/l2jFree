/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.model.actor.knownlist;

import com.l2jfree.gameserver.ConfigHelper;
import com.l2jfree.gameserver.model.L2DummyObject;
import com.l2jfree.gameserver.model.actor.knownlist.ObjectKnownList;

import junit.framework.TestCase;

public class TestObjectKnownList extends TestCase
{

    public void testCreateObjectKnownList ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        ObjectKnownList objectKnownList = new ObjectKnownList(l2Potion);
        assertNotNull(objectKnownList);
    }
    
    public void testKnowsObject ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        L2DummyObject l2Potion2 = new L2DummyObject(6);
        ObjectKnownList objectKnownList = new ObjectKnownList(l2Potion);
        assertNotNull(objectKnownList);
        
        assertFalse(objectKnownList.knowsObject(l2Potion2));
    }
    
    public void testAddKnownObject ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        L2DummyObject l2Potion2 = new L2DummyObject(6);
        ObjectKnownList objectKnownList = new ObjectKnownList(l2Potion);
        assertNotNull(objectKnownList);
        
        assertFalse(objectKnownList.knowsObject(l2Potion2));
        
        objectKnownList.addKnownObject(l2Potion2);
        
        assertTrue(objectKnownList.knowsObject(l2Potion2));
    }
    
    public void testGetDistanceToObject()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        L2DummyObject l2Potion2 = new L2DummyObject(6);
        ObjectKnownList objectKnownList = new ObjectKnownList(l2Potion);
        assertNotNull(objectKnownList);
        assertEquals(0, objectKnownList.getDistanceToForgetObject(l2Potion2));
        assertEquals(0, objectKnownList.getDistanceToWatchObject(l2Potion2));        
    }
    
    public void testRemoveKnownObject ()
    {
        ConfigHelper.configure();
        L2DummyObject l2Potion = new L2DummyObject(5);
        L2DummyObject l2Potion2 = new L2DummyObject(6);
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
        L2DummyObject l2Potion = new L2DummyObject(5);
        L2DummyObject l2Potion2 = new L2DummyObject(6);
        L2DummyObject l2Potion3 = new L2DummyObject(7);
        L2DummyObject l2Potion4 = new L2DummyObject(8);
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
