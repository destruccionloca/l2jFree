package net.sf.l2j.gameserver.communitybbs.dao.forum.impl;

import net.sf.l2j.gameserver.communitybbs.dao.forum.PostsDAO;
import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;

// Generated 19 févr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8


/**
 * DAO object for domain model class Posts.
 * @see net.sf.l2j.gameserver.communitybbs.model.forum.Posts
 */
public class PostsDAOMock implements PostsDAO
{

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.PostsDAO#modifyPost(net.sf.l2j.gameserver.communitybbs.model.forum.Posts)
	 */
	public void modifyPost(Posts obj)
	{
		if ( !obj.getPostTxt().equals("good"))
			throw new RuntimeException ("Unable to change post mock");
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.PostsDAO#createPost(net.sf.l2j.gameserver.communitybbs.model.forum.Posts)
	 */
	public int createPost(Posts obj)
	{
		if (obj.getPostTxt().equals("good"))
			return 2;
		else
			throw new RuntimeException ("Unable to get post mock");
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.PostsDAO#getPostById(java.lang.Integer)
	 */
	public Posts getPostById(Integer id)
	{
		if ( id != 1 )
			throw new RuntimeException ("Unable to get by id post mock");
		else
		{
			Posts post = new Posts();
			post.setPostId(1);
			post.setPostTxt("good");
			return post;
		}
	}

 


}
