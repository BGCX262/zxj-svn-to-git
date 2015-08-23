/*
 * Created on Feb 19, 2004
 * $Id: UserProfile.java,v 1.1.2.24 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import java.util.Iterator;
import java.util.Map;

import org.zxframework.datasources.DSHandler;
import org.zxframework.property.Property;
import org.zxframework.util.PasswordService;
import org.zxframework.util.StringUtil;

/**
 * ZX Business object used to store the User preferences and security settings.
 * 
 * <pre>
 * 
 * Change    : BD22MAY03
 * Why       : No longer populate printer1 and printer2 with a list of
 *             available printers; is now a foreign key to zXPrntr
 * 
 * Change    : BD4JUN03
 * Why       : Fixed problem in setPreference: is not name but nme also
 *             has to return rcOK instead of string
 * 
 * Change    : BD30MAR04
 * Why       : And in getPreference....
 * 
 * Change    : BD7MAY04
 * Why       : And in fixed bug in the problem that I fixed...
 * 
 * Change    : DGS01JUL2004
 * Why       : Removed some error logging, so that we can stop repeating ourselves and
 *             can then show the messages to the user.
 *             
 * Change    : BD18APR05
 * Why       : Removed final references to zX.db and made this class data-source aware
 *
 * Change    : V1.4:62 - DGS05APR2005
 * Why       : Trap an exception on database commit
 * 
 * Change    : V1.4:77 - DGS11MAY2005
 * Why       : Groups must terminate with a pipe and be compared to |name| or it falsely
 *             matched a group starting with a similar string.
 * 
 * Change    : DGS28APR2006 - V1.5:95 
 * Why       : Enhanced password encryption (VB only)
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class UserProfile extends ZXBO {
	
    //------------------------ Members
	
    /** A | seperated string of the group permissions of a logged on user. **/
    private String groups;
    /** A collection (Tuple) of user groups. **/
    private Map userGroups;
    
    //------------------------ Constuctor 
    
    /**
     * Default constructor.
     */
    public UserProfile() {
        super();
        
        // Init the userGroups ZXCollection
        setUserGroups(new ZXCollection());
    }  
    
    //------------------------ Getters/Setters
    
    /**
     * A | seperated string of the group permissions of a logged on user.
     * 
     * @return Returns the groups.
     */
    public String getGroups() {
        return this.groups;
    }

    /**
     * @param groups The groups to set.
     */
    public void setGroups(String groups) {
        this.groups = groups;
    }

    /**
     * @param groups The groups to set.
     */
    public void setGroups(Property groups) {
        this.groups = groups.getStringValue();
    }    
    
    /**
     * A collection (Tuple) of user groups.
     * 
     * @return Returns the userGroups.
     */
    public Map getUserGroups() {
        return this.userGroups;
    }
    
    /**
     * @param userGroups The userGroups to set.
     */
    public void setUserGroups(Map userGroups) {
        this.userGroups = userGroups;
    }
    
    //------------------------ Public methods
    
	/**
	 * Added some specialised validation.
	 * 
	 * NOTE : This could not be handled by event action.
	 * 
	 * @see ZXBO#prePersist(org.zxframework.zXType.persistAction, String, String, String)
	 */
	public zXType.rc prePersist(zXType.persistAction penmPersistAction, 
	        						String pstrGroup, 
	        						String pstrWhereGroup, 
	        						String pstrId
	        						) throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	        getZx().trace.enterMethod();
	        getZx().trace.traceParam("penmPersistAction", penmPersistAction);
	        getZx().trace.traceParam("pstrAttributeGroup", pstrGroup);
	        getZx().trace.traceParam("pstrWhereGroup", pstrWhereGroup);
	        getZx().trace.traceParam("pstrId", pstrId);
	    }
	
	    zXType.rc prePersist = zXType.rc.rcOK;
	    
	    try {
	        /**
	         * Call the super to execute the pre persistent script actions.
	         */
	        prePersist = super.prePersist(penmPersistAction, pstrGroup, pstrWhereGroup, pstrId);
	        
	        /**
	         * Make sure to return the highest return code (ie most severe) from
	         * the eventActions and the standard BO prePersist
	         */
	        boolean blnWarning = false;
	        if (prePersist.pos == zXType.rc.rcWarning.pos) {
	            blnWarning = true;
	        }
 	        
	        /**
	         * Do check for passwords : 
	         */
            int intPersistAction = penmPersistAction.pos;
	        if (intPersistAction == zXType.persistAction.paInsert.pos 
                || intPersistAction == zXType.persistAction.paUpdate.pos) {
                
	            /**
	             * newPsswrd and retype Psswrd must be the same
	             */
	            if (!getValue("newPsswrd").getStringValue().equals(getValue("rtpePsswrd").getStringValue()) 
	                && (getValue("newPsswrd").getStringValue() + getValue("rtpePsswrd").getStringValue()).length() > 0) {
	                throw new Exception("New password and retype password do not match");
	            }
	            
	            /**
	             * If in a update the user can choose not
	             * to change his password.
	             */
	            if (intPersistAction == zXType.persistAction.paUpdate.pos) {
	                if (getValue("newPsswrd").isNull) {
	                    return prePersist;
	                }
	            }
	            
	            /**
	             * Check if a password was entered for insert :  
	             */
	            if (intPersistAction == zXType.persistAction.paInsert.pos 
                    && StringUtil.len(getValue("newPsswrd").getStringValue()) == 0) {
	                throw new Exception("No password given");
	            }
	             
	            /**
	             * And ensure that the actual password is set correctly
	             */
                if(!StringUtil.isEmptyTrim(getValue("newPsswrd").getStringValue())) {
                	/**
                	 * Encrypt the password to match the version in the database.
                	 */
                    prePersist = setValue("psswrd", PasswordService.getInstance().encrypt(getValue("newPsswrd").getStringValue()));
                }
	        }
	        
	        if (blnWarning && prePersist.pos == zXType.rc.rcOK.pos) {
	            prePersist = zXType.rc.rcWarning;
	        }
            
	        return prePersist;
	    } catch (Exception e) {
	        getZx().trace.addError("Failed to : Do the UserProfile pre persist action", e);
	        if (getZx().log.isErrorEnabled()) {
	            getZx().log.error("Parameter : penmPersistAction = " + penmPersistAction);
	            getZx().log.error("Parameter : pstrAttributeGroup = " + pstrGroup);
	            getZx().log.error("Parameter : pstrWhereGroup = " + pstrWhereGroup);
	            getZx().log.error("Parameter : pstrId = " + pstrId);
	        }
	        
	        if (getZx().throwException) throw new ZXException(e);
	        return prePersist;
	    } finally {
	        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
	            getZx().trace.exitMethod();
	        }
	    }
	}
	
	/**
	 * connect - Try to retrieve user profile information using given user-id /
	 * password
	 * 
	 * @param pstrUserId The user id
	 * @param pstrPassword The user's password
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if user fails to log in.
	 */
	public zXType.rc connect(String pstrUserId, String pstrPassword) throws ZXException {
	    /**
	     * DGS01JUL2004: Commented out the trace (we already raised an error in 'retrieve')
	     * i.zX.trace.addError CLASS_NAME, ROUTINE_NAME, "Unable to retrieve userprofile information"
	     */
        return retrieve(pstrUserId, pstrPassword).pos != zXType.rc.rcOK.pos ? zXType.rc.rcError:zXType.rc.rcOK;
	}
	
    /**
     * Used by connect (for very first log-in when a password is required) or
     * subsequent connects (ie without a password).
     * 
     * @param pstrUserId User name
     * @param pstrPassword Password. Optional and defaults is null. Only call with
     *                     Password on the first connect
	 * @return Returns the return code of the method.
     * @throws ZXException Thrown if retrieve fails
     */
	public zXType.rc retrieve(String pstrUserId, String pstrPassword) throws ZXException {
	    if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrUserId", pstrUserId);
            getZx().trace.traceParam("pstrPassword", "xxxxx");
        }
	    
	    zXType.rc retrieve = zXType.rc.rcOK;
	    
        try {
            /**
             * Set where clause criteria
             */
            setValue("id", pstrUserId);
            
            if (!StringUtil.isEmpty(pstrPassword)) {
                /** First encrypt  the password  */
                String strHashPassword = PasswordService.getInstance().encrypt(pstrPassword);
                setValue("psswrd", strHashPassword);
            }
            
            /**
             * Try to load
             */
            int intRC = loadBO("*", StringUtil.isEmpty(pstrPassword)?"id":"id,psswrd",  false).pos;
            
            switch (intRC) {
	            case 0: // zXType.rc.rcOk;
	                /**
	                 * Also load the groups
	                 */
	                if (resolveHasIs("zXUsrGrpMmbr", "zXUsrGrp", 
                                      "*", "id", "usrGrp", false,  // Has
                                      "*", false).pos != zXType.rc.rcOK.pos) { // Is
	                    /**
	                     * DGS01JUL2004: Reset the error stack and then add an appropriate message.
	                     * We can then show this to the user. Previously all these messages were
	                     * messy and duplicated, and were never shown to the user. Also, don't go
	                     * to errExit because that adds another error.
	                     */
	                    getZx().trace.resetStack();
	                    
	                    getZx().trace.addError("Unable to retrieve groups");
                        retrieve = zXType.rc.rcWarning;
                        return retrieve;
	                }
	                
	                /**
	                 * Now create seperated string of groups
	                 */
	                this.userGroups = (ZXCollection)getOM().get("zXUsrGrpMmbr");
	                this.groups = "";
	                
	                if (getUserGroups() != null) {
	                	ZXBO objGrpMmbr;
	                	Iterator iter = this.userGroups.values().iterator();
		                while (iter.hasNext()) {
		                    objGrpMmbr = (ZXBO)iter.next();
		                    this.groups =  groups + '|' + objGrpMmbr.getValue("usrGrp").getStringValue();
		                }
		                
	                    /**
	                     * V1.4:77 - DGS11MAY2005: Without this additional line of code it would match
	                     * a group starting with the same name (and see userInGroup function below)
	                     */
	                    groups = groups + '|';
	                }
	                
	                break;
                    
	            case 1:  // zXType.rc.rcWarning;
	                getZx().trace.addError("Invalid user-id / password");
	                retrieve = zXType.rc.rcWarning;
	                return retrieve;
                    
	            case 2: // zXType.rc.rcError;
                    getZx().trace.addError("Unable to retrieve userprofile");
                    retrieve = zXType.rc.rcError;
                    return retrieve;
            }
            
            /**
             * Set the users language based on there offices selected language.
             */
            if (!getValue("zXOffce").isNull) {
                ZXBO objOffce = getZx().quickLoad("zxOffce", getValue("zXOffce"), "", "lngg");
                
                if (objOffce == null) {
                    getZx().trace.addError("Unable to retrieve office");
                    retrieve = zXType.rc.rcError;
                    return retrieve;
                }
                
                getZx().setLanguage(objOffce.getValue("lngg").getStringValue());
            }
            
            return retrieve;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Retrieve account info ? ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrUserId = " + pstrUserId);
                getZx().log.error("Parameter : pstrPassword = xxxx");
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return retrieve;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
	}
	
    /**
     * Check whether user is in given group.
     * 
     * <pre>
     * 
     * NOTE : Used to be called userInGroup
     * </pre>
     * 
     * @param pstrGroup The permission group you want to check to see if the user is
     *                 a member of
     * @return Return true if the user is in the specified group
     */
    public boolean isUserInGroup(String pstrGroup) {
        boolean isUserInGroup = false;
        
        /**
         * Check if there is on accurance of this group in the groups string.
         * 
         * V1.4:77 - DGS11MAY2005: Without this change it would match a group starting with the
         * same name. See also 'retrieve' function above.
         */
        isUserInGroup = (getGroups().indexOf('|' + pstrGroup.toUpperCase() + '|') == -1) ? false : true;
        
        return isUserInGroup;
    }
    
    /**
     * Try to reset user-password.
     * 
     * Assumes : id, current, new and retype have been set database transaction
     * in progress or implied
     * 
	 * @return Returns the return code of the method.
     * @throws ZXException Thrown if resetPassword fails
     */
    public zXType.rc resetPassword() throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
        
        zXType.rc resetPassword = zXType.rc.rcOK;
        DSHandler objDSHandler = null;
        
		try {
		    objDSHandler = getDS();
            
		    /**
		     * Password may be blank
		     */
		    if (getValue("newPsswrd").isNull) {
		        throw new Exception("No new password given");
		    }
		    
		    /**
		     * Check that current password is correct
		     */
	        resetPassword = retrieve(getValue("id").getStringValue(),getValue("crrntPsswrd").getStringValue());
		    if (resetPassword.pos != zXType.rc.rcOK.pos) {
		        getZx().trace.userErrorAdd("No new password given","","crrntPswrd");
		    }
		    
		    /**
		     * Check that the new and retype password are the same
		     */
		    if (!getValue("newPsswrd").getStringValue().equals(getValue("rtpePsswrd").getStringValue())) {
	                getZx().trace.addError("New password and confirmation password are not equal");
	                
	                // GoTo errExit
	                resetPassword = zXType.rc.rcError;
	                return resetPassword;
		    }
		    
		    /**
		     * Should be enough to set password
		     */
            String strHashPassword = PasswordService.getInstance().encrypt(getValue("newPsswrd").getStringValue());
		    setValue("psswrd", strHashPassword);
		    
		    objDSHandler.beginTx();
		    
		    if (updateBO("psswrd").pos != zXType.rc.rcOK.pos) {
		        getZx().trace.addError("Unable to update password");
		        
                // GoTo errExit
                resetPassword = zXType.rc.rcError;
                return resetPassword;
		    }
		    
		    objDSHandler.commitTx();
		    
		    return resetPassword;
		    
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : resetPassword - Try to reset user-password ", e);
            if (objDSHandler != null) {
                objDSHandler.rollbackTx();
            }
            
		    if (getZx().throwException) throw new ZXException(e); 
		    return resetPassword;
		} finally {
		    if(getZx().trace.isFrameworkTraceEnabled()) {
		        getZx().trace.returnValue(resetPassword);
		        getZx().trace.exitMethod();
		    }
		}
    }
    
    /**
     * Delete a user profile.
     * 
     * @throws ZXException Thrown if delete fails
     */
    public void delete() throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
        }
        
        DSHandler objDSHandler = null;
        
		try {
		    /**
		     * Check if allowed
		     */
		    if(!mayDelete()) {
		        throw new Exception("Your credentials do not allow you to delete this row");
		    }
		    
            objDSHandler = getDS();
            
		    /**
		     * Do not allow to delete current user
		     */
		    if ( getValue("id").getStringValue().equals( getZx().getUserProfile().getValue("id").getStringValue() ) ) {
		        throw new Exception("You are not allowed to delete the current user");
		    }
		    
		    /**
		     * Delete any memberships for this user
		     * 
		     * NOTE : zXUsrGrpMmbr has to be of type ZXBO!!
		     */
		    ZXBO objUsrGrpMmbr = getZx().createBO("zXUsrGrpMmbr");
		    if(objUsrGrpMmbr == null) {
		        throw new Exception("Unable to create instance of zXUsrGrpMmbr");
		    }
		    
		    objUsrGrpMmbr.setValue("usrPrf", getValue("id"));
            
		    objDSHandler.beginTx();
            
		    objUsrGrpMmbr.deleteBO("usrPrf");
		    
		    /**
		     * And delete the user
		     */
		    deleteBO();
            
            /**
             * V1.4:62 - DGS05APR2005: In theory this commit can fail, so test for it
             */
		    if (objDSHandler.commitTx().pos != zXType.rc.rcOK.pos) {
		        throw new ZXException("Failed to commit transaction.");
            }
            
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : delete - Delete a user profile", e);
	    	if (objDSHandler != null) {
                objDSHandler.rollbackTx();
            }
            
		    if (getZx().throwException) throw new ZXException(e);
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.exitMethod();
		    }
		}
    }
    
    /**
     * Persist the object model.
     * 
     * @throws ZXException Thrown if persist fails
     */
    public void persist() throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
		try {
		    /**
		     * First persist me. 
		     * Make sure not to update the password if this is not required
		     */
		    String strGroup = null;
		    if( StringUtil.isEmpty(getValue("newPsswrd").getStringValue()) ) {
		        strGroup = "*,-psswrd";
		    } else {
		        strGroup = "*";
		    }
		    
		    persistBO(strGroup);
		    
		    /**
		     * Next the group member collection
		     */
		    Object objPersist = getFromOM("zXUsrGrpMmbr");
		    if (objPersist instanceof ZXCollection) {
		        persistCollection((ZXCollection)objPersist);
                
		    } else {
		        ((ZXBO)objPersist).persistBO("*");
                
		    }
		    
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : persist - Persist the object model ", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.exitMethod();
		    }
		}
		        
    }
    
    /**
     * Retrieve user preference.
     * 
     * @param pstrName The name of the preference you want
     * @return Returns the preference value of the request preference
     * @throws ZXException Thrown if getPreference fails
     */
    public String getPreference(String pstrName) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
        }
        
        String getPreference = null;
        
        try {
            /**
             * Dont bother if name is blank
             */
            if (StringUtil.len(pstrName) == 0) {
                return "";
            }
            
            /**
             * You can only access the super class methods.
             */
            ZXBO objusrPref = getZx().createBO("zXUsrPrfPref", false);
            if (objusrPref == null) {
                throw new RuntimeException("Unable to create instance of zXUsrPrfPref");
            }
            
            /**
             * Try to load from the database
             */
            objusrPref.setValue("usrPrf", getValue("id"));
            objusrPref.setValue("nme", pstrName);
            
            if (objusrPref.loadBO("*", "usrPrf,nme", false).pos != zXType.rc.rcOK.pos) {
                /**
                 * Not really a problem, just no value found
                 */
            	getPreference = "";
            } else {
                getPreference = objusrPref.getValue("vlue").getStringValue();
            }            
            
            return getPreference;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : getPreference - Retrieve user preference", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrName = " + pstrName);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getPreference;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getPreference);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Set user preference.
     * 
     * @param pstrName The name of the preference you want to set
     * @param pstrValue The value you want to set the preference to
     * @throws ZXException Thrown if setPreference fails
     */
    public void setPreference(String pstrName, String pstrValue) throws ZXException {
    	if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pstrValue", pstrValue);
        }
    	
        try {
            /**
             * If name is blank dont bother
             */
            if (StringUtil.len(pstrName) > 0) {
	            /**
	             * You can only access the super class methods.
	             */
	            ZXBO objusrPref = getZx().createBO("zXUsrPrfPref");
	            if (objusrPref == null) {
	                throw new RuntimeException("Unable to create instance of zXUsrPrfPref");
	            }
	            
	            /**
	             * Try to update the database
	             */
	            objusrPref.setValue("usrPrf", getValue("id"));
	            objusrPref.setValue("nme", pstrName);
	            objusrPref.setValue("vlue", pstrValue);
	            
	            if (objusrPref.updateBO("vlue", "usrPrf,nme").pos == zXType.rc.rcWarning.pos) {
	                /**
	                 * Did not exists yet so do an insert
	                 */
	            	objusrPref.setAutomatics("+");
	                objusrPref.insertBO();
	            }
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : setPreference - set a user preference", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrName = " + pstrName);
                getZx().log.error("Parameter : pstrValue = " + pstrValue);
            }
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
}