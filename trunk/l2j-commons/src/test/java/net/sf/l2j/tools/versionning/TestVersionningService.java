package net.sf.l2j.tools.versionning;

import junit.framework.TestCase;
import net.sf.l2j.tools.versionning.dao.impl.VersionDAOJar;
import net.sf.l2j.tools.versionning.model.Version;
import net.sf.l2j.tools.versionning.service.VersionningService;

import org.apache.commons.io.IOUtils;

import com.mysql.jdbc.Connection;

public class TestVersionningService extends TestCase
{
    public void testGetVersionNumber ()
    {
        VersionningService vs = new VersionningService ();
        VersionDAOJar vsDao = new VersionDAOJar();
        vsDao.setClazz(IOUtils.class);
        vs.setVersionDAO(vsDao);
        Version version = vs.getVersion();
        
        assertEquals("1.2",version.getVersionNumber());        
        
    }
    
    public void testGetBuildJdk ()
    {
        VersionningService vs = new VersionningService ();
        VersionDAOJar vsDao = new VersionDAOJar();
        vsDao.setClazz(Connection.class);
        vs.setVersionDAO(vsDao);
        Version version = vs.getVersion();
        assertEquals("1.4.2-b28 (Sun Microsystems Inc.)",version.getBuildJdk());
        
    }
}
