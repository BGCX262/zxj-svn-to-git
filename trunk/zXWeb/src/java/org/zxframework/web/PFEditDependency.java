/*
 * Created on Apr 15, 2004
 * $Id: PFEditDependency.java,v 1.1.2.6 2006/07/17 16:29:23 mike Exp $
 */
package org.zxframework.web;

import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.util.ToStringBuilder;

/**
 * Pageflow EditForm Dependency object.
 * 
 * <pre>
 * 
 * Change    : BD24MAY03
 * Why       : Added resolvedEntity and attr for performance
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PFEditDependency extends ZXObject {

    //------------------------ Members
    
    private String depattr;
    private String depentity;
    private zXType.pageflowDependencyType depType;
    private String depvalue;
    private String fkfromattr;
    private String fktoattr;
    private zXType.pageflowDependencyOperator opeRator;
    private String relentity;
    private String relattr;
    private String relvalue;
    private String value;
    
    //------------------------ Runtime methods
    
    private String resolvedEntity;
    private String resolvedAttr;
    
    //------------------------ Constructor
    
    /**
     * Default constructor.
     */
    public PFEditDependency() {
        super();
        
        // Init the resolved values
        setResolvedAttr("");
        setResolvedEntity("");
    }

    //------------------------ Getters/Setters
    
    /**
     * @return Returns the depattr.
     */
    public String getDepattr() {
        return depattr;
    }
    
    /**
     * @param depattr The depattr to set.
     */
    public void setDepattr(String depattr) {
        this.depattr = depattr;
    }
    
    /**
     * @return Returns the depentity.
     */
    public String getDepentity() {
        return depentity;
    }
    
    /**
     * @param depentity The depentity to set.
     */
    public void setDepentity(String depentity) {
        this.depentity = depentity;
    }
    
    /**
     * @return Returns the enmdeptype.
     */
    public zXType.pageflowDependencyType getDepType() {
        return depType;
    }
    
    /**
     * @param enmdeptype The enmdeptype to set.
     */
    public void setDepType(zXType.pageflowDependencyType enmdeptype) {
        this.depType = enmdeptype;
    }
    
    /**
     * @return Returns the depvalue.
     */
    public String getDepvalue() {
        return depvalue;
    }
    
    /**
     * @param depvalue The depvalue to set.
     */
    public void setDepvalue(String depvalue) {
        this.depvalue = depvalue;
    }
    
    /**
     * @return Returns the fkfromattr.
     */
    public String getFkfromattr() {
        return fkfromattr;
    }
    
    /**
     * Sets the fkFromAttr.
     * 
     * @param fkfromattr The attribute.
     */
    public void setFKFromAttr(String fkfromattr) {
        this.fkfromattr = fkfromattr;
    }
    
    /**
     * @param fkfromattr The fkfromattr to set.
     */
    public void setFkfromattr(String fkfromattr) {
        this.fkfromattr = fkfromattr;
    }
    
    /**
     * @return Returns the fktoattr.
     */
    public String getFktoattr() {
        return fktoattr;
    }
    
    /**
     * Sets the FKToAttr.
     * 
     * @param fktoattr The fktoattr to set.
     */
    public void setFKToAttr(String fktoattr) {
        this.fktoattr = fktoattr;
    }
    
    /**
     * @param fktoattr The fktoattr to set.
     */
    public void setFktoattr(String fktoattr) {
        this.fktoattr = fktoattr;
    }
    
    /**
     * @return Returns the enmoperator.
     */
    public zXType.pageflowDependencyOperator getOpeRator() {
        return opeRator;
    }
    
    /**
     * @param enmoperator The enmoperator to set.
     */
    public void setOpeRator(zXType.pageflowDependencyOperator enmoperator) {
        this.opeRator = enmoperator;
    }
    
    /**
     * @return Returns the relattr.
     */
    public String getRelattr() {
        return relattr;
    }
    
    /**
     * @param relattr The relattr to set.
     */
    public void setRelattr(String relattr) {
        this.relattr = relattr;
    }
    
    /**
     * @return Returns the relentity.
     */
    public String getRelentity() {
        return relentity;
    }
    
    /**
     * @param relentity The relentity to set.
     */
    public void setRelentity(String relentity) {
        this.relentity = relentity;
    }
    
    /**
     * @return Returns the relvalue.
     */
    public String getRelvalue() {
        return relvalue;
    }
    
    /**
     * @param relvalue The relvalue to set.
     */
    public void setRelvalue(String relvalue) {
        this.relvalue = relvalue;
    }
    
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    
    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * The name of the resolved attribute.
     * 
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
     * The name of the resolved entity.
     * 
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
    
    //------------------------ Digester helper methods
    
    /**
     * Sets the enm deptype as well. 
     * 
     * @param deptype The deptype to set.
     */
    public void setDeptype(String deptype) {
        this.depType = zXType.pageflowDependencyType.getEnum(deptype);
    }
    
    /**
     * Set the enum operator.
     * 
     * @param operator The operator to set.
     */
    public void setOperator(int operator) {
        this.opeRator = zXType.pageflowDependencyOperator.getEnum(operator);
    }
    
//    /**
//     * Set the enum operator.
//     * 
//     * @param operator The operator to set.
//     */
//    public void setOperator(String operator) {
//        this.opeRator = zXType.pageflowDependencyOperator.getEnum(operator);
//    }
    
    //------------------------ Object overridden methods.
    
    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        PFEditDependency objPFEditDependency = new PFEditDependency();
        
        objPFEditDependency.setDepattr(getDepattr());
        objPFEditDependency.setDepentity(getDepentity());
        objPFEditDependency.setDepType(getDepType());
        objPFEditDependency.setDepvalue(getDepvalue());
        objPFEditDependency.setFkfromattr(getFkfromattr());
        objPFEditDependency.setFktoattr(getFktoattr());
        objPFEditDependency.setOpeRator(getOpeRator());
        objPFEditDependency.setRelattr(getRelattr());
        objPFEditDependency.setRelentity(getRelentity());
        objPFEditDependency.setRelvalue(getRelvalue());
        objPFEditDependency.setValue(getValue());

        return objPFEditDependency;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		ToStringBuilder toString = new ToStringBuilder(this);
		
		toString.append("depattr", getDepattr());
		toString.append("depentity", getDepentity());
		toString.append("deptype", zXType.valueOf(getDepType()));
		toString.append("depvalue", getDepvalue());
		
		toString.append("operator", getOpeRator());
		
		toString.append("fkfromattr", getFkfromattr());
		toString.append("fktoattr", getFktoattr());
		
		toString.append("relattr", getRelattr());
		toString.append("relentity", getRelentity());
		toString.append("relvalue", getRelvalue());
		
		toString.append("value", getValue());
        
        return toString.toString();
    }
}