/*
 * Created on Nov 16, 2004 by Michael Brewer
 * $Id: PFLayoutCell.java,v 1.1.2.7 2005/11/21 15:23:37 mike Exp $ 
 */
package org.zxframework.web;

import org.zxframework.LabelCollection;
import org.zxframework.ZXObject;

/**
 * PFLayoutCell is a cell in a PFLayout action.
 * 
 * <pre>
 * 
 * Not the name is the key of the object.
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1 
 */
public class PFLayoutCell extends ZXObject {
    
    //------------------------ Members
    
    private String align;
    private String bgcolor;
    private String border;
    private String bordercolor;
    private String clazz;
    private String height;
    private String name;
    private String orientation;
    private String style;
    private String valign;
    private String width;
    private LabelCollection title;
    
    //------------------------ Constructor
    
    /**
     * Default constructor.
     */
    public PFLayoutCell() {
        super();
    }
    
    //------------------------ Getters and Setters.

    /**
     * The alignment of the contents of this cell.
     * 
     * @return Returns the align
     */
    public String getAlign() {
        return align;
    }
    
    /**
     * @param align The align to set.
     */
    public void setAlign(String align) {
        this.align = align;
    }
    
    /**
     * Whether you want to display a border around the cell. 
     * 
     * <pre>
     * 
     * Can of an expression or a string that evaluates to true or false.
     * </pre>
     * 
     * @return Returns the border.
     */
    public String getBorder() {
        return border;
    }
    
    /**
     * @param border The border to set.
     */
    public void setBorder(String border) {
        this.border = border;
    }
    
    /**
     * The background color of the cell.
     * @return Returns the bgcolor.
     */
    public String getBgcolor() {
        return bgcolor;
    }
    
    /**
     * @param bgcolor The bgcolor to set.
     */
    public void setBgcolor(String bgcolor) {
        this.bgcolor = bgcolor;
    }
    
    /**
     * The color of the border if one is present.
     * @return Returns the bordercolor.
     */
    public String getBordercolor() {
        return bordercolor;
    }
    
    /**
     * @param bordercolor The bordercolor to set.
     */
    public void setBordercolor(String bordercolor) {
        this.bordercolor = bordercolor;
    }
    
    /**
     * A custom style for the cell
     * @return Returns the style.
     */
    public String getStyle() {
        return style;
    }
    
    /**
     * @param style The style to set.
     */
    public void setStyle(String style) {
        this.style = style;
    }
    
    /**
     * The name of a css stylesheet that you may want to specify for this cell.
     * 
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
     * The height of this cell. 
     * 
     * <pre>
     * 
     * Can be an expression but must evaluate to a integar.
     * </pre>
     * 
     * @return Returns the height.
     */
    public String getHeight() {
        return height;
    }
    
    /**
     * @param height The height to set.
     */
    public void setHeight(String height) {
        this.height = height;
    }
    
    /**
     * The name is used to uniquely identify the cell and is 
     * used to link the subaction to the cell.
     * 
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
        setKey(name);
    }
    
    /**
     * @return Returns the orientation.
     */
    public String getOrientation() {
        return orientation;
    }
    
    /**
     * @param orientation The orientation to set.
     */
    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
    
    /**
     * The horizonal alignment of the cell. 
     * 
     * <pre>
     * 
     * Can be an expression but must evaluate to either - left/right or center.
     * </pre>
     * 
     * @return Returns the valign.
     */
    public String getValign() {
        return valign;
    }
    
    /**
     * @param valign The valign to set.
     */
    public void setValign(String valign) {
        this.valign = valign;
    }
    
    /**
     * The width of the cell. 
     * 
     * <pre>
     * 
     * Can be an expression but must evaluate to a integar.
     * </pre>
     * 
     * @return Returns the width.
     */
    public String getWidth() {
        return width;
    }
    
    /**
     * @param width The width to set.
     */
    public void setWidth(String width) {
        this.width = width;
    }
    
    /**
     * Each cell can have its own title. This is used at the moment when 
     * displaying the tabbed layout.
     * 
     * @return Returns the title.
     */
    public LabelCollection getTitle() {
        if (this.title == null) {
            this.title = new LabelCollection();
        }
        return title;
    }
    
    /**
     * @param title The title to set.
     */
    public void setTitle(LabelCollection title) {
        this.title = title;
    }
    
    //------------------------ PFObject overridden methods
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFLayoutCell objLayoutCell = new PFLayoutCell();
        
        objLayoutCell.setAlign(getAlign());
        objLayoutCell.setBgcolor(getBgcolor());
        objLayoutCell.setBorder(getBorder());
        objLayoutCell.setBordercolor(getBordercolor());
        objLayoutCell.setClazz(getClazz());
        objLayoutCell.setHeight(getHeight());
        objLayoutCell.setName(getName());
        objLayoutCell.setOrientation(getOrientation());
        objLayoutCell.setStyle(getStyle());
        objLayoutCell.setValign(getValign());
        objLayoutCell.setWidth(getWidth());
        
        if (this.title != null && !this.title.isEmpty()) {
            objLayoutCell.setTitle((LabelCollection)this.title.clone());
        }
        
        return objLayoutCell;
    }
}