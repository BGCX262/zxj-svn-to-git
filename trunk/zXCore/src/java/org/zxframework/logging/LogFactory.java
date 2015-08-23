/*
 * Created on Jan 13, 2004 by Michael Brewer
 * $Id: LogFactory.java,v 1.1.2.6 2006/07/17 16:18:15 mike Exp $
 */
package org.zxframework.logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;

import org.zxframework.ZX;

/**
 * <p>Factory for creating {@link Log} instances, with discovery and
 * configuration features similar to that employed by standard Java APIs
 * such as JAXP.</p>
 * 
 * <p><strong>IMPLEMENTATION NOTE</strong> - This implementation is heavily
 * based on the SAXParserFactory and DocumentBuilderFactory implementations
 * (corresponding to the JAXP pluggability APIs) found in Apache Xerces.</p>
 *
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @author Richard A. Sitze
 * @version 0.0.1
 */
public abstract class LogFactory {

		// -------------------------------------------- Manifest Constants

		/**
		 * The name of the property used to identify the LogFactory implementation
		 * class name.
		 */
		public static final String FACTORY_PROPERTY = "org.zxframework.logging.LogFactory";

		/**
		 * The fully qualified class name of the fallback <code>LogFactory</code>
		 * implementation class to use, if no other can be found.
		 */
		public static String FACTORY_DEFAULT = "org.zxframework.logging.impl.Log4jFactory";


		// --------------------------------------------- Constructors

		/**
		 * Protected constructor that is not available for public use.
		 */
		protected LogFactory() {
			super();
		}

		// --------------------------------------------------------- Public Methods

		/**
		 * Return the configuration attribute with the specified name (if any),
		 * or <code>null</code> if there is no such attribute.
		 *
		 * @param name Name of the attribute to return
		 * @return Return the configuration attribute with the specified name (if any)
		 */
		public abstract Object getAttribute(String name);

		/**
		 * Return an array containing the names of all currently defined
		 * configuration attributes.  
		 * 
		 * If there are no such attributes, a zero
		 * length array is returned.
		 * @return Returns an array containing the names of all currently defined configuration attributes
		 */
		public abstract String[] getAttributeNames();

		/**
		 * Convenience method to derive a name from the specified class and
		 * call <code>getInstance(String)</code> with it.
		 *
		 * @param clazz Class for which a suitable Log name will be derived
		 *
		 * @return Returns an instance of the logger
		 * @exception LogConfigurationException if a suitable <code>Log</code>
		 *  instance cannot be returned
		 */
		public abstract Log getInstance(Class clazz) throws LogConfigurationException;

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
		 * @return Returns an instances of this loggers
		 * @exception LogConfigurationException if a suitable <code>Log</code>
		 *  instance cannot be returned
		 */
		public abstract Log getInstance(String name) throws LogConfigurationException;
		
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
		 * <p><strong>NOTE</strong> - For each logging implementation you 
		 * want to use you need to pass the configuration settings of settings
		 * to the logger.</p>
		 *
		 * @param pobjZX Pass a handle to the zX object. 
		 * @return Returns an instance of this logger
		 * @exception LogConfigurationException if a suitable <code>Log</code>
		 *  instance cannot be returned
		 */
		public abstract Log getInstance(ZX pobjZX) throws LogConfigurationException;

		/**
		 * Release any internal references to previously created {@link Log}
		 * instances returned by this factory.  This is useful environments
		 * like servlet containers, which implement application reloading by
		 * throwing away a ClassLoader.  Dangling references to objects in that
		 * class loader would prevent garbage collection.
		 */
		public abstract void release();

		/**
		 * Remove any configuration attribute associated with the specified name.
		 * If there is no such attribute, no action is taken.
		 *
		 * @param name Name of the attribute to remove
		 */
		public abstract void removeAttribute(String name);

