package org.zxframework;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import org.zxframework.exception.ParsingException;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * The current parsing implementation for the descriptor. Ideally we would use SAX or Digestor.
 * However if we use these more performant parsers we may need to sacrifice the ability to use just
 * in time parsing.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DescriptorParserDomImpl extends ZXObject implements DescriptorParser{

	private Descriptor descriptor;
	
    //------------------------ XML Constants
    
    private static final String ATTR_NAME = "name";
    private static final String ATTR_HELPID = "helpid";
    private static final String ATTR_LABEL = "label";
    private static final String ATTR_DATATYPE = "datatype";
    private static final String ATTR_COMMENT = "comment";
    private static final String ATTR_TESTDATAVALUE = "testdatavalue";
    private static final String ATTR_PASSWORD = "password";
    private static final String ATTR_DYNAMICVALUE = "dynamicvalue";
    private static final String ATTR_MULTILINE = "multiline";
    private static final String ATTR_LENGTH = "length";
    private static final String ATTR_PRECISION = "precision";
    private static final String ATTR_FOREIGNKEY = "foreignkey";
    private static final String ATTR_FOREIGNKEYALIAS = "foreignkeyalias";
    private static final String ATTR_FKRELATTR = "fkrelattr";
    private static final String ATTR_REGULAREXPRESSION = "regularexpression";
    private static final String ATTR_EDITMASK = "editmask";
    private static final String ATTR_DEFAULTVALUE = "defaultvalue";
    private static final String ATTR_OPTIONAL = "optional";
    private static final String ATTR_EXPLICIT = "explicit";
    private static final String ATTR_SEARCHMETHOD = "searchmethod";
    private static final String ATTR_COLUMN = "column";
    private static final String ATTR_VIRTUALCOLUMN = "virtualcolumn";
    private static final String ATTR_LOCK = "lock";
    private static final String ATTR_CASE = "case";
    private static final String ATTR_LOWERRANGE = "lowerrange";
    private static final String ATTR_UPPERRANGE = "upperrange";
    private static final String ATTR_OUTPUTLENGTH = "outputlength";
    private static final String ATTR_OUTPUTMASK = "outputmask";
    private static final String ATTR_LIST = "list";
    private static final String ATTR_COMBOBOX = "combobox";
    private static final String ATTR_TAGS = "tags";
    private static final String ATTR_FKLABELGROUP = "fklabelgroup";
    private static final String ATTR_FKLABELEXPRESSION = "fklabelexpression";
    private static final String ATTR_SORTATTR = "sortattr";
    private static final String ATTR_ENSURELOADGROUP = "ensureloadgroup";
    
    private static final String ENTITY_BASEDON = "basedon";
    private static final String ENTITY_NAME = "name";
    private static final String ENTITY_HELPID = "helpid";
    private static final String ENTITY_VERSION = "version";
    private static final String ENTITY_LASTCHANGE = "lastchange";
    private static final String ENTITY_COMMENT = "comment";
    private static final String ENTITY_TESTDATAGROUP = "testdatagroup";
    private static final String ENTITY_TESTDATAVALIDATION = "testdatavalidation";
    private static final String ENTITY_DATACONSTANTGROUP = "dataconstantgroup";
    private static final String ENTITY_SIZE = "size";
    private static final String ENTITY_TABLE = "table";
    private static final String ENTITY_ALIAS = "alias";
    private static final String ENTITY_PRIMARYKEY = "primarykey";
    private static final String ENTITY_CLASSNAME = "jclassname";
    private static final String ENTITY_UNIQUECONSTRAINT = "uniqueconstraint";
    private static final String ENTITY_AUDITABLE = "auditable";
    private static final String ENTITY_DELETABLE = "deletable";
    private static final String ENTITY_DELETERULE = "deleterule";
    private static final String ENTITY_SEQUENCE = "sequence";
    private static final String ENTITY_LABEL = "label";
    private static final String ENTITY_EVENTACTIONS = "eventactions";
    private static final String ENTITY_TAGS = "tags";
    private static final String ENTITY_ATTRIBUTES = "attributes";
    private static final String ENTITY_BORELATIONS = "borelations";
    private static final String ENTITY_ATTRIBUTEGROUPS = "attributegroups";
    private static final String ENTITY_SECURITY = "security";
    private static final String ENTITY_DATASOURCE = "datasource";
	
	/**
	 * @see org.zxframework.DescriptorParser#parse(java.io.InputStream, Descriptor)
	 */
	public void parse(InputStream in, Descriptor pobjDesc) throws ParsingException {
		this.descriptor = pobjDesc;
		
        boolean blnBasedOnAuditable = false;
        
        /**
         * Load the xml file :
         */
        Element objXMLTopNode = null;
        try {
            Document doc = new SAXBuilder().build(in);
            objXMLTopNode = doc.getRootElement();
        } catch (Exception e) {
            throw new ParsingException("Failed to parse xml file", e);
        }

        String elementName;
        Iterator objXMLNode = objXMLTopNode.getChildren().iterator();
        while (objXMLNode.hasNext()) {
            Element element = (Element) objXMLNode.next();
            // Performance increases ?
            elementName = element.getName().intern();
            
            if (StringUtil.len(elementName) > 0) {
                if (elementName == ENTITY_BASEDON) {
                    if (StringUtil.len(descriptor.getBasedOn()) == 0) {
                        /**
                         *DGS11AUG2003: If the business object is based on another, we parse that one in full,
                         * then parse this one to overrule anything defined in both.
                         */
                    	descriptor.setBasedOn(element.getText());
                    	descriptor.setParsingBasedOn(true);
                        String strBasedOnXmlFile = getZx().fullPathName(getZx().getBoDir())  + File.separatorChar + descriptor.getBasedOn() + ".xml";
                        
                        Element objXMLTopNodeBasedOn = null;
                        try {
                            Document doc = new SAXBuilder().build(new File(strBasedOnXmlFile));
                            objXMLTopNodeBasedOn = doc.getRootElement();
                        } catch (Exception e) {
                            throw new ParsingException("Unable to load based on XML file " + strBasedOnXmlFile + " [" + e.getMessage() + "]", e);
                        }
                        objXMLNode = objXMLTopNodeBasedOn.getChildren().iterator();
                    }
                    
                } else if (elementName == ENTITY_NAME) {
                	descriptor.setName(element.getText());
                    
                } else if (elementName == ENTITY_HELPID) {
                	descriptor.setHelpId(element.getText());
                    
                } else if (elementName == ENTITY_VERSION) {
                	descriptor.setVersion(element.getText());
                    
                } else if (elementName == ENTITY_LASTCHANGE) {
                    if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
                    	descriptor.setLastChange(element.getText());
                    }
                    
                } else if (elementName == ENTITY_COMMENT) {
                    if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
                    	descriptor.setComment(element.getText());
                    }
                    
                } else if (elementName == ENTITY_TESTDATAGROUP) {
                    if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
                    	descriptor.setTestDataGroup(element.getText());
                    }
                    
                } else if (elementName == ENTITY_TESTDATAVALIDATION) {
                    if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
                    	descriptor.setTestDataValidation(element.getText());
                    }
                    
                /**
                 * DGS04FEB2004: Optional group of attributes that are constant values
                 */
                } else if (elementName == ENTITY_DATACONSTANTGROUP) {
                	descriptor.setDataConstantGroup(element.getText());
                    
                } else if (elementName == ENTITY_SIZE) {
                	descriptor.setSize(zXType.entitySize.getEnum(element.getText()));
                    
                } else if (elementName == ENTITY_TABLE) {
                	descriptor.setTable(element.getText());
                    
                } else if (elementName == ENTITY_ALIAS) {
                	descriptor.setAlias(element.getText());
                    
                } else if (elementName == ENTITY_PRIMARYKEY) {
                	// NOTE : Lower casing as attributte names are case insensitive.
                	descriptor.setPrimaryKey(element.getText().toLowerCase());
                    
                } else if (elementName == "classname") {
                	descriptor.setVBClassName(element.getText());
                    
                } else if (elementName == ENTITY_CLASSNAME) {
                	descriptor.setClassName(element.getText());
                    
                } else if (elementName == ENTITY_UNIQUECONSTRAINT) {
                	descriptor.setUniqueConstraint(element.getText());
                    
                } else if (elementName == ENTITY_AUDITABLE) {
                    /**
                     * BD9JUN04 - Here is some legacy at work: originally 'auditable'
                     * was a simlpe flag; since it has been changed to a list
                     * of options but we still have to support the simple flag
                     */
                	descriptor.setAuditableAsString(element.getText());
                    //this.auditable = StringUtil.booleanValue(element.getText());
                    
                } else if (elementName == ENTITY_DELETABLE) {
                	descriptor.setDeletable(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName == ENTITY_DELETERULE) {
                	descriptor.setDeleteRule(zXType.deleteRule.getEnum(element.getText()));
                    
                } else if (elementName == ENTITY_SEQUENCE) {
                	descriptor.setSequence(element.getText());
                    
                } else if (elementName == ENTITY_LABEL) {
                    setColLabel(element, null);
                    
                } else if (elementName == "description") {
                    // This is what parsing label does.
                	
                } else if (elementName == ENTITY_EVENTACTIONS) {
                    /**
                     * BD14MAY04 Added
                     */
                    setEventActions(element);
                    
                } else if (elementName == ENTITY_TAGS) {
                    /**
                     * DGS13OCT2003 Added
                     */
                	descriptor.setTags(parseTags(element));
                    
                } else if (elementName == ENTITY_ATTRIBUTES) {
                    
                    setAttributes(element);
                    
                    /**
                     * Optionally add the audit columns
                     * 26NOV2002 DGS: Must do this before parsing attr groups, in case reference any.
                     * so have moved this from beneath the "end select"
                     */
                    if (descriptor.isAuditable()) {
                        /**
                         * Add the audit attributes
                         * DGS11AUG2003: Support for basing on another bus obj xml:
                         * Ensure we don't add the audit attributes twice...
                         */
                        if (descriptor.isParsingBasedOn()) {
                            blnBasedOnAuditable = true;
                        }

                        if (descriptor.isParsingBasedOn() || !blnBasedOnAuditable) {
                            /**
                             *... by only adding the audit attributes if we are parsing the 'based
                             * on' xml and it is auditable, or we are parsing the 'real' xml and it
                             * is auditable and the based on wasn't.
                             */
                        	descriptor.addAuditAttributes();
                        }
                    }
                    
                } else if (elementName == ENTITY_BORELATIONS) {
                    setBORelations(element);
                    
                } else if (elementName == ENTITY_ATTRIBUTEGROUPS) {
                    setAttributeGroups(element);
                    
                } else if (elementName == ENTITY_SECURITY) {
                    setSecurity(element);
                    
                } else if (elementName == ENTITY_DATASOURCE) {
                	descriptor.setDataSource(element.getText());
                    
                } else {
                    getZx().log.warn("Unhandled tag : " + elementName);
                    
                }
                
                /**
                 * Before exiting add the based on attributes
                 */
                if (!objXMLNode.hasNext()) {
                    /**
                     * DGS11AUG2003: Support for basing on another bus obj xml.
                     * After parsing this xml, see if it was the 'based on' xml. If so,
                     * continue to parse the 'overlying' xml:
                     */
                    if (descriptor.isParsingBasedOn()) {
                        objXMLNode = objXMLTopNode.getChildren().iterator();
                        descriptor.setParsingBasedOn(false);
                    }
                }
            }
        }
        
	}
	
    /**
     * Parse and return a Single Attribute from a xml node.
     * 
     * @param pobj The xml node of a single attribute
     * @return Returns a single Attribute.
     * @throws ParsingException Thrown if parseSingleAttribute fails.
     */
    public Attribute parseSingleAttribute(Object pobj) throws ParsingException {
    	try {
            return parseSingleAttribute((Element)pobj, false);
    	} catch (Exception e) {
    		throw new ParsingException(e);
    	}
    }
    
    /**
     * parseSingleAttributeGroup - Parse single attribute group
     * 
     * @param pobj The element containing a single attribute group.
     * @param pcolAttrGroup A handle to the AttributeGroup.
     * @return Returns a single attribute group
     * @throws ParsingException Thrown if parseSingleAttributeGroup fails.
     */
    public AttributeGroup parseSingleAttributeGroup(Object pobj, AttributeGroup pcolAttrGroup) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobj);
        }
        
        AttributeGroup parseSingleAttributeGroup;
        if (pcolAttrGroup != null) {
        	parseSingleAttributeGroup = pcolAttrGroup;
        } else {
        	parseSingleAttributeGroup = new AttributeGroup();
        }
        
        try {
            Iterator iterXMLNode = ((Element)pobj).getChildren().iterator();
            while (iterXMLNode.hasNext()) {
                Element element = (Element) iterXMLNode.next();
                String elementName = element.getName();
                if (elementName.equalsIgnoreCase("name")) {
                	parseSingleAttributeGroup.setName(element.getText().toLowerCase());  // NOTE : Lower casing
                } else if (elementName.equalsIgnoreCase("attributes")) {
                    // Parse the list of attributes in an attribute group.
                	parseSingleAttributeGroup.setAttributes(parseAttributeGroupList(element));
                } else if (elementName.equalsIgnoreCase("comment")) {
                    if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
                    	parseSingleAttributeGroup.setComment(element.getText());
                    }
                }
            }
            parseSingleAttributeGroup.setParsed(true);

            return parseSingleAttributeGroup;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Parse single attribute group ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobj);
            }
            
            if (getZx().throwException) throw new ParsingException(e);
            return parseSingleAttributeGroup;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(parseSingleAttributeGroup);
                getZx().trace.exitMethod();
            }
        }
    }

	//------------------------ Private Helper Methods
	
    /**
     * setAttributes - Parse colection of attributes :
     * 
     * @param pobjXMLNode The xml element containing the attributes.
     * @throws ParsingException Thrown if setAttributes fails
     */
    private void setAttributes(Element pobjXMLNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        
        try {
            
            /**
             * Init attribute collection when necessary.
             */
            if (descriptor.getAttributes() == null) {
                descriptor.setAttributes(new AttributeCollection());
            }
            if (descriptor.getImplicitAttributes() == null) {
                descriptor.setImplicitAttributes(new AttributeCollection());
            }
            
            Attribute objAttr;
            Iterator iter = pobjXMLNode.getChildren().iterator();
            while (iter.hasNext()) {
                objAttr = parseSingleAttribute(iter.next());
                
                /**
                 * DGS11AUG2003: If there is a 'based on' descriptor and we are not in the 
                 * process of parsing that, we may already have included this attribute.
                 * If so, this one overrules that in the 'based on'.
                 *
                 * NOTE : The java collection class does this automatically.
                 */
                objAttr.setInherited(descriptor.isParsingBasedOn());
                
                /**
                 * Add this to the attribute collection for the business object.
                 */
                descriptor.getAttributes().put(objAttr.getName(), objAttr);
                
                /**
                 * Also add to group of implicit only attributes when
                 * required
                 */
                if (!objAttr.isExplicit()) {
                	descriptor.getImplicitAttributes().put(objAttr.getName(), objAttr);
                }
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Parse collection of attributes", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            
            if (getZx().throwException) throw new ParsingException(e);
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * parseSingleAttribute - Returns a single attribute from a xml node.
     * 
     * This can interprit both new and old style xml.
     * 
     * <pre>
     * 
     * V1.5:18 - BD17JUN05 - Added support for ensureLOadGroup
     * V1.5:20 - BD30JUN05 - Added support for enhanced FK label behaviour
     * V1.5:25 - BD1JUL05 - Added support order attribute for dynamic values
     * V1.5:91 - BD13FEB06 - Added support for test data
     * </pre>
     * 
     * @param pobjXMLNode The xml node of a single attribute
     * @param pblnOld Whether to parse of the old or new form of xml.
     * @return Returns a single attribute.
     * @throws ParsingException Thrown if parseSingleAttribute fails.
     */
    private Attribute parseSingleAttribute(Element pobjXMLNode, boolean pblnOld) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
            getZx().trace.traceParam("pblnOld", pblnOld);
        }
        
        Attribute parseSingleAttribute = new Attribute();

        try {
        	Iterator objXMLNode = pobjXMLNode.getChildren().iterator();
        	
            // Initialize values thjat may never be found
            parseSingleAttribute.setLowerRangeInclude(false);
            parseSingleAttribute.setUpperRangeInclude(false);
            parseSingleAttribute.setOptions(null);
            
            String elementName;
            
            while (objXMLNode.hasNext()) {
                Element element = (Element) objXMLNode.next();
                elementName = element.getName().toLowerCase().intern();

                if (elementName == ATTR_NAME) {
                    // NOTE : Names of attributes are lower case for case insensitive matches.
                    parseSingleAttribute.setName(element.getText().toLowerCase());
                    
                } else if (elementName == ATTR_HELPID) {
                    parseSingleAttribute.setHelpId(element.getText());
                    
                } else if (elementName == ATTR_LABEL) {
                    // Passes the label and the description
                    Element strDescription = null;
                    if (pblnOld) {
                        strDescription = (Element) objXMLNode.next();
                    }
                    parseSingleAttribute.setLabel(parseLabel(element, strDescription));
                    
                } else if (elementName == "description") {
                    // LEGACY : Check if this is called.
                    getZx().log.warn("Show not be able to reach this part of the code.");
                    
                } else if (elementName == ATTR_DATATYPE) {
                    String strElementText = element.getText().toLowerCase();
                    if(strElementText.startsWith("z")) {
                        strElementText = strElementText.substring(1);
                    }
                    parseSingleAttribute.setDataType(zXType.dataType.getEnum(strElementText));
                    
                } else if (elementName == ATTR_COMMENT) {
                    if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
                        parseSingleAttribute.setComment(element.getText());
                    }
                    
                } else if (elementName == ATTR_TESTDATAVALUE) {
                    if (getZx().getRunMode().pos == zXType.runMode.rmEditor.pos) {
                        parseSingleAttribute.setTestDataValue(element.getText());
                    }
                    
                } else if (elementName == ATTR_PASSWORD) {
                    parseSingleAttribute.setPassword(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName == ATTR_DYNAMICVALUE) {
                    parseSingleAttribute.setDynamicValue(element.getText());
                    
                } else if (elementName == ATTR_MULTILINE) {
                    parseSingleAttribute.setMultiLine(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName == ATTR_LENGTH) {
                    parseSingleAttribute.setLength(new Integer(element.getText()).intValue());
                    
                } else if (elementName == ATTR_PRECISION) {
                    parseSingleAttribute.setPrecision(Integer.parseInt(element.getText()));
                    
                } else if (elementName == ATTR_FOREIGNKEY) {
                    parseSingleAttribute.setForeignKey(element.getText());
                    
                } else if (elementName == ATTR_FOREIGNKEYALIAS) {
                    parseSingleAttribute.setForeignKeyAlias(element.getText());
                    
                } else if (elementName == ATTR_FKRELATTR) {
                    parseSingleAttribute.setFkRelAttr(element.getText());
                    
                } else if (elementName == ATTR_FKLABELGROUP) {
                    parseSingleAttribute.setFkLabelGroup(element.getText());
                    
                } else if (elementName == ATTR_FKLABELEXPRESSION) {
                    parseSingleAttribute.setFkLabelExpression(element.getText());
                    
                } else if (elementName == ATTR_REGULAREXPRESSION) {
                    parseSingleAttribute.setRegularExpression(element.getText());
                    
                } else if (elementName == ATTR_EDITMASK) {
                    parseSingleAttribute.setEditMask(element.getText());
                    
                } else if (elementName == ATTR_DEFAULTVALUE) {
                    parseSingleAttribute.setDefaultValue(element.getText());
                    
                } else if (elementName == ATTR_OPTIONAL) {
                    parseSingleAttribute.setOptional(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName == ATTR_EXPLICIT) {
                    parseSingleAttribute.setExplicit(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName == ATTR_SEARCHMETHOD) {
                    parseSingleAttribute.setSearchMethod(zXType.searchMethod.getEnum(element.getText()));
                    
                } else if (elementName == ATTR_COLUMN) {
                    parseSingleAttribute.setColumn(element.getText());
                    
                } else if (elementName == ATTR_VIRTUALCOLUMN) {
                    parseSingleAttribute.setVirtualColumn(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName == ATTR_SORTATTR) {
                    parseSingleAttribute.setSortAttr(element.getText());
                    
                } else if (elementName == ATTR_ENSURELOADGROUP) {
                    parseSingleAttribute.setEnsureLoadGroup(element.getText());
                    
                } else if (elementName == "null") {
					/**
					 * BD20MAR04 Now officialy obsolete; leave case statement in
					 * only for backward compatibility
					 */
                    getZx().log.warn("Use of a legacy method.");
                    
                } else if (elementName == ATTR_LOCK) {
                    parseSingleAttribute.setLocked(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName == ATTR_CASE) {
                    parseSingleAttribute.setTextCase(zXType.textCase.getEnum(element.getText()));
                    
                } else if (elementName == ATTR_LOWERRANGE) {
                    parseSingleAttribute.setLowerRange(element.getText());
                    String lowerRangeInclude = element.getAttributeValue("include");
                    if (StringUtil.len(lowerRangeInclude) > 0) {
                        parseSingleAttribute.setLowerRangeInclude(StringUtil.booleanValue(element.getText()));
                    }
                    
                } else if (elementName == ATTR_UPPERRANGE) {
                    parseSingleAttribute.setUpperRange(element.getText());
                    String upperRangeInclude = element.getAttributeValue("include");
                    if (StringUtil.len(upperRangeInclude) > 0) {
                        parseSingleAttribute.setUpperRangeInclude(StringUtil.booleanValue(element.getText()));
                    }
                    
                } else if (elementName == ATTR_OUTPUTLENGTH) {
                    parseSingleAttribute.setOutputLength(new Integer(element.getText()).intValue());
                    
                } else if (elementName == ATTR_OUTPUTMASK) {
                    parseSingleAttribute.setOutputMask(element.getText());
                    
                } else if (elementName == ATTR_LIST) {
                    parseSingleAttribute.setOptions(parseList(element));
                    
                } else if (elementName == ATTR_COMBOBOX) {
                    parseSingleAttribute.setCombobox(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName == ATTR_TAGS) {
                    /**
                     * DGS13OCT2003 Added
                     */
                    parseSingleAttribute.setTags(parseTags(element));
                    
                } else {
                    getZx().log.warn("Unknown xml tag : " + elementName);
                }
                
            }
            
            return parseSingleAttribute;

        } catch (Exception e) {
            getZx().trace.addError("Failed to : parseSingleAttribute - Returns a single attribute from a xml node. ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
                getZx().log.error("Parameter : pblnOld = " + pblnOld);
            }
            
            if (getZx().throwException) { throw new ParsingException(e); }
            return parseSingleAttribute;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(parseSingleAttribute);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * parseList - Parse list of options
     * 
     * @param pobjXMNode Handle to a node that is a label.
     * @return A collection of options.
     * @throws ParsingException Thrown if parseList fails. This is normally a programming error.
     */
    private ZXCollection parseList(Element pobjXMNode) throws ParsingException {
        ZXCollection parseList = new ZXCollection();
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMNode", pobjXMNode);
        }
        try {
            Option option;
            Iterator objXMLNode = pobjXMNode.getChildren().iterator();
            while (objXMLNode.hasNext()) {
                option = parseSingleOption((Element) objXMLNode.next());
                parseList.put(option.getValue(), option);
            }
            return parseList;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : parseList - Parse list of options ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMNode = " + pobjXMNode);
            }
            if (getZx().throwException) throw new ParsingException(e);
            return parseList;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(parseList);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * parseSingleOption - Parse and return a single option.
     * 
     * @param pobjXMLNode The element of a single option.
     * @return Returns a Single Option
     * @throws ParsingException Thrown if parseSingleOption fails
     */
    private Option parseSingleOption(Element pobjXMLNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        
        Option parseSingleOption = new Option();

        try {
            Iterator objXMLNode = pobjXMLNode.getChildren().iterator();
            while (objXMLNode.hasNext()) {
                Element element = (Element) objXMLNode.next();
                String elementName = element.getName();
                if (elementName.equals("value")) {
                    parseSingleOption.setValue(element.getText());
                } else if (elementName.equals("label")) {
                    parseSingleOption.setLabel(parseLabel(element));
                } else if (elementName.equals("description")) {
                    parseSingleOption.setDescription(parseLabel(element));
                } else {
                    getZx().log.error("Unhandled tag : " + elementName);
                }
            }
            return parseSingleOption;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : parseSingleOption - Parse and return a single option.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            
            if (getZx().throwException) throw new ParsingException(e);
            return parseSingleOption;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(parseSingleOption);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * parseTags - Parse the tag entries and set Descriptor's tags.
     * 
     * @param pobjXMLNode The xml node with the tags.
     * @return Returns a collections of tags.
     * @throws ParsingException Thrown if parseTags fails
     */
    private ZXCollection parseTags(Element pobjXMLNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        
        ZXCollection parseTags = new ZXCollection();
        
        try {
            Element objChildTag;
            Tuple objTag;
            
            Iterator objChildOpt = pobjXMLNode.getChildren().iterator();
            while (objChildOpt.hasNext()) {
                objChildTag = (Element) objChildOpt.next();
                
                objTag = new Tuple();
                objTag.setName(objChildTag.getChildText("name"));
                objTag.setValue(objChildTag.getChildText("value"));
                
                parseTags.put(objTag.getName(), objTag);
                
                /**
                 * If there is a 'based on' descriptor and we are not in the
                 * process of parsing trhat, we may already have included this
                 * tag if so this one overrules that in the 'based on'.
                 * 
                 * 
                 * NOTE : Not really necessary as Hashmaps does this for you
                 * automatically which is what the zXCollection class is using.
                 * 
                 * if (basedOn != null && !basedOn == null &&
                 * !parsingBasedOn) {
                 *  }
                 */
            }
            return parseTags;
        } catch (Exception e) {
            if (getZx().log.isErrorEnabled()) {
                getZx().trace.addError("Failed to : setTags - Parse the tag entries and set Descriptor's tags.", e);
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            
            if (getZx().throwException) throw new ParsingException(e);
            return parseTags;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * parseAttrValues - Parse the attribute values for a event action
     * 
     * @param pobjXMLNode The xml node with the attribute values.
     * @return Returns a collection (Tuple) of attribute values.
     * @throws ParsingException Thrown if parseAttrValues fails.
     */
    private List parseAttrValues(Element pobjXMLNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        
        List parseAttrValues = new ArrayList();
        
        try {
            
            Element objChildAttrValue;
            Tuple objAttrValue;
            Iterator objChildOpt = pobjXMLNode.getChildren().iterator();
            while (objChildOpt.hasNext()) {
                objChildAttrValue = (Element) objChildOpt.next();
                
                objAttrValue = new Tuple();
                objAttrValue.setName(objChildAttrValue.getChildText("name"));
                objAttrValue.setValue(objChildAttrValue.getChildText("value"));
                
                parseAttrValues.add(objAttrValue);
            }
            
            return parseAttrValues;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : parseAttrValues - Parse the attribute values for a event action", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            
            if (getZx().throwException) throw new ParsingException(e);
            return parseAttrValues;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }    
    
    /**
     * Called by newer version of the xml parsing ...
     * 
     * @param labelNode
     * @return Returns the LabelCollection of a xml node.
     * @deprecated Use parseLabel(Element labelNode, Element description)
     *                    instead.
     * @throws ParsingException
     */
    private LabelCollection parseLabel(Element labelNode) throws ParsingException {
        /**
         * Hack for older versions :
         */
        Element desriptionNode = null;
        return parseLabel(labelNode, desriptionNode);
    }

    /**
     * parseLabel - Returns a LabelCollection from the label xml node.
     * 
     * A hack to get the descriptions of the labels.
     * 
     * @param labelNode The label element.
     * @param descriptionNode The decription element. Based on older code. Will be depricated in the future.
     * @return Returns a LabelCollection
     * @throws ZXException Thrown if parseLabel fails.
     */
    private LabelCollection parseLabel(Element labelNode, Element descriptionNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("labelNode", labelNode);
            getZx().trace.traceParam("descriptionNode", descriptionNode);
        }

        LabelCollection parseLabel = new LabelCollection();

        try {
            Element objLabelElement;
            String labelLanguage;
            String strDescription;
            String labelStr;
            
            Iterator labelsByLang = labelNode.getChildren().iterator();
            Iterator descriptionsByLang = null;
            
            if (descriptionNode != null) {
                descriptionsByLang = descriptionNode.getChildren().iterator();
            }
            
            while (labelsByLang.hasNext()) {
                objLabelElement = (Element) labelsByLang.next();
                labelLanguage = objLabelElement.getName();
                labelStr = "";
                strDescription = "Unable to parse description";

                if (descriptionNode == null) {
                    // Proper way
                    labelStr = objLabelElement.getChildText("label");
                    strDescription = objLabelElement.getChildText("description");
                } else if (descriptionsByLang != null){
                    /**
                     * Old style xml :
                     */
                    Element descriptionElement = (Element) descriptionsByLang.next();
                    if (descriptionElement.getName().equals(labelLanguage)) {
                        strDescription = descriptionElement.getText();
                    }
                }
                
                //Label objLabel = new Label(labelLanguage, labelStr, strDescription);
                Label objLabel = new Label();
                objLabel.setLanguage(labelLanguage);
                objLabel.setLabel(labelStr);
                objLabel.setDescription(strDescription);
                
                /**
                 * DGS04FEB2004: (Missed when doing the based on change last August) 
                 * If there is a 'based on' descriptor and we are not in the
                 * process of parsing that, we may already have included this label.
                 * If so, this one overrules that in the 'based on'.
                 */
                if (this.descriptor != null && StringUtil.len(descriptor.getBasedOn()) > 0 &&  !descriptor.isParsingBasedOn()) {
                	parseLabel.remove(objLabel.getLanguage());
                }
                
                parseLabel.put(objLabel.getLanguage(), objLabel);
            }
            
            return parseLabel;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : parseLabel - Returns a LabelCollection from the label xml node.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : labelNode = " + labelNode);
                getZx().log.error("Parameter : descriptionNode =  " + descriptionNode);
            }
            
            if (getZx().throwException) throw new ParsingException(e);
            return parseLabel;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(parseLabel);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * @param pobjXMLNode  Handle to XML node that is a title
     * @return Returns a collection with all titles, using language as key
     * @throws ParsingException Thrown if parseTitle fails
     */
    private LabelCollection parseTitle(Element pobjXMLNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        
        LabelCollection parseTitle = new LabelCollection();

        try {
            Element element;
            Label objLabel;
            
            Iterator labelsByLang = pobjXMLNode.getChildren().iterator();
            while (labelsByLang.hasNext()) {
                element = (Element) labelsByLang.next();
                
                objLabel = new Label();
                objLabel.setLanguage(element.getName());
                objLabel.setLabel(element.getText());
                
                parseTitle.put(objLabel.getLanguage(), objLabel);
            }
            return parseTitle;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : parseLabel - Returns a LabelCollection from the label xml node.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " +  pobjXMLNode);
            }
            
            if (getZx().throwException) throw new ParsingException(e);
            return parseTitle;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(parseTitle);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * setColLabel - Sets the ColLabel from the label xml node.
     * 
     * A hack to get the descriptions of the labels. This setz the Descriptors
     * ColLabels
     * 
     * @param labelNode The label element.
     * @param descriptionNode The decription element.
     * @throws ParsingException
     */
    private void setColLabel(Element labelNode, Element descriptionNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("labelNode", labelNode);
            getZx().trace.traceParam("descriptionNode", descriptionNode);
        }
        
        try {
            Iterator labelsByLang = labelNode.getChildren().iterator();

            Iterator descriptionsByLang = null;
            if (descriptionNode != null) {
                descriptionsByLang = descriptionNode.getChildren().iterator();
            }

            while (labelsByLang.hasNext()) {
                Element objLabelElement = (Element) labelsByLang.next();
                String labelLanguage = objLabelElement.getName();

                String labelStr = "";
                String descriptionStr = "Unable to parse description";
                // This should be the norm :
                if (descriptionNode == null) {
                    labelStr = objLabelElement.getChildText("label");
                    descriptionStr = objLabelElement.getChildText("description");
                } else if (descriptionsByLang != null){
                    // This will have to be factored out.
                    Element descriptionElement = (Element) descriptionsByLang.next();
                    if (descriptionElement.getName().equals(labelLanguage)) {
                        descriptionStr = descriptionElement.getText();
                    }
                    
                }

                // Label objLabel = new Label(labelLanguage, labelStr, descriptionStr);
                Label objLabel = new Label();
                objLabel.setLanguage(labelLanguage);
                objLabel.setLabel(labelStr);
                objLabel.setDescription(descriptionStr);
                
                descriptor.getLabel().put(objLabel.getLanguage(), objLabel);
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : setColLabel - Sets the ColLabel from the label xml node. ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : labelNode = " + labelNode);
                getZx().log.error("Parameter : descriptionNode = " + descriptionNode);
            }
            
            if (getZx().throwException) throw new ParsingException(e);
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Parse collection of event actions
     * 
     * @param pobjXMLNode Handle to XML node that contains event actions
     * @throws ParsingException Thrown if setEventActions fails
     */
    private void setEventActions(Element pobjXMLNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        
        try {
            
            if (descriptor.getEventActions() == null) {
            	descriptor.setEventActions(new ArrayList());
            }
            EventAction objEventAction;
            Iterator objXMLNode = pobjXMLNode.getChildren().iterator();
            Element element;
            String elementName;
            while (objXMLNode.hasNext()) {
                element = (Element) objXMLNode.next();
                elementName = element.getName();
                if (elementName.equals("action")) {
                    objEventAction = parseSingleEventAction(element);
                    descriptor.getEventActions().add(objEventAction);
                } else if (elementName.equals("preevents")) {
                    descriptor.setPreEvents(element.getText());
                } else if (elementName.equals("postevents")) {
                	descriptor.setPostEvents(element.getText());
                }
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : setEventActions - Parse the Event Action. ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            
            if (getZx().throwException) { throw new ParsingException(e); }
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }        
    }
    
    /**
     * Parse a single BO relation
     * 
     * BD7JUL05 - V1.5:29 - Added ensureLoaded
     * BD26MAR06 - V1.5:95 - Added extendGroup
     * 
     * @param pobjXMLNode Handle to XML node that is an eventAction
     * @return Returns the EventAction from the xml.
     * @throws ParsingException Thrown if the parseSingleBORelation fails.
     */
    private EventAction parseSingleEventAction(Element pobjXMLNode) throws ParsingException {
        EventAction parseSingleEventAction = new EventAction();
        try {
            Iterator objXMLNode = pobjXMLNode.getChildren().iterator();
            while (objXMLNode.hasNext()) {
                
                Element element = (Element) objXMLNode.next();
                String elementName = element.getName();
                
                if (elementName.equals("name")) {
                    parseSingleEventAction.setName(element.getText());
                    
                } else if (elementName.equals("comment")) {
                    if (getZx().getRunMode().pos ==zXType.runMode.rmEditor.pos) {
                        parseSingleEventAction.setComment(element.getText());
                    }
                    
                } else if (elementName.equals("msg")) {
                    parseSingleEventAction.setMsg( parseTitle(element) );
                    
                } else if (elementName.equals("action")) {
                    parseSingleEventAction.setAction(element.getText());
                    
                } else if (elementName.equals("timing")) {
                    parseSingleEventAction.setTiming( zXType.eaTiming.getEnum(element.getText()));
                    
                } else if (elementName.equals("notnullgroup")) {
                    parseSingleEventAction.setNotNullGroup(element.getText());
                    
                } else if (elementName.equals("wheregroup")) {
                    parseSingleEventAction.setWhereGroup(element.getText());
                    
                } else if (elementName.equals("activegroup")) {
                    parseSingleEventAction.setActiveGroup(element.getText());
                    
                } else if (elementName.equals("groupbehaviour")) {
                    parseSingleEventAction.setGroupBehaviour(zXType.eaGroupBehaviour.getEnum(element.getText()));
                    
                } else if (elementName.equals("activeid")) {
                    /**
                     * Always lowercase it for convenience of
                     * defining event actions
                     */
                    parseSingleEventAction.setActiveId(element.getText().toLowerCase());
                } else if (elementName.equals("idbehaviour")) {
                    parseSingleEventAction.setIDBehaviour(zXType.eaIDBehaviour.getEnum(element.getText()));
                    
                } else if (elementName.equals("active")) {
                    parseSingleEventAction.setActive(element.getText());
                    
                } else if (elementName.equals("continuation")) {
                    parseSingleEventAction.setContinuation(zXType.eaContinuation.getEnum(element.getText()));
                    
                } else if (elementName.equals("bovalidation")) {
                    parseSingleEventAction.setBOValidation(StringUtil.booleanValue(element.getText()));
                    
                } else if (elementName.equals("focusattribute")) {
                    parseSingleEventAction.setFocusAttribute(element.getText());
                    
                } else if (elementName.equals("events")) {
                    parseSingleEventAction.setEvents(element.getText());
                    
                } else if (elementName.equals("returncode")) {
                    parseSingleEventAction.setReturnCode(zXType.rc.getEnum(element.getText()));
                    
                } else if (elementName.equals("ensureloaded")) {
                    parseSingleEventAction.setEnsureLoaded(element.getText());
                    
                } else if (elementName.equals("extendgroup")) {
                    parseSingleEventAction.setExtendgroup(element.getText());
                    
                } else if (elementName.equals("attrvalues")) {
                    parseSingleEventAction.setAttrValues(parseAttrValues(element));
                    
                }
                
            }
        } catch (Exception e) {
            getZx().trace.addError("Failed to : parseSingleEventAction - Parse the attribute groups. ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            
            if (getZx().throwException) { throw new ParsingException(e); }
        }
        return parseSingleEventAction;
    }
    
    /**
     * Parse the bo relations
     * 
     * @param pobjXMLNode The xml node for the BO relations.
     * @throws ParsingException Thrown if setBORelations fails
     */
    private void setBORelations(Element pobjXMLNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        try {
            
            if (descriptor.getBORelations() == null) {
            	descriptor.setBORelations(new ArrayList());
            }
            BORelation objRelation;
            Iterator objXMLNode = pobjXMLNode.getChildren().iterator();
            Element element;
            while (objXMLNode.hasNext()) {
                element = (Element) objXMLNode.next();
                objRelation = parseSingleBORelation(element);
                descriptor.getBORelations().add(objRelation);
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : setBORelations - Parse the BO relatiions. ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            
            if (getZx().throwException) { throw new ParsingException(e); }
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }        
    }
    
    /**
     * Parse a single BO relation
     * 
     * @param pobjXMLNode The xml element with the bo relation
     * @return Returns the BORelation from the xml.
     * @throws ParsingException Thrown if the parseSingleBORelation fails.
     */
    private BORelation parseSingleBORelation(Element pobjXMLNode) throws ParsingException {
        BORelation parseSingleBORelation = new BORelation();
        try {
            Iterator objXMLNode = pobjXMLNode.getChildren().iterator();
            while (objXMLNode.hasNext()) {
                Element element = (Element) objXMLNode.next();
                String elementName = element.getName();
                if (elementName.equals("entity")) {
                    parseSingleBORelation.setEntity(element.getText());
                } else if (elementName.equals("fkattr")) {
                    parseSingleBORelation.setFKAttr(element.getText());
                } else if (elementName.equals("deleterule")) {
                    parseSingleBORelation.setDeleteRule( zXType.deleteRule.getEnum(element.getText()));
                }
            }
        } catch (Exception e) {
            getZx().trace.addError("Failed to : parseSingleBORelation - Parse the bo relation. ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            if (getZx().throwException) { throw new ParsingException(e); }
        }
        return parseSingleBORelation;
    }
    
    /**
     * setAttributeGroups - Parse the attribute groups.
     * 
     * @param pobjXMLNode The xml node or element of the attribute groups.
     * @throws ParsingException Thrown if setAttributeGroups fails
     */
    private void setAttributeGroups(Element pobjXMLNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        try {
            
            Iterator objXMLNode = pobjXMLNode.getChildren().iterator();
            while (objXMLNode.hasNext()) {
                Element element = (Element) objXMLNode.next();
                AttributeGroup objGroup = new AttributeGroup();
                
                if (descriptor.isJustInTime()) {
                    /**
                     * If just in time, we do not have to parse. just save the
                     * xml and the name.
                     */
                    objGroup.setXmlNode(element);
                    objGroup.setName(element.getChildText("name").toLowerCase());

                    objGroup.setParsed(false);
                } else {
                    objGroup = parseSingleAttributeGroup(element, objGroup);
                }

                /**
                 * Not needed as has if (!basedOn == null &&
                 * !parsingBasedOn) { }
                 */
                objGroup.setInherited(descriptor.isParsingBasedOn());
                
                descriptor.getAttributeGroups().put(objGroup.getName(), objGroup);
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : setAttributeGroups - Parse the attribute groups. ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            
            if (getZx().throwException) { throw new ParsingException(e); }
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * parseAttributeGroupList - Parse the attributes in an attributegroup.
     * 
     * @param pobjXMLNode The element containing the attributes of a attribute group
     * @return Returns AttributeCollection
     * @throws ParsingException Thrown if parseAttributeGroupList fails.
     */
    private AttributeCollection parseAttributeGroupList(Element pobjXMLNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        
        AttributeCollection parseAttributeGroupList = new AttributeCollection();
        
        try {
            Element element;
            String elementName;
            Attribute objAttr;
            
            Iterator objXMLNode = pobjXMLNode.getChildren().iterator();
            while (objXMLNode.hasNext()) {
                element = (Element) objXMLNode.next();
                elementName = element.getName();
                if (elementName.equalsIgnoreCase("attribute")) {
                    objAttr = descriptor.getAttribute(element.getText().toLowerCase());
                    if (objAttr != null) {
                        parseAttributeGroupList.put(objAttr.getName(), objAttr);
                    }
                }
            }
            
            return parseAttributeGroupList;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Parse the attributes in an attributegroup. ",e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            
            if (getZx().throwException) throw new ParsingException(e);
            return parseAttributeGroupList;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(parseAttributeGroupList);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * setSecurity - Parse the security section of the xml.
     * 
     * @param pobjXMLNode XML node with all of the security settings.
     * @throws ParsingException Thrown if setSecurity fails.
     */
    public void setSecurity(Element pobjXMLNode) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        try {
            Iterator objXMLNode = pobjXMLNode.getChildren().iterator();
            
            while (objXMLNode.hasNext()) {
                Element element = (Element) objXMLNode.next();
                String elementName = element.getName();
                
                if (elementName.equals("update")) {
                	descriptor.setSecurityUpdate(parseSingleSecurity(element));
                } else if (elementName.equals("select")) {
                	descriptor.setSecuritySelect(parseSingleSecurity(element));
                } else if (elementName.equals("insert")) {
                	descriptor.setSecurityInsert(parseSingleSecurity(element));
                } else if (elementName.equals("delete")) {
                	descriptor.setSecurityDelete(parseSingleSecurity(element));
                }
                
            }
        } catch (Exception e) {
            getZx().trace.addError("Failed to : setSecurity - Parse the security section of the xml. ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjXMLNode = " + pobjXMLNode);
            }
            
            if (getZx().throwException) { throw new ParsingException(e); }
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * parseSingleSecurity - Parse the SingleSecurity section
     * 
     * @param pobjXMLNode Handle to the xml node with the permissions
     * @return Returns a ZXCOllection of a single security element.
     */
    private ZXCollection parseSingleSecurity(Element pobjXMLNode) {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjXMLNode", pobjXMLNode);
        }
        
        ZXCollection parseSingleSecurity = new ZXCollection();
        
        try {
            
            Iterator objXMLNode = pobjXMLNode.getChildren().iterator();
            while (objXMLNode.hasNext()) {
                Element element = (Element) objXMLNode.next();
                
                Tuple objTuple = new Tuple();
                objTuple.setName(element.getText());
                objTuple.setValue(element.getText());
                
                parseSingleSecurity.put(objTuple.getName(), objTuple);
            }
            
            return parseSingleSecurity;
            
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(parseSingleSecurity);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Repository helper methods.
    
    /**
	 * dumpAsXML - Dumps this object as a formatted xml string.
	 * 
	 * <pre>
	 * 
	 * V1.5:20 - BD30JUN05 - Added support for enhanced FK label behaviour
	 * V1.5:29 - BD8JUL05 - Added ensureLoaded
	 * V1.5:91 - BD13FEB06 - Added test data support
	 * V1.5:95 - BD26MAR06 - Added extendGroup for eventAction
	 * </pre>
	 * 
	 * @param pobjDesc A handle to the bo descriptor to dump
	 * @return A xml representation of the Descriptor.
	 */
	public static String dumpAsXML(Descriptor pobjDesc) {
		XMLGen dumpAsXML = new XMLGen(1000);
		dumpAsXML.xmlHeader();

		try {
			dumpAsXML.openTag("entity");

			dumpAsXML.taggedValue(ENTITY_NAME, pobjDesc.getName(), true);
			dumpAsXML.taggedValue(ENTITY_BASEDON, pobjDesc.getBasedOn());
			dumpAsXML.taggedCData(ENTITY_VERSION, pobjDesc.getVersion());
			dumpAsXML.taggedValue(ENTITY_LASTCHANGE, pobjDesc.getLastChange());
			dumpAsXML.taggedCData(ENTITY_COMMENT, pobjDesc.getComment());
			dumpAsXML.taggedCData(ENTITY_TESTDATAGROUP, pobjDesc.getTestDataGroup());
			dumpAsXML.taggedCData(ENTITY_TESTDATAVALIDATION, pobjDesc.getTestDataValidation());
			
			dumpAsXML.openTag(ENTITY_LABEL);
			Iterator objLabels = pobjDesc.getLabel().iterator();
			while (objLabels.hasNext()) {
				Label objLabel = (Label) objLabels.next();

				dumpAsXML.openTag(objLabel.getLanguage());
				dumpAsXML.taggedCData("label", objLabel.getLabel());
				dumpAsXML.taggedCData("description", objLabel.getDescription());
				dumpAsXML.closeTag(objLabel.getLanguage());
			}
			dumpAsXML.closeTag(ENTITY_LABEL);

			if (pobjDesc.getTags() != null && !pobjDesc.getTags().isEmpty()) {
				dumpAsXML.openTag(ENTITY_TAGS);

				Iterator objTags = pobjDesc.getTags().iterator();
				while (objTags.hasNext()) {
					Tuple objTag = (Tuple) objTags.next();

					dumpAsXML.openTag("tag");
					dumpAsXML.taggedValue("name", objTag.getName());
					dumpAsXML.taggedValue("value", objTag.getValue());
					dumpAsXML.closeTag("tag");

				}
				dumpAsXML.closeTag(ENTITY_TAGS);
			}

			dumpAsXML.taggedValue(ENTITY_HELPID, pobjDesc.getHelpId());
			// BD10APR01 Added unique constraint
			dumpAsXML.taggedValue(ENTITY_UNIQUECONSTRAINT, pobjDesc
					.getUniqueConstraint());
			dumpAsXML.taggedValue(ENTITY_SIZE, zXType.valueOf(pobjDesc.getSize()));
			dumpAsXML.taggedValue(ENTITY_TABLE, pobjDesc.getTable());
			dumpAsXML.taggedValue("sequence", pobjDesc.getSequence());
			// BD16JAN01 Had gone missing, re-added- Not in java version.
			// MB18SEPT04 - Added jclassname for java files.
			dumpAsXML.taggedValue(ENTITY_CLASSNAME, pobjDesc.getClassName(),
					true);
			dumpAsXML.taggedValue(ENTITY_PRIMARYKEY, pobjDesc.getPrimaryKey());
			/**
			 * C1.5:10 DGS04MAY2005: Don't write out this tag if is the 'primary' datasource.
			 * Helps maintain backward compatibility if edited using later zX.
			 */
			if (StringUtil.len(pobjDesc.getDataSource()) != 0 && pobjDesc.getDataSource().equalsIgnoreCase("primary")) {
				dumpAsXML.taggedValue(ENTITY_DATASOURCE, pobjDesc.getDataSource());
			}

			dumpAsXML.taggedValue(ENTITY_AUDITABLE, pobjDesc.getAuditableAsString());

			/**
			 * DGS04FEB2004: Added Data Constant attribute group
			 */
			dumpAsXML.taggedValue(ENTITY_DATACONSTANTGROUP, pobjDesc.getDataConstantGroup());
			/**
			 * DGS290101 added Deletable:
			 * BD7DEC02 Delete-able now replaced with deleterule
			 */
			dumpAsXML.taggedValue(ENTITY_DELETERULE, pobjDesc.getDeleteRuleAsString());

			dumpAsXML.openTag(ENTITY_ATTRIBUTES);

			Iterator objAttributes = pobjDesc.getAttributes().iterator();
			while (objAttributes.hasNext()) {
				Attribute objAttr = (Attribute) objAttributes.next();
				String strAttributeName = objAttr.getName();
				
				/**
				 * DGS11AUG2003: Ignore any 'based on' columns
				 */
				if (!objAttr.isInherited()) {
					if (!strAttributeName.equalsIgnoreCase("zXCrtdBy")
						&& !strAttributeName.equalsIgnoreCase("zXCrtdWhn")
						&& !strAttributeName.equalsIgnoreCase("zXUpdtdBy")
						&& !strAttributeName.equalsIgnoreCase("zXUpdtdWhn")
						&& !strAttributeName.equalsIgnoreCase("zXCrtdBy")
						&& !strAttributeName.equalsIgnoreCase("zXUpdtdId")) {

						dumpAsXML.openTag("attribute");

						dumpAsXML.taggedValue(ATTR_NAME, strAttributeName);

						dumpAsXML.openTag(ATTR_LABEL);
						Iterator objAttrLabels = objAttr.getLabel().iterator();
						while (objAttrLabels.hasNext()) {
							Label objLabel = (Label) objAttrLabels.next();

							dumpAsXML.openTag(objLabel.getLanguage().toUpperCase());
							dumpAsXML.taggedValue("label", objLabel.getLabel());
							dumpAsXML.taggedValue("description", objLabel.getDescription());
							dumpAsXML.closeTag(objLabel.getLanguage().toUpperCase());

						}
						dumpAsXML.closeTag(ATTR_LABEL);

						dumpAsXML.taggedValue(ATTR_HELPID, objAttr.getHelpId());
						dumpAsXML.taggedValue(ATTR_DATATYPE, objAttr.getDataTypeAsString());
						dumpAsXML.taggedCData(ATTR_COMMENT, objAttr.getComment());
						dumpAsXML.taggedCData(ATTR_TESTDATAVALUE, objAttr.getTestDataValue());
						dumpAsXML.taggedValue(ATTR_PASSWORD, objAttr.isPassword());
						dumpAsXML.taggedValue(ATTR_MULTILINE, objAttr.isMultiLine());
						dumpAsXML.taggedValue(ATTR_LENGTH, String.valueOf(objAttr.getLength()));
						dumpAsXML.taggedValue(ATTR_PRECISION, String.valueOf(objAttr.getPrecision()));
						dumpAsXML.taggedValue(ATTR_FOREIGNKEY, objAttr.getForeignKey());
						dumpAsXML.taggedValue(ATTR_FOREIGNKEYALIAS, objAttr.getForeignKeyAlias());
						dumpAsXML.taggedValue(ATTR_DYNAMICVALUE, objAttr.getDynamicValue());
						// BD8MAY01
						dumpAsXML.taggedValue(ATTR_FKRELATTR, objAttr.getFkRelAttr());
						// BD30JUN05 - V1.5:20
						dumpAsXML.taggedValue(ATTR_FKLABELGROUP, objAttr.getFkLabelGroup());
						dumpAsXML.taggedValue(ATTR_FKLABELEXPRESSION, objAttr.getFkLabelExpression());
						dumpAsXML.taggedValue(ATTR_REGULAREXPRESSION, objAttr.getRegularExpression());
						dumpAsXML.taggedValue(ATTR_OPTIONAL, objAttr.isOptional());
						dumpAsXML.taggedValue(ATTR_DEFAULTVALUE, objAttr.getDefaultValue());
						dumpAsXML.taggedValue(ATTR_SEARCHMETHOD, objAttr.getSearchMethodAsString());
						dumpAsXML.taggedValue(ATTR_COLUMN, objAttr.getColumn());
						dumpAsXML.taggedValue(ATTR_SORTATTR, objAttr.getSortAttr());
						dumpAsXML.taggedValue(ATTR_ENSURELOADGROUP, objAttr.getEnsureLoadGroup());
						dumpAsXML.taggedValue(ATTR_VIRTUALCOLUMN, objAttr.isVirtualColumn());
						dumpAsXML.taggedValue(ATTR_LOCK, objAttr.isLocked());

						if (objAttr.isExplicit()) {
							dumpAsXML.taggedValue(ATTR_EXPLICIT, blnAsZXStr(objAttr.isExplicit()));
						}

						dumpAsXML.taggedValue(ATTR_CASE, objAttr.getTextCaseAsString());

						String lowerrange = objAttr.getLowerRange();
						if (StringUtil.len(lowerrange) > 0) {
							dumpAsXML.taggedValueWithAttr(ATTR_LOWERRANGE, lowerrange, 
														 "include", blnAsZXStr(objAttr.isLowerRangeInclude()));
						}

						String upperrange = objAttr.getLowerRange();
						if (StringUtil.len(upperrange) > 0) {
							dumpAsXML.taggedValueWithAttr(ATTR_UPPERRANGE, upperrange, 
														  "include", blnAsZXStr(objAttr.isLowerRangeInclude()));
						}

						dumpAsXML.taggedValue(ATTR_OUTPUTLENGTH, String.valueOf(objAttr.getOutputLength()));
						dumpAsXML.taggedValue(ATTR_OUTPUTMASK, objAttr.getOutputMask());
						dumpAsXML.taggedValue(ATTR_EDITMASK, objAttr.getEditMask());

						if (objAttr.getOptions() != null
							&& !objAttr.getOptions().isEmpty()) {
							dumpAsXML.openTag(ATTR_LIST);

							Iterator objOptions = objAttr.getOptions().values().iterator();
							while (objOptions.hasNext()) {
								Option objOption = (Option) objOptions.next();

								dumpAsXML.openTag("option");
								dumpAsXML.taggedValue("value", objOption.getValue());

								dumpAsXML.openTag("label");
								Iterator objOptLabels = objOption.getLabel().iterator();
								while (objOptLabels.hasNext()) {
									Label objLabel = (Label) objOptLabels.next();

									dumpAsXML.openTag(objLabel.getLanguage().toUpperCase());
									dumpAsXML.taggedCData("label", objLabel.getLabel());
									dumpAsXML.taggedCData("description", objLabel.getDescription());
									dumpAsXML.closeTag(objLabel.getLanguage().toUpperCase());
								}
								dumpAsXML.closeTag("label");

								dumpAsXML.closeTag("option");
							}

							dumpAsXML.closeTag(ATTR_LIST);
						}

						dumpAsXML.taggedValue(ATTR_COMBOBOX, objAttr.isCombobox());

						if (objAttr.getTags() != null
							&& !objAttr.getTags().isEmpty()) {
							dumpAsXML.openTag(ATTR_TAGS);
							Iterator objTags = objAttr.getTags().iterator();
							while (objTags.hasNext()) {
								Tuple objTag = (Tuple) objTags.next();
								dumpAsXML.openTag("tag");
								dumpAsXML.taggedValue("name", objTag.getName());
								dumpAsXML.taggedValue("value", objTag.getValue());
								dumpAsXML.closeTag("tag");
							}
							dumpAsXML.closeTag(ATTR_TAGS);
						}

						dumpAsXML.closeTag("attribute");
					}
				}
			}
			dumpAsXML.closeTag(ENTITY_ATTRIBUTES);

			dumpAsXML.openTag(ENTITY_ATTRIBUTEGROUPS);
			Iterator objAttributeGroups = pobjDesc.getAttributeGroups().iterator();
			while (objAttributeGroups.hasNext()) {
				AttributeGroup objAttributeGroup = (AttributeGroup) objAttributeGroups
						.next();

				/**
				 * DGS11AUG2003: Ignore any 'based on' groups
				 */
				if (!objAttributeGroup.isInherited()) {
					dumpAsXML.openTag("attributegroup");
					dumpAsXML.taggedValue("name", objAttributeGroup.getName(),true);

					String strComment = objAttributeGroup.getComment();
					dumpAsXML.taggedCData("comment", strComment);

					dumpAsXML.openTag("attributes");
					Iterator iterAttributeGroupAttributes = objAttributeGroup.getAttributes().iterator();
					while (iterAttributeGroupAttributes.hasNext()) {
						Attribute objAttributeGroupAttribute = (Attribute) iterAttributeGroupAttributes.next();
						/**
						 * BD13JUN04 Ignore zx stuff based on audit setting
						 */
						boolean blnInclude;
						String attrName = objAttributeGroupAttribute.getName();
						if (StringUtil.equalsAnyOf(new String[] { "zXCrtdBy",
								"zXCrtdWhn", "zXUpdtdBy", "zXUpdtdWhn" },
								attrName)) {
							blnInclude = pobjDesc.isAuditable();
						} else if (attrName.equals("zXUpdtdId")) {
							blnInclude = pobjDesc.isConcurrencyControl();
						} else {
							blnInclude = true;
						}
						if (blnInclude) {
							dumpAsXML.taggedValue("attribute",objAttributeGroupAttribute.getName());
						}
					}
					dumpAsXML.closeTag("attributes");

					dumpAsXML.closeTag("attributegroup");
				}
			}
			dumpAsXML.closeTag(ENTITY_ATTRIBUTEGROUPS);

			if (pobjDesc.getBORelations() != null) {
				int intBORelations = pobjDesc.getBORelations().size();
				if (intBORelations > 0) {
					BORelation objRelation;
					dumpAsXML.openTag(ENTITY_BORELATIONS);

					for (int i = 0; i < intBORelations; i++) {
						objRelation = (BORelation) pobjDesc.getBORelations().get(i);

						dumpAsXML.openTag("relation");
						dumpAsXML.taggedValue("entity", objRelation.getEntity(), true);
						dumpAsXML.taggedValue("fkattr", objRelation.getFKAttr());
						dumpAsXML.taggedValue("deleterule", objRelation.getDeleteRuleAsString());
						dumpAsXML.closeTag("relation");
					}

					dumpAsXML.closeTag(ENTITY_BORELATIONS);
				}
			}

			if (pobjDesc.getEventActions() != null
				&& !pobjDesc.getEventActions().isEmpty()) {
				dumpAsXML.openTag(ENTITY_EVENTACTIONS);

				/**
				 * Now we have to maintain the overall event strings
				 * This is used as a quick way to tell whether we have to
				 * go through all the trouble of evaluating the eventActions.
				 * You have to remember that prePersist and postPersist are called
				 * hundreds of times so every millisecond saved makes a difference
				 * 
				 * The string contains a 0 or 1; the location marks the persistAction
				 * 1 indicates relevant, 0 indicates not relevant
				 */
				String str = StringUtil.padString(zXType.persistAction.getHighestValue(), '0');
				int strLength = str.length();
				StringBuffer strPreEvents = new StringBuffer(strLength).append(str);
				StringBuffer strPostEvents = new StringBuffer(strLength).append(str);
				Iterator iterEventActions = pobjDesc.getEventActions().iterator();
				while (iterEventActions.hasNext()) {
					EventAction objEventAction = (EventAction) iterEventActions.next();
					for (int i = 0; i < strLength; i++) {
						if (objEventAction.getEvents().charAt(i) == '1') {
							// We have a pre or post persist action set.
							if (objEventAction.getTiming().equals(zXType.eaTiming.eatPre)) {
								strPreEvents.replace(i, i + 1, "1");
							} else {
								strPostEvents.replace(i, i + 1, "1");
							}
						}
					}

					dumpAsXML.openTag("action");

					dumpAsXML.taggedValue("name", objEventAction.getName());
					dumpAsXML.taggedValue("timing", objEventAction.getTimingAsString());
					dumpAsXML.taggedValue("wheregroup", objEventAction.getWhereGroup());
					dumpAsXML.taggedValue("focusattribute", objEventAction.getFocusAttribute());
					dumpAsXML.taggedValue("notnullgroup", objEventAction.getNotNullGroup());
					dumpAsXML.taggedValue("activegroup", objEventAction.getActiveGroup());
					dumpAsXML.taggedValue("ensureloaded", objEventAction.getEnsureLoaded());
					dumpAsXML.taggedValue("extendgroup", objEventAction.getExtendgroup());
					dumpAsXML.taggedValue("groupbehaviour", objEventAction.getGroupBehaviourAsString());
					dumpAsXML.taggedValue("activeid", objEventAction.getActiveId());
					dumpAsXML.taggedValue("continuation", objEventAction.getContinuationAsString());
					dumpAsXML.taggedValue("idbehaviour", objEventAction.getIDBehaviourAsString());
					dumpAsXML.taggedValue("bovalidation", objEventAction.getBOValidationAsString());
					dumpAsXML.taggedValue("events", objEventAction.getEvents());

					dumpAsXML.taggedCData("active", objEventAction.getActive());
					dumpAsXML.taggedCData("action", objEventAction.getAction());

					dumpAsXML.taggedValue("returncode", objEventAction.getReturnCodeAsString());

					if (objEventAction.getMsg() != null
						&& !objEventAction.getMsg().isEmpty()) {
						dumpAsXML.openTag("msg");
						Iterator iterMsg = objEventAction.getMsg().iterator();
						while (iterMsg.hasNext()) {
							Label objLabel = (Label) iterMsg.next();
							dumpAsXML.taggedValue(objLabel.getLanguage().toUpperCase(), objLabel.getLabel());
						}
						dumpAsXML.closeTag("msg");
					}

					if (objEventAction.getAttrValues() != null
						&& !objEventAction.getAttrValues().isEmpty()) {
						dumpAsXML.openTag("attrvalues");
						
						Iterator iterAttrValues = objEventAction.getAttrValues().iterator();
						while (iterAttrValues.hasNext()) {
							Tuple objTuple = (Tuple) iterAttrValues.next();
							dumpAsXML.openTag("attrvalue");

							dumpAsXML.taggedValue("name", objTuple.getName());
							dumpAsXML.taggedValue("value", objTuple.getValue());

							dumpAsXML.closeTag("attrvalue");
						}

						dumpAsXML.closeTag("attrvalues");
					}

					dumpAsXML.taggedCData("comment", objEventAction.getComment());

					dumpAsXML.closeTag("action");
				}

				pobjDesc.setPreEvents(strPreEvents.toString());
				pobjDesc.setPostEvents(strPostEvents.toString());

				/**
				 * 26MAY2004: Only write either string if is not all zeroes
				 */
				if (StringUtil.inStr(pobjDesc.getPreEvents(), '1')) {
					dumpAsXML.taggedValue("preevents", pobjDesc.getPreEvents());
				}
				if (StringUtil.inStr(pobjDesc.getPostEvents(), '1')) {
					dumpAsXML.taggedValue("postevents", pobjDesc.getPostEvents());
				}

				dumpAsXML.closeTag(ENTITY_EVENTACTIONS);
			}

			dumpAsXML.openTag(ENTITY_SECURITY);
			Iterator iterSecuritys;
			Tuple objSecurity;
			if (!pobjDesc.getSecurityInsert().isEmpty()) {
				dumpAsXML.openTag("insert");
				iterSecuritys = pobjDesc.getSecurityInsert().iterator();
				while (iterSecuritys.hasNext()) {
					objSecurity = (Tuple) iterSecuritys.next();
					dumpAsXML.taggedValue("group", objSecurity.getName());
				}
				dumpAsXML.closeTag("insert");
			}

			if (!pobjDesc.getSecuritySelect().isEmpty()) {
				dumpAsXML.openTag("select");
				iterSecuritys = pobjDesc.getSecuritySelect().iterator();
				while (iterSecuritys.hasNext()) {
					objSecurity = (Tuple) iterSecuritys.next();
					dumpAsXML.taggedValue("group", objSecurity.getName());
				}
				dumpAsXML.closeTag("select");
			}

			if (!pobjDesc.getSecurityUpdate().isEmpty()) {
				dumpAsXML.openTag("update");
				iterSecuritys = pobjDesc.getSecurityUpdate().iterator();
				while (iterSecuritys.hasNext()) {
					objSecurity = (Tuple) iterSecuritys.next();
					dumpAsXML.taggedValue("group", objSecurity.getName());
				}
				dumpAsXML.closeTag("update");
			}

			if (!pobjDesc.getSecurityDelete().isEmpty()) {
				dumpAsXML.openTag("delete");
				iterSecuritys = pobjDesc.getSecurityDelete().iterator();
				while (iterSecuritys.hasNext()) {
					objSecurity = (Tuple) iterSecuritys.next();
					dumpAsXML.taggedValue("group", objSecurity.getName());
				}
				dumpAsXML.closeTag("delete");
			}
			dumpAsXML.closeTag(ENTITY_SECURITY);

			dumpAsXML.closeTag("entity");

			return dumpAsXML.getXML();

		} catch (Exception e) {
			/**
			 * Ignore any gernerated errors
			 */
			return "";
		}
	}
    
	/**
	 * blnAsZXStr - Returns a string form of a boolean used by config files
	 * 
	 * @param bln
	 * @return String form of the boolean
	 */
	private static String blnAsZXStr(boolean bln) {
	    return bln ? "yes" : "no";
	}
}