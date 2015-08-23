/*
 * Created on Apr 16, 2005
 * $Id: DSWhereClause.java,v 1.1.2.31 2006/07/17 16:26:59 mike Exp $
 */
package org.zxframework.datasources;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.zxframework.Attribute;
import org.zxframework.AttributeCollection;
import org.zxframework.Tuple;
import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.property.DateProperty;
import org.zxframework.property.DoubleProperty;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.DateUtil;
import org.zxframework.util.StringUtil;

/**
 * A data-source where clause.
 * 
 * <pre>
 * 
 * A data-source where clause is a where clause in the format as supported by all data sources
 * and has three variations:
 * 
 * basic: id,stts
 *      Takes values from BO and translates to id=<value> and stts=<value>; note that
 *      in case of a single element, it can also refer to a group
 * 
 * simple: =id,>stts
 *     Takes values from BO but takes operator into consideration
 *     
 * full: :id=12&(stts=1|stts=2)
 *      All singing all dancing, starts with ':' to indicate this syntax
 *      
 * Assume the following where clause:
 * 
 * id=1 | (id=2 & stts=1) | (dte>#date & stts=2)
 *
 * This clause breaks down in 5 conditions:
 * 1.    id=1
 * 2.    id=2
 * 3.    stts=1
 * 4.    dte>#date
 * 5.    stts=2
 *
 * The nesting and operators (| and &) are associated with each condition:
 *       Condition       Operator    Open nesting    Close nesting
 * 1.    id=1            -           0               0
 * 2.    id=2            |           1               0
 * 3.    stts=1          &           0               1
 * 4.    dte>#date       |           1               0
 * 5.    stts=2          &           0               1
 *
 * Terminology:
 *
 * - Where clause: the clause as a whole
 * - Where condition: single condition, optionally with operator open nesting and close nesting
 * - Operator: AND (&) or OR (|)
 * - Operand: =, <>, >, >=, <, <=, % (starts with), !% (does not start with), %% (contains), !%% (does not contain)
 * - Open nesting: integer indicating number of open parenthesis ('(') before a condition
 * - Close nesting: integer indicating number of close parenthesis (')') after a condition
 * - Token: same as condition
 * - LHS: left-hand-side of condition
 * - RHS: right-hand-side of a condition
 * - Base BO: business object that the where clause is for
 * - Attr reference: LHS / RHS is an attribute name of the base BO (simply an attribute name)
 * - BO Context attr: LHS / RHS is an attribute name of a BO in the BO context (syntax: bo.attrName)
 * - String: LHS / RHS is a string literal (enclosed in either insgle- or double quotes, use '\' to escape characters)
 * - Date: LHS / RHS is a date literal (enclosed in '#')
 * - Number: LHS / RHS is a integer or number
 * - Special: LHS / RHS starts with a '#' and is one of the following special values:
 *       - #date     current date
 *       - #time     current time
 *       - #now      current timestamp
 *       - #user     current user-id
 *       - #true     true
 *       - #false    false
 *       - #null     null value
 *       - #value    use the value of property (can only be used if other-HS is reference to attribute)
 * - Id: only used during parsing, will translate into attr / BO Context attr
 * 
 * 
 * Change    : BD13APR05 - V1.5:4
 * Why       : Fixed bug introduced with 1.5:1 (support of data sources)
 *             Did not support booleans correctly in simple where groups
 *             
 * Change    : BD29APR05 - V1.5:7
 * Why       : Fixed bug for full where conditions that did not allow empty strings, e.g.
 *             :id = ''
 *               
 * Change    : BD9MAY05 - V1.5:11
 * Why       : Removed use of type def (used by evaluate) as this caused a massive problem on
 *             some servers with ASP. ASP is typeless and, only on some servers mind you,
 *             will refuse to create any object from a DLL that contains a type definition.
 *             The ASP error is something like 080-0030 or so with reason code 0117 or so.
 *             Took hours to solve....
 *
 * Change    : BD9MAY05 - V1.5:12
 * Why       : Fixed bug for singleWhereCondition where operand is EQ or NE and value is null
 * 
 * Change    : BD6FEB06 - V1.5:88
 * Why       : Added support for #qs.xxx construct
 *  
 * Change    : BD8FEB06 - V1.5:89
 * Why       : Reinstated support for useEqual and useOr parameters to
 *               whereCondition
 *
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class DSWhereClause extends ZXObject {
    
    //------------------------ Members
    
    private transient ArrayList tokens;
    private transient ZXBO baseBO;
    
    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public DSWhereClause() {
        super();
        
        // Init values
        this.tokens = new ArrayList();
        this.baseBO = null;
    }
    
    //------------------------ Getters/Setters
    
    /**
     * BO that was used to create basic clause.
     * 
     * @return Returns the baseBO.
     */
    public ZXBO getBaseBO() {
        return baseBO;
    }
    
    /**
     * @param baseBO The baseBO to set.
     */
    public void setBaseBO(ZXBO baseBO) {
        this.baseBO = baseBO;
    }
    
    /**
     * Collection of clsDSWhereCondition for a parsed clause.
     * 
     * @return Returns the tokens.
     */
    public ArrayList getTokens() {
        return tokens;
    }
    
    /**
     * @param tokens The tokens to set.
     */
    public void setTokens(ArrayList tokens) {
        this.tokens = tokens;
    }
    
    //------------------------ Parsing code
    
    /**
     * Parse a BO clause.
     *
     * @param pobjBO The business object. 
     * @param pstrWhereClause The whereClause to parse. 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if parse fails. 
     */
    public zXType.rc parse(ZXBO pobjBO, String pstrWhereClause) throws ZXException {
        return parse(pobjBO, pstrWhereClause, true, false);
    }
    
	/**
	 * Parse a BO clause.
	 *
	 * @param pobjBO The business object. 
	 * @param pstrWhereClause The whereClause to parse. 
	 * @param pblnUseEqual Only available for simple clauses. Optional, default should be true 
	 * @param pblnUseOr Only available for simple clauses. Optional, default should be false. 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if parse fails. 
	 */
	public zXType.rc parse(ZXBO pobjBO, String pstrWhereClause, boolean pblnUseEqual, boolean pblnUseOr) throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjBO", pobjBO);
			getZx().trace.traceParam("pstrWhereClause", pstrWhereClause);
			getZx().trace.traceParam("pblnUseEqual", pblnUseEqual);
			getZx().trace.traceParam("pblnUseOr", pblnUseOr);
		}

		zXType.rc parse = zXType.rc.rcOK;
		
		try {
            // Reset values.
            this.tokens = new ArrayList();
            this.baseBO = pobjBO;
            
            if (StringUtil.len(pstrWhereClause) == 0) return parse;
            
            /**
             * Can be basic / simple or full
             */
            if (pstrWhereClause.charAt(0) != ':') {
                if (StringUtil.containsAny(",!=<>%", pstrWhereClause)) {
                    parse = parseSimple(pobjBO, pstrWhereClause, pblnUseEqual, pblnUseOr);
                } else {
                    parse = parseBasic(pobjBO, pstrWhereClause, pblnUseEqual, pblnUseOr);
                }
                
            } else {
                parse = parseFull(pobjBO, pstrWhereClause);
                
            } // Simple / enhanced or full
            
			return parse;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Parse a BO clause.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjBO = " + pobjBO);
				getZx().log.error("Parameter : pstrWhereClause = " + pstrWhereClause);
				getZx().log.error("Parameter : pblnUseEqual = " + pblnUseEqual);
				getZx().log.error("Parameter : pblnUseOr = " + pblnUseOr);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			parse = zXType.rc.rcError;
			return parse;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(parse);
				getZx().trace.exitMethod();
			}
		}
	}
    
	/**
	 * Parse a basic where clause (ie where the single element is a group).
	 *
	 * @param pobjBO The business object. 
	 * @param pstrWhereClause The whereclause to parse. 
	 * @param pblnUseEqual Optional, default should be true. 
	 * @param pblnUseOr Optional, default should be false 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if parseBasic fails. 
	 */
	private zXType.rc parseBasic(ZXBO pobjBO, 
                                 String pstrWhereClause, 
                                 boolean pblnUseEqual, 
                                 boolean pblnUseOr) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjBO", pobjBO);
			getZx().trace.traceParam("pstrWhereClause", pstrWhereClause);
			getZx().trace.traceParam("pblnUseEqual", pblnUseEqual);
			getZx().trace.traceParam("pblnUseOr", pblnUseOr);
		}

		zXType.rc parseBasic = zXType.rc.rcOK;
		
		try {
		    /**
             * Can be single attribute or group
             */
            String strGroup;
            if (pobjBO.getDescriptor().getAttributes().get(pstrWhereClause) == null) {
                // Rather call getGroupPlus directly than call groupPlusAsString. 
                strGroup = pobjBO.getDescriptor().getGroup(pstrWhereClause).formattedString();
                
            } else {
                strGroup = pstrWhereClause;
            }
            
            if (StringUtil.len(strGroup) == 0) {
                throw new ZXException("Unable to retrieve group / attribute", pstrWhereClause);
            }
            
            parseBasic = parseSimple(pobjBO, strGroup, pblnUseEqual, pblnUseOr);
            
			return parseBasic;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Parse a basic where clause (ie where the single element is a group).", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjBO = "+ pobjBO);
				getZx().log.error("Parameter : pstrWhereClause = "+ pstrWhereClause);
				getZx().log.error("Parameter : pblnUseEqual = "+ pblnUseEqual);
				getZx().log.error("Parameter : pblnUseOr = "+ pblnUseOr);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			parseBasic = zXType.rc.rcError;
			return parseBasic;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(parseBasic);
				getZx().trace.exitMethod();
			}
		}
	}
    
	/**
	 * Parse a full where clause.
	 *
	 * @param pobjBO The business object. 
	 * @param pstrWhereClause The whereclause to parse. 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if parseFull fails. 
	 */
	private zXType.rc parseFull(ZXBO pobjBO, String pstrWhereClause) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjBO", pobjBO);
			getZx().trace.traceParam("pstrWhereClause", pstrWhereClause);
		}

		zXType.rc parseFull = zXType.rc.rcOK;
		
		try {
		    
            int intChar;                                    // Position in string
            String strToken;                                // Next token
            DSWHereClauseToken objToken;                    // Next token
            String strRawToken = "";                        // 'Raw' token
            zXType.dsWhereConditionType enmTokenType = zXType.dsWhereConditionType.dswctDefault;       // Type of token
            zXType.dsWhereConditionOperator enmOperator;    // Operator (AND / OR)
            int intOpenNest = 0;                            // Number of open (
            int intCloseNest = 0;                           // Number of closing )
            String strLHS;                                  // LHS
            String strLHSRaw;                               // LHS in 'raw' format
            zXType.dsWhereConditionType enmLHSType;         // Token type of LHS
            String strOperand;                              // Operand
            String strRHS;                                  // RHS
            String strRHSRaw;                               // RHS in 'raw' format
            zXType.dsWhereConditionType enmRHSType;         // Token type of RHS
            
            /**
             * Note that the first character may or may not be a ':'; when called
             * directly it may, when called recursively it will not
             */
            if (pstrWhereClause.charAt(0) == ':') {
                pstrWhereClause = pstrWhereClause.substring(1);
            }
            
            intChar = 0; 
            
            objToken = getNextToken(pstrWhereClause, intChar, enmTokenType, strRawToken);
            strToken = objToken.strToken;
            intChar = objToken.intChar;
            enmTokenType = objToken.enmTokenType;
            strRawToken = objToken.strRawToken;
            
            while (StringUtil.len(strToken) > 0) {
                
                /**
                 * See if there is a leading AND or OR
                 */
                if (enmTokenType.pos == zXType.dsWhereConditionType.dswctOperator.pos) {
                    if (strToken.equals("&")) {
                        enmOperator = zXType.dsWhereConditionOperator.dswcoAnd;
                    } else if (strToken.equals("|")) {
                        enmOperator = zXType.dsWhereConditionOperator.dswcoOr;
                    } else {
                        throw new ZXException("Unexpected token at position " + intChar, strToken);
                    } // Token
                    
                    /**
                     * Get next token
                     */
                    objToken = getNextToken(pstrWhereClause, intChar, enmTokenType, strRawToken);
                    strToken = objToken.strToken;
                    intChar = objToken.intChar;
                    enmTokenType = objToken.enmTokenType;
                    strRawToken = objToken.strRawToken;
                    
                    if (StringUtil.len(strToken) == 0) {
                        throw new ZXException("Unexpected end-of-string " + intChar);
                    }
                    
                } else {
                    enmOperator = zXType.dsWhereConditionOperator.dswcoNone;
                    
                } // Starts with & or |
                
                /**
                 * Nibble away all opening parenthesis
                 */
                while (enmTokenType.pos == zXType.dsWhereConditionType.dswctNesting.pos) {
                    if (strToken.equals("(")) {
                        intOpenNest++;
                        
                        objToken = getNextToken(pstrWhereClause, intChar, enmTokenType, strRawToken);
                        strToken = objToken.strToken;
                        intChar = objToken.intChar;
                        enmTokenType = objToken.enmTokenType;
                        strRawToken = objToken.strRawToken;
                        
                        if (StringUtil.len(strToken) == 0) {
                            throw new ZXException("Unexpected end-of-string " + intChar);   
                        }
                        
                    } else {
                        throw new ZXException("Unexpected token at position " + intChar, strToken);
                        
                    } // Found ( or )
                    
                } // Nibble away all (
                
                /**
                 * Now expect a valid LHS
                 */
                if (enmTokenType.pos == zXType.dsWhereConditionType.dswctAttr.pos
                    || enmTokenType.pos == zXType.dsWhereConditionType.dswctString.pos
                    || enmTokenType.pos == zXType.dsWhereConditionType.dswctDate.pos
                    || enmTokenType.pos == zXType.dsWhereConditionType.dswctNumber.pos) {
                    enmLHSType = enmTokenType;
                    strLHS = strToken;
                    strLHSRaw = strRawToken;
                    
                    objToken = getNextToken(pstrWhereClause, intChar, enmTokenType, strRawToken);
                    strToken = objToken.strToken;
                    intChar = objToken.intChar;
                    enmTokenType = objToken.enmTokenType;
                    strRawToken = objToken.strRawToken;
                    
                    if (StringUtil.len(strToken) == 0) {
                        throw new ZXException("Unexpected end-of-string " + intChar);
                    }
                    
                } else {
                    throw new ZXException("Unexpected token at position " + intChar, strToken);
                    
                }
                
                /**
                 * Now expect an operand
                 */
                if (enmTokenType.pos == zXType.dsWhereConditionType.dswctOperand.pos) {
                    strOperand = strToken;
                    
                    objToken = getNextToken(pstrWhereClause, intChar, enmTokenType, strRawToken);
                    strToken = objToken.strToken;
                    intChar = objToken.intChar;
                    enmTokenType = objToken.enmTokenType;
                    strRawToken = objToken.strRawToken;
                    
                    if (StringUtil.len(strRawToken) == 0) {
                        throw new ZXException("Unexpected end-of-string " + intChar);
                    }
                } else {
                    throw new ZXException("Unexpected token at position " + intChar, strToken);
                    
                }
                
                /**
                 * And valid RHS
                 */
                if (enmTokenType.pos == zXType.dsWhereConditionType.dswctAttr.pos
                        || enmTokenType.pos == zXType.dsWhereConditionType.dswctString.pos
                        || enmTokenType.pos == zXType.dsWhereConditionType.dswctDate.pos
                        || enmTokenType.pos == zXType.dsWhereConditionType.dswctNumber.pos) {
                        enmRHSType = enmTokenType;
                        strRHS = strToken;
                        strRHSRaw = strRawToken;
                        
                        objToken = getNextToken(pstrWhereClause, intChar, enmTokenType, strRawToken);
                        strToken = objToken.strToken;
                        intChar = objToken.intChar;
                        enmTokenType = objToken.enmTokenType;
                        strRawToken = objToken.strRawToken;
                } else {
                    throw new ZXException("Unexpected token at position " + intChar, strToken);
                    
                }
                
                /**
                 * And optional closing )
                 */
                while (enmTokenType.pos == zXType.dsWhereConditionType.dswctNesting.pos) {
                    if (strToken.equals(")")) {
                        intCloseNest++;
                        
                        objToken = getNextToken(pstrWhereClause, intChar, enmTokenType, strRawToken);
                        strToken = objToken.strToken;
                        intChar = objToken.intChar;
                        enmTokenType = objToken.enmTokenType;
                        strRawToken = objToken.strRawToken;
                    }
                    
                } // Nibble away all )
                
                
                /**
                 * Now we have enough info to create a where-clause condition
                 */
                
                if (constructWhereConditionFromToken(pobjBO,
                                                     strLHS, strLHSRaw, enmLHSType,
                                                     strOperand,
                                                     strRHS, strRHSRaw, enmRHSType,
                                                     intOpenNest, intCloseNest,
                                                     enmOperator).pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to construct valid where-condition", strLHS + " " + strOperand + " " + strRHS);
                }
                
                /**
                 * Ready for next condition
                 */
                intOpenNest = 0;
                intCloseNest = 0;
                enmOperator = zXType.dsWhereConditionOperator.dswcoNone;
                enmLHSType = zXType.dsWhereConditionType.dswctDefault;
                strLHS = "";
                enmRHSType = zXType.dsWhereConditionType.dswctDefault;
                strRHS = "";
                
            } // Loop until no more tokens
            
			return parseFull;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Parse a full where clause.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjBO = " + pobjBO);
				getZx().log.error("Parameter : pstrWhereClause = " + pstrWhereClause);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			parseFull = zXType.rc.rcError;
			return parseFull;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(parseFull);
				getZx().trace.exitMethod();
			}
		}
	}
    
	/**
	 * Parse a simple where clause.
	 *
	 * <pre>
	 * 
	 * Reviewed for V1.5:89
	 * </pre>
	 * 
	 * @param pobjBO The business object. 
	 * @param pstrWhereClause The where clause to parse.
	 * @param pblnUseEqual Optional, default should be true. 
	 * @param pblnUseOr Optional, default should be false. 
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if parseSimple fails. 
	 */
	private zXType.rc parseSimple(ZXBO pobjBO, 
                                  String pstrWhereClause, 
                                  boolean pblnUseEqual, 
                                  boolean pblnUseOr) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrWhereClause", pstrWhereClause);
			getZx().trace.traceParam("pblnUseEqual", pblnUseEqual);
			getZx().trace.traceParam("pblnUseOr", pblnUseOr);
		}

		zXType.rc parseSimple = zXType.rc.rcOK;
		
		try {
		    
            /**
             * the clause can be a list of zero or more attributes
             * separated by commas (note that this is different from a
             * traditional ad-hoc attribute group)
             * Each element is considered to be an attribute
             * 
             * And each item can be prefixed with the following:
             * 
             *   - Nothing       --> use =
             *   - <> or !       --> use <>
             *   - >=            --> use >=
             *   - >             --> use >
             *   - <=            --> use <=
             *   - <             --> use <
             *   - | and any of the above:
             *                   --> OR with where clause so far rather than AND
             */
            String strElement;
            boolean blnUseOr;
            zXType.compareOperand enmOp;
            zXType.dsWhereConditionOperator enmOperator;
            String strAttr;
            String strRawValue;
            zXType.dsWhereConditionType enmType;
            
            StringTokenizer strElements = new StringTokenizer(pstrWhereClause,",");
            while (strElements.hasMoreElements()) {
                strElement = strElements.nextToken();
                
                /**
                 * Now we have single element; see if it starts with a '|'; save
                 * that knowledge and drop the '|'
                 */
                if (strElement.charAt(0) == '|') {
                    blnUseOr = true;
                    strElement = strElement.substring(1);
                } else {
                    blnUseOr = pblnUseOr;
                }
                
                /**
                 * Now process the single element
                 */
                /**
                 *Now process the single element
                 *
                 *Note that '-' is now actually obsolete!!!!
                 */
                if (strElement.charAt(0) == '!' || strElement.charAt(0) == '-') {
                    enmOp = zXType.compareOperand.coNE;
                    strAttr = strElement.substring(1);
                    
                } else if ( strElement.startsWith("<>") ) {
                    enmOp = zXType.compareOperand.coNE;
                    strAttr = strElement.substring(2);
                    
                } else if ( strElement.startsWith("<=") ) {
                    enmOp = zXType.compareOperand.coLE;
                    strAttr = strElement.substring(2);
                    
                } else if ( strElement.charAt(0) == '<' ) {
                    enmOp = zXType.compareOperand.coLT;
                    strAttr = strElement.substring(1);
                    
                } else if ( strElement.startsWith(">=") ) {
                    enmOp = zXType.compareOperand.coGE;
                    strAttr = strElement.substring(2);
                    
                } else if (strElement.startsWith(">")) {
                    enmOp = zXType.compareOperand.coGT;
                    strAttr = strElement.substring(1);  
                    
                } else if (strElement.charAt(0) == '=') {
                    enmOp = zXType.compareOperand.coEQ;
                    strAttr = strElement.substring(1);  
                    
                } else if (strElement.startsWith("%%")) {
                    enmOp = zXType.compareOperand.coCNT;
                    strAttr = strElement.substring(2);  
                    
                } else if (strElement.charAt(0) =='%') {
                    enmOp = zXType.compareOperand.coSW;
                    strAttr = strElement.substring(1);  
                    
                } else {
                	if (pblnUseEqual) {
                		enmOp = zXType.compareOperand.coEQ;
                	} else {
                		enmOp = zXType.compareOperand.coNE;
                	}
                	
                    strAttr = strElement;
                    
                }
                
                /**
                 * Add to token collection
                 * Determine whether to use AND / OR or NONE
                 */
                if (this.tokens.size() > 0) {
                    if (blnUseOr) {
                        enmOperator = zXType.dsWhereConditionOperator.dswcoOr;
                    } else {
                        enmOperator = zXType.dsWhereConditionOperator.dswcoAnd;
                    }
                    
                } else {
                    enmOperator = zXType.dsWhereConditionOperator.dswcoNone;
                }
                
                /**
                 * Get rid of any leading / trailing spaces for attribute name
                 */
                strAttr = strAttr.trim();
                
                Attribute objAttr = pobjBO.getDescriptor().getAttribute(strAttr);
                if (objAttr == null) {
                    throw new ZXException("Unable to parse 'simple' where clause; attribute not found", strAttr);
                }
                
                /**
                 * Translate BO datatype to conditionType
                 */
                /**
                 * Take value in raw format, may be overwritten later for some special values
                 */
                String strValue = pobjBO.getValue(strAttr).getStringValue();
                
                /**
                 * BD22APR05 - V1.5:4
                 */
                if (pobjBO.getValue(strAttr).isNull) {
                    enmType = zXType.dsWhereConditionType.dswctSpecial;
                    strRawValue = "#null";
                    strValue = "null";
                    
                } else {
                    int intDataType = objAttr.getDataType().pos;
                    if (intDataType == zXType.dataType.dtAutomatic.pos 
                        || intDataType == zXType.dataType.dtDouble.pos 
                        || intDataType == zXType.dataType.dtLong.pos) {
                        enmType = zXType.dsWhereConditionType.dswctNumber;
                        strRawValue = pobjBO.getValue(strAttr).getStringValue();
                        
                    } else if (intDataType == zXType.dataType.dtDate.pos 
                               || intDataType == zXType.dataType.dtTime.pos 
                               || intDataType == zXType.dataType.dtTimestamp.pos) {
                        enmType = zXType.dsWhereConditionType.dswctDate;
                        strRawValue = "#" + pobjBO.getValue(strAttr).getStringValue() + "#";
                        
                    } else if (intDataType == zXType.dataType.dtBoolean.pos) {
                        /**
                         * V1.5:4 - Added explicit support for boolean data type
                         */
                        enmType = zXType.dsWhereConditionType.dswctSpecial;
                        
                        if (pobjBO.getValue(strAttr).booleanValue()) {
                            strRawValue = "#true";
                        } else {
                            strRawValue = "#false";
                        }
                        
                    } else {
                        enmType = zXType.dsWhereConditionType.dswctString;
                        strRawValue = "'" + pobjBO.getValue(strAttr).getStringValue() + "'";
                    }
                } //  Null?
                
                if (addCondition(pobjBO,
                                 zXType.dsWhereConditionType.dswctAttr, strAttr, strAttr,
                                 enmOp,
                                 enmType, strValue, strRawValue,
                                 enmOperator).pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to construct where-condition", 
                                          pstrWhereClause + " (error at " + strElement + ")");
                }
                
            }
            
			return parseSimple;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Parse a simple where clause.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrWhereClause = " + pstrWhereClause);
                getZx().log.error("Parameter : pblnUseEqual = " + pblnUseEqual);
				getZx().log.error("Parameter : pblnUseOr = " + pblnUseOr);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			parseSimple = zXType.rc.rcError;
			return parseSimple;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(parseSimple);
				getZx().trace.exitMethod();
			}
		}
	}
    
    /**
     * Evaluate a where clause; this routine can be used by channel 
     * handlers that want to implement a where clause mechanism.
     * 
     * <pre>
     * Returns:
     * 
     * rcError: unable to evaluate, horrible problem
     * rcOk: evaluated to true
     * rcWarning: evaluated to false
     * </pre>
     * 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if evaluate failed.
     */
    public zXType.rc evaluate() throws ZXException {
        zXType.rc evaluate = zXType.rc.rcOK;
        
        /**
         * Resolve all conditions as the BO context may have 
         * changed since we parsed the clause
         */
        DSWhereCondition objCondition;
        
        int intTokens = this.tokens.size();
        for (int i = 0; i < intTokens; i++) {
            objCondition = (DSWhereCondition)this.tokens.get(i);
            
            if (objCondition.resolve().pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Unable to resolve condition");
            }
        }
        
        // Will throw exception if there is an error.
        EvaluateToken objEvaluteToken = new EvaluateToken();
        objEvaluteToken.intToken = 0;
        int intRC = evaluateToken(0, objEvaluteToken).pos;
        if (intRC == zXType.rc.rcOK.pos) {
            evaluate = zXType.rc.rcOK;
            
        } else if (intRC == zXType.rc.rcWarning.pos) {
            evaluate = zXType.rc.rcWarning;
            
        } else {
            /**
             * We have an error.
             */
            getZx().trace.addError("Unable to evaluate where-clause" , new Exception());
            evaluate =zXType.rc.rcError;
            return evaluate;
        }
        
        return evaluate;
    }
    
    //------------------------ Parsing helper methods.
    
    /**
     * Adhoc inner class.
     */
    private class DSWHERECONDITION_RESULT {
        /** Comment for <code>enmOperator</code> */
        public zXType.dsWhereConditionOperator enmOperator;
        /** Comment for <code>blnValue</code> */
        public boolean blnValue;
    }
    
    /**
     * Use to emulate VB pass by ref of ints.
     */
    private class EvaluateToken {
        /** Number of tokens eaten so far. */
        public int intToken;
        
        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return new StringBuffer(26)
            .append("intToken=").append(intToken).toString();
        }
    }
    
    /**
     * Evaluate a single token; used by evaluate and called recursively.
     * 
     * <pre>
     * 
     * NOTE : Remember primitives in java are passed by value and not by ref.
     * 
     * V1.5:11 - Reviewed to no longer rely on type definition as this caused major problems
     *           in ASP environment
     * NOTE : This change is not relevant for us.
     * </pre>
     * 
     * @param pintNestLevel The level deep. 
     * @param pobjEvaluateToken The current level and eaten tokens.
     * @return Returns the number of tokens eaten up.
     * @throws ZXException Thrown if evaluateToken fails. 
     */
    private zXType.rc evaluateToken(int pintNestLevel, EvaluateToken pobjEvaluateToken) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjEvaluateToken", pobjEvaluateToken);
        }
        
        zXType.rc evaluateToken = zXType.rc.rcOK; 
        
        try {
            boolean blnEndResult = false;
            
            DSWhereCondition  objCondition = null;      // Handle to next condition
            DSWHERECONDITION_RESULT arrResult[];        // Result so far
            DSWHERECONDITION_RESULT arrFinalResult[];   // Pointer to next free final result slot
            int intResult;                              // Pointer to next free result slot
            int intFinalResult;                         // Pointer to next free final result slot
            int intLastLoop = -1;                        // Protect against endless loops
            
            /**
             * This routine may look horribly complex and probably is.
             * The challenge is that the conditions that make up a where clause are stored
             * as a collection where a 'proper' parser would store them in an object-model-like structure.
             * This is done for speed and partially for simplicity.
             *
             * Assume the following where clause:
             *
             * id=1 | (id=2 & stts=1) | (dte>#date & stts=2)
             *
             * This clause breaks down in 5 conditions:
             * 1.    id=1
             * 2.    id=2
             * 3.    stts=1
             * 4.    dte>#date
             * 5.    stts=2
             *
             * The nesting and operators (| and &) are associated with each condition:
             *       Condition       Operator    Open nesting    Close nesting
             * 1.    id=1            -           0               0
             * 2.    id=2            |           1               0
             * 3.    stts=1          &           0               1
             * 4.    dte>#date       |           1               0
             * 5.    stts=2          &           0               1
             *
             * When we call this routine we pass the number of the token in the token condition (BYREF)
             * and the nesting level (i.e. number of open parenthesis) (also BYREF).
             *
             * Whenever we come accross an open-parenthesis we call this routine recursively and just
             * take the return value as the result of whatever is inside the parenthesis.
             * For non-nested conditions, we loop over each condition and evaluate the value (either true or false).
             *
             * We store all results in an array as an operator / value pair; the above example may result in:
             *
             * 1.    false
             * 2. |  false       (as a result of recursive call)
             * 3. |  true        (as a result of recursive call)
             *
             * The final step is equally confusing to make sure that AND operators take precendence over
             * OR operators. For this we loop over the result-set and copy the values appropriately to
             * the final result set. Imagine the following result-set:
             *
             * true | false | true & true & false | true
             *
             * This is stored in arrResult as:
             *
             * 1.        true
             * 2.    |   false
             * 3.    |   true
             * 4.    &   true
             * 5.    &   false
             * 6.    |   true
             *
             * We first loop over the result set and take care of all AND operators; we simply copy each non-AND
             * value to the final result set:
             *
             * Result set            true | false | true & true & false | true
             *                         |      |      |
             * Final result set      true | false | true
             *
             * As soon as we come accross an AND, we take special action: we AND the AND-value with the last
             * value stored in the final result set and replace the result
             *
             * Result set            true | false | true & true & false | true
             *                         |      |      |--&---|               |
             *                       true | false | true                    |
             *                         |      |      |------&-------|       |
             *                       true | false | false                   |
             *                         |      |      |       |--------------|
             *                       true | false | false | true
             *
             * At the end of all of this we are left with a series of true's and falses that are all OR-ed
             * together.
             * 
             * NOTE : This is not relevant.
             * To further complicate matters we had to split the arrResult and arrFinalResult; we used to
             * store a typedef in this array (with a boolean for the fina value of a token and an operand
             * for the operand with next token); storing objects in an array is a no-go zone in VB and now
             * it turned out using typedefs in a VB projects CAN (depends on OS level or something) cause
             * a massive head-ache in a type-less environment like ASP.
             * 
             * Amen!
             */
            int intTokens = this.tokens.size();
            arrResult = new DSWHERECONDITION_RESULT[intTokens];
            intResult = 0;
            
            doneLoop : while (pobjEvaluateToken.intToken < intTokens) {
                if (intLastLoop == pobjEvaluateToken.intToken) {
                    throw new ZXException("Unable to evaluate where clause");
                }
                intLastLoop = pobjEvaluateToken.intToken;
                
                arrResult[intResult] = new DSWHERECONDITION_RESULT();
                
                objCondition = (DSWhereCondition)this.tokens.get(pobjEvaluateToken.intToken);
                
                if (objCondition.getOpenNesting() - pintNestLevel > 0) {
                    /**
                     * For each nesting level, we call recursively
                     */
                    int intRC = evaluateToken(pintNestLevel + 1, pobjEvaluateToken).pos;
                    if (intRC == zXType.rc.rcOK.pos) {
                        arrResult[intResult].blnValue = true;
                        
                    } else if (intRC == zXType.rc.rcWarning.pos) {
                        arrResult[intResult].blnValue = false;
                        
                    } else {
                        /**
                         * An error have accurred.
                         */
                        getZx().trace.addError("Error evaluating clause from token " + objCondition.dump());
                        evaluateToken = zXType.rc.rcError;
                        return evaluateToken;
                        
                    }
                    
                    arrResult[intResult].enmOperator = objCondition.getOperator();
                    intResult++;
                    
                } else {
                    /**
                     * Evaluate condition and add to clause
                     */
                    int intRC = objCondition.evaluate().pos;
                    if (intRC == zXType.rc.rcOK.pos) {
                        arrResult[intResult].blnValue = true;
                        
                    } else if (intRC == zXType.rc.rcWarning.pos) {
                        arrResult[intResult].blnValue = false;
                        
                    } else if (intRC == zXType.rc.rcError.pos) {
                        throw new ZXException("Error evaluating clause from token " + objCondition.dump());
                        
                    }
                    
                    /**
                     * Always ignore operator on first result of this call
                     */
                    if(intResult == 0) { // Was 0
                        arrResult[intResult].enmOperator = zXType.dsWhereConditionOperator.dswcoNone;
                    } else {
                        arrResult[intResult].enmOperator = objCondition.getOperator();
                    }
                    
                    intResult++;
                    
                    pobjEvaluateToken.intToken++;
                }
                
                /**
                 * if close nesting, we are done with this call
                 */
                if (objCondition.getCloseNesting() - pintNestLevel == 0 && objCondition.getCloseNesting() > 0) {
                    break doneLoop; // GoTo doneLoop
                }
            }
            
            
     //------------------------------
     // doneLoop : --------------------
     //------------------------------
            /**
             * For the grande finally, see the comments above!
             */
            arrFinalResult = new DSWHERECONDITION_RESULT[intResult];
            intFinalResult = 0; // was 0 in VB.
            
            for (int i = 0; i < intResult; i++) {
            	arrFinalResult[i] = new DSWHERECONDITION_RESULT();
            	
                if (arrResult[i].enmOperator.pos == zXType.dsWhereConditionOperator.dswcoAnd.pos) {
                    arrFinalResult[intFinalResult - 1].blnValue = arrFinalResult[intFinalResult - 1].blnValue && arrResult[i].blnValue;
                    if (intFinalResult - 1 > 1) {
                        arrFinalResult[intFinalResult - 1].enmOperator = zXType.dsWhereConditionOperator.dswcoOr;
                    } else {
                        arrFinalResult[intFinalResult - 1].enmOperator = zXType.dsWhereConditionOperator.dswcoNone;
                    }
                    
                } else {
                    arrFinalResult[intFinalResult] = arrResult[i];
                    intFinalResult++;
                    
                }
                
            }
            
            for (int i = 0; i < intFinalResult; i++) {
                if (arrFinalResult[i].enmOperator.pos == zXType.dsWhereConditionOperator.dswcoNone.pos) {
                    blnEndResult = arrFinalResult[i].blnValue;
                } else if (arrFinalResult[i].enmOperator.pos == zXType.dsWhereConditionOperator.dswcoAnd.pos) {
                    // Ignored cause.
                } else if (arrFinalResult[i].enmOperator.pos == zXType.dsWhereConditionOperator.dswcoOr.pos) {
                    blnEndResult = blnEndResult || arrFinalResult[i].blnValue;
                }
            }
            
            if (blnEndResult) {
                evaluateToken = zXType.rc.rcOK;
            } else {
                evaluateToken = zXType.rc.rcWarning;
            }
        //------------------------------
        // doneLoop : --------------------
        //------------------------------
            
            return evaluateToken;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Evaluate a single token; used by evaluate and called recursively.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pintNestLevel = "+ pobjEvaluateToken);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            evaluateToken = zXType.rc.rcError;
            return evaluateToken;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(evaluateToken);
                getZx().trace.returnValue(pobjEvaluateToken);
                getZx().trace.exitMethod();
            }
        }
    }    
    
    /**
     * Add a condition to the token collection
     *
     * @param pobjBO The business object the condition is for. 
     * @param penmLHSType The type of the lhs. 
     * @param pstrLHSValue The value of the LHS 
     * @param pstrLHSRawValue The raw value of the LHS 
     * @param penmOperand The operand. 
     * @param penmRHSType The RHS Type. 
     * @param pstrRHSValue The RH SValue 
     * @param pstrRHSRawValue The RHS Raw value 
     * @param penmOperator The operator 
     * @return Returns the return code of addCondition.
     * @throws ZXException Thrown if addCondition fails. 
     */
    public zXType.rc addCondition(ZXBO pobjBO, 
                             	  zXType.dsWhereConditionType penmLHSType, 
                             	  String pstrLHSValue, 
                             	  String pstrLHSRawValue, 
                             	  zXType.compareOperand penmOperand, 
                             	  zXType.dsWhereConditionType penmRHSType, 
                             	  String pstrRHSValue, 
                             	  String pstrRHSRawValue, 
                             	  zXType.dsWhereConditionOperator penmOperator) throws ZXException {
        
        return addCondition(pobjBO,
                            penmLHSType, 
                            pstrLHSValue, 
                            pstrLHSRawValue, 
                            penmOperand, 
                            penmRHSType, 
                            pstrRHSValue, 
                            pstrRHSRawValue, 
                            penmOperator, 
                            0, 0);
        
    }
    
	/**
	 * Add a condition to the token collection
	 *
	 * @param pobjBO The business object the condition is for. 
	 * @param penmLHSType The type of the lhs. 
	 * @param pstrLHSValue The value of the LHS 
	 * @param pstrLHSRawValue The raw value of the LHS 
	 * @param penmOperand The operand. 
	 * @param penmRHSType The RHS Type. 
	 * @param pstrRHSValue The RH SValue 
	 * @param pstrRHSRawValue The RHS Raw value 
	 * @param penmOperator The operator. Optional, default should be "dswcoNone"
	 * @param pintOpenNesting The level of open nesting. Optional, default should be 0.
	 * @param pintCloseNesting The level of closed nesting. Optional, default should be 0.
     * @return Returns the return code of addCondition.
	 * @throws ZXException Thrown if addCondition fails. 
	 */
	public zXType.rc addCondition(ZXBO pobjBO, 
                             zXType.dsWhereConditionType penmLHSType, 
                             String pstrLHSValue, 
                             String pstrLHSRawValue, 
                             zXType.compareOperand penmOperand, 
                             zXType.dsWhereConditionType penmRHSType, 
                             String pstrRHSValue, 
                             String pstrRHSRawValue, 
                             zXType.dsWhereConditionOperator penmOperator, 
                             int pintOpenNesting, 
                             int pintCloseNesting) throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pobjBO", pobjBO);
			getZx().trace.traceParam("penmLHSType", penmLHSType);
			getZx().trace.traceParam("pstrLHSValue", pstrLHSValue);
			getZx().trace.traceParam("pstrLHSRawValue", pstrLHSRawValue);
			getZx().trace.traceParam("penmOperand", penmOperand);
			getZx().trace.traceParam("penmRHSType", penmRHSType);
			getZx().trace.traceParam("pstrRHSValue", pstrRHSValue);
			getZx().trace.traceParam("pstrRHSRawValue", pstrRHSRawValue);
			getZx().trace.traceParam("penmOperator", penmOperator);
			getZx().trace.traceParam("pintOpenNesting", pintOpenNesting);
			getZx().trace.traceParam("pintCloseNesting", pintCloseNesting);
		}

        zXType.rc addCondition = zXType.rc.rcOK;
        
        if (penmOperator == null) {
            penmOperator = zXType.dsWhereConditionOperator.dswcoNone;
        }
        
		try {
            DSWhereCondition objCondition = new DSWhereCondition();
            
            objCondition.setWhereClause(this);
            objCondition.setLHSType(penmLHSType);
            int intType = objCondition.getLHSType().pos;
            
            if (intType == zXType.dsWhereConditionType.dswctAttr.pos 
                || intType == zXType.dsWhereConditionType.dswctBOContextAttr.pos) {
                if (resolveAttrReference(pstrLHSValue, pobjBO, objCondition, true).pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to resolve attribute reference", pstrLHSValue);
                }
                
                objCondition.setLHSRawValue(pstrLHSValue);
            } else {
                /**
                 * Literals must be passed as property objects
                 */
                objCondition.setLHSValue(pstrLHSValue);
                objCondition.setLHSRawValue(pstrLHSRawValue);
            }
            
            objCondition.setOperand(penmOperand);
            
            objCondition.setRHSType(penmRHSType);
            intType = objCondition.getRHSType().pos;
            
            if (intType == zXType.dsWhereConditionType.dswctAttr.pos 
                    || intType == zXType.dsWhereConditionType.dswctBOContextAttr.pos) {
                if (resolveAttrReference(pstrRHSValue, pobjBO, objCondition, false).pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to resolve attribute reference", pstrRHSValue);
                }
                
                objCondition.setRHSRawValue(pstrRHSValue);
            } else {
                /**
                 * Literals must be passed as property objects
                 */
                objCondition.setRHSValue(pstrRHSValue);
                objCondition.setRHSRawValue(pstrRHSRawValue);
            }
            
            objCondition.setOpenNesting(pintOpenNesting);
            objCondition.setCloseNesting(pintCloseNesting);
            
            /**
             * Never allow an operator when first token
             */
            if (this.tokens.size() == 0) {
                objCondition.setOperator(zXType.dsWhereConditionOperator.dswcoNone);
            } else {
                objCondition.setOperator(penmOperator);
            }
            
            /**
             * So far we have been dealing with LHS and RHS values as either a reference to an attribute
             * or a literal value in string format. Now lets do some sanity checks on the types of LHS and
             * RHS values....
             *
             * one-HS            Other-HS        Rule
             * Attribute         Literal         one-HS = string, all Other-HS values OK
             *                                   one-HS = number, Other-HS must be numeric
             *                                   one-HS = date, Other-HS must be date
             *                                   one-HS = boolean, Other-HS must be boolean value
             * Attribute         Attribute       Must be of same type
             * Literal           Literal         Must be of same type
             */
            objCondition.validateCondition();
            
            /**
             * Add the where condition.
             */
            this.tokens.add(objCondition);
            
            return addCondition;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Add a condition to the token collection", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pobjBO = "+ pobjBO);
				getZx().log.error("Parameter : penmLHSType = "+ penmLHSType);
				getZx().log.error("Parameter : pstrLHSValue = "+ pstrLHSValue);
				getZx().log.error("Parameter : pstrLHSRawValue = "+ pstrLHSRawValue);
				getZx().log.error("Parameter : penmOperand = "+ penmOperand);
				getZx().log.error("Parameter : penmRHSType = "+ penmRHSType);
				getZx().log.error("Parameter : pstrRHSValue = "+ pstrRHSValue);
				getZx().log.error("Parameter : pstrRHSRawValue = "+ pstrRHSRawValue);
				getZx().log.error("Parameter : penmOperator = "+ penmOperator);
				getZx().log.error("Parameter : pintOpenNesting = "+ pintOpenNesting);
				getZx().log.error("Parameter : pintCloseNesting = "+ pintCloseNesting);
			}
			
			if (getZx().throwException) throw new ZXException(e);
            addCondition = zXType.rc.rcError;
            return addCondition;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.exitMethod();
			}
		}
	}
    
	/**
	 * Convert a token that represents an attribute to an attribute.
     * 
     * <pre>
     * 
	 * The BO that is passed is the default BO (ie the
	 * one that is used when no BO is specified).
     * </pre>
	 *
	 * @param pstrToken The token we are going to resolve.
	 * @param pobjBO The business object.
	 * @param pobjCondition The condition we are resolve for.
	 * @param pblnLHS Whether the token is on the LHS or the RHS
	 * @return Returns the return code of the method. 
	 * @throws ZXException Thrown if resolveAttrReference fails. 
	 */
	public zXType.rc resolveAttrReference(String pstrToken, 
                                          ZXBO pobjBO, 
                                          DSWhereCondition pobjCondition, 
                                          boolean pblnLHS) throws ZXException{
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
			getZx().trace.enterMethod();
			getZx().trace.traceParam("pstrToken", pstrToken);
			getZx().trace.traceParam("pobjBO", pobjBO);
			getZx().trace.traceParam("pobjCondition", pobjCondition);
			getZx().trace.traceParam("pblnLHS", pblnLHS);
		}

		zXType.rc resolveAttrReference = zXType.rc.rcOK;
		
		try {
            String strAttr;
            ZXBO objBO;
            String strBO = "";
            
            /**
             * Options are:
             *  - attr -> Attribute of pobjBO
             *   - bo.attr -> Attribute of bo (as found in BO context)
             */
		    int intToken = pstrToken.indexOf('.');
            if (intToken != -1) {
                strBO = pstrToken.substring(0, intToken);
                strAttr = pstrToken.substring(intToken);
                
                /**
                 * Try BO context
                 */
                objBO = getZx().getBOContext().getBO(strBO);
                if (pobjBO == null) {
                    throw new ZXException("BO not found in BO context nor a valid entity", strBO);
                }
                
            } else {
                strAttr = pstrToken;
                
                /**
                 * In order to reuse code below, set objBO to pobjBO
                 */
                objBO = pobjBO;
                
            }
            
            Attribute objAttr = objBO.getDescriptor().getAttribute(strAttr);
            if (objAttr == null) {
                throw new ZXException("Unable to retrieve attribute", pstrToken);
            }
            
            /**
             * Set the reference parameters
             */
            if (pblnLHS) {
                pobjCondition.setLHSBO(objBO);
                pobjCondition.setLHSAttr(objAttr);
                
                pobjCondition.setLHSBOName(strBO);
                pobjCondition.setLHSAttrName(strAttr);
                
            } else {
                pobjCondition.setRHSBO(objBO);
                pobjCondition.setRHSAttr(objAttr);
                
                pobjCondition.setRHSBOName(strBO);
                pobjCondition.setRHSAttrName(strAttr);
                                
            } // Set LHS or RHS data
            
			return resolveAttrReference;
		} catch (Exception e) {
			getZx().trace.addError("Failed to : Convert a token that represents an attribute to an attribute.", e);
			if (getZx().log.isErrorEnabled()) {
				getZx().log.error("Parameter : pstrToken = "+ pstrToken);
				getZx().log.error("Parameter : pobjBO = "+ pobjBO);
				getZx().log.error("Parameter : pobjCondition = "+ pobjCondition);
				getZx().log.error("Parameter : pblnLHS = "+ pblnLHS);
			}
			
			if (getZx().throwException) throw new ZXException(e);
			resolveAttrReference = zXType.rc.rcError;
			return resolveAttrReference;
		} finally {
			if(getZx().trace.isFrameworkCoreTraceEnabled()) {
				getZx().trace.returnValue(resolveAttrReference);
				getZx().trace.exitMethod();
			}
		}
	}
    
    /**
     * Add a clause to this clause and combine the two using AND.
     * 
     * @param pobjBO The business object.
     * @param pstrClause The where clause.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if addClauseWithOR fails.
     */
    public zXType.rc addClauseWithAND(ZXBO pobjBO, String pstrClause) throws ZXException {
        return addClauseWith(pobjBO, pstrClause, zXType.dsWhereConditionOperator.dswcoAnd);
    }
    
    /**
     * Add a clause to this clause and combine the two using AND.
     * 
     * @param pobjBO The business object.
     * @param pstrClause The where clause.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if addClauseWithOR fails.
     */
    public zXType.rc addClauseWithOR(ZXBO pobjBO, String pstrClause) throws ZXException {
        return addClauseWith(pobjBO, pstrClause, zXType.dsWhereConditionOperator.dswcoOr);
    }
    
    /**
     * Add a clause to this clause and combine the two using OR/AND.
     * 
     * @param pobjBO The business object.
     * @param pstrClause The where clause.
     * @param penmType The type of merge to do.
     * @return Returns the return code of the method.
     * @throws ZXException Thrown if addClauseWithOR fails.
     */
    private zXType.rc addClauseWith(ZXBO pobjBO, String pstrClause, zXType.dsWhereConditionOperator penmType) throws ZXException {
        if (getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrClause", pstrClause);
        }
        
        zXType.rc addClauseWith = zXType.rc.rcOK;
        
        try {
            
            if (StringUtil.len(pstrClause) == 0) return addClauseWith;
            
            if (this.tokens.size() == 0) {
                addClauseWith = parse(pobjBO, pstrClause);
                
            } else {
                /**
                 * Parse the second clause
                 */
                DSWhereClause objDSWhereClause = new DSWhereClause();
                
                if (objDSWhereClause.parse(pobjBO, pstrClause).pos != zXType.rc.rcOK.pos) {
                    throw new ZXException("Unable to parse clause");
                }
                
                /**
                 * The idea is as follows:
                 *  - Wrap original clause in ( and )
                 *  - Wrap new clause in ( and )
                 *   - Add all tokens from new clause
                 */
                wrapClauseInParenthesis();
                objDSWhereClause.wrapClauseInParenthesis();
                
                /**
                 * Now add all tokens of new clause to first and set OR/AND for first
                 */
                DSWhereCondition objCondition;
                boolean blnDoneFirst = false;
                int intToken = objDSWhereClause.getTokens().size();
                for (int i = 0; i < intToken; i++) {
                    objCondition = (DSWhereCondition)objDSWhereClause.getTokens().get(i);
                    
                    if (!blnDoneFirst) {
                        blnDoneFirst = true;
                        
                        if (objCondition.getOperator().pos != zXType.dsWhereConditionOperator.dswcoNone.pos) {
                            throw new ZXException("Unable to merge clause that already starts with AND or OR");
                        }
                        objCondition.setOperator(penmType);
                    }
                    
                    this.tokens.add(objCondition);
                }
                
            } // Nothing to OR/AND with yet
            
            
            return addClauseWith;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Add a clause to this clause and combine the two using OR/AND.",e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrClause = " + pstrClause);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            addClauseWith = zXType.rc.rcError;
            return addClauseWith;
        } finally {
            if (getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(addClauseWith);
                getZx().trace.exitMethod();
            }
        }
    }

    /**
     * Construct WhereCondition from a token,
     *
     * @param pobjBO The business object. 
     * @param pstrLeft The left hand value 
     * @param pstrLeftRaw Raw Left hand value 
     * @param penmLeftType LHS Type 
     * @param pstrOp The operand. 
     * @param pstrRight The right hand value 
     * @param pstrRightRaw Raw right hand side value. 
     * @param penmRightType The right hand side type. 
     * @param pintOpenNest The open nest level. 
     * @param pintCloseNest The close nest level 
     * @param penmOperator The operator. 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if constructWhereConditionFromToken fails. 
     */
    public zXType.rc constructWhereConditionFromToken(ZXBO pobjBO, 
    												  String pstrLeft, 
    												  String pstrLeftRaw, 
    												  zXType.dsWhereConditionType penmLeftType, 
    												  String pstrOp, 
    												  String pstrRight, String pstrRightRaw, 
    												  zXType.dsWhereConditionType penmRightType, 
    												  int pintOpenNest, int pintCloseNest, 
    												  zXType.dsWhereConditionOperator penmOperator) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrLeft", pstrLeft);
            getZx().trace.traceParam("pstrLeftRaw", pstrLeftRaw);
            getZx().trace.traceParam("penmLeftType", penmLeftType);
            getZx().trace.traceParam("pstrOp", pstrOp);
            getZx().trace.traceParam("pstrRight", pstrRight);
            getZx().trace.traceParam("pstrRightRaw", pstrRightRaw);
            getZx().trace.traceParam("penmRightType", penmRightType);
            getZx().trace.traceParam("pintOpenNest", pintOpenNest);
            getZx().trace.traceParam("pintCloseNest", pintCloseNest);
            getZx().trace.traceParam("penmOperator", penmOperator);
        }

        zXType.rc constructWhereConditionFromToken = zXType.rc.rcOK;
        
        try {
            
            zXType.dsWhereConditionType enmLeft;        // LHS type
            zXType.dsWhereConditionType enmRight;       // RHS type
            zXType.compareOperand enmOperand = null;    // Operand
            
            /**
             * Resolve LHS Type
             */
            enmLeft = resolveType(penmLeftType, pstrLeft);
            
            /**
             * Same stuff for RHS
             */
            enmRight = resolveType(penmRightType, pstrRight);
            
            if (pstrOp.charAt(0) == '=') {
                enmOperand = zXType.compareOperand.coEQ;
                
            } else if (pstrOp.equals("<>") || pstrOp.equals("!")) {
                enmOperand = zXType.compareOperand.coNE;
                
            } else if (pstrOp.equals(">=")) {
                enmOperand = zXType.compareOperand.coGE;
                
            } else if (pstrOp.equals("<=")) {
                enmOperand = zXType.compareOperand.coLE;
                
            } else if (pstrOp.equals("%%")) {
                enmOperand = zXType.compareOperand.coCNT;
                
            } else if (pstrOp.equals("!%")) {
                enmOperand = zXType.compareOperand.coNSW;
                
            } else if (pstrOp.equals("!%%")) {
                enmOperand = zXType.compareOperand.coNCNT;
                
            } else if (pstrOp.charAt(0) == '>') {
                enmOperand = zXType.compareOperand.coGT;
                
            } else if (pstrOp.charAt(0) == '<') {
                enmOperand = zXType.compareOperand.coLT;
                
            } else if (pstrOp.charAt(0) == '%') {
                enmOperand = zXType.compareOperand.coSW;
                
            } else {
                throw new ZXException("Unsupported operand", pstrOp);
                
            }
            
            if (addCondition(pobjBO,
                    		 enmLeft, pstrLeft, pstrLeftRaw,
                    		 enmOperand,
                    		 enmRight, pstrRight, pstrRightRaw,
                    		 penmOperator,
                    		 pintOpenNest, pintCloseNest).pos != zXType.rc.rcOK.pos) {
            	
                getZx().trace.addError("Unable to create where-clause condition");
                constructWhereConditionFromToken = zXType.rc.rcError;
                return constructWhereConditionFromToken;
                
            }
            
            return constructWhereConditionFromToken;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Construct WhereCondition from a token,", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = " + pobjBO);
                getZx().log.error("Parameter : pstrLeft = " + pstrLeft);
                getZx().log.error("Parameter : pstrLeftRaw = " + pstrLeftRaw);
                getZx().log.error("Parameter : penmLeftType = " + penmLeftType);
                getZx().log.error("Parameter : pstrOp = " + pstrOp);
                getZx().log.error("Parameter : pstrRight = " + pstrRight);
                getZx().log.error("Parameter : pstrRightRaw = " + pstrRightRaw);
                getZx().log.error("Parameter : penmRightType = " + penmRightType);
                getZx().log.error("Parameter : pintOpenNest = " + pintOpenNest);
                getZx().log.error("Parameter : pintCloseNest = " + pintCloseNest);
                getZx().log.error("Parameter : penmOperator = " + penmOperator);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            constructWhereConditionFromToken = zXType.rc.rcError;
            return constructWhereConditionFromToken;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(constructWhereConditionFromToken);
                getZx().trace.exitMethod();
            }
        }
    }
    
    private zXType.dsWhereConditionType resolveType(zXType.dsWhereConditionType penmType, 
                                                    String pstrValue) throws ZXException {
        zXType.dsWhereConditionType resolveType;
        
        /**
         * We have made a very consious decision in not trying to be too clever
         * and come up with all sorts of clever data manipulation based on the
         * type on the left and type on the right.
         * When you are using extended where conditions you really have to know
         * what you are doing...
         */
        int intLeftType = penmType.pos;
        if (intLeftType == zXType.dsWhereConditionType.dswctString.pos) {
            resolveType = zXType.dsWhereConditionType.dswctString;
            
        } else if (intLeftType == zXType.dsWhereConditionType.dswctNumber.pos) {
            resolveType = zXType.dsWhereConditionType.dswctNumber;
            if (!StringUtil.isNumeric(pstrValue)) {
                throw new ZXException("Unable to interpret as numeric value", pstrValue);
            }
            
        } else if (intLeftType == zXType.dsWhereConditionType.dswctDate.pos) {
           /**
            * Note that tokenType date is used for both date and special (as they start with
            * '#') so when we get here we alsways assume it is a date (see getNextToken)
            */
            resolveType = zXType.dsWhereConditionType.dswctSpecial;
            
            String arrTokens[] = {"user", "date", "time", "now", "false", "true", "null", "value"};
            if (!StringUtil.equalsAnyOf(arrTokens, pstrValue)) {
            	if (!pstrValue.toLowerCase().startsWith("qs.")) {
            		resolveType = zXType.dsWhereConditionType.dswctDate;
            	}
            }
            
        } else {
            /**
             * Must be a column
             */
            if (pstrValue.charAt(0) == 0) {
                resolveType = zXType.dsWhereConditionType.dswctAttr;
            } else {
                resolveType = zXType.dsWhereConditionType.dswctBOContextAttr;
            }
            
        }
        return resolveType;
    }
    
    /**
     * Quick inner class for the return value of getNextToken.
     */
    private class DSWHereClauseToken {
        /** Position to start */
        public int intChar;
        /** Type of token */
        public zXType.dsWhereConditionType enmTokenType;
        /** Next token but with quotes and escape characters */
        public String strRawToken;
        /** The next token. */
        public String strToken;
    }
    
    /**
     * Get next token from a string with tokens, starting at position intChar.
     *
     * <pre>
     * 
     * NOTE : Java passes primitives always by value and not by ref.
     * </pre>
     * 
     * @param pstrString String to get next token from 
     * @param pintChar Position to start 
     * @param penmTokenType Type of token 
     * @param pstrRawToken Next token but with quotes and escape characters 
     * @return Returns the getNextToken.
     * @throws ZXException Thrown if getNextToken fails. 
     */
    private DSWHereClauseToken getNextToken(String pstrString, 
                                            int pintChar, 
                                            zXType.dsWhereConditionType penmTokenType, 
                                            String pstrRawToken) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrString", pstrString);
            getZx().trace.traceParam("pintChar", pintChar);
            getZx().trace.traceParam("penmTokenType", penmTokenType);
            getZx().trace.traceParam("pstrRawToken", pstrRawToken);
        }
        
        DSWHereClauseToken getNextToken = new DSWHereClauseToken(); 

        try {
            String strToken = "";                            // Token so far
            char strChar = ' ';                              // Current character
            zXType.dsWhereConditionType enmState;            // Tokenize state
            boolean blnDouble = false;                       // String started with double quotes
            boolean blnSingle = false;                       // String started with single quote
            boolean blnEscape = false;                       // Need to escape character in string
            
            zXType.dsWhereConditionType enmLastState = zXType.dsWhereConditionType.dswctDefault; // Protect against endless loop
            int intLastChar = -1;                             // Protect against endless loop
            
            int intState = 0;
            
            /**
             * Start in default state
             */
            enmState = zXType.dsWhereConditionType.dswctDefault;
            
            int intString = pstrString.length();
            toploop : while (pintChar < intString) {
                /**
                 * Protect against endless loops
                 */
                if (pintChar == intLastChar && enmState.pos == enmLastState.pos) {
                    throw new ZXException("Unsupported state when tokenizing where clause at position " 
                                          + pintChar);
                }
                
                enmLastState = enmState;
                intState = enmState.pos;
                intLastChar = pintChar;
                
                strChar = pstrString.charAt(pintChar);
                
                if (intState == zXType.dsWhereConditionType.dswctDefault.pos) {
                    switch (strChar) {
                    case '\'':
                        /**
                         * Start of string with single quotes
                         */
                        enmState = zXType.dsWhereConditionType.dswctString;
                        blnSingle = true;
                        
                        pstrRawToken = strChar + "";
                        pintChar++;
                        break;

                    case '"':
                        /**
                         * Start of string with double quotes
                         */
                        enmState = zXType.dsWhereConditionType.dswctString;
                        blnSingle = true;
                        
                        pstrRawToken = strChar + "";
                        pintChar++;
                        break;
                        
                    case '#':
                        /**
                         * Start of date
                         */
                        enmState = zXType.dsWhereConditionType.dswctDate;
                        
                        pstrRawToken = strChar + "";
                        pintChar++;
                        break;
                        
                    case '0':  case '1':  case '2':  case '3':  case '4':
                    case '5':  case '6':  case '7':  case '8':  case '9': 
                    case '.':  case '-':
                        /**
                         * Start of number
                         */
                        enmState = zXType.dsWhereConditionType.dswctNumber;
                        strToken = strChar + "";
                        
                        pstrRawToken = strChar + "";
                        pintChar++;
                        break;
                        
                    case 'a':  case 'b':  case 'c':  case 'd':  case 'e':
                    case 'f':  case 'g':  case 'h':  case 'i':  case 'j':
                    case 'k':  case 'l':  case 'm':  case 'n':  case 'o':
                    case 'p':  case 'q':  case 'r':  case 's':  case 't':
                    case 'u':  case 'v':  case 'w':  case 'x':  case 'y':
                    case 'z':
                    case 'A':  case 'B':  case 'C':  case 'D':  case 'E':
                    case 'F':  case 'G':  case 'H':  case 'I':  case 'J':
                    case 'K':  case 'L':  case 'M':  case 'N':  case 'O':
                    case 'P':  case 'Q':  case 'R':  case 'S':  case 'T':
                    case 'U':  case 'V':  case 'W':  case 'X':  case 'Y':
                    case 'Z':
                    case '_' :
                        /**
                         * Start of id
                         */
                        enmState = zXType.dsWhereConditionType.dswctAttr;
                        strToken = strChar + "";
                        
                        pstrRawToken = strChar + "";
                        pintChar++;
                        break;
                        
                    case '&':  case '|':
                        /**
                         * Operators are single-character tokens
                         */
                        enmState = zXType.dsWhereConditionType.dswctOperator;
                        strToken = strChar + "";
                        pstrRawToken = strChar + "";
                        pintChar++;
                        
                        break toploop; // GoTo done
                    
                    case '(':  case ')':
                        /**
                         * Open / close parenthesis are single-character tokens
                         */
                        enmState = zXType.dsWhereConditionType.dswctNesting;
                        strToken = strChar + "";
                        
                        pstrRawToken = strChar + "";
                        pintChar++;
                        break toploop; // GoTo done
                        
                    case '>':  case '<':  case '=':  case '!':  case '%':
                        /**
                         * Start of operand
                         */
                        enmState = zXType.dsWhereConditionType.dswctOperand;
                        strToken = strChar + "";
                        
                        pstrRawToken = strChar + "";
                        pintChar++;
                        break;
                        
                    case ' ':  case '\t':  case '\n':  case '\r':
                        /**
                         * Skip white-space
                         */
                        pintChar++;
                        break;
                        
                    default:
                        throw new ZXException("Unexpected character at position " 
                                              + pintChar + "(" + strChar + ")");
                    }
                    
                } else if (intState == zXType.dsWhereConditionType.dswctString.pos) {
                    if (blnEscape) {
                        /**
                         * No special character meaning, take as-is
                         */
                        strToken = strToken + strChar;
                        blnEscape = false;
                        
                        pstrRawToken = pstrRawToken + strChar;
                        pintChar++;
                        
                    } else {
                        switch (strChar) {
                        case '"':
                            /**
                             * End of string when started with double quote
                             */
                            pstrRawToken = pstrRawToken + strChar;
                            pintChar++;
                            
                            if (blnDouble) break toploop; // GoTo done
                            break;

                        case '\'':
                            /**
                             * End of string when started with single quote
                             */
                            pstrRawToken = pstrRawToken + strChar;
                            pintChar++;
                            
                            if (blnSingle) break toploop; // GoTo done
                            
                            break;
                            
                        case '\\':
                            /**
                             * Next character is escaped
                             */
                            blnEscape = true;
                            
                            pstrRawToken = pstrRawToken + strChar;
                            pintChar++;
                            break;
                            
                        default:
                            strToken = strToken + strChar;
                            
                            pstrRawToken = pstrRawToken + strChar;
                            pintChar++;
                            break;
                        }
                        
                    } // In escape mode?
                    
                } else if (intState == zXType.dsWhereConditionType.dswctNumber.pos) {
                    switch (strChar) {
                    case '0':  case '1':  case '2':  case '3':  case '4':
                    case '5':  case '6':  case '7':  case '8':  case '9': 
                    case '.':
                        strToken = strToken + strChar;
                        
                        pstrRawToken = pstrRawToken + strChar;
                        pintChar++;
                        break;

                    default:
                        /**
                         * Must be end of token
                         */
                        break toploop;
                    }
                    
                } else if (intState == zXType.dsWhereConditionType.dswctAttr.pos) {
                    switch (strChar) {
                    case 'a':  case 'b':  case 'c':  case 'd':  case 'e':
                    case 'f':  case 'g':  case 'h':  case 'i':  case 'j':
                    case 'k':  case 'l':  case 'm':  case 'n':  case 'o':
                    case 'p':  case 'q':  case 'r':  case 's':  case 't':
                    case 'u':  case 'v':  case 'w':  case 'x':  case 'y':
                    case 'z':
                    case 'A':  case 'B':  case 'C':  case 'D':  case 'E':
                    case 'F':  case 'G':  case 'H':  case 'I':  case 'J':
                    case 'K':  case 'L':  case 'M':  case 'N':  case 'O':
                    case 'P':  case 'Q':  case 'R':  case 'S':  case 'T':
                    case 'U':  case 'V':  case 'W':  case 'X':  case 'Y':
                    case 'Z':
                    case '_' :  case '.' :  case '-' :
                        strToken = strToken + strChar;
                        
                        pstrRawToken = pstrRawToken + strChar;
                        pintChar++;
                        break;

                    default:
                        /**
                         * Must be end of token
                         */
                        break toploop;                        
                    }
                    
                } else if (intState == zXType.dsWhereConditionType.dswctDate.pos) {
                    switch (strChar) {
                    case '#':
                        /**
                         * End of date
                         */
                        pstrRawToken = pstrRawToken + strChar;
                        pintChar++;
                        break toploop; // GoTo done
                        
                    case ')':  case '|':  case '&':  case '¬':  case '\t':  
                    case '\n': case '\r':
                        /**
                         * Ending a token like this (instead of a #) means that this token was
                         * not a date but a special value instead (e.g. #date, #user or so) but
                         * the calling routine will find this out
                         */
                        break toploop; // GoTo done
                        
                    default:
                        /**
                         * Must be part of date, in the calling routine we will find out whether date was valid
                         * or not so not trying to be too clever at this stage
                         */
                        strToken = strToken + strChar;
                        
                        pstrRawToken = pstrRawToken + strChar;
                        pintChar++;
                        break;
                        
                    }
                    
                } else if (intState == zXType.dsWhereConditionType.dswctOperand.pos) {
                    switch (strChar) {
                    case '<':  case '>':  case '%':  case '=':  case '!':
                        strToken = strToken + strChar;
                        
                        pstrRawToken = pstrRawToken + strChar;
                        pintChar++;
                        break;

                    default:
                        break toploop; // GoTo done
                    }
                    
                } // State
                
            } // Over charcters
            
            //---- GOTO done.
            
            /**
             * Check some final details
             */
            if (enmState.pos == zXType.dsWhereConditionType.dswctString.pos) {
                if (strChar == '"') {
                    if (blnSingle) {
                        throw new ZXException("String token not closed correctly");
                    }
                    
                } else if (strChar == '\'') {
                    if (blnDouble) {
                        throw new ZXException("String token not closed correctly");
                    }
                    
                } else {
                    throw new ZXException("String token not closed correctly");
                }
                
            }
            
            penmTokenType = enmState;
            
            /**
             * Populate.
             */
            getNextToken.enmTokenType = penmTokenType;
            getNextToken.intChar = pintChar;
            getNextToken.strRawToken = pstrRawToken;
            getNextToken.strToken = strToken.trim();
            
            //---- GOTO done.
            
            return getNextToken;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get next token from a string with tokens, starting at position intChar,", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrString = "+ pstrString);
                getZx().log.error("Parameter : pintChar = "+ pintChar);
                getZx().log.error("Parameter : penmTokenType = "+ penmTokenType);
                getZx().log.error("Parameter : pstrRawToken = "+ pstrRawToken);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getNextToken;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getNextToken);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------------ Public helper methods.
    
    /**
     * Create a clause based on a single condition as BO.attr <op> value.
     * 
     * NOTE: This will replace existing statement.
     * 
     * @param pobjBO The business object. 
     * @param pobjAttr The attribute the single where condition is for. 
     * @param penmOperand The type of operand. 
     * @param pobjValue The value of the attribute. 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if singleWhereCondition fails.
     * @see #singleWhereCondition(ZXBO, Attribute, zXType.compareOperand, Property, zXType.dsWhereConditionOperator)
     */
    public zXType.rc singleWhereCondition(ZXBO pobjBO, 
                                           Attribute pobjAttr, 
                                           zXType.compareOperand penmOperand, 
                                           Property pobjValue) throws ZXException {
        return singleWhereCondition(pobjBO,pobjAttr,penmOperand,pobjValue, 
                                    zXType.dsWhereConditionOperator.dswcoNone);
    }
    
    /**
     * Create a clause based on a single condition as BO.attr <op> value.
     * 
     * V1.5:12 - BD9MAY05 - Fixed bug with EQ / NE null value
     * 
     * @param pobjBO The business object. 
     * @param pobjAttr The attribute the single where condition is for. 
     * @param penmOperand The type of operand. 
     * @param pobjValue The value of the attribute. 
     * @param penmOperator The operator. Optional, default should be dswcoNone.
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if singleWhereCondition fails. 
     */
    public zXType.rc singleWhereCondition(ZXBO pobjBO, 
                                          Attribute pobjAttr, 
                                          zXType.compareOperand penmOperand, 
                                          Property pobjValue, 
                                          zXType.dsWhereConditionOperator penmOperator) throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pobjAttr", pobjAttr);
            getZx().trace.traceParam("penmOperand", penmOperand);
            getZx().trace.traceParam("pobjValue", pobjValue);
            getZx().trace.traceParam("penmOperator", penmOperator);
        }
        
        zXType.rc singleWhereCondition = zXType.rc.rcOK;
        
        if (penmOperator == null) {
            penmOperator = zXType.dsWhereConditionOperator.dswcoNone;
        }
        
        try {
            String strRHSRawValue = null;
            zXType.dsWhereConditionType enmRHSType = null;
            String strRHSValue = null;

            /**
             * If an AND / OR operator is passed, do NOT re-initialise as we want to maintain the
             * tokens created so far
             */
            if (penmOperator.pos == zXType.dsWhereConditionOperator.dswcoNone.pos) {
                // Reset the values
                this.tokens = new ArrayList();
                setBaseBO(pobjBO);
            } else {
                // Set the current business object.
                setBaseBO(pobjBO);
            }
            
            if (pobjValue.isNull 
                && (penmOperand.pos == zXType.compareOperand.coEQ.pos || penmOperand.pos == zXType.compareOperand.coNE.pos)) {
                /**
                 * Special care for following scenarios:
                 * - attr is null
                 * - attr not is null
                 */
                strRHSValue = "null";
                strRHSRawValue = "#null";
                enmRHSType = zXType.dsWhereConditionType.dswctSpecial;
                
            } else {
                /**
                 * All other scenarios
                 */
                if (pobjValue instanceof StringProperty) {
                    /**
                     * Make a light-weight attempt to get to proper literal given the attribute type
                     */
                    int intDataType = pobjAttr.getDataType().pos;
                    
                    if (intDataType == zXType.dataType.dtLong.pos 
                        || intDataType == zXType.dataType.dtAutomatic.pos 
                        || intDataType == zXType.dataType.dtDouble.pos) {
                        if (StringUtil.isNumeric(pobjValue.getStringValue())) {
                            enmRHSType = zXType.dsWhereConditionType.dswctNumber;
                            strRHSRawValue = pobjValue.getStringValue();
                            strRHSValue = pobjValue.getStringValue();
                        }
                        
                    } else if (intDataType == zXType.dataType.dtDate.pos 
                            || intDataType == zXType.dataType.dtTime.pos 
                            || intDataType == zXType.dataType.dtTimestamp.pos) {
                    	/**
                    	 * The string could represent a Date/DateTime/Timestamp etc..
                    	 */
                    	DateFormat[] arrDateFormats = {getZx().getTimestampFormat(),
                    								   getZx().getDateFormat(),
                    								   getZx().getTimeFormat()};
                    	
                        if (DateUtil.isValid(arrDateFormats, pobjValue.getStringValue())) {
                            enmRHSType = zXType.dsWhereConditionType.dswctDate;
                            strRHSRawValue = makeRawValue(pobjValue.getStringValue(), enmRHSType);
                            strRHSValue = pobjValue.getStringValue();
                        }
                        
                    } else if (intDataType == zXType.dataType.dtBoolean.pos) { 
                        if (pobjValue.booleanValue()) {
                            enmRHSType = zXType.dsWhereConditionType.dswctSpecial;
                            strRHSRawValue = "#true";
                            strRHSValue = "true";
                        } else {
                            enmRHSType = zXType.dsWhereConditionType.dswctSpecial;
                            strRHSRawValue = "#false";
                            strRHSValue = "false";
                        }
                        
                    } else {
                        enmRHSType = zXType.dsWhereConditionType.dswctString;
                        strRHSRawValue = makeRawValue(pobjValue.getStringValue(), enmRHSType);
                        strRHSValue = pobjValue.getStringValue();
                        
                    } // Attribute datatype
                    
                } else if (pobjValue instanceof LongProperty || pobjValue instanceof DoubleProperty) {
                    enmRHSType = zXType.dsWhereConditionType.dswctNumber;
                    strRHSRawValue = pobjValue.getStringValue();
                    strRHSValue = pobjValue.getStringValue();
                    
                } else if (pobjValue instanceof DateProperty) {
                    enmRHSType = zXType.dsWhereConditionType.dswctDate;
                    strRHSRawValue = makeRawValue(pobjValue.getStringValue(), enmRHSType);
                    strRHSValue = pobjValue.getStringValue();
                    
                } else {
                    enmRHSType = zXType.dsWhereConditionType.dswctSpecial;
                    if (pobjValue.booleanValue()) {
                        strRHSRawValue = "#true";
                        strRHSValue = "true";
                    } else {
                        strRHSRawValue = "#false";
                        strRHSValue = "false";
                    }
                }
                
            } // Is null or not is null?
            
            if (addCondition(pobjBO, 
                             zXType.dsWhereConditionType.dswctAttr, 
                             pobjAttr.getName(), pobjAttr.getName(),
                             penmOperand,
                             enmRHSType, strRHSValue, strRHSRawValue,
                             penmOperator).pos != zXType.rc.rcOK.pos) {
                throw new ZXException("Unable to add condition for single where condition");
            }
            
            return singleWhereCondition;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Create a clause based on a single condition", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pobjAttr = "+ pobjAttr);
                getZx().log.error("Parameter : penmOperand = "+ penmOperand);
                getZx().log.error("Parameter : pobjValue = "+ pobjValue);
                getZx().log.error("Parameter : penmOperator = "+ penmOperator);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            singleWhereCondition = zXType.rc.rcError;
            return singleWhereCondition;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(singleWhereCondition);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Create a where clause that implements a quick search.
     *
     * @param pobjBO The business object. 
     * @param pstrValue QS value.
     * @param pstrGroup Optional group to include in QS (use QS as default) 
     * @return Returns the return code of the method. 
     * @throws ZXException Thrown if QSClause fails. 
     */
    public zXType.rc QSClause(ZXBO pobjBO, String pstrValue, String pstrGroup) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pobjBO", pobjBO);
            getZx().trace.traceParam("pstrValue", pstrValue);
            getZx().trace.traceParam("pstrGroup", pstrGroup);
        }
        
        zXType.rc QSClause = zXType.rc.rcOK;
        
        // Reset Values
        this.baseBO = pobjBO;
        this.tokens = new ArrayList();
        
        /**
         * Handle defaults.
         */
        if (pstrGroup == null) {
            pstrGroup = "QS";
        }
        
        try {
            Tuple objTag;                                       // Used to support soundex feature
            zXType.compareOperand enmOperand = null;            // Operand to use
            zXType.dsWhereConditionOperator enmOperator;        // Operator to use
            String strRHS = "";                                 // Value of RHS (LHS is always an attribute)
            zXType.dsWhereConditionType enmRHSType = null;      // Type of RHS
            
            if (StringUtil.len(pstrValue) == 0) return QSClause;
            
            AttributeCollection  colAttr = pobjBO.getDescriptor().getGroup(pstrGroup);
            if (colAttr == null) {
                throw new ZXException("Unable to find group", pstrGroup);
            }
            
            Attribute objAttr;
            Iterator iter = colAttr.iterator();
            while (iter.hasNext()) {
                objAttr = (Attribute)iter.next();
                
                if (StringUtil.len(objAttr.getColumn()) > 0) {
                    int intDataType = objAttr.getDataType().pos;
                    
                    if (intDataType == zXType.dataType.dtString.pos) {
                        /**
                         * BD3AUG04 If the user has entered a qs value that starts
                         * with an '=' than it means: simply take it at
                         * face value; no soundex stuff; no like stuff; simply
                         * as-is and equals
                         */
                        if (pstrValue.charAt(0) == '=') {
                            enmOperand = zXType.compareOperand.coEQ;
                            strRHS = pstrValue.substring(1);
                            enmRHSType = zXType.dsWhereConditionType.dswctString;
                            
                        } else {
                            /**
                             * DGS27FEB2004: If the attr has a soundex tag, convert the QS value to its
                             * soundex equivalent and compare equal to that (not like)
                             * DGS05MAR2004: Unless the soundex has trailing zeroes, in which case trim
                             * them off and compare the trimmed soundex 'like' the DB soundex. This is
                             * useful because often names end up shorter than the available positions,
                             * and using this 'SMYTH' will be like 'SMITH AND CO'.
                             * Use the new 'simplifiedSoundex' function for improved behaviour.
                             */
                            objTag = (Tuple)objAttr.getTags().get("zXSoundex");
                            if (objTag != null) {
                                /**
                                 * Remove zeroes from calculated soundex. Will only ever be trailing.
                                 */
                                String strSoundex = StringUtil.soundex(pstrValue);
                                String strSoundexTrimmed = StringUtil.removeAll(strSoundex,'0');
                                if (StringUtil.len(strSoundexTrimmed) < StringUtil.len(strSoundex)) {
                                    /**
                                     * Short calculated soundex - compare 'like'
                                     */
                                    enmOperand = zXType.compareOperand.coSW;
                                    strRHS = strSoundex;
                                    enmRHSType = zXType.dsWhereConditionType.dswctString;
                                    
                                } else {
                                    /**
                                     * Full size soundex - compare 'equals'
                                     */
                                    enmOperand = zXType.compareOperand.coEQ;
                                    strRHS = strSoundexTrimmed;
                                    enmRHSType = zXType.dsWhereConditionType.dswctString;
                                    
                                } // Use trimmed soundex or original soundex
                                
                            } else if (pobjBO.getDescriptor().getSize().pos == zXType.entitySize.esLarge.pos) {
                                /**
                                 * QS does not start with an '=' sign and no soundex; can mean either a
                                 * contains or a starts-with depending on the size of the entity
                                 */
                                enmOperand = zXType.compareOperand.coSW;
                                strRHS = pstrValue;
                                enmRHSType = zXType.dsWhereConditionType.dswctString;
                                
                            } else {
                                enmOperand = zXType.compareOperand.coCNT;
                                strRHS = pstrValue;
                                enmRHSType = zXType.dsWhereConditionType.dswctString;
                                
                            } // Soundex / large / small
                            
                        } // Did not start with =
                        
                    } else if (intDataType == zXType.dataType.dtAutomatic.pos 
                               || intDataType == zXType.dataType.dtLong.pos
                               || intDataType == zXType.dataType.dtDouble.pos) {
                        /**
                         * If not numeric, exclude this from QS
                         **/
                        if (StringUtil.isNumeric(pstrValue) || StringUtil.isDouble(pstrValue)) {
                            enmOperand = zXType.compareOperand.coEQ;
                            strRHS = pstrValue;
                            enmRHSType = zXType.dsWhereConditionType.dswctNumber;
                        }
                        
                    } else if (intDataType == zXType.dataType.dtDate.pos 
                               || intDataType == zXType.dataType.dtTime.pos
                               || intDataType == zXType.dataType.dtTimestamp.pos) {
                    	
                    	/**
                    	 * The string could represent a Date/DateTime/Timestamp etc..
                    	 */
                    	DateFormat[] arrDateFormats = {getZx().getTimestampFormat(), 
                    								   getZx().getDateFormat(),
                    								   getZx().getTimeFormat()};
                    	
                        /**
                         * If no date, exclude this from QS
                         **/
                		if (DateUtil.isValid(arrDateFormats, pstrValue)) {
                            enmOperand = zXType.compareOperand.coEQ;
                            strRHS = pstrValue;
                            enmRHSType = zXType.dsWhereConditionType.dswctDate;
                        }
                        
                    } else if (intDataType == zXType.dataType.dtBoolean.pos) {
                        /**
                         * Never attempt to include booleans in QS
                         */
                        
                    } else {
                        enmOperand = zXType.compareOperand.coEQ;
                        strRHS = pstrValue;
                        enmRHSType = zXType.dsWhereConditionType.dswctString;
                        
                    }
                    
                }
                
                /**
                 * It can be that we do not want to include this attribute in the final clause
                 * (eg for numeric attributes when the QS value is not numeric)
                 */
                if (StringUtil.len(strRHS) > 0) {
                    /**
                     * Or them all together (except for the first one)
                     */
                    if (this.tokens.size() == 0) {
                        enmOperator = zXType.dsWhereConditionOperator.dswcoNone;
                    } else {
                        enmOperator = zXType.dsWhereConditionOperator.dswcoOr;
                    }
                    
                    if (addCondition(pobjBO, 
                                     zXType.dsWhereConditionType.dswctAttr, objAttr.getName(), "",
                                     enmOperand,
                                     enmRHSType, strRHS, makeRawValue(strRHS, enmRHSType),
                                     enmOperator).pos != zXType.rc.rcOK.pos) {
                        throw new ZXException("Unable to construct single QS where condition");
                    }
                    
                    /**
                     * And ready for next
                     */
                    strRHS = "";
                    enmOperator = zXType.dsWhereConditionOperator.dswcoNone;
                    
                } // Need to include in final clause?
                
            }
            
            return QSClause;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Create a where clause that implements a quick search.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pobjBO = "+ pobjBO);
                getZx().log.error("Parameter : pstrValue = "+ pstrValue);
                getZx().log.error("Parameter : pstrGroup = "+ pstrGroup);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            QSClause = zXType.rc.rcError;
            return QSClause;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(QSClause);
                getZx().trace.exitMethod();
            }
        }
    }
    
    //------------------------ Construct the where clause for use.
    
    /**
     * Get where-clause including ':' if there is a where clause, return empty string otherwise
     * 
     * @return Returns where clause.
     */
    public String getAsCompleteWhereClause() {
        String getAsCompleteWhereClause = "";
        
        getAsCompleteWhereClause = getAsWhereClause();
        
        if (StringUtil.len(getAsCompleteWhereClause) > 0) {
            getAsCompleteWhereClause = ':' + getAsCompleteWhereClause;
        }
        
        return getAsCompleteWhereClause;
    }
    
    /**
     * Return as SQL where clause
     * @return Return as SQL where clause
     * @throws ZXException Thrown if getAsSQL fails.
     */
    public String getAsSQL() throws ZXException {
        StringBuffer getAsSQL = new StringBuffer();
        
        int intTokens = this.tokens.size();
        if (intTokens == 0) return getAsSQL.toString();
        
        DSWhereCondition objToken;
        for (int i = 0; i < intTokens; i++) {
            objToken = (DSWhereCondition)this.tokens.get(i);
            
            /**
             * Space out the previous where condition.
             */
            if (getAsSQL.length() > 0) {
                getAsSQL.append(' ');
            }
            getAsSQL.append(objToken.getAsSQLWhereCondition());
        }
        
        return getAsSQL.toString();
    }
    
    /**
     * Return current where clause in verbose format.
     * 
     * @return Return current where clause in verbose format.
     */
    public String getAsVerbose() {
        StringBuffer getAsVerbose = new StringBuffer();
        
        int intTokens = this.tokens.size();
        if (intTokens == 0) return getAsVerbose.toString();
        
        DSWhereCondition objToken;
        
        for (int i = 0; i < intTokens; i++) {
            objToken = (DSWhereCondition)this.tokens.get(i);
            
            /**
             * Space out the previous where condition.
             */
            if (getAsVerbose.length() > 0) {
                getAsVerbose.append(' ');
            }
            
            getAsVerbose.append(objToken.getAsVerbose());
        }
        
        return getAsVerbose.toString();
    }
    
    /**
     * Rebuild and return as where clause.
     * 
     * @return Returns rebuilt where clause.
     */
    public String getAsWhereClause() {
        StringBuffer getAsWhereClause = new StringBuffer();
        
        int intTokens = this.tokens.size();
        if (intTokens == 0) return getAsWhereClause.toString();
        
        DSWhereCondition objToken;
        
        for (int i = 0; i < intTokens; i++) {
            objToken = (DSWhereCondition)this.tokens.get(i);
            
            /**
             * Space out the previous where condition.
             */
            if (getAsWhereClause.length() > 0) {
                getAsWhereClause.append(' ');
            }
            
            getAsWhereClause.append(objToken.getAsWhereCondition());
        }
        
        return getAsWhereClause.toString();
    }
    
    //------------------------ Util Methods.
    
    /**
     * Wrap clause in extra parenthesis.
     * @return Returns the return code of the method.
     * 
     */
    private zXType.rc wrapClauseInParenthesis() {
        zXType.rc wrapClauseInParenthesis = zXType.rc.rcOK;
        int intTokens = this.tokens.size();
        
        if (intTokens == 0) return wrapClauseInParenthesis;
        
        /**
         * Add ( to first
         */
        DSWhereCondition objCondition = (DSWhereCondition)this.tokens.get(0);
        objCondition.setOpenNesting(objCondition.getOpenNesting() + 1);
        
        /**
         * And ) to last
         */
        objCondition = (DSWhereCondition)this.tokens.get(intTokens - 1);
        objCondition.setCloseNesting(objCondition.getCloseNesting() + 1);
        
        return wrapClauseInParenthesis;
    }
    
    /**
     * Turn a value into a raw value. A raw value is one
     * with quotes, '#' and escape characters (ie as
     * it would appear raw in a where group definition.
     * 
     * @param pstrValue The value.
     * @param penmType The where condition type.
     * @return Returns a raw version of the value.
     */
    private String makeRawValue(String pstrValue, zXType.dsWhereConditionType penmType) {
        String makeRawValue;
        
        if (penmType.pos == zXType.dsWhereConditionType.dswctString.pos) {
            makeRawValue = '\'' + StringUtil.encodezXString(pstrValue) + '\'';
            
        } else if (penmType.pos == zXType.dsWhereConditionType.dswctDate.pos) {
            makeRawValue = '#' + pstrValue + '#';
            
        } else {
            makeRawValue = pstrValue;
            
        }
        
        return makeRawValue;
    }
}