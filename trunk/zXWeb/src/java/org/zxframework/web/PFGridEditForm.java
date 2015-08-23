/*
 * Created on Apr 12, 2004
 * $Id: PFGridEditForm.java,v 1.1.2.27 2006/07/17 16:28:43 mike Exp $
 */
package org.zxframework.web;

import java.util.Iterator;

import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;

/**
 * Pageflow grid edit form action object.
 * 
 * <pre>
 * 
 * Change    : BD27OCT03
 * Why       : Added support for the tags zXGridNoUpdate and zXGridNoDelete
 * 
 * Change    : BD26NOV03
 * Why       : Fixed minor bug: make sure that the entities are updated
 *             in the BO context when we loop over the recordset
 * 
 * Change    : DGS12FEB2004
 * Why       : While creating the 'Matrix' edit form, realised was duplicating the code
 *             that handles enhancers, so removed it to a common function.
 * 
 * Change    : BD16FEB04
 * Why       : Fixed few small inefficiencies and added support for
 *             entities that have no selectEdit group set
 * 
 * Change    : BD21FEB04
 * Why       : Do a processAttributeValues for all entities per row
 * 
 * Change    : DGS23FEB04
 * Why       : - Highlight error rows
 *             - Major change to the way 'in progress' is handled - now always selects from
 *               the database, and then incorporates entered data from the createupdate saver BO.
 * 
 * Change    : BD24FEB04
 * Why       : Fixed small problem for new rows: now also do process attribute values
 *             for new rows
 * 
 * Change    : BD27FEB04
 * Why       : When generating popup menus, use the action name as well to
 *             make the name unique; this allows for combining different
 *             actions with popup menus on a single page
 * 
 * Change    : DGS09MAR2004
 * Why       : Use new variant of resetBO so that default values are not loaded into BOs.
 *             Otherwise new rows get zeroes, 1900 date etc. which looks bad.
 *             Also incorporated support for tag to switch off showing row counts.
 * 
 * Change    : BD1MAY04
 * Why       : Fixed bug in constructRowURL; did not take frameNo into
 *             consideration
 * 
 * Change    : BD9JUN04
 * Why       : Added support for concurrencyControl (rather than auditable) and
 *             limit audit columns to ~ instead of !
 * 
 * Change    : MB08NOV04
 * Why       : Add support for resorturl.
 * 
 * Change    : BD24FEB05 V1.4:47
 * Why       : Fixed problem with updating BO context
 * 
 * Change    : BD5APR05 - V1.5:1
 * Why       : Added support for data-sources
 * 
 * Change    : BD5APR05 - V1.5:13
 * Why       : Implement paging at data-source level
 * 
 * Change    : BD/DGS14JUL2005 - V1.5:40
 * Why       : Fixed bug in 'go' where was referencing objRS.EOF after it was closed
 * 
 * Change    : V1.5:65 - BD7NOV05
 * Why       : Added parameterBag support
 *
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFGridEditForm extends PFAction {

    //------------------------ Members
    
    private PFUrl pagingurl;
    private PFUrl resorturl;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFGridEditForm() {
        super();
    }
    
    //------------------------ Getters/Setters
    
    /**
     * The base url of the next previous links.
     * @return Returns the pagingurl.
     */
    public PFUrl getPagingurl() {
        return pagingurl;
    }
    
    /**
     * @param pagingurl The pagingurl to set.
     */
    public void setPagingurl(PFUrl pagingurl) {
        this.pagingurl = pagingurl;
    }
    
    /**
     * The url for resorting the list form.
     * 
     * @return Returns the resorturl.
     */
    public PFUrl getResorturl() {
        return resorturl;
    }
    
    /**
     * @param resorturl The resorturl to set.
     */
    public void setResorturl(PFUrl resorturl) {
        this.resorturl = resorturl;
    }
    
    //------------------------ Private helper methods
    
    /**
     * Determine what the URL should be for this row (a popup). 
     * 
     * <pre>
     * DGS28FEB2003: New code for a popup
     * 
     * Note that this function is similar
     * to the normal constructRowURL but uses the given URL rather than that of the listform itself.
     * We use the entity object to get entity name and PK, and we use the URL object to get the
     *  main url (this has come from a popup ref url, not the listform's url).
     * </pre>
     *
     * @param pobjEntity  The Pageflow entity you want the popup of.
     * @param pobjUrl  The url that it is going to.
     * @param pstrConfirm Confirm message. 
     * @return Returns a string for the link for a popup window.
     * @throws ZXException Thrown if constructPopupRowURL fails. 
     */
    private String constructPopupRowURL(PFEntity pobjEntity, PFUrl pobjUrl, String pstrConfirm) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjEntity", pobjEntity);
            getZx().trace.traceParam("pobjUrl", pobjUrl);
            getZx().trace.traceParam("pstrConfirm", pstrConfirm);
        }
        
        String constructPopupRowURL = ""; 
        
        try {
            
            /**
             * If no URL: we're done
             */
            if (pobjUrl == null) {
                return constructPopupRowURL;
            }
            
            constructPopupRowURL = getPageflow().constructURL(pobjUrl);    
            
            /**
             * Be smart and replace rRef with fRefx if frame x is requested
             * Only replace first instance
             * DGS28FEB2003: Again, don't do this for popups
             */
            constructPopupRowURL = getPageflow().wrapRefUrl(getPageflow().resolveDirector(pobjUrl.getFrameno()), 
														    constructPopupRowURL, 
															pstrConfirm, 
															false,
															true);
            
            return constructPopupRowURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine what the URL should be for this row (a popup).", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjEntity = "+ pobjEntity);
                getZx().log.error("Parameter : pobjUrl = "+ pobjUrl);
                getZx().log.error("Parameter : pstrConfirm = "+ pstrConfirm);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return constructPopupRowURL;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(constructPopupRowURL);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Determine what the URL should be for this row.
     *
     * @param pobjUrl The list url.
     * @param pobjEntity The PF Entity used.
     * @param pstrID The pk.
     * @return Returns the URL should be for this row.
     * @throws ZXException  Thrown if constructRowURL fails. 
     */
    private String constructRowURL(PFUrl pobjUrl, 
    							   PFEntity pobjEntity, 
    							   String pstrID) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjUrl", pobjUrl);
            getZx().trace.traceParam("pobjEntity", pobjEntity);
            getZx().trace.traceParam("pstrID", pstrID);
        }

        String constructRowURL = "";
        
        try {
            
            /**
             * If no URL for the list: we're done
             */
            if (pobjUrl == null) {
                return constructRowURL;
            }
            
            if (pobjUrl.getUrlType().equals(zXType.pageflowUrlType.putPopup)) {
                /**
                 * url is appended by unique id. This is then undone after constructing the row URL
                 * and generating the popup (as the same url is used by all rows, not just this one).
                 */
                String strPopupName = pobjUrl.getUrl();
                pobjUrl.setUrl(StringEscapeUtils.escapeHTMLTag(getName() + strPopupName + "_" + pstrID));
                
                constructRowURL = getPageflow().constructURL(pobjUrl);
                
                /**
                 * Now generate the popup menu javascript.
                 */
                boolean blnStartMenu = true;
                
                PFRef objRef;
                
                int intRefs = getPopups().size();
                for (int i = 0; i < intRefs; i++) {
                    objRef = (PFRef)getPopups().get(i);
                    
                    /**
                     * Only interested in popups for this name...
                     */
                    if (objRef.getName().equalsIgnoreCase(strPopupName)) {
                        /**
                         * ...that are active
                         */
                        if (getPageflow().isActive(objRef.getUrl().getActive())) {
                            
                            if (blnStartMenu) {
                                blnStartMenu = false;
                                /**
                                 * Start new entire popup menu:
                                 */
                                getPageflow().getPage().popupMenuStart(pobjUrl.getUrl());
                            }
                            
                            /**
                             * Popup menu name (in url) has already been suffixed by PK to keep it unique
                             */
                            getPageflow().getPage().popupMenuOption(pobjUrl.getUrl(),
                            										getPageflow().resolveLabel(objRef.getLabel()),
                            										constructPopupRowURL(pobjEntity,
                            															 objRef.getUrl(),
                            															 getZx().resolveDirector(getPageflow().resolveLabel(objRef.getConfirm()))),
                            										objRef.getImg(), 
                            										objRef.getImgover(), 
                            										objRef.isStartsubmenu());
                        }
                    }
                      
                }
                
                if (!blnStartMenu) {
                    /**
                     * We found at least one active popup, so end the popup
                     */
                    getPageflow().getPage().popupMenuEnd();
                }
                
                /**
                 * Copy the original url back (without the PK suffix)
                 */
                pobjUrl.setUrl(strPopupName);
                
            } else {
                constructRowURL = getPageflow().constructURL(pobjUrl);
                String strFrameno = getPageflow().resolveDirector(pobjUrl.getFrameno());
                constructRowURL = getPageflow().wrapRefUrl(strFrameno, constructRowURL, "",  false, false);
                
            }
            
            return constructRowURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine what the URL should be for this row.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjUrl = "+ pobjUrl);
                getZx().log.error("Parameter : pobjEntity = "+ pobjEntity);
                getZx().log.error("Parameter : pstrID = "+ pstrID);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return constructRowURL;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(constructRowURL);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Implemented Methods from PFAction
    
    /**
     * Handle this action
     *  
     * @see PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
		if(getZx().trace.isApplicationTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
        zXType.rc go = zXType.rc.rcOK;
        DSRS objRS = null;
        String strOrgSelectWidth = null;
        
		try {
		    /**
		     * Determine how progress stored in the querystring
		     */
		    String strQSProgress = StringUtil.len(getQsprogress()) == 0 ? "-prgs" : getQsprogress();
		    
		    /**
		     * Check if we are already in progress by looking at the prgs entry in
		     * the querystring; but only when this matches my name!
		     */
		    boolean blnInProgress = getPageflow().getQs().getEntryAsString(strQSProgress).equals(getName());
		    
		    /**
		     * Save name of attribute last in error
		     */
		    String strAttr = getZx().trace.getUserErrorAttr();
		    
		    /**
		     * Get entity collection and store in context. However: if the subaction is
		     * error, it means that this routine is called directly after a
		     * clsPFCreateUpdate.go that detected input errors. In that case, we
		     * will use the entity collection from the context (ie the one that has
		     * been created by the clsPFCreateUpdate.go routine) because otherwise
		     * we will create new business objects and thus overwrite any input that
		     * the user has already done
		     */
		    ZXCollection colEntities = getPageflow().getEntityCollection(this,
                                                                         zXType.pageflowActionType.patGridEditForm,
                                                                         zXType.pageflowQueryType.pqtSearchForm, 
                                                                         "");		    
		    if (colEntities == null) {
	            throw new Exception("Unable to get entities for action");
	        }
            
            /**
             * See if we are not doing things we cant do
             */
            if (!getPageflow().validDataSourceEntities(colEntities)) {
                throw new Exception("Unsupported combination of data-source handlers");
            }
            
		    /**
		     * Handle edit enhancers - now done in called function
		     */
            if (!getPageflow().handleEnhancers(getEditenhancers(), colEntities).equals(zXType.rc.rcOK)) {
                throw new Exception("Failed to handle the enhancers for the grid edit form.");
            }
            
	        /**
	         * Set context variable
	         */
	        getPageflow().setContextEntities(colEntities);
	        getPageflow().setContextEntity((PFEntity)colEntities.iterator().next());
	        
            /**
             * Get data-source handler
             */
            PFEntity objTheEntity = getPageflow().getContextEntity(); // In case of a channel there can ever only be one entity
            DSHandler objDSHandler = objTheEntity.getDSHandler(); // Data-source handler
            boolean blnIsDSChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            /**
             * Handle any parameter-bag URL entries
             */
            PFUrl objUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getUrl());
            PFUrl objPagingUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getPagingurl());
            PFUrl objResortUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getResorturl());
            
	        /**
	         * Page title
	         */
	        if (getTitle() != null) {
	            getPageflow().getPage().formTitle(zXType.webFormType.wftEdit, getPageflow().resolveLabel(getTitle()));
	        }
	        
	        /**
	         * Handle any outstanding messages
	         */
		    getPageflow().processMessages(this);
		    
		    /**
		     * Check if there should be a select button. The default is no.
		     */
		    boolean blnSelect;
		    if (objUrl == null || StringUtil.len(objUrl.getUrl()) == 0) {
		        blnSelect = false;
		    } else {
		        blnSelect = !objUrl.getUrl().equalsIgnoreCase("#dummy");
		    }
            
		    /**
		     * Make sure that the form header is not be generated
		     * if the calling application handles it
		     */
		    String strUrl;
            if (!getPageflow().isOwnForm() && !hasTag("zXNoFormStart")) {
                /**
                 * Construct formaction URL
                 */
                strUrl = getPageflow().constructURL(getFormaction());
                
                /**
                 * Only one page.
                 */
                getPageflow().getPage().s.append("<form ")
                						 .appendAttr("method", "post")
                						 .appendAttr("action", strUrl)
                						 .appendNL('>');
            }
		    
		   /**
		    *  Multi-step warning mechanism
		    * If the editform is called as a result of a warning being found (and not ignored by
		    * the user) in the create-update, we are going to insert a checkbox in the edit form
		    * that will allow the user to ignore any messages
		    */ 
		    if (StringUtil.len(getPageflow().getQs().getEntryAsString("-wrn")) > 0) {
		        getPageflow().getPage().editFormIgnoreWarning();
		        
		        /**
		         * For true safety, delete -wrn from the query string
		         * it is for internal use only and ensures that we do not by accident
		         * when we have multiple edit forms on a single screen
		         */
		        getPageflow().getQs().setEntry("-wrn", null);
                
		    } // Has -wrn set
		    
		    /**
		     * Get query and print as debug if needed
		     */
            String strQuery = "";
            String strQueryName = getPageflow().resolveDirector(getQueryname());
            
            // Query elements for channel
            String strWhereClause =  "";
            String strOrderByClause = "";
            boolean blnReverse = false;
            
            if (blnIsDSChannel) {
                strWhereClause = getPageflow().retrieveQueryWhereClause(strQueryName);
                strOrderByClause = getPageflow().retrieveQueryOrderByClause(strQueryName);
                
                /**
                 * When the order by clause was generated as a result of the query action (eg from search form),
                 * it may start with a - that indicatesd DESC
                 */
                if (StringUtil.len(strOrderByClause) > 1 && strOrderByClause.charAt(0) == '-') {
                    strOrderByClause = strOrderByClause.substring(1);
                    blnReverse = true;
                }
                
                /**
                 * Clause not stored with leading ':'
                 */
                if (StringUtil.len(strWhereClause) > 0) strWhereClause  = ":" + strWhereClause;

                if (getPageflow().isDebugOn()) {
                    getPageflow().getPage().debugMsg(objTheEntity.getName() + "." + objTheEntity.getSelectlistgroup());
                    if (StringUtil.len(strWhereClause) > 0) {
                        getPageflow().getPage().debugMsg("Where " + strWhereClause);
                    }
                    if (StringUtil.len(strOrderByClause) > 0) {
                        getPageflow().getPage().debugMsg("Order by " + strOrderByClause);
                    }
                }
                
            } else {
                strQuery = getPageflow().retrieveSQL(strQueryName);
                
                if (getPageflow().isDebugOn()) {
                    getPageflow().getPage().debugMsg(strQuery);
                }
                
            } // Channel or RDBMS
		    
		    /**
		     * Check whether paging is required
		     */
		    boolean blnPaging = (StringUtil.len(getPageflow().constructURL(objPagingUrl)) > 0);
		    
		    /**
		     * Open table
		     */
		    getPageflow().getPage().s.append("<table ")
						             .appendAttr("width", (StringUtil.len(getWidth()) > 0? getWidth() : "100%"))
						             .appendNL('>');
		    
		    /**
		     * The label for the select button column may have been overridden
		     * by the tag zXSelectLabel
		     * The width of the select button column can be overruled by the tag
		     * zXSelectWidth
		     */
		    String strSelectLabel = "";
		    String strTmp = tagValue("zXSelectLabel");
		    if (StringUtil.len(strTmp) > 0) {
		        strSelectLabel = strTmp;
		    }
		    
		    strTmp = tagValue("zXSelectWidth");
		    if (StringUtil.len(strTmp) > 0) {
		        strOrgSelectWidth = getPageflow().getPage().getWebSettings().getListFormColumn1();
		        getPageflow().getPage().getWebSettings().setListFormColumn1(strTmp);
		    }
		    
		    /**
		     * The label for the action drop-down column may have been overridden
		     * by the tag zXActionLabel
		     */
		    String strActionLabel = "";
		    strTmp = tagValue("zXActionLabel");
		    if (StringUtil.len(strTmp) > 0) {
		        strActionLabel = strTmp;
		    }
		    
	        /**
	         * Bit tricky: in HTML.gridHeader we add -oa and -od to the
	         * URL; these may already be in the QS collection so will also
	         * be added by constructUrl so we have to take them out again
	         */
	        if (getPageflow().getPFDesc().isPropagaTeqs()) {
	            getPageflow().getQs().setEntry("-oa" + getPageflow().QSSortKeyPostFix(), null);
	            getPageflow().getQs().setEntry("-od" + getPageflow().QSSortKeyPostFix(), null);
	        }
		    
		    getPageflow().getPage().gridHeaderOpen(blnSelect, strSelectLabel, strActionLabel);
		    
		    /**
		     * Determine whether we have permission to create/edit and delete entitys
		     */
		    boolean blnMayInsert = true;
		    boolean blnMayUpdate = true;
		    boolean blnMayDelete = true;
		    
		    PFEntity objEntity;
		    Iterator iter = colEntities.iterator();
		    while (iter.hasNext()) {
		        objEntity = (PFEntity)iter.next();
		        
		        getPageflow().getPage().gridHeader(objEntity.getBo(), 
		        								   objEntity.getSelecteditgroup(), 
		        								   objEntity.getVisiblegroup(), 
		        								   getPageflow().constructURL(objResortUrl), 
		        								   isResolvefk());
		        
		        if (blnMayInsert) {
		            blnMayInsert = objEntity.getBo().mayInsert();
		        }
		        
		        if (blnMayUpdate) {
		            blnMayUpdate = objEntity.getBo().mayUpdate();
		        }
		        
		        if (blnMayDelete) {
		            blnMayDelete = objEntity.getBo().mayDelete();
		        }
                
		    } // Loop over enities
		    
		    /**
		     * If there is a tag zXGridNoUpdate with value 1 we should not allow updates
		     */
		    if (getPageflow().resolveDirector(tagValue("zXGridNoUpdate")).equals("1")) {
		        blnMayUpdate = false;
		    }
		    
		    /**
		     * Same for delete
		     */
		    if (getPageflow().resolveDirector(tagValue("zXGridNoDelete")).equals("1")) {
		        blnMayDelete = false;
		    }
		    
		    getPageflow().getPage().gridHeaderClose();
		    
		    /**
		     * Determine the maximum number of rows to display
		     */
		    int intMaxResultRows = 0;
		    if (getMaxrows() > 0) {
		        intMaxResultRows = getMaxrows();
		    } else {
		        if (getPageflow().getPage().getWebSettings().getMaxListRows() > 0) {
		            intMaxResultRows = getPageflow().getPage().getWebSettings().getMaxListRows();
		        } else {
		            intMaxResultRows = 10;
		        }
		    }
		    
		    /**
		     * Determine the number of new rows to display
		     */
		    int intNewRows = 0;
		    String strNewRows = getPageflow().resolveDirector(getNewrows());
		    if (StringUtil.isNumeric(strNewRows)) {
		        intNewRows = new Integer(strNewRows).intValue();
		    }
		    
		    /**
		     * Figure out on what page we are on. Default to page 1.
		     */
		    int intPage = 0;
            int lngStartRow = 0;            // The position to start
            int lngBatchSize = 0;           // The number of records to recieve.
		    boolean blnMorePages = false;
		    if (blnPaging) {
		        String strPage = getPageflow().getQs().getEntryAsString("-pg");
		        if (StringUtil.len(strPage) == 0) {
                    // Default to page one.
		            intPage = 1;
		            
		        } else {
		            intPage = Integer.parseInt(strPage);
		            if (blnInProgress) {
		                blnMorePages = (StringUtil.len(getPageflow().getQs().getEntryAsString("-pgmore")) > 0);
		            }
		            
		        }
                
                /**
                 * Implement the 'limit' if requested.
                 * 
                 * Note that we always ask the system for one more row than we really need so that we
                 * can give a message if the resultset is truncated
                 */
                lngStartRow = intMaxResultRows * (intPage - 1) + 1;
                if (isLimitrows()) {
                    lngBatchSize = intMaxResultRows + 1;
                }
                
		    } else {
                lngStartRow = 0;
                if (isLimitrows()) {
                    lngBatchSize = intMaxResultRows + 1;
                }
                
            } // Paging
		    
		    /*******************************************
		     * Execute query and loop through the results
		     *******************************************/
		    
		    /**
		     * Turn query into recordset
		     * DGS24FEB2004: Previously we didn't access the database if 'in progress' i.e.
		     * after an error. However, there may be attributes in the select list group (or
		     * in another entity that has no select edit attrs) that are needed for
		     * expressions etc., so the only safe thing to so is select again and then
		     * place the select edit attr values back over the top of the selected data.
		     */
            if (blnIsDSChannel) {
                objRS = objDSHandler.boRS(objTheEntity.getBo(),
                                          objTheEntity.getSelectlistgroup(),
                                          strWhereClause,
                                          isResolvefk(),
                                          strOrderByClause, blnReverse,
                                          lngStartRow, lngBatchSize);
            } else {
                objRS = ((DSHRdbms)objDSHandler).sqlRS(strQuery,
                                                       lngStartRow, lngBatchSize);
                
            }
            
		    if (objRS == null) {
		        throw new Exception("Unable to execute query");
		    }
            
		    ZXBO objIBO;
		    zXType.persistStatus enmPersistStatus;
		    String strGroup = "";
            int intRowCount = 1;
            
	        while (!objRS.eof() && intRowCount <= intMaxResultRows) {
	            /**
	             * Populate all the business objects...
	             */
	            iter = colEntities.iterator();
	            while (iter.hasNext()) {
	                objEntity = (PFEntity)iter.next();
	                
	                /**
	                 * Make sure we have new collection on first time around
	                 */
	                if (!blnInProgress && intRowCount == 1) {
	                    objEntity.setBOSavers(new  ZXCollection());
	                }
                    
	                objRS.rs2obj(objEntity.getBo(), 
	                			 objEntity.getSelectlistgroup(), 
	                			 isResolvefk());
	                
	                /**
	                 * BD21FEB04 Added
	                 */
	                getZx().getBOContext().setEntry(objEntity.getName(), objEntity.getBo());
	                getPageflow().processAttributeValues(objEntity);
	                
	                /**
	                 * BD21FEB04 Was group *
	                 * DGS24FEB2004: If we already have the BO in the BOSavers because the
	                 * createupdate put it there, don't add the DB-selected BO but do use
	                 * its attr values except for those in the select edit group, as those
	                 * will have been loaded by the createupdate from the form.
	                 */
	                if (blnInProgress && StringUtil.len(objEntity.getSelecteditgroup()) > 0) {
	                    objIBO = (ZXBO)objEntity.getBOSavers().get(String.valueOf(intRowCount));
	                    if (objIBO == null) {
	                        throw new Exception("Unable to load expected saved row");
	                    }
	                    enmPersistStatus = objIBO.getPersistStatus();
	                    
	                    if (enmPersistStatus.equals(zXType.persistStatus.psClean)) {
	                        /**
	                         * Ignore the data from the form if the row has a blank persist status
	                         * i.e. the user doesn't want to change the row
	                         */
	                        strGroup = objEntity.getSelectlistgroup();
	                        
	                    } else {
	                        /**
	                         * Only copy the data from the form to the BO if the row has a non-blank
	                         * persist status i.e. the user wants to change something on the row
	                         */
	                        strGroup = objIBO.getDescriptor().getGroupMinus(objEntity.getSelectlistgroup(), 
	                        												objEntity.getSelecteditgroup()).formattedString();
	                        
	                    }
	                    
	                    objEntity.getBo().bo2bo(objIBO, strGroup, false);
	                    
	                    /**
	                     * Reset the persist status to what it was from the editform, because
	                     * the BO2BO will have made it 'dirty'.
	                     */
	                    objIBO.setPersistStatus(enmPersistStatus);
	                    
	                } else {
	                    objIBO = objEntity.getBo().cloneBO(objEntity.getSelectlistgroup(), true, false);
	                    objEntity.getBOSavers().put(String.valueOf(intRowCount), objIBO);
	                    
	                }
	            }
                
                intRowCount++;
                  
                objRS.moveNext();
                
	        } // Loop over recordset		    
	        
	        /**
	         * Is there more results for paging
	         */
	        if (!objRS.eof()) {
	            blnMorePages = true;
	        }
            
	        /****************************************************************************
	         * Here have finished selecting data and loading into BOSavers. 
             * Now loop through to display
	         ****************************************************************************/
	        int intActualUpdateRowCount = intRowCount - 1;
	        
	        intRowCount = 1;
	        boolean blnOdd = false;
	        boolean blnDidOpen = false;
	        String strClassToUse = "";
	        String strStoreAlias = "";
	        String strFirstError = "";
	        
	        boolean blnSelected = false;
	        while (intRowCount <= intActualUpdateRowCount) {
	            /**
	             * We need this because when we have multiple entities, we still
	             * only want to generate the open i.HTML once and only once
	             */
	            blnDidOpen = false;
	            strClassToUse = "";
	        
	            /**
	             * Tricky: the BOSavers collections is the true working set
	             * however we have to re-update the BO context as we may
	             * need this when constructing the URL (e.g. #attrValue)
	             */
	            iter = colEntities.iterator();
	            while (iter.hasNext()) {
	                objEntity = (PFEntity)iter.next();
	                
	                if (StringUtil.len(objEntity.getSelecteditgroup()) > 0 || StringUtil.len(objEntity.getSelectlistgroup()) > 0) {
	                    objIBO = (ZXBO)objEntity.getBOSavers().get(String.valueOf(intRowCount));
	                    if (objIBO != null) {
	                        getZx().getBOContext().setEntry(objEntity.getName(), objIBO);
	                        /**
	                         * DGS23FEB2004: A property object is put into the saver BO by the createupdate if
	                         * this entity on this row was in error. If so, will highlight using stylesheet class
	                         */
	                        if (StringUtil.len(strClassToUse) == 0) {
	                            if (objIBO.getOM().get("zXGridErr") != null) {
	                                strClassToUse = "zxErrorCell";
	                            }
	                        }
                            
	                    } // Could retrieve BO from BOSavers collection
                        
	                } // Has edit or selectList group
                    
	            } // Loop over entities
	            
	            int intEntityCount = 1;
	            
	            iter = colEntities.iterator();
	            while (iter.hasNext()) {
	                objEntity = (PFEntity)iter.next();
	                
	                if (StringUtil.len(objEntity.getSelecteditgroup()) > 0) {
	                    objIBO = (ZXBO)objEntity.getBOSavers().get(String.valueOf(intRowCount));
	                    
	                    /**
	                     * Update the global context entity.
	                     * 
	                     * BD9JUN04 - Changed from auditable to concurrencyControl
	                     * 
                         * BD24FEB05 V1.4:47 - Use select list group rather than edit group
	                     */
	                    objIBO.bo2bo(objEntity.getBo(), 
                                    (objEntity.getBo().getDescriptor().isConcurrencyControl() ? 
                                    		objEntity.getSelectlistgroup() + ",~" : 
                                    		objEntity.getSelectlistgroup()), 
                                     false);
	                    
	                    /**
	                     * BD24FEB04 Added
	                     */
	                    getZx().getBOContext().setEntry(objEntity.getName(), objEntity.getBo());
	                    
	                    getPageflow().setContextEntity(objEntity);
	                    
	                    if (!blnDidOpen) {
	                        /**
	                         * Open the list row
	                         */
	                        blnDidOpen = true;
	                        
	                        /**
	                         * Determine the url associated with the row (if there
	                         * is one)
	                         */
	                        if (blnSelect) {
	                            strUrl = constructRowURL(objUrl, objEntity, "" + intRowCount);
	                        } else {
	                            strUrl = "";
	                        }
	                        
	                        /**
	                         * DGS23FEB2004: We might have already decided this row is in error and set the class accordingly
	                         */
	                        if (StringUtil.len(strClassToUse) == 0) {
	                            /**
	                             * Determine whether we want to add a special class
	                             */
	                            strClassToUse = getPageflow().resolveDirector(getClazz());
	                            
	                            if (StringUtil.len(strClassToUse) > 0) {
	                                if (isAddparitytoclass()) {
	                                    if (blnOdd) {
	                                        strClassToUse = strClassToUse + "Odd";
	                                    } else {
	                                        strClassToUse = strClassToUse + "Even";
	                                    }
	                                }
	                            }
	                            
	                        }
	                        
	                        /**
	                         * Now ready to generate the i.HTML to open the gridrow
	                         */
	                        blnSelected = (!objIBO.getPersistStatus().equals(zXType.persistStatus.psClean));
	                        
	                        getPageflow().getPage().gridRowOpen(intRowCount, 
									                            blnOdd, 
									                            objIBO.getPersistStatus(), 
									                            blnSelected, 
									                            blnMayInsert, 
									                            blnMayUpdate, 
									                            blnMayDelete, 
									                            strUrl, 
									                            strClassToUse);
	                        
	                    } // Has already done row open sequence
	                    
	                    /**
	                     * Tweak the alias so that every field is unique per row/entity
	                     */
	                    strStoreAlias = objIBO.getDescriptor().getAlias();
	                    objIBO.getDescriptor().setAlias(objEntity.getName() + getName() + "Row" + intRowCount);
	                    
	                    /**
	                     * Insert anchor
	                     */
	                    String strRowPK = objIBO.getPKValue().getStringValue();
	                    String strPK = "";
	                    if (strRowPK.equals(strPK)) {
	                        getPageflow().getPage().s.appendNL("<A name='zXActiveRow' id='zXActiveRow'></A>");
	                    }
	                    
	                    getPageflow().resolveGroups(objEntity);
	                    
	                    /**
	                     * Generate the columns
	                     */
	                    getPageflow().getPage().gridEditForm(intRowCount, 
	                    									 objIBO, 
	                    									 objEntity.getSelecteditgroup(), 
	                    									 objEntity.getLockgroup(), 
	                    									 objEntity.getVisiblegroup());
	                    
                        if (objIBO.getOM().get("zXGridErr") != null) {
                            if (StringUtil.len(strFirstError) == 0 && StringUtil.len(strAttr) > 0) {
                                strFirstError = getPageflow().getPage().controlName(objIBO, objIBO.getDescriptor().getAttribute(strAttr));
                            }
                        }
	                    
	                    /**
	                     * Set the alias back to original value to avoid any problems
	                     */
	                    objIBO.getDescriptor().setAlias(strStoreAlias);
	                    
	                } // Has selectdit group
	                
	                intEntityCount = intEntityCount + 1;
	                
	            } // Loop over entities
	            
                /**
                 * Close the listrow
                 */
	            getPageflow().getPage().gridRowClose();
	            
	            /**
	             * Switch row color
	             */
	            blnOdd = !blnOdd;
	            intRowCount = intRowCount + 1;
	        }
	        
	        /**
	         * Create a hidden field to hold the count of update rows
	         */
	        getPageflow().getPage().s.append("<input type=\"hidden\" ")
	        						 .appendAttr("name", "zXGridEditFormCount" + getName())
	        						 .appendAttr("value", String.valueOf(intActualUpdateRowCount))
	        						 .appendNL('>');
	        
	        /**
	         * Create a hidden field to indicate there are more pages
	         */
	        if (blnPaging) {
	            getPageflow().getPage().s.append("<input type=\"hidden\" ")
					            		 .appendAttr("name", "zXGridPage" + getName())
					            		 .appendAttr("value", "" + intPage)
					            		 .appendNL('>');
	            
	            if (blnMorePages) {
	                getPageflow().getPage().s.append("<input type=\"hidden\" ")
	                						 .appendAttr("name", "zXGridMorePages" + getName())
	                						 .appendAttr("value", "Y")
	                						 .appendNL('>');
	            }
	        }
		    
		    /**
		     * Now add required empty rows for new.
		     */
            int intEntityCount;
	        if (blnMayInsert) {
                
	            int intNewRowCount = 1;
                
	            while (intNewRowCount <= intNewRows) {
	                /**
	                 * We need this because when we have multiple entities, we still
	                 * only want to generate the open i.HTML once and only once
	                 */
	                blnDidOpen = false;
	                strClassToUse = "";
	                /**
	                 * Initialize all the business objects...
	                 */
                    intEntityCount = 1;
	                iter = colEntities.iterator();
	                while (iter.hasNext()) {
	                    objEntity = (PFEntity)iter.next();
	                    
	                    if (blnInProgress) {
	                        objIBO = (ZXBO)objEntity.getBOSavers().get(String.valueOf(intRowCount));
                            
	                        /**
	                         * DGS23FEB2004: A property object is put into the saver BO by the createupdate if
	                         * this entity on this row was in error. If so, will highlight using stylesheet class
	                         */
	                        if (StringUtil.len(strClassToUse) == 0) {
	                            if (objIBO.getOM().get("zXGridErr") != null) {
	                                strClassToUse = "zxErrorCell";
	                            }
	                        }
	                        
	                    } else {
	                        /**
	                         * When generating stuff for the first time we have to
	                         * process the attribute values as well
	                         * DGS09MAR2004: Use new 'resetExplicitBO' function. This behaves just
	                         * like the classic 'resetBO' except that it does not set attrs to
	                         * default values based on datatype - only explicit default values
	                         * set within the BO XML are set.
	                         */
	                        objEntity.getBo().resetBO("*", false, true);
	                        getZx().getBOContext().setEntry(objEntity.getName(), objEntity.getBo());
	                        getPageflow().processAttributeValues(objEntity);
	                        
	                        /**
	                         * Now clone and copy the attributes but make sure we
	                         * do not create unwanted error messages
	                         */
	                        objIBO = objEntity.getBo().cloneBO();
	                        
	                        boolean blnOrig = objIBO.isValidate();
	                        objIBO.setValidate(false);
	                        objEntity.getBo().bo2bo(objIBO, "*");
	                        objIBO.setValidate(blnOrig);
	                    }
	                    
	                    if (StringUtil.len(objEntity.getSelecteditgroup()) > 0) {
	                        
	                        objEntity.getBo().resetBO();
	                        getPageflow().setContextEntity(objEntity);
	                        
	                        if (!blnDidOpen) {
	                            /**
	                             * Open the list row
	                             */
	                            blnDidOpen = true;
	                            
	                            /**
	                             * Determine the url associated with the row (if there
	                             * is one). In the case of new rows like this we don't use the URL,
	                             * but we need to know if there is one so that the column for the select
	                             *  button is present, to match the update rows.
	                             */
	                            if (blnSelect) {
	                                strUrl = constructRowURL(objUrl, objEntity, "" + intRowCount);
	                            } else {
	                                strUrl = "";
	                            }
	                            
	                            /**
	                             * DGS23FEB2004: We might have already decided this row is in error and set the class accordingly
	                             */
	                            if (StringUtil.len(strClassToUse) == 0) {
	                                /**
	                                 * Determine whether we want to add a special class
	                                 */
	                                strClassToUse = getPageflow().resolveDirector(getClazz());
	                                
	                                if (StringUtil.len(strClassToUse) > 0) {
	                                    if (isAddparitytoclass()) {
	                                        if (blnOdd) {
	                                            strClassToUse = strClassToUse + "Odd";
	                                        } else {
	                                            strClassToUse = strClassToUse + "Even";
	                                        }
	                                    }
	                                }
	                            }
	                            
	                            /**
	                             * Now ready to generate the i.HTML to open the gridrow
	                             */
	                            blnSelected = (!objIBO.getPersistStatus().equals(zXType.persistStatus.psClean) && blnInProgress);
	                            
	                            getPageflow().getPage().gridRowOpen(intRowCount, 
	                            									blnOdd, 
	                            									zXType.persistStatus.psNew, 
	                            									blnSelected,
	                            									
	                            									blnMayInsert, 
	                            									blnMayUpdate, 
	                            									blnMayDelete,
	                            									
	                            									strUrl, 
	                            									strClassToUse);
	                            
	                        } // Has already done row open sequence
	                        
	                        /**
	                         * Generate the columns
	                         */
	                        strStoreAlias = objIBO.getDescriptor().getAlias();
	                        objIBO.getDescriptor().setAlias(objEntity.getName() + getName() + "Row" + intRowCount);
	                        
	                        /**
	                         * At this point we have to force the persist status to New, or the attrs
	                         * are not generated correctly.
	                         */
	                        objIBO.setPersistStatus(zXType.persistStatus.psNew);
	                        
	                        getPageflow().getPage().gridEditForm(intRowCount, 
	                        									 objIBO, 
	                        									 objEntity.getSelecteditgroup(), 
	                        									 objEntity.getLockgroup(), 
	                        									 objEntity.getVisiblegroup());
	                        
	                        objIBO.getDescriptor().setAlias(strStoreAlias);
	                        
	                    } // Has selectEdit group
	                    
	                    intEntityCount = intEntityCount + 1;
                        
	                } //Loop over entities
	                
	                /**
	                 * Close the listrow
	                 */
	                getPageflow().getPage().gridRowClose();
	                
	                /**
	                 * Switch row color
	                 */
	                blnOdd = !blnOdd;
	                
	                intRowCount = intRowCount + 1;
	                intNewRowCount = intNewRowCount + 1;
	                
	            } // Over number of requested new rows
                
	        } // Can do insert
	        
	        /**
	         * Add paging buttons
	         */
	        if (blnPaging) {
	            getPageflow().getPage().s.appendNL("<table width='100%'>");
	            getPageflow().getPage().s.appendNL("<tr>");
	            getPageflow().getPage().s.appendNL("<td width='*'></td>");
	            
	            /**
	             * Do we need previous page button?
	             */
                String strFrameno = getPageflow().resolveDirector(objPagingUrl.getFrameno());
	            if (intPage > 1) {
	                getPageflow().getQs().setEntry("-pg", intPage - 1 + "");
	                strUrl = getPageflow().wrapRefUrl(strFrameno, getPageflow().constructURL(objPagingUrl));
	                
	                getPageflow().getPage().s.appendNL("<td width='1%' align='right'>");
	                
	                getPageflow().getPage().s.append("<img ")
	                						 .appendAttr("src", "../images/prevPage.gif")
	                						 .appendAttr("onMouseOver", "this.src='../images/../images/prevPageOver.gif';")
	                						 .appendAttr("onMouseOut", "this.src='../images/../images/prevPage.gif';")
	                						 .appendAttr("onClick", strUrl)
	                						 .appendNL('>');
	                
	                getPageflow().getPage().s.appendNL("</td>");
	            }
	            
	            /**
	             * Do we need next page button?
	             */
	            if (blnMorePages) {
	                getPageflow().getQs().setEntry("-pg", intPage + 1 + "");
	                strUrl = getPageflow().wrapRefUrl(strFrameno, getPageflow().constructURL(objPagingUrl));
	                
	                getPageflow().getPage().s.appendNL("<td  width='1%' align='right'>");
	                
	                getPageflow().getPage().s.append("<img")
	                						 .appendAttr("src", "../images/nextPage.gif")
	                						 .appendAttr("onMouseOver", "this.src='../images/../images/nextPageOver.gif';")
	                						 .appendAttr("onMouseOut", "this.src='../images/../images/nextPage.gif';")
	                						 .appendAttr("onClick", strUrl)
	                						 .appendNL('>');
	                
	                getPageflow().getPage().s.appendNL("</td>");
	            }
	            
	            /**
	             * Restore page to original value as we do not want any ref buttons at the bottom
	             * of the list page to point to the next or previous page!!!!
	             */
	            getPageflow().getQs().setEntry("-pg", intPage+"");
	            
	            getPageflow().getPage().s.appendNL("</tr>");
	            getPageflow().getPage().s.appendNL("</table>");
	            
	            /**
	             * End for the message
	             * DGS09MAR2004: Can switch off counts as in list forms
	             */
	            if (!hasTag("zXListFormNoCount")) {
	                if (intActualUpdateRowCount == 0 && intPage == 1) {
	                    /**
	                     * No results found
	                     */
	                    getPageflow().getPage().infoMsg("No matches found");
	                } else if (!blnMorePages && intPage == 1) {
	                    getPageflow().getPage().softMsg(intActualUpdateRowCount + " row" + (intActualUpdateRowCount == 1?"":"s") + " displayed");
	                    
	                } else {
	                    getPageflow().getPage().softMsg("Page " + intPage + " displayed, records " + (1 + (intPage - 1) * intMaxResultRows) + " to "  +  ((intPage - 1) * intMaxResultRows + intActualUpdateRowCount));
	                }
	                
	            }
	            
	        } else {
	            
	            /**
	             * DGS09MAR2004: Can switch off counts as in list forms
	             */
	            if (!hasTag("zXListFormNoCount")) {
	                if (intActualUpdateRowCount == 0) {
	                    /**
	                     * No results found
	                     */
	                    getPageflow().getPage().infoMsg("No matches found");
	                    /**
	                     * 1.5:40 BD/DGS14JUL2005: Was referencing objRS.EOF but has already been closed - already
	                     * have a boolean 'blnMorePages' to represent this, just needs using here too
	                     */
	                } else if (intActualUpdateRowCount >= intMaxResultRows && blnMorePages) {
	                    getPageflow().getPage().softMsg("Resultset truncated, " + intActualUpdateRowCount + " existing rows displayed");
	                } else {
	                    getPageflow().getPage().softMsg(intActualUpdateRowCount + " existing row" + (intActualUpdateRowCount == 1?"":"s") +" displayed");
	                }
	            }
	            
	        } // Paging
	        		    
		    /**
		     * Handle buttons
		     */
	        getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftList);
	        
	        getPageflow().processFormButtons(this);
	        
	        getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftList);
		    
	        if (!getPageflow().isOwnForm() && !hasTag("zXNoFormEnd")) {
	            getPageflow().getPage().s.appendNL("</form>");
	        }
		    
	        /**
	         * Close page
	         */
	        getPageflow().getPage().s.appendNL("<script type=\"text/javascript\" language=\"JavaScript\">");
	        
	        if (!getPageflow().getQs().getEntryAsString("-err").equalsIgnoreCase(getName())) {
	        	/**
	        	 * Position cursor on the first field of the form.
	        	 */
        		getPageflow().getPage().s.appendNL("  zXSetCursorToFirstField();");
	        } else {
	            /**
	             * Position the cursor to where the error accurred
	             * all fields are visible
	             */
	            getPageflow().getPage().s.append("  zXSetCursorToFirstField('").append(strFirstError).appendNL("');");
	        }
	        
	        getPageflow().getPage().s.appendNL("</script>");
	        
	        /**
	         * Handle window title and footer
	         */
	        getPageflow().handleFooterAndTitle(this, "Edit Data");
	        
	        
		    getPageflow().setAction(getPageflow().resolveLink(this.getLink()));
		    
		    return go;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Execute the Grid Edit Pageflow", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    go = zXType.rc.rcError;
		    return go;
		} finally {
            /**
             * Restore values
             */
            if (StringUtil.len(strOrgSelectWidth ) > 0) {
                getPageflow().getPage().getWebSettings().setListFormColumn1(strOrgSelectWidth);
            }
            
            /**
             * Close resultset
             */
            if (objRS != null) objRS.RSClose();
            
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
        
        getDescriptor().xmlUrl("resorturl", getResorturl());
        getDescriptor().xmlUrl("pagingurl", getPagingurl());
    }
    
    /**
     * @see PFAction#clone(Pageflow)
     */
    public PFAction clone(Pageflow pobjPageflow) {
        PFGridEditForm cleanClone = (PFGridEditForm)super.clone(pobjPageflow);
        
        // Shared with PFListForm :
        if (getPagingurl() != null) {
            cleanClone.setPagingurl((PFUrl)getPagingurl().clone());
        }
        
        if (getResorturl() != null) {
            cleanClone.setResorturl((PFUrl)getResorturl().clone());
        }
        
        return cleanClone;
    }
}