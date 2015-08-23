/*
 * Created on Apr 14, 2004 by Michael Brewer
 * $Id: PFDBAction.java,v 1.1.2.19 2006/07/17 16:28:43 mike Exp $ 
 */
package org.zxframework.web;

import java.util.Iterator;

import org.zxframework.LabelCollection;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Pageflow DBAction action object.
 * 
 * <pre>
 * 
 * Change    : DGS15APR2003
 * Why       : In Go, pass back the return code from the DB update. If in a loopover
 *             it needs to know if this was successful or not.
 * 
 * Change    : DGS24APR2003
 * Why       : Added errorAction property - in case of error, go to this action next (if set).
 * 
 * Change    : BD9JUN04
 * Why       : Added support for concurrencyControl (rather than auditable) and
 * 			   limit audit columns to ~ instead of !
 * 
 * Change    : BD19OCT04
 * Why       : When action type is update and a where group has been set, do not try to load
 *             single instance first as we do not wish to update by pk
 *             
 * Change    : V1.4:62 - DGS05APR2005
 * Why       : Trap an exception on database commit
 * 
 * Change    : BD2APR05 - V1.5:1
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
public class PFDBAction extends PFAction {

    //------------------------ Members
    
    private String dbactionentity;
    private boolean reset;
    private boolean setautomatics;
    private LabelCollection dbactioninfomsg;
    private LabelCollection dbactionerrormsg;
    private zXType.pageflowDBActionType dbActionType;
    private PFComponent erroraction;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFDBAction() {
        super();
    }

    //------------------------ Getters and Setters
    
    /** @return Returns the dbactionentity. */
    public String getDbactionentity() {
        return dbactionentity;
    }

    /**
     * @param dbactionentity The dbactionentity to set.
     */
    public void setDbactionentity(String dbactionentity) {
        this.dbactionentity = dbactionentity;
    }

    /** 
     * @return Returns the dbactionerrormsg. 
     */
    public LabelCollection getDbactionerrormsg() {
        return dbactionerrormsg;
    }

    /**
     * @param dbactionerrormsg The dbactionerrormsg to set.
     */
    public void setDbactionerrormsg(LabelCollection dbactionerrormsg) {
        this.dbactionerrormsg = dbactionerrormsg;
    }

    /** 
     * @return Returns the dbactioninfomsg. 
     */
    public LabelCollection getDbactioninfomsg() {
        return dbactioninfomsg;
    }

    /**
     * @param dbactioninfomsg The dbactioninfomsg to set.
     */
    public void setDbactioninfomsg(LabelCollection dbactioninfomsg) {
        this.dbactioninfomsg = dbactioninfomsg;
    }

    /**
     * The type of database action to perform.
	 * @return Returns the dbActionType.
	 */
	public zXType.pageflowDBActionType getDbActionType() {
		return dbActionType;
	}

	/**
	 * @param dbActionType The dbActionType to set.
	 */
	public void setDbActionType(zXType.pageflowDBActionType dbActionType) {
		this.dbActionType = dbActionType;
	}
    
	/** 
     * @return Returns the erroraction. 
     */
    public PFComponent getErroraction() {
        return erroraction;
    }

    /**
     * @param erroraction The erroraction to set.
     */
    public void setErroraction(PFComponent erroraction) {
        this.erroraction = erroraction;
    }

    /** 
     * @return Returns the reset. 
     */
    public boolean isReset() {
        return reset;
    }

    /**
     * @param reset The reset to set.
     */
    public void setReset(boolean reset) {
        this.reset = reset;
    }
    
    /** 
     * @return Returns the setautomatics. 
     */
    public boolean isSetautomatics() {
        return setautomatics;
    }

    /**
     * @param setautomatics The setautomatics to set.
     */
    public void setSetautomatics(boolean setautomatics) {
        this.setautomatics = setautomatics;
    }

    //------------------------ Digester helper methods

