/*
 * Created on Mar 3, 2004
 * $Id: ZXBOS.java,v 1.1.2.7 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import java.io.StringReader;
import java.util.ArrayList;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import org.zxframework.datasources.DSWhereClause;
import org.zxframework.property.Property;
import org.zxframework.util.StringUtil;

/**
 * The object for Business Object Support.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @see org.zxframework.ZXBO For the changelog for ZXBOS and ZXBO.
 * 
 * @version 0.0.1
 */
public class ZXBOS extends ZXObject {

    // ----------------------------------------------------- Constructor
    
    /**
     * Default constructor.
     */
    public ZXBOS() {
        super();
    }

    // ---------------------------------------------------- Public method
    
	/**
	 * Count the number of attributes in the given attribute groups.
	 * 
	 * <pre>
	 * 
	 * Assumes   :
	 *    group may be blank
	 *    objBO as nothing indicates last BO
	 *    Returns -1 on error
	 * </pre>
	 * 
	 * @param pobjBO A collection of ZXBO to count.
	 * @param pstrAttributeGroup A collection of attribute group each of the ZXBO's
	 * @return Returns the number of groups. -1 on error
	 * @throws ZXException Thrown if countAttr fails
	 */
	public int countAttr(ZXBO[] pobjBO, String[] pstrAttributeGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrAttributeGroup", pstrAttributeGroup);
        }
        
	    int countAttr = 0;
        
