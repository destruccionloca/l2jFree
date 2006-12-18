package net.sf.l2j.util;

import net.sf.l2j.gameserver.model.L2DropData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RandomIntGenerator{
    
    private static final Log _log = LogFactory.getLog(RandomIntGenerator.class);
    
    private int low;
    private int high;
    private static final int BUFFER_SIZE = 1001;
    private static double[] buffer = new double[BUFFER_SIZE];
    static{
        int i;
        for (i = 0; i < BUFFER_SIZE; i++)
            buffer[i] = java.lang.Math.random();
    }
    private static RandomIntGenerator _Instance;
    
    public static final RandomIntGenerator getInstance()
    {
        if (_Instance == null)
        {
            _log.info("Initializing RandomIntGenerator");
            _Instance = new RandomIntGenerator(0, L2DropData.MAX_CHANCE);
        }
        return _Instance;
    }
    
    public RandomIntGenerator(int l, int h){
        low = l;
        high = h;
    }

    public int getRnd(){
        int r = low + (int) ((high - low + 1) * nextRandom());
        if (r > high) r = high;
        return r;
    }
    
    private static double nextRandom(){
        int pos = (int) (java.lang.Math.random() * BUFFER_SIZE);
        if (pos == BUFFER_SIZE) pos = BUFFER_SIZE - 1;
        double r = buffer[pos];
        buffer[pos] = java.lang.Math.random();
        return r;
    }
}
