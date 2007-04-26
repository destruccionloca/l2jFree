package net.sf.l2j.loginserver.beans;

import java.net.InetAddress;

public class BanInfo
{
    private InetAddress _ipAddress;

    // Expiration
    private long        _expiration;

    public BanInfo(InetAddress ipAddress, long expiration)
    {
        _ipAddress = ipAddress;
        _expiration = expiration;
    }

    public InetAddress getAddress()
    {
        return _ipAddress;
    }

    public boolean hasExpired()
    {
        return System.currentTimeMillis() > _expiration;
    }
}