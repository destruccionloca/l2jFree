package net.sf.l2j.gameserver.dao.forum.impl;

import java.util.List;

import net.sf.l2j.gameserver.dao.forum.ForumsDAO;
import net.sf.l2j.gameserver.model.forum.Forums;

// Generated 19 févr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8


/**
 * DAO object for domain model class Forums.
 * @see net.sf.l2j.gameserver.model.forum.Forums
 */
public class ForumsDAOHib extends BaseRootDAOHib implements ForumsDAO
{

    /**
     * @see net.sf.l2j.gameserver.dao.forum.ForumsDAO#createForums(net.sf.l2j.gameserver.model.forum.Forums)
     */
    public int createForums(Forums obj)
    {
        return (Integer)save(obj);        
    }

    /**
     * @see net.sf.l2j.gameserver.dao.forum.ForumsDAO#getAllForums()
     */
    @SuppressWarnings("unchecked")
    public List<Forums> getAllForums()
    {
        return findAll(Forums.class);
    }

}
