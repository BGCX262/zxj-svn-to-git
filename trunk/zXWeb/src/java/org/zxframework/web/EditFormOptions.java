/*
 * Created on Feb 24, 2005 by Michael
 * $Id: EditFormOptions.java,v 1.1.2.6 2006/07/17 13:53:38 mike Exp $  
 */
package org.zxframework.web;

import org.zxframework.zXType;
import org.zxframework.util.ToStringBuilder;

/**
 * Represents a Edit forms options.
 * 
 * <pre>
 * 
 * DGS07APR2003: Additional parameters to capture dependency 'Restrict' info.
 * DGS20OCT2003: Added foreign key where clause
 * DGS18FEB2005 V1.4:43: Added strBoundAttr - this is the attribute that this one is bound to,
 *              in terms of its name as an attribute of this BO, not when used in FK BOs etc.
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class EditFormOptions {
	
    //------------------------ Members
	
    boolean bound = false;
    String FKFromAttr = "";
    String FKToAttr = "";
    String boundAttr = "";
    boolean restrict = false;
    String restrictFKFromAttr = "";
    zXType.pageflowDependencyOperator restrictOp = null;
    String restrictValue = "";
    String FKWhere = "";
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public EditFormOptions() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    /**
     * @return Returns the bound.
     */
    public boolean isBound() {
        return bound;
    }
    
    /**
     * @param bound The bound to set.
     */
    public void setBound(boolean bound) {
        this.bound = bound;
    }
    
    /**
     * @return Returns the boundAttr.
     */
    public String getBoundAttr() {
        return boundAttr;
    }
    
    /**
     * @param boundAttr The boundAttr to set.
     */
    public void setBoundAttr(String boundAttr) {
        this.boundAttr = boundAttr;
    }
    
    /**
     * @return Returns the fKFromAttr.
     */
    public String getFKFromAttr() {
        return FKFromAttr;
    }
    
    /**
     * @param fromAttr The fKFromAttr to set.
     */
    public void setFKFromAttr(String fromAttr) {
        FKFromAttr = fromAttr;
    }
    
    /**
     * @return Returns the fKToAttr.
     */
    public String getFKToAttr() {
        return FKToAttr;
    }
    
    /**
     * @param toAttr The fKToAttr to set.
     */
    public void setFKToAttr(String toAttr) {
        FKToAttr = toAttr;
    }
    
    /**
     * @return Returns the fKWhere.
     */
    public String getFKWhere() {
        return FKWhere;
    }
    
    /**
     * @param where The fKWhere to set.
     */
    public void setFKWhere(String where) {
        FKWhere = where;
    }
    
    /**
     * @return Returns the restrict.
     */
    public boolean isRestrict() {
        return restrict;
    }
    
    /**
     * @param restrict The restrict to set.
     */
    public void setRestrict(boolean restrict) {
        this.restrict = restrict;
    }
    
    /**
     * @return Returns the restrictFKFromAttr.
     */
    public String getRestrictFKFromAttr() {
        return restrictFKFromAttr;
    }
    
    /**
     * @param restrictFKFromAttr The restrictFKFromAttr to set.
     */
    public void setRestrictFKFromAttr(String restrictFKFromAttr) {
        this.restrictFKFromAttr = restrictFKFromAttr;
    }
    
    /**
     * @return Returns the restrictOp.
     */
    public zXType.pageflowDependencyOperator getRestrictOp() {
        return restrictOp;
    }
    
    /**
     * @param restrictOp The restrictOp to set.
     */
    public void setRestrictOp(zXType.pageflowDependencyOperator restrictOp) {
        this.restrictOp = restrictOp;
    }
    
    /**
     * @return Returns the restrictValue.
     */
    public String getRestrictValue() {
        return restrictValue;
    }
    
    /**
     * @param restrictValue The restrictValue to set.
     */
    public void setRestrictValue(String restrictValue) {
        this.restrictValue = restrictValue;
    }
    
    //------------------------ Overidden methods
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
        toString.append("bound", isBound());
        toString.append("boundAttr", getBoundAttr());
        toString.append("FKFromAttr", getFKFromAttr());
        toString.append("FKToAttr", getFKToAttr());
        toString.append("FKWhere", getFKWhere());
        toString.append("restrict", isRestrict());
        toString.append("restrictFKFromAttr", getRestrictFKFromAttr());
    	toString.append("restrictOp", zXType.valueOf(getRestrictOp()));
        toString.append("restrictValue",getRestrictValue());
        
        return toString.toString();
    }
}