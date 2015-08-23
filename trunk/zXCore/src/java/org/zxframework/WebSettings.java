/*
 * Created on Mar 15, 2004 by Michael Brewer
 * $Id: WebSettings.java,v 1.1.2.9 2005/11/21 15:14:16 mike Exp $
 */
package org.zxframework;

import java.io.StringReader;

import org.apache.commons.digester.Digester;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import org.zxframework.exception.ParsingException;
import org.zxframework.util.StringUtil;

/**
 * Web specific settings.
 */
public final class WebSettings implements CloneableObject {
    
	//------------------------  Members
	
    private String editFormColumn1;
    private String editFormColumn2;
    private String editFormColumn3;
    private String editFormColumn4;
    
    private String searchFormColumn1;
    private String searchFormColumn2;
    private String searchFormColumn3;
    private String searchFormColumn4;
    
    private String listFormColumn1;
    
    private String menuColumn1;
    private String menuColumn2;
    private String menuColumn3;
    private String menuColumn4;
    
    private String treeColumn1;
    private String treeColumn2;
    private String treeColumn3;
    
    private int multilineCols;
    private int multilineRows;
    private int maxListRows;
    
    private String titleBulletImage;
    private String bgColorDark;
    private String bgColorLight;
    
    private String searchOrderByImage;
    
    /** Default to onClick search behaviour. */
    private zXType.webFKBehaviour FKSearchType = zXType.webFKBehaviour.wfkbOnClick;
    /** Defaults to max of 5 fk results. */
    private int fkMaxSize = 5;
    /** Defaults to 400ms delay between searches. */
    private int fkSearchDelay = 400;
    
    /** Values that where in global.asa */
    private String logoImg;
    private String pageTitle; // Like formTitle
    private String backgroundImg;
    
    /**
     * Date format for the javascript control.
     */
    private String dateFormat;
    private String timestampFormat;
    
    //------------------------  ZX Config values
    
    //------------------------ Constructors
    
    /**
     * Hide default constructor.
     */
    private WebSettings() {
    	super();
    }
    
    /**
     * @param pobjElement
     * @throws ParsingException Thrown if we have a parsing exception.
     */
    public WebSettings(Element pobjElement) throws ParsingException {
        /**
         * You are free to add what ever you want to the zx/html namespace as long as you 
         * a the appropiate setter/setter.
         * 
         * Cache html settings in production mode for a performance improvements.
         */
        try {
            if (pobjElement != null) {
                String strXML = new XMLOutputter().outputString(pobjElement);
                if (strXML != null) {
                	/**
                	 * NOTE : Parsing is make simple because all of the values
                	 * are autowired, but this means that we have to stick to a name convension.
                	 */
                    Digester d = new Digester();
                    d.addSetNestedProperties("html");
                    d.push(this);
                    d.parse(new StringReader(strXML));
                }
            }
            
        } catch (Exception e) {
            /** 
             * Bomb out here as we have a fatal exception, this should then be resolved before continuing.. 
             */
            throw new ParsingException("Failed to parse Page settings", e);
        }
    }
    
    //------------------------ Gettings/settings.
    
    /**
     * @return Returns the bgColorDark.
     */
    public String getBgColorDark() {
        return bgColorDark;
    }

    /**
     * @param pstrBgColorDark The bgColorDark to set.
     */
    public void setBgColorDark(String pstrBgColorDark) {
        bgColorDark = pstrBgColorDark;
    }

    /**
     * @return Returns the backgroundImg.
     */
    public String getBackgroundImg() {
        return backgroundImg;
    }
    
    /**
     * @param pstrBackgroundImg The backgroundImg to set.
     */
    public void setBackgroundImg(String pstrBackgroundImg) {
        backgroundImg = pstrBackgroundImg;
    }
    
    /**
     * @return Returns the logoImg.
     */
    public String getLogoImg() {
        return logoImg;
    }
    
    /**
     * @param pstrLogoImg The logoImg to set.
     */
    public void setLogoImg(String pstrLogoImg) {
        logoImg = pstrLogoImg;
    }
    
