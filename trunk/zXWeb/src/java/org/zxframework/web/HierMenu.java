/*
 * Created on May 25, 2004 by Michael Brewer
 * $Id: HierMenu.java,v 1.1.2.28 2006/07/17 13:54:53 mike Exp $
 */
package org.zxframework.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.digester.Digester;

import org.zxframework.Environment;
import org.zxframework.Label;
import org.zxframework.LabelCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.exception.ParsingException;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Hierarchical menu support.
 * 
 * <pre>
 * 
 * Change    : BD4DEC02
 * Why       : Missing '>' in img tag in menuHierItem
 * 
 * Change    : BD13DEC02
 * Why       : Added toggle image to main level of menu
 * 
 * Change    : BD24FEB04
 * Why       : Support for non-standard characters in menu name
 * 
 * Change    : DGS19MAR04
 * Why       : Support for definable indentation string (not just 3 spaces); option
 *             to make all of a menu line selectable; ability to start with a sub-menu
 *             open.
 * 
 * Change    : DGS21APR04
 * Why       : Related to above: Allow text indentation to be switched off
 * 
 * Change    : BD19JUN04
 * Why       : Added isDirector option
 * 
 * Change    : V1.4:73 DGS04MAY2005
 * Why       : When parsing, for forward compatibility, don't raise an error if an unknown tag
 *             is found. Do trace it if tracing is on.
 *             
 * Change    : V1.5:95 DGS28APR2006
 * Why       : New feature to be able to specify images for each main menu heading and item
 *             
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class HierMenu extends ZXObject {
    
    //------------------------ Members
	
    // XML Values :
    private String name;
    private LabelCollection title;
    private List groups;
    private ArrayList items;
    private MenuURL url;
    private boolean startopen;
    private boolean lineselect;
    private char[] indentation;
    private boolean notextindentation;
    private String menuitem;
    private String menuitemover;
    private String menuopen;
    private String menuclosed;
    
    // A handle the page object model :
    private PageBuilder page;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     * 
     * Care when using this constructor it should only be used during the parsing.
     */
    public HierMenu() {
        super();
        
        this.items = new ArrayList();
    }
    
    /**
     * Initialise hierMenu object. Do not bother with tracing etc..
     *
     * @param pobjPage A handle to the page object model. 
     */
    public void init(PageBuilder pobjPage){
        this.page = pobjPage;
    }
    
    //------------------------ Getters/Setters
    
    /**
     * A collection (String) of group permissions for the menu entry.
     * 
     * @return Returns the groups.
     */
    public List getGroups() {
        return groups;
    }
    
    /**
     * @param groups The groups to set.
     */
    public void setGroups(List groups) {
        this.groups = groups;
    }
    
    /**
     * DGS19MAR2004: New property to define the indentation string. Defaults to 3 non-breaking spaces.
     * @return Returns the indentation.
     */
    public char[] getIndentation() {
        return indentation;
    }
    
    /**
     * @param indentation The indentation to set.
     */
    public void setIndentation(char[] indentation) {
        this.indentation = indentation;
    }
    
    /**
     * A collection (MenuUrl) of menu items.
     * 
     * @return Returns the items.
     */
    public ArrayList getItems() {
        return items;
    }
    
    /**
     * @param items The items to set.
     */
    public void setItems(ArrayList items) {
        this.items = items;
    }
    
    /**
     * DGS19MAR2004: New property (applies at top level only) to define that a line in the menu can be selected
     * by clicking any part of it, not just the leftmost icon. Applies to submenus and items.
     * 
     * @return Returns the lineselect.
     */
    public boolean isLineselect() {
        return lineselect;
    }
    
    /**
     * @param lineselect The lineSelect to set.
     */
    public void setLineselect(boolean lineselect) {
        this.lineselect = lineselect;
    }
    
    /**
     * The name of the link.
     * 
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = StringUtil.stripChars("./\\", name);
    }
    
    /**
     * DGS21APR2004: New property (applies at top level only) to define that the menu titles are 
     * not idented, only the selectable icon.
     * 
     * @return Returns the notextindentation.
     */
    public boolean isNotextindentation() {
        return notextindentation;
    }
    
    /**
     * @param notextindentation The noTextIndentation to set.
     */
    public void setNotextindentation(boolean notextindentation) {
        this.notextindentation = notextindentation;
    }
    
    /**
	 * @return the menuclosed
	 */
	public String getMenuclosed() {
		return menuclosed;
	}

	/**
	 * @param menuclosed the menuclosed to set
	 */
	public void setMenuclosed(String menuclosed) {
		this.menuclosed = menuclosed;
	}

	/**
	 * The menu item icon.
	 * 
	 * <pre>
	 * 
	 * v1.5:95 DGS26APR2006: New properties to override images
	 * </pre>
	 * 
	 * @return the menuitem
	 */
	public String getMenuitem() {
		return menuitem;
	}

	/**
	 * @param menuitem the menuitem to set
	 */
	public void setMenuitem(String menuitem) {
		this.menuitem = menuitem;
	}

	/**
	 * The menu item over item.
	 * 
	 * @return the menuitemover
	 */
	public String getMenuitemover() {
		return menuitemover;
	}

	/**
	 * @param menuitemover the menuitemover to set
	 */
	public void setMenuitemover(String menuitemover) {
		this.menuitemover = menuitemover;
	}

	/**
	 * The opened menu icon.
	 * 
	 * @return the menuopen
	 */
	public String getMenuopen() {
		return menuopen;
	}

	/**
	 * @param menuopen the menuopen to set
	 */
	public void setMenuopen(String menuopen) {
		this.menuopen = menuopen;
	}
	
    /**
     * DGS19MAR2004: New property to define a submenu as starting already open.
     * 
     * @return Returns the startopen.
     */
    public boolean isStartopen() {
        return startopen;
    }
    
    /**
     * @param startopen The startopen to set.
     */
    public void setStartopen(boolean startopen) {
        this.startopen = startopen;
    }
    
    /**
     * The internationalized title of the menu entry.
     * 
     * @return Returns the title.
     */
    public LabelCollection getTitle() {
        return title;
    }
    
    /**
     * @param title The title to set.
     */
    public void setTitle(LabelCollection title) {
        this.title = title;
    }
    
    /**
     * A menu url.
     * 
     * @return Returns the url.
     */
    public MenuURL getUrl() {
        return url;
    }
    
    /**
     * @param url The url to set.
     */
    public void setUrl(MenuURL url) {
        this.url = url;
    }
    
	//--------------------------------- Runtime getters/setters
	
	/**
     * @return Returns the page.
     */
    public PageBuilder getPage() {
        return page;
    }
    
    /**
     * @param page The page to set.
     */
    public void setPage(PageBuilder page) {
        this.page = page;
    }
    
    
    //------------------------ Parsing methods.
    
    /**
     * ReadMenuFileialise the hierMenu object.
     *
     * @param pstrXmlFile The filename of the Main Menu xml file, with out the path. 
     * @return Returns the return code of the method. @see zXType#rc 
     * @throws ParsingException  Thrown if readMenuFile fails.
     */
    public zXType.rc readMenuFile(String pstrXmlFile) throws ParsingException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrXmlFile", pstrXmlFile);
        }
        
        zXType.rc readMenuFile = zXType.rc.rcOK; 

        try {
            
            /**
             * If no XML file given; done
             */
            if (StringUtil.len(pstrXmlFile) == 0) {
                return readMenuFile;
            }
            
            /**
             * Parse the menu xml file
             */
            parseMenuFile(getZx().fullPathName(getZx().getPageflowDir())  + File.separatorChar + pstrXmlFile);
            
            return readMenuFile;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : ReadMenuFileialise the hierMenu object.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrXmlFile = "+ pstrXmlFile);
            }
            if (getZx().throwException) throw new ParsingException(e);
            readMenuFile = zXType.rc.rcError;
            return readMenuFile;
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(readMenuFile);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Parsing the menu file.
     *
     * @param pstrXmlFile The Main menu file name. Including the full path. 
     * @return Returns the return code of the method.
     * @throws ParsingException Thrown if parseMenuFile fails. 
     */
    private zXType.rc parseMenuFile(String pstrXmlFile) throws ParsingException {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrXmlFile", pstrXmlFile);
        }
        
        zXType.rc parseMenuFile = zXType.rc.rcOK;
        
        try {
            /**
             * DGS19MAR2004: Default the indentation to 3 non-breaking spaces but it can be
             * defined in the menu XML.
             */
            this.indentation = PageBuilder.space(3);
            
            MainMenuRuleSet ruleSet = new MainMenuRuleSet();
            
            Digester digester = new Digester();
            digester.push(this);
            digester.addRuleSet(ruleSet);
            digester.parse(new File(pstrXmlFile));
            
            return parseMenuFile;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : readMenuFileialise the hierMenu object.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrXmlFile = " + pstrXmlFile);
            }
            throw new ParsingException(e);
            
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(parseMenuFile);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Methods to generate the menu
    
    /**
     * Check whether current user is in the menu group.
     *
     * @param pobjMenu The heirMenu item.
     * @return Returns whether the current user is in menu group.
     * @throws ZXException  Thrown if userInGroup fails. 
     */
    private boolean userInGroup(HierMenu pobjMenu) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjMenu", pobjMenu);
        }
        
        boolean userInGroup = false; 

        try {
        	/**
        	 * Short circuit if we have no groups defined.
        	 */
        	if (pobjMenu.getGroups() == null) return userInGroup;
        	
            int intGroups= pobjMenu.getGroups().size();
            for (int i = 0; i < intGroups; i++) {
                if (getZx().getUserProfile().isUserInGroup((String)pobjMenu.getGroups().get(i))) {
                    userInGroup = true;
                    return userInGroup;
                }
            }
            
            return userInGroup;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(userInGroup);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for menu.
     *
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if generateMenu fails. 
     */
    public zXType.rc generateMenu() throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        zXType.rc generateMenu = zXType.rc.rcOK; 
        
        String strMenuClosed;
        String strMenuOpen;
        
        try {
            boolean blnEven = true;
            
            /**
             * Generate top border
             * DGS19MAR2004: Missing tr added in
             */
            this.page.s.appendNL("<table width=\"100%\"><tr>");
            
            this.page.s.append("<td ").appendAttr("width", this.page.getWebSettings().getMenuColumn1()).appendNL('>');
            this.page.s.appendNL("</td>");
            
            this.page.s.append("<td ")
            		   .appendAttr("class", "zXMenuTopNoAlign")
            		   .appendAttr("align", "left")
            		   .appendAttr("valign", "center")
            		   .appendAttr("width", this.page.getWebSettings().getMenuColumn2())
            		   .appendNL('>');
            
            /**
             * BD13DEC02: Added toggle image to main level of menu
             * HTML.attr("src", "../images/menuClosed.gif") &
             */
            strMenuClosed = "../images/menuClosed.gif";
            strMenuOpen = "../images/menuOpen.gif";
            
            if (StringUtil.len(getMenuclosed()) > 0) {
                strMenuClosed = getMenuclosed();
                strMenuOpen = strMenuClosed;
            }
            
            if (StringUtil.len(getMenuopen()) > 0) {
                strMenuOpen = getMenuopen();
                if (StringUtil.len(getMenuclosed()) == 0) {
                    strMenuClosed = getMenuopen();
                }
            }
            
            this.page.s.append("<img ")
            		   .appendAttr("id", "_mnuLvlImg_Main")
            		   .appendAttr("src", strMenuClosed)
            		   
            		   /**
            		    * Accesskey support
            		    */
  					   .appendAttr("accessKey", "l") 
					   .appendAttr("tabindex", "1") 
					   .appendAttr("onKeyPress", "zXMMToggleWholeMenu();")
					   
            		   .appendAttr("onClick", "zXMMToggleWholeMenu();")
            		   .appendNL('>');
            
            if (StringUtil.len(getMenuopen()) > 0 || StringUtil.len(getMenuclosed()) > 0) {
            	this.page.s.appendNL("<script language='Javascript'>");
            	this.page.s.appendNL("var _mnuLvlImg_Main_OPEN = '" + strMenuOpen + "';");
            	this.page.s.appendNL("var _mnuLvlImg_Main_CLOSED = '" + strMenuClosed + "';");
	            this.page.s.appendNL("</script>");
            }
            
            this.page.s.appendNL("</td>");
            this.page.s.append("<td ")
            		   .appendAttr("class", "zXMenuTop")
            		   .appendAttr("width", this.page.getWebSettings().getMenuColumn3())
            		   .appendNL('>');
            
            this.page.s.append(getLabel(this.title));
            this.page.s.appendNL("</td>");
            
            this.page.s.append("<td ")
            		   .appendAttr("width", this.page.getWebSettings().getMenuColumn4())
            		   .appendNL('>');
            this.page.s.appendNL("</td>");
            
            this.page.s.appendNL("</tr>");
            this.page.s.appendNL("</table>");
            
            /**
             * And generate menu
             */
            int intItems = getItems().size();
            HierMenu objMenu;
            for (int i = 0; i < intItems; i++) {
                objMenu = (HierMenu)getItems().get(i);
                
                blnEven = generateMenuItem(objMenu, 0, blnEven);
            }
            
            return generateMenu;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for menu.", e);
            if (getZx().throwException) throw new ZXException(e);
            generateMenu = zXType.rc.rcError;
            return generateMenu;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(generateMenu);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get the label for the appropriate language.
     * 
     * <pre>
     * 
     * DGS10SEP2003: Added this because cannot use the same function in clsPageflow as we
     * have no pageflow when generating the menu.
     * </pre>
     *
     * @param pcolLabel The label collection 
     * @return Returns correct label for the users language.
     */
    private String getLabel(LabelCollection pcolLabel) {
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pcolLabel", pcolLabel);
        }
        
        String getLabel = ""; 
        
        try {
        	/**
        	 * Short circuit if pcolLabel is null.
        	 */
        	if (pcolLabel == null) return getLabel;
        	
        	Label objLabel = pcolLabel.get(getZx().getLanguage());
        	
        	// NOTE: Strictly speaking this should be configurable.
            if (objLabel == null && !Environment.DEFAULT_LANGUAGE.equalsIgnoreCase(getZx().getLanguage())) {
            	/**
            	 * Use the default language.
            	 */
                objLabel = pcolLabel.get(Environment.DEFAULT_LANGUAGE);
            } 
            if (objLabel != null) {
            	getLabel = objLabel.getLabel();
            }
            
            return getLabel;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(getLabel);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Generate HTML for single menu item.
     *
     * @param pobjMenu The menu item to generate
     * @param pintLevel The level of indentation
     * @param pblnEven Whether the row is odd or even.
     * @return Returns whether the new row is odd or even.
     * @throws ZXException  Thrown if generateMenuItem fails. 
     */
    public boolean generateMenuItem(HierMenu pobjMenu, int pintLevel, boolean pblnEven) throws ZXException{
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjMenu", pobjMenu);
            getZx().trace.traceParam("pintLevel", pintLevel);
            getZx().trace.traceParam("pblnEven", pblnEven);
        }
        
        boolean generateMenuItem = pblnEven; 
        
        try {
            /**
             * First check whether user is in the groups for this menu
             */
            if (!userInGroup(pobjMenu)) {
                return generateMenuItem;
            }
            
            pblnEven = !pblnEven;
            generateMenuItem = pblnEven;
            
            int intItems = pobjMenu.getItems().size();
            if (intItems > 0) {
                /**
                 * Sub menu
                 */
                String strMenuName = "_mnu_" + pobjMenu.getName();
                
                this.page.s.appendNL("<table width=\"100%\" border=\"0\">");
                // this.page.s.appendNL("<tr><td>");
                
                /**
                 * DGS19MAR2004: Changes for 'start open' and 'line select' mean that some
                 * of the setting of the URL is done in the called method not here
                 */
                menuHierItem(strMenuName,
                        	 getLabel(pobjMenu.getTitle()),
                        	 generateMenuItem,
                        	 "Click to open / close menu", 
                        	 pintLevel, true, 
                        	 pobjMenu.isStartopen(),
                        	 pobjMenu.getMenuitem(), pobjMenu.getMenuitemover(),
                             pobjMenu.getMenuopen(), pobjMenu.getMenuclosed());
                
                //this.page.s.appendNL("</td></tr>");
                this.page.s.appendNL("</table>");
                
                /**
                 * DGS19MAR2004: Change for 'start open':  might not be style=display:none
                 */
                this.page.s.append("<div id=\"").append(strMenuName).append("\" ").appendNL(pobjMenu.isStartopen()?"":"style='display:none'>");
                this.page.s.appendNL("<table width=\"100%\">");
                //this.page.s.appendNL("<tr><td>");

                /**
                 * Child menus' sequence starts with the opposite colout to their parent.
                 */
                boolean blnChildEven = pblnEven;
                HierMenu objNextMenu;
                for (int i = 0; i < intItems; i++) {
                    objNextMenu = (HierMenu)pobjMenu.getItems().get(i);
                    
                    blnChildEven = generateMenuItem(objNextMenu, pintLevel + 1, blnChildEven);
                }
                
                // this.page.s.appendNL("</td></tr>");
                this.page.s.appendNL("</table>");
                this.page.s.appendNL("</div>");
                
            } else if ((pobjMenu.getUrl() == null || StringUtil.len(pobjMenu.getUrl().getUrl()) == 0) && intItems == 0) {
                /**
                 * No link required if no URL
                 */
                this.page.s.appendNL("<table width=\"100%\">");
                
                menuHierItem("", 
                			 getLabel(pobjMenu.getTitle()),
                             pblnEven, 
                             "Go to " + getLabel(pobjMenu.getTitle()),
                             pintLevel, false, 
                             pobjMenu.isStartopen(),
                        	 pobjMenu.getMenuitem(), pobjMenu.getMenuitemover(),
                             pobjMenu.getMenuopen(), pobjMenu.getMenuclosed());
                
                this.page.s.appendNL("</table>");
                
            } else {
                String strUrl = resolveURL(pobjMenu.getUrl());
                
                this.page.s.appendNL("<table width=\"100%\">");
                
                /**
                 * DGS19MAR2004: Change for 'start open' - pass boolean
                 */
                menuHierItem(strUrl,
                        	 getLabel(pobjMenu.getTitle()),
                        	 pblnEven, 
                        	 "Go to " + getLabel(pobjMenu.getTitle()),
                        	 pintLevel, false, pobjMenu.isStartopen(),
                        	 pobjMenu.getMenuitem(), pobjMenu.getMenuitemover(),
                             pobjMenu.getMenuopen(), pobjMenu.getMenuclosed());
                
                this.page.s.appendNL("</table>");
            }
            
            return generateMenuItem;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for single menu item.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjMenu = "+ pobjMenu);
                getZx().log.error("Parameter : pintLevel = "+ pintLevel);
                getZx().log.error("Parameter : pblnEven = "+ pblnEven);
            }
            if (getZx().throwException) throw new ZXException(e);
            return generateMenuItem;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(generateMenuItem);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Resolve to a runtime url.
     * 
     * @param objUrl The MenuUrl to resolve
     * @return Returns the resolved url.
     * @throws Exception Thrown if resolveURL fails.
     */
    private String resolveURL(MenuURL objUrl) throws Exception {
    	StringBuffer resolveUrl = new StringBuffer();
    	
        /**
         * Menu option
         */
        String strTmp = objUrl.getUrl();
        
        /**
         * See if we are talking diretor stuff here
         */
        if (objUrl.isDirector()) {
        	strTmp = getZx().resolveDirector(objUrl.getUrl());
        }
        
    	boolean blnAppendEnd = strTmp.endsWith("')");
        if (blnAppendEnd) {
        	resolveUrl.append(strTmp.substring(0, strTmp.length() -2));
        } else {
        	resolveUrl.append(strTmp);
        }
        
        if (objUrl.isAppendsession()) {
        	/**
        	 * Add session id
        	 */
        	resolveUrl.append(strTmp.indexOf('-') != -1?"&":"")
        			  .append("-s=").append(getZx().getSession().getSessionid());
        }
        
        if (objUrl.isNewwindow()) {
        	/**
        	 * Subsession ids can only be 9 characters
        	 */
        	String strSubsessionID = String.valueOf(System.currentTimeMillis());
        	if (strSubsessionID.length() > 8) {
        		strSubsessionID = strSubsessionID.substring(strSubsessionID.length()-9);
        	}
        	
        	/**
        	 * Add subsession id
        	 */
        	resolveUrl.append(strTmp.indexOf('-')!= -1?"&":"")
        			  .append("-ss=").append(strSubsessionID);
        	
            if (blnAppendEnd) {
            	resolveUrl.append("')");
            }
            
            return "zXPopupStep1('" + getZx().getSession().getSessionid() + "','" + resolveUrl.toString() + "')";
        }
        
        if (blnAppendEnd) {
        	resolveUrl.append("')");
        }
        return resolveUrl.toString();
    }
    
    /**
     * Generate HTML for single hierarchical menu item (either level or item).
     * 
     * <pre>
     * 
     * DGS19MAR2004: Added subMenu and open parameters - affects how menu is generated
     * </pre>
     *
     * @param pstrURL The url of the menu item 
     * @param pstrDescription The description of the link. 
     * @param pblnEven Whether is it a odd or even element. 
     * @param pstrTitle The hover title of the url.
     * @param pintLevel The level of indentation. 
     * @param pblnSubMenu Whether it is a submenu. 
     * @param pblnOpen Whether to have the menu item open of not.
     * @param pstrMenuItem The menu item icon.
     * @param pstrMenuItemOver The menu item over icon.
     * @param pstrMenuOpen The menu open icon.
     * @param pstrMenuClosed The menu closed icon.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if menuHierItem fails. 
     */
    private zXType.rc menuHierItem(String pstrURL, 
    							   String pstrDescription, 
    							   boolean pblnEven, 
    							   String pstrTitle, 
    							   int pintLevel, 
    							   boolean pblnSubMenu,
    							   
    							   boolean pblnOpen,
    							   String pstrMenuItem,
    							   String pstrMenuItemOver,
    							   String pstrMenuOpen,
    							   String pstrMenuClosed) throws ZXException{
    	
        if(getZx().trace.isFrameworkTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrURL", pstrURL);
            getZx().trace.traceParam("pstrDescription", pstrDescription);
            getZx().trace.traceParam("pblnEven", pblnEven);
            getZx().trace.traceParam("pstrTitle", pstrTitle);
            getZx().trace.traceParam("pintLevel", pintLevel);
            getZx().trace.traceParam("pblnSubMenu", pblnSubMenu);
            getZx().trace.traceParam("pblnOpen", pblnOpen);
            getZx().trace.traceParam("pstrMenuItem", pstrMenuItem);
            getZx().trace.traceParam("pstrMenuItemOver", pstrMenuItemOver);
            getZx().trace.traceParam("pstrMenuOpen", pstrMenuOpen);
            getZx().trace.traceParam("pstrMenuClosed", pstrMenuClosed);
        }
        
        zXType.rc menuHierItem = zXType.rc.rcOK;
        
        try {
        	/**
        	 * v1.5:95 DGS26APR2006: New properties to override images - default values:
        	 */
        	String strMenuClosed = "../images/menuClosed.gif";
    	    String strMenuOpen = "../images/menuOpen.gif";
    	    String strMenuItem = "../images/menuItem.gif";
    	    String strMenuItemOver = "../images/menuItemOver.gif";
    	    
		    if (StringUtil.len(pstrMenuClosed) > 0) {
		        strMenuClosed = pstrMenuClosed;
		        strMenuOpen = pstrMenuClosed;
		    }
		    if (StringUtil.len(pstrMenuOpen) > 0) {
		        strMenuOpen = pstrMenuOpen;
		        if (StringUtil.len(pstrMenuClosed) == 0) {
		            strMenuClosed = pstrMenuOpen;
		        }
		    }
		    if (StringUtil.len(pstrMenuItem) > 0) {
		        strMenuItem = pstrMenuItem;
		        strMenuItemOver = pstrMenuItem;
		        if (StringUtil.len(pstrMenuItemOver) > 0) {
		            strMenuItemOver = pstrMenuItemOver;
		        }
		    }
        	
            /**
             * Some hocus-pocus to make sure that menu items and sub menu headers have
             * a different color scheme
             * DGS19MAR2004: Now use boolean to determine if is a submenu, not the presence
             * of zXSubMMenuToggle in the url
             */
            String strMenuItemClass;
            String strSubMenuClass;
            if (pblnEven) {
                if (pblnSubMenu) {
                    strMenuItemClass = "zxNorMenuItem";
                    strSubMenuClass = "zxNorSubMenu";
                } else {
                    strMenuItemClass = "zxNorMenuItemNor";
                    strSubMenuClass = "zxNorSubMenuNor";
                }
            } else {
                if (pblnSubMenu) {
                    strMenuItemClass = "zxAltMenuItem";
                    strSubMenuClass = "zxAltSubMenu";
                } else {
                    strMenuItemClass = "zxAltMenuItemNor";
                    strSubMenuClass = "zxAltSubMenuNor";
                }
            }
            
            StringBuffer strJSCAllBuffer = new StringBuffer(128);
            if (pblnSubMenu) {
                // OLD WAY  : Non Netscape complaint.
                // "zXSubMMenuToggle(_mnuLvlImg_" + pstrURL + "," + pstrURL + ");" +
                
                /**
                 * DGS19MAR2004: This is a submenu, so have to wrap the toggle javascript around the url.
                 * This code moved up from further down so that it is available to the tr, to support
                 * the 'line select' change
                 */
                strJSCAllBuffer.append("window.document.body.style.cursor='wait';zXSubMMenuToggle(this,'").append(pstrURL).append("');window.document.body.style.cursor='';");
            } else {
                strJSCAllBuffer.append("window.document.body.style.cursor='wait';").append(pstrURL).append(";window.document.body.style.cursor='';");
            }
            String strJSCAll = strJSCAllBuffer.toString();
            
            /**
             * DGS19MAR2004: If whole line is selectable, the javascript applies to the tr
             */
            this.page.s.append("<tr ");
            if (this.lineselect) {
            	this.page.s.appendAttr("onMouseDown", strJSCAll);
            	this.page.s.appendAttr("id", "menulineselect");            	
            }
            this.page.s.appendNL('>');
            
            this.page.s.append("<td ")
            		   .appendAttr("width", this.page.getWebSettings().getMenuColumn1())
            		   .appendNL('>');
            this.page.s.appendNL("</td>");
            
            this.page.s.append("<td ")
            		   .appendAttr("class", strMenuItemClass)
            		   .appendAttr("width", this.page.getWebSettings().getMenuColumn2())
            		   .appendNL('>');
            
            /**
             * DGS19MAR2004: Indentation is now defined in the XML, not fixed 3 spaces
             */
            StringBuffer strTmp =  new StringBuffer( indentation.length * pintLevel );
            for (int i = 0; i < pintLevel; i++) {
                strTmp.append(indentation);
            }
            this.page.s.append(strTmp);
            
            /**
             * DGS19MAR2004: More changes for submenu, line open etc.
             */
            String strImage;
            if (pblnSubMenu) {
                /**
                 * DGS19MAR2004: Sub menu item - can start open or closed
                 */
                // strImage = (pblnOpen?"../images/menuOpen.gif":"../images/menuClosed.gif");
            	strImage = (pblnOpen?strMenuOpen:strMenuClosed);
            	
                /**
                 * DGS19MAR2004: If whole line is NOT selectable, the javascript applies to the image
                 * Need to give the image a unique name so that it can be passed to the javascript
                 * (previously used 'this' but can now (potentially) call the function from anywhere on the row)
                 */
                this.page.s.append("<img ");
                if (!lineselect) {
                	this.page.s.appendAttr("onClick", strJSCAll);
                }
                
                /**
                 * AccessKeys Support
                 */
            	this.page.s.appendAttr("onKeyPress", strJSCAll)
            			   .appendAttr("accessKey", "l")
            			   .appendAttr("tabindex", "1");

                this.page.s.appendAttr("src", strImage)
                    	   .appendAttr("id", "_mnuLvlImg_" + pstrURL)
                    	   .appendAttr("border", "0")
                    	   .appendAttr("alt", pstrTitle)
                    	   .appendNL('>');
                
                if (StringUtil.len(pstrMenuOpen) > 0 || StringUtil.len(pstrMenuClosed) > 0) {
	                this.page.s.appendNL("<script language='Javascript'>");
	                this.page.s.appendNL("var _mnuLvlImg_" + pstrURL + "_OPEN = '" + strMenuOpen + "';");
	                this.page.s.appendNL("var _mnuLvlImg_" + pstrURL + "_CLOSED = '" + strMenuClosed + "';");
	                this.page.s.appendNL("</script>");
                }
                
                this.page.s.appendNL("</td>");
                
                this.page.s.append("<td ")
				           .appendAttr("class", strSubMenuClass)
				           .appendAttr("width", this.page.getWebSettings().getMenuColumn3())
				           .appendNL('>');
            } else {
                /**
                 * There is not link to click on, so do not draw a icon for one.
                 */
                if (StringUtil.len(pstrURL) > 0) {
                    /**
                     * Menu item
                     */
                    //strImage = "../images/menuItem.gif";
                	strImage = strMenuItem;
                	
                    /**
                     * BD19DEC02 Remove dir from zxiconfocus call as this is included
                     * in the JS function
                     * 
                     * v1.5:95 DGS26APR2006: We now have the image paths in variables, and have to use a
                     * variant of zXIconFocus that expects the full path of the image
                     */
                	strJSCAll = "javascript:zXIconFocusPath(this, '" + strMenuItem + "');" + strJSCAll;
                	
                	/**
                	 * v1.5:95 DGS26APR2006: We now have the image paths in variables, and have to use a
                	 * variant of zXIconFocus that expects the full path of the image
                	 */
                    this.page.s.append("<img ")
    				           .appendAttr("src", strImage)
    				           .appendAttr("border", "0")
    				           .appendAttr("onClick", strJSCAll)
    				           
				                /**
				                 * AccessKeys Support
				                 */
    				           .appendAttr("onKeyPress", strJSCAll)
		                	   .appendAttr("accessKey", "l")
		                	   .appendAttr("tabindex", "1")
    				           .appendAttr("onFocus", "zXIconFocus(this, 'menuItemOver.gif');")
    				           .appendAttr("onBlur", "zXIconFocus(this, 'menuItem.gif');")

			                   .appendAttr("onMouseOver", "zXIconFocusPath(this, '" + strMenuItemOver + "');")
			                   .appendAttr("onMouseOut", "zXIconFocusPath(this, '" + strMenuItem + "');")
    				           .appendNL('>');
                }
                
                this.page.s.appendNL("</td>");
                
                this.page.s.append("<td ")
			               .appendAttr("class", strMenuItemClass)
			               .appendAttr("width", this.page.getWebSettings().getMenuColumn3())
			               .appendNL('>');
            }
            
            /**
             * DGS21APR2004: Indentation is now defined in the XML, not fixed 3 spaces, and will not
             * apply at all to the text if the new boolean is set. Also avoid unnecessary LFs.
             */
            strTmp = new StringBuffer(!notextindentation?indentation.length*pintLevel:16);
            if (!notextindentation) {
                for (int i = 0; i < pintLevel; i++) {
                    strTmp.append(this.indentation);
                }
            }
            this.page.s.append(strTmp).append(pstrDescription).append("</td>");
            
            this.page.s.append("<td ")
            		   .appendAttr("width", this.page.getWebSettings().getMenuColumn4())
            		   .appendNL('>');		
            this.page.s.appendNL("</td>");
            
            this.page.s.appendNL("</tr>");
            
            return menuHierItem;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Generate HTML for single hierarchical menu item (either level or item).", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrURL = "+ pstrURL);
                getZx().log.error("Parameter : pstrDescription = "+ pstrDescription);
                getZx().log.error("Parameter : pblnEven = "+ pblnEven);
                getZx().log.error("Parameter : pstrTitle = "+ pstrTitle);
                getZx().log.error("Parameter : pintLevel = "+ pintLevel);
                getZx().log.error("Parameter : pblnSubMenu = "+ pblnSubMenu);
                getZx().log.error("Parameter : pblnOpen = "+ pblnOpen);
            }
            if (getZx().throwException) throw new ZXException(e);
            menuHierItem = zXType.rc.rcError;
            return menuHierItem;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(menuHierItem);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Repository methods
    
    /**
     * Dump Me as XML. This might be used in the java version of the repository editor.
     * 
     * @return Returns dump of the HierMenu as xml.
     */
    public String dumpAsXML() {
        XMLGen dumpAsXML = new XMLGen(1000);
        
        dumpAsXML.xmlHeader();
        
        dumpAsXML.openTag("mainmenu");
        
        /**
         * Top-level menu item only has title and item collections. Lower-levels have additional
         * tags - see dumpItem.
         */
        dumpAsXML.openTag("title");
        Label objLabel;
        Iterator iter = this.getTitle().iterator();
        while (iter.hasNext()) {
            objLabel = (Label)iter.next();
            dumpAsXML.taggedValue(objLabel.getLanguage(), objLabel.getLabel());
        }
        dumpAsXML.closeTag("title");
        
        /**
         * DGS19MAR2004: New tags for indentation and line select. For backwards compatibility,
         * don't write out unless different to old defaults
         */
        if (!this.indentation.equals(PageBuilder.space(3))) {
            dumpAsXML.taggedCData("indentation", new String(this.indentation));
        }
        if (this.lineselect) {
            dumpAsXML.taggedValue("lineselect", true);
        }
        
        /**
         * DGS21APR2004: New tag for 'no text indentation'. For backwards compatibility,
         * don't write out unless different to old default
         */
        if (this.notextindentation) {
            dumpAsXML.taggedValue("notextindentation", true);
        }
        
        /**
         * v1.5:95 DGS26APR2006: New tags for image overrides. Don't write out unless non-blank
         **/
        dumpAsXML.taggedCData("menuitem", getMenuitem());
        dumpAsXML.taggedCData("menuitemover", getMenuitemover());
        dumpAsXML.taggedCData("menuopen", getMenuopen());
        dumpAsXML.taggedCData("menuclosed", getMenuclosed());
        
        if (this.items.size() > 0) {
            dumpAsXML.openTag("items");
            
            /**
             * Call function for each item - the function will call itself for lower-level items:
             */
            dumpItem(dumpAsXML, this);
            
            dumpAsXML.closeTag("items");
        }
        
        dumpAsXML.closeTag("mainmenu");
        
        return dumpAsXML.toString();
    }
    
    /**
     * Called for each menu item, and calls itself recursively for each sub-item.
     * 
     * @param objXMLGen A handle to the xml generator
     * @param pobjMenuLine The menu item.
     */
    private void dumpItem(XMLGen objXMLGen, HierMenu pobjMenuLine) {
        Label objLabel;
        HierMenu objItem;
        Iterator iter2;
        String strGroup;
        
        int intItems = pobjMenuLine.getItems().size();
        for (int i = 0; i < intItems; i++) {
            objItem = (HierMenu)pobjMenuLine.getItems().get(i);
            
            objXMLGen.openTag("item");            
            objXMLGen.taggedValue("name", objItem.getName());
            
            objXMLGen.openTag("title");
            iter2 = objItem.getTitle().iterator();
            while (iter2.hasNext()) {
                objLabel = (Label)iter2.next();
                objXMLGen.taggedValue(objLabel.getLanguage(), objLabel.getLabel());
            }
            objXMLGen.closeTag("title");
            
            /**
             * DGS19MAR2004: New tag for start open. For backwards compatibility,
             * don't write out unless different to old default
             */
            if (objItem.isStartopen()) {
                objXMLGen.taggedValue("startopen", true); // or yes
            }
            
            /**
             * v1.5:95 DGS26APR2006: New tags for image overrides. Don't write out unless non-blank
             **/
            objXMLGen.taggedCData("menuitem", objItem.getMenuitem());
            objXMLGen.taggedCData("menuitemover", objItem.getMenuitemover());
            objXMLGen.taggedCData("menuopen", objItem.getMenuopen());
            objXMLGen.taggedCData("menuclosed", objItem.getMenuclosed());
            
            objXMLGen.openTag("groups");
            int intGroups = objItem.getGroups().size();
            for (int j = 0; j < intGroups; j++) {
                strGroup = (String)objItem.getGroups().get(i);
                objXMLGen.taggedValue("group", strGroup);
            }
            objXMLGen.closeTag("groups");
            
            if (objItem.getUrl() != null) {
                objXMLGen.addCustomContent(
                        new StringBuffer("<url appendsession=\"").append(objItem.getUrl().isAppendsession()?"yes":"no")
                            .append("\" newwindow=\"").append(objItem.getUrl().isNewwindow()?"yes":"no")
                            .append("\" isdirector=\"").append(objItem.getUrl().isDirector()?"yes":"no").append("\">")
                            .append(removeAmpersand(objItem.getUrl().getUrl())).append("</url>\n").toString());
            }
            
            if (objItem.getItems().size() > 0) {
                // Recursive call to this function:
                objXMLGen.openTag("items");
                dumpItem(objXMLGen, objItem);
                objXMLGen.closeTag("items");
            }
            
            objXMLGen.closeTag("item");
        }
    }
    
    /**
     * Removes ampersands and replaces by "&amp;". The main menu
     * cannot cope with raw ampersands. However, do not want to
     * encode other characters so cannot use encodeString.
     * 
     * @param str The url you want to clean. 
     * @return Returns a cleaned string.
     */
    private String removeAmpersand(String str) {
        return StringUtil.replaceAll(str, '&', "&amp;");
    }
    
    //------------------------ Special methods for AJAX
    
    /**
     * Searches your defined menu items.
     * 
     * @param strQury What to search
     * @return Returns the results.
     * @throws Exception Thrown if searchHierMenu fails
     */
    public String searchHierMenu(String strQury) throws Exception {
        String searchHierMenu = "<ul>";
        searchHierMenu = searchHierMenu + searchHierMenuItems(this, strQury);                
        searchHierMenu = searchHierMenu + "</ul>";
        return searchHierMenu;
    }
    
    /**
     * @param pobjMenu The root of the menu to search within.
     * @param strQury What we are searching for.
     * @return Returns a list of results.
     * @throws Exception
     */
    private String searchHierMenuItems(HierMenu pobjMenu, String strQury) throws Exception {
        StringBuffer searchHierMenu = new StringBuffer();
        
        HierMenu objMenuItem;
        String strItemLabel;
        String strUrl;
        MenuURL objMenuUrl;
        
        int size = pobjMenu.getItems().size();
        for (int i = 0; i < size; i++) {
            objMenuItem = (HierMenu) pobjMenu.getItems().get(i);
            strItemLabel = objMenuItem.getTitle().getLabel();
            objMenuUrl = objMenuItem.getUrl();
            
            if (userInGroup(objMenuItem)) {
                if (strItemLabel != null && objMenuUrl != null
                    && StringUtil.len(objMenuUrl.getUrl()) > 0) {
                	
                    if (objMenuItem.getTitle().getLabel().toLowerCase().indexOf(strQury.toLowerCase()) != -1
                        || objMenuItem.getName().toLowerCase().indexOf(strQury.toLowerCase()) != -1) {
                    	
                        strUrl = pobjMenu.resolveURL(objMenuUrl);
                        
                        searchHierMenu.append("<li onClick=\"");
                        searchHierMenu.append(strUrl);
                        searchHierMenu.append("\" title=\"");
                        searchHierMenu.append(strUrl);
                        searchHierMenu.append("\">");
                        searchHierMenu.append(strItemLabel);
                        searchHierMenu.append("</li>");
                    }
                }
                
                searchHierMenu.append(searchHierMenuItems(objMenuItem, strQury));
                
            }
            
        }
        
        return searchHierMenu.toString();
    }
    
    //------------------------ Object's overridden methods.
    
    /**
     * Creates a clone of the HierMenu, this is not a proper clone, so if you run into any problems,
     * this is the first place to look.
     *  
     * @see java.lang.Object#clone()
     **/
    public Object clone() {
        HierMenu objMenuItem = this;
        return objMenuItem;
    }
}