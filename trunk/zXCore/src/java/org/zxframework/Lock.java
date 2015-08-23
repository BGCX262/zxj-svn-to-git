/*
 * Created on Feb 20, 2004
 * $Id: Lock.java,v 1.1.2.10 2006/07/17 16:40:33 mike Exp $
 */
package org.zxframework;

import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringUtil;

/**
 * Lock - Generic lock class.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 22 March 2003
 * 
 * What  :   BD30MAR04 - V1.5:1
 * Why   :   Support for data-sources
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class Lock extends ZXBO {

    //------------------------ Members
    
    //------------------------ Constuctor 
    
    /**
     * Default constructor.
     */
    public Lock() {
        super();
    }    
    
    //------------------------ Public methods
    
    /**
     * getLock - Try to obtain a lock for a BO.
     * 
     * <pre>
     * 
     * Returns:
     * 	Ok - have lock
     *	Warning - was already locked
     *	Error - severe technical error
     *</pre>
     *
     * @param pstrEntity The entity to check
     * @param pobjPK The property to check
     * @param pstrLockId Optional, default is null
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if getLock fails.
     */
    public zXType.rc getLock(String pstrEntity, Property pobjPK, String pstrLockId) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrEntity", pstrEntity);
        }

        zXType.rc getLock = zXType.rc.rcOK;
        
        DSHRdbms objDSHandler = null;
        
        try {
            /**
             * Reset all values.
             */
            resetBO();
            
            /**
             * Check that lock is not already in place
             */
            if(isLocked(pstrEntity, pobjPK)) {
                /**
                 * Someone else has the lock.
                 */
                getLock = zXType.rc.rcWarning;
                
            } else {
                /**
                 * And create new lock
                 */
                setAutomatics("+");
                
                setValue("lckId", pstrLockId);
                
                objDSHandler = (DSHRdbms)getDS();
                
                /**
                 * Swap database connection
                 */
                objDSHandler.swapConnection();
                
                /**
                 * Mark as a save point :
                 */
                objDSHandler.beginTx();
                
                /**
                 * Add insert
                 */
                insertBO();
                
                objDSHandler.commitTx();
                
                /**
                 * And swap connections again
                 */
                objDSHandler.swapConnection();
            }
            
            return getLock;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : getLock - Try to obtain a lock for a BO ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrEntity = " + pstrEntity);
            }
            
            /**
             * Rollback insert.
             */
            if (objDSHandler != null) {
                if (objDSHandler.isSecondaryInTransaction()) {
                    objDSHandler.rollbackTx();
                }
                
                if (objDSHandler.isSecondaryActive()) {
                    objDSHandler.swapConnection();
                }
            }
            
            if (getZx().throwException) throw new ZXException(e);
            getLock =zXType.rc.rcError;
            return getLock;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(getLock);
                getZx().trace.exitMethod();
            }
        }
    }    
    
	/**
	 * releaseLock - Release a lock on a BO.
	 * 
	 * <pre>
	 * 
	 * Returns: 
	 * 	Ok - lock released 
	 * 	Warning - was not locked or not locked using this id 
	 * 	Error - severe technical error
	 * </pre>
	 * 
	 * @param pstrEntity The entity
	 * @param pobjPK The property
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if releaseLock fails.
	 */
    public zXType.rc releaseLock(String pstrEntity, Property pobjPK) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrEntity", pstrEntity);
            getZx().trace.traceParam("pobjPK", pobjPK);
        }

        zXType.rc releaseLock = zXType.rc.rcOK;
        
        DSHandler objDSHandler = null;
        
        try {
            /**
             * Check that a lock is in place
             */
            if(!isLocked(pstrEntity, pobjPK)) {
                releaseLock = zXType.rc.rcWarning;
            }
            
            /**
             * Set a savepoint
             */
            objDSHandler = getDS();
            
            objDSHandler.beginTx();
            
            /**
             * Delete the lock.
             */
            deleteBO("setLock");
            
            objDSHandler.commitTx();

            return releaseLock;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : releaseLock - Release a lock on a BO ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrEntity = " + pstrEntity);
                getZx().log.error("Parameter : pobjPK = " + pobjPK);
            }
            
            /**
             * Rollback in case of failure.
             */
            if (objDSHandler != null) {
                objDSHandler.rollbackTx();
            }
            
            if (getZx().throwException) throw new ZXException(e);
            releaseLock = zXType.rc.rcError;
            return releaseLock;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(releaseLock);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Check whether a BO is locked.
     * 
     * <pre>
     * 
     * Assumes   :
     * 	true - yes, locked
     * 	false - No, not locked
     * </pre>
     * 
     * @param pstrEntityAttribute The entity attribute to check
     * @param pobjPK The property of the primary key.
     * @return Returns true if it is locked.
     * @throws ZXException Thrown if isLocked fails
     */
    public boolean isLocked(String pstrEntityAttribute, Property pobjPK) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrEntityAttribute", pstrEntityAttribute);
            getZx().trace.traceParam("pobjPK", pobjPK);
        }
        
        boolean isLocked = true;
        
        try {
            
            setValue("entty", new StringProperty(pstrEntityAttribute));
            setValue("pk", pobjPK);
            
            zXType.rc  loadBOrc = loadBO("*", "checkLock", false);
            
            if (loadBOrc == null || loadBOrc.pos == zXType.rc.rcOK.pos) {
                isLocked = true;
            } else {
                isLocked = false;
            }
            
            return isLocked;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Check whether a BO is locked ", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrEntityAttribute = " + pstrEntityAttribute);
                getZx().log.error("Parameter : pobjPK = " + pobjPK);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return isLocked;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(isLocked);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * haveLock - Check whether 'I' have a lock.
     * 
     * <pre>
     * 
     * Assumes : 
     *	true - Yes 'You' have a lock 
     *	false - No, unlocked or locked by someone else
     * </pre>
     * 
     * @param pstrEntity The entity to check
     * @param pobjPK property to check
     * @param pstrLockId Optional, default is null
     * @return Returns true if you do have a lock
     * @throws ZXException Thrown if haveLock fails.
     */
    public boolean haveLock(String pstrEntity, Property pobjPK, String pstrLockId) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrEntity", pstrEntity);
        }

        boolean haveLock = true;

        try {

            if (!isLocked(pstrEntity, pobjPK)) {
                /**
                 * Surely if there is no lock then I wont have it
                 */
                haveLock = false;
            }

            /**
             * If id is given: check this, otherwise compare user-id. 
             * Note that after a isLocked, all my attributes have been set
             */
            if (StringUtil.len(pstrLockId) > 0) {
                if (pstrLockId.equalsIgnoreCase(getValue("lckId").getStringValue())) {
                    haveLock = true;
                } else {
                    haveLock = false;
                }
                
            } else {
                if (getZx().getUserProfile().getValue("id").getStringValue().equalsIgnoreCase(getValue("zXCrtdBy").getStringValue())) {
                    haveLock = true;
                } else {
                    haveLock = false;
                }
            }
            
            return haveLock;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : haveLock - Check whether 'I' have a lock", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrEntity = " + pstrEntity);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return haveLock;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(haveLock);
                getZx().trace.exitMethod();
            }
        }
    }
}