		/**
		 * Set the configuration attribute with the specified name.  Calling
		 * this with a <code>null</code> value is equivalent to calling
		 * <code>removeAttribute(name)</code>.
		 *
		 * @param name Name of the attribute to set
		 * @param value Value of the attribute to set, or <code>null</code>
		 *  to remove any setting for this attribute
		 */
		public abstract void setAttribute(String name, Object value);

		// ------------------------------------------------------- Static Variables
		
		/**
		 * The previously constructed <code>LogFactory</code> instances, keyed by
		 * the <code>ClassLoader</code> with which it was created.
		 */
		protected static Hashtable factories = new Hashtable();
        
		// --------------------------------------------------------- Static Methods

		/**
		 * <p>Construct (if necessary) and return a <code>LogFactory</code>
		 * instance, using the following ordered lookup procedure to determine
		 * the name of the implementation class to be loaded.</p>
		 * <ul>
		 * <li>The <code>org.zxframework.logging.LogFactory</code> system
		 *     property.</li>
		 * <li>The JDK 1.3 Service Discovery mechanism</li>
		 * <li>Use the properties file <code>commons-logging.properties</code>
		 *     file, if found in the class path of this class.  The configuration
		 *     file is in standard <code>java.util.Properties</code> format and
		 *     contains the fully qualified name of the implementation class
		 *     with the key being the system property defined above.</li>
		 * <li>Fall back to a default implementation class
		 *     (<code>org.zxframework.logging.impl.LogFactoryImpl</code>).</li>
		 * </ul>
		 *
		 * <p><em>NOTE</em> - If the properties file method of identifying the
		 * <code>LogFactory</code> implementation class is utilized, all of the
		 * properties defined in this file will be set as configuration attributes
		 * on the corresponding <code>LogFactory</code> instance.</p>
		 *
		 * <p><strong>Extra Note:</strong> - 
		 * This method will read from the zXConfigFile to select which LogFactory to use.:
		 * org.zxframework.logging.impl.LogFactoryImpl - This dynamically checks for which logging implementation to 
		 * use the order is Log4J then JDK14 and then SimpleLog
		 * org.zxframework.logging.impl.Log4JFactory - The will load the log4j logger, this is ideal for 1.1+ jdk's and
		 * it very fast.
		 * We could add on that loads our own coded logger, however if we use log4j we can do tracing and loggin in one
		 * go.
		 * 
		 * @return Returns the logfactory
		 * @exception LogConfigurationException if the implementation class is not
		 *  available or cannot be instantiated.
		 */
		public static LogFactory getFactory() throws LogConfigurationException {

			// Identify the class loader we will be using
			ClassLoader contextClassLoader =
			(ClassLoader)AccessController.doPrivileged(
					new PrivilegedAction() {
						public Object run() {
							return getContextClassLoader();
						}
					});

			// Return any previously registered factory for this class loader
			LogFactory factory = getCachedFactory(contextClassLoader);
			if (factory != null)
				return factory;
			
			// First, try the system property :
			try {
				String factoryClass = System.getProperty(FACTORY_PROPERTY);
				if (factoryClass != null) {
					factory = newFactory(factoryClass, contextClassLoader);
				}
			} catch (SecurityException e) {
				// ignore
			}

			
			/**
			 * Read from the zXConfig
			 */
			if (factory == null) {
				/**
				 * get zX specific values :
				 */
				if (ZX.settings != null) {
					/**
					 * This gets the selected LogFactory from the zXConfig file :
					 * org.zxframework.logging.impl.LogFactoryImpl - This dynamically checks for which logging implementation to 
					 * use the order is Log4J then JDK14 and then SimpleLog
					 * org.zxframework.logging.impl.Log4JFactory - The will load the log4j logger, this is ideal for 1.1+ jdk's and
					 * it very fast.
					 * 
					 * We could add on that loads our own coded logger, however if we use log4j we can do tracing and loggin in one
					 * go.
					 */
				    FACTORY_DEFAULT = ZX.settings.getLogFactory();
				}
				factory = newFactory(FACTORY_DEFAULT, LogFactory.class.getClassLoader());
			}
			
			/**
			 * Should have a handle on a logger factory.
			 */
			if (factory != null) {
				/**
				 * Always cache using context class loader..
				 */
				cacheFactory(contextClassLoader, factory);
			}
			return factory;
		}

