/*
 * Created on Jan 13, 2004 by Michael Brewer
 * $Id: LogFactoryImpl.java,v 1.1.2.5 2006/07/17 16:18:08 mike Exp $
 */
package org.zxframework.logging.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.zxframework.Settings;
import org.zxframework.ZX;
import org.zxframework.ZXException;
import org.zxframework.logging.Log;
import org.zxframework.logging.LogConfigurationException;
import org.zxframework.logging.LogFactory;

/**
 * <p>Concrete subclass of {@link LogFactory} that implements the
 * following algorithm to dynamically select a logging implementation
 * class to instantiate a wrapper for.</p>
 * <ul>
 * <li>Use a factory configuration attribute named
 *     <code>org.zxframework.logging.Log</code> to identify the
 *     requested implementation class.</li>
 * <li>Use the <code>org.zxframework.logging.Log</code> system property
 *     to identify the requested implementation class.</li>
 * <li>If <em>Log4J</em> is available, return an instance of
 *     <code>org.zxframework.logging.impl.Log4JLogger</code>.</li>
 * <li>If <em>JDK 1.4 or later</em> is available, return an instance of
 *     <code>org.zxframework.logging.impl.Jdk14Logger</code>.</li>
 * <li>Otherwise, return an instance of
 *     <code>org.zxframework.logging.impl.SimpleLog</code>.</li>
 * </ul>
 *
 * <p>If the selected {@link Log} implementation class has a
 * <code>setLogFactory()</code> method that accepts a {@link LogFactory}
 * parameter, this method will be called on each newly created instance
 * to identify the associated factory.  This makes factory configuration
 * attributes available to the Log instance, if it so desires.</p>
 *
 * <p>This factory will remember previously created <code>Log</code> instances
 * for the same name, and will return them on repeated requests to the
 * <code>getInstance()</code> method.  This implementation ignores any
 * configured attributes.</p>
 *
 * @author Rod Waldhoff
 * @author Craig R. McClanahan
 * @author Richard A. Sitze
 * @version 0.0.1
 */

public class LogFactoryImpl extends LogFactory {

    // ----------------------------------------------------------- Constructors	

    /**
     * Public no-arguments constructor required by the lookup mechanism.
     */
    public LogFactoryImpl() {
        super();
    }

    // ----------------------------------------------------- Manifest Constants

    /**
     * The name of the system property identifying our {@link Log}
     * implementation class.
     */
    public static final String LOG_PROPERTY = "org.zxframework.logging.Log";

    // ----------------------------------------------------- Static Variables
    
    /**
     * Detected log type
     */
    public final static int LOG_TYPE_LOG4J = 0;
    /**
     * Comment for <code>LOG_TYPE_JDK14</code>
     */
    public final static int LOG_TYPE_JDK14 = 1;
    /**
     * Comment for <code>LOG_TYPE_SIMPLE</code>
     */
    public final static int LOG_TYPE_SIMPLE = 2;
    

    // ----------------------------------------------------- Instance Variables
    
    private int logType = 0;

    /**
     * Configuration attributes
     */
    protected Hashtable attributes = new Hashtable();

    /**
     * The {@link org.zxframework.logging.Log} instances that have
     * already been created, keyed by logger name.
     */
    protected Hashtable instances = new Hashtable();

    /**
     * Name of the class implementing the Log interface.
     */
    private String logClassName;

    /**
     * The one-argument constructor of the
     * {@link org.zxframework.logging.Log}
     * implementation class that will be used to create new instances.
     * This value is initialized by <code>getLogConstructor()</code>,
     * and then returned repeatedly.
     */
    protected Constructor logConstructor = null;

    /**
     * The signature of the Constructor to be used.
     */
    protected Class logConstructorSignature[] = { java.lang.String.class };

    /**
     * The one-argument <code>setLogFactory</code> method of the selected
     * {@link org.zxframework.logging.Log} method, if it exists.
     */
    protected Method logMethod = null;

    /**
     * The signature of the <code>setLogFactory</code> method to be used.
     */
    protected Class logMethodSignature[] = { LogFactory.class };

    // --------------------------------------------------------- Public Methods

    /**
     * Return the configuration attribute with the specified name (if any),
     * or <code>null</code> if there is no such attribute.
     *
     * @param name Name of the attribute to return
     * @return Returns the configuration attribute
     */
    public Object getAttribute(String name) {
        return (attributes.get(name));
    }

