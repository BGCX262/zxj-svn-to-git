/*
 * Created on May 11, 2005
 * $Id: ReflectUtil.java,v 1.1.2.2 2006/07/17 16:16:38 mike Exp $
 */
package org.zxframework.util;

/**
 * Reflection tools.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 */
public class ReflectUtil {
    
    /**
     * @param name The fully qualified name of the class.
     * @return Return a constructed classname.
     * @throws ClassNotFoundException Thrown if we do not find the class.
     */
    public static Class classForName(String name) throws ClassNotFoundException {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if(contextClassLoader!=null) {
                return contextClassLoader.loadClass(name);
            }
            
            return Class.forName(name);
            
        } catch (Exception e) {
            return Class.forName(name);
        }
    }
}