package org.zxframework;

import java.io.CharArrayWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.zxframework.exception.ParsingException;
import org.zxframework.util.StringUtil;

/**
 * Handles the parsing of a business object entity definition.
 */
public class BODescriptorHandler extends DefaultHandler {
	
	// ------------------------ XML Constants

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
	
    private static final String EVENTACTIONS_TAG = "action";
    private static final String EVENTACTIONS_NAME = "name";
    private static final String EVENTACTIONS_TIMING = "timing";
    private static final String EVENTACTIONS_WHEREGROUP = "wheregroup";
    private static final String EVENTACTIONS_FOCUSATTRIBUTE = "focusattribute";
    private static final String EVENTACTIONS_NOTNULLGROUP = "notnullgroup";
    private static final String EVENTACTIONS_ACTIVEGROUP = "activegroup";
    private static final String EVENTACTIONS_ENSURELOADED = "ensureloaded";
    private static final String EVENTACTIONS_EXTENDGROUP = "extendgroup";
    private static final String EVENTACTIONS_GROUPBEHAVIOUR = "groupbehaviour";
    private static final String EVENTACTIONS_ACTIVEID = "activeid";
    private static final String EVENTACTIONS_CONTINUATION = "continuation";
    private static final String EVENTACTIONS_IDBEHAVIOUR = "idbehaviour";
    private static final String EVENTACTIONS_BOVALIDATION = "bovalidation";
    private static final String EVENTACTIONS_EVENTS = "events";
    private static final String EVENTACTIONS_ACTIVE = "active";
    private static final String EVENTACTIONS_ACTION = "action";
    private static final String EVENTACTIONS_RETURNCODE = "returncode";
    private static final String EVENTACTIONS_MSG = "msg";
    private static final String EVENTACTIONS_COMMENT = "comment";
    private static final String EVENTACTIONS_ATTRVALUES = "attrvalues";
    
	private XMLReader parser;
	private CharArrayWriter contents = new CharArrayWriter();
	
	private LabelHandler labelMapper = new LabelHandler();
	private TagHandler tagMapper = new TagHandler();
	private SecurityTypeHandler securityTypeMapper = new SecurityTypeHandler();
	private AttributeHandler attributeMapper = new AttributeHandler();
	private AttributeGroupHandler attributeGroupMapper = new AttributeGroupHandler();
	private BORelationHandler boRelationMapper = new BORelationHandler();
	private EventActionHandler eventActionMapper = new EventActionHandler();
	private OptionListHandler optionListMapper = new OptionListHandler();
	
	private Descriptor currentDescriptor;
	private String baseDir;
	
	/**
	 * @param parser Handle to current parser.
	 * @param pobjDescriptor A handle to the descriptor.
	 * @param pstrBaseDir The base director for the business object xml files.
	 */
	public BODescriptorHandler(XMLReader parser, Descriptor pobjDescriptor, String pstrBaseDir) {
		this.parser = parser;
		this.currentDescriptor = pobjDescriptor;
		this.baseDir = pstrBaseDir;
	}
	
