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
package net.sf.l2j.gameserver.model.entity.geodata;

import java.util.Enumeration;
import java.util.Hashtable;

public class GeoFilePoolManager
{
    private Hashtable<GeoDataFileReader,Long> locked, unlocked;
    private short defaultUnlockedCapacity;
    private long expirationTime;
    public static GeoFilePoolManager _instance = new GeoFilePoolManager();
    
    private GeoFilePoolManager()
    {
       defaultUnlockedCapacity = 5;
       expirationTime  = 900000;     // unused files expire after 15 minutes 
       locked = new Hashtable<GeoDataFileReader,Long>();        
       unlocked = new Hashtable<GeoDataFileReader,Long>();
    }
   
    public static GeoFilePoolManager getInstance()
    {
        if(_instance == null)
            _instance = new GeoFilePoolManager();
         
        return _instance;
    }
    
    public synchronized GeoDataFileReader checkOut(String filename)
    {
        long now = System.currentTimeMillis();
        GeoDataFileReader fileReader;        
        GeoDataFileReader returnFileReader = null;
        if( unlocked.size() > 0 )
        {
            Enumeration<GeoDataFileReader> e = unlocked.keys();  
            while( e.hasMoreElements() )
            {
               fileReader = e.nextElement(); 

               // fileReaders that failed to initialize are not needed. 
               if (fileReader.getName() == null)
               {
                   unlocked.remove(fileReader);
               }
               // if this is the file we need, lock it and set it aside to return it.
               else if(fileReader.getName() == filename)
               {
                   unlocked.remove(fileReader);
                   locked.put( fileReader, new Long(now)); 
                   returnFileReader = fileReader;
               }
               // for all other files, leave then in unless the max capacity has been reached...
               // if the latter case, remove the oldest file.
               if(unlocked.size() > defaultUnlockedCapacity)
                   if((now-(unlocked.get(fileReader)).longValue()) > expirationTime )
                   {
                       // object has expired
                       unlocked.remove(fileReader);
                       fileReader.closeFile();
                       fileReader = null;
                   }
           }
        }        
        
        // if the requested file is not available in the current pool, add it.
        if (returnFileReader == null)
        {
            returnFileReader = new GeoDataFileReader(filename); 
            
            // if the file cannot be openned, return a null
            if (returnFileReader.getName() == null)
                return null;
            
            // otherwise lock it and set it aside ready to be returned.
            locked.put( returnFileReader, new Long(now));
        }
        
        // finally, return the side we set aside.
        return(returnFileReader);
    }
         
    public synchronized void checkIn(GeoDataFileReader fileReader)
    {
        locked.remove(fileReader);
        unlocked.put(fileReader, new Long(System.currentTimeMillis()));
    }
}
