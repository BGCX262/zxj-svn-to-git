/*
 * Created on Apr 12, 2004 by Michael Brewer
 * $Id: PFLoopOver.java,v 1.1.2.22 2006/07/17 16:28:43 mike Exp $ 
 */
package org.zxframework.web;

import java.util.Iterator;

import org.zxframework.LabelCollection;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.datasources.DSWhereClause;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Pageflow loopover action object.
 * 
 * <pre>
 * 
 * Change    : DGS15APR2003
 * Why       : In Go, previously did a goto errExit after one iteration that failed, but
 *             actually should bow out more gracefully now that we use zXBOMdl to handle
 *             data integrity as we can now get an error status in normal use.
 * 
 * Change    : BD19MAY03
 * Why       : Resolve entities inside the loop to make sure that the
 *             zX.BOContext does not get confused because the same
 *             entity name is used in actions called within the loop
 * 
 * Change    : BD16JAN04
 * Why       : Resolve link action also when nothing to do!
 * 
 * Change    : BD27MAY04
 * Why       : Bug fixed with erroneous query when refine is not set
 * 
 * Change    : DGS02FEB2005
 * Why       : In Go, the change made on 15APR2003 was causing the rollback to be attempted
 *             twice, and unnecessary setting of the error message twice. Also changed the
 *             setting of the error message so that it is only done if there is a messagedefined 
 *             against the loopover. Previously was always overwriting any existing
 *             messages (such as from entity actions) even when the loopover message was blank.
 *             
 * Change    : V1.4:62 - DGS05APR2005
 * Why       : Trap an exception on database commit
 * 
 * Change    : BD3APR05 - V1.5:1
 * Why       : Support for data-sources
 * 
 * Change    : V1.4:62 - DGS05APR2005
 * Why       : Trap an exception on database commit
 *
 * Change    : V1.5:55 - MB15SEP2005
 * Why       : Previously was always overwriting any existing
 *             messages even when the loopover error message was blank.
 *
 * Change    : V1.5:95 - BD30APR06
 * Why       : DBAction in loopOver can have link action
 *
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFLoopOver extends PFAction {
    
    //------------------------ Members
    
	private String query;
	private boolean refinelist;
	private boolean mustrefine;
	private zXType.pageflowTxBehaviour enmtransaction;
	
	private LabelCollection loopoverinfomsg;
	private LabelCollection loopovererrormsg;
	
	private LabelCollection loopoverrefineerrormsg;
	private PFComponent loopoveraction;

    //------------------------ Constructors
    
	/**
	 * Default constructor.
	 */
	public PFLoopOver() {
	    super();
	}
    
    //------------------------ Getters/Setters
    
    /**
     * @return Returns the loopoveraction.
     */
    public PFComponent getLoopoveraction() {
        return loopoveraction;
    }
    
    /**
     * @param loopoveraction The loopoveraction to set.
     */
    public void setLoopoveraction(PFComponent loopoveraction) {
        this.loopoveraction = loopoveraction;
    }
    
    /**
     * The internationalised error message.
     * Should this just be errorMsg ?
     * 
     * @return Returns the loopovererrormsg.
     */
    public LabelCollection getLoopovererrormsg() {
        return loopovererrormsg;
    }
    
    /**
     * @param loopovererrormsg The loopovererrormsg to set.
     */
    public void setLoopovererrormsg(LabelCollection loopovererrormsg) {
        this.loopovererrormsg = loopovererrormsg;
    }
    
    /**
     * The internationalised informational message.
     * Should this be just infoMsg?
     * 
     * @return Returns the loopoverinfomsg.
     */
    public LabelCollection getLoopoverinfomsg() {
        return loopoverinfomsg;
    }
    
    /**
     * @param loopoverinfomsg The loopoverinfomsg to set.
     */
    public void setLoopoverinfomsg(LabelCollection loopoverinfomsg) {
        this.loopoverinfomsg = loopoverinfomsg;
    }
    
    /**
     * The internationalised refine error message.
     * 
     * @return Returns the loopoverrefineerrormsg.
     */
    public LabelCollection getLoopoverrefineerrormsg() {
        return loopoverrefineerrormsg;
    }
    
    /**
     * @param loopoverrefineerrormsg The loopoverrefineerrormsg to set.
     */
    public void setLoopoverrefineerrormsg(LabelCollection loopoverrefineerrormsg) {
        this.loopoverrefineerrormsg = loopoverrefineerrormsg;
    }
    
    /**
     * @return Returns the mustrefine.
     */
    public boolean isMustrefine() {
        return mustrefine;
    }
    
    /**
     * @param mustrefine The mustrefine to set.
     */
    public void setMustrefine(boolean mustrefine) {
        this.mustrefine = mustrefine;
    }
    
    /**
     * The SQL query to perform.
     * 
     * @return Returns the query.
     */
    public String getQuery() {
        return query;
    }
    
    /**
     * @param query The query to set.
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * @return Returns the refinelist.
     */
    public boolean isRefinelist() {
        return refinelist;
    }
    
    /**
     * @param refinelist The refinelist to set.
     */
    public void setRefinelist(boolean refinelist) {
        this.refinelist = refinelist;
    }
    
    /**
     * @return Returns the enmtransaction.
     */
    public zXType.pageflowTxBehaviour getEnmtransaction() {
        return enmtransaction;
    }
    
    /**
     * @param enmtransaction The enmtransaction to set.
     */
    public void setEnmtransaction(zXType.pageflowTxBehaviour enmtransaction) {
        this.enmtransaction = enmtransaction;
    }
    
    //----------------------- Digester helper methods.
    
    /**
     * @deprecated Using BooleanConverter
     * @param refinelist The refinelist to set.
     */
    public void setRefinelist(String refinelist) {
        this.refinelist = StringUtil.booleanValue(refinelist);
    }
    
    /**
     * @deprecated Using BooleanConverter
     * @param mustrefine The mustrefine to set.
     */
    public void setMustrefine(String mustrefine) {
        this.mustrefine = StringUtil.booleanValue(mustrefine);
    }
    
    /**
     * @param transaction The transaction to set.
     */
    public void setTransaction(String transaction) {
        this.enmtransaction = zXType.pageflowTxBehaviour.getEnum(transaction);
    }
    
    //------------------------ Implemented Methods from PFAction
    
    /**
     * Handle this action.
     * 
     * @see PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
    	if (getZx().trace.isApplicationTraceEnabled()) {
    		getZx().trace.enterMethod();
    	}
    	
        zXType.rc go = zXType.rc.rcOK;
        
        // The current return code of a individual loopover action.
        int intTmpRc = zXType.rc.rcOK.pos;
        // The overall return code (The highest of all of the individual loopover actions)
        int intRC = zXType.rc.rcOK.pos;
        /** In case of loop with next action **/
        int intLoopCounter;
        DSHandler objDSHandler = null;
        DSRS objRS = null;
        
        try {
            /**
             * Get entities
             */
            ZXCollection colEntities = getPageflow().getEntityCollection(this, 
            															 zXType.pageflowActionType.patSearchForm, 
            															 zXType.pageflowQueryType.pqtSearchForm);
            if (colEntities == null) {
                throw new Exception("Unable to retrieve entity collection");
            }
            
            /**
             * Set context variable
             */
            getPageflow().setContextEntities(colEntities);
            getPageflow().setContextEntity((PFEntity)colEntities.iterator().next());
            
            /**
             * Reset result in context
             */
            getPageflow().setContextLoopOverError(0);
            getPageflow().setContextLoopOverOk(0);
            
            if (!getPageflow().validDataSourceEntities(colEntities)) {
                throw new Exception("Combination of data-source handlers not supported");
            }
            
            /**
             * Get data-source handler
             */
            objDSHandler = getPageflow().getContextEntity().getDSHandler();
            boolean blnIsDSChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            DSWhereClause objDSWhereClause = new DSWhereClause();
            
            /**
             * Construct the query to use
             */
            String strTmp = getPageflow().resolveDirector(getQuery());
            
            String strSelectClause = getPageflow().getFromContext(strTmp + Pageflow.QRYSELECTCLAUSE);
            String strWhereClause = getPageflow().getFromContext(strTmp + Pageflow.QRYWHERECLAUSE);
            String strOrderByClause = getPageflow().getFromContext(strTmp + Pageflow.QRYORDERBYCLAUSE);
            
            /**
             * (optionally) process a refine list....
             */
            String strRefine = "";
            if (isRefinelist()) {
                /**
                 * Select first if no entity has been given, otherwise select that one....
                 */
                PFEntity objEntity = getPageflow().getContextEntity();
                
                /**
                 * The number of checkboxes for a multilist can be controlled using
                 * the tag zXNumSelectCB. This is only relevant when refining using radio
                 * buttons, where a negative number is used to indicate it is a radio
                 * button, and the absolute value of that indicates which radio
                 * button we are interested in (i.e. which has to be selected to make
                 * it into the query)
                 */
                int intNumSelect = 1;
                
	            String strTag = tagValue("zXNumSelectCB");
	            if (StringUtil.len(strTag) > 0) {
	                if (StringUtil.isNumeric(strTag)) {
	                    intNumSelect = new Integer(strTag).intValue();
	                }
	            } else {
	            	strTag = tagValue("zXNumSelectRB");
	                if (StringUtil.len(strTag) > 0) {
		                if (StringUtil.isNumeric(strTag)) {
		                    intNumSelect = new Integer(strTag).intValue();
		                    if (intNumSelect > 0) {
		                        intNumSelect = intNumSelect * -1;
		                    }
		                }
	                } // zXNumSelectRB tag set?
	            }  // zXNumSelectCBtag set?
                
                strRefine = getPageflow().getPage().processMultilist(getPageflow().getRequest(), objEntity.getBo(), null, intNumSelect);
                if (isMustrefine() && StringUtil.len(strRefine) == 0) {
                    /**
                     * Refine is mandatory and user has not selected any items.....
                     * 
                	 * Only set error message if we have one. Otherwise we might override an existing
                	 * error message.
                     */
                	String strRefineMsg = getPageflow().resolveLabel(getLoopoverrefineerrormsg());
                	if (StringUtil.len(strRefineMsg) > 0) {
                		getPageflow().setErrorMsg(strRefineMsg);
                	}
                	
                    getPageflow().setAction(getPageflow().resolveLink(getLink()));
                    
                    // GoTo okExit
                    return go;
                }
                
                /**
                 * Add parenthesis for to ensure safe query
                 */
                if (blnIsDSChannel) {
                    if (StringUtil.len(strRefine) > 0) {
                        objDSWhereClause.addClauseWithAND(getPageflow().getContextEntity().getBo(), ":" + strRefine);
                    } else {
                        objDSWhereClause.addClauseWithAND(getPageflow().getContextEntity().getBo(), ":0=1");
                    }
                    
                } else {
                    if (StringUtil.len(strRefine) > 0) {
                        strRefine = "(0=1 OR " + strRefine + ")";
                    } else {
                        strRefine = "(0=1)";
                    }
                    
                } // Channel or RDBMS
            }
            
            String strQuery = "";
            if (blnIsDSChannel) {
                if (StringUtil.len(strWhereClause) > 0) {
                    objDSWhereClause.addClauseWithAND(getPageflow().getContextEntity().getBo(), ":" + strWhereClause);
                }
                
            } else {
                /**
                 * Build the full sql query :
                 */
                strQuery = strSelectClause;
                if (StringUtil.len(strWhereClause) == 0) {
                    strWhereClause = strRefine;
                } else {
                    if (StringUtil.len(strRefine) > 0) {
                        strWhereClause = strWhereClause + " AND " + strRefine;
                    }
                }
                
                if (StringUtil.len(strWhereClause) > 0) {
                    strQuery = strQuery + " AND " + strWhereClause;
                }
                
                if (StringUtil.len(strOrderByClause) > 0) {
                    strQuery = strQuery + " ORDER BY " + strOrderByClause;
                }
                
            } // Channel or RDBMS?
            
            /**
             * Determine the maximum number of rows to display
             */
            int intMaxResultRows;
            if (getMaxrows() > 0) {
                intMaxResultRows = getMaxrows();
            } else {
                intMaxResultRows = getMaxrows();
            }
            
            /**
             * Set the limits for the number of rows that we retrieve.
             */
            int lngStartRow = 0;
            int lngBatchSize = 0;
            if (isLimitrows()) {
                // Get one extra to allow for paging.
                lngBatchSize = intMaxResultRows + 1;
            }
            
            /**
             * Start transaction
             */
            objDSHandler.beginTx();
            
            /**
             *  Get result set
             */
            if (blnIsDSChannel) {
                strWhereClause = objDSWhereClause.getAsWhereClause();
                
                if (StringUtil.len(strWhereClause) > 0) strWhereClause = ":" + strWhereClause; 
                
                boolean blnReverse = false;
                if (StringUtil.len(strOrderByClause) > 1 && strOrderByClause.charAt(0) == '-') {
                    strOrderByClause = strOrderByClause.substring(1);
                    blnReverse = true;
                }
                
                objRS = objDSHandler.boRS(getPageflow().getContextEntity().getBo(),
                                          strSelectClause,
                                          strWhereClause,
                                          false,
                                          strOrderByClause,
                                          blnReverse,
                                          lngStartRow, lngBatchSize);
                
            } else {
                objRS = ((DSHRdbms)objDSHandler).sqlRS(strQuery,
                                                       lngStartRow, lngBatchSize);
            }
            
            if (objRS == null) {
                throw new Exception("Unable to execute query");
            }
            
            /**
             * And go go go
             */
            int intRowCount = 1;
            toploop : while (!objRS.eof() && intRowCount < intMaxResultRows) {
                /**
                 * Get the entities again; this may seem overkill but is needed because
                 *  the zX.BOContext may get confused when entities are used
                 *  in any of the actions used within the loop that have the same
                 *  name as ones being used in this action
                 */
                colEntities = getPageflow().getEntityCollection(this, 
                												zXType.pageflowActionType.patSearchForm, 
                												zXType.pageflowQueryType.pqtSearchForm);
                if (colEntities == null) {
                    throw new Exception("Unable to retrieve entity collection");
                }
                
                /**
                 * Set context variable again as it may have been overwritten in the
                 *  action that has been called
                 */
                getPageflow().setContextEntities(colEntities);
                getPageflow().setContextEntity((PFEntity)colEntities.iterator().next());
                
                /**
                 * Populate all the business objects...
                 */
                Iterator iter = colEntities.iterator();
                PFEntity objEntity;
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    objRS.rs2obj(objEntity.getBo(), objEntity.getSelecteditgroup(), objEntity.isResolveFK());
                }
                
                /**
                 * Determine the action to perform
                 */
                String strAction = getPageflow().resolveLink(getLoopoveraction());
                
                /**
                 * v1.5:95 BD30APR06: action in loopOver can have link action - added this
                 * 'Do While' loop and can skip out to new label foundError
                 */
                intLoopCounter = 0;
                while (StringUtil.len(strAction) > 0 && intLoopCounter < 10) {
                    intLoopCounter = intLoopCounter + 1;
                    PFAction objAction = getPageflow().getPFDesc().getAction(strAction);
                    
                    if (objAction == null) {
                        getZx().trace.addError("Unable to retrieve action", strAction);
                        go = zXType.rc.rcError;
                        return go;
                    }
                    
                    intTmpRc = getPageflow().dispatchAction(objAction).pos;
                    
                    /**
                     * The dbaction must NOT have a error action defined. Otherwise
                     * it will return a rcOK.
                     */
//                    if (StringUtil.len(getPageflow().getErrorMsg()) > 0) {
//                    	intTmpRc = zXType.rc.rcError.pos;
//                    }
                    
                    if (intTmpRc == zXType.rc.rcOK.pos) {
                        getPageflow().setContextLoopOverOk(getPageflow().getContextLoopOverOk() + 1);
                        
                    } else {
                    	/**
                    	 * Count the number of errors 
                    	 */
                        getPageflow().setContextLoopOverError(getPageflow().getContextLoopOverError() + 1);
                        
                        /**
                         * If we are dealing with a safe transaction mode (ie or all succeed
                         * or all fail, we might as well stop after the first error
                         * DGS02FEB2005: Previously also set i.pageflow.errorMsg here, but that is
                         * also done at the end, so no need to do here 
                         */
                        if (getEnmtransaction().equals(zXType.pageflowTxBehaviour.ptbSafe)) {
                            objDSHandler.rollbackTx();
                            /**
                             * DGS15APR2003: Previously did a goto errExit here, but actually should
                            ' bow out more gracefully now that we use zXBOMdl to handle data integrity
                            ' as we can now get an error status in normal use. 
                             */
                            intRC = intTmpRc;
                            // GoTo foundError
                            break toploop;
                        }
                    }
                    
                    if (intTmpRc > intRC) {
                        intRC = intTmpRc;
                    }
                
                    strAction = getPageflow().getAction();
                    
                } // Loop over linked dbactions
                
                if (intLoopCounter >= 10) {
                    getZx().trace.addError("Loop counter exceeded 10 linked actions on action " + strAction);
                    return go;
                }
                
                objRS.moveNext();
                
                intRowCount++;
            }
            
            if (intRC == zXType.rc.rcOK.pos) {
            	/**
            	 * V1.4:62 - DGS05APR2005: Commit can fail, so test for it
            	 * In java this will throw an exception.
            	 */
                objDSHandler.commitTx();
                
            } else {
                /**
                 * If transaction mode is safe, rollback if one transaction has failed
                 * for mode is try, commit the ones that have been successful.
                 * DGS02FEB2005: Have already rolled back if safe, so don't do it again
                 */
                if (!getEnmtransaction().equals(zXType.pageflowTxBehaviour.ptbSafe)) {
                    objDSHandler.commitTx();
                }
            }
            
            /**
             * Set the appropriate message
             */
            if (intRC == zXType.rc.rcOK.pos) {
                getPageflow().setInfoMsg(getPageflow().resolveLabel(getLoopoverinfomsg()));
                
            } else {
                /**
                 *  DGS02FEB2005: May have a message already, such as from an entity action that failed,
                 *  so only overwrite with the loopover's own message if there actually is a message
                 *  defined specifically for the loopover i.e. don't blank anything out
                 */
                strTmp = getPageflow().resolveLabel(getLoopovererrormsg()); 
                if (StringUtil.len(strTmp) > 0) {
                    getPageflow().setErrorMsg(strTmp);
                }
                
            }
            
            getPageflow().setAction(getPageflow().resolveLink(getLink()));
            
            return go;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process action.", e);
            
            /**
             * Rollback for safety.
             */
            if (getEnmtransaction().equals(zXType.pageflowTxBehaviour.ptbSafe)) {
                if (objDSHandler != null && objDSHandler.inTx()) {
                    objDSHandler.rollbackTx();
                }
            }
            
            if (getZx().throwException) throw new ZXException(e);
            go = zXType.rc.rcError;
            return go;
        } finally {
            /**
             * Close resultset.
             */
            if (objRS != null) objRS.RSClose();
            
            if(getZx().trace.isApplicationTraceEnabled()) {
                getZx().trace.returnValue(go);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ PFAction overidden methods
    
    /** 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();
        
        objXMLGen.taggedCData("query", getQuery());
        objXMLGen.taggedValue("refinelist", isRefinelist());
        objXMLGen.taggedValue("mustrefine", isMustrefine());
        objXMLGen.taggedValue("transaction", zXType.valueOf(getEnmtransaction()));
        
        getDescriptor().xmlLabel("loopoverinfomsg", getLoopoverinfomsg());
        getDescriptor().xmlLabel("loopovererrormsg", getLoopovererrormsg());
        getDescriptor().xmlLabel("loopoverrefineerrormsg", getLoopoverrefineerrormsg());
        
        getDescriptor().xmlComponent("loopoveraction", getLoopoveraction());
    }
    
    /**
     * @see PFAction#clone(Pageflow)
     */
    public PFAction clone(Pageflow pobjPageflow) {
        PFLoopOver cleanClone = (PFLoopOver)super.clone(pobjPageflow);
        
        if (getLoopoveraction() != null) {
            cleanClone.setLoopoveraction((PFComponent)getLoopoveraction().clone());
        }
        
        if (getLoopovererrormsg() != null) {
            cleanClone.setLoopovererrormsg((LabelCollection)getLoopovererrormsg().clone());
        }
        
        if (getLoopoverinfomsg() != null) {
            cleanClone.setLoopoverinfomsg((LabelCollection)getLoopoverinfomsg().clone());
        }
        
        if (getLoopoverrefineerrormsg() != null) {
            cleanClone.setLoopoverrefineerrormsg((LabelCollection)getLoopoverrefineerrormsg().clone());
        }
        
        cleanClone.setMustrefine(isMustrefine());
        cleanClone.setQuery(getQuery());
        
        cleanClone.setRefinelist(isRefinelist());
        cleanClone.setEnmtransaction(getEnmtransaction());
        
        return cleanClone;
    }
}