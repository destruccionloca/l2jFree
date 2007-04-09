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
package net.sf.l2j.loginserver.manager;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.loginserver.LoginServer;
import net.sf.l2j.loginserver.beans.Accounts;
import net.sf.l2j.loginserver.beans.SessionKey;
import net.sf.l2j.loginserver.services.AccountsServices;
import net.sf.l2j.loginserver.services.exception.AccountModificationException;
import net.sf.l2j.loginserver.services.exception.HackingException;
import net.sf.l2j.loginserver.thread.GameServerListener;
import net.sf.l2j.loginserver.thread.GameServerThread;
import net.sf.l2j.tools.L2Registry;
import net.sf.l2j.tools.codec.Base64;
import net.sf.l2j.tools.math.ScrambledKeyPair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class handles login on loginserver.
 * It store connection for each account.
 * 
 * The ClientThread use LoginManager to :
 *  - store his connection identifier
 *  - retrieve basic information
 *  - delog an account
 */
public class LoginManager
{
	private static final Log _log = LogFactory.getLog(LoginManager.class);
    private static final Log _logLogin = LogFactory.getLog("login");
    private static final Log _logLoginTries = LogFactory.getLog("login.try");
    private static final Log _logLoginFailed = LogFactory.getLog("login.failed");
	
	private static LoginManager _instance;
	
	//TODO: use 2 id maps (server selection + login ok)
	/** this map contains the session ids that belong to one account */
	private Map<String, SessionKey> _logins;
	/** this map contains the connections of the players that are in the loginserver*/
	private Map<String, Socket> _accountsInLoginServer;
    /** this map contains the number of failed connection for an adress*/
	private Map<String, Integer> _hackProtection;
	private Map<String, String> _lastPassword;
	private KeyPairGenerator _keyGen;
	private ScrambledKeyPair[] _keyPairs;
	private AtomicInteger _keyPairToUpdate;
	private long _lastKeyPairUpdate;
	private Random _rnd;
    private AccountsServices _service = null;
	

