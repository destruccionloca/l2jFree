/**
 * 
 */
package elayne.actions;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import elayne.IImageKeys;
import elayne.application.Activator;
import elayne.instancemanager.ClansManager;
import elayne.model.instance.L2CharacterBriefEntry;
import elayne.model.instance.L2Clan;
import elayne.util.connector.ServerDB;

/**
 * @author polbat02
 */
public class RequestClanPlayerKick extends ElayneAction
{

	private static final String attClanLeader = "Attention!! This player is the leader of this Clan. Kicking him from the clan will make the clan disappear! Are you sure you want to continue?";
	private static final String ID = "requestClanPalayerKick";
	private static final String removeAprentice = "UPDATE characters SET apprentice=0 WHERE apprentice=?";
	private static final String removePlayerFromClan = "UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE charId=?";
	private static final String removeSponsor = "UPDATE characters SET sponsor=0 WHERE sponsor=?";

	/**
	 * @param window
	 * @param treeViewer
	 */
	public RequestClanPlayerKick(IWorkbenchWindow window, TreeViewer treeViewer)
	{
		super(window, treeViewer);
		setNewId(ID);
		setText("Kick Clan Member");
		setToolTipText("Kick a Clan member from a Clan");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, IImageKeys.KICK_PLAYER_FROM_CLAN));
	}

	private boolean deletePlayerFromClan(int objectId)
	{
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(removePlayerFromClan);
			statement.setString(1, "");
			statement.setLong(2, 0);
			statement.setLong(3, 0);
			statement.setInt(4, objectId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement(removeAprentice);
			statement.setInt(1, objectId);
			statement.execute();
			statement.close();

			statement = con.prepareStatement(removeSponsor);
			statement.setInt(1, objectId);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("RequestKickPlayerFromClan: Error while removing clan member in db " + e.getMessage());
			return false;
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{}
		}
		return true;
	}

	private boolean removeClan(String clanName, String clanId)
	{
		java.sql.Connection con = null;
		try
		{
			con = ServerDB.getInstance().getConnection();

			PreparedStatement statement;
			System.out.println("RequestKickPlayerFromClan: Cleaning clan Wars from the clan: " + clanName);
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?;");
			statement.setEscapeProcessing(true);
			statement.setString(1, clanName);
			statement.setString(2, clanName);
			statement.executeUpdate();

			// Remove All From clan
			System.out.println("RequestKickPlayerFromClan: Cleaning members from the clan: " + clanName);
			statement.close();
			statement = con.prepareStatement("UPDATE characters SET clanid=0 WHERE clanid=?;");
			statement.setString(1, clanId);
			statement.executeUpdate();

			// Free Clan Halls
			System.out.println("RequestKickPlayerFromClan: Cleaning clan halls from the clan: " + clanName);
			statement.close();
			statement = con.prepareStatement("UPDATE clanhall SET ownerId=0, paidUntil=0, paid=0 WHERE ownerId=?;");
			statement.setString(1, clanId);
			statement.executeUpdate();

			// Delete Clan
			System.out.println("RequestKickPlayerFromClan: Deleting clan: " + clanName);
			statement.close();
			statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?;");
			statement.setString(1, clanId);
			statement.executeUpdate();

			// Clan privileges
			System.out.println("RequestKickPlayerFromClan: Cleaning clan privis of the clan: " + clanName);
			statement.close();
			statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?;");
			statement.setString(1, clanId);
			statement.executeUpdate();

			// Clan subpledges
			System.out.println("RequestKickPlayerFromClan: Cleaning clan subpledges from the clan: " + clanName);
			statement.close();
			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?;");
			statement.setString(1, clanId);
			statement.executeUpdate();

			// Clan skills
			System.out.println("RequestKickPlayerFromClan: Cleaning clan skills from the clan: " + clanName);
			statement.close();
			statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?;");
			statement.setString(1, clanId);
			statement.executeUpdate();
			statement.close();
		}
		catch (SQLException e)
		{
			System.out.println("RequestKickPlayerFromClan: Error while deleating clan: " + e.getMessage());
		}
		catch (Exception ex)
		{
			System.out.println("RequestKickPlayerFromClan: Error while deleating clan: " + ex.getMessage());
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (Exception e)
			{}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see elayne.ations.RequestAction#run()
	 */
	@Override
	public void run()
	{
		Object obj = selection.getFirstElement();
		L2CharacterBriefEntry player = ((L2CharacterBriefEntry) obj);
		if (player.getParent().getParent() instanceof L2Clan)
		{
			int clanId = player.getClanId();

			if (ClansManager.getInstance().isKnownClan(clanId))
			{
				L2Clan clan = ClansManager.getInstance().getClan(clanId);
				if (clan == null)
					return;
				if (sendConfirmationMessage("Kick Player From Clan", "Are you sure that you want to kick the player " + player.getName() + " from the Clan " + clan.getName() + "?"))
				{
					if (player.isOnline())
					{
						sendMessage("You can't kick a player that is online right now.");
						return;
					}
					// Course of action in case the player selected is the clan leader.
					if (clan.getLeaderId() == player.getObjectId())
					{
						if (sendConfirmationMessage("Remove Clan", attClanLeader))
						{
							// Remove this clan from the saved Clans.
							if (removeClan(clan.getName(), String.valueOf(clan.getId())))
								ClansManager.getInstance().removeClan(clan, true);
						}
					}
					else if (clan.getClanMembers().contains(player))
					{
						if (deletePlayerFromClan(player.getObjectId()))
						{
							ClansManager.getInstance().removeClanMemberFromKnownClan(clan, player);
							System.out.println("Removed player " + player.getName() + " from the clan " + clan.getName() + ".");
						}
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
			selection = (IStructuredSelection) incoming;

			setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof L2CharacterBriefEntry
									&& ((L2CharacterBriefEntry) selection.getFirstElement()).getParent().getName().equals("Clan Members"));
		}
		// Not enable the action.
		else
			setEnabled(false);
	}
}
