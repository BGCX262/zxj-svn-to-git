/*
 * OOoUtils.java
 *
 * Created on February 22, 2003, 12:10 PM
 *
 * Copyright 2003 Danny Brewer
 * Anyone may run this code.
 * If you wish to modify or distribute this code, then
 *  you are granted a license to do so only under the terms
 *  of the Gnu Lesser General Public License.
 * See:  http://www.gnu.org/licenses/lgpl.html
 */

package org.zxframework.doc;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.container.XNamed;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

/**
 * @author danny brewer
 */
public class OOoUtils {
	
	/**
	 * Hide default constructor.
	 */
	private OOoUtils() {
		super();
	}
	
	// ----------------------------------------------------------------------
	// Get remote service manager.
	// This is the first object you need to connect to a
	// running instance of OpenOffice.org.
	// ----------------------------------------------------------------------
	
	/** The connection url for the local service mananger. */
	public final static String LOCAL_OO_SERVICE_MGR_URL = "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager";
	
	/**
	 * Connect to OpenOffice's ServiceManager, port 8100, localhost.
	 * 
	 * @return Returns XMultiServiceFactory
	 */
	public static XMultiServiceFactory getRemoteOOoServiceManager() {
		return getRemoteOOoServiceManager(LOCAL_OO_SERVICE_MGR_URL);
	}
	
	/**
	 * Connect to OpenOffice's ServiceManager, port 8100, localhost.
	 * 
	 * @param host
	 * @param port
	 * @return Returns XMultiServiceFactory
	 */
	public static XMultiServiceFactory getRemoteOOoServiceManager(String host, String port){
		return getRemoteOOoServiceManager("uno:socket,host=" + host + ",port=" + port + ";urp;StarOffice.ServiceManager");
	}
	
