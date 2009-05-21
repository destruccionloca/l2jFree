/**
 * 
 */
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.ClientThread;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author zabbix
 * Lets drink to code!
 */
public class DummyPacket extends ClientBasePacket
{
	private final static Log _log = LogFactory.getLog(DummyPacket.class.getName());
	
	private int _packetId;
	
	public DummyPacket(ByteBuffer buf, ClientThread client, int packetId)
	{
		super(buf,client);
		_packetId = packetId;
	}

	public void runImpl()
	{
		_log.warn("DummyPacket " + _packetId + " (Length = " + getLength() + ") recieved.");
		//getClient().getConnection().close();
	}

	public String getType()
	{
		return "DummyPacket";
	}
}
