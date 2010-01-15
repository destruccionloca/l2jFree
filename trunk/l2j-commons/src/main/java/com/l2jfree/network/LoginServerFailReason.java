package com.l2jfree.network;

public enum LoginServerFailReason
{
	REASON_NONE("None"), // 0x00
	REASON_IP_BANNED("Reason: ip banned"), // 0x01
	REASON_IP_RESERVED("Reason: ip reserved"), // 0x02
	REASON_WRONG_HEXID("Reason: wrong hexid"), // 0x03
	REASON_ID_RESERVED("Reason: id reserved"), // 0x04
	REASON_NO_FREE_ID("Reason: no free ID"), // 0x05
	REASON_NOT_AUTHED("Not authed"), // 0x06
	REASON_ALREADY_LOGGED_IN("Reason: already logged in"); // 0x07
	
	private final String _reason;
	
	private LoginServerFailReason(String reason)
	{
		_reason = reason;
	}
	
	public String getReasonString()
	{
		return _reason;
	}
	
	private static final LoginServerFailReason[] VALUES = LoginServerFailReason.values();
	
	public static LoginServerFailReason valueOf(int index)
	{
		if (index < 0 || VALUES.length <= index)
			return REASON_NONE;
		
		return VALUES[index];
	}
}