        try {
            
            for (int i = 0; i < pobjBO.length; i++) {
                ZXBO objBO = pobjBO[i];
                String strGroup = pstrAttributeGroup[i];
                
                if (objBO == null) break;
                
                if(StringUtil.len(strGroup) > 0) {
                    countAttr = countAttr + objBO.getDescriptor().getGroup(strGroup).size();
                }
            }
            
            return countAttr;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Count the number of attributes in the given attribute groups", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrAttributeGroup = " +  pstrAttributeGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return countAttr;
            
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(countAttr);
                getZx().trace.exitMethod();
            }
        }
	}	
	
	/**
	 * Retrieve an attribute by its ordinal number from an array of business objects.
	 * 
	 * <pre>
	 * This will go through the business objects until it reaches the position selected.
	 * 
	 * NOTE : The arrays passed much much in length.
	 * 
	 * Usage :
	 * getAttrByNumber(pintAttr, new ZXBO[]{objBO}, new String[]{pstrGroup});
	 * </pre>
	 * 
	 * @param pintAttr The number of the postion in the business object that you want to retrieve
	 * @param pobjBO An array of Business objects.
	 * @param pstrGroup An array of attribute groups you want to select a attribute from.
	 * @return Returns attribute by ordinal number. It will return null if the position is greater than the number of attributes
	 * @throws ZXException Thrown if getAttrByNumber fails.
	 */
	public Attribute getAttrByNumber(int pintAttr, ZXBO[] pobjBO, String[] pstrGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pintAttr", pintAttr);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
	    Attribute getAttrByNumber = null;
        
        try {
            
            ArrayList colAttr;
            for (int i = 0; i < pobjBO.length; i++) {
                ZXBO objBO = pobjBO[i];
                
                /**
                 * Assume the business objects are consecutive
                 */
                if (objBO == null) {
                    break;
                }
                
                String strGroup = pstrGroup[i];
                
                /**
                 * Get the AttributeCollection for the AttributeGroup
                 */
                if (objBO.getDescriptor().getGroup(strGroup).getCollection() == null) { 
                    throw new Exception("Unable to get retrieve group : " + strGroup); 
                }
                colAttr = new ArrayList(objBO.getDescriptor().getGroup(strGroup).getCollection());
                
                /**
                 * If the requested ordinal position > #items in collection, move
                 * on to next collection
                 */
                
                if (pintAttr > colAttr.size()) {
                    pintAttr = pintAttr - colAttr.size();
                } else {
                    getAttrByNumber = (Attribute)colAttr.get(pintAttr);
                    break;
                }
            }
            
            return getAttrByNumber;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Retrieve an attribute by its ordinal number ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pintAttr = " + pintAttr);
                getZx().log.error("Parameter : pstrGroup = " + pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getAttrByNumber;
            
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getAttrByNumber);
                getZx().trace.exitMethod();
            }
        }
	}
	
    /**
     * xml2bo - Creates and populates a bo. 
     * 
     * <pre>
     * 
     * If you do have a handle to a business object you want populate you should call xml2bo of that business object.
	 * 
	 * NOTE : This was previously called XML2BO
	 * Assumes : XML has been generated using BO2XML
	 * </pre>
     * 
     * @param pstrXML The xml you want to load.
     * @param pstrAttributeGroup The attributegroup you want to load.
     * @return Returns a populated business object.
     * @throws ZXException Thrown if xml2bo fails.
     */
    public ZXBO xml2bo(String pstrXML, String pstrAttributeGroup) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrXML", pstrXML);
            getZx().trace.traceParam("pstrAttributeGroup", pstrAttributeGroup);
        }
        
        ZXBO xml2bo = null;
        try {
	        /**
	         * Load XML
	         */
	        Element objElement = null;
	        try {
	            Document doc = new SAXBuilder().build(new StringReader(pstrXML));
	            objElement = doc.getRootElement();
                
	        } catch (Exception e) {
	            throw new ZXException("Failed to parse xml : " + pstrXML, e);
	        }
	        
	        /**
	         * Get name of BO
	         */
	        String entityName = objElement.getChildText("entity");
            
	        /**
	         * Create BO :
	         */
	        xml2bo = getZx().createBO(entityName);
            
	        /**
	         * Populate the business object.
	         */
	        xml2bo.xml2bo(objElement, pstrAttributeGroup);
	        
	        return xml2bo;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Create BO based on XML ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrXML = " + pstrXML);
                getZx().log.error("Parameter : pstrAttributeGroup = " + pstrAttributeGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
	        return xml2bo;
            
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
    * Create instance of BO and load it.
    *
    * @param pstrBO The name of the entity. 
    * @param pobjKeyValue Value for key 
    * @return Returns the created and loaded BO. 
    * @throws ZXException Thrown if quickLoad fails. 
     */
    public ZXBO quickLoad(String pstrBO, Property pobjKeyValue) throws ZXException {
        return quickLoad(pstrBO, pobjKeyValue, "", "*");
    }
    
	/**
	* Create instance of BO and load it.
	*
	* @param pstrBO The name of the entity. 
	* @param pobjKeyValue Value for key 
	* @param pstrKeyAttr Key attribute to use. Optional, default is ""
	* @return Returns the created and loaded BO. 
	* @throws ZXException Thrown if quickLoad fails. 
	*/
	public ZXBO quickLoad(String pstrBO, Property pobjKeyValue, String pstrKeyAttr) throws ZXException {
	    return quickLoad(pstrBO, pobjKeyValue, pstrKeyAttr, "*");
	}
        
    /**
    * Create instance of BO and load it.
    *
    * @param pstrBO The name of the entity. 
    * @param pobjKeyValue Value for key 
    * @param pstrKeyAttr Key attribute to use. Optional, default is ""
    * @param pstrLoadGroup Group to load. Optional, default is "*"
    * @return Returns the created and loaded BO. 
    * @throws ZXException Thrown if quickLoad fails. 
    */
    public ZXBO quickLoad(String pstrBO, 
                          Property pobjKeyValue, 
                          String pstrKeyAttr, 
                          String pstrLoadGroup) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrBO", pstrBO);
            getZx().trace.traceParam("pobjKeyValue", pobjKeyValue);
            getZx().trace.traceParam("pstrKeyAttr", pstrKeyAttr);
            getZx().trace.traceParam("pstrLoadGroup", pstrLoadGroup);
        }
        
        ZXBO quickLoad = null; 
        
        /**
         * Set defaults
         */
        if (pstrKeyAttr == null) {
            pstrKeyAttr ="";
        }
        if (pstrLoadGroup == null) {
            pstrLoadGroup = "*";
        }
        
        try {
            /**
             * Create the Business Object :
             */
            quickLoad = getZx().createBO(pstrBO);
            if (quickLoad == null) {
                throw new Exception("Unable to create instance of " +pstrBO);
            }
            
            /**
             * Get the correct primary key.
             */
            if (StringUtil.len(pstrKeyAttr) == 0) {
                pstrKeyAttr = quickLoad.getDescriptor().getPrimaryKey();
            }
            
            quickLoad.setValue(pstrKeyAttr, pobjKeyValue);
            
            try {
                
                quickLoad.loadBO(pstrLoadGroup, pstrKeyAttr, false);
                
            } catch (Exception e) {
                /**
                 * DGS25MAR2004: Don't raise an error. The calling program won't catch it
                 * unless they explicitly test the return for nothing anyway, and often this
                 * can be called when it is not known if the row exists or not, when the error
                 * adds to the log file unnecessarily.
                 */
            }
            
            return quickLoad;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Create instance of BO and load it.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrBO = "+ pstrBO);
                getZx().log.error("Parameter : pobjKeyValue = "+ pobjKeyValue);
                getZx().log.error("Parameter : pstrKeyAttr = "+ pstrKeyAttr);
                getZx().log.error("Parameter : pstrLoadGroup = "+ pstrLoadGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return quickLoad;
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(quickLoad);
                getZx().trace.exitMethod();
            }
        }
    }
	
    /**
     * Check whether this business object matches the criteria.
     * 
     * @param pobjBO The business to look against.
     * @param pobjWhereClause The whereclause
     * @return Returns true if a match.
     * @throws Exception Thrown if findBORecord fails.
     */
    public boolean isMatchingBO(ZXBO pobjBO, DSWhereClause pobjWhereClause) throws Exception {
    	boolean findBORecord = false;
    	
        /**
         * No conditions. Always true.
         */
        if (pobjWhereClause.getTokens().size() == 0) {
            findBORecord = true;
            return findBORecord;
        }
        
    	/**
    	 * The business object we want to check.
    	 * NOTE: What about when we do have the bo context set to a specific value.
    	 */
    	pobjWhereClause.setBaseBO(pobjBO);
    	getZx().getBOContext().setEntry("", pobjBO);
    	
    	/**
    	 * Evaluate the condition to see if it is true.
    	 */
    	zXType.rc rc = pobjWhereClause.evaluate();
    	if (rc.pos == zXType.rc.rcOK.pos) {
    		findBORecord = true;
    	} else {
    		findBORecord = false;
    	}
    	
    	return findBORecord;
    }
}