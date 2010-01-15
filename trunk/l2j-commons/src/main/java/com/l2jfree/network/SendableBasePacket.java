package com.l2jfree.network;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class SendableBasePacket
{
	protected static final Log _log = LogFactory.getLog(SendableBasePacket.class);
	
	private final ByteArrayOutputStream _bao = new ByteArrayOutputStream();
	
	protected SendableBasePacket(int opCode)
	{
		writeC(opCode);
	}
	
	protected final void writeD(int value)
	{
		_bao.write(value & 0xff);
		_bao.write(value >> 8 & 0xff);
		_bao.write(value >> 16 & 0xff);
		_bao.write(value >> 24 & 0xff);
	}
	
	protected final void writeH(int value)
	{
		_bao.write(value & 0xff);
		_bao.write(value >> 8 & 0xff);
	}
	
	protected final void writeC(int value)
	{
		_bao.write(value & 0xff);
	}
	
	protected final void writeF(double org)
	{
		long value = Double.doubleToRawLongBits(org);
		_bao.write((int)(value & 0xff));
		_bao.write((int)(value >> 8 & 0xff));
		_bao.write((int)(value >> 16 & 0xff));
		_bao.write((int)(value >> 24 & 0xff));
		_bao.write((int)(value >> 32 & 0xff));
		_bao.write((int)(value >> 40 & 0xff));
		_bao.write((int)(value >> 48 & 0xff));
		_bao.write((int)(value >> 56 & 0xff));
	}
	
	protected final void writeS(String text)
	{
		if (text != null)
		{
			try
			{
				final byte[] bytes = text.getBytes("UTF-16LE");
				
				_bao.write(bytes, 0, bytes.length);
			}
			catch (UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
		
		_bao.write(0);
		_bao.write(0);
	}
	
	protected final void writeB(byte[] bytes)
	{
		_bao.write(bytes, 0, bytes.length);
	}
	
	public byte[] getContent()
	{
		writeD(0x00); // reserve for checksum
		
		int padding = _bao.size() % 8;
		if (padding != 0)
			for (int i = padding; i < 8; i++)
				writeC(0x00);
		
		return _bao.toByteArray();
	}
}
