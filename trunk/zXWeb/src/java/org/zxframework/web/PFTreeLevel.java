/*
 * Created on Apr 9, 2004 by Michael Brewer
 * $Id: PFTreeLevel.java,v 1.1.2.10 2005/09/02 13:56:44 mike Exp $ 
 */
package org.zxframework.web;

import org.zxframework.CloneableObject;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;

/**
 * A java bean which holds all information for a tree level for PFTreeForm.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFTreeLevel implements CloneableObject {
    
    //------------------------ Members
	
    private PFUrl url;
    private String clazz;
    private boolean addparitytoclass;
    
    //------------------------ Constructors

    /** 
     * Default constructor 
     */
    public PFTreeLevel() { 
        super(); 
    }
    
    //------------------------ Getters and Setters

    /**
     * Whether to have alternating colours.
     * 
     * @return Returns the addparitytoclass.
     */
    public boolean isAddparitytoclass() {
        return addparitytoclass;
    }
    
    /**
     * @param addparitytoclass The addparitytoclass to set.
     */
    public void setAddparitytoclass(boolean addparitytoclass) {
        this.addparitytoclass = addparitytoclass;
    }
    
    /**
     * The css class used to override the look and feel on the site.
     * 
     * @return Returns the class.
     */
    public String getClazz() {
        return clazz;
    }
    
    /**
     * @param clazz1 The class to set.
     */
    public void setClass(String clazz1) {
        clazz = clazz1;
    }
    
    /**
     * The link on the tree node.
     * 
     * @return Returns the url.
     */
    public PFUrl getUrl() {
        return url;
    }
    
    /**
     * @param url The url to set.
     */
    public void setUrl(PFUrl url) {
        this.url = url;
    }
    
    //------------------------ Digester helper methods.
    
    /**
     * @deprecated Using BooleanConverter
     * @param addparitytoclass The addparitytoclass to set.
     */
    public void setAddparitytoclass(String addparitytoclass) {
        this.addparitytoclass = StringUtil.booleanValue(addparitytoclass);
    }
    
    //------------------------ Object Overidden methods.
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFTreeLevel objPFTreeLevel = new PFTreeLevel();
        
        objPFTreeLevel.setAddparitytoclass(isAddparitytoclass());
        objPFTreeLevel.setClass(getClazz());
        
        if (getUrl() != null) {
            objPFTreeLevel.setUrl((PFUrl)getUrl().clone());
        }
        
        return objPFTreeLevel;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
		if (getUrl() != null) {
			toString.append("url", getUrl().getUrl());
		}
		
        toString.append("clazz", getClazz());
        toString.append("addParity", isAddparitytoclass());
        
        return super.toString();
    }
}