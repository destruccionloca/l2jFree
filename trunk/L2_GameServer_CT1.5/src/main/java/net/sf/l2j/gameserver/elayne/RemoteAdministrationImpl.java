package net.sf.l2j.gameserver.elayne;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.instancemanager.Manager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemChatChannelId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class RemoteAdministrationImpl extends UnicastRemoteObject implements IRemoteAdministration
{
    private static final long serialVersionUID = -8523099127883669758L;

    private static RemoteAdministrationImpl _instance;

    private IRemoteAdministration obj;

    @SuppressWarnings("unused")
    private Registry lReg;

    private String pass;

    private int port;

    public static RemoteAdministrationImpl getInstance()
    {
	if (_instance == null)
	    try
	    {
		_instance = new RemoteAdministrationImpl();
	    }
	    catch (RemoteException e)
	    {
		System.out.println("RemoteAdministrationImpl: Problems ocurred while starting RMI Server.");
		e.printStackTrace();
	    }
	return _instance;
    }

    public RemoteAdministrationImpl() throws RemoteException
    {
	super();
	this.pass = Config.RMI_SERVER_PASSWORD.toLowerCase();
	this.port = Config.RMI_SERVER_PORT;
    }

    public void startServer()
    {
	if (Config.ALLOW_RMI_SERVER && !pass.equals(null) && !pass.equals("") && port != 0)
	{
	    try
	    {
		lReg = LocateRegistry.createRegistry(port);
		obj = new RemoteAdministrationImpl();

		Naming.rebind("//localhost:" + port + "/Elayne", obj);
		System.out.println("RMI Server bound in registry: Port:" + port + ", Password: " + pass + ".");
	    }
	    catch (Exception e)
	    {
		System.out.println("RemoteAdministrationImpl error: " + e.getMessage());
		e.printStackTrace();
	    }
	}
	else
	    System.out.println("RMI Server is currently disabled.");
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#getOnlineUsersCount()
     */
    @Override
    public int getOnlineUsersCount(String password) throws RemoteException
    {
	if (password != pass)
	    return 0;
	return L2World.getInstance().getAllPlayersCount();
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#getPlayerInformation(java.lang.String)
     */
    @Override
    public IRemotePlayer getPlayerInformation(String password, String playerName) throws RemoteException
    {
	if (password != pass)
	    return null;
	L2PcInstance player = L2World.getInstance().getPlayer(playerName);
	if (player != null)
	    return new RemotePlayerImpl(player);
	return null;
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#announceToAll(java.lang.String)
     */
    @Override
    public void announceToAll(String password, String announcement) throws RemoteException
    {
	if (!password.equals(pass))
	    return;
	Announcements.getInstance().announceToAll(announcement);
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#abortServerRestart()
     */
    @Override
    public void abortServerRestart(String password) throws RemoteException
    {
	if (password.equals(pass))
	    Shutdown.getInstance().abort("127.0.0.1");
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#kickPlayerFromServer(java.lang.String)
     */
    @Override
    public int kickPlayerFromServer(String password, String playerName) throws RemoteException
    {
	if (password.equals(pass))
	{
	    L2PcInstance player = L2World.getInstance().getPlayer(playerName);
	    if (player != null)
	    {
		player.sendMessage("You are getting kicked out by a GM.");
		player.logout();
		return 1;
	    }
	    return 2;
	}
	else
	    return 3;
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#reload(int)
     */
    @Override
    public void reload(String password, int reloadProcedure) throws RemoteException
    {
	if (password.equals(pass))
	{
	    switch (reloadProcedure)
	    {
		case 1:
		    L2Multisell.getInstance().reload();
		    break;
		case 2:
		    SkillTable.getInstance().reload();
		    break;
		case 3:
		    NpcTable.getInstance().reloadAll();
		    break;
		case 4:
		    HtmCache.getInstance().reload();
		    break;
		case 5:
		    ItemTable.getInstance().reload();
		    break;
		case 6:
		    Manager.reloadAll();
		    break;
		case 7:
		    break;
		case 8:
		    TeleportLocationTable.getInstance().reloadAll();
		    break;
		case 9:
		    RaidBossSpawnManager.getInstance().cleanUp();
		    DayNightSpawnManager.getInstance().cleanUp();
		    L2World.getInstance().deleteVisibleNpcSpawns();
		    NpcTable.getInstance().reloadAll();
		    SpawnTable.getInstance().reloadAll();
		    RaidBossSpawnManager.getInstance().reloadBosses();
		    break;
	    }
	}
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#scheduleServerRestart(int)
     */
    @Override
    public void scheduleServerRestart(String password, int secondsUntilRestart) throws RemoteException
    {
	if (password.equals(pass))
	    Shutdown.getInstance().startShutdown("127.0.0.1", secondsUntilRestart, Shutdown.shutdownModeType.SHUTDOWN);
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#scheduleServerShutDown(int)
     */
    @Override
    public void scheduleServerShutDown(String password, int secondsUntilShutDown) throws RemoteException
    {
	if (password.equals(pass))
	    Shutdown.getInstance().startShutdown("127.0.0.1", secondsUntilShutDown, Shutdown.shutdownModeType.RESTART);

    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#sendMessageToGms(java.lang.String)
     */
    @Override
    public int sendMessageToGms(String password, String message) throws RemoteException
    {
	if (!password.equals(pass))
	    return 0;
	CreatureSay cs = new CreatureSay(0, 9, "Message From Elayne GM Tool", message);
	GmListTable.broadcastToGMs(cs);
	return GmListTable.getInstance().getAllGms(true).size();
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#sendPrivateMessage(java.lang.String, java.lang.String)
     */
    @Override
    public int sendPrivateMessage(String password, String player, String message) throws RemoteException
    {
	if (!password.equals(pass))
	    return 2;
	L2PcInstance reciever = L2World.getInstance().getPlayer(player);
	CreatureSay cs = new CreatureSay(0, SystemChatChannelId.Chat_Tell.getId(), "Elayne GM Tool MSG", message);
	if (reciever != null)
	{
	    reciever.sendPacket(cs);
	    return 1;
	}
	else
	    return 2;
    }

    /**
     * @see net.sf.l2j.gameserver.elayne.IRemoteAdministration#getOnlinePlayersDetails(java.lang.String)
     */
    @Override
    public FastMap<String, IRemotePlayer> getOnlinePlayersDetails(String rmiPassword) throws RemoteException
    {
	if (!rmiPassword.equals(pass))
	    return null;
	return null;
    }
}
