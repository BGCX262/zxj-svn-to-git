/*
 * Created on Apr 15, 2004
 * $Id: XMLGen.java,v 1.1.2.2 2006/07/17 16:16:09 mike Exp $
 */
package org.zxframework.util;

/**
 * Class to help generating XML. Not a very OO class, but it does the job :).
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class XMLGen {
    
    //------------------------------------------------------------------------------------------ Members
    
	/** The string buffer used to build the xml **/
	private StringBuffer s;
	
	/** <code>level</code> - The level of the xml tag**/
	private int level;
	
	/** <code>NEW_LINE</code> - The line seperator. */
	public static final String NEW_LINE = "\n";
	
	/** <code>XML_CDATA_OPEN</code> - The open cdata. */
	public static final String XML_CDATA_OPEN = "<![CDATA[";
	
	/** <code>XML_CDATA_CLOSE</code> - The close cdata. */
	public static final String XML_CDATA_CLOSE = "]]>";
	
	/** <code>XML_HEADER</code> - XML header. */
	public static final char[] XML_HEADER = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n".toCharArray();
	
	/**<code>TAB_CHAR</code> - The java tab character **/
	public static final char TAB_CHAR = '\t';
	
    //------------------------------------------------------------------------------------------ Constructors
    
	/** 
	 * Default constructor 
	 **/
	public XMLGen() {
	    /**
	     * Reset XML string and start with standard XML heading
	     */
	    s = new StringBuffer(128);
	}
	
	/** 
	 * Construtor that allow you to preset the buffer size.
	 * 
	 * @param buffersize Set the initial size of a StringBuffer.
	 **/
	public XMLGen(int buffersize) {
	    /**
	     * Reset XML string and start with standard XML heading
	     */
	    s = new StringBuffer(buffersize);
	}
	
    //------------------------------------------------------------------------------------------ Public Methods
	
	/**
	 * Add the xml header.
	 */
	public void xmlHeader() {
	    s.append(XML_HEADER);
	}
    
	/**
	* Open a tag and increases the indent level.
	* 
	* @param pstrName The name of the tag
	*/
	public void openTag(String pstrName) {
	    if (StringUtil.len(pstrName) > 0) {
	        s.append(tabLevel()).append("<").append(pstrName).append(">\n");
	        this.level++;
	    }
	}
	
    /**
    * Close a tag and decreases the indent level.
    *
    * @param pstrName The element name
    */
    public void closeTag(String pstrName) {
        if (StringUtil.len(pstrName) > 0) {
            this.level--;
            s.append(tabLevel()).append("</").append(pstrName).append(">\n");
        }
    }
    
    /**
    * Open a tag with an attribute value.
    *
    * @param pstrName The element name.  
    * @param pstrAttrName The element attribute name.
    * @param pstrAttrValue The element attribute value.
    */
    public void openTagWithAttr(String pstrName, String pstrAttrName, String pstrAttrValue){
        if (StringUtil.len(pstrName) > 0 && StringUtil.len(pstrAttrName) > 0) {
            /**
             * Set defaults
             */
            if (pstrAttrValue == null) {
                pstrAttrValue = "";
            }
            
            s.append(tabLevel()).append("<").append(pstrName).append(' ')
            		.append(pstrAttrName).append("=\"").append(pstrAttrValue).append("\">\n");
          this.level++;
        }
    }
    
    /**
    *Add Value to the XML.
    *
    *<pre>
	* 
	* As &lt;name>Value&lt;/name>
	* </pre>
	* 
    * @param pstrName Element name  
    * @param pstrValue Element value. If the value if null or "" then nothing will be added.
    */
    public void taggedValue(String pstrName, String pstrValue) {
        taggedValue(pstrName, pstrValue, false);
    }
    
	/**
	 *Add Value to the XML.
	 *
	 * @param pstrName Element name  
	 * @param pblnValue Element value which is converted to a string value.
	 */
    public void taggedValue(String pstrName, boolean pblnValue) {
        taggedValue(pstrName, StringUtil.valueOf(pblnValue), true);
    }
	 
	/**
	* Add Value to the XML.
	* 
	* <pre>
	* 
	* As &lt;name>Value&lt;/name>
	* </pre>
	*
	* @param pstrName Element name  
	* @param pstrValue Element value
	* @param pblnMandantory Whether the value is compulsary. Optional default is false.
	*/
    public void taggedValue(String pstrName, String pstrValue, boolean pblnMandantory) {
        if ( StringUtil.len(pstrName) > 0 && (StringUtil.len(pstrValue) > 0 || pblnMandantory) ) {
            /**
             * Set defaults
             */
			if (pstrValue == null) {
			    pstrValue = "";
			}
            
            s.append(tabLevel()).append("<").append(pstrName).append(">")
            	.append(pstrValue)
              .append("</").append(pstrName).append(">\n");
        }
    }
    
    /**
    * Add tagged value to XML with an attribute.
    *
    * @param pstrName The name of the xml element.
    * @param pstrValue The value of the xml element
    * @param pstrAttrName The name of a attribute of this xml element.
    * @param pstrAttrValue The value for the attribute of this xml element.
    */
    public void taggedCDataWithAttr(String pstrName, String pstrValue, 
            										 String pstrAttrName, String pstrAttrValue) {
        if (StringUtil.len(pstrName) > 0 && StringUtil.len(pstrAttrName) > 0) {
            /**
             * Set defaults
             */
            if (pstrValue == null) {
                pstrValue = "";
            }
            if (pstrAttrValue == null) {
                pstrAttrValue = "";
            }
            
            s.append(tabLevel()).append("<").append(pstrName).append(' ').append(pstrAttrName).append("=\"").append(pstrAttrValue).append("\">")
             			.append(XML_CDATA_OPEN).append(pstrValue).append(XML_CDATA_CLOSE)
             	.append("</").append(pstrName).append(">\n");
        }
    }
    
    /**
     * Add tagged value to XML with an attribute.
     *
     * @param pstrName The name of the xml element.
     * @param pstrValue The value of the xml element
     * @param pstrAttrName The name of a attribute of this xml element.
     * @param pstrAttrValue The value for the attribute of this xml element.
     */
     public void taggedValueWithAttr(String pstrName, String pstrValue, 
             										 String pstrAttrName, String pstrAttrValue) {
         if (StringUtil.len(pstrName) > 0 && StringUtil.len(pstrAttrName) > 0) {
             /**
              * Set defaults
              */
             if (pstrValue == null) {
                 pstrValue = "";
             }
             if (pstrAttrValue == null) {
                 pstrAttrValue = "";
             }
             
             s.append(tabLevel()).append("<").append(pstrName).append(' ').append(pstrAttrName).append("=\"").append(pstrAttrValue).append("\">")
              			.append(pstrValue)
              	.append("</").append(pstrName).append(">\n");
         }
     }
     
	/**
	 * Add tagged value to XML with an array of attributes.
	 *
	 * @param pstrName The name of the xml element.
	 * @param pstrValue The value of the xml element
	 * @param parrAttrName The name of a attribute of this xml element.
	 * @param parrAttrValue The value for the attribute of this xml element.
	 */
	public void taggedCDataWithAttr(String pstrName, String pstrValue, 
	         										 String[] parrAttrName, String[] parrAttrValue) {
	     if (StringUtil.len(pstrName) > 0 && parrAttrName != null) {
	         /**
	          * Set defaults
	          */
             if (pstrValue == null) {
                 pstrValue = "";
             }
             
			s.append(tabLevel()).append("<").append(pstrName);
	    	
			for (int i = 0; i < parrAttrValue.length; i++) {
			    s.append(attr(parrAttrName[i], parrAttrValue[i]));
			}
			
			s.append("\">");
			s.append(XML_CDATA_OPEN).append(pstrValue).append(XML_CDATA_CLOSE);
			s.append("</").append(pstrName).append(">\n");
		}
	     
	}
    
    /**
    * Add Cdata to the XML.
    * 
    * <pre>
    * 
    * NOTE : This calls taggedCData(pstrName, pstrValue, true);
    * </pre>
    * 
    * @param pstrName The element name
    * @param pstrValue The element value. If this is null nothing nothing will be added.
     */
    public void taggedCData(String pstrName, String pstrValue) {
        taggedCData(pstrName, pstrValue, false);
    }
    
    /**
    * Add Cdata to the XML.
    * 
    * <pre>
    * 
    * As &lt;name>cdata&lt;/name>
    * </pre>
    *
    * @param pstrName The element name
    * @param pstrValue The element value.
    * @param pblnMandantory Print if empty. Optional, default should be false.
    */
    public void taggedCData(String pstrName, String pstrValue, boolean pblnMandantory) {
        
        if ( (StringUtil.len(pstrName) > 0 && StringUtil.len(pstrValue) > 0) || pblnMandantory) {
            /**
             * Set defaults
             */
            if (pstrValue == null) {
                pstrValue = "";
            }
            
            s.append(tabLevel())
            	.append("<").append(pstrName).append(">")
            		.append(XML_CDATA_OPEN).append(pstrValue).append(XML_CDATA_CLOSE)
            	.append("</").append(pstrName).append(">\n");
        }
        
    }
    
    /**
     * Construct attribute that can be added to a tag.
     *
     *<pre>
     *
     * Example : 
     * <input pstrName="pstrValue"/>
     *</pre>
     *
     * @param pstrName The name of the attribute 
     * @param pstrValue The value of the attribute.
     * @return Returns the returns a XML/HTML attribute.
     */
     public String attr(String pstrName, String pstrValue) {
         StringBuffer attr = new StringBuffer(); 
         if (StringUtil.len(pstrName) > 0) {
             attr.append(" ").append(pstrName).append("=\"").append(pstrValue).append("\"");
         }
         return attr.toString();
     }
     
    /**
     * Used to add some custom xml.
     * @param pstr The peice of xml you want to add.
     */
    public void addCustomContent(String pstr) {
        if (StringUtil.len(pstr) > 0) {
            s.append(tabLevel()).append(pstr);
        }
    }
    
    /** 
     * @return Returns the xml level.
     */
    public char[] tabLevel() {
        
        // Handle incorrect usage :
        if (this.level <= 0) {
            this.level = 0;
        }
        
        char tabLevel[] = new char[this.level];
        for (int i = 0; i < this.level; i++) {
            tabLevel[i] = TAB_CHAR;
        }
        
        return tabLevel;
    }
    
    /**
    * @return Returns the XML that we have generated. 
    */
    public String getXML(){
        String getXML = s.toString();
        
        // Reset the string buffer so we can reuse the handle to this object.
        s = new StringBuffer();
        this.level = 0;
        
        return getXML;
    }
    
    //----------------------------------------------------------- Object overwritten methods.
    
    /**
     * @see java.lang.Object#toString()
     **/
    public String toString(){
        return s.toString();
    }
    
    /** 
     * @see java.lang.Object#equals(java.lang.Object)
     **/
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof XMLGen) {
            XMLGen objXMLGen = (XMLGen)obj;
            if (objXMLGen.s.toString().equals(this.s.toString()) && this.level == objXMLGen.level) {
                return true;
            }
        }
        return false;
    }
}