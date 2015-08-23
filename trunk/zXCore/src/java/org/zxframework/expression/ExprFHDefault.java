/*
 * Created on Mar 28, 2004 by Michael Brewer
 * $Id: ExprFHDefault.java,v 1.1.2.20 2006/07/17 16:40:43 mike Exp $
 */
package org.zxframework.expression;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.zxframework.UserProfile;
import org.zxframework.ZXException;
import org.zxframework.zXType;
import org.zxframework.property.BooleanProperty;
import org.zxframework.property.DateProperty;
import org.zxframework.property.DoubleProperty;
import org.zxframework.property.LongProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.DateUtil;
import org.zxframework.util.PasswordService;
import org.zxframework.util.StringUtil;

/**
 * Default function handler for expression handler.
 * 
 * <pre>
 * 
 * Change    : BD27MAR03
 * Why       : Added haveLock and inGroup functions
 * 
 * Change    : BD1APR03
 * Why       : Fixed stupid bug in ifnull function where I swapped
 *             the 1st and 2nd argument
 * 
 * Change    : BD1APR03
 *             - Today() should return a proper date and not include any
 *               time elements
 *             - Added now function that does what today did
 * 
 * Change    : BD29APR03
 * Why       : Added user function
 * 
 * Change    : 5MAY03
 * Why       : Fixed bug in choose function
 * 
 * Change    : 23MAY03
 * Why       : Added eval and in functions
 * 
 * Change    : 5JUN03
 * Why       : Added userPref function
 * 
 * Change    : 15jun03
 * Why       : Between should be >= <= instead of > <
 * 
 * Change    : 27JUN03
 * Why       : Added replace function
 * 
 * Change    : BD4SEP03
 * Why       : Added script function
 * 
 * Change    : BD30OCT
 * Why       : Fixed bug in allOr function (ws unchanged copy of allAnd)
 * 
 * Change    : BD26NOV03
 * Why       : - Fixed bug with in function in describe mode
 *             - And one in eval mode (static must be false)
 * 
 * Change    : BD29NOV03
 * Why       : Fixed bug with in function choose describe mode
 * 
 * Change    : BD6JAN04
 * Why       : Added translate function
 * 
 * Change    : BD20JAN04
 * Why       : Added systemMsg function
 * 
 * Change    : BD22JAN04
 * Why       : Added alias property
 * 
 * Change    : BD16FEB04
 * Why       : Fixed bug in QS (no static property) and set null if empty string
 * 
 * Change    : BD22FEB04
 * Why       : Added msg function
 * 
 * Change    : BD25FEB04
 * Why       : And fixed the recently added msg function (sorry)
 * 
 * Change    : BD4MAR04
 * Why       : Fix terrible bug with qs function (used static....)
 *   
 * Change    : BD13MAR04
 * Why       : Long overdue: improved the verbose of many functions
 * 
 * Change    : BD30MAR04
 * Why       : In the Translate function, return a null property if none matches.
 * 
 * Change    : BD29APR04
 * Why       : Added select function
 * 
 * Change    : DS04MAY04
 * Why       : Added back in the changes made on 30MAR that were regressed out by the above.
 *              Also minor tweak to select function.
 * 
 * Change    : BD28MAY04
 * Why       : Added director function and changed main case statement
 *                for better performance
 * 
 * Change    : BD2JUN04
 * Why       : Added null() function
 * 
 * Change    : BD4JUN04
 * Why       : Added div() function with 3 parameters (3rd is default if dividor is 0)
 * 
 * Change    : DGS15JUN2004
 * Why       : Added new function instr
 * 
 * Change    : BD22JUN04
 * Why       : Fixed small things with instr (returns long, no string)
 * 
 * Change    : BD24JUN04
 * Why       : Added cr, lf, crLf, tab functions
 * 
 * Change    : BD9JUL04
 * Why       : Added summarize function
 * 
 * Change    : BD10JUL04
 * Why       : Added grep and cgrep functions
 * 
 * Change    : DGS21JUL2004
 * Why       : Added instrrev function
 * 
 * Change    : BD22JUL2004
 * Why       : Added context function
 * 
 * Change    : BD31JUL04
 * Why       : Added stripChars and stripNonChars functions
 * 
 * Change    : DGS05AUG2004
 * Why       : Added config function
 * 
 * Change    : BD20AUG04
 * Why       : Fixed problem with from function: now allow 0 as starting point
 * 
 * Change    : BD6SEP04
 * Why       : Post live release - ? - Added getLine function
 * 
 * Change    : BD15SEP04
 * Why       : Made error message better when we get to the else that
 *             indicates that we have not been able to handle a
 *             function. Can now be either because we did not have
 *             the right number of parameters or because we really
 *             never heard of this function
 * 
 * Change    : DGS21OCT2004
 * Why       : Error in 'supports' and typo in 'go' for 'stripnonchars'
 * 
 * Change    : BD27OCT04
 * Why       : Added chr() function
 * 
 * Change    : BD17JAN05 / V1.4:18
 * Why       : Added the following functions:
 *             - hoursDiff / minutesDiff / secondsDiff
 *             - ascii
 *             - getWord
 *              
 * Change    : BD17JAN05 / V1.4:37
 * Why       : Fixed problems with stripChars / stripNonChars
 *              
 * Change    : DGS02FEB2005
 * Why       : Very minor,  but in 'go' the 'describe' for 'replace' was slightly wrong.
 * 
 * Change    : BD24MAR05
 * Why       : Support for iterators and getExprContext, also fixed some minor
 *             typos in describe
 * 
 * Change    : BD1MAY05 - V1.5:9
 * Why       : In IF function, if 2nd argument is a long and 3rd argument if a double,
 *             consider the result to be double
 * 
 * Change    : BD29JUN05 - V1.5:22
 * Why       : In between function for doubles did not work
 * 
 * Change    : BD15SEP05 - V1.5:56
 * Why       : Fixed bug in makeDate function
 * 
 * Change    : BD15SEP05 - V1.5:69
 * Why       : Added soundex function
 * 
 * Change    : BD30APR06 - V1.5:95
 * Why       : Added addMinute function
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ExprFHDefault extends ExprFH {

    //------------------------ Constructors
    
    /**
     * Default constructor.
     */
    public ExprFHDefault() {
        super();
    }
    
    //------------------------ Inner classes :
    
    //------------------------ All functions with no parameters : 
    
    /**
     * null - Simply return null value.
     * 
     * <pre>
     * 
     * Usage : 
     * <code>null()</code>
     * </pre>
     */
    public static class Null extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Simply return null value", "P", "0", "-"};
        /** Default constructor. */
        public Null() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkNoArg(pcolArgs);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty("", true);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("null");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS], false);
            }
            
            return exec;
        }
    }
    
    /**
     * cr - Returns carraige return literal
     */
    public static class cr extends ExpressionFunction {
        
        protected int type;
        
        protected static final int TYPE_CR = 0;
        protected static final int TYPE_LF = 1;
        protected static final int TYPE_CRLF = 2;
        protected static final int TYPE_TAB = 3;
        
        /** Discribes the expression */
        public static final String[] describe = { "Sort of literals", "P", "0", "-"};
        /** Default constructor. */
        public cr() { 
            super(); 
            this.type = TYPE_CR;
        }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkNoArg(pcolArgs);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                String strType;
                switch (this.type) {
                    case TYPE_CR:
                        strType = "\r";
                        break;
                        
                    case TYPE_CRLF:
                        strType = "\r\n";
                        break;
                        
                    case TYPE_LF:
                        strType = "\n";
                        break;
                        
                    case TYPE_TAB:
                        strType = "\t";
                        break;
                        
                    default:
                        strType = "\n";
                        break;
                }
                exec = new StringProperty(strType, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     *lf - line feed literal
     */
    public static class lf extends cr {
        /** Default constructor. */
        public lf() { 
            super(); 
            this.type = TYPE_LF;
        }
    }
    
    /**
     *crlf - carraige return + line feed Sort of literals
     */
    public static class crlf extends cr {
        /** Default constructor. */
        public crlf() { 
            super(); 
            this.type = TYPE_CRLF;
        }
    }

    /**
     *tab - Tab literal
     */
    public static class tab extends cr {
        /** Default constructor. */
        public tab() { 
            super(); 
            this.type = TYPE_CRLF;
        }
    }
    
    /**
     * now - Returns the application date in the configuration file or the date 
     * when the zx server was started.
     * 
     * <pre>
     * 
     * Usage : 
     * <code>now()</code>
     * </pre>
     * 
     */
    public static class now extends ExpressionFunction {
		/** Discribes the expression */
		public static final String[] describe = {"Get the application date", "D", "0", "-"};
		/** Default constructor. */
		public now() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkNoArg(pcolArgs);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new DateProperty(getZx().getAppDate(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * systemmsg - Returns the system message. This is the currect stack trace of the application.
     * 
     * <pre>
     * 
     * Usage : 
     * <code>systemmsg()</code>
     * </pre>
     */
    public static class systemmsg extends ExpressionFunction {
		/** Discribes the expression */
        public static final String[] describe = {"Get the system message", "S", "0", "-"};
        /** Default constructor */
        public systemmsg() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkNoArg(pcolArgs);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                String strTmp = getZx().trace.formatStack(false);
                exec = new StringProperty(strTmp,StringUtil.len(strTmp) >0 ? false : true);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * now - Returns the application date in the configuration file or the date 
     * when the zx server was started.
     * 
     * <pre>
     * 
     * Usage : 
     * <code>today()</code>
     * </pre>
     * 
     */
    public static class today extends ExpressionFunction {
		/** Discribes the expression */
		public static final String[] describe = {"Get the application date", "D", "0", "-"};
        /** Default constructor. */
        public today() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            checkNoArg(pcolArgs);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new DateProperty(getZx().getAppDate(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * True - Returns a true boolean property.
     * 
     * <pre>
     * 
     * Usage : 
     * <code>True()</code>
     * </pre>
     */
    public static class True extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns a true boolean property", "B", "0", "-"};
        /** Default constructor. */
        public True() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkNoArg(pcolArgs);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(true, false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * False - Returns a true boolean property.
     * 
     * <pre>
     * 
     * Usage : 
     *  false()
     * </pre> 
     */
    public static class False extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns a false boolean property", "B", "0", "-"};
        /** Default constructor. */
        public False() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkNoArg(pcolArgs);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(false, false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * random - Returns a random generated long value.
     * 
     * <pre>
     * 
     * Usage : 
     * <code>random()</code>
     * </pre> 
     */
    public static class random extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns a random generated long value", "D", "0", "-"};
        /** Default constructor. */
        public random() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkNoArg(pcolArgs);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new DoubleProperty(Math.random(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * user - Get the current user's user id.
     * 
     * <pre>
     * 
     * Usage : 
     *  user()
     * </pre> 
     */
    public static class user extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the current user's user id", "P", "0", "-"};
        /** Default constructor. */
        public user() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkNoArg(pcolArgs);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = getZx().getUserProfile().getValue("id");
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    //------------------------ All functions with 1 parameters : 
    
    /**
     * resetiterator - Reset an iterator
     * 
     * <pre>
     * Usage :
     *  resetIterator(name) -> ok | error
     * </pre>
     */
    public static class resetiterator extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Reset an iterator", "L", "1", "str"};
        /** Default constructor. */
        public resetiterator() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                ExprFHI objIterator = getZx().getExpressionHandler().getIterator(objArg.getStringValue());
                if (objIterator == null) {
                    throw new ZXException("Iterator not found " + objArg.getStringValue());
                }
                
                ((ExprFHIDefault)objIterator).clear();
                
                exec = new LongProperty(zXType.rc.rcOK.pos, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Reset iterator " + objArg.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }    
    
    /**
     * getexprcontext - Get an entry from the expression context. 
     * 
     * <pre>
     * Same as [name] but name can now be the result of an expression.
     * 
     * Usage :
     *  getexprcontext(name) -> value
     * </pre>
     */
    public static class getexprcontext extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Get an entry from the expression context", "P", "1", "str"};
        /** Default constructor. */
        public getexprcontext() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = (Property)getZx().getExpressionHandler().getContext().get(objArg.getStringValue());
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("[" + objArg.getStringValue()+ "]");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }    
    
    /**
     * psswrd - Returns a encypted password 
     */
    public static class psswrd extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Returns a encypted password", "P", "1", "str"};
        /** Default constructor. */
        public psswrd() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                PasswordService obj = PasswordService.getInstance();
                
                try {
                    String strEncrypedPsswrd = obj.encrypt(objArg.getStringValue());
                    exec = new StringProperty(strEncrypedPsswrd, StringUtil.len(strEncrypedPsswrd) == 0 );
                } catch (Exception e) {
                    exec = new StringProperty("", true);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * soundex - Get the soundex of a string.
     */
    public static class soundex extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Get the soundex of a string", "S", "1", "str"};
        /** Default constructor. */
        public soundex() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(StringUtil.soundex(objArg.getStringValue()), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(" soundex value of (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * chr - Casts a int to a chr
     */
    public static class chr extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Casts a int to a char", "P", "1", "int"};
        /** Default constructor. */
        public chr() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(objArg.longValue() + "", false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("as character(" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * quickquerydef - Execute a query definition and return a specific column as property
     */
    public static class quickquerydef extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Execute a query definition and return a specific column as property", "P", "1", "str"};
        /** Default constructor. */
        public quickquerydef() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = getZx().getDataSources().getPrimary().executeQuickQueryDef(objArg.getStringValue(), 1);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("execute query definition (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * groovy - Returns a result of a groovy script.
     * 
     * <pre>
     * Usage :
     * 		groovy(script, [extra data]);
     * </pre>
     */
    public static class groovy extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = { "Returns a result of a groovy script", "P", "1", "str"};
        /** Default constructor. */
        public groovy() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int size = pcolArgs.size();
            checkArgs(size, 1, 99);
            
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                Object objReturn = null;
                Binding binding = new Binding();
                Property objValue;
                
                // Pass a handle to zx.
                binding.setVariable("zx", getZx());
                
                // Any extra parameters :
                for (int i = 1; i < size; i++) {
                    objValue = (Property)pcolArgs.get(i);
                    binding.setVariable("arg" + i, objValue.getStringValue());
                }
                
                try {
                    GroovyShell shell = new GroovyShell(binding);
                    objReturn = shell.evaluate(objArg.getStringValue());
                } catch (Exception e) {
                    throw new ZXException("Failed to parse groovy script", e);
                }
                
                if (objReturn instanceof Date) {
                    exec = new DateProperty((Date)objReturn, false);
                } else if (objReturn instanceof Double){
                    exec = new DoubleProperty((Double)objReturn, false);
                } else if (objReturn instanceof Long) {
                    exec = new LongProperty((Long)objReturn, false);
                } else if (objReturn instanceof String) {
                    exec = new StringProperty((String)objReturn); 
                } else {
                    exec = new StringProperty(objReturn.toString());
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC]);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * eval - Returns the evaluated expression.
     * 
     * <pre>
     * 
     * Usage :
     * eval(strExpression)
     * </pre>
     */
    public static class eval extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Evaluate the expression", "P", "1", "str"};
        /** Default constructor. */
        public eval() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = getZx().getExpressionHandler().eval(objArg.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("evaluate (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * abs - Returns the absolute value.
     * 
     * <pre>
     * 
     * Usage :
     * abs(dblValue)
     * </pre>
     */
    public static class abs extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the absolute value", "P", "1", "dbl|lng"};
        /** Default constructor. */
        public abs() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
//	                double dbl = objArg.doubleValue();
//	                // i.zX.dblValue(IIf(objArg.dblValue < 0, objArg.dblValue * -1, objArg.dblValue), pblnUseStatic:=False)
//	                exec = new DoubleProperty(this.zx, dbl < 0?dbl * -1:dbl, false);
                
                double dbl = objArg.doubleValue();
                exec = new DoubleProperty(Math.abs(dbl), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("absolute value of (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * qs - Returns the value from quick context.
     * 
     * <pre>
     * 
     * Usage :
     * qs(valueName)
     * </pre>
     */
    public static class qs extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns the value from quick context", "S", "1", "str"};
        /** Default constructor. */
        public qs() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                String strContext = getZx().getQuickContext().getEntry(objArg.getStringValue());
                if (StringUtil.len(strContext) == 0) {
                    exec = new StringProperty("", true);
                } else {
                    exec = new StringProperty(strContext, false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Get from quick context (" + objArg.getStringValue() + ") ");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * qc - Returns the get from context.
     * 
     * <pre>
     * 
     * Usage :
     * qc(valueName)
     * </pre>
     */
    public static class qc extends qs {
        /** Default constructor. */     
        public qc() {  super(); }
    }
    
    /**
     * context - Get a value from the context.
     * 
     * <pre>
     * 
     * Usage :
     * context(value)
     * </pre>     
     */
    public static class context extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get a value from the context", "S", "1", "str"};
        /** Default constructor. */
        public context() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                String contextValue = getZx().getSession().getFromContext(objArg.getStringValue());
                exec = new StringProperty(contextValue, StringUtil.len(contextValue) == 0);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("context (" + objArg.getStringValue() + ") ");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * not - Get the negate of a value.
     * 
     * <pre>
     * 
     * Usage :
     * not(value)
     * </pre>     
     */
    public static class not extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Negates the argument", "B", "1", "bln"};
        /** Default constructor. */
        public not() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(!objArg.booleanValue(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("negate (" + objArg.getStringValue() + ") ");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * trunc - Truncates from double to int.
     * 
     * <pre>
     * 
     * Usage :
     * trunc(valueName)
     * </pre>
     */
    public static class trunc extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Truncate from double to int", "P", "1", "int|dbl"};
		/** Default constructor. */
		public trunc() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new DoubleProperty(objArg.intValue(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Truncate (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * director - Treat parameter as a director
     * 
     * <pre>
     * 
     * Usage :
     * director('#qs.-pk')
     * </pre>
     */
    public static class director extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Treat parameter as a director", "P", "1", "str"};
        /** Default constructor. */
        public director() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(getZx().resolveDirector(objArg.getStringValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Resolve " + objArg.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * dbl - Gets the value as a double.
     * 
     * <pre>
     * 
     * Usage :
     * dbl(value)
     * </pre>
     */
    public static class dbl extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns value as a double", "D", "1", "str|int|dbl"};
        /** Default constructor. */
        public dbl() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new DoubleProperty(objArg.doubleValue(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("(" + objArg.getStringValue() + ") as double");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * dat - Returns value as a date .
      * 
     * <pre>
     * 
     * Usage :
     * dat(value)
     * </pre>
    */
    public static class dat extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns value as a date", "D", "1", "str|dat"};
        /** Default constructor. */
        public dat() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new DateProperty(objArg.dateValue(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("(" + objArg.getStringValue() + ") as date");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * int - Returns the value as a integar.
     * 
     * <pre>
     * 
     * Usage :
     * int(value)
     * </pre>
     */
    public static class Int extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns the value as a integar", "L", "1", "str|int|dbl"};
        /** Default constructor. */
        public Int() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new LongProperty(objArg.longValue(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("(" + objArg.getStringValue() + ") as integer");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * bln - Returns value as boolean.
      * 
     * <pre>
     * 
     * Usage :
     * bln(value)
     * </pre>
    */
    public static class bln extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Returns value as boolean", "B", "1", "str|int|dbl"};
		/** Default constructor. */
		public bln() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                boolean bln;
                if (objArg instanceof BooleanProperty) {
                    bln = ((BooleanProperty)objArg).getValue();
                } else {
                    bln = StringUtil.booleanValue(objArg.getStringValue());
                }
                exec = new BooleanProperty(bln, false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("(" + objArg.getStringValue() + ") as boolean");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * str - Returns value as a string.
     * 
     * <pre>
     * 
     * Usage :
     * str(value)
     * </pre>
     */
    public static class str extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns value as a string", "S", "1", "str|dat|bln|int|dbl"};
        /** Default constructor. */
        public str() {  super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(objArg.getStringValue(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("(" + objArg.getStringValue() + ") as string");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * ascii - Returns the value as ascii
     * 
     * <pre>
     * 
     * Usage :
     *  ascii(value)
     * </pre>
     */
    public static class ascii extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns the value as ascii", "L", "1", "str|dat|bln|int|dbl"};
        /** Default constructor. */
        public ascii() {  super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (StringUtil.len(objArg.getStringValue()) == 0) {
                    exec = new LongProperty(0, false);
                } else {
                    exec = new LongProperty(StringUtil.ascii(objArg.getStringValue()), false);
                }
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("ascii value of (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * trim - Returns a trimmed value.
     * 
     * <pre>
     * 
     * Usage :
     * trim(string)
     * </pre>
     */
    public static class trim extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns a trimmed value", "S", "1", "str"};
        /** Default constructor. */
        public trim() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(objArg.getStringValue().trim(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("trim$(" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * ltrim - Returns a left trimmed value .
     * 
     * <pre>
     * 
     * Usage :
     * ltrim(string)
     * </pre>
     */
    public static class ltrim extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns a left trimmed value", "S", "1", "str"};
        /** Default constructor. */
        public ltrim() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(StringUtil.ltrim(objArg.getStringValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("ltrim$(" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * rtrim - Returns a right trimmed value.
     * 
     * <pre>
     * 
     * Usage :
     * rtrim(string)
     * </pre>
     */
    public static class rtrim extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Returns a right trimmed value", "S", "1", "str"};
		/** Default constructor. */
		public rtrim() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(StringUtil.rtrim(objArg.getStringValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("rtrim$(" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * len - Returns length of the value.
     * 
     * <pre>
     * 
     * Usage :
     * len(string)
     * </pre>
     */
    public static class len extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns length of the value", "L", "1", "str"};
        /** Default constructor. */
        public len() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new LongProperty(StringUtil.len(objArg.getStringValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("length of (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * ucase - Returns value in uppercase.
     * 
     * <pre>
     * 
     * Usage :
     * ucase(string)
     * </pre>
     */
    public static class ucase extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Returns value in uppercase", "S", "1", "str"};
		/** Default constructor. */
		public ucase() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(objArg.getStringValue().toUpperCase(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("upper case (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * lcase - Returns value in lowercase.
     * 
     * <pre>
     * 
     * Usage :
     * lcase(string)
     * </pre>
     */
    public static class lcase extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Returns value in lowercase", "S", "1", "str"};
		/** Default constructor. */
		public lcase() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(objArg.getStringValue().toLowerCase(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("lower case (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * startofyear - Get the first day of the select year.
     * 
     * <pre>
     * 
     * Usage :
     * startofyear(date)
     * </pre>
     */
    public static class startofyear extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Get the first day of the select year", "D", "1", "dat"};
		/** Default constructor. */
		public startofyear() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                Date date = objArg.dateValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.DAY_OF_YEAR,1);
                
                exec = new DateProperty(cal.getTime(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("first day of year (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * startofmonth - Get the first day of the month from the selected value.
     * 
     * <pre>
     * 
     * Usage :
     * startofmonth(date)
     * </pre>
     */
    public static class startofmonth extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Get the first day of the month from the selected value", "D", "1", "dat"};
		/** Default constructor. */
		public startofmonth() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                Date date = objArg.dateValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                
                exec = new DateProperty(cal.getTime(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("first day of month (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * endofyear - Get the last day of the year for the select value.
     * 
     * <pre>
     * 
     * Usage :
     * endofyear(date)
     * </pre>
     */
    public static class endofyear extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Get the last day of the year for the select value", "D", "1", "dat"};
		/** Default constructor. */
		public endofyear() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                Date date = objArg.dateValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.DAY_OF_YEAR, cal.getMaximum(Calendar.DAY_OF_YEAR));
                
                exec = new DateProperty(cal.getTime(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("last day of year (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * endofmonth - Get the last day of the month of the select value .
     * 
     * <pre>
     * 
     * Usage :
     * endofmonth(date)
     * </pre>
     */
    public static class endofmonth extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Get the last day of the month of the select value", "D", "1", "dat"};
		/** Default constructor. */
		public endofmonth() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                Date date = objArg.dateValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.DAY_OF_MONTH, cal.getMaximum(Calendar.DAY_OF_MONTH));
                
                exec = new DateProperty(cal.getTime(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("last day of month (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * year - Returns month of the value.
     * 
     * <pre>
     * 
     * Usage :
     * year(date)
     * </pre>
     */
    public static class year extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Returns month of the value", "L", "1", "dat"};
		/** Default constructor. */
		public year() { super(); }
    		
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                Date date = objArg.dateValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                
                exec = new LongProperty(cal.get(Calendar.YEAR), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("year of (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * month - Get the month of the value.
     * 
     * <pre>
     * 
     * Usage :
     * month(date)
     * </pre>
     */
    public static class month extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns month of the value", "L", "1", "dat"};
        /** Default constructor. */
        public month() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                Date date = objArg.dateValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                
                exec = new LongProperty(cal.get(Calendar.MONTH), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("month of (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * day - Get the day of the value.
     * 
     * <pre>
     * 
     * Usage :
     * day(date)
     * </pre>
     */
    public static class day extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Returns day of the value", "L", "1", "dat"};
        /** Default constructor. */
        public day() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                Date date = objArg.dateValue();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                
                exec = new LongProperty(cal.get(Calendar.DAY_OF_WEEK), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("day of (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * ingroup - Check whether user is member of a group .
     * 
     * <pre>
     * 
     * Usage :
     * ingroup(userid)
     * </pre>
     */
    public static class ingroup extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Check whether user is member of a group", "B", "1", "str"};
        /** Default constructor. */
        public ingroup() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(getZx().getUserProfile().isUserInGroup(objArg.getStringValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Is user a member of (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * ingroup - Get config value.
     * 
     * <pre>
     * 
     * Usage :
     * 		config(name)
     * </pre>
     */
    public static class config extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get config value", "S", "1", "str"};
        /** Default constructor. */
        public config() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 1);
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(getZx().configValue(objArg.getStringValue()), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Config value of (" + objArg.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    //--------------------------------------------------------------------- All functions with two parameters
    
    /**
     * setqs - Set value in quick context and returns whether it was successfull or not.
     * 
     * <pre>
     * 
     * Usage : 
     * setqs (name,value)
     * </pre>
     */
    public static class setqs extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Set value in quick context and returns whether it was successfull or not.", "B", "2", "str,int|dbl|dat|str"};
        /** Default constructor. */
        public setqs() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                try {
                    getZx().getQuickContext().setEntry(objArg1.getStringValue(), objArg2.getStringValue());
                    exec = new BooleanProperty(true, false);
                } catch (Exception e) {
                    exec = new BooleanProperty(false, false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Store " + objArg2.getStringValue() + " in quick context as " + objArg1.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * setexprcontext - Set value in expression context
     * 
     * <pre>
     * 
     * Usage : 
     * setcontext(name, value)
     * </pre>
     */
    public static class setexprcontext extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Set value in expression context.", "B", "2", "int|dbl|dat|str"};
        /** Default constructor. */
        public setexprcontext() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (getZx().getExpressionHandler().setContext(objArg1.getStringValue(), objArg2).pos == zXType.rc.rcOK.pos) {
                    exec = new BooleanProperty(true);
                } else {
                    exec = new BooleanProperty(false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("expression context (" +  objArg1.getStringValue() + ") = " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }    
    
    /**
     * add - Adds the 2 arguments.
     * 
     * <pre>
     * 
     * Usage : 
     * add (arg1,arg2)
     * </pre>
     */
    public static class add extends ExpressionFunction {
        /** Discribes the expression */
		public static final String[] describe = {"Adds the 2 arguments.", "L", "2", "int|dbl"};
		/** Default constructor. */
        public add() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (objArg1 instanceof LongProperty && objArg2 instanceof LongProperty) {
                    long lngArg1 = objArg1.longValue();
                    long lngArg2 = objArg2.longValue();
                    
                    exec = new LongProperty(lngArg1 + lngArg2, false);
                } else {
                    double dblArg1 = objArg1.doubleValue();
                    double dblArg2 = objArg2.doubleValue();
                    
                    exec = new DoubleProperty(dblArg1 + dblArg2, false);
                }
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty( objArg1.getStringValue() + " + " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * stripchars - Strip all occurences of specific characters from a string. 
     * 
     * <pre>
     * Strip all occurences of specific characters from a string.
     * 
     * Second parameter is regular expresspion
     * 		stripchars('t1e2s3t4', '[1234]') -> 'test'
     * 
     * Or strip all characters that do not match from a string:
     * 		stripchars('t-e!s.t@', '[^a-zA-Z0-9]') -> 'test'
     * 
     * </pre>
     */
    public static class stripchars extends ExpressionFunction {
        /** Discribes the expression*/
        public static final String[] describe = { "Strip all occurences of specific characters from a string.", "P", "2", "str,str"};
        /** Default constructor. */
        public stripchars() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                // NOTE : ReplaceAll is part of JDK 1.4 only.
                exec = new StringProperty(objArg1.getStringValue().replaceAll(objArg2.getStringValue(), ""));
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Strip " + objArg2.getStringValue() + " from " + objArg1.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * mul - Multiples 2 argument.
     * 
     * <pre>
     * 
     * Usage : 
     * mul (arg1,arg2)
     * </pre>
     */
    public static class mul extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Multiples 2 argument", "L", "2", "int|dbl"};
        /** Default constructor. */
        public mul() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (objArg1 instanceof LongProperty && objArg2 instanceof LongProperty) {
                    long lngArg1 = objArg1.longValue();
                    long lngArg2 = objArg2.longValue();
                    
                    exec = new LongProperty(lngArg1 * lngArg2, false);
                } else {
                    double dblArg1 = objArg1.doubleValue();
                    double dblArg2 = objArg2.doubleValue();
                    
                    exec = new DoubleProperty(dblArg1 * dblArg2, false);
                }
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(objArg1.getStringValue() + " * " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * sub - Substracts the 2 arguments.
     * 
     * <pre>
     * 
     * Usage : 
     * sub (arg1,arg2)
     * </pre>
     */
    public static class sub extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Substracts the 2 arguments", "D", "2", "int|dbl"};
        /** Default constructor. */
        public sub() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (objArg1 instanceof LongProperty && objArg2 instanceof LongProperty) {
                    long lngArg1 = objArg1.longValue();
                    long lngArg2 = objArg2.longValue();
                    
                    exec = new LongProperty(lngArg1 - lngArg2, false);
                } else {
                    double dblArg1 = objArg1.doubleValue();
                    double dblArg2 = objArg2.doubleValue();
                    
                    exec = new DoubleProperty(dblArg1 - dblArg2, false);
                }
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(objArg1 + " - " + objArg2.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * div - Divide the first argument by the second.
     * 
     * <pre>
     * Added : Div with 3 parameters is used for default in case dividor is 0
     * 
     * Usage : 
     * div (arg1,arg2)
     * </pre>
     */
    public static class div extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Divide the first argument by the second", "C", "2", "int|dbl"};
		/** Default constructor. */
		public div() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int size = pcolArgs.size();
            checkArgs(size, 2, 3);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                double  dblArg2 = objArg2.doubleValue();
                if (dblArg2 != 0) {
                    double dblArg1 = objArg1.doubleValue();
                    exec = new DoubleProperty(dblArg1 / dblArg2, false);
                    
                } else if (size == 3) {
                    exec = (Property)pcolArgs.get(2);
                } else {
                    // Error : Not divible by zero.
                    exec = new StringProperty("", false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(objArg1.getStringValue() + " / " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * mod - Mod the 2 arguments.
     * 
     * <pre>
     * 
     * Usage : 
     * mod (arg1,arg2)
     * </pre>
     */
    public static class mod extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Mod the 2 arguments", "C", "2", "int|dbl,int|dbl"};
        /** Default constructor. */
        public mod() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                double dblArg1 = objArg1.doubleValue();
                double dblArg2 = objArg2.doubleValue();
                
                exec = new DoubleProperty(dblArg1 % dblArg2, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(objArg1.getStringValue() + " % " + objArg2.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * userpref - Get a selected user prefences from the selected user.
     * 
     * <pre>
     * 
     * Usage :
     * userpref (userid, preference)
     * </pre>
     */
    public static class userpref extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get a selected user prefences from the selected user", "S", "2", "str,str"};
        /** Default constructor. */
        public userpref() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                UserProfile objUsrPrf = (UserProfile)getZx().quickLoad("zxUsrPrf", objArg1);
                if (objUsrPrf == null) {
                    throw new ZXException("Faild to quickload the user profile for " + objArg1.getStringValue());
                }
                exec = new StringProperty(objUsrPrf.getPreference(objArg2.getStringValue()), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Get user preference '" 
                                            + objArg2.getStringValue() + "' for user '" 
                                            + objArg1.getStringValue() +  "'");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * addday - Add days to a date.
     * 
     * <pre>
     * Usage :
     * 
     * addday (date, days)
     * </pre>
     */
    public static class addday extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Add days to a date", "D", "2", "dat,dbl|int"};
        /** Default constructor. */
        public addday() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                // Get the date from the first argument.
                Date datArg1;
                if (objArg1 instanceof DateProperty) {
                    datArg1 = ((DateProperty)objArg1).getValue();
                } else {
                    datArg1 = objArg1.dateValue();
                }
                
                int intArg2 = objArg2.intValue();
                
                Calendar cal = Calendar.getInstance();
                
                // Use the first arg to set the date.
                cal.setTime(datArg1);
                // Add the second arg to the day of the year.
                cal.add(Calendar.DAY_OF_YEAR,  intArg2);
                
                exec = new DateProperty(cal.getTime(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(objArg1.getStringValue() + " + " + objArg2.getStringValue() + " days");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * addmonth - Add some months to a date.
     * 
     * <pre>
     * 
     * Usage:
     * addmonth <date> <months>
     * </pre> 
     */
    public static class addmonth extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Add some months to a date", "D", "2", "dat,dbl|int"};
        /** Default constructor. */
        public addmonth() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                Date datArg1;
                if (objArg1 instanceof DateProperty) {
                    datArg1 = ((DateProperty)objArg1).getValue();
                } else {
                    datArg1 = objArg1.dateValue();
                }
                int intArg2 = objArg2.intValue();
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(datArg1);
                cal.add(Calendar.MONTH, intArg2);
                
                exec = new DateProperty(cal.getTime(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(objArg1.getStringValue() + " + " + objArg2.getStringValue() + " months");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * addyear - Adds x years to a date.
     * 
     * <pre>
     * 
     * Usage :
     * addyear <date> <years>
     * </pre>
     */
    public static class addyear extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Adds x years to a date", "D", "2", "dat,dbl|lng"};
        /** Default constructor. */
        public addyear() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
                Date datArg1;
                if (objArg1 instanceof DateProperty) {
                    datArg1 = ((DateProperty)objArg1).getValue();
                } else {
                    datArg1 = objArg1.dateValue();
                }
                
                int intArg2 = objArg2.intValue();
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(datArg1);
                cal.add(Calendar.YEAR, intArg2);
                
                exec = new DateProperty(cal.getTime(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(objArg1.getStringValue() + " + " + objArg2.getStringValue() + " years");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty("dat,dbl|lng");
            }
            return exec;
        }
    }
    
    /**
     * addminute - Adds x minues to a date.
     * 
     * <pre>
     * V1.5:95 - BD30APR06: New function
     * 
     * Usage :
     * addminute <date> <minutes>
     * </pre>
     */
    public static class addminute extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Adds x minutes to a date", "D", "2", "dat,dbl|lng"};
        /** Default constructor. */
        public addminute() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
            	
                Date datArg1;
                if (objArg1 instanceof DateProperty) {
                    datArg1 = ((DateProperty)objArg1).getValue();
                } else {
                    datArg1 = objArg1.dateValue();
                }
                
                int intArg2 = objArg2.intValue();
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(datArg1);
                cal.add(Calendar.MINUTE, intArg2);
                
                exec = new DateProperty(cal.getTime(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(objArg1.getStringValue() + " + " + objArg2.getStringValue() + " minutes");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty("dat,dbl|lng");
            }
            return exec;
        }
    }
    
    /**
     * format - Formats the first parameter as specified in the second.
     * 
     * <pre>
     * 
     * Usage :
     * format (value, format)
     * </pre>
     */
    public static class format extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Formats the first parameter as specified in the second", "S", "2", "int|dbl|dat|str|bln,str"};
        /** Default constructor. */
        public format() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                String strTmp = "";
                
                /**
                 * The formatter to used.
                 */
                String strPattern = objArg2.getStringValue();
                
                /**
                 * Try to detect what type of formatting you want to perform.
                 */
                if (objArg1 instanceof DateProperty) {
                    DateFormat df = new SimpleDateFormat(strPattern);
                    strTmp =  df.format(((DateProperty)objArg1).getValue());
                    
                } else if (objArg1 instanceof DoubleProperty) {
                    DecimalFormat dbf = new DecimalFormat(strPattern);
                    strTmp = dbf.format(((DoubleProperty)objArg1).getValue());
                    
                } else if (objArg1 instanceof LongProperty) {
                    DecimalFormat dbf = new DecimalFormat(strPattern);
                    strTmp = dbf.format(((LongProperty)objArg1).getValue());
                    
                } else {
                    /**
                     * Lets try to see if this is a valid date :
                     */
                    if (DateUtil.isValid(getZx().getDateFormat(), objArg1.getStringValue())) {
                        try {
                            Date date = DateUtil.parse(new DateFormat[]{getZx().getDateFormat()}, objArg1.getStringValue());
                            
                            DateFormat df = new SimpleDateFormat(strPattern);
                            strTmp = df.format(date);
                        } catch (Exception e) {
                        	/** Failed to format date. **/
                        }
                        
                    } else {
                        MessageFormat msgf = new MessageFormat(objArg2.getStringValue());
                        strTmp = msgf.format(objArg1.getStringValue());
                    }
                }
                
                exec = new StringProperty(strTmp, false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(objArg1.getStringValue() + " formated as " + objArg2.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * round - Rounds the first argument to the second argument's number of decimal point .
     * 
     * <pre>
     * 
     * Usage: 
     * round(dbl,precision)
     * </pre>
     */
    public static class round extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Rounds the first argument to the second argument's number of decimal point", "C", "2", "dbl|int,int"};
        /** Default constructor. */
        public round() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
                NumberFormat  fmt = NumberFormat.getNumberInstance();
                fmt.setMaximumFractionDigits(objArg2.intValue());
                String rnd = fmt.format(objArg1.doubleValue());
                
                exec = new DoubleProperty( Double.parseDouble(rnd), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("round " + objArg1.getStringValue() + " to " + objArg2.getStringValue() +  " decimal places");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty("dbl|int,int");
            }
            return exec;
        }
    }
    
    /**
     * and - Check if 2 value are true. 
     * 
     * <pre>
     * 
     * Usage: 
     * and(arg1,arg2)
     * </pre> 
     */
    public static class and extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Check if 2 value are true. ", "B", "2", "bln,bln"};
        /** Default constructor.*/
        public and() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(objArg1.booleanValue() && objArg2.booleanValue(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("both " + objArg1.getStringValue() + " and " + objArg2.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * or - Check whether one of the values are true. 
     * 
     * <pre>
     * 
     * Usage: 
     * or(arg1,arg2)
     * </pre> 
     */
    public static class or extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Check whether one of the values are true ", "B", "2", "bln,bln"};
        /** Default constructor. */
        public or() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(objArg1.booleanValue() || objArg2.booleanValue(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Either " + objArg1.getStringValue() + " or " + objArg2.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * xor - Check only whether one of the values are true, if does are true then false.  
     * 
     * <pre>
     * 
     * Usage: 
     * xor(arg1,arg2)
     * </pre> 
     */
    public static class xor extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Check only whether one of the values are true", "B", "2", "bln,bln"};
		/** Default constructor. */
		public xor() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(objArg1.booleanValue() ^ objArg2.booleanValue(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Only " + objArg1.getStringValue() + " or " + objArg2.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * lt - Check if the first argument less than the second.
     * 
     * <pre>
     * 
     * Usage: 
     * lt(arg1,arg2)
     * </pre> 
     */
    public static class lt extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Check if the first argument less than the second", "B", "2", "int|dbl|dat|str|bln,int|dbl|dat|str|bln"};
		/** Default constructor. */
		public lt() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(objArg1.compareTo(objArg2) < 0, false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("is " + objArg1.getStringValue() + " < " + objArg2.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * le - Check if the first argument less than or equal to the second .
     * 
     * <pre>
     * 
     * Usage: 
     * le(arg1,arg2)
     * </pre> 
     */
    public static class le extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Check if the first argument less than or equal than the second", "B", "2", "int|dbl|dat|str|bln,int|dbl|dat|str|bln"};
        /** Default constructor. */
        public le() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(objArg1.compareTo(objArg2) <= 0, false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("is " + objArg1.getStringValue() + " <= " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * eq - Check if the first argument equal to the second.
     * 
     * <pre>
     * 
     * Usage: 
     * eq(arg1,arg2)
     * </pre> 
     */
    public static class eq extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Check if the first argument equal to the second", "B", "2", "int|dbl|dat|str|bln,int|dbl|dat|str|bln"};
		/** Default constructor. */
		public eq() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                // False takes presidence
                boolean eq = false;
                if (objArg1 instanceof BooleanProperty) {
                    eq = objArg1.booleanValue() == objArg2.booleanValue();
                } else if (objArg1 instanceof StringProperty){ // And expressions ?
                    eq = objArg1.getStringValue().equals(objArg2.getStringValue());
                } else if (objArg1 instanceof DoubleProperty){
                    eq = objArg1.doubleValue() == objArg2.doubleValue();
                } else if (objArg1 instanceof LongProperty){
                    eq = objArg1.longValue() == objArg2.doubleValue();
                } else if (objArg1 instanceof DateProperty){
                    eq = objArg1.dateValue().equals(objArg2.dateValue());
                }
                exec = new BooleanProperty(eq, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("is " + objArg1.getStringValue() + " == " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * ne - Check if the first argument not equal to the second.
     * 
     * <pre>
     * 
     * Usage: 
     * ne(arg1,arg2)
     * </pre> 
     */
    public static class ne extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Check if the first argument not equal to the second", "B", "2", "int|dbl|dat|str|bln,int|dbl|dat|str|bln"};
        /** Default constructor. */
        public ne() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
            	/**
            	 * Take some extra effort to see whether these 2 values do not match.
            	 */
                boolean ne = false;
                if (objArg1 instanceof BooleanProperty) {
                    ne = objArg1.booleanValue() != objArg2.booleanValue();
                } else if (objArg1 instanceof StringProperty){ // And expressions ?
                    ne = !objArg1.getStringValue().equals(objArg2.getStringValue());
                } else if (objArg1 instanceof DoubleProperty){
                    ne = objArg1.doubleValue() != objArg2.doubleValue();
                } else if (objArg1 instanceof LongProperty){
                    ne = objArg1.longValue() != objArg2.doubleValue();
                } else if (objArg1 instanceof DateProperty){
                    ne = !objArg1.dateValue().equals(objArg2.dateValue());
                }
                exec = new BooleanProperty(ne, false);
                
                /**
                 * Technically this could be more correct, as it takes in consideration whether it was a null property
                 * 
                 * BUT then an expression like this will fail when it should
                 * script(setqs("-test", ""), ne(qs("-test"),"")) => returns true
                 * as qs("-test") will return a null property and "" will return an empty string property.
                 * 
                 * This could be better then :
                 * script(setqs("-test", ""), ne(qs("-test"),null())
                 */
                // exec = new BooleanProperty(objArg1.compareTo(objArg2) != 0, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("is " + objArg1.getStringValue() + " != " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * gt - Check if the first argument greater than the second.
     * 
     * <pre>
     * 
     * Usage: 
     * gt(arg1,arg2)
     * </pre> 
     */
    public static class gt extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Check if the first argument greater than the second", "B", "2", "int|dbl|dat|str|bln,int|dbl|dat|str|bln"};
        /** Default constructor. */
        public gt() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(objArg1.compareTo(objArg2) > 0, false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("is " + objArg1.getStringValue() + " > " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * ge - Check if the first argument greater than or equal to the second .
     * 
     * <pre>
     * 
     * Usage: 
     * ge(arg1,arg2)
     * </pre> 
     */
    public static class ge extends ExpressionFunction {
        /** Discribes the expression */
		public static final String[] describe = {"Check if the first argument greater than or equal to the second", "B", "2", "Check if the first argument greater than or equal to the second"};
        /** Default constructor. */
        public ge() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(objArg1.compareTo(objArg2) >= 0, false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("is " + objArg1.getStringValue() + " >= " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * left - Pad the left side of a string.
     * 
     * <pre>
     * 
     * Usage: 
     * left(string, int)
     * </pre> 
     */
    public static class left extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Pad the left side of a string", "S", "2", "str,int"};
        /** Default constructor. */
        public left() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(objArg1.getStringValue().substring(0, objArg2.intValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("left " + objArg2.getStringValue() + " characters of " + objArg1.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * right - Pad the right side of a string.
     * 
     * <pre>
     * 
     * Usage: 
     * right(arg1,arg2)
     * </pre> 
     */
    public static class right extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Pad the right side of a string", "S", "2", "str,int"};
        /** Default constructor. */
        public right() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                String strTmp = objArg1.getStringValue();
                exec = new StringProperty(strTmp.substring(strTmp.length() - objArg2.intValue(), strTmp.length()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("right " + objArg2.getStringValue() + " characters of " + objArg1.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * string - string with x no instances of  y .
     * 
     * <pre>
     * 
     * Usage: 
     * string(num,string)
     * </pre> 
     */
    public static class string extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"string with x no instances of  y", "S", "2", "int,str"};
        /** Default constructor. */
        public string() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(StringUtil.padString((int)objArg1.longValue(), objArg2.getStringValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("string with " + objArg1.longValue() + " instances of " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * Return line <line number> from <string>, the system will check
     * to see what the line separator is (CrLf or Lf only).
     * 
     * <pre>
     * 
     * Usage: 
     * </pre> 
     * getline(string, line number) -> string
     */
    public static class getline extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Return line <line number> from <string>", "S", "2", "str,int"};
		/** Default constructor */
        public getline() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(StringUtil.getLine(objArg1.getStringValue(), (int)objArg2.longValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("get line (" + objArg1.longValue() + ") of, (" + objArg2.getStringValue() + ")");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * from - Get substring with from point only .
     * 
     * <pre>
     * This is like Java String#substring(pos)
     * 
     * Usage: 
     * 	from(value,int)
     * </pre> 
     */
    public static class from extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Get substring with from point only", "S", "2", "str,int"};
		/** Default constructor. */
		public from() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(objArg1.getStringValue().substring(objArg2.intValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("string " + objArg1.getStringValue() + " from position " + objArg2.longValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * hoursdiff - Get the diferrent between 2 dates in hours.
     * 
     * <pre>
     * 
     * Usage: 
     * hoursdiff(arg1,arg2)
     * </pre> 
     */
    public static class hoursdiff extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the diferrent between 2 dates in hours", "L", "2", "dat,dat"};
        /** Default constructor. */
        public hoursdiff() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new LongProperty(DateUtil.datediff(DateUtil.HOUR_DIFF, objArg1.dateValue(), objArg2.dateValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("hoursdiff (" + objArg1.getStringValue() + " , " + objArg2.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * minutesdiff - Get the diferrent between 2 dates in minutes.
     * 
     * <pre>
     * Required. Date. The two date/time values you want to use in the calculation. The value of Date1 is subtracted 
     * from the value of Date2 to produce the difference. Neither value is changed in the calling program.
     * 
     * Usage: 
     * minutesdiff(date1,date2)
     * </pre> 
     */
    public static class minutesdiff extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the diferrent between 2 dates in minutes", "L", "2", "dat,dat"};
        /** Default constructor. */
        public minutesdiff() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new LongProperty(DateUtil.datediff(DateUtil.MIN_DIFF, objArg1.dateValue(), objArg2.dateValue()), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("minutesdiff (" + objArg1.getStringValue() + " , " + objArg2.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * secondsdiff - Get the diferrent between 2 dates in seconds.
     * 
     * <pre>
     * 
     * Usage: 
     * secondsdiff(arg1,arg2)
     * </pre> 
     */
    public static class secondsdiff extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the diferrent between 2 dates in seconds", "L", "2", "dat,dat"};
        /** Default constructor. */
        public secondsdiff() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new LongProperty(DateUtil.datediff(DateUtil.SEC_DIFF, objArg1.dateValue(), objArg2.dateValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("secondsdiff (" + objArg1.getStringValue() + " , " + objArg2.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * millisecondsdiff - Get the diferrent between 2 dates in milliseconds.
     * 
     * <pre>
     * 
     * Usage: 
     * millisecondsdiff(arg1,arg2)
     * </pre> 
     */
    public static class millisecondsdiff extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the diferrent between 2 dates in milliseconds", "L", "2", "dat,dat"};
        /** Default constructor. */
        public millisecondsdiff() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new LongProperty(DateUtil.datediff(DateUtil.MILLI_DIFF, objArg1.dateValue(), objArg2.dateValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("millisecondsdiff (" + objArg1.getStringValue() + " , " + objArg2.getStringValue() + ")");
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * daysdiff - Get the diferrent between 2 date in days.
     * 
     * <pre>
     * 
     * Usage: 
     * daysdiff(arg1,arg2)
     * </pre> 
     */
    public static class daysdiff extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the diferrent between 2 date in days", "L", "2", "dat,dat"};
        /** Default constructor. */
        public daysdiff() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new LongProperty(DateUtil.datediff(DateUtil.DAY_DIFF, objArg1.dateValue(), objArg2.dateValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Number of days between " + objArg1.getStringValue() + " and " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * monthsdiff - Get the diferrent between 2 date in months.
      * 
     * <pre>
     * 
     * Usage: 
     * monthsdiff(arg1,arg2)
     * </pre> 
    */
    public static class monthsdiff extends ExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Get the diferrent between 2 date in months", "L", "2", "dat,dat"};
		/** Default constructor. */
        public monthsdiff() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new LongProperty(DateUtil.datediff(DateUtil.MONTH_DIFF, objArg1.dateValue(), objArg2.dateValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Number of months between " + objArg1.getStringValue() + " and " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * yearsdiff - Get the diferrent between 2 date in years.
     * 
     * <pre>
     * 
     * Usage: 
     * yearsdiff(arg1,arg2)
     * </pre> 
     */
    public static class yearsdiff extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the diferrent between 2 date in years", "B", "2", "dat"};
        /** Default constructor. */
        public yearsdiff() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new LongProperty(DateUtil.datediff(DateUtil.MONTH_DIFF, objArg1.dateValue(), objArg2.dateValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Number of years between " + objArg1.getStringValue() + " and " + objArg2.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * ifnull - Get the value that is not null. If the first is null get the second .
     * 
     * <pre>
     * 
     * Usage: 
     * ifnull(arg1,arg2)
     * </pre> 
     */
    public static class ifnull extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the value that is not null.", "B", "2", "int|dbl|dat|str|bln,int|dbl|dat|str|bln"};
        /** Default constructor. */
        public ifnull() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (objArg1.isNull) {
                    exec = objArg2;
                } else {
                    exec = objArg1;
                }
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("if " + objArg1.getStringValue() + " is null,  " 
                                            + objArg2.getStringValue() + " otherwise " 
                                            + objArg1.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * nextann - first anniversary of date1 on or after date2 .
     * 
     * <pre>
     * 
     * Usage: 
     * nextann(date1,date2)
     * </pre> 
     */
    public static class nextann extends ExpressionFunction {
        /** Discribes the expression */
		public static final String[] describe = {"first anniversary of date1 on or after date2", "B", "2", "dat|dat"};
		/** Default constructor. */
        public nextann() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                /**
				If right("0" & Month(objArg1.datValue), 2) & _
                        right("0" & Day(objArg1.datValue), 2) >= _
                    right("0" & Month(objArg2.datValue), 2) & _
                        right("0" & Day(objArg2.datValue), 2) Then
                   Set objReturn = i.zX.datValue(DateAdd("yyyy", 0, DateSerial(Year(objArg2.datValue), Month(objArg1.datValue), Day(objArg1.datValue))), pblnUseStatic:=False)
                Else
                   Set objReturn = i.zX.datValue(DateAdd("yyyy", 1, DateSerial(Year(objArg2.datValue), Month(objArg1.datValue), Day(objArg1.datValue))), pblnUseStatic:=False)
                End If
                 */
                
                if (DateUtil.datediff(DateUtil.YEAR_DIFF, objArg1.dateValue(), objArg2.dateValue()) < 1) {
                    // Under a year till this date.
                    exec = new DateProperty(objArg2.dateValue(),false);
                } else {
                    // Over a year
                    exec = new DateProperty(objArg1.dateValue(),false);
                }
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("first anniversary of ( " + objArg1.getStringValue() 
                                        + " on or after " + objArg2.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * match - Check whether str matches a regular expression.
     * 
     * <pre>
     * 
     * Usage: 
     * match(string,pattern)
     * </pre> 
     */
    public static class match extends ExpressionFunction {
        /** Discribes the expression */
    	public static final String[] describe = {"Check whether str matches a regular expression", "B", "2", "str|str"};
        /** Previously caches Regular expression */
        private static Pattern regExp;
        /** Default constructor. */
        public match() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (regExp == null || !regExp.pattern().equals(objArg2.getStringValue())) {
                    regExp = Pattern.compile(objArg2.getStringValue(), Pattern.CASE_INSENSITIVE);
                }
                Matcher objMatcher = regExp.matcher(objArg1.getStringValue());
                
                exec = new BooleanProperty(objMatcher.matches(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("does ( " + objArg1.getStringValue() + " ) match the pattern " + objArg2.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * cmatch -  Check whether case sensitive str matches a regular expression .
     * 
     * <pre>
     * 
     * Usage: 
     * cmatch(arg1,arg2)
     * </pre> 
     */
    public static class cmatch extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Check whether case str matches a regular expression", "B", "2", "str|str"};
        /** Previously caches Regular expression */
        private static Pattern regExp;
        /** Default constructor. */
        public cmatch() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (regExp == null || !regExp.pattern().equals(objArg2.getStringValue())) {
                    regExp = Pattern.compile(objArg2.getStringValue());
                }
                Matcher objMatcher = regExp.matcher(objArg1.getStringValue());
                
                exec = new BooleanProperty(objMatcher.matches(), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("does( " + objArg1.getStringValue() 
                                            + " match the pattern " + objArg2.getStringValue() + " (case sensitive)");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * instr - Get postion of the first accurance.
     * 
     * <pre>
     * 
     * Usage: 
     * instr(arg1,arg2)
     * </pre> 
     */
    public static class instr extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get postion of the first accurance", "L", "2", "str|str"};
        /** Default constructor. */
        public instr() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
                exec = new LongProperty(objArg1.getStringValue().indexOf(objArg2.getStringValue()), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Start position of string '" 
                                        + objArg2.getStringValue() + "' in '" 
                                        + objArg1.getStringValue() + "'");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * instrrev - Get postion of the last accurance.
     * 
     * <pre>
     * 
     * Usage: 
     * instrrev(arg1,arg2)
     * </pre> 
     */
    public static class instrrev extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get postion of the last accurance", "L", "2", "str|str"};
        /** Defatult constructor. */
        public instrrev() {super();}

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
                String strreverse = StringUtil.reverse(objArg1.getStringValue());
                exec = new LongProperty(strreverse.indexOf(objArg2.getStringValue()), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Start position of string '" 
                                            + objArg2.getStringValue() + "' in reversed '" 
                                            + objArg1.getStringValue() + "'");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * summarize - Summarize a string in a given number of characters.
     * 
     * <pre>
     * 
     * Synopsis: Summarize a string in a given number of characters
     * Use: sumarize(string, maxlength) -> string
     * In:   - string to summarize
     *        - number of characters to summarize to
     * Out:  - summarized string
     * 
     *  - Remove all instances of crLf
     *  - Truncate at maxLength - 3 and add '...' when truncated
     * </pre> 
     */
    public static class summarize extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Summarize a string in a given number of characters", "S", "2", "str|str"};
        /** Default constructor. */
        public summarize() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            
            checkArgs(pcolArgs, 2);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                /**
                 * - Remove any instance of crlf
                 * - Truncate to x characters
                 */
                String strTmp = StringUtil.stripChars("\n\r", objArg1.getStringValue());
                
                int length = objArg2.intValue();
                if (StringUtil.len(strTmp) > length) {
                    strTmp = strTmp.substring(0, length-3) + "...";
                }
                
                exec = new StringProperty(strTmp, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("summarize '" + objArg1.getStringValue() + "' to " + objArg1.getStringValue() + " characters");
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }

    //--------------------------------------------------- Three parameter functions
    
    /**
     * if - Emulates an if statement .
     * 
     * <pre>
     * If the first statement is true, then the second argument is retruned,
     * else return the third argument.
     * 
     * Usage: 
     * if(blnTest,trueValue,falseValue)
     * </pre> 
     */
    public static class If extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Emulates an if statement", "P", "3", "bln,int|dbl|dat|str|bln,int|dbl|dat|str|bln"};
        /** Default constructor. */
        public If() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 3);
                            
            Iterator iter = pcolArgs.iterator();
            Property objArg1 = (Property) iter.next();
            Property objArg2 = (Property) iter.next();
            Property objArg3 = (Property) iter.next();

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (objArg1.booleanValue()) {
                    exec = objArg2;
                } else {
                    exec = objArg3;
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("if " + objArg1.getStringValue() 
                                          + " then " + objArg2.getStringValue() 
                                          + " else " + objArg3.getStringValue());
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
                
            }
            return exec;
        }
    }
    
    /**
     * mid - Performs a substring on a string.
     * 
     * <pre>
     * This is like the VB mid function or the java substring function.
     * NOTE : However java's starting point is 0 and VB is 1. For this implementation. We start
     * from 1.
     * 
     * Usage: 
     * mid(str,intStartPos,intEndPos)
     * </pre> 
     */
    public static class mid extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Performs a substring on a string.", "S", "3", "str,int,int"};
        /** Default constructor. */
        public mid() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 3);
            
            Iterator iter = pcolArgs.iterator();
            Property objArg1 = (Property) iter.next();
            Property objArg2 = (Property) iter.next();
            Property objArg3 = (Property) iter.next();

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                // Add one to match vb usage.
                exec = new StringProperty(objArg1.getStringValue().substring(objArg2.intValue() + 1, objArg3.intValue()) + 1, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(objArg3.getStringValue() + " characters from " + objArg1.getStringValue() 
                                            + " starting at " + objArg2.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * replace - Replace all accurances of a value in a String with another value .
     * 
     * <pre>
     * This is equivalent to the Java replaceAll method for String
     * 
     * Usage: 
     * replace(string,replace,with)
     * </pre> 
     */
    public static class replace extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Replace all accurances of a value in a String with another value", "S", "3", "str,str,str"};
        /** Default constructor. */
        public replace() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            
            checkArgs(pcolArgs, 3);
            
            Iterator iter = pcolArgs.iterator();
            Property objArg1 = (Property) iter.next();
            Property objArg2 = (Property) iter.next();
            Property objArg3 = (Property) iter.next();

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new StringProperty(objArg1.getStringValue().replaceAll(objArg2.getStringValue(), objArg3.getStringValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Replace all occurences of " + objArg2.getStringValue() 
                                            + " in " + objArg1.getStringValue() 
                                            + " with " + objArg3.getStringValue());
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * makedate - Try to create a date from day/month and year .
     * 
     * <pre>
     * This is equivalent to the VB DateSerial or Java Calendar.set(year,month,day)
     * 
     * Usage: 
     * makedate(day,month,year)
     * </pre> 
     */
    public static class makedate extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Try to create a date from day/month and year", "D", "3", "int,int,int"};
        /** Default constructor. */
        public makedate() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 3);
            
            Iterator iter = pcolArgs.iterator();
            Property objArg1 = (Property) iter.next();
            Property objArg2 = (Property) iter.next();
            Property objArg3 = (Property) iter.next();

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
            	/**
            	 * Using the new Date(int,int,int) constructor is not 
            	 * depricated
            	 */
                Calendar cal = Calendar.getInstance();
                cal.set(objArg1.intValue(), objArg2.intValue(), objArg3.intValue());
                exec = new DateProperty(cal.getTime(), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("date with day " + objArg1.getStringValue() 
                						  + " and month " + objArg2.getStringValue() 
                                          + " and year " + objArg3.getStringValue());
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }        
    
    /**
     * between - Check whether a value is in the range of 2 other values .
     * 
     * <pre>
     * 
     * Usage: 
     * between(value,high,low)
     * </pre> 
    */
    public static class between extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Check whether a value is in the range of 2 other values ", "B", "3", "int|dbl|dat|str|bln,int|dbl|dat|str|bln,int|dbl|dat|str|bln"};
        /** Default constructor. */
        public between() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 3);
            
            Iterator iter = pcolArgs.iterator();
            Property objArg1 = (Property) iter.next();
            Property objArg2 = (Property) iter.next();
            Property objArg3 = (Property) iter.next();

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty( objArg1.compareTo(objArg2) > 0 && objArg1.compareTo(objArg3) > 0  ,false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("is " + objArg1.getStringValue() + " between " + objArg2.getStringValue() 
                                            + " and " + objArg3.getStringValue());
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS], false);
                
            }
            
            return exec;
        }
    }
    
    /**
     * havelock - Does user have a lock in place .
      * 
     * <pre>
     * Does user have a lock in place
     * 
     * Usage :
     * <code>havelock(entity, pk, id)</code>
     * </pre> 
     */
    public static class havelock extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Does user have a lock in place", "B", "3", "str,str|dbl|int,str"};
        /** Default constructor. */
        public havelock() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;

            checkArgs(pcolArgs, 3);
            
            Iterator iter = pcolArgs.iterator();
            Property objArg1 = (Property) iter.next();
            Property objArg2 = (Property) iter.next();
            Property objArg3 = (Property) iter.next();

            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(getZx().getBOLock().haveLock(objArg1.getStringValue(), objArg2, objArg3.getStringValue()), false);
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty("Does user have lock on " + objArg1.getStringValue() 
                                          + ", primary key " + objArg2.getStringValue() 
                                          + " with lock id " + objArg3.getStringValue());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    //---------------------------------------- Any number of parameter functions (including 0)

    /**
     * grep - Case-insensitive (grep) or case sensitive (cgrep).
     * 
     * <pre>
     * 
     * name:     grep / cgrep
     * synopsis: Case-insensitive (grep) or case sensitive (cgrep) extracting
     *            lines from a string; terminates by the termination string
     * use:      grep(string, pattern [,separator]) -> string
     * in:       string - string to grep
     *           pattern - pattern to match (regular expression)
     *           sep - separator to use (crLf by default)
     * </pre>
     */
    public static class grep extends ExpressionFunction {
        /** Previously caches Regular expression */
        private static Pattern regExp;
        
        /** Discribes the expression */
        public static final String[] describe = {"Case-insensitive (grep) or case sensitive (cgrep).", "S", "2-3", "str|str|str"};
        /** Default constructor. */
        public grep() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int size = pcolArgs.size();
            checkArgs(size, 2, 3);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                String strString;
                String strPattern;
                String strSep;
                
                if (size == 2) {
                    strString = objArg1.getStringValue();
                    strPattern = objArg2.getStringValue();
                    strSep = "\n";
                    
                } else { // 3 parameters
                    strString = objArg1.getStringValue();
                    strPattern = objArg2.getStringValue();
                    strSep = ((Property) pcolArgs.get(2)).getStringValue();  
                    
                }
                
                /**
                 * Singleton for multiple calls, like in a list form.
                 */
                if (regExp == null || !regExp.pattern().equals(strPattern)) {
                    regExp = Pattern.compile(strPattern);
                }
                
                String[] arrStr = strString.split(strSep);
                
                Matcher objMatcher;
                StringBuffer strReturn = new StringBuffer();
                
                for (int i = 0; i < arrStr.length; i++) {
                    objMatcher = regExp.matcher(arrStr[i]);
                    
                    if (objMatcher.matches()) {
                        if (StringUtil.len(strReturn) > 0) {
                            strReturn.append(strSep);
                        }
                        
                        strReturn.append(arrStr[i]);
                    }
                }
                
                exec = new StringProperty(strReturn.toString(), strReturn.length()==0);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                String strString;
                String strPattern;
                String strSep;
                
                if (size == 2) {
                    strString = objArg1.getStringValue();
                    strPattern = objArg2.getStringValue();
                    strSep = "\n";
                } else {
                    strString = objArg1.getStringValue();
                    strPattern = objArg2.getStringValue();
                    strSep = ((Property) pcolArgs.get(2)).getStringValue();           
                }
                
                exec = new StringProperty("grep '" + strString + "' for '" + strPattern + "', separated by '" + strSep + "'");
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }

    /**
     * getword - Get the nth word from a string.
     * 
     * <pre>
     * 
     * name:     getword
     * synopsis: get the nth word from a string; either 3 or 2 parameters. The 3rd parameter
     *            are the separators that indicate a new word
     * use:      getWord(string, n [,separator]) -> string
     * in:       string - string to get word from
     *           n - get the nth word; if negative, get from the end
     *                  1 is the first word, -1 is the last word
     *           sep - separator to use (whitespace and punctuation as default)
     *  usage :
     *     getword("Hello World", 2,  " ") => "World" 
     *     getword("Wow this is cool", -1,  " ") => "Cool"
     * </pre>
     */
    public static class getword extends ExpressionFunction {
        
        /** Discribes the expression */
        public static final String[] describe = {"Get the nth word from a string.", "S", "2-3", "str|str|str"};
        /** Default constructor. */
        public getword() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int size = pcolArgs.size();
            checkArgs(size, 2, 3);
            
            Property objArg1 = (Property) pcolArgs.get(0);
            Property objArg2 = (Property) pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (size == 2) {
                    exec = new StringProperty(StringUtil.getWord(objArg1.getStringValue(),objArg2.intValue()));
                } else { // 3 parameters
                    exec = new StringProperty(StringUtil.getWord(objArg1.getStringValue(),
                                                                 objArg2.intValue(), 
                                                                 ((Property)pcolArgs.get(2)).getStringValue()));
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                if (size == 2) {
                    exec = new StringProperty("get word " + objArg2.getStringValue() 
                                                + " from '" + objArg1.getStringValue() + "'");
                } else {
                    exec = new StringProperty("get word " + objArg2.getStringValue() 
                                                + " from '" + objArg1.getStringValue() + "' separated by '" 
                                                + ((Property)pcolArgs.get(2)).getStringValue() + "'");
                }
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * isnull - Check whether a value isNull.
     * 
     * <pre>
     * 
     * You may expect this one in the list of one-parameter
     * functions but look at the following:
     * isNull(qs("-pk")) where -pk is simply not in the quick context
     * thus the expression would evaluate to isNull() and that is
     * not a one parameter function yet you would expect true as output
     * 
     * Usage :
     * isNull(value)
     * </pre>
     */
    public static class isnull extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Check whether a value isNull", "P", "0-1", "str|dat|int|dbl|bln"};
        /** Default constructor. */
        public isnull() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            
            checkArgs(pcolArgs, 1, false);
            
            Property objArg = null;
            if (pcolArgs.size() > 0) {
                objArg = (Property)pcolArgs.iterator().next();
            }
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                if (objArg == null || objArg.isNull) {
                    exec = new BooleanProperty(true, false);
                } else {
                    exec = new BooleanProperty(objArg.isNull, false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                if (objArg == null) {
                    exec = new StringProperty("() is null");
                } else {
                    exec = new StringProperty("(" + objArg.getStringValue() +") is null");
                }
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * msg - Return zXMsg .
     * 
     * <pre>
     * 
     * Usage : 
     * msg(message-id, [up to 5 optional parameters])
     * </pre>
     */
    public static class msg extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Return zXMsg", "P", "1-6", "str,str,..."};
        /** Default constructor. */
        public msg() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            checkArgs(pcolArgs, 6, false); // 1-6 parameters
            Iterator iter = pcolArgs.iterator();
            
            if (!iter.hasNext()) throw new ZXException("We need at least 1 argument");
            
            Property objArg1 = (Property)iter.next();
            Property objArg; 
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
                int intNumArgs = pcolArgs.size();
                if (intNumArgs == 1) {
                    exec = new StringProperty(getZx().getMsg().getMsg(objArg1.getStringValue()), false);
                } else {
                    int i = 0;
                    
                    // Populate an array of string arguements :
                    String[] args = new String[intNumArgs];
                    // Carry on from the 2 argument :
                    while (iter.hasNext()) {
                        objArg = (Property)iter.next();
                        
                        args[i] = objArg.getStringValue();
                        i++;
                    }
                    
                    exec = new StringProperty(getZx().getMsg().getMsg(objArg1.getStringValue(), args), false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                StringBuffer strTmp = new StringBuffer("Get zXMsg '").append(objArg1.getStringValue()).append("'");
                if (iter.hasNext()) {
                    strTmp.append(", using as parameter ");
                    
                    while (iter.hasNext()) {
                        objArg = (Property) iter.next();
                        strTmp.append(objArg.getStringValue());
                        
                        if (iter.hasNext()) {
                            strTmp.append(", ");
                        }
                    }
                }
                exec = new StringProperty(strTmp.toString());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * biggest - Get the biggers value from a list.
     * 
     * <pre>
     * 
     * Usage :
     * biggest(arg1,arg2...)
     * </pre> 
     */
    public static class biggest extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the biggest/latest/earliest/smallest value from a list.", "P", "1-99", "str|int|dbl|dat,..."};
        /** Default constructor. */
        public biggest() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            Property objArg;
            Iterator iter = pcolArgs.iterator();
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                boolean bigger = true;
                if (this instanceof smallest || this instanceof earliest) {
                    bigger = false;
                }
                
                while (iter.hasNext()) {
                    exec = (Property)iter.next();
                    Iterator iter2 = pcolArgs.iterator();
                    while (iter2.hasNext()) {
                        objArg = (Property)iter2.next();
                        if (bigger) {
                            if (objArg.compareTo(exec) > 0) {
                                exec = objArg;
                            }
                        } else {
                            if (objArg.compareTo(exec) < -1) {
                                exec = objArg;
                            }
                        }
                    }
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                StringBuffer strTmp = new StringBuffer(this.getClass().getName());
                strTmp.append(" of ");
                
                int j = 1;
                int pinArgs = pcolArgs.size();
                while(iter.hasNext()) {
                    objArg = (Property)iter.next();
                    
                    strTmp.append(" (").append(objArg.getStringValue()).append(") ");
                    
                    if (j == pinArgs -1) {
                        strTmp.append(" and ");
                    } else if (j  < pinArgs) {
                        strTmp.append(", ");
                    }
                    
                    j++;
                }
                
                exec = new StringProperty(strTmp.toString());
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * latest - Get the latest value from a list of values.
     * 
     * <pre>
     * 
     * Usage :
     * latest(arg1,arg2...)
     * </pre> 
     */
    public static class latest extends biggest {
        /** Default constructor. */
        public latest() { super(); }
    }
    
    /**
     * smallest - Get the smallest value from a list of values.
     * 
     * <pre>
     * 
     * Usage :
     * smallest(arg1,arg2...)
     * </pre> 
     * 
     */
    public static class smallest extends biggest {
        /** Default constructor. */
        public smallest(){ super(); }
    }
        
    /**
     * earliest - Get the earliest value from a list of values.
     * 
     * <pre>
     * 
     * Usage :
     * earliest(arg1,arg2...)
     * </pre> 
     */
    public static class earliest extends biggest {
        /** Default constructor. */
        public earliest() { super(); }
    }    
    
    /**
     * sum - Gets the sum of the arguements .
     * 
     * <pre>
     * 
     * Usage :
     * 	sum(int,int,....)
     * </pre>
     */
    public static class sum extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Gets the sum or average of the arguements", "C", "1-99", "int|dbl,..."};
        /** Default constructor. */
        public sum() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            Iterator iter = pcolArgs.iterator();
            Property objArg = null;
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                double dblTotal = 0; 
                while (iter.hasNext()) {
                    objArg = (Property)iter.next();
                    dblTotal = dblTotal + objArg.doubleValue();
                }
                
                if (this instanceof avg){
                    exec = new DoubleProperty(pcolArgs.size()== 0 ? 0 : dblTotal/pcolArgs.size(), false);
                } else {
                    exec = new DoubleProperty(dblTotal, false);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                StringBuffer strTmp = new StringBuffer();
                if (this instanceof avg){
                    strTmp.append("average of ");
                } else {
                    strTmp.append("add ");
                }
                
                int j = 1;
                int pintArgs = pcolArgs.size();
                
                while (iter.hasNext()) {
                    objArg = (Property)iter.next();
                    strTmp.append(" (").append(objArg.getStringValue()).append(") ");
                    
                    if (j == pintArgs -1) {
                        strTmp.append(" and ");
                    } else if (j  < pintArgs) {
                        strTmp.append(", ");
                    }
                    
                    j++;
                }
                
                exec = new StringProperty(strTmp.toString());
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * avg - Gets the avarage of the arguements.
     *   
     * <pre>
     * 
     * Usage :
     * 	avg(int,int,....)
     * </pre>
     */
    public static class avg extends sum {
        /** Default constructor. */
        public avg() { super(); }
    }
    
    /**
     * concat - Concatenate the parameters to a single string .
     * 
     * <pre>
     * 
     * Usage :
     * 	concat(str,str,..)
     * </pre>
     */
    public static class concat extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Concatenate the parameters to a single string", "P", "1-99", "str,..."};
        /** Default constructor. */
        public concat() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            Iterator iter = pcolArgs.iterator();
            Property objArg = null;
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                StringBuffer strTmp = new StringBuffer();
                while (iter.hasNext()) {
                    objArg = (Property)iter.next();
                    strTmp.append(objArg.getStringValue());
                }
                
                exec = new StringProperty(strTmp.toString(), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                StringBuffer strTmp = new StringBuffer("concatenate ");
                int j = 1;
                int pintArgs = pcolArgs.size();
                
                while (iter.hasNext()) {
                    objArg = (Property)iter.next();
                    strTmp.append(" (").append(objArg.getStringValue()).append(") ");
                    
                    if (j == pintArgs -1) {
                        strTmp.append(" and ");
                    } else if (j  < pintArgs) {
                        strTmp.append(", ");
                    }
                    
                    j++;
                }
                
                exec = new StringProperty(strTmp.toString());
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epAPI)) {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
                
        }
    }
    
    /**
     * script - Execute a number of functions (all passed as parameters) and return the value of the last .
     * 
     * <pre>
     * 
     * Usage :
     * 	script(func, func...)
     * </pre>
     */
    public static class script extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Execute a number of functions and return the value of the last", "P", "1-10", "str,..."};
        /** Default constructor. */
        public script() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            
            int size = pcolArgs.size();
            checkArgs(size, 1, 99);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                for (int i = 0; i < size; i++) {
                    exec = (Property)pcolArgs.get(i);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                StringBuffer strTmp = new StringBuffer("Execute  ");
                for (int i = 0; i < size; i++) {
                    exec = (Property)pcolArgs.get(i);
                    strTmp.append(" (").append(exec.getStringValue()).append(") ");
                    if (i == size -1) {
                        strTmp.append(" and return value of ");
                    } else if (i  < size) {
                        strTmp.append(", ");
                    }
                }
                exec = new StringProperty(strTmp.toString());
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * choose - Pick a particular item from a list based on its position in that list.
     * 
     * <pre>
     * 
     * Usage :
     *  choose(3, 'a', 'b', 'c', 'd') ==> 'c'
     * </pre> 
     */
    public static class choose extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Pick a particular item from a list based on its position in that list", "P", "2-99", "int,dbl|str|dat|bln|int,..."};
        /** Default constuctor. */
        public choose() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int size = pcolArgs.size();
            checkArgs(size, 2, 99); // any number of arguements
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
                // Get the position
                int intArg = ((Property)pcolArgs.get(0)).intValue(); 
                
                if (intArg < size && intArg > 0) {
                    exec = (Property)pcolArgs.get(intArg);
                    
                } else {
                    // Error : Out of range. 
                    // ie : greater than 0 and smaller than size
                    exec = new StringProperty("", true);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                Property objArg = (Property)pcolArgs.get(0);
                
                StringBuffer strTmp = new StringBuffer("argument (").append(objArg.getStringValue()).append(") of ");
                for (int i = 1; i < size; i++) {
                    strTmp.append(" (").append( ((Property)pcolArgs.get(i)).getStringValue() ).append(") ");
                    
                    if (i == size -1) {
                        strTmp.append(" and "); // second last entry.
                        
                    } else if (i  < size) {
                        strTmp.append(", ");
                    }
                    
                }
                
                exec = new StringProperty(strTmp.toString());
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * translate - Translate a value.
     *  
     * <pre>
     * 
     * The first parameter is the value the other 
     * parameters are input value / output value pairs 
     * and the output value is selected where the input 
     * value matches
     * 
     * Usage :
     *  translate (value, key, translation, key, translation)
     * 
     * Example : 
     *  translate(1, 0, 'a', 1, 'b', 2, 'c', 3, 'd') ==> 'b'
     * </pre> 
     */
    public static class translate extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Translate a value", "P", "2-99", "dbl|str|dat|bln|int,dbl|str|dat|bln|int,dbl|str|dat|bln|int,..."};
        /** Default constructor. */
        public translate() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int size = pcolArgs.size();
            checkArgs(size, 2, 99);
            
            Iterator iter = pcolArgs.iterator();
            Property objArg1 = (Property)iter.next();
            Property objArg;
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                while (iter.hasNext()) {
                    objArg = (Property)iter.next(); // Always the even element
                    if (iter.hasNext()) {
                        if (objArg1.compareTo(objArg) == 0) {
                            exec = 	objArg = (Property)iter.next();
                            return exec;
                        }
                        
                        objArg = (Property)iter.next(); // Always the odd elment
                    }
                }
                /**
                 * DGS29MAR2004: If no matches, return a null property to prevent an error arising
                 */
                exec = new StringProperty("", true);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                StringBuffer strTmp = new StringBuffer("value where (");
                strTmp.append(objArg1.getStringValue()).append(") matches ");
                int j = 2;
                while (iter.hasNext()) {
                    objArg = (Property)iter.next();
                    strTmp.append(" (").append(objArg.getStringValue()).append(") ");
                    if (j == size-1) {
                        strTmp.append(" AND ");
                    } else if (j < size){
                        strTmp.append(", ");
                    }
                }
                exec = new StringProperty(strTmp.toString());
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            return exec;
        }
    }
    
    /**
     * select - The expression language equivalent of the 3GL select function.
     * 
     * <pre>
     * 
     * The expression language equivalent of the 3GL select
     * function; at least 2 parameters. Return value associated with a condition that is
     * matched
     * 
     * e.g. (assume qs('-mode') = 'all')
     * select( eq( qs('-mode'), 'all'), 1, eq( qs('-mode'), 'filter'), 2, true(), 3) ==> '1'
     * </pre> 
     */
    public static class select extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"The expression language equivalent of the 3GL select function", "P", "2-98", "dbl|str|dat|bln|int,dbl|str|dat|bln|int,dbl|str|dat|bln|int,..."};
        /** Default constructor. */
        public select() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int size = pcolArgs.size();
            checkArgs(size, 2, 99);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                for (int i = 1; i < size; i++) {
                    if ( ((Property)pcolArgs.get(i - 1)).booleanValue() ) {
                        exec = (Property)pcolArgs.get(i);
                        return exec;
                    }
                    // Skip the boolean expression.
                    i++;
                }
                exec = new StringProperty("", true);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                StringBuffer strTmp = new StringBuffer();
                for (int i = 1; i < size; i++) {
                    if ( ((Property)pcolArgs.get(i - 1)).booleanValue() ) {
                        strTmp.append("if (")
                        			.append( ((Property)pcolArgs.get(i - 1)).getStringValue())
                        			.append(") then (").append( ((Property)pcolArgs.get(i)).getStringValue() ).append(") ");
                    }
                    i++;
                }
                
                exec = new StringProperty(strTmp.toString());
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * in - Is the first item in the list of remaining items .
     * 
     *<pre>
     * 
     * Usage :
     * in(arg1,arg2,arg3...)
     * </pre>
     * 
     */
    public static class in extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Is the first item in the list of remaining items", "B", "2-99", "int,dbl|str|dat|bln|int,..."};
        /** Default constructor. */
        public in() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int size = pcolArgs.size();
            checkArgs(size, 2, 99);
            
            Property objArg1 = (Property)pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
				boolean blnFound = false;
				Property objArg;
				
				for (int i = 1; i < size; i++) {
					objArg = (Property)pcolArgs.get(i);
					if (objArg.compareTo(objArg1) == 0) {
						blnFound = true;
					}
				}
	                
                exec = new  BooleanProperty(blnFound, false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                	StringBuffer strTmp = new StringBuffer();
                	
                	if (size > 1) {
        				strTmp.append("is  (").append(objArg1.getStringValue()).append(") in ");
        				
        				for (int i = 1; i < size; i++) {
        				    strTmp.append(" (").append( ((Property)pcolArgs.get(i)).getStringValue()).append(") ");
        				    
        				    if (i == size - 1) {
        				        strTmp.append(" and ");
        				    } else if (i < size) {
        				        strTmp.append(", ");
        				    }
        				}
                	}
				exec = new StringProperty(strTmp.toString());
                	
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * firstof - Get the first of the arguments that are not null.
     * 
     *<pre>
     * 
     * Usage :
     * firstof(arg1,arg2,arg3...)
     * </pre>
     */
    public static class firstof extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get the first of the arguments that are not null", "P", "1-99", "dbl|str|dat|bln|int,..."};
        /** Default constructor. */
        public firstof() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            int size = pcolArgs.size();
            checkArgs(size, 1, 99);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                for (int i = 0; i < size; i++) {
                    exec = (Property)pcolArgs.get(i);
                    if (!exec.isNull) {
                        return exec;
                    }
                }
                exec = new StringProperty("", true);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                StringBuffer strTmp = new StringBuffer();
                
                for (int i = 0; i < size; i++) {
                    exec = (Property)pcolArgs.get(i);
                    
                    strTmp.append(" use (").append(exec.getStringValue()).append(") if not null");
                
                    if (i < size) {
                        strTmp.append(" otherwise ");
                    }
                }
                
                exec = new StringProperty(strTmp.toString());
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }

    /**
     * iterator - Create and save an iterator and load the parameters as iteration items
     * 
     *<pre>
     * 
     * Usage :
     *  iterator(name, arg1, arg2, ..., argx)
     *  
     *  Example : 
     *  
     *  script(iterator('iter','one','two','three'),_loopover('iter',setqs('val',[iter])),qs('val'))
     *  
     *  script(_loopover(iterator('iter','one','two','three'),setqs('val',[iter])),qs('val'))
     * </pre>
     */
    public static class iterator extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Create and save an iterator.", "L", "1-99", "dbl|str|dat|bln|int,..."};
        /** Default constructor. */
        public iterator() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            
            /** Must have at least 1 parameter (the name of the iterator)*/
            int size = pcolArgs.size();
            checkArgs(size, 1, 99);
            
            ExprFHIDefault objIterator = new ExprFHIDefault(((Property)pcolArgs.get(0)).getStringValue());
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
                for (int i = 1; i < size; i++) {
                    objIterator.addElement(pcolArgs.get(i));
                    // Could be but what is the benifit?
                    // objIterator.addElement "usrP" & CStr(j - 1), pcolArgs(j)
                }
                
                /**
                 * And store
                 */
                getZx().getExpressionHandler().setIterator(objIterator.getName(), objIterator);
                
                // exec = new LongProperty(zXType.rc.rcOK.pos, false);
                exec = new StringProperty(objIterator.getName());
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                StringBuffer strTmp = new StringBuffer();
                
                for (int i = 1; i < size; i++) {
                    if (strTmp.length()> 0) {
                        strTmp.append(", ");
                    }
                    strTmp.append(((Property)pcolArgs.get(i)).getStringValue());
                }
                
                exec = new StringProperty("Create iterator for (" + strTmp + ") and store as " + objIterator.getName());
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * alland - Checks whether all of the arguements are true .
     * 
     * <pre>
     * 
     * Usage :
     * alland(arg1,arg2,arg3...)
     * </pre>
     */
    public static class alland extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Checks whether all of the arguements are true", "B", "1-99", "bln,..."};
        /** Default constructor. */
        public alland() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            Property objArg;
            
            int size = pcolArgs.size();
            checkArgs(size, 1, 99);

            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(true, false);
                
                for (int i = 0; i < size; i++) {
                    objArg = (Property)pcolArgs.get(i);
                    
                    if ( !StringUtil.booleanValue(objArg.getStringValue()) ) {
                        exec = new BooleanProperty(false, false);
                        break;
                    }
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {  
                StringBuffer strTmp = new StringBuffer();
                
                if (size == 1) {
                    strTmp.append("is ");
                } else {
                    strTmp.append("are ");
                }
                
                for (int i = 0; i < size; i++) {
                    objArg = (Property)pcolArgs.get(i);
                    
                    if (i > 0) {
                        strTmp.append(" and ");
                    }
                    strTmp.append("(").append(objArg.getStringValue()).append(")");
                }
                
                if (size == 1) {
                    strTmp.append(" true");
                } else {
                    strTmp.append(" all true");
                }
                
                exec = new StringProperty(strTmp.toString());
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
    
    /**
     * allor - Checks whether one of the arguements are true.
     * 
     * <pre>
     * 
     * Usage :
     * allor(arg1,arg2,arg3...)
     * </pre>
     */
    public static class allor extends ExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Checks whether all of the arguements are true", "B", "1-99", "bln,..."};
        /** Default constructor. */
        public allor() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            Property objArg;
            
            int size = pcolArgs.size();
            checkArgs(size, 1, 99);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                exec = new BooleanProperty(false, false);
                
                for (int i = 0; i < size; i++) {
                    objArg = (Property)pcolArgs.get(i);
                    
                    if ( StringUtil.booleanValue(objArg.getStringValue()) ) {
                        exec = new BooleanProperty(true, false);
                        break;
                    }
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {  
                StringBuffer strTmp = new StringBuffer();
                
                if (size == 1) {
                    strTmp.append("is ");
                } else {
                    strTmp.append("are ");
                }
                
                for (int i = 0; i < size; i++) {
                    objArg = (Property)pcolArgs.get(i);
                    
                    if (i > 0) {
                        strTmp.append(" or ");
                    }
                    strTmp.append("(").append(objArg.getStringValue()).append(")");
                }
                strTmp.append(" true");
                
                exec = new StringProperty(strTmp.toString());
                
            } else {
                exec = new StringProperty(describe[EXPR_ARGS]);
            }
            
            return exec;
        }
    }
}