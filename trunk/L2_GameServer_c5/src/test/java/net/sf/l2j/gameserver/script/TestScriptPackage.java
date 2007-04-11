package net.sf.l2j.gameserver.script;

import java.io.IOException;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

public class TestScriptPackage extends TestCase
{
    public void testOpenScriptValid() throws IOException
    {
        ScriptPackage sp=null;
        String zipFile = this.getClass().getResource("Valentines.zip").getFile().replace("%20", " ");
        sp = new ScriptPackage(new ZipFile(zipFile));
        assertEquals(sp.getScriptFiles().size(),1 );
    }
    
    public void testOpenNotExistentScript()
    {
        try
        {
            new ScriptPackage(new ZipFile("unknownfile"));
            fail("File is found !");
        } 
        catch (IOException e)
        {
            assertNotNull(e);
        }
    }    
    
    public void testEmptyPackage() throws IOException
    {
        ScriptPackage sp=null;
        String zipFile = this.getClass().getResource("emptydata.zip").getFile().replace("%20", " ");
        sp = new ScriptPackage(new ZipFile(zipFile));
        assertEquals(sp.getScriptFiles().size(),0 );
    }    
    
    public void testZipPackageWithInvalidScript() throws IOException
    {
        ScriptPackage sp=null;
        String zipFile = this.getClass().getResource("ValentinesNotValid.zip").getFile().replace("%20", " ");
        sp = new ScriptPackage(new ZipFile(zipFile));
        // We found 1 document. But the ScriptDocument does not contain any document
        assertEquals(sp.getScriptFiles().size(),1 );
        assertEquals(sp.getScriptFiles().get(0).getDocument(),null );
    }         
}
