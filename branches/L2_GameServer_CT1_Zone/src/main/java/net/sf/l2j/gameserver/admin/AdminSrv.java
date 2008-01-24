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
package net.sf.l2j.gameserver.admin;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.SSLSocketFactory;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2World;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jdmk.comm.HtmlAdaptorServer;

/**
 * JMX administration server Initialize jmx admin and publish service though addMonitoredObject A HTTP adapter could be launched too. This adapter is able to
 * expose the service on a web page but without security, so, don't use it in production !
 */
public class AdminSrv extends Thread
{
	/** Logger */
	private static Log			_log		= LogFactory.getLog(AdminSrv.class);
	/** singleton */
	protected static AdminSrv	_instance	= null;
	/** Protocol JMXmp */
	public static final String	JMXMP		= "jmxmp";
	/** Protocol RMI */
	public static final String	RMI			= "rmi";
	/** The protocole */
	public static final String	PROTOCOL	= JMXMP;
	/** The server JMX */
	private JMXConnectorServer	m_Server;
	/** The MBeanServer */
	private MBeanServer			m_beanServer;
	/** Html Adaptor */
	private HtmlAdaptorServer	m_hadaptor	= null;
	/** Port socket used * */
	private int					m_iSrvPort;
	private int					m_iHttpPort;
	/** keystore file */
	private File				m_KeystoreFile;
	/** Keystore Password */
	private String				m_keystorePassword;
	
	/**
	 * Initialize the admin server
	 * 
	 * @return the instance
	 * @throws IOException
	 *             if admin failed to start
	 */
	public static AdminSrv getInstance() throws IOException
	{
		if (_instance == null)
		{
			_instance = new AdminSrv();
		}
		return _instance;
	}
	
	/**
	 * Register all beans into mbean server in order to expose it
	 */
	public void registerMbeans()
	{
		// Adding Mbean to manage
		try
		{
			// admin.addMonitoredObject(this, "L2J:type=gameserver");
			addMonitoredObject(SkillTable.getInstance(), "L2J:type=SkillTable");
			addMonitoredObject(ThreadPoolManager.getInstance(), "L2J:type=ThreadPools");
			addMonitoredObject(ItemTable.getInstance(), "L2J:type=ItemTable");
			addMonitoredObject(SkillTreeTable.getInstance(), "L2J:type=SkillTreeTable");
			addMonitoredObject(HtmCache.getInstance(), "L2J:type=HtmCache");
			addMonitoredObject(NpcTable.getInstance(), "L2J:type=NpcTable");
			addMonitoredObject(L2World.getInstance(), "L2J:type=L2World");
			addMonitoredObject(SpawnTable.getInstance(), "L2J:type=SpawnTable");
			addMonitoredObject(Announcements.getInstance(), "L2J:type=Announcements");
			addMonitoredObject(Shutdown.getInstance(), "L2J:type=Shutdown");
		}
		catch (IOException e)
		{
			_log.error("Unable to register mbeans into server", e);
		}
	}
	
