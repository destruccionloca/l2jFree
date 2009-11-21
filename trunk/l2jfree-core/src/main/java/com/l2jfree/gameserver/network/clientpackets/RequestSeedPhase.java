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

/**
 * This packet is sent by the client every time the world map is
 * opened. Should contain info about who owns Seed of Infinity and
 * Seed of Destruction.<BR>
 * BY DEFAULT THIS PACKET IS IGNORED! Most probably you do not get
 * seed info while being in Aden continent.<BR>
 * Related? to serverpacket:<BR>
 * fea1 ExShowSeedMapInfo
 * @author savormix
 */
public final class RequestSeedPhase extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger packet
	}

	@Override
	protected void runImpl()
	{
		//_log.info("RequestSeedPhase received from " + getActiveChar());
		//requestFailed(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
	}
}
