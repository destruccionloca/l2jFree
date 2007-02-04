/**
 * 
 */
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author zabbix
 * Lets drink to code!
 */
public class RequestLinkHtml extends ClientBasePacket
{
	private final static Log _log = LogFactory.getLog(RequestLinkHtml.class.getName());
	private static final String REQUESTLINKHTML__C__20 = "[C] 20 RequestLinkHtml";
	private String _link;

	public RequestLinkHtml(ByteBuffer buf, ClientThread client)
	{
		super(buf,client);
	}
	
	public void runImpl()
	{
		L2PcInstance actor = getClient().getActiveChar();
		if(actor == null)
			return;
		
		_link = readS();
		
		if(_link.contains("..") || !_link.contains(".htm"))
		{
			_log.warn("[RequestLinkHtml] hack? link contains prohibited characters: '"+_link+"', skipped");
			return;
		}
		
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setFile(_link);
		
		sendPacket(msg);
	}
	
	public String getType()
	{
		return REQUESTLINKHTML__C__20;
	}
}
