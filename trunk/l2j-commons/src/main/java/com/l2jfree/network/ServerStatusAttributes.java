package com.l2jfree.network;

// Compatible, legacy values
public enum ServerStatusAttributes
{
	NONE,
	SERVER_LIST_STATUS,
	SERVER_LIST_CLOCK,
	SERVER_LIST_BRACKETS,
	SERVER_LIST_MAX_PLAYERS,
	TEST_SERVER,
	SERVER_LIST_PVP,
	SERVER_LIST_UNK,
	SERVER_LIST_HIDE_NAME,
	SERVER_AGE_LIMITATION;
	
	private static final ServerStatusAttributes[] VALUES = ServerStatusAttributes.values();
	
	public static ServerStatusAttributes valueOf(int index)
	{
		if (index < 0 || VALUES.length <= index)
			return NONE;
		
		return VALUES[index];
	}
}
