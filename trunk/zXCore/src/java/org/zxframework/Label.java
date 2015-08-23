/*
 * Created on Jan 13, 2004 by michael
 * $Id: Label.java,v 1.1.2.7 2005/08/25 11:24:16 mike Exp $
 */
package org.zxframework;

import org.zxframework.util.ToStringBuilder;

/**
 * Label - Object to stored text with a language tag.
 * 
 * <pre>
 * NOTE : getLanguage and setLanguage manipulate the key of the object used in storing
 * in ZXCollection 
 * 
 * Who    : Bertus Dispa
 * When   : 28 August 2002
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class Label implements KeyedObject, CloneableObject {

    //------------------------ Members
    
	private String language;
    private String description;
    private String label;

    //------------------------ Constructors

    /**
     * Default constructor.
     */
    public Label() {
        super();
    }
    
    //------------------------ Getters and Setters
    
    /**
     * The label description.
     * 
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This returns the label name of the label.
     * 
     * @return Returns the label.
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * This returns the language of the language, used to select which label to
     * display. 
     * NOTE : This is also the key of the object.
     * 
     * @return Returns the language.
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Set the language label, which is also the key of this object.
     * 
     * @param language The language to set.
     */
    public void setLanguage(String language) {
        this.language = language;
        // Converting this object to a non ZXObject.
        // setKey(language);
    }
    
    //------------------------ KeyedObject implemented methods
    
    /**
     * Returns the key of this object which is language.
     * 
     * @see #getLanguage()
     * @see org.zxframework.KeyedObject#getKey()
     */
    public String getKey() {
    	return this.language;
    }
    
    //------------------------ Object overidden methods.
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        Label objLabel = new Label();
        
        objLabel.setDescription(getDescription());
        objLabel.setLabel(getLabel());
        objLabel.setLanguage(getLanguage());
        
        return objLabel;
    }
    
    /** 
     * @see java.lang.Object#toString()
     **/
    public String toString() {
        return new ToStringBuilder(this)
	               .append("language", getLanguage())
	        	   .append("label", getLabel())
	        	   .append("description", getDescription()).toString();
    }
}