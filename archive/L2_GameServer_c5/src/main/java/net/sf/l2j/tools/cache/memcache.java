package net.sf.l2j.tools.cache;

import java.util.HashMap;
import java.util.Map;

import net.sf.ehcache.Ehcache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Basic memory cache
 * 
 * @deprecated use ehcache instead
 * @see Ehcache
 */
public class memcache
{
	private final static Log _log = LogFactory.getLog(memcache.class.getName());
	private Map<Integer,String> _hms;
	private Map<Integer,Integer> _hmi;
	private Map<Integer,Long> _last_access;
	
	private static final memcache _instance = new memcache();
	
	public static memcache getInstance()
	{
		return _instance;
	}
	
	private memcache()
	{
		_hms = new HashMap<Integer,String>();
		_hmi = new HashMap<Integer,Integer>();
		_last_access = new HashMap<Integer,Long>();
	}

	private void check_expired()
	{
		for(Integer k : _hmi.keySet())
			if(_last_access.get(k) + 3600000 < System.currentTimeMillis())
			{
//				_hmi.remove(k);
//				_last_access.remove(k);
		   	}

		for(Integer k : _hms.keySet())
			if(_last_access.get(k) + 3600000 < System.currentTimeMillis())
			{
//				_hms.remove(k);
//				_last_access.remove(k);
		   	}
	}

	public void set(String type, String key, int value)
	{
		int hash = (type+"->"+key).hashCode();
//	    _log.debug("Set memcache "+type+"("+key+")["+hash+"] to "+value);
		_hmi.put(hash, value);
		_last_access.put(hash, System.currentTimeMillis());
		check_expired();
	}

	public boolean is_set(String type, String key)
	{
		int hash = (type+"->"+key).hashCode();
		boolean exists = _hmi.containsKey(hash) || _hms.containsKey(hash);
		if(exists)
			_last_access.put(hash, System.currentTimeMillis());

		check_expired();
	    _log.debug("Check exists memcache "+type+"("+key+")["+hash+"] is "+exists);
		return exists;
	}

	public Integer get_int(String type, String key)
	{
		int hash = (type+"->"+key).hashCode();
		_last_access.put(hash, System.currentTimeMillis());
		check_expired();
	    _log.debug("Get memcache "+type+"("+key+")["+hash+"] = "+_hmi.get(hash));
		return _hmi.get(hash);
	}
}
