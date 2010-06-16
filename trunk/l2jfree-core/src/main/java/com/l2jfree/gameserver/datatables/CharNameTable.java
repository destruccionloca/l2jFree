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
import java.sql.SQLException;
import java.util.Map;

import javolution.util.FastMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.lang.L2Integer;
import com.l2jfree.util.L2Collections;

public final class CharNameTable
{
	private static final Log _log = LogFactory.getLog(CharNameTable.class);
	
	public static CharNameTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final Map<Integer, CharacterInfo> _mapByObjectId = new FastMap<Integer, CharacterInfo>().setShared(true);
	private final Map<String, CharacterInfo> _mapByName = new FastMap<String, CharacterInfo>().setShared(true);
	
	private CharNameTable()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("SELECT charId, account_name, char_name, accesslevel FROM characters");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
			{
				final int objectId = rset.getInt("charId");
				final String accountName =rset.getString("account_name");
				final String name = rset.getString("char_name");
				final int accessLevel = rset.getInt("accesslevel");
				
				update(objectId, accountName, name, accessLevel);
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warn("", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		_log.info("CharNameTable: Loaded " + _mapByObjectId.size() + " character names.");
	}
	
	public String getByObjectId(Integer objectId)
	{
		CharacterInfo characterInfo = _mapByObjectId.get(objectId);
		
		return characterInfo == null ? null : characterInfo._name;
	}
	
	public Integer getByName(String name)
	{
		CharacterInfo characterInfo = _mapByName.get(name.toLowerCase());
		
		return characterInfo == null ? null : characterInfo._objectId;
	}
	
	public int getAccessLevelById(Integer objectId)
	{
		CharacterInfo characterInfo = _mapByObjectId.get(objectId);
		
		return characterInfo == null ? 0 : characterInfo._accessLevel;
	}
	
	public void update(L2PcInstance player)
	{
		update(player.getObjectId(), player.getAccountName(), player.getName(), player.getAccessLevel());
	}
	
	public void update(int objectId, String accountName, String name, int accessLevel)
	{
		CharacterInfo characterInfo = _mapByObjectId.get(objectId);
		if (characterInfo == null)
			characterInfo = new CharacterInfo(objectId);
		
		characterInfo.updateNames(accountName, name, accessLevel);
	}
	
	private class CharacterInfo
	{
		private final Integer _objectId;
		
		private String _accountName;
		private String _name;
		private int _accessLevel;
		
		private CharacterInfo(int objectId)
		{
			_objectId = L2Integer.valueOf(objectId);
			
			CharacterInfo characterInfo = _mapByObjectId.put(_objectId, this);
			if (characterInfo != null)
				_log.warn("CharNameTable: Duplicated objectId: [" + this + "] - [" + characterInfo + "]");
		}
		
		private void updateNames(String accountName, String name, int accessLevel)
		{
			_accountName = accountName;
			
			if (_name != null)
				_mapByName.remove(_name.toLowerCase());
			
			_name = name.intern();
			
			CharacterInfo characterInfo = _mapByName.put(_name.toLowerCase(), this);
			if (characterInfo != null)
				_log.warn("CharNameTable: Duplicated hashName: [" + this + "] - [" + characterInfo + "]");
			
			_accessLevel = accessLevel;
		}
		
		@Override
		public String toString()
		{
			return "objectId: " + _objectId + ", accountName: " + _accountName + ", name: " + _name;
		}
	}
	
	public boolean doesCharNameExist(String name)
	{
		return getByName(name) != null;
	}
	
	public int accountCharNumber(String account)
	{
		int count = 0;
		
		for (CharacterInfo characterInfo : _mapByObjectId.values())
			if (characterInfo._accountName.equalsIgnoreCase(account))
				count++;
		
		return count;
	}
	
	public Iterable<Integer> getObjectIdsForAccount(final String account)
	{
		return L2Collections.convertingIterable(_mapByObjectId.values(),
				new L2Collections.Converter<CharacterInfo, Integer>() {
					@Override
					public Integer convert(CharacterInfo characterInfo)
					{
						if (characterInfo._accountName.equalsIgnoreCase(account))
							return characterInfo._objectId;
						
						return null;
					}
				});
	}
	
	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CharNameTable _instance = new CharNameTable();
	}
}
