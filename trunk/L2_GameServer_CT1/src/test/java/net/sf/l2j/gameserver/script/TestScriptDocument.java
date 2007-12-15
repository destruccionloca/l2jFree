package net.sf.l2j.gameserver.script;

import java.io.InputStream;

import junit.framework.TestCase;

public class TestScriptDocument extends TestCase
{
    public void testParseValidDocument ()
    {
        InputStream ip = getClass().getResourceAsStream("Valentines.xml");
        
        ScriptDocument sd = new ScriptDocument("test",ip);
        
        assertNotNull(sd.getDocument());
        assertEquals("test",sd.getName());
    }
    
    public void testParseInexistantDocument ()
    {
        InputStream ip = getClass().getResourceAsStream("notexistant.xml");
        
        ScriptDocument sd = new ScriptDocument("test",ip);
        
        assertNull(sd.getDocument());
        assertNull(sd.getName());
    }
    
    public void testParseNotValidDocument ()
    {
        InputStream ip = getClass().getResourceAsStream("ValentinesNotValid.xml");
        
        ScriptDocument sd = new ScriptDocument("test",ip);
        
        assertNull(sd.getDocument());
        assertEquals("test",sd.toString());
    }    
    
    
}
