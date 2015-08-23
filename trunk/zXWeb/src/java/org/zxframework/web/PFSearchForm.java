/*
 * Created on Apr 12, 2004 by Michael Brewer
 * $Id: PFSearchForm.java,v 1.1.2.11 2006/07/17 13:58:14 mike Exp $ 
 */
package org.zxframework.web;

import java.util.Iterator;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.CloneableObject;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Pageflow searchForm object.
 * 
 * <pre>
 * 
 * This is a generic pageflow action for search forms. It also supports saved searches.
 * 
 * Things to add :
 * - Going back to a search should remember the search form fields. The challenge will be the between search
 * forms.
 * 
 * Change    : BD12JUN03
 * Why       : Added support for multiple entities without sub titles
 * 
 * Change    : DGS27AUG2003
 * Why       : Changes to support saved queries.
 * 
 * Change    : BD19JAN04
 * Why       : - Do not attempt to generate orde-by stuff when there is no X1 (and thus
 *               no X2, X3, X4 or X5 either)
 *             - Support for the zXNoOrderBy tag (i.e. do not generate any order by stuff)
 * 
 * Change    : BD24FEB04
 * Why       : Added support for noFormStart and end
 * 
 * Change    : BD27FEB04
 * Why       : Now use the new handle footer and title that takes into consideration
 *             to not change the title when in a popup
 * 
 * Change    : BD26MAR04
 * Why       : Force cursor to first proper field (i.e. not the first pulldown list with operands)
 * 
 * Change    : BD30MAR05 V1.5:1
 * Why       : Support for data sources
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFSearchForm extends PFAction implements CloneableObject {
    
    //------------------------ Members
    
    private boolean savesearch;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFSearchForm() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    /** 
     * Whether add the save search facility.
     * 
     * @return Returns the savesearch. 
     */
    public boolean isSavesSarch() {
        return savesearch;
    }

    /**
     * @param savesearch The savesearch to set.
     */
    public void setSavesearch(boolean savesearch) {
        this.savesearch = savesearch;
    }
    
    //------------------------ Implemented Methods from PFAction 
    
    /**
     * Process action.
     * 
     * Reviewed for 1.5:1.
     * 
     * @see PFAction#go()
     * @return Returns the return code of the method.
     * @throws ZXException  Thrown if go fails. 
     */
    public zXType.rc go() throws ZXException{
        if(getZx().trace.isApplicationTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc go = zXType.rc.rcOK; 
        
        try {
            /**
             * Get entities
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
             * Page title
             */
            if (getTitle() != null) {
                getPageflow().getPage().formTitle(zXType.webFormType.wftSearch, getPageflow().resolveLabel(getTitle()));
            }
            
            /**
             * Handle any messages
             */
            getPageflow().processMessages(this);
            
            /**
             * Form header (if not handled by calling asp)
             */
            if (!getPageflow().isOwnForm() && !getPageflow().resolveDirector(tagValue("zXNoFormStart")).equals("1")) {
                getPageflow().getPage().s.append("<form ")
                						 .appendAttr("method", "post")
                						 .appendAttr("action", getPageflow().constructURL(getFormaction()))
                						 .appendNL('>');
            }
            
            PFEntity objEntity;
            String strEntity = "";
            String strFirstField = "";
            AttributeCollection colAttr;
            Attribute objAttr;
            
            int j = 1;

            Iterator iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                if (StringUtil.len(objEntity.getDSHandler().getSearchGroup()) > 0) {
                    /** 
                     * Set context
                     **/
                    getPageflow().setContextEntity(objEntity);
                    
                    if (j == 1) { 
                        strEntity = objEntity.getBo().getDescriptor().getName();
                    }
                    
                    /**
                     * Sub header - Display a subtitle for each entity on the form :
                     */
                    if (j > 1) {
                        if (colEntities.size() > 0) {
                            if (StringUtil.len(tagValue("zXNoSubTitles")) ==0) {
                                getPageflow().getPage().formSubTitle(zXType.webFormType.wftSearch, objEntity.getBo().getDescriptor().getLabel().getLabel());
                            }
                        }
                    }
                    
                    if (getPageflow().getPage().searchForm(objEntity.getBo(), objEntity.getWheregroup()).pos != zXType.rc.rcOK.pos) {
                        throw new ZXException("Unable to generate search form for entity", objEntity.getName());
                    }
                
                    /**
                     * Keep track fo the field name for the first field as this is 
                     * where we want to set the cursor to (otherwise cursor defaults
                     * to first operations list)
                     */
                    if (StringUtil.len(strFirstField) ==  0 ){
                        colAttr = objEntity.getBo().getDescriptor().getGroup(objEntity.getWheregroup());
                        if (colAttr == null) {
                            throw new Exception("Failed to get the attribute group collection for the where group : " + objEntity.getWheregroup());
                        }
                        
                        if (colAttr.size() > 0) {
                            objAttr = (Attribute)colAttr.iterator().next();
                            strFirstField = getPageflow().getPage().controlName(objEntity.getBo(), objAttr);
                        }
                    }
                    
                    j++;
                    
                } // DS handler does not support search
                
            } // Loop over entities
            
            /**
             * Handle buttons.
             */
            getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftSearch);
            getPageflow().processFormButtons(this);
            getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftSearch);
            
            /**
             * zXNoOrderBy - Tag used to determine whether you display a order by list.
             * 
             * The order-by stuff:
             *  No need when there is no X1 (and probably no X2, X3, X4, etc either)
             *  No need when the zXNoSort tag is 1
             */
            String arrGroups[] = {"X1","X2","X3","X4"};
            if (!tagValue("zXNoOrderBy").equals("1")) {
                
                iter = colEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    if (objEntity.getDSHandler().getOrderSupport().pos > zXType.dsOrderSupport.dsosNone.pos) {
                        if (objEntity.getBo().getDescriptor().getGroup(arrGroups[0]).size() > 0) {
                            getPageflow().getPage().orderByOpen(objEntity.getBo());
                            for (int i = 0; i < arrGroups.length; i++) {
                                getPageflow().getPage().orderBy(objEntity.getBo(), arrGroups[i]);
                            }
                            getPageflow().getPage().orderByClose();
                        }
                    }
                }
                
            } else {
                
                /**
                 * New Tag : zXHiddenOrderBy - Allows you to have an implicit order by on your search forms without 
                 * have to display a orderBy dropdown.
                 */
                if (tagValue("zXHiddenOrderBy").equals("1")) {
                    iter = colEntities.iterator();
                    while (iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        if (objEntity.getBo().getDescriptor().getGroup(arrGroups[0]).size() > 0) {
                            Iterator iterGroups;
                            for (int i = 0; i < arrGroups.length; i++) {
                                iterGroups = objEntity.getBo().getDescriptor().getGroup(arrGroups[i]).iterator();
                                if (iterGroups.hasNext()) {
                                    objAttr = (Attribute)iterGroups.next();
                                    getPageflow().getPage().s.append("<input type=\"hidden\" name=\"")
                                    						 .append(getPageflow().getPage().controlName(objEntity.getBo()))
                                    						 .append(arrGroups[i]).append("OrderBy\" value=\"")
                                    						 .append(objAttr.getName()).appendNL("\">");
                                }
                            }
                        }
                    }
                }
            }
            
            if (isSavesSarch()) {
                /** And the saved queries stuff */
                getPageflow().getPage().savedQueryOpen();
                getPageflow().getPage().savedQuery(getPageflow().getPFDesc().getName(), strEntity);
                getPageflow().getPage().savedQueryClose();
            }
            
            /** Close the form. */
            if (!getPageflow().isOwnForm() && !getPageflow().resolveDirector(tagValue("zXNoFormEnd")).equals("1")) {
                getPageflow().getPage().s.appendNL("</form>");
            }
            
            /** Close page. */
            getPageflow().handleFooterAndTitle(this, "Enter search criteria");
            
            /**
             * Position cursor on the first field of the form.
             */
            if (!getPageflow().resolveDirector(tagValue("zXNoAutoPosition")).equals("1")) {
	            getPageflow().getPage().s.appendNL("<script type=\"text/javascript\" language=\"JavaScript\">");
	            getPageflow().getPage().s.appendNL("  zXSetCursorToFirstField('" + strFirstField + "');");
	            getPageflow().getPage().s.appendNL("  zXDirty=0;");
	            getPageflow().getPage().s.appendNL("</script>");
            }
            
            /** And determine next action */
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
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
        
        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();
        
        // Search form specific values to be dumped.
        objXMLGen.taggedValue("savesearch", isSavesSarch());
    }
    
    /** 
     * @see PFAction#clone(Pageflow)
     **/
    public PFAction clone(Pageflow pobjPageflow) {
        PFSearchForm cleanClone = (PFSearchForm)super.clone(pobjPageflow);
        cleanClone.setSavesearch(isSavesSarch());
        return cleanClone;
    }
}