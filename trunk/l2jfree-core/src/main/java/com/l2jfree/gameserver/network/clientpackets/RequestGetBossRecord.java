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

import com.l2jfree.gameserver.instancemanager.RaidPointsManager;
import com.l2jfree.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfree.gameserver.network.serverpackets.ExGetBossRecord;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Format: (ch) d
 * @author  -Wooden-
 * 
 */
public class RequestGetBossRecord extends L2GameClientPacket
{
	private static final String _C__D0_18_REQUESTGETBOSSRECORD = "[C] D0:18 RequestGetBossRecord";
	private final static Log _log = LogFactory.getLog(RequestGetBossRecord.class.getName());

	private int _bossId;

	/**
	* @param buf
	* @param client
	*/
	@Override
	protected void readImpl()
	{
		_bossId = readD(); // always 0?
	}

	/**
	* @see com.l2jfree.gameserver.network.clientpackets.ClientBasePacket#runImpl()
	*/
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if (_bossId != 0)
		{
			_log.info("C5: RequestGetBossRecord: d: "+_bossId+" ActiveChar: "+activeChar); // should be always 0, log it if isnt 0 for furture research
		}
		activeChar.sendPacket(new ExGetBossRecord(RaidPointsManager.getInstance().getPlayerEntry(activeChar)));
	}

	/**
	* @see com.l2jfree.gameserver.network.BasePacket#getType()
	*/
	@Override
	public String getType()
	{
		return _C__D0_18_REQUESTGETBOSSRECORD;
	}
}
