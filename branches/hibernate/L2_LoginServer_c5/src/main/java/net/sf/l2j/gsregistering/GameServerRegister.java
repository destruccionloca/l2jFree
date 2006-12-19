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
package net.sf.l2j.gsregistering;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Properties;

import net.sf.l2j.Config;
import net.sf.l2j.L2Registry;
import net.sf.l2j.loginserver.beans.GameServer;
import net.sf.l2j.loginserver.beans.Gameservers;
import net.sf.l2j.loginserver.manager.GameServerManager;
import net.sf.l2j.util.HexUtil;

public class GameServerRegister
{
	private static String _choice;
	private static GameServerManager gsTable;
	private static boolean _choiseOk;

	public static void main(String[] args) throws IOException
	{
		Config.load();
        L2Registry.loadRegistry();
		gsTable = GameServerManager.getInstance();
		System.out.println("Welcome to l2j GameServer Registering");
		System.out.println("Enter The id of the server you want to register or type help to get a list of ids:");
		LineNumberReader _in = new LineNumberReader(new InputStreamReader(System.in));
		while(!_choiseOk)
		{
			System.out.println("Your choice:");
			_choice = _in.readLine();
			if(_choice.equalsIgnoreCase("help"))
			{
				for(Gameservers gs : gsTable.getServers())
				{
					System.out.println("Server: id:"+gs.getServerId()+" - "+gs.getServerName());
				}
				System.out.println("You can also see servername.xml");
			}
			else
			{
				try
				{
					int id = new Integer(_choice).intValue();
					if(id >= gsTable.getServers().size())
					{
						System.out.println("ID is too high (max is "+(gsTable.getServers().size()-1)+")");
						continue;
					}
					if(id < 0)
					{
						System.out.println("ID must be positive number");
						continue;
					}
					else
					{
						if(gsTable.isIDfree(id))
						{
							byte[] hex = HexUtil.generateHex(16);
							gsTable.createServer(new GameServer(hex , id));
							saveHexid(new BigInteger(hex).toString(16),"hexid(server "+id+").txt");
							System.out.println("Server Registered hexid saved to 'hexid(server "+id+").txt'");
							System.out.println("Put this file in the /config folder of your gameserver and rename it to 'hexid.txt'");
							return;
						}
						else
						{
							System.out.println("This id is not free");
						}
					}
				}
				catch (NumberFormatException nfe)
				{
					System.out.println("Please, type a number or 'help'");
				}
			}
		}
	}
    


    /**
     * Save hexadecimal ID of the server in the properties file.
     * @param string (String) : hexadecimal ID of the server to store
     * @param fileName (String) : name of the properties file
     */
    public static void saveHexid(String string, String fileName)
    {
        try
        {
            Properties hexSetting    = new Properties();
            File file = new File(fileName);
            //Create a new empty file only if it doesn't exist
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);
            hexSetting.setProperty("HexID",string);
            hexSetting.store(out,"the hexID to auth into login");
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}