/*
 * Created on Apr 12, 2004 by Michael Brewer
 * $Id: PFListForm.java,v 1.1.2.45 2006/07/17 16:28:43 mike Exp $ 
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.Tuple;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;
import org.zxframework.web.PFUrl;

/**
 * Pageflow List Form object.
 * 
 * <pre>
 * 
 * Change    : BD23DEC02
 * Why       : Change < to <= in check for maxrows so we actually display
 *             the maxrows number of rows rather than truncating at maxrows - 1
 * 
 * Change    : DGS22JAN2003
 * Why       : Can pre-check multilist checkboxes by setting a tag in the pageflow
 * 
 * Change    : BD19FEB03
 * Why       : Added support for class in list
 * 
 * Change    : BD24FEB03
 * Why       : Added support for active
 * 
 * Change    : DGS20FEB03
 * Why       : Added popup functionality; this required changes to 'go' and to 'constructRowUrl'
 *             including new code to generate javascript for a popup for each row if necessary,
 *             and a new function 'constructPopupRowURL' to generate the slightly different urls
 *             required for a popup.
 * 
 * Change    : BD28MAR03
 * Why       : - Make popup menus case insensitive
 *             - Added support for dialog / msgbox like popup windows
 * 
 * Change    : BD8APR03
 * Why       : Use revised version of pageflow.processRef
 * 
 * Change    : BD11APR03
 * Why       : Implemented paging on listform
 * 
 * Change    : BD16APR03
 * Why       : Added zXNoFormStart and zXNoFormEnd tags
 * 
 * Change    : BD28APR03
 * Why       : Introduced propagateQS feature
 * 
 * Change    : BD15MAY03
 * Why       : Fixed bug: forgot to implement support for noTouch frameno somewhere
 * 
 * Change    : BD18JUN03
 * Why       : Confirmation support in popup menus
 * 
 * Change    : BD27JUN03
 * Why       : Fixed problems with activeRow
 * 
 * Change    : DGS12FEB2004
 * Why       : Added multiple column ability, two new tags to support it.
 * 
 * Change    : BD16FEB04
 * Why       : Support for zXNoAutoPosition to stop auto positioning
 * 
 * Change    : BD24FEB04
 * Why       : Support of directors in zXNoFormStart and End
 * 
 * Change    : BD27FEB04
 * Why       : When generating popup menus, use the action name as well to
 *             make the name unique; this allows for combining different
 *             actions with popup menus on a single page
 * 
 * Change    : DGS02MAR2004
 * Why       : Support for zXListFormNoCount to inhibit row counts
 * 
 * Change    : DGS11MAY2004
 * Why       : New frame type of 'simple ref'
 * 
 * Change    : DGS09JUL2004
 * Why       : Don't page forward if at EOF (only happens when -pg QS left over by mistake)
 * 
 * Change    : MB15NOV2004
 * Why       : Terminate the list form straight after the list and not after the paging code.
 * 
 * Change    : BD24FEB04 - V1.4:49
 * Why       : Handle zXMultiCheck properly: since it is an expression it can be set
 *             unset per row; a bug now causes it to stay 'checked' once it
 *             is checked
 *             
 * Change    : BD31MAR05 - V1.5:1
 * Why       : Support for data-sources
 * 
 * Change    : V1.4:71 - DGS26APR2005
 * Why       : Handle new property that can force restriction of rows selected to the
 *             current page/screen full
 *
 * Change    : V1.4:78 - DGS13MAY2005
 * Why       : Above V1.4:49 introduced another bug such that the checkbox was unchecked
 *             when it shouldn't have been.
 *
 * Change    : V1.5:13 - BD16MAY05
 * Why       : The implementation of paging is now moved to the datasource handler rather than
 *             in the listForm
 * 
 * Change    : V1.4:71 (addendum)- DGS11JUL2005
 * Why       : Was wrongly setting start row to zero for all pages
 *
 * Change    : V1.5:60 - DGS19SEP2005
 * Why       : Introduced optional totalling
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
public class PFListForm extends PFAction {
    
    //------------------------ Members
    
    private String qsentity;
    private String qspk;
    private boolean autocheck;
    private boolean multilist;
    private boolean multicolumn;
    private int rowspercolumn;
    private PFUrl resorturl;
    private PFUrl pagingurl;
    
    /** C1.5:60 DGS19SEP2005: Totalling */
    private List totalgroups;
    private List totalgroupbys;
    private String totalrowclass;
    private String totalcellclass;
    private boolean grandtotal;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFListForm() {
        super();
        
        /**
         * Set the defaults :
         * -e = is the default for the parameter name of the entity for this list.
         * -pk = is the default for the parameter name of the primary key.
         */
        setQsentity("-e");
        setQspk("-pk");
    }
    
    //------------------------ Getters/Setters
    
    /**
     * Whether we want to auto check an item in the list based on the qspk.
     * 
     * @return Returns the autocheck.
     */
    public boolean isAutocheck() {
        return autocheck;
    }
    
    /**
     * @param autocheck The autocheck to set.
     */
    public void setAutocheck(boolean autocheck) {
        this.autocheck = autocheck;
    }
    
    /**
     * Whether this is a multicolumn list form.
     * 
     * @return Returns the multicolumn.
     */
    public boolean isMulticolumn() {
        return multicolumn;
    }
    
    /**
     * @param multicolumn The multicolumn to set.
     */
    public void setMulticolumn(boolean multicolumn) {
        this.multicolumn = multicolumn;
    }
    
    /**
     * Whether to generate check boxes for the list form.
     * See zXMany2Many pageflow.
     * 
     * @return Returns the multilist.
     */
    public boolean isMultilist() {
        return multilist;
    }
    
    /**
     * @param multilist The multilist to set.
     */
    public void setMultilist(boolean multilist) {
        this.multilist = multilist;
    }
    
    /**
     * The base url of the next previous links.
     * 
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
     * The parameter name to use to get the entity from PFQS.
     * 
     * @return Returns the qsentity.
     */
    public String getQsentity() {
        return qsentity;
    }
    
    /**
     * @param qsentity The qsentity to set.
     */
    public void setQsentity(String qsentity) {
        this.qsentity = qsentity;
    }
    
    /**
     * The parameter name to use to get the primarty key of the entity from PFQ.
     * 
     * @return Returns the qspk.
     */
    public String getQspk() {
        return qspk;
    }
    
    /**
     * @param qspk The qspk to set.
     */
    public void setQspk(String qspk) {
        this.qspk = qspk;
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
    
    /**
     * Number of rows before creating a new column.
     * 
     * @return Returns the rowspercolumn.
     */
    public int getRowspercolumn() {
        return rowspercolumn;
    }
    
    /**
     * @param rowspercolumn The rowspercolumn to set.
     */
    public void setRowspercolumn(int rowspercolumn) {
        this.rowspercolumn = rowspercolumn;
    }
    
    /**
	 * @return Returns the grandtotal.
	 */
	public boolean isGrandtotal() {
		return grandtotal;
	}

	/**
	 * @param grandtotal The grandtotal to set.
	 */
	public void setGrandtotal(boolean grandtotal) {
		this.grandtotal = grandtotal;
	}

	/**
	 * @return Returns the totalcellclass.
	 */
	public String getTotalcellclass() {
		return totalcellclass;
	}

	/**
	 * @param totalcellclass The totalcellclass to set.
	 */
	public void setTotalcellclass(String totalcellclass) {
		this.totalcellclass = totalcellclass;
	}

	/**
	 * @return Returns the totalgroupbys.
	 */
	public List getTotalgroupbys() {
		return totalgroupbys;
	}

	/**
	 * @param totalgroupbys The totalgroupbys to set.
	 */
	public void setTotalgroupbys(List totalgroupbys) {
		this.totalgroupbys = totalgroupbys;
	}

	/**
	 * @return Returns the totalgroups.
	 */
	public List getTotalgroups() {
		return totalgroups;
	}

	/**
	 * @param totalgroups The totalgroups to set.
	 */
	public void setTotalgroups(List totalgroups) {
		this.totalgroups = totalgroups;
	}

	/**
	 * @return Returns the totalrowclass.
	 */
	public String getTotalrowclass() {
		return totalrowclass;
	}

	/**
	 * @param totalrowclass The totalrowclass to set.
	 */
	public void setTotalrowclass(String totalrowclass) {
		this.totalrowclass = totalrowclass;
	}
	
    //------------------------ Digester helper methods.
    
    /**
     * @deprecated Using BooleanConverter
     * @param autocheck The autocheck to set.
     */
    public void setAutocheck(String autocheck) {
        this.autocheck = StringUtil.booleanValue(autocheck);
    }
    
    /**
     * @deprecated Using BooleanConverter
     * @param multicolumn The multicolumn to set.
     */
    public void setMulticolumn(String multicolumn) {
        this.multicolumn = StringUtil.booleanValue(multicolumn);
    }
    
    /**
     * @deprecated Using BooleanConverter
     * @param multilist The multilist to set.
     */
    public void setMultilist(String multilist) {
        this.multilist = StringUtil.booleanValue(multilist);
    }
    
    /**
     * @deprecated Using BooleanConverter
     * @param grandTotal The grandTotal to set.
     */
    public void setGrandtotal(String grandTotal) {
        this.grandtotal = StringUtil.booleanValue(grandTotal);
    }
    
    //------------------------ Private Helper methods.
    
	/**
     * Constucts the column headings.
     * 
     * <pre>
     * 
     * DGS12FEB2004: New code for multiple columns, although is also called (once)
     *  if not multi column.
     * </pre>
     * 
	 * @param pobjResortUrl The resort url.
     * @param pblnSelect Whether to include the select column.
     * @param pstrLabel The label of the column 
     * @param pcolEntities The PFEntities used. 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if columnHeadings fails. 
     */
    private zXType.rc columnHeadings(PFUrl pobjResortUrl,
    								 boolean pblnSelect, 
    								 String pstrLabel, 
    								 Map pcolEntities) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pblnSelect", pblnSelect);
            getZx().trace.traceParam("pstrLabel", pstrLabel);
            getZx().trace.traceParam("pcolEntities", pcolEntities);
        }

        zXType.rc columnHeadings = zXType.rc.rcOK; 
        
        try {
            if (isMulticolumn()) {
                /**
                 * If multi-column listform open another cell for the next column
                 */
                getPageflow().getPage().s.appendNL("<td valign='top'>");
                getPageflow().getPage().s.appendNL("<table width='100%' id='listform'>");
            }
            
            if (isMultilist()) {
                getPageflow().getPage().multiListHeaderOpen(pblnSelect, pstrLabel);
            } else {
                getPageflow().getPage().listHeaderOpen(pblnSelect, pstrLabel);
            }
            
            PFEntity objEntity;
            
            Iterator iter = pcolEntities.values().iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                if (isMultilist()) {
                    getPageflow().getPage().multiListHeader(objEntity.getBo(), 
                                                            objEntity.getListgroup(), 
                                                            getPageflow().constructURL(pobjResortUrl), 
                                                            isResolvefk());
                } else {
                    getPageflow().getPage().listHeader(objEntity.getBo(), 
                                                       objEntity.getListgroup(), 
                                                       getPageflow().constructURL(pobjResortUrl), 
                                                       isResolvefk());
                }
            }
            
            if (isMultilist()) {
                getPageflow().getPage().multiListHeaderClose();
            } else {
                getPageflow().getPage().listHeaderClose();
            }
            
            /**
             * To prevent the re-sort icons appearing on the second and subsequent columns...
             * (bit of a cludge so don't do it unless we have to i.e. in a a multi column)
             */    
            if (isMulticolumn() && pobjResortUrl != null) {
            	pobjResortUrl.setUrl("");
            }
            
            return columnHeadings;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Constucts the column headings.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pblnSelect = "+ pblnSelect);
                getZx().log.error("Parameter : pstrLabel = "+ pstrLabel);
                getZx().log.error("Parameter : pcolEntities = "+ pcolEntities);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            columnHeadings = zXType.rc.rcError;
            return columnHeadings;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(columnHeadings);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Determine what the URL should be for this row.
     * 
     * <pre>
     * 
     * DGS28FEB2003: Changed to handle popup refs.
     * </pre>
     *
     * @param pobjListUrl The list url.
     * @param pobjEntity The entity of the select item. 
     * @param pintId The id of the entity 
     * @return Returns the URL for this row.
     * @throws ZXException Thrown if constructRowURL fails. 
     */
    private String constructRowURL(PFUrl pobjListUrl, PFEntity pobjEntity, int pintId) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjUrl", pobjListUrl);
            getZx().trace.traceParam("pobjEntity", pobjEntity);
            getZx().trace.traceParam("pstrId", pintId);
        }
        
        String constructRowURL = ""; 

        try {
            
            /**
             * If no URL for the list: we're done
             */
            if (pobjListUrl == null) {
                return constructRowURL;
            }
            
            if (pobjListUrl.getUrlType().equals(zXType.pageflowUrlType.putPopup)) {
                /**
                 * Url is appended by unique id. This is then undone after constructing the row URL
                 * and generating the popup (as the same url is used by all rows, not just this one).
                 * Note that we use constructUrl here, not constructRowURL, because we don't want the
                 * entity and pk appending to the url, so the standard function is correct.
                 */
                String strPopupName = pobjListUrl.getUrl();
                pobjListUrl.setUrl(StringEscapeUtils.escapeHTMLTag(this.getName()) + strPopupName + "_" + pintId);
                
                constructRowURL = getPageflow().constructURL(pobjListUrl);
                
                /**
                 * Now generate the popup menu javascript.
                 */
                boolean blnStartMenu =true;
                
                PFRef objRef;
                
                int intRefs = getPopups().size();
                for (int i = 0; i < intRefs; i++) {
                    objRef = (PFRef)getPopups().get(i);
                    
                    /**
                     * Only interested in popups for this name...
                     */
                    if (objRef.getName().equalsIgnoreCase(strPopupName)) {
                        
                        /**
                         *  ...that are active
                         */
                        if (getPageflow().isActive(objRef.getUrl().getActive())) {
                            
                            if (blnStartMenu) {
                                blnStartMenu = false;
                                
                                /**
                                 * Start new entire popup menu:
                                 */
                                getPageflow().getPage().popupMenuStart(pobjListUrl.getUrl());
                            }
                            
                            /**
                             * Popup menu name (in url) has already been suffixed by PK to keep it unique
                             */
                            getPageflow().getPage().popupMenuOption(pobjListUrl.getUrl(), 
                            										getPageflow().resolveLabel(objRef.getLabel()),
                            										constructPopupRowURL(pobjListUrl,
    				                                							 		 pobjEntity,
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
                pobjListUrl.setUrl(strPopupName);
                
            } else {
                /**
                 * The framenos fixed and 0 have a special meaning, the indicate that
                 * the URL should NOT be tampered with.
                 * DGS11MAY2004: simple ref should also not be tampered with
                 */
                String strFrameno = getPageflow().resolveDirector(pobjListUrl.getFrameno());
                if ("0".equals(strFrameno) || "notouch".equalsIgnoreCase(strFrameno)  || "simpleref".equalsIgnoreCase(strFrameno)) {
                	constructRowURL = getPageflow().constructURL(pobjListUrl);
                	
                } else {
                    /**
                     * In all other cases, we have to append the PK and the entity
                     */
                	
                    /**
                     * We normally use -pk and -e but this may have been overruled
                     */
                    if (getPageflow().getPFDesc().isPropagaTeqs()) {
                        getPageflow().getQs().setEntry(getQsentity(), pobjEntity.getBo().getDescriptor().getName());
                        getPageflow().getQs().setEntry(getQspk(), pobjEntity.getBo().getPKValue().getStringValue());
                        
                        constructRowURL = getPageflow().constructURL(pobjListUrl);
                    } else {
                        constructRowURL = getPageflow().appendToUrl(getPageflow().constructURL(pobjListUrl), 
                                		  							getQsentity() + "=" + pobjEntity.getBo().getDescriptor().getName() + 
                                		  							"&" + getQspk() + 
                                		  							"=" + pobjEntity.getBo().getPKValue().getStringValue());
                    }
                }
                
                constructRowURL = getPageflow().wrapRefUrl(strFrameno, 
                        								   constructRowURL, 
                        								   "", false, false);
            }
            
            return constructRowURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine what the URL should be for this row.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjUrl = " + pobjListUrl);
                getZx().log.error("Parameter : pobjEntity = " + pobjEntity);
                getZx().log.error("Parameter : pstrId = " + pintId);
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
    
    /**
     * Determine what the URL should be for this row (a popup). 
     * 
     * <pre>
     * Note that this function is similar to the normal constructRowURL but 
     * uses the given URL rather than that of the listform itself. We use the 
     * entity object to get entity name and PK, and we use the URL object to 
     * get the main url (this has come from a popup ref url, not the listform's url).
     * </pre>
     * 
     * @param pobjListUrl The list action url.
     * @param pobjEntity The pageflow entity the popup is for.
     * @param pobjUrl The url for the popup. 
     * @param pstrConfirm The confirm message. 
     * @return Returns a popup url.
     * @throws ZXException Thrown if constructPopupRowURL fails. 
     */
    private String constructPopupRowURL(PFUrl pobjListUrl,
    								 	PFEntity pobjEntity, 
    									PFUrl pobjUrl, 
    									String pstrConfirm) throws ZXException{
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
            
            /**
             * The framenos fixed and 0 have a special meaning, they indicate that
             * the URL should NOT be tampered with.
             * DGS11MAY2004: Don't append QS for simple ref either
             */
            String strFrameno = getPageflow().resolveDirector(pobjUrl.getFrameno());
            if ("0".equals(strFrameno) || "notouch".equalsIgnoreCase(strFrameno) 
                || "simpleref".equalsIgnoreCase(strFrameno)) {
                
                constructPopupRowURL = getPageflow().constructURL(pobjUrl);
                
            } else {
                /**
                 * In all other cases, we have to append the PK and the entity
                 */
                
                /**
                 * We normally use -pk and -e but this may have been overruled
                 *  Note the difference to normal constructRowURL, which uses the URL of
                 *  the listform. Here we use the given URL i.e. from one of the popup refs.
                 */
                if (getPageflow().getPFDesc().isPropagaTeqs()) {
                    getPageflow().getQs().setEntry(getQsentity(), pobjEntity.getBo().getDescriptor().getName());
                    getPageflow().getQs().setEntry(getQspk(), pobjEntity.getBo().getPKValue().getStringValue());
                    
                    constructPopupRowURL = getPageflow().constructURL(pobjUrl);
                    
                } else {
                    constructPopupRowURL = getPageflow().appendToUrl(getPageflow().constructURL(pobjListUrl), 
                            										 getQsentity() +
                            										 "=" + pobjEntity.getBo().getDescriptor().getName() + 
                            										 "&" + getQspk() +
                            										 "=" + pobjEntity.getBo().getPKValue().getStringValue());
                }
            }
            
            /**
             * Be smart and replace rRef with fRefx if frame x is requested
             * Only replace first instance
             * DGS28FEB2003: Again, don't do this for popups
             */
            constructPopupRowURL = getPageflow().wrapRefUrl(strFrameno, constructPopupRowURL, pstrConfirm, false, true);
            
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
     * Prints the totals.
     * 
     * v1.5:60 DGS19SEP2005: Optional totalling.
     * 
     * @param pcolAttrTags A collection used to keep track of the totals generated so far. 
     * @param pblnSelect select row boolean
     * @param pblnGrandTotal is grand total boolean
     * @param pcolEntities collection of entities
     * @return Returns the return code of the method.
     * @throws ZXException
     */
    private zXType.rc printTotals(AttributeTagCollection pcolAttrTags,
    		 					  boolean pblnSelect, 
    							  boolean pblnGrandTotal, 
    							  ZXCollection pcolEntities) throws ZXException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pblnSelect", pblnSelect);
            getZx().trace.traceParam("pblnGrandTotal", pblnGrandTotal);
            getZx().trace.traceParam("pcolEntities", pcolEntities);
        }
        
        zXType.rc printTotals = zXType.rc.rcOK;
        
        try {
        	String strTagName;
            if (pblnGrandTotal) {
            	strTagName = "zXGrandTotalCounter";
            } else {
            	strTagName = "zXTotalCounter";
            }
            
            boolean blnDidOpen = false;
            String strTmp;
            PFEntity objEntity;
            
            int intTotalGroups = 0;
            if (getTotalgroups() != null) {
            	intTotalGroups = getTotalgroups().size();
            }
            int intTotalGroupBys = 0;
            if (getTotalgroupbys() != null) {
            	getTotalgroupbys().size();
            }
            
            Iterator iterEntities = pcolEntities.iterator();
            while (iterEntities.hasNext()) {
            	objEntity = (PFEntity)iterEntities.next();
            	
            	if (StringUtil.len(objEntity.getListgroup()) > 0) {
            		if (!blnDidOpen) {
            			blnDidOpen = true;
            			
            			/**
            			 * Determine whether we want to use a special class for the total row
            			 */
            			strTmp = getPageflow().resolveDirector(getTotalrowclass());
                        if(StringUtil.len(strTmp) == 0) {
                        	strTmp = "zxTotalRow";
                        }
                        
            			/**
            			 * Open the list row
            			 */
            			getPageflow().getPage().listRowOpen(true, "", strTmp);
            			if (pblnSelect) {
            				getPageflow().getPage().s.appendNL("<td> </td>");
            			}
            			
            		} // Has already done row open sequence
            		
            		AttributeCollection colAttr = objEntity.getBODesc().getGroup(objEntity.getListgroup());
                    if (colAttr == null) {
                        getZx().trace.addError("In sub-totals, unable to get group", objEntity.getListgroup());
                        printTotals = zXType.rc.rcError;
                        return printTotals;
                    }
            		
                    /**
                     * Determine whether we want to use a special class for the total cells
                     */
                    strTmp = getPageflow().resolveDirector(getTotalcellclass());
                    if (StringUtil.len(strTmp) == 0) {
                    	strTmp = "zxTotalCell";
                    }
                    
                    boolean blnTotalAttr = false;
                	Tuple objTotalGroup;
                    AttributeCollection colAttrTotal;
                    Iterator iterAttrTotal;
                    Attribute objAttrTotal;
                    
                    Iterator iterAttr = colAttr.iterator();
                    while (iterAttr.hasNext()) {
                    	Attribute objAttr = (Attribute)iterAttr.next();
                    	
	                    /**
	                     * We need a cell for each attr in the normal list row group. Those
	                     * that are not totalled will be empty, but have to be present
	                     **/
                    	blnTotalAttr = false;
                    	
	                    /**
	                     * First see if this attr is a totalling attr:
	                     **/
                    	for (int i = 0; i < intTotalGroups; i++) {
                    		objTotalGroup = (Tuple)getTotalgroups().get(i);
                    		
	                        if (StringUtil.len(objTotalGroup.getName()) > 0 && StringUtil.len(objTotalGroup.getValue()) > 0) {
	                            if (objTotalGroup.getName().equalsIgnoreCase(objEntity.getName())) {
	                                colAttrTotal = objEntity.getBODesc().getGroup(objTotalGroup.getValue());
	                                if (colAttrTotal == null) {
	                                    getZx().trace.addError("In sub-totals, unable to get totals group", objTotalGroup.getValue());
	                                    printTotals = zXType.rc.rcError;
	                                    return printTotals;
	                                }
	                                
	                                iterAttrTotal = colAttrTotal.iterator();
	                                while (iterAttrTotal.hasNext()) {
	                                	objAttrTotal = (Attribute)iterAttrTotal.next();
	                                	
	                                    if (objAttr.getName().equals(objAttrTotal.getName())) {
	                                        blnTotalAttr = true;
	                                        
	                                        /**
	                                         * No point in continuing through remaining attrs in the total
	                                         * group as we have found the match
	                                         **/
	                                        break;
	                                    }
	                                }
	                            }
	                        }
	                        
	                        if (blnTotalAttr) {
	                            /**
	                             * No point in continuing through remaining attrs in the list group
	                             * as we have found the match
	                             **/
	                            break;
	                        }
                    	}
                    	
	                    if (blnTotalAttr) {
	                        /**
	                         * Create a property and set its value to the total, then get the
	                         * formatted value. That way we know it will be correctly displayed
	                         **/
	                    	String strValue = pcolAttrTags.getValue(objAttr.getName(), strTagName);
                        	//objEntity.getBo().setValue(objAttr.getName(), strValue);
                        	//strValue = objEntity.getBo().getValue(objAttr.getName()).formattedValue();
	                    	
	                    	if (objAttr.getDataType().pos == zXType.dataType.dtLong.pos || objAttr.getDataType().pos == zXType.dataType.dtAutomatic.pos) {
	                    		strValue = String.valueOf((int)Double.parseDouble(strValue));
	                    	}
	                    	
	                        getPageflow().getPage().s.appendNL("<td align=\"right\" class=\"" + strTmp + "\">" + strValue + "</td>");
	                        pcolAttrTags.setValue(objAttr.getName(), strTagName, null);
	                        
	                    } else {
	                        /**
	                         * Not a totalling attr - it might be a group by attr. We don't print these
	                         * in the grand totals:
	                         **/
	                        if (!pblnGrandTotal) {
	                        	for (int i = 0; i < intTotalGroupBys; i++) {
	                        		objTotalGroup = (Tuple)getTotalgroupbys().get(i);
	                        		
	                                if (StringUtil.len(objTotalGroup.getName()) > 0 && StringUtil.len(objTotalGroup.getValue()) > 0) {
	                                    if (objTotalGroup.getName().equalsIgnoreCase(objEntity.getName())) {
	                                        colAttrTotal = objEntity.getBODesc().getGroup(objTotalGroup.getValue());
	                                        if (colAttrTotal == null) {
	                                            getZx().trace.addError("Printing groupby attr, unable to get totals group", 
	                                            					   objTotalGroup.getValue());
	                                            printTotals = zXType.rc.rcError;
	                                            return printTotals;
	                                        }
	                                        
	                                        iterAttrTotal = colAttrTotal.iterator();
	                                        while (iterAttrTotal.hasNext()) {
	                                        	objAttrTotal = (Attribute)iterAttrTotal.next();
	                                        	
	                                            if (objAttr.getName().equals(objAttrTotal.getName())) {
	                                                blnTotalAttr = true;
	                                                
	                                                /**
	                                                 * No point in continuing through remaining attrs in the total
	                                                 * group as we have found the match
	                                                 **/
	                                                break;
	                                            }
	                                        }
	                                    }
	                                }
	                                
	                                if (blnTotalAttr) {
	                                    /**
	                                     * No point in continuing through remaining attrs in the list group
	                                     * as we have found the match
	                                     **/
	                                    break;
	                                }
	                        	}
	                        }
	                        
	                        if (blnTotalAttr) {
	                            /**
	                             * Create a property and set its value to the groupby latest value, then get the
	                             * formatted value. That way we know it will be correctly displayed
	                             **/
	                        	String strValue = pcolAttrTags.getValue(objAttr.getName(), "zXTotalGroupBy");
	                        	//objEntity.getBo().setValue(objAttr.getName(), strValue);
	                        	//strValue = objEntity.getBo().getValue(objAttr.getName()).formattedValue();
	                        	
	                            getPageflow().getPage().s.appendNL("<td align=\"left\" class=\"" + strTmp + "\">" + strValue + "</td>");
	                            pcolAttrTags.setValue(objAttr.getName(), strTagName, null);
	                            
	                        } else {
	                            /**
	                             * Neither a groupby nor a totalling attr - print a blank cell
	                             **/
	                            getPageflow().getPage().s.appendNL("<td> </td>");
	                            
	                        }
	                    }
                    } 
            	}
            }
            
        	getPageflow().getPage().listRowClose();
        	
        	return printTotals;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Print totals.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pblnSelect = " + pblnSelect);
                getZx().log.error("Parameter : pblnGrandTotal = " + pblnGrandTotal);
                getZx().log.error("Parameter : pcolEntities = " + pcolEntities);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            
            return printTotals;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(printTotals);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * A helper inner class for doing the totaling. This is used to
     * replace the attribute tags as this could coz problems with caching.
     * 
     * @author Michael Brewer
     */
    class AttributeTagCollection {
    	Map collection;
    	
    	AttributeTagCollection() {
    		collection = new HashMap();
    	}
    	
    	/**
    	 * Get a tag value for a attributte.
    	 * 
    	 * @param pstrAttrName The name of the attribute with a tag
    	 * @param pstrTagName The name of the tag to look up.
    	 * @return Returns the value if possible else a empty string.
    	 */
    	String getValue(String pstrAttrName, String pstrTagName) {
    		if (collection != null) {
    			Map col = (Map)collection.get(pstrAttrName);
    			if (col != null) {
    				String getValue = (String)col.get(pstrTagName);
    				if (getValue != null) {
    					return getValue;
    				}
    			}
    		}
    		return "";
    	}
    	
    	/**
    	 * Set the tag value for a attribute.
    	 * 
    	 * @param pstrAttrName The name of the attribute with a tag
    	 * @param pstrTagName The name of the tag to set
    	 * @param pstrTagValue The tag value.
    	 */
    	void setValue(String pstrAttrName, String pstrTagName, String pstrTagValue) {
    		if (collection == null) collection = new HashMap();
    		if (collection.get(pstrAttrName) == null) collection.put(pstrAttrName, new HashMap());
    		
    		Map col = (Map)collection.get(pstrAttrName);
    		
    		if (StringUtil.len(pstrTagValue) > 0) {
        		col.put(pstrTagName, pstrTagValue);
    		} else {
    			col.remove(pstrTagName);
    		}
    	}
    }
    
    //------------------------ Implemented Methods from PFAction
    
    /**
     * Process action.
     * 
     * Reviewed for V1.5:1 - Introduction of datasources
     * Reviewed for V1.5:63 - Added support for zXOneRowAutoSelect  tag
     * Reviewed for V1.5:65 - Added support for parameterBag
     * @see PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
    	if (getZx().trace.isApplicationTraceEnabled()) {
    		getZx().trace.enterMethod();
    	}
    	
        zXType.rc go = zXType.rc.rcOK;
        
        DSRS objRS = null;
        String strOrgSelectWidth = "";
        
        // Paging : This will allow for multiple listing forms on the same page and not have conflicting paging :
        String strPg = "-pg." + getName();
        
        /** Save last URL for zXOneRowAutoSelect feature */
        String strLastListUrl = "";
        /** Has zXOneRowAutoSelect been requested */
        boolean blnOnlyOneRow = false;
        
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
	        
	        /**
	         * Set context variable
	         */
	        getPageflow().setContextEntities(colEntities);
	        getPageflow().setContextEntity((PFEntity)colEntities.iterator().next());
            
            if (!getPageflow().validDataSourceEntities(colEntities)) {
                throw new Exception("Unsupported combination of data-source handlers");
            }
            
            DSHandler objDSHandler = getPageflow().getContextEntity().getDSHandler();
            boolean blnIsDSChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            /**
             * In case of a channel we can only have one entity, may as well retrieve this
             */
            PFEntity objTheEntity = null;
            if (blnIsDSChannel) {
                objTheEntity = getPageflow().getContextEntity();
            }
            
            /**
             * Handle any parameter-bag URL entries
             */
            PFUrl objListUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getUrl());
            PFUrl objPagingUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getPagingurl());
            PFUrl objResortUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getResorturl());
            
	        /**
	         * Create header
	         */
	        if (getTitle() != null) {
	            getPageflow().getPage().formTitle(zXType.webFormType.wftSearch, getPageflow().resolveLabel(getTitle()));
	        }
	        
	        /**
	         * Handle any messages
	         */
	        getPageflow().processMessages(this);
	        
	        /**
	         * If this list is a multilist it must contain a form; however we do not
	         * have a form if: ownForm has been set; noFormStart tag = 1
	         */
	        if (isMultilist() && !getPageflow().isOwnForm() 
	        	&& !"1".equals(getPageflow().resolveDirector(tagValue("zXNoFormStart")))  ) {
	            getPageflow().getPage().s.append("<form ")
	            	.appendAttr("method", "post")
	            	.appendAttr("action", getPageflow().constructURL(this.getFormaction()))
	            	.appendNL('>');
	        }
	        
	        /**
	         * Check if there should be a select button
	         */
	        boolean blnSelect;
	        if (objListUrl == null || objListUrl.getUrl() == null) {
	            blnSelect = false;
	        } else {
	            blnSelect = (!objListUrl.getUrl().equalsIgnoreCase("#dummy"));
	        }
	            
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
                
                if (getPageflow().isDebugOn() && objTheEntity != null) {
                    getPageflow().getPage().debugMsg(objTheEntity.getName() + "." + getPageflow().getContextEntity().getSelectlistgroup());
                    if (StringUtil.len(strWhereClause) > 0) {
                        getPageflow().getPage().debugMsg("Where " + strWhereClause);
                    }
                    if (StringUtil.len(strOrderByClause) > 0) {
                        getPageflow().getPage().debugMsg("Order by " + strOrderByClause);
                    }
                }
                
            } else {
                strQuery = getPageflow().retrieveSQL(strQueryName);
                // NOTE : This is NOT printing the fulll SQL query that has been messaged by the limit string
                if (getPageflow().isDebugOn()) {
                    getPageflow().getPage().debugMsg(strQuery);
                }
                
            } // Channel or RDBMS
            
	        /**
	         * Check whether paging is required
	         * 
	         * C1.4:71 DGS26APR2005: This moved from below so that page number is know
	         * before the SQL is generated.
	         */
            int intPage = 1;
	        boolean blnPaging = false;
	        
	        if (StringUtil.len(getPageflow().constructURL(objPagingUrl)) > 0) {
	            blnPaging = true;
	            
	            /**
	             * Figure out on what page we are and skip the records that we
	             * are not interested in
	             */
	            
                /**
                 * Get our current page.
                 */
                String strPage = getPageflow().getQs().getEntryAsString("-pg");
                if (StringUtil.len(strPage) > 0) {
                    // Clean up. otherwise the next listform will also move to this page.
                    getPageflow().getQs().removeEntry("-pg");
                } else {
                	/**
                	 * If we have multiple list forms the page number but be stored else where.
                	 */
                    strPage = getPageflow().getQs().getEntryAsString(strPg);
                }
                
                /**
                 * Figure out on what page we are and skip the records that we
                 * are not interested in
                 */
                if (StringUtil.len(strPage) > 0) {
                    intPage = Integer.parseInt(strPage);
                }
	        }
	        
	        /**
	         * Bit tricky: in HTML.listHeader we add -oa and -od to the
	         * URL; these may already be in the QS collection so will also
	         * be added by constructUrl so we have to take them out again
	         */
	        if (getPageflow().getPFDesc().isPropagaTeqs()) {
	            getPageflow().getQs().setEntry("-oa" + getPageflow().QSSortKeyPostFix(), null);
	            getPageflow().getQs().setEntry("-od" + getPageflow().QSSortKeyPostFix(), null);
	        }
	        
            /**
             * Determine the maximum number of rows to display
             */
            int intMaxResultRows;
            if (getMaxrows() > 0) {
                intMaxResultRows = getMaxrows();
                
            } else {
                if (getPageflow().getPage().getWebSettings().getMaxListRows() > 0) {
                    intMaxResultRows = getPageflow().getPage().getWebSettings().getMaxListRows();
                } else {
                    intMaxResultRows = 150;
                }
                
            } // Maxrows has been specified explicitly
            
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.trace("Maxrows set to " + intMaxResultRows);
            }
            
            /**
             * C1.4:71 DGS26APR2005: Might want to only retrieve as many rows as needed for the
             * current page. If we are not paging, intPage will be 1, and we will only select
             * as many rows as can be viewed before truncation.
             * 
             * Note that we retrieve one more than a page so that we know if there are any
             * subsequent pages
             */
            int lngStartRow = 0;
            int lngBatchSize = 0;
            if (isLimitrows()) {
                /**
                 * Implement the 'limit' if requested.
                 * 
                 * Note that we always ask the system for one more row than we really need so that we
                 * can give a message if the resultset is truncated
                 */
                lngBatchSize = intMaxResultRows + 1;
                
            } // Limit Rows?
            
        	if (blnPaging) {
        		/**
        		 * Only set the start row when paging.
        		 */
        		lngStartRow = intMaxResultRows * (intPage - 1) + 1;
        	}
            
	        /**
	         * Open table
	         */
	        getPageflow().getPage().s
                         .append("<table ")
                         .appendAttr("width",(StringUtil.len(getWidth()) > 0)? getWidth() :"100%");
            
	        if (!isMulticolumn()) {
            	getPageflow().getPage().s.appendAttr("id", "listform");
            }
            
            getPageflow().getPage().s.appendNL('>');
	        
	        /**
	         * Create list header
	         * The label for the select button column may have been overridden
	         * by the tag zXSelectLabel
	         * The width of the select button column can be overruled by the tag
	         * zXSelectWidth
	         */
	        String strLabel;
	        if (hasTag("zXSelectLabel")) {
	            strLabel = tagValue("zXSelectLabel");
	        } else {
	            strLabel = "Select";
	        }
	        
	        if (hasTag("zXSelectWidth")) {
	            strOrgSelectWidth = getPageflow().getPage().getWebSettings().getListFormColumn1();
	            getPageflow().getPage().getWebSettings().setListFormColumn1(tagValue("zXSelectWidth"));
	        }
	        
	        /**
	         * DGS12FEB2004: If multi-column listform, open another level of table to
	         * handle columns. We do this by starting a row, which will be the only
	         * row in this table, and then having a td per column, with each td containing
	         * its own table of n rows, where n = 'rowsPerColumn'.
	         * Here we only open the row - the table etc. is handled inside the local function
	         * 'columnHeadings'.
	         */
	        if (isMulticolumn()) {
	            getPageflow().getPage().s.appendNL("<tr>");
	        }
	        
	        /**
	         * DGS12FEB2004: column headings are now generated in a called function, as they
	         * are also generated at the beginning of each column if multi-column.
	         */
	        columnHeadings(objResortUrl, blnSelect, strLabel, colEntities);
	        
	        /**
	         * DGS12FEB2004: To avoid ugly errors, make sure the rows per column
	         * cannot be zero.
	         */
	        int intRowsPerColumn = getRowspercolumn();
	        if (intRowsPerColumn == 0) {
	            intRowsPerColumn = intMaxResultRows;
	        }
	        
	        /**
	         * The number of checkboxes for a multilist can be controlled using
	         * the tag zXNumSelectCB
	         */
	        int intNumSelect = 1;
	        if (isMultilist()) {
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
                
	        } // Multi-list?
	        
	        /**
	         * Get primary key from querystring
	         */
	        String strPK = getPageflow().getQs().getEntryAsString(StringUtil.len(getQspk())==0?"-pk":getQspk());
	        
	        /**
	         * Turn query into recordset
	         */
            if (blnIsDSChannel && objTheEntity != null) {
                objRS = objDSHandler.boRS(objTheEntity.getBo(),
                                          objTheEntity.getSelectlistgroup(),
                                          strWhereClause,
                                          isResolvefk(),        // Will this generate a special query?
                                          strOrderByClause, blnReverse,
                                          lngStartRow, lngBatchSize);
                
            } else {
                objRS = ((DSHRdbms)objDSHandler).sqlRS(strQuery,
                                                       lngStartRow, lngBatchSize);
                
            } // Channel or RDBMS?
            
	        if (objRS == null) {
	            throw new Exception("Unable to execute query");
	        }
            
	        Iterator iter;
	        PFEntity objEntity;
	        
	        String strUrl ="";
	        String strTmp;
	        boolean blnOdd = false;
	        
	        if (!isMulticolumn()) {
	        	getPageflow().getPage().s.appendNL("<tbody>");
	        }
	        
	        /**
	         * v1.5:60 DGS19SEP2005: resolve the various total directors now
	         */
            double dblTotalValue;
            double dblAttrValue;
            AttributeCollection colAttr;
	        Tuple objTotalGroup;
	        Tuple objTotalGroupBy;
	        int intTotalGroups = 0;
	        int intTotalGroupBy = 0;
	        
	        if (getTotalgroups() != null) {
	        	intTotalGroups = getTotalgroups().size();
	        	for (int i = 0; i < intTotalGroups; i++) {
	        		objTotalGroup = (Tuple)getTotalgroups().get(i);
	        		
	        		objTotalGroup.setName(getPageflow().resolveDirector(objTotalGroup.getName()));
					objTotalGroup.setValue(getPageflow().resolveDirector(objTotalGroup.getValue()));
				}
	        }
	        if (getTotalgroupbys() != null) {
	        	intTotalGroupBy = getTotalgroupbys().size();
	        	for (int i = 0; i < intTotalGroupBy; i++) {
	        		objTotalGroupBy = (Tuple)getTotalgroupbys().get(i);
	        		
	        		objTotalGroupBy.setName(getPageflow().resolveDirector(objTotalGroupBy.getName()));
	        		objTotalGroupBy.setValue(getPageflow().resolveDirector(objTotalGroupBy.getValue()));
				}
	        }
	        
	        /** Using to keep track of the attribute totals. */
	        AttributeTagCollection colAttrTags = new AttributeTagCollection();
	        
	        /**
	         * And generate each list row
	         */
	        /** Number of rows returned.*/
	        int intRowCount = 1;
	        /** Used to track whether there are move rows */
	        
	        Attribute objAttr;
	        Iterator iterAttr;
	        
	        while (!objRS.eof() && intRowCount <= intMaxResultRows) {
	            /**
	             * Populate all the business objects...
	             */
	            iter = colEntities.iterator();
	            while (iter.hasNext()) {
	                objEntity = (PFEntity)iter.next();
	                objRS.rs2obj(objEntity.getBo(), 
	                			 objEntity.getSelectlistgroup(), 
	                			 isResolvefk());
	            }
	            
	            /**
	             * May be, the row is not 'active' at all
	             */
	            if (objListUrl != null && getPageflow().isActive(objListUrl.getActive())) {
	                /**
	                 * DGS12FEB2004: If multi-column listform and we have generated enough rows
	                 * for this column, close the cell and open another one for the next column,
	                 * with headings repeated at the top (mostly done in called local function)
	                 */
	                if (isMulticolumn()) {
	                    if (intRowCount > 1 && ((intRowCount - 1) / intRowsPerColumn) * intRowsPerColumn == (intRowCount - 1)) {
	                        getPageflow().getPage().s.appendNL("</table>\n</td>");
	                        columnHeadings(objResortUrl, blnSelect, strLabel, colEntities);
	        	        	getPageflow().getPage().s.appendNL("<tbody>");
	                    }
	                }
	                
	                /**
	                 * v1.5:60 DGS19SEP2005: See if this row is different to the previous one in terms
	                 * of totalling attributes to group by (if we have any defined)
	                 */
	                if (intRowCount > 1) {
	                	boolean blnSubTotal = false;
	                	for (int i = 0; i < intTotalGroupBy; i++) {
	                		objTotalGroupBy = (Tuple)getTotalgroupbys().get(i);
	                		
	        	            iter = colEntities.iterator();
	        	            loopentities : while (iter.hasNext()) {
	        	                objEntity = (PFEntity)iter.next();
	        	                
	        	                if (objTotalGroupBy.getName().equalsIgnoreCase(objEntity.getName())) {
	        	                	/**
	        	                	 * For each attr in the group, if the current value is different
	        	                	 * from the last one, and we are not on the first row of the list,
	        	                	 * we will generate a sub-totals line
	        	                	 */
	        	                	colAttr = objEntity.getBODesc().getGroup(objTotalGroupBy.getValue());
	        	                	if (colAttr == null) {
	        	                		getZx().trace.addError("At sub total, unable to get group " + objTotalGroupBy.getValue() + 
	        	                							   " of " + objTotalGroupBy.getName());
	        	                		go = zXType.rc.rcError;
	        	                		return go;
	        	                	}
	        	                	iterAttr = colAttr.iterator();
	        	                	loopattr : while (iterAttr.hasNext()) {
	        	                		objAttr = (Attribute)iterAttr.next();
	        	                		
	        	                		if (!colAttrTags.getValue(objAttr.getName(), "zXTotalGroupBy").equals(objEntity.getBo().getValue(objAttr.getName()).getStringValue())) {
	        	                			/**
	        	                			 * As soon as we know there's going to be a sub-total, stop
	        	                			 * looking for any other mismatches.
	        	                			 */
	        	                			blnSubTotal = true;
	        	                			break loopattr;
	        	                		}
	        	                	}
	        	                	if (blnSubTotal) {
	        	                		/**
	        	                		 * As soon as we know there's going to be a sub-total, stop
	        	                		 * looking for any other mismatches.
	        	                		 */
	        	                		break loopentities;
	        	                	}
	        	                }
	        	            }
						}
	                	if (blnSubTotal) {
	                		if (printTotals(colAttrTags, blnSelect, false, colEntities).pos != zXType.rc.rcOK.pos) {
	                			getZx().trace.addError("Unable to print sub-totals");
	                			go = zXType.rc.rcError;
	                			return go;
	                		}
	                	}
	                }
	                
	                /**
	                 * Now set the current values into the tags
	                 */
	                for (int i = 0; i < intTotalGroupBy; i++) {
	                	objTotalGroupBy = (Tuple)getTotalgroupbys().get(i);
	                	
	    	            iter = colEntities.iterator();
	    	            while (iter.hasNext()) {
	    	                objEntity = (PFEntity)iter.next();
	    	                
	    	                if (objTotalGroupBy.getName().equalsIgnoreCase(objEntity.getName())){
	    	                	colAttr = objEntity.getBODesc().getGroup(objTotalGroupBy.getValue());
	    	                	if (colAttr == null) {
	    	                		getZx().trace.addError("At sub total, unable to get group " + objTotalGroupBy.getValue() + " of " + objTotalGroupBy.getName());
	    	                		go = zXType.rc.rcError;
	    	                		return go;
	    	                	}
	    	                	iterAttr = colAttr.iterator();
	    	                	while(iterAttr.hasNext()) {
	    	                		objAttr = (Attribute)iterAttr.next();
	    	                		colAttrTags.setValue(objAttr.getName(), 
	    	                							  "zXTotalGroupBy", 
	    	                							  objEntity.getBo().getValue(objAttr.getName()).getStringValue());
	    	                	}
	    	                }
	    	            }
					}
	                
	                /**
	                 * We need this because when we have multiple entities, we still
	                 * only want to generate the open i.HTML once and only once
	                 */
	                boolean blnDidOpen = false;
	                
	                iter = colEntities.iterator();
	                while (iter.hasNext()) {
	                    objEntity = (PFEntity)iter.next();
	                    if (StringUtil.len(objEntity.getListgroup()) > 0) {
	                    	/**
	                    	 * Resolve earlier.
	                    	 */
		                    boolean blnIsPK = objEntity.getBo().getPKValue().getStringValue().equals(strPK);
		                    
		                    if (!blnDidOpen) {
		                        /**
		                         * Open the list row
		                         */
		                        blnDidOpen = true;
		                        /**
		                         * Determine the url associated with the row (if there
		                         *    is one)
		                         */
		                        if (blnSelect) {
		                            strUrl = constructRowURL(objListUrl, objEntity, intRowCount);
		                            
		                            /**
		                             * Save last URL as it may be used when
		                             * zXOneRowAutoSelect is used
		                             */
		                            strLastListUrl = strUrl;
		                            
		                        } else {
		                            strUrl = "";
		                        }
		                        
		                        /**
		                         * If this row has the same pk value as what was passed
		                         *  in querystring than we may have to do 2 things:
		                         *  - include an anchor that will allow us to reposition
		                         *    we we last left off
		                         *  - generate the row with the checkbox checked (in case
		                         *    of autocheck / multilist)
	                             * 
	                             * DGS15MAY2005 V1.4:78: Set checkRow false here. It might get set true later
		                         */
		                        boolean blnCheckRow = false;
		                        if (blnIsPK) {
		                            /**
		                             * Only do check box for most recently visited row
		                             * when autocheck is active...
		                             */
		                            if (isAutocheck()) {
		                                blnCheckRow = true;
		                            }
	                                
		                        }
		                        
		                        /**
		                         * DGS22JAN2003: Can pre-check the checkbox by setting a tag in the pageflow:
		                         * DGS02MAR2004: More efficient way of assessing this tag, and also supports it
		                         * being set to a non-1 value (as often used in expressions) to switch on and off
		                         * at run time.
	                             * BD24FEB05 V1.4:49: also a good idea to set to false in
	                             * else as otherwise, once checked wll never
	                             * be unchecked
	                             * DGS15MAY2005 V1.4:78: Above is a good idea, but not here, as we may already have
	                             * set it true in the previous statement. Now set it false earlier, before this.
		                         */
		                        if (!blnCheckRow && getPageflow().resolveDirector(tagValue("zXMultiCheck")).equals("1")) {
		                            blnCheckRow = true;
		                        }
		                        
		                        /**
		                         * Determine whether we want to add a special class
		                         */
		                        strTmp = getPageflow().resolveDirector(getClazz());
		                        if (StringUtil.len(strTmp) > 0) {
		                            if (isAddparitytoclass()) {
		                                if (blnOdd) {
		                                    strTmp = strTmp + "Odd";
		                                } else {
		                                    strTmp = strTmp + "Even";
		                                }
		                            }
	                                
		                        } else if (!isAddparitytoclass()) {
	                                // Should we default to zxNor style?
	                                //strTmp = "zxNor";
	                            }
		                        
		                        /**
		                         * Now ready to generate the i.HTML to open the listrow
		                         */
		                        if (isMultilist()) {
		                            getPageflow().getPage().multiListRowOpen(objEntity.getBo(), blnOdd, strUrl, blnCheckRow, intNumSelect, strTmp);
		                        } else {
		                            getPageflow().getPage().listRowOpen(blnOdd, strUrl, strTmp);
		                        }
		                        
		                    } // Has already done row open sequence
		                    
		                    /**
		                     * Insert a named anchor
		                     */
		                    if (blnIsPK) {
		                        getPageflow().getPage().s.appendNL("<A name='zXActiveRow' id='zXActiveRow'></A>");
		                    }
		                    
		                    /**
		                     * Generate the columns
		                     */
		                    if (isMultilist()) {
		                        getPageflow().getPage().multiListRow(objEntity.getBo(), objEntity.getListgroup());
		                    } else {
		                        getPageflow().getPage().listRow(objEntity.getBo(), objEntity.getListgroup(), 0);
		                    }
		                    
	                	} // Has listgroup?
	                    
	                    /**
	                     * v1.5:60 DGS19SEP2005: Keep the totals and grand totals for all
	                     * columns defined for totalling
	                     */
                    	for (int i = 0; i < intTotalGroups; i++) {
                    		objTotalGroup = (Tuple)getTotalgroups().get(i);
                    		
                    		if (StringUtil.len(objTotalGroup.getName()) > 0 && StringUtil.len(objTotalGroup.getValue()) > 0) {
                    			if (objTotalGroup.getName().equalsIgnoreCase(objEntity.getName())) {
                    				colAttr = objEntity.getBODesc().getGroup(objTotalGroup.getValue());
                                    if (colAttr == null) {
                                        getZx().trace.addError("In counting, unable to get group " + objTotalGroup.getValue() 
                                        						+ " of " + objTotalGroup.getName());
                                        go = zXType.rc.rcError;
                                        return go;
                                    }
                                    
                                    iterAttr = colAttr.iterator();
                                    while(iterAttr.hasNext()) {
                                    	objAttr = (Attribute)iterAttr.next();
                                    	
                                        /**
                                         * Great care needed here. We don't know for sure that this attr is
                                         * numeric. We don't want to look at the datatype because we can
                                         * happily total string attrs if the values are always numeric, so
                                         * why not. So just be careful not to use a value if it isn't numeric.
                                         * 
                                         * If the value is not numeric it will be ignored.
                                         **/
                                        String strAttrValue = objEntity.getBo().getValue(objAttr.getName()).getStringValue();
                                        if (StringUtil.isNumeric(strAttrValue) || StringUtil.isDouble(strAttrValue)) {
                                            dblAttrValue = Double.parseDouble(strAttrValue);
                                            
                                            /**
                                             * First the sub total:
                                             **/
                                            String strTotalValue = colAttrTags.getValue(objAttr.getName(), "zXTotalCounter");
                                            if (StringUtil.isNumeric(strTotalValue) || StringUtil.isDouble(strTotalValue)) {
                                                dblTotalValue = dblAttrValue + Double.parseDouble(strTotalValue);
                                            } else {
                                                dblTotalValue = dblAttrValue;
                                            }
                                            
                                            /**
                                             * Set the total into a tag against the attr
                                             **/
                                            colAttrTags.setValue(objAttr.getName(), 
                                            					 "zXTotalCounter", 
                                            					 String.valueOf(dblTotalValue));
                                            
                                            /**
                                             * Now the grand total:
                                             **/
                                            strTotalValue = colAttrTags.getValue(objAttr.getName(), "zXGrandTotalCounter");
                                            if (StringUtil.isNumeric(strTotalValue) || StringUtil.isDouble(strTotalValue)) {
                                                dblTotalValue = dblAttrValue + Double.parseDouble(strTotalValue);
                                            } else {
                                                dblTotalValue = dblAttrValue;
                                            }
                                            
                                            colAttrTags.setValue(objAttr.getName(), 
                                            					 "zXGrandTotalCounter", 
                                            					 String.valueOf(dblTotalValue));
                                        }
                                    } // Add value to the total.
                                    
                    			}
                    		}
						}
	                    
	                } // Loop over entities
	                
	                /**
	                 * Close the listrow
	                 */
	                if (isMultilist()){
	                    getPageflow().getPage().multiListRowClose();
	                } else {
	                    getPageflow().getPage().listRowClose();
	                }
	                
	                if (isMulticolumn()) {
	                    if (intRowCount > 1 && ((intRowCount - 1) / intRowsPerColumn) * intRowsPerColumn == (intRowCount - 1)) {
            	        	getPageflow().getPage().s.appendNL("</tbody>");
	                    }
	                }
	                
	            } // Is active ?
	            
                /** Iterate over the result set */
                objRS.moveNext();
                
	            /**
	             * Switch row color
	             */
	            blnOdd = !blnOdd;
	            intRowCount++;
	        }
	        
	        /**
	         * v1.5:60 DGS19SEP2005: If we had any columns to total on and at least one row found,
	         * show the sub total for the last set of rows
	         */
	        if (intRowCount > 1 && intTotalGroupBy > 0) {
        		if (printTotals(colAttrTags, blnSelect, false, colEntities).pos != zXType.rc.rcOK.pos) {
        			getZx().trace.addError("Unable to print sub-totals");
        			go = zXType.rc.rcError;
        			return go;
        		}
	        }
	        
	        /**
	         * We can tell that there was only one row when we have hit
	         * eof and the rowCount was 2 (remember, rowCount is always one
	         * higher than number of rows found as it is increased at the
	         * end of the loop)
	         */
	        if (objRS.eof() && intRowCount == 2) {
	        	blnOnlyOneRow = true;
	        }
	        
	        /**
	         * Allows for row highlighting in Mozilla
	         */
	        if (!isMulticolumn()) {
	        	getPageflow().getPage().s.appendNL("<tbody>");
	        }
	        
	        /**
	         * DGS12FEB2004: If multi-column listform close off the outer level of table and close
	         *  the final row
	         */
	        if (isMulticolumn()) {
	            getPageflow().getPage().s.appendNL("</table>\n</td>\n</tr>");
	        }
	        
	        /**
	         * v1.5:60 DGS19SEP2005: If grand total, show it now for each attr in the total groups
	         */
	        if (intRowCount > 1 && isGrandtotal() && (intTotalGroupBy > 0 || intTotalGroups > 0)) {
	        	getPageflow().getPage().s.appendNL("<tr><td/></tr>");
	        	
        		if (printTotals(colAttrTags, blnSelect, true, colEntities).pos != zXType.rc.rcOK.pos) {
        			getZx().trace.addError("Unable to print sub-totals");
        			go = zXType.rc.rcError;
        			return go;
        		}
	        }
	        
	        getPageflow().getPage().s.appendNL("</table>");
	        
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
	            if (intPage > 1) {
	                getPageflow().getQs().setEntry(strPg, intPage - 1 + "");
	                strUrl = "zXRef('" + getPageflow().constructURL(objPagingUrl) + "');";
	                
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
	            if (!objRS.eof()) {
	                getPageflow().getQs().setEntry(strPg, intPage + 1 + "");
	                strUrl = "zXRef('" + getPageflow().constructURL(objPagingUrl) + "');";
	                
	                getPageflow().getPage().s.appendNL("<td  width='1%' align='right'>");
	                getPageflow().getPage().s.append("<img ")
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
	            getPageflow().getQs().setEntry(strPg, intPage+"");
	            
	            getPageflow().getPage().s.appendNL("</tr>");
	            getPageflow().getPage().s.appendNL("</table>");
	            
	            if (!hasTag("zXListFormNoCount")) {
	                /**
	                 * End for the message
	                 */
	                if (intRowCount == 1 && intPage == 1) {
                        /**
                         * No results found
                         */
	                    getPageflow().getPage().infoMsg("No matches found");
                        
	                } else if (objRS.eof() && intPage == 1) {
	                    getPageflow().getPage().softMsg(intRowCount - 1 + " row" + (intRowCount == 2?"":"s") + " displayed");
                        
	                } else {
	                    getPageflow().getPage().softMsg("Page " + intPage + " displayed, records " + (1 + (intPage - 1) * intMaxResultRows) + " to "  +  ((intPage - 1) * intMaxResultRows + intRowCount - 1));
                        
	                }
	            }
                
	        } else {
                if (!hasTag("zXListFormNoCount")) {
                    /**
                     * End for the message
                     */
                    if (intRowCount == 1) {
                        /**
                         * No results found
                         */
                        getPageflow().getPage().infoMsg("No matches found");
                        
                    } else if (!objRS.eof() || (intRowCount -1) > intMaxResultRows) {
                        getPageflow().getPage().softMsg("Resultset truncated, " + (intRowCount - 1) + " row displayed");
                        
                    } else {
                        getPageflow().getPage().softMsg((intRowCount - 1) + " row" + (intRowCount == 2? "":"s") + " displayed");
                        
                    }
                }
                
            }
	        
	        /**
	         * Handle buttons; make sure that the buttons are aligned with the
	         *  right column of the list
	         */
	        if (!isMultilist() && !blnSelect) {
	            getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftNull);
	        } else {
	            getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftList);
	        }
	        
	        getPageflow().processFormButtons(this);
	        
	        if (!isMultilist() && !blnSelect) {
	            getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftNull);
	        } else {
	            getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftList);
	        }
	        
	        /**
	         * Close the form that we have opened in case this was a submit query
	         */
	        if (isMultilist() && !getPageflow().isOwnForm() && !getPageflow().resolveDirector(tagValue("zXNoFormEnd")).equals("1")) {
	            getPageflow().getPage().s.appendNL("</form>");
	        }
	        
	        /**
	         * Handle window footer title
	         */
	        getPageflow().handleFooterAndTitle(this, "Select row / action");
	        
	        /**
	         * Set cursor to the active row
	         */
	        if (!tagValue("zXNoAutoPosition").equals("1")) {
	            getPageflow().getPage().s.append("<script type=\"text/javascript\" language=\"JavaScript\">").appendNL()
            						.append("	zXRef('#zXActiveRow');").appendNL()
            						.append("</script>").appendNL();
	        }
	        
	        /**
	         * If we have only found one row and the zXOneRowAutoSelect feature
	         * has been requested, simply repeat the URL as associated with the
	         * only item in the list and let Javascript handle the rest
	         */
	        if (blnOnlyOneRow) {
	        	if (getPageflow().resolveDirector(tagValue("zXOneRowAutoSelect")).equals("1")) {
	                getPageflow().getPage().s.appendNL("<script language=\"javascript\">");
	                getPageflow().getPage().s.appendNL(strLastListUrl);
	                getPageflow().getPage().s.appendNL("</script>");
	        	}
	        }
	        
	        getPageflow().setAction(getPageflow().resolveLink(this.getLink()));
            
	        return go;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Execute pageflow.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            go = zXType.rc.rcError;
            return go;
        } finally {
            /**
             * Restore select column width
             */
            if (StringUtil.len(strOrgSelectWidth)> 0) {
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
    
    /** 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();
        
        objXMLGen.taggedValue("qsentity", getQsentity());
        objXMLGen.taggedValue("qspk", getQspk());
        objXMLGen.taggedValue("autocheck", isAutocheck());
        objXMLGen.taggedValue("multilist", isMultilist());
        
        /**
         * DGS12FEB2004: New properties for multiple columns. To avoid adding these
         * tags to old pageflows when saving them for other reasons (which makes too
         * many unnecessary differences appear when comparing changes), only include
         * them if they apply i.e. the multi column if its checkbox is checked, and
         * the rows per column if that is checked or the rows is not zero
         */
        if (isMulticolumn()) {
            objXMLGen.taggedValue("multicolumn", isMulticolumn());
        }
        if (isMulticolumn() || getRowspercolumn() > 0) {
            objXMLGen.taggedValue("rowspercolumn", String.valueOf(getRowspercolumn()));
        }
        
        getDescriptor().xmlUrl("resorturl", getResorturl());
        getDescriptor().xmlUrl("pagingurl", getPagingurl());
        
        /**
         * C1.5:60 DGS03OCT2005: New properties for totals
         */
        if (getTotalgroups() != null) {
        	objXMLGen.openTag("totalgroups");
        	
        	Tuple objTuple;
        	int intTotalGroups = getTotalgroups().size();
        	for (int i = 0; i < intTotalGroups; i++) {
        		objTuple = (Tuple)getTotalgroups().get(i);
        		
            	objXMLGen.openTag("totalgroup");
        		objXMLGen.taggedCData("entity", objTuple.getName());
        		objXMLGen.taggedCData("group", objTuple.getValue());
            	objXMLGen.closeTag("totalgroup");
			}
        	
        	objXMLGen.closeTag("totalgroups");
        }
        
        if (getTotalgroupbys() != null) {
        	objXMLGen.openTag("totalgroupbys");
        	
        	Tuple objTuple;
        	int intTotalGroupBys = getTotalgroupbys().size();
        	for (int i = 0; i < intTotalGroupBys; i++) {
        		objTuple = (Tuple)getTotalgroupbys().get(i);
        		
            	objXMLGen.openTag("totalgroupby");
        		objXMLGen.taggedCData("entity", objTuple.getName());
        		objXMLGen.taggedCData("group", objTuple.getValue());
            	objXMLGen.closeTag("totalgroupby");
			}
        	
        	objXMLGen.closeTag("totalgroupbys");
        }
        
        if (isGrandtotal()) {
        	objXMLGen.taggedValue("grandtotal", isGrandtotal());
        }
        
		objXMLGen.taggedCData("totalrowclass", getTotalrowclass());
		objXMLGen.taggedCData("totalcellclass", getTotalcellclass());
    }
    
    /**
     * @see PFAction#clone(Pageflow)
     */
    public PFAction clone(Pageflow pobjPageflow) {
        PFListForm cleanClone = (PFListForm)super.clone(pobjPageflow);
        
        cleanClone.setAutocheck(isAutocheck());
        cleanClone.setMulticolumn(isMulticolumn());
        cleanClone.setMultilist(isMultilist());
        
        // Shared with PFGridEditForm :
        if (getPagingurl() != null) {
            cleanClone.setPagingurl((PFUrl)getPagingurl().clone());
        }
        
        cleanClone.setQsentity(getQsentity());
        // Shared with PFStdPopup :
        cleanClone.setQspk(getQspk());
        
        if (getResorturl() != null) {
            cleanClone.setResorturl((PFUrl)getResorturl().clone());
        }
        
        cleanClone.setRowspercolumn(getRowspercolumn());
        
        /**
         * C1.5:60 DGS03OCT2005: New properties for totals
         */
        if (getTotalgroups() != null && getTotalgroups().size() > 0) {
        	cleanClone.setTotalgroups(CloneUtil.clone((ArrayList)getTotalgroups()));
        }
        if (getTotalgroupbys() != null && getTotalgroupbys().size() > 0) {
        	cleanClone.setTotalgroupbys(CloneUtil.clone((ArrayList)getTotalgroupbys()));
        }
        
        cleanClone.setGrandtotal(isGrandtotal());
		cleanClone.setTotalrowclass(getTotalrowclass());
		cleanClone.setTotalcellclass(getTotalcellclass());
        
        return cleanClone;
    }
}