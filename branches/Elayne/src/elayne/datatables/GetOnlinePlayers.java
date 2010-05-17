package elayne.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastList;

import org.eclipse.jface.dialogs.MessageDialog;

import elayne.instancemanager.PlayersManager;
import elayne.model.L2Character;
import elayne.model.L2RootSession;
import elayne.model.instance.L2CharacterBriefEntry;
import elayne.model.instance.L2GroupEntry;
import elayne.model.instance.L2RegularGroup;
import elayne.util.connector.ServerDB;
import elayne.views.OnlinePlayersView;

public class GetOnlinePlayers
{
	private static GetOnlinePlayers _instance = null;

	public static GetOnlinePlayers getInstance()
	{
		if (_instance == null)
		{
			_instance = new GetOnlinePlayers();
		}
		return _instance;
	}

	private boolean connectionDone = false;

	private FastList<L2CharacterBriefEntry> players = new FastList<L2CharacterBriefEntry>();

	/**
	 * Gets the list of online Players in the Server. TODO: Add more extensive
	 * comment here.
	 * @param view
	 * @param isUpdate
	 */
	public void getOnlinePlayers(OnlinePlayersView view, boolean isUpdate)
	{
		if (!connectionDone || isUpdate)
		{
			players.clear();
			java.sql.Connection con = null;
			try
			{
				con = ServerDB.getInstance().getConnection();
				String previous = "SELECT charId, account_name, char_name, level, accesslevel, sex, clanid FROM `characters` WHERE `online` = '1' ORDER BY char_name";
				PreparedStatement statement = con.prepareStatement(previous);
				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					int objId = rset.getInt("charId");
					String account = rset.getString("account_name");
					String name = rset.getString("char_name");
					int level = rset.getInt("level");
					int accesslevel = rset.getInt("accesslevel");
					int sex = rset.getInt("sex");
					int clanId = rset.getInt("clanid");
					players.add(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
				}
				rset.close();
				statement.close();
				con.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			connectionDone = true;
		}
		if (view != null)
			showPlayers(view);
	}

	private boolean gmsOnline()
	{
		boolean online = false;
		for (L2CharacterBriefEntry player : players)
		{
			int gmOnline = player.getAccessLevel();
			if (gmOnline >= 75)
			{
				online = true;
				break;
			}
		}
		return online;
	}

	/**
	 * Displays a message to the user.
	 * @param message
	 * @param view
	 */
	private void showMessage(String message, OnlinePlayersView view)
	{
		MessageDialog.openInformation(view.getViewer().getControl().getShell(), "Online Players", message);
	}

	/**
	 * Main void that sorts players by name without extra groups (A,B,C...)
	 * @param view
	 */
	public void showPlayers(OnlinePlayersView view)
	{
		view._session = new L2RootSession();
		// Empty Group that acts as the Father of all other
		// groups.
		L2GroupEntry root = view._session.getRoot();

		// Add dummy group
		L2RegularGroup onlinePlayers = new L2RegularGroup(root, "Online Players (" + players.size() + ")");
		root.addEntry(onlinePlayers);

		// Collections.sort(name);
		for (L2CharacterBriefEntry player : players)
		{
			player.setParent(onlinePlayers);
			onlinePlayers.addEntry(player);
		}
		// That's a good chance to update the players online
		// in our Players View
		PlayersManager.getInstance().updatePlayersOnline(players);
	}

	/**
	 * Sorts the online Players By Access Level.
	 * @param view
	 */
	public void sortOnlinePlayersByAccesslevel(OnlinePlayersView view)
	{
		if (!gmsOnline())
		{
			showMessage("No Gms online at this moment.", view);
			showPlayers(view);
		}
		else
		{
			view._session = new L2RootSession();
			// Empty Group that acts as the Father of all
			// other groups.
			L2GroupEntry root = view._session.getRoot();

			// GMS Group
			L2RegularGroup gms = new L2RegularGroup(root, "GMs");
			root.addEntry(gms);
			// Players Group
			L2RegularGroup playergroup = new L2RegularGroup(root, "Other Players");
			root.addEntry(playergroup);

			int i = 0;
			for (L2CharacterBriefEntry player : players)
			{
				int objId = player.getObjectId();
				int level = player.getLevel();
				String name = player.getName();
				String account = player.getAccount();
				int accesslevel = player.getAccessLevel();
				int sex = player.getSex();
				int clanId = player.getClanId();
				if (accesslevel >= 75)
					gms.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
				else
					playergroup.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
				i++;
			}
			for (L2Character entry : root.getEntries())
			{
				if (entry instanceof L2RegularGroup)
				{
					L2RegularGroup group = ((L2RegularGroup) entry);
					group.setName(group.getName() + ": (" + group.getEntries().length + " players)");
				}
			}
		}
	}

	/**
	 * Sorts the online Players By Sex.
	 * @param view
	 */
	public void sortOnlinePlayersByGender(OnlinePlayersView view)
	{
		view._session = new L2RootSession();
		// Empty Group that acts as the Father of all other
		// groups.
		// This Group is Invisible to users.
		L2GroupEntry root = view._session.getRoot();

		// Males Group
		L2RegularGroup males = new L2RegularGroup(root, "Males");
		root.addEntry(males);

		// Female Group
		L2RegularGroup female = new L2RegularGroup(root, "Females");
		root.addEntry(female);

		for (L2CharacterBriefEntry player : players)
		{
			int objId = player.getObjectId();
			int level = player.getLevel();
			String name = player.getName();
			String account = player.getAccount();
			int accesslevel = player.getAccessLevel();
			int sex = player.getSex();
			int clanId = player.getClanId();
			if (sex == 0)
				males.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else
				female.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
		}
		for (L2Character entry : root.getEntries())
		{
			if (entry instanceof L2RegularGroup)
			{
				L2RegularGroup group = ((L2RegularGroup) entry);
				group.setName(group.getName() + ": (" + group.getEntries().length + " players)");
			}
		}
	}

	/**
	 * Sorts the Online Players By Level.
	 * @param view
	 */
	public void sortOnlinePlayersByLevel(OnlinePlayersView view)
	{
		view._session = new L2RootSession();
		// Empty Group that acts as the Father of all other
		// groups.
		L2GroupEntry root = view._session.getRoot();

		// Add dummy group
		L2RegularGroup onlinePlayers = new L2RegularGroup(root, "Groups of Players");
		root.addEntry(onlinePlayers);

		// LEVEL GROUPS
		L2RegularGroup l10 = new L2RegularGroup(root, "Under level 10");
		onlinePlayers.addEntry(l10);

		L2RegularGroup l40 = new L2RegularGroup(root, "Under level 40");
		onlinePlayers.addEntry(l40);

		L2RegularGroup l60 = new L2RegularGroup(root, "Under level 60");
		onlinePlayers.addEntry(l60);

		L2RegularGroup l75 = new L2RegularGroup(root, "Under level 75");
		onlinePlayers.addEntry(l75);

		L2RegularGroup l80 = new L2RegularGroup(root, "Level equals or greater than 75");
		onlinePlayers.addEntry(l80);

		for (L2CharacterBriefEntry player : players)
		{
			int objId = player.getObjectId();
			int level = player.getLevel();
			String name = player.getName();
			String account = player.getAccount();
			int accesslevel = player.getAccessLevel();
			int sex = player.getSex();
			int clanId = player.getClanId();
			if (level >= 0 && level < 10)
				l10.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (level >= 10 && level < 40)
				l40.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (level >= 40 && level < 60)
				l60.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (level >= 60 && level < 75)
				l75.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (level >= 75)
				l80.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else
				onlinePlayers.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
		}
		l10.setName(l10.getName() + ": (" + l10.getEntries().length + " players)");
		l40.setName(l40.getName() + ": (" + l40.getEntries().length + " players)");
		l60.setName(l60.getName() + ": (" + l60.getEntries().length + " players)");
		l75.setName(l75.getName() + ": (" + l75.getEntries().length + " players)");
		l80.setName(l80.getName() + ": (" + l80.getEntries().length + " players)");
	}

	/**
	 * Sorts the online Players By Name.
	 * @param view
	 */
	public void sortOnlinePlayersByName(OnlinePlayersView view)
	{
		view._session = new L2RootSession();
		// Empty Group that acts as the Father of all other
		// groups.
		L2GroupEntry root = view._session.getRoot();

		// Add dummy group
		L2RegularGroup onlinePlayers = new L2RegularGroup(root, "Groups of Players");
		root.addEntry(onlinePlayers);
		// Online Groups by Letter
		L2RegularGroup a = new L2RegularGroup(onlinePlayers, "A");
		onlinePlayers.addEntry(a);
		L2RegularGroup b = new L2RegularGroup(onlinePlayers, "B");
		onlinePlayers.addEntry(b);
		L2RegularGroup c = new L2RegularGroup(onlinePlayers, "C");
		onlinePlayers.addEntry(c);
		L2RegularGroup d = new L2RegularGroup(onlinePlayers, "D");
		onlinePlayers.addEntry(d);
		L2RegularGroup e = new L2RegularGroup(onlinePlayers, "E");
		onlinePlayers.addEntry(e);
		L2RegularGroup f = new L2RegularGroup(onlinePlayers, "F");
		onlinePlayers.addEntry(f);
		L2RegularGroup g = new L2RegularGroup(onlinePlayers, "G");
		onlinePlayers.addEntry(g);
		L2RegularGroup h = new L2RegularGroup(onlinePlayers, "H");
		onlinePlayers.addEntry(h);
		L2RegularGroup i = new L2RegularGroup(onlinePlayers, "I");
		onlinePlayers.addEntry(i);
		L2RegularGroup j = new L2RegularGroup(onlinePlayers, "J");
		onlinePlayers.addEntry(j);
		L2RegularGroup k = new L2RegularGroup(onlinePlayers, "K");
		onlinePlayers.addEntry(k);
		L2RegularGroup l = new L2RegularGroup(onlinePlayers, "L");
		onlinePlayers.addEntry(l);
		L2RegularGroup m = new L2RegularGroup(onlinePlayers, "M");
		onlinePlayers.addEntry(m);
		L2RegularGroup n = new L2RegularGroup(onlinePlayers, "N");
		onlinePlayers.addEntry(n);
		L2RegularGroup o = new L2RegularGroup(onlinePlayers, "O");
		onlinePlayers.addEntry(o);
		L2RegularGroup p = new L2RegularGroup(onlinePlayers, "P");
		onlinePlayers.addEntry(p);
		L2RegularGroup q = new L2RegularGroup(onlinePlayers, "Q");
		onlinePlayers.addEntry(q);
		L2RegularGroup r = new L2RegularGroup(onlinePlayers, "R");
		onlinePlayers.addEntry(r);
		L2RegularGroup s = new L2RegularGroup(onlinePlayers, "S");
		onlinePlayers.addEntry(s);
		L2RegularGroup t = new L2RegularGroup(onlinePlayers, "T");
		onlinePlayers.addEntry(t);
		L2RegularGroup u = new L2RegularGroup(onlinePlayers, "U");
		onlinePlayers.addEntry(u);
		L2RegularGroup v = new L2RegularGroup(onlinePlayers, "V");
		onlinePlayers.addEntry(v);
		L2RegularGroup w = new L2RegularGroup(onlinePlayers, "W");
		onlinePlayers.addEntry(w);
		L2RegularGroup x = new L2RegularGroup(onlinePlayers, "X");
		onlinePlayers.addEntry(x);
		L2RegularGroup y = new L2RegularGroup(onlinePlayers, "Y");
		onlinePlayers.addEntry(y);
		L2RegularGroup z = new L2RegularGroup(onlinePlayers, "Z");
		onlinePlayers.addEntry(z);
		for (L2CharacterBriefEntry player : players)
		{
			int objId = player.getObjectId();
			int level = player.getLevel();
			String name = player.getName();
			String account = player.getAccount();
			int accesslevel = player.getAccessLevel();
			int sex = player.getSex();
			int clanId = player.getClanId();
			if (name.toLowerCase().startsWith("a"))
				a.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("b"))
				b.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("c"))
				c.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("d"))
				d.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("e"))
				e.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("f"))
				f.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("g"))
				g.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("h"))
				h.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("i"))
				i.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("j"))
				j.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("k"))
				k.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("l"))
				l.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("n"))
				n.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("m"))
				m.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("o"))
				o.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("p"))
				p.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("q"))
				q.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("r"))
				r.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("s"))
				s.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("t"))
				t.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("u"))
				u.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("v"))
				v.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("w"))
				w.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("x"))
				x.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("y"))
				y.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else if (name.toLowerCase().startsWith("z"))
				z.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
			else
				onlinePlayers.addEntry(new L2CharacterBriefEntry(objId, level, name, account, 1, accesslevel, sex, clanId));
		}

		for (L2Character entry : onlinePlayers.getEntries())
		{
			if (entry instanceof L2RegularGroup)
			{
				L2RegularGroup group = ((L2RegularGroup) entry);
				group.setName(group.getName() + ": (" + group.getEntries().length + " players)");
			}
		}
	}
}
