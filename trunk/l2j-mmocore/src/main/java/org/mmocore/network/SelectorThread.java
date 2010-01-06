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
	private final int HELPER_BUFFER_SIZE;
	private final int HELPER_BUFFER_COUNT;
	private final int MAX_SEND_PER_PASS;
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
		HELPER_BUFFER_SIZE = sc.getHelperBufferSize();
		HELPER_BUFFER_COUNT = sc.getHelperBufferCount();
		MAX_SEND_PER_PASS = sc.getMaxSendPerPass();
		BYTE_ORDER = sc.getByteOrder();
		SLEEP_TIME = sc.getSelectorSleepTime();
		
		DIRECT_WRITE_BUFFER = ByteBuffer.allocateDirect(sc.getWriteBufferSize()).order(BYTE_ORDER);
		WRITE_BUFFER = ByteBuffer.wrap(new byte[sc.getWriteBufferSize()]).order(BYTE_ORDER);
		READ_BUFFER = ByteBuffer.wrap(new byte[sc.getReadBufferSize()]).order(BYTE_ORDER);
		
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
			getFreeBuffers().addLast(ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER));
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
			return ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER);
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
							case SelectionKey.OP_CONNECT:
								finishConnection(key);
								break;
							case SelectionKey.OP_ACCEPT:
								acceptConnection(key);
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
		
		ByteBuffer buf;
		if ((buf = con.getReadBuffer()) == null)
		{
			buf = READ_BUFFER;
		}
		int result = -2;
		
		// if we try to to do a read with no space in the buffer it will read 0 bytes
		// going into infinite loop
		if (buf.position() == buf.limit())
		{
			// should never happen
			System.err.println("POS ANTES SC.READ(): " + buf.position() + " limit: " + buf.limit());
			System.err.println("NOOBISH ERROR " + (buf == READ_BUFFER ? "READ_BUFFER" : "temp"));
			System.exit(0);
		}
		
		//System.out.println("POS ANTES SC.READ(): "+buf.position()+" limit: "+buf.limit()+" - buf: "+(buf == READ_BUFFER ? "READ_BUFFER" : "TEMP"));
		
		try
		{
			result = con.getReadableByteChannel().read(buf);
		}
		catch (IOException e)
		{
			//error handling goes bellow
		}
		
		//System.out.println("LEU: "+result+" pos: "+buf.position());
		if (result > 0)
		{
			// TODO this should be done vefore even reading
			if (!con.isClosed())
			{
				buf.flip();
				// try to read as many packets as possible
				while (tryReadPacket2(key, con, buf))
				{
					// ...
				}
			}
			else
			{
				if (buf == READ_BUFFER)
				{
					READ_BUFFER.clear();
				}
			}
		}
		else if (result == 0)
		{
			// read interest but nothing to read? wtf?
			System.out.println("NOOBISH ERROR 2 THE MISSION");
			//System.exit(0);
		}
		else if (result == -1)
		{
			closeConnectionImpl(con, false);
		}
		else
		{
			closeConnectionImpl(con, true);
		}
	}
	
	private boolean tryReadPacket2(SelectionKey key, T con, ByteBuffer buf)
	{
		//System.out.println("BUFF POS ANTES DE LER: "+buf.position()+" - REMAINING: "+buf.remaining());
		
		if (buf.hasRemaining())
		{
			// parse all headers
			final int headerPending;
			final int dataPending;
			
			if (buf.remaining() >= 2)
			{
				headerPending = 0;
				dataPending = (buf.getShort() & 0xffff) - 2;
			}
			else
			{
				headerPending = 2 - buf.remaining();
				dataPending = 0;
			}
			
			int result = buf.remaining();
			
			// then check if header was processed
			if (headerPending == 0)
			{
				// get expected packet size
				int size = dataPending;
				
				//System.out.println("IF: ("+size+" <= "+result+") => (size <= result)");
				// do we got enough bytes for the packet?
				if (size <= result)
				{
					// avoid parsing dummy packets (packets without body)
					if (size > 0)
					{
						int pos = buf.position();
						parseClientPacket(buf, size, con);
						buf.position(pos + size);
					}
					
					// if we are done with this buffer
					if (!buf.hasRemaining())
					{
						//System.out.println("BOA 2");
						if (buf != READ_BUFFER)
						{
							con.setReadBuffer(null);
							recycleBuffer(buf);
						}
						else
						{
							READ_BUFFER.clear();
						}
						
						return false;
					}
					else
					{
						// nothing
					}
					
					return true;
				}
				else
				{
					// we dont have enough bytes for the dataPacket so we need to read
					con.enableReadInterest();
					
					//System.out.println("LIMIT "+buf.limit());
					if (buf == READ_BUFFER)
					{
						buf.position(buf.position() - HEADER_SIZE);
						allocateReadBuffer(con);
					}
					else
					{
						buf.position(buf.position() - HEADER_SIZE);
						buf.compact();
					}
					return false;
				}
			}
			else
			{
				// we dont have enough data for header so we need to read
				con.enableReadInterest();
				
				if (buf == READ_BUFFER)
				{
					allocateReadBuffer(con);
				}
				else
				{
					buf.compact();
				}
				return false;
			}
		}
		else
			//con.disableReadInterest();
			return false; //empty buffer
	}
	
	private void allocateReadBuffer(T con)
	{
		//System.out.println("con: "+Integer.toHexString(con.hashCode()));
		//Util.printHexDump(READ_BUFFER);
		con.setReadBuffer(getPooledBuffer().put(READ_BUFFER));
		READ_BUFFER.clear();
	}
	
	private void parseClientPacket(ByteBuffer buf, int dataSize, T client)
	{
		int pos = buf.position();
		
		boolean ret = client.decrypt(buf, dataSize);
		
		//buf.position(pos); //can be annoying for some decrypt impl decrypt should place the pos at the right place itself
		
		//System.out.println("pCP -> BUF: POS: "+buf.position()+" - LIMIT: "+buf.limit()+" == Packet: SIZE: "+dataSize);
		
		if (buf.hasRemaining() && ret)
		{
			//  apply limit
			int limit = buf.limit();
			buf.limit(pos + dataSize);
			//System.out.println("pCP2 -> BUF: POS: "+buf.position()+" - LIMIT: "+buf.limit()+" == Packet: SIZE: "+size);
			RP cp = getPacketHandler().handlePacket(buf, client);
			
			if (cp != null)
			{
				cp.setByteBuffer(buf);
				cp.setClient(client);
				
				if (cp.read())
				{
					getExecutor().execute(cp);
				}
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
		
		//System.err.println("WRITE SIZE: "+size);
		int result = -1;
		
		try
		{
			result = con.getWritableChannel().write(DIRECT_WRITE_BUFFER);
		}
		catch (IOException e)
		{
			// error handling goes on the if bellow
			//System.err.println("IOError: " + e.getMessage());
		}
		
		// check if no error happened
		if (result >= 0)
		{
			// check if we writed everything
			if (result == size)
			{
				// complete write
				//System.err.println("FULL WRITE");
				//System.err.flush();
				
				// if there was a a pending write then we need to finish the operation
				/*if (con.getWriterMark() > 0)
				{
				    con.finishPrepending(con.getWriterMark());
				}*/

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
			//incomplete write
			{
				con.createWriteBuffer(DIRECT_WRITE_BUFFER);
				return false;
				//System.err.println("DEBUG: INCOMPLETE WRITE - write size: "+size);
				//System.err.flush();
			}
			
			//if (result == 0)
			//{
			//System.err.println("DEBUG: write result: 0 - write size: "+size+" - DWB rem: "+DIRECT_WRITE_BUFFER.remaining());
			//System.err.flush();
			//}
		}
		else
		{
			//System.err.println("IOError: "+result);
			//System.err.flush();
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
			//System.err.println("ADDED PENDING TO DIRECT "+DIRECT_WRITE_BUFFER.position());
		}
		
		if (DIRECT_WRITE_BUFFER.remaining() > 1 && !con.hasPendingWriteBuffer())
		{
			for (int i = 0; i < MAX_SEND_PER_PASS; i++)
			{
				SP sp = null;
				
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
				//System.err.println("WB SIZE: "+WRITE_BUFFER.limit());
				if (DIRECT_WRITE_BUFFER.remaining() >= WRITE_BUFFER.limit())
				{
					/*if (i == 0)
					{
					    // mark begining of new data from previous pending data
					    con.setWriterMark(DIRECT_WRITE_BUFFER.position());
					}*/
					DIRECT_WRITE_BUFFER.put(WRITE_BUFFER);
				}
				else
				{
					// there is no more space in the direct buffer
					//con.addWriteBuffer(getPooledBuffer().put(WRITE_BUFFER));
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
		int headerPos = sp.getByteBuffer().position();
		sp.getByteBuffer().position(headerPos + HEADER_SIZE);
		
		// write content to buffer
		sp.write(client);
		
		// size (incl header)
		int dataSize = sp.getByteBuffer().position() - headerPos - HEADER_SIZE;
		sp.getByteBuffer().position(headerPos + HEADER_SIZE);
		client.encrypt(sp.getByteBuffer(), dataSize);
		
		// recalculate size after encryption
		dataSize = sp.getByteBuffer().position() - headerPos - HEADER_SIZE;
		
		// prepend header
		//prependHeader(headerPos, size);
		sp.getByteBuffer().position(headerPos);
		sp.writeH(HEADER_SIZE + dataSize);
		sp.getByteBuffer().position(headerPos + HEADER_SIZE + dataSize);
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
