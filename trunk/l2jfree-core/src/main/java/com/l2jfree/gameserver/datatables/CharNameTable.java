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
import com.l2jfree.lang.L2Integer;

public final class CharNameTable
{
	private static final Log _log = LogFactory.getLog(CharNameTable.class);

	public static CharNameTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final Map<Integer, CharacterInfo> _mapByObjectId = new FastMap<Integer, CharacterInfo>();
	private final Map<String, CharacterInfo> _mapByName = new FastMap<String, CharacterInfo>();
	
	private CharNameTable()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("SELECT charId, char_name FROM characters");
			ResultSet rset = statement.executeQuery();
			
			while (rset.next())
				update(rset.getInt("charId"), rset.getString("char_name"));
			
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
	
	public void update(int objectId, String name)
	{
		CharacterInfo characterInfo = _mapByObjectId.get(objectId);
		if (characterInfo == null)
			characterInfo = new CharacterInfo(objectId);
		
		characterInfo.updateName(name);
	}
	
	private class CharacterInfo
	{
		private final Integer _objectId;
		private String _name;
		
		private CharacterInfo(int objectId)
		{
			_objectId = L2Integer.valueOf(objectId);
			
			CharacterInfo characterInfo = _mapByObjectId.put(_objectId, this);
			if (characterInfo != null)
				_log.warn("CharNameTable: Duplicated objectId: [" + this + "] - [" + characterInfo + "]");
		}
		
		private void updateName(String name)
		{
			if (_name != null)
				_mapByName.remove(_name.toLowerCase());
			
			_name = name.intern();
			
			CharacterInfo characterInfo = _mapByName.put(_name.toLowerCase(), this);
			if (characterInfo != null)
				_log.warn("CharNameTable: Duplicated hashName: [" + this + "] - [" + characterInfo + "]");
		}
		
		@Override
		public String toString()
		{
			return "objectId: " + _objectId + ", name: " + _name;
		}
	}
	
	public boolean doesCharNameExist(String name)
	{
		return getByName(name) != null;
	}
	
	public int accountCharNumber(String account)
	{
		int number = 0;
		
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(con);
			
			PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM characters WHERE account_name=?");
			statement.setString(1, account);
			ResultSet rset = statement.executeQuery();
			
			if (rset.next())
				number = rset.getInt(1);
			
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
		
		return number;
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder
	{
		protected static final CharNameTable _instance = new CharNameTable();
	}
}
