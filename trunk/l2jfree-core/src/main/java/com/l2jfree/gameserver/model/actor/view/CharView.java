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
package com.l2jfree.gameserver.model.actor.view;

import com.l2jfree.gameserver.model.actor.L2Character;
import com.l2jfree.gameserver.model.actor.position.ObjectPosition;

/**
 * @author NB4L1
 */
public abstract class CharView<T extends L2Character> implements CharLikeView
{
	protected final T _activeChar;
	
	protected CharView(T activeChar)
	{
		_activeChar = activeChar;
	}
	
	private long _lastRefreshTime;
	
	@Override
	public final void refresh()
	{
		if (System.currentTimeMillis() - _lastRefreshTime < 100)
			return;
		
		_lastRefreshTime = System.currentTimeMillis();
		
		refreshImpl();
	}
	
	protected void refreshImpl()
	{
		final ObjectPosition position = _activeChar.getPosition();
		
		_objectId = _activeChar.getObjectId();
		_x = position.getX();
		_y = position.getY();
		_z = position.getZ();
		_heading = position.getHeading();
		
		// TODO:
		//_runSpd
		//_walkSpd
	}
	
	protected int _objectId;
	protected int _x;
	protected int _y;
	protected int _z;
	protected int _heading;
	protected int _runSpd;
	protected int _walkSpd;
	
	public final int getObjectId()
	{
		return _objectId;
	}
	
	public final int getX()
	{
		return _x;
	}
	
	public final int getY()
	{
		return _y;
	}
	
	public final int getZ()
	{
		return _z;
	}
	
	public final int getHeading()
	{
		return _heading;
	}
	
	public final int getRunSpd()
	{
		return _runSpd;
	}
	
	public final int getWalkSpd()
	{
		return _walkSpd;
	}
	
	public final int getSwimRunSpd()
	{
		return getRunSpd();
	}
	
	public final int getSwimWalkSpd()
	{
		return getWalkSpd();
	}
	
	public final int getFlRunSpd()
	{
		return getRunSpd();
	}
	
	public final int getFlWalkSpd()
	{
		return getWalkSpd();
	}
	
	public final int getFlyRunSpd()
	{
		return getRunSpd();
	}
	
	public final int getFlyWalkSpd()
	{
		return getWalkSpd();
	}
}
