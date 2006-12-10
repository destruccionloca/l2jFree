package net.sf.l2j.loginserver.dao.impl;

// Generated 7 déc. 2006 18:49:52 by Hibernate Tools 3.2.0.beta8

import java.util.List;
import javax.naming.InitialContext;

import net.sf.l2j.loginserver.beans.Characters;
import net.sf.l2j.loginserver.dao.CharactersDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import static org.hibernate.criterion.Example.create;

/**
 * Home object for domain model class Characters.
 * @see net.sf.l2j.loginserver.beans.Characters
 * @author Hibernate Tools
 */
public class CharactersDAOHib extends BaseRootDAOHib implements CharactersDAO
{

    private static final Log log = LogFactory.getLog(CharactersDAOHib.class);

}
