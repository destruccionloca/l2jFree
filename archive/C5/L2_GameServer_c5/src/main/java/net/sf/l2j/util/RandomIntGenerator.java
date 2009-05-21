package net.sf.l2j.util;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2DropData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RandomIntGenerator{
    
    private static final Log _log = LogFactory.getLog(RandomIntGenerator.class); 
    private boolean[] buffer = new boolean[L2DropData.MAX_CHANCE];
    private static RandomIntGenerator _Instance;
    
    public static final RandomIntGenerator getInstance()
    {
        if (_Instance == null)
        {
            _log.info("Initializing RandomIntGenerator");
            _Instance = new RandomIntGenerator();
        }
        return _Instance;
    }
    
    private RandomIntGenerator(){
        Restart();
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Sched(),1000, 1000);
    }
    
    private class Sched implements Runnable
    {
        public void run()
        {
            nextRandom();
        }            
    }

    public synchronized double nextRandom(){
        double r = 0;
        int pos = 0, iteration = 0;;
        pos = (int) (java.lang.Math.random() * L2DropData.MAX_CHANCE);
        while(buffer[pos] && iteration <= L2DropData.MAX_CHANCE)
        {
            pos+=33333;
            iteration++;
            if(pos>=L2DropData.MAX_CHANCE) pos = pos - L2DropData.MAX_CHANCE;
        }
        if(iteration >= L2DropData.MAX_CHANCE)
        {
            Restart();
            pos = (int) (java.lang.Math.random() * L2DropData.MAX_CHANCE);
        }
        r = (double)pos / L2DropData.MAX_CHANCE;
        buffer[pos] = true;
        return r;
    }

    public void Restart()
    {
        for (int i = 0; i < L2DropData.MAX_CHANCE; i++) buffer[i] = false;        
    }
}
