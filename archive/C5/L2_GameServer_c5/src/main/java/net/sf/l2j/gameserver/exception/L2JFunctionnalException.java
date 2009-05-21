package net.sf.l2j.gameserver.exception;

public class L2JFunctionnalException extends Exception {

	/**
	 * serial Version uid
	 */
	private static final long serialVersionUID = -4561810450704054574L;

	
	public L2JFunctionnalException (String msg)
	{
		super ("Functionnal error : "+msg);
	}
}