	/**
	 * @see DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		contents.write( ch, start, length );
	}
	
	/**
	 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		contents.reset();
		
		if (localName.equals(ENTITY_ATTRIBUTES)) {
			AttributeCollection colAttributes = new AttributeCollection();
	        currentDescriptor.setImplicitAttributes(new AttributeCollection());
	        currentDescriptor.setAttributes(colAttributes);
	        
	        attributeMapper.collectAttributes(parser, this, currentDescriptor, colAttributes);
	        
		} else if (localName.equals(ENTITY_ATTRIBUTEGROUPS)) {
			AttributeGroupCollection colAttributeGroups = new AttributeGroupCollection();
	        currentDescriptor.setAttributeGroups(colAttributeGroups);
	        
	        attributeGroupMapper.collectAttributeGroups(parser, this, currentDescriptor, colAttributeGroups);
	        
		} else if (localName.equals(ENTITY_BORELATIONS)) {
			List colBORelations = new ArrayList();
	        currentDescriptor.setBORelations(colBORelations);
	        boRelationMapper.collectBORelations(parser, this, colBORelations);
	        
		} else if (localName.equals(ENTITY_EVENTACTIONS)) {
			List colEventActions = new ArrayList();
	        currentDescriptor.setEventActions(colEventActions);
	        
	        eventActionMapper.collectEventActions(parser, this, colEventActions);
			
		} else if (localName.equals(ENTITY_LABEL)) {
			LabelCollection colLabels = new LabelCollection();
	        currentDescriptor.setLabel(colLabels);
	        labelMapper.collectLabels(parser, this, currentDescriptor, colLabels);
	        
		} else if (localName.equals(ENTITY_SECURITY)) {
	        securityTypeMapper.collectSecurityTypes(parser, this, currentDescriptor);
	        
		} else if (localName.equals(ENTITY_TAGS)) {
			ZXCollection colAttrTags = new ZXCollection();
			currentDescriptor.setTags(colAttrTags);
	        tagMapper.collectTags(parser, this, colAttrTags);
	        
		}
		
	}
	
	/**
	 * @see DefaultHandler#endElement(String, String, String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (localName.equals(ENTITY_ALIAS)) {
			currentDescriptor.setAlias(contents.toString());
			
		// ENTITY_ATTRIBUTEGROUPS - handled in startElement
			
		// ENTITY_ATTRIBUTES  - handled in startElement
			
		} else if (localName.equals(ENTITY_AUDITABLE)) {
			currentDescriptor.setAuditable(StringUtil.booleanValue(contents.toString()));
			
		} else if (localName.equals(ENTITY_BASEDON)) {
			if (StringUtil.len(currentDescriptor.getBasedOn()) == 0) {
				/**
				 * NOTE: For this to work the basedon tag has to be the first tag
				 * in the entity xml.
				 */
				currentDescriptor.setBasedOn(contents.toString());
				currentDescriptor.setParsingBasedOn(true);
				
