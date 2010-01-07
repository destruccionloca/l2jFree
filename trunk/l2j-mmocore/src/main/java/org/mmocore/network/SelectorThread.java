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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import javolution.util.FastList;

/**
 * @author KenM<BR>
 *         Parts of design based on networkcore from WoodenGil
 */
public final class SelectorThread<T extends MMOConnection<T, RP, SP>, RP extends ReceivablePacket<T, RP, SP>, SP extends SendablePacket<T, RP, SP>>
	extends Thread
{
	private final Selector _selector;
	
	// Implementations
	private final IPacketHandler<T, RP, SP> _packetHandler;
	private final IMMOExecutor<T, RP, SP> _executor;
	private final IClientFactory<T, RP, SP> _clientFactory;
	private final IAcceptFilter _acceptFilter;
	
	private volatile boolean _shutdown;
	
	// Pending Close
	private final FastList<T> _pendingClose = new FastList<T>();
	
	// Configs
	private final int BUFFER_SIZE;
	private final int HELPER_BUFFER_COUNT;
	private final int MAX_SEND_PER_PASS;
	private final int MAX_READ_PER_PASS;
	private final int MAX_SEND_BYTE_PER_PASS;
	private final int MAX_READ_BYTE_PER_PASS;
	private final int HEADER_SIZE = 2;
	private final ByteOrder BYTE_ORDER;
	private final long SLEEP_TIME;
	
	// MAIN BUFFERS
	private final ByteBuffer DIRECT_WRITE_BUFFER;
	private final ByteBuffer WRITE_BUFFER;
	private final ByteBuffer READ_BUFFER;
	
	// ByteBuffers General Purpose Pool
	private final FastList<ByteBuffer> _bufferPool = new FastList<ByteBuffer>();
	
	public SelectorThread(SelectorConfig<T, RP, SP> sc) throws IOException
	{
		BUFFER_SIZE = sc.getBufferSize();
		HELPER_BUFFER_COUNT = sc.getHelperBufferCount();
		MAX_SEND_PER_PASS = sc.getMaxSendPerPass();
		MAX_READ_PER_PASS = sc.getMaxReadPerPass();
		MAX_SEND_BYTE_PER_PASS = sc.getMaxSendBytePerPass();
		MAX_READ_BYTE_PER_PASS = sc.getMaxReadBytePerPass();
		BYTE_ORDER = sc.getByteOrder();
		SLEEP_TIME = sc.getSelectorSleepTime();
		
		DIRECT_WRITE_BUFFER = ByteBuffer.allocateDirect(BUFFER_SIZE).order(BYTE_ORDER);
		WRITE_BUFFER = ByteBuffer.allocate(BUFFER_SIZE).order(BYTE_ORDER);
		READ_BUFFER = ByteBuffer.allocate(BUFFER_SIZE).order(BYTE_ORDER);
		
		initBufferPool();
		_acceptFilter = sc.getAcceptFilter();
		_packetHandler = sc.getPacketHandler();
		_clientFactory = sc.getClientFactory();
		_executor = sc.getExecutor();
		setName("SelectorThread-" + getId());
		_selector = Selector.open();
	}
	
	private void initBufferPool()
	{
		for (int i = 0; i < HELPER_BUFFER_COUNT; i++)
		{
			getFreeBuffers().addLast(ByteBuffer.allocate(BUFFER_SIZE).order(BYTE_ORDER));
		}
	}
	
	public void openServerSocket(InetAddress address, int tcpPort) throws IOException
	{
		ServerSocketChannel selectable = ServerSocketChannel.open();
		selectable.configureBlocking(false);
		
		ServerSocket ss = selectable.socket();
		if (address == null)
		{
			ss.bind(new InetSocketAddress(tcpPort));
		}
		else
		{
			ss.bind(new InetSocketAddress(address, tcpPort));
		}
		selectable.register(getSelector(), SelectionKey.OP_ACCEPT);
	}
	
	ByteBuffer getPooledBuffer()
	{
		if (getFreeBuffers().isEmpty())
			return ByteBuffer.allocate(BUFFER_SIZE).order(BYTE_ORDER);
		else
			return getFreeBuffers().removeFirst();
	}
	
	void recycleBuffer(ByteBuffer buf)
	{
		if (getFreeBuffers().size() < HELPER_BUFFER_COUNT)
		{
			buf.clear();
			getFreeBuffers().addLast(buf);
		}
	}
	
	private FastList<ByteBuffer> getFreeBuffers()
	{
		return _bufferPool;
	}
	
	@Override
	public void run()
	{
		// main loop
		for (;;)
		{
			// check for shutdown
			if (isShuttingDown())
			{
				close();
				break;
			}
			
			boolean hasPendingWrite = false;
			
			try
			{
				if (getSelector().selectNow() > 0)
				{
					Set<SelectionKey> keys = getSelector().selectedKeys();
					
					for (SelectionKey key : keys)
					{
						switch (key.readyOps())
						{
							case SelectionKey.OP_ACCEPT:
								acceptConnection(key);
								break;
							case SelectionKey.OP_CONNECT:
								finishConnection(key);
								break;
							case SelectionKey.OP_READ:
								readPacket(key);
								break;
							case SelectionKey.OP_WRITE:
								hasPendingWrite |= writePacket2(key);
								break;
							case SelectionKey.OP_READ | SelectionKey.OP_WRITE:
								hasPendingWrite |= writePacket2(key);
								// key might have been invalidated on writePacket
								if (key.isValid())
									readPacket(key);
								break;
						}
					}
					
					keys.clear();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			closePendingConnections();
			
			try
			{
				if (!hasPendingWrite)
					Thread.sleep(SLEEP_TIME);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
			closePendingConnections();
		}
	}
	
	private void closePendingConnections()
	{
		// process pending close
		synchronized (getPendingClose())
		{
			for (FastList.Node<T> n = getPendingClose().head(), end = getPendingClose().tail(); (n = n.getNext()) != end;)
			{
				final T con = n.getValue();
				
				synchronized (con)
				{
					if (con.getSendQueue2().isEmpty() && !con.hasPendingWriteBuffer() || con.closeTimeouted())
					{
						FastList.Node<T> temp = n.getPrevious();
						getPendingClose().delete(n);
						n = temp;
						closeConnectionImpl(con, false);
					}
				}
			}
		}
	}
	
	private void finishConnection(SelectionKey key)
	{
		try
		{
			((SocketChannel)key.channel()).finishConnect();
		}
		catch (IOException e)
		{
			@SuppressWarnings("unchecked")
			T con = (T)key.attachment();
			closeConnectionImpl(con, true);
		}
		
		// key might have been invalidated on finishConnect()
		if (key.isValid())
		{
			key.interestOps(key.interestOps() | SelectionKey.OP_READ);
			key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
		}
	}
	
	private void acceptConnection(SelectionKey key)
	{
		SocketChannel sc;
		try
		{
			while ((sc = ((ServerSocketChannel)key.channel()).accept()) != null)
			{
				if (getAcceptFilter() == null || getAcceptFilter().accept(sc))
				{
					sc.configureBlocking(false);
					
					SelectionKey clientKey = sc.register(getSelector(), SelectionKey.OP_READ);
					
					clientKey.attach(getClientFactory().create(this, sc.socket(), clientKey));
				}
				else
				{
					sc.socket().close();
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void readPacket(SelectionKey key)
	{
		@SuppressWarnings("unchecked")
		T con = (T)key.attachment();
		
		ByteBuffer buf = con.getReadBuffer();
		
		if (buf == null)
		{
			buf = READ_BUFFER;
			buf.clear();
		}
		
		int result = -2;
		
		try
		{
			result = con.getReadableByteChannel().read(buf);
		}
		catch (IOException e)
		{
			//error handling goes bellow
		}
		
		switch (result)
		{
			case -2: // IOException
			{
				closeConnectionImpl(con, true);
				break;
			}
			case -1: // EOS
			{
				closeConnectionImpl(con, false);
				break;
			}
			default:
			{
				buf.flip();
				// try to read as many packets as possible
				for (int i = 0; i < MAX_READ_PER_PASS && buf.position() < MAX_READ_BYTE_PER_PASS; i++)
				{
					if (!tryReadPacket2(con, buf))
						break;
				}
				break;
			}
		}
		
		if (!key.isValid())
			return;
		
		// check if there are some more bytes in buffer and allocate/compact to prevent content lose.
		if (buf.hasRemaining())
		{
			if (buf == READ_BUFFER)
			{
				con.setReadBuffer(getPooledBuffer().put(READ_BUFFER));
			}
			else
			{
				buf.compact();
			}
		}
		else
		{
			if (buf == READ_BUFFER)
			{
			}
			else
			{
				con.setReadBuffer(null);
				recycleBuffer(buf);
			}
		}
	}
	
	private boolean tryReadPacket2(T con, ByteBuffer buf)
	{
		// check if header could be processed
		if (buf.remaining() >= 2)
		{
			// parse all headers and get expected packet size
			final int size = (buf.getShort() & 0xFFFF) - HEADER_SIZE;
			
			// do we got enough bytes for the packet?
			if (size <= buf.remaining())
			{
				// avoid parsing dummy packets (packets without body)
				if (size > 0)
				{
					int pos = buf.position();
					parseClientPacket(buf, size, con);
					buf.position(pos + size);
				}
				
				return true;
			}
			else if (size > BUFFER_SIZE - HEADER_SIZE)
			{
				// that packet simly won't fit in our buffer and will result in a never succeeding packet parsing
				// so close the connection as this is an invalid packet (probably an offensive attack)
				closeConnectionImpl(con, false);
				return false;
			}
			else
			{
				// we dont have enough bytes for the packet so we need to read and revert the header
				buf.position(buf.position() - HEADER_SIZE);
				return false;
			}
		}
		else
		{
			// we dont have enough data for header so we need to read
			return false;
		}
	}
	
	private void parseClientPacket(ByteBuffer buf, int dataSize, T client)
	{
		int pos = buf.position();
		
		if (client.decrypt(buf, dataSize) && buf.hasRemaining())
		{
			// apply limit
			int limit = buf.limit();
			buf.limit(pos + dataSize);
			
			RP cp = getPacketHandler().handlePacket(buf, client);
			
			if (cp != null)
			{
				cp.setByteBuffer(buf);
				cp.setClient(client);
				
				if (cp.read())
				{
					getExecutor().execute(cp);
				}
				
				cp.setByteBuffer(null);
			}
			
			buf.limit(limit);
		}
	}
	
	private boolean writePacket2(SelectionKey key)
	{
		@SuppressWarnings("unchecked")
		T con = (T)key.attachment();
		
		prepareWriteBuffer2(con);
		DIRECT_WRITE_BUFFER.flip();
		
		int size = DIRECT_WRITE_BUFFER.remaining();
		
		int result = -1;
		
		try
		{
			result = con.getWritableChannel().write(DIRECT_WRITE_BUFFER);
		}
		catch (IOException e)
		{
			// error handling goes on the if bellow
		}
		
		// check if no error happened
		if (result >= 0)
		{
			// check if we wrote everything
			if (result == size)
			{
				// complete write
				synchronized (con)
				{
					if (con.getSendQueue2().isEmpty() && !con.hasPendingWriteBuffer())
					{
						con.disableWriteInterest();
						return false;
					}
					else
						return true;
				}
			}
			else
			// incomplete write
			{
				con.createWriteBuffer(DIRECT_WRITE_BUFFER);
				return false;
			}
		}
		else
		{
			closeConnectionImpl(con, true);
			return false;
		}
	}
	
	private void prepareWriteBuffer2(T con)
	{
		DIRECT_WRITE_BUFFER.clear();
		
		// if theres pending content add it
		if (con.hasPendingWriteBuffer())
		{
			con.movePendingWriteBufferTo(DIRECT_WRITE_BUFFER);
			// ADDED PENDING TO DIRECT
		}
		
		// don't write additional, if there are still pending content
		if (!con.hasPendingWriteBuffer())
		{
			for (int i = 0; DIRECT_WRITE_BUFFER.remaining() >= 2 && i < MAX_SEND_PER_PASS
				&& DIRECT_WRITE_BUFFER.position() < MAX_SEND_BYTE_PER_PASS; i++)
			{
				final SP sp;
				
				synchronized (con)
				{
					final FastList<SP> sendQueue = con.getSendQueue2();
					
					if (sendQueue.isEmpty())
						break;
					
					sp = sendQueue.removeFirst();
				}
				
				// put into WriteBuffer
				putPacketIntoWriteBuffer(con, sp);
				WRITE_BUFFER.flip();
				
				if (DIRECT_WRITE_BUFFER.remaining() >= WRITE_BUFFER.limit())
				{
					// put last written packet to the direct buffer
					DIRECT_WRITE_BUFFER.put(WRITE_BUFFER);
				}
				else
				{
					// there isn't enough space in the direct buffer
					con.createWriteBuffer(WRITE_BUFFER);
					break;
				}
			}
		}
	}
	
	private void putPacketIntoWriteBuffer(T client, SP sp)
	{
		WRITE_BUFFER.clear();
		
		// set the write buffer
		sp.setByteBuffer(WRITE_BUFFER);
		
		// reserve space for the size
		WRITE_BUFFER.position(HEADER_SIZE);
		
		// write content to buffer
		sp.write(client);
		
		// calculate size and encrypt content
		int dataSize = WRITE_BUFFER.position() - HEADER_SIZE;
		WRITE_BUFFER.position(HEADER_SIZE);
		client.encrypt(WRITE_BUFFER, dataSize);
		
		// recalculate size after encryption
		dataSize = WRITE_BUFFER.position() - HEADER_SIZE;
		
		// prepend header
		WRITE_BUFFER.position(0);
		WRITE_BUFFER.putShort((short)(HEADER_SIZE + dataSize));
		
		WRITE_BUFFER.position(HEADER_SIZE + dataSize);
		
		// set the write buffer
		sp.setByteBuffer(null);
	}
	
	private Selector getSelector()
	{
		return _selector;
	}
	
	private IMMOExecutor<T, RP, SP> getExecutor()
	{
		return _executor;
	}
	
	private IPacketHandler<T, RP, SP> getPacketHandler()
	{
		return _packetHandler;
	}
	
	private IClientFactory<T, RP, SP> getClientFactory()
	{
		return _clientFactory;
	}
	
	private IAcceptFilter getAcceptFilter()
	{
		return _acceptFilter;
	}
	
	void closeConnection(T con)
	{
		synchronized (getPendingClose())
		{
			getPendingClose().addLast(con);
		}
	}
	
	private void closeConnectionImpl(T con, boolean forced)
	{
		try
		{
			if (forced)
				con.onForcedDisconnection();
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			// notify connection
			con.onDisconnection();
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				// close socket and the SocketChannel
				con.getSocket().close();
			}
			catch (IOException e)
			{
				// ignore, we are closing anyway
			}
			finally
			{
				con.releaseBuffers();
				// clear attachment
				con.getSelectionKey().attach(null);
				// cancel key
				con.getSelectionKey().cancel();
			}
		}
	}
	
	private FastList<T> getPendingClose()
	{
		return _pendingClose;
	}
	
	public void shutdown() throws InterruptedException
	{
		_shutdown = true;
		
		join();
	}
	
	private boolean isShuttingDown()
	{
		return _shutdown;
	}
	
	private void close()
	{
		for (SelectionKey key : getSelector().keys())
		{
			try
			{
				key.channel().close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}
		
		try
		{
			getSelector().close();
		}
		catch (IOException e)
		{
			// Ignore
		}
	}
}
