/*
 * Created on Apr 12, 2004 by Michael Brewer
 * $Id: PFStdPopup.java,v 1.1.2.24 2006/07/17 13:57:59 mike Exp $ 
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.Iterator;

import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.property.StringProperty;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * The implementation of the pageflow action 'standard popup'.
 * 
 * <pre>
 * 
 * Change    : BD25MAR03
 * Why       : Changed default navigation URL from zXBlank to zXLeft
 * 
 * Change    : BD27MAR03
 * Why       : Lock entity is no longer reference to entity but plain entity name
 * 
 * Change    : BD28MAR03
 * Why       : - Support for window title
 *             - Retrieve entities from both generateFrameset and buttons
 * 
 * Change    : BD31MAR03
 * Why       : Added -vo=1 to querystring in case you requested a lock but were
 *             not able to get it (vo == view only)
 * 
 * Change    : BD1APR03
 * Why       : Re-interpret the base url when we have added -vo so if the
 *             developer refers to -vo in the base url
 * 
 * Change    : BD9MAY03
 * Why       : Added PK querystring override to header as well
 * 
 * Change    : BD13MAY03
 * Why       : Never any debug messages in frameset of button generation
 * 
 * Change    : BD15MAY03
 * Why       : Support footer-less tabulated form so it can be used inside
 *             any frame and not just in a popup window. Note that a footer
 *             (and thus a popup window) IS required to make use of auto-locking
 * 
 * Change    : DGS15MAY03
 * Why       : Uses -zxpk rather than -pk to avoid conflict.
 *             In generateFrameset, if propagating QS, make sure we pass everything on to the
 *             popup.
 * 
 * Change    : BD22MAY03
 * Why       : Fixed problem not properly fixed at 13MAY:
 *             i.pageflow.PFDesc.debugMode = pdmOff  (was false)
 * 
 * Change    : BD3DEC03
 * Why       : Added reset tab (more efficient to refreshTab)
 * 
 * Change    : DGS16DEC2003
 * Why       : Already didn't create a tab when URL was not active, but was still trying to
 *             do other things on all tabs including non-active. Now does not if not active.
 * 
 * Change    : BD6JAN2004
 * Why       : Fixed small bug with inactive tabs
 * 
 * Change    : BD14FEB04
 * Why       : Add -zXInStdPopupTab to quesrystring to tell pageflows that
 *             they are loaded into standard popup
 * 
 * Change    : BD16FEB04
 * Why       : Add support for preceding action before stdPopup action
 * 
 * Change    : BD25FEB04
 * Why       : Added support for frameset-cols override
 * 
 * Change    : BD25FEB04
 * Why       : Added support for frameset-cols override
 * 
 * Change    : DGS11MAY2004
 * Why       : Tabs now handled differently in javascript. Change required because of javascript
 *             security restrictions on using href. Use a boolean variable now.
 * 
 * Change    : BD14MAY04
 * Why       : Resize frames to 0 points so ther really dissappear!
 * 
 * Change    : BD4JUL04
 * Why       : - When generating the frameset, we now set the requested pk
 *               querystring at the earliest possible moment so that even
 *               in the entities of the popup definition we can refer to it
 *             - If selectListGroup is blank, use selectEditGroup instead
 * 
 * Change    : DGS08JUL2004
 * Why       : Show who has lock if entity is locked
 * 
 * Change    : BD26AUG04
 * Why       : Add support for -zXPopupDef querydef entry
 * 
 * Change    : MB01NOV04
 * Why       : Allow for browser independence.
 * 
 * Change    : MB01NOV04
 * Why       : Set the marginwidth to 0 to make the frameset look seemless.
 * 
 * Change    : BD11NOV04 - 1.5.1.1.11
 * Why       : When generating a title we should escape the value
 * 
 * Change    : DGS15DEC2004
 * Why       : In generateButtons, generate any javascript tags.
 * 
 * Change    : BD20JAN05 - V1.4:24
 * Why       : Prompt user for confirmation if any of the tabs has been
 *             marked as dirty
 *             
 * Change    : MB04SEP05  V1.5:44
 * Why       : Added support for access-key (BD15NOV04)
 * 
 * Change    : BD3OCT05  V1.5:61
 * Why       : Added support for confirmation message for stdPopup action
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFStdPopup extends PFAction {
	
	//------------------------ Members
	
	private PFUrl headerurl;
	private PFUrl footerurl;
	private PFUrl navigationurl;
	private String lockentity;
	private String lockid;
	private ArrayList tabs;
	private String qspk;
	private String framesetrows;
	private String framesetcols;
	private boolean navigation;
	private boolean footer;
	
	//------------------------ Constants
	
	private static final char ZERO = '0';
	private static final char START = '*';
	private static final char COMMA = ',';

	//------------------------ Constructors
	
	/**
	 * Default contructor.
	 */
	public PFStdPopup() {
	    super();
	}
    
    //------------------------ Getters/Setters
    
	/** 
	 * Whether to display the footer navigation on the bottom of a standard popup.
	 * 
	 * @return Returns the footer. 
	 */
	public boolean isFooter() {
	    return footer;
	}
	
	/**
	 * @param footer The footer to set.
	 */
	public void setFooter(boolean footer) {
	    this.footer = footer;
	}

	/** 
	 * The URL of the footer page of the Standard popup.
	 * 
	 * @return Returns the footerurl. 
	 */
	public PFUrl getFooterurl() {
	    return footerurl;
	}
	
	/**
	 * @param footerurl The footerurl to set.
	 */
	public void setFooterurl(PFUrl footerurl) {
	    this.footerurl = footerurl;
	}
	
	/** 
	 * The sizes of the frameset columns.
	 * 
	 * @return Returns the framesetcols. 
	 */
	public String getFramesetcols() {
	    return framesetcols;
	}
	
	/**
	 * @param framesetcols The framesetcols to set.
	 */
	public void setFramesetcols(String framesetcols) {
	    this.framesetcols = framesetcols;
	}
	
	/** 
	 * The sizes of the frameset rows.
	 * 
	 * @return Returns the framesetrows. 
	 */
	public String getFramesetrows() {
	    return framesetrows;
	}
	
	/**
	 * @param framesetrows The framesetrows to set.
	 */
	public void setFramesetrows(String framesetrows) {
	    this.framesetrows = framesetrows;
	}
	
	/** 
	 * The URL of the header page of the Standard popup. 
	 * This can be a jsp or another pageflow action
	 * 
	 * @return Returns the headerurl. 
	 */
	public PFUrl getHeaderurl() {
	    return headerurl;
	}
	
	/**
	 * @param headerurl The headerurl to set.
	 */
	public void setHeaderurl(PFUrl headerurl) {
	    this.headerurl = headerurl;
	}
	
	/** 
	 * The business object to lock.
	 * 
	 * @return Returns the lockentity. 
	 */
	public String getLockentity() {
	    return lockentity;
	}
	
	/**
	 * @param lockentity The lockentity to set.
	 */
	public void setLockentity(String lockentity) {
	    this.lockentity = lockentity;
	}
	
	/** 
	 * The Primary key of the entity to lock while this window on.
	 * 
	 * @return Returns the lockid. 
	 */
	public String getLockid() {
	    return lockid;
	}
	
	/**
	 * @param lockid The lockid to set.
	 */
	public void setLockid(String lockid) {
	    this.lockid = lockid;
	}
	
	/** @return Returns the navigation. */
	public boolean isNavigation() {
	    return navigation;
	}
	
	/**
	 * Whether to display the navigation bar on the side. 
	 * This normally displays the title of the standard popup.
	 * 
	 * @param navigation The navigation to set.
	 */
	public void setNavigation(boolean navigation) {
	    this.navigation = navigation;
	}
	
	/** 
	 * The URL of the navigation page of the Standard popup. 
	 * The standard navigation page is zxstdpopupnav.jsp 
	 * 
	 * @return Returns the navigationurl. 
	 */
	public PFUrl getNavigationurl() {
	    return navigationurl;
	}
	
	/**
	 * @param navigationurl The navigationurl to set.
	 */
	public void setNavigationurl(PFUrl navigationurl) {
	    this.navigationurl = navigationurl;
	}
	
	/**
	 * The name of the query string/request parameter that 
	 * contains the primary key of the standard popup's entity.
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
	 * A Collection (ArrayList)(PFRef) of Pageflows actions for the tabs of the Standard popup. 
	 * 
	 * @return Returns the tabs. 
	 * */
	public ArrayList getTabs() {
	    return tabs;
	}
	
	/** 
	 * @param tabs The tabs to set. 
	 */
	public void setTabs(ArrayList tabs) {
	    this.tabs = tabs;
	}

	//------------------------ Digester helper methods.
	
	/**
	 * @deprecated Using BooleanConverter
	 * @param navigation The navigation to set.
	 */
	public void setNavigation(String navigation) {
	    this.navigation = StringUtil.booleanValue(navigation);
	}
	
	/**
	 * @deprecated Using BooleanConverter
	 * @param footer The footer to set.
	 */
	public void setFooter(String footer) {
	    this.footer = StringUtil.booleanValue(footer);
	}
    
    //------------------------ Implemented Methods from PFAction
    
	/**
	 * @see PFAction#go()
	 **/
	public zXType.rc go() throws ZXException {
		if(getZx().trace.isApplicationTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
	    zXType.rc go = zXType.rc.rcOK;
	
		try {
		    /**
		     * BD16FEB04 This may look a bit weird; the popup definition action can be
		     * preceded by another action (this must be a null action). This can be used
		     * to add some stuff to the querystring that may cmoe in handy.
		     * Now the frameset definition HAS to be the only thing in the HTML otherwise
		     * it will simply not work so we force the html so far to be empty by calling
		     * html.html and assigning it to a rubbish string.
		     * 
		     * NOTE : Some html is generated over this pageflow action is completed.
		     */
		    getPageflow().getPage().flush();
		    
		    generateFrameset();
		    
		    getPageflow().setAction(getPageflow().resolveLink(getLink()));
		    
		    return go;
		} catch (Exception e) {
	    	getZx().trace.addError("Failed to : Execute the Standard popup pageflow action", e);
	    	
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
    
    //------------------------ Public methods (Used by JSPs)
    
    /**
     * Generate the button section of the standard popup.
     * 
     * <pre>
     *  
     *  Assumes   :
     *  	This action is the frameset definition
     *  
     * Reviewed for V1.5:62 - Added support for confirmation message on
     * 						  stdPopupAction
     * </pre>
     * 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if generateButtons fails.
     */
    public zXType.rc generateButtons() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        zXType.rc generateButtons = zXType.rc.rcOK;
        
        zXType.pageflowDebugMode enmDebugMode = getPageflow().getPFDesc().getDebugMode();
        
        try {
        	/**
        	 * Short circuit if there is not tabs.
        	 */
        	if (getTabs() == null) return generateButtons;
        	
            String strName;
            
            /**
             * Switch off debug-ing as we cannot afford to have any debug messages
             * in the Javascript stuff
             */
            getPageflow().getPFDesc().setDebugMode(zXType.pageflowDebugMode.pdmOff);
            
            /**
             * Resolve entities as we may want to refer to them in the URLs
             */
            resolveEntities();
            
            //--------------------------------------------------------------------
            //---------------------------------------- Print the html for the tabs
            /**
             * DGS27JUN2003: For some reason this was 90% wide - looks better 100%
             */
            getPageflow().getPage().s.appendNL("<table width='100%' cellpadding='0' cellspacing='0'>");
            getPageflow().getPage().s.appendNL("<tr>");
            
            PFRef objRef;
            PFRef objFirstActiveRef = null;
            String strJS;
            
            int intTabs = getTabs().size();
            for (int i = 0; i < intTabs; i++) {
                objRef = (PFRef)getTabs().get(i);
                
                strName = objRef.getName().toLowerCase();
                
                /**
                 * Only show tab when active
                 */
                if (getPageflow().isActive(objRef.getUrl().getActive())) {
                    /**
                     * Save the first active tabs ref
                     */
                    if (objFirstActiveRef == null) {
                        objFirstActiveRef = objRef;
                    }
                    
                    strJS = "javascript:zXStdPopupHandleTabClick(findObj('tab" + strName;
                    strJS = strJS + "', window), '" + getPageflow().getQs().getEntryAsString("-s") + "'";
                    strJS = strJS + ", '" + getPageflow().getQs().getEntryAsString("-ss") + "'";
                    strJS = strJS + ");";
                    
                    /**
                     * MB04SEP05  V1.5:44 : Added support for access-key (BD15NOV04)
                     **/
                    String strAccessKey = getPageflow().getLabel(objRef.getAccesskey());
                    
                    /**
                     * Set the default accessKey to t.
                     */
                    if (StringUtil.len(strAccessKey) == 0) {
                    	strAccessKey = "t";
                    }
                    
                    /**
                     * Optionally add a description.
                     */
                    String strDescription = getPageflow().getLabel(objRef.getDescription());
                    if (StringUtil.len(strDescription) == 0) {
                    	strDescription = getPageflow().resolveLabel(objRef.getLabel());
                    	
                    	if (strDescription == null) {
                    		strDescription = "";
                    	}
                    }
                    
                  	/**
                   	 * Add the access key to the description.
                   	 */
                   	strDescription = strDescription + " (Alt-" + strAccessKey + ")";
                   	
                    getPageflow().getPage().s.append("<td ")
                    						 .appendAttr("id","tab" + strName)
                    						 .appendAttr("onMouseDown", strJS)
                    						 .appendAttr("accesskey", strAccessKey)
                    						 .appendAttr("onFocus", strJS)
                    						 .appendAttr("tabindex", StringUtil.len(objRef.getTabindex())>0?objRef.getTabindex():"-1")
                    						 .appendAttr("title", strDescription)
                    						 .appendNL('>');
                    
                    /**
                     * Could be label and / or image
                     */
                    getPageflow().getPage().s.appendNL(getPageflow().resolveLabel(objRef.getLabel()));
                    
                    if (StringUtil.len(objRef.getImg()) > 0) {
                    	String strImgOver = StringUtil.len(objRef.getImgover()) == 0 ? objRef.getImg() : objRef.getImgover();
                    	
                        getPageflow().getPage().s.append("<img ")
                        						 .appendAttr("src", objRef.getImg())
                        						 .appendAttr("onMouseOver", "javascript:this.src='" + strImgOver + "'")
                        						 .appendAttr("onMouseOut", "javascript:this.src='" + objRef.getImg() + "'")
                        						 .appendNL('>');
                    }
                    
                    getPageflow().getPage().s.appendNL("</td>");
                }
                
            }
            
            getPageflow().getPage().s.appendNL("<td width='*'></td>");
            getPageflow().getPage().s.appendNL("</tr>");
            getPageflow().getPage().s.append("</table>").appendNL().appendNL();
            
            //---------------------------------------- Print the html for the tabs
            //--------------------------------------------------------------------
            
            /**
             * Generate the necessary Javascript
             */
            getPageflow().getPage().s.appendNL("<script type=\"text/javascript\" language=\"JavaScript\">");
            
            /**
             * Variables to keep track of active frame and tab
             */
            getPageflow().getPage().s.appendNL("var frazXActiveFrame;");
            getPageflow().getPage().s.appendNL("var tabzXActiveTab;");
            
            /**
             * DGS11MAY2004: Create a boolean for each tab. This indicates if the tab is loaded
             * or not. Previously we compared the URL, but the problem is that javascript does
             * not allow us to look at any URL from a different server. This works fine anyway.
             */
            for (int i = 0; i < intTabs; i++) {
                objRef = (PFRef)getTabs().get(i);
                strName = objRef.getName().toLowerCase();
                getPageflow().getPage().s.append("var bln").append(strName).append("Loaded = false;").appendNL();
            }
            getPageflow().getPage().s.appendNL();
            
            /**
             * Activate the very first tab
             */
            if (objFirstActiveRef != null) {
	            getPageflow().getPage().s.append("zXStdPopupHandleTabClick(findObj('tab")
	            						 .append(objFirstActiveRef.getName().toLowerCase())
	            						 .append("', window), ");
	            getPageflow().getPage().s.append('\'')
	            						 .append(getPageflow().getQs().getEntryAsString("-s"))
	            						 .append('\'');
	            getPageflow().getPage().s.append(",'")
	            						 .append(getPageflow().getQs().getEntryAsString("-ss"))
	            						 .append("');")
	            						 .appendNL().appendNL();
            }
            
            /**
             * The zXStdPopupSelectTab function
             */
            getPageflow().getPage().s.appendNL("function zXStdPopupHandleTabClick(_ctrTab, _session, _ss) {");
            
            /**
             * Ignore click if tab is deactivated (as during a Do Action)
             */
            getPageflow().getPage().s.appendNL("\t if (_ctrTab.className.toLowerCase() == \"zxtabdeactive\") return true;");
            
            /**
             * Make all buttons look inactive
             */
            for (int i = 0; i < intTabs; i++) {
                objRef = (PFRef)getTabs().get(i);
                
                if (getPageflow().isActive(objRef.getUrl().getActive())) {
                    strName = objRef.getName().toLowerCase();
                    getPageflow().getPage().s.appendNL("\t findObj('tab" + strName + "', window).className = 'zxTabInactive';");
                }
                
            }
            
            /**
             * Make the right one look active
             */
            getPageflow().getPage().s.appendNL("\t _ctrTab.className = 'zxTabActive';");
            getPageflow().getPage().s.appendNL("\t tabzXActiveTab = _ctrTab;");
            
            /**
             * And load the appropriate URL
             */
            getPageflow().getPage().s.appendNL("\t switch(_ctrTab.id)");
            getPageflow().getPage().s.appendNL("\t {");
            
            int j = 0;
            
            for (int i = 0; i < intTabs; i++) {
                objRef = (PFRef)getTabs().get(i);
                
                /**
                 * DGS16DEC2003: Only when tab is active; make sure that we always
                 * update j as this is the pointer to the actual frame and we ALWAYS
                 * generate the frame even though the tab is inactive
                 */
                j++;
                
                if (getPageflow().isActive(objRef.getUrl().getActive())) {
                    strName = objRef.getName().toLowerCase();
                    
                    getPageflow().getPage().s.append("\t\t case 'tab").append(strName).append("':").appendNL();
                    
                    /**
                     * DGS11MAY2004: Boolean (one for each tab) indicates if the tab is loaded.
                     */
                    getPageflow().getPage().s.append("\t\t\t if (!bln").append(strName).append("Loaded) {").appendNL();
                    getPageflow().getPage().s.append("\t\t\t\t findObj('tab").append(strName).append("', window).style.cursor = 'wait';").appendNL();
                    
                    /**
                     * Causes timing problems with document not there yet
                     * .add "  parent.fra" & LCase$(objRef.name) & ".document.body.style.cursor = 'wait';"
                     */
                    getPageflow().getPage().s.append("\t\t\t\t zXStdPopupRefreshTab(parent.fra").append(strName).append(");").appendNL();
                    getPageflow().getPage().s.append("\t\t\t }").appendNL();
                    
                    /**
                     * Save active frame
                     */
                    getPageflow().getPage().s.append("\t\t\t frazXActiveFrame = parent.fra").append(strName).append(";").appendNL();
                    
                    getPageflow().getPage().s.append("\t\t\t parent.document.getElementsByTagName('frameset')[3].setAttribute('rows', '")
                    						 .append(frameSizeString(intTabs, j))
                    						 .append("');")
                    						 .appendNL();
                    
                    getPageflow().getPage().s.append("\t\t\t findObj('tab").append(strName).append("', window).style.cursor = '';").appendNL();
                    getPageflow().getPage().s.append("\t\t\t break;").appendNL().appendNL();
                    
               }
            }
            
            getPageflow().getPage().s.append("\t }" ).appendNL(); // ' end switch
            getPageflow().getPage().s.append("}" ).appendNL().appendNL(); // ' end function
            
            /**
             * Script to (re-) load actual URL for all the tabs
             */
            getPageflow().getPage().s.append("function zXStdPopupRefreshTab(_fraTab) {").appendNL();
            
            /**
             * Switch statement that takes care of all the standard tabs
             */
            getPageflow().getPage().s.appendNL("\t switch(_fraTab.name)");
            getPageflow().getPage().s.appendNL("\t {");
            
            for (int i = 0; i < intTabs; i++) {
            	objRef = (PFRef)getTabs().get(i);
                
            	strName = objRef.getName().toLowerCase();
            	j++;
                
            	String strUrl = getPageflow().constructURL(objRef.getUrl());
                
                getPageflow().getPage().s.append("\t\t case 'fra").append(strName).append("': ").appendNL();
                getPageflow().getPage().s.append("\t\t\t parent.fra").append(strName).append(".document.location = '").append(strUrl).append("';").appendNL();
                /**
                 * DGS11MAY2004: Boolean (one for each tab) indicates if the tab is loaded.
                 */
                getPageflow().getPage().s.append("\t\t\t bln").append(strName).append("Loaded = true;").appendNL();
                getPageflow().getPage().s.append("\t\t\t break;").appendNL();
            }
            
            getPageflow().getPage().s.append("\t }" ).appendNL(); // ' end switch
            getPageflow().getPage().s.append("}" ).appendNL().appendNL(); // ' end function            
            
            /**
             * Script to reset tab contents to initial value (ie zXBlank)
             */
            getPageflow().getPage().s.append("function zXStdPopupResetTab(_fraTab) {").appendNL();
            
            /**
             * Switch statement that takes care of all the standard tabs
             */
            getPageflow().getPage().s.append("\t switch(_fraTab.name)").appendNL();
            getPageflow().getPage().s.append("\t {").appendNL();
            
            for (int i = 0; i < intTabs; i++) {
                objRef = (PFRef)getTabs().get(i);
                
                strName = objRef.getName().toLowerCase();
                j++;
                
                getPageflow().getPage().s.append("\t\t case 'fra").append(strName).append("':").appendNL();
                getPageflow().getPage().s.append("\t\t\t parent.fra").append(strName).append(".document.location = '../html/zXBlank.html';").appendNL();
                
                /**
                 * DGS11MAY2004: Boolean (one for each tab) indicates if the tab is loaded.
                 */
                getPageflow().getPage().s.append("\t\t\t bln").append(strName).append("Loaded = false;").appendNL();
                getPageflow().getPage().s.append("\t\t break;").appendNL();
            }
            
            getPageflow().getPage().s.append("\t }" ).appendNL(); // ' end switch
            getPageflow().getPage().s.append("}" ).appendNL().appendNL(); // ' end function
            
            /**
             * Script to click on a certain tab
             */
            getPageflow().getPage().s.append("function zXStdPopupClickTab(_name) {").appendNL();
            
            /**
             * Switch statement that takes care of all the standard tabs
             */
            getPageflow().getPage().s.append("\t switch(_name.toLowerCase())").appendNL();
            getPageflow().getPage().s.append("\t {").appendNL();
            
            for (int i = 0; i < intTabs; i++) {
                objRef = (PFRef)getTabs().get(i);
                
                strName = objRef.getName().toLowerCase();
                j++;
                
                getPageflow().getPage().s.append("\t\t case '").append(strName).append("':").appendNL();
                getPageflow().getPage().s.append("\t\t\t zXStdPopupHandleTabClick(findObj('tab").append(strName).append("', window), '")
                												.append(getPageflow().getQs().getEntryAsString("-s")).append("'")
                												.append(" ,'").append(getPageflow().getQs().getEntryAsString("-ss")).append("');")
                												.appendNL();
                getPageflow().getPage().s.append("\t\t break;").appendNL();
                
            }

            getPageflow().getPage().s.append("\t }" ).appendNL(); // ' end switch
            getPageflow().getPage().s.append("}" ).appendNL().appendNL(); // ' end function
            
            /**
             * Script to make the 'Do Action' frame visible and all tab buttons deactivated
             */
            getPageflow().getPage().s.append("function zXStdPopupStartAction(_strUrl) {").appendNL();
            
            for (int i = 0; i < intTabs; i++) {
                objRef = (PFRef)getTabs().get(i);
                
                /**
                 * DGS16DEC2003: Only when tab is active
                 */
                if (getPageflow().isActive(objRef.getUrl().getActive())){
                    strName = objRef.getName().toLowerCase();
                    
                    getPageflow().getPage().s.append("\t findObj('tab").append(strName)
                    						 .append("', window).className = 'zxTabDeactive';")
                    						 .appendNL();
                }
                
            }
            
            getPageflow().getPage().s.append("\t parent.frazxdoaction.document.location = _strUrl;").appendNL();
            
//            getPageflow().getPage().s.append("\t parent.document.all.fraDetailsInnerSet.rows = '")
//            									 .append(frameSizeString(getTabs().size(), getTabs().size() + 1)).append("';")
//            									 .appendNL();
            
            getPageflow().getPage().s.append("\t parent.document.getElementsByTagName('frameset')[3].setAttribute('rows', '")
			 										.append(frameSizeString(intTabs, intTabs + 1))
			 										.append("');")
			 										.appendNL();
            
            getPageflow().getPage().s.append("}").appendNL().appendNL(); // End function
            
            /**
             * BD3OCT05  V1.5:61 : Same but with confirm message
             **/
            getPageflow().getPage().s.appendNL("function zXStdPopupStartActionConfirm(_strConfirm, _strUrl)");
            getPageflow().getPage().s.appendNL("{");
            
            getPageflow().getPage().s.appendNL("  if ( confirm(_strConfirm) )");
            getPageflow().getPage().s.appendNL("  {");
            
            for (int i = 0; i < intTabs; i++) {
                objRef = (PFRef)getTabs().get(i);
                
                /**
                 * DGS16DEC2003: Only when tab is active
                 */
                if (getPageflow().isActive(objRef.getUrl().getActive())){
                    strName = objRef.getName().toLowerCase();
                    
                    getPageflow().getPage().s.append("      findObj('tab").append(strName)
                    						 .append("', window).className = 'zxTabDeactive';")
                    						 .appendNL();
                
                }
            }
                
            getPageflow().getPage().s.appendNL("      parent.frazxdoaction.document.location = _strUrl;");
            getPageflow().getPage().s.append("      parent.document.getElementsByTagName('frameset')[3].setAttribute('rows', '")
            						 .append(frameSizeString(intTabs, intTabs + 1)).append("');").appendNL();
            
            getPageflow().getPage().s.appendNL("  }"); // end if confirm
                            
            getPageflow().getPage().s.appendNL("}").appendNL(); // end function
            
            /**
             * Script to (re-) load actual URL for the header
             */
            getPageflow().getPage().s.append("function zXStdPopupRefreshHeader() {").appendNL();
            getPageflow().getPage().s.append("\t parent.fraHeader.document.location = '").append(getPageflow().constructURL(getHeaderurl())).append("';").appendNL();
            getPageflow().getPage().s.append("}").appendNL().appendNL();
            
            /**
             * Script to (re-) load actual URL for the navigation bar
             */
            getPageflow().getPage().s.append("function zXStdPopupRefreshNavigation() {").appendNL();
            getPageflow().getPage().s.append("\t parent.fraLeft.document.location = '").append(getPageflow().constructURL(getNavigationurl())).append("';").appendNL();
            getPageflow().getPage().s.append("}").appendNL().appendNL();
            
            String strLabel = getPageflow().resolveLabel(getTitle());
            if (StringUtil.len(strLabel) > 0) {
                /**
                 * BD11NOV04 - Escape the title, otherwise a quote in the
                 * title would cause major problems with Javascript errors
                 */
                strLabel = StringEscapeUtils.escapeJavaScript(strLabel);
                
                /**
                 * Add (view only) to title
                 * DGS08JUL2004: Also show who has the lock, if we know
                 */
                if(StringUtil.len(getPageflow().getQs().getEntryAsString("-vo")) > 0) {
                    getPageflow().getPage().s.append("zXTitle('").append(strLabel).append(" (view only")
                    							.append( (StringUtil.len(getPageflow().getQs().getEntryAsString("-vo.lckdBy")) > 0 ? " - locked by " + getPageflow().getQs().getEntryAsString("-vo.lckdBy") : "") )
                    							.append("');").appendNL();
                } else {
                    getPageflow().getPage().s.append("zXTitle('").append(strLabel).append("');").appendNL();
                }
            }
            
            /**
             * MB14MAR2005 : Method to check whether any of the tab-window is marked dirty.
             */
            getPageflow().getPage().s.append("var blnCheck = true;").appendNL().appendNL();
            getPageflow().getPage().s.append("function zXStdPopupCheck() { ").appendNL();
            
            getPageflow().getPage().s.append("if (blnCheck) { ").appendNL().appendNL();
            getPageflow().getPage().s.append("  var dirty = 0;").appendNL();
            getPageflow().getPage().s.append("  var label = '';").appendNL().appendNL();
            
            for (int i = 0; i < intTabs; i++) {
                objRef = (PFRef)getTabs().get(i);
                
                if (getPageflow().isActive(objRef.getUrl().getActive())) {
                    strName = objRef.getName().toLowerCase();
                    strLabel = getPageflow().resolveLabel(objRef.getLabel()).toLowerCase();
                    
                    getPageflow().getPage().s.append("  if (parent.fra").append(strName).append(".zXDirty != undefined) {").appendNL();
                    getPageflow().getPage().s.append("      if (parent.fra").append(strName).append(".zXDirty == 1) {").appendNL();
                    getPageflow().getPage().s.append("          dirty += parent.fra").append(strName).append(".zXDirty;").appendNL();
                    getPageflow().getPage().s.append("          if (label != '') {").appendNL();
                    getPageflow().getPage().s.append("              label += ', ';").appendNL();
                    getPageflow().getPage().s.append("          }").appendNL();
                    getPageflow().getPage().s.append("          label += '")
                                                .append(StringEscapeUtils.escapeJavaScript(strLabel)).append("';").appendNL();
                    getPageflow().getPage().s.append("      }").appendNL();
                    getPageflow().getPage().s.append("  };").appendNL();
                }
            }
            getPageflow().getPage().s.appendNL();
            
            getPageflow().getPage().s.append("  if (dirty == 1) {").appendNL();
            getPageflow().getPage().s.append("      return 'Unsaved changes on tab ' + label + '; close anyway?';").appendNL();
            getPageflow().getPage().s.append("  }").appendNL();
            getPageflow().getPage().s.append("  if (dirty > 1) {").appendNL();
            getPageflow().getPage().s.append("      return 'Unsaved changes on tabs ' + label + '; close anyway?';").appendNL();
            getPageflow().getPage().s.append("  }").appendNL();
            getPageflow().getPage().s.append("}").appendNL().appendNL();
            
            getPageflow().getPage().s.append("return '';").appendNL().appendNL();
            getPageflow().getPage().s.append("}").appendNL().appendNL();    // end function
            
            /**
             * BD20JAN05 - V1.4:24
             * Script to close window but prompt for confirmation if any
             * tab-window is marked as 'dirty'
             */
            getPageflow().getPage().s.append("function zXStdPopupClose(){ ").appendNL();
            getPageflow().getPage().s.append("  var label;").appendNL();
            getPageflow().getPage().s.append("  label= zXStdPopupCheck();").appendNL().appendNL();
            getPageflow().getPage().s.append("  blnCheck = false;").appendNL().appendNL();
            
            getPageflow().getPage().s.append("  if (label != undefined && label != '') {").appendNL();
            getPageflow().getPage().s.append("      if(confirm(label)) {").appendNL();
            getPageflow().getPage().s.append("          top.window.close();").appendNL();
            getPageflow().getPage().s.append("      } else {").appendNL();
            getPageflow().getPage().s.append("          blnCheck = true;").appendNL();
            getPageflow().getPage().s.append("      }").appendNL();
            getPageflow().getPage().s.append("  } else {").appendNL();
            getPageflow().getPage().s.append("      top.window.close();").appendNL();
            getPageflow().getPage().s.append("  }").appendNL();
            getPageflow().getPage().s.append("}").appendNL();    // end function
            
            getPageflow().getPage().s.append("</script>").appendNL();
            
            /**
             *  DGS15DEC2004: Generate javascript tags here. The buttons frame is a handy place to
             *  do this because it isn't usually refreshed, so gets executed just the once.
             */
            getPageflow().processJavascriptTags(this);
            
            return generateButtons;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate the button section of the standard popup.", e);
            if (getZx().throwException) throw new ZXException(e);
            
            generateButtons = zXType.rc.rcError;
            return generateButtons;
        } finally {
            // Restore debug mode setting
            getPageflow().getPFDesc().setDebugMode(enmDebugMode);
            
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(generateButtons);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Private methods
    
	/**
	 * Generate frameset HTML.
	 * 
	 * <pre>
	 *  This generates the frameset in when the navigation and the editforms etc.. lie.
	 *  
	 *  Assumes   :
	 *    Me to be the frameset action of choice
	 * </pre>
	 * 
	 * @return Returns the return code of the method.
	 * @throws ZXException Thrown if generateFrameset fails.
	 */
    private zXType.rc generateFrameset() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        zXType.rc generateFrameset = zXType.rc.rcOK; 
        
        zXType.pageflowDebugMode enmDebugMode = getPageflow().getPFDesc().getDebugMode();
        
        try {
            /**
             * Switch off debug-ing as we cannot afford to have any debug messages
             * in the Javascript stuff
             */
            getPageflow().getPFDesc().setDebugMode(zXType.pageflowDebugMode.pdmOff);
            
            /**
             * Set proper qs entry for pk here as we may refer to it in resolveEntities
             */
            getZx().getQuickContext().setEntry(getQspk(), getPageflow().getQs().getEntryAsString("-zxpk"));
            
            /**
             * Resolve entities as we may want to refer to them in the URLs
             */
            resolveEntities();
            
            /**
             * Add -zXInPopupTab to the querystring so we can always tell
             * in the pageflows that we are loaded in a tab of a standard popup
             */
            getZx().getQuickContext().setEntry("-zXInStdPopupTab", "1");
            
            /**
             * Handle the lock (if requested)
             * Note that locking is NOT supported when no footer is available (Not true for new AJAX code)
             */
            String strEntity;
            if (isFooter()) {
            	
                strEntity = getPageflow().resolveDirector(getLockentity());
                
                if (StringUtil.len(strEntity) > 0) {
                    /**
                     * And get lock using the sub-session as lock-id
                     * DGS15MAY2003: Use -zxpk rather than -pk to avoid conflict.
                     */
                    zXType.rc enmRC = getZx().getBOLock().getLock(strEntity,
                            new StringProperty(getPageflow().getQs().getEntryAsString("-zxpk"), false),
                            getPageflow().getQs().getEntryAsString("-ss"));
                    /**
                     * All is fine; you now have a lock
                     */
                    if (enmRC.equals(zXType.rc.rcOK)) {
                        /**
                         * All is fine; you now have a lock
                         */
                    } else if(enmRC.equals(zXType.rc.rcWarning)) {
                        /**
                         * Somebody else has a lock so add -vo (view only) to the querystring
                         * so the details in the frame can do clever things
                         * 
                         * DGS08JUL2004: Also can show who has the lock
                         */
                        getPageflow().getQs().setEntry("-vo", "1");
                        getPageflow().getQs().setEntry("-vo.lckdBy", getZx().getBOLock().getValue("zXCrtdBy").getStringValue());
                        
                        /**
                         * Bit weird; on initialisation of a pageflow the base url is
                         * interpreted and cached for performance. Now we have added -vo
                         * to the querystring, we have to re-interpret the base URL so that
                         * we can be sure that if we use -vo in the base url it is actually
                         * being picked-up
                         */
                        if (!getPageflow().getPFDesc().isPropagaTeqs()) {
                            getPageflow().setBaseURL(getPageflow().constructURL(getPageflow().getPFDesc().getBaseurl()));
                        }
                        
                    } else if (enmRC.equals(zXType.rc.rcError)) {
                        /**
                         * Severe error obtaining a lock - Java exception handling will actually skip this.
                         */
                        throw new Exception("Unable to get lock");
                    }
                } // Locking entity
                
            } // footer requested and locking thus allowed
            
            /**
             * See if we have to leave space for a footer
             */
            if (isFooter()) {
                getPageflow().getPage().s.append("<frameset rows='*,55'name='fraTop' frameborder='no' border='1' framespacing='0'>").appendNL();
            } else {
                getPageflow().getPage().s.append("<frameset rows='*,0'name='fraTop' frameborder='no' border='1' framespacing='0'>").appendNL();
            }
            
            /**
             * See if room is required for proper navigation frame
             */
            if (isNavigation()) {
                /**
                 * We may have a frameset override
                 */
                if (StringUtil.len(getFramesetcols()) > 0) {
                    getPageflow().getPage().s.append("<frameset cols='").append(getFramesetcols()).append("' name='fraDetailsOuterSet'>").appendNL();
                } else {
                    getPageflow().getPage().s.append("<frameset cols='12%,*' name='fraDetailsOuterSet'>").appendNL();
                }
                
            } else {
                getPageflow().getPage().s.append("<frameset cols='0,*' name='fraDetailsOuterSet'>").appendNL();
            }
            
            /**
             * Navigation frame
             */
            StringBuffer strUrl;
            if (isNavigation()) {
                if (StringUtil.len(getNavigationurl().getUrl()) == 0) {
                    strUrl = new StringBuffer("../html/zXLeft.html");
                } else {
                    /**
                     * DGS15MAY2003: Use -zxpk rather than -pk to avoid conflict.
                     */
                    getPageflow().getQs().setEntry(getQspk(), getPageflow().getQs().getEntryAsString("-zxpk"));
                    strUrl = new StringBuffer(getPageflow().constructURL(getNavigationurl()));
                }
            } else {
                strUrl = new StringBuffer("../html/zXLeft.html");
            }
            
            getPageflow().getPage().s.append("<frame name='fraLeft' src='").append(strUrl).append("' scrolling='no' frameborder='0'>").appendNL();
            
            /**
             * Header
             */
            String strFramesetRows;
            if (StringUtil.len(getHeaderurl().getUrl()) == 0) {
                /**
                 * If no header URL, assume that there is hardly any need for an header area
                 */
                strFramesetRows = "0,40,*";
                strUrl = new StringBuffer("../html/zXBlank.html");
                
            } else {
                strFramesetRows = "15%,40,*";
                
                /**
                 * GS15MAY2003: Use -zxpk rather than -pk to avoid conflict.
                 */
                getPageflow().getQs().setEntry(getQspk(), getPageflow().getQs().getEntryAsString("-zxpk"));
                strUrl = new StringBuffer(getPageflow().constructURL(getHeaderurl()));
            }
            
            /**
             * Add -zXInPopupTab to the querystring so we can always tell
             * in the pageflows that we are loaded in a tab of a standard popup
             */
            getZx().getQuickContext().setEntry("-zXInStdPopupTab", "1");
            
            /**
             * If the pageflow has an overriding frameset rows value, use that regardless of the above
             */
            if (StringUtil.len(getFramesetrows()) != 0) {
                strFramesetRows = getFramesetrows();
            }
            
            getPageflow().getPage().s.append("<frameset rows='").append(strFramesetRows).append("' name='fraDetailsSet'>").appendNL();
            
            getPageflow().getPage().s.append("<frame name='fraHeader' src='")
            								.append(strUrl).append("' marginwidth='0' marginheight='0' scrolling='no' frameborder='0' noresize>").appendNL();
            
            /**
             * The buttons is where all the hard work is going to be
             * DGS15MAY2003: If propagating QS, make sure all other QS values are passed on in the URL. To
             * make life simple, load certain required values into the quick context, then use a temporary
             * URL object to construct the URL by calling constructURL, which will propagate as required.
             * DGS15MAY2003: Use -zxpk rather than -pk to avoid conflict.
             * DGS08JUL2004: New QS value for vo.lckdBy
             */
            if(getPageflow().getPFDesc().isPropagaTeqs()) {
                    getZx().getQuickContext().setEntry("-a", "aspButtons");
                    getZx().getQuickContext().setEntry("-s", getPageflow().getQs().getEntryAsString("-s"));
                    getZx().getQuickContext().setEntry("-ss", getPageflow().getQs().getEntryAsString("-ss"));
                    getZx().getQuickContext().setEntry("-pf", getPageflow().getQs().getEntryAsString("-pf"));
                    getZx().getQuickContext().setEntry("-action", getPageflow().getQs().getEntryAsString("-action"));
                    getZx().getQuickContext().setEntry("-vo", getPageflow().getQs().getEntryAsString("-vo"));
                    getZx().getQuickContext().setEntry("-vo.lckdBy", getPageflow().getQs().getEntryAsString("-vo.lckdBy"));
                    getZx().getQuickContext().setEntry(getQspk(), getPageflow().getQs().getEntryAsString("-zxpk"));
                    
                    PFUrl objUrl = new PFUrl();
                    objUrl.setUrl("../jsp/zXStdPopup.jsp");
                    strUrl = new StringBuffer(getPageflow().constructURL(objUrl));
                    
            } else {
                	strUrl = new StringBuffer("../jsp/zXStdPopup.jsp?-a=aspButtons");
                    strUrl.append("&-s=").append(getPageflow().getQs().getEntryAsString("-s"));
                    strUrl.append("&-ss=").append(getPageflow().getQs().getEntryAsString("-ss"));
                    strUrl.append("&-pf=" + getPageflow().getQs().getEntryAsString("-pf"));
                    strUrl.append("&-action=").append(getPageflow().getQs().getEntryAsString("-action"));
                    strUrl.append("&-vo=").append(getPageflow().getQs().getEntryAsString("-vo"));
                    strUrl.append("&-vo.lckdBy=").append(getPageflow().getQs().getEntryAsString("-vo.lckdBy"));
                    
                    /**
                     * -pk can be overruled
                     * DGS15MAY2003: Use -zxpk rather than -pk to avoid conflict.
                     */
                    strUrl.append("&").append(getQspk()).append("=").append(getPageflow().getQs().getEntryAsString("-zxpk"));
            }
            
            getPageflow().getPage().s.append("<frame name='fraTabButtons' src='")
            			 .append(strUrl)
            			 .append("' marginwidth='0' marginheight='0' scrolling='no' frameborder='0'  noresize>")
            			 .appendNL();
            
            int intTabs = 1;
            if (getTabs() != null) {
            	intTabs = getTabs().size();
            }
            
            /**
             * fraDetailsInnerset open
             */
            getPageflow().getPage().s.append("<frameset rows='")
            					.append(frameSizeString(intTabs, 1))
            					.append("' name='fraDetailsInnerSet' frameborder='no' border='0' framespacing='0'>")
            					.appendNL();
            
            /**
             * Actual frames
             */
            if (getTabs() != null) {
	            PFRef objRef;
	            String strRefName;
	            for (int i = 0; i < intTabs; i++) {
	                objRef = (PFRef)getTabs().get(i);
	                strRefName = objRef.getName().toLowerCase();
	                
	                getPageflow().getPage().s.append("<frame name='fra").append(strRefName)
	                		.append("' src='../html/zXBlank.html'  marginwidth='0' marginheight='0' scrolling='auto' frameborder='0'  noresize>")
	                		.appendNL();
	                
	            }
            }
            
			/**
			 * n-th frame is the action frame (not always used but always available)
			 */
			getPageflow().getPage().s
				 .appendNL("<frame name='frazxdoaction' src='../html/zXBlank.html'  marginwidth='0' marginheight='0' scrolling='auto' frameborder='0'  noresize>");
			getPageflow().getPage().s.appendNL("</frameset>"); // fraDetailsInnerset
			getPageflow().getPage().s.appendNL("</frameset>"); // fraDetailsSet
			
			getPageflow().getPage().s.appendNL("</frameset>"); // First inner
            
            /**
             * Determine footer URL (if at all applicable).
             * NOTE : If there is not footer the subsession will not be closed.
             */
            if (isFooter()) {
                if (StringUtil.len(getFooterurl().getUrl()) == 0) {
                    strUrl = new StringBuffer("../jsp/zXStdPopup.jsp?-a=aspFooter")
                            .append("&-s=").append(getPageflow().getQs().getEntryAsString("-s"))
                            .append("&-ss=").append(getPageflow().getQs().getEntryAsString("-ss"))
                            .append("&-pf=").append(getPageflow().getQs().getEntryAsString("-pf"))
                            .append("&-action=").append(getPageflow().getQs().getEntryAsString("-action"));
                    /**
                     * -pk can be overruled
                     * DGS15MAY2003: Use -zxpk rather than -pk to avoid conflict.
                     */
                    strUrl.append("&").append(getQspk()).append("=").append(getPageflow().getQs().getEntryAsString("-zxpk"));
                    
                    /**
                     * May need to pass zXPopupDef
                     */
                    String strPopupDef = getPageflow().getQs().getEntryAsString("-zXPopupDef");
                    if (StringUtil.len(strPopupDef) > 0) {
                        strUrl.append("&-zXPopupDef=").append(strPopupDef);
                    }
                    
                } else {
                    getPageflow().getQs().setEntry(getQspk(), getPageflow().getQs().getEntryAsString("-zxpk"));
                    strUrl = new StringBuffer(getPageflow().constructURL(getFooterurl()));
                }
                
                getPageflow().getPage().s.append("<frame name='fraFooter' src='")
                		.append(strUrl)
                		.append("' vspace='0' marginwidth='0' marginheight='0' scrolling='no' frameborder='0' noresize>")
                		.appendNL();
                
			} else {
			    getPageflow().getPage().s.appendNL("<frame name='fraFooter' src='../html/zXBlank.html' vspace='0' marginwidth='0' marginheight='0' scrolling='no' frameborder='0' noresize>");
			}
			
			getPageflow().getPage().s.appendNL("</frameset>"); //  Outer
			
			return generateFrameset;
            
		} catch (Exception e) {
		    getZx().trace.addError("Failed to : Generate frameset HTML.", e);
		    if (getZx().throwException) throw new ZXException(e);
		    generateFrameset = zXType.rc.rcError;
		    return generateFrameset;
		} finally {
		    // Restore debugmode setting.
		    getPageflow().getPFDesc().setDebugMode(enmDebugMode);
		    
		    if(getZx().trace.isFrameworkTraceEnabled()) {
		        getZx().trace.returnValue(generateFrameset);
		        getZx().trace.exitMethod();
		    }
		}
	}    
    
	/**
	 * Resolve entity collection.
	 *
	 * @return Returns a collection of PFEntities.
	 * @throws ZXException Thrown if resolveEntities fails. 
	 */
	private ZXCollection resolveEntities() throws ZXException{
		if(getZx().trace.isFrameworkTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
		ZXCollection resolveEntities = null;
		 
		try {
			/**
			 * Resolve entities as we may want to refer to them in the URL directors
			 */
			String strPK;
			 
			resolveEntities = getPageflow().getEntityCollection(this, zXType.pageflowActionType.patNull, zXType.pageflowQueryType.pqtAll);
			 
			if (resolveEntities != null) {
				getPageflow().setContextEntities(resolveEntities);
				 
				PFEntity objEntity;
				 
				Iterator iter = resolveEntities.iterator();
				while (iter.hasNext()) {
					objEntity = (PFEntity)iter.next();
					
					getPageflow().processAttributeValues(objEntity);
					
					String strLoadGroup;
					if (StringUtil.len(objEntity.getSelectlistgroup()) > 0) {
					    strLoadGroup = objEntity.getSelecteditgroup();
					} else {
					    strLoadGroup = objEntity.getSelectlistgroup();
					}
					
					strPK = getPageflow().resolveDirector(objEntity.getPk());
					if (StringUtil.len(strPK) > 0) {
					    objEntity.getBo().setPKValue(strPK);
					    objEntity.getBo().loadBO(strLoadGroup);
					} else {
					    if (StringUtil.len(strPK) > 0) {
					        objEntity.getBo().loadBO(strLoadGroup, objEntity.getPkwheregroup(), false);
					    }
				    }                     
				}
			}
			 
			return resolveEntities;
		} catch (Exception e) {
		    getZx().trace.addError("Failed to : Resolve entity collection.", e);
		    
		    if (getZx().throwException) throw new ZXException(e);
		    return resolveEntities;
		} finally {
		    if(getZx().trace.isFrameworkTraceEnabled()) {
		        getZx().trace.returnValue(resolveEntities);
		        getZx().trace.exitMethod();
		    }
		}
	}
     
    /**
     * Generate string like 0,*,0,0,0,0 to resize frames.
     * 
     * @param pintTotalFrames The total number frames used.
     * @param pintActiveFrame The active frame.
     * @return Returns the frameset sizing for the html attribute.
     */
    private String frameSizeString(int pintTotalFrames, int pintActiveFrame) {
        StringBuffer frameSizeString = new StringBuffer(pintTotalFrames * 2 - 1);
        
        /**
         * Note that we use total frames count plus 1, so that we have one extra frame for the actions.
         */
        for (int j = 1; j <= pintTotalFrames + 1; j++) {
            if (j > 1) {
                frameSizeString.append(COMMA);
            }
            frameSizeString.append(j == pintActiveFrame?START:ZERO);
        }
        
        return frameSizeString.toString();
    }
    
    //------------------------ PFAction Overidden methods 
    
    /** 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();
        
        getDescriptor().xmlUrl("headerurl", getHeaderurl());
        getDescriptor().xmlUrl("footerurl", getFooterurl());
        getDescriptor().xmlUrl("navigationurl", getNavigationurl());
        
        if (getTabs() != null && getTabs().size() > 0) {
            objXMLGen.openTag("tabs");
            PFRef objRef;
            
            int intTabs = getTabs().size();
            for (int i = 0; i < intTabs; i++) {
                objRef = (PFRef)getTabs().get(i);
                
                getDescriptor().xmlRef("tab", objRef);
            }
            objXMLGen.closeTag("tabs");
        }
        
        objXMLGen.taggedValue("lockentity", getLockentity());
        objXMLGen.taggedValue("lockid", getLockid());
        
        objXMLGen.taggedValue("qspk", getQspk());
        objXMLGen.taggedValue("framesetrows", getFramesetrows());
        objXMLGen.taggedValue("framesetcols", getFramesetcols());
        
        objXMLGen.taggedValue("navigation", isNavigation());
        objXMLGen.taggedValue("footer", isFooter());
    }  
    
    /** 
     * @see PFAction#clone(Pageflow)
     **/
    public PFAction clone(Pageflow pobjPageflow) {
        PFStdPopup cleanClone = (PFStdPopup)super.clone(pobjPageflow);
        
        cleanClone.setLockid(getLockid());
        cleanClone.setLockentity(getLockentity());
        cleanClone.setFramesetcols(getFramesetcols());
        
        cleanClone.setNavigation(isNavigation());
        cleanClone.setFooter(isFooter());
        
        if (getHeaderurl() != null) {
            cleanClone.setHeaderurl((PFUrl)getHeaderurl().clone());
        }
        
        if (getFooterurl() != null) {
            cleanClone.setFooterurl((PFUrl)getFooterurl().clone());
        }
        
        cleanClone.setFramesetrows(getFramesetrows());
        
        if (getTabs() != null && getTabs().size() > 0) {
            cleanClone.setTabs(CloneUtil.clone(getTabs()));
        }
        
        if (getNavigationurl() != null) {
            cleanClone.setNavigationurl((PFUrl)getNavigationurl().clone());
        }
        
        cleanClone.setQspk(getQspk());
        
        return cleanClone;
	}
}