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
