/*
 * Created on Sep 1, 2004 by michael
 * $Id: Audit.java,v 1.1.2.8 2006/07/17 15:38:31 mike Exp $
 */
package org.zxframework;

import java.util.Iterator;

import org.zxframework.property.Property;
import org.zxframework.util.StringUtil;

/**
 * Data audit business object.
 * 
 * <pre>
 * 
 * Who    : David Swann
 * When   : February 2004
 * 
 * Change: BD6AUG04
 * Why:      Promoted from Domarque to zX
 * 
 * Change:  BD4SEP04 
 * Why:      Was copied from Domarque and was clearly tailored for Domarque.
 *           Now more generic
 * 
 * Change:   BD17JUN05 - V1.5:18
 * Why:      Added support for ensureLoadGroup
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class Audit extends ZXBO {
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public Audit() {
        super();
    }
    
    //------------------------ Public methods.
    
    /**
     * Create an audit row.
     * 
     * <pre>
     *
     * Assumes   :
     *  DB transaction is handled outside of here
     *  
     * BD17JUN05 - V1.5:18 - Added support for enureLoadGroup
     *</pre>
     *
     * @param pobjBO The BO to audit for (ie the anchor entity) 
     * @param pstrAudtTpe The audit type (zXAudtTpe) 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if newMethod fails. 
     */
    public zXType.rc writeAudit(ZXBO pobjBO, String pstrAudtTpe) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrAudtTpe", pstrAudtTpe);
        }
        
        zXType.rc writeAudit = zXType.rc.rcOK; 
        
        try {
            String strTmp;
            
            setAutomatics("+");
            
            setValue("audtTpe", pstrAudtTpe.toUpperCase());
            
            ZXBO objIBOAudtTpe = quickFKLoad("audtTpe");
            if (objIBOAudtTpe == null) {
                /**
                 * No such audit type - assume not to be audited
                 * (may need to rethink this...)
                 */
                if (getZx().log.isInfoEnabled()) {
                    getZx().log.info("No such audit type has been defined yet :" + pstrAudtTpe);
                }
                
                return writeAudit;
            }
            
            /**
             * BD4SEP04 - No use getPK method rather than on relying the PK to be of
             * name id
             */
            setValue("pk", pobjBO.getPKValue());
            
            /**
             * See if we need to load some more
             */
            Property objProp = objIBOAudtTpe.getValue("ensreLdGrp");
            if (!objProp.isNull) {
            	/**
            	 * Can only do this if we have PK
            	 */
            	if (!pobjBO.getPKValue().isNull) {
            		/**
            		 * Ensure load group will not cause a failure
            		 */
            		String strEnsureLoad = objProp.getStringValue();
            		if (StringUtil.len(strEnsureLoad) > 0) {
            			pobjBO.ensureGroupIsLoaded(strEnsureLoad);
            		}
            	}
            }
            
            if (objIBOAudtTpe.getValue("entty").getStringValue().equals(objIBOAudtTpe.getValue("anchrEntty").getStringValue())) {
                setValue("anchrPk", getValue("pk"));
                
            } else {
                if (objIBOAudtTpe.getValue("anchrEnttyExpr").isNull) {
                    ZXBO objIBOAnchr = getZx().createBO(objIBOAudtTpe.getValue("anchrEntty").getStringValue());
                    if (objIBOAnchr == null) {
                        throw new Exception("Failed to create bo :" + objIBOAudtTpe.getValue("anchrEntty").getStringValue());
                    }
                    Attribute objAttr = pobjBO.getFKAttr(objIBOAnchr);
                    if (objAttr == null) {
                        throw new Exception("Failed to get PK attribute from : " + pobjBO.getDescriptor().getName());
                    }
                    setValue("anchrPk", pobjBO.getValue(objAttr.getName()));
                    
                } else {
                    strTmp = getZx().getDirectorHandler().resolve(objIBOAudtTpe.getValue("anchrEnttyExpr").getStringValue());
                    if (StringUtil.isNumeric(strTmp)) {
                        setValue("anchrPk", strTmp);
                    }
                }
            }
            
            /**
             * BD4SEP04 - There was code here to check that strTmp was numeric, but
             * a PK can also be non-numeric
             */
            if (!objIBOAudtTpe.getValue("audtGrp").isNull) {
                strTmp = pobjBO.bo2XML(objIBOAudtTpe.getValue("audtGrp").getStringValue());
                setValue("dscrptn", strTmp);
                
            }
            
            if (getValue("smmry").isNull) {
                setValue("smmry", getZx().getDirectorHandler().resolve(objIBOAudtTpe.getValue("smmry").getStringValue()));
            }
            
            insertBO();
            
            return writeAudit;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Create an audit row.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrAudtTpe = "+ pstrAudtTpe);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            writeAudit = zXType.rc.rcError;
            return writeAudit;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(writeAudit);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Add a group to BOs that contains all the attributes that differ.
     *
     * @param pobjBO1 The one bo 
     * @param pobjBO2 The other BO 
     * @param pstrCompareGroup The group to compare by 
     * @param pstrGroupName The name of the new group 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if createDifferenceGroup fails. 
     */
    public zXType.rc createDifferenceGroup(ZXBO pobjBO1, 
    									   ZXBO pobjBO2, 
    									   String pstrCompareGroup, 
    									   String pstrGroupName) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO1", pobjBO1);
            getZx().trace.traceParam("pobjBO2", pobjBO2);
            getZx().trace.traceParam("pstrCompareGroup", pstrCompareGroup);
            getZx().trace.traceParam("pstrGroupName", pstrGroupName);
        }

        zXType.rc createDifferenceGroup = zXType.rc.rcOK; 
        
        try {
            
            /**
             * MUST be of same type
             */
            if (!pobjBO1.getDescriptor().getName().equals(pobjBO2.getDescriptor().getName())) {
                throw new Exception("Objects MUST be of same entity");
            }
            
            AttributeCollection colAttr = pobjBO1.getDescriptor().getGroup(pstrCompareGroup);
            if (colAttr == null) {
                throw new Exception("Could not get attribute collection for : " + pstrCompareGroup);
            }
            
            AttributeGroup colGroup = new AttributeGroup();
            colGroup.setParsed(true);
            
            Attribute objAttr;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if (pobjBO1.getValue(objAttr.getName()).compareTo(pobjBO2.getValue(objAttr.getName())) != 0) {
                    colGroup.getAttributes().put(objAttr.getName(), objAttr);
                }
            }
            
            /**
             * Look weird to only add it bo1 but remember that descriptors
             * are shared....
             */
            pobjBO1.getDescriptor().getAttributeGroups().put(pstrGroupName, colGroup);
            
            return createDifferenceGroup;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Add a group to BOs that contains all the attributes that differ.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO1 = "+ pobjBO1);
                getZx().log.error("Parameter : pobjBO2 = "+ pobjBO2);
                getZx().log.error("Parameter : pstrCompareGroup = "+ pstrCompareGroup);
                getZx().log.error("Parameter : pstrGroupName = "+ pstrGroupName);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            createDifferenceGroup = zXType.rc.rcError;
            return createDifferenceGroup;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(createDifferenceGroup);
                getZx().trace.exitMethod();
            }
        }
    }
}