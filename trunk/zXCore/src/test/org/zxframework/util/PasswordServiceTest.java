/*
 * Created on Aug 30, 2004
 * $Id: PasswordServiceTest.java,v 1.1.2.1 2005/05/12 15:52:42 mike Exp $
 */
package org.zxframework.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.util.PasswordService} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class PasswordServiceTest extends TestCase {

    /**
     * Constructor for PasswordServiceTest.
     * @param name The name of the test case
     */
    public PasswordServiceTest(String name) {
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
        TestSuite suite = new TestSuite(PasswordServiceTest.class);
        suite.setName("PasswordService Tests");
        return suite;
    }
    
    //----------------------------------------------------------------- Test constructors
    
    /**
     * Test the available constructors.
     */
    public void testConstructor() {
        Constructor[] cons = PasswordService.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPrivate(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(PasswordService.class.getModifiers()));
        assertEquals(true, Modifier.isFinal(PasswordService.class.getModifiers()));
    }
    
    /**
     * Tests the encypt method.
     * @throws Exception Thrown if testEncrypt fails
     */
    public void testEncrypt() throws Exception {
        assertEquals("", PasswordService.getInstance().encrypt(null));
        assertEquals("2jmj7l5rSw0yVb/vlWAYkK/YBwk=", PasswordService.getInstance().encrypt(""));
        assertEquals("qUqP5cyxm6YcTAhz05Hph5gvu9M=", PasswordService.getInstance().encrypt("test"));
    }
}