/*
 * Created on Apr 12, 2004 by Michael Brewer
 * $Id: PFEditForm.java,v 1.1.2.20 2006/07/17 16:08:21 mike Exp $ 
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.Iterator;

import org.zxframework.Tuple;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Pageflow edit form action object.
 * 
 * <pre>
 * 
 * Change    : DGS13FEB2003
 * Why       : Added editform type (normal, columns or tabs) and collection of edit pages
 *             that apply to column and tab type. Changed function Go a fair bit to
 *             support columns and tabs.
 * 
 * Change    : BD19FEB03
 * Why       : Implemented the multi-step warning mechanism
 * 
 * Change    : 4MAR03
 * Why       : In multi-tab pages, give the focus to the right tab in case
 *             an error has occured
 * 
 * Change    : BD21MAR03
 * Why       : Fixed bug with loading / resetting BO's for each tab on a multi-tab form
 * 
 * Change    : BD16APR03
 * Why       : Added zXNoFormStart and zXNoFormEnd tags
 * 
 * Change    : BD16APR03
 * Why       : Call getEntityCollection even when editForm has been called from
 *             createUpdate in case of an error. This makes the framework even more
 *             powerful because it allow us to combine submitable editforms with
 *             other pageflow actions
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
 * Change    : BD29APR03
 * Why       : Added proagateQS feature
 * 
 * Change    : BD23MAY03
 * Why       : Fixed problem with qs.-err; it did no longer position the cursor
 *             on the field in error
 * 
 * Change    : 12JUN03
 * Why       : Added support for multiple entities without having sub titles
 * 
 * Change    : DGS27JUN2003
 * Why       : Added sub actions
 * 
 * Change    : DGS12FEB2004
 * Why       : While creating the 'Matrix' edit form, realised was duplicating the code
 *             that handles enhancers, so removed it to a common function.
 * 
 * Change    : BD24FEB04
 * Why       : Added support for directors in zXNoFormEnd and Start tags
 * 
 * Change    : BD05MAR04
 * Why       : Change to deal with multi-tab edit forms where the edit group
 *             has additional columns to the groups actually displayed in the
 *             tabs. See comment marked with BD05MAR04
 *                
 * Change    : BD9JUN04
 * Why       : Only use ~ instead of ! for BOs with concurrency control
 * 
 * Change    : BD14JUN04
 * Why       : Added support for forceFocusAttr
 * 
 * Change    : DGS09JUL2004
 * Why       : An unrelated problem showed that an error when calling HTML.editForm was not caught.
 * 
 * Change    : MB01NOV2004
 * Why       : Fix casing for the footer title. ie : "Edit Data"
 * 
 * Change    : DGS06DEC2004
 * Why       : In iPFAction_go, just before using editpage details, evaluate the editpage attribute
 *             group as it is a director.
 *             
 * Change    : BD13JUL05 - V1.5:36
 * Why       : Add nodb option; setting this flag has same effect as passing
 *             -sa=editNoDB around but with having to worry about doing so;
 *             this is useful when an editForm is used for non-database
 *             BOs
 * 
 * Change    : BD23FEB06 - V1.5:93
 * Why       : There is a problem with placing the cursor on the first
 *             unlocked field: assume the first field is an option list and
 *             the next field is a text box; there is a 'disable' dependency
 *             from the option list to the text box such that when the form is
 *             first shown, the text box is disabled.
 *             For example: option list 'receiverType' with values 'Client', 'Lender'
 *             or 'Other'; and text field 'otherReceiver' only enabled when the
 *             'receiverType' is set to 'Other'.
 *             All the JS code for this is added to a postJS collection that is
 *             dumped at the end of the action. The code for setting the cursor
 *             however is added to HTML.s directly and thus ends up in the HTML
 *             source BEFORE the code where the text field is locked; thus the
 *             focus is set to a field that is locked afterwards....
 *             
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFEditForm extends PFAction {
    
    //------------------------ Members
    
    private String qssubaction;
    private ZXCollection editsubactions;
    private zXType.pageflowEditFormType editFormType;
    private boolean nodb;
    private ArrayList editpages;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFEditForm() {
        super();
        
        // Init a collection of edit pages. As there will always be at least one.
        setEditpages(new ArrayList());
    }
    
    //------------------------ Getters/Setters
    
    /**
     * @return Returns the qssubaction.
     */
    public String getQssubaction() {
        return qssubaction;
    }
    
    /**
     * @param qssubaction The qssubaction to set.
     */
    public void setQssubaction(String qssubaction) {
        this.qssubaction = qssubaction;
    }
    
    /**
     * The enum of the type of edit form.
     * 
	 * @return Returns the editFormType.
	 */
	public zXType.pageflowEditFormType getEditFormType() {
		return editFormType;
	}

	/**
	 * @param editFormType The editFormType to set.
	 */
	public void setEditFormType(zXType.pageflowEditFormType editFormType) {
		this.editFormType = editFormType;
	}
    
	/**
     * An ArrayList (PFEditPage) of editpages for a edit form.
     * 
     * Do not clone editPages for a normal edit form, as this is created on the fly. 
     * 
     * @return Returns the editpages.
     */
    public ArrayList getEditpages() {
        return this.editpages;
    }
    
    /**
     * @param editpages The editpages to set.
     */
    public void setEditpages(ArrayList editpages) {
        this.editpages = editpages;
    }
    
    /**
     * A ZXCollection (Tuple) of subactions.
     * 
     * @return Returns a ZXCollection(Tuple) of editsubactions.
     */
    public ZXCollection getEditsubactions() {
        if (this.editsubactions == null) {
            this.editsubactions = new ZXCollection();
        }
        return editsubactions;
    }
    
    /**
     * @param editsubactions The editsubactions to set.
     */
    public void setEditsubactions(ZXCollection editsubactions) {
        this.editsubactions = editsubactions;
    }
    
    /**
     * A no db action.
     * 
	 * @return Returns the nodb.
	 */
	public boolean isNodb() {
		return nodb;
	}
	
	/**
	 * @param nodb The nodb to set.
	 */
	public void setNodb(boolean nodb) {
		this.nodb = nodb;
	}
	
	//------------------------ Digester helper methods.
	
	/**
	 * @deprecated Using BooleanConverter
	 * @param nodb The nodb to set.
	 */
	public void setNodb(String nodb) {
		this.nodb = StringUtil.booleanValue(nodb);
	}
	
    /**
     * Special set method used by Digester.
     * 
     * @param editformtype The editformtype to set.
     */
    public void setEditformtype(String editformtype) {
        this.editFormType = zXType.pageflowEditFormType.getEnum(editformtype);
    }
	
    //------------------------ Implemented Methods from PFAction
    
	/**
     * @see PFAction#go()
     * 
     * DGS18FEB2003: Changed a fair bit to support use of columns and tabs.
     * BD13JUL05 - V1.5:36 - Support for nodb
     * BD23FEB06 - V1.5:93 - Fixed problem with setting cursor to first field
     **/
    public zXType.rc go() throws ZXException {
		if(getZx().trace.isApplicationTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
        zXType.rc go = zXType.rc.rcOK;
		try {
		    String strTmp;
		    Iterator iter;
		    Tuple objEntityGroup;
		    PFEntity objEntity;
		    PFEntity objEntityLocal;
		    
		    /**
		     * Determine how progress
		     * 
		     * Check if we are already in progress by looking at the prgs entry in
		     * the querystring; but only when this matches my name!
		     */
		    String strQSProgress = StringUtil.len(getQsprogress()) == 0 ? "-prgs" : getQsprogress();
		    boolean blnInProgress = getPageflow().getQs().getEntryAsString(strQSProgress).equalsIgnoreCase(getName());
		    
		    /**
		     * Save name of attribute last in error
		     */
		    String strAttr = getZx().trace.getUserErrorAttr();
		    
		    /**
		     *  Subaction are stored in the querystring
		     * 
		     * DGS27JUN2003: Added sub actions in new style. Now can get a sub action name, and
		     * this should correspond with one of the names in the collection of sub actions.
		     */
		    String strQSSubAction = StringUtil.len(getQssubaction()) == 0 ? "-sa" : getQssubaction();
		    String strSubActionName = getPageflow().getQs().getEntryAsString(strQSSubAction).toLowerCase();
		    
		    /**
		     * If noDb flag has been set than we force editNoDb unless something was
		     * passed as -sa explicitly...
		     */
		    if (StringUtil.len(strSubActionName) == 0) {
		    	if (isNodb()) {
		    		strSubActionName = "editnodb";
		    	} // nodb flag set?
		    	
		    } // No -sa passed
		    
		    /**
		     * Get the subaction - this may or may not find a match in the collection;
		     * One could argue that this should not be done when the nodb flag is set and
		     * one if probably right....
		     */
		    String strSubActionValue ="";
		    if (StringUtil.len(strSubActionName) > 0) {
			    Tuple objSubAction;
			    iter = getEditsubactions().iterator();
			    while (iter.hasNext()) {
			        objSubAction = (Tuple)iter.next();
			        if (objSubAction.getName().equalsIgnoreCase(strSubActionName)) {
			            strSubActionValue = objSubAction.getValue();
			            break;
			        }
			    }
		    }
		    
		    zXType.pageflowSubActionType enmSubaction = null;
		    if (StringUtil.len(strSubActionValue) == 0) {
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
		             * The default behaviour has always been a standard edit:
		             */
		            enmSubaction = zXType.pageflowSubActionType.psatEdit;
		            strSubActionValue = "s.u";
		        }
		    }
		    
		    /**
		     * Get entity collection and store in context. However: if the subaction is
		     * error, it means that this routine is called directly after a
		     * clsPFCreateUpdate.go that detected input errors. In that case, we 
		     * will use the entity collection from the context (ie the one that has
		     * been created by the clsPFCreateUpdate.go routine) because otherwise
		     * we will create new business objects and thus overwrite any input that
		     * the user has already done
		     */
		    
		    /**
		     * BD16APR03 - We have made a change to getEntityCollection to simply not
		     * create a new BO when the BO is not nothing
		     */
		    ZXCollection colEntities = getPageflow().getEntityCollection(this, 
                                                                         zXType.pageflowActionType.patEditForm, 
		            													 zXType.pageflowQueryType.pqtSearchForm, 
		            													 strSubActionValue);
		    if (colEntities == null) {
		        throw new Exception("Unable to retrieve entity collection");
		    }
		    
		    /**
		     * Use another collection of entities to hold the selectEdit attr groups. We need to
		     * manipulate this, and mustn't do it to the entity itself in case it gets reused.
		     */
		    ZXCollection colEntitiesLocal = new ZXCollection();
		    
		    iter = colEntities.iterator();
		    while (iter.hasNext()) {
		        objEntity = (PFEntity)iter.next();
		        
		        objEntityLocal = new PFEntity();
		        objEntityLocal.setName(objEntity.getName());
		        objEntityLocal.setSelecteditgroup(objEntity.getSelecteditgroup());
		        
		        colEntitiesLocal.put(objEntityLocal.getName(), objEntityLocal);
		    }
		    
		    /**
		     * Handle edit enhancers - now done in called function
		     */
            if (!getPageflow().handleEnhancers(getEditenhancers(), colEntities).equals(zXType.rc.rcOK)) {
                throw new Exception("Failed to handle the enhancers for the edit form.");
            }
		    
		    /**
		     * Set context variable
		     */
		    getPageflow().setContextEntities(colEntities);
		    getPageflow().setContextEntity((PFEntity)colEntities.iterator().next());
		    
		    // Determine the control asap :
		    String strControlName = getPageflow().getPage().controlName(getPageflow().getContextEntity().getBo(), 
                                                                        getPageflow().getContextEntity().getBo().getDescriptor().getAttribute(strAttr));
		    
		    /**
		     * If it is a normal edit form, generate a 'page' on the fly so that we can loop through
		     * pages later on using the same code for all types of edit form.
		     */
		    PFEditPage objEditPage;
		    if (zXType.pageflowEditFormType.pfeftNormal.equals(getEditFormType())) {
		    	/**
		    	 * Reset the editpages, even though we may have parsed some column edit pages.
		    	 * This is noticed when caching is turned off.
		    	 */
		    	// setEditpages(new ArrayList());
		    	
		        iter = colEntities.iterator();
		        while (iter.hasNext()) {
		            objEntity = (PFEntity)iter.next();
			        objEditPage = new PFEditPage();
                    
			        /** Create on a fly Entity Group. **/
		            objEntityGroup = new Tuple();
		            objEntityGroup.setName(getPageflow().resolveDirector(objEntity.getName()));
                    /**
		             * Changed 10JUN2003 for edit pages. 
                     * Previously always used visibleGroup, but now we use this .value later, 
                     * so set it to SelectEditGroup if no visible.
		             */
		            if (StringUtil.len(objEntity.getVisiblegroup()) > 0) {
		                objEntityGroup.setValue(objEntity.getVisiblegroup());
		            } else {
		                objEntityGroup.setValue(objEntity.getSelecteditgroup());
		            }
                    
                    // Note : this is generated on the fly.
		            objEditPage.getEntitygroups().add(objEntityGroup);
		            
			        getEditpages().add(objEditPage);		            
		        }
		    }
		    
            //-----------------------------------------------------------------------------------------
		    //--------------------------------------------------------------- Load / reset the entities
            //-----------------------------------------------------------------------------------------
		    /**
		     * Do this sooner so that we can use some data earlier :
             * 
		     * eg : you can now put #pk in the title. 
		     */
		    iter = colEntities.iterator();
		    while (iter.hasNext()) {
		        objEntity = (PFEntity)iter.next();
		        
		        /**
		         * When already in progress do not load anything from the database or
		         *  reset values and generate automatics
		         */
		        if (!blnInProgress) {
		            /**
		             * Update the global context entity
		             */
		            getPageflow().setContextEntity(objEntity);
		            
		            /**
		             * Get the primary key
		             */
		            String strPK = getPageflow().resolveDirector(objEntity.getPk());
		            
		            /**
		             * When in edit mode, we must have a primary key, otherwise
		             * there is not much to edit....
		             */
		            if (getPageflow().editSubActionIncludes(strSubActionValue, "s")) {
		                if (StringUtil.len(strPK) == 0) {
		                    throw new ZXException("Unable to determine primary key", objEntity.getName());
		                }
                        
		                /**
		                 * This is an update so load from the database...
		                 */
		                objEntity.getBo().setPKValue(strPK);
                        
		                if (!objEntity.isLoaded()) {
                            /** Load entity by primary key. **/
		                    objEntity.getBo().loadBO(objEntity.getSelecteditgroup(), "+", false);
		                }
		            }
		            
		            if (getPageflow().editSubActionIncludes(strSubActionValue, "r")) {
		                /**
		                 * When a new entry: reset all variables and generated
		                 * new automatic numbers
		                 */
		                objEntity.getBo().resetBO();
		            }
		            
		            if (getPageflow().editSubActionIncludes(strSubActionValue, "a")) {
		                /**
		                 * When a new entry: reset all variables and generated
		                 * new automatic numbers
		                 */
		                objEntity.getBo().setAutomatics("+");
		            }
		            
		            if (enmSubaction != null && enmSubaction.equals(zXType.pageflowSubActionType.psatEditNoDB)) {
		                /**
		                 * This is a memory only edit, do set the PK as this may be -
		                 * required for an optional createUpdate action
		                 */
		                if (StringUtil.len(strPK) > 0) {
		                    objEntity.getBo().setPKValue(strPK);
		                }
                        
		            }
		            
		            if (!getPageflow().getQs().getEntryAsString("-err").equalsIgnoreCase(getName())) {
		                /**
		                 * The edit form is shown 2nd time around as a result of
		                 *  input errors. Only do the process attribute values and
		                 *  copy all values from the BO to the saver BO (that is
		                 *  actually being used to generate the form)
		                 */
		                getPageflow().processAttributeValues(objEntity);
		                
		                /**
		                 * BD4DEC02: Add audit columns!! - Note this will copy the persist status.
                         * BD9JUN04: Now only the zXUpdtdId
		                 */
		                objEntity.getBo().bo2bo(objEntity.getBosaver(), objEntity.getSelecteditgroup() + ",~");
		            }
		        } // In Progress
		    }
            //-----------------------------------------------------------------------------------------
            //--------------------------------------------------------------- Load / reset the entities
            //-----------------------------------------------------------------------------------------
		    
		    /**
		     * Page title
		     */
		    if (getTitle() != null && getTitle().size() > 0) {
		        getPageflow().getPage().formTitle(zXType.webFormType.wftEdit, getPageflow().resolveLabel(getTitle()));
		    }
            
		    /**
		     * Handle any outstanding messages
		     */
		    getPageflow().processMessages(this);
		    
		    /**
		     * Make sure that the form header is not be generated
		     * if the calling application handles it.
             * 
             * This maybe true when you have a custom jsp file or
             * multiple edit forms on a single page.
		     */
		    String strUrl;
		    if (!getPageflow().isOwnForm() 
		        && !getPageflow().resolveDirector(tagValue("zXNoFormStart")).equalsIgnoreCase("1")) {
		        /**
		         * Construct formaction URL
		         */
		        strUrl = getPageflow().constructURL(getFormaction());
		        
		        /**
		         * Second part is the sub action that we need to add if not yet
		         *  present
		         */
		        if (getPageflow().getPFDesc().isPropagaTeqs()) {
		            getPageflow().getQs().setEntry(strQSSubAction, getPageflow().getQs().getEntryAsString(strQSSubAction));
		            
		            strUrl = getPageflow().constructURL(getFormaction());
                    
		        } else {
		            /**
		             * Construct formaction URL
		             */
		            strUrl = getPageflow().constructURL(getFormaction());
		            
		            if (strUrl.indexOf(strQSSubAction + "=") == -1) {
		                strUrl = getPageflow().appendToUrl(strUrl, 
		                                                   strQSSubAction + '=' + getPageflow().getQs().getEntryAsString(strQSSubAction));
		            }
                    
		        }
		        
		        /**
		         * Only one page
		         */
		        getPageflow().getPage().s.append("<form ")
				                .appendAttr("method", "post")
				                .appendAttr("action", strUrl)
				                .appendNL('>');
		    } // Header
		    
		    /**
		     * Multi-step warning mechanism
		     * If the editform is called as a result of a warning being found (and not ignored by the user) in the create-update, we are going to insert a checkbox in the edit form
		     * that will allow the user to ignore any messages
		     */
		    if (StringUtil.len(getPageflow().getQs().getEntryAsString("-wrn")) > 0) {
		        getPageflow().getPage().editFormIgnoreWarning();
		        
		        /**
		         * For true safety, delete -wrn from the query string
		         *  it is for internal use only and ensures that we do not by accident
		         *  when we have multiple edit forms on a single screen
		         */
		        getPageflow().getQs().setEntry("-wrn", null);
		    }
		    
		    int intPage = 0;
		    int intEditpages = getEditpages().size();
		    
		    if (zXType.pageflowEditFormType.pfeftTabs.equals(getEditFormType())) {
		        /**
		         * Generate the tabs across the top, one per page. The id must be 'zXPageN' where N
		         * is the page number. This will be used by javascript in zx.js when a tab is clicked.
		         */
		        intPage = 0;
		        getPageflow().getPage().s.appendNL("<table width='90%'><tr>");
		        for (int i = 0; i < intEditpages; i++) {
		            objEditPage = (PFEditPage)getEditpages().get(i);
		            intPage = intPage + 1;
		            
		            getPageflow().getPage().s.append("<td id='zXPage").append(intPage)
		            						 .append("' onMouseDown='javascript:zXSelectTab(").append(intPage).appendNL(");' >");
		            getPageflow().getPage().s.appendNL(getPageflow().resolveLabel(objEditPage.getLabel()));
		            getPageflow().getPage().s.appendNL("</td>");
		        }
		        getPageflow().getPage().s.appendNL("</tr>");
		        
		        /**
		         * Add some space
		         */
		        getPageflow().getPage().s.append("<tr><td colspan='")
		        						 .append(intEditpages)
		        						 .appendNL("'><image src='../images/spacer.gif' height=9></td></tr>");
		        getPageflow().getPage().s.appendNL("</table>");
		    }
		    
		    if (zXType.pageflowEditFormType.pfeftColumns.equals(getEditFormType())) {
		        getPageflow().getPage().s.appendNL("<table width='90%'><tr valign='top'>");
		    }
            
            /**
             * Load / reset the entities.
             * NOTE: This has move up just above the form title generation.
             */
            
		    //--------------------------------------------------------------------------------------
		    //------------------------------------------------------------------- Process Edit Pages
            //--------------------------------------------------------------------------------------
            
		    intPage = 0;
		    int j = 0;
		    String strPK;
		    
	        for (int i = 0; i < intEditpages; i++) {
	            objEditPage = (PFEditPage)getEditpages().get(i);
		        intPage = intPage + 1;
		        
		        if (zXType.pageflowEditFormType.pfeftTabs.equals(getEditFormType())) {
		            /**
		             * Tab format: Start a 'div' for each tab. Make it invisible for now then
		             * click the first using javascript (see below).
		             */
		            getPageflow().getPage().s.append("<div id='zXPage").append(intPage).appendNL("' style='display:none'>");
		            
		        } else if (zXType.pageflowEditFormType.pfeftColumns.equals(getEditFormType())) {
		            /**
		             * Columns format: Show a subtitle at the top of each column.
		             */
		            getPageflow().getPage().s.append("<td width=").append(100/intEditpages).appendNL("% >");
		            
		            /** 
		             * Rather print nothing than label not found :
		             */
		            String strLabel;
		            if (objEditPage.getLabel() == null || objEditPage.getLabel().isEmpty()) {
		                strLabel = "";
		            } else {
		                strLabel = getPageflow().resolveLabel(objEditPage.getLabel());
		            }
		            getPageflow().getPage().formSubTitle(zXType.webFormType.wftEdit, strLabel);
                    
		        }
		        
		        int intEntitygroups = objEditPage.getEntitygroups().size();
		        for (int k = 0; k < intEntitygroups; k++) {
		            objEntityGroup = (Tuple)objEditPage.getEntitygroups().get(k);
		            
                    /**
                     * DGS06DEC2004: Before using this page, make sure any expression is evaluated.
                     * Safest to do it here, after loading entities, but before using this group.
                     */
                    objEntityGroup.setValue(getPageflow().resolveDirector(objEntityGroup.getValue()));
                    
		            objEntity = (PFEntity)colEntities.get(objEntityGroup.getName());
		            objEntityLocal = (PFEntity)colEntitiesLocal.get(objEntityGroup.getName());
		            
		            /**
		             * See if this entity is to be displayed
		             */
		            if (StringUtil.len(objEntity.getSelecteditgroup()) > 0) {
		                j = j + 1;
		                
		                /**
		                 * Update the global context entity
		                 */
		                getPageflow().setContextEntity(objEntity);
		                
		                /**
		                 * Get the primary key
		                 */
		                strPK = getPageflow().resolveDirector(objEntity.getPk());
		                
		                /**
		                 * When in edit mode, we must have a primary key, otherwise
		                 * there is not much to edit....
		                 */
		                if (getPageflow().editSubActionIncludes(strSubActionValue, "s") && StringUtil.len(strPK) == 0) {
		                    throw new Exception("Unable to determine primary key" + objEntity.getName());
		                }
		                
		                /**
		                 * Either I have a PK or the subaction does not require one....
		                 */
		                if (!getPageflow().editSubActionIncludes(strSubActionValue, "s") || StringUtil.len(strPK) > 0) {
		                    /**
		                     * If we have multiple entities to display and the current
		                     * entity has actually some visible fields create a sub title
		                     * for all but the first (only applies to normal format edit forms
		                     *  i.e. not tab or column format).
		                     *  Note that having a tag zXNoSubTitles will cause no sub titles
		                     *  to be displayed
		                     */
		                    String strLockGroup = objEntity.getLockgroup();
		                    String strVisibleGroup = getPageflow().resolveDirector(objEntityGroup.getValue());
		                    
		                    if (zXType.pageflowEditFormType.pfeftNormal.equals(getEditFormType()) 
		                        && j > 1 
		                        && objEntity.getBODesc().getGroup(strVisibleGroup).size() > 0 
		                        && StringUtil.len(tagValue("zXNoSubTitles")) == 0) {
		                        
		                        getPageflow().getPage().formSubTitle(zXType.webFormType.wftEdit, objEntity.getBODesc().getLabel().getLabel());
		                        
		                    }
		                    
                            /**
                             * Merge the visible group and the edit group.
                             * Where the result is in the order of the edit group, but
                             * contains only the elements in the visible group.
                             */
		                    String strSelectEditGroup = "";
		                    if (StringUtil.len(objEntity.getVisiblegroup()) == 0) {
		                        /**
		                         * Blank visible group means all visible, so none are hidden:
		                         * Changed 10JUN2003 for edit pages.
		                         */
		                        strSelectEditGroup = objEntityGroup.getValue();
                                
		                    } else {
		                        /**
		                         * Otherwise must find out the hidden attrs, which is entity
		                         * selectedit minus entity visible...
		                         */
		                        strSelectEditGroup = objEntity.getBo().getDescriptor().getGroupMinus(objEntityLocal.getSelecteditgroup(), 
                                                                                                     objEntity.getVisiblegroup()).formattedString();
		                        
		                        /**
		                         * ...then add the result to the page's visible group for this page's selectEdit group.
		                         * 
		                         * NOTE : This is different fromt the COM+ version at the moment. But it does improve things a little.
		                         */
                                if (StringUtil.len(strSelectEditGroup) > 0) {
                                    strSelectEditGroup = objEntity.getBo().getDescriptor().getGroupPlus(strSelectEditGroup, strVisibleGroup).formattedString();
                                } else {
                                    // The 2 attribute groups contain the same attribute, but we should take that sequence of the
                                    // selectEditGroup.
                                    strSelectEditGroup = strVisibleGroup;
                                }
		                    }
                            
		                    /**
		                     * Very serious change that needs lost of testing:
		                     * Use the visible group of the overall entity (i.e. not of the current page)
		                     * as this is very important when we want to hide certain fields
		                     */
		                    getPageflow().getPage().editForm(objEntity.getBosaver(), 
		                            						 strSelectEditGroup, 
		                            						 strLockGroup, 
		                            						 objEntity.getVisiblegroup()); // strVisibleGroup
		                    
		                    /**
		                     * We have to keep track of the attributes for which we have
		                     * generated fields so far; see big comment later, marked with
		                     * BD05MAR04
		                     */
		                    if (StringUtil.len(objEntity.getGeneratedFields()) == 0) {
		                        objEntity.setGeneratedFields(strSelectEditGroup);
		                    } else {
		                        objEntity.setGeneratedFields(objEntity.getGeneratedFields() + "," + strSelectEditGroup);
		                    }
		                    
		                    
		                    /**
		                     * Now clear out the select edit group for this entity, so that if we use the same
		                     * entity again for another page, we don't duplicate the hidden columns.
		                     */
		                    objEntityLocal.setSelecteditgroup("");
		                }    
		            }
		        }
		        
		        if (zXType.pageflowEditFormType.pfeftTabs.equals(getEditFormType())){
		            getPageflow().getPage().s.appendNL("</div>");
		            
		        } else if (zXType.pageflowEditFormType.pfeftColumns.equals(getEditFormType())){
		            getPageflow().getPage().s.appendNL("</td>");
		        }
                
		    }
            //--------------------------------------------------------------------------------------
            //------------------------------------------------------------------- Process Edit Pages
            //--------------------------------------------------------------------------------------
            
		    /**
		     * BD05MAR04; bit triky: assume we have a multi-tab edit form with 2 tabs
		     * and the following groups:
		     * 
		     * tab 1: a,b
		     * tab 2: c,d
		     * 
		     * and an edit group of a,b,c,d,e
		     * 
		     * Now the most intuitive behaviour is that e ends up as a hidden field.
		     * So what we have done is keep track of all the fields that have
		     * appeared on the screen as a result of the tabs. Now simply
		     * generate an edit form with all hidden fields for the attributes
		     * that are 'surplus' in the edit group
		     */
		    iter = colEntities.iterator();
		    while (iter.hasNext()) {
		        objEntity = (PFEntity)iter.next();
		        if (StringUtil.len(objEntity.getSelecteditgroup()) > 0) {
		            if (StringUtil.len(objEntity.getGeneratedFields()) > 0) {
		                strTmp = objEntity.getBo().getDescriptor().getGroupMinus(objEntity.getSelecteditgroup(), objEntity.getGeneratedFields()).formattedString();
		            } else {
		                strTmp = objEntity.getSelecteditgroup();
		            }
		            if (StringUtil.len(strTmp) > 0) {
		                getPageflow().getPage().editForm(objEntity.getBosaver(), strTmp, "", "null");
		            }
		        }
		    }
		    
		    if (zXType.pageflowEditFormType.pfeftTabs.equals(getEditFormType())) {
		        getPageflow().getPage().s.appendNL("<script type=\"text/javascript\" language=\"JavaScript\">");
		        getPageflow().getPage().s.appendNL("zXSelectTab(1);");
		        getPageflow().getPage().s.appendNL("</script>");
		        
		    } else if (zXType.pageflowEditFormType.pfeftColumns.equals(getEditFormType())) {
		        getPageflow().getPage().s.appendNL("</tr></table>");
		    }
		    
		    /**
		     * Handle buttons
		     */
		    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftEdit);
		    getPageflow().processFormButtons(this);
		    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftEdit);
		    
            
            /**
             * Optionally close the form.
             */
		    if (!getPageflow().isOwnForm() 
		        && !getPageflow().resolveDirector(tagValue("zXNoFormEnd")).equalsIgnoreCase("1")) {
		        getPageflow().getPage().s.appendNL("</form>");
		    }
		    
		    /**
		     * Close page.
		     */
		    if (!getPageflow().getQs().getEntryAsString("-err").equalsIgnoreCase(getName())
		        || (StringUtil.len(strAttr) == 0 && StringUtil.len(getPageflow().getForceFocusAttr()) == 0 )) {
		        getPageflow().getPage().getPostLoadJavascript().add("  zXSetCursorToFirstField();");
		        
		    } else {
		        /**
		         * Special attention to multi-tab pages as the error may not be on the
		         * active tab in which case we have to make the appropriate tab active
                 *  If strAttr is '' it means we got here because we have a force focus attr
		         */
                if (StringUtil.len(strAttr) == 0) strAttr = getPageflow().getForceFocusAttr();
                
		        if(zXType.pageflowEditFormType.pfeftTabs.equals(getEditFormType())) {
		            intPage = 0;
		            
		            iter = getEditpages().iterator();
		            while (iter.hasNext()) {
		                objEditPage = (PFEditPage)iter.next();
		                
		                intPage = intPage + 1;
		                
		                for (int i = 0; i < intEditpages; i++) {
		                    objEntityGroup = (Tuple)objEditPage.getEntitygroups().get(i);
		                    objEntity = (PFEntity)colEntities.get(objEntityGroup.getName());
		                    
		                    /**
		                     * It is NOT water tight as the only thing we no is the attribute where
		                     *  the error occured, it may be that the same attribute is available
		                     *  in multiple entities but it is better than nothing:
		                     *  go through the pages and simply assume that the first attribute that
		                     *  we find of that name is the one in error....
		                     */
		                    if (objEntity.getBo().getDescriptor().getGroup(objEntityGroup.getValue()).inGroup(strAttr)) {
		                        getPageflow().getPage().getPostLoadJavascript().add("  zXSelectTab(" + intPage + ");");
		                        getPageflow().getPage().getPostLoadJavascript().add("  zXSetCursorToFirstField('" + strControlName + "');");
		                    }
		                }  
		            }
		        } else {
		            /**
		             * No tabs, so all fields are visible
		             */
		            getPageflow().getPage().getPostLoadJavascript().add("  zXSetCursorToFirstField('" + strControlName + "');");        
		        }
		    }
		    
		    /**
		     * Handle window title and footer
		     */
		    if (getPageflow().editSubActionIncludes(strSubActionValue, "r")) {
		        getPageflow().handleFooterAndTitle(this, "Enter Data");
		    } else {
		        getPageflow().handleFooterAndTitle(this, "Edit Data");
		    }
            
            /**
             * Find the link action.
             */
	        getPageflow().setAction(getPageflow().resolveLink(getLink()));
	        
		    return go;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Execute pageflow action.", e);
            
		    if (getZx().throwException) throw new ZXException(e);
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
        
        Tuple objTuple;
        Iterator iter;
        
        objXMLGen.taggedValue("qssubaction", getQssubaction());
        
        /**
         * BD13JUL05 - V1.5:36
         */
        objXMLGen.taggedValue("nodb", isNodb());
        
		/**
		 * DGS 13FEB2003: Added editformtype and editpages tags
		 */
        objXMLGen.taggedValue("editformtype", zXType.valueOf(getEditFormType()));
        
		if (getEditpages() != null && !getEditpages().isEmpty()) {
		    objXMLGen.openTag("editpages");
		    
			PFEditPage objEditPage;
			
			iter = getEditpages().iterator();
			while (iter.hasNext()) {
			    objEditPage = (PFEditPage)iter.next();
			    
			    objXMLGen.openTag("editpage");
			    
			    if (objEditPage.getEntitygroups() != null && !objEditPage.getEntitygroups().isEmpty()) {
			        objXMLGen.openTag("entitygroups");
			        
			        int intEntitygroups = objEditPage.getEntitygroups().size();
			        for (int i = 0; i < intEntitygroups; i++) {
			            objTuple = (Tuple)objEditPage.getEntitygroups().get(i);
			            
			            objXMLGen.openTag("entitygroup");
			            objXMLGen.taggedValue("entity", objTuple.getName());
			            objXMLGen.taggedValue("group", objTuple.getValue());
			            objXMLGen.closeTag("entitygroup");
			        }
			        
			        objXMLGen.closeTag("entitygroups");
			    }
			    
			    getDescriptor().xmlLabel("label", objEditPage.getLabel());
			    
			    objXMLGen.closeTag("editpage");
			}
			
			objXMLGen.closeTag("editpages");
		}
		
		/**
		 * DGS 27JUN2003: Added edit sub actions
		 */
		if (this.editsubactions != null && !this.editsubactions.isEmpty()) {
		    objXMLGen.openTag("editsubactions");
		    
		    iter = getEditsubactions().iterator();
		    while (iter.hasNext()) {
		        objTuple = (Tuple)iter.next();
		        objXMLGen.taggedValue(objTuple.getName(), objTuple.getValue());
		    }
		    
		    objXMLGen.closeTag("editsubactions");
		}
		
    }
    
    /**
     * @see PFAction#clone(Pageflow)
     */
    public PFAction clone(Pageflow pobjPageflow) {
        PFEditForm cleanClone = (PFEditForm)super.clone(pobjPageflow);
        
        cleanClone.setQssubaction(getQssubaction());
        if (this.editsubactions != null) {
            cleanClone.setEditsubactions((ZXCollection)getEditsubactions().clone());
        }
        
        cleanClone.setEditFormType(getEditFormType());
        cleanClone.setNodb(isNodb());
        
        /** 
         * Do not clone editPages for a normal edit form, as this is created on the fly.
         **/
        if (!zXType.pageflowEditFormType.pfeftNormal.equals(getEditFormType())) {
            if (getEditpages() != null && getEditpages().size() > 0) {
                cleanClone.setEditpages(CloneUtil.clone(getEditpages()));
            }
        }
        
        return cleanClone;
    }
}