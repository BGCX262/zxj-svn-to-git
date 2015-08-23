/*
 * Created on Jan 13, 2004 by michael
 * $Id: AttributeGroup.java,v 1.1.2.6 2005/08/25 11:35:01 mike Exp $
 */
package org.zxframework;

import java.util.Iterator;

import org.jdom.Element;
import org.zxframework.util.ToStringBuilder;

/**
 * AttributeGroup - A AttributeGroup represents a group of ZXBO attributes.
 * 
 * <pre>
 * 
 * NOTE : getName and setName manipulate the key of the object used in storing
 * in ZXCollection
 * 
 * Who    : Bertus Dispa
 * When   : 28 August 2002
 * 
 * Change    : DGS13JUN2003
 * Why       : Added 'comment' property.
 * 
 * Change    : DGS11AUG2003
 * Why       : Added 'inherited' property.
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class AttributeGroup implements KeyedObject {

    //------------------------ Members
    
	private String name;
    private boolean inherited;
    private String comment;
    private boolean parsed;
    private Element xmlNode;
    private AttributeCollection attributes;

    //------------------------ Constructors 
    
    /**
     * Default constructor. 
     */
    public AttributeGroup() { super(); }
    
    //------------------------ Getters and Setters    
    
    /**
     * @return Returns the attributes.
     */
    public AttributeCollection getAttributes() {
        if (this.attributes == null) {
            this.attributes = new AttributeCollection();
        }
        return this.attributes;
    }

    /**
     * @param attributes The attributes to set.
     */
    public void setAttributes(AttributeCollection attributes) {
        this.attributes = attributes;
    }

    /**
     * @return Returns the comment.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * This is the name of the attribute group, as well as the key of the group
     * in the collection.
     * 
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the key and name of the attribute group.
     * 
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
        // Converting this object to a non ZXObject.
        // setKey(name);
    }

    /**
     * @return Returns the parsed.
     */
    public boolean isParsed() {
        return this.parsed;
    }

    /**
     * @param parsed The parsed to set.
     */
    public void setParsed(boolean parsed) {
        this.parsed = parsed;
    }

    /**
     * @return Returns the xmlNode.
     */
    public Element getXmlNode() {
        return this.xmlNode;
    }

    /**
     * @param xmlNode The xmlNode to set.
     */
    public void setXmlNode(Element xmlNode) {
        this.xmlNode = xmlNode;
    }
    
    /**
     * @return Returns the inherited.
     */
    public boolean isInherited() {
        return this.inherited;
    }

    /**
     * @param inherited The inherited to set.
     */
    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }
    
    //------------------------ Util methods
    
    /**
     * @return Rethurns the string form of the Attributes.
     */
    public String getAttributesAsString() {
        return getAttributesAsString(",");
    }

    /**
     * @param pstrDelim The delimeter of the string returned
     * @return Returns the string form of the Attributes, using the delimeter
     *             specified.
     */
    public String getAttributesAsString(String pstrDelim) {
        StringBuffer attributesAsString = new StringBuffer();
        
        Iterator iter = this.attributes.iterator();
        while (iter.hasNext()) {
            attributesAsString.append(((Attribute) iter.next()).getName());
            
            /** Add the seperator at the end of each attribute. */
            if (iter.hasNext()) {
                attributesAsString.append(pstrDelim);
            }
        }
        
        return attributesAsString.toString();
    }
    
    //------------------------ KeyedObject implemented methods
    
    /**
     * Returns the key of this object which is name.
     * 
     * @see #getName()
     * @see org.zxframework.KeyedObject#getKey()
     */
    public String getKey() {
    	return this.name;
    }
    
    //------------------------ Object overidden methods.
    
    /** 
     * @see java.lang.Object#toString()
     **/
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
        toString.append("name", getName());
        toString.append("inherited", isInherited());
        toString.append("parsed", isParsed());
        toString.append("xmlNode", getXmlNode());
        toString.append("attributes", getAttributesAsString());
        
        return toString.toString();
    }    
}