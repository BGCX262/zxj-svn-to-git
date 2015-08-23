/*
 * Created on Apr 11, 2004 by Michael Brewer
 * $Id: PFInclude.java,v 1.1.2.9 2005/11/21 15:18:08 mike Exp $ 
 */
package org.zxframework.web;

import org.zxframework.CloneableObject;
import org.zxframework.util.ToStringBuilder;

/**
 * Pageflow Include file object.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFInclude implements CloneableObject {

    //------------------------ Members
    
    private String path;
    private String includeType;
    
    //------------------------ Constructors
    
    /**
     * The default constructor. 
     */
    public PFInclude() {
        super();
    }
    
    //------------------------ Getters/Setters

    /**
     * @return Returns the includeType.
     */
    public String getIncludeType() {
        return includeType;
    }
    
    /**
     * @param includeType The includeType to set.
     */
    public void setIncludeType(String includeType) {
        this.includeType = includeType;
    }
    
    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * @param path The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }    
    

    //------------------------ Object overidden methods.
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFInclude objPFInclude = new PFInclude();
        
        objPFInclude.setIncludeType(getIncludeType());
        objPFInclude.setPath(getPath());
        
        return objPFInclude;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	ToStringBuilder toString = new ToStringBuilder(this);
    	
    	toString.append("path", getPath());
    	toString.append("includetype", getIncludeType());
    	
    	return toString.toString();
    }
}