/*
 * Created on Mar 12, 2004 by Michael Brewer
 * $Id: DirectorHandler.java,v 1.1.2.10 2006/07/17 16:38:21 mike Exp $
 */
package org.zxframework.director;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.zxframework.ZXCollection;
import org.zxframework.ZXException;
import org.zxframework.ZXObject;
import org.zxframework.zXType;
import org.zxframework.property.Property;
import org.zxframework.util.StringUtil;

/**
 * The framework director handler.
 * 
 * <pre>
 * 
 * Who    : Bertus Dispa
 * When   : 11 May 2003
 * 
 * Change    : BD20MAY03
 * Why       : Fixed problem with getToken; had to introduce byRef
 *             pintToken to see up to where we have tokenized
 * 
 * Change    : BD21JAN04
 * Why       : Added support for configurable function handlers (read from
 *              the configuration file)
 * 
 * Change    : BD23FEB04
 * Why       : - Handle #eval as a special case to avoid re-parsing parameter several times
 *             - Improved performance by maintaining cache of tokenized directors
 * 
 * Change    : BD14MAY04
 * Why       : Fixed bug in parsing nested directors
 *  
 * Change    : V1.4:74 - BD6MAY05
 * Why       : Fixed bug when trying to resolve plain strings as
 *             directors; no longer do a true resolve as this
 *             can cause funny results. The system will store
 *             parsed directors in the cache and use the director
 *             as the key. Because the key is case-insensitive
 *             it will result in 'directors' 'Back' and 'back' to be
 *             treated as the same thing ('Back' or 'back' which-ever was
 *             resolved first!)
 * 
 * Change    : V1.5:58 - BD15SEP05
 * Why       : A director of type #expr is no longer treated as a director;
 *             see comment in resolve
 * 
 * Change    : BD1JUN06 - V1.5:97
 * Why       : Get-token is changed; it now supports escaped characters (ie following a '\')
 *             in sub-director; Be vigilant as this could be a tricky change with consequences
 *               
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DirectorHandler extends ZXObject {

    //------------------------ Members
    
    private boolean handlersLoaded = false;
    
    private ZXCollection fh;
    private ZXCollection parsedDirectors;
    
    private zXType.drctrNextToken enmNextToken = zXType.drctrNextToken.dntUnSpecified;
    private int intPosition = 0;
    
    //------------------------ Constants
    
    private static final int IN_NORMAL = 0;
    private static final int IN_FUNCTION = 1;
    private static final int IN_ESCAPE = 2;
    private static final int IN_SUBDIRECTOR = 3;
    
    //------------------------ Construtors
    
    /**
     * Default constructor.
     */
    public DirectorHandler() {
        super();
    }
    
    //------------------------ Getters and Setters
    
    /**
     * Indicates whether the handlers have been initialised and loaded.
     * 
     * @return Returns the handlersLoaded.
     */
    public boolean isHandlersLoaded() {
        return handlersLoaded;
    }
    
    /**
     * @param blnHandlersLoaded The blnHandlersLoaded to set.
     */
    public void setHandlersLoaded(boolean blnHandlersLoaded) {
        this.handlersLoaded = blnHandlersLoaded;
    }
    
    /**
     * Collection fo function handlers.
     * 
     * @return Returns the fh.
     */
    public ZXCollection getFh() {
        return fh;
    }
    
    /**
     * @param fh The fh to set.
     */
    public void setFh(ZXCollection fh) {
        this.fh = fh;
    }

    /**
     * @return Returns the parsedDirectors.
     */
    public ZXCollection getParsedDirectors() {
        if (this.parsedDirectors == null) {
            this.parsedDirectors = new ZXCollection();
        }
        return parsedDirectors;
    }
    
    /**
     * @param parsedDirectors The parsedDirectors to set.
     */
    public void setParsedDirectors(ZXCollection parsedDirectors) {
        this.parsedDirectors = parsedDirectors;
    }
    
    //------------------------ Public methods 
    
    /**
     * Register a director function handler.
     *
     * @param pstrName Name given to the function handler 
     * @param pobjFH The function handler 
     */
    public void registerFH(String pstrName, DrctrFH pobjFH) {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrName", pstrName);
            getZx().trace.traceParam("pobjFH", pobjFH);
        }
        try {
        	
        	/** Create a new collection when needed. **/
            if (fh == null) {
                this.fh = new ZXCollection();
            }
            
            this.fh.put(pstrName, pobjFH);
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Reset the function handler collection.
     */
    public void resetFH() {
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        try {
            
            setFh(new ZXCollection());
            
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Resolve a director.
     * 
     * <pre>
     * 
     * This is what it is all about: resolving a director!
     * 
     * Note the special characters:
     * 	\   escape next character from its special meaning
     * 	#  Start / end director function
     * 	<  Start sub director
     * 	 >  End sub director
     * 	¬  Seperate director function arguments
     *</pre>
     *
     * @param pstrDirector The director to resolve 
     * @return Returns the result of an director function 
     * @throws ZXException Thrown if resolve fails. 
     */
    public String resolve(String pstrDirector) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDirector", pstrDirector);
        }

        String resolve = null;
        
        try {
            
            if(StringUtil.len(pstrDirector) > 0) {
                /**
                 * V1.4:74 - When string does not have any special characters
                 * it will just be a plain string and does not need resolving
                 */
                if (!StringUtil.containsAny("<#", pstrDirector)) {
                    resolve = pstrDirector;
                    
                } else {
                    /**
                     * There is a special case for the #eval director: when a director statrs with #eval,
                     * we have to take the remainder and evaluate it as a proper director; this is
                     * done as a special case as otherwise we would parse the remainder twice: once
                     * to find out that it starts with the #eval function and the second time around
                     * when actually evaluating the remainder. If we do not treat this as a special case
                     * we end up having to escape all special director characters 2 or 3 times over!
                     * 
                     * The other exception that we support is #expr.; if a developer uses
                     * this director, we can safely (well, we hope) assume that the remainder is
                     * an expression and does not have to be evaluated first as being a director
                     * with <#qs.-someKey> constructs nested.
                     * Originally we did treat it as a director before treating it as an expression
                     * but this caused great difficulties when using #expr. to construct a
                     * complex where condition that contained characters like < and > (for
                     * lt and gt).
                     * If you want to nest expressions and directors, use something like:
                     * #expr.director()
                     */
                    if(pstrDirector.startsWith("#eval.")) {
                        resolve = internalResolve(pstrDirector.substring(6));
                        
                    } else if(pstrDirector.startsWith("#expr.")) {
                    	/**
                    	 * Parse explicitly as an expression
                    	 */
                    	Property objProperty = getZx().getExpressionHandler().eval(pstrDirector.substring(6));
                    	if (objProperty == null) {
                    		getZx().trace.addError("Unable to evaluate expression for #expr director", pstrDirector.substring(6));
                    		return pstrDirector;
                    	} // Unable to evaluate expression
                    	
                    	resolve = objProperty.getStringValue();
                    	
                    } else {
                        resolve = internalResolve(pstrDirector);
                        
                    } // Could it be #eval?
                    
                } // Contains #?
                
            } // Not empty string?
            
            return resolve;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Resolve a director", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrDirector = "+ pstrDirector);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return resolve;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(resolve);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Internal resolve.
     * 
     * <pre>
     * 
     * NOTE : This calls internalResolve(pstrDirector, false);
     * </pre>
     * 
     * @param pstrDirector The director text 
     * @return Returns director
     * @throws ZXException
     */
    private String internalResolve(String pstrDirector) throws ZXException {
        return internalResolve(pstrDirector, false);
    }
    
    /**
     * Internal resolve.
     * 
     * Same as resolve but with an additional parameter that indicates 
     * whether we are in sub-director mode
     *
     * @param pstrDirector The director text 
     * @param pblnSubDirector Optional, default is false 
     * @return Returns director 
     * @throws ZXException Thrown if internalResolve fails. 
     */
    private String internalResolve(String pstrDirector, boolean pblnSubDirector) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDirector", pstrDirector);
            getZx().trace.traceParam("pblnSubDirector", pblnSubDirector);
        }

        String internalResolve = null;
        
        try {
            // Reset the position to zero.
            this.intPosition = 0;
            String strToken = "";
            
            /**
             * Check if we already have tokenized director in cache; otherwise
             * tokenize and add to cache
             */
            DirectorToken objToken;
            String strTmp;
            
            List colTokens = (ArrayList)getParsedDirectors().get(pstrDirector);
            if (colTokens == null) {
                // The director to resolve
                strTmp = pstrDirector;
                
                colTokens = new ArrayList(); // Create a new one for this director
                strToken = getToken(pstrDirector); // This will effect intPosition and enmNextToken
                
                while(StringUtil.len(strToken) >0) {
                    objToken = new DirectorToken();
                    
                    objToken.setToken(strToken);
                    objToken.setPosition(this.intPosition);
                    objToken.setNextToken(this.enmNextToken);
                    
                    colTokens.add(objToken); // Add this token to the collection
                    
                    strTmp = strTmp.substring(this.intPosition); // shorten the director
                    strToken = getToken(strTmp); // try to get more token
                }
                
                getParsedDirectors().put(pstrDirector, colTokens); // Save the tokens for this director
            }
            
            String strReturn = "";
            String strFunction = "";
            
            int intState = IN_NORMAL;
            
            /**
             * Bit tricky: if the very first character is a '#', we treat
             * it the same as if the whole expression was enclosed within
             * '<' and '>'. Image the following director:
             * #expr.concat('TMS', format(<#qs.-pk>, '00000'))
             * You would expect that this works but syntactically it is wrong
             * as you cannot simply nest director functions without using '<' and '>'
             * thats why we make an exception when the director starts with a director
             * function
             */
            if(pstrDirector.charAt(0) == '#') {
                pblnSubDirector = true;
            }
            
            // The length of the token to parse.
            int intToken;
            
            Iterator iter = colTokens.iterator();
            while(iter.hasNext()) {
                objToken = (DirectorToken)iter.next();
                
                this.enmNextToken = objToken.getNextToken();
                strToken = objToken.getToken();
                intToken = strToken.length();
                
                /**
                 * The first character of the token dictates what happens next:
                 * # - Function
                 * < - sub director
                 * Else - nothing special
                 */
                char c = strToken.charAt(0);
                
                switch (intState) {
                    
                    case IN_NORMAL:
                        switch (c) {
                            /**
                             * Two scenarios here: if the next token is not
                             * a sub-director, we can simply evaluate the director
                             * and add the result to the output.
                             * If the next token is a sub-director, we first have
                             * to evaluate that and add it to the function before we
                             * can evaulate the function
                             */
                            case '#':
                                if (enmNextToken.equals(zXType.drctrNextToken.dntSubDirector)) {
                                    strFunction = strToken;
                                    intState = IN_FUNCTION;
                                } else {
                                    /**
                                     * Evaluate the function
                                     */
                                    strReturn = strReturn + evalDirectorFunction(strToken);
                                }
                                break;

                            /**
                             * If the sub-director is valid (i.e. starts with '<' and
                             * ends with '>', than we can simply recursively call this
                             * function with the string between the '<' and the '>'
                             */
                            case '<':
                                if (StringUtil.len(strToken) > 2) {
                                    
                                    if (strToken.charAt(0) == '<' && strToken.charAt(intToken-1) == '>') {
                                        strTmp = strToken.substring(1, intToken-1); // Chop off ><
                                        
                                        /**
                                         * Call recursively but make known to system that we are
                                         * in sub-director mode
                                         */
                                        strReturn = strReturn + internalResolve(strTmp, true);
                                        
                                    } else {
                                        throw new Exception("Invalid syntax for sub-director. Does not start with < and end with > : " + strToken);
                                    }
                                    
                                } else {
                                    throw new Exception("Invalid syntax for sub-director. It is to short: " + strToken);
                                }
                                break;

                            /**
                             * Token does not start with '<' or '#'
                             * Simply add to output
                             */
                            default:
                                strReturn = strReturn + strToken;
                                break;
                        }
                        break;

                    case IN_FUNCTION:
                        switch (c) {
                            /**
                             * Add the result of the sub-director to the function
                             * Syntax : <SUB_DIRECTER>
                             */
                            case '<':
                                if (StringUtil.len(strToken) > 2) {
                                    
                                    if (strToken.charAt(0) == '<' && strToken.charAt(intToken-1) == '>') {
                                        strTmp = strToken.substring(1, intToken-1); // Chop off ><
                                        
                                        /**
                                         * Call recursively but make known to system that we are
                                         * in sub-director mode
                                         */
                                        strFunction = strFunction + internalResolve(strTmp, true);
                                    } else {
                                        throw new Exception("Invalid syntax for sub-director. Does not start with < and end with > : " + strToken);
                                    }
                                    
                                } else {
                                    throw new Exception("Invalid syntax for sub-director. It is to short: " + strToken);
                                }
                                break;
                                
                            /**
                             * Add token to function
                             */
                             default:
                                 strFunction = strFunction + strToken;
                                 break;
                        }
                        
                        /**
                         * If we are called in recursive mode; we are NOT done yet
                         * otherwise we are
                         */
                        if(!pblnSubDirector) {
                            /**
                             * Now we can evaluate the director function and add
                             * the result to the output
                             */
                            strReturn = strReturn + evalDirectorFunction(strFunction);
                            
                            strFunction = "";
                            intState = IN_NORMAL;
                        }
                        break;
                }
            }
            
            /**
             * When we have hit end-of-string, we may have just finished a function
             */
            if (StringUtil.len(strFunction) > 0 && intState == IN_FUNCTION) {
                strReturn = strReturn + evalDirectorFunction(strFunction);
            }
            
            internalResolve = strReturn;
            
            return internalResolve;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Internal resolve.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrDirector = "+ pstrDirector);
                getZx().log.error("Parameter : pblnSubDirector = "+ pblnSubDirector);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return internalResolve;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(internalResolve);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Evaluate a director function. 
     * 
     * This will start with a '#'
     *
     * @param pstrFunction Function to eval 
     * @return Return the evaluate DirectorFunction
     * @throws ZXException Thrown if evalDirectorFunction fails. 
     */
    public String evalDirectorFunction(String pstrFunction) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrFunction", pstrFunction);
        }

        String evalDirectorFunction = null;
        
        try {
            String strResult = "";
            
            /**
             * Parse the director
             */
            DirectorFunction objFunc = new DirectorFunction();
            objFunc.parse(pstrFunction);
            
            /**
             * Load the handlers if needed
             */
            if (!isHandlersLoaded()) {
                loadHandlers();
                setHandlersLoaded(true);
            }
            
            /**
             * We basically loop over the various handlers until one has picked the
             * function up
             */
            if (fh == null) {
                this.fh = new ZXCollection();
            }
            Iterator iter = this.fh.iterator();
            
            DrctrFH objHandler;
            while (iter.hasNext()) {
                objHandler = (DrctrFH) iter.next();
                
                objHandler.setTouched(false);
                strResult = objHandler.resolve(objFunc);
                
                if (objHandler.isTouched()) {
                    evalDirectorFunction = strResult;
                    break;
                }
                
                /**
                 * VB CODE :
                 * 
                 	strResult = objHandler.resolve(objFunc, blnTouched)
			        If blnTouched Then
			            evalDirectorFunction = strResult
			            Exit For
			        End If
		         *
                 */
            }
            
            return evalDirectorFunction;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Evaluate a director function.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrFunction = "+ pstrFunction);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return evalDirectorFunction;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(evalDirectorFunction);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Get the next token from a director string. 
     * 
     * <pre>
     * 
     * The next token byref parameter returns what the next  token is going to be
     * 
     * BD1JUN06 - V1.5:97
     * </pre>
     *
     * @param pstrDirector The director string
     * @return Returns the token for the director.
     * @throws ZXException Thrown if getToken fails. 
     */
    public String getToken(String pstrDirector) throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
            getZx().trace.traceParam("pstrDirector", pstrDirector);
        }

        String getToken = null;
        
        try {
            int intSubLevel =0;
            int intLastState = 0;
            String strReturn = "";
            
            boolean blnEndParsing = false;
            
            int intState = IN_NORMAL;
            char[] strChars = pstrDirector.toCharArray();
            int i = 0;
            
            doneParsing : for (i=0; i < strChars.length;) {
                char strChar = strChars[i];
                
                switch (intState) {
                    case IN_NORMAL:
                        switch (strChar) {
                            case '\\':
                                /**
                                 * Tell system that we are in escape mode
                                 */
                                intLastState = IN_NORMAL;
                                intState = IN_ESCAPE;
                                break;
                                
                            case '<':
                                /**
                                 * Inidicates start of sub director; this may be the end
                                 * of the current token (if we already have parsed something)
                                 */
                                if(StringUtil.len(strReturn) == 0) {
                                    strReturn = strReturn + strChar;
                                    intState = IN_SUBDIRECTOR;
                                    
                                    /**
                                     * BD14MAY04 Serious bug fix: start at sublevel 1
                                     */
                                    intSubLevel = 1;
                                    
                                } else {
                                    this.enmNextToken = zXType.drctrNextToken.dntSubDirector;
                                    /**
                                     * Need to add 1 to j in order to keep track of the number
                                     * of characters that we have processed 
                                     */
                                    // j = j + 1 // Commented out in VB version as well.
                                    
                                    // Same as GoTo doneParsing
                                    blnEndParsing =true;
                                    break doneParsing;
                                }
                                break;
                                
                            case '#':
                                /**
                                 * Inidicates start of director function; this may be the end 
                                 * of the current token (if we already have parsed something)
                                 */
                                if (StringUtil.len(strReturn) == 0) {
                                    intState = IN_FUNCTION;
                                    strReturn = strReturn + strChar;
                                } else {
                                    enmNextToken = zXType.drctrNextToken.dntFunction;
                                    
                                    // Same as GoTo doneParsing
                                    blnEndParsing = true;
                                    break doneParsing;
                                }
                                break;
                                
                            default:
                                /**
                                 * Simply stick to end of what we are going to return
                                 */
                                strReturn = strReturn + strChar;
                                break;
                                
                        }
                        break;
                        
                    case IN_ESCAPE:
                        /**
                         * We are in escape mode; i.e. no characters have any special meaning
                         * and should thus be added to the output string and we go back to
                         * the state that we were in when we encountered the '\' character
                         */
                        strReturn = strReturn + strChar;
                        intState = intLastState;
                        break;
                        
                    case IN_FUNCTION:
                        switch (strChar) {
                            case '\\':
                                /**
                                 * Tell system that we are in escape mode
                                 */
                                intLastState = IN_FUNCTION;
                                intState = IN_ESCAPE;
                                break;
                                
                                
                            case '#':
                                /**
                                 * Indicates the end of the function (traditional in-string director)
                                 */
                                strReturn = strReturn + strChar;
                                enmNextToken = zXType.drctrNextToken.dntUnSpecified;
                                
                                /**
                                 * Need to add 1 to j in order to keep track of the number
                                 * of characters that we have processed
                                 */
                                i++;
                                
                                // Same as GoTo doneParsing
                                blnEndParsing = true;
                                break doneParsing;
                                
                            case '>':
                                /**
                                 * This is never good news when we do not expect it
                                 */
                                throw new Exception("Did not expect end-of-subdirector at position : " + i);
                                
                            case '<':
                                /**
                                 * Indicates the start of a sub-director (and thus end of this token)
                                 */
                                enmNextToken = zXType.drctrNextToken.dntSubDirector;
                                /**
                                 * Need to add 1 to j in order to keep track of the number 
                                 * of characters that we have processed
                                 */
                                // i++; // Commented out in the VB versoin
                                
                                // Same as GoTo doneParsing
                                blnEndParsing = true;
                                break doneParsing; 
                                
                            /**
                             * Simply stick to end of what we are going to return
                             */
                            default:
                                strReturn = strReturn + strChar;
                                break;
                                
                        }
                        break;
                        
                    case IN_SUBDIRECTOR:
                        switch (strChar) {
                            case '<':
                                /**
                                 * Indicates the start of next-level sub-director
                                 */
                                strReturn = strReturn + strChar;
                                intSubLevel++;
                                break;

                            case '>':
                                /**
                                 * Only ends this token if this is the > associated with the
                                 * < that started this token
                                 */
                                strReturn = strReturn + strChar;
                                
                                if (intSubLevel > 0) {
                                    intSubLevel--;
                                    
                                    if (intSubLevel == 0) {
                                        /**
                                         * BD14MAY04 - Serious bug fix: when the sublevel
                                         * is back to 0, it means end of token
                                         */
                                        enmNextToken = zXType.drctrNextToken.dntUnSpecified;
                                        
                                        /**
                                         * Need to add 1 to j in order to keep track of the number
                                         * of characters that we have processed
                                         */
                                        i++;
                                        
                                        // Same as GoTo doneParsing
                                        blnEndParsing = true;
                                        break doneParsing;
                                        
                                        // intState = IN_NORMAL; // Commented out in the VB versoin                               
                                    }
                                    
                                } else {
                                    enmNextToken = zXType.drctrNextToken.dntUnSpecified;
                                    /**
                                     * Need to add 1 to j in order to keep track of the number
                                     * of characters that we have processed
                                     */
                                    i++;
                                    
                                    // Same as GoTo doneParsing
                                    blnEndParsing = true;
                                    break doneParsing;
                                }
                                break;
                                
                            case '\\':
                                /**
                                 * BD1JUN06 Very serious change: if we see a '\' in subdir mode, we need to ignore
                                 * the next character; normally we would not include the actual '\' in the strReturn
                                 * but in subdir mode, it means that the strReturn will later be interpreted as a
                                 * director (ie again goes through this code). This means that dropping the '\' would
                                 * invalidate the director...
                                 * 
                                 * Tell system that we are in escape mode 
                                 */
                                strReturn = strReturn + strChar;
                                intLastState = IN_SUBDIRECTOR;
                                intState = IN_ESCAPE;
                                break;
                                
                            default:
                                strReturn = strReturn + strChar;
                                break;
                        }
                        break;
                }
                
                i++;
            }
            
            if (!blnEndParsing) {
                /**
                 * We have reached the end of the string; this is not good we were expecting
                 * the close marker of a sub director
                 */
                if (intState == IN_SUBDIRECTOR) {
                    throw new Exception("End-of-string encountered when expecting close of sub-director");
                }
                
                this.enmNextToken = zXType.drctrNextToken.dntEndOfString;
            }
            
//--------------------------
// doneParsing: ------------
//--------------------------
            getToken = strReturn;
            this.intPosition = i;
//--------------------------
// doneParsing: ------------
//--------------------------
            
            return getToken;
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Get the next token from a director string.", e);
            if (getZx().log.isErrorEnabled()) {
                getZx().log.error("Parameter : pstrDirector = " + pstrDirector);
            }
            
            if (getZx().throwException) throw new ZXException(e);
            return getToken;
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.returnValue(getToken);
                getZx().trace.exitMethod();
            }
        }
    }
    
    /**
     * Load the handlers set in the zX config files.
     *
     * @throws ZXException  Thrown if loadHandlers fails. 
     */
    public void loadHandlers() throws ZXException{
        if(getZx().trace.isFrameworkCoreTraceEnabled()) {
            getZx().trace.enterMethod();
        }

        try {
        	
            Element objXMLNode = getZx().configXMLNode("//drctrFH");
            if (objXMLNode != null) {
                Element element;
                Iterator iter = objXMLNode.getChildren().iterator();
                while (iter.hasNext()) {
                    element = (Element)iter.next();
                    
                    String strName = element.getChildText("name");
                    String strHandler = element.getChildText("object");
                    DrctrFH objHandler = (DrctrFH)getZx().createObject(strHandler);
                    
                    registerFH(strName, objHandler);
                }
            }
            
        } catch (Exception e) {
            getZx().trace.addError("Failed to : Load the handlers", e);
            
            if (getZx().throwException) throw new ZXException(e);
        } finally {
            if(getZx().trace.isFrameworkCoreTraceEnabled()) {
                getZx().trace.exitMethod();
            }
        }
    }
}