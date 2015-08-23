/*
 * Created on Jan 13, 2004 by Michael Brewer
 * $Id: Log4jFactory.java,v 1.1.2.5 2006/07/17 16:18:08 mike Exp $
 */
package org.zxframework.logging.impl;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.zxframework.Settings;
import org.zxframework.ZX;
import org.zxframework.ZXException;
import org.zxframework.logging.Log;
import org.zxframework.logging.LogConfigurationException;
import org.zxframework.logging.LogFactory;
import org.zxframework.util.StringUtil;

/**
 * <p>Concrete subclass of {@link LogFactory} specific to log4j.
 *
 * @author Costin Manolache
 * @author Michael Brewer
 * @version 0.0.1 
 */
public final class Log4jFactory extends LogFactory {
    
    /**
     * Default constructor.
     */
    public Log4jFactory() {
    	super();  	
    }
    
    /**
     * The configuration attributes for this {@link LogFactory}.
     */
    private Hashtable attributes = new Hashtable();
    // previously returned instances, to avoid creation of proxies
    private Hashtable instances = new Hashtable();

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
     * Return an array containing the names of all currently defined configuration 
     * attributes.  If there are no such attributes, a zero length array is returned.
     * 
     * @return Returns an array
     */
    public String[] getAttributeNames() {
        Vector names = new Vector();
        
        Enumeration keys = attributes.keys();
        while (keys.hasMoreElements()) {
            names.addElement(keys.nextElement());
        }
        
        String results[] = new String[names.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = (String)names.elementAt(i);
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
        Log instance = (Log) instances.get(clazz);
        
        if( instance != null ) {
            return instance;    
        }
        
        instance=new Log4JLogger(Logger.getLogger( clazz ));
        instances.put(clazz, instance);
        return instance;
    }
    
    /** 
     * @see org.zxframework.logging.LogFactory#getInstance(java.lang.String)
     **/
    public Log getInstance(String name) throws LogConfigurationException {
        Log instance = (Log) instances.get(name);
        if( instance != null ) {
            return instance;
        }
        
        instance=new Log4JLogger(Logger.getLogger( name ));
        instances.put(name, instance);
        return instance;
    }
    
    /**
     * This the Log4J implementation of the Logging using the zX object.
     * 
     * @param pobjZX A handle to zX
     * @see org.zxframework.logging.LogFactory#getInstance(ZX)
     * @return Returns an instance of this logger
     * @throws LogConfigurationException
     */
    public Log getInstance(ZX pobjZX) throws LogConfigurationException {
    	String name = pobjZX.getSettings().getAppName();
    	
    	try {
            configure();
        } catch (Exception e) {
        	pobjZX.log.error("Failed to parse init logging : " + logSettings, e);
        	
            throw new LogConfigurationException(e);
        }
        
    	Log instance = (Log)instances.get("Logging." + name);
    	if( instance != null ) {
    		return instance;
    	}
    	
    	instance= new Log4JLogger(Logger.getLogger(name));
    	instances.put(name, instance);
    	return instance;
    }
    
    /**
     * Release any internal references to previously created {@link Log}
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
    
    /**
     * Use the Properties object to configure Log4j 
     */
    private static Properties logSettings;
    
    /**
     * Configures the logging implementation based on the zX config file
     * 
     * @throws ZXException Thrown if configure fails
     */
    public static void configure() throws ZXException {

        try {
            Settings settings = ZX.settings;
            
            /**
             * Check whether we should reconfigure the Log4J settings :
             * 
             * This is should be done when :
             * 1)When we init zX
             * 2)When something changes like logLevel etc..
             */
            if (logSettings == null 
                || !logSettings.get("log4j.append.log.Threshold").equals(settings.getLogLevel())) {
            	
            	/**
            	 * Get log file.
            	 */
                String logFile = settings.getLogFileName();
                if(StringUtil.len(logFile) > 0) {
                    /**
                     * Check for absolute paths like :
                     */
                    if (logFile.charAt(0) != '/'  && !logFile.startsWith("//") && logFile.indexOf(':') == -1) {
                        logFile = settings.getBaseDir() + File.separatorChar + logFile;
                    }
                } else {
                    logFile = settings.getBaseDir() + File.separatorChar + "log" + File.separatorChar + settings.getAppName() + ".log";
                }
                
                /**
                 * Create the log directory if necessary.
                 */
                try {
                    File file = new File(logFile);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().createNewFile();
                    }
                    file.createNewFile();
                } catch (java.io.IOException e) {
                	throw new Exception("Failed to create logFile : " + logFile, e);
                }
                
                /**
                 * Tracing settings : as the tracing can also use the log api, 
                 * the tracing needs to be initialized here
                 */
                
                /**
                 * Get the filename of the trace file.
                 */ 
                String traceFile = settings.getTraceFileName();
                if(StringUtil.len(traceFile) > 0) {
                    /**
                     * Check for absolute paths like :
                     */
                    if (traceFile.charAt(0) != '/'  && !traceFile.startsWith("//") && traceFile.indexOf(':') == -1) {
                        traceFile = settings.getBaseDir() + File.separatorChar + traceFile;
                    }
                } else {
                    traceFile = settings.getBaseDir() + File.separatorChar + "trace" + File.separatorChar + settings.getAppName() + ".trc";
                }
                
                /**
                 * Create the trace directory if necessary.
                 */
                try {
                    File file = new File(traceFile);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().createNewFile();
                    }
                    file.createNewFile();
                    
                } catch (java.io.IOException e) {
                	throw new Exception("Failed to create traceFile : " + traceFile, e);
                }
                
                logSettings = new Properties();
                
                /**
                 * Logging Settings : This is the setttings for all of the logging. 
                 */
                logSettings.put("log4j.rootCategory", settings.getLogLevel() + ", log");
                logSettings.put("log4j.additivity.rootCategory","false");
                logSettings.put("log4j.appender.log","org.apache.log4j.RollingFileAppender");
                
                /**
                 * Normal logging settings.
                 */
                logSettings.put("log4j.appender.log.File", logFile);
                logSettings.put("log4j.appender.trace.Append",settings.isLogAppend()?"true":"false");
                logSettings.put("log4j.append.log.Threshold", settings.getLogLevel());
                logSettings.put("log4j.appender.log.layout","org.apache.log4j.PatternLayout");
                logSettings.put("log4j.appender.log.layout.ConversionPattern",settings.getLogFormat());     
                
                /**
                 * Tracing settings
                 */
                logSettings.put("log4j.category.Tracing","TRACE, trace");   
                logSettings.put("log4j.additivity.Tracing","false");
                logSettings.put("log4j.appender.trace","org.apache.log4j.RollingFileAppender");
                logSettings.put("log4j.appender.trace.File", traceFile);
                logSettings.put("log4j.appender.trace.Append", settings.isTraceAppend()?"true":"false");
                logSettings.put("log4j.appender.trace.layout", "org.apache.log4j.PatternLayout");
                logSettings.put("log4j.appender.trace.layout.ConversionPattern", settings.getTraceFormat());  
                
                PropertyConfigurator.configure(logSettings);
            }
            
        } catch (Exception e) {
            throw new ZXException("Failed to configure logger : " + logSettings.toString(), e);
        }        
    }
}