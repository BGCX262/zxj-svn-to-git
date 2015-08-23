/*
 * Created on May 25, 2004 by Michael Brewer
 * $Id: ReceiveParameterBagEntry.java,v 1.1.2.2 2006/07/24 15:44:52 mike Exp $
 */
package org.zxframework.web;

import org.zxframework.CloneableObject;
import org.zxframework.zXType;

import org.zxframework.util.ToStringBuilder;

/**
 * Single entry for the parameter list of recieve parameterbags.
 * 
 * What   : ReceiveParameterBagEntry
 * Who    : Bertus Dispa
 * When   : 17 October 2005
 *  
 * @author Bertus Dispa
 * @author Michael Brewer
 * @author David Swann
 * 
 * @version 0.0.1
 * @since J1.5
 */
public class ReceiveParameterBagEntry extends SendParameterBagEntry 
									  implements CloneableObject {
	
	//------------------------ Members
	
	private String description;
	private boolean mandatory;
	
	//------------------------ Runtime members
	
	private transient Object actualValueObject;
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public ReceiveParameterBagEntry() {
		super();
	}
	
	//------------------------ Runtime Getters/Setters
	
	/**
	 * The actual resolved runtime value.
	 * 
	 * @return Returns the actualValueObject.
	 */
	public Object getActualValueObject() {
		return actualValueObject;
	}

	/**
	 * @param actualValueObject The actualValueObject to set.
	 */
	public void setActualValueObject(Object actualValueObject) {
		this.actualValueObject = actualValueObject;
	}

	//------------------------ Getters/Setters
	
	/**
	 * The description of the parameter.
	 * 
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Whether this parameter bag entry must be defined on the sending side.
	 * 
	 * @return Returns the mandatory.
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * @param mandatory The mandatory to set.
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
	
    //------------------------ Object overidden methods.
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
    	try {
            ReceiveParameterBagEntry objRecieveParam  = (ReceiveParameterBagEntry)super.clone();
            
            objRecieveParam.setDescription(getDescription());
            objRecieveParam.setMandatory(isMandatory());
            
            return objRecieveParam;
    	} catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }
    
    /** 
     * @see java.lang.Object#toString()
     **/
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
        toString.append("name", getName());
        toString.append("comment", getComment());
        toString.append("entrytype", zXType.valueOf(getEntryType()));
        toString.append("value", getValue());
        
        toString.append("mandatory", isMandatory());
        toString.append("description", getDescription());
        toString.append("actualValue", getActualValueObject());
        
        return toString.toString();
    }
}