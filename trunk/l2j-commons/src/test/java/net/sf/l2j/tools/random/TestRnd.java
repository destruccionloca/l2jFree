package net.sf.l2j.tools.random;

import junit.framework.TestCase;

public class TestRnd extends TestCase
{
    public void testNextGaussian()  
    {
        for (int i=0 ;i<50;i++ )
        {
            double value = Rnd.nextGaussian();
            assertTrue( "Value was "+value, value <= 10.0 && value >= -10.0 );
        }
    }

    public void testBoolean ()  
    {
        for (int i=0 ;i<50;i++ )
        {
            Boolean value = Rnd.nextBoolean();
            assertTrue (value instanceof Boolean);
        }
    }    
}