    /**
     * @return Returns the pageTitle.
     */
    public String getPageTitle() {
        return pageTitle;
    }
    /**
     * @param pstrPageTitle The pageTitle to set.
     */
    public void setPageTitle(String pstrPageTitle) {
        pageTitle = pstrPageTitle;
    }
    
    /**
     * @return Returns the bgColorLight.
     */
    public String getBgColorLight() {
        return bgColorLight;
    }

    /**
     * @param pstrBgColorLight The bgColorLight to set.
     */
    public void setBgColorLight(String pstrBgColorLight) {
        bgColorLight = pstrBgColorLight;
    }

    /**
     * Image used in search-order-by label.
     * 
     * @return Returns the searchOrderByImage.
     */
    public String getSearchOrderByImage() {
        return searchOrderByImage;
    }
    
    /**
     * @param pstrSearchOrderByImage The searchOrderByImage to set.
     */
    public void setSearchOrderByImage(String pstrSearchOrderByImage) {
        searchOrderByImage = pstrSearchOrderByImage;
    }
    /**
     * @return Returns the editFormColumn1.
     */
    public String getEditFormColumn1() {
        return editFormColumn1;
    }

    /**
     * @param pstrEditFormColumn1 The editFormColumn1 to set.
     */
    public void setEditFormColumn1(String pstrEditFormColumn1) {
        editFormColumn1 = pstrEditFormColumn1;
    }

    /**
     * @return Returns the editFormColumn2.
     */
    public String getEditFormColumn2() {
        return editFormColumn2;
    }
    
    /**
     * @param pstrEditFormColumn2 The editFormColumn2 to set.
     */
    public void setEditFormColumn2(String pstrEditFormColumn2) {
        editFormColumn2 = pstrEditFormColumn2;
    }

    /**
     * @return Returns the editFormColumn3.
     */
    public String getEditFormColumn3() {
        return editFormColumn3;
    }

    /**
     * @param pstrEditFormColumn3 The editFormColumn3 to set.
     */
    public void setEditFormColumn3(String pstrEditFormColumn3) {
        editFormColumn3 = pstrEditFormColumn3;
    }

    /**
     * @return Returns the editFormColumn4.
     */
    public String getEditFormColumn4() {
        return editFormColumn4;
    }

    /**
     * @param pstrEditFormColumn4 The editFormColumn4 to set.
     */
    public void setEditFormColumn4(String pstrEditFormColumn4) {
        editFormColumn4 = pstrEditFormColumn4;
    }

    /**
     * @return Returns the listFormColumn1.
     */
    public String getListFormColumn1() {
        return listFormColumn1;
    }

    /**
     * @param pstrListFormColumn1 The listFormColumn1 to set.
     */
    public void setListFormColumn1(String pstrListFormColumn1) {
        listFormColumn1 = pstrListFormColumn1;
    }

    /**
     * @return Returns the maxListRows.
     */
    public int getMaxListRows() {
        return maxListRows;
    }

    /**
     * @param pintMaxListRows The maxListRows to set.
     */
    public void setMaxListRows(int pintMaxListRows) {
        maxListRows = pintMaxListRows;
    }

    /**
     * @return Returns the menuColumn1.
     */
    public String getMenuColumn1() {
        return menuColumn1;
    }

    /**
     * @param pstrMenuColumn1 The menuColumn1 to set.
     */
    public void setMenuColumn1(String pstrMenuColumn1) {
        menuColumn1 = pstrMenuColumn1;
    }

    /**
     * @return Returns the menuColumn2.
     */
    public String getMenuColumn2() {
        return menuColumn2;
    }

    /**
     * @param pstrMenuColumn2 The menuColumn2 to set.
     */
    public void setMenuColumn2(String pstrMenuColumn2) {
        menuColumn2 = pstrMenuColumn2;
    }

    /**
     * @return Returns the menuColumn3.
     */
    public String getMenuColumn3() {
        return menuColumn3;
    }

