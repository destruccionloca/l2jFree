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

import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

import javolution.util.FastList;

/**
 * @author KenM
 */
public abstract class MMOConnection<T extends MMOConnection<T>>
{
	private final SelectorThread<T> _selectorThread;
	private final ISocket _socket;
	
	private FastList<SendablePacket<T>> _sendQueue;
	private final SelectionKey _selectionKey;
	
	private ByteBuffer _readBuffer;
	
	private ByteBuffer _primaryWriteBuffer;
	private ByteBuffer _secondaryWriteBuffer;
	
	private long _timeClosed = -1;
	
	protected MMOConnection(SelectorThread<T> selectorThread, ISocket socket, SelectionKey key)
	{
		_selectorThread = selectorThread;
		_socket = socket;
		_selectionKey = key;
	}
	
	public synchronized void sendPacket(SendablePacket<T> sp)
	{
		if (isClosed())
			return;
		
		try
		{
			getSelectionKey().interestOps(getSelectionKey().interestOps() | SelectionKey.OP_WRITE);
			getSendQueue2().addLast(sp);
		}
		catch (CancelledKeyException e)
		{
			// ignore
		}
	}
	
	private SelectorThread<T> getSelectorThread()
	{
		return _selectorThread;
	}
	
	SelectionKey getSelectionKey()
	{
		return _selectionKey;
	}
	
	void enableReadInterest()
	{
		try
		{
			getSelectionKey().interestOps(getSelectionKey().interestOps() | SelectionKey.OP_READ);
		}
		catch (CancelledKeyException e)
		{
			// ignore
		}
	}
	
	void disableReadInterest()
	{
		try
		{
			getSelectionKey().interestOps(getSelectionKey().interestOps() & ~SelectionKey.OP_READ);
		}
		catch (CancelledKeyException e)
		{
			// ignore
		}
	}
	
	void enableWriteInterest()
	{
		try
		{
			getSelectionKey().interestOps(getSelectionKey().interestOps() | SelectionKey.OP_WRITE);
		}
		catch (CancelledKeyException e)
		{
			// ignore
		}
	}
	
	void disableWriteInterest()
	{
		try
		{
			getSelectionKey().interestOps(getSelectionKey().interestOps() & ~SelectionKey.OP_WRITE);
		}
		catch (CancelledKeyException e)
		{
			// ignore
		}
	}
	
	public ISocket getSocket()
	{
		return _socket;
	}
	
	WritableByteChannel getWritableChannel()
	{
		return _socket.getWritableByteChannel();
	}
	
	ReadableByteChannel getReadableByteChannel()
	{
		return _socket.getReadableByteChannel();
	}
	
	synchronized FastList<SendablePacket<T>> getSendQueue2()
	{
		if (_sendQueue == null)
			_sendQueue = new FastList<SendablePacket<T>>();
		
		return _sendQueue;
	}
	
	void createWriteBuffer(ByteBuffer buf)
	{
		if (_primaryWriteBuffer == null)
		{
			//System.err.println("APPENDING FOR NULL");
			//System.err.flush();
			_primaryWriteBuffer = getSelectorThread().getPooledBuffer();
			_primaryWriteBuffer.put(buf);
		}
		else
		{
			//System.err.println("PREPENDING ON EXISTING");
			//System.err.flush();
			
			ByteBuffer temp = getSelectorThread().getPooledBuffer();
			temp.put(buf);
			
			int remaining = temp.remaining();
			_primaryWriteBuffer.flip();
			int limit = _primaryWriteBuffer.limit();
			
			if (remaining >= _primaryWriteBuffer.remaining())
			{
				temp.put(_primaryWriteBuffer);
				getSelectorThread().recycleBuffer(_primaryWriteBuffer);
				_primaryWriteBuffer = temp;
			}
			else
			{
				_primaryWriteBuffer.limit(remaining);
				temp.put(_primaryWriteBuffer);
				_primaryWriteBuffer.limit(limit);
				_primaryWriteBuffer.compact();
				_secondaryWriteBuffer = _primaryWriteBuffer;
				_primaryWriteBuffer = temp;
			}
		}
	}
	