	/**
     * Private constructor to avoid direct instantiation. 
     * Initialize a key generator.
	 */
	private LoginManager()
	{
		_log.info("LoginManager initiating");
		_logins = new FastMap<String, SessionKey>();
		_accountsInLoginServer = new FastMap<String, Socket>();
		_hackProtection = new FastMap<String, Integer>();
		_lastPassword = new FastMap<String, String>();
		_keyPairToUpdate = new AtomicInteger(0);
		_keyPairs = new ScrambledKeyPair[10];
        _service = (AccountsServices)L2Registry.getBean("AccountsServices");
        
		try
		{
			_keyGen = KeyPairGenerator.getInstance("RSA");
			RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024,RSAKeyGenParameterSpec.F4);
			_keyGen.initialize(spec);
		}
		catch (GeneralSecurityException e)
		{
			_log.fatal("Error in RSA setup:" + e);
			_log.info("Server shutting down now");
			System.exit(2);
		}
		_rnd = new Random();
        if (_log.isDebugEnabled())_log.debug("LoginController : RSA keygen initiated");
		//generate the initial set of keys
		for(int i = 0; i < 10; i++)
		{
			_keyPairs[i] = new ScrambledKeyPair(_keyGen.generateKeyPair());
		}
		_lastKeyPairUpdate = System.currentTimeMillis();
		_log.info("Stored 10 KeyPair for RSA communication");
	}
	
    /**
     * @return LoginManager singleton
     */
	public static LoginManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new LoginManager();
		}
		
		return _instance;
	}
	
    /**
     * 
     * @param account
     * @param _csocket
     * @return
     */
	public SessionKey assignSessionKeyToLogin(String account, Socket _csocket)
	{
		SessionKey key;
		
		key = new SessionKey(_rnd.nextInt(),_rnd.nextInt(), _rnd.nextInt(), _rnd.nextInt());
		_logins.put(account, key);
		_accountsInLoginServer.put(account, _csocket);
		return key;
	}
	
    /**
     * 
     * @param account
     */
	public void removeAccountFromGameServer(String account)
	{
		if (account != null)
		{
			_logins.remove(account);
		}
	}
	
    /**
     * 
     * @param account
     */
	public void removeAccountFromLoginServer(String account)
	{
		if (account != null)
		{
			_accountsInLoginServer.remove(account);
		}
	}
	
    /**
     * 
     * @param account
     * @return true or false is account is logged
     */
	public boolean isAccountInLoginServer(String account)
	{
		return _accountsInLoginServer.containsKey(account);
	}
	
    /**
     * 
     * @param account
     * @return true or false is account is logged on any gameserver
     */
	public boolean isAccountInAnyGameServer(String account)
	{
		List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
		synchronized (gslist)
		{
			for(GameServerThread gs : gslist)
			{
				if(gs.isPlayerInGameServer(account))
					return true;
			}
		}
		return false;
	}
	
    /**
     * 
     * @param account
     * @return gs id for an account
     */
	public int getGameServerIDforAccount(String account)
	{
		List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
		synchronized (gslist)
		{
			for(GameServerThread gs : gslist)
			{
				if(gs.isPlayerInGameServer(account))
					return gs.getServerID();
			}
		}
		return -1;
	}
	
    /**
     * 
     * @param account
     * @return session key for an account
     */
	public SessionKey getKeyForAccount(String account)
	{
		return _logins.get(account);
	}
	
	
    /**
     * 
     * @param ServerID
     * @return online player count for a server
     */
	public int getOnlinePlayerCount(int ServerID)
	{
		List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
		synchronized (gslist)
		{
			for(GameServerThread gs : gslist)
			{
				if(gs.getServerID() == ServerID)
					return gs.getCurrentPlayers();
			}
		}
		return 0;
	}
	
	/***
     * 
     * @param ServerID
     * @return max allowed online player for a server
	 */
    public int getMaxAllowedOnlinePlayers(int ServerID)
	{
		List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
		synchronized (gslist)
		{
			for(GameServerThread gs : gslist)
			{
				if(gs.getServerID() == ServerID)
					return gs.getMaxPlayers();
			}
		}
		return 0;
	}

	/**
     *  
     * @param loginName
     * @return connection socket for a account
	 */
	public Socket getLoginServerConnection(String loginName)
	{
		return _accountsInLoginServer.get(loginName);
	}
	
    /**
     * 
     * @param user
     * @param banLevel
     */
	public void setAccountAccessLevel(String user, int banLevel)
	{
        try
        {
            _service.changeAccountLevel(user,banLevel);
        }
        catch (AccountModificationException e)
        {
            _log.error("Could not set accessLevl for user : " + user,e);
        }
	}
	
    /**
     * 
     * @param user
     * @return true if a user is a GM account
     */
	public boolean isGM(String user)
	{
        Accounts acc = _service.getAccountById(user);
        if ( acc != null )
            return acc.getAccessLevel() >= Config.GM_MIN;
        else
            return false;
                
	}
    
    /**
     * 
     * @param user
     * @return account if exist, null if not
     */
    public Accounts getAccount (String user)
    {
        Accounts acc = _service.getAccountById(user);
        if ( acc != null )
            return acc;
        else
            return null;
                
    }
	
	/**
	 * <p>This method returns one of the 10 {@link ScrambledKeyPair}.</p>
	 * <p>One of them the re-newed asynchronously using a {@link UpdateKeyPairTask} if necessary.</p>
	 * @return a scrambled keypair
	 */
	public ScrambledKeyPair getScrambledRSAKeyPair()
	{
		// ensure that the task will update the keypair only after a keypair is returned.
		synchronized (_keyPairs)
		{
			if ((System.currentTimeMillis() - _lastKeyPairUpdate) > 1000 * 60) // update a key every minutes
			{
				if(_keyPairToUpdate.get() == 10)
					_keyPairToUpdate.set(0);
				UpdateKeyPairTask task = new UpdateKeyPairTask(_keyPairToUpdate.getAndIncrement());
				task.start();
				_lastKeyPairUpdate = System.currentTimeMillis();
			}
			return _keyPairs[_rnd.nextInt(10)];
		}
	}
	
	/**
	 * user name is not case sensitive any more
	 * @param user
	 * @param password
	 * @param address
	 * @return
	 */
	public boolean loginValid(String user, String password, InetAddress address) throws HackingException
	{
		boolean ok = false;
		
        // o get Last information of connection
        // -----------------------------------
		Integer failedConnects  = _hackProtection.get(address.getHostAddress());
		String lastPassword = _lastPassword.get(address.getHostAddress());
		
        _logLoginTries.info("User trying to connect  '"+user+"' "+address.getHostAddress());
		
        // o Check max number of failed connection
        // -------------------------------------
		if (failedConnects != null && failedConnects > Config.LOGIN_TRY_BEFORE_BAN)
		{
			_log.warn("hacking detected from ip:"+address.getHostAddress()+" .. adding IP to banlist");
			failedConnects++;
			throw new HackingException(address.getHostAddress(), failedConnects);
		}
		
		try
		{			
			
            // o Convert password in utf8 byte array
            // ----------------------------------
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] raw = password.getBytes("UTF-8");
            byte[] hash = md.digest(raw);            
            
            // o find Account
            // -------------
			Accounts acc = _service.getAccountById(user);
            
            // If account is not found
            // try to create it if AUTO_CREATE_ACCOUNTS is activated
            // or return false
            // ------------------------------------------------------
			if (acc == null)
			{
				return handleAccountNotFound(user, address, hash);
			}
            // If account is found
            // check password and update last ip/last active
            // ---------------------------------------------
            else
            {
                ok = checkPassword(hash,acc);
    			if (ok)
    			{
    				acc.setLastactive(new BigDecimal(System.currentTimeMillis()));
                    acc.setLastIp(address.getHostAddress());
                    _service.addOrUpdateAccount(acc);
    			}
            }
		}
		catch (Exception e)
		{
			// digest algo not found ??
			// out of bounds should not be possible
			_log.warn("could not check password:"+e);
			ok = false;
		} 
		
        // If password are different
        // -------------------------
		if (!ok)
		{
            handleBadLogin(user, password, address, failedConnects, lastPassword);
		}
        // else...
		else
		{
			handleGoodLogin(user, address);
		}
		
		return ok;
	}

    /**
     * @param user
     * @param address
     */
    private void handleGoodLogin(String user, InetAddress address)
    {
        // for long running servers, this should prevent blocking 
        // of users that mistype their passwords once every day :)
        _hackProtection.remove(address.getHostAddress());
        _lastPassword.remove(address.getHostAddress());
        if (_logLogin.isDebugEnabled())_log.debug("login successfull for '"+user+"' "+address.getHostAddress());
    }

    /**
     * 
     * If login are different, increment hackProtection counter. It's maybe a hacking attempt
     * 
     * @param user
     * @param password
     * @param address
     * @param failedConnects
     * @param lastPassword
     */
    private void handleBadLogin(String user, String password, InetAddress address, Integer failedConnects, String lastPassword)
    {
        _logLoginFailed.info("login failed for user : '"+user+"' "+address.getHostAddress());
        
        // add 1 to the failed counter for this IP 
        int failedCount = 1;
        if (failedConnects != null)
        {
        	failedCount = failedConnects.intValue() + 1;
        }
        
        if(!password.equals(lastPassword))
        {
        	_hackProtection.put(address.getHostAddress(), new Integer(failedCount));
        	_lastPassword.put(address.getHostAddress(), password);
        }
    }

    /**
     * @param hash
     * @param acc 
     * @return true if password are identical
     */
    private boolean checkPassword(byte[] hash, Accounts acc)
    {
        boolean ok;
        if (_log.isDebugEnabled() )_log.debug("account exists");
        
        ok = true;
        
        byte[] expected = Base64.decode(acc.getPassword());
        
        for (int i=0;i<expected.length;i++)
        {
        	if (hash[i] != expected[i])
        	{
        		ok = false;
        		break;
        	}
        }
        return ok;
    }

    /**
     * @param user
     * @param address
     * @param hash
     * @return true if accounts was successfully created or false is AUTO_CREATE_ACCOUNTS = false or creation failed
     * @throws AccountModificationException
     */
    private boolean handleAccountNotFound(String user, InetAddress address, byte[] hash) throws AccountModificationException
    {
        Accounts acc;
        if (Config.AUTO_CREATE_ACCOUNTS)
        {
        	if ((user.length() >= 2) && (user.length() <= 14))
        	{
                acc = new Accounts(user,Base64.encodeBytes(hash),new BigDecimal(System.currentTimeMillis()),0,address.getHostAddress());
        		_service.addOrUpdateAccount(acc);
        		
                _logLogin.info("created new account for "+ user);

                if ( LoginServer.statusServer != null )
        			LoginServer.statusServer.SendMessageToTelnets("Account created for player "+user);
        		
        		return true;
        		
        	}
            _logLogin.warn("Invalid username creation/use attempt: "+user);
        	return false;
        }
        _logLogin.warn("account missing for user "+user);
        return false;
    }
	
    /**
     * 
     * @param user
     * @return true if user is banned (check access level in DB)
     */
	public boolean loginBanned(String user)
	{
        Accounts acc = _service.getAccountById(user);
        if (acc != null )
        {
            return (acc.getAccessLevel() < 0 );
        }
        else
        {
            _log.warn("could not check ban state.");
            return false;            
        }
                
	}
	
	/**
     * 
     * @param ipAddress
     * @return true if ip was correctly unblocked, false if not or if ip was not blocked
	 */
	public boolean unblockIp(String ipAddress)
	{
		int tries = 0;
		
		if (_hackProtection.containsKey(ipAddress))
			tries = _hackProtection.get(ipAddress);
		
		if (tries > Config.LOGIN_TRY_BEFORE_BAN)
		{
			_hackProtection.remove(ipAddress);
			_log.warn("Removed host from hacklist! IP number: " + ipAddress);
			return true;
		}
		
		return false;
	}
	
	private class UpdateKeyPairTask extends Thread
	{
		private int _keyPairId;
		
		public UpdateKeyPairTask(int keyPairId)
		{
			_keyPairId = keyPairId;
		}
		
		public void run()
		{
			_keyPairs[_keyPairId] = new ScrambledKeyPair(_keyGen.generateKeyPair());
            
            if (_log.isDebugEnabled())_log.debug("Updated a RSA key");
		}
	}
	

}
