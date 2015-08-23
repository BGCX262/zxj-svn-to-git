/*
 * Created on Mar 29, 2005
 * $Id: ExprFHIDefault.java,v 1.1.2.4 2006/07/17 16:40:43 mike Exp $
 */
package org.zxframework.expression;

import java.util.ArrayList;

import org.zxframework.property.Property;

/**
 * Default implementation of the expression handler iterator. 
 * Uses a collection
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version C1.5 
 */
public class ExprFHIDefault extends ExprFHI {
    
    //------------------------ Members
    
    private ArrayList theCollection;
    private int cursor;
    
    //------------------------ Constructors
    
    /**
     * Hide the default constructor.
     */
    private ExprFHIDefault() {
        super();
    }
    
    /**
     * @param name The name of the iterator.
     */
    public ExprFHIDefault(String name){
        setName(name);
        
        // Init values.
        cursor = -1; // VB collections start at 1 java at 0.
        this.theCollection = new ArrayList();
    }
    
    //------------------------ Implemented methods.
    
    /**
     * @see org.zxframework.expression.ExprFHI#hasNext()
     */
    public boolean hasNext() {
        if (this.theCollection != null && !this.theCollection.isEmpty()) {
            return this.theCollection.size() > cursor;
        }
        return false;
    }

    /**
     * @see org.zxframework.expression.ExprFHI#doNext()
     */
    public boolean doNext() {
    	boolean doNext = false;
    	
        this.cursor++;
        
        if (this.theCollection != null && this.cursor <= this.theCollection.size()) {
            Property objProperty = (Property)this.theCollection.get(cursor);
            getZx().getExpressionHandler().setContext(getName(), objProperty);
            doNext = true;
            
        } else {
        	/**
        	 * There are not more elements in the collection.
        	 */
            getZx().getExpressionHandler().setContext(getName(), null);
        }
        
        return doNext;
    }
    
    /**
     * @see org.zxframework.expression.ExprFHI#reset()
     */
    public void reset() {
        this.cursor = -1;
        // this.theCollection = new ArrayList();
    }

    /**
     * @see org.zxframework.expression.ExprFHI#term()
     */
    public void term() {
        /**
         * Nothing happens here.
         */
    }
    
    //------------------------ Maybe iterface.
    
    /**
     * clear the current collection
     */
    public void clear() {
        this.cursor = -1;
        this.theCollection = new ArrayList();
    }

    //------------------------ Public methods
    
    /**
     * @param pobjValue The value to add to the collection.
     */
    public void addElement(Object pobjValue) {
        this.theCollection.add(pobjValue);
    }
    
    /**
     * @param pint The position to add the object. Remember that this is 0 based.
     * @param pobjValue The value to add to the collection.
     */
    public void addElement(int pint, Object pobjValue) {
        /**
         * Delete first
         */
        this.theCollection.remove(pint);
        
        /**
         * Add if not null
         */
        if (pobjValue != null) {
            this.theCollection.add(pint, pobjValue);
        }
    }
    
    /**
     * @param pint The postion of the element you want to retrieve.
     * @return Returns the object at the position specified.
     */
    public Object getElement(int pint) {
        return this.theCollection.get(pint);
    }
}