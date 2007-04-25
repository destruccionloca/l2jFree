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
package net.sf.l2j.loginserver.thread;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.interfaces.RSAPrivateKey;

import net.sf.l2j.Config;
import net.sf.l2j.loginserver.beans.SessionKey;
import net.sf.l2j.loginserver.clientpackets.RequestAuthLogin;
import net.sf.l2j.loginserver.clientpackets.RequestServerLogin;
import net.sf.l2j.loginserver.gameserverpackets.ServerStatus;
import net.sf.l2j.loginserver.manager.BanManager;
import net.sf.l2j.loginserver.manager.GameServerManager;
import net.sf.l2j.loginserver.manager.LoginManager;
import net.sf.l2j.loginserver.serverpackets.GGAuth;
import net.sf.l2j.loginserver.serverpackets.Init;
import net.sf.l2j.loginserver.serverpackets.LoginFail;
import net.sf.l2j.loginserver.serverpackets.LoginOk;
import net.sf.l2j.loginserver.serverpackets.PlayFail;
import net.sf.l2j.loginserver.serverpackets.PlayOk;
import net.sf.l2j.loginserver.serverpackets.ServerBasePacket;
import net.sf.l2j.loginserver.serverpackets.ServerList;
import net.sf.l2j.loginserver.services.exception.HackingException;
import net.sf.l2j.tools.math.ScrambledKeyPair;
import net.sf.l2j.tools.util.Util;
import net.sf.l2j.util.NewCrypt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...
 * 
 * @version $Revision: 1.15.2.5.2.5 $ $Date: 2005/04/06 16:13:46 $
 */
public class ClientThread extends Thread
{
	private final static Log _log = LogFactory.getLog(ClientThread.class);
	
	private InputStream _in;
	private OutputStream _out;
	private NewCrypt _crypt;
	
	private Socket _csocket;

	private String ip;
    private boolean _ggAuthRecieved;
    
    private RSAPrivateKey _privateKey;
    
	
	public ClientThread(Socket client) throws IOException
	{
		super("Login Client "+client.getInetAddress());
		setDaemon(true);
		_csocket = client;
		
		ip = client.getInetAddress().getHostAddress();
		
		_in = client.getInputStream();
		_out = new BufferedOutputStream(client.getOutputStream());
        _crypt = new NewCrypt("_;5.]94-31==-%xT!^[$\000");
		_ggAuthRecieved = false;
        
        if (BanManager.getInstance().isIpBanned(ip))
        {
            LoginFail lok = new LoginFail(LoginFail.REASON_ACCOUNT_BANNED);
            sendPacket(lok);
            
            try
            {
                _csocket.close();
            }
            catch (Exception e1)
            {
                // ignore problems
            }
        } else {
            start();
        }
	}
	
