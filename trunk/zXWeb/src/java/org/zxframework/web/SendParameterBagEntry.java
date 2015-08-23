/*
 * Created on May 25, 2004 by Michael Brewer
 * $Id: SendParameterBagEntry.java,v 1.1.2.5 2006/07/24 16:47:00 mike Exp $
 */
package org.zxframework.web;

import java.util.Iterator;

import org.zxframework.CloneableObject;
import org.zxframework.KeyedObject;
import org.zxframework.LabelCollection;
import org.zxframework.ZXCollection;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.util.ToStringBuilder;

/**
 * Single entry for the parameter list of send parameterbags.
 * 
 * What   : SendParameterBagEntry
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
public class SendParameterBagEntry implements CloneableObject, KeyedObject {

	//------------------------ Members
	
	private String name;
	private String comment;
	private zXType.pageflowParameterBagEntryType entryType;
	private Object value;

	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public SendParameterBagEntry() {
		super();
	}
	
	//------------------------ Getters/Setters

	/**
	 * Design time comment.
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
	 * The type of the parameter value.
	 * 
	 * @return Returns the entrytype.
	 */
	public zXType.pageflowParameterBagEntryType getEntryType() {
		return entryType;
	}

	/**
	 * @param entrytype The entrytype to set.
	 */
	public void setEntryType(zXType.pageflowParameterBagEntryType entrytype) {
		this.entryType = entrytype;
	}

	/**
	 * The name of the parameter.
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
	 * @return Returns the value.
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	//------------------------ Descriptor helper method
	
	/**
	 * @param entrytype The entrytype to set.
	 */
	public void setEntrytype(String entrytype) {
		this.entryType = zXType.pageflowParameterBagEntryType.getEnum(entrytype);
	}
	
	//------------------------ KeyedObject implemented methods.
	
	/**
	 * The a potential key of this object in a collection.
	 * 
	 * @see org.zxframework.KeyedObject#getKey()
	 */
	public String getKey() {
		return name;
	}
	
    //------------------------ Object overidden methods.
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
    	try {
            SendParameterBagEntry objSendParam  = (SendParameterBagEntry)this.getClass().newInstance();
            
            objSendParam.setName(getName());
            objSendParam.setComment(getComment());
            objSendParam.setEntryType(getEntryType());
            
            if (getValue() != null) {
                /**
                 * Specialized cloning for the value.
                 */
                int intEntryType = getEntryType().pos;
                switch (intEntryType) {
        		case 0: // ppbetUrl
        			objSendParam.setValue(((PFUrl)getValue()).clone());
        			break;
        			
        		case 1: // ppbetRef
        			objSendParam.setValue(((PFRef)getValue()).clone());
        			break;
        			
        		case 2: // ppbetEntities
        			PFEntity objEntity;
        			ZXCollection colEntities = (ZXCollection)getValue();
        			ZXCollection colTmp = new ZXCollection(colEntities.size());
        			
        			Iterator iter = colEntities.iterator();
        			Iterator iterKeys = colEntities.iteratorKey();
        			while (iter.hasNext()) {
        				objEntity = (PFEntity)iter.next();
        				colTmp.put(iterKeys.next(), objEntity.clone());
        			}
        			
        			objSendParam.setValue(colTmp);
        			
        			break;
        			
        		case 3: // ppbetEntity
        			objSendParam.setValue(((PFEntity)getValue()).clone());
        			break;
        			
        		case 4: // ppbetLabel
        			objSendParam.setValue(((LabelCollection)getValue()).clone());
        			break;
        			
        		case 5: // ppbetComponent
        			objSendParam.setValue(((PFComponent)getValue()).clone());
        			break;
        		case 6: // ppbetString
        			/**
        			 * String are immutable so try do not need cloning.
        			 */
        			objSendParam.setValue(getValue());
        			break;
        			
        		default:
        			/**
        			 * This is probably all we really have to do.
        			 * As all objects that need to be cloned should
        			 * implement CloneableObject.
        			 */
        			if (getValue() instanceof CloneableObject) {
        				/**
        				 * If the object implements clonable lets just used that.
        				 */
        				objSendParam.setValue(((ZXObject)getValue()).clone());
        				
        			} else {
        				/**
        				 * Fall back on a staight copy
        				 */
        				objSendParam.setValue(getValue());
        			}
        			break;
        		}
            }
            
            return objSendParam;
            
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
        toString.append("entrytype", zXType.valueOf(getEntryType()));
        toString.append("value", getValue());
        
        return toString.toString();
    }
}