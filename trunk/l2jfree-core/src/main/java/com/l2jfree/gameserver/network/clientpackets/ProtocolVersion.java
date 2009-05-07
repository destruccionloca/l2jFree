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

import com.l2jfree.Config;
import com.l2jfree.gameserver.network.serverpackets.KeyPacket;

/**
 * This class represents the first packet that is sent by the client to the game server.
 * 
 * @version $Revision: 1.5.2.8.2.8 $ $Date: 2005/04/02 10:43:04 $
 */
public class ProtocolVersion extends L2GameClientPacket
{
	private static final String	_C__00_PROTOCOLVERSION	= "[C] 00 ProtocolVersion";
	private static Log			_log					= LogFactory.getLog(ProtocolVersion.class.getName());

	private long				_version;

	/**
	 * packet type id 0x00
	 * format: cd
	 */
	@Override
	protected void readImpl()
	{
		_version = readD();
	}

	@Override
	protected void runImpl()
	{
		KeyPacket kp = null;
		// this packet is never encrypted
		if (_version == -2)
		{
			if (_log.isDebugEnabled())
				_log.info("Ping received");
			// this is just a ping attempt from the C2+ client
			getClient().closeNow();
		}
		else if (_version < Config.MIN_PROTOCOL_REVISION)
		{
			_log.info("Client Protocol Revision:" + _version + " is too low. only " + Config.MIN_PROTOCOL_REVISION + " and " + Config.MAX_PROTOCOL_REVISION
					+ " are supported. Closing connection.");
			_log.warn("Wrong Protocol Version " + _version);
			kp = new KeyPacket(getClient().enableCrypt(), 0);
			getClient().sendPacket(kp);
			getClient().setProtocolOk(false);
		}
		else if (_version > Config.MAX_PROTOCOL_REVISION)
		{
			_log.info("Client Protocol Revision:" + _version + " is too high. only " + Config.MIN_PROTOCOL_REVISION + " and " + Config.MAX_PROTOCOL_REVISION
					+ " are supported. Closing connection.");
			_log.warn("Wrong Protocol Version " + _version);
			kp = new KeyPacket(getClient().enableCrypt(), 0);
			getClient().sendPacket(kp);
			getClient().setProtocolOk(false);
		}
		else
		{
			if (_log.isDebugEnabled())
				_log.debug("Client Protocol Revision is ok: " + _version);
			kp = new KeyPacket(getClient().enableCrypt(), 1);
			sendPacket(kp);
			getClient().setProtocolOk(true);
		}
		kp = null;
	}

	@Override
	public String getType()
	{
		return _C__00_PROTOCOLVERSION;
	}
}
