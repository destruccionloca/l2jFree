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

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.communitybbs.model.forum.Forums;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;
import net.sf.l2j.tools.db.hibernate.ADAOTestCase;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * 
 */
public class TestForumsDAOHib extends ADAOTestCase
{
	/**
	 * DAO to test
	 */
	private ForumsDAOHib __forumsDAO = null;


    public TestForumsDAOHib(String name)
    {
    	super(name);
    }

	@SuppressWarnings("deprecation")
	public void setUp() throws Exception
    {
    	super.setUp();
    	// Set DAO to test
    	setForumsDAO(new ForumsDAOHib());
    	getForumsDAO().setCurrentSession(getSession());
    }

	public String[] getMappings()
    {
    	return new String [] {"Forums.hbm.xml","Posts.hbm.xml", "Topic.hbm.xml"};
    }


    /**
     * Test method for
     * {@link net.sf.l2j.gameserver.dao.impl.ForumsDAO#getForumById(int)}.
     */
    public void testFindById()
    {
    	Forums forum = getForumsDAO().getForumById(6);
    	assertNotNull(forum);
    	assertEquals(6, forum.getForumId());
    }

	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#getChildrens(Forums)}.
	 */
    public void testFindChildrenByEntity()
    {
    	Forums forum = getForumsDAO().getForumById(2);
    	assertNotNull(forum);
    	assertEquals("ClanRoot", forum.getForumName());
    	assertEquals(2,getForumsDAO().getChildrens(forum).size());
    }
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#getChildForumByName(Integer,String)}.
	 */
    public void testFindChildrenByName()
    {
    	Forums forum = getForumsDAO().getForumById(2);
    	assertNotNull(forum);
    	assertEquals("ClanRoot", forum.getForumName());
    	Forums fils = getForumsDAO().getChildForumByName(forum.getForumId(), "Kyor");
    	assertNotNull(fils);
    	assertEquals("Kyor", fils.getForumName());
    	assertEquals(forum.getForumId(), fils.getForumParent());
    }
    
    /**
     * Test method for {@link
     * net.sf.l2j.gameserver.dao.impl.ForumsDAO#getChildForumByName(Integer,String)}.
     */
    public void testFindChildrenByNameWithQuote()
    {
        Forums forum = getForumsDAO().getForumById(2);
        assertNotNull(forum);
        assertEquals("ClanRoot", forum.getForumName());
        // Test should not fail even if there is a quote in the request
        Forums fils = getForumsDAO().getChildForumByName(forum.getForumId(), "Kyor'Khel");
        assertNotNull(fils);
        assertEquals("Kyor'Khel", fils.getForumName());
    }    
    
	/**
	 * Test method for getTopics and getPosts
	 */
    public void testFindChildTopics()
    {
    	Forums forum = getForumsDAO().getForumById(6);
    	assertNotNull(forum);
    	assertEquals(6, forum.getForumId());
    	assertEquals(3,forum.getTopics().size());
    }
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#getChildrens(Integer)}.
	 */
    public void testFindChildrenByKey()
    {
    	assertEquals(2,getForumsDAO().getChildrens(2).size());
    }   
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#getAllForums()}.
	 */
    public void testFindAllForums()
    {
    	assertEquals(6,getForumsDAO().getAllForums().size());
    }      
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#createForum()}.
	 */
    public void testCreateForum()
    {
    	// note that forum is not important because we use increment strategies 
    	// for forum ID
    	Forums forums = new Forums(-1,"clanTest",2,0,1,1,25555);
    	assertEquals(7,getForumsDAO().createForums(forums));
        // flush session to persist entity created 
        getForumsDAO().getCurrentSession().flush();
    	assertEquals(7,getForumsDAO().getAllForums().size());
    	assertEquals(3,getForumsDAO().getChildrens(2).size());
    }   
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#createForum()}.
	 */
    public void testCreateForumAndAddTopic()
    {
    	// note that forum is not important because we use increment strategies 
    	// for forum ID
    	Forums forums = new Forums(-1,"clanTest",2,0,1,1,25555);
    	assertEquals(7,getForumsDAO().createForums(forums));
//       flush session to persist entity created 
        getForumsDAO().getCurrentSession().flush();
    	assertEquals(7,getForumsDAO().getAllForums().size());
    	assertEquals(3,getForumsDAO().getChildrens(2).size());
    	
    	Topic topic = new Topic();
    	topic.setTopicForumId(7);
    	topic.setTopicName("topic test");
    	topic.setTopicOwnerid(9);
    	topic.setTopicOwnername("a player");
    	topic.setTopicDate(new BigDecimal(11112588));
    	
    	// add topic
    	forums.getTopics().add(topic);
    	
    	getForumsDAO().saveOrUpdate(forums);
    	
    	// just to be sure that the session is flushed 
    	getForumsDAO().getCurrentSession().flush();
    	
    	forums = getForumsDAO().getForumById(7);
    	assertEquals(1,forums.getTopics().size());
    }       
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#deleteForum()}.
	 */
    public void testDeleteForumWithCascade()
    {
    	Forums forum = getForumsDAO().getForumById(6);
    	assertNotNull(forum);
    	assertEquals(6, forum.getForumId());
    	
    	getForumsDAO().deleteForum(forum);
    	// flush session to persist entity created 
        getForumsDAO().getCurrentSession().flush();
    	try
    	{
    		forum = getForumsDAO().getForumById(6);
    		fail("Forums found but it should be deleted.");
    	}
    	catch (ObjectRetrievalFailureException e)
    	{
    		assertNotNull(e);
    	}
    	
    	// check that children were erased
    	assertEquals(0,getForumsDAO().getCurrentSession().createQuery("from "+Topic.class.getName()+" where topicForumId="+6).list().size());
    }     
    
    /**
     * @return the forumsDAO
     */
    public ForumsDAOHib getForumsDAO()
    {
    	return __forumsDAO;
    }

    /**
     * @param _postsDAO
     * the postsDAO to set
     */
    public void setForumsDAO(ForumsDAOHib _postsDAO)
    {
    	__forumsDAO = _postsDAO;
    }

    protected List<IDataSet> getDataSet() throws Exception
    {
    	String [] dataSetNameList = {"forums.xml","topic.xml","posts.xml"};
    	String dtdName = "/l2jdb.dtd";
    	List<IDataSet> dataSetList = new ArrayList<IDataSet>();
	
    	InputStream inDTD = this.getClass().getResourceAsStream(dtdName);
    	FlatDtdDataSet dtdDataSet = new FlatDtdDataSet(inDTD);
    	for(int indice=0; indice<dataSetNameList.length; indice++)
    	{
    		InputStream in = this.getClass().getResourceAsStream(dataSetNameList[indice]);
    		IDataSet dataSet = new FlatXmlDataSet(in, dtdDataSet);
    		dataSetList.add(dataSet);
    	}
    	return dataSetList;
    }
 	
}
