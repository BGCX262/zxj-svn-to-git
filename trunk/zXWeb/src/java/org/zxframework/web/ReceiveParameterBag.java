/*
 * Created on May 25, 2004 by Michael Brewer
 * $Id: ReceiveParameterBag.java,v 1.1.2.2 2006/07/24 15:44:53 mike Exp $
 */
package org.zxframework.web;

import org.zxframework.CloneableObject;
import org.zxframework.KeyedObject;
import org.zxframework.ZXCollection;

import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

/**
 * What   : ReceiveParameterBag
 * Who    : Bertus Dispa
 * When   : 17 October 2005
 * 
 * The definition for the 'receive' (ie 'called') part of remote pagflow calls
 * 
 * Functional properties:
 * ---------------
 * zX - Good old handle to zX
 * pageflow - Handle to hosting pageflow
 * comment - design-time only comment
 *
 * name - Name (unique within a pageflow)
 * parameters - collection of parameter definitions
 *
 * Internal properties:
 * ---------------
 * parsed - Indicates that XML Node has been parsed (just-in-time parsing)
 * xmlNode - handle to raw XML (only relevant before parsing)
 * 
 * @author Bertus Dispa
 * @author Michael Brewer
 * @author David Swann
 * 
 * @version 0.0.1
 * @since J1.5
 */
public class ReceiveParameterBag implements KeyedObject, CloneableObject {
	
	//------------------------ Members
	
	private String comment;
	private String name;
	private ZXCollection parameters;
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public ReceiveParameterBag() {
		super();
	}
	
	//------------------------ Getters/Setters
	
	/**
	 * Design-time only comment.
	 * 
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Name (unique within a pageflow).
	 * 
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
	 * A collection ((Send/Receive)ParameterBagEntry) of parameter definitions.
	 * 
	 * @return Returns the parameters.
	 */
	public ZXCollection getParameters() {
		return parameters;
	}

	/**
	 * @param parameters The parameters to set.
	 */
	public void setParameters(ZXCollection parameters) {
		this.parameters = parameters;
	}
	
	//------------------------ Parsing/Dump XML
	
	/**
	 * @param pobjXMLGen Handle to the current xml dump.
	 */
	public void dumpAsXML(XMLGen pobjXMLGen) {
		boolean blnMe = this.getClass().getClass().equals(ReceiveParameterBag.class);
		
		if (blnMe) {
			pobjXMLGen.openTag("receiveparameterbag");
		}
		
		pobjXMLGen.taggedCData("name", getName());
		pobjXMLGen.taggedCData("comment", getComment());
		
		/**
		 * Dump Parameter entries
		 */
		
		if (blnMe) {
			pobjXMLGen.closeTag("receiveparameterbag");
		}
	}
	
	//------------------------ Keyed Object iterface
	
	/**
	 * The name is also the key of this object in a collection.
	 * 
	 * @see org.zxframework.KeyedObject#getKey()
	 */
	public String getKey() {
		return getName();
	}
	
    //------------------------ Object overidden methods.
	
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
    	try {
        	ReceiveParameterBag objRecieveParamBag  = (ReceiveParameterBag)this.getClass().newInstance();
        	
        	objRecieveParamBag.setName(getName());
        	objRecieveParamBag.setComment(getComment());
        	
        	if (getParameters() != null && getParameters().size() > 0) {
        		objRecieveParamBag.setParameters((ZXCollection)getParameters().clone());
        	}
        	
            return objRecieveParamBag;
    	} catch (Exception e) {
    		throw new RuntimeException("Failed to clone object");
    	}
    }
    
    /** 
     * @see java.lang.Object#toString()
     **/
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
        toString.append("name", getName());
        toString.append("comment", getComment());
        toString.append("parameters", getParameters());
        
        return toString.toString();
    }
}