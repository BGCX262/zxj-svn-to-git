package org.zxframework.logging;

import org.zxframework.ZX;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Tests the logging implementation.
 * 
 * @author Michael Brewer
 */
public class LogTest extends TestCase {
	
    /**
     * @param name The name of the test suite.
     */
    public LogTest(String name) {
        super(name);
    }
    
    /**
     * @param args Program parameters.
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    /**
     * @return Returns the test to run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(LogTest.class);
        suite.setName("Logging Tests");
        return suite;
    }
    
    //------------------------ Tests
    
    /**
     * Tests the basic functionality of the logging.
     * @throws Exception 
     */
    public void testLog() throws Exception {
    	Log log = LogFactory.getLog(LogTest.class);
    	log.fatal("Pre zX Config");
    	
    	ZX zx = new ZX(TestUtil.getCfgPath());
    	zx.log.fatal("Post zX Config");
    	
    	log = LogFactory.getLog(LogTest.class);
    	log.fatal("New log handle");
    	
    }
    
}