/*
 * Created on Apr 16, 2005
 * $Id: DSWhereCondition.java,v 1.1.2.18 2006/07/17 16:37:16 mike Exp $
 */
package org.zxframework.datasources;

import java.text.DateFormat;

import org.zxframework.Attribute;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.property.BooleanProperty;
import org.zxframework.property.DateProperty;
import org.zxframework.property.DoubleProperty;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.sql.SQL;
import org.zxframework.util.DateUtil;
import org.zxframework.util.StringUtil;

/**
 * The DSWhereClause class.
 * 
 * <pre>
 * 
 * This is used by DSWhereClause and is a single token from a parsed where clause.
 * It contains a left-hand-side (LHS), an operator and a right-hand-side (RHS), when multiple
 * conditions are joined in a single clause, they are joined with an operand (either AND or OR)
 * and priorities can be defined by nesting conditions using parenthesis.
 *
 * If speed was not such an issue (speed of execution that is), we would store a parsed
 * where clause in a proper tree-like structure; storing it in a sequential way however is
 * much faster.
 * 
 * Change    : BD13APR05 - V1.5:4
 * Why       : - Fixed bug in getAsSQLWhereCondition when having a condition like <attr> = #null,
 *             <attr> <> #null, #null = <attr> or #null <> <attr>
 *             - Still reference to zX.DB
 *             - Make sure we always add DB type as parameter to dbGetStrValue
 *             - Fixed bug in resolve, referred to LHS where should be RHS
 *             
 * Change    : BD13APR05 - V1.5:12
 * Why       : - In resolve, we also resolve when we have an attr reference, not just when we have
 *               a BO context attr reference. We do this as this makes it possible to use evaluate
 *               more effectively as we don't have to parse for each object, but can simply re-set
 *               baseBO
 *               
 * Change    : DGS08NOV2005 - V1.5:67
 * Why       : In getAsSQLWhereCondition when converting non-strings for comparison with strings for
 *             SQL Server, was trying to use CSTR (as Access) but that is not valid - now uses CAST
 * 
 * Change    : BD7FEB06 - V1.5:87
 * Why       : Did not handle #value in combination with % or %% operand correctly
 *
 * Change    : BD7FEB06 - V1.5:88
 * Why       : Added support for #qs.xxx construct
 * 
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class DSWhereCondition extends ZXObject {
    
    //--------------------------- Members
    
    private DSHRdbms DSHandler;
    private String LHSBOName;
    private String LHSAttrName;
    private String RHSBOName;
    private String RHSAttrName;
    private DSWhereClause whereClause;
    private String LHSRawValue;
    private String RHSRawValue;
    private String LHSValue;
    private String RHSValue;
    private ZXBO LHSBO;
    private Attribute LHSAttr;
    private ZXBO RHSBO;
    private Attribute RHSAttr;
    private zXType.dsWhereConditionType LHSType;
    private zXType.compareOperand operand;
    private zXType.dsWhereConditionType RHSType;
    private int openNesting;
    private int closeNesting;
    private zXType.dsWhereConditionOperator operator;
    
    //-------------------------------- Constructor
    
    /**
     * Default constructor.
     */
    public DSWhereCondition() {
        super();
    }
    
    //-------------------------------- Getters/Setters
    
    /**
     * @return Returns the closeNesting.
     */
    public int getCloseNesting() {
        return closeNesting;
    }
    
    /**
     * @param closeNesting The closeNesting to set.
     */
    public void setCloseNesting(int closeNesting) {
        this.closeNesting = closeNesting;
    }
    
    /**
     * @return Returns the dSHandler.
     */
    public DSHRdbms getDSHandler() {
        return DSHandler;
    }
    
    /**
     * @param handler The dSHandler to set.
     */
    public void setDSHandler(DSHRdbms handler) {
        DSHandler = handler;
    }
    
    /**
     * @return Returns the lHSAttr.
     */
    public Attribute getLHSAttr() {
        return LHSAttr;
    }
    
    /**
     * @param attr The lHSAttr to set.
     */
    public void setLHSAttr(Attribute attr) {
        LHSAttr = attr;
    }
    
    /**
     * @return Returns the lHSAttrName.
     */
    public String getLHSAttrName() {
        return LHSAttrName;
    }
    
    /**
     * @param attrName The lHSAttrName to set.
     */
    public void setLHSAttrName(String attrName) {
        LHSAttrName = attrName;
    }
    
    /**
     * @return Returns the lHSBO.
     */
    public ZXBO getLHSBO() {
        return LHSBO;
    }
    
    /**
     * @param lhsbo The lHSBO to set.
     */
    public void setLHSBO(ZXBO lhsbo) {
        LHSBO = lhsbo;
    }
    
    /**
     * @return Returns the lHSBOName.
     */
    public String getLHSBOName() {
        return LHSBOName;
    }
    
    /**
     * @param name The lHSBOName to set.
     */
    public void setLHSBOName(String name) {
        LHSBOName = name;
    }
    
    /**
     * @return Returns the lHSRawValue.
     */
    public String getLHSRawValue() {
        return LHSRawValue;
    }
    
    /**
     * @param rawValue The lHSRawValue to set.
     */
    public void setLHSRawValue(String rawValue) {
        LHSRawValue = rawValue;
    }
    
    /**
     * @return Returns the lHSType.
     */
    public zXType.dsWhereConditionType getLHSType() {
        return LHSType;
    }
    
    /**
     * @param type The lHSType to set.
     */
    public void setLHSType(zXType.dsWhereConditionType type) {
        LHSType = type;
    }
    
    /**
     * @return Returns the lHSValue.
     */
    public String getLHSValue() {
        return LHSValue;
    }
    
    /**
     * @param value The lHSValue to set.
     */
    public void setLHSValue(String value) {
        LHSValue = value;
    }
    
    /**
     * @return Returns the openNesting.
     */
    public int getOpenNesting() {
        return openNesting;
    }
    
    /**
     * @param openNesting The openNesting to set.
     */
    public void setOpenNesting(int openNesting) {
        this.openNesting = openNesting;
    }
    
    /**
     * @return Returns the operand.
     */
    public zXType.compareOperand getOperand() {
        return operand;
    }
    
    /**
     * @param operand The operand to set.
     */
    public void setOperand(zXType.compareOperand operand) {
        this.operand = operand;
    }
    
    /**
     * @return Returns the operator.
     */
    public zXType.dsWhereConditionOperator getOperator() {
        return operator;
    }
    
    /**
     * @param operator The operator to set.
     */
    public void setOperator(zXType.dsWhereConditionOperator operator) {
        this.operator = operator;
    }
    
    /**
     * @return Returns the rHSAttr.
     */
    public Attribute getRHSAttr() {
        return RHSAttr;
    }
    
    /**
     * @param attr The rHSAttr to set.
     */
    public void setRHSAttr(Attribute attr) {
        RHSAttr = attr;
    }
    
    /**
     * @return Returns the rHSAttrName.
     */
    public String getRHSAttrName() {
        return RHSAttrName;
    }
    
    /**
     * @param attrName The rHSAttrName to set.
     */
    public void setRHSAttrName(String attrName) {
        RHSAttrName = attrName;
    }
    
    /**
     * @return Returns the rHSBO.
     */
    public ZXBO getRHSBO() {
        return RHSBO;
    }
    
    /**
     * @param rhsbo The rHSBO to set.
     */
    public void setRHSBO(ZXBO rhsbo) {
        RHSBO = rhsbo;
    }
    
    /**
     * @return Returns the rHSBOName.
     */
    public String getRHSBOName() {
        return RHSBOName;
    }
    
    /**
     * @param name The rHSBOName to set.
     */
    public void setRHSBOName(String name) {
        RHSBOName = name;
    }
    
    /**
     * @return Returns the rHSRawValue.
     */
    public String getRHSRawValue() {
        return RHSRawValue;
    }
    
    /**
     * @param rawValue The rHSRawValue to set.
     */
    public void setRHSRawValue(String rawValue) {
        RHSRawValue = rawValue;
    }
    
    /**
     * @return Returns the rHSType.
     */
    public zXType.dsWhereConditionType getRHSType() {
        return RHSType;
    }
    
    /**
     * @param type The rHSType to set.
     */
    public void setRHSType(zXType.dsWhereConditionType type) {
        RHSType = type;
    }
    
    /**
     * @return Returns the rHSValue.
     */
    public String getRHSValue() {
        return RHSValue;
    }
    
    /**
     * @param value The rHSValue to set.
     */
    public void setRHSValue(String value) {
        RHSValue = value;
    }
    
    /**
     * @return Returns the whereClause.
     */
    public DSWhereClause getWhereClause() {
        return whereClause;
    }
    
    /**
     * @param whereClause The whereClause to set.
     */
    public void setWhereClause(DSWhereClause whereClause) {
        this.whereClause = whereClause;
    }
    
    /**
     * @return Quick alternative for either type attr or BO context attr
     */
    public boolean isLHSAttrReference() {
        return this.LHSType.equals(zXType.dsWhereConditionType.dswctAttr)
               || this.LHSType.equals(zXType.dsWhereConditionType.dswctBOContextAttr);
    }
    
    /**
     * @return Quick alternative for either type attr or BO context attr
     */
    public boolean isRHSAttrReference() {
        return this.RHSType.equals(zXType.dsWhereConditionType.dswctAttr)
               || this.RHSType.equals(zXType.dsWhereConditionType.dswctBOContextAttr);
    }
    
    //--------------------------------------- Public methods
    
    /**
     * Return current where condition in verbose format
     * @return Return current where condition in verbose format
     */
    public String getAsVerbose() {
        StringBuffer getAsVerbose = new StringBuffer();
        
        if (this.operator.equals(zXType.dsWhereConditionOperator.dswcoAnd)) {
            getAsVerbose.append(" AND ");
        } else if (this.operator.equals(zXType.dsWhereConditionOperator.dswcoOr)) {
            getAsVerbose.append(" OR ");
        }
        
        if (this.openNesting > 0) {
            getAsVerbose.append(StringUtil.padString(this.openNesting, '('));
        }
        
        if (this.LHSType.equals(zXType.dsWhereConditionType.dswctAttr)
            || this.LHSType.equals(zXType.dsWhereConditionType.dswctBOContextAttr)) {
            getAsVerbose.append(this.LHSBO.getDescriptor().getAttribute(this.LHSAttr.getName()).getLabel().getLabel());
        } else {
            getAsVerbose.append(this.LHSRawValue);
        }
        
        getAsVerbose.append(this.operand.getAsVerbose());
        
        if (this.RHSType.equals(zXType.dsWhereConditionType.dswctAttr)
            || this.RHSType.equals(zXType.dsWhereConditionType.dswctBOContextAttr)) {
            getAsVerbose.append(this.RHSBO.getDescriptor().getAttribute(this.RHSAttr.getName()).getLabel().getLabel());
        } else {
            getAsVerbose.append(this.RHSRawValue);            
        }
        
        if (this.closeNesting > 0) {
            getAsVerbose.append(StringUtil.padString(this.closeNesting, ')'));
        }
        
        return getAsVerbose().toString();
    }
    
    /**
     * Dump condition so it can be used to re-construct a where clause.
     * 
     * @return Returns a dump of the condition.
     */
    public String getAsWhereCondition() {
        StringBuffer getAsWhereCondition = new StringBuffer();
        
        /**
         * oprtr ( LHS op RHS )
         */
        if (this.operator.equals(zXType.dsWhereConditionOperator.dswcoNone)) {
        	// Ignored case.
        } else if (this.operator.equals(zXType.dsWhereConditionOperator.dswcoOr)) {
            getAsWhereCondition.append("| ");
        } else if (this.operator.equals(zXType.dsWhereConditionOperator.dswcoAnd)) {
            getAsWhereCondition.append("& ");
        }
        
        if (this.openNesting > 0) {
            getAsWhereCondition.append(StringUtil.padString(this.openNesting, '('));
        }
        
        getAsWhereCondition.append(this.LHSRawValue);
        getAsWhereCondition.append(this.operand.getOperator());
        getAsWhereCondition.append(this.RHSRawValue);
        
        if (this.closeNesting > 0) {
            getAsWhereCondition.append(StringUtil.padString(this.closeNesting, ')'));
        }
        
        return getAsWhereCondition.toString();
    }
    
    /**
     * Construct as where condition for a SQL where clause.
     * 
     * <pre>
     * 
     * Reviewed for V1.5:87
     * </pre>
     * 
     * @return Returns a constructed where condition.
     * @throws ZXException Thrown if getAsSQLWhereCondition fails.
     */
    public String getAsSQLWhereCondition() throws ZXException {
        StringBuffer getAsSQLWhereCondition = new StringBuffer();
        
        String strLeft = "";                // LHS
        String strRight = "";               // RHS
        DSHRdbms objLHSDSHandler = null;    // DS handler associated with LHS (where applicable)
        DSHRdbms objRHSDSHandler = null;    // DS handler associated with RHS (where applicable)
        DSHRdbms objBaseDSHandler;          // DS handler asscoiated with where clause base BO
        zXType.databaseType enmDBType;      // Database type (of base bo)
        
        /**
         * A condition has the following form:
         * 
         *  [openNesting] {operator] LHS operand RHS {closeNesting]
         *
         * Examples:
         *
         * id = 12               - No nesting, no operator
         * (id = 12)             - Open and close nesting
         * ((nme %% 'Bertus'     - Double open nesting, no close nesting
         * OR id = 12            - OR operator, no nesting
         * AND id > stts))       - AND operator, double close nesting
         *
         * Additional complexity is caused as we may have to convert LHS or RHS to a string
         * to make it all meaningful.
         */
        
        /**
         * Get handle to DS handlers as me may need them; also get database type
         */
        objBaseDSHandler = (DSHRdbms)this.whereClause.getBaseBO().getDS();

        if (isLHSAttrReference()) {
            objLHSDSHandler = (DSHRdbms)this.LHSBO.getDS();
            if (objLHSDSHandler == null) {
                throw new ZXException("Failed to load either LHS DSHandler");
            }
        }
        if (isRHSAttrReference()) {
            objRHSDSHandler = (DSHRdbms)this.RHSBO.getDS();
            if (objRHSDSHandler == null) {
                throw new ZXException("Failed to load either RHS DSHandler");
            }
        }
        
        // Resolve the dbType :
        enmDBType = objBaseDSHandler.getDbType();
        
        int intLHSDataType = 0;
        int intRHSDataType = 0;
        if (this.LHSAttr != null) {
            intLHSDataType = this.LHSAttr.getDataType().pos;
        }
        if (this.RHSAttr != null) {
            intRHSDataType = this.RHSAttr.getDataType().pos;
        }
        
        /**
         * Do the left first.
         */
        if (this.LHSType.pos == zXType.dsWhereConditionType.dswctString.pos) {
            strLeft = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, this.LHSValue, enmDBType);
            
        } else if (this.LHSType.pos == zXType.dsWhereConditionType.dswctNumber.pos) {
            /**
             * We may have to convert the LHS to a string using the RDBMS conversion routine;
             * this is the case if the RHS is an attribute reference and the datatype of the
             * RHS is a string
             */
            if (isRHSAttrReference()) {
                if (intRHSDataType == zXType.dataType.dtAutomatic.pos
                    || intRHSDataType == zXType.dataType.dtDouble.pos
                    || intRHSDataType == zXType.dataType.dtLong.pos) {
                    strLeft = getZx().getSql().dbStrValue(zXType.dataType.dtDouble.pos, this.LHSValue, enmDBType);
                    
                } else if (intRHSDataType == zXType.dataType.dtString.pos
                           || intRHSDataType == zXType.dataType.dtExpression.pos){
                    /**
                     * <number> = <string attribute>
                     */
                    strLeft = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, this.LHSValue, enmDBType);
                    
                }
                
            } else {
                strLeft = getZx().getSql().dbStrValue(zXType.dataType.dtDouble.pos, this.LHSValue, enmDBType);
                
            } // RHS is attribute reference?
            
        } else if (this.LHSType.pos == zXType.dsWhereConditionType.dswctDate.pos) {
            strLeft = getZx().getSql().dbStrValue(zXType.dataType.dtDate.pos, this.LHSValue, enmDBType);
            
        } else if (this.LHSType.pos == zXType.dsWhereConditionType.dswctSpecial.pos) {
            strLeft = resolveSpecialValue(this.LHSValue, enmDBType, this.RHSBO, this.RHSAttr);
            
        } else {
            /**
             * Must be a column (ie not a literal)
             * 
             * We may have to convert LHS if LHS = number and RHS is string
             */
            if (isRHSAttrReference()) {
                if (intLHSDataType == zXType.dataType.dtAutomatic.pos
                    || intLHSDataType == zXType.dataType.dtDouble.pos
                    || intLHSDataType == zXType.dataType.dtLong.pos) {
                    
                    if (intRHSDataType == zXType.dataType.dtString.pos
                        || intRHSDataType == zXType.dataType.dtExpression.pos) {
                        /**
                         * Need to do conversion
                         */
                        strLeft = SQL.dbStringWrapper(enmDBType, 
                                              		  getZx().getSql().columnName(this.LHSBO,
                                              				  					  this.LHSAttr,
                                              				  					  zXType.sqlObjectName.sonName),
                                                      this.LHSAttr.getLength());
                        
                    } else {
                        strLeft = getZx().getSql().columnName(this.LHSBO, this.LHSAttr, zXType.sqlObjectName.sonName);
                        
                    } // RHS DB type
                    
                } else {
                    strLeft = getZx().getSql().columnName(this.LHSBO, this.LHSAttr, zXType.sqlObjectName.sonName);
                    
                } // LHS datatype
                
            } else {
                strLeft = getZx().getSql().columnName(this.LHSBO, this.LHSAttr, zXType.sqlObjectName.sonName);
                
            } // RHS is attribute reference?
        }
        
        /**
         * Do the right.
         */
        if (this.RHSType.pos == zXType.dsWhereConditionType.dswctString.pos) {
            if (isLHSAttrReference()) {
                if (intLHSDataType == zXType.dataType.dtString.pos
                    || intLHSDataType == zXType.dataType.dtExpression.pos) {
                    strRight = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, this.RHSValue, enmDBType);
                    
                } else if (intLHSDataType == zXType.dataType.dtAutomatic.pos
                           || intLHSDataType == zXType.dataType.dtLong.pos
                           || intLHSDataType == zXType.dataType.dtDouble.pos) {
                    /**
                     * <number attr> = <string>
                     * No need for validation for proper number as it is already confirmed that RHS
                     * is a proper number
                     */
                    strRight = getZx().getSql().dbStrValue(zXType.dataType.dtDouble.pos, this.RHSValue, enmDBType);
                    
                } else {
                    /**
                     * Some unsupported situation (e.g. <date attr> = <string>, should never get here....
                     */
                    strRight = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, this.RHSValue, enmDBType);
                    
                }
                
            } else {
                strRight = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, this.RHSValue, enmDBType);
                
            } // LHS attribute?
            
        } else if (this.RHSType.pos == zXType.dsWhereConditionType.dswctNumber.pos) {
            /**
             * Support for optional data conversion
             */
            if (isLHSAttrReference()) {
                if (intLHSDataType == zXType.dataType.dtAutomatic.pos
                    || intLHSDataType == zXType.dataType.dtDouble.pos
                    || intLHSDataType == zXType.dataType.dtLong.pos) {
                    /**
                     * <number attr> = <number>
                     */
                    strRight = getZx().getSql().dbStrValue(zXType.dataType.dtDouble.pos, this.RHSValue, enmDBType);
                    
                } else if (intLHSDataType == zXType.dataType.dtString.pos
                           || intLHSDataType == zXType.dataType.dtExpression.pos) {
                    /**
                     * <string attribute> = <number>
                     */
                    strRight = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, this.RHSValue, enmDBType);
                    
                }
                
            } else {
                strRight = getZx().getSql().dbStrValue(zXType.dataType.dtDouble.pos, this.RHSValue, enmDBType);
                
            } // LHS is attribute reference?
            
        } else if (this.RHSType.pos == zXType.dsWhereConditionType.dswctDate.pos) {
            /**
             * If we have a LHS attr reference, use the exact datatype for date / time / timestamp
             */
            if (isLHSAttrReference()) {
                strRight = getZx().getSql().dbStrValue(this.LHSAttr.getDataType().pos, this.RHSValue, enmDBType);
            } else {
                strRight = getZx().getSql().dbStrValue(zXType.dataType.dtDate.pos, this.RHSValue, enmDBType);
            }
            
        } else if (this.RHSType.pos == zXType.dsWhereConditionType.dswctSpecial.pos) {
            strRight = resolveSpecialValue(this.RHSValue, enmDBType, this.LHSBO, this.LHSAttr);
            
        } else {
            /**
             * Must be a column (ie not a literal)
             *  
             * We may have to convert RHS if RHS = number and LHS is string
             */
            if (isLHSAttrReference()) {
                if (intRHSDataType == zXType.dataType.dtAutomatic.pos
                    || intRHSDataType == zXType.dataType.dtDouble.pos
                    || intRHSDataType == zXType.dataType.dtLong.pos) {
                    
                    if (intLHSDataType == zXType.dataType.dtString.pos
                        || intLHSDataType == zXType.dataType.dtExpression.pos) {
                        /**
                         * Need to do conversion
                         */
                        strRight = SQL.dbStringWrapper(enmDBType,
                        							   getZx().getSql().columnName(this.RHSBO,
                        								   					  	   this.RHSAttr,
                        									   					   zXType.sqlObjectName.sonName),
                        							   this.RHSAttr.getLength());
                        
                    } else {
                        strRight = getZx().getSql().columnName(this.RHSBO, this.RHSAttr, zXType.sqlObjectName.sonName);
                        
                    }
                    
                } else {
                    strLeft = getZx().getSql().columnName(this.RHSBO, this.RHSAttr, zXType.sqlObjectName.sonName);
                    
                } // RHS datatype
                
            } else {
                strRight = getZx().getSql().columnName(this.RHSBO, this.RHSAttr, zXType.sqlObjectName.sonName);
                
            } // LHS is attribute reference?
            
        }
        
        /**
         * Handle case sensitivity
         * Only relevant for:
         * column operator string, where column is case insenstive
         * string operator column
         */
        if (isLHSAttrReference() && this.RHSType.pos == zXType.dataType.dtString.pos) {
            if (this.LHSAttr.getTextCase().pos == zXType.textCase.tcInsensitive.pos) {
                strLeft = getZx().getSql().makeCaseInsensitive(strLeft, enmDBType);
                strRight = getZx().getSql().makeCaseInsensitive(strRight, enmDBType);
            }
        } // Handle case insensitive
        
        if (isRHSAttrReference() && this.LHSType.pos == zXType.dataType.dtString.pos) {
            if (this.RHSAttr.getTextCase().pos == zXType.textCase.tcInsensitive.pos) {
                strLeft = getZx().getSql().makeCaseInsensitive(strLeft, enmDBType);
                strRight = getZx().getSql().makeCaseInsensitive(strRight, enmDBType);
            }
        } // Handle case insensitive
        
        int intOperand = this.operand.pos;
        if (intOperand == zXType.compareOperand.coEQ.pos
            || intOperand == zXType.compareOperand.coNE.pos) {
            /**
             * Special cases:
             * - <attr> = #null
             * - #null = <attr>
             */
            if (isLHSAttrReference()
                && this.RHSType.pos == zXType.dsWhereConditionType.dswctSpecial.pos
                && this.RHSValue.equalsIgnoreCase("null")) {
                if (this.operand.pos == zXType.compareOperand.coEQ.pos) {
                    getAsSQLWhereCondition.append(strLeft).append(" IS NULL");
                } else {
                    getAsSQLWhereCondition.append(strLeft).append(" IS NOT NULL");
                }
            
            } else if (isRHSAttrReference() 
                       && this.LHSType.pos == zXType.dsWhereConditionType.dswctSpecial.pos
                       && this.LHSValue.equalsIgnoreCase("null")) {
                if (this.operand.pos == zXType.compareOperand.coEQ.pos) {
                    getAsSQLWhereCondition.append(strRight).append(" IS NULL");
                } else {
                    getAsSQLWhereCondition.append(strRight).append(" IS NOT NULL");
                }
                
            } else {
                getAsSQLWhereCondition.append(strLeft);
                getAsSQLWhereCondition.append(intOperand == zXType.compareOperand.coEQ.pos ? " = " : " <> ");
                getAsSQLWhereCondition.append(strRight);
                
            }
            
        } else if (intOperand == zXType.compareOperand.coSW.pos
                   || intOperand == zXType.compareOperand.coNSW.pos
                   || intOperand == zXType.compareOperand.coCNT.pos
                   || intOperand == zXType.compareOperand.coNCNT.pos) {
            /**
             * Results in
             * LHS (not) like RHS%
             *   or
             * LHS (not) like %RHS%
             * 
             * The wild-card is always on the RHS, add it to the string 
             * literal or concatenate to the RHS attribute
             */
            if (isRHSAttrReference()) {
                if (enmDBType.equals(zXType.databaseType.dbAccess) 
                    || enmDBType.equals(zXType.databaseType.dbSQLServer)) {
                    
                    if (intOperand == zXType.compareOperand.coCNT.pos
                        || intOperand == zXType.compareOperand.coNCNT.pos) {
                        strRight = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, "%", enmDBType) 
                                   + " & " + strRight;
                    }
                    strRight = strRight + " & " 
                               + getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, "%", enmDBType);
                    
                } else if (enmDBType.equals(zXType.databaseType.dbOracle)) {
                    if (intOperand == zXType.compareOperand.coCNT.pos
                        || intOperand == zXType.compareOperand.coNCNT.pos) {
                        strRight = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, "%", enmDBType) 
                                   + " || " + strRight;
                    }
                    strRight = strRight + " || " 
                               + getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, "%", enmDBType);
                    
                } else {
                    /**
                     * TBD: check whether this is supported for other databases
                     */
                    
                } // Database type.
                
            } else {
                /**
                 * If no attr reference it must be a string literal; now this is a bit tricky, we have
                 * constructed strRight so that it is already a proper string, this means we cannot
                 * simply add '%' as it would result in something like 'dispa'% (watch the quotes!),
                 * so we simply use me.RHSValue again.
                 * We also have a special problem when the RHS was the special value #value
                 * as obviously using the RHSValue (which is 'value') is less than 100% meaningful
                 */
            	String strRHSTmp = "";
            	if (this.RHSType.pos == zXType.dsWhereConditionType.dswctSpecial.pos
            		&& this.RHSValue.equalsIgnoreCase("value")) {
            		strRHSTmp = this.LHSBO.getValue(this.LHSAttrName).getStringValue();
            		
            	} else {
            		strRHSTmp = this.RHSValue;
            		
            	} // Are we dealing with #value?
            	
                if (intOperand == zXType.compareOperand.coCNT.pos) {
                    strRight = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos,
                                                           "%" + strRHSTmp + "%", 
                                                           enmDBType);
                } else {
                    strRight = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, 
                    									   strRHSTmp + "%",
                    									   enmDBType);
                }
                
            } // Attr reference of string literal
            
            if (intOperand == zXType.compareOperand.coSW.pos 
                || intOperand == zXType.compareOperand.coCNT.pos) {
                getAsSQLWhereCondition.append(strLeft);
                getAsSQLWhereCondition.append(" LIKE ");
                getAsSQLWhereCondition.append(strRight);
                
            } else {
                getAsSQLWhereCondition.append(strLeft);
                getAsSQLWhereCondition.append(" NOT LIKE ");
                getAsSQLWhereCondition.append(strRight);
                
            }
            
        } else {
            if (intOperand == zXType.compareOperand.coGT.pos) {
                getAsSQLWhereCondition.append(strLeft).append(" > ").append(strRight);
            } else if (intOperand == zXType.compareOperand.coGE.pos) {
                getAsSQLWhereCondition.append(strLeft).append(" >= ").append(strRight);
            } else if (intOperand == zXType.compareOperand.coLT.pos) {
                getAsSQLWhereCondition.append(strLeft).append(" < ").append(strRight);
            } else if (intOperand == zXType.compareOperand.coLE.pos) {
                getAsSQLWhereCondition.append(strLeft).append(" <= ").append(strRight);
            }
            
        } // Select operand
        
        /**
         * And finally operator and the nesting
         */
        if (this.openNesting > 0) {
            getAsSQLWhereCondition.insert(0, StringUtil.padString(this.openNesting, '('));
        }
        if (this.closeNesting > 0) {
            getAsSQLWhereCondition.append(StringUtil.padString(this.closeNesting, ')'));
        }
        
        /**
         * Insert in the operand.
         */
        if (this.operator.pos == zXType.dsWhereConditionOperator.dswcoNone.pos) {
            // Do nothing.
        } else if (this.operator.pos == zXType.dsWhereConditionOperator.dswcoOr.pos) {
            getAsSQLWhereCondition.insert(0, "OR ");
        } else if (this.operator.pos == zXType.dsWhereConditionOperator.dswcoAnd.pos) {
            getAsSQLWhereCondition.insert(0, "AND ");
        }
        
        return getAsSQLWhereCondition.toString();
    }
    
    /**
     * Resolve the BO and attr of a condition in case of a BO Attr Reference. 
     * This can be required when the BO context has changed since parsing 
     * the where clause.
     * 
     * V1.5:12 - Also resolve when attr reference (not just BO context attr reference)
     * 
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if resolve fails.
     */
    protected zXType.rc resolve() throws ZXException {
        zXType.rc resolve = zXType.rc.rcOK;
        
        if (this.LHSType.pos == zXType.dsWhereConditionType.dswctBOContextAttr.pos) {
            this.LHSBO = getZx().getBOContext().getBO(this.LHSBOName);
            if (this.LHSBO == null) {
                throw new ZXException("Unable to resolve LHS of condition, BO not found in context", 
                                      this.LHSBOName);
            }
            
            this.LHSAttr = this.LHSBO.getDescriptor().getAttribute(this.LHSAttrName);
            if (this.LHSAttr == null) {
                throw new ZXException("Unable to resolve LHS of condition, attr not found in BO",
                                      this.LHSBOName + "." + this.LHSAttrName);
            }
            
        } else if (this.LHSType.pos == zXType.dsWhereConditionType.dswctAttr.pos) {
            /**
             * Use baseBO; this means we only have to parse once and simply do a set BaseBO 
             * instead of a re-parse for every record.
             */
            this.LHSBO = this.whereClause.getBaseBO();
            this.LHSAttr = this.LHSBO.getDescriptor().getAttribute(this.LHSAttrName);
            
        } // LHS
        
        if (this.RHSType.pos == zXType.dsWhereConditionType.dswctBOContextAttr.pos) {
            this.RHSBO = getZx().getBOContext().getBO(this.RHSBOName);
            if (this.RHSBO == null) {
                throw new ZXException("Unable to resolve LHS of condition, BO not found in context", 
                                      this.RHSBOName);
            }
            
            this.RHSAttr = this.RHSBO.getDescriptor().getAttribute(this.RHSAttrName);
            if (this.RHSAttr == null) {
                throw new ZXException("Unable to resolve RHS of condition, attr not found in BO",
                                      this.RHSBOName + "." + this.RHSAttrName);
            }
            
        } else if (this.RHSType.pos == zXType.dsWhereConditionType.dswctAttr.pos) {
            /**
             * Use baseBO; this means we only have to parse once and simply do a set BaseBO 
             * instead of a re-parse for every record.
             */
            this.RHSBO = this.whereClause.getBaseBO();
            this.RHSAttr = this.RHSBO.getDescriptor().getAttribute(this.RHSAttrName);
            
        } // RHS
        
        return resolve;
    }
    
    /**
    * Evaluate a single condition.
    *
    * @return Returns the return code of the method. 
    * @throws ZXException Thrown if evaluate fails. 
    */
    public zXType.rc evaluate() throws ZXException{
        zXType.rc evaluate = zXType.rc.rcOK; 
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        try {
            
            /**
             * Convert LHS to property
             */
            Property objLHSProperty = resolvePropertyValue(this.LHSValue,
                                                       this.LHSBO,
                                                       this.LHSAttrName,
                                                       this.LHSType,
                                                       
                                                       this.RHSBO,
                                                       this.RHSAttrName);
            /**
             * And then RHS.
             */
            Property objRHSProperty = resolvePropertyValue(this.RHSValue,
                                                       this.RHSBO,
                                                       this.RHSAttrName,
                                                       this.RHSType,
                                                       
                                                       this.LHSBO,
                                                       this.LHSAttrName);
            
            /**
             * Allow for case insensitive searches.
             */
            boolean blnCaseInsensitive = false;
            if (isLHSAttrReference() 
                && this.LHSAttr.getTextCase().pos == zXType.textCase.tcInsensitive.pos) {
                if (isRHSAttrReference()) {
                    if (this.RHSAttr.getTextCase().pos == zXType.textCase.tcInsensitive.pos) {
                        blnCaseInsensitive = true;
                    }
                } else if (this.RHSType.pos == zXType.dsWhereConditionType.dswctString.pos){
                    blnCaseInsensitive = true;
                }
            }
            if (isRHSAttrReference() 
                && this.RHSAttr.getTextCase().pos == zXType.textCase.tcInsensitive.pos 
                && this.LHSType.pos == zXType.dsWhereConditionType.dswctString.pos){
                blnCaseInsensitive = true;
            }
            if (blnCaseInsensitive) {
                objLHSProperty = new StringProperty(objLHSProperty.getStringValue().toLowerCase());
                objRHSProperty = new StringProperty(objRHSProperty.getStringValue().toLowerCase());
            }
            
            /**
             * And compare trying to take datatypes into consideration
             */
            evaluate = objLHSProperty.evaluate(this.operand, objRHSProperty);
            if (evaluate.pos == zXType.rc.rcError.pos) {
                throw new ZXException("Unsupported operator for datatypes", dump());
            }
            
            return evaluate;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Evaluate a single condition.", e);
            if (getZx().throwException) throw new ZXException(e);
            evaluate = zXType.rc.rcError;
            return evaluate;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(evaluate);
                getZx().trace.exitMethod();
            }
        }
    }    
    
    //-------------------------------- Util Methods
    
    /**
     * @param pstrValue The value.
     * @param pobjThisBO The side you are on ie : LHS or RHS.
     * @param pstrThisAttrName The side you are on ie : LHS or RHS.
     * @param penmType The type of condition.
     * @param pobjOtherBO The oposite side.
     * @param pstrOtherAttrName The oposite side. 
     * @return Returns the property.
     * @throws Exception Thrown if getProtertyValue fails.
     */
    private Property resolvePropertyValue(String pstrValue,

                                      ZXBO pobjThisBO, 
                                      String pstrThisAttrName,
                                      zXType.dsWhereConditionType penmType, 
                                      
                                      ZXBO pobjOtherBO, 
                                      String pstrOtherAttrName) throws Exception {
        Property getPropertyValue = null;
        
        int intType = penmType.pos;
        if (intType == zXType.dsWhereConditionType.dswctAttr.pos
            || intType == zXType.dsWhereConditionType.dswctBOContextAttr.pos) {
            getPropertyValue = pobjThisBO.getValue(pstrThisAttrName);
            
        } else if (intType == zXType.dsWhereConditionType.dswctDate.pos) {
        	/**
        	 * The string could represent a Date/DateTime/Timestamp etc..
        	 */
        	DateFormat[] arrDateFormats = {getZx().getTimestampFormat(),
        								   getZx().getDateFormat(),
        								   getZx().getTimeFormat()};
        	
            getPropertyValue = new DateProperty(DateUtil.parse(arrDateFormats, pstrValue));
            
        } else if (intType == zXType.dsWhereConditionType.dswctNumber.pos) {
        	/**
        	 * This could be a interger/long or double.
        	 */
            try {
                long lngTmp = Long.parseLong(pstrValue);
                getPropertyValue = new LongProperty(lngTmp, false);
                
            } catch (NumberFormatException e) {
                double dblTmp = Double.parseDouble(pstrValue);
                getPropertyValue = new DoubleProperty(dblTmp, false);
            }
            
        } else if (intType == zXType.dsWhereConditionType.dswctString.pos) {
            getPropertyValue = new StringProperty(pstrValue);
            
        } else {
            if (pstrValue.equalsIgnoreCase("user")) {
                getPropertyValue = getZx().getUserProfile().getValue("id");
                
            } else if (pstrValue.equalsIgnoreCase("date")) {
                getPropertyValue = new DateProperty(getZx().getAppDate(), false, zXType.dataType.dtDate);
                
            } else if (pstrValue.equalsIgnoreCase("time")) {
                getPropertyValue = new DateProperty(getZx().getAppDate(), false, zXType.dataType.dtTime);
                
            } else if (pstrValue.equalsIgnoreCase("now")) {
                getPropertyValue = new DateProperty(getZx().getAppDate(), false, zXType.dataType.dtTimestamp);
                
            } else if (pstrValue.equalsIgnoreCase("true")) {
                getPropertyValue = new BooleanProperty(true);
                
            } else if (pstrValue.equalsIgnoreCase("false")) {
                getPropertyValue = new BooleanProperty(false);
                
            } else if (pstrValue.equalsIgnoreCase("value")) {
                getPropertyValue = pobjOtherBO.getValue(pstrOtherAttrName);
                
            } else if (pstrValue.equalsIgnoreCase("null")) {
                getPropertyValue = new StringProperty("", true);
                
            } else {
            	getPropertyValue = new StringProperty(getZx().getQuickContext().getEntry(this.LHSValue.substring(4)));
            	
            }
            
        }
        
        return getPropertyValue;
    }
    
    /**
     * @param pstrValue The value you want to resolve.
     * @param penmDBType The database type.
     * @param pobjBO The business object.
     * @param pobjAttr The attribute you want get the value of.
     * @return Returns the resolved value.
     * @throws ZXException Thrown if resolveSpecialValue fails.
     */
    private String resolveSpecialValue(String pstrValue,
                                       zXType.databaseType penmDBType,
                                       ZXBO pobjBO, 
                                       Attribute pobjAttr) throws ZXException {
        String getSpecialValue = "";
        
        if (pstrValue.equalsIgnoreCase("user")) {
            getSpecialValue = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, 
                                                          getZx().getUserProfile().getValue("id").getStringValue(), 
                                                          penmDBType);
            
        } else if (pstrValue.equalsIgnoreCase("date")) {
            getSpecialValue = getZx().getSql().dbStrValue(zXType.dataType.dtDate.pos, 
                                                          getZx().getAppDate(), 
                                                          penmDBType);
            
        } else if (pstrValue.equalsIgnoreCase("time")) {
            getSpecialValue = getZx().getSql().dbStrValue(zXType.dataType.dtTime.pos, 
                                                          getZx().getAppDate(), 
                                                          penmDBType);
            
        } else if (pstrValue.equalsIgnoreCase("now")) {
            getSpecialValue = getZx().getSql().dbStrValue(zXType.dataType.dtTimestamp.pos,
                                                          getZx().getAppDate(), 
                                                          penmDBType);
            
        } else if (pstrValue.equalsIgnoreCase("false")
                   || pstrValue.equalsIgnoreCase("true")) {
            getSpecialValue = getZx().getSql().dbStrValue(zXType.dataType.dtBoolean.pos, 
                                                          pstrValue, 
                                                          penmDBType);
            
        } else if (pstrValue.equalsIgnoreCase("value")) {
            if (pobjBO != null && pobjAttr != null) {
                getSpecialValue = getZx().getSql().dbValue(pobjBO, pobjAttr);
            }
            
        } else if (pstrValue.equalsIgnoreCase("null")) {
            /**
             * Does not make any sense but there you are...
             */
            getSpecialValue = getZx().getSql().dbStrValue(zXType.dataType.dtString.pos, 
                                                          "", 
                                                          penmDBType);
        } else {
        	/**
        	 * Must be qs.xxx
        	 */
        	getSpecialValue = getZx().getSql().dbStrValue(pobjAttr.getDataType().pos, 
        												  getZx().getQuickContext().getEntry(pstrValue.substring(4)), 
        												  penmDBType);
        	
        }
        
        return getSpecialValue;
    }
    
    /**
     * @return Returns the return code of validateCondition.
     * @throws ZXException Throws an exception if the Where Condition is invalid.
     */
    public zXType.rc validateCondition() throws ZXException {
        zXType.rc validateCondition = zXType.rc.rcOK;
        
        boolean blnLHSAttrReference = isLHSAttrReference();
        boolean blnRHSAttrReference = isRHSAttrReference();
        if (blnLHSAttrReference && blnRHSAttrReference) {
            if (checkCompatbilityAttrAttr().pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Incompatibility found between LHS and RHS of condition", dump());
            }
        } else if (blnLHSAttrReference && !blnRHSAttrReference) {
            if (checkCompatbilityAttrLiteral().pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Incompatibility found between LHS and RHS of condition", dump());
            }
            
        } else if (!blnLHSAttrReference && blnRHSAttrReference) {
            if (checkCompatbilityAttrLiteral().pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Incompatibility found between LHS and RHS of condition", dump());
            }
            
        } else if (!blnLHSAttrReference && 
                   (this.RHSType.pos == zXType.dsWhereConditionType.dswctSpecial.pos 
                   && (this.RHSRawValue.equalsIgnoreCase("#value") || this.RHSRawValue.equalsIgnoreCase("#qs")) )) {
            throw new ZXException("Cannot use #value special value if not used in combination with attribute reference", 
                                  dump());
            
        } else if (!blnRHSAttrReference && 
                   (this.LHSType.pos == zXType.dsWhereConditionType.dswctSpecial.pos 
                   && (this.LHSRawValue.equalsIgnoreCase("#value") || this.LHSRawValue.equalsIgnoreCase("#qs")) )) {
            throw new ZXException("Cannot use #value special value if not used in combination with attribute reference", 
                                dump());
            
        } else {
            if (checkCompatbilityLiteralLiteral().pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Incompatibility found between LHS and RHS of condition", dump());
            }
            
        }
        
        return validateCondition;
    }
    
    //--------------------------------- DSWhereCondition Checks
    
    /**
     * Check the compatibility between two attributes.
     * @return Returns an rcError if incompatible.
     * @throws ZXException Thrown if checkCompatbilityAttrAttr fails.
     */
    private zXType.rc checkCompatbilityAttrAttr() throws ZXException {
        zXType.rc checkCompatbilityAttrAttr = zXType.rc.rcOK;
        
        DSHandler objLHSDSHandler = this.LHSBO.getDS();
        DSHandler objRHSDSHandler = this.RHSBO.getDS();
        
        /**
         * May-be a join is out of the question all-together
         */
        if (!getZx().getDataSources().canDoDBJoin(this.LHSBO, this.RHSBO)) {
            getZx().trace.addError("Cannot combine data-sources in condition", 
                                   objLHSDSHandler.getName() + " / " + objRHSDSHandler.getName());
            checkCompatbilityAttrAttr = zXType.rc.rcError;
        }
        
        /**
         * Compare data types
         */
        // TODO : May use the properties to handle this for me. At the moment this is not been used.
        
        return checkCompatbilityAttrAttr;
    }
    
    /**
     * Check the compatibility between an attribute and a literal.
     * @return Returns an rcError if incompatible.
     */
    private zXType.rc checkCompatbilityAttrLiteral() {
        zXType.rc checkCompatbilityAttrLiteral = zXType.rc.rcOK;
        
        // This may be overkill.
        
        return checkCompatbilityAttrLiteral;
    }
    
    /**
     * Check the compatibility between two literals.
     * 
     * @return Returns an rcError if incompatible.
     */
    private zXType.rc checkCompatbilityLiteralLiteral() {
        zXType.rc checkCompatbilityLiteralLiteral = zXType.rc.rcOK;
        
        /**
         *  With a literal / literal condition we have no mercy: must be of same time; developer
         *  should simply know better
         */
        
        if (this.LHSType.pos != this.RHSType.pos) {
            getZx().trace.addError("Incompatble literal types combined in where condition", 
                                   this.LHSValue + " / " + this.RHSValue);
            checkCompatbilityLiteralLiteral = zXType.rc.rcError;
        }
        
        return checkCompatbilityLiteralLiteral;
    }
    
    /**
     * Dump the contens of condition for debugging purposes.
     * 
     * @return Returns a dump of the where condition.
     */
    public String dump() {
        StringBuffer dump = new StringBuffer();
        
        dump.append(this.LHSRawValue);
        dump.append(this.operand.getOperator());
        dump.append(this.RHSRawValue);
        
        return dump.toString();
    }
}