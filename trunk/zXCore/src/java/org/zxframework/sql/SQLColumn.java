/*
 * Created on Jan 14, 2004 by michael
 * $Id: SQLColumn.java,v 1.1.2.5 2005/07/12 07:28:55 mike Exp $
 */
package org.zxframework.sql;

import org.zxframework.ZXBO;
import org.zxframework.util.ToStringBuilder;

/**
 * Support object only used by zx.clsSQL to generate queries.
 * 
 * Note : getAlias and setAlias are the key of this object :)
 * 
 * <pre>
 * 
 * Change    : BD29MAR05 - V1.5:1
 * Why       : No longer any need for handle to zX, see init method; has minor performance gain
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class SQLColumn {

    //------------------------ Members
    
    /** The alias of the column **/
    private String alias;
    /** The actual name of the column. **/
    private String name;
    /** The original attribute name. **/
    private String attrName;
    /** The business object linked to this column. **/
    private ZXBO bo;

    //------------------------ Constructor
   
    /**
     * Default constructor.
     */
    public SQLColumn() { 
        super();
    }
    
    //------------------------ Getter and Setters
    
    /**
     * Alias of column
     * 
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
        // this.key = alias;
    }

    /**
     * Name of attribute that column belongs to
     * 
     * @return Returns the attrName.
     */
    public String getAttrName() {
        return attrName;
    }

    /**
     * @param attrName The attrName to set.
     */
    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    /**
     * Original name of column
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
     * @return Returns the handle to BO that the column belongs to
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
    
    //------------------------ Overidden methods
    
    /**
     * @see java.lang.Object#toString()
     */    
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);

        toString.append("attrName", this.attrName);
        toString.append("name", this.name);
        if (this.bo != null) {
            toString.append("bo", this.bo.getDescriptor().getName());
        }
        
        return toString.toString();
    }
}