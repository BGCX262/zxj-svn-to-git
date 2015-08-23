/*
 * Created on Sep 6, 2004
 * $Id: DescriptorTest.java,v 1.1.2.3 2005/05/12 16:02:21 mike Exp $
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
public class DescriptorTest extends TestCase {
    
    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public DescriptorTest(String name) {
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
        TestSuite suite = new TestSuite(DescriptorTest.class);
        suite.setName("Descriptor Tests");
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
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        assertNotNull(new Descriptor());
        
        Constructor[] cons = Descriptor.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(Descriptor.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(Descriptor.class.getModifiers()));
    }
}
