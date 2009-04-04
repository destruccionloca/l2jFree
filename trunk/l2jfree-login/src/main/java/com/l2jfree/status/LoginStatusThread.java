package com.l2jfree.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.l2jfree.loginserver.beans.Accounts;
import com.l2jfree.loginserver.manager.LoginManager;
import com.l2jfree.status.commands.Restart;
import com.l2jfree.status.commands.Shutdown;
import com.l2jfree.status.commands.Statistics;
import com.l2jfree.status.commands.UnblockIP;
import com.l2jfree.tools.codec.Base64;

public final class LoginStatusThread extends StatusThread
{
	public LoginStatusThread(Status server, Socket socket) throws IOException
	{
		super(server, socket);
		
		register(new Shutdown());
		register(new Restart());
		register(new Statistics());
		register(new UnblockIP());
	}
	
	@Override
	protected boolean login() throws IOException
	{
		print("Account: ");
		final String account = readLine();
		print("Password: ");
		final String password = readLine();
		
		if (!isValidGMAccount(account, password))
		{
			println("Incorrect login!");
			return false;
		}
		
		return true;
	}
	
	private boolean isValidGMAccount(String account, String password)
	{
		final Accounts acc = LoginManager.getInstance().getAccount(account);
		
		if (!LoginManager.getInstance().isGM(acc))
			return false;
		
		final String expectedPassword = acc.getPassword();
		
		if (expectedPassword == null || password == null)
			return false;
		
		try
		{
			final byte[] expectedPass = Base64.decode(expectedPassword);
			final byte[] givenPass = MessageDigest.getInstance("SHA").digest(password.getBytes("UTF-8"));
			
			return Arrays.equals(expectedPass, givenPass);
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
}
