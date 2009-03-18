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
import java.nio.channels.SelectionKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import javolution.text.TextBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmocore.network.ISocket;
import org.mmocore.network.MMOConnection;
import org.mmocore.network.ReceivablePacket;
import org.mmocore.network.SelectorThread;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.LoginServerThread;
import com.l2jfree.gameserver.ThreadPoolManager;
import com.l2jfree.gameserver.LoginServerThread.SessionKey;
import com.l2jfree.gameserver.model.CharSelectInfoPackage;
import com.l2jfree.gameserver.model.L2World;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfree.gameserver.threadmanager.FIFORunnableQueue;
import com.l2jfree.tools.security.BlowFishKeygen;
import com.l2jfree.tools.security.GameCrypt;

/**
 * Represents a client connected on Game Server
 * 
 * @author KenM
 */
public final class L2GameClient extends MMOConnection<L2GameClient>
{
	private static final Log _log = LogFactory.getLog(L2GameClient.class);
	
	/**
	 * @author KenM
	 */
	public static enum GameClientState
	{
		CONNECTED, // client has just connected
		AUTHED, // client has authed but doesnt has character attached to it yet
		IN_GAME; // client has selected a char and is in game
	}
	
	private GameClientState _state = GameClientState.CONNECTED;
	private String _accountName;
	private SessionKey _sessionId;
	private L2PcInstance _activeChar;
	private boolean _isAuthedGG;
	private int[] _charSlotMapping;
	private GameCrypt _crypt;
	private boolean _disconnected;
	private boolean _isDetached = false;
	private boolean _protocol;

	protected ScheduledFuture<?> _cleanupTask = null;
	
	public L2GameClient(SelectorThread<L2GameClient> selectorThread, ISocket socket, SelectionKey key)
	{
		super(selectorThread, socket, key);
	}
	
	private GameCrypt getCrypt()
	{
		if (_crypt == null)
			_crypt = new GameCrypt();
		
		return _crypt;
	}
	
	public byte[] enableCrypt()
	{
		byte[] key = BlowFishKeygen.getRandomKey();
		getCrypt().setKey(key);
		return key;
	}
	
	public GameClientState getState()
	{
		return _state;
	}
	
	public void setState(GameClientState pState)
	{
		_state = pState;
	}
	
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		getCrypt().decrypt(buf.array(), buf.position(), size);
		return true;
	}
	
	@Override
	public boolean encrypt(final ByteBuffer buf, final int size)
	{
		getCrypt().encrypt(buf.array(), buf.position(), size);
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
		if (_isDetached)
			return;
		gsp.runImpl(this, getActiveChar());
		
		super.sendPacket(gsp);
	}

	@Override
	public void closeNow()
	{
		super.closeNow();
		cleanMe(true);
	}

	public boolean isDetached()
	{
		return _isDetached;
	}
	
	public void isDetached(boolean b)
	{
		_isDetached = b;
	}

	public void execute(ReceivablePacket<L2GameClient> rp)
	{
		getPacketQueue().execute(rp);
	}
	
	private FIFORunnableQueue<ReceivablePacket<L2GameClient>> _packetQueue;
	
	private FIFORunnableQueue<ReceivablePacket<L2GameClient>> getPacketQueue()
	{
		if (_packetQueue == null)
			_packetQueue = new FIFORunnableQueue<ReceivablePacket<L2GameClient>>() {};
		
		return _packetQueue;
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
			
			statement =
				con
					.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			statement =
				con
					.prepareStatement("DELETE FROM item_attributes WHERE itemId IN (SELECT object_id FROM items WHERE items.owner_id=?)");
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
		_charSlotMapping = new int[chars.length];
		
		int i = 0;
		for (CharSelectInfoPackage element : chars)
			_charSlotMapping[i++] = element.getObjectId();
	}
	
	/**
	 * @param charslot
	 * @return
	 */
	private int getObjectIdForSlot(int charslot)
	{
		if (_charSlotMapping == null || charslot < 0 || charslot >= _charSlotMapping.length)
		{
			_log.warn(toString() + " tried to delete Character in slot " + charslot
				+ " but no characters exits at that slot.");
			return -1;
		}
		
		return _charSlotMapping[charslot];
	}
	
	@Override
	protected void onForcedDisconnection()
	{
		if (_log.isDebugEnabled())
			_log.info("Client " + toString() + " disconnected abnormally.");
	}
	
	@Override
	protected void onDisconnection()
	{
		ThreadPoolManager.getInstance().execute(new DisconnectTask());
	}
	
	@Override
	public String toString()
	{
		TextBuilder tb = TextBuilder.newInstance();
		
		tb.append("[State: ").append(getState());
		
		String host = null;
		InetAddress address =  getSocket().getInetAddress();
		if (address != null)
			host = address.getHostAddress();
		
		if (host != null)
			tb.append(" | IP: ").append(String.format("%-15s", host));
		
		String account = getAccountName();
		if (account != null)
			tb.append(" | Account: ").append(String.format("%-15s", account));
		
		L2PcInstance player = getActiveChar();
		if (player != null)
			tb.append(" | Character: ").append(String.format("%-15s", player.getName()));
		
		tb.append("]");
		
		final String toString = tb.toString();
		
		TextBuilder.recycle(tb);
		
		return toString;
	}

	public boolean isProtocolOk()
	{
		return _protocol;
	}

	public void setProtocolOk(boolean b)
	{
		_protocol = b;
	}
	
	private final class DisconnectTask implements Runnable
	{
		/**
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			boolean fast = true;

			try
			{
				isDetached(true);
				L2PcInstance player = L2GameClient.this.getActiveChar();
				if (player != null && player.isInCombat())
				{
					fast = false;
				}
				cleanMe(fast);
			}
			catch (Exception e1)
			{
				_log.error("Error while disconnecting client.", e1);
			}
		}
	}

	public void cleanMe(boolean fast)
	{
		try
		{
			synchronized(this)
			{
				if (_cleanupTask == null)
				{
					_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), fast ? 500 : 15000L);
				}
			}
		}
		catch (Exception e1)
		{
			_log.error("Error during cleanup.", e1);
		}
	}

	public class CleanupTask implements Runnable
	{
		public void run()
		{
			synchronized (L2GameClient.this)
			{
				if (_disconnected)
					return;
				
				_disconnected = true;
			}
			
			// to prevent call cleanMe() again
			isDetached(false);
			
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
