/*
 * Created on Apr 10, 2004 by Michael Brewer
 * $Id: PFQS.java,v 1.1.2.11 2006/07/17 16:27:57 mike Exp $ 
 */
package org.zxframework.web;

import java.util.Enumeration;

import org.zxframework.ZXObject;
import org.zxframework.zXType;

/**
 * Pageflow quesrystring object.
 * 
 * <pre>
 * 
 * Change    : BD11MAY03
 * Why       : Changed to use the zX quick context rather than a local
 *            collection. Kept interface intact for backward compatibility
 * 
 * Change    : BD17OCT03
 * Why       : On parsing the external query string, do NOT overwrite any
 *             entries that have been added / overwritten since the
 *             pageflow was invoked
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFQS extends ZXObject {

    //------------------------ Members
    
    private Pageflow pageFlow;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFQS() {
        super();
    }
    
    /**
     * Initialise querystring object.
     *
     * @param pobjPageflow The handle to the current pageflow.
     * @return Returns the return code of the method.
     */
	public zXType.rc init(Pageflow pobjPageflow) {
         zXType.rc init = zXType.rc.rcOK; 
         this.pageFlow = pobjPageflow;
         return init;
	}
        
    //------------------------ Public method
    
    /**
     * Parse the querystring from the Servlet request object.
     * 
     * Need to parse the query string stuff properly !!! :(
     * 
     * @deprecated Not longer needed, this is done in ZXTag now.
     * @return Returns the return code of the method.
     */
    public zXType.rc parseQuerystring() {
        zXType.rc parseQuerystring = zXType.rc.rcOK; 
        String paramName;
        Enumeration enm = this.pageFlow.getRequest().getParameterNames();
        while (enm.hasMoreElements()) {
            paramName = (String)enm.nextElement();
            if (getZx().getQuickContext().getStatus(paramName) != 1 && paramName.charAt(0) == '-') {
                getZx().getQuickContext().setEntry(paramName, this.pageFlow.getRequest().getParameter(paramName));
            }
        }
        return parseQuerystring;
    }
    
    /**
     * Add / replace entry in querystring.
     *
     * @param pstrName The name of the context to set
     * @param pstrValue The value to set the context to.
     * @return Returns the return code of the method.
     */
    public zXType.rc setEntry(String pstrName, String pstrValue) {
        zXType.rc setEntry = zXType.rc.rcOK;
        
        /**
         * Set into quick context and set the status to 1 so we can tel the
         * difference between an entry that was copied from the incoming
         * querystring or set manually
         */
        setEntry = getZx().getQuickContext().setEntry(pstrName, pstrValue, 1);
        
        return setEntry;
    }
    
    /**
     * Remove  entry from querystring.
     *
     * @param pstrName  The name of the QuickContext to remove.
     * @return Returns the return code f the method. 
     */
    public zXType.rc removeEntry(String pstrName) {
        zXType.rc removeEntry = zXType.rc.rcOK; 
        
        /**
         * NOTE : Make the name lower case to be case insensetive.
         */
        if (getZx().getQuickContext().getEntries().remove(pstrName.toLowerCase()) == null) {
        	removeEntry = zXType.rc.rcWarning;
        }
        
        return removeEntry;
    }
    
    /**
     * Get value from entry from querystring.
     * 
     * @param pstrName Name of the QuickContext.
     * @return Returns the String form of an QuickContext.
     */
    public String getEntryAsString(String pstrName) {
        return getZx().getQuickContext().getEntry(pstrName);
    }
    
    /**
     * Dump the quickContext details.
     *
     * @return Returns a dump of the contents of the QuickContext.
     */
    public String dump() {
        return getZx().getQuickContext().dump();
    }
}