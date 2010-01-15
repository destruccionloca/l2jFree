package com.l2jfree.network;

// Good, Normal and Full are not used for years
// Oh and they wont be used. ever.
public enum ServerStatus
{
	STATUS_AUTO,
	STATUS_GOOD,
	STATUS_NORMAL,
	STATUS_FULL,
	STATUS_DOWN,
	STATUS_GM_ONLY;
	
	private static final ServerStatus[] VALUES = ServerStatus.values();
	
	public static ServerStatus valueOf(int index)
	{
		if (index < 0 || VALUES.length <= index)
			return STATUS_AUTO;
		
		return VALUES[index];
	}
}
