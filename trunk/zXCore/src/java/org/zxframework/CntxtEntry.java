/*
 * Created on Jan 13, 2004 by Michael Brewer
 * $Id: CntxtEntry.java,v 1.1.2.10 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import org.zxframework.util.ToStringBuilder;

/**
 * Entry for context (quick context or BO context).
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 09 May 2003
 * 
 *</pre>
 *
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class CntxtEntry implements CloneableObject {
    
    //------------------------ Members    
    
    private String name;
	private String strValue;
	private Object objValue;
	private int status;

    //------------------------ Constructors
	
    /**
     * Default constructor.
     */
    public CntxtEntry() { 
    	super(); 
    }
	
    //------------------------ Getters and Setters   
	
	/**
	 * The name of the context entry, used as a key in the quickcontext.
	 * 
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
        // Converting this object to a non ZXObject.
        // setKey(name);
	}
    
	/**
	 * @return Returns the objValue.
	 */
	public Object getObjValue() {
		return this.objValue;
	}

	/**
	 * @param objValue The objValue to set.
	 */
	public void setValue(Object objValue) {
		this.objValue = objValue;
	}

	/**
	 * @param strValue The strValue to set.
	 */
	public void setValue(String strValue) {
		this.strValue = strValue;
	}
	
	/**
	 * @return Returns the status.
	 */
	public int getStatus() {
		return this.status;
	}

	/**
	 * @param status The status to set.
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return Returns the strValue.
	 */
	public String getStrValue() {
		
	    if (this.strValue == null && this.objValue instanceof String) {
	        return (String)this.objValue;
	    }
	    
		return this.strValue;
	}
    
    //------------------------ Object overidden methods.
    
    /**
     * Creates a clean clone of the CntxtEntry, 
     * note this will try to create a clean clone 
     * of the value as well.
     * 
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        CntxtEntry objCleanClone = new CntxtEntry();
        
        objCleanClone.setName(getName());
        
        /** 
         * Clone the value : 
         **/
        if (this.objValue != null) {
            if (objValue instanceof CloneableObject) {
                // Clone the CloneableObject for a really clean context value.
            	// NOTE: If the object is an instance of ZXObject it is most likely a ZXBO
                objCleanClone.setValue(((CloneableObject)this.objValue).clone());
            } else {
                objCleanClone.setValue(this.objValue);
            }
        }
        if (this.strValue != null) {
            objCleanClone.setValue(this.strValue);
        }
        
        objCleanClone.setStatus(getStatus());
        
        return objCleanClone;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
        toString.append("name", this.name);
        if (this.objValue != null) {
            toString.append("objValue", this.objValue);
        } else {
            toString.append("strValue", this.strValue);
        }
        toString.append("status", this.status);
        
        return toString.toString();
    }
}