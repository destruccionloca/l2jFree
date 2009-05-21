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
