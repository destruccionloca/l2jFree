package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Hero;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles following admin commands:
 * - donatorhero = turns a character's permanent hero-state on/off
 */
public class AdminDonator implements IAdminCommandHandler {
	private final static Log _log = LogFactory.getLog(AdminInvul.class.getName());
	private static final String[] ADMIN_COMMANDS = {"admin_donatorhero"};
	private static final int REQUIRED_LEVEL1 = Config.GM_CHAR_EDIT;
	private static final int REQUIRED_LEVEL2 = Config.GM_CHAR_EDIT_OTHER;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if (command.startsWith("admin_donatorhero"))
		{
			try {
				String playername = st.nextToken();

				L2PcInstance player = L2World.getInstance().getPlayer(playername);

				if (player != null)
					handleDonatorHero(activeChar, player);
				else
				{
					Connection con = null;
					PreparedStatement statement;
					ResultSet rset;
					int objectId;

					try
					{
						con = L2DatabaseFactory.getInstance().getConnection(con);

						statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=? LIMIT 1");
						statement.setString(1, playername);
						rset = statement.executeQuery();

						objectId = rset.getInt("obj_Id");

						rset.close();
						statement.close();

						player = L2PcInstance.load(objectId);
						handleDonatorHero(activeChar, player);
						player.deleteMe();

					} catch(SQLException e)
					{
						activeChar.sendMessage("Set donator hero failed!");
						if (_log.isDebugEnabled())  _log.debug("",e);
					} finally
					{
						try{con.close();}catch(Exception e){ _log.error("",e);}
					}
				}
			} catch (Exception e) {}
		}

		return true;
	}

	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleDonatorHero(L2PcInstance activeChar, L2PcInstance player)
	{
		if (Hero.getInstance().isDonatorHero(player))
		{
			Hero.getInstance().setDonatorHero(player, false);
			activeChar.sendMessage("Added hero state to " + player.getName() + "!");
		}
		else
		{
			Hero.getInstance().setDonatorHero(player, true);
			activeChar.sendMessage("Removed hero state from " + player.getName() + "!");
		}
	}
	
	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL1 && level >= REQUIRED_LEVEL2);
	}
}
