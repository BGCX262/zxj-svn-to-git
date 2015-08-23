/*
 * Created on Mar 29, 2005
 * $Id: ExprFHI.java,v 1.1.2.3 2005/08/30 08:44:11 mike Exp $
 */
package org.zxframework.expression;

import org.zxframework.ZXObject;

/**
 * The interface definition for an expression function handler iterator
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version C1.5
 */
public abstract class ExprFHI extends ZXObject {
    
    //----------------------- Members
	
    private String name;
    
    //----------------------- Constructor
    
    //----------------------- Getters and Setters
    
    /**
     * Name of the iterator as it is stored in the 
     * expression handler iterator collection.
     * 
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Name of the iterator as it is stored in the 
     * expression handler iterator collection.
     * 
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    //----------------------- Methods to implement
    
    /**
     * Indicates whether there is a next iteration available
     * 
     * @return Returns whether there is a next iteration.
     */
    public abstract boolean hasNext();
    
    /**
     * Do the next iteration.
     * 
     * @return Returns whether it went to the next postion sucessfully.
     */
    public abstract boolean doNext();
    
    /**
     * Reset the iterator ready for a next loop over elements
     */
    public abstract void reset();
    
    /**
     * Action to perform on termination.
     */
    public abstract void term();
}