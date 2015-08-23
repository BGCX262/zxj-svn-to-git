/*
 * Created on Mar 24, 2004
 * $Id: ExprFH.java,v 1.1.2.9 2006/07/17 16:40:43 mike Exp $
 */
package org.zxframework.expression;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import org.zxframework.Label;
import org.zxframework.Option;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.property.Property;
import org.zxframework.property.StringProperty;
import org.zxframework.util.StringUtil;

/**
 * Interface for expression function handlers.
 * 
 * <pre>
 * 
 *  Change    : BD23JAN04
 *  Why       : Added 'alias'; needed for support of expression popup
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class ExprFH extends ZXObject {
    
    //------------------------ Members
    
    private String alias;
    
    protected static final int EXPR_DESC = 0;
    protected static final int EXPR_RTN = 1;
    protected static final int EXPR_NOARGS = 2;
    protected static final int EXPR_ARGS = 3;
    
    //------------------------ Constructors
        
    /**
     * Default efault constructor
     */
    public ExprFH() { super(); }
    
    //------------------------ Getters/Setters
    
    /**
     * @return Returns the alias.
     */
    public String getAlias() {
        return alias;
    }
    
    /**
     * @param alias The alias to set.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    //------------------------ Methods to overide 
    
    /**
     * Execute function.
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
            
            Class cls  = Class.forName(this.getClass().getName() +  "$" + StringUtil.makeClassName(pstrFunction));
            
            if (cls != null) {
                
	            Constructor c =  cls.getConstructor(new  Class []{});
	            ExpressionFunction objExpression = (ExpressionFunction)c.newInstance(new Object[]{});
	            
	            //try {
	                go = objExpression.exec(pcolArgs, penmPurpose);
	            //} catch (Exception e) {
                    // Need to present the exception data back to the user?
	            //    getZx().log.error("Failed to : execute director " + pstrFunction, e);
	            //}
	            
            }
            
            return go;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Execute function : " + pstrFunction, e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrFunction = "+ pstrFunction);
                getZx().log.error("Parameter : pcolArgs = "+ pcolArgs);
                getZx().log.error("Parameter : penmPurpose = "+ penmPurpose);
            }
            
            if (getZx().throwException) throw new ZXException("Failed to execute function : " + pstrFunction, e);
            return go;
        } finally {
            if(getZx().trace.isFrameworkTraceEnabled()) {
                getZx().trace.returnValue(go);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get the list of supported expression functions.
     * 
     * <pre>
     * 
     * 0 - no parameters
     * 1-10 - no of parameters
     * -1 - any number
     * 
     * int - Integar
     * lng - Long
     * dbl - Double
     * 
     * dbl|lng,... - any number of dbls or lng parameter
     * 
     * L - LongProperty
     * E - ExpressionProperty
     * D - DateProperty
     * C - DoubleProperty
     * S - StringProperty
     * </p>
     * 
     * @return Returns a collection of the supported functions as Options.
     * @throws ZXException Thrown if supports fails
     */
    public ZXCollection supports()  throws ZXException {
		if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		    getZx().trace.enterMethod();
		}
		
        ZXCollection supports = new ZXCollection();
        
		try {
	        Class[] cls = this.getClass().getClasses();
	        String name;
	        String describe[];
            Label objLabel;
	        Property objArg = new StringProperty("");
            ArrayList colArgs;
            Property objExprResult = null;
            String strLabel;
            
	        for (int i = 0; i < cls.length; i++) {
	            Class class1 = cls[i];
	            try {
	                
	                if (!class1.isInterface()) {
	                    name = class1.getName().substring(class1.getName().indexOf('$')+1).toLowerCase();
	                    describe = (String[])class1.getField("describe").get(null);
	        	        
	                    /**
                         * Generate some dummy arguments then call the handler to get the API.
                         * This will provide some useful information in the drop down. However,
                         * whatever the user selects from the drop down, we don't place the same
                         * string into the expression - we use the 'commas' version as it is
                         * better to use. E.g. user selects 'addday (dat,int|dbl)' from the
                         * dropdown, gets 'addday(,) in the expression.
	                     */
	                    int intSize = Integer.parseInt(describe[EXPR_NOARGS]);
                        colArgs = new ArrayList(intSize);
                        for (int j = 0; j < intSize; j++) {
                            colArgs.add(objArg);
                        }
                        
                        try {
                            objExprResult = go(name, colArgs, zXType.exprPurpose.epAPI);
                        } catch (Exception e) {
                        	/**
                        	 * Ignore exception
                        	 */
                        	getZx().trace.addError("Failed to describe expression", name, e);
                        }
                        
                        if (objExprResult != null) {
                            strLabel = name + " ( " + objExprResult.getStringValue() + ")";
                        } else {
                            strLabel = name + "( " + describe[EXPR_DESC] + ")";
                        }
                        
	                    objLabel = new Label();
	                    objLabel.setLabel(name);
	                    objLabel.setLanguage("EN");
	                    objLabel.setDescription(strLabel);
	                    
	                    Option objOption = new Option();
	                    objOption.setValue(name);
	                    objOption.getLabel().put(objLabel.getLanguage(), objLabel);
                        
	                    supports.put(name, objOption);
	                }
	                
	            } catch (SecurityException e) {
	                getZx().log.error("SecurityException",e);
	            } catch (IllegalArgumentException e) {
	                getZx().log.error("IllegalArgumentException",e);
	            } catch (IllegalAccessException e) {
	                getZx().log.error("IllegalAccessException",e);
	            } catch (NoSuchFieldException e) {
	                getZx().log.error("NoSuchFieldException",e);
	            }
	        }
	        
		    return supports;
		} catch (Exception e) {
		    if (getZx().log.isErrorEnabled()) {
		    	getZx().trace.addError("Failed to : Get supported directors", e);
		    }
		    
		    if (getZx().throwException) throw new ZXException(e);
		    return supports;
		} finally {
		    if(getZx().trace.isFrameworkCoreTraceEnabled()) {
		        getZx().trace.returnValue(supports);
		        getZx().trace.exitMethod();
		    }
		}
    }
}