    /**
     * @deprecated Using BooleanConverter
     * @param reset The reset to set.
     */
    public void setReset(String reset) {
        this.reset = StringUtil.booleanValue(reset);
    }
    
    /**
     * @deprecated Using BooleanConverter
     * @param setautomatics The setautomatics to set.
     */
    public void setSetautomatics(String setautomatics) {
        this.setautomatics = StringUtil.booleanValue(setautomatics);
    }
    
    /**
     * Special set method used by Digester.
     * 
     * @param dbactiontype The dbactiontype to set.
     */
    public void setDbactiontype(String dbactiontype) {
        this.dbActionType = zXType.pageflowDBActionType.getEnum(dbactiontype);
    }
    
    //------------------------ Implemented Methods from PFAction
    
    /**
     * Handle this action.
     * 
     * Reviewed for V1.5:1
     * 
     * @see PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
		if(getZx().trace.isApplicationTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
        zXType.rc go = zXType.rc.rcOK;
        
		try {
            /**
             * Get all entities
             */
            ZXCollection colEntities = getPageflow().getEntityCollection(this,
                                                                         zXType.pageflowActionType.patSearchForm, 
                                                                         zXType.pageflowQueryType.pqtSearchForm);
            if (colEntities == null) {
                throw new Exception("Unable to retrieve entity collection for : " +  getName());
            }
            
            /**
             * Set context variable
             */
            getPageflow().setContextEntities(colEntities);
            getPageflow().setContextEntity((PFEntity)colEntities.iterator().next());
            
            /**
             * Load any entities that may be required to set any attributes
             */
            PFEntity objActionEntity;               // Entity to apply action to
		    String strPK = "";
            PFEntity objEntity;                     // Loop over variable
            
		    Iterator iter = colEntities.iterator();
		    while (iter.hasNext()) {
		        objEntity = (PFEntity)iter.next();
		        
		        /**
		         * Load all entities OTHER than the one that we want to do something with
		         * as we may refer to them in directors
		         */
		        if (!objEntity.getName().equalsIgnoreCase(getDbactionentity())) {
		            strPK = getPageflow().resolveDirector(objEntity.getPk());
		            
		            if (StringUtil.len(strPK) > 0) {
		                objEntity.getBo().setPKValue(strPK);
		                objEntity.getBo().loadBO(objEntity.getSelecteditgroup());
		            }
		        }
		    }
            
		    /**
		     * Now for the real thing
		     * First set any attributes that need to be set...
		     */
		    objActionEntity = (PFEntity)colEntities.get(getDbactionentity());
		    if (objActionEntity == null) {
		        throw new Exception("Unable to retrieve DBAction entity " + getDbactionentity());
		    }
		    getPageflow().setContextEntity(objActionEntity);
		    
		    /**
		     * Set all attributes to default values and generate new unique numbers
		     * This does not make much sense for deletes
		     */
		    if (zXType.pageflowDBActionType.pdaInsert.equals(getDbActionType())) {
		        if (isReset()) {
                    objActionEntity.getBo().resetBO();
		        }
		        if (isSetautomatics()) {
                    objActionEntity.getBo().setAutomatics("+");
		        }
                
		    } else if (zXType.pageflowDBActionType.pdaUpdate.equals(getDbActionType())) {
                /**
                 * When dealing with an update, we have to load from thye DB what we actually
                 * want to update.
                 * BD19OCT04 - This is NOT the case when a where group has been set as we will
                 * update by wheregroup and not by pk
                 */
		        strPK = getPageflow().resolveDirector(objActionEntity.getPk());
		        if (StringUtil.len(strPK) > 0) {
                    objActionEntity.getBo().setPKValue(strPK);
                    objActionEntity.getBo().loadBO(objActionEntity.getSelecteditgroup());
                    
		        } else if (StringUtil.len(objActionEntity.getPkwheregroup()) == 0 ){
		            getZx().trace.addError("No PK nor a where group available for entity", 
		            					   objActionEntity.getName());
                    return zXType.rc.rcError;
		        }
                
		    } else if (getDbActionType().equals(zXType.pageflowDBActionType.pdaDelete)) {
		        strPK = getPageflow().resolveDirector(objActionEntity.getPk());
		        if (StringUtil.len(strPK) > 0) {
                    objActionEntity.getBo().setPKValue(strPK);
		            
		            /**
		             * If about to delete an auditable entity, make sure that we
		             * load the audit columns since they are part of the where
		             * clause of the delete...
		             * BD9JUN04 - Now concurrency control
		             */
		            if (objActionEntity.getBo().getDescriptor().isConcurrencyControl()) {
                        objActionEntity.getBo().loadBO("~");
		            }
		        }
                
		    } // Database action type
		    
