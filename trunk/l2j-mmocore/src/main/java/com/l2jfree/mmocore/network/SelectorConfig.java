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
package com.l2jfree.mmocore.network;

import java.nio.ByteOrder;

/**
 * @author KenM
 */
public final class SelectorConfig<T extends MMOConnection<T, RP, SP>, RP extends ReceivablePacket<T, RP, SP>, SP extends SendablePacket<T, RP, SP>>
{
	private IAcceptFilter ACCEPT_FILTER;
	private IClientFactory<T, RP, SP> CLIENT_FACTORY;
	private IMMOExecutor<T, RP, SP> EXECUTOR;
	private IPacketHandler<T, RP, SP> PACKET_HANDLER;
	
	private int BUFFER_SIZE = 64 * 1024; // 0xFFFF + 1
	
	private int MAX_SEND_PER_PASS = 1;
	private int MAX_READ_PER_PASS = 1;
	
	private int MAX_SEND_BYTE_PER_PASS = BUFFER_SIZE;
	private int MAX_READ_BYTE_PER_PASS = BUFFER_SIZE;
	
	private int SLEEP_TIME = 10;
	
	private int HELPER_BUFFER_COUNT = 20;
	
	private ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
	
	public SelectorConfig()
	{
	}
	
	public void setBufferSize(int bufferSize)
	{
		if (bufferSize < 64 * 1024) // 0xFFFF + 1
			throw new IllegalArgumentException();
		
		BUFFER_SIZE = bufferSize;
	}
	
	int getBufferSize()
	{
		return BUFFER_SIZE;
	}
	
	public void setHelperBufferCount(int helperBufferCount)
	{
		HELPER_BUFFER_COUNT = helperBufferCount;
	}
	
	int getHelperBufferCount()
	{
		return HELPER_BUFFER_COUNT;
	}
	
	public void setByteOrder(ByteOrder byteOrder)
	{
		BYTE_ORDER = byteOrder;
	}
	
	ByteOrder getByteOrder()
	{
		return BYTE_ORDER;
	}
	
	public void setAcceptFilter(IAcceptFilter acceptFilter)
	{
		ACCEPT_FILTER = acceptFilter;
	}
	
	IAcceptFilter getAcceptFilter()
	{
		return ACCEPT_FILTER;
	}
	
	public void setClientFactory(IClientFactory<T, RP, SP> clientFactory)
	{
		CLIENT_FACTORY = clientFactory;
	}
	
	IClientFactory<T, RP, SP> getClientFactory()
	{
		return CLIENT_FACTORY;
	}
	
	public void setExecutor(IMMOExecutor<T, RP, SP> executor)
	{
		EXECUTOR = executor;
	}
	
	IMMOExecutor<T, RP, SP> getExecutor()
	{
		return EXECUTOR;
	}
	
	public void setPacketHandler(IPacketHandler<T, RP, SP> packetHandler)
	{
		PACKET_HANDLER = packetHandler;
	}
	
	IPacketHandler<T, RP, SP> getPacketHandler()
	{
		return PACKET_HANDLER;
	}
	
	/**
	 * Server will try to send maxSendPerPass packets per socket write call however it may send less if the write buffer
	 * was filled before achieving this value.
	 * 
	 * @param The maximum number of packets to be sent on a single socket write call
	 */
	public void setMaxSendPerPass(int maxSendPerPass)
	{
		MAX_SEND_PER_PASS = maxSendPerPass;
	}
	
	/**
	 * @return The maximum number of packets sent in an socket write call
	 */
	int getMaxSendPerPass()
	{
		return MAX_SEND_PER_PASS;
	}
	
	public void setMaxReadPerPass(int maxReadPerPass)
	{
		MAX_READ_PER_PASS = maxReadPerPass;
	}
	
	int getMaxReadPerPass()
	{
		return MAX_READ_PER_PASS;
	}
	
	public void setMaxSendBytePerPass(int maxSendBytePerPass)
	{
		MAX_SEND_BYTE_PER_PASS = maxSendBytePerPass;
	}
	
	int getMaxSendBytePerPass()
	{
		return MAX_SEND_BYTE_PER_PASS;
	}
	
	public void setMaxReadBytePerPass(int maxReadBytePerPass)
	{
		MAX_READ_BYTE_PER_PASS = maxReadBytePerPass;
	}
	
	int getMaxReadBytePerPass()
	{
		return MAX_READ_BYTE_PER_PASS;
	}
	
	/**
	 * Defines how much time (in milis) should the selector sleep, an higher value increases throughput but also
	 * increases latency(to a max of the sleep value itself).<BR>
	 * Also an extremely high value(usually > 100) will decrease throughput due to the server not doing enough sends per
	 * second (depends on max sends per pass).<BR>
	 * <BR>
	 * Recommended values:<BR>
	 * 1 for minimal latency.<BR>
	 * 10-30 for an latency/troughput trade-off based on your needs.<BR>
	 * 
	 * @param sleepTime the sleepTime to set
	 */
	public void setSelectorSleepTime(int sleepTime)
	{
		SLEEP_TIME = sleepTime;
	}
	
	/**
	 * @return the sleepTime setting for the selector
	 */
	int getSelectorSleepTime()
	{
		return SLEEP_TIME;
	}
}
