/*
 * Created on Jan 13, 2004 by michael
 * $Id: SQLTable.java,v 1.1.2.6 2005/08/22 08:08:34 mike Exp $
 */
package org.zxframework.sql;

import org.zxframework.ZXBO;
import org.zxframework.util.ToStringBuilder;

/**
 * Table object, used in SQL object to generate queries.
 * 
 * <pre>
 * 
 * NOTE : alias is also the key of this object 
 * 
 * Change    : BD29MAR05 - V1.5:1
 * Why       : No longer any need for handle to zX, see init method; has minor performance gain
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class SQLTable {
    
    //------------------------ Members
    
    private String alias;
    private String name;
    private ZXBO bo;
    private boolean doneOuterJoinYet;
    
    //------------------------ Constructors
    
    /**
     * Default constructor. We do not need zx from the class.
     */
    public SQLTable() {
        super();
    }

    //------------------------ Getter and Setter 
    
    /**
     * The business object linked to this table.
     *  
     * @return Returns the bo.
     */
    public ZXBO getBo() {
        return bo;
    }
    
    /**
     * @param bo The bo to set.
     */
    public void setBo(ZXBO bo) {
        this.bo = bo;
    }
    
    /**
     * @return Returns the alias.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias The alias to set.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * The actual name of the table. 
     * 
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Whether we have performed a SQL outer join on the SQLTable yet.
     * 
     * @return Returns the doneOuterJoinYet.
     */
    public boolean isDoneOuterJoinYet() {
        return doneOuterJoinYet;
    }
    
    /**
     * @param doneOuterJoinYet The doneOuterJoinYet to set.
     */
    public void setDoneOuterJoinYet(boolean doneOuterJoinYet) {
        this.doneOuterJoinYet = doneOuterJoinYet;
    }
    
    //------------------------ Overidden methods
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);

        toString.append("name", this.name);
        toString.append("doneOuterJoinYet", this.doneOuterJoinYet);
        
        if (this.bo != null) {
            toString.append("bo", this.bo.getDescriptor().getName());
        }
        
        return toString.toString();
    }
}