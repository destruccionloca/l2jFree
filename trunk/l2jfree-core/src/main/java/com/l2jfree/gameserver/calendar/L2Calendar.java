package com.l2jfree.gameserver.calendar;

import java.io.Serializable;
import java.util.GregorianCalendar;

import com.l2jfree.gameserver.GameTimeController;

public class L2Calendar implements Serializable
{
	private static final long	serialVersionUID	= 3020475454828258247L;

	private GregorianCalendar	cal					= new GregorianCalendar();
	public int					gameTicks			= 3600000 / GameTimeController.getInstance().MILLIS_IN_TICK;
	private long				gameStarted;

	public long getGameStarted()
	{
		return gameStarted;
	}

	public void setGameStarted(long started)
	{
		gameStarted = started;
	}

	public GregorianCalendar getDate()
	{
		return cal;
	}

	public int getGameTime()
	{
		return gameTicks / (GameTimeController.TICKS_PER_SECOND * 10);
	}
}