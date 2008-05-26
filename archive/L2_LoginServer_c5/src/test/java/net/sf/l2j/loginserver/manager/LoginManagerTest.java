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

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import junit.framework.TestCase;
import net.sf.l2j.Config;
import net.sf.l2j.loginserver.beans.SessionKey;
import net.sf.l2j.loginserver.services.exception.HackingException;
import net.sf.l2j.tools.L2Registry;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This class test ban management
 * 
 */
public class LoginManagerTest extends TestCase
{
    private ClassPathXmlApplicationContext context = null;
    private LoginManager loginManager;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        context = new ClassPathXmlApplicationContext("classpath*:/**/**/applicationContext-TestMock.xml");
        L2Registry.setApplicationContext(context);
        loginManager = LoginManager.getInstance();
    }

    /**
     *
     */
    public void testAssignSessionKey()
    {
        SessionKey sk = loginManager.assignSessionKeyToLogin("player1", null);
        assertNotNull(sk);

        assertTrue(loginManager.isAccountInLoginServer("player1"));
        assertEquals(loginManager.getKeyForAccount("player1"), sk);
    }

    /**
     *
     */
    public void testRemoveAccount()
    {
        loginManager.assignSessionKeyToLogin("player1", null);

        loginManager.removeAccountFromGameServer("player1");
        loginManager.removeAccountFromLoginServer("player1");

        assertTrue(!loginManager.isAccountInLoginServer("player1"));
        assertNull(loginManager.getKeyForAccount("player1"));

    }

    public void testChangeAccountLevel()
    {
        Config.GM_MIN = 100;
        // check that an account is a GM
        assertTrue(loginManager.isGM("player1"));
        loginManager.setAccountAccessLevel("player1", 1);
        assertTrue(!loginManager.isGM("player1"));
    }

    public void testConnection() throws IOException
    {
        Socket client=null;
        Config.LOGIN_TRY_BEFORE_BAN = 3;
        try
        {
            // just for test, open a socket on something
            client = new Socket("www.google.com", 80);
        }
        catch (UnknownHostException e1)
        {
            fail(e1.getMessage());
        }
        catch (IOException e1)
        {
            fail(e1.getMessage());
        }
        try
        {
            assertFalse(loginManager.loginValid("player1", "testpwd", client.getInetAddress()));
            assertTrue(loginManager.loginValid("player1", "testpwd1", client.getInetAddress()));
        }
        catch (HackingException e)
        {
            fail(e.getMessage());
        }
        finally
        {
            if (client != null)
            {
                client.close();
            }
        }
    }
    
    public void testHackingAttempt() throws IOException
    {
        Socket client=null;
        Config.LOGIN_TRY_BEFORE_BAN = 3;
        try
        {
            // just for test, open a socket on something
            client = new Socket("www.google.com", 80);
        }
        catch (UnknownHostException e1)
        {
            fail(e1.getMessage());
        }
        catch (IOException e1)
        {
            fail(e1.getMessage());
        }
        try
        {
            // First try, failed connect = 0
            assertFalse(loginManager.loginValid("player1", "testpwd", client.getInetAddress()));
            // 2nd try, failed connect = 1
            assertFalse(loginManager.loginValid("player1", "testpwd2", client.getInetAddress()));
            // 3rd try, failed connect = 2
            assertFalse(loginManager.loginValid("player1", "testpwd3", client.getInetAddress()));
            // 4th try, failed connect = 3
            assertFalse(loginManager.loginValid("player1", "testpwd4", client.getInetAddress()));
            // 5th try, failed connect = 4 and is > Config.LOGIN_TRY_BEFORE_BAN => ban ip
            assertFalse(loginManager.loginValid("player1", "testpwd5", client.getInetAddress()));
            fail ("not banned");
        }
        catch (HackingException e)
        {
            assertNotNull(e);
        }
        finally
        {
            if (client != null)
            {
                client.close();
            }
        }
        // don't forget to unban client to avoid perturbation on other tests
        loginManager.unblockIp(client.getInetAddress().getHostAddress());
    }
    
    public void testAutoCreateAccount () throws IOException
    {
        Socket client=null;
        Config.AUTO_CREATE_ACCOUNTS = true;
        try
        {
            // just for test, open a socket on something
            client = new Socket("www.google.fr", 80);
        }
        catch (UnknownHostException e1)
        {
            fail(e1.getMessage());
        }
        catch (IOException e1)
        {
            fail(e1.getMessage());
        }
        try
        {
            assertTrue(loginManager.loginValid("unknownplayer", "pwdforplayer", client.getInetAddress()));
        }
        catch (HackingException e)
        {
            fail(e.getMessage());
        }
        finally
        {
            if (client != null)
            {
                client.close();
            }
        }        
    }

}
