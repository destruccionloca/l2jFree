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
package net.sf.l2j.gameserver.communitybbs.model.forum;

import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

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
    
    private int postId;
    private String postOwnerName;
    private int postOwnerid;
    private BigDecimal postDate;
    private int postTopicId;
    private String postTxt;
    

    // Constructors

    /** default constructor */
    public Posts()
    {
    }
    
    /** minimal constructor */
    public Posts(int _postId, String _postOwnerName, int _postOwnerid, BigDecimal _postDate,
                 int _postTopicId, String _postTxt)
    {
        postId = _postId;
        postOwnerName = _postOwnerName;
        postOwnerid = _postOwnerid;
        postDate = _postDate;
        postTopicId = _postTopicId;
        postTxt = _postTxt;
    }
    
    // Property accessors
    public int getPostId()
    {
        return postId;
    }

    public void setPostId(int _postId)
    {
        postId = _postId;
    }

    public String getPostOwnerName()
    {
        return postOwnerName;
    }

    public void setPostOwnerName(String _postOwnerName)
    {
        postOwnerName = _postOwnerName;
    }

    public int getPostOwnerid()
    {
        return postOwnerid;
    }

    public void setPostOwnerid(int _postOwnerid)
    {
        postOwnerid = _postOwnerid;
    }

    public BigDecimal getPostDate()
    {
        return postDate;
    }

    public void setPostDate(BigDecimal _postDate)
    {
        postDate = _postDate;
    }

    public int getPostTopicId()
    {
        return postTopicId;
    }

    public void setPostTopicId(int _postTopicId)
    {
        postTopicId = _postTopicId;
    }

    public String getPostTxt()
    {
        return postTxt;
    }

    public void setPostTxt(String _postTxt)
    {
        postTxt = _postTxt;
    }
    
    /**
     * @return the hashcode of the object
     */
    @Override
    public int hashCode() 
    {
        return new HashCodeBuilder(17,37)
                        .append(postOwnerid)
                        .append(postTopicId)
                        .append(postDate)
                        .toHashCode();
    }
    
    /**
     * @return true or false if the two objects are equals (not based on post id)
     * @param obj
     */
    @Override
    public boolean equals(Object _obj) 
    {
        if (_obj == null) 
        {
            return false;
        }
        if (this == _obj) 
        {
            return true;
        }
        Posts rhs = (Posts) _obj;
        return new EqualsBuilder()
                        .appendSuper(super.equals(_obj))
                        .append(postOwnerid, rhs.getPostOwnerid())
                        .append(postTopicId, rhs.getPostTopicId())
                        .append(postDate, rhs.getPostDate())
                        .isEquals();        
    }
    
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}