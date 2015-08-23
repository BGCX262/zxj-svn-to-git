/*
 * Created on Jan 13, 2004 by Michael Brewer
 * $Id: NoOpLog.java,v 1.1.2.2 2006/07/17 16:18:08 mike Exp $
 */
package org.zxframework.logging.impl;

import org.zxframework.logging.Log;

/**
 * <p>Trivial implementation of Log that throws away all messages.  No
 * configurable system properties are supported.</p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @version 0.0.1
 */
public final class NoOpLog implements Log {

    /**
     * Default constructor.
     */
    public NoOpLog() { 
    	super();
    }
    
    /**
     * @param name The name of the logger.
     */
    public NoOpLog(String name) {
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#trace(Object)
     **/
    public void trace(Object message) { 
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#trace(Object, Throwable)
     **/
    public void trace(Object message, Throwable t) { 
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /**
     * @see Log#debug(Object)
     **/
    public void debug(Object message) { 
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#debug(Object, Throwable)
     **/
    public void debug(Object message, Throwable t) {
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#info(Object)
     **/
    public void info(Object message) {
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#info(Object, Throwable)
     **/
    public void info(Object message, Throwable t) {
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#warn(Object)
     **/
    public void warn(Object message) {
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#warn(Object, Throwable)
     **/
    public void warn(Object message, Throwable t) {
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#error(Object)
     **/
    public void error(Object message) {
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#error(Object, Throwable)
     **/
    public void error(Object message, Throwable t) {
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#fatal(Object)
     **/
    public void fatal(Object message) {
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /** 
     * @see Log#fatal(Object, Throwable)
     **/
    public void fatal(Object message, Throwable t) {
    	/**
    	 * Nothing happends here
    	 */
    }
    
    /**
     * @see Log#isDebugEnabled()
     **/
    public final boolean isDebugEnabled() { return false; }
    
    /** 
     * @see Log#isErrorEnabled()
     **/
    public final boolean isErrorEnabled() { return false; }
    
    /** 
     * @see Log#isFatalEnabled()
     **/
    public final boolean isFatalEnabled() { return false; }
    
    /**
     * @see Log#isInfoEnabled()
     */
    public final boolean isInfoEnabled() { return false; }
    
    /** 
     * @see Log#isTraceEnabled()
     **/
    public final boolean isTraceEnabled() { return false; }
    
    /**
     * @see Log#isWarnEnabled()
     */
    public final boolean isWarnEnabled() { return false; }
    
	/**
	 * @see Log#getLoglevel()
	 **/
	public int getLoglevel() { return 0; }
	
	/** 
	 * @see Log#setLogLevel(int)
	 **/
	public void setLogLevel(int level) {
		// Not implemented.
	}
}