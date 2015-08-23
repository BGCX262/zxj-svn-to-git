/*
 * Created on Sep 2, 2004
 * $Id: StringPropertyTest.java,v 1.1.2.4 2006/01/27 18:12:56 mike Exp $
 */
package org.zxframework.property;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.zxframework.UserProfile;
import org.zxframework.ZX;
import org.zxframework.ZXException;
import org.zxframework.util.TestUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.property.StringProperty} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 * 
 * @version 0.0.1
 */
public class StringPropertyTest extends TestCase {

    private ZX zx;
    
    /**
     * @param name The name of the test suite.
     */
    public StringPropertyTest(String name) {
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
        TestSuite suite = new TestSuite(StringPropertyTest.class);
        suite.setName("StringPropertyTest Tests");
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
    
    //------------------------ Tests constructors 
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        
        assertNotNull(new StringProperty());
        
        Constructor[] cons = StringProperty.class.getDeclaredConstructors();
        assertEquals(3, cons.length);
        
        // Test to see if both constructors are public.
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(cons[1].getModifiers()));
        assertEquals(true, Modifier.isPublic(cons[2].getModifiers()));
        
        assertEquals(true, Modifier.isPublic(StringProperty.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(StringProperty.class.getModifiers()));
        
    }
    
    /**
     * Test the setValue method.
     * 
     * @throws ZXException Thrown if testSetValue fails
     */
    public void testSetValue() throws ZXException {
        
        UserProfile userProfile = (UserProfile)this.zx.createBO("zxUsrPrf");
        userProfile.setValidate(true);
        userProfile.setValue("crrntPsswrd", "te");
        
    }
}