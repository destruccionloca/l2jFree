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
package net.sf.l2j.tools.versionning.service;

import net.sf.l2j.tools.versionning.dao.VersionDAO;
import net.sf.l2j.tools.versionning.model.Version;

/**
 * 
 * Utility class to retrieve version of the soft
 * 
 */
public class VersionningService
{
    private VersionDAO __versionDAO;
    
    /**
     * Instantiate a versionning service. 
     */
    public VersionningService ()
    {
    }
    
    /**
     * 
     * @return the soft version
     */
    public Version getVersion ()
    {
        if ( __versionDAO == null )
        {
            throw new NullPointerException ("dao is null, you have to initialize it with the setter first");
        }
        return __versionDAO.getVersion();
    }

    /**
     * @param _versiondao the  versionDAO to set
     */
    public void setVersionDAO(VersionDAO _versiondao)
    {
        __versionDAO = _versiondao;
    }
}
