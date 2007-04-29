/**
 * 
 */
package net.sf.l2j.gameserver.clientpackets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author zabbix
 * Lets drink to code!
 */
public class DummyPacket extends L2GameClientPacket
{
	private final static Log _log = LogFactory.getLog(DummyPacket.class.getName());
	
	private int _packetId;
	
    protected void readImpl()
    {
        
    }

	public void runImpl()
	{
		_log.warn("DummyPacket " + _packetId + " received.");
		//getClient().getConnection().close();
	}

	public String getType()
	{
		return "DummyPacket";
	}
}
