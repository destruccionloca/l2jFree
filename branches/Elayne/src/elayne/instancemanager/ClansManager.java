package elayne.instancemanager;

import java.util.Collection;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import elayne.model.L2Character;
import elayne.model.L2RootSession;
import elayne.model.instance.L2CharacterBriefEntry;
import elayne.model.instance.L2Clan;
import elayne.model.instance.L2GroupEntry;
import elayne.model.instance.L2PcInstance;
import elayne.views.ClanInfoView;

/**
 * This class is responsible for managing all known clans and providing
 * information for the Clan Information View ({@link ClanInfoView}). It's
 * access should always be static through the {@link ClansManager#getInstance()}
 * method to ensure that all clans are stored in one same place.
 * @author polbat02
 */
public class ClansManager implements IStructuredContentProvider, ITreeContentProvider
{
	// =======================================
	// INSTANCE
	/** The static instance of this Manager */
	private static ClansManager 		_instance;
	/** Map of all the clans represented in this instance */
	private FastMap<Integer, L2Clan> 	_clanMap 	= new FastMap<Integer, L2Clan>();
	/** List in which the root groups are stored */
	private FastList<L2GroupEntry> 		_rootGroup 	= new FastList<L2GroupEntry>();
	/** The root Group of this Manager */
	L2RootSession 						_session 	= new L2RootSession();
	/**
	 * The Viewer in which this class will be visually represented
	 */
	private TreeViewer 					_viewer;

	/**
	 * @return the only instance of this Class.
	 */
	public static ClansManager getInstance()
	{
		if (_instance == null)
			_instance = new ClansManager();
		return _instance;
	}

	/**
	 * Private Constructor that defines this Content provider.
	 */
	private ClansManager()
	{
		_rootGroup.add(_session.getRoot());
	}

	/**
	 * Adds a clan to this manager:<br>
	 * If this ain't a known clan we'll add it into the clan map. If the clan
	 * isn't in in the root group already, we'll add it into the root group and
	 * we will proceed to refresh the view.
	 * @param clan
	 */
	public void addClan(L2Clan clan)
	{
		if (!isKnownClan(clan.getId()))
		{
			_clanMap.put(clan.getId(), clan);
			clan.setParent(_session.getRoot());
		}
		if (!containsGroup(clan.getName()))
		{
			_session.getRoot().addEntry(clan);
			if (_viewer != null)
			{
				_viewer.refresh();
				_viewer.collapseToLevel(_session.getRoot(), 2);
				_viewer.expandToLevel(2);
			}
		}
	}

	/**
	 * This method checks if the root group contains a certain entry, in this
	 * case, a clan.
	 * @param name -> The name of the entry to look for.
	 * @return true if the root contains the given name.
	 */
	public boolean containsGroup(String name)
	{
		for (L2Character cha : _session.getRoot().getEntries())
		{
			if (cha.getName().toLowerCase().equals(name.toLowerCase()))
				return true;
		}
		return false;
	}

	public void dispose()
	{}

