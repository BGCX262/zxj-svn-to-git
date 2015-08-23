/*
 * Created on Apr 9, 2004
 */
package org.zxframework.web;

import org.zxframework.CloneableObject;
import org.zxframework.util.ToStringBuilder;

/**
 * Pageflow EditForm Entity Massager object.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFEntityMassager implements CloneableObject {
    
    //------------------------ Members

    private String attr;
    private String property;
    private String value;
    
    //------------------------ Constructor

    /**
     * Default constructor.
     */
    public PFEntityMassager() {
        super();
    }
    
    //------------------------ Getters and Setters

    /**
     * @return Returns the attr.
     */
    public String getAttr() {
        return attr;
    }
    
    /**
     * @param attr The attr to set.
     */
    public void setAttr(String attr) {
        this.attr = attr;
        // setKey(attr);
    }
    
    /**
     * @return Returns the property.
     */
    public String getProperty() {
        return property;
    }
    
    /**
     * @param property The property to set.
     */
    public void setProperty(String property) {
        this.property = property;
    }
    
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    //------------------------ Overidden methods
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
	    PFEntityMassager objEntityMessager = new PFEntityMassager();
	    objEntityMessager.setAttr(getAttr());
	    objEntityMessager.setProperty(getProperty());
	    objEntityMessager.setValue(getValue());
	    return objEntityMessager;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
        toString.append("attr", getAttr());
        toString.append("property", getProperty());
        toString.append("value", getValue());
        
        return toString.toString();
    }
}