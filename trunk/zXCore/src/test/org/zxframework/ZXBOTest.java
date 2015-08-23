/*
 * Created on Oct 12, 2004 by mbrewer-admin
 * $Id: ZXBOTest.java,v 1.1.2.6 2006/07/17 16:13:47 mike Exp $
 */
package org.zxframework;

import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.ZXBO} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ZXBOTest extends TestCase {

    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public ZXBOTest(String name) {
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
        TestSuite suite = new TestSuite(ZXBOTest.class);
        suite.setName("ZXBO Tests");
        return suite;
    }
    
    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        zx = new ZX(TestUtil.getCfgPath());
        super.setUp();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        this.zx.cleanup();
        super.tearDown();
    }
    
    //------------------------------------------------------------------------ Test constructor
    
    //------------------------------------------------------------------------ Test Methods.
    
    /**
     * Tests whether the getPKValue is correct
     * @throws ZXException Thrown if test fails.
     */
    public void testGetPKValue() throws ZXException {
        // book has a primary key is id.
        ZXBO objBO = zx.createBO("test/cntry");
        objBO.setValue("id", "1");
        
        assertEquals("The Pk value does not match.", "1", objBO.getPKValue().getStringValue());
    }

    /**
     * Class under test for zXType.rc loadBO(String, String, boolean)
     * @throws ZXException Thrown if test fails
     */
    public void testLoadBOStringStringboolean() throws ZXException {
        
        // Set up
        ZXBO objBO = zx.createBO("test/cntry");
        objBO.setAutomatics("+");
        objBO.setValue("nme", "Monkey world");
        objBO.setValue("cde", "mk");
        objBO.setValue("dscrptn", "#now", true);
        objBO.insertBO();
        String strPK = objBO.getPKValue().getStringValue();
        
        objBO = zx.createBO("test/cntry");
        objBO.setPKValue(strPK);
        
        assertEquals(zXType.rc.rcOK, objBO.loadBO("*", "+", false));
        assertEquals("Monkey world", objBO.getValue("nme").getStringValue());
        
        // Clean up :
        assertEquals(zXType.rc.rcOK, objBO.deleteBO());
    }
    
    /**
     * @throws ZXException Thrown if test fails.
     */
    public void testFormatStack() throws ZXException {
        ZXBO objBO = zx.createBO("test/cntry");
        try {
        objBO.loadBO("xxxx", "+", false);
        } catch (Exception e ) {
            System.out.println(zx.trace.formatStack(true));
        }
    }
}