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
package com.l2jfree.gameserver.model.actor.appearance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.Config;
import com.l2jfree.L2DatabaseFactory;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.model.restriction.global.GlobalRestrictions;
import com.l2jfree.gameserver.util.Util;
import com.l2jfree.tools.util.HexUtil;

public final class PcAppearance
{
	private static final Log _log = LogFactory.getLog(PcAppearance.class);
	
	/** The default hexadecimal color of players' name (white is 0xFFFFFF) */
	public static final int DEFAULT_NAME_COLOR = 0xFFFFFF;
	/** The default hexadecimal color of players' title (light blue is 0xFFFF77) */
	public static final int DEFAULT_TITLE_COLOR = 0xFFFF77;
	
	// =========================================================
	// Data Field
	private L2PcInstance _owner;
	private byte _face;
	private byte _hairColor;
	private byte _hairStyle;
	private boolean _sex; // Female true(1)
	
	/** true if the player is invisible */
	private boolean _invisible = false;
	
	/** The current visisble name of this palyer, not necessarily the real one */
	private String _visibleName;
	/** The current visisble title of this palyer, not necessarily the real one */
	private String _visibleTitle;
	
	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int _nameColor = DEFAULT_NAME_COLOR;
	private int _visibleNameColor = -1;
	// No idea if this should be stored between sessions
	private int _nickColor = -1;
	/** The hexadecimal Color of players title (light blue is 0xFFFF77) */
	private int _titleColor = DEFAULT_TITLE_COLOR;
	private int _visibleTitleColor = -1;
	
	// =========================================================
	// Constructor
	public PcAppearance(byte face, byte hColor, byte hStyle, boolean sex)
	{
		_face = face;
		_hairColor = hColor;
		_hairStyle = hStyle;
		_sex = sex;
	}
	
	public void setVisibleName(String visibleName)
	{
		_visibleName = visibleName;
	}
	
	public String getVisibleName()
	{
		if (_visibleName != null)
			return _visibleName;
		
		return _owner.getName();
	}
	
	public void setVisibleTitle(String visibleTitle)
	{
		_visibleTitle = visibleTitle;
	}
	
	public String getVisibleTitle()
	{
		if (_visibleTitle != null)
			return _visibleTitle;
		
		return _owner.getTitle();
	}
	
	public byte getFace()
	{
		return _face;
	}
	
	public void setFace(int value)
	{
		_face = (byte)value;
	}
	
	public byte getHairColor()
	{
		return _hairColor;
	}
	
	public void setHairColor(int value)
	{
		_hairColor = (byte)value;
	}
	
	public byte getHairStyle()
	{
		return _hairStyle;
	}
	
	public void setHairStyle(int value)
	{
		_hairStyle = (byte)value;
	}
	
	public boolean getSex()
	{
		return _sex;
	}
	
	public void setSex(boolean isfemale)
	{
		_sex = isfemale;
	}
	
	public void setInvisible()
	{
		_invisible = true;
	}
	
	public void setVisible()
	{
		_invisible = false;
	}
	
	public boolean isInvisible()
	{
		return _invisible;
	}
	
	public int getNameColor()
	{
		final int value = GlobalRestrictions.getNameColor(_owner);
		
		if (value != -1)
			return value;
		
		if (_visibleNameColor != -1)
			return _visibleNameColor;
		
		if (_nickColor != -1)
			return _nickColor;
		
		return _nameColor;
	}
	
	public void setNameColor(int nameColor)
	{
		_nameColor = nameColor;
	}
	
	public void setNameColor(int red, int green, int blue)
	{
		setNameColor((red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16));
	}
	
	public int getTitleColor()
	{
		final int value = GlobalRestrictions.getTitleColor(_owner);
		
		if (value != -1)
			return value;
		
		if (_visibleTitleColor != -1)
			return _visibleTitleColor;
		
		return _titleColor;
	}
	
	public void setTitleColor(int titleColor)
	{
		_titleColor = titleColor;
	}
	
	public void setTitleColor(int red, int green, int blue)
	{
		setTitleColor((red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16));
	}
	
	public void setOwner(L2PcInstance owner)
	{
		_owner = owner;
	}
	
	public int getNickColor()
	{
		return _nickColor;
	}
	
	public void setNickColor(int color)
	{
		_nickColor = color;
	}
	
	public void updateNameTitleColor()
	{
		int nameColor = -1;
		int titleColor = -1;
		
		if (_owner.isClanLeader() && Config.CLAN_LEADER_COLOR_ENABLED
				&& _owner.getClan().getLevel() >= Config.CLAN_LEADER_COLOR_CLAN_LEVEL)
		{
			if (Config.CLAN_LEADER_COLORED == Config.ClanLeaderColored.name)
				nameColor = Config.CLAN_LEADER_COLOR;
			else
				titleColor = Config.CLAN_LEADER_COLOR;
		}
		
		if (Config.CHAR_VIP_COLOR_ENABLED)
		{
			if (_owner.isCharViP())
				nameColor = Config.CHAR_VIP_COLOR;
		}
		
		if (Config.ALLOW_OFFLINE_TRADE_COLOR_NAME)
		{
			if (_owner.isInOfflineMode())
				nameColor = Config.OFFLINE_TRADE_COLOR_NAME;
		}
		
		if (_owner.isGM())
		{
			if (Config.GM_NAME_COLOR_ENABLED)
			{
				if (_owner.getAccessLevel() >= 100)
					nameColor = Config.ADMIN_NAME_COLOR;
				else if (_owner.getAccessLevel() >= 75)
					nameColor = Config.GM_NAME_COLOR;
			}
			
			if (Config.GM_TITLE_COLOR_ENABLED)
			{
				if (_owner.getAccessLevel() >= 100)
					titleColor = Config.ADMIN_TITLE_COLOR;
				else if (_owner.getAccessLevel() >= 75)
					titleColor = Config.GM_TITLE_COLOR;
			}
		}
		
		_visibleTitleColor = titleColor;
		_visibleNameColor = nameColor;
		
		_owner.broadcastUserInfo();
	}
	
	public void restoreNameTitleColors()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con
					.prepareStatement("SELECT name_color, title_color FROM character_name_title_colors WHERE char_id=?");
			statement.setInt(1, _owner.getObjectId());
			ResultSet result = statement.executeQuery();
			
			if (result.next())
			{
				setNameColor(Util.reverseRGBChanels(Integer.decode("0x" + result.getString(1))));
				setTitleColor(Util.reverseRGBChanels(Integer.decode("0x" + result.getString(2))));
			}
			else
			{
				setNameColor(PcAppearance.DEFAULT_NAME_COLOR);
				setTitleColor(PcAppearance.DEFAULT_TITLE_COLOR);
			}
			
			result.close();
			statement.close();
		}
		catch (Exception e)
		{
			setNameColor(PcAppearance.DEFAULT_NAME_COLOR);
			setTitleColor(PcAppearance.DEFAULT_TITLE_COLOR);
			
			_log.error("Could not load character name/title colors!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		
		updateNameTitleColor();
	}
	
	public void storeNameTitleColors()
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con
					.prepareStatement("REPLACE INTO character_name_title_colors VALUES(?,?,?)");
			statement.setInt(1, _owner.getObjectId());
			statement.setString(2, HexUtil.fillHex(Util.reverseRGBChanels(_nameColor), 6));
			statement.setString(3, HexUtil.fillHex(Util.reverseRGBChanels(_titleColor), 6));
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.error("Could not store character name/title colors!", e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
}