		/**
		 * Convenience method to return a named logger, without the application
		 * having to care about factories.
		 *
		 * @param clazz Class for which a log name will be derived
		 *
		 * @return Returns the loggers
		 * @exception LogConfigurationException if a suitable <code>Log</code>
		 *  instance cannot be returned
		 */
		public static Log getLog(Class clazz) throws LogConfigurationException {
			return (getFactory().getInstance(clazz));
		}

		/**
		 * Convenience method to return a named logger, without the application
		 * having to care about factories.
		 *
		 * @param name Logical name of the <code>Log</code> instance to be
		 *  returned (the meaning of this name is only known to the underlying
		 *  logging implementation that is being wrapped)
		 *
		 * @return Returns the loggers
		 * @exception LogConfigurationException if a suitable <code>Log</code>
		 *  instance cannot be returned
		 */
		public static Log getLog(String name) throws LogConfigurationException {
			return (getFactory().getInstance(name));
		}

		/**
		 * This will only get called once by the zXCls at the moment of initialisation.
		 * @param pobjZX
		 * @return Returns the logger
		 * @throws LogConfigurationException
		 */
		public static Log getLog(ZX pobjZX) throws LogConfigurationException {
			return (getFactory().getInstance(pobjZX));
		}

		/**
		 * Release any internal references to previously created {@link LogFactory}
		 * instances that have been associated with the specified class loader
		 * (if any), after calling the instance method <code>release()</code> on
		 * each of them.
		 *
		 * @param classLoader ClassLoader for which to release the LogFactory
		 */
		public static void release(ClassLoader classLoader) {
			synchronized (factories) {
				LogFactory factory = (LogFactory) factories.get(classLoader);
				if (factory != null) {
					factory.release();
					factories.remove(classLoader);
				}
			}
		}

		// ------------------------------------------------------ Protected Methods


		/**
		 * Return the thread context class loader if available.
		 * Otherwise return null.
		 * 
		 * The thread context class loader is available for JDK 1.2
		 * or later, if certain security conditions are met.
		 *
		 * @return Return the thread context class loader
		 * @exception LogConfigurationException if a suitable class loader
		 * cannot be identified.
		 */
		public static ClassLoader getContextClassLoader() throws LogConfigurationException {
			ClassLoader classLoader = null;

			try {
				// Are we running on a JDK 1.2 or later system?
				Method method = Thread.class.getMethod("getContextClassLoader", null);

				// Get the thread context class loader (if there is one)
				try {
					classLoader = (ClassLoader)method.invoke(Thread.currentThread(), null);
				} catch (IllegalAccessException e) {
					throw new LogConfigurationException
					("Unexpected IllegalAccessException", e);
				} catch (InvocationTargetException e) {
					/**
					 * InvocationTargetException is thrown by 'invoke' when
					 * the method being invoked (getContextClassLoader) throws
					 * an exception.
					 * 
					 * getContextClassLoader() throws SecurityException when
					 * the context class loader isn't an ancestor of the
					 * calling class's class loader, or if security
					 * permissions are restricted.
					 * 
					 * In the first case (not related), we want to ignore and
					 * keep going.  We cannot help but also ignore the second
					 * with the logic below, but other calls elsewhere (to
					 * obtain a class loader) will trigger this exception where
					 * we can make a distinction.
					 */
					if (e.getTargetException() instanceof SecurityException) {
						// ignore
					} else {
						// Capture 'e.getTargetException()' exception for details
						// alternate: log 'e.getTargetException()', and pass back 'e'.
						throw new LogConfigurationException("Unexpected InvocationTargetException", e.getTargetException());
					}
				}
			} catch (NoSuchMethodException e) {
				// Assume we are running on JDK 1.1
				classLoader = LogFactory.class.getClassLoader();
			}

			// Return the selected class loader
			return classLoader;
		}

