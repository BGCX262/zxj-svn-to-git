/*
 * Created on Jun 12, 2005
 * $Id: DBDescriptorTest.java,v 1.1.2.2 2005/09/20 15:25:23 mike Exp $
 */
package org.zxframework.doc;

import java.io.File;

import org.zxframework.ZX;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.doc.DBDescriptor} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DBDescriptorTest extends TestCase {
	
    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public DBDescriptorTest(String name) {
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
        TestSuite suite = new TestSuite(DBDescriptorTest.class);
        suite.setName("DBDescriptor Tests");
        return suite;
    }
    
    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        zx = new ZX(TestUtil.getCfgPath());
        zx.getSession().connect("test","test");
        
        super.setUp();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    //------------------------  Tests
    
    /**
     * @throws Exception Thrown if test fails
     */
    public void testParsing() throws Exception {
    	String strFileName = zx.fullPathName(zx.getSettings().getTemplatesDir()
			    							 + File.separatorChar + "test/doc" 
			    							 + ".xml");
    	DBDescriptor objDBDescriptor = new DBDescriptor(strFileName);
    	System.out.println(objDBDescriptor.dumpAsXML());
    }
}