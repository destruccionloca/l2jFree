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
package net.sf.l2j.util;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2DropData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import net.sf.l2j.gameserver.lib.MersenneTwisterFast;

public class RandomIntGenerator
{
    private static final Log _log = LogFactory.getLog(RandomIntGenerator.class); 

    private static MersenneTwisterFast _randomMTF;
    private boolean[] buffer = new boolean[L2DropData.MAX_CHANCE];
    private static RandomIntGenerator _instance;
    
    public MersenneTwisterFast getMTF()
    {
    	return _randomMTF;
    }
    
    public static final RandomIntGenerator getInstance()
    {
        if (_instance == null)
            _instance = new RandomIntGenerator();
        return _instance;
    }
    
    private RandomIntGenerator()
    {
        Restart();
   	 	_randomMTF = new MersenneTwisterFast(4357); //seed
        ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Sched(),1000, 1000);
        _log.info("RandomIntGenerator: initialized");
    }
    
    private class Sched implements Runnable
    {
        public void run()
        {
            nextRandom();
        }            
    }

    public synchronized double nextRandom()
    {
        double r = 0;
        int pos = 0, iteration = 0;;
        pos = (int) (_randomMTF.nextDouble() * L2DropData.MAX_CHANCE);
        
        while(buffer[pos] && iteration <= L2DropData.MAX_CHANCE)
        {
            pos+=33333;
            iteration++;
            if(pos>=L2DropData.MAX_CHANCE) pos = pos - L2DropData.MAX_CHANCE;
        }
        
        if(iteration >= L2DropData.MAX_CHANCE)
        {
            Restart();
            pos = (int) (_randomMTF.nextDouble() * L2DropData.MAX_CHANCE);
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
