/**
 * 
 */
package elayne.actions;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.model.instance.L2PcInstance;
import elayne.rmi.RemoteAdministrationClient;
import elayne.util.connector.LoginDB;
import elayne.util.connector.ServerDB;

/**
 * The {@link RequestPlayerBan} class defines different actions that are used to
 * manage banning and unbanning inside the game. As already said, this action
 * can have different results depending on a parameter in the constructor. For
 * instance:<br>
 * Action Id 1 will ban a player.<br>
 * Action Id 2 will ban a player's account.<br>
 * Action Id 3 will ban any account that has the players last IP. Be careful
 * when calling this action since you could ban entire Internet Cafes.<br>
 * Action Id 4 will re allow a player back into the game.<br>
 * Action Id 5 will re allow an account back into the game.<br>
 * Action Id 6 will re allow all the accounts that share the player's last IP.<br>
 * @author polbat02
 */
public class RequestPlayerBan extends ElayneAction
{

	public static final int BAN_ACCOUNT = 2;
	public static final int BAN_ALL_ACCOUNTS = 3;
	public static final int BAN_PLAYER = 1;
	public static final String ID = "requestPlayerBan";
	public static final int UN_BAN_ACCOUNT = 5;
	public static final int UN_BAN_ALL_ACCOUNTS = 6;

	public static final int UN_BAN_PLAYER = 4;

	private int _banActionId = 0;

	/**
	 * Starts a new Ban / UnBan action. See {@link RequestPlayerBan} for more
	 * information.
	 * @param window
	 * @param treeViewer
	 * @param banAction the action that will be executed upon run.
	 */
	public RequestPlayerBan(IWorkbenchWindow window, TreeViewer treeViewer, int banAction)
	{
		super(window, treeViewer);
		_banActionId = banAction;
		setNewId(ID);
		setText(this.toString());
		setToolTipText(getToolTipText());
		setImageDescriptor(getDescriptor());
	}

