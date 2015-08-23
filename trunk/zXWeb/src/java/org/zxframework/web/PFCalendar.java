/*
 * Created on Nov 29, 2004 by Michael Brewer
 * $Id: PFCalendar.java,v 1.1.2.37 2006/07/17 16:07:13 mike Exp $
 */
package org.zxframework.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.zxframework.Attribute;
import org.zxframework.LabelCollection;
import org.zxframework.ZXBO;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.datasources.DSHRdbms;
import org.zxframework.datasources.DSHandler;
import org.zxframework.datasources.DSRS;
import org.zxframework.datasources.DSWhereClause;
import org.zxframework.property.DateProperty;
import org.zxframework.util.DateUtil;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Pageflow calendar action object.
 * 
 * <pre>
 * 
 * Who    : David Swann
 * When   : 14 September 2004
 * 
 * Change    : BD24OCT04
 * Why       : Small changes as result of first use in real-life:
 *             - Calculation of first date changed slightly
 *             - Fixed error in constructing SQL when having a where
 *               group in entity definition
 *             - Add BO to BO context when looping over it
 * 
 * Change    : DGS01NOV2004
 * Why       : Minor revision to first date calculation change - now forces
 *             all dates obtained through the pageflow to have no time component.
 *             This ensures that all ranges are full days, not stopping the query
 *             part way through the start or end date.
 * 
 * Change    : MB10NOV2004
 * Why       : zoomfactor should be parsed as a string. And default zoomfactor should be 2.
 * 
 * Change    : MB11NOV2004
 * Why       : Reset the QS -zXCalendarZoom to the original value when printing cell urls.
 *             Also use good defaults for labelWidth and minHeight.
 *             Top align the contents of the calendar.
 * 
 * Change    : BD5APR05 - V1.5:1
 * Why       : Support for data-sources
 * 
 * Change    : v1.4:94 DGS22JUL2005
 * Why       : (spotted by BD): use zX isDate not VB's
 * 
 * Change    : V1.5:65 - BD7NOV05
 * Why       : Added parameterBag support
 * 
 * Change    : V1.5:75 - BD3JAN06
 * Why       : Added support for cell action
 *
 * Change    : V1.5:76 - BD3JAN06
 * Why       : Fixed bug with quarter zoom factor
 * 
 * Change    : V1.5:84 - BD31JAN06
 * Why       : Fixed bug active expression in cell URL
 *
 * Change    : V1.5:95 - BD30APR06
 * Why       : Add -zXCalendarCellStartDate and -zXCalendarCellEndDate to QS before
 *               evaluating active for cellURL so we can refer to this
 * </pre>
 */
public class PFCalendar extends PFAction {

	//------------------------ Members
	
    private String labelwidth;
    private String colwidth;
    private String minheight;

    private int rowspercell;

    private zXType.pageflowCellMode cellMode;
    private zXType.displayOrientation orienTation;

    private String currentclass;
    private String normalclass;
    private String anchorentity;
    private String anchorattr;
    private String currentdate;
    private String startperiod;
    private String zoomfactor;
    private String active;
    private String labelformat;

    private PFUrl prevurl;
    private PFUrl nexturl;
    private PFUrl zoomurl;
    private PFUrl cellurl;

    private LabelCollection calendartitle;

    private ArrayList categories;
    
    //------------------------ Constants
    
    private static final String[] arrZoomFactors = {"Week", "Month", "Quarter", "Half-year", "Year"};
    private static final String[] arrWeekDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    
    /** Zoom level : Week - 1 */
    private static final int ZL_WEEK = 1;
    /** Zoom level : Month - 2 (Default) */
    private static final int ZL_MONTH = 2;
    /** Zoom level : Quartetly - 3 (Default) */
    private static final int ZL_QUARTLY = 3;
    /** Zoom level : Half Yearly - 4 (Default) */
    private static final int ZL_HALF = 4;
    /** Zoom level : Yearly - 5 (Default) */
    private static final int ZL_YEARLY = 5;
    
    //------------------------ Constructors

    /**
     * Default constructor.
     */
    public PFCalendar() {
        super();
    }

    //------------------------ Getters and Setters

    /**
     * @return Returns the active.
     */
    public String getActive() {
        return this.active;
    }

    /**
     * @param active The active to set.
     */
    public void setActive(String active) {
        this.active = active;
    }

    /**
     * @return Returns the anchorattr.
     */
    public String getAnchorattr() {
        return this.anchorattr;
    }

    /**
     * @param anchorattr The anchorattr to set.
     */
    public void setAnchorattr(String anchorattr) {
        this.anchorattr = anchorattr;
    }

    /**
     * @return Returns the anchorentity.
     */
    public String getAnchorentity() {
        return this.anchorentity;
    }

    /**
     * @param anchorentity The anchorentity to set.
     */
    public void setAnchorentity(String anchorentity) {
        this.anchorentity = anchorentity;
    }

    /**
     * @return Returns the calendartitle.
     */
    public LabelCollection getCalendartitle() {
        return this.calendartitle;
    }

    /**
     * @param calendartitle The calendartitle to set.
     */
    public void setCalendartitle(LabelCollection calendartitle) {
        this.calendartitle = calendartitle;
    }

    /**
     * @return Returns the categories.
     */
    public ArrayList getCategories() {
        return this.categories;
    }

    /**
     * @param categories The categories to set.
     */
    public void setCategories(ArrayList categories) {
        this.categories = categories;
    }

    /**
	 * @return Returns the cellMode.
	 */
	public zXType.pageflowCellMode getCellMode() {
		return this.cellMode;
	}

	/**
	 * @param cellMode The cellMode to set.
	 */
	public void setCellMode(zXType.pageflowCellMode cellMode) {
		this.cellMode = cellMode;
	}

	/**
     * @return Returns the colwidth.
     */
    public String getColwidth() {
        return this.colwidth;
    }

    /**
     * @param colwidth The colwidth to set.
     */
    public void setColwidth(String colwidth) {
        this.colwidth = colwidth;
    }

    /**
     * @return Returns the currentclass.
     */
    public String getCurrentclass() {
        return this.currentclass;
    }

    /**
     * @param currentclass The currentclass to set.
     */
    public void setCurrentclass(String currentclass) {
        this.currentclass = currentclass;
    }

    /**
     * @return Returns the currentdate.
     */
    public String getCurrentdate() {
        return this.currentdate;
    }

    /**
     * @param currentdate The currentdate to set.
     */
    public void setCurrentdate(String currentdate) {
        this.currentdate = currentdate;
    }

    /**
     * @return Returns the labelformat.
     */
    public String getLabelformat() {
        return this.labelformat;
    }

    /**
     * @param labelformat The labelformat to set.
     */
    public void setLabelformat(String labelformat) {
        this.labelformat = labelformat;
    }

    /**
     * @return Returns the labelwidth.
     */
    public String getLabelwidth() {
        return this.labelwidth;
    }

    /**
     * @param labelwidth The labelwidth to set.
     */
    public void setLabelwidth(String labelwidth) {
        this.labelwidth = labelwidth;
    }

    /**
     * @return Returns the minheight.
     */
    public String getMinheight() {
        return this.minheight;
    }

    /**
     * @param minheight The minheight to set.
     */
    public void setMinheight(String minheight) {
        this.minheight = minheight;
    }

    /**
     * @return Returns the nexturl.
     */
    public PFUrl getNexturl() {
        return this.nexturl;
    }

    /**
     * @param nexturl The nexturl to set.
     */
    public void setNexturl(PFUrl nexturl) {
        this.nexturl = nexturl;
    }

    /**
     * @return Returns the zoomurl.
     */
    public PFUrl getZoomurl() {
        return this.zoomurl;
    }

    /**
     * @param zoomurl The zoomurl to set.
     */
    public void setZoomurl(PFUrl zoomurl) {
        this.zoomurl = zoomurl;
    }

    /**
	 * @return Returns the cellurl.
	 */
	public PFUrl getCellurl() {
		return this.cellurl;
	}

	/**
	 * @param cellurl The cellurl to set.
	 */
	public void setCellurl(PFUrl cellurl) {
		this.cellurl = cellurl;
	}

	/**
     * @return Returns the normalclass.
     */
    public String getNormalclass() {
        return this.normalclass;
    }

    /**
     * @param normalclass The normalclass to set.
     */
    public void setNormalclass(String normalclass) {
        this.normalclass = normalclass;
    }
    
    /**
	 * @return Returns the orienTation.
	 */
	public zXType.displayOrientation getOrienTation() {
		return this.orienTation;
	}

	/**
	 * @param orienTation The orienTation to set.
	 */
	public void setOrienTation(zXType.displayOrientation orienTation) {
		this.orienTation = orienTation;
	}

	/**
     * @return Returns the prevurl.
     */
    public PFUrl getPrevurl() {
        return this.prevurl;
    }

    /**
     * @param prevurl The prevurl to set.
     */
    public void setPrevurl(PFUrl prevurl) {
        this.prevurl = prevurl;
    }

    /**
     * @return Returns the rowspercell.
     */
    public int getRowspercell() {
        return this.rowspercell;
    }

    /**
     * @param rowspercell The rowspercell to set.
     */
    public void setRowspercell(int rowspercell) {
        this.rowspercell = rowspercell;
    }

    /**
     * @return Returns the startperiod.
     */
    public String getStartperiod() {
        return this.startperiod;
    }

    /**
     * @param startperiod The startperiod to set.
     */
    public void setStartperiod(String startperiod) {
        this.startperiod = startperiod;
    }

    /**
     * @return Returns the zoomfactor.
     */
    public String getZoomfactor() {
        return this.zoomfactor;
    }
    
    /**
     * @param zoomfactor The zoomfactor to set.
     */
    public void setZoomfactor(String zoomfactor) {
        this.zoomfactor = zoomfactor;
    }

    //------------------------ Digester helper methods.
    
    /**
     * Special set method used by Digester.
     * 
     * @param orientation The orientation to set.
     */
    public void setOrientation(String orientation) {
        this.orienTation = zXType.displayOrientation.getEnum(orientation);
        
        /**
         * Default to the vertical layout.
         */
        if (this.orienTation == null) {
			this.orienTation = zXType.displayOrientation.doVertical;
		}
    }

