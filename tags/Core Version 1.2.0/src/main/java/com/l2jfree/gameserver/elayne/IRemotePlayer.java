package com.l2jfree.gameserver.elayne;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface defines "virtual" players from the server and implements methods that ease the organization of information from players inside the
 * server.
 * 
 * @author polbat02
 * 
 */
public interface IRemotePlayer extends Remote
{
	/**
	 * This method returns the name of a certain player.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;

	public String getAccount() throws RemoteException;

	public int getObjectId() throws RemoteException;

	public int getLevel() throws RemoteException;

	public int online() throws RemoteException;

	public int getAccessLevel() throws RemoteException;

	public int getSex() throws RemoteException;
}
