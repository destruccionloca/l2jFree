package net.sf.l2j.gameserver.beans;

// Generated 19 f�vr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8

/**
 * Posts generated by hbm2java
 */
public class Posts implements java.io.Serializable
{

    // Fields    

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -2197088778138212029L;
    private PostsId id;
    private Forums forums;
    private Topic topic;

    // Constructors

    /** default constructor */
    public Posts()
    {
    }

    /** full constructor */
    public Posts(PostsId _id, Forums _forums, Topic _topic)
    {
        this.id = _id;
        this.forums = _forums;
        this.topic = _topic;
    }

    // Property accessors
    public PostsId getId()
    {
        return this.id;
    }

    public void setId(PostsId _id)
    {
        this.id = _id;
    }

    public Forums getForums()
    {
        return this.forums;
    }

    public void setForums(Forums _forums)
    {
        this.forums = _forums;
    }
    public Topic getTopic()
    {
        return this.topic;
    }

    public void setTopic(Topic _topic)
    {
        this.topic = _topic;
    }    

}
