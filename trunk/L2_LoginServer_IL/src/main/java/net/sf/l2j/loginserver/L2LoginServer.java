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
package net.sf.l2j.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.manager.BanManager;
import net.sf.l2j.loginserver.manager.GameServerManager;
import net.sf.l2j.loginserver.thread.GameServerListener;
import net.sf.l2j.status.Status;
import net.sf.l2j.tools.L2Registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jserver.mmocore.network.SelectorServerConfig;
import com.l2jserver.mmocore.network.SelectorThread;

/**
 * Main class for loginserver
 * 
 */
public class L2LoginServer
{
    private static Log _log = LogFactory.getLog(L2LoginServer.class);
    public static Status statusServer;
    private static L2LoginServer _instance;
    private SelectorThread<L2LoginClient> _selectorThread;
    private GameServerListener _gameServerListener;
    
    
    public static L2LoginServer getInstance()
    {
        return _instance;
    }    
    
    /**
     * Instantiate loginserver and launch it
     * Initialize log folder, telnet console and registry
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Throwable
    {
        _instance = new L2LoginServer();
    }
    
    public L2LoginServer() throws Throwable 
    {        
        // Local Constants
        // ----------------
        final String LOG_FOLDER = "log"; // Name of folder for log file
        final String LOG_NAME = "./config/logging.properties"; // Name of log file

        // Create log folder
        // ------------------
        File logFolder = new File(LOG_FOLDER);
        logFolder.mkdir();

        // Create input stream for log file -- or store file data into memory
        // ------------------------------------------------------------------
        InputStream is = new FileInputStream(new File(LOG_NAME));
        LogManager.getLogManager().readConfiguration(is);
        is.close();

        // Initialize config 
        // ------------------
        Config.load();
        
        // Initialize Application context (registry of beans)
        // ---------------------------------------------------
        L2Registry.loadRegistry(new String[]{"spring.xml"});
        
        // o Initialize GameServer Manager
        // ------------------------------
        GameServerManager.getInstance();
        
        // o Initialize ban list
        // ----------------------
        BanManager.getInstance();        

        SelectorServerConfig ssc = new SelectorServerConfig(Config.PORT_LOGIN);
        L2LoginPacketHandler loginPacketHandler = new L2LoginPacketHandler();
        SelectorHelper sh = new SelectorHelper();
        try
        {
            _selectorThread = new SelectorThread<L2LoginClient>(ssc, loginPacketHandler, sh, sh);
            _selectorThread.setAcceptFilter(sh);
        }
        catch (IOException e)
        {
            _log.fatal("FATAL: Failed to open Selector. Reason: "+e.getMessage(),e);
            System.exit(1);
        }
        
        try
        {
            _gameServerListener = new GameServerListener();
            _gameServerListener.start();
            _log.info("Listening for GameServers on "+Config.GAME_SERVER_LOGIN_HOST+":"+Config.GAME_SERVER_LOGIN_PORT);
        }
        catch (IOException e)
        {
            _log.fatal("FATAL: Failed to start the Game Server Listener. Reason: "+e.getMessage(),e);
            System.exit(1);
        }        
        
        // Start status telnet server
        // --------------------------
        if (Config.IS_TELNET_ENABLED)
        {
            statusServer = new Status();
            statusServer.start();
        }
        else
        {
            _log.info("Telnet server is currently disabled.");
        }
        
        try
        {
            _selectorThread.openServerSocket();
        }
        catch (IOException e)
        {
            _log.fatal("FATAL: Failed to open server socket. Reason: "+e.getMessage(),e);
            System.exit(1);
        }
        _selectorThread.start();
        _log.info("Login Server ready on port "+Config.PORT_LOGIN);        
    }

}
