package org.zxframework;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.zxframework.exception.ParsingException;
import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * @author Michael Brewer
 */
public class DescriptorParserSaXImpl implements DescriptorParser {
	
	private static Log log = LogFactory.getLog(DescriptorParserSaXImpl.class);
	
	/**
	 * Parse a BO Descriptor
	 * @param pobjDescriptor A handle to the descriptor.
	 * @param pstrBODir The base directory for the bo files.
	 * @param pstrFile The file to parse
	 * @throws ParsingException
	 */
	public static void parse(Descriptor pobjDescriptor, String pstrBODir, File pstrFile) throws ParsingException {
		/**
		 * Parse the BO Entity:
		 */
		try {
			
			XMLReader xr = XMLReaderFactory.createXMLReader();
			BODescriptorHandler objHandler = new BODescriptorHandler(xr, pobjDescriptor, pstrBODir);
			xr.setContentHandler(objHandler);
			xr.parse(new InputSource(new FileReader(pstrFile)));
			
		} catch (Exception e) {
			log.fatal("Failed to parse xml : " + pstrFile.toString(), e);
			throw new ParsingException(e);
		}
	}
	
	/**
	 * @see org.zxframework.DescriptorParser#parse(InputStream, Descriptor)
	 */
	public void parse(InputStream in, Descriptor pobjDesc) throws ParsingException {
		throw new ParsingException("Not implemented for SAX.");
	}

	/**
	 * @see org.zxframework.DescriptorParser#parseSingleAttribute(java.lang.Object)
	 */
	public Attribute parseSingleAttribute(Object pobj) throws ParsingException {
		throw new ParsingException("Not implemented for SAX");
	}
	
	/**
	 * @see org.zxframework.DescriptorParser#parseSingleAttributeGroup(Object, AttributeGroup)
	 */
	public AttributeGroup parseSingleAttributeGroup(Object pobjXMLNode, AttributeGroup pcolAttrGroup) throws ParsingException {
		throw new ParsingException("Not implemented for SAX.");
	}

}
