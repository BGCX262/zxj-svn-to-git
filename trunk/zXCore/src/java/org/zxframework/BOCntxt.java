/*
 * Created on Mar 15, 2004 by Michael Brewer
 * $Id: BOCntxt.java,v 1.1.2.12 2006/07/17 15:40:32 mike Exp $
 */
package org.zxframework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.zxframework.util.StringUtil;

/**
 * The framework BO context. Used with store business objects for performance improvements.
 * 
 * <pre>
 * 
 * NOTE : I have made the keys for the entries of this BOContext case insensitive.
 * 
 * Who    : Bertus Dispa
 * When   : 11 May 2003
 * 
 * Change    : BD28AUG03
 * Why       : Do not generate error message when there is no need
 *             in getBO
 * 
 * Change    : BD21OCT03
 * Why       : Added getCreateBO
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class BOCntxt extends ZXObject {

    //------------------------ Members    
    
    private Map entries;
    
    //------------------------ Constuctors

    /**
     * Default constructor.
     */
    public BOCntxt() {
        super();
        
        setEntries(new ZXCollection());
    }
    
    //------------------------ Getters and Setters

    /**
     * The actual context.
     * 
     * A collection (CntxtEntry) of business objects.
     * 
     * @return Returns the entries.
     */
    public Map getEntries() {
        return entries;
    }
    
    /**
     * @param entries The entries to set.
     */
    public void setEntries(Map entries) {
        this.entries = entries;
    }

    //------------------------ Public Methods
    
    /**
     * Clears the context.
     */
    public void clear() {
        setEntries(new ZXCollection());
    }
    
    /**
     * Get status of context entry from context
     *
     * @param pstrName Name of the Entry to get the status of 
     * @return Return the status of the entry. Default is 0.
     */
    public int getStatus(String pstrName) {
        int getStatus = 0; 
        
        /** Lower case to allow for case insensitive keys.  **/
        CntxtEntry objCntxtEntry = (CntxtEntry)getEntries().get(pstrName.toLowerCase());
        if (objCntxtEntry != null) {
            getStatus = objCntxtEntry.getStatus();
        }
        
        return getStatus;
    }
    
    /**
     * Set the status for a quick context entry
     *
     * @param pstrName Name of the entry to set. 
     * @param pintStatus Status of the entry 
     * @return Returns the return code of the method.
     */
    public zXType.rc setStatus(String pstrName, int pintStatus) {
    	zXType.rc setStatus = zXType.rc.rcOK;
    	
        /** Lower case to allow for case insensitive keys.  **/
        CntxtEntry objEntry = (CntxtEntry)entries.get(pstrName.toLowerCase());    
        if(objEntry == null) {
            getZx().trace.addError("Entry not found in quick context : " + pstrName);
            setStatus = zXType.rc.rcError;
            return setStatus;
        }
        
        objEntry.setStatus(pintStatus);
        
        return setStatus;
    }
    
    //------------------------ Wrappable Methods
    
    /**
	 * Add / set an entry in the context. If value is blank then the entry is removed
	 * 
	 * @param pstrName Name of entry
	 * @param pobjValue Value of entry. If value is blank then the entry is removed
     * @return Returns the return code of the method.
	 * @see BOCntxt#setEntry(String, ZXBO, int)
	 */
	public zXType.rc setEntry(String pstrName, ZXBO pobjValue) {
		return setEntry(pstrName, pobjValue, 0);
	}
    
    /**
	 * Add / set an entry in the context. If value is blank than the entry is removed.
     * NOTE: This method does not have tracing for performance reasons
	 * 
	 * @param pstrName Name of entry
	 * @param pobjValue Value of entry. If value is blank then the entry is removed
	 * @param pintStatus Status of entry. Optional, the default should be 0
     * @return Returns the return code of the method.
	 */
    public zXType.rc setEntry(String pstrName, ZXBO pobjValue, int pintStatus) {
    	zXType.rc setEntry = zXType.rc.rcOK;
    	
        /** 
         * Remove entry if value is null or empty. 
         **/    	
        if (pobjValue != null) {
            CntxtEntry objEntry = new CntxtEntry();
            
            objEntry.setName(pstrName);
            objEntry.setValue(pobjValue);
            objEntry.setStatus(pintStatus);
            
            /** 
             * NOTE : The key of the entry is lower case for case insensitivity. 
             **/
            Object oldValue = getEntries().put(pstrName.toLowerCase(), objEntry);
            
            /**
             * Return a warning if we are overiding an existing entry.
             */
            if (oldValue != null) {
            	setEntry = zXType.rc.rcWarning;
            }
            
        } else {
            /** 
             * NOTE : The key of the entry is lower case for case insensitivity. 
             **/
            Object oldValue = getEntries().remove(pstrName.toLowerCase());
            
            /**
             * We failed to remove any entry by this name.
             */
            if (oldValue == null) {
            	setEntry = zXType.rc.rcWarning;
            }
        }
        
        return setEntry;
    }
    
    //------------------------ BO specific
    
    /**
     * Get entry from context
     *
     * @param pstrName Name of the entry 
     * @return Returns the entry by its name.
     */
    public ZXBO getEntry(String pstrName) {
        ZXBO getEntry = null; 
        
        /** Lower case to allow for case insensitive matches **/
        if (StringUtil.len(pstrName) > 0) {
            pstrName = pstrName.toLowerCase();
        }
        
        if (getEntries() != null) {
            CntxtEntry objEntry = (CntxtEntry)getEntries().get(pstrName);
            if (objEntry != null) {
                getEntry =(ZXBO)(objEntry).getObjValue();
            }
        }
        
        return getEntry;
    }

    /**
     * Dump the contents of the context (for debug purposes)
     * 
     * NOTE : This calles this objects toString method.
     * 
     * @return Returns a dump of the QuickContext entries.
     */
    public String dump() {
        StringBuffer dump = new StringBuffer();
        
        CntxtEntry objEntry;
        Iterator iter = getEntries().values().iterator();
        while (iter.hasNext()) {
            objEntry = (CntxtEntry) iter.next();
            
            dump.append(objEntry.getName());
            dump.append(" = ");
            dump.append( ((ZXBO)objEntry.getObjValue()).getDescriptor().getName() );
            dump.append(" [");
            dump.append(objEntry.getStatus());
            dump.append("]\n");
            
            dump.append("\t\t");
            try {
                dump.append( ((ZXBO)objEntry.getObjValue()).formattedString("Label") );
            } catch (ZXException e) {
                getZx().log.error("Failed to : Get the formattedString of the label.", e);
            }
            dump.append("\n");
        }
        
        return dump.toString();
    }
    
    /**
     * Get either the named BO from the context or the first one
     *
     * @param pstrName Name of the BO entry 
     * @return Return a cache business object.
     * @throws ZXException Thrown if getBO fails. 
     */
    public ZXBO getBO(String pstrName) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
        }

        ZXBO getBO = null;
        
        try {
            CntxtEntry objEntry;
            
            if (StringUtil.len(pstrName) > 0) {
                /** Make case insensitive. **/
                pstrName = pstrName.toLowerCase();
                objEntry = (CntxtEntry)getEntries().get(pstrName);
                
            } else {
                // Get the oldest entry in the collection :
                int intSize = getEntries().size();
                if (intSize == 1) {
                    objEntry = (CntxtEntry)getEntries().values().iterator().next();
                } else {
                    // NOTE : This has some overhead so it would be better to refer to the entity by its name.
                    objEntry = (CntxtEntry)new ArrayList(getEntries().values()).get(intSize-1);
                }
                
            }
            
            if (objEntry != null) {
                getBO = (ZXBO)objEntry.getObjValue();
            }
            
            return getBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get either the named BO from the context or the first one", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrName = "+ pstrName);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getBO;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getBO);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get BO from context OR create it.
     * 
     * This means the name can either be the name of an BO in the context 
     * or the entity name. This is convenient for use in expressions and directors
     *
     * @param pstrName Name of the ZXBO to retrieve 
     * @return Return the ZXBO from the context or create a new one. 
     * @throws ZXException  Thrown if getCreateBO fails. 
     */
    public ZXBO getCreateBO(String pstrName) throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
        }
        
        ZXBO getCreateBO = null;
        
        try {
            
            getCreateBO = getBO(pstrName);
            
            if(getCreateBO == null) {
                getCreateBO = getZx().createBO(pstrName);
            }
            
            return getCreateBO;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get BO from context OR create it.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrName = "+ pstrName);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getCreateBO;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getCreateBO);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Object overridden methods 
    
    /** 
     * @see java.lang.Object#toString()
     **/
    public String toString() {
        return StringUtil.stripPackageName(getClass()) + "{" + dump() + "}";
    }
}