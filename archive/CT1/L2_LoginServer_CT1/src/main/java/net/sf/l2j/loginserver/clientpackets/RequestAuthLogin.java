/*
 * This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.loginserver.clientpackets;

import java.net.InetAddress;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.L2LoginClient;
import net.sf.l2j.loginserver.L2LoginClient.LoginClientState;
import net.sf.l2j.loginserver.beans.GameServerInfo;
import net.sf.l2j.loginserver.manager.BanManager;
import net.sf.l2j.loginserver.manager.LoginManager;
import net.sf.l2j.loginserver.manager.LoginManager.AuthLoginResult;
import net.sf.l2j.loginserver.serverpackets.LoginFailReason;
import net.sf.l2j.loginserver.serverpackets.LoginOk;
import net.sf.l2j.loginserver.serverpackets.ServerList;
import net.sf.l2j.loginserver.services.exception.AccountBannedException;
import net.sf.l2j.loginserver.services.exception.AccountWrongPasswordException;
import net.sf.l2j.loginserver.services.exception.HackingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Format: x
 * 0 (a leading null)
 * x: the rsa encrypted block with the login an password
 */
public class RequestAuthLogin extends L2LoginClientPacket
{
	private final static Log _log = LogFactory.getLog(RequestAuthLogin.class);
	
    private byte[] _raw = new byte[128];
	
    private String _user;
	private String _password;
    private int _ncotp;
	
	/**
	 * @return
	 */
	public String getPassword()
	{
		return _password;
	}

	/**
	 * @return
	 */
	public String getUser()
	{
		return _user;
	}

    public int getOneTimePassword()
    {
        return _ncotp;
    }

    
    @Override
    public boolean readImpl()
    {
        if (this.getAvaliableBytes() >= 128)
        {
            readB(_raw);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public void run()
    {
        byte[] decrypted = null;
        try
        {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
            rsaCipher.init(Cipher.DECRYPT_MODE, this.getClient().getRSAPrivateKey());
            decrypted = rsaCipher.doFinal(_raw, 0x00, 0x80 );
        }
        catch (GeneralSecurityException e)
        {
            e.printStackTrace();
            return;
        }
        
        _user = new String(decrypted, 0x5E, 14 ).trim();
        _user = _user.toLowerCase();
        _password = new String(decrypted, 0x6C, 16).trim();
        _ncotp = decrypted[0x7c];
        _ncotp |= decrypted[0x7d] << 8;
        _ncotp |= decrypted[0x7e] << 16;
        _ncotp |= decrypted[0x7f] << 24;
        
        LoginManager lc = LoginManager.getInstance();
        L2LoginClient client = this.getClient();
        try
        {
            AuthLoginResult result = lc.tryAuthLogin(_user, _password, this.getClient()); 
            switch (result) 
            {
                case AUTH_SUCCESS:
                    client.setAccount(_user);
                    client.setState(LoginClientState.AUTHED_LOGIN);
                    client.setSessionKey(lc.assignSessionKeyToClient(_user, client));
                    if (Config.SHOW_LICENCE)
                    {
                        client.sendPacket(new LoginOk(this.getClient().getSessionKey()));
                    }
                    else
                    {
                        this.getClient().sendPacket(new ServerList(this.getClient()));
                    }
                    break;
                case ALREADY_ON_LS:
                    L2LoginClient oldClient;
                    if ((oldClient = lc.getAuthedClient(_user)) != null)
                    {
                        // kick the other client
                        oldClient.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
                        lc.removeAuthedLoginClient(_user); 
                    }
                    break;
                case ALREADY_ON_GS:
                    GameServerInfo gsi;
                    if ((gsi = lc.getAccountOnGameServer(_user)) != null)
                    {
                        client.close(LoginFailReason.REASON_ACCOUNT_IN_USE);
    
                        // kick from there
                        if (gsi.isAuthed())
                        {
                            gsi.getGameServerThread().kickPlayer(_user);
                        }
                    }
                    break;
                 case SYSTEM_ERROR:
                 default : 
                     client.close(LoginFailReason.REASON_SYSTEM_ERROR);
                     
            }            
        }
        catch (HackingException e)
        {
            InetAddress address = getClient().getConnection().getSocket().getInetAddress();
            BanManager.getInstance().addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN*1000);
            _log.info("Banned ("+address+") for "+Config.LOGIN_BLOCK_AFTER_BAN+" seconds, due to "+e.getConnects()+" incorrect login attempts.");
        }
        catch (AccountBannedException e)
        {
            client.close(LoginFailReason.REASON_ACCOUNT_BANNED);
        } 
        catch (AccountWrongPasswordException e)
        {
            client.close(LoginFailReason.REASON_USER_OR_PASS_WRONG);
        }
    }    

}
