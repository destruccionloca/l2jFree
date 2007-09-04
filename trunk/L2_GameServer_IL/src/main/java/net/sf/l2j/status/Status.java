/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.lib.Rnd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Status extends Thread
{
    private static final Log _log = LogFactory.getLog(Status.class.getName());
    
    private ServerSocket    statusServerSocket;
    
    private int                     _uptime;
    private int                     _statusPort;
    private String                  _statusPw;
    
    public void run()
    {
        while (true)
        {
            try
            {
                Socket connection = statusServerSocket.accept();
                
                    new GameStatusThread(connection, _uptime, _statusPw);
                if (isInterrupted())
                {
                    try
                    {
                        statusServerSocket.close();
                    }
                    catch (IOException io) { _log.warn(io.getMessage(),io); }
                    break;
                }
            }
            catch (IOException e)
            {
                if (isInterrupted())
                {
                    try
                    {
                        statusServerSocket.close();
                    }
                    catch (IOException io) { _log.warn(io.getMessage(),io); }
                    break;
                }
            }
        }
    }
    
    public Status() throws IOException
    {
        super("Status");
        Properties telnetSettings = new Properties();
        InputStream is = new FileInputStream( new File(Config.TELNET_FILE));
        telnetSettings.load(is);
        is.close();
        
        _statusPort       = Integer.parseInt(telnetSettings.getProperty("StatusPort", "12345"));
        _statusPw         = telnetSettings.getProperty("StatusPW");
            if (_statusPw == null)
            {
                _log.warn("Server's Telnet Function Has No Password Defined!");
                _log.warn("A Password Has Been Automaticly Created!");
                _statusPw = RndPW(10);
                _log.warn("Password Has Been Set To: " + _statusPw);
            }
            _log.info("StatusServer Started! - Listening on Port: " + _statusPort);
            _log.info("Password Has Been Set To: " + _statusPw);
        statusServerSocket = new ServerSocket(_statusPort);
        _uptime = (int) System.currentTimeMillis();
    }
    
    
    
    private String RndPW(int length)
    {
        TextBuilder password = new TextBuilder(); 
        String lowerChar= "qwertyuiopasdfghjklzxcvbnm";
        String upperChar = "QWERTYUIOPASDFGHJKLZXCVBNM";
        String digits = "1234567890";
        for (int i = 0; i < length; i++)
        {
            int charSet = Rnd.nextInt(3);
            switch (charSet)
            {
                case 0:
                    password.append(lowerChar.charAt(Rnd.nextInt(lowerChar.length()-1)));
                    break;
                case 1:
                    password.append(upperChar.charAt(Rnd.nextInt(upperChar.length()-1)));
                    break;
                case 2:
                    password.append(digits.charAt(Rnd.nextInt(digits.length()-1)));
                    break;
            }
        }
        return password.toString();
    }
}
