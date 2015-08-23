/*
 * Created on Sep 5, 2004 by Michael Brewer
 * $Id: ExpressionTestSuite.java,v 1.1.2.1 2005/02/25 22:05:19 mike Exp $
 */
package org.zxframework.expression;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * The org.zxframework.expression test suite
 * 
 * @author Michael Brewer
 * @author Bertus Dispa
 * @author David Swann
 *
 * @version 0.0.1
 */
public class ExpressionTestSuite {

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
        TestSuite suite = new TestSuite("Test for org.zxframework.expression");
        //$JUnit-BEGIN$
        suite.addTest(ExprFHBOTest.suite());
        suite.addTest(ExprFHDefaultTest.suite());
        //$JUnit-END$
        return suite;
    }
}