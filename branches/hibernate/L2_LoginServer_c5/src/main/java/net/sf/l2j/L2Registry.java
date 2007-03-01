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
package net.sf.l2j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * Object registry for L2 LS.
 * 
 * The registry store singleton and is able to act as a factory.
 * All singleton and factory are declared in spring.xml
 * 
 * There is no risk to call the load method more than one time. 
 * The first call initialize all singleton by IoC mechanism.
 * 
 */
public class L2Registry
{
    private static final Log _log = LogFactory.getLog(L2Registry.class.getName());
    
    private static ApplicationContext __ctx = null;

    /**
     * Load registry from spring
     * The registry is a facade behind ApplicationContext from spring.
     */
    public static void loadRegistry ()
    {
        try
        {
            // init properties for spring 
            String[] paths = {"spring.xml"};
            __ctx = new ClassPathXmlApplicationContext(paths);
        }
        catch (Throwable e)
        {
            _log.fatal("Unable to load registry : " + e.getMessage(),e);
            System.exit(1);
        }
    }
    
    /**
     * Retrieve a bean from registry
     * @param bean - the bean name
     * @return the Object 
     */
    public static Object getBean (String bean)
    {
        return __ctx.getBean(bean);
    }    
    
    
    
    // =========================================================
    // Data Field
    private static L2Registry _instance;
	
    // =========================================================
    // Constructor
	private L2Registry()
	{
	}
    
    // =========================================================
    // Property - Public
	public static L2Registry getInstance() 
	{
		if (_instance == null)
		{
			_instance = new L2Registry();
		}
		return _instance;
	}
	
    public static ApplicationContext getApplicationContext() 
    {
        return __ctx;
    }
    
    /**
     * Give ability to overload application context (for test purpose)
     * @param ctx
     */
    public static void setApplicationContext (ApplicationContext ctx)
    {
        __ctx = ctx;
    }

    
    
}
