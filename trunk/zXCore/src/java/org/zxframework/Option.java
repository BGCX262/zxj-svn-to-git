/*
 * Created on Jan 13, 2004 by michael
 * $Id: Option.java,v 1.1.2.7 2005/08/25 11:24:16 mike Exp $
 */
package org.zxframework;

import org.zxframework.util.ToStringBuilder;

/**
 * Option - Is a list of options in a descriptor attribute.
 * 
 * <pre>
 * NOTE : getValue and setValue manipulate the key of the object used in
 * storing in ZXCollection.
 * 
 * Who    : Bertus Dispa
 * When   : 28 August 2002
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class Option implements KeyedObject {

    //------------------------ Members
	
    private String value;
    private LabelCollection label;
    private LabelCollection description;

    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public Option() {
        super();
        
        setLabel(new LabelCollection());
    }
    
    //------------------------ Getters and Setters
    
    /**
     * Is the description of the option.
     * 
     * @return Returns the description.
     */
    public LabelCollection getDescription() {
        return this.description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(LabelCollection description) {
        this.description = description;
    }

    /**
     * The label of the option. This is the value that will be displayed
     * in the drop down list.
     * 
     * @return Returns the label.
     */
    public LabelCollection getLabel() {
        return label;
    }
    
    /**
     * @param label The label to set.
     */
    public void setLabel(LabelCollection label) {
        this.label = label;
    }

    /**
     * The value of the option. This is the 
     * value that is submitted in the form.
     * NOTE : This is also used as the key.
     * 
     * @return Returns the value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value of the option.
     * 
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
        // Converting this object to a non ZXObject.
        // setKey(value);
    }
    
    //------------------------ Helper methods
    
    /**
     * Get string value of the label.
     * 
     * @return Returns a string representation of the label. 
     * 			If the label object is null then we will return null.
     */
    public String getLabelAsString() {
    	String getLabelAsString = null;
    	
    	if (getLabel() != null) {
    		getLabelAsString = getLabel().getLabel();
    	}
    	
        return getLabelAsString;
    }
    
    //------------------------ KeyedObject implemented methods
    
    /**
     * Returns the key of this object which is value
     * 
     * @see #getValue()
     * @see org.zxframework.KeyedObject#getKey()
     */
    public String getKey() {
    	return this.value;
    }
    
    //------------------------ Object overidden methods.
    
    /** 
     * @see java.lang.Object#toString()
     **/
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
        if (getLabel() != null) {
            toString.append("label", getLabelAsString());
        }
        
        if (getDescription() != null) {
            toString.append("description", getDescription().getLabel());
        }
        
        toString.append("value", getValue());
        
        return toString.toString();
    }
}