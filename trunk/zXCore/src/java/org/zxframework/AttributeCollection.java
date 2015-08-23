/*
 * Created on Jan 13, 2004 by michael
 * $Id: AttributeCollection.java,v 1.1.2.7 2005/08/24 08:03:57 mike Exp $
 */
package org.zxframework;

import java.util.Iterator;

/**
 * AttributeCollection is a collection of Attributes it extends ZXCollection
 * and contains a couple of helper methods.
 * 
 * <pre>
 * 
 * NOTE : This was ColAttributes in the VB code.
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class AttributeCollection extends ZXCollection {
    
    //------------------------ Constructors  
    
    /**
     * The default constructor.
     */
    public AttributeCollection() {
        super();
    }
    
    //------------------------ Helper methods
    
    /**
     * Get a attribute from the collection.
     * 
     * <pre>
     * 
     * A helper method that return Attribute from the collection, instead of
     * just Object. It is safer to do it like this when you initialised the
     * ZXCollection as a ColAttribute.
     * 
     * NOTE : Called the super class get and Casts it to a Attribute
     * </pre>
     * 
     * @param pstrAttribute The key of the the ColAttribute, this will be the 
     * 				name of the Attribute.
     * @return Returns a attribute but the key.
     */
    public Attribute get(String pstrAttribute) {
        return (Attribute) super.get(pstrAttribute);
    }
    
    /**
     * Check whether this attribute exists in this collection.
     * 
     * @param pstrName Name of the attribute to check.
     * @return Returns true if the attribute exists in this collection.
     */
    public boolean inGroup(String pstrName) {
        return super.get(pstrName) != null;
    }
    
    //------------------------ Object overloaded method
    
    /**
     * @return Returns a string version of the attributes seperated by comma's
     */
    public String formattedString() {
        if (size() > 0) {
            StringBuffer formattedString = new StringBuffer();
            
            Attribute objAttr;
            
            Iterator iter = iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if (formattedString.length() > 0) {
                    formattedString.append(',');
                }
                formattedString.append(objAttr.getName());
            }
            return formattedString.toString();
        }
        return "";
    }
    
    //------------------------ Object overridden methods.  
    
    /** 
     * Clone the attribute collection object.
     * 
     * <pre>
     * 
     * NOTE : This is only a shallow clone.
     * </pre>
     * 
     * @see java.lang.Object#clone()
     **/
    public Object clone() {
        AttributeCollection colCollection = new AttributeCollection();
        Attribute objAttr;
        
        Iterator iter = iterator();
        while (iter.hasNext()) {
            objAttr = (Attribute)iter.next();
            colCollection.put(objAttr.getName(), objAttr);
        }
        
        return colCollection;
    }
}