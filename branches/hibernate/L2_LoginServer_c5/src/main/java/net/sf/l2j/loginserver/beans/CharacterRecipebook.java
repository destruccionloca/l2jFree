package net.sf.l2j.loginserver.beans;

// Generated 7 d�c. 2006 18:49:51 by Hibernate Tools 3.2.0.beta8

/**
 * CharacterRecipebook generated by hbm2java
 */
public class CharacterRecipebook implements java.io.Serializable
{

    // Fields    

    private CharacterRecipebookId id;
    private int type;

    // Constructors

    /** default constructor */
    public CharacterRecipebook()
    {
    }

    /** full constructor */
    public CharacterRecipebook(CharacterRecipebookId id, int type)
    {
        this.id = id;
        this.type = type;
    }

    // Property accessors
    public CharacterRecipebookId getId()
    {
        return this.id;
    }

    public void setId(CharacterRecipebookId id)
    {
        this.id = id;
    }

    public int getType()
    {
        return this.type;
    }

    public void setType(int type)
    {
        this.type = type;
    }

}
