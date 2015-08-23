/*
 * Created on Mar 12, 2004 by Michael Brewer
 * $Id: DirectorFunction.java,v 1.1.2.5 2005/08/30 08:42:44 mike Exp $
 */
package org.zxframework.director;

import java.util.ArrayList;

import org.zxframework.Tuple;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.util.StringUtil;

/**
 * A framework director function.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DirectorFunction extends ZXObject {
    
    //------------------------ Members
    
    /** The remainder of the function argument **/
    private String remainder;
    /** The function name. **/
    private String func;
    /** An collection (Tuple) of arguments **/
    private ArrayList args;
    
    //------------------------ Constructors 
    
    /**
     * Default constructor.
     * 
     * NOTE : This will call reset() to init some values
     */
    public DirectorFunction() {
        super();
        
        /**
         * And reset.
         */
        reset();
    }
    
    //------------------------ Getters and Setters

    /**
     * A collection (Tuple) of arguments
     * 
     * @return Returns the args.
     */
    public ArrayList getArgs() {
        return args;
    }
    
    /**
     * @param pcolArgs The args to set.
     */
    public void setArgs(ArrayList pcolArgs) {
        this.args = pcolArgs;
    }
    
    /**
     * The name of the function that needs to be executed.
     * 
     * @return Returns the func.
     */
    public String getFunc() {
        return func;
    }
    
    /**
     * @param func The func to set.
     */
    public void setFunc(String func) {
        this.func = func;
    }
    
    /**
     * Remainder of function without function name
     * 
     * @return Returns the remainder.
     */
    public String getRemainder() {
        return remainder;
    }
    
    /**
     * @param remainder The remainder to set.
     */
    public void setRemainder(String remainder) {
        this.remainder = remainder;
    }
    
    //------------------------ Public Functions
    
    /**
     * Reset the director function.
     */
    public void reset() {
        this.func = "";
        this.args = new ArrayList();
    }
    
    /**
     * Parse a function into ints components.
     * 
     * <pre>
     * 
     * Assumes   :
     * 	Function is 'clean', i.e. does only
     * 	contain function name and arguments,
     * 	no stuff like embedded '#' and '<'
     * </pre>
     *
     * @param pstrFunc The function for perform
     * @throws ZXException Thrown if parse fails. 
     */
    public void parse(String pstrFunc) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrFunc", pstrFunc);
        }

        try {
            
            /**
             * Check that left most character is a '#'
             */
            if (pstrFunc.charAt(0) != '#') {
                throw new Exception("Check that left most character is a '#' :"  + pstrFunc);
            }
            
            /**
             * Righmost character may be a '#' (in case of an instring director)
             * Just truncate this as we do not need it
             */
            if(pstrFunc.endsWith("#") && pstrFunc.length() > 1) {
                pstrFunc = pstrFunc.substring(0, pstrFunc.length()-1);
            }
            
            
            /**
             * Skip first '#' as this makes tokenizing a bit easier
             */
            String strToken = "#";
            
            char[] strChars = pstrFunc.toCharArray();
            for (int i = 1; i < strChars.length; i++) {
                char c = strChars[i];
                
                if(c == '.' || c == '¬') {
                    if(StringUtil.len(this.func) == 0) {
                        /**
                         * Set function 
                         */
                        this.func = strToken;
                        
                        /**
                         * And remainder from the current postion + 1 onwards. 
                         */
                        this.remainder = pstrFunc.substring(i+1);
                        
                        /**
                         * This is a bit ugly but the #expr function gets a special
                         *  treatment as the parameter can contain all sorts of characters
                         */
                        if (this.func.equalsIgnoreCase("#expr")) {
                        	strToken = "";
                            break;
                        }
                        
                        strToken = ""; 
                        
                    } else {
                        /**
                         * Must then be an arguement
                         */
                        Tuple objTuple = new Tuple();
                        objTuple.setName(strToken);
                        objTuple.setValue(strToken);
                        getArgs().add(objTuple);
                        
                        strToken = "";
                        
                    }
                } else {
                    strToken = strToken + c;
                }
            }
            
            if(StringUtil.len(strToken) > 0) {
            	/** If the func is not set use the strToken. ie: no parameters. **/
                if(StringUtil.len(this.func) == 0) {
                    this.func = strToken;
                } else {
                	/** Add the last arguement. **/
                    Tuple objTuple = new Tuple();
                    objTuple.setName(strToken);
                    objTuple.setValue(strToken);
                    getArgs().add(objTuple);
                }
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Parse a function into ints components", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrFunc = "+ pstrFunc);
            }
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get a numbered argument.
     *
     *<pre>
     *
     * NOTE : 0 is the first element.
     * </pre>
     *
     * @param pintNumber The position in the collection to get the arg 
     * @return Returns the args at a specfic position.
     */
    public String getArg(int pintNumber) {
        String getArg = "";
        
    	if (getArgs() != null) {
    		Tuple objTuple = ((Tuple)getArgs().get(pintNumber));
    		if (objTuple != null) {
    			getArg = objTuple.getValue();
    		}
    	}
    	
        return getArg;
    }
    
    /**
     * Get number of arguments.
     *
     * @return Returns the number of arguments for the Director
     */
    public int numArgs() {
        int numArgs = 0;
        
        if (getArgs() != null) {
        	numArgs = getArgs().size();
        }
        
        return numArgs;
    }
    
    //------------------------ Object overridden methods.
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer toString = new StringBuffer();
        toString.append(this.func);
        
        if  (this.remainder != null) {
            toString.append(".").append(this.remainder);
        }
        
        return toString.toString();
    }
}