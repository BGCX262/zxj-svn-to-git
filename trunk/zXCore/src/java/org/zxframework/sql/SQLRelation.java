/*
 * Created on Jan 14, 2004 by michael
 * $Id: SQLRelation.java,v 1.1.2.7 2005/07/12 07:28:55 mike Exp $
 */
package org.zxframework.sql;

import org.zxframework.Attribute;
import org.zxframework.ZXBO;
import org.zxframework.util.ToStringBuilder;

/**
 * Support object only used by zx.clsSQL to generate queries.
 * 
 * <pre>
 * 
 * NOTE : attrName is the key for this object if stored in a collection.
 * 
 * Change    : BD29MAR05 - V1.5:1
 * Why       : No longer any need for handle to zX, see init method; has minor performance gain
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class SQLRelation {

    //------------------------ Members
    
    private String attrName;
    private boolean NTo1;
    private boolean inner;
    private ZXBO left;
    private ZXBO right;

    //------------------------ Constructors    
    
    /**
     * Default constructor.
     */
    public SQLRelation() {
        super();
    }

    //------------------------ Getter and Setters

    /**
     * The left hand side business object.
     * 
     * @return Returns the left.
     */
    public ZXBO getLeft() {
        return left;
    }

    /**
     * @see SQLRelation#getLeft()
     * @param left The left to set.
     */
    public void setLeft(ZXBO left) {
        this.left = left;
    }
    
    /**
     * The right hand side business object.
     * @return Returns the right.
     */
    public ZXBO getRight() {
        return right;
    }

    /**
     * @see SQLRelation#getRight()
     * @param right The right to set.
     */
    public void setRight(ZXBO right) {
        this.right = right;
    }
    
    /**
     * @return Returns the attrName.
     */
    public String getAttrName() {
        return attrName;
    }

    /**
     * @param attrName
     *                 The attrName to set.
     */
    public void setAttrName(String attrName) {
        this.attrName = attrName;
        // this.key = attrName;
    }

    /**
     * Whether the relationship type is a N to 1.
     * @return Returns the nTo1.
     */
    public boolean isNTo1() {
        return NTo1;
    }

    /**
     * @see SQLRelation#isNTo1()
     * @param to1 The nTo1 to set.
     */
    public void setNTo1(boolean to1) {
        NTo1 = to1;
    }
    
    /**
     * Whether to perform a inner join.
     * 
     * @return Returns the inner.
     */
    public boolean isInner() {
        return inner;
    }
    
    /**
     * @see SQLRelation#isInner()
     * @param inner The inner to set.
     */
    public void setInner(boolean inner) {
        this.inner = inner;
    }
    
    //------------------------ Public methods.

    /**
     * This is a helper method to get the Right ZXBO attribute, by the right's ZXBO's primary key.
     * 
     * <pre>
     * 
     * NOTE : This is calling : getRight().getDescriptor().getAttributes().get(getRight().getDescriptor().getPrimaryKey())
     * </pre>
     * 
     * @return Returns the Right ZXBO attribute for the right's ZXBO's primary key
     */
    public Attribute getRightAttributeByAttrName() {
        Attribute getRightAttributeByAttrName = null;

        getRightAttributeByAttrName =  getRight().getDescriptor().getAttribute(getRight().getDescriptor().getPrimaryKey());
	    
	    return getRightAttributeByAttrName;
    }
    
    /**
     * This is a helper method to get the left ZXBO attribute, by the attrName.
     * 
     * <pre>
     * 
     * NOTE :This is calling : getLeft().getDescriptor().getAttributes().get(getAttrName())
     * </pre>
     * 
     * @return Returns the left ZXBO attribute for the set attrName
     */
    public Attribute getLeftAttributeByAttrName() {
        Attribute getLeftAttributeByAttrName = null;
	    getLeftAttributeByAttrName =  getLeft().getDescriptor().getAttribute(getAttrName());
	    return getLeftAttributeByAttrName;
    }
    
    //------------------------ Overidden methods
    
    /**
     * @see java.lang.Object#toString()
     */    
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);

        toString.append("attrName", this.attrName);
        toString.append("NTo1", this.NTo1);
        toString.append("inner", this.inner);
        if (this.left != null) {
            toString.append("left", this.left.getDescriptor().getName());
        }
        if (this.right != null) {
            toString.append("right", this.right.getDescriptor().getName());
        }
        
        return toString.toString();
    }    
}