    /**
     * Special set method used by Digester.
     * 
     * @param cellmode The cellmode to set.
     */
    public void setCellmode(String cellmode) {
        this.cellMode = zXType.pageflowCellMode.getEnum(cellmode);
        
        // Default to list mode.
        if (this.cellMode == null) {
			this.cellMode = zXType.pageflowCellMode.pcmList;
		}
    }
    
    //------------------------ Helper methods

    /**
     * We keep an array of terms for each of the zoom factors. This property is only provided
     *  to allow a consistent translation
     * 
     * @param pintZoomFactor The number of the zoomfactor to select. First value is 1.
     * @return Returns the zoomfactor.
     */
    public String getZoomfactorAsString(int pintZoomFactor) {
        String getZoomfactorAsString = "";

        if (pintZoomFactor > 0 && pintZoomFactor <= PFCalendar.arrZoomFactors.length) {
			// Unfortunately we have to add one to make numbering consistent.
            getZoomfactorAsString = PFCalendar.arrZoomFactors[pintZoomFactor - 1];
		}

        return getZoomfactorAsString;
    }

    //------------------------ Private helper methods
    
    /**
     * Determine the class.
     *
     * @param pstrClass The base class name. 
     * @param pblnAddParityToClass Whether to add parity to the class name. 
     * @param pblnOdd Whether this is a odd row 
     * @return Returns the class name.
     */
    private String constructClass(String pstrClass, 
    							  boolean pblnAddParityToClass, 
    							  boolean pblnOdd) {
        return constructClass(pstrClass,pblnAddParityToClass,pblnOdd,"zx");
    }
    
    /**
     * Determine the class.
     *
     * @param pstrClass The base class name. 
     * @param pblnAddParityToClass Whether to add parity to the class name. 
     * @param pblnOdd Whether this is a odd row 
     * @param pstrDefaultClass The default class name to use, 
     * @return Returns the class name.
     */
    private String constructClass(String pstrClass, 
                                  boolean pblnAddParityToClass, 
                                  boolean pblnOdd, 
                                  String pstrDefaultClass) {
        String constructClass = "";
        
        /**
         * Handle defaults :
         */ 
        if (pstrDefaultClass == null) {
			pstrDefaultClass = "zx";
		}
        
        try {
            constructClass = getPageflow().resolveDirector(pstrClass);
        } catch (ZXException e) {
        	/**
        	 * Log error but continue exception, this is a none fatal exception, but 
        	 * should have been found during development
        	 */
        	getZx().trace.addError("Failed to resolve director for css class", e);
        }
        
        /**
         * Fall back to the default class.
         */
        if (StringUtil.len(constructClass) == 0) {
			if (pblnAddParityToClass) {
				constructClass = pstrDefaultClass;
			} else {
				constructClass = pstrDefaultClass + "Odd";
			}
		}
        
        if (pblnAddParityToClass) {
			if (pblnOdd) {
				constructClass = constructClass + "Odd";
			} else {
				constructClass = constructClass + "Even";
			}
		}

        return constructClass;

    }