	/** Returns all known clans */
	public Collection<L2Clan> getAllClans()
	{
		return _clanMap.values();
	}

	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof L2GroupEntry)
			return ((L2GroupEntry) parentElement).getEntries();
		return null;
	}

	/**
	 * Returns a particular clan for a given clan Id if known.
	 * @param id -> The clan id to get.
	 * @return The found clan. May be null.
	 */
	public L2Clan getClan(int id)
	{
		return _clanMap.get(id);
	}

	public Object[] getElements(Object inputElement)
	{
		return _rootGroup.toArray();
	}

	public Object getParent(Object element)
	{
		if (element instanceof L2GroupEntry)
			return ((L2GroupEntry) element).getParent();
		return null;
	}

	/** @return -> the root group of this instance. */
	public L2GroupEntry getRoot()
	{
		return _session.getRoot();
	}

	public boolean hasChildren(Object element)
	{
		if (element instanceof L2GroupEntry)
			return ((L2GroupEntry) element).getEntries().length > 0;
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		viewer.refresh();
	}

	/** Returns true if the clan map contains the given Id */
	public boolean isKnownClan(int id)
	{
		return _clanMap.containsKey(id);
	}

	/** Refreshes the Tree Viewer */
	public void refreshViewer()
	{
		if (_viewer != null)
			_viewer.refresh();
	}

	/**
	 * Removes a clan from this instance:<br>
	 * This method attempts to remove a clan from the viewer while notifying any
	 * other views/players that may contain information about this clan. <br>
	 * If the clan is a known clan, it will remove the clan from the root of
	 * this instance while setting the parent of the <code>clan = null</code>.
	 * <br>
	 * If the parameter isRemovedFromServer is true, the method will look for
	 * any player that may have this clan as his clan and remove it from that
	 * particular player. The player clan will be set to null and the clan id of
	 * the player will also be set to 0. The players view in that case will be
	 * refreshed according to the changes made.
	 * @param clan
	 * @param isRemovedFromServer
	 */
	public void removeClan(L2Clan clan, boolean isRemovedFromServer)
	{
		// Check first of all if this clan is known
		if (isKnownClan(clan.getId()))
		{
			clan.setParent(null);
			_session.getRoot().removeEntry(clan);
			if (_viewer != null)
				_viewer.refresh();
			if (isRemovedFromServer)
				_clanMap.remove(clan);
		}
		if (isRemovedFromServer)
		{
			/*
			 * Get from all known players all the players that have this clan as
			 * their Clan and remove it. Set the clan as null and set the
			 * player's clan id equal to 0.
			 */
			for (L2PcInstance player : PlayersManager.getInstance().getAllPlayers())
			{
				L2PcInstance pcPlayer = PlayersManager.getInstance().getPlayer(player.getName());
				if (pcPlayer == null)
					continue;
				if (pcPlayer.getClanId() == clan.getId())
				{
					pcPlayer.removeEntry(pcPlayer.getClan());
					pcPlayer.setClan(null);
					pcPlayer.setClanId(0);
				}
			}
			// Refresh the player's view.
			PlayersManager.getInstance().refreshViewer();
		}
		// Refresh this input manager.
		refreshViewer();
	}

	/**
	 * Attempts to remove a clan member from a known clan. This method will
	 * attempt to remove the clan member from a known clan and will also attempt
	 * to clean the removed clan member from all known players. In the case of
	 * the removed player, we will remove the clan entry from that player, set
	 * the clan to null and set the clan id equal to 0.
	 * @param clan
	 * @param player
	 * @return
	 */
	public boolean removeClanMemberFromKnownClan(L2Clan clan, L2CharacterBriefEntry player)
	{
		if (isKnownClan(clan.getId()))
		{
			boolean removed = false;
			if (clan.getClanMembers().contains(player))
			{
				clan.getClanMembers().remove(player);
				clan.getClanMembersGroup().removeEntry(player);
				removed = true;
			}
			if (removed)
			{
				for (L2PcInstance pcChar : PlayersManager.getInstance().getAllPlayers())
				{
					L2PcInstance pcPlayer = PlayersManager.getInstance().getPlayer(pcChar.getName());
					if (player == null)
						continue;

					/*
					 * If this player is the player that's getting kicked from
					 * the clan, remove the clan from that player and all
					 * related information.
					 */
					if (pcPlayer.getName().equals(player.getName()))
					{
						pcPlayer.removeEntry(pcPlayer.getClan());
						pcPlayer.setClan(null);
						pcPlayer.setClanId(0);
					}
					// Else remove the clan member.
					else if (pcPlayer.getClanId() == clan.getId())
					{
						pcPlayer.getClan().getClanMembersGroup().removeEntry(player);
					}
				}
				// Refresh viewers.
				refreshViewer();
				PlayersManager.getInstance().refreshViewer();
			}
			return removed;
		}
		return false;
	}

	/** Sets a viewer to this input provider. */
	public void setViewer(TreeViewer viewer)
	{
		_viewer = viewer;
	}
}
