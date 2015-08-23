package org.zxframework.property;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.ZX;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 * Unit tests {@link Property} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class PropertyTest extends TestCase {

    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public PropertyTest(String name) {
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
        TestSuite suite = new TestSuite(PropertyTest.class);
        suite.setName("Property Tests");
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
    
    //------------------------ Test constructors
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        Constructor[] cons = Property.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        // Test to see if both constructors are public.
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        
        assertEquals(true, Modifier.isPublic(Property.class.getModifiers()));
        assertEquals(true, Modifier.isAbstract(Property.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(Property.class.getModifiers()));
    }
    
    /**
     * Tests Property type conversions.
     * 
     * @throws Exception
     */
    public void testConversions() throws Exception {
    	Property objProp = new DoubleProperty(1.5);
    	assertEquals(1, objProp.longValue());
    	assertEquals(1.5, objProp.doubleValue(), 0);
    	assertEquals("1.5", objProp.strValue());
    	
    	objProp = new LongProperty(1);
    	assertEquals(1, objProp.longValue());
    	assertEquals(1.0, objProp.doubleValue(), 0);
    	assertEquals("1", objProp.strValue());
    }
}