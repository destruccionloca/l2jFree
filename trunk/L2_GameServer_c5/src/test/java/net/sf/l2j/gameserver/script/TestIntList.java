package net.sf.l2j.gameserver.script;

import junit.framework.TestCase;

public class TestIntList extends TestCase
{
    public void testGetIntListWithDash ()
    {
        int[] arrayInt = IntList.parse("4209-4125");
        assertNotNull(arrayInt);
        assertEquals(4209,arrayInt[0]);
        assertEquals(4125,arrayInt[1]);
    }
    
    public void testGetIntListWithComa ()
    {
        int[] arrayInt = IntList.parse("4209,4125");
        assertNotNull(arrayInt);
        assertEquals(4209,arrayInt[0]);
        assertEquals(4125,arrayInt[1]);
    }    
    
    public void testGetIntWithWrongFormat ()
    {
        try
        {
            IntList.parse("4209 4125");
            fail("You should have an error of format");
        } catch (NumberFormatException e)
        {
            assertNotNull(e);
        }
    }    
}
