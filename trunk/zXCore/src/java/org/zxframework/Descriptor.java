/*
 * Created on Jan 14, 2004 by michael
 * $Id: Descriptor.java,v 1.1.2.31 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.zxframework.exception.ParsingException;
import org.zxframework.util.StringUtil;

/**
 * Descriptor - This object parses the enitity xml file and creates a object
 * representation of it.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 28 August 2002
 * 
 * NOTES : There are some key differences between the java and vb version of the entity xml format :
 * 
 * 1) The label format is standardised :
 * 	Only the following format works now :
 * 	&lt;label>
 * 		&lt;EN>
 * 			&lt;label>Document&lt;/label> 
 * 			&lt;description>Document&lt;/description>
 * 		&lt;/EN> 
 * 	&lt;/label>
 * 
 *	2) Class name are different and there is a possible of a different way of handling the classloading than from 
 *     the way it is currently done :
 * 
 *	VB version : 
 *	&lt;classname>zX.clsBO&lt;/classname>
 * 
 *  Java version : 
 *	&lt;jclassname>org.zxframework.ZXBO&lt;/jclassname>
 * 
 * 3) Datatype and all of the enum values now lose the "z" or "zx" at the beginning  and all the enum values are
 * 	being treated as lower case :
 * eg :
 * 	&lt;datatype>zString&lt;/datatype> 
 * 	&lt;password>zNo&lt;/password> 
 * 	&lt;multiline>zNo&lt;/multiline>
 * becomes :
 * 	&lt;datatype>string&lt;/datatype>
 * 	&lt;password>no&lt;/password>
 * 	&lt;multiline>no&lt;/multiline>
 * 
 * VB Changelog :
 * 
 * Change    : BD7DEC02
 * What      : Support for deleterule instead of deleteable (the latter is still
 *             around for backward compatibility)
 * 
 * Change    : DGS18FEB2003
 * What      : New functions groupMinusAsString and groupPlusAsString. Call the existing
 *             groupPlus/groupMinus but then unpack the returned collection into a string
 *             of comma separated attributes that can be used as an attr group string.
 * 
 * Change    : DGS24FEB2003
 * What      : Added combobox property to attribute.
 * 
 * Change    : BD15APR03
 * What      : Better identation of generated XML file
 * 
 * Change    : 21MAY03
 * What      : Added the following standard groups: null / empty and print
 * 
 * Change    : DGS13JUN2003
 * What      : Added comments, last change and version.
 * 
 * Change    : DGS11AUG2003
 * Why       : Added 'basedOn' property.
 * 
 * Change    : DGS13OCT2003
 * Why       : Added attribute tags.
 * 
 * Change    : BD29OCT03
 * Why       : Added groupHasAttr method
 * 
 * Change    : DGS04FEB2004
 * Why       : Added properties for Data Constant attribute group.
 *             Also fixed ommission from 'based on' change earlier
 * 
 * Change    : BD22FEB04
 * Why       : Fixed bug in getSingleGroup: it is allowed (although a bit strange) to
 *             try to remove an entry from an adhoc group that does not exist
 * 
 * Change    : BD25FEB04
 * Why       : Added support for fkPopup group
 * 
 * Change    : BD10MAR04
 * Why       : Added support routines for tags
 * 
 * Change    : DGS10MAR2004
 * Why       : In getSingleGroup, if we are looking for QSSearch and can't find it, use
 *             the Search group instead. This allows the standard quick search pageflow
 *             to use a different group from the normal Search group, but still works if
 *             the QSSearch group isn't defined for a particular BO.
 * 
 * Change    : BD20MAR04
 * Why       : Added support for isExplicit attribute flag and thus
 * 			   implicitAttributes and the @ group
 * 
 * Change    : BD26MAR04
 * Why       : Added support for BORelations for more advanced
 *             delete rules
 * 
 * Change    : BD24APR04
 * Why       : XML-escape labels and descriptions (so we can have <'s and >'s)
 * 
 * Change    : BD15MAY04
 * Why       : Added BO pre- and post-persist actions (referred to as eventActions)
 *            (minor tweak to this on 25MAY)
 * 
 * Change    : BD9JUN04
 * Why       : Changed 'auditble' into auditble and conurrencyControl
 * 
 * Change    : BD13JUN04
 * Why       : - Added action to event action
 *             - Added dynamicValue to atribute
 * 
 * Change    : BD14JUN04
 * Why       : Do NOT store BO relation in collection using the entity as a key because it
 *             may be that multiple relations with the same entity are required
 * 
 * Change    : BD19JUN04
 * Why       : Added support for QSOrder group
 * 
 * Change    : DGS29JUL2004
 * Why       : Very minor tweak to the above QSOrder change
 * 
 * Change    : BD31JUL04
 * Why       : Now write dynamic attribute value as CDATA
 * 
 * Change    : BD16SEP04
 * Why       : Very minor changes as result of attempting DOM document caching
 *             this makes sense for pageflow files (that are larger in size)
 *             but makes things actually slower for the smaller BO files
 * 
 * Change    : MB18SEP04
 * Why       : Added java classname
 * 
 * Change    : BD11NOV04
 * Why       : Added standard attribute groups description.load and label.load
 * 
 * Change    : DGS12JAN2005
 * Why       : There can be a problem with getGroup when subsequently changing the returned
 *             collection. The solution could be to change getGroup extensively, but as
 *             is widely used and needs good performance, the answer is to create a new
 *             function 'getGroupSafe', to be used when you know you are going to change
 *             the returned collection. Also made a minor fix to getGroup caching.
 *
 * Change    : BD25MAR05 - V1.5:1
 * Why       : - Added support for data sources
 * 
 * Change    : C1.4:73 DGS04MAY2005
 * Why       : When parsing, for forward compatibility, don't raise an error if an unknown tag
 *             is found. Do trace it if tracing is on.
 *             
 * Change    : C1.5:10 DGS04MAY2005
 * Why       : In dumpAsXML, don't write out the datasource tag if is 'primary'
 * 
 * Change    : C1.4:75 BD6MAY05
 * Why       : Added groupIntersect methods; they are now being used in zX.clsBOS.updateBO
 *             to address a possible issue that can occur with uniqueConstraints
 * 
 * Change    : C1.5:18 BD17JUN05
 * Why       : Added support for attribute ensureLoadGroup
 * 
 * Change    : BD30JUN05 - V1.5:20
 * Why       : Added support for enhanced FK label behaviour
 * 
 * Change    : BD01JUL05 - V1.5:25
 * Why       : Added support for sort attribute for dynamic attributes
 * 
 * Change    : BD01JUL05 - V1.5:29
 * Why       : Added support for ensureLoaded group for event actions
 * 
 * Change    : BD13FEB06 - V1.5:91
 * Why       : Added support for test data
 * 
 * Change    : BD28APR2006 (BD26MAR06) - V1.5:95
 * Why       : Added extendGroup support for eventAction. Also new attr group prefix of \- for use in order by
 *  
 *</pre>
 *
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class Descriptor extends ZXObject {

    //------------------------ Members
    
    private String version;
    private String lastChange;
    private String comment;

    private zXType.deleteRule deleteRule;
    private boolean justInTime;

    private ZXCollection securityInsert;
    private ZXCollection securityDelete;
    private ZXCollection securityUpdate;
    private ZXCollection securitySelect;

    private AttributeCollection auditAttributes;

    private String alias;

    private zXType.entitySize size;

    private AttributeGroupCollection attributeGroups;
    
    /** A collection (Attribute) of all of the attributes of a BO. **/
    private AttributeCollection attributes;
    
    private ZXCollection description;
    private ZXCollection tags;
    private LabelCollection label;

    private String name;
    private String basedOn;
    private boolean parsingBasedOn;
    private String helpId;
    private String table;
    private String primaryKey;
    private String className = "org.zxframework.ZXBO";
    private String vbclassName;
    private String uniqueConstraint;
    private boolean auditable;
    private boolean deletable;
    private String sequence;
    /** DGS04FEB2004: Added Data Constant attribute group */
    private String dataConstantGroup;
    private AttributeCollection implicitAttributes;
    private List BORelations;
    
    /**
     * This are used the cache previous values for getGroup.
     */
    private transient String lastGroupDef;
    private transient AttributeCollection lastGroup;
    private transient String lastGroupDef2;
    private transient AttributeCollection lastGroup2;
    private transient int lastCacheSlot;
    
    private long lastModified;
    
    /**
     * BD15MAY04 Added event actions
     */
    private List eventActions;
    private String preEvents;
    private String postEvents;
    
    /**
     * BD9JUN04 - Added
     */
    private boolean concurrencyControl;
    
    /**
     * BD25MAR05 - Added
     */
    private String dataSource;
    
    /**
     * BD13FEB06 - Added
     */
    private String testDataGroup;
    private String testDataValidation;
    
    //------------------------ Constructors  
    
    /**
     * Default constructor.
     */
    public Descriptor() {
        super();
        
        // Init values to prevent null pointers.
        this.attributeGroups = new AttributeGroupCollection();
        this.tags = new ZXCollection();
        this.BORelations = new ArrayList();
    }
    
    /**
     * Initialise descriptor (ie read / parse XML file).
     * 
     * <pre>
     * 
     * V1.5:91 - BD13FEB06 - Added test data support
     * </pre>
     * 
     * @param pstrXMLFile Full path name of XML file
     * @param pblnJustinTime Flag to specify whether just-in-time parsing is required yes / no
     * @throws ParsingException Thrown if init fails
     */
    public void init(String pstrXMLFile, boolean pblnJustinTime) throws ParsingException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrXMLFile", pstrXMLFile);
            getZx().trace.traceParam("pblnJustinTime", pblnJustinTime);
        }
        
        // Save the just in time parsing flag
        this.justInTime = pblnJustinTime;

        try {
            /**
             * If the XML file is empty: this descriptor is only used to get
             * access to some methods that would be have been defined as static
             * if VB supported that concept (VB.net?)
             */
            if (StringUtil.len(pstrXMLFile) == 0) { 
                // Exit method with error? maybe throw an exception.
                throw new ParsingException("Could not get the filename for Descriptor"); 
            }
            
            File file = new File(pstrXMLFile);
            // Used by the caching implementation
            this.lastModified = file.lastModified();
            
            if (true) {
            	/**
            	 * Using dom;
            	 * Might be replaces with SAX.
            	 */
	            DescriptorParser objDescParser = new DescriptorParserDomImpl();
	            
	            /**
	             * Give attribuite collection object a handle to me, as it may
	             * require some of my methods for the just-in-time parsing
	             */
	            getAttributeGroups().setDescriptor(objDescParser);
	            
	            try {
	                objDescParser.parse(new FileInputStream(file), this);
	            } catch (FileNotFoundException e) {
	            	throw new ParsingException("File not found : " + pstrXMLFile, e);
	            }
	            
            } else {
            	/**
            	 * Using sax;
            	 */
            	DescriptorParserSaXImpl.parse(this, getZx().getBoDir() + File.separator, file);
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Initialise descriptor", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrXMLFile = " + pstrXMLFile);
                getZx().log.error("Parameter : pblnJustinTime = " + pblnJustinTime);
            }
            
            if (getZx().throwException) { throw new ParsingException(e); }
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Runtime Getters/Setters
    
    protected long getLastModified() {
        return lastModified;
    }
    
    //------------------------ Getters and Setters    
    
    /**
     * The alias for the business object.
     * 
     * @return Returns the alias.
     */
    public String getAlias() {
        if (this.alias == null) {
            this.alias = ""; // Just to be on the safe side.
        }
        return this.alias;
    }

    /**
     * @param alias The alias to set.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * The datasource name for this business object.
     * 
     * <p>
     * <b>NOTE :</b> When blank it defaults to the primary datasource.
     * </p>
     * 
     * @return Returns the dataSource.
     */
    public String getDataSource() {
        return dataSource;
    }
    
    /**
     * @param dataSource The dataSource to set.
     */
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Defined attribute groups.
     * 
     * <p>
     * The collection of attribute groups specifies the attribute 
     * groups that are defined for this business object; more on 
     * attributes in the chapter on ‘Properties, attributes and 
     * attribute groups’.
     * </p>
     * 
     * <p>
     * <b>NOTE:</b> There are some standard attributes that you do
     * not need to explicitly define. Like &amp;,!,*,All and +.
     * </p>
     * 
     * @return Returns a collection (AttributeGroup) of attributeGroups.
     */
    public AttributeGroupCollection getAttributeGroups() {
        return this.attributeGroups;
    }

    /**
     * @param attributeGroups The attributeGroups to set.
     */
    public void setAttributeGroups(AttributeGroupCollection attributeGroups) {
        this.attributeGroups = attributeGroups;
    }

    /**
     * A collection of attributes belonging to the business object.
     * 
     * @return Returns a collection (Attribute) of all of the attributes of a BO.
     */
    public AttributeCollection getAttributes() {
        return this.attributes;
    }

    /**
     * @param attributes The attributes to set.
     */
    public void setAttributes(AttributeCollection attributes) {
        this.attributes = attributes;
    }

    /**
     * Whether the business object is auditable or not.
     * 
     * <p>
     * The auditable value indicates that the business object 
     * will support the ‘audit’ attributes: <i>zXUPdtdId, zXCrtdWhn, 
     * zXCrtdBy, zXUpdtdWhn, zXUpdtdBy</i>.<br/>
     * The audit columns will be maintained by zX and are used 
     * for the following features:
     * </p>
     * <ul><li>
     * Keep track of whom created the instance of the business 
     * object and when (values ‘auditable’ and ‘concurrency control’)
     * </li><li>
     * Keep track of whom last updated the instance of the 
     * business object and when (values ‘auditable’ and ‘concurrency 
     * control’)
     * </li><li>
     * Concurrency control in Web environment (‘concurrency 
     * control’ only)
     * </li></ul>
     * 
     * See the chapter on ‘Basic BO auditing and concurrency control’ 
     * for more information.
     * 
     * @return Returns the auditable.
     */
    public boolean isAuditable() {
        return this.auditable;
    }

    /**
     * @see #isAuditable()
     * @return Returns a boolean form of the Auditable.
     */
    public String getAuditableAsString() {
        /**
         * BD9JUN04 - Used to be a boolean; now an option list that
         * actually controls two variables
         */
        if (this.auditable && this.concurrencyControl) {
            return "concurrencycontrol";
        } else if (this.auditable && !this.concurrencyControl) {
            return "auditable";
        } else {
            return "none";
        }
        
    }
    
    /**
     * @param pstr The string form for the setting for auditable.
     */
    public void setAuditableAsString(String pstr) {
        /**
         * BD9JUN04 - Used to be a boolean; now an option list that
         * actually controls two variables
         */
        if (pstr.endsWith("yes") || pstr.equalsIgnoreCase("concurrencycontrol")) {
            auditable = true;
            concurrencyControl = true;
            
        } else if (pstr.equalsIgnoreCase("auditable")) {
            /**
             * Auditing only
             */
            auditable = true;
            concurrencyControl = false;
        	
        } else if ( !StringUtil.booleanValue(pstr) ) {
            auditable = false;
            concurrencyControl = false;
        } else {
            /**
             * Auditing only
             */
            auditable = true;
            concurrencyControl = false;
        }
    }
    
    /**
     * @param auditable The auditable to set.
     */
    public void setAuditable(boolean auditable) {
        this.auditable = auditable;
    }

    /**
     * The collection of audit attributes.
     * 
     * @return Returns the auditAttributes.
     */
    public AttributeCollection getAuditAttributes() {
        if (this.auditAttributes == null) {
            this.auditAttributes = new AttributeCollection();
        }
        return this.auditAttributes;
    }

    /**
     * @param auditAttributes The auditAttributes to set.
     */
    public void setAuditAttributes(AttributeCollection auditAttributes) {
        this.auditAttributes = auditAttributes;
    }

    /**
     * Name of entity that this entity is based on.
     * 
     * <p>
     * The ‘based on’ feature can be used to maintain different 
     * business objects that share a great number of attributes 
     * and groups.<br/>
     * An example has been presented earlier where there is a 
     * single ‘role’ table used for both client- and supplier- 
     * roles. The role-type is specified by the ‘type’ attribute.
     * </p>
     * <p>
     * For this example a generic BO is created that is related 
     * to the role table. Next, two BO’s are created that are 
     * based-on the genric role BO; one for the client role and 
     * one for the supplier role. These will have some values 
     * overridden:
     * </p>
     * <ul>
     * <li>
     * <i>Label</i>. One will say ‘Client role’, the other 
     * ‘Supplier role’
     * </li><li>
     * <i>Type</i>. One will have the type for a client role 
     * (e.g. ‘C’) and one for a supplier role (e.g. ‘S’)
     * </li></ul>
     * 
     * Any changes that are made to the original BO (other 
     * than to the label and the type attribute) will automatically 
     * be inherited by the ‘based-on’ business objects.
     * 
     * @return Returns the basedOn.
     */
    public String getBasedOn() {
        return this.basedOn;
    }

    /**
     * @param basedOn The basedOn to set.
     */
    public void setBasedOn(String basedOn) {
        this.basedOn = basedOn;
    }

    /**
     * The class name of the business object implementation.
     * 
     * The class name identifies the Java class that 
     * is used to implement the business object. For ‘no-thrills’ 
     * (i.e. when no special properties or methods are required) 
     * business objects this will be ZXBO.
     * 
     * @return Returns the className.
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * @see #getClassName()
     * @param className The className to set.
     */
    public void setClassName(String className) {
        this.className = className;
    }
    
    /**
     * @return Returns the className.
     */
    public String getVBClassName() {
        return this.vbclassName;
    }

    /**
     * @param className The className to set.
     */
    public void setVBClassName(String className) {
        this.vbclassName = className;
    }

    /**
     * The comment for the business object.
     * 
     * The comment tag allows for a free format description 
     * of the business object. This information is not loaded 
     * at zX runtime and is only available within the 
     * repository editor and related zX development tools.
     * 
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
     * Whether you are allowed to delete this business object.
     * 
     * @return Returns the deletable.
     */
    public boolean isDeletable() {
        return this.deletable;
    }

    /**
     * @param deletable The deletable to set.
     */
    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    /**
     * The delete rule for this business object.
     * 
     * <p>
     * The delete rule specifies if and, if so, under what 
     * circumstances a business may be deleted from the system.
     * </p>
     * <ul><li>
     * <i>Not Allowed</i>. Instances of this business object can 
     * never be deleted
     * </li><li>
     * <i>Cascade</i>. When an instance of this business object 
     * is deleted, associated business objects will be deleted 
     * as well
     * </li><li>
     * <i>No Associates</i>. An instance of this business object 
     * can only be deleted if there are no associated business 
     * objects
     * </li><li>
     * <i>Per relation</i>. This rule indicates that the delete 
     * rule is defined per related business object in the BO 
     * relations section
     * </li></ul>
     * 
     * @return Returns the deleteRule.
     */
    public zXType.deleteRule getDeleteRule() {
        if (this.deleteRule == null) {
            this.deleteRule = zXType.deleteRule.drAllowed;
        }
        return this.deleteRule;
    }
    
    /**
     * @see #getDeleteRule()
     * @return Returns the deleteRule.
     */
    public String getDeleteRuleAsString() {
        return zXType.valueOf(getDeleteRule());
    }
    
    /**
     * @see #getDeleteRule()
     * @param deleteRule The deleteRule to set.
     */
    public void setDeleteRule(zXType.deleteRule deleteRule) {
        this.deleteRule = deleteRule;
    }

    /**
     * The description of this business object.
     * 
     * The label and description can be entered in multiple 
     * languages and give a ‘natural language’ name to a business 
     * object.
     * 
     * @return Returns the description.
     */
    public ZXCollection getDescription() {
        if (this.description ==  null) {
            this.description = new ZXCollection();
        }
        return this.description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(ZXCollection description) {
        this.description = description;
    }

    /**
     * The helpid for this business object.
     * 
     * The help-id is designed for future use and will be used 
     * to implement a context sensitive, online help system. 
     *   
     * @return Returns the helpId.
     */
    public String getHelpId() {
        return this.helpId;
    }

    /**
     * @see #getHelpId()
     * @param helpId The helpId to set.
     */
    public void setHelpId(String helpId) {
        this.helpId = helpId;
    }

    /**
     * Whether we use just in time parsing.
     * 
     * @return Returns the justInTime.
     */
    public boolean isJustInTime() {
        return this.justInTime;
    }

    /**
     * @param justInTime The justInTime to set.
     */
    public void setJustInTime(boolean justInTime) {
        this.justInTime = justInTime;
    }

    /**
     * The label for this business object.
     * 
     * The label and description can be entered in multiple 
     * languages and give a ‘natural language’ name to a business 
     * object.
     * 
     * @return Returns the label.
     */
    public LabelCollection getLabel() {
        /** Lazy load the LabelCollection. */
        if (this.label == null) {
            this.label = new LabelCollection();
        }
        return this.label;
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(LabelCollection label) {
        this.label = label;
    }

    /**
     * The comment for the last change on this business object.
     * 
     * The last change tag can be used for a free format 
     * description of the last change made to the business 
     * object. It is used for informational purposes only 
     * and not available at zX runtime.
     * 
     * @return Returns the lastChange.
     */
    public String getLastChange() {
        return this.lastChange;
    }

    /**
     * @param lastChange The lastChange to set.
     */
    public void setLastChange(String lastChange) {
        this.lastChange = lastChange;
    }

    /**
     * The name of the business object.
     * 
     * The name of the business object is the same as  the name of 
     * the XML file without the .xml extension and relative to the 
     * business object directory.
     * <p/>
     * In more complex systems, it is advisable to group the 
     * business objects by functional area. Each functional area is 
     * implemented as a sub-directory in the business object directory; 
     * for example <i>cfg/bo/clnt</i> for all client related business 
     * objects and <i>cfg/bo/prdct</i> for all product related entities. 
     * The name of these entities must also be prefixed with the area; for example:
     * 
     * <ul>
     * <li>clnt/clntCntct</li>
     * <li>prdct/prdctTpe</li>
     * </ul>
     * 
     * <p>
     * Note : although the repository editor does allow you to save an 
     * entity using a file name that does not match the entity name, it is 
     * strongly recommended not to do so 
     * </p>
     * 
     * <p>
     * Note : although a backslash will work (e.g. clnt\clntCntct) it is 
     * strongly advised to get used to using a forward slash for the following reasons:
     * </p>
     * 
     * <ul>
     * <li>The backslash is a reserved character in ‘directors’ and ‘expressions’</li>
     * <li>In case of a port of the framework to an Unix environment</li>
     * </ul>
     * 
     * @return Returns the name of the Business Object
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The primary key for this business object.
     * 
     * <p>
     * The primary key is the attribute of the business object 
     * that uniquely identifies the business object and is likely 
     * to correspond to the primary key of the associated table.
     * <br/>
     * Note that zX assumes a single primary key. Compound keys 
     * (i.e. a key that is made up of more than one attribute) are 
     * supported but are far more cumbersome to use.
     * 
     * <p>
     * In case of a compound key, a ‘non-persistent’ attribute has 
     * to be created (this is an attribute that is not associated 
     * with a table column, more about non-persistent attributes in 
     * the chapter on properties and attributes) that has no purpose 
     * other than to serve as a ‘dummy’ primary key.
     * </p>
     * 
     * @return Returns the primaryKey.
     */
    public String getPrimaryKey() {
        return this.primaryKey;
    }

    /**
     * @see #getPrimaryKey()
     * @param primaryKey The primaryKey to set.
     */
    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    /**
     * The delete permissions for this business object.
     * <p>
     * The collection of security groups specify what groups 
     * are able to select, update, delete and insert instances 
     * of this business object; more on security in the chapter 
     * on ‘Audit, locking and security’.
     * 
     * @return Returns the securityDelete.
     */
    public ZXCollection getSecurityDelete() {
        if (this.securityDelete == null) {
            this.securityDelete = new ZXCollection();
        }
        return this.securityDelete;
    }

    /**
     * @param securityDelete The securityDelete to set.
     */
    public void setSecurityDelete(ZXCollection securityDelete) {
        this.securityDelete = securityDelete;
    }

    /**
     * The insert permissions for this business object.
     * 
     * @return Returns the securityInsert.
     */
    public ZXCollection getSecurityInsert() {
        if (this.securityInsert == null) {
            this.securityInsert = new ZXCollection();
        }
        return this.securityInsert;
    }

    /**
     * @param securityInsert The securityInsert to set.
     */
    public void setSecurityInsert(ZXCollection securityInsert) {
        this.securityInsert = securityInsert;
    }

    /**
     * The select permissions for this business object.
     * 
     * @return Returns the securitySelect.
     */
    public ZXCollection getSecuritySelect() {
        if (this.securitySelect == null) {
            this.securitySelect = new ZXCollection();
        }
        return this.securitySelect;
    }

    /**
     * @param securitySelect The securitySelect to set.
     */
    public void setSecuritySelect(ZXCollection securitySelect) {
        this.securitySelect = securitySelect;
    }

    /**
     * The update permissions for this business object.
     * 
     * @return Returns the securityUpdate.
     */
    public ZXCollection getSecurityUpdate() {
        if (this.securityUpdate == null) {
            this.securityUpdate = new ZXCollection();
        }
        return this.securityUpdate;
    }

    /**
     * @param securityUpdate The securityUpdate to set.
     */
    public void setSecurityUpdate(ZXCollection securityUpdate) {
        this.securityUpdate = securityUpdate;
    }

    /**
     * The name of the sequence for this business object.
     * 
     * The sequence is used to generate unique keys in case the 
     * primary key is of type ‘automatic’ (i.e. the system will 
     * generate a guaranteed unique primary key).
     * 
     * @return Returns the sequence.
     */
    public String getSequence() {
        return this.sequence;
    }

    /**
     * @see #getSequence()
     * @param sequence The sequence to set.
     */
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    /**
     * The data constants for this business object.
     * 
     * <p>
     * The data-constants field is optional but can contain an 
     * attribute or group name (or even an adhoc group).
     * </p>
     * <p>
     * Attributes referred to in the data constants group are 
     * likely to be ‘locked’ (i.e. can never be set to a value using 
     * an on-line form) and should really have a default value set. 
     * </p>
     * <p>
     * Whenever a select query is generated by the system, the 
     * attributes in the data constant group wil be added to the 
     * where clause. Effectively this restricts all select queries 
     * to those entries where the value matches that of the default 
     * value of the BO attribute.
     * </p>
     * <p>
     * A good example of use is where the data constants fature 
     * is used in combination with the ‘based on’ feature. <br/>
     * Imagine a ‘role’ table that is used for both client roles 
     * and supplier roles. One attribute (‘type’) indicates wheter 
     * the row is a client- or supplier role. <br/>
     * A single BO is designed for the generic role table, without 
     * any data constants. Two BO’s are created, both based on the 
     * generic role BO. Both BO’s have a data constant set for ‘type’, 
     * one with default value ‘Supplier’ and one with ‘Client’.
     * </p><p>
     * In the application, supplier contacts and client contacts 
     * will have a foreign key relation to their respective role 
     * BO’s.
     * </p>
     * 
     * <pre>
     * DGS04FEB2004: Added Data Constant attribute group
     * </pre>
     * 
     * @return Returns the dataConstantGroup.
     */
    public String getDataConstantGroup() {
        return dataConstantGroup;
    }
    
    /**
     * DGS04FEB2004: Added Data Constant attribute group
     * 
     * @see #getDataConstantGroup()
     * @param dataConstantGroup The dataConstantGroup to set.
     */
    public void setDataConstantGroup(String dataConstantGroup) {
        this.dataConstantGroup = dataConstantGroup;
    }
    
    /**
     * Related business objects.
     * 
     * <p>
     * The BO relation collection is used to define business objects 
     * that have a foreign key to this business object.<br/>
     * Currently this collection is only used to specify delete rules 
     * per related BO but it is expected that other features will 
     * be added in the future.
     * </p>
     * <p>
     * Note that the delete rules will only be considered if the BO 
     * rule is ‘Per relation’.
     * </p><p>
     * Each relation has the name of an entity that must have a 
     * foreign key to this BO. Optionally the name of the attribute 
     * that defines the FK can be specified but in most of the cases 
     * this can be left blank (a good example where it may be useful 
     * is when the related entity has multiple attributes that are a 
     * FK to this BO; e.g. an exchange rate with a ‘from’ and ‘to’ 
     * currency).
     * </p><p>
     * Finally the delete rule for this specific relation is 
     * defined. This is one of the following values:
     * </p>
     * <ul><li>
     * <i>Cascade</i>. Any rows in that table that are related to 
     * this entity will be deleted as well (if their delete rule 
     * permits)
     * </li><li>
     * <i>No associates</i>. A delete is only allowed if there are 
     * no related rows in the related table
     * </li><li>
     * <i>Allowed</i>. No delete special rule applies. This option 
     * is designed for use when a relation may be included for other 
     * reasons that a delete rule and is reserved for future use
     * </li></ul>
     * 
     * @return Returns the BORelations.
     */
    public List getBORelations() {
        return BORelations;
    }
    
    /**
     * @see #getBORelations()
     * @param relations The BORelations to set.
     */
    public void setBORelations(List relations) {
        BORelations = relations;
    }
    
    /**
     * The entity size.
     * 
     * <p>
     * The size property indicates the number of instances that 
     * are available for the business object (i.e. the number 
     * of rows in the table or view associated with the business 
     * object). <br/>
     * Size is mainly used to determine how to display look-up 
     * tables for the business object:
     * </p>
     * 
     * <ul><li>
     * <i>Small</i>. A lookup table is displayed as a pull-down list will 
     * all entries of the table.
     * </li><li>
     * <i>Medium</i>. A lookup table is implemented as a popup-window where 
     * all entries are displayed in a list without a search form
     * </li><li>
     * <i>Large</i>. A lookup table is implemented as a popup where the 
     * user has to search for items using a search form
     * </li></ul>
     * 
     * Size will be explained in more detail when we discuss 
     * relationships between business object.
     * 
     * @return Returns the size.
     */
    public zXType.entitySize getSize() {
        return this.size;
    }

    /**
     * @see #getSize()
     * @param size The size to set.
     */
    public void setSize(zXType.entitySize size) {
        this.size = size;
    }

    /**
     * The table name.
     * <p>
     * The table tag is used to specify what table (or view or 
     * even stored procedure) the business object is associated with.
     * It is valid to have business objects that are not associated 
     * with a database object; use the hyphen (‘-‘) as table name 
     * for this purpose.<br/>
     * Note that business objects can be associated with tables 
     * as well as views (and even stored procedures) but that this 
     * may limit the functionality available at runtime (e.g. a view 
     * may prohibit updates and / or inserts).  
     * 
     * <p>
     * <b>Note:</b> because of legacy reasons the table field is 
     * mandatory even though it is valid to have business objects 
     * that are non-persist-able and thus not associated with a 
     * table or view. Enter ‘-‘ as the table name when no table 
     * is required. Also with datasources this field may be referring
     * to something else.
     * </p>
     * 
     * @return Returns the table name.
     */
    public String getTable() {
        return this.table;
    }

    /**
     * @param table The table to set.
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * The tags for this business object.
     * <p>
     * The collection of tags are ‘tags’ that are associated with 
     * the business object. A tag is a free format name / value pair 
     * that can be referred to in an application. Tags may be used 
     * to store business object related configuration values.
     * 
     * @return Returns the tags.
     */
    public ZXCollection getTags() {
        if (this.tags == null) {
            this.tags = new ZXCollection();
        }
        return this.tags;
    }

    /**
     * @see #getTags()
     * @param tags The tags to set.
     */
    public void setTags(ZXCollection tags) {
        this.tags = tags;
    }

    /**
     * This unique constraint for the business object.
     * 
     * The primary key is assumed to be unique. It can be 
     * however that an instance of a business object has to 
     * be unique by other attributes as well. The unique 
     * constraint can be used to specify one or more attributes 
     * (or a group of attributes) that the business object also 
     * has to have an unique value in the database.
     * 
     * @return Returns the uniqueConstraint.
     */
    public String getUniqueConstraint() {
        return this.uniqueConstraint;
    }

    /**
     * @see #getUniqueConstraint()
     * @param uniqueConstraint The uniqueConstraint to set.
     */
    public void setUniqueConstraint(String uniqueConstraint) {
        this.uniqueConstraint = uniqueConstraint;
    }

    /**
     * The version.
     * <p>
     * The version tag is not used by zX but may be used by 
     * the application for version control purposes. The 
     * repository editor does not apply any validation to this field.  
     * 
     * @return Returns the version.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return Returns the parsingBasedOn.
     */
    public boolean isParsingBasedOn() {
        return this.parsingBasedOn;
    }

    /**
     * @param parsingBasedOn The parsingBasedOn to set.
     */
    public void setParsingBasedOn(boolean parsingBasedOn) {
        this.parsingBasedOn = parsingBasedOn;
    }

    /**
     * Collection of all implicit attributes.
     * 
     * i.e. non-explicit attributes 
     * 
     * @return Returns the implicitAttributes.
     */
    public AttributeCollection getImplicitAttributes() {
        if (this.implicitAttributes == null) {
            this.implicitAttributes = new AttributeCollection();
        }
        return implicitAttributes;
    }
    
    /**
     * @see #getImplicitAttributes()
     * @param implicitAttributes The implicitAttributes to set.
     */
    public void setImplicitAttributes(AttributeCollection implicitAttributes) {
        this.implicitAttributes = implicitAttributes;
    }
    
    /**
     * @return Returns the concurrencyControl.
     */
    public boolean isConcurrencyControl() {
        return concurrencyControl;
    }
    
    /**
     * @param concurrencyControl The concurrencyControl to set.
     */
    public void setConcurrencyControl(boolean concurrencyControl) {
        this.concurrencyControl = concurrencyControl;
    }
    
    /**
     * Event actions defined for this busisness object.
     * Event actions are actions that are executed at defined 
     * events and are mainly used for data validation and 
     * data completion.
     * <br/>
     * See the chapter ‘Event actions’ for more information.
     * 
     * @return Returns the eventActions.
     */
    public List getEventActions() {
        return eventActions;
    }
    
    /**
     * @param eventActions The eventActions to set.
     */
    public void setEventActions(List eventActions) {
        this.eventActions = eventActions;
    }
    
    /**
     * @return Returns the postEvents.
     */
    public String getPostEvents() {
        return postEvents;
    }
    
    /**
     * @param postEvents The postEvents to set.
     */
    public void setPostEvents(String postEvents) {
        this.postEvents = postEvents;
    }
    
    /**
     * @return Returns the preEvents.
     */
    public String getPreEvents() {
        return preEvents;
    }
    
    /**
     * @param preEvents The preEvents to set.
     */
    public void setPreEvents(String preEvents) {
        this.preEvents = preEvents;
    }

    /**
	 * @return the testDataGroup
	 */
	public String getTestDataGroup() {
		return testDataGroup;
	}

	/**
	 * @param testDataGroup the testDataGroup to set
	 */
	public void setTestDataGroup(String testDataGroup) {
		this.testDataGroup = testDataGroup;
	}
	
	/**
	 * @return the testDataValidation
	 */
	public String getTestDataValidation() {
		return testDataValidation;
	}

	/**
	 * @param testDataValidation the testDataValidation to set
	 */
	public void setTestDataValidation(String testDataValidation) {
		this.testDataValidation = testDataValidation;
	}
		
    //------------------------ Utility methods
    
    /**
     * Get attribute group from the descriptor by the name.
     * 
     * <pre>
     * 
     * <b>There are some rules around the name :</b>
     * 
     * These are the rules and the order in which they are executed : "" or
     * null (java null) - will return a empty attribute as the group THEN <br/>
     * name is equal to lastgroup or lastgroup2 - if the name is equal to one
     * of the last 2 cached group, it will reused it. THEN <br/>name is equal
     * to one of the attributeGroups - Returns the attributes of the
     * attributeGroup. THEN <br/>*, print and all - will return all of the
     * attributes as the group. THEN <br/>empty or null (string) - Will return
     * a empty attribute as the group. THEN <br/>
     * 
     * If none of these conditions are met then it is presumed to be a comma
     * seperate string.
     * 
     * The elements beginning with + will be added to a ColAttribute. and
     * element beginning with - will be subtracted from the ColAttribute.
     * 
     * 
     * NOTE : All AttributeCollections returned a clones. 
     * ie :
     * <code>
     * AttributeCollection colAttr = objDesc.getGroup("id,nme");
     * AttributeCollection colAttr2 = objDesc.getGroup("id,nme");
     * if (colAttr != colAttr2) // Now the same handle
     * // and
     * colAttr.remove("id") will not effect colAttr2
     * </code>
     * 
     * However these are shallow clones, eg : if i have a handle to a Attribute and update it, all of the 
     * AttributeCollections will be effected by these changes.
     * 
     * NOTE : calling getGroup twice with the same string with return a cached collection, but a shallow 
     * cloned one.
     * 
     * DGS12JAN2005: Revised to ensure when get from cache, don't overwrite the other cache
     * </pre>
     * 
     * @param pstrGroup Name of the attributegroup to return.
     * @return Returns a AttributeCollection.
     */
    public AttributeCollection getGroup(String pstrGroup) {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        AttributeCollection getGroup = new AttributeCollection();
        
        try {
            
            /**
             * In some circumstances the empty group may be requested, simple
             * return an empty collection
             */
            if (StringUtil.len(pstrGroup) == 0) {
                return getGroup;
            }
            
            /**
             * Make this case insensitive : Usefull considering people always assume or forget about case sensitivity. 
             */
            pstrGroup = pstrGroup.toLowerCase();

            /**
             * See if the requested group is the same as one of the last
             * requested groups. Have a cache of 2 groups as often as you find
             * that it is an alternation between 2 groups.
             * 
             * DGS12JAN2005: Made a change here so that it re-caches it in the same one -
             * was previously putting it (also) into the other cache, so losing any other
             * group cached there.
             */
            if (pstrGroup.equals(this.lastGroupDef)) {
                getGroup = (AttributeCollection)this.lastGroup.clone();
                // This has the effect of re-caching back in 1
                this.lastCacheSlot = 2; 
                return getGroup;
            } else if (pstrGroup.equals(this.lastGroupDef2)) {
                getGroup = (AttributeCollection)this.lastGroup2.clone();
                // This has the effect of re-caching back in 2
                this.lastCacheSlot = 1; 
                return getGroup;
            }

            /**
             * The most simple form is that the group is simply a defined group
             */
            AttributeGroup objAttributeGroup = getAttributeGroups().get(pstrGroup);
            if (objAttributeGroup != null) {
                getGroup = (AttributeCollection)objAttributeGroup.getAttributes().clone();
                return getGroup;
            }

            /**
             * Another simple one is '*' (I want all attributes) or the print
             * group as this is the same as all if no print group has been
             * explicitly set-up
             * 
             * Also cater for all for backwards compatability
             */
            if (pstrGroup.equals("*") || pstrGroup.equals("print") || pstrGroup.equals("all")) {
                getGroup = (AttributeCollection)this.implicitAttributes.clone();
                return getGroup;
            }
            
            /**
             * All of the attribute including explicit ones 
             */
            if (pstrGroup.equals("@")) {
                getGroup = (AttributeCollection)this.attributes.clone();
                return getGroup;
            }
            
            /**
             * ! = Is the audit attribute groups 
             */
            if (pstrGroup.equals("!")) {
                getGroup = (AttributeCollection)getAuditAttributes().clone();
                return getGroup;
            }
            
            /**
             * The empty group
             */
            if (pstrGroup.equals("empty") || pstrGroup.equals("null")) {
                return getGroup;
            }

            /**
             * Now we are dealing with seperated named attributes or misc "+", "!" etc..
             * For example :  "!,-id" 
             */
            AttributeCollection colTmp;
            Iterator iterAttrs;
            String strGroup;
            Attribute objAttr;
            
            // Break the attribute group string into tokens seperaed by ","
            StringTokenizer groupTokens = new StringTokenizer(pstrGroup, ",");
            while (groupTokens.hasMoreElements()) {
                strGroup = groupTokens.nextToken().trim(); // Should be trimmed anyway.
                
                if (strGroup.charAt(0) == '-') {
                    colTmp = getSingleGroup(strGroup.substring(1));
                    
                    /**
                     * Remove entries from the collection.
                     * 
                     * NOTE : -id,* will not work, do a *,-id rather.
                     */
                    if (colTmp != null) {
                        iterAttrs = colTmp.iteratorKey();
                        while (iterAttrs.hasNext()) {
                            getGroup.remove(iterAttrs.next());
                        }
                    }
                    
                } else if (strGroup.startsWith("\\-")) {
                	/**
                	 * This is a special construct desigend for order by groups
                     * for example date,\-time would result in
                     * order by date asc, time desc
                     * We add attributes to collection but set an attrbute tag
                     * that is picked up by the orderByGroup method in clsSQL
                	 */
                    colTmp = getSingleGroup(strGroup.substring(2));
                    
                    /**
                     * Now add the colTmp collection to the base collection
                     * and set the special tag 
                     */
                    if (colTmp != null && colTmp.size() != 0) {
                        iterAttrs = colTmp.iterator();
                        while (iterAttrs.hasNext()) {
                            objAttr = (Attribute)iterAttrs.next();
                            objAttr.setTag("zXOrderDesc", "1");
                            getGroup.put(objAttr.getName(), objAttr);
                        }
                    }
                    
                } else {
                    colTmp = getSingleGroup(strGroup);
                    
                    /**
                     * Add to the getGroup collection
                     */
                    if (colTmp != null && colTmp.size() != 0) {
                        iterAttrs = colTmp.iterator();
                        while (iterAttrs.hasNext()) {
                            objAttr = (Attribute)iterAttrs.next();
                            
                            // NOTE : Duplicates will be overidden.
                            getGroup.put(objAttr.getName(), objAttr);
                        }
                    }
                }
            }
            
            return getGroup;
        } finally {
            /**
             * Cache only if we have successfully create a AttributeCollection
             */
            if (getGroup != null) {
	            if (this.lastCacheSlot == 1) {
	                this.lastGroupDef2 = pstrGroup;
	                this.lastGroup2 = (AttributeCollection)getGroup.clone();
	                this.lastCacheSlot = 2;
	            } else {
	                this.lastGroupDef = pstrGroup;
	                this.lastGroup = (AttributeCollection)getGroup.clone();
	                this.lastCacheSlot = 1;
	            }
            }
            
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(getGroup);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * A safer version of the getGroup method.
     * 
     * <pre>
     * 
     * DGS12JAN2005: New function to solve problem with getGroup groups being corrupted. 
     * Get attribute group from descriptor - return safe copy that can be changed without
     * affecting the source collection.
     * </pre>
     * 
     * @param pstrGroup The attribute group to get.
     * @return Returns a cloned attribute collection.
     */
    public AttributeCollection getGroupSafe(String pstrGroup) {
        return (AttributeCollection)getGroup(pstrGroup).clone();
    }
    
    /**
     * Get a single AttributeCollection from a string.
     * 
     * <pre>
     * 
     * Order in which the attribute collection gets resolved :
     * <attributegroupname> = If there is a match attribute collection it will return that one first.
     * "*"			= All implicit attributes.
     * "all"			= All implicit attributes.
     * "@"   			= All of the attributes.
     * "+"   			= The primary key attribute collection.
     * "!"  	 		= The audit attribute collection.
     * "~" 			= zXUpdtdId attribute collection.
     * "<attributename>" = If there is a matching attribute return it in a attribute collection.
     * "pklabel" 		= Is also "+,Label".
     * "pkdescription" =  Is also "+,Description".
     * "fkpopup" = Is also "*".
     * "qssearch" = Is also "search".
     * qsorder = Is also "+".
     * 
     * </pre>
     * @param pstrGroup The name of the group.(This is case insensitive)
     * @return Returns a single AttributeCollection from a string.
     */
    public AttributeCollection getSingleGroup(String pstrGroup) {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        AttributeCollection getSingleGroup = null;
        
        try {
            //  Make case insensitve
            pstrGroup = pstrGroup.toLowerCase();

            /**
             * Scenario 1: group is a defined group
             */
            AttributeGroup objAttributeGroup = getAttributeGroups().get(pstrGroup);
            if (objAttributeGroup != null) {
                getSingleGroup = (AttributeCollection)objAttributeGroup.getAttributes().clone();
                return getSingleGroup;
            }
            
            /**
             * Scenario 2: group is * (ie all attributes)
             * 
             * All = Alias for *, legacy
             */
            if (pstrGroup.equals("*") || pstrGroup.equals("all")) {
                getSingleGroup = (AttributeCollection)this.implicitAttributes.clone();
                return getSingleGroup;
            }
            
            /**
             * OR 
             * 
             * @ = Truly all attibutes, including explicit ones
             */
            if (pstrGroup.equals("@")) {
                getSingleGroup = (AttributeCollection)this.attributes.clone();
                return getSingleGroup;
            }
            
            /**
             * Scenario 3: group is + (ie PK)
             */
            if (pstrGroup.equals("+")) {
                getSingleGroup = new AttributeCollection();
                getSingleGroup.put(this.primaryKey, getAttribute(this.primaryKey));
                return getSingleGroup;
            }

            /**
             * Scenario 4: group is ! (ie the audit attributes)
             */
            if (pstrGroup.equals("!")) {
                getSingleGroup = (AttributeCollection)getAuditAttributes().clone();
                return getSingleGroup;
            }
            
            /**
             *  Scenario 5: Group is ~ (ie the zXUpdtdId attributes)
             */
            if (pstrGroup.equals("~") && this.concurrencyControl) {
                getSingleGroup = new AttributeCollection();
                getSingleGroup.put("zxupdtdid", this.auditAttributes.get("zxupdtdid"));
                return getSingleGroup;
            }
            
            /**
             * Scenario 6: group is a single attribute
             */
            Attribute objAttr = getAttribute(pstrGroup);
            if (objAttr != null) {
                getSingleGroup = new AttributeCollection();
                getSingleGroup.put(objAttr.getName(), objAttr);
                return getSingleGroup;
            }
            
            /**
             * Scenario 7 + 8: PKLabel / PKDescription
             */
            if (pstrGroup.equals("pklabel")) {
                getSingleGroup = getGroup("+,Label");
                return getSingleGroup;
            }
            
            if (pstrGroup.equals("pkdescription")) {
                getSingleGroup = getGroup("+,Description");
                return getSingleGroup;
            }
            
            /**
             * The fkPopup group may not exist in which case we use *
             */
            if (pstrGroup.equals("fkpopup")) {
                getSingleGroup = getGroup("*");
                return getSingleGroup;
            }
            
            /**
             * The *.load variations may not exist, in which case we use description or label
             **/
            if (pstrGroup.equals("description.load")) {
                getSingleGroup = getGroup("description");
                return getSingleGroup;
            }
            if (pstrGroup.equals("label.load")) {
                getSingleGroup = getGroup("label");
                return getSingleGroup;
            }
            
            /**
             * The QSSearch group may not exist in which case we use Search, if that exists
             */
            if (pstrGroup.equals("qssearch")) {
                getSingleGroup = getGroup("search");
                return getSingleGroup;
            }            
            
            /**
             * The QSOrder group may not exists in which case we return empty group
             * DGS28JUL2004: Remove some unnecessary saveCollectionItem stuff here
             */
            if (pstrGroup.equals("qsorder")) {
                getSingleGroup = getGroup("+");
                return getSingleGroup;
            }
            
            if (getZx().log.isWarnEnabled()) {
                getZx().log.warn("Failed to get AttributeGroup : " + pstrGroup);
            }
            
            return getSingleGroup; // throw Exeception ?
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getSingleGroup);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * groupMinus - Do the mathematical minus of two attribute groups
     * 
     * @param pstrGroup1 The group you want to subtract from
     * @param pstrGroup2 The group to subtract.
     * @return A AttributeCollection of the result.
     */
    public AttributeCollection getGroupMinus(String pstrGroup1, String pstrGroup2) {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup1", pstrGroup1);
            getZx().trace.traceParam("pstrGroup2", pstrGroup2);
        }
        
        AttributeCollection getGroupMinus = null;
        
        try {
            if (pstrGroup1.equalsIgnoreCase(pstrGroup2)) {
                // Short circuit as they cancel each other out.
                return new AttributeCollection();
                
            } else if (StringUtil.len(pstrGroup2) == 0){
                return getGroupMinus = getGroup(pstrGroup1);
                
            } else {
                getGroupMinus = getGroup(pstrGroup1);
                AttributeCollection colGroupsMinus = getGroup(pstrGroup2);
                Iterator iterKeys = colGroupsMinus.iteratorKey();
                while (iterKeys.hasNext()) {
                    getGroupMinus.remove(iterKeys.next());
                }
            }
            return getGroupMinus;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getGroupMinus);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * groupMinusAsString - String version of the subtraction
     * 
     * @param pstrGroup1 The group to subtract from
     * @param pstrGroup2 The group to subtract
     * @deprecated do getGroupMinus(pstrGroup1,pstrGroup2).toString()
     * @return Returns the string from of a group minus another group.
     */
    public String getGroupMinusAsString(String pstrGroup1, String pstrGroup2) {
        return getGroupMinus(pstrGroup1, pstrGroup2).formattedString();
    }

    /**
     * getGroupPlus - Do the mathematical plus of two attribute groups
     * 
     * @param pstrGroup1 Group to add
     * @param pstrGroup2 Group to add
     * @return Returns the AttributeCollection of 2 groups.
     */
    public AttributeCollection getGroupPlus(String pstrGroup1, String pstrGroup2) {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup1", pstrGroup1);
            getZx().trace.traceParam("pstrGroup2", pstrGroup2);
        }

        AttributeCollection getGroupPlus = null;

        try {
            
            if (StringUtil.len(pstrGroup1) > 0) {
                getGroupPlus = getGroup(pstrGroup1);
                
                AttributeCollection getGroupPlus2 = getGroup(StringUtil.len(pstrGroup2) == 0 ? "" : "," + pstrGroup2);
                Attribute objAttr;
                Iterator iter = getGroupPlus2.iterator();
                while (iter.hasNext()) {
                    objAttr = (Attribute)iter.next();
                    getGroupPlus.put(objAttr.getName(), objAttr);
                }
                
            } else {
                getGroupPlus = getGroup(pstrGroup2);
            }
            
            return getGroupPlus;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getGroupPlus);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * getGroupPlusAsString - String form of the addition
     * 
     * @param pstrGroup1 Group to add
     * @param pstrGroup2 Group to add
     * @deprecated Do not use this rather used toString.
     * @return Gets the string form of a group plus another group.
     */
    public String getGroupPlusAsString(String pstrGroup1, String pstrGroup2) {
        return getGroupPlus(pstrGroup1, pstrGroup2).toString();
    }
    
    /**
     * Return the collection of attributes that is the
     * intersection of two groups
     * 
     * @param pstrGroup1 The first group
     * @param pstrGroup2 The second group.
     * @return Returns the intersection of the two groups.
     */
    public AttributeCollection groupIntersect(String pstrGroup1, String pstrGroup2) {
        AttributeCollection groupIntersect = new AttributeCollection();
        
        AttributeCollection colOneGroup;
        AttributeCollection colOtherGroup;
        
        /**
         * First get the two groups
         */
        AttributeCollection colGroup1 = getGroup(pstrGroup1);
        AttributeCollection colGroup2 = getGroup(pstrGroup2);
        
        /**
         * Include all attributes from group 1 that are in group 2
         * 
         * For minor efficiency gain, loop over group with smalles
         * number of entries
         */
        if (colGroup1.size() > colGroup2.size()) {
            colOneGroup = colGroup1;
            colOtherGroup = colGroup2;
        } else {
            colOneGroup = colGroup2;
            colOtherGroup = colGroup1;
        } // Decide on most efficient way of doing business
        
        Attribute objAttr;
        Iterator iter = colOneGroup.iterator();
        while (iter.hasNext()) {
            objAttr = (Attribute)iter.next();
            if (colOtherGroup.inGroup(objAttr.getName())) {
                groupIntersect.put(objAttr.getName(), objAttr);
            }
        }
        
        /**
         * And done
         */
        return groupIntersect;
    }
    
    /**
     * Does group contain the named attributed
     *
     * @param pstrGroup The group to check 
     * @param pstrAttr The attribute to look for. 
     * @return Return whether the group has a specific named attribute.
     */
     public boolean groupHasAttr(String pstrGroup, String pstrAttr) {
         if(getZx().trace.isFrameworkCoreTraceEnabled()) {
             getZx().trace.enterMethod();
             getZx().trace.traceParam("pstrGroup", pstrGroup);
             getZx().trace.traceParam("pstrAttr", pstrAttr);
         }
         
         boolean groupHasAttr = false; 
         
         try {
             AttributeCollection colAttr = getGroup(pstrGroup);
             if (colAttr != null) {
                 Attribute objAttr = colAttr.get(pstrAttr);
                 groupHasAttr = objAttr == null ? false : true;
             }
             
             return groupHasAttr;
             
         } finally {
             if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                 getZx().trace.returnValue(groupHasAttr);
                 getZx().trace.exitMethod();
             }
         }
     }
    
    /**
     * getAttr - Get attribute by name, either from attributes collection or
     * audit attributes collection.
     * 
     * <pre>
     * 
     * NOTE : This was getAttr in the VB version of the framework
     * NOTE : This lower cases the name for case insensetivity.
     * </pre>
     * 
     * @param pstrAttr The name of the attribute you want. This is case insensitive for now.
     * @return Returns a Attribute
     */
    public Attribute getAttribute(String pstrAttr) {
        if (StringUtil.len(pstrAttr) > 0) {
            // To allow for case insensetive.
            pstrAttr = pstrAttr.toLowerCase();
        }
        
        /**
         * Treat + as the primary key
         */
        if (pstrAttr.equals("+")) pstrAttr = this.primaryKey;
        
        Attribute getAttribute = getAttributes().get(pstrAttr);
        if (getAttribute == null) { 
            return getAuditAttributes().get(pstrAttr); 
        }
        
        return getAttribute;
    }

    /**
     * pkAttr - Return the primary key attribute.
     * 
     * @return Returns the PK attribute.
     */
    public Attribute pkAttr() {
        return getAttribute(this.primaryKey);
    }
    
    /**
     * Does this descriptor have a certain tag
     *
     * @param pstrTag The name of the tag you want to look for 
     * @return Return whether does this descriptor have a certain tag
     */
     public boolean hasTag(String pstrTag) {
         if(getZx().trace.isFrameworkCoreTraceEnabled()) {
             getZx().trace.enterMethod();
             getZx().trace.traceParam("pstrTag", pstrTag);
         }

         boolean hasTag = false; 
         
         try {
             
             return this.tags.get(pstrTag) != null;
             
         } finally {
             if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                 getZx().trace.returnValue(hasTag);
                 getZx().trace.exitMethod();
             }
         }
     }
     
     /**
      * Return the value of a tag .
      * 
      * <pre>
      * 
      * Return the value of a tag or vbnullstring in case the tag had not been found
      * </pre>
      *
      * @param pstrName The name of the tag 
      * @return Return the value of a tag or vbnullstring in case the tag had not been found
      */
     public String tagValue(String pstrName) {
         if(getZx().trace.isFrameworkCoreTraceEnabled()) {
             getZx().trace.enterMethod();
             getZx().trace.traceParam("pstrName", pstrName);
         }

         String tagValue = ""; 
         
         try {
             
             Tuple objTuple = (Tuple)this.tags.get(pstrName);
             if (objTuple != null) {
                 tagValue = objTuple.getValue();
             }
             
             return tagValue;
             
         } finally {
             if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                 getZx().trace.returnValue(tagValue);
                 getZx().trace.exitMethod();
             }
         }
     }
     
     /**
      * Set a certain tag.
      * 
      * <pre>
      * 
      * Set a certain tag; same as remove when value is ''
      * </pre>
      *
      * @param pstrName Name of tag 
      * @param pstrValue Value of the tag 
      */
     public void setTag(String pstrName, String pstrValue){
         if(getZx().trace.isFrameworkCoreTraceEnabled()) {
             getZx().trace.enterMethod();
             getZx().trace.traceParam("pstrName", pstrName);
             getZx().trace.traceParam("pstrValue", pstrValue);
         }

         try {
             
             if (StringUtil.len(pstrValue) > 0) {
                 Tuple objTuble = new Tuple();
                 objTuble.setName(pstrName);
                 objTuble.setValue(pstrValue);
                 
                 /**
                  * Delete and add -- not needed in java :)
                  */
                 this.tags.put(pstrName, objTuble);
                 
             } else {
                 /**
                  * Just delete
                  */
                 this.tags.remove(pstrName);
             }
             
         } finally {
             if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                 getZx().trace.exitMethod();
             }
         }
     }
     
     /**
      * Add attribute to attributes and implicitAttributes collection
      * 
      * VERY IMPORTANT: USE INSTEAD OF ME.ATTRIBUTES.ADD UNLESS YOU ARE
      * AWARE THAT YOU ALSO NEED TO ADD TO ME.IMLPICITATTRIBUTES.ADD
      * 
      * @param pobjAttr The attribute you want to add to the attribute collection and possible the implicite attribute collection.
      */
     public void addAttribute(Attribute pobjAttr) {
         /**
          * Always add to attributes
          */
         this.attributes.put(pobjAttr.getName(), pobjAttr);
         
         /**
          * Optionally to implicit ones as well
          */
         if (!pobjAttr.isExplicit()) {
             this.implicitAttributes.put(pobjAttr.getName(), pobjAttr);
         }
     }
     
     /**
      * Remove attribute from attributes and implicitAttributes collection.
      * 
      * <pre>
      * 
      * NOTE : pstrName is case sensitive. 
      * 
      * VERY IMPORTANT: USE INSTEAD OF ME.ATTRIBUTES.REMOVE UNLESS YOU ARE
      * AWARE THAT YOU ALSO NEED TO ADD TO ME.IMLPICITATTRIBUTES.REMOVE
      * </pre>
      * 
     * @param pstrName The name of the attribute you want to remove.
     */
    public void delAttribute(String pstrName) {
        
         if (this.implicitAttributes != null) {
             this.implicitAttributes.remove(pstrName);
         }
         this.attributes.remove(pstrName);
         
    }
    
    /**
	 * Add the 5 audit attributes defined in the zxAuditAttribute.xml file to
	 * the business object.
	 */
	public void addAuditAttributes() {
		if (getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
		}

		try {
			if (getAuditAttributes() == null) {
				setAuditAttributes(new AttributeCollection());
			}

			/**
			 * Simply copy the audit attributes from the definition collection
			 * read-in by zx.
			 */
			boolean blnInclude = true;
			Attribute objAttr;

			AttributeCollection colAuditAttr = getZx().getAuditAttributes();
			if (colAuditAttr == null) {
				/**
				 * A fatal error programming error have accurred.
				 */
				throw new RuntimeException("Failed to get audit attributes");
			}

			Iterator iterAuditAttr = colAuditAttr.iterator();
			while (iterAuditAttr.hasNext()) {
				objAttr = (Attribute) iterAuditAttr.next();
				
				if (objAttr.getName().equalsIgnoreCase("zXUpdtdId")) {
					blnInclude = isConcurrencyControl();
				}

				if (blnInclude) {
					getAuditAttributes().put(objAttr.getName(), objAttr);

					/**
					 * Do NOT use me.attributes.add unless you are aware that
					 * you may also need to add to me.implicitAttributes.add
					 */
					addAttribute(objAttr);
				}
			}

		} finally {
			if (getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * dumpAsXML - Dumps this object as a formatted xml string.
	 * 
	 * V1.5:20 - BD30JUN05 - Added support for enhanced FK label behaviour
	 * V1.5:29 - BD8JUL05 - Added ensureLoaded
	 * 
	 * @return A xml representation of the Descriptor.
	 */
     public String dumpAsXML() {
    	 return DescriptorParserDomImpl.dumpAsXML(this);
     }
     
     //------------------------ Object overriden methods
     
     /**
      * @see java.lang.Object#toString()
      */
     public String toString() {
         StringBuffer toString = new StringBuffer(256);
         toString.append(dumpAsXML()); // Maybe not do this :)
         return toString.toString();
     }

}