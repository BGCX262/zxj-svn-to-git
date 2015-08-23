/*
 * Created on Mar 15, 2004 by Michael Brewer
 * $Id: MenuURL.java,v 1.1.2.7 2005/09/02 08:06:56 mike Exp $
 */
package org.zxframework.web;

import org.zxframework.util.ToStringBuilder;

/**
 * URL for hierarchical menu.
 * 
 * <pre>
 * 
 * Represents a menu items url :
 * 
 * &lt;url&gt; 
 * 		appendsession="yes" 
 * 		newwindow="no">
 * 			zXMMGoToPage('../jsp/zXGPF.jsp?-pf=zXDM&amp;-e=erp/playlist')
 * &lt;/url>
 * 
 * Change    : BD19JUN04
 * Why       : Added isDirector option
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class MenuURL {

    //------------------------ Members
    
    private boolean appendsession;
    private boolean newwindow;
    private String url;
    private boolean isDirector = false;

    //------------------------ Constructor

    /**
     * Default constructor.
     */
    public MenuURL() { 
        super(); 
    }
    
    //------------------------ Getters and Setters
    
    /**
     * Whether to append the session id to the url.
     * 
     * @return Returns the appendsession.
     */
    public boolean isAppendsession() {
        return appendsession;
    }
    
    /**
     * @param appendsession The appendsession to set.
     */
    public void setAppendsession(boolean appendsession) {
        this.appendsession = appendsession;
    }
    
    /**
     * Whether the url opens in a new window.
     * 
     * @return Returns the newwindow.
     */
    public boolean isNewwindow() {
        return newwindow;
    }
    
    /**
     * @param newwindow The newwindow to set.
     */
    public void setNewwindow(boolean newwindow) {
        this.newwindow = newwindow;
    }
    
    /**
     * The url for the link.
     * 
     * @return Returns the url.
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * @param url The url to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Whether the link is a director. Default is false.
     * 
     * @return Returns the isDirector.
     */
    public boolean isDirector() {
        return isDirector;
    }
    
    /**
     * @param isDirector The isDirector to set.
     */
    public void setDirector(boolean isDirector) {
        this.isDirector = isDirector;
    }
    
    //------------------------ Overidden methods
    
    /**
     * @see java.lang.Object#toString()
     */    
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
        toString.append("url", getUrl());
        toString.append("appendsession", isAppendsession());
        toString.append("isDirector", isDirector());
        toString.append("newwindow", isNewwindow());
        
        return toString.toString();
    }
}