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
package net.sf.l2j.loginserver.thread;

import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.manager.BanManager;
import net.sf.l2j.loginserver.manager.GameServerManager;
import net.sf.l2j.loginserver.manager.LoginManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Thread of loginserver
 * 
 * - Manage connection of clients
 * - Protect from flood
 * - Check if client is in banned list
 * 
 * @version $Revision: $ $Date: $
 * @author  Administrateur
 */
public class LoginServerThread extends FloodProtectedListener
{
    public final static int PROTOCOL_REV = 0x0102;
    
    private static LoginServerThread _instance;
    private static Log           _log = LogFactory.getLog(LoginServerThread.class);

    private LoginManager          loginController;

    private GameServerListener _gslistener;


    /**
     * Try to add a client
     * If client is not banned, launch a clientThread to communicate with him
     *  
     * @see net.sf.l2j.loginserver.thread.FloodProtectedListener#addClient(java.net.Socket)
     */
    @Override
    public void addClient(Socket s)
    {
        try
        {
            new ClientThread(s);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    /**
     * - Stop thread
     * - Stop gameserver listener
     * - Stop gameserver manager
     * 
     * @param restart
     */
    public void shutdown(boolean restart)
    {
        this.interrupt();
        _gslistener.interrupt();
        GameServerManager.getInstance().shutDown();
        close();
        if(restart)
        {
            Runtime.getRuntime().exit(2);
        }
        else
        {
            Runtime.getRuntime().exit(0);
        }
    }

    /**
     * 
     * @throws IOException
     */
    private LoginServerThread() throws IOException
    {
        super(Config.GAME_SERVER_LOGIN_HOST, Config.PORT_LOGIN);

        loginController = LoginManager.getInstance();
        
        _gslistener = GameServerListener.getInstance();
        _gslistener.start();
        
        // Initialize Ban Manager
        // -----------------------
        BanManager.getInstance();
    }
    
    /**
     * This returns a unique LoginServer instance (singleton)
     * This doesnt start the Login in the case of the creation of a new instance
     * like it used to do.
     * @throws GeneralSecurityException 
     */
    public static LoginServerThread getInstance()
    {
        // If no instances started before, try to start a new one
        if (_instance == null)
        {
            try
            {
                _instance = new LoginServerThread();
            }
            catch (IOException e)
            {
                // Throws the exception, if any
                _log.fatal(e.getMessage(),e);
            }
        }
        // Return the actual instance
        return _instance;
    }
    
    /**
     * 
     */
    public boolean unblockIp(String ipAddress)
    {
        if (loginController.unblockIp(ipAddress))
        {
            return true;
        }
        return false;
    }
    
    public static class ForeignConnection
    {
        /**
         * @param l
         */
        public ForeignConnection(long time)
        {
            lastConnection = time;
            connectionNumber = 1;
        }
        public int connectionNumber;
        public long lastConnection;
    }    

}
