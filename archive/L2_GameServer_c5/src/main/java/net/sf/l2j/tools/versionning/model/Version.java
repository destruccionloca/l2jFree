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
package net.sf.l2j.tools.versionning.model;


/**
 * 
 * Java beans used to store information about a version
 *
 */
public class Version
{
    
    private String revisionNumber="-1";
    private String versionNumber="-1";
    private String buildDate="";
    private String buildJdk="";
    /**
     * @return the buildDate
     */
    public String getBuildDate()
    {
        return buildDate;
    }

    /**
     * @return the buildJdk
     */
    public String getBuildJdk()
    {
        return buildJdk;      
    }

    /**
     * @return the revisionNumber
     */
    public String getRevisionNumber()
    {
        return revisionNumber;
    }

    /**
     * @return the versionNumber
     */
    public String getVersionNumber()
    {
        return versionNumber;
    }



    /**
     * @param buildDate the buildDate to set
     */
    public void setBuildDate(String buildDate)
    {
        this.buildDate = buildDate;
    }

    /**
     * @param buildJdk the buildJdk to set
     */
    public void setBuildJdk(String buildJdk)
    {
        this.buildJdk = buildJdk;
    }

    /**
     * @param revisionNumber the revisionNumber to set
     */
    public void setRevisionNumber(String revisionNumber)
    {
        this.revisionNumber = revisionNumber;
    }

    /**
     * @param versionNumber the versionNumber to set
     */
    public void setVersionNumber(String versionNumber)
    {
        this.versionNumber = versionNumber;
    }
    
    
    
}
