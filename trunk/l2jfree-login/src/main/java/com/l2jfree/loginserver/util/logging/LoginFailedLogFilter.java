package com.l2jfree.loginserver.util.logging;

import com.l2jfree.util.logging.L2LogFilter;

public final class LoginFailedLogFilter extends L2LogFilter
{
	@Override
	protected String getLoggerName()
	{
		return "login.failed";
	}
}