		    /**
		     * Now set any attributes based on the attrValues collection
		     */
		    getPageflow().processAttributeValues(objActionEntity);
		    
		    boolean blnFoundErrors = false;
		    if (go.equals(zXType.rc.rcError)) {
		        /**
		         * Add error message to the i.pageflow error message
		         */
		        if (StringUtil.len(getPageflow().getErrorMsg()) > 0) {
		            getPageflow().getPage().formatErrorStack(false);
		        } else {
		            getPageflow().setErrorMsg(getZx().trace.formatStack(false));
		        }
		        
		        blnFoundErrors = true;
		    }
		    
		    if (!blnFoundErrors) {
		        boolean blnOwnTx = false;
		        try {
		            /**
		             * Start transaction and go! Note that the tx may have been started
		             * if this action is called as part of a loopOver....
                     * 
                     * Changed in V1.5 to use data source of entity at hand rather than DB tx calls
		             */
                    if (objActionEntity.getBo().getDS().inTx()) {
                        blnOwnTx = false;
                    } else {
                        blnOwnTx = true;
                        objActionEntity.getBo().getDS().beginTx();
                    }
                    
		            if(getDbActionType().equals(zXType.pageflowDBActionType.pdaDelete)) {
		                if (StringUtil.len(objActionEntity.getPkwheregroup()) > 0) {
		                    go = objActionEntity.getBo().deleteBO(objActionEntity.getPkwheregroup());
		                } else {
		                    go = objActionEntity.getBo().deleteBO();
		                }
                        
		            } else if (getDbActionType().equals(zXType.pageflowDBActionType.pdaInsert)) {
		                go = objActionEntity.getBo().insertBO();
                        
		            } else  if (getDbActionType().equals(zXType.pageflowDBActionType.pdaUpdate)) {
		                if (StringUtil.len(objActionEntity.getPkwheregroup()) > 0) {
		                    go = objActionEntity.getBo().updateBO(objActionEntity.getPkwheregroup());
		                } else {
                            String strGroup = StringUtil.len(objActionEntity.getSelecteditgroup()) > 0 ? objActionEntity.getSelecteditgroup() : "*";
		                    go = objActionEntity.getBo().updateBO(strGroup);
		                }
                        
		            }
		            
                    /**
                     * DGS14JUL2004: The above can return warning, which is really ok, but if we pass
                     * warning back up to the calling functions it can be treated as failure.
                     */
		            if (go.pos == zXType.rc.rcWarning.pos) {
		                // Could be a warning error ? 
		                //throw new Exception("Failed to perform db action");
		                go = zXType.rc.rcOK;
		            }
		            
                    if (go.pos != zXType.rc.rcOK.pos) {
                    	/**
                    	 * Not allowed to perform db action.
                    	 */
                    	if(blnOwnTx) {
                    		objActionEntity.getBo().getDS().rollbackTx();
                    	}
                    	
    		            /**
    		             * Set the error message
    		             */
	                    if (StringUtil.len(getZx().trace.formatStack(true)) == 0) {
	                        getPageflow().setErrorMsg(getPageflow().resolveLabel(getDbactionerrormsg()));
	                    } else {
	                        getPageflow().setErrorMsg(getZx().trace.formatStack(false));
	                    }
	                    
                    } else {
    		            if (blnOwnTx) {
    		            	/**
    		            	 * V1.4:62 - DGS05APR2005: Commit can fail, so test for it
    		            	 */
                            objActionEntity.getBo().getDS().commitTx();
    		            }
                        
                        getPageflow().setInfoMsg(getPageflow().resolveLabel(getDbactioninfomsg()));
                    }
                    
		        } catch (Exception e) {
		        	/**
		        	 * Trace error message
		        	 */
		        	getZx().trace.addError("Failed to perform dbaction", e);
		        	
		            /**
		             * Handle failure : But this is actually a major error.
		             */
		            if (blnOwnTx) {
                        objActionEntity.getBo().getDS().rollbackTx();
		            }
                	
		            /**
		             * Set the error message
		             */
                    if (StringUtil.len(getZx().trace.formatStack(true)) == 0) {
                        getPageflow().setErrorMsg(getPageflow().resolveLabel(getErrormsg()));
                    } else {
                        getPageflow().setErrorMsg(getZx().trace.formatStack(false));
                    }
                    
                }
		    }
		    
