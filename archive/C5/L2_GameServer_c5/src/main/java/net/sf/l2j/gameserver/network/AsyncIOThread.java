package net.sf.l2j.gameserver.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.io.async.AsyncException;
import com.ibm.io.async.AsyncServerSocketChannel;
import com.ibm.io.async.AsyncSocketChannel;
import com.ibm.io.async.IAbstractAsyncFuture;
import com.ibm.io.async.IAsyncFuture;
import com.ibm.io.async.ICompletionListener;

/**
 * @author -Nemesiss-
 * 
 */

public class AsyncIOThread extends IOThread implements ICompletionListener
{
    private final static Log _log = LogFactory.getLog(AsyncIOThread.class.getName());
    private static AsyncIOThread _instance;
    private static final int Read_State  = 0;
    private static final int Write_State = 1;
    private static int       ToWrite    = 0;
    private final String _host;
    private final int _port;    

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.IOThread#getInstance()
     */
    public static AsyncIOThread getInstance()
    {
        if (_instance == null)
            _instance = new AsyncIOThread(Config.GAMESERVER_HOSTNAME, Config.PORT_GAME);
        return _instance;
    }

    /**
     * @param hostname
     * @param port
     */
    private AsyncIOThread(String hostname, int port)
    {
        super("AIO4J Thread");
        this._host = hostname;
        this._port = port;
        this.setDaemon(true);
        this.setPriority(Thread.NORM_PRIORITY + 2);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        // Create Server Channel
        AsyncServerSocketChannel serverChannel = null;
        try
        {
            serverChannel = AsyncServerSocketChannel.open();

            ServerSocket serverSocket = serverChannel.socket();
            InetSocketAddress endpoint;
            if ("*".equals(_host))
            {
                endpoint = new InetSocketAddress(_port);
                _log.info("IOCP listening on all available IPs on Port " + _port);
            }
            else
            {
                endpoint = new InetSocketAddress(_host, _port);
                _log.info("IOCP listening on IP: " + _host + " Port " + _port);
            }
            // Bind to a listening socket addess.
            serverSocket.bind(endpoint);
        }
        catch (AsyncException ax)
        {
            _log.error(ax);
        }
        catch (IOException iox)
        {
            _log.error(iox);
            return;
        }

        while (true)
        {
            try
            {
                // Listen for client connections.
                AsyncSocketChannel clientChannel = serverChannel.accept();
                _log.debug("Conn from " + clientChannel.socket());
                
                IOCP Iocp = new IOCP(new ClientThread(clientChannel), Read_State);

                ByteBuffer read = ByteBuffer.allocateDirect(8192);
                // When operation will be done the listener will be notify
                IAsyncFuture readFuture = clientChannel.read(read);
                readFuture.addCompletionListener(this, Iocp);
            }
            catch (Exception e)
            {
                _log.error(e);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.ibm.io.async.ICompletionListener#futureCompleted(com.ibm.io.async.IAbstractAsyncFuture, java.lang.Object)
     */   
    public void futureCompleted(IAbstractAsyncFuture fut, Object state)
    {
        IAsyncFuture future = (IAsyncFuture) fut;
        IOCP iocp = (IOCP) state;
        Connection con = iocp._client.getConnection();

        try
        {
            if ((future.isCompleted() && future.getByteCount() == 0 && iocp._state == 0)
                || !iocp._channel.isOpen())
            {
                try
                {
                    con.getClient().getActiveChar().getInventory().updateDatabase();
                    _log.info("Connection close by peer, player "+ iocp._client.getConnection().getClient().getActiveChar().getName()+ " disconnected.");
                }
                catch (NullPointerException npe)
                {
                    _log.info("Connection close by peer, player disconnected (nullpointer, couldn't get player name)");
                }
                future.cancel();
                if (iocp != null) closeClient(iocp);
                return;
            }
        }
        catch (Exception e)
        {
            _log.error (e);
        }

        if (iocp._state == Read_State)
        {
            try
            {
                future.getBuffer().flip();
                future.getBuffer().order(ByteOrder.LITTLE_ENDIAN);

                int sz = future.getBuffer().getShort() & 0xffff;
                sz -= 2;

                if (sz > 0)
                {
                    ByteBuffer b = future.getBuffer();
                    b.order(ByteOrder.LITTLE_ENDIAN);
                    b.position(2);
                    iocp._client.getConnection().decript(b);
                    iocp._client.getConnection().addReceivedMsg(b);
                }
                IAsyncFuture readFuture = iocp._channel.read(future.getBuffer());
                readFuture.addCompletionListener(this, iocp, true);
            }
            catch (Exception e)
            {
                try
                {
                    con.getClient().getActiveChar().getInventory().updateDatabase();
                    _log.info("Error on network read, player "+ iocp._client.getConnection().getClient().getActiveChar().getName()+ " disconnected. " + e);
                }
                catch (NullPointerException npe)
                {
                    _log.info("Error on network read, player disconnected (nullpointer, couldn't get player name)");
                }
                if (iocp != null) closeClient(iocp);
            }
        }
        else if (iocp._state == Write_State)
        {
            try
            {
                //Only to make sure that operation was ended succesful. 
                ToWrite -= future.getByteCount();
            }
            catch (Exception e)
            {
                try
                {
                    con.getClient().getActiveChar().getInventory().updateDatabase();
                    _log.info("Error on network write, player "+ iocp._client.getConnection().getClient().getActiveChar().getName()+ " disconnected. " + e);
                }
                catch (NullPointerException npe)
                {
                    _log.info("Error on network write, player disconnected (nullpointer, couldn't get player name)");
                }
                if (iocp != null) closeClient(iocp);
            }
        }
    }
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.IOThread#sendMessage(net.sf.l2j.gameserver.serverpackets.ServerBasePacket)
     */
    void sendMessage(ServerBasePacket pkt)
    {        
        //ByteBuffer buf = ByteBuffer.allocateDirect(8192);
        ByteBuffer buf = ByteBuffer.allocate(64*1024);
        buf.position(0);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        pkt.write(buf);
        try
        {
            _log.info("Buff "+buf.toString());
            ToWrite += buf.remaining();
            _log.info("ToWrite = "+ToWrite);
            if (pkt.getConnection().getAsyncChannel() == null)
            {
                _log.warn("getAsyncChannel() == null!!");
            }
            IAsyncFuture writeFuture = pkt.getConnection().getAsyncChannel().write(buf);
            // When operation will be done the listener will be notify.
            writeFuture.addCompletionListener(this, new IOCP(pkt.getClient(), Write_State));
        }
        catch (Exception e)
        {
            _log.error(e);
        }
    }
    
    /**
     * @param iocp
     */
    private void closeClient(IOCP iocp)
    {
        Connection c = iocp._client.getConnection();

        if (c.readBuffer != null)        
            c.readBuffer = null;        
        if (c.writeBuffer != null)        
            c.writeBuffer = null;        
        // check if player is fighting
        L2PcInstance player = c.getClient().getActiveChar();
        if (player != null)
        {
            player.setConnected(false);
            if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
            {
                c.onlyClose();
                ThreadPoolManager.getInstance().scheduleGeneral(new DisconnectionTask(iocp), 15000);
                return;
            }
        }
        try
        {
            c.close();
        }
        catch (Exception dummy)
        {
        }
    }

    /**
     * @param iocp
     * @param Forced
     */
    protected void closeClient(IOCP iocp, boolean Forced)
    {
        Connection c = iocp._client.getConnection();
        if (c.readBuffer != null)        
            c.readBuffer = null;
        
        if (c.writeBuffer != null)            
            c.writeBuffer = null;        
        L2PcInstance player = c.getClient().getActiveChar();
        if (player != null)
        {
            player.setConnected(false);
            if (!Forced)
            {
                // check if player is fighthing
                if (AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
                {
                    c.onlyClose();
                    return;
                }
            }
        }
        try
        {
            c.close();
        }
        catch (Exception dummy)
        {
        }
    }
    
    private class IOCP
    {
        public ClientThread _client;
        public int _state;
        AsyncSocketChannel _channel;

        /**
         * @param client
         * @param state
         */
        public IOCP(ClientThread client, int state)
        {
            _client = client;
            _state = state;
            _channel = client.getConnection().getAsyncChannel();
        }
    }
    
    private class DisconnectionTask implements Runnable
    {
        private IOCP _iocp;

        /**
         * @param iocp
         */
        public DisconnectionTask(IOCP iocp)
        {
            _iocp = iocp;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            closeClient(_iocp, true);
        }
    }
}