	private String accountIP(String account)
	{
		java.sql.Connection con = null;
		String ip = "";

		try
		{
			con = LoginDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT COUNT(lastIP) FROM accounts WHERE login=?");
			statement.setString(1, account);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				ip = rset.getString(1);
			}
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{
				System.out.println("RequestBanPlayer: Exception while closing connection.");
			}
		}
		System.out.println("RequestBanPlayer: IP for the account " + account + " = " + ip);
		return ip;
	}

	private boolean banAccount(String account)
	{
		String sql = "UPDATE accounts SET `accessLevel`='-100' WHERE (`login`=?)";
		Connection con = null;
		boolean executed = false;
		try
		{
			con = LoginDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, account);
			statement.executeUpdate();
			statement.close();
			executed = true;
		}
		catch (Exception e)
		{
			System.out.println("RequestBanPlayer: Problems ocurred during the banning process: ");
			e.printStackTrace();

		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return executed;
	}

	private boolean banAllAccounts(String lastIP)
	{
		String sql = "UPDATE accounts SET `accessLevel`='-100' WHERE (`lastIP`=?)";
		Connection con = null;
		boolean executed = false;
		try
		{
			con = LoginDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, lastIP);
			statement.executeUpdate();
			statement.close();
			executed = true;
		}
		catch (Exception e)
		{
			System.out.println("RequestBanPlayer: Problems ocurred during the banning process: ");
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return executed;
	}

	private boolean banPlayer(int objectId)
	{
		String sql = "UPDATE `characters` SET `accesslevel`='-100' WHERE (`charId`=?)";
		Connection con = null;
		boolean executed = false;
		try
		{
			con = ServerDB.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, objectId);
			statement.executeUpdate();
			statement.close();
			executed = true;
		}
		catch (Exception e)
		{
			System.out.println("RequestBanPlayer: Problems ocurred during the banning process: ");
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return executed;
	}

	/**
	 * Returns a text to apply to the confirmation dialog according to the
	 * actionId and the player Name.
	 * @param player
	 * @return
	 */
	private String getConfirmationDialogText(L2PcInstance player)
	{
		switch (_banActionId)
		{
			case 1:
				return "Are you sure you want to ban the player " + player.getName() + "?";
			case 2:
				return "Are you sure you want to ban the account " + player.getAccount() + "?";
			case 3:
				return "Are you sure you want to ban all the accounts of the player " + player.getName() + "?";
			case 4:
				return "Are you sure you want to unban the player " + player.getName() + "?";
			case 5:
				return "Are you sure you want to unban the account " + player.getAccount() + "?";
			case 6:
				return "Are you sure you want to unban all the accounts of the player " + player.getName() + "?";
			default:
				return "";
		}
	}

	private ImageDescriptor getDescriptor()
	{
		switch (_banActionId)
		{
			case 1:
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.BAN_PLAYER);
			case 2:
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.BAN_ACCOUNT);
			case 3:
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.BAN_ALL_ACCOUNTS);
			case 4:
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.UN_BAN_PLAYER);
			case 5:
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.UN_BAN_ACCOUNT);
			case 6:
				return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.UN_BAN_ALL_ACCOUNTS);
			default:
				return null;
		}
	}

	@Override
	public String getToolTipText()
	{
		switch (_banActionId)
		{
			case 1:
				return "Ban a player from the server.";
			case 2:
				return "Ban selected player's account.";
			case 3:
				return "Ban all the selected player's accounts.";
			case 4:
				return "Unban a player from the server.";
			case 5:
				return "Unban selected player's account.";
			case 6:
				return "Unban all the selected player's accounts.";
			default:
				return super.getToolTipText();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see elayne.ations.RequestAction#run()
	 */
	@Override
	public void run()
	{
		Object obj = _selection.getFirstElement();
		if (obj instanceof L2PcInstance)
		{
			L2PcInstance player = ((L2PcInstance) obj);
			if (sendConfirmationMessage("Ban Player", getConfirmationDialogText(player)))
			{
				switch (_banActionId)
				{
					case 1:
						/*
						 * If player is online try to kick him from the game
						 * before proceeding. Else we would find out that the
						 * ban has not been set whenever the Player logs off the
						 * game. This is only required in the case we only want
						 * to ban the player and he/she is still online.
						 */
						if (player.isOnline() && isRMIConnected())
						{
							try
							{
								if (RemoteAdministrationClient.getInstance().kickPlayerFromServer(player.getName()) == 1)
									player.setOnline(false);
							}
							catch (RemoteException e)
							{
								System.out.println("RequestBanPlayer: An error ocurred while kicking player from the server" + "Error: " + e.getMessage());
								e.printStackTrace();
							}
						}
						if (banPlayer(player.getObjectId()))
						{
							player.setAccessLevel(-100);
							sendMessage("The Player " + player.getName() + " has been banned.");
						}
						break;
					case 2:
						if (banAccount(player.getAccount()))
						{
							sendMessage("The Account " + player.getAccount() + " has been banned.");
							break;
						}
					case 3:
						String ip = accountIP(player.getAccount());
						if (banAllAccounts(ip))
						{
							sendMessage("All the accounts of the ip " + ip + " have been banned.");
							break;
						}
					case 4:
						if (unBanPlayer(player.getObjectId()))
						{
							player.setAccessLevel(0);
							sendMessage("The Player " + player.getName() + " has been unbanned.");
							break;
						}
					case 5:
						if (unBanAccount(player.getAccount()))
						{
							sendMessage("The Account " + player.getAccount() + " has been unbanned.");
							break;
						}
					case 6:
						String ips = accountIP(player.getAccount());
						if (unBanAllAccounts(ips))
						{
							sendMessage("All the accounts of the ip " + ips + " have been unbanned.");
							break;
						}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see elayne.ations.RequestAction#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection incoming)
	{
		if (incoming instanceof IStructuredSelection)
		{
			_selection = (IStructuredSelection) incoming;
			setEnabled(_selection.size() == 1 && _selection.getFirstElement() instanceof L2PcInstance);
		}
		else
			setEnabled(false);
	}

	@Override
	public String toString()
	{
		switch (_banActionId)
		{
			case 1:
				return "Ban Player";
			case 2:
				return "Ban Account";
			case 3:
				return "Ban all Accounts";
			case 4:
				return "Unban Player";
			case 5:
				return "Unban Account";
			case 6:
				return "Unban all Accounts";
			default:
				return super.toString();
		}
	}

	private boolean unBanAccount(String account)
	{
		String sql = "UPDATE accounts SET `accessLevel`='0' WHERE (`login`=?)";
		Connection con = null;
		boolean executed = false;
		try
		{
			con = LoginDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, account);
			statement.executeUpdate();
			statement.close();
			executed = true;
		}
		catch (Exception e)
		{
			System.out.println("RequestBanPlayer: Problems ocurred during the unban process: ");
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return executed;
	}

	private boolean unBanAllAccounts(String lastIP)
	{
		String sql = "UPDATE accounts SET `accessLevel`='0' WHERE (`lastIP`=?)";
		Connection con = null;
		boolean executed = false;
		try
		{
			con = LoginDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(sql);
			statement.setString(1, lastIP);
			statement.executeUpdate();
			statement.close();
			executed = true;
		}
		catch (Exception e)
		{
			System.out.println("RequestBanPlayer: Problems ocurred during the banning process: ");
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return executed;
	}

	private boolean unBanPlayer(int objectId)
	{
		String sql = "UPDATE `characters` SET `accesslevel`='0' WHERE (`charId`=?)";
		Connection con = null;
		boolean executed = false;
		try
		{
			con = ServerDB.getInstance().getConnection();

			PreparedStatement statement = con.prepareStatement(sql);
			statement.setInt(1, objectId);
			statement.executeUpdate();
			statement.close();
			executed = true;
		}
		catch (Exception e)
		{
			System.out.println("RequestBanPlayer: Problems ocurred during the unban process: ");
			e.printStackTrace();

		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return executed;
	}
}
