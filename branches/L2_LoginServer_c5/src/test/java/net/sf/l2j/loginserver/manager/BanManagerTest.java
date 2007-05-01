/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 * 
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
package net.sf.l2j.loginserver.manager;

import junit.framework.TestCase;

/**
 * This class test ban management
 * 
 */
public class BanManagerTest extends TestCase
{
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        BanManager.BAN_LIST = getClass().getResource("banlist.cfg").getFile().replace("%20", " ");
        BanManager.getInstance();
    }
    
    public void testLoadBanList ()
    {
        BanManager bm = BanManager.getInstance();
        assertEquals(3,bm.getNbOfBannedIp());
        assertTrue(bm.isIpBanned("127.0.0.1"));
    }
    
    public void testUnBan ()
    {
        BanManager bm = BanManager.getInstance();
        assertTrue(bm.isIpBanned("127.0.0.1"));
        
        bm.unBanIP("127.0.0.1");
        assertTrue(!bm.isIpBanned("127.0.0.1")); 
    }
    
    public void testBanIp () throws Exception
    {
        BanManager bm = BanManager.getInstance();
        if ( bm.isIpBanned("127.0.0.1"))
        {
            bm.unBanIP("127.0.0.1");
        }
        
        bm.addBannedIP("127.0.0.1",0);
        assertTrue(bm.isIpBanned("127.0.0.1"));
        // TODO the following should work
        //Thread.sleep(2000);
        // check that account is unban
        //assertTrue(!bm.isIpBanned("127.0.0.1"));
    }
}
