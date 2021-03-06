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
package com.l2jfree.loginserver.network.gameserver;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.l2jserver.util.IPSubnet;

/**
 * @author savormix
 */
public class L2GameServerAddress extends IPSubnet
{
	private final InetAddress _advertisedAddress;
	
	public L2GameServerAddress(String subnet, String advertisedAddress) throws UnknownHostException
	{
		super(subnet);
		
		_advertisedAddress = InetAddress.getByName(advertisedAddress);
	}
	
	public byte[] getAdvertisedAddress()
	{
		return _advertisedAddress.getAddress();
	}
	
	@Override
	public String toString()
	{
		return _advertisedAddress + " @ " + super.toString();
	}
}
