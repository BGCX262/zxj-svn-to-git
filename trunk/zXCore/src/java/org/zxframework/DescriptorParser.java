package org.zxframework;

import java.io.InputStream;

import org.zxframework.exception.ParsingException;

/**
 * Interface for Parsing the Descriptor.
 * 
 * This may later be replaced by Spring. Or we may have a different implementation using Sax or Digester.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public interface DescriptorParser {
	
	/**
	 * @param in The input streeam to read.
	 * @param pobjDesc The descriptor object.
	 * @throws ParsingException 
	 */
	public void parse(InputStream in, Descriptor pobjDesc) throws ParsingException;
	
	/**
	 * @param pobj
	 * @return Returns Attribute based on the config object
	 * @throws ParsingException 
	 */
	public Attribute parseSingleAttribute(Object pobj) throws ParsingException;
	
    /**
     * parseSingleAttributeGroup - Parse single attribute group
     * 
     * @param pobjXMLNode The element containing a single attribute group.
     * @param pcolAttrGroup A handle to the AttributeGroup.
     * @return Returns a single attribute group
     * @throws ParsingException Thrown if parseSingleAttributeGroup fails.
     */
	public AttributeGroup parseSingleAttributeGroup(Object pobjXMLNode, 
													AttributeGroup pcolAttrGroup) throws ParsingException;
}