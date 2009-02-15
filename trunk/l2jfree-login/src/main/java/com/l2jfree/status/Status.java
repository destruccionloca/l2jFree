package com.l2jfree.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

import com.l2jfree.Config;

import javolution.util.FastList;

public class Status extends Thread
{

	private ServerSocket			statusServerSocket;

	private int						_uptime;
	private int						_StatusPort;
	private List<LoginStatusThread>	_loginStatus;

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				Socket connection = statusServerSocket.accept();

				LoginStatusThread lst = new LoginStatusThread(connection, _uptime);
				if (lst.isAlive())
				{
					_loginStatus.add(lst);
				}
				if (this.isInterrupted())
				{
					try
					{
						statusServerSocket.close();
					}
					catch (IOException io)
					{
						io.printStackTrace();
					}
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
					catch (IOException io)
					{
						io.printStackTrace();
					}
					break;
				}
			}
		}
	}

	public Status() throws IOException
	{
		super("Status");
		Properties telnetSettings = new Properties();
		InputStream is = new FileInputStream(new File(Config.TELNET_FILE));
		telnetSettings.load(is);
		is.close();

		_StatusPort = Integer.parseInt(telnetSettings.getProperty("StatusPort", "12345"));
		System.out.println("StatusServer Started! - Listening on Port: " + _StatusPort);
		statusServerSocket = new ServerSocket(_StatusPort);
		_uptime = (int) System.currentTimeMillis();
		_loginStatus = new FastList<LoginStatusThread>();
	}

	public void sendMessageToTelnets(String msg)
	{
		List<LoginStatusThread> lsToRemove = new FastList<LoginStatusThread>();
		for (LoginStatusThread ls : _loginStatus)
		{
			if (ls.isInterrupted())
			{
				lsToRemove.add(ls);
			}
			else
			{
				ls.printToTelnet(msg);
			}
		}
	}
}
