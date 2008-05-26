/*
 * This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.npcserverpackets.GsBasePacket;
import net.sf.l2j.gameserver.npcserverpackets.PacketsQueue;

/**
 * 
 *
 * @author  Luno
 */
public class NpcServerThread extends Thread
{
    private static NpcServerThread _instance;
    
    private ServerSocket _serverSocket;
    private Socket       _connection;
    private OutputStream  out;
    PacketsQueue _queue;
    
    public static NpcServerThread getInstance()
    {
        if(_instance == null)
            _instance = new NpcServerThread();
        return _instance;
    }
    private NpcServerThread()
    {
        _queue = new PacketsQueue();
    }
    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                _serverSocket = new ServerSocket(3100);

                while(true)
                {
                    try
                    {
                        System.out.println("Listening for npc server.");
                        _connection = _serverSocket.accept();
                        
                        System.out.println("Connection from "+_connection.getRemoteSocketAddress()+" "+_connection.getPort());
                        
                        out =  _connection.getOutputStream() ;

                        while(true)
                        {
    
                            if(!_queue.isEmpty())
                            {
                                GsBasePacket packet = _queue.get();
                                
                                int length = packet.getLength();
                                ByteBuffer b = packet.getData();
                                byte[] data = new byte[length];
                                for(int i = 0; i < length; i++)
                                    data[i] = b.get(i);
                                
                                out.write((length + 2)%256); // Size is set on 2 bytes
                                out.write((length + 2)/256);
                                
                                out.write(data); // data contains packet type and content
                                out.flush();
    
                            }
                            yield();
                        }
                    }
                    catch(IOException e){System.out.println("Connection lost");}
                }
            }
            catch(IOException e)
            {
                System.out.println("Couldn't bind socket.");
            }
        }
    }
    public synchronized void addPacket(GsBasePacket packet)
    {
            _queue.add(packet);
    }
}
