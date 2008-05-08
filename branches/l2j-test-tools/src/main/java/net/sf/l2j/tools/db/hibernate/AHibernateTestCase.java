package net.sf.l2j.tools.db.hibernate;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Iterator;

import junit.framework.TestCase;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;

/**
 * <b>Description :</b> Adaptation of org.hibernate.test.HibernateTestCase for
 * hibernate test case. Allow to test dynamic model with hbm2ddl.
 * 
 */
public abstract class AHibernateTestCase extends TestCase
{
	private static SessionFactory sessions;

	private static Configuration cfg;

	private static Dialect dialect;

	private org.hibernate.classic.Session __session;

	public boolean recreateSchema()
	{
		return true;
	}

	public AHibernateTestCase(String x)
	{
		super(x);
	}

	public void configure(Configuration cfg)
	{
	}

	/**
	 * 
	 * @param files
	 * @throws Exception
	 */
	private void __buildSessionFactory(String[] files) throws Exception
	{

		if (getSessions() != null)
			getSessions().close();

		try
		{

			setCfg(new Configuration());

			if (recreateSchema())
			{
				cfg.setProperty(Environment.HBM2DDL_AUTO, "create-drop");
			}

			for (int i = 0; i < files.length; i++)
			{
				if (!files[i].startsWith("mapping/"))
					files[i] = getBaseForMappings() + files[i];
				getCfg().addResource(files[i], AHibernateTestCase.class.getClassLoader());
			}
			setDialect(Dialect.getDialect());

			configure(cfg);

			if (getCacheConcurrencyStrategy() != null)
			{

				Iterator iter = cfg.getClassMappings();
				while (iter.hasNext())
				{
					PersistentClass clazz = (PersistentClass) iter.next();
					Iterator props = clazz.getPropertyClosureIterator();
					boolean hasLob = false;
					while (props.hasNext())
					{
						Property prop = (Property) props.next();
						if (prop.getValue().isSimpleValue())
						{
							String type = ((SimpleValue) prop.getValue()).getTypeName();
							if ("blob".equals(type) || "clob".equals(type))
								hasLob = true;
							if (Blob.class.getName().equals(type) || Clob.class.getName().equals(type))
								hasLob = true;
						}
					}
					if (!hasLob && !clazz.isInherited())
					{
						cfg.setCacheConcurrencyStrategy(clazz.getEntityName(), getCacheConcurrencyStrategy());
					}
				}

				iter = cfg.getCollectionMappings();
				while (iter.hasNext())
				{
					Collection coll = (Collection) iter.next();
					cfg.setCollectionCacheConcurrencyStrategy(coll.getRole(), getCacheConcurrencyStrategy());
				}

			}

			setSessions(getCfg().buildSessionFactory(/* new TestInterceptor() */
			));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * @return
	 */
	public String getBaseForMappings()
	{
		return "mappings/";
	}

	public String getCacheConcurrencyStrategy()
	{
		return "nonstrict-read-write";
	}

	public void setUp() throws Exception
	{
		__buildSessionFactory(getMappings());
		__session = openSession();
	}

	public void runTest() throws Throwable
	{
		final boolean stats = ((SessionFactoryImplementor) sessions).getStatistics().isStatisticsEnabled();
		try
		{
			if (stats)
				sessions.getStatistics().clear();
			super.runTest();
			if (stats)
				sessions.getStatistics().logSummary();
			if (__session != null && __session.isOpen())
			{
				if (__session.isConnected())
					__session.connection().commit();
				__session.close();
				__session = null;
			}
			else
			{
				__session = null;
			}
		}
		catch (Throwable e)
		{
			try
			{
				if (__session != null && __session.isOpen())
				{
					if (__session.isConnected())
						__session.connection().rollback();
					__session.close();
					__session = null;
				}
			}
			catch (Exception ignore)
			{
			}
			try
			{
				if (dropAfterFailure() && sessions != null)
				{
					sessions.close();
					sessions = null;
				}
			}
			catch (Exception ignore)
			{
			}
			throw e;
		}
	}

	public boolean dropAfterFailure()
	{
		return true;
	}

	protected org.hibernate.classic.Session openSession() throws HibernateException
	{
		__session = getSessions().openSession();
		return __session;
	}

	public org.hibernate.classic.Session openSession(Interceptor interceptor) throws HibernateException
	{
		__session = getSessions().openSession(interceptor);
		return __session;
	}

	public abstract String[] getMappings();

	public void setSessions(SessionFactory _sessions)
	{
		AHibernateTestCase.sessions = _sessions;
	}

	public SessionFactory getSessions()
	{
		return sessions;
	}

	public void setDialect(Dialect _dialect)
	{
		dialect = _dialect;
	}

	public Dialect getDialect()
	{
		return dialect;
	}

	public static void setCfg(Configuration _cfg)
	{
		AHibernateTestCase.cfg = _cfg;
	}

	public static Configuration getCfg()
	{
		return cfg;
	}

	public org.hibernate.classic.Session getSession()
	{
		return __session;
	}

	public void setSession(org.hibernate.classic.Session __session)
	{
		this.__session = __session;
	}
}
