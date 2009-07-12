/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.loginserver.serverpackets;

import com.l2jfree.loginserver.L2LoginClient;

/** Format: d d: the failure reason */
public final class LoginFail extends L2LoginServerPacket
{
	//codes done by savormix
	public static final int REASON_THERE_IS_A_SYSTEM_ERROR = 1;
	public static final int REASON_PASSWORD_INCORRECT = 2; //3
	public static final int REASON_ACCESS_FAILED_TRY_AGAIN = 4; //6,8,9,10,11,13,14
	public static final int REASON_ACCOUNT_INFO_INCORRECT = 5;
	public static final int REASON_ALREADY_IN_USE = 7;
	public static final int REASON_AGE_LIMITATION = 12;
	public static final int REASON_TOO_HIGH_TRAFFIC = 15;
	public static final int REASON_MAINTENANCE_UNDERGOING = 16;
	public static final int REASON_CHANGE_TEMP_PASSWORD = 17;
	public static final int REASON_GAME_TIME_EXPIRED = 18;
	public static final int REASON_NO_TIME_LEFT = 19;
	public static final int REASON_SYSTEM_ERROR = 20;
	public static final int REASON_ACCESS_FAILED = 21;
	public static final int REASON_IP_RESTRICTED = 22;
	public static final int REASON_IGNORE = 23; //shows the copyright; 24-27, 34
	//BACKSLASH(28), //29
	public static final int REASON_WEEK_TIME_FINISHED = 30;
	/** also pops up a dialog that allows to login using the id */
	public static final int REASON_INVALID_SECURITY_CARD_NO = 31;
	public static final int REASON_TIME_LIMITATION_AGE_NOT_VERIFIED = 32;
	public static final int REASON_INCORRECT_COUPON_FOR_SERVER = 33;
	public static final int REASON_USING_A_COMPUTER_NO_DUAL_BOX = 35;
	public static final int REASON_SUSPENDED_INACTIVITY = 36;
	public static final int REASON_MUST_ACCEPT_AGREEMENT = 37;
	public static final int REASON_GUARDIANS_CONSENT_NEEDED = 38;
	public static final int REASON_PENDING_WITHDRAWL_REQUEST = 39;
	public static final int REASON_SUSPENDED_PHONE_CC = 40;
	public static final int REASON_CHANGE_PASSWORD_AND_QUIZ = 41;
	public static final int REASON_ACCOUNT_LIMITATION = 42;
	/** Unknown packet to use this */
	public static final int REASON_TEMP_BAN = 1136;
	//43-204 shows the copyright (nothing), negative values do the same.
	private final int _reason;

	public LoginFail(int reason, boolean accessLevel)
	{
		if (!accessLevel)
			_reason = reason;
		else
			_reason = getReasonFromBan(reason);
	}

	public LoginFail(int reason)
	{
		this(reason, false);
	}

	/**
	 * @see com.l2jserver.mmocore.network.SendablePacket#write()
	 */
	@Override
	protected void write(L2LoginClient client)
	{
		writeC(0x01);
		writeC(_reason);
		//overlap default message
		/* not ready
		if ((_reason <= -429 && _reason > -457) || 
				(_reason < -457 &&  _reason >= -463) ||
				_reason == -1136 || _reason == -1796 ||
				(_reason <= -1804 && _reason >= -1809) ||
				_reason == -2042 || _reason == -2108 ||
				(_reason <= -2115 && _reason >= -2123) ||
				(_reason <= -2126 && _reason >= -2128))
		{
			client.sendPacket(new SystemMessage(_reason));
		} */
	}

	/**
	 * Returns LoginFail code from a given access level.
	 * @param accessLevel current banned account's accessLevel
	 * @return reason
	 */
	public static final int getReasonFromBan(int accessLevel)
    {		
    	switch (accessLevel)
    	{
    	//1-10: automatic bans
    	//These bans should be done straight from your website
    	//or from a sql script
    	case -1:
    		return REASON_SUSPENDED_INACTIVITY;
    	case -2:
    		return REASON_CHANGE_TEMP_PASSWORD;
    	case -3:
    		return REASON_CHANGE_PASSWORD_AND_QUIZ;
    	//10-20: specific bans
    	case -10:
    		return REASON_ACCOUNT_INFO_INCORRECT;
    	case -11:
    		return REASON_AGE_LIMITATION;
    	case -12:
    		return REASON_GUARDIANS_CONSENT_NEEDED;
    	case -13:
    		return REASON_GAME_TIME_EXPIRED;
    	case -14:
    		return REASON_NO_TIME_LEFT;
    	case -15:
    		return REASON_WEEK_TIME_FINISHED;
    	case -16:
    		return REASON_INCORRECT_COUPON_FOR_SERVER;
    	case -17:
    		return REASON_PENDING_WITHDRAWL_REQUEST;
    	default:
    		return REASON_SUSPENDED_PHONE_CC;
    	}
    }
}
