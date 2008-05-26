package net.sf.l2j.gameserver.exception.clientpackets;

import net.sf.l2j.gameserver.exception.L2JFunctionnalException;

public class MultiSellChooseException extends L2JFunctionnalException {

	/**
	 * serial Version uid
	 */
	private static final long serialVersionUID = 5760255150312655106L;
	
	public MultiSellChooseException (String msg)
	{
		super (msg);
	}
}
