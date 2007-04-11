/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package net.sf.l2j.tools.versionning.dao.impl;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import net.sf.l2j.tools.versionning.dao.VersionDAO;
import net.sf.l2j.tools.versionning.model.Version;
import net.sf.l2j.tools.versionning.service.Locator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Concrete class used to retrieve a soft information from its jar
 *
 */
public class VersionDAOJar implements VersionDAO
{
    
    private static final Log _log = LogFactory.getLog(VersionDAOJar.class);    
    
    private Version __version=null;
    
    private Class clazz;
    
    public VersionDAOJar ()
    {
    }
    
    private void loadInformation ()
    {
        File jarName = null;
        __version = new Version();
        try
        {
            jarName = Locator.getClassSource(clazz);
            JarFile jarFile = new JarFile(jarName);

            Attributes attrs = jarFile.getManifest().getMainAttributes();
            
            setBuildJdk(attrs);
            
            setRevisionNumber(attrs);
            
            setVersionNumber(attrs);        
        } 
        catch (IOException e)
        {
            if (_log.isErrorEnabled())
                _log.error("Unable to get Soft information\nFile name '" + (jarName == null ? "null" : jarName.getAbsolutePath()) + "' isn't a valid jar", e);
        }        
        
    }


    /**
     * @param attrs
     */
    private void setVersionNumber(Attributes attrs)
    {
        String versionNumber = attrs.getValue("Implementation-Version");
        if (versionNumber != null )
        {
            __version.setVersionNumber(versionNumber);
        }
        else
        {
            __version.setVersionNumber("-1");
        }
    }


    /**
     * @param attrs
     */
    private void setRevisionNumber(Attributes attrs)
    {
        String revisionNumber = attrs.getValue("Implementation-Build");
        if (revisionNumber != null )
        {
            __version.setRevisionNumber(revisionNumber);
        }
        else
        {
            __version.setRevisionNumber("-1");
        }
    }


    /**
     * @param attrs
     */
    private void setBuildJdk(Attributes attrs)
    {
        String buildJdk = attrs.getValue("Build-Jdk");
        if (buildJdk != null )
        {
            __version.setBuildJdk(buildJdk);
        }
        else
        {
            buildJdk = attrs.getValue("Created-By");
            if (buildJdk != null )
            {
                __version.setBuildJdk(buildJdk);
            } 
            else
            {
                __version.setBuildJdk("-1");
            }
        }
    }
    
    
    public Version getVersion( )
    {
        return __version;
    }


    /**
     * @return the clazz
     */
    public Class getClazz()
    {
        return clazz;
    }


    /**
     * @param clazz the clazz to set
     */
    public void setClazz(Class clazz)
    {
        this.clazz = clazz;
        loadInformation();
    }

}
