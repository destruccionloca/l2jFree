/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.status;

import java.io.IOException;
import java.net.Socket;

import com.l2jfree.Config;
import com.l2jfree.gameserver.datatables.CharNameTable;
import com.l2jfree.gameserver.datatables.CharNameTable.ICharacterInfo;
import com.l2jfree.status.commands.Abort;
import com.l2jfree.status.commands.Announce;
import com.l2jfree.status.commands.Clean;
import com.l2jfree.status.commands.Clear;
import com.l2jfree.status.commands.Decay;
import com.l2jfree.status.commands.Enchant;
import com.l2jfree.status.commands.GMChat;
import com.l2jfree.status.commands.GMList;
import com.l2jfree.status.commands.GameStat;
import com.l2jfree.status.commands.Give;
import com.l2jfree.status.commands.Halt;
import com.l2jfree.status.commands.IP;
import com.l2jfree.status.commands.IrcC;
import com.l2jfree.status.commands.IrcM;
import com.l2jfree.status.commands.Jail;
import com.l2jfree.status.commands.Kick;
import com.l2jfree.status.commands.Msg;
import com.l2jfree.status.commands.Performance;
import com.l2jfree.status.commands.Purge;
import com.l2jfree.status.commands.Reload;
import com.l2jfree.status.commands.ReloadConfig;
import com.l2jfree.status.commands.Restart;
import com.l2jfree.status.commands.ShutdownCommand;
import com.l2jfree.status.commands.Statistics;
import com.l2jfree.status.commands.Unjail;

// TODO: DynamicExtension related commands were dropped, add if necessary
public final class GameStatusThread extends StatusThread
{
	private final String _password;
	private String _gm;
	
	public GameStatusThread(GameStatusServer server, Socket socket, String password) throws IOException
	{
		super(server, socket);
		
		_password = password;
		
		register(new Abort());
		register(new Announce());
		register(new Clean());
		register(new Clear());
		register(new Decay());
		register(new Enchant());
		register(new GMChat());
		register(new GMList());
		register(new GameStat());
		register(new Give());
		register(new Halt());
		register(new IP());
		register(new IrcC());
		register(new IrcM());
		register(new Jail());
		register(new Kick());
		register(new Msg());
		register(new Performance());
		register(new Purge());
		register(new Reload());
		register(new ReloadConfig());
		register(new Restart());
		register(new ShutdownCommand());
		register(new Statistics());
		register(new Unjail());
	}
	
	public String getGM()
	{
		return _gm;
	}
	
	@Override
	protected boolean login() throws IOException
	{
		print("Password: ");
		final String password = readLine();
		
		if (password == null || !password.equals(_password))
		{
			println("Incorrect password!");
			return false;
		}
		
		if (Config.ALT_TELNET)
		{
			print("GM name: ");
			_gm = readLine();
			
			final ICharacterInfo info = CharNameTable.getInstance().getICharacterInfoByName(_gm);
			
			if (info == null || info.getAccessLevel() < 100)
			{
				println("Incorrect GM name!");
				return false;
			}
			else
			{
				println("Welcome, " + _gm + "!");
			}
		}
		else
		{
			println("Welcome!");
		}
		
		return true;
	}
}
