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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javolution.util.FastList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author KenM<BR>
 *         Parts of design based on networkcore from WoodenGil
 */
public abstract class SelectorThread<T extends MMOConnection<T, RP, SP>, RP extends ReceivablePacket<T, RP, SP>, SP extends SendablePacket<T, RP, SP>>
	extends Thread
{
	protected static final Log _log = LogFactory.getLog(SelectorThread.class);
	
	private final Selector _selector;
	
	// Implementations
	private final IPacketHandler<T, RP, SP> _packetHandler;
	
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
	
	protected SelectorThread(SelectorConfig sc, IPacketHandler<T, RP, SP> packetHandler) throws IOException
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
		_packetHandler = packetHandler;
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
	
	public final void openServerSocket(String address, int port) throws IOException
	{
		openServerSocket(InetAddress.getByName(address), port);
	}
	
	public final void openServerSocket(InetAddress address, int port) throws IOException
	{
		ServerSocketChannel selectable = ServerSocketChannel.open();
		selectable.configureBlocking(false);
		
		ServerSocket ss = selectable.socket();
		if (address == null)
		{
			ss.bind(new InetSocketAddress(port));
		}
		else
		{
			ss.bind(new InetSocketAddress(address, port));
		}
		selectable.register(getSelector(), SelectionKey.OP_ACCEPT);
	}
	
	final ByteBuffer getPooledBuffer()
	{
		if (getFreeBuffers().isEmpty())
			return ByteBuffer.allocate(BUFFER_SIZE).order(BYTE_ORDER);
		else
			return getFreeBuffers().removeFirst();
	}
	
	final void recycleBuffer(ByteBuffer buf)
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
	public final void run()
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
								writePacket(key);
								break;
							case SelectionKey.OP_READ | SelectionKey.OP_WRITE:
								writePacket(key);
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
			return;
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
				if (acceptConnectionFrom(sc))
				{
					sc.configureBlocking(false);
					
					SelectionKey clientKey = sc.register(getSelector(), SelectionKey.OP_READ);
					
					clientKey.attach(createClient(sc.socket(), clientKey));
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
		
		for (;;)
		{
			final int remainingFreeSpace = buf.remaining();
			int result = -2;
			
			try
			{
				result = con.getReadableByteChannel().read(buf);
			}
			catch (IOException e)
			{
				//error handling goes bellow
			}
			
			boolean hasReachedAConfigLimit = false;
			
			switch (result)
			{
				case -2: // IOException
				{
					closeConnectionImpl(con, true);
					return;
				}
				case -1: // EOS
				{
					closeConnectionImpl(con, false);
					return;
				}
				default:
				{
					buf.flip();
					// try to read as many packets as possible
					for (int i = 0;; i++)
					{
						if (i >= MAX_READ_PER_PASS || buf.position() >= MAX_READ_BYTE_PER_PASS)
						{
							hasReachedAConfigLimit = true;
							break;
						}
						
						if (!tryReadPacket2(con, buf))
							break;
					}
					break;
				}
			}
			
			// stop reading, if we have reached a config limit
			if (hasReachedAConfigLimit)
				break;
			
			// if the buffer wasn't filled completely, we should stop trying as the input channel is empty
			if (remainingFreeSpace > result)
				break;
			
			// compact the buffer for reusing the remaining bytes
			if (buf.hasRemaining())
				buf.compact();
			else
				buf.clear();
		}
		
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
			
			final int opcode = buf.get() & 0xFF;
			
			if (canReceivePacketFrom(client, opcode))
			{
				RP cp = getPacketHandler().handlePacket(buf, client, opcode);
				
				if (cp != null)
				{
					cp.setByteBuffer(buf);
					cp.setClient(client);
					
					try
					{
						if (cp.getAvaliableBytes() < cp.getMinimumLength())
						{
							report(ErrorMode.BUFFER_UNDER_FLOW, client, cp, null);
						}
						else if (cp.read())
						{
							executePacket(cp);
							
							if (buf.hasRemaining())
							{
								// disabled until packet structures updated properly
								//report(ErrorMode.BUFFER_OVER_FLOW, client, cp, null);
								
								_log.info("Invalid packet format (buf: " + buf + ", dataSize: " + dataSize + ", pos: "
									+ pos + ", limit: " + limit + ", opcode: " + opcode + ") used for reading - "
									+ client + " - " + cp.getType() + " - " + getVersionInfo());
							}
						}
					}
					catch (BufferUnderflowException e)
					{
						report(ErrorMode.BUFFER_UNDER_FLOW, client, cp, e);
					}
					catch (RuntimeException e)
					{
						report(ErrorMode.FAILED_READING, client, cp, e);
					}
					
					cp.setByteBuffer(null);
				}
			}
			
			buf.limit(limit);
		}
	}
	
	private void writePacket(SelectionKey key)
	{
		@SuppressWarnings("unchecked")
		T con = (T)key.attachment();
		
		for (;;)
		{
			final boolean hasReachedAConfigLimit = prepareWriteBuffer2(con);
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
							return;
						}
						else if (hasReachedAConfigLimit)
							return;
					}
				}
				else
				// incomplete write
				{
					con.createWriteBuffer(DIRECT_WRITE_BUFFER);
					return;
				}
			}
			else
			{
				closeConnectionImpl(con, true);
				return;
			}
		}
	}
	
	private boolean prepareWriteBuffer2(T con)
	{
		boolean hasReachedAConfigLimit = false;
		
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
			for (int i = 0; DIRECT_WRITE_BUFFER.remaining() >= 2; i++)
			{
				if (i >= MAX_SEND_PER_PASS || DIRECT_WRITE_BUFFER.position() >= MAX_SEND_BYTE_PER_PASS)
				{
					hasReachedAConfigLimit = true;
					break;
				}
				
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
		
		return hasReachedAConfigLimit;
	}
	
	private void putPacketIntoWriteBuffer(T client, SP sp)
	{
		WRITE_BUFFER.clear();
		
		// set the write buffer
		sp.setByteBuffer(WRITE_BUFFER);
		
		// reserve space for the size
		WRITE_BUFFER.position(HEADER_SIZE);
		
		// write content to buffer
		try
		{
			sp.write(client);
		}
		catch (RuntimeException e)
		{
			_log.fatal("Failed writing: " + client + " - " + sp.getType() + " - " + getVersionInfo(), e);
		}
		
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
	
	private IPacketHandler<T, RP, SP> getPacketHandler()
	{
		return _packetHandler;
	}
	
	final void closeConnection(T con)
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
	
	public final void shutdown() throws InterruptedException
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
	
	// ==============================================
	
	protected abstract T createClient(Socket socket, SelectionKey key);
	
	protected abstract void executePacket(RP packet);
	
	// ==============================================
	
	public static enum ErrorMode
	{
		INVALID_OPCODE,
		BUFFER_UNDER_FLOW,
		BUFFER_OVER_FLOW,
		FAILED_READING,
		FAILED_RUNNING;
	}
	
	private static enum Result
	{
		ACCEPTED,
		WARNED,
		REJECTED;
		
		public static Result max(Result r1, Result r2)
		{
			if (r1.ordinal() > r2.ordinal())
				return r1;
			
			return r2;
		}
	}
	
	private final FloodManager _accepts;
	private final FloodManager _packets;
	private final FloodManager _errors;
	
	{
		// TODO: fine tune
		_accepts = new FloodManager(1000, 60);
		_accepts.addFloodFilter(10, 20, 10);
		_accepts.addFloodFilter(30, 60, 60);
		
		_packets = new FloodManager(1000, 10);
		_packets.addFloodFilter(250, 300, 2);
		
		_errors = new FloodManager(200, 10);
		_errors.addFloodFilter(10, 10, 1);
	}
	
	protected String getVersionInfo()
	{
		return "";
	}
	
	public boolean acceptConnectionFrom(SocketChannel sc)
	{
		final String host = sc.socket().getInetAddress().getHostAddress();
		
		final Result isFlooding = _accepts.isFlooding(host, true);
		
		switch (isFlooding)
		{
			case REJECTED:
			{
				// TODO: punish, warn, log, etc
				_log.warn("Rejected connection from " + host);
				return false;
			}
			case WARNED:
			{
				// TODO: punish, warn, log, etc
				_log.warn("Connection over warn limit from " + host);
				return true;
			}
			default:
				return true;
		}
	}
	
	public void report(ErrorMode mode, MMOConnection<?, ?, ?> client, ReceivablePacket<?, ?, ?> packet,
		Throwable throwable)
	{
		final Result isFlooding = _errors.isFlooding(client.getUID(), true);
		
		final StringBuilder sb = new StringBuilder();
		if (isFlooding != Result.ACCEPTED)
		{
			sb.append("Flooding with ");
		}
		sb.append(mode);
		sb.append(": ");
		sb.append(client);
		if (packet != null)
		{
			sb.append(" - ");
			sb.append(packet.getType());
		}
		final String versionInfo = getVersionInfo();
		if (versionInfo != null && !versionInfo.isEmpty())
		{
			sb.append(" - ");
			sb.append(versionInfo);
		}
		
		if (throwable != null)
			_log.info(sb, throwable);
		else
			_log.info(sb);
		
		//if (isFlooding != Result.ACCEPTED)
		//{
		//	// TODO: punish, warn, log, etc
		//}
	}
	
	public boolean canReceivePacketFrom(MMOConnection<?, ?, ?> client, int opcode)
	{
		final String key = client.getUID();
		
		switch (Result.max(_packets.isFlooding(key, true), _errors.isFlooding(key, false)))
		{
			case REJECTED:
			{
				// TODO: punish, warn, log, etc
				_log.warn("Rejected packet (0x" + Integer.toHexString(opcode) + ") from " + client);
				return false;
			}
			case WARNED:
			{
				// TODO: punish, warn, log, etc
				_log.warn("Packet over warn limit (0x" + Integer.toHexString(opcode) + ") from " + client);
				return true;
			}
			default:
				return true;
		}
	}
	
	private static final class FloodManager
	{
		private static final long ZERO = System.currentTimeMillis();
		
		private final Map<String, LogEntry> _entries = new HashMap<String, LogEntry>();
		
		private final ReentrantLock _lock = new ReentrantLock();
		
		private final int _tickLength;
		private final int _tickAmount;
		
		private FloodFilter[] _filters = new FloodFilter[0];
		
		private FloodManager(int msecPerTick, int tickAmount)
		{
			_tickLength = msecPerTick;
			_tickAmount = tickAmount;
		}
		
		private void addFloodFilter(int warnLimit, int rejectLimit, int tickLimit)
		{
			_filters = Arrays.copyOf(_filters, _filters.length + 1);
			_filters[_filters.length - 1] = new FloodFilter(warnLimit, rejectLimit, tickLimit);
		}
		
		private static final class FloodFilter
		{
			private final int _warnLimit;
			private final int _rejectLimit;
			private final int _tickLimit;
			
			private FloodFilter(int warnLimit, int rejectLimit, int tickLimit)
			{
				_warnLimit = warnLimit;
				_rejectLimit = rejectLimit;
				_tickLimit = tickLimit;
			}
			
			public int getWarnLimit()
			{
				return _warnLimit;
			}
			
			public int getRejectLimit()
			{
				return _rejectLimit;
			}
			
			public int getTickLimit()
			{
				return _tickLimit;
			}
		}
		
		public Result isFlooding(String key, boolean increment)
		{
			if (key == null || key.isEmpty())
				return Result.ACCEPTED;
			
			_lock.lock();
			try
			{
				LogEntry entry = _entries.get(key);
				
				if (entry == null)
				{
					entry = new LogEntry();
					
					_entries.put(key, entry);
				}
				
				return entry.isFlooding(increment);
			}
			finally
			{
				_lock.unlock();
			}
		}
		
		private final class LogEntry
		{
			private final short[] _ticks = new short[_tickAmount];
			
			private int _lastTick = getCurrentTick();
			
			public int getCurrentTick()
			{
				return (int)((System.currentTimeMillis() - ZERO) / _tickLength);
			}
			
			public Result isFlooding(boolean increment)
			{
				final int currentTick = getCurrentTick();
				
				if (currentTick - _lastTick >= _ticks.length)
				{
					_lastTick = currentTick;
					
					Arrays.fill(_ticks, (short)0);
				}
				else if (_lastTick > currentTick)
				{
					_log.warn("The current tick (" + currentTick + ") is smaller than the last (" + _lastTick + ")!",
						new IllegalStateException());
					
					_lastTick = currentTick;
				}
				else
				{
					while (currentTick != _lastTick)
					{
						_lastTick++;
						
						_ticks[_lastTick % _ticks.length] = 0;
					}
				}
				
				if (increment)
					_ticks[_lastTick % _ticks.length]++;
				
				for (FloodFilter filter : _filters)
				{
					int previousSum = 0;
					int currentSum = 0;
					
					for (int i = 0; i <= filter.getTickLimit(); i++)
					{
						int value = _ticks[(_lastTick - i) % _ticks.length];
						
						if (i != 0)
							previousSum += value;
						
						if (i != filter.getTickLimit())
							currentSum += value;
					}
					
					if (previousSum > filter.getRejectLimit() || currentSum > filter.getRejectLimit())
						return Result.REJECTED;
					
					if (previousSum > filter.getWarnLimit() || currentSum > filter.getWarnLimit())
						return Result.WARNED;
				}
				
				return Result.ACCEPTED;
			}
		}
	}
}
