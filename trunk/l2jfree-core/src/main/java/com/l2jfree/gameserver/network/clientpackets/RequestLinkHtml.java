/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jfree.gameserver.network.clientpackets;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author zabbix
 * Lets drink to code!
 */
public class RequestLinkHtml extends L2GameClientPacket
{
	private final static Log _log = LogFactory.getLog(RequestLinkHtml.class.getName());
	private static final String REQUESTLINKHTML__C__20 = "[C] 20 RequestLinkHtml";
	private String _link;

    @Override
    protected void readImpl()
    {
        _link = readS();
    }
	
	@Override
	public void runImpl()
	{
		L2PcInstance actor = getClient().getActiveChar();
		if(actor == null)
			return;
		
		_link = readS();
		
		if(_link.contains("..") || !_link.contains(".htm"))
		{
			_log.warn("[RequestLinkHtml] hack by " + actor.getName() + "? link contains prohibited characters: '"+_link+"', skipped");
			return;
		}
		
		try
		{
			String filename = "data/html/"+_link;
			NpcHtmlMessage msg = new NpcHtmlMessage(0);
			msg.setFile(filename);
			sendPacket(msg);
		}
		catch (Exception e)
		{
			_log.warn("Bad RequestLinkHtml: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public String getType()
	{
		return REQUESTLINKHTML__C__20;
	}
}
