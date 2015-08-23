/*
 * Created on Apr 9, 2004 by Michael Brewer
 * $Id: PFQryExpr.java,v 1.1.2.9 2006/07/17 16:08:45 mike Exp $ 
 */
package org.zxframework.web;

import org.zxframework.ZXObject;
import org.zxframework.zXType;

/**
 * Pageflow Query Expression object.
 * 
 * <pre>
 * 
 *  Change    : DGS25FEB2004
 *  Why       : Support for null and not null operators
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFQryExpr extends ZXObject {

    //------------------------ Members
    
    private String lhs;
    private String rhs;
    private String attrlhs;
    private String attrrhs;
    private zXType.pageflowQueryExprOperator enmoperator;
    private PFQryExpr queryexprlhs;
    private PFQryExpr queryexprrhs;
    
    //------------------------ Constructors
    
    /** 
     * Default constructor.
     */
    public PFQryExpr() {
        super();
    }
    
    //------------------------ Getters and Setters
    
    /**
     * @return Returns the attrlhs. 
     */
    public String getAttrlhs() {
        return attrlhs;
    }

    /**
     * @param attrlhs The attrlhs to set.
     */
    public void setAttrlhs(String attrlhs) {
        this.attrlhs = attrlhs;
    }

    /** 
     * @return Returns the attrrhs. 
     */
    public String getAttrrhs() {
        return attrrhs;
    }

    /**
     * @param attrrhs The attrrhs to set.
     */
    public void setAttrrhs(String attrrhs) {
        this.attrrhs = attrrhs;
    }

    /** 
     * @return Returns the enmoperator. 
     */
    public zXType.pageflowQueryExprOperator getEnmoperator() {
        return enmoperator;
    }

    /**
     * @param enmoperator The enmoperator to set.
     */
    public void setEnmoperator(zXType.pageflowQueryExprOperator enmoperator) {
        this.enmoperator = enmoperator;
    }

    /** 
     * @return Returns the lhs. 
     */
    public String getLhs() {
        return lhs;
    }

    /**
     * @param lhs The lhs to set.
     */
    public void setLhs(String lhs) {
        this.lhs = lhs;
    }
    
    /** 
     * @return Returns the queryexprlhs. 
     */
    public PFQryExpr getQueryexprlhs() {
        return queryexprlhs;
    }

    /**
     * @param queryexprlhs The queryexprlhs to set.
     */
    public void setQueryexprlhs(PFQryExpr queryexprlhs) {
        this.queryexprlhs = queryexprlhs;
    }

    /** 
     * @return Returns the queryexprrhs. 
     */
    public PFQryExpr getQueryexprrhs() {
        return queryexprrhs;
    }

    /**
     * @param queryexprrhs The queryexprrhs to set.
     */
    public void setQueryexprrhs(PFQryExpr queryexprrhs) {
        this.queryexprrhs = queryexprrhs;
    }

    /** 
     * @return Returns the rhs. 
     */
    public String getRhs() {
        return rhs;
    }

    /** 
     * @param rhs The rhs to set. 
     */
    public void setRhs(String rhs) {
        this.rhs = rhs;
    }
    
    //------------------------ Digester helper methods.
    
    /**
     * @param operator The operator to set.
     */
    public void setOperator(String operator) {
        this.enmoperator = zXType.pageflowQueryExprOperator.getEnum(operator);
    }
    
    //------------------------ Helper methods.
    
    /** 
     * @return Returns whether the query is recursive. ie : a AND or OR operator.
     **/
    public boolean operatorIsRecursive() {
        return (
        		getEnmoperator().equals(zXType.pageflowQueryExprOperator.pqeoAND) 
        		|| getEnmoperator().equals(zXType.pageflowQueryExprOperator.pqeoOR)
        	    );
    }
    
    //------------------------ Object overidden methods.
    
    /** 
     * @see java.lang.Object#clone()
     **/
    public Object clone() {
        PFQryExpr objPFQryExpr = new PFQryExpr();
        
        objPFQryExpr.setAttrlhs(getAttrlhs());
        objPFQryExpr.setAttrrhs(getAttrrhs());
        
        objPFQryExpr.setEnmoperator(getEnmoperator());
        
        objPFQryExpr.setLhs(getLhs());
        objPFQryExpr.setRhs(getRhs());
        
        if (getQueryexprlhs() != null) {
            objPFQryExpr.setQueryexprlhs((PFQryExpr)getQueryexprlhs().clone());
        }
        if (getQueryexprrhs() != null) {
            objPFQryExpr.setQueryexprrhs((PFQryExpr)getQueryexprrhs().clone());
        }
        
        return objPFQryExpr;
    }
}