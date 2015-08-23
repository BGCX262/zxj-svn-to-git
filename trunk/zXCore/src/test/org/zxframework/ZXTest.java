/*
 * Created on Aug 30, 2004
 * $Id: ZXTest.java,v 1.1.2.9 2006/07/17 16:13:47 mike Exp $
 */
package org.zxframework;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.ZX} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class ZXTest extends TestCase {

    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public ZXTest(String name) {
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
        TestSuite suite = new TestSuite(ZXTest.class);
        suite.setName("ZX Tests");
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
        super.tearDown();
    }
    
    //------------------------------------------------------------------------ Test constructor 
    
    /**
     * Test the available constructors.
     * @throws ZXException Thrown if testConstructor fails
     */
    public void testConstructor() throws ZXException {
        assertNotNull(new ZX(TestUtil.getCfgPath()));
        
        Constructor[] cons = ZX.class.getDeclaredConstructors();
        assertEquals(2, cons.length);
        
        if (Modifier.isPrivate(cons[0].getModifiers())) {
            assertEquals(0, cons[0].getParameterTypes().length);
            assertEquals(1, cons[1].getParameterTypes().length);
            
        } else {
            assertEquals(1, cons[0].getParameterTypes().length);
            assertEquals(0, cons[1].getParameterTypes().length);
            
        }
        
        assertEquals(true, Modifier.isPublic(ZX.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(ZX.class.getModifiers()));
        
    }
    
    /**
     * Class under test for void ZX(String)
     * @throws Exception Thrown if testZXString fails
     */
    public void testZXString() throws Exception {
        
        // Testing the descriptor :
        assertEquals("zXTemplate", zx.getAppName());
        assertEquals("cfg\\bo\\", zx.getBoDir());
        assertEquals("cfg\\pf\\", zx.getPageflowDir());
        assertEquals("cfg\\qry\\", zx.getQueryDir());
        assertEquals("cfg\\tmplts\\", zx.getSettings().getTemplatesDir());
        assertEquals("tmp\\", zx.getSettings().getTmpDir());
        
        assertEquals(25, zx.getSessionTimeOut());
        assertEquals(zXType.runMode.rmTest, zx.getRunMode());
        assertEquals("test", zx.getSettings().getAppMode());
        
        assertEquals("v0.1", zx.getSettings().getAppVersion());
        
//        SimpleDateFormat df = new SimpleDateFormat(zx.getTimestampFormat());
//        Date date = df.parse("12Feb2004 12:30:24");
//        assertEquals(date,zx.getAppDate());
        
        assertEquals("EN", zx.getLanguage());
        
        // assertEquals("..//..//..//test2.log", zx.getLogFileName());
        assertEquals(true, zx.getSettings().isLogActive());
        assertEquals(true, zx.getSettings().isLogAppend());
        assertEquals("ERROR", zx.getSettings().getLogLevel());
        assertEquals("org.zxframework.logging.impl.LogFactoryImpl", zx.getSettings().getLogFactory());
        assertEquals("%d{ISO8601} --  %p -- %c : %l  - %m%n", zx.getSettings().getLogFormat());
        
    }
    
    /**
     * 
     */
    public void testConfigValue() {
    	//
    }

    /**
     * 
     */
    public void testConfigXMLNode() {
    	//
    }

    /**
     * 
     */
    public void testGetAuditAttributes() {
    	//
    }

    /**
     * 
     */
    public void testGetFileAsStream() {
    	//
    }

    /**
     * Class under test for ZXBO createBO(String)
     */
    public void testCreateBOString() {
    	//
    }

    /**
     * Class under test for ZXBO createBO(String, boolean)
     */
    public void testCreateBOStringboolean() {
    	//
    }

    /**
     * 
     */
    public void testGetCacheZXBO() {
    	//
    }

    /**
     * Class under test for ZXObject createObject(String)
     */
    public void testCreateObjectString() {
    	//
    }

    /**
     * Class under test for ZXObject createObject(Class)
     */
    public void testCreateObjectClass() {
    	//
    }

    /**
     * 
     */
    public void testParseAuditAttributes() {
    	//
    }

    /**
     * 
     */
    public void testResolveDirector() {
    	//
    }

    /**
     * 
     */
    public void testParseLogSettings() {
    	//
    }

    /**
     * 
     */
    public void testParseTraceSettings() {
    	//
    }
    
    /**
     * Tests the parsing of the Required zX Version setting.
     */
    public void testParseRequiredzXVersion() {
        
        // For a 1.4 project
        //assertEquals(1, zx.getRequiredzXVersionMajor());
        //assertEquals(4, zx.getRequiredzXVersionMinor());
        
        // Set to 1.5  eg : <requiredzXVersion>1.5</requiredzXVersion>
        assertEquals(1, zx.getSettings().getRequiredzXVersionMajor());
        assertEquals(5, zx.getSettings().getRequiredzXVersionMinor());
        
    }
    
    /**
     * Tests version compatibility checker.
     */
    public void testzXVersionSupport() {
        
        // zX config should be set to 1.5
        assertEquals(true, zx.zXVersionSupport(0,5));
        assertEquals(true, zx.zXVersionSupport(1,5));
        assertEquals(false, zx.zXVersionSupport(5,5));
        assertEquals(true, zx.zXVersionSupport(1,4));
        assertEquals(false, zx.zXVersionSupport(1,6));
        
    }
    
}