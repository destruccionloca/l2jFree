package net.sf.l2j.loginserver.beans;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil
{

    private static final SessionFactory sessionFactory;

    static
    {
        try
        {
            // Crée la SessionFactory
            sessionFactory = new Configuration().configure().buildSessionFactory();
        }
        catch (HibernateException ex)
        {
            throw new RuntimeException("Configuration error : " + ex.getMessage(), ex);
        }
    }

    public static final ThreadLocal session = new ThreadLocal();

    public static Session currentSession() throws HibernateException
    {
        Session s = (Session) session.get();
        // Ouvre une nouvelle Session, si ce Thread n'en a aucune
        if (s == null)
        {
            s = sessionFactory.openSession();
            session.set(s);
        }
        return s;
    }

    public static void closeSession() throws HibernateException
    {
        Session s = (Session) session.get();
        session.set(null);
        if (s != null) s.close();
    }
}