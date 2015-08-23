/*
 * Created on Jan 28, 2004 by Michael Brewer
 * $Id: ZXBO.java,v 1.1.2.64 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.datasources.DSWhereClause;
import org.zxframework.exception.NestableException;
import org.zxframework.jdbc.ZXResultSet;
import org.zxframework.property.BooleanProperty;
import org.zxframework.property.DateProperty;
import org.zxframework.property.DoubleProperty;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * ZXBO - The core ZX Business object.
 * 
 * <pre>
 * 
 * NOTE : This will be the super class for the BO objects.
 * NOTE: We might have to more some of these methods into ZXBOS to make this object more or less
 * stateless.
 * 
 * TODO : Move the majority of these methods into the ZXBOS object. And only leave the most frequently used methods.
 * 
 * Change    : BD7DEC02
 * What      : Changed insertBO and updateBO to go through recordset rather
 *             that old fashioned queries as the latter didnt seem to work with
 *             long Oracle LONG columns
 * 
 * Change    : BD7DEC02
 * What      : deleterule support
 * 
 * Change    : BD7APR03
 * Why       : Added support for zXExpression datatype
 * 
 * Change    : DGS15APR2003
 * Why       : New optional parameter to rs2obj to use 'Pure' attribute names. This is then
 *             used in DeleteBO when getting the PK, as we have to use pure names when deleting.
 *             Getting the PK in DeleteBO is itself a change, to solve a bug when using zXBOMdl
 *             at multiple levels.
 * 
 * Change    : DGS28APR2003
 * Why       : In updateBO, don't perform the duplicates check if the unique constraint is the PK.
 *             The user may or may not have changed the PK value to another one, and if not there
 *             will be one 'duplicate' (itself), so not a useful test.
 * 
 * Change    : BD15JUN03
 * Why       : Use raw formatted value in CSV row
 * 
 * Change    : BD15JUL03
 * Why       : Looked more closely at resetBO; now really reset attributes
 * 
 * Change    : BD1SEP03
 * Why       : Added special values for reset including director support
 * 
 * Change    : BD16OCT03
 * Why       : Added sum / max / avg / min / max functions and the support
 *             function SQLFunc
 * 
 * Change    : DGS19NOV2003
 * Why       : In csvRow, get the formatted value of each attr, not the 'raw' value. Scope for future
 *             compatibility-breaking change to make this an optional parameter defaulting to 'not raw'.
 * 
 * Change    : BD26NOV03
 * Why       : Change return datatype of quickFKLoad to object so it can be used
 *             by ASP
 * 
 * Change    : BD3DEC03
 * Why       : Added quickFKCollection method
 * 
 * Change    : DGS04FEB2004
 * Why       : In loadBO, add further where clause in support of new ability to specify
 *             data constant attributes.
 * 
 * Change    : DGS16FEB2004
 * Why       : Very minor bug fix to Compare method
 * 
 * Change    : DGS01MAR2004
 * Why       : quickFKLoad must return an iBO, not a BO, for ASP to work correctly
 * 
 * Change    : BD04MAR04
 * Why       : If there is no table associated with a BO (i.e table is either
 *             blank or '-'); do allow to enter load / update / insert / bo
 *             but do not do anything other than the persist calls
 * 
 * Change    : BD5MAR04
 * Why       : Added DBClone and DBCloneFK functions
 * 
 * Change    : DGS09MAR2004
 * Why       : resetBO now calls a new public method that does basically the same job.
 *             That new method has a parameter that can be set to do things slightly differently
 *             when called directly (see resetExplicitBO).
 * 
 * Change    : BD20MAR04
 * Why       : Started to implement prePersist / postPersist calls through the
 *             appropriate BOS wrapper functions in preparation of very
 *             exiting new features like auditing and BO persist actions
 * 
 * Change    : BD26MAR04
 * Why       : Support for new delete rules based on BO relations
 * 
 * Change    : BD28MAR04
 * Why       : Added lastRc as parameter to postPersist
 * 
 * Change    : DGS29MAR2004
 * Why       : quickLoad and quickFKLoad don't raise an error if the load fails.
 * 
 * Change    : BD15MAY04
 * Why       : Implemented the long awaited event actions!!!!
 *             See post- and prePersist
 * 
 * Change    : BD16MAY04
 * Why       : Small change: only eveluate event actions when BO validation is on
 * 
 * Change    : DGS20MAY2004
 * Why       : In DB2Collection, the whereClause needed ' AND ' prefixing.
 * 
 * Change    : BD20MAY04
 * Why       : Added sumGroup method to sum group of (numeric) attributes
 * 
 * Change    : DGS?
 * Why       : Added improved error handling to rs2obj and bo2rs
 * 
 * Change    : BD2JUN04
 * Why       : In handleEventAction do not consider group on insert / delete as the group i
 *             no relevant for these persist actions
 * 
 * Change    : BD8JUN04
 * Why       : Small performance gain by caching most recent query used in
 *             loadBO; especially usefull for matrix edit forms
 * 
 * Change    : BD9JUN04
 * Why       : Added support for auditable or concurrency control BOs
 * 
 * Change    : BD13JUN04
 * Why       : - Save and restore zXMe when handlingeventActions as it may
 *               be that an event action can cause recursive calling
 *               of event actions
 *             - Added event action group behaviour 'only'
 *             - Added dynamicValue to attribute
 * 
 * Change    : BD18JUN04
 * Why       : Fixed important bug in rs2obj to do with distinct queries and
 *             auditable. See rs2obj for details
 *             Also added better error message
 * 
 * Change    : BD21JUN04
 * Why       : Added small performance gain in rs2obj by trying to retrieve
 *             fields from rs by number first and only if this fails by
 *             column name
 * 
 * Change    : DGS21JUL2004
 * Why       : Minor glitch fixed in handleEventActions for Some and None behaviours
 * 
 * Change    : DGS27JUL2004
 * Why       : In rs2obj, only treat "~" group as "null" after adding "~" if it wasn't "~"
 *             in the first place.
 * 
 * Change    : BD7AUG04
 * Why       : Fixed bug with query caching in loadBO in case of alias (minor tweaks to this change by DGS)
 * 
 * Change    : BD10AUG04
 * Why       : Fixed bug updateBO in combination with alias set
 * 
 * Change    : DGS11AUG2004
 * Why       : In boModelDeleteBO and boRelationDeleteBO, if rule is noAssociates, instead of
 *             deleting related rows, check if any exist. No need to work down deleting through
 *             related BOs because as soon as we find one below we can stop.
 * 
 * Change    : DGS16AUG2004
 * Why       : Added new function boValidate, which does a setAttr for each attr
 *             and reports an error if any fails (wide range of checks in setAttr)
 * 
 * Change    : BD16AUG2004
 * Why       : Fixed serious bug in db2collection: if a key attribute is used,
 *             do not use formattedValue; use strValue instead
 * 
 * Change    : BD17AUG2004
 * Why       : Introduced new bug in db2collection; now we moved from formattedString
 *             to getAttr.strValue, we do no longer support '+'
 * 
 * Change    : BD5SEP04
 * Why       : Allow the developer to use the bo.validate flag to tell that it is
 *             OK to insert / update a row (see rs2bo)
 * 
 * Change    : BD9SEP04
 * Why       : In getFKAttr now also take the FK alias into consideration
 * 
 * Change    : BD20NOV04
 * Why       : In BO2XML, escape non-XML characters and always use vbNullstring for null values;
 *             do NOT rely on attr.strValue as this will return 0 in case of datatype long, double or automatic
 *             In XML2BO handle null values properly
 * 
 * Change    : BD17DEC04 - 1.4:8
 * Why       : Added loadByQS method
 * 
 * Change    : DGS17JAN2005
 * Why       : In rs2obj, if field name is empty, assume have got correct field by position
 * 
 * Change    : BD24JAN04 - V1.4:29
 * Why       : No longer escape funny characters as it
 *             causes problems with MSXML3 and does not seem to do too much
 *             harm with MSXML4
 *
 * Change    : DGS08FEB2005
 * Why       : In simpleDeleteBO has to be able to handle an empty where condition
 * 
 * Change    : DGS21FEB2005 - V1.4:50
 * Why       : In setAuditAttr when setting zXUpdtId take the rightmost 9 characters,
 *             not the leftmost as was the case, for maximum chance of uniqueness
 * 
 * Change    : BD28FEB05 - V1.4:52
 * Why       : In BO2BO, also copy the FKLabel
 * 
 * Change    : BD28FEB05 - V1.5:1
 * Why       : Introduced data sources / persistence channels
 * 
 * Change    : BD13APR05 - V1.5:4
 * Why       : Fixed bug in bo2rs that was introduced in V1.5:1 - did not read appropriate
 *             adodb.recordset property to determine whether we were in addNew state or not
 * 
 * Change    : BD18MAY05 - V1.5:16
 * Why       : - Fixed bug in sqlFunc when descriptor has an alias
 *             - Fixed bug in XML2BO when passing an attribute group
 *             
 * Change    : BD18MAY05 - V1.5:18
 * Why       : Added support for property persistStatus
 * 
 * Change    : BD01JUL05 - V1.5:29
 * Why       : Added support for ensureLoaded group for event actions
 * 
 * Change    : BD11JUL05 - V1.5:30
 * Why       : Fixed bug in setPropertyPersistStatus ; make sure not to
 *             resolve dynamic values when there is no need to
 * 
 * Change    : DGS31AUG2005 - V1.4:100
 * Why       : In bo2rs, set any large objects after all other columns, to avoid problems such
 *             as are caused in Oracle with LOBs.
 * 
 * Change    : BD2SEP05 - V1.5:45
 * Why       : Error is generated in updateBO when no records were found to be updated; this is
 *             not an error but a warning
 *             
 * Change    : BD28DEC05 - V1.5:74
 * Why       : Do not touch persistStatus when getting dynamic value of attribute
 * 
 * Change    : BD07FEB06 - V1.5:86
 * Why       : Fix bug in doesExists; now handles enhanced and full where-groups properly
 *
 * Change    : BD26MAR06 - V1.5:95 (TODO : Need to think about how to merge this feature.)
 * Why       : Added extendGroup support for eventAction
 *
 * Change    : BD1JUN06 - V1.5:96
 * Why       : The logic in rs2obj to reset all the property persist statuses is now moved
 *               to clsDSRS
 *            
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ZXBO extends ZXObject {

    //------------------------ Members
    
    private Descriptor descriptor;
    
    //------------------------ Runtime members
    
    private transient Map properties;
    private transient boolean validate;
    private transient zXType.persistStatus persistStatus;
    private transient zXType.rc lastPostPersistRC;
    private transient DSHandler DSHandler;
    
    private transient Map editEnhancers;
    private transient Map editEnhancersDependant;
    
    //------------------------ Constructors  
    
    /**
     * Default constructor.
     */
    public ZXBO() {
        super();
        
        // Initialize the properties collection
        this.properties = new HashMap();
        
        /**
         * set validate to true and persist to clean
         */
        this.validate = true;
        this.persistStatus = zXType.persistStatus.psClean;
    }
    
    /**
     * Initialises the ZXBO descriptor.
     * 
     * @param pobjDesc The descriptor for the zXBO
     */
    public void init(Descriptor pobjDesc) {
        this.descriptor = pobjDesc;
    }
    
    //------------------------ Getters and Setters

    /**
     * The property descriptor is the handle to the business 
     * object descriptor.
     * 
     * @return Returns the descriptor.
     */
    public Descriptor getDescriptor() {
        return this.descriptor;
    }

    /**
     * @see ZXBO#getDescriptor()
     * @param pobjDescriptor The descriptor to set.
     */
    public void setDescriptor(Descriptor pobjDescriptor) {
        this.descriptor = pobjDescriptor;
    }
    
    //------------------------ Runtime getters/setters
    
    /**
     * A collection (Hashmap) of edit enhancers(ArrayList), this can be used to override the stylesheet of the attribute.
     * 
	 * @return the editEnhancers
	 */
	public Map getEditEnhancers() {
		return editEnhancers;
	}
	
	/**
	 * @param editEnhancers the editEnhancers to set
	 */
	public void setEditEnhancers(Map editEnhancers) {
		this.editEnhancers = editEnhancers;
	}

	/**
	 * A collection (Hashmap) edit enhancers depencies (ArrayList). 
	 * 
	 * @return the editEnhancersDependant
	 */
	public Map getEditEnhancersDependant() {
		return editEnhancersDependant;
	}

	/**
	 * @param editEnhancersDependant the editEnhancersDependant to set
	 */
	public void setEditEnhancersDependant(Map editEnhancersDependant) {
		this.editEnhancersDependant = editEnhancersDependant;
	}

	/**
     * The current persist status of this object.
     * 
     * <p>
     * Persist-status indicates what the most likely required 
     * database action is to get the database in line with the 
     * business object value.
     * </p>
     * 
     * @return Returns the persistStatus.
     */
    public zXType.persistStatus getPersistStatus() {
        return this.persistStatus;
    }

    /**
     * @see ZXBO#getPersistStatus()
     * @param pobjStatus The persistStatus to set.
     */
    public void setPersistStatus(zXType.persistStatus pobjStatus) {
        this.persistStatus = pobjStatus;
    }
    
    /**
     * A collection (Property) of the business object properies.
     * 
     * @return Returns the properties.
     */
    public Map getProperties() {
        return properties;
    }
    
    /**
     * @param properties The properties to set.
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

    /**
     * The last return code of the post persist action call. 
     * 
     * <p>
     * The zX object has a property postPersistLastRc. This property 
     * is of type zXType.rc and can thus have any of the values rcOk, 
     * rcWarning and rcError.
     * </p>
     * <p>
     * This property has been introduced to address an omission in the 
     * original design in the postPersist method.
     * </p>
     * <p>
     * Imagine a scenario where the pre- and post-persist routines 
     * are called as part of a delete operation. Assume that, for 
     * whatever reason, the delete fails. The post-persist routine 
     * will still be called.
     * </p>
     * <p>
     * It may be that different logic is required in post-persist 
     * for a successful delete than for an unsuccessful delete. The 
     * omission is that the return code of the actual operation (i.e. 
     * ‘delete’ in this example) is not passed as one of the parameters.
     * </p>
     * <p>
     * Adding this parameter would have resulted in a major 
     * rework operation so it was decided that the return code 
     * of the operation that triggered the post-persist to be 
     * called is stored in the postPersistLastRc property of zX.
     * </p>
     * 
     * <p>
     * NOTE : May refactor this later on.
     * </p>
     * 
     * @return Returns the lastPostPersistRC.
     */
    public zXType.rc getLastPostPersistRC() {
        if (this.lastPostPersistRC == null) {
            this.lastPostPersistRC = zXType.rc.rcOK;
        }
        
        return lastPostPersistRC;
    }
    
    /**
     * @see ZXBO#getLastPostPersistRC()
     * @param lastPostPersistRC The lastPostPersistRC to set.
     */
    public void setLastPostPersistRC(zXType.rc lastPostPersistRC) {
        this.lastPostPersistRC = lastPostPersistRC;
    }
    
    /**
     * Whether or not the validate this business object.
     * 
     * <p>
     * The property validate is used to indicate that validation is 
     * required when setting attributes. This value defaults to true.
     * </p>
     * 
     * <p>
     * The main reason to de-activate validation is for performance; 
     * attribute validation adds some overhead and when dealing with high 
     * volumes of data (e.g. a data migration), validation may be less of 
     * a concern than performance.
     * </p>
     * 
     * @return Returns the validate.
     */
    public boolean isValidate() {
        return this.validate;
    }

    /**
     * @param validate The validate to set.
     */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }
    
    /**
     * @return Returns the dSHandler.
     */
    public DSHandler getDSHandler() {
        return this.DSHandler;
    }
    
    /**
     * @param handler The dSHandler to set.
     */
    public void setDSHandler(DSHandler handler) {
        this.DSHandler = handler;
    }
    
    //------------------------ Public methods
    
    /**
     * Set a default value of property in a entity.
     * 
     * <pre>
     * 
     * Only call this when you are setting the defaults.
     * 
     * 
     * NOTE : This was previous part of ZXBOS and was called setAttr. As this
     * confused matters it has been changed.
     * NOTE :  This called : setValue(pstrAttribute, pobjValue, false)
     * </pre>
     * 
     * @param pstrAttribute The name of the Attribute to set the Property Value.
     * @param pobjValue The value to set it to.
     * @return Returns the return code of the method
     * @throws ZXException This is not thrown when zx.throwException is set to false.
     */
    public zXType.rc  setValue(String pstrAttribute, String pobjValue) throws ZXException {
        return setValue(pstrAttribute, pobjValue, false, false);
    }
    
    /**
     * Set a property of a entity.
     * 
     * <pre>
     * 
     * NOTE : This was previous part of ZXBOS and was called setAttr. As this
     * confused matters it has been changed.
     * </pre>
     * 
     * @param pstrAttribute The name of the Attribute to set the Property Value.
     * @param pobjValue The value to set it to.
     * @param pblnDefaultValue Whether you are setting the value to the property default. Optional, default is false
     * @return Returns the return code of the method
     * @throws ZXException This is not thrown when zx.throwException is set to false.
     */
    public zXType.rc setValue(String pstrAttribute, String pobjValue, boolean pblnDefaultValue) throws ZXException {
        return setValue(pstrAttribute, pobjValue, pblnDefaultValue, false);
    }
    
    /**
     * Set a property of a entity.
     * 
     * <pre>
     * 
     * NOTE : This was previous part of ZXBOS and was called setAttr. As this
     * confused matters it has been changed.
     * 
     * BD17JUN05 - V1.5:18 - Support for property persist status
     * </pre>
     * 
     * @param pstrAttribute The name of the Attribute to set the Property Value.
     * @param pstrValue The value to set it to.
     * @param pblnDefaultValue Whether you are setting the value to the property default. Optional, default is false
     * @param pblnIsNull Whether the property is set to a null value. Optional, default is false
     * @return Returns the return code of the method
     * @throws ZXException This is not thrown when zx.throwException is set to false.
     */
    public zXType.rc setValue(String pstrAttribute, 
    						  String pstrValue, 
    						  boolean pblnDefaultValue, 
    						  boolean pblnIsNull) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrAttribute", pstrAttribute);
            getZx().trace.traceParam("pobjValue", pstrValue);
        }
        
        zXType.rc setValue = zXType.rc.rcOK;
        
        try {
            /**
             * Get handle to property
             */
            Property objProperty = retrieveProperty(pstrAttribute);
            if (objProperty == null) { 
            	getZx().trace.addError("Unable to retrieve property", getDescriptor().getName() + "." + pstrAttribute);
            	setValue = zXType.rc.rcError;
            	return setValue;
            }
            
            /**
             * Are we sending through a special setValue with the default value eg:
             * #now,#date.#user etc..
             */
            if (pblnDefaultValue) {
                objProperty.setDefaultValue(pstrValue);
                
            } else {
                /**
                 * Do NOT set attribute value when there is a dynamic value
                 * the reason is that this value is determined at run-time in
                 * getAttr
                 */
                if (StringUtil.len(objProperty.getAttribute().getDynamicValue()) == 0){
                    /**
                     * We said it was NOT null, but the value we are using IS null.
                     */
                	if (!pblnIsNull) {
                    	pblnIsNull = (pstrValue == null);
                    }
                	
                    objProperty.setNull(pblnIsNull);
                    setValue = objProperty.setValue(pstrValue);
                    
                    /**
                     * When the property was set it may think that is
                     * it no long null.
                     */
                    objProperty.setNull(pblnIsNull);
                    
                    /**
                     * Value has now officialy been set
                     */
                    objProperty.setPersistStatus(zXType.propertyPersistStatus.ppsSet);
                }
                
            }
            
            return setValue;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Set a property of a entity", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrAttribute = " + pstrAttribute);
                getZx().log.error("Parameter : pobjValue = " + pstrValue);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            setValue = zXType.rc.rcError;
            return setValue;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Set a property of a entity.
     * 
     * <pre>
     * 
     * NOTE : This was previous part of ZXBOS and was called setAttr. As this
     * confused matters it has been changed.
     * </pre>
     * 
     * @param pstrAttribute The name of the Attribute to set the Property Value.
     * @param pobjValue The value to set it to.
     * @return Returns the return code of the method.
     * @throws ZXException This is not thrown when zx.throwException is set to false.
     */
    public zXType.rc setValue(String pstrAttribute, Property pobjValue) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrAttribute", pstrAttribute);
            getZx().trace.traceParam("pobjValue", pobjValue);
        }
        
        zXType.rc setValue = zXType.rc.rcOK;
        
        try {
            /**
             * Get handle to property
             */
            Property objProperty = retrieveProperty(pstrAttribute);
            if (objProperty == null) { 
            	getZx().trace.addError("Unable to retrieve property", getDescriptor().getName() + "." + pstrAttribute);
            	setValue = zXType.rc.rcError;
            	return setValue;
            }
            
            /**
             * Do NOT set attribute value when there is a dynamic value
             * the reason is that this value is determined at run-time in
             * getAttr.
             */
            if (StringUtil.len(objProperty.getAttribute().getDynamicValue()) == 0) {
            	setValue = objProperty.setValue(pobjValue);
            	
                /**
                 * Value has now officialy been set 
                 */
                objProperty.setPersistStatus(zXType.propertyPersistStatus.ppsSet);
            }
            
            return setValue;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Set a property of a entity", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrAttribute = " + pstrAttribute);
                getZx().log.error("Parameter : pobjValue = " + pobjValue);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
            setValue = zXType.rc.rcError;
            return setValue;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * getValue - Return property for a BO.
     * 
     * <pre>
     * 
     * NOTE : This was part of ZXBOS and was called getAttr.
     * NOTE : Property names at case-sensitive.
     * 
     * - BD17JUN05 - V1.5:18 - Support for ensureLoadGroup
     * - BD28DEC05 - V1.5:74 - Do not touch persistStatus for dynamic attributes
     * </pre>
     * 
     * @param pstrAttribute The name of the attribute to get the Property.
     * @return Returns the property of a entity by its Attribute name
     * @throws ZXException Thrown if getValue fails to get the Property.
     */
    public Property getValue(String pstrAttribute) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrAttribute", pstrAttribute);
        }
        
        Property getValue = null;
        ZXBO objOldzXMe = null;
        
        try {
	        /**
	         * Get handle to property
	         */
	        getValue = retrieveProperty(pstrAttribute);
	        if (getValue == null) { 
	            throw new ZXException("Unable to retrieve property : " + this.descriptor.getName() + "." + pstrAttribute);
	        }
	        
            if (StringUtil.len(getValue.getAttribute().getEnsureLoadGroup()) > 0) {
            	/**
            	 * At this stage we can only load when we have a PK otherwise
            	 * we have nothing to load by
            	 */
            	if (!getPKValue().isNull) {
            		/**
            		 * Try to load but don't be too harsh if not OK
            		 */
            		ensureGroupIsLoaded(getValue.getAttribute().getEnsureLoadGroup());
            	}
            }
            
            /**
             * In case of a dynamic value, we do not return the property
             * but instead evaluate the dynamic value.
             */
            if (StringUtil.len(getValue.getAttribute().getDynamicValue()) > 0) {
                /**
                 * The expression is very likely to refer to 'me' which is
                 * stored in the BO context; it could be that this or an event action
                 * is called recursively and so we have to save the original zXMe value
                 * and restore when done
                 */
                objOldzXMe = getZx().getBOContext().getBO("zXMe");
                
                /**
                 * And set me.
                 * 
                 * This variable can be used in the dynamic value expression.
                 */
                getZx().getBOContext().setEntry("zXMe", this);

                Property objExprResult = getZx().getExpressionHandler().eval(getValue.getAttribute().getDynamicValue());
                if (objExprResult == null) {
                	/**
                	 * We could not execute the expression.
                	 */
                    getZx().trace.addError("Unable to evaluate dynamic value for " + getValue.getAttribute().getName(), 
                    					   getValue.getAttribute().getDynamicValue());
                    return getValue;
                }
                
                /**
                 * Now we do not return objExprResult directly for a very important
                 * reason: there is a difference between an ordinary property and an
                 * ad-hoc property (i.e. zx.strValue or so) and that is that the former
                 * is associated with an attribute and thus has knowledge about formatting
                 * and all other attr stuff.
                 * 
                 * Note that we save and restore the persist status; we do not want this
                 * setValue call to set it to 'dirty'...
                 */
                zXType.persistStatus enmPersistStatus = getPersistStatus();
                getValue.setValue(objExprResult);
                setPersistStatus(enmPersistStatus);
                
            }
            
            return getValue;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Return property for a BO", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrAttribute = " + pstrAttribute);
            }
            if (getZx().throwException) throw new ZXException(e);
            return getValue;
            
        } finally {
            /**
             * Restore old zXMe again, when necessary.
             */
            if (objOldzXMe != null) {
                getZx().getBOContext().setEntry("zXMe", objOldzXMe);
            }
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getValue);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * getPKValue - Return porperty value for primary of the business object.
     * 
     * @return Returns the Primary Key property of the Business Object.
     * @throws ZXException Thrown if getPKValue fails
     */
    public Property getPKValue() throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        Property getPKValue = null;
        
        try {
            getPKValue = getValue(this.descriptor.getPrimaryKey());
            return getPKValue;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : getPKValue - Return value for PK ", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return getPKValue;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getPKValue);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * setPKValue - Set value for the Primary Key property.
     * 
     * @param pstrValue The value to set the primary key to
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if setPKValue fails
     */
    public zXType.rc setPKValue(String pstrValue) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrValue", pstrValue);
        }

        zXType.rc setPKValue = zXType.rc.rcOK;
        
        try {
            setPKValue = setValue(this.descriptor.getPrimaryKey(), pstrValue);
            return setPKValue;
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : setPKValue - Set value for the Primary Key property", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrValue = " + pstrValue);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
            return setPKValue;
            
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            	getZx().trace.returnValue(setPKValue);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * retrieveProperty - Retrieve property in a just-in-time way
     * 
     * @param pstrAttribute Name of the property to retrieve.
     * @return Returns a property from the collection of creates a new one.
     * @throws ZXException Thrown if method failed to retrieve property.
     */
    private Property retrieveProperty(String pstrAttribute) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrAttribute", pstrAttribute);
        }
        
        Property retrieveProperty = null;
        
        try {
            // NOTE : Lower casing for case insensetive
            pstrAttribute = pstrAttribute.toLowerCase();  
            
            /**
             * Try to get handle to property from previous set ones : 
             */
            try {
                
                if (pstrAttribute.equals("+")) {
                    retrieveProperty = (Property) this.properties.get(getDescriptor().getPrimaryKey());
                } else {
                    retrieveProperty = (Property) this.properties.get(pstrAttribute);
                }
                
            } catch (Exception e) {
                getZx().log.warn("This attribute does not have an initilise property " 
                                 + descriptor.getName() + "." + pstrAttribute);	
            }

            /**
             * If this property does not exist, create it (just in time
             * property creation)
             */
            if (retrieveProperty == null) {
                /**
                 * Select correct property
                 */
                int dataType = this.descriptor.getAttribute(pstrAttribute).getDataType().pos;
//                if(dataType == null) {
//                    throw new Exception("Failed to get attribute from the entity : " + descriptor.getName() + "." + pstrAttribute);
//                }
                
                if (dataType == zXType.dataType.dtString.pos || dataType == zXType.dataType.dtExpression.pos) {
                    retrieveProperty = new StringProperty();
                } else if (dataType == zXType.dataType.dtLong.pos || dataType == zXType.dataType.dtAutomatic.pos) {
                    retrieveProperty = new LongProperty();
                    
                } else if (dataType == zXType.dataType.dtDouble.pos) {
                    retrieveProperty = new DoubleProperty();
                    
                } else if (dataType == zXType.dataType.dtDate.pos || dataType == zXType.dataType.dtTime.pos || dataType == zXType.dataType.dtTimestamp.pos) {
                    retrieveProperty = new DateProperty();
                    
                } else if (dataType == zXType.dataType.dtBoolean.pos) {
                    retrieveProperty = new BooleanProperty();
                    
                } else {
                    throw new Exception("Unhandled dataType : " + this.descriptor.getAttribute(pstrAttribute).getDataType());
                }
                
                retrieveProperty.init(this, pstrAttribute);
                
                /**
                 * And add to collection of properties
                 */
                this.properties.put(pstrAttribute, retrieveProperty);
            }

            /**
             * Fundamental error : Could not load a property.
             */
            if (retrieveProperty == null) throw new Exception("Failed to create a Property for " + pstrAttribute);

            return retrieveProperty;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Retrieve property in a just-in-time way", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrAttribute" + pstrAttribute);
            }
            
            if (getZx().throwException) throw new ZXException("Failed to : Retrieve property in a just-in-time way", e);
            return retrieveProperty;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(retrieveProperty);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * formattedString - Formatted string of attributed.
     * 
     * @param pstrGroup The AttributeGroup to get the formatted string of
     * @return Returns a formatted string of the attributegroup
     * @throws ZXException Thrown if formattedString fails
     */
    public String formattedString(String pstrGroup) throws ZXException {
        return formattedString(pstrGroup,true, " ");
    }
    
    /**
     * formattedString - Formatted string of attributed.
     * 
     * Return formatted string with attribute values concatenated and separated
     * by an optional seperation string
     * 
     * @param pstrGroup The attribute group get set.
     * @param pblnTrim Whether to trim the string or not. Optional, default is false
     * @param pstrSeperator The seperator. Optional, default is " "
     * @return Returns a formatted of the selected attributegroup
     * @throws ZXException
     */
    public String formattedString(String pstrGroup, 
    							  boolean pblnTrim, 
    							  String pstrSeperator) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pblnTrim", pblnTrim);
            getZx().trace.traceParam("pstrSeperator", pstrSeperator);
        }

        StringBuffer formattedString = new StringBuffer();
        
        /**
         * Set defaults
         */
        if (pstrSeperator == null) {
            pstrSeperator = " ";
        }
        
        try {
            
            /**
             * Get handle to group
             */
            AttributeCollection colGroup = this.descriptor.getGroup(pstrGroup);
            if (colGroup == null) { throw new Exception("Group not found :" + pstrGroup); }

            Iterator iter = colGroup.iterator();
            while (iter.hasNext()) {
                formattedString.append(getValue(((Attribute)iter.next()).getName()).formattedValue(pblnTrim));
                formattedString.append(pstrSeperator);
            }
            
            return formattedString.toString().trim();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : formattedString - Formatted string of attributed ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pblnTrim = " + pblnTrim);
                getZx().log.error("Parameter : pstrSeperator = " + pstrSeperator);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return formattedString.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(formattedString);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * nameValuePairs - Dump object as name / value pairs.
     * 
     * @param pstrGroup The attribute group you want name value pairs.
     * @return Returns a name value/pair formatted
     * @throws ZXException Thrown if nameValuePairs fails
     */
    public String nameValuePairs(String pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        StringBuffer nameValuePairs = new StringBuffer(64);
        
        try {
            
            /**
             * Get handle to group
             */
            AttributeCollection colGroup = this.descriptor.getGroup(pstrGroup);
            if (colGroup == null) { throw new Exception("Group not found :" + pstrGroup); }

            Iterator iter = colGroup.iterator();
            Attribute objAttr;
            while (iter.hasNext()) {
                objAttr = (Attribute) iter.next();
                
                nameValuePairs.append(objAttr.getLabel().getLabel());
                nameValuePairs.append('\t'); // Inserting a tab
                nameValuePairs.append(getValue(objAttr.getName()).formattedValue());
                nameValuePairs.append("\n\r");
                
            }

            return nameValuePairs.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : nameValuePairs - Dump object as name / value pairs", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return nameValuePairs.toString();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(nameValuePairs);
                getZx().trace.exitMethod();
            }
        }
    }
    
	/**
	 * Load a BO instance from the database.
	 * 
	 * NOTE : This calls loadBO("*","+",false); 
	 * 
	 * @see ZXBO#loadBO(String, String, boolean)
	 * @return Returns whether the loadBO was successfull of not.
	 * @throws ZXException Thrown if loadBO fails
	 */
	public zXType.rc loadBO() throws ZXException {
	    return loadBO("*", "+", false);
	}

	/**
	 * Load a BO instance from the database.
	 * 
	 * NOTE : This calls loadBO(pstrGroup, "+", false); 
	 * 
     * @param pstrGroup Group to load (defaults to all).
	 * @see ZXBO#loadBO(String, String, boolean)
	 * @return Returns whether the loadBO was successfull of not.
	 * @throws ZXException Thrown if loadBO fails
	 */
	public zXType.rc loadBO(String pstrGroup) throws ZXException {
	    return loadBO(pstrGroup, "+", false);
	}
    
    /**
     * Load a BO instance from the database.
     * 
     * @param pstrGroup Group to load (defaults to all). Optional default is "*"
     * @param pstrWhereGroup Group to use to generate where clause (defaults to primary
     *                 key). Optional, default is "+"
     * @param pblnResolveFK Resolve foreign keys yes / no. Optional, default is false.
     * @return Returns whether the loadBO was successfull of not.
     * @throws ZXException Thrown if loadBO fails.
     */
	public zXType.rc loadBO(String pstrGroup, String pstrWhereGroup, boolean pblnResolveFK) throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
        }
	    
		zXType.rc loadBO = zXType.rc.rcOK;
	    
	    DSHandler objDSHandler;
        
	    /**
	     * Set defaults
	     */
	    if (pstrGroup == null) {
	        pstrGroup = "*";
	    }
	    if (pstrWhereGroup == null) {
	        pstrWhereGroup = "+";
	    }
	    
        try {
            
            /**
             * Get the handler associated with this datasource
             */
            objDSHandler = getDS();
            
            /**
             * Does channel support load?
             */
            if (!objDSHandler.isSupportsLoad()) {
                throw new ZXException("Date-source handler does not support load", objDSHandler.getName());
            }
            
            /**
             * Prepersist
             */
            if (prePersist(zXType.persistAction.paProcess, pstrGroup, pstrWhereGroup, null).pos != zXType.rc.rcOK.pos) {
                // GoTo errExit
                // Do not do any exception handling.
                loadBO = zXType.rc.rcError;
                return loadBO;
            }
            
            /**
             * Let the DSHandler load the business object.
             */
            loadBO = objDSHandler.loadBO(this, pstrGroup, pstrWhereGroup, pblnResolveFK);
            
            if(loadBO.pos == zXType.rc.rcError.pos) {
                throw new ZXException("Error during loadBO for data source", objDSHandler.getName());
            }
            
            /**
             * Postpersist
             */
            if (postPersist(zXType.persistAction.paProcess, pstrGroup, pstrWhereGroup, null).pos != zXType.rc.rcOK.pos) {
                throw new ZXException();
            }
            
            /**
             * Reset the persist status to clean :
             */
            this.persistStatus = zXType.persistStatus.psClean;
            
            return loadBO;
        } catch (Exception e) {
            getZx().trace.addError("Load a BO instance from the database", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
                getZx().log.error("Parameter : pblnResolveFK = " + pblnResolveFK);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            loadBO = zXType.rc.rcError;
            return loadBO;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(loadBO);
                getZx().trace.exitMethod();
            }
        }
	}
    
    /**
     * Load a BO instance from a line record.
     * 
     * <pre>
     * DGS17DEC2003 New function.
     * 
     * Your business object should not be mapped to a table eg : "-"
     * And your columns are actually the postion in the line to start reading and the length is the end postion.
     * 
     * NOTE : Should should be moved into ZXBOS maybe?
     *</pre>
     *
     * @param pstrRecord A specially formatted string representing a record.
     * @param pstrLoadGroup Group to load (defaults to "*") 
     * @param pblnResolveFK Where to resolve the foriegn keys. This is not yet implemented. Optional, default should be false.
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if loadBORecord fails. 
     * @since C1.4 Implemented 17DEC2003
     */
    public zXType.rc loadBORecord(String pstrRecord, 
                                  String pstrLoadGroup, 
                                  boolean pblnResolveFK) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrRecord", pstrRecord);
            getZx().trace.traceParam("pstrLoadGroup", pstrLoadGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
        }
        
        zXType.rc loadBORecord = zXType.rc.rcOK;
        
        /**
         * Set defaults
         */
        if (pstrLoadGroup == null) {
            pstrLoadGroup = "*";
        }
        
        try {
            /**
             * Prepersist
             */
            prePersist(zXType.persistAction.paProcess, pstrLoadGroup);
            
            /**
             * And copy fields
             */
            ZXCollection colAttr = getDescriptor().getGroup(pstrLoadGroup);
            if (colAttr == null) {
                throw new Exception("Attribute group not found : " + pstrLoadGroup);
            }
            
            Attribute objAttr;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if (!StringUtil.isNumeric(objAttr.getColumn())) {
                    throw new ZXException("Attribute must have numeric position as column.", objAttr.getName());
                }
                
                int intColumn = new Integer(objAttr.getColumn()).intValue();
                if (StringUtil.len(pstrRecord) >= intColumn + objAttr.getLength()) {
                    /**
                     * Only do this if the record is long enough to actually have data in that
                     * column position
                     */
                    String strTmp = pstrRecord.substring(intColumn, intColumn+objAttr.getLength());
                    setValue(objAttr.getName(), strTmp);
                    
                } // Is the line long enough
                
            }
            
            /**
             * Postpersist
             */
            loadBORecord = postPersist(zXType.persistAction.paProcess, pstrLoadGroup);
            
            if (loadBORecord.pos != zXType.rc.rcOK.pos) {
                // GoTo errExit
                throw new ZXException("Failed in postPersist");
            }
            
            return loadBORecord;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Load a BO instance from a record.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrRecord = "+ pstrRecord);
                getZx().log.error("Parameter : pstrLoadGroup = "+ pstrLoadGroup);
                getZx().log.error("Parameter : pblnResolveFK = "+ pblnResolveFK);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            loadBORecord = zXType.rc.rcError;
            return loadBORecord;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(loadBORecord);
                getZx().trace.exitMethod();
            }
        }
    }

	/**
	 * Delete BO .
	 * 
	 * NOTE : This is a wrapper for deleteBO("+")
	 * 
	 * @see ZXBO#deleteBO(String)
	 * @return Return code of the method
	 * @throws ZXException Thrown if deleteBO fails
	 */
	public zXType.rc deleteBO() throws ZXException {
	    return deleteBO("+");
	}
	
	/**
     * Delete BO.
     * 
     * @param pstrWhereGroup Where group to delete by. Optional, default should be "+"
     * @return Returns sucess or failure. It can also report a warning if nothing is deleted
     * @throws ZXException Thrown if deleteBO fails
	 */
	public zXType.rc deleteBO(String pstrWhereGroup) throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrWhereAttributeGroup", pstrWhereGroup);
        }
	    
	    zXType.rc deleteBO = zXType.rc.rcOK;
        DSHandler objDSHandler;
        
        try {
            
            /**
             * Is user allowed to delete?
             */
            if (!mayDelete()) {
                throw new Exception("User credentials do not allow you to delete this object : " + getDescriptor().getName());
            }
            
            objDSHandler = getDS();
            
            /**
             * Does data source support deletes?
             */
            if (!objDSHandler.isSupportsDelete()) {
                getZx().trace.addError("Data-source handler does not support delete", objDSHandler.getName());
                deleteBO = zXType.rc.rcError;
                return deleteBO;
            }
            
            /**
             * Is delete at all allowed?
             */
            if (getDescriptor().getDeleteRule().equals(zXType.deleteRule.drNotAllowed)) {
                /**
                 * Simply not allowed
                 */
                getZx().trace.userErrorAdd("Delete rule is set to not-allowed for this entity (" 
						   				   + getDescriptor().getLabel().getLabel() 
						   				   + ")");
                deleteBO = zXType.rc.rcError;
                return deleteBO;
            }
            
            /**
             * Prepersist
             */
            if (prePersist(zXType.persistAction.paDelete, "", pstrWhereGroup).pos != zXType.rc.rcOK.pos) {
                // GoTo errExit
                throw new ZXException();
            }
            
            /**
             * Let the DSHandler do the delete.
             */
            deleteBO = objDSHandler.deleteBO(this, pstrWhereGroup);
            if (deleteBO.pos == zXType.rc.rcError.pos) {
            	/**
            	 * This may not be an actual error. It may be that the user
            	 * is not allowed to delete this entity.
            	 */
                getZx().trace.addError("Delete by data-source handler failed for " + getDescriptor().getName(), objDSHandler.getName());
                return deleteBO;
            }
            
            /**
             * Postpersist
             */
            if (postPersist(zXType.persistAction.paDelete, "", pstrWhereGroup).pos != zXType.rc.rcOK.pos) {
            	// GoTo errExit
                throw new ZXException(); 
            }
            
            /**
             * Clean the persistStatus
             */
            setPersistStatus(zXType.persistStatus.psClean);
            
            return deleteBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Delete BO", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrWhereAttributeGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
            return deleteBO;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(deleteBO);
                getZx().trace.exitMethod();
            }
        }
	}
	
    /**
     * Insert a business object in the database.
     * 
     * NOTE : The callee has to handle any exceptions thrown.
     * 
     * @return Returns the rc of the method
     * @throws ZXException Thrown if insertBO fails
     */
	public zXType.rc insertBO() throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }

	    zXType.rc insertBO = zXType.rc.rcOK;
	    DSHandler objDSHandler;
        
        try {
            
            /**
             * Is user allowed to insert?
             */
            if(!mayInsert()) {
                throw new Exception("User credentials does not allow you to insert this object : " 
                                    + getDescriptor().getName() 
                                    + " - " + getDescriptor().getLabel().getLabel());
            }
            
            objDSHandler = getDS();
            
            /**
             * Does channel support insert?
             */
            if(!objDSHandler.isSupportsInsert()) {
                throw new ZXException("Date-source handler does not support insert", objDSHandler.getName());
            }
            
            /**
             * Prepersist
             */
            if (prePersist(zXType.persistAction.paInsert, "").pos != zXType.rc.rcOK.pos) {
                throw new ZXException();
            }
            
            /**
             * Let DSHandler do the insert.
             */
            if (objDSHandler.insertBO(this).pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Handler was not able to insert BO", objDSHandler.getName());
            }
            
            /**
             * Postpersist - This can fail
             */
            if (postPersist(zXType.persistAction.paInsert, "").pos != zXType.rc.rcOK.pos) {
                throw new ZXException(); // Was GoTo errExit
            }
            
            /**
             * And set persist status to clean
             */
            setPersistStatus(zXType.persistStatus.psClean);
            
            return insertBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Insert a business object in the database", e);
            
            if (getZx().throwException) { throw new ZXException(e); }
            insertBO = zXType.rc.rcError;
            return insertBO;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(insertBO);
                getZx().trace.exitMethod();
            }
        }
	}
	
	/**
	 * Update a business object.
	 * 
	 * NOTE : This just called : updateBO("*","+");
	 * 
	 * @return Returns success or failure 
	 * @throws ZXException Thrown if updateBO fails 
	 */
	public zXType.rc updateBO() throws ZXException {
	    return updateBO("*","+");
	}
	
	/**
	 * Update a business object.
	 * 
	 * NOTE : This just called : updateBO(pstrGroup,"+");
	 * 
	 * @param pstrGroup Attribute Group to update
	 * @return Return success/failure of warning.
	 * @throws ZXException Thrown if updateBO fails
	 */
	public zXType.rc updateBO(String pstrGroup) throws ZXException{
	    return updateBO(pstrGroup, "+");
	}
	
	/**
	 * Update a business object.
	 * 
	 * <pre>
	 * 
	 * Reviewed for V1.5:45
	 * </pre>
	 * 
	 * @param pstrGroup Group to update. Optional, defaults to "*"
	 * @param pstrWhereGroup Key to use. Optional, defaults to "+"
	 * @return Returns the return code of the update.
	 * @throws ZXException Thrown when updateBO fails.
	 */
	public zXType.rc updateBO(String pstrGroup, String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }

	    zXType.rc updateBO = zXType.rc.rcOK;
	    DSHandler objDSHandler;
	    
        try {
            /**
             * Is user allowed to update this business object
             */
            if (!mayUpdate()) {
                throw new ZXException("User credentials do not allow you to update this object.",
                					  getDescriptor().getLabel().getLabel());
            }
            
            objDSHandler = getDS();
            
            /**
             * Does channel support update?
             */
            if (!objDSHandler.isSupportsUpdate()) {
                throw new ZXException("Date-source handler does not support update", objDSHandler.getName());
            }
            
            /**
             * Prepersist
             */
            if (prePersist(zXType.persistAction.paUpdate, pstrGroup, pstrWhereGroup).pos != zXType.rc.rcOK.pos) {
                // GoTo errExit
                throw new ZXException();
            }
            
            zXType.rc enmRC = objDSHandler.updateBO(this, pstrGroup, pstrWhereGroup);
            if (enmRC.pos == zXType.rc.rcWarning.pos) {
				getZx().setPostPersistLastRc(enmRC);
		        updateBO = enmRC;
		        
            } else if (enmRC.pos == zXType.rc.rcError.pos) {
                throw new ZXException("Update by data-source handler failed", objDSHandler.getName());
                
			} // updateBO call
            
            /**
             * Postpersist
             */
            if (postPersist(zXType.persistAction.paUpdate, pstrGroup, pstrWhereGroup).pos != zXType.rc.rcOK.pos) {
                throw new ZXException(); // GoTo errExit
            }
            
            /**
             * Mark as clean as we have now successfully updated the business object
             */
            setPersistStatus(zXType.persistStatus.psClean);
            
            return updateBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Update a business object", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            if (getZx().throwException) throw new ZXException(e);
            return updateBO;
            
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(updateBO);
                getZx().trace.exitMethod();
            }
        } 
	}
	
	/**
	 *  Do database action that is indicated by the persist status of the BO.
	 * 
	 * @param pstrGroup The attribute group to affect.
	 * @return Returns the return code of the method
	 * @throws ZXException Thrown if persistBO fails
	 */
	public zXType.rc persistBO(String pstrGroup) throws ZXException {
	    return persistBO(pstrGroup, "+");
	}

	/***
	 *  Do database action that is indicated by the persist status of the BO.
	 * 
	 * @param pstrGroup The attribute group to affect. Optional, default is "*"
	 * @param pstrWhereAttributeGroup The key to use. Optional, default is "+"
	 * @return Returns whether the action was successful.  
	 * @throws ZXException Thrown if persistBO
	 */
	public zXType.rc persistBO(String pstrGroup, String pstrWhereAttributeGroup) throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("pstrGroup", pstrGroup);
	        getZx().trace.traceParam("pstrWhereAttributeGroup", pstrWhereAttributeGroup);
	    }
	
	    zXType.rc persistBO = zXType.rc.rcOK;
		   
	    try {
	        
	        if (getPersistStatus().equals(zXType.persistStatus.psClean)) {
	            /**
	             * No action required.
	             */
	        } else if (getPersistStatus().equals(zXType.persistStatus.psDeleted)) {
	            persistBO = deleteBO(pstrWhereAttributeGroup);
	        } else if (getPersistStatus().equals(zXType.persistStatus.psDirty)) {
	            persistBO = updateBO(pstrGroup, pstrWhereAttributeGroup);
	        } else if (getPersistStatus().equals(zXType.persistStatus.psNew)) {
	            persistBO = insertBO();
	        }
	        
	        return persistBO;
	    } catch (Exception e) {
            getZx().trace.addError("Failed to : Do database action that is indicated by the persist status of the BO ", e);
	        if (getZx().log.isErrorEnabled()) {
	            getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
	            getZx().log.error("Parameter : pstrWhereAttributeGroup = " + pstrWhereAttributeGroup);
	        }
	        
	        persistBO = zXType.rc.rcError;
	        if (getZx().throwException) throw new ZXException(e);
	        return persistBO;
	    } finally {
	        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	            getZx().trace.returnValue(persistBO);
	            getZx().trace.exitMethod();
	        }
	    } 
	}
    
    /**
     * Copy values from recordset to business object. Optionally resolving FK.
     * 
     * <pre>
     * 
     * NOTE : This calls rs2obj(pobjRS, pstrGroup, pblnResolveFK, pblnPureNames, 1);
     * Where objRS2Obj has the level and colNo set to 1.
     * </pre>
     * 
     * @param pobjRS The resultset used to populate the ZXBO
     * @param pstrGroup The attribute group of attributes you want to populate
     * @param pblnResolveFK Whether to resolve the foriegn key attributes
     *            Optional, default is false
     * @param pblnPureNames Whether to use pure names or not. Optional, default is false          
     * @return Returns the return code of the function.           
     * @see #rs2obj(ZXResultSet, String, boolean, boolean, int)
     * @throws ZXException Thrown if populate fails
     */
    public zXType.rc rs2obj(ZXResultSet pobjRS,
                            String pstrGroup, 
                            boolean pblnResolveFK, 
                            boolean pblnPureNames) throws ZXException {
        return rs2obj(pobjRS, pstrGroup, pblnResolveFK, pblnPureNames, 1);
    }
    
    /**
     * Copy values from recordset to business object. Optionally resolving FK.
     * 
     * <pre>
     * 
     * NOTE : This calls rs2obj(pobjRS, pstrGroup, pblnResolveFK, false, pintLevel);
     * </pre>
     * 
     * @param pobjRS The resultset used to populate the ZXBO
     * @param pstrGroup The attribute group of attributes you want to populate
     * @param pblnResolveFK Whether to resolve the foriegn key attributes. Optional, default is false
     * @param pintLevel Optional, default is 1. Used to check whether recursive call is at work
     * @return Returns the return code of the function.           
     * @see #rs2obj(ZXResultSet, String, boolean, boolean, int)
     * @throws ZXException Thrown if populate fails
     */
    private zXType.rc rs2obj(ZXResultSet pobjRS,
                     String pstrGroup, 
                     boolean pblnResolveFK, 
                     int pintLevel) throws ZXException {
        return rs2obj(pobjRS, pstrGroup, pblnResolveFK, false, pintLevel);
    }
    
	/**
     * Copy values from recordset to business object. Optionally resolving FK.
     * 
     * <pre>
     * 
     * DGS15APR2003: New option to use 'Pure' attr names here. Used in
     * DeleteBO.
     * 
     * NOTE: This routine should no longer be used directly; use the rs2bo method of the
     * clsDSRS class instead
     * 
     * BD17JUN05 - V1.5:18 - Added support for propertyPersistStatus
     * BD30JUN05 - V1.5:20 - Added support for enhanced FK Label behaviour
     * BD1JUN06 - V1.5:96 - Logic to reset property persist statuses moved to clsDSRS
     * </pre>
     * 
     * @param pobjRS The resultset used to populate the ZXBO
     * @param pstrGroup The attribute group of attributes you want to populate
     * @param pblnResolveFK Whether to resolve the foriegn key attributes Optional, default is false
     * @param pintLevel Optional, default is 1. Used to check whether recursive call is at work
     * @param pblnPureNames Whether to use pure names or not. Optional, default is false
	 * @return Returns the return code of the function.
     * @throws ZXException Thrown if populate.
     */
    private zXType.rc rs2obj(ZXResultSet pobjRS,
                             String pstrGroup,
                             boolean pblnResolveFK,
                             boolean pblnPureNames,
                             int pintLevel) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRS",pobjRS);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
            getZx().trace.traceParam("pintLevel", pintLevel);
            getZx().trace.traceParam("pblnPureNames", pblnPureNames);
        }
        
        zXType.rc rs2obj = zXType.rc.rcOK;
        
        /**
         * Save the original validation setting.
         * 
         * We do not do validation when doing a rs2obj to boost performance a bit
         */
        boolean blnOrgValidate = isValidate();
        
        try {
            /**
             * Prepersist (cannot cause rs2obj to fail)
             */
            if (prePersist(zXType.persistAction.paRs2obj, pstrGroup).pos != zXType.rc.rcOK.pos) {
                rs2obj = zXType.rc.rcWarning;
            }
            
            ZXBO objFK;
            Attribute objAttr;                      // The attribute for the column 
            Property objProperty;                   // The value of a column as a ZX property
            String strColumn;                       // The name of the column
            String strLabelGroup;
            
            /**
             * Check whether we are at the end of the records
             * 
             * If the resultset type is not seekable we cannot check whether be are past the last row.
             */
            if (pobjRS.getTarget().isAfterLast()) {
                if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                    getZx().trace.trace("EOF on rs2obj");
                }
                /**
                 * BD9JUN04 I Guess this should really be okExit as in
                 * errExit we set the return code to rcError
                 */
                throw new Exception("Failed to populate BO");
                
            }
            
            /**
             * First we have to set all properties to 'new' that are currently 'loaded'; this
             * is to overcome some weird scenario as the following. Imagine we have
             * 5 attributes (A, B, C, D, E) where E is a dynamic value that requires
             * D to be loaded. The developer loops over a recordset (and thus calls
             * rs2obj for each row) but only loads A, B and C but does want to show
             * E on ths screen. The ensureLoad logic that is called when trying to get
             * the value of E forces D to be loaded. However, the next time around (ie
             * next call to rs2obj) A, B and C and re-loaded and D still has the persist status
             * of loaded although it has the value of the previous record!
             */
            //-------------
            // BD1JUN06
            // TODO : MOVED TO CLSDSRS; CAN BE REMOVED AS SOON AS PROVEN OK
            //setPropertyPersistStatus("*", zXType.propertyPersistStatus.ppsNew, zXType.propertyPersistStatus.ppsLoaded);
            //-------------
            
            /**
             * We do not do validation when doing a rs2obj to boost performance a bit
             * Why? Because we assume that the values in the database is
             * correct.
             */
			setValidate(false);
            
            /**
             * Get attribute group. For auditable BOs and not being called
             * recursively, add the audit columns
             * 
             * DGS27JUL2004: Don't return null group if the only attr we were originally asking for was
             * the ~, or we will not even get that (so added the 'And pstrGroup <> "~"' clause below).
             */
            AttributeCollection colAttr = null;
            if(descriptor.isConcurrencyControl() && pintLevel == 1 && !pstrGroup.equals("~")) {
                colAttr = descriptor.getGroup(pstrGroup + ",~");
                
                /**
                 * Bit daft but we may have called this routine with a valid empty
                 * group that we now have added the ~ to (e.g. null
                 * so if we now have 1 attribute we pretend that nothing happened
                 */
                if (colAttr.size() == 1)  { 
                    colAttr = getDescriptor().getGroup("null");
                }
            } else {
                colAttr = descriptor.getGroup(pstrGroup);
            }
            
            if(colAttr == null) {
                throw new ZXException("Attribute group not found : " + pstrGroup);
            }
            
            zXType.sqlObjectName enmSQLObjectName = pblnPureNames ? zXType.sqlObjectName.sonPureName : zXType.sqlObjectName.sonRSName;
            int intRC;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute) iter.next();
                
                /**
                 * Skip attributes that are not related to a column
                 */
                if (StringUtil.len(objAttr.getColumn()) > 0) {
                    /**
                     * DGS15APR2003: Use pure names if asked. DeleteBO does.
                     */
                    strColumn = getZx().sql.columnName(this, objAttr, enmSQLObjectName);
                    
                    // Get name by position
                    intRC = pobjRS.setPropertyFromDB(this, objAttr, strColumn).pos;
                    
                    // Get the property value from the database.
                    objProperty = getValue(objAttr.getName());
                    if (objProperty == null || intRC != zXType.rc.rcOK.pos) {
                        getZx().trace.addError("Failed to load property.", objAttr.getName());
                        rs2obj = zXType.rc.rcError;
                        return rs2obj;
                    }
                    
                    /**
                     * We have now officially loaded the property from the database
                     * (well, at least we have tried to) so set property persist status
                     * accordingly
                     */
                    getValue(objAttr.getName()).setPersistStatus(zXType.propertyPersistStatus.ppsLoaded);
                    
                    /**
                     * Now for the difficult bit where we have a foreign key
                     *  Do not bother to resolve FK if:
                     *  - no resolve fk has been requested
                     *  - there is no foreign key for the attribute at hand
                     *  - the foreign key is null
                     */
                     if (pblnResolveFK) {
                        /**
                         * Split expression out for performance reasons (VB does not do
                         * expression short cutting)
                         */
                         if (StringUtil.len(objAttr.getForeignKey()) > 0) {
                             if (objProperty != null && !objProperty.isNull) {
                                 objFK = objProperty.getFKBO();
                                 if (objFK != null) {
                                    /**
                                     * It may be that the FK BO was not included in the query for this rs
                                     * because the BO and FK BO could not be joined as a result of different
                                     * data sources. In this case, we resolve 'manually'.
                                     * Can be the 'label' group or a label group override
                                     */
                                    if (getZx().getDataSources().canDoDBJoin(this, objFK)) {
                                    	
                                    	if (StringUtil.len(objAttr.getFkLabelGroup()) > 0) {
                                    		strLabelGroup = objAttr.getFkLabelGroup();
                                    	} else {
                                    		strLabelGroup = "label";
                                    	}
                                    	
                                        if (objFK.rs2obj(pobjRS, strLabelGroup, true, pintLevel + 1).pos != zXType.rc.rcOK.pos) {
                                        	getZx().trace.addError("Failed to load foreign key value", 
                                            					  objAttr.getName() + " / " + strLabelGroup);
                                        	/**
                                        	 * This is VERY obscure; in a complex pageflow the
                                        	 * query with resolveFK and the list (from where
                                        	 * the rs2obj is done) can be miles apart; in a
                                        	 * query with resolveFK it is possible that aliases
                                        	 * are automatically generated for tables and attributes;
                                        	 * this is to deal with complex situations. If the
                                        	 * query and list action are linked, the list action will
                                        	 * use same descriptors and thus have these dynamic aliases
                                        	 * set; if they are not linked, this may not be the case and
                                        	 * the rs2obj thus fails. Instead of bombing out we now
                                        	 * do the resolve the hard way; not very efficient but
                                        	 * at least better result
                                        	 */
                                        	objProperty.resolveFKLabel(false);
                                        }
                                        
                                        if (objProperty.resolveFKLabel(true).pos != zXType.rc.rcOK.pos) {
                                        	getZx().trace.addError("Unable to resolve FK label for attribute", objAttr.getName());
                                        	/**
                                        	 * Is not fatal!
                                        	 */
                                        }
                                        
                                        // objProperty.setFkLabel(objFK.formattedString("label"));
                                        
                                    } else {
                                        resolveLabels(objAttr.getName());
                                        
                                    } // Can BO and FK Bo be joined?
                                    
                                 } else {
                                    // Error : should be be able to load fk entity.
                                    throw new ZXException("Unable to create object for foreign key", objAttr.getForeignKey());
                                 }
                                 
                             } // Null
                             
                         }
                         
                     } // no FK attribute
                     
                } // No column
            }
            
            /**
             * Postpersist (cannot cause rs2obj to fail)
             */
            try {
                if (postPersist(zXType.persistAction.paRs2obj, pstrGroup).pos != zXType.rc.rcOK.pos) {
                    rs2obj = zXType.rc.rcWarning;
                }
            } catch (Exception e) {
                getZx().log.error("Failed to perform postpersist.", e);
                rs2obj = zXType.rc.rcWarning;
            }
            
            /**
             * Set persist status to clean
             */
            setPersistStatus(zXType.persistStatus.psClean);
            
            return rs2obj;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Copy values from recordset to business object.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRS = " + pobjRS);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pblnResolveFK = " + pblnResolveFK);
                getZx().log.error("Parameter : pintLevel = " + pintLevel);
                getZx().log.error("Parameter : pblnPureNames = " + pblnPureNames);
            }
            rs2obj = zXType.rc.rcError;
            if (getZx().throwException) throw new ZXException(e);
            return rs2obj;
        } finally {
            /**
             * Restore original validation
             */
            setValidate(blnOrgValidate);            

            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * resetBO - Reset all of the attributes to their original values.
     * 
     * <pre>
     * 
     * NOTE : This calls resetBO("*",  false, true); and therefore resets all of the attributes
     * </pre>
     * 
     * @see ZXBO#resetBO(String, boolean)
     * @throws ZXException Thrown if resetBO fails
     */
    public void resetBO() throws ZXException {
        resetBO("*", false,true);
    }    
    
    /**
     * resetBO - Reset all of the attributes to their original values.
     * 
     * NOTE : This calls resetBO(pstrGroup,  false, true); and therefore resets all of the attributes
     * 
     * @param pstrGroup Which attributeGroup to reset
     *            					Optional, defaults to 
     * @see ZXBO#resetBO(String, boolean)
     * @throws ZXException Thrown if resetBO fails
     */
    public void resetBO(String pstrGroup) throws ZXException {
        resetBO(pstrGroup, false,true);
    }    
    
    /**
     * resetBO - Reset group of attributes to their original values.
     * 
     * <pre>
     * 
     * NOTE : This calls : resetBO(pstrGroup, pblAssignAutomatics, true);
     * </pre>
     * 
     * @param pstrGroup Which attributeGroup to reset
     *            Optional, defaults to *
     * @param pblAssignAutomatics Whether to set automatics or not.
     *            Optional, default action should be false
     * @throws ZXException Thrown if resetBO fails
     */
    public void resetBO(String pstrGroup, boolean pblAssignAutomatics) throws ZXException {
        resetBO(pstrGroup, pblAssignAutomatics, true);
    }
    
    /**
     * resetBO - Reset group of attributes to their original values.
     * 
     * <pre>
     * 
     * DGS09MAR2004: This function now calls a new public method that does basically the same
     * job. That new method has a parameter that can be set to do things slightly differently
     * when called directly (see resetExplicitBO below).
     * 
     * This IS like resetExplicitBO in VB.
     * 
     * BD17JUN05 - V1.5:18 - Support for property persist status
     * </pre>
     * 
     * @param pstrGroup Which attributeGroup to reset
     *            		Optional, defaults to *
     * @param pblnAssignAutomatics Whether to set automatics or not.
     *            				   Optional, default action should be false
     * @param pblnExplicitsOnly Only set explicit default values
     * 		 				    Optional, default is true
     * @throws ZXException Thrown if resetBO fails
     */
    public void resetBO(String pstrGroup, boolean pblnAssignAutomatics, boolean pblnExplicitsOnly) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pblAssignAutomatics", pblnAssignAutomatics);
            getZx().trace.traceParam("pblnExplicitsOnly", pblnExplicitsOnly);
        }
        
        /**
         * Set defaults
         */
        if(StringUtil.isEmpty(pstrGroup)) {
            pstrGroup = "*";
        }

        /**
         * Save current settings
         */
        boolean blnOrgValidate = isValidate();
        
        try {
            /**
             * DGS04FEB2004: Lock attributes in the data constants group
             */
            if(StringUtil.len(getDescriptor().getDataConstantGroup()) > 0) {
                AttributeCollection colAttr = getDescriptor().getGroup(getDescriptor().getDataConstantGroup());
                if (colAttr == null) {
                    throw new Exception("Data constants attribute group not found : " + getDescriptor().getDataConstantGroup());
                }
                Iterator iter = colAttr.iterator();
                while(iter.hasNext()) {
                    ((Attribute)iter.next()).setLocked(true);
                }
            }
            
            /**
             * Get handle to group
             */
            AttributeCollection colGroup = this.descriptor.getGroup(pstrGroup);
            if (colGroup == null) { throw new Exception("Attribute group not found :" + pstrGroup); }

            /**
             * Turn of any validations.
             */
            setValidate(false);
            
            Iterator iter = colGroup.iterator();
            Attribute objAttr;
            String attributeName;
            while (iter.hasNext()) {
                objAttr = (Attribute) iter.next();
                attributeName = objAttr.getName();
                
                /**
                 * Set to nullif optional
                 * DGS09MAR2004: No, now always set to null first.
                 */
                setValue(attributeName, "", false, true);
                
                if (!pblnExplicitsOnly) {
                    /**
                     * Set to value most reasonable for data type
                     */
                    getValue(attributeName).setValuesToDefault();
                }
                
                /**
                 * Handle any default value available for attribute
                 */
                if (!StringUtil.isEmpty(objAttr.getDefaultValue())) {
                    String defaultValue = objAttr.getDefaultValue(); // Case insensitive
                    
                    if (defaultValue.equals("##")) {
                        setValue(attributeName, "#"); // Escaped "#"
                    } else if (defaultValue.startsWith("##")) {
	                    /**
	                     * ## indicates that we are dealing with a director
	                     * e.g. ##qs.-pf)
	                     */
	                    setValue(attributeName, getZx().resolveDirector(defaultValue.substring(1)));
                    } else if (defaultValue.charAt(0) == '#') { // #user, #false etc..
                        setValue(attributeName, defaultValue, true);  // This is a special value :
                    } else {
                        /**
                         * It does not start with a #, but it still could be a special value : 
                         */
                        if (defaultValue.startsWith("\\#")) {
                            setValue(attributeName, defaultValue.substring(1)); // Remove back space eg : \#hello = #hello
                        } else if (defaultValue.endsWith("yes")){ // yes or zyes
                            setValue(attributeName, "#" + defaultValue, true); // This is a special value
                        } else if (defaultValue.endsWith("no")){ // no or zno
                            setValue(attributeName, "#" + defaultValue, true); // This is a special value
                        } else if (defaultValue.endsWith("who")){ // who or zwho
                            setValue(attributeName, "#" + defaultValue, true); // This is a special value
                        } else if (defaultValue.endsWith("time")){ // time or ztime
                            setValue(attributeName, "#" + defaultValue, true); // This is a special value
                        } else if (defaultValue.endsWith("date")){ // date or zdate
                            setValue(attributeName, "#" + defaultValue, true); // This is a special value
                        } else {
                            setValue(attributeName, defaultValue); // Treat as is.
                        }
                    }
                }
                
                /**
                 * Property has now officialy been reset
                 */
                getValue(objAttr.getName()).setPersistStatus(zXType.propertyPersistStatus.ppsReset);
            }
            
            if (pblnAssignAutomatics) {
                setAutomatics(pstrGroup);
            }
            
            /**
             * Postpersist (cannot cause resetbo to fail)
             */
            try {
               postPersist(zXType.persistAction.paReset, pstrGroup, null, null); 
            } catch (Exception e) {
                getZx().log.warn("Failed to perform post persist action ", e);
            }
            
            setPersistStatus(zXType.persistStatus.psNew);
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : resetBO - Reset group of attributes to their original values", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pblnAssignAutomatics = " + pblnAssignAutomatics);
                getZx().log.error("Parameter : pblnExplicitsOnly = " + pblnExplicitsOnly);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
        } finally {
            /**
             * Restore original validate setting
             **/
        	setValidate(blnOrgValidate);
        	
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }	           
        }
    }
    
    /**
     * Assign new sequence number to all automatics in the specified attributegroup.
     * 
     * @throws ZXException Thrown if setAutomatics fail
     */
    public void setAutomatics() throws ZXException {
    	setAutomatics("+");
    }
    
	/**
	 * Assign new sequence number to all automatics in the specified attributegroup
	 * 
	 * @param pstrGroup The attributeGroup to set the automatics for. Optional, default should be "+".
	 * @throws ZXException Thrown if setAutomatics fails
	 */
    public void setAutomatics(String pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        try {
            /**
             * Get handle to group
             */
            AttributeCollection colGroup = this.descriptor.getGroup(pstrGroup);
            if (colGroup == null) { 
                getZx().trace.addError("Attribute group not found "  + pstrGroup + " for enitity ", getDescriptor().getName());
                return;
            }
            
            Attribute objAttr;
            Iterator iter = colGroup.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if (objAttr.getDataType().pos == zXType.dataType.dtAutomatic.pos) {
                    /**
                     * Sequence numbers are always generated using the primary data source
                     */
                    int lngSeqNo = getZx().getDataSources().getPrimary().getSeqNo(getDescriptor().getSequence());
                    if (lngSeqNo == -1) {
                        throw new RuntimeException("Unable to retrieve sequence number : " + objAttr.getName());
                    }
                    
                    setValue(objAttr.getName(), new LongProperty(lngSeqNo));
                }
            }
            
            /**
             * Postpersist (cannot cause setAutomatics to fail)
             */
            try {
                postPersist(zXType.persistAction.paSetAutomatics, pstrGroup);
            } catch (Exception e) {
                getZx().log.warn("Failed in post persist action.", e);
            }
            
            /**
             * Set the persist status to new. 
             */
            setPersistStatus(zXType.persistStatus.psNew);
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to :  Assign new sequence number to all automatics in the group ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e); 
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * getFKAttr - Find that attribute from FROM that is the foreign key to TO.
     * 
     * <pre>
     * 
     * Note : This should return a Attribute Collection.
     * Note : In this case FROM is "this" and to is "pobjToBO". 
     * </pre>
     * 
     * @param pobjToBO The ZXBO you want to link to.
     * @return Returns the foriegn key that links to the table you have select.
     * @throws ZXException Thrown if getFKAttr
     */
    public Attribute getFKAttr(ZXBO pobjToBO) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjToBO", pobjToBO);
        }

        Attribute getFKAttr = null;

        try {
            
            String strLookingFor = pobjToBO.descriptor.getName();
            
            /**
             * Loop through my attributes till i find a matching one :
             */
            Attribute objAttr;
            
            Iterator iter = this.descriptor.getAttributes().iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                /**
                 * TODO : Need to verify this fix with Bertus.
                 */
                
                /**
                 * BD9SEP04 - Fixed bug: now also take FK alias into consideration
                 */
                if (StringUtil.len(objAttr.getForeignKeyAlias()) > 0) {
                    if(objAttr.getForeignKeyAlias().equalsIgnoreCase(pobjToBO.getDescriptor().getAlias())) {
                        getFKAttr = objAttr;
                        break;
                    }
                }
                
                /**
                 * Carry on trying
                 */
                if(strLookingFor.equalsIgnoreCase(objAttr.getForeignKey())) {
                    getFKAttr = objAttr;
                    break;
                }
            }
            
            return getFKAttr;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Find that attribute from FROM that is the foreign key to TO ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjToBO = " + pobjToBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getFKAttr;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getFKAttr);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * setAuditAttr - Update the audit attributes for the given persist action.
     * 
     * @param penmAction The persistion action to perform on the Audit attributes.
     *                 Only paInsert and paUpdate are handled
     * @throws ZXException Thrown if setAuditAttr fails
     */
    public void setAuditAttr(zXType.persistAction penmAction) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmAction", penmAction);
        }

        try {
            /**
             * First check that BO is actually auditable
             */
            if (this.descriptor.isAuditable()) {
                if (penmAction.equals(zXType.persistAction.paInsert)) {
                    resetBO("!,-~", false);
                } else if (penmAction.equals(zXType.persistAction.paUpdate)) {
                    resetBO("zXUpdtdBy,zXUpdtdWhn", false);
                }
                
                /**
                 * BD9JUN04 - Set the update-id to either subsession (if available) or
                 *   the session-id
                 * The sub-session-id can be too long so truncate to 9 positions max
                 * so it does not cause an overflow
                 * DGS21FEB2005 - V1.4:50: Take the rightmost 9, not leftmost, for uniqueness
                 */
                String strSS = getZx().getQuickContext().getEntry("-ss");
                if (getDescriptor().isConcurrencyControl()) {
                    if(StringUtil.len(strSS) > 0) {
                        if (StringUtil.len(strSS) > 9) strSS = strSS.substring(strSS.length()-9);
                        setValue("zXUpdtdId", strSS);
                    } else {
                        setValue("zXUpdtdId", getZx().getSession().getPKValue());
                    }
                }
                
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to  : Update the audit attributes for the given persist action ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmAction = " + penmAction);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Test whether current user is allowed to delete the given BO.
     * 
     * Reviewed for V1.5:1
     * 
     * @return Returns true if the user (aka framework user) can delete this business object
     * @throws ZXException Thrown if mayDelete fails
     */
    public boolean mayDelete() throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
        
        boolean mayDelete = false;
        
		try {
            /**
             * See if data-sopurce handler supports it
             */
            if (!getDS().isSupportsDelete()) {
                return mayDelete;
            }
            
		    /**
		     * If there are no entries: assume all are allowed to
		     */
		    if (this.descriptor.getSecurityDelete() == null || this.descriptor.getSecurityDelete().isEmpty()) {
		        mayDelete = true;
		        return mayDelete;
		    }
		    
	        /**
	         * Else one of the user groups must be allowed
	         */
	        Tuple objTuple;
	        Iterator iter = this.descriptor.getSecurityDelete().iterator();
	        while (iter.hasNext()) {
	            objTuple = (Tuple)iter.next();
	            if(getZx().getUserProfile().isUserInGroup(objTuple.getValue())) {
	                mayDelete = true;
	                return mayDelete;
	            }
	        }
	        
		    return mayDelete;
		    
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Test whether user may delete this bo ", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    return mayDelete;
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.returnValue(mayDelete);
		        getZx().trace.exitMethod();
		    }
		}
    }
    
    /**
     * Test whether current user is allowed to Insert the given BO.
     * 
     * Reviewed for V1.5:1
     * 
     * @return Returns true if the user (aka framework user) can insert this business object
     * @throws ZXException Thrown if mayDelete fails
     */
    public boolean mayInsert() throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
        boolean mayInsert = false;
        
		try {
            /**
             * See if data-sopurce handler supports it
             */
            if (!getDS().isSupportsInsert()) {
                return mayInsert;
            }
            
		    /**
		     * If there are no entries: assume all are allowed to
		     */
		    if (this.descriptor.getSecurityInsert() == null || this.descriptor.getSecurityInsert().isEmpty()) {
		        mayInsert = true;
		        return mayInsert;
		    }
		    
	        /**
	         * Else one of the user groups must be allowed
	         */
	        Iterator iter = this.descriptor.getSecurityInsert().iterator();
	        Tuple objTuple;
	        while (iter.hasNext()) {
	            objTuple = (Tuple)iter.next();
	            if(getZx().getUserProfile().isUserInGroup(objTuple.getValue())) {
	                mayInsert = true;
	                return mayInsert;
	            }
	        }
	        
		    return mayInsert;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Check whether user can insert ", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    return mayInsert;
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.returnValue(mayInsert);
		        getZx().trace.exitMethod();
		    }
		}
    }
    
    /**
     * Test whether current user is allowed to Update the given BO.
     * 
     * Reviewed for V1.5:1
     * 
     * @return Returns true if the user (aka framework user) can update this business object
     * @throws ZXException Thrown if mayDelete fails
     */
    public boolean mayUpdate() throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
        boolean mayUpdate = false;
        
		try {
            /**
             * See if data-sopurce handler supports it
             */
            if (!getDS().isSupportsUpdate()) {
                return mayUpdate;
            }
            
		    /**
		     * If there are no entries: assume all are allowed to
		     */
		    if (this.descriptor.getSecurityUpdate() == null || this.descriptor.getSecurityUpdate().isEmpty()) {
		        mayUpdate = true;
		        return mayUpdate;
		    }
		    
	        /**
	         * Else one of the user groups must be allowed
	         */
            Tuple objTuple;
	        Iterator iter = this.descriptor.getSecurityUpdate().iterator();
	        while (iter.hasNext()) {
	            objTuple = (Tuple)iter.next();
	            if(getZx().getUserProfile().isUserInGroup(objTuple.getValue())) {
	                mayUpdate = true;
	                return mayUpdate;
	            }
	        }
	        
		    return mayUpdate;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Check whether user can update ", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    return mayUpdate;
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.returnValue(mayUpdate);
		        getZx().trace.exitMethod();
		    }
		}
    }

    /**
     * Test whether current user is allowed to Select the given BO.
     * 
     * Reviewed for V1.5:1
     * 
     * @return Returns true if the user (aka framework user) can Select this business object
     * @throws ZXException Thrown if mayDelete fails
     */
    public boolean maySelect() throws ZXException {
        boolean maySelect = false;
        
        /**
         * See if data-sopurce handler supports it
         */
        if (!getDS().isSupportsLoad()) {
            return maySelect;
        }
        
	    /**
	     * Selects are always true. 
         * For now, some clients may want more paranoid security settings :)
	     */
	    return true;
    }
    
    /**
     * Tell whether a business object already exists in the database.
     * 
     * <pre>
     * 
     * Reviewed for V1.5:86
     * NOTE : Was part of clsBOS
     * </pre>
     * 
     * @param pstrGroup Group for where clause. Is PK by default. Optional, default is "+"
     * @return Returns true if the BO already exists in the database
     * @throws ZXException Thrown of doesExist fails
     */
    public boolean doesExist(String pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrWhereAttributeGroup", pstrGroup);
        }

        boolean doesExist = false;
        boolean blnExtendedWhereGroup = false;
        
        /**
         * Set defaults
         */
        if (pstrGroup == null) {
            pstrGroup = "+";
        }
        
        try {
            /**
             * First clone BO ( as we do not want to upset the values of pobjBO); there
             * is a small catch here; if the group is an enhanced or full where group (e.g.
             * '<>nme' or 'dte>#value' than of-course we cannot simply interpret this
             * group as an attribute group; instead we simply copy all attributes just
             * in case we need them
             */            
            ZXBO objBO = cloneBO();
            if (objBO == null) {
                throw new ZXException("Failed to create a clone", getDescriptor().getName());
            }
            
            /**
             * Set validation to false as we do not want any false error messages in the log
             */
            objBO.setValidate(false);
            
            if (pstrGroup.charAt(0) != ':') {
            	// WAS ,!=<>%
            	// BUT if we have a , then it might just be a normal comma seperated attributes
            	if (StringUtil.containsAny("!=<>%", pstrGroup)) {
	                bo2bo(objBO, "*");
	                blnExtendedWhereGroup = true;
            	} else {
            		bo2bo(objBO, pstrGroup);
            	} // Enhanced where group?
            	
            } else {
	            bo2bo(objBO, "*");
	            blnExtendedWhereGroup = true;
            } // Full where group?
            
            /**
             * Try to load
             */
            zXType.rc enmRC =  objBO.loadBO( "+", pstrGroup, false);
            if(enmRC != null && enmRC.equals(zXType.rc.rcOK)) {
                /**
                 * Does exists is true when the retrieved BO is different than the
                 * original BO (ie different PKs)
                 */
                if(pstrGroup.equals("+") || pstrGroup.equals(objBO.getDescriptor().getPrimaryKey())) {
                	/**
                	 * PK is unique so when we have found one, it does exist
                	 */
                    doesExist = true;
                    
                } else if (!blnExtendedWhereGroup) {
                	/**
                	 * Bit of legacy really; when we use a simple attribute group,
                	 * we always compare the result with the PK to see if the row
                	 * we have found differs from pobjBO
                	 */
                    doesExist = (getPKValue().compareTo(objBO.getPKValue()) != 0);
                    
                } else {
                	/**
                	 * This is really how it should work: if a row is found, it thus
                	 * does exists....
                	 */
                	doesExist = true;
                	
                }
                
            } else {
                /**
                 * Does not exists so can never be duplicate
                 */
                doesExist = false;
                
            }
            
            return doesExist;
        } catch (Exception e) {
            getZx().trace.addError("Tell whether a business object already exists in the database", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrWhereAttributeGroup = " + pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return doesExist;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(doesExist);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Copy the FKBOs for the attributes in the group.
     * 
     * NOTE : This called cloneFKBO(pobjBOTo, "*");
     * 
     * @param pobjBOTo Thie Business object to copy the values to.
     * @return Returns success or failure, rather use try/catch
     * @throws ZXException Thrown if cloneFKBO fails.
     */
    public zXType.rc cloneFKBO(ZXBO pobjBOTo) throws ZXException {
        return cloneFKBO(pobjBOTo, "*");
    }
    
    /**
     * Copy the FKBOs for the attributes in the group.
     * 
     * @param pobjBOTo The Business object to copy the values to.
     * @param pstrGroup The attribute group to use when copying.
     * @return Returns failiare or success, but it is better to NOT use this but rather use the java try catch.
     * @throws ZXException Thrown if cloneFKBO fails, most likely in the pstrGroup
     */
    public zXType.rc cloneFKBO(ZXBO pobjBOTo, String pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBOTo", pobjBOTo);
            getZx().trace.traceParam("pstrGroup",pstrGroup);
        }

        zXType.rc cloneFKBO = zXType.rc.rcOK;
        
        try {
            
            /**
             * Get group
             */
            AttributeCollection colAttr = pobjBOTo.getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) {
                throw new Exception("Unable to get retrieve group : " + pstrGroup);
            }
            
            Iterator iter = colAttr.iterator();
            Attribute objAttr;
            while(iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                pobjBOTo.setValue(objAttr.getName(), getValue(objAttr.getName()));
            }
            
            return cloneFKBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Copy the FKBOs for the attributes in the group", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBOTo = " + pobjBOTo);
                getZx().log.error("Parameter : pstrGroup = " + pobjBOTo);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            cloneFKBO = zXType.rc.rcError;
            return cloneFKBO;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(cloneFKBO);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * bo2XML - Dump BO to xml.
     * 
     * @param pstrGroup Which attributes to select from the Business Object
     * @return Returns the xml format of a ZX Busniness Object
     * @throws ZXException Thrown if toXMLString fails
     */
    public String bo2XML(String pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        /**
         * Set defaults
         */
        if (pstrGroup == null) {
            pstrGroup = "*";
        }
        
        XMLGen bo2XML = new XMLGen(500);
        bo2XML.xmlHeader();
        
        try {
            
            AttributeCollection colAttr = this.descriptor.getGroup(pstrGroup);
            if (colAttr == null) { 
                throw new Exception("Unable to retrieve attributegroup : " + pstrGroup); 
            }
            
            bo2XML.openTag("BO");
            bo2XML.taggedValue("entity", this.descriptor.getName());

            /**
             * This is actually the property -- the attributes are the names of
             * the property. These are the values of the entity attributes.
             */
            bo2XML.openTag("attr");
            
            Attribute objAttr;
            String strTmp;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute) iter.next();
                
                /**
                 * BD20NOV04 - Use '' for null values (see also XML2BO)
                 */
                if (getValue(objAttr.getName()).isNull) {
                    strTmp = "";
                } else {
                    strTmp = getValue(objAttr.getName()).getStringValue();
                }
                
                /**
                 * BD20NOV04 - Escape all non-XML characters
                 * BD24JAN04 - V1.4:29 - No longer escape funny characters as it
                 * causes problems with MSXML3 and does not seem to do too much
                 * harm with MSXML4
                 */
                strTmp = StringEscapeUtils.escapeXml(strTmp);
                
                bo2XML.taggedCData( objAttr.getName(), strTmp );
                
            }
            
            bo2XML.closeTag("attr");
            
            bo2XML.closeTag("BO");
            
            return bo2XML.getXML();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : toXML - Dump BO to xml ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return bo2XML.getXML();
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(bo2XML.getXML());
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * xml2bo - Populating a bo from xml.
     * 
     * <pre>
     * 
     * Assumes : XML has been generated using BO2XML
     * </pre>
     * @param pstrXML The business objects as xml.
     * @throws ZXException Thrown if xml2bo fails.
     */
    public void xml2bo(String pstrXML) throws ZXException {
        xml2bo(pstrXML, "*");
    }
    
    /**
     * xml2bo - Populating a bo from xml.
     * 
     * <pre>
	 * 
	 * Assumes : XML has been generated using BO2XML
	 * </pre>
     * @param pstrXML The business objects as xml.
     * @param pstrGroup The attribute group used to load the business object.
     * @throws ZXException Thrown if xml2bo fails.
     */
    public void xml2bo(String pstrXML, String pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrXML", pstrXML);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        /**
         * Set defaults
         */
        if (pstrGroup == null) {
            pstrGroup = "*";
        }
        
        try {
            
            if (StringUtil.len(pstrXML) > 0) {
    	        /**
    	         * Load XML
    	         */
    	        Element objEntity = null;
    	        try {
    	            Document doc = new SAXBuilder().build(new StringReader(pstrXML));
    	            objEntity = doc.getRootElement();
    	        } catch (Exception e) {
    	            throw new NestableException("Failed to parse xml : " + pstrXML , e);
    	        }
                xml2bo(objEntity, pstrGroup);
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : populate - Create BO based on XML ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrXML = " + pstrXML);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
	/**
	 * xml2bo - Populate a bo from xml element.
     * 
	 * V1.15:16 - Fixed bug when you pass an attribute group
     * 
	 * @param pobjXML The xml from which you want to populate this bo.
	 * @param pstrGroup Optional group to retrieve (otherwise retrieve whatever is in XML)
	 * @throws ZXException Thrown if xml2bo fails
	 */
    public void xml2bo(Element pobjXML, String pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrXML", pobjXML);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        try {
            
            /**
             * Get the attribute root :
             */
            Element objBaseNode = pobjXML.getChild("attr");
            Element element;
            
            if (StringUtil.len(pstrGroup) == 0) {
                /**
                 * Load all the data that can be found in the XML Note the
                 * structure of the XML: BO/entity = entity name BO/attr =
                 * collection of attributes BO/attr/name = is single attribute
                 */
                Iterator objXMLNode = objBaseNode.getChildren().iterator();
                while (objXMLNode.hasNext()) {
                    element = (Element) objXMLNode.next();
                    setValue(element.getName(), element.getText());
                }
                
            } else {
                /**
                 * Get handle to collection
                 */
                AttributeCollection colGroup = this.descriptor.getGroup(pstrGroup);
                if (colGroup == null) { 
                    throw new Exception("Unable to get handle to group : " 
                                        + pstrGroup + " for enitity " + this.descriptor.getName()); 
                }
                
                Attribute objAttr;
                String strValue = "";
                
                Iterator iter = colGroup.iterator();
                while (iter.hasNext()) {
                    objAttr = (Attribute) iter.next();
                    
                    /**
                     * Cater for refering to attributes that do not exist in the XML
                     */
                    strValue = objBaseNode.getChildText(objAttr.getName());
                    if (StringUtil.len(strValue) == 0 
                        && getDescriptor().getAttribute(objAttr.getName()).isOptional()) {
                        /**
                         * Handle non-optional fields.
                         */
                        setValue(objAttr.getName(), "", false, true);
                        
                    } else {
                        setValue(objAttr.getName(), strValue);
                        
                    }
                    
                }
                
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : populate - Create BO based on XML ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrXML = " + pobjXML);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
	    
	/**
     * Simply return the entity name of a busines object.
     * 
     * @return Returns the name of this Business Object
     */
	public String whoAmI() {
        return this.descriptor.getName();
	}
	
	/**
	 * csvHeader - Create comma separated file header.
	 * 
	 * @param pstrGroup The attribute group to select from the entity
	 * @return Returns a csv header row of the attributegroup selected.
	 * @throws ZXException Thrown if csvHeader fails
	 */
	public String csvHeader(String pstrGroup) throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("pstrGroup", pstrGroup);
	    }
	
	    StringBuffer csvHeader = new StringBuffer();
		
	    try {
	        /**
	         * Get handle to collection
	         */
	        AttributeCollection colGroup = this.descriptor.getGroup(pstrGroup);
	        if (colGroup == null) { throw new Exception("Unable to retrieve attribute group :" + pstrGroup); }
	        
	        Iterator iter = colGroup.iterator();
	        Attribute objAttr;
	        while (iter.hasNext()) {
	            objAttr = (Attribute)iter.next();
	            
	            csvHeader.append("\"");
	            csvHeader.append(objAttr.getLabel().getLabel());
	            csvHeader.append("\"");
	            
	            if(iter.hasNext()) {
	                csvHeader.append(",");
	            }
	        }
	        
	        return csvHeader.toString();
	    } catch (Exception e) {
            getZx().trace.addError("Failed to : csvHeader - Create comma separated file header", e);
	        if (getZx().log.isErrorEnabled()) {
	            getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
	        }
	        
	        if (getZx().throwException) throw new ZXException(e);
	        return csvHeader.toString();
	    } finally {
	        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	            getZx().trace.returnValue(csvHeader);
	            getZx().trace.exitMethod();
	        }
	    }
	}
	
	/**
	 * csvRow - Create comma separated file row .
	 * 
	 * @param pstrGroup The attribute group to get the cvs row
	 * @return Returns a CSV formatted string of the select attribute group
	 * @throws ZXException Thrown if cvsRow fails.
	 */
	public String csvRow(String pstrGroup) throws ZXException {
	    return csvRow(pstrGroup, false);
	}
	
	/**
	 * csvRow - Create comma separated file row 
	 * 
	 * @param pstrGroup The attribute group to get the cvs row
	 * @param pblnRawFormatted Whether to format the row.
	 * @return Returns a CSV formatted string of the select attribute group
	 * @throws ZXException Thrown if cvsRow fails.
	 */
	public String csvRow(String pstrGroup, boolean pblnRawFormatted) throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("pstrGroup", pstrGroup);
	    }
	    
	    StringBuffer csvRow = new StringBuffer();
	    
	    try {
	        /**
	         * Get handle to collection
	         */
	        AttributeCollection colGroup = this.descriptor.getGroup(pstrGroup);
	        if (colGroup == null) { throw new Exception("Unable to retrieve attribute group :" + pstrGroup); }
	        
	        /**
	         * DGS19NOV2003: It is better to default to getting the formatted value rather than the
	         *  'raw' formatted value. This will then give us the FK label rather than just the id
	         *  as a string. Need to keep the 'raw' option available so that can export in a format that
	         *  would import ok.
	         *  Probably best to get the calling program to optionally pass in a boolean
	         *  which defaults to False. That would break compatibility, so for now will always assume
	         *  we want non-raw. In future feel free to add the parameter to this function
	         *  'optional pblnRaw as boolean = false'. Suggest the calling function (such as
	         *  zXWeb.clsPageflow.exportList) uses the business object's 'ResolveFK' setting to determine
	         *  the pblnRaw setting i.e. is raw when not resolveFK.
	         *  Here it is hard-coded (and not prefixed by p):
	         */
	        
	        Iterator iter = colGroup.iterator();
	        Attribute objAttr;
	        while (iter.hasNext()) {
	            objAttr = (Attribute)iter.next();
                
	            /**
	             * DGS19NOV2003: Related to above - also, as we always get a string back, best to wrap it in
	             * quotes every time, not just when not a numeric datatype, as we might get a string back
	             * even if datatype is numeric (e.g. resolved FK, option list value)
	             */
	            // Wrap non-ints in "" so that they are treated as stirng..
	            csvRow.append('"');
	            csvRow.append(pblnRawFormatted?getValue(objAttr.getName()).getStringValue():getValue(objAttr.getName()).formattedValue(true));
	            csvRow.append('"');
	                
	            /**    
	            }
	            **/
	            
	            /**
	             * Do not append if there is not more attributes.
	             */
	            if (iter.hasNext()) {
	                csvRow.append(",");
	            }
	        }
	        return csvRow.toString();
	    } catch (Exception e) {
            getZx().trace.addError("Failed to : csvRow - Create comma separated file row ", e);
	        if (getZx().log.isErrorEnabled()) {
	            getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
	        }
	        
	        if (getZx().throwException) throw new ZXException(e);
	        return csvRow.toString();
	    } finally {
	        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	            getZx().trace.returnValue(csvRow);
	            getZx().trace.exitMethod();
	        }
	    }
	}
	
	/**
	 * bo2bo - Copy properties by name from one BO to another.
	 * 
	 * @param pobjToBO The ZXBO to copy the property value to.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if bo2bo fails
	 */
	public zXType.rc bo2bo(ZXBO pobjToBO) throws ZXException {
	    return bo2bo(pobjToBO, "*", true);
	}
	
	/**
	 * bo2bo - Copy properties by name from one BO to another.
     * <pre>
     * So for example : 
     *  <code>objFromBO.bo2bo(objToBO, pstrGroup)</code>
     * 
     * Will copy values in the "pstrGroup"  in "objFromBo" to "objToBO", and will omit the persitStatus.
     * 
     * Which is the same as :
     * 
     * <code>zX.BOS.BO2BO objFromBO, objToBO, strLoadGroup</code>
     * 
     * in visual basic.
     * 
     * </pre>
     * 
     * @param pobjToBO The ZXBO to copy the property value to.
	 * @param pstrGroup The attribute from which the values will be copies from. Optional, default should be "*"
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if bo2bo fails
	 */
	public zXType.rc bo2bo(ZXBO pobjToBO, String pstrGroup) throws ZXException {
	    return bo2bo(pobjToBO, pstrGroup, true);
	}
	
	/**
	 * bo2bo - Copy properties by name from one BO to another.
	 * 
	 * <pre>
	 * So for example : 
	 * 	<code>objFromBO.bo2bo(objToBO, pstrGroup, false)</code>
     * 
     * Will copy values in the "pstrGroup"  in "objFromBo" to "objToBO", and will omit the persitStatus.
     * 
     * Which is the same as :
     * 
     * <code>zX.BOS.BO2BO objFromBO, objToBO, strLoadGroup</code>
     * 
	 * in visual basic.
     * 
	 * </pre>
	 * 
	 * @param pobjToBO The ZXBO to copy the property value to.
	 * @param pstrGroup The attribute from which the values will be copies from. Optional, default should be "*"
	 * @param pblnCopyPersistStatus Whether to copy the persist status of the object. Optional, default should be true.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if bo2bo fails
	 */
	public zXType.rc bo2bo(ZXBO pobjToBO, String pstrGroup, boolean pblnCopyPersistStatus) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjToBO", pobjToBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pblnCopyPersistStatus", pblnCopyPersistStatus);
        }

	    zXType.rc bo2bo = zXType.rc.rcOK;
	    
        try {
            
            /**
             * Prepersist (cannot cause BO2BO to fail)
             */
            try {
                if (pobjToBO.prePersist(zXType.persistAction.paBO2BO, pstrGroup).pos != zXType.rc.rcOK.pos) {
                    bo2bo = zXType.rc.rcWarning;
                }
                
            } catch (Exception e) {
                if(getZx().log.isWarnEnabled()) {
                    getZx().log.warn("Failed in presist of : " + getDescriptor().getName() , e);
                }
                bo2bo = zXType.rc.rcWarning;
                
            }
            
            /**
             * Get handle to group
             */
            AttributeCollection colAttr = this.descriptor.getGroup(pstrGroup);
            if(colAttr == null) {
                throw new Exception("Unable to retrieve group : " + pstrGroup);
            }
            
            Iterator iter = colAttr.iterator();
            Attribute objAttr;
            zXType.rc enmRC;
            
            while(iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                enmRC = pobjToBO.setValue(objAttr.getName(), getValue(objAttr.getName()));
                if (enmRC.pos == zXType.rc.rcOK.pos) {
                    /**
                     * BD28FEB05 - V1.5:52 - Also copy FKLabel
                     */
                    pobjToBO.getValue(objAttr.getName()).setFkLabel(getValue(objAttr.getName()).getFkLabel());
                    
                } else if (enmRC.pos > bo2bo.pos) {
                    /**
                     * Return the largest error :
                     */
                    bo2bo = enmRC;
                }
            }
            
            /**
             * Whether to copy the persist status
             */
            if (pblnCopyPersistStatus) {
                pobjToBO.setPersistStatus(getPersistStatus()) ;
            }
            
            /**
             * Postpersist (cannot cause cloneBO to fail)
             */
            try {
                if (pobjToBO.postPersist(zXType.persistAction.paBO2BO, pstrGroup).pos != zXType.rc.rcOK.pos) {
                    bo2bo = zXType.rc.rcWarning;
                }
                
            } catch (Exception e) {
                if(getZx().log.isWarnEnabled()) {
                    getZx().log.warn("Failed in post persist of " + getDescriptor().getName(), e);
                }
                bo2bo = zXType.rc.rcWarning;
                
            }
            
            return bo2bo;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : bo2bo - Copy properties by name from one BO to another ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjToBO = " + pobjToBO);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pblnCopyPersistStatus = " + pblnCopyPersistStatus);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return bo2bo;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
	}
	
    /**
	 * Copy BO values to fields in recordset.
	 * 
	 * <pre>
	 * 
	 * NOTE : THis calls bo2rs(pobjRs, "*", true);
	 * </pre>
	 *
	 * @param pobjRs The restultset the copy the values to. 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if bo2rs fails. 
	 */
	public zXType.rc bo2rs(ZXResultSet pobjRs) throws ZXException {
	    return bo2rs(pobjRs, "*", true);
	}
	
    /**
	 * Copy BO values to fields in recordset.
     * 
	 * NOTE: This routine should no longer be used directly; use the bo2rs method of the
     * clsDSRS class instead
     * 
     * TODO : Need to make sure that this works with CLOBS as well.
     *  
	 * @param pobjRs The restultset the copy the values to. 
	 * @param pstrGroup The attribute group to use when copying. Optional, default should be * 
	 * @param pblnForInsert Whether this is for an insert of update? Optional, default should be true 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if bo2rs fails. 
	 */
	public zXType.rc bo2rs(ZXResultSet pobjRs, 
                           String pstrGroup, 
                           boolean pblnForInsert) throws ZXException {
	    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("pobjRs", pobjRs);
	        getZx().trace.traceParam("pstrGroup", pstrGroup);
	        getZx().trace.traceParam("pblnForInsert", pblnForInsert);
	    }
	    
	    zXType.rc bo2rs = zXType.rc.rcOK;
        DSHRdbms objDSHandler;
        
	    /**
	     * Set defaults
	     */
	    if (pstrGroup == null) {
	        pstrGroup = "*";
	    }
	    
	    try {
            objDSHandler = (DSHRdbms)getDS();
            
	        /**
	         * Get attribute group. For auditable BOs and not being called
	         * recursively, add the audit columns.
             * 
	         * DGS27JUN2003: If an update, don't get the 'created by/when' columns, as these
	         * should not change on an update (and we might not have selected first, so they
	         * would get blanked).
	         */
	        ZXCollection colAttr;
	        if (getDescriptor().isAuditable()) {
                /**
                 * BD25MAR05 - Used to use pblnForInsert but this is a better way as we do not have the
                 * forInsert flag in clsDSRS
                 * BD13APR05 - V1.5:4 Used pobjRS.state, this is not good, must use editMode
                 */
	            if (pblnForInsert) {
	                colAttr = getDescriptor().getGroup(pstrGroup + ",!");
	            } else {
	                colAttr = getDescriptor().getGroup(pstrGroup + ",!,-zXCrtdWhn,-zXCrtdBy");
	            }
                
	        } else {
	            colAttr = getDescriptor().getGroup(pstrGroup);
	        }
	        
	        if (colAttr == null) {
	            throw new ZXException("Attribute group not found", pstrGroup);
	        }
	        
	        Attribute objAttr;
	        Property objProperty;
	        String strFieldName;
	        zXType.databaseType dbType = objDSHandler.getDbType();
            
	        Iterator iter = colAttr.iterator();
	        while (iter.hasNext()) {
	            objAttr = (Attribute)iter.next();
	            strFieldName = objAttr.getColumn();
	            
	            if (StringUtil.len(strFieldName) > 0 && !objAttr.isVirtualColumn()) {
	                objProperty = getValue(objAttr.getName());
	                
                    if (objProperty.isNull) {
                        /**
                         * BD9DEC02 Check for null: when the attribute is not set up as null
                         * the Oracle driver generates a nasty error that we do wish to show
                         * the user
                         * BD5SEP04 Added option to use validate to ignore this; it assumes
                         * that all database fields are created as optional / nulllable (this
                         * I believe is the best anyway so that the Framework can take care
                         * of what is optional and what is mandatory).
                         * This feature can be useful in situations where you want to create
                         * a row without all the fields being known (eg in a multi-step
                         * wizard)
                         */
                        if (objAttr.isOptional() || !isValidate()) {
                            pobjRs.updateProperty(strFieldName, null, null);
                            
                        } else {
                            throw new ZXException("Application error: attempt to assign null to a non-null field.",
                                                  getDescriptor().getLabel().getLabel() + " / " + objAttr.getName());
                        }
                        
                    } else {
                        pobjRs.updateProperty(strFieldName, objProperty, dbType);
                    }
                    
	            } // Column and no virtual attr
	        }
	        
	        return bo2rs;
	    } catch (Exception e) {
	        getZx().trace.addError("Failed to : Copy BO values to fields in recordset.", e);
	        if (getZx().log.isErrorEnabled()) {
	            getZx().log.error("Parameter : pobjRs = "+ pobjRs);
	            getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
	            getZx().log.error("Parameter : pblnForInsert = "+ pblnForInsert);
	        }
	        if (getZx().throwException) throw new ZXException(e);
	        bo2rs = zXType.rc.rcError;
	        return bo2rs;
	        
	    } finally {
	        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
	            getZx().trace.returnValue(bo2rs);
	            getZx().trace.exitMethod();
	        }
	    }
	}
    
    /**
     * Execute a query based on a business object and store in a collection.
     * 
     * @param pstrGroup Attributegroup that will be selected. Optional, default is "*"
     * @param pstrWhereGroup Attribute group that is used to construct where clause. Use
     *                 vbnullstring (is default) to have no where clause
     * @return Returns a BOCollection of ZXBO's from the database.
     * @throws ZXException Thrown if db2Collection fails
     */
    public BOCollection db2Collection(String pstrGroup, String pstrWhereGroup) throws ZXException {
        return db2Collection("", pstrGroup, false, pstrWhereGroup, null, false);
    }
    
    /**
     * Execute a query based on a business object and store in a collection.
     * 
     * BD25MAR05 - V1.5:1; Important notice: support for whereClause is now stopped
     * 
     * @param pstrKeyAttr Attribute (from BO) that will be used as the key to store
     *                 elements. Use null for no key. Optional, default is "+"
     * @param pstrGroup Attributegroup that will be selected. Optional, default is "*"
     * @param pblnResolveFK Whether to resolve the foriegn keys or not. Optional, default is false.
     * @param pstrWhereGroup Attribute group that is used to construct where clause. Use
     *                 vbnullstring (is default) to have no where clause
     * @param pstrOrderByGroup Attribute group that will be used. Optional, default is null
     *                 to order the elements before they are added to the
     *                 collection. Optional, default is null
     * @param pblnReverse Reverse order yes / no (defaults to no). Optional, default is false
     * @return Returns a BOCollection of ZXBO's from the database.
     * @throws ZXException Thrown if db2Collection fails
     */
	public BOCollection db2Collection(String pstrKeyAttr, 
                                      String pstrGroup, 
                                      boolean pblnResolveFK,
	                                  String pstrWhereGroup, 
	                                  String pstrOrderByGroup, 
                                      boolean pblnReverse) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrKeyAttr", pstrKeyAttr);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
            getZx().trace.traceParam("pstrOrderByGroup", pstrOrderByGroup);
            getZx().trace.traceParam("pblnReverse", pblnReverse);
        }

        BOCollection db2Collection = new BOCollection();
        
        DSHandler objDSHandler;
        DSRS objRS = null;
        
	    /**
	     * Defaults : 
	     */
	    if (pstrGroup == null) {
	        pstrGroup = "*";
	    }
	    if (pstrKeyAttr == null) {
	        pstrKeyAttr = "+";
	    }
	    
        try {
            /**
             * Get data-source handler
             */
            objDSHandler = getDS();
            
            /**
             * Turn into recordset
             */
            objRS = objDSHandler.boRS(this,
                                      pstrGroup,
                                      pstrWhereGroup,
                                      pblnResolveFK,
                                      pstrOrderByGroup,
                                      pblnReverse, 
                                      0, 0);
            if (objRS == null) {
                throw new Exception("Unable to execute query");
            }
            
            /**
             * Loop over the recordset and add to collection
             */
            boolean blnUseKey = StringUtil.len(pstrKeyAttr) > 0;
            int pos = 0;
            while (!objRS.eof()) {
                /**
                 * Create new instance of object; must be of type object (ie
                 * not of type iBO) to allow use in ASP as well
                 */
                ZXBO  objItem = this.cloneBO();
                if (objItem == null) {
                    throw new Exception("Unable to cloneBO");
                }
                
                objRS.rs2obj(objItem, pstrGroup, pblnResolveFK);
                
                /**
                 * Add to collection
                 */
                if (blnUseKey) {
                    /**
                     * BD16AUG04 Very serious bug: do not use formattedString; if the
                     * keyAttr is an FK two things may happen:
                     * 		- We store the item by resolved FK (i.e. Netherlands instead of NL)
                     * 		- If resolveFK is false, we retrieve the row every time
                     */
                    //db2Collection.put(objItem.formattedString(pstrKeyAttr, true, " "), objItem);
                    db2Collection.put(objItem.getValue(pstrKeyAttr).getStringValue(), objItem);
                    
                } else {
                    // ?? Added without key, there a int position key will be used.
                    // Well then we should use an array list in the ZXCOllection
                    db2Collection.put(new Integer(pos), objItem);
                    pos++;
                    
                }
                
                objRS.moveNext();
            }
            
            return db2Collection;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Execute a query based on a business object and store in a collection", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrKeyAttr = " + pstrKeyAttr);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pblnResolveFK = " + pblnResolveFK);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
                getZx().log.error("Parameter : pstrOrderByGroup = " + pstrOrderByGroup);
                getZx().log.error("Parameter : pblnReverse = " + pblnReverse);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return db2Collection;
        } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(db2Collection);
                getZx().trace.exitMethod();
            }
        }
	}
    
    /**
     * Execute a query based on a business object and store in a array.
     * 
     * @param pstrGroup Attributegroup that will be selected. Optional, default is "*"
     * @param pstrWhereGroup Attribute group that is used to construct where clause. Use
     *                 vbnullstring (is default) to have no where clause
     * @return Returns a BOCollection of ZXBO's from the database.
     * @throws ZXException Thrown if db2Collection fails
     */
    public ZXBO[] db2Array(String pstrGroup, String pstrWhereGroup) throws ZXException {
        return db2Array(pstrGroup, false, pstrWhereGroup, null, null, false);
    }
    
	/**
     * Execute a query based on a business object and store in a array.
     * 
     * @param pstrGroup Attributegroup that will be selected. Optional, default is "*"
     * @param pblnResolveFK Whether to resolve the foriegn keys or not. Optional, default is false.
     * @param pstrWhereGroup Attribute group that is used to construct where clause. Use
     *                              vbnullstring (is default) to have no where clause
     * @param pstrWhereClause Optional whereclause that will be added at and of generated
     *                 query. Must be based on BO and should start with 'AND' as it
     *                 will be concatenated as-is
     * @param pstrOrderByGroup Attribute group that will be used. Optional, default is null
     *                 to order the elements before they are added to the
     *                 collection. Optional, default is null
     * @param pblnReverse Reverse order yes / no (defaults to no). Optional, default is false
     * @return Returns a BOCollection of ZXBO's from the database.
     * @throws ZXException Thrown if db2Collection fails
	 */
	public ZXBO[] db2Array(String pstrGroup, boolean pblnResolveFK,
                           String pstrWhereGroup,String pstrWhereClause,
                           String pstrOrderByGroup, boolean pblnReverse) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
            getZx().trace.traceParam("pstrWhereClause", pstrWhereClause);
            getZx().trace.traceParam("pstrOrderByGroup", pstrOrderByGroup);
            getZx().trace.traceParam("pblnReverse", pblnReverse);
        }
        
	    ArrayList db2Array = new ArrayList();
        
        DSHRdbms objDSHandler;
        DSRS objRS = null;
        
	    /**
	     * Defaults : 
	     */
	    if (pstrGroup == null) {
	        pstrGroup = "*";
	    }
	    
        try {
            objDSHandler = (DSHRdbms)getDS();
            
            /**
             * Generate query
             */
            String strQry = getZx().sql.loadQuery(this, pstrGroup, pblnResolveFK, false);
            
            /**
             * Add optional where clause
             */
            if (!StringUtil.isEmpty(pstrWhereGroup)) {
                strQry = strQry + " AND " + getZx().sql.whereCondition(this, pstrWhereGroup);
            }
            
            /**
             * Add the optional where clause provided by the caller
             */
            if (!StringUtil.isEmpty(pstrWhereClause)) {
                strQry = strQry + " " + pstrWhereClause;
            }
                
            /**
             * Add the optional order by clause
             */
            if (!StringUtil.isEmpty(pstrOrderByGroup)) {
                strQry = strQry + getZx().sql.orderByClause(this, pstrOrderByGroup, pblnReverse);
            }
            
            /**
             * Turn into recordset
             */
            objRS = objDSHandler.sqlRS(strQry);
            if (objRS == null) {
                throw new Exception("Unable to execute query");
            }
            
            /**
             * Loop over the recordset and add to collection
             */
            ZXBO objItem;
            while (!objRS.eof()) {
                /**
                 * Create new instance of object
                 */
                objItem = this.cloneBO();
                if (objItem == null) {
                    throw new Exception("Unable to cloneBO");
                }
                objRS.rs2obj(objItem, pstrGroup, pblnResolveFK);
                db2Array.add(getProperties());
                
                objRS.moveNext();
            }
            
            return (ZXBO[])db2Array.toArray();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Create array Object", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pblnResolveFK = " + pblnResolveFK);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
                getZx().log.error("Parameter : pstrWhereClause = " + pstrWhereClause);
                getZx().log.error("Parameter : pstrOrderByGroup = " + pstrOrderByGroup);
                getZx().log.error("Parameter : pblnReverse = " + pblnReverse);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return (ZXBO[])db2Array.toArray();
        } finally {
            /**
             * Close resultset
             */
            if (objRS != null) objRS.RSClose();
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(db2Array);
                getZx().trace.exitMethod();
            }
        }
	}
	
	/**
	 * Persist a collection of BOs.
	 * 
	 * NOTE : This calls persistCollection(pcolCol, "*", "+");
	 * 
	 * @param pcolCol  The collection of BO's to persist
	 * @throws ZXException Thrown if persistCollection fails
	 */
	public void persistCollection(ZXCollection pcolCol) throws ZXException {
        persistCollection(pcolCol, "*", "+");
	}	
	
    /**
     * Persist a collection of BOs.
     * 
     * @param pcolCol The collection of BO's to persist
     * @param pstrGroup The attributeGroup to use. Optional, default is "*"
     * @param pstrWhereGroup Attribute group that is used to construct where clause.
     *                 	Optional, default is "+"
     * @return Returns the return code of the method
     * @throws ZXException Thrown if persistCollection fails
     */
	public zXType.rc persistCollection(ZXCollection pcolCol, String pstrGroup, String pstrWhereGroup) throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pcolCol", pcolCol);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }

	    zXType.rc persistCollection = zXType.rc.rcOK;
	    
	    /**
	     * Set defaults
	     */
	    if (pstrGroup == null) {
	        pstrGroup = "*";
	    }
	    if (pstrWhereGroup == null) {
	        pstrWhereGroup = "+";
	    }
	    
        try {
            ZXBO objBO;
            
            Iterator iter = pcolCol.iterator();
            while (iter.hasNext()) {
                objBO = (ZXBO)iter.next();
                
                if (!objBO.persistBO(pstrGroup, pstrWhereGroup).equals(zXType.rc.rcOK)) {
                    throw new ZXException("Unable to persist BO", objBO.getDescriptor().getName());
                }
            }
            
            return persistCollection;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Persist a collection of BOs", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pcolCol = " + pcolCol);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
            persistCollection = zXType.rc.rcError;
            return persistCollection;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
	}

	/**
	 * Load a BO from the database using the FK attribute of a business object.
	 * 
	 * NOTE : This was originally in clsBOS.
	 * NOTE : This calls  quickFKLoad(pstrFkAttr, "*")
	 * 
	 * @param pstrFkAttr The name of the foriegn attribute.
	 * @return Returns a ZXBO by the selected FK attribute
	 * @throws ZXException Thrown if quickFKLoad fails
	 */
	public ZXBO quickFKLoad(String pstrFkAttr) throws ZXException {
	    return quickFKLoad(pstrFkAttr, "*");
	}
	
	/**
	 * Load a BO from the database using the FK attribute of a business object.
	 * 
	 * NOTE : This was originally in clsBOS.
	 * 
	 * @param pstrFkAttr The name of the foriegn attribute.
	 * @param pstrGroup Group to load, defaults to *
	 * @return Returns a ZXBO by the selected FK attribute
	 * @throws ZXException Thrown if quickFKLoad fails
	 */
	public ZXBO quickFKLoad(String pstrFkAttr,String pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrFkAttr", pstrFkAttr);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        ZXBO quickFKLoad = null;
	    
        /**
         * Set defaults
         */
        if (pstrGroup == null) {
            pstrGroup = "*";
        }
        
        try {
            
            Attribute objAttr = getDescriptor().getAttribute(pstrFkAttr);
            
            /**
             * Check that the attribute requested is actually a foreign key
             */            
            if (StringUtil.len(objAttr.getForeignKey()) == 0) {
                throw new Exception("Attribute (" + pstrFkAttr + ") is not a foreign key : " + getDescriptor().getName());
            }
            
            /**
             *Check that the foreign key value is not null
             */
            if (getValue(pstrFkAttr).isNull) {
                throw new Exception("Value of attribute (" + pstrFkAttr + ") is null in " + getDescriptor().getName());
            }
            
            /**
             * Time to load
             */
            quickFKLoad = getZx().quickLoad(objAttr.getForeignKey(), 
                    						getValue(pstrFkAttr), "", pstrGroup);
            
            /**
             * 01MAR2004: Must return an iBO for ASP to function correctly
             * 23MAR2004: Don't cause an error if quickload didn't find it 
             * NOTE : Not needed in java.
             */
            
            return quickFKLoad;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Load a BO from the database using the FK attribute", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrFkAttr = " + pstrFkAttr);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return quickFKLoad;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(quickFKLoad);
                getZx().trace.exitMethod();
            }
        }
	}
	
	/**
	 * Return value of single column.
     * 
     * NOTE: This routine should no longer be used directly; use the rs2bo method of the
     * clsDSRS class instead.
     * 
	 * @param pobjRs The result set tp use
	 * @param pobjAttr The attribute to select.
	 * @return Returns the value of a single column
	 * @throws ZXException Thrown if rsColumn fails.
	 */
	public Property rsColumn(ZXResultSet pobjRs, Attribute pobjAttr) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRs", pobjRs);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
        }

	    Property rsColumn = null;
	    
        try {
            
            rsColumn = pobjRs.getPropertyFromDB(this, pobjAttr, zXType.sqlObjectName.sonRSName);
            
            return rsColumn;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Return value of single column", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRs = " + pobjRs);
                getZx().log.error("Parameter : pobjAttr = " + pobjAttr);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return rsColumn;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(rsColumn);
                getZx().trace.exitMethod();
            }
        }
	}
	
	/**
	 * Retrieve an attribute by its ordinal number.
	 * 
	 * <pre>
	 * 
	 * NOTE : This calles : this.zx.getBos().getAttrByNumber(pintAttr, new ZXBO[]{this}, new String[]{pstrGroup});
	 * </pre>
	 * 
	 * @param pintAttr The number of the postion in the business object that you want to retrieve
	 * @param pstrGroup The attribute group you want to select the attribute from.
	 * @see ZXBOS#getAttrByNumber(int, ZXBO[], String[])
	 * @return Returns a attribute by number in the business object.
	 * @throws ZXException Thrown if getAttrByNumber fails.
	 */
	public Attribute getAttrByNumber(int pintAttr, String pstrGroup) throws ZXException {
            return getZx().getBos().getAttrByNumber(pintAttr, new ZXBO[]{this}, new String[]{pstrGroup});
	}

	/**
	 * Load objects relate (one to many) to another object.
	 * 
	 * <pre>
	 * 
	 * Assumes : nothing returned in case of problems; empty collectio if none
	 * found
	 * </pre>
	 * 
	 * @param pstrEntity  Name of entity that has FK to BO
	 * @param pstrFkAttr Attribute that is FK to BO; determined automatically if left blank	 
	 * @return Load objects relate (one to many) to another object
	 * @throws ZXException
	 */
	public BOCollection quickFKCollection(String pstrEntity, String pstrFkAttr) throws ZXException {
	    return quickFKCollection(pstrEntity, pstrFkAttr, "+", "*", false, null, null, false);
	}	
	
	/**
	 * Load objects relate (one to many) to another object.
	 * 
	 * <pre>
	 * 
	 * Assumes : nothing returned in case of problems; empty collectio if none
	 * found
     * Assumes : Instance of BO, must have PK set
	 * 
	 * </pre>
	 * 
	 * @param pstrEntity  Name of entity that has FK to BO
	 * @param pstrFkAttr Attribute that is FK to BO; determined automatically if left blank
	 * @param pstrKeyAttr Name of attribute to use as key. Optional, default is "+"
	 * @param pstrGroup group to load. Optional, default is "*"
	 * @param pblnResolveFK resolve fk yes / no. Optional, default is false.
	 * @param pstrWhereGroup optional where group. Optional, default is null.
	 * @param pstrOrderByGroup optional order by groups. Optional, default is null.
	 * @param pblnReverse need to reverse order (only relevant when orderByGroup <> ''). Optional, default is false
	 * 
	 * @return Returns a collection of business objects related (one to many) to another object
	 * @throws ZXException Thrown if quickFKCollection fails
	 */
	public BOCollection quickFKCollection(String pstrEntity, 
                                          String pstrFkAttr, 
                                          String pstrKeyAttr, 
                                          String pstrGroup, 
                                          boolean pblnResolveFK,
                                          String pstrWhereGroup, 
                                          String pstrOrderByGroup, 
                                          boolean pblnReverse) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrEntity", pstrEntity);
            getZx().trace.traceParam("pstrFkAttr", pstrFkAttr);
            getZx().trace.traceParam("pstrKeyAttr", pstrKeyAttr);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
            getZx().trace.traceParam("pstrOrderByGroup", pstrOrderByGroup);
            getZx().trace.traceParam("pblnReverse", pblnReverse);
        }

        BOCollection quickFKCollection = null;
	    
	    /**
	     * Set defaults
	     */
	    if (pstrKeyAttr == null) {
	    	pstrKeyAttr = "+";
	    }
	    if (pstrGroup == null) {
	        pstrGroup = "*";
	    }
	    
	    try {
            ZXBO objBO = getZx().createBO(pstrEntity);
            if (objBO == null) { 
                throw new Exception("Failed to : Create an instance of zx for : " + pstrEntity);
            }
            
            if (StringUtil.len(pstrFkAttr) == 0) {
                /**
                 * No pstrFKAttr was not send so try and get it from the FK attr
                 */
                Attribute objFKAttr = objBO.getFKAttr(this);
                
                if (objFKAttr == null) {
                    throw new Exception("Failed to : Create the objFKAttr for " + this.getDescriptor().getName());
                }
                
                pstrFkAttr = objFKAttr.getName();
            }
            
            objBO.setValue(pstrFkAttr, getPKValue());
            
            /**
             * Bit tricky: if we have a whereGroup, we have to merge this with the FKAttr. A wheregroup
             * can have two formats indicated by the first position (':' or not).
             */
            if (StringUtil.len(pstrWhereGroup) > 0) {
                DSWhereClause objDSWhereClause = new DSWhereClause();
                
                if (objDSWhereClause.parse(objBO, pstrWhereGroup).pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to parse where group");
                }
                
                if (objDSWhereClause.addClauseWithAND(this, pstrKeyAttr).pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to add to where group");
                }
                
                pstrWhereGroup = objDSWhereClause.getAsCompleteWhereClause();
                
            } else {
                pstrWhereGroup = pstrFkAttr;
                
            } // Has wheregroup?
            
            quickFKCollection = objBO.db2Collection(pstrKeyAttr, 
                                                    pstrGroup, 
                                                    pblnResolveFK, 
                                                    pstrFkAttr, 
                                                    pstrOrderByGroup, 
                                                    pblnReverse);
            if (quickFKCollection == null) {
                throw new Exception("Failed to get a Collection of ZXBO for : " + pstrEntity);
            }
            
            return quickFKCollection;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Load objects relate (one to many) to another object ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrEntity = " + pstrEntity);
                getZx().log.error("Parameter : pstrFkAttr = " + pstrFkAttr);
                getZx().log.error("Parameter : pstrKeyAttr = " +  pstrKeyAttr);
                getZx().log.error("Parameter : pstrGroup = " +  pstrGroup);
                getZx().log.error("Parameter : pblnResolveFK = " +  pblnResolveFK);
                getZx().log.error("Parameter : pstrWhereGroup = " +  pstrWhereGroup);
                getZx().log.error("Parameter : pstrOrderByGroup = " +  pstrOrderByGroup);
                getZx().log.error("Parameter : pblnReverse = " + pblnReverse);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return quickFKCollection;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(quickFKCollection);
                getZx().trace.exitMethod();
            }
        }
	}
	
	//------------------------ Need to overload this
	
	/**
	 * Allow developer to validate / massage entity before something happens.
	 * 
	 * NOTE : This calls postPersist(penmPersistAction, pstrGroup, null,null);  
	 * 
	 * @param penmPersistAction The persist action that is being performed
	 * @param pstrGroup Changed to StringBuffer as can be changed by extendGroup. The attribute group of the values being persisted
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if postPersist fails
	 */
	public zXType.rc postPersist(zXType.persistAction penmPersistAction, String pstrGroup) throws ZXException {
	    return postPersist(penmPersistAction, pstrGroup, "", "");          
	}
	
	/**
	 * Allow developer to validate / massage entity before something happens.
	 * 
	 * <pre>
	 * 
	 * NOTE : This calls postPersist(penmPersistAction, pstrGroup, pstrWhereAttributeGroup,null);  
	 * </pre>
	 * 
	 * @param penmPersistAction The persist action that is being performed
	 * @param pstrGroup Changed to StringBuffer as can be changed by extendGroup. The attribute group of the values being persisted
	 * @param pstrWhereGroup The key being used
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if postPersist fails
	 */
	public zXType.rc postPersist(zXType.persistAction penmPersistAction, 
								 String pstrGroup, 
								 String pstrWhereGroup) throws ZXException {
	    return postPersist(penmPersistAction, pstrGroup, pstrWhereGroup, "");          
	}	
	
	/**
	 * Allow developer to validate / massage entity before something
	 * happens.
	 * 
	 * <p>
	 * The Methods prePersist and postPersist are ‘exits’ that are called 
	 * at strategic places by all zX routines.
	 * </p>
	 * 
	 * <p>
	 * PrePersist is called before a key event (with penmPersistAction 
	 * indicating the event) and postPersist after the event. These methods 
	 * can as such be compared to database triggers.
	 * </p>
	 * 
	 * <p>
	 * Both methods are ‘empty’ by default but are designed to allow the 
	 * developer to implement specific business object behaviour. Typical 
	 * examples of use are:
	 * </p>
	 * 
	 * <ul>
	 * <li>Implement validation before updating or inserting a business object</li>
	 * <li>Update related business objects as part of a database transaction</li>
	 * </ul>
	 * 
	 * <p>
	 * NOTE : This is a template for people who want to implement this method.
	 * </p>
	 * 
	 * @param penmPersistAction Optional, defaults to paProcess
	 * @param pstrGroup Changed to StringBuffer as can be changed by extendGroup. Optional, defaults to null
	 * @param pstrWhereGroup Optional defaults to null
	 * @param pstrId Optional defaults to null
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if postPersist fails
	 */
	public zXType.rc postPersist(zXType.persistAction penmPersistAction, 
	        					 String pstrGroup, 
	        					 String pstrWhereGroup,
						         String pstrId) throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("penmPersistAction", penmPersistAction);
	        getZx().trace.traceParam("pstrGroup", pstrGroup);
	        getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
	        getZx().trace.traceParam("pstrId", pstrId);
	    }
	
	    zXType.rc postPersist = zXType.rc.rcOK;
	    
	    /**
	     * Handle defaults :
	     */
	    if (penmPersistAction == null) {
	        penmPersistAction = zXType.persistAction.paProcess;
	    }

	    try {
	        
	        /**
	         * See if we have any event actions defined at BO level
	         */
	        if (StringUtil.len(getDescriptor().getPostEvents()) > 0) {
	            /**
	             * See if one has been defined for this persistaction
	             */
	            if (getDescriptor().getPostEvents().charAt(penmPersistAction.pos) == '1') {
	                postPersist = handleEventActions(zXType.eaTiming.eatPost,
	                								 penmPersistAction,
	                								 pstrGroup,
	                								 pstrWhereGroup,
	                								 pstrId);
	            }
	        }
	        
	        /**
	         * BD28MAR04
	         * Save the last RC (i.e. the rc of whatever we were doing that
	         * caused the pre- and post-persist to be called) so we
	         * use it in the iBO.postPersist to take some special
	         * action in case of failure.
	         * 
	         * A cleaner solution would have been to add the lastRc as a parameter
	         * to the interface method postPersist but this would have caused
	         * too much rework. 
	         */
	        if (postPersist.pos > getLastPostPersistRC().pos) {
	            setLastPostPersistRC(postPersist);
	        }
	        
	        getZx().setPostPersistLastRc(getLastPostPersistRC());
	        
	        return postPersist;
	    } catch (Exception e) {
            getZx().trace.addError("Failed to : Do post persist action ", e);
	        if (getZx().log.isErrorEnabled()) {
	            getZx().log.error("Parameter : penmPersistAction = " + penmPersistAction);
	            getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
	            getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
	            getZx().log.error("Parameter : pstrId = " + pstrId);
	        }
	        
	        if (getZx().throwException) throw new ZXException(e);
	        return postPersist;
	    } finally {
	        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	            getZx().trace.exitMethod();
	        }
	    }
	
	}
	
    /**
     * Allow developer to validate / massage entity before something happens.
     * 
     * <pre>
     * 
     * NOTE : This calls prePersist(penmPersistAction, pstrGroup, null, null);
     * </pre>
     * 
     * @param penmPersistAction 
     * 					The persistAction being performs, ie : insert/update and
     *                 delete etc..
     * @param pstrGroup Changed to StringBuffer as can be changed by extendGroup. The attribute group that is being used.
     * @return Returns the return code of the method.
     * @see ZXBO#prePersist(zXType.persistAction, String, String, String)
     * @throws ZXException Thrown if prePersist fails
     */
    public zXType.rc prePersist(zXType.persistAction penmPersistAction, String pstrGroup) throws ZXException {
        return prePersist(penmPersistAction, pstrGroup, "", "");
    }
    
	/**
	 * Allow developer to validate / massage entity before something happens.
	 * 
	 * <pre>
	 * 
	 * NOTE : This calles : prePersist(penmPersistAction, pstrGroup, pstrWhereGroup, null);
	 * </pre>
	 * 
     * @param penmPersistAction The persistAction being performs, ie : insert/update and
     *                 delete etc..
     * @param pstrGroup Changed to StringBuffer as can be changed by extendGroup. The attribute group that is being used.
	 * @param pstrWhereGroup The key group to use
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if prePersist fails
	 * @see ZXBO#prePersist(zXType.persistAction, String, String, String)
	 */
	public zXType.rc prePersist(zXType.persistAction penmPersistAction, 
								String pstrGroup, 
								String pstrWhereGroup) throws ZXException {
	    return prePersist(penmPersistAction, pstrGroup, pstrWhereGroup, "");
	}
	
	/**
	 * Allow developer to validate / massage entity before something
	 * happens.
	 * 
	 * <pre>
	 * 
	 * NOTE : Pre persist action should not throw exception when there is a validation error. 
	 * Only if there is an exception in the code.
	 * NOTE : This is a template for people who want to implement this method.
	 * </pre>
	 * 
	 * @param penmPersistAction Optional, defaults to paProcess
	 * @param pstrGroup Changed to StringBuffer as can be changed by extendGroup. Optional, defaults to null
	 * @param pstrWhereGroup Optional defaults to null
	 * @param pstrId Optional defaults to null
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if postPersist fails
	 * 
	 * @see #postPersist(zXType.persistAction, String, String, String)
	 */
	public zXType.rc prePersist(zXType.persistAction penmPersistAction, 
	        					String pstrGroup, 
	        					String pstrWhereGroup,
	        					String pstrId)throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("penmPersistAction", penmPersistAction);
	        getZx().trace.traceParam("pstrGroup", pstrGroup);
	        getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
	        getZx().trace.traceParam("pstrId", pstrId);
	    }
	    
	    zXType.rc prePersist = zXType.rc.rcOK;
	    
	    /**
	     * Set defaults
	     */
	    if (penmPersistAction == null) {
	        penmPersistAction = zXType.persistAction.paProcess;
	    }

	    try {
	        
	        /**
	         * See if we have any event actions defined at BO level
	         */
	        if (StringUtil.len(getDescriptor().getPreEvents()) > 0) {
	            /**
	             * See if one has been defined for this persistaction
	             */
	            if (getDescriptor().getPreEvents().charAt(penmPersistAction.pos) == '1') {
	                prePersist = handleEventActions(zXType.eaTiming.eatPre, 
	                								penmPersistAction, 
	                								pstrGroup, 
	                								pstrWhereGroup, 
	                								pstrId);
	            }
	        }
	        
	        return prePersist;
	    } catch (Exception e) {
            getZx().trace.addError("Failed to : Do pre persist action ", e);
	        if (getZx().log.isErrorEnabled()) {
	            getZx().log.error("Parameter : penmPersistAction = " + penmPersistAction);
	            getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
	            getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
	            getZx().log.error("Parameter : pstrId = " + pstrId);
	        }
	        
	        if (getZx().throwException) throw new ZXException(e);
	        return prePersist;
	    } finally {
	        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	        	getZx().trace.returnValue(prePersist);
	            getZx().trace.exitMethod();
	        }
	    }
	}		

	//------------------------ Moved from clsZX object :
	
	/**
	 * Clone a business object (ie do not create a new descriptor).
	 * 
	 * <pre>
	 * 
	 * NOTE : We could just overload Object#clone as this is the default clone dehaviour
	 * NOTE : cloneBO(null,false,false)
	 * </pre>
	 * 
	 * @return Returns a plain clone of the object : 
	 * @throws ZXException Thrown if cloneBO.
	 */
	public ZXBO cloneBO() throws ZXException {
	    return cloneBO(null,false,false);
	}
	
	/**
	 * Clone a business object (ie do not create a new descriptor).
	 * 
	 * <pre>
	 * 
	 * NOTE : cloneBO(pstrCopyGroup,false,false)
	 * </pre>
	 * 
	 * @param pstrCopyGroup group of attributes to copy. Optional, defaults to null or empty 
	 * @return Returns a plain clone of the object : 
	 * @see ZXBO#cloneBO(String, boolean, boolean)
	 * @throws ZXException Thrown if cloneBO.
	 */
	public ZXBO cloneBO(String pstrCopyGroup) throws ZXException {
	    return cloneBO(pstrCopyGroup,false,false);
	}
	
	/**
	 * Clone a business object (ie do not create a new descriptor).
	 * 
	 * @param pstrCopyGroup group of attributes to copy. Optional, defaults to null or empty
	 * @param pblnClonePersistStatus Copy persist status yes / no. Optional, defaults to false
	 * @param pblnCloneFKBO Clone foreign key BOs as well. Optional, defaults to false
	 * @return Returns a ZXBO, or subclass there of?
	 * @see ZXBO#cloneBO(String, boolean, boolean) 
	 * @throws ZXException
	 */
	public ZXBO cloneBO(String pstrCopyGroup, boolean pblnClonePersistStatus, boolean pblnCloneFKBO) throws ZXException {
	    /**
	     * Would be nice if we coul use iBO but this causes
	     * misery when using objects created by cloneBO in ASP pages. 
	     * Amazing enough, in debug mode (ie with VB project running 
	     * in the background, it DOES work!
	     */
	    ZXBO cloneBO = null;

        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrCopyGroup", pstrCopyGroup);
            getZx().trace.traceParam("pblnClonePersistStatus", pblnClonePersistStatus);
            getZx().trace.traceParam("pblnCloneFKBO", pblnCloneFKBO);
        }

        try {
            // Create an object :
            cloneBO = (ZXBO)getZx().createObject( getDescriptor().getClassName() );
            
            cloneBO.init(getDescriptor());
            
            /**
             * Optionally copy attributes
             */
            if (StringUtil.len(pstrCopyGroup) > 0) {
                /**
                 * This should never cause cloneBO to fail but do raise error if it happens : 
                 */
                try {
                    bo2bo(cloneBO, pstrCopyGroup, false);
                } catch (Exception e) {
                    getZx().log.error("Unable to copy (all) attributes values Entity name : " 
                    				  + getDescriptor().getName() + "." + pstrCopyGroup, e);
                }
            }
            
            /**
             * Clone FK BO as well?
             */
            if(pblnCloneFKBO) {
                /**
                 * This should never cause cloneBO to fail but do log an error if it happens
                 */
                try {
                    cloneFKBO(cloneBO);
                } catch (Exception e) {
                   getZx().log.error("Unable to clone FK BO", e); 
                }
            }
            
            /**
             * Mark as new if we are not copying the persist status. 
             */
            if(pblnClonePersistStatus) {
                cloneBO.setPersistStatus(getPersistStatus());
            } else {
                cloneBO.setPersistStatus(zXType.persistStatus.psNew);
            }
            
            /**
             * Straight copy the editenhancers.
             * NOTE : This gets called by createBO, so when handleEnhancers is called it should clears these cached
             * entries.
             */
        	cloneBO.setEditEnhancers(getEditEnhancers());
        	cloneBO.setEditEnhancersDependant(getEditEnhancersDependant());
        	
            return cloneBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Clone a business object", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrCopyGroup = " + pstrCopyGroup);
                getZx().log.error("Parameter : pblnClonePersistStatus = " + pblnClonePersistStatus);
                getZx().log.error("Parameter : pblnCloneFKBO = " + pblnCloneFKBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return cloneBO;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(cloneBO);
                getZx().trace.exitMethod();
            }
        }
	    
	}

	//------------------------ Utility class 
	
    /**
     * Count items by quick search.
     * 
     * <pre>
     * 
     * NOTE : This was part of clsBOS#countByQS
     * </pre>
     * 
     * @param pobjValue The key to use in the where attribute group.
     * @param pstrQSGroup The where attribute group to use.
     * @return Returns the number of rows for a quick search.
     * @throws ZXException Thrown if countByQS fails
     */
    public int countByQS(Property pobjValue, String pstrQSGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjValue", pobjValue);
            getZx().trace.traceParam("pstrQSGroup", pstrQSGroup);
        }

        int countByQS = 0;
        
        DSRS objRS = null;
        
        try {
            /**
             * Count by QS works different for channels as we cannot have the RDBMS do all
             * the hard work
             */
            DSHandler objDSHandler = getDS();
            
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                /**
                 * We can only count for channel handlers that store result-set in collection
                 */
                if (objDSHandler.getRsType().pos == zXType.dsRSType.dsrstCollection.pos) {
                    DSWhereClause objDSWhereClause = new DSWhereClause();
                    
                    if (objDSWhereClause.QSClause(this, 
                                                  pobjValue.getStringValue(), 
                                                  pstrQSGroup).pos != zXType.rc.rcOK.pos) {
                        throw new ZXException("Unable to construct QS where clause");
                    }
                    
                    objRS = objDSHandler.boRS(this, "+", objDSWhereClause.getAsWhereClause());
                    if (objRS == null) {
                        throw new ZXException("Data-source handler failed to get RS by QS group", 
                                              objDSHandler.getName());
                    }
                    
                    countByQS = objRS.getData().size();
                    
                } else {
                    throw new ZXException("Data-source handler does not support count", 
                                          objDSHandler.getName());
                } // result-set type of collection?
                
            } else {
                /**
                 * RDBMS can do all the hard work
                 */
                StringBuffer strQry = new StringBuffer("SELECT COUNT(*) FROM ");
                strQry.append(getZx().sql.tableName(this, zXType.sqlObjectName.sonClause));
                strQry.append(" WHERE ");
                strQry.append(getZx().sql.QSWhereClause(this, pobjValue, pstrQSGroup));
                
                objRS = ((DSHRdbms)objDSHandler).sqlRS(strQry.toString());
                if (objRS == null) {
                    throw new Exception("Unable to execute count query");
                }
                
                countByQS = objRS.getRs().getTarget().getInt(1);
            }
            
            return countByQS;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Count items by quick search ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjValue = " + pobjValue);
                getZx().log.error("Parameter : pstrQSGroup = " + pstrQSGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return countByQS;
        } finally {
            /**
             * Clean up the resultset.
             */
            if (objRS != null) objRS.RSClose();
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(countByQS);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Count number of rows by doing a count(*) with a
     * where clause based on one or more attributes .
     * 
     * <pre>
     * 
     * NOTE : This calls calles : countByGroup(pstrWhereAttributeGroup, false);
     * </pre>
     * 
     * @param pstrWhereAttributeGroup The SQL where group to do the count(*) on.
     * @return Returns the number of rows.
     * @throws ZXException Thrown if countByGroup fails
     * @see ZXBO#countByGroup(String, boolean)
     */
    public int countByGroup(String pstrWhereAttributeGroup) throws ZXException {
        return countByGroup(pstrWhereAttributeGroup, false);
    }
    
    /**
     * Count number of rows by doing a count(*) with a
     * where clause based on one or more attributes .
     * 
     * <pre>
     * 
     * NOTE : We should rather say count(id) than count(*) for performance reasons.
     * </pre>
     *  
     * @param pstrGroup The SQL where group to do the count(*) on.
     * @param pblnNegative Negate all values (i.e. id <> 12 rather than id = 12).
     * 			Optional, default is false.
     * @return Returns the number of rows.
     * @throws ZXException Thrown if countByGroup fails
     */
    public int countByGroup(String pstrGroup, boolean pblnNegative) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pblnNegative", pblnNegative);
        }
        
        int countByGroup = 0;
        DSRS objRS = null;

        try {
            /**
             * Two flavours: channel or database; in case of a channel we do a boRS and count the number
             * of rows returned
             */
            DSHandler objDSHandler = getDS();
            
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                /**
                 * We can only count for channel handlers that store result-set in collection
                 */
                if (objDSHandler.getRsType().pos == zXType.dsRSType.dsrstCollection.pos) {
                    if (pblnNegative) {
                        /**
                         * Negate a bit complex but we abuse dsWhereClause to get the proper where clause
                         */
                        DSWhereClause objDSWhereClause = new DSWhereClause();
                        
                        if(objDSWhereClause.parse(this, pstrGroup, false, false).pos != zXType.rc.rcOK.pos) {
                            throw new ZXException("Unable to construct negated where clause");
                        }
                        
                        pstrGroup = objDSWhereClause.getAsWhereClause();
                    }
                    
                    objRS = objDSHandler.boRS(this, "+", pstrGroup);
                    
                    if (objRS == null) {
                        throw new ZXException("Unable to get BO rs");
                    }
                    
                    countByGroup = objRS.getData().size();
                    
                } else {
                    throw new ZXException("Data-source handler does not support count", 
                                          objDSHandler.getName());
                } // result-set type of collection?
                
            } else {
                /**
                 * In case of a database we can have the RDBMS do all the hard work....
                 */
                StringBuffer strQry = new StringBuffer("SELECT COUNT(*) FROM ");
                strQry.append(getZx().sql.tableName(this, zXType.sqlObjectName.sonClause));
                strQry.append(" WHERE ");
                strQry.append(getZx().sql.whereCondition(this, pstrGroup, !pblnNegative, false));
                
                /**
                 * Execute and get the count :  
                 */
                objRS = ((DSHRdbms)objDSHandler).sqlRS(strQry.toString());
                if(objRS == null) {
                    throw new Exception("Unable to execute count query");
                }
                
                countByGroup = objRS.getRs().getTarget().getInt(1);
            }
            
            return countByGroup;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Count number of rows ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pblnNegative = " + pblnNegative);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return countByGroup;
        } finally {
            /**
             * Clean up resultset.
             */
            if (objRS != null) objRS.RSClose();
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(countByGroup);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Resolve the labels for the attributes with a foreign key. 
     * 
     * <pre>
     * 
     * This can be used when a BO is manually populated instead of using 
     * rs2obj ie : populate
     * 
     *  NOTE : This calls resolveLabels("*")
     * </pre>
     * 
     * @throws ZXException Thrown if resolveLabels fails
     */
    public void resolveLabels() throws ZXException {
        resolveLabels("*");
    }
    
    /**
     * Resolve the labels for the attributes with a foreign key. 
     * 
     * <pre>
     * 
     * This can be used when a BO is manually populated instead of using 
     * rs2obj ie : populate
     * 
     * BD30JUN05 - V1.5:20 - Enhanced FK label behaviour
     * </pre>
     * 
     * @param pstrGroup The attribute group to resolve the labels for. Optional, default should be "*"
     * @throws ZXException Thrown if resolveLabels fails
     */
    public void resolveLabels(String pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        /**
         * Set defaults
         */
        if (pstrGroup == null) {
            pstrGroup = "*";
        }
        
        try {
            /**
             * Get the AttributeCollection for the AttributeGroup
             */
            AttributeCollection colAttr = getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) { throw new Exception("Unable to get retrieve group : " + pstrGroup); }

            Iterator iter = colAttr.iterator();
            Attribute objAttr;
            while (iter.hasNext()) {
                objAttr = (Attribute) iter.next();
                
                if (!StringUtil.isEmpty(objAttr.getForeignKey())) {
                    Property objProperty = getValue(objAttr.getName());
                    if (!objProperty.isNull) {
                    	if (objProperty.resolveFKLabel(false).pos != zXType.rc.rcOK.pos) {
                    		throw new ZXException("Unable to get FK BO for attr " + objAttr.getName() + " fk " + objAttr.getForeignKey());
                    	}
                    	
//                        ZXBO objFKBO = objProperty.getFKBO();
//                        if (objFKBO == null) {
//                            throw new Exception("Unable to get FK BO for attr " + objAttr.getName() + " fk " + objAttr.getForeignKey());
//                        }
//                        
//                        objFKBO.setPKValue(objProperty.getStringValue()); // NOTE : Might need a Property version.
//                        objFKBO.loadBO("label");
//                        objProperty.fkLabel = objFKBO.formattedString("label", true, " ");
                    } // Got handle to property
                    
                } // Has FK
                
            } // Loop over attributes
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Resolve the labels for the attributes with a foreign key.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
            }
            
            if (getZx().throwException) { throw new ZXException(e); }
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * selectSum one or more attributes using the SQL Sum function.
     * 
     * <pre>
     * 
     * Assumes :
     * 		Value of properties will have been set;
     * 		validation will have been de-activated to set
     * </pre>
     * 
     * @param pstrGroup The attribute group to use.
     * @param pstrWhereGroup The where group to use. Optional, default is null
     * @return Returns success or failure.
     * @throws ZXException Thrown if selectAvg fails
     */
    public zXType.rc selectSum(String pstrGroup, String pstrWhereGroup) throws ZXException {
        return sqlFunc("SUM", pstrGroup, pstrWhereGroup);
    }
    
    /**
     * selectMax one or more attributes using the SQL Max function.
     * 
     * <pre>
     * 
     * Assumes :
     * 		Value of properties will have been set;
     * 		validation will have been de-activated to set
     * </pre>
     * 
     * @param pstrGroup The attribute group to use.
     * @param pstrWhereGroup The where group to use. Optional, default is null
     * @return Returns success or failure.
     * @throws ZXException Thrown if selectAvg fails
     */
    public zXType.rc selectMax(String pstrGroup, String pstrWhereGroup) throws ZXException {
        return sqlFunc("MAX", pstrGroup, pstrWhereGroup);
    }    

    /**
     * selectMin one or more attributes using the SQL Min function.
     * 
     * <pre>
     * 
     * Assumes :
     * 		Value of properties will have been set;
     * 		validation will have been de-activated to set
     * </pre>
     * 
     * @param pstrGroup The attribute group to use.
     * @param pstrWhereGroup The where group to use. Optional, default is null
     * @return Returns success or failure.
     * @throws ZXException Thrown if selectAvg fails
     */
    public zXType.rc selectMin(String pstrGroup, String pstrWhereGroup) throws ZXException {
        return sqlFunc("MIN", pstrGroup, pstrWhereGroup);
    }
    
    /**
     * selectAvg one or more attributes using the SQL Avg function.
     * 
     * <pre>
     * 
     * Assumes :
     * 		Value of properties will have been set;
     * 		validation will have been de-activated to set
     * </pre>
     * 
     * @param pstrGroup The attribute group to use.
     * @param pstrWhereGroup The where group to use. Optional, default is null
     * @return Returns success or failure.
     * @throws ZXException Thrown if selectAvg fails
     */
    public zXType.rc selectAvg(String pstrGroup, String pstrWhereGroup) throws ZXException {
        return sqlFunc("AVG", pstrGroup, pstrWhereGroup);
    }
    
    /**
     * Used by min / max / sum / avg.
     * 
     * V1.5:16 - fixed bug when descriptor has alias
     * 
     * @param pstrFunc The sql function of use.
     * @param pstrGroup The attribute group to use.
     * @param pstrWhereGroup The where group to use. Optional, default is null
     * @return Returns success or failure.
     * @throws ZXException Thrown if sqlFunc fails.
     */
    public zXType.rc sqlFunc(String pstrFunc,String pstrGroup, String pstrWhereGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrFunc", pstrFunc);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
        }
        
        zXType.rc sqlFunc = zXType.rc.rcOK;
        
        /**
         * Save the current setting.
         */
        boolean blnValidation = isValidate(); 
        DSRS objRS = null;
        
        try {
            /**
             * Get data-source handler
             */
            DSHandler objDSHandler = getDS();
            
            /**
             * Channel handler simply do not support SQL functions in our world...
             */
            if (objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos) {
                throw new ZXException("SQL functions are not supported by channel data-source handlers", 
                                      objDSHandler.getName());
            }
            
            /**
             * Disable validation.
             */
            setValidate(false);
            
            /**
             * Get the AttributeCollection for the AttributeGroup
             */
            AttributeCollection colAttr = getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) { throw new Exception("Unable to get retrieve group : " + pstrGroup); }
            
            String strTmp = "";
            StringBuffer strSQL = new StringBuffer();
            Attribute objAttr;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute) iter.next();
                strTmp = getZx().sql.columnName(this, objAttr, zXType.sqlObjectName.sonName);
                
                if(StringUtil.len(strTmp) > 0) {
                    if (strSQL.length() > 0) {
                        strSQL.append(',');
                    }
                    strSQL .append(pstrFunc).append('(').append(strTmp).append(')');
                    
                }
            }
            
            if (strSQL.length() == 0)  {
                throw new Exception("No relevant attributes found");
            }
            
            strSQL.insert(0,"SELECT "); // Insert the first part : 
            
            strSQL.append(" FROM ");
            strSQL.append(getZx().sql.tableName(this, zXType.sqlObjectName.sonName));
            strSQL.append(" WHERE (1 = 1) ");
            
            /**
             * And where group
             */
            if (StringUtil.len(pstrWhereGroup) > 0) {
                strTmp = getZx().sql.whereCondition(this, pstrWhereGroup);
                if (StringUtil.len(strTmp) > 0) {
                    strSQL.append(" AND ");
                    strSQL.append(strTmp);
                }
            }
            
            /**
             * Turn into a recordset
             */
            objRS = ((DSHRdbms)objDSHandler).sqlRS(strSQL.toString());
            if(objRS == null) {
                throw new Exception("Failed to get result");
            }
            
            /**
             * Re iterate  :)
             */
            int j = 1;      // The postion in the resultset.
            int intRC = 0;
            
            /**
             * And populate the attributes
             */
            iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                strTmp = getZx().sql.columnName(this, objAttr, zXType.sqlObjectName.sonRSName);
                
                if (!StringUtil.isEmpty(strTmp)) {
                    try {
                        objRS.getRs().setPropertyFromDB(this, objAttr, j);
                        
                    } catch (Exception e) {
                        // We have failed to get the value
                        setValue(objAttr.getName(), "", false, true);
                        
                    }
                    
                    switch (intRC) {
                    case 0:
                        break;
                    case 1:
                        sqlFunc = zXType.rc.rcWarning;
                        break;
                    }
                    
                    j++;
                }
                
            }
            
            return sqlFunc;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : sqlFunc : "  + pstrFunc, e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrFunc = " + pstrFunc);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            sqlFunc = zXType.rc.rcError;
            return sqlFunc;
        } finally {
            /**
             * Restore validation setting
             */
            setValidate(blnValidation);
            
            /**
             * Close resultset
             */
            if (objRS != null) objRS.RSClose();
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(sqlFunc);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Check whether a ZXBO has a table accosiated.
     * 
     * <pre>
     * 
     * Simple function to check whether a BO has a table associated with it.
     * This is used by insertBO, deleteBO etc. to check if a ZXBO can perform any database tasks
     *</pre>
     *
     * @return Returns true is there is no table associated with this ZXBO. 
     */
    public boolean noTable() {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        boolean noTable = false;
        
        try {
            /**
             * Because table is not an optional field, we put a '-'
             *  in the table
             */
            if(StringUtil.len(this.getDescriptor().getTable()) == 0 || this.getDescriptor().getTable().equals("-")) {
                noTable = true;
            }
            
            return noTable;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(noTable);
                getZx().trace.exitMethod();
            }
        }
    }
     
     /**
      * Clone a BO and insert into the database.
      * 
      * <pre>
      * 
      * Assumes   :
      *    Database transaction in progress
      * </pre>
      *
      * @param pstrGroup Group to copy 
      * @return Returns a handle to the clone BO
      * @throws ZXException Thrown if DBClone fails. 
      */
     public ZXBO DBClone(String pstrGroup) throws ZXException{
         if(getZx().trace.isFrameworkCoreTraceEnabled()) {
             getZx().trace.enterMethod();
             getZx().trace.traceParam("pstrGroup", pstrGroup);
         }

         ZXBO DBClone = null;
         
         try {
             /**
              * Create new instance and reset and set automatics
              */
             DBClone = this.cloneBO();
             if (DBClone == null) {
                 throw new Exception("Failed to : Create a clone of ZXBO : " + this.getDescriptor().getName());
             }
             
             DBClone.resetBO();
             
             /**
              * Now copy the fields that we want from the original
              */
             this.bo2bo(DBClone, pstrGroup, false);
             
             /**
              * Now assign new PK
              */
             DBClone.setAutomatics("+");
             
             /**
              * And insert
              */
             DBClone.insertBO();
             
             return DBClone;
         } catch (Exception e) {
             getZx().trace.addError("Failed to : Clone a BO and insert into the database", e);
             if (getZx().log.isErrorEnabled()) {
                 getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
             }
             
             if (getZx().throwException) throw new ZXException(e);
             return DBClone;
         } finally {
             if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                 getZx().trace.returnValue(DBClone);
                 getZx().trace.exitMethod();
             }
         }
     }
     
     /**
      * Clone entities related to a BO; use in conjunction with dbClone.
      * 
      * <pre>
      * 
      * Assumes   :
      *    - Database transaction in progress
      *    - PK of old and new have been set
      * </pre>
      *
      * @param pobjNewBO
      * @param pstrRelatedEntity The related entity to copy 
      * @param pstrFkAttr optional attribute that is FK from entity to anchor group 
      * @param pstrGroup Group to copy. Optional, default should be "*"
      * @param pstrWhereGroup Optional additional where clause (e.g. to exclude non-active elements or so) 
      * @param pstrOrderByGroup Optional orderByGroup to force order of new elements 
      * @return Returns the returns code of the method.
      * @throws ZXException Thrown if DBCloneFK fails. 
      */
     public zXType.rc DBCloneFK(ZXBO pobjNewBO, 
    		 					String pstrRelatedEntity, 
    		 					String pstrFkAttr, 
    		 					String pstrGroup, 
    		 					String pstrWhereGroup, 
    		 					String pstrOrderByGroup) throws ZXException{
         if(getZx().trace.isFrameworkCoreTraceEnabled()) {
             getZx().trace.enterMethod();
             getZx().trace.traceParam("pobjNewBO", pobjNewBO);
             getZx().trace.traceParam("pstrRelatedEntity", pstrRelatedEntity);
             getZx().trace.traceParam("pstrFkAttr", pstrFkAttr);
             getZx().trace.traceParam("pstrGroup", pstrGroup);
             getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
             getZx().trace.traceParam("pstrOrderByGroup", pstrOrderByGroup);
         }

         zXType.rc DBCloneFK = zXType.rc.rcOK;
         
         /**
          * Set defaults
          */
         if (pstrGroup == null) {
             pstrGroup = "*";
         }
         
         try {
             
             /**
              * Get the collection of related entities
              */
             ZXCollection colBO = quickFKCollection(pstrRelatedEntity, pstrFkAttr, null, "*", false, pstrWhereGroup, pstrOrderByGroup, false);
             if (colBO == null) {
                 throw new Exception("Failed to get collection of related entities for " + this.getDescriptor().getName());
             }
             
             /**
              * Create instance of BO that we will need
              */
             ZXBO objNewBO = getZx().createBO(pstrRelatedEntity);
             if (objNewBO == null) {
                 throw new Exception("Failed to create the related entity");
             }
             
             /**
              * Find FKattr
              */
             if (StringUtil.len(pstrFkAttr) == 0) {
                 Attribute objAttr = objNewBO.getFKAttr(this); // FROM.getFKAttr(TO)
                 if (objAttr == null) {
                     throw new Exception("Failed to get foriegn key between : " + objNewBO.getDescriptor().getName() 
                             							+ " and " + this.getDescriptor().getName());
                 }
                 pstrFkAttr = objAttr.getName();
             }
             
             Iterator iter = colBO.iterator();
             ZXBO objBO;
             while(iter.hasNext()) {
                 objBO = (ZXBO)iter.next();
                 
                 /**
                  * Create new instance, copy appropriate fields, assign new PK,
                  * and associate with new anchor
                  */
                 objNewBO.resetBO();
                 objBO.bo2bo(objNewBO, pstrGroup, false);
                 objNewBO.setAutomatics("+");
                 objNewBO.setValue(pstrFkAttr, pobjNewBO.getPKValue());
                 
                 /**
                  * And insert
                  */
                 objNewBO.insertBO();
             }
             
             return DBCloneFK;
             
         } catch (Exception e) {
             getZx().trace.addError("Failed to : Clone entities related to a BO; use in conjunction with dbClone", e);
             if (getZx().log.isErrorEnabled()) {
                 getZx().log.error("Parameter : pstrRelatedEntity = "+ pstrRelatedEntity);
                 getZx().log.error("Parameter : pstrFkAttr = "+ pstrFkAttr);
                 getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
                 getZx().log.error("Parameter : pstrWhereGroup = "+ pstrWhereGroup);
                 getZx().log.error("Parameter : pstrOrderByGroup = "+ pstrOrderByGroup);
             }
             
             if (getZx().throwException) throw new ZXException(e);
             DBCloneFK = zXType.rc.rcError;
             return DBCloneFK;
         } finally {
             if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                 getZx().trace.exitMethod();
             }
         }
     }    
    
    /**
     * Compare two BOs as in 1 xxxx 2.
     * 
     * <pre>
     * 
     * Assumes   :
     *    Null value is always smaller
     * </pre>
     * 
     * @param pobjBO The ZXBO to compare against
     * @param pstrCompareAttributeGroup The attribute group to compare.
     * @return Returns whether the pobjBO
     * @throws ZXException Thrown if  compare fails.
     */
    public int compare(ZXBO pobjBO, String pstrCompareAttributeGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrCompareAttributeGroup", pstrCompareAttributeGroup);
        }
        
        int compare = -1;

        try {
            
            /**
             * Get the AttributeCollection for the AttributeGroup
             */
            AttributeCollection colAttr = pobjBO.getDescriptor().getGroup(pstrCompareAttributeGroup);
            if (colAttr == null) { throw new Exception("Unable to get retrieve group : " + pstrCompareAttributeGroup); }

            /**
             * Start comparing
             */
            Iterator iter = colAttr.iterator();
            Attribute objAttr;
            Property objProperty;
            Property objPropertyCompare;
            while (iter.hasNext()) {
                objAttr = (Attribute) iter.next();
                
                objProperty = getValue(objAttr.getName());
                objPropertyCompare = pobjBO.getValue(objAttr.getName());
                
                compare = objProperty.compareTo(objPropertyCompare);
                if (compare != 0) {
                    // Exit on the first property that is a different size
                    break;
                }
            }
            return compare;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Perform a BO compare", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrCompareAttributeGroup = " + pstrCompareAttributeGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return compare;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(compare);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /** 
     * Standard compareTo.
     * 
     * <pre>
     * 
     * NOTE : This ignore any exceptions thrown in sub call compare 
    * </pre>
     * 
     * @param objBO A ZXBO object to compare against. Note all Object will be casted to ZXBO.
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     * @return the value <code>0</code> if this <code>ZXBO</code> is
     * 		equal to the argument <code>ZXBO</code>; a value less than
     * 		<code>0</code> if this <code>ZXBO</code> is numerically less
     * 		than the argument <code>ZXBO</code>; and a value greater 
     * 		than <code>0</code> if this <code>ZXBO</code> is numerically
     * 		 greater than the argument <code>ZXBO</code> (signed
     * 		 comparison).
     **/
    public int compareTo(Object objBO) {
        try {
            return compare((ZXBO)objBO, "*");
        } catch (ZXException e) {
            // Ignore exceptions !
            return -1;
        }
    } 
    
    /*****
     * 
     * OM - Object model support routines
     * 
     *****/
    
    private Map OM;
    
    /**
     * 
     * @return Returns the oM.
     */
    public Map getOM() {
        if (this.OM == null) {
            this.OM = new ZXCollection();
        }
        return OM;
    }
    
    /**
     * @deprecated This may be removed or reimplemented
     * @param om The oM to set.
     */
    public void setOM(Map om) {
        OM = om;
    }
    
    /**
     * Add entry to object model.
     *
     * @param pstrKey The key uniquely identifying the zxobject in the om collection.
     * @param pobjOMEntry The object to store in the OM, this can be a zxcollection or a zxobject.
     * @deprecated use getOM().put(pstrKey,pobjOMEntry) instead 
     * @return Returns the return code of the method.
     */
    public zXType.rc addToOM(String pstrKey, Object pobjOMEntry) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrKey", pstrKey);
            getZx().trace.traceParam("pobjOMEntry", pobjOMEntry);
        }

        zXType.rc addToOM = zXType.rc.rcOK; 
        
        try {
            /**
             * And add
             */
            getOM().put(pstrKey, pobjOMEntry);
            
            return addToOM;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(addToOM);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get an entry from the object model.
     *
     * @param pstrKey The used to retrieve the om 
     * @return Returns an entry from the object model.
     */
    public Object getFromOM(String pstrKey) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrKey", pstrKey);
        }

        Object getFromOM = null;
        
        try {
            
            getFromOM = getOM().get(pstrKey);
            
            return getFromOM;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(getFromOM);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
	 * Persist an object model.
	 * 
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if persistOM fails.
	 */
	public zXType.rc persistOM() throws ZXException {
		return persistOM(null);
	}
    
    /**
	 * Persist an object model.
	 * 
	 * @param pobjRootBO The root zxobject to persist. If it is a colleciton, it will
	 *            	     persist the collectio.
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if persistOM fails.
	 */
    public zXType.rc persistOM(Object pobjRootBO) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjRootBO", pobjRootBO);
        }

        zXType.rc persistOM = zXType.rc.rcOK;
        
        try {
            ZXBO objBO;
            
            if (pobjRootBO == null) {
                /**
                 * Persist the BO itself
                 */
                persistBO("*");
                
                /**
                 * And persist the OM collection
                 */
                persistOM(getOM());
                
            } else if (pobjRootBO instanceof Map) {
                /**
                 * The object is either a collection or an iBO
                 */
                Iterator iter = ((Map)pobjRootBO).values().iterator();
                while (iter.hasNext()) {
                    objBO = (ZXBO)iter.next();
                    objBO.persistOM(objBO);
                }
                
            } else if (pobjRootBO instanceof List) {
                /**
                 * The object is either a collection or an iBO
                 */
            	List colRootBO = (List)pobjRootBO;
                int intSize = colRootBO.size();
                for (int i = 0; i < intSize; i++) {
                    objBO = (ZXBO)colRootBO.get(i);
                    objBO.persistOM();
                }
                
            } else if (pobjRootBO instanceof ZXBO) {
                /**
                 * Persist this object.
                 */
            	((ZXBO)pobjRootBO).persistOM();
            	
            } else {
                /**
                 * Unsupported object in object model
                 */
            }
            
            return persistOM;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Persist an object model.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjRootBO = "+ pobjRootBO);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            persistOM = zXType.rc.rcError;
            return persistOM;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(persistOM);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add a HAS to the object model.
     *
     * @param pstrEntity The name of the business object to resolve to. 
     * @param pstrGroup The attribute group to load. Optional, default should be "*"
     * @param pstrSort The sort group. Optional, default should be null.
     * @param pblnSortDesc Sort ascending or descending. Optional, default should be false
     * @param pstrKey Group to use to store entries in. Optional, default should be "+"
     * @param pblnResolveFK Whether to resolve foriegn keys. Optional, default should be false
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if resolveHas fails. 
     */
    public zXType.rc resolveHas(String pstrEntity, 
                                String pstrGroup, 
                                String pstrSort, 
                                boolean pblnSortDesc, 
                                String pstrKey, 
                                boolean pblnResolveFK) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrEntity", pstrEntity);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrSort", pstrSort);
            getZx().trace.traceParam("pblnSortDesc", pblnSortDesc);
            getZx().trace.traceParam("pstrKey", pstrKey);
            getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
        }
        
        zXType.rc resolveHas = zXType.rc.rcOK;
        
        /**
         * Set defaults
         */
        if (pstrGroup == null) {
            pstrGroup = "*";
        }
        if (pstrKey == null) {
            pstrKey = "+";
        }
        
        try {
            /**
             * First create an instance of the other entity
             */
            ZXBO objOtherBO = getZx().createBO(pstrEntity);
            if (objOtherBO == null) {
                throw new Exception("Unable to create instance for HAS entity : " + pstrEntity);
            }
            
            /**
             * Find the FK from objBO to
             */
            Attribute objFKAttr = objOtherBO.getFKAttr(this);
            if (objFKAttr == null) {
                throw new Exception("Unable to find FK to base object. Base : " 
                                    + getDescriptor().getName() + " Other : " + objOtherBO.getDescriptor().getName());
            }
            
            ZXCollection colHas = objOtherBO.quickFKCollection(pstrEntity,
                                                               null,
                                                               pstrKey,
                                                               pstrGroup,
                                                               pblnResolveFK,
                                                               objFKAttr.getName(),
                                                               pstrSort, 
                                                               pblnSortDesc);
            if (colHas == null) {
                throw new ZXException("Unable to load related BOs");
            }
            
            /**
             * Now add has collection to object model
             */
            getOM().put(pstrEntity, colHas);
            
            return resolveHas;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Add a HAS to the object model.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrEntity = "+ pstrEntity);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
                getZx().log.error("Parameter : pstrSort = "+ pstrSort);
                getZx().log.error("Parameter : pblnSortDesc = "+ pblnSortDesc);
                getZx().log.error("Parameter : pstrKey = "+ pstrKey);
                getZx().log.error("Parameter : pblnResolveFK = "+ pblnResolveFK);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            resolveHas = zXType.rc.rcError;
            return resolveHas;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(resolveHas);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add objects to object model for a many-to-many relation. 
     * 
     * <pre>
     * 
     * It will add the HAS objects to the object model of BO and each entry has as IS 
     * in their object model
     * </pre>
     *
     * @param pstrHasEntity The has business object name. 
     * @param pstrIsEntity The is business object name. 
     * @param pstrHasGroup Group to retrieve for HAS. 
     * 					  Optional, default should be "*" 
     * @param pstrHasSort Group to sort HAS entries by. 
     * 					 Optional, default is null 
     * @param pstrHasKey Group to use as key to store HAS. 
     * 					Optional, default is "+" 
     * @param pblnHasResolveFK Resolve FK for HAS?. 
     * 						  Optional, default should be false 
     * @param pstrIsGroup Group to retrieve for IS. 
     * 					 Optioanal, default is "*" 
     * @param pblnIsResolveFK Resolve FK for is ?. 
     * 						 Optional, default should be false. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if resolveHasIs fails. 
     */
    public zXType.rc resolveHasIs(String pstrHasEntity, 
                                  String pstrIsEntity, 
                                  String pstrHasGroup, 
                                  String pstrHasSort, 
                                  String pstrHasKey, 
                                  boolean pblnHasResolveFK, 
                                  String pstrIsGroup, 
                                  boolean pblnIsResolveFK) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrHasEntity", pstrHasEntity);
            getZx().trace.traceParam("pstrIsEntity", pstrIsEntity);
            getZx().trace.traceParam("pstrHasGroup", pstrHasGroup);
            getZx().trace.traceParam("pstrHasSort", pstrHasSort);
            getZx().trace.traceParam("pstrHasKey", pstrHasKey);
            getZx().trace.traceParam("pblnHasResolveFK", pblnHasResolveFK);
            getZx().trace.traceParam("pstrIsGroup", pstrIsGroup);
            getZx().trace.traceParam("pblnIsResolveFK", pblnIsResolveFK);
        }

        zXType.rc resolveHasIs = zXType.rc.rcOK;
        DSRS objRS= null;
        
        try {
            /**
             * First create instances of HAS and IS objects
             */
            ZXBO objHasBO = getZx().createBO(pstrHasEntity);
            if (objHasBO == null) {
                throw new Exception("Unable to create HAS object : " + pstrHasEntity);
            }
            
            ZXBO objIsBO = getZx().createBO(pstrIsEntity);
            if (objIsBO == null) {
                throw new Exception("Unable to create Is object : " + pstrIsEntity);
            }
            
            /**
             * We can only use the database join capabilities if a) both entities are either
             * associated with primary or alternative data source and b) with the same data source
             */
            if (getZx().getDataSources().canDoDBJoin(objHasBO, objIsBO)) {
                /**
                 * It is possible to resolve this problem by doing a join
                 */
                DSHRdbms objDSHandler = (DSHRdbms)objHasBO.getDS();
                
                /**
                 * Generate query
                 */
                String strQry = getZx().getSql().hasIsQuery(this, objHasBO, objIsBO,
                        pstrHasGroup, pblnHasResolveFK, pstrIsGroup, pblnIsResolveFK);
                
                /**
                 * Add order by clause
                 */
                if (StringUtil.len(pstrHasSort) > 0) {
                    strQry = strQry + getZx().getSql().orderByClause(objHasBO, pstrHasSort,false);
                }
                
                /**
                 * Turn into recordset
                 */
                objRS = objDSHandler.sqlRS(strQry);
                if (objRS == null) {
                    throw new Exception("Unable to execute query, " + strQry);
                }
                
                ZXCollection colHas = new ZXCollection();
                boolean blnUseKey = StringUtil.len(pstrHasKey) > 0;
                int j = 0;
                
                while (!objRS.eof()) {
                    /**
                     * Create new instance of HAS to store in collection
                     */
                    if (j  > 0) {
                        objHasBO = objHasBO.cloneBO(null, false, true);
                        if (objHasBO == null) {
                            throw new Exception("Unable to clone HAS object");
                        }
                    }
                    
                    objRS.rs2obj(objHasBO, pstrHasGroup, pblnHasResolveFK);
                    
                    /**
                     * And add to has collection
                     */
                    if (blnUseKey) {
                    	/**
                    	 * DGS01JUL2004: This should really use the key as the collection key,
                    	 * and not the formatted string, because that returns the label.
                    	 * WAS: colHas.add objHasBO, zX.BOS.formattedString(objHasBO, pstrHasKey)
                    	 */
                        colHas.put(objHasBO.getValue(pstrHasKey).getStringValue(), objHasBO);
                        
                    } else {
                        colHas.put(new Integer(j), objHasBO);
                    }
                    
                    /**
                     * Now get the IS part
                     */
                    if (j  > 0) {
                        objIsBO = objIsBO.cloneBO(null, false, true);
                        if (objIsBO == null) {
                            throw new Exception("Unable to clone IS object");
                        }
                    }
                    objRS.rs2obj(objIsBO, pstrIsGroup, pblnIsResolveFK);
                    
                    /**
                     * And set to HAS object model
                     */
                    objHasBO.getOM().put(pstrIsEntity, objIsBO);
                    
                    objRS.moveNext();
                    
                    j++;
                } // Loop over each 'has'
                objRS.RSClose();
                
                /**
                 * Now add has collection to object model
                 */
                getOM().put(pstrHasEntity, colHas);
                
            } else {
                /**
                 * The BOs cannot be joined so we have to do it without being able to rely on the Rdbms for all
                 * the hard work.
                 * - Retrieve all the hasBos associated with the base BO (if at all possible)
                 * - For each
                 * - Retrieve the associated isBO
                 */
                ZXCollection colHas = quickFKCollection(pstrHasEntity, 
                                                        null, 
                                                        pstrHasKey, 
                                                        pstrHasGroup,
                                                        pblnHasResolveFK,
                                                        null,
                                                        pstrHasSort, 
                                                        false);
                if (colHas == null) {
                    throw new ZXException("Unable to load related BOs");
                }
                
                /**
                 * Now load and associate the objIs with each item in has collection
                 * Get attribute on HAS that is the FK to base (ie order.client)
                 */
                Attribute objFKAttr = objHasBO.getFKAttr(objIsBO);
                if (objFKAttr == null) {
                    throw new ZXException("Unable to find FK from HAS to IS", 
                                          "Base : " + objHasBO.getDescriptor().getName() + " Other : " + objIsBO.getDescriptor().getName());
                }
                
                ZXBO objItem;
                Iterator iter = colHas.iterator();
                while (iter.hasNext()) {
                    objItem = (ZXBO)iter.next();
                    
                    objItem = objItem.quickFKLoad(objFKAttr.getName(), pstrIsGroup);
                    
                    if (pblnIsResolveFK) {
                        objIsBO.resolveLabels(pstrIsGroup);
                    }
                    
                    objItem.getOM().put(pstrIsEntity, objIsBO);
                }
                
                /**
                 * And add has collection to object model
                 */
                getOM().put(pstrHasEntity, colHas);
                
            }
            
            return resolveHasIs;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Add objects to object model for a many-to-many relation.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrHasEntity = "+ pstrHasEntity);
                getZx().log.error("Parameter : pstrIsEntity = "+ pstrIsEntity);
                getZx().log.error("Parameter : pstrHasGroup = "+ pstrHasGroup);
                getZx().log.error("Parameter : pstrHasSort = "+ pstrHasSort);
                getZx().log.error("Parameter : pstrHasKey = "+ pstrHasKey);
                getZx().log.error("Parameter : pblnHasResolveFK = "+ pblnHasResolveFK);
                getZx().log.error("Parameter : pstrIsGroup = "+ pstrIsGroup);
                getZx().log.error("Parameter : pblnIsResolveFK = "+ pblnIsResolveFK);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            resolveHasIs = zXType.rc.rcError;
            return resolveHasIs;
        } finally {
            /**
             * Close resultset
             */
            if (objRS != null) objRS.RSClose();
            
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(resolveHasIs);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Handle event actions as part of pre- or post persist.
     * 
     * <pre>
     * 
     * V1.5:29 - BD8JUL05 - Added support for ensureLoaded group
     * </pre>
     * 
     * @param penmTiming The time of this action. ie: pre or post action.
     * @param penmPersistAction The persistAction that is being performed.
     * @param pstrGroup The attribute group this action is happening on.
     * @param pstrWhereGroup The where attribute group this actions is happening on.
     * @param pstrId The context under which we are using this event action.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if handleEventActions fails. 
     */
    public zXType.rc handleEventActions(zXType.eaTiming penmTiming, 
                                        zXType.persistAction penmPersistAction, 
                                        String pstrGroup, 
                                        String pstrWhereGroup,
                                        String pstrId) throws ZXException {
        /**
         * Note that this routine, although part of a zX core module,
         * actually uses application level tracing. This is because
         * I believe that developers should want to see this in their
         * trace file even when framework tracing is not activated
         */
        if (getZx().trace.isApplicationTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("penmTiming", penmTiming);
            getZx().trace.traceParam("penmPersistAction", penmPersistAction);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
            getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
            getZx().trace.traceParam("pstrId", pstrId);
        }
        
        zXType.rc handleEventActions = zXType.rc.rcOK;
        ZXBO objOldzXMe = null;
        
        try {
        	/**
             * If the id passed to this method is blank, we use the
             * contextAction, if that is blank as well we ignore this
             * check altogther
             * Also, always lowercase it
             */
            if (StringUtil.len(pstrId) == 0) {
                pstrId = getZx().getActionContext();
            }
            if (StringUtil.len(pstrId) == 0) {
                pstrId = "";
            }
            pstrId = pstrId.toLowerCase();

            /**
             * Check if we really need to get into action
             * - In pre- and postPersist we have already established that
             * there should be at least one applicable eventAction definition
             * so we simply have to find them
             */
            EventAction objEventAction;
            boolean blnStopWhenNotFired = false;

            int intEventActions = getDescriptor().getEventActions().size();
            loopeventactions : for (int i = 0; i < intEventActions; i++) {
                objEventAction = (EventAction) getDescriptor().getEventActions().get(i);

                blnStopWhenNotFired = zXType.eaContinuation.eacStopWhenNotFired.equals(objEventAction.getContinuation());

                if (getZx().isTraceActive()) {
                    getZx().trace.trace("Evaluating " + objEventAction.getName());
                }

                /**
                 * Ignore event actions if 'timing' is wrong (e.g. we are
                 * called from prePersist and action is defined for post....)
                 */
                if (!objEventAction.getTiming().equals(penmTiming)) {
                    if (getZx().isTraceActive()) {
                        getZx().trace.trace("- Ignored because of timing");
                    }
                    
                    //GoTo nextEventActionNotFired
                    if (blnStopWhenNotFired) return handleEventActions;
                    continue loopeventactions;
                }

                /**
                 * Ignore event actions when BO Validation is required but
                 * not set
                 */
                if (objEventAction.isBOValidation() && !isValidate()) {
                    if (getZx().isTraceActive()) {
                        getZx().trace.trace("- Ignored because of BO validation not active");
                    }
                    
                    //GoTo nextEventActionNotFired
                    if (blnStopWhenNotFired) return handleEventActions;
                    continue loopeventactions;
                }

                /**
                 * Action must be defined for this event
                 */
                if (objEventAction.getEvents().charAt(penmPersistAction.pos) == '0') {
                    if (getZx().isTraceActive()) {
                        getZx().trace.trace("- Ignored because not set for event");
                    }
                    
                    //GoTo nextEventActionNotFired
                    if (blnStopWhenNotFired) return handleEventActions;
                    continue loopeventactions;
                }
                
                /**
                 * If an active id is set it must match
                 */
                if (StringUtil.len(objEventAction.getActiveId()) > 0) {
                    /**
                     * If no id is set (also not zX.contextaction, see above)
                     * we ignore this test; in the parse the activeId has
                     * already been converted to lowercase
                     */
                    if (StringUtil.len(pstrId) > 0) {
                        boolean blnMatch = false;
                        /**
                         * Can be one of a number of modes
                         */
                        zXType.eaIDBehaviour enmBehaviour = objEventAction.getIDBehaviour();
                        if (enmBehaviour.equals(zXType.eaIDBehaviour.eaibMatch)) {
                            /**
                             * Must be exact match
                             */
                            blnMatch = (pstrId.equals(objEventAction.getActiveId()));
                            
                        } else if (enmBehaviour.equals(zXType.eaIDBehaviour.eaibContains)) {
                            /**
                             * Must contain somewhere
                             */
                            blnMatch = (pstrId.indexOf(objEventAction.getActiveId()) > 0);

                        } else if (enmBehaviour.equals(zXType.eaIDBehaviour.eaibDoesNotContain)) {
                            /**
                             * Should not contain
                             */
                            blnMatch = (pstrId.indexOf(objEventAction.getActiveId()) == -1);
                            
                        } else if (enmBehaviour.equals(zXType.eaIDBehaviour.eaibStartsWith)) {
                            /**
                             * Must start
                             */
                            blnMatch = (pstrId.startsWith(objEventAction.getActiveId()));

                        } else if (enmBehaviour.equals(zXType.eaIDBehaviour.eaibRegExp)) {
                            /**
                             * Regular expression
                             */
                            /**
                             * We need a dummy test here otherwise the first test fails
                             **/
                            Pattern pattern = Pattern.compile(objEventAction.getActiveId(), Pattern.CASE_INSENSITIVE);
                            Matcher matcher = pattern.matcher(pstrId);
                            blnMatch = matcher.matches();
                            
                        }

                        if (!blnMatch) {
                            if (getZx().isTraceActive()) {
                                getZx().trace.trace("- Ignored because non-matching activeId (" + pstrId + ")");
                            }
                            
                            //GoTo nextEventActionNotFired
                            if (blnStopWhenNotFired) return handleEventActions;
                            continue loopeventactions;
                        }
                        
                    } else {
                        
                        if (getZx().isTraceActive()) {
                            getZx().trace.trace("- Id not evaluated as no id or contextAction available");
                        }
                        
                    } // pstrId not set
                    
                } // Need to check activeId
                
                /**
                 * Check the active group but do not bother when insert or delete when a group
                 * is simply not relevant
                 */
                if (StringUtil.len(objEventAction.getActiveGroup()) > 0 
                        && !penmPersistAction.equals(zXType.persistAction.paInsert)
                        && !penmPersistAction.equals(zXType.persistAction.paDelete)) {
                    
                    zXType.eaGroupBehaviour enmGroupBehaviour = objEventAction.getGroupBehaviour();
                    if (zXType.eaGroupBehaviour.eagbAll.equals(enmGroupBehaviour)) {
                        /**
                         * Group must fully contain the active group
                         */
                        AttributeCollection objGroup = getDescriptor().getGroupMinus(objEventAction.getActiveGroup(), pstrGroup.toString());
                        if (objGroup == null) { 
                            throw new ZXException("Failed to get attribute group : " + objEventAction.getActiveGroup() + "-" + pstrGroup );
                        }
                        
                        if (objGroup.size() > 0) {
                            if (getZx().isTraceActive()) {
                                getZx().trace.trace("- Ignored because group does not contain all activeGroup attributes (" + pstrGroup + ")");
                            }
                            
                            //GoTo nextEventActionNotFired
                            if (blnStopWhenNotFired) return handleEventActions;
                            continue loopeventactions;
                        }
                        
                    } else if (zXType.eaGroupBehaviour.eagbOnly.equals(enmGroupBehaviour)) {
                        /**
                         * The groups must match exactly
                         */
                        AttributeCollection objGroup = getDescriptor().getGroupMinus(objEventAction.getActiveGroup(), pstrGroup.toString());
                        if (objGroup == null) { 
                            throw new ZXException("Failed to get attribute group : " + pstrGroup);
                        }

                        AttributeCollection objActiveGroup = getDescriptor().getGroup(objEventAction.getActiveGroup());
                        if (objActiveGroup == null) { 
                            throw new ZXException("Failed to get attribute group : " + objEventAction.getActiveGroup());
                        }
                        
                        if (objGroup.size() != 0 || objGroup.size() != objActiveGroup.size()) {
                            /**
                             * The active group contains tems that are not in the
                             * group
                             */
                            if (getZx().isTraceActive()) {
                                getZx().trace.trace("- Ignored because group does not contain only activeGroup attributes (" + pstrGroup + ")");
                            }
                            
                            //GoTo nextEventActionNotFired
                            if (blnStopWhenNotFired) return handleEventActions;
                            continue loopeventactions;
                        }

                    } else if (zXType.eaGroupBehaviour.eagbNone.equals(enmGroupBehaviour)) {
                        /**
                         * Group mustn't contain any of the active group
                         */
                        AttributeCollection objGroup = getDescriptor().getGroup(pstrGroup.toString());
                        if (objGroup == null) { 
                            throw new ZXException("Failed to get attribute group : " + pstrGroup);
                        }

                        AttributeCollection objActiveGroup = getDescriptor().getGroupMinus(pstrGroup.toString(), objEventAction.getActiveGroup());
                        if (objActiveGroup == null) { 
                            throw new ZXException("Failed to get attribute group : " + pstrGroup + "-" + objEventAction.getActiveGroup());
                        }

                        if (objGroup.size() != objActiveGroup.size()) {
                            if (getZx().isTraceActive()) {
                                getZx().trace.trace("- Ignored because group contains all or some activeGroup attributes (" + pstrGroup + ")");
                            }
                            
                            //GoTo nextEventActionNotFired
                            if (blnStopWhenNotFired) return handleEventActions;
                            continue loopeventactions;
                        }

                    } else if (zXType.eaGroupBehaviour.eagbSome.equals(enmGroupBehaviour)) {
                        /**
                         * Group must contain some of the active group
                         */
                        AttributeCollection objGroup = getDescriptor().getGroup(pstrGroup.toString());
                        if (objGroup == null) { 
                            throw new ZXException("Failed to get attribute group : " + pstrGroup);
                        }

                        AttributeCollection objActiveGroup = getDescriptor().getGroupMinus(pstrGroup.toString(), objEventAction.getActiveGroup());
                        if (objActiveGroup == null) { 
                            throw new ZXException("Failed to get attribute group : " + pstrGroup + "-" + objEventAction.getActiveGroup());
                        }

                        if (objGroup.size() <= objActiveGroup.size()) {
                            if (getZx().isTraceActive()) {
                                getZx().trace.trace("- Ignored because group does not contain any activeGroup attributes (" + pstrGroup + ")");
                            }
                            
                            //GoTo nextEventActionNotFired
                            if (blnStopWhenNotFired) return handleEventActions;
                            continue loopeventactions;
                        }
                    }
                    
                }

                /**
                 * Next is the where group but ignore on insert as there is no where group
                 */
                if (StringUtil.len(objEventAction.getWhereGroup()) > 0 && !penmPersistAction.equals(zXType.persistAction.paInsert)) {
                    /**
                     * Group must fully contain the active group
                     */
                    AttributeCollection objGroup = getDescriptor().getGroupMinus(objEventAction.getWhereGroup(), pstrWhereGroup);
                    if (objGroup == null) { 
                        throw new ZXException("Failed to get attribute group : " + objEventAction.getWhereGroup() + "-" + pstrWhereGroup);
                    }

                    if (objGroup.size() > 0) {
                        if (getZx().isTraceActive()) {
                            getZx().trace.trace("- Ignored because where groups do not match (" + pstrWhereGroup + ")");
                        }
                        
                        //GoTo nextEventActionNotFired
                        if (blnStopWhenNotFired) return handleEventActions;
                        continue loopeventactions;
                    }
                }

                /**
                 * Next is the notNull group
                 */
                if (StringUtil.len(objEventAction.getNotNullGroup()) > 0) {
                    /**
                     * All attributes in group must have value
                     */
                    AttributeCollection objGroup = getDescriptor().getGroup(objEventAction.getNotNullGroup());
                    if (objGroup == null) { 
                        throw new ZXException("Failed to get attribute group : " + objEventAction.getNotNullGroup());
                    }
                    
                    Attribute objAttr;
                    
                    Iterator iterGroups = objGroup.iterator();
                    while (iterGroups.hasNext()) {
                        objAttr = (Attribute) iterGroups.next();
                        
                        if (getValue(objAttr.getName()).isNull) {
                            if (getZx().isTraceActive()) {
                                getZx().trace.trace("- Ignored because of null value (" + objAttr.getName() + ")");
                            }
                            
                            //GoTo nextEventActionNotFired
                            if (blnStopWhenNotFired) return handleEventActions;
                            continue loopeventactions;
                        }
                    }
                }
                
                /**
                 * And the active expression
                 * For this we insert pobjBO in the BO context as zXMe;
                 * Now it could be that all sorts of cleverness is going on in
                 * the event action and it even triggers event actions for other
                 * entities inw hich case the zXMe entry may be changed; therefore
                 * we have to save a copy of the old version and reset it when done
                 */
                objOldzXMe = getZx().getBOContext().getBO("zXMe");
                getZx().getBOContext().setEntry("zXMe", this);
                if (StringUtil.len(objEventAction.getActive()) > 0) {
                    Property objProperty = getZx().getExpressionHandler().eval(objEventAction.getActive());
                    if (objProperty == null) { return zXType.rc.rcError; }
                    if (!objProperty.booleanValue()) {
                        if (getZx().isTraceActive()) {
                            getZx().trace.trace("- Ignored because of active expression");
                        }
                        
                        //GoTo nextEventActionNotFired
                        if (blnStopWhenNotFired) return handleEventActions;
                        continue loopeventactions;
                    }
                } // Check active
                
                /**
                 * When we get this far we have to 'execute' the
                 * event action
                 */
                if (getZx().isTraceActive()) {
                    getZx().trace.trace("- About to 'execute' event action");
                }
                
                /**
                 * Handle the ensure loaded group
                 */
                if (StringUtil.len(objEventAction.getEnsureLoaded()) > 0) {
	                if (ensureGroupIsLoaded(objEventAction.getEnsureLoaded()).pos != zXType.rc.rcOK.pos) {
	                	throw new ZXException("Unable to load ensureLoaded group from event action " + objEventAction.getName(), 
	                						  "Group: " + objEventAction.getEnsureLoaded());	
	                }
                }
                
                /**
                 * Handle the action
                 * The action is simply an expression that we execute and do not
                 * bother about the result; most likely to be a script that updates
                 * some related entity or so
                 */
                if (StringUtil.len(objEventAction.getAction()) > 0) {
                    if (getZx().getExpressionHandler().eval(objEventAction.getAction()) == null) {
                        if (getZx().isTraceActive()) {
                            getZx().trace.trace("- Evaluation of action failed(" + objEventAction.getAction() + ")");
                        }
                        
                        throw new ZXException("Evaluation of action failed(" + objEventAction.getAction() + ")");
                    }
                    
                } // Has action
                
                /**
                 * Handle the attribute values
                 */
                Tuple objTuple;
                int intAttrValues = objEventAction.getAttrValues().size();
                for (int j = 0; j < intAttrValues; j++) {
                    objTuple = (Tuple) objEventAction.getAttrValues().get(j);
                    
                    /**
                     * Both source and destination are directors; if
                     * the desitination evaluates to null it means that
                     * we do not want to set anything....
                     */
                    String strAttr = getZx().getDirectorHandler().resolve(objTuple.getValue());
                    
                    if (StringUtil.len(strAttr) > 0) {
                        String strValue = getZx().getDirectorHandler().resolve(objTuple.getName());
                        Property objProperty = new StringProperty(strValue, (StringUtil.len(strValue) == 0
                        													 && getDescriptor().getAttribute(strAttr).isOptional())
                        										  );
                        /**
                         * Setting attr value cannot result in premature exit of
                         * this routine
                         * Note that we interpret a blank value as null when
                         * the attribute to set is optional
                         */
                        try {
                            int intRC = setValue(strAttr, objProperty).pos;
                            if (intRC == zXType.rc.rcOK.pos) {
                                if (getZx().isTraceActive()) {
                                    getZx().trace.trace("- Successfuly set " + strAttr + " to '" + strValue + "'");
                                }
                            } else if (intRC == zXType.rc.rcWarning.pos) {
                                if (getZx().isTraceActive()) {
                                    getZx().trace.trace("- Tried to set " + strAttr + " to '" + strValue + "' but raised warning");
                                }
                            } else if (intRC == zXType.rc.rcError.pos) {
                                if (getZx().isTraceActive()) {
                                    getZx().trace.trace("- Could not set " + strAttr + " to '" + strValue + "'");
                                }
                            }
                            
                        } catch (Exception e) {
                        	getZx().log.error("Failed to setter attr. attr=" + strAttr + ", value=" + strValue, e);
                            if (getZx().isTraceActive()) {
                                getZx().trace.trace("- Could not set " + strAttr + " to '" + strValue + "'");
                            }
                        }
                    }
                    
                }

                /**
                 * Get message only if we set returncode to anything other than ok
                 */
                if (objEventAction.getReturnCode().pos != zXType.rc.rcOK.pos) {
                    
                    if (objEventAction.getMsg() != null && objEventAction.getMsg().size() > 0) {
                        /**
                         * Try to get language for preferred language, otherwise
                         * default language
                         */
                        Label objLabel = objEventAction.getMsg().get(getZx().getLanguage());
                        if (objLabel == null) {
                            objLabel = objEventAction.getMsg().get(Environment.DEFAULT_LANGUAGE);
                            if (objLabel == null) { 
                            	// Do tracing and error handling.
                            	return zXType.rc.rcError; 
                            }
                        }
                        
                        String strMsg = objLabel.getLabel();
                        
                        /**
                         * If this is empty try the default language again.
                         */
                        if (StringUtil.len(strMsg) == 0) {
                            strMsg = objEventAction.getMsg().get(Environment.DEFAULT_LANGUAGE).getLabel();
                        }
                        
                        /**
                         * Must treat as director
                         */
                        if (StringUtil.len(strMsg) > 0) {
                            strMsg = getZx().getDirectorHandler().resolve(strMsg);
                        }

                        /**
                         * Also get attribute if available
                         */
                        String strAttr = "";
                        if (StringUtil.len(strMsg) > 0) {
                            strAttr = getZx().getDirectorHandler().resolve(objEventAction.getFocusAttribute());
                        }

                        if (StringUtil.len(strAttr) > 0) {
                            getZx().trace.userErrorAdd(strMsg, null, strAttr);
                        } else {
                            getZx().trace.userErrorAdd(strMsg);
                        }
                        
                    } // Has message to set
                    
                    /**
                     * And save highest return code so far
                     */
                    if (objEventAction.getReturnCode().pos > handleEventActions.pos) {
                        handleEventActions = objEventAction.getReturnCode();
                    }
                    
                } // Get message only if we set returncode to anything other than ok
                
                /**
                 * Handle extend group
                 */
                if (StringUtil.len(objEventAction.getExtendgroup()) > 0) {
                    if (StringUtil.len(pstrGroup) > 0) {
                        pstrGroup = pstrGroup + "," + objEventAction.getExtendgroup();
                    } else {
                        pstrGroup = objEventAction.getExtendgroup();
            		}
                }
                
                /**
                 * We may have to stop now
                 */
                if (objEventAction.getContinuation().equals(zXType.eaContinuation.eacStopWhenFired)) return handleEventActions;
            }
            
            return handleEventActions;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Handle event actions as part of pre- or post persist", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : penmTiming = " + penmTiming);
                getZx().log.error("Parameter : penmPersistAction = " + penmPersistAction);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
                getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
                getZx().log.error("Parameter : pstrId = " + pstrId);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            handleEventActions = zXType.rc.rcError;
            return handleEventActions;
        } finally {
            /**
             * Restore old zXMe again, when necessary.
             */
            if (objOldzXMe != null) {
                getZx().getBOContext().setEntry("zXMe", objOldzXMe);
            }
            
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(handleEventActions);
                getZx().trace.exitMethod();
            }
        }
    }
     
	/**
	 * Sum the values in an attribute group.
	 * 
	 * Assumes   : All non numerics are ignored
	 * 
	 * @param pstrGroup The attribute group to use.
	 * @return Returns a sum of the values in a attribute group.
	 * @throws ZXException Thrown if sumGroup fails.
	 */
	public double sumGroup(String pstrGroup) throws ZXException {
	    double sumGroup = 0;
	     AttributeCollection objGroup = getDescriptor().getGroup(pstrGroup);
	     if (objGroup == null) {
	         throw new ZXException("Failed to get attribute collection");
	     }
	     Attribute objAttr;
	     Iterator iter = objGroup.iterator();
	     while (iter.hasNext()) {
	         objAttr = (Attribute)iter.next();
	         if (objAttr.getDataType().pos == zXType.dataType.dtLong.pos 
                 || objAttr.getDataType().pos == zXType.dataType.dtDouble.pos) {
	             sumGroup = sumGroup + getValue(objAttr.getName()).doubleValue();
	         }
	     }
	     return sumGroup;
	}
	
    /**
     * Validate this BO. Alias for  boValidate(String pstrGroup)
     * 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if boValidate fails. 
     */
    public zXType.rc boValidate() throws ZXException {
        return boValidate(null);
    }
    
    /**
     * Validate this BO. 
     * 
     * @param pstrGroup Optional, default is "*" 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if boValidate fails. 
     */
    public zXType.rc boValidate(String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        zXType.rc boValidate = zXType.rc.rcOK;
        
        /**
         * Handle defaults
         */
        if (pstrGroup == null) {
            pstrGroup = "*";
        }
        
        try {
            
            /**
             * Get the attribute group
             */
            AttributeCollection colAttr = getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) {
                getZx().trace.addError("Attribute group not found", pstrGroup);
                boValidate = zXType.rc.rcError;
                return boValidate;
            }
            
            /**
             * Loop through the attribute group and validate the values by calling setValue
             */
            Attribute objAttr;
            Property objProperty;
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                objProperty = getValue(objAttr.getName());
                
                if (!setValue(objAttr.getName(), objProperty).equals(zXType.rc.rcOK)) {
                    throw new Exception("Failed at attribute name " + objAttr.getName());
                }
            }
            
            return boValidate;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Validate this BO.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            boValidate = zXType.rc.rcError;
            return boValidate;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(boValidate);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Object overloaded
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer toString = new StringBuffer();
        
        if (this.descriptor != null) {
            toString.append(this.descriptor.getLabel().getLabel()).append(" - ")
            			.append(this.descriptor.getName())
            			.append(" (").append(getClass().getName()).append(")");
        } else {
            toString.append(getClass().getName());
        }
        
        return toString.toString();
    }
    
	/**
	 * Count items by quick search.
	 *
     * <pre>
     * 
     * -1: error
     * 0: found none
     * 1: found exactly 1
     * 2: Loaded arbitrary first but found more
     * </pre>
     *
	 * @param pobjValue The search criteria
	 * @param pstrQSGroup Optional, default should by "QS" 
	 * @param pstrLoadGroup Optional, default should be * 
	 * @return Returns count items by quick search.
	 * @throws ZXException Thrown if loadByQS fails. 
	 */
	public int loadByQS(Property pobjValue, String pstrQSGroup, String pstrLoadGroup) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjValue", pobjValue);
			getZx().trace.traceParam("pstrQSGroup", pstrQSGroup);
			getZx().trace.traceParam("pstrLoadGroup", pstrLoadGroup);
		}

		int loadByQS = 0;
        DSHandler objDSHandler;
        DSRS objRS = null;
        
        /**
         * Set defaults
         */
        if (pstrQSGroup == null){
            pstrQSGroup = "QS";
        }
        if (pstrLoadGroup == null) {
            pstrLoadGroup = "*";
        }
        
        try {
		    /**
             * Get data source handler for the BO 
		     */
            objDSHandler = getDS();
            
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            if (objDSWhereClause.QSClause(this, 
                                          pobjValue.getStringValue(), 
                                          pstrQSGroup).pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Unable to construct QS where clause");
            }
            
            objRS = objDSHandler.boRS(this, pstrLoadGroup, objDSWhereClause.getAsWhereClause());
            if (objRS == null) {
                throw new ZXException("Data-source handler failed to get RS by QS group", 
                                      objDSHandler.getName());
            }
            
            if (objRS.eof()) {
                loadByQS = 0;
                
            } else {
                /**
                 * Load the first one we have found
                 */
                objRS.rs2obj(this, pstrLoadGroup);
                
                /**
                 * Skip to see if there is more than one
                 **/
                objRS.moveNext();
                
                if (objRS.eof()) {
                    /**
                     * Was only one
                     */
                    loadByQS = 1;
                    
                } else {
                    /**
                     * More than one
                     */
                    loadByQS = 2;
                    
                } // 1 found
                
            } // None found
            
			return loadByQS;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Count items by quick search.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjValue = "+ pobjValue);
				getZx().log.error("Parameter : pstrQSGroup = "+ pstrQSGroup);
				getZx().log.error("Parameter : pstrLoadGroup = "+ pstrLoadGroup);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return loadByQS;
		} finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(loadByQS);
				getZx().trace.exitMethod();
			}
		}
	}
    
	/**
	 * Return the value of the attribute in the format accepted by data-source where clause.
     * If value is passed (3rd parameter), use that rather than the value of the BO.attr.
	 *
	 * @param pstrAttr The attribute to get the value of. 
	 * @param pobjValue User this value instead of the attribute value. Optional, default should be null. 
	 * @return Return the value of the attribute in the format accepted by data-source where clause
	 * @throws ZXException Thrown if dsWhereClauseValue fails. 
	 */
	public String dsWhereClauseValue(String pstrAttr, Property pobjValue) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrAttr", pstrAttr);
			getZx().trace.traceParam("pobjValue", pobjValue);
		}

		String dsWhereClauseValue = ""; 
		
		try {
            Property objValue;
            
            Attribute objAttr = getDescriptor().getAttribute(pstrAttr);
            if (objAttr == null) {
                throw new ZXException("Failed to get attribute.");
            }
            
            if (pobjValue == null) {
                objValue = getValue(pstrAttr);
            } else {
                objValue = pobjValue;
            } // Use BO value or has value been passed
            
            if (objValue.isNull) {
                dsWhereClauseValue = "#null";
                
            } else {
                int intDataType = objAttr.getDataType().pos;
                if (intDataType == zXType.dataType.dtAutomatic.pos
                    || intDataType == zXType.dataType.dtLong.pos
                    || intDataType == zXType.dataType.dtDouble.pos) {
                    dsWhereClauseValue = objValue.getStringValue();
                } else if (intDataType == zXType.dataType.dtString.pos
                           || intDataType == zXType.dataType.dtExpression.pos) {
                    dsWhereClauseValue = "'" + StringUtil.encodezXString(objValue.getStringValue()) + "'";
                } else if (intDataType == zXType.dataType.dtDate.pos 
                           || intDataType == zXType.dataType.dtTime.pos
                           || intDataType == zXType.dataType.dtTimestamp.pos) {
                    dsWhereClauseValue = "#" + objValue.getStringValue() + "#";
                } else if (intDataType == zXType.dataType.dtBoolean.pos) {
                    if (objValue.booleanValue()) {
                        dsWhereClauseValue = "#true";
                    } else {
                        dsWhereClauseValue = "#false";
                    }
                }
                
            } // Is null?
            
			return dsWhereClauseValue;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Return the value of the attribute in the format accepted by data-source where clause.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pstrAttr = "+ pstrAttr);
				getZx().log.error("Parameter : pobjValue = "+ pobjValue);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			return dsWhereClauseValue;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(dsWhereClauseValue);
				getZx().trace.exitMethod();
			}
		}
	}
    
    //------------------------ New datasource code.
    
    /**
     * Get handle to the datasource handler
     * associated with this descriptor.
     * 
     * @return Returns a handle to the ds.
     * @throws ZXException Thrown if getDS fails.
     */
    public DSHandler getDS() throws ZXException {
        return getDS(true);
    }
    
    /**
	 * Get handle to the datasource handler associated with this descriptor.
	 * 
	 * @param pblnActivate Whether this datasource is already active. Optional, default should be true.
	 * @return Returns a handle to the ds.
	 * @throws ZXException Thrown if getDS fails. Normally if the DSHandler is null.
	 */
	public DSHandler getDS(boolean pblnActivate) throws ZXException {
		DSHandler getDS = null;
		if (this.DSHandler == null) {
			getDS = getZx().getDataSources().getDSByName(getDescriptor().getDataSource(), pblnActivate);
			this.DSHandler = getDS;
		} else {
			if (pblnActivate
					&& this.DSHandler.getState().pos != zXType.dsState.dssActive.pos) {
				if (this.DSHandler.connect().pos != zXType.rc.rcOK.pos) {
					throw new RuntimeException("Unable to connect to data-source handler : " + this.DSHandler.getName());
				}
			}
			getDS = this.DSHandler;
		}

		if (getDS == null) {
			throw new ZXException("Failed to get handle to the DSHandler");
		}

		return getDS;
	}
	
	/**
	 * Set the property persist status of the properties in the given group.
	 * 
	 * <pre>
	 * 
	 * Optionally you can set it only where the current persistStatus is some sort of value.
	 * 
	 * BD11JUL05 - V1.5:30 - Reviewed
	 * </pre>
	 * 
	 * @param pstrGroup The attribute group to update.  
	 * @param penmPersistStatus The new persist status to use.
	 * @param penmWhereStatus Which persist status we should update. Optional, default should be null 
	 * 						  which means update all in the attribute group specified.
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if setPropertyPersistStatus fails. 
	 */
	public zXType.rc setPropertyPersistStatus(String pstrGroup, 
											  zXType.propertyPersistStatus penmPersistStatus,
											  zXType.propertyPersistStatus penmWhereStatus) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrGroup", pstrGroup);
			getZx().trace.traceParam("penmPersistStatus", penmPersistStatus);
			getZx().trace.traceParam("penmWhereStatus", penmWhereStatus);
		}
		
		zXType.rc setPropertyPersistStatus = zXType.rc.rcOK;
		
		try {
			AttributeCollection colAttr = getDescriptor().getGroup(pstrGroup);
			if (colAttr == null) {
				getZx().trace.addError("Unable to get handle to group", pstrGroup);
				setPropertyPersistStatus = zXType.rc.rcError;
				return setPropertyPersistStatus;
			}
			
			Attribute objAttr;						// Loop over variable
			Property objProp;						// Handle to property
			
			Iterator iter = colAttr.values().iterator();
			
			/**
			 * Either set for all attrs in group or only there where the current
			 * status is the whereStatus.
			 * 
			 * BD11JUL05 - V1.5:30 : Do NOT use getAttr to retrieve the property as this will resolve
			 * dynamic values and there is no need for that
			 */
			if (penmWhereStatus == null) {
				while(iter.hasNext()) {
					objAttr = (Attribute)iter.next();
					retrieveProperty(objAttr.getName()).setPersistStatus(penmPersistStatus);
				} // Loop over group
				
			} else {
				while(iter.hasNext()) {
					objAttr = (Attribute)iter.next();
					
					objProp = retrieveProperty(objAttr.getName());
					if (objProp.getPersistStatus().pos == penmWhereStatus.pos) {
						objProp.setPersistStatus(penmPersistStatus);
					}
				} // Loop over group
				
			} // For all attr in group or only where current value is something
			
			return setPropertyPersistStatus;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Set the property persist status of the properties in the given group.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
				getZx().log.error("Parameter : penmPersistStatus = "+ penmPersistStatus);
			}
			if (getZx().throwException) throw new ZXException(e);
			setPropertyPersistStatus = zXType.rc.rcError;
			return setPropertyPersistStatus;
			
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(setPropertyPersistStatus);
				getZx().trace.exitMethod();
			}
		}
	}
	
	/**
	 * Ensure that all attributes in the given group are loaded from the database.
	 *
	 * @param pstrGroup The attribute group to use. 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if ensureGroupIsLoaded fails. 
	 */
	public zXType.rc ensureGroupIsLoaded(String pstrGroup) throws ZXException{
		return ensureGroupIsLoaded(pstrGroup, "+", false);
	}
	
	/**
	 * Ensure that all attributes in the given group are loaded from the database.
	 *
	 * @param pstrGroup The attribute group to use. 
	 * @param pstrWhereGroup Optional, default should be "+". 
	 * @param pblnResolveFK Resolve foriegn key. Optional, default should be false. 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if ensureGroupIsLoaded fails. 
	 */
	public zXType.rc ensureGroupIsLoaded(String pstrGroup, 
									     String pstrWhereGroup, 
									     boolean pblnResolveFK) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrGroup", pstrGroup);
			getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
			getZx().trace.traceParam("pblnResolveFK", pblnResolveFK);
		}

		zXType.rc ensureGroupIsLoaded = zXType.rc.rcOK;
		
		/**
		 * Set defaults
		 */
		if (pstrWhereGroup == null) {
			pstrWhereGroup = "+";
		}
		
		try {
			
			AttributeCollection colAttr = getDescriptor().getGroup(pstrGroup);
			if (colAttr == null) {
				getZx().trace.addError("Unable to get handle to group", pstrGroup);
				ensureGroupIsLoaded = zXType.rc.rcError;
				return ensureGroupIsLoaded;
			}
			
			int intDsType = getDS(false).getDsType().pos;
			Attribute objAttr;
			String strGroup = "";
			
			/**
			 * Determine which attributes still needs to be loaded.
			 */
			Iterator iter = colAttr.values().iterator();
			while(iter.hasNext()) {
				objAttr = (Attribute)iter.next();
				int intPersistStatus = getValue(objAttr.getName()).getPersistStatus().pos;
				
				if (intPersistStatus == zXType.propertyPersistStatus.ppsNew.pos 
					|| intPersistStatus == zXType.propertyPersistStatus.ppsReset.pos) {
					if (StringUtil.len(strGroup) > 0)strGroup = strGroup + ",";
					strGroup = strGroup + objAttr.getName();
					
				} else {
					/**
					 * This seem to happen with Channel datasources.
					 * NOTE : There seems to be a problem when the fk attribute and fkbo id does not match in datatype.
					 */
					if (intDsType == zXType.dsType.dstChannel.pos) {
						if (StringUtil.len(strGroup) > 0)strGroup = strGroup + ",";
						strGroup = strGroup + objAttr.getName();
						
						/**
						 * Print error for now.
						 */
						getZx().log.error("Using cached value : " 
										  + objAttr.getName() + " = " + getValue(objAttr.getName()).getStringValue() 
										  + " loadGroup = " + pstrGroup
										  + " dsType= " + getDS(false).getDsType().getName());
					}
				}
				
			} // Loop over attribute group
			
			if (StringUtil.len(strGroup) > 0) {
				ensureGroupIsLoaded = loadBO(strGroup, pstrWhereGroup, pblnResolveFK);
				/**
				 * Ignore warning.
				 */
				if (ensureGroupIsLoaded.pos == zXType.rc.rcWarning.pos) {
					ensureGroupIsLoaded = zXType.rc.rcOK;
				}
			}

			return ensureGroupIsLoaded;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Ensure that all attributes in the given group are loaded from the database.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
				getZx().log.error("Parameter : pstrWhereGroup = "+ pstrWhereGroup);
				getZx().log.error("Parameter : pblnResolveFK = "+ pblnResolveFK);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			ensureGroupIsLoaded = zXType.rc.rcError;
			return ensureGroupIsLoaded;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(ensureGroupIsLoaded);
				getZx().trace.exitMethod();
			}
		}
	}
	
    /**
     * A collection List edit enhancers, this can be used to override the stylesheet of the attribute.
     * 
     * @param pstrAttr 
     * 
	 * @return the editEnhancers
	 */
	public List getEditEnhancers(String pstrAttr) {
		/**
		 * Common case : No enhancers have been defined exit early.
		 */
		if (getEditEnhancers() == null) {
			return null;
		}
		
		if (StringUtil.len(pstrAttr) > 0) {
			return (List)getEditEnhancers().get(pstrAttr.toLowerCase());
		}
		
		/**
		 * Usage error
		 */
		return null;
	}
	
    /**
     * A collection List edit enhancers, this can be used to override the stylesheet of the attribute.
     * 
     * @param pstrAttr 
     * 
	 * @return the editEnhancers
	 */
	public List getEditEnhancersDependant(String pstrAttr) {
		/**
		 * Common case : No enhancers have been defined exit early.
		 */
		if (getEditEnhancersDependant() == null) {
			return null;
		}
		
		if (StringUtil.len(pstrAttr) > 0) {
			return (List)getEditEnhancersDependant().get(pstrAttr.toLowerCase());
		}
		
		/**
		 * Usage error
		 */
		return null;
	}
	
	/**
	 * @param pstrAttr The name of the attribute that has an enhnancer
	 */
	public void initEditEnhancers(String pstrAttr) {
		if (StringUtil.len(pstrAttr) > 0) {
			
			if (getEditEnhancers() == null) {
				/**
				 * First time call so all must be new
				 */
				setEditEnhancers(new HashMap());
				getEditEnhancers().put(pstrAttr.toLowerCase(), new ArrayList());
				
			} else {
				/**
				 * Maybe the attribute enhancer is new
				 */
				if (getEditEnhancers().get(pstrAttr.toLowerCase()) == null) {
					getEditEnhancers().put(pstrAttr.toLowerCase(), new ArrayList());
				}
				
			}
		}
	}
	
	/**
	 * @param pstrAttr The name of the attribute that has an enhnancer
	 */
	public void initEditEnhancersDependant(String pstrAttr) {
		if (StringUtil.len(pstrAttr) > 0) {
			if (getEditEnhancersDependant() == null) {
				/**
				 * First time call so all must be new
				 */
				setEditEnhancersDependant(new HashMap());
				getEditEnhancersDependant().put(pstrAttr.toLowerCase(), new ArrayList());
				
			} else {
				/**
				 * Maybe the attribute enhancer dependent is new
				 */
				if (getEditEnhancersDependant().get(pstrAttr.toLowerCase()) == null) {
					getEditEnhancersDependant().put(pstrAttr.toLowerCase(), new ArrayList());
				}
			}
			
		}
	}
}