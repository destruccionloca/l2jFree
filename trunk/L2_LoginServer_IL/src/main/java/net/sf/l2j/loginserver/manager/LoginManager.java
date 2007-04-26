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
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;

import javolution.util.FastMap;
import javolution.util.FastSet;
import net.sf.l2j.Config;
import net.sf.l2j.loginserver.L2LoginClient;
import net.sf.l2j.loginserver.L2LoginServer;
import net.sf.l2j.loginserver.beans.Accounts;
import net.sf.l2j.loginserver.beans.BanInfo;
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
    protected byte[][] _blowfishKeys;
    private static final int BLOWFISH_KEYS = 20;
    protected Set<L2LoginClient> _clients = new FastSet<L2LoginClient>();

    private Map<InetAddress, BanInfo> _bannedIps = new FastMap<InetAddress, BanInfo>().setShared(true);

	/**
     * Private constructor to avoid direct instantiation. 
     * Initialize a key generator.
	 */
	private LoginManager()
	{
		try
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
            
            this.testCipher((RSAPrivateKey) _keyPairs[0].getPair().getPrivate());
            
            // Store keys for blowfish communication
            this.generateBlowFishKeys();
        } 
        catch (GeneralSecurityException e)
        {
            _log.fatal("FATAL: Failed initializing LoginManager. Reason: "+e.getMessage(),e);
            System.exit(1);
        }
        
	}
    
    /**
     * This is mostly to force the initialization of the Crypto Implementation, avoiding it being done on runtime when its first needed.<BR>
     * In short it avoids the worst-case execution time on runtime by doing it on loading.
     * @param key Any private RSA Key just for testing purposes.
     * @throws GeneralSecurityException if a underlying exception was thrown by the Cipher
     */
    private void testCipher(RSAPrivateKey key) throws GeneralSecurityException
    {
        // avoid worst-case execution, KenM
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
    }    
	
    /**
     * 
     *
     */
    private void generateBlowFishKeys()
    {
        _blowfishKeys = new byte[BLOWFISH_KEYS][16];

        for (int i = 0; i < BLOWFISH_KEYS; i++)
        {
            for (int j = 0; j < _blowfishKeys[i].length; j++)
            {
                _blowfishKeys[i][j] = (byte) (_rnd.nextInt(255)+1);
            }
        }
        _log.info("Stored "+_blowfishKeys.length+" keys for Blowfish communication");
    }    
    
    /**
     * @return Returns a random key
     */
    public byte[] getBlowfishKey()
    {
        return _blowfishKeys[(int) (Math.random()*BLOWFISH_KEYS)];
    }
    
    public void addLoginClient(L2LoginClient client)
    {
        synchronized (_clients)
        {
            _clients.add(client);
        }
    }

    public void removeLoginClient(L2LoginClient client)
    {
        synchronized (_clients)
        {
            _clients.remove(client);
        }
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

                if ( L2LoginServer.statusServer != null )
        			L2LoginServer.statusServer.SendMessageToTelnets("Account created for player "+user);
        		
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
     * Adds the address to the ban list of the login server, with the given duration.
     * 
     * @param address The Address to be banned.
     * @param expiration Timestamp in miliseconds when this ban expires
     * @throws UnknownHostException if the address is invalid.
     */
    public void addBanForAddress(String address, long expiration) throws UnknownHostException
    {
        InetAddress netAddress = InetAddress.getByName(address);
        _bannedIps.put(netAddress, new BanInfo(netAddress,  expiration));
    }
    
    /**
     * Adds the address to the ban list of the login server, with the given duration.
     * 
     * @param address The Address to be banned.
     * @param duration is miliseconds
     */
    public void addBanForAddress(InetAddress address, long duration)
    {
        _bannedIps.put(address, new BanInfo(address,  System.currentTimeMillis() + duration));
    }    
    
    public boolean isBannedAddres(InetAddress address)
    {
        BanInfo bi = _bannedIps.get(address);
        if (bi != null)
        {
            if (bi.hasExpired())
            {
                _bannedIps.remove(address);
                return false;
            }
            else
            {
                return true;
            }
        }
        return false;
    }    
	
    public Map<InetAddress, BanInfo> getBannedIps()
    {
        return _bannedIps;
    }


    /**
     * Remove the specified address from the ban list
     * @param address The address to be removed from the ban list
     * @return true if the ban was removed, false if there was no ban for this ip
     */
    public boolean removeBanForAddress(InetAddress address)
    {
        return _bannedIps.remove(address) != null;
    }
    
    /**
     * Remove the specified address from the ban list
     * @param address The address to be removed from the ban list
     * @return true if the ban was removed, false if there was no ban for this ip or the address was invalid.
     */
    public boolean removeBanForAddress(String address)
    {
        try
        {
            return this.removeBanForAddress(InetAddress.getByName(address));
        }
        catch (UnknownHostException e)
        {
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
