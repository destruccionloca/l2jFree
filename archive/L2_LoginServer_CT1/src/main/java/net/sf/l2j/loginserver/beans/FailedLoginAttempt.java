package net.sf.l2j.loginserver.beans;

import java.net.InetAddress;

public class FailedLoginAttempt {
	// private InetAddress _ipAddress;
	private int _count;

	private long _lastAttempTime;

	private String _lastPassword;

	public FailedLoginAttempt(InetAddress address, String lastPassword) {
		// _ipAddress = address;
		_count = 1;
		_lastAttempTime = System.currentTimeMillis();
		_lastPassword = lastPassword;
	}

	public void increaseCounter(String password) {
		if (!_lastPassword.equals(password)) {
			// check if theres a long time since last wrong try
			if (System.currentTimeMillis() - _lastAttempTime < 300 * 1000) {
				_count++;
			} else {
				// restart the status
				_count = 1;

			}
			_lastPassword = password;
			_lastAttempTime = System.currentTimeMillis();
		} else // trying the same password is not brute force
		{
			_lastAttempTime = System.currentTimeMillis();
		}
	}

	public int getCount() {
		return _count;
	}
}
