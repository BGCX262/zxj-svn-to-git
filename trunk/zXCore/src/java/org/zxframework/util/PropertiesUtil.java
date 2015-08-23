/*
 * Created on Aug 12, 2004
 * $Id: PropertiesUtil.java,v 1.1.2.2 2006/07/17 16:16:47 mike Exp $
 */
package org.zxframework.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Properties tools.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 */
public final class PropertiesUtil {
    
    /** 
     * Hide Default constructor. 
     **/
    private PropertiesUtil() {
    	super();
    }
    
    /**
     * @param property The property you want to retrieve
     * @param properties The properties object you want to read it from.
     * @return Returns the boolean value of the propeties.
     */
    public static boolean getBoolean(String property, Properties properties) {
        return Boolean.valueOf(properties.getProperty(property)).booleanValue();
    }

    /**
     * @param property The property you want to retrieve
     * @param properties The properties object you want to read it from.
     * @param defaultValue The value to default.
     * @return Returns the boolean value, if null return the defaultValue.
     */
    public static boolean getBoolean(String property, Properties properties, boolean defaultValue) {
        String setting = properties.getProperty(property);
        return (setting==null) ? defaultValue : Boolean.valueOf(setting).booleanValue();
    }
    
    /**
     * @param property The property you want to retrieve
     * @param properties The properties object you want to read it from.
     * @param defaultValue The value to default.
     * @return Returns the boolean value, if null return the defaultValue.
     */
    public static int getInt(String property, Properties properties, int defaultValue) {
        String propValue = properties.getProperty(property);
        return (propValue==null) ? defaultValue : Integer.parseInt(propValue);
    }
    
    /**
     * @param property The property you want to retrieve
     * @param properties The properties object you want to read it from.
     * @param defaultValue The value to default.
     * @return Returns the String value, if null return the defaultValue.
     */
    public static String getString(String property, Properties properties, String defaultValue) {
        String propValue = properties.getProperty(property);
        return (propValue==null) ? defaultValue : propValue;
    }

    /**
     * @param property The property you want to retrieve
     * @param properties The properties object you want to read it from.
     * @return Returns the Integer value.
     */
    public static Integer getInteger(String property, Properties properties) {
        String propValue = properties.getProperty(property);
        return (propValue==null) ? null : Integer.valueOf(propValue);
    }

    /**
     * @param property The property to retrieve.
     * @param delim The delim.
     * @param properties The properties object.
     * @return Returns a Map of the properties values.
     */
    public static Map toMap(String property, String delim, Properties properties) {
        Map map = new HashMap();
        String propValue = properties.getProperty(property);
        if (propValue!=null) {
            StringTokenizer tokens = new StringTokenizer(propValue, delim);
            while ( tokens.hasMoreTokens() ) {
                map.put(
                    tokens.nextToken(),
                    tokens.hasMoreElements() ? tokens.nextToken() : ""
                );
            }
        }
        return map;
    }
    
    /**
     * replace a property by a starred version
     * 
     * @param props properties to check
     * @param key proeprty to mask
     * @return cloned and masked properties
     */
    public static Properties maskOut(Properties props, String key) {
        Properties clone = (Properties) props.clone();
        if (clone.get(key) != null) {
            clone.setProperty(key, "****");
        }
        return clone;
    }
}