/*
 * Created on Jan 15, 2004 by michael
 * $Id: AttributeGroupCollection.java,v 1.1.2.8 2005/08/25 11:21:08 mike Exp $
 */
package org.zxframework;

import org.zxframework.exception.ParsingException;

/**
 * ColAttribute, is a collection of AttributeGroups. It extends and has a
 * couple of extra methods and a handle to Desriptor.
 * 
 * <pre>
 * 
 * NOTE : This was ColAttributeGroups in the old VB code.
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since J1.4
 * @version 0.0.1
 * 
 */
public class AttributeGroupCollection extends ZXCollection {

    //------------------------ Members      
    
    /** A handle to the BO descriptor parser. **/
    private DescriptorParser descriptor;

    //------------------------ Constructors      
    
    /**
     * Default constructor.
     */
    public AttributeGroupCollection() { super(); }
    
    /**
     * @param size The intial size of the collection
     */
    public AttributeGroupCollection(int size) { super(size); }
    
    //------------------------ Getters and Setters
    
    /**
     * @param descriptor The descriptor to set.
     */
    public void setDescriptor(DescriptorParser descriptor) {
        this.descriptor = descriptor;
    }

    //------------------------ Helper methods

    /**
     * Get the attributeGroup by the name.
     * 
     * @param groupName The name of the attribute groups to retrieve
     * @return Returns a attribute group by its name. Null if there is not one.
     * @throws ParsingException Thrown if the xml parsing fails.
     */
    public AttributeGroup get(String groupName) throws ParsingException {
    	AttributeGroup objAttrGroup = (AttributeGroup) super.get(groupName);
    	
    	/**
    	 * Perform on the fly parsing if needed.
    	 */
    	if (objAttrGroup != null && !objAttrGroup.isParsed() && objAttrGroup.getXmlNode() != null) {
			objAttrGroup = this.descriptor.parseSingleAttributeGroup(objAttrGroup.getXmlNode(), objAttrGroup);
    	}
    	
        return objAttrGroup;
    }
}