/*
 * Created on Jan 13, 2004 by Michael Brewer
 * $Id: Log.java,v 1.1.2.1 2005/02/28 13:30:12 mike Exp $
 */
package org.zxframework.logging;

/**
 * <p>A simple logging interface abstracting logging APIs.  In order to be
 * instantiated successfully by {@link org.zxframework.logging.LogFactory}, classes that implement
 * this interface must have a constructor that takes a single String
 * parameter representing the "name" of this Log.</p>
 *
 * <p> The six logging levels used by <code>Log</code> are (in order):
 * <ol>
 * <li>trace (the least serious)</li>
 * <li>debug</li>
 * <li>info</li>
 * <li>warn</li>
 * <li>error</li>
 * <li>fatal (the most serious)</li>
 * </ol>
 * The mapping of these log levels to the concepts used by the underlying
 * logging system is implementation dependent.
 * The implemention should ensure, though, that this ordering behaves
 * as expected.</p>
 *
 * <p>Performance is often a logging concern.
 * By examining the appropriate property,
 * a component can avoid expensive operations (producing information
 * to be logged).</p>
 *
 * <p> For example,
 * <code><pre>
 *    if (log.isDebugEnabled()) {
 *        ... do something expensive ...
 *        log.debug(theResult);
 *    }
 * </pre></code>
 * </p>
 *
 * <p>Configuration of the underlying logging system will generally be done
 * external to the Logging APIs, through whatever mechanism is supported by
 * that system.</p>
 * 
 * </pre>
 *
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public interface Log {
    
    // ----------------------------------------------------- Logging Properties

	/**
	 * Level of logging (info / warning / error).
     * 
	 * @return Returns the int value of the implementation logLevel.
	 */
	public int getLoglevel ();
	
	/**
	 * <p>Sets the log level.</p>
	 * 
	 * @param level The zX level you wish set the logger, this gets translated 
	 * to the implementation log level.
	 */
	public void setLogLevel(int level);
	
    /**
     * <p> Is debug logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than debug. </p>
     * @return Is debug logging currently enabled?
     */
    public boolean isDebugEnabled();

    /**
     * <p> Is error logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than error. </p>
     * @return Is error logging currently enabled?
     */
    public boolean isErrorEnabled();

    /**
     * <p> Is fatal logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than fatal. </p>
     * @return Is fatal logging currently enabled?
     */
    public boolean isFatalEnabled();

    /**
     * <p> Is info logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than info. </p>
     * @return Is info logging currently enabled?
     */
    public boolean isInfoEnabled();

    /**
     * <p> Is trace logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than trace. </p>
     * @return Is trace logging currently enabled? 
     */
    public boolean isTraceEnabled();

    /**
     * <p> Is warning logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than warning. </p>
     * @return Is warning logging currently enabled?
     */
    public boolean isWarnEnabled();

    // -------------------------------------------------------- Logging Methods

    /**
     * <p> Log a message with trace log level. </p>
     *
     * @param message log this message
     */
    public void trace(Object message);

    /**
     * <p> Log an error with trace log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void trace(Object message, Throwable t);

    /**
     * <p> Log a message with debug log level. </p>
     *
     * @param message log this message
     */
    public void debug(Object message);

    /**
     * <p> Log an error with debug log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void debug(Object message, Throwable t);

    /**
     * <p> Log a message with info log level. </p>
     *
     * @param message log this message
     */
    public void info(Object message);

    /**
     * <p> Log an error with info log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void info(Object message, Throwable t);

    /**
     * <p> Log a message with warn log level. </p>
     *
     * @param message log this message
     */
    public void warn(Object message);

    /**
     * <p> Log an error with warn log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void warn(Object message, Throwable t);

    /**
     * <p> Log a message with error log level. </p>
     *
     * @param message log this message
     */
    public void error(Object message);

    /**
     * <p> Log an error with error log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void error(Object message, Throwable t);

    /**
     * <p> Log a message with fatal log level. </p>
     *
     * @param message log this message
     */
    public void fatal(Object message);

    /**
     * <p> Log an error with fatal log level. </p>
     *
     * @param message log this message
     * @param t log this cause
     */
    public void fatal(Object message, Throwable t);
}