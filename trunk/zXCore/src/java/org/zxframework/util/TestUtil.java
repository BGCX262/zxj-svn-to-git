/*
 * Created on 03-Feb-2005, by Michael Brewer
 * $Id: TestUtil.java,v 1.1.2.3 2005/06/16 16:42:50 mike Exp $
 */
package org.zxframework.util;

import java.util.ResourceBundle;

import junit.framework.AssertionFailedError;

import org.zxframework.Environment;

/**
 * Utility class use in the unit tests.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class TestUtil {
    private static String config_file;
    
    /**
     * @return Returns the path to the zX Config file.
     */
    public static String getCfgPath() {
        if (config_file == null) {
            ResourceBundle prop = ResourceBundle.getBundle("zX");
            config_file = prop.getString(Environment.CONFIG_FILE);
        }
        
        return config_file;
    }
    
    /**
     * Called when code should not be reachable (because a test is expected to throw an exception). 
     * throws AssertionFailedError always.
     */
    public static void unreachable() {
    	throw new AssertionFailedError();
    }
}