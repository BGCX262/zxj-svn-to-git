/*
 * Created on Mar 23, 2004 by Michael Brewer
 * $Id: Expression.java,v 1.1.2.18 2006/07/17 16:40:43 mike Exp $
 */
package org.zxframework.expression;

import java.util.ArrayList;
import java.util.Iterator;

import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.property.Property;
import org.zxframework.util.DateUtil;
import org.zxframework.util.StringUtil;

/**
 * A single, parsed expression.
 * 
 * <pre>
 * 
 * Change    : BD7APR03
 * Why       : Added parseMsg
 * 
 * Change    : BD23MAY03
 * Why       : Fixed bug with tokenise: would not parse a simple expression
 *              like 1 or 'test' correctly
 * 
 * Change    : BD4SEP03 
 * Why : Fixed bug: also allow digits in identifier names
 * 
 * Change    : BD26NOV03
 * Why       : Added support for liveDump (for better real-time tracing)
 * 
 * Change    : BD3DEC03
 * Why       : Added support for unCompress (ie nice-ify an expression)
 * 
 * Change    : BD18JUN04
 * Why       : Added liveDump for context variables
 * 
 * Change    : BD22JUN04
 * Why       : Allow hyphens ('-') in context variable names
 * 
 * Change    : BD15DEC04 - V1.4:7
 * Why       : - Complete review of go function so it is easier to add
 *             control keywords
 *             - Made uncompress less error prone
 *             - Improved error messages
 *             
 * Change    : BD17JAN05 - V1.4:20
 * Why       : - The review of 1.4:7 has allowed a bug that has been in for
 *             some time to roar its ugly head: a string inside an
 *             expression with the value '(', ',' or ')' could cause
 *             the expression to be mis-parsed; for example:
 *             concat('1', '2', ')', '4') would return 12 as ')' would
 *             be interpreted as the end of the parameter list
 *
 * Change    : BD23MAR05 - V1.4:40
 * Why       : Improved error handling when an expression cannot be parsed
 * 
 * Change    : BDS1MAY05 - V1.5:8
 * Why       : Fixed bug in liveDump: no support for double
 * 
 * Change    : BDS1MAY05 - V1.5:21
 * Why       : Fixed bug; expression handler is not a weak reference and thus
 *             caused memory lead (zX.expressionHandler was never released)
 *
 * Change    : BD2SEP05 - V1.5:47
 * Why       : An error in the expression parser causes a perfectly valid expression
 *             like '[value]' (ie refer to expression context value 'value') to be
 *             considered of incorrect syntax; same for a single date value
 *             
 * Change    : BD5DEC05 - V1.5:73
 * Why       : Added comments to expressions (all enclosed in { and })
 * 
 * </pre>
 * 	
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @version 0.01
 */
public class Expression extends ZXObject {

    //------------------------ Members
    
    private String expression;
    // Using arraylist for speed :
    private ArrayList tokens;
    
    private static final char[] END_OF_STR = new char[]{' ','\t','\n','\r'};
    /** Represents the end/beginning of the identifier */
    private static final char[] END_OF_TOKEN = new char[]{' ', '\t', '\n', '\r', '(', ')', ',', '{', '}'};
    
    //------------------------ Constructors
    
    /**
     * Default constructor
     */
    public Expression() {
        super();
    }
    
    //------------------------ Getters and Setters
    
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
    
    //------------------------ Public methods

