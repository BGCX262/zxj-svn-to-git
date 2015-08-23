/*
 * Created on Apr 12, 2004 by Michael Brewer
 * $Id: PFMatrixCreateUpdate.java,v 1.1.2.19 2006/07/17 16:27:35 mike Exp $ 
 */
package org.zxframework.web;

import java.util.Iterator;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.LabelCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHandler;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringUtil;

/**
 * Pageflow matrix createUpdate action object.
 * 
 * <pre>
 * 
 * Change    : DGS09MAR2004
 * Why       : - Bug fix to prevent error when no rows or cols
 *             - More sophisticated comparison of changed BO required so new local
 *               function 'cellToUpdate' introduced to replace simple BO 'compare'.
 *             - Bug fix: when counting blank fields, don't consider locked fields.
 *             - Bug fix: class name was wrong (as used in log/debug trace).
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
 * Change    : BD5APR05 - V1.5:1
 * Why       : Add support for data sources
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
public class PFMatrixCreateUpdate extends PFAbstractCreateUpdate {

    //------------------------ Members
	
    //------------------------ Runtime members
    
    private int blankFieldsAll;
    private int blankFieldsNull;
    private int blankFieldsNullMand;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFMatrixCreateUpdate() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    //------------------------ Private helper methods.
    
    /**
     * Determine blank fields for a particular matrix point
     *
     * @param pobjIBO The Business Object. 
     * @param pstrGroup The attribute group restrictor. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if blankFields fails. 
     */
    private zXType.rc blankFields(ZXBO pobjIBO, String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjIBO", pobjIBO);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        zXType.rc blankFields = zXType.rc.rcOK; 

        try {
            
            // int are parsed by value and not by reference.
            // Reset to 0.
            this.blankFieldsAll = 0;
            this.blankFieldsNull = 0;
            this.blankFieldsNullMand = 0;
            
            AttributeCollection colAttr = pobjIBO.getDescriptor().getGroup(pstrGroup);
            
            Attribute objAttr;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                /**
                 * DGS09MAR2004: Don't count locked fields in any of this
                 */
                if (!objAttr.isLocked()) {
                    this.blankFieldsAll++;
                    
                    if (!objAttr.isOptional() && pobjIBO.getValue(objAttr.getName()).isNull) {
                        this.blankFieldsNullMand++;
                    }
                    
                    // False is null
                    if (pobjIBO.getValue(objAttr.getName()).isNull ||
                        (objAttr.getDataType().pos == zXType.dataType.dtBoolean.pos  && !pobjIBO.getValue(objAttr.getName()).booleanValue())     
                    	) {
                        this.blankFieldsNull++;
                    }
                }
            }
            
            return blankFields;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine blank fields for a particular matrix point", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjIBO = "+ pobjIBO);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            blankFields = zXType.rc.rcError;
            return blankFields;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(blankFields);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Determine if cell is to be deleted
     *
     * @param pstrDeleteWhen An expression which needs to be resolved to decide whether this cell can be deleted. 
     * @return Returns true if the cell can be deleted.
     * @throws ZXException Thrown if cellToDelete fails. 
     */
    private boolean cellToDelete(String pstrDeleteWhen) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDeleteWhen", pstrDeleteWhen);
        }

        boolean cellToDelete = false; 
        
        try {
            
            if (StringUtil.len(pstrDeleteWhen) == 0) {
                cellToDelete = false;
                return cellToDelete;
            }
            cellToDelete = StringUtil.booleanValue(getPageflow().resolveDirector(pstrDeleteWhen));
            
            return cellToDelete;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine if cell is to be deleted", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrDeleteWhen = "+ pstrDeleteWhen);
            }
            if (getZx().throwException) throw new ZXException(e);
            return cellToDelete;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(cellToDelete);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Determine blank fields for a particular matrix point
     *
     * @param pobjIBODB Entity from 
     * @param pobjIBOForm The Entity form from 
     * @param pstrGroup The attribute group used to compare 
     * @return Returns the pesist status enumator.
     * @throws ZXException Thrown if cellToUpdate fails. 
     */
    private zXType.persistStatus cellToUpdate(ZXBO pobjIBODB, ZXBO pobjIBOForm, String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjIBODB", pobjIBODB);
            getZx().trace.traceParam("pobjIBOForm", pobjIBOForm);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }

        zXType.persistStatus cellToUpdate = zXType.persistStatus.psClean;
        
        try {
            AttributeCollection colAttr = pobjIBODB.getDescriptor().getGroup(pstrGroup);
            Attribute objAttr;
            
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                /**
                 * Don't include locked fields in any of this
                 */
                if (!objAttr.isLocked()) {
                    if (pobjIBODB.getValue( objAttr.getName() ).compareTo(pobjIBOForm.getValue(objAttr.getName())) != 0) {
                        cellToUpdate = zXType.persistStatus.psDirty;
                        return cellToUpdate;
                    }
                }
            }
            
            return cellToUpdate;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine blank fields for a particular matrix point", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjIBODB = "+ pobjIBODB);
                getZx().log.error("Parameter : pobjIBOForm = "+ pobjIBOForm);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return cellToUpdate;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(cellToUpdate);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Implemented Methods from PFAction
    
    /**
     * Handle this action.
     * 
     * <pre>
     * 
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
		
        zXType.rc go = zXType.rc.rcOK;
		int intRC = zXType.rc.rcOK.pos;
		boolean blnCheckboxChecked = false;
		boolean blnFoundErrors = false;
        DSHandler objDSHandler = null;
        
		try {
		    /**
		     * Get handle to the linked editForm action and actual action implementation
		     */
		    PFMatrixEditForm objLinkedEditFormAction = (PFMatrixEditForm)getPageflow().getPFDesc().getAction(getLinkededitform());
		    if (objLinkedEditFormAction == null) {
		        throw new Exception("LinkedEditForm action not found : " + getLinkededitform());
		    }
		    String strLinkedEditFormAction = objLinkedEditFormAction.getName();
		    
		    /**
		     * Determine how progress stored in the query string
		     */
		    String strQSProgress = (StringUtil.len(objLinkedEditFormAction.getQsprogress()) == 0 ? "-prgs" : objLinkedEditFormAction.getQsprogress());
		    
		    /**
		     * Check if we are already in progress
		     */
		    boolean blnInProgress = (StringUtil.len(getPageflow().getQs().getEntryAsString(strQSProgress)) > 0);
            if (blnInProgress) {
                // ?? Wait ??
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
                                                                         zXType.pageflowActionType.patCreateUpdate,
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
             * Data source handler
             */
            PFEntity objTheEntity = getPageflow().getContextEntity();
            objDSHandler = objTheEntity.getDSHandler();
            
		    /**
		     * Get the count of rows and columns in the matrix edit form and the attr names for FKs.
		     */
		    int intColCountTotal = new Integer(getPageflow().getRequest().getParameter("zXMatrixEditFormColCount" + strLinkedEditFormAction)).intValue();
		    int intRowCountTotal = new Integer(getPageflow().getRequest().getParameter("zXMatrixEditFormRowCount" + strLinkedEditFormAction)).intValue() + 1;
		    
		    String strRowPKAttr = getPageflow().getRequest().getParameter("zXMatrixEditFormRowPKAttr" + strLinkedEditFormAction);
		    String strColPKAttr = getPageflow().getRequest().getParameter("zXMatrixEditFormColPKAttr" + strLinkedEditFormAction);
		    
		    /**
		     * Size the 4D array that will hold persist status of each cell entity. We need this
		     * array because we cannot mess with the saver BOs until time for the persist itself.
		     * DGS09MAR2004: Don't do this if no rows or columns or it will fail.
		     */
		    zXType.persistStatus aenmPersistStatus[][][] = new zXType.persistStatus[0][0][0];
		    if (intColCountTotal > 0 && intRowCountTotal > 0) {
		        aenmPersistStatus = new zXType.persistStatus[colEntities.size()][intColCountTotal][intRowCountTotal];
		    }
		    
		    /**
		     * Process the edit form to see if any input validation errors have been
		     * made...
		     */
		    String strColPK;
		    PFEntity objEntity;
		    String strStoreAlias;
		    
		    for (int intColCount = 0; intColCount < intColCountTotal; intColCount++) {
		        strColPK = getPageflow().getRequest().getParameter("zXMatrixEditFormColPK" + intColCount + strLinkedEditFormAction);
		        
		        for (int intRowCount=0;  intRowCount < intRowCountTotal; intRowCount++) {
		            String strRowColId = StringUtil.right(String.valueOf(intRowCount),'0', 5) + StringUtil.right(String.valueOf(intColCount),'0', 5);
		            String strRowPK = getPageflow().getRequest().getParameter("zXMatrixEditFormRowPK" + intRowCount + strLinkedEditFormAction);
		            
		            int intEntityCount = -1;
		            Iterator iter = colEntities.iterator();
		            while (iter.hasNext()) {
		            	objEntity = (PFEntity)iter.next();
		            	intEntityCount++;
		            	
		            	/**
		            	 * Tweak the BO alias, so that each row and entity's fields are uniquely named
		            	 */
		            	strStoreAlias = objEntity.getBo().getDescriptor().getAlias();
		            	objEntity.getBo().getDescriptor().setAlias(objEntity.getName() + objLinkedEditFormAction.getName() + "Mtrx" + strRowColId);
		            	
		            	/**
		            	 * set the global context
		            	 */
		            	getPageflow().setContextEntity(objEntity);
		            	
		            	/**
		            	 * Allow any attribute values to be set to fixed values before
		            	 * populating the properties by processing the submitted form
		            	 */
		            	getPageflow().processAttributeValues(objEntity);
		            	
		            	ZXBO objIBO = null;
		            	if (StringUtil.len(objEntity.getSelecteditgroup()) > 0) {
		            	    /**
		            	     * Read the form values into the save object (this has
		            	     * validation switched off and thus accepts bad input)
		            	     */
		            	    objIBO = objEntity.getBo().cloneBO();
		            	    objIBO.setValidate(false);
		            	    
		            	    objEntity.getBo().bo2bo(objIBO);
		            	    
		            	    /**
		            	     * Get the persist status from the edit form. This tells us whether the row
		            	     * is new or existing. Then load it into the array of persist statuses. From now
		            	     * on we might change the array value, but not that in the BO until the point of
		            	     * doing the persist.
		            	     */
		            	    objIBO.setPersistStatus(
		            	            zXType.persistStatus.getEnum(
		            	                    getPageflow().getRequest().getParameter("zXMatrixPersist" + objEntity.getBo().getDescriptor().getAlias()))
		            	            );
			            	aenmPersistStatus[intEntityCount] [intColCount] [intRowCount] = objIBO.getPersistStatus();
			            	
			            	/**
			            	 * If no visible fields (just a checkbox), get the value of the checkbox from the
			            	 * edit form. This tells us whether the row is to be deleted or inserted, or no change
			            	 */
			            	if (StringUtil.len(objEntity.getVisiblegroup()) == 0) {
			            	    intRC = zXType.rc.rcOK.pos;
			            	    blnCheckboxChecked = (StringUtil.len(getPageflow().getRequest().getParameter("zXMatrixCheck" + this.getPageflow().getPage().controlName(objIBO))) > 0);
			            	}
			            	
			            	intRC = this.getPageflow().getPage().processMatrixEditForm(this.getPageflow().
                                                                                      getRequest(),
			            	        												  objIBO, 
                                                                                      objEntity.getSelecteditgroup()).pos;
			            	
			            	/**
			            	 * Reset the BO alias to its original value as soon as possible, to avoid any nasty problems
			            	 */
			            	objIBO.getDescriptor().setAlias(strStoreAlias);
			            	
			            	/**
			            	 * Set the FKs
			            	 */
			            	objIBO.setValue(strRowPKAttr, strRowPK);
			            	objIBO.setValue(strColPKAttr, strColPK);
			            
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
			            	
			            	/**
			            	 * set the context for the bo
			            	 */
			            	getZx().getBOContext().setEntry(objEntity.getName(), objIBO);
			            	
			            	if (intRC == zXType.rc.rcOK.pos) {
			            	    /**
			            	     * Count blank fields for this cell. We want to know how many fields in total
			            	     * (blank or not), how many blank fields and how many blank mandatory fields
			            	     */
			            	    if (!blankFields(objIBO, objEntity.getVisiblegroup()).equals(zXType.rc.rcOK)) {
			            	        throw new Exception("Fails to calculate blank field");
			            	    }
                                
			            	    if (aenmPersistStatus[intEntityCount] [intColCount] [intRowCount].equals(zXType.persistStatus.psNew) ) {
			            	        
			            	        if ((StringUtil.len(objEntity.getVisiblegroup()) > 0 && blankFieldsNull == blankFieldsAll	) 
			            	                || (StringUtil.len(objEntity.getVisiblegroup()) == 0 && !blnCheckboxChecked)) {
                                        
			            	            /**
			            	             * If a new row but nothing entered in the visible fields for this cell, or
			            	             * no visible fields (checkbox only) and a new row and checkbox not checked,
			            	             * we are not going to do anything, so set the persist status to clean
			            	             */
			            	            aenmPersistStatus[intEntityCount][intColCount][intRowCount] = zXType.persistStatus.psClean;
			            	        } else {
			            	            if (blankFieldsNullMand > 0) {
			            	                /**
			            	                 * If a new row and a mandatory field is blank and at least one field has been entered:
			            	                 */
			            	                intRC = zXType.rc.rcError.pos;
			            	                getZx().trace.userErrorAdd("Mandatory fields not entered", "", ((Attribute)objIBO.getDescriptor().getGroup(objEntity.getVisiblegroup()).iterator().next()).getName() );
			            	            }
			            	        }
			            	        
			            	    } else {
			            	        /**
			            	         * Not a new row: see if we need to delete it
			            	         * First re-read the existing row  - partly to see if anything changed
			            	         * but first here to see if the row has since been deleted elsewhere.
			            	         */
			            	        ZXBO objIBOTmp = null;
			            	        objIBOTmp = getZx().getBos().quickLoad(objIBO.getDescriptor().getName(), objIBO.getPKValue(), null, objEntity.getSelecteditgroup());
			            	        if (objIBOTmp == null) {
			            	            intRC = zXType.rc.rcError.pos;
			            	            getZx().trace.userErrorAdd("Data has been removed by someone else", 
			            	            						   "", 
			            	            						   ((Attribute)objIBO.getDescriptor().getGroup(objEntity.getVisiblegroup()).iterator().next()).getName());
			            	        } else {
			            	            if (StringUtil.len(objEntity.getVisiblegroup()) == 0) {
			            	                /**
			            	                 * No visible fields (only a checkbox) so we are going to either delete it
			            	                 * if the checkbox is now unchecked, or do nothing if it remains checked
			            	                 */
			            	                aenmPersistStatus[intEntityCount] [intColCount] [intRowCount] = blnCheckboxChecked ? zXType.persistStatus.psClean : zXType.persistStatus.psDeleted;
			            	            } else {
			            	                /**
			            	                 * Evaluate expression to see if this cell is to be deleted. If so
			            	                 * set persist status to delete. A typical use of this would be to have
			            	                 * an expression that evaluates true when one important field in the cell
			            	                 * is blank, or perhaps when all visible fields are blank.
			            	                 */
			            	                if (cellToDelete(objLinkedEditFormAction.getDeletewhen())) {
			            	                    aenmPersistStatus[intEntityCount][intColCount][intRowCount] = zXType.persistStatus.psDeleted;
			            	                } else {
			            	                    /**
			            	                     * If an existing row, and a mandatory field is blank it is an error situation.
			            	                     */
			            	                    if (blankFieldsNullMand > 0) {
			            	                        intRC = zXType.rc.rcError.pos;
			            	                        getZx().trace.userErrorAdd("Mandatory fields not entered", "", ((Attribute)objIBO.getDescriptor().getGroup(objEntity.getVisiblegroup()).iterator().next()).getName());
			            	                    } else {
			            	                        /**
			            	                         * Compare with the existing row and set the persist status to 'dirty'
			            	                         * if anything changed.
			            	                         * DGS09MAR2004: Call new local function to see if cell needs to be updated.
			            	                         * Can't just compare the BOs as was the case because the loaded BO might
			            	                         * have virtual columns, and these won't get loaded into an iBO but might
			            	                         * be present from the screen. So have to compare each unlocked field.
			            	                         */
			            	                        aenmPersistStatus[intEntityCount][intColCount][intRowCount] = cellToUpdate(objIBOTmp, objIBO, objEntity.getSelecteditgroup());
			            	                    }
			            	                } // Need to delete
                                            
			            	            } // Has visible group
                                        
			            	        } // Load existing row
                                    
			            	    } // New ?
                                
			            	} // All ok?
			            	
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
			            	     * Set an object of  a specific name into the object model of the BO
			            	     * so that the matrix edit form can spot errors and use a different
			            	     * style class.
			            	     */
			            	    objIBO.getOM().put("zXMatrixErr", new StringProperty("Y", false));
			            	    
			            	    /**
			            	     * Unlike normal and grid edit form we do not need to repeat
			            	     * the process edit form with validation off, because it was
			            	     * already switched off the first time through. So all the
			            	     * user's data will already be in the saver BO as far as possible.
			            	     */
			            	}
		            	}
		            	objEntity.getBOSavers().put(strRowColId, objIBO);
		        	} // Entity
                    
		        } // Row
                
            } // Column
		    
            int updated = 0;
            int inserted = 0;
            int deleted = 0;
            
		    /**
		     * If we have come across an error....
		     */
		    if (blnFoundErrors) {
		        /**
		         * Add error message to the i.pageflow error message, and append trace stack
		         * DGS16JUL2004: First collect any error messages defined against this action
		         */
		        getPageflow().collectMessages(this, true, zXType.logLevel.llError.pos);
		        
		        getPageflow().setErrorMsg(getPageflow().getErrorMsg() + "\n" + getZx().trace.formatStack(false));
		        
		        /**
		         * Tell the editform that we are in progress and that we have
		         * come across an error.
		         */
		        getPageflow().getQs().setEntry(strQSProgress, strLinkedEditFormAction.toLowerCase());
		        getPageflow().getQs().setEntry("-err", strLinkedEditFormAction.toLowerCase());
		        
		        /**
		         * In case of a warning, add the warning to the QS as well
		         */
		        if (intRC == zXType.rc.rcWarning.pos) {
		            getPageflow().getQs().setEntry("-wrn", "Y");
		        }
		        
		        /**
		         * Go to related editForm again
		         * 
		         * To the editform startaction if there is one defined:
		         */
		        if (StringUtil.len(getEditformstartaction().getAction()) == 0) {
		            getPageflow().setAction(getPageflow().resolveDirector(strLinkedEditFormAction));
		        } else {
		            getPageflow().setAction(getPageflow().resolveLink(getEditformstartaction()));
		        }
		        
		    } else {
		        /**
                 * No errors 
		         */
		        objDSHandler.beginTx();
		        
		        for (int intColCount = 0; intColCount < intColCountTotal; intColCount++) {
		            for(int intRowCount =0; intRowCount < intRowCountTotal; intRowCount++) {
		                String strRowColId = StringUtil.right(String.valueOf(intRowCount),'0', 5) + StringUtil.right(String.valueOf(intColCount),'0', 5);
		                
		                int intEntityCount = -1;
		                Iterator iter = colEntities.iterator();
		                while (iter.hasNext()) {
		                    objEntity = (PFEntity)iter.next();
		                    
		                    intEntityCount++;
		                    
		                    ZXBO objIBO = (ZXBO)objEntity.getBOSavers().get(strRowColId);
		                    
		                    /**
		                     * DGS01MAR2004: set the context for the bo
		                     */
		                    getZx().getBOContext().setEntry(objEntity.getName(), objIBO);
		                    
		                    /**
		                     * Bit of complexity here. First of all, store the BO's current persist status.
		                     * Then set the BO to the array's value, which could have been manipulated by us
		                     * to make it a delete or clean, or could remain unchanged as the new or dirty
		                     * it started as from the process edit form.
		                     */
		                    zXType.persistStatus enmTmpPersistStatus = objIBO.getPersistStatus();
		                    objIBO.setPersistStatus(aenmPersistStatus[intEntityCount][intColCount][intRowCount]);
		                    
		                    if (objIBO.getPersistStatus().equals(zXType.persistStatus.psNew)) {
		                        /**
		                         * Finally we can set automatics for new rows.
		                         */
		                        objIBO.setAutomatics("+"); 
		                    }
		                    
		                    /**
		                     * BD16JUL04 - Before we do the actual persist, we switch
		                     * back on the validation (is off for the processEditForms).
		                     * This is needed as we may do validations (e.g. eventActions)
		                     * as part of the pre- / post persist and this may check on
		                     * the validate flag
		                     */
		                    objIBO.setValidate(true);
		                    
		                    /**
		                     * Only persist if we have updated something.
		                     */
		                    if (!objIBO.getPersistStatus().equals(zXType.persistStatus.psClean)) {
		                    	/**
		                    	 * Keep track of the number of updated records.
		                    	 */
                                if (objIBO.getPersistStatus().equals(zXType.persistStatus.psNew)) {
                                    inserted++;
                                } else if (objIBO.getPersistStatus().equals(zXType.persistStatus.psDirty)) {
                                    updated++;
                                } else {
                                    deleted++;
                                }
                                
                                intRC = objIBO.persistBO(objEntity.getSelecteditgroup()).pos;
                                
                            }
		                    
		                    /**
		                     * Immediately set the persist status back to what it was in the BO,
		                     * because the 'persistBO' method will have made it 'clean', and if
		                     * we get any errors we are going to re-use the collection of BO savers
		                     * to redisplay the editform, and that depends on knowing whether cells
		                     * are 'new' or not. Unlikely to get an error at this stage mind you.
		                     */
		                    objIBO.setPersistStatus(enmTmpPersistStatus);
		                    
		                    if (intRC != zXType.rc.rcOK.pos) {
		                        /**
		                         * If something went wrong in the persist, set
		                         * a flag on the BO in error so we can highlight this
		                         * row in the associated edit form
		                         */
		                        objIBO.getOM().put("zXMatrixErr", new StringProperty("Y"));
		                        
		                        if (intRC == zXType.rc.rcError.pos) {
		                            getPageflow().setErrorMsg(getZx().trace.formatStack(false));
		                        } else {
		                            getPageflow().setErrorMsg("Data updated by another user since it was retrieved");
		                        }
		                        
		                        objDSHandler.rollbackTx();
		                        
		                        /**
		                         * Tell the editform that we are in progress and that we have
		                         * come across an error
		                         */
		        		        getPageflow().getQs().setEntry(strQSProgress, strLinkedEditFormAction.toLowerCase());
		        		        getPageflow().getQs().setEntry("-err", strLinkedEditFormAction.toLowerCase());
		        		        
		        		        /**
		        		         * Go to related editForm again
		        		         * 
		        		         * To the editform startaction if there is one defined:
		        		         */
		        		        if (getEditformstartaction() == null || StringUtil.len(getEditformstartaction().getAction()) == 0) {
		        		            getPageflow().setAction(getPageflow().resolveDirector(strLinkedEditFormAction));
		        		        } else {
		        		            getPageflow().setAction(getPageflow().resolveLink(getEditformstartaction()));
		        		        }
		        		        
		        		        /**
		        		         * Skip the rest.
		        		         */
		        		        // GoTo okExit
		        		        return go;
		        		        
		                    } // Persist ok ?
                            
		                } // Entities
                        
		            } // Rows
                    
		        } // Columns
		        
		        /**
		         * V1.4:62 - DGS05APR2005: Commit can fail, so test for it
		         * NOTE : In java this will throw an exception.
		         */
                objDSHandler.commitTx();
                
		        /**
		         * Clear the error and progress flag from the QS
		         */
		        getPageflow().getQs().removeEntry("-err");
		        getPageflow().getQs().removeEntry(strQSProgress);
		        
                /**
                 * Temp hack to show that we can get the number of updated fields..
                 * The problem is that we presume what the user want to use the info message for. 
                 * 
                 * ie : "Updated xxxx"
                 */
                if (updated + inserted + deleted != 0) {
                    StringBuffer infoMessage = new StringBuffer();
                    String strMsg = getPageflow().resolveLabel(getInfomsg());
                    if (strMsg != null && strMsg.length() > 0) infoMessage.append(strMsg).append("\n"); // Add new line.
                    setInfomsg(new LabelCollection());
                    if (inserted>0) {
                        infoMessage.append(inserted).append(" inserted").append(updated>0||deleted>0?", ":"");
                    }
                    if (updated>0) {
                        infoMessage.append(updated).append(" updated").append(deleted>0?", ":"");
                    }
                    if (deleted>0) {
                        infoMessage.append(deleted).append(" deleted");
                    }
                    getPageflow().setInfoMsg(infoMessage.toString());
                } else {
                    // Nulify :).
                    setInfomsg(new LabelCollection());
                    //getPageflow().setInfoMsg("Nothing happened???");
                }
                
		        /**
		         * DGS16JUL2004: Collect any messages (not error messages) ready for display by next visible page
		         */
		        getPageflow().collectMessages(this, true, zXType.logLevel.llInfo.pos);
		        
		        getPageflow().setAction(getPageflow().resolveLink(getLink()));
		        
		    } // No errors found
		    
		    return go;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Execute the Matrix Create/Update pageflow action.", e);
		    
		    /**
		     * Rollback any active transaction
		     */
            if (objDSHandler != null && objDSHandler.inTx()) {
                objDSHandler.rollbackTx();
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