/*
 * Created on Feb 20, 2004
 * $Id: Message.java,v 1.1.2.3 2005/08/30 08:40:43 mike Exp $
 */
package org.zxframework;

import org.zxframework.util.StringUtil;

/**
 * Message - The system message class.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 17 February 2003
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class Message extends ZXBO {
	
    //------------------------ Members
    
    //------------------------ Constructors        
    
    /**
     * Default constructor.
     */
    public Message() {
        super();
    }
	
    //------------------------ Public methods.
    
    /**
     * Get system message and replace any occurence of "$1->$10" with corresponding optional parameter.
     *
     * @param pstrId The primary key of the message 
     * @return Returns the message from the array of parameters
     * @throws ZXException Thrown if getMsg fails. 
     */    
    public String getMsg(String pstrId) throws ZXException{
        return getMsg(pstrId, null);
    }
    
    /**
     * Get system message and replace any occurence of "$1->$10" with corresponding optional parameter.
     *
     * @param pstrId The primary key of the message 
     * @param parrParms The params passed. 
     * @return Returns the message from the array of parameters
     * @throws ZXException Thrown if getMsg fails. 
     */
    public String getMsg(String pstrId, String[] parrParms) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrId", pstrId);
            getZx().trace.traceParam("parrParms", parrParms);
        }

        String getMsg = null;
        
        try {
            /**
             * Try to load the appropriate message
             */
            setValue("id", StringUtil.toUpperCase(pstrId));
            
            if (!loadBO().equals(zXType.rc.rcOK)) {
                /**
                 * To avoid risk of endless loop: hardwire message in here
                 */
            	getZx().trace.addError("Unable to find message", pstrId);
            }
            
            /**
             * Get appropriate language
             */
            getMsg = getValue(getZx().getLanguage()).getStringValue();
            
            if (parrParms != null) {
                /**
                 * Replace any occurences of $1 .. $10 or the size of the array :)
                 */
                for (int i = 0; i < parrParms.length; i++) {
                    getMsg = StringUtil.replaceAll(getMsg, "$" + i+1, parrParms[i]);
                }
            }
            
            return getMsg;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get system message.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrId = "+ pstrId);
                getZx().log.error("Parameter : parrParms = "+ parrParms);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getMsg;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(getMsg);
                getZx().trace.exitMethod();
            }
        }
    }
}