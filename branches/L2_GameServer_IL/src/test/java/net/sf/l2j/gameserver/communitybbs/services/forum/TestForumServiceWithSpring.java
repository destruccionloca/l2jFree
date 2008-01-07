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

import net.sf.l2j.gameserver.communitybbs.model.forum.Forums;
import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;
import net.sf.l2j.gameserver.registry.IServiceRegistry;
import net.sf.l2j.tools.db.spring.ADAOTestWithSpringAndDerby;

/**
 * This class test ban management
 * 
 */
public class TestForumServiceWithSpring extends ADAOTestWithSpringAndDerby
{
    private ForumService __fs=null;

    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        __fs = (ForumService)getBean(IServiceRegistry.FORUM);
        assertNotNull(__fs);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public String getInitialDataSetName()
    {
        return "net/sf/l2j/gameserver/communitybbs/services/forum/initialForum.xml";
    }

    public String getRootDirName()
    {
        return null;
    }

    public String getDtdName()
    {
        return "/l2jdb.dtd";
    }
    
    /**
     * Test method for {@link
     * net.sf.l2j.gameserver.dao.impl.ForumsDAO#createForum()}.
     */
    public void testGetNumberOfTopic()
    {
        Forums forum = __fs.getForumById(6);
        assertEquals(3,__fs.getTopicNumberForForum(forum));
    }   
    
    public void testGetPostByIndexForTopic ()
    {
        Topic topic = __fs.getTopicById(1);
        assertEquals("TopicTest",topic.getTopicName());
        topic.getPostses();
        
        Posts firstPost = __fs.getPostByIndexForTopic(topic,0);
        assertNotNull(firstPost);
    }
    
    public void testCreateForum ()
    {
        __fs.createForum("test", 5, 4, 3, 1);
        
        assertEqualsDatabaseDataSet("net/sf/l2j/gameserver/communitybbs/services/forum/afterForumCreation.xml");
    }
   
}
