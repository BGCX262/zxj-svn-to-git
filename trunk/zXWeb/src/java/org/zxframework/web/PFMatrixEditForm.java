/*
 * Created on Apr 12, 2004 by Michael Brewer
 * $Id: PFMatrixEditForm.java,v 1.1.2.27 2006/07/17 13:59:25 mike Exp $ 
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.Iterator;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;
import org.zxframework.web.PFUrl;

/**
 * Pageflow matrix edit form action object.
 * 
 * <pre>
 * 
 * NOTE : The persist status is now the name of the enum and not the position like in the COM+ version
 * NOTE : The beginning postion of a matrix edit element is 0. This is due to java arrays starting a 0.
 * 
 * Change    : DGS26FEB2004
 * Why       : Allow the use of PK where group when loading the cell BO
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
 * Change    : DGS11MAY2004
 * Why       : New frame type of 'simple ref'
 * 
 * Change    : BD5APR05 - V1.5:1
 * Why       : Added support for data-sources
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
public class PFMatrixEditForm extends PFAction {
    
    //------------------------ Members
    
	private String rowslinkedquery;
	private String colslinkedquery;
	private String rowsentity;
	private String colsentity;
	private String rowslabelswidth;
	private boolean colslabels;
	private String cellsentity;
	private boolean verticallystacked;
	private String deletewhen;
    
	//------------------------ Constructors
	
	/**
	 * Default constructor.
	 */
	public PFMatrixEditForm() {
	    super();
	}
	
	//------------------------ Getters/Setters
	
	/**
	 * @return Returns the colsentity.
	 */
	public String getColsentity() {
	    return colsentity;
	}
	
	/**
	 * @param colsentity The colsentity to set.
	 */
	public void setColsentity(String colsentity) {
	    this.colsentity = colsentity;
	}
	
	/**
	 * @return Returns the colslabels.
	 */
	public boolean isColslabels() {
	    return colslabels;
	}
	
	/**
	 * @param colslabels The colslabels to set.
	 */
	public void setColslabels(boolean colslabels) {
	    this.colslabels = colslabels;
	}
	
	/**
	 * @return Returns the colslinkedquery.
	 */
	public String getColslinkedquery() {
	    return colslinkedquery;
	}
	
	/**
	 * @param colslinkedquery The colslinkedquery to set.
	 */
	public void setColslinkedquery(String colslinkedquery) {
	    this.colslinkedquery = colslinkedquery;
	}
	
	/**
	 * @return Returns the cellsentity.
	 */
	public String getCellsentity() {
	    return cellsentity;
	}
	
	/**
	 * @param cellsentity The cellsentity to set.
	 */
	public void setCellsentity(String cellsentity) {
	    this.cellsentity = cellsentity;
	}
	
	/**
	 * @return Returns the deletewhen.
	 */
	public String getDeletewhen() {
	    return deletewhen;
	}
	
	/**
	 * @param deletewhen The deletewhen to set.
	 */
	public void setDeletewhen(String deletewhen) {
	    this.deletewhen = deletewhen;
	}
	
	/**
	 * @return Returns the rowsentity.
	 */
	public String getRowsentity() {
	    return rowsentity;
	}
	
	/**
	 * @param rowsentity The rowsentity to set.
	 */
	public void setRowsentity(String rowsentity) {
	    this.rowsentity = rowsentity;
	}
	
	/**
	 * @return Returns the rowslabelswidth.
	 */
	public String getRowslabelswidth() {
	    return rowslabelswidth;
	}
	
	/**
	 * @param rowslabelswidth The rowslabelswidth to set.
	 */
	public void setRowslabelswidth(String rowslabelswidth) {
	    this.rowslabelswidth = rowslabelswidth;
	}
	
	/**
	 * @return Returns the rowslinkedquery.
	 */
	public String getRowslinkedquery() {
	    return rowslinkedquery;
	}
	
	/**
	 * @param rowslinkedquery The rowslinkedquery to set.
	 */
	public void setRowslinkedquery(String rowslinkedquery) {
	    this.rowslinkedquery = rowslinkedquery;
	}
	
	/**
	 * @return Returns the verticallystacked.
	 */
	public boolean isVerticallystacked() {
	    return verticallystacked;
	}
	
	/**
	 * @param verticallystacked The verticallystacked to set.
	 */
	public void setVerticallystacked(boolean verticallystacked) {
	    this.verticallystacked = verticallystacked;
	}
	
	//------------------------ Digester helper methods.
	
	/**
	 * @deprecated Using BooleanConverter
	 * @param colslabels The colslabels to set.
	 */
	public void setColslabels(String colslabels) {
	    this.colslabels = StringUtil.booleanValue(colslabels);
	}
	
	/**
	 * @deprecated Using BooleanConverter
	 * @param verticallystacked The verticallystacked to set.
	 */
	public void setVerticallystacked(String verticallystacked) {
	    this.verticallystacked = StringUtil.booleanValue(verticallystacked);
	}

    //------------------------ Private helper methods
    
    /**
     * Determine what the URL should be for this row (a popup). 
     * 
     * <pre>
     * 
     * Note that this function is similar
     * to the normal constructRowURL but uses the given URL rather than that of the listform itself.
     * We use the entity object to get entity name and PK, and we use the URL object to get the
     * main url (this has come from a popup ref url, not the listform's url).
     * </pre>
     *
     * @param pobjEntity The pageflow entity to display the link of. 
     * @param pobjUrl The url to use 
     * @param pstrConfirm The confirm message to use. Optional. default is null 
     * @return Returns url for a popup url.
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
     * @param pobjEntity The pageflow entity. 
     * @param pstrID The id of the row. 
     * @return Returns the url for a single row record.
     * @throws ZXException Thrown if constructRowURL fails. 
     */
    private String constructRowURL(PFUrl pobjUrl, 
    							   PFEntity pobjEntity, 
    							   String pstrID) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
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
                 * Note that we use constructUrl here, not constructRowURL, because we don't want the
                 * entity and pk appending to the url, so the standard function is correct.
                 */
                String strPopupName = pobjUrl.getUrl();
                pobjUrl.setUrl(StringEscapeUtils.escapeHTMLTag(getName()) + strPopupName + "_" + pstrID);
                
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
                        if (blnStartMenu) {
                            blnStartMenu = false;
                            
                            /**
                             * Start new entire popup menu:
                             */
                            getPageflow().getPage().popupMenuStart(pobjUrl.getUrl());
                            
                            /**
                             * Popup menu name (in url) has already been suffixed by PK to keep it unique
                             */
                            getPageflow().getPage().popupMenuOption(pobjUrl.getUrl(), 
                            										getPageflow().resolveLabel(objRef.getLabel()),
                            										constructPopupRowURL(pobjEntity, 
                            															 objRef.getUrl(), 
                            															 getPageflow().resolveLabel(objRef.getConfirm())), 
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
                
                /**
                 * The framenos fixed and 0 have a special meaning, the indicate that
                 * the URL should NOT be tampered with.
                 * DGS11MAY2004: Don't append QS for simple ref either
                 */
                String strFrameno = getPageflow().resolveDirector(pobjUrl.getFrameno());
                
                if (strFrameno.equalsIgnoreCase("0") 
                        		|| strFrameno.equalsIgnoreCase("notouch") 
                        		|| strFrameno.equalsIgnoreCase("simpleref")) {
                    constructRowURL = getPageflow().constructURL(pobjUrl);
                } else {
                    constructRowURL = getPageflow().constructURL(pobjUrl);
                }
                
                constructRowURL = getPageflow().wrapRefUrl(strFrameno, 
                        								   constructRowURL,
                        								   "",
                        								   false,false);
            } // popup yes / no
            
            return constructRowURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine what the URL should be for this row.", e);
            if (getZx().log.isErrorEnabled()) {
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
     * Handle this action.
     * 
     * Reviewed for V1.5:65
     * 
     * @see PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
		if(getZx().trace.isApplicationTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
		zXType.rc go = zXType.rc.rcOK;
		int intRC = zXType.rc.rcOK.pos;
        
        DSRS objRS = null;
        String strOrgSelectWidth = null;
        
		try {
		    
		    /**
		     * Determine how progress stored in the querystring
		     */
		    String strQSProgress = (StringUtil.len(getQsprogress()) == 0 ? "-prgs" : getQsprogress());
		    
		    /**
		     * Check if we are already in progress by looking at the prgs entry in
		     * the querystring; but only when this matches my name!
		     */
		    boolean blnInProgress = getPageflow().getQs().getEntryAsString(strQSProgress).equalsIgnoreCase(getName());
		    
		    /**
		     * Save name of attribute last in error
		     */
		    String strAttr = getZx().trace.getUserErrorAttr();
		    
		    /**
		     * Get entity collection and store in context. However: if the subaction is
		     * error, it means that this routine is called directly after a
		     * Create Update 'go' that detected input errors. In that case, we
		     * will use the entity collection from the context (ie the one that has
		     * been created by the CreateUpdate go routine) because otherwise
		     * we will create new business objects and thus overwrite any input that
		     * the user has already done
		     */
		    ZXCollection colEntities = getPageflow().getEntityCollection(this,
                                                                         zXType.pageflowActionType.patEditForm, 
		            													 zXType.pageflowQueryType.pqtSearchForm);
		    if (colEntities == null) {
		        throw new Exception("Unable to retrieve entity collection");
		    }
            
		    /**
		     * Handle any parameter-bag URL entries
		     */
		    PFUrl objUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getUrl());
		    
		    /**
		     * Handle edit enhancers - now done in called function
		     */
		    if (!getPageflow().handleEnhancers(getEditenhancers(), colEntities).equals(zXType.rc.rcOK)) {
		        throw new Exception("Failed to handle the enhancers for the matrix edit form.");
		    }
		    
		    /**
		     * Set context variable
		     */
		    getPageflow().setContextEntities(colEntities);
		    getPageflow().setContextEntity((PFEntity)colEntities.iterator().next());
		    
		    /**
		     * Page title
		     */
		    if (getTitle().size() > 0) {
		        getPageflow().getPage().formTitle(zXType.webFormType.wftEdit, getPageflow().resolveLabel(getTitle()));
		    }
		    
		    /**
		     * Handle any outstanding messages
		     */
		    getPageflow().processMessages(this);
		    
		    /**
		     * Check if there should be a select button
		     */
		    boolean blnSelect;
		    if (objUrl == null) {
		        blnSelect = false;
		    } else {
		        blnSelect = (StringUtil.len(objUrl.getUrl()) > 0 && !objUrl.getUrl().equalsIgnoreCase("#dummy"));
		    }
		    
		    /**
		     * Make sure that the form header is not be generated
		     * if the calling application handles it
		     */
		    String strUrl = "";
		    if (!getPageflow().isOwnForm() && !hasTag("zXNoFormStart")) {
		        /**
		         * Construct formaction URL
		         */
		        strUrl = getPageflow().constructURL(getFormaction());
		        
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
		        getPageflow().getQs().setEntry("-wrn", "");
		    }
		    
            //--------------
            // Begin of COLS
            //--------------
		    /**
		     * Get columns query and print as debug if needed
		     */
		    PFAction objQueryAction = getPageflow().getPFDesc().getAction(getColslinkedquery());
		    if (objQueryAction == null) {
		        throw new Exception("Unable to retrieve columns query action : " + getColslinkedquery());
		    }
		    
		    ZXCollection colColEntities = getPageflow().getEntityCollection(objQueryAction, 
		            												   zXType.pageflowActionType.patListForm, 
		            												   zXType.pageflowQueryType.pqtAll);
		    if (colColEntities == null) {
		        throw new Exception("Unable to retrieve columns entity collection");
		    }
		    
            /**
             * See if we have not broken any rules
             */
            if (!getPageflow().validDataSourceEntities(colColEntities)) {
                throw new ZXException("Unsupported combination of data-sources for columns");
            }
            
            PFEntity objColEntity = (PFEntity)colColEntities.iterator().next();
            DSHandler objColDSHandler = objColEntity.getDSHandler();
            boolean blnIsColDSChannel = objColDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            String strColQuery = "";
            String strColQueryName = getPageflow().resolveDirector(objQueryAction.getQueryname());
            
            // Query elements for Col channel
            String strColSelectClause; // ???
            String strColWhereClause = "";
            String strColOrderByClause = "";
            boolean blnColReverse = false;
            
            if (blnIsColDSChannel) {
                strColSelectClause = getPageflow().retrieveQuerySelectClause(strColQueryName);
                strColWhereClause = getPageflow().retrieveQueryWhereClause(strColQueryName);
                strColOrderByClause = getPageflow().retrieveQueryOrderByClause(strColQueryName);
                
                /**
                 * When the order by clause was generated as a result of the query action (eg from search form),
                 * it may start with a - that indicatesd DESC
                 */
                if (StringUtil.len(strColOrderByClause) > 1 && strColOrderByClause.charAt(0) == '-') {
                    strColOrderByClause = strColOrderByClause.substring(1);
                    blnColReverse = true;
                }
                
                /**
                 * Clause not stored with leading ':'
                 */
                if (StringUtil.len(strColWhereClause) > 0) strColWhereClause  = ":" + strColWhereClause;
                
                if (getPageflow().isDebugOn()) {
                    getPageflow().getPage().debugMsg("Cols: " + objColEntity.getName() + "." + strColSelectClause);
                    getPageflow().getPage().debugMsg("Where " + strColWhereClause);
                    if (StringUtil.len(strColOrderByClause) > 0) getPageflow().getPage().debugMsg("Order by " + strColOrderByClause);
                }
                
            } else {
                strColQuery = getPageflow().retrieveSQL(getPageflow().resolveDirector(strColQueryName));
                
                if (getPageflow().isDebugOn()) {
                    getPageflow().getPage().debugMsg("Cols: " + strColQuery);
                }
                
            } // Channel or RDBMS
            
            //-------------
            // End of COLS
            //-------------
            
            //--------------
            // Begin of ROWS
            //--------------
		    /**
		     * Get rows query and print as debug if needed
		     */
		    objQueryAction = getPageflow().getPFDesc().getAction(getRowslinkedquery());
		    if (objQueryAction == null) {
		        throw new Exception("Unable to retrieve rows query action : " + getRowslinkedquery());
		    }
            
		    ZXCollection colRowEntities = getPageflow().getEntityCollection(objQueryAction, 
		            														zXType.pageflowActionType.patListForm,
		            														zXType.pageflowQueryType.pqtAll);
		    if (colRowEntities == null) {
		        throw new Exception("Unable to retrieve rows entity collection");
		    }
            
            /**
             * See if we have not broken any rules
             */
            if (!getPageflow().validDataSourceEntities(colRowEntities)) {
                throw new Exception("Unsupported combination of data-sources for rows");
            }
            
            PFEntity objRowEntity = (PFEntity)colRowEntities.iterator().next();
            DSHandler objRowDSHandler = objRowEntity.getDSHandler();
            boolean blnIsRowDSChannel = objRowDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            String strRowQuery = "";
            String strRowQueryName = getPageflow().resolveDirector(objQueryAction.getQueryname());
            
            // Query elements for Row channel
            String strRowSelectClause; // ???
            String strRowWhereClause = "";
            String strRowOrderByClause = "";
            boolean blnRowReverse = false;
            
            if (blnIsRowDSChannel) {
                strRowSelectClause = getPageflow().retrieveQuerySelectClause(strRowQueryName);
                
                strRowWhereClause = getPageflow().retrieveQueryWhereClause(strRowQueryName);
                strRowOrderByClause = getPageflow().retrieveQueryOrderByClause(strRowQueryName);
                
                /**
                 * When the order by clause was generated as a result of the query action (eg from search form),
                 * it may start with a - that indicatesd DESC
                 */
                if (StringUtil.len(strRowOrderByClause) > 1 && strRowOrderByClause.charAt(0) == '-') {
                    strRowOrderByClause = strRowOrderByClause.substring(1);
                    blnRowReverse = true;
                }
                
                /**
                 * Clause not stored with leading ':'
                 */
                if (StringUtil.len(strRowWhereClause) > 0) strRowWhereClause  = ":" + strRowWhereClause;
                
                if (getPageflow().isDebugOn()) {
                    getPageflow().getPage().debugMsg("Cols: " + objRowEntity.getName() + "." + strRowSelectClause);
                    getPageflow().getPage().debugMsg("Where " + strRowWhereClause);
                    if (StringUtil.len(strRowOrderByClause) > 0) getPageflow().getPage().debugMsg("Order by " + strRowOrderByClause);
                }
                
            } else {
                strRowQuery = getPageflow().retrieveSQL(strRowQueryName);
                
                if (getPageflow().isDebugOn()) {
                    getPageflow().getPage().debugMsg(strRowQuery);
                }
                
            } // Channel or RDBMS
            
            //------------
            // End of ROWS
            //------------
            
		    /**
		     * Determine the maximum number of rows to display
		     */
		    int intMaxResultRows = 0;
		    if (getMaxrows() > 0) {
		        intMaxResultRows = getMaxrows();
		    } else {
		        if (getPageflow().getPage().getWebSettings().getMaxListRows() > 0 ) {
		            intMaxResultRows = getPageflow().getPage().getWebSettings().getMaxListRows();
		        } else {
		            intMaxResultRows = 150;
		        }
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
		     * Open table
		     */
		    getPageflow().getPage().s.append("<table ")
		    				.appendAttr("width", (StringUtil.len(getWidth()) > 0 ? getWidth() :  "100%")) 
		    				.appendNL('>');
		    
		    boolean blnShowSelectLabel = false;
		    String strSelectLabel = "";
            
		    if (blnSelect) {
		        /**
		         * The label for the select button column may have been overridden
		         * by the tag zXSelectLabel
		         * Here in the Matrix we differ from a normal list form in that we default
		         * to not showing any label for the search column, not even 'select', unless
		         * something is explicitly present in the tag.
		         * The width of the select button column can be overruled by the tag
		         * zXSelectWidth
		         */
		        String strTag = tagValue("zXSelectLabel");
		        if (StringUtil.len(strTag) > 0) {
		            strSelectLabel = strTag;
		            blnShowSelectLabel = true;
		        }
		        
		        strTag = tagValue("zXSelectWidth");
		        if (StringUtil.len(strTag) > 0) {
		            strOrgSelectWidth = getPageflow().getPage().getWebSettings().getListFormColumn1();
		            getPageflow().getPage().getWebSettings().setListFormColumn1(strTag);
		        }
                
		    } // Has select column
		    
		    /**
		     * Column Headings start here
		     */
		    getPageflow().getPage().matrixRowOpen(true, "zXLabelPlain");
		    
		    getPageflow().getPage().matrixAxisOpen(getRowslabelswidth());
		    getPageflow().getPage().listHeaderOpen(blnShowSelectLabel, strSelectLabel);
		    
            if (blnIsColDSChannel) {
                objRS = objColDSHandler.boRS(objColEntity.getBo(),
                                             objColEntity.getSelectlistgroup(),
                                             strColWhereClause,
                                             isResolvefk(),
                                             strColOrderByClause, blnColReverse,
                                             lngStartRow, lngBatchSize);
            } else {
                objRS = ((DSHRdbms)objColDSHandler).sqlRS(strColQuery,
                                                          lngStartRow, lngBatchSize);
                
            }
            
		    if (objRS == null) {
		        throw new Exception("Unable to execute columns query");
		    }
		    
		    getPageflow().getPage().matrixAxisClose();
		    
		    ArrayList astrColPK = new ArrayList();
		    int intColCount = 0;
		    Iterator iter;
		    PFEntity objEntity;
		    ZXBO objIBOColEntity = null;
		    String strColPK;
		    
		    while (!objRS.eof()) {
		        iter = colColEntities.iterator();
		        while (iter.hasNext()) {
		            objEntity = (PFEntity)iter.next();
		            
		            objRS.rs2obj(objEntity.getBo(), objEntity.getSelectlistgroup(), isResolvefk());
		            
		            if (getPageflow().resolveDirector(getColsentity()).equalsIgnoreCase(objEntity.getName())) {
		                
		                objIBOColEntity = objEntity.getBo();
		                strColPK = objIBOColEntity.getPKValue().getStringValue();
		                astrColPK.add(intColCount, strColPK);
		                
		                /**
		                 * Create a hidden field to hold the column PK
		                 */
		                getPageflow().getPage().s.append("<input type=\"hidden\" ")
	            						.appendAttr("name", "zXMatrixEditFormColPK" + intColCount + getName())
	            						.appendAttr("value", strColPK)
	            						.appendNL('>');
		            }
		            
	                getPageflow().getPage().matrixHeader(colColEntities, isResolvefk());
		        }
		        
		        intColCount++;
                
                objRS.moveNext();
		    }
            
		    objRS.RSClose();
		    objRS = null;
            
		    getPageflow().getPage().matrixRowClose();
		    
		    /**
		     * Column Headings end here.
		     * Do we display labels beneath each column heading?
		     */
		    String strHeadingLabels = "";
		    AttributeCollection colAttrr;
		    Iterator iterAttr;
		    Attribute objAttr;
		    
		    if (isColslabels()) {
		        if (isVerticallystacked()) {
		            strHeadingLabels = "<td><table width = '100%' class='zxMatrixLabel'>";
		        }
		        
		        iter = colEntities.iterator();
		        while (iter.hasNext()) {
		            objEntity = (PFEntity)iter.next();
		            
		            colAttrr = objEntity.getBo().getDescriptor().getGroup(objEntity.getVisiblegroup());
		            iterAttr = colAttrr.iterator();
		            while (iterAttr.hasNext()) {
		                objAttr = (Attribute)iterAttr.next();
		                
		                if (isVerticallystacked()) {
		                    strHeadingLabels = strHeadingLabels + "<tr><td align ='right'>" + objAttr.getLabel().getLabel() + "</td></tr>";
		                } else {
		                    strHeadingLabels = strHeadingLabels + "<td>" + objAttr.getLabel().getLabel() + "</td>";
		                }
		            }
		        }
		        
		        if (isVerticallystacked()) {
		            /**
		             * If vertically stacked fields will show labels to left of each row
		             */
		            strHeadingLabels = strHeadingLabels + "</table></td>";
		            
		        } else {
		            
		            /**
		             * Otherwise fields are horizontally stacked fields and labels are shown now above each field
		             * in each column
		             */
		            getPageflow().getPage().matrixRowOpen(true, "zXLabelPlain");
		            getPageflow().getPage().matrixAxisOpen(getRowslabelswidth());
		            
		            /**
		             * The empty cell top left, above the row labels:
		             */
		            getPageflow().getPage().s.append("<tr><td></td>");
		            getPageflow().getPage().matrixAxisClose();
		            
		            for (int i = 0; i < intColCount; i++) {
		                /**
		                 * Repeat the column labels beneath each column heading.
		                 */
		                getPageflow().getPage().matrixCellOpen(true, "zxMatrixLabel");
		                getPageflow().getPage().s.append(strHeadingLabels);
		                getPageflow().getPage().matrixCellClose();
                    }
		            getPageflow().getPage().matrixRowClose();
		        }
		    }
		    
            if (blnIsRowDSChannel) {
                objRS = objRowDSHandler.boRS(objRowEntity.getBo(),
                        				 	 objRowEntity.getSelectlistgroup(),
                        				 	 strRowWhereClause,
                        				 	 isResolvefk(),
                        				 	 strRowOrderByClause, blnRowReverse,
                        				 	 lngStartRow, lngBatchSize);
                
            } else {
                objRS = ((DSHRdbms)objRowDSHandler).sqlRS(strRowQuery,
                                                          lngStartRow, lngBatchSize);
                
            }
		    if (objRS == null) {
		        throw new Exception("Unable to execute query");
		    }
		    
		    /**
		     * Repeating rows data starts here
		     */
		    String strRowPKAttr = "";
		    String strRowPK = "";
		    String strColPKAttr = "";
		    boolean blnDidOpen = false;
		    String strFirstError = "";
		    boolean blnOdd = false;
		    
		    ZXBO objIBO;
		    ZXBO objIBORowEntity = null;
		    
		    int intRowCount = 0;
		    while (!objRS.eof() && intRowCount <= intMaxResultRows) {
		        /**
		         * Populate all the business objects...
		         */
		        iter = colRowEntities.iterator();
		        while (iter.hasNext()) {
		            objEntity = (PFEntity)iter.next();
		            
		            objRS.rs2obj(objEntity.getBo(), objEntity.getSelectlistgroup(), isResolvefk());
		            
		            if (getPageflow().resolveDirector(getRowsentity()).equalsIgnoreCase(objEntity.getName())) {
		                objIBORowEntity = objEntity.getBo();
		                strRowPK = objIBORowEntity.getPKValue().getStringValue();
		            }
                    
		        } // Loop over row entities
		        
		        /**
		         * Maybe the row is not 'active' at all
		         */
		        if (objUrl != null && getPageflow().isActive(objUrl.getActive())) {
		            
		            /**
		             * We need this because when we have multiple entities, we still
		             * only want to generate the open i.HTML once and only once
		             */
		            blnDidOpen = false;
		            
		            /**
		             * Determine whether we want to add a special class
		             */
		            String strClass = getPageflow().resolveDirector(getClazz());
		            if (StringUtil.len(strClass) > 0) {
		                if (isAddparitytoclass()) {
		                    if (blnOdd) {
		                        strClass = strClass + "Odd";
		                    } else {
		                        strClass = strClass + "Even";
		                    }
		                }
		            }
		            
		            getPageflow().getPage().matrixRowOpen(blnOdd, strClass);
		            
		            getPageflow().getPage().matrixAxisOpen(getRowslabelswidth());
		            
		            /**
		             * Create a hidden field to hold the row PK
		             */
		            getPageflow().getPage().s.append("<input type=\"hidden\" ")
				                    .appendAttr("name", "zXMatrixEditFormRowPK" + intRowCount + getName())
				                    .appendAttr("value", strRowPK)
				                    .appendNL('>');
		            
		            /**
		             * Insert anchor - NOTE : This is not seem possibe :(.
		             */
		            String strPK = "";
		            if (strRowPK.equals(strPK)) {
		                getPageflow().getPage().s.appendNL("<A name='zXActiveRow' id='zXActiveRow'></A>");
		            }
		            
		            iter = colRowEntities.iterator();
		            while (iter.hasNext()) {
		                objEntity = (PFEntity)iter.next();
		                
		                if (StringUtil.len(objEntity.getListgroup()) > 0) {
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
			                        strUrl = constructRowURL(objUrl, objEntity, intRowCount + "");  
			                    } else {
			                        strUrl = "";
			                    }
			                    
			                    getPageflow().getPage().listRowOpen(blnOdd, strUrl, strClass);
                                
			                } // Has already done row open sequence
			                
			                /**
			                 * Generate the row label for this row entity
			                 */
			                getPageflow().getPage().matrixRow(objEntity.getBo(), objEntity.getListgroup());
			                
		                }
		                
		            } // Loop over entities
		            
		            /***
		             * If we show field labels, and the fields are vertically stacked, we need
		             * to show the labels here in the left axis, also vertically stacked
		             */
		            if (isColslabels() && isVerticallystacked()) {
		                getPageflow().getPage().s.append(strHeadingLabels);
		            }
		            
		            /**
		             * Close the labels
		             */
		            getPageflow().getPage().matrixAxisClose();
		            
		            /**
		             * Display the cells
		             */
		            loopcols : for (int intColCountSoFar = 0; intColCountSoFar < intColCount; intColCountSoFar++) {
		                // Need to improve this :(
		                String strRowColId = StringUtil.right(String.valueOf(intRowCount),'0', 5) + StringUtil.right(String.valueOf(intColCountSoFar),'0', 5);
		                String strClassToUse = strClass;
		                
		                if (blnInProgress) {
		                    /**
		                     * Here we are 'in progress' which means something was wrong in the create update
		                     * and we are redisplaying entered data. An object will have been put into the object
		                     * model (OM) of each business object in error. If that object is present in any
		                     * entity on this row, use an error class to highlight the cell.
		                     */
		                    iter = colEntities.iterator();
		                    while (iter.hasNext()) {
		                        objEntity = (PFEntity)iter.next();
		                        
		                        objIBO = (ZXBO)objEntity.getBOSavers().get(strRowColId);
		                        if (objIBO.getOM().get("zXMatrixErr") != null) {
		                            strClassToUse = "zxErrorCell";
		                            break loopcols; // We have an error. This might be coz in the submit.
		                        }
		                    }
		                }
		                
		                getPageflow().getPage().matrixCellOpen(blnOdd, strClassToUse);
		                iter = colEntities.iterator();
		                loopentities : while (iter.hasNext()) {
		                    objEntity = (PFEntity)iter.next();
                            
		                    if (blnInProgress) {
		                        objIBO = (ZXBO)objEntity.getBOSavers().get(strRowColId);
		                    } else {
		                        objIBO = objEntity.getBo();
		                    }
		                    
		                    /**
		                     * DGS26FEB2004: Allow any attribute values to be set to fixed values
		                     * before loading the BO e.g. other PK where group attrs.
		                     */
		                    getPageflow().processAttributeValues(objEntity);
		                    
		                    getPageflow().setContextEntity(objEntity);
		                    
		                    getZx().getBOContext().setEntry(objEntity.getName(), objIBO);
		                    
		                    objIBO.setValidate(false);
		                    
		                    strColPKAttr = objIBO.getFKAttr(objIBOColEntity).getName();
		                    strRowPKAttr = objIBO.getFKAttr(objIBORowEntity).getName();
		                    
		                    if (!blnInProgress) {
		                        /**
		                         * Load the BO where it has FKs pointing to the two entities
		                         * identified as the column and row entitities.
		                         */
		                        objIBO.setValue(strColPKAttr, (String)astrColPK.get(intColCountSoFar));
		                        objIBO.setValue(strRowPKAttr, strRowPK);
		                        
		                        /**
		                         * DGS26FEB2004: For the loadBO, if there is a PK where group defined for
		                         * the entity, use that. Otherwise use the two FKs that relate to the column
		                         * and the row. This allows flexibility where there might be other significant
		                         * attribute values set in the BO by the call to processAttributeValues above.
		                         * If using the PK where group, output a debug message just to alert a developer
		                         * who might be expecting the row and col attrs to be used.
		                         */
		                        String strPKWhereGroup = objEntity.getPkwheregroup();
		                        
		                        if (StringUtil.len(strPKWhereGroup) == 0) {
		                            strPKWhereGroup = strColPKAttr + "," + strRowPKAttr;
		                            
		                        } else {
		                            if (getPageflow().isDebugOn()) {
		                                getPageflow().getPage().debugMsg("Retrieving cells using entity PK where group " + strPKWhereGroup);
		                            }
		                            
		                        }
		                        
		                        intRC = objIBO.loadBO("*", strPKWhereGroup,isResolvefk()).pos;
		                        
		                        if (intRC == zXType.rc.rcError.pos) {
		                        	// GoTo errExit
		                        	go = zXType.rc.rcError;
		                        	return go;
		                        	
		                        } else if (intRC ==  zXType.rc.rcWarning.pos) {
		                            /**
		                             * A return of warning means no such row - so reset as new
		                             * 
		                             * DGS09MAR2004: Use new 'resetExplicitBO' function. This behaves just
		                             * like the classic 'resetBO' except that it does not set attrs to
		                             * default values based on datatype - only explicit default values
		                             * set within the BO XML are set.
		                             */
		                            objIBO.resetBO("*", false, true);
		                        }
                                
		                    } // Not in progress
		                    
		                    /**
		                     * Tweak the descriptor alias so that the editform generates a unique
		                     * field name per attr
		                     */
		                    String strStoreAlias = objIBO.getDescriptor().getAlias();
		                    objIBO.getDescriptor().setAlias(objEntity.getName() + getName() + "Mtrx" + strRowColId);
                            
		                    getPageflow().getPage().matrixEditForm(objIBO, objEntity.getSelecteditgroup(), 
										                           objEntity.getLockgroup(),
										                           objEntity.getVisiblegroup(),
										                           isVerticallystacked());
                            
		                    if (blnInProgress && StringUtil.len(strFirstError) == 0) {
		                        /**
		                         * Here we are 'in progress' which means something was wrong in the create update
		                         * and we are redisplaying entered data. An object will have been put into the object
		                         * model (OM) of each business object in error. If that object is present, and we haven't
		                         * yet come across the first error on the page, note the attr's control name now before
		                         * we tweak the descriptor alias back again.
		                         */
		                        if (objIBO.getOM().get("zXMatrixErr") != null) {
		                            strFirstError = getPageflow().getPage().controlName(objIBO, objIBO.getDescriptor().getAttribute(strAttr));
                                    /**
                                     * Set the alias back to original value to avoid any problems
                                     */
                                    objIBO.getDescriptor().setAlias(strStoreAlias);
                                    
		                            break loopentities;
		                        }
                                
		                    }
		                    
		                    /**
		                     * Set the alias back to original value to avoid any problems
		                     */
		                    objIBO.getDescriptor().setAlias(strStoreAlias);
		                    
		                } // Loop over entities
                        
		                getPageflow().getPage().matrixCellClose();
                        
                    } // Loop over columns
		            
		            getPageflow().getPage().matrixRowClose();
		            
		            /**
		             * Switch row color
		             */
		            blnOdd = !blnOdd;
		            
		        } // is active?
		        
		        /** 
                 * Iterate over the next entry in the resulset
                 */
		        objRS.moveNext();
                
                intRowCount++;
                
		    } // Loop over rows
		    
            // Do we have more rows?
            boolean blnEOF = objRS.eof();
            
            // Clean up resultset.
		    objRS.RSClose();
            objRS = null;
		    
		    getPageflow().getPage().s.appendNL("</table>");
		    
		    /**
		     * Create hidden fields to hold the counts of columns and rows
		     */
		    getPageflow().getPage().s.append("<input type=\"hidden\" ")
		    				.appendAttr("name", "zXMatrixEditFormRowCount" + getName())
		    				.appendAttr("value", intRowCount - 1  +"")
		    				.appendNL('>');
		    
		    getPageflow().getPage().s.append("<input type=\"hidden\" ")
		                    .appendAttr("name", "zXMatrixEditFormColCount" + getName())
		                    .appendAttr("value", intColCount + "")
		                    .appendNL('>');
		    
		    getPageflow().getPage().s.append("<input type=\"hidden\" ")
		                    .appendAttr("name", "zXMatrixEditFormRowPKAttr" + getName())
		                    .appendAttr("value", strRowPKAttr)
		                    .appendNL('>');
		    
		    getPageflow().getPage().s.append("<input type=\"hidden\" ")
				            .appendAttr("name", "zXMatrixEditFormColPKAttr" + getName())
				            .appendAttr("value", strColPKAttr)
				            .appendNL('>');
		    
		    /**
		     * DGS09MAR2004: Can switch off counts as in list forms
		     */
		    if (!hasTag("zXListFormNoCount")) {
		        getPageflow().getPage().softMsg( ( (blnEOF && intRowCount > intMaxResultRows) ? "Result rows truncated, ": "") + (intRowCount) + " row" + (intRowCount == 2 ? "" : "s") + " by " + (intColCount) + " column" + (intColCount == 1 ? "" : "s") + " displayed");
		    }
		    
		    /**
		     * Handle buttons
		     */
		    getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftList);
		    
		    if (!getPageflow().processFormButtons(this).equals(zXType.rc.rcOK)) {
		        throw new Exception("Unable to generate form buttons");
		    }
		    
		    getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftList);
		    
		    if (!getPageflow().isOwnForm() && !hasTag("zXNoFormEnd")) {
		        getPageflow().getPage().s.appendNL("</form>");
		    }
		    
		    getPageflow().getPage().s.appendNL("<script type=\"text/javascript\" language=\"JavaScript\">");
		    
		    if (getPageflow().getQs().getEntryAsString("-err").equalsIgnoreCase(getName()) 
		            		|| StringUtil.len(strAttr) == 0) {
		        getPageflow().getPage().s.appendNL("  zXSetCursorToFirstField();");
		    } else {
		        /**
		         * all fields are visible
		         */
		        getPageflow().getPage().s.appendNL("  zXSetCursorToFirstField('" + strFirstError + "');");
		    }
		    
		    getPageflow().getPage().s.appendNL("</script>");
		    
		    /**
		     * Handle window title and footer
		     */
		    getPageflow().handleFooterAndTitle(this, "Edit Data");
		    
		    getPageflow().setAction(getPageflow().resolveLink(getLink()));
		    
		    return go;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Execute the matrix edit form.", e);
	    	
		    if (getZx().throwException) throw new ZXException(e);
		    go = zXType.rc.rcError;
		    return go;
		} finally {
            /**
             * Restore original width.
             */
            if (StringUtil.len(strOrgSelectWidth) > 0) {
                getPageflow().getPage().getWebSettings().setListFormColumn1(strOrgSelectWidth);
            }
            
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
		
		/**
		 * DGS 06FEB2004: Added matrix edit form and create update actions
		 */
		objXMLGen.taggedCData("colslinkedquery", getColslinkedquery());
		objXMLGen.taggedCData("rowslinkedquery", getRowslinkedquery());
		objXMLGen.taggedCData("colsentity", getColsentity());
		objXMLGen.taggedCData("rowsentity", getRowsentity());
		objXMLGen.taggedCData("cellsentity", getCellsentity());
		objXMLGen.taggedCData("rowsentity", getRowsentity());
		objXMLGen.taggedValue("colslabels", isColslabels());
		objXMLGen.taggedValue("verticallystacked", isVerticallystacked());
		objXMLGen.taggedValue("rowslabelswidth", getRowslabelswidth());
		objXMLGen.taggedCData("deletewhen", getDeletewhen());
	}

	/** 
	 * @see PFAction#clone(Pageflow)
	 **/
	public PFAction clone(Pageflow pobjPageflow) {
	    PFMatrixEditForm cleanClone = (PFMatrixEditForm)super.clone(pobjPageflow);
	    
	    cleanClone.setRowslinkedquery(getRowslinkedquery());
	    cleanClone.setColslinkedquery(getColslinkedquery());
	    cleanClone.setRowsentity(getRowsentity());
	    cleanClone.setColsentity(getColsentity());
	    cleanClone.setRowslabelswidth(getRowslabelswidth());
	    cleanClone.setColslabels(isColslabels());
	    cleanClone.setCellsentity(getCellsentity());
	    cleanClone.setVerticallystacked(isVerticallystacked());
	    cleanClone.setDeletewhen(getDeletewhen());
	    
	    return cleanClone;
	}
}