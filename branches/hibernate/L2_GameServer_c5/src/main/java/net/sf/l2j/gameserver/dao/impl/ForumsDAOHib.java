package net.sf.l2j.gameserver.dao.impl;

import java.util.List;

import net.sf.l2j.gameserver.beans.Forums;
import net.sf.l2j.gameserver.beans.ForumsId;
import net.sf.l2j.gameserver.dao.ForumsDAO;

// Generated 19 févr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8


/**
 * DAO object for domain model class Forums.
 * @see net.sf.l2j.gameserver.beans.Forums
 */
public class ForumsDAOHib extends BaseRootDAOHib implements ForumsDAO
{

    /**
     * @see net.sf.l2j.gameserver.dao.ForumsDAO#createForums(net.sf.l2j.gameserver.beans.Forums)
     */
    public ForumsId createForums(Forums obj)
    {
        return (ForumsId)save(obj);        
    }

    /**
     * @see net.sf.l2j.gameserver.dao.ForumsDAO#getAllForums()
     */
    @SuppressWarnings("unchecked")
    public List<Forums> getAllForums()
    {
        return findAll(Forums.class);
    }

}
