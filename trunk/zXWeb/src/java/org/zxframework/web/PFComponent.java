/*
 * Created on Apr 9, 2004 by Michael Brewer
 * $Id: PFComponent.java,v 1.1.2.10 2005/11/21 15:50:55 mike Exp $
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.CloneableObject;
import org.zxframework.util.CloneUtil;

/**
 * Pageflow component object.
 * 
 * <pre>
 * 
 * This is normally used as a linkaction of a pageflow action.
 * So the name will either have to match the name of a action inside the same pageflow.
 * You could also refer to external pageflow action with the pageflow:action syntax.
 * </pre>  
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1 
 */
public class PFComponent implements CloneableObject {
    
    //------------------------ Memebers
    
    private String action;
    private List querystring;
    
    //------------------------ Constructor
    
    /**
     * Default constructor.
     */
    public PFComponent() {
        super();
    }
    
    //------------------------ Getters and Setters.
    
    /**
     * The name of the action to perform. 
     * 
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }

    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }
    
    /**
     * A collection (ArrayList)(PFDirector) of querystring parameters that will be send to the action specified.
     *  
     * @return Returns the queryString.
     */
    public List getQuerystring() {
        return querystring;
    }

    /**
     * @param querystring The queryString to set.
     */
    public void setQuerystring(List querystring) {
        this.querystring = querystring;
    }

    //------------------------ PFAction overidden methods
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFComponent objPFComponent = new PFComponent();
            
        objPFComponent.setAction(getAction());
            
        if (getQuerystring() != null && getQuerystring().size() > 0) {
            objPFComponent.setQuerystring(CloneUtil.clone((ArrayList)getQuerystring()));
        }

        return objPFComponent;
    }
}