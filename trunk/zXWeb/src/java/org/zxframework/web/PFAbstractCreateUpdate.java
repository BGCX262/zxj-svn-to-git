package org.zxframework.web;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.util.CloneUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.XMLGen;

/**
 * Abstact class used to make implement createupdate type action. Like
 * PFGridCreateUpdate, PFCreateUpdate and PFMatrixCreateUpdate
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class PFAbstractCreateUpdate extends PFAction {
	
    //------------------------ Members
    
    private PFComponent editformstartaction;
    private String linkededitform;
    private boolean useeditbocontext;
    private List cuactions;
    
    //------------------------ Getters/Setters
    
    /**
     * The edit form start action.
     * 
     * @return Returns the editformstartaction.
     */
    public PFComponent getEditformstartaction() {
        return editformstartaction;
    }
    
    /**
     * @param editformstartaction The editformstartaction to set.
     */
    public void setEditformstartaction(PFComponent editformstartaction) {
        this.editformstartaction = editformstartaction;
    }
    
    /**
     * The linked edit form.
     * 
     * @return Returns the linkededitform.
     */
    public String getLinkededitform() {
        return linkededitform;
    }
    
    /**
     * @param linkededitform The linkededitform to set.
     */
    public void setLinkededitform(String linkededitform) {
        this.linkededitform = linkededitform;
    }
    
    /**
     * A collection (ArrayList)(PFCuAction) of create update actions to perform.
     * 
	 * @return Returns the cuactions.
	 */
	public List getCuactions() {
		return cuactions;
	}

	/**
	 * @param cuactions The cuactions to set.
	 */
	public void setCuactions(List cuactions) {
		this.cuactions = cuactions;
	}
	
	/**
	 * Whether to use the linked edit forms bo context.
	 * 
	 * @return Returns the useeditbocontext.
	 */
	public boolean isUseeditbocontext() {
		return useeditbocontext;
	}

	/**
	 * @param useeditbocontext The useeditbocontext to set.
	 */
	public void setUseeditbocontext(boolean useeditbocontext) {
		this.useeditbocontext = useeditbocontext;
	}
	
	//------------------------ Digester util methods
	
	/**
	 * @deprecated Using BooleanConverter
	 * @param useeditbocontext The useeditbocontext to set.
	 */
	public void setUseeditbocontext(String useeditbocontext) {
		this.useeditbocontext = StringUtil.booleanValue(useeditbocontext);
	}
	
    //------------------------ PFAction overidden methods
    
    /** 
     * @see org.zxframework.web.PFAction#dumpAsXML()
     **/
    public void dumpAsXML() {
        // Call the super to get the first generic parts of the xml.
        super.dumpAsXML();
        // Get a handle to the PFDescriptor xmlgen :
        XMLGen objXMLGen = getDescriptor().getXMLGen();
        
        objXMLGen.taggedValue("linkededitform", getLinkededitform());
        
        /**
         * DGS 24FEB2003: Added edit form start action tag (a component with sub tags)
         */
        getDescriptor().xmlComponent("editformstartaction", getEditformstartaction());
        
        getDescriptor().xmlCuActions(getCuactions());
        objXMLGen.taggedValue("useeditbocontext", isUseeditbocontext());
    }
    
    /** 
     * @see PFAction#clone(Pageflow)
     **/
    public PFAction clone(Pageflow pobjPageflow) {
        PFAbstractCreateUpdate clone = (PFAbstractCreateUpdate)super.clone(pobjPageflow);
        
        if (getEditformstartaction() != null) {
            clone.setEditformstartaction((PFComponent)getEditformstartaction().clone());
        }
        
        clone.setLinkededitform(getLinkededitform());
        
        if (getCuactions() != null && getCuactions().size() > 0) {
        	clone.setCuactions(CloneUtil.clone((ArrayList)getCuactions()));
        }
        
        clone.setUseeditbocontext(isUseeditbocontext());
        
        return clone;
    }
}