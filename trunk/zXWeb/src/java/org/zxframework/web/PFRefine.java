/*
 * Created on Apr 14, 2004 by Michael Brewer
 * $Id: PFRefine.java,v 1.1.2.9 2006/07/17 13:58:23 mike Exp $ 
 */
package org.zxframework.web;

import org.zxframework.LabelCollection;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Pageflow refine action object.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 12 September 2002
 * 
 * Change    : BD3SEP04
 * Why       : Add support to force user to tick at least one box
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFRefine extends PFAction {
    
    //------------------------ Members
    
    private boolean mustrefine;
    private LabelCollection refineerrormsg;
    private PFComponent refineerroraction;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFRefine() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    /**
     * Indicates whether the user must tick at least one box.
     * 
     * @return Returns the blnmustrefine.
     */
    public boolean isMustrefine() {
        return mustrefine;
    }
    
    /**
     * @param mustrefine The blnmustRefine to set.
     */
    public void setMustrefine(boolean mustrefine) {
        this.mustrefine = mustrefine;
    }
    
    /**
     * The action where to go to when the user forgets to tick at least one box.
     * 
     * @return Returns the refineerroraction.
     */
    public PFComponent getRefineerroraction() {
        return refineerroraction;
    }
    
    /**
     * @param refineerroraction The refineerroraction to set.
     */
    public void setRefineerroraction(PFComponent refineerroraction) {
        this.refineerroraction = refineerroraction;
    }
    
    /**
     * The error message that is displayed when the user forgets to tick at least one box.
     * 
     * @return Returns the refineerrormsg.
     */
    public LabelCollection getRefineerrormsg() {
        return refineerrormsg;
    }
    
    /**
     * @param refineerrormsg The refineerrormsg to set.
     */
    public void setRefineerrormsg(LabelCollection refineerrormsg) {
        this.refineerrormsg = refineerrormsg;
    }
    
    //------------------------ Digester helper methods
    
    /**
     * @deprecated Using BooleanConverter
     * @param mustrefine The blnmustRefine to set.
     */
    public void setMustrefine(String mustrefine) {
        this.mustrefine = StringUtil.booleanValue(mustrefine);
    }
    
    //------------------------ Implemented Methods from PFAction
    
    /**
     * @see PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
    	if (getZx().trace.isApplicationTraceEnabled()) {
    		getZx().trace.enterMethod();
    	}
    	
        zXType.rc go = zXType.rc.rcOK;
        
        try {
            /**
             * Get relevant entities and store in context
             */
            ZXCollection colEntities = getPageflow().getEntityCollection(this, 
            															 zXType.pageflowActionType.patListForm, 
            															 zXType.pageflowQueryType.pqtAll);
            if (colEntities == null) {
                throw new Exception("Unable to get entities for action");
            }
            
            getPageflow().setContextEntities(colEntities);
            
            /**
             * Select first entity
             */
            PFEntity objEntity = (PFEntity)colEntities.iterator().next();
            getPageflow().setContextEntity(objEntity);
            
            /**
             * The number of checkboxes for a multilist can be controlled using
             * the tag zXNumSelectCB. This is only relevant in the Refine for radio
             * buttons, where a negative number is used to indicate it is a radio
             * button, and the absolute value of that indicates which radio
             * button we are interested in (i.e. which has to be selected to make
             * it into the query). The preferred new tag zXNumSelectRB is also
             * used for radio buttons, entered as a positive number.
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
            
            String strTmp = getPageflow().getPage().processMultilist(getPageflow().getRequest(), objEntity.getBo(),null, intNumSelect);
            if (StringUtil.len(strTmp) > 0) {
                getPageflow().addToContext(getPageflow().resolveDirector(getQueryname()) + Pageflow.QRYWHERECLAUSE,  "(" + strTmp + ")");
                getPageflow().setAction(getPageflow().resolveLink(getLink()));
                
            } else {
                /**
                 * If we have not generated an addition to the where clause, it means
                 * that no item was selected. This is NOT ok if the mustrefine box
                 * is ticked
                 */
                if (isMustrefine()) {
                    /**
                     * Set error message
                     */
                    getPageflow().setErrorMsg(getPageflow().resolveLabel(getRefineerrormsg()));
                    
                    /**
                     * Got to the refine error action if there is one defined,
                     * otherwise go to the link action.
                     */
                    if (getRefineerroraction() != null && StringUtil.len(getRefineerroraction().getAction()) > 0) {
                        getPageflow().setAction(getPageflow().resolveLink(getRefineerroraction()));
                    } else {
                        getPageflow().setAction(getPageflow().resolveLink(getLink()));
                    }
                    
                } else {
                    /**
                     * No boxes ticked but this is fine
                     */
                    getPageflow().setAction(getPageflow().resolveLink(getLink()));
                } // Must refine?
                
            } // Was any box checked?
            
            return go;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process refine action.", e);
            
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
    
    /** 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
        
        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();
        
        // Refine specific values to be dumped.
        objXMLGen.taggedValue("mustrefine", isMustrefine());
        
        if (getRefineerrormsg() != null) {
            getDescriptor().xmlLabel("refineerrormsg", getRefineerrormsg());
        }
        
        if (getRefineerroraction() != null) {
        	getDescriptor().xmlComponent("refineerroraction", getRefineerroraction());
        }
    }
    
    /**
     * @see PFAction#clone(Pageflow)
     */
    public PFAction clone(Pageflow pobjPageflow) {
        PFRefine cleanClone = (PFRefine)super.clone(pobjPageflow);
        
        cleanClone.setMustrefine(isMustrefine());
        
        if (getRefineerroraction() != null ) {
            cleanClone.setRefineerroraction((PFComponent)getRefineerroraction().clone());
        }
        
        if (getRefineerrormsg() != null) { 
            cleanClone.setRefineerrormsg((LabelCollection)getRefineerrormsg().clone());
        }
        
        return cleanClone;
    }    
}