package net.sf.l2j.loginserver.serverpackets;

public enum LoginFailReason
{
    REASON_SYSTEM_ERROR(0x01), 
    REASON_PASS_WRONG(0x02), 
    REASON_USER_OR_PASS_WRONG(0x03), 
    REASON_ACCESS_FAILED(0x04), 
    REASON_ACCOUNT_IN_USE(0x07), 
    REASON_ACCOUNT_BANNED(0x09);

    private final int _code;

    LoginFailReason(int code)
    {
        _code = code;
    }

    public final int getCode()
    {
        return _code;
    }
}