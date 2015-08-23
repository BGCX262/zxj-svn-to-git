/*
 * Created on 04-Feb-2005, by Michael Brewer
 * $Id: PFDirectorTest.java,v 1.1.2.4 2006/07/17 14:04:04 mike Exp $
 */
package org.zxframework.web;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.ZX;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit tests {@link org.zxframework.web.PFDirector}.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * @since 1.5
 * @version 0.0.1
 */
public class PFDirectorTest extends TestCase {

    private ZX zx;
    
    /**
     * @param name Name of the unit test.
     */
    public PFDirectorTest(String name) {
        super(name);
    }
    
    /**
     * @param args Arguments.
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(PFDirectorTest.class);
    }
    
    /**
     * @return Returns the test to run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(PFDirectorTest.class);
        suite.setName("PFDirector Tests");
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
    
    //------------------------------------------------- Tests constructors 
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        assertNotNull(new PFDirector());
        
        Constructor[] cons = PFDirector.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        
        assertEquals(true, Modifier.isPublic(PFDirector.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(PFDirector.class.getModifiers()));
    }
    
    //------------------------------------------------ Test Methods
    
    /**
     * Tests constructQSEntry.
     * 
     * @throws Exception Thrown if testConstructQSEntry failes
     */
    public void testConstructQSEntry() throws Exception {
        PageBuilder objPageBuilder = new PageBuilder();
        Pageflow objPageflow = new Pageflow();
        objPageflow.setPage(objPageBuilder);
        PFDirector objPFDirector = new PFDirector();
        assertEquals("", objPageflow.constructQSEntry(objPFDirector));
        objPFDirector.setSource("test&");
        assertEquals("test&", objPageflow.constructQSEntry(objPFDirector));
        objPFDirector.setDestination("hello");
        assertEquals("hello=test%26", objPageflow.constructQSEntry(objPFDirector));
    }
    
    /**
     * Class under test for PFObject clone().
     */
    public void testClone() {
    	// Ignore
    }
}
