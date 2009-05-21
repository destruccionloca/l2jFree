package com.l2jfree.loginserver.services.exception;

public final class MaturityException extends Exception {
	private static final long serialVersionUID = 6179849705218182298L;

	public MaturityException(int accAge, int reqAge) {
		super("Account owner is " + accAge + " years old, while you must have " + reqAge + 
				" to login to the game server.");
	}
}
