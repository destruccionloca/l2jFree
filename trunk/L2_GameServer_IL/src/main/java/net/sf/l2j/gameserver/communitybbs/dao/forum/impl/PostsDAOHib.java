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

import net.sf.l2j.gameserver.communitybbs.dao.forum.PostsDAO;
import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;
import net.sf.l2j.tools.dao.impl.BaseRootDAOHib;

// Generated 19 févr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8


/**
 * Home object for domain model class Posts.
 * @see net.sf.l2j.gameserver.communitybbs.model.forum.Posts
 * @author Hibernate Tools
 */
public class PostsDAOHib extends BaseRootDAOHib implements PostsDAO
{

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.PostsDAO#modifyPost(net.sf.l2j.gameserver.communitybbs.model.forum.Posts)
	 */
	public void modifyPost(Posts obj)
	{
		saveOrUpdate(obj);
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.PostsDAO#createPost(net.sf.l2j.gameserver.communitybbs.model.forum.Posts)
	 */
	public int createPost(Posts obj)
	{
		return (Integer)save(obj);
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.PostsDAO#getPostById(java.lang.Integer)
	 */
	public Posts getPostById(Integer id)
	{
		return (Posts)get(Posts.class, id);
	}

  
}