    /**
     * Return an array containing the names of all currently defined
     * configuration attributes.  
     * If there are no such attributes, a zero length array is returned.
     * 
     * @return Return an array containing the names of all currently defined configuration attributes
     */
    public String[] getAttributeNames() {

        Vector names = new Vector();
        Enumeration keys = attributes.keys();
        while (keys.hasMoreElements()) {
            names.addElement(keys.nextElement());
        }
        String results[] = new String[names.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = (String) names.elementAt(i);
        }
        return (results);
    }

    /**
     * Convenience method to derive a name from the specified class and
     * call <code>getInstance(String)</code> with it.
     *
     * @param clazz Class for which a suitable Log name will be derived
     *
     * @return Returns an instance of this logger
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public Log getInstance(Class clazz) throws LogConfigurationException {
        return (getInstance(clazz.getName()));
    }

    /**
     * <p>Construct (if necessary) and return a <code>Log</code> instance,
     * using the factory's current set of configuration attributes.</p>
     *
     * <p><strong>NOTE</strong> - Depending upon the implementation of
     * the <code>LogFactory</code> you are using, the <code>Log</code>
     * instance you are returned may or may not be local to the current
     * application, and may or may not be returned again on a subsequent
     * call with the same name argument.</p>
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     *
     * @return Returns an instance of this logger
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public Log getInstance(String name) throws LogConfigurationException {
        Log instance = (Log)instances.get(name);
        if (instance == null) {
            instance = newInstance(name);
            instances.put(name, instance);
        }
        return (instance);
    }

    /** 
     * @see org.zxframework.logging.LogFactory#getInstance(org.zxframework.ZX)
     **/
    public Log getInstance(ZX pobjZX) throws LogConfigurationException {
        Settings settings = ZX.settings;
    	String name = settings.getAppName();
    	
    	/**
    	 * Configure the logger here :
    	 * NOTE : If the logger has been created already then it will not be
    	 * reconfigured each time. So if you want to be able to update configs
    	 * during runtime you need to move this up.
    	 */
    	switch (logType) {
    	case LOG_TYPE_LOG4J :
    		try {
                // Check if we should reconfigure?
                // Calling it like so in case there is no 
                // log4j and you want this to run.
                Log4jFactory.configure();
                
            } catch (ZXException e) {
                throw new LogConfigurationException(e);
            }
            
    		break;
            
    	case LOG_TYPE_JDK14 :
            // Use defaults
            return getInstance(ZX.class);
    	default :
            // Use defaults
            return getInstance(ZX.class);
    	}
        
    	Log instance = (Log)instances.get(name);
    	if (instance == null) {
    		instance = newInstance(name);
    		instances.put(name, instance);
    	}
        
    	return (instance);
    }    
    
    /**
     * Release any internal references to previously created
     * {@link org.zxframework.logging.Log}
     * instances returned by this factory.  This is useful environments
     * like servlet containers, which implement application reloading by
     * throwing away a ClassLoader.  Dangling references to objects in that
     * class loader would prevent garbage collection.
     */
    public void release() {
        instances.clear();
    }

    /**
     * Remove any configuration attribute associated with the specified name.
     * If there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    /**
     * Set the configuration attribute with the specified name.  Calling
     * this with a <code>null</code> value is equivalent to calling
     * <code>removeAttribute(name)</code>.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set, or <code>null</code>
     *  to remove any setting for this attribute
     */
    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * This does the detection of the logger to use.
     * 
     * 
     * Return the fully qualified Java classname of the {@link Log}
     * implementation we will be using.
     * @return Returns log classname
     */
    protected String getLogClassName() {
        // Return the previously identified class name (if any)
        if (logClassName != null) {
            return logClassName;
        }
        
        logClassName = (String) getAttribute(LOG_PROPERTY);
        if (logClassName == null) {
            try {
                logClassName = System.getProperty(LOG_PROPERTY);
            } catch (SecurityException e) {
                // Ignore.
            }
        }
        if ((logClassName == null) && isLog4JAvailable()) {
            logClassName = "org.zxframework.logging.impl.Log4JLogger";
            logType = LOG_TYPE_LOG4J;
        }
        if ((logClassName == null) && isJdk14Available()) {
            logClassName = "org.zxframework.logging.impl.Jdk14Logger";
            logType = LOG_TYPE_JDK14;
        }
        if (logClassName == null) {
            logClassName = "org.zxframework.logging.impl.SimpleLog";
            logType = LOG_TYPE_SIMPLE;
        }
        return (logClassName);
    }

