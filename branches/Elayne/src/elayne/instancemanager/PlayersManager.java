package elayne.instancemanager;

import java.util.Collection;
import java.util.List;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import elayne.model.L2Character;
import elayne.model.L2RootSession;
import elayne.model.instance.L2CharacterBriefEntry;
import elayne.model.instance.L2GroupEntry;
import elayne.model.instance.L2PcInstance;
import elayne.views.PlayerInfoView;

/**
 * This class manages the input shown in the {@link PlayerInfoView}. Any player
 * shown in the {@link PlayerInfoView} is represented and stored here. This
 * class contains methods that manage all the player related information.
 * @author polbat02
 */
public class PlayersManager implements IStructuredContentProvider, ITreeContentProvider
{
	// =======================================
	// INSTANCE
	private static PlayersManager 				_instance;
	/** Map containing all the players known in the server */
	private FastMap<String, L2PcInstance> 		_playerMap 	= new FastMap<String, L2PcInstance>();
	/** List of root groups */
	private FastList<L2GroupEntry> 				_rootGroup 	= new FastList<L2GroupEntry>();
	/** The root Group of this Manager */
	L2RootSession 								_session 	= new L2RootSession();
	/**
	 * The Viewer in which this class will be visually represented
	 */
	private TreeViewer 							_viewer;

	/**
	 * @return the only instance of this class.
	 */
	public static PlayersManager getInstance()
	{
		if (_instance == null)
			_instance = new PlayersManager();
		return _instance;
	}

	/**
	 * Private constructor that defines a this content provider.
	 */
	private PlayersManager()
	{
		_rootGroup.add(_session.getRoot());
	}

	/**
	 * Adds a player to this content provider.
	 * @param player
	 */
	public void addPlayer(L2PcInstance player)
	{
		if (!isKnownPlayer(player.getName()))
		{
			_playerMap.put(player.getName().toLowerCase(), player);
			player.setParent(_session.getRoot());
		}
		if (!containsGroup(player.getName()))
		{
			_session.getRoot().addEntry(player);
			if (_viewer != null)
			{
				_viewer.refresh();
				_viewer.collapseToLevel(_session.getRoot(), 2);
				_viewer.expandToLevel(2);
			}
		}
	}

	/**
	 * Check if the given name is the name of a containing entry of the root
	 * group which would mean the root group contains the group who's name is
	 * the parameter name.
	 * @param name
	 * @return true if the root group contains the given entry name.
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
	{

	}

	public Collection<L2PcInstance> getAllPlayers()
	{
		return _playerMap.values();
	}

	public Object[] getChildren(Object parentElement)
	{
		if (parentElement instanceof L2GroupEntry)
			return ((L2GroupEntry) parentElement).getEntries();
		return null;
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

	public L2PcInstance getPlayer(String name)
	{
		return _playerMap.get(name.toLowerCase());
	}

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

	/**
	 * Returns true if the given name is the one of one of the known players.
	 */
	public boolean isKnownPlayer(String name)
	{
		return _playerMap.containsKey(name.toLowerCase());
	}

	/**
	 * Attempts to refresh the defined viewer.
	 */
	public void refreshViewer()
	{
		if (_viewer != null)
			_viewer.refresh();
	}

	/**
	 * Removes a player from the viewer, and if specified, it also removes it
	 * from the known players map.
	 * @param player
	 * @param removeFromKnownPlayers
	 */
	public void removePlayer(L2PcInstance player, boolean removeFromKnownPlayers)
	{
		if (isKnownPlayer(player.getName()))
		{
			player.setParent(null);
			_session.getRoot().removeEntry(player);
			if (_viewer != null)
				_viewer.refresh();
			if (removeFromKnownPlayers)
				_playerMap.remove(player);
		}
	}

	/** Setter for the viewer. */
	public void setViewer(TreeViewer viewer)
	{
		_viewer = viewer;
	}

	/**
	 * Attempts to update all known players online state while refreshing the
	 * players online view.
	 */
	public void updatePlayersOnline(FastList<L2CharacterBriefEntry> players)
	{
		List<String> updatedPlayers = new FastList<String>();
		int updatedPlayersCount = 0;
		// Set online all of those players that are in the
		// given list
		for (L2CharacterBriefEntry player : players)
		{
			if (isKnownPlayer(player.getName()))
			{
				getPlayer(player.getName()).setOnline(true);
				updatedPlayers.add(player.getName());
				updatedPlayersCount++;
			}
		}
		for (L2PcInstance player : getAllPlayers())
		{
			if (updatedPlayers.contains(player.getName()))
				continue;
			player.setOnline(false);
			updatedPlayersCount++;
		}
	}
}
