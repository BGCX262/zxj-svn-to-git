/*
 * Created on Apr 9, 2004
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.CloneableObject;
import org.zxframework.LabelCollection;
import org.zxframework.util.CloneUtil;

/**
 * Pageflow EditForm Page object.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFEditPage implements CloneableObject {
    
    //------------------------ Members
    
    private List entitygroups;
    private LabelCollection label;
    
    //------------------------ Constructor
    
    /**
     * Default constructor.
     */
    public PFEditPage() {
        super();
        
        // Init entityGroups, as there will be at least one.
        setEntitygroups(new ArrayList());
        setLabel(new LabelCollection());
    }
    
    //------------------------ Getters and Setters
    
    /**
     * A collection (ArrayList)(Tuple) of entity's and there attribute groups. 
     * Sequence in this collection is more important than being key based.
     * 
     * @return Returns a collection (ArrayList)(Tuple) of entitygroups.
     */
    public List getEntitygroups() {
        return entitygroups;
    }
    
    /**
     * @param entitygroups The entitygroups to set.
     */
    public void setEntitygroups(List entitygroups) {
        this.entitygroups = entitygroups;
    }
    
    /**
     * The label of the editpage.
     * 
     * @return Returns the label.
     */
    public LabelCollection getLabel() {
        return label;
    }
    
    /**
     * @param label The label to set.
     */
    public void setLabel(LabelCollection label) {
        this.label = label;
    }
    
    //------------------------ ZXObject overidden methods.
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFEditPage objEditPage = new PFEditPage();
        
        if (getEntitygroups() != null && getEntitygroups().size() > 0) {
            objEditPage.setEntitygroups(CloneUtil.clone((ArrayList)getEntitygroups()));
        }
        
        if (getLabel() != null) {
            objEditPage.setLabel((LabelCollection)getLabel().clone());
        }
            
        return objEditPage;
    }
}