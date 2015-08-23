/*
 * Created on Sep 2, 2004
 * $Id: OpenofficeWebSpellCheckerTest.java,v 1.1.2.1 2005/06/16 11:48:33 mike Exp $
 */
package org.zxframework.web.util;

import org.zxframework.web.util.OpenofficeWebSpellChecker;
import org.zxframework.web.util.WebSpellChecker;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.web.util.OpenofficeWebSpellChecker} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class OpenofficeWebSpellCheckerTest extends TestCase {

    /**
     * Constructor for OpenofficeWebSpellCheckerTest.
     * @param name The name of the test case
     */
    public OpenofficeWebSpellCheckerTest(String name) {
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
        TestSuite suite = new TestSuite(OpenofficeWebSpellChecker.class);
        suite.setName("OpenofficeWebSpellChecker Tests");
        return suite;
    }
    
    //------------------------ Tests
    
    /**
     * @throws Exception Thrown if testGetCheckerHeader fails
     */
    public void testGetCheckerHeader() throws Exception {
    	WebSpellChecker spel = new OpenofficeWebSpellChecker("testw");
        String result = spel.getCheckerHeader();
        assertEquals("textinputs[0] = decodeURIComponent('testw');\n", result);
    }

    /**
     * @throws Exception Thrown if testGetCheckerResults fails
     */
    public void testGetCheckerResults() throws Exception {
        WebSpellChecker spel = new OpenofficeWebSpellChecker("testw helleo");
        String results = spel.getCheckerResults();
        
        StringBuffer compare = new StringBuffer("words[0] = [];\n");
        compare.append("suggs[0] = [];\n");
        compare.append("words[0][0] = 'testw';\n");
        compare.append("suggs[0][0] = ['test', 'tests', 'testy', 'test w'];\n");
        compare.append("words[0][1] = 'helleo';\n");
        compare.append("suggs[0][1] = ['hello', 'heller', 'helled'];\n");
        
        assertEquals(compare.toString(), results);
    }
}