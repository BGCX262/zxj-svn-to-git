/*
 * Created on Aug 30, 2004
 * $Id: UtilTestSuite.java,v 1.1.2.2 2005/06/16 11:48:07 mike Exp $
 */
package org.zxframework.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * The org.zxframework.util test suite
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class UtilTestSuite {

    /**
     * @param args Program parameters.
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    /**
     * @return Returns the test suite.
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.zxframework.util");
        //$JUnit-BEGIN$
        suite.addTest(DateUtilTest.suite());
        suite.addTest(EntitiesTest.suite());
        suite.addTest(PasswordServiceTest.suite());
        suite.addTest(StringEscapeUtilsTest.suite());
        suite.addTest(StringUtilTest.suite());
        suite.addTest(XMLGenTest.suite());
        // suite.addTest(EntitiesPerformanceTest.suite()); // Skip performance tests
        //$JUnit-END$
        return suite;
    }
}
