package net.sf.l2j.loginserver.beans;

// Generated 7 d�c. 2006 18:49:51 by Hibernate Tools 3.2.0.beta8

/**
 * CharacterQuests generated by hbm2java
 */
public class CharacterQuests implements java.io.Serializable
{

    // Fields    

    private CharacterQuestsId id;
    private String value;

    // Constructors

    /** default constructor */
    public CharacterQuests()
    {
    }

    /** minimal constructor */
    public CharacterQuests(CharacterQuestsId id)
    {
        this.id = id;
    }

    /** full constructor */
    public CharacterQuests(CharacterQuestsId id, String value)
    {
        this.id = id;
        this.value = value;
    }

    // Property accessors
    public CharacterQuestsId getId()
    {
        return this.id;
    }

    public void setId(CharacterQuestsId id)
    {
        this.id = id;
    }

    public String getValue()
    {
        return this.value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

}
