package net.sf.l2j.tools.math;

import junit.framework.TestCase;

public class TestMTRandom extends TestCase
{
    
    private static MTRandom _rnd = null;
    
    @Override
    protected void setUp() throws Exception
    {
        _rnd = new MTRandom();
        assertNotNull(_rnd);
        super.setUp();
    }
    
    public void testNextGaussian()  
    {
        for (int i=0 ;i<50;i++ )
        {
            double value = _rnd.nextGaussian();
            assertTrue( "Value was "+value, value <= 10.0 && value >= -10.0 );
        }
    }

    public void testBoolean ()  
    {
        for (int i=0 ;i<50;i++ )
        {
            Boolean value = _rnd.nextBoolean();
            assertTrue (value instanceof Boolean);
        }
    }    
}
