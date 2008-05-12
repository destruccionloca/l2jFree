package elayne.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastList;
import elayne.model.L2RootSession;
import elayne.model.instance.L2CharacterBriefEntry;
import elayne.model.instance.L2GroupEntry;
import elayne.model.instance.L2RegularGroup;
import elayne.util.connector.ServerDB;
import elayne.views.BannedPlayersView;

public class GetBannedPlayers
{
	private static GetBannedPlayers _instance = null;

	public static GetBannedPlayers getInstance()
	{
		if (_instance == null)
		{
			_instance = new GetBannedPlayers();
		}
		return _instance;
	}

	private boolean connectionDone = false;

	private FastList<L2CharacterBriefEntry> players = new FastList<L2CharacterBriefEntry>();

	public void getBannedPlayers(BannedPlayersView view, boolean isUpdate)
	{
		if (!connectionDone || isUpdate)
		{
			java.sql.Connection con = null;
			try
			{
				players.clear();
				con = ServerDB.getInstance().getConnection();
				String previous = "SELECT charId, account_name, char_name, level, accesslevel, online, sex, clanid FROM `characters` WHERE `accesslevel` < '0' ORDER BY `accesslevel` DESC";
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
		{
			view.session = new L2RootSession();
			// Empty Group that acts as the Father of all
			// other groups.
			L2GroupEntry root = view.session.getRoot();

			// Add dummy group
			L2GroupEntry bannedGroup = new L2RegularGroup(root, "Banned Players");
			root.addEntry(bannedGroup);
			// Collections.sort(name);
			for (L2CharacterBriefEntry player : players)
			{
				player.setParent(bannedGroup);
				bannedGroup.addEntry(player);
			}
		}
	}
}
