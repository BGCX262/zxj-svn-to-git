/*
 * Created on Jan 13, 2004 by Michael Brewer
 * $Id: Jdk14Logger.java,v 1.1.2.1 2005/02/28 13:30:11 mike Exp $
 */
package org.zxframework.logging.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.zxframework.logging.Log;

/**
 * <p>Implementation of the <code>org.zxframework.logging.Log</code>
 * interfaces that wraps the standard JDK logging mechanisms that were
 * introduced in the Merlin release (JDK 1.4).</p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:donaldp@apache.org">Peter Donald</a>
 * @version 0.0.1
 */

public final class Jdk14Logger implements Log {

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a named instance of this Logger.
     *
     * @param name Name of the logger to be constructed
     */
    public Jdk14Logger(String name) {
        logger = Logger.getLogger(name);
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The underlying Logger implementation we are using.
     */
    protected Logger logger = null;

    // --------------------------------------------------------- Public Methods

    private void log(Level level, String msg, Throwable ex) {
        if (logger.isLoggable(level)) {
            // Hack (?) to get the stack trace.
            Throwable dummyException = new Throwable();
            StackTraceElement locations[] = dummyException.getStackTrace();
            // Caller will be the third element
            String cname="unknown";
            String method="unknown";
            if( locations!=null && locations.length >2) {
                StackTraceElement caller = locations[2];
                cname = caller.getClassName();
                method = caller.getMethodName();
            }
            
            if(ex == null) {
                logger.logp(level, cname, method, msg);
            } else {
                logger.logp(level, cname, method, msg, ex);
            }
        }
    }

    /**
     * Log a message with debug log level.
     * @param message
     */
    public void debug(Object message) {
        log(Level.FINE, String.valueOf(message), null);
    }

    /**
     * Log a message and exception with debug log level.
     * @param message
     * @param exception
     */
    public void debug(Object message, Throwable exception) {
        log(Level.FINE, String.valueOf(message), exception);
    }

    /**
     * Log a message with error log level.
     * 
     * @param message
     */
    public void error(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    /**
     * Log a message and exception with error log level.
     * @param message
     * @param exception
     */
    public void error(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    /**
     * Log a message with fatal log level.
     * @param message
     */
    public void fatal(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    /**
     * Log a message and exception with fatal log level.
     * 
     * @param message
     * @param exception
     */
    public void fatal(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    /**
     * Return the native Logger instance we are using.
     * 
     * @return Returns the native Logger instance we are using.
     */
    public Logger getLogger() {
        return (this.logger);
    }

    /**
     * Log a message with info log level.
     * 
     * @param message
     */
    public void info(Object message) {
        log(Level.INFO, String.valueOf(message), null);
    }

    /**
     * Log a message and exception with info log level.
     * 
     * @param message
     * @param exception
     */
    public void info(Object message, Throwable exception) {
        log(Level.INFO, String.valueOf(message), exception);
    }

    /**
     * Is debug logging currently enabled?
     * @return Is debug logging currently enabled?
     */
    public boolean isDebugEnabled() {
        return (logger.isLoggable(Level.FINE));
    }


    /**
     * Is error logging currently enabled?
     * @return Is error logging currently enabled?
     */
    public boolean isErrorEnabled() {
        return (logger.isLoggable(Level.SEVERE));
    }

    /**
     * Is fatal logging currently enabled?
     * @return Is fatal logging currently enabled?
     */
    public boolean isFatalEnabled() {
        return (logger.isLoggable(Level.SEVERE));
    }

    /**
     * Is info logging currently enabled?
     * @return Is info logging currently enabled?
     */
    public boolean isInfoEnabled() {
        return (logger.isLoggable(Level.INFO));
    }

    /**
     * Is tace logging currently enabled?
     * @return Is tace logging currently enabled?
     */
    public boolean isTraceEnabled() {
        return (logger.isLoggable(Level.FINEST));
    }
    
    /**
     * Is warning logging currently enabled?
     * @return Is warning logging currently enabled?
     */
    public boolean isWarnEnabled() {
        return (logger.isLoggable(Level.WARNING));
    }
    
    /**
     * Log a message with trace log level.
     * @param message
     */
    public void trace(Object message) {
        log(Level.FINEST, String.valueOf(message), null);
    }
    
    /**
     * Log a message and exception with trace log level.
     * 
     * @param message
     * @param exception
     */
    public void trace(Object message, Throwable exception) {
        log(Level.FINEST, String.valueOf(message), exception);
    }
    
    /**
     * Log a message with warn log level.
     * 
     * @param message
     */
    public void warn(Object message) {
        log(Level.WARNING, String.valueOf(message), null);
    }

    /**
     * Log a message and exception with warn log level.
     * 
     * @param message
     * @param exception
     */
    public void warn(Object message, Throwable exception) {
        log(Level.WARNING, String.valueOf(message), exception);
    }

	/**
	 * Loglevel.
	 * 
	 * <strong>TO DO :</strong>
	 * <ol>
	 * 	<li>
	 * TODO : Return the correct loglevel for zX if needed
	 * 	</li>
	 * </ol>
	 * 
	 * @see org.zxframework.logging.Log#getLoglevel()
	 */
	public int getLoglevel() {
		return logger.getLevel().intValue();
	}

	/**
	 * <p>Sets the log4j level.</p>
	 * 
	 * <strong>NOTE :</strong> This is functional, however there levels are 
	 * still to be determined.
	 * 
	 * <p><strong>TO DO</strong> 
	 * <ol>
	 * 	<li>
	 * TODO : Do a mapping between the zX loglevels and the JDK14 levels.
	 * 	</li>
	 * </ol>
	 * </p> 
	 * @see org.zxframework.logging.Log#setLogLevel(int)
	 **/
	public void setLogLevel(int level) {
		logger.setLevel(Level.parse(String.valueOf(level)));
	}
}