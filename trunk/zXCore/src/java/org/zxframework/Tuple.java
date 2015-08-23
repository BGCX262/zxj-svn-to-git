/*
 * Created on Jan 13, 2004 by michael
 * $Id: Tuple.java,v 1.1.2.5 2005/08/24 16:14:36 mike Exp $
 */
package org.zxframework;

/**
 * Tuple- Generic name / value pair object.
 * 
 * <pre>
 * 
 * NOTE : getName and setName manipulate the key of the object used in storing
 * in ZXCollection
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class Tuple extends ZXObject {
    
    //------------------------ Members
    
    /** The value of the Tuple. **/
	private String name;
    private String value = "";

    //------------------------ Constructors      
    
    /**
     * Default constructor. 
     */
    public Tuple() {
        super();
    }
    
    //------------------------ Getters and Setters       
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
    	this.name = name;
        setKey(name);
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    //------------------------ Object overidden method.
    
    /** 
     * @see java.lang.Object#clone()
     **/
    public Object clone() {
        Tuple objTuple = new Tuple();
        
        objTuple.setName(getName());
        objTuple.setValue(getValue());
        
        return objTuple;
    }
    
    /** 
     * @see java.lang.Object#toString()
     **/
    public String toString() {
        return "name=" + getName() + ", vaue=" + getValue();
    }
}