	/**
	 * Initialize jmx server with config options. If the user provide a keystore file, this file will be to enable SSL between l2jmx and the GS. It is highly
	 * recommended to use SSL ! The server will start on a tcp socket configured by JMX_TCP_PORT
	 * 
	 * @throws IOException
	 *             if admin failed to start (connection problem or initialization failure
	 */
	private AdminSrv() throws IOException
	{
		m_iSrvPort = Config.JMX_TCP_PORT;
		m_iHttpPort = Config.JMX_HTTP_PORT;
		
		m_keystorePassword = Config.JMX_KEYSTORE_PASSWORD;
		
		HashMap<String, Object> env = new HashMap<String, Object>();
		
		// For SSL Support
		m_KeystoreFile = new File("./config" + File.separator + Config.JMX_KEYSTORE);
		if (m_KeystoreFile != null && m_KeystoreFile.canRead())
		{
			System.setProperty("javax.net.ssl.keyStore", m_KeystoreFile.getAbsolutePath());
			System.setProperty("javax.net.ssl.keyStorePassword", m_keystorePassword);
			SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
			
			env.put("jmx.remote.profiles", "TLS");
			
			env.put("jmx.remote.tls.socket.factory", ssf);
			_log.info("SSL activated with file :" + m_KeystoreFile.getAbsolutePath());//$NON-NLS-1$
		}
		else
		{
			_log.warn("SSL Disabled ! Are you sure you want to disable SSL ?");//$NON-NLS-1$
		}
		
		// Le serveur jmx
		if (this.m_iSrvPort != -1)
		{
			m_beanServer = ManagementFactory.getPlatformMBeanServer();
			JMXServiceURL url = new JMXServiceURL(PROTOCOL, null, m_iSrvPort);
			
			m_Server = JMXConnectorServerFactory.newJMXConnectorServer(url, env, m_beanServer);
			startAdmin();
			_log.info("JMX server started on " + url.toString());
		}
		
		if (m_iHttpPort != -1)
		{
			if (m_iSrvPort == -1) { throw new IOException("AdminHTTPNotStartedMissingJMX"); }
			// Le serveur jmx
			enableHTTP(m_iHttpPort);
		}
	}
	
	/**
	 * Add HTTP adapter
	 * 
	 * @param iPort
	 *            port HTTP
	 * @throws IOException
	 */
	public void enableHTTP(int iPort) throws IOException
	{
		_log.debug("Creating an HTML protocol adaptor...");
		m_hadaptor = new HtmlAdaptorServer();
		m_hadaptor.setPort(iPort);
		ObjectName adaptorName = null;
		
		try
		{
			adaptorName = new ObjectName("Adaptor:name=hadaptor,port=" + iPort);
			m_beanServer.registerMBean(m_hadaptor, adaptorName);
		}
		catch (Exception e)
		{
			IOException ex = new IOException("InitHttpAdaptorError");
			ex.initCause(e);
			throw ex;
		}
		
		m_hadaptor.start();
		String url = "http://" + InetAddress.getLocalHost().getHostName() + ":" + iPort;
		_log.info("HTTP adaptor started on " + url.toString()); //$NON-NLS-1$
	}
	
	/**
	 * Register an mbean instance.
	 * 
	 * @param oToRegister
	 * @param sNameToUse
	 * @throws IOException
	 */
	public void registerInstance(Object oToRegister, String sNameToUse) throws IOException
	{
		try
		{
			ObjectName oName = new ObjectName(sNameToUse);
			m_beanServer.registerMBean(oToRegister, oName);
		}
		catch (Exception ex)
		{
			IOException exc = new IOException(ex.getMessage());
			exc.initCause(ex);
			throw exc;
		}
	}
	
	/**
	 * unRegister an mbean instance.
	 * 
	 * @param sNameToUse
	 * @throws IOException
	 */
	public void unregisterInstance(String sNameToUse) throws IOException
	{
		try
		{
			ObjectName oName = new ObjectName(sNameToUse);
			m_beanServer.unregisterMBean(oName);
		}
		catch (Exception ex)
		{
			IOException exc = new IOException(ex.getMessage());
			exc.initCause(ex);
			throw exc;
		}
	}
	
	/**
	 * add an object to monitor and exposed his service
	 * 
	 * @param oToMonitor
	 * @param sName
	 * @throws IOException
	 *             if operation failed
	 */
	public void addMonitoredObject(Object oToMonitor, String sName) throws IOException
	{
		if (m_Server != null)
			registerInstance(oToMonitor, sName);
	}
	
	/**
	 * remove an object to monitor
	 * 
	 * @param sName
	 * @throws IOException
	 *             if operation failed
	 */
	public void removeMonitoredObject(String sName) throws IOException
	{
		if (m_Server != null)
			unregisterInstance(sName);
	}
	
	/**
	 * start the administration
	 */
	public void startAdmin() throws IOException
	{
		m_Server.start();
	}
	
	/**
	 * Stop jmx servers
	 */
	public void stopAdmin() throws IOException
	{
		if (m_hadaptor != null)
		{
			m_hadaptor.stop();
		}
		m_Server.stop();
	}
}
