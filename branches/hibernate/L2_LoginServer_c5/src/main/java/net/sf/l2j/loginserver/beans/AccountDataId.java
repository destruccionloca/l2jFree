package net.sf.l2j.loginserver.beans;

// Generated 8 d�c. 2006 15:26:57 by Hibernate Tools 3.2.0.beta8

/**
 * AccountDataId generated by hbm2java
 */
public class AccountDataId implements java.io.Serializable
{

    // Fields    

    private String accountName;
    private String var;

    // Constructors

    /** default constructor */
    public AccountDataId()
    {
    }

    /** full constructor */
    public AccountDataId(String accountName, String var)
    {
        this.accountName = accountName;
        this.var = var;
    }

    // Property accessors
    public String getAccountName()
    {
        return this.accountName;
    }

    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }

    public String getVar()
    {
        return this.var;
    }

    public void setVar(String var)
    {
        this.var = var;
    }

    public boolean equals(Object other)
    {
        if ((this == other)) return true;
        if ((other == null)) return false;
        if (!(other instanceof AccountDataId)) return false;
        AccountDataId castOther = (AccountDataId) other;

        return ((this.getAccountName() == castOther.getAccountName()) || (this.getAccountName() != null
            && castOther.getAccountName() != null && this.getAccountName().equals(
                                                                                  castOther.getAccountName())))
            && ((this.getVar() == castOther.getVar()) || (this.getVar() != null
                && castOther.getVar() != null && this.getVar().equals(castOther.getVar())));
    }

    public int hashCode()
    {
        int result = 17;

        result = 37 * result + (getAccountName() == null ? 0 : this.getAccountName().hashCode());
        result = 37 * result + (getVar() == null ? 0 : this.getVar().hashCode());
        return result;
    }

}
