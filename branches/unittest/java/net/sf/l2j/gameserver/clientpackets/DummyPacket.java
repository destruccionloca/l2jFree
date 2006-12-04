/**
 * 
 */
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

import net.sf.l2j.gameserver.ClientThread;

/**
 * @author zabbix
 * Lets drink to code!
 */
public class DummyPacket extends ClientBasePacket
{
	private static Logger _log = Logger.getLogger(DummyPacket.class.getName());
	
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
