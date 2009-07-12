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
package org.mmocore.network;

public final class HeaderInfo<T>
{
	private int _headerPending;
	private int _dataPending;
	private boolean _multiPacket;
	private T _client;
	
	HeaderInfo()
	{
	}
	
	public HeaderInfo<T> set(int headerPending, int dataPending, boolean multiPacket, T client)
	{
		setHeaderPending(headerPending);
		setDataPending(dataPending);
		setMultiPacket(multiPacket);
		setClient(client);
		return this;
	}
	
	boolean headerFinished()
	{
		return getHeaderPending() == 0;
	}
	
	boolean packetFinished()
	{
		return getDataPending() == 0;
	}
	
	/**
	 * @param dataPending the dataPending to set
	 */
	private void setDataPending(int dataPending)
	{
		_dataPending = dataPending;
	}
	
	/**
	 * @return the dataPending
	 */
	int getDataPending()
	{
		return _dataPending;
	}
	
	/**
	 * @param headerPending the headerPending to set
	 */
	private void setHeaderPending(int headerPending)
	{
		_headerPending = headerPending;
	}
	
	/**
	 * @return the headerPending
	 */
	int getHeaderPending()
	{
		return _headerPending;
	}
	
	/**
	 * @param client the client to set
	 */
	void setClient(T client)
	{
		_client = client;
	}
	
	/**
	 * @return the client
	 */
	T getClient()
	{
		return _client;
	}
	
	/**
	 * @param multiPacket the multiPacket to set
	 */
	private void setMultiPacket(boolean multiPacket)
	{
		_multiPacket = multiPacket;
	}
	
	/**
	 * @return the multiPacket
	 */
	boolean isMultiPacket()
	{
		return _multiPacket;
	}
}
