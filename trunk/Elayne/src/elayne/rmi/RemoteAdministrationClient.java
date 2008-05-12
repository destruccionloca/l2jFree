package elayne.rmi;

import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.RemoteException;

import net.sf.l2j.gameserver.elayne.IRemoteAdministration;
import net.sf.l2j.gameserver.elayne.IRemotePlayer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;

import elayne.application.Activator;
import elayne.model.instance.L2CharacterBriefEntry;
import elayne.preferences.LoginPreferencePage;

public class RemoteAdministrationClient
{
	IPreferencesService service = Platform.getPreferencesService();
	String rmiPassword = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.RMI_SERVER_PASSWORD, "password", null).toLowerCase();
	String serverDbHost = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.SERVER_DB_HOST, "127.0.0.1", null);
	String port = service.getString(Activator.PLUGIN_ID, LoginPreferencePage.RMI_SERVER_PORT, "1099", null);

	private static RemoteAdministrationClient _instance;

	public static RemoteAdministrationClient getInstance()
	{
		if (_instance == null)
			_instance = new RemoteAdministrationClient();
		return _instance;
	}

	private IRemoteAdministration ra;
	private boolean connected = false;

	public void connect()
	{
		try
		{
			ra = (IRemoteAdministration) Naming.lookup("//" + serverDbHost + ":" + port + "/Elayne");

			System.out.println("RemoteAdministrationClient: Connected to RMI Server.");
			connected = true;
		}
		catch (ConnectException ex)
		{
			System.out.println("RemoteAdministrationClient: Server Refused the Connection.");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public boolean isConnected()
	{
		return connected;
	}

	/**
	 * Returns the number of online players in the server.
	 * @return
	 */
	public int getOnlineUsersCount()
	{
		int usersOnline = 0;
		try
		{
			usersOnline = ra.getOnlineUsersCount(rmiPassword);
		}
		catch (RemoteException e)
		{
			System.out.println("RemoteAdministrationClient: Problems occurred while getting server Online Users Cound: " + e.getMessage() + ".");
			e.printStackTrace();
		}
		return usersOnline;
	}

	/**
	 * This is just a kick example on how to get certain information we need for
	 * the tool from interfaces.<br>
	 * Here we're working with interfaces just as we where working with normal
	 * Java Classes.<br>
	 * From this method we will get a new {@link L2CharacterBriefEntry}.
	 * @param playerName
	 * @throws RemoteException
	 */
	public L2CharacterBriefEntry getPlayer(String playerName) throws RemoteException
	{
		// Define a new L2CharacterBriefEntry.
		L2CharacterBriefEntry bp = null;
		IRemotePlayer player;
		// Get the player information from the server.
		player = ra.getPlayerInformation(rmiPassword, playerName);
		// Once we have the required information define the L2CharacterBriefEntry.
		bp = new L2CharacterBriefEntry(player.getObjectId(), player.getLevel(), player.getName(), player.getAccount(), player.online(), player.getAccessLevel(), player.getSex(), player.getClanId());
		return bp;
	}

	/**
	 * Performs an announcement inside the game.
	 * @param announcement
	 * @throws RemoteException
	 */
	public void announceToAll(String announcement) throws RemoteException
	{
		ra.announceToAll(rmiPassword, announcement);
	}

	/**
	 * Attempts to send a private message to an online Player.
	 * @param player --> The player that will receive the message.
	 * @param message --> The message to broadcast to the player.
	 * @return: 1 if message was sent and the player receive it. 2 if player is
	 * not online or is null. 3 other errors.
	 * @throws RemoteException
	 */
	public int sendPrivateMessage(String player, String message) throws RemoteException
	{
		return ra.sendPrivateMessage(rmiPassword, player, message);
	}

	/**
	 * Attempts to send a message to Any GMs online at one particular moment.
	 * @param message
	 * @return
	 * @throws RemoteException
	 */
	public int sendMessageToGms(String message) throws RemoteException
	{
		return ra.sendMessageToGms(rmiPassword, message);
	}

	/**
	 * This method will try to kick a player from the Server.
	 * @param playerName --> The player that needs to be kicked from the server.
	 * @return: 1 if the player was found in game and was successfully kicked. 2
	 * if the player was not found in game. 3 other errors.
	 * @throws RemoteException
	 */
	public int kickPlayerFromServer(String playerName) throws RemoteException
	{
		return ra.kickPlayerFromServer(rmiPassword, playerName);
	}

	/**
	 * Attempt a server restart once the given seconds are over.
	 * @param secondsUntilRestart
	 * @throws RemoteException
	 */
	public void scheduleServerRestart(int secondsUntilRestart) throws RemoteException
	{
		ra.scheduleServerRestart(rmiPassword, secondsUntilRestart);
	}

	/**
	 * Attempt a server shut down once the given seconds are over.
	 * @param secondsUntilShutDown
	 * @throws RemoteException
	 */
	public void scheduleServerShutDown(int secondsUntilShutDown) throws RemoteException
	{
		ra.scheduleServerShutDown(rmiPassword, secondsUntilShutDown);
	}

	/**
	 * Attempt to abort a server restart/shut down procedure.
	 * @throws RemoteException
	 */
	public void abortServerRestart() throws RemoteException
	{
		ra.abortServerRestart(rmiPassword);
	}

	/**
	 * Reloads something in-game.
	 * @param reloadProcedure --> Allowed procedures: 1(MULTISELL), 2(SKILLS),
	 * 3(NPC), 4(HTML), 5(ITEMS), 6(INSTANCE MANAGERS), 7(ZONES), 8(TELEPORTS),
	 * 9(SPAWNS).
	 * @throws RemoteException
	 */
	public void reload(int reloadProcedure) throws RemoteException
	{
		ra.reload(rmiPassword, reloadProcedure);
	}

	/**
	 * Adds an item to a particular player.
	 * @param playerName
	 * @param itemId
	 * @param amount
	 * @return true if the item was handed out correctly, false else.
	 * @throws RemoteException
	 */
	public int awardItemToPlayer(String playerName, int itemId, int amount) throws RemoteException
	{
		return ra.awardItemToPlayer(rmiPassword, playerName, itemId);
	}
}
