/*
 * Created on Mar 29, 2004 by michael
 * $Id: ExprFHPageflow.java,v 1.1.2.11 2006/07/17 14:03:41 mike Exp $
 */
package org.zxframework.web.expression;

import java.lang.reflect.Constructor;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.zxframework.ZXBO;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.expression.ExprFH;
import org.zxframework.expression.ExpressionFunction;
import org.zxframework.property.BooleanProperty;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringEscapeUtils;
import org.zxframework.util.StringUtil;
import org.zxframework.web.Pageflow;

/**
 * Function handler for pageflow related stuff.
 * 
 * <pre>
 * 
 * At the moment there is only a couple of the pageflow expressions, and also 
 * this has been bolted on at the moment, as this expression library need a special
 * go() method to execute the pageflows.
 * 
 * Change    : BD27JUN03
 * Why       : Added encode function
 * 
 * Change    : BD22JAN04
 * Why       : Added alias property
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ExprFHPageflow extends ExprFH {

    //------------------------ Member
    
    private Pageflow pageflow;
    
    //------------------------ Constructors

	/**
     * @param pobjPageflow A handle to the pageflow object.
     */
    public ExprFHPageflow(Pageflow pobjPageflow) {
        super();
        
        this.pageflow = pobjPageflow;
    }
    
    //------------------------ Inner Classes
    
    //------------------------ All functions with 1 parameters : 

    /**
     * qs - Get a value from query string.
     * 
     * <pre>
     * 
     * Usage :
     * qs(str);
     *  </pre>
     */
    public static class qs extends PageflowExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get a value from query string.", "S", "1", "str"};
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
                String strTmp = getPageflow().getQs().getEntryAsString(objArg.getStringValue());
                exec = new StringProperty(strTmp,StringUtil.len(strTmp) == 0);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC], false);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS], false);
            }
            return exec;
        }
    }
    
    /**
     * req - Get a value from the request .
     * 
     * <pre>
     * 
     * Usage :
     * req(str)
     * </pre>
     */
    public static class req extends PageflowExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Get a value from the request", "S", "1", "str"};
        /** Default constructor. */
        public req() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec = null;
            checkArgs(pcolArgs, 1);            
            Property objArg = (Property) pcolArgs.get(0);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                String strTmp = getPageflow().getRequest().getParameter(objArg.getStringValue());
                exec = new StringProperty(strTmp, StringUtil.len(strTmp) == 0);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC], false);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS], false);
            }
            return exec;
        }
    }
    
    /**
     * encode - Encode a string for proper Javascript / HTML use .
     * 
     * <pre>
     * 
     * Usage :
     * encode(str)
     * </pre>
     */
    public static class encode extends PageflowExpressionFunction {
        /** Discribes the expression */
        public static final String[] describe = {"Encode a string for proper HTML use ", "S", "1", "str"};
        /** Default constructor. */
        public encode() { super(); }
        
        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            Property objArg = (Property) pcolArgs.get(0);
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                
                try {
                    String strTmp = URLEncoder.encode(objArg.getStringValue(), "UTF-8");
                    exec = new StringProperty(strTmp, StringUtil.len(strTmp) ==0);
                    
                } catch (Exception e) {
                    exec = new StringProperty("", true);
                }
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC], false);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS], false);
            }
            return exec;
        }
    }
    
    /**
     * jsencode - Encode a string for proper Javascript / HTML use.
     * 
     * <pre>
     * 
     * Usage : 
     * jsencode(str)
     * </pre> 
     */
    public static class jsencode extends PageflowExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Encode a string for proper Javascript use", "S", "1", "str"};
		/** Default constructor. */
		public jsencode() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            Property objArg = (Property) pcolArgs.get(0);
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                String strTmp = StringEscapeUtils.escapeJavaScript(objArg.getStringValue());
                exec = new StringProperty(strTmp, StringUtil.len(strTmp) == 0);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC], false);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS], false);
            }
            return exec;
        }
    }
    
    //------------------------ Has 2 arguments
    
    /**
     * bolabel - Get label value for BO from context or create a new one.
     * 
     * <pre>
     * 
     * bolabel('entity', 'pk') - Get label for newly created BO and pk
     * </pre>     *  
     */
    public static class bolabel extends PageflowExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Get label value for BO from context or create a new one.", "S", "1-2", "str,str"};
		/** Default constructor. */
		public bolabel() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {                
                /**
                 * First argument is the BO to use
                 */
                ZXBO objBO = getZx().createBO(objArg1.getStringValue());
                if (objBO == null) {
                    throw new ZXException("Cannot create instance of '" + objArg1.getStringValue() + "'");
                }
                
                /**
                 * 2nd arg is the pk to use
                 */
                objBO.setPKValue(objArg2.getStringValue());
                objBO.loadBO("Label", "+", true);
                
                exec = new StringProperty(objBO.formattedString("Label"), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC], false);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS], false);
            }
            
            return exec;
        }
    }
    
    //------------------------ Any number of args
    
    /**
     * havelock - Check whether a lock exists for the given entity.
     * 
     * <pre>
     * 
     * argument 1 = entity
     * argument 2 = pk
     * argument 3 (optional) = lock id, if not given: use sub-session
     * 
     * Usage : 
     *  havelock(entity, pk, lock_id)
     */
    public static class havelock extends PageflowExpressionFunction {
    	/** Discribes the expression */
		public static final String[] describe = {"Check whether a lock exists for the given entity.", "S", "2-3", "str"};
		/** @see ZXObject#ZXObject() */
		public havelock() { super(); }

        /** 
         * @see ExpressionFunction#exec(ArrayList, zXType.exprPurpose)
         **/
        public Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException {
            Property exec;
            checkArgs(pcolArgs, 3, false);
            
            Property objArg1 = (Property)pcolArgs.get(0);
            Property objArg2 = (Property)pcolArgs.get(1);
            
            if (penmPurpose.equals(zXType.exprPurpose.epEval)) {
                String strTmp;
                if (pcolArgs.size() == 3) {
                    strTmp = ((Property)pcolArgs.get(2)).getStringValue();
                } else {
                    strTmp = getPageflow().getQs().getEntryAsString("-ss");
                }
                
                exec = new BooleanProperty(getZx().getBOLock().haveLock(objArg1.getStringValue(), objArg2, strTmp), false);
                
            } else if (penmPurpose.equals(zXType.exprPurpose.epDescribe)) {
                exec = new StringProperty(describe[EXPR_DESC], false);
            } else {
                exec = new StringProperty(describe[EXPR_ARGS], false);
            }
            
            return exec;
        }
    }
    
    //------------------------ Overloaded methods
    
    /**
     * Execute function. This over loads the default default behaviour of
     * the expression handler.
     *
     * @param pstrFunction The function to execute 
     * @param pcolArgs List of arguements passed to the function.
     * @param penmPurpose The purpose of the execution 
     * @return Returns the result of the executed function
     * @throws ZXException Thrown if go fails. 
     */
     public Property go(String pstrFunction, ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException{
		if(getZx().trace.isFrameworkTraceEnabled()) {
		    getZx().trace.enterMethod();
		    getZx().trace.traceParam("pstrFunction", pstrFunction);
		    getZx().trace.traceParam("pcolArgs", pcolArgs);
		    getZx().trace.traceParam("penmPurpose", penmPurpose);
		}
		
		Property go = null; 
		
		try {
		    
			Class cls = Class.forName(this.getClass().getName() +  "$" + StringUtil.makeClassName(pstrFunction));
			
			if (cls != null) {
				Constructor c =  cls.getConstructor(new  Class []{});
				
				//-------- Allow for Pageflow expressions  
				//- Pass a handle to pageflow object.
				PageflowExpressionFunction objDirector = (PageflowExpressionFunction)c.newInstance(new Object[]{});
				objDirector.setPageflow(this.pageflow);
				//-------- Allow for Pageflow expressions  
				
				try {
				    go = objDirector.exec(pcolArgs, penmPurpose);
				} catch (Exception e) {
				    getZx().log.error("Failed to : execute director : " + pstrFunction, e);
				}
				
			}
			
		    return go;
		} catch (Exception e) {
	        getZx().trace.addError("Failed to : Execute function", e);
		    if (getZx().log.isErrorEnabled()) {
		        getZx().log.error("Parameter : pstrFunction = "+ pstrFunction);
		        getZx().log.error("Parameter : pcolArgs = "+ pcolArgs);
		        getZx().log.error("Parameter : penmPurpose = "+ penmPurpose);
		    }
		    
		    if (getZx().throwException) throw new ZXException(e);
		    return go;
		} finally {
		    if(getZx().trace.isFrameworkTraceEnabled()) {
		        getZx().trace.returnValue(go);
		        getZx().trace.exitMethod();
		    }
		}
     }
}