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

import javolution.text.TextBuilder;

/**
 * @author KenM
 */
public abstract class ReceivablePacket<T extends MMOConnection<T>> extends AbstractPacket implements Runnable
{
	protected ReceivablePacket()
	{
	}
	
	private T _client;
	
	void setClient(T client)
	{
		_client = client;
	}
	
	public T getClient()
	{
		return _client;
	}
	
	protected int getAvaliableBytes()
	{
		return getByteBuffer().remaining();
	}
	
	protected abstract boolean read();
	
	public abstract void run();
	
	protected void readB(byte[] dst)
	{
		getByteBuffer().get(dst);
	}
	
	protected void readB(byte[] dst, int offset, int len)
	{
		getByteBuffer().get(dst, offset, len);
	}
	
	protected int readC()
	{
		return getByteBuffer().get() & 0xFF;
	}
	
	protected int readH()
	{
		return getByteBuffer().getShort() & 0xFFFF;
	}
	
	protected int readD()
	{
		return getByteBuffer().getInt();
	}
	
	protected long readQ()
	{
		return getByteBuffer().getLong();
	}
	
	protected double readF()
	{
		return getByteBuffer().getDouble();
	}
	
	protected String readS()
	{
		TextBuilder tb = TextBuilder.newInstance();
		
		for (char c; (c = getByteBuffer().getChar()) != 0;)
			tb.append(c);
		
		String str = tb.toString();
		TextBuilder.recycle(tb);
		return str;
	}
}
