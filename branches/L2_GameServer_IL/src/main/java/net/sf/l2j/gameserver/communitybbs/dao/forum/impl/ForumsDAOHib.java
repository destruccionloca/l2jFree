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
package net.sf.l2j.gameserver.communitybbs.dao.forum.impl;

import java.util.List;
import java.util.Set;

import net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO;
import net.sf.l2j.gameserver.communitybbs.model.forum.Forums;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;
import net.sf.l2j.tools.dao.impl.BaseRootDAOHib;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;

// Generated 19 févr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8


/**
 * DAO object for domain model class Forums.
 * @see net.sf.l2j.gameserver.communitybbs.model.forum.Forums
 */
public class ForumsDAOHib extends BaseRootDAOHib implements ForumsDAO
{

    /**
     * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#createForums(net.sf.l2j.gameserver.communitybbs.model.forum.Forums)
     */
    public int createForums(Forums obj)
    {
        return (Integer)save(obj);        
    }

    /**
     * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#getAllForums()
     */
    @SuppressWarnings("unchecked")
    public List<Forums> getAllForums()
    {
        return findAll(Forums.class);
    }

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#getChildrens(net.sf.l2j.gameserver.communitybbs.model.forum.Forums)
	 */
	@SuppressWarnings("unchecked")
	public List<Forums> getChildrens(Forums obj)
	{
		return getCurrentSession().createQuery(
				"from " + Forums.class.getName()+ " where forumParent = :forumId").setInteger("forumId", obj.getForumId()).list();
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#getChildrens(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Forums> getChildrens(Integer forumId)
	{
        return getCurrentSession().createQuery(
                "from " + Forums.class.getName()+ " where forumParent = :forumId").setInteger("forumId", forumId).list();
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#getForumById(java.lang.Integer)
	 */
	public Forums getForumById(Integer id)
	{
		return (Forums)get(Forums.class, id);
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#deleteForum(net.sf.l2j.gameserver.communitybbs.model.forum.Forums)
	 */
	public void deleteForum(Forums obj)
	{
		delete(obj);		
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#getChildForumByName(Integer ,java.lang.String)
	 */
	public Forums getChildForumByName(Integer forumId, String name)
	{
        return (Forums)getCurrentSession().createCriteria(Forums.class)
                                                .add(Restrictions.eq("forumName", name))
                                                .add(Restrictions.eq("forumParent", forumId)).uniqueResult();
	}
	
    /**
     * Initialize the object (force retrieval of associated objects
     * 
     * @param obj
     * @return 
     */
    public Set<Topic> getTopicsForForum(Forums obj)
    {
        if ( !Hibernate.isInitialized(obj.getTopics()))
        {
            getCurrentSession().refresh(obj);
        }
        return obj.getTopics();
    }


}
