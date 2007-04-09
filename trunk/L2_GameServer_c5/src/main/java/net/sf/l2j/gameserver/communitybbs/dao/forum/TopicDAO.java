/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 * 
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
package net.sf.l2j.gameserver.communitybbs.dao.forum;

import java.util.List;
import java.util.Set;

import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;

/**
 * Topic DAO to access data for topics
 */
public interface TopicDAO
{
	public int createTopic(Topic obj);
	
	public Topic getTopicByName (String name);

	public Topic getTopicById (Integer id);

	public List<Topic> getPaginatedTopicByForumId (Integer iPageSize, Integer iIdx, Integer id);

    public void deleteTopic (Topic obj);
    
    public Set<Posts> getPostses (Topic obj);
}
