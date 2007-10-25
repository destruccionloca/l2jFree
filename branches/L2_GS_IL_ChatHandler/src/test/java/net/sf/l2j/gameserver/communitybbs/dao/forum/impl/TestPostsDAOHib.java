package net.sf.l2j.gameserver.communitybbs.dao.forum.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.communitybbs.model.forum.Posts;
import net.sf.l2j.tools.db.hibernate.ADAOTestCase;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

/**
 * 
 */
public class TestPostsDAOHib extends ADAOTestCase
{
	/**
	 * DAO to test
	 */
	private PostsDAOHib __postsDAO = null;


    public TestPostsDAOHib(String name)
    {
    	super(name);
    }

	@SuppressWarnings("deprecation")
	public void setUp() throws Exception
    {
    	super.setUp();
    	// Set DAO to test
    	setPostsDAO(new PostsDAOHib());
    	getPostsDAO().setCurrentSession(getSession());
    }

	public String[] getMappings()
    {
    	return new String [] {"Forums.hbm.xml","Topic.hbm.xml", "Posts.hbm.xml"};
    }


    /**
     * Test method for
     * {@link net.sf.l2j.gameserver.dao.impl.PostsDAO#getForumById(int)}.
     */
    public void testFindById()
    {
    	Posts post = getPostsDAO().getPostById(1);
    	assertNotNull(post);
    	assertEquals(1, post.getPostId());
    }

	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.PostsDAO#createPosts()}.
	 */
    public void testCreatePosts()
    {
    	// note that topic id is not important because we use increment strategies 
    	// for topic ID
    	Posts post = new Posts();
    	post.setPostTopicId(2);
    	post.setPostId(-1);
    	post.setPostOwnerid(666);
    	post.setPostOwnerName("one player");
    	
    	assertEquals(2,getPostsDAO().createPost(post));
    }   
    
    /**
     * @return the postsDAO
     */
    public PostsDAOHib getPostsDAO()
    {
    	return __postsDAO;
    }

    /**
     * @param _postsDAO
     * the postsDAO to set
     */
    public void setPostsDAO(PostsDAOHib _postsDAO)
    {
    	__postsDAO = _postsDAO;
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
