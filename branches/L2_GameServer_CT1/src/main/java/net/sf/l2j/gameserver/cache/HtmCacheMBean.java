/**
 * 
 */
package net.sf.l2j.gameserver.cache;


/**
 * interface for jmx administration
 *
 * This MBean gives the ability to manipulate the html cache
 *
 */
public interface HtmCacheMBean
{
    public int getLoadedFiles();
    public void reload();
    public double getMemoryUsage();

}
