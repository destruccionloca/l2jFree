/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.model.L2NpcWalkerNode;

/**
 * Main Table to Load Npc Walkers Routes and Chat SQL Table.<br>
 * 
 * @author Rayan RPG for L2Emu Project
 *
 */
public class NpcWalkerRoutesTable
{
	private final static Log			_log	= LogFactory.getLog(SpawnTable.class);

	private final FastList<L2NpcWalkerNode>	_routes = new FastList<L2NpcWalkerNode>();

	public static NpcWalkerRoutesTable getInstance()
	{
		return SingletonHolder._instance;
	}

	private NpcWalkerRoutesTable()
	{
		_log.info("Initializing Walker Routes Table.");
	}

	public void load()
	{
		_routes.clear();
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con
					.prepareStatement("SELECT route_id, npc_id, move_point, chatText, move_x, move_y, move_z, delay, running FROM walker_routes ORDER By move_point ASC");
			ResultSet rset = statement.executeQuery();
			L2NpcWalkerNode route;
			while (rset.next())
			{
				route = new L2NpcWalkerNode();
				route.setRouteId(rset.getInt("route_id"));
				route.setNpcId(rset.getInt("npc_id"));
				route.setMovePoint(rset.getString("move_point"));
				route.setChatText(rset.getString("chatText"));

				route.setMoveX(rset.getInt("move_x"));
				route.setMoveY(rset.getInt("move_y"));
				route.setMoveZ(rset.getInt("move_z"));
				route.setDelay(rset.getInt("delay"));
				route.setRunning(rset.getBoolean("running"));

				_routes.add(route);
			}

			rset.close();
			statement.close();

			_log.info("WalkerRoutesTable: Loaded " + _routes.size() + " Npc Walker Routes.");
		}
		catch (Exception e)
		{
			_log.fatal("WalkerRoutesTable: Error while loading Npc Walker Routes: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	public ArrayList<L2NpcWalkerNode> getRouteForNpc(int id)
	{
		ArrayList<L2NpcWalkerNode> _return = new ArrayList<L2NpcWalkerNode>();

		for (FastList.Node<L2NpcWalkerNode> n = _routes.head(), end = _routes.tail(); (n = n.getNext()) != end;)
		{
			if (n.getValue().getNpcId() == id)
			{
				_return.add(n.getValue());
			}
		}
		return _return;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final NpcWalkerRoutesTable _instance = new NpcWalkerRoutesTable();
	}
}
