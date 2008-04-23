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
