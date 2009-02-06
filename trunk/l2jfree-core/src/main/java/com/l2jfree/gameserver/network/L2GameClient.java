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
package com.l2jfree.gameserver.network;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.MMOClient;
import org.mmocore.network.MMOConnection;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.LoginServerThread;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.LoginServerThread.SessionKey;
import com.l2jfree.gameserver.model.CharSelectInfoPackage;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfree.tools.security.BlowFishKeygen;
import com.l2jfree.tools.security.GameCrypt;

/**
 * Represents a client connected on Game Server
 * 
 * @author KenM
 */
public final class L2GameClient extends MMOClient<MMOConnection<L2GameClient>>
{
	protected static final Log	_log	= LogFactory.getLog(L2GameClient.class.getName());

	/**
	 * CONNECTED - client has just connected AUTHED - client has authed but
	 * doesnt has character attached to it yet IN_GAME - client has selected a
	 * char and is in game
	 * 
	 * @author KenM
	 */
	public static enum GameClientState
	{
		CONNECTED, AUTHED, IN_GAME
	}

	public GameClientState				state;

	// Info
	private String						_accountName;
	private SessionKey					_sessionId;
	private L2PcInstance				_activeChar;
	private ReentrantLock				_activeCharLock			= new ReentrantLock();

	private boolean						_isAuthedGG;
	private long						_connectionStartTime;
	private List<Integer>				_charSlotMapping		= new FastList<Integer>();

	// Crypt
	private GameCrypt					_crypt;

	// Flood protection
	public byte							packetsSentInSec		= 0;
	public int							packetsSentStartTick	= 0;

	private boolean						_disconnected;

	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();
		_crypt = new GameCrypt();
	}

	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		_crypt.setKey(key);
		return key;
	}

	public GameClientState getState()
	{
		return state;
	}

	public void setState(GameClientState pState)
	{
		state = pState;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		_crypt.decrypt(buf.array(), buf.position(), size);
		return true;
	}

	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		_crypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}

	public void setActiveChar(L2PcInstance pActiveChar)
	{
		_activeChar = pActiveChar;
		if (_activeChar != null)
		{
			L2World.getInstance().storeObject(getActiveChar());
		}
	}

	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}

	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}

	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}

	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;
	}

	public String getAccountName()
	{
		return _accountName;
	}

	public void setSessionId(SessionKey sk)
	{
		_sessionId = sk;
	}

	public SessionKey getSessionId()
	{
		return _sessionId;
	}

	public void sendPacket(L2GameServerPacket gsp)
	{
		getConnection().sendPacket(gsp);
		gsp.runImpl();
	}

	public void sendPacket(SystemMessageId sm)
	{
		sendPacket(sm.getSystemMessage());
	}

	public L2PcInstance markToDeleteChar(int charslot) throws Exception
	{
		// have to make sure active character must be nulled
		/*
		 * if (getActiveChar() != null) { saveCharToDisk (getActiveChar()); if
		 * (_log.isDebugEnabled()) _log.debug("active Char saved"); _activeChar =
		 * null; }
		 */

		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return null;

		L2PcInstance character = L2PcInstance.load(objid);
		if (character.getClanId() != 0)
			return character;

		character.deleteMe();

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE charId=?");
			statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L); // 24*60*60*1000
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error updating delete time of character.", e);
		}
		finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }

		return null;
	}

	public L2PcInstance deleteChar(int charslot) throws Exception
	{
		// have to make sure active character must be nulled
		/*
		 * if (getActiveChar() != null) { saveCharToDisk (getActiveChar()); if
		 * (_log.isDebugEnabled()) _log.debug("active Char saved"); _activeChar =
		 * null; }
		 */

		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return null;

		L2PcInstance character = L2PcInstance.load(objid);
		if (character.getClanId() != 0)
			return character;
		character.deleteMe();

		deleteCharByObjId(objid);
		return null;
	}

	public static void saveCharToDisk(L2PcInstance cha)
	{
		saveCharToDisk(cha, false);
	}

	/**
	 * Save the L2PcInstance to the database.
	 */
	public static void saveCharToDisk(L2PcInstance cha, boolean storeItems)
	{
		try
		{
			cha.store();
			if (Config.UPDATE_ITEMS_ON_CHAR_STORE || storeItems)
			{
				cha.getInventory().updateDatabase();
			}
		}
		catch (Exception e)
		{
			_log.error("Error saving character.", e);
		}
	}

	public void markRestoredChar(int charslot) throws Exception
	{
		// have to make sure active character must be nulled
		/*
		 * if (getActiveChar() != null) { saveCharToDisk (getActiveChar()); if
		 * (_log.isDebugEnabled()) _log.debug("active Char saved"); _activeChar =
		 * null; }
		 */

		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
			return;

		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error restoring character.", e);
		}
		finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }
	}

	public static void deleteCharByObjId(int objid)
	{
		if (objid < 0)
			return;

		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			PreparedStatement statement;

			statement = con.prepareStatement("DELETE FROM character_friends WHERE charId=? OR friendId=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_macroses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_raid_points WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_recommends WHERE charId=? OR target_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM seven_signs WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM characters WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Error deleting character.", e);
		}
		finally { try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); } }
	}

	public L2PcInstance loadCharFromDisk(int charslot)
	{
		L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));

		if (character != null)
		{
			// preinit some values for each login
			character.setRunning(); // running is default
			character.standUp(); // standing is default

			character.refreshOverloaded();
			character.refreshExpertisePenalty();
			character.setOnlineStatus(true);
		}
		else
		{
			_log.fatal("could not restore in slot: " + charslot);
		}

		// setCharacter(character);
		return character;
	}

	/**
	 * @param chars
	 */
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping.clear();

		for (CharSelectInfoPackage element : chars)
		{
			int objectId = element.getObjectId();
			_charSlotMapping.add(objectId);
		}
	}

	public void close(L2GameServerPacket gsp)
	{
		getConnection().close(gsp);
	}

	/**
	 * @param charslot
	 * @return
	 */
	private int getObjectIdForSlot(int charslot)
	{
		if (charslot < 0 || charslot >= _charSlotMapping.size())
		{
			_log.warn(toString() + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		Integer objectId = _charSlotMapping.get(charslot);
		return objectId;
	}

	@Override
	protected void onForcedDisconnection()
	{
		if(_log.isDebugEnabled())
			_log.info("Client " + toString() + " disconnected abnormally.");
	}

	@Override
	protected void onDisconnection()
	{
		ThreadPoolManager.getInstance().execute(new DisconnectTask());
	}

	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString()
	{
		try
		{
			InetAddress address = getConnection().getSocket().getInetAddress();
			switch (getState())
			{
			case CONNECTED:
				return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
			case AUTHED:
				return "[Account: " + getAccountName() + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + "]";
			case IN_GAME:
				return "[Character: " + (getActiveChar() == null ? "disconnected" : getActiveChar().getName()) + " - Account: " + getAccountName() + " - IP: "
						+ (address == null ? "disconnected" : address.getHostAddress()) + "]";
			default:
				throw new IllegalStateException("Missing state on switch");
			}
		}
		catch (NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}

	private final class DisconnectTask implements Runnable
	{
		public void run()
		{
			synchronized (L2GameClient.this)
			{
				if (_disconnected)
					return;
				
				_disconnected = true;
			}
			
			LoginServerThread.getInstance().sendLogout(getAccountName());
			
			L2PcInstance player = getActiveChar();
			setActiveChar(null);
			
			if (player != null) // this should only happen on connection loss
			{
				player.setClient(null);
				
				saveCharToDisk(player, true);
				player.deleteMe();
			}
		}
	}
}