    /**
     * @param pstrMenuColumn3 The menuColumn3 to set.
     */
    public void setMenuColumn3(String pstrMenuColumn3) {
        menuColumn3 = pstrMenuColumn3;
    }

    /**
     * @return Returns the menuColumn4.
     */
    public String getMenuColumn4() {
        return menuColumn4;
    }

    /**
     * @param pstrMenuColumn4 The menuColumn4 to set.
     */
    public void setMenuColumn4(String pstrMenuColumn4) {
        menuColumn4 = pstrMenuColumn4;
    }
    
    /**
     * @return Returns the multilineCols.
     */
    public int getMultilineCols() {
        return multilineCols;
    }

    /**
     * @param pintMultilineCols The multilineCols to set.
     */
    public void setMultilineCols(int pintMultilineCols) {
        multilineCols = pintMultilineCols;
    }

    /**
     * @return Returns the multilineRows.
     */
    public int getMultilineRows() {
        return multilineRows;
    }

    /**
     * @param pintMultilineRows The multilineRows to set.
     */
    public void setMultilineRows(int pintMultilineRows) {
        multilineRows = pintMultilineRows;
    }

    /**
     * @return Returns the searchFormColumn1.
     */
    public String getSearchFormColumn1() {
        return searchFormColumn1;
    }

    /**
     * @param pstrSearchFormColumn1 The searchFormColumn1 to set.
     */
    public void setSearchFormColumn1(String pstrSearchFormColumn1) {
        searchFormColumn1 = pstrSearchFormColumn1;
    }

    /**
     * @return Returns the searchFormColumn2.
     */
    public String getSearchFormColumn2() {
        return searchFormColumn2;
    }

    /**
     * @param pstrSearchFormColumn2 The searchFormColumn2 to set.
     */
    public void setSearchFormColumn2(String pstrSearchFormColumn2) {
        searchFormColumn2 = pstrSearchFormColumn2;
    }

    /**
     * @return Returns the searchFormColumn3.
     */
    public String getSearchFormColumn3() {
        return searchFormColumn3;
    }

    /**
     * @param pstrSearchFormColumn3 The searchFormColumn3 to set.
     */
    public void setSearchFormColumn3(String pstrSearchFormColumn3) {
        searchFormColumn3 = pstrSearchFormColumn3;
    }

    /**
     * @return Returns the searchFormColumn4.
     */
    public String getSearchFormColumn4() {
        return searchFormColumn4;
    }

    /**
     * @param pstrSearchFormColumn4 The searchFormColumn4 to set.
     */
    public void setSearchFormColumn4(String pstrSearchFormColumn4) {
        searchFormColumn4 = pstrSearchFormColumn4;
    }

    /**
     * @return Returns the titleBulletImage.
     */
    public String getTitleBulletImage() {
        return titleBulletImage;
    }

    /**
     * @param pstrTitleBulletImage The titleBulletImage to set.
     */
    public void setTitleBulletImage(String pstrTitleBulletImage) {
        titleBulletImage = pstrTitleBulletImage;
    }

    /**
     * @return Returns the treeColumn1.
     */
    public String getTreeColumn1() {
        return treeColumn1;
    }

    /**
     * @param pstrTreeColumn1 The treeColumn1 to set.
     */
    public void setTreeColumn1(String pstrTreeColumn1) {
        treeColumn1 = pstrTreeColumn1;
    }

    /**
     * @return Returns the treeColumn2.
     */
    public String getTreeColumn2() {
        return treeColumn2;
    }

    /**
     * @param pstrTreeColumn2 The treeColumn2 to set.
     */
    public void setTreeColumn2(String pstrTreeColumn2) {
        treeColumn2 = pstrTreeColumn2;
    }

    /**
     * @return Returns the treeColumn3.
     */
    public String getTreeColumn3() {
        return treeColumn3;
    }

    /**
     * @param pstrTreeColumn3 The treeColumn3 to set.
     */
    public void setTreeColumn3(String pstrTreeColumn3) {
        treeColumn3 = pstrTreeColumn3;
    }
    