				try {
					/**
					 * Parse the based on xml completely seperately.
					 */
					XMLReader xr = XMLReaderFactory.createXMLReader();
					BODescriptorHandler objHandler = new BODescriptorHandler(xr, currentDescriptor, this.baseDir);
					xr.setContentHandler(objHandler);
					xr.parse(new InputSource(new FileReader(this.baseDir + currentDescriptor.getBasedOn())));
					
				} catch (Exception e) {
					throw new ParsingException(e);
				}
			}
			
		// ENTITY_BORELATIONS - handled in startElement
			
		} else if (localName.equals(ENTITY_CLASSNAME)) {
			currentDescriptor.setClassName(contents.toString());
			
		} else if (localName.equals(ENTITY_COMMENT)) {
			currentDescriptor.setComment(contents.toString());
			
		} else if (localName.equals(ENTITY_TESTDATAGROUP)) {
			currentDescriptor.setTestDataGroup(contents.toString());
			
		} else if (localName.equals(ENTITY_TESTDATAVALIDATION)) {
			currentDescriptor.setTestDataValidation(contents.toString());
			
		} else if (localName.equals(ENTITY_DATACONSTANTGROUP)) {
			currentDescriptor.setDataConstantGroup(contents.toString());
			
		} else if (localName.equals(ENTITY_DATASOURCE)) {
			currentDescriptor.setDataSource(contents.toString());
			
		} else if (localName.equals(ENTITY_DELETABLE)) {
			currentDescriptor.setDeletable(StringUtil.booleanValue(contents.toString()));
			
		} else if (localName.equals(ENTITY_DELETERULE)) {
			currentDescriptor.setDeleteRule(zXType.deleteRule.getEnum(contents.toString()));
			
		// ENTITY_EVENTACTIONS - handled in startElement
			
		} else if (localName.equals(ENTITY_HELPID)) {
			currentDescriptor.setHelpId(contents.toString());
			
		// ENTITY_LABEL - handled in startElement
			
		} else if (localName.equals(ENTITY_LASTCHANGE)) {
			currentDescriptor.setLastChange(contents.toString());
			
		} else if (localName.equals(ENTITY_NAME)) {
			currentDescriptor.setName(contents.toString());
			
		} else if (localName.equals(ENTITY_PRIMARYKEY)) {
			currentDescriptor.setPrimaryKey(contents.toString());
			
		// ENTITY_SECURITY - handled in startElement
		
		} else if (localName.equals(ENTITY_SEQUENCE)) {
			currentDescriptor.setSequence(contents.toString());
			
		} else if (localName.equals(ENTITY_SIZE)) {
			currentDescriptor.setSize(zXType.entitySize.getEnum(contents.toString()));
			
		} else if (localName.equals(ENTITY_TABLE)) {
			currentDescriptor.setTable(contents.toString());
			
		// ENTITY_TAGS - handled in startElement
			
		} else if (localName.equals(ENTITY_UNIQUECONSTRAINT)) {
			currentDescriptor.setUniqueConstraint(contents.toString());
			
		} else if (localName.equals(ENTITY_VERSION)) {
			currentDescriptor.setVersion(contents.toString());
			
		}
	}
	
	/**
	 * Handles the parsing of a label.
	 *
	 *<pre>
	 * Example xml :
	 *
	 *&lt;label>
	 *	&lt;EN>
	 *		&lt;label>User profile&lt;/label>
	 *		&lt;description>User profile&lt;/description>
	 *	&lt;/EN>
	 *&lt;/label>
	 *
	 * Where "EN" is the language code. Due to this structure the parsing is a little tricky.
	 *</pre>
	 */
	class LabelHandler extends DefaultHandler {
		
		//------------------------ Members 
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private Descriptor currentDescriptor;
		private LabelCollection currentLabels;
		private Label currentLabel;
		
		private int labelNo;
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pobjDescriptor A handle to the descriptor
		 * @param pcolLabels A collection of Labels
		 */
		public void collectLabels(XMLReader pobjParser,
								  ContentHandler pobjParent,
								  Descriptor pobjDescriptor,
								  LabelCollection pcolLabels) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentDescriptor = pobjDescriptor;
			this.currentLabels = pcolLabels;
			
			this.parser.setContentHandler(this);
			this.labelNo = 1;
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			if (localName.equals("label")) {
				this.labelNo++;
			}
			
			/**
			 * Start of a new label.
			 * 
			 * If it is not a label or description tag it must be the language tag.
			 */
			if (!localName.equals("label") && !localName.equals("description")) {
				currentLabel = new Label();
				currentLabel.setLanguage(localName.toUpperCase());
				
				/**
				 * If there is a 'based on' descriptor and we are not in the
				 * process of parsing that, we may already have included this label.
				 * If so, this one overrules that in the 'based on'.
				 */
				if (StringUtil.len(currentDescriptor.getBasedOn()) > 0 &&  !currentDescriptor.isParsingBasedOn()) {
					currentLabels.remove(currentLabel.getLanguage());
				}
				
				currentLabels.put(currentLabel.getLanguage(), currentLabel);
			}
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("label")) {
				this.labelNo++;
				
				/**
				 * If divisable by 2 this must be the last closing label tag.
				 */
				if (this.labelNo%2 == 0) {
					parser.setContentHandler(parent);
					
				} else {
					currentLabel.setLabel(contents.toString());
					
				}
				
			} else if (localName.equals("description")) {
				currentLabel.setDescription(contents.toString());
			}
		}
		
	} // LabelHandler
	
	/**
	 * Attribute Value Handler
	 */
	class AttrValueHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private List currentTuples;
		private Tuple currentTuple;
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pcolTuples A collection of tuples.
		 */
		public void collectAttrValues(XMLReader pobjParser,
									  ContentHandler pobjParent,
									  List pcolTuples) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentTuples = pcolTuples;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			/**
			 * Start of new tag.
			 */
			if (localName.equals("attrvalue")) {
				currentTuple = new Tuple();
				currentTuples.add(currentTuple);
			}
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("name")) {
				currentTuple.setName(contents.toString());
				
			} else if (localName.equals("value")) {
				currentTuple.setValue(contents.toString());
			}
			
			/**
			 * EOF Attribute Tags
			 */
			if (localName.equals("attrvalues")) {
				parser.setContentHandler(parent);
			}
		}
		
	} // AttrValueHandler
	
	/**
	 * Tag Handler
	 */
	class TagHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private Map currentTags;
		private Tuple currentTag;
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pcolTags A collection of tags
		 */
		public void collectTags(XMLReader pobjParser,
							    ContentHandler pobjParent,
							    Map pcolTags) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentTags = pcolTags;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			/**
			 * Start of new tag.
			 */
			if (localName.equals("tag")) {
				currentTag = new Tuple();
			}
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("name")) {
				currentTag.setName(contents.toString());
				currentTags.put(currentTag.getName(), currentTag);
				
			} else if (localName.equals("value")) {
				currentTag.setValue(contents.toString());
			}
			
			/**
			 * EOF Attribute Tags
			 */
			if (localName.equals("tags")) {
				parser.setContentHandler(parent);
			}
		}
		
	} // TagHandler
	
	/**
	 * Attribute Handler
	 */
	class AttributeHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private Descriptor currentDescriptor;
		private AttributeCollection currentAttributes;
		private Attribute currentAttribute;
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pobjDescriptor A handle to the descriptor
		 * @param pcolAttributes A collection of attributes
		 */
		public void collectAttributes(XMLReader pobjParser,
								 	  ContentHandler pobjParent,
								 	  Descriptor pobjDescriptor,
								 	  AttributeCollection pcolAttributes) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentDescriptor = pobjDescriptor;
			this.currentAttributes = pcolAttributes;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			/**
			 * Start of new Attribute
			 */
			if (localName.equals("attribute")) {
				currentAttribute = new Attribute();
				currentAttribute.setInherited(currentDescriptor.isParsingBasedOn());
				
			} else if (localName.equals(ATTR_LABEL)) {
				LabelCollection colLabels = new LabelCollection();
		        currentAttribute.setLabel(colLabels);
		        labelMapper.collectLabels(parser, this, currentDescriptor, colLabels);
		        
			} else if (localName.equals(ATTR_LIST)) {
				Map colOptionList = new HashMap();
		        currentAttribute.setOptions(colOptionList);
		        optionListMapper.collectOptionList(parser, this, currentDescriptor, colOptionList);
		        
			} else if (localName.equals(ATTR_LOWERRANGE)) {
				currentAttribute.setLowerRangeInclude(StringUtil.booleanValue(attributes.getValue("include")));
				
			} else if (localName.equals(ATTR_TAGS)) {
				ZXCollection colAttrTags = new ZXCollection();
		        currentAttribute.setTags(colAttrTags);
		        tagMapper.collectTags(parser, this, colAttrTags);
		        
			} else if (localName.equals(ATTR_UPPERRANGE)) {
				currentAttribute.setUpperRangeInclude(StringUtil.booleanValue(attributes.getValue("include")));
				
			}
			
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			
			if (localName.equals(ATTR_NAME)) {
				currentAttribute.setName(contents.toString());
				currentAttributes.put(currentAttribute.getName().toLowerCase(), currentAttribute);
				
			} else if (localName.equals(ATTR_CASE)) {
				currentAttribute.setTextCase(zXType.textCase.getEnum(contents.toString()));
				
			} else if (localName.equals(ATTR_COLUMN)) {
				currentAttribute.setColumn(contents.toString());
				
			} else if (localName.equals(ATTR_COMBOBOX)) {
				currentAttribute.setCombobox(StringUtil.booleanValue(contents.toString()));
				
			} else if (localName.equals(ATTR_COMMENT)) {
				currentAttribute.setComment(contents.toString());
				
			} else if (localName.equals(ATTR_TESTDATAVALUE)) {
				currentAttribute.setTestDataValue(contents.toString());
				
			} else if (localName.equals(ATTR_DATATYPE)) {
				currentAttribute.setDataType(zXType.dataType.getEnum(contents.toString()));
				
			} else if (localName.equals(ATTR_DEFAULTVALUE)) {
				currentAttribute.setDefaultValue(contents.toString());
				
			} else if (localName.equals(ATTR_DYNAMICVALUE)) {
				currentAttribute.setDynamicValue(contents.toString());
				
			} else if (localName.equals(ATTR_EDITMASK)) {
				currentAttribute.setEditMask(contents.toString());
				
			} else if (localName.equals(ATTR_ENSURELOADGROUP)) {
				currentAttribute.setEnsureLoadGroup(contents.toString());
				
			} else if (localName.equals(ATTR_EXPLICIT)) {
				currentAttribute.setExplicit(StringUtil.booleanValue(contents.toString()));
				
			} else if (localName.equals(ATTR_FKLABELEXPRESSION)) {
				currentAttribute.setFkLabelExpression(contents.toString());
				
			} else if (localName.equals(ATTR_FKLABELGROUP)) {
				currentAttribute.setFkLabelGroup(contents.toString());
				
			} else if (localName.equals(ATTR_FKRELATTR)) {
				currentAttribute.setFkRelAttr(contents.toString());
				
			} else if (localName.equals(ATTR_FOREIGNKEY)) {
				currentAttribute.setForeignKey(contents.toString());
				
			} else if (localName.equals(ATTR_FOREIGNKEYALIAS)) {
				currentAttribute.setForeignKeyAlias(contents.toString());
				
			} else if (localName.equals(ATTR_HELPID)) {
				currentAttribute.setHelpId(contents.toString());
			
			// ATTR_LABEL handled in startElement
			
			} else if (localName.equals(ATTR_LENGTH)) {
				currentAttribute.setLength(Integer.parseInt(contents.toString()));
				
			// ATTR_LIST handled in startElement
				
			} else if (localName.equals(ATTR_LOCK)) {
				currentAttribute.setLocked(StringUtil.booleanValue(contents.toString()));
				
			} else if (localName.equals(ATTR_LOWERRANGE)) {
				currentAttribute.setLowerRange(contents.toString());
				
			} else if (localName.equals(ATTR_MULTILINE)) {
				currentAttribute.setMultiLine(StringUtil.booleanValue(contents.toString()));
				
			} else if (localName.equals(ATTR_OPTIONAL)) {
				currentAttribute.setOptional(StringUtil.booleanValue(contents.toString()));
				
			} else if (localName.equals(ATTR_OUTPUTLENGTH)) {
				currentAttribute.setOutputLength(Integer.parseInt(contents.toString()));
				
			} else if (localName.equals(ATTR_OUTPUTMASK)) {
				currentAttribute.setOutputMask(contents.toString());
				
			} else if (localName.equals(ATTR_PASSWORD)) {
				currentAttribute.setPassword(StringUtil.booleanValue(contents.toString()));
				
			} else if (localName.equals(ATTR_PRECISION)) {
				currentAttribute.setPrecision(Integer.parseInt(contents.toString()));
				
			} else if (localName.equals(ATTR_REGULAREXPRESSION)) {
				currentAttribute.setRegularExpression(contents.toString());
				
			} else if (localName.equals(ATTR_SEARCHMETHOD)) {
				currentAttribute.setSearchMethod(zXType.searchMethod.getEnum(contents.toString()));
				
			} else if (localName.equals(ATTR_SORTATTR)) {
				currentAttribute.setSortAttr(contents.toString());
				
			// ATTR_TAG handled in startElement
				
			} else if (localName.equals(ATTR_UPPERRANGE)) {
				currentAttribute.setUpperRange(contents.toString());
				
			} else if (localName.equals(ATTR_VIRTUALCOLUMN)) {
				currentAttribute.setVirtualColumn(StringUtil.booleanValue(contents.toString()));
				
			}
			
			/**
			 * End of a attribute
			 */
			if (localName.equals("attribute")) {
				/**
				 * Add to group of implicit only attributes when required.
				 */
				if (!currentAttribute.isExplicit()) {
					currentDescriptor.getImplicitAttributes().put(currentAttribute.getName(), currentAttribute);
				}
			}
			
			/**
			 * End of all Attributes hand control back to the parent parser.
			 */
			if (localName.equals(ENTITY_ATTRIBUTES)) {
                /**
                 * Optionally add the audit columns
                 */
                if (currentDescriptor.isAuditable()) {
                    if (currentDescriptor.isParsingBasedOn() || currentDescriptor.getAuditAttributes() == null) {
                        /**
                         *... by only adding the audit attributes if we are parsing the 'based
                         * on' xml and it is auditable, or we are parsing the 'real' xml and it
                         * is auditable and the based on wasn't.
                         */
                        currentDescriptor.addAuditAttributes();
                    }
                    
                }
                
				parser.setContentHandler(parent);
			}
		}
		
	} // AttributeHandler
	
	/**
	 * Handles the parsing of the attribute group.
	 */
	class AttributeGroupHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private Descriptor currentDescriptor;
		private AttributeGroupCollection currentAttributeGroups;
		private AttributeGroup currentAttributeGroup;
		
		private AttributeGroupAttrsHandler attrGroupMapping = new AttributeGroupAttrsHandler();
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pobjDescriptor A handle to the descriptor
		 * @param pcolAttributeGroups A collections of attributeGroups.
		 */
		public void collectAttributeGroups(XMLReader pobjParser,
										   ContentHandler pobjParent,
										   Descriptor pobjDescriptor,
										   AttributeGroupCollection pcolAttributeGroups) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentDescriptor = pobjDescriptor;
			this.currentAttributeGroups = pcolAttributeGroups;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			/**
			 * Start of new Attribute Group
			 */
			if (localName.equals("attributegroup")) {
				currentAttributeGroup = new AttributeGroup();
				/**
				 * This sax parser does not support just in time parsing.
				 */
				currentAttributeGroup.setParsed(true);
				
				currentAttributeGroup.setInherited(currentDescriptor.isParsingBasedOn());
				
			} else if (localName.equals("attributes")) {
				AttributeCollection colAttributes = new AttributeCollection();
		        currentAttributeGroup.setAttributes(colAttributes);
		        attrGroupMapping.collectAttributes(parser, this, currentDescriptor, colAttributes);
		        
			}
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals(ATTR_NAME)) {
				currentAttributeGroup.setName(contents.toString());
				currentAttributeGroups.put(currentAttributeGroup.getName().toLowerCase(), currentAttributeGroup);
				return;
			}
			
			/**
			 * EOF AttributeGroups
			 */
			if (localName.equals(ENTITY_ATTRIBUTEGROUPS)) {
				parser.setContentHandler(parent);
			}
		}
		
	} // AttributeGroupHandler
	
	/**
	 * Handles the parsing of the list of attributes associated with a Attribute Group.
	 */
	class AttributeGroupAttrsHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private Descriptor currentDescriptor;
		private AttributeCollection currentAttributes;
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pobjDescriptor A handle to the descriptor
		 * @param pcolAttributes A collection of attribute values.
		 */
		public void collectAttributes(XMLReader pobjParser,
									  ContentHandler pobjParent,
									  Descriptor pobjDescriptor,
									  AttributeCollection pcolAttributes) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentDescriptor = pobjDescriptor;
			this.currentAttributes = pcolAttributes;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("attribute")) {
				Attribute objAttr = currentDescriptor.getAttributes().get(contents.toString().toLowerCase());
				if (objAttr != null) {
					currentAttributes.put(objAttr.getName(), objAttr);
				}
			}
			
			/**
			 * EOF AttributeGroups
			 */
			if (localName.equals("attributes")) {
				parser.setContentHandler(parent);
			}
		}
		
	} // AttrGroupHandler
	
	class BORelationHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private List currentBORelations;
		private BORelation currentBORelation;
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pcolBORelations A collection of Business object relations.
		 */
		public void collectBORelations(XMLReader pobjParser,
									    ContentHandler pobjParent,
									    List pcolBORelations) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentBORelations = pcolBORelations;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			/**
			 * Start of new BORelation
			 */
			if (localName.equals("relation")) {
				currentBORelation = new BORelation();
				currentBORelations.add(currentBORelation);
				return;
			}
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("entity")) {
				currentBORelation.setEntity(contents.toString());
				
			} else if (localName.equals("fkattr")) {
				currentBORelation.setFKAttr(contents.toString());
				
			} else if (localName.equals("deleterule")) {
				currentBORelation.setDeleteRule(zXType.deleteRule.getEnum(contents.toString()));
				
			}
			
			/**
			 * EOF BORelations
			 */
			if (localName.equals(ENTITY_BORELATIONS)) {
				parser.setContentHandler(parent);
				return;
			}
		}
		
	} // BORelationHandler -- 100% done
	
	/**
	 * Handles the parsing of a EventAction message.
	 * 
	 * <pre>
	 * Example xml :
	 * 
	 * &lt;msg>
	 * 	&lt;EN>Message&lt;/EN>
	 * &lt;/msg>
	 * </pre>
	 */
	class MSGHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private LabelCollection currentMSGs;
		private Label currentMSG;
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pcolMSGs A label collection.
		 */
		public void collectMSGs(XMLReader pobjParser,
								ContentHandler pobjParent,
								LabelCollection pcolMSGs) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentMSGs = pcolMSGs;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}
		
		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			/**
			 * Start of new Message
			 */
			if (!localName.equals("msg")) {
				currentMSG = new Label();
				currentMSG.setLanguage(localName.toUpperCase());
				
				currentMSGs.put(currentMSG.getLanguage(), currentMSG);
			}
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("msg")) {
				parser.setContentHandler(parent);
			} else {
				currentMSG.setLabel(contents.toString());
			}
		}
		
	} // MSGHandler
	
	/**
	 * Event Action Handler
	 */
	class EventActionHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private List currentEventActions;
		private EventAction currentEventAction;
		
		private MSGHandler msgMapping = new MSGHandler();
		private AttrValueHandler attrValueMapping = new AttrValueHandler();
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pcolEventActions A collection of EventActions.
		 */
		public void collectEventActions(XMLReader pobjParser,
								 	    ContentHandler pobjParent,
								 	    List pcolEventActions) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentEventActions = pcolEventActions;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			/**
			 * Start of new event action.
			 */
			if (localName.equals(EVENTACTIONS_ATTRVALUES)) {
				List colAttrValues = new ArrayList();
				currentEventAction.setAttrValues(colAttrValues);
				attrValueMapping.collectAttrValues(parser, this, colAttrValues);
		        
			} else if (localName.equals(EVENTACTIONS_MSG)) {
				LabelCollection colMSGs = new LabelCollection();
		        currentEventAction.setMsg(colMSGs);
		        msgMapping.collectMSGs(parser, this, colMSGs);
		        
			} else if (localName.equals(EVENTACTIONS_TAG)) {
				currentEventAction = new EventAction();
				currentEventActions.add(currentEventAction);
				
			}
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			
			if (localName.equals(EVENTACTIONS_ACTION)) {
				currentEventAction.setAction(contents.toString());
				
			} else if (localName.equals(EVENTACTIONS_ACTIVE)) {
				currentEventAction.setActive(contents.toString());
				
			} else if (localName.equals(EVENTACTIONS_ACTIVEGROUP)) {
				currentEventAction.setActiveGroup(contents.toString());
				
			} else if (localName.equals(EVENTACTIONS_ACTIVEID)) {
				currentEventAction.setActiveId(contents.toString());
				
			//  attrvalues - Handled in startElement	
				
			} else if (localName.equals(EVENTACTIONS_BOVALIDATION)) {
				currentEventAction.setBOValidation(StringUtil.booleanValue(contents.toString()));
				
			} else if (localName.equals(EVENTACTIONS_COMMENT)) {
				currentEventAction.setComment(contents.toString());
				
			} else if (localName.equals(EVENTACTIONS_CONTINUATION)) {
				currentEventAction.setContinuation(zXType.eaContinuation.getEnum(contents.toString()));
				
			} else if (localName.equals(EVENTACTIONS_ENSURELOADED)) {
				currentEventAction.setEnsureLoaded(contents.toString());
				
			} else if (localName.equals(EVENTACTIONS_EXTENDGROUP)) {
				currentEventAction.setExtendgroup(contents.toString());
				
			} else if (localName.equals(EVENTACTIONS_EVENTS)) {
				currentEventAction.setEvents(contents.toString());
				
			} else if (localName.equals(EVENTACTIONS_FOCUSATTRIBUTE)) {
				currentEventAction.setFocusAttribute(contents.toString());
				
			} else if (localName.equals(EVENTACTIONS_GROUPBEHAVIOUR)) {
				currentEventAction.setGroupBehaviour(zXType.eaGroupBehaviour.getEnum(contents.toString()));
				
			} else if (localName.equals(EVENTACTIONS_IDBEHAVIOUR)) {
				currentEventAction.setIDBehaviour(zXType.eaIDBehaviour.getEnum(contents.toString()));
				
			// msgs - Handled in startElement
				
			} else if (localName.equals(EVENTACTIONS_NAME)) {
				currentEventAction.setName(contents.toString());
				
			} else if (localName.equals(EVENTACTIONS_NOTNULLGROUP)) {
				currentEventAction.setNotNullGroup(contents.toString());
				
			} else if (localName.equals(EVENTACTIONS_RETURNCODE)) {
				currentEventAction.setReturnCode(zXType.rc.getEnum(contents.toString()));
			
			} else if (localName.equals(EVENTACTIONS_TIMING)) {
				currentEventAction.setTiming(zXType.eaTiming.getEnum(contents.toString()));
				
			} else if (localName.equals(EVENTACTIONS_WHEREGROUP)) {
				currentEventAction.setWhereGroup(contents.toString());
				
			}
			
			/**
			 * EOF Event Actions
			 */
			if (localName.equals(ENTITY_EVENTACTIONS)) {
				parser.setContentHandler(parent);
			}
		}
		
	} // EventActionHandler
	
	/**
	 * Handlers the parsing of the attribute option list.
	 */
	class OptionListHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private Descriptor currentDescriptor;
		private Map currentOptionList;
		private Option currentOption;
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pobjDescriptor A handle to the descriptor
		 * @param pcolOptionList A collections of options
		 */
		public void collectOptionList(XMLReader pobjParser,
								 	  ContentHandler pobjParent,
								 	  Descriptor pobjDescriptor,
								 	  Map pcolOptionList) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentDescriptor = pobjDescriptor;
			this.currentOptionList = pcolOptionList;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}
		
		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			if (localName.equals("option")) {
				currentOption = new Option();
				
			} else if (localName.equals("label")) {
				LabelCollection colLabels = new LabelCollection();
				currentOption.setLabel(colLabels);
		        labelMapper.collectLabels(parser, this, currentDescriptor, colLabels);
		        
			}
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("value")) {
				currentOption.setValue(contents.toString());
				currentOptionList.put(currentOption.getValue(), currentOption);
				return;
			}
			
			/**
			 * EOF Option List
			 */
			if (localName.equals(ATTR_LIST)) {
				parser.setContentHandler(parent);
				return;
			}
		}
		
	} // OptionListHandler - 100% done
	
	/**
	 * Handles the parsing of Entity Security.
	 * 
	 * <pre>
	 * Example xml :
	 * 
	 *&lt;security>
	 *	&lt;insert>
	 *		&lt;group>ADMIN&lt;/group>
	 *		&lt;group>SU&lt;/group>
	 *	&lt;/insert>
	 *	&lt;select>
	 *		&lt;group>ANY&lt;/group>
	 *	&lt;/select>
	 *	&lt;update>
	 *		&lt;group>ANY&lt;/group>
	 *	&lt;/update>
	 *	&lt;delete>
	 *		&lt;group>ADMIN&lt;/group>
	 *	&lt;/delete>
	 *&lt;/security>
	 *
	 * </pre>
	 */
	class SecurityTypeHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private Descriptor currentDescriptor;
		
		private SecurityHandler securityHandler = new SecurityHandler();
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pobjDescriptor A handle to the descriptor
		 */
		public void collectSecurityTypes(XMLReader pobjParser,
								 	     ContentHandler pobjParent,
								 	     Descriptor pobjDescriptor) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.currentDescriptor = pobjDescriptor;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}
		
		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			/**
			 * Start of new security.
			 */
			if (localName.equals("insert")) {
				ZXCollection colSecurities = new ZXCollection();
				currentDescriptor.setSecurityInsert(colSecurities);
				securityHandler.collectSecuritys(parser, this, colSecurities, "insert");
				
			} else if (localName.equals("select")) {
				ZXCollection colSecurities = new ZXCollection();
				currentDescriptor.setSecuritySelect(colSecurities);
				securityHandler.collectSecuritys(parser, this, colSecurities, "select");
				
			} else if (localName.equals("delete")) {
				ZXCollection colSecurities = new ZXCollection();
				currentDescriptor.setSecurityDelete(colSecurities);
				securityHandler.collectSecuritys(parser, this, colSecurities, "delete");
				
			}
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			/**
			 * EOF All Security Type Tags
			 */
			if (localName.equals(ENTITY_SECURITY)) {
				parser.setContentHandler(parent);
			}
		}
		
	} // SecurityTypeHandler
	
	/**
	 * Handles the parsing of a Security Type like "insert" or "update".
	 */
	class SecurityHandler extends DefaultHandler {
		
		//------------------------ Members
		
		private ContentHandler parent;
		private XMLReader parser;
		private CharArrayWriter contents = new CharArrayWriter();
		
		private String securityType;
		private ZXCollection currentSecurities;
		private Tuple currentSecurity;
		
		/**
		 * @param pobjParser A handle to the current parser.
		 * @param pobjParent The parser handler
		 * @param pcolSecurities A collection of security settings.
		 * @param pstrSecurityType The name of the security type.
		 */
		public void collectSecuritys(XMLReader pobjParser,
								 	  ContentHandler pobjParent,
								 	 ZXCollection pcolSecurities,
								 	  String pstrSecurityType) {
			this.parent = pobjParent;
			this.parser = pobjParser;
			this.securityType = pstrSecurityType;
			this.currentSecurities = pcolSecurities;
			
			this.parser.setContentHandler(this);
		}
		
		/**
		 * @see DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write( ch, start, length );
		}

		/**
		 * @see DefaultHandler#startElement(String, String, String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			contents.reset();
			
			/**
			 * Start of new security.
			 */
			if (localName.equals("group")) {
				currentSecurity = new Tuple();
			}
		}
		
		/**
		 * @see DefaultHandler#endElement(String, String, String)
		 */
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (localName.equals("group")) {
				currentSecurity.setName(contents.toString());
				currentSecurity.setValue(contents.toString());
				
				currentSecurities.put(currentSecurity.getName(), currentSecurity);
			}
			
			/**
			 * EOF Attribute Tags
			 */
			if (localName.equals(this.securityType)) {
				parser.setContentHandler(parent);
			}
		}
		
	} // SecurityHandler
}