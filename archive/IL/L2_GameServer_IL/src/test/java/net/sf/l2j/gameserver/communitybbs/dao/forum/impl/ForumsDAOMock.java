package net.sf.l2j.gameserver.communitybbs.dao.forum.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO;
import net.sf.l2j.gameserver.communitybbs.model.forum.Forums;
import net.sf.l2j.gameserver.communitybbs.model.forum.Topic;

// Generated 19 févr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8


/**
 * DAO object for domain model class Forums.
 * @see net.sf.l2j.gameserver.communitybbs.model.forum.Forums
 */
public class ForumsDAOMock implements ForumsDAO
{

    /**
     * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#getAllForums()
     */
    @SuppressWarnings("unchecked")
    public List<Forums> getAllForums()
    {
        List<Forums> forums = new ArrayList<Forums>();
        
        Forums forum1 = new Forums();
        forum1.setForumId(1);
        forum1.setForumName("CLANROOT");
        forum1.setForumOwnerId(3);
        forum1.setForumPerm(2);
        
        forums.add(forum1);
        
        Forums forum2 = new Forums();
        forum2.setForumId(2);
        forum2.setForumName("MEMOROOT");
        forum2.setForumOwnerId(4);
        forum2.setForumPerm(3);        
        
        forums.add(forum2);
        return forums;
    }
    
    
    /**
     * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#createForums(net.sf.l2j.gameserver.communitybbs.model.forum.Forums)
     */
    public int createForums(Forums obj)
    {
    	if (obj.getForumName().equals("TestForum"))
    		return 1;
    	else
    		return -1;         
    }

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#getChildrens(net.sf.l2j.gameserver.communitybbs.model.forum.Forums)
	 */
	@SuppressWarnings("unchecked")
	public List<Forums> getChildrens(Forums obj)
	{
        List<Forums> forums = new ArrayList<Forums>();
        
        Forums forum1 = new Forums();
        forum1.setForumId(1);
        forum1.setForumName("CLANROOT");
        forum1.setForumOwnerId(3);
        forum1.setForumPerm(2);
        
        forums.add(forum1);
        
        Forums forum2 = new Forums();
        forum2.setForumId(2);
        forum2.setForumName("MEMOROOT");
        forum2.setForumOwnerId(4);
        forum2.setForumPerm(3);        
        
        forums.add(forum2);
    	return forums;	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#getChildrens(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Forums> getChildrens(Integer forumId)
	{
        List<Forums> forums = new ArrayList<Forums>();
        
        Forums forum1 = new Forums();
        forum1.setForumId(1);
        forum1.setForumName("CLANROOT");
        forum1.setForumOwnerId(3);
        forum1.setForumPerm(2);
        
        forums.add(forum1);
        
        Forums forum2 = new Forums();
        forum2.setForumId(2);
        forum2.setForumName("MEMOROOT");
        forum2.setForumOwnerId(4);
        forum2.setForumPerm(3);        
        
        forums.add(forum2);
    	return forums;	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#getForumById(java.lang.Integer)
	 */
	public Forums getForumById(Integer id)
	{
        Forums forum1 = new Forums();
		if ( id != 2)
		{
	        forum1.setForumId(id);
	        forum1.setForumName("TestGetForumById");
	        forum1.setForumOwnerId(3);
	        forum1.setForumPerm(2);
		}
		else
		{
			throw new RuntimeException("Unable to find forums (Mock)");
		}
		return forum1;
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#deleteForum(net.sf.l2j.gameserver.communitybbs.model.forum.Forums)
	 */
	public void deleteForum(Forums obj)
	{
		if (obj.getForumId()==1)
			throw new RuntimeException ("unable to delete forums (Mock)");
	}

	/**
	 * @see net.sf.l2j.gameserver.communitybbs.dao.forum.ForumsDAO#getChildForumByName(java.lang.Integer, java.lang.String)
	 */
	public Forums getChildForumByName(Integer forumId, String name)
	{
        Forums forum1 = new Forums();
		if (name.equals("good"))
		{
			forum1.setForumId(1);
	        forum1.setForumParent(forumId);
	        forum1.setForumName(name);
	        forum1.setForumOwnerId(25556);
	        forum1.setForumPerm(2);
	        forum1.setForumType(3);
		}
		else
		{
			throw new RuntimeException("Unable to find forums (Mock)");
		}
		return forum1;	
	}

    public Set<Topic> getTopicsForForum(Forums obj)
    {
        return obj.getTopics();
    }


}