		/**
		 * Check cached factories (keyed by classLoader)
		 * @param contextClassLoader
		 * @return Returns the logfactory
		 */
		private static LogFactory getCachedFactory(ClassLoader contextClassLoader) {
			LogFactory factory = null;
			
			if (contextClassLoader != null) {
				factory = (LogFactory) factories.get(contextClassLoader);
            }
			
			return factory;
		}
		
		private static void cacheFactory(ClassLoader classLoader, LogFactory factory) {
			if (classLoader != null && factory != null) {
				factories.put(classLoader, factory);
            }
		}

		/**
		 * Return a new instance of the specified <code>LogFactory</code>
		 * implementation class, loaded by the specified class loader.
		 * If that fails, try the class loader used to load this
		 * (abstract) LogFactory.
		 *
		 * @param factoryClass Fully qualified name of the <code>LogFactory</code>
		 *  implementation class
		 * @param classLoader ClassLoader from which to load this class
		 *
		 * @return Returns the factory
		 * @exception LogConfigurationException if a suitable instance
		 *  cannot be created
		 */
		protected static LogFactory newFactory(final String factoryClass,
											   final ClassLoader classLoader) throws LogConfigurationException {
			Object result = AccessController.doPrivileged(
					new PrivilegedAction() {
						public Object run() {
							try {
								if (classLoader != null) {
									try {
										// first the given class loader param (thread class loader)
										
										// warning: must typecast here & allow exception
										// to be generated/caught & recast propertly.
										return classLoader.loadClass(factoryClass).newInstance();
									} catch (ClassNotFoundException ex) {
										if (classLoader == LogFactory.class.getClassLoader()) {
											// Nothing more to try, onwards.
											throw ex;
										}
										// ignore exception, continue
									} catch (NoClassDefFoundError e) {
										if (classLoader == LogFactory.class.getClassLoader()) {
											// Nothing more to try, onwards.
											throw e;
										}
										
									}catch(ClassCastException e){
										
										if (classLoader == LogFactory.class.getClassLoader()) {
											// Nothing more to try, onwards (bug in loader implementation).
											throw e;
										}
									}
									// ignore exception, continue  
								}
								
								/* At this point, either classLoader == null, OR
								 * classLoader was unable to load factoryClass..
								 * try the class loader that loaded this class:
								 * LogFactory.getClassLoader().
								 * 
								 * Notes:
								 * a) LogFactory.class.getClassLoader() may return 'null'
								 *    if LogFactory is loaded by the bootstrap classloader.
								 * b) The Java endorsed library mechanism is instead
								 *    Class.forName(factoryClass);
								 */
								// warning: must typecast here & allow exception
								// to be generated/caught & recast propertly.
								return Class.forName(factoryClass).newInstance();
							} catch (Exception e) {
								return new LogConfigurationException(e);
							}
						}
					});
			
			if (result instanceof LogConfigurationException) {
				throw (LogConfigurationException)result;
            }
			
			return (LogFactory)result;
		}
        
        /**
         * Release any internal references to previously created {@link LogFactory}
         * instances, after calling the instance method <code>release()</code> on
         * each of them.  This is useful in environments like servlet containers,
         * which implement application reloading by throwing away a ClassLoader.
         * Dangling references to objects in that class loader would prevent
         * garbage collection.
         */
        public static void releaseAll() {
            synchronized (factories) {
                Enumeration elements = factories.elements();
                while (elements.hasMoreElements()) {
                    LogFactory element = (LogFactory) elements.nextElement();
                    element.release();
                }
                factories.clear();
            }
        }
}