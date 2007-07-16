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
import java.util.Random;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Status extends Thread
{
    private static final Log _log = LogFactory.getLog(Status.class.getName());
    
    private ServerSocket    statusServerSocket;
    
    private int                     _uptime;
    private int                     _StatusPort;
    private String                  _StatusPW;
    
    public void run()
    {
        while (true)
        {
            try
            {
                Socket connection = statusServerSocket.accept();
                
                    new GameStatusThread(connection, _uptime, _StatusPW);
                if (this.isInterrupted())
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
                if (this.isInterrupted())
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
        
        _StatusPort       = Integer.parseInt(telnetSettings.getProperty("StatusPort", "12345"));
        _StatusPW         = telnetSettings.getProperty("StatusPW");
            if (_StatusPW == null)
            {
                _log.warn("Server's Telnet Function Has No Password Defined!");
                _log.warn("A Password Has Been Automaticly Created!");
                _StatusPW = RndPW(10);
                _log.warn("Password Has Been Set To: " + _StatusPW);
            }
            _log.info("StatusServer Started! - Listening on Port: " + _StatusPort);
            _log.info("Password Has Been Set To: " + _StatusPW);
        statusServerSocket = new ServerSocket(_StatusPort);
        _uptime = (int) System.currentTimeMillis();
    }
    
    
    
    private String RndPW(int length)
    {
        TextBuilder password = new TextBuilder(); 
        String lowerChar= "qwertyuiopasdfghjklzxcvbnm";
        String upperChar = "QWERTYUIOPASDFGHJKLZXCVBNM";
        String digits = "1234567890";
        Random randInt = new Random();
        for (int i = 0; i < length; i++)
        {
            int charSet = randInt.nextInt(3);
            switch (charSet)
            {
                case 0:
                    password.append(lowerChar.charAt(randInt.nextInt(lowerChar.length()-1)));
                    break;
                case 1:
                    password.append(upperChar.charAt(randInt.nextInt(upperChar.length()-1)));
                    break;
                case 2:
                    password.append(digits.charAt(randInt.nextInt(digits.length()-1)));
                    break;
            }
        }
        return password.toString();
    }
}
