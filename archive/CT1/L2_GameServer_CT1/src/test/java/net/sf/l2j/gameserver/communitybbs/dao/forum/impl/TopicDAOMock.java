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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.l2j.gameserver.communitybbs.dao.forum.TopicDAO;
import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;

// Generated 19 févr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8


/**
 * DAO object for domain model class Topic.
 * @see net.sf.l2j.gameserver.communitybbs.model.forum.Topic
 */
public class TopicDAOMock implements TopicDAO
{

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.TopicDAO#createTopic(net.sf.l2j.gameserver.communitybbs.model.forum.Topic)
	 */
	public int createTopic(Topic obj)
	{
		if (obj.getTopicName().equals("good"))
			return 2;
		else
			throw new RuntimeException ("Unable to get topic mock");
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.TopicDAO#deleteTopic(net.sf.l2j.gameserver.communitybbs.model.forum.Topic)
	 */
	public void deleteTopic(Topic obj)
	{
		if ( !obj.getTopicName().equals("good"))
			throw new RuntimeException ("Unable to delete topic mock");
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.TopicDAO#getTopicById(java.lang.Integer)
	 */
	public Topic getTopicById(Integer id)
	{
		if ( id != 1 )
			throw new RuntimeException ("Unable to get by id topic mock");
		else
		{
			Topic topic = new Topic();
			topic.setTopicId(1);
			topic.setTopicName("good");
			return topic;
		}
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.TopicDAO#getTopicByName(java.lang.String)
	 */
	public Topic getTopicByName(String name)
	{
		if ( !name.equals("good"))
			throw new RuntimeException ("Unable to get by name topic mock");
		else
		{
			Topic topic = new Topic();
			topic.setTopicId(1);
			topic.setTopicName("good");
			return topic;
		}	
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.TopicDAO#getPaginatedTopicByForumId(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public List<Topic> getPaginatedTopicByForumId(Integer iPageSize, Integer iIdx, Integer id)
	{
		if ( id != 2 && iPageSize == 2  )
		{
	        List<Topic> topics = new ArrayList<Topic>();
	        
	        Topic topic1 = new Topic();
	        topic1.setTopicForumId(id);
	        topic1.setTopicId(2);
	        
	        topics.add(topic1);
	        
	        Topic topic2 = new Topic();
	        topic2.setTopicForumId(id);
	        topic2.setTopicId(3);
	        
	        topics.add(topic2);
	    	return topics;
		}
		throw new RuntimeException ("Unable to get topics paginated mock");
	}

    public Set<Posts> getPostses(Topic obj)
    {
        return obj.getPostses();
    }

 


}
