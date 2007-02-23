package net.sf.l2j.gameserver.dao.forum.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.gameserver.dao.forum.ForumsDAO;
import net.sf.l2j.gameserver.model.forum.Forums;
import net.sf.l2j.tools.hibernate.ADAOTestCase;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

/**
 * 
 */
public class TestForumsDAOHib extends ADAOTestCase
{
	/**
	 * DAO to test
	 */
	private ForumsDAOHib __postsDAO = null;


    public TestForumsDAOHib(String name)
    {
    	super(name);
    }

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
    	//Forums forum = getForumsDAO().getForumById(6);
    	//assertNotNull(forum);
    	//assertEquals(6, forum.getForumId());
    }

	/**
	 * Test method for {@link
	 * net.sf.l2j.gameserver.dao.impl.ForumsDAO#getForumByName(String)}.
	 */
    public void testFindByName()
    {
    	Forums forum = getForumsDAO().getForumByName("Kyor");
    	assertNotNull(forum);
    	assertEquals("Kyor", forum.getForumName());
    }
    
    /**
     * @return the postsDAO
     */
    public ForumsDAOHib getForumsDAO()
    {
    	return __postsDAO;
    }

    /**
     * @param _postsDAO
     * the postsDAO to set
     */
    public void setForumsDAO(ForumsDAOHib _postsDAO)
    {
    	__postsDAO = _postsDAO;
    }

    protected List<IDataSet> getDataSet() throws Exception
    {
    	String [] dataSetNameList = {"forums.xml","topic.xml","posts.xml"};
    	String dtdName = "database/l2jdb.dtd";
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

	/**
	 * @return the postsDAO
	 */
	public ForumsDAO getpostsDAO() {
		return __postsDAO;
	}
  	
}
