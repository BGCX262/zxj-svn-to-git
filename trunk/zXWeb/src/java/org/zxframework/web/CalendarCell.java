/*
 * Created on Nov 30, 2004 by Michael Brewer
 * $Id: CalendarCell.java,v 1.1.2.11 2005/11/30 11:54:36 mike Exp $
 */
package org.zxframework.web;

import java.util.ArrayList;
import java.util.Date;

import org.zxframework.util.ToStringBuilder;

/**
 * A single cell within a calendar pageflow action.
 * 
 * <pre>
 * 
 * NOTE: This is a runtime object.
 * 
 * Who    : David Swann
 * When   : 17 September 2004
 * </pre>
 */
public class CalendarCell {
    
    //------------------------ Members
    
    private Date dateStart;
    private Date dateEnd;
    private String cellLabel;
    private boolean moreRows;
    private boolean current;
    private ArrayList rows;
    private int rowCount;
    
    //------------------------ Construtors
    
    /**
     * Default constructor.
     */
    public CalendarCell() {
        super();
        
    	/**
    	 * Safe gaurd against null pointer exceptions.
    	 */
    	this.rows = new ArrayList();
    }
    
    //------------------------ Getters and setters
    
    /**
     * @return Returns the cellLabel.
     */
    public String getCellLabel() {
        return cellLabel;
    }
    
    /**
     * @param cellLabel The cellLabel to set.
     */
    public void setCellLabel(String cellLabel) {
        this.cellLabel = cellLabel;
    }
    
    /**
     * @return Returns the current.
     */
    public boolean isCurrent() {
        return current;
    }
    
    /**
     * @param current The current to set.
     */
    public void setCurrent(boolean current) {
        this.current = current;
    }
    
    /**
     * @return Returns the dateEnd.
     */
    public Date getDateEnd() {
        return dateEnd;
    }
    
    /**
     * @param dateEnd The dateEnd to set.
     */
    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }
    
    /**
     * @return Returns the dateStart.
     */
    public Date getDateStart() {
        return dateStart;
    }
    
    /**
     * @param dateStart The dateStart to set.
     */
    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }
    
    /**
     * @return Returns the moreRows.
     */
    public boolean isMoreRows() {
        return moreRows;
    }
    
    /**
     * @param moreRows The moreRows to set.
     */
    public void setMoreRows(boolean moreRows) {
        this.moreRows = moreRows;
    }
    
    /**
     * @return Returns the rowCount.
     */
    public int getRowCount() {
        return rowCount;
    }
    
    /**
     * @param rowCount The rowCount to set.
     */
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }
    
    /**
     * @return Returns the rows.
     */
    public ArrayList getRows() {
        return rows;
    }
    
    /**
     * @param rows The rows to set.
     */
    public void setRows(ArrayList rows) {
        this.rows = rows;
    }
    
    //------------------------ Overidden methods
    
    /**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		toString.append("cellLabel", getCellLabel());
		return toString.toString();
	}
}