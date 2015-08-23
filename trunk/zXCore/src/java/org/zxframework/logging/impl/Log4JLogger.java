/*
 * Created on Jan 13, 2004 by Michael Brewer
 * $Id: Log4JLogger.java,v 1.1.2.2 2006/07/17 16:18:08 mike Exp $
 */
package org.zxframework.logging.impl;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.zxframework.logging.Log;

/**
 * <p>Implementation of {@link Log} that maps directly to a Log4J
 * <strong>Logger</strong>.  Initial configuration of the corresponding
 * Logger instances should be done in the usual manner, as outlined in
 * the Log4J documentation.</p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @version 0.0.1
 */
public final class Log4JLogger implements Log {

    // ------------------------------------------------------------- Attributes

    /** The fully qualified name of the Log4JLogger class. */
    private static final String FQCN = Log4JLogger.class.getName();
    
    /** Log to this logger */
    private Logger logger = null;

    // ------------------------------------------------------------ Constructor

    /**
     * Default constructor.
     */
    public Log4JLogger() {
    	super();
    }

    /**
     * Base constructor
     * @param name
     */
    public Log4JLogger(String name) {
        this.logger=Logger.getLogger(name);
    }

    /** For use with a log4j factory
     * @param logger
     */
    public Log4JLogger(Logger logger ) {
        this.logger=logger;
    }

    // ---------------------------------------------------------- Implmentation
    
    /**
     * Log a message to the Log4j Logger with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     * 
     * @see Log#trace(Object) 
     */
    public void trace(Object message) {
        logger.log(FQCN, Priority.DEBUG, message, null);
    }
    
    /**
     * Log an error to the Log4j Logger with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     * 
     * @see Log#trace(Object, Throwable)
     */
    public void trace(Object message, Throwable t) {
        logger.log(FQCN, Priority.DEBUG, message, t );
    }
    
    /**
     * Log a message to the Log4j Logger with <code>DEBUG</code> priority.
     * 
     * @see Log#debug(Object)
     */
    public void debug(Object message) {
        logger.log(FQCN, Priority.DEBUG, message, null);
    }
    
    /**
     * Log an error to the Log4j Logger with <code>DEBUG</code> priority.
     * 
     * @see Log#debug(Object, Throwable)
     */
    public void debug(Object message, Throwable t) {
        logger.log(FQCN, Priority.DEBUG, message, t );
    }
    
    /**
     * Log a message to the Log4j Logger with <code>INFO</code> priority.
     * 
     * @see Log#info(Object)
     */
    public void info(Object message) {
        logger.log(FQCN, Priority.INFO, message, null );
    }
    
    /**
     * Log an error to the Log4j Logger with <code>INFO</code> priority.
     * 
     * @see Log#info(Object, Throwable)
     */
    public void info(Object message, Throwable t) {
        logger.log(FQCN, Priority.INFO, message, t );
    }

    /**
     * Log a message to the Log4j Logger with <code>WARN</code> priority.
     * 
     * @see Log#warn(Object)
     */
    public void warn(Object message) {
        logger.log(FQCN, Priority.WARN, message, null );
    }

    /**
     * Log an error to the Log4j Logger with <code>WARN</code> priority.
     * 
     * @see Log#warn(Object, Throwable)
     */
    public void warn(Object message, Throwable t) {
        logger.log(FQCN, Priority.WARN, message, t );
    }
    
    /**
     * Log a message to the Log4j Logger with <code>ERROR</code> priority.
     * 
     * @see Log#error(Object)
     */
    public void error(Object message) {
        logger.log(FQCN, Priority.ERROR, message, null );
    }
    
    /**
     * Log an error to the Log4j Logger with <code>ERROR</code> priority.
     * 
     * @see Log#error(Object, Throwable)
     */
    public void error(Object message, Throwable t) {
        logger.log(FQCN, Priority.ERROR, message, t );
    }
    
    /**
     * Log a message to the Log4j Logger with <code>FATAL</code> priority.
     * 
     * @see Log#fatal(Object)
     */
    public void fatal(Object message) {
        logger.log(FQCN, Priority.FATAL, message, null );
    }
    
    /**
     * Log an error to the Log4j Logger with <code>FATAL</code> priority.
     * 
     * @see Log#fatal(Object, Throwable)
     */
    public void fatal(Object message, Throwable t) {
        logger.log(FQCN, Priority.FATAL, message, t );
    }
    
    /**
     * Return the native Logger instance we are using.
     * @return Return the native Logger instance we are using.
     */
    public Logger getLogger() {
        return (this.logger);
    }
    
    /**
     * Check whether the Log4j Logger used is enabled for <code>DEBUG</code> priority.
     * 
     * @return Check whether the Log4j Logger used is enabled for <code>DEBUG</code> priority.
     */
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
    
    /**
     * Check whether the Log4j Logger used is enabled for <code>ERROR</code> priority.
     * 
     * @return Check whether the Log4j Logger used is enabled for <code>DEBUG</code> priority.
     */
    public boolean isErrorEnabled() {
        return logger.isEnabledFor(Priority.ERROR);
    }
    
    /**
     * Check whether the Log4j Logger used is enabled for <code>FATAL</code> priority.
     * 
     * @return Check whether the Log4j Logger used is enabled for <code>DEBUG</code> priority.
     */
    public boolean isFatalEnabled() {
        return logger.isEnabledFor(Priority.FATAL);
    }
    
    /**
     * Check whether the Log4j Logger used is enabled for <code>INFO</code> priority.
     * 
     * @return Check whether the Log4j Logger used is enabled for <code>INFO</code> priority.
     */
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>TRACE</code> priority.
     * For Log4J, this returns the value of <code>isDebugEnabled()</code>
     * 
     * @return Check whether the Log4j Logger used is enabled for <code>TRACE</code> priority.
     */
    public boolean isTraceEnabled() {
        return logger.isDebugEnabled();
    }
    
    /**
     * Check whether the Log4j Logger used is enabled for <code>WARN</code> priority.
     * 
     * @return Check whether the Log4j Logger used is enabled for <code>WARN</code> priority.
     */
    public boolean isWarnEnabled() {
        return logger.isEnabledFor(Priority.WARN);
    }
    
    /**
     * Level of logging (info / warning / error).
     * 
     * @return For now returns the log4j value.
     */
    public int getLoglevel () {
    	return logger.getLevel().toInt();
    }

	/**
	 * Sets the log4j level.
     * 
	 * @see org.zxframework.logging.Log#setLogLevel(int)
	 **/
	public void setLogLevel(int level) {
		logger.setLevel(Level.toLevel(level));
	}
}