	/**
	 * Connect to a specified URL to get a remote service manager.
	 * 
	 * @param unoRemoteServiceManagerUrl
	 * @return Return XMultiServiceFactory
	 * @throws java.lang.Exception
	 * @throws com.sun.star.uno.Exception
	 * @throws com.sun.star.connection.NoConnectException
	 * @throws com.sun.star.beans.UnknownPropertyException
	 */
	public static XMultiServiceFactory getRemoteOOoServiceManager(String unoRemoteServiceManagerUrl) {
		
		try {
			/**
			 * Create a local component context.
			 */ 
			XComponentContext localContext = com.sun.star.comp.helper.Bootstrap.createInitialComponentContext(null);
			
			/**
			 * Get the local service manager from the local component context.
			 */
			XMultiComponentFactory localServiceManager = localContext.getServiceManager();
			
			/**
			 * Ask local service manager for a UnoUrlResolver object with an
			 * XUnoUrlResolver interface.
			 */
			Object unoUrlResolver = localServiceManager.createInstanceWithContext("com.sun.star.bridge.UnoUrlResolver", localContext);

			/**
			 * Query the UnoUrlResolver object for an XUnoUrlResolver interface.
			 */
			XUnoUrlResolver unoUrlResolver_XUnoUrlResolver = QI.XUnoUrlResolver(unoUrlResolver);
			/**
			 * At this point, the variables
			 * unoUrlResolver
			 * unoUrlResolver_XUnoUrlResolver
			 * point to the same service. Just different interfaces of it.
			 */
			
			/**
			 * Use the unoUrlResolver to get a remote service manager,
			 * by resolving the URL that was passed in as a parameter.
			 */
			Object remoteOOoServiceManager = unoUrlResolver_XUnoUrlResolver.resolve(unoRemoteServiceManagerUrl);

			/**
			 * Get a more convenient interface.
			 */
			XMultiServiceFactory getRemoteOOoServiceManager = QI.XMultiServiceFactory(remoteOOoServiceManager);
			
			return getRemoteOOoServiceManager;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Given a remote service manager (or not),
	 * return the Desktop object, conveniently in the form
	 * of an XComponentLoader.
	 * 
	 * @return Returns the desktop object
	 */
	public static XComponentLoader getDesktop() {
		XMultiServiceFactory remoteOOoServiceManager = OOoUtils.getRemoteOOoServiceManager();
		return getDesktop(remoteOOoServiceManager);
	}
	
	/**
	 * Given a remote service manager (or not),
	 * return the Desktop object, conveniently in the form
	 * of an XComponentLoader.
	 * 
	 * @param remoteOOoServiceManager
	 * @return Returns the desktop object
	 */
	public static XComponentLoader getDesktop(Object remoteOOoServiceManager) {
		
		/**
		 * Get the desired interface to the object.
		 */
		XMultiServiceFactory remoteOOoServiceManagerMultiServiceFactory = QI.XMultiServiceFactory(remoteOOoServiceManager);
		
		/**
		 * At this point, the variables
		 * oOORmtServiceMgr
		 * oOORmtServiceMgr_MultiServiceFactory
		 * point to the same service. Just different interfaces of it.
		 */

		return getDesktop(remoteOOoServiceManagerMultiServiceFactory);
	}

	/**
	 * Given a remote service manager (or not),
	 * return the Desktop object, conveniently in the form
	 * of an XComponentLoader.
	 * 
	 * @param remoteOOoServiceManager
	 * @return Returns the desktop object
	 * @throws com.sun.star.uno.Exception
	 */
	public static XComponentLoader getDesktop(XMultiServiceFactory remoteOOoServiceManager) {
		try {
			/**
			 * Ask the MultiServiceFactory interface of the service manager for a 
			 * Desktop.
			 * This might throw com.sun.star.uno.Exception.
			 * This gives us the desktop, via. it's XInterface interface.
			 */
			XInterface desktopXInterface = (XInterface)remoteOOoServiceManager.createInstance("com.sun.star.frame.Desktop");
			
			/**
			 * Get the desired interface from the object.
			 * We don't need the XInterface interface, we need the XComponentLoader
			 * interface to the object.
			 */
			XComponentLoader desktopXComponentLoader = QI.XComponentLoader(desktopXInterface);
			
			/**
			 * At this point, the variables
			 * desktop_XInterface
			 * desktop_XComponentLoader
			 * point to the same service. Just different interfaces of it.
			 */
			return desktopXComponentLoader;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ----------------------------------------------------------------------
	// Conveniences to make some structures
	// ----------------------------------------------------------------------

	/**
	 * @param propertyName
	 * @param value
	 * @return Returns a int propertyValue.
	 */
	public static PropertyValue makePropertyValue(String propertyName, int value) {
		return makePropertyValue(propertyName, new Integer(value));
	}

	/**
	 * @param propertyName
	 * @param value
	 * @return Returns a long propertyValue.
	 */
	public static PropertyValue makePropertyValue(String propertyName, long value) {
		return makePropertyValue(propertyName, new Long(value));
	}

	/**
	 * @param propertyName
	 * @param value
	 * @return Reutns a boolean propertyValue.
	 */
	public static PropertyValue makePropertyValue(String propertyName, boolean value) {
		return makePropertyValue(propertyName, new Boolean(value));
	}

	/**
	 * @param propertyName
	 * @param value
	 * @return Returns a Object propertyvalue.
	 */
	public static PropertyValue makePropertyValue(String propertyName, Object value) {
		PropertyValue propValue = new PropertyValue();
		propValue.Name = propertyName;
		propValue.Value = value;
		return propValue;
	}

	/**
	 * @param x
	 * @param y
	 * @return Returns the point.
	 */
	public static com.sun.star.awt.Point makePoint(int x, int y) {
		com.sun.star.awt.Point point = new com.sun.star.awt.Point();
		point.X = x;
		point.Y = y;
		return point;
	}

	/**
	 * @param width
	 * @param height
	 * @return Returns the size object.
	 */
	public static com.sun.star.awt.Size makeSize(int width, int height) {
		com.sun.star.awt.Size size = new com.sun.star.awt.Size();
		size.Width = width;
		size.Height = height;
		return size;
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return Returns the rectangle object.
	 */
	public static com.sun.star.awt.Rectangle makeRectangle(int x, int y, int width, int height) {
		com.sun.star.awt.Rectangle rect = new com.sun.star.awt.Rectangle();
		rect.X = x;
		rect.Y = y;
		rect.Width = width;
		rect.Height = height;
		return rect;
	}
	
	// ----------------------------------------------------------------------
	// Property Manipulation
	// ----------------------------------------------------------------------
	
	/**
	 * Set a PropertyValue to a int.
	 * 
	 * @param obj 
	 * @param propName 
	 * @param value 
	 */
	public static void setIntProperty(Object obj, String propName, int value) {
		setProperty(obj, propName, new Integer(value));
	}
	
	/**
	 * @param obj
	 * @param propName
	 * @return Returns the int of a property value.
	 */
	public static int getIntProperty(Object obj, String propName) {
		Object value = getProperty(obj, propName);
		
		if (value != null) {
			if (value instanceof Integer) {
				Integer intValue = (Integer) value;
				return intValue.intValue();
			}
		}
		
		return 0;
	}
	
	/**
	 * @param obj
	 * @param propName
	 * @param value
	 */
	public static void setBooleanProperty(Object obj, String propName, boolean value) {
		setProperty(obj, propName, new Boolean(value));
	}
	
	/**
	 * @param obj
	 * @param propName
	 * @return Returns the boolean value of a property
	 */
	public static boolean getBooleanProperty(Object obj, String propName) {
		Object value = getProperty(obj, propName);
		
		if (value != null) {
			if (value instanceof Boolean) {
				Boolean booleanValue = (Boolean) value;
				return booleanValue.booleanValue();
			}
		}
		
		return false;
	}
	
	/**
	 * @param obj
	 * @param propName
	 * @param value
	 */
	public static void setStringProperty(Object obj, String propName, String value) {
		setProperty(obj, propName, value);
	}
	
	/**
	 * @param obj
	 * @param propName
	 * @return Returns the String value of a property.
	 */
	public static String getStringProperty(Object obj, String propName) {
		Object value = getProperty(obj, propName);
		
		if (value != null) {
			if (value instanceof String) {
				String stringValue = (String)value;
				return stringValue;
			}
			
			return value.toString();
		}
		
		return "";
	}
	
	/**
	 * @param obj
	 * @param propName
	 * @param value
	 */
	public static void setProperty(Object obj, String propName, Object value) {
		/**
		 * We need the XPropertySet interface.
		 */
		XPropertySet objXPropertySet;
		if (obj instanceof XPropertySet) {
			/**
			 * If the right interface was passed in, just typecaset it.
			 */
			objXPropertySet = (XPropertySet)obj;
		} else {
			/**
			 * Get a different interface to the drawDoc.
			 * The parameter passed in to us is the wrong interface to the
			 * object.
			 */
			objXPropertySet = QI.XPropertySet(obj);
		}

		/**
		 * Now just call our sibling using the correct interface
		 */
		setProperty(objXPropertySet, propName, value);
	}
	
	/**
	 * @param obj
	 * @param propName
	 * @param value
	 */
	public static void setProperty(XPropertySet obj, String propName, Object value) {
		try {
			obj.setPropertyValue(propName, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * @param obj
	 * @param propName
	 * @return Returns the object value of a property.
	 */
	public static Object getProperty(Object obj, String propName) {
		/**
		 * We need the XPropertySet interface.
		 */
		XPropertySet obj_XPropertySet;
		if (obj instanceof XPropertySet) {
			/**
			 * If the right interface was passed in, just typecaset it.
			 */
			obj_XPropertySet = (XPropertySet) obj;
		} else {
			/**
			 * Get a different interface to the drawDoc.
			 * The parameter passed in to us is the wrong interface to the
			 * object.
			 */
			obj_XPropertySet = QI.XPropertySet(obj);
		}
		
		/**
		 * Now just call our sibling using the correct interface.
		 */
		return getProperty(obj_XPropertySet, propName);
	}
	
	/**
	 * @param obj
	 * @param propName
	 * @return Return get property value
	 */
	public static Object getProperty(XPropertySet obj, String propName) {
		try {
			return obj.getPropertyValue(propName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	// ----------------------------------------------------------------------
	// Sugar coating for com.sun.star.container.XNamed interface.
	// ----------------------------------------------------------------------
	
	/**
	 * @param obj The service that implements XNamed.
	 * @param name The new name.
	 */
	public static void XNamed_setName(Object obj, String name) {
		XNamed xNamed = QI.XNamed(obj);
		xNamed.setName(name);
	}
	
	/**
	 * @param obj The service that implements XNamed.
	 * @return Return the name of an object
	 */
	public static String XNamed_getName(Object obj) {
		XNamed xNamed = QI.XNamed(obj);
		return xNamed.getName();
	}
}