	/*
	void appendIntoWriteBuffer(ByteBuffer buf)
	{
	    // if we already have a buffer
	    if (_secondaryWriteBuffer != null && (_primaryWriteBuffer != null && !_primaryWriteBuffer.hasRemaining()))
	    {
	        _secondaryWriteBuffer.put(buf);
	        
	        if (MMOCore.ASSERTIONS_ENABLED)
	        {
	            // correct state
	            assert _primaryWriteBuffer == null || !_primaryWriteBuffer.hasRemaining();
	            // full write
	            assert !buf.hasRemaining();
	        }
	    }
	    else if (_primaryWriteBuffer != null)
	    {
	        int size = Math.min(buf.limit(), _primaryWriteBuffer.remaining());
	        _primaryWriteBuffer.put(buf.array(), buf.position(), size);
	        buf.position(buf.position() + size);
	        
	        // primary wasnt enough
	        if (buf.hasRemaining())
	        {
	            _secondaryWriteBuffer = getSelectorThread().getPooledBuffer();
	            _secondaryWriteBuffer.put(buf);
	        }
	        
	        if (MMOCore.ASSERTIONS_ENABLED)
	        {
	            // full write
	            assert !buf.hasRemaining();
	        }
	    }
	    else
	    {
	        // a single empty buffer should be always enough by design
	        _primaryWriteBuffer = getSelectorThread().getPooledBuffer();
	        _primaryWriteBuffer.put(buf);
	        System.err.println("ESCREVI "+_primaryWriteBuffer.position());
	        if (MMOCore.ASSERTIONS_ENABLED)
	        {
	            // full write
	            assert !buf.hasRemaining();
	        }
	    }
	}*/

	/*protected void prependIntoPendingWriteBuffer(ByteBuffer buf)
	{
	    int remaining = buf.remaining();
	    
	    //do we already have some buffer
	    if (_primaryWriteBuffer != null && _primaryWriteBuffer.hasRemaining())
	    {
	        if (remaining == _primaryWriteBuffer.capacity())
	        {
	            if (MMOCore.ASSERTIONS_ENABLED)
	            {
	                assert _secondaryWriteBuffer == null;
	            }
	            
	            _secondaryWriteBuffer = _primaryWriteBuffer;
	            _primaryWriteBuffer = getSelectorThread().getPooledBuffer();
	            _primaryWriteBuffer.put(buf);
	        }
	        else if (remaining < _primaryWriteBuffer.remaining())
	        {
	            
	        }
	    }
	    else
	    {
	        
	    }
	}*/

	boolean hasPendingWriteBuffer()
	{
		return _primaryWriteBuffer != null;
	}
	
	void movePendingWriteBufferTo(ByteBuffer dest)
	{
		//System.err.println("PRI SIZE: "+_primaryWriteBuffer.position());
		//System.err.flush();
		_primaryWriteBuffer.flip();
		dest.put(_primaryWriteBuffer);
		getSelectorThread().recycleBuffer(_primaryWriteBuffer);
		_primaryWriteBuffer = _secondaryWriteBuffer;
		_secondaryWriteBuffer = null;
	}
	
	/*protected void finishPrepending(int written)
	{
	    _primaryWriteBuffer.position(Math.min(written, _primaryWriteBuffer.limit()));
	    // discard only the written bytes
	    _primaryWriteBuffer.compact();
	    
	    if (_secondaryWriteBuffer != null)
	    {
	        _secondaryWriteBuffer.flip();
	        _primaryWriteBuffer.put(_secondaryWriteBuffer);
	        
	        if (!_secondaryWriteBuffer.hasRemaining())
	        {
	            getSelectorThread().recycleBuffer(_secondaryWriteBuffer);
	            _secondaryWriteBuffer = null;
	        }
	        else
	        {
	            _secondaryWriteBuffer.compact();
	        }
	    }
	}*/

	void setReadBuffer(ByteBuffer buf)
	{
		_readBuffer = buf;
	}
	
	ByteBuffer getReadBuffer()
	{
		return _readBuffer;
	}
	
	boolean isClosed()
	{
		return _timeClosed != -1;
	}
	
	boolean closeTimeouted()
	{
		return System.currentTimeMillis() > _timeClosed + 10000;
	}
	
	public synchronized void closeNow()
	{
		if (isClosed())
			return;
		
		_timeClosed = System.currentTimeMillis();
		getSendQueue2().clear();
		disableWriteInterest();
		getSelectorThread().closeConnection(this);
	}
	
	public synchronized void close(SendablePacket<T> sp)
	{
		if (isClosed())
			return;
		
		getSendQueue2().clear();
		sendPacket(sp);
		_timeClosed = System.currentTimeMillis();
		getSelectorThread().closeConnection(this);
	}
	
	void releaseBuffers()
	{
		if (_primaryWriteBuffer != null)
		{
			getSelectorThread().recycleBuffer(_primaryWriteBuffer);
			_primaryWriteBuffer = null;
			if (_secondaryWriteBuffer != null)
			{
				getSelectorThread().recycleBuffer(_secondaryWriteBuffer);
				_secondaryWriteBuffer = null;
			}
		}
		
		if (_readBuffer != null)
		{
			getSelectorThread().recycleBuffer(_readBuffer);
			_readBuffer = null;
		}
	}
	
	protected abstract void onDisconnection();
	
	protected abstract void onForcedDisconnection();
	
	protected abstract boolean decrypt(ByteBuffer buf, int size);
	
	protected abstract boolean encrypt(ByteBuffer buf, int size);
}
