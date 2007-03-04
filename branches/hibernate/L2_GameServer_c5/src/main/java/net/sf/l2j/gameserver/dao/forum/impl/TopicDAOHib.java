package net.sf.l2j.gameserver.dao.forum.impl;

import java.util.List;

import net.sf.l2j.gameserver.dao.forum.TopicDAO;
import net.sf.l2j.gameserver.model.forum.Forums;
import net.sf.l2j.gameserver.model.forum.Topic;

// Generated 19 févr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8


/**
 * Home object for domain model class Topic.
 * @see net.sf.l2j.gameserver.model.forum.Topic
 */
public class TopicDAOHib extends BaseRootDAOHib implements TopicDAO
{

	/**
	 * @see net.sf.l2j.gameserver.dao.forum.TopicDAO#createTopic(net.sf.l2j.gameserver.model.forum.Topic)
	 */
	public int createTopic(Topic obj)
	{
        return (Integer)save(obj);        
	}

	/**
	 * @see net.sf.l2j.gameserver.dao.forum.TopicDAO#deleteTopic(net.sf.l2j.gameserver.model.forum.Topic)
	 */
	public void deleteTopic(Topic obj)
	{
		Forums forums = obj.getForums();
		forums.getTopics().remove(obj);
		getCurrentSession().save(forums);
		obj.setForums(forums);
		delete(obj);		
	}

	/**
	 * @see net.sf.l2j.gameserver.dao.forum.TopicDAO#getTopicById(java.lang.Integer)
	 */
	public Topic getTopicById(Integer id)
	{
		return (Topic)get(Topic.class, id);
	}

	/**
	 * @see net.sf.l2j.gameserver.dao.forum.TopicDAO#getTopicByName(java.lang.String)
	 */
	public Topic getTopicByName(String name)
	{
		return (Topic)getCurrentSession().createQuery(
				"from " + Topic.class.getName()+ " where topicName = '"+name+"'").uniqueResult();
	}

	/**
	 * @see net.sf.l2j.gameserver.dao.forum.TopicDAO#getPaginatedTopicById(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Topic> getPaginatedTopicByForumId(Integer iPageSize, Integer iIdx, Integer id)
	{
		return (List <Topic>)getCurrentSession().createQuery(
				"from " + Topic.class.getName()+ " where topicForumId = "+id).setMaxResults(iPageSize).setFirstResult(iPageSize*iIdx).list();
	}
}
