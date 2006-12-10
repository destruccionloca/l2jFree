package net.sf.l2j.loginserver.beans;

// Generated 7 d�c. 2006 18:49:51 by Hibernate Tools 3.2.0.beta8

import java.math.BigDecimal;

/**
 * CharacterSubclasses generated by hbm2java
 */
public class CharacterSubclasses implements java.io.Serializable
{

    // Fields    

    private CharacterSubclassesId id;
    private BigDecimal exp;
    private long sp;
    private int level;
    private int classIndex;

    // Constructors

    /** default constructor */
    public CharacterSubclasses()
    {
    }

    /** full constructor */
    public CharacterSubclasses(CharacterSubclassesId id, BigDecimal exp, long sp, int level,
                               int classIndex)
    {
        this.id = id;
        this.exp = exp;
        this.sp = sp;
        this.level = level;
        this.classIndex = classIndex;
    }

    // Property accessors
    public CharacterSubclassesId getId()
    {
        return this.id;
    }

    public void setId(CharacterSubclassesId id)
    {
        this.id = id;
    }

    public BigDecimal getExp()
    {
        return this.exp;
    }

    public void setExp(BigDecimal exp)
    {
        this.exp = exp;
    }

    public long getSp()
    {
        return this.sp;
    }

    public void setSp(long sp)
    {
        this.sp = sp;
    }

    public int getLevel()
    {
        return this.level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public int getClassIndex()
    {
        return this.classIndex;
    }

    public void setClassIndex(int classIndex)
    {
        this.classIndex = classIndex;
    }

}