    /**
     * <p>Return the <code>Constructor</code> that can be called to instantiate
     * new {@link org.zxframework.logging.Log} instances.</p>
     *
     * <p><strong>IMPLEMENTATION NOTE</strong> - Race conditions caused by
     * calling this method from more than one thread are ignored, because
     * the same <code>Constructor</code> instance will ultimately be derived
     * in all circumstances.</p>
     *
     * @return Return the log constructor
     * @exception LogConfigurationException if a suitable constructor
     *  cannot be returned
     */
    protected Constructor getLogConstructor()
        throws LogConfigurationException {

        // Return the previously identified Constructor (if any)
        if (logConstructor != null) {
            return logConstructor;
        }

        String strLogClassName = getLogClassName();

        // Attempt to load the Log implementation class
        Class logClass = null;
        try {
            logClass = loadClass(strLogClassName);
            if (logClass == null) {
                throw new LogConfigurationException
                    ("No suitable Log implementation for " + strLogClassName);
            }
            if (!Log.class.isAssignableFrom(logClass)) {
                throw new LogConfigurationException
                    ("Class " + strLogClassName + " does not implement Log");
            }
        } catch (Throwable t) {
            throw new LogConfigurationException(t);
        }

        // Identify the <code>setLogFactory</code> method (if there is one)
        try {
            logMethod = logClass.getMethod("setLogFactory",
                                           logMethodSignature);
        } catch (Throwable t) {
            logMethod = null;
        }

        // Identify the corresponding constructor to be used
        try {
            logConstructor = logClass.getConstructor(logConstructorSignature);
            return (logConstructor);
        } catch (Throwable t) {
            throw new LogConfigurationException
                ("No suitable Log constructor " +
                 logConstructorSignature+ " for " + strLogClassName, t);
        }
    }
    
    /**
     * MUST KEEP THIS METHOD PRIVATE.
     *
     * <p>Exposing this method outside of
     * <code>org.zxframework.logging.impl.LogFactoryImpl</code>
     * will create a security violation:
     * This method uses <code>AccessController.doPrivileged()</code>.
     * </p>
     *
     * Load a class, try first the thread class loader, and
     * if it fails use the loader that loaded this class.
     * 
     * @param name The name of the class to load.
     * @return Returns the class.
     * @throws ClassNotFoundException
     */
    private static Class loadClass( final String name ) throws ClassNotFoundException {
        Object result = AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    ClassLoader threadCL = getContextClassLoader();
                    if (threadCL != null) {
                        try {
                            return threadCL.loadClass(name);
                        } catch( ClassNotFoundException ex ) {
                            // ignore
                        }
                    }
                    try {
                        return Class.forName( name );
                    } catch (ClassNotFoundException e) {
                        return e;
                    }
                }
            });

        if (result instanceof Class)
            return (Class)result;

        throw (ClassNotFoundException)result;
    }

    /**
     * Is <em>JDK 1.4 or later</em> logging available?
     * 
     * @return Is <em>JDK 1.4 or later</em> logging available?
     */
    protected boolean isJdk14Available() {

        try {
            loadClass("java.util.logging.Logger");
            loadClass("org.zxframework.logging.impl.Jdk14Logger");
            return (true);
        } catch (Throwable t) {
            return (false);
        }
    }

    /**
     * Is a <em>Log4J</em> implementation available?
     * 
     * @return Is a <em>Log4J</em> implementation available?
     */
    protected boolean isLog4JAvailable() {

        try {
            loadClass("org.apache.log4j.Logger");
            loadClass("org.zxframework.logging.impl.Log4JLogger");
            return (true);
        } catch (Throwable t) {
            return (false);
        }
    }

    /**
     * Create and return a new {@link org.zxframework.logging.Log}
     * instance for the specified name.
     *
     * @param name Name of the new logger
     *
     * @return Returns a new instance of this logger
     * @exception LogConfigurationException if a new instance cannot
     *  be created
     */
    protected Log newInstance(String name) throws LogConfigurationException {

        Log instance = null;
        try {
            Object params[] = new Object[1];
            params[0] = name;
            instance = (Log)getLogConstructor().newInstance(params);
            if (logMethod != null) {
                params[0] = this;
                logMethod.invoke(instance, params);
            }
            
            return (instance);
            
        } catch (Throwable t) {
            throw new LogConfigurationException(t);
        }
    }
}