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
