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
package net.sf.l2j.gameserver.communitybbs.services.forum;

import java.util.List;
import java.util.Set;

import net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO;
import net.sf.l2j.gameserver.communitybbs.dao.forum.PostsDAO;
import net.sf.l2j.gameserver.communitybbs.dao.forum.TopicDAO;
import net.sf.l2j.gameserver.communitybbs.model.forum.Forums;
import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Account service to handle account management
 * 
 */
public class ForumService 
{
    private static Log _log = LogFactory.getLog(ForumService.class);
    
	//type
	public static final int ROOT = 0;
	public static final int NORMAL = 1;
	public static final int CLAN = 2;
	public static final int MEMO = 3;
	public static final int MAIL = 4;
	//perm
	public static final int INVISIBLE = 0;
	public static final int ALL = 1;
	public static final int CLANMEMBERONLY = 2;
	public static final int OWNERONLY = 3;
    
    
    
    private ForumsDAO __forumDAO = null;
    private TopicDAO __topicDAO = null;
    private PostsDAO __postsDAO = null;
    
    public void setForumsDAO (ForumsDAO forumDAO)
    {
        __forumDAO =  forumDAO;
    }
    
    public void setTopicDAO (TopicDAO topicDAO)
    {
    	__topicDAO =  topicDAO;
    }    
    public void setPostsDAO (PostsDAO postsDAO)
    {
    	__postsDAO =  postsDAO;
    }       
    
    /**
     * 
     * @param name
     * @param iForumParent
     * @param type
     * @param perm
     * @param owner id
     * @return Forum
     */
    public Forums createForum(String name, Integer forumParent, int type, int perm, int oid) 
    {
    	Forums forum;
		forum = new Forums();
		forum.setForumParent(forumParent);
		forum.setForumName(name);
		forum.setForumType(type);
		forum.setForumPerm(perm);
		forum.setForumOwnerId(oid);
		try
		{
			__forumDAO.createForums(forum);
		}
		catch (Exception e)
		{
			_log.error("Unable to create forum : "+name+". Error : "+e.getMessage());
			return null;
		}
		return forum;
    }    
    
    /**
     * Return the list of all forums
     * 
     * @return list of all forums
     */
    public List<Forums>getAllForums ()
    {
        List<Forums> forums=null;
        try
        {
            forums= __forumDAO.getAllForums();
        }
        catch (Exception e)
        {
            _log.error("Unable to list all forums. Error : "+e.getMessage());
        }
        return forums;
    }
    
    /**
     * Get Forum information for a specific id
     * 
     */
    public Forums getForumById(int id)
    {
        try
        {
            Forums forum = __forumDAO.getForumById(id);
            return forum;
        }
        catch (Exception e)
        {
            _log.info ("unable to find forum : "+id+". Error : "+e.getMessage());
            return null;
        }
    }       
    
    /**
     * 
     * @param name
     * @param objecId
     * @return Forums f
     */
    public Forums getMemoForAccountAndCreateIfNotAvailable(String name,int objecId)
    {
    	Forums f = getChildForumByName(ForumService.MEMO,name);
        if (f == null)
        {
        	f = createForum(name, ForumService.MEMO, ForumService.MEMO, ForumService.OWNERONLY, objecId);
        }
        return f;
    }
    
    /**
     * 
     * @param name
     * @param objecId
     * @return Forums f
     */
    public Forums getForumForClanAndCreateIfNotAvailable(String name,int clanId)
    {
    	Forums f = getChildForumByName(ForumService.CLAN,name);
        if (f == null)
        {
        	f = createForum(name, ForumService.CLAN, ForumService.CLAN, ForumService.CLANMEMBERONLY, clanId);
        }
        return f;
    }
    
    /**
     * 
     * @param name
     * @param objecId
     * @return Forums f
     */
    public Forums getMailForAccountAndCreateIfNotAvailable(String name,int objecId)
    {
    	Forums f = getChildForumByName(ForumService.MAIL,name);
        if (f == null)
        {
        	f = createForum(name, ForumService.MAIL, ForumService.MAIL, ForumService.OWNERONLY, objecId);
        }
        return f;
    }    
    
