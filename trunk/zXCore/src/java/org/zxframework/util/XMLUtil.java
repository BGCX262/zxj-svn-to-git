/*
 * Created on Jan 27, 2004 by michael
 * $Id: XMLUtil.java,v 1.1.2.1 2005/05/12 15:52:59 mike Exp $
 */
package org.zxframework.util;

/**
 * <p>A xml utility class to help the creation of xml.</p>
 *
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class XMLUtil {

	/** <code>NEW_LINE</code> - The line seperator. */
	public static final char NEW_LINE = '\n';
	/** <code>SPACE</code> - Represents a space. */
	public static final char SPACE = ' ';
	/** <code>XML_CDATA_OPEN</code> - The open cdata. */
	public static final char[] XML_CDATA_OPEN = "<![CDATA[".toCharArray();
	/** <code>XML_CDATA_CLOSE</code> - The close cdata. */
	public static final char[] XML_CDATA_CLOSE = "]]>".toCharArray();
	/** <code>XML_HEADER</code> - XML header. */
	public static final char[] XML_HEADER = "<?xml version=\"1.0\" encoding=\"US-ASCII\"?>\n".toCharArray();
	
	/**
	 * Indents n tabs 
	 * @param pintNum Number of tabs to indent.
	 * @return A tab indented string.
	 */
	public static char[] indent(int pintNum) {
	    char indent[] = new char[pintNum];
	    for (int i = 0; i < pintNum; i++) {
            indent[i] = SPACE;
        }
	    return indent;
	}
}