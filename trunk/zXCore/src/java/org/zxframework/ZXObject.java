/*
 * Created on Feb 2, 2004 by michael
 * $Id: ZXObject.java,v 1.1.2.12 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import java.io.Serializable;

/**
 * Super class for all of the core framework classes.
 * 
 * <pre>
 * 
 * The object is quite rich at the moment, implmenting Cloneable, this is used in the Pageflow
 * caching, but may not be necessary, and Comparable for the ability of comparing to objects
 * and Seriazable for caching by Ehcache.
 * 
 * NOTE : Being Serializable means that any object that is a subclass
 * it automatically Serializable. Why? EHCache and some caching
 * libraries requires the object you want to cache to be Serializable.
 * This may or may not be ideal.
 *  
 * <img src="../../doc-files/zxobject.png">
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class ZXObject implements Comparable, Serializable, CloneableObject, KeyedObject {

    //------------------------ Members    
    
    /** 
     * This uniquely identifies this object in a collection.
     * (Well most of the time.) 
     **/
    private String key;

    //------------------------ Constructors      
    
    /**
     * Default construtor.
     */
    public ZXObject() {
    	super();
    }
    
    //------------------------ Getters and Setters    
    
    /**
     * Get the key, this uniquely identifies this object in a collection.
     * 
     * @return Returns the key.
     */
    public String getKey() {
        if (this.key == null) {
            this.key = "";
        }
        return this.key;
    }

    /**
     * Set the key identifier for this object, this will be used to uniquely
     * identify this object in the ZXCollection.
     * 
     * @param key The key to set.
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    //------------------------ Object overloaded 
    
    /**
     * May need to overide this to allow more specific cloning in the
     * subclasses of ZXObject.
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
        	/**
        	 * Ignore any exceptions
        	 */
        }
        
        return null;
    }

    /** 
     * Each class will need to overload this to allow for compareTo to work.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     **/
    public int compareTo(Object o) {
        return 0;
    }
    
    //------------------------ Specialised getter for zX.
    
    /**
     * Used for an optimized check for null. 
     * Used to replaces (zx != null), this might be faster ? 
     **/ 
    private boolean not_null;
    
    /** A handle to the zX object. Do not cache a handle to zX either. **/
    private transient ZX zx; // Do not create a local member of this if subclasses ZXObject .
    
    /**
     * Get the current zx handle.
     * 
     * <pre>
     * 
     * This will returns the handle to zx for this Thread, note this 
     * does have a slight overhead. In the future we may change how we get
     * a handle to zX.
     * </pre>
     * 
     * @return Returns the zx.
     */
    public ZX getZx() {
        /**
         * Exit Early if a valid handle to zX.
         */
        if (this.not_null && !this.zx.isInCached()) {
            return this.zx;
        }
        
        this.zx = ThreadLocalZX.getZX();
        this.not_null = true;
        return this.zx;
    }
}