	public void run()
	{
        if (_log.isDebugEnabled())  _log.debug("loginserver thread[C] started");
		
		int lengthHi =0;
		int lengthLo =0;
		int length = 0;
		boolean checksumOk = false;
		SessionKey sessionKey = new SessionKey(-1,-1,-1,-1);
		String account = null;
		
		try
		{
			ScrambledKeyPair srambledPair = LoginManager.getInstance().getScrambledRSAKeyPair();
			_privateKey = (RSAPrivateKey) srambledPair.getPair().getPrivate();
			Init startPacket = new Init(srambledPair.getScrambledModulus());
			_out.write(startPacket.getLength() & 0xff);
			_out.write(startPacket.getLength() >> 8 &0xff);
			_out.write(startPacket.getContent());
			_out.flush();
            if (_log.isDebugEnabled())  _log.debug("[S]\n"+Util.printData(startPacket.getContent()));
			
			while (true)
			{
                lengthLo = _in.read();
				lengthHi = _in.read();
				length= lengthHi*256 + lengthLo;  
				
				if (length < 2 )
				{
                    if (_log.isDebugEnabled()) _log.debug("LoginServer: Client terminated the connection or sent illegal packet size.");
					break;
				}
				
				byte[] incoming = new byte[length];
				incoming[0] = (byte) lengthLo;
				incoming[1] = (byte) lengthHi;
				
				int receivedBytes = 0;
				int newBytes = 0;
				while (newBytes != -1 && receivedBytes<length-2)
				{
					newBytes =  _in.read(incoming, 2, length-2);
					receivedBytes = receivedBytes + newBytes;
				}
				
				if (receivedBytes != length-2)
				{
					_log.warn("Incomplete Packet is sent to the server, closing connection.");
					break;
				}
				
				byte[] decrypt = new byte[length - 2];
				System.arraycopy(incoming, 2, decrypt, 0, decrypt.length);
				// decrypt if we have a key
				decrypt = _crypt.decrypt(decrypt);
				checksumOk = _crypt.checksum(decrypt);
				
				if (!checksumOk)
				{
					//_log.warn("Incorrect packet checksum, closing connection..");
                    _log.warn("Client is not using latest Authentication method.");
					break;
				}
				
				if (_log.isDebugEnabled()) 
                    _log.debug("[C]\n"+Util.printData(decrypt));
				
				int packetType = decrypt[0]&0xff;
				
				/**
				 * if recieved packet is not ggauth packet
				 * and gg authorization enforced in server config - 
				 * dont allow client to log in
				 */
				if(Config.FORCE_GGAUTH && packetType != 0x07 && !_ggAuthRecieved)
				{
					_log.warn("[ClientThread] client bypassed GGAuth, kicking off..");
                    LoginFail lof = new LoginFail(LoginFail.REASON_ACCESS_FAILED);
                    sendPacket(lof);
                    break;
				}
				
				switch (packetType)
				{
					case 00:
					{
						RequestAuthLogin ral = new RequestAuthLogin(decrypt, _privateKey);
						account = ral.getUser().toLowerCase();
                        if (_log.isDebugEnabled())_log.debug("RequestAuthLogin from user:" + account);
						
						LoginManager lc = LoginManager.getInstance();
						if (LoginManager.getInstance().loginValid(account, ral.getPassword(), _csocket.getInetAddress()))
						{	
                            if (LoginManager.getInstance().loginBanned(account))
                            {
                                //Login BANNED
                                LoginFail lok = new LoginFail(LoginFail.REASON_ACCOUNT_BANNED);
                                sendPacket(lok);
                            }
                            else
                            {
                                if (lc.isGM(account) || (!lc.isAccountInAnyGameServer(account) && !lc.isAccountInLoginServer(account)))
                                {
                                    sessionKey = lc.assignSessionKeyToLogin(account, _csocket);
                                    if (_log.isDebugEnabled())  _log.debug("assigned SessionKey:" + sessionKey.toString());
                                    if(Config.SHOW_LICENCE)
                                    {
                                    	LoginOk lok = new LoginOk(sessionKey);
                                    	sendPacket(lok);
                                    }
                                    else
                                    {
                                    	ServerList sl = GameServerManager.getInstance().makeServerList(lc.isGM(account), ip);
                						sendPacket(sl);
                                    }
                                }
                                else
                                {
                                    if (_log.isDebugEnabled())  _log.debug("KICKING!");
                                    if (lc.isAccountInLoginServer(account)) {
                                        _log.warn("Account is in use on Login server (kicking off): " + account);
                                        try
                                        {
                                            lc.removeAccountFromLoginServer(account);
                                        }
                                        catch(NullPointerException e)
                                        {
                                            _log.warn("Error while kicking loginserver account, lost connection?");
                                        }
                                    }
                                    if (lc.isAccountInAnyGameServer(account)) {
                                        _log.warn("Account is in use on Game server (kicking off): " + account);
                                    	GameServerManager.getInstance().getGameServerThread(lc.getGameServerIDforAccount(account)).KickPlayer(account);
                                        lc.removeAccountFromGameServer(account);
                                    }
                                    LoginFail lok = new LoginFail(LoginFail.REASON_ACCOUNT_IN_USE);
                                    sendPacket(lok);
                                }
                            }
						}
						else
						{
							LoginFail lok = new LoginFail(LoginFail.REASON_USER_OR_PASS_WRONG);
							sendPacket(lok);
                            if (_log.isDebugEnabled())  _log.debug("login failed sent");
						}

						break;
					}
					
					case 02:
					{
                        if (_log.isDebugEnabled())  _log.info("RequestServerLogin");
						RequestServerLogin rsl = new RequestServerLogin(decrypt);
						if (_log.isDebugEnabled())  _log.info("login to server:" + rsl.getServerID());
						LoginManager lc = LoginManager.getInstance();
						if(lc.getOnlinePlayerCount(rsl.getServerID()-1) >= lc.getMaxAllowedOnlinePlayers(rsl.getServerID()-1))
						{
                            if (!lc.isGM(account))
                            {
                                PlayFail pf = new PlayFail(PlayFail.REASON_TOO_MANY_PLAYERS);
                                sendPacket(pf);
                                break;
                            }
						}
						if((GameServerManager.getInstance().getGameServerStatus(rsl.getServerID()-1) == ServerStatus.STATUS_GM_ONLY)
								&& !lc.isGM(account))
						{
							PlayFail pf = new PlayFail(PlayFail.REASON_SYSTEM_ERROR);
							sendPacket(pf);
							break;
						}
						PlayOk po = new PlayOk(sessionKey);
						sendPacket(po);
						break;
					}
					
					case 05:
					{
						if (_log.isDebugEnabled())  _log.info("RequestServerList");
						//RequestServerList rsl = new RequestServerList(decrypt);
						
						ServerList sl = GameServerManager.getInstance().makeServerList(LoginManager.getInstance().isGM(account), ip);
						if (_log.isDebugEnabled()) 
						{
							byte [] content = sl.getContent();
							_log.info(Util.printData(content));
						}
						sendPacket(sl);
						break;
					}
                    
                    case 07: // new GG Auth packet (GG = GameGuard)
                    {
                        GGAuth sp = new GGAuth(GGAuth.SKIP_GG_AUTH_REQUEST);
                        sendPacket(sp);
                        _ggAuthRecieved=true;
                        break;
                    }
					
					default:
					{
						_log.warn("Unknown Packet:" + packetType);
						_log.warn(Util.printData(decrypt));
					}
				}
			}
		}
		catch (SocketException e)
		{
			if ( _log.isDebugEnabled())_log.debug("Connection closed unexpectedly.");
		}
		catch (HackingException e)
		{
			BanManager.getInstance().addBannedIP(e.getIP(), e.getConnects());
		}
		catch (Exception e)
		{
			_log.warn(e.getMessage(), e);
		}
		finally
		{
			LoginServerThread.getInstance().removeFloodProtection(_csocket.getInetAddress().getHostAddress());
			try
			{
				_csocket.close();
			}
			catch (Exception e1)
			{
				// ignore problems
			}
			
			LoginManager.getInstance().removeAccountFromLoginServer(account);
			if (_log.isDebugEnabled())  _log.debug("loginserver thread[C] stopped");
		}
	}
	
	/**
	 * @param sl
	 * @throws IOException
	 */
	private void sendPacket(ServerBasePacket sl) throws IOException
	{
		byte[] data = sl.getContent();
		_crypt.checksum(data);
		if (_log.isDebugEnabled())  _log.debug("[S]\n"+Util.printData(data));
		data = _crypt.crypt(data);

		int len = data.length+2;
		_out.write(len & 0xff);
		_out.write(len >> 8 &0xff);
		_out.write(data);
		_out.flush();
	}
}