    /**
     * Get child Forum information with a specific name
     * 
     */
    public Forums getChildForumByName(Integer id,String name)
    {
        try
        {
            Forums forum = __forumDAO.getChildForumByName(id,name);
            return forum;
        }
        catch (Exception e)
        {
            _log.info ("unable to find forum : "+name+" with parent id = "+id+". Error : "+e.getMessage());
            return null;
        }
    }        
    
    /**
     * Get the number of topics for a forum
     * @param topic
     * @param index
     * @return the posts located at index index or an empty posts
     */
    public Posts getPostByIndexForTopic(Topic topic,int index)
    {
        try
        {
            Set<Posts> posts = __topicDAO.getPostses(topic);
            Posts post = (Posts)posts.toArray()[index];
            return post;
        }
        catch (Exception e)
        {
            _log.error ("unable to load post ("+index+") for topic : "+topic.getTopicName()+". Error : "+e.getMessage());
            return new Posts();
        }
    }      
    
    /**
     * Get the number of topics for a forum
     * @param obj
     * @return the size of the topic set 
     */
    public int getTopicNumberForForum(Forums obj)
    {
        try
        {
            Set<Topic> topics = __forumDAO.getTopicsForForum(obj);
            return topics.size();
        }
        catch (Exception e)
        {
            _log.error ("unable to load topics for forums : "+obj.getForumName()+". Error : "+e.getMessage());
            return 0;
        }
    }    
    
    /**
     * 
     * @param Topic (without id)
     * @return Topic (with id)
     */
    public Topic createTopic(Topic topic) 
    {
		try
		{
			int id = __topicDAO.createTopic(topic);
			topic.setTopicId(id);
		}
		catch (Exception e)
		{
			_log.error("Unable to create topic : "+topic.getTopicName()+". Error : "+e.getMessage());
			return null;
		}
		return topic;
    }
    
    /**
     * Get topic information for a specific id
     * @param int
     */
    public Topic getTopicById(int id)
    {
        try
        {
        	Topic topic = __topicDAO.getTopicById(id);
            return topic;
        }
        catch (Exception e)
        {
            _log.info ("unable to find topic : "+id+". Error : "+e.getMessage());
            return null;
        }
    }       

    /**
     * Get topic information for a specific forum id and paginated
     * @param forumId
     * @param iPageSize
     * @param iIdx
     * @return Topics 
     */
    public List<Topic> getPaginatedTopicsByForumId(int forumId,int iPageSize,int iIdx)
    {
        try
        {
        	List<Topic> topics = __topicDAO.getPaginatedTopicByForumId(iPageSize, iIdx, forumId);
            return topics;
        }
        catch (Exception e)
        {
            _log.info ("unable to find topics for the forum : "+forumId+" in page "+iIdx+". Error : "+e.getMessage());
            return null;
        }
    }    
    
    /**
     * Delete topic
     * @param Topic
     */
    public void deleteTopic(Topic topic )
    {
        try
        {
        	__topicDAO.deleteTopic(topic);
        }
        catch (Exception e)
        {
            _log.error ("unable to delete topic. Error : "+e.getMessage());
        }
    }        

    /**
     * Get post information for a specific id
     * @param int
     */
    public Posts getPostById(int id)
    {
        try
        {
        	Posts post = __postsDAO.getPostById(id);
            return post;
        }
        catch (Exception e)
        {
            _log.info ("unable to find post : "+id+". Error : "+e.getMessage());
            return null;
        }
    }       
    
    /**
     * 
     * @param Posts (without id)
     * @return Posts (with id)
     */
    public Posts createPost(Posts post) 
    {
		try
		{
			int id = __postsDAO.createPost(post);
			post.setPostId(id);
		}
		catch (Exception e)
		{
			_log.error("Unable to create post. Error : "+e.getMessage());
			return null;
		}
		return post;
    }   
    
    /**
     * 
     * @param Posts (without id)
     */
    public void modifyPost(Posts post) 
    {
		try
		{
			__postsDAO.modifyPost(post);
		}
		catch (Exception e)
		{
			_log.error("Unable to modify post "+post.getPostId()+". Error : "+e.getMessage());
		}
    }

   
    
}
