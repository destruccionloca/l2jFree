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
