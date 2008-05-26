package net.sf.l2j.util;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.tools.network.Net;

public class GameServerNetConfig {
	private String _hostName;

	private String _hostAddress;

	private List<Net> _nets = new FastList<Net>();

	public GameServerNetConfig(String hostName) {
		_hostName = hostName;
	}

	public void addNet(String net, String mask) {
		Net _net = new Net(net, mask);
		if (_net != null)
			_nets.add(_net);
	}

	public boolean checkHost(String _ip) {
		boolean _rightHost = false;
		for (Net net : _nets)
			if (net.isInNet(_ip)) {
				_rightHost = true;
				break;
			}
		return _rightHost;
	}

	public String getHost() {
		return _hostName;
	}

	public void setHost(String hostName) {
		_hostName = hostName;
	}

	public String getIp() {
		return _hostAddress;
	}

	public void setIp(String hostAddress) {
		_hostAddress = hostAddress;
	}
}
