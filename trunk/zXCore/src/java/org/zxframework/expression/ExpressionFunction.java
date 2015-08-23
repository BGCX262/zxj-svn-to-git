/*
 * Created on Mar 27, 2004
 * $Id: ExpressionFunction.java,v 1.1.2.5 2006/07/17 16:40:43 mike Exp $
 */
package org.zxframework.expression;

import java.util.ArrayList;

import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.property.Property;

/**
 * <p>This represents a single Expression Function, like #user.  
 * It is very similar to how the directors work</p>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public abstract class ExpressionFunction extends ZXObject {
    
    /**
     * Default constructor
     */
    public ExpressionFunction() {
        super();
    }
    
    /**
     * This is the actual execution point of an expression
     * 
     * @param pcolArgs An ZXCollection of Propertys as arguments to the Expression
     * @param penmPurpose The purpose/use of the Expression
     * @return Returns the result of an expresion
     * @throws ZXException Thrown if exec fails.
     */
    public abstract Property exec(ArrayList pcolArgs, zXType.exprPurpose penmPurpose) throws ZXException;
    
    /**
     * Check whether a expression has the correct number of arguments
     *
     * @param pcolArgs The arguments being passed to the expressio 
     * @param pintNmRqrdArgs Number of arguments expected 
     * @return True if there is a correct number of args
     * @throws ZXException Thrown if checkArgs fails. 
     */
    protected boolean checkArgs(ArrayList pcolArgs, int pintNmRqrdArgs) throws ZXException {
        int intSize = 0;
        if (pcolArgs != null) {
        	intSize = pcolArgs.size();
        }
        
        if (intSize != pintNmRqrdArgs) {
            throw new ZXException("Arguments " + intSize + ", expected " + pintNmRqrdArgs);
        }
        
        return true;
     }
    
    /**
     * Check whether a expression has the correct number of arguments
     *
     * @param pcolArgs The arguments being passed to the expression, null can not be excepted
     * @param pintNmRqrdArgs Number of arguments expected 
     * @param pblnStrict Whether to be strick. True if you do want to be trick, this is the default behaviour
     * @return Returns true if an expression has the correct number of arguments. 
     * @throws ZXException Thrown if checkArgs fails. 
     */
    protected boolean checkArgs(ArrayList pcolArgs, 
                                int pintNmRqrdArgs, 
                                boolean pblnStrict) throws ZXException {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pcolArgs", pcolArgs);
            getZx().trace.traceParam("pintNumber", pintNmRqrdArgs);
            getZx().trace.traceParam("pblnStrict", pblnStrict);
        }

        boolean correctArgs = false;
        
        try {
            int intSize = 0;
            if (pcolArgs != null) {
            	intSize = pcolArgs.size();
            }
            
            switch (pintNmRqrdArgs) {
                case -1:
                	/**
                	 * Any number of arguements, just not none
                	 */
                    if (intSize > 0)  correctArgs = true;
                    break;
                case 0:
                	/**
                	 * None only
                	 */
                    if (pcolArgs != null || intSize == 0) correctArgs = true;
                    break;
                default:
                	/**
                	 * For a set numnber of arguements
                	 */
                    if (pblnStrict) {
                    	/**
                    	 * A fixed number of arguments only
                    	 */
                        if (intSize == pintNmRqrdArgs) {
                            correctArgs = true;
                        }
                    } else {
                    	/**
                    	 * Any number of arguements from 0-n
                    	 */
                        if (pcolArgs != null || intSize >= pintNmRqrdArgs) {
                            correctArgs = true;
                        }
                    }
                    break;
            }
            
            if (!correctArgs) {
                throw new ZXException("Incorrect number of arguments passed to this expression. " +
                					  "Expected : " + pintNmRqrdArgs + " Recieved : " + intSize);
            }
            
            return correctArgs;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Checking arguments", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pcolArgs = "+ pcolArgs);
                getZx().log.error("Parameter : pintNumber = "+ pintNmRqrdArgs);
                getZx().log.error("Parameter : pblnStrict = "+ pblnStrict);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return correctArgs;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Check the arguments for a expression.
     * 
     * @param size the number of arguments
     * @param required the required number of parameters 
     * @return Returns true if size is equal to required.
     */
    protected boolean checkArgs(int size, int required) {
        return size == required;
    }
    
    /**
     * Check whether the size if within the min/max range.
     * 
     * @param size The size to check.
     * @param min The minimum number.
     * @param max The maximum number.
     * @return Returns true if within range.
     * @throws ZXException Out of range.
     */
    protected boolean checkArgs(int size, int min, int max) throws ZXException {
        
        if (size >= min && size <= max) {
            return true;
        }
        
        /**
         * The number of arguements does not match the range specified.
         */
        throw new ZXException("Arguments (" + size + ") is not within range (" 
                              + min + " to " + max + ")");
    }
    
    /**
     * Check if there are not arguments.
     * 
     * @param pcolArgs The args to check.
     * @return Returns true if pcolArgs is empty.
     * @throws ZXException Thrown if checkNoArg fails
     */
    protected boolean checkNoArg(ArrayList pcolArgs) throws ZXException {
        if (!pcolArgs.isEmpty()) { throw new ZXException("No arguments are allowed for " + getClass()); }
        return true;
    }
}