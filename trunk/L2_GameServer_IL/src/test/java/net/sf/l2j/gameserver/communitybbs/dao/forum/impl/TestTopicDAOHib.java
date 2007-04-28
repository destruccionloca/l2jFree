package net.sf.l2j.gameserver.communitybbs.dao.forum.impl;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;
import net.sf.l2j.tools.db.hibernate.ADAOTestCase;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * 
 */
public class TestTopicDAOHib extends ADAOTestCase
{
	/**
	 * DAO to test
	 */
	private TopicDAOHib __topicsDAO = null;


    public TestTopicDAOHib(String name)
    {
    	super(name);
    }

	@SuppressWarnings("deprecation")
	public void setUp() throws Exception
    {
    	super.setUp();
    	// Set DAO to test
    	setTopicDAO(new TopicDAOHib());
    	getTopicDAO().setCurrentSession(getSession());
    }

	public String[] getMappings()
    {
    	return new String [] {"Forums.hbm.xml","Posts.hbm.xml", "Topic.hbm.xml"};
    }


    /**
     * Test method for
     * {@link net.sf.l2j.gameserver.dao.impl.TopicDAO#getForumById(int)}.
     */
    public void testFindById()
    {
    	Topic topic = getTopicDAO().getTopicById(1);
    	assertNotNull(topic);
    	assertEquals(1, topic.getTopicId());
    }

	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.TopicDAO#getTopicByName(String)}.
	 */
    public void testFindByName()
    {
    	Topic topic = getTopicDAO().getTopicByName("TopicTest");
    	assertNotNull(topic);
    	assertEquals("TopicTest", topic.getTopicName());
    }
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.TopicDAO#getChildrens(Topic)}.
	 */
    public void testFindChildrenByEntity()
    {
    	Topic topic = getTopicDAO().getTopicByName("TopicTest");
    	assertNotNull(topic);
    	assertEquals("TopicTest", topic.getTopicName());
    	assertEquals(1,topic.getPostses().size());
    }
    
    
	/**
	 * Test method for getTopics and getPosts
	 */
    public void testFindChildTopicsAndPosts()
    {
    	Topic topic = getTopicDAO().getTopicById(1);
    	assertNotNull(topic);
    	assertEquals(1, topic.getTopicId());
    	assertEquals(1,topic.getPostses().size());
    }
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.TopicDAO#createTopic()}.
	 */
    public void testCreateTopic()
    {
    	// note that topic id is not important because we use increment strategies 
    	// for topic ID
    	Topic topic = new Topic();
    	topic.setTopicForumId(6);
    	topic.setTopicId(-1);
    	topic.setTopicName("testCreateTopic");
    	topic.setTopicOwnerid(666);
    	topic.setTopicOwnername("one player");
    	
    	assertEquals(5,getTopicDAO().createTopic(topic));
    }   
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.TopicDAO#createTopic()}.
	 */
    public void testCreateTopicAndAddPosts()
    {
    	// note that topic is not important because we use increment strategies 
    	// for topic ID
    	Topic topic = new Topic();
    	topic.setTopicForumId(6);
    	topic.setTopicId(0);
    	topic.setTopicName("testCreateTopic");
    	topic.setTopicOwnerid(666);
    	topic.setTopicOwnername("one player");
    	getTopicDAO().createTopic(topic);
    	
    	Posts posts = new Posts();
    	posts.setPostId(0);
    	posts.setPostDate(new BigDecimal(122));
    	posts.setPostTopicId(2);
    	posts.setPostOwnerid(666);
    	posts.setPostOwnerName("one player");
    	posts.setPostTxt("a text");
    	
    	// add post
    	topic.getPostses().add(posts);
    	
    	getTopicDAO().save(topic);
    	
    	// just to be sure that the session is flushed 
    	getTopicDAO().getCurrentSession().flush();
    	
    	topic = getTopicDAO().getTopicById(2);
    	assertEquals(1,topic.getPostses().size());
    }       
    
    public void testGetPaginatedTopics ()
    {
    	// Get page 2 with 1 topic per page for forum id 6
    	List<Topic> topics = getTopicDAO().getPaginatedTopicByForumId(1, 2, 6);
    	// we should have 1 result
    	assertEquals(1,topics.size());
    	// the topic id should be the second
    	assertEquals(3,topics.get(0).getTopicId());
    }
    
	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.TopicDAO#deleteTopic()}.
	 */
    public void testDeleteTopicWithCascade()
    {
    	Topic topic = getTopicDAO().getTopicById(1);
    	assertNotNull(topic);
    	assertEquals(1, topic.getTopicId());
    	
    	getTopicDAO().deleteTopic(topic);
    	
    	try
    	{
    		topic = getTopicDAO().getTopicById(1);
    		fail("Topic found but it should be deleted.");
    	}
    	catch (ObjectRetrievalFailureException e)
    	{
    		assertNotNull(e);
    	}
        getTopicDAO().getCurrentSession().flush();
    	// check that children were erased
    	assertEquals(0,getTopicDAO().getCurrentSession().createQuery("from "+Posts.class.getName()+" where postTopicId="+1).list().size());
    }       
    
    /**
     * @return the postsDAO
     */
    public TopicDAOHib getTopicDAO()
    {
    	return __topicsDAO;
    }

    /**
     * @param _postsDAO
     * the postsDAO to set
     */
    public void setTopicDAO(TopicDAOHib _postsDAO)
    {
    	__topicsDAO = _postsDAO;
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
