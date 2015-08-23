/*
 * Created on Sep 21, 2004 by Michael
 * $Id: Text.java,v 1.1.2.11 2006/07/17 16:11:16 mike Exp $
 */
package org.zxframework.misc;

import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.util.StringUtil;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;

/**
 * Used for zX Text Messages.
 * 
 * <pre>
 * 
 * Change    : DGS07MAR2005 - V1.4:56
 * Why       : In handleNew, when mandatory text message attributes not given, the error gets
 *              shown twice.
 *              
 * Change    : BD5APR05 - V1:5.1
 * Why       : Added support for data-sources
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class Text extends ZXBO {
    
    //------------------------ Members
	
	private static Log log = LogFactory.getLog(Text.class);
	
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public Text() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    //------------------------ Public Methods
    
    /**
     * Count the number of unread messages available for this user.
     *
     * @return Returns the number of unread messages for this user. 
     * @throws ZXException Thrown if countUnread fails. 
     */
    public int countUnread() throws ZXException{
        return countUnread(null);
    }
    
    /**
     * Count the number of unread messages available for this user.
     *
     * @param pstrUser The user id for whom the messages belong. Optional. 
     * @return Returns the number of unread messages for this user. 
     * @throws ZXException Thrown if countUnread fails. 
     */
    public int countUnread(String pstrUser) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrUser", pstrUser);
        }

        int countUnread = 0; 
        
        try {
            // Default to the current logged in user.
            if (StringUtil.len(pstrUser) == 0) {
                pstrUser = getZx().getUserProfile().getValue("id").getStringValue();
            }
            
            setValue("rcvr", pstrUser);
            
            /**
             * We want rdWhn (Read When) is null in the where clause, so just don't set it
             */
            countUnread = countByGroup("rcvr,rdWhn");
            if (countUnread < -1) {
                throw new Exception("Unable to count instances of zXTxt");
            }
            
            return countUnread;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Count the number of unread messages available for this user.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pstrUser = "+ pstrUser);
            }
            if (getZx().throwException) throw new ZXException(e);
            return countUnread;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(countUnread);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Handle new text message. 
     * 
     * <pre>
     * 
     * This uses a dummy business object that includes attributes for
     * both user and group. We have to create a true zXTxt object and then call the 'send'
     * method to perform the insert(s).
     * 
     * Assumes   :
     *    Required data of me set or loaded
     *    DB transaction is handled outside of here
     * </pre>
     *
     * @return  Returns the return code of the method. @see zXType#rc 
     * @throws ZXException  Thrown if handleNew fails. 
     */
    private zXType.rc handleNew() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc handleNew = zXType.rc.rcOK; 
        try {
            Text objTxt = (Text)getZx().createBO("zXTxt");
            if (objTxt == null) {
                throw new Exception("Failed to created the zXTxt object.");
            }
            
            /**
             * Reset the BO but don't set automatics yet, because we might do it in a loop
             * if generating messages for group
             */
            objTxt.resetBO("*");
            
            /**
             * Copy the relevant attributes from this dummy BO to the real zXTxt. Don't try to
             * copy the receiver because it might be blank in the dummy BO (if user is sending to
             * a group not an individual) and if so the BO2BO will fail on that attr as it is mandatory
             * in zXTxt
             *  
             * TM1.5 (pre 2.0 release) DGS04MAR2005: Same problem with all the mandatory attributes,
             * so only set any of them if not null
             * 
             * Calling  bo2bo twice of non-optional fields causes duplicate error messages.
             */
            if (!getValue("tpe").isNull) {
            	if (bo2bo(objTxt, "tpe").pos != zXType.rc.rcOK.pos) {
            		handleNew = zXType.rc.rcError;
            		return handleNew;
            	}
            }
            if (!getValue("smmry").isNull) {
            	if(bo2bo(objTxt, "smmry").pos != zXType.rc.rcOK.pos) {
            		handleNew = zXType.rc.rcError;
            		return handleNew;
            	}
            }
            if (bo2bo(objTxt, "bdy").pos != zXType.rc.rcOK.pos) {
        		handleNew = zXType.rc.rcError;
        		return handleNew;
            }
            /**
             * See above comment - here we only copy the receiver attribute if it is set
             */
            if (!getValue("rcvr").isNull) {
                if (bo2bo(objTxt, "rcvr").pos != zXType.rc.rcOK.pos) {
                	handleNew = zXType.rc.rcOK;
                	return handleNew;
                }
            }
            
            /**
             * Do not call send if we are missing non-optional fields.
             * 
             * NOTE: This should have been trapped  by the bo validation,
             * but when we redisplay the edit for we turn bo validation off.
             * 
             * The same happens in the vb version but we do not throw an exception then.
             * Also in the java we add exception details to the error stack so this will appear
             * in the error page.
             */
            if ((getValue("usrGrp").isNull && getValue("rcvr").isNull) || getValue("tpe").isNull) {
            	return handleNew;
            }
            
            /**
             * Now call the public 'send' method of the real zXTxt (not of 'me').
             * 
             * This will  expect a partially-populated business object. The user group we pass it might
             * be blank.
             */
            handleNew = objTxt.send(getValue("usrGrp").getStringValue());
            
            return handleNew;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Handle new text message.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            handleNew = zXType.rc.rcError;
            return handleNew;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(handleNew);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Insert a new text message. 
     * 
     * <pre>
     * 
     * If the group is passed in, insert it for every member.
     * 
     * Assumes   :
     *    Required data of me set or loaded
     *    DB transaction is handled outside of here
     * 
     * Reviewed for V1.5:1
     * </pre>
     *
     * @param pstrGroup Optional. 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if send fails. 
     */
    public zXType.rc send(String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        zXType.rc send = zXType.rc.rcOK;
        
        DSRS objRS = null;
        DSHandler objDSHandler = null;
        
        try {
        	
            if (StringUtil.len(pstrGroup) == 0) {
                /**
                 * User group not given so assume we want to create a single text message for
                 * the user whose id must already be in the receiver attribute
                 */
                setAutomatics("+");
            	send = insertBO();
            	return send;
            }
            
            /**
             * Here to create a text message for every user within the given group.
             * First create a recordset to get every member of the group:
             */
            ZXBO objIBOUsrGrpMmbr = getZx().createBO("zXUsrGrpMmbr");
            if (objIBOUsrGrpMmbr == null) {
                throw new Exception("Failed to create zXUsrGrpMmbr");
            }
            
            objDSHandler = getDS();
            
            //objIBOUsrGrpMmbr.setValue("usrGrp", pstrGroup);
            //String strQry = getZx().getSql().loadQuery(objIBOUsrGrpMmbr, "usrPrf");
            //strQry = strQry + " AND " + getZx().getSql().whereCondition(objIBOUsrGrpMmbr, "usrGrp");
            
            objRS = objDSHandler.boRS(objIBOUsrGrpMmbr, "usrPrf", ":usrGrp='" + pstrGroup + "'");
            if (objRS == null) {
                throw new Exception("Unable to execute query to get user group members");
            }
            
            /**
             * Now loop through each member
             */
            while (!objRS.eof()) {
            	objRS.rs2obj(objIBOUsrGrpMmbr, "usrPrf");
                
                /**
                 * Create a message for this user. Needs new id (automatic).
                 */
                setAutomatics("+");
                setValue("rcvr", objIBOUsrGrpMmbr.getValue("usrPrf"));
                insertBO();
                
                objRS.moveNext();
            }
            
            return send;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Insert a new text message.", e);
            if (log.isErrorEnabled()) {
                log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            send = zXType.rc.rcError;
            return send;
        } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
        	
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(send);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Overidden methods.
    
    /** 
     * Allow developer to validate / massage entity before something happens.
     * 
     * @see org.zxframework.ZXBO#postPersist(org.zxframework.zXType.persistAction, String, String, String)
     **/
	public zXType.rc postPersist(zXType.persistAction penmPersistAction, 
	        					 String pstrGroup, 
	        					 String pstrWhereGroup,
						         String pstrId) throws ZXException {
	    if (getZx().trace.isFrameworkTraceEnabled()) {
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
	    	 * Process the generic event actions. 
	    	 */
	        postPersist = super.postPersist(penmPersistAction, pstrGroup, pstrWhereGroup, pstrId);
	        
	        /** 
	         * Only continue if postpersist action passes. 
	         */
	        if (postPersist.pos != zXType.rc.rcError.pos) {
		        String strWhoIs = getDescriptor().getName().toLowerCase();
		        if (strWhoIs.equals("zxtxtnew")) {
		            /**
		             * New text message - insert it (might have to insert for all members of group)
		             */
		            if (penmPersistAction.equals(zXType.persistAction.paProcessEditForm)) {
		                postPersist = handleNew();
		            }
		        }
	        }
	        
	        return postPersist;
	    } catch (Exception e) {
            getZx().trace.addError("Failed to : Do post persist action ", e);
	        if (log.isErrorEnabled()) {
	            log.error("Parameter : penmPersistAction = " + penmPersistAction);
	            log.error("Parameter : pstrGroup = " + pstrGroup);
	            log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
	            log.error("Parameter : pstrId = " + pstrId);
	        }
	        
	        if (getZx().throwException) throw new ZXException(e);
	        return postPersist;
	    } finally {
	        if (getZx().trace.isFrameworkTraceEnabled()) {
	            getZx().trace.exitMethod();
	        }
	    }
	}
}