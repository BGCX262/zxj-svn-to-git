/*
 * Created on Apr 12, 2004 by Michael Brewer
 * $Id: PFCreateUpdate.java,v 1.1.2.28 2006/07/17 16:28:43 mike Exp $
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.Iterator;

import org.zxframework.Tuple;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHandler;
import org.zxframework.util.StringUtil;

/**
 * Pageflow createUpdate action object.
 * 
 * <pre>
 * 
 * Change    : BD19FEB03
 * Why       : Introduced support for multi-step warning messages
 * 
 * Change    : DGS24FEB2003
 * Why       : New property for edit form start action
 * 
 * Change    : BD16APR03
 * Why       : Do any QS upadtes in case of an error before resolving
 *             the action link so we could, if we choose to, fiddle around with
 *             QS entries like -err and -prgs
 * 
 * Change    : BD22APR03
 * Why       : The in-progress querystring entry is no longer a simple Y or N (or
 *             absent) but it now contains the name of the edit form action that
 *             is in error. This is to allow multiple edit forms to be linked (where
 *             one is a true edit form and the others are locked). If someting goes
 *             wrong on the true edit form (as we find in createUpdate) the repaint
 *             of the editform will load the BOs for the locked edit forms as they
 *             can now tell that it wasnt them in error!
 * 
 * Change    : DGS24FEB2003
 * Why       : In Go, before trying to insert or update the DB check that this would not create
 *             any duplicates. We have to do it at this point so that any infraction is reported
 *             back to the user before we lose the entered data. This does mean the check is
 *             repeated when the real DB action takes place. We don't do this check for updates
 *             when the unique constraint is the PK.
 * 
 * Change    : BD11MAY03
 * Why       : Fixed error: when a createUpdate was successfull, the progress qs entry should
 *             be cleared
 * 
 * Change    : DGS27JUN2003
 * Why       : Added sub actions
 * 
 * Change    : DGS06AUG2003
 * Why       : Should not try to setPK unless sub action includes 'select'. Also was using
 *             wrong kind of set error message when a duplicate.
 * 
 * Change    : BD29NOV03
 * Why       : VERY IMPORTANT CHANGE:
 *             Process the attribute values AFTER we have reset / setautomatics so
 *             we can set FK values (and other attributes) without them being
 *             available on the associated edit-form.
 *             THIS CHANGE MAY HAVE SIDE-EFFECTS FOR EXISTING PAGEFLOWS AND NEED TO
 *             BE CAREFULLY MONITORED
 * 
 * Change    : BD29MAR04
 * Why       : When the subaction contains an 'i' (i.e. 'insert') force the
 *             persist to new
 * 
 * Change    : DGS14MAY2004
 * Why       : When the subaction contains an 's' (i.e. 'select') force the
 *             persist to clean
 * 
 * Change    : DGS25MAY2004
 * Why       : The existing duplicate check was being performed when neither an insert nor
 *             an update, such as when editnodb.
 * 
 * Change    : BD9JUN04
 * Why       : Added support for concurrencyControl (rather than auditable) and
 *             limit audit columns to ~ instead of !
 * 
 * Change    : BD9JUL04
 * Why       : Fixed bug: if no errors are found and no warnings; make sure we remove
 *             -wrn entry from quick context
 * 
 * Change    : DGS16JUL2004
 * Why       : Collect any success or error messages ready for display by next visible page
 * 
 * Change    : V1.4:62 - DGS05APR2005
 * Why       : Trap an exception on database commit
 * 
 * Change    : BD2APR05 - V1.5:1
 * Why       : Added support for data sources
 * 
 * Change    : V1.5:39 - BD15JUL05
 * Why       : Added support for noDB option
 * 
 * Change    : BD4AUG05 - V1.5:32
 * Why       : Added support for createUpdateActions
 *
 * Change    : BD4AUG05 - V1.5:43
 * Why       : Added support for re-using boContext of edit form
 * 
 * Change    : BD2DEC05 - V1.5:71
 * Why       : Resolve BO context from related edit form before resolving
 * 			   entities; otherwise you cannot refer to BO context in
 * 			   entity groups or attribute values
 * 
 * Change    : V1.5:95 - BD28APR2006
 * Why       : If ignoring warnings, do not show warnings on redraw of page
 *
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFCreateUpdate extends PFAbstractCreateUpdate {
    
    //------------------------ Members
	
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFCreateUpdate() {
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
     * Reviewed for V1.5:36
     * Reviewed for V1.5:32
     * Reviewed for V1.5:43
     * Reviewed for V1.5:71
     * </pre>
     * 
     * @see PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
		if(getZx().trace.isApplicationTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
		// The overall return code for the pageflow action
		zXType.rc go = zXType.rc.rcOK;
		// This internal return code
    	int intRC = zXType.rc.rcOK.pos;
        
        ArrayList colDSHandlersTx = new ArrayList();
        DSHandler objDShandler;
        
		try {
		    PFEditForm objLinkedEditForm;     // Associated edit form as PFEditForm     
		    PFEntity objEntity;               // Loop over variable
		    boolean blnFoundErrors = false;   // User-errors in edit form?
		    
		    /**
		     * Get handle to the linked editForm action and actual action implementation
		     */
		    objLinkedEditForm = (PFEditForm)getPageflow().getPFDesc().getAction(getPageflow().resolveDirector(getLinkededitform()));
		    if (objLinkedEditForm == null) {
		        throw new Exception("LinkedEditForm action not found " + getLinkededitform());
		    }
		    
		    String strLinkedEditFormName = objLinkedEditForm.getName().trim().toLowerCase();
		    
		    /**
		     * Determine how progress and subaction are stored in the query string
		     * 
		     * Key to find in QS whether in progress
		     */
		    String strQSProgress = (StringUtil.len(objLinkedEditForm.getQsprogress()) == 0?"-prgs":objLinkedEditForm.getQsprogress());
		    /**
		     * Key to find in QS the sub action
		     */
		    String strQSSubAction = (StringUtil.len(objLinkedEditForm.getQssubaction()) == 0)? "-sa":objLinkedEditForm.getQssubaction();
		    
		    /**
		     * Check if we are already in progress
		     * 
		     * Set when called for 2nd / 3rd / 4th time
		     */
			boolean blnInProgress = (StringUtil.len(getPageflow().getQs().getEntryAsString(strQSProgress)) > 0);
            if (blnInProgress) {
            	// TODO : Note to bertus, we do not use this flag at the moment, should we remove this.
            }
			
            /**
             * Get sub action as passed in QS
             * 
             * Name and value of user defined sub action
             */
			String strSubActionName = getPageflow().getQs().getEntryAsString(strQSSubAction);
		    
			/**
			 * If noDb flag has been set than we force editNoDb unless something was
			 * passed as -sa explicitly...
			 */
			if (StringUtil.len(strSubActionName) == 0) {
				if (objLinkedEditForm.isNodb()) {
					strSubActionName  = "editnodb";
				} // noDB flag set?
				
			} // No -sa passed
			
			/**
			 * Get the subaction - this may or may not find a match in the collection;
			 * One could argue that this should not be done when the noDB flag is set and
			 * one if probably right.
			 */
		    Tuple objSubAction = (Tuple)objLinkedEditForm.getEditsubactions().get(strSubActionName);
		    String strSubActionValue = "";
		    if (objSubAction !=null) {
		        strSubActionValue = objSubAction.getValue();
		    }
			
		    if (StringUtil.len(strSubActionValue) == 0) {
			    zXType.pageflowSubActionType enmSubaction = null;
			    
		        /**
		         * Here we didn't find a match in the collection of sub actions, so we can assume
		         * it is one of the old style hard-coded sub actions such as 'editnodb'.
		         */
		        if (strSubActionName.endsWith("insert")) {
		        	enmSubaction = zXType.pageflowSubActionType.psatInsert;
		            strSubActionValue = "ra.i";
		        } else if (strSubActionName.endsWith("copy")) {
		            enmSubaction = zXType.pageflowSubActionType.psatCopy;
		            strSubActionValue = "sa.i";
		        } else if (strSubActionName.endsWith("error")) {
		            enmSubaction = zXType.pageflowSubActionType.psatError;
		            throw new Exception("Unexpected error sub action");
		        } else if (strSubActionName.endsWith("editnodb")) {
		            enmSubaction = zXType.pageflowSubActionType.psatEditNoDB;
		        } else if (strSubActionName.endsWith("editnodbupdate")) {
		            enmSubaction = zXType.pageflowSubActionType.psatEditNoDBUpdate;
		            strSubActionValue = "s.";
		        } else {
		            /**
                     * None of the standard sub actions
		             * The default behaviour has always been a standard edit:
		             */
		            enmSubaction = zXType.pageflowSubActionType.psatEdit;
		            strSubActionValue = "s.u";
		            
		        }
                
			    if (getZx().log.isDebugEnabled()) {
			    	getZx().log.debug("CreateUpdate action :" + enmSubaction.getName());
			    }
			    
		    } // User defined sub action passed?
		    
		    /**
		     * Perhaps we also have to set the BO context using the related edit form
		     * BO context definition
		     */
		    if (isUseeditbocontext()) {
		    	getPageflow().processBOContexts(objLinkedEditForm);
		    }
		    
		    /**
		     * Get entities
		     */
		    ZXCollection colEntities = getPageflow().getEntityCollection(this, 
		    															 zXType.pageflowActionType.patCreateUpdate, 
		    															 zXType.pageflowQueryType.pqtSearchForm, 
		    															 strSubActionValue);
		    if (colEntities == null) {
		        throw new Exception("Unable to retrieve entity collection");
		    }
		    
		    /**
		     * Set context variable
		     */
		    getPageflow().setContextEntities(colEntities);
		    getPageflow().setContextEntity((PFEntity)colEntities.iterator().next());
		    
		    /**
		     * Process the edit form to see if any input validation errors have been
		     *  made...
		     */
		    Iterator iter = colEntities.iterator();
		    while (iter.hasNext()) {
		        objEntity = (PFEntity)iter.next();
		        
		        /**
		         * set the global context
		         */
		        getPageflow().setContextEntity(objEntity);
		        
		        /**
		         * BD29NOV03 VERY IMPORTANT CHANGE
		         *  ORIGINALLY WE USED TO PROCESS THE ATTRIBUTE VALUES BEFORE WE DO
		         *  A RESET / SETAUTOMATICS. THIS MEANS THAT YOU ALWAYS HAVE TO HAVE
		         *  FIELDS ON THE EDIT FORM (OPTIONALLY INVISIBLE). WE HAVE NOW
		         *  MOVED THIS BUT THIS MAY HAVE SOME CONSEQUENCES
		         */
		        // i.pageflow.processAttributeValues objEntity -- OLD VB CODE.
		        
		        /**
		         * In case of an update we have to set the PK of the BO, in case of an insert
		         *  we reset the BO before we process the edit form
		         *  DGS06AUG2003: Previously always tried to setPK if not an insert. Now only tries
		         *  it if explicitly has 's' in the sub action i.e. is to select existing details
		         */
		        if (getPageflow().editSubActionIncludes(strSubActionValue, "i")) {
		            objEntity.getBo().resetBO();
                    
		        } else if (getPageflow().editSubActionIncludes(strSubActionValue, "s")) {
		            /**
		             * DGS14MAY2004: Was not explicitly setting the persist status. Normally ok, but if
		             * the sub action is like 'editnodbupdate', the persist status will be 'new' by default
		             * and will not be replaced by 'dirty' as there is no post-edit DB activity. 'New'
		             * is wrong when have selected (as here). If you do want 'new', use the post-edit
		             * sub action value to force it.
		             */
		            objEntity.getBo().setPersistStatus(zXType.persistStatus.psClean);
		            objEntity.getBo().setPKValue(getPageflow().resolveDirector(objEntity.getPk()));
                    
		        } // Handle insert and save sub actions
		        
		        /**
		         * Allow any attribute values to be set to fixed values before
		         * populating the properties by processing the submitted form
		         */
		        getPageflow().processAttributeValues(objEntity);
		        
		        if (StringUtil.len(objEntity.getSelecteditgroup()) > 0) {
		            /**
		             * Read the form values into the save object (this has
		             * validation switched off and thus accepts more crap input)
		             */
		            intRC = getPageflow().getPage().processEditForm(getPageflow().getRequest(),
                                                                 objEntity.getBo(), 
                                                                 objEntity.getSelecteditgroup()).pos;
		            
		            /**
		             * Now here is an interesting one:
		             * If the return code was warning, it could be that the user
		             * has also ticked the zXIgnoreWarning checkbox in which case
		             * we can safely ignore the warning.
                     * DGS02FEB2005: Only ignore the warning if rc is warning. Don't want user
                     * changing things to be in error and getting those changes through
                     * because last time we showed the form the allow warning flag was available
		             */
		            if (intRC == zXType.rc.rcWarning.pos && StringUtil.len(getPageflow().getRequest().getParameter("zXIgnoreWarning")) > 0) {
		                /**
		                 * User decided to ignore the warning....
		                 */
		                intRC = zXType.rc.rcOK.pos;
		            }
		            
		            /**
		             * DGS28APR2003: OK so far, but before trying to insert or update the DB check that this
		             * would not create any duplicates. We have to do it here so that any infraction is
		             * reported back to the user before we lose the entered data (see comment below).
		             * This does mean the check is repeated when the real DB action takes place.
		             * Note that we don't do this check for updates when the unique constraint is the PK.
		             * DGS25MAY2004: ...and we don't do it at all if it's neither an insert nor an update.
		             */
		            if (intRC == zXType.rc.rcOK.pos) {
		                String strUniqueConstraint = objEntity.getBo().getDescriptor().getUniqueConstraint();
		                if (StringUtil.len(strUniqueConstraint) > 0) {
                            
		                    if ( (getPageflow().editSubActionIncludes(strSubActionValue, "i") || 
		                          (getPageflow().editSubActionIncludes(strSubActionValue, "u") ) &&
		                           !strUniqueConstraint.equals("+") && 
		                           !strUniqueConstraint.equalsIgnoreCase(objEntity.getBo().getDescriptor().getPrimaryKey()))
		                        ) {
		                    	
		                        if (objEntity.getBo().doesExist(strUniqueConstraint)) {
		                            /**
		                             * DGS08AUG2003: Previously used setError but this is a friendly user error
		                             */
		                            getZx().trace.userErrorAdd("Inserting this record would cause duplicates");
		                            intRC = zXType.rc.rcError.pos;
		                        }
		                        
		                    } // Insert or update and unique constraint is not the primary key
                            
		                } // Check whether unique constraint is still met
                        
		            } // No errors found?
		            
		            if (intRC != zXType.rc.rcOK.pos) {
		                blnFoundErrors = true;
		                
		                /**
		                 * Add error message to the i.pageflow error message
		                 * DGS19FEB2004: Don't do anything at this stage. Any errors will be safely in the
		                 * error stack. After processing all entities, show the stack (see later below).
		                 * Note also that HTML.formatErrorStack was the wrong function anyway - it does output
		                 * the errors as HTML but returns rc, which obviously we don't want to show, and in
		                 * any case with a multi-entity edit form it repeats the message each time. OK, we
		                 * could reset the error stack, but we'd still get multiple error icons etc. Best to
		                 * do it at the end.
		                 */
						//If Len(i.pageflow.errorMsg) > 0 Then
						//	i.pageflow.errorMsg = i.pageflow.errorMsg & vbCrLf & i.HTML.formatErrorStack
						//Else
						//	i.pageflow.errorMsg = i.zX.trace.formatStack(False)
						//End If
		                
		                /**
		                 * This may look a bit weird but there is a good reason:
		                 * The user has entered information that is not correct
		                 * according to the validation rules. Obviously we have to
		                 * use the saver object (that has validation switched off)
		                 * to generate the editform otherwise the user would loose
		                 * the whole entered text where only one character may be wrong.
		                 * Now we have to read the input from the form into the
		                 * saver object
		                 */
		                getPageflow().getPage().processEditForm(getPageflow().getRequest(),
                                                                objEntity.getBosaver(), 
                                                                objEntity.getSelecteditgroup());
		                
		            } else {
		                /**
		                 * All was fine so we can safely copy the values to
		                 *  BOSaver;
		                 * To add some good old tricky-ness: in case of an auditable BO, the
		                 * audit columns are also on the form; we have already processed them
		                 * (ie copied them from the form to the save BO) so we also have to copy them
		                 * to the real thing!
		                 * BD9JUN04 - Is now changed to concurrency control
		                 */
		                objEntity.getBo().bo2bo(objEntity.getBosaver(), 
		                        				objEntity.getSelecteditgroup() + (objEntity.getBo().getDescriptor().isConcurrencyControl()?",~":""), 
		                        				false);
		            } // Found errors?
                    
		        } // Has edit group?
                
		    } // Loop over entities
		    
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
		    
		    /**
		     * If we have come across an error....
		     */
		    if (blnFoundErrors) {
                /**
                 * Manually trigger business object validation actions.
                 */
                if (getPageflow().editSubActionIncludes(strSubActionValue, "u") 
                	|| getPageflow().editSubActionIncludes(strSubActionValue, "i")) {
                    zXType.persistAction enmPersist = getPageflow().editSubActionIncludes(strSubActionValue, "u")? zXType.persistAction.paUpdate: zXType.persistAction.paInsert;
                    iter = colEntities.iterator();
                    while (iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        objEntity.getBo().prePersist(enmPersist, objEntity.getSelecteditgroup());
                    }
                }
                
		        /**
		         * DGS16JUL2004: First collect any error messages defined against this action.
		         * NOTE : This may be causing duplicates ?
		         */
		        getPageflow().collectMessages(this, true, zXType.logLevel.llError.pos);
		        
		        /**
		         * DGS19FEB2004: Now is the right time to show error messages.
		         */
		        getPageflow().setErrorMsg(getPageflow().getErrorMsg() + "\n" + getZx().trace.formatStack(false));
		        
		        /**
		         * Tell the editform that we are in progress and that we have
		         * come accross an error
		         */
		        getPageflow().getQs().setEntry(strQSProgress, strLinkedEditFormName);
		        getPageflow().getQs().setEntry("-err", strLinkedEditFormName);
		        
		        /**
		         * In case of a warning, add the warning to the QS as well
		         */
		        if (intRC == zXType.rc.rcWarning.pos) {
		            getPageflow().getQs().setEntry("-wrn", "Y");
		        } else {
		            /**
                     * DGS02FEB2005: In case of no warning we make sure that -wrn is no longer in QS
		             */
                    getPageflow().getQs().setEntry("-wrn", null);
                }
		        
		        /**
		         * Go to related editForm again
		         * DGS24FEB2003: No, go to the editform startaction if there is one defined:
		         */
		        if (StringUtil.len(getEditformstartaction().getAction()) == 0) {
		            getPageflow().setAction(getPageflow().resolveDirector(strLinkedEditFormName));
		        } else {
		            getPageflow().setAction(getPageflow().resolveLink(getEditformstartaction()));
		        }
		        
		    } else {
		        /**
		         * in case of no warning we make sure that -wrn is no longer in QS
		         */
		        getPageflow().getQs().setEntry("-wrn", null);
		        
		        /**
		         * No errors found so we can safely do the create / update unless the
		         * sub-action tells us not to....
		         */
		        if (getPageflow().editSubActionIncludes(strSubActionValue, "u") 
		        	|| getPageflow().editSubActionIncludes(strSubActionValue, "i")) {
                    /**
                     * BD2APR04 - V1.5:1 transaction handling now done per data source handler.
                     * 
                     * The reason being that each datasource maybe on a seperate database.
                     */
		        	
                    /**
                     * Bit of special transaction handling: we can have multiple entities and these entities
                     * can even be associated with different data source handlers. So we need to start a transaction
                     * through each datasource handler.
                     * It can be that a trsnaction was already started outside of this routine, in which case we
                     * dont have to start it and it would also not be fair for us to close it! Therefore we maintain
                     * a collection for all entities for which we have started the transaction
                     * This is a new feature that was not supported pre-1.5
                     */
		            iter = colEntities.iterator();
		            while (iter.hasNext()) {
		                objEntity = (PFEntity)iter.next();
		                
                        /**
                         * Need to start transaction?
                         * NOTE: For each business object we will start a new 
                         * transaction and then keep track of them.
                         */
                        if (objEntity.getDSHandler().inTx()) {
                            objEntity.getBo().getDS().beginTx();
                            
                            colDSHandlersTx.add(objEntity.getBo().getDS());
                        }
                        
		                /**
		                 * Because the web is not conversational, the persist status
		                 * is not really what it should be. In case of sub action
		                 * edit, the persist status should actually be dirty in orde
		                 * to force a update
		                 */
		                if (getPageflow().editSubActionIncludes(strSubActionValue, "u")) {
		                    objEntity.getBo().setPersistStatus(zXType.persistStatus.psDirty);
		                }
		                
		                /**
		                 * BD29MAR04
		                 */
		                if (getPageflow().editSubActionIncludes(strSubActionValue, "i")) {
		                    objEntity.getBo().setPersistStatus(zXType.persistStatus.psNew);
		                }
		                
		                try {
		                	/**
		                	 * The business object we be able to figure which to
		                	 * perform a insert or a update.
		                	 */
		                    intRC = objEntity.getBo().persistBO(objEntity.getSelecteditgroup()).pos;
		                    
		                } catch (Exception e) {
		                	getZx().trace.addError("Failed to update " + objEntity.getBo().getDescriptor().getName());
		                    intRC = zXType.rc.rcError.pos;
		                }
		                
		                if (intRC != zXType.rc.rcOK.pos) {
		                    if (intRC == zXType.rc.rcError.pos) {
		                        getPageflow().setErrorMsg(getZx().trace.formatStack(false));
		                    } else {
		                        getPageflow().setErrorMsg("Row updated by other user since it was retrieved");
		                    }
		                    
                            /**
                             * Is different since 1.5:1
                             * Now we have to rollback all transactions that we have started
                             */
                            int intHandlers = colDSHandlersTx.size();
                            for (int i = 0; i < intHandlers; i++) {
                                objDShandler = ((DSHandler)colDSHandlersTx.get(i));
                                
                                if (objDShandler.inTx()) {
                                	try {
                                		objDShandler.rollbackTx();
                                	} catch (Exception e) {
                                		/**
                                		 * We will try our best to rollback all open transactions.
                                		 */
                                		getZx().trace.addError("Failed to rollback transaction for datasource", 
                                							   objDShandler.getName(),e);
                                	}
                                }
                            }
		                    
		                    /**
		                     * Tell the editform that we are in progress and that we have
		                     * come accross an error
		                     */
		                    getPageflow().getQs().setEntry(strQSProgress, strLinkedEditFormName);
		                    getPageflow().getQs().setEntry("-err", strLinkedEditFormName);
		                    
		                    /**
		                     * Go to related editForm again
		                     * DGS24FEB2003: No, go to the editform startaction if there is one defined:
		                     */
		                    if (StringUtil.len(getEditformstartaction().getAction()) == 0) {
		                        getPageflow().setAction(strLinkedEditFormName);
		                    } else {
		                        getPageflow().setAction(getPageflow().resolveLink(getEditformstartaction()));
		                    }
		                    
		                    go = zXType.rc.rcError;
		                    return go;
                        } // Able to persist
		                
		            } // Loop over entities
                    
                    /**
                     * Commit for all handlers where we have started a transaction.
                     */
                    int intHandlers = colDSHandlersTx.size();
                    for (int i = 0; i < intHandlers; i++) {
                        if (((DSHandler)colDSHandlersTx.get(i)).commitTx().pos != zXType.rc.rcOK.pos) {
                            getZx().trace.addError("Unable to commit for datasource-handler");
                            
		                    go = zXType.rc.rcError;
		                    return go;
                        }
                        
                    } // Loop over dsHandlers that we started a tx for
		        }
		        
		        /**
		         * Clear the error and progress flag from the QS
		         */
		        getPageflow().getQs().removeEntry("-err");
		        getPageflow().getQs().removeEntry(strQSProgress);
		        
		        /**
		         * DGS16JUL2004: Collect any messages (not error messages) ready for display by next visible page
		         */
		        getPageflow().collectMessages(this, true, zXType.logLevel.llInfo.pos);
		        
		        getPageflow().setAction(getPageflow().resolveLink(getLink()));
		    }
		    
		    return go;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to perform createUpdate.", e);
            
            /**
             * Rollback any uncommitted actions.
             */
            int intHandlers = colDSHandlersTx.size();
            for (int i = 0; i < intHandlers; i++) {
                objDShandler = ((DSHandler)colDSHandlersTx.get(i));
                
                if (objDShandler.inTx()) {
                	try {
                        objDShandler.rollbackTx();
                	} catch (Exception e1) {
                		getZx().trace.addError("Failed to rollback transaction for this datasource", 
                							   objDShandler.getName(), 
                							   e);
                	}
                }
            }
            
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