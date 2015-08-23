/*
 * Created on Apr 12, 2004 by michael
 * $Id: PFGridCreateUpdate.java,v 1.1.2.20 2006/07/17 16:28:43 mike Exp $
 */
package org.zxframework.web;

import java.util.Iterator;

import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHandler;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;

/**
 * Pageflow grid createUpdate action object.
 * 
 * <pre>
 * 
 * Change    : BD27OCT03
 * Why       : When doing a cloneBO do not clone all fields but
 *             only the relevant ones
 * 
 * Change    : BD16FEB04
 * Why       : Escape names generated for grid related tags
 * 
 * Change    : BD23FEB04
 * Why       : Use selectEditGroup rather than * for cloning
 * 
 * Change    : DGS23FEB04
 * Why       : - Highlight error rows
 *             - Changes to prevent repeating set attr error messages on new rows
 * 
 * Change    : BD9JUN04
 * Why       : Added support for concurrencyControl (rather than auditable) and
 *             limit audit columns to ~ instead of !
 * 
 * Change    : DGS16JUL2004
 * Why       : Collect any success or error messages ready for display by next visible page
 * 
 * Change    : BD16JUL04
 * Why       : Addressed two minor issues
 *             - Switch validation back on before we actually persist
 *             - If something goes wrong on the persist, also mark the
 *               bo so that the related matrixEditform can highlight the
 *               row in error
 *
 * Change    : V1.4:62 - DGS05APR2005
 * Why       : Trap an exception on database commit
 *                    
 * Change    : V1.4:68 - BD15APR05
 * Why       : When a user chooses to ignore a warning, only ignore the warning and do not
 *             ignore errors....
 * 
 * Change    : BD5APR05 - V1.5:1
 * Why       : Support for data-sources
 * 
 * Change    : V1.4:62 - DGS05APR2005
 * Why       : Trap an exception on database commit
 *
 * Change    : V1.4:68 - BD15APR05
 * Why       : When a user chooses to ignore a warning, only ignore the warning and do not
 *               ignore errors....
 *
 * Change    : BD4AUG05 - V1.5:32
 * Why       : Added support for createUpdateActions
 *
 * Change    : BD4AUG05 - V1.5:43
 * Why       : Added support for re-using boContext of edit form
 * 
 * Change    : BD2DEC05 - V1.5:71
 * Why       : Resolve BO context from related edit form before resolving
 *             entities; otherwise you cannot refer to BO context in
 *             entity groups or attribute values
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFGridCreateUpdate extends PFAbstractCreateUpdate {

    //------------------------ Members
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFGridCreateUpdate() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    //------------------------ Implemented Methods from PFAction
    
    /**
     * Handle this action.
     * 
     * <pre>
     * 
     * Reviewed for V1.5:1
     * Reviewed for V1.5:32
     * Reviewed for V1.5:43
     * Reviewed for V1.5:71
     * </pre>
     * 
     * @see PFAction#go() 
     */
    public zXType.rc go() throws ZXException {
		if(getZx().trace.isApplicationTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
		zXType.rc go = zXType.rc.rcOK;
		int intRC = zXType.rc.rcOK.pos;

		try {
		    
		    /**
		     * Get handle to the linked editForm action and actual action implementation
		     */
		    PFAction objLinkedEditFormAction = getPageflow().getPFDesc().getAction(getLinkededitform());
		    if (objLinkedEditFormAction == null) {
		        throw new Exception("LinkedEditForm action not found : " + getLinkededitform());
		    }
		    
		    /**
		     * Determine how progress stored in the query string
		     */
		    String strQSProgress = (StringUtil.len(objLinkedEditFormAction.getQsprogress()) == 0 ? "-prgs" : objLinkedEditFormAction.getQsprogress());
		    
		    /**
		     * Determine name in escaped format
		     */
		    String strEscapedActionName =StringEscapeUtils.escapeHTMLTag(objLinkedEditFormAction.getName());
		    
		    /**
		     * Check if we are already in progress
		     */
		    boolean blnInProgress = (StringUtil.len(getPageflow().getQs().getEntryAsString(strQSProgress)) > 0);
		    if (blnInProgress) {
		        // ?? Wait..
            }
		    
		    /**
		     * Perhaps we also have to set the BO context using the related edit form
		     * BO context definition
		     */
		    if (isUseeditbocontext()) {
		    	getPageflow().processBOContexts(objLinkedEditFormAction);
		    }
		    
		    /**
		     * Get entities
		     */
		    ZXCollection colEntities = getPageflow().getEntityCollection(this,
                                                                         zXType.pageflowActionType.patGridCreateUpdate,
                                                                         zXType.pageflowQueryType.pqtSearchForm, 
                                                                         "");
		    if (colEntities == null) {
		        throw new Exception("Unable to retrieve entity collection");
		    }
		    
		    /**
		     * Set context variable
		     */
		    getPageflow().setContextEntities(colEntities);
		    getPageflow().setContextEntity((PFEntity)colEntities.iterator().next());
		    
            /**
             * Get DSHandler
             */
            PFEntity objTheEntity = getPageflow().getContextEntity();
            DSHandler objDSHandler = objTheEntity.getDSHandler();
            
		    /**
		     * Get the count of update rows in the grid edit form.
		     * BD16FEB04 Use escaped name in case action name has weird characters
		     */
		    String strCount = getPageflow().getRequest().getParameter("zXGridEditFormCount" + objLinkedEditFormAction.getName());
		    if (StringUtil.len(strCount) == 0) {
		        strCount = "0";
		    }
		    int intUpdateRowCount = new Integer(strCount).intValue();
		    
		    /**
		     * Determine the number of new rows to display. The count of insert rows is
		     * defined in the related grid edit form action.
		     */
		    int intInsertRowCount = 0;
		    String strNewRows = getPageflow().resolveDirector(objLinkedEditFormAction.getNewrows());
		    if (StringUtil.isNumeric(strNewRows)) {
		        intInsertRowCount = new Integer(strNewRows).intValue();
		    }
		    
		    boolean blnMorePages = (StringUtil.len(getPageflow().getRequest().getParameter("zXGridMorePages" + objLinkedEditFormAction.getName())) > 0);
		    
		    /**
		     * Process the edit form to see if any input validation errors have been
		     * made...
		     */
		    String strPersist;
		    zXType.persistStatus enmPersistStatus;
		    zXType.persistStatus enmPersistStatusEntity;
		    Iterator iter; 
		    PFEntity objEntity;                           // Loop over variable
		    ZXBO objIBO = null;
		    ZXBO objIBOTmp = null;
		    boolean blnFoundErrors = false;
		    
		    for (int intRowCount = 1; intRowCount <= intUpdateRowCount + intInsertRowCount; intRowCount++) {
		        strPersist = getPageflow().getRequest().getParameter("zXPersist" + strEscapedActionName + intRowCount);
                if (StringUtil.len(strPersist) == 0) {
                    enmPersistStatus = zXType.persistStatus.psClean;
                } else if (strPersist.equalsIgnoreCase("I")) {
		            enmPersistStatus = zXType.persistStatus.psNew;
		        } else if (strPersist.equalsIgnoreCase("U")) {
		            enmPersistStatus = zXType.persistStatus.psDirty;
		        } else if (strPersist.equalsIgnoreCase("D")) {
		            enmPersistStatus = zXType.persistStatus.psDeleted;
		        } else {
		            enmPersistStatus = zXType.persistStatus.psClean;
		        }
		        
                int intEntityCount = 1;
                iter = colEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    enmPersistStatusEntity = enmPersistStatus;
                    
                    if (intRowCount > intUpdateRowCount  && enmPersistStatusEntity.equals(zXType.persistStatus.psClean)) {
                        /**
                         * If user not selected Insert for a new row, don't do anything with that row.
                         * Need the BO though, just for persist status
                         */
                        objIBO = objEntity.getBo().cloneBO();
                        
                    } else {
                        
                        /**
                         * set the global context
                         */
                        getPageflow().setContextEntity(objEntity);
                
                        if (enmPersistStatusEntity.equals(zXType.persistStatus.psNew)) {
                            /**
                             * New - now is the time to reset, ready to overlay by user-entered data.
                             * Note that we DO NOT set automatics yet. We don't want to use up
                             * sequence numbers when an error might arise, and we don't want to
                             * show a number that might then get replaced by yet another on the
                             * second pass through - it would confuse the user. The normal process
                             * edit form would fail with empty automatic fields, but the special
                             * processGridEditForm doesn't object to those, as it knows that grid
                             * rows will get their automatics later.
                             */
                            objEntity.getBo().resetBO();
                        }
                        
                        /**
                         * Allow any attribute values to be set to fixed values before
                         * populating the properties by processing the submitted form
                         */
                        getPageflow().processAttributeValues(objEntity);
                        
                        if (StringUtil.len(objEntity.getSelecteditgroup()) > 0) {
                            /**
                             * Tweak the BO alias, so that each row and entity's fields are uniquely named
                             * DGS24FEB2004: Prevously did this before the 'if' statement above, which meant
                             * also did it for entities where not actually required (and then didn't unset it).
                             * Also, changes here to ensure that we don't have validation on when copying
                             * attrs from one BO to another - we only want to catch errors when processing
                             * the editform a bit further down.
                             */
                            objIBO = objEntity.getBo().cloneBO();
                            objIBO.setValidate(false);
                            objEntity.getBo().bo2bo(objIBO, "*");
                            objIBO.setValidate(true);
                            String strStoreAlias = objEntity.getBo().getDescriptor().getAlias();
                            objIBO.getDescriptor().setAlias(objEntity.getName() + objLinkedEditFormAction.getName() + "Row" + intRowCount);
                            
                            /**
                             * DGS01MAR2004 Added
                             */
                            getZx().getBOContext().setEntry(objEntity.getName(), objIBO);
                            
                            /**
                             * Read the form values into the save object (this has
                             * validation switched off and thus accepts bad input)
                             * No it doesn't, and nor does it in the original PF Edit Form.
                             * ;> 
                             */
                            objIBO.setPersistStatus(enmPersistStatusEntity);
                            if (!enmPersistStatusEntity.equals(zXType.persistStatus.psClean)) {
                                try {
                                    intRC = getPageflow().getPage().processGridEditForm(getPageflow().getRequest(), 
                                    													objIBO, 
                                    													objEntity.getSelecteditgroup()).pos;
                                } catch (ZXException e) {
                                	getZx().trace.addError("Failed to processGridEditForm", e);
                                    go = zXType.rc.rcError;
                                    return go;
                                }
                            }
                            
                            /**
                             * Reset the BO alias to its original value as soon as possible, to avoid any nasty problems
                             */
                            objIBO.getDescriptor().setAlias(strStoreAlias);
                            
                            /**
                             * Now here is an interesting one:
                             * If the return code was warning, it could be that the user
                             * has also ticked the zXIgnoreWarning checkbox in which case
                             * we can safely ignore the warning
                             */
                            if (intRC == zXType.rc.rcWarning.pos 
                                && StringUtil.len(getPageflow().getRequest().getParameter("zXIgnoreWarning")) > 0) {
                                /**
                                 * User decided to ignore the warning....
                                 */
                                intRC = zXType.rc.rcOK.pos;
                            }
                            
                            if (!enmPersistStatusEntity.equals(zXType.persistStatus.psClean)) {
                                /**
                                 * DGS28APR2003: OK so far, but before trying to insert or update the DB check that this
                                 * would not create any duplicates. We have to do it here so that any infraction is
                                 * reported back to the user before we lose the entered data (see comment below).
                                 * This does mean the check is repeated when the real DB action takes place.
                                 * Note that we don't do this check for updates when the unique constraint is the PK.
                                 * We also don't do it for new rows where the PK is empty - these will be automatic
                                 * PKs, and as such are bound to be unique. If the user has left a non-automatic PK
                                 * empty it would have been errored earlier anyway.
                                 * This isn't completely foolproof but anything that slips through will fail on the
                                 * update and get rolled back anyway.
                                 */
                                if (intRC == zXType.rc.rcOK.pos && !enmPersistStatus.equals(zXType.persistStatus.psDeleted)) {
                                    /**
                                     * Not a deleted row...
                                     */
                                    if (StringUtil.len(objEntity.getBo().getDescriptor().getUniqueConstraint()) > 0) {
                                        /**
                                         * ...and there is a unique constraint...
                                         */
                                        if (enmPersistStatusEntity.equals(zXType.persistStatus.psNew) && StringUtil.len(objIBO.formattedString("+")) > 0
                                            || (
                                                    !objEntity.getBo().getDescriptor().getUniqueConstraint().equals("+") &&
                                                    !objEntity.getBo().getDescriptor().getUniqueConstraint().equals(objEntity.getBo().getDescriptor().getPrimaryKey()) 
                                                 ) ) {
                                            /**
                                             * ...and it's a new row and the PK is not empty OR
                                             * ...the unique constraint is not the PK (by name or shorthand "+")
                                             */
                                            if (objIBO.doesExist(objEntity.getBo().getDescriptor().getUniqueConstraint())) {
                                                /**
                                                 * DGS08AUG2003: Previously used setError but this is a friendly user error
                                                 */
                                                getZx().trace.userErrorAdd("Inserting this record would cause duplicates");
                                                intRC = zXType.rc.rcError.pos;
                                            }  
                                        }
                                        
                                        /**
                                         * Now check that the user is not trying to insert more than one duplicate
                                         * at the same time. The above 'doesExist' check won't catch these as they
                                         * are not on the database yet. This next check catches them:
                                         */
                                        if (enmPersistStatusEntity.equals(zXType.persistStatus.psNew)) {
                                            /**
                                             * Only interested in new inserts
                                             */
                                            for (int intPrevInsert = intUpdateRowCount + 1; intPrevInsert <= intRowCount - 1; intPrevInsert ++) {
                                                /**
                                                 * Look back, starting from the first new row (intUpdateRowCount + 1) up until the one before this new row...
                                                 */
                                                objIBOTmp = (ZXBO)objEntity.getBOSavers().get(String.valueOf(intPrevInsert));
                                                if (objIBOTmp.getPersistStatus().equals(zXType.persistStatus.psNew)) {
                                                    /**
                                                     * That one is also being newly inserted...
                                                     */
                                                    if (objIBOTmp.formattedString(objEntity.getBo().getDescriptor().getUniqueConstraint()).equals(objIBO.formattedString(objEntity.getBo().getDescriptor().getUniqueConstraint()))) {
                                                        /**
                                                         * ...and has exactly the same unique key values, so an error:
                                                         */
                                                        getZx().trace.userErrorAdd("Duplicate inserts");
                                                        intRC = zXType.rc.rcError.pos;  
                                                    }  
                                                } // Is new 
                                                
                                            } // Loop over new inserts  
                                            
                                        } // Is new  
                                    } // There is a unique constraint
                                    
                                } // Ok and not deleted
                    		    
                    		    /**
                    		     * Now process any cuActions that we may have...
                    		     */
                                int intProcessCUActionsRC = getPageflow().processCUActions(getCuactions(), 
                                														   StringUtil.len(getPageflow().getRequest().getParameter("zXIgnoreWarning")) > 0).pos;
                    		    if (intProcessCUActionsRC == zXType.rc.rcWarning.pos) {
                    		    	if (StringUtil.len(getPageflow().getRequest().getParameter("zXIgnoreWarning")) <= 0) {
                    		    		/**
                    		    		 * User decided not to ignore the warning....
                    		    		 */
                    		    		if (intRC == zXType.rc.rcOK.pos) {
                    		    			intRC = zXType.rc.rcWarning.pos;
                    		    		}
                    		    		blnFoundErrors = true;
                    		    	}
                    		    	
                    		    } else if (intProcessCUActionsRC == zXType.rc.rcError.pos) {
                    		    	intRC = zXType.rc.rcError.pos;
                    		        blnFoundErrors = true;
                    		        
                    		    } // Process cu actions
                    		    
                                if (intRC != zXType.rc.rcOK.pos) {
                                    blnFoundErrors = true;
                                    
                                    /**
                                     * Add error message to the i.pageflow error message
                                     * DGS23FEB2004: No, do it at the end of all rows.
                                     * In any case this call to formatErrorStack was wrong
                                     * as it returns an rc, not a string (but it did format
                                     * the error as well, so it wasn't totally pointless)
                                     */
                                    
                                    /**
                                     * This may look a bit weird but there is a good reason:
                                     * The user has entered information that is not correct
                                     * according to the validation rules. Obviously we have to
                                     * use the saver object (that has validation switched off)
                                     * to generate the editform otherwise the user would lose
                                     * the whole entered text where only one character may be wrong.
                                     * Now we have to read the input from the form into the
                                     * saver object
                                     */
                                    strStoreAlias = objIBO.getDescriptor().getAlias();
                                    objIBO.getDescriptor().setAlias(objEntity.getName() + objLinkedEditFormAction.getName() + "Row" + intRowCount);
                                    
                                    objIBO.setValidate(false);
                                    getPageflow().getPage().processGridEditForm(getPageflow().getRequest(), 
                                     										    objIBO,
                                            									objEntity.getSelecteditgroup());
                                    
                                    objIBO.getDescriptor().setAlias(strStoreAlias);
                                    
                                    /**
                                     * DGS23FEB2004: Set an object of a specific name into the object
                                     * model of the BO so that the grid edit form can spot errors and
                                     * use a different style class on that row.
                                     */
                                    objIBO.getOM().put("zXGridErr", new StringProperty("Y", false));
                                    
                                }  // Not OK?
                                
                            } // If not a clean row
                            
                        } // Relevant as has selectditGroup
                        
                    } // Possible update
                    
                    if (objIBO != null) {
                        /**
                         * Note that we may end up with not having an objIbo in case
                         * of an entity that has no relevance for the cerateUpdate but only
                         * for the edit form
                         */
                        objIBO.setPersistStatus(enmPersistStatusEntity);
                        objEntity.getBOSavers().put(String.valueOf(intRowCount), objIBO);
                        objIBO = null;
                        
                    }
                    
                    intEntityCount = intEntityCount + 1;  
                }
                
            }
		    
		    /**
		     * If we have come across an error....
		     */
		    if (blnFoundErrors) {
		        /**
		         * DGS16JUL2004: First collect any error messages defined against this action
		         */
		        getPageflow().collectMessages(this, blnFoundErrors, zXType.logLevel.llError.pos);
		        
		        /**
		         * DGS23FEB2004: Now is the right time to show errors, not within each row as was the case.
		         */
		        getPageflow().setErrorMsg(getPageflow().getErrorMsg() + "\n" + getZx().trace.formatStack(false));
		        
		        /**
		         * Tell the editform that we are in progress and that we have
		         * come across an error. Also tell it if there are more pages - so that the editform
		         * can set the paging buttons at the bottom.
		         */
		        getPageflow().getQs().setEntry(strQSProgress, objLinkedEditFormAction.getName());
		        getPageflow().getQs().setEntry("-err", objLinkedEditFormAction.getName());
		        getPageflow().getQs().setEntry("-pg", getPageflow().getRequest().getParameter("zXGridPage" + objLinkedEditFormAction.getName()));
		        getPageflow().getQs().setEntry("-pgmore", (blnMorePages?"Y":""));
		        
		        /**
		         * In case of a warning, add the warning to the QS as well
		         */
		        if (intRC == zXType.rc.rcWarning.pos) {
		            getPageflow().getQs().setEntry("-wrn", "Y");
		        }
		        
		        /**
		         * Go to related editForm again
		         * DGS24FEB2003: No, go to the editform startaction if there is one defined:
		         */
		        if (StringUtil.len(getEditformstartaction().getAction()) == 0) {
		            getPageflow().setAction(getPageflow().resolveDirector(objLinkedEditFormAction.getName()));
		        } else {
		            getPageflow().setAction(getPageflow().resolveLink(getEditformstartaction()));
		        }
		        
		    } else {
		        /** 
                 * No errors found while reading values from screen so now all is in BOSavers and now
                 * is time to persist changes
                 */
		        objDSHandler.beginTx();
                
                Iterator iterBOSavers;
		        iter = colEntities.iterator();
		        while (iter.hasNext()) {
		            objEntity = (PFEntity)iter.next();
		            
		            iterBOSavers = objEntity.getBOSavers().iterator();
		            while (iterBOSavers.hasNext()) {
		                objIBO = (ZXBO)iterBOSavers.next();
		                
		                /**
		                 * Ignore objects in the collection where persist action is clean
		                 * or that are not relevant because of selectEditGroup
		                 */
		                if (!objIBO.getPersistStatus().equals(zXType.persistStatus.psClean) 
		                    && StringUtil.len(objEntity.getSelecteditgroup()) > 0) {
                            
		                    if (objIBO.getPersistStatus().equals(zXType.persistStatus.psNew)) {
		                        /**
		                         * Finally we can set automatics for new rows
		                         */
		                        objIBO.setAutomatics("+");
		                    }
		                    
		                    /**
		                     * DGS01MAR2004 Added
		                     */
		                    getZx().getBOContext().setEntry(objEntity.getName(), objIBO);
		                    
		                    /**
		                     * BD16JUL04 - Before we do the actual persist, we switch
		                     * back on the validation (is off for the processEditForms).
		                     * This is needed as we may do validations (e.g. eventActions)
		                     * as part of the pre- / post persist and this may check on
		                     * the validate flag
		                     */
		                    objIBO.setValidate(true);
		                    
		                    /**
		                     * Temporarly turn of exception handling :
		                     */
		                    getZx().setThrowException(false);
		                    intRC = objIBO.persistBO(objEntity.getSelecteditgroup()).pos;
		                    getZx().setThrowException(false);
		                    
		                    if (intRC != zXType.rc.rcOK.pos) {
		                    	/**
		                    	 * If something went wrong in the persist, set
		                    	 * a flag on the BO in error so we can highlight this
		                    	 * row in the associated edit form
		                    	 */
		                    	objIBO.getOM().put("zXGridErr", new StringProperty("Y"));
		                    	
		                        /**
		                         * Something has gone wrong let up see how we can recover 
		                         */
		                        if (intRC == zXType.rc.rcError.pos) {
		                            getPageflow().setErrorMsg(getZx().trace.formatStack(false));
		                        } else {
		                            getPageflow().setErrorMsg("Row updated by other user since it was retrieved");
		                        }
		                        
		                        objDSHandler.rollbackTx();
		                        
		                        /**
		                         * Tell the editform that we are in progress and that we have
		                         * come across an error
		                         */
		                        getPageflow().getQs().setEntry(strQSProgress, objLinkedEditFormAction.getName().toLowerCase());
		                        getPageflow().getQs().setEntry("-err", objLinkedEditFormAction.getName().toLowerCase());
		                        getPageflow().getQs().setEntry("-pg", getPageflow().getRequest().getParameter("zXGridPage" + strEscapedActionName));
		                        getPageflow().getQs().setEntry("-pgmore",blnMorePages?"Y":"");
		                        
		                        /**
		                         * Go to related editForm or editform startaction if there is one defined:
		                         */
		                        if (StringUtil.len(getEditformstartaction().getAction()) == 0) {
		                            getPageflow().setAction(objLinkedEditFormAction.getName());
		                        } else {
		                            getPageflow().setAction(getPageflow().resolveLink(getEditformstartaction()));
		                        }
		                        
		                        /**
		                         * Reset the persist status of every row so that the action drop down
		                         * in the edit form appears correctly.
		                         */
		                        for (int intRowCount = 0; intRowCount <= intUpdateRowCount + intInsertRowCount; intRowCount++) {
                                    
		                            strPersist = getPageflow().getRequest().getParameter("zXPersist" + strEscapedActionName + intRowCount);
		                            if (StringUtil.len(strPersist) == 0) {
		                                enmPersistStatus = zXType.persistStatus.psClean;
		                            } else if (strPersist.equalsIgnoreCase("I")) {
		            		            enmPersistStatus = zXType.persistStatus.psNew;
		            		        } else if (strPersist.equalsIgnoreCase("U")) {
		            		            enmPersistStatus = zXType.persistStatus.psDirty;
		            		        } else if (strPersist.equalsIgnoreCase("D")) {
		            		            enmPersistStatus = zXType.persistStatus.psDeleted;
		            		        } else {
		            		            enmPersistStatus = zXType.persistStatus.psClean;
		            		        }
                                    
                                    PFEntity objEntityErr;
		                            Iterator iterEntities = colEntities.iterator();
		                            while(iterEntities.hasNext()) {
		                                objEntityErr = (PFEntity)iterEntities.next();
		                                objIBOTmp = (ZXBO)objEntityErr.getBOSavers().get(String.valueOf(intRowCount + 1)); // index miss match. stored as 1,2,3 assuming 0,1,2 etc..
                                        
		                                /**
		                                 * If this is an update row and is auditable, reload the audit
		                                 * columns from the database or they will be wrong on the editform
                                         * 
		                                 * BD9JUN04 - Now concurrency control
		                                 */
		                                if (enmPersistStatus.equals(zXType.persistStatus.psDirty)) {
		                                    if (objIBOTmp.getDescriptor().isConcurrencyControl()) {
		                                        objIBOTmp.loadBO("~");
		                                    }
		                                }
                                        
		                                /**
		                                 * Reset the persist status AFTER loading the audit columns
		                                 */
		                                objIBOTmp.setPersistStatus(enmPersistStatus);
                                        
		                            } // Loop over entities in error
                                    
                                } // Loop over all rows (existing + new)
                                
		                        // GoTo okExit
                                return go;
                                
		                    } // Persist ok?
                            
		                } // Persist action not clean and has edit group?
                        
		            } // Loop over BO Savers
                    
		        } // Loop over entities
		        
		        objDSHandler.commitTx();
		        
		        /**
			     * Clear the error and progress flag from the QS
			     */
			    getPageflow().getQs().removeEntry("-err");
			    getPageflow().getQs().removeEntry(strQSProgress);
			    
			    getPageflow().setAction(getPageflow().resolveLink(getLink()));
		    }
		    
		    return go;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Execute execute the Grid Create Update action.", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    go = zXType.rc.rcError;
		    return go;
		} finally {
		    if(getZx().trace.isApplicationTraceEnabled()) {
		        getZx().trace.returnValue(go);
		        getZx().trace.exitMethod();
		    }
		}
    }
}