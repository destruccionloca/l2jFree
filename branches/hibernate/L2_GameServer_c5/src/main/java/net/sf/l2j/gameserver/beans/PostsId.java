package net.sf.l2j.gameserver.beans;

// Generated 19 f�vr. 2007 22:07:55 by Hibernate Tools 3.2.0.beta8

import java.math.BigDecimal;

/**
 * PostsId generated by hbm2java
 */
public class PostsId implements java.io.Serializable
{

    // Fields    

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3341904290078478681L;
    private int postId;
    private String postOwnerName;
    private int postOwnerid;
    private BigDecimal postDate;
    private int postTopicId;
    private int postForumId;
    private String postTxt;

    // Constructors

    /** default constructor */
    public PostsId()
    {
    }

    /** full constructor */
    public PostsId(int _postId, String _postOwnerName, int _postOwnerid, BigDecimal _postDate,
                   int _postTopicId, int _postForumId, String _postTxt)
    {
        this.postId = _postId;
        this.postOwnerName = _postOwnerName;
        this.postOwnerid = _postOwnerid;
        this.postDate = _postDate;
        this.postTopicId = _postTopicId;
        this.postForumId = _postForumId;
        this.postTxt = _postTxt;
    }

    // Property accessors
    public int getPostId()
    {
        return this.postId;
    }

    public void setPostId(int _postId)
    {
        this.postId = _postId;
    }

    public String getPostOwnerName()
    {
        return this.postOwnerName;
    }

    public void setPostOwnerName(String _postOwnerName)
    {
        this.postOwnerName = _postOwnerName;
    }

    public int getPostOwnerid()
    {
        return this.postOwnerid;
    }

    public void setPostOwnerid(int _postOwnerid)
    {
        this.postOwnerid = _postOwnerid;
    }

    public BigDecimal getPostDate()
    {
        return this.postDate;
    }

    public void setPostDate(BigDecimal _postDate)
    {
        this.postDate = _postDate;
    }

    public int getPostTopicId()
    {
        return this.postTopicId;
    }

    public void setPostTopicId(int _postTopicId)
    {
        this.postTopicId = _postTopicId;
    }

    public int getPostForumId()
    {
        return this.postForumId;
    }

    public void setPostForumId(int _postForumId)
    {
        this.postForumId = _postForumId;
    }

    public String getPostTxt()
    {
        return this.postTxt;
    }

    public void setPostTxt(String _postTxt)
    {
        this.postTxt = _postTxt;
    }

    public boolean equals(Object other)
    {
        if ((this == other)) return true;
        if ((other == null)) return false;
        if (!(other instanceof PostsId)) return false;
        PostsId castOther = (PostsId) other;

        return (this.getPostId() == castOther.getPostId())
            && ((this.getPostOwnerName() == castOther.getPostOwnerName()) || (this.getPostOwnerName() != null
                && castOther.getPostOwnerName() != null && this.getPostOwnerName().equals(
                                                                                          castOther.getPostOwnerName())))
            && (this.getPostOwnerid() == castOther.getPostOwnerid())
            && ((this.getPostDate() == castOther.getPostDate()) || (this.getPostDate() != null
                && castOther.getPostDate() != null && this.getPostDate().equals(castOther.getPostDate())))
            && (this.getPostTopicId() == castOther.getPostTopicId())
            && (this.getPostForumId() == castOther.getPostForumId())
            && ((this.getPostTxt() == castOther.getPostTxt()) || (this.getPostTxt() != null
                && castOther.getPostTxt() != null && this.getPostTxt().equals(castOther.getPostTxt())));
    }

    public int hashCode()
    {
        int result = 17;

        result = 37 * result + this.getPostId();
        result = 37 * result + (getPostOwnerName() == null ? 0 : this.getPostOwnerName().hashCode());
        result = 37 * result + this.getPostOwnerid();
        result = 37 * result + (getPostDate() == null ? 0 : this.getPostDate().hashCode());
        result = 37 * result + this.getPostTopicId();
        result = 37 * result + this.getPostForumId();
        result = 37 * result + (getPostTxt() == null ? 0 : this.getPostTxt().hashCode());
        return result;
    }

}