		    /**
             * DGS24APR2003: If not success go to the error action if there is one defined,
             * and force a successful return, so that the error action can be invoked:
             */
            if (go.pos != zXType.rc.rcOK.pos 
                && getErroraction() != null 
                && StringUtil.len(getErroraction().getAction()) > 0) {
            	
                getPageflow().setAction(getPageflow().resolveLink(getErroraction()));
                go = zXType.rc.rcOK;
                
            }else {
                getPageflow().setAction(getPageflow().resolveLink(getLink()));
                
            }
            
		    /**
             * DGS15APR2003: Pass back the return code from the DB update. If in a loopover
             * it needs to know if this was successful or not.
		     */
		    return go;
		} catch (Exception e) {
            getZx().trace.addError("Failed to : execute pageflow action " + getName(), e);
            
		    if (getZx().throwException) throw new ZXException(e);
		    return go;
		} finally {
		    if(getZx().trace.isApplicationTraceEnabled()) {
		        getZx().trace.returnValue(go);
		        getZx().trace.exitMethod();
		    }
		}
    }
    
    //------------------------ PAction overidden methods.
    
    /** 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
        
        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();
        
        objXMLGen.taggedValue("dbactionentity", getDbactionentity());
        objXMLGen.taggedValue("reset", isReset());
        objXMLGen.taggedValue("setautomatics", isSetautomatics());
        
        getDescriptor().xmlLabel("dbactioninfomsg", getDbactioninfomsg());
        getDescriptor().xmlLabel("dbactionerrormsg", getDbactionerrormsg());
        
        objXMLGen.taggedValue("dbactiontype", zXType.valueOf(getDbActionType()));
        
        /**
         * DGS 24APR2003: Added error action tag (a component with sub tags)
         */
        getDescriptor().xmlComponent("erroraction", getErroraction());
    }
    
    /** 
     * @see PFAction#clone(Pageflow)
     **/
    public PFAction clone(Pageflow pobjPageflow) {
        PFDBAction cleanClone = (PFDBAction)super.clone(pobjPageflow);
        
        cleanClone.setDbactionentity(getDbactionentity());
        cleanClone.setReset(isReset());
        cleanClone.setSetautomatics(isSetautomatics());
        
        if (getDbactioninfomsg() != null) {
            cleanClone.setDbactioninfomsg((LabelCollection)getDbactioninfomsg().clone());
        }
        
        if(getDbactionerrormsg() != null) {
        	cleanClone.setDbactionerrormsg((LabelCollection)getDbactionerrormsg().clone());
        }
        
        cleanClone.setDbActionType(getDbActionType());
        
        if (getErroraction() != null) {
        	cleanClone.setErroraction((PFComponent)getErroraction().clone());   
        }
        
        return cleanClone;
    }
}