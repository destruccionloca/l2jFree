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
import java.io.InputStream;
import java.util.logging.LogManager;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.manager.GameServerManager;
import net.sf.l2j.loginserver.thread.LoginServerThread;
import net.sf.l2j.status.Status;
import net.sf.l2j.tools.L2Registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Main class for loginserver
 * 
 */
public class LoginServer
{
    private static Log _log = LogFactory.getLog(LoginServer.class);
    public static Status statusServer;

    /**
     * Instantiate loginserver and launch it
     * Initialize log folder, telnet console and registry
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Throwable
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

        // Get instance of loginserver thread
        // ----------------------------------
        LoginServerThread server = LoginServerThread.getInstance();
        _log.info("Stand Alone LoginServer Listening on port " + Config.PORT_LOGIN);
        server.start();

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
    }

}
