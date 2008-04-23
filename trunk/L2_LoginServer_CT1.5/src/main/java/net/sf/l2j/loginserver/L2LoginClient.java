/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;

import net.sf.l2j.loginserver.beans.SessionKey;
import net.sf.l2j.loginserver.crypt.LoginCrypt;
import net.sf.l2j.loginserver.manager.LoginManager;
import net.sf.l2j.loginserver.serverpackets.L2LoginServerPacket;
import net.sf.l2j.loginserver.serverpackets.LoginFail;
import net.sf.l2j.loginserver.serverpackets.LoginFailReason;
import net.sf.l2j.loginserver.serverpackets.PlayFail;
import net.sf.l2j.loginserver.serverpackets.PlayFailReason;
import net.sf.l2j.tools.math.ScrambledKeyPair;
import net.sf.l2j.util.Rnd; 

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mmocore.network.MMOClient;
import org.mmocore.network.MMOConnection;

/**
 * Represents a client connected into the LoginServer
 *
 * @author  KenM
 */
public class L2LoginClient extends MMOClient<MMOConnection<L2LoginClient>>
{
	private static final Log _log = LogFactory.getLog(L2LoginClient.class.getName());
	
	public static enum LoginClientState { CONNECTED, AUTHED_GG, AUTHED_LOGIN};
	
	private LoginClientState _state;
	
	// Crypt
	private LoginCrypt _loginCrypt;
	private ScrambledKeyPair _scrambledPair;
	private byte[] _blowfishKey;
	
	private String _account;
	private int _accessLevel;
	private SessionKey _sessionKey;
	private int _sessionId;
    private boolean _joinedGS;
    private String _ip;
	private long _connectionStartTime;
	
	/**
	 * @param con
	 */
	public L2LoginClient(MMOConnection<L2LoginClient> con)
	{
		super(con);
		_state = LoginClientState.CONNECTED;
		_ip = getConnection().getSocket().getInetAddress().getHostAddress();
		
		_scrambledPair = LoginManager.getInstance().getScrambledRSAKeyPair();
		_blowfishKey = LoginManager.getInstance().getBlowfishKey();
		_sessionId = Rnd.nextInt();
		_connectionStartTime = System.currentTimeMillis();
		_loginCrypt = new LoginCrypt();
		_loginCrypt.setKey(_blowfishKey);
	}
	
	public String getIp()
	{
		return _ip;
	}
	
	/**
	 * @see com.l2jserver.mmocore.interfaces.MMOClient#decrypt(java.nio.ByteBuffer, int)
	 */
	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		boolean ret = false;
		try
		{
			ret = _loginCrypt.decrypt(buf.array(), buf.position(), size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			this.closeNow();
			return false;
		}
		
		if (!ret)
		{
			byte[] dump = new byte[size];
			System.arraycopy(buf.array(), buf.position(), dump, 0, size);
			_log.warn("Wrong checksum from client: "+this.toString());
			this.closeNow();
		}
		
		return ret;
	}

	/**
	 * @see com.l2jserver.mmocore.interfaces.MMOClient#encrypt(java.nio.ByteBuffer, int)
	 */
	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		final int offset = buf.position();
		try
		{
			size = _loginCrypt.encrypt(buf.array(), offset, size);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		buf.position(offset + size);
		return true;
	}

	public LoginClientState getState()
	{
		return _state;
	}
	
	public void setState(LoginClientState state)
	{
		_state = state;
	}
	
	public byte[] getBlowfishKey()
	{
		return _blowfishKey;
	}
	
	public byte[] getScrambledModulus()
	{
		return _scrambledPair.getScrambledModulus();
	}
	
	public RSAPrivateKey getRSAPrivateKey()
	{
		return (RSAPrivateKey) _scrambledPair.getPair().getPrivate();
	}
	
	public String getAccount()
	{
		return _account;
	}
	
	public void setAccount(String account)
	{
		_account = account;
	}
    
    public void setAccessLevel(int accessLevel)
    {
        _accessLevel = accessLevel;
    }    
	
	public int getAccessLevel()
	{
		return _accessLevel;
	}
	
	public int getSessionId()
	{
		return _sessionId;
	}
	
	public void setSessionKey(SessionKey sessionKey)
	{
		_sessionKey = sessionKey;
	}
	
    public boolean hasJoinedGS() 
    {
        return _joinedGS;
    }
    
    public void setJoinedGS(boolean val) 
    {
        _joinedGS = val;
    }
    
	public SessionKey getSessionKey()
	{
		return _sessionKey;
	}
	
	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}
	
	public void sendPacket(L2LoginServerPacket lsp)
	{
		this.getConnection().sendPacket(lsp);
	}
	
	public void close(LoginFailReason reason)
	{
		this.getConnection().close(new LoginFail(reason));
	}
	
	public void close(L2LoginServerPacket lsp)
	{
		this.getConnection().close(lsp);
	}
    
    public void close(PlayFailReason reason) 
    { 
        this.getConnection().close(new PlayFail(reason)); 
    } 
    
    public InetAddress getInetAddress ()
    {
        return getConnection().getSocket().getInetAddress();
    }
	
	@Override
	public void onDisconnection()
	{
		if (_log.isDebugEnabled())
		{
			_log.info("DISCONNECTED: "+this.toString());
		}
        
        // If player was not on GS, don't forget to remove it from authed login on LS
        if ( this.getState() == LoginClientState.AUTHED_LOGIN && !this.hasJoinedGS())
        {
            LoginManager.getInstance().removeAuthedLoginClient(this.getAccount());
        }
	}
	
	public String toString()
	{
		InetAddress address = getConnection().getSocket().getInetAddress();
		if (this.getState() == LoginClientState.AUTHED_LOGIN)
		{
			return "["+this.getAccount()+" ("+(address == null ? "disconnected" : address.getHostAddress())+")]";
		}
		else
		{
			return "["+(address == null ? "disconnected" : address.getHostAddress())+"]";
		}
	}
}
