package net.sf.l2j.loginserver;

import net.sf.l2j.util.Net;
import javolution.util.FastList;
/**
 * @author G1ta0
 *
 */

	public class GameServerNetConfig
	{
	    private String _host;
	    private FastList<Net> _nets = new FastList<Net>();
	    
	    public GameServerNetConfig(String host)
	    {
	    	_host = host;
	    }
	    
	    public void addNet(String net, String mask)
	    {
	    	Net _net = new Net(net,mask);
	    	if (_net!=null)
	    	_nets.add(_net);
	    }
	    
	    public boolean checkHost(String _ip)
	    {
	    	boolean _rightHost=false;
	    	for(Net net: _nets) if (net.isInNet(_ip)) { _rightHost=true; break;}
	    	return _rightHost;
	    }
	    
	    public String getHost()
	    {
	    	return _host;
	    }
	    
	    public void setHost(String host)
	    {
	    	_host=host;
	    }
}