    /**
	 * @return Returns the fKSearchType.
	 */
	public zXType.webFKBehaviour getFKSearchType() {
		return FKSearchType;
	}

	/**
	 * @param searchType The KKSearchType to set.
	 */
	public void setFKSearchType(zXType.webFKBehaviour searchType) {
		FKSearchType = searchType;
	}

	/**
	 * @param searchType The fkSearchType to set.
	 */
	public void setFkSearchType(String searchType) {
		if (StringUtil.len(searchType) > 0) {
			FKSearchType = zXType.webFKBehaviour.getEnum(searchType.toLowerCase());
		} else {
			FKSearchType = zXType.webFKBehaviour.wfkbOnClick;
		}
	}
	
	/**
	 * @return Returns the fkMaxSize.
	 */
	public int getFkMaxSize() {
		return fkMaxSize;
	}

	/**
	 * @param fkMaxSize The fkMaxSize to set.
	 */
	public void setFkMaxSize(int fkMaxSize) {
		this.fkMaxSize = fkMaxSize;
	}

	/**
	 * @return Returns the fkSearchDelay.
	 */
	public int getFkSearchDelay() {
		return fkSearchDelay;
	}

	/**
	 * @param fkSearchDelay The fkSearchDelay to set.
	 */
	public void setFkSearchDelay(int fkSearchDelay) {
		this.fkSearchDelay = fkSearchDelay;
	}

	/**
	 * @return Returns the dateFormat.
	 */
	public String getDateFormat() {
		return dateFormat;
	}

	/**
	 * @param dateFormat The dateFormat to set.
	 */
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * @return Returns the timestampFormat.
	 */
	public String getTimestampFormat() {
		return timestampFormat;
	}

	/**
	 * @param timestampFormat The timestampFormat to set.
	 */
	public void setTimestampFormat(String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}
	
	//------------------------ Object implemented methods
	
	/**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        WebSettings webSettings = new WebSettings();
        
        webSettings.setBackgroundImg(getBackgroundImg());
        webSettings.setBgColorDark(getBgColorDark());
        webSettings.setBgColorLight(getBgColorLight());
        webSettings.setEditFormColumn1(getEditFormColumn1());
        webSettings.setEditFormColumn2(getEditFormColumn2());
        webSettings.setEditFormColumn3(getEditFormColumn3());
        webSettings.setEditFormColumn3(getEditFormColumn4());
        webSettings.setListFormColumn1(getListFormColumn1());
        webSettings.setLogoImg(getLogoImg());
        webSettings.setMaxListRows(getMaxListRows());
        webSettings.setMenuColumn1(getMenuColumn1());
        webSettings.setMenuColumn2(getMenuColumn2());
        webSettings.setMenuColumn3(getMenuColumn3());
        webSettings.setMenuColumn4(getMenuColumn4());
        webSettings.setMultilineCols(getMultilineCols());
        webSettings.setMultilineRows(getMultilineRows());
        webSettings.setPageTitle(getPageTitle());
        webSettings.setSearchFormColumn1(getSearchFormColumn1());
        webSettings.setSearchFormColumn2(getSearchFormColumn2());
        webSettings.setSearchFormColumn3(getSearchFormColumn3());
        webSettings.setSearchFormColumn4(getSearchFormColumn4());
        webSettings.setSearchOrderByImage(getSearchOrderByImage());
        webSettings.setTitleBulletImage(getTitleBulletImage());
        webSettings.setTreeColumn1(getTreeColumn1());
        webSettings.setTreeColumn2(getTreeColumn2());
        webSettings.setTreeColumn3(getTreeColumn3());
        webSettings.setFKSearchType(getFKSearchType());
        webSettings.setFkMaxSize(getFkMaxSize());
        webSettings.setFkSearchDelay(getFkSearchDelay());
        webSettings.setDateFormat(getDateFormat());
        webSettings.setTimestampFormat(getTimestampFormat());
        
        return webSettings;
    }
    
}