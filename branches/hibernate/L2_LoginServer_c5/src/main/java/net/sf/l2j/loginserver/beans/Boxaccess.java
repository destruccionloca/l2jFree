package net.sf.l2j.loginserver.beans;

// Generated 7 d�c. 2006 18:49:51 by Hibernate Tools 3.2.0.beta8

/**
 * Boxaccess generated by hbm2java
 */
public class Boxaccess implements java.io.Serializable
{

    // Fields    

    private BoxaccessId id;
    private Characters characters;

    // Constructors

    /** default constructor */
    public Boxaccess()
    {
    }

    /** minimal constructor */
    public Boxaccess(BoxaccessId id)
    {
        this.id = id;
    }

    /** full constructor */
    public Boxaccess(BoxaccessId id, Characters characters)
    {
        this.id = id;
        this.characters = characters;
    }

    // Property accessors
    public BoxaccessId getId()
    {
        return this.id;
    }

    public void setId(BoxaccessId id)
    {
        this.id = id;
    }

    public Characters getCharacters()
    {
        return this.characters;
    }

    public void setCharacters(Characters characters)
    {
        this.characters = characters;
    }

}
