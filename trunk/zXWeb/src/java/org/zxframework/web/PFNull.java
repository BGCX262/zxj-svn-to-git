/*
 * Created on Apr 14, 2004 by Michael Brewer
 * $Id: PFNull.java,v 1.1.2.8 2006/07/17 16:09:14 mike Exp $ 
 */
package org.zxframework.web;

import java.util.Iterator;

import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;

/**
 * Pageflow null action object.
 * 
 * <pre>
 * 
 * Change:   BD24FEB03
 * Why   :   Swapped title and handle messages so that if you use both
 *           it renders the same as for all other page types (ie messages after title)
 * 
 * Change    : BD28MAR03
 * Why       : Make entities that have been resolved the contextEntities
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFNull extends PFAction {

    //------------------------ Members
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFNull() {
        super();
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
             * Resolve entities as we may want to refer to them in the URL directors
             */
            ZXCollection colEntities = getPageflow().getEntityCollection(this, 
            															 zXType.pageflowActionType.patNull, 
            															 zXType.pageflowQueryType.pqtAll);
            if (colEntities != null) {
                getPageflow().setContextEntities(colEntities);
                
                PFEntity objEntity;
                String strPK;
                Iterator iter = colEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    getPageflow().setContextEntity(objEntity);
                    strPK = getPageflow().resolveDirector(objEntity.getPk());
                    if (StringUtil.len(strPK) > 0) {
                        objEntity.getBo().setPKValue(strPK);
                        
                        /**
                         * DGS23FEB2004: Use the select edit group unless it is empty, in which case use the select list group
                         */
                        objEntity.getBo().loadBO((StringUtil.len(objEntity.getSelecteditgroup())>0)?objEntity.getSelecteditgroup():objEntity.getSelectlistgroup());
                    } else {
                        if (StringUtil.len(objEntity.getPkwheregroup()) > 0) {
                            objEntity.getBo().loadBO(objEntity.getSelectlistgroup(), objEntity.getPkwheregroup(), false);
                        }
                    }
                }
            }
            
            /**
             * Null is often used for button only stuff so do messages and button
             */
            
            /**
             * Title
             */
            String strTag = tagValue("zxForm");
            if (StringUtil.len(strTag) > 0) {
                if (strTag.equalsIgnoreCase("menu")) {
                    getPageflow().getPage().formTitle(zXType.webFormType.wftMenu, getPageflow().resolveLabel(getTitle()));
                } else if (strTag.equalsIgnoreCase("edit")) {
                    getPageflow().getPage().formTitle(zXType.webFormType.wftEdit, getPageflow().resolveLabel(getTitle()));
                } else if (strTag.equalsIgnoreCase("list")) {
                    getPageflow().getPage().formTitle(zXType.webFormType.wftList, getPageflow().resolveLabel(getTitle()));
                } else if (strTag.equalsIgnoreCase("search")) {
                    getPageflow().getPage().formTitle(zXType.webFormType.wftSearch, getPageflow().resolveLabel(getTitle()));
                } else {
                    getPageflow().getPage().formTitle(zXType.webFormType.wftEdit, getPageflow().resolveLabel(getTitle()));
                }
                
            } else {
                getPageflow().getPage().formTitle(zXType.webFormType.wftEdit, getPageflow().resolveLabel(getTitle()));
            }
            
            /**
             * Handle any outstanding messages
             */
            getPageflow().processMessages(this);
            
            /**
             * Handle buttons. With the tag zXForm you can control the placement of the button
             */
            if (StringUtil.len(strTag) == 0) {
                getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftNull);
            } else {
                if (strTag.equalsIgnoreCase("menu")) {
                    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftMenu);
                } else if (strTag.equalsIgnoreCase("edit")) {
                    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftEdit);
                } else if (strTag.equalsIgnoreCase("list")) {
                    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftList);
                } else if (strTag.equalsIgnoreCase("search")) {
                    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftSearch);
                } else {
                    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftNull);
                }
            }

            getPageflow().processFormButtons(this);
            
            if (StringUtil.len(strTag) == 0) {
                getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftNull);
            } else {
                if (strTag.equalsIgnoreCase("menu")) {
                    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftMenu);
                } else if (strTag.equalsIgnoreCase("edit")) {
                    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftEdit);
                } else if (strTag.equalsIgnoreCase("list")) {
                    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftList);
                } else if (strTag.equalsIgnoreCase("search")) {
                    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftSearch);
                } else {
                    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftNull);
                }
            }
            
            /**
             * Handle window title and footer
             */
            getPageflow().handleFooterAndTitle(this, null);
            
            /**
             * Determine next action
             */
            getPageflow().setAction(getPageflow().resolveLink(getLink()));
            
            return go;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process action.", e);
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
     * In this case all of the work in done in the super class implementation.
     * 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
    }
}