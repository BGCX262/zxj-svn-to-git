/*
 * Created on Dec 23, 2003 by michael
 * $Id: Trace.java,v 1.1.2.12 2006/07/26 09:50:03 mike Exp $
 */
package org.zxframework;

import java.util.ArrayList;
import java.util.List;

import org.zxframework.logging.Log;
import org.zxframework.logging.LogFactory;
import org.zxframework.util.LocationInfo;
import org.zxframework.util.StringUtil;

/**
 * Trace object is using for Tracing method calles and getting 
 * execution times.
 * 
 * <p>
 * <B>THIS MAY BE DUE FOR A REWRITE.</B>
 * 
 * The public methods should remain the same through as it is too much of a
 * pain to refactor everything else. The reason for the change is coz we want 
 * to have as dynamic tracing as possible. Also in the future we may just
 * resort to using AOP tracing.
 * 
 * Also it is one of the first objects to be created and i got is quite wrong initially.
 * </p>
 * 
 * <pre>
 * 
 * This class is used for both error handling and application tracing.
 * Tracing relies heavily on a disciplined use of the enter and leave routine calls otherwise
 * the identation of the trace file will not be correct.
 * 
 * Here is how you should use tracing and logging. This is generated by the zx method plugin :
 *     
 *    public String peekToken(ZXCollection pcolTokens, int pintOffset) throws ZXException {
 *        String peekToken = null; 
 *        if(zx.trace.isTraceEnabled()) {
 *            zx.trace.enterMethod();
 *            zx.trace.traceParam("pcolTokens", pcolTokens);
 *            zx.trace.traceParam("pintOffset", pintOffset);
 *        }
 *        try {	            
 *            ExprToken objToken = (ExprToken)pcolTokens.get(pintOffset);
 *            peekToken = objToken.getToken();
 *            return peekToken;
 * 
 *        } catch (Exception e) {
 *            if (zx.log.isErrorEnabled()) {
 *                zx.trace.addError("Failed to : Return token from a specified position.", e);
 *                zx.log.error("Parameter : pcolTokens = "+ pcolTokens);
 *                zx.log.error("Parameter : pintOffset = "+ pintOffset);
 *            }
 *            if (zx.throwException) throw new ZXException(e);
 *            return peekToken;
 * 
 *        } finally {
 *            if(zx.trace.isTraceEnabled()) {
 *                zx.trace.returnValue(peekToken);
 *                zx.trace.exitMethod();
 *            }
 *        }
 *    }
 * 
 * Change    : BD27MAR03
 * Why       : Added support for tracingACtive flag on zX (for performance reasons)
 * 
 * Change    : DGS24NOV2003
 * Why       : In addError include the zx.actionContext in the error message.
 * 
 * Change    : DGS02FEB2004
 * Why       : Addendum to the above: only include the zx.actionContext in the error
 *             message when zX is known (with failure early on it is not known).
 * </pre>
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class Trace {
	
	/** The fully qualified class name of the Trace class. */
	private static final String FQCN = Trace.class.getName();	

	/** CONSTANTS - Trace levels **/
	private static final int APPLICATION_ACTIVE=3; // Do tracing for applications, like Taskmaster or EDT :)
	private static final int FRAMEWORK_ACTIVE=2; // Do tracing for the framework
	private static final int FAMEWORK_CORE_ACTIVE = 1; // Do tracing for the core framework
	
	/** This used for the tracing depth, this allows you to see how deep in the method calls you are. **/
	private int stackLevel = 0; // Keep track of stack level.
	private int currentTraceLevel = 3; // The current stack level.
	
	/** Whether tracing is enabled or not, default is false. **/
	private boolean active = false; 
	// private static boolean active = false; 
	
	private String userErrorAttr = "";
	private String userError = "";
	private String lastMsg = "";
	
	/** List of error built up. **/
	private List errorStack = new ArrayList();
	
	/** Last know action before the exception accured. **/
	private String aboutTo = "";
	
	/** A handle to the logger. **/
	private Log out;
	
	private ZX zx;

	/**
	 * You need zx to be able to do logging
	 */
	private Trace() {
	    super();
	}
	
	/**
	 * The Trace contructor.
	 * 
	 * @param zx
	 * @throws ZXException
	 */
	public Trace(ZX zx) throws ZXException {
		
		// Pass on the zx object.
		this.zx = zx;
		
        try {
    		// Reset Stack 
    		resetStack();
    		// Get configuration settings.
    		active = zx.isTraceActive();
    		
    		this.currentTraceLevel =  zx.getTraceLevel();
    		
    		// if tracing is active start tracing.
    		if (active) {
    			startTracing();
    		}
            
        } catch (Exception e) {
            throw new ZXException(e);
        }
	}	
	
	// ------------------------------------------------------ Getters/Setters Methods
	/**
	 * @return Returns the active.
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param blnactive The active to set.
	 */
	public void setActive(boolean blnactive) {
		active = blnactive;
	}

	/**
	 * @return Returns the currentTraceLevel.
	 */
	public int getCurrentTraceLevel() {
		return this.currentTraceLevel;
	}

	/**
	 * @param currentTraceLevel The currentTraceLevel to set.
	 */
	public void setCurrentTraceLevel(int currentTraceLevel) {
		this.currentTraceLevel = currentTraceLevel;
	}

	/**
	 * @return Returns the errorStack.
	 */
	public List getErrorStack() {
		return this.errorStack;
	}

	/**
	 * @param errorStack The errorStack to set.
	 */
	public void setErrorStack(List errorStack) {
		this.errorStack = errorStack;
	}

	/**
	 * @return Returns the lastMsg.
	 */
	public String getLastMsg() {
		return lastMsg;
	}

	/**
	 * @param pstrLastMsg The lastMsg to set.
	 */
	public void setLastMsg(String pstrLastMsg) {
		lastMsg = pstrLastMsg;
	}

	/**
	 * @return Returns the stackLevel.
	 */
	public int getStackLevel() {
		return this.stackLevel;
	}

	/**
	 * @param stackLevel The stackLevel to set.
	 */
	public void setStackLevel(int stackLevel) {
		this.stackLevel = stackLevel;
	}

	/**
	 * @return Returns the userError.
	 */
	public String getUserError() {
		return this.userError;
	}

	/**
	 * @param pstrUserError The userError to set.
	 */
	public void setUserError(String pstrUserError) {
		this.userError = pstrUserError;
	}

	/**
	 * @return Returns the userErrorAttr.
	 */
	public String getUserErrorAttr() {
		return userErrorAttr;
	}

	/**
	 * @param pstrUserErrorAttr The userErrorAttr to set.
	 */
	public void setUserErrorAttr(String pstrUserErrorAttr) {
		userErrorAttr = pstrUserErrorAttr;
	}
	
	/**
	 * Is the given trace level currently enabled?
	 *
	 * @param traceLevel is this level enabled?
	 * @return Returns whether the level is enabled.
	 */
	protected boolean isLevelEnabled(int traceLevel) {
		// trace level are numerically ordered so can use simple numeric
		// comparison
		return (traceLevel >= this.currentTraceLevel);
	}
	
	/**
	 * Is standard tracing enabled 
	 * 
	 * @return Returns true if tracing is enabled.
	 */
	public boolean isTraceEnabled() {
		return active;
	}
	
	/**
	 * Is tracing for the framework enabled. If tracing is tracing is turned off then this will return false.
	 * 
	 * @return Returns true if the framework tracing is enabled
	 */
	public boolean isFrameworkTraceEnabled() {
	    
		return active ? isLevelEnabled(Trace.FRAMEWORK_ACTIVE) : false;
		
	}
	
	/**
	 * Is tracing for the framework code enabled 
	 * 
	 * @return Returns true if the framework core tracing is enabled
	 */
	public boolean isFrameworkCoreTraceEnabled() {
	    
	    // First check if tracing if enabled first and then check if framework core tracing is enabled.
		return active ? isLevelEnabled(Trace.FAMEWORK_CORE_ACTIVE) : false;
		
	}
	
	/**
	 * Is tracing enabled for the application 
	 * 
	 * @return Returns true if the application tracing is enabled.
	 */
	public boolean isApplicationTraceEnabled() {
		return active ? isLevelEnabled(Trace.APPLICATION_ACTIVE) : false;
	}
	
	// ------------------------------------------------------------- Util methods
	
	/**
	 * Tracing for entering a routine (ie adding an entry
	 * to the trace stack)
	 * 
	 * Used when entering a method :
	 * It will increase the traceLevel and print info about the current
	 * class and method.
	 */
	public void enterMethod() {
	    
		if (active) {
			// Reset aboutTo
			setAboutTo("");

			// Get the callee
			LocationInfo loc = new LocationInfo(new Throwable(), FQCN);
			
			// And write msg
			trace("Enter " + loc.getClassName() + "." + loc.getMethodName());
			
			//Increase stack level
			this.stackLevel++;
		}
		
	}
	
	/**
	 * Call this at the end of a method  
	 *
	 * Tracing for leaving a routine (ie removing an entry
	 * to the trace stack)
	 */
	public void exitMethod() {
	    
		if (active) {
			// Get the callee
			LocationInfo loc = new LocationInfo(new Throwable(), FQCN);
			
			// Decrease stack level and safety check
			if (this.stackLevel < 1) {
				this.stackLevel = 0;
			} else {
				this.stackLevel--;
			}
			
			// And write msg
			trace("Leave " + loc.getClassName() + "." + loc.getMethodName());
		}
		
	}
	
	/**
	 * Trace a message but make it stand out! 
	 * 
	 * @param pstrMsg The message to shout.
	 */
	public void shoutMsg(String pstrMsg) {
		trace(">>>>>> " + pstrMsg);
	}
	
	/**
	 * Trace a array of method parameters 
	 * 
	 * @param name
	 * @param value
	 */
	public void traceParams(String[] name, Object[] value) {
		for (int i = 0; i < value.length; i++) {
			 traceParam(name[i],value[i]);
		}
		
	}
	
	/**
	 * Traces parameters, used for all java objects. Due to the overhead allows call traceParam.
	 * 
	 * @param name The name of the parameter to trace.
	 * @param value The value to trace.
	 */
	public void traceParam(String name, Object value) {
	    
	    StringBuffer traceParam = new StringBuffer(60);
		trace(traceParam.append("	> ").append(name).append(" = ").append(value).toString());
		
	}

	/** 
	 * Traces int method parameters
	 * 
	 * @param name The name of the parameter to trace.
	 * @param value The value to trace.
	 */
	public void traceParam(String name, int value) {
	    
	    StringBuffer traceParam = new StringBuffer(30);
		trace(traceParam.append("	> ").append(name).append(" = ").append(value).toString());
		
	}
	
	/** 
	 * Traces int method parameters
	 * 
	 * @param name The name of the parameter to trace.
	 * @param value The value to trace.
	 */
	public void traceParam(String name, double value) {
	    
	    StringBuffer traceParam = new StringBuffer(30);
		trace(traceParam.append("	> ").append(name).append(" = ").append(value).toString());
		
	}
	
	/**
	 *  Traces long method parameters
	 * 
	 * @param name The name of the parameter to trace.
	 * @param value The value to trace.
	 */
	public void traceParam(String name, long value) {
	    
	    StringBuffer traceParam = new StringBuffer(30);
		trace(traceParam.append("	> ").append(name).append(" = ").append(value).toString());
		
	}
	
	/**
	 *  Traces char method paramaters
	 * 
	 * @param name The name of the parameter to trace.
	 * @param value The value to trace.
	 */
	public void traceParam(String name, char value) {
	    
	    StringBuffer traceParam = new StringBuffer(25);
		trace(traceParam.append("	> ").append(name).append(" = ").append(value).toString());
		
	}
	
	/**
	 *  Traces boolean method paramaters
	 * 
	 * @param name The name of the parameter to trace.
	 * @param value The value to trace.
	 */
	public void traceParam(String name, boolean value) {
	    
	    StringBuffer traceParam = new StringBuffer(25);
		trace(traceParam.append("	> ").append(name).append(" = ").append(value).toString());
		
	}
	
	/**
	 * Dump what the code was about todo before is crashed
	 *
	 */
	public void traceAboutTo() {
	    
		trace("-" + this.aboutTo);
		
	}

	/**
	 * Trace what you are about to do.
	 * @param pstrAboutTo The action about to be performed.
	 */
	public void traceAboutTo(String pstrAboutTo) {
		trace("-" + pstrAboutTo);
	}	
	
	/** 
	 * Tracing a primative double.
	 * @param dble The double to trace.
	 */
	public void returnValue(double dble) {
		trace("< " + dble);
	}	
	
	/** 
	 * Tracing a primative boolean.
	 * @param bln The boolean to trace.
	 */
	public void returnValue(boolean bln) {
		trace("< " + bln);
	}	
	
	/** 
	 * 
	 * @param lng
	 */
	public void returnValue(long lng) {
		trace("< " + lng);
	}
	
	
    /**
     * @param i The primitive int value to trace.
     */
    public void returnValue(int i) {
		trace("< " + i);
    }	
	
	/** 
	 * 
	 * @param obj
	 */
	public void returnValue(Object obj) {
		trace("< " + obj);
	}
	
	/**
	 * Write debug msg
	 * 
	 * @param pstrMsg The message to trace
	 */
	public void trace(String pstrMsg) {
		if (active) {
			out.trace(System.currentTimeMillis() + " - " + indent() + pstrMsg);
		}
	}
	
	/**
	 * Similar to addError but now looks at err.description 
	 * 
	 * 
	 * @param pstrDescription This is supposed to be err.description
	 * @param e The exception
	 */
	public void handleUntrappedError(String pstrDescription, Exception e){
		// If description (which is supposed to be err.description) is not vbNullString,
		// we assume that a technical, untrapped error had occurred that we need to process
		if ( StringUtil.len(pstrDescription) > 0 ) {
			addError("Untrapped exception", pstrDescription, e);
		}
		
	}
	
	/**
	 * @param pstrDescription
	 */
	public void addError(String pstrDescription) {
        addError(pstrDescription,null,null);
	}
	
    /**
     * Helper method :
     * Use when calling a method that failed but did not return an expection.
     * 
     * @param pstrDescription The error desprition
     * @param pstrAdditionalInfo Any addition info.
     */
    public void addError(String pstrDescription, String pstrAdditionalInfo) {
		addError(pstrDescription, pstrAdditionalInfo,null);
    }
    
    /**
     * Helper method : 
     * Use when calling a method that failed but does return an exception
     * 
     * @param e The exception that causes the error
     */
    public void addError(Exception e) {
		addError(e.getMessage(), e);
    }
    
    /**
     * Helper method : 
     * Use when calling a method that failed but does return an exception
     * 
     * @param pstrDescription The description of the error :
     * @param e The exception that causes the error
     */
    public void addError(String pstrDescription, Exception e) {
        String strMessage = e instanceof ZXException || e.getCause() instanceof ZXException ? "" : e.getMessage();
        
        // The e contains the cause of the exception :
		addError(strMessage, pstrDescription, e);
    }
	
	/**
	 * Add an error entry to the error stack 
	 * 
	 * @param pstrDescription Description of error
	 * @param pstrAdditionalInfo Optional additional info
	 * @param e Exception that caused the error.
	 */
	public void addError(String pstrDescription,String pstrAdditionalInfo, Exception e){
	    
	    // Get the callee info : 
		LocationInfo loc = new LocationInfo(new Throwable(), FQCN);
		
		// Copy Values
		ErrorEntry objErrorEntry = new ErrorEntry(loc, pstrDescription, pstrAdditionalInfo, e);
        
		// And log the error
		if(zx.log.isErrorEnabled()) {
		    if (e == null) {
                // There was no exception actually thrown
		        zx.log.error(objErrorEntry.format());
		        
		    } else {
		        // Log the exception : Optional, but supplies extra error details.
		        if(e instanceof ZXException) {
		            if (((ZXException)e).isLogged()) {
		                zx.log.error(objErrorEntry.format());
		            } else {
		            	// Ensure we do not log the same exception twice.
                        ((ZXException)e).setLogged(true);
			            zx.log.error(objErrorEntry.format(false), e);
			            
		            }
                    
		        } else {
                    if (!(e.getCause() instanceof ZXException)) {
                        zx.log.error(objErrorEntry.format(), e);
                    }
                    
		        }
		        
		    }
		}
		
        // Log the exception : Optional, but supplies extra error details.
        if(e != null && (e instanceof ZXException || e.getCause() instanceof ZXException) && false) {
            // Ignore it maybe ?
            
        } else {
    		// And add to stack
    		this.errorStack.add(objErrorEntry);
        }
        
		// Trace the error if tracing is turned on.
		if (active) {
		    trace("Error " + objErrorEntry.format());
		}
		
		// And set the most recent error message
		if(pstrAdditionalInfo != null) {
			setLastMsg(pstrDescription + " [" + pstrAdditionalInfo + "]");
		} else {
			setLastMsg(pstrDescription);
		}
	}
	
	/**
	 * Add an error entry to the error stack after emptying the error stack 
	 * 
	 * @param pstrDescription Description of error
	 */
	public void setError(String pstrDescription){
		setError(pstrDescription,null);	
	}
	
	/**
	 * Add an error entry to the error stack after emptying the error stack 
	 * 
	 * @param pstrDescription Description of error
	 * @param pstrAdditionalInfo Optional additional info
	 */
	public void setError(String pstrDescription,String pstrAdditionalInfo) {
		// Reset the stack
		resetStack();
		
		// And add the error
		addError(pstrDescription,pstrAdditionalInfo);
	}
	
	/**
	 * Return the error stack in human readable form 
	 * 
	 * @param pblnFull Full - Full or short dump
	 * @return Returns a formatted string of the error stack.
	 */
	public String formatStack(boolean pblnFull) {
		StringBuffer formatStack = new StringBuffer();
		
		if( StringUtil.len(getUserError()) > 0 ) {
			formatStack.append(userErrorFormat());
		}
		
		if( StringUtil.len(getUserError()) == 0 ) {
			int e = getErrorStack().size();
			for (int i = 0; i < e; i++) {
			    String strMsg =  ((ErrorEntry)getErrorStack().get(i)).format(pblnFull);
			    if (StringUtil.len(strMsg) > 0) {
			        
			        if (formatStack.length() > 0) {
						formatStack.append("\n\r");
			        }
			        
			        formatStack.append(strMsg);    
			    }
			}
		}
		
		return formatStack.toString();
	}
	
	/**
	 * return user error as formatted string 
	 * 
	 * @return Returns a formatted error string.
	 */
	public String userErrorFormat(){
	    
		String userErrorFormat = getUserError();
		if( StringUtil.len(userErrorFormat) == 0 ) {
			userErrorFormat = formatStack(false);
		}
		return userErrorFormat;
		
	}
	
	/**
	 * Returns a string of the indent level 
	 * 
	 * @return A padded string by the stack level.
	 */
	public String indent() {
	    char indent[] = new char[this.stackLevel];
	    for (int i = 0; i < this.stackLevel; i++) {
            indent[i] = ' ';
        }
	    return new String(indent);
	}

	/**
	 * Reset the error stack.
	 */
	public void resetStack() {
		// Simply create a new collection (ArrayList)
		setErrorStack(new ArrayList());
		// Also reset user error
		setUserError("");
		setUserErrorAttr("");
	}
	
	/**
	 * Add string to the user error
	 * 
	 * @param pstrMsg Message to add
	 * @see Trace#userErrorAdd(String, String, String)
	 */
	public void userErrorAdd(String pstrMsg) {
		userErrorAdd(pstrMsg,null,null);
	}
	
	
	/**
	 * Add string to the user error 
	 * 
	 * This is used for the form processing, this 
	 * @param pstrMsg Message to add
	 * @param pstrAdditionalInfo Optional parameter, default to be null
	 * @param pstrAttr Optional parameter, default to be null
	 */
	public void userErrorAdd(String pstrMsg,String pstrAdditionalInfo,String pstrAttr){
	    
	    /**
	     * Only add to the error messages if there is one.s
	     */
		if( StringUtil.len(pstrMsg) > 0 ) {
		    StringBuffer userErrorAdd = new StringBuffer(StringUtil.len(getUserError()) == 0 ? "" : getUserError());
		    
		    /**
		     * Add a new line if there is a error message already
		     */
			if( StringUtil.len(getUserError()) > 0 ) {
				userErrorAdd.append("\n\r");
			}
			
			// Add the actual error message
			userErrorAdd.append(pstrMsg);
			
			/**
			 * Add in any addtional information
			 */
			if(StringUtil.len(pstrAdditionalInfo) > 0) {
				userErrorAdd.append(" [").append(pstrAdditionalInfo).append("]");
			}
			
			/**
			 * The attribute related to the error, do not add if null.
			 */
			if (StringUtil.len(getUserError()) == 0 && StringUtil.len(pstrAttr) > 0) {
				setUserErrorAttr(pstrAttr); 
			}
			
			setUserError(userErrorAdd.toString());
			
		}
		
	}
	
	/**
	 * Reset usererror 
	 */
	public void userErrorReset() {

		setUserError("");
		setUserErrorAttr("");
		
		//Also reset error stack
		resetStack();
	}
	
	/**
	 * Start tracing 
	 */
	public void startTracing(){
	    
		if (out == null ){
			out = LogFactory.getLog("Tracing");
		}
		
	}
	
	/**
	 * Stop tracing 
	 */
	public void stopTracing(){	
		// does nothing here for now, but should do the logging clean up stuff here.
	}	
	/**
	 * @return Returns the aboutTo.
	 */
	public String getAboutTo() {
		return this.aboutTo;
	}

	/**
	 * @param aboutTo The aboutTo to set.
	 */
	public void setAboutTo(String aboutTo) {
		this.aboutTo = aboutTo;
	}
}