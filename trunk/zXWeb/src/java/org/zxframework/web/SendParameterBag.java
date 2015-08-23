/*
 * Created on May 25, 2004 by Michael Brewer
 * $Id: SendParameterBag.java,v 1.1.2.2 2006/07/24 15:44:52 mike Exp $
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.CloneableObject;
import org.zxframework.KeyedObject;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.ToStringBuilder;
import org.zxframework.util.XMLGen;

/**
 * What   : SendParameterBag
 * Who    : Bertus Dispa
 * When   : 17 October 2005
 *
 * The definition for the 'send' (ie 'caller') part of remote pagflow calls
 *
 * Functional properties:
 * ---------------
 *
 * zX - Good old handle to zX
 * pageflow - Handle to hosting pageflow
 * comment - design-time only comment
 *
 * name - Name (unique within a pageflow)
 * parameters - collection of parameter definitions
 * 
 * callPageflow - Name of pageflow to call
 * parameterBagDefinition - Name of receiving end of the parameter bag
 * BOContext - BO context definition
 *
 * Internal properties:
 * ---------------
 *
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
public class SendParameterBag extends ReceiveParameterBag implements CloneableObject, KeyedObject {
	
	//------------------------ Members
	
	private String callpageflow;
	private String parameterbagdefinition;
	private List bocontexts;
	
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public SendParameterBag(){
		super();
	}
	
	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the bocontexts.
	 */
	public List getBocontexts() {
		return bocontexts;
	}

	/**
	 * @param bocontexts The bocontexts to set.
	 */
	public void setBocontexts(List bocontexts) {
		this.bocontexts = bocontexts;
	}

	/**
	 * @return Returns the callpageflow.
	 */
	public String getCallpageflow() {
		return callpageflow;
	}

	/**
	 * @param callpageflow The callpageflow to set.
	 */
	public void setCallpageflow(String callpageflow) {
		this.callpageflow = callpageflow;
	}

	/**
	 * @return Returns the parameterbagdefinition.
	 */
	public String getParameterbagdefinition() {
		return parameterbagdefinition;
	}

	/**
	 * @param parameterbagdefinition The parameterbagdefinition to set.
	 */
	public void setParameterbagdefinition(String parameterbagdefinition) {
		this.parameterbagdefinition = parameterbagdefinition;
	}

	//------------------------ Parsing/Dump XML
	
	/**
	 * @param pobjXMLGen Handle to the current xml dump.
	 */
	public void dumpAsXML(XMLGen pobjXMLGen) {
		pobjXMLGen.openTag("sendparameterbag");
		
		super.dumpAsXML(pobjXMLGen);
		
		pobjXMLGen.taggedCData("callpageflow", getCallpageflow());
		pobjXMLGen.taggedCData("parameterebagdefinition", getParameterbagdefinition());
		
		pobjXMLGen.closeTag("sendparameterbag");
	}
	
    //------------------------ Object overidden methods.
	
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
    	try {
        	SendParameterBag objSendParamBag  = (SendParameterBag)super.clone();
        	
        	objSendParamBag.setCallpageflow(getCallpageflow());
        	objSendParamBag.setParameterbagdefinition(getParameterbagdefinition());
        	
            if (getBocontexts() != null && getBocontexts().size() > 0) {
            	objSendParamBag.setBocontexts(CloneUtil.clone((ArrayList)getBocontexts()));
            }
            
            return objSendParamBag;
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
        
        toString.append("callpageflow", getCallpageflow());
        toString.append("parameterbagdefinition", getParameterbagdefinition());
        toString.append("bocontexts", getBocontexts());
        
        return toString.toString();
    }
}