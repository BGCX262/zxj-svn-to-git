/*
 * Created on Nov 29, 2004 by Michael Brewer
 * $Id: PFCalendarCategory.java,v 1.1.2.10 2005/09/02 14:04:29 mike Exp $
 */
package org.zxframework.web;

import java.util.ArrayList;

import org.zxframework.CloneableObject;
import org.zxframework.LabelCollection;
import org.zxframework.util.StringUtil;
import org.zxframework.util.ToStringBuilder;

/**
 * A category of a calendar pageflow action.
 * 
 * <pre>
 * 
 * Who    : David Swann
 * When   : 17 September 2004
 * </pre>
 */
public class PFCalendarCategory implements CloneableObject {

    //------------------------ Members
    
    private String name;
    
    private String expression;
    private String clazz;

    private boolean addParityToClass;
    private boolean showWhenZero;

    private PFUrl url;
    private LabelCollection label;
    
    //------------------------ Runtime Members
    
    // Resolved runtime values.
    private int[] rowCount = new int[31];
    private ArrayList[] rows = new ArrayList[31];
    
    //------------------------ Constructor
    
    /**
     * Default constructor.
     */
    public PFCalendarCategory() {
        super();
    }
    
    //------------------------ Getters and Setters
    
    /**
     * @return Returns the addParityToClass.
     */
    public boolean isAddParityToClass() {
        return addParityToClass;
    }

    /**
     * @param addParityToClass The addParityToClass to set.
     */
    public void setAddParityToClass(boolean addParityToClass) {
        this.addParityToClass = addParityToClass;
    }
    
    /**
     * @return Returns the clazz.
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * @param clazz The clazz to set.
     */
    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    /**
     * @return Returns the expression.
     */
    public String getExpression() {
        return expression;
    }

    /**
     * @param expression The expression to set.
     */
    public void setExpression(String expression) {
        this.expression = expression;
    }

    /**
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

    /**
     * @return Returns the name.
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
     * @return Returns the showWhenZero.
     */
    public boolean isShowWhenZero() {
        return showWhenZero;
    }

    /**
     * @param showWhenZero The showWhenZero to set.
     */
    public void setShowWhenZero(boolean showWhenZero) {
        this.showWhenZero = showWhenZero;
    }

    /**
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
    
    //------------------------ Digester helper methods
    
    /**
     * @param addparitytoclass Whether to alternate the colours.
     */
    public void setAddparitytoclass(String addparitytoclass) {
        this.addParityToClass = StringUtil.booleanValue(addparitytoclass);
    }
    
    /**
     * @param showwhenzero The showWhenZero to set.
     */
    public void setShowwhenzero(String showwhenzero) {
        this.showWhenZero = StringUtil.booleanValue(showwhenzero);
    }
    
    //------------------------ Runtime values
    
    /**
     * property : rowCount [run time only - not parsed from XML].
     * 
     * @return Returns the rowCount.
     */
    public int[] getRowCount() {
        return rowCount;
    }
    
    /**
     * property : rowCount [run time only - not parsed from XML].
     * 
     * @param rowCount The rowCount to set.
     */
    public void setRowCount(int[] rowCount) {
        this.rowCount = rowCount;
    }
    
    /**
     * property : rowCount [run time only - not parsed from XML].
     * 
     * @param pintDay The day to update the count.
     * @param pint The count for that day.
     */
    public void setRowCount(int pintDay, int pint) {
        this.rowCount[pintDay] = pint;
    }
    
    /**
     * property : rows  [run time only - not parsed from XML].
     * 
     * @return Returns the rows.
     */
    public ArrayList[] getRows() {
        return rows;
    }
    
    /**
     * property : rows  [run time only - not parsed from XML].
     * 
     * @param rows The rows to set.
     */
    public void setRows(ArrayList[] rows) {
        this.rows = rows;
    }

    /**
     * property : rows  [run time only - not parsed from XML].
     * 
     * @param pintDay The day to set.
     * @param pcol The rows to set.
     */
    public void setRows(int pintDay, ArrayList pcol) {
        this.rows[pintDay] = pcol;
    }
    
    //------------------------ Object overloaded methods
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFCalendarCategory objCalendarCategory = new PFCalendarCategory();
        
        objCalendarCategory.setAddParityToClass(isAddParityToClass());
        objCalendarCategory.setClazz(getClazz());
        objCalendarCategory.setExpression(getExpression());
        
        if (getLabel() != null && getLabel().size() > 0) {
            objCalendarCategory.setLabel((LabelCollection)getLabel().clone());
        }
        
        objCalendarCategory.setName(getName());
        objCalendarCategory.setShowWhenZero(isShowWhenZero());
        
        if (getUrl() != null) {
            objCalendarCategory.setUrl((PFUrl)getUrl().clone());    
        }
        
        return objCalendarCategory;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
        toString.append("name", getName());
        toString.append("addParityToClass", isAddParityToClass());
        toString.append("showWhenZero", isShowWhenZero());
        toString.append("clazz", getClazz());
        toString.append("expression", getExpression());
        
        if (getUrl() != null) {
            toString.append("url", getUrl().getUrl());
        }
        
        if (getLabel() != null) {
        	toString.append("label", getLabel().getLabel());
        }
        
        return toString.toString();
    }
}