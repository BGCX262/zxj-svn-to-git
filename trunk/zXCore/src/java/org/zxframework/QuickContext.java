/*
 * Created on Mar 15, 2004, by Michael Brewer
 * $Id: QuickContext.java,v 1.1.2.11 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import java.util.Iterator;
import java.util.Map;

import org.zxframework.util.StringUtil;

/**
 * The Quick context; this is a very lite weight context that can be used to store strings.
 *  It originates from the pageflow querystring.
 * 
 *<pre>
 * NOTE : The key of the QuickContext is case-insensitive.
 *  
 * Change    : BD12AUG03
 * Why       : Added save / restore feature
 * 
 * Change    : BD17OCT03
 * Why       : Fixed massive bug in getStatus; only set to -1 in errExit
 *             not in okExit
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class QuickContext extends ZXObject {

    //------------------------ Members   
	
    private Map entries;
    private Map savedEntries;
    
    //------------------------ Constructors      
    
    /**
     * Default constructor.
     */
    public QuickContext() {
        super();
        
        setEntries(new ZXCollection());
    }
    
    //------------------------ Getters and Setters

    /**
     * The actual context.
     * 
     * A collection (CntxtEntry) of entries.
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
     * Clear the quick context.
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
     * Add / set an entry in the context. If value is blank than the entry is removed
     * 
     * @param pstrName Name of entry 
     * @param pstrValue Value of entry.If value is blank then the entry is removed  
     * @return Returns the return code of the method
     * @see #setEntry(String, String, int)
     */
    public zXType.rc setEntry(String pstrName, String pstrValue) {
        return setEntry(pstrName, pstrValue, 0);
    }
    
    /**
     * Add / set an entry in the context. If value is blank than the entry is removed.
     * NOTE: This method does not have tracing for performance reasons
     *
     * @param pstrName Name of entry 
     * @param pstrValue Value of entry. If null the context value with the some name will be removed
     * @param pintStatus Status of entry. Optional, the default should be 0 
     * @return Returns the return code of the method.
     */
    public zXType.rc setEntry(String pstrName, String pstrValue, int pintStatus) {
    	zXType.rc setEntry = zXType.rc.rcOK;
    	
    	/** 
         * Remove entry if value is null or empty. 
         **/
        if (StringUtil.len(pstrValue) > 0) {
            CntxtEntry objEntry = new CntxtEntry();
            
            // Keep original casing.
            objEntry.setName(pstrName);
            objEntry.setValue(pstrValue);
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
    
    //------------------------ String Specific
    
    /**
     * Get entry from the context. 
     * 
     * If there is not entry then a empty string will be returned.
     *
     * @param pstrName Name of the entry 
     * @return Returns the entry by its name. Returns an empty string if there is no match.
     */
    public String getEntry(String pstrName) {
    	
        /** NOTE : Lower case to allow for case insensitive matches **/
        if (StringUtil.len(pstrName) > 0) {
            pstrName = pstrName.toLowerCase();
        }
        
        if (getEntries() == null) {
        	return "";
        }
        
        CntxtEntry objCntxtEntry = (CntxtEntry)getEntries().get(pstrName);
        if (objCntxtEntry != null) {
            return objCntxtEntry.getStrValue();
        }
        
        return "";
    }
    
    /**
     * Dump the contents of the context (for debug purposes).
     * 
     * @return Returns a dump of the QuickContext entries.
     */
    public String dump() {
        StringBuffer dump = new StringBuffer();
        CntxtEntry objEntry;
        
        if (this.entries != null) {
            
            Iterator iter = this.entries.values().iterator();
            while (iter.hasNext()) {
                objEntry = (CntxtEntry) iter.next();
                
                dump.append(objEntry.getName());
                dump.append(" = ");
                dump.append(objEntry.getStrValue());
                dump.append(" [");
                dump.append(objEntry.getStatus());
                dump.append("]\n");
            }
        }
        
        return dump.toString();
    }
    
    /**
     * Save current QS.
     * 
     * <pre>
     * 
     * NOTE : This has a lot of overhead as it does a clean save of the collection.
     * </pre>
     */
    public void saveState() {
        // Reset the saved entities :
        this.savedEntries = new ZXCollection();
        
        CntxtEntry objEntry;
        CntxtEntry objCleanEntry;
        
        Iterator iter = getEntries().values().iterator();
        Iterator iterKeys = getEntries().keySet().iterator();
        while (iter.hasNext()) {
            objEntry = (CntxtEntry)iter.next();
            
            objCleanEntry = (CntxtEntry)objEntry.clone();
            
            this.savedEntries.put(iterKeys.next(), objCleanEntry);
        }
    }
    
    /**
     * Restore current Quick Context from the savedEntries.
     * 
     * <pre>
     * 
     * Assumes   :
     *   Fails if no save has been done previously and leaves QS intact
     *</pre>
     *
     * @return Returns the return code of the method.
     */
    public zXType.rc restoreState() {
        zXType.rc restoreState = zXType.rc.rcOK;
        
        if (this.savedEntries == null) {
        	/**
        	 * This indicates a usage error.
        	 */
        	getZx().trace.addError("No save entries to restore from");
        	restoreState = zXType.rc.rcError;
        	return restoreState;
        }
        
        //Pass handle to the saved Entries.
        this.entries = this.savedEntries;
        // Kill off the saved entries handle.
        this.savedEntries = null;  
        
        return restoreState;
    }
    //------------------------ Object overridden methods 
    
    /** 
     * @see java.lang.Object#toString()
     **/
    public String toString() {
        return StringUtil.stripPackageName(getClass()) + "{" + dump() + "}";
    }
}