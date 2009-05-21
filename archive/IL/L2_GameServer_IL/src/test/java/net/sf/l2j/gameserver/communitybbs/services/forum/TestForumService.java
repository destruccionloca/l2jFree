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
package net.sf.l2j.gameserver.communitybbs.services.forum;

import java.util.List;

import junit.framework.TestCase;
import net.sf.l2j.gameserver.communitybbs.model.forum.Forums;
import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.tools.L2Registry;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This class test ban management
 * 
 */
public class TestForumService extends TestCase
{
    private ClassPathXmlApplicationContext context = null;
    private ForumService __forumService;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        context = new ClassPathXmlApplicationContext("classpath*:net/sf/l2j/applicationContext-TestMock.xml");
        L2Registry.setApplicationContext(context);
        __forumService = (ForumService)L2Registry.getBean(IServiceRegistry.FORUM);
    }
    
    public void testCreateForum()
	{
    	Forums fils = __forumService.createForum("TestForum", 3 , 1, 2, 3);
    	
    	assertNotNull(fils);
    	assertEquals(fils.getForumName(), "TestForum");
    	assertEquals(fils.getForumOwnerId(),3);
    	assertEquals(fils.getForumParent(), 3);
    	assertEquals(fils.getForumPerm(), 2);    	
	}
    
    public void testGetMemoForAccountAndCreateIfNotAvailable ()
    {
    	Forums memo = __forumService.getMemoForAccountAndCreateIfNotAvailable("player1", 25556);
    	assertNotNull(memo);
    	assertEquals(memo.getForumName(), "player1");
    	assertEquals(memo.getForumType(),ForumService.MEMO);
    	assertEquals(memo.getForumPerm(), ForumService.OWNERONLY);
    	assertEquals(memo.getForumOwnerId(), 25556);    	
    }
    
    public void testGetMailForAccountAndCreateIfNotAvailable ()
    {
    	Forums memo = __forumService.getMailForAccountAndCreateIfNotAvailable("player1", 25556);
    	assertNotNull(memo);
    	assertEquals(memo.getForumName(), "player1");
    	assertEquals(memo.getForumType(),ForumService.MAIL);
    	assertEquals(memo.getForumPerm(), ForumService.OWNERONLY);
    	assertEquals(memo.getForumOwnerId(), 25556);    	
    }

    public void testGetClanForumAndCreateIfNotAvailable ()
    {
    	Forums memo = __forumService.getForumForClanAndCreateIfNotAvailable("clan1", 25556);
    	assertNotNull(memo);
    	assertEquals(memo.getForumName(), "clan1");
    	assertEquals(memo.getForumType(),ForumService.CLAN);
    	assertEquals(memo.getForumPerm(), ForumService.CLANMEMBERONLY);
    	assertEquals(memo.getForumOwnerId(), 25556);    	
    }
        
    
    public void testgetForumById()
	{
    	Forums fils = __forumService.getForumById(1);
    	
    	assertNotNull(fils);
    	assertEquals(fils.getForumId(), 1);
    	assertEquals(fils.getForumName(), "TestGetForumById");
	}    

    public void testgetForumByIdIfNotExistant()
	{
    	Forums fils = __forumService.getForumById(2);
    	
    	assertNull(fils);
	}    
    
    public void testgetChildForumByName()
	{
    	Forums fils = __forumService.getChildForumByName(1,"good");
    	
    	assertNotNull(fils);
    	assertEquals(fils.getForumParent(), 1);
    	assertEquals(fils.getForumName(), "good");
	}   
    
    public void testgetChildForumByNameNotExistant()
	{
    	Forums fils = __forumService.getChildForumByName(1,"bad");
    	
    	assertNull(fils);
	}      
    
    public void testCreateTopic()
	{
    	Topic topic = new Topic();
    	topic.setTopicName("good");
    	topic = __forumService.createTopic(topic);
    	
    	assertNotNull(topic);
    	assertEquals(topic.getTopicName(), "good");
    	assertEquals(topic.getTopicId(),2);
	}
    
    public void testgetTopicById()
	{
    	Topic fils = __forumService.getTopicById(1);
    	
    	assertNotNull(fils);
    	assertEquals(fils.getTopicId(), 1);
    	assertEquals(fils.getTopicName(), "good");
	}   
    
    public void testgetPaginatedTopicsByForumId()
	{
    	List<Topic> fils = __forumService.getPaginatedTopicsByForumId(1, 2, 1);
    	
    	assertNotNull(fils);
    	assertEquals(fils.size(),2);
    	assertEquals(fils.get(0).getTopicId(), 2);
    	assertEquals(fils.get(1).getTopicId(), 3);
	}    
    

    public void testGetTopicByIdIfNotExistant()
	{
    	Topic fils = __forumService.getTopicById(2);
    	
    	assertNull(fils);
	}    
    
    public void testDeleteTopic()
	{
    	Topic topic = new Topic();
    	topic.setTopicName("good");
    	
    	try
    	{
    		__forumService.deleteTopic(topic);
    	}
    	catch (Throwable e)
    	{
    		fail (e.getMessage());
    	}
    	assertTrue(true);
	}    

    public void testDeleteTopicIfNotExistant()
	{
    	Topic topic = new Topic();
    	topic.setTopicName("bad");
    	
    	try
    	{
    		__forumService.deleteTopic(topic);
    		fail("delete succeed");
    	}
    	catch (Throwable e)
    	{
    		assertNotNull(e);
    	}
	}

    public void testCreatePost()
	{
    	Posts post = new Posts();
    	post.setPostTxt("good");
    	post.setPostTopicId(1);
    	post = __forumService.createPost(post);
    	
    	assertNotNull(post);
    	assertEquals(post.getPostTxt(), "good");
    	assertEquals(post.getPostId(),2);
	}
    
    public void testgetPostById()
	{
    	Posts fils = __forumService.getPostById(1);
    	
    	assertNotNull(fils);
    	assertEquals(fils.getPostId(), 1);
    	assertEquals(fils.getPostTxt(), "good");
	}    

    public void testGetPostByIdIfNotExistant()
	{
    	Posts fils = __forumService.getPostById(2);
    	
    	assertNull(fils);
	}    
    
    public void testModifyPost()
	{
    	Posts fils = __forumService.getPostById(1);
    	fils.setPostTxt("good");
    	fils.setPostOwnerName("toto");
    	try
    	{
    		__forumService.modifyPost(fils);
    	}
    	catch (Exception e)
    	{
    		fail (e.getMessage());
    	}
	}
    

}
