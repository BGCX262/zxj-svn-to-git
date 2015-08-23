/*
 * Created on Aug 24, 2004
 * $Id: BORelation.java,v 1.1.2.7 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import org.zxframework.util.ToStringBuilder;

/**
 *Class to maintain BO relation details.
 *
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 27 March 2004
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class BORelation {
    
    //------------------------ Members
    
    private String entity;
    private String FKAttr;
    private zXType.deleteRule deleteRule;
    
    //------------------------ Constructor
    
    /**
     * Default constructor.
     */
    public BORelation() {
    	super();
    }
    
    //------------------------ Getters and Setters
    
    /**
     * @return Returns the deleteRule.
     */
    public zXType.deleteRule getDeleteRule() {
        return deleteRule;
    }
    
    /**
     * @return Returns the deleteRule.
     */
    public String getDeleteRuleAsString() {
        return zXType.valueOf(getDeleteRule());
    }
    
    /**
     * @param deleteRule The deleteRule to set.
     */
    public void setDeleteRule(zXType.deleteRule deleteRule) {
        this.deleteRule = deleteRule;
    }
    
    /**
     * @return Returns the entity.
     */
    public String getEntity() {
        return entity;
    }
    
    /**
     * @param entity The entity to set.
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }
    
    /**
     * @return Returns the fKAttr.
     */
    public String getFKAttr() {
        return FKAttr;
    }
    
    /**
     * @param attr The fKAttr to set.
     */
    public void setFKAttr(String attr) {
        FKAttr = attr;
    }
    
    //------------------------ Overidden methods
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
        toString.append("entity", this.entity);
        toString.append("deleteRule", this.deleteRule);
        toString.append("FKAttr", this.FKAttr);
        return toString.toString();
    }
}