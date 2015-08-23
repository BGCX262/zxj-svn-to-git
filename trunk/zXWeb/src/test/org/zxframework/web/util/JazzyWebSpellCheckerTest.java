/*
 * Created on Sep 2, 2004
 * $Id: JazzyWebSpellCheckerTest.java,v 1.1.2.1 2005/06/16 11:48:32 mike Exp $
 */
package org.zxframework.web.util;

import org.zxframework.web.util.JazzyWebSpellChecker;
import org.zxframework.web.util.WebSpellChecker;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Unit tests {@link org.zxframework.web.util.JazzyWebSpellChecker} methods.
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class JazzyWebSpellCheckerTest extends TestCase {

    /**
     * Constructor for JazzyWebSpellCheckerTest.
     * 
     * @param name The name of the test case
     */
    public JazzyWebSpellCheckerTest(String name) {
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
        TestSuite suite = new TestSuite(JazzyWebSpellCheckerTest.class);
        suite.setName("JazzyWebSpellChecker Tests");
        return suite;
    }
    
    //------------------------ Tests
    
    /**
     * @throws Exception Thrown if testGetCheckerHeader fails
     */
    public void testGetCheckerHeader() throws Exception {
    	WebSpellChecker spel = new JazzyWebSpellChecker("testw");
        String result = spel.getCheckerHeader();
        assertEquals("textinputs[0] = decodeURIComponent('testw');\n", result);
    }

    /**
     * @throws Exception Thrown if testGetCheckerResults fails
     */
    public void testGetCheckerResults() throws Exception {
        WebSpellChecker spel = new JazzyWebSpellChecker("testing the wordz and twize");
        String results = spel.getCheckerResults();
        
        StringBuffer compare = new StringBuffer("words[0] = [];\n");
        compare.append("suggs[0] = [];\n");
        compare.append("words[0][0] = 'wordz';\n");
        compare.append("suggs[0][0] = ['words', 'word', 'wordy'];\n");
        compare.append("words[0][1] = 'twize';\n");
        compare.append("suggs[0][1] = ['twice', 'twine'];\n");
        
        assertEquals(compare.toString(), results);
    }
}