package net.sf.l2j.gameserver.network;

import static net.sf.l2j.gameserver.TaskPriority.PR_NORMAL;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.clientpackets.ClientBasePacket;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;

/**
* @author -Nemesiss-
* 
*/

//Just to handle many I/O types for L2j
public class IOThread extends Thread
{
    private static IOThread _instance;
    
    public IOThread(String name)
    {
        super(name);
    }
    
    public static IOThread getInstance()
    {
        if(_instance == null)
        {
            if (Config.IO_TYPE == Config.IOType.nio)
                _instance = SelectorThread.getInstance();
            else if (Config.IO_TYPE == Config.IOType.aio4j)
                _instance = AsyncIOThread.getInstance();            
        }
        return _instance;
    }
    
    public void addReceivedPkt(ClientBasePacket pkt)
    {
        TaskPriority pr = pkt.getPriority();
        
        if (pr == null)
            pr = PR_NORMAL;
        
        // Add a task to one of the pool of thread in function of its priority (HIGH, MEDIUM,LOW)
        // This has effect equivalent to schedule(command, 0, anyUnit)
        switch (pr)
        {
        case PR_URGENT:
            pkt.run();
            return;
        case PR_HIGH:
            ThreadPoolManager.getInstance().executeUrgentPacket(pkt);
            return;
        case PR_NORMAL:
        default:
            ThreadPoolManager.getInstance().executePacket(pkt);
            return;
        }
    }
    void sendMessage(ServerBasePacket pkt)
    {
        //nothing.
    }
}
