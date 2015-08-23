/*
 * Created on Sep 16, 2004
 * $Id: DirecotorHandlerTest.java,v 1.1.2.4 2005/04/08 09:12:59 mike Exp $
 */
package org.zxframework.director;

import org.zxframework.ZX;
import org.zxframework.ZXException;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.director.DirectorHandler} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class DirecotorHandlerTest extends TestCase {

    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public DirecotorHandlerTest(String name) {
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
        TestSuite suite = new TestSuite(DirecotorHandlerTest.class);
        suite.setName("DirecotorHandlerTest Tests");
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
        zx.cleanup();
        super.tearDown();
    }
    
    //-----------------------------------------------  Tests
    
    /**
     * Tests whether the director parser is working fine. 
     * 
     * @throws ZXException Thrown if testResolve fails.
     */
    public void testResolve() throws ZXException {
        
        // #eval.<#pref.<#qs.-prefPrinter>>
        zx.getQuickContext().setEntry("-test", "-hello");
        zx.getQuickContext().setEntry("-hello", "Hello world");
        assertEquals("Hello world", zx.resolveDirector("#eval.<#qs.<#qs.-test>>"));
        
        zx.getQuickContext().setEntry("-s", "100");
        
        assertEquals("100", zx.resolveDirector("#qs.-s#"));
        assertEquals("zXStdPopup('100', 'fm/lwPopup', 'start', '')", zx.resolveDirector("zXStdPopup('#qs.-s#', 'fm/lwPopup', 'start', '')"));
        
        // Testing inline directors
        assertEquals("xc100xc", zx.resolveDirector("xc<#qs.-s#>xc"));
        assertEquals("zXStdPopup('100', 'fm/lwPopup', 'start', '')", zx.resolveDirector("zXStdPopup('<#qs.-s#>', 'fm/lwPopup', 'start', '')"));
        
        zx.getQuickContext().setEntry("-me", "zxUsrPrf");
        zx.getQuickContext().setEntry("-le", "zxOffce");
        assertEquals("zxoffce", zx.resolveDirector("#fkAttrName.<#qs.-me>.<#qs.-le>"));
    }
    
}