    /**
     * Parse an expression.
     *
     * <pre>
     * 
     * Reviewed for V1.5:47
     * Reviewed for V1.5:73
     * </pre>
     * 
     * @param pstrExpression The expression to parse. 
     * @return Returns the return code of the parse method.
     */
    protected zXType.rc parse(String pstrExpression) throws ExpressionParseException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrExpression", pstrExpression);
        }
        
        zXType.rc parse = zXType.rc.rcOK; 
        
        try {
            /**
             * Save the expression in unspoiled form
             */
            this.expression = pstrExpression;
            
            /**
             * Ensure we start with empty token collection
             */
            setTokens(new ArrayList());
           
            /**
             * Start parsing
             */
            int intLength = StringUtil.len(pstrExpression);
            if (intLength == 0) return zXType.rc.rcError;
            
            int intNumTokens = 0;
            int intParenthesisLevel = 0;
            int enmState = zXType.exprParseState.epsStart.pos;
            
            /**
             * Create object for first token
             */
            ExprToken objToken = new ExprToken();
            String strToken = "";
            zXType.exprTokenType enmTokenType = zXType.exprTokenType.ettUnknown;
            
            char[] chars = pstrExpression.toCharArray();
            char strStringStart = ' ';

            int i = 0;
            // Should no longer test for epsError.
            while (i < intLength && enmState != zXType.exprParseState.epsError.pos) {
                char strChar  = chars[i];
                /**
                 * Start the parsing, first get the parse state :
                 */
                if (enmState == zXType.exprParseState.epsStart.pos) {
                    /**
                     * Add token_string to handle null strings
                     */
                    if (StringUtil.len(strToken) > 0 || enmTokenType.pos ==zXType.exprTokenType.ettString.pos) {
                        intNumTokens++;
                        
                        objToken.setToken(strToken);
                        objToken.setTokenType(enmTokenType);
                        objToken.setPosition(i - StringUtil.len(strToken));
                        objToken.setStartCharacter(strStringStart);
                        getTokens().add(objToken);
                        objToken = new ExprToken(); // Reset token
                    }
                    
                    /**
                     * Do char by char :
                     */
                    if ( StringUtil.equalsAnyOf(END_OF_STR, strChar)) {
                        /**
                         * Ignore white space in start state
                         */
                        strToken = "";
                        enmTokenType = zXType.exprTokenType.ettUnknown;
                        
                    } else if (strChar ==  '{') {
                    	/**
                    	 * Start of comment
                    	 */
                    	strToken = "";
                        enmTokenType = zXType.exprTokenType.ettComment;
                        enmState = zXType.exprParseState.epsComment.pos;
                    	
                    } else if (StringUtil.isNumeric(strChar) || strChar == '+' || strChar == '-') {
                        /**
                         * Start of integer (may be upgraded to double)
                         */
                        strToken = String.valueOf(strChar);
                        enmTokenType = zXType.exprTokenType.ettInteger;
                        enmState = zXType.exprParseState.epsInteger.pos;
                        
                    } else if (strChar ==  '#') {
                        /**
                         * Start of date, get the '#' of the token
                         */
                        strToken = "";
                        enmTokenType = zXType.exprTokenType.ettDate;
                        enmState = zXType.exprParseState.epsDate.pos;
                        
                    } else if (strChar == '"' || strChar == '\'') {
                        /**
                         * Start of literal string, get the " or ' of the token
                         */
                        strToken = "";
                        enmTokenType = zXType.exprTokenType.ettString;
                        enmState = zXType.exprParseState.epsString.pos;
                        
                        /**
                         * Save how we started the string
                         */
                        strStringStart = strChar;
                        
                    } else if (strChar == '[') {
                        /**
                         * Start of context
                         */
                        strToken = "";
                        enmTokenType = zXType.exprTokenType.ettContext;
                        enmState = zXType.exprParseState.epsContext.pos;
                        
                    } else if (StringUtil.isAlpha(strChar) || strChar == '_') {
                        /**
                         * Identifier, may be upgraded to external identifier
                         */
                        strToken = "" + strChar;
                        enmTokenType = zXType.exprTokenType.ettId;
                        enmState = zXType.exprParseState.epsId.pos;
                        
                    } else if (strChar == '(') {
                        /**
                         * Start of parm list
                         */
                        strToken = strChar + "";
                        enmTokenType = zXType.exprTokenType.ettStartParmList;
                        enmState = zXType.exprParseState.epsStart.pos;
                        intParenthesisLevel++;
                        
                    } else if (strChar == ')') {
                        /**
                         * End of parm list
                         */
                        strToken = strChar + "";
                        enmTokenType = zXType.exprTokenType.ettEndParmList;
                        enmState = zXType.exprParseState.epsStart.pos;
                        intParenthesisLevel--;
                        
                    } else if (strChar == ',') {
                        /**
                         * Parm list separator
                         */
                        strToken = "" + strChar;
                        enmTokenType = zXType.exprTokenType.ettNextParm;
                        enmState = zXType.exprParseState.epsStart.pos;
                        
                    } else {
                        throw new ExpressionParseException("Unexpected character for identifier '" + strChar + "'", strToken, i);                       
                    }
                    
                } else if (enmState == zXType.exprParseState.epsId.pos) {
                    
                    /**
                     * We are in an identifier
                     */
                    if (StringUtil.isAlphaNumeric(strChar) || strChar == '_') {
                        /**
                         * Next character of identifier
                         */
                        strToken = strToken + strChar;
                        
                    } else if (strChar == '.') {
                        /**
                         * a.b means an external identifier
                         */
                        strToken = strToken + strChar;
                        enmTokenType = zXType.exprTokenType.ettExternalId;
                        enmState = zXType.exprParseState.epsExternalId.pos;
                        
                    } else if (StringUtil.equalsAnyOf(END_OF_TOKEN, strChar)) {
                        /**
                         * Indicates the end of the identifier
                         */
                        enmState = zXType.exprParseState.epsStart.pos;
                        
                        /**
                         * Make sure that we go back one position so that we do not
                         * consume the character that triggered the end of the identifier
                         */
                        i--;
                        
                    } else {
                        throw new ExpressionParseException("Unexpected character for identifier '" + strChar + "'", strToken, i);
                    }
                    
                } else if (enmState == zXType.exprParseState.epsExternalId.pos) {
                    
                    /**
                     * We are in an external identifier
                     */
                    if (StringUtil.isAlphaNumeric(strChar)) {
                        /**
                         * Next character of identifier
                         */
                        strToken = strToken + strChar;
                        
                    } else if (StringUtil.equalsAnyOf(END_OF_TOKEN, strChar)) {
                        /**
                         * Indicates the end of the identifier
                         */
                        enmState = zXType.exprParseState.epsStart.pos;
                        
                        /**
                         * Make sure that we go back one position so that we do not
                         *  consume the character that triggered the end of the identifier
                         */
                        i--;
                        
                    } else {
                        throw new ExpressionParseException("Unexpected character for external identifier '" + strChar + "'", strToken, i);
                    }
                    
                } else if (enmState == zXType.exprParseState.epsDate.pos) {
                    /**
                     * We are in a date.
                     * 
                     * We support #24Dec2004# or #23/12/2004# formats.
                     */
                    if (StringUtil.isAlphaNumeric(strChar) || strChar == '/') {
                        strToken = strToken + strChar;
                        
                    } else  if (strChar == '#'){
                        enmState = zXType.exprParseState.epsDateClose.pos;
                        
                    } else {
                        throw new ExpressionParseException("Unexpected character for date token '" + strChar + "'", strToken, i);
                    }
                    
                } else if (enmState == zXType.exprParseState.epsDateClose.pos) {
                    /**
                     * Parsing the end of a date : 
                     */
                    if (StringUtil.equalsAnyOf(END_OF_TOKEN, strChar)) {
                        /**
                         * End of date, lets see if we managed to get a date out of it.
                         * 
                         * NOTE : The dateform has to match the dateformat set in the zx config file.
                         */
                        if (DateUtil.isValid(getZx().getDateFormat(), strToken)) { 
                            enmState = zXType.exprParseState.epsStart.pos;
                            
                        } else {
                            /**
                             * Not a valid date
                             */
                            throw new ExpressionParseException("Token cannot be interpreted as a date", strToken, i);
                        }
                        
                        i = i - 1;
                        
                    } else {
                        throw new ExpressionParseException("Unexpected character found '" + strChar + "'", strToken, i);
                    }
                    
                } else if (enmState == zXType.exprParseState.epsContext.pos) {
                    /**
                     * We are parsing a context identifier
                     */
                    if (StringUtil.isAlphaNumeric(strChar) || strChar =='-' || strChar == '_') {
                        strToken = strToken + strChar;
                        
                    } else if (strChar == ']') {
                        enmState = zXType.exprParseState.epsContextClose.pos;
                        
                    } else {
                        throw new ExpressionParseException("Unexpected character for context variable '" + strChar + "'", strToken, i);
                    }
                    
                } else if (enmState == zXType.exprParseState.epsContextClose.pos) {
                    /**
                     * We have found the context closing character
                     */
                    if (StringUtil.equalsAnyOf(END_OF_TOKEN, strChar)) {
                        enmState = zXType.exprParseState.epsStart.pos;
                        i--;
                    } else {
                        throw new ExpressionParseException("Unexpected character for context variable '" + strChar + "'", strToken, i);
                    }
                    
                } else if (enmState == zXType.exprParseState.epsComment.pos) {
                    /**
                     * We are parsing a comment
                     */
                	if (strChar == '}') {
                		enmState = zXType.exprParseState.epsStart.pos;
                		
                	} else {
                		strToken = strToken + strChar;
                		
                	}
                	
                } else if (enmState == zXType.exprParseState.epsInteger.pos) {
                    /**
                     * We are parsing an integer
                     */
                    if (StringUtil.isNumeric(strChar)) {
                        strToken = strToken + strChar;
                        
                    } else if (StringUtil.equalsAnyOf(END_OF_TOKEN, strChar)){
                        /**
                         * End of integer found
                         */
                        if (StringUtil.isNumeric(strToken)) {
                            enmState = zXType.exprParseState.epsStart.pos;
                        } else {
                            /**
                             * Not a value integar
                             */
                            throw new ExpressionParseException("Token cannot be interpreted as a number", strToken, i);
                        }
                        i--;
                        
                    } else if (strChar == '.') {
                        /**
                         * A dot moves it from an integer to a double
                         */
                        strToken = strToken + strChar;
                        enmState = zXType.exprParseState.epsDouble.pos;
                        enmTokenType = zXType.exprTokenType.ettDouble;
                        
                    } else {
                        throw new ExpressionParseException("Unexpected character for number '" + strChar + "'", strToken, i);
                    }
                    
                } else if (enmState == zXType.exprParseState.epsDouble.pos) {
                    /**
                     * We are parsing an integer
                     */
                    if (StringUtil.isNumeric(strChar)) {
                        strToken = strToken + strChar;
                        
                    } else if (StringUtil.equalsAnyOf(END_OF_TOKEN, strChar)){
                        /**
                         * End of double found
                         */
                        if (StringUtil.isDouble(strToken)) {
                            enmState = zXType.exprParseState.epsStart.pos;
                        } else {
                            /**
                             * Not a value integar
                             */
                            throw new ExpressionParseException("Token cannot be interpreted as a double", strToken, i);
                        }
                        i--;
                        
                    } else {
                        throw new ExpressionParseException("Unexpected character for number '" + strChar + "'", strToken, i);
                    }
                                        
                } else if (enmState == zXType.exprParseState.epsString.pos) {
                    /**
                     * We are in a String
                     */
                    if (strChar == strStringStart) {
                        enmState = zXType.exprParseState.epsStringClose.pos;
                        
                    } else if (strChar == '\\') {
                        enmState = zXType.exprParseState.epsStringEscape.pos;
                        
                    } else {
                        strToken = strToken + strChar;
                    }
                    
                } else if (enmState == zXType.exprParseState.epsStringClose.pos) {
                    /**
                     * End of String
                     */
                    if (StringUtil.equalsAnyOf(END_OF_TOKEN, strChar)) {
                        enmState = zXType.exprParseState.epsStart.pos;
                        i--;
                    } else {
                        throw new ExpressionParseException("Unexpected character for a string '" + strChar + "'", strToken, i);
                    }
                    
                } else if (enmState == zXType.exprParseState.epsStringEscape.pos) {
                    /**
                     * We have found an \ in a string, indicating that the next
                     * character needs to be taken literaly
                     */
                    strToken = strToken + strChar;
                    enmState = zXType.exprParseState.epsString.pos;
                    
                }
                
                i++;
            }
            
            // Should never be true if we thrown ExpressionParseException instead of setting the enmState to zXType.exprParseState.epsError.
            if (enmState != zXType.exprParseState.epsError.pos) {
                
                if (intParenthesisLevel != 0) {
	                parse = zXType.rc.rcError;
	                intNumTokens = 0;
	                
	                throw new ExpressionParseException("Unbalanced parenthesis / quote / hash or bracket. Last token parsed '" + strToken + "', position : " + i, strToken, i);
                }
                
                if (enmState != zXType.exprParseState.epsDone.pos && 
                    enmState != zXType.exprParseState.epsStart.pos &&
                    enmState != zXType.exprParseState.epsInteger.pos &&
                    enmState != zXType.exprParseState.epsContextClose.pos &&
                    enmState != zXType.exprParseState.epsDateClose.pos &&
                    enmState != zXType.exprParseState.epsCommentClose.pos &&
                    enmState != zXType.exprParseState.epsStringClose.pos) {
                    
                    parse = zXType.rc.rcError;
                    intNumTokens = 0;
                    
                    throw new ExpressionParseException("Invalid state at end of expression. Last token parsed '" + strToken + "', position : " + i, strToken, i);
                }
                
                /**
                 * Handle the very last token that was in progress
                 */
                if (StringUtil.len(strToken) > 0 || enmTokenType.pos == zXType.exprTokenType.ettString.pos) {
                    intNumTokens++;
                    
                    objToken.setToken(strToken);
                    objToken.setTokenType(enmTokenType);
                    objToken.setPosition(i - StringUtil.len(strToken));
                    objToken.setStartCharacter(strStringStart);
                    
                    getTokens().add(objToken);
                }
                parse = zXType.rc.rcOK;
            }
            
            return parse;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(parse);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Dump the expression.
     * 
     * Dump expression from token collection just to show how the thing parsed
     *
     * @return Returns a string of the expression 
     */
    public String dump() {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }
        
        StringBuffer dump = new StringBuffer();
        
        try {
            
            ExprToken objToken;
            
            if (getTokens() != null) {
                Iterator iter = getTokens().iterator();
                while (iter.hasNext()) {
                   objToken = (ExprToken)iter.next();
                   
                   dump.append("- " + objToken.getToken());
                }
            }
            
            return dump.toString();
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(dump);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Dump expression from token collection just to show how the thing parsed. 
     * 
     * <pre>
     * 
     * Much like dump but now with function results
     * </pre>
     *
     * @return Returns a dumpof the expression
     * @throws ZXException Thrown if liveDump fails. 
     */
    public String liveDump() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        StringBuffer liveDump = new StringBuffer(64); 
        
        try {
            ExprToken objToken;
            
            Iterator iter = getTokens().iterator();
            while(iter.hasNext()) {
                objToken = (ExprToken)iter.next();
                
                if (liveDump.length() > 0) liveDump.append(" ");
                
                if (objToken.getTokenType().pos  == zXType.exprTokenType.ettContext.pos) {
                    Property objProperty = (Property)getZx().getExpressionHandler().getContext().get(objToken.getToken());
                    if (objProperty != null) {
                        liveDump.append(" {=").append(objProperty.getStringValue()).append("} [").append(objToken.getToken()).append("]");
                        
                    } else {
                        liveDump.append(" {null}[").append(objToken.getToken()).append("]");
                    }
                    
                } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettDate.pos) {
                    liveDump.append("#").append(objToken.getToken()).append("#");
                    
                } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettComment.pos) {
                    liveDump.append("{").append(objToken.getToken()).append("}");
                    
                } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettEndParmList.pos) {
                    liveDump.append(")");
                    
                } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettExternalId.pos) {
                    liveDump.append(objToken.getToken());
                    
                } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettId.pos) {
                    liveDump.append(objToken.getToken());
                    
                } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettInteger.pos) {
                    liveDump.append(objToken.getToken());
                    
                } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettNextParm.pos) {
                    liveDump.append(",");
                    
                } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettStartParmList.pos) {
                    liveDump.append("(");
                    
                } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettString.pos) {
                    liveDump.append("'").append(objToken.getToken()).append("'");
                    
                }
                
                if (StringUtil.len(objToken.getFunctionResult()) > 0) {
                    liveDump.append(" {=").append(objToken.getFunctionResult()).append("}");
                }
                
            }
            
            return liveDump.toString();
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Dump expression from token collection just to show how the thing parsed.", e);
            
            if (getZx().throwException) throw new ZXException(e);
            return liveDump.toString();
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(liveDump);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Adhoc data class.
     * 
     * Why? Coz primitives are always passed by value and not by ref.
     */
    class UnCompress {
    	String strValue;
    	int intValue;
    	
    	UnCompress(String pstrValue, int pintValue) {
    		this.strValue = pstrValue;
    		this.intValue = pintValue;
    	}
    	
    	int getIntValue() {
			return intValue;
		}
    	void setIntValue(int i) {
			this.intValue = i;
		}
    	String getStrValue() {
			return strValue;
		}
    	void setStrValue(String str) {
			this.strValue = str;
		}
    }
    
    /**
     * Uncompress me .
     * 
     * <pre>
     * 
     * i.e. print in readable form
     * 
     * NOTE: This calls : unCompress(null, null, 1, 1);
     *</pre>
     *
     * @return Returns a readable form of the expression
     * @throws ZXException  Thrown if unCompress fails. 
     */
	public String unCompress() throws ZXException {
	    return unCompress(null, null, 0, 1).getStrValue();
	}
    
    /**
     * Uncompress me .
     * 
     * <pre>
     * 
     * i.e. print in readable form
     * 
     * NOTE: This calls : unCompress(pstrNewLine, pstrIdent, 0, 1)
     *</pre>
     *
     * @param pstrNewLine Character to use for newline (default vbCrLf but use <BR> for use in HTML)
     * @param pstrIdent Character to use for identation (default vbTab but use &nbsp; for use in HTML) 
     * @return Returns a readable form of the expression
     * @throws ZXException  Thrown if unCompress fails. 
     */
    public String unCompress(String pstrNewLine, String pstrIdent) throws ZXException {
        return unCompress(pstrNewLine, pstrIdent, 0, 1).getStrValue();
    }
    
    /**
     * Uncompress me .
     * 
     *<pre>
     * 
     * i.e. print in readable form
     * 
     * Reviewed for V1.5:73
     *</pre>
     *
     * @param pstrNewLine Character to use for newline (default vbCrLf but use <BR> for use in HTML)
     * @param pstrIdent Character to use for identation (default vbTab but use &nbsp; for use in HTML) 
     * @param pintToken The number of the token where to start (for recursive calls). Optional, default should be 1. Remember this is 0 based. 
     * @param pintLevel The number of recursive calls . Optional, default should be 1.
     * @return Returns a readable form of the expression and the current token.
     * @throws ZXException  Thrown if unCompress fails. 
     */
    private UnCompress unCompress(String pstrNewLine, String pstrIdent, int pintToken, int pintLevel) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrNewLine", pstrNewLine);
            getZx().trace.traceParam("pstrIdent", pstrIdent);
            getZx().trace.traceParam("pintToken", pintToken);
            getZx().trace.traceParam("pintLevel", pintLevel);
        }

        UnCompress unCompress = new UnCompress("", pintToken); 
        
        /**
         * Defaults 
         */
        if (pstrNewLine == null) {
            pstrNewLine = "\n";
        }
        
        if (pstrIdent == null) {
            pstrIdent = "\t";
        }
        
        try {
        	String strReturn = "";
        	String strTmp;
        	ArrayList colTokens = getTokens();
        	
        	/**
        	 * Could be that there are some starting comments
        	 */
        	if (pintLevel == 1) {
        		for (;pintToken < colTokens.size(); pintToken++) {
					if (getZx().getExpressionHandler().peekTokenType(colTokens, pintToken).pos == zXType.exprTokenType.ettComment.pos) {
						strReturn = strReturn + "{" + getZx().getExpressionHandler().peekToken(colTokens, pintToken) + "}";
					} else {
						break;
					}
				}
        	}
        	
        	/**
        	 * Add space if we had added a comment
        	 */
        	if (StringUtil.len(strReturn) > 0) {
        		strReturn = strReturn + " ";
        	}
        	
            /**
             * Get handle to token
             **/
            ExprToken objToken = (ExprToken)colTokens.get(pintToken);
            
        	/**
        	 *The easy ones: literals or context entries 
        	 */
            if (objToken.getTokenType().pos == zXType.exprTokenType.ettDate.pos) {
                strReturn = strReturn + "#" + objToken.getToken() + "#";
            
            } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettContext.pos) {
                strReturn = strReturn + "[" + objToken.getToken() + "]";
                            
            } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettDouble.pos 
            		  || objToken.getTokenType().pos == zXType.exprTokenType.ettInteger.pos) {
                strReturn = strReturn + objToken.getToken();
                
            } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettComment.pos) {
                strReturn = strReturn + "{" + objToken.getToken() + "}";
                
            } else if (objToken.getTokenType().pos == zXType.exprTokenType.ettString.pos) {
                /**
                 * Escape any quotes
                 **/
                if (objToken.getStartCharacter() == '"') {
                    strReturn = strReturn + objToken.getStartCharacter() + StringUtil.replaceAll(objToken.getToken(), "\"", "\\\"") + objToken.getStartCharacter();
                } else {
                    strReturn = strReturn + objToken.getStartCharacter() + StringUtil.replaceAll(objToken.getToken(), "'", "\\'") + objToken.getStartCharacter();
                }
                    
            } else if (objToken.getTokenType().pos ==zXType.exprTokenType.ettId.pos 
            		   || objToken.getTokenType().pos == zXType.exprTokenType.ettExternalId.pos) {
                /**
                 * Function call
                 **/
                if (getZx().getExpressionHandler().expectTokenType(zXType.exprTokenType.ettStartParmList, colTokens, pintToken + 1).pos == zXType.rc.rcError.pos) {
                    /**
                     * Error: No start of parameter list found
                     **/
                	// GoTo errExit
                    return null;
                }
                
            	/**
            	 * Start with functionCall (
            	 */
            	strReturn = strReturn + objToken.getToken() + "(";
            	
            	/**
            	 * Loop over parameters to function
            	 */
                pintToken = pintToken + 2;
                int intNumArg = 0;
                
                while (getZx().getExpressionHandler().peekTokenType(colTokens, pintToken).pos != zXType.exprTokenType.ettEndParmList.pos
                	   && pintToken <= colTokens.size()) {
                	intNumArg++;
                	
                	int enmEprTokenType = getZx().getExpressionHandler().peekTokenType(colTokens, pintToken).pos;
                	if (enmEprTokenType == zXType.exprTokenType.ettComment.pos) {
                		if (intNumArg == 1) {
                        	strReturn = strReturn + pstrNewLine;
                        	
                            if (StringUtil.len(pstrIdent) > 0) {
                                strReturn = strReturn + StringUtil.padString(pintLevel, pstrIdent);
                            }
                		}
                        
                        strReturn = strReturn + " {" + getZx().getExpressionHandler().peekToken(colTokens, pintToken) + "} ";
                        
                	} else if (enmEprTokenType == zXType.exprTokenType.ettNextParm.pos) {
                    	strReturn = strReturn + pstrNewLine;
                    	
                        if (StringUtil.len(pstrIdent) > 0) {
                            strReturn = strReturn + StringUtil.padString(pintLevel, pstrIdent);
                        }
                        
                        strReturn = strReturn + ",";
                        
                	} else {
                		if (intNumArg == 1) {
                        	strReturn = strReturn + pstrNewLine;
                        	
                            if (StringUtil.len(pstrIdent) > 0) {
                                strReturn = strReturn + StringUtil.padString(pintLevel, pstrIdent);
                            }
                		}
                		
                        UnCompress strI = unCompress(pstrNewLine, 
                        							 pstrIdent, 
                        							 pintToken, 
                        							 pintLevel + 1);
                        strTmp = strI.getStrValue();
                        pintToken = strI.getIntValue();
                        
                        /**
                         * Actual parameter
                         */
                        strReturn = strReturn + strTmp;
                        
                	}
                	
                    /**
                     * Advance pointer to next token
                     **/
                    pintToken = pintToken + 1;
                }
                
                /**
                 * End of function call.
                 */
                if (intNumArg > 0) {
                    if (StringUtil.len(pstrIdent) > 0) {
                        strReturn = strReturn + pstrNewLine + StringUtil.padString(pintLevel - 1, pstrIdent) + ")";
                    } else {
                        strReturn = strReturn + pstrNewLine + ")";
                    }
                } else {
                	strReturn = strReturn + ")";
                }
                
            } // Found expression list
            
            /**
             * Could be that there are some trailing comments
             */
        	if (pintLevel == 1) {
        		for (pintToken++;pintToken < colTokens.size(); pintToken++) {
					if (getZx().getExpressionHandler().peekTokenType(colTokens, pintToken).pos == zXType.exprTokenType.ettComment.pos) {
						strReturn = strReturn + "{" + getZx().getExpressionHandler().peekToken(colTokens, pintToken) + "}";
					} else {
						break;
					}
				}
        	}
        	
            /**
             * Set the outgoing values.
             */
            unCompress.setStrValue(strReturn);
            unCompress.setIntValue(pintToken);
            
            return unCompress;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Uncompress me", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrNewLine = "+ pstrNewLine);
                getZx().log.error("Parameter : pstrIdent = "+ pstrIdent);
                getZx().log.error("Parameter : pintToken = "+ pintToken);
                getZx().log.error("Parameter : pintLevel = "+ pintLevel);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return unCompress;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(unCompress);
                getZx().trace.exitMethod();
            }
        }
    }
}