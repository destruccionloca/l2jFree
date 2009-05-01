/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package org.mmocore.network;

/**
 * @author KenM
 */
public abstract class SendablePacket<T extends MMOConnection<T>> extends AbstractPacket
{
	protected SendablePacket()
	{
	}
	
	protected void writeC(boolean value)
	{
		getByteBuffer().put((byte)(value ? 1 : 0));
	}
	
	protected void writeC(long value)
	{
		getByteBuffer().put(value < Byte.MAX_VALUE ? (byte)value : Byte.MAX_VALUE);
	}
	
	protected void writeH(boolean value)
	{
		getByteBuffer().putShort((short)(value ? 1 : 0));
	}
	
	protected void writeH(long value)
	{
		getByteBuffer().putShort(value < Short.MAX_VALUE ? (short)value : Short.MAX_VALUE);
	}
	
	protected void writeD(boolean value)
	{
		getByteBuffer().putInt(value ? 1 : 0);
	}
	
	protected void writeD(long value)
	{
		getByteBuffer().putInt(value < Integer.MAX_VALUE ? (int)value : Integer.MAX_VALUE);
	}
	
	protected void writeQ(boolean value)
	{
		getByteBuffer().putLong(value ? 1 : 0);
	}
	
	protected void writeQ(long value)
	{
		getByteBuffer().putLong(value);
	}
	
	protected void writeF(double value)
	{
		getByteBuffer().putDouble(value);
	}
	
	protected void writeB(byte[] data)
	{
		getByteBuffer().put(data);
	}
	
	protected void writeS(CharSequence charSequence)
	{
		if (charSequence == null)
			charSequence = "";
		
		int length = charSequence.length();
		for (int i = 0; i < length; i++)
			getByteBuffer().putChar(charSequence.charAt(i));
		
		getByteBuffer().putChar('\000');
	}
	
	protected abstract void write(T client);
	
	protected abstract int getHeaderSize();
	
	protected abstract void writeHeader(int dataSize);
}
