package net.sf.l2j.gameserver.dao.forum.impl;

import net.sf.l2j.gameserver.dao.forum.PostsDAO;
import net.sf.l2j.gameserver.model.forum.Posts;

// Generated 19 févr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8


/**
 * Home object for domain model class Posts.
 * @see net.sf.l2j.gameserver.model.forum.Posts
 * @author Hibernate Tools
 */
public class PostsDAOHib extends BaseRootDAOHib implements PostsDAO
{

	/**
	 * @see net.sf.l2j.gameserver.dao.forum.PostsDAO#modifyPost(net.sf.l2j.gameserver.model.forum.Posts)
	 */
	public void modifyPost(Posts obj)
	{
		saveOrUpdate(obj);
	}

	/**
	 * @see net.sf.l2j.gameserver.dao.forum.PostsDAO#createPost(net.sf.l2j.gameserver.model.forum.Posts)
	 */
	public int createPost(Posts obj)
	{
		return (Integer)save(obj);
	}

	/**
	 * @see net.sf.l2j.gameserver.dao.forum.PostsDAO#getPostById(java.lang.Integer)
	 */
	public Posts getPostById(Integer id)
	{
		return (Posts)get(Posts.class, id);
	}

  
}