    /**
     * Construct a popup menu for the category, including every row in that category.
     *
     * @param pobjCategory The category to display 
     * @param pintCell The cell to display. 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if constructCategoryPopup fails. 
     */
    private zXType.rc constructCategoryPopup(PFCalendarCategory pobjCategory, int pintCell) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjCategory", pobjCategory);
            getZx().trace.traceParam("pintCell", pintCell);
        }
        
        zXType.rc constructCategoryPopup = zXType.rc.rcOK;

        try {

            String strPopupName = pobjCategory.getName() + pintCell;

            /**
             * Now generate the popup menu javascript.
             */
            boolean blnStartMenu = true;

            // I presume it is an array list of business objects ?
            ArrayList arrCategory = pobjCategory.getRows()[pintCell];
            ZXBO objBO;

            int intCategoryRows = pobjCategory.getRows()[pintCell].size();
            for (int i = 0; i < intCategoryRows; i++) {
                objBO = (ZXBO) arrCategory.get(i);

                if (blnStartMenu) {
                    blnStartMenu = false;

                    /**
                     * Start new entire popup menu:
                     */
                    getPageflow().getPage().popupMenuStart(strPopupName);
                }

                getPageflow().getPage().popupMenuOption(strPopupName, 
                										objBO.formattedString("Label"),
                										constructPopupRowURL(pobjCategory.getUrl()), 
                										"", 
                										"", 
                										false);
                
            }

            if (!blnStartMenu) {
				/**
                 * We found at least one row, so end the popup
                 */
                getPageflow().getPage().popupMenuEnd();
			}

            constructCategoryPopup = zXType.rc.rcOK;

            return constructCategoryPopup;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Construct a popup menu for the category, including every row in that category.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjCategory = " + pobjCategory);
                getZx().log.error("Parameter : pintCell = " + pintCell);
            }
            
            if (getZx().throwException) {
				throw new ZXException(e);
			}
            constructCategoryPopup = zXType.rc.rcError;
            return constructCategoryPopup;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(constructCategoryPopup);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Construct a popup menu for the category, including every row in that category.
     *
     * @param pobjCategory The category to display 
     * @param pintCell The cell the draw 
     * @return Returns the popup string.
     * @throws ZXException Thrown if constructCategoryUrl fails. 
     */
    private String constructCategoryUrl(PFCalendarCategory pobjCategory, int pintCell) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjCategory", pobjCategory);
            getZx().trace.traceParam("pintCell", pintCell);
        }

        String constructCategoryUrl = "";
        
        try {

            String strPopupName = pobjCategory.getName() + pintCell;

            PFUrl objUrl = new PFUrl();
            objUrl.setUrlType(zXType.pageflowUrlType.putPopup);
            objUrl.setUrl(strPopupName);

            constructCategoryUrl = getPageflow().constructURL(objUrl);

            return constructCategoryUrl;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Construct a popup menu for the category, including every row in that category.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjCategory = " + pobjCategory);
                getZx().log.error("Parameter : pintCell = " + pintCell);
            }
            
            if (getZx().throwException) {
				throw new ZXException(e);
			}
            return constructCategoryUrl;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(constructCategoryUrl);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Calls, constructPopupRowURL(pobjUrl, "");
     * 
     * @param pobjUrl The url to display 
     * @return Returns 
     * @throws ZXException Thrown if constructPopupRowURL fails. 
     */
    private String constructPopupRowURL(PFUrl pobjUrl) throws ZXException {
        return constructPopupRowURL(pobjUrl, "");
    }

    /**
     * Determine what the URL should be for this row (a popup). Note that this function is similar.
     * to the normal constructRowURL but uses the given URL rather than that of the listform itself.
     * We use URL object to get the main url (this has come from a popup ref url, not the listform's url)..
     *
     * @param pobjUrl The url to display 
     * @param pstrConfirm Optional confirmation message to display. 
     * @return Returns Popuprow url.
     * @throws ZXException Thrown if constructPopupRowURL fails. 
     */
    private String constructPopupRowURL(PFUrl pobjUrl, String pstrConfirm) throws ZXException {
        if (getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjUrl", pobjUrl);
            getZx().trace.traceParam("pstrConfirm", pstrConfirm);
        }
        
        String constructPopupRowURL = "";

        /**
         * Handle defaults
         */
        if (pstrConfirm == null) {
			pstrConfirm = "";
		}
        
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
             */
            constructPopupRowURL = getPageflow().wrapRefUrl(getPageflow().resolveDirector(pobjUrl.getFrameno()),
										                	constructPopupRowURL,
										                	pstrConfirm,
										                	false,
										                	true);
            
            return constructPopupRowURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine what the URL should be for this row (a popup). Note that this function is similar", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjUrl = " + pobjUrl);
                getZx().log.error("Parameter : pstrConfirm = " + pstrConfirm);
            }
            
            if (getZx().throwException) {
				throw new ZXException(e);
			}
            return constructPopupRowURL;
        } finally {
            if (getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(constructPopupRowURL);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Determine what the URL should be for this row.
     *
     * @param pobjUrl The url to print. 
     * @param pstrID The id of the row. 
     * @return Returns what the URL should be for this row.
     * @throws ZXException Thrown if constructRowURL fails. 
     */
    private String constructRowURL(PFUrl pobjUrl, String pstrID) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjUrl", pobjUrl);
            getZx().trace.traceParam("pstrID", pstrID);
        }

        String constructRowURL = ""; 
        
        try {
            
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
                
                int intPopups = getPopups().size();
                for (int i = 0; i < intPopups; i++) {
                    objRef = (PFRef)getPopups().get(i);
                    
                    /**
                     * Only interested in popups for this name...
                     */
                    if (strPopupName.equalsIgnoreCase(objRef.getName())) {
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
								                                    constructPopupRowURL(objRef.getUrl(),
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
                String strFrameno = getPageflow().resolveDirector(pobjUrl.getFrameno());
                constructRowURL = getPageflow().constructURL(pobjUrl); 
                constructRowURL = getPageflow().wrapRefUrl(strFrameno, 
                        								   constructRowURL, 
                        								   "", false, false);
            }
            
            return constructRowURL;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Determine what the URL should be for this row.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjUrl = "+ pobjUrl);
                getZx().log.error("Parameter : pstrID = "+ pstrID);
            }
            
            if (getZx().throwException) {
				throw new ZXException(e);
			}
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
     * Process action.
     * 
     * <pre>
     * 
     * Reviewed for V1.5:65 - Support for parameter-bags
     * Reviewed for V1.5:75 - Added support for cell URL
     * Reviewed for V1.5:76 - Fixed bug with quarter zoom factor
     * Reviewed for V1.5:84 - Fixed bug with active expression in cell URL
     * </pre>
     * 
     * @see org.zxframework.web.PFAction#go()
     **/
    public zXType.rc go() throws ZXException {
    	if (getZx().trace.isApplicationTraceEnabled()) {
			getZx().trace.enterMethod();
		}
    	
        zXType.rc go = zXType.rc.rcOK;
        
        DSRS objRs = null;
        
        try {
            PFEntity objEntity;			// Loop over variables
            PFEntity objTheEntity;		// Channels only support one entity
            String strQuery = "";
            
            String strCellURL = ""; 	// URL associated with cell
            Date datEndDatePrevCell = null;	// Keep track of end date of previous cell
            
            /**
             * Get relevant entities and store in context
             */
            ZXCollection colEntities = getPageflow().getEntityCollection(this, 
                                                                        zXType.pageflowActionType.patCalendar, 
                                                                        zXType.pageflowQueryType.pqtAll);
            if (colEntities == null) {
                getZx().trace.addError("Unable to get entities for action");
                go = zXType.rc.rcError;
                return go;
            }
            
            getPageflow().setContextEntities(colEntities);
            
            objTheEntity = (PFEntity)colEntities.iterator().next();
            getPageflow().setContextEntity(objTheEntity);
            
            /**
             * Make sure we do not break any rules
             */
            if (!getPageflow().validDataSourceEntities(colEntities)) {
            	getZx().trace.addError("Unsupported combination of data-source handlers");
                go = zXType.rc.rcError;
                return go;
			}
            
            /**
             * And get data-source handler
             */
            DSHandler objDSHandler = objTheEntity.getDSHandler();
            boolean blnIsDSChannel = objDSHandler.getDsType().pos == zXType.dsType.dstChannel.pos;
            
            /**
             * Handle any parameter-bag URL entries
             */
            PFUrl objUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getUrl());
            PFUrl objPrevUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getPrevurl());
            PFUrl objNextUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getNexturl());
            PFUrl objZoomUrl = getPageflow().tryToResolveParameterDirectorAsUrl(getZoomurl());
            
            /**
             * Determine the maximum number of rows to display
             */
            int intMaxResultRows;
            if (getMaxrows() > 0) {
				intMaxResultRows = getMaxrows();
			} else if (getPageflow().getPage().getWebSettings().getMaxListRows() > 0) {
				intMaxResultRows = getPageflow().getPage().getWebSettings().getMaxListRows();
			} else {
				intMaxResultRows = 150;
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
             * Get anchor entity from entity collection
             */
            String strAnchorEntity = getPageflow().resolveDirector(getAnchorentity());
            String strAnchorAttr = getPageflow().resolveDirector(getAnchorattr());
            
            ZXBO objIBOAnchorEntity = null;
            Attribute objAnchorAttr = null;
            
            Iterator iter = colEntities.iterator();
            while (iter.hasNext()) {
                objEntity = (PFEntity)iter.next();
                
                if (strAnchorEntity.equalsIgnoreCase(objEntity.getName())) {
                    objIBOAnchorEntity = objEntity.getBo();
                    objAnchorAttr = objIBOAnchorEntity.getDescriptor().getAttribute(strAnchorAttr);
                }
            }
            
            if (objAnchorAttr == null) {
            	getZx().trace.addError("No anchor attribute found", strAnchorAttr);
                go = zXType.rc.rcError;
                return go;
            }
            
            /**
             * There is a special QS parameter for the 'current date'. If it is not set assume
             * today. Once set it can be used as the basis of 'prev' and 'next' calculations,
             * so having derived it, add it into the quick context for immediate availability
             * to other directors etc. such as me.currentDate
             * There is also a date that defines the current date (stays the same regardless of
             * paging next or previous). Defaults to today
             */
            Date datStartCurrentDate;
            
            /** 
             * Unless otherwise specified that DateFormat is the zX Database Date format. 
             * 
             * This dateFormat is reused through out. 
             */
            DateFormat dateFormat = getZx().getDateFormat();
            
            /** NOTE : current date has to conform to the zx date format. **/
            String strCurrentDate = getPageflow().resolveDirector(getCurrentdate());
            if (StringUtil.len(strCurrentDate) > 0 && DateUtil.isValid(getZx().getDateFormat(), strCurrentDate)) {
				/**
                 * DGS01NOV2004: Make sure any given date has no time component.
                 * Calendar should only deal in whole days.
                 */
                datStartCurrentDate = DateUtil.parse(dateFormat, strCurrentDate);
			} else {
				datStartCurrentDate = new Date();
			}
            
            Date datThisCurrentDate;
            String strTmp = getZx().getQuickContext().getEntry("-zXCalendarStartDate");
            if (StringUtil.len(strTmp) > 0 && DateUtil.isValid(getZx().getDateFormat(), strTmp)) {
				/**
                 * DGS01NOV2004: Make sure any given date has no time component.
                 * Calendar should only deal in whole days.
                 */
                datThisCurrentDate = DateUtil.parse(dateFormat, strTmp);
			} else {
                datThisCurrentDate = datStartCurrentDate;
                getZx().getQuickContext().setEntry("-zXCalendarStartDate", dateFormat.format(datStartCurrentDate));
                
            }
            
            /**
             * Start period is where the visible calendar starts. If not set, we choose something
             * sensible e.g. January of the same year as current for a yearly calendar.
             */
            String strStartPeriod = getPageflow().resolveDirector(getStartperiod());
            
            /**
             * Get zoom factor. Default is Month (2)
             */
            int intZoomFactor;
            String strZoomFactor = getZx().getQuickContext().getEntry("-zXCalendarZoom");
            if (StringUtil.len(strZoomFactor) == 0) {
				strZoomFactor = getPageflow().resolveDirector(getZoomfactor());
			}
            if (!StringUtil.isNumeric(strZoomFactor)) {
				intZoomFactor = PFCalendar.ZL_MONTH;
			} else {
				intZoomFactor = Integer.parseInt(strZoomFactor);
			}
            
            /**
             * Try to get the label width.
             * Label width defaults to the listForm Column one setting.
             */
            String strLabelFormat = getLabelformat();
            String strLabelWidth = getLabelwidth();
            
            /**
             * This is not used at the moment :
             */
            if (StringUtil.len(strLabelWidth) == 0) {
				strLabelWidth = getPageflow().getPage().getWebSettings().getListFormColumn1();
			}
            /**
             * Try do a good guess of labelWidth, if labelFormat and labelWidth is not set
             */
            if (StringUtil.len(getLabelformat()) == 0 && StringUtil.len(getLabelwidth()) == 0) {
				if (intZoomFactor == PFCalendar.ZL_MONTH) {
					strLabelWidth = "15";
				} else if (intZoomFactor == PFCalendar.ZL_WEEK) {
					strLabelWidth = "100";
				} else if (intZoomFactor == PFCalendar.ZL_QUARTLY) {
					strLabelWidth = "230";
				} else if (intZoomFactor == PFCalendar.ZL_HALF) {
					strLabelWidth= "70";
				} else if (intZoomFactor == PFCalendar.ZL_YEARLY) {
					strLabelWidth = "70";
				} else {
					strLabelWidth = "20%";
				}
			} else {
				strLabelWidth = getLabelwidth();
			}
            
            /**
             * Get the rowspercells, default to 1.
             * To avoid ugly errors, make sure the rows per cell cannot be zero.
             */
            int intRowsPerCell = getRowspercell();
            if (intRowsPerCell == 0) {
				intRowsPerCell = 1;
			}
            
            String strColWidth = getColwidth();
            
            /**
             * Default so a height so the cell are printed out nicely.
             */
            String strMinHeight = getMinheight();
            if (StringUtil.len(strMinHeight) == 0) {
				strMinHeight = "5";
			}
            
            /**
             * Now we set various parameters and working variables depending on the zoom factor
             */
            // The interval between each cell. This is based on the Calendar static vars
            int intInterval; 
            int intCalendarCells;
            int intColumnCount;
            Date datStartCurrentCell;
            Date datStartSelect;
            Date datEndSelect;
            Date datPrevDate;
            Date datNextDate;
            Calendar cal = Calendar.getInstance();
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            
            switch (intZoomFactor) {
                case ZL_WEEK:                           //  Week
                    intInterval = Calendar.DAY_OF_YEAR; // Each cell is a day
                    intCalendarCells = 7;               // Will be 7 cells in the object model
                    intColumnCount = getOrienTation().equals(zXType.displayOrientation.doHorizontal)?7:1; // Get the number of columns.
                    
                    /**
                     * Get start and end of day that current date lies within
                     */
                    datStartCurrentCell = datStartCurrentDate;
                    /**
                     * Get start and end of week that is to be shown. Must start on a Monday.
                     * Ends 7 days later one second before midnight.
                     * Previous and Next will move one week earlier and later
                     */
                    cal.setTime(datThisCurrentDate);
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    datStartSelect = cal.getTime();
                    
                    cal.setTime(datStartSelect);
                    cal.add(Calendar.DATE, 7);
                    cal.add(Calendar.SECOND, -1);
                    datEndSelect = cal.getTime();
                    
                    /**
                     * Get the date for the next button
                     */
                    cal.setTime(datThisCurrentDate);
                    cal.add(Calendar.DAY_OF_MONTH, -7);
                    datPrevDate = cal.getTime();
                    
                    /**
                     * Get the date for the previous button
                     */
                    cal.setTime(datThisCurrentDate);
                    cal.add(Calendar.DAY_OF_MONTH, +7);
                    datNextDate = cal.getTime();
                    
                    /**
                     * Can override default of showing the day and date
                     */
                    if (StringUtil.len(strLabelFormat) == 0) {
						// EEEE - Name of day of week eg : Monday
                        // d - Day of the month. eg: 5
                        strLabelFormat = "EEEE d";
					}
                    
                    break;
                    
                case ZL_MONTH: //  Month
                    intInterval = Calendar.DAY_OF_YEAR; // Each cell is a day
                    intColumnCount = 7;                 // Always same format for month calendar, no horiz/vert difference
                    setOrienTation(zXType.displayOrientation.doVertical);
                    
                    /**
                     * Get start and end of day that current date lies within
                     */
                    datStartCurrentCell = datStartCurrentDate;
                    /**
                     * Get start and end of month that is to be shown. Must start on the first.
                     */
                    cal.setTime(datThisCurrentDate);
                    cal.add(Calendar.DATE, - (cal.get(Calendar.DAY_OF_MONTH) - 1));
                    datStartSelect = cal.getTime();
                    
                    cal.setTime(datStartSelect);
                    cal.add(Calendar.MONTH, 1);
                    cal.add(Calendar.SECOND, -1);
                    datEndSelect = cal.getTime();
                    
                    /**
                     * BD24OCT04 - Could the s be a mistake?
                     * DGS01NOV2004: It was wrong when the datThisCurrentDate had a time component,
                     * but now it doesn't, so the s is ok
                     */
                    cal.setTime(datThisCurrentDate);
                    cal.add(Calendar.MONTH, -1);
                    datPrevDate = cal.getTime();
                    
                    cal.setTime(datThisCurrentDate);
                    cal.add(Calendar.MONTH, 1);
                    datNextDate = cal.getTime();
                    
                    /**
                     * Can override default of showing the day number
                     */
                    if (StringUtil.len(strLabelFormat) == 0) {
						strLabelFormat = "d";
					}
                    /**
                     * Will create an empty object model of 28-31 cells (one per actual day in month)
                     */
                    cal.setTime(datEndSelect);
                    intCalendarCells = cal.get(Calendar.DAY_OF_MONTH);
                    
                    break;
                    
                case ZL_QUARTLY: //  Quarterly
                    intInterval = Calendar.WEEK_OF_YEAR;    // Each cell is a week
                    intColumnCount = 1;                     // Always same format for quarter calendar, no horiz/vert difference
                    setOrienTation(zXType.displayOrientation.doVertical);
                    
                    /**
                     * Get start and end of week that current date lies within. DatePart of "w" gives
                     * the day of the week. We subtract this from the current date to move back to
                     * Monday (although we subtract 2 less, as the "w" week starts on Sunday
                     * whereas to us it's Monday). E.g. if today is Thursday 8 Jan, we want to start
                     * on Monday 5 Jan, so we get "w"=5, reduce it by 2 to 3, subtract that from the
                     * date gives 5 Jan.
                     */
                    cal.setTime(datStartCurrentDate);
                    cal.add(Calendar.DAY_OF_WEEK, - (cal.get(Calendar.DAY_OF_WEEK) - 1)); // Was -2 in VB
                    datStartCurrentCell = cal.getTime();
                    
                    /**
                     * Get start and end of quarter that is to be shown. The current month lies within
                     * one of the four quarters i.e. months 1-3 = q1, 4-6 = q2 and so on.
                     * Having got the quarter we then get the month that starts that quarter (1, 4, etc)
                     * Our date range is from the first of that month to three months later.
                     */
                    cal.setTime(datThisCurrentDate);
                    int intQuarter = (cal.get(Calendar.MONTH) + 1)  / 3;
                    int intMonth = intQuarter * 3;
                    
                    /**
                     * Get beginning of the quarter
                     */
                    cal.setTime(datThisCurrentDate);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    /** NOTE : MONTH field starts from 0 in java 1 */
                    cal.set(Calendar.MONTH, intMonth);
                    datStartSelect = cal.getTime();
                    
                    /**
                     * Get the end of the quarter
                     */
                    cal.setTime(datStartSelect);
                    cal.add(Calendar.MONTH, 3);
                    cal.add(Calendar.SECOND, -1);
                    datEndSelect = cal.getTime();
                    
                    /**
                     * Date of the previous quarter
                     */
                    cal.setTime(datThisCurrentDate);
                    cal.add(Calendar.MONTH, -3);
                    datPrevDate = cal.getTime();
                    
                    /**
                     * Date of the next quarter
                     */
                    cal.setTime(datThisCurrentDate);
                    cal.add(Calendar.MONTH, 3);
                    datNextDate = cal.getTime();
                    
                    /**
                     * Can override default appearance. Must escape the W in literal Week in a format
                     */
                    if (StringUtil.len(strLabelFormat) == 0) {
						// w - week in a year
                        // W - week in a month
                        strLabelFormat = "'Week' w";
                    }
                    
                    /**
                     * Will create an empty object model of cells, one per week in the quarter
                     */
                    cal.setTimeInMillis(datEndSelect.getTime());
                    cal.setMinimalDaysInFirstWeek(getMinimalDaysInFirstWeek(datEndSelect));
                    int intEndSelect = cal.get(Calendar.WEEK_OF_YEAR);
                    cal.setTimeInMillis(datStartSelect.getTime());
                    cal.setMinimalDaysInFirstWeek(getMinimalDaysInFirstWeek(datStartSelect));
                    int intStartSelect= cal.get(Calendar.WEEK_OF_YEAR);
                    
                    intCalendarCells = intEndSelect - intStartSelect + 1;
                    
                    // Quick. Does not always work
                    //intCalendarCells = 14;
                    
                    break;
                    
                default: // Half year/Year
                    // Half year, Year ar treated the same
                    intInterval = Calendar.MONTH;   // Each cell is a month
                    /**
                     * Will create an empty object model of 6 or 12 cells, one per month
                     */
                	if (intZoomFactor == PFCalendar.ZL_HALF) {
						intCalendarCells = 6; // Half yearly
					} else {
						intCalendarCells = 12; // Yearly
					}
                	intColumnCount = getOrienTation().equals(zXType.displayOrientation.doHorizontal)?intCalendarCells:1;
                	
                	/**
                	 * Get start and end of month that current date lies within
                	 */
                	cal.setTime(datStartCurrentDate);
                	cal.add(Calendar.DAY_OF_MONTH, - (cal.get(Calendar.DAY_OF_MONTH) - 1));
                	datStartCurrentCell = cal.getTime();
                	
                	/**
                	 * Get start and end of half-year/year that is to be shown
                	 */
                	if (StringUtil.len(strStartPeriod) == 0) {
                        cal.setTime(datThisCurrentDate);
                        cal.set(Calendar.DAY_OF_YEAR, 1);
                        datStartSelect = cal.getTime();
                        
                        cal.setTimeInMillis(datThisCurrentDate.getTime());
                	    if (cal.get(Calendar.MONTH) > intCalendarCells) {
                	        cal.setTime(datStartSelect);
                	        cal.add(Calendar.MONTH, intCalendarCells);
                	        datStartSelect = cal.getTime();
                	    }
                        
                	} else {
                        // Sharing the datFormar from above. NOTE : This used the zx db date format.
                	    datStartSelect = DateUtil.parse(dateFormat, strStartPeriod);
                	    cal.setTime(datStartSelect);
                	    cal.add(Calendar.DAY_OF_YEAR, - (cal.get(Calendar.DAY_OF_YEAR) -1));
                	    datStartSelect = cal.getTime();
                        
                	}
                	
                	cal.setTime(datStartSelect);
                	cal.add(Calendar.MONTH, intCalendarCells);
                	cal.add(Calendar.SECOND, -1);
                	datEndSelect = cal.getTime();
                	
                	cal.setTime(datThisCurrentDate);
                	cal.add(Calendar.MONTH, - intCalendarCells);
                	datPrevDate = cal.getTime();
                	
                	cal.setTime(datThisCurrentDate);
                	cal.add(Calendar.MONTH, intCalendarCells);
                	datNextDate = cal.getTime();
                	
                	/**
                	 * Can override default of showing the month name
                	 */
                	if (StringUtil.len(strLabelFormat) == 0) {
						// m - Minute in hour
                        // M - Month number
                        // MMMM - Text of the month.
                	    strLabelFormat = "MMMM";
					}
                    
                    break;
            } // Zoom factor
            
            /**
             * Create an empty object model of cells
             */
            ArrayList colCalendarCells = new ArrayList(intCalendarCells);
            int intCell;
            CalendarCell objCalendarCell;
            
            for (intCell = 0; intCell < intCalendarCells; intCell++) { // intCell = 0, Arrays and ArrayList are 0 based.
                objCalendarCell = new CalendarCell();
                
                cal.setTime(datStartSelect);
                //NOTE : Calendar.MONTH starts from 0, not 1 like the rest.
                cal.add(intInterval, intCell);
                objCalendarCell.setDateStart(cal.getTime());
                
                /**
                 * The end of the period must go all the way up to 23:59:59
                 */
                cal.setTime(objCalendarCell.getDateStart());
                cal.add(intInterval, 1);
                cal.add(Calendar.SECOND, -1);
                objCalendarCell.setDateEnd(cal.getTime());
                
                if (intZoomFactor == PFCalendar.ZL_QUARTLY) {
                    /**
                     * Handle Quartly as a special case :
                     */
                	
                    /**
                     * Many changes in this area for V1.5:76
                     * Quarter zoom is tricky as we are dealing with weeks in the cells
                     * We get the start of the week and the end of the week. We can however not rely on
                     * the objCalendarCell.dateStart as it has been calculated above as this does not handle
                     * broken weeks (e.g. 1JAN is a Wednesday) properly; we therefore start each next week
                     * a day after the previous week was done....
                     */
                    if (intCell > 1) {
                    	/**
                    	 * Move to the next day from the previous cells end date
                    	 */
                    	cal.setTime(datEndDatePrevCell);
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    	objCalendarCell.setDateStart(getStartOfWeek(cal.getTime(), datStartSelect));
                    	
                    } else {
						objCalendarCell.setDateStart(getStartOfWeek(objCalendarCell.getDateStart(), datStartSelect));
					}
                    
                    objCalendarCell.setDateEnd(getEndOfWeek(objCalendarCell.getDateStart(), datEndSelect));
                    
                    /**
                     * We now have the last day of the week / period; now alter it to include the very last
                     * second of that period. ie : Go to the end of the day.
                     */
                    cal.setTime(objCalendarCell.getDateEnd());
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    objCalendarCell.setDateEnd(cal.getTime());
                    
                    datEndDatePrevCell = objCalendarCell.getDateEnd();
                    
                    /**
                     * Print label
                     */
                    cal = Calendar.getInstance();
                    cal.setFirstDayOfWeek(Calendar.MONDAY);
                    cal.setMinimalDaysInFirstWeek(getMinimalDaysInFirstWeek(objCalendarCell.getDateStart()));
                    
                    /** 
                     * Used for custom date formatting (IE : Label format is different for each zoom level). 
                     **/
                    DateFormat customDateFormat = new SimpleDateFormat(strLabelFormat);
                    
                    customDateFormat.setCalendar(cal);
                    strTmp = customDateFormat.format(objCalendarCell.getDateStart()) + " ";
                    
                    customDateFormat = new SimpleDateFormat("MMMM d");
                    strTmp = strTmp + customDateFormat.format(objCalendarCell.getDateStart()) 
                             + " - " + customDateFormat.format(objCalendarCell.getDateEnd());
                    
                    objCalendarCell.setCellLabel(strTmp);
                    
                } else {
                    /** 
                     * Used for custom date formatting (IE : Label format is different for each zoom level). 
                     **/
                    DateFormat customDateFormat = new SimpleDateFormat(strLabelFormat);
                    objCalendarCell.setCellLabel(customDateFormat.format(objCalendarCell.getDateStart()));
                    
                } // Zoom factor other than quarter
                
                objCalendarCell.setMoreRows(false);
                objCalendarCell.setCurrent(datStartCurrentCell.compareTo(objCalendarCell.getDateStart()) == 0);
                
                colCalendarCells.add(intCell, objCalendarCell);
                
            } // For each calendar-cell
            
            /**
             * Save more significant dates in the quick context
             */
            getZx().getQuickContext().setEntry("-zXCalendarStartPeriod", dateFormat.format(datStartSelect));
            getZx().getQuickContext().setEntry("-zXCalendarEndPeriod", dateFormat.format(datEndSelect));
            
            /**
             * Get query and print as debug if needed
             */
            
            /**
             * Construct the query to use
             */
            String strQueryName = getPageflow().resolveDirector(getQueryname());
            
            String strSelectClause = getPageflow().getFromContext(strQueryName + Pageflow.QRYSELECTCLAUSE);
            String strWhereClause = getPageflow().getFromContext(strQueryName + Pageflow.QRYWHERECLAUSE);
            String strOrderByClause = getPageflow().getFromContext(strQueryName + Pageflow.QRYORDERBYCLAUSE);
            
            if (blnIsDSChannel) {
                DSWhereClause objDSWhereClause = new DSWhereClause();
                
                if (StringUtil.len(strWhereClause) > 0) {
					objDSWhereClause.parse(objIBOAnchorEntity, ":" + strWhereClause);
				}
                
                /**
                 * And add <anchor date> in range
                 */
                objDSWhereClause.singleWhereCondition(objIBOAnchorEntity,
                                                      objAnchorAttr,
                                                      zXType.compareOperand.coGE,
                                                      new DateProperty(datStartSelect),
                                                      zXType.dsWhereConditionOperator.dswcoAnd); // Add to a AND statement
                
                objDSWhereClause.singleWhereCondition(objIBOAnchorEntity,
                                                      objAnchorAttr,
                                                      zXType.compareOperand.coLE,
                                                      new DateProperty(datEndSelect),
                                                      zXType.dsWhereConditionOperator.dswcoAnd); // Add to a AND statement
                /**
                 * And convert back to whereclause
                 */
                strWhereClause = ":" + objDSWhereClause.getAsWhereClause();
                
                /**
                 * Handle order by
                 */
                boolean blnReverse = false;
                if (StringUtil.len(strOrderByClause) > 1 && strOrderByClause.charAt(0) == '-') {
                    strOrderByClause = strOrderByClause.substring(1);
                    blnReverse = true;
                }
                
                /**
                 * Turn query into recordset
                 */
                objRs = objDSHandler.boRS(objTheEntity.getBo(), 
                                          objTheEntity.getSelectlistgroup(),
                                          strWhereClause,
                                          isResolvefk(),
                                          strOrderByClause, blnReverse,
                                          lngStartRow, lngBatchSize); // Limit support
                
            } else {
                /**
                 * BD24OCT04 - Fixed problem when already having a where clause
                 */
                if (StringUtil.len(strWhereClause) > 0) {
					strWhereClause = strWhereClause + " AND ";
				}
                
                strWhereClause = strWhereClause + getZx().getSql().singleWhereCondition(objIBOAnchorEntity, 
                                                                                        objAnchorAttr, 
                                                                                        zXType.compareOperand.coGE,
                                                                                        new DateProperty(datStartSelect, false));
                strWhereClause = strWhereClause + " AND " + getZx().getSql().singleWhereCondition(objIBOAnchorEntity,
                                                                                                  objAnchorAttr, 
                                                                                                  zXType.compareOperand.coLE,
                                                                                                  new DateProperty(datEndSelect, false));
                strQuery = strSelectClause;
                strQuery = strQuery + " AND " + strWhereClause;
                
                if (StringUtil.len(strOrderByClause) > 0) {
					strQuery = strQuery + " ORDER BY " + strOrderByClause;
				}
                
                if (getPageflow().isDebugOn()) {
					getPageflow().getPage().debugMsg(strQuery);
				}
                
                /**
                 * Turn query into recordset
                 */
                objRs = ((DSHRdbms)objDSHandler).sqlRS(strQuery, 
                                                       lngStartRow, lngBatchSize);
                
            } // Channel or RDBMS
            
            if (objRs == null) {
                getZx().trace.addError("Unable to execute query");
                throw new ZXException("Unable to execute query");
            }
            
            /**
             * And generate each position in the collection (e.q. 12 for a year, 31 for a month)
             */
            int intRowCount = 1;
            PFCalendarCategory objCategory;
            ArrayList colRowBOs;
            PFEntity objEntity2;
            Iterator iter2;
            ZXBO objIBO;
            
            /**
             * Get the number of categories as soon as possible to increase performance.
             */
            int intCategories = 0;
            if (getCategories() != null) {
				intCategories = getCategories().size();
			}
            
            while (!objRs.eof() && intRowCount <= intMaxResultRows) {
                intRowCount++;
                
                /**
                 * Populate all the business objects...
                 */
                iter = colEntities.iterator();
                while (iter.hasNext()) {
                    objEntity = (PFEntity)iter.next();
                    
                    objRs.rs2obj(objEntity.getBo(), objEntity.getSelectlistgroup(), isResolvefk());
                }
                
                /**
                 * Maybe the row is not 'active' at all. Default is Active.
                 */
                if (getPageflow().isActive(getActive())) {
                    
                    iter = colEntities.iterator();
                    while (iter.hasNext()) {
                        objEntity = (PFEntity)iter.next();
                        
                        if (strAnchorEntity.equalsIgnoreCase(objEntity.getName())) {
                            
                            intCell = 0; // intCell = 0; Arrays and Arraylists are 0 based.
                            for (int i = 0; i < intCalendarCells; i++) {
                                objCalendarCell = (CalendarCell)colCalendarCells.get(i);
                                intCell++;
                                
                                if (objCalendarCell.getDateStart().compareTo(objEntity.getBo().getValue(objAnchorAttr.getName()).dateValue()) <= 0 &&
                                    objCalendarCell.getDateEnd().compareTo(objEntity.getBo().getValue(objAnchorAttr.getName()).dateValue()) >= 0 ) {
                                    
                                    if (getCellMode().equals(zXType.pageflowCellMode.pcmList)) {
                                        
                                        objCalendarCell.setRowCount(objCalendarCell.getRowCount() + 1);
                                        
                                        if (objCalendarCell.getRows().size() >= intRowsPerCell) {
											objCalendarCell.setMoreRows(true);
										} else {
                                            /**
                                             * Copy each BO and contents to collection that is
                                             * stored for the row within the cell
                                             */
                                            colRowBOs = new ArrayList();
                                            iter2 = colEntities.iterator();
                                            while (iter2.hasNext()) {
                                                objEntity2 = (PFEntity)iter2.next();
                                                objIBO = objEntity2.getBo().cloneBO("*");
                                                colRowBOs.add(objIBO);
                                            }
                                            objCalendarCell.getRows().add(colRowBOs);
                                            
                                        }
                                        
                                    } else {
										for (int j = 0; j < intCategories; j++) {
                                            
                                            objCategory = (PFCalendarCategory)getCategories().get(j);
                                            
                                            if (getPageflow().isActive(objCategory.getExpression())) {
                                                
                                                objCalendarCell.setRowCount(objCalendarCell.getRowCount() + 1);
                                                objCategory.getRowCount()[intCell] = objCategory.getRowCount()[intCell] + 1;
                                                
                                                if (getCellMode().equals(zXType.pageflowCellMode.pcmPopup)) {
													if (objCategory.getRowCount()[intCell] >= intRowsPerCell) {
														objCalendarCell.setMoreRows(true);
													} else {
                                                        /**
                                                         * Copy each BO and contents to collection that is
                                                         * stored for the row within the cell
                                                         */
                                                        colRowBOs = new ArrayList();
                                                        iter2 = colEntities.iterator();
                                                        while (iter2.hasNext()) {
                                                            objEntity2 = (PFEntity)iter2.next();
                                                            objIBO = objEntity2.getBo().cloneBO("*");
                                                            colRowBOs.add(objIBO);
                                                        }
                                                        objCalendarCell.getRows().add(colRowBOs);
                                                    }
												}
                                            }
                                            
                                            // Stop looking for a category
                                            break;
                                        }
									}
                                    
                                    // No need to look through remaining cells
                                    break;
                                }
                            } // Cell
                            
                        } // is the anchor entity
                        
                    } // entity
                    
                } // is active?
                
                objRs.moveNext();
                
            } // Through RS rows
            
            // ------------------------------------------------ GUI Stuff
            
            /**
             * Messages, narrative, title etc. Do this now, before any URLs can change QS values
             */
            getPageflow().getPage().formTitle(zXType.webFormType.wftList, getPageflow().resolveLabel(getTitle()));
            getPageflow().processMessages(this);
            String strCalendarTitle = getPageflow().resolveLabel(getCalendartitle());
            
            /**
             * Determine the url associated with the prev and next buttons
             */
            getZx().getQuickContext().setEntry("-zXCalendarStartDate", dateFormat.format(datPrevDate));
            String strPrevUrl = constructRowURL(objPrevUrl, "PREV");
            getZx().getQuickContext().setEntry("-zXCalendarStartDate", dateFormat.format(datNextDate));
            String strNextUrl = constructRowURL(objNextUrl, "NEXT");
            getZx().getQuickContext().setEntry("-zXCalendarStartDate", dateFormat.format(datThisCurrentDate));
            
            if (getCellMode().equals(zXType.pageflowCellMode.pcmPopup)) {
                /**
                 * Generate any javascript for dynamic popups
                 */
                intCell = 0;
                for (int i = 0; i < intCalendarCells; i++) {
                    for (int j = 0; j < intCategories; j++) {
                        objCategory = (PFCalendarCategory)getCategories().get(j);
                        if (objCategory.getRowCount()[intCell] > 0 && objCategory.isShowWhenZero()) {
                            getZx().getQuickContext().setEntry("-zXCategoryCount", String.valueOf(objCategory.getRowCount()[intCell]));
                            constructCategoryPopup(objCategory, intCell);
                        } 
                    } // Loop over rows for this cell
                    
                    intCell++;
                }
                
            }
            
            //------------------------------------------------ START OF HEADER
            
            /**
             * Open table
             */
            getPageflow().getPage().s.append("<table ")
            						 .appendAttr("width", StringUtil.len(getWidth()) > 0?getWidth():"100%")
            						 .appendAttr("cellPadding", 0)
            						 .appendAttr("cellSpacing", 0)
            						 .appendNL('>');
            
            
            getPageflow().getPage().s.append("<tr ")
            						 .appendAttr("class", "zXCalendarTitle")
            						 .appendNL('>');
            
            /**
             * Only add prev and next buttons if there is an appropriate url
             */
            if (StringUtil.len(strPrevUrl) > 0) {
				getPageflow().getPage().s.append("<td ")
										 .appendAttr("align", "left")
										 .appendAttr("width", "10%")
										 .append(">").appendNL()
										 
										 .append("<img ")
										 .appendAttr("src", "../images/calendarPrev.gif")
										 .appendAttr("alt", "Previous")
										 .appendAttr("title", "Previous")
										 .appendAttr("onMouseDown", strPrevUrl)
										 .appendAttr("onMouseOver", "javascript:this.src='../images/calendarPrevOver.gif'")
										 .appendAttr("onMouseOut", "javascript:this.src='../images/calendarPrev.gif'")
										 .append("\n></td>").appendNL();
			}
            
            getPageflow().getPage().s.append("<td ")
            						 .appendAttr("align", "center")
            						 .appendAttr("width", "*")
            						 .appendNL('>');
            
            getPageflow().getPage().s.appendNL(strCalendarTitle);
            
            getPageflow().getPage().s.appendNL("</td>");
            
            /**
             * Only show zoom buttons if there is an appropriate url
             */
            String strZoomUrl = objZoomUrl.getUrl();
            if (StringUtil.len(strZoomUrl) > 0) {
                
                getPageflow().getPage().s.append("<td ")
                						 .appendAttr("align", "center")
                						 .appendAttr("width", "20%")
                						 .appendNL('>');
                
                /**
                 * If not fully zoomed in, make the zoom in button url have a zoom in effect
                 */
                if (intZoomFactor > 1) {
                    getZx().getQuickContext().setEntry("-zXCalendarZoom", "" + (intZoomFactor - 1));
                    strZoomUrl = constructRowURL(objZoomUrl, "Zoom" + (intZoomFactor - 1));
                }
                
                /**
                 * Always show the zoom in image, but it only responds if not fully zoomed in
                 */
                getPageflow().getPage().s.append("<img ")
                			             .appendAttr("src", "../images/calendarZoomIn.gif");
                
                if (intZoomFactor > 1) {
					getPageflow().getPage().s.appendAttr("alt", "View by " + getZoomfactorAsString(intZoomFactor - 1))
											 .appendAttr("title", "View by " + getZoomfactorAsString(intZoomFactor - 1))
											 .appendAttr("onMouseDown", strZoomUrl)
											 .appendAttr("onMouseOver", "javascript:this.src='../images/calendarZoomInOver.gif'")
											 .appendAttr("onMouseOut", "javascript:this.src='../images/calendarZoomIn.gif'");
				}
                
                getPageflow().getPage().s.appendNL('>');
                
                /**
                 * Always show what the current zoom factor is, although this button does not
                 * respond to anything
                 */
                getPageflow().getPage().s.append("<img ")
                	    .appendAttr("src", "../images/calendarZoom" + intZoomFactor + ".gif")
                	    .appendAttr("alt", "Viewing by " + getZoomfactorAsString(intZoomFactor))
                	    .appendAttr("title", "Viewing by " + getZoomfactorAsString(intZoomFactor))
                	    .appendNL('>');
                
                /***
                 * If not fully zoomed out, make the zoom out button url have a zoom out effect
                 */
                if (intZoomFactor < 5) {
                    getZx().getQuickContext().setEntry("-zXCalendarZoom", "" + (intZoomFactor + 1));
                    strZoomUrl = constructRowURL(objZoomUrl, "Zoom" + (intZoomFactor + 1));
                }
                
                /**
                 * Always show the zoom out image, but it only responds if not fully zoomed out
                 */
                getPageflow().getPage().s.append("<img ")
                						.appendAttr("src", "../images/calendarZoomOut.gif");
			    if (intZoomFactor < 5) {
					getPageflow().getPage().s
		        		.appendAttr("alt", "View by " + getZoomfactorAsString(intZoomFactor + 1))
			            .appendAttr("title", "View by " + getZoomfactorAsString(intZoomFactor + 1))
			            .appendAttr("onMouseDown", strZoomUrl)
			            .appendAttr("onMouseOver", "javascript:this.src='../images/calendarZoomOutOver.gif'")
			            .appendAttr("onMouseOut", "javascript:this.src='../images/calendarZoomOut.gif'");
				}
			    getPageflow().getPage().s.appendNL('>');
			    
			    getPageflow().getPage().s.appendNL("</td>");
            }
            
            /**
             * Only show the next button if there is an appropriate url
             */
            if (StringUtil.len(strNextUrl) > 0) {
                getPageflow().getPage().s.append("<td ")
							.appendAttr("align", "right")
							.appendAttr("width", "10%")
							.appendNL('>');
                
                getPageflow().getPage().s.append("<img ")
							.appendAttr("src", "../images/calendarNext.gif")
							.appendAttr("alt", "Next")
							.appendAttr("title", "Next")
							.appendAttr("onMouseDown", strNextUrl)
							.appendAttr("onMouseOver", "javascript:this.src='../images/calendarNextOver.gif'")
							.appendAttr("onMouseOut", "javascript:this.src='../images/calendarNext.gif'")
							.appendNL('>');
                
			    getPageflow().getPage().s.appendNL("</td>");
            }
            
		    getPageflow().getPage().s.appendNL("</tr></table>");
            
            //------------------------------------------------ END OF HEADER
            
            
            //------------------------------------------------ START OF CALENDAR
            
		    getPageflow().getPage().s.append("<table ")
		            			.appendAttr("width", (StringUtil.len(getWidth()) > 0?getWidth():"100%"))
								.appendAttr("border", 1)
								.appendAttr("cellspacing", 0)
								.appendAttr("cellpadding", 0)
								.appendNL('>');
            
		    int intRow = 0;
            
            boolean blnOdd = false;
            int intColumn;
            
            /** Whether there is a cell url. */
	        boolean blnCellURL = getCellurl() != null && StringUtil.len(getCellurl().getUrl()) != 0 && !getCellurl().getUrl().equalsIgnoreCase("#dummy");

		    String strClass;
		    if (intZoomFactor == PFCalendar.ZL_MONTH || getOrienTation().equals(zXType.displayOrientation.doHorizontal)) {
                
		        if (intZoomFactor == PFCalendar.ZL_MONTH) {
					strClass = "zXCalendarNormal";
				} else {
					strClass = constructClass(getNormalclass(), isAddparitytoclass(), blnOdd, "zXCalendarNormal");
				}
                
		        getPageflow().getPage().s.append("<tr ")
		        						 .appendAttr("class", strClass)
		        						 .appendNL('>');
		        		        
		        for (intColumn = 0; intColumn < intColumnCount; intColumn++) { // intColumn = 0, Arrays and ArrayLists are 0 based.
		        	/**
		        	 * There may be an action associated with the cell
		        	 * 
		        	 * v1.5:95 BD30APR06: Set the next two earlier so that they can be referenced
		        	 */
	        		objCalendarCell = (CalendarCell)colCalendarCells.get(intColumn);
	                getZx().getQuickContext().setEntry("-zXCalendarCellStartDate", dateFormat.format(objCalendarCell.getDateStart()));
	                getZx().getQuickContext().setEntry("-zXCalendarCellEndDate", dateFormat.format(objCalendarCell.getDateEnd()));

		        	if (!blnCellURL || !getPageflow().isActive(getCellurl().getActive())) {
						getPageflow().getPage().s.append("<td ")
			            						 .appendAttr("width", strColWidth)
			            						 .appendNL('>');
						
					} else {
		        		/**
		        		 * Add start of cell date to the QS
		        		 */
		                strCellURL = getPageflow().wrapRefUrl(getCellurl().getFrameno(),
		                									  getPageflow().constructURL(getCellurl()));
		                
		                strCellURL = "window.document.body.style.cursor='wait';"
		                             + strCellURL + ";" 
		                             + "window.document.body.style.cursor='';";
		                
		                getPageflow().getPage().s.append("<td ")
		                						 .appendAttr("width", strColWidth)
		                						 .appendAttr("onMouseOver", "this.style.cursor='hand';")
		                						 .appendAttr("onMouseOut", "this.style.cursor='default';")
		                						 .appendAttr("onMouseDown", strCellURL).appendNL(">");
		        	} // Has cell URL
		        	
		            if (intZoomFactor == PFCalendar.ZL_MONTH) {
						// Use array, note that is is not internationalised ... yet.
                        getPageflow().getPage().s.appendNL(PFCalendar.arrWeekDays[intColumn]);
                        
					} else {
		                objCalendarCell = (CalendarCell)colCalendarCells.get(intColumn);
		                
		                getPageflow().getPage().s.appendNL(objCalendarCell.getCellLabel());
		                
		                if (objCalendarCell.isMoreRows()) {
							getPageflow().getPage().s.append("<i>... (")
		                    						 .append(objCalendarCell.getRowCount())
		                    						 .append(")</i>").appendNL();
						}
                        
		            }
		            
		            getPageflow().getPage().s.appendNL("</td>");
                    
		        }
                
	            getPageflow().getPage().s.appendNL("</tr>");
	            
		    } // Zoomfactor = 2 and orientation is horizontal
		    
		    // Reset the QS -zXCalendarZoom to the original value
		    getZx().getQuickContext().setEntry("-zXCalendarZoom", String.valueOf(intZoomFactor));
            
            //------------------------------------------------ BUILD CALENDAR CELLS
            
            int intEntity;
            String strUrl;
            boolean blnDidOpen;
            
		    intCell = 0; // Arrays and Arraylists are 0 based.
		    while (intCell < intCalendarCells) {
		        objCalendarCell = (CalendarCell)colCalendarCells.get(intCell);
                
		        getPageflow().getPage().s.append("<tr ")
		        						 .appendAttr("height", StringUtil.len(strMinHeight) > 0?strMinHeight:"")
		        						 .appendNL('>');
		        
		        for (intColumn = 0; intColumn < intColumnCount; intColumn++) {
		            
			        getPageflow().getPage().s.append("<td valign='top' class='zXCalendarCell' ")
			        						 .appendAttr("width", StringUtil.len(strColWidth) > 0?strColWidth:"")
			        						 .appendNL('>');
                    
			        getPageflow().getPage().s.append("<table height='100%' ")
			        						 .appendAttr("cellspacing", "0")
			        						 .appendAttr("cellpadding", "0")
			        						 .appendAttr("width", "100%")
			        						 .appendNL('>');
                    
			        int intDyofWk = DateUtil.getRealDayofWeek(Calendar.MONDAY, objCalendarCell.getDateStart());
			        
                    /**
                     * NOTE : intColumn starts from 0 now instead of 1 like in the VB version.
                     */
			        if (intZoomFactor == PFCalendar.ZL_MONTH && intColumn + 1 != intDyofWk) {
			        	/**
			        	 * Print empty cells for the days of this week no in the same month as the current
			        	 * month.
			        	 */
						getPageflow().getPage().s.appendNL("<tr class='zXCalendarEmpty'><td>&nbsp</td>");
						
			        } else {
			            getPageflow().getPage().s.appendNL("<tr>");
			            
			            if (intZoomFactor == PFCalendar.ZL_MONTH || getOrienTation().equals(zXType.displayOrientation.doVertical)) {
			                /**
			                 * Determine whether we want to add a special class
			                 */
			                if (objCalendarCell.isCurrent()) {
								strClass = constructClass(getCurrentclass(), isAddparitytoclass(), blnOdd, "zXCalendarCurrent");
			                } else {
								strClass = constructClass(getNormalclass(), isAddparitytoclass(), blnOdd, "zXCalendarNormal");
			                }
			                
				        	/**
				        	 * There may be an action associated with the cell
			        		 * Add start of cell date to the QS
			        		 * 
			        		 * v1.5:95 BD30APR06: Set the next two earlier so that they can be referenced
			        		 */
			                getZx().getQuickContext().setEntry("-zXCalendarCellStartDate", dateFormat.format(objCalendarCell.getDateStart()));
			                getZx().getQuickContext().setEntry("-zXCalendarCellEndDate", dateFormat.format(objCalendarCell.getDateEnd()));
			                
				        	if (!blnCellURL || !getPageflow().isActive(getCellurl().getActive())) {
								getPageflow().getPage().s.append("<td valign='top' ")
														 .appendAttr("class", strClass)
														 .appendAttr("width", strLabelWidth)
														 .appendNL('>');
								
			            	} else {
				                strCellURL = getPageflow().wrapRefUrl(getCellurl().getFrameno(),
				                									  getPageflow().constructURL(getCellurl()));
				                
				                strCellURL = "window.document.body.style.cursor='wait';"
				                             + strCellURL + ";" 
				                             + "window.document.body.style.cursor='';";
				                
				                getPageflow().getPage().s.append("<td valign='top' ")
				                						 .appendAttr("class", strClass)
				                						 .appendAttr("width", strLabelWidth)
				                						 .appendAttr("onMouseOver", "this.style.cursor='hand';")
				                						 .appendAttr("onMouseOut", "this.style.cursor='default';")
				                						 .appendAttr("onMouseDown", strCellURL).appendNL(">");
				        	} // Has cell URL
				        	
			                getPageflow().getPage().s.appendNL(objCalendarCell.getCellLabel());
			                
			                if (objCalendarCell.isMoreRows()) {
								getPageflow().getPage().s.append("<i>... (")
			                    						 .append(objCalendarCell.getRowCount())
			                    						 .append(")</i>").appendNL();
							}
                            
			                getPageflow().getPage().s.appendNL("</td>");
                            
			            }
			            
			            getPageflow().getPage().s.append("<td ")
			                    				 .appendAttr("class", "zXCalendarCell")
			                    				 .appendAttr("width", "*")
			                    				 .appendNL('>');
			            
                        /**
                         * Handle list/category cell layouts.
                         */
			            if (getCellMode().equals(zXType.pageflowCellMode.pcmList)) {
							/**
                             * List Cell Layout : Display the entries for this day in a normal list :
			                 */
			                if (objCalendarCell.getRows().size() != 0) {
			                    getPageflow().getPage().s.append("  <table ")
			                    						 .appendAttr("width", "100%")
			                    						 .appendNL('>');
			                    
			                    colRowBOs = new ArrayList();
			                    ArrayList arrEntities = new ArrayList(colEntities.getCollection());
                                
			                    int intCalendarCellRows = objCalendarCell.getRows().size();
			                    for (int i = 0; i < intCalendarCellRows; i++) {
			                        colRowBOs = (ArrayList)objCalendarCell.getRows().get(i);
			                        
			                        blnDidOpen = false;
	                                intEntity = 0;
                                    
			                        int intRowBOs = colRowBOs.size();
			                        for (int j = 0; j < intRowBOs; j++) {
			                            objIBO = (ZXBO)colRowBOs.get(j);
			                            objEntity = (PFEntity)arrEntities.get(intEntity);
                                        intEntity++;
                                        
			                            /**
			                             * BD24OCT04 - Store in BO context
			                             */
			                            getZx().getBOContext().setEntry(objEntity.getName(), objIBO);
			                            
			                            if (StringUtil.len(objEntity.getListgroup()) > 0) {
			                                
			                                if (!blnDidOpen) {
			                                    /**
			                                     * Open the list row
			                                     */
			                                    blnDidOpen = true;
			                                    
			                                    strUrl = constructRowURL(objUrl, intCell + "e" + intEntity);
			                                    getPageflow().getPage().listRowOpen(blnOdd, strUrl, "");  //, pstrClass:=strTmp
                                                
			                                }
                                            
			                                getPageflow().getPage().listRow(objIBO, objEntity.getListgroup(),0);
			                            }
			                            
                                    } // Loop over BOs for this row
	                                
	                                getPageflow().getPage().listRowClose(); // /tr
			                        
			                        /**
			                         * Switch row color
			                         */
	                                blnOdd = !blnOdd;
			                        
                                } // Loop over rows for this cell
			                    
			                    getPageflow().getPage().s.appendNL("</table>");
                                
			                } else {
								// Dump something in the cell so that is displays correctly.
			                    getPageflow().getPage().s.appendNL("&nbsp;");
			                    
							}
			                
						} else if (getCellMode().equals(zXType.pageflowCellMode.pcmCategory)) {
                            /**
                             * Handles the Category cell layout
                             */
			                blnDidOpen = false;
	                        intEntity = 0;
			                
	                        for (int i = 0; i < intCategories; i++) {
	                            objCategory = (PFCalendarCategory)getCategories().get(i);
                                
                                if (!blnDidOpen) {
                                    blnDidOpen = true;
                                    getPageflow().getPage().s.append("<table ")
                                                                .appendAttr("width", "100%")
                                                                .appendNL('>');
                                }
                                
                                /**
                                 * Determine whether we want to add a special class
                                 */
                                strClass = constructClass(objCategory.getClazz(), objCategory.isAddParityToClass(), blnOdd);
                                
                                intEntity++;
                                strUrl = constructRowURL(objCategory.getUrl(), intCell + "e" + intEntity);
                                getPageflow().getPage().listRowOpen(blnOdd, strUrl, strClass);
                                        
                                getPageflow().getPage().s.append("<td>")
                                                         .append(getPageflow().resolveLabel(objCategory.getLabel()))
                                                         .append("</td>").appendNL();
                                        
                                getPageflow().getPage().listRowClose(); // /tr
                                
                                /**
                                 * Switch row color
                                 */
                                blnOdd = !blnOdd;
                                
                            } // Loop over rows for this cell
	                        
	                        if (blnDidOpen) {
								getPageflow().getPage().s.appendNL("</table>");
							} else {
								getPageflow().getPage().s.appendNL("&nbsp");
							}
                            
			            } else {
			                /**
                             * Handles the popup cell layout
			                 */
			                blnDidOpen = false;
			                
	                        for (int i = 0; i < intCategories; i++) {
	                            objCategory = (PFCalendarCategory)getCategories().get(i);
                                
	                            if (objCategory.getRowCount()[intCell] > 0 || objCategory.isShowWhenZero()) {
                                    getZx().getQuickContext().setEntry("-zXCategoryCount", String.valueOf(objCategory.getRowCount()[intCell]));
                                    
                                    
                                    if (!blnDidOpen) {
                                        blnDidOpen = true;
                                        getPageflow().getPage().s.append("<table ")
                                                                 .appendAttr("width", "100%")
                                                                 .appendNL('>');
                                    }
                                    
                                    /**
                                     * Determine whether we want to add a special class
                                     */
                                    strClass = constructClass(objCategory.getClazz(), objCategory.isAddParityToClass(), blnOdd);
                                    
                                    strUrl = constructCategoryUrl(objCategory, intCell); // constructCategoryPopup(pobjAction, objCategory, intCell)
                                    getPageflow().getPage().listRowOpen(blnOdd, strUrl, strClass);
                                            
                                    getPageflow().getPage().s.append("<td>")
                                                            .append(getPageflow().resolveLabel(objCategory.getLabel()))
                                                            .append("</td>").appendNL();
                                            
                                    getPageflow().getPage().listRowClose(); // /tr
                                    
                                    /**
                                     * Switch row color
                                     */
                                    blnOdd = !blnOdd;
                                }
                            } // Loop over rows for this cell
			                
			                
	                        if (blnDidOpen) {
								getPageflow().getPage().s.appendNL("</table>");
							} else {
								getPageflow().getPage().s.appendNL("&nbsp");
							}
			            }
			            
                        intCell++; // Get the next cell.
			            if (intCell < intCalendarCells) {
							objCalendarCell = (CalendarCell)colCalendarCells.get(intCell);
						}
                        
			            getPageflow().getPage().s.appendNL("</td>");
			        }
			        
		            getPageflow().getPage().s.appendNL("</tr>\n</table>\n</td>");
                }
                
		        getPageflow().getPage().s.appendNL("</tr>");
		        
		        intRow++;
		    }
            
            //------------------------------------------------ BUILD CALENDAR CELLS

		    getPageflow().getPage().s.appendNL("</table>");
            
            //------------------------------------------------ END OF MAIN CALENDAR
		    
		    if (intRowCount >= intMaxResultRows) {
				getPageflow().getPage().softMsg("Resultset truncated at " + (intRowCount - 1) + " rows");
			}
		    
            /**
             * Handle buttons; make sure that the buttons are aligned with the
             * right column of the list.
             */
            getPageflow().getPage().buttonAreaOpen(zXType.webFormType.wftNull);
            
            if (getPageflow().processFormButtons(this).pos != zXType.rc.rcOK.pos) {
                getZx().trace.addError("Unable to generate form buttons");
                
                // GoTo errExit
                go = zXType.rc.rcError;
                return go;
            }
            
            getPageflow().getPage().buttonAreaClose(zXType.webFormType.wftNull);
            
            /**
             * Handle window footer title
             */
            getPageflow().handleFooterAndTitle(this, "Select date");
            
            /**
             * Resolve the link action.
             */
            getPageflow().setAction(getPageflow().resolveLink(getLink()));
            
            return go;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Process action.", e);
            
            if (getZx().throwException) {
				throw new ZXException(e);
			}
            go = zXType.rc.rcError;
            return go;
            
        } finally {
            /**
             * Close resultset.
             */
            if (objRs != null) {
				objRs.RSClose();
			}
            
            if (getZx().trace.isApplicationTraceEnabled()) {
                getZx().trace.returnValue(go);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get the date that is the start of the week for the given date but never before the cut-off date.
     * 
     * <pre>
     * 
     * Assumes   :
     *  cutOffDate is in the same week or less than start of week
     * </pre>
     * 
     * @param pdatDate The current date
     * @param pdatCutoffDate The cut off date.
     * @return Returns the start of week.
     */
    private Date getStartOfWeek(Date pdatDate, Date pdatCutoffDate) {
    	Date getStartOfWeek;
    	
    	/**
    	 * Get week number of the given date; we are looking for the start of that week
    	 */
    	Calendar cal = Calendar.getInstance();
    	cal.setFirstDayOfWeek(Calendar.MONDAY);
    	cal.setTimeInMillis(pdatDate.getTime());
    	
    	/**
    	 * Start with the date that we are given and keep subtracting days until we are in another week,
    	 * the last date saved was thus the start of the week we are looking for
    	 */
    	Date datReturn = pdatDate;
    	int intCurrentYear = cal.get(Calendar.YEAR);
		while (true) {
			datReturn = cal.getTime();
			
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) {
				break;
			}
			
			cal.add(Calendar.DATE, -1);
			
			/**
			 * Within this year.
			 */
			if (intCurrentYear > cal.get(Calendar.YEAR)) {
				break;
			}
		}
		
    	/**
    	 * If the calculated date is before the cut-off date, return the cut-off date
    	 */
    	if (datReturn.compareTo(pdatCutoffDate) < 0) {
			getStartOfWeek = pdatCutoffDate;
		} else {
			getStartOfWeek = datReturn;
		}
    	
    	return getStartOfWeek;
    }
    
    /**
     * Get the date that is the end of the week for the given date but never before the cut-off date.
     * 
     * @param pdatDate The current date
     * @param pdatCutoffDate The cut off date.
     * @return Returns the end of week. 
     */
    private Date getEndOfWeek(Date pdatDate, Date pdatCutoffDate) {
    	Date getEndOfWeek;
    	
    	/**
    	 * Get week number of the given date; we are looking for the start of that week
    	 */
    	Calendar cal = Calendar.getInstance();
    	cal.setFirstDayOfWeek(Calendar.MONDAY);
    	cal.setTimeInMillis(pdatDate.getTime());
    	
    	/**
    	 * Start with the date that we are given and keep adding days until we are in another week,
    	 * the last date saved was thus the end of the week we are looking for
    	 */
    	Date datReturn = pdatDate;
    	int intCurrentYear = cal.get(Calendar.YEAR);
		while (true) {
			datReturn = cal.getTime();
			
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				break;
			}
			
			cal.add(Calendar.DATE, 1);
			
			/**
			 * Within this year.
			 */
			if (intCurrentYear < cal.get(Calendar.YEAR)) {
				break;
			}
		}
    	
    	/**
    	 * When the calculated date is beyond the cut-off date, use the cut-off date instead
    	 */
    	if (datReturn.compareTo(pdatCutoffDate) > 0) {
			getEndOfWeek = pdatCutoffDate;
		} else {
			getEndOfWeek = datReturn;
		}
    	
    	return getEndOfWeek;
    }
    
    /**
     * Calculate the number of days in week 1 of this year.
     * This helps us calculate the current WEEK_OF_YEAR for us.
     * 
     * @param dat The current date.
     * @return Returns the number of days in the first week of the year.
     */
    private int getMinimalDaysInFirstWeek(Date dat) {
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(dat.getTime());
    	cal.setFirstDayOfWeek(Calendar.MONDAY);
    	
        /**
         * Calculate the number of days in week 1 of this year.
         * This helps us calculate the current WEEK_OF_YEAR for us.
         */
        cal.set(Calendar.DAY_OF_YEAR, 1);
        int intDaysToEndOfWeek = 1;
		while (true) {
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				break;
			}
			cal.add(Calendar.DATE, 1);
			intDaysToEndOfWeek++;
		}
		
		return intDaysToEndOfWeek;
    }
    
    //------------------------ PAction overidden methods
    
    /** 
     * In this case all of the work in done in the super class implementation.
     * 
     * <pre>
     * 
     * Reviewed for V1.5:75
     * </pre>
     * 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();

        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();

        // Search form specific values to be dumped.
        objXMLGen.taggedCData("colwidth", getColwidth(), true);
        objXMLGen.taggedCData("currentclass", getCurrentclass(), true);
        objXMLGen.taggedCData("normalclass", getNormalclass(), true);
        objXMLGen.taggedCData("orientation", zXType.valueOf(getOrienTation()), true);
        objXMLGen.taggedCData("rowspercell", String.valueOf(getRowspercell()));
        objXMLGen.taggedCData("currentdate", getCurrentdate(), true);
        objXMLGen.taggedCData("labelformat", getLabelformat(), true);
        objXMLGen.taggedCData("labelwidth", getLabelwidth(), true);
        objXMLGen.taggedCData("minheight", getMinheight(), true);
        objXMLGen.taggedCData("anchorentity", getAnchorentity(), true);
        objXMLGen.taggedCData("anchorattr", getAnchorattr(), true);
        objXMLGen.taggedCData("active", getActive(), true);
        objXMLGen.taggedCData("currentdate", getCurrentdate(), true);
        objXMLGen.taggedCData("startperiod", getStartperiod(), true);
        objXMLGen.taggedCData("zoomfactor", getZoomfactor(), true);
        objXMLGen.taggedCData("cellmode", zXType.valueOf(getCellMode()), true);

        if (getPrevurl() != null) {
			getDescriptor().xmlUrl("prevurl", getPrevurl());
		}
        if (getNexturl() != null) {
			getDescriptor().xmlUrl("nexturl", getNexturl());
		}
        if (getZoomurl() != null) {
			getDescriptor().xmlUrl("zoomurl", getZoomurl());
		}
        if (getCellurl() != null) {
			getDescriptor().xmlUrl("cellurl", getCellurl());
		}
        
        if (getCalendartitle() != null && !getCalendartitle().isEmpty()) {
			getDescriptor().xmlLabel("calendartitle", getCalendartitle());
		}
        
        if (getCategories() != null && !getCategories().isEmpty()) {
            objXMLGen.openTag("categories");

            PFCalendarCategory objPFCalendarCategory;

            int intCategories = getCategories().size();
            for (int i = 0; i < intCategories; i++) {
                objPFCalendarCategory = (PFCalendarCategory) getCategories().get(i);

                objXMLGen.openTag("category");

                objXMLGen.taggedCData("name", objPFCalendarCategory.getName());
                objXMLGen.taggedCData("expression", objPFCalendarCategory.getExpression());
                objXMLGen.taggedCData("class", objPFCalendarCategory.getClazz());

                // NOTE : please no more zYes/zNo and just true/false... please !!!
                objXMLGen.taggedCData("addparitytoclass", objPFCalendarCategory.isAddParityToClass() ? "zYes" : "zNo");
                objXMLGen.taggedCData("showwhenzero", objPFCalendarCategory.isShowWhenZero() ? "zYes" : "zNo");

                getDescriptor().xmlUrl("url", objPFCalendarCategory.getUrl());
                getDescriptor().xmlLabel("label", objPFCalendarCategory.getLabel());

                objXMLGen.closeTag("category");
            }

            objXMLGen.closeTag("categories");
        }
    }

    /** 
     * @see PFAction#clone(Pageflow)
     **/
    public PFAction clone(Pageflow pobjPageflow) {
        PFCalendar cleanClone = (PFCalendar) super.clone(pobjPageflow);
        
        cleanClone.setActive(getActive());
        cleanClone.setAnchorattr(getAnchorattr());
        cleanClone.setAnchorentity(getAnchorentity());
        cleanClone.setCellMode(getCellMode());
        cleanClone.setColwidth(getColwidth());
        cleanClone.setCurrentclass(getCurrentclass());
        cleanClone.setCurrentdate(getCurrentdate());
        cleanClone.setLabelformat(getLabelformat());
        cleanClone.setLabelwidth(getLabelwidth());
        cleanClone.setMinheight(getMinheight());
        cleanClone.setNormalclass(getNormalclass());
        cleanClone.setOrienTation(getOrienTation());
        cleanClone.setRowspercell(getRowspercell());
        cleanClone.setStartperiod(getStartperiod());
        cleanClone.setZoomfactor(getZoomfactor());
        
        if (getPrevurl() != null) {
			cleanClone.setPrevurl((PFUrl) getPrevurl().clone());
		}
        if (getNexturl() != null) {
			cleanClone.setNexturl((PFUrl) getNexturl().clone());
		}
        if (getZoomurl() != null) {
			cleanClone.setZoomurl((PFUrl) getZoomurl().clone());
		}
        if (getCellurl() != null) {
			cleanClone.setCellurl((PFUrl) getCellurl().clone());
		}
                
        if (getCalendartitle() != null && !getCalendartitle().isEmpty()) {
			cleanClone.setCalendartitle((LabelCollection) getCalendartitle().clone());
		}
        
        if (getCategories() != null && !getCategories().isEmpty()) {
            int intCategories = getCategories().size();
            ArrayList colCategory = new ArrayList(intCategories);
            PFCalendarCategory objPFCalendarCategory;
            
            for (int i = 0; i < intCategories; i++) {
                objPFCalendarCategory = (PFCalendarCategory)getCategories().get(i);
                colCategory.add(objPFCalendarCategory.clone());
            }
            
            cleanClone.setCategories(colCategory);
        }
        
        return cleanClone;
    }
}