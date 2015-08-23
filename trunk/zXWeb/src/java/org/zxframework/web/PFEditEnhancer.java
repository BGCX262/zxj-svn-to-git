/*
 * Created on Apr 9, 2004 by Michael Brewer
 * $Id: PFEditEnhancer.java,v 1.1.2.13 2006/07/17 16:29:23 mike Exp $  
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.CloneableObject;
import org.zxframework.util.CloneUtil;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;

/**
 * Pageflow EditForm Enhancer object.
 * 
 * <pre>
 * 
 * Change    : DGS09APR2003
 * Why       : Added the fklookup boolean
 * 
 * Change    : BD24MAY03
 * Why       : - Added resolvedEntity and attr for performance
 *             - Added refs
 * 
 * Change    : DGS29AUG2003
 * Why       : Added the fkadd boolean
 * 
 * Change    : DGS20OCT2003
 * Why       : Added property 'FKWhere' to restrict the recordset of FK items.
 * 
 * Change    : DGS13FEB2004
 * Why       : New enhancer ability to partially override entity size in dropdowns.
 * 
 * Change    : DGS28JUN2004
 * Why       : New enhancers to add a post-edit field label and optionally its class
 * 
 * Change    : BD19JUL04
 * Why       : New enhancers to allow merging two or more editform fields on single line
 *  
 * Change    : BD1MAR05 - V1.4:54
 * Why       : New enhancers to control width and height of multi-line fields
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFEditEnhancer implements CloneableObject {
    
    //------------------------ Members
    
    private String entity;
    private String attr;
    private String labelclass;
    private String inputClass;
    private String stdbuttonclass;
    private String fkwhere;
    private String tabindex;
    private String disabled;
    private String onfocus;
    private String onblur;
    private String onchange;
    private String onkeydown;
    private String onkeypress;
    private String onkeyup;
    private String onclick;
    private String onmouseover;
    private String onmouseout;
    private String onmousedown;
    private String onmouseup;
    private String entitysize;
    
    private List refs;
    private List editdependencies;
    
    private String postlabel;
    private String postlabelclass;
    
    /** BD1MAR05 - V1.4:54 */
    private String multilinerows;
    private String multilinecols;
    
    private boolean spellCheck;
    private boolean fkLookup;
    /** DGS20AUG2003: Added the fkadd boolean to enhancers. **/
    private boolean fkAdd;
    private boolean mergeWithPrevious;
    private boolean mergeWithNext;
    private boolean noLabel;
    
    //------------------------ Runtime variables
    
    private String resolvedEntity;
    private String resolvedAttr;

    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public PFEditEnhancer() {
        super();
        
        this.editdependencies = new ArrayList();
    }
    
    //------------------------ Getters and Setters
    
    /**
     * The  name of the attribute.
     * 
     * @return Returns the attr.
     */
    public String getAttr() {
        return attr;
    }
    
    /**
     * @param attr The attr to set.
     */
    public void setAttr(String attr) {
        this.attr = attr;
    }
    
    /**
     * Whether the control should be disabled or not.
     * 
     * @return Returns the disabled.
     */
    public String getDisabled() {
        return disabled;
    }
    
    /**
     * @param disabled The disabled to set.
     */
    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }
    
    /**
     * A collection (ArrayList)(PFEditDependency) of editdependencies.
     * 
     * @return Returns the editdependencies.
     */
    public List getEditdependencies() {
        return editdependencies;
    }
    
    /**
     * @param editdependencies The editdependencies to set.
     */
    public void setEditdependencies(List editdependencies) {
        this.editdependencies = editdependencies;
    }
    
    /**
     * The name of the PFEntity.
     * 
     * @return Returns the entity.
     */
    public String getEntity() {
        return entity;
    }
    
    /**
     * @param entity The entity to set.
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }
    
    /**
     * @return Returns the entitysize.
     */
    public String getEntitysize() {
    
        return entitysize;
    }
    
    /**
     * @param entitysize The entitysize to set.
     */
    public void setEntitysize(String entitysize) {
        this.entitysize = entitysize;
    }
    
    /**
     * @return Returns the fkadd.
     */
    public boolean isFkAdd() {
        return fkAdd;
    }
    
    /**
     * @param fkadd The fkadd to set.
     */
    public void setFkAdd(boolean fkadd) {
        this.fkAdd = fkadd;
    }
    
    /**
     * @return Returns the multilinecols.
     */
    public String getMultilinecols() {
        return multilinecols;
    }
    
    /**
     * @param multiLineColumns The multilinecols to set.
     */
    public void setMultilinecols(String multiLineColumns) {
        this.multilinecols = multiLineColumns;
    }
    
    /**
     * @return Returns the multilinerows.
     */
    public String getMultilinerows() {
        return multilinerows;
    }
    
    /**
     * @param multiLineRows The multilinerows to set.
     */
    public void setMultilinerows(String multiLineRows) {
        this.multilinerows = multiLineRows;
    }
    
    /**
     * DGS09APR2003: Added the fklookup boolean to enhancers.
     * 
     * @return Returns the fklookup.
     */
    public boolean isFkLookup() {
        return fkLookup;
    }
    
    /**
     * @param fklookup The fklookup to set.
     */
    public void setFkLookup(boolean fklookup) {
        this.fkLookup = fklookup;
    }
    
    /**
     * @return Returns the fkwhere.
     */
    public String getFkwhere() {
        return fkwhere;
    }
    
    /**
     * DGS 20OCT2003: Added FK where clause
     * 
     * @param fkwhere The fkwhere to set.
     */
    public void setFkwhere(String fkwhere) {
        this.fkwhere = fkwhere;
    }
    
    /**
     * @return Returns the inputclass.
     */
    public String getInputClass() {
        return inputClass;
    }
    
    /**
     * @param inputclass The inputclass to set.
     */
    public void setInputClass(String inputclass) {
        this.inputClass = inputclass;
    }
    
    /**
     * @return Returns the labelclass.
     */
    public String getLabelclass() {
        return labelclass;
    }
    
    /**
     * @param labelclass The labelclass to set.
     */
    public void setLabelclass(String labelclass) {
        this.labelclass = labelclass;
    }
    
    /**
     * @return Returns the onblur.
     */
    public String getOnblur() {
        return onblur;
    }
    
    /**
     * @param onblur The onblur to set.
     */
    public void setOnblur(String onblur) {
        this.onblur = onblur;
    }
    
    /**
     * @return Returns the onchange.
     */
    public String getOnchange() {
        return onchange;
    }
    
    /**
     * @param onchange The onchange to set.
     */
    public void setOnchange(String onchange) {
        this.onchange = onchange;
    }
    
    /**
     * @return Returns the onclick.
     */
    public String getOnclick() {
        return onclick;
    }
    
    /**
     * @param onclick The onclick to set.
     */
    public void setOnclick(String onclick) {
        this.onclick = onclick;
    }
    
    /**
     * @return Returns the onfocus.
     */
    public String getOnfocus() {
        return onfocus;
    }
    
    /**
     * @param onfocus The onfocus to set.
     */
    public void setOnfocus(String onfocus) {
        this.onfocus = onfocus;
    }
    
    /**
     * @return Returns the onkeydown.
     */
    public String getOnkeydown() {
        return onkeydown;
    }
    
    /**
     * @param onkeydown The onkeydown to set.
     */
    public void setOnkeydown(String onkeydown) {
        this.onkeydown = onkeydown;
    }
    
    /**
     * @return Returns the onkeypress.
     */
    public String getOnkeypress() {
        return onkeypress;
    }
    
    /**
     * @param onkeypress The onkeypress to set.
     */
    public void setOnkeypress(String onkeypress) {
        this.onkeypress = onkeypress;
    }
    
    /**
     * @return Returns the onkeyup.
     */
    public String getOnkeyup() {
        return onkeyup;
    }
    
    /**
     * @param onkeyup The onkeyup to set.
     */
    public void setOnkeyup(String onkeyup) {
        this.onkeyup = onkeyup;
    }
    
    /**
     * @return Returns the onmousedown.
     */
    public String getOnmousedown() {
        return onmousedown;
    }
    
    /**
     * @param onmousedown The onmousedown to set.
     */
    public void setOnmousedown(String onmousedown) {
        this.onmousedown = onmousedown;
    }
    
    /**
     * @return Returns the onmouseout.
     */
    public String getOnmouseout() {
        return onmouseout;
    }
    
    /**
     * @param onmouseout The onmouseout to set.
     */
    public void setOnmouseout(String onmouseout) {
        this.onmouseout = onmouseout;
    }
    
    /**
     * @return Returns the onmouseover.
     */
    public String getOnmouseover() {
        return onmouseover;
    }
    
    /**
     * @param onmouseover The onmouseover to set.
     */
    public void setOnmouseover(String onmouseover) {
        this.onmouseover = onmouseover;
    }
    
    /**
     * @return Returns the onmouseup.
     */
    public String getOnmouseup() {
        return onmouseup;
    }
    
    /**
     * @param onmouseup The onmouseup to set.
     */
    public void setOnmouseup(String onmouseup) {
        this.onmouseup = onmouseup;
    }
    
    /**
     * A collection (ArrayList)(PFRefs) of Refs.
     * 
     * @return Returns the refs.
     */
    public List getRefs() {
        return refs;
    }
    
    /**
     * @param refs The refs to set.
     */
    public void setRefs(ArrayList refs) {
        this.refs = refs;
    }
    
    /**
     * Whether to use a spellsheck component.
     * 
     * @return Returns the spellcheck.
     */
    public boolean isSpellCheck() {
        return spellCheck;
    }
    
    /**
     * @param spellcheck The spellcheck to set.
     */
    public void setSpellCheck(boolean spellcheck) {
        this.spellCheck = spellcheck;
    }
    
    /**
     * @return Returns the stdbuttonclass.
     */
    public String getStdbuttonclass() {
        return stdbuttonclass;
    }
    
    /**
     * @param stdbuttonclass The stdbuttonclass to set.
     */
    public void setStdbuttonclass(String stdbuttonclass) {
        this.stdbuttonclass = stdbuttonclass;
    }
    
    /**
     * @return Returns the tabindex.
     */
    public String getTabindex() {
        return tabindex;
    }
    
    /**
     * @param tabindex The tabindex to set.
     */
    public void setTabindex(String tabindex) {
        this.tabindex = tabindex;
    }

    /**
     * @return Returns the mergewithnext.
     */
    public boolean isMergeWithNext() {
        return mergeWithNext;
    }
    
    /**
     * @param mergewithnext The mergewithnext to set.
     */
    public void setMergeWithNext(boolean mergewithnext) {
        this.mergeWithNext = mergewithnext;
    }
    
    /**
     * @return Returns the mergewithprevious.
     */
    public boolean isMergeWithPrevious() {
        return mergeWithPrevious;
    }
    
    /**
     * @param mergewithprevious The mergewithprevious to set.
     */
    public void setMergeWithPrevious(boolean mergewithprevious) {
        this.mergeWithPrevious = mergewithprevious;
    }
    
    /**
     * @return Returns the nolabel.
     */
    public boolean isNoLabel() {
        return noLabel;
    }
    
    /**
     * @param nolabel The nolabel to set.
     */
    public void setNoLabel(boolean nolabel) {
        this.noLabel = nolabel;
    }
    
    /**
     * @return Returns the postlabel.
     */
    public String getPostlabel() {
        return postlabel;
    }
    
    /**
     * @param postlabel The postlabel to set.
     */
    public void setPostlabel(String postlabel) {
        this.postlabel = postlabel;
    }
    
    /**
     * @return Returns the postlabelclass.
     */
    public String getPostlabelclass() {
        return postlabelclass;
    }
    
    /**
     * @param postlabelclass The postlabelclass to set.
     */
    public void setPostlabelclass(String postlabelclass) {
        this.postlabelclass = postlabelclass;
    }
    
    //------------------------ Digester helper methods

    /**
     * NOTE : Boolean properties are treated differently here.
     * The reason being is the Apache Digester allows for auto conversion from String -> Boolean in
     * the xml parsing, however in the framework we have extra boolean values that the Digester does not
     * support like Yes/zYes/zTrue etc.. So to allow this i much the setter name up with the xml element name
     * and allow the setter to handle the conversion from String -> Boolean. However this means that the boolean property
     * does not much the xml element name.
     **/
    
    /**
     * @param spellcheck The spellcheck to set.
     */
    public void setSpellcheck(String spellcheck) {
        this.spellCheck = StringUtil.booleanValue(spellcheck);
    }
    
    /**
     * @param fkadd The fkadd to set.
     */
    public void setFkadd(String fkadd) {
        this.fkAdd = StringUtil.booleanValue(fkadd);
    }
    
    /**
     * @param fklookup The fklookup to set.
     */
    public void setFklookup(String fklookup) {
        this.fkLookup = StringUtil.booleanValue(fklookup);
    }
    
    /**
     * @param mergewithnext The mergewithnext to set.
     */
    public void setMergewithnext(String mergewithnext) {
        this.mergeWithNext = StringUtil.booleanValue(mergewithnext);
    }
    
    /**
     * @param mergewithprevious The mergewithprevious to set.
     */
    public void setMergewithprevious(String mergewithprevious) {
        this.mergeWithPrevious = StringUtil.booleanValue(mergewithprevious);
    }
    
    /**
     * @param nolabel The nolabel to set.
     */
    public void setNolabel(String nolabel) {
        this.noLabel = StringUtil.booleanValue(nolabel);
    }
    
    //------------------------ Runtime getters/setters
    
    /**
     * These are runtime only variables and should not be cloned
     */
    
    /**
     * @return Returns the resolvedAttr.
     */
    public String getResolvedAttr() {
        return resolvedAttr;
    }
    
    /**
     * @param resolvedAttr The resolvedAttr to set.
     */
    public void setResolvedAttr(String resolvedAttr) {
        this.resolvedAttr = resolvedAttr;
    }
    
    /**
     * @return Returns the resolvedEntity.
     */
    public String getResolvedEntity() {
        return resolvedEntity;
    }
    
    /**
     * @param resolvedEntity The resolvedEntity to set.
     */
    public void setResolvedEntity(String resolvedEntity) {
        this.resolvedEntity = resolvedEntity;
    }
    
    //------------------------ Object Methods
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFEditEnhancer objEditEnhancer = new PFEditEnhancer();
        
        objEditEnhancer.setAttr(getAttr());
        objEditEnhancer.setDisabled(getDisabled());
        
        if (getEditdependencies() != null && getEditdependencies().size() > 0) {
            objEditEnhancer.setEditdependencies(CloneUtil.clone((ArrayList)getEditdependencies()));
        }
        
        objEditEnhancer.setEntity(getEntity());
        objEditEnhancer.setEntitysize(getEntitysize());
        objEditEnhancer.setFkAdd(isFkAdd());
        objEditEnhancer.setFkLookup(isFkLookup());
        objEditEnhancer.setFkwhere(getFkwhere());
        objEditEnhancer.setInputClass(getInputClass());
        
        objEditEnhancer.setLabelclass(getLabelclass());
        objEditEnhancer.setMergeWithNext(isMergeWithNext());
        objEditEnhancer.setMergeWithPrevious(isMergeWithPrevious());
        objEditEnhancer.setMultilinecols(getMultilinecols());
        objEditEnhancer.setMultilinerows(getMultilinerows());
        objEditEnhancer.setNoLabel(isNoLabel());
        objEditEnhancer.setOnblur(getOnblur());
        objEditEnhancer.setOnchange(getOnchange());
        objEditEnhancer.setOnclick(getOnclick());
        objEditEnhancer.setOnfocus(getOnfocus());
        objEditEnhancer.setOnkeydown(getOnkeydown());
        objEditEnhancer.setOnkeypress(getOnkeypress());
        objEditEnhancer.setOnkeyup(getOnkeyup());
        objEditEnhancer.setOnmousedown(getOnmousedown());
        objEditEnhancer.setOnmouseout(getOnmouseout());
        objEditEnhancer.setOnmouseover(getOnmouseover());
        objEditEnhancer.setOnmouseup(getOnmouseup());
        
        objEditEnhancer.setPostlabel(getPostlabel());
        objEditEnhancer.setPostlabelclass(getPostlabelclass());
        
        if (getRefs() != null && getRefs().size() > 0) {
            objEditEnhancer.setRefs(CloneUtil.clone((ArrayList)getRefs()));
        }
        
        objEditEnhancer.setSpellCheck(isSpellCheck());
        objEditEnhancer.setStdbuttonclass(getStdbuttonclass());
        objEditEnhancer.setTabindex(getTabindex());
            
        return objEditEnhancer;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
		toString.append("attr", getAttr());
        
        return toString.toString();
    }
}