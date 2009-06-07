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
package com.l2jfree.gameserver;

import com.l2jfree.Config;
import com.l2jfree.gameserver.loginserverthread.LoginServerThreadBase;
import com.l2jfree.gameserver.loginserverthread.LoginServerThreadL2j;
import com.l2jfree.gameserver.loginserverthread.LoginServerThreadL2jfree;

public class LoginServerThread
{
	private static LoginServerThreadBase	_instance;

	public void stopInstance()
	{
		_instance.interrupt();
		_instance = null;
	}

	public static LoginServerThreadBase getInstance()
	{
		if (_instance == null)
		{
			if (Config.L2JFREE_LOGIN)
				_instance = new LoginServerThreadL2jfree();
			else
				_instance = new LoginServerThreadL2j();
		}
		return _instance;
	}

}
