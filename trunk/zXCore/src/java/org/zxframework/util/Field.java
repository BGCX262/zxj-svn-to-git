/*
 * Created on 04-Feb-2005, by Michael Brewer
 * $Id: Field.java,v 1.1.2.6 2006/07/17 16:15:53 mike Exp $
 */
package org.zxframework.util;

/**
 * Represents a column in a table.
 * 
 * This is used by DDL util
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class Field {
    
    private String name;
    private int type;
    private int definedSize;
    
    /**
     * Default constructor.
     */
    public Field( ){ 
        super();
    }
    
    /**
     * @return Returns the definedSize.
     */
    public int getDefinedSize() {
        return definedSize;
    }
    
    /**
     * @param definedSize The definedSize to set.
     */
    public void setDefinedSize(int definedSize) {
        this.definedSize = definedSize;
    }
    
    /**
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
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }
    
    /**
     * @param type The type to set.
     */
    public void setType(int type) {
        this.type = type;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String toString = "Field : name = " + name 
                          + ", type = " + type + ", definedSize=" + definedSize;
        return toString;
    }
}