/*
 * Created on Nov 19, 2004 by Michael Brewer
 * $Id: PFLayoutComponent.java,v 1.1.2.8 2005/11/21 15:24:11 mike Exp $ 
 */
package org.zxframework.web;

import org.zxframework.CloneableObject;
import org.zxframework.LabelCollection;

/**
 * A component in a PFLayout Action.
 * 
 * <pre>
 * 
 * NOTE : Is directly associated to a cell by it name in the cell layout type.
 * 
 * Change    : DGS14JUL2005 - V1.5:39
 * Why       : Added new get (only) property to return the action name of the component.
 *  		   Is used by the Repositiry Editor.
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1 
 */
public class PFLayoutComponent implements CloneableObject {

    //------------------------ Memebers
    
    private String name;
    private PFComponent component;
    private String active;
    
    private LabelCollection title;
    
    //------------------------ Constructor
    
    /**
     * Default constructor. 
     */
    public PFLayoutComponent() {
        super();
    }

    //------------------------ Getters and Setters.
    
    /**
     * A name to uniquely indentify a PFCompenent in a collection
     * 
     * @return Returns the name of the PFComponent.
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }    
    
    /**
     * The component to use.
     * 
     * @return Returns the component.
     */
    public PFComponent getComponent() {
        return component;
    }
    
    /**
     * @param action The action to set.
     */
    public void setComponent(PFComponent action) {
        this.component = action;
    }
    
    /**
     * Whether a component is active or not.
     * 
     * <pre>
     * 
     * NOTE : This can be an expression that evaluates to a boolean value.
     * </pre>
     * 
     * @return Returns the active.
     */
    public String getActive() {
        return active;
    }
    
    /**
     * @param active The active to set.
     */
    public void setActive(String active) {
        this.active = active;
    }
    
    /**
     * Each component in a cell can have its own title. 
     * This is used at the moment when displaying the tabbed layout.
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
    
    //------------------------ PFAction overloaded methods
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFLayoutComponent objPFLayoutComponent = new PFLayoutComponent();
            
        objPFLayoutComponent.setName(getName());
        objPFLayoutComponent.setActive(getActive());
        
        if (getComponent() != null) {
        	objPFLayoutComponent.setComponent((PFComponent)getComponent().clone());
        }
        
        if (getTitle() != null && !getTitle().isEmpty()) {
            objPFLayoutComponent.setTitle((LabelCollection)getTitle().clone());
        }
        
        return objPFLayoutComponent;
    }
}